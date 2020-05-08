 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.model.APINotificationEmail;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 
 /**
  *
  * @author Sławomir
  */
 public class UserOperationsSample {
 
    public static final APIClient CLIENT = Common.createApiClient();
     
     public static void main(String[] args) {
         try {
             // Get authenticated user
             APIUser me = CLIENT.me();
             
             // List available data
             printUser(me);
             
             // Updating user data
             me.setAddress("Address");
             me.setCity("City");
             me.setCode("1234567890");
             me.setCountry("Finland");
             me.setEmail("admin@localhost");
             me.setName("My name is ...");
             me.setOrganization("BitBar");
             me.setPhone("0987654321");
             me.setState("Some state");
             me.setVatID("FR1234890");
             
             me.update();
             
             // List data again
             printUser(me);
         } catch(APIException apie) {
             System.err.println(apie.getMessage());
         }
     }
     
     private static void printUser(APIUser user) {
         System.out.println(String.format("Address: %s", user.getAddress()));
         System.out.println(String.format("City: %s", user.getCity()));
         System.out.println(String.format("Code: %s", user.getCode()));
         System.out.println(String.format("Country: %s", user.getCountry()));
         System.out.println(String.format("Emial: %s", user.getEmail()));
         System.out.println(String.format("Name: %s", user.getName()));
         System.out.println(String.format("Organization: %s", user.getOrganization()));
         System.out.println(String.format("Phone: %s", user.getPhone()));
         System.out.println(String.format("State: %s", user.getState()));
         System.out.println(String.format("Time zone: %s", user.getTimeZone()));
         System.out.println(String.format("Vat ID: %s", user.getVatID()));
     }
 }
