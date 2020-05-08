 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PAMercerCountyParserTest extends BaseParserTest {
   
   public PAMercerCountyParserTest() {
     setParser(new PAMercerCountyParser(), "MERCER COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: N BROAD ST/LINCOLN AVE GROV EID: 2360315 TYPE CODE: MVU CALLER NAME: LARRY GRACE CALLER ADDR: 522 E MAIN ST E",
         "ADDR:N BROAD ST & LINCOLN AVE",
         "CITY:GROVE CITY",
         "ID:2360315",
         "CALL:Motor Vehicle Crash w/ unknown injuries",
         "NAME:LARRY GRACE");
 
     doTest("T2",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 666 FLOWER ST GROV EID: 2363268 TYPE CODE: COM CALLER NAME:  CALLER ADDR: 109 GEORGE JUNIOR RD PINE TIME: 20:",
         "ADDR:666 FLOWER ST",
         "CITY:GROVE CITY",
         "ID:2363268",
         "CALL:Commercial Fire",
         "TIME:20:");
 
     doTest("T3",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 597 CLARKSVILLE RD COOL: alias 597 RT 258 0 COOL EID: 2366343 TYPE CODE: HF CALLER NAME: ",
         "ADDR:597 CLARKSVILLE RD",
         "CITY:COOLSPRING TWP",
         "PLACE:597 RT 258 0",
         "ID:2366343",
         "CALL:House fire");
 
     doTest("T4",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 129 E PINE ST GROV: @PENN GROVE HOTEL EID: 2368267 TYPE CODE: FALM CALLER NAME: VECTOR-42",
         "ADDR:129 E PINE ST",
         "CITY:GROVE CITY",
         "PLACE:PENN GROVE HOTEL",
         "ID:2368267",
         "CALL:Fire alarm",
         "NAME:VECTOR-42");
 
     doTest("T5",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: FOREST DR/LINCOLN AVE GROV EID: 2368281 TYPE CODE: TREE CALLER NAME: LUKE SPEARS CALLER A",
         "ADDR:FOREST DR & LINCOLN AVE",
         "CITY:GROVE CITY",
         "ID:2368281",
         "CALL:Tree down",
         "NAME:LUKE SPEARS");
 
     doTest("T6",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: N CENTER ST/W CHESTNUT ST GROV EID: 2370662 TYPE CODE: MVF CALLER NAME: TAMMY OCONNER CAL",
         "ADDR:N CENTER ST & W CHESTNUT ST",
         "CITY:GROVE CITY",
         "ID:2370662",
         "CALL:MVF",
         "NAME:TAMMY OCONNER");
 
     doTest("T7",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 209 W MERCER ST EID: 2351183 TYPE CODE: MUT CALLER NAME: BUTLER CALLER ADDR:  TIM",
         "ADDR:209 W MERCER ST",
         "ID:2351183",
         "CALL:Mutual Aid",
         "NAME:BUTLER");
 
     doTest("T8",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 55 FRANKLIN ST STON EID: 2351363 TYPE CODE: CHIM CALLER NAME: BARTHOLOMEW, BRENDA",
         "ADDR:55 FRANKLIN ST",
         "CITY:STONEBORO",
         "ID:2351363",
         "CALL:Chimney Fire",
         "NAME:BARTHOLOMEW, BRENDA");
 
     doTest("T9",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 150 LATONKA DR JACK EID: 2353811 TYPE CODE: HF CALLER NAME: MARKS, KATHLEEN CALLE",
         "ADDR:150 LATONKA DR",
         "CITY:JACKSON TWP",
         "ID:2353811",
         "CALL:House fire",
         "NAME:MARKS, KATHLEEN");
 
     doTest("T10",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 819 SUNSET AVE GROV EID: 2354410 TYPE CODE: ELO CALLER NAME: MONTGOMERY BEVERLY C",
         "ADDR:819 SUNSET AVE",
         "CITY:GROVE CITY",
         "ID:2354410",
         "CALL:Electric Lines down",
         "NAME:MONTGOMERY BEVERLY");
 
     doTest("T11",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 201 ERIE ST GROV: @ALLIED NEWS / THE HERALD EID: 2354451 TYPE CODE: ELI CALLER NA",
         "ADDR:201 ERIE ST",
         "CITY:GROVE CITY",
         "PLACE:ALLIED NEWS / THE HERALD",
         "ID:2354451",
         "CALL:Electric Fire");
 
     doTest("T12",
         "\"9-1-1\"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 123 W MAIN ST GROV: @GROVE CITY BOROUGH EID: 2354882 TYPE CODE: BRU CALLER NAME: C",
         "ADDR:123 W MAIN ST",
         "CITY:GROVE CITY",
         "PLACE:GROVE CITY BOROUGH",
         "ID:2354882",
         "CALL:Brush Fire");
  }
   
   public static void main(String[] args) {
     new PAMercerCountyParserTest().generateTests("T1");
   }
 }
