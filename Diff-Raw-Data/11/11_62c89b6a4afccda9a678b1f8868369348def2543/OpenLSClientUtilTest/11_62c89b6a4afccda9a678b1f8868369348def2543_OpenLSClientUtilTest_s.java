 /**
  * 
  */
 package nl.mineleni.cbsviewer.servlet.gazetteer.lusclient;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import nl.mineleni.openls.databinding.openls.GeocodeResponse;
 import nl.mineleni.openls.parser.OpenLSResponseParser;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Testcase voor
  * {@link nl.mineleni.cbsviewer.servlet.gazetteer.lusclient.OpenLSClientUtil}
  * 
  * @author PrinsMC
  * 
  */
 public class OpenLSClientUtilTest {
 	private GeocodeResponse bigResp;
 	private GeocodeResponse smallResp;
 
 	/**
 	 * opzetten test benodigdheden.
 	 * 
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		final OpenLSResponseParser rp = new OpenLSResponseParser();
 		byte[] buf = new byte[1024];
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		int numRead;
 
 		InputStream in = this.getClass().getClassLoader()
 				.getResourceAsStream("sampleresponses/openls-pointlist.xml");
 		while ((numRead = in.read(buf)) > 0) {
 			bos.write(buf, 0, numRead);
 		}
 		bos.close();
 		smallResp = rp.parseOpenLSResponse(bos.toString());
 
 		buf = new byte[1024];
 		bos = new ByteArrayOutputStream();
 		in = this
 				.getClass()
 				.getClassLoader()
 				.getResourceAsStream(
 						"sampleresponses/jacobstraat200utrecht.xml");
 		while ((numRead = in.read(buf)) > 0) {
 			bos.write(buf, 0, numRead);
 		}
 		bos.close();
 		bigResp = rp.parseOpenLSResponse(bos.toString());
 	}
 
 	/**
 	 * Test methode voor
 	 * {@link nl.mineleni.cbsviewer.servlet.gazetteer.lusclient.OpenLSClientUtil#getGeocodedAddressList(nl.mineleni.openls.databinding.openls.GeocodeResponse)}
 	 * .
 	 */
 	@Test
 	public final void testGetGeocodedAddressList() {
 		assertEquals(3, OpenLSClientUtil.getGeocodedAddressList(smallResp)
 				.size());
 		assertEquals(142, OpenLSClientUtil.getGeocodedAddressList(bigResp)
 				.size());
 	}
 
 	/**
 	 * Test methode voor
 	 * {@link nl.mineleni.cbsviewer.servlet.gazetteer.lusclient.OpenLSClientUtil#getOpenLSClientAddressList(nl.mineleni.openls.databinding.openls.GeocodeResponse, int[])}
 	 * .
 	 */
 	@Test
 	public final void testGetOpenLSClientAddressList() {
 		assertEquals(3, OpenLSClientUtil.getOpenLSClientAddressList(smallResp)
 				.size());
 		assertEquals(3,
 				OpenLSClientUtil.getOpenLSClientAddressList(smallResp, null)
 						.size());
 		assertEquals(2,
 				OpenLSClientUtil.getOpenLSClientAddressList(smallResp, 2)
 						.size());
 		assertEquals(142,
 				OpenLSClientUtil.getOpenLSClientAddressList(bigResp, null)
 						.size());
 		assertEquals(10,
 				OpenLSClientUtil.getOpenLSClientAddressList(bigResp, 10).size());
 	}
 
 }
