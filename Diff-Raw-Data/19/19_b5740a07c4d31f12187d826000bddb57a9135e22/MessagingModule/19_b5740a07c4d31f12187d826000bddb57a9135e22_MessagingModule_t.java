
 package axirassa.ioc;
 
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.hornetq.api.core.HornetQException;

 import axirassa.webapp.services.EmailNotifyService;
 import axirassa.webapp.services.MessagingSession;
 import axirassa.webapp.services.MessagingSessionManager;
 import axirassa.webapp.services.SmsNotifyService;
 import axirassa.webapp.services.VoiceNotifyService;
 import axirassa.webapp.services.internal.EmailNotifyServiceImpl;
 import axirassa.webapp.services.internal.MessagingSessionManagerImpl;
 import axirassa.webapp.services.internal.SmsNotifyServiceImpl;
 import axirassa.webapp.services.internal.VoiceNotifyServiceImpl;
 
 /**
 * 
  * @author wiktor
  */
 public class MessagingModule {
 
 	@Scope(ScopeConstants.PERTHREAD)
 	public static MessagingSessionManager buildMessagingSessionManager (PerthreadManager perthreadManager)
 	        throws HornetQException {
 		MessagingSessionManagerImpl sessionManager = new MessagingSessionManagerImpl();
 		perthreadManager.addThreadCleanupListener(sessionManager);
 		return sessionManager;
 	}
 
 
	@Scope(ScopeConstants.PERTHREAD)
 	public static MessagingSession buildMessagingSession (MessagingSessionManager sessionManager,
 	        PropertyShadowBuilder propertyShadowBuilder) throws HornetQException {
 		return propertyShadowBuilder.build(sessionManager, "session", MessagingSession.class);
 	}
 
 
 	@Scope(ScopeConstants.PERTHREAD)
 	public EmailNotifyService buildEmailNotifyService (MessagingSession messagingSession) throws HornetQException {
 		return new EmailNotifyServiceImpl(messagingSession);
 	}
 
 
 	@Scope(ScopeConstants.PERTHREAD)
 	public SmsNotifyService buildSmsNotifyService (MessagingSession messagingSession) throws HornetQException {
 		return new SmsNotifyServiceImpl(messagingSession);
 	}
 
 
 	@Scope(ScopeConstants.PERTHREAD)
 	public VoiceNotifyService buildVoiceNotifyService (MessagingSession messagingSession) throws HornetQException {
 		return new VoiceNotifyServiceImpl(messagingSession);
 	}
 
 }
