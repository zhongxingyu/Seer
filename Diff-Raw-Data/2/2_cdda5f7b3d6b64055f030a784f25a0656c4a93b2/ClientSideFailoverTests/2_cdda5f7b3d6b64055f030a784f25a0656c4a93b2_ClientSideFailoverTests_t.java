 package org.ebayopensource.turmeric.qajunittests.advertisinguniqueidservicev1.sif;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.ebayopensource.turmeric.advertising.v1.services.EchoMessageRequest;
 import org.ebayopensource.turmeric.advertising.v1.services.GetRequestIDResponse;
 import org.ebayopensource.turmeric.advertisinguniqueservicev1.AdvertisingUniqueIDServiceV1SharedConsumer;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceInvocationRuntimeException;
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.ebayopensource.turmeric.runtime.tests.common.jetty.AbstractWithServerTest;
 import org.ebayopensource.turmeric.runtime.tests.common.util.HttpTestClient;
 import org.ebayopensource.turmeric.runtime.tests.common.util.MetricUtil;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class ClientSideFailoverTests extends AbstractWithServerTest{
 	public static HttpTestClient http = HttpTestClient.getInstance();
 	public Map<String, String> queryParams = new HashMap<String, String>();
 
 	/*
 	 * CC.xml 
 	 * <service-location>http://localhost:9090/foo</service-location>
 	 * <service-location>http://localhost:9091/services/advertise/UniqueIDService/v1</service-location>
 	 * <service-location>http://localhost:8080/services/advertise/UniqueIDService/v1</service-location>
 	 */
 	@Test
 	public void testValidScenario1() throws ServiceException, MalformedURLException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/foo"));
 		sl.add(new URL("http://"+serverUri.getHost()+":9091/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 		client.getService().setServiceLocations(sl);
 		
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 	@Test
 	public void testValidScenario2() throws ServiceException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		client.getServiceInvokerOptions().setTransportName(SOAConstants.TRANSPORT_LOCAL);
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 	@Test
 	public void testValidScenario3() throws ServiceException, MalformedURLException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		client.getService().setServiceLocation(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 	@Test
 	public void testValidScenario4() throws ServiceException, MalformedURLException {
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":8081/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		client.getService().setServiceLocations(sl);
 		
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 	@Test
 	public void testValidScenario5() throws ServiceException, MalformedURLException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		client.getService().setServiceLocation(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/ws/spf?X-EBAY-SOA-SERVICE-VERSION=1.0.0"));
 		
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 	@Test
 	public void testValidScenario6() throws ServiceException, MalformedURLException {
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":8081/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/ws/spf"));
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		client.getService().setServiceLocations(sl);
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 	@Test
 	public void testValidScenario7() throws ServiceException, MalformedURLException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover2");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/foo"));
 		sl.add(new URL("http://"+serverUri.getHost()+"/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 		client.getService().setServiceLocations(sl);
 	
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 		///	MarkdownTestHelper.markupClientManually("test1", null, null);
 	}
 
 	@Test
 	public void testChainedServiceConfig() throws ServiceException, MalformedURLException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		try {
 			client.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/foo"));
 		sl.add(new URL("http://"+serverUri.getHost()+":9091/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 		client.getService().setServiceLocations(sl);
 		
 		client.getService().getRequestContext().setTransportHeader("CLIENT-FAILOVER", "failover");
 		GetRequestIDResponse res = client.getReqID("HTTP10");
 		System.out.println(res.getRequestID());
 		Assert.assertTrue(res.getRequestID().contains("AdvertisingUniqueIDServiceV1"));
 	}
 
 	@Test
 	public void testViewConfigBean() throws ServiceException, MalformedURLException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client1 = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 		client1.getService().getRequestContext().setTransportHeader("CLIENT-FAILOVER", "failover");
 		try {
 			client1.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client1.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/foo"));
 		sl.add(new URL("http://"+serverUri.getHost()+":9091/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 		client1.getService().setServiceLocations(sl);
 		
 		GetRequestIDResponse res = client1.getReqID("HTTP10");
 		Assert.assertTrue(res.getRequestID().contains("AdvertisingUniqueIDServiceV1"));
 		try {
 			queryParams.put("id",
 			"com.ebay.soa.client.AdvertisingUniqueIDServiceV2.UniqueIDServiceV2Client.failover.Invoker");
 			queryParams.put("forceXml","true");
 			String response = MetricUtil.invokeHttpClient(queryParams, "view");
 			System.out.println("Response - " + response);
 			Assert.assertTrue(response.contains("name=\"SERVICE_URL\""));
 			Assert.assertTrue(response.contains("http:"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v2"));
 			Assert.assertTrue(response.contains("http:"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v2"));
 			Assert.assertTrue(response.contains("http:"+serverUri.getHost()+":"+serverUri.getPort()+"/foo"));
 		} catch (Exception se) {
 			se.printStackTrace();
 			Assert.assertTrue("Error - No Exception should be thrown ", false);
 		} 
 
 	}
 	@Test
 	public void testUpdateConfigBean() throws ServiceException, MalformedURLException  {
 		AdvertisingUniqueIDServiceV1SharedConsumer client2 = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover1");
 
 		client2.getService().getRequestContext().setTransportHeader("CLIENT-FAILOVER", "failover");
 		try {
 			client2.setHostName(serverUri.getHost()+":"+serverUri.getPort());
 			System.out.println(client2.getHostName());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch blocks
 			e.printStackTrace();
 		}
 		
 		List<URL> sl = new ArrayList<URL> ();
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/foo"));
 		sl.add(new URL("http://"+serverUri.getHost()+":9091/services/advertise/UniqueIDService/v1"));
 		sl.add(new URL("http://"+serverUri.getHost()+":"+serverUri.getPort()+"/services/advertise/UniqueIDService/v1"));
 		client2.getService().setServiceLocations(sl);
 		
 		GetRequestIDResponse res = client2.getReqID("HTTP10");
 		Assert.assertTrue(res.getRequestID().contains("AdvertisingUniqueIDServiceV1"));
 
 		try {
 			queryParams.put("id","com.ebay.soa.client.AdvertisingUniqueIDServiceV2.UniqueIDServiceV2Client.failover.Invoker");
 			queryParams.put("SERVICE_URL", "http://localhost:9090/services/advertise/UniqueIDService/v2,http://localhost:9090/foo");
 			String response = MetricUtil.invokeHttpClient(queryParams, "update");
 			System.out.println("Response - " + response);
 			Assert.assertTrue(response.contains("SERVICE_URL"));
 			Assert.assertTrue(response.
 					contains("http://localhost:9090/services/advertise/UniqueIDService/v2"));
 			Assert.assertFalse(response.
 					contains("http://localhost:8080/services/advertise/UniqueIDService/v2"));
 			Assert.assertTrue(response.
 					contains("http://localhost:9090/foo"));
 		} catch (Exception se) {
 			se.printStackTrace();
 //			Assert.assertTrue("Error - No Exception should be thrown ", false);
 		} 
 		finally {
 		queryParams.put("id","com.ebay.soa.client.AdvertisingUniqueIDServiceV2.UniqueIDServiceV2Client.failover.Invoker");
 		queryParams.put("SERVICE_URL", "http://localhost:9090/services/advertise/UniqueIDService/v2,http://localhost:8080/foo," +
 				"http://localhost:8080/services/advertise/UniqueIDService/v2");
 		String response = MetricUtil.invokeHttpClient(queryParams, "update");
 		} 
 
 	}
 
 	//	 * -ve scenarios
 
 
 	@Test
 	public void testMissingServiceLocations1() throws ServiceException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
 			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failoverError1");
 		
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 	@Test
 	public void testMissingServiceLocations2() throws ServiceException {
 		AdvertisingUniqueIDServiceV1SharedConsumer client = 
			new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failoverError2");
 		
 		EchoMessageRequest req = new EchoMessageRequest();
 		req.setIn("vasu");
 		String response = client.echoMessage(req).getOut();
 		Assert.assertEquals(" Echo Message = vasu", response);
 	}
 
 
 	@Test
 	public void testNullCheck1() {
 
 		AdvertisingUniqueIDServiceV1SharedConsumer client;
 		try {
 			client = new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover2");
 
 			client.getService().setServiceLocations(null);
 			EchoMessageRequest req = new EchoMessageRequest();
 			req.setIn("vasu");
 			String response = client.echoMessage(req).getOut();
 			System.out.println(response);
 		} catch (ServiceInvocationRuntimeException e) {
 			// TODO Auto-generated catch block
 			System.out.println(e.getMessage());
 			Assert.assertTrue(e.getMessage().contains("No service address defined for invocation of service"));
 			e.printStackTrace();
 
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 	@Test
 	public void testNullCheck2() {
 
 		AdvertisingUniqueIDServiceV1SharedConsumer client;
 		try {
 			client = new AdvertisingUniqueIDServiceV1SharedConsumer("AdvertisingUniqueIDServiceV1Consumer", "failover2");
 			//			client.getService().setServiceLocation(new URL("http://localhost:9090/services/advertise/UniqueIDService/v1"));
 			List<URL> locations = new ArrayList<URL> ();
 			locations.add(new URL(""));
 			locations.add(new URL("http://localhost:9080/services/advertise/UniqueIDService/v1"));
 			locations.add(new URL("http://localhost:8080/services/advertise/UniqueIDService/v1"));
 			client.getService().setServiceLocations(locations);
 			EchoMessageRequest req = new EchoMessageRequest();
 			req.setIn("vasu");
 			String response = client.echoMessage(req).getOut();
 			System.out.println(response);
 			Assert.assertEquals(" Echo Message = vasu", response);
 		} catch (ServiceInvocationRuntimeException e) {
 			// TODO Auto-generated catch block
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			Assert.assertTrue("Should not throw an exception", false);
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 }
