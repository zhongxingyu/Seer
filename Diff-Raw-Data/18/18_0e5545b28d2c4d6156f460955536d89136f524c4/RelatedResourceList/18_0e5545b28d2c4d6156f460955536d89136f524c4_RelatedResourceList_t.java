 package net.cyklotron.cms.category.components;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableFilter;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.category.query.CategoryResolver;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.util.SiteFilter;
 
 /**
  * This component displays lists of resources assigned to categories assigned to current document
  * node. Category list is filtered upon this component's configuration.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: RelatedResourceList.java,v 1.8 2005-05-17 06:19:58 zwierzem Exp $
  */
 public class RelatedResourceList
 extends BaseResourceList
 {
 	protected CategoryQueryService categoryQueryService;
 	
     protected CategoryService categoryService;
 	
 	public RelatedResourceList(Context context, IntegrationService integrationService,
         CmsDataFactory cmsDataFactory,  CategoryQueryService categoryQueryService,
         CategoryService categoryService)
     {
         super(context,integrationService, cmsDataFactory);
         this.categoryService = categoryService;
         this.categoryQueryService = categoryQueryService;
     }
     
 	
     public BaseResourceListConfiguration createConfig()
     {
         return new RelatedResourceListConfiguration();
     }
     
     public String getTableStateName()
     {
         return "net.cyklotron.cms.category.related_resource_list";
     }
 
 	protected String[] getResourceClasses(CoralSession coralSession, BaseResourceListConfiguration config)
 	throws ProcessingException
 	{
 		RelatedResourceListConfiguration config2 = (RelatedResourceListConfiguration)config;
 		return config2.getResourceClasses();
 	}
 
     protected TableFilter[] getTableFilters(CoralSession coralSession, BaseResourceListConfiguration config)
     throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         NavigationNodeResource node = cmsData.getNode();
         RelatedResourceListConfiguration config2 = (RelatedResourceListConfiguration)config;
 		SiteFilter siteFilter = new SiteFilter(new SiteResource[] { cmsData.getSite() });
        if(node != null)
         {
            if(config2.isSiteFilterEnabled())
            {
                TableFilter[] filters = new TableFilter[2];
                filters[0] = new RejectResourceFilter(node);
                filters[1] = siteFilter;
                return filters;
            }
            else
            {
                return new TableFilter[] { new RejectResourceFilter(node) };
            }
         }
         else if(config2.isSiteFilterEnabled())
         {
             return new TableFilter[] { siteFilter };
         }
         else
         {
             return new TableFilter[] {};
         }
     }
 
     public String getQuery(CoralSession coralSession, BaseResourceListConfiguration config)
     throws ProcessingException
     {
         // get categories accepted in query
         Set acceptedCategories = new HashSet();
 
 		CategoryResolver resolver = categoryQueryService.getCategoryResolver();
 			
         RelatedResourceListConfiguration config2 = (RelatedResourceListConfiguration)config;
         String[] activeCategoriesPaths = config2.getActiveCategoriesPaths();
         for(int i = 0; i < activeCategoriesPaths.length; i++)
         {
             // get accepted category
 			CategoryResource category =
 				resolver.resolveCategoryIdentifier(activeCategoriesPaths[i]);
 			if(category != null)
 			{
 				acceptedCategories.add(category);
 
 	            // get subcategories of this category - they are also accepted
 	            //      (they imply the parent category)
 	            CategoryResource[] subCategories =
 	                categoryService.getSubCategories(coralSession, category, false);
 	            for(int j = 0; j < subCategories.length; j++)
 	            {
 	                acceptedCategories.add(subCategories[j]);
 	            }
 			}
         }
 
         CmsData cmsData = cmsDataFactory.getCmsData(context);
 		NavigationNodeResource node = cmsData.getNode();
         if(node != null)
         {
             CategoryResource[] categories = categoryService.getCategories(coralSession, node, false);
         
             StringBuilder buf = new StringBuilder(100);
             for(int i = 0; i < categories.length; i++)
             {
                 CategoryResource category = categories[i];
                 if(acceptedCategories.contains(category))
                 {
                     if(i > 0 && buf.length() > 0)
                     {
                         buf.append(" + ");
                     }
                     buf.append("MAP('category.References'){ RES(");
                     buf.append(category.getIdString());
                     buf.append(") }");
                 }
             }
         
             if(buf.length() > 0)
             {
                 return buf.append(';').toString();
             }
             else
             {
                 return null;
             }
         }
         else
         {
         	if(cmsData.getComponent() != null)
         	{
 				cmsData.getComponent().error("No navigation node selected", null);
         	}
             return null;
         }
     }
     
     // implementation /////////////////////////////////////////////////////////////////////////////
     
     private class RejectResourceFilter
     implements TableFilter
     {
         Resource rejectedResource;
         
         RejectResourceFilter(Resource rejectedResource)
         {
             this.rejectedResource = rejectedResource;
         }
         
         public boolean accept(Object o)
         {
             if(!(o instanceof Resource))
             {
                 return false;
             }
             
             Resource r = (Resource)o;
             return rejectedResource.getId() != r.getId();
         }
     }
 }
