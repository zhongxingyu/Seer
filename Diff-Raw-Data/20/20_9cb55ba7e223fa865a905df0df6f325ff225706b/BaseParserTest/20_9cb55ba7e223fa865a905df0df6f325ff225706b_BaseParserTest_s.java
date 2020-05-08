 package net.anei.cadpage.parsers;
 
 import org.junit.Test;
 
 import net.anei.cadpage.SmsMsgInfo;
 
 import static org.junit.Assert.*;
 
 
 public abstract class BaseParserTest {
 
   private SmsMsgParser parser;
   private String defCity;
   private String defState;
   
   public void setParser(SmsMsgParser parser, String defCity, String defState) {
     this.parser = parser;
     this.defCity = defCity;
     this.defState = defState;
   }
   
   @Test
   public void testBadMsg() {
     // Just call the parser with a badly formated msg and make sure it doesn't croak
     parser.parse("BAD MSG", new SmsMsgInfo.Data());
     parser.parse("", new SmsMsgInfo.Data());
    parser.parse(" ", new SmsMsgInfo.Data());
   }
   
   /**
    * Main parse test method
    * @param title - test name
    * @param test - test message
    * @param result - expected results
    */
   public void doTest(String title, String test, String ... result) {
     
     assertTrue(title + ":isPage", parser.isPageMsg(test));
     
     SmsMsgInfo.Data data = new SmsMsgInfo.Data();
     for (String str : result) {
       int pt = str.indexOf(':');
       String sType = str.substring(0, pt);
       str = str.substring(pt+1);
       if (sType.equals("CALL")) data.strCall = str;
       else if (sType.equals("PLACE")) data.strPlace = str;
       else if (sType.equals("ADDR")) data.strAddress = str;
       else if (sType.equals("CITY")) data.strCity = str;
       else if (sType.equals("APT")) data.strApt = str;
       else if (sType.equals("X")) data.strCross = str;
       else if (sType.equals("BOX")) data.strBox = str;
       else if (sType.equals("UNIT")) data.strUnit = str;
       else if (sType.equals("ST")) data.strState = str;
       else if (sType.equals("MAP")) data.strMap = str;
       else if (sType.equals("ID")) data.strCallId = str;
       else if (sType.equals("PHONE")) data.strPhone = str;
       else if (sType.equals("INFO")) data.strSupp = str;
       else fail("Keyword " + sType + " is not defined");
     }
     
     SmsMsgInfo info = parser.parse(test);
     
     assertEquals(title + ":Call", data.strCall, info.getCall());
     assertEquals(title + ":Place", data.strPlace, info.getPlace());
     assertEquals(title + ":Address", data.strAddress, info.getAddress());
     assertEquals(title + ":City", data.strCity, info.getCity());
     assertEquals(title + ":Apt", data.strApt, info.getApt());
     assertEquals(title + ":Cross", data.strCross, info.getCross());
     assertEquals(title + ":Box", data.strBox, info.getBox());
     assertEquals(title + ":Unit", data.strUnit, info.getUnit());
     assertEquals(title + ":State", data.strState, info.getState());
     assertEquals(title + ":Map", data.strMap, info.getMap());
     assertEquals(title + ":ID", data.strCallId, info.getCallId());
     assertEquals(title + ":Phone", data.strPhone, info.getPhone());
     assertEquals(title + ":Supp", data.strSupp, info.getSupp());
     
     assertEquals(title + ":DefCity", defCity, info.getDefCity());
     assertEquals(title + ":DefState", defState, info.getDefState());
   }
 }
