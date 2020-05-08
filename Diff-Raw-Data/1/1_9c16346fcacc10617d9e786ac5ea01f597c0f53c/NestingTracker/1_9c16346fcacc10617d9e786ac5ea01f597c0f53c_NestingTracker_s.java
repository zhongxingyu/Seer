 package com.fatwire.cs.profiling.ss.reporting.reporters;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.fatwire.cs.profiling.ss.QueryString;
 import com.fatwire.cs.profiling.ss.ResultPage;
 
 public class NestingTracker {
 
     private final Map<QueryString, List<QueryString>> pages = new HashMap<QueryString, List<QueryString>>();
 
     public void add(ResultPage page) {
         if (!pages.containsKey(page.getUri())) {
             pages.put(page.getUri(), page.getMarkers());
         }
     }
 
     public Set<QueryString> getKeys() {
         return pages.keySet();
     }
 
     public int getNestingLevel(QueryString qs) {
         int level = 0;
         if (qs == null)
             return 0;
         if (pages.containsKey(qs)) {
             for (QueryString inner : pages.get(qs)) {
                 level = level + getNestingLevel(inner);
             }
         }
         return level;
 
     }
 }
