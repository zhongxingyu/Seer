 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entity;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Query;
 import meetdirector.MeetDBConnection;
 import meetdirector.MeetEntriesImportDialog;
 import org.usa_swimming.xsdif.AthleteEntryType;
 import org.usa_swimming.xsdif.ClubEntryType;
 import org.usa_swimming.xsdif.LscCodeType;
 
 /**
  *
  * @author nhorman
  */
 @Entity
 public class SwimMeetClub extends PersistingObject implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     private String clubFullName;
     private String clubShortName;
     private BigInteger phone;
     private BigInteger fax;
     private BigInteger mobilePhone;
     private String clubCode;
     private LscCodeType lscCode;
     @OneToMany
     private List<SwimMeetAthlete> athletes;
     
     
     public SwimMeetClub(ClubEntryType club) {
         MeetEntriesImportDialog.UpdateLog("Adding club " + club.getClubCode());
         this.clubFullName = new String(club.getClubFullName());
         this.clubShortName = new String(club.getClubShortName());
         this.phone = club.getPhone();
         this.fax = club.getFax();
         this.mobilePhone = club.getMobilePhone();
         this.clubCode = new String(club.getClubCode());
         this.lscCode = club.getLSCCode();
         this.athletes = new ArrayList<SwimMeetAthlete>();
         this.ParseClubAthletes(club);
         MeetEntriesImportDialog.UpdateLog("Done Adding club " + club.getClubCode());
         this.persist();
     }
     
     public SwimMeetClub() {
         this.clubFullName = null;
         this.clubShortName = null;
         this.clubCode = null;
         this.athletes = null;
     }
     
     public void ParseClubAthletes(ClubEntryType club) {
         List <AthleteEntryType> athletes;
         athletes = club.getAthleteEntries().getAthleteEntry();
         Iterator<AthleteEntryType> iterator = athletes.iterator();
         while (iterator.hasNext()) {
                 AthleteEntryType athlete = iterator.next();
                 String usasid = athlete.getAthlete().getUsasID();
                 SwimMeetAthlete check = SwimMeetAthlete.getAthleteByUsasId(athlete.getAthlete().getUsasID());
                if (check == null) {
                     MeetEntriesImportDialog.UpdateLog("Already have swimmer " + usasid);
                 } else
                     this.athletes.add(new SwimMeetAthlete(athlete, true));
         }
     }
     
     
     public Long getId() {
         return id;
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
         if (!(object instanceof SwimMeetClub)) {
             return false;
         }
         SwimMeetClub other = (SwimMeetClub) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entity.SwimMeetClub[ id=" + id + " ]";
     }
     
     public static SwimMeetClub GetClub(String clubcode) {
         String myquery = "SELECT * FROM SwimMeetClub WHERE SwimMeetClub.clubcode = '" + clubcode + "'";
         List<SwimMeetClub> results = PersistingObject.queryClassObjects(myquery, SwimMeetClub.class);
         if (results.isEmpty())
             return null;
         if (results.size() > 1)
             MeetEntriesImportDialog.UpdateLog("ERROR! TWO SWIM CLUBS WITH THE SAME CLUB CODE " + clubcode);
         
         return results.get(0);
     }
 
     /**
      * @return the clubFullName
      */
     public String getClubFullName() {
         return clubFullName;
     }
 
     /**
      * @param clubFullName the clubFullName to set
      */
     public void setClubFullName(String clubFullName) {
         this.clubFullName = clubFullName;
     }
 
     /**
      * @return the clubShortName
      */
     public String getClubShortName() {
         return clubShortName;
     }
 
     /**
      * @param clubShortName the clubShortName to set
      */
     public void setClubShortName(String clubShortName) {
         this.clubShortName = clubShortName;
     }
 
     /**
      * @return the phone
      */
     public BigInteger getPhone() {
         return phone;
     }
 
     /**
      * @param phone the phone to set
      */
     public void setPhone(BigInteger phone) {
         this.phone = phone;
     }
 
     /**
      * @return the fax
      */
     public BigInteger getFax() {
         return fax;
     }
 
     /**
      * @param fax the fax to set
      */
     public void setFax(BigInteger fax) {
         this.fax = fax;
     }
 
     /**
      * @return the mobilePhone
      */
     public BigInteger getMobilePhone() {
         return mobilePhone;
     }
 
     /**
      * @param mobilePhone the mobilePhone to set
      */
     public void setMobilePhone(BigInteger mobilePhone) {
         this.mobilePhone = mobilePhone;
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
      * @return the lscCode
      */
     public LscCodeType getLscCode() {
         return lscCode;
     }
 
     /**
      * @param lscCode the lscCode to set
      */
     public void setLscCode(LscCodeType lscCode) {
         this.lscCode = lscCode;
     }
 
     /**
      * @return the athletes
      */
     public List<SwimMeetAthlete> getAthletes() {
         return athletes;
     }
 
     /**
      * @param athletes the athletes to set
      */
     public void setAthletes(List<SwimMeetAthlete> athletes) {
         this.athletes = athletes;
     }
 }
