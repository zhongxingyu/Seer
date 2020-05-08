 package polly.core.mail;
 
 import java.lang.reflect.Constructor;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import org.apache.log4j.Level;
 
 import polly.configuration.ConfigurationImpl;
 import polly.configuration.ConfigurationProviderImpl;
 import polly.core.mail.senders.MailSender;
 import polly.moduleloader.SetupException;
 
 
 
 public class MailConfig extends ConfigurationImpl {
 
     public final static String SMTP_FROM = "mail.smtp.from";
     public final static String SMTP_HOST = "mail.smtp.host";
     public final static String SMTP_PORT = "mail.smtp.port";
     public final static String SMTP_PASSWORD = "mail.smtp.password";
     public final static String SMTP_LOGIN = "mail.smtp.login";
     
     public final static String MAIL_DELAY = "mailDelay";
     
     private InternetAddress[] recipients;
     private MailSender sender;
     private Level logLevel;
     
     
     
    public MailConfig(ConfigurationImpl config, ConfigurationProviderImpl parent, 
            String recipients, String logLevel, String mailProvider) 
                    throws SetupException {
        super(parent, config);
         
         // parse recipients
         try {
             this.recipients = InternetAddress.parse(recipients, false);
         } catch (AddressException e) {
             throw new SetupException("invalid recipient list");
         }
         
         
         this.logLevel = Level.toLevel(logLevel);
         if (this.logLevel.toInt() < Level.INFO_INT) {
             throw new SetupException(
                 "Log level threshold too low. Must at least be 'ERROR'.");
         }
         
         // initialize mail provider
         String cls = mailProvider;
         try {
             Class<?> provider = Class.forName(cls);
             Constructor<?> con = provider.getConstructor(MailConfig.class);
             Object tmp = con.newInstance(this);
             
             if (!(tmp instanceof MailSender)) {
                 throw new Exception(cls + " is no valid MailSender");
             }
             this.sender = (MailSender) tmp;
         } catch (Exception e) {
             throw new SetupException("Invalid MailProvider: " + cls, e);
         }
     }
     
     
     
     public MailSender getSender() {
         return this.sender;
     }
     
     
     
     public InternetAddress[] getRecipients() {
         return this.recipients;
     }
 
 
 
     public Level getLevel() {
         return this.logLevel;
     }
 }
