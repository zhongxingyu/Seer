 package gov.usgs.gdp.bean;
 
 import static org.junit.Assert.*;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class THREDDSInfoBeanTest {
 	THREDDSInfoBean tIBean = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		this.tIBean = new THREDDSInfoBean();
 		this.tIBean.setDatasetGridTimes(new ArrayList<String>());
 		for (int yearCount = 1;yearCount < 12;yearCount++) {
 			for (int monthCount = 1; monthCount < 13;monthCount++) {
 				String year = Integer.toString(yearCount + 1900);
 				String month = "";
 				if (monthCount < 10) {
 					month = "0" + Integer.toString(monthCount);
 				} else {
 					month = Integer.toString(monthCount);
 				}
 				String instanceDate = year + "-" + month + "-15 00:00:00Z";
 				this.tIBean.getDatasetGridTimes().add(instanceDate);
 			}
 		}
 	}
 
 	
 	@Test
 	public final void testGetTHREDDSUrlMap() {
 		Map<String, String> result = THREDDSInfoBean.getTHREDDSUrlMap();
 		assertFalse(result.isEmpty());
		assertEquals(result.get("RUNOFF"),"http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
 	}
 	
 	@Test
 	public final void testGetFromYearFunction() {
 		int year = -1;
 		try {
 			year = this.tIBean.getFromYear();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(year < 0);
 	}
 	
 	@Test
 	public final void testGetToYearFunction() {
 		int year = -1;
 		try {
 			year = this.tIBean.getToYear();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(year < 0);
 		assertEquals(1911, year);
 	}
 	
 	@Test
 	public final void testGetFromMonthFunction() {
 		int month = -1;
 		try {
 			month = this.tIBean.getFromMonth();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(month < 0);
 	}
 	
 	@Test
 	public final void testGetToMonthFunction() {
 		int month = -1;
 		try {
 			month = this.tIBean.getToMonth();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(month < 0);
 		assertEquals(11, month);
 	}
 	
 	@Test
 	public final void testGetToDayFunction() {
 		int day = -1;
 		try {
 			day = this.tIBean.getToDay();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(day < 0);
 		assertEquals(15, day);
 	}
 	
 	@Test
 	public final void testGetFromDayFunction() {
 		int day = -1;
 		try {
 			day = this.tIBean.getFromDay();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 		assertFalse(day < 0);
 		assertEquals(15, day);
 	}
 	
 }
