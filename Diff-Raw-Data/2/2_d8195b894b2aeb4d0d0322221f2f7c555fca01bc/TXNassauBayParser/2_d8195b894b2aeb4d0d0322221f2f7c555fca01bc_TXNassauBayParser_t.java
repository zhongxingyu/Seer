 package net.anei.cadpage.parsers.TX;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /**
  * Nassau Bay (Harris County, TX)  
  */
 
 public class TXNassauBayParser extends DispatchOSSIParser {
   
   private static final Pattern PREFIX = Pattern.compile("^\\d*:");
   
   public TXNassauBayParser() {
     this("NASSAU BAY", "TX");
   }
   
   protected TXNassauBayParser(String defCity, String defState) {
     super(CITY_CODES, defCity, defState,
           "( FYI SRC CALL! ADDR! UNIT? CITY? CODE? DATETIME! | CANCEL ADDR CITY! ) INFO+");
   }
   
   @Override
   public String getAliasCode() {
     return "TXNassauBay";
   }
   
   @Override
   public String getFilter() {
     return "cad@ossicadpaging";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     // Strip off option number prefix
     Matcher match = PREFIX.matcher(body);
     if (match.find()) {
       body = body.substring(match.end()).trim();
     }
     return super.parseMsg(body, data);
   }
   
   private static final Pattern LEAD_TRASH_PTN = Pattern.compile("^(?:Event spawned from |\\(S\\) *\\(N\\)) *");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = LEAD_TRASH_PTN.matcher(field);
       if (match.find()) field = field.substring(match.end());
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CANCEL")) return new CallField("CANCEL", true);
     if (name.equals("UNIT")) return new UnitField("[A-Z]+\\d+|[A-Z]{2}FD", true);
     if (name.equals("CODE")) return new CodeField("[A-Z]{2,4}", true);
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
      "FW", "FRIENDSWOOD",
       "HO", "NASSAU BAY",
       "LC", "LEAGUE CITY",
       "NB", "NASSAU BAY",
       "SB", "SEABROOK",
       "WB", "WEBSTER"
   });
 }
