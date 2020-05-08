 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.engage.types;
 
 /*
  * XXX some of these getAs<T> methods return a copy of the value (getAsDictionary,
  * getAsProviderList) and some return a reference to the value.
  */
 
 import android.text.TextUtils;
 import android.util.Log;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.utils.StringUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 import org.json.JSONTokener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * @class JRDictionary
  * @brief A dictionary that maps \e String keys to values of different types
  *
  * @nosubgrouping
  *
  * @internal
  * iPhone dictionary work-alike class
  * @endinternal
  **/
 public final class JRDictionary extends HashMap<String, Object> {
 	public static final String DEFAULT_VALUE_STRING = "";
 	public static final int DEFAULT_VALUE_INT = -1;
 	public static final boolean DEFAULT_VALUE_BOOLEAN = false;
 
     private static final String TAG = JRDictionary.class.getSimpleName();
 
 /**
  * @name Constructors
  * Constructors for JRDictionary
  **/
 /*@{*/
     /**
      * Default constructor.
      **/
     public JRDictionary() {
         super();
     }
 
 /**
  * @name JSON Serialization
  * Methods that serialize/deserialize the JRDictionary to/from JSON
  **/
 /*@{*/
     /**
      * Serializes the specified dictionary object to a JSON string.
      *
      * @return
      *      JSON representation of the specified JRDictionary object
      **/
     public String toJSON() {
         JSONStringer jsonStringer = new JSONStringer();
 
         try {
             jsonify(this, jsonStringer);
             return jsonStringer.toString();
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static void jsonify(Object object, JSONStringer jsonStringer) throws JSONException {
         if (object instanceof JRDictionary) {
             jsonStringer.object();
             for (String key : ((JRDictionary) object).keySet()) {
                 jsonStringer.key(key);
                 jsonify(((JRDictionary) object).get(key), jsonStringer);
             }
             jsonStringer.endObject();
         } else if (object instanceof Object[]) {
             jsonStringer.array();
             for (Object o : (Object[]) object) {
                 jsonify(o, jsonStringer);
             }
             jsonStringer.endArray();
         } else if (object instanceof Collection) {
             jsonify(((Collection) object).toArray(), jsonStringer);
         } else if (object instanceof String) {
             jsonStringer.value(object);
         } else if (object instanceof Boolean) {
             jsonStringer.value(object);
         } else if (object instanceof Integer) {
             jsonStringer.value(object);
         } else if (object instanceof Double) {
             jsonStringer.value(object);
         } else if (object instanceof Long) {
             jsonStringer.value(object);
         } else if (object instanceof JRJsonifiable) {
             jsonify(((JRJsonifiable) object).toJRDictionary(), jsonStringer);
         } else if (object == null) {
             jsonStringer.value(JSONObject.NULL);
         } else {
             throw new RuntimeException("Unexpected jsonify value: " + object);
         }
     }
 
     /**
      * Deserializes the specified JSON string to a JRDictionary instance.
      *
      * @param json
      *      The JSON string to be deserialized.
      *
      * @return
      *      A JRDictionary object representation of the JSON string
      *
      * @throws JSONException
      *      When the JSON couldn't be parsed.
      **/
     public static JRDictionary fromJSON(String json) throws JSONException {
         JSONTokener jsonTokener = new JSONTokener(json);
 
         Object jsonObject = jsonTokener.nextValue();
 
         try {
             return (JRDictionary) unjsonify(jsonObject);
         } catch (ClassCastException e) {
             throw new JSONException(e.toString());
         }
     }
 
     private static Object unjsonify(Object jsonValue) throws JSONException {
         if (jsonValue instanceof JSONArray) {
             ArrayList returnArray = new ArrayList();
             for (int i=0; i < ((JSONArray) jsonValue).length(); i++) {
                 returnArray.add(unjsonify(((JSONArray) jsonValue).get(i)));
             }
             return returnArray;
         } if (jsonValue instanceof JSONObject) {
             JRDictionary returnDictionary = new JRDictionary();
             Iterator<String> i = ((JSONObject) jsonValue).keys();
             while (i.hasNext()) {
                 String key = i.next();
                 Object value = ((JSONObject) jsonValue).get(key);
                 returnDictionary.put(key, unjsonify(value));
             }
             return returnDictionary;
         } if (jsonValue instanceof Boolean) {
             return jsonValue;
         } if (jsonValue == JSONObject.NULL) {
             return null;
         } if (jsonValue == null) {
             Log.e(TAG, "unexpected null primitive non-sentinel non-JSONObject.NULL");
             return null;
         } if (jsonValue instanceof Double) {
             return jsonValue;
         } if (jsonValue instanceof Integer) {
            return jsonValue;
         } if (jsonValue instanceof Long) {
             return ((Long) jsonValue).doubleValue();
         } if (jsonValue instanceof String) {
             return jsonValue;
         } else {
             throw new RuntimeException("unexpected unjsonify token");
         }
     }
 
     @Deprecated
     @Override
     public Object put(String key, Object value) {
         if (value instanceof JRDictionary || value instanceof String || value instanceof Number ||
                 value instanceof Collection || value instanceof Object[] || value == null
                 || value instanceof Boolean) {
             return super.put(key, value);
         } else {
             throw new IllegalArgumentException("Non-jsonifiable object could not be added to JRDictionary");
             //Log.e(TAG, "Non-jsonifiable object added to JRDictionary");
             //return super.put(key, value);
         }
      }
 
     @Deprecated
     @Override
     public void putAll(Map<? extends String, ?> map) {
         throw new UnsupportedOperationException();
         //super.putAll(map);
     }
 
     public Object put (String key, String value) {
         return super.put(key, value);
     }
 
     public Object put (String key, Integer value) {
         return super.put(key, value);
     }
 
     public Object put (String key, Long value) {
         return super.put(key, value);
     }
 
     public Object put (String key, Double value) {
         return super.put(key, value);
     }
 
     public Object put (String key, JRDictionary value) {
         return super.put(key, value);
     }
 
     public Object put (String key, Object[] value) {
         return super.put(key, value);
     }
 
     public Object put (String key, Boolean value) {
         return super.put(key, value);
     }
 
     public Object put (String key, Collection value) {
         return super.put(key, value);
     }
 
     public Object put (String key, JRJsonifiable value) {
         return super.put(key, value);
     }
 
     /*@}*/
 
 /**
  * @name Getting Dictionary Content
  * Methods that return typed values given a \e String key
  **/
 /*@{*/
 	/**
 	 * Convenience method used to retrieve a named value as a \e String object.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @return
 	 * 		The \e String object if found, empty string otherwise
 	 **/
 	public String getAsString(String key) {
 		return getAsString(key, DEFAULT_VALUE_STRING);
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as a \e String object.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @param defaultValue
 	 * 		The value to be returned if the key is not found
 	 *
 	 * @return
 	 * 		The \e String value if found, value of \e defaultValue otherwise
 	 **/
 	public String getAsString(String key, String defaultValue) {
 		return (containsKey(key)) ? (String)get(key) : defaultValue;
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as an \e int.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @return
 	 * 		The \e int value if found, \c -1 otherwise
 	 **/
 	public int getAsInt(String key) {
 		return getAsInt(key, DEFAULT_VALUE_INT);
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as an \e int.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @param defaultValue
 	 * 		The value to be returned if the key is not found
 	 *
 	 * @return
 	 * 		The \e int value if found, value of \e defaultValue otherwise
 	 **/
 	public int getAsInt(String key, int defaultValue) {
 		int retval = defaultValue;
 		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
 			Object value = get(key);
 			if (value instanceof Integer) {
 				retval = (Integer) value;
 			} else if (value instanceof String) {
 				String strValue = (String)value;
 				if (!TextUtils.isEmpty(strValue)) {
 					try {
 						retval = Integer.parseInt(strValue);
 					} catch (Exception ignore) {
 						// string value is not an integer...return default value...
 					}
 				}
 			}
 		}
 		return retval;
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as a \e boolean.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @return
 	 * 		The \e boolean value if found, \c false otherwise
 	 **/
 	public boolean getAsBoolean(String key) {
 		return getAsBoolean(key, DEFAULT_VALUE_BOOLEAN);
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as a \e boolean.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @param defaultValue
 	 * 		The value to be returned if the key is not found
 	 *
 	 * @return
 	 * 		The \e boolean value if found, value of \e defaultValue otherwise
 	 **/
 	public boolean getAsBoolean(String key, boolean defaultValue) {
 		boolean retval = defaultValue;
 		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
 			Object value = get(key);
 			if (value instanceof Boolean) {
 				retval = (Boolean) value;
 			} else if (value instanceof String) {
 				String strValue = (String)value;
                 retval = StringUtils.stringToBoolean(strValue, false);
 			}
 		}
 		return retval;
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as a JRDictionary.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @return
 	 * 		The JRDictionary value if key is found, \e null otherwise
 	 **/
 	public JRDictionary getAsDictionary(String key) {
 		return getAsDictionary(key, false);
 	}
 
 	/**
 	 * Convenience method used to retrieve a named value as a JRDictionary.
 	 *
 	 * @param key
 	 * 		The key of the value to be retrieved
 	 *
 	 * @param shouldCreateIfNotFound
 	 * 		Flag indicating whether or not a new JRDictionary object should be created if the
 	 * 		specified key does not exist
 	 *
 	 * @return
 	 * 		The JRDictionary value if key is found, empty object or \e null otherwise (based on value
 	 *      of the \e shouldCreateIfNotFound flag)
 	 **/
 	public JRDictionary getAsDictionary(String key, boolean shouldCreateIfNotFound) {
 		JRDictionary retval = null;
 		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
 			Object value = get(key);
 			if (value instanceof JRDictionary) {
 				retval = (JRDictionary)value;
 			} else {
                 throw new RuntimeException("Unexpected type in JRDictionary");
             }
 		}
 
 		return ((retval == null) && shouldCreateIfNotFound) ? new JRDictionary() : retval;
 	}
 
     /**
      * Convenience method used to retrieve a named value as an array of strings.
      *
      * @param key
      * 		The key of the value to be retrieved
      *
      * @return
      * 		The \e ArrayList<String> value if key is found, \e null otherwise
      **/
     public ArrayList<String> getAsListOfStrings(String key) {
         return getAsListOfStrings(key, false);
     }
 
     /**
      * Convenience method used to retrieve a named value as an array of strings.
      *
      * @param key
      * 		The key of the value to be retrieved
      *
      * @param shouldCreateIfNotFound
      * 		Flag indicating whether or not a new \e ArrayList<String> object should be created
      * 		if the specified key does not exist
      *
      * @return
      * 		The \e ArrayList<String> value if key is found, empty array or \e null otherwise (based on value
 	 *      of the \e shouldCreateIfNotFound flag)
      **/
 
     // We runtime type check the return value so we can safely ignore this unchecked
     // assignment error.
     @SuppressWarnings("unchecked")
     public ArrayList<String> getAsListOfStrings(String key, boolean shouldCreateIfNotFound) {
         ArrayList<String> retval = null;
         if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
             Object value = get(key);
             if (value instanceof ArrayList) {
                 for (Object v : (ArrayList) value) assert v instanceof String;
                 retval = (ArrayList<String>)value;
             }
         }
 
         return ((retval == null) && shouldCreateIfNotFound)
             ? new ArrayList<String>()
             : retval;
     }
 /*@}*/
 
     /**
      * @internal
      * Convenience method used to retrieve a named value as a JRProvider
      *
      * @param key
      *      The key of the value to be retrieved
      *
      * @return
      *      The JRProvider object if found, \e null otherwise
      **/
     public JRProvider getAsProvider(String key) {
         JRProvider retval = null;
         if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
             Object value = get(key);
             if (value instanceof JRProvider) {
                 retval = (JRProvider) value;
             }
         }
 
         return retval;
     }
 
 /**
  * @name Miscellaneous
  * Miscellaneous methods
  **/
 /*@{*/
     /**
      * Utility method used to check if a dictionary object is "empty", that is, it is \e null or
      * contains zero items
      *
      * @param dictionary
      *      The dictionary object to be tested
      *
      * @return
      *      \c true if the dictionary is null or contains zero items, \c false
      *      otherwise
      **/
     public static boolean isEmpty(JRDictionary dictionary) {
         return ((dictionary == null) || (dictionary.size() == 0));
     }
 /*@}*/
 }
