 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 
import net.anei.cadpage.parsers.FieldProgramParser;
import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*    
 Somerset County, MD
 Contact: "Ford, Allen C" <acford@dpscs.state.md.us>
 Contact: Chris McLain <ctm826@gmail.com>
 Sender: CAD@somerset911.local
 
 CAD:S5A;BACK PAIN;23196 PAUL BENTON CIR;DEAL
 CAD:ST2;MEDICAL ASSIST;4248 STROBEL LN;CRIS
 CAD:ST3;MEDICAL ASSIST;29762 LOVERS LN;MARI
 CAD:ST3;MEDICAL ASSIST;28259 CRISFIELD MARION RD;MARI
  
 */
 
public class MDSomersetCountyParser extends FieldProgramParser {
   
   public MDSomersetCountyParser() {
     super(CITY_CODES, "SOMERSET COUNTY", "MD",
           "SRC CALL ADDR CITY");
   }
   
   @Override
   public String getFilter() {
     return "CAD@somerset911.local";
   }

   @Override
  protected boolean parseMsg(String body, Data data) {
    if (!body.startsWith("CAD:")) return false;
    String[] flds = body.split(";");
    if (flds.length != 4) return false;
    return parseFields(flds, data);
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
