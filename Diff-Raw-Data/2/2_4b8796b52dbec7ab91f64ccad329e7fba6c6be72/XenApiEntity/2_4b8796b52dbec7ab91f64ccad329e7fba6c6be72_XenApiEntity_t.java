 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.io.Serializable;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import net.wgr.core.ReflectionUtils;
 import net.wgr.xenmaster.api.util.CachingFacility;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 import net.wgr.xenmaster.monitoring.LogEntry;
 import net.wgr.xenmaster.monitoring.LogKeeper;
 import org.apache.log4j.Logger;
 
 /**
  * Xen API workhorse
  * @created Oct 2, 2011
  * @author double-u
  */
 public class XenApiEntity implements Serializable {
 
     protected String reference;
     protected String uuid;
     protected final static transient String NULL_REF = "OpaqueRef:NULL";
     protected final static transient Map<String, String> globalInterpretation = new HashMap<>();
     protected final static transient String packageName = XenApiEntity.class.getPackage().getName();
 
     public XenApiEntity() {
         // Translating to upper case in favor of not following code conventions
         // todo : see if there is a better way to do this
         if (globalInterpretation.isEmpty()) {
             Map<String, String> i = globalInterpretation;
             i.put("vm", "VM");
             i.put("vdi", "VDI");
             i.put("vbd", "VBD");
             i.put("vif", "VIF");
             i.put("sr", "SR");
         }
     }
 
     public XenApiEntity(String ref) {
         this(ref, ref != null);
     }
 
     public XenApiEntity(String ref, boolean autoFill) {
         this();
         
         try {
             checkReference(ref);
         } catch (IllegalReferenceException ex) {
             error(ex);
             return;
         }
         this.reference = ref;
         
         if (autoFill) {
             fillOut(getAPIName(), null);
         }
     }
     
     protected final void checkReference(String ref) throws IllegalReferenceException {
         if (NULL_REF.equals(ref)) {
             throw new IllegalReferenceException();
         }
     }
 
     protected String getAPIName() {
         return getAPIName(getClass());
     }
 
     public static String getAPIName(Class clazz) {
         String sn = clazz.getSimpleName();
         if (sn.toUpperCase().equals(sn)) {
             return sn;
         } else {
             return sn.toLowerCase();
         }
     }
 
     public String getReference() {
         // Try to obtain a reference by its UUID
         if (reference == null && uuid != null) {
             // Get reference is a safe op that does not throw errors
             try {
                 reference = dispatch("get_by_uuid", uuid).toString();
             } catch (BadAPICallException ex) {
                 error(ex);
             }
         }
         return reference;
     }
 
     public UUID getUUID() {
         uuid = value(uuid, "get_uuid");
         return UUID.fromString(uuid);
     }
 
     public String getIDString() {
         return getUUID().toString();
     }
 
     public void setUUID(UUID uuid) {
         this.uuid = uuid.toString();
     }
 
     /**
      * Allow us to give better names to some fields
      * @return 
      */
     protected Map<String, String> interpretation() {
         return new HashMap<>();
     }
 
     protected final void fillOut() {
         fillOut(null);
     }
 
     protected <T> T value(T obj, String name, Object... params) {
         if (obj != null) {
             return obj;
         } else {
             if (this.reference == null || this.reference.isEmpty()) {
                 return null;
             }
             try {
                 return (T) dispatch(name, params);
             } catch (BadAPICallException ex) {
                 Logger.getLogger(getClass()).warn("Getter value failed", ex);
                 return null;
             }
         }
     }
 
     protected <T> T setter(T obj, String name) throws BadAPICallException {
         if (obj == null) {
             Logger.getLogger(getClass()).error("Setter failed", new IllegalArgumentException("No value provided for setter"));
         }
         if (reference != null && !reference.isEmpty() && name != null && !name.isEmpty()) {
             dispatch(name, obj);
         }
 
         return obj;
     }
 
     protected <T extends XenApiEntity> String setter(T obj, String name) throws BadAPICallException {
         if (obj == null) {
             Logger.getLogger(getClass()).error("Setter failed", new IllegalArgumentException("No value provided for setter"));
         }
         if (reference != null && !reference.isEmpty() && name == null) {
             if (obj == null) {
                 throw new IllegalArgumentException("Null value is not allowed for " + name);
             } else {
                 dispatch(getAPIName() + "." + name, obj.getReference());
             }
         }
 
         return obj.getReference();
     }
 
     protected Object dispatch(String methodName, Object... params) throws BadAPICallException {
         ArrayList arr = new ArrayList();
         if (this.reference != null) {
             arr.add(this.reference);
         }
         arr.addAll(Arrays.asList(params));
 
         try {
             return Controller.dispatch(getAPIName() + "." + methodName, arr.toArray());
         } catch (BadAPICallException ex) {
             // Check if we can handle it
             switch (ex.getMessage()) {
                 case "OPERATION_NOT_ALLOWED":
                     ex.setErrorDescription("Tried to perform an unallowed operation");
                     break;
                 case "OTHER_OPERATION_IN_PROGRESS":
                     ex.setErrorDescription("Another operation is in progress");
                     break;
             }
 
             error(ex);
             throw ex;
         }
     }
 
     public void info(String message) {
         for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
             if (ste.getClassName().startsWith(packageName) && !ste.getClassName().equals(packageName + ".XenApiEntity")) {
                 LogKeeper.log(new LogEntry(reference, getClass().getCanonicalName(), ste.getMethodName(), message, LogEntry.Level.INFORMATION));
                 Logger.getLogger(getClass()).info(ste.getMethodName() + " : " + message);
             }
         }
     }
 
     public void warn(Exception ex) {
         parseThrowable(ex, LogEntry.Level.WARNING);
     }
 
     public void error(Exception ex) {
         parseThrowable(ex, LogEntry.Level.ERROR);
     }
 
     protected void parseThrowable(Exception ex, LogEntry.Level level) {
         // Find caller (people say that doing this is quite expensive ...)
         for (StackTraceElement ste : ex.getStackTrace()) {
             if (ste.getClassName().startsWith(packageName) && !ste.getClassName().equals(packageName + ".XenApiEntity")) {
                 log(ste.getClassName(), ste.getMethodName(), ex, level);
                 break;
             }
         }
     }
 
     protected void log(String className, String functionName, Exception ex, LogEntry.Level level) {
         // TODO check db for friendly error msg
         String title = null;
         if (ex instanceof BadAPICallException) {
             title = functionName + " : " + ((BadAPICallException) ex).getErrorName();
         } else {
             title = functionName;
         }
 
         LogKeeper.log(new LogEntry(reference, getClass().getCanonicalName(), title, ex.getMessage(), level));
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     public @interface Fill {
 
         boolean fillAPIObject() default false;
         // If the values are not returned from a get_record operation, store them externally
 
         boolean storeExternally() default false;
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     public @interface ConstructorArgument {
     }
 
     protected Map<String, Object> collectConstructorArgs() {
         HashMap<String, Object> args = new HashMap<>();
         Map<String, String> interpretation = interpretation();
         interpretation.putAll(globalInterpretation);
 
         for (Field f : ReflectionUtils.getAllFields(getClass())) {
 
             if (f.isAnnotationPresent(ConstructorArgument.class)) {
                 String keyName = f.getName();
                 if (interpretation.containsKey(keyName)) {
                     keyName = interpretation.get(keyName);
                 } else {
                     // If the keyName is fully in uppercase, preserve it
                     if (!keyName.toUpperCase().equals(keyName)) {
                         keyName = keyName.replaceAll("(.)(\\p{Lu})", "$1_$2").toLowerCase();
                     }
                 }
 
                 Object val = null;
 
                 try {
                     switch (f.getType().getName()) {
                         case "long":
                             val = "" + f.getLong(this);
                             break;
                         case "int":
                             val = "" + f.getInt(this);
                             break;
                         case "java.util.Map":
                             val = f.get(this);
                             if (val == null) {
                                 val = new HashMap();
                             }
                             break;
                         default:
                             val = f.get(this);
                             if (val instanceof Enum) {
                                 val = val.toString();
                             }
                             break;
                     }
 
                     if (val == null) {
                         val = "";
                     }
 
                     args.put(keyName, val);
                 } catch (IllegalArgumentException | IllegalAccessException ex) {
                     Logger.getLogger(getClass()).error("Reflection fail", ex);
                 }
             }
         }
 
         return args;
     }
 
     public static <T extends XenApiEntity> List<T> getAllEntities(Class<T> type) throws BadAPICallException {
         return getEntities(type, XenApiEntity.getAPIName(type) + ".get_all", null);
     }
 
     public static <T extends XenApiEntity> List<T> getEntities(Class<T> type, String methodName, String reference) throws BadAPICallException {
         Object[] records = (Object[]) (reference == null ? Controller.dispatch(methodName) : Controller.dispatch(methodName, reference));
         ArrayList<T> objects = new ArrayList<>();
         for (Object o : records) {
             String ref = (String) o;
             objects.add(CachingFacility.get(ref, type));
         }
         return objects;
     }
 
     protected <T extends XenApiEntity> List<T> getEntities(Class<T> type, String methodName) {
         try {
             return getEntities(type, getAPIName() + '.' + methodName, reference);
         } catch (BadAPICallException ex) {
             Logger.getLogger(getClass()).error("Failed to retrieve all " + type.getSimpleName() + " with " + methodName);
         }
         return null;
     }
 
     public void fillOut(Map<String, Object> data) {
         fillOut(getAPIName(), data);
     }
 
     protected final void fillOut(String className, Map<String, Object> data) {
 
         if (data == null) {
             try {
                 data = (Map<String, Object>) Controller.dispatch((className == null ? getClass().getSimpleName().toLowerCase() : className) + ".get_record", this.reference);
             } catch (BadAPICallException ex) {
                 Logger.getLogger(getClass()).error(ex);
             }
         }
 
         if (data == null) {
             throw new Error("Get record failed");
         }
 
         Map<String, String> interpretation = interpretation();
         interpretation.putAll(globalInterpretation);
 
         for (Field f : ReflectionUtils.getAllFields(getClass())) {
             
             if (Modifier.isTransient(f.getModifiers())) continue;
 
             String processedName = "";
             if (interpretation.containsKey(f.getName())) {
                 processedName = interpretation.get(f.getName());
             } else {
                 // Try exact match
                 if (data.keySet().contains(f.getName())) {
                     processedName = f.getName();
                 } else {
                     // MyNameIsHans -> my_name_is_hans
                     processedName = f.getName().replaceAll("(.)(\\p{Lu})", "$1_$2").toLowerCase();
                 }
             }
             processedName = processedName.toLowerCase();
 
             Object value = null;
             for (Map.Entry<String, Object> entry : data.entrySet()) {
                 if (entry.getKey().toLowerCase().equals(processedName)) {
                     value = entry.getValue();
                     break;
                 }
             }
             
             if (value == null) {
                 continue;
             }
 
             if (Modifier.isProtected(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
                 f.setAccessible(true);
             }
 
             try {
                 switch (f.getType().getName()) {
                     case "java.lang.String":
                         f.set(this, value);
                         break;
                     case "boolean":
                         if ((value.getClass().getName().equals("java.lang.String"))) {
                             // The API is nuts
                             f.setBoolean(this, (value.toString().equals("true") ? true : false));
                         } else {
                             f.setBoolean(this, (boolean) value);
                         }
                         break;
                     case "int":
                         // The API returns numeric values as String ><
                         if (value.getClass().getName().equals("java.lang.String")) {
                             f.set(this, Integer.parseInt(value.toString()));
                         } else {
                             f.setInt(this, (int) value);
                         }
                         break;
                     case "long":
                         if (value.getClass().getName().equals("java.lang.String")) {
                             f.set(this, Long.parseLong(value.toString()));
                         } else {
                             f.setLong(this, (long) value);
                         }
                         break;
                     case "float":
                         f.setFloat(this, (float) value);
                         break;
                     default:
                         if (f.getType().isEnum()) {
                             for (Object obj : f.getType().getEnumConstants()) {
                                 if (obj.toString().toLowerCase().equals(value.toString().toLowerCase())) {
                                     f.set(this, obj);
                                 }
                             }
                         } else if (f.isAnnotationPresent(Fill.class)) {
                             Object casted = f.getType().cast(value);
                             /*if (XenApiEntity.class.isAssignableFrom(f.getType()) && f.getAnnotation(Fill.class).fillAPIObject()) {
                             }*/
                             f.set(this, casted);
                         }
                         break;
                 }
             } catch (IllegalAccessException | IllegalArgumentException | ClassCastException ex) {
                Logger.getLogger(getClass()).error("Failed to fill out object field " + f.getName(), ex);
             }
         }
     }
 }
