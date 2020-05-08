 
 package test.com.zanoccio.axirassa.services;
 
 import org.hibernate.Session;
 import org.junit.Test;
 
import com.zanoccio.axirassa.services.sentinel.CPUSentinelAgent;
import com.zanoccio.axirassa.services.sentinel.DiskSentinelAgent;
import com.zanoccio.axirassa.services.sentinel.MemorySentinelAgent;
import com.zanoccio.axirassa.services.sentinel.NetworkSentinelAgent;
 import com.zanoccio.axirassa.services.sentinel.SentinelService;
 import com.zanoccio.axirassa.util.AbstractDomainTest;
 
 public class TestSentinelService {
 
 	public static Session session;
 
 
 	@Test
 	public void test() throws Exception {
 		AbstractDomainTest.hibernateSession();
 		session = AbstractDomainTest.session;
 
		SentinelService service = new SentinelService(session, 1);

		service.addAgent(CPUSentinelAgent.class);
		service.addAgent(MemorySentinelAgent.class);
		service.addAgent(DiskSentinelAgent.class);
		service.addAgent(NetworkSentinelAgent.class);

		service.initialize();
 
 		for (int i = 0; i < (60 * 8); i++) {
 			service.execute();
 			Thread.sleep(1000);
 		}
 	}
}
