 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 
import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*    
 Somerset County, MD
 Contact: "Ford, Allen C" <acford@dpscs.state.md.us>
 Contact: Chris McLain <ctm826@gmail.com>
 Sender: CAD@somerset911.local
 
 CAD:S5A;BACK PAIN;23196 PAUL BENTON CIR;DEAL
 CAD:ST2;MEDICAL ASSIST;4248 STROBEL LN;CRIS
 CAD:ST3;MEDICAL ASSIST;29762 LOVERS LN;MARI
 CAD:ST3;MEDICAL ASSIST;28259 CRISFIELD MARION RD;MARI
  
Contact: Allen <n3zot@verizon.net>
Contact: Tracey Hall <snofox68@gmail.com>
Sender: CAD@somerset911.local
CAD:ST3;OUTSIDE FIRE;30549 MARUMSCO RD;MARI;[Fire Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Outside Fire. SMALL OUTSIDE fire (Unknown).Caller Statement
CAD:MUTUAL AID ASSIST OUTSIDE AGEN;24666 SILVER LN

 */
 
public class MDSomersetCountyParser extends DispatchOSSIParser {
   
   public MDSomersetCountyParser() {
     super(CITY_CODES, "SOMERSET COUNTY", "MD",
           "SRC? CALL ADDR! CITY INFO+");
   }
   
   @Override
   public String getFilter() {
     return "CAD@somerset911.local";
   }
  
   @Override
  public Field getField(String name) {
    if (name.equals("SRC")) return new SourceField("[A-Z0-9]{3}");
    return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ALL",  "ALLEN",
       "BERL", "BERLIN",
       "CRIS", "CRISFIELD",
       "DEAL", "DEAL ISLAND",
       "EDEN", "EDEN",
       "EWEL", "EWELL",
       "FAIR", "UPPER FAIRMOUNT",
       "FRUI", "FRUITLAND",
       "MANO", "MANOKIN",
       "MARI", "MARION STATION",
       "POCO", "POCOMOKE CITY",
       "PRIN", "PRINCESS ANNE",
       "RHOD", "RHODES POINT",
       "SALI", "SALISBURY",
       "TYLE", "TYLERTON",
       "VENT", "VENTON",
       "WEST", "WESTOVER"
   });
 }
