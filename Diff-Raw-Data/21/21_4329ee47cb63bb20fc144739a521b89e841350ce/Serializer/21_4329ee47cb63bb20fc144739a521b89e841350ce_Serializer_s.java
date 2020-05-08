 package com.razie.sdk.util;
 
 import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
import com.razie.pub.assets.AssetBrief;
import com.razie.pub.assets.AssetKey;
 import com.razie.pub.draw.DrawList;
 
 /**
  * utility to serialize from/into different formats
  * 
  * @author razvanc
  * @version $Id$
  * 
  */
 public class Serializer {
 
    public static String assetsToJson(Map<AssetKey, AssetBrief> briefs) throws JSONException {
         JSONArray write = new JSONArray(briefs.values());
         String json = write.toString(2);
         return json;
     }
 
     public String assetsToJson(Collection<AssetBrief> briefs) throws JSONException {
         JSONArray write = new JSONArray(briefs);
         String json = write.toString(2);
         return json;
     }
 
     public String assetsToJson(DrawList briefs) throws JSONException {
         JSONArray write = new JSONArray(briefs.getList());
         String json = write.toString(2);
         return json;
     }
 
    public static Map<AssetKey, AssetBrief> assetsFromJson(String json) throws JSONException {
         JSONArray read = new JSONArray(json);
        Map<AssetKey, AssetBrief> newbriefs = new HashMap<AssetKey, AssetBrief>();
         for (int i = 0; i < read.length(); i++) {
            AssetBrief b = AssetBrief.fromJson(read.getJSONObject(i));
             newbriefs.put(b.getKey(), b);
         }
 
         return newbriefs;
     }
 
 }
