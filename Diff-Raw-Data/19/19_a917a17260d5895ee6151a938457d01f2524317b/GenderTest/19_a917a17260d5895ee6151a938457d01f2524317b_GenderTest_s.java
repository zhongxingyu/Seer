 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import junit.framework.TestCase;
 
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Saurabh Agrawal
  */
 public class GenderTest extends TestCase {
 
     public void testDislayName() {
         assertEquals("Male", Gender.MALE.getDisplayName());
         assertEquals("Female", Gender.FEMALE.getDisplayName());
         assertEquals("Not Reported", Gender.NOT_REPORTED.getDisplayName());
         assertEquals("Unknown", Gender.UNKNOWN.getDisplayName());
     }
 
     public void testCodes() throws Exception {
         assertEquals("Male", Gender.MALE.getCode());
         assertEquals("Female", Gender.FEMALE.getCode());
         assertEquals("Not Reported", Gender.NOT_REPORTED.getCode());
         assertEquals("Unknown", Gender.UNKNOWN.getCode());
     }
 
     public void testGetByCode() throws Exception {
         assertSame(Gender.MALE, Gender.getByCode("Male"));
         assertSame(Gender.FEMALE, Gender.getByCode("Female"));
         assertSame(Gender.NOT_REPORTED, Gender.getByCode("Not Reported"));
         assertSame(Gender.UNKNOWN, Gender.getByCode("Unknown"));
     }
 
     public void testGetGenderMap() {
         assertEquals(4, Gender.values().length);
 
         Map<String, String> genders = Gender.getGenderMap();
         Set<String> keys = genders.keySet();
         assertEquals(4, keys.size());
         assertTrue(keys.contains("Male"));
         assertTrue(keys.contains("Female"));
         assertTrue(keys.contains("Not Reported"));
         assertTrue(keys.contains("Unknown"));
     }
 
     public void testGetByVariationsOfMale() throws Exception {
         assertSame(Gender.MALE, Gender.getByCode("Male"));
         assertSame(Gender.MALE,Gender.getByCode("M"));
         assertSame(Gender.MALE,Gender.getByCode("male"));
         assertSame(Gender.MALE, Gender.getByCode("MALE"));
     }
 
     public void testGetByVariationsOfFemale() throws Exception {
         assertSame(Gender.FEMALE, Gender.getByCode("Female"));
         assertSame(Gender.FEMALE,Gender.getByCode("F"));
         assertSame(Gender.FEMALE,Gender.getByCode("female"));
         assertSame(Gender.FEMALE,Gender.getByCode("FEMALE"));
     }
 
     public void testWrongGender() throws Exception {
         try {
             Gender.getByCode("MOLE");
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             assertEquals("The specified gender 'MOLE' is invalid: Please check the spelling.", scve.getMessage());
         }
     }
 }
