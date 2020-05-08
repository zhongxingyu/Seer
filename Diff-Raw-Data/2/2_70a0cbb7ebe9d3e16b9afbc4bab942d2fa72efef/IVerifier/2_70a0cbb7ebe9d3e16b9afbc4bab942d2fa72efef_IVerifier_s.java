 package org.ritsuka.youji.util.yaconfig;
 
 
 import org.ritsuka.youji.util.reflection.TypeReference;
 
 import java.util.List;
 
 /**
  * Date: 10/1/11
  * Time: 4:31 PM
  */
 public interface IVerifier<T> {
     class True<T> implements IVerifier<T> {
         @Override
         public boolean verify(T value) {
             return true;
         }
     }
 
     abstract class ListVerifier<V>
             extends TypeReference<V>
             implements IVerifier<List<V>> {
         @Override
         public boolean verify(List value) {
             for (Object obj: value) {
                if (obj !=null && obj.getClass() == getTypeClass())
                     return false;
             }
             return true;
         }
     }
 
     boolean verify(T value);
 }
