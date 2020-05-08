 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDSomersetCountyParserTest extends BaseParserTest {
   
   public MDSomersetCountyParserTest() {
     setParser(new MDSomersetCountyParser(), "SOMERSET COUNTY", "MD");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "CAD:S5A;BACK PAIN;23196 PAUL BENTON CIR;DEAL",
        "SRC:S5A",
         "CALL:BACK PAIN",
         "ADDR:23196 PAUL BENTON CIR",
         "CITY:DEAL ISLAND");
 
     doTest("T2",
         "CAD:ST2;MEDICAL ASSIST;4248 STROBEL LN;CRIS",
        "SRC:ST2",
         "CALL:MEDICAL ASSIST",
         "ADDR:4248 STROBEL LN",
         "CITY:CRISFIELD");
 
     doTest("T3",
         "CAD:ST3;MEDICAL ASSIST;29762 LOVERS LN;MARI",
        "SRC:ST3",
         "CALL:MEDICAL ASSIST",
         "ADDR:29762 LOVERS LN",
         "CITY:MARION STATION");
 
     doTest("T4",
         "CAD:ST3;MEDICAL ASSIST;28259 CRISFIELD MARION RD;MARI",
        "SRC:ST3",
         "CALL:MEDICAL ASSIST",
         "ADDR:28259 CRISFIELD MARION RD",
         "CITY:MARION STATION");

    doTest("T5",
        "CAD:ST3;OUTSIDE FIRE;30549 MARUMSCO RD;MARI;[Fire Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Outside Fire. SMALL OUTSIDE fire (Unknown).Caller Statement",
        "SRC:ST3",
        "CALL:OUTSIDE FIRE",
        "ADDR:30549 MARUMSCO RD",
        "CITY:MARION STATION",
        "INFO:Outside Fire. SMALL OUTSIDE fire (Unknown).Caller Statement");

    doTest("T6",
        "CAD:MUTUAL AID ASSIST OUTSIDE AGEN;24666 SILVER LN",
        "CALL:MUTUAL AID ASSIST OUTSIDE AGEN",
        "ADDR:24666 SILVER LN");
   }
   
   public static void main(String[] args) {
    new MDSomersetCountyParserTest().generateTests("T5");
   }
 }
