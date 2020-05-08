 package net.praqma.util.xml.feed;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.junit.Test;
 
 import junit.framework.TestCase;
 
 public class FeedTest extends TestCase {
 
 	public static SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
 	
 	@Test
 	public void testFeed() throws FeedException, ParseException {
 		Feed feed = new Feed( "test", "my--001", format.parse( "2012-01-01" ) );
 		feed.link = "http://www.praqma.net";
 		feed.author = new Person( "PRAQMA" );
 		
 		Entry e = new Entry( "Title E1", "My-id-001", format.parse( "2012-01-02" ) );
 		e.summary = "Something cool";
 		e.author = new Person( "PRAQMA #1" );
 		e.content = "A very long content tag";
 		feed.addEntry( e );
 		
 		Entry e2 = new Entry( "Title E2", "My-id-002", format.parse( "2012-01-03" ) );
 		e2.summary = "Something cool 2";
 		e2.author = new Person( "PRAQMA #2" );
 		e2.content = "A very long content tag 2";
 		feed.addEntry( e2 );
 		
 		String xml = feed.getXML( new AtomPublisher() );
 		
 		System.out.println( "XML: " + xml );
 	}
 	
 	@Test
 	public void testToFeed() throws FeedException, IOException {
		File xml = new File( FeedTest.class.getClassLoader().getResource( "feed.xml" ).getFile() );
 		Feed feed = Feed.getFeed( new AtomPublisher(), xml );
 		
 		System.out.println( "XML2way: " + feed.getXML( new AtomPublisher() ) );
 	}
 	
 	@Test
 	public void testToFeedAdd() throws FeedException, IOException, ParseException {
		File xml = new File( FeedTest.class.getClassLoader().getResource( "feed.xml" ).getFile() );
 		Feed feed = Feed.getFeed( new AtomPublisher(), xml );
 		
 		Entry e3 = new Entry( "Title E3", "My-id-003", format.parse( "2012-01-04" ) );
 		e3.summary = "Something cool 3";
 		e3.author = new Person( "PRAQMA #3" );
 		e3.content = "A very long content tag 3";
 		feed.addEntry( e3 );
 		
 		System.out.println( "XML2way: " + feed.getXML( new AtomPublisher() ) );
 	}
 	
 	@Test
 	public void testLimit() throws FeedException, IOException {
		File xml = new File( FeedTest.class.getClassLoader().getResource( "5feeds.xml" ).getFile() );
 		Feed feed = Feed.getFeed( new AtomPublisher(), xml );
 		
 		System.out.println( "LIMIT TO 3: " + feed.getXML( new AtomPublisher(), 3 ) );
 	}
 	
 	@Test
 	public void testBackwardsCompToOldFormat() throws FeedException, IOException, ParseException {
		File xml = new File( FeedTest.class.getClassLoader().getResource( "feedOldFormat.xml" ).getFile() );
 		Feed feed = Feed.getFeed( new AtomPublisher(), xml );
 		
 		System.out.println( "FEED: " + feed );
 		
 		Date when = FeedPublisher.oldformat.parse( "2012-01-01T00:00:00Z" );
 		assertEquals( when, feed.updated );
 		
 	}
 }
