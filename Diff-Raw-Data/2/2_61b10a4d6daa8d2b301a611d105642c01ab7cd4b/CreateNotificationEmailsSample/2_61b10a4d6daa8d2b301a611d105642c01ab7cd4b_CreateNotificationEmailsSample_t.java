 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIList;
 import com.testdroid.api.model.APINotificationEmail;
 import com.testdroid.api.model.APIProject;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 
 /**
  *
  * @author Sławomir
  */
 public class CreateNotificationEmailsSample {
 
    public static final APIClient CLIENT = Common.createApiClient();
     
     public static void main(String[] args) {
         try {
             // Get authenticated user
             APIUser me = CLIENT.me();
             
             // Clean notification emails, if exist
             for(APINotificationEmail notificationEmail: me.getNotificationEmails().getEntity().getData()) {
                 System.out.println(String.format("Deleting notification email %s", notificationEmail.getEmail()));
                 notificationEmail.delete();
             }
             
             System.out.println("Now add some notification emails");
             // Create notification email, for specified email, notified always
             APINotificationEmail alwaysNotificationEmail = me.createNotificationEmail("email@localhost", APINotificationEmail.Type.ALWAYS);
             
             // Create notification email, for specified email, notified on test run failures
             APINotificationEmail onFailureNotificationEmail = me.createNotificationEmail("different.email@localhost", APINotificationEmail.Type.ON_FAILURE);
             
             // Then you can get list of created notification emails
             APIList<APINotificationEmail> notificationEmailsList = me.getNotificationEmails().getEntity();
             
             System.out.println("Notification emails are:");
             for(APINotificationEmail notificationEmail: notificationEmailsList.getData()) {
                 System.out.println(String.format("%s, type: %s", notificationEmail.getEmail(), notificationEmail.getType()));
             }
             
             // You can create notification email for project only
             APIProject project = me.createProject(APIProject.Type.ANDROID);
             
             project.createNotificationEmail("user@localhost", APINotificationEmail.Type.ON_FAILURE);
             
             // And get it
             for(APINotificationEmail notificationEmail: project.getNotificationEmails().getEntity().getData()) {
                 System.out.println(String.format("%s, type: %s", notificationEmail.getEmail(), notificationEmail.getType()));
             }
             
         } catch(APIException apie) {
             System.err.println(apie.getMessage());
         }
     }
     
 }
