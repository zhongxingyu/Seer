 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PASomersetCountyParserTest extends BaseParserTest {
   
   public PASomersetCountyParserTest() {
     setParser(new PASomersetCountyParser(), "SOMERSET COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Code:  MVA   >MOTOR VEHICLE ACCIDENT\nCode Detail:  Address:  XS:  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #:  2011-0000029533\n\nUnformatted Message: 9-1-1 CENTER:GRANT ST AT MAIN ST MEYERSDALE RICH SECHLER 8142891447 Cad: 2011-0000029533",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "ADDR:GRANT ST & MAIN ST",
         "CITY:MEYERSDALE",
         "NAME:RICH SECHLER",
         "PHONE:8142891447",
         "ID:2011-0000029533");
 
     doTest("T2",
         "Code:  FIRES >STRUCTURE FIRE\nCode Detail:  Address: 9-1-1 CENTER:605 PINEY RUN RD  XS:  ENGLES MILL RD SALISBURY WEIMER SUSAN  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #: 2011-0000028647\n\nUnformatted Message: 9-1-1 CENTER:605 PINEY RUN RD XS: ENGLES MILL RD SALISBURY WEIMER SUSAN 8146624073 Map: Grids:, Cad: 2011-0000028647",
         "CALL:STRUCTURE FIRE",
         "ADDR:605 PINEY RUN RD",
         "CITY:SALISBURY",
         "X:ENGLES MILL RD",
         "NAME:WEIMER SUSAN",
         "PHONE:8146624073",
         "ID:2011-0000028647");
 
     doTest("T3",
         "Code:  FIRES >STRUCTURE FIRE\nCode Detail:  Address: 9-1-1 CENTER:5467 MOUNT DAVIS RD  XS:  SAINT PAUL RD MEYERSDALE BENDER LINDA  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #: 2011-0000030088\n\nUnformatted Message: 9-1-1 CENTER:5467 MOUNT DAVIS RD XS: SAINT PAUL RD MEYERSDALE BENDER LINDA 8146622352 Map: Grids:, Cad: 2011-0000030088",
         "CALL:STRUCTURE FIRE",
         "ADDR:5467 MOUNT DAVIS RD",
         "CITY:MEYERSDALE",
         "X:SAINT PAUL RD",
         "NAME:BENDER LINDA",
         "PHONE:8146622352",
         "ID:2011-0000030088");
 
     doTest("T4",
         "Code:  SPILL >SPILL (TYPE)\nCode Detail:  Address: 9-1-1 CENTER:1041 SHAW MINES RD  XS:  City:  Caller: Caller Ph:  Map:  Grid: CAD INC #:  2011-0000029694\n\nUnformatted Message: 9-1-1 CENTER:1041 SHAW MINES RD XS: SKYLINE DR MEYERSDALE 130 Map: Grids:, Cad: 2011-0000029694",
         "CALL:SPILL (TYPE)",
         "ADDR:1041 SHAW MINES RD",
         "CITY:MEYERSDALE",
         "X:SKYLINE DR",
         "NAME:130",
         "ID:2011-0000029694");
 
     doTest("T5",
         "Code:  TC    >TRAFFIC CONTROL\nCode Detail:  Address: 9-1-1 CENTER:1222 GLADE CITY RD  XS:  DEAL RD MEYERSDALE LOTTIG,C  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #: 2011-0000030890\n\nUnformatted Message: 9-1-1 CENTER:1222 GLADE CITY RD XS: DEAL RD MEYERSDALE LOTTIG,C 8146348503 Map: Grids:, Cad: 2011-0000030890",
         "CALL:TRAFFIC CONTROL",
         "ADDR:1222 GLADE CITY RD",
         "CITY:MEYERSDALE",
         "X:DEAL RD",
         "NAME:LOTTIG,C",
         "PHONE:8146348503",
         "ID:2011-0000030890");
   
   }
   
   public static void main(String[] args) {
    new PASomersetCountyParserTest().generateTests("T1", "CALL ADDR CITY X PLACE NAME PHONE MAP ID");
   }
 }
