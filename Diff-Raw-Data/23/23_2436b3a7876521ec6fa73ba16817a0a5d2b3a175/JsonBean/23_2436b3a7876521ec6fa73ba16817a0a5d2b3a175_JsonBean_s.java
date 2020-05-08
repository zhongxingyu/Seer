 /*
  * 
  */
 package org.smartly.packages.http.impl.serialization.json;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.smartly.commons.util.StringUtils;
 import org.smartly.packages.http.impl.serialization.json.serializer.BeanSerializer;
 import org.smartly.packages.http.impl.serialization.json.utils.JsonBeanUtils;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Simple serializer/deserializer from java bean to json string and from
  * json string to JSONObject (or Exception or List of JSONObject)
  *
  * @author angelo.geminiani
  */
 public final class JsonBean {
 
     private static final String TAG_VALUE = JsonBeanUtils.TAG_VALUE;
     private static final String TAG_CLASS = JsonBeanUtils.TAG_CLASS;
     private static final String TAG_TYPE = JsonBeanUtils.TAG_TYPE;
     private static final String TAG_MESSAGE = JsonBeanUtils.TAG_MESSAGE;
     private static final String TAG_CAUSE = JsonBeanUtils.TAG_CAUSE;
     private static final String OBJECT = JsonBeanUtils.TYPE_OBJECT;
     private static final String ERROR = JsonBeanUtils.TYPE_ERROR;
     // internal object
     private JSONObject _jsonobject;
 
     public JsonBean(final Object object) {
         _jsonobject = BeanSerializer.getInstance().serialize(object);
     }
 
     public JsonBean(final String jsontext) {
         try {
             _jsonobject = StringUtils.hasText(jsontext)
                     ? new JSONObject(jsontext)
                     : new JSONObject();
         } catch (Throwable t) {
             _jsonobject = new JSONObject();
             JsonBeanUtils.addError(_jsonobject, t);
         }
     }
 
     @Override
     public String toString() {
         return _jsonobject.toString();
     }
 
     public String asString() {
         return this.getTagValue();
     }
 
     /**
      * Deserialize JSON string. Return:<br/>
      * <ul>
      * <li>Exception</li><br/>
      * <li>JSONObject</li><br/>
      * <li>List of JSONObject</li><br/>
      * </ul>
      *
      * @return
      */
     public Object asObject() {
         try {
             if (null != _jsonobject) {
                 if (this.isError()) {
                     return this.asException();
                 } else {
                     if (_jsonobject.has(TAG_VALUE)) {
                         final String object = this.getTagValue();
                         if (object.startsWith("{")) {
                             // object
                             return new JSONObject(object);
                         } else if (object.startsWith("[")) {
                            final LinkedList<JSONObject> result = new LinkedList<JSONObject>();
                             final JSONArray array = new JSONArray(object);
                             final int len = array.length();
                             for (int i = 0; i < len; i++) {
                                result.add(array.optJSONObject(i));
                             }
                             return result;
                         } else {
                             // native type
                             return object;
                         }
                     } else {
                         // does not contains tag object ( is native JSON )
                         return _jsonobject;
                     }
                 }
             }
         } catch (Throwable t) {
             return t;
         }
         return null;
     }
 
     /**
      * Returns JSONObjects List or JSONObject
      * @return JSONObjects List or JSONObject
      */
     public Object asJSONObject() {
         final Object object = this.asObject();
         if (object instanceof JSONObject) {
             return (JSONObject) object;
         } else if (object instanceof List) {
            return (List<JSONObject>) object;
         } else if (object instanceof Exception) {
             try {
                 final JSONObject json = new JSONObject();
                 json.putOnce("error", object.toString());
                 return json;
             } catch (Throwable ignored) {
             }
         }
         return null;
     }
 
     public Exception asException() {
         if (isError()) {
             final String cause = _jsonobject.optString(TAG_CAUSE, null);
             final Exception result = StringUtils.hasText(cause)
                     ? new Exception(this.getTagValue(), new Exception(cause))
                     : new Exception(this.getTagValue());
             return result;
         } else {
             return null;
         }
     }
 
     public int asInteger() {
         if (JsonBeanUtils.isNative(this.getTagClass())) {
             return Integer.parseInt(this.getTagValue());
         }
         return -1;
     }
 
     public double asDouble() {
         if (JsonBeanUtils.isNative(this.getTagClass())) {
             return Double.parseDouble(this.getTagValue());
         }
         return -1.0;
     }
 
     public long asLong() {
         if (JsonBeanUtils.isNative(this.getTagClass())) {
             return Long.parseLong(this.getTagValue());
         }
         return -1L;
     }
 
     public byte asByte() {
         if (JsonBeanUtils.isNative(this.getTagClass())) {
             return Byte.parseByte(this.getTagValue());
         }
         return 0;
     }
 
     public char asChar() {
         if (JsonBeanUtils.isNative(this.getTagClass())) {
             return this.getTagValue().charAt(0);
         }
         return 0;
     }
 
     public boolean asBoolean() {
         if (JsonBeanUtils.isNative(this.getTagClass())) {
             return Boolean.parseBoolean(this.getTagValue());
         }
         return false;
     }
 
     public boolean isError() {
         if (null != _jsonobject) {
             final String classType = _jsonobject.optString(TAG_TYPE, null);
             if (null != classType) {
                 return classType.equals(ERROR);
             }
         }
         return false;
     }
 
     public boolean isObject() {
         if (null != _jsonobject) {
             final String classType = _jsonobject.optString(TAG_TYPE, null);
             if (null != classType) {
                 return classType.equals(OBJECT);
             }
         }
         return false;
     }
 
     public boolean isNull() {
         final String object = this.getTagValue();
         return this.isObject() && !StringUtils.hasText(object);
     }
 
     public String getTagValue() {
         if (null != _jsonobject) {
             return _jsonobject.optString(TAG_VALUE, "");
         }
         return "";
     }
 
     public String getTagClass() {
         if (null != _jsonobject) {
             return _jsonobject.optString(TAG_CLASS, "");
         }
         return "";
     }
 
     public String getTagMessage() {
         if (null != _jsonobject) {
             return _jsonobject.optString(TAG_MESSAGE, "");
         }
         return "";
     }
 
     public String getTagCause() {
         if (null != _jsonobject) {
             return _jsonobject.optString(TAG_CAUSE, "");
         }
         return "";
     }
 
     public String getTagType() {
         if (null != _jsonobject) {
             return _jsonobject.optString(TAG_TYPE, "");
         }
         return "";
     }
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
 }
