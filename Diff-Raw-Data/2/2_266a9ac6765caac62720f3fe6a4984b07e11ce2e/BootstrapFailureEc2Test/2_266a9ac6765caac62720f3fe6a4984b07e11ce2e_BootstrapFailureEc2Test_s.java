 package test.cli.cloudify.cloud;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.NodeState;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 /**
  * This test makes a bootstrap on ec2 fail by changing the JAVA_HOME path to a bad one in the bootstrap-management.sh file.
  * <p>After the bootstrap fails, the test checks if the management machine was shutdown.
  * 
  * @author nirb
  *
  */
 public class BootstrapFailureEc2Test extends AbstractCloudTest{
 
 	private Ec2CloudService service;
 	private NodeMetadata managementMachine;
 	private long curTestTime;
 	private File originialBootstrapManagement;
 	private File ec2UploadDir;
 	private static final long TIME_TO_TERMINATE_IN_MILLS = 60000;
 
 	@BeforeMethod
 	public void init() throws IOException, InterruptedException {	
 		
 		ec2UploadDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/ec2/upload");
 		originialBootstrapManagement = new File(ec2UploadDir, "bootstrap-management.sh");
		backupAndReplaceOriginalFile(originialBootstrapManagement,SGTestHelper.getSGTestRootDir() + "\\apps\\cloudify\\cloud\\ec2\\bad-bootstrap-management.sh");
 		
 		curTestTime = System.currentTimeMillis();
 		service = new Ec2CloudService();
 		service.setMachinePrefix(this.getClass().getName() + "_" + CloudTestUtils.SGTEST_MACHINE_PREFIX + curTestTime + "_");	
 	}
 	
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void installTest() throws IOException, InterruptedException, RunNodesException{
 		
 		try {
 			service.bootstrapCloud();
 		} catch (AssertionError ae) {
 			super.setService(service);
 		}
 	
 		JcloudsUtils.createContext(service);
 		Set<? extends NodeMetadata> machines = JcloudsUtils.getServersByName(Long.toString(curTestTime));
 		Assert.assertTrue(machines != null);
 		managementMachine = machines.iterator().next();
 				
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				Set<? extends NodeMetadata> machines = JcloudsUtils.getServersByName(Long.toString(curTestTime));
 				managementMachine = machines.iterator().next();
 				return (managementMachine.getState() == NodeState.TERMINATED);
 			}
 		};
 		
 		AssertUtils.repetitiveAssertTrue("management machine was not terminated", condition, TIME_TO_TERMINATE_IN_MILLS);
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void teardown() throws IOException {
 		JcloudsUtils.closeContext();
 		
 		try {
 			service.teardownCloud();			
 		}
 		catch (Throwable e) {
 			LogUtils.log("caught an exception while tearing down ec2", e);
 			sendTeardownCloudFailedMail("ec2", e);
 		}
 		
 		restoreOriginalBootstrapManagementFile();
 	}
 	
 	private File backupAndReplaceOriginalFile(File originalFile, String replacementFilePath) throws IOException {
 		// replace the default originalFile with a different one
 		// first make a backup of the original file
 		File backupFile = new File(originalFile.getAbsolutePath() + ".backup");
 		FileUtils.copyFile(originalFile, backupFile);
 
 		// copy replacement file to upload dir as the original file's name
 		File replacementFile = new File(replacementFilePath);
 
 		FileUtils.deleteQuietly(originalFile);
 		File newOriginalFile = new File(originalFile.getParent(), originalFile.getName());
 		FileUtils.copyFile(replacementFile, newOriginalFile);
 		
 		return newOriginalFile;
 	}
 	
 	private void restoreOriginalBootstrapManagementFile() throws IOException {
 		originialBootstrapManagement.delete();
 		File backupStartManagementFile = new File(ec2UploadDir, "bootstrap-management.sh.backup");
 		FileUtils.moveFile(backupStartManagementFile, originialBootstrapManagement);
 
 	}
 	
 }
