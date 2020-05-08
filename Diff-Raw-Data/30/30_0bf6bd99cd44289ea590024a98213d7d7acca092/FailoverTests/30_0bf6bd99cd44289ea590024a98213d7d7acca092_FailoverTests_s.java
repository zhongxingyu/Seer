 package org.ebayopensource.turmeric.runtime.tests.failover;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
 import org.ebayopensource.turmeric.runtime.tests.common.sif.error.MarkdownTestHelper;
 
 public class FailoverTests extends TestCase {
 
	public void setUp(){
 		
 		MarkdownTestHelper.markupClientManually("test1", null, null);
 	}
 	
 	public void testSimple() throws Exception{
 		URL url = new URL("http://localhost:8080/ws/spf");
 		Service service = ServiceFactory.create("Test1Service", "keepAlive", url);
 		String outMessage = (String) service.createDispatch("echoString").invoke("hello");
 		System.out.println("outMessage:"+outMessage);
 	}
 	
 	public void testMultiple() throws Exception{
 		List<URL> urls = new ArrayList<URL>();
 		URL url = new URL("http://nothing:8080/ws/spf");
 		urls.add(url);
 		url = new URL("http://localhost:8080/ws/spf");
 		urls.add(url);
 		
 		Service service = ServiceFactory.create("test1", "failover");
 		service.setServiceLocations(urls);
 		String outMessage = (String) service.createDispatch("echoString").invoke("hello");
 		System.out.println("outMessage:"+outMessage);
 		assertEquals("hello", outMessage);
 	}
 	
 	public void testMultipleFail() throws Exception{
 		List<URL> urls = new ArrayList<URL>();
 		URL url = new URL("http://nothing:8080/ws/spf");
 		urls.add(url);
 		url = new URL("http://nothingagain:8080/ws/spf");
 		urls.add(url);
 		
 		Service service = ServiceFactory.create("test1b", "failover");
 		service.setServiceLocations(urls);
 		Map<String, String> options = new HashMap<String, String>();
 		service.getInvokerOptions().getTransportOptions().setProperties(options);
 		try{
 			service.createDispatch("echoString").invoke("hello");
 			assertTrue(false);
 		}catch(Exception e){
 			assertTrue(true);
 		}
 		
 			
 	}
 	
 	public void testMultipleWithConfigFile() throws Exception{
 		Service service = ServiceFactory.create("test1", "failover"); //shd pick up the cc.xml?
 		String outMessage = (String) service.createDispatch("echoString").invoke("hello");
 		System.out.println("outMessage:"+outMessage);
 	}
 	
 	public void testMultipleFailWithConfigFile() throws Exception{
 		Service service = ServiceFactory.create("test1a", "failover"); //shd pick up the cc.xml?
 		try{
 			String outMessage = (String) service.createDispatch("echoString").invoke("hello");
 			assertTrue(false); //shoiuld have failed
 		}catch(Exception e){
 			assertTrue(true); // will ultimately fail with the unresolved exception after 
 			// going through the failovers
 		}
 	}
 	
 	public void testSuccessiveInvocations() throws Exception{
 		List<URL> urls = new ArrayList<URL>();
 		//URL url = new URL("http://nothing:8080/ws/spf");
 		//urls.add(url);
 		URL url = new URL("http://localhost:8080/ws/spf");
 		urls.add(url);
 		Service service = ServiceFactory.create("test1", "failover");
 		service.setServiceLocations(urls);
 		String outMessage = (String) service.createDispatch("echoString").invoke("hello");
 		assertEquals("hello", outMessage);
 		url = new URL("http://nothing:8080/ws/spf");
 		service.setServiceLocation(url);
 		try{
 			outMessage = (String) service.createDispatch("echoString").invoke("hello");
 			assertTrue(false);
 		}catch(Exception e){
 			assertTrue(true);
 		}
 	}
 	
 	
 	/*public void testFailure() throws Exception{
 			GetDeploymentKeysRequest request = new GetDeploymentKeysRequest();
 			BaseResourcePersistenceServiceConsumer service = getClient();
 			GetDeploymentKeysResponse response = service.getDeploymentKeys(request);
 			
 	}
 	
 	public BaseResourcePersistenceServiceConsumer getClient(){
 		BaseResourcePersistenceServiceConsumer client;
 		try {
 			String m_urlStr = null;
 			if (m_urlStr == null) {
 				IRemoteResourceRepoResolver resolver=null;
 				if (resolver == null) {
 					resolver = DsfRuntime.getInstance().getCommonFwk().getRemoteResourceRepoResolver("testSource");
 				}
 				
 				client = new ResourceServiceClient(resolver);
 			}
 			else {
 				URL m_url = new URL(m_urlStr);
 				client = new ResourceServiceClient(m_url, "AppName", "token", IRemoteResourceRepoResolver.AuthMode.Default);
 			}
 				
 		} catch (MalformedURLException e) {
 			throw new RepositoryRuntimeException(e);
 		}
 		
 		return client;
 	}*/
 }
