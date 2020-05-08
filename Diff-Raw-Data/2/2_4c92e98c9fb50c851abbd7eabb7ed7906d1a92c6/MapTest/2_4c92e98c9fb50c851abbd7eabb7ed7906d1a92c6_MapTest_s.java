 package org.fourdnest.androidclient.test.mapview;
 
 import org.fourdnest.androidclient.tools.OsmStaticMapGetter;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import android.net.Uri;
 import android.test.AndroidTestCase;
 
 
 public class MapTest extends AndroidTestCase {
 	@Before
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	@After
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	@Test
 	public void testGetMediaFile() throws Exception {
		boolean val = (new OsmStaticMapGetter().getStaticMap(Uri.parse("/sdcard/test2.png"), null));
 		assertTrue(val);
 	}
 
 }
 
