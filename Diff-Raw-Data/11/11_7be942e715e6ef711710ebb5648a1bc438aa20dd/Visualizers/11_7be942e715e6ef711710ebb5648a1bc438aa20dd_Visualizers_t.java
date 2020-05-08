 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Visualizers {
 	
	public static String spiralHTML = "Analyze a session's data with the <b>Spiral</b> visualizer";
	public static String waveHTML = "Analyze a session's data with the <b>Wave</b> visualizer";
	public static String graphicalHTML = "Analyze a session's data with the <b>Graphing</b> visualizer";
	public static String verboseHTML = "Use the <b>Verbose Output</b> visualizer (testing)";
 	
 	
 	public static ArrayList<String> names = new ArrayList<String>();
 	public static HashMap<String,String> vizMap = new HashMap<String,String>();
 	static
 	{
 		names.add("Spiral");
 		names.add("Wave");
 		names.add("Verbose");
 		names.add("Graphical");
 		vizMap.put("Spiral", spiralHTML);
 		vizMap.put("Wave", waveHTML);
 		vizMap.put("TestVerbose", verboseHTML);
	//	vizMap.put("Graphical", graphicalHTML);
 	}
 
 	public static List<String>  getNames() {
 		return names;
 	}
 	
 	public static String getHTMLFor( String viz )
 	{
 		return vizMap.get(viz);
 	}
 
 }
