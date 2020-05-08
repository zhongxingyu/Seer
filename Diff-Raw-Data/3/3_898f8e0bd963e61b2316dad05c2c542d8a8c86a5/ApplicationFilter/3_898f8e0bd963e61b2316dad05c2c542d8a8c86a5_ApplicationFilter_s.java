 package com.psddev.dari.db;
 
 import com.psddev.dari.util.AbstractFilter;
 import com.psddev.dari.util.SourceFilter;
 import com.psddev.dari.util.StandardFilter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.Filter;
 
 /**
  * Takes care of initializing and destroying all the components used in
  * a typical Dari application.
  *
  * <p>This filter loads:</p>
  *
  * <ul>
  * <li>{@link SourceFilter}</li>
  * <li>{@link ResetFilter}</li>
  * <li>{@link StandardFilter}</li>
  * <li>{@link ProfilingDatabaseFilter}</li>
  * <li>{@link CachingDatabaseFilter}</li>
  * </ul>
  */
 public class ApplicationFilter extends AbstractFilter {
 
     // --- AbstractFilter support ---
 
     @Override
     protected Iterable<Class<? extends Filter>> dependencies() {
         List<Class<? extends Filter>> dependencies = new ArrayList<Class<? extends Filter>>();
         dependencies.add(SourceFilter.class);
        dependencies.add(ResetFilter.class);
         dependencies.add(StandardFilter.class);
         dependencies.add(ProfilingDatabaseFilter.class);
         dependencies.add(CachingDatabaseFilter.class);
         return dependencies;
     }
 }
