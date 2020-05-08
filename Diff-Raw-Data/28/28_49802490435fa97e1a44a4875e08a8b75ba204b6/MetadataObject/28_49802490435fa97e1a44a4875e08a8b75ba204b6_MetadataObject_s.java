 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.gooddata.integration.rest;
 
 import com.gooddata.exception.GdcRestApiException;
 import net.sf.json.JSON;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JsonConfig;
 import org.apache.log4j.Logger;
 
 import java.io.Writer;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Represents GoodData metadata object
  * 
  * @author Zdenek Svoboda <zd@gooddata.com>
  * @version 1.0
  */
 public class MetadataObject implements JSON, Map, Comparable {
 
     private static Logger l = Logger.getLogger(MetadataObject.class);
 
     private JSONObject jsonObject = new JSONObject();
 
     public MetadataObject(JSONObject o) {
         jsonObject = o;
     }
 
     public MetadataObject() {
         jsonObject = new JSONObject();
     }
 
     /**
      * Extracts the dependent objects uris from the content
      * @return list of depenedent object uris
      */
     public List<String> getDependentObjectUris() {
         List<String> uris = new ArrayList<String>();
         String uri = getUri();
         String content = toString();
        Pattern p = Pattern.compile("\\\"/gdc/md/[^/]*?/obj/[0-9]+?\\\"");
         Matcher m = p.matcher(content);
         while(m.find()) {
             String u = m.group();
            u = u.replace("\"","");
             if(!u.equalsIgnoreCase(uri) && !uris.contains(u))
                 uris.add(u);
         }
         return uris;
     }
 
     /**
      * Extracts the dependent objects IDs from the content
      * @return list of depenedent object IDs
      */
      public List<String> getDependentObjectIds() {
         List<String> uris = getDependentObjectUris();
         List<String> ids = new ArrayList<String>();
         for(String uri : uris) {
             int i = uri.lastIndexOf("/");
             String id = uri.substring(i+1);
             ids.add(id);
         }
         return ids;
     }
 
     /**
      * Strips all keys that should not be sent to the create method
      */
     public void stripKeysForCreate() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             m.discard("uri");
 
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     /**
      * Strips all keys that should not be sent to the create method
      */
     public void stripKeysForRead() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             m.discard("author");
             m.discard("contributor");
             m.discard("created");
             m.discard("updated");
 
             JSONObject c = r.getJSONObject("content");
             if (c == null || c.isNullObject() || c.isEmpty()) {
                 l.debug("The JSON object doesn't contain the content tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the content tag object=" + jsonObject.toString());
             }
             if(rootKey.equalsIgnoreCase("report")) {
                 c.discard("results");
             }
             if(rootKey.equalsIgnoreCase("folder") || rootKey.equalsIgnoreCase("domain")) {
                 c.discard("entries");
             }
 
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     public JSONObject getContent() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject c = r.getJSONObject("content");
             if (c == null || c.isNullObject() || c.isEmpty()) {
                 l.debug("The JSON object doesn't contain the content tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the content tag object=" + jsonObject.toString());
             }
             return c;
 
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     public JSONObject getMeta() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             return m;
 
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     /**
      * Extracts the object's identifier
      * @return the GoodData object identifier
      */
     public String getIdentifier() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             return m.getString("identifier");
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     /**
      * Extracts the object's uri
      * @return the GoodData object uri
      */
     public String getUri() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             return m.getString("uri");
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     /**
      * Extracts the object's type
      * @return the GoodData object type
      */
     public String getType() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             return m.getString("category");
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     /**
      * Extracts the object's id
      *
      * @return the GoodData object id
      */
     public String getId() {
         Iterator keys = jsonObject.keys();
         if (keys.hasNext()) {
             String rootKey = (String) keys.next();
             JSONObject r = jsonObject.getJSONObject(rootKey);
             if (r == null || r.isNullObject() || r.isEmpty()) {
                 l.debug("The JSON object doesn't contain the root object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the root object=" + jsonObject.toString());
             }
             JSONObject m = r.getJSONObject("meta");
             if (m == null || m.isNullObject() || m.isEmpty()) {
                 l.debug("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
                 throw new GdcRestApiException("The JSON object doesn't contain the meta tag object=" + jsonObject.toString());
             }
             return m.getString("id");
         } else {
             l.debug("The JSON object doesn't contain any root object=" + jsonObject.toString());
             throw new GdcRestApiException("The JSON object doesn't contain any root object=" + jsonObject.toString());
         }
     }
 
     public JSONObject accumulate(String key, boolean value) {
         return jsonObject.accumulate(key, value);
     }
 
     public JSONObject accumulate(String key, double value) {
         return jsonObject.accumulate(key, value);
     }
 
     public JSONObject accumulate(String key, int value) {
         return jsonObject.accumulate(key, value);
     }
 
     public JSONObject accumulate(String key, long value) {
         return jsonObject.accumulate(key, value);
     }
 
     public JSONObject accumulate(String key, Object value) {
         return jsonObject.accumulate(key, value);
     }
 
     public JSONObject accumulate(String key, Object value, JsonConfig jsonConfig) {
         return jsonObject.accumulate(key, value, jsonConfig);
     }
 
     public void accumulateAll(Map map) {
         jsonObject.accumulateAll(map);
     }
 
     public void accumulateAll(Map map, JsonConfig jsonConfig) {
         jsonObject.accumulateAll(map, jsonConfig);
     }
 
     public void clear() {
         jsonObject.clear();
     }
 
     public int compareTo(Object obj) {
         return jsonObject.compareTo(obj);
     }
 
     public boolean containsKey(Object key) {
         return jsonObject.containsKey(key);
     }
 
     public boolean containsValue(Object value) {
         return jsonObject.containsValue(value);
     }
 
     public boolean containsValue(Object value, JsonConfig jsonConfig) {
         return jsonObject.containsValue(value, jsonConfig);
     }
 
     public JSONObject discard(String key) {
         return jsonObject.discard(key);
     }
 
     public JSONObject element(String key, boolean value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, Collection value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, Collection value, JsonConfig jsonConfig) {
         return jsonObject.element(key, value, jsonConfig);
     }
 
     public JSONObject element(String key, double value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, int value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, long value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, Map value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, Map value, JsonConfig jsonConfig) {
         return jsonObject.element(key, value, jsonConfig);
     }
 
     public JSONObject element(String key, Object value) {
         return jsonObject.element(key, value);
     }
 
     public JSONObject element(String key, Object value, JsonConfig jsonConfig) {
         return jsonObject.element(key, value, jsonConfig);
     }
 
     public JSONObject elementOpt(String key, Object value) {
         return jsonObject.elementOpt(key, value);
     }
 
     public JSONObject elementOpt(String key, Object value, JsonConfig jsonConfig) {
         return jsonObject.elementOpt(key, value, jsonConfig);
     }
 
     public Set entrySet() {
         return jsonObject.entrySet();
     }
 
     public boolean equals(Object obj) {
         return jsonObject.equals(obj);
     }
 
     public Object get(Object key) {
         return jsonObject.get(key);
     }
 
     public Object get(String key) {
         return jsonObject.get(key);
     }
 
     public boolean getBoolean(String key) {
         return jsonObject.getBoolean(key);
     }
 
     public double getDouble(String key) {
         return jsonObject.getDouble(key);
     }
 
     public int getInt(String key) {
         return jsonObject.getInt(key);
     }
 
     public JSONArray getJSONArray(String key) {
         return jsonObject.getJSONArray(key);
     }
 
     public JSONObject getJSONObject(String key) {
         return jsonObject.getJSONObject(key);
     }
 
     public long getLong(String key) {
         return jsonObject.getLong(key);
     }
 
     public String getString(String key) {
         return jsonObject.getString(key);
     }
 
     public boolean has(String key) {
         return jsonObject.has(key);
     }
 
     public int hashCode() {
         return jsonObject.hashCode();
     }
 
     public boolean isArray() {
         return jsonObject.isArray();
     }
 
     public boolean isEmpty() {
         return jsonObject.isEmpty();
     }
 
     public boolean isNullObject() {
         return jsonObject.isNullObject();
     }
 
     public Iterator keys() {
         return jsonObject.keys();
     }
 
     public Set keySet() {
         return jsonObject.keySet();
     }
 
     public JSONArray names() {
         return jsonObject.names();
     }
 
     public JSONArray names(JsonConfig jsonConfig) {
         return jsonObject.names(jsonConfig);
     }
 
     public Object opt(String key) {
         return jsonObject.opt(key);
     }
 
     public boolean optBoolean(String key) {
         return jsonObject.optBoolean(key);
     }
 
     public boolean optBoolean(String key, boolean defaultValue) {
         return jsonObject.optBoolean(key, defaultValue);
     }
 
     public double optDouble(String key) {
         return jsonObject.optDouble(key);
     }
 
     public double optDouble(String key, double defaultValue) {
         return jsonObject.optDouble(key, defaultValue);
     }
 
     public int optInt(String key) {
         return jsonObject.optInt(key);
     }
 
     public int optInt(String key, int defaultValue) {
         return jsonObject.optInt(key, defaultValue);
     }
 
     public JSONArray optJSONArray(String key) {
         return jsonObject.optJSONArray(key);
     }
 
     public JSONObject optJSONObject(String key) {
         return jsonObject.optJSONObject(key);
     }
 
     public long optLong(String key) {
         return jsonObject.optLong(key);
     }
 
     public long optLong(String key, long defaultValue) {
         return jsonObject.optLong(key, defaultValue);
     }
 
     public String optString(String key) {
         return jsonObject.optString(key);
     }
 
     public String optString(String key, String defaultValue) {
         return jsonObject.optString(key, defaultValue);
     }
 
     public Object put(Object key, Object value) {
         return jsonObject.put(key, value);
     }
 
     public void putAll(Map map) {
         jsonObject.putAll(map);
     }
 
     public void putAll(Map map, JsonConfig jsonConfig) {
         jsonObject.putAll(map, jsonConfig);
     }
 
     public Object remove(Object key) {
         return jsonObject.remove(key);
     }
 
     public Object remove(String key) {
         return jsonObject.remove(key);
     }
 
     public int size() {
         return jsonObject.size();
     }
 
     public JSONArray toJSONArray(JSONArray names) {
         return jsonObject.toJSONArray(names);
     }
 
     public String toString() {
         return jsonObject.toString();
     }
 
     public String toString(int indentFactor) {
         return jsonObject.toString(indentFactor);
     }
 
     public String toString(int indentFactor, int indent) {
         return jsonObject.toString(indentFactor, indent);
     }
 
     public Collection values() {
         return jsonObject.values();
     }
 
     public Writer write(Writer writer) {
         return jsonObject.write(writer);
     }
 
 }
