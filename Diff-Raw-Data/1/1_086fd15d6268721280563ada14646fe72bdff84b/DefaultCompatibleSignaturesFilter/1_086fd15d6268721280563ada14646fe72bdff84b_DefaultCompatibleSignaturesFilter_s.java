 package au.net.netstorm.boost.nursery.autoedge.utils;
 
 import java.lang.reflect.Constructor;
 import java.util.List;
 
 public class DefaultCompatibleSignaturesFilter implements CompatibleSignaturesFilter {
     private final Class<?>[] target;
     JLSOverloadRules jls;
 
     public DefaultCompatibleSignaturesFilter(List<Class<?>> target) {
         this.target = target.toArray(new Class[target.size()]);
     }
 
     public boolean accept(Constructor<?> c) {
         Class<?>[] candidate = c.getParameterTypes();
         return jls.compatible(candidate, target);
        // FIX 2328 should i flip these inside or outside
     }
 }
