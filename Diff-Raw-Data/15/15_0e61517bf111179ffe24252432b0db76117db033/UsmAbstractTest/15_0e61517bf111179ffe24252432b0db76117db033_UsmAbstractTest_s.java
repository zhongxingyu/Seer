 package test.usm;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTest;
 
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 
 import framework.utils.AdminUtils;
 
 public class UsmAbstractTest extends AbstractTest{
 	
     public final static long DEFAULT_TEST_TIMEOUT = 7 * 60 * 1000;
     
 	public static final String SIMPLE_JAVA = "simplejavaprocess";
 	public static final String SIMPLE_JAVA_SERVICE_FILE_NAME = "simplejava-service.groovy";
 	public static final String LEGIT_FILEWRITTER_JAVA = "filewriterprocess_fileAndRegxFound-service";
 	public static final String WRONG_REGX_FILEWRITTER_JAVA = "filewriterprocess_regxNotFound-service";
 	public static final String WRONG_FILENAME_FILEWRITTER_JAVA = "filewriterprocess_fileNotFound-service";
 	public static final String WRONG_PORTS_JAVA = "portTestingProcess_notAllPortsOpen-service";
 	public static final String LEGIT_PORTS_JAVA = "portTestingProcess_AllPortsOpen-service";
 	
 	public static final int PROCESSING_UNIT_TIMEOUT = 5;
 	
 	public static final String CASSANDRA = "cassandra";
 	public static final String CASSANDRA_SERVICE_FILE_NAME = "cassandra-service.groovy";
 	protected String processName = null;
 	protected String serviceFileName = null;
 	
 	@BeforeMethod
 	@Override
 	public void beforeTest() {
 		super.beforeTest();
 		String testGroups = AdminUtils.getTestGroups();
 		System.setProperty("com.gs.jini_lus.groups", testGroups);
 		processName = SIMPLE_JAVA; //default is simple java
 		serviceFileName = SIMPLE_JAVA_SERVICE_FILE_NAME; // default simple service config
 	}
 	
 	public void setProcessName(String processName) {
 		this.processName = processName;
 	}
 	
 	public void assertZeroPUsForProcess() throws InterruptedException, IOException, PackagingException{
 		ProcessingUnit pu = admin.getProcessingUnits().waitFor(processName, PROCESSING_UNIT_TIMEOUT, TimeUnit.SECONDS);
 		assertEquals(null, pu);
 		
 		USMTestUtils.usmDeploy(processName, serviceFileName);
 		
 		//wait for more than the defined timeout in the groovy conf file.
 		Thread.sleep(40000);
 		pu = admin.getProcessingUnits().waitFor(processName, PROCESSING_UNIT_TIMEOUT, TimeUnit.SECONDS);
 		//We expect to have 0 instance of the process
 		assertTrue(pu.waitFor(0, PROCESSING_UNIT_TIMEOUT, TimeUnit.SECONDS));
 	}
 	
 	public void assertOnePUForProcess() throws InterruptedException, IOException, PackagingException{
 		//Verify that no process with the same name exists.
 		ProcessingUnit pu = admin.getProcessingUnits().waitFor(processName, PROCESSING_UNIT_TIMEOUT, TimeUnit.SECONDS);
 		assertEquals(null, pu);
 		
 		USMTestUtils.usmDeploy(processName, serviceFileName);
 		
 		//wait for more than the defined timeout in the groovy conf file.
 		Thread.sleep(40000);
 		pu = admin.getProcessingUnits().waitFor(processName, PROCESSING_UNIT_TIMEOUT, TimeUnit.SECONDS);
 		//We expect to have one instance of the process
 		assertTrue(pu.waitFor(1, PROCESSING_UNIT_TIMEOUT, TimeUnit.SECONDS));
 	}
 }
