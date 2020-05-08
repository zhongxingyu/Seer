 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDPrinceGeorgesCountyEParserTest extends BaseParserTest {
   
   public MDPrinceGeorgesCountyEParserTest() {
     setParser(new MDPrinceGeorgesCountyEParser(), "PRINCE GEORGES COUNTY", "MD");
   }
 
   
   @Test
   public void testChristopherOlsen() {
 
     doTest("T1",
         "(CAD Feed) ALS1, Medic Local, 8500 MAGNOLIA DR, PP, btwn WOOD THRUSH DR and BLUE BIRD CT, TGA2, 4820, 5411 C 4, Medical ProQA recommends dispatch at this time, Units:A848, MD818 - From FED01 06/14/2012 19:47:08\n\n",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:8500 MAGNOLIA DR",
         "X:WOOD THRUSH DR and BLUE BIRD CT",
         "CH:TGA2",
         "BOX:4820",
         "MAP:5411 C 4",
         "UNIT:A848,MD818",
         "DATE:06/14/2012",
         "TIME:19:47:08");
 
     doTest("T2",
         "PIAT, PIA w/Entrapment, NB CAP BELT OL A HWY AT NB INDIAN HEAD HWY, PP, at NB CAP BELT OL A HWY AT NB INDIAN HEAD HWY, PP <6298/0>, TGB3, 4219, 5765 A 2, 3 VEHICLE ACC, AT LEAST ONE BLU VEH INTO THE JERSEY WALL,..POSS ENTRAPPED... UNK INJ, Units:A847, BO885, E821B, MD847, PE842, SQ847 - From FED03 06/14/2012 19:31:19",
         "CODE:PIAT",
         "CALL:PIA w/Entrapment",
         "ADDR:NB CAP BELT OL A HWY & NB INDIAN HEAD HWY",
         "MADDR:CAPITAL BELTWAY & INDIAN HEAD HWY",
         "CH:TGB3",
         "BOX:4219",
         "MAP:5765 A 2",
         "INFO:3 VEHICLE ACC / AT LEAST ONE BLU VEH INTO THE JERSEY WALL / ..POSS ENTRAPPED... UNK INJ",
         "UNIT:A847,BO885,E821B,MD847,PE842,SQ847",
         "DATE:06/14/2012",
         "TIME:19:31:19");
 
     doTest("T3",
         "(CAD Feed) ALS1, Medic Local, 7737 RIVERDALE RD #204, PP, btwn MAHONEY DR and ANNAPOLIS RD, NEW CARROLLTON, TGA2, 2831, 5410 K 7, Medical ProQA recommends dispatch at this time, Units:A830, E828, PE830 - From FED01 06/14/2012 19:11:54",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:7737 RIVERDALE RD",
         "APT:204",
         "X:MAHONEY DR and ANNAPOLIS RD",
         "CITY:NEW CARROLLTON",
         "CH:TGA2",
         "BOX:2831",
         "MAP:5410 K 7",
         "UNIT:A830,E828,PE830",
         "DATE:06/14/2012",
         "TIME:19:11:54");
 
     doTest("T4",
         "STREET, Street Alarm, 3745 WILKINSON DR, PP, btwn CHERRY DR and PARK BLVD, SUITLAND, TC11, 2906, 5649 J 7, Fire ProQA recommends dispatch at this time, Units:BO883, E826B, E829B, QT838, TK832 - From FES01 06/14/2012 20:51:25",
         "CODE:STREET",
         "CALL:Street Alarm",
         "ADDR:3745 WILKINSON DR",
         "X:CHERRY DR and PARK BLVD",
         "CITY:SUITLAND",
         "CH:TC11",
         "BOX:2906",
         "MAP:5649 J 7",
         "UNIT:BO883,E826B,E829B,QT838,TK832",
         "DATE:06/14/2012",
         "TIME:20:51:25");
 
     doTest("T5",
         "(CAD Feed) PIAT, PIA w/Entrapment, POWDER MILL RD/LAUREL BOWIE RD, PP,  <9999/ 11400>, LAUREL, TGB3, 1902, 5289 K 3, Police ProQA recommends dispatch at this time, Units:BO882, MD818 - From FED03 06/14/2012 22:19:35",
         "CODE:PIAT",
         "CALL:PIA w/Entrapment",
         "ADDR:POWDER MILL RD & LAUREL BOWIE RD",
         "CITY:LAUREL",
         "CH:TGB3",
         "BOX:1902",
         "MAP:5289 K 3",
         "UNIT:BO882,MD818",
         "DATE:06/14/2012",
         "TIME:22:19:35");
 
     doTest("T6",
         "(CAD Feed) HOUSEF, House Fire, 9300 ALLENTOWN RD, PP, btwn PINEHURST DR and CONSTANTINE DR, FORT WASHINGTON, TGC3, 3206, 5765 G 9, Fire ProQA recommends dispatch at this time, Units:BO885, E821B, E823B, E832B, PE842, SQ847, TK825, TK829 - From FED10 06/14/2012 22:25:10",
         "CODE:HOUSEF",
         "CALL:House Fire",
         "ADDR:9300 ALLENTOWN RD",
         "X:PINEHURST DR and CONSTANTINE DR",
         "CITY:FORT WASHINGTON",
         "CH:TGC3",
         "BOX:3206",
         "MAP:5765 G 9",
         "UNIT:BO885,E821B,E823B,E832B,PE842,SQ847,TK825,TK829",
         "DATE:06/14/2012",
         "TIME:22:25:10");
 
     doTest("T7",
         "(CAD Feed) PIAH, PIA Limited Access, SB CAP BELT IL A HWY/SB BALTIMORE WASHINGTON PKY SB, PP, at SB CAP BELT IL A HWY/SB BALTIMORE WASHINGTON PKY SB, PP, TGB3, 3516, 5410 J 2, COMPL ADV 2 CAR ACC BROWN MITSUBISHI VS GIANT FOOD TRACTOR TRAILER, UNK INJ, FEM MAY NEED AMBO. WILL NOTF MSP, Units:A830, E828 - From FED03 06/15/2012 00:59:09",
         "CODE:PIAH",
         "CALL:PIA Limited Access",
         "ADDR:SB CAP BELT IL A HWY & SB BALTIMORE WASHINGTON PKY SB",
         "MADDR:CAP BELT IL A HWY & BALTIMORE WASHINGTON PKY",
         "CH:TGB3",
         "BOX:3516",
         "MAP:5410 J 2",
         "INFO:COMPL ADV 2 CAR ACC BROWN MITSUBISHI VS GIANT FOOD TRACTOR TRAILER / UNK INJ / FEM MAY NEED AMBO. WILL NOTF MSP",
         "UNIT:A830,E828",
         "DATE:06/15/2012",
         "TIME:00:59:09");
 
     doTest("T8",
         "ALS1, Medic Local, 7321 SPLIT RAIL LN, LP, btwn MILL CREEK CT and DORSET RD, LAUREL,MD, TGA2, 1013, 5168 K 3, Medical ProQA recommends dispatch at this time, Units:A724, E810C, HMD06 - From FED01 06/15/2012 13:11:28",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:7321 SPLIT RAIL LN",
         "X:MILL CREEK CT and DORSET RD",
         "CITY:LAUREL",
         "CH:TGA2",
         "BOX:1013",
         "MAP:5168 K 3",
         "UNIT:A724,E810C,HMD06",
         "DATE:06/15/2012",
         "TIME:13:11:28");
 
     doTest("T9",
         "DEPFD, Departmental Acci, SB CAP BELT IL A HWY/NB BALTIMORE WASHINGTON PKY NB, PP, at SB CAP BELT IL A HWY/NB BALTIMORE WASHINGTON PKY NB, PP, TGA2, 3520, 5410 J 2, EMSDO REAR ENDED BY A VEHICLE ON THE BELTWAY WHILE SHE WAS RESPONDING TO PGTC....UNABLE TO GET A DESCRIPTION OF THE STRIKING VEHICLE....PROPERTY DAMAGE ONLY, Units:BO886, E835, EMSDO, NSO, SO8 00",
         "CODE:DEPFD",
         "CALL:Departmental Acci",
         "ADDR:SB CAP BELT IL A HWY & NB BALTIMORE WASHINGTON PKY NB",
         "MADDR:CAP BELT IL A HWY & BALTIMORE WASHINGTON PKY",
         "CH:TGA2",
         "BOX:3520",
         "MAP:5410 J 2",
         "INFO:EMSDO REAR ENDED BY A VEHICLE ON THE BELTWAY WHILE SHE WAS RESPONDING TO PGTC....UNABLE TO GET A DESCRIPTION OF THE STRIKING VEHICLE....PROPERTY DAMAGE ONLY",
         "UNIT:BO886,E835,EMSDO,NSO,SO8 00");
 
     doTest("T10",
         "F121790236: Brush Fire, 8310 ANNAPOLIS RD, PP, btwn RIVERDALE RD and 85TH AVE, TGA2, 2831, Unit:E828",
         "ID:F121790236",
         "CALL:Brush Fire",
         "ADDR:8310 ANNAPOLIS RD",
         "X:RIVERDALE RD and 85TH AVE",
         "CH:TGA2",
         "BOX:2831",
         "UNIT:E828");
 
     doTest("T11",
         "F121790235: BLS Amb, TIPICOS LAS AMERICAS, PP, at 9452 LANHAM SEVERN RD, PP, TGA2, 4816, Unit:A848",
         "ID:F121790235",
         "CALL:BLS Amb",
         "PLACE:TIPICOS LAS AMERICAS",
         "ADDR:9452 LANHAM SEVERN RD",
         "CH:TGA2",
         "BOX:4816",
         "UNIT:A848");
 
     doTest("T12",
         "F121800211: Acc w/Inj, EB ANNAPOLIS RD BEFORE NB CAP BELT OL A HWY, PP,\\2s<0/ 0>, TGA2, 2806, Units:A830, TK828",
         "ID:F121800211",
         "CALL:Acc w/Inj",
         "ADDR:EB ANNAPOLIS RD BEFORE NB CAP BELT OL A HWY",
         "MADDR:ANNAPOLIS RD BEFORE CAPITAL BELTWAY",
         "CITY:\\2s<0/ 0>",
         "CH:TGA2",
         "BOX:2806",
         "UNIT:A830,TK828");
 
     doTest("T13",
         "F121800225: Medic Local, 8300 QUINTANA ST, PP, btwn END and 84TH AVE, TGA2, 2825, Units:A807, E828, MD818",
         "ID:F121800225",
         "CALL:Medic Local",
         "ADDR:8300 QUINTANA ST",
         "X:END and 84TH AVE",
         "CH:TGA2",
         "BOX:2825",
         "UNIT:A807,E828,MD818");
 
     doTest("T14",
         "F121800232: Medic Local, 9206 5TH ST, PP, btwn MAIN ST and END, TGA2, 4809, Units:A848, MD830",
         "ID:F121800232",
         "CALL:Medic Local",
         "ADDR:9206 5TH ST",
         "X:MAIN ST and END",
         "CH:TGA2",
         "BOX:4809",
         "UNIT:A848,MD830");
 
     doTest("T15",
         "F121800279: PIA w/Entrapment, EQUESTRIAN CENTER, PP, at 5651 WATER ST, PP, TGB3, 2020, Units:A823, BO887, E845, MD845, SQ820",
         "ID:F121800279",
         "CALL:PIA w/Entrapment",
         "PLACE:EQUESTRIAN CENTER",
         "ADDR:5651 WATER ST",
         "CH:TGB3",
         "BOX:2020",
         "UNIT:A823,BO887,E845,MD845,SQ820");
 
     doTest("T16",
         "F121800373: Collapse, 1501 CABIN BRANCH DR, PP, btwn SAVAGE ST and COLUMBIA PARK RD, TGA7, 3808, Unit:TW810",
         "ID:F121800373",
         "CALL:Collapse",
         "ADDR:1501 CABIN BRANCH DR",
         "X:SAVAGE ST and COLUMBIA PARK RD",
         "CH:TGA7",
         "BOX:3808",
         "UNIT:TW810");

    doTest("T1",
        "TR F121850028: Confined Space Rescu, 4706 HOLLYWOOD RD, PP, btwn 47TH PL and 48TH AVE, TGD3, 1102, Units:A812, AU816, BO886, C699, E811, EMSDO, HC816, MD812, NSO, TR806, TS814, WI",
        "ID:F121850028",
        "CALL:Confined Space Rescu",
        "ADDR:4706 HOLLYWOOD RD",
        "X:47TH PL and 48TH AVE",
        "CH:TGD3",
        "BOX:1102",
        "UNIT:A812,AU816,BO886,C699,E811,EMSDO,HC816,MD812,NSO,TR806,TS814,WI");
  
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "[CAD Feed] STREET, Street Alarm, 9001 MCHUGH DR, PP, btwn END and CAMPUS WAY N, TA11, 0616, 5531 D 3, ODOR OF ELECTRICAL BURNING IN THE STORE, Units:BO881, E833, E837, QT838, TK828\n",
         "CODE:STREET",
         "CALL:Street Alarm",
         "ADDR:9001 MCHUGH DR",
         "X:END and CAMPUS WAY N",
         "CH:TA11",
         "BOX:0616",
         "MAP:5531 D 3",
         "INFO:ODOR OF ELECTRICAL BURNING IN THE STORE",
         "UNIT:BO881,E833,E837,QT838,TK828");
 
     doTest("T2",
         "[CAD Feed] ELEV, Stuck Elevator, 9301 ANNAPOLIS RD, PP, btwn MORLEY RD and WASHINGTON BLVD, TGA2, 4802, 5411 D 6, Fire ProQA recommends dispatch at this time, Unit:E828B\n",
         "CODE:ELEV",
         "CALL:Stuck Elevator",
         "ADDR:9301 ANNAPOLIS RD",
         "X:MORLEY RD and WASHINGTON BLVD",
         "CH:TGA2",
         "BOX:4802",
         "MAP:5411 D 6",
         "UNIT:E828B");
 
     doTest("T3",
         "[CAD Feed] PIAH, PIA Limited Access, 2200 CAP BELT OL A HWY, PP, btwn BALTIMORE WASHINGTON PKY SB and NB CAP BELT HWY OFRP SB, TGB3, 2833, 5410 J 2, CALLER ADV THAT A TIRE FELL OFF OF A VEH AND MADE A VEH SPIN OUT ADVI TS ON THE LEFT SIDE, Units:A819, E828B, SQ814\n",
         "CODE:PIAH",
         "CALL:PIA Limited Access",
         "ADDR:2200 CAP BELT OL A HWY",
         "MADDR:2200 CAPITAL BELTWAY",
         "X:BALTIMORE WASHINGTON PKY SB and NB CAP BELT HWY OFRP SB",
         "CH:TGB3",
         "BOX:2833",
         "MAP:5410 J 2",
         "INFO:CALLER ADV THAT A TIRE FELL OFF OF A VEH AND MADE A VEH SPIN OUT ADVI TS ON THE LEFT SIDE",
         "UNIT:A819,E828B,SQ814");
 
     doTest("T4",
         "[CAD Feed] HOUSEF, House Fire, 400 COMMERCE DR, PP, btwn COMMERCE CT and TRADE ZONE AVE, TGA3, 4308, 5533 D 9, Fire ProQA recommends dispatch at this time, Units:BO882, E816, E820B, E845, PE846, QT838, RE833, TW839\n",
         "CODE:HOUSEF",
         "CALL:House Fire",
         "ADDR:400 COMMERCE DR",
         "X:COMMERCE CT and TRADE ZONE AVE",
         "CH:TGA3",
         "BOX:4308",
         "MAP:5533 D 9",
         "UNIT:BO882,E816,E820B,E845,PE846,QT838,RE833,TW839");
 
     doTest("T5",
         "[CAD Feed] OUTF, Outside Fire, EB JOHN HANSON HWY EB/NB MARTIN LUTHER KING HWY, PP, at EB JOHN HANSON HWY EB/NB MARTIN LUTHER KING HWY, PP <939, TGA2, 2832, 5411 E 9, Fire ProQA recommends dispatch at this time, Unit:E828\n",
         "CODE:OUTF",
         "CALL:Outside Fire",
         "ADDR:EB JOHN HANSON HWY EB & NB MARTIN LUTHER KING HWY",
         "MADDR:JOHN HANSON HWY & MARTIN LUTHER KING HWY",
         "CH:TGA2",
         "BOX:2832",
         "MAP:5411 E 9",
         "UNIT:E828");
 
     doTest("T6",
         "[CAD Feed] PIAT, PIA w/Entrapment, 9052 KENILWORTH AVE, GP, btwn CAP BELT OL A HWY and NB CAP BELT HWY OFRP SB KENILWORT, TGB3, 3519, 5288 G 10, ONE OVERTURNED, Units:A831, BO886, E811, MD712, SQ814\n",
         "CODE:PIAT",
         "CALL:PIA w/Entrapment",
         "ADDR:9052 KENILWORTH AVE",
         "X:CAP BELT OL A HWY and NB CAP BELT HWY OFRP SB KENILWORT",
         "CH:TGB3",
         "BOX:3519",
         "MAP:5288 G 10",
         "INFO:ONE OVERTURNED",
         "UNIT:A831,BO886,E811,MD712,SQ814");
 
     doTest("T7",
         "[CAD Feed] PIAH, PIA Limited Access, EB JOHN HANSON HWY EB/NB CRAIN HWY NE, PP, at EB JOHN HANSON HWY EB/NB CRAIN HWY NE, PP <4698/0>, TGB3, 1619, 5413 F 8, VEH ACCIDENT POSSIBLE INJ.. ADV HER VEH IS SMOKING, Units:A830, E828\n",
         "CODE:PIAH",
         "CALL:PIA Limited Access",
         "ADDR:EB JOHN HANSON HWY EB & NB CRAIN HWY NE",
         "MADDR:JOHN HANSON HWY & CRAIN HWY NE",
         "CH:TGB3",
         "BOX:1619",
         "MAP:5413 F 8",
         "INFO:VEH ACCIDENT POSSIBLE INJ.. ADV HER VEH IS SMOKING",
         "UNIT:A830,E828");
 
     doTest("T8",
         "[CAD Feed] ALS1, Medic Local, 7501 GOOD LUCK RD, PP, btwn CATHEDRAL AVE and CAP BELT IL SO HWY, TGA2, 4812, 5410 K 4, C, Units:A830, E828, MD818\n",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:7501 GOOD LUCK RD",
         "X:CATHEDRAL AVE and CAP BELT IL SO HWY",
         "CH:TGA2",
         "BOX:4812",
         "MAP:5410 K 4",
         "INFO:C",
         "UNIT:A830,E828,MD818");
 
     doTest("T9",
         "[CAD Feed] COLAPI, Collapse Invest, 6805 10TH AVE, PP, btwn FAIRVIEW AVE and EAST WEST HWY, TGD3, 4411, 5409 B 5, Police ProQA recommends dispatch at this time, Units:BO884, MD844, PE844, SQ801, TS814\n",
         "CODE:COLAPI",
         "CALL:Collapse Invest",
         "ADDR:6805 10TH AVE",
         "X:FAIRVIEW AVE and EAST WEST HWY",
         "CH:TGD3",
         "BOX:4411",
         "MAP:5409 B 5",
         "UNIT:BO884,MD844,PE844,SQ801,TS814");
 
     doTest("T10",
         "[CAD Feed] PIA, Acc w/Inj, 8796 ANNAPOLIS RD, PP, btwn SB CAP BELT HWY ONRP EB ANNAPOLIS RD and SB CAP BELT HW, TGA2, 2805, 5411 B 7, Police ProQA recommends dispatch at this time, Units:A833, E828\n",
         "CODE:PIA",
         "CALL:Acc w/Inj",
         "ADDR:8796 ANNAPOLIS RD",
         "X:SB CAP BELT HWY ONRP EB ANNAPOLIS RD and SB CAP BELT HW",
         "CH:TGA2",
         "BOX:2805",
         "MAP:5411 B 7",
         "UNIT:A833,E828");
 
     doTest("T11",
         "[CAD Feed] DEPFD, Departmental Acci, FIRESTATION 31 - BELTSVILLE, PP, at 4911 PRINCE GEORGES AVE, PP, btwn TALBOT AVE and QUEEN ANNE AVE, TGA2, 3130, 5288 E 4, U831 INVOLVED IN PDO...REQ PD AND NOTIFICATIONS ...NO INJ, Units:BO886, NSO, SO800, U831\n",
         "CODE:DEPFD",
         "CALL:Departmental Acci",
         "PLACE:FIRESTATION 31 - BELTSVILLE",
         "ADDR:4911 PRINCE GEORGES AVE",
         "CITY:btwn TALBOT AVE and QUEEN ANNE AVE",
         "CH:TGA2",
         "BOX:3130",
         "MAP:5288 E 4",
         "INFO:U831 INVOLVED IN PDO...REQ PD AND NOTIFICATIONS ...NO INJ",
         "UNIT:BO886,NSO,SO800,U831");
 
     doTest("T12",
         "[CAD Feed] DEPFD, Departmental Acci, FIRESTATION 31 - BELTSVILLE, PP, at 4911 PRINCE GEORGES AVE, PP, btwn TALBOT AVE and QUEEN ANNE AVE, TGA2, 3130, 5288 E 4, U831 INVOLVED IN PDO...REQ PD AND NOTIFICATIONS ...NO INJ, Units:BO886, NSO, SO800, U831\n",
         "CODE:DEPFD",
         "CALL:Departmental Acci",
         "PLACE:FIRESTATION 31 - BELTSVILLE",
         "ADDR:4911 PRINCE GEORGES AVE",
         "CITY:btwn TALBOT AVE and QUEEN ANNE AVE",
         "CH:TGA2",
         "BOX:3130",
         "MAP:5288 E 4",
         "INFO:U831 INVOLVED IN PDO...REQ PD AND NOTIFICATIONS ...NO INJ",
         "UNIT:BO886,NSO,SO800,U831");
 
     doTest("T13",
         "[CAD Feed] STREET, Street Alarm, 6900 JARRETT AVE, PP, btwn END and DUMFRIES ST, TC11, 2108, 5765 C 3, SMELLS BURNING IN THE UTILITY CLOSET, Units:BO885, E832B, PE842, SQ847, TW821\n",
         "CODE:STREET",
         "CALL:Street Alarm",
         "ADDR:6900 JARRETT AVE",
         "X:END and DUMFRIES ST",
         "CH:TC11",
         "BOX:2108",
         "MAP:5765 C 3",
         "INFO:SMELLS BURNING IN THE UTILITY CLOSET",
         "UNIT:BO885,E832B,PE842,SQ847,TW821");
 
     doTest("T14",
         "[CAD Feed] BUILDF, Building Fire, 905 MARCY AVE #3T, PP, btwn NEPTUNE AVE and DEAL DR, TGC3, 4203, 5649 A 10, Units:BO885, E829B, E832B, E847, PE842, SQ827, TK825, TW821\n",
         "CODE:BUILDF",
         "CALL:Building Fire",
         "ADDR:905 MARCY AVE",
         "APT:3T",
         "X:NEPTUNE AVE and DEAL DR",
         "CH:TGC3",
         "BOX:4203",
         "MAP:5649 A 10",
         "UNIT:BO885,E829B,E832B,E847,PE842,SQ827,TK825,TW821");
 
     doTest("T15",
         "[CAD Feed] ALS1, Medic Local, 8120 GAVIN ST, PP, btwn END and QUENTIN ST, TGA2, 4811, 5410 J 6, Medical ProQA recommends dispatch at this time, Units:A848, MD830\n",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:8120 GAVIN ST",
         "X:END and QUENTIN ST",
         "CH:TGA2",
         "BOX:4811",
         "MAP:5410 J 6",
         "UNIT:A848,MD830");
 
     doTest("T16",
         "[CAD Feed] BLS, BLS Amb, 9008 ANNAPOLIS RD, PP, btwn PRINCESS GARDEN PKY and LANHAM SEVERN RD, TGA2, 4805, 5411 B 7, MSPCP ONSCENE, 1 WITH A HEAD INJURY, REQ AMBO, Units:A813, E828\n",
         "CODE:BLS",
         "CALL:BLS Amb",
         "ADDR:9008 ANNAPOLIS RD",
         "X:PRINCESS GARDEN PKY and LANHAM SEVERN RD",
         "CH:TGA2",
         "BOX:4805",
         "MAP:5411 B 7",
         "INFO:MSPCP ONSCENE / 1 WITH A HEAD INJURY / REQ AMBO",
         "UNIT:A813,E828");
 
     doTest("T17",
         "[CAD Feed] ALS2, Medic Local, MAGNOLIA GARDENS NURSG HOME, PP, at 8200 GOOD LUCK RD, PP, btwn HANOVER PKY and MALLERY DR, TGA2, 4807, 5411 B 3, Medical ProQA recommends dispatch at this time, Units:A848, E848, MD818\n",
         "CODE:ALS2",
         "CALL:Medic Local",
         "PLACE:MAGNOLIA GARDENS NURSG HOME",
         "ADDR:8200 GOOD LUCK RD",
         "CITY:btwn HANOVER PKY and MALLERY DR",
         "CH:TGA2",
         "BOX:4807",
         "MAP:5411 B 3",
         "UNIT:A848,E848,MD818");
 
     doTest("T18",
         "[CAD Feed] PIAH, PIA Limited Access, O/L 49/ NEW HAMPSHIRE AVE, TGA2, MAMC, BETWEEN CO-LLINE AND NEW HAMPSHIRE A41 PE41 SQ14, Units:A841, BO886, PE841, SQ814\n",
         "CODE:PIAH",
         "CALL:PIA Limited Access",
         "ADDR:O & L 49 & NEW HAMPSHIRE AVE",  // Where is this?????
         "CH:TGA2",
         "BOX:MAMC",
         "MAP:BETWEEN CO-LLINE AND NEW HAMPSHIRE A41 PE41 SQ14",
         "UNIT:A841,BO886,PE841,SQ814");
 
     doTest("T19",
         "[CAD Feed] TOWNHF, Townhouse Fire, 626 BRIGHT SUN DR, PP, btwn LAZY DAY LN and END, TGA3, 4604, 5531 J 6, Fire ProQA recommends dispatch at this time, Units:BO881, E806, E833, E837, PE846, QT838, RE818, TK828\n",
         "CODE:TOWNHF",
         "CALL:Townhouse Fire",
         "ADDR:626 BRIGHT SUN DR",
         "X:LAZY DAY LN and END",
         "CH:TGA3",
         "BOX:4604",
         "MAP:5531 J 6",
         "UNIT:BO881,E806,E833,E837,PE846,QT838,RE818,TK828");
 
     doTest("T20",
         "[CAD Feed] ALS1, Medic Local, 9301 ANNAPOLIS RD, PP, btwn MORLEY RD and WASHINGTON BLVD, TGA2, 4802, 5411 D 6, Medical ProQA recommends dispatch at this time, Units:A848, MD830\n",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:9301 ANNAPOLIS RD",
         "X:MORLEY RD and WASHINGTON BLVD",
         "CH:TGA2",
         "BOX:4802",
         "MAP:5411 D 6",
         "UNIT:A848,MD830");
 
     doTest("T21",
         "[CAD Feed] ALS1, Medic Local, 5100 PHILADELPHIA WAY #B, PP, btwn PHILADELPHIA CT and END, TGA2, 4832, 5411 F 7, Medical ProQA recommends dispatch at this time, Units:A806, E828, MD812\n",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:5100 PHILADELPHIA WAY",
         "APT:B",
         "X:PHILADELPHIA CT and END",
         "CH:TGA2",
         "BOX:4832",
         "MAP:5411 F 7",
         "UNIT:A806,E828,MD812");
 
     doTest("T22",
         "[CAD Feed] STREET, Street Alarm, 4678 SUITLAND RD, PP, btwn HOMER AVE and HURON AVE, TC11, 1708, 5650 B 6, Units:BO883, E817, E826B, SQ827, TK829\n",
         "CODE:STREET",
         "CALL:Street Alarm",
         "ADDR:4678 SUITLAND RD",
         "X:HOMER AVE and HURON AVE",
         "CH:TC11",
         "BOX:1708",
         "MAP:5650 B 6",
         "UNIT:BO883,E817,E826B,SQ827,TK829");
 
     doTest("T23",
         "[CAD Feed] SHOT, Shooting, 7104 LORY LN, PP, btwn LOIS LN and LOIS LN, TGA2, 2824, 5410 H 6, Police ProQA recommends dispatch at this time, Units:A813, E828, MD830\n",
         "CODE:SHOT",
         "CALL:Shooting",
         "ADDR:7104 LORY LN",
         "X:LOIS LN and LOIS LN",
         "CH:TGA2",
         "BOX:2824",
         "MAP:5410 H 6",
         "UNIT:A813,E828,MD830");
 
     doTest("T24",
         "[CAD Feed] DEPFD, Departmental Acci, CLINTON FD 25, PP, at 9025 WOODYARD RD, PP, btwn CLINTON ST and PISCATAWAY RD, TGC2, 2507, 5766 G 8, MD825 BACKED INTO THE BUILDING...NO ONE INJURED.., Units:BO887, SBO, SDC, SO800\n",
         "CODE:DEPFD",
         "CALL:Departmental Acci",
         "PLACE:CLINTON FD 25",
         "ADDR:9025 WOODYARD RD",
         "CITY:btwn CLINTON ST and PISCATAWAY RD",
         "CH:TGC2",
         "BOX:2507",
         "MAP:5766 G 8",
         "INFO:MD825 BACKED INTO THE BUILDING...NO ONE INJURED..",
         "UNIT:BO887,SBO,SDC,SO800");
 
     doTest("T25",
         "[CAD Feed] BLS0, BLS Amb, 9801 LANHAM SEVERN RD, PP, btwn 98TH AVE and SANTA CRUZ ST, TGA2, 4804, 5411 F 4, Medical ProQA processing complete, Unit:A848\n",
         "CODE:BLS0",
         "CALL:BLS Amb",
         "ADDR:9801 LANHAM SEVERN RD",
         "X:98TH AVE and SANTA CRUZ ST",
         "CH:TGA2",
         "BOX:4804",
         "MAP:5411 F 4",
         "INFO:Medical ProQA processing complete",
         "UNIT:A848");
 
     doTest("T26",
         "[CAD Feed] BLS, BLS Amb, METRO NEW CARROLLTON - R, PP, at 4300 GARDEN CITY DR, PP, btwn WB JOHN HASON HWY OFRP GARDEN CITY DR and CORPORATE DR, TGA2, 2809, 5411 A 9, HAVE A EMPLOYEE WITH HEAT RELATED ILLNESS WILL BE AT THE METRO YARD NOT THE STATION ... IN THE ADMIN BUILDING, Units:A806, E828\n",
         "CODE:BLS",
         "CALL:BLS Amb",
         "PLACE:METRO NEW CARROLLTON - R",
         "ADDR:4300 GARDEN CITY DR",
         "CITY:btwn WB JOHN HASON HWY OFRP GARDEN CITY DR and CORPORATE DR",
         "CH:TGA2",
         "BOX:2809",
         "MAP:5411 A 9",
         "INFO:HAVE A EMPLOYEE WITH HEAT RELATED ILLNESS WILL BE AT THE METRO YARD NOT THE STATION ... IN THE ADMIN BUILDING",
         "UNIT:A806,E828");
 
     doTest("T27",
         "[CAD Feed] BLS0, BLS Amb, 9537 ELVIS LN, PP, btwn LURIA LN and ANITA LN, TGA2, 4822, 5411 F 3, Medical ProQA recommends dispatch at this time, Unit:A848\n",
         "CODE:BLS0",
         "CALL:BLS Amb",
         "ADDR:9537 ELVIS LN",
         "X:LURIA LN and ANITA LN",
         "CH:TGA2",
         "BOX:4822",
         "MAP:5411 F 3",
         "UNIT:A848");
 
     doTest("T28",
         "[CAD Feed] HOUSEG, House Gas Leak, 7412 HENDRICKS DR, PP, btwn END and 76TH AVE, TA11, 2819, 5410 J 8, Fire ProQA recommends dispatch at this time, Units:A809, BO882, E828, PE830, RE833, TK809\n",
         "CODE:HOUSEG",
         "CALL:House Gas Leak",
         "ADDR:7412 HENDRICKS DR",
         "X:END and 76TH AVE",
         "CH:TA11",
         "BOX:2819",
         "MAP:5410 J 8",
         "UNIT:A809,BO882,E828,PE830,RE833,TK809");
 
     doTest("T29",
         "[CAD Feed] HOUSEG, House Gas Leak, 7412 HENDRICKS DR, PP, btwn END and 76TH AVE, TA11, 2819, 5410 J 8, Fire ProQA recommends dispatch at this time, Units:A809, BO882, E828, PE830, RE833, TK809\n",
         "CODE:HOUSEG",
         "CALL:House Gas Leak",
         "ADDR:7412 HENDRICKS DR",
         "X:END and 76TH AVE",
         "CH:TA11",
         "BOX:2819",
         "MAP:5410 J 8",
         "UNIT:A809,BO882,E828,PE830,RE833,TK809");
 
     doTest("T30",
         "[CAD Feed] STREET, Street Alarm, 20317 AQUASCO RD, PP, btwn CROOM RD and AQUASCO FARM RD, TGC3, 3606, 6119 D 3, Fire ProQA recommends dispatch at this time, Units:BO887, CSQ02, CTK03, CTN02, E836, E840\n",
         "CODE:STREET",
         "CALL:Street Alarm",
         "ADDR:20317 AQUASCO RD",
         "X:CROOM RD and AQUASCO FARM RD",
         "CH:TGC3",
         "BOX:3606",
         "MAP:6119 D 3",
         "UNIT:BO887,CSQ02,CTK03,CTN02,E836,E840");
 
     doTest("T31",
         "[CAD Feed] PIA, Acc w/Inj, DOCTORS HOSPITAL, PP, at 8118 GOOD LUCK RD, PP, btwn HANOVER PKY and MALLERY DR, TGA2, 4807, 5411 B 3, CIT CALLED STATION ADV 9I INFRONT OF THE HOSPITAL ON GOOD LUCK, Units:A848, E828\n",
         "CODE:PIA",
         "CALL:Acc w/Inj",
         "PLACE:DOCTORS HOSPITAL",
         "ADDR:8118 GOOD LUCK RD",
         "CITY:btwn HANOVER PKY and MALLERY DR",
         "CH:TGA2",
         "BOX:4807",
         "MAP:5411 B 3",
         "INFO:CIT CALLED STATION ADV 9I INFRONT OF THE HOSPITAL ON GOOD LUCK",
         "UNIT:A848,E828");
 
     doTest("T32",
         "[CAD Feed] PIA, Acc w/Inj, DOCTORS HOSPITAL, PP, at 8118 GOOD LUCK RD, PP, btwn HANOVER PKY and MALLERY DR, TGA2, 4807, 5411 B 3, CIT CALLED STATION ADV 9I INFRONT OF THE HOSPITAL ON GOOD LUCK, Units:A848, E828\n",
         "CODE:PIA",
         "CALL:Acc w/Inj",
         "PLACE:DOCTORS HOSPITAL",
         "ADDR:8118 GOOD LUCK RD",
         "CITY:btwn HANOVER PKY and MALLERY DR",
         "CH:TGA2",
         "BOX:4807",
         "MAP:5411 B 3",
         "INFO:CIT CALLED STATION ADV 9I INFRONT OF THE HOSPITAL ON GOOD LUCK",
         "UNIT:A848,E828");
 
     doTest("T33",
         "[CAD Feed] LOC, Lock In/Out, 9443 ANNAPOLIS RD, PP, btwn FONTANA DR and CARTER AVE, TGA2, 4802, 5411 E 6, ANIMAL LOCKED INSIDE OF CAR,,, Unit:E828\n",
         "CODE:LOC",
         "CALL:Lock In/Out",
         "ADDR:9443 ANNAPOLIS RD",
         "X:FONTANA DR and CARTER AVE",
         "CH:TGA2",
         "BOX:4802",
         "MAP:5411 E 6",
         "INFO:ANIMAL LOCKED INSIDE OF CAR",
         "UNIT:E828");
 
   }
   
   @Test
   public void testCodeMessaging() {
 
     doTest("T1",
         "[PG09] ALS1, Medic Local, 3521 54TH AVE #1, PP, btwn MACBETH ST and NEWTON ST, TGA2, 0916, 5530 B 1, Medical ProQA recommends dispatch at this time, Units:A830, E809B, PE830",
         "CODE:ALS1",
         "CALL:Medic Local",
         "ADDR:3521 54TH AVE",
         "APT:1",
         "X:MACBETH ST and NEWTON ST",
         "CH:TGA2",
         "BOX:0916",
         "MAP:5530 B 1",
         "UNIT:A830,E809B,PE830");
 
     doTest("T2",
         "[PGWFD] F121510277: House Fire, 5311 LORRAINE DR, PP, btwn JOYCE DR and END, TGC3, 2931, Units:A827, EMSDO, MD829, SSO, WI",
         "ID:F121510277",
         "CALL:House Fire",
         "ADDR:5311 LORRAINE DR",
         "X:JOYCE DR and END",
         "CH:TGC3",
         "BOX:2931",
         "UNIT:A827,EMSDO,MD829,SSO,WI");
 
     doTest("T3",
         "[BATT6] F121520221: House Gas Leak, 3713 GREEN ASH CT, PP, btwn GREEN ASH LN and END, TA11, 4101, Units:BO886, PE712, PE841, TK812, TK831",
         "ID:F121520221",
         "CALL:House Gas Leak",
         "ADDR:3713 GREEN ASH CT",
         "X:GREEN ASH LN and END",
         "CH:TA11",
         "BOX:4101",
         "UNIT:BO886,PE712,PE841,TK812,TK831");
 
     doTest("T4",
         "[PGWFD] F121520220: Building Fire, 4221 BRANCH AVE, PP, btwn SB BRANCH AVE ONRP EB IVERSON ST and ST BARNABAS RD, TGC3, 2907, Units:A827, BO881, EMSDO, MD826, NSO, WI",
         "ID:F121520220",
         "CALL:Building Fire",
         "ADDR:4221 BRANCH AVE",
         "X:SB BRANCH AVE ONRP EB IVERSON ST and ST BARNABAS RD",
         "CH:TGC3",
         "BOX:2907",
         "UNIT:A827,BO881,EMSDO,MD826,NSO,WI");
 
     doTest("T5",
         "[SAFE] F121520220: Building Fire, 4221 BRANCH AVE, PP, btwn SB BRANCH AVE ONRP EB IVERSON ST and ST BARNABAS RD, TGC3, 2907, Units:A827, BO881, EMSDO, MD826, NSO, WI",
         "ID:F121520220",
         "CALL:Building Fire",
         "ADDR:4221 BRANCH AVE",
         "X:SB BRANCH AVE ONRP EB IVERSON ST and ST BARNABAS RD",
         "CH:TGC3",
         "BOX:2907",
         "UNIT:A827,BO881,EMSDO,MD826,NSO,WI");
 
     doTest("T6",
         "[PGWFD] F121520346: PIA w/Entrapment, 2254 HANNON ST, PP, btwn FORDHAM ST and 23RD AVE, TGB3, 3412, Unit:WI",
         "ID:F121520346",
         "CALL:PIA w/Entrapment",
         "ADDR:2254 HANNON ST",
         "X:FORDHAM ST and 23RD AVE",
         "CH:TGB3",
         "BOX:3412",
         "UNIT:WI");
 
     doTest("T7",
         "[BATT4] F121530476: Street Alarm, 4108 CRITTENDEN ST, HP, btwn 40TH PL and RHODE ISLAND AVE, TA11, 5511, Units:BO884, E809B, E855, TK809, TW807",
         "ID:F121530476",
         "CALL:Street Alarm",
         "ADDR:4108 CRITTENDEN ST",
         "X:40TH PL and RHODE ISLAND AVE",
         "CH:TA11",
         "BOX:5511",
         "UNIT:BO884,E809B,E855,TK809,TW807");
 
     doTest("T8",
         "[BATT6] F121540040: House Fire, 3307 MAJOR DENTON DR, PP, btwn CHERRY HILL RD and CHERRY HILL CT, TGA3, 4101, Units:BO886, E811B, E812B, E831, E834B, TK812, TK814, TW715",
         "ID:F121540040",
         "CALL:House Fire",
         "ADDR:3307 MAJOR DENTON DR",
         "X:CHERRY HILL RD and CHERRY HILL CT",
         "CH:TGA3",
         "BOX:4101",
         "UNIT:BO886,E811B,E812B,E831,E834B,TK812,TK814,TW715");
 
     doTest("T9",
         "[BATT4] F121540061: PIA w/Entrapment, 9200 NEW HAMPSHIRE AVE, PP, btwn METZEROTT RD and END, TGB3, 3410, Units:A834, BO884, E834B, MD712, SQ814",
         "ID:F121540061",
         "CALL:PIA w/Entrapment",
         "ADDR:9200 NEW HAMPSHIRE AVE",
         "X:METZEROTT RD and END",
         "CH:TGB3",
         "BOX:3410",
         "UNIT:A834,BO884,E834B,MD712,SQ814");
 
     doTest("T10",
         "[BATT6] F121540097: House Fire, 7525 LOCKMAN LN, PP, btwn CEDARHURST DR and LONGWOOD DR, TGA3, 3115, Units:BO886, E811B, E831, E849, PE841, TK812, TK814, TW810",
         "ID:F121540097",
         "CALL:House Fire",
         "ADDR:7525 LOCKMAN LN",
         "X:CEDARHURST DR and LONGWOOD DR",
         "CH:TGA3",
         "BOX:3115",
         "UNIT:BO886,E811B,E831,E849,PE841,TK812,TK814,TW810");
 
     doTest("T11",
         "[SAFE] F121540252: Water Rescue, NATIONAL HARBOR, PP, at 300 WATERFRONT ST, PP, TGD3, 5801, Units:BO885, BT847, BT856, E847, EMSDO, MD847, NSO, PA842, PE842, TR806, WI, WR856, WR857",
         "ID:F121540252",
         "CALL:Water Rescue",
         "PLACE:NATIONAL HARBOR",
         "ADDR:300 WATERFRONT ST",
         "CH:TGD3",
         "BOX:5801",
         "UNIT:BO885,BT847,BT856,E847,EMSDO,MD847,NSO,PA842,PE842,TR806,WI,WR856,WR857");
   
   }
  
   public static void main(String[] args) {
     new MDPrinceGeorgesCountyEParserTest().generateTests("T1");
   }
 }
