 package test.cli.cloudify;
 
 import static framework.utils.LogUtils.log;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import net.jini.discovery.Constants;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTest;
 
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.shell.StringUtils;
 import com.gigaspaces.cloudify.shell.commands.CLIException;
 import com.gigaspaces.cloudify.shell.rest.ErrorStatusException;
 
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.PortConnectionUtils;
 
 public class AbstractLocalCloudTest extends AbstractTest {
 	
	protected final String DEFAULT_APPLICTION_NAME = "default";
 	protected final int WAIT_FOR_TIMEOUT = 20;
 	private final int HTTP_STATUS_OK = 200;
 	private final int restPort = 8100;
 	protected String restUrl;
 	//Teardown any existing localclouds left from previous tests
 	@BeforeClass
 	public void beforeClass() throws FileNotFoundException, PackagingException, IOException, InterruptedException{
 		LogUtils.log("Tearing-down existing localclouds");
 		runCommand("teardown-localcloud");
 	}
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest(){
 		LogUtils.log("Test Configuration Started: "+ this.getClass());
 		try {
 			LogUtils.log("Performing bootstrap");
 			boolean portOpenBeforeBootstrap =  PortConnectionUtils.isPortOpen("localhost", restPort);
 			assertTrue("port " + restPort +" is open on localhost before rest deployment. will not try to deploy rest"
 					, !portOpenBeforeBootstrap);
 			runCommand("bootstrap-localcloud");
 		} catch (IOException e) {
 			LogUtils.log("Booststrap Failed.");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			LogUtils.log("Booststrap Failed.");
 			e.printStackTrace();
 		}
 		
 		try {
 			this.admin = getAdminWithLocators();
 		} catch (UnknownHostException e1) {
 			LogUtils.log("Could not create admin " + e1);
 			e1.printStackTrace();
 		}
 		assertTrue("Could not find LUS of local cloud", admin.getLookupServices().waitFor(1, WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
 		try {
 			this.restUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8100";
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected String runCommand(String command) throws IOException, InterruptedException {
 		return CommandTestUtils.runCommandAndWait(command);
 	}
 	
 	@Override
 	@AfterMethod
 	public void afterTest(){
 		try {
 			LogUtils.log("Tearing-down localcloud");
 			runCommand("teardown-localcloud");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if (admin != null) {
 	    	try {
 	            DumpUtils.dumpLogs(admin);
 	        } catch (Throwable t) {
 	            log("failed to dump logs", t);
 	        }
 	    }
 	}
 	
 	private Admin getAdminWithLocators() throws UnknownHostException {
 		admin = newAdmin();
 		//Class LocalhostGridAgentBootsrapper defines the locator discovery addresses.
 		String nicAddress = Constants.getHostAddress();
 		//int defaultLusPort = Constants.getDiscoveryPort();
 		AdminFactory factory = new AdminFactory();
 		LogUtils.log("adding locator to admin : " + nicAddress + ":4168");
 		factory.addLocator(nicAddress + ":4168");
 		return factory.createAdmin();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	public void afterClass() throws IOException, InterruptedException{	
 		runCommand("teardown-localcloud");
 	}
 	
 	//This method implementation is used in order to access the admin api
 	//without having to worry about locators issue. 
 	protected Map<String, Object> getAdminData(final String relativeUrl)
 	throws CLIException {
 		final String url = getFullUrl("/admin/" + relativeUrl);
 		LogUtils.log("performing http get to url: " + url);
 		final HttpGet httpMethod = new HttpGet(url);
 		return readHttpAdminMethod(httpMethod);
 	}
 	
 	private String getFullUrl(String relativeUrl) {
 
 		return this.restUrl + relativeUrl;
 	}
 	
 	private Map<String, Object> readHttpAdminMethod(
 			final HttpRequestBase httpMethod) throws CLIException {
 		InputStream instream = null;
 		try {
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			final HttpResponse response = httpClient.execute(httpMethod);
 			if (response.getStatusLine().getStatusCode() != HTTP_STATUS_OK) {
 				LogUtils.log(httpMethod.getURI() + " response code " + response.getStatusLine().getStatusCode());
 				throw new CLIException(response.getStatusLine().toString());
 			}
 			final HttpEntity entity = response.getEntity();
 			if (entity == null) {
 				final ErrorStatusException e = new ErrorStatusException(
 				"comm_error");
 				LogUtils.log(httpMethod.getURI() + " response entity is null", e);
 				throw e;
 			}
 			instream = entity.getContent();
 			final String responseBody = StringUtils.getStringFromStream(instream);
 			LogUtils.log(httpMethod.getURI() + " http get response: " + responseBody);
 			final Map<String, Object> responseMap = jsonToMap(responseBody);
 			return responseMap;
 		} catch (final ClientProtocolException e) {
 			LogUtils.log(httpMethod.getURI() + " Rest api error", e);
 			throw new ErrorStatusException("comm_error", e, e.getMessage());
 		} catch (final IOException e) {
 			LogUtils.log(httpMethod.getURI() + " Rest api error", e);
 			throw new ErrorStatusException("comm_error", e, e.getMessage());
 		} finally {
 			if (instream != null) {
 				try {
 					instream.close();
 				} catch (final IOException e) {
 				}
 			}
 			httpMethod.abort();
 		}
 	}
 	
 	//returns the number of processing unit instances of the specified service
 	protected int getProcessingUnitInstanceCount(String absolutePUName) throws CLIException{
 		String puNameAdminUrl = "processingUnits/Names/" + absolutePUName;
 		Map<String, Object> mongoProcessingUnitAdminData = getAdminData(puNameAdminUrl);
 		return (Integer)mongoProcessingUnitAdminData.get("Instances-Size");
 	}
 	
 	private static Map<String, Object> jsonToMap(final String response)
 	throws IOException {
 		final JavaType javaType = TypeFactory.type(Map.class);
 		ObjectMapper objectMapper = new ObjectMapper();
 		return objectMapper.readValue(response, javaType);
 	}
 	
 }
