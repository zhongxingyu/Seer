 package net.anei.cadpage;
 
 import static org.junit.Assert.*;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import net.anei.cadpage.SmsMmsMessage;
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MsgInfo;
 import net.anei.cadpage.parsers.MsgParser;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class SmsMmsMessageTest extends BaseParserTest {
   
   private TestManagePreferences testPrefs;
   
   @Before
   public void classSetup() {
     testPrefs = new TestManagePreferences();
   }
   
   @Test
   public void testIncidentDateAmPm() {
     doTimeTest("full dt/AM time", "01/01/2012 13:23:10", "12/13/2011", "1:18:50 AM",
                "12/13/2011 01:18:50");
     doTimeTest("full dt/AM time", "01/01/2012 13:23:10", "12/13/2011", "1:18:50 PM",
                "12/13/2011 13:18:50");
   }
 
   @Test
   public void testEscape() {
     assertEquals("BIG RED ONE", SmsMmsMessage.escape("BIG RED ONE"));
     assertEquals("BIG RED ONE\\n\n\\tOVER THERE\\n\nSIX\\6sSPACES\\3s3", 
                  SmsMmsMessage.escape("BIG RED ONE\n\tOVER THERE\nSIX      SPACES   3"));
     assertEquals("ODD CHARS \\t\\b\\f\\r\\n\n", SmsMmsMessage.escape("ODD CHARS \t\b\f\r\n"));
     assertEquals("BIG\\u0001ONE\\u0005RED", SmsMmsMessage.escape("BIG\u0001ONE\u0005RED"));
   }
   
   @Test
   public void testIncidentDate() {
     doTimeTest("Nothing", "12/13/2011 13:23:10", "", "", "12/13/2011 13:23:10");
     doTimeTest("full dt/time", "01/01/2012 13:23:10", "12/13/2011", "23:18:50",
                                "12/13/2011 23:18:50");
     doTimeTest("miss yr/sec", "01/01/2012 13:23:10", "01/12", "23:18",
                               "01/12/2011 23:18:00");
     doTimeTest("miss yr roll", "01/01/2012 13:23:10", "12/13", "23:18:20",
                                "12/13/2011 23:18:20");
     doTimeTest("no date", "01/01/2012 23:18:00", "", "23:18:50",
                           "01/01/2012 23:18:50");
     doTimeTest("no date roll", "01/01/2012 10:50:00", "", "23:18:50",
                                "12/31/2011 23:18:50");
   }
   
   private static final DateFormat DATE_FMT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
   private static void doTimeTest(String title, String base, 
                                    final String date, final String time, 
                                    String expect) {
     long timestamp = 0;
     try {
       timestamp = DATE_FMT.parse(base).getTime();
     } catch (ParseException ex) {
       ex.printStackTrace();
       fail("Parse error");
     }
     
     SmsMmsMessage msg = new SmsMmsMessage("", "", "", timestamp, 0);
     MsgParser parser = new MsgParser("", ""){
       @Override
       protected boolean parseMsg(String strMessage, MsgInfo.Data data) {
         data.strDate = date;
         data.strTime = time;
         return true;
       }
     };
     assertTrue(parser.isPageMsg(msg.getParseInfo(), 0));
     Date rDate = msg.getIncidentDate();
     String result = (rDate == null ? null : DATE_FMT.format(rDate));
     assertEquals(title, expect, result);
   }
 
   // Test some parsing combinations that have given us trouble
   @Test
   public void testYorkCountyParser() {
     testPrefs.setTestLocation("PAYorkCounty");
     testPrefs.setTestOverrideFilter(false);
     testPrefs.setTestGenAlert(true);
     setDefaults("YORK COUNTY", "PA");
     setExpLocCode("PAYorkCountyB");
     
     doMsgTest("T1", "messaging@iamresponding.com", "",
         "Rip and Run Report\n" +
         "Rip and Run Report\n\n" +
         "~\n" +
         "Location Information:\n\n" +
         "Location:~~I~83~NB/~I~83X14LEADERHEIGHTS~NB\n" +
         "Venue:~YORK TWP\n" +
         "Cross Streets~\n" +
         "Quadrant:~19-833\n" +
         "District:~19-833\n" +
         "~\n" +
         "Call Information:\n\n" +
         "Call Number:~3936\n" +
         "Call Type:~ FIRE VEHICLE \n" +
         "Source:~DISPATCHED BY YORK COUNTY 911\n" +
         "Priority:~ 2\n" +
         "Status:~IN PROGRESS\n" +
         "Assigned To:~CALL TAKER 2\n" +
         "Caller:~Himmelright,~Christine\n" +
         "Call Date/Time:~02/26/2012 17:42:25\n" +
         "Dispatch Date/Time:~02/26/2012 17:44:07\n" +
         "Arrive Date/Time:~\n" +
         "Clear Date/Time:~\n" +
         "Enroute Date/Time:~\n" +
         "Latest Unit Cleared Date/Time:~\n" +
         "~\n" +
         "Incident Number(s)\n" +
         "2012-00001075~~GOODWILL FIRE COMPANY NO. 1 YORK TOWNSHIP\n" +
         "~\n" +
         "Units Sent:~1\n" +
         "FIRESTA19~\n\n" +
         "Alerts:\n" +
         "~\n" +
         "Location Alerts\n\n" +
         "Person Alerts\n\n" +
         "Vehicle Alerts\n" +
         "~~\n" +
         "Narrative:\n\n" +
         "Narrative Time~Narrative\n" +
         "02/26/12 17:43:52~In the medium of the road smoke and flames",
 
         "ADDR:I 83 NB & I 83X14LEADERHEIGHTS NB",
         "MADDR:I 83 & I 83X14LEADERHEIGHTS",
         "CITY:YORK TWP",
         "MAP:19-833",
         "ID:3936",
         "CALL:FIRE VEHICLE",
         "PRI:2",
         "NAME:Himmelright, Christine",
         "DATE:02/26/2012",
        "TIME:17:42:25",
         "UNIT:FIRESTA19",
         "INFO:In the medium of the road smoke and flames");
     
   }
   
   private void doMsgTest(String title, String from, String subject, String body, String... result) {
     SmsMmsMessage msg = new SmsMmsMessage(from, subject, body, 0, 0);
     assertTrue(title + ":parse", msg.isPageMsg());
     doMsgTest(title, true, msg.getParseInfo(), result);
   }
 }
