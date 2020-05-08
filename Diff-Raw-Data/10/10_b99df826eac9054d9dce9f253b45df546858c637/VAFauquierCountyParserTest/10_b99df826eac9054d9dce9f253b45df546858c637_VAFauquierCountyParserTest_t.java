 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class VAFauquierCountyParserTest extends BaseParserTest {
   
   public VAFauquierCountyParserTest() {
     setParser(new VAFauquierCountyParser(), "Fauquier County", "VA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:0301;TRAFFIC ACCIDENT;8323 WEST MAIN ST;WINCHESTER RD;FROST ST;CO3;11C",
         "BOX:0301",
         "CALL:TRAFFIC ACCIDENT",
         "ADDR:8323 WEST MAIN ST",
         "X:WINCHESTER RD & FROST ST",
         "UNIT:CO3",
         "CH:11C");
 
     doTest("T2",
         "CAD:0366;1050 I 66;179 I-66 W;I-66;CO3;11B",
         "BOX:0366",
         "CALL:1050 I 66",
         "ADDR:179 I-66 W",
         "MADDR:179 I 66 W",
         "X:I-66",
         "UNIT:CO3",
         "CH:11B");
 
     doTest("T3",
         "CAD:0410;PUBLIC SERVICE FIRE;5089 OLD TAVERN RD;OLD WINCHESTER RD;WINCHESTER RD;CO4",
         "BOX:0410",
         "CALL:PUBLIC SERVICE FIRE",
         "ADDR:5089 OLD TAVERN RD",
         "X:OLD WINCHESTER RD & WINCHESTER RD",
         "UNIT:CO4");
 
     doTest("T4",
         "CAD:0304;OUTSIDE/BRUSH/DUMPSTER FIRE;3800-BLK COBBLER MOUNTAIN RD;EASTVIEW LN;DOUBLE J LN;CO3;11B",
         "BOX:0304",
         "CALL:OUTSIDE/BRUSH/DUMPSTER FIRE",
         "ADDR:3800-BLK COBBLER MOUNTAIN RD",
         "MADDR:3800 COBBLER MOUNTAIN RD",
         "X:EASTVIEW LN & DOUBLE J LN",
         "UNIT:CO3",
         "CH:11B");
 
     doTest("T5",
         "CAD:0301;OUTSIDE/BRUSH/DUMPSTER FIRE;8307 EAST MAIN ST;MELODY LN;WINCHESTER RD;CO3;11B",
         "BOX:0301",
         "CALL:OUTSIDE/BRUSH/DUMPSTER FIRE",
         "ADDR:8307 EAST MAIN ST",
         "X:MELODY LN & WINCHESTER RD",
         "UNIT:CO3",
         "CH:11B");
 
     doTest("T6",
         "CAD:0367;1050 I 66;250 I-66 W;I-66;I-66;CO3",
         "BOX:0367",
         "CALL:1050 I 66",
         "ADDR:250 I-66 W",
         "MADDR:250 I 66 W",
         "X:I-66 & I-66",
         "UNIT:CO3");
 
     doTest("T7",
         "CAD:0101;FIRE ALARM INSTITUTIONAL;500 HOSPITAL DR;VETERANS DR;CO1",
         "BOX:0101",
         "CALL:FIRE ALARM INSTITUTIONAL",
         "ADDR:500 HOSPITAL DR",
         "X:VETERANS DR",
         "UNIT:CO1");
 
     doTest("T8",
         "CAD:0306;OUTSIDE/BRUSH/DUMPSTER FIRE;10747 MORELAND RD;FOX HOLLOW RD;CARRINGTON RD;CO3;11B",
         "BOX:0306",
         "CALL:OUTSIDE/BRUSH/DUMPSTER FIRE",
         "ADDR:10747 MORELAND RD",
         "X:FOX HOLLOW RD & CARRINGTON RD",
         "UNIT:CO3",
         "CH:11B");
 
     doTest("T9",
         "CAD:0366;VEHICLE FIRE - COMMERCIAL;160 I-66 E;I-66;CO3;11C",
         "BOX:0366",
         "CALL:VEHICLE FIRE - COMMERCIAL",
         "ADDR:160 I-66 E",
         "MADDR:160 I 66 E",
         "X:I-66",
         "UNIT:CO3",
         "CH:11C");
 
     doTest("T10",
         "CAD:0305;OUTDOOR ELECTRICAL HAZARD;10244 GLENARA LN;CREST HILL RD;CO3;11C",
         "BOX:0305",
         "CALL:OUTDOOR ELECTRICAL HAZARD",
         "ADDR:10244 GLENARA LN",
         "X:CREST HILL RD",
         "UNIT:CO3",
         "CH:11C");
 
     doTest("T11",
         "CAD:0311;PUBLIC SERVICE FIRE;3700-BLK GROVE LN;JUSTICE LN;ASHVILLE RD;CO3",
         "BOX:0311",
         "CALL:PUBLIC SERVICE FIRE",
         "ADDR:3700-BLK GROVE LN",
         "MADDR:3700 GROVE LN",
         "X:JUSTICE LN & ASHVILLE RD",
         "UNIT:CO3");
 
     doTest("T12",
         "CAD:0309;1050 WITH ENTRAPMENT/ROLLOVER;2787 LEEDS MANOR RD;RAVEN LN;MARSHALL SCHOOL LN;CO3;11C",
         "BOX:0309",
         "CALL:1050 WITH ENTRAPMENT/ROLLOVER",
         "ADDR:2787 LEEDS MANOR RD",
         "X:RAVEN LN & MARSHALL SCHOOL LN",
         "UNIT:CO3",
         "CH:11C");
 
     doTest("T13",
         "CAD:0303;SMOKE/ELECTRICAL ODORS OUTSIDE;4000-BLK ZULLA RD;LITTLE RIVER LN;SERENITY LN;CO3;11 B",
         "BOX:0303",
         "CALL:SMOKE/ELECTRICAL ODORS OUTSIDE",
         "ADDR:4000-BLK ZULLA RD",
         "MADDR:4000 ZULLA RD",
         "X:LITTLE RIVER LN & SERENITY LN",
         "UNIT:CO3",
         "CH:11 B");
 
     doTest("T14",
         "CAD:0301;SMOKE/ELECTRICAL ODORS OUTSIDE;8267-BLK EAST MAIN ST;OLD STOCKYARD RD;MELODY LN;CO3;11 B",
         "BOX:0301",
         "CALL:SMOKE/ELECTRICAL ODORS OUTSIDE",
         "ADDR:8267-BLK EAST MAIN ST",
         "MADDR:8267 EAST MAIN ST",
         "X:OLD STOCKYARD RD & MELODY LN",
         "UNIT:CO3",
         "CH:11 B");
 
     doTest("T15",
         "CAD:0101;STRUCTURE FIRE - COMMERCIAL;333 CARRIAGE HOUSE LN;WEST SHIRLEY AVE;VETERANS DR;CO1;11C",
         "BOX:0101",
         "CALL:STRUCTURE FIRE - COMMERCIAL",
         "ADDR:333 CARRIAGE HOUSE LN",
         "X:WEST SHIRLEY AVE & VETERANS DR",
         "UNIT:CO1",
         "CH:11C");
  }
  
  @Test
  public void testMutualAid() {
    setDefaults("", "VA");
    
    doTest("T1",
        "CAD:STRUCTURE FIRE - COMMERCIAL;14101 WHITNEY RD",
        "CALL:STRUCTURE FIRE - COMMERCIAL",
        "ADDR:14101 WHITNEY RD");
 
   }
   
   public static void main(String[] args) {
     new VAFauquierCountyParserTest().generateTests("T1");
   }
 }
