 package models;
 
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 import play.Logger;
 
 import java.io.IOException;
 
 public class GcmService {
 
    private static final String API_KEY = "AIzaSyD788SjyS_FByYToA1DmGxs7Od9W79khwI";
 
     public static void sendMessage(String regId, String adId) throws IOException {
         Sender sender = new Sender(API_KEY);
         Message message = new Message.Builder().addData("adId", adId).build();
         Result result = sender.send(message, regId, 3);
         Logger.info(result.toString());
     }
 
 }
