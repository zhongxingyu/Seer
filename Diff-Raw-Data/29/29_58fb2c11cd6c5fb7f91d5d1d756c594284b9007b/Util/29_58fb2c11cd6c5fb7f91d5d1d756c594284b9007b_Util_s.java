 package tira;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Defines the global constants used in TIRA.
  * Provides a couple of convenient functions for "merging" pairs of JSONObjects in different ways.
  * Provides a couple of convenient functions for resolving $-placeholders in strings.
  * @author tim
  *
  */
 public class Util {
     
     /*
     "NODE": "http://localhost:2306/",
     "DATABASE" : "$NODE",    
     "WORKER": 1,
     
     "DATAROOT" : "data",
     "PROGRAMROOT" : "programs",
     "DATA" : "$DATAROOT/$PNAME"
     "PROGRAM" : "$PROGRAMROOT/$PNAME",  
       
     "RECORD" : "record.json",
     "INFO" : "$BIN/info.html",
     "UI": "ui"
      */
     public static final String NODE = "NODE";
     public static final String DATABASE = "DATABASE";
     public static final String WORKER = "WORKER";
     
     public static final String DATAROOT = "DATAROOT";
     public static final String DATA = "DATA";
     public static final String PROGRAMROOT = "PROGRAMROOT";    
     public static final String PROGRAM = "PROGRAM";
     public static final String PNAME = "PNAME";
     
     public static final String RECORD = "RECORD";
     public static final String INFO = "INFO";
     public static final String CONFIG = "CONFIG";
     public static final String RESULTS = "RESULTS";
     public static final String UI = "UI";
     public static final String SCRIPT = "SCRIPT"; 
       
     public static final String MAIN = "MAIN";
     public static final String SYSTEM = "SYSTEM";
 	public static final String LASTMOD = "LASTMOD";
     public static final String ID = "ID";
     public static final String DEFAULT = "default";
     public static final String VALUE = "value";
     
     public static final String STATE = "STATE";
     public static final String TODO = "TODO";
     public static final String ERROR = "ERROR";
     public static final String RUNNING = "RUNNING";
     public static final String DONE = "DONE";
     
     /*public static final String POISSONPILL = "POISSONPILL";*/
 
     
     public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
     
     @SuppressWarnings("serial")
     private final static Map<String,String> MIME_TYPES = new HashMap<String,String>()
     {{
         put("json","application/json");
         put("js","application/javascript");
         put("xml","application/xml");
         put("xsl","application/xml");
         put("css","text/css");
         put("html","text/html");
         put("htm","text/html");
         put("rss","application/xml");
         put("png","image/png");
         put("gif","image/gif");
         put("ico","image/x-icon");
         put("","text/plain");
     }};
     
     public static String guessType(String filename) {
         String extension = "";
         if(filename.contains(".") && !filename.endsWith("."))
         {
             extension = filename.substring(filename.lastIndexOf(".")+1);
         }
         if(MIME_TYPES.containsKey(extension)) {
             return MIME_TYPES.get(extension.toLowerCase());
         }
         return "text/plain";
     }
 	
 	/***
 	 * Transforms any given file into a String.
 	 * 
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
     public static String fileToString(File file) throws IOException
     {
         if(file.length()==0){return "";}
         FileReader in = new FileReader(file);
         BufferedReader bin = new BufferedReader(in);
         StringBuilder sb = new StringBuilder();
         char[] cbuf = new char[(int)file.length()];
         while(bin.read(cbuf) != -1) {
              sb.append(cbuf);
         }
         return sb.toString();
     }
     
     public static String substitute(String cmd, JSONObject config) throws JSONException
     {
         String key = Util.findMatch(cmd,config);
         if(key==null){return cmd;}
         cmd = cmd.replace("$"+key, config.getString(key));
         return substitute(cmd,config);
     }
     
    //if more than one key matches the same place-holder take longer key.
     public static String findMatch(String cmd, JSONObject config) {
         if(!cmd.contains("$")){return null;}
         String matchingKey = null; int maxLength=0;
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = config.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             if(cmd.contains("$"+key) && key.length()>maxLength)
             {
                 maxLength = key.length(); matchingKey = key;
             }
         }
         return matchingKey;
     }
 
     public static List<String> findAllMatches(String cmd, JSONObject config) {
         List<String> matches = new LinkedList<String>();
         if(!cmd.contains("$")){return matches;}
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = config.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             if(cmd.contains("$"+key)){matches.add(key);}
         }
         return matches;
     }
     
     public static JSONObject augment(JSONObject base, JSONObject supplement) throws JSONException {
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = supplement.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             if(!base.has(key)){base.put(key, supplement.get(key)); }
         }
         return base;
     }
     
     public static JSONObject augmentDeep(JSONObject base, JSONObject supplement) throws JSONException {
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = supplement.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             if(!base.has(key)){base.put(key, supplement.get(key)); }
             else if(base.optJSONObject(key)!=null)
             {
                 base.put(key, augmentDeep(base.getJSONObject(key),supplement.getJSONObject(key)));
             }
         }
         return base;
     }
 
     public static JSONObject override(JSONObject base, JSONObject update) throws JSONException {
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = update.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             base.put(key, update.get(key));
         }
         return base;
     }
     
     public static JSONObject notIn(JSONObject filter, JSONObject base) throws JSONException
     {
         JSONObject result = new JSONObject();
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = base.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             if(!filter.has(key)){result.put(key, base.get(key)); }
         }
         return result;
     }
 
     public static JSONObject queryParamsToJSON(
             MultivaluedMap<String, String> queryParams) throws JSONException {
         Set<String> keys = queryParams.keySet();
         JSONObject config = new JSONObject();
         for(String key: keys)
         {
            JSONArray array = new JSONArray(queryParams.get(key));
             if(array.length()>1){config.put(key, array);}
             //if(!array.getString(0).equals("")){config.put(key, array.getString(0));}
             else{config.put(key, array.getString(0));}
         }
         return config;
     }
 
     public static List<JSONObject> decompose(JSONObject runConfig, JSONObject programConfig) throws JSONException {
         Util.augment(runConfig, programConfig);
         List<JSONObject> runs = new LinkedList<JSONObject>();
         JSONObject start = new JSONObject().put(Util.MAIN, runConfig.get(Util.MAIN));        
         runs.add(start);
         Util.resolveElements(Arrays.asList(Util.MAIN),start,runConfig,runs);
         return runs;
     }
     
     private static void resolveElements(List<String> keys, JSONObject run, JSONObject config, List<JSONObject> runs) throws JSONException
     {
         for(String key : keys)
         {
             if(run.get(key) instanceof String) 
             {
                 String value = run.getString(key);
                 List<String> nextKeys = Util.findAllMatches(value,config);
                 for(String nextKey : nextKeys)
                 {   
                     if(!run.has(nextKey)){run.put(nextKey, config.get(nextKey));}
                 }
                 resolveElements(nextKeys,run,config,runs);
             }
             else //assume JSONArray
             {
                 JSONArray array = run.getJSONArray(key);            
                 String firstValue = array.getString(0);
                 if(array.length()>1)
                 {
                     JSONObject rest = new JSONObject(run.toString());
                     rest.getJSONArray(key).remove(0);
                     runs.add(rest);              
                     resolveElements(keys,rest,config,runs);
                 }            
                 run.put(key, firstValue);
                 resolveElements(keys,run,config,runs);
             }        
         }
     }    
 
 }
