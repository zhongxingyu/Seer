 package com.robestone.hudson.compactcolumns;
 
 import java.util.Locale;
 
 import hudson.model.Run;
 
 public class BuildInfo implements Comparable<BuildInfo> {
 
 	private Run<?, ?> run;
 	private String color;
 	private String timeAgoString;
 	private long buildTime;
 	private String status;
 	private String urlPart;
 	private boolean isFirst;
 	private boolean isLatestBuild;
 	private boolean multipleBuilds;
 	
 	public BuildInfo(Run<?, ?> run, String color, String timeAgoString,
 			long buildTime, String status, String urlPart,
 			boolean isLatestBuild) {
 		this.run = run;
 		this.color = color;
 		this.timeAgoString = timeAgoString;
 		this.buildTime = buildTime;
 		this.status = status;
 		this.urlPart = urlPart;
 		this.isLatestBuild = isLatestBuild;
 	}
 	public Run<?, ?> getRun() {
 		return run;
 	}
 	public String getColor() {
 		return color;
 	}
 	public String getTimeAgoString() {
 		return timeAgoString;
 	}
 	public String getBuildTimeString(Locale locale) {
 		return AbstractCompactColumn.getBuildTimeString(buildTime, locale);
 	}
 	public String getStatus() {
 		return status;
 	}
 	public String getUrlPart() {
 		return urlPart;
 	}
 	public boolean isFirst() {
 		return isFirst;
 	}
 	public boolean isLatestBuild() {
 		return isLatestBuild;
 	}
 	
 	// ----
 	
 	public String getLatestBuildString() {
     	if (isLatestBuild) {
     		// TODO message
     		return " (Latest Build)";
     	} else {
     		return "";
     	}
 	}
 	public String getFontWeight() {
     	if (isLatestBuild && multipleBuilds) {
     		return "bold";
     	} else {
     		return "normal";
     	}
 	}
 	public void setFirst(boolean first) {
 		this.isFirst = first;
 	}
 	public void setMultipleBuilds(boolean multipleBuilds) {
 		this.multipleBuilds = multipleBuilds;
 	}
 	/**
 	 * Sort by build number.
 	 */
 	public int compareTo(BuildInfo that) {
 		return new Integer(that.run.number).compareTo(this.run.number);
 	}
 }
