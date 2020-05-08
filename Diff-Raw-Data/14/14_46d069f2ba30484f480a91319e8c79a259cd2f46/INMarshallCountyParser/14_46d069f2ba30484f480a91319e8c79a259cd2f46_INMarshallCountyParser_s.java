 package net.anei.cadpage.parsers.IN;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Marshall County, IN
 Contact: "John Gomola" <johng@bourbonfire.com>
 Sender: cad@co.marshall.in.us
 
 CAD:FYI: ;702 S WASHINGTON ST;BREM;E RAYMOND ST;ALARM CO DETECTOR;08/23/2011 10:17:48            
 CAD:FYI: ;200 W FLORENCE ST;BOUR;N THOMPSON ST;FIRE ALARM;08/23/2011 10:34:48
 CAD:FYI: ;601 S BOURBON ST;BOUR;W FRANK ST;FIRE STRUCTURE;08/21/2011 21:30:37 
 CAD:FYI: ;SR 110/ELM RD;TIPP;FIRE GRASS;08/21/2011 19:17:18 
 Update: ;625-33 OAKHILL AVE;PLYM;NURSERY ST;S SECOND ST;MEDICAL CALL;08/19/2011 20:52:25
 
 */
 
 public class INMarshallCountyParser extends DispatchOSSIParser {
   
   public INMarshallCountyParser() {
     super(CITY_CODES, "MARSHALL COUNTY", "IN",
           "SKIP ADDR CITY X/Z+? CALL! END");
   }
   
   @Override
   public String getFilter() {
     return "cad@co.marshall.in.us";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.startsWith("Update: ;")) {
       body = "CAD:FYI:" + body.substring(7);
     } else if (!body.startsWith("CAD:FYI: ;")) return false;
     return super.parseMsg(body, data);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ARGO", "ARGOS",
       "BOUR", "BOURBON",
       "BREM", "BREMEN",
       "CULV", "CULVER",
       "DONA", "DONALDSON",
       "LAPA", "LAPAZ",
       "PLYM", "PLYMOUTH",
       "TIPP", "TIPPECANOE",
       "TYNE", "TYNER",
       
       "ETNA", "ETNA GREEN",
       "GROV", "GROVERTOWN",
       "KNOX", "KNOX",
       "LAKE", "LAKEVILLE",
       "MENT", "MENTONE",
       "NAPP", "NAPPANEE",
       "ROCH", "ROCHESTER",
       "WALK", "WALKERTON",
   });
 }
