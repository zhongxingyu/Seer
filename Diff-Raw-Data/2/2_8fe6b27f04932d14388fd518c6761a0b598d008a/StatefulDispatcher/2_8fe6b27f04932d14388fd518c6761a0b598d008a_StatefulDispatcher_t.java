 package org.nohope.bean;
 
 import javax.annotation.Nonnull;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 12/10/12 3:08 PM
  */
 public abstract class StatefulDispatcher implements IDispatcher {
    private final Map<Object, Map<String, Object>> map = new ConcurrentHashMap<>();
 
     @Override
     public final synchronized void handle(@Nonnull final IDispatchable obj,
                                           @Nonnull final String propertyName,
                                           final Object newValue) {
         Map<String, Object> properties = map.get(obj);
         if (properties == null) {
             properties = new HashMap<>();
             map.put(obj, properties);
         }
 
         boolean previousExists = true;
         if (!properties.containsKey(propertyName)) {
             properties.put(propertyName, newValue);
             previousExists = false;
         }
 
         final Object old = properties.put(propertyName, newValue);
         handle(obj, propertyName, old, newValue, previousExists);
     }
 
     protected abstract void handle(@Nonnull final IDispatchable obj,
                                    @Nonnull final String propertyName,
                                    final Object oldValue,
                                    final Object newValue,
                                    final boolean previousExists);
 }
