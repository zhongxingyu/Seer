 package gr.dsigned.atom.parser;
 
 import gr.dsigned.atom.domain.Feed;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import junit.framework.TestCase;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 public class AtomParserTest extends TestCase {
 
 	public void testParse() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
 		parser.setParseDates(false);
 		URL url = new URL("http://feeds.huffingtonpost.com/huffingtonpost/raw_feed");
 		Feed feed = parser.parse(url.openConnection().getInputStream());
 		assertNotNull(feed);
 		assertTrue(feed.getEntries().size() > 0);
 	}
 
 	public void testParseFile() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
 		parser.setParseDates(false);
 		InputStream in = this.getClass().getResource("AtomTestFeed.xml").openStream();
 		Feed feed = parser.parse(in);
 		assertNotNull(feed);
 		assertNotNull(feed.getEntries());
 		assertTrue(feed.getEntries().size() > 0);
 		assertTrue(feed.getEntries().get(0).getCatogories().size() > 0);
 		assertTrue(feed.getEntries().get(0).getLinks().size() > 0);
 	}
 
 	public void testParseDatesTrue() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
 		parser.setParseDates(true);
 		URL url = new URL("http://feeds.huffingtonpost.com/huffingtonpost/raw_feed");
 		Feed feed = parser.parse(url.openConnection().getInputStream());
 		assertNotNull(feed);
 		assertTrue(feed.getEntries().size() > 0);
 	}
 
 	public void testParseFileParseDatesTrue() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
 		parser.setParseDates(true);
 		InputStream in = this.getClass().getResource("AtomTestFeed.xml").openStream();
 		Feed feed = parser.parse(in);
 		assertNotNull(feed);
 		assertNotNull(feed.getEntries());
 		assertTrue(feed.getEntries().size() > 0);
 		assertTrue(feed.getEntries().get(0).getCatogories().size() > 0);
 		assertTrue(feed.getEntries().get(0).getLinks().size() > 0);
 	}
 
 	public void testTimeout() throws XmlPullParserException, IOException {
 		AtomParser parser = new AtomParser();
 		InputStream in = new DelayingInputStream();
 		assertNull(parser.parse(in, 100));
		in = this.getClass().getResource("AtomTestFeed.xml").openStream();
 		Feed feed = parser.parse(in, 5000);
 		assertNotNull(feed);
 		assertNotNull(feed.getEntries());
 		assertTrue(feed.getEntries().size() > 0);
 		assertTrue(feed.getEntries().get(0).getCatogories().size() > 0);
 		assertTrue(feed.getEntries().get(0).getLinks().size() > 0);
 	}
 
 	private class DelayingInputStream extends InputStream {
 
 		@Override
 		public int read() throws IOException {
 			while (true) {
 				// loop
 			}
 		}
 	}
 }
