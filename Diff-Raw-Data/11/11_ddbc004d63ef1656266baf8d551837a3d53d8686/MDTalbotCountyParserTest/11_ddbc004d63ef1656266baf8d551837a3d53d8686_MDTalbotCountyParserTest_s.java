 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDTalbotCountyParserTest extends BaseParserTest {
   
   public MDTalbotCountyParserTest() {
     setParser(new MDTalbotCountyParser(), "TALBOT COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Talbot 911:*D 61 B-ABDOMINAL PAIN 319 LINDEN AVE T60",
         "BOX:61",
         "CALL:B-ABDOMINAL PAIN",
         "ADDR:319 LINDEN AVE",
         "INFO:T60");
 
     doTest("T2",
         "Talbot 911:*D 70 A-CHEST PAIN 5656 BLACK WALNUT POINT RD BREATHING NORMAL <35 T70",
         "BOX:70",
         "CALL:A-CHEST PAIN",
         "ADDR:5656 BLACK WALNUT POINT RD",
         "INFO:BREATHING NORMAL <35 T70");
 
     doTest("T3",
         "Talbot 911:*D 66 MVA/PI OCEAN GTWY / GOLDSBOROUGH ST T60",
         "BOX:66",
         "CALL:MVA/PI",
         "ADDR:OCEAN GTWY & GOLDSBOROUGH ST",
         "INFO:T60");
 
     doTest("T4",
         "Talbot 911:*D 40C B-FALLS/BACK TRAUMA 103 MILES LN POSS DANGER BODY PT T40",
         "BOX:40C",
         "CALL:B-FALLS/BACK TRAUMA",
         "ADDR:103 MILES LN",
         "INFO:POSS DANGER BODY PT T40");
 
     doTest("T5",
         "Talbot 911:*D DOR ASSIST EMS 821 FIELDCREST DR D01",
         "BOX:DOR",
         "CALL:ASSIST EMS",
         "ADDR:821 FIELDCREST DR",
         "INFO:D01");
 
     doTest("T6",
         "Talbot 911:*D 61 POLICE REQUEST EMS IDLEWILD PARK @116 IDLEWILD AVE BRAVO RESPONSE T60",
         "BOX:61",
         "CALL:POLICE REQUEST EMS IDLEWILD PARK",
         "ADDR:116 IDLEWILD AVE",
         "INFO:BRAVO RESPONSE T60");
 
     doTest("T7",
         "Talbot 911:*D 43 SM NON-DWELLING FIRE 7011 HOPKINS NECK RD SHED/DETACH GARAGE T40",
         "BOX:43",
         "CALL:SM NON-DWELLING FIRE",
         "ADDR:7011 HOPKINS NECK RD",
         "INFO:SHED / DETACH GARAGE T40");
 
   }
   
   public static void main(String[] args) {
    new MDTalbotCountyParserTest().generateTests("T1", "BOX CALL ADDR INFO");
   }
 }
