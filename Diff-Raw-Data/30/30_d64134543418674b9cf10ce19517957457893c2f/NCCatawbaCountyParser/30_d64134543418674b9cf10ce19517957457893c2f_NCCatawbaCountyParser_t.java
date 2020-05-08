 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Catawba County, NC
 Contact: kevin head <headly1531@gmail.com>
 Contact: Paul Couch <theprofit1611@gmail.com>
 Contact: Adam Sparks <asparks453@gmail.com>
 Contact:"Brian A Drum" <BADRUM@catawbacountync.gov>  (works in 911 center)
 Sender: E-911@catawbacountync.gov
 
 |2011-000821*213 9TH ST SW***CONOVER**STRUCTURE*********
 |2010-190520*3977 C & B FARM RD***CONOVER**AUTO PD*********
 |2010-188877*1127 2ND ST NE*218**CONOVER**LIFELINE*********
 |2010-188192*1405 DEBORAH HERMAN RD SW***CONOVER**FIRE ALARM*********
 |2011-000273*1104 CONOVER BLVD E***CONOVER**VEHICLE FIRE*********
 |2010-190520*3977 C & B FARM RD***CONOVER**AUTO PD*********
 2010-189098*1211 PECAN ST***CONOVER**OVERDOSE*********
 2011-020998*100 SOUTHWEST BLVD*B*1*NEWTON**TESTING*ALARMING TESTING*CATAWBA COUNTY SHERIFFS OFFICE*828-464-5241*C51*363*Medical: No*Hazards: No*02/09/2011 07:52:50 : pos4 : BADRUM   Landmark: CATAWBA COUNTY SHERIFFS OFFICE*
 2011-021005*SHERRILLS FORD RD // ALLEY RD***CATAWBA**TESTING*ALARMING TESTING*TEST*828-   -*ZZZ,C51**Medical: No*Hazards: No*02/09/2011 08:07:18 : pos4 : BADRUM   SHERRILLS FORD RD//ALLEY RD*
 2011-021016*1101 S BRADY AV*E*12*NEWTON**TESTING*ALARMING TESTING*EMS STANDBY NEWTON AREA*828-465-0034*C51*364*Medical: No*Hazards: No*02/09/2011 08:20:34 : pos4 : BADRUM   Landmark: EMS STANDBY NEWTON AREA*
 2011-021018*100 SOUTHWEST BLVD*A**NEWTON*Landmark Comment: AED 1ST FLOOR LOBBY HALLWAY NEAR PUBLIC REST ROOMS*TESTING*ALARMING TESTING**828-465-8200*C51*363*Medical: No*Hazards: No*02/09/2011 08:26:53 : pos4 : BADRUM   Landmark: CATAWBA COUNTY GOVERNMENT CENTER  Landmark Comment: AED 1ST FLOOR LOBBY HALLWAY NEAR PUBLIC REST ROOMS*
 2011-021063*2204 WITHERS RD***MAIDEN**OVERDOSE*********
 2011-021052*35 S US 321 HWY***NEWTON**Traffic Stop*********
 2011-021048*3724 ROCK BRIDGE DR NE***CONOVER**ALARM RES*********
 2011-021037*3983 HOKE ST***CLAREMONT**TRAUMATIC IN*********
 
  
 
 Above are some example pages if everything was turned on with the CAD paging. Each field is separated by an asterisk * . Below is a list of each field and if they are used or not.
 
 Call Number
 
 Location (Address)
 
 Apartment / Suite
 
 Floor / Building (not turned on currently but maybe in the future)
 
 City
 
 Location Comment (not turned on)
 
 Complaint
 
 Complaint Description (not turned on)
 
 Caller Name (not turned on currently but maybe in the future)
 
 Phone Number (not turned on currently but maybe in the future)
 
 Units Assigned (not turned on currently but maybe in the future)
 
 Internal ESN (not turned on)
 
 Medical Information (not turned on)
 
 Hazards Information (not turned on)
 
 Narrative (not turned on)
 
 
 */
 
 public class NCCatawbaCountyParser extends FieldProgramParser {
   
   private static final Pattern ID_PATTERN = Pattern.compile("20\\d\\d-\\d{6}");
  private static final Pattern C_AND_B = Pattern.compile("\\bC AND B\\b|\\bC *& *B\\b", Pattern.CASE_INSENSITIVE);
   
   public NCCatawbaCountyParser() {
     super("CATAWBA COUNTY", "NC",
            "ID ADDR APT APT CITY INFO CALL! INFO PLACE PHONE UNIT SKIP INFO INFO INFO");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (body.length() == 0) return false;
     if (body.charAt(0) == '|') body = body.substring(1).trim();
     body = body.replaceAll("//+", "/");
    body = C_AND_B.matcher(body).replaceAll("C%B");
     String[] flds = body.split("\\*");

     if (! ID_PATTERN.matcher(flds[0]).matches()) return false;

    if (!parseFields(flds, data)) return false;
    data.strAddress = data.strAddress.replace("C%B", "C AND B");
    data.strCross = data.strCross.replace("C%B", "C AND B");
     
    return true;
   }
   
   
 }
