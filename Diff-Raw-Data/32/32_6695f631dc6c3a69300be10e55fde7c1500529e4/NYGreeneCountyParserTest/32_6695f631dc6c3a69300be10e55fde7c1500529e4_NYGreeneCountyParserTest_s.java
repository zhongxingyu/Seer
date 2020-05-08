 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYGreeneCountyParser;
 
 import org.junit.Test;
 
 
 public class NYGreeneCountyParserTest extends BaseParserTest {
   
   public NYGreeneCountyParserTest() {
     setParser(new NYGreeneCountyParser(), "GREENE COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "GREENE 911:WIRES >WIRES DOWN / ARCING 117 RIVER RD ATHENS BRAUMULLER, HELENE Map: Grids:0,0 Cad: 2010-0000033052",
         "CALL:WIRES DOWN/ARCING",
         "ADDR:117 RIVER RD",
         "CITY:ATHENS",
         "NAME:BRAUMULLER, HELENE",
         "ID:2010-0000033052");
     
     doTest("T2",
         "GREENE 911:ALARMF>FIRE ALARM 558 SLEEPY HOLLOW RD ATHENS ADT SECURITY-OP ANDREA- Map:62 Grids:0,0 Cad: 2010-0000033344",
         "CALL:FIRE ALARM",
         "ADDR:558 SLEEPY HOLLOW RD",
         "CITY:ATHENS",
         "NAME:ADT SECURITY-OP ANDREA-",
         "MAP:62",
         "ID:2010-0000033344");
     
     doTest("T3",
         "GREENE 911:SMOKE >SMOKE INVESTIGATION 2ND ST VENTE JOEAN Cad: 2010-0000034969",
         "CALL:SMOKE INVESTIGATION",
         "ADDR:2ND ST",
         "NAME:VENTE JOEAN",
         "ID:2010-0000034969");
     
     doTest("T4",
         "GREENE 911:PIAA >MVA WITH INJURIES MARKET ST ATHENS BUCHAKIAN,DAVID Cad: 2010-0000033852",
         "CALL:MVA WITH INJURIES",
         "ADDR:MARKET ST",
         "CITY:ATHENS",
         "NAME:BUCHAKIAN,DAVID",
         "ID:2010-0000033852");
     
     doTest("T5",
         "GREENE 911:STRUCT>STRUCTURE FIRE 408 W BRIDGE ST CATSKILL LAIRD, JACQUELYNN Cad: 2010-0000034875",
         "CALL:STRUCTURE FIRE",
         "ADDR:408 W BRIDGE ST",
         "CITY:CATSKILL",
         "NAME:LAIRD, JACQUELYNN",
         "ID:2010-0000034875");
 
     doTest("T6",
         "GREENE 911:ALARMF>FIRE ALARM 341 W MAIN ST CATSKILL VILLAGE #401 Map: Grids:, Cad: 2011-0000011139",
         "CALL:FIRE ALARM",
         "ADDR:341 W MAIN ST",
         "CITY:CATSKILL",
         "NAME:VILLAGE #401",
         "ID:2011-0000011139");
    
   }
   
   public static void main(String[] args) {
    new NYGreeneCountyParserTest().generateTests("T6", "CALL ADDR CITY NAME MAP ID");
   }
 }
