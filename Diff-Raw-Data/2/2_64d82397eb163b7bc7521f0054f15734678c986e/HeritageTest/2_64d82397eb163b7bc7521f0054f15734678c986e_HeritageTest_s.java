 package test.cli.cloudify.recipes.heritage;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.Application;
 import com.gigaspaces.cloudify.dsl.Service;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 
 import test.cli.cloudify.AbstractCommandTest;
 import test.cli.cloudify.CommandTestUtils;
 
 public class HeritageTest extends AbstractCommandTest {
 	
 	private String tomcatParentPath = CommandTestUtils.getPath("apps/USM/usm/tomcatHttpLivenessDetectorPlugin");
 	private String cassandraParent = CommandTestUtils.getPath("apps/USM/usm/cassandra");
 	private Application app;
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void simpleHeritageTest() throws FileNotFoundException, PackagingException, IOException, InterruptedException{
 		String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtended");
 		
		Service tomcatParent = ServiceReader.getServiceFromFile(new File(tomcatParentPath));
 		installApplication(appChildDirPath);
 		Service s1 = app.getServices().get(0);
 		Service s2 = app.getServices().get(1);
 		Service tomcat = s1.getName().equals("tomcat-extend") ? s1 : s2;
 		
 		int tomcatParentPort , tomcatChildPort;
 		tomcatChildPort = tomcat.getNetwork().getPort();
 		tomcatParentPort = tomcatParent.getNetwork().getPort();
 		
 		assertEquals("tomcat port isn't equal to the tomcat's parent port", tomcatChildPort ,tomcatParentPort);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = false)
 	public void overrideTomcatPortTest() throws FileNotFoundException, PackagingException, IOException, InterruptedException{
 		String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtendedTomcatPortOverride");
 		installApplication(appChildDirPath);
 		Service s1 = app.getServices().get(0);
 		Service s2 = app.getServices().get(1);
 		Service tomcat = s1.getName().equals("tomcat") ? s1 : s2;
 		int tomcatChildPort = tomcat.getNetwork().getPort();
 		assertEquals("tomcat's child port was not overriden", 9876, tomcatChildPort);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = false)
 	public void overrideTomcatNumInstancesTest() throws FileNotFoundException, PackagingException, IOException, InterruptedException{
 		String appChildDirPath = CommandTestUtils.getPath("apps/USM/usm/applications/travelExtendedTomcatNumInstancesOverride");
 		installApplication(appChildDirPath);
 		
 		int tomcatInstances = admin.getProcessingUnits().getProcessingUnit("tomcat").getInstances().length;
 		assertEquals("tomcat instances where overriden to be 3", 3, tomcatInstances);
 		
 	}
 	
 	private void installApplication(String appDirPath) throws FileNotFoundException,
 	PackagingException, IOException, InterruptedException {
 	File applicationDir = new File(appDirPath);
 	app = ServiceReader.getApplicationFromFile(applicationDir).getApplication();
 	
 	runCommand("connect " + this.restUrl + ";install-application --verbose " + appDirPath);
 }
 }
