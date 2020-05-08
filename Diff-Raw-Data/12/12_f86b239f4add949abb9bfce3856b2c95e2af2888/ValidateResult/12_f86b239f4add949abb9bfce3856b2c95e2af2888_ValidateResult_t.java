 package com.osastudio.newshub.data.user;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.osastudio.newshub.data.NewsResult;
 
 public class ValidateResult extends NewsResult {
 
    public static final String JSON_KEY_USER_IDS = "student_ids";
 
    private List<String> userIds;
 
    public ValidateResult(JSONObject jsonObject) {
       super(jsonObject);
 
       if (isSuccess()) {
          try {
            JSONObject idsObject = null;
            if (!jsonObject.isNull(JSON_KEY_LIST)) {
               idsObject = jsonObject.getJSONObject(JSON_KEY_LIST);
            }
            if (idsObject == null) {
               return;
            }

            if (!idsObject.isNull(JSON_KEY_USER_IDS)) {
               JSONArray jsonArray = idsObject.getJSONArray(JSON_KEY_USER_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                   this.userIds = new ArrayList<String>();
                   for (int i = 0; i < jsonArray.length(); i++) {
                      this.userIds.add(jsonArray.getString(i));
                   }
                }
             }
          } catch (JSONException e) {
 
          }
       }
    }
 
    public boolean isValidated() {
       return isSuccess();
    }
 
    public boolean hasUserIds() {
       return this.userIds != null && this.userIds.size() > 0;
    }
 
    public List<String> getUserIds() {
       return this.userIds;
    }
 
    public void setUserIds(List<String> userIds) {
       this.userIds = userIds;
    }
 
 }
