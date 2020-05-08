 package com.razie.sdk.util;
 
 import java.util.Collection;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
import razie.assets.AssetBrief;
import razie.assets.AssetBrief$;
import razie.assets.AssetMap;

 import com.razie.pub.draw.DrawList;
 
 /**
  * utility to serialize from/into different formats
  * 
  * @author razvanc
  * @version $Id$
  * 
  */
 public class Serializer {
 
    public static String assetsToJson(AssetMap briefs) throws JSONException {
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
 
    public static AssetMap assetsFromJson(String json) throws JSONException {
         JSONArray read = new JSONArray(json);
        AssetMap newbriefs = new AssetMap();
         for (int i = 0; i < read.length(); i++) {
            AssetBrief b = AssetBrief$.MODULE$.fromJson(read.getJSONObject(i));
             newbriefs.put(b.getKey(), b);
         }
 
         return newbriefs;
     }
 
 }
