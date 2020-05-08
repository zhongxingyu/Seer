 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import java.io.FileReader;
 
 public class IntegrationTestConfiguration {
     /*
     * KidoZen general configuration
     *
     * Replace with the right values
     * */
     public static  String KZ_TENANT;
     public static  String KZ_APP;
     public static  String KZ_USER;
 
     public static  String KZ_PASS;
     public static  String KZ_PROVIDER;
 
     /*
     * Enterprise services configuration
     *
     * Replace with the right values
     *
     * You must configure the following service in the Global section of your KidoZen Marketplace
     *
     * */
     public static  String KZ_SHAREFILE_SERVICEID;
     public static  String KZ_SHAREFILE_USER;
     public static  String KZ_SHAREFILE_PASS;
 
 
     /*
     * EMail configuration
     *
     * Replace with the right values
     * */
     public static  String KZ_EMAIL_TO;
     public static  String KZ_EMAIL_FROM;
 
     static {
         String file_settings = System.getProperty("settings");
         String current_directory = System.getProperty("user.dir");
 
         try {
            if (file_settings !="" && file_settings == null)
             {
                 System.out.print("Current dir:" + current_directory + "\n");
                 System.out.print("File settings:" + file_settings + "\n");
 
                 JSONParser jp = new JSONParser();
                 JSONObject settings =(JSONObject) jp.parse(new FileReader(current_directory + "/" + file_settings));
 
                 KZ_TENANT = settings.get("kz_tenant").toString();
                 KZ_APP = settings.get("kz_app").toString();
                 KZ_USER = settings.get("kz_usr").toString();
                 KZ_PASS = settings.get("kz_pass").toString();
                 KZ_PROVIDER= settings.get("kz_provider").toString();
                 KZ_SHAREFILE_PASS = settings.get("kz_sharefile_pass").toString();
                 KZ_SHAREFILE_SERVICEID = settings.get("kz_sharefile_serviceid").toString();
                 KZ_SHAREFILE_USER = settings.get("kz_sharefile_user").toString();
                 KZ_EMAIL_FROM = settings.get("kz_email_from").toString();
                 KZ_EMAIL_TO = settings.get("kz_email_to").toString();
 
                 System.out.print("==================================================================\n");
                 System.out.print("Tenant: " + KZ_TENANT + "\n");
                 System.out.print("Application: " + KZ_APP + "\n");
                 System.out.print("User: " + KZ_USER + "\n");
                 System.out.print("Password: " + KZ_PASS + "\n");
                 System.out.print("ShareFile Password: " + KZ_SHAREFILE_PASS + "\n");
                 System.out.print("ShareFile Service ID: " + KZ_SHAREFILE_SERVICEID+ "\n");
                 System.out.print("ShareFile User: " + KZ_SHAREFILE_USER + "\n");
                 System.out.print("Email from: " + KZ_EMAIL_FROM + "\n");
                 System.out.print("Email to: " + KZ_EMAIL_TO + "\n");
                 System.out.print("==================================================================\n");
             }
         }
         catch (Exception e)
         {
             System.out.print(e.getMessage().toString() + "\n");
         }
 
     }
 }
