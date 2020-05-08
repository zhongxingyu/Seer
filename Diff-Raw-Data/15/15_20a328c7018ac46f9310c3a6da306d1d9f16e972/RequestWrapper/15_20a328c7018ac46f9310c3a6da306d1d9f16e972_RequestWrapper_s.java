 package com.amee.base.resource;
 
 import com.amee.base.domain.Version;
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class RequestWrapper implements Serializable {
 
     private String target = "";
     private Version version;
     private Map<String, String> attributes = new HashMap<String, String>();
     private Map<String, String> matrixParameters = new HashMap<String, String>();
     private Map<String, String> queryParameters = new HashMap<String, String>();
     private Map<String, String> formParameters = new HashMap<String, String>();
     private byte[] body = null;
     private String mediaType = "";
 
     public RequestWrapper() {
         super();
     }
 
     public RequestWrapper(
             String target,
             Version version,
             Map<String, String> attributes) {
         this();
         setTarget(target);
         setVersion(version);
         setAttributes(attributes);
     }
 
     public RequestWrapper(
             String target,
             Version version,
             Map<String, String> attributes,
             Map<String, String> matrixParameters,
             Map<String, String> queryParameters) {
         this(target, version, attributes);
         setMatrixParameters(matrixParameters);
         setQueryParameters(queryParameters);
     }
 
     public RequestWrapper(
             String target,
             Version version,
             Map<String, String> attributes,
             Map<String, String> matrixParameters,
             Map<String, String> queryParameters,
             Map<String, String> formParameters) {
         this(target, version, attributes, matrixParameters, queryParameters);
         setFormParameters(formParameters);
         setMediaType("application/x-www-form-urlencoded");
     }
 
     public RequestWrapper(
             String target,
             Version version,
             Map<String, String> attributes,
             Map<String, String> matrixParameters,
             Map<String, String> queryParameters,
             InputStream body,
             String mediaType) {
         this(target, version, attributes, matrixParameters, queryParameters);
         setBody(body);
         setMediaType(mediaType);
     }
 
     public RequestWrapper(JSONObject obj) {
         super();
         try {
             setTarget(obj.getString("target"));
             setVersion(new Version(obj.getString("version")));
             addToMap(getAttributes(), obj, "attributes");
             addToMap(getMatrixParameters(), obj, "matrixParameters");
             addToMap(getQueryParameters(), obj, "queryParameters");
             addToMap(getFormParameters(), obj, "formParameters");
             setMediaType(obj.getString("mediaType"));
         } catch (JSONException e) {
             throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
         }
     }
 

     public JSONObject toJSONObject() {
         try {
             JSONObject obj = new JSONObject();
             obj.put("target", getTarget());
             obj.put("version", getVersion().toString());
             obj.put("attributes", new JSONObject(getAttributes()));
             obj.put("matrixParameters", new JSONObject(getMatrixParameters()));
             obj.put("queryParameters", new JSONObject(getQueryParameters()));
             obj.put("formParameters", new JSONObject(getFormParameters()));
             obj.put("mediaType", getMediaType());
             return obj;
         } catch (JSONException e) {
             throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
         }
     }
 
     protected void addToMap(Map<String, String> m, JSONObject obj, String name) throws JSONException {
         JSONObject node = obj.getJSONObject(name);
         for (Iterator i = node.keys(); i.hasNext();) {
             String key = (String) i.next();
             if (!node.isNull(key)) {
                 m.put(key, node.getString(key));
             } else {
                 m.put(key, "");
             }
         }
     }
 
     protected void setMapFromMap(Map<String, String> target, Map<String, String> source) {
         target.clear();
         if (source != null) {
             for (String key : source.keySet()) {
                 target.put(key, source.get(key) != null ? source.get(key) : "");
             }
         }
     }
 
     public Version getVersion() {
         return version;
     }
 
     public void setVersion(Version version) {
         this.version = version;
     }
 
     public Map<String, String> getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Map<String, String> attributes) {
         setMapFromMap(this.attributes, attributes);
     }
 
     public Map<String, String> getMatrixParameters() {
         return matrixParameters;
     }
 
     public void setMatrixParameters(Map<String, String> matrixParameters) {
         setMapFromMap(this.matrixParameters, matrixParameters);
     }
 
     public Map<String, String> getQueryParameters() {
         return queryParameters;
     }
 
     public void setQueryParameters(Map<String, String> queryParameters) {
         setMapFromMap(this.queryParameters, queryParameters);
     }
 
     public Map<String, String> getFormParameters() {
         return formParameters;
     }
 
     public void setFormParameters(Map<String, String> formParameters) {
         setMapFromMap(this.formParameters, formParameters);
     }
 
     public boolean hasBody() {
         return body != null;
     }
 
     public byte[] getBody() {
         return body;
     }
 
     public String getBodyAsString() {
         try {
             return new String(body, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("Caught UnsupportedEncodingException: " + e.getMessage(), e);
         }
     }
 
     public void setBody(InputStream stream) {
         try {
             body = IOUtils.toByteArray(stream);
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     public String getMediaType() {
         return mediaType;
     }
 
     public void setMediaType(String mediaType) {
         if (mediaType != null) {
             this.mediaType = mediaType;
         }
     }
 
     public String getTarget() {
         return target;
     }
 
     public void setTarget(String target) {
         if (target != null) {
             this.target = target;
         }
     }
 }
