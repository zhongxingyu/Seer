 package org.atlasapi.remotesite.channel4;
 
  import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.startsWith;
 import junit.framework.TestCase;
 
 import org.atlasapi.genres.AtlasGenre;
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.io.Resources;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.sun.syndication.feed.atom.Feed;
 
 @RunWith(JMock.class)
 public class C4BrandBasicDetailsExtractorTest extends TestCase {
 
     private final Mockery context = new Mockery();
 	private C4BrandBasicDetailsExtractor extractor;
 	
 	@Before
 	public void setUp() {
 		final ChannelResolver channelResolver = context.mock(ChannelResolver.class);
 		context.checking(new Expectations() {
 			{
 				one(channelResolver).fromUri("http://www.channel4.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "Channel 4", "channel4", MediaType.VIDEO, "http://www.channel4.com"))));
 				one(channelResolver).fromUri("http://www.channel4.com/more4");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "More4", "more4", MediaType.VIDEO, "http://www.more4.com"))));
 				one(channelResolver).fromUri("http://film4.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "Film4", "more4", MediaType.VIDEO, "http://film4.com"))));
 				one(channelResolver).fromUri("http://www.e4.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "E4", "more4", MediaType.VIDEO, "http://www.e4.com"))));
 				one(channelResolver).fromUri("http://www.4music.com");
 				will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "4Music", "more4", MediaType.VIDEO, "http://www.4music.com"))));
				allowing(channelResolver).fromUri("http://www.channel4.com/4seven");
                will(returnValue(Maybe.just(new Channel(Publisher.METABROADCAST, "4seven", "4seven", MediaType.VIDEO, "http://www.channel4.com/4seven"))));
 			}
 		});
 		extractor = new C4BrandBasicDetailsExtractor(channelResolver);
 	}
 	
     @Test
 	public void testExtractingABrand() throws Exception {
 		
 		
 		AtomFeedBuilder brandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares.atom"));
 		
 		Brand brand = extractor.extract(brandFeed.build());
 		
 		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
 		assertThat(brand.getAliases(), hasItem("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od"));
 		assertThat(brand.getAliases(), hasItem("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares"));
 		assertThat(brand.getCurie(), is("c4:ramsays-kitchen-nightmares"));
 		assertThat(brand.getTitle(), is("Ramsay's Kitchen Nightmares"));
 		assertThat(brand.getLastUpdated(), is(new DateTime("2010-11-17T17:35:38.468Z", DateTimeZones.UTC)));
 		assertThat(brand.getPublisher(), is(Publisher.C4));
 		assertThat(brand.getDescription(), startsWith("Gordon Ramsay attempts to transform struggling restaurants with his"));
 		assertThat(brand.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_200x113.jpg"));
 		assertThat(brand.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_625x352.jpg"));
 		assertThat(brand.getGenres(), hasItems(
 		        "http://www.channel4.com/programmes/tags/food",
 		        "http://www.channel4.com/programmes/tags/lifestyle",
 		        AtlasGenre.LIFESTYLE.getUri()
 		));
 	}
 
     @Test
 	public void testThatNonBrandPagesAreRejected() throws Exception {
 		checkIllegalArgument("an id");
 		checkIllegalArgument("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/video");
 		checkIllegalArgument("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide");
 		checkIllegalArgument("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-1");
 	}
 
 	private void checkIllegalArgument(String feedId) {
 		Feed feed = new Feed();
 		feed.setId(feedId);
 		try {
 			extractor.extract(feed);
 			fail("ID " + feedId + " should not be accepted");
 		} catch (IllegalArgumentException e) {
 			assertThat(e.getMessage(), startsWith("Not a brand feed"));
 		}
 	}
 }
