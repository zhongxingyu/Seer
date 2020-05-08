 package test;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.eventu.webtier.ApplicationService;
 import com.eventu.webtier.DataService;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class JsonTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		DataService userDB = new DataService("UserProfile");
 		ApplicationService appSer1 = new ApplicationService(userDB);
 		
 		HttpServletRequest request = null;
		String json = "{\"password\":\"cb0769dcb7e8d3f7b30762264e649cd52ad32b5be6298b0bfc54896724353f3f\",\"action\":\"register\",\"userEmail\":\"bobbychen@dsf.sdf\"}";
 		JsonParser jsonParser = new JsonParser();
 		JsonObject jo = (JsonObject)jsonParser.parse(json);
 		
 		JsonObject rtv = appSer1.register(request, jo );
 	}
 
 }
