 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PAYorkCountyAParserTest extends BaseParserTest {
   
   public PAYorkCountyAParserTest() {
     setParser(new PAYorkCountyAParser(), "YORK COUNTY", "PA");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "NORTH CODORUS TWP \n\n1983 SLAGEL RD STOVERSTOWN RD / VIEW DR FIRE STRUCT RESID Working Fire 12-01 S99-1 RIT1 17:05",
         "CITY:NORTH CODORUS TWP",
         "ADDR:1983 SLAGEL RD",
         "X:STOVERSTOWN RD / VIEW DR",
         "CALL:FIRE STRUCT RESID Working Fire",
         "MAP:12-01",
         "UNIT:S99-1 RIT1");
 
     doTest("T2",
         "YORK TWP COUNTRY MEADOWS \n\n2760 PINE GROVE RD 2760 PINE GROVE RD LEADER HEIGHTS RD, PINE GROVE CIR / PINE GROVE CMN FIRE AFA HIGH RISK general alm from the Leader Heights Building 19-04 FIRESTA19 E19 E18-1 E89-2 E35-2 TK19 TK34 EMSSTA18 A18 12:22",
         "CITY:YORK TWP",
         "PLACE:COUNTRY MEADOWS",
         "ADDR:2760 PINE GROVE RD",
         "APT:2760",
         "X:PINE GROVE RD / LEADER HEIGHTS RD",
         "CALL:PINE GROVE CIR / PINE GROVE CMN FIRE AFA HIGH RISK general alm from the Leader Heights Building",
         "MAP:19-04",
         "SRC:FIRESTA19",
         "UNIT:E19 E18-1 E89-2 E35-2 TK19 TK34 EMSSTA18 A18");
 
     doTest("T3",
         "PENN TWP \n\n1060 WILSON AVE OLD RIDGE RD / YORK ST, GRANDVIEW RD FIRE VEHICLE HR mulching machine on fire 49-17 FIRESTA49 E49-2 E46-3 E49-1",
         "CITY:PENN TWP",
         "ADDR:1060 WILSON AVE",
         "X:OLD RIDGE RD / YORK ST",
         "CALL:GRANDVIEW RD FIRE VEHICLE HR mulching machine on fire",
         "MAP:49-17",
         "SRC:FIRESTA49",
         "UNIT:E49-2 E46-3 E49-1");
 
     doTest("T4",
         "YORK CITY \n\n900 E KING ST S TREMONT ST / S CAMBRIDGE ST HAZMAT NO INJ hit gas service outside residence 99-06 FIRESTA99 E99-5 E99-1 TK99-1 DO99 15:23",
         "CITY:YORK CITY",
         "ADDR:900 E KING ST S",
         "X:TREMONT ST / S CAMBRIDGE ST",
         "CALL:HAZMAT NO INJ hit gas service outside residence",
         "MAP:99-06",
         "SRC:FIRESTA99",
         "UNIT:E99-5 E99-1 TK99-1 DO99");
 
     doTest("T5",
         "MANCH TWP METRO BANK MANCH TWP \n\n55 ARSENAL RD I 83X21BARSENALRD NB / I 83 NB VEH ACC INJ CL 2 30/F/leaking fluids 24-07 FIRESTA24 E24-1 R24  13:20",
         "CITY:MANCHESTER TWP",
         "PLACE:METRO BANK MANCH TWP",
         "ADDR:55 ARSENAL RD",
         "CALL:I 83X21BARSENALRD NB / I 83 NB VEH ACC INJ CL 2 30 / F / leaking fluids",
         "MAP:24-07",
         "SRC:FIRESTA24",
         "UNIT:E24-1 R24");
 
     doTest("T6",
         "HELLAM TWP \n\n4045 CODORUS FURNACE RD RIVER FARM RD / JENNIFER LN LANDING ZONE Jennifer and Furnace for LZ 21-13 R24  11:08",
         "CITY:HELLAM TWP",
         "ADDR:4045 CODORUS FURNACE RD",
         "X:RIVER FARM RD / JENNIFER LN",
         "CALL:LANDING ZONE Jennifer and Furnace for LZ",
         "MAP:21-13",
         "UNIT:R24");
 
     doTest("T7",
         "Fire Incident / YORK TWP I83 SB EXIT 16A\n\nI 83 SB I 83X16ASQUEENST SB FIRE VEHICLE HR cancel call 19-832 UFIRE24 UFIRE25 UEMS24  00:27¿\n\n\n",
         "CITY:YORK TWP",
         "PLACE:I83 SB EXIT 16A",
         "ADDR:I 83 SB",
         "MADDR:I83 SB EXIT 16A",
         "CALL:I 83X16ASQUEENST SB FIRE VEHICLE HR cancel call",
         "MAP:19-832",
         "UNIT:UFIRE24 UFIRE25 UEMS24");
 
     doTest("T8",
         "MANCH TWP CODORUS STONE COMPANY\n\n135 MUNDIS RACE RD EMIG RD, MUNDIS LN / DELLINGER RD FIRE AFA HIGH RISK / 2nd floor smoke /  24-03 FIRESTA24 E24-1 E89-3 TK24 EMSSTA24 A23-2  11:58",
         "CITY:MANCHESTER TWP",
         "PLACE:CODORUS STONE COMPANY",
         "ADDR:135 MUNDIS RACE RD",
         "X:EMIG RD / MUNDIS LN",
         "CALL:/ DELLINGER RD FIRE AFA HIGH RISK / 2nd floor smoke /",
         "MAP:24-03",
         "SRC:FIRESTA24",
         "UNIT:E24-1 E89-3 TK24 EMSSTA24 A23-2");
 
     doTest("T9",
         "MANCH TWP GRAHAM PKG 3280 FARMTRAIL RD\n\n3280 FARMTRAIL RD CHURCH RD / FARMBROOK LN FIRE AFA HIGH RISK general fire alarm 24-10 FIRESTA24 E24-1 E23-1 TK24 EMSSTA24 A24-2  04:41",
         "CITY:MANCHESTER TWP",
         "PLACE:GRAHAM PKG 3280 FARMTRAIL RD",
         "ADDR:3280 FARMTRAIL RD",
         "X:CHURCH RD / FARMBROOK LN",
         "CALL:FIRE AFA HIGH RISK general fire alarm",
         "MAP:24-10",
         "SRC:FIRESTA24",
         "UNIT:E24-1 E23-1 TK24 EMSSTA24 A24-2");
 
     doTest("T10",
         "Fire Incident / MANCH TWP \n\nCHURCH RD BOARD RD DEBRIS REMOVAL veh acc no inj leaking 24-02 FIRESTA24 E24-1  18:17¿\n\n\n",
         "CITY:MANCHESTER TWP",
         "ADDR:CHURCH RD & BOARD RD",
         "CALL:DEBRIS REMOVAL veh acc no inj leaking",
         "MAP:24-02",
         "SRC:FIRESTA24",
         "UNIT:E24-1");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "Subject:Fire Incident\nMANCH TWP    975 DETWILER DR BELAIR WAY / STILLMEADOW LN FIRE STRUCT RESID shed on fire 24-14 FIRESTA24 E24-1 E2-1 TK24 R",
         "CITY:MANCHESTER TWP",
         "ADDR:975 DETWILER DR",
         "X:BELAIR WAY / STILLMEADOW LN",
         "CALL:FIRE STRUCT RESID shed on fire",
         "MAP:24-14",
         "SRC:FIRESTA24",
         "UNIT:E24-1 E2-1 TK24 R");
 
     doTest("T2",
         "Subject:Fire Incident\nMANCH TWP CARS PLUS  2008 N  GEORGE ST WOODLAND AVE / I 83 SB, LIGHTNER RD, I 83X22NGEORGEST SB FIRE WIRES 24-07 FIRESTA",
         "CITY:MANCHESTER TWP",
         "PLACE:CARS PLUS",
         "ADDR:2008 N GEORGE ST",
         "X:WOODLAND AVE / I 83 SB",
         "CALL:LIGHTNER RD I 83X22NGEORGEST SB FIRE WIRES",
         "MAP:24-07",
         "UNIT:FIRESTA");
 
     doTest("T3",
         "Subject:Fire Incident\nMANCH TWP I83 NB EXIT EMIGSVILLE   I 83 NB I 83X24EMIGSVILLE NB DEBRIS REMOVAL non injury accident south of exit    24-83",
         "CITY:MANCHESTER TWP",
         "PLACE:I83 NB EXIT EMIGSVILLE",
         "MADDR:I83 NB EXIT EMIGSVILLE",
         "ADDR:I 83 NB",
         "CALL:I 83X24EMIGSVILLE NB DEBRIS REMOVAL non injury accident south of exit",
         "MAP:24-83");
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "0049000  HANOVER BORO GOLDEN EXCHANGE CONDO ASSOCIATION   9 2ND AVE EXCHANGE PL / W WALNUT ST FIRE AFA HIGH RISK General Alarm  46-02 FIRESTA46 E46-1 E49-1",
         "ID:0049000",
         "CITY:HANOVER BORO",
         "PLACE:GOLDEN EXCHANGE CONDO ASSOCIATION",
         "ADDR:9 2ND AVE",
         "X:EXCHANGE PL / W WALNUT ST",
         "CALL:FIRE AFA HIGH RISK General Alarm",
         "MAP:46-02",
         "SRC:FIRESTA46",
         "UNIT:E46-1 E49-1");
 
   }
   
   @Test
   public void testParser4() {
 
     doTest("T1",
         "FAIRVIEW TWP I83 SB EXIT FISHING CREEK   I 83 SB I 83X36FISHINGCREEK SB VEH ACC INJ CL 2 unk age pt/bleeding from face/unk dest 68-83 FIRESTA68 E68-2 R68  11:36",
         "SRC:FIRESTA68",
         "CITY:FAIRVIEW TWP",
         "PLACE:I83 SB EXIT FISHING CREEK",
         "ADDR:I 83 SB",
         "MADDR:I83 SB EXIT FISHING CREEK",
         "CALL:I 83X36FISHINGCREEK SB VEH ACC INJ CL 2 unk age pt / bleeding from face / unk dest",
         "MAP:68-83",
         "SRC:FIRESTA68",
         "UNIT:E68-2 R68");
 
     doTest("T2",
         "NEWBERRY TWP    46 MALL RD PRIVET DR / BILL DUGAN DR FIRE STRUCT RESID stove fire reported out still smoking  31-07 FIRESTA31 T31-1 E68-2 T28 T68 TK69 EMSSTA31 MICU31  12:51",
         "SRC:FIRESTA31",
         "CITY:NEWBERRY TWP",
         "ADDR:46 MALL RD",
         "X:PRIVET DR / BILL DUGAN DR",
         "CALL:FIRE STRUCT RESID stove fire reported out still smoking",
         "MAP:31-07",
         "SRC:FIRESTA31",
         "UNIT:T31-1 E68-2 T28 T68 TK69 EMSSTA31 MICU31");
 
     doTest("T3",
         "(Station 68) FAIRVIEW TWP    426 VALLEY RD SAND SPUR DR / TRAILER PARK FIRE STRUCT RESID Holding box to E69, E68-2,BLS 68-02 UFIRE68 UFIRE28  03:36",
         "SRC:Station 68",
         "CITY:FAIRVIEW TWP",
         "ADDR:426 VALLEY RD",
         "X:SAND SPUR DR",
         "CALL:/ TRAILER PARK FIRE STRUCT RESID Holding box to E69 E68-2 BLS",
         "MAP:68-02",
         "SRC:Station 68",
         "UNIT:UFIRE68 UFIRE28");
 
     doTest("T4",
         "(Station 68) FAIRVIEW TWP    56 MEADOWBROOK CT DEAD END / SHAUFFNERTOWN RD, DILLER RD FIRE STRUCT RESID Mobile home fire   68-05 FIRESTA68 E68-2 E69-1 T",
        "SRC:Station 68",
         "CITY:FAIRVIEW TWP",
         "ADDR:56 MEADOWBROOK CT",
         "CALL:DEAD END / SHAUFFNERTOWN RD DILLER RD FIRE STRUCT RESID Mobile home fire",
         "MAP:68-05",
         "SRC:FIRESTA68",
         "UNIT:E68-2 E69-1 T");
 
   }
   
   public static void main(String[] args) {
     new PAYorkCountyAParserTest().generateTests("T1", "SRC ID CITY PLACE ADDR APT X CALL MAP SRC UNIT");
   }
 }
