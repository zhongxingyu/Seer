 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.uriplay.remotesite.bbc;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.Matchers.startsWith;
 
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.joda.time.DateTime;
 import org.springframework.core.io.ClassPathResource;
 import org.uriplay.media.TransportType;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Broadcast;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Encoding;
 import org.uriplay.media.entity.Episode;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Location;
 import org.uriplay.media.entity.Playlist;
 import org.uriplay.media.entity.Version;
 import org.uriplay.persistence.system.NullRequestTimer;
 import org.uriplay.persistence.system.RemoteSiteClient;
 import org.uriplay.persistence.system.RequestTimer;
 import org.uriplay.remotesite.SiteSpecificAdapter;
 import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
 import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
 import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;
 import org.uriplay.remotesite.synd.SyndicationSource;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 @SuppressWarnings("unchecked")
 public class BbcIplayerGraphExtractorTest extends MockObjectTestCase {
 	
 	static final String FEED_URI = "http://feeds.bbc.co.uk/iplayer/atoz/a/list";
 	
 	static final String BRAND_URI = "http://www.bbc.co.uk/programmes/b006v04h";
 	
 	static final Brand BRAND = new Brand(BRAND_URI, "curie"); {{ BRAND.setTitle("Spotlight"); }}
 	
 	static final String EPISODE_2_URI = "http://www.bbc.co.uk/programmes/b00kjbrc";
 
 	static final String ORPHAN_ITEM_URI = "http://www.bbc.co.uk/programmes/b00kfr9s";
 
 	static final RequestTimer TIMER = null;
 	
 	DateTime tuesday10pm = new DateTime(2009, 04, 21, 22, 00, 00, 00);
 	
 	RemoteSiteClient<SlashProgrammesRdf> episodeRdfClient = mock(RemoteSiteClient.class, "episodeClient");
 	RemoteSiteClient<SlashProgrammesVersionRdf> versionRdfClient = mock(RemoteSiteClient.class, "versionClient");
 	SiteSpecificAdapter<Description> brandClient = mock(SiteSpecificAdapter.class);
 	
 	SlashProgrammesVersion version = new SlashProgrammesVersion().withResourceUri("/programmes/b00k2vtr#programme");
 			
 	SlashProgrammesEpisode episode = new SlashProgrammesEpisode().inPosition(6)
 																 .withVersion(version)
 																 .withDescription("Claire Savage investigates a workers sit-in against those that shut the Visteon plant down")
 	                                                             .withGenres("/programmes/genres/factual/politics#genre", "/programmes/genres/news#genre")
 																 .withTitle("Shutdown");
 
 	SlashProgrammesContainerRef brandRef = new SlashProgrammesContainerRef().withUri("/programmes/b001#programme");
 	SlashProgrammesRdf episode2Rdf = new SlashProgrammesRdf().withEpisode(episode).withBrand(brandRef);
 	SlashProgrammesVersionRdf episode2versionRdf = new SlashProgrammesVersionRdf().withLastTransmitted(tuesday10pm, "/bbctwo#service");
 	
 	BbcIplayerGraphExtractor extractor;
 	SyndFeed feed;
 	SyndicationSource source;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		feed = createFeed("bbc-one-feed.atom.xml");
 		source = new SyndicationSource(feed, FEED_URI, TIMER);
 		extractor = new BbcIplayerGraphExtractor(episodeRdfClient, versionRdfClient, brandClient);
 	}
 	
 	public void testCreatesEpisodesFromFeedEntries() throws Exception {
 		
 		checking(new Expectations() {{ 
 			atLeast(1).of(episodeRdfClient).get(EPISODE_2_URI + ".rdf"); will(returnValue(episode2Rdf));
 			allowing(episodeRdfClient).get(with(not(startsWith(EPISODE_2_URI)))); will(returnValue(new SlashProgrammesRdf().withEpisode(new SlashProgrammesEpisode())));
 
			atLeast(1).of(brandClient).fetch(BRAND_URI + ".rdf", new NullRequestTimer()); will(returnValue(BRAND));
 			allowing(brandClient).fetch(with(not(startsWith(BRAND_URI))), with(new NullRequestTimer())); will(returnValue(new Brand()));
 			
 			atLeast(1).of(versionRdfClient).get("http://www.bbc.co.uk/programmes/b00k2vtr.rdf"); will(returnValue(episode2versionRdf));
 		}});
 		
 		Playlist playlist = extractor.extract(source);
 		assertThat(playlist.getCanonicalUri(), is(FEED_URI));
 		assertThat(playlist.getPublisher(), is("bbc.co.uk"));
 
 		System.out.println(playlist.getPlaylists());
 		
 		Brand brand = (Brand) byUri(BRAND_URI, playlist.getPlaylists());
 		
 		assertThat(brand.getCanonicalUri(), is(BRAND_URI));
 		assertThat(brand.getTitle(), is("Spotlight"));
 		
 		Episode episode = (Episode) Iterables.getOnlyElement(brand.getItems());
 		
 		assertThat(episode.getCanonicalUri(), is(EPISODE_2_URI));
 		assertThat(episode.getBrand(), is(brand));
 		
 		assertThat(episode.getTitle(), is("Shutdown"));
 		assertThat(episode.getDescription(), is("Claire Savage investigates a workers sit-in against those that shut the Visteon plant down"));
 		
 		assertThat(episode.getEpisodeNumber(), is(6));
 		
 		assertThat(episode.getPublisher(), is("bbc.co.uk"));
 	
 		
 		Set<String> expectedGenres = bbcGenreUris("factual/politics", "news");
 		expectedGenres.addAll(uriplayGenreUris("news", "factual"));
 		
 		assertThat(episode.getGenres(), is(expectedGenres));
 		assertThat(episode.getIsLongForm(), is(true));
 		
 		assertThat(episode.getThumbnail(), is("http://www.bbc.co.uk/iplayer/images/episode/b00kjbrc_150_84.jpg"));
 		
 		Version version = Iterables.getOnlyElement(episode.getVersions());
 		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
 		
 		Broadcast broadcast = Iterables.getOnlyElement(version.getBroadcasts());
 		
 		assertThat(broadcast.getBroadcastOn(), is("http://www.bbc.co.uk/bbctwo"));
 		
 		Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
 		assertThat(location.getUri(), is("http://www.bbc.co.uk/iplayer/episode/b00kjbrc"));
 		
 		assertThat(location.getTransportType(), is(TransportType.LINK));
 		assertThat(location.getAvailable(), is(true));
 
 		Item orphan = byUri(ORPHAN_ITEM_URI, playlist.getItems());
 		
 		// Check not a subclass
 		assertEquals(Item.class, orphan.getClass());
 	}
 
 	private <T extends Description> T byUri(String uri, Iterable<T> playlists) {
 		for (T description : playlists) {
 			if (uri.equals(description.getCanonicalUri())) {
 				return description;
 			}
 		}
 		throw new NoSuchElementException();
 	}
 
 	private Set<String> bbcGenreUris(String... genres) {
 		Set<String> uris = Sets.newHashSetWithExpectedSize(genres.length);
 		for (String genre : genres) {
 			uris.add("http://www.bbc.co.uk/programmes/genres/" + genre);
 		}
 		return uris;
 	}
 	
 	private Set<String> uriplayGenreUris(String... genres) {
 		Set<String> uris = Sets.newHashSetWithExpectedSize(genres.length);
 		for (String genre : genres) {
 			uris.add("http://uriplay.org/genres/uriplay/" + genre);
 		}
 		return uris;
 	}
 	
 	SyndFeed createFeed(String filename) throws Exception {
 		SyndFeedInput input = new SyndFeedInput();
 		SyndFeed feed = input.build(new XmlReader(new ClassPathResource(filename).getInputStream()));
 		return feed; 
 	}
 }
