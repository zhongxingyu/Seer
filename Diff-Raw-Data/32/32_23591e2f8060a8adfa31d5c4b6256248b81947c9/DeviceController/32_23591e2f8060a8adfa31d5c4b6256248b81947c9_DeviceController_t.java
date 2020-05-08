 package controllers;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import models.Device;
 import models.Notification;
 import org.apache.commons.lang3.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.*;
 import play.data.Form;
 import play.mvc.*;
 import utils.GCMGateway;
 import views.html.*;
 
 import java.io.IOException;
 import java.util.*;
 
 public class DeviceController extends Controller {
     private static transient Map<String, Device> registeredDevices = new HashMap<String, Device>();
     private static Logger logger = LoggerFactory.getLogger("DeviceController");
     private static Form<Notification> messageForm = Form.form(Notification.class);
     private static GCMGateway gcmGateway = new GCMGateway();
 
     public static Result register() {
         Map<String, String[]> params = request().body().asFormUrlEncoded();
 
         if (params == null) return badRequest();
 
         String[] deviceIds = params.containsKey("deviceId") ? params.get("deviceId") : new String[0];
         String[] registrationIds = params.containsKey("registrationId") ? params.get("registrationId") : new String[0];
 
         if (!ArrayUtils.isEmpty(deviceIds) && !ArrayUtils.isEmpty(registrationIds)) {
             String deviceId = deviceIds[0];
             String registartionId = registrationIds[0];
 
              logger.info("Adding device: [" + deviceId+ "]");
 
             registeredDevices.put(deviceId, new Device(deviceId, registartionId));
 
             return redirect(routes.DeviceController.devices());
         } else {
             return badRequest();
         }
     }
 
     public static Result devices() {
         logger.info("Listing all devices");
         return ok(devices.render(Lists.newArrayList(registeredDevices.values())));
     }
 
    public static Result showSendForm() {
         return ok(sendMessage.render(messageForm, ""));
     }
 
     public static Result send() throws IOException {
         Form<Notification> sentForm = messageForm.bindFromRequest();
 
         if (sentForm.hasErrors()) {
             return badRequest(sendMessage.render(sentForm, ""));
         }
 
         gcmGateway.sendMessage(registeredDevices.values(), sentForm.get());
         return ok(sendMessage.render(messageForm,"Message sent!"));
     }
 
     @VisibleForTesting
     static Map<String, Device> getDevices() {
         return registeredDevices;
     }
 }
