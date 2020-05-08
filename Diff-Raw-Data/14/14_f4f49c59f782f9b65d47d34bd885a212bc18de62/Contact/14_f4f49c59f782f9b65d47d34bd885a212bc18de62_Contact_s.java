 package com.tuvistavie.meetup.contact.model;
 
 import com.tuvistavie.meetup.App;
 import com.tuvistavie.meetup.contact.util.ContactHelper;
 import com.tuvistavie.meetup.model.AbstractEntity;
 import com.tuvistavie.meetup.util.JSONHelper;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.List;
 
 /**
  * Created by daniel on 9/3/13.
  */
 public class Contact extends AbstractEntity {
 
     protected String displayName;
     protected List<String> emails;
     protected List<String> phoneNumbers;
 
     public Contact(String displayName, List<String> emails, List<String> phoneNumbers) {
         this.displayName = displayName;
         this.emails = emails;
         this.phoneNumbers = phoneNumbers;
     }
 
     @Override
     public void fromJSON(JSONObject jsonObject) {
         JSONHelper<String> helper = new JSONHelper<String>();
         try {
             this.displayName = jsonObject.getString("display_name");
             this.phoneNumbers = helper.jsonArrayToList(jsonObject.getJSONArray("phone_numbers"));
             this.emails = helper.jsonArrayToList(jsonObject.getJSONArray("emails"));
         } catch (JSONException e) {
 
         }
     }
 


     @Override
     public JSONObject toJSONObject() {
         JSONObject jsonObject = new JSONObject();
         JSONHelper<String> helper = new JSONHelper<String>();
         try {
             jsonObject.put("display_name", displayName);
             jsonObject.put("phone_numbers", helper.listToJsonArray(phoneNumbers));
             jsonObject.put("emails", helper.listToJsonArray(emails));
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         return jsonObject;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
     public List<String> getEmails() {
         return emails;
     }
 
     public void setEmails(List<String> emails) {
         this.emails = emails;
     }
 
     public List<String> getPhoneNumbers() {
         return phoneNumbers;
     }
 
     public void setPhoneNumbers(List<String> phoneNumbers) {
         this.phoneNumbers = phoneNumbers;
     }
 }
