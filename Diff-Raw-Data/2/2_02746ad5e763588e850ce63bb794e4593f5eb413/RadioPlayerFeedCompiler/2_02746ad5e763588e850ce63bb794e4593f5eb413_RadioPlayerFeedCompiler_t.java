 package org.atlasapi.feeds.radioplayer;
 
 import static com.google.common.collect.Iterables.concat;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.atlasapi.content.criteria.AtomicQuery;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.content.criteria.attribute.Attributes;
 import org.atlasapi.content.criteria.operator.Operators;
 import org.atlasapi.feeds.radioplayer.outputting.NoItemsException;
 import org.atlasapi.feeds.radioplayer.outputting.RadioPlayerBroadcastItem;
 import org.atlasapi.feeds.radioplayer.outputting.RadioPlayerProgrammeInformationOutputter;
 import org.atlasapi.feeds.radioplayer.outputting.RadioPlayerXMLOutputter;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public abstract class RadioPlayerFeedCompiler {
     
     private final RadioPlayerXMLOutputter outputter;
     private final KnownTypeQueryExecutor executor;
 
     public RadioPlayerFeedCompiler(RadioPlayerXMLOutputter outputter, KnownTypeQueryExecutor executor) {
         this.outputter = outputter;
         this.executor = executor;
     }
     
     private static Map<String, RadioPlayerFeedCompiler> compilerMap;
     
     public static void init(KnownTypeQueryExecutor queryExecutor) {
         compilerMap = ImmutableMap.of(
                 "PI",new RadioPlayerProgrammeInformationFeedCompiler(queryExecutor),
                 "OD",new RadioPlayerOnDemandFeedCompiler(queryExecutor));
     }
     
     public static RadioPlayerFeedCompiler valueOf(String type) {
         return compilerMap.get(type);
     }
 	
     private static class RadioPlayerProgrammeInformationFeedCompiler extends RadioPlayerFeedCompiler {
         public RadioPlayerProgrammeInformationFeedCompiler(KnownTypeQueryExecutor executor) {
             super(new RadioPlayerProgrammeInformationOutputter(), executor);
         }
 
         @Override
         public ContentQuery queryFor(LocalDate day, String serviceUri) {
             DateTime date = day.toDateTimeAtStartOfDay(DateTimeZones.UTC);
             Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
                     Attributes.DESCRIPTION_PUBLISHER.createQuery(Operators.EQUALS, ImmutableList.of(Publisher.BBC)),
                     Attributes.BROADCAST_ON.createQuery(Operators.EQUALS, ImmutableList.of(serviceUri)),
                    Attributes.BROADCAST_TRANSMISSION_TIME.createQuery(Operators.AFTER, ImmutableList.of(date.minusMillis(1))),
                     Attributes.BROADCAST_TRANSMISSION_TIME.createQuery(Operators.BEFORE, ImmutableList.of(date.plusDays(1)))
             );
 
             return new ContentQuery(queryAtoms);
         }
     }
     
     private static class RadioPlayerOnDemandFeedCompiler extends RadioPlayerFeedCompiler {
         public RadioPlayerOnDemandFeedCompiler(KnownTypeQueryExecutor executor) {
             super(null, executor);
         }
 
         @Override
         public ContentQuery queryFor(LocalDate broadcastOn, String serviceUri) {
             Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
                     Attributes.BROADCAST_ON.createQuery(Operators.EQUALS, ImmutableList.of(serviceUri)),
                     Attributes.LOCATION_AVAILABLE.createQuery(Operators.EQUALS, ImmutableList.of(Boolean.TRUE))
             );
             return new ContentQuery(queryAtoms);
         }
     }
 
 	public abstract ContentQuery queryFor(LocalDate date, String serviceUri);
 	
     public RadioPlayerXMLOutputter getOutputter() {
         if (outputter == null) {
             throw new UnsupportedOperationException(this.toString() + " feeds are not currently supported");
         }
         return outputter;
     }
 
     public void compileFeedFor(LocalDate day, RadioPlayerService service, OutputStream out) throws IOException {
         if (outputter != null) {
             String serviceUri = service.getServiceUri();
             List<Item> items = executor.schedule(queryFor(day, serviceUri)).getItemsFromOnlyChannel();
             if (items.isEmpty()) {
                 throw new NoItemsException(day, service);
             }
             outputter.output(day, service, sort(transform(items, serviceUri, day)), out);
         }
     }
 
     private List<RadioPlayerBroadcastItem> sort(Iterable<RadioPlayerBroadcastItem> broadcastItems) {
         return Ordering.natural().immutableSortedCopy(broadcastItems);
     }
 
     private List<RadioPlayerBroadcastItem> transform(List<Item> items, String serviceUri, LocalDate day) {
         return ImmutableList.copyOf(concat(Iterables.transform(items, new Function<Item, Iterable<RadioPlayerBroadcastItem>>() {
             @Override
             public Iterable<RadioPlayerBroadcastItem> apply(Item item) {
                 ArrayList<RadioPlayerBroadcastItem> broadcastItems = Lists.newArrayList();
                 for (Version version : item.getVersions()) {
                     for (Broadcast broadcast : version.getBroadcasts()) {
                         broadcastItems.add(new RadioPlayerBroadcastItem(item, version, broadcast));
                     }
                 }
                 return broadcastItems;
             }
         })));
     }
 }
