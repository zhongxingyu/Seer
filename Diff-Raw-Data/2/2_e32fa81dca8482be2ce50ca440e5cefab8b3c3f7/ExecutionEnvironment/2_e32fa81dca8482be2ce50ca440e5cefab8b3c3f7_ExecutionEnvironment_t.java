 package com.epms.common.type;
 
 import java.util.EnumSet;
 import java.util.LinkedList;
 import java.util.List;
 
 public enum ExecutionEnvironment {
 	Local(new String[] {"local"}, "Local Developer Environment", "LOCAL"),
 	Development1(new String[] {"dev1"}, "Long-term path Development Environment", "DEV1"),
 	SystemTest(new String[] {"syst", "systest"}, "System Testing Environment", "SYSTEST"),
 	Development(new String[] {"dev"}, "Short-term path Development Environment", "DEV"),
 	Integration(new String[] {"int"}, "Integration Environment", "INT"),
 	UAT(new String[] {"uat"}, "User Acceptance Testing Environment", "UAT"),
 	Production(new String[] {"prod"}, "Production Environment", "PRODUCTION"),
	All(new String[] {"all"}, "All Environments", "ALL"),
 	Unknown(new String[] {""}, "Unknown", "Unknown");
 
 	private static EnumSet<ExecutionEnvironment> allEnums = EnumSet.allOf(ExecutionEnvironment.class);
 	
 	private final List<String> lookupValues;
 	private final String description;
 	private final String jsonValue;
 	
 	private ExecutionEnvironment(String[] lookupText, String description, String jsonValue) {
 		lookupValues = new LinkedList<String>();
 		for (int i = 0; i < lookupText.length; i++) {
 			lookupValues.add(lookupText[i].toLowerCase());
 		}
 		this.description = description;
 		this.jsonValue = jsonValue;
 	}
 
 	public static ExecutionEnvironment findByLookupText(String text) {
 		for (ExecutionEnvironment e : allEnums) {
 			if (e.matches(text)) {
 				return e;
 			}
 		}
 		return Unknown;
 	}
 
 	public static ExecutionEnvironment findByJSON(String json) {
 		for (ExecutionEnvironment e : allEnums) {
 			if (e.jsonValue.equals(json)) {
 				return e;
 			}
 		}
 		return Unknown;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getJSONValue() {
 		return jsonValue;
 	}
 
 	private boolean matches(String lookupText) {
 		if (lookupValues.contains(lookupText.toLowerCase())) return true;
 		return false;
 	}
 }
