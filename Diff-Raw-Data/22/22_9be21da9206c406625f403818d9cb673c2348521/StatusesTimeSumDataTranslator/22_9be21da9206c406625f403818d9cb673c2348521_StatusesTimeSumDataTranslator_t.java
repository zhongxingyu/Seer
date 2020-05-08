 package ru.andreymarkelov.atlas.plugins.datacollector.customfield;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.util.json.JSONArray;
 import com.atlassian.jira.util.json.JSONException;
 import com.atlassian.jira.util.json.JSONObject;
 
 public class StatusesTimeSumDataTranslator {
     private static final Logger logger = Logger.getLogger(StatusesTimeSumDataTranslator.class);
 
     public static StatusesTimeSumData jsonDataFromString(String json) {
         try {
             JSONObject jsonObj = new JSONObject(json);
             StatusesTimeSumData data = new StatusesTimeSumData();
            if (jsonObj.has("compareField")) {
                data.setCompareField(jsonObj.getString("compareField"));
            }
             JSONArray statuses = jsonObj.getJSONArray("statuses");
             for (int i = 0; i < statuses.length(); i++) {
                 data.addStatus(statuses.getString(i));
             }
             return data;
         } catch (JSONException e) {
             logger.error("Error parse JSON", e);
             return null;
         }
     }
 
     public static String jsonDataToString(StatusesTimeSumData obj) {
         JSONObject jsonObj = new JSONObject();
         try {
             JSONArray statuses = new JSONArray();
             for (String statusId : obj.getStatuses()) {
                 statuses.put(statusId);
             }
             jsonObj.put("statuses", statuses);

            if (obj.getCompareField() != null) {
                jsonObj.put("compareField", obj.getCompareField());
            }
         } catch (JSONException e) {
             logger.error("Error write JSON", e);
             return null;
         }
         return jsonObj.toString();
     }
 
     private StatusesTimeSumDataTranslator() {
     }
 }
