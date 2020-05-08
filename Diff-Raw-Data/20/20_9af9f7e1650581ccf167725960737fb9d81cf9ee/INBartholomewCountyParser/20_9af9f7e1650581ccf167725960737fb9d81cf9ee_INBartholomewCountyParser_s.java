 package net.anei.cadpage.parsers.IN;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /**
  * Bartholomew County, IN
  */
 public class INBartholomewCountyParser extends DispatchOSSIParser {
   
   public INBartholomewCountyParser() {
     super(CITY_CODES, "BARTHOLOMEW COUNTY", "IN",
            "FYI DATETIME CALL ADDR CITY! X+");
   }
   
   @Override
   public String getFilter() {
     return "bcpaging@bartholomew.in.gov";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     int pt = body.lastIndexOf('-');
     if (pt >= 0 && "- File Messenger Login".startsWith(body.substring(pt))) body = body.substring(0,pt).trim();
     if (!body.startsWith("CAD:")) body = "CAD:" + body;
     return super.parseMsg(body, data);
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("DATETIME")) return new DateTimeField("\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d:\\d\\d", true);
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CLIF", "CLIFFORD",
       "COL",  "COLUMBUS",
      "HOPE", "HOPE"
   });
 }
