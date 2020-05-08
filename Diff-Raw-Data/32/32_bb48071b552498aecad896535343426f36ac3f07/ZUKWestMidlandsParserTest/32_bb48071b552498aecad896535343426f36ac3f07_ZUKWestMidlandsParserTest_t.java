 package net.anei.cadpage.parsers.ZUK;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ZUKWestMidlandsParserTest extends BaseParserTest {
   
   public ZUKWestMidlandsParserTest() {
     setParser(new ZUKWestMidlandsParser(), "", "West Midlands");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "E:1699 (16:45) Unconscious DY8 4JA The Brambles Residential Home 69a Vicarage Road Amblecote Stourbridge (390070,285129)",
        "ID:E:1699",
         "TIME:16:45",
         "CALL:Unconscious",
         "PLACE:The Brambles Residential Home",
         "ADDR:DY8 4JA 69a Vicarage Road",
         "CITY:Amblecote Stourbridge",
         "MAP:390070,285129");
 
     doTest("T2",
         "E:989 (12:13) Abdo Pain DY8 1HB Highbury House Nursing Home The Old Rectory Parkfield Road Stourbridge (390537,284075)",
        "ID:E:989",
         "TIME:12:13",
         "CALL:Abdo Pain",
         "PLACE:Highbury House Nursing Home The Old Rectory",
         "ADDR:DY8 1HB Parkfield Road",
         "MADDR:Highbury House Nursing Home The Old Rectory,DY8 1HB Parkfield Road",
         "CITY:Stourbridge",
         "MAP:390537,284075");
 
     doTest("T3",
         "E:1131 (13:07) Allergy DY8 4AT 15 Jardine Close Amblecote Stourbridge (390184,284942)",
        "ID:E:1131",
         "TIME:13:07",
         "CALL:Allergy",
         "ADDR:DY8 4AT 15 Jardine Close",
         "CITY:Amblecote Stourbridge",
         "MAP:390184,284942");
 
     doTest("T4",
         "E:1112 (12:20) UNK DIAGNOSIS DY9 9AE 37 Grove Road Wollescote Stourbridge (392960,283446)",
        "ID:E:1112",
         "TIME:12:20",
         "CALL:UNK DIAGNOSIS",
         "ADDR:DY9 9AE 37 Grove Road",
         "CITY:Wollescote Stourbridge",
         "MAP:392960,283446");
 
     doTest("T5",
         "E:1481 (14:27) Burn DY8 3JS 14 Orwell Close Norton Stourbridge (388565,283699)",
        "ID:E:1481",
         "TIME:14:27",
         "CALL:Burn",
         "ADDR:DY8 3JS 14 Orwell Close",
         "CITY:Norton Stourbridge",
         "MAP:388565,283699");
 
     doTest("T6",
         "E:1302 (14:00) UNK DIAGNOSIS DY8 3ER 10 Manor Lane Norton Stourbridge (388759,283434)",
        "ID:E:1302",
         "TIME:14:00",
         "CALL:UNK DIAGNOSIS",
         "ADDR:DY8 3ER 10 Manor Lane",
         "CITY:Norton Stourbridge",
         "MAP:388759,283434");
 
     doTest("T7",
         "E:1444 (14:53) Chest Pain DY6 9PG 19 Barnett Green Kingswinford (388879,287721)",
        "ID:E:1444",
         "TIME:14:53",
         "CALL:Chest Pain",
         "ADDR:DY6 9PG 19 Barnett Green",
         "CITY:Kingswinford",
         "MAP:388879,287721");
 
     doTest("T8",
         "E:1294 (13:52) Convulsions DY8 4JU 11 Corbett Crescent Amblecote Stourbridge (390356,285008)",
        "ID:E:1294",
         "TIME:13:52",
         "CALL:Convulsions",
         "ADDR:DY8 4JU 11 Corbett Crescent",
         "CITY:Amblecote Stourbridge",
         "MAP:390356,285008");
 
     doTest("T9",
         "E:1517 (15:24) Chest Pain DY6 8JG 15 Wellington Close Kingswinford (389335,287797)",
        "ID:E:1517",
         "TIME:15:24",
         "CALL:Chest Pain",
         "ADDR:DY6 8JG 15 Wellington Close",
         "CITY:Kingswinford",
         "MAP:389335,287797");
 
     doTest("T10",
         "E:1642 (16:19) Falls DY9 0QE 17 Brook Crescent Hagley Stourbridge (390590,280007)",
        "ID:E:1642",
         "TIME:16:19",
         "CALL:Falls",
         "ADDR:DY9 0QE 17 Brook Crescent",
         "CITY:Hagley Stourbridge",
         "MAP:390590,280007");
 
     doTest("T11",
         "E:1075 (11:55) UNK DIAGNOSIS DY8 5UN 56 Brooklands Wordsley Stourbridge (389821,286440)",
        "ID:E:1075",
         "TIME:11:55",
         "CALL:UNK DIAGNOSIS",
         "ADDR:DY8 5UN 56 Brooklands",
         "CITY:Wordsley Stourbridge",
         "MAP:389821,286440");
     
   }
   
   public static void main(String[] args) {
     new ZUKWestMidlandsParserTest().generateTests("T1", "ID TIME CALL PLACE ADDR CITY MAP");
   }
 }
