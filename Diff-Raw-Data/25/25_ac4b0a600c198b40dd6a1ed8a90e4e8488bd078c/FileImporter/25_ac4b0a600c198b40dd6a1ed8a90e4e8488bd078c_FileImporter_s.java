 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package meetdirector;
 
 import entity.SeedTime;
 import entity.SwimMeet;
 import entity.SwimMeetAthlete;
 import entity.SwimMeetClub;
 import entity.SwimMeetEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import javassist.tools.rmi.ObjectNotFoundException;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import org.usa_swimming.xsdif.AthleteEntryType;
 import org.usa_swimming.xsdif.ClubEntryType;
 import org.usa_swimming.xsdif.IndividualEntryType;
 import org.usa_swimming.xsdif.MeetEntryFileType;
 
 /**
  *
  * @author nhorman
  */
 public class FileImporter {
     
     private File importFile;
     private javax.swing.JTextArea outputText;
     private HashMap options;
     
     //Values for the options
     public static final String IMPORT_NOT_FOUND_EVENTS = "ImportNotFoundEvents";
     
     public FileImporter(File file) {
         importFile = file;
         this.outputText = null;
         this.options = new HashMap();
         this.options.put(IMPORT_NOT_FOUND_EVENTS, false);
     }
     
     public FileImporter (File file, javax.swing.JTextArea output) {
         this(file);
         this.outputText = output;
     }
     
     protected <T> T getTopLevelDocument(Class DocClassType) throws ObjectNotFoundException {
         
         T objectInstance = null;
         
         try {
             JAXBContext context = JAXBContext.newInstance(DocClassType);
             Unmarshaller u = context.createUnmarshaller();
             InputStreamReader in = new InputStreamReader(new FileInputStream(this.importFile));
             XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(in);
             JAXBElement <T> ObjectElement = u.unmarshal(xer, DocClassType);
             objectInstance = ObjectElement.getValue();
             
         } catch (Exception e) {
             this.updateOutputText("Unable to find meet entry information in file");
             throw new ObjectNotFoundException("Could not find Document type " + DocClassType.getName());
         }
         
         return objectInstance;
     }
     
     public void ImportMeetEntryFile() {
         MeetEntryFileType meetEntry;
         List<ClubEntryType> clubs;
         
         try {
             meetEntry = this.getTopLevelDocument(MeetEntryFileType.class);
         } catch (ObjectNotFoundException e) {
             this.updateOutputText("Aborting File Import");
             return;
         }
         this.updateOutputText("Found Meet Entry " + meetEntry.getMeet().getMeetName());
         try {
             this.updateOutputText("Attempting import of meet overview data");
             SwimMeet newMeet = new SwimMeet(meetEntry.getMeet());
             this.updateOutputText("Imported new meet overview data");
             newMeet.persist();
         } catch (Exception e) {
             this.updateOutputText("Error: " + e.getLocalizedMessage());
             this.updateOutputText("Meet overview already exists, skipping");
         }
         
         this.updateOutputText("Importing Clubs");
         this.ImportClubs(meetEntry.getClubs().getClub());
         
     }
     
     public void ImportClubs(List<ClubEntryType> clubs) {
         ClubEntryType club;
         Iterator<ClubEntryType> iterator = clubs.iterator();
         while (iterator.hasNext()) {
             club = iterator.next();
             SwimMeetClub DbClub = SwimMeetClub.GetClub(club.getClubCode());
             if (DbClub == null) {
                 DbClub = new SwimMeetClub(club);
             } else {
                 this.updateOutputText("Found Existing swim club " + DbClub.getClubCode() + " ...updating athletes");
             }
             // for each club update the athletes in that club
             this.ImportAthletes(DbClub, club.getAthleteEntries().getAthleteEntry());
         }
     }
     
     public void ImportAthletes(SwimMeetClub DbClub, List <AthleteEntryType> athletes) {
         Iterator<AthleteEntryType> iterator = athletes.iterator();
         List<SwimMeetAthlete> newAthletes = new ArrayList<SwimMeetAthlete>();
         
         while (iterator.hasNext()) {
                 AthleteEntryType athlete = iterator.next();
                 String usasid = athlete.getAthlete().getUsasID();
                 SwimMeetAthlete check = SwimMeetAthlete.getAthleteByUsasId(athlete.getAthlete().getUsasID());
                 if (check != null) {
                     this.updateOutputText("Already have swimmer " + usasid);
                     newAthletes.add(check);
                 } else {
                     this.updateOutputText("Adding new swimmer " + usasid);
                     check = new SwimMeetAthlete(athlete, true);
                     newAthletes.add(check);
                 }
                 this.ImportAthleteEvents(check, athlete.getEntries().getEventEntry());
         }
         // Now that we have a list of Athletes, lets update the club with the new list
         DbClub.startUpdate();
         DbClub.setAthletes(newAthletes);
         DbClub.commitUpdate();
         
     }
     
     public void ImportAthleteEvents(SwimMeetAthlete athlete, List<IndividualEntryType> entries) {
         Iterator<IndividualEntryType> iterator = entries.iterator();
         List<SwimMeetEvent> newEvents = new ArrayList<SwimMeetEvent>();
         SwimMeetEvent check;
         
         this.updateOutputText("Importing Events for Swimmer " + athlete.getName().getFullName());
         while (iterator.hasNext()) {
             IndividualEntryType event = iterator.next();
             check = SwimMeetEvent.getEventByEventNumber(event.getEventNumber());
             if (check != null) {
                 this.updateOutputText("Found Event " + event.getEventNumber() + "...Validating");
                 // Note, need to validate the events here, make sure more than the event number matches
                 newEvents.add(check);
                 //add the new athlete here
                 try {
                     check.addSwimmer(athlete, true);
                 } catch (Exception e) {
                     this.updateOutputText("Cant add swimmer " + athlete.getName().getFullName() + " : " + e.getLocalizedMessage());
                 }
                 
             } else {
                 // Events have to be created in the event creation window, they won't auto create on import
                 // unless the user has selected the import events button
                 if (this.getOptions().get(IMPORT_NOT_FOUND_EVENTS) == true) {
                     this.updateOutputText("Importing new event number " + event.getEventNumber() + " From File");
                     check = new SwimMeetEvent(event, true);
                     try {
                         check.addSwimmer(athlete, true);
                     } catch (Exception e) {
                         this.updateOutputText("Error adding swimmer " + athlete.getName().getFullName() + " to event " + event.getEventNumber() + ": " + e.getMessage());
                     }
                     newEvents.add(check);
                 } else {
                     this.updateOutputText("Hmm, I don't have Event " + event.getEventNumber() + " ... Skipping");
                 }
             }
             
             // Now lets import the seed information
             // Note if there is no seed time for this entry, one will be created as blank
            SeedTime seed = athlete.getSeedTime(check);
         }
         
         // Now record teh new event list for this swimmer
         athlete.startUpdate();
         athlete.setEnteredEvents(newEvents);
         athlete.commitUpdate();
         
        
     }
     
     public void updateOutputText(String msg) {
         if (this.outputText != null)
             this.outputText.append(msg + "\n");
         else
             System.out.println(msg + "\n");
     }
 
     /**
      * @return the options
      */
     public HashMap getOptions() {
         return options;
     }
 
     /**
      * @param options the options to set
      */
     public void setOptions(HashMap options) {
         this.options = options;
     }
     
 }
