 package puf.m2.hms.model;
 
 import java.util.Date;
 
 import org.junit.Test;
 
 public class PhysicianAssignmentTest {
     @Test
     public void testSave() throws HmsException {
         Patient patient = Patient.getPatientById(1);
        Physician physician = Physician.getDoctors().get(0);
 
         PhysicianAssignment pa = new PhysicianAssignment(patient, physician);
         pa.save();
 
     }
     
     @Test
     public void test123() {
     	
     }
 }
