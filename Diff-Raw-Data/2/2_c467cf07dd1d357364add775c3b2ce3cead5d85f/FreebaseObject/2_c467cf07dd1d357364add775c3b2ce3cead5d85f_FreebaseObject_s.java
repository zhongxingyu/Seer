 package ru.yandex.semantic_geo.freebase;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Created by IntelliJ IDEA.
  * User: rasifiel
  * Date: 06.10.12
  * Time: 14:38
  */
 public class FreebaseObject {
 
     public final String text;
     public final String mid;
     public final String img_url;
     public final JSONObject attrs;
 
     public FreebaseObject(final String text, final String mid, final String img_url, final JSONObject attrs) {
         this.text = text;
         this.mid = mid;
         this.img_url = img_url;
         this.attrs = attrs;
     }
 
     @Override
     public String toString() {
         try {
             return "FreebaseObject{" +
                     "text='" + text + '\'' +
                     ", mid='" + mid + '\'' +
                     ", img_url='" + img_url + '\'' +
                     ", attrs=" + attrs.toString(1) +
                     '}';
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
     }
 
     public JSONObject toJson() {
         try {
             JSONObject res = new JSONObject().put("text", text).put("mid", mid);
             if (img_url != null) {
                 res.put("image", "http://img.freebase.com/api/trans/image_thumb" + img_url +
                        "?maxheight=200&mode=fit&maxwidth=150,");
             }
             res.put("attrs", attrs);
             FreebaseAPI.mid2url(res);
             return res;
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
 
     }
 }
