 package net.jonp.sorm.codegen.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * Represents a SQL query, organized by dialect.
  */
 public class Query
     implements Iterable<String>
 {
     // Map of dialect onto SQL query
     private final Map<String, String> byDialect = new LinkedHashMap<String, String>();
 
     public Query()
     {
         // Nothing to do
     }
 
     public Collection<String> getDialects()
     {
         return new ArrayList<String>(byDialect.keySet());
     }
 
     public String getQuery(final String dialect)
     {
         return byDialect.get(dialect);
     }
 
     public Iterator<String> iterator()
     {
         return byDialect.keySet().iterator();
     }
 
     public int size()
     {
         return byDialect.size();
     }
 
     public String putQuery(final String dialect, final String query)
     {
         return byDialect.put(dialect, query);
     }
 
     public String removeQuery(final String dialect)
     {
         return byDialect.remove(dialect);
     }
 
     public boolean contains(final String dialect)
     {
         return byDialect.containsKey(dialect);
     }
 
     public boolean isEmpty()
     {
         return byDialect.isEmpty();
     }
 }
