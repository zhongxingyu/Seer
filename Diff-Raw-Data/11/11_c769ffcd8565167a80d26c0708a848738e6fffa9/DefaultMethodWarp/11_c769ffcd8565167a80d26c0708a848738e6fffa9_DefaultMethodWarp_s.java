 package au.net.netstorm.boost.nursery.autoedge;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import au.net.netstorm.boost.edge.java.lang.EdgeClass;
 
 public final class DefaultMethodWarp implements MethodWarp {
     EdgeClass classer;
 
    public Method warp(Class<?> cls, Method src) {
        String name = src.getName();
        Class<?>[] params = src.getParameterTypes();
         unedge(params);
        return classer.getMethod(cls, name, params);
     }
 
     private void unedge(Class<?>[] params) {
         for (int i = 0; i < params.length; ++i) {
             params[i] = unedge(params[i]);
         }
     }
 
     private Class<?> unedge(Class<?> cls) {
         if (!Edge.class.isAssignableFrom(cls)) return cls;
         ParameterizedType edge = getEdgeType(cls);
         return getEdgeTypeArgument(edge);
     }
 
     private ParameterizedType getEdgeType(Class<?> cls) {
         Type[] superC = cls.getGenericInterfaces();
         for (Type t : superC) {
             if (isEdgeType(t)) return (ParameterizedType) t;
         }
         throw new AssertionError("Edge must be assignable from cls");
     }
 
     private boolean isEdgeType(Type t) {
         if (!(t instanceof ParameterizedType)) return false;
         ParameterizedType pType = (ParameterizedType) t;
         return pType.getRawType() == Edge.class;
     }
 
     private Class<?> getEdgeTypeArgument(ParameterizedType edge) {
         Type[] args = edge.getActualTypeArguments();
         if (args.length != 1) throw new AssertionError("Edge class must only have one type argument");
         Type arg = args[0];
         // FIX 2328 this should be codified in a test
         if (!(arg instanceof Class)) throw new AssertionError("Edge classes must have concrete type argument");
         return (Class<?>) arg;
     }
 }
