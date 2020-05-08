 package com.github.croesch.types;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.croesch.types.exceptions.RequiredFieldSetToNullException;
 
 /**
  * Provides different test methods for {@link Participant}
  * 
  * @author croesch
  * @since Date: Jun 18, 2011
  */
 public class ParticipantTest {
 
   /** example post code */
   private static final int SAMPLE_POSTCODE = 12345;
 
   /** object to test with */
   private Participant participant;
 
   /**
    * Sets up an object of {@link Participant}
    * 
    * @author croesch
    * @since Date: Jun 18, 2011
    * @throws java.lang.Exception
    */
   @Before
   public final void setUp() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for {@link Participant#Participant(Participant)}.
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantParticipantRFSTNE() {
     new Participant(null);
   }
 
   /**
    * Test method for {@link Participant#Participant(Participant)}.
    */
   @Test
   public final void testParticipantParticipant() {
     final Date d1 = new Date(123);
 
     this.participant.setBank("bank");
     this.participant.setBankAccountNumber(1);
     this.participant.setBankCodeNumber(12);
     this.participant.setComment("comment");
     this.participant.setDateSinceInDataBase(d1);
     this.participant.setDateUpToInSystem(d1);
     this.participant.setDenomination(Denomination.JEWISH);
     this.participant.setFax("fax");
     this.participant.setMailAddress("mail");
     this.participant.setMobilePhone("mobile");
     this.participant.setPhone("phone");
     this.participant.setPhoneOfParents("phone");
     this.participant.setPossibleAGE(true);
     this.participant.setPossibleBoard(false);
     this.participant.setPossibleExtendedBoard(false);
     this.participant.setPossibleKitchen(true);
     this.participant.setPossibleMAK(true);
     this.participant.setPossibleMisc(false);
     this.participant.setPossibleParticipant(false);
     this.participant.setPossibleSeminar(false);
     this.participant.setPossibleStaff(false);
     this.participant.setPossibleStaffYouth(false);
     this.participant.setCityPostal("city");
     this.participant.setPostCodePostal(3124);
     this.participant.setStreetPostal("street");
 
     final Participant p = new Participant(this.participant);
     assertThat(p).isEqualTo(this.participant);
 
     d1.setTime(1212);
     assertThat(p.getDateSinceInDataBase()).isNotEqualTo(d1);
     assertThat(p.getDateUpToInSystem()).isNotEqualTo(d1);
     assertThat(p).isEqualTo(this.participant);
 
     p.setBirthDate(d1);
     assertThat(p).isNotEqualTo(this.participant);
 
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE1() {
     this.participant = new Participant(null,
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE2() {
     this.participant = new Participant("Mustermann",
                                        null,
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE3() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        null,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE4() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        null,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE5() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        null,
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE6() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        null,
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE7() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        null,
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = RequiredFieldSetToNullException.class)
   public final void testParticipantRFSTNE8() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        null);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testParticipantIAE() {
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        new Date(),
                                        "Musterstrasse 12",
                                        -1,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
   }
 
   /**
    * Test method for
    * {@link Participant#Participant(String, String, Gender, Denomination, Date, String, int, String, CountyCouncil)} .
    */
   @Test
   public final void testParticipant() {
     final Date birth = new Date();
     this.participant = new Participant("Mustermann",
                                        "Max",
                                        Gender.MALE,
                                        Denomination.OTHER,
                                        birth,
                                        "Musterstrasse 12",
                                        SAMPLE_POSTCODE,
                                        "Musterhausen",
                                        CountyCouncil.OTHER);
     assertThat(this.participant.getForeName()).isEqualTo("Max");
     assertThat(this.participant.getLastName()).isEqualTo("Mustermann");
     assertThat(this.participant.getGender()).isEqualTo(Gender.MALE);
     assertThat(this.participant.getDenomination()).isEqualTo(Denomination.OTHER);
     assertThat(this.participant.getBirthDate()).isEqualTo(birth);
     assertThat(this.participant.getStreet()).isEqualTo("Musterstrasse 12");
     assertThat(this.participant.getPostCode()).isEqualTo(SAMPLE_POSTCODE);
     assertThat(this.participant.getCity()).isEqualTo("Musterhausen");
     assertThat(this.participant.getCountyCouncil()).isEqualTo(CountyCouncil.OTHER);
   }
 
   /**
    * Test method for {@link Participant#getLastName()} and {@link Participant#setLastName()}.
    */
   @Test
   public final void testSetAndGetLastName() {
     this.participant.setLastName("last");
     assertThat(this.participant.getLastName()).isEqualTo("last");
 
     this.participant.setLastName("another");
     assertThat(this.participant.getLastName()).isEqualTo("another");
 
     this.participant.setLastName("");
     assertThat(this.participant.getLastName()).isEmpty();
   }
 
   /**
    * Test method for {@link Participant#getForeName()} and {@link Participant#setForeName()}.
    */
   @Test
   public final void testSetAndGetForeName() {
     this.participant.setForeName("first");
     assertThat(this.participant.getForeName()).isEqualTo("first");
 
     this.participant.setForeName("another");
     assertThat(this.participant.getForeName()).isEqualTo("another");
 
     this.participant.setForeName("");
     assertThat(this.participant.getForeName()).isEmpty();
   }
 
   /**
    * Test method for {@link Participant#getGender()} and {@link Participant#setGender()}.
    */
   @Test
   public final void testSetAndGetGender() {
     this.participant.setGender(Gender.FEMALE);
     assertThat(this.participant.getGender()).isEqualTo(Gender.FEMALE);
 
     this.participant.setGender(Gender.MALE);
     assertThat(this.participant.getGender()).isEqualTo(Gender.MALE);
   }
 
   /**
    * Test method for {@link Participant#getDenomination()} and {@link Participant#setDenomination()}.
    */
   @Test
   public final void testSetAndGetDenomination() {
     this.participant.setDenomination(Denomination.CATHOLIC);
     assertThat(this.participant.getDenomination()).isEqualTo(Denomination.CATHOLIC);
 
     this.participant.setDenomination(Denomination.JEWISH);
     assertThat(this.participant.getDenomination()).isEqualTo(Denomination.JEWISH);
 
     this.participant.setDenomination(Denomination.FREE_CHURCH);
     assertThat(this.participant.getDenomination()).isEqualTo(Denomination.FREE_CHURCH);
   }
 
   /**
    * Test method for {@link Participant#getBirthDate()} and {@link Participant#setBirthDate()}.
    */
   @Test
   public final void testSetAndGetBirthDate() {
     final Date birth = new Date();
     final Date copy = (Date) birth.clone();
     this.participant.setBirthDate(birth);
     assertThat(this.participant.getBirthDate()).isEqualTo(birth);
 
     birth.setTime(0);
     assertThat(this.participant.getBirthDate()).isNotEqualTo(birth);
     assertThat(this.participant.getBirthDate()).isEqualTo(copy);
 
     final Date d = this.participant.getBirthDate();
     d.setTime(0);
     assertThat(this.participant.getBirthDate()).isEqualTo(copy);
   }
 
   /**
    * Test method for {@link Participant#getStreet()} and {@link Participant#setStreet()}.
    */
   @Test
   public final void testSetAndGetStreet() {
     this.participant.setStreet("street");
     assertThat(this.participant.getStreet()).isEqualTo("street");
 
     this.participant.setStreet("other");
     assertThat(this.participant.getStreet()).isEqualTo("other");
 
     this.participant.setStreet("");
     assertThat(this.participant.getStreet()).isEqualTo("");
   }
 
   /**
    * Test method for {@link Participant#getPostCode()} and {@link Participant#setPostCode()}.
    */
   @Test
   public final void testSetAndGetPostCode() {
     final int pc1 = 1;
     this.participant.setPostCode(pc1);
     assertThat(this.participant.getPostCode()).isEqualTo(pc1);
 
     final int pc2 = 13423;
     this.participant.setPostCode(pc2);
     assertThat(this.participant.getPostCode()).isEqualTo(pc2);
 
     final int pc3 = 77789;
     this.participant.setPostCode(pc3);
     assertThat(this.participant.getPostCode()).isEqualTo(Integer.valueOf(pc3).intValue());
   }
 
   /**
    * Test method for {@link Participant#getPostCode()} and {@link Participant#setPostCode()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testSetAndGetPostCodeIAE1() {
     this.participant.setPostCode(-1);
   }
 
   /**
    * Test method for {@link Participant#getPostCode()} and {@link Participant#setPostCode()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testSetAndGetPostCodeIAE2() {
     final int tooHigh = 100000;
     this.participant.setPostCode(tooHigh);
   }
 
   /**
    * Test method for {@link Participant#getCity()} and {@link Participant#setCity()}.
    */
   @Test
   public final void testSetAndGetCity() {
     this.participant.setCity("stadt");
     assertThat(this.participant.getCity()).isEqualTo("stadt");
 
     this.participant.setCity("");
     assertThat(this.participant.getCity()).isEqualTo("");
 
     this.participant.setCity("...");
     assertThat(this.participant.getCity()).isEqualTo("...");
   }
 
   /**
    * Test method for {@link Participant#getStreetPostal()} and {@link Participant#setStreetPostal()}.
    */
   @Test
   public final void testSetAndGetStreetPostal() {
     this.participant.setStreetPostal("street");
     assertThat(this.participant.getStreetPostal()).isEqualTo("street");
 
     this.participant.setStreetPostal("other");
     assertThat(this.participant.getStreetPostal()).isEqualTo("other");
 
     this.participant.setStreetPostal("");
     assertThat(this.participant.getStreetPostal()).isEqualTo("");
   }
 
   /**
    * Test method for {@link Participant#getPostCodePostal()} and {@link Participant#setPostCodePostal()}.
    */
   @Test
   public final void testSetAndGetPostCodePostal() {
     final int pc1 = 1;
     this.participant.setPostCodePostal(pc1);
     assertThat(this.participant.getPostCodePostal()).isEqualTo(pc1);
 
     final int pc2 = 13423;
     this.participant.setPostCodePostal(pc2);
     assertThat(this.participant.getPostCodePostal()).isEqualTo(pc2);
 
     final int pc3 = 77789;
     this.participant.setPostCodePostal(pc3);
     assertThat(this.participant.getPostCodePostal()).isEqualTo(Integer.valueOf(pc3).intValue());
   }
 
   /**
    * Test method for {@link Participant#getPostCodePostal()} and {@link Participant#setPostCodePostal()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testSetAndGetPostCodePostalIAE1() {
     this.participant.setPostCodePostal(-1);
   }
 
   /**
    * Test method for {@link Participant#getPostCodePostal()} and {@link Participant#setPostCodePostal()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testSetAndGetPostCodePostalIAE2() {
     final int tooHigh = 100000;
     this.participant.setPostCodePostal(tooHigh);
   }
 
   /**
    * Test method for {@link Participant#getCityPostal()} and {@link Participant#setCityPostal()}.
    */
   @Test
   public final void testSetAndGetCityPostal() {
     this.participant.setCityPostal("stadt");
     assertThat(this.participant.getCityPostal()).isEqualTo("stadt");
 
     this.participant.setCityPostal("");
     assertThat(this.participant.getCityPostal()).isEqualTo("");
 
     this.participant.setCityPostal("...");
     assertThat(this.participant.getCityPostal()).isEqualTo("...");
   }
 
   /**
    * Test method for {@link Participant#getPhone()} and {@link Participant#setPhone()}.
    */
   @Test
   public final void testSetAndGetPhone() {
     this.participant.setPhone("...");
     assertThat(this.participant.getPhone()).isEqualTo("...");
 
     this.participant.setPhone(null);
     assertThat(this.participant.getPhone()).isNull();
 
     this.participant.setPhone("0123/456789");
     assertThat(this.participant.getPhone()).isEqualTo("0123/456789");
   }
 
   /**
    * Test method for {@link Participant#getFax()} and {@link Participant#setFax()}.
    */
   @Test
   public final void testSetAndGetFax() {
     this.participant.setFax("...");
     assertThat(this.participant.getFax()).isEqualTo("...");
 
     this.participant.setFax(null);
     assertThat(this.participant.getFax()).isNull();
 
     this.participant.setFax("0123/456789");
     assertThat(this.participant.getFax()).isEqualTo("0123/456789");
   }
 
   /**
    * Test method for {@link Participant#getMobilePhone()} and {@link Participant#setMobilePhone()}.
    */
   @Test
   public final void testSetAndGetMobilePhone() {
     this.participant.setMobilePhone("...");
     assertThat(this.participant.getMobilePhone()).isEqualTo("...");
 
     this.participant.setMobilePhone(null);
     assertThat(this.participant.getMobilePhone()).isNull();
 
     this.participant.setMobilePhone("0123/456789");
     assertThat(this.participant.getMobilePhone()).isEqualTo("0123/456789");
   }
 
   /**
    * Test method for {@link Participant#getPhoneOfParents()} and {@link Participant#setPhoneOfParents()}.
    */
   @Test
   public final void testSetAndGetPhoneOfParents() {
     this.participant.setPhoneOfParents("...");
     assertThat(this.participant.getPhoneOfParents()).isEqualTo("...");
 
     this.participant.setPhoneOfParents(null);
     assertThat(this.participant.getPhoneOfParents()).isNull();
 
     this.participant.setPhoneOfParents("0123/456789");
     assertThat(this.participant.getPhoneOfParents()).isEqualTo("0123/456789");
   }
 
   /**
    * Test method for {@link Participant#getMailAddress()} and {@link Participant#setMailAddress()}.
    */
   @Test
   public final void testSetAndGetMailAddress() {
     this.participant.setMailAddress("...");
     assertThat(this.participant.getMailAddress()).isEqualTo("...");
 
     this.participant.setMailAddress(null);
     assertThat(this.participant.getMailAddress()).isEqualTo(null);
 
     this.participant.setMailAddress("abc@def-ghi.jk");
     assertThat(this.participant.getMailAddress()).isEqualTo("abc@def-ghi.jk");
   }
 
   /**
    * Test method for {@link Participant#getCountyCouncil()} and {@link Participant#setCountyCouncil()}.
    */
   @Test
   public final void testSetAndGetCountyCouncil() {
     this.participant.setCountyCouncil(CountyCouncil.CITY_FRANKENTHAL);
     assertThat(this.participant.getCountyCouncil()).isEqualTo(CountyCouncil.CITY_FRANKENTHAL);
 
     this.participant.setCountyCouncil(CountyCouncil.CITY_LUDWIGSHAFEN);
     assertThat(this.participant.getCountyCouncil()).isEqualTo(CountyCouncil.CITY_LUDWIGSHAFEN);
 
     this.participant.setCountyCouncil(CountyCouncil.COUNTY_BAD_KREUZNACH);
     assertThat(this.participant.getCountyCouncil()).isEqualTo(CountyCouncil.COUNTY_BAD_KREUZNACH);
   }
 
   /**
    * Test method for {@link Participant#getBankCodeNumber()} and {@link Participant#setBankCodeNumber()}.
    */
   @Test
   public final void testSetAndGetBankCodeNumber() {
     final int nr1 = 1234567;
     this.participant.setBankCodeNumber(nr1);
     assertThat(this.participant.getBankCodeNumber()).isEqualTo(nr1);
 
     final int nr2 = 0;
     this.participant.setBankCodeNumber(nr2);
     assertThat(this.participant.getBankCodeNumber()).isEqualTo(nr2);
 
     final int nr3 = 98765432;
     this.participant.setBankCodeNumber(nr3);
     assertThat(this.participant.getBankCodeNumber()).isEqualTo(nr3);
   }
 
   /**
    * Test method for {@link Participant#getBank()} and {@link Participant#setBank()}.
    */
   @Test
   public final void testSetAndGetBank() {
     this.participant.setBank("...");
     assertThat(this.participant.getBank()).isEqualTo("...");
 
     this.participant.setBank(null);
     assertThat(this.participant.getBank()).isEqualTo(null);
 
     this.participant.setBank("bank of him");
     assertThat(this.participant.getBank()).isEqualTo("bank of him");
   }
 
   /**
    * Test method for {@link Participant#getBankAccountNumber()} and {@link Participant#setBankAccountNumber()}.
    */
   @Test
   public final void testSetAndGetBankAccountNumber() {
     final int nr1 = 1234567;
     this.participant.setBankAccountNumber(nr1);
     assertThat(this.participant.getBankAccountNumber()).isEqualTo(nr1);
 
     final int nr2 = 0;
     this.participant.setBankAccountNumber(nr2);
     assertThat(this.participant.getBankAccountNumber()).isEqualTo(nr2);
 
     final int nr3 = 98765432;
     this.participant.setBankAccountNumber(nr3);
     assertThat(this.participant.getBankAccountNumber()).isEqualTo(nr3);
   }
 
   /**
    * Test method for {@link Participant#getComment()} and {@link Participant#setComment()}.
    */
   @Test
   public final void testSetAndGetComment() {
     this.participant.setComment("...");
     assertThat(this.participant.getComment()).isEqualTo("...");
 
     this.participant.setComment(null);
     assertThat(this.participant.getComment()).isEqualTo(null);
 
     this.participant.setComment("this is a stupid comment! <-");
     assertThat(this.participant.getComment()).isEqualTo("this is a stupid comment! <-");
   }
 
   /**
    * Test method for {@link Participant#getDateSinceInDataBase()} and {@link Participant#setDateSinceInDataBase()}.
    */
   @Test
   public final void testSetAndGetDateSinceInDataBase() {
     this.participant.setDateSinceInDataBase(null);
     assertThat(this.participant.getDateSinceInDataBase()).isNull();
 
     Date d1 = new Date();
     this.participant.setDateSinceInDataBase(d1);
     assertThat(this.participant.getDateSinceInDataBase()).isEqualTo(d1);
 
     final Date d2 = new Date(0);
     this.participant.setDateSinceInDataBase(d2);
     assertThat(this.participant.getDateSinceInDataBase()).isEqualTo(d2);
     d2.setTime(d1.getTime());
     assertThat(this.participant.getDateSinceInDataBase()).isNotEqualTo(d2);
 
     this.participant.setDateSinceInDataBase(d2);
     d1 = this.participant.getDateSinceInDataBase();
     d1.setTime(12);
     assertThat(this.participant.getDateSinceInDataBase()).isEqualTo(d2);
   }
 
   /**
    * Test method for {@link Participant#getDateUpToInSystem()} and {@link Participant#setDateUpToInSystem()}.
    */
   @Test
   public final void testSetAndGetDateUpToInSystem() {
     this.participant.setDateUpToInSystem(null);
     assertThat(this.participant.getDateUpToInSystem()).isNull();
 
     Date d1 = new Date();
     this.participant.setDateUpToInSystem(d1);
     assertThat(this.participant.getDateUpToInSystem()).isEqualTo(d1);
 
     final Date d2 = new Date(0);
     this.participant.setDateUpToInSystem(d2);
     assertThat(this.participant.getDateUpToInSystem()).isEqualTo(d2);
     d2.setTime(d1.getTime());
     assertThat(this.participant.getDateUpToInSystem()).isNotEqualTo(d2);
 
     this.participant.setDateUpToInSystem(d2);
     d1 = this.participant.getDateUpToInSystem();
     d1.setTime(12);
     assertThat(this.participant.getDateUpToInSystem()).isEqualTo(d2);
   }
 
   /**
    * Test method for {@link Participant#isPossibleParticipant()} and {@link Participant#setCanBeParticipant()}.
    */
   @Test
   public final void testSetAndIsCanBeParticipant() {
     this.participant.setPossibleParticipant(false);
     assertThat(this.participant.isPossibleParticipant()).isFalse();
 
     this.participant.setPossibleParticipant(true);
     assertThat(this.participant.isPossibleParticipant()).isTrue();
 
     this.participant.setPossibleParticipant(false);
     assertThat(this.participant.isPossibleParticipant()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleStaffYouth()} and {@link Participant#setCanBeStaffYouth()}.
    */
   @Test
   public final void testSetAndIsCanBeStaffYouth() {
     this.participant.setPossibleStaffYouth(false);
     assertThat(this.participant.isPossibleStaffYouth()).isFalse();
 
     this.participant.setPossibleStaffYouth(true);
     assertThat(this.participant.isPossibleStaffYouth()).isTrue();
 
     this.participant.setPossibleStaffYouth(false);
     assertThat(this.participant.isPossibleStaffYouth()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleAGE()} and {@link Participant#setPossibleAGE(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleAGE() {
     this.participant.setPossibleAGE(false);
     assertThat(this.participant.isPossibleAGE()).isFalse();
 
     this.participant.setPossibleAGE(true);
     assertThat(this.participant.isPossibleAGE()).isTrue();
 
     this.participant.setPossibleAGE(false);
     assertThat(this.participant.isPossibleAGE()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleMAK()} and {@link Participant#setPossibleMAK(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleMAK() {
     this.participant.setPossibleMAK(false);
     assertThat(this.participant.isPossibleMAK()).isFalse();
 
     this.participant.setPossibleMAK(true);
     assertThat(this.participant.isPossibleMAK()).isTrue();
 
     this.participant.setPossibleMAK(false);
     assertThat(this.participant.isPossibleMAK()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleBoard()} and {@link Participant#setPossibleBoard(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleBoard() {
     this.participant.setPossibleBoard(false);
     assertThat(this.participant.isPossibleBoard()).isFalse();
 
     this.participant.setPossibleBoard(true);
     assertThat(this.participant.isPossibleBoard()).isTrue();
 
     this.participant.setPossibleBoard(false);
     assertThat(this.participant.isPossibleBoard()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleExtendedBoard()} and
    * {@link Participant#setPossibleExtendedBoard(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleExtendedBoard() {
     this.participant.setPossibleExtendedBoard(false);
     assertThat(this.participant.isPossibleExtendedBoard()).isFalse();
 
     this.participant.setPossibleExtendedBoard(true);
     assertThat(this.participant.isPossibleExtendedBoard()).isTrue();
 
     this.participant.setPossibleExtendedBoard(false);
     assertThat(this.participant.isPossibleExtendedBoard()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleKitchen()} and {@link Participant#setPossibleKitchen(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleKitchen() {
     this.participant.setPossibleKitchen(false);
     assertThat(this.participant.isPossibleKitchen()).isFalse();
 
     this.participant.setPossibleKitchen(true);
     assertThat(this.participant.isPossibleKitchen()).isTrue();
 
     this.participant.setPossibleKitchen(false);
     assertThat(this.participant.isPossibleKitchen()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleMisc()} and {@link Participant#setPossibleMisc(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleMisc() {
     this.participant.setPossibleMisc(false);
     assertThat(this.participant.isPossibleMisc()).isFalse();
 
     this.participant.setPossibleMisc(true);
     assertThat(this.participant.isPossibleMisc()).isTrue();
 
     this.participant.setPossibleMisc(false);
     assertThat(this.participant.isPossibleMisc()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleSeminar()} and {@link Participant#setPossibleSeminar(boolean)}.
    */
   @Test
   public final void testSetAndIsPossibleSeminar() {
     this.participant.setPossibleSeminar(false);
     assertThat(this.participant.isPossibleSeminar()).isFalse();
 
     this.participant.setPossibleSeminar(true);
     assertThat(this.participant.isPossibleSeminar()).isTrue();
 
     this.participant.setPossibleSeminar(false);
     assertThat(this.participant.isPossibleSeminar()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#isPossibleStaff()} and {@link Participant#setCanBeStaff()}.
    */
   @Test
   public final void testSetAndIsCanBeStaff() {
     this.participant.setPossibleStaff(false);
     assertThat(this.participant.isPossibleStaff()).isFalse();
 
     this.participant.setPossibleStaff(true);
     assertThat(this.participant.isPossibleStaff()).isTrue();
 
     this.participant.setPossibleStaff(false);
     assertThat(this.participant.isPossibleStaff()).isFalse();
   }
 
   /**
    * Test method for {@link Participant#getId()}.
    */
   @Test
   public final void testGetId() {
     final long id = this.participant.getId();
     this.participant = new Participant("name",
                                        "first",
                                        Gender.FEMALE,
                                        Denomination.EVANGELIC,
                                        new Date(),
                                        "street",
                                        SAMPLE_POSTCODE,
                                        "city",
                                        CountyCouncil.COUNTY_GERMERSHEIM);
 
     assertThat(this.participant.getId()).isEqualTo(id + 1);
     this.participant = new Participant(id + 2,
                                        "name",
                                        "first",
                                        Gender.FEMALE,
                                        Denomination.EVANGELIC,
                                        new Date(),
                                        "street",
                                        SAMPLE_POSTCODE,
                                        "city",
                                        CountyCouncil.COUNTY_GERMERSHEIM);
     assertThat(this.participant.getId()).isEqualTo(id + 2);
   }
 
   /**
    * Test method for {@link Participant#getId()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testGetIdIAE1() {
     final long id = this.participant.getId();
     this.participant = new Participant(id,
                                        "name",
                                        "first",
                                        Gender.FEMALE,
                                        Denomination.EVANGELIC,
                                        new Date(),
                                        "street",
                                        SAMPLE_POSTCODE,
                                        "city",
                                        CountyCouncil.COUNTY_GERMERSHEIM);
   }
 
   /**
    * Test method for {@link Participant#getId()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testGetIdIAE2() {
     final long id = this.participant.getId();
     this.participant = new Participant(id - 1,
                                        "name",
                                        "first",
                                        Gender.FEMALE,
                                        Denomination.EVANGELIC,
                                        new Date(),
                                        "street",
                                        SAMPLE_POSTCODE,
                                        "city",
                                        CountyCouncil.COUNTY_GERMERSHEIM);
   }
 
   /**
    * Test method for {@link Participant#getId()}.
    */
   @Test(expected = IllegalArgumentException.class)
   public final void testGetIdIAE3() {
     this.participant = new Participant(-1,
                                        "name",
                                        "first",
                                        Gender.FEMALE,
                                        Denomination.EVANGELIC,
                                        new Date(),
                                        "street",
                                        SAMPLE_POSTCODE,
                                        "city",
                                        CountyCouncil.COUNTY_GERMERSHEIM);
   }
 
   /**
    * Test method for {@link Participant#toString()}.
    */
   @Test
   public final void testToString() {
     assertThat(this.participant.toString()).contains(String.valueOf(this.participant.getId()));
   }
 
   /**
    * Test method for {@link Participant#equals(Object)}.
    */
   @Test
   public final void testEquals() {
     Participant p = new Participant("Mustermann",
                                     "Hans",
                                     Gender.FEMALE,
                                     Denomination.OTHER,
                                     new Date(),
                                     "Musterstrasse 12",
                                     SAMPLE_POSTCODE,
                                     "Musterhausen",
                                     CountyCouncil.OTHER);
 
     p.setBank("bank");
     p.setBankAccountNumber(4);
     p.setBankCodeNumber(7);
     p.setComment("com");
     p.setCountyCouncil(CountyCouncil.CITY_LANDAU);
     p.setDateSinceInDataBase(new Date(12));
     p.setDateUpToInSystem(new Date(24));
     p.setDenomination(Denomination.FREE_CHURCH);
     p.setFax("fax");
 
     assertThat(this.participant).isEqualTo(this.participant);
     assertThat(this.participant).isNotEqualTo(null);
     assertThat(this.participant).isNotEqualTo("participant");
     assertThat(this.participant).isNotEqualTo(p);
     assertThat(p).isNotEqualTo(this.participant);
 
     this.participant.setBank("bank");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setBankAccountNumber(4);
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setBankCodeNumber(7);
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setBirthDate(this.participant.getBirthDate());
     assertThat(this.participant).isNotEqualTo(p);
     assertThat(p).isNotEqualTo(this.participant);
 
     this.participant.setComment("comment");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setComment("com");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setCountyCouncil(CountyCouncil.CITY_LANDAU);
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setDateSinceInDataBase(new Date());
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setDateSinceInDataBase(new Date(12));
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setDateUpToInSystem(new Date());
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setDateUpToInSystem(new Date(24));
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setDenomination(p.getDenomination());
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setFax("d");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setFax(p.getFax());
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setForeName(p.getForeName());
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setGender(p.getGender());
     assertThat(this.participant).isNotEqualTo(p);
 
     p = new Participant(this.participant);
     p.setLastName("l");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setLastName(p.getLastName());
     p.setStreet("s");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setStreet(p.getStreet());
     p.setMailAddress("mail-addy");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setMailAddress("mail");
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setMailAddress(this.participant.getMailAddress());
     p.setMobilePhone("mobile");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setMobilePhone("phone");
     assertThat(this.participant).isNotEqualTo(p);
     this.participant.setMobilePhone(p.getMobilePhone());
     p.setPhone("mobile");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setPhone("phone");
     assertThat(this.participant).isNotEqualTo(p);
     p.setPhone(this.participant.getPhone());
     p.setPhoneOfParents("pop");
     assertThat(this.participant).isNotEqualTo(p);
 
     this.participant.setPhoneOfParents("phone-of-parents");
     assertThat(this.participant).isNotEqualTo(p);
     p.setPhoneOfParents(this.participant.getPhoneOfParents());
 
     p.setPossibleAGE(!this.participant.isPossibleAGE());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleAGE(this.participant.isPossibleAGE());
     p.setPossibleBoard(!this.participant.isPossibleBoard());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleBoard(this.participant.isPossibleBoard());
     p.setPossibleExtendedBoard(!this.participant.isPossibleExtendedBoard());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleExtendedBoard(this.participant.isPossibleExtendedBoard());
     p.setPossibleKitchen(!this.participant.isPossibleKitchen());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleKitchen(this.participant.isPossibleKitchen());
     p.setPossibleMAK(!this.participant.isPossibleMAK());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleMAK(this.participant.isPossibleMAK());
     p.setPossibleMisc(!this.participant.isPossibleMisc());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleMisc(this.participant.isPossibleMisc());
     p.setPossibleParticipant(!this.participant.isPossibleParticipant());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleParticipant(this.participant.isPossibleParticipant());
     p.setPossibleSeminar(!this.participant.isPossibleSeminar());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleSeminar(this.participant.isPossibleSeminar());
     p.setPossibleStaff(!this.participant.isPossibleStaff());
     assertThat(this.participant).isNotEqualTo(p);
 
     p.setPossibleStaff(this.participant.isPossibleStaff());
     p.setPossibleStaffYouth(!this.participant.isPossibleStaffYouth());
     assertThat(this.participant).isNotEqualTo(p);
     p.setPossibleStaffYouth(this.participant.isPossibleStaffYouth());
 
     assertThat(this.participant).isEqualTo(p);
     p.setPostCodePostal(177);
     assertThat(this.participant).isNotEqualTo(p);
   }

  /**
   * Test method for {@link Participant#equals(Object)}.
   */
  @Test
  public final void testEquals_OtherDateNull() {
    final Participant p = new Participant(this.participant);

    p.setDateUpToInSystem(new Date(24));

    assertThat(p).isNotEqualTo(this.participant);
  }
 }
