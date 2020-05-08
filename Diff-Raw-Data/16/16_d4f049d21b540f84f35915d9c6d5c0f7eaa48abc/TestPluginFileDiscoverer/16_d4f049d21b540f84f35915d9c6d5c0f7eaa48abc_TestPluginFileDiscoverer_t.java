 package example.deploy.hotdeploy.discovery;
 
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import example.deploy.hotdeploy.file.DeploymentFile;
 import example.deploy.hotdeploy.file.JarDeploymentFile;
 
 public class TestPluginFileDiscoverer extends TestCase {
 	public void testFilesFound() throws Exception {
 		List<DeploymentFile> files = new PluginFileDiscoverer()
 				.getFilesToImport();
 
 		Assert.assertEquals(3, files.size());
 
 		Assert.assertEquals("bootstrap.xml",
 				((JarDeploymentFile) files.get(0)).getNameWithinJar());
 		Assert.assertEquals("otherplugincontent.xml",
 				((JarDeploymentFile) files.get(1)).getNameWithinJar());
 		Assert.assertEquals("content/plugincontentindirectory.xml",
 				((JarDeploymentFile) files.get(2)).getNameWithinJar());
 	}
 }
