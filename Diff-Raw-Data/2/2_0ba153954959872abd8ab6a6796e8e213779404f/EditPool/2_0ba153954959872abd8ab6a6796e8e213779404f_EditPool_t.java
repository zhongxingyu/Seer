 package net.cyklotron.cms.modules.views.poll;
 
 import java.util.Arrays;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.PollsResource;
 import net.cyklotron.cms.poll.PoolResource;
 import net.cyklotron.cms.poll.PoolResourceImpl;
 import net.cyklotron.cms.preferences.PreferencesService;
 
 
 
 /**
  *
  */
 public class EditPool
     extends BasePollScreen
 {
     
     public EditPool(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, PollService pollService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager, pollService);
         
     }
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         Boolean fromComponent = (Boolean)httpContext.getSessionAttribute(FROM_COMPONENT);
         if(fromComponent != null && fromComponent.booleanValue())
         {
             templatingContext.put("from_component",fromComponent);
             templatingContext.put("component_node",(Long)httpContext.getSessionAttribute(COMPONENT_NODE));
             templatingContext.put("component_instance",(String)httpContext.getSessionAttribute(COMPONENT_INSTANCE));
         }
 
         int poolId = parameters.getInt("pool_id", -1);
         if(poolId == -1)
         {
             throw new ProcessingException("Pool id not found");
         }
         try
         {
             PoolResource pool = PoolResourceImpl.getPoolResource(coralSession, poolId);
             templatingContext.put("pool",pool);
             PollsResource pollsRoot = (PollsResource)pool.getParent();
             templatingContext.put("pollsRoot",pollsRoot);
 
            Resource[] pollResources = pollService.getRelation(coralSession).get(pool);
             templatingContext.put("polls",Arrays.asList(pollResources));
         }
         catch(EntityDoesNotExistException e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("PollException: ",e);
             return;
         }
     }
 }
