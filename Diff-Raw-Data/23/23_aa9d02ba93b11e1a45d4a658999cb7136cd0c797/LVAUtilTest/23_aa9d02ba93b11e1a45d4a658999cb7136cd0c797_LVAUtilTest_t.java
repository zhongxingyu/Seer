 package at.ac.tuwien.sepm.service.impl;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.LvaDate;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.service.LvaType;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.TimeFrame;
 import at.ac.tuwien.sepm.service.impl.LVAUtil;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class LVAUtilTest {
 
 	@Before
 	public void setUp(){
 	}
     /**
     * tests, if the format methods return something else than an empty String
      */
     @Test
     public void testFormat(){
         MetaLVA metaLVA = new MetaLVA();
         metaLVA.setName("meta-LVA name");
         metaLVA.setNr("123.123");
         metaLVA.setPriority(5);
         metaLVA.setType(LvaType.VO);
         LVA lva1 = new LVA();
         lva1.setYear(2013);
         lva1.setSemester(Semester.S);
         LVA lva2 = new LVA();
         lva2.setSemester(Semester.W);
         lva2.setYear(2014);
 
         List<MetaLVA> metaLVAs = new ArrayList<MetaLVA>(2);
         metaLVAs.add(metaLVA);
         metaLVAs.add(metaLVA);
         List<LVA> lvas = new ArrayList<LVA>();
         lvas.add(lva1);
         lvas.add(lva2);
 
         metaLVA.setLVAs(lvas);
         assertTrue(metaLVA.toString().length() > 0);
         assertTrue(metaLVA.toShortString().length()>0);
         assertTrue(metaLVA.toDetailedString(1).length()>0);
         assertTrue(metaLVA.toShortDetailedString(1).length()>0);
 

        assertTrue(LVAUtil.formatMetaLVA(metaLVAs,1).length()>0);
        assertTrue(LVAUtil.formatDetailedMetaLVA(metaLVAs,1).length()>0);
        assertTrue(LVAUtil.formatShortDetailedMetaLVA(metaLVAs,1).length()>0);
        assertTrue(LVAUtil.formatShortMetaLVA(metaLVAs,1).length()>0);

        assertTrue(LVAUtil.formatLVA(lvas,1).length()>0);
        assertTrue(LVAUtil.formatShortLVA(lvas,1).length()>0);
     }
 
     /**
      * tests if the hasNext() of the Iterator works as expected.
      */
 	@Test
 	public void testIteratorHasNext(){
 		LVA lva = new LVA();
 		assertFalse(LVAUtil.iterator(lva).hasNext());
 		ArrayList<LvaDate> times = new ArrayList<LvaDate>();
 		
         LvaDate date = new LvaDate();
         date.setTime(new TimeFrame(new DateTime("2000-01-04"),new DateTime("2000-01-06")));
         times.add(date);
         lva.setLectures(times);
         assertTrue(LVAUtil.iterator(lva).hasNext());
         assertFalse(LVAUtil.iterator(lva,LVAUtil.EXAM_TIMES).hasNext());
     }
 
     /**
      * tests the iterator on the examTimes
      */
 	@Test
 	public void testIteratorExamTimes(){
 		LVA lva = new LVA();
 		ArrayList<LvaDate> dates1 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates2 = new ArrayList<LvaDate>();
 
         LvaDate date1 = new LvaDate();
         date1.setTime(new TimeFrame(new DateTime("2000-01-04"),new DateTime("2000-01-06")));
 		dates1.add(date1);
         LvaDate date2 = new LvaDate();
         date2.setTime(new TimeFrame(new DateTime("2000-01-25"),new DateTime("2000-01-26")));
         dates2.add(date2);
         
         lva.setLectures(dates1);
         lva.setExercises(dates2);
         Iterator<LvaDate> iter1 = LVAUtil.iterator(lva,LVAUtil.EXAM_TIMES);
         Iterator<LvaDate> iter2 = LVAUtil.iterator(lva,LVAUtil.EXERCISES_TIMES);
         assertFalse(iter1.hasNext());
         assertTrue(iter2.hasNext());
         assertTrue(iter2.next()==date2);
         assertFalse(iter2.hasNext());
     }
 
     /**
      * test if the Iterator works correctly on all times
      */
 	@Test
 	public void testIteratorAllTimes(){
 		LVA lva = new LVA();
 		ArrayList<LvaDate> dates1 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates2 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates3 = new ArrayList<LvaDate>();
 
 
         LvaDate date0 = new LvaDate();
         date0.setTime(new TimeFrame(new DateTime("2000-01-04"),new DateTime("2000-01-06")));
 		dates1.add(date0);
 
         LvaDate date1 = new LvaDate();
         date1.setTime(new TimeFrame(new DateTime("2000-01-25"),new DateTime("2000-01-26")));
 		dates1.add(date1);
 
 		LvaDate date2 = new LvaDate();
         date2.setTime(new TimeFrame(new DateTime("2000-01-01"),new DateTime("2000-01-05")));
 		dates2.add(date2);
         LvaDate date3 = new LvaDate();
         date3.setTime(new TimeFrame(new DateTime("2000-01-10"),new DateTime("2000-01-13")));
 		dates2.add(date3);
 
         LvaDate date4 = new LvaDate();
         date4.setTime(new TimeFrame(new DateTime("2000-01-07"),new DateTime("2000-01-16")));
 		dates3.add(date4);
         LvaDate date5 = new LvaDate();
         date5.setTime(new TimeFrame(new DateTime("2000-01-20"),new DateTime("2000-01-21")));
 		dates3.add(date5);
 		
 		lva.setLectures(dates1);
 		lva.setExercises(dates2);
 		lva.setExams(dates3);
 		Iterator<LvaDate> iter = LVAUtil.iterator(lva);
 		assertTrue(iter.hasNext());
 		assertTrue(iter.next()==date2);
 		assertTrue(iter.hasNext());
 		assertTrue(iter.next()==date0);
 		assertTrue(iter.hasNext());
 		assertTrue(iter.next()==date4);
 		assertTrue(iter.hasNext());
 		assertTrue(iter.next()==date3);
 		assertTrue(iter.hasNext());
 		assertTrue(iter.next()==date5);
 		assertTrue(iter.hasNext());
 		assertTrue(iter.next()==date1);
 		assertFalse(iter.hasNext());
 	}
 	/**
 	 * tests, if the intersectAllTypes() method works
 	 */
 	@Test
 	public void testIntersectLVAs() {
 		LVA lva1 = new LVA();
 		LVA lva2 = new LVA();
 		LVA lva3 = new LVA();
 		LVA lva4 = new LVA();
 		
 		ArrayList<LvaDate> dates1 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates2 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates3 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates4 = new ArrayList<LvaDate>();
 		ArrayList<LvaDate> dates5 = new ArrayList<LvaDate>();
 
 		dates1.add(new LvaDate(new TimeFrame(new DateTime("2000-01-01"),new DateTime("2000-01-03"))));
 		dates1.add(new LvaDate(new TimeFrame(new DateTime("2000-01-07"),new DateTime("2000-01-09"))));
 		dates1.add(new LvaDate(new TimeFrame(new DateTime("2000-01-14"),new DateTime("2000-01-15"))));
 		
 		dates2.add(new LvaDate(new TimeFrame(new DateTime("2000-01-04"),new DateTime("2000-01-06"))));
 		dates2.add(new LvaDate(new TimeFrame(new DateTime("2000-01-10"),new DateTime("2000-01-13"))));
 		
 		dates3.add(new LvaDate(new TimeFrame(new DateTime("2000-01-14"),new DateTime("2000-01-16"))));
 		dates3.add(new LvaDate(new TimeFrame(new DateTime("2000-01-20"),new DateTime("2000-01-21"))));
 		
 		dates4.add(new LvaDate(new TimeFrame(new DateTime("2000-02-01"),new DateTime("2000-02-03"))));
 		dates4.add(new LvaDate(new TimeFrame(new DateTime("2000-02-07"),new DateTime("2000-02-09"))));
 		
 		dates5.add(new LvaDate(new TimeFrame(new DateTime("2000-02-04"),new DateTime("2000-02-06"))));
 		
 		lva1.setLectures(dates1);
 		lva1.setExercises(dates4);
 		
 		lva2.setExercises(dates5);
 		lva2.setLectures(dates2);
 		
 		lva3.setLectures(dates3);
 		lva3.setExams(dates5);
 		
 		lva4.setExams(dates1);
 		
 		assertFalse(LVAUtil.intersectAllTypes(lva1, lva2));
 		assertFalse(LVAUtil.intersectAllTypes(lva2, lva1));
 		
 		assertTrue(LVAUtil.intersectAllTypes(lva1, lva3));
 		assertTrue(LVAUtil.intersectAllTypes(lva3, lva1));
 
         assertFalse(LVAUtil.intersect(lva1,lva2,LVAUtil.EXERCISES_TIMES));
         assertFalse(LVAUtil.intersect(lva2,lva1,LVAUtil.EXERCISES_TIMES));
 
         assertFalse(LVAUtil.intersect(lva3,lva4,LVAUtil.EXAM_TIMES));
         assertFalse(LVAUtil.intersect(lva4,lva3,LVAUtil.EXAM_TIMES));
     }
 
 }
