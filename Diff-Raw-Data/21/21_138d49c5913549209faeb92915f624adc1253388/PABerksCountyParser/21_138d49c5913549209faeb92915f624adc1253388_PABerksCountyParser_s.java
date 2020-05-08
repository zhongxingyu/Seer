 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Berks County, PA
 Contact: "thirty5tripleuno@yahoo.com" <thirty5tripleuno@yahoo.com>
 CAD MSG: *D MVAUNK   E STATE ST / S KEMP ST 0082 2 VEH / COMP IS ANDREW BROOKS / INVOLVED // AT THE INTERSECTION B
 CAD MSG: *D MVAWITH  102 BEAVER CREEK RD 0087 1 VEH / SPUN OUT / MALE OCC (COMP'S HUSBAND) BELIEVES HE FRACTURED H
 CAD MSG: *D FTEST    STATION 35 @110 PARK AV 0082 BERKS TESING THE TEXT PAGING SYSTEM NO ACTION REQUIRED
 CAD MSG: *D MVAUNK   NOBLE ST / BASTIAN RD 0079 ONE CAR, OCCUPIED, INTO A FIELD // ONLY LANDMARK SHE COULD MEN﻿﻿
 CAD MSG: *D SF       28 NOBLE ST 0081 SEES FLAMES IN BASEMENT //ADVISED TO EVAC /
 CAD MSG: *D MVAENT   FORGEDALE RD / DAVIDS DR 0087 2VEHS HEAD ON/2 VICTIMS ENTRAPPED /1 FEMALE SERIOUSLY INJ/BLEED﻿﻿
 Subject:1/2\nCAD MSG: *D FTELE    STATION 35 @110 PARK AV 0082 CALL REF ITEMS SHE HAS THAT ARE BELEIVED TO BELONG TO YOUR COMPA
 
 Contact: Jesse Zerbe <medicjz@gmail.com>
 CAD MSG: *D ALSMED   107 ALLISON PL 0072 80YOM /RESP DIFF / SPITTING UP BLOOD //BEEN INCREASING OVER THE PAST
 
 Contact: Duane Good <duaneg123@gmail.com>
 Sender: alrt_1ODM@berks.alertpa.org
 CAD MSG: *D ALSMED   106 MAIN ST 0063 56 YOF THROAT FEELS LIKE IS SWELLING CLOSED /BREAKING OUT IN RASH/RESP
 
 Contact: jarrod emes <jkemes@gmail.com>
 Sender: _1ZLG@berks.alertpa.org
 CAD MSG: *D MVAWITH  I78 / MP 39.6 EB 0078 MC DRIVER DOWN IN THE ROADWAY / NOT MOVING / 2 TT PULLED OFF TO THE SI
 
 Contact: support@active911.com
 Sender: alert_2NTE@berks.alertpa.org
 CAD MSG: *D BLSTRAUM 101 DOE MOUNTAIN LN @BEAR CREEK SKI AREA 0086   14YOM/BROKEN LEFT WRIST/CON AND ALERT/PT WILL BE IN THE SKI PATROLE RO
 
 Contact: Joe Mcglynn <jmladder57@gmail.com>
 Sender: 1410000029
 FRM: \nMSG:CAD MSG: *D SF       65B MEADOW GLEN LN 0034 HEAVY SMOKE, GETTING WORSE; POSSIBLY COMING FROM DOWNSTAIRS APT; HUNGU
 
 Contact: Michael Wertz <jrfirefighter85@msn.com>
 CAD MSG: *D SF       DIRECTLINK @2561 BERNVILLE RD 0054 PENSKI ENTERANCE / BUILDING 1 TRANSFORMER ROOM / SMOKE IN THE BUILDING
 CAD MSG: *D AFA      RHMC C BUILDING @600 SPRUCE ST 0038 BLDG C/GENERAL FIRE/ATN/610-988-8222
 
 COntact: Diana York <hawkmeadow@gmail.com>
 Sender: alrt_350Z@berks.alertpa.org
 CAD MSG: *D ALSMED   459 MEMORIAL HWY 0091 F IN SEIZURES / 1YOF /
 
 ** NOT PARSING YET ***
 Contact: "greek@vjgreek.com" <greek@vjgreek.com>
 CAD MSG: *D FSB      CITY SB 1, 10, 36, 55, 64,69 @ CITY FIRE INCIDENT CITY 2ND ALARM FIRE/E1 AND E64 RESPOND INTO THE CITYI
 
 
 */
 
 public class PABerksCountyParser extends SmartAddressParser {
   
   private static final Pattern MUNI_CODE_PAT = Pattern.compile(" 00\\d\\d ");
   
   public PABerksCountyParser() {
     super("BERKS COUNTY", "PA");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     // Strip off any prefix
     int pt = body.indexOf("CAD MSG: *D ");
     if (pt < 0) return false;
     body = body.substring(pt);
     
     // Extract primary call description
     if (body.length() < 20) return false;
     String call = body.substring(12, 20).trim();
     String desc = CALL_CODES.getProperty(call);
     if (desc != null) call = call + " - " + desc;
     data.strCall = call;
     body = body.substring(20);
     
     // Look for a 4 digit station ID, this marks the end of the address
     Matcher match = MUNI_CODE_PAT.matcher(body);
     if (! match.find()) return false;
     
     String muniCode = body.substring(match.start()+1, match.end()-1);
     int iMuniCode = Integer.parseInt(muniCode);
     iMuniCode -= FIRST_MUNI_CODE;
     if (iMuniCode >= 0 && iMuniCode < MUNI_CODES.length && MUNI_CODES[iMuniCode] != null) {
       data.strCity = MUNI_CODES[iMuniCode];
     } else {
       data.strCity = muniCode;
     }
     String address = body.substring(0, match.start()).trim();
     body = body.substring(match.end()).trim();
     
     // An '@' splits place name from address
     // They aren't consistent about which is which, so we will use the address
     // check logic to make that determination
     pt = address.indexOf('@');
     if (pt >= 0) {
       String part1 = address.substring(0, pt).trim();
       String part2 = address.substring(pt+1).trim();
       if (checkAddress(part1) > checkAddress(part2)) {
         address = part1;
         data.strPlace = part2;
       } else {
         data.strPlace = part1;
         address = part2;
       }
     }
     parseAddress(address, data);
     
     // Anything beyond that is supplemental info
     data.strSupp = body.replaceAll("//+", "/");
     
     return true;
   }
   
   private static final int FIRST_MUNI_CODE = 19;
   private static final String[] MUNI_CODES = new String[]{
     /*19*/  "READING",
     /*20*/  null,
     /*21*/  "MOUNT PENN",
     /*22*/  "ST. LAWRENCE",
     /*23*/  "LOWER ALSACE TWP",
     /*24*/  "ALSACE TWP",
     /*25*/  "EXETER TWP",
     /*26*/  "AMITY TWP",
     /*27*/  "DOUGLASS TWP",
     /*28*/  "UNION TWP",
     /*29*/  "BIRDSBORO",
     /*30*/  "NEW MORGAN",
     /*31*/  "ROBESON TWP",
     /*32*/  "CAERNARVON TWP",
     /*33*/  "BRECKNOCK TWP",
     /*34*/  "CUMRU TWP",
     /*35*/  "MOHTON",
     /*36*/  "SHILLINGTON",
     /*37*/  "KENHORST",
     /*38*/  "WEST READING",
     /*39*/  "WYOMISSING",
     /*40*/  null,
     /*41*/  "OLD WYOMISSING HILLS",
     /*42*/  "OLD WEST LAWN",
     /*43*/  "SPRING TWP",
     /*44*/  "SINKING SPRINGS",
     /*45*/  "SOUTH HEIDELBERG TWP",
     /*46*/  "WERNERSVILLE",
     /*47*/  "ROBESONIA",
     /*48*/  "HEIDELBERG",
     /*49*/  "WOMELSDORF",
     /*50*/  null,
     /*51*/  "MARION TWP",
     /*52*/  "NORTH HEIDELBERG",
     /*53*/  "LOWER HEIDELBERG",
     /*54*/  "BERN TWP",
     /*55*/  "CENTRE TWP",
     /*56*/  "PENN TWP",
     /*57*/  "BERNVILLE",
     /*58*/  "JEFFERSON TWP",
     /*59*/  "UPPER BERN TWP",
     /*60*/  "CENTERPORT",
     /*61*/  "TILDEN TWP",
     /*62*/  "UPPER TULPEHOCKEN TWP",
     /*63*/  "STRAUSSTOWN",
     /*64*/  "BETHEL TWP",
     /*65*/  "TULPEHOCKEN TWP",
     /*66*/  "MUHLENBERG TWP",
     /*67*/  "LAURELDALE",
     /*68*/  null,
     /*69*/  "ONTELAUNEE TWP",
     /*70*/  null,
     /*71*/  "LEESPORT",
     /*72*/  "MAIDENCREEK TWP",
     /*73*/  "PERRY TWP",
     /*74*/  "SHOEMAKERSVILE",
     /*75*/  "WINDSOR TWP",
     /*76*/  "HAMBURG",
     /*77*/  "ALBANY TWP",
     /*78*/  "GREENWICH TWP",
     /*79*/  "MAXATAWNY TWP",
     /*80*/  "LENHARTSVILLE",
     /*81*/  "KUTZTOWN",
     /*82*/  "LYONS",
     /*83*/  "FLEETWOOD",
     /*84*/  "RICHMOND TWP",
     /*85*/  "TOPTON",
     /*86*/  "LONGSWAMP TWP",
     /*87*/  "ROCKLAND TWP",
     /*88*/  "DISTRICT TWP",
     /*89*/  "PIKE TWP",
     /*90*/  null,
     /*91*/  "RUSCOMBMANOR TWP",
     /*92*/  "OLEY TWP",
     /*93*/  "EARL TWP",
     /*94*/  "BOYERTOWN",
     /*95*/  "COLEBROOKDALE TWP",
     /*96*/  "BECHTELSVILLE",
     /*97*/  "WASHINGTON TWP",
     /*98*/  "BALLY",
     /*99*/  "HEREFORD TWP"
   };
   
   private static final Properties CALL_CODES = buildCodeTable(new String[]{
       "MVAENT", "Accident w/ entrapment",
       "MVAUNK", "Accident unknown inj",
       "MVAWITH", "Accident w/ injury",
       "SF", "Structure Fire",
       "RSF", "Reading Structure Fire",
       "RAFA", "Reading Fire Alarm",
       "REMERG", "Reading Emerge",
       "RMISC", "Reading Misc",
       "RBF", "Reading Brush Fire",
       "BF", "Brush Fire",
       "CMA", "Carbon Monoxide",
       "AFA", "Fire Alarm",
       "VF", "Vehicle Fire",
       "FS", "Fire Service",
       "FSB", "Fire Scene Standby"
   });
 }
