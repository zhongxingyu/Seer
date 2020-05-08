 package ro.igstan.fuzzy.test;
 
 import java.util.Calendar;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import ro.igstan.fuzzy.FuzzyDateFormatter;
 import ro.igstan.fuzzy.FuzzyDateMessages;
 import ro.igstan.fuzzy.FuzzyDateRomanianMessages;
 
 /**
  * @author Ionut G. Stan <ionut.g.stan@gmail.com>
  */
 
 public class FuzzyDateFormatterTest {
 
     private FuzzyDateMessages fuzzyMessages;
 
     @Before
     public void setUp() {
         fuzzyMessages = new FuzzyDateRomanianMessages();
     }
 
     @Test
     public void acum1Secundă() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 49);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 secundă", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Secunde() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 48);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 secunde", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum1Minut() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 49, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 minut", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Minute() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 48, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 minute", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum1Oră() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 22, 15, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 oră", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Ore() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 22, 14, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 ore", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum1Zi() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 21, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 zi", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Zile() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 20, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 zile", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum1Săptămână() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 15, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 săptămână", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Săptămâni() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.FEBRUARY, 8, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 săptămâni", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum1Lună() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2010, Calendar.JANUARY, 22, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 lună", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Luni() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2009, Calendar.DECEMBER, 22, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 luni", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum1An() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2009, Calendar.FEBRUARY, 22, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 1 an", fuzzyFormatter.timeAgo(before));
     }
 
     @Test
     public void acum2Ani() {
         Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
         Calendar before = makeCalendar(2008, Calendar.FEBRUARY, 22, 16, 50, 50);
 
         FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
         assertEquals("acum 2 ani", fuzzyFormatter.timeAgo(before));
     }
 
    @Test
    public void acum3Ani() {
        Calendar now = makeCalendar(2010, Calendar.FEBRUARY, 22, 16, 50, 50);
        Calendar before = makeCalendar(2007, Calendar.FEBRUARY, 22, 16, 50, 50);

        FuzzyDateFormatter fuzzyFormatter = new FuzzyDateFormatter(now, fuzzyMessages);
        assertEquals("acum 3 ani", fuzzyFormatter.timeAgo(before));
    }

     private Calendar makeCalendar(int year, int month, int day, int hour, int minute, int second) {
         Calendar calendar = Calendar.getInstance();
         calendar.set(year, month, day, hour, minute, second);
         return calendar;
     }
 }
