 package gr.dsigned.atom.parser;
 
 import gr.dsigned.atom.domain.Feed;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import junit.framework.TestCase;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 public class AtomParserTest extends TestCase {
 
 	public void testParse() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
 		URL url = new URL("http://feeds.huffingtonpost.com/huffingtonpost/raw_feed");
 		Feed feed = parser.parse(url.openConnection().getInputStream());
 		assertNotNull(feed);
 		assertTrue(feed.getEntries().size() > 0);
 	}
 
 	public void testParseFile() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
		InputStream in = this.getClass().getResource("AtomTestFeed.xml").openStream();
 		Feed feed = parser.parse(in);
 		assertNotNull(feed);
 		assertTrue(feed.getEntries().size() > 0);
 	}
 }
