 package de.deepamehta.core.model;
 
 import de.deepamehta.core.util.JSONHelper;
 
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 /**
  * A data field. Part of the meta-model (like an attribute). A data field is part of a {@link TopicType}.
  * <br><br>
  * A data field has a label and a data type.
  * A data field is identified by an URI.
  * Furthermore a data field has a) an indexing mode which controls the indexing of the data field's value, and
  * b) hints which control the building of an corresponding editor widget.
  *
  * @author <a href="mailto:jri@deepamehta.de">Jörg Richter</a>
  */
 public class DataField {
     
     // ------------------------------------------------------------------------------------------------ Static Variables
 
     private static final String KEY_URI = "uri";
     private static final String KEY_LABEL = "label";
     private static final String KEY_DATA_TYPE = "data_type";
     private static final String KEY_REF_TOPIC_TYPE_URI = "ref_topic_type_uri";
     private static final String KEY_EDITABLE = "editable";
     private static final String KEY_EDITOR = "editor";
     private static final String KEY_INDEXING_MODE = "indexing_mode";
     private static final String KEY_JS_RENDERER_CLASS = "js_renderer_class";
 
     private static final Map<String, String> DEFAULT_RENDERERS = new HashMap();
     static {
         DEFAULT_RENDERERS.put("text", "TextFieldRenderer");
         DEFAULT_RENDERERS.put("number", "NumberFieldRenderer");
         DEFAULT_RENDERERS.put("date", "DateFieldRenderer");
         DEFAULT_RENDERERS.put("html", "HTMLFieldRenderer");
         DEFAULT_RENDERERS.put("reference", "ReferenceFieldRenderer");
     }
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private Map<String, Object> properties = new HashMap();
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     public DataField(String label, String dataType) {
         setLabel(label);
         setDataType(dataType);
         setDefaults();
     }
 
     public DataField(Map properties) {
         this.properties = properties;
         setDefaults();
     }
 
     public DataField(JSONObject dataField) {
         try {
             JSONHelper.toMap(dataField, properties);
             setDefaults();
         } catch (Throwable e) {
             throw new RuntimeException("Error while parsing data field " + this, e);
         }
     }
 
     // ---
 
     protected DataField() {
     }
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     public Object getProperty(String key) {
         return properties.get(key);
     }
 
     public Map<String, Object> getProperties() {
         return properties;
     }
 
     // ---
 
     public void setProperty(String key, Object value) {
         properties.put(key, value);
     }
 
     public void setProperties(Map<String, Object> properties) {
         // log override warning
         StringBuilder log = new StringBuilder();
         for (String key : properties.keySet()) {
             Object newValue = properties.get(key);
             Object oldValue = getProperty(key);
             if (oldValue != null && !oldValue.equals(newValue)) {
                 log.append("\n  " + key + ": \"" + oldValue + "\" => \"" + newValue + "\"");
             }
         }
         if (log.length() > 0) {
             logger.warning("Overriding properties of data field " + this + ":" + log);
         }
         //
        this.properties = properties;   // FIXME: use putAll() instead?
         setDefaults();
     }
 
     // ---
 
     public String getUri() {
         return (String) getProperty(KEY_URI);
     }
 
     public String getDataType() {
         return (String) getProperty(KEY_DATA_TYPE);
     }
 
     public Boolean getEditable() {
         return (Boolean) getProperty(KEY_EDITABLE);
     }
 
     public String getEditor() {
         return (String) getProperty(KEY_EDITOR);
     }
 
     public String getIndexingMode() {
         return (String) getProperty(KEY_INDEXING_MODE);
     }
 
     public String getRendererClass() {
         return (String) getProperty(KEY_JS_RENDERER_CLASS);
     }
 
     // ---
 
     public void setUri(String uri) {
         setProperty(KEY_URI, uri);
     }
 
     public void setLabel(String label) {
         setProperty(KEY_LABEL, label);
     }
 
     // "text" (default) / "number" / "date" / "html" / "reference"
     public void setDataType(String dataType) {
         setProperty(KEY_DATA_TYPE, dataType);
     }
 
     // used for dataType="reference" fields
     public void setRelatedTypeUri(String relatedTypeUri) {
         setProperty(KEY_REF_TOPIC_TYPE_URI, relatedTypeUri);
     }
 
     public void setEditable(boolean editable) {
         setProperty(KEY_EDITABLE, editable);
     }
 
     // "single line" (default) / "multi line"
     public void setEditor(String editor) {
         setProperty(KEY_EDITOR, editor);
     }
 
     // "OFF" (default) / "KEY" / "FULLTEXT" / "FULLTEXT_KEY"
     public void setIndexingMode(String indexingMode) {
         setProperty(KEY_INDEXING_MODE, indexingMode);
     }
 
     public void setRendererClass(String rendererClass) {
         setProperty(KEY_JS_RENDERER_CLASS, rendererClass);
     }
 
     // ---
 
     public JSONObject toJSON() throws JSONException {
         return new JSONObject(properties);
     }
 
     // ---
 
     @Override
     public boolean equals(Object o) {
         return ((DataField) o).getProperty(KEY_URI).equals(getProperty(KEY_URI));
     }
 
     @Override
     public String toString() {
         return "\"" + getProperty(KEY_LABEL) + "\" (uri=\"" + getProperty(KEY_URI) + "\")";
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     private void setDefaults() {
         if (getDataType() == null) {
             setDataType("text");
         }
         //
         if (getEditable() == null) {
             setEditable(true);
         }
         //
         if (getEditor() == null) {
             setEditor("single line");
         }
         //
         if (getIndexingMode() == null) {
             setIndexingMode("OFF");
         }
         //
         if (getRendererClass() == null) {
             String dataType = getDataType();
             String rendererClass = DEFAULT_RENDERERS.get(dataType);
             if (rendererClass != null) {
                 setRendererClass(rendererClass);
             } else {
                 logger.warning("No renderer declared for data field " + this +
                     " (there is no default renderer for data type \"" + dataType + "\")");
             }
         }
     }
 }
