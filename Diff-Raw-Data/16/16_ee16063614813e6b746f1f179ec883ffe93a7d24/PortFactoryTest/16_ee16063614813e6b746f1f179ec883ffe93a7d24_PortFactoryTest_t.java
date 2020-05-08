 package kop.ports;
 
 import org.junit.Test;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Map;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 4/5/11
  * Time: 4:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PortFactoryTest {
//	@Test
 	public void testReadFromFile() throws IOException, URISyntaxException {
 		PortFactory factory = new PortFactory();
 
 		Map<String, Port> map = factory.createPorts();
 		assertNotNull(map);
 		assertFalse(map.size() == 0);
 		Port p = map.get("ROTTERDAM");
 		assertNotNull(p);
 		assertTrue(p.getCountryCode().equals("NL"));
 	}
 }
