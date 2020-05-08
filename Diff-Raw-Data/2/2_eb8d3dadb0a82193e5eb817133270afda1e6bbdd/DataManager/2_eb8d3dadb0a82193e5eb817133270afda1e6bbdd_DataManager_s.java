 package org.sparkfiregames.chemdata.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.sparkfiregames.chemdata.R;
 import android.content.Context;
 
 /**
  * Reads and stores element data. The source is a CSV data file containing data copied
  * from Wikipedia's "List of elements" and converted into a CSV using LibreOffice Calc.
  * Note that the value separator used is not a comma, but a semicolon.
  * 
  * @author Allen Guo
  */
 public class DataManager implements Serializable {
 	
 	public static String[] FIELD_NAMES = new String[] {"Atomic number",
 												       "Symbol",
 												       "Name",
 												       "Etymology",
 												       "Group",
 												       "Period",
 												       "Weight",
 												       "Density",
 												       "Melting point",
 												       "Boiling point",
 												       "Specific heat capacity",
 												       "Electronegativity",
 												       "Abundance"};
 	
 	private static final long serialVersionUID = -6820232165935379937L;
 	
 	private ArrayList<ElementData> dataset = new ArrayList<DataManager.ElementData>(120);
 	
 	/**
 	 * Loads element data from file.
 	 */
 	public void load(Context context) throws IOException {
 		InputStream in = context.getAssets().open(context.getString(R.string.data_file));
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		String line;
 		while ((line = reader.readLine()) != null) {
 			dataset.add(new ElementData(line));
 		}
 	}
 	
 	/**
 	 * Returns {@link ElementData} for given key (atomic number, symbol,
 	 * name, or name part of 4+ characters).
 	 */
 	public ElementData getData(String key) {
 		if (key.length() <= 3) {
 			if (isInteger(key)) {
 				// Find by atomic number
 				for (ElementData element : dataset) {
 					if (element.get("at").equalsIgnoreCase(key)) {
 						return element;
 					}
 				}
 				return null;
 			} else {
 				// Find by symbol
 				for (ElementData element : dataset) {
 					if (element.get("sy").equalsIgnoreCase(key)) {
 						return element;
 					}
 				}
 				return null;
 			}
 		} else {
 			// Find by name
 			for (ElementData element : dataset) {
 				if (element.get("na").equalsIgnoreCase(key)) {
 					return element;
 				}
 			}
 			// Find by part of name
 			for (ElementData element : dataset) {
 				// Ignore-case contains()
 				if (element.get("na").toLowerCase().contains(key.toLowerCase())) {
 					return element;
 				}
 			}
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns all {@link ElementData} objects held.
 	 */
 	public List<ElementData> getData() {
 		return dataset;
 	}
 	
 	/**
 	 * Returns whether or not the given string is an integer.
 	 */
 	public static boolean isInteger(String s) {
 	    try { 
 	        Integer.parseInt(s); 
 	    } catch(NumberFormatException e) { 
 	        return false; 
 	    }
 	    return true;
 	}
 	
 	/**
 	 * Representation of one chemical element.
 	 * 
 	 * @author Allen Guo
 	 */
 	public class ElementData implements Serializable {
 		
 		private static final long serialVersionUID = -6217922548106611774L;
 		
 		private HashMap<String, String> fields = new HashMap<String, String>(FIELD_NAMES.length, 1);
 		
 		public ElementData(String data) {
 			String[] dataValues = data.split(";");
 			if (dataValues.length != FIELD_NAMES.length) {
 				throw new IllegalArgumentException("Data row does not contain correct number of columns.");
 			}
 			for (int i = 0; i < FIELD_NAMES.length; i++) {
 				fields.put(FIELD_NAMES[i].toLowerCase().substring(0, 2), dataValues[i]);
 			}
 			cleanValues();
 		}
 		
 		public void cleanValues() {
 			// Remove Wikipedia endnotes from weight
 			String we = fields.get("we");
 			if (we.contains("(")) {
 				we = we.split("\\)")[0] + ")";
 			} else if (we.contains("[")) {
 				we = we.split("\\]")[0] + "]";
 			}
 			fields.put("we", we);
 			// Remove Wikipedia endnotes from melting point and abundance
 			for (String key : new String[] {"me", "ab"}) {
 				String value = fields.get(key);
 				value = value.split(" ")[0];
 				value = value.replace("<", "&lt;");
 				fields.put(key, value);
 			}
 			// Replace empty group values
			if (fields.get("gr").isEmpty()) {
 				fields.put("gr", "n/a");
 			}
 		}
 		
 		public String get(String key) {
 			return fields.get(key);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("<b>%s (%s)</b><br>Atomic number: %s<br>Weight: %s<br>Group: %s<br>Period: %s<br>Density: %s<br>Melt: %s<br>Boil: %s<br>Heat: %s<br>Neg: %s<br>Abundance: %s", 
 			                     get("na"), get("sy"), get("at"), get("we"), get("gr"), get("pe"), get("de"), get("me"), get("bo"), get("sp"), get("el"), get("ab"));
 		}
 		
 	}
 
 }
