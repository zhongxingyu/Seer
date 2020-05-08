 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Delaware County, PA (alternate)
 Contact: Steve Castellano <stevecast2024@gmail.com>
 Sender: Many
 
 X1: OAK AV X2: PEMBROKE AV Nature: FIRE-NON  VEHICLE FIRE Time: 13:25:12  Inc: F11032418
 6636 PERRY AV UD  X2: MONTGOMERY AV Nature: ALS-EMS  RESPIRATORY DIFFICULTY Time: 12:13:17  Notes:  67 Y/O FEMALE   Inc: F11032408
 507 HOLLY RD YE  X1: MYRA AV X2: BAILEY RD Nature: FIRE-BLD  BUILDING FIRE, RES/DWELLING Time: 20:21:17  Notes: STOVE   Inc: F11031897
 39 E PROVIDENCE RD YE  X1: S LANSDOWNE AV X2: ELBERON AV Nature: FIRE-BLD  BUILDING FIRE, RES/DWELLING Time: 18:28:43  Notes: STOVE Inc: F11031009
 957 BALTIMORE AV EL : @BALTIMORE PIKE CHECK CASHING X1: E BALTIMORE AV X2: E BALTIMORE AV Nature: FIRE-BLD  BUILDING FIRE, RES/DWELLING Time: 20:39:44  Notes: ODOR OF SMOKE 2ND FLOOR   2ND EMERGENCY 20   Inc: F1103170
 115 S EAGLE RD HV : @56 MANOA FIRE CO X1: WYNDMOOR RD X2: STANLEY AV Nature: FIRE-OTH  COVER ASSIGNMENT Time: 13:25:09  Notes:  1 ENG FROM STA 20    CH 9   Inc: F11029664
 801 FERN ST YE  X1: PARMLEY AV X2: W COBBS CREEK PKWY Nature: ALS-EMS FALL W/TRAUMA Time: 03:36:04  Notes: HEAD INJURY   DIABETIC PATIENT ENGINE ASSIST EMS   FD RESPOND AT EMERGENCY SPEED   Inc: F11028163
 2402 ALFRED DR YE,A  X1: HEATHER CIR X2: KAREN DR Nature: FIRE-BLD BUILDING FIRE, COMMERCIAL BLDG Time: 13:37:14  Notes:  STOVE   Inc: F11027836
 11 WILDWOOD AV EL  X1: E BALTIMORE AV X2: PEMBROKE AV Nature: ASSIST FD TO ASSIST EMS Time: 00:15:59  Notes: LEG WOUND - DIABETIC - NO BLOOD THINNERS   65YOM   3RD EMER 103   LIFT ASSIST   Inc: F11029789
 X1: BALTIMORE AV X2: LEXINGTON AV Nature: ALS-EMS  SEIZURES Time: 01:53:58  Inc: F11030225
 X1: WEST CHESTER PKE X2: PARK AV Nature: DISASTER  EMS BOX ALARM Time: 15:29:36  Inc: F11028912
 136 BEVERLY AV EL  X1: PEMBROKE AV X2: EMERSON AV Nature: ALARM AUTOMATIC FIRE ALARM Time: 08:35:32  Notes: UPSTAIRS SMOKE DET RES/SCOTT   Inc: F11027387
 164 LEXINGTON AV EL : @24 EAST LANSDOWNE FIRE CO X1: PEMBROKE AV X2: EMERSON AV Nature: TEST  PRINTER TEST Time: 11:06:09  Inc: F11023838
 269 HIRST AV EL  X1: EMERSON AV X2: GLENWOOD AV Nature: ALARM  CARBON MONOXIDE ALARM Time: 20:43:53  Inc: F11024160
 1203 ALFRED AV YE,3FL E  X1: LORI DR X2: ALFRED DR Nature: FIRE-BLD BUILDING FIRE, WITH ENTRAPMENT Time: 01:21:32  Notes: SMOKE INSIDE - FEMALE CALLER CANNOT GET OUT   Inc: F11021062
 164 MELROSE AV EL  X1: PEMBROKE AV X2: EMERSON AV Nature: FIRE-OTH WIRES/TRANSFORMER,  W/HAZARDS Time: 17:21:01  Inc: F11023150
 
Contact: support@active911.com
[ALERT! Upper Darby (*)] 1220 BLYTHE AV UD X1: DESMOND AVXtWOWNSHIP LINE RD Nature: FIRE-OTH FD\nINVESTIGATION Time: 23:42:27 Notes: 2 PTS Inc: Fq20p703 AM\n
[ALERT! Upper Darby (*)] 2314 GARRETT RD UD : @DON PAPA'S BAKERY X1: OWEN AV X2: WINDERMERE AV\nNature: ALARM AUTOMATIC FIRE ALARM Time: 12:20:36 Notes: BASEMENT Inc:\nF12006791 AM\n
[ALERT! Upper Darby (*)] 6430 MARKET ST UD : ASTRA FOODS INC X1: SEPTA X2: S MILLBOURNE AV\nNature: FIRE-OTH FD INVESTIGATION Time: 06:37:29 Inc: F12006748 AM\n

 */
 
 public class PADelawareCountyBParser extends FieldProgramParser {
   
   private AddressField addressField;
   
   private boolean crossAddress;
   
   public PADelawareCountyBParser() {
     super(CITY_CODES, "DELAWARE COUNTY", "PA",
           "ADDR/S X1:XADDR? X2:X? Nature:CALL! Time:TIME Notes:INFO? Inc:ID");
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     crossAddress = false;
    return super.parseMsg(body.replace('\n', ' '), data);
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
      data.strPlace = p.getLastOptional(':');
      if (data.strPlace.startsWith("@")) data.strPlace = data.strPlace.substring(1).trim();
       data.strApt = p.getLastOptional(',');
       super.parse(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY APT PLACE";
     }
   }
   
   private class CrossAddressField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (data.strAddress.length() == 0) {
         crossAddress = true;
         addressField.parse(field, data);
       } else {
         super.parse(field, data);
       }
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (crossAddress) {
         data.strAddress = append(data.strAddress, " & ", field);
       } else {
         super.parse(field, data);
       }
     }
   }
   
   @Override
   public Field getField(String name) {
     if (addressField == null) addressField = new MyAddressField();
     if (name.equals("ADDR")) return addressField;
     if (name.equals("XADDR")) return new CrossAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "AL", "ALDAN",
       "AS", "ASTON TWP",
       "BE", "BETHEL TWP",
       "BM", "BIRMINGHAM TWP",
       "BR", "BROOKHAVEN",
       "CC", "CHESTER",
       "CF", "CHADDS FORD TWP",
       "CH", "CHESTER HEIGHTS",
       "CL", "CLIFTON HEIGHTS",
       "CN", "CONCORD TWP",
       "CO", "COLLINGDALE",
       "CW", "COLWYN",
       "DB", "DARBY",
       "DT", "DARBY TWP",
       "ED", "EDDYSTONE",
       "EG", "EDGEMONT TWP",
       "EL", "EAST LANSDOWNE",
       "ES", "ESSINGTON", // SECTION OF TINNICUM TWP
       "FL", "FOLCROFT",
       "GL", "GLENOLDEN",
       "HV", "HAVERFORD TWP",
       "LA", "LANSDOWNE",
       "LC", "LOWER CHICHESTER TWP",
       "LS", "LESTER", // SECTION OF TINNICUM TWP
       "MB", "MILLBOURNE",
       "MD", "MIDDLETOWN TWP",
       "ME", "MEDIA",
       "MH", "MARCUS HOOK",
       "MO", "MORTON",
       "MP", "MARPLE TWP",
       "NP", "NETHER PROVIDENCE TWP",
       "NT", "NEWTOWN TWP",
       "NW", "NORWOOD",
       "PK", "PARKSIDE",
       "PP", "PROSPECT PARK",
       "RN", "RADNOR TWP",
       "RP", "RIDLEY PARK",
       "RT", "RIDLEY TWP",
       "RU", "RUTLEDGE",
       "RV", "ROSE VALLEY",
       "SH", "SHARON HILL",
       "SP", "SPRINGFIELD TWP",
       "SW", "SWARTHMORE",
       "TC", "CHESTER TWP",
       "TN", "TINNICUM TWP",
       "TR", "TRAINER",
       "UC", "UPPER CHICHESTER TWP",
       "UD", "UPPER DARBY TWP",
       "UL", "UPLAND",
       "UP", "UPPER PROVIDENCE TWP",
       "YE", "YEADON"
      
   });
 }
