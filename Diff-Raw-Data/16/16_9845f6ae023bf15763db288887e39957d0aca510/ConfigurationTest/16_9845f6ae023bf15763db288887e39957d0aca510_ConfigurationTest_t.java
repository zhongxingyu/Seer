 package mon4h.collector.configuration;
 
 import java.util.Properties;
 
 import org.junit.Test;
 
 public class ConfigurationTest {
 
 	@Test
 	public void testConfiguration() {
		Properties prop = Configuration.getProperties();
 		String address = prop.getProperty("restful.server.address");
 		assert(address != null);
 	}
 }
