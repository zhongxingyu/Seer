 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDPrinceGeorgesCountyCParserTest extends BaseParserTest {
   
   public MDPrinceGeorgesCountyCParserTest() {
     setParser(new MDPrinceGeorgesCountyCParser(), "PRINCE GEORGES COUNTY", "MD");
   }
   
   @Test
   public void testChristopherOlsen() {
 
     doTest("T1",
         "E812 Suicide, HAGERSTOWN HALL - UM 258, UM, at 258 ELLICOTT DR, UM, TGA2, 1210, Units:A812, E812, F121220001",
         "SRC:E812",
         "CALL:Suicide",
         "PLACE:HAGERSTOWN HALL - UM 258",
         "ADDR:258 ELLICOTT DR",  // NO MAP
         "CH:TGA2",
         "BOX:1210",
         "UNIT:A812,E812",
         "ID:F121220001");
 
     doTest("T2",
         "E816 Non-Emerg Service, BOWIE HEALTH CENTER, PP, at 15001 HEALTH CENTER DR, PP, TGA2, 1608, Unit:E816, F121220004",
         "SRC:E816",
         "CALL:Non-Emerg Service",
         "PLACE:BOWIE HEALTH CENTER",
         "ADDR:15001 HEALTH CENTER DR",
         "CH:TGA2",
         "BOX:1608",
         "UNIT:E816",
         "ID:F121220004");
 
     doTest("T3",
         "E841 House Fire, 3408 OLIVE BRANCH, TGA2, MAMC, Units:BO886, E831, PE841B, SQ849, TW810, F121220113",
         "SRC:E841",
         "CALL:House Fire",
         "ADDR:3408 OLIVE BRANCH",  // NO MAP
         "CH:TGA2",
         "BOX:MAMC",
         "UNIT:BO886,E831,PE841B,SQ849,TW810",
         "ID:F121220113");
 
     doTest("T4",
         "House Fire, 3408 OLIVE BRANCH, TGA2, MAMC, Units:BO886, E831, PE841B, SQ849, TW810, F121220113",
         "CALL:House Fire",
         "ADDR:3408 OLIVE BRANCH",
         "CH:TGA2",
         "BOX:MAMC",
         "UNIT:BO886,E831,PE841B,SQ849,TW810",
         "ID:F121220113");
 
     doTest("T5",
         "E831 House Fire, 3408 OLIVE BRANCH, TGA2, MAMC, Units:BO886, E831, PE841B, SQ849, TW810, F121220113",
         "SRC:E831",
         "CALL:House Fire",
         "ADDR:3408 OLIVE BRANCH",
         "CH:TGA2",
         "BOX:MAMC",
         "UNIT:BO886,E831,PE841B,SQ849,TW810",
         "ID:F121220113");
 
     doTest("T6",
         "E833 BLS Amb, LANDOVER METRO STATION, PP, at 3000 PENNSY DR, PP, TGA2, 3301, Units:A830, E833, F121220089",
         "SRC:E833",
         "CALL:BLS Amb",
         "PLACE:LANDOVER METRO STATION",
         "ADDR:3000 PENNSY DR",  // NO MAP
         "CH:TGA2",
         "BOX:3301",
         "UNIT:A830,E833",
         "ID:F121220089");
 
     doTest("T7",
         "A836 BLS Amb, BRANDYWINE FD 40, PP, at 14201 BRANDYWINE RD, PP, TGC2, 4010, Unit:A836, F121220133",
         "SRC:A836",
         "CALL:BLS Amb",
         "PLACE:BRANDYWINE FD 40",
         "ADDR:14201 BRANDYWINE RD",
         "CH:TGC2",
         "BOX:4010",
         "UNIT:A836",
         "ID:F121220133");
 
     doTest("T8",
         "E836 Medic Local, MOLLY BERRY RD/VAN BRADY RD, PP,  <12000/ 13899>, TGC2, 4016, Units:E836, MD845, F121220136",
         "SRC:E836",
         "CALL:Medic Local",
         "ADDR:MOLLY BERRY RD & VAN BRADY RD",
         "CH:TGC2",
         "BOX:4016",
         "UNIT:E836,MD845",
         "ID:F121220136");
 
     doTest("T9",
         "A848 Medic Local, DUVAL HIGH SCHOOL, PP, at 9880 GOOD LUCK RD, PP, TGA2, 4825, Units:A848, MD830, F121220179",
         "SRC:A848",
         "CALL:Medic Local",
         "PLACE:DUVAL HIGH SCHOOL",
         "ADDR:9880 GOOD LUCK RD",
         "CH:TGA2",
         "BOX:4825",
         "UNIT:A848,MD830",
         "ID:F121220179");
 
     doTest("T10",
         "A848 F121220192: Medic Local, ELEANOR ROOSEVELT HIGH SCHOOL, GP, at 7601 HANOVER PKY, GP, TGA2, 3506, Units:A848, E835, MD830",
         "SRC:A848",
         "CALL:Medic Local",
         "PLACE:ELEANOR ROOSEVELT HIGH SCHOOL",
         "ADDR:7601 HANOVER PKY",
         "CH:TGA2",
         "BOX:3506",
         "UNIT:A848,E835,MD830",
         "ID:F121220192");
 
     doTest("T11",
         "E828 F121220324: PIA Limited Access, WB JOHN HANSON HWY WB AT NB CAP BELT OL A HWY, PP, at WB JOHN HANSON HWY WB AT NB CAP BELT OL A HWY, PP <1900/, TGA2, 0619, Units:A830, E828B",
         "SRC:E828",
         "CALL:PIA Limited Access",
         "ADDR:WB JOHN HANSON HWY WB & NB CAP BELT OL A HWY",
         "MADDR:JOHN HANSON HWY & CAPITAL BELTWAY",
         "CH:TGA2",
         "BOX:0619",
         "UNIT:A830,E828B",
         "ID:F121220324");
 
     doTest("T12",
         "F121230149: Assault, 4351 GARDEN CITY DR, PP, btwn CORPORATE DR and PROFESSIONAL PL, TGA2, 2809, Units:A830, E828B, MD818",
         "CALL:Assault",
         "ADDR:4351 GARDEN CITY DR",
         "X:CORPORATE DR and PROFESSIONAL PL",
         "CH:TGA2",
         "BOX:2809",
         "UNIT:A830,E828B,MD818",
         "ID:F121230149");
 
     doTest("T13",
         "A848 F121230155: BLS Amb, 8600 GLENARDEN PKY, PP, btwn MARTIN LUTHER KING HWY and MCLAIN AVE, TGA2, 3314, Unit:A848",
         "SRC:A848",
         "CALL:BLS Amb",
         "ADDR:8600 GLENARDEN PKY",
         "X:MARTIN LUTHER KING HWY and MCLAIN AVE",
         "CH:TGA2",
         "BOX:3314",
         "UNIT:A848",
         "ID:F121230155");
 
     doTest("T14",
         "F121230193: Street Alarm, 3722 HALLOWAY PL, PP, btwn HALLOWAY CT and HALLOWAY NORTH, TA11, 2002, Units:BO887, E843, E845, SQ820, TW839",
         "CALL:Street Alarm",
         "ADDR:3722 HALLOWAY PL",
         "X:HALLOWAY CT and HALLOWAY NORTH",
         "CH:TA11",
         "BOX:2002",
         "UNIT:BO887,E843,E845,SQ820,TW839",
         "ID:F121230193");
 
     doTest("T15",
         "A848 F121230210: BLS Amb, DUVAL HIGH SCHOOL, PP, at 9880 GOOD LUCK RD, PP, TGA2, 4825, Unit:A848",
         "SRC:A848",
         "CALL:BLS Amb",
         "PLACE:DUVAL HIGH SCHOOL",
         "ADDR:9880 GOOD LUCK RD",
         "CH:TGA2",
         "BOX:4825",
         "UNIT:A848",
         "ID:F121230210");
 
     doTest("T16",
         "F121240261: PIA Limited Access, IL 495 PRIOR TO THE COUNTY LINE, TGA2, MAMC, Units:A841, BO886, PE841B, SQ814B",
         "CALL:PIA Limited Access",
         "ADDR:IL 495 PRIOR TO THE COUNTY LINE",
         "CH:TGA2",
         "BOX:MAMC",
         "UNIT:A841,BO886,PE841B,SQ814B",
         "ID:F121240261");
 
     doTest("T17",
         "F121240264: PIA Limited Access, NB CAP BELT OL A HWY AT NB BALTIMORE WASHINGTON PKY NB, PP, at NB CAP BELT OL A HWY AT NB BALTIMORE WASHINGTON PKY NB, P, TGB3, 2833, Units:A830, E828B, SQ814B",
         "CALL:PIA Limited Access",
         "ADDR:NB CAP BELT OL A HWY & NB BALTIMORE WASHINGTON PKY NB",
         "MADDR:CAPITAL BELTWAY & BALTIMORE WASHINGTON PKY",
         "CH:TGB3",
         "BOX:2833",
         "UNIT:A830,E828B,SQ814B",
         "ID:F121240264");
 
     doTest("T18",
         "F121240280: Medic Local, MAGNOLIA GARDENS NURSG HOME, PP, at 8200 GOOD LUCK RD, PP, TGA2, 4807, Units:A84S8, MD818",
         "CALL:Medic Local",
         "PLACE:MAGNOLIA GARDENS NURSG HOME",
         "ADDR:8200 GOOD LUCK RD",
         "CH:TGA2",
         "BOX:4807",
         "UNIT:A84S8,MD818",
         "ID:F121240280");
 
     doTest("T19",
         "F121330384: PIA Limited Access, WB JOHN HANSON HWY WB/NB CAP BELT OL A HWY, PP, at WB JOHN HANSON HWY WB/NB CAP BELT OL A HWY, PP <1900/0>, TGB3, 0619, Units:A848, E828B",
         "CALL:PIA Limited Access",
         "ADDR:WB JOHN HANSON HWY WB & NB CAP BELT OL A HWY",
         "MADDR:JOHN HANSON HWY & CAPITAL BELTWAY",
         "CH:TGB3",
         "BOX:0619",
         "UNIT:A848,E828B",
         "ID:F121330384");
 
   }
   
   @Test
   public void testFrancisBauer() {
 
     doTest("T1",
         "E816 Fire Alarm-AFA, 607 JARVIS CREST CT, PP, btwn END and JOHNSTONE LN, TGA2, 4313, Unit:E816, F1212201",
         "SRC:E816",
         "CALL:Fire Alarm-AFA",
         "ADDR:607 JARVIS CREST CT",
         "X:END and JOHNSTONE LN",
         "CH:TGA2",
         "BOX:4313",
         "UNIT:E816",
         "ID:F1212201");
 
     doTest("T2",
         "PA816 F121220329: Assault, 5611 CHURCH RD, PP, btwn WESTVIEW FOREST DR and URBANA LN, TGA2, 3906, Units: E839, PA816",
         "SRC:PA816",
         "CALL:Assault",
         "ADDR:5611 CHURCH RD",
         "X:WESTVIEW FOREST DR and URBANA LN",
         "CH:TGA2",
         "BOX:3906",
         "UNIT:E839,PA816",
         "ID:F121220329");
 
     doTest("T3",
         "F121230316: Departmental Acci, EB ANNAPOLIS RD/HILLMEADE RD, PP,  <12699/ 5899>, TGB3, 1815, Units:A839,",
         "CALL:Departmental Acci",
         "ADDR:EB ANNAPOLIS RD & HILLMEADE RD",
         "MADDR:ANNAPOLIS RD & HILLMEADE RD",
         "CH:TGB3",
         "BOX:1815",
         "UNIT:A839",
         "ID:F121230316");
 
   }
   
   @Test
   public void testNhedrick36() {
 
     doTest("T1",
         "E836 F121220300: Medic Local, NB CAP BELT OL NO HWY/GLENARDEN PKY, PP, TGA2, 3332, Unit:E836",
         "SRC:E836",
         "CALL:Medic Local",
         "ADDR:NB CAP BELT OL NO HWY & GLENARDEN PKY",
         "MADDR:CAPITAL BELTWAY & GLENARDEN PKY",
         "CH:TGA2",
         "BOX:3332",
         "UNIT:E836",
         "ID:F121220300");
  
   }
   
   @Test
   public void testCavaliere() {
 
     doTest("T1",
         "FRM:rc.505@c-msg.net\n" +
         "SUBJ:CAD\n" +
         "MSG:[PG11] F121230151: Acc w/Inj, 9330 BALTIMORE AVE, PP, btwn ERIE ST and FOX ST, TGA2, 1103, Units:A811B, E811\r\n" +
         "\r\n",
 
         "CALL:Acc w/Inj",
         "ADDR:9330 BALTIMORE AVE",
         "X:ERIE ST and FOX ST",
         "CH:TGA2",
         "BOX:1103",
         "UNIT:A811B,E811",
         "ID:F121230151");
 
     doTest("T2",
         "FRM:rc.505@c-msg.net\n" +
         "SUBJ:CAD\n" +
         "MSG:[PG11] F121230126: Medic Local, 9020 49TH AVE, PP, btwn BLACKFOOT RD and CHEROKEE ST, TGA2, 1111, Units:A811B, MD812\r\n" +
         "\r\n",
 
         "CALL:Medic Local",
         "ADDR:9020 49TH AVE",
         "X:BLACKFOOT RD and CHEROKEE ST",
         "CH:TGA2",
         "BOX:1111",
         "UNIT:A811B,MD812",
         "ID:F121230126");
 
     doTest("T3",
         "FRM:rc.505@c-msg.net\n" +
         "SUBJ:CAD\n" +
         "MSG:[PG11] F121220412: Fire Alarm-AFA, 9300 CHERRY HILL RD, PP, btwn PARK DR and SOUTH FARM DR, TGA2, 1101, Unit:E811\r\n" +
         "\r\n",
 
         "CALL:Fire Alarm-AFA",
         "ADDR:9300 CHERRY HILL RD",
         "X:PARK DR and SOUTH FARM DR",
         "CH:TGA2",
         "BOX:1101",
         "UNIT:E811",
         "ID:F121220412");
 
     doTest("T4",
         "1 of 2\n" +
         "FRM:rc.505@c-msg.net\n" +
         "SUBJ:CAD\n" +
         "MSG:[PG11] F121220359: PIA Limited Access, NB CAP BELT OL A/SB I95, PP, at NB CAP BELT OL A/SB I95, PP\n" +
         "(Con't) 2 of 2\n" +
         "<23268/0>, TGB3, 1119, Units:A811B, E811, SQ814B\r\n" +
         "\r\n" +
         "\r\n" +
         "(End)",
 
         "CALL:PIA Limited Access",
         "ADDR:NB CAP BELT OL A & SB I95",
         "MADDR:CAPITAL BELTWAY & I 95",
         "CH:TGB3",
         "BOX:1119",
         "UNIT:A811B,E811,SQ814B",
         "ID:F121220359");
 
     doTest("T5",
         "1 of 2\n" +
         "FRM:rc.505@c-msg.net\n" +
         "SUBJ:CAD\n" +
         "MSG:[PG11] F121230187: Pedestrian Struck, 9591 BALTIMORE AVE, PP, btwn INDIAN LN and PERU RD, TGA2, 1103,\n" +
         "(Con't) 2 of 2\n" +
         "Units:A811B, E811, MD812\r\n" +
         "\r\n" +
         "(End)",
 
         "CALL:Pedestrian Struck",
         "ADDR:9591 BALTIMORE AVE",
         "X:INDIAN LN and PERU RD",
         "CH:TGA2",
         "BOX:1103",
         "UNIT:A811B,E811,MD812",
         "ID:F121230187");
 
     doTest("T6",
         "FRM:rc.505@c-msg.net\n" +
         "SUBJ:CAD\n" +
         "MSG:[PG11] F121230226: Medic Local, 2213 APACHE ST, PP, btwn RIGGS RD and 23RD AVE, TGA2, 3405, Units:A811B, E834B, MD812\r\n" +
         "\r\n",
 
         "CALL:Medic Local",
         "ADDR:2213 APACHE ST",
         "X:RIGGS RD and 23RD AVE",
         "CH:TGA2",
         "BOX:3405",
         "UNIT:A811B,E834B,MD812",
         "ID:F121230226");
 
   }
   
   @Test
   public void testJohnCrisman() {
 
     doTest("T1",
         "A820 F121240285: Acc w/Inj, LARGO RD AT BROCK DR, PP,  <3100/ 3099>, TGA2, 2011, Units:A820, PE846, SQ820",
         "SRC:A820",
         "CALL:Acc w/Inj",
         "ADDR:LARGO RD & BROCK DR",
         "CH:TGA2",
         "BOX:2011",
         "UNIT:A820,PE846,SQ820",
         "ID:F121240285");
    
   }
   
   public static void main(String[] args) {
     new MDPrinceGeorgesCountyCParserTest().generateTests("T16");
   }
 }
