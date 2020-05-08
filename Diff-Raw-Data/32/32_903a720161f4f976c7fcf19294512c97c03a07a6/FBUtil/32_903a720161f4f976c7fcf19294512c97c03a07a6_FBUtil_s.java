 package com.lopefied.pepemon.util;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import org.json.JSONException;
 
 import android.os.Bundle;
 import android.util.Log;
 
 import com.facebook.android.Util;
 
 /**
  * 
  * @author Lope Chupijay Emano
  * 
  */
 public class FBUtil {
     public static final String TAG = FBUtil.class.getSimpleName();
 
     public static String generateImageURL(String imageID, String accessToken) {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("https://graph.facebook.com/");
         stringBuilder.append(imageID);
         stringBuilder.append("/picture?type=normal&method=GET&access_token=");
         stringBuilder.append(accessToken);
         String returnURL = stringBuilder.toString();
         return returnURL;
     }
 
     public static String extractImageURLFromPID(String pid, String accessToken) {
         String returnImageURL = null;
         String result = "";
         String query = "SELECT pid, src_big, images FROM photo WHERE pid=\""
                 + pid + "\"";
         Bundle b = new Bundle();
         b.putString("access_token", accessToken);
         b.putString("q", query);
         try {
             result = Util.openUrl("https://graph.facebook.com/fql", "GET", b);
            Log.i("FQL Loader", result);
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         try {
             returnImageURL = FBParseUtils.extractURL(result);
             Log.i(TAG, "extracted url : " + returnImageURL);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return returnImageURL;
     }
 }
