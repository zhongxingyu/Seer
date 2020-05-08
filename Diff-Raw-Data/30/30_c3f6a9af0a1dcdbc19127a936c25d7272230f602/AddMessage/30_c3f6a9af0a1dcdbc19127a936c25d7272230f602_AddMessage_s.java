 package net.cyklotron.cms.modules.actions.forum;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.encodings.HTMLEntityEncoder;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.utils.StringUtils;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.captcha.CaptchaService;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.CmsTool;
 import net.cyklotron.cms.forum.DiscussionResource;
 import net.cyklotron.cms.forum.DiscussionResourceImpl;
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.forum.MessageResourceImpl;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 
 /**
  *
  * @author <a href="mailo:pablo@ngo.pl">Pawel Potempski</a>
  * @version $Id: AddMessage.java,v 1.7 2007-02-25 14:14:11 pablo Exp $
  */
 public class AddMessage
     extends BaseForumAction
 {
     protected CaptchaService captchaService;
     
     public AddMessage(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, ForumService forumService, WorkflowService workflowService, CaptchaService captchaService)
     {
         super(logger, structureService, cmsDataFactory, forumService, workflowService);
         this.captchaService = captchaService;
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             Subject subject = coralSession.getUserSubject();
             String instanceName = parameters.get("ci","");
             templatingContext.put("result_scope", "forum_"+instanceName);
             String name = parameters.get("name","");
             if(name.equals(""))
             {
                 templatingContext.put("result","illegal_message_name");
                 parameters.set("state", "NewMessage");
                 return;
             }
             HTMLEntityEncoder encoder = new HTMLEntityEncoder();
             name = encoder.encodeAttribute(name, httpContext.getEncoding());
             long parentId = parameters.getLong("parent", -1);
             long discussionId = parameters.getLong("did", -1);
             long resourceId = parameters.getLong("resid", -1);
             if((parentId == -1 || discussionId == -1) && resourceId == -1)
             {
                 templatingContext.put("result","parameter_not_found");
                 parameters.set("state", "NewMessage");
                 return;
             }
 
             DiscussionResource discussion = null;
             Resource parent = null;
             if(parentId != -1)
             {
                 discussion = DiscussionResourceImpl.getDiscussionResource(coralSession,discussionId);
                 parent = coralSession.getStore().getResource(parentId);
             }
             else
             {
                 Resource res = coralSession.getStore().getResource(resourceId);
                 SiteResource site = CmsTool.getSite(res);
                 ForumResource forum = forumService.getForum(coralSession, site);
                 discussion = forumService.createCommentary(coralSession, forum, "comments/"+Long.toString(resourceId), res);
                 parent = discussion;
                 parameters.set("did", discussion.getIdString());
             }
             
             if(parent instanceof MessageResource && "locked".equals(((MessageResource)parent).getState().getName()))
             {
                 templatingContext.put("result","parent_message_locked");
                 parameters.set("state", "NewMessage");
                 return;
             }
             
             if(discussion.getState().getName().equals("hidden"))
             {
 				templatingContext.put("result","hidden_discussion");
 				parameters.set("state", "NewMessage");
 				return;
             }
             
            CmsData cmsData = cmsDataFactory.getCmsData(context);
            ForumResource forum = forumService.getForum(coralSession, cmsData.getSite());
            Parameters config = cmsData.getComponent(instanceName).getConfiguration();
            if(config == null)
             {
                config = cmsData.getEmbeddedScreenConfig();
            }

            if(captchaService.isCaptchaRequired(config, coralSession.getUserSubject().getPrincipal())
                && !captchaService.checkCaptcha(httpContext, (RequestParameters)parameters))
            {
                templatingContext.put("result", "invalid_captcha_verification");
                parameters.set("state", "NewMessage");
                return;
             }
             
             String content = parameters.get("content","");
             content = StringUtils.wrap(content, 78);
             content = encoder.encodeAttribute(content, httpContext.getEncoding());
             int priority = parameters.getInt("priority", 0);
 
             MessageResource message = MessageResourceImpl.
                 createMessageResource(coralSession, name, parent, httpContext.getEncoding(),
                                       content, discussion, priority, name);
             String author = parameters.get("author","");
             String email = parameters.get("email","");
             message.setAuthor(author);
             message.setEmail(email);
             message.setSticky(false);
             message.update();
             
             // workflow
             if(discussion.getForum().getSite() != null)
             {
                 workflowService.assignState(coralSession, discussion.getForum().getSite().getParent().getParent(), message);
             }
             else
             {
                 workflowService.assignState(coralSession, null, message);
             }
 			if(discussion.getState().getName().equals("open"))
 			{
 				workflowService.performTransition(coralSession, message, "accept", subject);		
                 templatingContext.put("result","added_successfully");
 			}
             else
             {
                 templatingContext.put("result","added_for_moderation_successfully");
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             parameters.set("state", "NewMessage");
             logger.error("ForumException: ",e);
             return;
         }
     }
 
 
     public boolean checkAccessRights(Context context) 
     throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         if(!cmsData.isApplicationEnabled("forum"))
         {
             logger.debug("Application 'forum' not enabled in site");
             return false;
         }
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         Permission forumAdd = coralSession.getSecurity().getUniquePermission("cms.forum.add");
         long parentId = parameters.getLong("parent", -1);
         if(parentId != -1)
         {
             try
             {
                 Resource parent = coralSession.getStore().getResource(parentId);
                 return coralSession.getUserSubject().hasPermission(parent, forumAdd);
             }   
             catch(Exception e)
             {
                 throw new ProcessingException("failed to check access rights", e);
             }    
         }
         else
         {
             long resId = parameters.getLong("resid", -1);
             if(resId == -1)
             {
                 logger.error("forum,AddMessage action: parent nor resid undefined");
                 return false; 
             }
             else
             {
                 try
                 {
                     Resource res = coralSession.getStore().getResource(resId);
                     SiteResource site = CmsTool.getSite(res);
                     ForumResource forum = forumService.getForum(coralSession, site);
                     Resource[] r = coralSession.getStore().getResource(forum, "discussions");
                     if(r.length == 0)
                     {
                         return coralSession.getUserSubject().hasPermission(forum, forumAdd);
                     }
                     else
                     {
                         return coralSession.getUserSubject().hasPermission(r[0], forumAdd);
                     }
                 }
                 catch (Exception e)
                 {
                     throw new ProcessingException("failed to check access rights", e);
                 }
             }
         }
     }
 }
