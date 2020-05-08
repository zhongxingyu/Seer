 package org.atlasapi.feeds.radioplayer.upload;
 
 import static org.atlasapi.feeds.radioplayer.upload.FTPUploadResult.FTPUploadResultType.SUCCESS;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.Set;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class CachingFTPUploadResultStoreTest {
 
     private Mockery context = new Mockery();
     private RadioPlayerFTPUploadResultStore delegateStore = context.mock(RadioPlayerFTPUploadResultStore.class);
     
     private LocalDate day = new LocalDate(DateTimeZones.UTC);
     private RadioPlayerService service = RadioPlayerServices.all.get("300");
 
     @Test
     public void testBasicRecordAndRead() {
 
         context.checking(new Expectations(){{
             allowing(delegateStore).record(with(any(RadioPlayerFTPUploadResult.class)));
         }});
         
         CachingFTPUploadResultStore store = new CachingFTPUploadResultStore(delegateStore);
         
         store.record(new RadioPlayerFTPUploadResult("test1", new DateTime(DateTimeZones.UTC), SUCCESS, service, day));
         
         DateTime later = new DateTime(DateTimeZones.UTC).plus(5000);
         store.record(new RadioPlayerFTPUploadResult("test1", later, SUCCESS, service, day));
         
         Set<RadioPlayerFTPUploadResult> results = store.resultsFor(service, day);
         
         assertThat(results.size(), is(equalTo(1)));
         assertThat(Iterables.getOnlyElement(results).uploadTime(), is(equalTo(later)));
     }
 
     @Test
     public void testEmptyRead() {
         
         context.checking(new Expectations(){{
             one(delegateStore).resultsFor(with(any(RadioPlayerService.class)), with(any(LocalDate.class)));
             will(returnValue(ImmutableSet.of(new RadioPlayerFTPUploadResult("test1", new DateTime(DateTimeZones.UTC), SUCCESS, service, day))));
         }});
         
         CachingFTPUploadResultStore store = new CachingFTPUploadResultStore(delegateStore);
         
         Set<RadioPlayerFTPUploadResult> results = store.resultsFor(service, day);
 
         Set<RadioPlayerFTPUploadResult> results2 = store.resultsFor(service, day);
         
         assertThat(results.equals(results2), is(true));
         assertThat(results.isEmpty(), is(false));
     }
 }
