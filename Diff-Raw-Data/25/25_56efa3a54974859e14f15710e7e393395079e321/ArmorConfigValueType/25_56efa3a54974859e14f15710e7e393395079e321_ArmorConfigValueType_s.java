 package com.glen3b.plugin.invpotions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public enum ArmorConfigValueType {
 	Acknowledge,
 	Ignore,
 	NonStandard;
 	
 	public static ArmorConfigValueType getType(String configvalue){
 		if(configvalue != null){
 		if(configvalue.replaceAll("\n", "").replaceAll("\r", "") == "Wool"){
 			return ArmorConfigValueType.NonStandard;
		}else if(configvalue.replaceAll("\n", "").replaceAll("\r", "") == "Ignore"){
 			return ArmorConfigValueType.Ignore;
 		}
 		return ArmorConfigValueType.Acknowledge;
 		}
 		return ArmorConfigValueType.Ignore;
 	}
 	
 	public static List<ArmorConfigValueType> populateList(String[] configvalues){
 		List<ArmorConfigValueType> acvt = new ArrayList<ArmorConfigValueType>();
 		for(int i = 0; i < configvalues.length; i++){
 			acvt.add(getType(configvalues[i]));
 		}
 		return acvt;
 	}
 }
