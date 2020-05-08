 package puf.m2.hms.model;
 
 import java.sql.SQLException;
 
 import org.junit.Test;
 import static junit.framework.Assert.*;
 
 public class PatientTest {
 
     @Test
     public void testAddPatient() {
         
     }
     
     @Test
     public void testUpdatePatient() {
         
     }
     
     @Test
     public void testLookingForPatient() {
         
     }
     
     @Test
     public void testCheckExistPatient() throws HmsException {
         assertTrue(Patient.checkExistPatient(1));
     }
     
     @Test
     public void testGetPatient() throws HmsException {
         assertNotNull(Patient.getPatientById(1));
     }
 }
