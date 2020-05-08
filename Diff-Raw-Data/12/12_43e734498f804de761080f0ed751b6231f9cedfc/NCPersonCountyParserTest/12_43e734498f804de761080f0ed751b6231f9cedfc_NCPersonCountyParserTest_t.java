 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCPersonCountyParserTest extends BaseParserTest {
   
   public NCPersonCountyParserTest() {
     setParser(new NCPersonCountyParser(), "PERSON COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "JBROWN:115 CROSS CREEK CT TIMBERLAKE 2011019922 18:27:47 FIRE STOVE",
         "ADDR:115 CROSS CREEK CT",
         "CITY:TIMBERLAKE",
         "ID:2011019922",
         "TIME:18:27:47",
         "CALL:FIRE STOVE");
 
     doTest("T2",
         "Jason_White:HURDLE MILLS RD / PAYNES TAVERN RD 2011019901 15:46:01 MVA PI CARD 29 car vs motorcycle",
         "ADDR:HURDLE MILLS RD & PAYNES TAVERN RD",
         "ID:2011019901",
         "TIME:15:46:01",
         "CALL:MVA PI CARD 29",
         "INFO:car vs motorcycle");
 
     doTest("T3",
         "Shutchings:GUESS RD / HURDLE MILLS RD 2011019810 00:35:44 MVA PI CARD 29",
         "ADDR:GUESS RD & HURDLE MILLS RD",
         "ID:2011019810",
         "TIME:00:35:44",
         "CALL:MVA PI CARD 29");
 
     doTest("T4",
         "Shutchings:115 JACOBS-MOORE LN HURDLE MILLS 2011019797 23:05:28 FIRE STRUCTURE FLAMES VISIBLE the house is on fire",
         "ADDR:115 JACOBS-MOORE LN",
         "CITY:HURDLE MILLS",
         "ID:2011019797",
         "TIME:23:05:28",
         "INFO:FIRE STRUCTURE FLAMES VISIBLE the house is on fire");
 
     doTest("T5",
         "Bmelton:HURDLE MILLS RD / POINDEXTER RD 2011019760 17:21:08 TREE DOWN",
         "ADDR:HURDLE MILLS RD & POINDEXTER RD",
         "ID:2011019760",
         "TIME:17:21:08",
         "CALL:TREE DOWN");
 
     doTest("T6",
         "BGILLISPIE:796 WHEELERS CHURCH RD HURDLE MILLS MDL 10C01 2011019661 05:54:13 CHEST PAIN CARD 10",
         "ADDR:796 WHEELERS CHURCH RD",
         "CITY:HURDLE MILLS",
         "CODE:10C01",
         "ID:2011019661",
         "TIME:05:54:13",
         "CALL:CHEST PAIN CARD 10");
 
     doTest("T7",
         "BROWN:225 CROWN BLVD TIMBERLAKE 2011020040 11:49:49 ALARMS-FIRE design works building 2 general fire alarm adt 8772857397",
         "ADDR:225 CROWN BLVD",
         "CITY:TIMBERLAKE",
         "ID:2011020040",
         "TIME:11:49:49",
         "CALL:ALARMS-FIRE",
         "INFO:design works building 2 general fire alarm adt 8772857397");
   }
   
  @Test
  public void testToddLane() {

    doTest("T1",
        "BOAKLEY:120 N OLD LANTERN RD TIMBERLAKE PLANTERS WAY X INT L/R CARRIAGE LN 18:44:47 FIRE STOVE GAS GRILL PROPANE TANK EXPLODED",
        "ADDR:120 N OLD LANTERN RD",
        "PLACE:TIMBERLAKE PLANTERS WAY X INT L / R CARRIAGE LN",
        "TIME:18:44:47",
        "INFO:FIRE STOVE GAS GRILL PROPANE TANK EXPLODED");
    
  }
  
 
   public static void main(String[] args) {
     new NCPersonCountyParserTest().generateTests("T1", "ADDR CITY PLACE CODE ID TIME CALL INFO");
   }
 }
