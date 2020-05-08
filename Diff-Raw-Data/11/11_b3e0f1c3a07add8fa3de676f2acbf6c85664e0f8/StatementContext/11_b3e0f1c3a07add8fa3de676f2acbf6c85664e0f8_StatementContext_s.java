 package net.citizensnpcs.adventures.dialog.statements;
 
 import java.util.Map;
 
 import net.citizensnpcs.adventures.dialog.DialogException;
 import net.citizensnpcs.adventures.dialog.evaluators.Evaluator;
 
 import com.google.common.collect.ForwardingMap;
 
 public class StatementContext {
     private final Map<String, Object> map;
     private final String name;
 
     public StatementContext(String name, Map<String, Object> map) {
         this.map = map;
         this.name = name;
     }
 
     public Map<String, Object> getMap() {
         return new ForwardingMap<String, Object>() {
             @Override
             protected Map<String, Object> delegate() {
                 return map;
             }
 
             @Override
             public Object get(Object key) {
                 Object value = map.get(key);
                 return value != null && value instanceof Evaluator ? ((Evaluator) value).get() : null;
             }
         };
     }
 
     public String getName() {
         return name;
     }
 
     public <T> T getRequired(String key, Class<? extends T> keyClass) throws DialogException {
         T unknown = getSafe(key, keyClass);
         if (unknown == null)
             throw new DialogException("Expected a value for key " + key);
         return unknown;
     }
 
     public <T> T getSafe(String key, Class<? extends T> keyClass) throws DialogException {
         Object raw = map.get(key);
         if (raw == null)
             return null;
         if (raw instanceof Evaluator)
             raw = ((Evaluator) raw).get();
         try {
             return keyClass.cast(raw);
         } catch (ClassCastException ex) {
             throw new DialogException("Expected " + keyClass.getSimpleName() + " but got "
                     + raw.getClass().getSimpleName() + " for key " + key);
         }
     }
 
     @SuppressWarnings("unchecked")
     public <T> T getUnsafe(String key) {
        return (T) map.get(key);
     }
 }
