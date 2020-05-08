 package net.anei.cadpage.parsers.MI;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Saginaw County, MI
 Contact: "Marco Isome" <misome@sbcglobal.net>
 Sender: emergin@saginawcounty.com
 TRITWP *: FIRE ALARM *: 5100 S GRAHAM RD CTY: 23 *: *WILLIAMS RES - GENERAL FIRE
 TRITWP *: FIRE ALARM *: 5401 S GRAHAM RD CTY: 23 *: *APOTHECARY PRODUCTS/SMK DET
 TRITWP *: PI PIN-IN *: S FORDNEY RD&NELSON RD CTY: 12 *: *OCCD VEH ROLLED OVER IN DITCH
 TRITWP *: STAND BY *: BENTLEY ST&FIRWOOD ST CTY: 46 *: STANDBY REF POSS FIRE IN CHESA
 TRITWP *: MUTUAL AID *: 2385 S FORDNEY RD CTY: 12 *: NEED LZ SET UP
 TRITWP *: WIRES UP/ARCING *: 1125 W BELLE CTY: 21 *: *TREE ON WIRE SPARKING
 TRITWP  *: FIRE OUTSIDE *: PARKWAY&N SAGINAW CTY: 39 *: *BARREL ON FIRE AT PARK
 
  */
 public class MISaginawCountyParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile(" *(?<= )\\*: +");
   
   public MISaginawCountyParser() {
     super("SAGINAW COUNTY", "MI",
           "SKIP CALL ADDR INFO! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "emergin@saginawcounty.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     String[] flds = DELIM.split(body);
    if (!flds[0].equals("TRITWP")) return false;
     return parseFields(flds, data);
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       super.parse(p.get(" CTY:"), data);
       data.strMap = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR MAP";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
