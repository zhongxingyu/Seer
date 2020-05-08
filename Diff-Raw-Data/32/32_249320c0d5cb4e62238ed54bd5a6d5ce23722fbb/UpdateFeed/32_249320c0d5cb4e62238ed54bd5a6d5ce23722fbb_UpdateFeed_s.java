 package net.cyklotron.cms.modules.actions.httpfeed;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 
import net.cyklotron.cms.CmsDataFactory;
import net.cyklotron.cms.httpfeed.HttpFeedException;
import net.cyklotron.cms.httpfeed.HttpFeedResource;
import net.cyklotron.cms.httpfeed.HttpFeedService;
import net.cyklotron.cms.structure.StructureService;

 /**
  * Action for updating http feeds in the site.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: UpdateFeed.java,v 1.4 2005-03-08 10:52:24 pablo Exp $
  */
 public class UpdateFeed extends AddFeed
 {
     public UpdateFeed(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, HttpFeedService httpFeedService)
     {
         super(logger, structureService, cmsDataFactory, httpFeedService);
         
     }
     
     protected boolean check(FeedParams params, Parameters parameters, Context context,
         TemplatingContext templatingContext, CoralSession coralSession)
     throws ProcessingException
     {
         boolean result = super.check(params, templatingContext, context);
         if(!result)
         {
             return false;
         }
         
         HttpFeedResource currentFeed = getFeed(coralSession, parameters);
         if(currentFeed.getName().equals(params.name))
         {
             return true;
         }
 
         Resource[] res;
         try
         {
             Resource parent = httpFeedService.getFeedsParent(coralSession, getSite(context));
             res = coralSession.getStore().getResource(parent, params.name);
         }
         catch(HttpFeedException e)
         {
             throw new ProcessingException("Problems getting http feed parent for site", e);
         }
             
         if(res.length == 0)
         {
             return true;
         }
         else if(res.length == 1)
         {
             templatingContext.put("result","feed_with_same_name_exists");
             return false;
         }
         else
         {
             templatingContext.put("result","many_feeds_with_same_name");
             return false;
         }
     }
     
    protected HttpFeedResource getFeedResource(Parameters parameters, FeedParams params, CoralSession coralSession)
     throws Exception
     {
         HttpFeedResource feed = getFeed(coralSession, parameters);
         coralSession.getStore().setName(feed, params.name);
         return feed;
     }
 
     protected String successResult()
     {
         return "updated_successfully";
     }
     
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return checkPermission(context, coralSession, "cms.httpfeed.modify");
     }
 }
