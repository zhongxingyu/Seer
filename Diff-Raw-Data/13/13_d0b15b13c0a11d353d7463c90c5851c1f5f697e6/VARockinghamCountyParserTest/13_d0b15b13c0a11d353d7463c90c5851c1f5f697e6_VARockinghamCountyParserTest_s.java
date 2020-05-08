 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.VA.VARockinghamCountyParser;
 
 import org.junit.Test;
 
 
 public class VARockinghamCountyParserTest extends BaseParserTest {
   
   public VARockinghamCountyParserTest() {
     setParser(new VARockinghamCountyParser(), "ROCKINGHAM COUNTY", "VA");
   }
   
   @Test
   public void testParser() {
 //    
 //    doTest("TX", 
 //        "(Rescue 40) 100 BLK OF QUALITY STREET IN BWATER IS CLOSED REVIEW THE TRANSMITTED FAX FOR THE ROAD CLOSURE-ECC GROUP PAGED C90");
     
     doTest("T1",
         "R40 EMS-CARDIAC CONDITION 1751 MAIN AVE HAR CFS# 2010-082726 CROSS: GARBERS CHURCH RD/S HIGH ST",
         "SRC:R40",
         "CALL:EMS-CARDIAC CONDITION",
         "ADDR:1751 MAIN AVE",
         "CITY:HARRISONBURG",
         "ID:2010-082726",
         "X:GARBERS CHURCH RD/S HIGH ST");
 
     doTest("T2",
         "R40 EMS-MENTAL PROBLEM 445 N MAIN ST 44 HAR CFS# 2010-082451 CROSS: WOLFE ST/ROCK ST",
         "SRC:R40",
         "CALL:EMS-MENTAL PROBLEM",
         "ADDR:445 N MAIN ST 44",
         "CITY:HARRISONBURG",
         "ID:2010-082451",
         "X:WOLFE ST/ROCK ST");
     
     doTest("T3",
         "R40 EMS-ABDOMINAL PAIN 1737 MORELAND DR HAR CFS# 2010-083119 CROSS: PHEASANT RUN CIR/ASHFORD CT",
         "SRC:R40",
         "CALL:EMS-ABDOMINAL PAIN",
         "ADDR:1737 MORELAND DR",
         "CITY:HARRISONBURG",
         "ID:2010-083119",
         "X:PHEASANT RUN CIR/ASHFORD CT");
     
     doTest("T4",
         "R40 EMS-CHEST PAIN 235 LAYMAN ST 101 HAR CFS# 2010-083046 CROSS: N MAIN ST/LONGVIEW DR",
         "SRC:R40",
         "CALL:EMS-CHEST PAIN",
         "ADDR:235 LAYMAN ST 101",
         "CITY:HARRISONBURG",
         "ID:2010-083046",
         "X:N MAIN ST/LONGVIEW DR");
     
     doTest("T5",
         "R40 TRAFFIC CRASH 300 BOYERS RD BLK HAR CFS# 2010-082984 CROSS: MYSTIC WOODS LN/CULLISON CT",
         "SRC:R40",
         "CALL:TRAFFIC CRASH",
         "ADDR:300 BOYERS RD BLK",
         "MADDR:300 BOYERS RD",
         "CITY:HARRISONBURG",
         "ID:2010-082984",
         "X:MYSTIC WOODS LN/CULLISON CT");
         
     doTest("T6",
         "R40 TRAFFIC CRASH RESERVOIR ST & CANTRELL AV HAR CFS# 2010-082327",
         "SRC:R40",
         "CALL:TRAFFIC CRASH",
         "ADDR:RESERVOIR ST & CANTRELL AV",
         "MADDR:RESERVOIR ST & CANTRELL AVE",
         "CITY:HARRISONBURG",
         "ID:2010-082327");
     
     doTest("T7",
         "C30 EMS-TRAUMA INJURIES E SPOTSWOOD AVE & MORGAN AVE ELK CFS# 2010-094660",
         "SRC:C30",
         "CALL:EMS-TRAUMA INJURIES",
         "ADDR:E SPOTSWOOD AVE & MORGAN AVE",
         "CITY:ELKTON",
         "ID:2010-094660");
     
     doTest("T8",
         "C30 EMS-DIFFICULTY BREATHING 3240 THOROUGHFARE RD ELK CFS# 2010-094548 CROSS: WHISPERING WINDS TRL/EPPARD LN ",
         "SRC:C30",
         "CALL:EMS-DIFFICULTY BREATHING",
         "ADDR:3240 THOROUGHFARE RD",
         "CITY:ELKTON",
         "ID:2010-094548",
         "X:WHISPERING WINDS TRL/EPPARD LN");
 
     doTest("T9",
         "MAILBOX@hrecc.org Msg: C80 EMS-FALLS 9718 VALLEY VIEW RD MCG CFS# 2011-034525 CROSS: TREE SIDE LN/LONGLEY RD",
         "SRC:C80",
         "CALL:EMS-FALLS",
         "ADDR:9718 VALLEY VIEW RD",
         "CITY:MCGAHEYSVILLE",
         "ID:2011-034525",
         "X:TREE SIDE LN/LONGLEY RD");
 
     doTest("T10",
         "(Rescue 40) R40 EMS-DIFFICULTY BREATHING 1825 S MAIN ST HAR CFS# 2011-051783 CROSS: PLEASANT HILL RD/DUKES PLAZA",
         "SRC:R40",
         "CALL:EMS-DIFFICULTY BREATHING",
         "ADDR:1825 S MAIN ST",
         "CITY:HARRISONBURG",
         "ID:2011-051783",
         "X:PLEASANT HILL RD/DUKES PLAZA");
 
     doTest("T11",
         "C60 EMS-FALLS 273 CENTER ST TIM CFS# 2012-022275 CROSS: SHENANDOAH AVE/NEW MARKET RD",
         "SRC:C60",
         "CALL:EMS-FALLS",
         "ADDR:273 CENTER ST",
         "CITY:TIMBERVILLE",
         "ID:2012-022275",
         "X:SHENANDOAH AVE/NEW MARKET RD");
  }
   
   public static void main(String[] args) {
    new VARockinghamCountyParserTest().generateTests("T11");
   }
 }
