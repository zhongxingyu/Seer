 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TXHumbleParserTest extends BaseParserTest {
   
   public TXHumbleParserTest() {
     setParser(new TXHumbleParser(), "HUMBLE", "TX");
   }
   
   @Test
   public void testStatusMsg() {
     doBadTest("(Chief ALT) [AVFD EMS TIMES] - Incident: 201117017 -- Unit: E-M19 Disp 18:20:53 -- Enroute: 18:22:50 -- Arrived: -- Transport: -- At Hosp: -- Available: 18:24:58");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[AVFD EMS] - Cardiac -- 17111 Dawn Shadows Dr, Humble -- Map: 376H- -- Xst's: Forest Rain Ln Old Arbor -- Units: E-M29 -- 201105590",
         "SRC:AVFD EMS",
         "CALL:Cardiac",
         "ADDR:17111 Dawn Shadows Dr",
         "CITY:Humble",
         "MAP:376H-",
         "X:Forest Rain Ln Old Arbor",
         "UNIT:E-M29",
         "ID:201105590");
 
     doTest("T2",
         "[AVFD EMS] - Diabetic Emergency -- 5310 Moonshadows Dr, Humble -- Map: 337S- -- Xst's: Pocito Ct Wellswood Ct -- Units: E-M29 -- 201105542",
         "SRC:AVFD EMS",
         "CALL:Diabetic Emergency",
         "ADDR:5310 Moonshadows Dr",
         "CITY:Humble",
         "MAP:337S-",
         "X:Pocito Ct Wellswood Ct",
         "UNIT:E-M29",
         "ID:201105542");
 
     doTest("T3",
         "[AVFD EMS] - Medical Call -- 6810 Amber Ash Ct, Humble -- Map: 337Q- -- Xst's: Crimson Oak Trl -- Units: E-M19 -- 201105487",
         "SRC:AVFD EMS",
         "CALL:Medical Call",
         "ADDR:6810 Amber Ash Ct",
         "CITY:Humble",
         "MAP:337Q-",
         "X:Crimson Oak Trl",
         "UNIT:E-M19",
         "ID:201105487");
 
     doTest("T4",
         "[AVFD FIRE] - Fire Alarm - Residential -- 19222 Pinewood Mist Ln, Humble -- Map: 338W- -- Xst's: Pinewood Canyon Ln Pinewo -- Units: ATFD E-E39 -- 201105452",
         "SRC:AVFD FIRE",
         "CALL:Fire Alarm - Residential",
         "ADDR:19222 Pinewood Mist Ln",
         "CITY:Humble",
         "MAP:338W-",
         "X:Pinewood Canyon Ln Pinewo",
         "UNIT:ATFD E-E39",
         "ID:201105452");
 
     doTest("T5",
         "[AVFD FIRE] - Motor Vehicle Incident -- E Fm 1960 & w Lake Houston P, Humble -- Map: 337Z- -- Xst's: W Lake Houston Pkwy Atasc -- Units: E-M19 E-E39 - 201105443",
         "SRC:AVFD FIRE",
         "CALL:Motor Vehicle Incident",
         "ADDR:E Fm 1960 & w Lake Houston P",
         "CITY:Humble",
         "MAP:337Z-",
         "X:W Lake Houston Pkwy Atasc",
         "UNIT:E-M19 E-E39",
         "ID:201105443");
 
     doTest("T6",
         "[AVFD FIRE][Repage] - Mvi - Unknown -- Will Clayton Pkwy & atascoci -- Map: 376C- -- Xst's: Atascocita Rd Hunters Ter -- Units: E-M29 E-E39 - 201105192",
         "SRC:AVFD FIRE",
         "CALL:Mvi - Unknown",
         "ADDR:Will Clayton Pkwy & atascoci",
         "MAP:376C-",
         "X:Atascocita Rd Hunters Ter",
         "UNIT:E-M29 E-E39",
         "ID:201105192");
 
     doTest("T7",
         "(Chief ALT) [AVFD FIRE][Repage] - Mutual Aid / Assist Agency -- 5711 Lakewater Dr -- Map: 338M- -- Water Wonderland -- Xst's: Running Water Dr Waterwoo -- Units: E-B3",
         "SRC:AVFD FIRE",
         "CALL:Mutual Aid / Assist Agency",
         "ADDR:5711 Lakewater Dr",
         "MAP:338M-",
         "PLACE:Water Wonderland",
         "X:Running Water Dr Waterwoo",
         "UNIT:E-B3");
 
     doTest("T8",
         "(Chief ALT) [EMS] - Medical Call - 19326 Aquatic Dr, Humble - Map: 378B- - Xst's: Red Sails Pass Rolling Ra - Units: E-M19 - 201139731",
         "SRC:EMS",
         "CALL:Medical Call",
         "ADDR:19326 Aquatic Dr",
         "CITY:Humble",
         "MAP:378B-",
         "X:Red Sails Pass Rolling Ra",
         "UNIT:E-M19",
         "ID:201139731");
 
     doTest("T8",
         "(Chief ALT) [EMS] - Injured Person - 13300 Will Clayton Pkwy, Humble - Map: 377C- - Atascocita High School - Xst's: Tabor Brook Dr Aerobic Av - Units: E-M19 - 2011398",
         "SRC:EMS",
         "CALL:Injured Person",
         "ADDR:13300 Will Clayton Pkwy",
         "CITY:Humble",
         "MAP:377C-",
         "PLACE:Atascocita High School",
         "X:Tabor Brook Dr Aerobic Av",
         "UNIT:E-M19",
         "ID:2011398");
 
     doTest("T9",
         "(Chief ALT) [FIRE] - Mutual Aid / Assist Agency - 142 Sunlit Park Dr, Humble - Map: 375V- - Xst's: Early Autumn Ct - Units: UT91 E-E39 - 201139857",
         "SRC:FIRE",
         "CALL:Mutual Aid / Assist Agency",
         "ADDR:142 Sunlit Park Dr",
         "CITY:Humble",
         "MAP:375V-",
         "X:Early Autumn Ct",
         "UNIT:UT91 E-E39",
         "ID:201139857");
 
     doTest("T10",
         "(Chief ALT) [FIRE] - Controlled Burn - Jodywood Dr & lonesome Woods - Map: 337Z- - Xst's: Lonesome Woods Trl Echo P - Units: E-E19 - 201139902",
         "SRC:FIRE",
         "CALL:Controlled Burn",
         "ADDR:Jodywood Dr & lonesome Woods",
         "MAP:337Z-",
         "X:Lonesome Woods Trl Echo P",
         "UNIT:E-E19",
         "ID:201139902");
 
     doTest("T11",
         "(Chief ALT) [EMS] - Medical Call - 7923 Palmer Place Ln, Humble - Map: 337Z- - Xst's: Match Play Dr - Units: E-M19 - 201139926",
         "SRC:EMS",
         "CALL:Medical Call",
         "ADDR:7923 Palmer Place Ln",
         "CITY:Humble",
         "MAP:337Z-",
         "X:Match Play Dr",
         "UNIT:E-M19",
         "ID:201139926");
 
     doTest("T12",
         "(Chief ALT) [EMS] - Assault - 17314 Wigeon Way Dr, Humble - Map: 376F- - Xst's: Shearwater Bend Dr - Units: E-M29 - 201139940",
         "SRC:EMS",
         "CALL:Assault",
         "ADDR:17314 Wigeon Way Dr",
         "CITY:Humble",
         "MAP:376F-",
         "X:Shearwater Bend Dr",
         "UNIT:E-M29",
         "ID:201139940");
  }
   
   public static void main(String[] args) {
     new TXHumbleParserTest().generateTests("T8");
   }
 }
