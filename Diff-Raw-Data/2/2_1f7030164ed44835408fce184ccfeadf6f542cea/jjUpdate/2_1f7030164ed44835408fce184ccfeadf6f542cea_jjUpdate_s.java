 /*
  * jjUpdate.java
  *
  * Created on October 17, 2005, 9:33 AM
  */
 
 package java_jjclient;
 
 import javax.net.ssl.HttpsURLConnection;
 import java.net.URL;
 import java.util.Date;
 import java.io.OutputStreamWriter;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 
 /**
  *
  * @author caryn
  */
 public class jjUpdate {
 
     // account information
     private String username;
     private String password;
 
     /**
      * Constructor
      * @param jjUsername
      * @param jjPassword
      */
     public jjUpdate(String jjUsername, String jjPassword) {
         username = jjUsername;
         password = jjPassword;
     }
 
     /**
      * Updates journal
      * @param subject
      * @param body
      * @param mood
      * @param location
      * @param security
      * @param music
      * @param format
      * @param email
      * @param allowComment
      * @return true if update succeeded.
      */
     public boolean update (String subject, String body, String mood,
                            String location, String security, String music,
                            boolean format, boolean email, boolean allowComment) {
         Date today = new Date();
         String strDate = today.toString();
         // location, mood, security are all int values
         // aformat, subject, body, allow_comments, email_comments (checked, unchecked) are strings
 
         // translate location to integer value
         int locationInteger = 0;
         if (location.equals("Home"))
             locationInteger = 1;
         else if (location.equals("Work"))
             locationInteger = 2;
         else if (location.equals("School"))
             locationInteger = 3;
         else if (location.equals("Other"))
             locationInteger = 5;
 
         // translate security to integer value
         // default value is public
         int securityInteger = 2;
         if (security.equals("Private"))
             securityInteger = 0;
         else if (security.equals("Friends Only"))
             securityInteger = 1;
 
         // translate format, email and allow comments to string
         // boolean to string for auto format
         String strFormat = "checked";
         if (!format)
             strFormat = "unchecked";
 
         // boolean to string for email comments
         String strEmail = "unchecked";
         if (email)
             strEmail = "checked";
 
         String strAllow = "checked";
         if (!allowComment)
             strAllow = "unchecked";
 
         try {
             // construct the POST request data
             String type = "application/x-www-form-urlencoded";
             String data = "";
             data += "user=" + username;
             data += "&pass=" + password;
             data += "&security=" + securityInteger;
             data += "&location=" + locationInteger;
             data += "&mood=12";  // Not Specified value
             data += "&music=" + music;
             data += "&aformat" + strFormat;
             data += "&allow_comment=" + strAllow;
             data += "&email_comment" + strEmail;
             data += "&date=" + strDate;
             data += "&subject=" + subject;
             data += "&body=" + body;
 
             String encodedData = java.net.URLEncoder.encode(data,"UTF-8");
 
             // open connection
            URL jj = new URL ("https://www.justjournal/updateJournal");
             HttpsURLConnection conn = (HttpsURLConnection) jj.openConnection();
             // set requesting agent, and POST
             conn.setRequestMethod ("POST");
             conn.setRequestProperty ("User-Agent", "JustJournal");
             conn.setRequestProperty( "Content-Type", type );
             conn.setDoOutput(true);
             conn.setDoInput(true);
 
             OutputStreamWriter writer =
                     new OutputStreamWriter(conn.getOutputStream());
 
             writer.write(encodedData);
             writer.flush();
             writer.close();
             // getting the response
             BufferedReader input = new BufferedReader (new InputStreamReader
                     (conn.getInputStream()));
             int response = input.read();
             char [] returnCode = new char [50];
             int i = 0;
             // for debugging
             while (response != -1) {
                 returnCode [i] = (char) response;
                 i++;
                 response = input.read();
             }
             input.close();
             String code = new String (returnCode);
             System.out.println(code);
         }
         catch (Exception e) {
             System.err.println(e.getMessage());
         }
 
         // if we get this far, we failed
         // display error msg
         return false;
     }
 
 }
