 package org.kvj.lima1.sync.controller.data;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.kvj.bravo7.ApplicationContext;
 import org.kvj.bravo7.SuperActivity;
 
 import android.text.TextUtils;
 import android.util.Log;
 
 public class AppInfo {
 	private static final String TAG = "AppInfo";
 	public AppDBHelper db = null;
 	public String name = null;
 	private long id = 0;
 	private JSONObject schema = null;
 	public SchemaInfo schemaInfo = null;
 	private ApplicationContext context;
 	private File folderName = null;
 
 	public AppInfo(ApplicationContext context, String name) {
 		this.name = name;
 		this.context = context;
 		db = new AppDBHelper(context, name + "-app.db");
 		if (!db.open()) {
 			db = null;
 		}
 		String json = context.getStringPreference(name + "-schema", "");
 		if (!TextUtils.isEmpty(json)) {
 			try {
 				schema = new JSONObject(json);
 				schemaInfo = parseSchema(schema);
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	public synchronized long id() {
 		long now = System.currentTimeMillis();
 		if (now > id) {
 			id = now;
 			return id;
 		}
 		return ++id;
 	}
 
 	private SchemaInfo parseSchema(JSONObject schema) throws JSONException {
 		SchemaInfo info = new SchemaInfo();
 		info.parseSchema(schema);
 		return info;
 	}
 
 	private List<String> generateAlter(String table, TableInfo oldSchema, TableInfo newSchema) {
 		List<String> sqls = new ArrayList<String>();
 		for (int i = oldSchema.numbers.size(); i < newSchema.numbers.size(); i++) {
 			// New numbers
 			sqls.add(generateAlter(table, newSchema.numbers.get(i), "integer"));
 		}
 		for (int i = oldSchema.texts.size(); i < newSchema.texts.size(); i++) {
 			// New texts
 			sqls.add(generateAlter(table, newSchema.texts.get(i), "text"));
 		}
 		for (int i = oldSchema.indexes.size(); i < newSchema.indexes.size(); i++) {
 			// New indexes
 			sqls.add(createIndex(table, i, newSchema.indexes.get(i)));
 		}
 		return sqls;
 	}
 
 	private String generateAlter(String table, String field, String type) {
 		return "alter table t_" + table + " add f_" + field + " " + type;
 	}
 
 	private List<String> generateCreate(String table, TableInfo info) {
 		List<String> sqls = new ArrayList<String>();
 		StringBuffer create = new StringBuffer(
 				"create table if not exists t_"
 						+ table
 						+ " (id integer primary key, status integer default 0, updated integer default 0, own integer default 1, stream text, data text");
 		for (String field : info.numbers) { // Add numbers
 			create.append(", f_" + field + " integer");
 		}
 		for (String field : info.texts) { // Add texts
 			create.append(", f_" + field + " text");
 		}
 		create.append(")");
 		sqls.add(create.toString());
 		for (int i = 0; i < info.indexes.size(); i++) { // Create index
 			sqls.add(createIndex(table, i, info.indexes.get(i)));
 		}
 		return sqls;
 	}
 
 	private String createIndex(String table, int index, List<String> fields) {
 		StringBuffer create = new StringBuffer("create index i_" + table + "_" + index + " on t_" + table + " (status");
 		for (String field : fields) { // Add fields
 			create.append(", f_" + field);
 		}
 		create.append(")");
 		return create.toString();
 	}
 
 	public String upgradeSchema(JSONObject newSchema) {
 		try { // DB and JSON errors
 			SchemaInfo newSchemaInfo = new SchemaInfo();
 			newSchemaInfo.parseSchema(newSchema);
 			// Log.i(TAG, "upgradeSchema: " + schemaInfo + ", " +
 			// newSchemaInfo);
 			if (null == schemaInfo || schemaInfo.upgrades < newSchemaInfo.upgrades) {
 				// New or upgrades are different
 				List<String> sqls = new ArrayList<String>();
 				for (String table : newSchemaInfo.tables.keySet()) {
 					// Check every table
 					if (null == schemaInfo || !schemaInfo.tables.containsKey(table)) {
 						// Create table
 						sqls.addAll(generateCreate(table, newSchemaInfo.tables.get(table)));
 					} else {
 						sqls.addAll(generateAlter(table, schemaInfo.tables.get(table), newSchemaInfo.tables.get(table)));
 					}
 				}
 				// Log.i(TAG, "Upgrade: " + sqls);
 				for (String sql : sqls) { // Execute SQL
 					Log.i(TAG, "Upgrade schema: " + sql);
 					db.getDatabase().execSQL(sql);
 				}
 				schemaInfo = newSchemaInfo;
 			}
 			return null;
 		} catch (Exception e) {
 			Log.e(TAG, "Error upgrading schema", e);
 			return "Error upgrading DB schema";
 		}
 	}
 
 	public void setSchema(JSONObject newSchema) {
 		schema = newSchema;
 		context.setStringPreference(name + "-schema", newSchema.toString());
 	}
 
 	public TableInfo getTableInfo(String stream) {
 		if (null == db) { // No DB
 			return null;
 		}
 		if (null == schemaInfo) { // Not synchronized
 			return null;
 		}
 		return schemaInfo.tables.get(stream);
 	}
 
 	public File getFilesFolder() {
		if (null == folderName) { //
 			folderName = new File(SuperActivity.getExternalCacheFolder(context), "files" + File.separatorChar + name);
 			if (!folderName.exists()) { // Not created yet
 				if (!folderName.mkdirs()) { // Failed
 					Log.w(TAG, "Error creating cache files folder");
 				}
 			}
 		}
 		return folderName;
 	}
 }
