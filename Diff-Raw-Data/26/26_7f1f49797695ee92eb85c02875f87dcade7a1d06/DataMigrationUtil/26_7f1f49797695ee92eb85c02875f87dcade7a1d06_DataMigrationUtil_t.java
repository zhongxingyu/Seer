 package edu.wustl.catissuecore.bizlogic.test;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import edu.wustl.catissuecore.bizlogic.test.UniqueKeyGeneratorUtil;
 import edu.wustl.catissuecore.domain.AbstractSpecimen;
 import edu.wustl.catissuecore.domain.CellSpecimen;
 import edu.wustl.catissuecore.domain.CollectionEventParameters;
 import edu.wustl.catissuecore.domain.CollectionProtocol;
 import edu.wustl.catissuecore.domain.CollectionProtocolEvent;
 import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
 import edu.wustl.catissuecore.domain.ConsentTier;
 import edu.wustl.catissuecore.domain.ConsentTierResponse;
 import edu.wustl.catissuecore.domain.FluidSpecimen;
 import edu.wustl.catissuecore.domain.MolecularSpecimen;
 import edu.wustl.catissuecore.domain.Participant;
 import edu.wustl.catissuecore.domain.ParticipantMedicalIdentifier;
 import edu.wustl.catissuecore.domain.Race;
 import edu.wustl.catissuecore.domain.ReceivedEventParameters;
 import edu.wustl.catissuecore.domain.Site;
 import edu.wustl.catissuecore.domain.Specimen;
 import edu.wustl.catissuecore.domain.SpecimenCharacteristics;
 import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
 import edu.wustl.catissuecore.domain.SpecimenEventParameters;
 import edu.wustl.catissuecore.domain.SpecimenObjectFactory;
 import edu.wustl.catissuecore.domain.SpecimenPosition;
 import edu.wustl.catissuecore.domain.SpecimenRequirement;
 import edu.wustl.catissuecore.domain.StorageContainer;
 import edu.wustl.catissuecore.domain.TissueSpecimen;
 import edu.wustl.catissuecore.domain.User;
 import edu.wustl.catissuecore.namegenerator.LabelGenerator;
 import edu.wustl.catissuecore.namegenerator.LabelGeneratorFactory;
 import edu.wustl.catissuecore.util.EventsUtil;
 import edu.wustl.common.exception.AssignDataException;
 import edu.wustl.common.util.Utility;
 import gov.nih.nci.common.util.HQLCriteria;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;
 import org.apache.poi.hssf.usermodel.HSSFDateUtil;
 
 /**
  * This class contains the functionality of migrating the domain data
  * from an excel sheet into the caTissue database.
  * @author vishvesh_mulay
  *
  */
 public class DataMigrationUtil extends CaTissueBaseTestCase {
 
//  private static int rowNo = 1; // Row number in the excel sheet.
//  static ApplicationService appService = null; // Application Service instance
 
   public void writeToCaTissue(String excel[][], int rowNo) throws Exception {
       
       System.out.println("---------START DataMigrationUtil.writeToCaTissue()---------");
 
       while (rowNo < excel.length -1) {
 
          System.out.println("----------START Processing for row number "+ rowNo + "---------------");
           
          Participant participant = null;  
          participant = createAndRegisterParticipantToCollectionProtocol(excel);
 
          System.out.println("----------END Processing for row number "+ rowNo + "---------------");
 
          rowNo++;
       } 
 
       System.out.println("---------END DataMigrationUtil.writeToCaTissue()---------");
   }
 
   public Participant createAndRegisterParticipantToCollectionProtocol(String excel[][]) throws Exception {
 
      System.out.println("---------START DataMigrationUtil.createAndRegisterParticipantToCollectionProtocol()---------");
      Participant participant = initParticipant(excel);
      CollectionProtocolRegistration collectionProtocolRegistration = initCollectionProtocolRegistration(participant);
 
      Collection collectionProtocolRegistrationCollection = new HashSet();
      collectionProtocolRegistrationCollection.add(collectionProtocolRegistration);
      participant.setCollectionProtocolRegistrationCollection(collectionProtocolRegistrationCollection);
 
      participant = (Participant) appService.createObject(participant);
      System.out.println("Participant Added successfully::" + participant.getId()+","+participant.getFirstName()+","+participant.getLastName());
      System.out.println("---------END DataMigrationUtil.createAndRegisterParticipantToCollectionProtocol()---------");
      return participant;
   }
 
   public static Site getSite() {
 
        Site returnedSite = null;
 
         Site site = new Site();
         site.setName(new String ("UABH"));
         try {
              List resultList = appService.search(Site.class,site);
              for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();) {
                 returnedSite = (Site) resultsIterator.next();
                 System.out.println(" Domain Object is successfully Found ---->  :: " + returnedSite.getId() +" "+returnedSite.getType());
              }
         } catch (Exception e) {
              System.out.println("TestParticipant.testSearchSite()"+e.getMessage());
              e.printStackTrace();
              assertFalse("Does not find Domain Object", true);
         }
         return returnedSite;
     }
 
    public static CollectionProtocol getCollectionProtocol() {
 
       CollectionProtocol returnedCollectionProtocol = null;
 
         CollectionProtocol cp = new CollectionProtocol();
         cp.setId(new Long ("63"));
         try {
              List resultList = appService.search(CollectionProtocol.class,cp);
              for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();) {
                 returnedCollectionProtocol = (CollectionProtocol) resultsIterator.next();
                 System.out.println(" Domain Object is successfully Found ---->  :: " + returnedCollectionProtocol.getTitle()+"," +returnedCollectionProtocol.getId()+"," +returnedCollectionProtocol.getShortTitle());
              }
         } catch (Exception e) {
              System.out.println("TestParticipant.getCollectionProtocol()"+e.getMessage());
              e.printStackTrace();
              assertFalse("Did not find Domain Object", true);
         }
         return returnedCollectionProtocol;
     }
 
     /*
      * This method initialize participant object  as given in the excel sheet
      * @param excel String double array
      */
 
     public Participant initParticipant(String excel[][]) {
         String     lastName = "";
         String     firstName = "";
         String     middleName = "";
         String     dob = "";
         String     genderFromAccess = "";
         String     medRecNo = "";
         String     raceFromAccess = "";
         String     gender = "";
         String     raceName = "";
         Date       date;
 
         System.out.println("---------START DataMigrationUtil.initParticipant()---------");
             lastName = excel[rowNo][0];
             firstName = excel[rowNo][1];
             middleName = excel[rowNo][2];
             dob = excel[rowNo][3];
             genderFromAccess = excel[rowNo][4];
             medRecNo = excel[rowNo][5];
             raceFromAccess = excel[rowNo][6];
             System.out.println("lastname=" +lastName+ "firstname=" +firstName+ "middlename=" +middleName+ "dob=" +dob+ "gender=" +genderFromAccess+ "medrecno="+medRecNo+ "race=" +raceFromAccess);
             Participant participant = new Participant();
             participant.setLastName(lastName);
             participant.setFirstName(firstName);
             participant.setMiddleName(middleName);
 
             try {
                date = convertDateFromExcel(dob);
                participant.setBirthDate(date);
            } catch (ParseException pe) {
               System.out.println("ERROR: could not parse date in String: " +dob);
             }

             gender = getGenderFromCaTissue(genderFromAccess);
             participant.setGender(gender);
 
             Collection participantMedicalIdentifierCollection = new HashSet();
             ParticipantMedicalIdentifier pmi = new ParticipantMedicalIdentifier();
 
             Site site = (Site) getSite();
             pmi.setSite(site);
             pmi.setMedicalRecordNumber(medRecNo);
             pmi.setParticipant(participant);
             participantMedicalIdentifierCollection.add(pmi);
             participant.setParticipantMedicalIdentifierCollection(participantMedicalIdentifierCollection);
 
             Collection<Race> raceCollection = new HashSet<Race>();
             Race race = new Race();
 
             raceName = getRaceFromCaTissue(raceFromAccess);
             race.setRaceName(raceName);
             race.setParticipant(participant);
             raceCollection.add(race);
             participant.setRaceCollection(raceCollection);
 
             if (raceFromAccess.equals("Hispanic"))
                participant.setEthnicity("Hispanic or Latino");
             else
                participant.setEthnicity("Unknown");
 
             participant.setActivityStatus("Active");
             System.out.println("Participant initiated successfully -->Name:"+participant.getFirstName()+" "+participant.getLastName());
             return participant;
     } //end initParticipant()
 
   public CollectionProtocolRegistration initCollectionProtocolRegistration(Participant participant) {
 
      CollectionProtocolRegistration collectionProtocolRegistration = new CollectionProtocolRegistration();
 
      CollectionProtocol cp = getCollectionProtocol();
 
      collectionProtocolRegistration.setCollectionProtocol(cp);
      collectionProtocolRegistration.setParticipant(participant);
      collectionProtocolRegistration.setActivityStatus("Active");
      try{
         //collectionProtocolRegistration.setRegistrationDate(Utility.parseDate(colldate.replace('/', '-'), "M-d-yyyy"));
         /*collDate is commented while inegrating with nightly bcz it should be in the format MM-dd-yyyy  **/
         //Date timestamp = EventsUtil.setTimeStamp(colldate,"15","45");
         Date timestamp = EventsUtil.setTimeStamp("08-15-1975","15","45");
         collectionProtocolRegistration.setRegistrationDate(timestamp);
      } catch (Exception e) {
         System.out.println("Exception in initCollectionProtocolRegistration" );
         System.err.println("Exception in initCollectionProtocolRegistration" );
         e.printStackTrace();
      }
     return collectionProtocolRegistration;
   }
   
    public String getGenderFromCaTissue (String g) {
 
       String gender = "";
 
       if (g.equals("M"))
         gender = "Male Gender";
       else if (g.equals("F"))
         gender = "Female Gender";
       else if (g.equals("UNKNOWN"))
         gender = "Unknown";
       else
         gender = "Unspecified";
 
       return gender;
 
    }
 
    public String getRaceFromCaTissue (String r) {
 
      String race = "";
 
      if (r.equals("Caucasian"))
        race = "White";
      else if (r.equals("Black"))
        race = "Black or African American";
      else if (r.equals("Oriental"))
        race = "Asian";
      else if (r.equals("Hispanic") || r.equals("U"))
        race = "Unknown";
 
      return race;
   }
 
   public Date convertDateFromExcel (String d) throws ParseException {
 
      // Courtesy: http://devpinoy.org/blogs/lamia/archive/2008/03/25/parsing-excel-date-with-poi-jxls.aspx
 
      Date actualDate=HSSFDateUtil.getJavaDate(Double.parseDouble(d), true);
 
      return actualDate;
 
   }
 
 } //end DataMigrationUtil()
