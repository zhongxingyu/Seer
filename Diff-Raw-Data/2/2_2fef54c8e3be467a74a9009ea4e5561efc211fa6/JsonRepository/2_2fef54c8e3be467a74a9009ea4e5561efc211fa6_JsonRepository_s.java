 package org.smartly.commons.io.jsonrepository;
 
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.smartly.commons.lang.CharEncoding;
 import org.smartly.commons.logging.Level;
 import org.smartly.commons.logging.Logger;
 import org.smartly.commons.logging.util.LoggingUtils;
 import org.smartly.commons.util.*;
 
 import java.io.File;
 import java.util.*;
 
 public class JsonRepository {
 
     private static final String NAME = "idx_name";
     private static final String VALUE = "idx_value";
     private static final String WILD_CHAR = "*.json";
     private static final String FLD_ID = "_id";
     private static final String FLD_PARENT = "_parent";
     private static final String CHARSET = CharEncoding.getDefault();
 
     private final String _root;
     private final JSONObject _indexes;
     private final Map<String, Object> _cache;
     private boolean _useCache;
 
     public JsonRepository(final String root) throws Exception {
         _root = root;
         _indexes = new JSONObject();
         _cache = new HashMap<String, Object>();
         this.load();
     }
 
     public Object get(final String path) {
         return this.lookupValue(path);
     }
 
     public List<JSONObject> getList(final String path) {
         final List<JSONObject> result = new LinkedList<JSONObject>();
         final Object data = this.get(path);
         if (data instanceof JSONObject) {
             final JSONObject jsonData = (JSONObject) data;
             final Iterator keys = jsonData.keys();
             while (keys.hasNext()) {
                 final String key = keys.next().toString();
                 final Object value = jsonData.opt(key);
                 if (value instanceof JSONObject) {
                     result.add((JSONObject) value);
                 }
             }
         }
         return result;
     }
 
     //-- utils --//
 
     public JSONObject getJSONObject(final String path) {
         final Object result =  this.get(path);
         if(result instanceof JSONObject){
            return (JSONObject)result;
         }
         return null;
     }
 
     public JSONArray getJSONArray(final String path) {
         final Object result =  this.get(path);
         if(result instanceof JSONArray){
             return (JSONArray)result;
         }
         return null;
     }
 
     public String getString(final String path) {
         return this.getString(path, "");
     }
 
     public String getString(final String path, final String def) {
         final Object value = this.get(path);
        return null != value ? (String) value : "";
     }
 
     public String[] getStringArray(final String path) {
         final Object value = this.get(path);
         return (value instanceof JSONArray)?JsonWrapper.toArrayOfString((JSONArray)value):new String[0];
     }
 
     public boolean getBoolean(final String path) {
         return this.getBoolean(path, false);
     }
 
     public boolean getBoolean(final String path, final boolean def) {
         final Object value = this.get(path);
         return ConversionUtils.toBoolean(value, def);
     }
 
     public int getInt(final String path) {
         return this.getInt(path, 0);
     }
 
     public int getInt(final String path, final int def) {
         final Object value = this.get(path);
         return ConversionUtils.toInteger(value, def);
     }
 
     public double getDouble(final String path) {
         return this.getDouble(path, 0.0);
     }
 
     public double getDouble(final String path, final double def) {
         final Object value = this.get(path);
         return ConversionUtils.toDouble(value, -1, def);
     }
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
 
     private Logger getLogger() {
         return LoggingUtils.getLogger(this);
     }
 
     private Object cacheGet(final String key) {
         synchronized (_cache) {
             return _cache.get(key);
         }
     }
 
     private void cacheAdd(final String key, final Object value) {
         synchronized (_cache) {
             _cache.put(key, value);
         }
     }
 
     //-- Methods for index initialization --//
 
     private void load() throws Exception {
         final List<File> files = new LinkedList<File>();
         FileUtils.listFiles(files, new File(_root), WILD_CHAR);
         for (final File file : files) {
             this.load(file);
         }
     }
 
     private void load(final File file) {
         if (null != file) {
             final String path = PathUtils.subtract(_root, PathUtils.validateFolderSeparator(file.getAbsolutePath()));
             final String[] tokens = StringUtils.split(path, "/");
             JSONObject parent = _indexes;
             for (final String token : tokens) {
                 parent = this.load(parent, token, file);
             }
         }
     }
 
     private JSONObject load(final JSONObject parent, final String path, final File file) {
         final String ext = PathUtils.getFilenameExtension(path);
         final JSONObject result;
         if (StringUtils.hasText(ext)) {
             // file
             result = this.loadFile(parent, path, file);
         } else {
             // directory
             result = this.loadDir(parent, path, file);
         }
         return result;
     }
 
     private JSONObject loadDir(final JSONObject parent, final String path, final File file) {
         final String name = PathUtils.getFilename(path, false);
         JSONObject result = parent.optJSONObject(name);
 
         if (null == result) {
             result = new JSONObject();
             this.setValue(result, NAME, name);
             // add node to parent
             this.setValue(parent, name, result);
         }
         return result;
     }
 
     private JSONObject loadFile(final JSONObject parent, final String path, final File file) {
         final String name = PathUtils.getFilename(path, false);
         final String parentName = parent.optString(NAME);
 
         if (name.equalsIgnoreCase(parentName)) {
             this.setValue(parent, VALUE, file.getAbsolutePath());
             return parent;
         }
 
         final JSONObject result = new JSONObject();
         this.setValue(result, NAME, name);
         this.setValue(result, VALUE, file.getAbsolutePath());
 
         // add node to parent
         this.setValue(parent, name, result);
 
         return result;
     }
 
     private void setValue(final JSONObject object, final String key,
                           final Object value) {
         try {
             object.set(key, value);
         } catch (Throwable t) {
             this.getLogger().log(Level.SEVERE, null, t);
         }
     }
 
     private JSONObject getIndex(final String indexName) {
         synchronized (_indexes) {
             return JsonWrapper.getJSON(_indexes, indexName);
         }
     }
 
     //-- Methods for values --//
 
     private Object lookupValue(final String path) {
         Object result = null;
         if (_useCache) {
             result = this.cacheGet(path);
         }
         if (null == result) {
             result = this.loadValue(path);
             if (_useCache && null != result) {
                 this.cacheAdd(path, result);
             }
         }
         return result;
     }
 
     private Object loadValue(final String path) {
         final int dotcount = StringUtils.countOccurrencesOf(path, ".");
         if (dotcount == 0) {
             final JSONObject index = this.getIndex(path);
             return this.loadItemFromIndex(index);
         } else {
             for (int i = dotcount; i > 0; i--) {
                 final String[] tokens = StringUtils.splitAt(i, path, ".");
                 if (tokens.length == 2) {
                     final JSONObject index = this.getIndex(tokens[0]);
                     if (null != index) {
                         final JsonDataManager data = this.loadDataFromIndex(index);
                         return data.get(tokens[1]);
                     }
                 }
             }
         }
         return null;
     }
 
     private JsonDataManager loadDataFromIndex(final JSONObject index) {
         if (null != index) {
             return new JsonDataManager(index);
         }
         return null;
     }
 
     private JSONObject loadItemFromIndex(final JSONObject index) {
         final JsonDataManager data = this.loadDataFromIndex(index);
         return null != data ? data.getAll() : null;
     }
 
     private class JsonDataManager {
 
         private final JSONObject _metadata;
         private final JSONObject _data;
 
         public JsonDataManager(final JSONObject metadata) {
             _metadata = metadata;
             _data = new JSONObject();
             this.init();
         }
 
         public JSONObject getAll() {
             return _data;
         }
 
         public Object get(final String path) {
             return JsonWrapper.get(_data, path);
         }
 
         public String getID() {
             if (null != _data) {
                 return _data.optString(FLD_ID, null);
             }
             return null;
         }
 
         public String getPARENT() {
             if (null != _data) {
                 return _data.optString(FLD_PARENT, null);
             }
             return null;
         }
 
         // ------------------------------------------------------------------------
         //                      p r i v a t e
         // ------------------------------------------------------------------------
         private Logger getLogger() {
             return LoggingUtils.getLogger();
         }
 
         private void init() {
             this.init(_data, _metadata);
         }
 
         private void init(final JSONObject parent, final JSONObject metadata) {
             final Iterator keys = metadata.keys();
             while (keys.hasNext()) {
                 final String key = keys.next().toString();
                 if (!NAME.equalsIgnoreCase(key)) {
                     // creates node or load details
                     if (VALUE.equalsIgnoreCase(key)) {
                         final String value = metadata.optString(key);
                         this.loadData(parent, value);
                         //-- _ID --//
                         this.set_ID(parent, metadata.optString(NAME));
                     } else {
                         // add new property
                         final JSONObject itemData = new JSONObject();
                         JsonWrapper.put(parent, key, itemData);
                         // retrieve metadata
                         final JSONObject itemMetadata = metadata.optJSONObject(key);
                         if (null != itemMetadata) {
                             this.init(itemData, itemMetadata);
                         }
                         //-- _PARENT --//
                         this.set_PARENT(itemData, metadata.optString(NAME));
                     }
                 }
             }
         }
 
         private void set_ID(final JSONObject itemData, final String id) {
             try {
                 if (!itemData.has(FLD_ID)) {
                     itemData.putOpt(FLD_ID, id);
                 }
             } catch (Throwable t) {
                 this.getLogger().log(Level.SEVERE, null, t);
             }
         }
 
         private void set_PARENT(final JSONObject itemData, final String id) {
             try {
                 if (!itemData.has(FLD_PARENT)) {
                     itemData.putOpt(FLD_PARENT, id);
                 }
             } catch (Throwable t) {
                 this.getLogger().log(Level.SEVERE, null, t);
             }
         }
 
         private void loadData(final JSONObject data, final String filename) {
             try {
                 final String jsontext = new String(FileUtils.copyToByteArray(new File(filename)), CHARSET);
                 if (StringUtils.hasText(jsontext)) {
                     final JSONObject json = new JSONObject(jsontext);
                     JsonWrapper.extend(data, json);
                 }
             } catch (Throwable t) {
                 this.getLogger().log(Level.SEVERE, null, t);
             }
         }
     }
 
 }
