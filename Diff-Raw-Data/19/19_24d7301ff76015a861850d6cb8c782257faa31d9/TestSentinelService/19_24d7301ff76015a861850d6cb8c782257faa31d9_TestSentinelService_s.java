 
 package test.com.zanoccio.axirassa.services;
 
 import org.hibernate.Session;
 import org.junit.Test;
 
import com.zanoccio.axirassa.services.Service;
 import com.zanoccio.axirassa.services.sentinel.SentinelService;
 import com.zanoccio.axirassa.util.AbstractDomainTest;
 
 public class TestSentinelService {
 
 	public static Session session;
 
 
 	@Test
 	public void test() throws Exception {
 		AbstractDomainTest.hibernateSession();
 		session = AbstractDomainTest.session;
 
		Service service = new SentinelService(session, 1);
 
 		for (int i = 0; i < (60 * 8); i++) {
 			service.execute();
 			Thread.sleep(1000);
 		}
 	}
}
