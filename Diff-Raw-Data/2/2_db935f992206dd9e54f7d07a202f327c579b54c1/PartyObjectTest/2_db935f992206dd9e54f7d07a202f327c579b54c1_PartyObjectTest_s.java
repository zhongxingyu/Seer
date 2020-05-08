 /* PartyObjectTest.java - created on Sep 30, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.party;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.theeuropeanlibrary.model.common.Identifier;
 import org.theeuropeanlibrary.model.common.qualifier.PartyIdentifierType;
 import org.theeuropeanlibrary.model.common.spatial.SpatialEntity;
 import org.theeuropeanlibrary.model.common.subject.Subject;
 import org.theeuropeanlibrary.model.common.time.Instant;
 import org.theeuropeanlibrary.model.common.time.Period;
 
 /**
  * 
  * 
  * @author Andreas Juffinger (andreas.juffinger@kb.nl)
  * @since Sep 30, 2011
  */
 public class PartyObjectTest {
 
     /**
      * 
      */
     @Test
     public void testPartySimple() {
         Party party = new Party();
         assertNull(party.getPartyName());
         
         party = new Party("test");
         assertNotNull(party.getPartyName());
         assertEquals("test", party.getPartyName());
                 
         party = new Party("main");
         assertNotNull(party.getPartyName());
         assertEquals("main", party.getPartyName());
         
         assertEquals(new Party("main"), party);
         assertEquals(new Party("main").hashCode(), party.hashCode());
         
         party.setLocation(new SpatialEntity());
         assertFalse(new Party("main").equals(party));
         
         party.setSubject(new Subject());
         assertFalse(new Party("main").equals(party));
 
         Party party2 = new Party("main");
         party2.setLocation(new SpatialEntity());
         party2.setSubject(new Subject());
                 
         assertEquals(party, party2);
     }
     
 
     /**
      * Tests the convertion of Party
      * 
      * @throws IOException
      */
     @Test
     public void testParty() throws IOException {
         Party enc = new Party("name");
         for (int i = 0; i < PartyIdentifierType.values().length; i++) {
             enc.getIdentifiers().add(
                     new Identifier("" + i, PartyIdentifierType.values()[i].toString()));
         }
 
         Assert.assertEquals("name", enc.getPartyName());
         Assert.assertEquals(PartyIdentifierType.values().length, enc.getIdentifiers().size());
         for (int i = 0; i < enc.getIdentifiers().size(); i++) {
             Assert.assertEquals("" + i, enc.getIdentifiers().get(i).getIdentifier());
             Assert.assertEquals(PartyIdentifierType.values()[i],
                     PartyIdentifierType.valueOf(enc.getIdentifiers().get(i).getScope()));
         }
     }
 
     
     /**
      * 
      */
     @Test
     public void testFamilySimple() {
         Family party = new Family();
         assertNull(party.getPartyName());
         
         party = new Family("test");
         assertNotNull(party.getPartyName());
         assertEquals("test", party.getPartyName());
                 
         party = new Family("main");
         assertNotNull(party.getPartyName());
         assertEquals("main", party.getPartyName());
         
         assertEquals(new Family("main"), party);
         assertEquals(new Family("main").hashCode(), party.hashCode());
         
         party.setLocation(new SpatialEntity());
         assertFalse(new Family("main").equals(party));
         
         party.setSubject(new Subject());
         assertFalse(new Family("main").equals(party));
 
         Family party2 = new Family("main");
         party2.setLocation(new SpatialEntity());
         party2.setSubject(new Subject());
                 
         assertEquals(party, party2);
     }
     
     
 
     /**
      * Tests the conversion of Family
      * 
      * @throws IOException
      */
     @Test
     public void testFamily() throws IOException {
         Family enc = new Family("Surname");
         for (int i = 0; i < PartyIdentifierType.values().length; i++) {
             enc.getIdentifiers().add(
                     new Identifier("" + i, PartyIdentifierType.values()[i].toString()));
         }
 
         Assert.assertEquals("Surname", enc.getPartyName());
         Assert.assertEquals(PartyIdentifierType.values().length, enc.getIdentifiers().size());
         for (int i = 0; i < enc.getIdentifiers().size(); i++) {
             Assert.assertEquals("" + i, enc.getIdentifiers().get(i).getIdentifier());
             Assert.assertEquals(PartyIdentifierType.values()[i],
                     PartyIdentifierType.valueOf(enc.getIdentifiers().get(i).getScope()));
         }
     }
 
     
     
     /**
      * 
      */
     @Test
     public void testOrganizationSimple() {
         Organization title = new Organization();
         assertNull(title.getPartyName());
         assertNull(title.getSubdivision());
         assertNull(title.getDisplay());
         
         title = new Organization("test");
         assertNotNull(title.getPartyName());
         assertEquals("test", title.getPartyName());
         assertNull(title.getSubdivision());
         assertNotNull(title.getDisplay());
         assertEquals("test", title.getDisplay());
                 
         title = new Organization("main", "sub");
         assertNotNull(title.getPartyName());
         assertEquals("main", title.getPartyName());
         assertNotNull(title.getSubdivision());
         assertEquals("sub", title.getSubdivision());
         assertNotNull(title.getDisplay());
         assertEquals("main, sub", title.getDisplay());
         
         assertEquals(new Organization("main", "sub"), title);
         assertEquals(new Organization("main", "sub").hashCode(), title.hashCode());
     }
 
     /**
      * Tests the conversion of Organization
      * 
      * @throws IOException
      */
     @Test
     public void testOrganization() throws IOException {
         Organization enc = new Organization("Surname");
         for (int i = 0; i < PartyIdentifierType.values().length; i++) {
             enc.getIdentifiers().add(
                     new Identifier("" + i, PartyIdentifierType.values()[i].toString()));
         }
 
         Assert.assertEquals("Surname", enc.getPartyName());
         Assert.assertEquals(PartyIdentifierType.values().length, enc.getIdentifiers().size());
         for (int i = 0; i < enc.getIdentifiers().size(); i++) {
             Assert.assertEquals("" + i, enc.getIdentifiers().get(i).getIdentifier());
             Assert.assertEquals(PartyIdentifierType.values()[i],
                     PartyIdentifierType.valueOf(enc.getIdentifiers().get(i).getScope()));
         }
     }
 
     
     /**
      * 
      */
     @Test
     public void testMeetingSimple() {
         Meeting title = new Meeting();
         assertNull(title.getPartyName());
         assertNull(title.getSubdivision());
         assertNull(title.getDisplay());
         
         title = new Meeting("test");
         assertNotNull(title.getPartyName());
         assertEquals("test", title.getPartyName());
         assertNull(title.getSubdivision());
         assertNotNull(title.getDisplay());
         assertEquals("test", title.getDisplay());
                 
         title = new Meeting("main", new Instant(1234));
         assertNotNull(title.getPartyName());
         assertEquals("main", title.getPartyName());
         
         assertEquals(new Meeting("main", new Instant(1234)), title);
         assertEquals(new Meeting("main", new Instant(1234)).hashCode(), title.hashCode());
     }
     
 
 
     /**
      * Tests the conversion of Meeting
      * 
      * @throws IOException
      */
     @Test
     public void testMeeting() throws IOException {
         Meeting enc = new Meeting("Surname");
         enc.setDate(new Instant(2010));
         for (int i = 0; i < PartyIdentifierType.values().length; i++) {
             enc.getIdentifiers().add(
                     new Identifier("" + i, PartyIdentifierType.values()[i].toString()));
         }
 
         SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
         Assert.assertEquals("Surname", enc.getPartyName());
         Assert.assertNotNull(enc.getDate());
         Assert.assertEquals("2010", simpleDateformat.format(enc.getDate().getTime()));
         Assert.assertEquals(PartyIdentifierType.values().length, enc.getIdentifiers().size());
         for (int i = 0; i < enc.getIdentifiers().size(); i++) {
             Assert.assertEquals("" + i, enc.getIdentifiers().get(i).getIdentifier());
             Assert.assertEquals(PartyIdentifierType.values()[i],
                     PartyIdentifierType.valueOf(enc.getIdentifiers().get(i).getScope()));
         }
     }
 
     
 
     /**
      * 
      */
     @Test
     public void testPersonSimple() {
         Person person = new Person();
         assertNull(person.getPartyName());
         assertNull(person.getFullName());
         
         person = new Person("test");
         assertNotNull(person.getPartyName());
         assertEquals("test", person.getPartyName());
         assertNotNull(person.getFullName());
         assertEquals("test", person.getFullName());
                 
         person = new Person("main", "sub");
         assertNotNull(person.getPartyName());
         assertEquals("sub main", person.getPartyName());
         assertNotNull(person.getSurname());
         assertEquals("sub", person.getFirstNames());
         assertNotNull(person.getFullName());
         assertEquals("sub main", person.getFullName());
        assertEquals("main sub", person.getFullNameInverted());
         assertEquals("sm", person.getNameInitials());
         
         assertEquals(new Person("main", "sub"), person);
         assertEquals(new Person("main", "sub").hashCode(), person.hashCode());
         
         
     }
     
 
     /**
      * Tests the conversion of Person
      * 
      * @throws IOException
      */
     @Test
     public void testPerson() throws IOException {
         Person enc = new Person("Surname");
         enc.setTitle("Sir");
         enc.setFirstNames("first name");
         enc.setNumerals("III");
         enc.setLifePeriod(new Period(new Instant(1900), new Instant(1975)));
         enc.setFlourishingPeriod(new Period(new Instant(1920), new Instant(1970)));
         for (int i = 0; i < PartyIdentifierType.values().length; i++) {
             enc.getIdentifiers().add(
                     new Identifier("" + i, PartyIdentifierType.values()[i].toString()));
         }
 
         Assert.assertEquals("Surname", enc.getPartyName());
         Assert.assertEquals("Sir", enc.getTitle());
         Assert.assertEquals("first name", enc.getFirstNames());
         Assert.assertEquals("III", enc.getNumerals());
 
         SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
         Assert.assertNotNull(enc.getLifePeriod());
         Assert.assertEquals("1900",
                 simpleDateformat.format(enc.getLifePeriod().getStart().getTime()));
         Assert.assertEquals("1975", simpleDateformat.format(enc.getLifePeriod().getEnd().getTime()));
         Assert.assertNotNull(enc.getFlourishingPeriod());
         Assert.assertEquals("1920",
                 simpleDateformat.format(enc.getFlourishingPeriod().getStart().getTime()));
         Assert.assertEquals("1970",
                 simpleDateformat.format(enc.getFlourishingPeriod().getEnd().getTime()));
 
         Assert.assertEquals(PartyIdentifierType.values().length, enc.getIdentifiers().size());
         for (int i = 0; i < enc.getIdentifiers().size(); i++) {
             Assert.assertEquals("" + i, enc.getIdentifiers().get(i).getIdentifier());
             Assert.assertEquals(PartyIdentifierType.values()[i],
                     PartyIdentifierType.valueOf(enc.getIdentifiers().get(i).getScope()));
         }
     }
 
     
 }
