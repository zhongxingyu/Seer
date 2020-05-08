 package web;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import what.FileHelper;
 import what.JSONReader;
 import what.Printer;
 
 /**
  * Singleton scanning all chart types available.
  * 
  * @author Lukas, PSE Gruppe 14
  */
 public class ChartIndex {
 	/** the instance of chart index for this singleton class. */
 	private static ChartIndex instance = null;
 	
 	/** an array containing the names of all available charts. */
 	private String[] charts;
 	
 	/** a hashmap containing the thumb-nail picture name of a chart. */
 	private HashMap<String, String> thumbs;
 	
 	/** a hashset containing the names of charts that have a css file. */
 	private HashSet<String> css;
 	
 	/** a hashmap containing the dimensions of a chart as read from the charts config file. */
 	private HashMap<String, Integer> dims;
 	
	/** a hashmap containing the dimensions of a chart as read from the charts config file. */
 	private HashMap<String, Boolean> aggregations;
 	
 	/**
 	 * on initialization scans the charts directory for sub-directories a.k.a. chartTypes.
 	 */
 	private ChartIndex() {
 		thumbs = new HashMap<String, String>();
 		css = new HashSet<String>();
 		dims = new HashMap<String, Integer>();
 		aggregations = new HashMap<String, Boolean>();
 		File chartDir = new File("./charts");
 		File[] dirList = chartDir.listFiles(new FileFilter() {
 			//accept only charts that have all components
 			public boolean accept(File file) {
 				if (file.isDirectory()) {
 					boolean thumb = false;
 					boolean js = false;
 					boolean config = false;
 					String name = file.getName();
 					for (String s : file.list()) {
 						if (s.matches("^(thumb)\\..*")) {
 							thumb = true;
 							thumbs.put(file.getName(), s);
 						}
 						if (s.equalsIgnoreCase(file.getName() + ".js")) {
 							js = true;
 						}
 						if (s.equalsIgnoreCase(file.getName() + ".css")) {
 							css.add(name);
 						}
 						if (s.equalsIgnoreCase("config.json")) {
 							String jsonString = FileHelper.getStringContent(new File(file, s));
 							if (jsonString != null) {
 								JSONObject json = JSONReader.getJSONObjectForString(jsonString);
 								if (json == null) {
 									Printer.perror("No legal JSONString in chart config.");
 									return false;
 								}
 								JSONReader reader = new JSONReader(json);
 								int dim = reader.getInt("dimensions");
 								if ((dim > 0) && (dim < 3)) {
 									dims.put(name, dim);
 									config = true;
 								} else {
 									Printer.perror("Illegal number of dimensions.");
 									return false;
 								}
 								
 								int agg = reader.getInt("aggregation");
 								if (agg < 0) {
 									Printer.perror("Illegal statement for aggregation attribute.");
 									return false;
 								} else if (agg == 0) {
 									aggregations.put(name, false);
 								} else {
 									aggregations.put(name, true);
 								}
 								
 							}
 						}
 					}
 					return thumb && js && config;
 				}
 				return false; 
 			}
 		});
 		charts = new String[dirList.length];
 		for (int i = 0; i < dirList.length; i++) {
 			charts[i] = dirList[i].getName();
 		}
 	}
 	
 	/**
 	 * get the instance of ChartIndex, initializes one, if not already done.
 	 * @return returns the instance of ChartIndex
 	 */
 	public static ChartIndex getInstance() {
 		if (instance == null) {
 			instance = new ChartIndex();
 		}
 		
 		return instance;
 	}
 	
 	/**
 	 * returns all names of valid charts.
 	 * 
 	 * @return a string array with all the names
 	 */
 	public String[] getCharts() {
 		return charts;
 	}
 	
 	/**
 	 * returns the file name of a thumb-nail for the chart type.
 	 * 
 	 * @param chart the chart name
 	 * @return returns the file name
 	 */
 	public String getThumb(String chart) {
 		return thumbs.get(chart);
 	}
 	
 	/**
 	 * returns whether a chart has a chart specific CSS file.
 	 * 
 	 * @param chart the chart name
 	 * @return returns true if a chart specific CSS file exists, false otherwise
 	 */
 	public boolean hasCss(String chart) {
 		return css.contains(chart);
 	}
 	
 	/**
 	 * get the number of dimensions for a chart.
 	 * 
 	 * @param chart the name of the chart
 	 * @return returns the number of dimensions
 	 */
 	public int getDim(String chart) {
 		return dims.get(chart);
 	}
 	
 	

 	public boolean getAggregation(String chart) {
 		return aggregations.get(chart);
 	}
 }
