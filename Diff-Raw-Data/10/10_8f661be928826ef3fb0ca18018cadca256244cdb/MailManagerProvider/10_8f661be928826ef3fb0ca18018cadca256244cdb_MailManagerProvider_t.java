 package polly.core.mail;
 
 import de.skuzzle.polly.sdk.Configuration;
 import polly.configuration.ConfigurationProviderImpl;
 import polly.configuration.ConfigurationImpl;
 import polly.moduleloader.AbstractProvider;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.moduleloader.annotations.Provide;
 import polly.moduleloader.annotations.Require;
 
 
 @Module(
     requires = @Require(component = ConfigurationProviderImpl.class),
     provides = {
         @Provide(component = MailManagerImpl.class),
         @Provide(component = MailConfig.class)})
 public class MailManagerProvider extends AbstractProvider {
 
     public final static String MAIL_CONFIG = "mail.cfg";
     
 
     
     public final static String RECIPIENTS = "recipients";
     public final static String LOG_THRESHOLD = "threshold";
     public final static String MAIL_PROVIDER = "mailProvider";
 
     
     
     
     public MailManagerProvider(ModuleLoader loader) {
         super("MAIL_MANAGER_PROVIDER", loader, false);
     }
 
 
     
     @Override
     public void setup() throws SetupException {
         ConfigurationProviderImpl configProvider = 
             this.requireNow(ConfigurationProviderImpl.class, true);
         Configuration config = null;
         try {
             config = configProvider.open(MAIL_CONFIG);
         } catch (Exception e) {
             throw new SetupException(e);
         }
         
         // HACK: MailConfig is a total hack currently
         MailConfig cfg = new MailConfig(
             (ConfigurationImpl) config,
             configProvider,
             config.readString(RECIPIENTS), 
             config.readString(LOG_THRESHOLD), 
             config.readString(MAIL_PROVIDER));
        
        
         this.provideComponent(cfg);
        MailManagerImpl mailManager = new MailManagerImpl(cfg.getSender());
         
         this.provideComponent(mailManager);
     }
 
     
 }
