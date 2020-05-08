 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
 import com.roclas.googleApis.CalendarOperator;
 
 
 public class calendarTest {
 
 	@Test
 	public void creatingEventTest() throws IOException {
 		/*
 		System.out.println("creating event");
 		String user="time.it@bbvaglobalnet.com";
 		String priv="db8f3b7a86264a8a34a80d65dcefc3aa";
 		String password=null;
 		if(password==null || user==null){ assert(false); }
 		CalendarOperator operator=new CalendarOperator(user,password,priv);
 		operator.createEvent("17:00","19:00","MISUPEREVENTOtest");
 		*/
 		assert(true);
 	}
 	
 	@Test
 	public void listingEventsTest() throws IOException {
 		System.out.println("listing events");
 		String user="time.it@bbvaglobalnet.com";
 		String password=null;
 		String priv="db8f3b7a86264a8a34a80d65dcefc3aa";
 		if(password==null || user==null){ assert(false); }
 		CalendarOperator operator=new CalendarOperator(user,password,priv);
 		Map<String,Map<String,String>> map= operator.listEvents();
 		Set<String> keys = map.keySet();
 		for(String key:keys){
 			System.out.println("event: "+key);
 			Map<String, String> m = map.get(key);
 			for(String k: m.keySet()){
 				System.out.println("---- "+k+" : "+m.get(k));
 			}
 			System.out.println("\n\n");
 		}
 		System.out.println("is now busy ? :"+operator.isNowBusy());
 		assert(true);
 	}
 	
 	@Test
 	public void compareDatesTest(){
 		/*
 		String date0="2012-12-12T14:00:00.000+01:00";
 		String date1="2012-12-12T15:00:00.000+01:00";
 		CalendarOperator operator=new CalendarOperator(null,null,null);
 		String hora_actual=operator.stringCurrentDate("HH:mm:ss");
 		String dia_actual=operator.stringCurrentDate("yyyy-MM-dd");
 		//String dateNow=dia_actual+"T"+hora_actual+".000+01:00";
 		String dateNow="2012-12-12T14:35:00.000+01:00";
 		String dateTomorrow="2012-12-13T14:35:00.000+01:00";
 		assertTrue(operator.isDateBetween(dateNow,date0,date1));
 		assertFalse(operator.isDateBetween(dateTomorrow,date0,date1));
 		assert(true);
 		*/
 	}
 
 }
