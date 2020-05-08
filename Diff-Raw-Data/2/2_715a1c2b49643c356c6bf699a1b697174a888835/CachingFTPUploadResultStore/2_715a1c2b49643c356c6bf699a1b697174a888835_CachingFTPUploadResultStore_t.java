 package org.atlasapi.feeds.radioplayer.upload;
 
 import java.util.Set;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Sets;
 
 public class CachingFTPUploadResultStore implements RadioPlayerFTPUploadResultStore {
 
     private static class RadioPlayerServiceDay {
         
         private final RadioPlayerService service;
         private final LocalDate day;
 
         public RadioPlayerServiceDay(RadioPlayerService service, LocalDate day) {
             this.service = service;
             this.day = day;
         }
         
         @Override
         public boolean equals(Object that) {
             if(this == that) {
                 return true;
             }
             if(that instanceof RadioPlayerServiceDay) {
                 RadioPlayerServiceDay other = (RadioPlayerServiceDay) that;
                 return service.equals(other.service) && day.equals(other.day);
             }
             return false;
         }
         
         @Override
         public int hashCode() {
             return Objects.hashCode(service, day);
         }
         
         @Override
         public String toString() {
             return service + ":" + day;
         }
     }
     
     private static class RadioPlayerUploadResultWrapper {
         private final RadioPlayerFTPUploadResult result;
 
         public RadioPlayerUploadResultWrapper(RadioPlayerFTPUploadResult result) {
             this.result = result;
         }
         
         @Override
         public boolean equals(Object that) {
             if (this == that) {
                 return true;
             }
             if(that instanceof RadioPlayerUploadResultWrapper) {
                 RadioPlayerUploadResultWrapper other = (RadioPlayerUploadResultWrapper) that;
                 return Objects.equal(result.service(),other.result.service()) && Objects.equal(result.day(),other.result.day()) && Objects.equal(result.type(),other.result.type());
             }
             return false;
         }
         
         @Override
         public int hashCode() {
             return Objects.hashCode(result.service(), result.day(), result.type());
         }
     }
     
     private final RadioPlayerFTPUploadResultStore delegate;
     private final ConcurrentMap<RadioPlayerServiceDay, Set<RadioPlayerUploadResultWrapper>> cache;
     
     public CachingFTPUploadResultStore(final RadioPlayerFTPUploadResultStore delegate) {
         this.delegate = delegate;
         this.cache = new MapMaker().softValues().expireAfterAccess(30, TimeUnit.MINUTES).makeComputingMap(new Function<RadioPlayerServiceDay, Set<RadioPlayerUploadResultWrapper>>() {
             @Override
             public Set<RadioPlayerUploadResultWrapper> apply(RadioPlayerServiceDay serviceDay) {
                return Sets.newHashSet(Iterables.transform(delegate.resultsFor(serviceDay.service, serviceDay.day), wrap));
             }
         });
     }
     
     @Override
     public void record(RadioPlayerFTPUploadResult result) {
         delegate.record(result);
         RadioPlayerUploadResultWrapper wrappedResult = new RadioPlayerUploadResultWrapper(result);
         Set<RadioPlayerUploadResultWrapper> current = cache.putIfAbsent(new RadioPlayerServiceDay(result.service(), result.day()), Sets.newHashSet(wrappedResult));
         if(current != null) {
             current.remove(wrappedResult);
             current.add(wrappedResult);
         }
     }
 
     @Override
     public Set<RadioPlayerFTPUploadResult> resultsFor(RadioPlayerService service, LocalDate day) {
         return ImmutableSet.copyOf(Iterables.transform(cache.get(new RadioPlayerServiceDay(service,day)), unwrap));
     }
 
     private final Function<RadioPlayerFTPUploadResult, RadioPlayerUploadResultWrapper> wrap = new Function<RadioPlayerFTPUploadResult, RadioPlayerUploadResultWrapper>() {
         @Override
         public RadioPlayerUploadResultWrapper apply(RadioPlayerFTPUploadResult result) {
             return new RadioPlayerUploadResultWrapper(result);
         }
     };
     
     private final Function<RadioPlayerUploadResultWrapper, RadioPlayerFTPUploadResult> unwrap = new Function<RadioPlayerUploadResultWrapper, RadioPlayerFTPUploadResult>() {
         @Override
         public RadioPlayerFTPUploadResult apply(RadioPlayerUploadResultWrapper wrapper) {
             return wrapper.result;
         }
     };
 }
