 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCNashCountyParserTest extends BaseParserTest {
   
   public NCNashCountyParserTest() {
     setParser(new NCNashCountyParser(), "NASH COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "NASH911:* 7400 LANCASTER STORE RD* * * SPRING HOPE* * ALARM-FIRE* * SEC CENTRAL* * * * * * *",
         "ADDR:7400 LANCASTER STORE RD",
         "CITY:SPRING HOPE",
         "CALL:ALARM-FIRE",
         "PLACE:SEC CENTRAL");
 
     doTest("T2",
         "NASH911:* 6658 RED OAK RD* * * BATTLEBORO* * ALARM-FIRE* * ADT SECURITY* * * * * * *",
         "ADDR:6658 RED OAK RD",
         "CITY:BATTLEBORO",
         "CALL:ALARM-FIRE",
         "PLACE:ADT SECURITY");
 
     doTest("T3",
         "NASH911:* 2311 DUKE RD* * * NASHVILLE* * HEMORRHA-H* * EVANS* * * * * * *",
         "ADDR:2311 DUKE RD",
         "CITY:NASHVILLE",
         "CALL:HEMORRHA-H",
         "PLACE:EVANS");
 
     doTest("T4",
         "NASH911:* 5526 SYKES RD* * * NASHVILLE* * STROKE-H* * MILLS, URDIS* * * * * * *",
         "ADDR:5526 SYKES RD",
         "CITY:NASHVILLE",
         "CALL:STROKE-H",
         "NAME:MILLS, URDIS");
 
     doTest("T5",
         "NASH911:* 7164 RACE TRACK RD* * * CASTALIA* * STRUCTURE* * DAVID TALBOT* * * * * * *",
         "ADDR:7164 RACE TRACK RD",
         "CITY:CASTALIA",
         "CALL:STRUCTURE",
         "PLACE:DAVID TALBOT");
 
     doTest("T6",
         "NASH911:* 5988 SPRING CREEK RD* * * CASTALIA* * ALLERGY-H* * 704* * * * * * *",
         "ADDR:5988 SPRING CREEK RD",
         "CITY:CASTALIA",
         "CALL:ALLERGY-H",
         "PLACE:704");
 
     doTest("T7",
         "NASH911:* SYKES RD* * * SPRING HOPE* * *FIRE* * SCOTT PARKER* * * * * * *",
         "ADDR:SYKES RD",
         "CITY:SPRING HOPE",
         "CALL:*FIRE",
         "PLACE:SCOTT PARKER");
 
     doTest("T8",
         "NASH911:* 7671 EDWARDS RD* * * CASTALIA* * ALARM-FIRE* * GUARDIAN PROTECTION* * * * * * *",
         "ADDR:7671 EDWARDS RD",
         "CITY:CASTALIA",
         "CALL:ALARM-FIRE",
         "PLACE:GUARDIAN PROTECTION");
 
     doTest("T9",
         "NASH911:* 4306 SWIFT CREEK SCHOOL RD* * * WHITAKERS* * STRUCTURE* * ALLTEL WIRELESS* * * * * * *",
         "ADDR:4306 SWIFT CREEK SCHOOL RD",
         "CITY:WHITAKERS",
         "CALL:STRUCTURE",
         "PLACE:ALLTEL WIRELESS");
 
     doTest("T10",
         "NASH911:* 5388 TAYLORS STORE RD* * * NASHVILLE* * OUTSIDE FI* * HEDGEPETH, AVERINE* * * * * * *",
         "ADDR:5388 TAYLORS STORE RD",
         "CITY:NASHVILLE",
         "CALL:OUTSIDE FI",
         "NAME:HEDGEPETH, AVERINE");
 
     doTest("T11",
         "/ NASH911:* 5557 N HALIFAX RD* * * BATTLEBORO* * MVA PIN-H* * LARNELL HARDING* * * * * * *\n",
         "ADDR:5557 N HALIFAX RD",
         "CITY:BATTLEBORO",
         "CALL:MVA PIN-H",
         "PLACE:LARNELL HARDING");

    doTest("T12",
        "NASH911:* S NASH ST* * * MIDDLESEX* * CHEST-H* * * * * * * * *",
        "ADDR:S NASH ST",
        "CITY:MIDDLESEX",
        "CALL:CHEST-H");
}
   
 
   public static void main(String[] args) {
    new NCNashCountyParserTest().generateTests("T12");
   }
 }
