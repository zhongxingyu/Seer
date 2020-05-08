 package com.github.croesch.types;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import com.github.croesch.i18n.Text;
 import com.github.croesch.types.exceptions.RequiredFieldSetToNullException;
 
 /**
  * Represents a participant.
  * 
  * @author croesch
  * @since Date: May 29, 2011 12:28:42 PM
  */
 public class Participant {
 
   /** the id of that participant as stored in data base */
   private final long id;
 
   /** the highest id to calculate the next id for a new participant */
   private static long highestId = 0;
 
   /** the last name of the participant */
   private String lastName = null;
 
   /** the first name of the participant */
   private String foreName = null;
 
   /** the gender of the participant */
   private Gender gender = null;
 
   /** the denomination/confession of the participant */
   private Denomination denomination = null;
 
   /** the date of birth of the participant */
   private Date birthDate = null;
 
   /** the address where the participant lives */
   private final Address livingAddress = new Address();
 
   /** the address to post to, if different to the address where the participant lives */
   private final Address postToAddress = new Address();
 
   /** the phone number of the participant */
   private String phone = null;
 
   /** the fax number of the participant */
   private String fax = null;
 
   /** the mobile phone number of the participant */
   private String mobilePhone = null;
 
   /** the phone number of the parents of the participant */
   private String phoneOfParents = null;
 
   /** the mail address of the participant */
   private String mailAddress = null;
 
   /** the county council of the participant */
   private CountyCouncil countyCouncil = null;
 
   /** the bank code number of the participant */
   private int bankCodeNumber = 0;
 
   /** the name of the bank of the participant */
   private String bank = null;
 
   /** the bank account number of the participant */
   private int bankAccountNumber = 0;
 
   /** a comment of the staff about the participant, eg about medicine */
   private String comment = null;
 
   /** date since the participant is registered */
   private Date dateSinceInDataBase = null;
 
   /** date until the participant is a member in the system */
   private Date dateUpToInSystem = null;
 
   /** if the participant can be a participant */
   private boolean possibleParticipant = false;
 
   /** if the participant can be staff */
   private boolean possibleStaff = false;
 
   /** if the participant can be staff-youth */
   private boolean possibleStaffYouth = false;
 
   /** if the participant can be member of board */
   private boolean possibleBoard = false;
 
   /** if the participant can be member of extended board */
   private boolean possibleExtendedBoard = false;
 
   /** if the participant can be mak */
   private boolean possibleMAK = false;
 
   /** if the participant can be age */
   private boolean possibleAGE = false;
 
   /** if the participant can be in kitchen */
   private boolean possibleKitchen = false;
 
   /** if the participant can be seminar */
   private boolean possibleSeminar = false;
 
   /** if the participant can be misc. */
   private boolean possibleMisc = false;
 
   /**
    * Constructs a new {@link Participant}.
    * 
    * @author croesch
    * @since Date: Jun 16, 2011 9:16:04 PM
    * @param forcedId the number to identify this participant, must be higher than the highest number until now
    * @param name the last name of the person
    * @param firstName the first name of the person
    * @param g the gender of the person
    * @param den the denomination/confession of the person
    * @param birth the birthday of the person
    * @param str the street where the person lives
    * @param pc the post code of the city where the person lives
    * @param c the city where the person lives
    * @param county the county council for this person
    * @throws IllegalArgumentException if the id is too small
    */
   public Participant(final long forcedId,
                      final String name,
                      final String firstName,
                      final Gender g,
                      final Denomination den,
                      final Date birth,
                      final String str,
                      final int pc,
                      final String c,
                      final CountyCouncil county) throws IllegalArgumentException {
     if (forcedId <= highestId) {
       throw new IllegalArgumentException();
     }
 
     setLastName(name);
     setForeName(firstName);
     setGender(g);
     setDenomination(den);
     setBirthDate(birth);
     setStreet(str);
     setPostCode(pc);
     setCity(c);
     setCountyCouncil(county);
 
     this.id = forcedId;
     highestId = forcedId;
   }
 
   /**
    * Constructs a new {@link Participant}.
    * 
    * @author croesch
    * @since Date: Jun 16, 2011 9:16:04 PM
    * @param name the last name of the person
    * @param firstName the first name of the person
    * @param g the gender of the person
    * @param den the denomination/confession of the person
    * @param birth the birthday of the person
    * @param str the street where the person lives
    * @param pc the post code of the city where the person lives
    * @param c the city where the person lives
    * @param county the county council for this person
    */
   public Participant(final String name,
                      final String firstName,
                      final Gender g,
                      final Denomination den,
                      final Date birth,
                      final String str,
                      final int pc,
                      final String c,
                      final CountyCouncil county) {
     this(highestId + 1, name, firstName, g, den, birth, str, pc, c, county);
   }
 
   /**
    * Constructs a new {@link Participant} that is equal to the given {@link Participant}.
    * 
    * @author croesch
    * @since Date: Jun 16, 2011 9:16:04 PM
    * @param p the {@link Participant} to fetch the data from.
    * @throws RequiredFieldSetToNullException if the given {@link Participant} is <code>null</code>
    */
   public Participant(final Participant p) throws RequiredFieldSetToNullException {
     if (p == null) {
       throw new RequiredFieldSetToNullException();
     }
 
     this.bank = p.bank;
     this.bankAccountNumber = p.bankAccountNumber;
     this.bankCodeNumber = p.bankCodeNumber;
     this.birthDate = new Date(p.birthDate.getTime());
     this.comment = p.comment;
     this.countyCouncil = p.countyCouncil;
     if (p.dateSinceInDataBase != null) {
       this.dateSinceInDataBase = new Date(p.dateSinceInDataBase.getTime());
     }
     if (p.dateUpToInSystem != null) {
       this.dateUpToInSystem = new Date(p.dateUpToInSystem.getTime());
     }
     this.denomination = p.denomination;
     this.fax = p.fax;
     this.foreName = p.foreName;
     this.gender = p.gender;
     this.id = p.id;
     this.lastName = p.lastName;
     this.livingAddress.setCity(p.livingAddress.getCity());
     this.livingAddress.setPostCode(p.livingAddress.getPostCode());
     this.livingAddress.setStreet(p.livingAddress.getStreet());
     this.mailAddress = p.mailAddress;
     this.mobilePhone = p.mobilePhone;
     this.phone = p.phone;
     this.phoneOfParents = p.phoneOfParents;
     this.possibleAGE = p.possibleAGE;
     this.possibleBoard = p.possibleBoard;
     this.possibleExtendedBoard = p.possibleExtendedBoard;
     this.possibleKitchen = p.possibleKitchen;
     this.possibleMAK = p.possibleMAK;
     this.possibleMisc = p.possibleMisc;
     this.possibleParticipant = p.possibleParticipant;
     this.possibleSeminar = p.possibleSeminar;
     this.possibleStaff = p.possibleStaff;
     this.possibleStaffYouth = p.possibleStaffYouth;
     this.postToAddress.setCity(p.postToAddress.getCity());
     this.postToAddress.setPostCode(p.postToAddress.getPostCode());
     this.postToAddress.setStreet(p.postToAddress.getStreet());
   }
 
   /**
    * Gets the last name of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the last name of the participant
    */
   public final String getLastName() {
     return this.lastName;
   }
 
   /**
    * Sets the last name of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param name the last name, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given name is <code>null</code>
    */
   public final void setLastName(final String name) throws RequiredFieldSetToNullException {
     if (name == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.lastName = name;
   }
 
   /**
    * Gets the fore name of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the fore name of the participant
    */
   public final String getForeName() {
     return this.foreName;
   }
 
   /**
    * Sets the first name of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param name the first name, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given name is <code>null</code>
    */
   public final void setForeName(final String name) throws RequiredFieldSetToNullException {
     if (name == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.foreName = name;
   }
 
   /**
    * Gets the gender of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the gender of the participant
    */
   public final Gender getGender() {
     return this.gender;
   }
 
   /**
    * Sets the gender of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param g the gender of the participant, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given gender is <code>null</code>
    */
   public final void setGender(final Gender g) throws RequiredFieldSetToNullException {
     if (g == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.gender = g;
   }
 
   /**
    * Gets the denomination of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the denomination of the participant
    */
   public final Denomination getDenomination() {
     return this.denomination;
   }
 
   /**
    * Sets the denomination of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param den the denomination of the participant
    * @throws RequiredFieldSetToNullException if the given gender is <code>null</code>
    */
   public final void setDenomination(final Denomination den) throws RequiredFieldSetToNullException {
     if (den == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.denomination = den;
   }
 
   /**
    * Gets the date of birth of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the date of birth of the participant
    */
   public final Date getBirthDate() {
     return (Date) this.birthDate.clone();
   }
 
   /**
    * Sets the birth day date of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param birth the date of birth day of the participant, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given birth date is <code>null</code>
    */
   public final void setBirthDate(final Date birth) throws RequiredFieldSetToNullException {
     if (birth == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.birthDate = new Date(birth.getTime());
   }
 
   /**
    * Gets the street of the participant (where he lives).
    * 
    * @since Date: Jun 18, 2011
    * @return the street of the participant (where he lives)
    */
   public final String getStreet() {
     return this.livingAddress.getStreet();
   }
 
   /**
    * Sets the street where the participant lives.
    * 
    * @since Date: Jun 18, 2011
    * @param str the street where the participant lives.
    * @throws RequiredFieldSetToNullException if the given street is <code>null</code>
    */
   public final void setStreet(final String str) throws RequiredFieldSetToNullException {
     if (str == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.livingAddress.setStreet(str);
   }
 
   /**
    * Gets the post code of the participant (where he lives).
    * 
    * @since Date: Jun 18, 2011
    * @return the post code of the participant (where he lives)
    */
   public final int getPostCode() {
     return this.livingAddress.getPostCode();
   }
 
   /**
    * Sets the post code where the participant lives.
    * 
    * @since Date: Jun 18, 2011
    * @param pc the post code where the participant lives.
    * @throws IllegalArgumentException if the given post code is less than zero
    */
   public final void setPostCode(final int pc) throws IllegalArgumentException {
     final int highestPostCode = 99999;
     if (pc < 0 || pc > highestPostCode) {
       throw new IllegalArgumentException();
     }
     this.livingAddress.setPostCode(pc);
   }
 
   /**
    * Gets the city of the participant (where he lives).
    * 
    * @since Date: Jun 18, 2011
    * @return the city of the participant (where he lives)
    */
   public final String getCity() {
     return this.livingAddress.getCity();
   }
 
   /**
    * Sets the city where the participant lives.
    * 
    * @since Date: Jun 18, 2011
    * @param c the city where the participant lives
    * @throws RequiredFieldSetToNullException if the given city is <code>null</code>
    */
   public final void setCity(final String c) throws RequiredFieldSetToNullException {
     if (c == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.livingAddress.setCity(c);
   }
 
   /**
    * Gets the street of the participant (where to post to).
    * 
    * @since Date: Jun 18, 2011
    * @return the street of the participant (where to post to)
    */
   public final String getStreetPostal() {
     return this.postToAddress.getStreet();
   }
 
   /**
    * Sets the street of the participant (where to post to).
    * 
    * @since Date: Jun 18, 2011
    * @param sp the street of the participant (where to post to)
    */
   public final void setStreetPostal(final String sp) {
     this.postToAddress.setStreet(sp);
   }
 
   /**
    * Gets the post code of the participant (where to post to).
    * 
    * @since Date: Jun 18, 2011
    * @return the post code of the participant (where to post to)
    */
   public final int getPostCodePostal() {
     return this.postToAddress.getPostCode();
   }
 
   /**
    * Sets the post code where to post to.
    * 
    * @since Date: Jun 18, 2011
    * @param pc the post code where to post to, if different to post code where the participant lives.
    * @throws IllegalArgumentException if the given post code is less than zero or greater than 99999
    */
   public final void setPostCodePostal(final int pc) throws IllegalArgumentException {
     final int highestPostCode = 99999;
     if (pc < 0 || pc > highestPostCode) {
       throw new IllegalArgumentException();
     }
     this.postToAddress.setPostCode(pc);
   }
 
   /**
    * Gets the city of the participant (where to post to).
    * 
    * @since Date: Jun 18, 2011
    * @return the city of the participant (where to post to)
    */
   public final String getCityPostal() {
     return this.postToAddress.getCity();
   }
 
   /**
    * Sets the city of the participant (where to post to).
    * 
    * @since Date: Jun 18, 2011
    * @param cp the city of the participant (where to post to)
    */
   public final void setCityPostal(final String cp) {
     this.postToAddress.setCity(cp);
   }
 
   /**
    * Gets the phone number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the phone number of the participant
    */
   public final String getPhone() {
     return this.phone;
   }
 
   /**
    * Sets the phone number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param p the phone number of the participant
    */
   public final void setPhone(final String p) {
     this.phone = p;
   }
 
   /**
    * Gets the fax number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the fax number of the participant
    */
   public final String getFax() {
     return this.fax;
   }
 
   /**
    * Sets the fax number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param f the fax number of the participant
    */
   public final void setFax(final String f) {
     this.fax = f;
   }
 
   /**
    * Gets the mobile phone number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the mobile phone number of the participant
    */
   public final String getMobilePhone() {
     return this.mobilePhone;
   }
 
   /**
    * Sets the mobile phone number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param mobile the mobile phone number of the participant
    */
   public final void setMobilePhone(final String mobile) {
     this.mobilePhone = mobile;
   }
 
   /**
    * Gets the phone number of the parents of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the phone number of the parents of the participant
    */
   public final String getPhoneOfParents() {
     return this.phoneOfParents;
   }
 
   /**
    * Sets the phone number of the parents of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param pop the phone number of the parents of the participant
    */
   public final void setPhoneOfParents(final String pop) {
     this.phoneOfParents = pop;
   }
 
   /**
    * Gets the mail address of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the mail address of the participant
    */
   public final String getMailAddress() {
     return this.mailAddress;
   }
 
   /**
    * Sets the mail address of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param mail the mail address of the participant
    */
   public final void setMailAddress(final String mail) {
     this.mailAddress = mail;
   }
 
   /**
    * Gets the county council of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the county council of the participant
    */
   public final CountyCouncil getCountyCouncil() {
     return this.countyCouncil;
   }
 
   /**
    * Sets the county council of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param cc the county council of the participant.
    * @throws RequiredFieldSetToNullException if the given county council is <code>null</code>
    */
   public final void setCountyCouncil(final CountyCouncil cc) throws RequiredFieldSetToNullException {
     if (cc == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.countyCouncil = cc;
   }
 
   /**
    * Gets the bank code number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the bank code number of the participant
    */
   public final int getBankCodeNumber() {
     return this.bankCodeNumber;
   }
 
   /**
    * Sets the bank code number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param bcn the bank code number of the participant
    */
   public final void setBankCodeNumber(final int bcn) {
     this.bankCodeNumber = bcn;
   }
 
   /**
    * Gets the bank of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the bank of the participant
    */
   public final String getBank() {
     return this.bank;
   }
 
   /**
    * Sets the bank of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param b the bank of the participant
    */
   public final void setBank(final String b) {
     this.bank = b;
   }
 
   /**
    * Gets the bank account number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the bank account number of the participant
    */
   public final int getBankAccountNumber() {
     return this.bankAccountNumber;
   }
 
   /**
    * Sets the bank account number of the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param ban the bank account number of the participant
    */
   public final void setBankAccountNumber(final int ban) {
     this.bankAccountNumber = ban;
   }
 
   /**
    * Gets the comment about the participant.
    * 
    * @since Date: Jun 18, 2011
    * @return the comment about the participant
    */
   public final String getComment() {
     return this.comment;
   }
 
   /**
    * Sets the comment about the participant.
    * 
    * @since Date: Jun 18, 2011
    * @param com the comment about the participant
    */
   public final void setComment(final String com) {
     this.comment = com;
   }
 
   /**
    * Gets the date since the participant is in the system.
    * 
    * @since Date: Jun 18, 2011
    * @return the date since the participant is in the system
    */
   public final Date getDateSinceInDataBase() {
     if (this.dateSinceInDataBase == null) {
       return null;
     }
     return (Date) this.dateSinceInDataBase.clone();
   }
 
   /**
    * Sets the date since the user is in the data base.
    * 
    * @since Date: Jun 18, 2011
    * @param dsidb the date since the user is in the database
    */
   public final void setDateSinceInDataBase(final Date dsidb) {
     if (dsidb != null) {
       this.dateSinceInDataBase = new Date(dsidb.getTime());
     } else {
       this.dateSinceInDataBase = null;
     }
   }
 
   /**
    * Gets the date up to that the user will be in the system.
    * 
    * @since Date: Jun 18, 2011
    * @return the date up to that the user will be in the system
    */
   public final Date getDateUpToInSystem() {
     if (this.dateUpToInSystem == null) {
       return null;
     }
     return (Date) this.dateUpToInSystem.clone();
   }
 
   /**
    * Sets the date up to that the user is in the system.
    * 
    * @since Date: Jun 18, 2011
    * @param dutis the date up to that the user is in the system.
    */
   public final void setDateUpToInSystem(final Date dutis) {
     if (dutis != null) {
       this.dateUpToInSystem = new Date(dutis.getTime());
     } else {
       this.dateUpToInSystem = null;
     }
   }
 
   /**
    * Gets whether the participant can participate as a participant.
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the user can participate as a participant
    */
   public final boolean isPossibleParticipant() {
     return this.possibleParticipant;
   }
 
   /**
    * Sets whether the participant can participate as a participant.
    * 
    * @since Date: Jun 18, 2011
    * @param cbp <code>true</code>, if the user can participate as a participant
    */
   public final void setPossibleParticipant(final boolean cbp) {
     this.possibleParticipant = cbp;
   }
 
   /**
    * Gets whether the user can participate as a staff member.
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the user can participate as a staff member
    */
   public final boolean isPossibleStaff() {
     return this.possibleStaff;
   }
 
   /**
    * Sets whether the participant can participate as a staff member.
    * 
    * @since Date: Jun 18, 2011
    * @param cbs <code>true</code>, if the participant can participate as a staff member
    */
   public final void setPossibleStaff(final boolean cbs) {
     this.possibleStaff = cbs;
   }
 
   /**
    * Returns the participant can be staff-youth
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be staff-youth
    */
   public final boolean isPossibleStaffYouth() {
     return this.possibleStaffYouth;
   }
 
   /**
    * Sets whether the participant can be staff-youth
    * 
    * @since Date: Jun 18, 2011
    * @param psy <code>true</code>, if the participant can be staff-youth
    */
   public final void setPossibleStaffYouth(final boolean psy) {
     this.possibleStaffYouth = psy;
   }
 
   /**
    * Returns whether the participant can be member of board
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be member of board
    */
   public final boolean isPossibleBoard() {
     return this.possibleBoard;
   }
 
   /**
    * Sets whether the participant can be member of board.
    * 
    * @since Date: Jun 18, 2011
    * @param pb <code>true</code>, if the participant can be member of board
    */
   public final void setPossibleBoard(final boolean pb) {
     this.possibleBoard = pb;
   }
 
   /**
    * Returns whether the participant can be member of extended board
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be member of extended board
    */
   public final boolean isPossibleExtendedBoard() {
     return this.possibleExtendedBoard;
   }
 
   /**
    * Sets whether the participant can be member of extended board.
    * 
    * @since Date: Jun 18, 2011
    * @param peb <code>true</code>, if the participant can be member of extended board
    */
   public final void setPossibleExtendedBoard(final boolean peb) {
     this.possibleExtendedBoard = peb;
   }
 
   /**
    * Returns whether the participant can be MAK
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be MAK
    */
   public final boolean isPossibleMAK() {
     return this.possibleMAK;
   }
 
   /**
    * Sets whether the participant can be MAK
    * 
    * @since Date: Jun 18, 2011
    * @param pMAK <code>true</code>, if the participant can be MAK
    */
   public final void setPossibleMAK(final boolean pMAK) {
     this.possibleMAK = pMAK;
   }
 
   /**
    * Returns whether the participant can be AGE
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be AGE
    */
   public final boolean isPossibleAGE() {
     return this.possibleAGE;
   }
 
   /**
    * Sets whether the participant can be AGE
    * 
    * @since Date: Jun 18, 2011
    * @param pAGE <code>true</code>, if the participant can be AGE
    */
   public final void setPossibleAGE(final boolean pAGE) {
     this.possibleAGE = pAGE;
   }
 
   /**
    * Returns whether the participant can be in kitchen.
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be in kitchen.
    */
   public final boolean isPossibleKitchen() {
     return this.possibleKitchen;
   }
 
   /**
    * Sets whether the participant can be in kitchen.
    * 
    * @since Date: Jun 18, 2011
    * @param pk <code>true</code>, if the participant can be in kitchen.
    */
   public final void setPossibleKitchen(final boolean pk) {
     this.possibleKitchen = pk;
   }
 
   /**
    * Returns whether the participant can be in a seminar.
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be in a seminar
    */
   public final boolean isPossibleSeminar() {
     return this.possibleSeminar;
   }
 
   /**
    * Sets whether the participant can be in a seminar.
    * 
    * @since Date: Jun 18, 2011
    * @param ps <code>true</code>, if the participant can be in a seminar
    */
   public final void setPossibleSeminar(final boolean ps) {
     this.possibleSeminar = ps;
   }
 
   /**
    * Returns whether the participant can be member of something else.
    * 
    * @since Date: Jun 18, 2011
    * @return <code>true</code>, if the participant can be member of anything else.
    */
   public final boolean isPossibleMisc() {
     return this.possibleMisc;
   }
 
   /**
    * Sets whether the participant can be member of anything else.
    * 
    * @since Date: Jun 18, 2011
    * @param pm <code>true</code>, if the participant can be member of anything else.
    */
   public final void setPossibleMisc(final boolean pm) {
     this.possibleMisc = pm;
   }
 
   /**
    * Returns the id of this participant as stored in the data base.
    * 
    * @author croesch
    * @since Date: Jun 18, 2011
    * @return the id that identifies this participant in something like a data base.
    */
   public final long getId() {
     return this.id;
   }
 
   @Override
   public final int hashCode() {
     final int prime = 31;
     int result = prime + getHash(this.bank);
     result = prime * result + this.bankAccountNumber;
     result = prime * result + this.bankCodeNumber;
     result = prime * result + getHash(this.birthDate);
     result = prime * result + getHash(this.comment);
     result = prime * result + this.countyCouncil.hashCode();
     result = prime * result + getHash(this.dateSinceInDataBase);
     result = prime * result + getHash(this.dateUpToInSystem);
     result = prime * result + getHash(this.denomination);
     result = prime * result + getHash(this.fax);
     result = prime * result + getHash(this.foreName);
     result = prime * result + this.gender.hashCode();
     result = prime * result + Long.valueOf(this.id).hashCode();
     result = prime * result + getHash(this.lastName);
     result = prime * result + this.livingAddress.hashCode();
     result = prime * result + getHash(this.mailAddress);
     result = prime * result + getHash(this.mobilePhone);
     result = prime * result + getHash(this.phone);
     result = prime * result + getHash(this.phoneOfParents);
     result = prime * result + Boolean.valueOf(this.possibleAGE).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleBoard).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleExtendedBoard).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleKitchen).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleMAK).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleMisc).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleParticipant).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleSeminar).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleStaff).hashCode();
     result = prime * result + Boolean.valueOf(this.possibleStaffYouth).hashCode();
     result = prime * result + this.postToAddress.hashCode();
     return result;
   }
 
   /**
    * Returns the hashcode of the given {@link String}
    * 
    * @since Date: Jul 8, 2011
    * @param s the string to create a hashcode from
    * @return the hashcode of the given {@link String}
    * @see String#hashCode()
    */
   private static int getHash(final String s) {
     if (s == null) {
       return 0;
     }
     return s.hashCode();
   }
 
   /**
    * Returns the hashcode of the given {@link Date}
    * 
    * @since Date: Jul 8, 2011
    * @param d the date to create a hashcode from
    * @return the hashcode of the given {@link Date}
    * @see Date#hashCode()
    */
   private static int getHash(final Date d) {
     if (d == null) {
       return 0;
     }
     return d.hashCode();
   }
 
   /**
    * Returns the hashcode of the given {@link Denomination}
    * 
    * @since Date: Jul 8, 2011
    * @param d the denomination to create a hashcode from
    * @return the hashcode of the given {@link Denomination}
    * @see Denomination#hashCode()
    */
   private static int getHash(final Denomination d) {
     if (d == null) {
       return 0;
     }
     return d.hashCode();
   }
 
   @Override
   public final boolean equals(final Object obj) {
     if (this == obj) {
       return true;
     }
     if (obj == null) {
       return false;
     }
     if (!(obj instanceof Participant)) {
       return false;
     }
     final Participant other = (Participant) obj;
     if (this.bank == null) {
       if (other.bank != null) {
         return false;
       }
     } else if (!this.bank.equals(other.bank)) {
       return false;
     }
     if (this.bankAccountNumber != other.bankAccountNumber) {
       return false;
     }
     if (this.bankCodeNumber != other.bankCodeNumber) {
       return false;
     }
     if (!areDatesEqual(this.birthDate, other.birthDate)) {
       return false;
     }
     if (this.comment == null) {
       if (other.comment != null) {
         return false;
       }
     } else if (!this.comment.equals(other.comment)) {
       return false;
     }
     if (this.countyCouncil != other.countyCouncil) {
       return false;
     }
     if (!areDatesEqual(this.dateSinceInDataBase, other.dateSinceInDataBase)) {
       return false;
     }
     if (!areDatesEqual(this.dateUpToInSystem, other.dateUpToInSystem)) {
       return false;
     }
     if (this.denomination != other.denomination) {
       return false;
     }
     if (this.fax == null) {
       if (other.fax != null) {
         return false;
       }
     } else if (!this.fax.equals(other.fax)) {
       return false;
     }
     if (!this.foreName.equals(other.foreName)) {
       return false;
     }
     if (this.gender != other.gender) {
       return false;
     }
     if (this.id != other.id) {
       return false;
     }
     if (!this.lastName.equals(other.lastName)) {
       return false;
     }
     if (!this.livingAddress.equals(other.livingAddress)) {
       return false;
     }
     if (this.mailAddress == null) {
       if (other.mailAddress != null) {
         return false;
       }
     } else if (!this.mailAddress.equals(other.mailAddress)) {
       return false;
     }
     if (this.mobilePhone == null) {
       if (other.mobilePhone != null) {
         return false;
       }
     } else if (!this.mobilePhone.equals(other.mobilePhone)) {
       return false;
     }
     if (this.phone == null) {
       if (other.phone != null) {
         return false;
       }
     } else if (!this.phone.equals(other.phone)) {
       return false;
     }
     if (this.phoneOfParents == null) {
       if (other.phoneOfParents != null) {
         return false;
       }
     } else if (!this.phoneOfParents.equals(other.phoneOfParents)) {
       return false;
     }
     if (!this.postToAddress.equals(other.postToAddress)) {
       return false;
     }
     return arePossibilitiesEqual(other);
   }
 
   /**
    * Returns whether two dates are equal, ignoring the time.
    * 
    * @since Date: Jul 10, 2011
    * @param one the date one to compare
    * @param two the date two to compare
    * @return <code>true</code>, if the dates are equal.
    */
   private boolean areDatesEqual(final Date one, final Date two) {
     if (one == null) {
       if (two != null) {
         return false;
       }
       return true;
     }
 
     if (one.equals(two)) {
       return true;
     }
     final Calendar cal1 = new GregorianCalendar();
     cal1.setTime(one);
 
     final Calendar cal2 = new GregorianCalendar();
     cal2.setTime(two);
 
     if (cal1.get(Calendar.DAY_OF_MONTH) != cal2.get(Calendar.DAY_OF_MONTH)) {
       return false;
     }
 
     if (cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) {
       return false;
     }
 
     if (cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
       return false;
     }
     return true;
   }
 
   /**
    * Returns whether the possible functions of this {@link Participant} are equal to the possible functions of the given
    * {@link Participant}.
    * 
    * @since Date: Jul 8, 2011
    * @param other {@link Participant} to compare with, must not be <code>null</code>!
    * @return <code>true</code>, if all possibilities are the same.
    */
   private boolean arePossibilitiesEqual(final Participant other) {
     if (this.possibleAGE != other.possibleAGE) {
       return false;
     }
     if (this.possibleBoard != other.possibleBoard) {
       return false;
     }
     if (this.possibleExtendedBoard != other.possibleExtendedBoard) {
       return false;
     }
     if (this.possibleKitchen != other.possibleKitchen) {
       return false;
     }
     if (this.possibleMAK != other.possibleMAK) {
       return false;
     }
     if (this.possibleMisc != other.possibleMisc) {
       return false;
     }
     if (this.possibleParticipant != other.possibleParticipant) {
       return false;
     }
     if (this.possibleSeminar != other.possibleSeminar) {
       return false;
     }
     if (this.possibleStaff != other.possibleStaff) {
       return false;
     }
     if (this.possibleStaffYouth != other.possibleStaffYouth) {
       return false;
     }
     return true;
   }
 
   @Override
   public final String toString() {
     final StringBuilder builder = new StringBuilder();
     builder.append(Text.PARTICIPANT);
     builder.append(" [").append(Text.PARTICIPANT_ID).append("=").append(this.id);
     builder.append(", ").append(Text.PARTICIPANT_LASTNAME).append("=");
     builder.append(this.lastName);
     builder.append(", ").append(Text.PARTICIPANT_FORENAME).append("=");
     builder.append(this.foreName);
     builder.append(", ").append(Text.PARTICIPANT_GENDER).append("=");
     builder.append(this.gender);
     builder.append(", ").append(Text.PARTICIPANT_DENOMINTAION).append("=");
     builder.append(this.denomination);
     builder.append(", ").append(Text.PARTICIPANT_BIRTHDAY).append("=");
     builder.append(this.birthDate);
     builder.append(", ").append(Text.PARTICIPANT_ADDRESS_LIVING).append("=");
     builder.append(this.livingAddress);
     builder.append(", ").append(Text.PARTICIPANT_ADDRESS_POSTAL).append("=");
     builder.append(this.postToAddress);
     builder.append(", ").append(Text.PARTICIPANT_PHONE).append("=");
     builder.append(this.phone);
     builder.append(", ").append(Text.PARTICIPANT_FAX).append("=");
     builder.append(this.fax);
     builder.append(", ").append(Text.PARTICIPANT_MOBILE_PHONE).append("=");
     builder.append(this.mobilePhone);
     builder.append(", ").append(Text.PARTICIPANT_PHONE_OF_PARENTS).append("=");
     builder.append(this.phoneOfParents);
     builder.append(", ").append(Text.PARTICIPANT_MAIL_ADDRESS).append("=");
     builder.append(this.mailAddress);
     builder.append(", ").append(Text.PARTICIPANT_COUNTY_COUNCIL).append("=");
     builder.append(this.countyCouncil);
     builder.append(", ").append(Text.PARTICIPANT_BANK_CODE_NUMBER).append("=");
     builder.append(this.bankCodeNumber);
     builder.append(", ").append(Text.PARTICIPANT_BANK_NAME).append("=");
     builder.append(this.bank);
     builder.append(", ").append(Text.PARTICIPANT_BANK_ACCOUNT_NUMBER).append("=");
     builder.append(this.bankAccountNumber);
     builder.append(", ").append(Text.PARTICIPANT_COMMENT).append("=");
     builder.append(this.comment);
     builder.append(", ").append(Text.PARTICIPANT_DATE_SINCE).append("=");
     builder.append(this.dateSinceInDataBase);
     builder.append(", ").append(Text.PARTICIPANT_DATE_UNTIL).append("=");
     builder.append(this.dateUpToInSystem);
     builder.append(", ").append(Text.PARTICIPANT_CAMP_PARTICIPANT).append("=");
     builder.append(this.possibleParticipant);
     builder.append(", ").append(Text.PARTICIPANT_STAFF_GENERAL).append("=");
     builder.append(this.possibleStaff);
     builder.append(", ").append(Text.PARTICIPANT_STAFF_YOUTH).append("=");
     builder.append(this.possibleStaffYouth);
     builder.append(", ").append(Text.PARTICIPANT_BOARD).append("=");
     builder.append(this.possibleBoard);
     builder.append(", ").append(Text.PARTICIPANT_EXTENDED_BOARD).append("=");
     builder.append(this.possibleExtendedBoard);
     builder.append(", ").append(Text.PARTICIPANT_MAK).append("=");
     builder.append(this.possibleMAK);
     builder.append(", ").append(Text.PARTICIPANT_AGE).append("=");
     builder.append(this.possibleAGE);
     builder.append(", ").append(Text.PARTICIPANT_CAMP_KITCHEN).append("=");
     builder.append(this.possibleKitchen);
     builder.append(", ").append(Text.PARTICIPANT_SEMINAR).append("=");
     builder.append(this.possibleSeminar);
     builder.append(", ").append(Text.PARTICIPANT_MISC).append("=");
     builder.append(this.possibleMisc);
     builder.append("]");
     return builder.toString();
   }
 
 }
