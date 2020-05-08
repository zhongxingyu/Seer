 package net.anei.cadpage.parsers.IA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class IAPottawattamieCountyParserTest extends BaseParserTest {
   
   public IAPottawattamieCountyParserTest() {
     setParser(new IAPottawattamieCountyParser(), "POTTAWATTAMIE COUNTY", "IA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[1700]  17-11-339 11/23 08:32\n20595 MCPHERSON AV, POTT COUNTY\nFIRE - WEED GRASS FIRE\nXST: STATE ORCHARD RD//SUNNYDALE RD ",
         "SRC:1700",
         "ID:17-11-339",
         "DATE:11/23",
         "TIME:08:32",
         "ADDR:20595 MCPHERSON AV",
         "MADDR:20595 MCPHERSON AVE",
         "CALL:FIRE - WEED GRASS FIRE",
         "X:STATE ORCHARD RD//SUNNYDALE RD");
 
     doTest("T2",
         "[1700]  17-11-338 11/21 17:33\n3000 RISEN SON BV, POTT COUNTY\nEMS/FIRE - UNCONCIOUS/FAINTING\nXST: VIRGINIA HILLS RD//",
         "SRC:1700",
         "ID:17-11-338",
         "DATE:11/21",
         "TIME:17:33",
         "ADDR:3000 RISEN SON BV",
         "CALL:EMS/FIRE - UNCONCIOUS/FAINTING",
         "X:VIRGINIA HILLS RD//");
 
     doTest("T3",
         "[1700]  17-11-337 11/20 16:02\n18088 BENT TREE RIDGE, POTT COUNTY\nFIRE - AUTOMATIC FIRE DETECTION SIGNAL\nXST: THREE BRIDGE RD//DEAD END",
         "SRC:1700",
         "ID:17-11-337",
         "DATE:11/20",
         "TIME:16:02",
         "ADDR:18088 BENT TREE RIDGE",
         "CALL:FIRE - AUTOMATIC FIRE DETECTION SIGNAL",
         "X:THREE BRIDGE RD//DEAD END");
 
     doTest("T4",
         "[1700]  17-11-336 11/20 15:26\n19897 BEVERLY MANOR, POTT COUNTY\nSICK PERSON - GENERIC\nXST: LONGVIEW LP//DEAD END",
         "SRC:1700",
         "ID:17-11-336",
         "DATE:11/20",
         "TIME:15:26",
         "ADDR:19897 BEVERLY MANOR",
         "CALL:SICK PERSON - GENERIC",
         "X:LONGVIEW LP//DEAD END");
 
     doTest("T5",
         "[1700]  17-11-334 11/20 15:19\n19900 VIRGINIA HILLS RD, POTT COUNTY\nFALLS - GENERIC\nXST: CONCORD LP//",
         "SRC:1700",
         "ID:17-11-334",
         "DATE:11/20",
         "TIME:15:19",
         "ADDR:19900 VIRGINIA HILLS RD",
         "CALL:FALLS - GENERIC",
         "X:CONCORD LP//");
 
     doTest("T6",
         "[1700]  17-11-333 11/19 22:07\n1102 LONGVIEW LP, POTT COUNTY\nEMS/FIRE - BREATHING PROBLEMS\nXST: VALLEY VIEW DR//GREENVIEW RD",
         "SRC:1700",
         "ID:17-11-333",
         "DATE:11/19",
         "TIME:22:07",
         "ADDR:1102 LONGVIEW LP",
         "CALL:EMS/FIRE - BREATHING PROBLEMS",
         "X:VALLEY VIEW DR//GREENVIEW RD");
 
     doTest("T7",
         "[1700]  17-11-332 11/19 14:21\n14186 230 ST, POTT COUNTY\nPOLE - GENERIC\nXST: CHESTNUT RD//",
         "SRC:1700",
         "ID:17-11-332",
         "DATE:11/19",
         "TIME:14:21",
         "ADDR:14186 230 ST",
         "CALL:POLE - GENERIC",
         "X:CHESTNUT RD//");
 
     doTest("T8",
         "[1700]  17-11-331 11/19 11:10\n21940 VINEYARD HILLS RD, POTT COUNTY\nSICK PERSON - GENERIC\nXST: EDGEWOOD RD// \n",
         "SRC:1700",
         "ID:17-11-331",
         "DATE:11/19",
         "TIME:11:10",
         "ADDR:21940 VINEYARD HILLS RD",
         "CALL:SICK PERSON - GENERIC",
         "X:EDGEWOOD RD//");
 
     doTest("T9",
         "[1700]  17-12-004 01/06 14:32\n209 TIMBER DR, POTT COUNTY\nEMS/FIRE - OVERDOSE/POISIONING\nXST: MEADOW LN//",
         "SRC:1700",
         "ID:17-12-004",
         "DATE:01/06",
         "TIME:14:32",
         "ADDR:209 TIMBER DR",
         "CALL:EMS/FIRE - OVERDOSE/POISIONING",
         "X:MEADOW LN//");
 
     doTest("T10",
         "[1700]  17-12-002 /23-12-004 01/05 19:42\n23221 MCPHERSON AV, POTT COUNTY\nFIRE - RESIDENCE STRUCTURE FIRE\nXST: LOCHLAND RG//TURNBERRY RG",
         "SRC:1700",
         "ID:17-12-002 /23-12-004",
         "DATE:01/05",
         "TIME:19:42",
         "ADDR:23221 MCPHERSON AV",
         "MADDR:23221 MCPHERSON AVE",
         "CALL:FIRE - RESIDENCE STRUCTURE FIRE",
         "X:LOCHLAND RG//TURNBERRY RG");
 
     doTest("T11",
         "[1700]  17-12-001 01/05 05:23\n3000 RISEN SON BV, POTT COUNTY\nEMERGENCY/NON EMERGENCY - MEDICAL TRANSPORT\nXST: VIRGINIA HILLS RD//",
         "SRC:1700",
         "ID:17-12-001",
         "DATE:01/05",
         "TIME:05:23",
         "ADDR:3000 RISEN SON BV",
         "CALL:EMERGENCY/NON EMERGENCY - MEDICAL TRANSPORT",
         "X:VIRGINIA HILLS RD//");
 
     doTest("T12",
         "[1700]  17-11-383 12/31 21:43\n15091 VALLEY VIEW DR, POTT COUNTY\nPOLE - GENERIC\nXST: HY 92//",
         "SRC:1700",
         "ID:17-11-383",
         "DATE:12/31",
         "TIME:21:43",
         "ADDR:15091 VALLEY VIEW DR",
         "CALL:POLE - GENERIC",
         "X:HY 92//");
 
     doTest("T13",
         "[1700]  17-11-381 12/30 13:59\n14185 OVERLAND TL, POTT COUNTY\nSICK PERSON - GENERIC\nXST: WABASH AV//",
         "SRC:1700",
         "ID:17-11-381",
         "DATE:12/30",
         "TIME:13:59",
         "ADDR:14185 OVERLAND TL",
         "MADDR:14185 OVERLAND TRL",
         "CALL:SICK PERSON - GENERIC",
         "X:WABASH AV//");
 
     doTest("T14",
         "[1700]  17-11-380 12/29 21:52\n19897 BEVERLY MANOR, POTT COUNTY\nFALLS - GENERIC\nXST: LONGVIEW LP//DEAD END",
         "SRC:1700",
         "ID:17-11-380",
         "DATE:12/29",
         "TIME:21:52",
         "ADDR:19897 BEVERLY MANOR",
         "CALL:FALLS - GENERIC",
         "X:LONGVIEW LP//DEAD END");
 
     doTest("T15",
         "[1700]  17-11-377 12/29 11:40\n22170 GREENVIEW RD, POTT COUNTY\nFIRE - WEED GRASS FIRE\nXST: VALLEY VIEW DR//LONGVIEW LP",
         "SRC:1700",
         "ID:17-11-377",
         "DATE:12/29",
         "TIME:11:40",
         "ADDR:22170 GREENVIEW RD",
         "CALL:FIRE - WEED GRASS FIRE",
         "X:VALLEY VIEW DR//LONGVIEW LP");
 
     doTest("T16",
         "[1700]  23-11-150 /17-11-372 /19-11-067 12/25 09:10\n24433 RICHFIELD LP, POTT COUNTY\nFIRE - RESIDENCE STRUCTURE FIRE\nXST: MCPHERSON AV//",
         "SRC:1700",
         "ID:23-11-150 /17-11-372 /19-11-067",
         "DATE:12/25",
         "TIME:09:10",
         "ADDR:24433 RICHFIELD LP",
         "CALL:FIRE - RESIDENCE STRUCTURE FIRE",
         "X:MCPHERSON AV//");
 
     doTest("T17",
         "[1700]  17-11-365 12/19 07:53\nSUNNYDALE RD // MCPHERSON AV, POTT COUNTY\nFIRE - EXTRICATION",
         "SRC:1700",
         "ID:17-11-365",
         "DATE:12/19",
         "TIME:07:53",
         "ADDR:SUNNYDALE RD &  & MCPHERSON AV",
         "MADDR:SUNNYDALE RD &  & MCPHERSON AVE",
         "CALL:FIRE - EXTRICATION");
 
     doTest("T18",
         "[1700]  17-11-339 11/23 08:32\n20595 MCPHERSON AV, POTT COUNTY\nFIRE - WEED GRASS FIRE\nXST: STATE ORCHARD RD//SUNNYDALE RD",
         "SRC:1700",
         "ID:17-11-339",
         "DATE:11/23",
         "TIME:08:32",
         "ADDR:20595 MCPHERSON AV",
         "MADDR:20595 MCPHERSON AVE",
         "CALL:FIRE - WEED GRASS FIRE",
         "X:STATE ORCHARD RD//SUNNYDALE RD");
   }
 
  @Test
  public void testParser2() {

    doTest("T1",
        "(1700) 05/06 14:48\n45 I29, POTT COUNTY\nFIRE - CAR AUTOMOBILE FIRE\nXST: 490 ST//ANTIQUE CITY DR",
        "SRC:1700",
        "DATE:05/06",
        "TIME:14:48",
        "ADDR:45 I29",
        "MADDR:45 I 29",
        "CALL:FIRE - CAR AUTOMOBILE FIRE",
        "X:490 ST//ANTIQUE CITY DR");

  }
   
   public static void main(String[] args) {
    new IAPottawattamieCountyParserTest().generateTests("T1");
   }
 }
