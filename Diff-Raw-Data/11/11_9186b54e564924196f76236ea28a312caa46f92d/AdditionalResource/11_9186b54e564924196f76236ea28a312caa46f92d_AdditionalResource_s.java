 package de.rallye.model.structures;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class AdditionalResource {
 
 	public static String additionalResourcesToString(List<AdditionalResource> res) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (AdditionalResource r: res) {
 			sb.append(r.toString()).append(',');
 		}
 		sb.deleteCharAt(sb.length());
 		return sb.toString();
 	}
 	
 	public static List<AdditionalResource> additionalResourcesFromString(String in) {
 		List<AdditionalResource> res = new ArrayList<AdditionalResource>();
 		
 		for (String s: in.split(",")) {
 			res.add(AdditionalPicture.fromString(s));
 		}
 		
 		return res;
 	}
 }
