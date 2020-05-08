 package de.hdodenhof.holoreader.backend.gcm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.gcm.server.Constants;
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.MulticastResult;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 
 import de.hdodenhof.holoreader.backend.exception.GCMException;
 import de.hdodenhof.holoreader.backend.persistence.services.UserAndDeviceService;
 
 public class GCMService {
 
     private static final String APIKEY = "";
 
     public void sendMessage(ArrayList<String> receipients, String data) throws GCMException {
         if (receipients.size() > 0) {
             try {
                 Sender sender = new Sender(APIKEY);
 
                 Message.Builder messageBuilder = new Message.Builder();
                 messageBuilder.addData("type", "addfeed");
                 messageBuilder.addData("data", data);
                 Message message = messageBuilder.build();
 
                 MulticastResult multicastResult = sender.send(message, receipients, 5);
 
                 if (multicastResult.getCanonicalIds() != 0 || multicastResult.getFailure() != 0) {
                     UserAndDeviceService userService = new UserAndDeviceService();
 
                     List<Result> results = multicastResult.getResults();
                     for (int i = 0; i < results.size(); i++) {
                         String canonicalRegId = results.get(i).getCanonicalRegistrationId();
                         if (canonicalRegId != null) {
                             // same device has more than on registration ID: update database
                             userService.updateRegId(receipients.get(i), canonicalRegId);
                         }
                         String error = results.get(i).getErrorCodeName();
                         // see http://developer.android.com/reference/com/google/android/gcm/server/Constants.html
                        if (error == Constants.ERROR_NOT_REGISTERED || error == Constants.ERROR_INVALID_REGISTRATION) {
                             userService.removeDevice(receipients.get(i));
                         } else {
                             // TODO
                         }
                     }
                 }
             } catch (Exception e) {
                 throw new GCMException();
             }
         }
     }
 }
