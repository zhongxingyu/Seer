 package uk.me.ajmfulcher.fpmplugin;
 
 import static org.junit.Assert.*;
 import java.io.File;
 
 import org.junit.Test;
 
 public class FpmPluginTest {
 	
 	@Test
 	public void testCreate() {
 		String inputType = "gem";
 		String outputType = "deb";
 		String outputDir = "/tmp";
 		String packageName = "json";
 		String packageVersion = "1.8.0";
 		FpmPlugin fpm = new FpmPlugin(inputType,outputType,outputDir,packageName, packageVersion);
 		String[] args = { "-s","gem","-t","deb","-C","/tmp/fpm/base","--workdir","/tmp/fpm/tmp","-v","1.8.0","json" };
 		assertArrayEquals(args,fpm.getArgs());
 	}
 	
 	@Test
 	public void addOptionalArgs() {
 		String inputType = "gem";
 		String outputType = "deb";
 		String outputDir = "/tmp";
 		String packageName = "json";
 		String packageVersion = "1.8.0";
 		FpmPlugin fpm = new FpmPlugin(inputType,outputType,outputDir,packageName, packageVersion);
 		String optionalArgs = "--debug";
 		String[] args = { optionalArgs, "-s","gem","-t","deb","-C","/tmp/fpm/base","--workdir","/tmp/fpm/tmp","-v","1.8.0","json" };
 		fpm.setOptionalArgs(optionalArgs);
 		assertArrayEquals(args,fpm.getArgs());
 	}
 	
 	@Test
 	public void createGemPackage(){
 		String inputType = "gem";
 		String outputType = "deb";
 		String outputDir = targetDir().toString();
 		String packageName = "json";
 		String packageVersion = "1.8.0";
 		FpmPlugin fpm = new FpmPlugin(inputType,outputType,outputDir,packageName, packageVersion);
 		try {
 			fpm.invoke();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		File file = new File(outputDir + "/fpm/rubygem-json_1.8.0_amd64.deb");
 		assertTrue(file.exists());
 	}
 	
 	@Test
 	public void createDirPackage(){
 		String inputType = "dir";
 		String outputType = "deb";
 		String outputDir = targetDir().toString();
 		String packageName = "stuff";
 		String packageVersion = "1.2.3";
 		
 		FpmPlugin fpm = new FpmPlugin(inputType,outputType,outputDir,packageName,packageVersion);
 		
 		// Create some files to test against
 		File testUsrDir = new File(outputDir + "/fpm/base/usr");
 		File testEtcDir = new File(outputDir + "/fpm/base/etc");
 		testUsrDir.mkdir();
 		testEtcDir.mkdir();
 		
 		try {
 			fpm.invoke();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		File file = new File(outputDir + "/fpm/stuff_1.2.3_amd64.deb");
 		assertTrue(file.exists());
 		
 		// Teardown
 		testUsrDir.delete();
 		testEtcDir.delete();
 	}
 	
 	@Test
 	public void getTypeArgsForDir(){
 		String inputType = "dir";
 		String outputType = "deb";
 		String outputDir = targetDir().toString();
 		String packageName = "json"; // This should be deprecated later on
 		String packageVersion = "1.8.0";
 		
 		// Create some files to test against
 		File testUsrDir = new File(targetDir() + "/fpm/base/usr");
 		File testEtcDir = new File(targetDir() + "/fpm/base/etc");
 		testUsrDir.mkdir();
 		testEtcDir.mkdir();
 		
 		FpmPlugin fpm = new FpmPlugin(inputType,outputType,outputDir,packageName, packageVersion);
 
 		// And the response we expect
		String[] response = { "usr", "etc" };
 		assertArrayEquals(fpm.getTypeArgs(inputType),response);
 		
 		// Teardown
 		testUsrDir.delete();
 		testEtcDir.delete();
 	}
 	
 	@Test
 	public void getTypeArgsForOther(){
 		String inputType = "gem";
 		String outputType = "deb";
 		String outputDir = targetDir().toString();
 		String packageName = "json"; // This should be deprecated later on
 		
 		// And the response we expect
 		String[] response = { "json" };
 		
 		String packageVersion = "1.8.0";
 		FpmPlugin fpm = new FpmPlugin(inputType,outputType,outputDir,packageName, packageVersion);
 		assertArrayEquals(fpm.getTypeArgs(inputType),response);
 	}
 	
 	public File targetDir(){
 	  String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
 	  File targetDir = new File(relPath + "../../target");
 	  if(!targetDir.exists()) {
 	    targetDir.mkdir();
 	  }
 	  return targetDir;
 	}
 
 }
