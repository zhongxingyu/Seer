 /**
  * 
  */
 package org.webreformatter.commons.json;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.webreformatter.commons.json.IJsonAccessor.JsonType;
 
 /**
  * @author kotelnikov
  */
 public class JsonObject extends JsonValue {
 
     public static IJsonValueFactory<JsonObject> FACTORY = OBJECT_FACTORY;
 
     public static JsonObject newValue(Object o) {
         return FACTORY.newValue(o);
     }
 
     public static String resolve(JsonObject object, String pattern) {
         StringBuilder buf = new StringBuilder();
         if (pattern != null) {
             String[] segments = pattern.split("[\\/]");
             for (String segment : segments) {
                 if (buf.length() > 0) {
                     buf.append("/");
                 }
                 String[] parts = segment.split("[{}]");
                 for (int i = 0; i < parts.length; i++) {
                     String part = parts[i];
                     if ((i % 2) == 0) {
                         buf.append(part);
                     } else {
                         String value = object.getString(part);
                         buf.append(value);
                     }
                 }
             }
         }
         return buf.toString();
     }
 
     public JsonObject() {
         super((Object) null);
     }
 
     /**
      * @param accessor
      * @param object
      */
     protected JsonObject(Object object) {
         super(object);
     }
 
     /**
      * Creates an fills values from the internal array in the given collection.
      * 
      * @param <W>
      * @param name the name of the property
      * @param collection the result collection where all values from the
      *        original internal array should be added
      * @param factory the factory used to create JSON wrappers for each array
      *        value
      */
     private <W, C extends Collection<? super W>> C addValues(
         String name,
         C collection,
         IJsonValueFactory<W> factory) {
         Object array = getArrayObject(name, false);
         return addValues(array, collection, factory);
     }
 
     /**
      * Returns the value of the specified type
      * 
      * @param <T> the expected type of the returned value
      * @param name the name of the property
      * @param type the expected type of the returned property
      * @return the value of the specified type
      */
     @SuppressWarnings("unchecked")
     private <T> T get(String name, JsonType type) {
         Object value = fAccessor.getValue(fObject, name);
         return (T) (fAccessor.getType(value) == type ? value : null);
     }
 
     /**
      * Returns a JsonArray instance corresponding to the specified key.
      * 
      * @param name the key of the property corresponding to an array
      * @param create if this flag is <code>true</code> then this method can
      *        creates a new array if the specified key does not correspond to an
      *        array (or it is <code>null</code>).
      * @return a JsonArray instance corresponding to the specified key
      */
     public JsonArray getArray(String name, boolean create) {
         Object obj = getArrayObject(name, create);
         if (obj == null) {
             return null;
         }
         JsonArray array = new JsonArray(obj);
         return array;
     }
 
     /**
      * Returns an array corresponding to the specified property name.
      * 
      * @param name the name of the property
      * @param create if this flag is <code>true</code> and the specified
      *        property does not contain an array then a new one will be created
      *        and initialized
      * @return the array corresponding to the specified property name
      */
     private Object getArrayObject(String name, boolean create) {
         Object array = fAccessor.getValue(fObject, name);
         if (fAccessor.getType(array) != JsonType.ARRAY) {
             if (create) {
                 array = fAccessor.newArray();
                 fAccessor.setValue(fObject, name, array);
             } else {
                 array = null;
             }
         }
         return array;
     }
 
     /**
      * Returns the specified property as a boolean value.
      * 
      * @param name the name of the property
      * @param defaultValue the default value; used if the property is not
      *        defined
      * @return the specified property as a boolean value.
      */
     public boolean getBoolean(String name, boolean defaultValue) {
         Object value = get(name, JsonType.BOOLEAN);
        return fAccessor.toBoolean(value);
     }
 
     /**
      * Returns the specified property as a float value.
      * 
      * @param name the name of the property
      * @param defaultValue the default value; used if the property is not
      *        defined
      * @return the specified property as a float value.
      */
     public double getDouble(String name, double defaultValue) {
         Object value = get(name, JsonType.DOUBLE);
        return fAccessor.toDouble(value);
     }
 
     /**
      * Returns the specified property as a integer value.
      * 
      * @param name the name of the property
      * @param defaultValue the default value; used if the property is not
      *        defined
      * @return the specified property as a integer value.
      */
     public int getInteger(String name, int defaultValue) {
         Object value = get(name, JsonType.INTEGER);
        return fAccessor.toInteger(value);
     }
 
     /**
      * Returns a set of property names.
      * 
      * @return a set of property names
      */
     public Set<String> getKeys() {
         Set<String> result = fAccessor.getObjectKeys(fObject);
         return result;
     }
 
     /**
      * Creates and returns an array of JSON wrappers corresponding to the
      * specified property name.
      * 
      * @param <W> the type of the wrapper
      * @param name the name of the property
      * @param factory the factory used to create new wrappers
      * @return an array of JsonObject instances
      */
     public <W> ArrayList<W> getList(String name, IJsonValueFactory<W> factory) {
         return addValues(name, new ArrayList<W>(), factory);
     }
 
     /**
      * Returns the specified property as a long value.
      * 
      * @param name the name of the property
      * @param defaultValue the default value; used if the property is not
      *        defined
      * @return the specified property as a long value.
      */
     public long getLong(String name, long defaultValue) {
         Object value = get(name, JsonType.LONG);
        return fAccessor.toLong(value);
     }
 
     /**
      * Returns the specified property as a wrapper of the a specific type.
      * 
      * @param <W> the type of the returned wrapper
      * @param name the name of the property
      * @param factory the factory used to create new wrappers
      * @return the specified property as a wrapper of the a specific type.
      */
     public <W> W getObject(String name, IJsonValueFactory<W> factory) {
         return getValue(name, factory);
     }
 
     /**
      * Creates and returns a set of JSON wrappers corresponding to the specified
      * property name.
      * 
      * @param <W> the type of the wrapper
      * @param name the name of the property
      * @param factory the factory used to create new wrappers
      * @return a set of JsonObject instances
      */
     public <W> Set<W> getSet(String name, IJsonValueFactory<W> factory) {
         return addValues(name, new LinkedHashSet<W>(), factory);
     }
 
     /**
      * Returns the specified property as a string.
      * 
      * @param name the name of the property
      * @return the specified property as a string.
      */
     public String getString(String name) {
         Object value = get(name, JsonType.STRING);
         return fAccessor.toString(value);
     }
 
     /**
      * Returns a JSON value corresponding to the specified key.
      * 
      * @param name the key of the property
      * @return a JSON value corresponding to the specified key
      */
     public Object getValue(String name) {
         return getValue(name, NULL_FACTORY);
     }
 
     /**
      * Returns the specified property as a wrapper of the a specific type.
      * 
      * @param <W> the type of the returned wrapper
      * @param name the name of the property
      * @param factory the factory used to create new wrappers
      * @return the specified property as a wrapper of the a specific type.
      */
     public <W> W getValue(String name, IJsonValueFactory<W> factory) {
         Object value = fAccessor.getValue(fObject, name);
         return value != null ? factory.newValue(value) : null;
     }
 
     @Override
     protected Object newJsonInstance() {
         return fAccessor.newObject();
     }
 
     /**
      * Removes the property with the specified name;
      * 
      * @param name the name of the property to remove
      */
     public JsonObject removeValue(String name) {
         fAccessor.removeValue(fObject, name);
         return this;
     }
 
     /**
      * Replaces an existing value (if any) of the property with the givene name
      * by a new value.
      * 
      * @param name the property name
      * @param value the value of to set
      */
     public JsonObject setValue(String name, Object value) {
         Object val = toJsonValue(value);
         fAccessor.setValue(fObject, name, val);
         return this;
     }
 
     /**
      * Replaces the property array by the given values
      * 
      * @param name the name of the property
      * @param values the values to set
      */
     public JsonObject setValues(String name, Iterable<?> values) {
         removeValue(name);
         Object array = getArrayObject(name, true);
         int i = 0;
         for (Object value : values) {
             Object val = toJsonValue(value);
             fAccessor.setArrayValue(array, i, val);
             i++;
         }
         return this;
     }
 
     /**
      * Replaces the property array by the given values
      * 
      * @param <T> the type of values to set
      * @param name the name of the property
      * @param values the values to set
      */
     public <T> void setValues(String name, T... values) {
         removeValue(name);
         Object array = getArrayObject(name, true);
         int i = 0;
         for (Object value : values) {
             Object val = toJsonValue(value);
             fAccessor.setArrayValue(array, i, val);
             i++;
         }
     }
 
 }
