 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PAVenangoCountyParserTest extends BaseParserTest {
   
   public PAVenangoCountyParserTest() {
     setParser(new PAVenangoCountyParser(), "VENANGO COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "VENANGO 911:FTREDN>FIRE TREE DOWN PETROLEUM CENTER RD XS: EAGLE ROCK RD CORNPLANTER Cad: 2011-0000017294",
         "CALL:FIRE TREE DOWN",
         "ADDR:PETROLEUM CENTER RD",
         "MADDR:PETROLEUM CENTER RD & EAGLE ROCK RD",
         "X:EAGLE ROCK RD",
         "CITY:CORNPLANTER",
         "ID:2011-0000017294");
 
     doTest("T2",
         "VENANGO 911:MEDD >MEDICAL DELTA 10 VO TECH DR XS: PROSPECT ST OIL CITY PRESBYTERIAN HOMES IN THE PRES Map: Grids:, Cad: 2011-0000017234",
         "CALL:MEDICAL DELTA",
         "ADDR:10 VO TECH DR",
         "X:PROSPECT ST",
         "CITY:OIL CITY",
         "NAME:PRESBYTERIAN HOMES IN THE PRES",
         "ID:2011-0000017234");
 
     doTest("T3",
         "VENANGO 911:FTREDN>FIRE TREE DOWN STATE ROUTE 227 XS: GRANDVIEW RD CORNPLANTER JULIE PETERSON Cad: 2011-0000017275",
         "CALL:FIRE TREE DOWN",
         "ADDR:STATE ROUTE 227",
         "MADDR:STATE 227 & GRANDVIEW RD",
         "X:GRANDVIEW RD",
         "CITY:CORNPLANTER",
         "NAME:JULIE PETERSON",
         "ID:2011-0000017275");
 
     doTest("T4",
         "VENANGO 911:FSMOIN>FIRE SMOKE INVESTIGATION 22 WOODSIDE AVE XS: FAIRVIEW AVE CORNPLANTER MORELLI ELIZABETH Map: Grids:, Cad: 2011-0000016964",
         "CALL:FIRE SMOKE INVESTIGATION",
         "ADDR:22 WOODSIDE AVE",
         "X:FAIRVIEW AVE",
         "CITY:CORNPLANTER",
         "NAME:MORELLI ELIZABETH",
         "ID:2011-0000016964");
 
     doTest("T5",
         "VENANGO 911:ELIFEL>EMS LIFELINE CALL 122 PLUMMER ST XS: PEARL AVE OIL CITY DUNLAP ANGIE Map: Grids:, Cad: 2011-0000016903",
         "CALL:EMS LIFELINE CALL",
         "ADDR:122 PLUMMER ST",
         "X:PEARL AVE",
         "CITY:OIL CITY",
         "NAME:DUNLAP ANGIE",
         "ID:2011-0000016903");
 
     doTest("T6",
         "VENANGO 911:MED >MEDICAL GENERIC 1435 RUSSELL CORNERS RD XS: WHITE CITY RD CORNPLANTER ENOS BYRON A Map: Grids:, Cad: 2011-0000017129",
         "CALL:MEDICAL GENERIC",
         "ADDR:1435 RUSSELL CORNERS RD",
         "X:WHITE CITY RD",
         "CITY:CORNPLANTER",
         "NAME:ENOS BYRON A",
         "ID:2011-0000017129");
   }
   
   public static void main(String[] args) {
     new PAVenangoCountyParserTest().generateTests("T1", "CALL ADDR APT X CITY PLACE NAME PHONE MAP ID");
   }
 }
