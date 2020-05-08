 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Lancaster County, PA
 Contact: Benjamin Herskowitz <bherskowitz@gmail.com>
 Sender: 911@lcwc911.us
 (VEH ACCIDENT-STANDBY) MANHEIM TWP~LITITZ PIKE / PETERSBURG RD~~ENG271,SQ27~20:19:57^
 (VEH ACCIDENT-MASS TRANSIT-1A) MANHEIM TWP~2475 OREGON PIKE~WHITEMARSH DR~VALLEY RD~ENG271~08:13:50^
 (SPILL CONTROL) EAST DONEGAL TWP~1158 RIVER RD~N BANK ST~ANDERSON FERRY RD~HAZ291~11:55:37^
 (AUTO ALARM-HIGH OCCUPANCY) MANHEIM TWP~115 BLUE STREAK BLVD~SCHOOL RD~VALLEY RD~ENG271,ENG272~15:12:34^
 (HAZ MAT INCIDENT-1A) LANC CITY~753 S PLUM ST~17 ALY~JUNIATA ST~HAZ291~08:19:13^
 
 Contact: Matthew Lenker <strappy39@gmail.com>
 VEH ACCIDENT-ENTRAPMENT-1A / ELIZABETHTOWN BORO~E COLLEGE AVE / S SPRUCE ST~~TRK74,ENG741~08:54:12^\n
 
 Contact: James Taylor <jtaylor574@gmail.com>
 VEH ACCIDENT-CLASS 2 / WEST COCALICO TWP~N KING ST / E QUEEN ST~~RES13~13:10:45^\n
 
 */
 
 public class PALancasterCountyParser extends MsgParser {
   
   public PALancasterCountyParser() {
     super("LANCASTER COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
    return "911@lcwc911.us";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (! body.contains("~")) return false;
    data.strCall = subject;
     
     int ndx = 0;
     for (String line : body.split("~")) {
       ndx++;
       line = line.trim();
       switch (ndx) {
       
       case 1:
         int pt = line.indexOf('/');
         if (pt >= 0) {
          data.strCall = line.substring(0,pt).trim();
           line = line.substring(pt+1).trim();
         } else {
           if (data.strCall.length() == 0) return false;
         }
         data.strCity = line;
         if (data.strCity.endsWith(" BORO")) data.strCity = data.strCity.substring(0, data.strCity.length()-5).trim();
         if (data.strCity.startsWith("LANC")) data.strCity = "LANCASTER";
         break;
         
       case 2:
         parseAddress(line, data);
         break;
         
       case 3:
         data.strCross = line;
         break;
         
       case 4:
         // Could be second cross st, or if there wasn't a cross street, this
         // would be the unit field
         if (line.length() == 0) break;
         if (data.strCross.length() > 0) {
           data.strCross += " & " + line;
           break;
         }
         ndx++;
         // fall through to case 5
     
       case 5:
         data.strUnit = line;
         break;
       }
     }
     return true;
   }
 }
