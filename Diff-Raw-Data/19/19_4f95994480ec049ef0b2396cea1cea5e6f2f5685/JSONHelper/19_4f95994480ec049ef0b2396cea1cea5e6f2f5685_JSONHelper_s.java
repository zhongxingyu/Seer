 package edu.thu.cslab.footwith.messenger;
 
 import edu.thu.cslab.footwith.utility.Util;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.LogManager;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: roselone
  * Date: 3/16/13
  * Time: 1:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class JSONHelper {
     //TODO encode decode
 
     public static final JSONHelper JSONHelperInstance=new JSONHelper();
     private Logger logger=LogManager.getLogger(this.getClass().getName());
 
     /**
      *
      * @return jsonhelper instance
      */
     public static JSONHelper getJSONHelperInstance(){
         return JSONHelperInstance;
     }
 
     public String convertToString(HashMap<String,String> map){
         JSONObject object=new JSONObject(map);
         return object.toString();
     }
 
     public String convertToString2(HashMap<String,byte[]> map){
         JSONObject object=new JSONObject(map);
         return object.toString();
     }
 
 
     public HashMap<String,String> convertToMap(String s) {
         if (Util.isEmpty(s)) return null;
         try {
             JSONObject object=new JSONObject(s);
             Iterator it=  object.keys();
             HashMap<String,String> map= new HashMap<String, String>();
             while (it.hasNext()){
                 String key = (String) it.next();
                 map.put(key,object.optString(key));
             }
             return map;
         } catch (JSONException e) {
             logger.error("JSON ERROR: can't convert to {} to JSONObject",s);
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
     public HashMap<String,byte[]> convertToMap2(String s) {
         if (Util.isEmpty(s)) return null;
         try {
             JSONObject object=new JSONObject(s);
             Iterator it=  object.keys();
             HashMap<String,byte[]> map= new HashMap<String, byte[]>();
             while (it.hasNext()){
                 String key = (String) it.next();
                 map.put(key,object.optString(key).getBytes());
             }
             return map;
         } catch (JSONException e) {
             logger.error("JSON ERROR: can't convert to {} to JSONObject",s);
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
     /**
      * convert vector to json array
      * @param parts
      * @return
      */
     public String convertToString(Vector<Integer> parts){
         JSONArray array=new JSONArray();
         for (int i=0;i<parts.size();i++){
             array.put(parts.get(i));
         }
         return array.toString();
     }
 
     public String convertToString2(Vector<String> parts){
         JSONArray array=new JSONArray();
         for (int i=0;i<parts.size();i++){
            array.put(Util.string2Json(parts.get(i)));
         }
         return array.toString();
     }
 
     /**
      * convert json array to vector
      * @param s             /home/roselone/Git/FootWith/messenger
      * @return vector
      * @throws JSONException
      */
     public Vector<Integer> convertToArray(String s) throws JSONException {
         if (Util.isEmpty(s)) return null;
         JSONArray array=new JSONArray(s);
         Vector<Integer> result=new Vector<Integer>();
         for (int i=0;i<array.length();i++) result.add(array.getInt(i));
         return result;
     }
 
     public Vector<String> convertToArray2(String s) throws JSONException {
         if (Util.isEmpty(s)) return null;
         JSONArray array=new JSONArray(s);
         Vector<String> result=new Vector<String>();
         for (int i=0;i<array.length();i++) result.add(array.getString(i));
         return result;
     }
 
     /**
      * add one int to json array
      * @param s
      * @param one
      * @return json array
      * @throws JSONException
      */
     public String addToArray(String s, int one) throws JSONException {
         Vector<Integer> tmp;
         if (Util.isEmpty(s)) {
             tmp=new Vector<Integer>();
         }else{
             tmp=convertToArray(s);
         }
         tmp.add(one);
         return convertToString(tmp);
     }
 
     /**
      * delete one from json array
      * @param s
      * @param one
      * @return json array
      * @throws JSONException
      */
     public String deleteFromArray(String s, int one) throws JSONException {
         if (Util.isEmpty(s)) return null;
         Vector<Integer> tmp=convertToArray(s);
         tmp.remove(tmp.indexOf(one));
         return convertToString(tmp);
     }
 
     /**
      * whether json array contains the one
      * @param s
      * @param one
      * @return isContained
      * @throws JSONException
      */
     public boolean isContained(String s,int one) throws JSONException {
         if (Util.isEmpty(s)) return false;
         Vector<Integer> tmp=convertToArray(s);
         return tmp.contains(one);
     }
 
 }
