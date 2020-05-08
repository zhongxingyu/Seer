 package com.orangeleap.tangerine.controller.importexport;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import com.orangeleap.tangerine.util.OLLogger;
 import org.springframework.context.ApplicationContext;
 
 import com.orangeleap.tangerine.controller.importexport.importers.EntityImporter;
 import com.orangeleap.tangerine.controller.importexport.importers.EntityImporterFactory;
 
 public class ImportHandler {
 	
	public static final int MAX_ERRORS = 20;
 	
 
 	protected final Log logger = OLLogger.getLog(getClass());
 
 	private EntityImporter entityImporter;
 	private List<String[]> data;
 	private String[] header;
 
 	private int adds = 0;
 	private int changes = 0;
 	private int deletes = 0;
    private int errorCount = 0;
 	private List<String> errors = new ArrayList<String>();
 
 	public ImportHandler(ImportRequest importRequest, List<String[]> data, ApplicationContext applicationContext) {
 
 		entityImporter = new EntityImporterFactory().getEntityImporter(importRequest, applicationContext);
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
 				for (int i = 0; i < header.length; i++) header[i] = header[i].trim();
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
 			String msg = translate(e);
 			msg = "Error in import file line "+linenumber+": "+msg;
 			logger.error(msg);
            errorCount++;
 			if (errors.size() < MAX_ERRORS) {
 				errors.add(msg);
 			} else if (errors.size() == MAX_ERRORS) {
 				errors.add("Additional errors truncated...");
 			}
 		}
 
 	}
 	
 	private String translate(Exception e) {
         String msg = e.getMessage();
         if (msg == null || msg.trim().length() == 0) msg = e.getClass().getSimpleName();
 		if (msg.contains("fieldValidationFailure")) {
 			msg = msg.substring(msg.indexOf("arguments"));
 			msg = msg.substring(msg.indexOf("[")+1);
 			msg = msg.substring(0,msg.indexOf("]"));
 			msg = "Invalid value: "+msg;
 		}
 		return msg;
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
 
    public int getErrorCount() {
        return errorCount;
    }



 	public List<String> getErrors() {
 		return errors;
 	}
 
 
 }
