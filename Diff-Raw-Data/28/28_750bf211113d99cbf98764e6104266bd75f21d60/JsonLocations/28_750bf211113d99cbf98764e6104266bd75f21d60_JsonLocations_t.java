 package net.cyklotron.cms.modules.views.locations;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.comparator.BaseStringComparator;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.json.AbstractJsonView;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.locations.Location;
 import net.cyklotron.cms.locations.LocationDatabaseService;
 import net.cyklotron.cms.preferences.PreferencesService;
 
 /**
  * The screen for serving files.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: Download.java,v 1.6 2006-01-02 11:42:17 rafal Exp $
  */
 public class JsonLocations
     extends AbstractJsonView
 {
     private static final int DEFAULT_LIMIT = 25;
 
     private static final String FIELD_VALUES = "fieldValues";
 
     private static final String FIELD_UNIQUE_LOCATIONS = "uniqueLocations";
 
     /** The Location service. */
     private LocationDatabaseService locationDatabaseService;
 
     public JsonLocations(Context context, Logger log, PreferencesService preferencesService,
         CmsDataFactory cmsDataFactory, TableStateManager tableStateManager,
         LocationDatabaseService locationDatabaseService)
     {
         super(context, log);
         this.locationDatabaseService = locationDatabaseService;
     }
 
     @Override
     protected void buildResponseHeaders(HttpContext httpContext)
         throws ProcessingException
     {
         httpContext.getResponse().setContentType("application/json;charset=UTF-8");
     }
 
     @Override
     protected void buildJsonStream()
         throws ProcessingException, JsonGenerationException, IOException
     {
         Map<String, Object> fieldValues = getFieldValues(context);
         writeResponseValue(fieldValues);
     }
 
     private Map<String, Object> getFieldValues(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         String requestedField = parameters.get("qfield", "");
         String query = parameters.get("q", "");
         int limit = parameters.getInt("limit", DEFAULT_LIMIT);
 
         Map<String, String> fieldValues = new HashMap<>();
         for(String param : parameters.getParameterNames())
         {
             if(param.startsWith("q") && !parameters.get(param).isEmpty()
                 && !(param.equals("q") || param.equals("qfield")))
             {
                 fieldValues.put(param.substring(1), parameters.get(param));
             }
         }
         if(query.length() > 0)
         {
             fieldValues.put(requestedField, query);
         }
 
         if(fieldValues.size() == 0 && requestedField.length() > 0)
         {
             Map<String, Object> fieldObjects = new HashMap<String, Object>();
             List<String> terms = locationDatabaseService.getAllTerms(requestedField);
             Collections.sort(terms);
             fieldObjects.put(FIELD_VALUES, terms);
             fieldObjects.put(FIELD_UNIQUE_LOCATIONS, new HashMap<String, Object>());
             return fieldObjects;
         }
         else
         {
             List<Location> locations = locationDatabaseService.getLocations(requestedField,
                 fieldValues);
             return getFieldValues(requestedField, locations, limit);
         }
     }
 
     private Map<String, Object> getFieldValues(String requestedField, List<Location> locations,
         int limit)
     {
         Map<String, Object> fieldObjects = new HashMap<String, Object>();
        SortedMap<String, Map<String, String>> uniqueLocations = new TreeMap<String, Map<String, String>>();
 
         for(Location location : locations)
         {
             Map<String, String> matchnigFields = getMatchingLocationFields(location.getEntries(),
                 uniqueLocations.get(location.get(requestedField)));
             uniqueLocations.put(location.get(requestedField), matchnigFields);
         }
        List<String> valueList = new ArrayList<String>(uniqueLocations.keySet());
        uniqueLocations = uniqueLocations.headMap(valueList.get(Math.min(limit,valueList.size())));
 
        fieldObjects.put(FIELD_VALUES, valueList);
         fieldObjects.put(FIELD_UNIQUE_LOCATIONS, uniqueLocations);
 
         return fieldObjects;
     }
 
     /**
      * Return non empty matching fields map
      * 
      * @param m1 map object
      * @param m2 map object
      * @return <code>Map<String, String></code>
      * @author lukasz
      */
     private Map<String, String> getMatchingLocationFields(Map<String, String> m1,
         Map<String, String> m2)
     {
        Map<String, String> matching = new HashMap<String, String>();
         if(m1 == null)
         {
             return new HashMap<String, String>();
         }
         if(m2 == null)
         {
             for(String key : m1.keySet())
             {
                 if(m1.get(key) != null)
                 {
                    matching.put(key, m1.get(key));
                 }
             }
         }
         else
         {
             for(String key : m1.keySet())
             {
                 if(m1.get(key) != null && m1.get(key).equals(m2.get(key)))
                 {
                    matching.put(key, m1.get(key));
                 }
             }
         }
        return matching;
     }
 
     /**
      * Location Comparator used to sort by defined Location field value
      * 
      * @author lukasz
      */
     private class LocationsComparator
         extends BaseStringComparator<Location>
     {
         private final String requestedField;
 
         public LocationsComparator(String requestedField)
         {
             super(I18nContext.getI18nContext(context).getLocale());
             this.requestedField = requestedField;
         }
 
         public int compare(Location l1, Location l2)
         {
             String f1 = l1.get(requestedField);
             String f2 = l2.get(requestedField);
 
             return compareStrings(f1, f2);
         }
     }
 }
