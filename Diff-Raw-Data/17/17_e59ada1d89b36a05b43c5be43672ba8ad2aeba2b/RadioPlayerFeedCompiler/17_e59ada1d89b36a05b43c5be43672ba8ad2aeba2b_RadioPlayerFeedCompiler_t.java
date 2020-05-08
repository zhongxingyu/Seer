 package org.atlasapi.feeds.radioplayer;
 
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
 import static com.google.common.collect.Iterables.concat;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.atlasapi.feeds.radioplayer.outputting.NoItemsException;
 import org.atlasapi.feeds.radioplayer.outputting.RadioPlayerBroadcastItem;
 import org.atlasapi.feeds.radioplayer.outputting.RadioPlayerProgrammeInformationOutputter;
 import org.atlasapi.feeds.radioplayer.outputting.RadioPlayerXMLOutputter;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.LookupRef.LookupType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public abstract class RadioPlayerFeedCompiler {
     
     private final RadioPlayerXMLOutputter outputter;
     protected final ScheduleResolver scheduleResolver;
     protected final KnownTypeContentResolver contentResolver;
 
     public RadioPlayerFeedCompiler(RadioPlayerXMLOutputter outputter, ScheduleResolver scheduleResolver, KnownTypeContentResolver contentResolver) {
         this.outputter = outputter;
         this.scheduleResolver = scheduleResolver;
         this.contentResolver = contentResolver;
     }
     
     private static Map<String, RadioPlayerFeedCompiler> compilerMap;
     
     public static void init(ScheduleResolver scheduleResolver, KnownTypeContentResolver contentResolver) {
         compilerMap = ImmutableMap.<String, RadioPlayerFeedCompiler>of(
                 "PI",new RadioPlayerProgrammeInformationFeedCompiler(scheduleResolver, contentResolver)
             );
     }
     
     public static RadioPlayerFeedCompiler valueOf(String type) {
        checkState(compilerMap != null, "Compiler map not initialised");
        return checkNotNull(compilerMap.get(type), "No compiler for type " + type);
     }
 	
     private static class RadioPlayerProgrammeInformationFeedCompiler extends RadioPlayerFeedCompiler {
         public RadioPlayerProgrammeInformationFeedCompiler(ScheduleResolver scheduleResolver, KnownTypeContentResolver contentResolver) {
             super(new RadioPlayerProgrammeInformationOutputter(), scheduleResolver, contentResolver);
         }
 
         @Override
         public List<Item> queryFor(LocalDate day, String serviceUri) {
             DateTime date = day.toDateTimeAtStartOfDay(DateTimeZones.UTC);
             Channel channel = Channel.fromUri(serviceUri).requireValue();
             Schedule schedule = scheduleResolver.schedule(date.minusMillis(1), date.plusDays(1), ImmutableSet.of(channel), ImmutableSet.of(Publisher.BBC));
             return Iterables.getOnlyElement(schedule.scheduleChannels()).items();
         }
     }
     
 	public abstract List<Item> queryFor(LocalDate date, String serviceUri);
 	
     public RadioPlayerXMLOutputter getOutputter() {
         if (outputter == null) {
             throw new UnsupportedOperationException(this.toString() + " feeds are not currently supported");
         }
         return outputter;
     }
 
     public void compileFeedFor(LocalDate day, RadioPlayerService service, OutputStream out) throws IOException {
         if (outputter != null) {
             String serviceUri = service.getServiceUri();
             List<Item> items = queryFor(day, serviceUri);
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
         final Map<String, Identified> containers = containersFor(items);
         return ImmutableList.copyOf(concat(Iterables.transform(items, new Function<Item, Iterable<RadioPlayerBroadcastItem>>() {
             @Override
             public Iterable<RadioPlayerBroadcastItem> apply(Item item) {
                 ArrayList<RadioPlayerBroadcastItem> broadcastItems = Lists.newArrayList();
                 for (Version version : item.nativeVersions()) {
                     for (Broadcast broadcast : version.getBroadcasts()) {
                         RadioPlayerBroadcastItem broadcastItem = new RadioPlayerBroadcastItem(item, version, broadcast);
                         if(item.getContainer() != null && containers.containsKey(item.getContainer().getUri())) {
                             broadcastItem.withContainer((Container<?>)containers.get(item.getContainer().getUri()));
                         }
                         broadcastItems.add(broadcastItem);
                     }
                 }
                 return broadcastItems;
             }
         })));
     }
 
     private Map<String, Identified> containersFor(List<Item> items) {
         Iterable<LookupRef> containerLookups = Iterables.filter(Iterables.transform(items, new Function<Item, LookupRef>() {
 
             @Override
             public LookupRef apply(Item input) {
                 if(input.getContainer() != null) {
                     return new LookupRef(input.getContainer().getUri(), input.getPublisher(), LookupType.CONTAINER);
                 }
                 return null;
             }
             
         }),Predicates.notNull());
         
         if(Iterables.isEmpty(containerLookups)) {
             return ImmutableMap.of();
         }
         
        return contentResolver.findByLookupRefs(ImmutableSet.copyOf(containerLookups)).asResolvedMap();
     }
 }
