 package net.cyklotron.cms.category.query;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.util.ResourceSelectionState;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.web.HttpContext;
 
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * Provides default values and state keeping for pool resource editing.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CategoryQueryResourceData.java,v 1.7 2005-03-30 08:55:01 zwierzem Exp $
  */
 public class CategoryQueryResourceData
 {
     public static CategoryQueryResourceData getData(HttpContext httpContext, CategoryQueryResource query)
     {
         String key = getDataKey(query);
         CategoryQueryResourceData currentData = (CategoryQueryResourceData)
             httpContext.getSessionAttribute(key);
         if(currentData == null)
         {
             currentData = new CategoryQueryResourceData();
             httpContext.setSessionAttribute(key, currentData);
         }
         return currentData;
     }
 
     public static void removeData(HttpContext httpContext, CategoryQueryResource query)
     {
         httpContext.removeSessionAttribute(getDataKey(query));
     }
 
     private static String getDataKey(CategoryQueryResource query)
     {
         if(query != null)
         {
             return "cms.category.query.data."+query.getIdString();
         }
         else
         {
             return "cms.category.query.data.NEW";
         }
     }
     
     private String query;
     private String name;
     private String description;
     private ResourceSelectionState categories;
     private ResourceSelectionState resourceClasses;
     private boolean useSimpleQuery;
     private boolean useIdsAsIdentifiers;
 	/** Sites selected fo query results filtering.*/
 	private Set siteNames = new HashSet();
 	
     /** <code>true</code> if the config object was created during current request. */
     private boolean newData;
 
     public CategoryQueryResourceData()
     {
         newData = true;
         categories = new ResourceSelectionState(null);
 		categories.setPrefix("category");
         resourceClasses = new ResourceSelectionState(null);
         resourceClasses.setPrefix("res-class");
         useSimpleQuery = true;
     } 
 
     public boolean isNew()
     {
         return newData;
     }
 
     public void init(CoralSession coralSession, CategoryQueryResource queryRes,
         CategoryQueryService categoryQueryService, IntegrationService integrationService)
         throws ProcessingException
     {
         if(queryRes != null)
         {
             name = queryRes.getName();
             description = queryRes.getDescription();
             useSimpleQuery = queryRes.getSimpleQuery(true);
             useIdsAsIdentifiers = queryRes.getUseIdsAsIdentifiers(true);
             query = queryRes.getQuery();
 
 			siteNames.addAll(Arrays.asList(queryRes.getAcceptedSiteNames()));
 
             Map map;
             map = categoryQueryService.initCategorySelection(coralSession, 
                queryRes.getRequiredCategoryIdentifiers(), "required");
             map.putAll(categoryQueryService.initCategorySelection(coralSession, 
                queryRes.getOptionalCategoryIdentifiers(), "optional"));
             categories.init(map);    
             map = integrationService.initResourceClassSelection(coralSession, 
                 queryRes.getAcceptedResourceClasses(), "accepted");
             resourceClasses.init(map);
         }
         // data was modified
         newData = false;
     }
 
     public void update(Parameters params)
     {
         name = params.get("name","");
         description = params.get("description","");
         query = params.get("categoryQuery","");
         useSimpleQuery = params.getBoolean("useSimpleQuery",false);
         useIdsAsIdentifiers = params.getBoolean("useIdsAsIdentifiers",true);
         categories.update(params);
         resourceClasses.update(params);
         siteNames.clear();
         siteNames.addAll(Arrays.asList(params.getStrings("acceptedSites")));
         
         // data was modified
         newData = false;
     }
 
     // getters /////////////////////////////////////////////////////////////////////////////////////
 
     public String getName()
     {
         return name;
     }
 
     public String getDescription()
     {
         return description;
     }
     
     public String getQuery()
     {
         return query;
     }
     
     public boolean useSimpleQuery()
     {
         return useSimpleQuery;
     }
     
     public boolean useIdsAsIdentifiers()
     {
         return useIdsAsIdentifiers;
     }
 
     public ResourceSelectionState getCategoriesSelection()
     {
         return categories;
     }
     
     public ResourceSelectionState getResourceClassSelection()
     {
         return resourceClasses;
     }
     
     public boolean hasSite(SiteResource site)
     {
     	return siteNames.contains(site.getName());
     }
 
     public String[] getSiteNames()
     {
     	return (String[]) siteNames.toArray(new String[siteNames.size()]);
     }
 }
