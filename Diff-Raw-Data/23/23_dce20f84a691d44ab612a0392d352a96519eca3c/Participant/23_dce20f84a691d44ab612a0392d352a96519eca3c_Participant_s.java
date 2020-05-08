 package com.github.croesch.partimana.types;
 
 import com.github.croesch.annotate.MayBeNull;
 import com.github.croesch.annotate.NotNull;
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.types.api.IFilterable;
 import com.github.croesch.partimana.types.exceptions.RequiredFieldSetToNullException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Represents a participant.
  *
  * @author croesch
  * @since Date: May 29, 2011
  */
 public final class Participant implements IFilterable {
 
   /** the id of that participant as stored in data base */
   private final long id;
 
   /** the highest id to calculate the next id for a new participant */
   private static long highestId = 0;
 
   /** the last name of the participant */
   @NotNull
   private String lastName = null;
 
   /** the first name of the participant */
   @NotNull
   private String foreName = null;
 
   /** the gender of the participant */
   @NotNull
   private Gender gender = null;
 
   /** the denomination/confession of the participant */
   @NotNull
   private Denomination denomination = null;
 
   /** the date of birth of the participant */
   @NotNull
   private Date birthDate = null;
 
   /** the address where the participant lives */
   @NotNull
   private final Address livingAddress = new Address();
 
   /** the address to post to, if different to the address where the participant lives */
   @NotNull
   private final Address postToAddress = new Address();
 
   /** the phone number of the participant */
   @MayBeNull
   private String phone = null;
 
   /** the fax number of the participant */
   @MayBeNull
   private String fax = null;
 
   /** the mobile phone number of the participant */
   @MayBeNull
   private String mobilePhone = null;
 
   /** the phone number of the parents of the participant */
   @MayBeNull
   private String phoneOfParents = null;
 
   /** the mail address of the participant */
   @MayBeNull
   private String mailAddress = null;
 
   /** the county council of the participant */
   @NotNull
   private CountyCouncil countyCouncil = null;
 
   /** the bank code number of the participant */
   private int bankCodeNumber = 0;
 
   /** the name of the bank of the participant */
   @MayBeNull
   private String bank = null;
 
   /** the bank account number of the participant */
   private int bankAccountNumber = 0;
 
   /** a comment of the staff about the participant, eg about medicine */
   @MayBeNull
   private String comment = null;
 
   /** date since the participant is registered */
   @MayBeNull
   private Date dateSinceInDataBase = null;
 
   /** date until the participant is a member in the system */
   @MayBeNull
   private Date dateUpToInSystem = null;
 
   /**
    * Constructs a new {@link Participant}.
    *
    * @param forcedId  the number to identify this participant, must be higher than the highest number until now
    * @param name      the last name of the person
    * @param firstName the first name of the person
    * @param g         the gender of the person
    * @param den       the denomination/confession of the person
    * @param birth     the birthday of the person
    * @param str       the street where the person lives
    * @param pc        the post code of the city where the person lives
    * @param c         the city where the person lives
    * @param county    the county council for this person
    * @throws IllegalArgumentException if the id is too small
    * @since Date: Jun 16, 2011
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
     setNewHighestIdTo(forcedId);
   }
 
   /**
    * Sets the new highest id to the given value.
    *
    * @param newId the new highest id of the {@link Participant}s.
    * @since Date: Sep 12, 2012
    */
   private static void setNewHighestIdTo(final long newId) {
     if (newId <= highestId) {
       return;
     }
     highestId = newId;
   }
 
   /**
    * Constructs a new {@link Participant}.
    *
    * @param name      the last name of the person
    * @param firstName the first name of the person
    * @param g         the gender of the person
    * @param den       the denomination/confession of the person
    * @param birth     the birthday of the person
    * @param str       the street where the person lives
    * @param pc        the post code of the city where the person lives
    * @param c         the city where the person lives
    * @param county    the county council for this person
    * @since Date: Jun 16, 2011
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
    * @param p the {@link Participant} to fetch the data from.
    * @throws RequiredFieldSetToNullException if the given {@link Participant} is <code>null</code>
    * @since Date: Jun 16, 2011
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
     this.postToAddress.setCity(p.postToAddress.getCity());
     this.postToAddress.setPostCode(p.postToAddress.getPostCode());
     this.postToAddress.setStreet(p.postToAddress.getStreet());
   }
 
   /**
    * Gets the last name of the participant.
    *
    * @return the last name of the participant
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public String getLastName() {
     return this.lastName;
   }
 
   /**
    * Sets the last name of the participant.
    *
    * @param name the last name, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given name is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setLastName(final String name) throws RequiredFieldSetToNullException {
     if (name == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.lastName = name;
   }
 
   /**
    * Gets the fore name of the participant.
    *
    * @return the fore name of the participant
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public String getForeName() {
     return this.foreName;
   }
 
   /**
    * Sets the first name of the participant.
    *
    * @param name the first name, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given name is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setForeName(final String name) throws RequiredFieldSetToNullException {
     if (name == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.foreName = name;
   }
 
   /**
    * Gets the gender of the participant.
    *
    * @return the gender of the participant
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public Gender getGender() {
     return this.gender;
   }
 
   /**
    * Sets the gender of the participant.
    *
    * @param g the gender of the participant, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given gender is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setGender(final Gender g) throws RequiredFieldSetToNullException {
     if (g == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.gender = g;
   }
 
   /**
    * Gets the denomination of the participant.
    *
    * @return the denomination of the participant
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public Denomination getDenomination() {
     return this.denomination;
   }
 
   /**
    * Sets the denomination of the participant.
    *
    * @param den the denomination of the participant
    * @throws RequiredFieldSetToNullException if the given gender is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setDenomination(final Denomination den) throws RequiredFieldSetToNullException {
     if (den == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.denomination = den;
   }
 
   /**
    * Gets the date of birth of the participant.
    *
    * @return the date of birth of the participant
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public Date getBirthDate() {
     return (Date) this.birthDate.clone();
   }
 
   /**
    * Sets the birth day date of the participant.
    *
    * @param birth the date of birth day of the participant, mustn't be <code>null</code>
    * @throws RequiredFieldSetToNullException if the given birth date is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setBirthDate(final Date birth) throws RequiredFieldSetToNullException {
     if (birth == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.birthDate = new Date(birth.getTime());
   }
 
   /**
    * Gets the street of the participant (where he lives).
    *
    * @return the street of the participant (where he lives)
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public String getStreet() {
     return this.livingAddress.getStreet();
   }
 
   /**
    * Sets the street where the participant lives.
    *
    * @param str the street where the participant lives.
    * @throws RequiredFieldSetToNullException if the given street is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setStreet(final String str) throws RequiredFieldSetToNullException {
     if (str == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.livingAddress.setStreet(str);
   }
 
   /**
    * Gets the post code of the participant (where he lives).
    *
    * @return the post code of the participant (where he lives)
    * @since Date: Jun 18, 2011
    */
   public int getPostCode() {
     return this.livingAddress.getPostCode();
   }
 
   /**
    * Sets the post code where the participant lives.
    *
    * @param pc the post code where the participant lives.
    * @throws IllegalArgumentException if the given post code is less than zero
    * @since Date: Jun 18, 2011
    */
   public void setPostCode(final int pc) throws IllegalArgumentException {
     final int highestPostCode = 99999;
     if (pc < 0 || pc > highestPostCode) {
       throw new IllegalArgumentException();
     }
     this.livingAddress.setPostCode(pc);
   }
 
   /**
    * Gets the city of the participant (where he lives).
    *
    * @return the city of the participant (where he lives)
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public String getCity() {
     return this.livingAddress.getCity();
   }
 
   /**
    * Sets the city where the participant lives.
    *
    * @param c the city where the participant lives
    * @throws RequiredFieldSetToNullException if the given city is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setCity(final String c) throws RequiredFieldSetToNullException {
     if (c == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.livingAddress.setCity(c);
   }
 
   /**
    * Gets the street of the participant (where to post to).
    *
    * @return the street of the participant (where to post to)
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getStreetPostal() {
     return this.postToAddress.getStreet();
   }
 
   /**
    * Sets the street of the participant (where to post to).
    *
    * @param sp the street of the participant (where to post to)
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public void setStreetPostal(final String sp) {
     this.postToAddress.setStreet(sp);
   }
 
   /**
    * Gets the post code of the participant (where to post to).
    *
    * @return the post code of the participant (where to post to)
    * @since Date: Jun 18, 2011
    */
   public int getPostCodePostal() {
     return this.postToAddress.getPostCode();
   }
 
   /**
    * Sets the post code where to post to.
    *
    * @param pc the post code where to post to, if different to post code where the participant lives.
    * @throws IllegalArgumentException if the given post code is less than zero or greater than 99999
    * @since Date: Jun 18, 2011
    */
   public void setPostCodePostal(final int pc) throws IllegalArgumentException {
     final int highestPostCode = 99999;
     if (pc < 0 || pc > highestPostCode) {
       throw new IllegalArgumentException();
     }
     this.postToAddress.setPostCode(pc);
   }
 
   /**
    * Gets the city of the participant (where to post to).
    *
    * @return the city of the participant (where to post to)
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getCityPostal() {
     return this.postToAddress.getCity();
   }
 
   /**
    * Sets the city of the participant (where to post to).
    *
    * @param cp the city of the participant (where to post to)
    * @since Date: Jun 18, 2011
    */
   public void setCityPostal(final String cp) {
     this.postToAddress.setCity(cp);
   }
 
   /**
    * Gets the phone number of the participant.
    *
    * @return the phone number of the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getPhone() {
     return this.phone;
   }
 
   /**
    * Sets the phone number of the participant.
    *
    * @param p the phone number of the participant
    * @since Date: Jun 18, 2011
    */
   public void setPhone(final String p) {
     this.phone = p;
   }
 
   /**
    * Gets the fax number of the participant.
    *
    * @return the fax number of the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getFax() {
     return this.fax;
   }
 
   /**
    * Sets the fax number of the participant.
    *
    * @param f the fax number of the participant
    * @since Date: Jun 18, 2011
    */
   public void setFax(final String f) {
     this.fax = f;
   }
 
   /**
    * Gets the mobile phone number of the participant.
    *
    * @return the mobile phone number of the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getMobilePhone() {
     return this.mobilePhone;
   }
 
   /**
    * Sets the mobile phone number of the participant.
    *
    * @param mobile the mobile phone number of the participant
    * @since Date: Jun 18, 2011
    */
   public void setMobilePhone(final String mobile) {
     this.mobilePhone = mobile;
   }
 
   /**
    * Gets the phone number of the parents of the participant.
    *
    * @return the phone number of the parents of the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getPhoneOfParents() {
     return this.phoneOfParents;
   }
 
   /**
    * Sets the phone number of the parents of the participant.
    *
    * @param pop the phone number of the parents of the participant
    * @since Date: Jun 18, 2011
    */
   public void setPhoneOfParents(final String pop) {
     this.phoneOfParents = pop;
   }
 
   /**
    * Gets the mail address of the participant.
    *
    * @return the mail address of the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getMailAddress() {
     return this.mailAddress;
   }
 
   /**
    * Sets the mail address of the participant.
    *
    * @param mail the mail address of the participant
    * @since Date: Jun 18, 2011
    */
   public void setMailAddress(final String mail) {
     this.mailAddress = mail;
   }
 
   /**
    * Gets the county council of the participant.
    *
    * @return the county council of the participant
    * @since Date: Jun 18, 2011
    */
   @NotNull
   public CountyCouncil getCountyCouncil() {
     return this.countyCouncil;
   }
 
   /**
    * Sets the county council of the participant.
    *
    * @param cc the county council of the participant.
    * @throws RequiredFieldSetToNullException if the given county council is <code>null</code>
    * @since Date: Jun 18, 2011
    */
   public void setCountyCouncil(final CountyCouncil cc) throws RequiredFieldSetToNullException {
     if (cc == null) {
       throw new RequiredFieldSetToNullException();
     }
     this.countyCouncil = cc;
   }
 
   /**
    * Gets the bank code number of the participant.
    *
    * @return the bank code number of the participant
    * @since Date: Jun 18, 2011
    */
   public int getBankCodeNumber() {
     return this.bankCodeNumber;
   }
 
   /**
    * Sets the bank code number of the participant.
    *
    * @param bcn the bank code number of the participant
    * @since Date: Jun 18, 2011
    */
   public void setBankCodeNumber(final int bcn) {
     this.bankCodeNumber = bcn;
   }
 
   /**
    * Gets the bank of the participant.
    *
    * @return the bank of the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getBank() {
     return this.bank;
   }
 
   /**
    * Sets the bank of the participant.
    *
    * @param b the bank of the participant
    * @since Date: Jun 18, 2011
    */
   public void setBank(final String b) {
     this.bank = b;
   }
 
   /**
    * Gets the bank account number of the participant.
    *
    * @return the bank account number of the participant
    * @since Date: Jun 18, 2011
    */
   public int getBankAccountNumber() {
     return this.bankAccountNumber;
   }
 
   /**
    * Sets the bank account number of the participant.
    *
    * @param ban the bank account number of the participant
    * @since Date: Jun 18, 2011
    */
   public void setBankAccountNumber(final int ban) {
     this.bankAccountNumber = ban;
   }
 
   /**
    * Gets the comment about the participant.
    *
    * @return the comment about the participant
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public String getComment() {
     return this.comment;
   }
 
   /**
    * Sets the comment about the participant.
    *
    * @param com the comment about the participant
    * @since Date: Jun 18, 2011
    */
   public void setComment(final String com) {
     this.comment = com;
   }
 
   /**
    * Gets the date since the participant is in the system.
    *
    * @return the date since the participant is in the system
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public Date getDateSinceInDataBase() {
     if (this.dateSinceInDataBase == null) {
       return null;
     }
     return (Date) this.dateSinceInDataBase.clone();
   }
 
   /**
    * Sets the date since the user is in the data base.
    *
    * @param since the date since the user is in the database
    * @since Date: Jun 18, 2011
    */
   public void setDateSinceInDataBase(final Date since) {
     if (since != null) {
       this.dateSinceInDataBase = new Date(since.getTime());
     } else {
       this.dateSinceInDataBase = null;
     }
   }
 
   /**
    * Gets the date up to that the user will be in the system.
    *
    * @return the date up to that the user will be in the system
    * @since Date: Jun 18, 2011
    */
   @MayBeNull
   public Date getDateUpToInSystem() {
     if (this.dateUpToInSystem == null) {
       return null;
     }
     return (Date) this.dateUpToInSystem.clone();
   }
 
   /**
    * Sets the date up to that the user is in the system.
    *
    * @param until the date up to that the user is in the system.
    * @since Date: Jun 18, 2011
    */
   public void setDateUpToInSystem(final Date until) {
     if (until != null) {
       this.dateUpToInSystem = new Date(until.getTime());
     } else {
       this.dateUpToInSystem = null;
     }
   }
 
   /**
    * Returns the id of this participant as stored in the data base.
    *
    * @return the id that identifies this participant in something like a data base.
    * @since Date: Jun 18, 2011
    */
   public long getId() {
     return this.id;
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = prime + getHash(this.bank);
     result = prime * result + this.bankAccountNumber;
     result = prime * result + this.bankCodeNumber;
     result = prime * result + getHash(this.birthDate);
     result = prime * result + getHash(this.comment);
     result = prime * result + this.countyCouncil.hashCode();
     result = prime * result + getHash(this.dateSinceInDataBase);
     result = prime * result + getHash(this.dateUpToInSystem);
     result = prime * result + this.denomination.hashCode();
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
     result = prime * result + this.postToAddress.hashCode();
     return result;
   }
 
   /**
    * Returns the hash code of the given {@link String}
    *
    * @param s the string to create a hash code from
    * @return the hash code of the given {@link String}
    * @see String#hashCode()
    * @since Date: Jul 8, 2011
    */
   private static int getHash(final String s) {
     if (s == null) {
       return 0;
     }
     return s.hashCode();
   }
 
   /**
    * Returns the hash code of the given {@link Date}
    *
    * @param d the date to create a hash code from
    * @return the hash code of the given {@link Date}
    * @see Date#hashCode()
    * @since Date: Jul 8, 2011
    */
   private static int getHash(final Date d) {
     if (d == null) {
       return 0;
     }
     return d.hashCode();
   }
 
   @Override
   public boolean equals(final Object obj) {
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
     if (areDatesDifferent(this.birthDate, other.birthDate)) {
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
     if (areDatesDifferent(this.dateSinceInDataBase, other.dateSinceInDataBase)) {
       return false;
     }
     if (areDatesDifferent(this.dateUpToInSystem, other.dateUpToInSystem)) {
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
     return this.postToAddress.equals(other.postToAddress);
   }
 
   /**
    * Returns whether two dates are equal, ignoring the time.
    *
    * @param one the date one to compare
    * @param two the date two to compare
    * @return <code>true</code>, if the dates are equal.
    * @since Date: Jul 10, 2011
    */
   private boolean areDatesDifferent(final Date one, final Date two) {
     if (one == null) {
       return two != null;
     }
     if (two == null) {
       return true;
     }
 
     if (one.equals(two)) {
       return false;
     }
     final Calendar cal1 = new GregorianCalendar();
     cal1.setTime(one);
 
     final Calendar cal2 = new GregorianCalendar();
     cal2.setTime(two);
 
     if (cal1.get(Calendar.DAY_OF_MONTH) != cal2.get(Calendar.DAY_OF_MONTH)) {
       return true;
     }
 
     if (cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) {
       return true;
     }
 
     return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR);
   }
 
   @Override
   public String toString() {
     return String.valueOf(Text.PARTICIPANT) + " [" + Text.PARTICIPANT_ID + "=" + this.id + ", "
            + Text.PARTICIPANT_LASTNAME + "=" + this.lastName + ", " + Text.PARTICIPANT_FORENAME + "=" + this.foreName
           + ", " + Text.PARTICIPANT_GENDER + "=" + this.gender + ", " + Text.PARTICIPANT_DENOMINTAION + "="
            + this.denomination + ", " + Text.PARTICIPANT_BIRTHDAY + "=" + this.birthDate + ", "
            + Text.PARTICIPANT_ADDRESS_LIVING + "=" + this.livingAddress + ", " + Text.PARTICIPANT_ADDRESS_POSTAL + "="
            + this.postToAddress + ", " + Text.PARTICIPANT_PHONE + "=" + this.phone + ", " + Text.PARTICIPANT_FAX + "="
            + this.fax + ", " + Text.PARTICIPANT_MOBILE_PHONE + "=" + this.mobilePhone + ", "
            + Text.PARTICIPANT_PHONE_OF_PARENTS + "=" + this.phoneOfParents + ", " + Text.PARTICIPANT_MAIL_ADDRESS + "="
            + this.mailAddress + ", " + Text.PARTICIPANT_COUNTY_COUNCIL + "=" + this.countyCouncil + ", "
            + Text.PARTICIPANT_BANK_CODE_NUMBER + "=" + this.bankCodeNumber + ", " + Text.PARTICIPANT_BANK_NAME + "="
            + this.bank + ", " + Text.PARTICIPANT_BANK_ACCOUNT_NUMBER + "=" + this.bankAccountNumber + ", "
            + Text.PARTICIPANT_COMMENT + "=" + this.comment + ", " + Text.PARTICIPANT_DATE_SINCE + "="
            + this.dateSinceInDataBase + ", " + Text.PARTICIPANT_DATE_UNTIL + "=" + this.dateUpToInSystem + "]";
   }
 }
