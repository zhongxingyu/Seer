 package com.mutation.resolver.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class IncludeFilter {
 
 	private String includePattern;
 
 	private Pattern pattern;
 
 	public String getIncludePattern() {
 		return includePattern;
 	}
 
 	public void setIncludePattern(String excludePattern) {
 		this.includePattern = excludePattern;
 
 		pattern = Pattern.compile(excludePattern);
 	}
 
 	public boolean shouldBeIncluded(String fqClassName) {
 
 		if (includePattern == null || includePattern.trim().equals("")) {
			setIncludePattern("(.*)");
 		}
 
 		Matcher matcher = pattern.matcher(fqClassName);
 
 		return matcher.find();
 	}
 }
