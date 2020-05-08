 package net.anei.cadpage.parsers.DE;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Kent County, DE (Variant A)
  */
 
 public class DEKentCountyAParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile("[^ ]*:");
   
   public DEKentCountyAParser() {
     super(CITY_LIST, "KENT COUNTY", "DE",
           "ADDR/SCXP Xsts:X CALLER:NAME");
   }
   
   @Override
   public String getFilter() {
     return "rc.187@c-msg.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     // Rule out Variant B pages
     if (body.startsWith("-")) return false;
     
     boolean good = subject.equals("!|K") || subject.equals("K") || subject.equals("CAD");
     body = body.replace("Xst's:", "Xsts:");
     if (!parseFields(splitMsg(body), data)) return false;
     return good || data.strAddress.length() > 0 || data.strCross.length() > 0 || data.strName.length() > 0;
   }
   
   private String[] splitMsg(String body) {
     List<String> list = new ArrayList<String>();
     Matcher match = DELIM.matcher(body);
     int pt = 0;
     String key = "";
     while (match.find()) {
       list.add((key + body.substring(pt,match.start())).trim());
       pt = match.end();
       key = match.group();
       if (key.length() == 1) key = "";
     }
     String tail = body.substring(pt);
     if (tail.length() > 0) list.add(tail);
     return list.toArray(new String[list.size()]);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "CAMDEN WYOMING",
     "DOVER",
     "HARRINGTON",
     "MILFORD",
     "BOWERS",
     "CAMDEN",
     "CHESWOLD",
     "CLAYTON",
     "FARMINGTON",
     "FELTON",
     "FREDERICA",
     "HARTLY",
     "HOUSTON",
     "KENTON",
     "LEIPSIC",
     "LITTLE CREEK",
     "MAGNOLIA",
     "SMYRNA",
     "VIOLA",
     "WOODSIDE",
     "WYOMING"
   };
 }
 
 
