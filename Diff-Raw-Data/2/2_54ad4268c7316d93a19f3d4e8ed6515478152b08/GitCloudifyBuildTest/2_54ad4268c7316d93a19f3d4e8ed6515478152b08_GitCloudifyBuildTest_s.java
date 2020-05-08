 package test.cli.cloudify.github;
 
 import framework.tools.SGTestHelper;
 import framework.utils.GitUtils;
 import framework.utils.LogUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.io.filefilter.RegexFileFilter;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.Test;
 import test.cli.cloudify.AbstractLocalCloudTest;
 import test.cli.cloudify.CommandTestUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 
 public class GitCloudifyBuildTest extends AbstractLocalCloudTest{
     private File gitDir = null;
 
     @Test(timeOut = 100000000, groups = "1", enabled = true)
     public void test() throws IOException, InterruptedException {
         String commandOutput = null;
         String url = "https://github.com/CloudifySource/cloudify.git";
         gitDir = new File(SGTestHelper.getBuildDir() + "/git/");
 
         GitUtils.pull(url, gitDir);
         String cloudifyFolder = SGTestHelper.getBuildDir() + "/git/cloudify/";
 
         LogUtils.log("building Cloudify...");
        commandOutput = CommandTestUtils.runLocalCommand("ant -buildfile=" + cloudifyFolder + "build.xml cloudify.zip", true, false);
         Assert.assertFalse(commandOutput.contains("BUILD FAILED"));
 
         Collection<File> files = FileUtils.listFiles(
                 new File(cloudifyFolder + "/releases/"),
                 new RegexFileFilter("gigaspaces-cloudify.*.zip"),
                 DirectoryFileFilter.DIRECTORY
         );
         Assert.assertEquals(1, files.size());
         Map<String, String> insideZipFiles = new HashMap<String, String>();
 
         FileInputStream fis = new FileInputStream(files.iterator().next());
         // this is where you start, with an InputStream containing the bytes from the zip file
         ZipInputStream zis = new ZipInputStream(fis);
         ZipEntry entry;
         // while there are entries I process them
         while ((entry = zis.getNextEntry()) != null) {
             insideZipFiles.put(entry.getName(), entry.getName());
             System.out.println("entry: " + entry.getName() + ", " + entry.getSize());
             // consume all the data from this entry
             while (zis.available() > 0)
                 zis.read();
         }
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/bin/cloudify.bat"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/bin/cloudify.sh"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/docs/cloudify-javadoc.zip"));
 
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/lib/platform/usm/usm.jar"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/lib/required/dsl.jar"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/recipes/apps/"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/recipes/services/"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/tools/cli/cli.jar"));
         Assert.assertNotNull(insideZipFiles.get("gigaspaces-cloudify-2.1.1-m1/tools/rest/rest.war"));
 
 
     }
 
     @AfterMethod
     public void afterTest() {
         try {
             FileUtils.forceDelete(gitDir);
         } catch (IOException e) {
             LogUtils.log("Failed to delete git Cloudify folder", e);
         }
         super.afterTest();
     }
 
 }
