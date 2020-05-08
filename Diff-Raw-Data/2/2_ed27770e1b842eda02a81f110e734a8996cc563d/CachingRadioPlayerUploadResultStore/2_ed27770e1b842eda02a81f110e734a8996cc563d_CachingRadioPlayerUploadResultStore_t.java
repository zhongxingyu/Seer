 package org.atlasapi.feeds.radioplayer.upload;
 
 import static org.atlasapi.feeds.upload.FileUploadResult.TYPE_ORDERING;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.atlasapi.feeds.upload.FileUploadResult;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ForwardingMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class CachingRadioPlayerUploadResultStore implements RadioPlayerUploadResultStore {
 
     private final RadioPlayerUploadResultStore delegate;
     private Map<String, RemoteServiceSpecificResultCache> remoteServiceCacheMap;
 
     public CachingRadioPlayerUploadResultStore(Iterable<String> remoteServiceIds, final RadioPlayerUploadResultStore delegate) {
         this.delegate = delegate;
         this.remoteServiceCacheMap = Maps.newHashMap();
         for (String remoteService : remoteServiceIds) {
             remoteServiceCacheMap.put(remoteService, new RemoteServiceSpecificResultCache(remoteService));
         }
     }
 
     @Override
     public void record(RadioPlayerUploadResult result) {
         delegate.record(result);
 
         RemoteServiceSpecificResultCache remoteServiceCache = remoteServiceCacheMap.get(result.getUpload().remote());
         
         ConcurrentMap<LocalDate,Set<FileUploadResult>> serviceMap = remoteServiceCache.get(result.service());
 
         Set<FileUploadResult> current = serviceMap.putIfAbsent(result.day(), treeSetWith(result.getUpload()));
         if (current != null) {
             current.remove(result.getUpload());
             current.add(result.getUpload());
         }
     }
 
     private Set<FileUploadResult> treeSetWith(FileUploadResult result) {
         TreeSet<FileUploadResult> set = Sets.newTreeSet(TYPE_ORDERING);
         set.add(result);
         return set;
     }
 
     @Override
     public Iterable<FileUploadResult> resultsFor(String remoteServiceId, RadioPlayerService service, LocalDate day) {
         return ImmutableSet.copyOf(remoteServiceCacheMap.get(remoteServiceId).get(service).get(day));
     }
     
     private class RemoteServiceSpecificResultCache extends ForwardingMap<RadioPlayerService, ConcurrentMap<LocalDate, Set<FileUploadResult>>>{
 
         private final Map<RadioPlayerService, ConcurrentMap<LocalDate, Set<FileUploadResult>>> cache;
         private final String rsi;
         
         public RemoteServiceSpecificResultCache(String remoteServiceIdentifier) {
             this.rsi = remoteServiceIdentifier;
             this.cache = Maps.newHashMap();
             loadCache();
         }
         
         private void loadCache() {
             for (final RadioPlayerService service : RadioPlayerServices.services) {
                cache.put(service, new MapMaker().softValues().expireAfterWrite(10, TimeUnit.MINUTES).<LocalDate, Set<FileUploadResult>>makeComputingMap(new Function<LocalDate, Set<FileUploadResult>>() {
                     @Override
                     public Set<FileUploadResult> apply(LocalDate day) {
                         TreeSet<FileUploadResult> set = Sets.newTreeSet(TYPE_ORDERING);
                         Iterables.addAll(set, delegate.resultsFor(rsi, service, day));
                         return set;
                     }
                 }));
             }
         }
 
         @Override
         protected Map<RadioPlayerService, ConcurrentMap<LocalDate, Set<FileUploadResult>>> delegate() {
             return cache;
         }
     }
 }
