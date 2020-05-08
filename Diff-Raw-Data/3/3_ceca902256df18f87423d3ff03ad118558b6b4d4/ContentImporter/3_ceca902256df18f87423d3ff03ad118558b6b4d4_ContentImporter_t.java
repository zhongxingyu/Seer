 package com.polopoly.ps.test;
 
 import static java.util.logging.Level.INFO;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.framework.Assert;
 
 import com.polopoly.cm.policy.PolicyCMServer;
 import com.polopoly.ps.hotdeploy.deployer.DefaultSingleFileDeployer;
 import com.polopoly.ps.hotdeploy.deployer.DeploymentResult;
 import com.polopoly.ps.hotdeploy.deployer.FatalDeployException;
 import com.polopoly.ps.hotdeploy.deployer.SingleFileDeployer;
 import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
 import com.polopoly.ps.hotdeploy.file.FileDeploymentFile;
 import com.polopoly.ps.hotdeploy.state.CouldNotUpdateStateException;
 import com.polopoly.ps.hotdeploy.state.DirectoryState;
 import com.polopoly.ps.hotdeploy.state.DirectoryStateFetcher;
 import com.polopoly.ps.hotdeploy.state.DirectoryWillBecomeJarDirectoryState;
 import com.polopoly.ps.test.client.PolopolyClientContext;
 
 public class ContentImporter {
 	private static final Logger LOGGER = Logger.getLogger(ContentImporter.class
 			.getName());
 
 	public void importTestContent(
 			Class<? extends AbstractIntegrationTest> testClass,
 			PolopolyClientContext context) {
 		FileDeploymentFile deploymentFile = null;
 
 		try {
 			PolicyCMServer server = context.getPolicyCMServer();
 
 			String resourceName = "/content/" + testClass.getName()
 					+ ".content";
 
 			URL resource = testClass.getResource(resourceName);
 
 			if (resource == null) {
 				LOGGER.log(Level.FINE, resourceName + " did not exist.");
 				return;
 			}
 
 			deploymentFile = getDeploymentFile(resource);
 
 			if (deploymentFile == null) {
 				return;
 			}
 
 			DirectoryState directoryState = getDirectoryState(server,
 					deploymentFile);
 
 			if (!directoryState.hasFileChanged(deploymentFile)) {
 				LOGGER.log(Level.INFO, resourceName
 						+ " had not changed and did not need importing.");
 				return;
 			}
 
 			SingleFileDeployer singleFileDeployer = new DefaultSingleFileDeployer(server, new DeploymentResult());
 
 			LOGGER.log(Level.INFO, "Importing " + resourceName + ".");
 
 			singleFileDeployer.prepare();
 
 			boolean result = singleFileDeployer
 					.importAndHandleException(deploymentFile);
 
 			directoryState.reset(deploymentFile, !result);
 			directoryState.persist();
 
 			if (!result) {
 				Assert.fail("Could not import test content " + resourceName
 						+ ". See previous logging message.");
 			} else {
 				LOGGER.log(INFO, "Imported " + resourceName + ".");
 			}
 		} catch (FatalDeployException e) {
 			String msg = "While importing " + deploymentFile + ": "
 					+ e.getMessage();
 
 			LOGGER.log(Level.WARNING, msg, e);
 			Assert.fail(msg);
 		} catch (ParserConfigurationException e) {
 			LOGGER.log(Level.WARNING, e.getMessage(), e);
 		} catch (CouldNotUpdateStateException e) {
 			LOGGER.log(Level.WARNING, e.getMessage(), e);
 		}
 	}
 
 	private DirectoryState getDirectoryState(PolicyCMServer server,
 			FileDeploymentFile deploymentFile) {
 		DirectoryState directoryState = new DirectoryStateFetcher(server)
 				.getDirectoryState();
 
 		List<FileDeploymentDirectory> deploymentDirectories = new ArrayList<FileDeploymentDirectory>(
 				1);
 
 		deploymentDirectories.add(new FileDeploymentDirectory(deploymentFile
 				.getFile().getParentFile().getParentFile()));
 
 		// this is something of a hack obviously. how could we find out the
 		// version in a nice way?
 		String jarFileName = new File(".").getAbsoluteFile().getParentFile()
 				.getName()
 				+ "-1.0-SNAPSHOT.jar";
 
 		directoryState = new DirectoryWillBecomeJarDirectoryState(
 				directoryState, deploymentDirectories, jarFileName);
 
 		return directoryState;
 	}
 
 	private FileDeploymentFile getDeploymentFile(URL resource) {
 		FileDeploymentFile deploymentFile = null;
 
 		if (resource.getProtocol().equals("file")) {
 			File file = new File(resource.getFile());
 
 			deploymentFile = new FileDeploymentFile(file);
 		}
 		// we ignore JAR resources since we only want to automatically
 		// import content when running from Eclipse anyway (in which
 		// case we are not running against JAR files).
 
 		return deploymentFile;
 	}
 
 }
