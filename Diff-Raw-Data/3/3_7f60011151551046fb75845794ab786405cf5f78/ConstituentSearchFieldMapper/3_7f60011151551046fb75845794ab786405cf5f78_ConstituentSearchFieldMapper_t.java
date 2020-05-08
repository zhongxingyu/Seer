 package com.orangeleap.tangerine.dao.util.search;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ConstituentSearchFieldMapper extends SearchFieldMapper {
 	
 	public Map<String, String> getMap() {
 		return MAP;
 	}
 	
     // These are the fields we support for searching.
     private static final Map<String, String> MAP = new HashMap<String, String>();
     static {
     	// Constituent
     	MAP.put("id", "CONSTITUENT_ID");
     	MAP.put("accountNumber", "ACCOUNT_NUMBER");
     	MAP.put("firstName", "FIRST_NAME");
     	MAP.put("lastName", "LAST_NAME");
     	MAP.put("organizationName", "ORGANIZATION_NAME");
 
     	// Email
     	MAP.put("email", "EMAIL_ADDRESS");
 
     	// Address
     	MAP.put("addressLine1", "ADDRESS_LINE_1");
     	MAP.put("city", "CITY");
     	MAP.put("stateProvince", "STATE_PROVINCE");
     	MAP.put("postalCode", "POSTAL_CODE");
 
     	// Phone
     	MAP.put("number", "NUMBER");

        MAP.put("byPassDuplicateDetection","BYPASS_DUPLICATE");
     }
     
 }
 
