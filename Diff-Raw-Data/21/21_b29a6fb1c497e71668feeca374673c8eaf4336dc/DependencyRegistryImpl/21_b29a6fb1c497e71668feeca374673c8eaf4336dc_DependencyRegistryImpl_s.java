 package org.apache.myfaces.scripting.core.dependencyScan.registry;
 
 import org.apache.myfaces.scripting.core.dependencyScan.api.ClassFilter;
 import org.apache.myfaces.scripting.core.dependencyScan.core.ClassDependencies;
 import org.apache.myfaces.scripting.core.dependencyScan.filter.ScanIdentifierFilter;
 import org.apache.myfaces.scripting.core.dependencyScan.filter.StandardNamespaceFilter;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * registry facade which is used to track our dependencies
  */
 public class DependencyRegistryImpl implements ExternalFilterDependencyRegistry {
     List<ClassFilter> _filters = new LinkedList<ClassFilter>();
 
     ClassDependencies _dependencMap;
 
     //private volatile Strategy _registrationStrategy;
     final Integer _engineType;
 
     public DependencyRegistryImpl(Integer engineType, ClassDependencies dependencyMap) {
         _dependencMap = dependencyMap;
         _engineType = engineType;
 
         _filters.add(new ScanIdentifierFilter(_engineType));
         _filters.add(new StandardNamespaceFilter());
     }
 
     public void clearFilters() {
         _filters.clear();
         _filters.add(new ScanIdentifierFilter(_engineType));
         _filters.add(new StandardNamespaceFilter());
     }
 
     public void addFilter(ClassFilter filter) {
         _filters.add(filter);
     }
 
     public boolean isAllowed(Integer engineType, String className) {
         for (ClassFilter filter : _filters) {
             if (!filter.isAllowed(_engineType, className)) {
                 return false;
             }
         }
         return true;
     }
 
     public void addDependency(Integer engineType, String rootClass, String currentlyVisitedClass, String dependency) {
         if (currentlyVisitedClass.equals(dependency)) {
             return;
         }
 
         if (dependency == null || dependency.trim().equals("")) {
             return;
         }
 
         if (!isAllowed(engineType, dependency)) {
             return;
         }
         //for now we code it into a list like we used to do before
         //but in the long run we have to directly register
         //to save one step
         //getDependencySet(source).add(dependency);
         _dependencMap.addDependency(rootClass,  dependency);
     }
     
     /**
      * flush to flush down our stored dependencies into our final map
      */
     public void flush(Integer engineType) {
         //_registrationStrategy.apply(_dependencies);
     }
    
 }
