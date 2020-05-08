 package at.yomi;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static at.yomi.util.ReflectUtil.getActualTypeArguments;
 
 public class Lazy<T> {
     private Map<String, T> cache = new HashMap<String, T>();
 
     private Field field;
 
     private Injector injector;
 
     Lazy(Injector injector, Field field) {
         this.injector = injector;
         this.field = field;
     }
 
     @SuppressWarnings("unchecked")
     public T bind(String hint) {
         if (cache.containsKey(hint)) {
             return cache.get(hint);
         }
 
         Type type = field.getGenericType();
         List<Class<?>> args = getActualTypeArguments(type);
 
         if (args.size() == 1) {
             Object instance = injector.getService(args.get(0), hint);
             if (instance != null) {
                 cache.put(hint, (T) instance);
                 return cache.get(hint);
             }
         }
 
         Inject injAnn = field.getAnnotation(Inject.class);
         if (injAnn.required()) {
            throw new at.yomi.ServiceNotFoundException(field);
         }
 
         return null;
     }
 }
