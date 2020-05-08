 package com.github.r1j0.bugspot;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.github.r1j0.bugspot.repository.LogEntries;
 
 public class Computate {
 
	private final Pattern fixesPattern = Pattern.compile("fix(es|ed)?|close(s|d)?", Pattern.CASE_INSENSITIVE);
	private final Pattern ignorePattern = Pattern.compile(".*\\.txt|.*\\.xml", Pattern.CASE_INSENSITIVE);
 	private final List<LogEntries> logEntries;
 	private Map<String, Double> hotspots = new HashMap<String, Double>();
 
 
 	public Computate(List<LogEntries> logEntries) {
 		this.logEntries = logEntries;
 	}
 
 
 	public void compute() {
 		List<LogEntries> fixes = extractFixes();
 		LogEntries lastEntry = fixes.get(fixes.size() - 1);
 
 		for (LogEntries logEntries : fixes) {
 			Map<String, String> logPath = logEntries.getLogPath();
 
 			for (Entry<String, String> entrySet : logPath.entrySet()) {
 				String fullPath = entrySet.getValue();
 
				Matcher ignoreMatcher = ignorePattern.matcher(fullPath);

				if (ignoreMatcher.matches()) {
					continue;
				}

 				Double bugSportValue = calculateBugSpot(lastEntry, logEntries, fullPath);
 				hotspots.put(fullPath, bugSportValue);
 			}
 		}
 	}
 
 
 	public Map<String, Double> getHotspots() {
 		return hotspots;
 	}
 
 
 	private List<LogEntries> extractFixes() {
 		final List<LogEntries> fixes = new ArrayList<LogEntries>();
 
 		for (LogEntries logEntry : logEntries) {
			Matcher fixesMatcher = fixesPattern.matcher(logEntry.getMessage());
 
			if (fixesMatcher.find()) {
 				fixes.add(logEntry);
 			}
 		}
 
 		return fixes;
 	}
 
 
 	private Double calculateBugSpot(LogEntries lastEntry, LogEntries logEntries, String fullPath) {
 		float t = 1 - (((System.currentTimeMillis() - logEntries.getDate().getTime()) / (System.currentTimeMillis() - lastEntry.getDate().getTime())) / 1000);
 
 		Double oldBugSpotValue = hotspots.get(fullPath);
 		Double newBugSpotValue;
 
 		if (oldBugSpotValue != null) {
 			newBugSpotValue = oldBugSpotValue + (1 / (1 + Math.exp((-12 * t) + 12)));
 		} else {
 			newBugSpotValue = 1 / (1 + Math.exp((-12 * t) + 12));
 		}
 
 		return newBugSpotValue;
 	}
 }
