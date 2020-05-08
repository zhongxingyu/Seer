 package net.anei.cadpage.donation;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 public class JulianDateTest {
   
   @Test
   public void testCalendar() {
     doCalTest(1,10,2012);
     doCalTest(12,1,2012);
   }
   
   private static void doCalTest(int m1, int d1, int y1) {
     Calendar t1 = new GregorianCalendar(y1,m1-1,d1);
     assertEquals(y1, t1.get(Calendar.YEAR));
     assertEquals(m1, t1.get(Calendar.MONTH)+1);
     assertEquals(d1, t1.get(Calendar.DAY_OF_MONTH));
   }
   
   @Test
   public void testDaysInYear() {
     assertEquals(365, JulianDate.daysInYear(2011));
     assertEquals(366, JulianDate.daysInYear(2012));
     assertEquals(365, JulianDate.daysInYear(2013));
   }
   
   @Test
   public void testJulianDate() {
     doTest("problem1", 1,10,2012, 12,1,2012);
     doTest("problem2", 12,1,2012, 1,10,2012);
     doTest("Same day", 5,1,2011, 5,1,2011);
     doTest("Same month", 5,1,2011, 5,20,2011);
     doTest("Same year", 4,10,2011, 5,21,2011);
     doTest("Cross year", 12,1,2010, 1,10,2011);
     doTest("Cross leap year", 12,1,2012, 1,10,2013);
     doTest("Multiyear", 12,31,2010, 1,1,2020);
     
   }
   
   private static void doTest(String title, int m1, int d1, int y1, int m2, int d2, int y2) {
     Calendar t1 = new GregorianCalendar(y1,m1-1,d1);
     Calendar t2 = new GregorianCalendar(y2,m2-1,d2);
     JulianDate jdt1 = new JulianDate(t1.getTime());
     JulianDate jdt2 = new JulianDate(t2.getTime());
     int diff1 = jdt1.diffDays(jdt2);
     int diff2 = jdt2.diffDays(jdt1);
     assertEquals(title+":rev check", diff1, -diff2);
     
    JulianDate jdt1A = new JulianDate(jdt2, diff2);
    JulianDate jdt2A = new JulianDate(jdt1, diff1);
    assertEquals(title+":add1 check", jdt2, jdt2A);
    assertEquals(title+":add2 check", jdt1, jdt1A);
    
     t1.add(Calendar.DAY_OF_YEAR, diff1);
     assertEquals(title+":main check", t1.getTime().getTime(), t2.getTime().getTime());
   }
 
 }
