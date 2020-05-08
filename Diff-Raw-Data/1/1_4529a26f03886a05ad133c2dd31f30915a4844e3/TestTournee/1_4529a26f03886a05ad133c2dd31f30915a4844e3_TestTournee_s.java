 package util;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Test;
 
 import beans.PatientBean;
import beans.Personnal;
 import beans.Visite;
 import beans.VisiteStatus;
 
 /*
 public class TestTournee {
 	
 	
 	private PatientBean patient;
 	private PatientBean patient2;
 	
 	private Visite visite;
 	private Visite visite2;
 	
 	private Personnal personnal;
 	private Personnal personnal2;
 	
 	
 	protected void setUp(){
 		patient = new PatientBean();
 		patient.setName("TONG");
 		 
 		patient2 = new PatientBean();
 		patient2.setName("ROY");
 		
 		visite = new Visite();
 		visite.setStatus(VisiteStatus.TO_PLACE);
 		visite.setPatient(patient);
 		visite.setDuree(2);
 		visite.setPriority(1);
 
 		visite2 = new Visite();
 		visite2.setStatus(VisiteStatus.TO_PLACE);
 		visite2.setPatient(patient2);
 		visite2.setDuree(1);
 		visite2.setPriority(2);
 		
 		personnal = new Personnal();
 		personnal.setName("Tony");
 		Set<Integer> workDay = new HashSet<Integer>();
 		workDay.add(Calendar.WEDNESDAY);
 		personnal.setDaysOfWeek(workDay);
 		
 	}
 	
 	@Test
 	public void test2patients1personnal(){
 		
 
 		
 		patient.getVisiteList().add(visite);
 
 		patient2.getVisiteList().add(visite2);
 		
 		List<Visite> visiteList = new ArrayList<Visite>();
 		visiteList.add(visite);
 		visiteList.add(visite2);
 
 
 		
 		
 		List<Personnal> aideSoignantList = new ArrayList<Personnal>();
 		aideSoignantList.add(personnal);
 		
 		
 		TourneeTestMain.generationTournee(visiteList, aideSoignantList);
 		
 		
 		Date date1;
 		Date date2;
 		
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
 		cal.set(Calendar.HOUR_OF_DAY,8);
 		cal.set(Calendar.MINUTE,00);
 		cal.set(Calendar.SECOND,0);
 		cal.set(Calendar.MILLISECOND,0);
 
 		date1 = cal.getTime();
 
 		cal = Calendar.getInstance();
 		cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
 		cal.set(Calendar.HOUR_OF_DAY,10);
 		cal.set(Calendar.MINUTE,00);
 		cal.set(Calendar.SECOND,0);
 		cal.set(Calendar.MILLISECOND,0);
 
 		date2 = cal.getTime();
 		
 		
 		PlageHoraire ph = new PlageHoraire(date1, date2);
 		
 		assertEquals(aideSoignantList.get(0).getVisitList().get(0).getPlageHoraire(), ph);
 	}
 
 }*/
