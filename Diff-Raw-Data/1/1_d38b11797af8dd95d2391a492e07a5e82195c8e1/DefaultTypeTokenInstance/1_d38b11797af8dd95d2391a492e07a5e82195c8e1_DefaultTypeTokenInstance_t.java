 package au.net.netstorm.boost.gunge.generics;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 public class DefaultTypeTokenInstance implements TypeTokenInstance {
     private Class<?> type;
 
     public DefaultTypeTokenInstance(ParameterizedType instance) {
         Type[] args = instance.getActualTypeArguments();
         if (args.length != 1) throw new IllegalArgumentException("Type tokens must only have a single type argument.");
         buildTypeData(args[0]);
     }
 
     public Class<?> rawType() {
         return type;
     }
 
    // FIX 2328 add support for paramertized and wildcard types
     private void buildTypeData(Type tokenType) {
         if (!(tokenType instanceof Class)) {
             throw new IllegalArgumentException("Type tokens currently only support Class references.");
         }
         type = (Class<?>) tokenType;
     }
 }
