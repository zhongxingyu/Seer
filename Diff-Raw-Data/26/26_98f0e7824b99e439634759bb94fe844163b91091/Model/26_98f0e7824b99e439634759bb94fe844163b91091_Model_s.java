 package com.codeminer42.dismantle;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class Model {
     abstract protected Map<String, String> externalRepresentationKeyPaths();
 
     protected Model() {
 
     }
 
     protected Model(Map<String, Object> externalRepresentation) {
         final Map<String, String> selfRepresentation = this.externalRepresentationKeyPaths();
         for (String property : selfRepresentation.keySet()) {
             String path = selfRepresentation.get(property);
             Object transformable = getData(externalRepresentation, selfRepresentation, path);
             assignProperty(property, transformable);
         }
     }
 
     private static Object getData(Map<String, Object> externalRepresentation, Map<String, String> selfRepresentation, String path) {
         if (path.contains(".")) {
             String[] subPaths = path.split("\\.");
             Object nestedObject = externalRepresentation.get(subPaths[0]);
             if (nestedObject instanceof Map) {
                 try {
                     Map<String, Object> nestedRepresentation = (Map<String, Object>) nestedObject;
                     return getData(nestedRepresentation, selfRepresentation, path.substring(subPaths[0].length()+1));
                 } catch (ClassCastException e) {
                     // Well, not a Map<String, Object> :/
                     e.printStackTrace();
                 }
             }
             return null;
         } else {
             return externalRepresentation.get(path);
         }
     }
 
     public Map<String, Object> externalRepresentation() {
         final Map<String, String> selfRepresentation = this.externalRepresentationKeyPaths();
         Map<String, Object> representation = new HashMap<String, Object>();
         for (String property : selfRepresentation.keySet()) {
            representation.put(property, tryToGetField(property));
         }
         return representation;
     }
 
     private void assignProperty(String property, Object mapValue) {
         Object result = null;
         try {
             try {
                result = tryToInvokeTransformation(property, mapValue);
             } catch (NoSuchMethodException e) {
                 // try to setTheField with the result we got
                 result = mapValue;
             } catch(Exception e) {
                 // Unexpected error while trying to invoke the transform method.
             } finally {
                 this.tryToSetField(property, result);
             }
         } catch (SecurityException e) {
             e.printStackTrace();
         }
     }
 
    private final Object tryToInvokeTransformation(String property, Object mapValue) throws NoSuchMethodException {
         try {
             Method method = this.getClass().getDeclaredMethod("transformTo" + property.substring(0, 1).toUpperCase() + property.substring(1), Object.class);
             method.setAccessible(true);
             return method.invoke(this, mapValue);
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private void tryToSetField(String property, Object data) {
         try {
             Field field = this.getClass().getDeclaredField(property);
             field.setAccessible(true);
             field.set(this, data);
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
     }
 
     private final Object tryToGetField(String property) {
         try {
             Field field = this.getClass().getDeclaredField(property);
             field.setAccessible(true);
             return field.get(this);
         } catch (NoSuchFieldException e) {
         } catch (IllegalAccessException e) {
         }
         return null;
     }
 }
