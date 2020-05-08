 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import net.wgr.core.ReflectionUtils;
 import net.wgr.core.data.DAObjectMatcher;
 import net.wgr.core.data.LazyQuery;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 import net.wgr.xenmaster.monitoring.LogEntry;
 import net.wgr.xenmaster.monitoring.LogKeeper;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 2, 2011
  * @author double-u
  */
 public class XenApiEntity {
 
     protected String reference;
     protected String uuid;
     protected final transient String packageName = getClass().getPackage().getName();
 
     public XenApiEntity() {
     }
 
     public XenApiEntity(String ref) {
         this(ref, ref != null);
     }
 
     public XenApiEntity(String ref, boolean autoFill) {
         this.reference = ref;
         if (autoFill) {
             fillOut(getAPIName(), null);
         }
     }
 
     protected String getAPIName() {
         String sn = getClass().getSimpleName();
         if (sn.toUpperCase().equals(sn)) {
             return sn;
         } else {
             return sn.toLowerCase();
         }
     }
 
     public String getReference() {
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
        } else {
            Logger.getLogger(getClass()).warn("Entity has no reference and/or setter name is not provided");
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
         CollectionUtils.addAll(arr, params);
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
         Logger.getLogger(getClass()).error(title, ex);
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
 
     public void persistFields() {
         for (Field f : ReflectionUtils.getAllFields(getClass())) {
             if (f.isAnnotationPresent(ConstructorArgument.class) && Map.class.isAssignableFrom(f.getType())) {
                 try {
                     Fill fill = f.getAnnotation(Fill.class);
                     if (fill.storeExternally()) {
                         PersistedField pf = new PersistedField(reference, f.getName(), f.get(this));
                         pf.insert();
                     }
                 } catch (IllegalAccessException | IllegalArgumentException ex) {
                     Logger.getLogger(getClass()).error("Failed to persist field", ex);
                 }
             }
         }
     }
 
     protected static class PersistedField extends net.wgr.core.dao.Object {
 
         protected String reference, name;
         protected Object value;
         protected static String COLUMN_FAMILY = "xapiPersistence";
 
         @Override
         public String getColumnFamily() {
             return COLUMN_FAMILY;
         }
 
         @Override
         public String getKeyFieldName() {
             return "reference";
         }
 
         public PersistedField(String reference, String name, Object value) {
             this.reference = reference;
             this.name = name;
             this.value = value;
         }
 
         public String getName() {
             return name;
         }
 
         public String getReference() {
             return reference;
         }
 
         public Object getValue() {
             return value;
         }
 
         private static List<PersistedField> getAllForReference(final String reference) {
             LazyQuery<PersistedField> lq = new LazyQuery(COLUMN_FAMILY, LazyQuery.ResultStrategy.FIND_ALL);
             lq.addMatcher(new DAObjectMatcher<PersistedField>(PersistedField.class) {
 
                 @Override
                 public boolean match(PersistedField object) {
                     if (object.getReference().equals(reference)) {
                         return true;
                     }
                     return false;
                 }
             });
 
             lq.run();
             return new ArrayList<>(lq.getResults().values());
         }
     }
 
     protected Map<String, Object> collectConstructorArgs() {
         HashMap<String, Object> args = new HashMap<>();
         Map<String, String> interpretation = interpretation();
 
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
 
         for (Field f : ReflectionUtils.getAllFields(getClass())) {
 
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
 
             Object value = null;
             for (Map.Entry<String, Object> entry : data.entrySet()) {
                 if (entry.getKey().equals(processedName)) {
                     value = entry.getValue();
                     break;
                 }
             }
 
             List<PersistedField> persistedFields = null;
 
             // Some fields are persisted, check if there are any and load them
             if (value == null) {
                 if (f.isAnnotationPresent(Fill.class) && f.getAnnotation(Fill.class).storeExternally()) {
                     if (persistedFields == null) {
                         persistedFields = PersistedField.getAllForReference(this.reference);
                     }
 
                     for (PersistedField ps : persistedFields) {
                         if (ps.getName().equals(f.getName())) {
                             value = ps.getValue();
                         }
                     }
                 }
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
                             String corrected = value.toString().replace("1", "true").replace("0", "false");
                             f.setBoolean(this, Boolean.parseBoolean(corrected));
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
             } catch (IllegalAccessException | IllegalArgumentException ex) {
                 Logger.getLogger(getClass()).error(ex);
             }
         }
     }
 }
