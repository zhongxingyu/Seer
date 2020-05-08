 package net.cyklotron.cms.modules.actions.poll;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpSession;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.captcha.CaptchaService;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.confirmation.EmailConfirmationService;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.poll.AnswerResource;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.VoteResource;
 import net.cyklotron.cms.poll.VoteResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.util.OfflineLinkRenderingService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: RespondPoll.java,v 1.7 2007-02-25 14:14:49 pablo Exp $
  */
 public class SendVote
     extends BasePollAction
 {
 
     private EmailConfirmationService emailConfirmationRequestService;
 
     private CaptchaService captchaService;
 
     private final OfflineLinkRenderingService linkRenderingService;
 
     public SendVote(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PollService pollService, WorkflowService workflowService,
         EmailConfirmationService emailConfirmationRequestService, CaptchaService captchaService,
         OfflineLinkRenderingService linkRenderingService)
     {
         super(logger, structureService, cmsDataFactory, pollService, workflowService);
         this.emailConfirmationRequestService = emailConfirmationRequestService;
         this.captchaService = captchaService;
         this.linkRenderingService = linkRenderingService;
     }
 
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         HttpSession session = httpContext.getRequest().getSession();
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
 
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
         Long answerId = parameters.getLong("answer", -1);
         if(answerId == -1)
         {
             templatingContext.put("result", "answer_not_found");
             return;
         }
         String email = parameters.get("email", "");
         if(!email.matches("[a-zA-Z0-9.-_]+@[a-zA-Z0-9.-_]+.[a-zA-Z]{1,4}"))
         {
             templatingContext.put("result", "invalid_email");
             return;
         }
         if(screenConfig.getBoolean("add_captcha", false)
             && !captchaService.checkCaptcha(httpContext, (RequestParameters)parameters))
         {
             templatingContext.put("result", "invalid_captcha_verification");
             return;
         }
 
         try
         {
             VoteResource voteResource = VoteResourceImpl.getVoteResource(coralSession, vid);
             Set<String> voteEmails = pollService.getBallotsEmails(coralSession, voteResource);
 
             if(pollService.hasVoted(httpContext, templatingContext, voteResource)
                 || voteEmails.contains(email))
             {
                 templatingContext.put("already_voted", Boolean.TRUE);
                 templatingContext.put("result", "already_responded");
                 return;
             }
             Resource[] answersResources = coralSession.getStore().getResource(voteResource);
             for(int i = 0; i < answersResources.length; i++)
             {
                 AnswerResource answerResource = (AnswerResource)answersResources[i];
                 if(answerId.equals(answerResource.getId()))
                 {
                     String confirmationRequest = emailConfirmationRequestService
                         .createEmailConfirmationRequest(coralSession, email, answerId.toString());
                     I18nContext i18nContext = I18nContext.getI18nContext(context);
                     Template template = pollService.getVoteConfiramationTicketTemplate(
                         voteResource, i18nContext.getLocale());
                     LinkRenderer linkRenderer = linkRenderingService.getLinkRenderer();
                     Map<String, Object> entries = new HashMap<String, Object>();
                     entries.put("vote", voteResource);
                     emailConfirmationRequestService.sendConfirmationRequest(confirmationRequest,
                         voteResource.getSenderAddress(), email, entries, cmsData.getNode(),
                         template, "PLAIN", linkRenderer, coralSession);
                     setCookie(httpContext, vid, answerId);
                     break;
                 }
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
            templatingContext.put("trace", new StackTrace(e));
             logger.error("Exception in poll,SendVote action", e);
             return;
         }
 
         templatingContext.put("result", "responded_successfully");
         templatingContext.put("already_voted", Boolean.TRUE);
     }
 
     private void setCookie(HttpContext httpContext, Integer vid, Long answerId)
     {
 
         String cookieKey = "vote_" + vid;
         Cookie cookie = new Cookie(cookieKey, answerId.toString());
         cookie.setMaxAge(30 * 24 * 3600);
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
