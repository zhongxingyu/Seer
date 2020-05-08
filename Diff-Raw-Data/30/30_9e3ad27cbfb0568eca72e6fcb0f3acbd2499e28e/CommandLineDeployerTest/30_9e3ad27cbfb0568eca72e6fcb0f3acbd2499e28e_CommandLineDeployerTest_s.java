 package no.f12.agiledeploy.deployer;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import net.sourceforge.argparse4j.inf.ArgumentParser;
 import net.sourceforge.argparse4j.inf.ArgumentParserException;
 
 import org.junit.Test;
 
 public class CommandLineDeployerTest extends AbstractFileSystemTest {
 
 	String environment = "test";
 
 	CommandLineDeployer deployer;
 
 	String[] springArgs = new String[] { "-e", environment, "-g", "org.springframework", "-a", "spring-core", "-v", "2.5.6", "-p", "jar" };
 
 	File workingDirectory;
 	File unpackDir;
 	File downloadedFile;
 
 	@Test(expected = ArgumentParserException.class)
 	public void shouldGiveErrorIfNotEnoughParameters() throws ArgumentParserException {
 		ArgumentParser parser = CommandLineDeployer.createArgumentParser();
 		parser.parseArgs(new String[] {});
 	}
 
 	private void createDeployerAndSetupDir() {
 		deployer = new CommandLineDeployer("classpath:spring/deployer-test-applicationContext.xml");
 
 		workingDirectory = deployer.getWorkingDirectory();
 		unpackDir = new File(workingDirectory, "spring-core/" + environment + "/current");
 		downloadedFile = new File(workingDirectory, "spring-core-2.5.6.jar");
 	}
 	
 	@Test
 	public void shouldStartUpGivenCorrectParameters() {
 		createDeployerAndSetupDir();
 		executeWithSpringStandard();
 		assertTrue(unpackDir.exists());
 	}
 
 	@Test
 	public void shouldCleanOutOldStuffFromDirectoryBeforeDeploy() {
 		createDeployerAndSetupDir();
 
 		File extraDir = new File(unpackDir, "extra");
 		extraDir.mkdirs();
 		assertTrue(extraDir.exists());
 
 		executeWithSpringStandard();
 
 		assertTrue(!extraDir.exists());
 	}
 
 	private void executeWithSpringStandard() {
 		deployer.execute(springArgs);
 	}
 
 }
