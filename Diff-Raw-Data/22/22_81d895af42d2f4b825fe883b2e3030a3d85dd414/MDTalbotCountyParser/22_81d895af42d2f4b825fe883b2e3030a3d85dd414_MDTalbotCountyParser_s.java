 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 public class MDTalbotCountyParser extends SmartAddressParser {
   
  private static final Pattern MARKER = Pattern.compile("^Talbot 911:\\*D ");
   
   public MDTalbotCountyParser() {
     super("TALBOT COUNTY", "MD");
   }
   
   @Override
   public String getFilter() {
    return "msg@cfmsg.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     Matcher match = MARKER.matcher(body);
     if (! match.find()) return false;
     body = body.substring(match.end()).trim();
     
     int pt = body.indexOf(' ');
     if (pt < 0) return false;
     data.strBox = body.substring(0,pt);
     body = body.substring(pt+1).trim();
     
     // OK, go do your magic!!
     parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_AT_BOTH, body, data);
     data.strSupp = getLeft();
     return true;
   }
 }
