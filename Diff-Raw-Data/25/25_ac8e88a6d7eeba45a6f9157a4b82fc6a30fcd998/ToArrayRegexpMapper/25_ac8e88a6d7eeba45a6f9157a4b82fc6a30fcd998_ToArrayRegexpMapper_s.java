 package com.mpages.parsing.crawler;
 
 import java.util.regex.Matcher;
 
 public class ToArrayRegexpMapper extends RegexpDataMapper<String[]>{
 	
 	public ToArrayRegexpMapper(String dataRegexp) {
 		this.dataRegexp = dataRegexp;
 	}
 	
 	protected String[] mapMatch(Matcher matcher) {
 		String[] result = new String[matcher.groupCount()];
		for (int i = 0; i < matcher.groupCount() - 1; i++) {
 			result[i] = matcher.group(i + 1);
 		}
 		return result;
 	}
 }
