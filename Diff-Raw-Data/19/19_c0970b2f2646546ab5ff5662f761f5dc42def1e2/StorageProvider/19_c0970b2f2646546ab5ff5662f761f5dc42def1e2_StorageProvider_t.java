 package net.ostis.confman.model.datastore;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 
 import net.ostis.confman.model.common.concurrency.ConcurrencyThreadExecutor;
 import net.ostis.confman.model.datastore.local.ConferenceReader;
 import net.ostis.confman.model.datastore.local.ParticipantReader;
 import net.ostis.confman.model.datastore.local.PersonReader;
 import net.ostis.confman.model.datastore.local.ReportReader;
 import net.ostis.confman.model.datastore.local.SectionReader;
 import net.ostis.confman.model.entity.Conference;
 import net.ostis.confman.model.entity.Conferences;
 import net.ostis.confman.model.entity.Participant;
 import net.ostis.confman.model.entity.Participants;
 import net.ostis.confman.model.entity.Person;
 import net.ostis.confman.model.entity.Persons;
 import net.ostis.confman.model.entity.Report;
 import net.ostis.confman.model.entity.Reports;
 import net.ostis.confman.model.entity.Section;
 import net.ostis.confman.model.entity.Sections;
 
 import org.apache.log4j.Logger;
 
 public class StorageProvider {
 
    public static final Logger     LOGGER       = Logger.getLogger(StorageProvider.class);
 
     private static StorageProvider INSTANCE;
 
    private List<Person>           persons      = new ArrayList<>();
 
    private List<Participant>      participants = new ArrayList<>();
 
    private List<Report>           reports      = new ArrayList<>();
 
    private List<Section>          sections     = new ArrayList<>();
 
    private List<Conference>       conferences  = new ArrayList<>();
 
     private StorageProvider() {
 
         super();
     }
 
     public static StorageProvider getInstance() {
 
         if (INSTANCE == null) {
             INSTANCE = new StorageProvider();
             INSTANCE.loadData();
         }
         return INSTANCE;
     }
 
     private void loadData() {
 
         this.persons = loadPersons();
         this.participants = loadParticipants();
         this.reports = loadReports();
         this.sections = loadSections();
         this.conferences = loadConferences();
     }
 
     private List<Person> loadPersons() {
 
         final PersonReader reader = new PersonReader();
         final Persons loadedData = (Persons) load(reader);
         return loadedData.getPersons();
     }
 
     private List<Participant> loadParticipants() {
 
         final ParticipantReader reader = new ParticipantReader();
         final Participants loadedData = (Participants) load(reader);
         return loadedData.getParticipants();
     }
 
     private List<Report> loadReports() {
 
         final ReportReader reader = new ReportReader();
         final Reports loadedData = (Reports) load(reader);
         return loadedData.getReports();
     }
 
     private List<Section> loadSections() {
 
         final SectionReader reader = new SectionReader();
         final Sections loadedData = (Sections) load(reader);
         return loadedData.getSections();
     }
 
     private List<Conference> loadConferences() {
 
         final ConferenceReader reader = new ConferenceReader();
         final Conferences loadedData = (Conferences) load(reader);
         return loadedData.getConferences();
     }
 
     private Object load(final Callable<?> reader) {
 
         try {
             return ConcurrencyThreadExecutor.getInstance().callTask(reader);
         } catch (InterruptedException | ExecutionException e) {
             throw new RuntimeException(e);
         }
     }
 
     public List<Person> getPersons() {
 
         return this.persons;
     }
 
     public List<Participant> getParticipants() {
 
         return this.participants;
     }
 
     public List<Report> getReports() {
 
         return this.reports;
     }
 
     public List<Section> getSections() {
 
         return this.sections;
     }
 
     public List<Conference> getConferences() {
 
         return this.conferences;
     }
 }
