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
 
   }
  
   public static void main(String[] args) {
     new MDPrinceGeorgesCountyEParserTest().generateTests("T1");
   }
 }
