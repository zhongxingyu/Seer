 package com.foxweave.connector.mixpanel;
 
 import com.foxweave.data.component.ConfigUtil;
 import com.foxweave.json.JSONUtil;
 import com.foxweave.pipeline.component.AbstractPipelineComponent;
 import com.foxweave.pipeline.component.ComponentConfigurationException;
 import com.foxweave.pipeline.component.OutputConnector;
 import com.foxweave.pipeline.exchange.Message;
 import com.foxweave.pipeline.lifecycle.Configurable;
 import com.mixpanel.mixpanelapi.MessageBuilder;
 import com.mixpanel.mixpanelapi.MixpanelAPI;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 
 /**
  * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
  */
 public class MixPanelOutputConnector extends AbstractPipelineComponent implements OutputConnector, Configurable<JSONObject> {
 
     private String token;
     private String objectName;
 
     @Override
     public void setConfiguration(JSONObject jsonObject) throws ComponentConfigurationException {
         token = ConfigUtil.getAccountParam("token", jsonObject);
         try {
             objectName = jsonObject.getString("objectName");
         } catch (JSONException e) {
             throw new ComponentConfigurationException("'objectName' not specified.");
         }
     }
 
     @Override
     public Message send(Message message) throws Exception {
         JSONObject payload = message.getPayload(JSONObject.class);
 
         if (objectName.equals("log_activity")) {
             logActivity(payload);
         } else if (objectName.equals("set_profile")) {
             setUserProfile(payload);
         } else if (objectName.equals("delete_profile")) {
             deleteUserProfile(payload);
         }
 
         return message;
     }
 
     protected void logActivity(JSONObject payload) throws JSONException, IOException {
         String eventName = payload.getString("event");
         JSONObject properties = payload.getJSONObject("properties");
 
         // Adjust the time from milliseconds to seconds...
        Long time = properties.optLong("time");
        if (time == null) {
             time = System.currentTimeMillis();
         }
         time = time/1000;
         properties.put("time", time);
 
         MixpanelAPI mixpanel = getMixpanelAPI();
         MessageBuilder messageBuilder = new MessageBuilder(token);
 
         String distinct_id = properties.getString("distinct_id");
         JSONObject mixPanelMessage = messageBuilder.event(distinct_id, eventName, properties);
 
        mixpanel.sendMessage(mixPanelMessage);
     }
 
     protected void setUserProfile(JSONObject payload) throws JSONException, IOException {
         // See https://mixpanel.com/help/reference/http#people-analytics-updates
 
         JSONObject properties = payload.optJSONObject("properties");
         MixpanelAPI mixpanel = getMixpanelAPI();
         MessageBuilder messageBuilder = new MessageBuilder(token);
 
         String distinct_id = payload.getString("distinct_id");
 
         if (properties == null) {
             properties = new JSONObject();
         }
         JSONObject mixPanelMessage = messageBuilder.set(distinct_id, properties);
 
         String ip = payload.optString("ip", null);
         if (ip != null) {
             JSONUtil.setValue(mixPanelMessage, ip, "message", "$ip");
         }
 
         Long time = payload.optLong("time", 0L);
         if (time != 0L) {
             JSONUtil.setValue(mixPanelMessage, time, "message", "$time");
         }
 
        mixpanel.sendMessage(mixPanelMessage);
     }
 
     protected void deleteUserProfile(JSONObject payload) throws JSONException, IOException {
         // See https://mixpanel.com/help/reference/http#people-analytics-updates
 
         MixpanelAPI mixpanel = getMixpanelAPI();
         MessageBuilder messageBuilder = new MessageBuilder(token);
 
         String distinct_id = payload.getString("distinct_id");
 
         JSONObject mixPanelMessage = messageBuilder.set(distinct_id, null);
         JSONUtil.removeValue(mixPanelMessage, "message", "$set");
         JSONUtil.setValue(mixPanelMessage, "", "message", "$delete");
 
         // Not allowing time to be set via the UI.
         // This is just for testing.
         Long time = payload.optLong("time", 0L);
         if (time != 0L) {
             JSONUtil.setValue(mixPanelMessage, time, "message", "$time");
         }
 
         mixpanel.sendMessage(mixPanelMessage);
     }
 
     protected MixpanelAPI getMixpanelAPI() {
         return new MixpanelAPI();
     }
 }
