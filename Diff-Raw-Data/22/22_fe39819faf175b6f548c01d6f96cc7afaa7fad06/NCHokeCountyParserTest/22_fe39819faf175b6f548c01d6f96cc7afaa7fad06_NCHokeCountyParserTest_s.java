 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCHokeCountyParserTest extends BaseParserTest {
   
   public NCHokeCountyParserTest() {
     setParser(new NCHokeCountyParser(), "HOKE COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Hoke Co 911:795 TC JONES RD* * * * * **EMD* * * * * * * * 09/15/2011 14:36:26 : pos4 : mblack3325 ** EMD Case Entry Finished ** Chief Complaint Number: 19 Key Q",
         "ADDR:795 TC JONES RD",
         "CALL:EMD",
         "CH:pos4");
 
     doTest("T2",
         "Hoke Co 911:385 SUNSET LAKE RD* * * * * **EMD* * * * * * * * 09/19/2011 06:20:35 : pos2 : SCOOPER9403 Cross streets: 7100 ARABIA RD//DEAD END Cross streets: 205",
         "ADDR:385 SUNSET LAKE RD",
         "CALL:EMD",
         "CH:pos2",
         "X:7100 ARABIA RD//DEAD END");
 
     doTest("T3",
         "Hoke Co 911:6020 PHILLIPI CHURCH RD* * * * * **26 SICK PERS* * * * * * * * 09/18/2011 00:57:12 : pos1 : LGRADY7274 ** EMD Case Complete ** Key Questions: 46 yea",
         "ADDR:6020 PHILLIPI CHURCH RD",
         "CALL:26 SICK PERS",
         "CH:pos1");
 
     doTest("T4",
         "Hoke Co 911:6020 PHILLIPI CHURCH RD* * * * * **EMD* * * * * * * * 09/18/2011 00:56:49 : pos1 : LGRADY7274 ** EMD Recommended Dispatch ** Response Text: Alpha Di",
         "ADDR:6020 PHILLIPI CHURCH RD",
         "CALL:EMD",
         "CH:pos1");
 
     doTest("T5",
         "Hoke Co 911:216 ROLLING PINES CT* * * * * **01 ABD PAIN* * * * * * * * 09/17/2011 19:54:51 : pos2 : SCOOPER9403 205 10-8 09/17/2011 19:54:38 : pos2 : SCOOPER940",
         "ADDR:216 ROLLING PINES CT",
         "CALL:01 ABD PAIN",
         "CH:pos2");
 
     doTest("T6",
         "Hoke Co 911:216 ROLLING PINES CT* * * * * **EMD* * * * * * * * 09/17/2011 19:37:54 : pos1 : LGRADY7274 ** EMD Recommended Dispatch ** Response Text: Alpha Dispa",
         "ADDR:216 ROLLING PINES CT",
         "CALL:EMD",
         "CH:pos1");
 
     doTest("T7",
         "Hoke Co 911:265 BETTY`S TRL* * * * * **EMD* * * * * * * * 09/16/2011 19:14:15 : pos2 : SCOOPER9403 Cross streets: 580 EVERITT RD//DEAD END WILL BE IN THE VERY E",
         "ADDR:265 BETTY`S TRL",
         "CALL:EMD",
         "CH:pos2",
         "X:580 EVERITT RD//DEAD END WILL BE IN THE VERY E");
 
     doTest("T8",
         "Hoke Co 911:730 GALATIA CHURCH RD* * * * * **EMD* * * * * * * * 09/15/2011 13:51:27 : pos4 : mblack3325 Cross streets: 5365 ROCKFISH RD//CUMBERLAND CO LINE*",
         "ADDR:730 GALATIA CHURCH RD",
         "CALL:EMD",
         "CH:pos4",
        "X:5365 ROCKFISH RD//CUMBERLAND CO LINE*");
   }
 
   public static void main(String[] args) {
     new NCHokeCountyParserTest().generateTests("T1", "ADDR CALL CH X");
   }
 }
