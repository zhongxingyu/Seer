 package de.enwida.web.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.enwida.transport.DataResolution;
 
 public class NavigationDictionary implements Cloneable {
 
 	private String title;
 	private String xAxisLabel;
 	private String yAxisLabel;
 	private Map<Integer, String> tsos;
 	private Map<String, String> timeRanges;
 	private Map<DataResolution, String> resolutions;	
 	private Map<String, String> infoKeys;
 	
 	public NavigationDictionary() {
 		title = "";
 		xAxisLabel = "";
 		yAxisLabel = "";
 		tsos = new HashMap<Integer, String>();
 		timeRanges = new HashMap<String, String>();
 		resolutions = new HashMap<DataResolution, String>();
 		infoKeys = new HashMap<String, String>();
 	}
 	
 	public NavigationDictionary clone() {
 		final NavigationDictionary result = new NavigationDictionary();
 		result.title = title;
 		result.xAxisLabel = xAxisLabel;
 		result.yAxisLabel = yAxisLabel;
 		result.tsos = new HashMap<Integer, String>(tsos);
 		result.timeRanges = new HashMap<String, String>(timeRanges);
 		result.resolutions = new HashMap<DataResolution, String>(resolutions);
 		result.infoKeys = new HashMap<String, String>(infoKeys);
 
 		return result;
 	}
 	
 	public Map<Integer, String> getTsos() {
 		return tsos;
 	}
 	public Map<String, String> getTimeRanges() {
 		return timeRanges;
 	}
 	public Map<DataResolution, String> getResolutions() {
 		return resolutions;
 	}
 	public Map<String, String> getInfoKeys() {
 		return infoKeys;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getxAxisLabel() {
 		return xAxisLabel;
 	}
 
 	public void setxAxisLabel(String xAxisLabel) {
 		this.xAxisLabel = xAxisLabel;
 	}
 
 	public String getyAxisLabel() {
 		return yAxisLabel;
 	}
 
 	public void setyAxisLabel(String yAxisLabel) {
 		this.yAxisLabel = yAxisLabel;
 	}	
 	
 }
