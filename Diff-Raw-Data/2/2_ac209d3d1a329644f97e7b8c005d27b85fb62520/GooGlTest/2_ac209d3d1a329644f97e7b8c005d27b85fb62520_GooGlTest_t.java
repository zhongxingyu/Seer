 package net.petersson.googl;
 
 import net.petersson.googl.analytics.AnalyticsResponse;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import static net.petersson.googl.TestConstants.LONG_URL;
 import static net.petersson.googl.TestConstants.SHORT_URL;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 public class GooGlTest {
 
 	private static Logger logger = Logger.getLogger(GooGlTest.class.getName());
 
 	private String apiKey;
 
 	@Before
 	public void setUp() throws IOException {
 
 		Properties properties = new Properties();
		properties.load(this.getClass().getResourceAsStream("test.properties"));
 		this.apiKey = (String) properties.get("API_KEY");
 	}
 
 	@Test
 	public void test_shorten() throws IOException, GooGlException {
 
 		GooGl gooGl = new GooGl(this.apiKey);
 		assertEquals(new URL(SHORT_URL), gooGl.shorten(new URL(LONG_URL)));
 	}
 
 	@Test
 	public void test_expand_existing() throws IOException, GooGlException {
 
 		GooGl gooGl = new GooGl(this.apiKey);
 		assertEquals(new URL(LONG_URL), gooGl.expand(new URL(SHORT_URL)));
 	}
 
 	@Test
 	public void test_expand_nonExisting() throws IOException {
 
 		GooGl gooGl = new GooGl(this.apiKey);
 		try {
 			gooGl.expand(new URL(SHORT_URL + "xxx"));
 			fail("Expected GooGlException");
 		} catch (GooGlException e) {
 
 		}
 	}
 
 	@Test
 	public void test_analytics() throws IOException, GooGlException {
 
 		GooGl gooGl = new GooGl(this.apiKey);
 		AnalyticsResponse response = gooGl.getAnalytics(SHORT_URL);
 		assertEquals(LONG_URL, response.getLongUrl());
 	}
 }
