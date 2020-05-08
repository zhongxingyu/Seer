 package net.cyklotron.cms.modules.actions.poll;
 
 import java.util.Date;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpSession;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestResource;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestResourceImpl;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestService;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.poll.AnswerResource;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.VoteResource;
 import net.cyklotron.cms.poll.VoteResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  *
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: RespondPoll.java,v 1.7 2007-02-25 14:14:49 pablo Exp $
  */
 public class SendVote
     extends BasePollAction
 {
 
     private EmailConfirmationRequestService emailConfirmationRequestService;
     
     public SendVote(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PollService pollService, WorkflowService workflowService, EmailConfirmationRequestService emailConfirmationRequestService)
     {
         super(logger, structureService, cmsDataFactory, pollService, workflowService);
         this.emailConfirmationRequestService = emailConfirmationRequestService;    
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         HttpSession session = httpContext.getRequest().getSession();
        CmsData cmsData = cmsDataFactory.getCmsData(context);
         
         if(session == null || session.isNew())
         {
             templatingContext.put("result", "new_session");
             return;
         }
 
         Subject subject = coralSession.getUserSubject();
         int vid = parameters.getInt("vid", -1);
         if(vid == -1)
         {
             throw new ProcessingException("Vote id not found");
         }
         String email = parameters.get("email", "");
         if(email.trim().isEmpty())
         {
             templatingContext.put("result", "invalid_email");
             return;
         }
 
         try
         {
             VoteResource voteResource = VoteResourceImpl.getVoteResource(coralSession, vid);
             if(pollService.hasVoted(httpContext, templatingContext, voteResource))
             {
                 templatingContext.put("result", "already_responded");
                 return;
             }
             Resource[] answersResources = coralSession.getStore().getResource(voteResource);
             for(int i = 0; i < answersResources.length; i++)
             {
                 AnswerResource answerResource = (AnswerResource)answersResources[i];
                 Long answerId = parameters.getLong("answer_"+answerResource.getSequence(), -1);
                 if(answerId != -1)
                 {
                     String confirmationRequest = emailConfirmationRequestService.createEmailConfirmationRequest(coralSession, email, answerResource.getName());
                     emailConfirmationRequestService.send(coralSession, confirmationRequest, null, null, null);
                    setCookie(httpContext,vid, answerId, cmsData);
                     break;
                 }
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
             logger.error("Exception in poll,RespondVote action", e);
             return;
         }
 
 
         templatingContext.put("result", "responded_successfully");
         templatingContext.put("already_voted", Boolean.TRUE);
     }
         
    private void setCookie(HttpContext httpContext, Integer vid,Long answerId, CmsData cmsData)
     {
         
         String cookieKey = "vote_"+vid;
         Cookie cookie = new Cookie(cookieKey, answerId.toString());
         cookie.setMaxAge(30 * 24 * 3600);
        cookie.setDomain(cmsData.getSite().getName());
         cookie.setPath("/");
         httpContext.getResponse().addCookie(cookie);
     }
 	
     public boolean checkAccessRights(Context context)
     	throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         if(!cmsData.isApplicationEnabled("poll"))
         {
             logger.debug("Application 'poll' not enabled in site");
             return false;
         }
 	    return true;
 	}
 }
 
 
