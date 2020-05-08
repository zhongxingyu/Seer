 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.parsers.CO.COAdamsCountyParser;
 import net.anei.cadpage.parsers.CO.COGreeleyParser;
 import net.anei.cadpage.parsers.CO.CONorthglennEMSParser;
 
 import org.junit.Test;
 
 
 public class GroupBestParserTest extends BaseParserTest {
   
   public GroupBestParserTest() {
     SmsMsgParser[] parsers = new SmsMsgParser[]{
         new COAdamsCountyParser(),
         new COGreeleyParser(),
         new CONorthglennEMSParser()
     };
     setParser(new GroupBestParser(parsers), "", "");
   }
   
   @Test
   public void testParser() {
     
     setDefaults("ADAMS COUNTY", "CO");
     doTest("ADAMS COUNTY",
         "Subject:IPS I/Page Notification E 64TH AVE/MONACO ST ADAM CCPD 09:51:48 TYPE CODE: ACCI CALLER NAME: TIME: 09:51:48 Comments: -104.90947",
         "ADDR:E 64TH AVE & MONACO ST",
         "CALL:ACCI");
 
     setDefaults("Greeley", "CO");
     doTest("Greeley",
         "20442,ALARMF,23691 CR 60H.E1 E4 L1,TEXT:AUDIBLE FROM GENERAL AND SMOKE DETECTOR \\COMP:1ST CLASS SECURITY \\PH:800 482 9800," ,
         "CALL:ALARMF",
         "ADDR:23691 CR 60H",
         "ID:20442",
         "UNIT:E1 E4 L1",
        "INFO:AUDIBLE FROM GENERAL AND SMOKE DETECTOR");
     
     setDefaults("ADAMS COUNTY", "CO");
     doTest("T1",
         "- part 1 of 1 / RC:Run# 10174/6211 OLIVE ST///Pregnancy / Childbirth/",
         "ID:10174",
         "ADDR:6211 OLIVE ST",
         "CALL:Pregnancy / Childbirth");
   }
 }
