 package net.cyklotron.cms.modules.components.category;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.cache.CacheFactory;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.Templating;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.CmsNodeResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.components.BaseResourceListConfiguration;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureUtil;
 
 
 /**
  * This component displays lists of resources assigned to categories assigned to current document
  * node. Category list is filtered upon this component's configuration.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: RelatedResourceList.java,v 1.4 2005-03-08 10:54:52 pablo Exp $
  */
 public class RelatedResourceList
 extends BaseResourceList
 {
     public RelatedResourceList(Context context, Logger logger, Templating templating,
         CmsDataFactory cmsDataFactory, SkinService skinService, MVCFinder mvcFinder,
         CategoryService categoryService, SiteService siteService,
         TableStateManager tableStateManager, CategoryQueryService categoryQueryService,
         CacheFactory cacheFactory, IntegrationService integrationService,
         PreferencesService preferencesService)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder, categoryService,
                         siteService, tableStateManager, categoryQueryService, cacheFactory,
                         integrationService, preferencesService);
         
     }
 	/* (non-Javadoc)
 	 * @see net.cyklotron.cms.modules.components.category.BaseResourceList#getResourceList()
 	 */
 	protected net.cyklotron.cms.category.components.BaseResourceList getResourceList(CmsData cmsData, Parameters parameters)
 	{
 		return new net.cyklotron.cms.category.components.RelatedResourceList(context, 
            integrationService, cmsDataFactory, categoryQueryService, categoryService, getContextNode(context, cmsData, parameters));
 	}
     
     /* (non-Javadoc)
      * @see net.cyklotron.cms.modules.components.category.BaseResourceList#getCacheKeyNode(net.cyklotron.cms.category.components.BaseResourceListConfiguration, net.cyklotron.cms.CmsData)
      */
     protected CmsNodeResource getCacheKeyNode(BaseResourceListConfiguration config, CmsData cmsData)
     {
         return cmsData.getNode();
     }
     
     
    protected NavigationNodeResource getContextNode(Context context, CmsData cmsData, Parameters parameters)
     {
         if(parameters.isDefined("node_id"))
         {
             try
             {
                 Long nodeId = parameters.getLong("node_id", -1L);
                 CoralSession coralSession = context.getAttribute(CoralSession.class);
                 NavigationNodeResource node = (NavigationNodeResource)StructureUtil.getNode(coralSession, nodeId);
                 // check if subject can view this node.
                if(!node.canView(coralSession, coralSession.getUserSubject(), cmsData.getDate()))
                 {
                     node = cmsData.getNode();
                 }
                 return node;
             }
             catch(ProcessingException e)
             {
                 return cmsData.getNode();
             }
         }
         else
         {
             return cmsData.getNode();
         }
     }
 }
