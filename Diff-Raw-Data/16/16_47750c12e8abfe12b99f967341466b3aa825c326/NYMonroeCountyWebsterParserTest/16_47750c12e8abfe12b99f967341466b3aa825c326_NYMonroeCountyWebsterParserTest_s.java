 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYMonroeCountyWebsterParserTest extends BaseParserTest {
   
   public NYMonroeCountyWebsterParserTest() {
     setParser(new NYMonroeCountyWebsterParser(), "MONROE COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "L: 701 AUDLEY END\nT: STRUCTURE FIRE\nO: \nB: \nC1: FINCHINGFIELD LA\nC2: WOODBRIDGE LA\nX: PLASTIC CONTAINER CAUGHT FIRE ON STOVE..COMP SAYS THAT CONTAINER IS SMOLD",
         "ADDR:701 AUDLEY END",
         "CALL:STRUCTURE FIRE",
         "X:FINCHINGFIELD LA & WOODBRIDGE LA",
         "INFO:PLASTIC CONTAINER CAUGHT FIRE ON STOVE..COMP SAYS THAT CONTAINER IS SMOLD");
 
     doTest("T2",
         "L: 286 PHILLIPS RD\nT: AUTOMATIC FIRE ALARM\nO: SMELTER RES - 265451\nB:\nC1: N CHIGWELL LA\nC2: MEADOW BREEZE LA\nX: FIRE ALRM ,GEN, C/C - ONLY CORNER STREET ALRM",
         "ADDR:286 PHILLIPS RD",
         "CALL:AUTOMATIC FIRE ALARM",
         "NAME:SMELTER RES - 265451",
         "X:N CHIGWELL LA & MEADOW BREEZE LA",
         "INFO:FIRE ALRM ,GEN, C/C - ONLY CORNER STREET ALRM");
 
     doTest("T3",
         "L: 736 BLUE CREEK DR\nT: AUTOMATIC FIRE ALARM\nO: FRANCO RESIDENCE\nB:\nC1: JOHN GLENN BL\nC2: LITHUANICA LA\nX: FIRE ALRM--SMOKE DETECTOR--UNK WHERE----C/C-----PH#",
         "ADDR:736 BLUE CREEK DR",
         "CALL:AUTOMATIC FIRE ALARM",
         "NAME:FRANCO RESIDENCE",
         "X:JOHN GLENN BL & LITHUANICA LA",
         "INFO:FIRE ALRM--SMOKE DETECTOR--UNK WHERE----C/C-----PH#");
 
     doTest("T4",
         "L: 1271 FAIRPRT NINE MILE PT RD\nT: MVAIA - W/INJURIES\nO:\nB:\nC1: BAINBRIDGE LA\nC2: PLANK RD\nX: PRECAUTIONARY FOR CHEST PAIN DUE TO MVA",
         "ADDR:1271 FAIRPRT NINE MILE PT RD",
         "CALL:MVAIA - W/INJURIES",
         "X:BAINBRIDGE LA & PLANK RD",
         "INFO:PRECAUTIONARY FOR CHEST PAIN DUE TO MVA");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "A: FAIF BOX: 0176\nL: 2 LEWIS ST\nT: CARDIAC/RESP-NOT BREATHING\nO: \nB: \nC1: POTTER PL\nC2: FILKINS ST\nX: 46 YO/M NOT BREATHING -- COLD TO THE TOUCH",
         "ADDR:2 LEWIS ST",
         "CALL:CARDIAC/RESP-NOT BREATHING",
         "X:POTTER PL & FILKINS ST",
         "INFO:46 YO/M NOT BREATHING -- COLD TO THE TOUCH");
 
   }
   
   public static void main(String[] args) {
     new NYMonroeCountyWebsterParserTest().generateTests("T1");
   }
 }
