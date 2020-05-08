 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDPrinceGeorgesCountyFireBizParser;
 
 import org.junit.Test;
 
 
 public class MDPrinceGeorgesCountyFireBizParserTest extends BaseParserTest {
   
   public MDPrinceGeorgesCountyFireBizParserTest() {
     setParser(new MDPrinceGeorgesCountyFireBizParser(), "PRINCE GEORGES COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "55: BUILDING FIRE\nE809 TK809 BO804\n4637 37TH ST (ALLISON ST & DEAD END)\n12/07 04:51\nhttp://fireblitz.com/09/9.html",
         "SRC:55",
         "CALL:BUILDING FIRE",
         "UNIT:E809 TK809 BO804",
         "ADDR:4637 37TH ST",
         "X:ALLISON ST & DEAD END",
         "INFO:http://fireblitz.com/09/9.html");
         
     doTest("T2",
         "0-2 H12: PI\nS11 R11\nPG INDIAN HEAD HWY/FARMINGTON RD WEST\nNotes: WANTS ENGINE AND AMBULANCE FROM STATION 11\n12/07 06:14\nhttp://fireblitz.com/11/0.html",
         "SRC:0",
         "MAP:2 H12",
         "CALL:PI",
         "UNIT:S11 R11",
         "ADDR:INDIAN HEAD HWY & FARMINGTON RD WEST",
         "INFO:WANTS ENGINE AND AMBULANCE FROM STATION 11\nhttp://fireblitz.com/11/0.html");
     
     doTest("T3",
         "26: INVESTIGATION\nE817\n6036 PARKLAND CT (OLD SILVER HILL RD & OLD SILVER HILL RD)\n12/06 22:40\nhttp://fireblitz.com/17/9.html",
         "SRC:26",
         "CALL:INVESTIGATION",
         "UNIT:E817",
         "ADDR:6036 PARKLAND CT",
         "X:OLD SILVER HILL RD & OLD SILVER HILL RD",
         "INFO:http://fireblitz.com/17/9.html");
       
     doTest("T4",
         "48: FD ACCIDENT\nSQ818 B802\nANNAPOLIS RD/PRINCESS GARDEN PY (9006 ANNAPOLIS RD/5900 PRINCESS GARD\n12/07 07:02\nhttp://fireblitz.com/18/6.html",
         "SRC:48",
         "CALL:FD ACCIDENT",
         "UNIT:SQ818 B802",
         "ADDR:ANNAPOLIS RD & PRINCESS GARDEN PY",
         "X:9006 ANNAPOLIS RD/5900 PRINCESS GARD",
         "INFO:http://fireblitz.com/18/6.html");
       
     doTest("T5",
         "24: OVERDOSE - ALS\nA824 MD847, CHARLIE OD/NOT ALRT\n1605 AIRPORT LN (SCHALL RD & BERRY RD)\n12/07 07:14\nhttp://fireblitz.com/24/8.html",
         "SRC:24",
         "CALL:OVERDOSE - ALS",
         "UNIT:A824 MD847",
         "ADDR:1605 AIRPORT LN",
         "X:SCHALL RD & BERRY RD",
         "INFO:http://fireblitz.com/24/8.html");
       
     doTest("T6",
         "27: INJURED/SICK\nA827\n6304 MAXWELL DR #4 (CARSWELL AV & MORRIS AV)\n12/07 05:14\nhttp://fireblitz.com/27/2.html",
         "SRC:27",
         "CALL:INJURED/SICK",
         "UNIT:A827",
         "ADDR:6304 MAXWELL DR",
         "APT:4",
         "X:CARSWELL AV & MORRIS AV",
         "INFO:http://fireblitz.com/27/2.html");
       
     doTest("T7",
         "46: BUILDING FIRE\nE833 TK833 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/33/9.html",
         "SRC:46",
         "CALL:BUILDING FIRE",
         "UNIT:E833 TK833 BO802",
         "ADDR:1200 CAPITAL CENTER BL",
         "X:BLVD AT THE CAP CTR",
         "INFO:http://fireblitz.com/33/9.html");
       
     doTest("T8",
         "46: BUILDING FIRE\nE837 TK837 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/37/5.html",
         "SRC:46",
         "CALL:BUILDING FIRE",
         "UNIT:E837 TK837 BO802",
         "ADDR:1200 CAPITAL CENTER BL",
         "X:BLVD AT THE CAP CTR",
         "INFO:http://fireblitz.com/37/5.html");
       
     doTest("T9",
         "49: MEDIC LOCAL\nA849\n13218 DEERFIELD RD (DEAD END & MATTHEWS CT)\n12/06 23:00\nhttp://fireblitz.com/49/1.html",
         "SRC:49",
         "CALL:MEDIC LOCAL",
         "UNIT:A849",
         "ADDR:13218 DEERFIELD RD",
         "X:DEAD END & MATTHEWS CT",
         "INFO:http://fireblitz.com/49/1.html");
     
     doTest("T10",
         "48: TOWNHOUSE FIRE\nE818 BO802\n9903 BREEZY KNOLL CT [DEAD END & GREEN HAVEN RD]\n12/23 23:32\nhttp://fireblitz.com/18/8.shtm",
         "SRC:48",
         "CALL:TOWNHOUSE FIRE",
         "UNIT:E818 BO802",
         "ADDR:9903 BREEZY KNOLL CT",
         "X:DEAD END & GREEN HAVEN RD",
         "INFO:http://fireblitz.com/18/8.shtm");
 
     doTest("T11",
         "45: PIA-HIGHWAY\nSQ827\nNB CRAIN HWY SE/NB HEATHERMORE BLVD, PP - <8322/ 0>\n07/05 10:02\nhttp://fireblitz.com/27/0.shtm",
         "SRC:45",
         "CALL:PIA-HIGHWAY",
         "UNIT:SQ827",
         "ADDR:NB CRAIN HWY SE & NB HEATHERMORE BLVD",
         "INFO:http://fireblitz.com/27/0.shtm");
 
     doTest("T12",
         "27: PIA-HIGHWAY\nA827 SQ827\nSB CAP BELT IL A HWY/NB BRANCH AVE, PP\n07/06 10:20\nhttp://fireblitz.com/27/5.shtm",
         "SRC:27",
         "CALL:PIA-HIGHWAY",
         "UNIT:A827 SQ827",
         "ADDR:SB CAP BELT IL A HWY & NB BRANCH AVE",
         "INFO:http://fireblitz.com/27/5.shtm");
 
 
     doTest("T13",
         "32: HOUSE FIRE\nE827 BO885\n5311 ACORN DR (GLEN OAK DR and WHITE OAK AVE)\n10/01 15:59\nhttp://fireblitz.com/PG/27/3.shtm",
         "SRC:32",
         "CALL:HOUSE FIRE",
         "UNIT:E827 BO885",
         "ADDR:5311 ACORN DR",
         "X:GLEN OAK DR and WHITE OAK AVE",
         "INFO:http://fireblitz.com/PG/27/3.shtm");
 
     doTest("T14",
         "26: APARTMENT FIRE\nSQ827 BO883\n6912 WALKER MILL RD #C2 (KAREN BLVD and SHADY GLEN DR)\n10/01 21:35\nhttp://fireblitz.com/PG/27/0.shtm",
         "SRC:26",
         "CALL:APARTMENT FIRE",
         "UNIT:SQ827 BO883",
         "ADDR:6912 WALKER MILL RD",
         "X:KAREN BLVD and SHADY GLEN DR",
         "INFO:http://fireblitz.com/PG/27/0.shtm");
  }
   
   public static void main(String[] args) {
    new MDPrinceGeorgesCountyFireBizParserTest().generateTests("T15", "SRC CALL UNIT ADDR X INFO");
   }
 }
