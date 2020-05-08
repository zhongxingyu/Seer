 package net.cyklotron.cms.modules.actions.httpfeed;
 
 import java.net.URI;
 
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
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.httpfeed.HttpFeedResource;
 import net.cyklotron.cms.httpfeed.HttpFeedResourceImpl;
 import net.cyklotron.cms.httpfeed.HttpFeedService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Action for adding http feeds to the site.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: AddFeed.java,v 1.5 2005-05-18 11:41:07 zwierzem Exp $
  */
 public class AddFeed extends BaseHttpFeedAction
 {
     
     public AddFeed(Logger logger, StructureService structureService, CmsDataFactory cmsDataFactory,
         HttpFeedService httpFeedService)
     {
         super(logger, structureService, cmsDataFactory, httpFeedService);
         
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         Subject subject = coralSession.getUserSubject();
 
         FeedParams params = new FeedParams(parameters);
         if(!check(params, templatingContext, context))
         {
             return;
         }
 
         try
         {
             HttpFeedResource feed = getFeedResource(context, params, coralSession);
 
             feed.setUrl(params.url);
             feed.setInterval(params.interval);
             feed.setDescription(params.description);
             feed.update();
         }
         catch(Exception e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("problem adding feed", e);
             return;
         }
 
         templatingContext.put("result", successResult());
        mvcContext.setView("httpfeed.FeedList");
     }
 
     public class FeedParams
     {
         String name;
         String url;
         int interval;
         String description;
 
         public FeedParams(Parameters parameters)
         {
             name = parameters.get("name","");
             url = parameters.get("url","");
             interval = parameters.getInt("interval", 60);
             description = parameters.get("description","");
         }
     }
 
     protected boolean check(FeedParams params, TemplatingContext templatingContext,
         Context context)
     throws ProcessingException
     {
         if(params.name.equals(""))
         {
             templatingContext.put("result","feed_name_empty");
             return false;
         }
         if(params.url.equals(""))
         {
             templatingContext.put("result","feed_url_empty");
             return false;
         }
         try
         {
             new URI(params.url);
         }
         catch(Exception e)
         {
             templatingContext.put("result","feed_url_bad");
             return false;
         }
         return true;
     }
 
     protected HttpFeedResource getFeedResource(Context context, FeedParams params, CoralSession coralSession)
     throws Exception
     {
         Resource parent = httpFeedService.getFeedsParent(coralSession, getSite(context));
         HttpFeedResource feed = HttpFeedResourceImpl.createHttpFeedResource(coralSession,
                             params.name, parent);
         feed.setFailedUpdates(0);
 
         return feed;
     }
 
     protected String successResult()
     {
         return "added_successfully";
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return checkPermission(context, coralSession, "cms.httpfeed.add");
     }
 }
