 package org.fourdnest.androidclient.test.comm;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 import org.fourdnest.androidclient.comm.*;
 import org.fourdnest.androidclient.test.comm.MemoryCardInitializer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import android.net.Uri;
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 
 
 
 
 public class httpTest extends AndroidTestCase {
 
 	@Before
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	@After
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 /*
 	@Test
 	public void testHttpGet() throws Exception {
 		
 		FourDNestProtocol protocol = new FourDNestProtocol();
 		String get = protocol.getTest();
 		assertTrue(get.length() != 0);
 		Log.v("httpget", get);
 	}
 */	
 	@Test
 	public void testHttpPost() throws Exception {
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		MemoryCardInitializer.initialize(this.getContext());
 		Uri uri = Uri.parse("/sdcard/kuva.jpg");
 		Log.v("Path", uri.getPath());
		Egg egg = new Egg(5, 10, "Meitsi", uri, null, "Eip toimi", tags, 100);
 
         /* SELECT to use local or web server */
 
 		Nest nest = new Nest(007, "testNest", "testNest", new URI("https://test42.4dnest.org/"), ProtocolFactory.PROTOCOL_4DNEST, "testuser", "secretkey");
 		//Nest nest = new Nest(007, "testNest", "testNest", new URI("http://10.0.2.2:8000/"), ProtocolFactory.PROTOCOL_4DNEST, "testuser", "secretkey");
 		Protocol protocol = nest.getProtocol();
 
 		protocol.setNest(nest);
 		ProtocolResult protResult = protocol.sendEgg(egg);
 		String post = String.valueOf(protResult.getStatusCode()) + " "; 
 		if (protResult.getUrl() != null) {
 		    post += protResult.getUrl();
 		}
 	    Log.v("httppost", post);
 		assertTrue(protResult.getStatusCode() == ProtocolResult.RESOURCE_UPLOADED);
 
 	}
 	
 	@Test
 	public void testGetEgg() throws Exception{
 		String eggId = "OTngW6mSd99l";
 		String expectedResult = "Test post with curl in Maarintalo";
 		Nest nest = new Nest(007, "testNest", "testNest", new URI("http://test42.4dnest.org/"), ProtocolFactory.PROTOCOL_4DNEST, "testuser", "secretkey");
 		Protocol protocol = nest.getProtocol();
 		protocol.setNest(nest);
 		Egg egg = protocol.getEgg(eggId);
 		assertTrue(egg != null);
 		assertTrue(egg.getCaption().compareToIgnoreCase(expectedResult) == 0);
 		Log.d("EGGCAPTION", egg.getCaption());
 	}
 	
 	@Test
 	public void testGetStream() throws Exception {
 		Nest nest = new Nest(007, "testNest", "testNest", new URI("http://test42.4dnest.org/"), ProtocolFactory.PROTOCOL_4DNEST, "testuser", "secretkey");
 		Protocol protocol = nest.getProtocol();
 		protocol.setNest(nest);
 		List<Egg> eggList = protocol.getStream();
 		assertTrue(eggList.size() > 0);
 	}
 	
 	private void getPreviouslySentEgg() {
 	    
 	}
 
 }
