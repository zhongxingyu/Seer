 package org.atlasapi.equiv.update.tasks;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.equiv.update.EquivalenceUpdater;
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.Schedule.ScheduleChannel;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.junit.Test;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 
 
 public class ScheduleEquivalenceUpdateTaskTest {
 
     @SuppressWarnings("unchecked") 
     private final EquivalenceUpdater<Content> updater = mock(EquivalenceUpdater.class);
     private final ContentResolver contentResolver = mock(ContentResolver.class);
 
     // TODO make this take multiple schedules
     private final ScheduleResolver scheduleResolver(final Schedule schedule) {
         return new ScheduleResolver() {
             
             @Override
             public Schedule schedule(DateTime from, DateTime to, Iterable<Channel> channels,
                     Iterable<Publisher> publisher, Optional<ApplicationConfiguration> mergeConfig) {
                 return schedule;
             }
         };
     };
     
     @Test
     public void testUpdateScheduleEquivalences() {
         
         Channel bbcOne = new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", false, MediaType.VIDEO, "bbconeuri");
         
         Item yvItemOne = new Item("yv1", "yv1c", Publisher.YOUVIEW);
         Item yvItemTwo = new Item("yv2", "yv2c", Publisher.YOUVIEW);
         
         DateTime now = new DateTime().withZone(DateTimeZone.UTC);
         
         ScheduleChannel schChannel1 = new ScheduleChannel(bbcOne, ImmutableList.of(yvItemOne, yvItemTwo));
        Schedule schedule1 = new Schedule(ImmutableList.of(schChannel1), new Interval(now, now.plusDays(1)));
         
         ScheduleResolver resolver = scheduleResolver(schedule1);
         
         ScheduleEquivalenceUpdateTask.builder()
            .withBack(Duration.standardDays(0))
            .withForward(Duration.standardDays(1))
             .withPublishers(ImmutableList.of(Publisher.YOUVIEW))
             .withChannels(ImmutableList.of(bbcOne))
             .withScheduleResolver(resolver)
             .withUpdater(updater)
             .build().run();
         
         
         verify(updater).updateEquivalences(yvItemOne);
         verify(updater).updateEquivalences(yvItemTwo);
     }
 
 }
