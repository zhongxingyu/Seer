 package net.anei.cadpage.parsers.DE;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class DEKentCountyBParserTest extends BaseParserTest {
   
   public DEKentCountyBParserTest() {
     setParser(new DEKentCountyBParser(), "KENT COUNTY", "DE");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Chief ALT) [40 EMS] - 26A10 - Sick Person - Unwell/Ill -- 2536 Skeeter Neck Rd, Frederica -- Xst's: Bowers Beach Rd / E Front St -- Caller: High Vue Logging Inc",
         "SRC:40 EMS",
         "CODE:26A10",
         "CALL:Sick Person - Unwell/Ill",
         "ADDR:2536 Skeeter Neck Rd",
         "CITY:Frederica",
         "X:Bowers Beach Rd / E Front St",
         "NAME:High Vue Logging Inc");
 
     doTest("T2",
         "(Chief ALT) [KCPS] - [911, Kent] NWS UPGRADED LAST TO A WARNING, WILL BE IN WESTERN KENT COUNTY AROUND 22:30 HRS.",
         "SRC:KCPS",
        "CALL:GENERAL ALERT",
         "PLACE:[911, Kent] NWS UPGRADED LAST TO A WARNING, WILL BE IN WESTERN KENT COUNTY AROUND 22:30 HRS.");
 
     doTest("T3",
         "(Chief ALT) [40 EMS] - 23C1V - Overdose/Poisoning -- 181 Holly Dr, Frederica -- High Point Mhp -- Xst's: Garden Pl / Maple Dr -- Caller: Marrillo Paul",
         "SRC:40 EMS",
         "CALL:23C1V - Overdose/Poisoning",
         "ADDR:181 Holly Dr",
         "CITY:Frederica",
         "PLACE:High Point Mhp",
         "X:Garden Pl / Maple Dr",
         "NAME:Marrillo Paul");
 
     doTest("T4",
         "(Chief ALT) [55 EMS 1] - 2D2 - Allergic Reaction -- 149 Juanita Dr, Magnolia -- London Village -- Xst's: Jeffrey Dr / Millchop Ln -- Caller: Price Laura",
         "CODE:2D2",
         "SRC:55 EMS 1",
         "CALL:Allergic Reaction",
         "ADDR:149 Juanita Dr",
         "CITY:Magnolia",
         "PLACE:London Village",
         "X:Jeffrey Dr / Millchop Ln",
         "NAME:Price Laura");
 
     doTest("T5",
         "(Chief ALT) [40 EMS] - 29B4 - MVC Unknown -- Mulberrie Point Rd, Frederica",
         "SRC:40 EMS",
         "CODE:29B4",
         "CALL:MVC Unknown",
         "ADDR:Mulberrie Point Rd",
         "CITY:Frederica");
 
     doTest("T6",
         "(Chief ALT) [40 EMS] - 10D2 - Chest Pains -- 196 N  Bayshore Dr, Frederica -- Bowers Beach -- Xst's: N Flack Ave / Main St -- Caller: Margaret",
         "SRC:40 EMS",
         "CODE:10D2",
         "CALL:Chest Pains",
         "ADDR:196 N  Bayshore Dr",
         "CITY:Frederica",
         "PLACE:Bowers Beach",
         "X:N Flack Ave / Main St",
         "NAME:Margaret");
 
     doTest("T7",
         "(Chief ALT) [40 EMS] - 26C2 - Sick Person -- 201 Hubbard Ave, Frederica -- Bowers Beach -- Xst's: Dead End / Canal St -- Caller: Ingle Heather",
         "SRC:40 EMS",
         "CODE:26C2",
         "CALL:Sick Person",
         "ADDR:201 Hubbard Ave",
         "CITY:Frederica",
         "PLACE:Bowers Beach",
         "X:Dead End / Canal St",
         "NAME:Ingle Heather");
 
     doTest("T8",
         "(Chief ALT) [40 EMS] - 10C4 - Chest Pains -- 302 Lake Shore Dr, Frederica -- High Point Mhp -- Xst's: Second St, Sycamore Dr / Lorna Ln -- Caller: Thom Ronald",
         "SRC:40 EMS",
         "CODE:10C4",
         "CALL:Chest Pains",
         "ADDR:302 Lake Shore Dr",
         "CITY:Frederica",
         "PLACE:High Point Mhp",
         "X:Second St, Sycamore Dr / Lorna Ln",
         "NAME:Thom Ronald");
 
     doTest("T9",
         "(Chief ALT) [40 EMS] - 6C1 - Breathing Problems -- 294 Lake Shore Dr, Frederica -- High Point Mhp -- Xst's: Kiwi Ct / Kylie Ln -- Caller: Hurd Shirley",
         "SRC:40 EMS",
         "CODE:6C1",
         "CALL:Breathing Problems",
         "ADDR:294 Lake Shore Dr",
         "CITY:Frederica",
         "PLACE:High Point Mhp",
         "X:Kiwi Ct / Kylie Ln",
         "NAME:Hurd Shirley");
 
     doTest("T10",
         "(Chief ALT) [40 Fire] - 17A2 - Falls - Non-Recent -- 254 Lake Shore Dr, Frederica -- High Point Mhp -- Xst's: Tyson Ln / Ibis Ct -- Caller: Spink Kimberley",
         "SRC:40 Fire",
         "CODE:17A2",
         "CALL:Falls - Non-Recent",
         "ADDR:254 Lake Shore Dr",
         "CITY:Frederica",
         "PLACE:High Point Mhp",
         "X:Tyson Ln / Ibis Ct",
         "NAME:Spink Kimberley");
 
     doTest("T11",
         "(Chief ALT) [40 Fire] - Outside Fire-Unknown 2124 BOWERS BEACH RD, Frederica -- Xst's: Grm Dr / Whitwell Delight Rd",
         "SRC:40 Fire",
         "CALL:Outside Fire-Unknown",
         "ADDR:2124 BOWERS BEACH RD",
         "CITY:Frederica",
         "X:Grm Dr / Whitwell Delight Rd");
   }
   
   public static void main(String[] args) {
     new DEKentCountyBParserTest().generateTests("T1");
   }
 }
     		
