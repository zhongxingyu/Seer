 
 package axirassa.util.test;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.realm.Realm;
 import org.junit.After;
 import org.tynamo.security.services.TapestryRealmSecurityManager;
 
 import axirassa.ioc.AxirassaSecurityModule;
 import axirassa.ioc.ExternalServicesMockingModule;
 import axirassa.ioc.FlowsModule;
 import axirassa.ioc.HibernateTestingModule;
 import axirassa.ioc.LoggingModule;
 import axirassa.ioc.MessagingModule;
 
 import com.formos.tapestry.testify.core.TapestryTester;
 import com.formos.tapestry.testify.junit4.TapestryTest;
 
 public class TapestryPageTest extends TapestryTest {
 	private static final TapestryTester SHARED_TESTER = new TapestryTester("axirassa.webapp", FlowsModule.class,
 	        MessagingModule.class, LoggingModule.class, ExternalServicesMockingModule.class,
 	        HibernateTestingModule.class, AxirassaSecurityModule.class);
 
 
 	public TapestryPageTest() {
 		super(SHARED_TESTER);
 		setSecurityManager();
 	}
 
 
 	private void setSecurityManager() {
 		CustomSecurityManagerFactory factory = SHARED_TESTER.autobuild(CustomSecurityManagerFactory.class);
 
 		Collection<Realm> realms = new ArrayList<Realm>();
 		realms.add(factory.getEntityRealm());
 
 		org.apache.shiro.mgt.SecurityManager manager = new TapestryRealmSecurityManager(realms);
 		SecurityUtils.setSecurityManager(manager);
 	}
 
 
 	@After
 	public void stopSessions() {
 		HibernateCleanupService cleanupService = SHARED_TESTER.autobuild(HibernateCleanupService.class);
 		cleanupService.cleanup();
 	}
 }
