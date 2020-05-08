 package org.hack;
 
 import com.google.common.base.Predicate;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class GlobalModifyingRule implements Rule {
     private final Predicate<Map<String,Object>> condition;
     private final Action action;
 
     public GlobalModifyingRule(Predicate<Map<String,Object>> condition, Action action) {
         this.condition = condition;
         this.action = action;
     }
 
     @Override
    public void evaluate(Map<String, Object> assset, Map<String, Object> globals) {
        Map<String,Object> map = new HashMap<String,Object>(assset);
         map.putAll(globals);
         if(condition.apply(map))
         {
             action.evaluate(map);
            for(String key:assset.keySet())
                 map.remove(key);
             globals.putAll(map);
         }
     }
 }
