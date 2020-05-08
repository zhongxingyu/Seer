 package interdroid.swan.tool;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 /**
  * This tool takes a sensor description, expressed as a JSON document, and
  * builds a project and code for implementing the sensor.
  * 
  * The description you pass to this tool should only include sensor specific
  * fields. Everything else will be added. The schema must be a record schema,
  * and must have a name and namespace. It may optionally include doc and author
  * properties which will be used to construct the javadoc for the generated
  * code.
  * 
  * @author nick &lt;palmer@cs.vu.nl&gt;
  * 
  */
 public class SensorMaker {
 	public static final int ERR_WRONG_ARGS = 1;
 	public static final int ERR_SCHEMA_UNREADABLE = 2;
 	public static final int ERR_PARSING_SCHEMA = 3;
 	public static final int ERR_NO_NAMESPACE = 4;
 	public static final int ERR_NO_NAME = 5;
 	public static final int ERR_NOT_A_RECORD = 6;
 	public static final int ERR_PROJECT_NOT_DIR = 7;
 	public static final int ERR_MKDIR = 8;
 	public static final int ERR_DIR_WRITE = 9;
 	public static final int FILE_NOT_WRITE = 10;
 	public static final int FILE_NOT_FILE = 11;
 	public static final int FILE_NOT_FOUND = 12;
 	public static final int UNABLE_TO_BACKUP = 13;
 	public static final int ERR_WRITING_ARRAYS = 14;
 	public static final int ERR_WRITING_PREFS = 15;
 	public static final int ERR_SCHEMA_PARSE = 16;
 	public static final int ERR_WRITING_MANIFEST = 17;
 	public static final int ERR_WRITING_CLASS = 18;
 	public static final int ERR_WRITING_CLASS_IMPL = 19;
 
 	private static final String[] ERRORS = { null,
 			"Incorrect number of arguments.",
 			"Schema file does not exist or is unreadable.",
 			"Error parsing the schema:", "Schema must have a namespace.",
 			"Schema must have a name.", "Root schema must be a record.",
 			"Unable to create project directory:",
 			"Error making the project directory:",
 			"Unable to write to project directory:", "File is not writable:",
 			"File is not a file:", "File not found:", "Unable to backup file:",
 			"Error writing arrays.", "Error writing preferences.",
 			"Error parsing schema.", "Error writing the manifest",
 			"Error writing sensor class",
 			"Error writing sensor implementation class" };
 
 	private static final String SRC_DIR = "src";
 	private static final String XML_DIR = "res/xml";
 	private static final String VALUES_DIR = "res/values";
 
 	private static final String MANIFEST_FILE = "AndroidManifest.xml";
 	private static final String PREFS_FILE_EXTENSION = "_preferences.xml";
 	private static final String ARRAYS_FILE = "_values.xml";
 	private static final String SENSOR_FILE_EXTENSION = "Sensor.java";
 	private static final String POLLER_FILE_EXTENSION = "Poller.java";
 	private static final String BACKUP_EXTENSION = ".bak";
 
 	private static final int MIN_ARGS = 1;
 	private static final int MAX_ARGS = 1;
 
 	private static final String CONFIGS = "configs";
 	private static final String NAMESPACE = "namespace";
 	private static final String NAME = "name";
 	private static final String CLASS = "class";
 	private static final String VALUE_PATHS = "valuePaths";
 	private static final String UNITS = "units";
 	private static final String UNIT = "unit";
 	private static final String DOC = "doc";
 	private static final String AUTHOR = "author";
 	private static final String TYPE = "type";
 	private static final String VALUES = "values";
 	private static final String ITEMS = "items";
 	private static final String DEFAULT = "default";
 	private static final String CUCKOO = "cuckoo";
 
 	public static void main(String[] args) {
 		if (args.length < MIN_ARGS || args.length > MAX_ARGS) {
 			usage(ERR_WRONG_ARGS);
 		}
 		File schemaFile = new File(args[0]);
 		if (!schemaFile.exists() || !schemaFile.canRead()) {
 			usage(ERR_SCHEMA_UNREADABLE);
 		}
 		try {
 			String schema = readFileAsString(schemaFile);
 
 			JSONObject schemaJSON = (JSONObject) new JSONTokener(schema)
 					.nextValue();
 
 			generateProject(schemaJSON, schemaFile.getParent());
 		} catch (Exception e) {
 			usage(ERR_SCHEMA_PARSE, e.getMessage());
 		}
 	}
 
 	private static String toFirstUpperCase(String string) {
 		return string.substring(0, 1).toUpperCase()
 				+ string.substring(1).toLowerCase();
 	}
 
 	private static String readFileAsString(File file)
 			throws java.io.IOException {
 		byte[] buffer = new byte[(int) file.length()];
 		BufferedInputStream f = null;
 		try {
 			f = new BufferedInputStream(new FileInputStream(file));
 			long read = 0;
 			do {
 				read += f.read(buffer);
 			} while (read < file.length());
 		} finally {
 			if (f != null) {
 				try {
 					f.close();
 				} catch (IOException ignored) {
 					// Ignored
 				}
 			}
 		}
 		return new String(buffer);
 	}
 
 	private static void generateProject(JSONObject schema, String projectRoot) {
 		if (projectRoot == null) {
 			projectRoot = "";
 		} else {
 			File projectDir = new File(projectRoot);
 			mkdir(projectDir);
 			projectRoot += File.separator;
 		}
 
 		File srcDir = new File(projectRoot + SRC_DIR);
 		mkdir(srcDir);
 
 		File classDir = null;
 		try {
 			classDir = new File(srcDir.getPath()
 					+ File.separator
 					+ schema.getString(NAMESPACE).replace('.',
 							File.separatorChar));
 		} catch (JSONException e) {
 			usage(ERR_NO_NAMESPACE, e.getMessage());
 		}
 		mkdir(classDir);
 
 		File xmlDir = new File(projectRoot + XML_DIR);
 		mkdir(xmlDir);
 
 		File valuesDir = new File(projectRoot + VALUES_DIR);
 		mkdir(valuesDir);
 
 		File manifest = new File(projectRoot + MANIFEST_FILE);
 		generateManifest(schema, manifest);
 
 		File arrays;
 		try {
 			arrays = new File(valuesDir.getPath() + File.separator
 					+ schema.getString(NAME).toLowerCase() + ARRAYS_FILE);
 			generateArrays(schema, arrays);
 		} catch (JSONException e) {
 			usage(ERR_NO_NAME, e.getMessage());
 		}
 
 		File prefs = null;
 		try {
 			prefs = new File(xmlDir + File.separator
 					+ schema.getString(NAME).toLowerCase()
 					+ PREFS_FILE_EXTENSION);
 		} catch (JSONException e) {
 			usage(ERR_NO_NAME, e.getMessage());
 		}
 		generatePrefs(schema, prefs);
 
 		File sensor = null;
 		try {
 			sensor = new File(classDir + File.separator
 					+ toFirstUpperCase(schema.getString(NAME))
 					+ SENSOR_FILE_EXTENSION);
 		} catch (JSONException e) {
 			usage(ERR_NO_NAME, e.getMessage());
 		}
 		generateSensor(schema, sensor);
 		try {
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 				File poller = null;
 				try {
 					poller = new File(classDir + File.separator
 							+ toFirstUpperCase(schema.getString(NAME))
 							+ POLLER_FILE_EXTENSION);
 				} catch (JSONException e) {
 					usage(ERR_NO_NAME, e.getMessage());
 				}
 				generatePoller(schema, poller);
 			}
 		} catch (JSONException e) {
 			// ignore...
 		}
 	}
 
 	private static void generatePoller(JSONObject schema, File poller) {
 		OutputStream file = makeFile(poller);
 		StringBuffer contents = new StringBuffer();
 
 		try {
 			contents.append("package ");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append(";");
 			contents.append("\n");
 			contents.append("\nimport interdroid.swan.cuckoo_sensors.CuckooPoller;");
 			contents.append("\nimport java.util.Map;");
 			contents.append("\nimport java.util.HashMap;");
 			contents.append("\n");
 			contents.append("\n/**");
 			if (schema.has(DOC)) {
 				contents.append("\n* ");
 				contents.append(schema.getString(DOC));
 			} else {
 				contents.append("\n* The ");
 				contents.append(schema.getString(NAME));
 				contents.append(" Sensor Implementation.");
 			}
 			contents.append("\n*");
 			if (schema.has(AUTHOR)) {
 				contents.append("\n* @author ");
 				contents.append(schema.getString(AUTHOR));
 				contents.append("\n*");
 			}
 			contents.append("\n*/");
 			contents.append("\npublic class ");
 			contents.append(toFirstUpperCase(schema.getString(NAME)));
 			contents.append("Poller implements CuckooPoller {");
 			contents.append("\n");
 			// Declare constants for all the configs.
 			if (schema.has(CONFIGS)) {
 				JSONArray configs = schema.getJSONArray(CONFIGS);
 				for (int i = 0; i < configs.length(); i++) {
 					JSONObject config = configs.getJSONObject(i);
 
 					contents.append("\n\t/**");
 					contents.append("\n\t* The ");
 					contents.append(config.getString(NAME));
 					contents.append(" configuration.");
 					contents.append("\n\t*/");
 					contents.append("\n\tpublic static final String ");
 					contents.append(config.getString(NAME).toUpperCase());
 					contents.append("_CONFIG = \"");
 					contents.append(config.getString(NAME));
 					contents.append("\";");
 					contents.append("\n");
 				}
 			}
 
 			// Declare constants for all the fields.
 			JSONArray fields = schema.getJSONArray(VALUE_PATHS);
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 
 				contents.append("\n\t/**");
 				contents.append("\n\t* The ");
 				contents.append(field.getString(NAME));
 				contents.append(" field.");
 				contents.append("\n\t*/");
 				contents.append("\n\tpublic static final String ");
 				contents.append(field.getString(NAME).toUpperCase());
 				contents.append("_FIELD = \"");
 				contents.append(field.getString(NAME));
 				contents.append("\";");
 				contents.append("\n");
 
 			}
 			contents.append("\n\t@Override");
 			contents.append("\n\tpublic Map<String, Object> poll(String valuePath,");
 			contents.append("\n\t\tMap<String, Object> configuration) {");
 			contents.append("\n\t\tMap<String, Object> result = new HashMap<String, Object>();");
 			contents.append("\n\t\t// put your polling code here");
 			contents.append("\n\t\treturn result;");
 			contents.append("\n\t}");
 			contents.append("\n");
 			contents.append("\n\t@Override");
 			contents.append("\n\tpublic long getInterval(Map<String, Object> configuration, boolean remote) {");
 			contents.append("\n\t\tif (remote) {");
 			contents.append("\n\t\t\tthrow new java.lang.RuntimeException(\"return the remote interval here\");");
 			contents.append("\n\t\t} else {");
 			contents.append("\n\t\t\tthrow new java.lang.RuntimeException(\"return the local interval here\");");
 			contents.append("\n\t\t}");
 			contents.append("\n\t}");
 			contents.append("\n}");
 			file.write(contents.toString().getBytes());
 		} catch (Exception e) {
 			usage(ERR_WRITING_CLASS, e.getMessage());
 		}
 
 	}
 
 	private static void generateSensor(JSONObject schema, File sensor) {
 		OutputStream file = makeFile(sensor);
 		StringBuffer contents = new StringBuffer();
 
 		try {
 			contents.append("package ");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append(";");
 			contents.append("\n");
 			contents.append("\nimport ");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append(".R;");
 			contents.append("\n");
 			contents.append("\nimport interdroid.swan.sensors.AbstractConfigurationActivity;");
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 				contents.append("\nimport interdroid.swan.sensors.AbstractCuckooSensor;");
 			} else {
 				contents.append("\nimport interdroid.swan.sensors.AbstractVdbSensor;");
 			}
 
 			contents.append("\nimport interdroid.vdb.content.avro.AvroContentProviderProxy; // link to android library: vdb-avro");
 			contents.append("\n");
 			contents.append("\nimport android.content.ContentValues;");
 			contents.append("\nimport android.os.Bundle;");
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 				contents.append("\nimport android.app.Activity;");
 				contents.append("\nimport android.util.Log;");
 				contents.append("\nimport interdroid.swan.cuckoo_sensors.CuckooPoller;");
 				contents.append("\nimport android.content.BroadcastReceiver;");
 				contents.append("\nimport android.content.Context;");
 				contents.append("\nimport android.content.Intent;");
 				contents.append("\nimport android.content.IntentFilter;");
 				contents.append("\nimport com.google.android.gms.gcm.GoogleCloudMessaging; // link to android library: google-play-services_lib");
 			}
 			contents.append("\n");
 			contents.append("\n/**");
 			if (schema.has(DOC)) {
 				contents.append("\n* ");
 				contents.append(schema.getString(DOC));
 			} else {
 				contents.append("\n* The ");
 				contents.append(schema.getString(NAME));
 				contents.append(" Sensor Implementation.");
 			}
 			contents.append("\n*");
 			if (schema.has(AUTHOR)) {
 				contents.append("\n* @author ");
 				contents.append(schema.getString(AUTHOR));
 				contents.append("\n*");
 			}
 			contents.append("\n*/");
 			contents.append("\npublic class ");
 			contents.append(toFirstUpperCase(schema.getString(NAME)));
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 				contents.append("Sensor extends AbstractCuckooSensor {");
 			} else {
 				contents.append("Sensor extends AbstractVdbSensor {");
 			}
 			contents.append("\n");
 			contents.append("\n\t/**");
 			contents.append("\n\t* The configuration activity for this sensor.");
 			contents.append("\n\t*/");
 			contents.append("\n\tpublic static class ConfigurationActivity");
 			contents.append("\n\t\textends AbstractConfigurationActivity {");
 			contents.append("\n");
 			contents.append("\n\t\t@Override");
 			contents.append("\n\t\tpublic final int getPreferencesXML() {");
 			contents.append("\n\t\t\treturn R.xml.");
 			contents.append(schema.getString(NAME).toLowerCase());
 			contents.append("_preferences;");
 			contents.append("\n\t\t}");
 			contents.append("\n");
 			contents.append("\n\t}");
 			contents.append("\n");
 
 			// Declare constants for all the configs.
 			if (schema.has(CONFIGS)) {
 				JSONArray configs = schema.getJSONArray(CONFIGS);
 				for (int i = 0; i < configs.length(); i++) {
 					JSONObject config = configs.getJSONObject(i);
 
 					contents.append("\n\t/**");
 					contents.append("\n\t* The ");
 					contents.append(config.getString(NAME));
 					contents.append(" configuration.");
 					contents.append("\n\t*/");
 					contents.append("\n\tpublic static final String ");
 					contents.append(config.getString(NAME).toUpperCase());
 					contents.append("_CONFIG = \"");
 					contents.append(config.getString(NAME));
 					contents.append("\";");
 					contents.append("\n");
 				}
 			}
 
 			// Declare constants for all the fields.
 			JSONArray fields = schema.getJSONArray(VALUE_PATHS);
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 
 				contents.append("\n\t/**");
 				contents.append("\n\t* The ");
 				contents.append(field.getString(NAME));
 				contents.append(" field.");
 				contents.append("\n\t*/");
 				contents.append("\n\tpublic static final String ");
 				contents.append(field.getString(NAME).toUpperCase());
 				contents.append("_FIELD = \"");
 				contents.append(field.getString(NAME));
 				contents.append("\";");
 				contents.append("\n");
 
 			}
 
 			contents.append("\n\t/**");
 			contents.append("\n\t* The schema for this sensor.");
 			contents.append("\n\t*/");
 			contents.append("\n\tpublic static final String SCHEME = getSchema();");
 			contents.append("\n");
 			contents.append("\n\t/**");
 			contents.append("\n\t* The provider for this sensor.");
 			contents.append("\n\t*/");
 			contents.append("\n\tpublic static class Provider extends AvroContentProviderProxy {");
 			contents.append("\n");
 			contents.append("\n\t\t/**");
 			contents.append("\n\t\t* Construct the provider for this sensor.");
 			contents.append("\n\t\t*/");
 			contents.append("\n\t\tpublic Provider() {");
 			contents.append("\n\t\t\tsuper(SCHEME);");
 			contents.append("\n\t\t}");
 			contents.append("\n");
 			contents.append("\n\t}");
 			contents.append("\n");
 			contents.append("\n\t/**");
 			contents.append("\n\t* @return the schema for this sensor.");
 			contents.append("\n\t*/");
 			contents.append("\n\tprivate static String getSchema() {");
 			contents.append("\n\t\tString scheme =");
 
 			contents.append("\n\t\t\t\"{'type': 'record', 'name': '");
 			contents.append(schema.getString(NAME));
 			contents.append("', \"");
 			contents.append("\n\t\t\t+ \"'namespace': '");
 			contents.append(schema.getString(NAMESPACE) + "."
 					+ schema.getString(NAME));
 			contents.append("',\"");
 			contents.append("\n\t\t\t+ \"\\n'fields': [\"");
 			contents.append("\n\t\t\t+ SCHEMA_TIMESTAMP_FIELDS");
 
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 
 				contents.append("\n\t\t\t+ \"\\n{'name': '\"");
 				contents.append("\n\t\t\t+ ");
 				contents.append(field.getString(NAME).toUpperCase());
 				contents.append("_FIELD");
 				contents.append("\n\t\t\t+ \"', 'type': '");
 				contents.append(field.getString(TYPE));
 				if (i < fields.length() - 1) {
 					contents.append("'},\"");
 				} else {
 					contents.append("'}\"");
 				}
 
 			}
 
 			contents.append("\n\t\t\t+ \"\\n]\"");
 			contents.append("\n\t\t\t+ \"}\";");
 			contents.append("\n\t\treturn scheme.replace('\\'', '\"');");
 			contents.append("\n\t}");
 			contents.append("\n");
 
 			contents.append("\n\t@Override");
 			contents.append("\n\tpublic final String[] getValuePaths() {");
 			contents.append("\n\t\treturn new String[] { ");
 
 			for (int i = 0; i < fields.length(); i++) {
 
 				JSONObject field = fields.getJSONObject(i);
 
 				contents.append(field.getString(NAME).toUpperCase());
 				contents.append("_FIELD");
 
 				if (i < fields.length() - 1) {
 					contents.append(", ");
 				}
 
 			}
 
 			contents.append(" };");
 			contents.append("\n\t}");
 			contents.append("\n");
 			contents.append("\n\t@Override");
 			contents.append("\n\tpublic void initDefaultConfiguration(final Bundle defaults) {");
 
 			// Setup default configurations
 			if (schema.has(CONFIGS)) {
 				JSONArray configs = schema.getJSONArray(CONFIGS);
 				for (int i = 0; i < configs.length(); i++) {
 					JSONObject config = configs.getJSONObject(i);
 					if (config.has(DEFAULT)) {
 						contents.append("\n\t\tdefaults.put");
 						contents.append(toFirstUpperCase(config.getString(TYPE)));
 						contents.append("(");
 						contents.append(config.getString(NAME).toUpperCase());
 						contents.append("_CONFIG");
 						contents.append(", ");
 						if (toFirstUpperCase(config.getString(TYPE)).equals(
 								"String")) {
 							contents.append("\"");
 							contents.append(config.getString(DEFAULT));
 							contents.append("\"");
 						} else {
 							contents.append(config.getString(DEFAULT));
 						}
 
 						contents.append(");");
 					}
 				}
 
 			}
 
 			contents.append("\n\t}");
 			contents.append("\n");
 			contents.append("\n\t@Override");
 			contents.append("\n\tpublic final String getScheme() {");
 			contents.append("\n\t\treturn SCHEME;");
 			contents.append("\n\t}");
 			contents.append("\n");
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 
 			} else {
 				contents.append("\n\t@Override");
 				contents.append("\n\tpublic void onConnected() {");
 				contents.append("\n\t\t/* Perform sensor specific sensor setup. */");
 				contents.append("\n\t}");
 				contents.append("\n");
 				contents.append("\n\t@Override");
 				contents.append("\n\tpublic final void register(final String id, final String valuePath,");
 				contents.append("\n\t\tfinal Bundle configuration) {");
 				contents.append("\n\t\tif (registeredConfigurations.size() == 1) {");
 				contents.append("\n\t\t\t/* Perform sensor specific listener registration. */");
 				contents.append("\n\t\t}");
 				contents.append("\n\t}");
 				contents.append("\n");
 				contents.append("\n\t@Override");
 				contents.append("\n\tpublic final void unregister(final String id) {");
 				contents.append("\n\t\tif (registeredConfigurations.size() == 0) {");
 				contents.append("\n\t\t\t/* Perform sensor specific listener un-registration. */");
 				contents.append("\n\t\t}");
 				contents.append("\n\t}");
 				contents.append("\n");
 				contents.append("\n\t@Override");
 				contents.append("\n\tpublic final void onDestroySensor() {");
 				contents.append("\n\t\tif (registeredConfigurations.size() > 0) {");
 				contents.append("\n\t\t\t/* Perform sensor specific listener un-registration. */");
 				// contents.append("\nunregisterReceiver(batteryReceiver);");
 				contents.append("\n\t\t}");
 				contents.append("\n\t\t/* Perform sensor specific shutdown. */");
 				contents.append("\n\t}");
 			}
 			contents.append("\n");
 
 			// Make a convenience method to store the data
 			contents.append("\n\t/**");
 			contents.append("\n\t* Data Storage Helper Method.");
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 				contents.append("\n\t* @param ");
 				contents.append(field.getString(NAME));
 				contents.append(" value for ");
 				contents.append(field.getString(NAME));
 			}
 			contents.append("\n\t*/");
 			contents.append("\n\tprivate void storeReading(");
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 				contents.append(field.getString(TYPE));
 				contents.append(" ");
 				contents.append(field.getString(NAME));
 				if (i < fields.length() - 1) {
 					contents.append(", ");
 				}
 			}
 			contents.append(") {");
 			contents.append("\n\t\tlong now = System.currentTimeMillis();");
 			contents.append("\n\t\tContentValues values = new ContentValues();");
 
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 				contents.append("\n\t\tvalues.put(");
 				contents.append(field.getString(NAME).toUpperCase());
 				contents.append("_FIELD, ");
 				contents.append(field.getString(NAME));
 				contents.append(");");
 			}
 			contents.append("\n\t\tputValues(values, now);");
 			contents.append("\n\t}");
 			contents.append("\n");
 
 			contents.append("\n\t/**");
 			contents.append("\n\t* =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
 			contents.append("\n\t* Sensor Specific Implementation");
 			contents.append("\n\t* =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
 			contents.append("\n\t*/");
 			contents.append("\n");
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 				contents.append("\n\t@Override");
 
 				contents.append("\n\tpublic final CuckooPoller getPoller() {");
 				contents.append("\n\t\treturn new "
 						+ toFirstUpperCase(schema.getString(NAME))
 						+ "Poller();");
 				contents.append("\n\t}");
 				contents.append("\n");
 				contents.append("\n\t@Override");
 				contents.append("\n\tpublic String getGCMSenderId() {");
 				contents.append("\n\t\tthrow new java.lang.RuntimeException(\"<put your gcm project id here>\");");
 				contents.append("\n\t}");
 				contents.append("\n");
 				contents.append("\n\t@Override");
 				contents.append("\n\tpublic String getGCMApiKey() {");
 				contents.append("\n\t\tthrow new java.lang.RuntimeException(\"<put your gcm api key here>\");");
 				contents.append("\n\t}");
 				contents.append("\n");
 				contents.append("\n\tpublic void registerReceiver() {");
 				contents.append("\n\t\tIntentFilter filter = new IntentFilter(\"com.google.android.c2dm.intent.RECEIVE\");");
 				contents.append("\n\t\tfilter.addCategory(getPackageName());");
 
 				contents.append("\n\t\tregisterReceiver(new BroadcastReceiver() {");
 
 				contents.append("\n\t\t\tprivate static final String TAG = \""
 						+ schema.getString(NAME) + "SensorReceiver\";");
 				contents.append("\n");
 				contents.append("\n\t\t\t@Override");
 				contents.append("\n\t\t\tpublic void onReceive(Context context, Intent intent) {");
 				contents.append("\n\t\t\t\tGoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);");
 				contents.append("\n\t\t\t\tString messageType = gcm.getMessageType(intent);");
 				contents.append("\n\t\t\t\tif (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR");
 				contents.append("\n\t\t\t\t\t\t.equals(messageType)) {");
 				contents.append("\n\t\t\t\t\tLog.d(TAG, \"Received update but encountered send error.\");");
 				contents.append("\n\t\t\t\t} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED");
 				contents.append("\n\t\t\t\t\t\t.equals(messageType)) {");
 				contents.append("\n\t\t\t\t\tLog.d(TAG, \"Messages were deleted at the server.\");");
 				contents.append("\n\t\t\t\t} else {");
 				for (int i = 0; i < fields.length(); i++) {
 					JSONObject field = fields.getJSONObject(i);
 					contents.append("\n\t\t\t\t\tif (intent.hasExtra("
 							+ field.getString(NAME).toUpperCase() + "_FIELD"
 							+ ")) {");
 					contents.append("\n\t\t\t\t\t\tstoreReading(intent.getExtras().get"
 							+ toFirstUpperCase(field.getString(TYPE))
 							+ "(\""
 							+ field.getString(NAME) + "\"));");
 					contents.append("\n\t\t\t\t\t}");
 				}
 				contents.append("\n\t\t\t\t}");
 				contents.append("\n\t\t\t\tsetResultCode(Activity.RESULT_OK);");
 				contents.append("\n\t\t\t}");
 				contents.append("\n\t}, filter, \"com.google.android.c2dm.permission.SEND\", null);");
 				contents.append("\n\t}");
 			}
 			contents.append("\n}");
 
 			file.write(contents.toString().getBytes());
 		} catch (Exception e) {
 			usage(ERR_WRITING_CLASS, e.getMessage());
 		}
 
 	}
 
 	private static void generatePrefs(JSONObject schema, File prefs) {
 		OutputStream file = makeFile(prefs);
 		StringBuffer content = new StringBuffer();
 		try {
 			content.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
 					+ "\n"
 					+ "<PreferenceScreen xmlns:android=\"http://schemas.android.com/apk/res/android\">"
 					+ "\n"
 					+ "	<PreferenceCategory android:title=\"Value Path\">"
 					+ "\n"
 					+ "		<ListPreference android:title=\"Value Path\""
 					+ "\n"
 					+ "			android:summary=\"Select a Value Path\" android:key=\"valuepath\""
 					+ "\n" + "			android:entries=\"@array/"
 					+ schema.getString(NAME) + "_valuepaths\" "
 					+ "android:entryValues=\"@array/" + schema.getString(NAME)
 					+ "_valuepaths\" />" + "\n" + "	</PreferenceCategory>"
 					+ "\n");
 			if (schema.has(CONFIGS)) {
 				content.append("	<PreferenceCategory android:title=\"Configuration\">");
 				JSONArray fields = schema.getJSONArray(CONFIGS);
 				for (int i = 0; i < fields.length(); i++) {
 					JSONObject field = fields.getJSONObject(i);
 					// TODO: Break each part into separate appends for
 					// efficiency
 					content.append("\n\t\t<" + field.getString(CLASS));
 					content.append("\n\t\t\tandroid:key=\""
 							+ field.getString(NAME) + "\"");
 					String[] names = JSONObject.getNames(field);
 					for (String name : names) {
 						if (name.startsWith("android")) {
 							content.append("\n\t\t\t");
 							content.append(name);
 							content.append("=\"");
 							content.append(field.getString(name));
 							content.append("\"");
 						}
 					}
 					content.append("\n\t\t/>");
 				}
 			}
 			content.append("\n\t</PreferenceCategory>\n</PreferenceScreen>");
 
 			file.write(content.toString().getBytes());
 		} catch (Exception e) {
 			usage(ERR_WRITING_PREFS, e.getMessage());
 		}
 	}
 
 	private static void generateArrays(JSONObject schema, File arrays) {
 		OutputStream file = makeFile(arrays);
 
 		try {
 			String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n"
 					+ "<resources>" + "\n" + "\n" + "    <!-- "
 					+ schema.getString(NAME) + " sensor -->" + "\n"
 					+ "    <string-array name=\"" + schema.getString(NAME)
 					+ "_valuepaths\">" + "\n";
 			String close = "    </string-array>\n\n";
 
 			String footer = "\n</resources>";
 			String itemOpen = "        <item>";
 			String itemClose = "</item>" + "\n";
 
 			file.write(header.getBytes());
 			JSONArray fields = schema.getJSONArray(VALUE_PATHS);
 			for (int i = 0; i < fields.length(); i++) {
 				JSONObject field = fields.getJSONObject(i);
 				file.write(itemOpen.getBytes());
 				file.write(field.getString(NAME).getBytes());
 				file.write(itemClose.getBytes());
 			}
 
 			file.write(close.getBytes());
 
 			if (schema.has(VALUES)) {
 				JSONArray values = schema.getJSONArray(VALUES);
 				for (int i = 0; i < values.length(); i++) {
 					JSONObject value = values.getJSONObject(i);
 					file.write("    <!-- ".getBytes());
 					file.write(value.getString(NAME).getBytes());
 					file.write(" -->\n".getBytes());
 					String type = value.getString(TYPE);
 					if (type.equals("string-array")) {
 						file.write("    <string-array name=\"".getBytes());
 						file.write(value.getString(NAME).getBytes());
 						file.write("\" >\n".getBytes());
 						JSONArray items = value.getJSONArray(ITEMS);
 						for (int j = 0; j < items.length(); j++) {
 							file.write(itemOpen.getBytes());
 							file.write(items.getString(j).getBytes());
 							file.write(itemClose.getBytes());
 						}
 						file.write("    </string-array>\n\n".getBytes());
 					} else if (type.equals("integer-array")) {
 						file.write("    <integer-array name=\"".getBytes());
 						file.write(value.getString(NAME).getBytes());
 						file.write("\" >\n".getBytes());
 						JSONArray items = value.getJSONArray(ITEMS);
 						for (int j = 0; j < items.length(); j++) {
 							file.write(itemOpen.getBytes());
 							file.write(String.valueOf(items.getInt(j))
 									.getBytes());
 							file.write(itemClose.getBytes());
 						}
 						file.write("    </integer-array>\n\n".getBytes());
 					} else {
 						throw new IllegalArgumentException(
 								"Unsupported values type: " + type);
 					}
 				}
 			}
 
 			file.write(footer.getBytes());
 		} catch (Exception e) {
 			usage(ERR_WRITING_ARRAYS, e.getMessage());
 		}
 	}
 
 	private static void generateManifest(JSONObject schema, File manifest) {
 		OutputStream file = makeFile(manifest);
 		StringBuffer contents = new StringBuffer();
 
 		try {
 			// Start the manifest
 			contents.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
 			contents.append("\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"");
 			contents.append("\n\tpackage=\"");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append("\"");
 			contents.append("\n\tandroid:versionCode=\"1\"");
 			contents.append("\n\tandroid:versionName=\"1.0\" >");
 
 			contents.append("\n");
 
 			// Start the application
 			contents.append("\n\t<application");
			contents.append("\n\tandroid:debuggable=\"true\"");
 			contents.append("\n\tandroid:icon=\"@drawable/ic_launcher\"");
 			contents.append("\n\tandroid:label=\"@string/app_name\" >");
 
 			contents.append("\n");
 
 			// Start the activity
 			contents.append("\n\t\t<activity android:name=\".");
 			contents.append(toFirstUpperCase(schema.getString(NAME)));
 			contents.append("Sensor$ConfigurationActivity\" android:exported=\"true\">");
 
 			contents.append("\n");
 
 			// Include the entityId meta-data
 			contents.append("\n\t\t\t\t<meta-data");
 			contents.append("\n\t\t\t\t\tandroid:name=\"entityId\" android:value=\"");
 			contents.append(schema.getString(NAME));
 			contents.append("\" />");
 
 			contents.append("\n");
 
 			// Include the valuePaths meta-data
 			contents.append("\n\t\t\t\t<meta-data");
 			contents.append("\n\t\t\t\t\tandroid:name=\"valuePaths\"");
 			contents.append("\n\t\t\t\t\tandroid:value=\"");
 
 			JSONArray fields = schema.getJSONArray(VALUE_PATHS);
 			boolean first = true;
 			for (int i = 0; i < fields.length(); i++) {
				if (!first && i < fields.length() - 1) {
 					contents.append(",");
 				} else {
 					first = false;
 				}
 				contents.append(fields.getJSONObject(i).getString(NAME));
 			}
 			contents.append("\" />\n");
 
 			// Include the units meta-data
 			contents.append("\n\t\t\t\t<meta-data");
 			contents.append("\n\t\t\t\t\tandroid:name=\"units\"");
 			contents.append("\n\t\t\t\t\tandroid:value=\"");
 
 			if (schema.has(UNITS)) {
 				JSONArray units = schema.getJSONArray(UNITS);
				first = true;
 				for (int i = 0; i < fields.length(); i++) {
					if (!first && i < fields.length() - 1) {
 						contents.append(",");
 					} else {
 						first = false;
 					}
 					String unit = "";
 					for (int j = 0; j < units.length(); j++) {
 						if (units
 								.getJSONObject(j)
 								.getString(NAME)
 								.equals(fields.getJSONObject(i).getString(NAME))) {
 							unit = units.getJSONObject(j).getString(UNIT);
 							break;
 						}
 					}
 					contents.append(unit);
 				}
 			} else {
 				for (int i = 0; i < fields.length(); i++) {
					if (!first && i < fields.length() - 1) {
 						contents.append(",");
 					} else {
 						first = false;
 					}
 					contents.append("");
 				}
 			}
 			contents.append("\" />\n");
 
 			// Include the authority meta-data
 			contents.append("\n\t\t\t\t<meta-data");
 			contents.append("\n\t\t\t\t\tandroid:name=\"authority\"");
 			contents.append("\n\t\t\t\t\tandroid:value=\"");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append('.');
 			contents.append(schema.getString(NAME));
 			contents.append("\" />\n");
 
 			// Include the configurations and default values
 			if (schema.has(CONFIGS)) {
 				JSONArray configs = schema.getJSONArray(CONFIGS);
 				for (int i = 0; i < configs.length(); i++) {
 					JSONObject config = configs.getJSONObject(i);
 					contents.append("\n\t\t\t\t<meta-data");
 					contents.append("\n\t\t\t\t\tandroid:name=\""
 							+ config.get("name") + "\"");
 					if (config.has("default")) {
 						contents.append("\n\t\t\t\t\tandroid:value=\""
 								+ config.get("default"));
 						if (config.has("type")) {
 							if (config.get("type").equals("long")) {
 								contents.append("L");
 							} else if (config.get("type").equals("double")) {
 								contents.append("D");
 							}
 						}
 						contents.append("\" />");
 					} else {
 						contents.append("\n\t\t\t\t\tandroid:value=\"null\" />");
 					}
 					contents.append("\n");
 				}
 			}
 
 			// Include the discovery intent filter
 			contents.append("\n\t\t\t<intent-filter >");
 			contents.append("\n\t\t\t\t<action android:name=\"interdroid.swan.sensor.DISCOVER\" />");
 			contents.append("\n\t\t\t</intent-filter>");
 
 			contents.append("\n");
 
 			contents.append("\n\t\t</activity>");
 
 			contents.append("\n");
 
 			// Start the service
 			contents.append("\n\t\t<service");
 			contents.append("\n\t\t\tandroid:exported=\"true\"");
 			contents.append("\n\t\t\tandroid:name=\".");
 			contents.append(toFirstUpperCase(schema.getString(NAME)));
 			contents.append("Sensor\" >");
 
 			// Finishes off the service
 			contents.append("\n\t\t</service>");
 
 			contents.append("\n");
 
 			// Do the content provider
 			contents.append("\n\t\t<provider");
 			contents.append("\n\t\t\tandroid:authorities=\"");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append('.');
 			contents.append(schema.getString(NAME));
 			contents.append("\"");
 			contents.append("\n\t\t\tandroid:name=\"");
 			contents.append(schema.getString(NAMESPACE));
 			contents.append('.');
 			contents.append(toFirstUpperCase(schema.getString(NAME)));
 			contents.append("Sensor$Provider\" />");
 
 			contents.append("\n");
 
 			// Finish off the application
 			contents.append("\n\t</application>");
 
 			contents.append("\n");
 
 			// Write the sdk version
 			contents.append("\n\t<uses-sdk android:minSdkVersion=\"7\" />");
 
 			contents.append("\n");
 
 			// Write the permissions
 			contents.append("\n\t<uses-permission android:name=\"interdroid.vdb.permission.READ_DATABASE\" />");
 			contents.append("\n\t<uses-permission android:name=\"interdroid.vdb.permission.WRITE_DATABASE\" />");
 
 			contents.append("\n");
 
 			if (schema.has(CUCKOO) && schema.getBoolean(CUCKOO)) {
 				contents.append("\n\t<!-- Start Permissions needed for Communication Offloading with Cuckoo -->");
 				contents.append("\n\t<uses-permission android:name=\"android.permission.INTERNET\" />");
 				contents.append("\n\t<uses-permission android:name=\"android.permission.GET_ACCOUNTS\" />");
 				contents.append("\n\t<uses-permission android:name=\"com.google.android.c2dm.permission.RECEIVE\" />");
 				contents.append("\n\t<permission android:name=\""
 						+ schema.getString(NAMESPACE)
 						+ ".permission.C2D_MESSAGE\" android:protectionLevel=\"signature\" />");
 				contents.append("\n\t<uses-permission android:name=\""
 						+ schema.getString(NAMESPACE)
 						+ ".permission.C2D_MESSAGE\" />");
 				contents.append("\n\t<!-- End Permissions needed for Communication Offloading with Cuckoo -->");
 			}
 
 			// Finish off the manifest
 			contents.append("\n</manifest>");
 
 			file.write(contents.toString().getBytes());
 		} catch (Exception e) {
 			usage(ERR_WRITING_MANIFEST, e.getMessage());
 		}
 	}
 
 	private static OutputStream makeFile(File file) {
 		if (file.exists()) {
 			backup(file);
 		} else {
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				usage(FILE_NOT_WRITE, file.getPath());
 			}
 		}
 		if (!file.canWrite()) {
 			usage(FILE_NOT_WRITE, file.getPath());
 		}
 		if (!file.isFile()) {
 			usage(FILE_NOT_FILE, file.getPath());
 		}
 
 		OutputStream out = null;
 		try {
 			out = new FileOutputStream(file);
 		} catch (FileNotFoundException e) {
 			usage(FILE_NOT_FOUND, file.getPath());
 		}
 		// Not reachable.
 		return out;
 	}
 
 	private static void backup(File file) {
 		String newName = file.getName() + "." + System.currentTimeMillis()
 				+ BACKUP_EXTENSION;
 		File newFile = new File(newName);
 
 		try {
 			InputStream in = new FileInputStream(file);
 			OutputStream out = new FileOutputStream(newFile);
 
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		} catch (Exception e) {
 			usage(UNABLE_TO_BACKUP, file.getName());
 		}
 		System.err.println("Backed up existing: " + file + " to " + newName);
 	}
 
 	private static void mkdir(File dir) {
 		if (dir.exists() && !dir.isDirectory()) {
 			usage(ERR_PROJECT_NOT_DIR, "The file: " + dir.getName()
 					+ " exists and is not a directory.");
 		}
 
 		if (!dir.exists() && !dir.mkdirs()) {
 			usage(ERR_MKDIR, dir.getPath());
 		}
 
 		if (!dir.canWrite()) {
 			usage(ERR_DIR_WRITE, dir.getPath());
 		}
 	}
 
 	private static void usage(int code) {
 		usage(code, null);
 	}
 
 	private static void usage(int code, String message) {
 		System.err.println(ERRORS[code]);
 		if (message != null) {
 			System.err.println(message);
 		}
 		System.err.println();
 		System.err.println("Usage:");
 		System.err.println("SensorMaker <sensor.schema>");
 
 		System.exit(code);
 	}
 }
