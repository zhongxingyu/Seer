 package kop.ports;
 
 import com.sun.org.apache.xpath.internal.operations.Mod;
 import junit.framework.TestCase;
 import kop.ships.ModelSerializer;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 4/26/11
  * Time: 7:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PortMapTest {
 	@Test
 	public void serializePortMap() throws Exception {
 		PortMap map = new PortMap();
 
 		Port port = new Port();
 		port.setName("London");
 		port.setUnlocode("GB LON");
 		map.put("London", port);
 		ModelSerializer.saveToFile("portmap.xml",PortMap.class, map);
 	}
 
//	@Test
 	public void deserializePortMap() throws Exception {
 		PortMap map = (PortMap) ModelSerializer.readFromFile("portmap.xml", PortMap.class);
 		assertNotNull(map);
 		assertNotNull(map.get("London"));
 	}
 }
