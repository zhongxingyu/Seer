 package helper;
 
 public class CrowdHelper {
 
 
 	public int getInt(String field, String value, boolean allowDefault, int defValue) throws Exception {
 		if (value==null || value.length()==0) {
 			if (allowDefault)
 				return defValue;
			else throw new Exception(field+" cannot be blank");
 		}
 		try {
 			return Integer.parseInt(value);
 		} catch (NumberFormatException e) {
 			throw new Exception(field+" must be a number");
 		}
 		
 	}
 	
 	public double getDouble(String field, String value, boolean allowDefault, double defValue) throws Exception {
 		if (value==null || value.length()==0) {
 			if (allowDefault)
 				return defValue;
			else throw new Exception(field+" cannot be blank");
 		}
 		try {
 			return Double.parseDouble(value);
 		} catch (NumberFormatException e) {
 			throw new Exception(field+" must be a number");
 		}
 		
 	}
 	
 	public String getValue(String field, String value) throws Exception {
 		if (value==null || value.length()==0) {
 			throw new Exception(field+" cannot be blank");
 		}
 		return value;
 	}
 	
 }
