 package org.atlasapi.remotesite.pa.people;
 
 import java.io.File;
 import java.util.NoSuchElementException;
 import java.util.concurrent.locks.Lock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.Unmarshaller.Listener;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.atlasapi.feeds.upload.FileUploadResult;
 import org.atlasapi.persistence.content.PeopleResolver;
 import org.atlasapi.persistence.content.people.PersonWriter;
 import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
 import org.atlasapi.remotesite.pa.profiles.bindings.Person;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.XMLReader;
 
 import com.google.common.base.Predicates;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 
 public class PaCompletePeopleUpdater extends ScheduledTask {
 
     private static final String SERVICE = "PA";
     private static final Pattern FULL_FILE = Pattern.compile("^.*(\\d{8})_profiles_full_dump.xml$");
     private static final Pattern DAILY_DATE = Pattern.compile("^.*(\\d{8})_profiles.xml$");
     
     private final PaProgrammeDataStore store;
     private final PeopleResolver personResolver;
     private final PersonWriter personWriter;
     private final Logger log = LoggerFactory.getLogger(PaDailyPeopleUpdater.class);
     private final DateTimeFormatter dateFormatter = ISODateTimeFormat.basicDate();
     private final Lock lock;
     
     // orders by the date (if the file matches the pattern) or by filename
     private final Ordering<File> BY_DATE_ORDER = new Ordering<File>() {
         @Override
         public int compare(File left, File right) {
             Matcher leftMatcher = FULL_FILE.matcher(left.toURI().toString());
             Matcher rightMatcher = FULL_FILE.matcher(right.toURI().toString());
             if (leftMatcher.matches()) {
                 if (rightMatcher.matches()) {
                     DateTime leftTime = dateFormatter.parseDateTime(leftMatcher.group(1));
                     DateTime rightTime = dateFormatter.parseDateTime(rightMatcher.group(1));
                     return leftTime.compareTo(rightTime);
                 } else {
                     return 1;
                 }
             } else {
                 if (rightMatcher.matches()) {
                     return -1;
                 } else {
                     return left.getName().compareTo(right.getName());
                 }
             }
         }
     };
     
     private PaPeopleProcessor processor;
 
     public PaCompletePeopleUpdater(PaProgrammeDataStore store, PeopleResolver personResolver, PersonWriter personWriter, Lock lock) {
         this.store = store;
         this.personResolver = personResolver;
         this.personWriter = personWriter;
         this.lock = lock;
     }
     
     @Override
     protected void runTask() {
         if (lock.tryLock()) {
             try {
                 processor = new PaPeopleProcessor(personResolver, personWriter);
                 processFiles(store.localProfilesFiles(Predicates.<File>alwaysTrue()));
             } finally {
                 lock.unlock();
             }
         } else {
             reportStatus("Another PA People ingest is running, this task has not run");
         }
     }
     
     private void processFiles(Iterable<File> files) {
         try { 
             JAXBContext context = JAXBContext.newInstance("org.atlasapi.remotesite.pa.profiles.bindings");
             Unmarshaller unmarshaller = context.createUnmarshaller();
 
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setNamespaceAware(true);
             XMLReader reader = factory.newSAXParser().getXMLReader();
             reader.setContentHandler(unmarshaller.getUnmarshallerHandler());
 
             File latestFile = BY_DATE_ORDER.max(files);
             Matcher matcher = FULL_FILE.matcher(latestFile.toURI().toString());
            if (matcher.matches()) {
                 log.info("Processing file " + latestFile.toString());
                 final File fileToProcess = store.copyForProcessing(latestFile);
 
                 unmarshaller.setListener(peopleProcessingListener());
                 reader.parse(fileToProcess.toURI().toString());
 
                 FileUploadResult.successfulUpload(SERVICE, latestFile.getName());
                 
                 // mark all daily files before that date as run
                 DateTime bootstrapDate = dateFormatter.parseDateTime(matcher.group(1));
                 for (File file : files) {
                     Matcher fileMatcher = DAILY_DATE.matcher(file.toURI().toString());
                     if (fileMatcher.matches()) {
                         DateTime fileTime = dateFormatter.parseDateTime(fileMatcher.group(1));
                         if (fileTime.isBefore(bootstrapDate)) {
                             FileUploadResult.successfulUpload(SERVICE, file.getName());    
                         }
                     }
                 }
                 
            } else {
                throw new RuntimeException("No full profiles dump file found");
             }
 
             reportStatus(String.format("found %s files, processed most recent file %s", Iterables.size(files), latestFile.getName()));
         } catch (NoSuchElementException e) {
             log.error("No files found when running PA Complete People updater", e);
             // this will stop the task
             Throwables.propagate(e);
         } catch (Exception e) {
             log.error("Exception running PA Complete People updater", e);
             // this will stop the task
             Throwables.propagate(e);
         }
     }
 
     private Listener peopleProcessingListener() {
         return new Unmarshaller.Listener() {
             public void beforeUnmarshal(Object target, Object parent) {
             }
 
             public void afterUnmarshal(Object target, Object parent) {
                 if (target instanceof Person) {
                     processor.process(((Person) target));
                 }
             }
         };
     }
 }
