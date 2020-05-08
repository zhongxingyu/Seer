 package io.seqware.queryengine.sandbox.testing;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 
 import io.seqware.queryengine.sandbox.testing.plugins.*;
 
 public class FeaturePluginRunnerTest {
 	@Test
 	public void runPlugin() throws IOException{
 		FeaturePluginRunner runMe = new FeaturePluginRunner();
 		
 		File jsonQuery = new File("src/main/resources/testdata/query.json");
 		InputStream is = new FileInputStream(jsonQuery);
 		String jsonTxt = IOUtils.toString(is);
 		
 		
		runMe.getFilteredFiles("/src/test/resources/PluginData", jsonTxt, System.getProperty("user.home") + "Filtered");

 	}
 	
 }
