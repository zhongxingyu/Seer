 package org.atlasapi.remotesite.bbc.schedule;
 
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.xml.bind.JAXBException;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.DefinitiveContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
 import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule.Programme;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 /**
  * Updater to download advance BBC schedules and get URIplay to load data for
  * the programmes that they include
  * 
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class BbcScheduledProgrammeUpdater implements Runnable {
 
     private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";
 
     private final RemoteSiteClient<ChannelSchedule> scheduleClient;
     private final BbcProgrammeAdapter fetcher;
 
     private final Iterable<String> uris;
 
     private final AdapterLog log;
 
     private final DefinitiveContentWriter writer;
 
     private final ContentResolver localFetcher;
 
     public BbcScheduledProgrammeUpdater(ContentResolver localFetcher, BbcProgrammeAdapter remoteFetcher, DefinitiveContentWriter writer, Iterable<String> uris, AdapterLog log) throws JAXBException {
         this(new BbcScheduleClient(), localFetcher, remoteFetcher, writer, uris, log);
     }
 
     BbcScheduledProgrammeUpdater(RemoteSiteClient<ChannelSchedule> scheduleClient, ContentResolver localFetcher, BbcProgrammeAdapter remoteFetcher, DefinitiveContentWriter writer,
             Iterable<String> uris, AdapterLog log) {
         this.scheduleClient = scheduleClient;
         this.localFetcher = localFetcher;
         this.fetcher = remoteFetcher;
         this.writer = writer;
         this.uris = uris;
         this.log = log;
     }
 
     private void update(String uri) {
         try {
             ChannelSchedule schedule = scheduleClient.get(uri);
             List<Programme> programmes = schedule.programmes();
             for (Programme programme : programmes) {
                 try {
                     if (programme.isEpisode()) {
                         Item fetchedItem = (Item) fetcher.fetch(SLASH_PROGRAMMES_BASE_URI + programme.pid());
                         if (fetchedItem != null) {
                             writeFetchedItem(fetchedItem);
                         }
                     }
                 } catch (Exception e) {
                     log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception updating programme with pid " + programme.pid()));
                 }
             }
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception updating BBC URI " + uri));
         }
     }
 
     private void writeFetchedItem(Item fetchedItem) {
         if (fetchedItem instanceof Episode) {
             Episode fetchedEpisode = (Episode) fetchedItem;
            Brand fetchedBrand = fetchBrandFor(fetchedEpisode.getBrand().getCanonicalUri());
            if (fetchedBrand != null) {
                addOrReplaceEpisode(fetchedEpisode, fetchedBrand);
                writer.createOrUpdateDefinitivePlaylist(fetchedBrand);
             }
         } else {
             writer.createOrUpdateDefinitiveItem(fetchedItem);
         }
     }
 
     private void addOrReplaceEpisode(Episode fetchedEpisode, Brand fetchedBrand) {
         if (!fetchedBrand.getItems().contains(fetchedEpisode)) {
             fetchedBrand.addItem(fetchedEpisode);
         } else { // replace
             List<Item> currentItems = Lists.newArrayList(fetchedBrand.getItems());
             currentItems.set(currentItems.indexOf(fetchedEpisode), fetchedEpisode);
             fetchedBrand.setItems(currentItems);
         }
     }
 
     private Brand fetchBrandFor(String brandUri) {
         Brand fetchedBrand = (Brand) localFetcher.findByUri(brandUri);
         if (fetchedBrand == null) {
             fetchedBrand = (Brand) fetcher.fetch(brandUri, false);
         }
         return fetchedBrand;
     }
 
     @Override
     public void run() {
         log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Schedule Updater started"));
 
         final CountDownLatch done = new CountDownLatch(Iterables.size(uris));
 
         ExecutorService executor = Executors.newFixedThreadPool(5);
         for (final String uri : uris) {
             executor.submit(new Runnable() {
                 @Override
                 public void run() {
                     log.record(new AdapterLogEntry(Severity.DEBUG).withSource(getClass()).withDescription("Updating from schedule: " + uri));
                     update(uri);
                     done.countDown();
                 }
             });
         }
         executor.shutdown();
 
         try {
             done.await();
             log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Schedule Updater finished"));
         } catch (InterruptedException e) {
             log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withCause(e).withDescription("BBC Schedule Updater interrupted waiting to finish"));
         }
     }
 }
