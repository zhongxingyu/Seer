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
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "1 of 2\n" +
         "FRM:911CENTER@co.somerset.pa.us\n" +
         "SUBJ:FALLB >FALL INJURY BLS\n" +
         "MSG:911CENTER:969 BARN ST Apt: 101 Bldg XS: HIGHLAND AVE HOOVERSVILLE STANKO,\n" +
         "(Con't) 2 of 2\n" +
         "GEORGIANA 8147987022 Map: Grids:, Cad: 2012-0000017811\r\n" +
         "\r\n" +
         "(End)",
 
         "CALL:FALL INJURY BLS",
         "ADDR:969 BARN ST",
         "APT:101 Bldg",
         "CITY:HOOVERSVILLE",
         "X:HIGHLAND AVE",
         "NAME:STANKO, GEORGIANA",
         "PHONE:8147987022",
         "ID:2012-0000017811");
 
     doTest("T2",
         "[FIRES >STRUCTURE FIRE]  911CENTER:MAIN ST BOSWELL LEWIS,NANCY 8146299728 Cad: 2012-0000017870",
         "CALL:STRUCTURE FIRE",
         "ADDR:MAIN ST",
         "CITY:BOSWELL",
         "NAME:LEWIS,NANCY",
         "PHONE:8146299728",
         "ID:2012-0000017870");
   }
   
   @Test
  public void testSamDively() {

    doTest("T1",
        "(FIREB >FIRE BRUSH) 911CENTER:TOOLAND RD XS: COMPS RD FAIRHOPE BUD CLAYTON/ CSX RR 8002320144 Map: Grids:, Cad: 2012-0000019181",
        "CALL:FIRE BRUSH",
        "ADDR:TOOLAND RD",
        "MADDR:TOOLAND RD & COMPS RD",
        "X:COMPS RD",
        "NAME:FAIRHOPE BUD CLAYTON / CSX RR",
        "PHONE:8002320144",
        "ID:2012-0000019181");

  }
  
  @Test
   public void testActive911() {
 
     doTest("T1",
         "(BREATH>BREATHING PROBLEMS/DIFFICULTY) 911CENTER:148 Y ST XS: WILBUR RD STOYSTOWN OLSEN,LINDA 8148936267 Map: Grids:, Cad: 2012-0000017210",
         "CALL:BREATHING PROBLEMS/DIFFICULTY",
         "ADDR:148 Y ST",
         "CITY:STOYSTOWN",
         "X:WILBUR RD",
         "NAME:OLSEN,LINDA",
         "PHONE:8148936267",
         "ID:2012-0000017210");
 
     doTest("T2",
         "(ILLB  >ILL PERSON BLS) 911CENTER:492 WILBUR RD XS: A ST STOYSTOWN ED BARTA 8147986054 Map: Grids:, Cad: 2012-0000017365",
         "CALL:ILL PERSON BLS",
         "ADDR:492 WILBUR RD",
         "CITY:STOYSTOWN",
         "X:A ST",
         "NAME:ED BARTA",
         "PHONE:8147986054",
         "ID:2012-0000017365");
 
     doTest("T3",
         "(INJURB>INJURED PERSON BLS) 911CENTER:1414 PLANK RD XS: BLOOM RD HOOVERSVILLE CAMP HARMONY 8147985885 Map: Grids:, Cad: 2012-0000017394",
         "CALL:INJURED PERSON BLS",
         "ADDR:1414 PLANK RD",
         "CITY:HOOVERSVILLE",
         "X:BLOOM RD",
         "NAME:CAMP HARMONY",
         "PHONE:8147985885",
         "ID:2012-0000017394");
 
     doTest("T4",
         "(FALLB >FALL INJURY BLS) 911CENTER:969 BARN ST Apt: 101 Bldg XS: HIGHLAND AVE HOOVERSVILLE STANKO, GEORGIANA 8147987022 Map: Grids:, Cad: 2012-0000017811",
         "CALL:FALL INJURY BLS",
         "ADDR:969 BARN ST",
         "APT:101 Bldg",
         "CITY:HOOVERSVILLE",
         "X:HIGHLAND AVE",
         "NAME:STANKO, GEORGIANA",
         "PHONE:8147987022",
         "ID:2012-0000017811");
  
   }
   
   public static void main(String[] args) {
     new PASomersetCountyParserTest().generateTests("T1", "CALL ADDR APT CITY X PLACE NAME PHONE MAP ID");
   }
 }
