 //
 // Copyright 2011 MERS Technologies.
 //
 
 import com.subuno.SUBUNOAPI;
 import com.subuno.SUBUNOAPIError;
 
 import java.util.HashMap;
 
 import org.json.JSONObject;
 import org.json.JSONException;
 
 
 public class Example1 {
 
 	public static void main(String[] args) {
 
 		HashMap<String, String> data = new HashMap<String, String>();
 
 		data.put("t_id"            , "7d3n89wn" );
 		data.put("ip_addr"         , "24.24.24.24");
 		data.put("customer_name"   , "John Doe");
 		data.put("phone"           , "212-456-7890");
 		data.put("email"           , "john.doe@domain.com");
 		data.put("company"         , "Doe LLC");
 		data.put("price"           , "50.0");
 		data.put("bin"             , "480128");
 
 		data.put("bill_street1"    , "12 East 71th St");
 		data.put("bill_street2"    , "#12");
 		data.put("bill_city"       , "New York");
 		data.put("bill_state"      , "NY");
 		data.put("bill_country"    , "US" );
 		data.put("bill_zip"        , "10021");
 
 		data.put("ship_street1"    , "12 East 71th St");
 		data.put("ship_street2"    , "#12");
 		data.put("ship_city"       , "New York");
 		data.put("ship_state"      , "NY");
 		data.put("ship_country"    , "US");
 		data.put("ship_zip"        , "10021");
 
 		try {
 			JSONObject result = new SUBUNOAPI().run(
 				// apikey
 					"2g4g747g843",
 				// data
 					data
 			);
 
 			// result is a org.json.JSONObject with keys/value pairs with data returned by api.
 			System.out.println(result);
 						
 		} catch (SUBUNOAPIError e) {
 			e.printStackTrace();
 		}
 	}
 
 }
