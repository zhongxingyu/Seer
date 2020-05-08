 package net.anei.cadpage.parsers.DE;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Sussex County, DE
Contact: Richard Short <rshort7815@gmail.com>
Sender: cad@sussexcountyde.gov 
(CAD Alert) 78ST   Call at: 21303 Airport Rd         Loc: TEST CALL TEST CALL      City:19947  Problem:  Structure Fire                  Inc#: 2012-018545   
(CAD Alert) Inc#: 2012-018946 78AS3  Call at: 10064 Memory Rd          Loc:                          City:19952  Problem:  Sick Person(Specific Diag)-BLS   D
(CAD Alert) Inc#: 2012-018835 78AST  Call at: 14514 Oak Rd             Loc:                          City:19950  Problem:  Assault/Sexual Assault-BLS       D
(CAD Alert) 78AST  Call at: 6300 Seashore Hwy        Loc: USE BACK DOOR            City:19933  Problem:  Chest Pain-ALS                  Inc#: 2012-018955    

 Contact: CodeMessaging
 Sender: @c-msg.net
 
 Sta: Inc#: 2012-018227 78AS3 Loc:: 14042 Mile Stretch Rd Loc: City: :19950 ---: Sick Person(Specific Diag)-BLS DISP: 16:23:46 Cross St: ADAMS RD/STONE LN
 Sta: 78AST Loc:: Redden Rd & Sussex Hwy Loc: City: :19933 ---: Traffic/TransportationAcdntBLS Inc#: 2012-016829 Lat: 38749167 Long: 75593147 DISP: 17:43:24 Cross St:
 Sta: Inc#: 2012-018395 78AST Loc:: 12046 Sunset Ln Loc: Country Rest Home, new seCity: :19950 ---: Interfacillity/Palliative-ALS DISP: 12:11:29 Cross St: YODER DR/MARELLA LN
 Sta: 78AST Loc:: 6496 Seashore Hwy Loc: white hse black suv in drCity: :19933 ---: Convulsions/Seizures-ALS Inc#: 2012-016825 Lat: 38759789 Long: 75637263 DISP: 16:16:57 Cross St: POLK RD/TWIN LANES FARM RD
 Sta: Inc#: 2012-018289 78ST Loc:: 12502 Woodbridge Rd Loc: City: :19950 ---: Breathing ---s-ALS DISP: 12:12:50 Cross St: MC DOWELL RD/DEER MEADOWS RD
 Sta: Inc#: 2012-018284 78AST Loc:: 11120 - 11123 Coon Den RdLoc: 11192 City: :19950 ---: Sick Person(Specific Diag)-BLS DISP: 11:40:15 Cross St: JOHNSON HUNT RN/SHAWNEE RD
 Sta: 78ST Loc:: 19 Haven Dr Loc: City: :19947 ---: Breathing ---s-ALS Inc#: 2012-018709 Lat: 38656946 Long: 75321836 DISP: 08:10:20 Cross St: PETERKINS RD/OAK KNOLL CT
 Sta: Inc#: 2012-018708 78ST Loc:: 292 Staytonville Rd Loc: tanker compant assist StaCity: :19952 ---: Structure Fire DISP: 08:08:35 Cross St: WOLFS TRAIL END DR/WOODYARD RD
 Sta: 78AS2 Loc:: 7 W Market St Loc: City: :19950 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017442 DISP: 12:35:34 Cross St: Dead End/N CHURCH ST
 Sta: Inc#: 2012-018217 78AS3 Loc:: 13469 Wolf Rd Loc: City: :19950 ---: Assault/Sexual Assault-BLS DISP: 14:40:31 Cross St: OWENS RD/Dead End
 Sta: 78C Loc:: 12808 Mennonite School RdLoc: City: :19950 ---: Alarm Company Inc#: 2012-016589 DISP: 01:28:21 Cross St: WOOD LN/VANITY LN
 Sta: 78AS3 Loc:: 13600 - 13738 Mile StretcLoc: stage City: :19950 ---: Overdose/Poisoning-ALS Inc#: 2012-016170 DISP: 20:12:27 Cross St: JAMESWAY LN/SCOTTS STORE RD
 Sta: 78AS3 Loc:: 11595 Sussex Hwy Loc: City: :19950 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017516 DISP: 08:55:58 Cross St: NANTICOKE BUSINESS PARK DR/GREENWOOD RD
 Sta: 78C Loc:: 0 Seachase Ln Loc: Seachase of Greenwood 304City: :19950 ---: Appliance Fire Inc#: 2012-017559 DISP: 18:55:11 Cross St: Cart Branch Cir/Cart Branch Cir
 Sta: 78AS1 Loc:: 13230 Hunters Cove Rd Loc: City: :19950 ---: Stroke-ALS Inc#: 2012-017266 DISP: 16:49:32 Cross St: Dead End/OWENS RD
 Sta: 78AS3 Loc:: 6190 Hickman Rd Loc: City: :19950 ---: Chest Pain-ALS Inc#: 2012-016107 DISP: 22:35:27 Cross St: NINE FOOT RD/Dead End
 Sta: 78AST Loc:: 100 W Market St Loc: City: :19950 ---: Psychiatric/Abnormal Behvr-BLS Inc#: 2012-016101 DISP: 21:26:39 Cross St: N CHURCH ST/N FIRST ST
 Sta: 78AS3 Loc:: 0 Lester Ave Loc: yellow and white trailor City: :19950 ---: Headache-BLS Inc#: 2012-016006 Lat: 38789084 Long: 75596637 DISP: 20:20:43 Cross St: ALPHA ST/ADAMS RD
 Sta: 78AS3 Loc:: 205 Governors Ave Loc: City: :19950 ---: Breathing ---s-ALS Inc#: 2012-016260 DISP: 20:19:51 Cross St: SUSSEX AVE/MARYLAND AVE
 Sta: 78AST Loc:: 6788 Whiteleysburg Rd Loc: amb 78 City: :19952 ---: Psychiatric/Abnormal Behvr-BLS Inc#: 2012-017161 DISP: 11:48:09 Cross St: BROWNSVILLE RD/Dead End
 Sta: 78AS1 Loc:: Sussex Hwy & Greenwood RdLoc: City: :19950 ---: Traffic/TransportationAcdntBLS Inc#: 2012-017619 DISP: 13:18:01 Cross St:
 Sta: 78C Loc:: 13149 Mennonite School RdLoc: City: :19950 ---: Outside Fire Inc#: 2012-017948 DISP: 16:11:32 Cross St: VANITY LN/BEACH HWY
 Sta: 78AST Loc:: Sussex Hwy & Newton Way Loc: n/b City: :19950 ---: Traffic/TransportationAcdntBLS Inc#: 2012-018120 Lat: 38767148 Long: 75592781 DISP: 15:21:34 Cross St:
 Sta: 78AS3 Loc:: 8862 Greenwood Rd Loc: City: :19950 ---: Convulsions/Seizures-ALS Inc#: 2012-016681 DISP: 01:19:12 Cross St: FIRST ST/NANTICOKE BUSINESS PARK DR
 Sta: 78AS2 Loc:: 2985 Seashore Hwy Loc: City: :19950 ---: Unconscious/Fainting(Near)-ALS Inc#: 2012-017090 DISP: 13:20:29 Cross St: WOODENHAWK RD/WALNUT TREE FARM RD
 Sta: 78AST Loc:: Tower Hill Rd & S Dupont Loc: amb 78 assist sta 50 City: :19952 ---: Traffic/TransportationAcdntBLS Inc#: 2012-017409 DISP: 01:10:36 Cross St:
 Sta: 78AST Loc:: 502 E Market St Loc: Discover Bank City: :19950 ---: Falls-BLS Inc#: 2012-017901 DISP: 10:45:15 Cross St: Sussex Hwy/Duck Creek Ln
 Sta: Inc#: 2012-017990 78AS3 Loc:: 300 W Market St Loc: City: :19950 ---: Abdominal Pain-ALS DISP: 08:15:44 Cross St: N FIRST ST/S 2ND ST
 Sta: 78AST Loc:: Redden Rd & Apple Tree RdLoc: City: :19933 ---: Traffic/TransportationAcdntALS Inc#: 2012-018637 Lat: 38748040 Long: 75551423 DISP: 11:57:02 Cross St:
 Sta: 78ST Loc:: 14530 Sparrow Dr Loc: City: :19950 ---: Chest Pain-ALS Inc#: 2012-017078 DISP: 10:45:22 Cross St: ADAMS RD/Dead End
 Sta: 78AST Loc:: 18427 - 18501 Sussex Hwy Loc: S/B City: :19933 ---: Traffic/TransportationAcdntALS Inc#: 2012-017808 Lat: 38729900 Long: 75591016 DISP: 12:38:11 Cross St: RIFLE RANGE RD/CONNECTING ROAD
 Sta: 78AST Loc:: 4003 Seashore Hwy Loc: City: :19933 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017809 DISP: 13:09:24 Cross St: WOODENHAWK RD/WALNUT TREE FARM RD
 Sta: Inc#: 2012-018589 78AS1 Loc:: 12733 Shawnee Rd Loc: City: :19950 ---: Chest Pain-ALS DISP: 21:29:15 Cross St: VICTORY LN/BEACH HWY
 Sta: 78AS3 Loc:: 8460 - 8518 Hickman Rd Loc: City: :19950 ---: Traffic/TransportationAcdntBLS Inc#: 2012-015931 Lat: 38808690 Long: 75600119 DISP: 07:32:05 Cross St: DELAWARE DR/W MARKET ST
 Sta: 78AST Loc:: 13384 Saint Johnstown Rd Loc: City: :19950 ---: Diabetic ---s-BLS Inc#: 2012-016052 Lat: 38804634 Long: 75575759 DISP: 11:45:43 Cross St: SHAWNEE RD/SUGAR HILL RD
 Sta: 78AST Loc:: 2985 Seashore Hwy Loc: City: :19950 ---: Chest Pain-ALS Inc#: 2012-016130 DISP: 10:04:39 Cross St: WOODENHAWK RD/WALNUT TREE FARM RD
 Sta: 78AST Loc:: 15639 Sharps Mill Rd Loc: City: : ---: Convulsions/Seizures-ALS Inc#: 2012-016126 DISP: 08:40:41 Cross St:
 Sta: 78AS3 Loc:: 5 W Market St Loc: City: :19950 ---: Traumatic Injuries-BLS Inc#: 2012-016324 DISP: 13:16:55 Cross St: Dead End/N CHURCH ST
 Sta: 78AS3 Loc:: 600 E Market St Loc: royal farms City: :19950 ---: Hemorrhage/Lacerations-ALS Inc#: 2012-016531 DISP: 11:32:21 Cross St: DUCK CREEK LN/SUSSEX HWY
 Sta: 78AS3 Loc:: 12608 Webb Farm Rd Loc: City: :19950 ---: Stroke-ALS Inc#: 2012-016972 DISP: 06:12:40 Cross St: CENTURY FARM RD/BEACH HWY
 Sta: 78AST Loc:: Hickman Rd & Scotts StoreLoc: City: :19950 ---: Traffic/TransportationAcdntBLS Inc#: 2012-017076 DISP: 10:16:55 Cross St:
 Sta: 78AST Loc:: 12388 - 12408 Sussex Hwy Loc: smiths City: :19950 ---: Falls-BLS Inc#: 2012-017062 DISP: 08:26:36 Cross St: GREENWOOD RD/CONNECTING ROAD
 Sta: 78AS2 Loc:: 980 Gallo Rd Loc: City: :19952 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017318 DISP: 05:53:01 Cross St: ANDREWVILLE RD/Dead End
 Sta: 78AST Loc:: 2667 Seashore Hwy Loc: City: :19950 ---: Traffic/TransportationAcdntBLS Inc#: 2012-017476 DISP: 18:13:25 Cross St: ADAMSVILLE RD/WOODENHAWK RD
 Sta: 78AST Loc:: 12154 Deer Forest Rd Loc: City: :19933 ---: Hemorrhage/Lacerations-BLS Inc#: 2012-016445 Lat: 38749267 Long: 75540050 DISP: 14:43:34 Cross St: JENNA LN/OAK RD
 Sta: 78C Loc:: 13544 Owens Rd Loc: City: :19950 ---: Field Fire Inc#: 2012-016461 DISP: 16:02:32 Cross St: WOLF RD/LOYDS LN
 Sta: 78AST Loc:: Hunters Cove Rd & Beach HLoc: City: :19950 ---: Traffic/TransportationAcdntALS Inc#: 2012-016476 DISP: 19:00:49 Cross St:
 Sta: 78AST Loc:: 104 N Church St Loc: unit 4 City: :19950 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-016742 DISP: 20:00:54 Cross St: W MINOR ST/BROAD ST
 Sta: 78AS3 Loc:: 8182 Stone Ln Loc: City: :19950 ---: Falls-BLS Inc#: 2012-016867 DISP: 23:58:58 Cross St: MILE STRETCH RD/Dead End
 Sta: 78AST Loc:: 103 Sussex Ave Loc: City: :19933 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-016933 Lat: 38738785 Long: 75598120 DISP: 17:44:55 Cross St: S MAIN ST/LAWS ST
 Sta: 78AST Loc:: 416 Market St Loc: Jeffs Tap Room City: :19933 ---: Burns(Scalds)/Explosion-BLS Inc#: 2012-016951 Lat: 38742585 Long: 75604883 DISP: 21:07:15 Cross St: Mill St/N Cannon St
 Sta: 78AST Loc:: Rock Rd & Sussex Hwy Loc: City: :19950 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017031 DISP: 18:51:25 Cross St:
 Sta: 78AST Loc:: 2381 Andrewville Rd Loc: City: :19950 ---: Breathing ---s-ALS Inc#: 2012-017041 DISP: 22:26:18 Cross St: PROSPECT CHURCH RD/Dead End
 Sta: 78C Loc:: 12018 Adamsville Rd Loc: City: :19950 ---: House Fire Inc#: 2012-017043 DISP: 23:16:29 Cross St: SQUIRREL LN/Dead End
 Sta: 78AST Loc:: 14577 Sussex Hwy Loc: City: :19950 ---: Breathing ---s-ALS Inc#: 2012-017493 DISP: 23:35:28 Cross St: CONNECTING ROAD/CAMPBELL LN
 Sta: 78AST Loc:: 14458 Owens Rd Loc: City: :19950 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017570 DISP: 21:02:53 Cross St: CLARDON FARM LN/TUCKERS RD
 Sta: 78AST Loc:: 12551 Beach Hwy Loc: City: :19950 ---: Choking-BLS Inc#: 2012-017676 DISP: 21:55:33 Cross St: JUDY RD/BLACKSMITH SHOP RD
 Sta: 78AS3 Loc:: 12456 Woodbridge Rd Loc: City: :19950 ---: Sick Person(Specific Diag)-BLS Inc#: 2012-017681 DISP: 23:09:59 Cross St: MC DOWELL RD/DEER MEADOWS RD
 Sta: 78AS3 Loc:: 0 E Market St Loc: Yoder's Market City: :19950 ---: Abdominal Pain-BLS Inc#: 2012-017726 DISP: 14:02:39 Cross St: Dead End/RAILROAD AVE
 Sta: 78AST Loc:: 1735 Woodyard Rd Loc: assist r47 City: :19952 ---: Abdominal Pain-BLS Inc#: 2012-017732 DISP: 15:54:19 Cross St: COON DEN RD/STAYTONVILLE RD
 Sta: 78AST Loc:: 13978 Mile Stretch Rd Loc: City: :19950 ---: Convulsions/Seizures-ALS Inc#: 2012-017843 DISP: 19:52:37 Cross St: CHAMPION LN/JAMESWAY LN
 Sta: 78AST Loc:: 508 N Cannon St Loc: City: :19933 ---: Assault/Sexual Assault-BLS Inc#: 2012-017845 Lat: 38746952 Long: 75603992 DISP: 20:54:38 Cross St: OAK ST/SECOND ST
 Sta: Inc#: 2012-018334 78AS3 Loc:: 499 Governors Ave Loc: Woodbridge Elementary SchCity: :19950 ---: Breathing ---s-ALS DISP: 20:07:09 Cross St: Sussex Hwy/Queen Ave
 Sta: Inc#: 2012-018512 78AS2 Loc:: 12036 Sussex Hwy Loc: Greenwood Inn/ RM11 City: :19950 ---: Headache-BLS DISP: 21:09:24 Cross St: Greenwood Rd/Nanticoke Business Park Dr
 
  */
 
 public class DESussexCountyBParser extends FieldProgramParser {
   
   private static final Pattern ALT_START_SEQ = Pattern.compile("Sta: +Inc#: +([-\\d]+) +(.*)");
   
   public DESussexCountyBParser() {
     super(CITY_CODES, "SUSEX COUNTY", "DE",
            "Inc0:ID? Sta:SRC! Loc0:ADDR! Loc:PLACE! City:CITY! ---:CALL! Inc#:ID? Lat:GPS? Long:GPS? DISP:TIME Cross_St:X");
   }
   
   @Override
   public String getFilter() {
    return "@c-msg.net,cad@sussexcountyde.gov";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    
    // CodeMessaging does some data massaging.  Which we have to duplicate if we are getting the page direct from dispatch
    if(subject.equals("CAD Alert")) {
      body = "Sta: " + body.replace(" Call at:", "Loc::").replace(" Problem:", " ---:");
    }
    
     body = ALT_START_SEQ.matcher(body).replaceAll("Inc0: $1 Sta: $2");
     body = body.replace(" Loc::", " Loc0:").replace("City: :", "City:").replace("Loc:", " Loc:").replace("City:", " City:");
     return super.parseMsg(body, data);
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("0 ")) field = field.substring(2).trim();
       super.parse(field, data);
     }
   }
   
   private class MyGPSField extends GPSField {
     @Override
     public void parse(String field, Data data) {
       int len = field.length();
       if (len >= 6) {
         field = field.substring(0,len-6) + '.' + field.substring(len-6);
       }
       data.strGPSLoc = append(data.strGPSLoc, " ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("GPS")) return new MyGPSField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "19330", "BETHANY BEACH",
       "19331", "BETHEL",
       "19333", "BRIDGEVILLE",
       "19339", "DAGSBORO",
       "19933", "BRIDGEVILLE",
       "19940", "DELMAR",
       "19941", "ELLENDALE",
       "19944", "FENWICK ISLAND",
       "19945", "FRANKFORD",
       "19947", "GEORGETOWN",
       "19950", "GREENWOOD",
       "19951", "HARBESON",
       "19952", "HARRINGTON",
       "19956", "LAUREL",
       "19958", "LEWES",
       "19960", "LINCOLN",
       "19963", "MILFORD",
       "19966", "MILLSBORO",
       "19967", "MILLVILLE",
       "19968", "MILTON",
       "19969", "NASSAU",
       "19970", "OCEAN VIEW",
       "19971", "REHOBOTH BEACH",
       "19973", "SEAFORD",
       "19975", "SELBYVILLE"
   });
 }
 
 
