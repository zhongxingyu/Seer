 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TXGalvestonCountyParserTest extends BaseParserTest {
   
   public TXGalvestonCountyParserTest() {
     setParser(new TXGalvestonCountyParser(), "GALVESTON COUNTY", "TX");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         ".... (Santa Fe Fire) CAD:GRASS FIRE 12603 PONDEROSA DR",
         "SRC:Santa Fe Fire",
         "CALL:GRASS FIRE",
         "ADDR:12603 PONDEROSA DR");
 
     doTest("T2",
         ".... (Santa Fe Fire) CAD:GAS LEAK 11929 15TH ST SMELL OF GAS [10/27/11 13:40:00 JHILLMAN]",
         "SRC:Santa Fe Fire",
         "CALL:GAS LEAK",
         "ADDR:11929 15TH ST",
         "INFO:SMELL OF GAS",
         "DATE:10/27/11",
         "TIME:13:40:00");
 
     doTest("T3",
         ".... (Santa Fe Fire) CAD:GRASS FIRE 5021 AVE O ST LARGE GRASS FIRE [10/28/11 09:48:02 EATONR]",
         "SRC:Santa Fe Fire",
         "CALL:GRASS FIRE",
         "ADDR:5021 AVE O",
         "INFO:LARGE GRASS FIRE",
         "DATE:10/28/11",
         "TIME:09:48:02");
 
     doTest("T4",
         ".... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 4146 HOLLOWAY RD/HWY 6 ST",
         "SRC:Santa Fe Fire",
         "CALL:FIRE - NON SPECIFIC",
         "ADDR:4146 HOLLOWAY RD",
         "X:HWY 6",
         "INFO:ST");
 
     doTest("T5",
         ".... (Santa Fe Fire) CAD:POLE FIRE 7231 AVE M ST GRASS IS ON FIRE ALSO [10/31/11 13:17:49 EATONR] UTILITY POLE ON FIRE [10/31/11 13:17:31 EATONR]",
         "SRC:Santa Fe Fire",
         "CALL:POLE FIRE",
         "ADDR:7231 AVE M",
         "INFO:GRASS IS ON FIRE ALSO / UTILITY POLE ON FIRE",
         "DATE:10/31/11",
         "TIME:13:17:49");
 
     doTest("T6",
         ".... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 4607 GEORGIA ST LARGE TRASH FIRE UNATTENDED [11/03/11 07:59:46 JHILLMAN]",
         "SRC:Santa Fe Fire",
         "CALL:FIRE - NON SPECIFIC",
         "ADDR:4607 GEORGIA ST",
         "INFO:LARGE TRASH FIRE UNATTENDED",
         "DATE:11/03/11",
         "TIME:07:59:46");
 
     doTest("T7",
         ".... (Santa Fe Fire) CAD:LINE DOWN 1946 N FM 646 RD/HARRIETT LN LINE DOWN ON THE GROUND [11/03/11 09:55:42 JHILLMAN]",
         "SRC:Santa Fe Fire",
         "CALL:LINE DOWN",
         "ADDR:1946 N FM 646 RD",
         "MADDR:1946 N FM 646",
         "X:HARRIETT LN",
         "INFO:LINE DOWN ON THE GROUND",
         "DATE:11/03/11",
         "TIME:09:55:42");
 
     doTest("T8",
         ".... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 7600 AVE E ST between 32nd st and 33rd st..res is on Ave E..large fire appears to be speading to brush [11/30/11 20:49:27 BSCOTT]",
         "SRC:Santa Fe Fire",
         "CALL:FIRE - NON SPECIFIC",
         "ADDR:7600 AVE E",
         "INFO:between 32nd st and 33rd stres is on Ave Elarge fire appears to be speading to brush",
         "DATE:11/30/11",
         "TIME:20:49:27");
 
     doTest("T9",
         ".... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 7330 AVE M ST FIRE IN HOME HEATER CENTRAL UNIT SMOKE THEN FLAMES IS OUT NOW WITH WATER IN TRL [12/07/11 10:30:23 JHILLMAN]",
         "SRC:Santa Fe Fire",
         "CALL:FIRE - NON SPECIFIC",
         "ADDR:7330 AVE M",
         "INFO:FIRE IN HOME HEATER CENTRAL UNIT SMOKE THEN FLAMES IS OUT NOW WITH WATER IN TRL",
         "DATE:12/07/11",
         "TIME:10:30:23");
 
     doTest("T10",
         ".... (Santa Fe Fire) CAD:Update: FIRE - NON SPECIFIC 12619 HARRIETT LN SMALL CONT IN FENCE DUE TO WEATHER FIRE PUT OUT [12/07/11 12:58:06 JHILLMAN] IN AREA LOOKING OUT [12/07/11 12:53:05 JHILLMAN] NO CONTROL BURNS CALLED IN TODAY ON HARRIET LN MADE CONT WITH FI",
         "SRC:Santa Fe Fire",
         "CALL:Update: FIRE - NON SPECIFIC",
         "ADDR:12619 HARRIETT LN",
         "INFO:SMALL CONT IN FENCE DUE TO WEATHER FIRE PUT OUT / IN AREA LOOKING OUT / NO CONTROL BURNS CALLED IN TODAY ON HARRIET LN MADE CONT WITH FI",
         "DATE:12/07/11",
         "TIME:12:58:06");
 
     doTest("T11",
         ".... (Santa Fe Fire) LANDING ZONE 15117 WALNUT ST Event spawned from STROKE. [03/04/2012 21:08:14 BSCOTT] {SFMED1} NEED LF OUT HERE [03/04/12 21:07:26 BSCOTT] CALLER ADV HE ASKED HIS WIFE IF SHE KNEW WHO HE WAS AND SHE THOUGHT HE WAS AN EMT.. [0",
         "SRC:Santa Fe Fire",
         "CALL:LANDING ZONE",
         "ADDR:15117 WALNUT ST",
         "INFO:Event spawned from STROKE / {SFMED1} NEED LF OUT HERE / CALLER ADV HE ASKED HIS WIFE IF SHE KNEW WHO HE WAS AND SHE THOUGHT HE WAS AN EMT..",
         "DATE:03/04/2012",
         "TIME:21:08:14");
 
     doTest("T12",
         ".... (Santa Fe Fire) POLE FIRE 5318 IKE FRANK RD CLR ADV THERE WAS A TRANSFORMER ON FIRE AT THIS ADDRESS. AVD THAT IT IS OUT. HAS STOPPED SMOKING [03/04/12 07:48:36 JIDEMA]",
         "SRC:Santa Fe Fire",
         "CALL:POLE FIRE",
         "ADDR:5318 IKE FRANK RD",
         "INFO:CLR ADV THERE WAS A TRANSFORMER ON FIRE AT THIS ADDRESS AVD THAT IT IS OUT HAS STOPPED SMOKING",
         "DATE:03/04/12",
         "TIME:07:48:36");
  }
  
  @Test
  public void testTimJohnson() {
 
    doTest("T1",
        "FYI: SMOKE INVESTIGATION 2322 AVE D ST 518 IN [07/01/12 07:47:16 MULLC] 517 IN [07/01/12 07:47:10 MULLC] COMING FROM A WIRE ON A TELEPHONE OR ELECTRICAL POLE",
        "CALL:SMOKE INVESTIGATION",
        "ADDR:2322 AVE D",
        "INFO:518 IN / 517 IN / COMING FROM A WIRE ON A TELEPHONE OR ELECTRICAL POLE",
        "DATE:07/01/12",
        "TIME:07:47:16");
 
   }
   
   public static void main(String[] args) {
     new TXGalvestonCountyParserTest().generateTests("T1");
   }
 }
