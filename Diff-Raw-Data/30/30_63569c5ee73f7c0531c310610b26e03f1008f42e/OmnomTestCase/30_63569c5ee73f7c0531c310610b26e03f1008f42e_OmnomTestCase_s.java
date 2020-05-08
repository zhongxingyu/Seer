 package eu.dm2e.ws;
 
 import java.io.File;
import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.IOUtils;
 
 import eu.dm2e.ws.services.Client;
 
 public class OmnomTestCase {
 	protected Logger log = Logger.getLogger(getClass().getName());
 	protected Client client = new Client();
 	protected String URI_BASE = "http://localhost:9998/";
 	protected Map<OmnomTestResources, String> configString = new HashMap<>();
 	protected Map<OmnomTestResources, File> configFile = new HashMap<>();;
 	
 //	@Before
 //	public void setUp() throws Exception {
 	public OmnomTestCase() {
		for (OmnomTestResources wsconf : OmnomTestResources.values()) { 
			URL testConfigURL = this.getClass().getResource(wsconf.getPath());
			configFile.put(wsconf, new File(testConfigURL.getFile()));
 			try {
				configString.put(wsconf, IOUtils.toString(testConfigURL.openStream()));
			} catch (IOException e) {
				org.junit.Assert.fail(e.toString());
 			}
 		}
 	}
 }
