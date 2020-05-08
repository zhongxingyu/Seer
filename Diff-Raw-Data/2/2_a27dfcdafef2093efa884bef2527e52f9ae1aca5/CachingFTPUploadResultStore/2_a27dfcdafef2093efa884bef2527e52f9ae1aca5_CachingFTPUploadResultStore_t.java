 package org.atlasapi.feeds.radioplayer.upload;
 
 import java.util.Comparator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class CachingFTPUploadResultStore implements RadioPlayerFTPUploadResultStore {
 
     private final RadioPlayerFTPUploadResultStore delegate;
     private final Map<RadioPlayerService, ConcurrentMap<LocalDate, Set<RadioPlayerFTPUploadResult>>> cache;
 
     public CachingFTPUploadResultStore(final RadioPlayerFTPUploadResultStore delegate) {
         this.delegate = delegate;
         this.cache = Maps.newHashMap();
         loadCache();
     }
 
     private void loadCache() {
         for (final RadioPlayerService service : RadioPlayerServices.services) {
            cache.put(service, new MapMaker().softValues().expireAfterWrite(30, TimeUnit.MINUTES).<LocalDate, Set<RadioPlayerFTPUploadResult>>makeComputingMap(new Function<LocalDate, Set<RadioPlayerFTPUploadResult>>() {
                 @Override
                 public Set<RadioPlayerFTPUploadResult> apply(LocalDate day) {
                     TreeSet<RadioPlayerFTPUploadResult> set = Sets.newTreeSet(resultComparator);
                     set.addAll(delegate.resultsFor(service, day));
                     return set;
                 }
             }));
         }
     }
 
     @Override
     public void record(RadioPlayerFTPUploadResult result) {
         delegate.record(result);
 
         ConcurrentMap<LocalDate, Set<RadioPlayerFTPUploadResult>> serviceMap = cache.get(result.service());
 
         Set<RadioPlayerFTPUploadResult> current = serviceMap.putIfAbsent(result.day(), treeSetWith(result));
         if (current != null) {
             current.remove(result);
             current.add(result);
         }
     }
 
     private Set<RadioPlayerFTPUploadResult> treeSetWith(RadioPlayerFTPUploadResult result) {
         TreeSet<RadioPlayerFTPUploadResult> set = Sets.newTreeSet(resultComparator);
         set.add(result);
         return set;
     }
 
     @Override
     public Set<RadioPlayerFTPUploadResult> resultsFor(RadioPlayerService service, LocalDate day) {
         return ImmutableSet.copyOf(cache.get(service).get(day));
     }
 
     private final Comparator<RadioPlayerFTPUploadResult> resultComparator = new Comparator<RadioPlayerFTPUploadResult>() {
 
         @Override
         public int compare(RadioPlayerFTPUploadResult r1, RadioPlayerFTPUploadResult r2) {
             int compareDays = r1.day().compareTo(r2.day());
             if (compareDays != 0) {
                 return compareDays;
             }
             return r1.type().compareTo(r2.type());
         }
 
     };
 }
