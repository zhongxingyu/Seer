 package md.fusionworks.hadoop;
 
 import java.io.IOException;
 
import junit.framework.Assert;

 import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.applications.distributedshell.ApplicationMaster;
import org.apache.hadoop.yarn.applications.distributedshell.Client;
 import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import org.apache.hadoop.yarn.exceptions.YarnException;
 import org.junit.Test;
 
 public class BasicConnectivityTest {
 	
	protected static Configuration conf = new YarnConfiguration();

 	@Test
 	public void test() throws YarnException, IOException {
 		YarnClient client = YarnClient.createYarnClient();  
		client.init(conf);
 		client.start();
 		client.createApplication();
 	}
 }
