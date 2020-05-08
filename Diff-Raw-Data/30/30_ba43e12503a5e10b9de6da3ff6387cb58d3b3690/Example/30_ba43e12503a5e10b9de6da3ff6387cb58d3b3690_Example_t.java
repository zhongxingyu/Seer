 import net.dmcloud.cloudkey.*;
 import net.dmcloud.util.*;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 
 public class Example {
 
     private static String user_id = "4c1a4d3edede832bfd000002";
     private static String api_key = "52a32c7890338770e3ea1601214c02142d297298";
     private static String base_url = "http://sebest.api.dev.int.dmcloud.net";
 
 
     public static void mediaList() {
         try {
             CloudKey cloud = new CloudKey(user_id, api_key, base_url, CloudKey.CDN_URL, "");
             DCObject result = cloud.call
                 (
                  "media.list",
                  DCObject.create()
                  .push("fields", DCArray.create()
                        .push("id")
                        .push("meta.title")
                        .push("assets.jpeg_thumbnail_auto.stream_url")
                        .push("assets.mp4_h264_aac.video_width")
                        .push("assets.mp4_h264_aac.video_height")
                        .push("assets.source.download_url")
                        )
                  );
 
             DCArray list = DCArray.create((ArrayList)result.get("list"));
             for(int i=0; i<list.size(); i++)
                 {
                     DCObject item = DCObject.create((Map)list.get(i));
                     System.out.println("-----------------------------------");
                     System.out.println("Title : " + item.pull("meta.title"));
                     System.out.println("Thumbnail URL: " + item.pull("assets.jpeg_thumbnail_auto.stream_url"));
                     String embed_url = cloud.mediaGetEmbedUrl(item.get("id").toString(), CloudKey.SECLEVEL_DELEGATE | CloudKey.SECLEVEL_ASNUM | CloudKey.SECLEVEL_USERAGENT, "", "", "", null, null, 0);
                     System.out.println("Embed " + item.pull("assets.mp4_h264_aac.video_width") + "x" + item.pull("assets.mp4_h264_aac.video_height") + " :" + embed_url);
                     String stream_url = cloud.mediaGetStreamUrl(item.get("id").toString(), "mp4_h264_aac", CloudKey.SECLEVEL_NONE, "", "", "", null, null, 0, "", false);
                     String dl_url = item.pull("assets.source.download_url");
                     System.out.println("Download source :" + dl_url);
                     System.out.println("Stream url :" + stream_url);
                 }
         } catch(Exception e) {
             System.out.println("Error : " + e.getMessage());
         }
     }
 
     public static void testToken() {
         FAToken token1 = new FAToken(user_id, "111111111111111111111111", api_key, 1338947483);
        token1.setCallbackUrl("http://www.dmcloud.com/fa2");
        token1.setMeta("firstname", "john");
        token1.setMeta("lastname", "doe");
         token1.setRight("start_date", "1327946483");
         token1.setRight("end_date", "1338947483");
         token1.setRight("playback_window", "600");
         System.out.println("user id " + token1.getUserId());
         System.out.println("Expired? " + token1.isExpired());
         System.out.println(token1.getUserId());
         try {
             System.out.println(token1.toJSON());
             System.out.println(token1.toBase64());
             FAToken token2 = new FAToken(token1.toJSON(), api_key);
             System.out.println("2 " + token2.getUserId());
             FAToken token3 = new FAToken(token1.toBase64(), api_key);
             System.out.println("3 " + token3.getUserId());
             token3.setExpires(10);
             System.out.println("Expired? " + token3.isExpired());
             HashMap<String,String> map = token3.getRights();
             if (map != null ) {
                 for (Map.Entry<String,String> entry : map.entrySet()) {
                     System.out.println("Right " + entry.getKey() + " -> " + entry.getValue());
                 }
             }
         } catch(Exception e) {
            e.printStackTrace();
             System.out.println("oups");
             System.out.println(e);
         }
     }
 
     public static void getApiKey() {
         String ck_user_id = "4c1a4d3edede832bfd000000";
         String ck_api_key = "59f7d6fca9080ec7b59b83eab00e104e3d3d2789";
         CloudKey cloudkey = new CloudKey(ck_user_id, ck_api_key, base_url, CloudKey.CDN_URL, "");
 
         DCObject ck_obj = DCObject.create()
             .push("id", user_id)
             .push("fields", DCArray.create()
                   .push("id")
                   .push("api_key")
                   .push("streaming_active")
                   .push("is_active")
                   .push("permissions")
                   );
         try {
             DCObject result = cloudkey.call("user.info", ck_obj);
             System.out.println(result.get("api_key"));
         } catch (DCException e) {
             System.out.println(e);
         }
    }
 
     public static void main(String[] args) {
         //mediaList();
        testToken();
        //getApiKey();
     }
 
 }
