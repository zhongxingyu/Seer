 package com.dev.campus.test;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.util.Log;
 
 import com.dev.campus.event.Event;
 import com.dev.campus.util.TimeExtractor;
 
 import junit.framework.TestCase;
 
 public class TimeExtractorTest extends TestCase {
 	
 	
 	public void testParseTime(){
 		String text = "Il y aura un certain truc lundi prochain à 12h";
 		List<String[]> l = new ArrayList<String[]>();
 		l = TimeExtractor.parseTime(text);	
 		assertEquals(1, l.size());
 		assertEquals(1, l.get(0).length);
 		assertEquals("12", l.get(0)[0]);
 		
 		String text2 = "Ce machin se déroulera finalement de 12h30 à 14h";
 		l = TimeExtractor.parseTime(text2);
 		
 		assertEquals(2, l.size());
 		assertEquals(2, l.get(0).length);
 		assertEquals(1, l.get(1).length);
 		assertEquals("12", l.get(0)[0]);
 		assertEquals("30", l.get(0)[1]);
 		assertEquals("14", l.get(1)[0]);
 		
 	}
 	
 	
 	public void testCreateDate(){
		String date = "Mon, 2 Jan 2013 12:35:04 +0000";
 		String format = "EEE, d MMM yyyy HH:mm:ss Z";
 		Date d = new Date();
 		try {
 			d = TimeExtractor.createDate(date, format);
 			assertEquals(12, d.getHours());
 			assertEquals(35, d.getMinutes());
 			assertEquals(04, d.getSeconds());
 			assertEquals(2, d.getDate());
 			assertEquals(0, d.getMonth());
 			assertEquals(113, d.getYear()); // 2013-1900
			assertEquals(0000, d.getTimezoneOffset());
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void testGetCorrectDate() {
 		String date = "Mon, 2 Mar 2013 00:00:00 +0100";
 		Event ev = new Event();
 		String detailEv = "Ce machin se déroulera finalement de 12h30 à 14h";
 		ev.setDetails(detailEv);
 		try{
 			TimeExtractor.getCorrectDate(date, ev);
 			assertEquals(12, ev.getStartDate().getHours());
 			assertEquals(30, ev.getStartDate().getMinutes());
 			Log.d("Tatiana", String.valueOf(ev.getEndDate().getHours()));
 			assertEquals(14, ev.getEndDate().getHours());
 			assertEquals(00, ev.getEndDate().getMinutes());			
 			
 		} catch (Exception e){
 			// TODO Auto-generated catch block
 						e.printStackTrace();
 		}
 	}
 
 	
 	public void testGetDateLabri(){
 		String src = "12:30-13:45";
 		Date start = new Date();
 		Date end = new Date();
 		
 		TimeExtractor.getDateLabri(src, start, end);
 		assertEquals(12, start.getHours());
 		assertEquals(30, start.getMinutes());
 		assertEquals(13, end.getHours());
 		assertEquals(45, end.getMinutes());
 	}
 	
 }
