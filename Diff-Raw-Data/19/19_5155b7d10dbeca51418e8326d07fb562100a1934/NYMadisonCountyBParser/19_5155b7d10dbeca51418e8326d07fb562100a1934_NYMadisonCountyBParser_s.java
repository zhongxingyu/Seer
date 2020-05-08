 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Madison County, NY
 Contact: bcsbeaverslew@yahoo.com <bcsbeaverslew@yahoo.com>
 
 [911 Dispatch]  BRKFD:2011:173\nDispatched\nChest Pain\n1929 ACADEMY RD , BROOKFIELD ( / SPOONER RD)
 [911 Dispatch]  BRKFD:2011:172\nDispatched\nPsychiatric/Suicide Attempt\n11325 MILL , BROOKFIELD ( CENTER /)
 [911 Dispatch]  BRKFD:2011:171\nDispatched\nBreathing Problem\n10825 HOXIE RD , BROOKFIELD ( COYOTE RUN /)
 [911 Dispatch]  BRKFD:2011:170\nDispatched\nMVA - Personal Injury\n10215 SKANEATELES TRNPK , BROOKFIELD ( VIDLER RD / OULEOU
 [911 Dispatch]  BRKFD:2011:169\nDispatched\nAssault/Sexual Assault\n2118 FRIAR RD , BROOKFIELD ( / VIDLER RD)
 [911 Dispatch]  BRKFD:2011:167\nDispatched\nBreathing Problem\n10505 MAIN , BROOKFIELD
 [911 Dispatch]  BRKFD:2011:166\nDispatched\nMVA - Personal Injury\nBEAVER CREEK RD , BROOKFIELD
 [911 Dispatch]  BRKFD:2011:165\nDispatched\nTraumatic Injuries\n@SCHOOL BOCES BROOKFIELD CENTRAL SCHOOL (1910 FAIRGROUND RD
 [911 Dispatch]  BRKFD:2011:164\nDispatched\nSick Person\n10799 HOXIE RD , BROOKFIELD ( COYOTE RUN /)
 [911 Dispatch]  BRKFD:2011:163\nDispatched\nTraumatic Injuries\n1910 FAIRGROUND RD , BROOKFIELD ( / ELM)\n
 
 */
 
 
 public class NYMadisonCountyBParser extends FieldProgramParser {
   
   public NYMadisonCountyBParser() {
     super("MADISON COUNTY", "NY",
            "SRCID SKIP CALL ADDR");
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    if (!subject.equals("911 Dispatch")) return false;
     String[] flds = body.split("\n");
     if (! flds[1].trim().equals("Dispatched")) return false;
     return parseFields(flds, data);
   }
   
   // SRCID field contains source and call ID
   private class SourceIdField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(':');
       if (pt < 0) abort();
       data.strSource = field.substring(0,pt).trim();
       data.strCallId = field.substring(pt+1).trim();
     }
     
     @Override
     public String getFieldNames() {
       return "SRC ID";
     }
   }
   
   // Address field contains address, city, and possibly cross streets
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("@")) {
         Parser p = new Parser(field.substring(1).trim());
         data.strPlace = p.get('(');
        parseAddress(p.get(')'), data);
       } else {
         Parser p = new Parser(field);
         parseAddress(p.get(','), data);
         data.strCity = p.get('(');
         String sCross = p.get(')');
         if (sCross.startsWith("/")) sCross = sCross.substring(1).trim();
         if (sCross.endsWith("/")) sCross = sCross.substring(0,sCross.length()-1).trim();
         data.strCross = sCross;
       }
     }
     
     @Override
     public String getFieldNames() {
      return "ADDR CITY X";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("SRCID")) return  new SourceIdField();
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
 }
 	
