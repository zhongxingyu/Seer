 package org.ritsuka.natsuo.yaconfig;
 
 import org.ritsuka.natsuo.reflection.TypeReference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author ketoth xupack <ketoth.xupack@gmail.com>
  * @since 10/7/11 4:21 AM
  */
 public abstract class SafeListTransformer<V, K>
         implements IConstructor<List<K>> {
 
     private final TypeReference<V> sourceRef;
     protected SafeListTransformer(final TypeReference<V> sourceRef) {
         this.sourceRef = sourceRef;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public final List<K> construct(final Object val) {
         if (!(val instanceof List)) {
             throw new IllegalArgumentException("incorrect parsers list type");
         }
 
         List rawParsers = (List) val;
         List<K> parsers = new ArrayList<K>();
         for (Object item : rawParsers) {
             try {
                 if (item == null
                    || item.getClass() != sourceRef.getTypeClass()) {
                     parsers.add(transform((V) item));
                 } else {
                     throw new IllegalArgumentException("incorrect type found");
                 }
             } catch (final Exception e){
                 throw new IllegalArgumentException(e);
             }
         }
 
         return parsers;
     }
 
     public abstract K transform(final V source);
 }
