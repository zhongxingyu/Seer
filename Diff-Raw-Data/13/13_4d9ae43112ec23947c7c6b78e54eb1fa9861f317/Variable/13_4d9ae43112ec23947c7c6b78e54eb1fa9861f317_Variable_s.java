 package com.djtemplate4j;
 
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class Variable {
     private static final String VARIABLE_LOOKUP_SEPARATOR = ".";
     private static final String VARIABLE_LOOKUP_SEPARATOR_REGEX = Pattern.quote(VARIABLE_LOOKUP_SEPARATOR);
 
     private final String variableName;
     private boolean complexLookup;
     private static final String NULL = "null";
 
     public Variable(String variableName) {
         this.variableName = variableName;
         this.complexLookup = variableName.contains(VARIABLE_LOOKUP_SEPARATOR);
     }
 
     public String render(Map<String, Object> context) {
         if (this.complexLookup) {
            final LinkedList lookupPath = new LinkedList(Arrays.asList(this.variableName.split(VARIABLE_LOOKUP_SEPARATOR_REGEX)));
             return complexResolve(lookupPath, context);
         }
         return simpleLookupOnMap(this.variableName, context);
     }
 
     private String simpleLookupOnMap(String stringKey, Map<?, ?> context) {
         final Map<String, Object> stringObjectMap = mapObjKeysToStringKeys(context.keySet());
 
         if (stringObjectMap.containsKey(stringKey)) {
             final Object objValue = context.get(stringObjectMap.get(stringKey));
            return objValue != null ? objValue.toString() : NULL;
         }
 
         throw new VariableDoesNotExist(this.variableName);
     }
 
     public String complexResolve(LinkedList<String> lookupPath, Map<?, ?> context) {
         if (lookupPath.size() == 1) {
             return simpleLookupOnMap(lookupPath.peek(), context);
         } else {
             final String stringKey = lookupPath.pop();
             final Map<String, Object> stringObjectMap = mapObjKeysToStringKeys(context.keySet());
 
             if (stringObjectMap.containsKey(stringKey)) {
                 final Object objValue = context.get(stringObjectMap.get(stringKey));
                 if (objValue instanceof Map) {
                     return complexResolve(lookupPath, (Map<?, ?>) objValue);
                 }
             }
         }
 
         throw new VariableDoesNotExist(this.variableName);
     }
 
     private Map<String, Object> mapObjKeysToStringKeys(Set<?> objKeys) {
         Map<String, Object> mapStringKeyToObjKey = new HashMap<String, Object>();
         for (Object objKey : objKeys) {
            mapStringKeyToObjKey.put(objKey != null ? objKey.toString() : NULL, objKey);
         }
         return mapStringKeyToObjKey;
     }
 }
