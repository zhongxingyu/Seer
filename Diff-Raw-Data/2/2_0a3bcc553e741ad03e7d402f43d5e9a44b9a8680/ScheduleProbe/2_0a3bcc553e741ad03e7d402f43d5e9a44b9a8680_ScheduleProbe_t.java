 package org.atlasapi.remotesite.health;
 
 import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.FAILURE;
 import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.INFO;
 import static org.atlasapi.content.criteria.attribute.Attributes.BROADCAST_ON;
 import static org.atlasapi.content.criteria.attribute.Attributes.BROADCAST_TRANSMISSION_TIME;
 import static org.atlasapi.content.criteria.attribute.Attributes.DESCRIPTION_PUBLISHER;
 import static org.atlasapi.content.criteria.operator.Operators.AFTER;
 import static org.atlasapi.content.criteria.operator.Operators.BEFORE;
 import static org.atlasapi.content.criteria.operator.Operators.EQUALS;
 import static org.atlasapi.media.entity.Schedule.ScheduleEntry.TO_ITEM;
 
 import java.util.List;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.content.criteria.AtomicQuery;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.Schedule.ScheduleEntry;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.health.ProbeResult;
 import com.metabroadcast.common.health.ProbeResult.ProbeResultEntry;
 import com.metabroadcast.common.time.Clock;
 
 public class ScheduleProbe implements HealthProbe {
 
     private static final Duration MAX_GAP = Duration.standardMinutes(5);
     private static final Duration MAX_STALENESS = Duration.standardHours(1);
     
     private final Publisher publisher;
     private final Channel channel;
     private final KnownTypeQueryExecutor queryExecutor;
     private final Clock clock;
 
     public ScheduleProbe(Publisher publisher, Channel channel, KnownTypeQueryExecutor queryExecutor, Clock clock) {
         this.publisher = publisher;
         this.channel = channel;
         this.queryExecutor = queryExecutor;
         this.clock = clock;
     }
 
     @Override
     public ProbeResult probe() throws Exception {
         ProbeResult result = new ProbeResult(title());
 
         Schedule schedule = queryExecutor.schedule(scheduleQuery());
         
         List<ScheduleEntry> scheduleEntries = schedule.getEntriesForOnlyChannel();
         result.addEntry(scheduleSize(scheduleEntries.size()));
 
         if(scheduleEntries.isEmpty()) {
             return result;
         }
 
         addContiguityEntries(schedule, result);
         addLastFetchedCheckEntry(schedule, result);
 
         return result;
     }
 
     private ProbeResultEntry scheduleSize(int scheduleEntries) {
         return new ProbeResultEntry(scheduleEntries > 0 ? INFO : FAILURE, "Schedule Entries", String.valueOf(scheduleEntries));
     }
 
     private void addContiguityEntries(Schedule schedule, ProbeResult result) {
         List<ScheduleEntry> entries = schedule.getEntriesForOnlyChannel();
         
         int breaks = 0, overlaps = 0;
         DateTime lastEnd = entries.get(0).broadcast().getTransmissionTime();
 
         for (ScheduleEntry entry : entries) {
             DateTime transmissionStart = entry.broadcast().getTransmissionTime();
             
             if(transmissionStart.isAfter(lastEnd.plus(MAX_GAP))) {
                 breaks++;
             } else if (transmissionStart.isBefore(lastEnd)) {
                 overlaps++;
             }
             
             lastEnd = entry.broadcast().getTransmissionEndTime();
         }
 
         result.add("Schedule Breaks", String.valueOf(breaks), !(breaks > 0));
         result.add("Schedule Overlaps", String.valueOf(overlaps), !(overlaps > 0));
     }
 
     private void addLastFetchedCheckEntry(Schedule schedule, ProbeResult result) {
         Iterable<Item> items = Iterables.transform(schedule.getEntriesForOnlyChannel(), TO_ITEM);
         
         DateTime oldestFetch = clock.now();
         
         for (Item item : items) {
             
             if(item.getLastFetched().isBefore(oldestFetch)) {
                 oldestFetch = item.getLastFetched();
             }
             
         }
         
        result.add("Oldest Fetch", oldestFetch.toString("HH:mm:ss dd/MM/yyyy"), oldestFetch.isAfter(clock.now().minus(MAX_STALENESS)));
     }
 
     private ContentQuery scheduleQuery() {
         DateTime date = clock.now().withTime(0, 0, 0, 0);
         return new ContentQuery(ImmutableSet.<AtomicQuery> of(
             DESCRIPTION_PUBLISHER.createQuery(EQUALS, ImmutableList.of(publisher)),
             BROADCAST_ON.createQuery(EQUALS, ImmutableList.of(channel.uri())), 
             BROADCAST_TRANSMISSION_TIME.createQuery(AFTER, ImmutableList.of(date.minusMillis(1))),
             BROADCAST_TRANSMISSION_TIME.createQuery(BEFORE, ImmutableList.of(date.plusDays(1)))
         )).copyWithApplicationConfiguration(ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithIncludedPublishers(ImmutableList.of(publisher)));
     }
 
     @Override
     public String title() {
         return String.format("Schedule %s: %s", publisher.title(), channel.title());
     }
 
     @Override
     public String slug() {
         return String.format("%s%sschedule", publisher.name(), channel.key());
     }
 
 }
