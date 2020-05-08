 package org.iplantc.core.tito.client.models;
 
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.metadata.client.property.DataObject;
 import org.iplantc.core.metadata.client.property.Property;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroup;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroupContainer;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 
 /**
  * 
  * A model that represents a Template that is defined by tool authors for a particular tool
  * 
  * @author sriram
  *
  */
 public class Template {
 
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
     public static final String ID = "id"; //$NON-NLS-1$
     public static final String NAME = "name"; //$NON-NLS-1$
     public static final String DESCRIPTION = "description"; //$NON-NLS-1$
     public static final String COMPONENT = "component"; //$NON-NLS-1$
     public static final String COMPONENT_ID = "component_id"; //$NON-NLS-1$
     public static final String TYPE = "TYPE"; //$NON-NLS-1$
     public static final String STATUS = "status"; //$NON-NLS-1$
     public static final String TITO_ID = "tito"; //$NON-NLS-1$
     public static final String LAST_EDITED_DATE = "edited_date"; //$NON-NLS-1$
     public static final String PUBLISHED_DATE = "published_date"; //$NON-NLS-1$
    public static final String REFERENCES = "refs"; //$NON-NLS-1$
     public static final String GROUPS = "groups"; //$NON-NLS-1$
 
     private String id;
     private String name;
     private String desc;
     private String compId;
     private String comp;
     private String compType;
     private String status;
     private String titoId;
     private String dateEdited;
     private String datePublished;
     private List<String> references;
 
     private PropertyGroupContainer container;
 
     /**
      * create a new instance of Template
      * 
      */
     public Template() {
         this("", "", "", "", "", "", null, "", "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
     }
 
     /**
      * create a new instance of Template
      * 
      * @param id
      * @param name
      * @param description
      * @param compid
      * @param comp
      * @param compType
      * @param references
      * @param status
      * @param tito
      * @param dataObjects
      * @param container
      */
     public Template(String id, String name, String description, String compid, String comp,
             String compType, List<String> references, String status, String tito,
             PropertyGroupContainer container) {
         setId(id);
         setName(name);
         setDescription(description);
         setCompId(compid);
         setComp(comp);
         setCompType(compType);
         setReferences(references);
         setStatus(status);
         setTitoId(tito);
         setContainer(container);
         setDateEdited(""); //$NON-NLS-1$
         setDatePublished(""); //$NON-NLS-1$
     }
 
     /**
      * create a new instance of the template
      * 
      * @param json
      */
     public Template(JSONObject json) {
         setId(JsonUtil.getString(json, ID));
         setName(JsonUtil.getString(json, NAME));
 
         setDescription(JsonUtil.getString(json, DESCRIPTION));
         if (getDescription().isEmpty()) {
             setDescription(JsonUtil.getString(json, "desc")); //$NON-NLS-1$
         }
 
         setCompId(JsonUtil.getString(json, COMPONENT_ID));
         setComp(JsonUtil.getString(json, COMPONENT));
         setCompType(JsonUtil.getString(json, TYPE));
         setStatus(JsonUtil.getString(json, STATUS));
         setTitoId(JsonUtil.getString(json, TITO_ID));
         setDateEdited(JsonUtil.getString(json, LAST_EDITED_DATE));
         setDatePublished(JsonUtil.getString(json, PUBLISHED_DATE));
        setReferences(JsonUtil.buildStringList(JsonUtil.getArray(json, REFERENCES)));
 
         setContainer(parseContainer(json));
 
         // check for old style DataObjects
         parseDataObjects(json);
     }
 
     private PropertyGroupContainer parseContainer(JSONObject json) {
         JSONObject groups = JsonUtil.getObject(json, GROUPS);
         if (groups != null) {
             return new PropertyGroupContainer(groups);
         }
 
         return null;
     }
 
     private void parseDataObjects(JSONObject json) {
         JSONArray input = JsonUtil.getArray(json, "input"); //$NON-NLS-1$
         JSONArray output = JsonUtil.getArray(json, "output"); //$NON-NLS-1$
         if (input == null && output == null) {
             // there are no old data objects in this JSON
             return;
         }
 
         PropertyGroup dataObjectGroup = new PropertyGroup();
         dataObjectGroup.setLabel(I18N.DISPLAY.inputOutput());
 
         convertDataObjects(dataObjectGroup, DataObject.INPUT_TYPE, input);
         convertDataObjects(dataObjectGroup, DataObject.OUTPUT_TYPE, output);
 
         if (dataObjectGroup.getProperties().size() > 0) {
             container.add(dataObjectGroup);
         }
     }
 
     private void convertDataObjects(PropertyGroup group, String dataType, JSONArray jsonDataObjects) {
         if (jsonDataObjects == null) {
             return;
         }
 
         for (int i = 0; i < jsonDataObjects.size(); i++) {
             JSONObject obj = jsonDataObjects.get(i).isObject();
             if (obj != null) {
                 DataObject dataObject = new DataObject(obj);
                 dataObject.setType(dataType);
 
                 Property dataProperty = new Property();
                 dataProperty.setType(dataType);
                 dataProperty.setName(dataObject.getCmdSwitch());
                 dataProperty.setLabel(dataObject.getName());
                 dataProperty.setDescription(dataObject.getDescription());
                 dataProperty.setOrder(dataObject.getOrder());
                 dataProperty.setVisible(dataObject.isVisible());
 
                 dataProperty.setDataObject(dataObject);
 
                 group.add(dataProperty);
             }
         }
     }
 
     /**
      * get the Template in json format
      * 
      * @return a json object representation of this Template
      */
     public JSONObject toJson() {
         JSONObject json = new JSONObject();
 
         json.put(ID, new JSONString(getId()));
 
         json.put(NAME, new JSONString(getName()));
         json.put(DESCRIPTION, new JSONString(getDescription()));
 
         json.put(COMPONENT_ID, new JSONString(getCompId()));
         json.put(COMPONENT, new JSONString(getComp()));
 
         json.put(TYPE, new JSONString(getCompType()));
         
         json.put(REFERENCES, JsonUtil.buildArrayFromStrings(references));
 
         json.put(TITO_ID, new JSONString(getTitoId()));
 
         json.put(LAST_EDITED_DATE, new JSONString(getDateEdited()));
         json.put(PUBLISHED_DATE, new JSONString(getDatePublished()));
 
         if (getContainer() != null) {
             json.put(GROUPS, getContainer().toJson());
         }
 
         return json;
     }
 
     /**
      * Like {@link #toJson()}, but includes user information.
      * 
      * @return a JSON object
      */
     public JSONObject toJsonExtended() {
         UserInfo user = UserInfo.getInstance();
         if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
             JSONObject json = toJson();
             json.put("user", new JSONString(user.getUsername())); //$NON-NLS-1$
             json.put("implementation", buildImplementationDetails(user)); //$NON-NLS-1$
             json.put("full_username", new JSONString(user.getFullUsername())); //$NON-NLS-1$
             return json;
         } else {
             return null;
         }
     }
 
     private JSONObject buildImplementationDetails(UserInfo user) {
         JSONObject obj = new JSONObject();
         obj.put("implementor_email", new JSONString(user.getEmail())); //$NON-NLS-1$
         obj.put("implementor", new JSONString(user.getUsername())); //$NON-NLS-1$
         JSONObject params = new JSONObject();
         params.put("params", new JSONArray()); //$NON-NLS-1$
         obj.put("test", params); //$NON-NLS-1$
         return obj;
     }
 
     /**
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return the description
      */
     public String getDescription() {
         return desc;
     }
 
     /**
      * @return the compid
      */
     public String getCompId() {
         return compId;
     }
 
     /**
      * @return the comp
      */
     public String getComp() {
         return comp;
     }
 
     /**
      * @return the compType
      */
     public String getCompType() {
         return compType;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @param desc the name to set
      */
     public void setDescription(String desc) {
         this.desc = desc;
     }
 
     /**
      * @param compid the compid to set
      */
     public void setCompId(String compid) {
         this.compId = compid;
     }
 
     /**
      * @param comp the comp to set
      */
     public void setComp(String comp) {
         this.comp = comp;
     }
 
     /**
      * @param compType the compType to set
      */
     public void setCompType(String compType) {
         this.compType = compType;
     }
 
     /**
      * @param container the container to set
      */
     public void setContainer(PropertyGroupContainer container) {
         this.container = container;
     }
 
     /**
      * @return the container
      */
     public PropertyGroupContainer getContainer() {
         return container;
     }
 
     /**
      * @param status the status to set
      */
     public void setStatus(String status) {
         this.status = status;
     }
 
     /**
      * @return the status
      */
     public String getStatus() {
         return status;
     }
 
     /**
      * @param titoId the titoId to set
      */
     public void setTitoId(String titoId) {
         this.titoId = titoId;
     }
 
     /**
      * @return the titoId
      */
     public String getTitoId() {
         return titoId;
     }
 
     /**
      * @param dateEdited the last edit date as a timestamp to set
      */
     public void setDateEdited(String dateEdited) {
         this.dateEdited = dateEdited;
     }
 
     /**
      * @return the last edit date as a timestamp
      */
     public String getDateEdited() {
         return dateEdited;
     }
 
     /**
      * @param datePublished the published date as a timestamp to set
      */
     public void setDatePublished(String datePublished) {
         this.datePublished = datePublished;
     }
 
     /**
      * @return the published date as a timestamp
      */
     public String getDatePublished() {
         return datePublished;
     }
     
     /**
      * Sets a list of urls, citations, etc. to save with the template
      * @param references
      */
     public void setReferences(List<String> references) {
         this.references = references;
     }
     
     /**
      * Returns urls, citations, etc. for the template, if there are any.
      * @return a list of strings, or an empty list, or null
      */
     public List<String> getReferences() {
         return references;
     }
 }
