 package pl.rawie.timetrack.domain.service.impl;
 
 import pl.rawie.timetrack.domain.model.AggregateEntry;
 import pl.rawie.timetrack.domain.model.Entry;
 import pl.rawie.timetrack.domain.service.AggregateService;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class AggregateServiceImpl implements AggregateService {
     @Override
     public List<AggregateEntry> aggregate(List<Entry> entries) {
         return new Aggregator().aggregate(entries);
     }
 
     @Override
     public List<AggregateEntry> normalize(List<AggregateEntry> aggregates) {
        return aggregates;
     }
 }
 
 class Aggregator {
     private List<List<Entry>> aggregates = new ArrayList<List<Entry>>();
 
     public List<AggregateEntry> aggregate(List<Entry> entries) {
         for (Entry entry : entries) {
             List<Entry> aggregate = getOrCreateAggregateFor(entry);
             aggregate.add(entry);
             if (!aggregates.contains(aggregate))
                 aggregates.add(aggregate);
         }
         return makeAggregates();
     }
 
     private List<Entry> getOrCreateAggregateFor(Entry entry) {
         for (List<Entry> aggregate : aggregates)
             if (entry.sameAs(aggregate.get(0)))
                 return aggregate;
         return new ArrayList<Entry>();
     }
 
     private List<AggregateEntry> makeAggregates() {
         List<AggregateEntry> result = new ArrayList<AggregateEntry>();
         for (List<Entry> aggregate : aggregates)
             result.add(new AggregateEntry(aggregate));
         return result;
     }
 }
