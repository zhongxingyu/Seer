 package org.atlasapi.remotesite.channel4.epg;
 
 import static org.hamcrest.Matchers.endsWith;
 
 import java.io.InputStreamReader;
 import java.util.List;
 
 import junit.framework.TestCase;
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.Element;
 
 import org.atlasapi.StubContentResolver;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.DayRangeGenerator;
 
 public class C4EpgUpdaterTest extends TestCase {
     
     private final Mockery context = new Mockery();
     
     private final Builder builder = new Builder(new C4EpgElementFactory());
     private Document c4EpgFeed;
     
     @SuppressWarnings("unchecked")
     private final RemoteSiteClient<Document> c4AtomFetcher = context.mock(RemoteSiteClient.class);
     private final ContentWriter contentWriter = context.mock(ContentWriter.class);
     private final ScheduleResolver scheduleResolver = context.mock(ScheduleResolver.class);
     private final DateTime day = new DateTime();
     
     private final AdapterLog log = new NullAdapterLog();
     private final ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
     
     private final BroadcastTrimmer trimmer = new BroadcastTrimmer(Publisher.C4, scheduleResolver, resolver,  contentWriter, log);
     
    private final C4EpgUpdater updater = new C4EpgUpdater(c4AtomFetcher, new C4EpgEntryProcessor(contentWriter, resolver, log), trimmer, log, new DayRangeGenerator());
     
     @Override
     public void setUp() throws Exception {
         c4EpgFeed = builder.build(new InputStreamReader(Resources.getResource("c4-epg-2011-01-07.atom").openStream()));
     }
 
     @SuppressWarnings("unchecked")
     public void testRun() throws Exception {
         final Schedule schedule = Schedule.fromChannelMap(ImmutableMap.<Channel, List<Item>>of(Channel.CHANNEL_FOUR, ImmutableList.<Item>of()), new Interval(day, day.plusDays(1)));
         
         
         context.checking(new Expectations() {{
             one(c4AtomFetcher).get(with(endsWith(String.format("%s/C4.atom", new DateTime(DateTimeZones.UTC).toString("yyyy/MM/dd")))));
                 will(returnValue(c4EpgFeed));
             allowing(c4AtomFetcher).get(with(any(String.class)));
                 will(returnValue(new Document(new Element("feed"))));
             allowing(contentWriter).createOrUpdate(with(any(Container.class)));
             allowing(scheduleResolver).schedule(with(any(DateTime.class)), with(any(DateTime.class)), with(any(Iterable.class)), with(any(Iterable.class))); will(returnValue(schedule));
         }});
         
         updater.run();
         
     }
 }
