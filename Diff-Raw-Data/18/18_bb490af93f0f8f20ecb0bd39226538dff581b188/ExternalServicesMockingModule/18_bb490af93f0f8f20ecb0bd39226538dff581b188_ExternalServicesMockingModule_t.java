 
 package axirassa.ioc;
 
 import static org.mockito.Mockito.mock;
 
 import org.apache.tapestry5.ioc.MappedConfiguration;
 
 import axirassa.webapp.services.EmailNotifyService;
 import axirassa.webapp.services.MessagingSession;
 import axirassa.webapp.services.MessagingSessionManager;
 import axirassa.webapp.services.SmsNotifyService;
 import axirassa.webapp.services.VoiceNotifyService;
 
 public class ExternalServicesMockingModule {
	public static void contributeServiceOverride(MappedConfiguration<Class<?>, Object> configuration) {
 		configuration.add(MessagingSessionManager.class, mock(MessagingSessionManager.class));
 		configuration.add(MessagingSession.class, mock(MessagingSession.class));
 		configuration.add(EmailNotifyService.class, mock(EmailNotifyService.class));
 		configuration.add(SmsNotifyService.class, mock(SmsNotifyService.class));
 		configuration.add(VoiceNotifyService.class, mock(VoiceNotifyService.class));
 	}
 }
