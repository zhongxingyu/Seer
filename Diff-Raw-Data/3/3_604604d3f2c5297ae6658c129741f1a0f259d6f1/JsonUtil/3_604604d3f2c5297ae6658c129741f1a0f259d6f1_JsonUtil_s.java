 package lab.cgcl.myOCR.util;
 
 import net.sf.json.JSONObject;
 
 public class JsonUtil {
 	public static String jsonUtil ( Object Jstring , String param) {
 		String ret = "";
 		try {
 			JSONObject jsonobj = JSONObject.fromObject(Jstring);
 			ret = jsonobj.get(param).toString();
 		} catch (Exception e) {
			e.printStackTrace();
 		}
 		return ret;
 	}
 }
