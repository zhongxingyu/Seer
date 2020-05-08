 package test.cli.cloudify.cloud.scale;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
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
 import org.openspaces.admin.zone.config.AnyZonesConfig;
 import org.openspaces.admin.zone.config.ExactZonesConfig;
 import org.openspaces.admin.zone.config.ExactZonesConfigurer;
 import org.openspaces.admin.zone.config.ZonesConfig;
 import org.testng.Assert;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 
 import com.gigaspaces.internal.utils.StringUtils;
 
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
 public abstract class AbstractScalingRulesCloudTest extends NewAbstractCloudTest {
 
 	private static final String APPLICATION_FOLDERNAME = "petclinic-simple";
 	private static final String APPLICATION_NAME = "petclinic";
 	private static final String TOMCAT_SERVICE_NAME = "tomcat";
 	
 	private static final int NUMBER_OF_HTTP_GET_THREADS = 10;
 	private static final int THROUGHPUT_PER_THREAD = 1;
 	private static final int TOMCAT_PORT = 8080;
 
 	protected ScheduledExecutorService executor;
 	private final List<HttpRequest> threads = new ArrayList<HttpRequest>();
 
 	public void startExecutorService() {	
 		executor = Executors.newScheduledThreadPool(NUMBER_OF_HTTP_GET_THREADS);
 	}
 
 	public void shutdownExecutorAndScanForLeakedAgentNodes() {
 		if (executor != null) {
 			executor.shutdownNow();
 		}
 
 		super.scanAgentNodesLeak();
 	}
 	
 
 	public void testPetclinicSimpleScalingRules() throws Exception {		
 		try {
 			LogUtils.log("installing application " + getApplicationName());
 
 			final String applicationPath = getApplicationPath();
 			installApplicationAndWait(applicationPath, getApplicationName());
 
 			repititiveAssertNumberOfInstances(getAbsoluteServiceName(), new AnyZonesConfig(), 1);
 
 
 			// increase web traffic, wait for scale out
 			LogUtils.log("starting threads");
 			startThreads();
 			repititiveAssertNumberOfInstances(getAbsoluteServiceName(), new AnyZonesConfig(), 2);
 
 			LogUtils.log("stopping threads");
 			// stop web traffic, wait for scale in
 			stopThreads();
 			repititiveAssertNumberOfInstances(getAbsoluteServiceName(), new AnyZonesConfig(),  1);
 
 			LogUtils.log("starting threads");
 			// Try to start a new machine and then cancel it.
 			startThreads();
 			executor.schedule(new Runnable() {
 
 				@Override
 				public void run() {
 					stopThreads();
 
 				}
 			}, 60, TimeUnit.SECONDS);
 			repetitiveNumberOfInstancesHolds(getAbsoluteServiceName(), new AnyZonesConfig(), 1, 500, TimeUnit.SECONDS);
 		} finally {
 			uninstallApplicationAndWait(getApplicationName());
 		}
 	}
 
 	protected String getApplicationPath() {
 		return ScriptUtils.getBuildPath() + "/recipes/apps/" + APPLICATION_FOLDERNAME;
 	} 
 
 	protected void repetitiveNumberOfInstancesHolds(String absoluteServiceName, ZonesConfig zones, int expectedNumberOfInstances, long duration, TimeUnit timeunit) {
 		AssertUtils.repetitiveAssertConditionHolds("Expected " + expectedNumberOfInstances + " "+ absoluteServiceName +" instance(s)", 
 				numberOfInstancesRepetitiveCondition(absoluteServiceName, zones, expectedNumberOfInstances), 
 				timeunit.toMillis(duration), 1000);
 
 	}
 
 	protected void stopThreads() {
 		Iterator<HttpRequest> iterator = threads.iterator();
 		while(iterator.hasNext()) {
 			iterator.next().close();
 			iterator.remove();
 		}
 	}
 
 	protected void startThreads() {
 		for(int i = 0 ; i < NUMBER_OF_HTTP_GET_THREADS ; i++){
 			final HttpRequest thread = new HttpRequest(getAbsoluteServiceName());
 			threads.add(thread);
 			executor.scheduleWithFixedDelay(thread, 0, THROUGHPUT_PER_THREAD, TimeUnit.SECONDS);
 		}
 	}
 	
 	protected void startThreads(ZonesConfig zones) {
 		for(int i = 0 ; i < NUMBER_OF_HTTP_GET_THREADS ; i++){
 			final HttpRequest thread = new HttpRequest(getAbsoluteServiceName());
 			thread.setZones(zones);
 			threads.add(thread);
 			executor.scheduleWithFixedDelay(thread, 0, THROUGHPUT_PER_THREAD, TimeUnit.SECONDS);
 		}
 	}
 
 	/**
 	 * Wait until petclinic has the specified number of instances
 	 */
 	protected void repititiveAssertNumberOfInstances(final String absoluteServiceName, ZonesConfig zones, final int expectedNumberOfInstances) {
 
 		repititiveAssertNumberOfInstances(absoluteServiceName, zones, expectedNumberOfInstances, OPERATION_TIMEOUT * expectedNumberOfInstances * 3, TimeUnit.MILLISECONDS);
 	}
 	
 	protected void repititiveAssertNumberOfInstances(final String absoluteServiceName, ZonesConfig zones, final int expectedNumberOfInstances, long timeout, TimeUnit timeunit) {
 
		repetitiveAssertTrue("Failed finding " + expectedNumberOfInstances + " for " + absoluteServiceName + " in zones " + zones.getZones() + " . waited " + expectedNumberOfInstances * OPERATION_TIMEOUT * 3, 
 				numberOfInstancesRepetitiveCondition(absoluteServiceName, zones, expectedNumberOfInstances), 
 				timeunit.toMillis(timeout));
 	}
 
 	private RepetitiveConditionProvider numberOfInstancesRepetitiveCondition(
 			final String absoluteServiceName, final ZonesConfig zones,
 			final int expectedNumberOfInstances) {
 		return new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				List<InstanceDetails> instancesDetails = null;
 				try {
 					instancesDetails = getInstancesDetails(absoluteServiceName, zones);
 				}
 				catch (Exception e) {
 					Assert.fail("Error while polling number of ip addresses", e);
 				}
 				boolean condition = instancesDetails.size() == expectedNumberOfInstances;
 				if (!condition) {
 					LogUtils.log("Expecting " + expectedNumberOfInstances + " " + absoluteServiceName + " instance(s) that satisfies zones " + zones.getZones() + ". Instead found " + instancesDetails);
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
 		private ZonesConfig zones = new AnyZonesConfig(); // to get all instances if no zone specified
 
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
 		
 		public void setZones(ZonesConfig zones) {
 			this.zones = zones;
 		}
 
 		@Override
 		public synchronized void run() {
 			if (closed) {
 				return;
 			}
 			List<InstanceDetails> instancesDetails = null;
 			try {
 				instancesDetails = getInstancesDetails(absoluteServiceName, zones);
 				assertTrue("Could not retrieve public ip addresses of " + absoluteServiceName, !instancesDetails.isEmpty());
 				// choose the instance with the lowest zone
 				Collections.sort(instancesDetails, new Comparator<InstanceDetails>(){
 
 					@Override
 					public int compare(InstanceDetails o1, InstanceDetails o2) {
 						String zones1ToString = o1.getAgentZones().toString();
 						String zones2ToString = o2.getAgentZones().toString();
 						return zones1ToString.compareTo(zones2ToString);
 					}});
 				URL publicIpAddress = instancesDetails.get(0).getPublicIp();
 				final URL petclinicHomePageUrl = new URL(publicIpAddress.getProtocol(),publicIpAddress.getHost(),TOMCAT_PORT,"/petclinic");
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
 	
 	public class InstanceDetails {
 		private final URL publicIp;
 		private final ExactZonesConfig agentZones;
 		
 		public InstanceDetails(URL publicIp, ExactZonesConfig agentZones) {
 			this.agentZones = agentZones;
 			this.publicIp = publicIp;
 		}
 
 		public URL getPublicIp() {
 			return publicIp;
 		}
 
 		public ExactZonesConfig getAgentZones() {
 			return agentZones;
 		}
 
 		@Override
 		public String toString() {
 			return "InstanceDetails [publicIp=" + publicIp + ", agentZones="
 					+ agentZones + "]";
 		}
 		
 		
 	}
 	
 	protected List<InstanceDetails> getInstancesDetails(String absoluteServiceName, ZonesConfig zones) throws Exception {
 		
 		List<InstanceDetails> instancesDetails = new ArrayList<InstanceDetails>();
 
 		final String publicIpAttrName = "Cloud Public IP";
 		final String zonesAttrName = "zones";
 		for (String instanceUrl : getInstancesUrls(absoluteServiceName)) {
 			
 			URL publicIpUrl = new URL(instanceUrl +"/ServiceDetailsByServiceId/USM/Attributes/"+publicIpAttrName.replace(" ","%20"));
 			String publicIpResponse = WebUtils.getURLContent(publicIpUrl);
 			URL publicIp = null;
 			if (publicIpResponse.length() > 0) {
 				Map<String, Object> publicIpMap = jsonToMap(publicIpResponse);
 				publicIp = new URL("http://"+ ((String)publicIpMap.get(publicIpAttrName)));
 			}
 			
 			URL agentZonesUrl = new URL(instanceUrl +"/GridServiceContainer/GridServiceAgent/ExactZones/Properties/"+zonesAttrName);
 			String agentZonesResponse = WebUtils.getURLContent(agentZonesUrl);
 			ExactZonesConfig agentZones = null;
 			String[] instanceZones = null;
 			if (agentZonesResponse.length() > 0) {
 				Map<String, Object> agentZoneMap = jsonToMap(agentZonesResponse);
 				instanceZones = StringUtils.commaDelimitedListToStringArray(
 						(String) agentZoneMap.get(zonesAttrName));
 				agentZones = 
 						new ExactZonesConfigurer()
 						.addZones(
 								instanceZones)
 						.create();
 			}
 			if (publicIp != null && agentZones != null && agentZones.isStasfies(zones)) {
 				instancesDetails.add(new InstanceDetails(publicIp,agentZones));
 			}
 		}
 
 		return instancesDetails;
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
 
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 	@Override
 	protected void customizeCloud() {
 
 	}
 
 	protected String getApplicationName() {
 		return APPLICATION_NAME;
 	}
 	
 	protected String getAbsoluteServiceName() {
 		return ServiceUtils.getAbsolutePUName(getApplicationName(), TOMCAT_SERVICE_NAME);
 	}
 }
