 package org.jds.spring;
 
 
 import org.jds.common.ISharedService;
 import org.jds.core.ServiceManager;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration
 @RunWith(SpringJUnit4ClassRunner.class)
 public class RemoteServiceTest {
 	@Autowired
 	@Qualifier("serviceManager")
 	ServiceManager manager;
 
 	@Autowired
 	@Qualifier("serviceManager2")
 	ServiceManager manager2;
 
	@Test(timeout = 0)
 	public void testAcquireRemoteService() throws InterruptedException {
 		// Waiting for node discovery
 		ISharedService service = null;
 
 		while (true) {
 			service = manager2.getService(ISharedService.class, "testService");
 
 			if (service != null) {
 				break;
 			}
 
 			System.out.println("Waiting for service discovery...");
 			Thread.sleep(500);
 		}
 
 		service.testCall("woot");
 	}
 }
