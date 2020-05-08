 package org.msf.survey.monthly.fup;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class CSVDataExport {
 	
 	public static List<File> generateDefaultReports(List<JSONObject> forms) throws JSONException, IOException {
 		List<File> reports = new ArrayList<File>(forms.size());
 		
 		for (JSONObject form : forms) {
 			final String formName = form.getString("name");
 			
 			File[] encountersForForm = Constants.ENCOUNTER_DIR.listFiles(new FilenameFilter() {
 				
 				@Override
 				public boolean accept(File dir, String filename) {
 					return filename.startsWith("encounter-" + formName);
 				}
 			});
 			
 			String reportName = FileUtilities.getSaveFileDatedName("report", formName, "csv");
 			File report = new File(Constants.REPORT_STORAGE_DIR, reportName);
 			
 			generateReport(form, encountersForForm, report);
 			reports.add(report);
 		}
 		
 		return reports;
 	}
 	
 	public static void generateReport(JSONObject formJSON, File[] encounters, File reportFile) throws IOException, JSONException{
 		List<String> conceptIds = getFormConceptIds(formJSON);
 		
 		reportFile.createNewFile();
 		CSVWriter writer = new CSVWriter(new FileWriter(reportFile));
 		
 		writer.writeNext(conceptIds.toArray(new String[conceptIds.size()]));
 		
 		String[] obs;
 		for (File encounter : encounters) {
 			obs = getObservationValues(new BufferedInputStream(new FileInputStream(encounter)), conceptIds);
 			writer.writeNext(obs);
 		}
 		
 		writer.close();
 	}
 	
 	public static List<String> getFormConceptIds(JSONObject formJSON) throws IOException, JSONException{
 		List<String> concepts = new ArrayList<String>();
 		
 		//pages in form
 		JSONArray pages = formJSON.optJSONArray("pages");
 		if (pages != null) {
 			for(int i = 0; i < pages.length(); i++) {
 				JSONObject page = pages.getJSONObject(i);
 				JSONObject content = page.getJSONObject("content");
 				JSONArray views = content.optJSONArray("views");
 				if (views != null) {
 					for(int j = 0; j < views.length(); j++) {
 						readViewsWithRecursion(views.getJSONObject(j), concepts);
 					}
 				}
 			}
 		} else {
 			throw new JSONException("Not a valid form!");
 		}
 		
 		return concepts;
 	}
 	
 	private static void readViewsWithRecursion(JSONObject object, List<String> concepts) throws JSONException {
 		if(object == null) {
 			return;
 		}
 		
 		String conceptId = object.optString("conceptId");
 		if (conceptId != null && conceptId.length() > 0) {
 			concepts.add(conceptId);
 		}
 		
 		//add child views with recursion
 		JSONArray children = object.optJSONArray("children");
 		if (children != null) {
 			for (int i = 0; i < children.length(); i++) {
 				readViewsWithRecursion(children.optJSONObject(i), concepts);
 			}
 		}
 	}
 	
 	private static String[] getObservationValues(InputStream encounterInputStream, List<String> concepts) throws IOException, JSONException {
 		String[] result = new String[concepts.size()];
 		Arrays.fill(result, "");
 		
 		String jsonString = FileUtilities.readStringFromInputStream(encounterInputStream);
 		JSONObject encounter = new JSONObject(jsonString);
 		
 		JSONArray obsList = encounter.getJSONArray("obs");
 		JSONObject obs;
 		String conceptId;
 		String value;
 		int conceptIndex;
 		for (int i = 0; i < obsList.length(); i++) {
 			try {
 				obs = obsList.getJSONObject(i);
 				conceptId = obs.getString("conceptId");
				value = obs.get("value").toString();
 				conceptIndex = concepts.indexOf(conceptId);
 				if (conceptId.length() == 0) {
 					Log.d("CSVDataExport", "Blank conceptId! Value is: " + value);
 				} else if (value == null) {
 					value = "";
 				} else if (conceptIndex < 0 || conceptIndex >= result.length) {
 					Log.d("CSVDataExport", "Illegal conceptId: " + 
 							conceptId + ", index " + conceptIndex + ", value" + value);
 				}
 				result[conceptIndex] = value;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return result;
 	}
 }
