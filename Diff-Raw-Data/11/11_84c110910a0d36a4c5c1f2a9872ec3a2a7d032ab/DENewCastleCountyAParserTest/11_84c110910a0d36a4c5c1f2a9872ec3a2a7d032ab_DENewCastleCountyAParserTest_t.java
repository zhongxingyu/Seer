 package net.anei.cadpage.parsers.DE;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class DENewCastleCountyAParserTest extends BaseParserTest {
   
   public DENewCastleCountyAParserTest() {
     setParser(new DENewCastleCountyAParser(), "NEW CASTLE COUNTY", "DE");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(25CAD) [FB] F00 19:58 1 - T:69D5 (L3*MULTI-RESIDENTIAL FIRE) L:755 MONTCLAIR DR #2 ,PF *NAAMANS APTS - X:btwn NAAMANS RD ~ SOCIETY DR DESC: smoke fromneighbors",
         "SRC:25CAD",
         "CALL:69D5 (L3*MULTI-RESIDENTIAL FIRE)",
         "ADDR:755 MONTCLAIR DR",
         "APT:2",
         "PLACE:NAAMANS APTS",
         "X:btwn NAAMANS RD ~ SOCIETY DR",
         "INFO:smoke fromneighbors");
 
     doTest("T2",
         "(25CAD) [FB] F00 12:11 1 - T:69D7 (L3*CHIMNEY FIRE) L:2007 FERNDALE DR ,X1 *WESTWOOD MANOR - X:btwn FORREST RD ~ VEALE RD DESC: chimney.?!?!",
         "SRC:25CAD",
         "CALL:69D7 (L3*CHIMNEY FIRE)",
         "ADDR:2007 FERNDALE DR",
         "PLACE:WESTWOOD MANOR",
         "X:btwn FORREST RD ~ VEALE RD",
         "INFO:chimney.?!?!");
 
     doTest("T3",
         "[FB] F00 03:14 1 - T:69D6 (L3*RESIDENTIAL FIRE) L:108 CARLIE RD ,NT *LYNNFIELD - X:btwn HOMEWOOD RD ~ RIDGELAND RD",
         "CALL:69D6 (L3*RESIDENTIAL FIRE)",
         "ADDR:108 CARLIE RD",
         "PLACE:LYNNFIELD",
         "X:btwn HOMEWOOD RD ~ RIDGELAND RD");
 
     doTest("T4",
         "[FB] F00 08:06 1 - T:55B2 (L1*ELECTRICAL HAZARD-WIRES DWN) L:ELGIN LA~SHIPLEY RD ,QB *OAK LANE MANOR - X: DESC: WIRES DOWN.?!?!",
         "CALL:55B2 (L1*ELECTRICAL HAZARD-WIRES DWN)",
         "ADDR:ELGIN LN & SHIPLEY RD",
         "PLACE:OAK LANE MANOR",
         "INFO:WIRES DOWN.?!?!");
 
     doTest("T5",
         "(25CAD) [FB] F00 01:38 1 - T:52C3S (L1*COMMERCIAL FIRE ALARM) L:4130 CONCORD PK ,HU06 -- 6TH AVE ELECTRONIC - X:btwn SILVERSIDE RD ~ PASSMORE RD DESC: fire alar",
         "SRC:25CAD",
         "CALL:52C3S (L1*COMMERCIAL FIRE ALARM)",
         "ADDR:4130 CONCORD PK",
         "PLACE:6TH AVE ELECTRONIC",
         "X:btwn SILVERSIDE RD ~ PASSMORE RD",
         "INFO:fire alar");
 
     doTest("T6",
         "(25CAD) [FB] F00 08:35 1 - T:69D4O (L3*COMMERCIAL STRUC FIRE W~HZ) L:4238 PHILADELPHIA PK ,HU06 -- CLAYMONT STEEL PLA - LI:PLATE MILL - X:btwn ALCOTT AV ~ NAAMANS RD",
         "SRC:25CAD",
         "CALL:69D4O (L3*COMMERCIAL STRUC FIRE W~HZ)",
         "ADDR:4238 PHILADELPHIA PK",
         "PLACE:CLAYMONT STEEL PLA - LI:PLATE MILL",
         "X:btwn ALCOTT AV ~ NAAMANS RD");
 
     doTest("T7",
         "(25CAD) [FB] F00 11:48 1 - T:52C3G (L1*COMMERCIAL FIRE ALARM) L:1801 AUGUSTINE CUT OFF ,HU06 -- ACCENTURE CONSULTI - X:btwn W EIGHTEENTH ST ~ STONE HILL RD DESC",
         "SRC:25CAD",
         "CALL:52C3G (L1*COMMERCIAL FIRE ALARM)",
         "ADDR:1801 AUGUSTINE CUT OFF",
         "PLACE:ACCENTURE CONSULTI",
         "X:btwn W EIGHTEENTH ST ~ STONE HILL RD");
 
     doTest("T8",
         "(25CAD) [FB] F00 22:27 1 - T:52C1G (L1*FIRE ALARM~GENERAL) L:4641 WELDIN RD ,XF -- MARY CAMPBELL CNTR *WELDIN WOODS - X:btwn JACOB WELDIN PL ~ WELDIN CI DESC: F",
         "SRC:25CAD",
         "CALL:52C1G (L1*FIRE ALARM~GENERAL)",
         "ADDR:4641 WELDIN RD",
         "PLACE:MARY CAMPBELL CNTR *WELDIN WOODS",
         "X:btwn JACOB WELDIN PL ~ WELDIN CI",
         "INFO:F");
 
     doTest("T9",
         "15:50T:52B1G (L1*RES FIRE ALARM~GENERAL ALAR) L:401 CAMPBELL RD ,KC GREENVILLE - X:KENNETT PK ~ MINKER CT : afa.?!?!",
         "CALL:52B1G (L1*RES FIRE ALARM~GENERAL ALAR)",
         "ADDR:401 CAMPBELL RD",
         "CITY:GREENVILLE",
         "X:KENNETT PK ~ MINKER CT",
         "INFO:afa.?!?!");
 
     doTest("T10",
         "23:34T:69D5 (L3*MULTI-RESIDENTIAL FIRE) L:100 ORCHARD LA #1 ,LU *HOLLY OAK - X:WALNUT ST ~ RIDGE RD",
         "CALL:69D5 (L3*MULTI-RESIDENTIAL FIRE)",
         "ADDR:100 ORCHARD LN",
         "CITY:HOLLY OAK",
         "X:WALNUT ST ~ RIDGE RD",
         "APT:1");
 
     doTest("T11",
         "17:33T:69D5 (L3*MULTI-RESIDENTIAL FIRE) L:301 HARBOR DR #10 ,ZY *WATER VIEW COURT APTS - X: high xst: GRUBBS LANDING RD : GREASE FIRE.",
         "CALL:69D5 (L3*MULTI-RESIDENTIAL FIRE)",
         "ADDR:301 HARBOR DR",
         "PLACE:WATER VIEW COURT APTS",
         "X:high xst: GRUBBS LANDING RD",
         "INFO:GREASE FIRE.",
         "APT:10");
 
     doTest("T12",
         "21:43T:M29B1 (MVC) L:CONCORD PK~MT LEBANON RD ,VE *TALLEYVILLE - :—àDSC:MVC—! PAT:1 SEX:Female AGE:25Years CON:Y BRE:Y",
         "CALL:M29B1 (MVC)",
         "ADDR:CONCORD PK & MT LEBANON RD",
         "PLACE:TALLEYVILLE",
         "INFO:—àDSC:MVC—! PAT:1 SEX:Female AGE:25Years CON:Y BRE:Y");
 
     doTest("T13",
         "15:50T:52B1G (L1*RES FIRE ALARM~GENERAL ALAR) L:401 CAMPBELL RD ,KC *GREENVILLE - X:KENNETT PK ~ MINKER CT : afa.",
         "CALL:52B1G (L1*RES FIRE ALARM~GENERAL ALAR)",
         "ADDR:401 CAMPBELL RD",
         "CITY:GREENVILLE",
         "X:KENNETT PK ~ MINKER CT",
         "INFO:afa.");
 
     doTest("T14",
         "13:58T:52B1G (L1*RES FIRE ALARM~GENERAL ALAR) L:102 PONDS LA ,PNGR *PONDS OF GREENVILLE - X:MONTCHANIN RD ~ DEAD-END : afa",
         "CALL:52B1G (L1*RES FIRE ALARM~GENERAL ALAR)",
         "ADDR:102 PONDS LN",
         "PLACE:PONDS OF GREENVILLE",
         "X:MONTCHANIN RD ~ DEAD-END",
         "INFO:afa");
 
     doTest("T15",
         "22:31T:M29D5 (MVC--NOT ALERT) L:CONCORD PK~INTERSTATE 95 ,HU06 - LI:FROM 202 ONTO 95 SOUTH - :—àDSC:MVC—! PAT:1 SEX:Male AGE:25Years CON:Y BRE:Y",
         "CALL:M29D5 (MVC--NOT ALERT)",
         "ADDR:CONCORD PK & INTERSTATE 95",
         "PLACE:- LI:FROM 202 ONTO 95 SOUTH",
         "INFO:—àDSC:MVC—! PAT:1 SEX:Male AGE:25Years CON:Y BRE:Y");
 
     doTest("T16",
         "09:02T:69D6 (L3*RESIDENTIAL FIRE) L:15 GLENROCK DR ,R5 *RADNOR GREEN - X:BENNING RD ~ PENNSYLVANIA AV : fire",
         "CALL:69D6 (L3*RESIDENTIAL FIRE)",
         "ADDR:15 GLENROCK DR",
         "PLACE:RADNOR GREEN",
         "X:BENNING RD ~ PENNSYLVANIA AV",
         "INFO:fire");
 
     doTest("T17",
         "19:06T:52C3G (L1*COMMERCIAL FIRE ALARM) L:1001 ROCKLAND RD ,HU06 -- DUPONT CC GOLF COU - LI:EQUIPMENT CENTER - X:BLACK GATES RD ~ COUNTRY CLUB DR",
         "CALL:52C3G (L1*COMMERCIAL FIRE ALARM)",
         "ADDR:1001 ROCKLAND RD",
         "PLACE:DUPONT CC GOLF COU - LI:EQUIPMENT CENTER",
         "X:BLACK GATES RD ~ COUNTRY CLUB DR");
 
     doTest("T18",
         "03:51T:55C1 (L1*ELECTRICAL HAZ W~NEAR WATER) L:112 BUCK RD ,HU07 - LI:IN FRONT OF ABV - X:ARDLEIGH DR ~ GREENOCK DR : transformer",
         "CALL:55C1 (L1*ELECTRICAL HAZ W~NEAR WATER)",
         "ADDR:112 BUCK RD",
         "PLACE:- LI:IN FRONT OF ABV",
         "X:ARDLEIGH DR ~ GREENOCK DR",
         "INFO:transformer");
 
     doTest("T19",
         "05:26T:71B1 (L1*VEH-FIRE) L:CONCORD PK~PIERCE RD ,GD *DEERHURST - X: : vehicle on fire",
         "CALL:71B1 (L1*VEH-FIRE)",
         "ADDR:CONCORD PK & PIERCE RD",
         "PLACE:DEERHURST",
         "INFO:vehicle on fire");
 
     doTest("T20",
         "21:42T:M29D2M (PEDESTRIAN STRUCK) L:5350 BRANDYWINE PY ,BRTC -- CHRISTMAS TREE SHO high xst: NAAMANS RD *BRANDYWINE TOWNE CENTER - :—àDSC:SUBJ STRUCK—! PAT:1 SEX:Female AGE:30Years CON:Y BRE:Y",
         "CALL:M29D2M (PEDESTRIAN STRUCK)",
         "ADDR:5350 BRANDYWINE PY",
         "PLACE:CHRISTMAS TREE SHO high xst: NAAMANS RD *BRANDYWINE TOWNE CENTER",
         "INFO:—àDSC:SUBJ STRUCK—! PAT:1 SEX:Female AGE:30Years CON:Y BRE:Y");
   
     doTest("T21",
         "Subject:24CAD\n[eFB] F00 21:22 1 - T:M10D2 (CHEST PAINS-DIFF SPEAKING BETW) L:1000 SNOWY EGRET LA ,AGC2 btwn POLE BRIDGE RD ~ BOBWHITE \nCT *AUGUSTINE CREEK II - DESC:??DSC:CHEST PAIN?! PAT:1 SEX:Male AGE:52Years CON:Y BRE:Y\n",
         "SRC:24CAD",
         "CALL:M10D2 (CHEST PAINS-DIFF SPEAKING BETW)",
         "ADDR:1000 SNOWY EGRET LN",
         "X:btwn POLE BRIDGE RD ~ BOBWHITE CT",
         "PLACE:AUGUSTINE CREEK II",
         "INFO:??DSC:CHEST PAIN?! PAT:1 SEX:Male AGE:52Years CON:Y BRE:Y");
 
     doTest("T22",
         "Subject:24CAD\n[eFB] F00 13:27 1 - T:M24B1 (EMERGENCY MATERNITY) L:106 N SIXTH ST ,QG btwn MAIN ST ~ HIGH ST *ODESSA - DESC:??DSC:water\n",
         "SRC:24CAD",
         "CALL:M24B1 (EMERGENCY MATERNITY)",
         "ADDR:106 N SIXTH ST",
         "X:btwn MAIN ST ~ HIGH ST",
         "CITY:ODESSA",
         "INFO:??DSC:water");
 
     doTest("T23",
         "(13CAD) [eFB] F00 18:39 1 - T:M24B1 (EMERGENCY MATERNITY) L:16 DEL HAVEN CT ,LU btwn GOVERNOR PRINTZ BL ~ WALNUT ST *HOLLY OAK - DESC:??DSC:MATERNITY 25YOF ~~~ ?! PAT:1 SEX:Female AGE:25Years CON:Y BRE:Y",
         "SRC:13CAD",
         "CALL:M24B1 (EMERGENCY MATERNITY)",
         "ADDR:16 DEL HAVEN CT",
         "CITY:HOLLY OAK",
         "X:btwn GOVERNOR PRINTZ BL ~ WALNUT ST",
         "INFO:??DSC:MATERNITY 25YOF ~~~ ?! PAT:1 SEX:Female AGE:25Years CON:Y BRE:Y");

    doTest("T24",
        "28CAD / [eFB] F00 17:23 1 - T:M29D5 (MVC--NOT ALERT) L:113 S DUPONT HY ,4Q -- LONE STAR STEAKHOU btwn CHRISTIANA RD ~ FIFTH AV * 28CAD / HARES CORNER - DESC:\n\n",
        "SRC:28CAD",
        "CALL:M29D5 (MVC--NOT ALERT)",
        "ADDR:113 S DUPONT HY",
        "PLACE:HARES CORNER",
        "X:btwn CHRISTIANA RD ~ FIFTH AV");
   
   }
   
   public static void main(String[] args) {
    new DENewCastleCountyAParserTest().generateTests("T25");
   }
 }
     		
