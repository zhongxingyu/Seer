 /**
  * Copyright 2012 J. Miguel P. Tavares
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ***************************************************************************/
 package org.bitpipeline.lib.friendlyjson;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /** The base of all JSON entities.
  * Classes that extend this abstract class can be easily serialized into JSON... well, with some very "strict" rules.
  *
  * Classes that extend this class can have fields defined as:
  * <ul>
  *    <li>java primitives (boolean, byte, char, short, int, long, float and double)</li>
  *    <li>java.util.Map&lt;String, ?&gt;</li>
  *    <li>java.util.List&lt;?&gt;</li>
  * </ul>
  *
  * To convert the instances to JSON objects just call the method
  * <tt>toJSON ()</tt>. To create a instance of the object from JSON use the
  * constructor with <tt>JSONObject</tt> as parameter. */
 public abstract class JSONEntity {
 	private static final String MSG_MUST_HAVE_CONSTRUCTOR = "JSONEntity must implement a constructor with a JSONObject as argument.";
 
 	public JSONEntity () {
 	}
 
 	public JSONEntity (String jsonString) throws JSONMappingException, JSONException {
 		this (new JSONObject (jsonString));
 	}
 	
 	/** Des-serialize a JSON object.
 	 * @throws JSONException
 	 * @throws JSONMappingException */
 	public JSONEntity (JSONObject json) throws JSONMappingException {
 		if (json == null)
 			return;
		for (Field field : this.getClass ().getDeclaredFields ()) {
 			if ( (field.getModifiers () & Modifier.TRANSIENT) != 0) { // don't care about transient fields.
 				continue;
 			}
 
 			String fieldName = field.getName ();
 			if (fieldName.equals ("this$0"))
 				continue;
 
 			boolean accessible = field.isAccessible ();
 			if (!accessible)
 				field.setAccessible (true);
 
 			Class<?> type = field.getType ();
 
 			if (type.isArray ()) {
 				Class<?> componentClass = type.getComponentType ();
 				JSONArray jsonArray = null;
 				try {
 					jsonArray = json.getJSONArray (fieldName);
 				} catch (JSONException e) {
 					// no data for this field found.
 					continue;
 				}
 
 				int size = jsonArray.length ();
 
 				Object array = Array.newInstance (componentClass, size);
 				for (int i=0; i<size; i++) {
 					try {
 						Object item = fromJson (componentClass, jsonArray.get (i));
 						Array.set (array, i, item);
 					} catch (JSONException e) {
 						// item not found?
 					}
 				}
 				
 				try {
 					field.set (this, array);
 				} catch (Exception e) {
 					throw new JSONMappingException (e);
 				}
 			} else if (JSONEntity.class.isAssignableFrom (type)) {
 				try {
 					Object entity = readJSONEntity (type, json.getJSONObject (fieldName));
 					field.set (this, entity);
 				} catch (JSONException e) {
 					// keep going.. the json representation doesn't have value for this field.
 				} catch (Exception e) {
 					throw new JSONMappingException (e);
 				}
 			} else {
 				FieldSetter setter = JSON_READERS.get (type.getName ());
 				if (setter != null) {
 					try {
 						setter.setField (this, field, json, fieldName);
 					} catch (JSONException e) {
 						// do nothing. We just didn't receive data for this field
 					} catch (Exception e) {
 						throw new JSONMappingException (e);
 					}
 				} else {
 					System.err.println ("No setter for " + field);
 				}
 			}
 
 			if (!accessible)
 				field.setAccessible (false);
 		}
 	}
 
 	private Object readJSONEntity (Class<?> clazz, JSONObject json) throws JSONMappingException {
 		Class<?> c = clazz;
 
 		Constructor<?> constructor = null;
 		try {
 			constructor = c.getConstructor (JSONObject.class);
 		} catch (Exception e) {
 			throw new JSONMappingException (JSONEntity.MSG_MUST_HAVE_CONSTRUCTOR, e);
 		}
 
 		if (constructor == null)
 			return null;
 
 		Object entity = null;
 		try {
 			entity = constructor.newInstance (json);
 		} catch (Exception e) {
 			throw new JSONMappingException (e);
 		}
 		return entity;
 	}
 
 	static interface FieldSetter {
 		void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception;
 	}
 
 	private static interface JSON2Obj {
 		Object convert (Object json) throws JSONException;
 	}
 
 	/** Read from the JSON into Java objects */
 	final protected static HashMap<String, FieldSetter> JSON_READERS = new HashMap<String, FieldSetter> ();
 	/** Convert from JSON complex objects into Java Objects. */
 	final protected static HashMap<String, JSON2Obj> JSON_CONVERTERS = new HashMap<String, JSON2Obj> ();
 
 	static {
 		JSON_READERS.put ("boolean", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setBoolean (obj, json.getBoolean (jsonName));
 			}
 		});
 		JSON_READERS.put ("byte", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setByte (obj, (byte) json.getInt (jsonName));
 			}
 		});
 		JSON_READERS.put ("char", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				String string = json.getString (jsonName);
 				if (string == null || string.length () == 0)
 					return;
 				char charAt = string.charAt (0);
 				field.setChar (obj, charAt);
 			}
 		});
 		JSON_READERS.put ("short", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setShort (obj, (short) json.getInt (jsonName));
 			}
 		});
 		JSON_READERS.put ("int", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setInt (obj, json.getInt (jsonName));
 			}
 		});
 		JSON_READERS.put ("long", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setLong (obj, json.getLong (jsonName));
 			}
 		});
 		JSON_READERS.put ("float", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setFloat (obj, (float) json.getDouble (jsonName));
 			}
 		});
 		JSON_READERS.put ("double", new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.setDouble (obj, json.getDouble (jsonName));
 			}
 		});
 
 		JSON_READERS.put (String.class.getName (), new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				field.set (obj, json.getString (jsonName));
 			}
 		});
 
 		JSON_READERS.put (List.class.getName (), new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				JSONArray jsonArray = json.getJSONArray (jsonName);
 				
 				ParameterizedType genericType = (ParameterizedType) field.getGenericType ();
 				Class<?> listClass = (Class<?>)genericType.getActualTypeArguments ()[0];
 				
 				ArrayList<Object> list = new ArrayList<Object> (jsonArray.length ());
 				for (int i=0; i<jsonArray.length (); i++) {
 					Object entry = jsonArray.get (i);
 					list.add (JSONEntity.fromJson (listClass, entry));
 				}
 				field.set (obj, list);
 			}
 		});
 
 		JSON_READERS.put (Map.class.getName (), new FieldSetter () {
 			public void setField (Object obj, Field field, JSONObject json, String jsonName) throws Exception {
 				JSONObject jsonMap = json.getJSONObject (jsonName);
 				if (jsonMap == null)
 					return;
 
 				HashMap<Object, Object> map = new HashMap <Object, Object> (jsonMap.length ());
 				Iterator<?> keys = jsonMap.keys ();
 				while (keys.hasNext ()) {
 					Object key = keys.next ();
 					Object jsonValue = jsonMap.get (key.toString ());
 					map.put (key, JSONEntity.fromJson (jsonValue));
 				}
 				field.set (obj, map);
 			}
 		});
 
 
 		JSON_CONVERTERS.put (List.class.getName (), new JSON2Obj () {
 			public Object convert (Object json) throws JSONException {
 				JSONArray jsonArray = (JSONArray) json;
 				Object[] array = new Object[jsonArray.length ()];
 
 				for (int i=0; i<jsonArray.length (); i++) {
 					Object jsonValue = jsonArray.get (i);
 					array[i] = JSONEntity.fromJson (jsonValue);
 				}
 				return Arrays.asList (array);
 			}
 		});
 
 		JSON_CONVERTERS.put (JSONObject.class.getName (), new JSON2Obj () {
 			public Object convert (Object json) throws JSONException {
 				JSONObject jsonMap = (JSONObject) json;
 				Map<Object, Object> map = new HashMap <Object, Object> (jsonMap.length ());
 				Iterator<?> keys = jsonMap.keys ();
 				while (keys.hasNext ()) {
 					Object key = keys.next ();
 					Object jsonValue = jsonMap.get (key.toString ());
 					map.put (key, JSONEntity.fromJson (jsonValue));
 				}
 				return map;
 			}
 		});
 
 		JSON_CONVERTERS.put (JSONArray.class.getName (), new JSON2Obj () {
 			public Object convert (Object json) throws JSONException {
 				JSONArray jsonArray = (JSONArray) json;
 				Object[] array = new Object[jsonArray.length ()];
 
 				for (int i=0; i<jsonArray.length (); i++) {
 					Object jsonValue = jsonArray.get (i);
 					array[i] = JSONEntity.fromJson (jsonValue);
 				}
 				return Arrays.asList (array);
 			}
 		});
 	}
 
 	private static Object fromJson (Object json) throws JSONException {
 		if (json == null)
 			return null;
 		JSON2Obj json2Obj = JSONEntity.JSON_CONVERTERS.get (json.getClass ().getName ());
 		if (json2Obj != null) {
 			return json2Obj.convert (json);
 		}
 		return json;
 	}
 
 	private static Object fromJson (Class<?> clazz, Object json) throws JSONMappingException {
 		if (json == null)
 			return null;
 		Object fromJson = null;
 		if (clazz != null && JSONEntity.class.isAssignableFrom (clazz)) {
 			Constructor<?> constructor;
 
 			if ((clazz.getModifiers () & Modifier.STATIC) == 0) {
 				Class<?> enclosingClass = clazz.getEnclosingClass ();
 				try {
 					constructor = clazz.getDeclaredConstructor (enclosingClass, JSONObject.class);
 					fromJson = constructor.newInstance (null, json); // NULL?? What should we actually use here?
 					return fromJson;
 				} catch (Exception e) {
 					throw new JSONMappingException (JSONEntity.MSG_MUST_HAVE_CONSTRUCTOR, e);
 				}
 			}
 			try {
 				constructor = clazz.getDeclaredConstructor (JSONObject.class);
 				fromJson = constructor.newInstance (json);
 				return fromJson;
 			} catch (Exception e) {
 				throw new JSONMappingException (JSONEntity.MSG_MUST_HAVE_CONSTRUCTOR, e);
 			}
 		}
 
 		try {
 			fromJson = JSONEntity.fromJson (json);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new JSONMappingException (e);
 		}
 		return fromJson;
 	}
 
 	/* ---------------------- */
 
 	/** */
 	static private Object toJson (Object obj) throws JSONMappingException {
 		if (obj == null)
 			return null;
 		Object json = obj;
 
 		if (obj instanceof JSONEntity) {
 			json = ((JSONEntity)obj).toJson ();
 		} else if (obj instanceof Map) {
 			json = new JSONObject ();
 			for (Object mapKey : ((Map<?, ?>)obj).keySet ()) {
 				Object jsonValue = toJson (((Map<?, ?>)obj).get (mapKey));
 				try {
 					((JSONObject)json).put (mapKey.toString (), jsonValue);
 				} catch (JSONException e) {
 					throw new JSONMappingException (e);
 				}
 			}
 
 		} else if (obj instanceof List) {
 			json = new JSONArray ();
 			for (Object listItem : ((List<?>)obj)) {
 				Object jsonListItem = toJson(listItem);
 				((JSONArray)json).put (jsonListItem);
 			}
 		} else if (obj.getClass ().isArray ()) {
 			json = new JSONArray ();
 			Object[] array = (Object[]) obj;
 			for (Object item : array) {
 				Object jsonItem = toJson (item);
 				((JSONArray)json).put (jsonItem);
 			}
 		} else {
 			json = obj;
 		}
 		return json;
 	}
 
 	public JSONObject fillWithClass (JSONObject json, Class<?> clazz) throws JSONMappingException {
 		for (Field field : clazz.getDeclaredFields ()) {
 			if ( (field.getModifiers () & Modifier.TRANSIENT) != 0) { // don't care about transient fields.
 				continue;
 			}
 
 			String jsonName = field.getName ();
 			if (jsonName.equals ("this$0"))
 				continue;
 
 			boolean accessible = field.isAccessible ();
 
 			if (!accessible)
 				field.setAccessible (true);
 
 			try {
 				Object value = field.get (this);
 				Object jsonField = toJson (value);
 				json.put (jsonName, jsonField);
 			} catch (Exception e) {
 				throw new JSONMappingException (e);
 			}
 
 			if (!accessible)
 				field.setAccessible (false);
 		}
 		
 		return json;
 	}
 
 	/** Serialize the object into a JSON object.
 	 * @throws JSONMappingException */
 	public JSONObject toJson () throws JSONMappingException {
 		JSONObject json = new JSONObject ();
 		Class<?> clazz = this.getClass ();
 		do {
 			json = fillWithClass (json, clazz);
 			clazz = clazz.getSuperclass ();
 		} while (clazz != null && !clazz.isAssignableFrom (JSONEntity.class));
 		
 		return json;
 	}
 
 	/** 
 	 * @return <tt>null</tt> if there was a problem converting the object into a JSON representation,
 	 *         a String representing the object with a JSON syntax otherwise. */
 	public String toString () {
 		try {
 			return toJson().toString ();
 		} catch (JSONMappingException e) {
 			return null;
 		}
 	}
 	
 	public String toString (int indent) {
 		try {
 			return toJson().toString (indent);
 		} catch (Exception e) {
 			StringWriter sw = new StringWriter ();
 			e.printStackTrace (new PrintWriter (sw));
 			return sw.toString ();
 		}
 	}
 }
