 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYOrleansCountyParser;
 
 import org.junit.Test;
 
 
 public class NYOrleansCountyParserTest extends BaseParserTest {
   
   public NYOrleansCountyParserTest() {
     setParser(new NYOrleansCountyParser(), "ORLEANS COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "BREATHING PROBLEMS; 231 CHAMBERLAIN ST VAL; 17yom t/b    6-c-1; TEST FROM ORLEANS",
         "CALL:BREATHING PROBLEMS",
         "ADDR:231 CHAMBERLAIN ST",
         "CITY:ALBION",
         "INFO:17yom t/b / TEST FROM ORLEANS",
         "CODE:6-c-1");
 
     doTest("T2",
         "CARDIAC/RESPIRATORY ARREST; 16044 GLIDDEN RD TCL; 61 YOM UNATTENDED /COLD  IN WARM ENVIRNMENT JOHN SMITH",
         "CALL:CARDIAC/RESPIRATORY ARREST",
         "ADDR:16044 GLIDDEN RD",
         "CITY:HOLLEY",
         "INFO:61 YOM UNATTENDED /COLD  IN WARM ENVIRNMENT JOHN SMITH");
     
     doTest("T3",
         "FALLS; 1794 OAK ORCHARD RD TCR; 49 yom fell out of tub difficulty breathing   17d4  PRI 1",
         "CALL:FALLS",
         "ADDR:1794 OAK ORCHARD RD",
         "CITY:CARLTON",
         "INFO:49 yom fell out of tub difficulty breathing PRI 1",
         "CODE:17d4");
     
     doTest("T4",
         "BREATHING PROBLEMS; 126 E BANK ST VAL; 76 yom B/P SWEATS DIZZY  6D4  PRI 1;",
         "CALL:BREATHING PROBLEMS",
         "ADDR:126 E BANK ST",
         "CITY:ALBION",
         "INFO:76 yom B/P SWEATS DIZZY PRI 1",
         "CODE:6D4");
     
     doTest("T5",
         "HEART PROBLEMS/AICD; 15140 RIDGE RD TGN; 50 YOM RAPID HEART 180 BPS ; PRI 1 19c4;",
         "CALL:HEART PROBLEMS/AICD",
         "ADDR:15140 RIDGE RD",
         "CITY:ALBION",
         "INFO:50 YOM RAPID HEART 180 BPS / PRI 1",
         "CODE:19c4");
     
     doTest("T6",
         "TRAFFIC/TRANSPORTATION INCIDENTS; 14681 RIDGE RD TGN; REPORTED 2 CAR MINOR INJ MVA   29-B-1;",
         "CALL:TRAFFIC/TRANSPORTATION INCIDENTS",
         "ADDR:14681 RIDGE RD",
         "CITY:ALBION",
         "INFO:REPORTED 2 CAR MINOR INJ MVA",
         "CODE:29-B-1");
     
     doTest("T7",
         "SICK PERSON; 1579 E KENT RD TCR; 75 yom cathater removed pain PRI 3;",
         "CALL:SICK PERSON",
         "ADDR:1579 E KENT RD",
         "CITY:CARLTON",
         "INFO:75 yom cathater removed pain PRI 3");
 
     doTest("T8",
         "(FrmFireCntrl1) TRAUMATIC INJURIES; HIP INH 10741 LAKESHORE FL41A RD TYA;LYNDONVILLE EMS MEDINA AMB PR-3",
         "CALL:TRAUMATIC INJURIES",
         "ADDR:HIP INH 10741 LAKESHORE FL41A RD",
         "CITY:YATES",
         "INFO:LYNDONVILLE EMS MEDINA AMB PR-3");
 
     doTest("T9",
         "(FrmFireCntrl3) ALS ONLY RESPONSE; 4540 LINCOLN DR TGP; 58 yof trouble  breathing, decreased level of consciousness 6-d-1",
         "CALL:ALS ONLY RESPONSE",
         "ADDR:4540 LINCOLN DR",
         "CITY:GASPORT",
         "INFO:58 yof trouble  breathing, decreased level of consciousness",
         "CODE:6-d-1");
 
     doTest("T10",
         "(FrmFireCntrl 2) HEART PROBLEMS/AICD; 12469 LAKESHORE FL44 RD TYA; 80 yof high BP & pulse 19-c-6",
         "CALL:HEART PROBLEMS/AICD",
         "ADDR:12469 LAKESHORE FL44 RD",
         "CITY:YATES",
         "INFO:80 yof high BP & pulse",
         "CODE:19-c-6");
 
     doTest("T11",
         "(FrmFireCntrl1) ALS ONLY RESPONSE; 28 MAIN ST MDL; MEDINA ALS 83 YOF ABDOMINAL PAIN",
         "CALL:ALS ONLY RESPONSE",
         "ADDR:28 MAIN ST",
         "CITY:MIDDLEPORT",
         "INFO:MEDINA ALS 83 YOF ABDOMINAL PAIN");
 
     doTest("T12",
         "(FrmFireCntrl3) ACCIDENT VEH PDO; 1355 YATES CARLTON TL RD TCR; ATV ROLLOVER, NO PT FOUND COVA/CAFD",
         "CALL:ACCIDENT VEH PDO",
         "ADDR:1355 YATES CARLTON TL RD",
         "MADDR:1355 YATES CARLTON TRL RD",
         "CITY:CARLTON",
         "INFO:ATV ROLLOVER, NO PT FOUND COVA/CAFD");

    doTest("T13",
        "OCDISPATCH@ORLEANSNY.COM ABDOMINAL PAIN /PROBLEMS; 2103 NIA ORL CTYLINE RD TYA; 73 YOF CANCER PT   1-A-1 TXT STOP to opt-out",
        "CALL:ABDOMINAL PAIN /PROBLEMS",
        "ADDR:2103 NIAGARA ORLEANS COUNTYLINE RD",
        "CITY:YATES",
        "INFO:73 YOF CANCER PT",
        "CODE:1-A-1");
  }
   
   public static void main(String[] args) {
    new NYOrleansCountyParserTest().generateTests("T1", "CALL ADDR CITY INFO CODE");
   }
 }
