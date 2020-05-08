 package library.tests;
 
 import static org.junit.Assert.*;
 import java.util.List;
 import library.Time;
 import library.Section;
 import org.junit.Test;
 import library.DataHandler;
 import org.junit.Before;
 
 /**
  * // -------------------------------------------------------------------------
 /**
  *  This class is for testing the DataHandler Class and its methods
  *
  *  @author cmbuck
  *  @version Apr 4, 2012
  */
 public class DataHandlerTest
 {
 
     DataHandler dh;
     Section testSection;
 
     /**
      * Sets up for the tests
      */
     @Before
     public void setUp()
     {
         dh = new DataHandler();
         testSection = new Section();
     }
 
     /**
      * Tests the cutData method
      */
     @Test
     public void testCutData()
     {
        String data1 = "asdf\nasdf </HTML>Search Results\n 123";
        String data2 = "<HTML>asdf\n<b>asdf</b> </HTML>\n<p>123Search Results";
        String data3 = "<html>asdf\nasdf </html>Search Results\n<p>123";
         String data4 = "<html>asdf\nasdf \n<p>123</html>";
        String data5 = "Search Resultsasdf1234";
         assertEquals("\n 123", dh.cutData(data1));
        assertEquals("", dh.cutData(data2));
         assertEquals("\n<p>123", dh.cutData(data3));
         assertEquals("", dh.cutData(data4));
        assertEquals("asdf1234", dh.cutData(data5));
     }
 
     /**
      * Tests the removeTags() method
      */
     @Test
     public void testRemoveTags()
     {
         String data1 = "<b>this is bold</b>";
         String data2 = "<font style='asdfasdfasdf'>blah\n</font>";
         String data3 = "<super tag \n and \n more...>123";
 
        assertEquals("\nthis is bold\n", dh.removeTags(data1));
        assertEquals("\nblah\n\n", dh.removeTags(data2));
        assertEquals("\n123", dh.removeTags(data3));
     }
 
 
 
     /**
      * Tests the removeEmptyLines method
      */
     @Test
     public void testSplitAndRemoveEmptyLines()
     {
         //String data1 = "\nasdf\n";
 
         //assertEquals("asdf", dh.removeEmptyLines(data1));
 
         String data2 = "asdf123";
 
         String[] expected2 = {"asdf123"};
         String[] returned2 = dh.splitAndRemoveEmptyLines(data2);
 
         assertNotNull(returned2);
         assertEquals(expected2.length, returned2.length);
         assertEquals(expected2[0], returned2[0]);
 
         String data3 = "\n12\n42\nB Snyder\nMCB 113\n";
 
         String[] expected3 = {"12","42","B Snyder","MCB 113"};
         String[] returned3 = dh.splitAndRemoveEmptyLines(data3);
 
         assertNotNull(returned3);
         assertEquals(expected3.length, returned3.length);
         for (int i=0; i < expected3.length; i++)
         {
             assertEquals(expected3[i], returned3[i]);
         }
     }
 
     /**
      * Tests the removeGarbage() method
      */
     @Test
     public void testRemoveGarbage()
     {
         String data1 = "95564&nbsp";
         String data2 = "&nbsp;";
         String data3 = "asdf&nbsp\nL\n&nbsp;\n40";
 
         String expected1 = "95564";
         String expected2 = "";
         String expected3 = "asdf\nL\n\n40";
 
         String returned1 = dh.removeGarbage(data1);
         String returned2 = dh.removeGarbage(data2);
         String returned3 = dh.removeGarbage(data3);
 
         assertEquals(expected1, returned1);
         assertEquals(expected2, returned2);
         assertEquals(expected3, returned3);
     }
 
     /**
      * tests the nextCRN() method
      */
     @Test
     public void testNextCRN()
     {
         String data1[] = {  "Campus: Blacksburg - Term: Fall Semester 2012",
                             "CRN ?",
                             "Course",
                             "Title",
                             "Type ? ",
                             "Cr Hrs",
                             "Capacity",
                             "Instructor",
                             "Days ? ",
                             "Begin",
                             "End",
                             "Location ? ",
                             "Exam",
                             "92084",
                             "ECE-1574"};
 
         int expected1 = 13;
         int returned1 = dh.nextCRN(data1, 0);
 
         assertEquals(expected1, returned1);
         assertEquals(-1, dh.nextCRN(data1, 14));
 
     }
 
 
     /**
      * tests the parseData() method
      */
     @Test
     public void testParseData()
     {
         String data1[] = {"92084",
                           "ECE-1574",
                           "Engr Problem Solving with C++",
                           "L",
                           "3",
                           "50",
                           "Staff",
                           "M W     ",
                           "8:00AM",
                           "8:50AM",
                           "RAND 331",
                           "08M",
                           "* Additional Times *",
                           "T      ",
                           "5:00PM",
                           "6:50PM",
                           "RAND 331"};
 
         List<Section> returned1 = dh.parseData(data1);
         assertEquals(1, returned1.size());
     }
 
     /**
      * tests the parseCRN() method
      */
     @Test
     public void testParseCRN()
     {
         String data1[] = {"12345","22222"};
 
         assertEquals(0, testSection.getCrn());
         dh.parseCRN(data1, 0, testSection);
         assertEquals(12345, testSection.getCrn());
 
         dh.parseCRN(data1, 1, testSection);
         assertEquals(22222, testSection.getCrn());
 
     }
 
     /**
      * tests the parseCourse() method
      */
     @Test
     public void testParseCourse()
     {
 
     }
 
     /**
      * tests the parseTitle() method
      */
     @Test
     public void testParseTitle()
     {
 
     }
 
     /**
      * tests the parseType() method
      */
     @Test
     public void testParseType()
     {
 
     }
 
     /**
      * tests the parseHrs() method
      */
     @Test
     public void testParseHrs()
     {
 
     }
 
     /**
      * tests the parseCapacity() method
      */
     @Test
     public void testParseCapacity()
     {
 
     }
 
     /**
      * tests the parseInstructor() method
      */
     @Test
     public void testParseInstructor()
     {
 
     }
 
     /**
      * tests the parseDays() method
      */
     @Test
     public void testParseDays()
     {
 
     }
 
     /**
      * tests the parseBegin() method
      */
     @Test
     public void testParseBegin()
     {
         String str[] = {"2:20PM", "12:10PM", "9:05AM"};
         Time times[] = {new Time(14, 20), new Time(12, 10), new Time(9, 05)};
         assertNull(testSection.getBegin());
         dh.parseBegin(str, 0, testSection);
         assertEquals(times[0], testSection.getBegin());
 
         dh.parseBegin(str, 1, testSection);
         assertEquals(times[1], testSection.getBegin());
 
         dh.parseBegin(str, 2, testSection);
         assertEquals(times[2], testSection.getBegin());
     }
 
     /**
      * tests the parseEnd() method
      */
     @Test
     public void testParseEnd()
     {
         String str[] = {"2:20PM", "12:10PM", "9:05AM"};
         Time times[] = {new Time(14, 20), new Time(12, 10), new Time(9, 05)};
         assertNull(testSection.getEnd());
         dh.parseEnd(str, 0, testSection);
         assertEquals(times[0], testSection.getEnd());
 
         dh.parseEnd(str, 1, testSection);
         assertEquals(times[1], testSection.getEnd());
 
         dh.parseEnd(str, 2, testSection);
         assertEquals(times[2], testSection.getEnd());
     }
 
     /**
      * tests the parseLocation() method
      */
     @Test
     public void testParseLocation()
     {
 
     }
 
     /**
      * tests the parseExam() method
      */
     @Test
     public void testParseExam()
     {
 
     }
 }
