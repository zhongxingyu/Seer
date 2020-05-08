 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2;
 
 import java.io.File;
 
 import org.cloudifysource.quality.iTests.framework.utils.ScriptUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractExamplesTest;
 import org.eclipse.jgit.api.Git;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class Ec2GitApplicationsTest extends AbstractExamplesTest {
 
 	private static String localGitRepoPath;
 	
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 		
	    localGitRepoPath = ScriptUtils.getBuildPath() + "/git-recipes";
 	    
 	    if (!new File(localGitRepoPath).exists()) {
 	    	String remotePath = "https://github.com/CloudifySource/cloudify-recipes.git";
 	    	
 	    	Git.cloneRepository()
 	    			.setURI(remotePath)
 	    			.setDirectory(new File(localGitRepoPath))
 	    			.call();	    	
 	    }	    
 	}
 	
 	//should work
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testComputers() throws Exception {
 		super.testComputers(localGitRepoPath + "/apps");
 	}
 
 	//should work
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testBabies() throws Exception {
 		super.testBabies(localGitRepoPath + "/apps");
 	}
 
 	//fails
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
 	public void testBiginsights() throws Exception {
 		super.testBiginsights(localGitRepoPath + "/apps");
 	}
 
 	//should work
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testPetclinicJboss() throws Exception {
 		super.testPetclinicJboss(localGitRepoPath + "/apps");
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testLamp() throws Exception {
 		super.testLamp(localGitRepoPath + "/apps");
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testMasterSlave() throws Exception {
 		super.testMasterSlave(localGitRepoPath + "/apps");
 	}
 
 	//needs configuration to work.
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
 	public void testPetclinicWas() throws Exception {
 		super.testPetclinicWas(localGitRepoPath + "/apps");
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testStorm() throws Exception {
 		super.testStorm(localGitRepoPath + "/apps");
 	}
 
 	//should work
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void testTravelLb () throws Exception {
 		super.testTravelLb(localGitRepoPath + "/apps");
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 
         //not deleting for debug
//		try {
//			FileUtils.deleteDirectory(new File(localGitRepoPath));
//		} catch (final Exception e) {
//			LogUtils.log("Failed deleting directory : " + localGitRepoPath);
//		}
 	}
 }
