 package org.atlasapi.feeds.radioplayer.outputting;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalDate;
 import org.junit.Test;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.io.Resources;
 
 public class RadioPlayerProgrammeInformationOutputterTest {
 
 	private static final DateTimeZone TIMEZONE = DateTimeZone.forOffsetHours(8);
 	private static RadioPlayerXMLOutputter outputter = new RadioPlayerProgrammeInformationOutputter();
 	
 	public static Episode buildItem(){
 		Episode testItem = new Episode("http://www.bbc.co.uk/programmes/b00f4d9c",
 				"bbc:b00f4d9c", Publisher.BBC);
 		testItem.setTitle("BBC Electric Proms: Saturday Night Fever");
 		testItem.setDescription("Another chance to hear Robin Gibb perform the Bee Gees' classic disco album with the BBC Concert Orchestra. It was recorded" +
 				" for the BBC Electric Proms back in October 2008, marking 30 years since Saturday Night Fever soundtrack topped the UK charts.");
 		testItem.setGenres(ImmutableSet.of(
 				"http://www.bbc.co.uk/programmes/genres/music",
 				"http://ref.atlasapi.org/genres/atlas/music")
 		);
 		testItem.setImage("http://www.bbc.co.uk/iplayer/images/episode/b00v6bbc_640_360.jpg");
 		
 		Version version = new Version();
 		
 		Broadcast broadcast = new Broadcast("http://www.bbc.co.uk/services/radio2", new DateTime(2008,10,25,18,30,0,0, TIMEZONE), new DateTime(2008,10,25,20,0,0,0, TIMEZONE));
 		version.addBroadcast(broadcast);
 		
 		Encoding encoding = new Encoding();
 		Location location = new Location();
 		location.setUri("http://www.bbc.co.uk/iplayer/episode/b00f4d9c");
 		Policy policy = new Policy();
 		policy.setAvailabilityEnd(new DateTime(2010, 8, 28, 23, 40, 19, 0, TIMEZONE));
 		policy.setAvailabilityStart(new DateTime(2010, 9,  4, 23, 02, 00, 0, TIMEZONE));
 		policy.addAvailableCountry(Countries.GB);
 		location.setPolicy(policy);
 		location.setTransportType(TransportType.LINK);
 		encoding.addAvailableAt(location);
 		version.addManifestedAs(encoding);
 		
 		testItem.addVersion(version);
 		
 		return testItem;
 	}
 
 	@Test
 	public void testOutputtingAPIFeed() throws Exception {
 		Episode testItem = buildItem();
 		
 		Series series = new Series("seriesUri", "seriesCurie", Publisher.BBC);
 		series.setTitle("This is the series title");
 		series.addContents(testItem);
 		
 		Brand brand = new Brand("http://www.bbc.co.uk/programmes/b006m9mf", "bbc:b006m9mf", Publisher.BBC);
 		brand.setTitle("Electric Proms");
 		((Episode)testItem).setContainer(brand);
 
         Version version = Iterables.getOnlyElement(testItem.getVersions());
         Broadcast broadcast = Iterables.getOnlyElement(version.getBroadcasts());
 		assertEquals(expectedFeed("basicPIFeedTest.xml"), output(ImmutableList.of(new RadioPlayerBroadcastItem(testItem, version, broadcast))));
 	}
 	
 	@Test
 	public void testOutputtingAPIFeedWithSeriesAndNoBrand() throws Exception {
 		Episode testItem = buildItem();
 		
 		Series series = new Series("seriesUri", "seriesCurie", Publisher.BBC);
 		series.setTitle("Series Title");
 		series.addContents(testItem);
 		
 		//make item available everywhere.
		getLocation(testItem).getPolicy().setAvailableCountries(ImmutableSet.of(Countries.GB));
 		
 		Version version = Iterables.getOnlyElement(testItem.getVersions());
         Broadcast broadcast = Iterables.getOnlyElement(version.getBroadcasts());
         assertEquals(expectedFeed("seriesNoBrandPIFeedTest.xml"), output(ImmutableList.of(new RadioPlayerBroadcastItem(testItem, version, broadcast))));
 	}
 
 	private Location getLocation(Item testItem) {
 		return Iterables.getLast(Iterables.getLast(Iterables.getLast(testItem.getVersions()).getManifestedAs()).getAvailableAt());
 	}
 	
 	@Test
 	public void testOutputtingPIFileWithNoLocation() throws Exception {
 
 		Item testItem = buildItem();
 		
 		Version version = new Version();
 		
 		Broadcast broadcast = new Broadcast("http://www.bbc.co.uk/services/radio2", new DateTime(2008,10,25,18,30,0,0, TIMEZONE), new DateTime(2008,10,25,20,0,0,0, TIMEZONE));
 		version.addBroadcast(broadcast);
 		
 		testItem.setVersions(ImmutableSet.of(version));
 		
 		assertEquals(expectedFeed("noLocationPIFeedTest.xml"), output(ImmutableList.of(new RadioPlayerBroadcastItem(testItem, version, broadcast))));
 	}
 	
 	private static String output(List<RadioPlayerBroadcastItem> items) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		outputter.output(new LocalDate(2010, 9, 6),
 						new RadioPlayerService(502, "radio2").withDabServiceId("e1_ce15_c222_0"), items, out);
 		return out.toString(Charsets.UTF_8.toString()).substring(550);
 	}
 
 	private String expectedFeed( String filename) throws IOException {
 		return Resources.toString(
 				Resources.getResource("org/atlasapi/feeds/radioplayer/"
 						+ filename), Charsets.UTF_8).substring(550);
 	}
 
 }
