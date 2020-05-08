 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.VA.VALouisaCountyParser;
 
 import org.junit.Test;
 
 
 public class VALouisaCountyParserTest extends BaseParserTest {
   
   public VALouisaCountyParserTest() {
     setParser(new VALouisaCountyParser(), "LOUISA COUNTY", "VA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Incident Notification) Call#: -1994 ; EMS-Extremity Injury ; 118 FREESTYLE LN ; ; Box 407 ; Info:  DOG ON SITE...  Closed APCO Case: -1994  APCO Narrative: Is the p",
         "ID:1994",
         "CALL:EMS-Extremity Injury",
         "ADDR:118 FREESTYLE LN",
         "BOX:407",
         "INFO:DOG ON SITE...");
 
     doTest("T2",
         "(Incident Notification) Call#: -2165 ; EMS-Chest Pain/Cardiac Problem ; 16247 JONES FARM RD ; ; Box 509 ; Info:  EMD/PAS  NO DRUGS USED  NOT FAINTED - HE IS WEAK AND",
         "ID:2165",
         "CALL:EMS-Chest Pain/Cardiac Problem",
         "ADDR:16247 JONES FARM RD",
         "BOX:509",
         "INFO:EMD/PAS  NO DRUGS USED  NOT FAINTED - HE IS WEAK AND");
 
     doTest("T3",
         "(Incident Notification) Call#: -2121 ; MVC-Injury ; 2752 BUMPASS RD ; ; Box 306 ; Info:  MEDIC2 CONTACT SPOTSY FOR CLOSER UNIT  CALLING SPOTSYLVANIA FOR ANOTHER EMS",
         "ID:2121",
         "CALL:MVC-Injury",
         "ADDR:2752 BUMPASS RD",
         "BOX:306",
         "INFO:MEDIC2 CONTACT SPOTSY FOR CLOSER UNIT  CALLING SPOTSYLVANIA FOR ANOTHER EMS");
 
     doTest("T4",
         "(Incident Notification) Call#: -1994 ; EMS-Extremity Injury ; 118 FREESTYLE LN ; ; Box 407 ; Info:  DOG ON SITE...  Closed APCO Case: -1994  APCO Narrative: Is the p",
         "ID:1994",
         "CALL:EMS-Extremity Injury",
         "ADDR:118 FREESTYLE LN",
         "BOX:407",
         "INFO:DOG ON SITE...");
 
     doTest("T5",
         "(Incident Notification) Call#: -1991 ; MVC-Injury ; JEFFERSON HWY ; ; Box 501 ; Info:  SPOKE TO VSP DISPATCH -- RESCUE NEEDED -- NO DEPUTY  CALLER WAS A PASSERBY WHO",
         "ID:1991",
         "CALL:MVC-Injury",
         "ADDR:JEFFERSON HWY",
         "BOX:501",
         "INFO:SPOKE TO VSP DISPATCH -- RESCUE NEEDED -- NO DEPUTY  CALLER WAS A PASSERBY WHO");
 
     doTest("T6",
         "(Incident Notification) Call#: -1979 ; EMS-Chest Pain/Cardiac Problem ; 3411 HOLLY GROVE DR ; ; Box 405 ; Info:  Opened APCO Case: -1979  E911 Info - Class of Servic",
         "ID:1979",
         "CALL:EMS-Chest Pain/Cardiac Problem",
         "ADDR:3411 HOLLY GROVE DR",
         "BOX:405");
 
     doTest("T7",
         "(Incident Notification) Call#: -1763 ; F-Alarm-Residential ; 7265 CROSS COUNTY RD ; ; Box 407 ; Info:  KEYPAD FIRE ALARM GAIL COOLEY TRIED TO CALL WENT TO  VM",
         "ID:1763",
         "CALL:F-Alarm-Residential",
         "ADDR:7265 CROSS COUNTY RD",
         "BOX:407",
         "INFO:KEYPAD FIRE ALARM GAIL COOLEY TRIED TO CALL WENT TO  VM");
 
     doTest("T8",
         "(Incident Notification) Call#: -1668 ; EMS-Pregnancy/Childbirth ; 298 WINDYKNIGHT RD CTOR ; ; Box 509 ; Info:  HAS NO IDEA IF SHE FEELS LIKE SHE WANTS TO PUSH  23 YO",
         "ID:1668",
         "CALL:EMS-Pregnancy/Childbirth",
         "ADDR:298 WINDYKNIGHT RD",
         "BOX:509",
         "INFO:HAS NO IDEA IF SHE FEELS LIKE SHE WANTS TO PUSH  23 YO");
 
     doTest("T9",
         "(Incident Notification) Call#: -1542 ; EMS-Seizure ; 342 SHELTON HILL RD ; ; Box 501 ; Info:  E911 Info - Class of Service: WPH2 Special Response Info: (540) 511-701",
         "ID:1542",
         "CALL:EMS-Seizure",
         "ADDR:342 SHELTON HILL RD",
         "BOX:501",
         "INFO:E911 Info - Class of Service: WPH2 Special Response Info: (540) 511-701");
 
     doTest("T10",
         "(Incident Notification) Call#: -2095 ; EMS-Seizure ; 172 DAVIS HWY ; DOLLAR GENERAL - MINERAL; Box 201 ; Info:  GREY VAN  3YOM SIEZING",
         "ID:2095",
         "CALL:EMS-Seizure",
         "ADDR:172 DAVIS HWY",
         "PLACE:DOLLAR GENERAL - MINERAL",
         "BOX:201",
         "INFO:GREY VAN  3YOM SIEZING");
 
     doTest("T11",
         "Subject:Incident Notification\nCall#: -3239 ; EMS-Trauma with Injury ; 17383 JEFFERSON HWY ; ; Box 509 ; Info: \r",
         "ID:3239",
         "CALL:EMS-Trauma with Injury",
         "ADDR:17383 JEFFERSON HWY",
         "BOX:509");
 
     doTest("T12",
         "(Incident Notification) Call#: -1991 ; MVC-Injury ; JEFFERSON HWY ; ; Box 501 ; Info:  SPOKE TO VSP DISPATCH -- RESCUE NEEDED -- NO DEPUTY  CALLER WAS A PASSERBY WHO",
         "ID:1991",
         "CALL:MVC-Injury",
         "ADDR:JEFFERSON HWY",
         "BOX:501",
         "INFO:SPOKE TO VSP DISPATCH -- RESCUE NEEDED -- NO DEPUTY  CALLER WAS A PASSERBY WHO");

    doTest("T13",
        "(Incident Notification) Call#: -3951 ; EMS-Chest Pain/Cardiac Problem ; 1503 BETHANY CHURCH RD ; ; ; Info:  NO HX OF HEART PROBLEMS  NO RECENT INJURY OR TRAUMA  AWAK",
        "ID:3951",
        "CALL:EMS-Chest Pain/Cardiac Problem",
        "ADDR:1503 BETHANY CHURCH RD",
        "INFO:NO HX OF HEART PROBLEMS  NO RECENT INJURY OR TRAUMA  AWAK");
    
   }
   
   public static void main(String[] args) {
     new VALouisaCountyParserTest().generateTests("T1");
   }
 }
