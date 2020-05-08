 package be.artesis.timelog.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Connection {
         
    private static final String server = "http://chronomatic-artesisti.rhcloud.com/ChronomaticServer/";
   // private static final String server = "http://localhost:8080/ChronomaticServer/";
     
     public static URL normalize(String commando) throws MalformedURLException, UnsupportedEncodingException {
         return new URL((server + commando).replaceAll(" ", "%20"));
     }
 
 	public static boolean execute(String commando) throws IOException, WebserviceException{
             URL url = normalize(commando);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setRequestProperty("Accept", "application/json");
             if (conn.getResponseCode() != 200) {
                 throw new WebserviceException("Failed : HTTP error code : " + conn.getResponseCode());
             }
             return true;
 	}
 	
 	public static JSONObject getObject(String commando) throws IOException, JSONException{
             JSONObject jObject = null;
             URL url = normalize(commando);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setRequestProperty("Accept", "application/json");
             //System.out.println(url);
             if (conn.getResponseCode() != 200){
                 throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
             }
             BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
             JSONArray jArray = new JSONArray(br.readLine());
             for(int i=0;i<jArray.length();i++)
             {
                 jObject = jArray.getJSONObject(i);
             }				 
             return jObject;
 	}
         public static JSONArray getArray(String commando) throws IOException, JSONException{		
             JSONArray Jarray = null;
             URL url = normalize(commando);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setRequestProperty("Accept", "application/json");
             if (conn.getResponseCode() != 200){
                 throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
             }
             BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
             Jarray = new JSONArray(br.readLine());
             return Jarray;
 	}
 }
