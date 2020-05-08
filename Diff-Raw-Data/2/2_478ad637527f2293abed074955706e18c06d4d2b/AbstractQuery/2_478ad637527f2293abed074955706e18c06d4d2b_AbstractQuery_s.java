 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.webdetails.cdb.query;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import pt.webdetails.cpf.persistence.PersistenceEngine;
 
 /**
  *
  * @author pdpi
  */
 public abstract class AbstractQuery implements Query {
 
   private String id, group, name, key;
   private Map<String, Object> properties;
   protected static final Log logger = LogFactory.getLog(AbstractQuery.class);
 
   protected AbstractQuery() {
     properties = new HashMap<String, Object>();
   }
 
   public String getId() {
     return id;
   }
 
   protected void setId(String id) {
     this.id = id;
   }
 
   @Override
   public String getGroup() {
     return group;
   }
 
   protected void setGroup(String group) {
     this.group = group;
   }
 
   @Override
   public String getName() {
     return name;
   }
 
   protected void setName(String name) {
     this.name = name;
   }
 
   @Override
   public String getKey() {
     return key;
   }
 
   protected void setKey(String key) {
     this.key = key;
   }
 
   @Override
   public Object getProperty(String prop) {
     return properties.get(prop);
   }
 
   @Override
   public JSONObject toJSON() {
     try {
       JSONObject json = new JSONObject();
       json.put("id", id);
       json.put("key", key);
       json.put("group", group);
       json.put("name", name);
       json.put("definition", properties);
       return json;
     } catch (JSONException jse) {
       return null;
     }
   }
 
   @Override
   public void fromJSON(JSONObject json) {
     String _id, _key, _group, _name;
     HashMap<String, Object> _properties = new HashMap<String, Object>();
     try {
       /* Load everything into temporary variables */
       _id = json.getString("guid");
       _key = json.getString("@rid");
       _group = json.getString("group");
       _name = json.getString("name");
      JSONObject props = new JSONObject(json.get("definition"));
       for (String key : JSONObject.getNames(props)) {
         _properties.put(key, props.get(key));
       }
       /* Seems like we managed to safely load everything, so it's
        * now safe to copy all the values over to the object
        */
       id = _id;
       key = _key;
       group = _group;
       name = _name;
       properties = _properties;
     } catch (JSONException jse) {
     }
   }
 
   @Override
   public void store() {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   @Override
   public void reload() {
     load(getKey());
   }
 
   @Override
   public void load(String key) {
     PersistenceEngine eng = PersistenceEngine.getInstance();
     try {
 
       Map<String, Object> params = new HashMap<String, Object>();
       params.put("id", id);
       JSONObject response = eng.query("select * from Query where @rid = :id", params);
       JSONObject query = (JSONObject) ((JSONArray) response.get("object")).get(0);
       fromJSON(query);
     } catch (Exception e) {
       logger.error(e);
     }
   }
 
 }
