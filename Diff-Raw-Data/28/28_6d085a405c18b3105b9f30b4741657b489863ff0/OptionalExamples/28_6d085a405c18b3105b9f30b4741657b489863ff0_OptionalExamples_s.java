 package com.jaxjug.guava;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.common.base.Optional;
 
 public class OptionalExamples {
 
 	private Map<String, String> map = new HashMap<String, String>();
 	
 	public OptionalExamples() { 
 		map.put("Bill", "Carlson");
 		map.put("Muthu", "Ramasamy");
 	}
 	
 	public void javaWay() {
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("Bill", "Carlson");
 		map.put("Muthu", "Ramasamy");
 		 
 		@SuppressWarnings("unused")
 		String bill = map.get("Bill").toUpperCase(); // Works great
 		@SuppressWarnings("unused")
 		String eyal = map.get("Eyal").toUpperCase(); // Uh oh!!
 			
 		String eyal2 = map.get("Eyal"); 
 		if (eyal2 != null) {
 			eyal = eyal2.toUpperCase();
 		}
 	}
 	
 	public void guavaWay() { 
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("Bill", "Carlson");
 		map.put("Muthu", "Ramasamy");
 
 		Optional<String> billOpt = Optional.fromNullable(map.get("Bill"));
 		if (billOpt.isPresent()) {
 			@SuppressWarnings("unused")
 			String bill  = billOpt.get();
 		}
 		
 		@SuppressWarnings("unused")
 		String eyal = Optional.fromNullable(map.get("Eyal")).or("Not Found").toUpperCase();
 	}
 }
