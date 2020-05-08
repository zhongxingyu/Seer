 package net.anei.cadpage.parsers.RI;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class RIWashingtonCountyParserTest extends BaseParserTest {
   
   public RIWashingtonCountyParserTest() {
     setParser(new RIWashingtonCountyParser(), "WASHINGTON COUNTY", "RI");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(CAD Notification) 2012/05/13 15:25:39\n" +
         "10 CEDAR HILLS DR\n" +
         "NO NAME\n" +
         "RICHMOND\n" +
         "EMERGENCY\n\n" +
         "Notes\n" +
         "71 FEMALE, FALL IN PAIN.  CALL FROM 303",
 
         "DATE:05/13/2012",
         "TIME:15:25:39",
         "ADDR:10 CEDAR HILLS DR",
         "CITY:RICHMOND",
         "CALL:EMERGENCY",
         "INFO:71 FEMALE, FALL IN PAIN.  CALL FROM 303");
 
     doTest("T2",
         "(CAD Notification) 2012/05/13 10:11:03\n825 MAIN ST APT C105\nNO NAME\nHOPKINTON\nEMERGENCY\n\nNotes\nMEDICAL ALARM, NO RESPONSE",
         "DATE:05/13/2012",
         "TIME:10:11:03",
         "ADDR:825 MAIN ST",
         "APT:C105",
         "CITY:HOPKINTON",
         "CALL:EMERGENCY",
         "INFO:MEDICAL ALARM, NO RESPONSE");
 
     doTest("T3",
         "(CAD Notification) 2012/05/13 04:41:24\n307 CHURCH ST A\nNO NAME\nRICHMOND\nEMERGENCY\n\nNotes\n86 Y/O F. FALL VICTIM",
         "DATE:05/13/2012",
         "TIME:04:41:24",
         "ADDR:307 CHURCH ST A",
         "CITY:RICHMOND",
         "CALL:EMERGENCY",
         "INFO:86 Y/O F. FALL VICTIM");
 
     doTest("T4",
         "(CAD Notification) 2012/05/13 02:27:03\n807 MAIN ST APT B5\nNO NAME\nHOPKINTON\nEMERGENCY\n\nNotes\n80 Y/O F. DEYDRATION AND WEAK",
         "DATE:05/13/2012",
         "TIME:02:27:03",
         "ADDR:807 MAIN ST",
         "APT:B5",
         "CITY:HOPKINTON",
         "CALL:EMERGENCY",
         "INFO:80 Y/O F. DEYDRATION AND WEAK");
 
     doTest("T5",
         "(CAD Notification) 2012/05/12 13:32:27\n" +
         "2 @95 N\n" +
         "@95 NORTH AT EXIT 2 (@95)\n" +
         "HOPKINTON\n" +
         "MVA\n\n" +
         "Assigned Units\n" +
         "AMB_313 RESP HOT\n\n" +
         "Notes\n" +
         "NORTH OF EXIT",
 
         "DATE:05/12/2012",
         "TIME:13:32:27",
         "ADDR:2 I95 N",
         "MADDR:2 I 95 N",
         "PLACE:I95 NORTH AT EXIT 2 (I95)",
         "CITY:HOPKINTON",
         "CALL:MVA",
         "UNIT:AMB_313 RESP HOT",
         "INFO:NORTH OF EXIT");
 
     doTest("T6",
         "(CAD Notification) 2012/05/12 01:41:45\n31 COLUMBIA HTS OVAL\nNO NAME\nCHARLESTOWN\nEMERGENCY\n\nNotes\nDISORIENTED MAN",
         "DATE:05/12/2012",
         "TIME:01:41:45",
         "ADDR:31 COLUMBIA HTS OVAL",
         "CITY:CHARLESTOWN",
         "CALL:EMERGENCY",
         "INFO:DISORIENTED MAN");
 
     doTest("T7",
         "(CAD Notification) 2012/05/12 16:26:30\n" +
         "54 NOOSENECK HILL RD\n" +
         "RI STATE POLICE (NOOSENECK HILL RD)\n" +
         "RICHMOND\n" +
         "EMERGENCY\n\n" +
         "Notes\n" +
         "EYE INJURY FROM PRE",
 
         "DATE:05/12/2012",
         "TIME:16:26:30",
         "ADDR:54 NOOSENECK HILL RD",
         "PLACE:RI STATE POLICE (NOOSENECK HILL RD)",
         "CITY:RICHMOND",
         "CALL:EMERGENCY",
         "INFO:EYE INJURY FROM PRE");
    
   }
   
   public static void main(String[] args) {
     new RIWashingtonCountyParserTest().generateTests("T1");
   }
 }
