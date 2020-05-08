 package com.codeminer42.dismantle;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.vidageek.mirror.dsl.AccessorsController;
 import net.vidageek.mirror.dsl.ClassController;
 import net.vidageek.mirror.dsl.Mirror;
 
 public abstract class Model {
     
     private ClassController<?> mirrorOnClass;
     private AccessorsController mirrorOnThis;
 
     public Map<String, String> externalRepresentationKeyPaths() {
         return new HashMap<String, String>();
     }
 
     protected Model() {
         mirrorOnClass = new Mirror().on(getClass());
         mirrorOnThis = new Mirror().on(this);
     }
 
     protected Model(Map<String, Object> externalRepresentation) {
         this();
         Map<String, String> selfRepresentation = completeExternalRepresentationKeyPaths();
         for (String property : selfRepresentation.keySet()) {
             String path = selfRepresentation.get(property);
             Object transformable = getData(externalRepresentation, path);
             setProperty(property, transformable);
         }
     }
 
     private Map<String, String> completeExternalRepresentationKeyPaths() {
         Map<String, String> selfRepresentation = protectAgainstNull(externalRepresentationKeyPaths());
         for(Field field : getClass().getDeclaredFields()) {
             if (!selfRepresentation.containsKey(field.getName())) {
                 if (!isNestedClassReference(field))
                     selfRepresentation.put(field.getName(), field.getName());
             }
 
         }
         return selfRepresentation;
     }
 
     private Map<String, String> protectAgainstNull(Map<String, String> map) {
         if(map == null) {
             return new HashMap<String, String>();
         }
         return map;
     }
 
     private boolean isNestedClassReference(Field field) {
         return field.getName().startsWith("this$");
     }
 
     private static Object getData(Map<String, Object> externalRepresentation, String path) {
         if (path.contains(".")) {
             String[] subPaths = path.split("\\.");
             Object nestedObject = externalRepresentation.get(subPaths[0]);
             if (nestedObject instanceof Map) {
                 try {
                     Map<String, Object> nestedRepresentation = (Map<String, Object>) nestedObject;
                     return getData(nestedRepresentation, path.substring(subPaths[0].length()+1));
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
         Map<String, String> selfRepresentation = this.completeExternalRepresentationKeyPaths();
         Map<String, Object> representation = new HashMap<String, Object>();
         for (String property : selfRepresentation.keySet()) {
             representation.put(selfRepresentation.get(property), getProperty(property));
         }
         return representation;
     }
 
     private void setProperty(String property, Object valueFromMap) {
         Object result = null;
         try {
             try {
                 result = tryToInvokeTransformTo(property, valueFromMap);
             } catch (NoSuchMethodException e) {
                 // try to setTheField with the result we got
                 result = valueFromMap;
             } catch(Exception e) {
                 // Unexpected error while trying to invoke the transform method.
             } finally {
                 tryToSetField(property, result);
             }
         } catch (SecurityException e) {
             e.printStackTrace();
         }
     }
 
     private Object getProperty(String property) {
         Object result = null;
         Object propertyData = null;
         try {
             Field propertyField = getField(this.getClass(), property);
             propertyData = tryToGetField(propertyField);
             result = tryToInvokeTransformationFrom(propertyField, propertyData);
         } catch (NoSuchMethodException e) {
             result = propertyData;
         } catch (NoSuchFieldException ignored) {
         }
         return result;
     }
 
     private Object tryToInvokeTransformTo(String property, Object value) throws NoSuchMethodException {
         Method transformTo = getMethod("transformTo" + capitalize(property), Object.class);
         return mirrorOnThis.invoke().method(transformTo).withArgs(value);
     }
 
     private Object tryToInvokeTransformationFrom(Field property, Object value) throws NoSuchMethodException {
         Method transformFrom = getMethod("transformFrom" + capitalize(property.getName()), property.getType());
         return mirrorOnThis.invoke().method(transformFrom).withArgs(value);
     }
 
     private Method getMethod(String methodName, Class<?> paramType) throws NoSuchMethodException {
         Method method = mirrorOnClass.reflect().method(methodName).withAnyArgs();
         if(method == null){
             throw new NoSuchMethodException();
         }
         return method;
     }
 
     private void tryToSetField(String property, Object data) {
         mirrorOnThis.set().field(property).withValue(data);
     }
 
     /**
      * Get the field through the super classes
      * @param property
      * @return property data
      */
     private Object tryToGetField(Field property) {
         return mirrorOnThis.get().field(property);
     }
 
    private String capitalize(String word) {
         return word.substring(0, 1).toUpperCase() + word.substring(1);
     }
 
     private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
         return mirrorOnClass.reflect().field(name);
     }
 }
