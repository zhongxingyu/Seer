 package org.atlasapi.feeds.interlinking.outputting;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.atlasapi.feeds.interlinking.InterlinkBase.Operation;
 import org.atlasapi.feeds.interlinking.InterlinkBrand;
 import org.atlasapi.feeds.interlinking.InterlinkBroadcast;
 import org.atlasapi.feeds.interlinking.InterlinkEpisode;
 import org.atlasapi.feeds.interlinking.InterlinkFeed;
 import org.atlasapi.feeds.interlinking.InterlinkFeed.InterlinkFeedAuthor;
 import org.atlasapi.feeds.interlinking.InterlinkOnDemand;
 import org.atlasapi.feeds.interlinking.InterlinkSeries;
 import org.atlasapi.feeds.interlinking.validation.InterlinkOutputValidator;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.junit.Test;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class InterlinkFeedOutputterTest {
 
 	private final static InterlinkFeedOutputter outputter = new InterlinkFeedOutputter();
 	private final DateTime lastUpdated = new DateTime("2010-04-27T09:49:40.803Z", DateTimeZones.UTC);
 	
 	@Test
 	public void testSerialisationOfAFeed() throws Exception {
 		
 		InterlinkBrand brand = new InterlinkBrand("1", Operation.STORE)
 			.withTitle("Lark Rise to Candleford")
 			.withDescription("Adaption of Flora Thompson's memoir of her Oxfordshire childhood")
 			.withLastUpdated(lastUpdated)
			.withSummary("summary")
 			.withThumbnail("thumbnail");
 		
 		// add an episode directly to the brand
		InterlinkEpisode episodeWithoutASeries = new InterlinkEpisode("notInASeries", Operation.STORE, 2, "link", brand).withTitle("Episode not in a series").withLastUpdated(lastUpdated).withSummary("summary");
 		InterlinkBroadcast broadcastWithoutASeries = new InterlinkBroadcast("broadcastNotInASeries", Operation.STORE, episodeWithoutASeries);
 		InterlinkOnDemand onDemandWithoutASeries = new InterlinkOnDemand("odNotInASeries", "link", Operation.STORE, lastUpdated, lastUpdated, new Duration(1000), "notInASeries");
 		
 		InterlinkSeries series = new InterlinkSeries("series2", Operation.STORE, 2, brand)
 
 			.withTitle("Lark Rise to Candleford Series 2")
			.withSummary("Adaption of Flora Thompson's").withLastUpdated(new DateTime(2011,02,11,22,00,00,00,DateTimeZones.UTC));
		    
 		
 		InterlinkEpisode episode = new InterlinkEpisode("episode3", Operation.STORE, 3, "link", series)
             .withTitle("Episode 3")
            .withLastUpdated(lastUpdated).withSummary("summary");
 		
 		InterlinkOnDemand onDemand = new InterlinkOnDemand("ondemand5", "link", Operation.STORE, lastUpdated, lastUpdated, new Duration(1000), "episode3");
 		
 		InterlinkBroadcast broadcast = new InterlinkBroadcast("broadcast4", Operation.STORE, episode)
 			.withBroadcastStart(new DateTime("2010-01-10T21:00:00Z"))
 			.withDuration(Duration.standardMinutes(45)).withLastUpdated(lastUpdated);
 		
 		InterlinkFeed feed = new InterlinkFeed("https://www.bbc.co.uk/interlinking/20100115")
 			.withTitle("BBC Daily Change Feed")
 			.withSubtitle("All metadata changes for on demand BBC content")
 			.withUpdatedAt(lastUpdated)
 			.withAuthor(new InterlinkFeedAuthor("a partner", "a supplier"))
 			.addEntry(brand)
 			.addEntry(episodeWithoutASeries)
 			.addEntry(broadcastWithoutASeries)
 			.addEntry(onDemandWithoutASeries)
 			.addEntry(series)
 			.addEntry(episode)
 			.addEntry(broadcast)
 			.addEntry(onDemand);
 		
 		String generated = output(feed);
 		assertEquals(expectedFeed("feed.atom"), generated);
 		
 		// for now just print out errors since the feed is not finished and will not validate
 		new InterlinkOutputValidator().validatesAgainstSchema(generated, System.out);
 	}
 
 	private String output(InterlinkFeed feed) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		outputter.output(feed, out, true);
 		return out.toString(Charsets.UTF_8.toString());
 	}
 
 	private String expectedFeed(String filename) throws IOException {
 		return Resources.toString(Resources.getResource("org/atlasapi/feeds/interlinking/outputting/" + filename), Charsets.UTF_8);
 	}	
 }
