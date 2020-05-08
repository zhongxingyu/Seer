 package kop;
 
 import kop.map.LatLong;
 import kop.ports.Port;
 import kop.ports.PortMap;
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 5/1/11
  * Time: 11:13 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ConvertUnlocodeToXMLTest {
 	private static final String LONGITUDE = "024&deg 57.0' E";
 	private static final String LATITUDE = "60&deg 10.0' N";
 	private static final String UNLOCODE = "FIHEL";
 
 	@Test
 	public void createAndVerifyPort() {
 		String[] line = { "310","200","AKUREYRI","IC","65","41","N","18","5","W","181","38681","V","CN","F","N","N","Y","Y","N","A","A","J","","2","","Y","","N","N","Y","N","Y","","","N","Y","Y","Y","","Y","Y","","","Y","","Y","","","","","Y","","","N","Y","N","N","","","","Y","","","","","","Y","Y","Y","Y","","","C","M","S" };
 		createAndVerifyPortHelper(line);
 //		String[] line2 = { "12980","12700","PARANAGUA","BR","25","30","S","48","31","W","124","24133","S","CN","G","","N","N","Y","Y","J","M","L","L","2","L","Y","Y","Y","N","Y","Y","Y","","","N","Y","Y","","Y","","","Y","","","Y","Y","Y","","","","Y","N","N","Y","Y","Y","","Y","","Y","Y","Y","Y","Y","N","N","Y","Y","Y","Y","Y","Y","B","","M" };
 //		createAndVerifyPortHelper(line2);
 	}
 
 	private void createAndVerifyPortHelper(String[] line) {
 		Port port = ConvertUnlocodeToXML.createPort(line);
 		assertNotNull(port);
 		assertNotNull(port.getLatitude());
 		assertEquals("65", port.getLatitude().getDeg());
 		assertEquals("41", port.getLatitude().getMin());
 		assertEquals("N", port.getLatitude().getHemisphere());
 		assertEquals(65.683,port.getLatitude().getCoordinate());
 		assertEquals("18", port.getLongitude().getDeg());
 		assertEquals("5", port.getLongitude().getMin());
 		assertEquals("W", port.getLongitude().getHemisphere());
 		assertEquals(-18.083, port.getLongitude().getCoordinate());
 	}
 
 	@Test
 	public void parseLatitudeShouldReturnInstance() {
 		LatLong latLong = ConvertUnlocodeToXML.parse(LONGITUDE);
 		assertNotNull(latLong);
 		assertEquals("24", latLong.getDeg());
 		assertEquals("57", latLong.getMin());
 		assertEquals("E", latLong.getHemisphere());
 	}
 
 	@Test
 	public void createPortShouldReturnInstance() {
 		Port port = ConvertUnlocodeToXML.createPort(LATITUDE, LONGITUDE, UNLOCODE);
 		assertNotNull(port);
 		assertEquals(60.167, port.getLatitude().getCoordinate());
 	}
 
//	@Test
 	public void createPortFromFileShouldReturnInstance() throws IOException {
 		String fileName = "e-ships.net/ports/Finland/1476.htm";
 		Port port = ConvertUnlocodeToXML.createPortFromFile(fileName);
 		assertNotNull(port);
 		assertEquals("FIHEL",port.getUnlocode());
 		fileName = "e-ships.net/ports/Kiribati/2699.htm";
 		port = ConvertUnlocodeToXML.createPortFromFile(fileName);
 		assertNull(port);
 	}
 
 	// this test is very resource-heavy, thus we avoid it for now
 //	@Test
 	public void createPortMapFromHTMLShouldReturnInstance() throws IOException {
 		PortMap map = ConvertUnlocodeToXML.createPortMapFromHTML();
 		assertNotNull(map);
 		assertNotNull(map.get("FIHEL"));
 	}
 }
