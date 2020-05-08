 package net.anei.cadpage.parsers;
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 
 /* 
  * Chester Country, PA (Class I)
 Contact: "buckhunter1113@aol.com" <buckhunter1113@aol.com>
 Sender: station24@comcast.net
 (Convert \n to blanks, then use the keyword parser
 
  Initial Type: ACCINJA    Final Type:ACCINJA (ACCIDENT - INJURIES / ALS *) 
  Loc: W BALTIMORE PK/SCARLETT RD ,60    (V) AKA:
 
  Initial Type: BFALL      Final Type: BFALL   (FALLS - BLS / LIFT ASSIST *)
  Loc: 118 RACE ST ,03 -- RACE PARK APTS btwn E STATE ST & E C
 
  Initial Type: BSICK      Final Type: BSICK   (SICK PERSON - BLS *) 
  Loc: 415 MEGAN CT ,62  btwn VICTORIA GARDENS DR & AZALEA LA (V)
  AKA
 
  Initial Type: ACVA       Final Type: ACVA    (CVA/STROKE - ALS *)
  Loc: 509 HAZEL AV ,03  btwn N WALNUT ST & WILSON RD (V)
  AKA:   Munic
 
  Initial Type: ALARMF     Final Type: ALARMF  (ALARM - FIRE *)
  Loc: 100 E SOUTH ST ,03 -- KENNETT HIGH SCHOOL btwn S BROAD ST & CHURCH A
 
  Initial Type: AOD        Final Type: AOD     (OVERDOSE - ALS *)
  Loc: 6 WAYS LA ,62  btwn MUNICIPAL BOUNDARY & E CYPRESS ST (V)
  AKA:
  
 (Station 24 EMS Call) Initial Type: BSICK      Final Type: BSICK
 (SICK PERSON - BLS *)
 Loc: 301 VICTORIA GARDENS DR ,62 -- BRANDYWINE ASSISTED LIVNG btwn L
  */
 
 
 public class PAChesterCountyParser extends SmsMsgParser {
 
   @Override
   public boolean isPageMsg(String body) {
     if (body.startsWith("(")) {
       int pt = body.indexOf(')');
       if (pt < 0) return false;
       body = body.substring(pt+1).trim();
     }
     return body.startsWith("Initial Type:");
   } 
 
 
   @Override
   protected void parse(String body, Data data) {
     data.defState="PA";
     data.defCity = "CHESTER COUNTY";
 
     int pt = body.indexOf("Final Type:"); 
     if (pt < 0) return;
     body = body.substring(pt+6);
 
     // Replace key chars for easier parsing
     body = body.replace("\n"," ");
     body = body.replace(" btwn "," btwn:");
 
     Properties props = parseMessage(body, new String[]{"Type","btwn","Loc","AKA"});
     data.strCall = props.getProperty("Type", "");
     data.strCross = props.getProperty("btwn", "");
 
     //Get address up to comma
     parseAddress(props.getProperty("Loc", ""), data);
     int ptc = data.strAddress.indexOf(",");
     if (ptc >= 0) data.strAddress = data.strAddress.substring(0,ptc).trim();
 
     
     data.strSupp = props.getProperty("AKA", "");
 
   }
 
 } 
