 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class CadpageParserTest extends BaseParserTest {
   
   public CadpageParserTest() {
     setParser(new CadpageParser(), "", "");
   }
   
   @Test
   public void testBad() {
     doBadTest("This is an Active911 test message");
   }
   
   @Test
   public void testActve911() {
     doTest("T1",
         "title:LIFELINE ACTIVATION  address:2037 MAIN ST  city:PHILOMATH  cad_code:121411-18  map_code:545-360  ",
         "CALL:LIFELINE ACTIVATION",
         "ADDR:2037 MAIN ST",
         "CITY:PHILOMATH",
         "ID:121411-18",
         "MAP:545-360");
   }
   
   @Test
   public void testEverything() {
    doAllTest("PRI: 1\nDATE: 12/15/2011\nTIME: 09:12\nCALL: HELP ME\nPL: MY HOUSE\nADDR: 611 PINE ST\nCITY: PHILOMATH\nST: XX\nAPT: J3\nX: BLACK ST & BLUE AV\nBOX: XX77\nMAP: J75S2\nCH: OP6\nUNIT: 221\nINFO: BEHIND HOUSE\nNAME: KEN\nPH: 5551212\nCODE: 77J23\nGPS: SOMEWHERE\nID: 2011-001\nSRC: ST202");
    doAllTest("PRI: 1\nDATE: 12/15/2011\nTIME: 09:12\nCALL: HELP ME\nPL: MY HOUSE\nADDR: 611 PINE ST\nAPT: J3\nX: BLACK ST & BLUE AV\nBOX: XX77\nMAP: J75S2\nCH: OP6\nUNIT: 221\nINFO: BEHIND HOUSE\nNAME: KEN\nPH: 5551212\nCODE: 77J23\nGPS: SOMEWHERE\nID: 2011-001\nSRC: ST202\nDCITY: BENTON COUNTY\nDST: OR");
   }
   
   private void doAllTest(String body) {
     Message msg = new Message(false, "ken@cadpage.org", "", body);
     MsgParser parser = new CadpageParser();
     assertTrue(parser.isPageMsg(msg, 0));
     assertEquals(body, CadpageParser.formatInfo(msg.getInfo(), "\n", false));
   }
 }
