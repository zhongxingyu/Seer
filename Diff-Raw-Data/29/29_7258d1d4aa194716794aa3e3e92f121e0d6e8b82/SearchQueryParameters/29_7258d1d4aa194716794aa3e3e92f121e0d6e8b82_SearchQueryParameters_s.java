 package de.fiz.aas.services.auxiliaryobjects;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.apache.commons.beanutils.BeanUtils;
 
 import de.fiz.ddb.aas.auxiliaryobjects.SearchParameters;
 
 /**
  * Class holds GET-parameters for search.
  * 
  * @author Michael Hoppe
 *
  */
 public class SearchQueryParameters {
 
     private int count = 20;
 
     private int start = 1;
 
     private String[] q;
 
     private String sort;
 
     private Map<String, List<String>> searchFields = new HashMap<String, List<String>>();
 
     /**
     * Constructor that takes MultivaluedMap from GET-Request and stores values in fields of this class.
      * 
      */
     public SearchQueryParameters(MultivaluedMap<String, String> queryParameters) throws IllegalAccessException,
         InvocationTargetException {
         for (String key : queryParameters.keySet()) {
             try {
                 BeanUtils.getProperty(this, key);
                 BeanUtils.setProperty(this, key, queryParameters.get(key));
             }
             catch (NoSuchMethodException e) {
                 searchFields.put(key, queryParameters.get(key));
             }
 
         }
     }
 
     /**
      * @return the sort
      */
     public String getSort() {
         return sort;
     }
 
     /**
     * @param sort the sort to set
      */
     public void setSort(String sort) {
         this.sort = sort;
     }
 
     /**
      * @return the q
      */
     public String[] getQ() {
         return q;
     }
 
     /**
     * @param q the q to set
      */
     public void setQ(String[] q) {
        this.q = q;
     }
 
     /**
      * @return the count
      */
     public int getCount() {
         return count;
     }
 
     /**
     * @param count the count to set
      */
     public void setCount(int count) {
         this.count = count;
     }
 
     /**
      * @return the start
      */
     public int getStart() {
         return start;
     }
 
     /**
     * @param start the start to set
      */
     public void setStart(int start) {
         this.start = start;
     }
 
     /**
      * @return the searchFields
      */
     public Map<String, List<String>> getSearchFields() {
         return searchFields;
     }
 
     /**
     * @param searchFields the searchFields to set
      */
     public void setSearchFields(Map<String, List<String>> searchFields) {
         this.searchFields = searchFields;
     }
 
     public SearchParameters getLdapSearchObject() {
         SearchParameters searchParameters = new SearchParameters();
         searchParameters.setCount(getCount());
         searchParameters.setStartRecord(getStart());
         searchParameters.setSort(sort);
         searchParameters.setQ(getQ());
         searchParameters.setSearchFields(getSearchFields());
         return searchParameters;
     }
 
 }
