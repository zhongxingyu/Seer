 package net.anei.cadpage.parsers.VA;
 
import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 
 
 /*
 Fauquier County, VA
 Contact: CodeMessaging
Contact: Matthew Demaree <mdemaree@htd.net>
 CAD:0301;TRAFFIC ACCIDENT;8323 WEST MAIN ST;WINCHESTER RD;FROST ST;CO3;11C
 CAD:0366;1050 I 66;179 I-66 W;I-66;CO3;11B
 CAD:0410;PUBLIC SERVICE FIRE;5089 OLD TAVERN RD;OLD WINCHESTER RD;WINCHESTER RD;CO4
 CAD:0304;OUTSIDE/BRUSH/DUMPSTER FIRE;3800-BLK COBBLER MOUNTAIN RD;EASTVIEW LN;DOUBLE J LN;CO3;11B
 CAD:0301;OUTSIDE/BRUSH/DUMPSTER FIRE;8307 EAST MAIN ST;MELODY LN;WINCHESTER RD;CO3;11B
 CAD:0367;1050 I 66;250 I-66 W;I-66;I-66;CO3
 CAD:0101;FIRE ALARM INSTITUTIONAL;500 HOSPITAL DR;VETERANS DR;CO1
 CAD:0306;OUTSIDE/BRUSH/DUMPSTER FIRE;10747 MORELAND RD;FOX HOLLOW RD;CARRINGTON RD;CO3;11B
 CAD:0366;VEHICLE FIRE - COMMERCIAL;160 I-66 E;I-66;CO3;11C
 CAD:0305;OUTDOOR ELECTRICAL HAZARD;10244 GLENARA LN;CREST HILL RD;CO3;11C
 CAD:0311;PUBLIC SERVICE FIRE;3700-BLK GROVE LN;JUSTICE LN;ASHVILLE RD;CO3
 CAD:0309;1050 WITH ENTRAPMENT/ROLLOVER;2787 LEEDS MANOR RD;RAVEN LN;MARSHALL SCHOOL LN;CO3;11C
 CAD:0303;SMOKE/ELECTRICAL ODORS OUTSIDE;4000-BLK ZULLA RD;LITTLE RIVER LN;SERENITY LN;CO3;11 B
 CAD:0301;SMOKE/ELECTRICAL ODORS OUTSIDE;8267-BLK EAST MAIN ST;OLD STOCKYARD RD;MELODY LN;CO3;11 B
 CAD:0101;STRUCTURE FIRE - COMMERCIAL;333 CARRIAGE HOUSE LN;WEST SHIRLEY AVE;VETERANS DR;CO1;11C
 
** OOC Mutual Aid **
CAD:STRUCTURE FIRE - COMMERCIAL;14101 WHITNEY RD
 
 */
 
 public class VAFauquierCountyParser extends DispatchOSSIParser {
   
   public VAFauquierCountyParser() {
     super("Fauquier County", "VA",
        "BOX? CALL! ADDR! X/Z+? UNIT CH");
   }
 
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
   
   @Override
  protected boolean parseMsg(String body, Data data) {
    if (!super.parseMsg(body, data)) return false;
    
    // Calls with no box number are OOC mutual aid calls
    if (data.strBox.length() == 0) data.defCity = "";
    return true;
  }
  
  @Override
   public Field getField(String name) {
    if (name.equals("BOX")) return new BoxField("\\d{4}");
     if (name.equals("UNIT")) return new UnitField("(?:CO|ST)\\d+");
     return super.getField(name);
   }
 }
 
