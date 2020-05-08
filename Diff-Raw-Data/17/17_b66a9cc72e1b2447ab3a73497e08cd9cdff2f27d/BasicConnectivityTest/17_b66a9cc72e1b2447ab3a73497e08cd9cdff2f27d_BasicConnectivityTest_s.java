 package md.fusionworks.hadoop;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.yarn.client.api.YarnClient;
 import org.apache.hadoop.yarn.exceptions.YarnException;
 import org.junit.Test;
 
 public class BasicConnectivityTest {

 	
 	@Test
 	public void test() throws YarnException, IOException {
 		YarnClient client = YarnClient.createYarnClient();  
		client.init(new Configuration());
 		client.start();
 		client.createApplication();
		
 	}
 }
