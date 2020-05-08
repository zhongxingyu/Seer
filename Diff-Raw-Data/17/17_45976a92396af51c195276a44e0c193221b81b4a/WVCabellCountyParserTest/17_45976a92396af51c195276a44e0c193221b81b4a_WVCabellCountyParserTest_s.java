 package net.anei.cadpage.parsers.WV;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class WVCabellCountyParserTest extends BaseParserTest {
   
   public WVCabellCountyParserTest() {
     setParser(new WVCabellCountyParser(), "CABELL COUNTY", "WV");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22225) CCERC911\nMSG:113030126/10-30-11/06:42:08/Other Fire/8160 FRAZIERS LN/LESAGE/LORENA LN/KATHI\n(Con't) 2 of 2\nLN/50YF/BLEEDING FROM A SURGICAL WOUND IN STOMACH//(End)",
         "ID:113030126",
         "DATE:10-30-11",
         "TIME:06:42:08",
         "CALL:Other Fire",
         "ADDR:8160 FRAZIERS LN",
         "CITY:LESAGE",
         "X:LORENA LN & KATHI LN",
         "INFO:50YF / BLEEDING FROM A SURGICAL WOUND IN STOMACH");
 
     doTest("T2",
         "1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22343) CCERC911\nMSG:113030199/10-30-11/14:16:29/Structure Fire/6419 RT 60/BARBOURSVILLE/GEORGES DRY\n(Con't) 2 of 2\nCLEANERS/FARMDALE RD/MAIN ST/201 231 ENRT/237/(End)",
         "ID:113030199",
         "DATE:10-30-11",
         "TIME:14:16:29",
         "CALL:Structure Fire",
         "ADDR:6419 RT 60",
         "CITY:BARBOURSVILLE",
         "PLACE:GEORGES DRY CLEANERS",
         "X:FARMDALE RD & MAIN ST",
         "INFO:201 231 ENRT / 237");
 
     doTest("T3",
         "1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22412) CCERC911\nMSG:113030260/10-30-11/16:53:24/Abdominal Pain/6430 OHIO RIVER RD APT1/LESAGE/TAN\n(Con't) 2 of 2\nZONE/DOUTHAT LN/NINE MILE RD/22YR FEMALE/27WKS PREGNANT/(End)",
         "ID:113030260",
         "DATE:10-30-11",
         "TIME:16:53:24",
         "CALL:Abdominal Pain",
         "ADDR:6430 OHIO RIVER RD",
         "APT:1",
         "CITY:LESAGE",
         "PLACE:TAN ZONE",
         "X:DOUTHAT LN & NINE MILE RD",
         "INFO:22YR FEMALE / 27WKS PREGNANT");
 
     doTest("T4",
         "1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22719) CCERC911\nMSG:113040332/10-31-11/20:53:50/MVA Inj./4805 HILLVIEW DR/HUNTINGTON/OHIO RIVER\n(Con't) 2 of 2\nRD/END/ONE VEH OVER THE HILL/ ADV ABOUT 50 YARD OVER A HILL/(End)",
         "ID:113040332",
         "DATE:10-31-11",
         "TIME:20:53:50",
         "CALL:MVA Inj.",
         "ADDR:4805 HILLVIEW DR",
         "CITY:HUNTINGTON",
         "X:OHIO RIVER RD & END",
         "INFO:ONE VEH OVER THE HILL / ADV ABOUT 50 YARD OVER A HILL");
 
     doTest("T5",
         "1 of 2\nFRM:dispatch@ccerc911.org\nSUBJ:(22809) CCERC911\nMSG:113050042/11-01-11/05:35:54/MVA Inj./5315 OHIO RIVER RD BLK/HUNTINGTON/THREE MILE\n(Con't) 2 of 2\nCREEK RD/MOBILE CITY RD/TRACTOR TRAILOR / TRUCK IS ON THE TRACKS/(End)",
         "ID:113050042",
         "DATE:11-01-11",
         "TIME:05:35:54",
         "CALL:MVA Inj.",
         "ADDR:5315 OHIO RIVER RD BLK",
         "MADDR:5315 OHIO RIVER RD",
         "CITY:HUNTINGTON",
         "X:THREE MILE CREEK RD & MOBILE CITY RD",
         "INFO:TRACTOR TRAILOR / TRUCK IS ON THE TRACKS");
     
   }
   
   @Test
   public void testJRVanOoteghem() {
 
     doTest("T1",
         "\" \" (29109) CCERC911\r\n" +
         ": 121420298/05-21-12/18:02:31/ARMED ROBBERY/1670 11TH AVE/HUNTINGTON/CYPRESS ST/17TH ST/MALE WAS HELD AT GUN POINT/ TXT STOP to opt-out",
 
         "ID:121420298",
         "DATE:05-21-12",
         "TIME:18:02:31",
         "CALL:ARMED ROBBERY",
         "ADDR:1670 11TH AVE",
         "CITY:HUNTINGTON",
         "X:CYPRESS ST & 17TH ST",
         "INFO:MALE WAS HELD AT GUN POINT");
 
     doTest("T2",
         "\" \" (29154) CCERC911\r\n" +
         ": 121420322/05-21-12/19:09:39/Jumper/9999 6TH ST/HUNTINGTON/6TH STREET BRIDGE/MALE ION THE BRIDGE THREATS TO JUMP/ TXT STOP to opt-out",
 
         "ID:121420322",
         "DATE:05-21-12",
         "TIME:19:09:39",
         "CALL:Jumper",
         "ADDR:9999 6TH ST",  // There is no 9999 6th ST
         "CITY:HUNTINGTON",
         "PLACE:6TH STREET BRIDGE",
         "X:MALE ION THE BRIDGE THREATS TO JUMP");
 
     doTest("T3",
         "\" \" (29096) CCERC911\r\n" +
         ": 121420255/05-21-12/16:31:23/Fire Alarm/7850 OHIO RIVER RD/LESAGE/RUSTOLEUM/CLUTTS HILL RD/OLD RT 2/GENERAL/ TXT STOP to opt-out",
 
         "ID:121420255",
         "DATE:05-21-12",
         "TIME:16:31:23",
         "CALL:Fire Alarm",
         "ADDR:7850 OHIO RIVER RD",
         "CITY:LESAGE",
         "PLACE:RUSTOLEUM",
         "X:CLUTTS HILL RD & OLD RT 2",
         "INFO:GENERAL");
 
     doTest("T4",
         "\" \" (29283) CCERC911\r\n" +
         ": 121420376/05-21-12/20:57:24/PURSUIT/900 8TH ST INTERSECTN/HUNTINGTON/9TH AVE/SB ON 8TH ST// TXT STOP to opt-out",
 
         "ID:121420376",
         "DATE:05-21-12",
         "TIME:20:57:24",
         "CALL:PURSUIT",
         "ADDR:900 8TH ST",
         "CITY:HUNTINGTON",
         "X:9TH AVE & SB ON 8TH ST");
 
     doTest("T5",
         "\" \" (29458) CCERC911\r\n" +
         ": 121430056/05-22-12/03:56:08/Shooting/200 8TH AVE INTERSECTN/HUNTINGTON/2ND ST/COMP ADV MALE SUBJ WAS SHOOT IN THE LEG VICT IS ON THE/ TXT STOP to opt-out",
 
         "ID:121430056",
         "DATE:05-22-12",
         "TIME:03:56:08",
         "CALL:Shooting",
         "ADDR:200 8TH AVE",
         "CITY:HUNTINGTON",
         "X:2ND ST",
         "INFO:COMP ADV MALE SUBJ WAS SHOOT IN THE LEG VICT IS ON THE");
 
     doTest("T6",
         "\" \" (29217) CCERC911\r\n" +
         ": 121420332/05-21-12/19:26:29/Structure Fire/722 4TH ST/HUNTINGTON/RR TRACKS/8TH AVE/SEND E1 PER HFD43/ TXT STOP to opt-out",
 
         "ID:121420332",
         "DATE:05-21-12",
         "TIME:19:26:29",
         "CALL:Structure Fire",
         "ADDR:722 4TH ST",
         "CITY:HUNTINGTON",
        "PLACE:RR TRACKS",
        "X:8TH AVE",
         "INFO:SEND E1 PER HFD43");
 
     doTest("T7",
         "\" \" (29364) CCERC911\r\n" +
         ": 121430008/05-22-12/00:20:02/Structure Fire/1801 12TH AVE/HUNTINGTON/18TH ST/19TH ST/VACANT HOUSE // TXT STOP to opt-out",
 
         "ID:121430008",
         "DATE:05-22-12",
         "TIME:00:20:02",
         "CALL:Structure Fire",
         "ADDR:1801 12TH AVE",
         "CITY:HUNTINGTON",
         "X:18TH ST & 19TH ST",
         "INFO:VACANT HOUSE");
 
     doTest("T8",
         "\" \" (29169) CCERC911\r\n" +
         ": 121420325/05-21-12/19:11:12/Smoke Investigation/7800 OHIO RIVER RD INTERSECTN/LESAGE/OLD RT 2/JUST BEFORE THIS/SEVERAL MEN STANDING BY THE ROADWAY AND/ TXT STOP to opt-out",
 
         "ID:121420325",
         "DATE:05-21-12",
         "TIME:19:11:12",
         "CALL:Smoke Investigation",
         "ADDR:7800 OHIO RIVER RD",
         "CITY:LESAGE",
         "X:OLD RT 2",
         "INFO:JUST BEFORE THIS / SEVERAL MEN STANDING BY THE ROADWAY AND");
 
   }
   
   public static void main(String[] args) {
     new WVCabellCountyParserTest().generateTests("T1");
   }
 }
