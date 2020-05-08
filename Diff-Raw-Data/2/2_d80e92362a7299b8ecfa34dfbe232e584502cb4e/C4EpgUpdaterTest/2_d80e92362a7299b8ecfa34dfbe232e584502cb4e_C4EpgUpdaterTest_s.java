 package org.atlasapi.remotesite.channel4.epg;
 
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.endsWith;
 import static org.hamcrest.Matchers.hasKey;
 import static org.hamcrest.Matchers.hasValue;
 import static org.hamcrest.Matchers.isIn;
 
 import java.io.InputStreamReader;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.Element;
 
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.persistence.testing.StubContentResolver;
 import org.atlasapi.remotesite.channel4.C4BrandUpdater;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.DayRangeGenerator;
 
 //TODO: extract a SiteSpecificAdapter or similar from C4EpgUpdater which handles a single channel day feed.
 @RunWith(JMock.class)
 public class C4EpgUpdaterTest extends TestCase {
     
 	private static final Channel CHANNEL_FOUR = new Channel(Publisher.METABROADCAST, "Channel 4", "channel4", MediaType.VIDEO, "http://www.channel4.com");
 	private static final Channel E_FOUR = new Channel(Publisher.METABROADCAST, "E4", "e4", MediaType.VIDEO, "http://www.e4.com");
 	private static final Channel FILM_4 = new Channel(Publisher.METABROADCAST, "Film4", "film4", MediaType.VIDEO, "http://www.film4.com");
 	private static final Channel FOUR_MUSIC = new Channel(Publisher.METABROADCAST, "4 Music", "4music", MediaType.VIDEO, "http://www.4music.com");
 	private static final Channel MORE_FOUR = new Channel(Publisher.METABROADCAST, "More4", "more4", MediaType.VIDEO, "http://www.channel4.com/more4");
 
 	
     private final Mockery context = new Mockery();
     
     private final Builder builder = new Builder(new C4EpgElementFactory());
     private Document c4EpgFeed;
     
     @SuppressWarnings("unchecked")
     private final RemoteSiteClient<Document> c4AtomFetcher = context.mock(RemoteSiteClient.class);
     private final ContentWriter contentWriter = context.mock(ContentWriter.class);
     private final C4BrandUpdater brandUpdater = context.mock(C4BrandUpdater.class);
     private final DateTime day = new DateTime(DateTimeZones.LONDON).withTime(6, 0, 0, 0);
     
     private final AdapterLog log = new NullAdapterLog();
     private final ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
     
     private final BroadcastTrimmer trimmer = context.mock(BroadcastTrimmer.class);
     
     private final C4EpgEntryProcessor entryProcessor = new C4EpgEntryProcessor(contentWriter, resolver, brandUpdater, log);
     private final C4EpgBrandlessEntryProcessor brandlessProcessor = new C4EpgBrandlessEntryProcessor(contentWriter, resolver, brandUpdater, log);
     private C4EpgUpdater updater;
     private ChannelResolver channelResolver;
     
     @Override
     @Before
     public void setUp() throws Exception {
         c4EpgFeed = builder.build(new InputStreamReader(Resources.getResource("c4-epg-2011-09-13.atom").openStream()));
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
 			}
 		});
 		updater = new C4EpgUpdater(c4AtomFetcher, entryProcessor, brandlessProcessor, trimmer, log, new DayRangeGenerator(), channelResolver);
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testRun() throws Exception {
         
         context.checking(new Expectations() {{
             one(c4AtomFetcher).get(with(endsWith(String.format("%s/C4.atom", day.toDateTime(DateTimeZones.UTC).toString("yyyy/MM/dd")))));
                 will(returnValue(c4EpgFeed));
             allowing(c4AtomFetcher).get(with(any(String.class)));
                 will(returnValue(new Document(new Element("feed"))));
             allowing(brandUpdater).createOrUpdateBrand(with(any(String.class))); will(throwException(new RuntimeException())); //this causes a new brand to be created
             allowing(contentWriter).createOrUpdate(with(any(Container.class)));
             allowing(contentWriter).createOrUpdate(with(any(Item.class)));
             one(trimmer).trimBroadcasts(
                     with(new Interval(day, day.plusDays(1))), 
                     with(CHANNEL_FOUR),
                     (Map<String,String>)with(allOf(
                             hasKey("c4:25939874"),hasValue("http://www.channel4.com/programmes/the-hoobs/episode-guide/series-1/episode-51"),
                             hasKey("c4:25955760"),hasValue("http://www.channel4.com/programmes/synthesized/25955760")
                     ))
             );
             allowing(trimmer).trimBroadcasts(with(new Interval(day, day.plusDays(1))), with(isIn(ImmutableSet.of(MORE_FOUR, E_FOUR, FILM_4, FOUR_MUSIC))), with(any(Map.class)));
         }});
         
         updater.run();
         
         context.assertIsSatisfied();
     }
 }
