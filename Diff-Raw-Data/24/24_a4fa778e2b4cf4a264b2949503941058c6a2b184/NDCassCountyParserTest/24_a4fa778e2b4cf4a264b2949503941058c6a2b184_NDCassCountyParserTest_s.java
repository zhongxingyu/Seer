 package net.anei.cadpage.parsers.ND;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NDCassCountyParserTest extends BaseParserTest {
   
   public NDCassCountyParserTest() {
     setParser(new NDCassCountyParser(), "CASS COUNTY", "ND");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Accident - Injury 15605 29 ST SE CAST 02/10/12 16:21 CFS #: 430 10-50 ROLLOVER I  Nature Of Call: 10-50 ROLLOVER I  Nature Of Call: 10-50 ROLLOVER  9500-CASTFIR 6611-CASTAMB 608 8800-CASSRES",
         "CALL:Accident - Injury",
         "ADDR:15605 29 ST SE",
         "CITY:CASSELTON",
         "DATE:02/10/12",
         "TIME:16:21",
         "ID:430",
         "INFO:10-50 ROLLOVER I",
         "UNIT:9500-CASTFIR 6611-CASTAMB 608 8800-CASSRES");
 
     doTest("T2",
         "Accident - Injury 102 LANGER AVE S CAST CENEX PETRO SERVE - 102 LANGER AVE S - CAST04/03/12 12:15 CFS #: 967 MEDICAL  Dispatch received by unit 3523 Dispatch received by unit 3518  M WAS HIT BY HIS OWN VEH  Nature Of Call: MEDICAL  9500-CASTFIR 6611-CASTAMB 609 8800-CASSRES",
         "CALL:Accident - Injury",
         "ADDR:102 LANGER AVE S",
         "CITY:CASSELTON",
         "PLACE:CENEX PETRO SERVE",
         "DATE:04/03/12",
         "TIME:12:15",
         "ID:967",
         "INFO:MEDICAL  Dispatch received by unit 3523 Dispatch received by unit 3518  M WAS HIT BY HIS OWN VEH",
         "UNIT:9500-CASTFIR 6611-CASTAMB 609 8800-CASSRES");
 
     doTest("T3",
         "Residential Fire 321 8 AVE N CAST 03/27/12 20:57 CFS #: 787 RESIDENTIAL FIRE  SMELLS HOT UP IN COMPS DAUGHTERS ROOM  Nature Of Call: RESIDENTIAL FIRE  9501-CASTFIR",
         "CALL:Residential Fire",
         "ADDR:321 8 AVE N",
         "CITY:CASSELTON",
         "DATE:03/27/12",
         "TIME:20:57",
         "ID:787",
         "INFO:RESIDENTIAL FIRE  SMELLS HOT UP IN COMPS DAUGHTERS ROOM",
         "UNIT:9501-CASTFIR");
 
     doTest("T4",
         "Residential Fire 411 8 AVE S CAST 06/24/12 13:52 CFS #: 963 RESIDENTIAL FIRE  Nature Of Call: RESIDENTIAL FIRE  ELEMENT IN BASEMENT IT ON FIRE, CAN NOT TURN IT OFF  Nature Of Call: RMF  9500-CASTFIR",
         "CALL:Residential Fire",
         "ADDR:411 8 AVE S",
         "CITY:CASSELTON",
         "DATE:06/24/12",
         "TIME:13:52",
         "ID:963",
         "INFO:RESIDENTIAL FIRE  ELEMENT IN BASEMENT IT ON FIRE, CAN NOT TURN IT OFF",
         "UNIT:9500-CASTFIR");
 
     doTest("T5",
         "Grass Fire 15556 37 ST SE CAST GORDYS - 15556 37 ST SE - CAST06/27/12 21:32 CFS #: 962 GRASS FIRE  Nature Of Call: GRASS FIRE  9500-CASTFIR",
         "CALL:Grass Fire",
         "ADDR:15556 37 ST SE",
         "CITY:CASSELTON",
         "PLACE:GORDYS",
         "DATE:06/27/12",
         "TIME:21:32",
         "ID:962",
         "INFO:GRASS FIRE",
         "UNIT:9500-CASTFIR");
 
     doTest("T6",
         "Vehicle Fire CAST 322 mm I94 CASS W06/18/12 09:15 CFS #: 277 VEH ON FIRE  BLAZER  NO HAZMAT IN VEH.  UNSURE EXACTLY WHICH EXIT.  THINKS 324, BUT COULD BE 322  ON WB OFF RAMP.    FLAMES IN ENG  Nature Of Call: VEH ON FIRE  ",
         "CALL:Vehicle Fire CAST",
         "ADDR:322 mm I94",
         "MADDR:322 mm I 94",
         "DATE:06/18/12",
         "TIME:09:15",
         "ID:277",
         "INFO:VEH ON FIRE  BLAZER  NO HAZMAT IN VEH.  UNSURE EXACTLY WHICH EXIT.  THINKS 324, BUT COULD BE 322  ON WB OFF RAMP.    FLAMES IN ENG");
 
     doTest("T7",
         "Accident - Injury 14666 42 ST SE CHAF 07/10/12 18:37 CFS #: 23 3 WHEELER 1050I  Nature Of Call: 3 WHEELER 1050I  43YOA M, AWAKE AND BREATHING  Nature Of Call: MDF  9501-CASTFIR 6612-CASTAMB 608 8800-CASSRES",
         "CALL:Accident - Injury",
         "ADDR:14666 42 ST SE",
         "CITY:CHAFFEE",
         "DATE:07/10/12",
         "TIME:18:37",
         "ID:23",
         "INFO:3 WHEELER 1050I  43YOA M, AWAKE AND BREATHING",
         "UNIT:9501-CASTFIR 6612-CASTAMB 608 8800-CASSRES");
   }
 
   public static void main(String[] args) {
     new NDCassCountyParserTest().generateTests("T1", "CALL ADDR APT CITY PLACE DATE TIME ID INFO UNIT");
   }
 }
