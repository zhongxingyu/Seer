 package edu.uw.cs.lil.tiny.tempeval;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public abstract class TemporalISO {
 	private final String[] fields = { "year", "quarter", "season", "month", "week", "day", "hour",
 			"minute", "weekday", "present_ref", "past_ref", "future_ref"};
 	private final Map<String, Set<Integer>> value;
 	private final boolean convexSet;
 
 	public TemporalISO(Map<String, Set<Integer>> data) {
		if (data.size() == 0)
			throw new IllegalArgumentException("Trying to create a TemporalISO from an empty map!");
 		convexSet = false;
 		for (String field : data.keySet()){
 			if (!stringInFields(field))
 				throw new IllegalArgumentException(field
 						+ " is not a valid key! Problem in TemporalISO.");
 		}
 		this.value = new HashMap<String, Set<Integer>>();
 		for (String s : data.keySet()) {
 			Set<Integer> tmpSet = new HashSet<Integer>();
 			tmpSet.addAll(data.get(s));
 			this.value.put(s, tmpSet);
 		}
 		
 	}
 	
 	// This constructor is for convex sets, like the set of all months, or the set of all quarters. 
 	public TemporalISO(String field){
 		this(field,-1, true);
 	}
 
 	public TemporalISO(String field, int num) {
 		this(field, num, false);
 	}
 	
 	public TemporalISO(String field, int num, boolean convex){
 		convexSet = convex;
 		if (!stringInFields(field))
 			throw new IllegalArgumentException(field
 					+ " is not a valid key! Problem in TemporalISO.");
 		this.value = new HashMap<String, Set<Integer>>();
 		Set<Integer> newValueSet = new HashSet<Integer>();
 		newValueSet.add(Integer.valueOf(num));
 		this.value.put(field, newValueSet);
 	}
 	
 	public Map<String, Set<Integer>> getFullMapping() {
 		Map<String, Set<Integer>> tmpMap = new HashMap<String, Set<Integer>>();
 		for (String s : this.getKeys()) {
 			tmpMap.put(s, new HashSet<Integer>());
 			(tmpMap.get(s)).addAll(this.getVal(s));
 		}
 		return tmpMap;
 	}
 
 	public Set<String> getKeys() {
 		Set<String> tmp = new HashSet<String>();
 		tmp.addAll(this.value.keySet());
 		return tmp;
 	}
 
 	public Set<Integer> getVal(String key) {
 		if (isSet(key)) {
 			Set<Integer> tmp = new HashSet<Integer>();
 			tmp.addAll(this.value.get(key));
 			return tmp;
 		}
 		if (stringInFields(key)) {
 			throw new IllegalArgumentException("Value for " + key
 					+ " has not been set yet in TemporalISO.");
 		}
 		throw new IllegalArgumentException(key + " is not a valid key!");
 	}
 
 	public boolean isSet(String key) {
 		return this.value.containsKey(key);
 	}
 
 	public boolean stringInFields(String f) {
 		boolean keyInFields = false;
 		for (int i = 0; i < this.fields.length; i++) {
 			if (this.fields[i].equals(f)) {
 				keyInFields = true;
 			}
 		}
 		return keyInFields;
 	}
 	
 	public boolean isConvexSet(){
 		return convexSet;
 	}
 	
 	// Sort of a hack, doesn't work if there is more than one value for a given
 	// field!
 	public static int getValueFromDate(TemporalISO d, String s) {
 		//int value = -2;
 		int value = Integer.MAX_VALUE;
 		if (d.getVal(s).size() > 1) {
 			throw new IllegalArgumentException(
 					"There is more than one value for " + s
 							+ " and that's not implemented.");
 		}
 		for (int i : d.getVal(s)) {
 			value = i;
 		}
 		
 		// Not sure this is a necessary check. 
 		if (value == Integer.MAX_VALUE) {
 			throw new IllegalArgumentException(
 					"Problem getting value of " + s + " in getValueFromDate for ISO " + d + ", within TemporalISO");
 		}
 		return value;
 	}
 	
 	public abstract String getType();
 	
 	public abstract String getVal();
 	
 	public abstract boolean isFullySpecified();
 }
