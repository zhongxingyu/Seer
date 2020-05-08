 package net.anei.cadpage.parsers.MT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MTLewisAndClarkCountyBParserTest extends BaseParserTest {
   
   public MTLewisAndClarkCountyBParserTest() {
     setParser(new MTLewisAndClarkCountyBParser(), "LEWIS AND CLARK COUNTY", "MT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
        "(MUTUAL AID REQ) REQ: 1 Management Staff |Engine 6411 |Tender 6423\nLOC: 5106 bluebell rd  TYPE: South Structure - 1st Alarm West Valley is requesting mutual aid, South Structure - 1st Alarm. CONTACT West Valley STAGING ON GOLD CHANNEL",
         "CALL:1 Management Staff |Engine 6411 |Tender 6423",
         "ADDR:5106 bluebell rd",
         "INFO:South Structure - 1st Alarm West Valley is requesting mutual aid, South Structure - 1st Alarm. CONTACT West Valley STAGING ON GOLD CHANNEL");
 
   }
   
   public static void main(String[] args) {
     new MTLewisAndClarkCountyBParserTest().generateTests();
   }
 }
