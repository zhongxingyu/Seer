 package net.bbm485.db;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.SerializedName;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import net.bbm485.exceptions.InvalidDataException;
 
 public class User {
 
     private String id;
     @SerializedName("username")
     private String userName;
     @SerializedName("fullname")
     private String fullName;
     @SerializedName("gender")
     private String gender;
     @SerializedName("birthDate")
     private String birthDate;
 
     /**
      * * Getters and Setters **
      */
     public String getId() {
         return id;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public String getFullName() {
         return fullName;
     }
 
     public String getGender() {
         return gender;
     }
 
     public String getBirthDate() {
         return birthDate;
     }
 
     public void setId(String id) {
         this.id = id == null ? "" : id;
     }
 
     public void setUserName(String userName) {
         this.userName = userName == null ? "" : userName;
     }
 
     public void setFullName(String fullName) {
         this.fullName = fullName == null ? "" : fullName;
     }
 
     public void setGender(String gender) {
         this.gender = gender == null ? "" : gender;
     }
 
     public void setBirthDate(String birthDate) {
         this.birthDate = birthDate == null ? "" : birthDate;
     }
 
     /**
      * * End of Getters and Setters **
      */
     public User() {
     }
 
     public User(String id, String userName, String fullName, String gender, String birthDate) {
         setId(id);
         setUserName(userName);
         setFullName(fullName);
         setGender(gender);
         setBirthDate(birthDate);
     }
 
     public void checkInfo() throws InvalidDataException {
         JSONArray errMsg = new JSONArray();
         JSONObject errObj = null;
         try {
             if (isNullorEmpty(userName)) {
                 errObj = new JSONObject();
                 errObj.put("fieldName", "username");
                 errObj.put("rejectedValue", JSONObject.NULL);
                 errMsg.put(errObj);
             }
             if (isNullorEmpty(fullName)) {
                 errObj = new JSONObject();
                 errObj.put("fieldName", "fullname");
                 errObj.put("rejectedValue", JSONObject.NULL);
                 errMsg.put(errObj);
             }
             if (isNullorEmpty(gender)) {
                 errObj = new JSONObject();
                 errObj.put("fieldName", "gender");
                 errObj.put("rejectedValue", JSONObject.NULL);
                 errMsg.put(errObj);
             }
             if (isNullorEmpty(birthDate)) {
                 errObj = new JSONObject();
                 errObj.put("fieldName", "birthDate");
                 errObj.put("rejectedValue", JSONObject.NULL);
                 errMsg.put(errObj);
             }
             if (errMsg.length() != 0) {
                 throw new InvalidDataException(errMsg);
             }
         }
         catch (JSONException e) {
         }
     }
     
     public void updateInfo(JSONObject info) {
         Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
         User dummyUser = (User) gson.fromJson(info.toString(), User.class);
         if (!isNullorEmpty(dummyUser.getUserName()))
             setUserName(dummyUser.getUserName());
         
         if (!isNullorEmpty(dummyUser.getFullName()))
             setFullName(dummyUser.getFullName());
         
         if (!isNullorEmpty(dummyUser.getGender()))
             setGender(dummyUser.getGender());
         
         if (!isNullorEmpty(dummyUser.getBirthDate()))
             setBirthDate(dummyUser.getBirthDate());
     }
 
     private boolean isNullorEmpty(String str) {
         return str == null || str.isEmpty() ? true : false;
     }
 
     public String toJson() {
         return new GsonBuilder().serializeNulls().disableHtmlEscaping().create().toJson(this);
     }
 }
