 package beans.config;
 
 import beans.GsMailer;
 import utils.Utils;
 
 import java.io.File;
 
 /**
  * User: guym
  * Date: 12/13/12
  * Time: 1:32 PM
  */
 public class Conf {
 
     public ApplicationConfiguration application = new ApplicationConfiguration();
 
     public SmtpConf smtp = new SmtpConf();
 
     // who is sending the mail?
     public GsMailer.Mailer mailer = new GsMailer.Mailer();
 
    public WidgetConfiguration widget = new WidgetConfiguration();

     public ServerConfig server = new ServerConfig();
 
     public SettingsConfig settings = new SettingsConfig();
 
     @Config(ignoreNullValues = true)
     public boolean sendErrorEmails = false;
 
     @Config( playKey = "spring.context")
     public String springContext = null;
 
     public CloudifyConfiguration cloudify = new CloudifyConfiguration();
 
     public String mixpanelApiKey = null;
 
 
     public static class SettingsConfig{
         @Config( ignoreNullValues = true )
         public boolean expireSession = false; // do not use the session expired mechanism.
     }
 

    public static class WidgetConfiguration{

        public String serverId;

        public long stopTimeoutMillis;
    }

     public static class CloudifyConfiguration{
 
         public long deployWatchDogProcessTimeoutMillis = Utils.parseTimeToMillis( "2mn" );
         
         public long bootstrapCloudWatchDogProcessTimeoutMillis = Utils.parseTimeToMillis( "2mn" );
 
         public File deployScript=Utils.getFileByRelativePath( "/bin/deployer.sh" );
 
         public String removeOutputLines = "";
 
         public String removeOutputString = "";
     }
 
 
 }
