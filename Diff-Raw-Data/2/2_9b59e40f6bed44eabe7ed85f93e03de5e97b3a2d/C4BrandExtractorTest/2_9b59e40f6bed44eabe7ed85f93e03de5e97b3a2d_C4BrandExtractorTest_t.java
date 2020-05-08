 package org.atlasapi.remotesite.channel4;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.is;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.persistence.testing.StubContentResolver;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpResponsePrologue;
 import com.metabroadcast.common.http.HttpStatusCodeException;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.sun.syndication.feed.atom.Feed;
 
 @RunWith(JMock.class)
 public class C4BrandExtractorTest extends TestCase {
     
     private final Mockery context = new Mockery();
 
 	private final AtomFeedBuilder rknSeries3Feed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-3.atom"));
 	private final AtomFeedBuilder rknSeries4Feed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-4.atom"));
 	private final AtomFeedBuilder rknBrandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares.atom"));
 	private final AtomFeedBuilder rknEpsiodeGuideFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-episode-guide.atom"));
 	private final AtomFeedBuilder rknFourOdFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-4od.atom"));
 	private final AtomFeedBuilder rknEpgFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-epg.atom"));
 	private final AtomFeedBuilder uglyBettyClipFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ugly-betty-video.atom"));
 
 	private final AtomFeedBuilder dispatchesBrandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "dispatches.atom"));
 	private final AtomFeedBuilder dispatchesEpisodeGuideFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "dispatches-episode-guide.atom"));
 
 	
 	private final RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/4od.atom", rknFourOdFeed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", rknEpsiodeGuideFeed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries3Feed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-4.atom", rknSeries4Feed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/epg.atom", rknEpgFeed.build());
 	
 	private final static AdapterLog nullLog = new NullAdapterLog();
 	private final StubContentResolver contentResolver = new StubContentResolver();
 	private ChannelResolver channelResolver;
 	
 	@Before
 	public void setUp() {
 		channelResolver = context.mock(ChannelResolver.class);
 		context.checking(new Expectations() {
 			{
 				allowing(channelResolver).fromUri("http://www.channel4.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "Channel 4", "channel4", MediaType.VIDEO, "http://www.channel4.com"))));
 				allowing(channelResolver).fromUri("http://www.channel4.com/more4");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "More4", "more4", MediaType.VIDEO, "http://www.more4.com"))));
 				allowing(channelResolver).fromUri("http://film4.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "Film4", "more4", MediaType.VIDEO, "http://film4.com"))));
 				allowing(channelResolver).fromUri("http://www.e4.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "E4", "more4", MediaType.VIDEO, "http://www.e4.com"))));
 				allowing(channelResolver).fromUri("http://www.4music.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "4Music", "more4", MediaType.VIDEO, "http://www.4music.com"))));
				allowing(channelResolver).fromUri("http://www.channel4.com/4seven");
                will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "4seven", "4seven", MediaType.VIDEO, "http://www.channel4.com/4seven"))));
 			}
 		});
 	}
 
     @Test
 	public void testExtractingABrand() throws Exception {
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		
 		new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 
 		Brand brand = Iterables.getOnlyElement(recordingWriter.updatedBrands);
 		
 		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
 
 		Item firstItem = recordingWriter.updatedItems.get(0);
 		
 		assertThat(firstItem.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1"));
 
 		assertThat(firstItem.getAliases(), is((Set<String>) ImmutableSet.of("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2921983", "tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1")));
 		
 		assertThat(firstItem.getTitle(), is(("Series 3 Episode 1")));
 		
 		Version firstItemVersion = Iterables.getOnlyElement(firstItem.getVersions());
 		
 		assertThat(firstItemVersion.getDuration(), is(2949));
 
 		Encoding firstItemEncoding = Iterables.getOnlyElement(firstItemVersion.getManifestedAs());
 		Location firstItemLocation = Iterables.getOnlyElement(firstItemEncoding.getAvailableAt());
 		assertThat(firstItemLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2921983"));
 		
 		Episode episodeNotOn4od = (Episode) find("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-5", recordingWriter.updatedItems);
 		assertThat(episodeNotOn4od.getVersions().size(), is(0));
 	}
 
     @Test
 	public void testThatBroadcastIsExtractedFromEpg() throws Exception {
 		
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 
 	    new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 	    
 	    boolean found = false;
 	    for (Item item : recordingWriter.updatedItems) {
 	        if (item.getCanonicalUri().equals("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5")) {
 	            assertFalse(item.getVersions().isEmpty());
 	            Version version = item.getVersions().iterator().next();
 	            
 	            assertEquals(1, version.getBroadcasts().size());
 	            for (Broadcast broadcast: version.getBroadcasts()) {
 	                if (broadcast.getBroadcastDuration() == 60*55) {
 	                    assertTrue(broadcast.getAliases().contains("tag:www.channel4.com,2009:slot/E439861"));
 	                    assertThat(broadcast.getSourceId(), is("e4:39861"));
 	                    assertEquals(new DateTime("2010-08-11T14:06:33.341Z", DateTimeZones.UTC), broadcast.getLastUpdated());
 	                    found = true;
 	                }
 	            }
 	        }
 	    }
 	    
 	    assertTrue(found);
 	}
 
     @Test
 	public void testOldEpisodeWithBroadcast() throws Exception {
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		
 	    Episode episode = new Episode("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5", "c4:ramsays-kitchen-nightmares_series-4_episode-5", Publisher.C4);
 	    Version version = new Version();
 	    episode.addVersion(version);
 	    Broadcast oldBroadcast = new Broadcast("some channel", new DateTime(), new DateTime());
 	    oldBroadcast.addAlias("tag:www.channel4.com:someid");
 	    version.addBroadcast(oldBroadcast);
 	    contentResolver.respondTo(episode);
 	    
 	    new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
         
         boolean found = false;
         boolean foundOld = false;
         for (Item item: recordingWriter.updatedItems) {
             if (item.getCanonicalUri().equals("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5")) {
                 assertFalse(item.getVersions().isEmpty());
                 version = item.getVersions().iterator().next();
                 
                 assertEquals(2, version.getBroadcasts().size());
                 for (Broadcast broadcast: version.getBroadcasts()) {
                     if (broadcast.getBroadcastDuration() == 60*55) {
                         assertTrue(broadcast.getAliases().contains("tag:www.channel4.com,2009:slot/E439861"));
                         assertThat(broadcast.getSourceId(), is("e4:39861"));
                         assertEquals(new DateTime("2010-08-11T14:06:33.341Z", DateTimeZones.UTC), broadcast.getLastUpdated());
                         found = true;
                     } else if (broadcast.getAliases().contains("tag:www.channel4.com:someid")) {
                         foundOld = true;
                     }
                 }
             }
         }
         
         assertTrue(found);
         assertTrue(foundOld);
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries1IsAssumed() throws Exception {
 	    HttpResponsePrologue response = new HttpResponsePrologue(403, "error").withFinalUrl("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
 		
 	    RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response, "403"))
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-1.atom", rknSeries3Feed.build());
 
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuide404sSeries1IsAssumed() throws Exception {
 	   HttpResponsePrologue response = new HttpResponsePrologue(404, "error");
 		
 	    RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response, "404"))
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-1.atom", rknSeries3Feed.build());
 
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries3IsReturned() throws Exception {
 	    HttpResponsePrologue response = new HttpResponsePrologue(403, "error").withFinalUrl("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
         RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
             .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
             .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response, "403"))
             .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries4Feed.build());
 
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
         new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
         assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
     }
 	
     @Test
 	public void testFlattenedBrandsItemsAreNotPutIntoSeries() throws Exception {
 		 RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
          .respondTo("http://api.channel4.com/pmlsd/dispatches.atom", dispatchesBrandFeed.build())
          .respondTo("http://api.channel4.com/pmlsd/dispatches/episode-guide.atom", dispatchesEpisodeGuideFeed.build());
 		 
 		 RecordingContentWriter recordingWriter = new RecordingContentWriter();
 	     new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/dispatches");
 	     
 	     assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	     for (Item item : recordingWriter.updatedItems) {
 			assertThat(item.getVersions().size(), is(0));
 		}
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuideRedirectsToAnEpisodeFeedTheSeriesIsFetched() throws Exception {
 	   
 		Feed episodeFeed = new Feed();
 		episodeFeed.setId("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-5");
 		 
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
            .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
            .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", episodeFeed)
            .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries3Feed.build());
 
 	   RecordingContentWriter recordingWriter = new RecordingContentWriter();
        new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
        assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuideRedirectsToSeries1TheSeriesIsRead() throws Exception {
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 			.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", rknSeries3Feed.build());
 
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 	    assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatClipsAreAddedToBrands() throws Exception {
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", rknSeries3Feed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/video.atom", uglyBettyClipFeed.build());
 		
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(Iterables.getOnlyElement(recordingWriter.updatedBrands).getClips().size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatOldLocationsAndBroadcastsAreCopied() {
 
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 		.respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", rknSeries3Feed.build())
         .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries3Feed.build());
 
 		
 		Episode series3Ep1 = new Episode("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1", "curie", Publisher.C4);
 		
 		Version c4Version = new Version();
 		c4Version.setCanonicalUri("v1");
 
 		// this version shouldn't be merged because it's not from C4
 		Version otherPublisherVersion = new Version();
 		otherPublisherVersion.setProvider(Publisher.YOUTUBE);
 		otherPublisherVersion.setCanonicalUri("v2");
 
 		series3Ep1.addVersion(c4Version);
 		series3Ep1.addVersion(otherPublisherVersion);
 		
 		contentResolver.respondTo(series3Ep1);
 		
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(feedClient, contentResolver, recordingWriter, channelResolver, null, nullLog).createOrUpdateBrand("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		Item series3Ep1Parsed = Iterables.get(recordingWriter.updatedItems, 0);
 		
 		assertTrue(Iterables.getOnlyElement(series3Ep1Parsed.getVersions()) == c4Version);
 	}
 	
 	private static class StubC4AtomClient implements RemoteSiteClient<Feed> {
 
 		private Map<String, Object> respondsTo = Maps.newHashMap();
 
 		@Override
 		public Feed get(String uri) throws Exception {
 			// Remove API key
 			uri = removeQueryString(uri);
 			Object response = respondsTo.get(uri);
 			if (response == null) {
 				throw new HttpStatusCodeException(404, "Not found: " + uri);
 			} else if (response instanceof HttpException) {
 			    throw (HttpException) response;
 			}
 			return (Feed) response;
 		}
 
 		private String removeQueryString(String url) throws MalformedURLException {
 			String queryString = "?" + new URL(url).getQuery();
 			return url.replace(queryString, "");
 		}
 		
 		StubC4AtomClient respondTo(String url, Feed feed) {
 			respondsTo.put(url, feed);
 			return this;
 		}
 		
 		StubC4AtomClient respondTo(String url, HttpException exception) {
 		    respondsTo.put(url, exception);
 		    return this;
 		}
 	}
 	
 	private final <T extends Content> T find(String uri, Iterable<T> episodes) {
 		for (T episode : episodes) {
 			if (episode.getCanonicalUri().equals(uri)) {
 				return episode;
 			}
 		}
 		throw new IllegalStateException("Not found");
 	}
 }
