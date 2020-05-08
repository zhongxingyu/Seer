 package net.cyklotron.cms.modules.views.poll;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.confirmation.ConfirmationRequestException;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestResource;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestService;
 import net.cyklotron.cms.modules.views.BaseSkinableScreen;
 import net.cyklotron.cms.poll.PollException;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.VoteResource;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 
 public class Voting
     extends BaseSkinableScreen
 {
     private EmailConfirmationRequestService emailConfirmationRequestService;
 
     private PollService pollService;
 
     public Voting(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager,
         EmailConfirmationRequestService emailConfirmationRequestService, PollService pollService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
 
         this.emailConfirmationRequestService = emailConfirmationRequestService;
         this.pollService = pollService;
     }
 
     @Override
     public String getState()
         throws ProcessingException
     {
         try
         {
             Parameters parameters = RequestParameters.getRequestParameters(context);
             CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
             TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
             
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             VoteResource vote = pollService.getVote(coralSession, screenConfig);
             boolean enableResults = screenConfig.getBoolean("enable_results", Boolean.FALSE);
 
             String state = (String)context.getAttribute(getClass().getName() + ".state");
             state = parameters.get("state", null);
             
            if("BallotSent".equals(state))
             {
                 try
                 {
                     String cookie = parameters.get("cookie", "");
                     EmailConfirmationRequestResource request = emailConfirmationRequestService
                         .getEmailConfirmationRequest(coralSession, cookie);
 
                     Set<String> voteEmails = pollService.getBallotsEmails(coralSession, vote);
                     if(request != null && !voteEmails.contains(request.getEmail()))
                     {
                         state = "Confirm";
                     }
                     else
                     {
                         state = "InvalidBallot";
                     }
                 }
                 catch(ConfirmationRequestException e)
                 {
                     state = "InvalidBallot";
                 }
             }
             else
             {
                 if(hasVoted())
                 {
                     templatingContext.put("already_voted", Boolean.TRUE);
                     if(enableResults)
                     {
                         state = "Results";
                     }
                     else
                     {
                         state = "BallotSent";
                     }
                 }
                 else
                 {
                     state = "Default";
                 }
             }
             return state;
         }
         catch(PollException e)
         {
             throw new ProcessingException("Vote not found." + e);
         }
     }
     
     public boolean hasVoted()
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         
         try
         {
             if(templatingContext.get("already_voted") != null
                 && ((Boolean)templatingContext.get("already_voted")).booleanValue())
             {
                 return true;
             }
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             VoteResource vote = pollService.getVote(coralSession, screenConfig);
             if(vote == null)
             {
                 return false;
             }
             return pollService.hasVoted(httpContext, templatingContext, vote);
         }
         catch(Exception e)
         {
             logger.error("Exception occured", e);
             return false;
         }
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         return true;
     }
 
     public void prepareDefault(Context context)
     throws ProcessingException
     {
         try
         {
             CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
             TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             VoteResource vote = pollService.getVote(coralSession, screenConfig);
             templatingContext.put("vote", vote);
             
             Map answers = new HashMap();
             pollService.prepareVoteMaps(coralSession, vote, answers, new HashMap(), new HashMap(), new HashMap());
             List answerKeys = new ArrayList();
             for(int i = 0; i< answers.size(); i++)
             {
                 answerKeys.add(new Integer(i));
             }
             templatingContext.put("answers", answers);
             templatingContext.put("answerKeys", answerKeys);
            
            boolean addCaptcha = screenConfig.getBoolean("add_captcha", Boolean.FALSE);
            templatingContext.put("add_captcha", addCaptcha);
            
         }
         catch(PollException e)
         {
             throw new ProcessingException("Vote not found." + e);
         }
     }
     
     public void prepareConfirm(Context context)
     throws ProcessingException
     {
 
     }
 
     public void prepareResults(Context context)
     throws ProcessingException
     {
         try
         {
             CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
             TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             VoteResource vote = pollService.getVote(coralSession, screenConfig);
             templatingContext.put("vote", vote);
 
             Map answers = new HashMap();
             Map resultMap= new HashMap();
             Map percentMap= new HashMap();
             Map ballotsMap= new HashMap();
             
             pollService.prepareVoteMaps(coralSession, vote, answers, resultMap, percentMap, ballotsMap);
             List answerKeys = new ArrayList();
             for(int i = 0; i< answers.size(); i++)
             {
                 answerKeys.add(new Integer(i));
             }
             templatingContext.put("answerKeys", answerKeys);
             templatingContext.put("results", resultMap);
             templatingContext.put("percent", percentMap);
             templatingContext.put("answers", answers);
         }
         catch(PollException e)
         {
             throw new ProcessingException("Vote not found." + e);
         }
     }
     
     public void prepareBallotSent(Context context)
     {
         // does nothing
     }
     
     public void prepareInvalidBallot(Context context)
     {
         // does nothing
     }
 }
