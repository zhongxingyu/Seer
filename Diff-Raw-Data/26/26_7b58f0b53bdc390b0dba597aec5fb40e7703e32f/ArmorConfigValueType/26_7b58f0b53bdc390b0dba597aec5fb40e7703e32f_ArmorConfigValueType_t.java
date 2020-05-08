 package com.glen3b.plugin.invpotions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public enum ArmorConfigValueType {
 	Acknowledge,
 	Ignore,
 	NonStandard;
 	
 	public static ArmorConfigValueType getType(String configvalue){
		if(configvalue.replaceAll("\n", "").replaceAll("\r", "").equalsIgnoreCase("Wool")){
 			return ArmorConfigValueType.NonStandard;
		}else if(configvalue.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "").equalsIgnoreCase("Ignore")){
 			return ArmorConfigValueType.Ignore;
 		}
 		return ArmorConfigValueType.Acknowledge;
 	}
 	
 	public static List<ArmorConfigValueType> populateList(String[] configvalues){
 		List<ArmorConfigValueType> acvt = new ArrayList<ArmorConfigValueType>();
		for(String cfgvalue : configvalues){
			acvt.add(getType(cfgvalue));
 		}
 		return acvt;
 	}
 }
