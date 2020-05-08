 package net.batkin.forms.server.formLoader;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import net.batkin.forms.server.configuration.Configuration;
 import net.batkin.forms.server.configuration.ConfigurationOption;
 import net.batkin.forms.server.db.dataModel.DbDataModel;
 import net.batkin.forms.server.db.dataModel.form.FormLayout;
 import net.batkin.forms.server.db.dataModel.schema.FormSchema;
 import net.batkin.forms.server.db.utility.DatabaseCollection;
 import net.batkin.forms.server.exception.ControllerException;
 import net.batkin.forms.server.upgrade.exception.UpgradeException;
 import net.batkin.forms.server.util.ConfigBuilder;
 
 import org.slf4j.LoggerFactory;
 
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 public class FormLoader {
 
 	public static void loadForms() throws IOException, UpgradeException, ControllerException {
 		String formDirName = ConfigBuilder.getResourceDirectory("forms", ConfigurationOption.CONFIG_FORMS_DIR);
 		File formDir = new File(formDirName);
 		if (!formDir.isDirectory()) {
 			return;
 		}
 
 		boolean reloadAllForms = Configuration.getInstance().getBooleanValue(ConfigurationOption.CONFIG_RELOAD_ALL_FORMS, false);
 
 		File[] jsonFiles = formDir.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".json");
 			}
 		});
 
		if (jsonFiles == null) {
			return;
		}
 		for (File jsonFile : jsonFiles) {
 			if (jsonFile.getName().endsWith("-layout.json")) {
 				processLayout(reloadAllForms, jsonFile);
 			} else if (jsonFile.getName().endsWith("-schema.json")) {
 				processSchema(reloadAllForms, jsonFile);
 			}
 		}
 	}
 
 	private static void processSchema(boolean reloadAllForms, File file) throws IOException, UpgradeException, ControllerException {
 		String data = slurpFile(file);
 		Object readObject = JSON.parse(data);
 		if (!(readObject instanceof DBObject)) {
 			throw new UpgradeException("Invalid schema data in " + file.getName());
 		}
 
 		DBObject bson = (DBObject) readObject;
 		String schemaName = DbDataModel.getStringValue(bson, "schemaName");
 
 		FormSchema schema = FormSchema.loadByName(schemaName);
 		if (schema != null && reloadAllForms) {
 			DatabaseCollection.Schemas.deleteObject(schema);
 			schema = null;
 		}
 
 		if (schema != null) {
 			return;
 		}
 
 		LoggerFactory.getLogger(FormLoader.class).info("Loading schema " + schemaName + " from " + file.getName());
 		DatabaseCollection.Schemas.saveDbObject(bson);
 	}
 
 	private static void processLayout(boolean reloadAllForms, File file) throws IOException, UpgradeException, ControllerException {
 		String data = slurpFile(file);
 		Object readObject = JSON.parse(data);
 		if (!(readObject instanceof DBObject)) {
 			throw new UpgradeException("Invalid layout data in " + file.getName());
 		}
 
 		DBObject bson = (DBObject) readObject;
 		String formName = DbDataModel.getStringValue(bson, "formName");
 
 		FormLayout layout = FormLayout.loadByName(formName);
 		if (layout != null && reloadAllForms) {
 			DatabaseCollection.Layouts.deleteObject(layout);
 			layout = null;
 		}
 
 		if (layout != null) {
 			return;
 		}
 
 		LoggerFactory.getLogger(FormLoader.class).info("Loading layout " + formName + " from " + file.getName());
 		DatabaseCollection.Layouts.saveDbObject(bson);
 	}
 
 	private static String slurpFile(File file) throws IOException {
 		FileReader reader = new FileReader(file);
 		StringBuilder builder = new StringBuilder();
 
 		char[] buf = new char[4096];
 		int numRead;
 		while ((numRead = reader.read(buf)) > 0) {
 			builder.append(buf, 0, numRead);
 		}
 
 		reader.close();
 		return builder.toString();
 	}
 }
