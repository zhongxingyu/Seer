 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJDeepwaterParserTest extends BaseParserTest {
   
   public NJDeepwaterParserTest() {
     setParser(new NJDeepwaterParser(), "DEEPWATER", "NJ");
   }
   
   @Test
   public void testBadMsg() {
     doBadTest("(Chief ALT) [CFC EMS] -- Syncope - Not Alert -- Amberwood -- 51 Karens Wy - Cross STS:Beechwood Ct & Silver Birch La - EMD:M31D3");  
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Chief ALT) [STA:7] -- ALS -- 118 E Pittsfield St Pennsville Township NJ - Cross STS:Sparks Ave -- Caller: DAULTON ",
         "SRC:STA:7",
        "CALL:-- ALS",
         "ADDR:118 E Pittsfield St",
         "CITY:Pennsville Township",
         "X:Sparks Ave",
         "NAME:DAULTON");
 
     doTest("T2",
         "(Chief ALT) [STA:7] -- Motor Vehicle Accident -- 1163 S Broadway Way Pennsville Township NJ - Cross STS:Freas Rd -- Caller: JACK KUGLER 9352515",
         "SRC:STA:7",
        "CALL:-- Motor Vehicle Accident",
         "ADDR:1163 S Broadway Way",
         "CITY:Pennsville Township",
         "X:Freas Rd",
         "NAME:JACK KUGLER",
         "PHONE:9352515");
 
     doTest("T3",
         "(Chief ALT) [STA:7] -- MVC -- Njtp South Mm 1-4.5 Njtp Carneys Point Township NJ -- Caller: SHAWN HAMMELL 6178168",
         "SRC:STA:7",
        "CALL:-- MVC",
         "ADDR:Njtp South Mm 1-4.5 Njtp",
         "CITY:Carneys Point Township",
         "NAME:SHAWN HAMMELL",
         "PHONE:6178168");
 
     doTest("T4",
         "(Chief ALT) [STA:7] -- Fire Alarm System -- 117 N Broadway Way Pennsville Township NJ - Cross STS:Brandriff Ave -- Caller: VERIZON / KAREN 6158005",
         "SRC:STA:7",
        "CALL:-- Fire Alarm System",
         "ADDR:117 N Broadway Way",
         "CITY:Pennsville Township",
         "X:Brandriff Ave",
         "NAME:VERIZON / KAREN",
         "PHONE:6158005");
 
     doTest("T5",
         "(Chief ALT) [STA:7] -- Out Of County Assist -- Delaware Memorial Brg New Castle DE -- Caller: COUNTY",
         "SRC:STA:7",
        "CALL:-- Out Of County Assist",
         "ADDR:Delaware Memorial Brg",
         "CITY:New Castle",
         "ST:DE",
         "NAME:COUNTY");
   }
   
   public static void main(String[] args) {
     new NJDeepwaterParserTest().generateTests("T1");
   }
 }
