 package churndb.couch;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.gson.Gson;
 
 public class CouchUtils {
 
 	public static String keys(String... keys) {
		/*
 		if (keys.length == 1) {
 			// TODO not working for some cases
 			return "key=" + urlEncode(new Gson().toJson(keys[0]));
		}*/
 		
		// FIXME to work with viewDelete a view need to export its key as an array		
 		return keysForRange(keys);
 	}
 
 	private static String keysForRange(String... keys) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("startkey=");
 		
 		List<String> keysList = new ArrayList<String>(Arrays.asList(keys));
 		
 		sb.append(urlEncode(new Gson().toJson(keysList)));
 				
 		keysList.add("{}");
 		sb.append("&endkey=");
 		sb.append(urlEncode(new Gson().toJson(keysList)));
 				
 		return sb.toString();
 	}
 	
 	private static String urlEncode(String key) {
 		try {
 			return URLEncoder.encode(key, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
