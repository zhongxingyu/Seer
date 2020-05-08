 package net.cyklotron.cms.modules.actions.fixes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.modules.actions.BaseCMSAction;
 import net.cyklotron.cms.poll.PollResource;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.PollsResource;
 import net.cyklotron.cms.poll.PoolResource;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: ConvertCategoryQuery1B.java,v 1.3 2007-11-18 21:24:37 rafal Exp $
  */
 public class ReorganizePollsResource
     extends BaseCMSAction
 {
 
     SiteService siteService;
 
     PollService pollService;
 
     public ReorganizePollsResource(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, SiteService siteService, SkinService skinService,
         IntegrationService integrationService, PollService pollService)
     {
         super(logger, structureService, cmsDataFactory);
         this.siteService = siteService;
         this.pollService = pollService;
     }
 
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             SiteResource[] sites = siteService.getSites(coralSession);
             for(SiteResource site : sites)
             {
                 List<PoolResource> pools = new ArrayList<PoolResource>();
                 List<PollResource> polls = new ArrayList<PollResource>();
                 
                 // get pollsRoot if not exist create it */
                 PollsResource pollsRoot = pollService.getPollsRoot(coralSession, site);
 
                 Resource[] resources = coralSession.getStore().getResource(pollsRoot);
                 for(Resource res : resources)
                 {
                     if(res instanceof PoolResource)
                     {
                         pools.add((PoolResource)res);
                     }
                     else if(res instanceof PollResource)
                     {
                         polls.add((PollResource)res);
                     }
                 }
                 
                 // get polls parent if not exist create it */
                 PollsResource pollsParent = pollService.getPollsParent(coralSession, site, pollService.POLLS_ROOT_NAME);
                 
                 // get pools parent if not exist create it */
                 PollsResource poolsParent = pollService.getPollsParent(coralSession, site, pollService.POOLS_ROOT_NAME);
                 
                 // get votes parent if not exist create it */
                 PollsResource votesParent = pollService.getPollsParent(coralSession, site, pollService.VOTES_ROOT_NAME);
                 
                 for(PollResource poll : polls)
                 {
                     coralSession.getStore().setParent(poll, pollsParent);
                 }
                 for(PoolResource pool : pools)
                 {
                     coralSession.getStore().setParent(pool, poolsParent);
                 }
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to reorganized polls resources", e);
         }
     }
     
     
     
 
 }
