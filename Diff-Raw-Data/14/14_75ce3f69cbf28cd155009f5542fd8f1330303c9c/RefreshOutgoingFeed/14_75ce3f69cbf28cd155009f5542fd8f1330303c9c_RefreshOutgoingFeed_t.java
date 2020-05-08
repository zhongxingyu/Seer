 package net.cyklotron.cms.modules.actions.syndication;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.syndication.OutgoingFeedResource;
 import net.cyklotron.cms.syndication.SyndicationService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 /**
  * Action for explicit refreshing of outgoing feeds in the site.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RefreshOutgoingFeed.java,v 1.3 2006-02-28 16:17:53 pablo Exp $
  */
 public class RefreshOutgoingFeed extends BaseSyndicationAction
 {
     public RefreshOutgoingFeed(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, SyndicationService syndicationService)
     {
         super(logger, structureService, cmsDataFactory, syndicationService);
     }
 
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         OutgoingFeedResource feed = getOutgoingFeed(coralSession, parameters);
         try
         {
             syndicationService.getOutgoingFeedsManager().refreshFeed(context, coralSession, feed);
         }
         catch(Exception e)
         {
            logger.error("error refreshing feed ",e);
             templatingContext.put("result","error_refreshing_feed");
             return;
         }
         templatingContext.put("result","refreshed_successfully");
     }
     
     public boolean checkAccessRights(Context context)
     throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return checkPermission(context, coralSession, "cms.syndication.outfeed.modify");
     }   
 }
