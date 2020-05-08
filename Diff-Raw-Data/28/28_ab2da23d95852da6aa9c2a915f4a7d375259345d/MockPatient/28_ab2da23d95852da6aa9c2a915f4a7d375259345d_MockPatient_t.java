 package Mocks;
 
import java.util.Date;
 import java.util.List;
 
 import models.PatientInfo;
 
 
 public class MockPatient {
 	
 	public PatientInfo pi;
 	public List<String> visits;
 	
 	public MockPatient() {
		pi = new PatientInfo("Alvin Mattsson Kjellqvist", "1", "Allergic to peanuts", new Date());
 		visits.add("2010-03-01");
 		visits.add("2009-08-23");
 		visits.add("2010-07-05");
 		visits.add("2008-11-16");		
 	}	
 
 }
