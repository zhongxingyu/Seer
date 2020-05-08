 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDAnneArundelCountyFireParserTest extends BaseParserTest {
   
   public MDAnneArundelCountyFireParserTest() {
     setParser(new MDAnneArundelCountyFireParser(), "ANNE ARUNDEL COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "SEVERN | ANNE ARUNDEL | FIRE ACTIVITY | I-97 & BENFIELD | BENFIELD RAMP CLOSED DUE TO BRUSH FIRE. | 31 |",
         "CITY:SEVERN",
         "CALL:FIRE ACTIVITY",
         "ADDR:I-97 & BENFIELD",
         "INFO:BENFIELD RAMP CLOSED DUE TO BRUSH FIRE.");
 
     doTest("T2",
         "GLEN BURNIE | ANNE ARUNDEL | SHOOTING | 1911 CRAIN HIGHWAY | 1 VICTIM SHOT AT THE MOOSE LODGE. | 31 |",
         "CITY:GLEN BURNIE",
         "CALL:SHOOTING",
         "ADDR:1911 CRAIN HIGHWAY",
         "INFO:1 VICTIM SHOT AT THE MOOSE LODGE.");
 
     doTest("T3",
         "ODENTON | ANNE ARUNDEL | *U/D* | CONWAY RD & MEYERS STATION | MULTI BARNS & STRUCTURES HAVE BEEN LOST. MULTI DWELLINGS AT RISK. | 31 |",
         "CITY:ODENTON",
         "CALL:*U/D*",
         "ADDR:CONWAY RD & MEYERS STATION",
         "INFO:MULTI BARNS & STRUCTURES HAVE BEEN LOST. MULTI DWELLINGS AT RISK.");
 
     doTest("T4",
         "HANOVER | ANNE ARUNDEL | 3RD ALM | 1703 MAPLE AVE | E291 O/L WITH A LARGE BRUSH FIRE. A HOUSE IS NOW ON FIRE. REQ THE 2ND & 3RD ALARM. MULTI EXPOSURE",
         "CITY:HANOVER",
         "CALL:3RD ALM",
         "ADDR:1703 MAPLE AVE",
         "INFO:E291 O/L WITH A LARGE BRUSH FIRE. A HOUSE IS NOW ON FIRE. REQ THE 2ND & 3RD ALARM. MULTI EXPOSURE");
 
     doTest("T5",
         "SEVERNA PARK | ANNE ARUNDEL | WORKING FIRE | 750 OAK GROVE CIRCLE | Q23 O/L WITH A WORKING BASEMENT FIRE. | 31",
         "CITY:SEVERNA PARK",
         "CALL:WORKING FIRE",
         "ADDR:750 OAK GROVE CIRCLE",
         "INFO:Q23 O/L WITH A WORKING BASEMENT FIRE.");
 
     doTest("T6",
         "LINTHICUM | ANNE ARUNDEL | AUTO ACCIDENT | AVIATION BLVD & TERMINAL RD | MVC INVOL BWI BUS. 1 INJ. | 31 |",
         "CITY:LINTHICUM",
         "CALL:AUTO ACCIDENT",
         "ADDR:AVIATION BLVD & TERMINAL RD",
         "INFO:MVC INVOL BWI BUS. 1 INJ.");
 
     doTest("T7",
         "DAVIDSONVILLE | ANNE ARUNDEL | *O/T AUTO* | BRICK CHURCH RD NEAR RT 214 | MEDIC 3 O/L SUV ON IT'S SIDE ON A GUARDRAIL.  OCC OUT,",
         "CITY:DAVIDSONVILLE",
         "CALL:*O/T AUTO*",
         "ADDR:BRICK CHURCH RD NEAR RT 214",
         "INFO:MEDIC 3 O/L SUV ON IT'S SIDE ON A GUARDRAIL.  OCC OUT,");
   }
 
   @Test
   public void testPrinceGeorgesParser() {
 
     doTest("T1",
         "CALVERTON CO.41 | PRINCE GEORGES | *2ND ALM* | 11338 CHERRY HILL RD | CHIEF 812 REPORTS A WORKING KITCHEN FIRE WITH RESCUES BEIN ",
         "CITY:CALVERTON",
         "UNIT:CO.41",
         "CALL:*2ND ALM*",
         "ADDR:11338 CHERRY HILL RD",
         "INFO:CHIEF 812 REPORTS A WORKING KITCHEN FIRE WITH RESCUES BEIN");
 
     doTest("T2",
         "BELTSVILLE 4101 | PRINCE GEORGES | *WSF* | 11338 CHERRY HILL RD | CMD HAS WORKING FIRE IN AN APT BUILDING | TG9 | MD227 AP16 | ",
         "CITY:BELTSVILLE",
         "BOX:4101",
         "CALL:*WSF*",
         "ADDR:11338 CHERRY HILL RD",
         "INFO:CMD HAS WORKING FIRE IN AN APT BUILDING");
 
    doTest("T3",
        "24-05 ACCOKEEK | PRINCE GEORGES | WSF | 16211 ACCOLAWN RD | RV FIRE NEXT TO THE HOUSE.  FIRE SPREADING TO HOUSE. | MD235 |",
        "CITY:ACCOKEEK",
        "UNIT:24-05",
        "CALL:WSF",
        "ADDR:16211 ACCOLAWN RD",
        "INFO:RV FIRE NEXT TO THE HOUSE.  FIRE SPREADING TO HOUSE.");

   }
   
   public static void main(String[] args) {
    new MDAnneArundelCountyFireParserTest().generateTests("T3");
   }
 }
