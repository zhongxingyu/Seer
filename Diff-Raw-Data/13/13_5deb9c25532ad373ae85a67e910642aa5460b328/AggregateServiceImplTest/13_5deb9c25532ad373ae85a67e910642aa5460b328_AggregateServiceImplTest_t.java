 package pl.rawie.timetrack.domain.service.impl;
 
 import org.hamcrest.Matcher;
import org.joda.time.Duration;
 import org.junit.Before;
 import org.junit.Test;
 import pl.rawie.timetrack.domain.model.AggregateEntry;
 import pl.rawie.timetrack.domain.model.Entry;
 import pl.rawie.timetrack.domain.model.SampleAggregateEntry;
 import pl.rawie.timetrack.domain.model.SampleEntry;
 import pl.rawie.timetrack.domain.service.AggregateService;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
 import static org.junit.Assert.assertThat;
 
 public class AggregateServiceImplTest {
     private AggregateService service;
 
     @Before
     public void setUp() {
         service = new AggregateServiceImpl();
     }
 
     private void assertAggregate(List<Entry> entries, Matcher... matchers) {
         List<AggregateEntry> aggregates = service.aggregate(entries);
         assertThat(matchers.length, equalTo(aggregates.size()));
         Iterator<AggregateEntry> iterator = aggregates.iterator();
         for (Matcher matcher : matchers)
             assertThat(iterator.next(), matcher);
     }
 
     private void assertNormalize(List<AggregateEntry> aggregates, Matcher... matchers) {
         List<AggregateEntry> normalized = service.normalize(aggregates);
         assertThat(matchers.length, equalTo(normalized.size()));
         Iterator<AggregateEntry> iterator = normalized.iterator();
         for (Matcher matcher : matchers)
             assertThat(iterator.next(), matcher);
     }
 
     @Test
     public void aggregate_noEntries() {
         assertAggregate(Arrays.<Entry>asList());
     }
 
     @Test
     public void aggregate_singleEntries() {
         List<Entry> entries = Arrays.asList(SampleEntry.entry());
         assertAggregate(entries, hasProperty("entries", equalTo(entries)));
     }
 
     @Test
     public void aggregate_sameTypeEntries() {
         List<Entry> entries = Arrays.asList(SampleEntry.entry(), SampleEntry.entry());
         assertAggregate(entries, hasProperty("entries", equalTo(entries)));
     }
 
     @Test
     public void aggregate_differentTypeEntries() {
         Entry entry1 = SampleEntry.builder().withSummary("summary-1").build();
         Entry entry2 = SampleEntry.builder().withSummary("summary-2").build();
         List<Entry> entries = Arrays.asList(entry1, entry2);
         assertAggregate(entries,
                 hasProperty("entries", equalTo(Arrays.asList(entry1))),
                 hasProperty("entries", equalTo(Arrays.asList(entry2))));
     }
 
     @Test
     public void normalize_noAggregates() {
         assertNormalize(Collections.<AggregateEntry>emptyList());
     }
 
     @Test
     public void normalize_singleAggregate() {
         AggregateEntry aggregate = SampleAggregateEntry.withDurationInHours(1);
         List<AggregateEntry> aggregates = Arrays.asList(aggregate);
        assertNormalize(aggregates, hasProperty("normalizedDuration", equalTo(Duration.standardHours(1))));
     }
 }
