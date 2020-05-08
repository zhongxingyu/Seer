 package net.cyklotron.cms.modules.actions.category.query;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.util.ResourceSelectionState;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryBuilder;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryResourceData;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.category.query.CategoryQueryUtil;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Category query pool base update and add action.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: BaseCategoryQueryAddUpdate.java,v 1.4 2005-03-30 08:50:23 zwierzem Exp $
  */
 public abstract class BaseCategoryQueryAddUpdate
     extends BaseCategoryQueryAction
 {
     
     
     public BaseCategoryQueryAddUpdate(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, CategoryQueryService categoryQueryService,
         CategoryService categoryService, SiteService siteService)
     {
         super(logger, structureService, cmsDataFactory, categoryQueryService, categoryService,
                         siteService);
         
     }
     public void updateQuery(CategoryQueryResource query, CategoryQueryResourceData queryData,
     	CoralSession coralSession)
         throws Exception
     {
 		String[] siteNames = queryData.getSiteNames();
 		Set acceptedCategoryRoots = new HashSet();
 		// add global categories root
 		acceptedCategoryRoots.add(categoryService.getCategoryRoot(coralSession, null));
 		if(siteNames.length == 1)
 		{
 			try
 			{
 				// add site categories root
 				acceptedCategoryRoots.add(
 					categoryService.getCategoryRoot(coralSession, siteService.getSite(coralSession, siteNames[0])));
 			}
 			catch (SiteException e)
 			{
 				throw new Exception("Cannot find selected site", e);
 			}
 		}
 		ResourceSelectionState catsSel = queryData.getCategoriesSelection();
 		for (Iterator iter = catsSel.getEntities(coralSession).keySet().iterator(); iter.hasNext();)
 		{
 			Resource category = (Resource) iter.next();
 			Resource parent = category;
 			while(parent != null)
 			{
 				if(acceptedCategoryRoots.contains(parent))
 				{
 					break;
 				}
 				parent = parent.getParent();
 			}
 			if(parent == null)
 			{
 				catsSel.remove(category);
 			}                
 		}
 
 		query.setAcceptedSiteNames(siteNames);
         query.setDescription(queryData.getDescription());
         query.setSimpleQuery(queryData.useSimpleQuery());
         query.setUseIdsAsIdentifiers(queryData.useIdsAsIdentifiers());
 		CategoryQueryBuilder parsedQuery = new CategoryQueryBuilder(coralSession, 
 			queryData.getCategoriesSelection(), queryData.useIdsAsIdentifiers());
 		if(queryData.useSimpleQuery())
 		{
 			query.setQuery(parsedQuery.getQuery());
 		}
 		else
 		{
 			query.setQuery(queryData.getQuery());
 		}
		query.setRequiredCategoryIdentifiers(CategoryQueryUtil.joinCategoryIdentifiers(
 			parsedQuery.getRequiredIdentifiers()));
		query.setOptionalCategoryIdentifiers(CategoryQueryUtil.joinCategoryIdentifiers(
 			parsedQuery.getOptionalIdentifiers()));
 		query.setAcceptedResourceClasses(CategoryQueryUtil.getNames(coralSession,
 			queryData.getResourceClassSelection(),"accepted"));
         query.update();
     }
 }
