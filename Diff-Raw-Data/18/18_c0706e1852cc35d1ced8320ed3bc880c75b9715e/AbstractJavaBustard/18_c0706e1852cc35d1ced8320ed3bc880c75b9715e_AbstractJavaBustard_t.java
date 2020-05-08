 package ru.finam.bustard.java;
 
 import com.google.common.base.Supplier;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 import ru.finam.bustard.AbstractBustard;
 import ru.finam.bustard.DirectExecutor;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.WeakHashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 public abstract class AbstractJavaBustard extends AbstractBustard {
 
     public static Multimap<String, Object> createWeakMultiMap() {
         // TODO: Make concurrent multiMap
         return Multimaps.synchronizedMultimap(
                 Multimaps.newMultimap(
                         new HashMap<String, Collection<Object>>(),
                         new Supplier<Collection<Object>>() {
                             @Override
                             public Collection<Object> get() {
                                 return Collections.newSetFromMap(new WeakHashMap<Object, Boolean>());
                             }
                         }
                 )
         );
     }
 
     public AbstractJavaBustard() {
         super(new DirectExecutor(), createWeakMultiMap(), new ConcurrentHashMap<String, Object>());
     }
 }
