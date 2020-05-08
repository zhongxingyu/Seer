 package org.tpi.pso.gemfire.dsl.dynamic;
 
 import java.util.Properties;
 
 import com.gemstone.gemfire.cache.Declarable;
 import com.gemstone.gemfire.cache.DynamicRegionFactory;
 
 public class Bootstrap implements Declarable {
 
     @Override
     public void init(Properties properties) {
        DynamicRegionFactory dynamicRegionFactory = DynamicRegionFactory.get();
        dynamicRegionFactory.open();
     }
 
 }
