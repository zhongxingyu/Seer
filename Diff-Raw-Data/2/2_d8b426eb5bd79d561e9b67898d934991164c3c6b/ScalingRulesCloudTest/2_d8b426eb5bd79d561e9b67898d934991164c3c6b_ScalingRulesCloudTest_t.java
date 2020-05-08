 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.testng.Assert;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 /**
  *  Tests scaling rules on various clouds.
  *  @author itaif
  *  @since 2.1.0
  */
 public class ScalingRulesCloudTest extends AbstractCloudTest {
 
	private static final String APPLICATION_FOLDERNAME = "petclinic-simple-scalingRules";
 	private static final String APPLICATION_NAME = "petclinic";
 	private static final String TOMCAT_SERVICE_NAME = "tomcat";
 	private static final String ABSOLUTE_SERVICE_NAME = ServiceUtils.getAbsolutePUName(APPLICATION_NAME, TOMCAT_SERVICE_NAME);
 	private static final int NUMBER_OF_HTTP_GET_THREADS = 10;
 	private static final int THROUGHPUT_PER_THREAD = 1;
 	private static final int TOMCAT_PORT = 8080;
 	
 	private ScheduledExecutorService executor;
 	private final List<HttpRequest> threads = new ArrayList<HttpRequest>();
 	
 	@BeforeTest
 	@Override
 	public void beforeTest() {
 		super.beforeTest();
 		executor= Executors.newScheduledThreadPool(NUMBER_OF_HTTP_GET_THREADS);
 	}
 	
 	@AfterTest
 	@Override
 	public void afterTest() {
 		if (executor != null) {
 			executor.shutdownNow();
 		}
 		
 		super.afterTest();
 	}
 	
 	//need to enable once it passes more than once
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 3, enabled = false, dataProvider = "supportedClouds")
 	public void testPetclinicSimpleScalingRules(String cloudName) throws Exception {		
 		
 		LogUtils.log("installing application " + APPLICATION_NAME + " on " + cloudName);
 		setCloudToUse(cloudName);
 		String applicationPath = ScriptUtils.getBuildPath() + "/examples/" + APPLICATION_FOLDERNAME;
 		installApplicationAndWait(applicationPath, APPLICATION_NAME);
 		
 		
 		repititiveAssertNumberOfInstances(ABSOLUTE_SERVICE_NAME, 1);
 	
 		
 		// increase web traffic, wait for scale out
 		startThreads();
 		repititiveAssertNumberOfInstances(ABSOLUTE_SERVICE_NAME, 2);
 		
 		// stop web traffic, wait for scale in
 		stopThreads();
 		repititiveAssertNumberOfInstances(ABSOLUTE_SERVICE_NAME, 1);
 		
 		/*
 		 Try to start a new machine and then cancel it.
 		 startThreads();
 		executor.schedule(new Runnable() {
 			
 			@Override
 			public void run() {
 				stopThreads();
 				
 			}
 		}, 60, TimeUnit.SECONDS);
 		repetitiveNumberOfInstancesHolds(ABSOLUTE_SERVICE_NAME, 1, 500, TimeUnit.SECONDS);
 		*/
 	}
 
 	private void repetitiveNumberOfInstancesHolds(String absoluteServiceName, int expectedNumberOfInstances, long duration, TimeUnit timeunit) {
 		AssertUtils.repetitiveAssertConditionHolds("Expected " + expectedNumberOfInstances + " "+ absoluteServiceName +" instance(s)", 
 				numberOfInstancesRepetitiveCondition(absoluteServiceName, expectedNumberOfInstances), 
 				timeunit.toMillis(duration), 1000);
 		
 	}
 
 	private void stopThreads() {
 		Iterator<HttpRequest> iterator = threads.iterator();
 		while(iterator.hasNext()) {
 		 	iterator.next().close();
 		 	iterator.remove();
 		}
 	}
 
 	private void startThreads() {
 		for(int i = 0 ; i < NUMBER_OF_HTTP_GET_THREADS ; i++){
 			final HttpRequest thread = new HttpRequest(ABSOLUTE_SERVICE_NAME);
 			threads.add(thread);
 			executor.scheduleWithFixedDelay(thread, 0, THROUGHPUT_PER_THREAD, TimeUnit.SECONDS);
 		}
 	}
 	
 	/**
 	 * Wait until petclinic has the specified number of instances
 	 */
 	private void repititiveAssertNumberOfInstances(final String absoluteServiceName, final int expectedNumberOfInstances) {
 		
 		repetitiveAssertTrue("Expected " + expectedNumberOfInstances + " "+ absoluteServiceName +" instance(s)", 
 				numberOfInstancesRepetitiveCondition(absoluteServiceName, expectedNumberOfInstances), 
 				expectedNumberOfInstances * OPERATION_TIMEOUT);
 	}
 
 	private RepetitiveConditionProvider numberOfInstancesRepetitiveCondition(
 			final String absoluteServiceName,
 			final int expectedNumberOfInstances) {
 		return new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				List<String> publicIpAddresses = null;
 				try {
 					publicIpAddresses = getPublicIpAddressesPerProcessingUnitInstance(absoluteServiceName);
 				}
 				catch (Exception e) {
 					Assert.fail("Error while polling number of ip addresses", e);
 				}
 				boolean condition = publicIpAddresses.size() == expectedNumberOfInstances;
 				if (!condition) {
 					LogUtils.log("Expecting " + expectedNumberOfInstances + " " + absoluteServiceName + " instance(s). Instead found " + publicIpAddresses);
 				}
 				return condition;
 				
 			}
 		};
 	}
 
 	/**
 	 * Bombards each tomcat instance with a web request
 	 */
 	public class HttpRequest implements Runnable{
 		
 		private final String absoluteServiceName;
 		private final HttpClient client;
 		private boolean closed = false;
 		
 		public HttpRequest(String absoluteServiceName){
 			this.absoluteServiceName = absoluteServiceName;
 			this.client = new DefaultHttpClient();;
 		}
 		
 		public synchronized void close() {
 			if (!closed) {
 				closed = true;
 				client.getConnectionManager().shutdown();
 			}
 		}
 		
 		@Override
 		public synchronized void run() {
 			if (closed) {
 				return;
 			}
 			List<String> publicIpAddresses = null;
 			try {
 				publicIpAddresses = getPublicIpAddressesPerProcessingUnitInstance(absoluteServiceName);
 				for (String publicIpAddress : publicIpAddresses) {
 					String petclinicHomePage = "http://"+publicIpAddress + ":"+TOMCAT_PORT + "/petclinic-mongo";
 					final URL petclinicHomePageUrl = new URL(petclinicHomePage);
 					final HttpGet get = new HttpGet(petclinicHomePageUrl.toURI());
 
 					final HttpResponse response = client.execute(get);
 					try {
 						Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
 					}
 					finally {
 						EntityUtils.consume(response.getEntity());
 						LogUtils.log("PING " + absoluteServiceName + " " + publicIpAddress + " success.");
 					}
 				}
 			}
 			catch (Throwable t) {
 				LogUtils.log("Failed to PING petclinic website",t);
 				//rethrowing the exception would have aborted the scheduler
 			}
 		}
 	}
 	
 	
 	private static final ObjectMapper PROJECT_MAPPER = new ObjectMapper();
 	
 	/**
 	 * Converts a json String to a Map<String, Object>.
 	 * 
 	 * @param response
 	 *            a json-format String to convert to a map
 	 * @return a Map<String, Object> based on the given String
 	 * @throws IOException
 	 *             Reporting failure to read or map the String
 	 */
 	private static Map<String, Object> jsonToMap(final String response) throws IOException {
 		final JavaType javaType = TypeFactory.type(Map.class);
 		return PROJECT_MAPPER.readValue(response, javaType);
 	}
 	
 	protected List<String> getPublicIpAddressesPerProcessingUnitInstance(String absoluteServiceName) throws Exception {
 		
 		List<String> publicIpAddresses = new ArrayList<String>();
 		 
 		final String attrName = "Cloud Public IP";
 		for (String instanceUrl : getInstancesUrls(absoluteServiceName)) {
 			URL publicIpUrl = new URL(instanceUrl +"/ServiceDetailsByServiceId/USM/Attributes/"+attrName.replace(" ","%20"));
 			String publicIpResponse = WebUtils.getURLContent(publicIpUrl);
 			if (publicIpResponse.length() > 0) {
 				Map<String, Object> publicIpMap = jsonToMap(publicIpResponse);
 				String publicIp = (String) publicIpMap.get(attrName);
 				publicIpAddresses.add(publicIp);
 			}
 		}
 		
 		return publicIpAddresses;
 	}
 
 	private List<String> getInstancesUrls(String absoluteServiceName)
 			throws Exception, MalformedURLException, IOException {
 		
 		String restUrl = super.getRestUrl();
 		final String instancesResponse = WebUtils.getURLContent(new URL(restUrl+"/admin/ProcessingUnits/Names/"+absoluteServiceName+"/Instances/"));
 		final Map<String, Object> instances = jsonToMap(instancesResponse);
 		@SuppressWarnings("unchecked")
 		ArrayList<String> instanceUrls = new ArrayList<String>((List<String>)instances.get("Instances-Elements"));
 		
 		//fix for CLOUDIFY-721
 		for (int i =0 ; i < instanceUrls.size() ; i++) {
 			instanceUrls.set(i,  instanceUrls.get(i).replace("/Instances/Instances", "/Instances"));
 		}
 		
 		return instanceUrls;
 	}
 }
