 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import org.usa_swimming.xsdif.AthleteEntryType;
 import org.usa_swimming.xsdif.AthleteType;
 import org.usa_swimming.xsdif.Gender;
 import org.usa_swimming.xsdif.LscCodeType;
 import org.usa_swimming.xsdif.OrganizationType;
 
 /**
  *
  * @author nhorman
  */
 @Entity
 public class SwimMeetAthlete extends PersistingObject implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @OneToOne(cascade=CascadeType.ALL)
     @JoinColumn
     private AthleteName name;
     private GregorianCalendar birthDate;
     private Gender gender;
     private boolean isAttached;
     private LscCodeType lsc;
     private String clubCode;
     private String[] citizenOf;
     private OrganizationType organization;
     private String usasID;
     @OneToMany
     @JoinColumn
     private List<SwimMeetEvent> enteredEvents;
     
     @OneToMany
     @JoinColumn
     private List<SeedTime> seedtimes;
     //Note Still need a seed time class list here
     
     public SwimMeetAthlete(AthleteEntryType swimmer, Boolean persist) {
         if (swimmer != null) {
             AthleteType athlete = swimmer.getAthlete();
             //MeetEntriesImportDialog.UpdateLog("Adding Swimmer " + athlete.getUsasID());
             this.name = new AthleteName(athlete.getName(), true);
             this.birthDate = athlete.getBirthDate().toGregorianCalendar();
             this.gender = athlete.getGender();
             this.isAttached = athlete.isIsAttached();
             this.lsc = athlete.getLSC();
             this.clubCode = new String(athlete.getClubCode());
             this.citizenOf = new String[athlete.getCitizenOf().size()];
             this.citizenOf = athlete.getCitizenOf().toArray(this.citizenOf);
             this.organization = athlete.getOrganization();
             this.usasID = athlete.getUsasID();
             this.enteredEvents = null;
             this.seedtimes = null;
             
         } else {
             this.name = new AthleteName();
             if (persist == true)
                 this.name.persist();
             this.birthDate = new GregorianCalendar();
             this.gender = Gender.FEMALE;
             this.isAttached = true;
             this.lsc = LscCodeType.AD;
             this.clubCode = "";
             this.citizenOf = new String[1];
             this.citizenOf[0] = "USA";
             this.organization = OrganizationType.USAS;
             this.usasID = "";
             this.enteredEvents = new ArrayList<SwimMeetEvent>();
             this.seedtimes = new ArrayList<SeedTime>();
         }
         if (persist == true)
             this.persist();
     }
     
     
     public SwimMeetAthlete() {
         this(null, false);
         this.id = null;
     }
     
     public SeedTime getSeedTime(SwimMeetEvent event) {
         Iterator<SeedTime> iterator = this.seedtimes.iterator();
         
         while (iterator.hasNext()) {
             SeedTime seed = iterator.next();
             if (seed.getEvent() == event)
                 return seed;
         }
        SeedTime newSeed = new SeedTime(this, event, true);
        this.startUpdate();
        List<SeedTime> seeds = this.getSeedtimes();
        seeds.add(newSeed);
        this.setSeedtimes(seeds);
        this.commitUpdate();
        return newSeed;
     }
     
     public static SwimMeetAthlete getAthleteByUsasId(String usasid){
         List<SwimMeetAthlete> results;
         String myquery = "SELECT * FROM SwimMeetAthlete WHERE SwimMeetAthlete.usasid = '" + usasid +"'";
         results = SwimMeetAthlete.queryClassObjects(myquery, SwimMeetAthlete.class);
         if (results.isEmpty())
             return null;
         if (results.size() > 1) {
             return null;
         }
         return results.get(0);
     }
     
     public static List<SwimMeetAthlete> getAllAthletes() {
         String myquery = "SELECT * FROM SwimMeetAthlete";
                 
         return SwimMeetAthlete.queryClassObjects(myquery, SwimMeetAthlete.class);   
     }
     
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof SwimMeetAthlete)) {
             return false;
         }
         SwimMeetAthlete other = (SwimMeetAthlete) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entity.SwimMeetAthlete[ usasID=" + getUsasID() + " ]";
         
     }
 
     @Override
     public void remove() {
         AthleteName name = this.getName();
         List<SwimMeetEvent> events = this.getEnteredEvents();
         List<SeedTime> seeds = this.getSeedtimes();
         
         this.startUpdate();
         //this.setName(null);
         this.setEnteredEvents(new ArrayList<SwimMeetEvent>());
         this.setSeedtimes(new ArrayList<SeedTime>());
         this.commitUpdate();
         
         //name.remove();
         Iterator<SwimMeetEvent> iterator1 = events.iterator();
         while (iterator1.hasNext()) {
             SwimMeetEvent event = iterator1.next();
             try {
                 event.removeSwimmer(this);
             } catch (Exception e) {
                     //pass
             }
         }
         Iterator<SeedTime> iterator2 = seeds.iterator();
         while (iterator2.hasNext()) {
             SeedTime seed = iterator2.next();
             seed.remove();
         }
         
         super.remove();
     }
     
     /**
      * @return the name
      */
     public AthleteName getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(AthleteName name) {
         this.name = name;
     }
 
 
 
     /**
      * @return the clubCode
      */
     public String getClubCode() {
         return clubCode;
     }
 
     /**
      * @param clubCode the clubCode to set
      */
     public void setClubCode(String clubCode) {
         this.clubCode = clubCode;
     }
 
 
     /**
      * @return the usasID
      */
     public String getUsasID() {
         return usasID;
     }
 
     /**
      * @param usasID the usasID to set
      */
     public void setUsasID(String usasID) {
         this.usasID = usasID;
     }
 
     /**
      * @return the enteredEvents
      */
     public List<SwimMeetEvent> getEnteredEvents() {
         return enteredEvents;
     }
 
     /**
      * @param enteredEvents the enteredEvents to set
      */
     public void setEnteredEvents(List<SwimMeetEvent> enteredEvents) {
         this.enteredEvents = enteredEvents;
     }
 
     /**
      * @return the birthDate
      */
     public GregorianCalendar getBirthDate() {
         return birthDate;
     }
 
     /**
      * @param birthDate the birthDate to set
      */
     public void setBirthDate(GregorianCalendar birthDate) {
         this.birthDate = birthDate;
     }
 
     /**
      * @return the gender
      */
     public Gender getGender() {
         return gender;
     }
 
     /**
      * @param gender the gender to set
      */
     public void setGender(Gender gender) {
         this.gender = gender;
     }
 
     /**
      * @return the isAttached
      */
     public boolean isIsAttached() {
         return isAttached;
     }
 
     /**
      * @param isAttached the isAttached to set
      */
     public void setIsAttached(boolean isAttached) {
         this.isAttached = isAttached;
     }
 
     /**
      * @return the lsc
      */
     public LscCodeType getLsc() {
         return lsc;
     }
 
     /**
      * @param lsc the lsc to set
      */
     public void setLsc(LscCodeType lsc) {
         this.lsc = lsc;
     }
 
     /**
      * @return the citizenOf
      */
     public String[] getCitizenOf() {
         return citizenOf;
     }
 
     /**
      * @param citizenOf the citizenOf to set
      */
     public void setCitizenOf(String[] citizenOf) {
         this.citizenOf = citizenOf;
     }
 
     /**
      * @return the organization
      */
     public OrganizationType getOrganization() {
         return organization;
     }
 
     /**
      * @param organization the organization to set
      */
     public void setOrganization(OrganizationType organization) {
         this.organization = organization;
     }
 
     /**
      * @return the seedtimes
      */
     public List<SeedTime> getSeedtimes() {
         return seedtimes;
     }
 
     /**
      * @param seedtimes the seedtimes to set
      */
     public void setSeedtimes(List<SeedTime> seedtimes) {
         this.seedtimes = seedtimes;
     }
     
 }
 
