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
   }
   
   public static void main(String[] args) {
     new TXHumbleParserTest().generateTests("T1");
   }
 }
