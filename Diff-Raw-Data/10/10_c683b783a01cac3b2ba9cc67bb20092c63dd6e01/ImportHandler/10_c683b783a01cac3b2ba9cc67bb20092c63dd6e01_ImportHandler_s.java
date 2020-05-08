 package com.orangeleap.tangerine.controller.importexport;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationContext;
 
 import com.orangeleap.tangerine.controller.importexport.importers.EntityImporter;
 import com.orangeleap.tangerine.controller.importexport.importers.EntityImporterFactory;
 
 public class ImportHandler {
 
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private EntityImporter entityImporter;
 	private List<String[]> data;
 	private String[] header;
 
 	private int adds = 0;
 	private int changes = 0;
 	private int deletes = 0;
 	private List<String> errors = new ArrayList<String>();
 
 	public ImportHandler(String entity, List<String[]> data, ApplicationContext applicationContext) {
 
 		entityImporter = new EntityImporterFactory().getEntityImporter(entity, applicationContext);
 		if (entityImporter == null) throw new RuntimeException("Select import type.");
 		this.data = data;
 
 	}
 
 	/*
 	 * "action" is a special column name that can have the values "add", "change", or "delete"
 	 * If it is missing, "add" is assumed.  For adds, the "account number" column is ignored.
 	 * For changes or deletes, "account number" is required.
 	 */
 	public void importData() {
 
 		if (data == null) return;
 		for (int linenumber = 0; linenumber < data.size() ;linenumber++) {
 			String[] line = data.get(linenumber); // Excel numbers starting at 1
 			if (header == null) {
 				header = line; // First line in CSV file is field name header
 			} else {
 				processLine(linenumber + 1, header, line);
 			}
 		}
 
 	}
 
 	private void processLine(int linenumber, String[] header, String[] line) {
 
 		logger.debug("Importing line " + linenumber);
 
 		Map<String, String> values = new HashMap<String, String>();
 		for (int i = 0; i < header.length && i < line.length; i++) {
 			values.put(header[i], line[i]);
 		}
 
 		String action = values.remove("action");
 		if (action == null) {
 			action = EntityImporter.ACTION_ADD; // default if not specified
 		}
 		action = action.toLowerCase();
 
 		try {
 			
 			if (!action.equals(EntityImporter.ACTION_ADD) && !action.equals(EntityImporter.ACTION_CHANGE) && !action.equals(EntityImporter.ACTION_DELETE)) throw new RuntimeException("Invalid action.");
 			
 			entityImporter.importValueMap(action, values);
 
 			if (EntityImporter.ACTION_ADD.equalsIgnoreCase(action)) {
 				adds++;
 			}
 			if (EntityImporter.ACTION_CHANGE.equalsIgnoreCase(action)) {
 				changes++;
 			}
 			if (EntityImporter.ACTION_DELETE.equalsIgnoreCase(action)) {
 				deletes++;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			String msg = "Error in import file line "+linenumber+": "+e.getMessage();
 			logger.error(msg);
			errors.add(msg);
 		}
 
 	}
 
 	public int getAdds() {
 		return adds;
 	}
 
 	public int getChanges() {
 		return changes;
 	}
 
 	public int getDeletes() {
 		return deletes;
 	}
 
 	public List<String> getErrors() {
 		return errors;
 	}
 
 
 }
