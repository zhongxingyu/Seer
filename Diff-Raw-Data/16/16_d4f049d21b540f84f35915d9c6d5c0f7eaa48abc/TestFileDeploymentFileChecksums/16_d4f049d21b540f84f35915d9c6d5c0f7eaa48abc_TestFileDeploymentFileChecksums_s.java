 package example.deploy.hotdeploy.file;
 
 import java.io.File;
 
public class TestFileDeploymentFileChecksums extends ChecksumTestCase{ 
     
     @Override
     protected DeploymentFile getFile(String fileName) {
         return new FileDeploymentFile(
                 new File(EQUAL_FILES_DIRECTORY + fileName));
 		}
 }
