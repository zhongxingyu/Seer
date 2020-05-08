 package org.cagrid.workflow.helper.tests.system;
 
 import gov.nih.nci.cagrid.testing.system.deployment.ContainerException;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainer;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerFactory;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerType;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.DeployServiceStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.StartContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.UnpackContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.story.ServiceStoryBase;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import junit.framework.Assert;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.apache.axis.message.addressing.EndpointReference;
 import org.apache.axis.types.URI;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.log4j.PropertyConfigurator;
 import org.cagrid.workflow.helper.tests.system.steps.CreateTestWorkflowsStep;
 
 
 public class BasicWorkflowServicesTest extends ServiceStoryBase {
     private EndpointReference helperEPR = null;
     private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
     private static final String HELPER_PATH_IN_CONTAINER = "/cagrid/WorkflowHelper";
 
 
     public BasicWorkflowServicesTest(ServiceContainer container) {
         super(container);
         /*PropertyConfigurator.configure("." + File.separator + "conf" +
          File.separator
         + "log4j.properties"); // */
     }
     
     public BasicWorkflowServicesTest() {
         super();
         try {
             ServiceContainer cont = ServiceContainerFactory.createContainer(ServiceContainerType.GLOBUS_CONTAINER);
             setContainer(cont);
         } catch (IOException e) {
 
             e.printStackTrace();
             Assert.fail();
         }
         /*PropertyConfigurator.configure("." + File.separator + "conf" +
          File.separator
         + "log4j.properties"); // */
     }
 
 
     @Override
     public String getDescription() {
         return "Basic Wokflows Testing";
     }
 
 
     @Override
     protected Vector steps() {
 
         System.out.println("BEGIN DeployWorkflowServicesTest");
 
         Vector steps = new Vector();
 
         // Set workflow services' directories
         String tests_basedir = System.getProperty("resources.dir") + File.separator;
         if (tests_basedir == null) {
             System.err.println("ERROR: System property 'resources.dir' not set");
             return null;
         }
         File services_dirs[] = new File[]{
               /*  new File(tests_basedir + "Service1"),
                 new File(tests_basedir + "Service2"),
                 new File(tests_basedir + "Service3"),
                 new File(tests_basedir + "Service4"),
                 new File(tests_basedir + "Service5"), // TODO REMOVE ME NOW!!*/ 
                 new File(tests_basedir + "ReceiveArrayService"),
                 new File(tests_basedir + "CreateArrayService"),
                //new File(tests_basedir + "ValidateOutputsService"),
                 new File(".." + File.separator + ".." + File.separatorChar + ".." + File.separator + "incubator"
                     + File.separator + "projects" + File.separator + "workflowHelper")};
 
         // Create container
         System.out.println("Adding unpack task");
         steps.add(new UnpackContainerStep(getContainer()));
 
         // Deploy each service
         System.out.println("Adding deploy services task");
         for (int i = 0; i < services_dirs.length; i++) {
             String curr_service_dir = null;
             try {
                 curr_service_dir = services_dirs[i].getCanonicalPath();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             System.out.println("Adding deploy for service in directory '" + curr_service_dir + "'");
             steps.add(new DeployServiceStep(getContainer(), curr_service_dir));
         }
         System.out.println("END DeployWorkflowServicesTest");
 
         // Start a container before anything else
         StartContainerStep start_step = new StartContainerStep(getContainer());
         steps.add(start_step);
 
         // Configure the workflows that are part of the system test
         try {
             URI helper_uri = getContainer().getContainerBaseURI();
             String container_base_url = "http://"+ helper_uri.getHost() + ':' + helper_uri.getPort(); 
             helper_uri.appendPath(HELPER_PATH_IN_CONTAINER);
             this.helperEPR = new EndpointReference(helper_uri);
             CreateTestWorkflowsStep create_workflows = new CreateTestWorkflowsStep(this.helperEPR, container_base_url);
             steps.add(create_workflows);
         } catch (MalformedURIException e) {
             e.printStackTrace();
             Assert.fail();
         }
 
         return steps;
     }
 
 
     public boolean storySetUp() {
        
         return true;
     }
 
 
     public void storyTearDown() {
         if (getContainer() != null) {
             try {
             	
             	System.out.println("Stopping container after executing tests");
                 getContainer().stopContainer();
                 getContainer().deleteContainer();
             } catch (ContainerException e) {
                 e.printStackTrace();
                 Assert.fail();
             } 
         } else
             System.err.println("getContainer returned null. Have the container been set?");
         // */
         
     }
     
     /**
      * Convenience method for running all the Steps in this Story.
      */
     public static void main(String args[]) {
    
     	try{
     		TestRunner runner = new TestRunner();
     		TestResult result = runner.doRun(new TestSuite(BasicWorkflowServicesTest.class));
     		System.exit(result.errorCount() + result.failureCount());
     	}
     	catch(Throwable t){
     		t.printStackTrace();
     		Assert.fail(t.getMessage());
     	}    	   
     	
     }
 
 }
