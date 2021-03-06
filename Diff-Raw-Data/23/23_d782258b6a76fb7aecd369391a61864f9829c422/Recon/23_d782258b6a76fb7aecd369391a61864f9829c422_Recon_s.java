 package com.metaweb.gridworks.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONWriter;
 
 import com.metaweb.gridworks.Jsonizable;
 import com.metaweb.gridworks.expr.HasFields;
  
 public class Recon implements HasFields, Jsonizable {
     
     static public enum Judgment {
         None,
         Matched,
         New
     }
     
     static public String judgmentToString(Judgment judgment) {
         if (judgment == Judgment.Matched) {
             return "matched";
         } else if (judgment == Judgment.New) {
             return "new";
         } else {
             return "none";
         }
     }
     
     static public Judgment stringToJudgment(String s) {
         if ("matched".equals(s)) {
             return Judgment.Matched;
         } else if ("new".equals(s)) {
             return Judgment.New;
         } else {
             return Judgment.None;
         }
     }
     
     static final public int Feature_typeMatch = 0;
     static final public int Feature_nameMatch = 1;
     static final public int Feature_nameLevenshtein = 2;
     static final public int Feature_nameWordDistance = 3;
     static final public int Feature_max = 4;
 
     static final protected Map<String, Integer> s_featureMap = new HashMap<String, Integer>();
     static {
         s_featureMap.put("typeMatch", Feature_typeMatch);
         s_featureMap.put("nameMatch", Feature_nameMatch);
         s_featureMap.put("nameLevenshtein", Feature_nameLevenshtein);
         s_featureMap.put("nameWordDistance", Feature_nameWordDistance);
     }
     
     final public long            id;
     public Object[]              features = new Object[Feature_max];
     public List<ReconCandidate>  candidates;
     public Judgment              judgment = Judgment.None;
     public ReconCandidate        match = null;
     
     public Recon() {
         id = System.currentTimeMillis() * 1000000 + Math.round(Math.random() * 1000000);
     }
     
     protected Recon(long id) {
         this.id = id;
     }
     
     public Recon dup() {
         Recon r = new Recon();
         
         System.arraycopy(features, 0, r.features, 0, features.length);
         
         if (candidates != null) {
             r.candidates = new ArrayList<ReconCandidate>(candidates);
         }
         
         r.judgment = judgment;
         r.match = match;
         
         return r;
     }
     
     public void addCandidate(ReconCandidate candidate) {
         if (candidates == null) {
             candidates = new ArrayList<ReconCandidate>(3);
         }
         candidates.add(candidate);
     }
     
     public ReconCandidate getBestCandidate() {
         if (candidates != null && candidates.size() > 0) {
             return candidates.get(0);
         }
         return null;
     }
     
     public Object getFeature(int feature) {
         return feature < features.length ? features[feature] : null;
     }
     
     public void setFeature(int feature, Object v) {
         if (feature >= features.length) {
             if (feature >= Feature_max) {
                 return;
             }
             
             // We deserialized this object from an older version of the class
             // that had fewer features, so we can just try to extend it
             
             Object[] newFeatures = new Object[Feature_max];
             
             System.arraycopy(features, 0, newFeatures, 0, features.length);
             
             features = newFeatures;
         }
         
         features[feature] = v;
     }
     
     public Object getField(String name, Properties bindings) {
         if ("best".equals(name)) {
             return candidates != null && candidates.size() > 0 ? candidates.get(0) : null;
         } else if ("judgment".equals(name) || "judgement".equals(name)) {
             return judgmentToString();
         } else if ("matched".equals(name)) {
             return judgment == Judgment.Matched;
         } else if ("new".equals(name)) {
             return judgment == Judgment.New;
         } else if ("match".equals(name)) {
             return match;
         } else if ("features".equals(name)) {
             return new Features();
         }
         return null;
     }
     
     public boolean fieldAlsoHasFields(String name) {
         return "match".equals(name) || "best".equals(name);
     }
     
     protected String judgmentToString() {
         return judgmentToString(judgment);
     }
     
     public class Features implements HasFields {
         public Object getField(String name, Properties bindings) {
             int index = s_featureMap.get(name);
             return index < features.length ? features[index] : null;
         }
 
         public boolean fieldAlsoHasFields(String name) {
             return false;
         }
     }
 
     public void write(JSONWriter writer, Properties options)
             throws JSONException {
         
     	boolean saveMode = "save".equals(options.getProperty("mode"));
     	
         writer.object();
         writer.key("id"); writer.value(id);
         writer.key("j"); writer.value(judgmentToString());
         
         if (match != null) {
             writer.key("m");
             match.write(writer, options);
         }
         if (match == null || saveMode) {
             writer.key("c"); writer.array();
             if (candidates != null) {
                 for (ReconCandidate c : candidates) {
                     c.write(writer, options);
                 }
             }
             writer.endArray();
         }
         
         if (saveMode) {
             writer.key("f");
                 writer.array();
                 for (Object o : features) {
                     writer.value(o);
                 }
                 writer.endArray();
         }
         
         writer.endObject();
     }
     
     static public Recon load(JSONObject obj, Map<Long, Recon> reconCache) throws Exception {
         if (obj == null) {
             return null;
         }
         
         long id = obj.getLong("id");
         if (reconCache.containsKey(id)) {
             return reconCache.get(id);
         }
         
         Recon recon = new Recon(id);
         
         if (obj.has("j")) {
             recon.judgment = stringToJudgment(obj.getString("j"));
         }
         if (obj.has("m")) {
             recon.match = ReconCandidate.load(obj.getJSONObject("m"));
         }
         if (obj.has("c")) {
             JSONArray a = obj.getJSONArray("c");
             int count = a.length();
             
             for (int i = 0; i < count; i++) {
                 recon.addCandidate(ReconCandidate.load(a.getJSONObject(i)));
             }
         }
         if (obj.has("f")) {
             JSONArray a = obj.getJSONArray("f");
             int count = a.length();
             
             for (int i = 0; i < count && i < Feature_max; i++) {
                 if (!a.isNull(i)) {
                     recon.features[i] = a.get(i);
                 }
             }
         }
         
         reconCache.put(id, recon);
         
         return recon;
     }
 
     static public Recon loadStreaming(JsonParser jp, Map<Long, Recon> reconCache) throws Exception {
         JsonToken t = jp.getCurrentToken();
         if (t == JsonToken.VALUE_NULL || t != JsonToken.START_OBJECT) {
             return null;
         }
         
         Recon recon = null;
         boolean old = true;
         
         while (jp.nextToken() != JsonToken.END_OBJECT) {
             String fieldName = jp.getCurrentName();
             jp.nextToken();
             
             if ("id".equals(fieldName)) {
                 long id = jp.getLongValue();
                 if (reconCache.containsKey(id)) {
                     recon = reconCache.get(id);
                 } else {
                     recon = new Recon(id);
                     old = false;
                 }
             } else if ("j".equals(fieldName)) {
                 recon.judgment = stringToJudgment(jp.getText());
             } else if ("m".equals(fieldName)) {
                 if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                     ReconCandidate match = ReconCandidate.loadStreaming(jp, reconCache);
                     if (!old) {
                         recon.match = match;
                     }
                 }
             } else if ("f".equals(fieldName)) {
                 if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
                     return null;
                 }
                 
                 int feature = 0;
                 while (jp.nextToken() != JsonToken.END_ARRAY) {
                     if (feature < recon.features.length && !old) {
                         JsonToken token = jp.getCurrentToken();
                         if (token == JsonToken.VALUE_STRING) {
                             recon.features[feature++] = jp.getText();
                         } else if (token == JsonToken.VALUE_NUMBER_INT) {
                            recon.features[feature++] = jp.getIntValue();
                         } else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                            recon.features[feature++] = jp.getFloatValue();
                         } else if (token == JsonToken.VALUE_FALSE) {
                             recon.features[feature++] = false;
                         } else if (token == JsonToken.VALUE_TRUE) {
                             recon.features[feature++] = true;
                         }
                     }
                 }
             } else if ("c".equals(fieldName)) {
                 if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
                     return null;
                 }
                 
                 while (jp.nextToken() != JsonToken.END_ARRAY) {
                     ReconCandidate rc = ReconCandidate.loadStreaming(jp, reconCache);
                     if (rc != null && !old) {
                         recon.addCandidate(rc);
                     }
                 }
             }
         }
         
         return recon;
     }
 }
