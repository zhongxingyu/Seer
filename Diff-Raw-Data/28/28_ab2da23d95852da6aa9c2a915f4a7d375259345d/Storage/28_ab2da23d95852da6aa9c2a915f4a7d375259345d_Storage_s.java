 package ict4mpower;
 
import java.util.Iterator;
 import java.util.Set;
 import junit.framework.TestCase;
import models.AppInfo;
 import models.Measurement;
 import models.PatientInfo;
 import models.Prescription;
 import storage.ApplicationSocket;
 import storage.MeasurementRecordSocket;
 import storage.MedicalRecordSocket;
 
 public class Storage extends TestCase{
 	
 	
 	public void testApplicationRecortd(){
 		ApplicationSocket a = new ApplicationSocket();
		AppInfo ar = new AppInfo();
		String result = a.storeData("app", "appInfo", ar);
		assertNotNull("No id returned!", result);
 		
 	}
 	
 	public void testMedicalRecords(){
 		MedicalRecordSocket MRSocket = new MedicalRecordSocket();
		PatientInfo p1 = new PatientInfo("Test1", "12", "None");
		PatientInfo p2 = new PatientInfo("Test1", "12", "Allerigic");
 		Prescription p = new Prescription();
		PatientInfo p3 = new PatientInfo("Test2", "13", "Allergic");
 		
 		MRSocket.SignEntry(p1, "12", 1L, "app");
 		MRSocket.SignEntry(p, "12", 1L, "app");
 		MRSocket.SignEntry(p2, "12", 2L, "app");
 		MRSocket.SignEntry(p3, "15", 1L, "app");
 				
 		Set<Object> s = MRSocket.getEntriesForPatientId("12", "PatientInfo", "app");
 		
 		for(Object o : s){
 			PatientInfo pi = (PatientInfo) o;
 			assertEquals(o.getClass().getSimpleName(), p1.getClass().getSimpleName());
 			assertEquals(pi.getName(), p1.getName());
 			assertTrue(pi.getVisit() == 1l || pi.getVisit() == 2l);
 		}		
 		
 		s = MRSocket.getVisitEntries(1l, "12", "Prescription", "app");
 		for(Object o : s){
 			Prescription pi = (Prescription) o;
 			assertEquals(o.getClass().getSimpleName(), pi.getClass().getSimpleName());	
 		}			
 	}
 	
 	public void testMeasurmentRecords(){
 		MeasurementRecordSocket mSocket = new MeasurementRecordSocket();
 		mSocket.SignEntry("test", "m", 10.0, "111");
 		mSocket.SignEntry("test", "m", 11.5, "111");
 		mSocket.SignEntry("Water", "m", 12.0, "111");
 		mSocket.SignEntry("Water", "l", 100.0, "112");
 		Set<Measurement> s;
 		
 		s = mSocket.getMesurmentByType("test", "111");
 		
 		double value=0;
 		for(Measurement m : s){
 			assertTrue("value is " +value + " and got " +m.getValue(),value != m.getValue());
 			value = m.getValue();
 			assertTrue(m.getValue() == 10.0 || m.getValue() == 11.5);
 			assertEquals("111", m.getPatientId());
 			assertEquals("m", m.getUnit());
 		
 		}
 		s = mSocket.getMesurmentByType("Water", "111");
 		for(Measurement m : s){
 			assertTrue("value is " +value + " and got " +m.getValue(),value != m.getValue());
 			value = m.getValue();
 			assertTrue(m.getValue() == 100.0 || m.getValue() == 12.0);
 			assertEquals("111", m.getPatientId());
 			assertEquals("m", m.getUnit());
 		
 		}
 		
 		
 	}
 }
