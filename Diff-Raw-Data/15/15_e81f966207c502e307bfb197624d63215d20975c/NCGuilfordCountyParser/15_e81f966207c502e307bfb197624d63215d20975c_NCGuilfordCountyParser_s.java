 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Guilford County, NC
 Contact: Shane Taylor <dstaylor9@gmail.com>
 Sender: @edispatches.com 
 System: OSSI CAD?
 [summerfieldfiredist Jan24-10:01]  CAD:SMFD;ST09;1;52C03C;FIRAL;7607 BLUE SAGE CT
 [summerfieldfiredist Jan19-18:15]  CAD:SMFD;ST29;1;50PI;SCALESVILLE RD/LAKE BRANDT RD
 [summerfieldfiredist Jan18-19:08]  CAD:1;MUTUAL;150 WAYNOR DR;GALLENT RD;STRUCTURE FIRE
 (summerfieldfiredist Jan18-17:00) CAD:SMFD;ST29;1;25D01;PSYCH;8579 HUDSON JAMES RD;WRIGHTS FARM LN
 [summerfieldfiredist Jan18-03:16]  CAD:SMFD;ST39;1;29B01;50PI;PLAINFIELD RD/LAKE BRANDT RD;BETWEEN LAKE BRANDT AND CHURCH ST
 (summerfieldfiredist Jan21-03:25) CAD:SMFD;ST09;1;67B01;OSFIR;2523 PLEASANT RIDGE RD;SADDLEWOOD DR;DIST: 35.58 FT
 (summerfieldfiredist Jan23-06:49) CAD:SMFD;ST39;1;17B03;FALL;6251 LAKE BRANDT RD;HI-LO LN
 [summerfieldfiredist Jan23-19:20]  CAD:OKRD;ST51;1;69D11;STRUC;4529 PEEPLES RD;RIDING TRAIL CT
 [summerfieldfiredist Jan23-19:18]  CAD:OKRD;ST51;1;STRUC;4529 PEEPLES RD;RIDING TRAIL CT
 [summerfieldfiredist Jan23-20:23]  CAD:OKRD;ST51;1;69D06;STRUC;5371 NORTHWEST SCHOOL RD;TRAIL PIEDMONT GREENWAY
 (summerfieldfiredist Jan24-10:31) CAD:UNDER CONTROL;7607 BLUE SAGE CT; SU
 (summerfieldfiredist Jan23-11:25) CAD:UNDER CONTROL;6308 PEPPERMILL DR; OAK
 (summerfieldfiredist Jan23-19:48) CAD:UNDER CONTROL;4529 PEEPLES RD; OAK
 (summerfieldfiredist Jun10-01:00) summerfieldfiredist\nCAD:UNDER CONTROL;5322 BUNCH RD; SU
 (summerfieldfiredist Jun10-19:27) summerfieldfiredist\nCAD:FD13;ST58;1;60C02;GAS;210 JAMES DOAK PKWY;BRIXHAM DR
 
 Contact: krpaschal@gmail.com <krpaschal@gmail.com>
 Sender: CAD@greensboro-nc.gov
 CAD@greensboro-nc.gov:CAD:CODE 47;7700 NEWSOME CT; B;CARDIAC / RESPIRATORY ARREST
 
 Contact: "Michael Capps" <firefightercapps@yahoo.com>
 FRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun11-00:23\nMSG:summerfieldfiredist\nCAD:UNDER CONTROL;210 JAMES DOAK PKWY; G\n
 1 of 2\nFRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun17-16:22\nMSG:summerfieldfiredist\nCAD:OPA4;OPA4;1;MEDEM;5570 US 220 N;M AND M\n(Con't) 2 of 2\nTIRE AND AUTOMOTIVE;SPOTSWOOD RD\n(End)
 FRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun17-14:57\nMSG:summerfieldfiredist\nCAD:SMFD;ST39;1;09E02;CARDIA;5718 OAK TREE RD;AIR HARBOR RD\n
 
 Contact: Greg Creech <gcreech1551@gmail.com>
 CAD:UNDER CONTROL;878 CROSSCREEK RD; K ;. [27]
 
 Contact: Matt Young <myoung92939@gmail.com>
 Contact: John Stanley <summerfield248@gmail.com>
 Contact:Brian Bradley <bkbradley@gmail.com>
 Sender: dispatch@edispatches.com
 Subject:summerfieldfiredist Aug06-16:44\nsummerfieldfiredist \nCAD:SMFD;ST09;1;31D02;UNCON;7912 SUMMERFIELD RD;FAIRGROVE BAPTIST CHURCH; 
 Subject:summerfieldfiredist Aug06-06:57\nsummerfieldfiredist \nCAD:OKRD;ST15;1;69D03;STRUC;8005 MARKETPLACE DR;TRACTOR SUPPLY CO;NC 68 N 
 Subject:summerfieldfiredist Aug08-11:36\nsummerfieldfiredist \nCAD:SMFD;ST39;1;19D01;HEART;1007 NC 150 W;SPEAR CLINIC;LAKE BRANDT RD;ROO 
 Subject:summerfieldfiredist Aug23-03:00\nsummerfieldfiredist\nCAD:SMFD;ST29;1;18C04;HEAD;8562 HUDSON JAMES RD;WRIGHTS FARM LN\n
 Subject:summerfieldfiredist Aug23-02:49\nsummerfieldfiredist\nCAD:SMFD;ST09;1;25B03;PSYCH;7619 BRISBANE DR;OAK RIDGE RD\n
 Subject:summerfieldfiredist Aug21-19:49\nsummerfieldfiredist\nCAD:SMFD;ST09;1;23C02A;OVDOSE;2307 SCALESVILLE RD;ROBINSON RD;HOUSE\n
 Subject:summerfieldfiredist Aug21-16:02\nsummerfieldfiredist\nCAD:OKRD;ST51;1;STRUC;6401 W BELGRAVE TER\n
 S:summerfieldfiredist Oct17-12:45 M:summerfieldfiredist\nCAD:UNDER CONTROL;808 JAMES DOAK PKWY; G\n\n
 Subject:summerfieldfiredist Oct17-13:50\nsummerfieldfiredist\nCAD:SMFD;ST29;1;MED;180 QUAL RIDGE RD;SADDLEBROOK DR\n
 S:summerfieldfiredist Oct21-13:14 M:summerfieldfiredist\nCAD:CODE 47;330 GEMSTONE LOOP; ROCK;ST 29 CARDIAC ARREST CALL\n\n
 
 Contact: Jamie Burgess <jrburgess87@gmail.com>
 Sender: 93001024 
 CAD@greensboro-nc.gov:CAD:MCVL;ST37;1;60B01;GAS;922 GOLF HOUSE ROAD EAST;HADDINGTON RD;ACROSS FROM THIS ADDRESS
 
 Contact: Brian Bradley <bkbradley@gmail.com>
 Subject:summerfieldfiredist Oct27-23:05\nsummerfieldfiredist\nCAD:UNDER CONTROL;300 NIBLICK DR; ROCK;REF FALL\n
 Subject:summerfieldfiredist Nov06-22:05\nsummerfieldfiredist\nCAD:SMFD;ST09;1;STROKE;5109 MEDEARIS ST;AYERS LN\n
 
 Contact: Jenna Daniels <ffjdaniels@gmail.com>
 Sender: dispatch@edispatches.com
 Subject:summerfieldfiredist Nov26-03:56\nsummerfieldfiredist\nCAD:SMFD;ST29;1;MED;132 LAKE DR;OLD MILL DR\n
 Subject:summerfieldfiredist Nov27-19:50\nsummerfieldfiredist\nCAD:SMFD;ST29;1;OSFIR;1359 SCALESVILLE RD;CARTER FARM RD\n
 
 Contact: Jamie Burgess <jrburgess87@gmail.com>
 Contact: Craig Hollar <spanky4142@gmail.com>
 Sender: firedistrict13@listserve.com
 Subject:[Firedistrict13] (no subject)\nCAD:UNDER CONTROL;616 SIMPSON-CALHOUN RD; G ;TAC 10\n\n_____________________________________
 Subject:[Firedistrict13] (no subject)\nCAD:FD13;ST55;1;29D02p;50PI;US 29 N/HICONE RD;29 NB NORTH OF HICONE\n\n_____________________
 Subject:[Firedistrict13] (no subject)\nCAD:FD13;ST58;1;26B01;SICK;7830 SUTTER RD;NC 150 W\n\n______________________________________
 Subject:[Firedistrict13] (no subject)\nCAD:FD13;ST55;1;03D00;ANBITE;2706 BRAME RD;WHITE ELDER RD;DIST: 245.80 FT\n\n_______________
 Subject:[Firedistrict13] (no subject)\nCAD:UNDER CONTROL;2706 BRAME RD; G ;DIST: 245.80 FT\n\n_____________________________________
 Subject:[Firedistrict13] (no subject)\nCAD:FD13;ST55;1;29D02p;50PI;US 29 N/HICONE RD;29 NB NORTH OF HICONE\n\n_____________________
 Subject:[Firedistrict13] (no subject)\nCAD:FD13;ST13;1;FIRAL;5207 OLD MINE RD\n\n_______________________________________________\nF
 Subject:[Firedistrict13] (no subject)\nCAD:NEFD;ST32;1;EXPLO;6303 THREE LOY RD\n\n_______________________________________________\n
 Subject:[Firedistrict13] (no subject)\nCAD:FD13;ST55;1;FIRAL;4006 STARLING CT\n\n_______________________________________________\nF
 Subject:[Firedistrict13] (no subject)\nCAD:NEFD;ST33;1;52B01S;FIRAL;7707 FRENCH DR;YANCEYVILLE RD;JOHNSON WILLIMS\n\n______________
 
 Contact: Active911
 ???
 Contact: Active911.com
 Agency name: Stokesdale Fire Department Location: Stokesdale, NC 
 From: CAD@greensboro-nc.gov
 CAD:STKD;ST12;1;SICK;7717 ATHENS RD;SOUTHERN MEADOWS DR;26C02
 CAD:UNDER CONTROL;7717 ATHENS RD; ST
 CAD:SMFD;ST29;1;OSFIR;US 158/HUDSON RD;67D02B
 CAD:STKD;ST12;1;FIRAST;7848 US 158;ATHENS RD
 CAD:UNDER CONTROL;7848 US 158; ST
 CAD:STKD;ST12;1;MED;1895 GIDEON GROVE CHURCH RD;EDDIES TR;CHEST PAIN
 CAD:UNDER CONTROL;1895 GIDEON GROVE CHURCH RD; ROCK; CHEST PAIN
 CAD:OKRD;ST15;1;50PI;NC 68 N/FOGLEMAN RD;29B04
 CAD:STKD;ST12;1;STROKE;8971 OSAGE RD;TYSOR RD
 CAD:UNDER CONTROL;8971 OSAGE RD; ROCK
 CAD:STKD;ST12;1;MED;160 SEVEN CREEKS DR;FOREST RIDGE DR;CHEST PAIN
 CAD:UNDER CONTROL;160 SEVEN CREEKS DR; ROCK;CHEST PAIN
 CAD:STKD;ST12;1;STROKE;7700 US 158;COUNTRYSIDE MANOR;ATHENS RD;ROOM A10 ASSISTED LIVING;28C10U
 CAD:UNDER CONTROL;7700 US 158; ST ;ROOM A10 ASSISTED LIVING
 CAD:STKD;ST12;1;ABDOM;7242 US 158;B & G CT;01C05
 CAD:UNDER CONTROL;7242 US 158; ST
 CAD:OKRD;ST15;1;50PI;NC 68 N/E HARRELL RD;DIST: 33.67 FT;29B01
 CAD:STKD;ST12;1;ANBITE;8255 CURRAGHMORE CT;STOKESDALE PARK;CURRAGHMORE DR;DIST: 517.83 ft;03D00
 CAD:UNDER CONTROL;8255 CURRAGHMORE CT; ST ;DIST: 517.83 ft
 CAD:1;MUTUAL;13863 US 158;GLENCOE CHURCH LOOP;FIRE ALARM
 CAD:STKD;ST12;1;FIRAST;435 HUMPHREYS RIDGE DR;HUMPHREYS RIDGE MARINA;WEST RIDGE CIR
 CAD:UNDER CONTROL;435 HUMPHREYS RIDGE DR; ROCK
 CAD:STKD;ST12;1;BREATH;8129 FLATROCK RD;06D02
 CAD:UNDER CONTROL;8129 FLATROCK RD; ST
 CAD:STKD;ST12;1;UNCON;7848 US 158;ATHENS RD;31C00
 CAD:UNDER CONTROL;7848 US 158; ST
 CAD:STKD;ST12;1;MED;8306 PATRICIA DR;US 158
 CAD:STKD;ST12;1;MED;8312 SOUTHARD RD;US 158;HOUSE
 CAD:UNDER CONTROL;8306 PATRICIA DR; ST
 CAD:STKD;ST12;1;MEDEM;8312 SOUTHARD RD;US 158;HOUSE
 CAD:UNDER CONTROL;8312 SOUTHARD RD; ST ;HOUSE
 CAD:STKD;ST12;1;MED;151 DIONNE WAY;MOUNT ZION CHURCH RD;21-D-3 HEMORRHAGE
 CAD:UNDER CONTROL;151 DIONNE WAY; ROCK;21-D-3 HEMORRHAGE
 CAD:1;MUTUAL;361 SARDIS CHURCH RD;LINDSEY BRIDGE RD;FIRE ALARM
 CAD:STKD;ST12;1;50PI;8945 BELEWS CREEK RD;SHEDAN RD;DIST: 109.55 ft;29D02l
 CAD:UNDER CONTROL;8945 BELEWS CREEK RD; ST ;DIST: 109.55 ft
 CAD:STKD;ST12;1;FIRAST;US 158/BELEWS CREEK RD
 CAD:CANCEL;US 158/BELEWS CREEK RD; ST
 CAD:STKD;ST12;1;FALL;8303 KNIGHT VILLAGE CIR;ATHENS RD;HOUSE;17B03
 CAD:UNDER CONTROL;8303 KNIGHT VILLAGE CIR; ST ;HOUSE
 CAD:STKD;ST12;1;BLEED;8108 BAYWOOD DR;NC 68 N;21D04
 CAD:UNDER CONTROL;8108 BAYWOOD DR; ST
 CAD:STKD;ST12;1;SEIZUR;8401 US 158;STOKESDALE FIRE ST 12;SHILLING ST
 CAD:UNDER CONTROL;8401 US 158; ST
 CAD:1;MUTUAL;SIMPSON RD/GIDEON GROVE CHURCH RD
 CAD:1;MUTUAL;SIMPSON RD/GIDEON GROVE CHURCH RD;STRUCT
 CAD:OKRD;ST15;1;FIRAL;8315 HAW RIVER RD;ANTHONY RD
 CAD:UNDER CONTROL;8315 HAW RIVER RD; OAK
 CAD:STKD;ST12;1;MED;366 FRIDDLE RD;PIN OAK DR;FALL
 CAD:STKD;ST12;1;FIRAST;366 FRIDDLE RD;PIN OAK DR;CARDIAC
 CAD:UNDER CONTROL;366 FRIDDLE RD; ROCK;CARDIAC
 CAD:STKD;ST12;1;FALL;7258 US 158;B & G CT;17B01G
 CAD:UNDER CONTROL;7258 US 158; ST
 CAD:STKD;ST12;1;FIRAST;4860 ELLISBORO RD;CAROUSEL LN
 CAD:UNDER CONTROL;4860 ELLISBORO RD; ROCK
 CAD:OKRD;ST15;1;FIRAL;2205 OAK RIDGE RD;LOWES FOOD;CADET DR;SUITE B
 CAD:STKD;ST12;1;STRUC;8107 DORSETT DOWNS DR;WESSEX DR;HOUSE;69D06
 CAD:STKD;ST12;1;BURN;8401 US 158;STOKESDALE FIRE ST 12;SHILLING ST
 CAD:UNDER CONTROL;8401 US 158; ST
 CAD:STKD;ST12;1;SERV;3774 ELLISBORO RD;WOODFORD LN
 CAD:STKD;ST12;1;50PI;564 NC 68 N;SYLVANIA RD;10-50 UNKN
 CAD:UNDER CONTROL;564 NC 68 N; ROCK;10-50 UNKN
 CAD:STKD;ST12;1;STRUC;4329 ELLISBORO RD;RENN RD;ROCKINGHAM
 CAD:UNDER CONTROL;4329 ELLISBORO RD; ROCK
 CAD:STKD;ST12;1;MED;138 GROVE DR;GIDEON GROVE CHURCH RD;FALL 17-B
 CAD:UNDER CONTROL;138 GROVE DR; ROCK;FALL 17-B
 CAD:STKD;ST12;1;MED;454 SADDLEBRED LOOP;SHETLAND CT
 CAD:STKD;ST12;1;UNCON;7300 US 158;B & G CT;APT 406;31D02
 CAD:STKD;ST12;1;FALL;8473 SOUTHARD RD;THOMAS RD;17D03
 CAD:UNDER CONTROL;8473 SOUTHARD RD; ST
 CAD:STKD;ST12;1;FALL;8598 BELEWS CREEK RD;WINDING HILL DR;DIST: 43.09 FT;28C10U
 CAD:STKD;ST12;1;ALLERG;7151 ELLISON RD;OAK CREEK DR;02C01M
 CAD:UNDER CONTROL;7151 ELLISON RD; ST
 CAD:STKD;ST12;1;FIRAST;112 KINGSBRANCH DR;OGBURN MILL RD
 CAD:STKD;ST12;1;MED;112 KINGSBRANCH DR;OGBURN MILL RD
 CAD:UNDER CONTROL;112 KINGSBRANCH DR; ROCK
 CAD:STKD;ST12;1;BREATH;8401 US 158;STOKESDALE FIRE ST 12;SHILLING ST
 CAD:UNDER CONTROL;8401 US 158; ST
 CAD:STKD;ST12;1;TRAUM;7605 NC 68 N;FOOD LION 2146;CHARTRESS RD;30B01
 CAD:UNDER CONTROL;7605 NC 68 N; ST
 CAD:1;FIRAST;1133 SARDIS CHURCH RD;ELLISBORO RD;STATION 150.....REF FILL IN
 CAD:1;SERV;8440 SHEWING ST
 CAD:STKD;ST12;1;SERV;8410 SHILLING ST;NEWBERRY ST
 CAD:UNDER CONTROL;8410 SHILLING ST; ST
 CAD:UNDER CONTROL;1133 SARDIS CHURCH RD; ROCK;STATION 150--FILL IN
 CAD:STKD;ST12;1;MED;345 OGBURN MILL RD;EXCALIBER DR;CHEST PAIN
 CAD:UNDER CONTROL;345 OGBURN MILL RD; ROCK;CHEST PAIN
 CAD:1;MUTUAL;9069 BELEWS CREEK RD;PLUNKETT RD
 CAD:POVS 10-22 RESPONSE;9069 BELEWS CREEK RD; FORS
 CAD:1;SERV;8205 CANDY
 CAD:STKD;ST12;1;SERV;8205 KANDI DR;WENDY GAYLE DR
 CAD:UNDER CONTROL;8205 KANDI DR; ST
 CAD:STKD;ST12;1;FIRAST;NC 68 N/SYLVANIA RD;WASHDOWN
 CAD:UNDER CONTROL;NC 68 N/SYLVANIA RD; ROCK;WASHDOWN
 CAD:1;MUTUAL;7335 PEGRAM RD;BELEWS CREEK RD;FIRE ALARM
 CAD:STKD;ST12;1;FALL;7500 US 158;B & G CT;COUNTRYSIDE MANOR;17D02
 CAD:UNDER CONTROL;7500 US 158; ST ;COUNTRYSIDE MANOR
 CAD:SMFD;ST09;1;OSFIR;SCALESVILLE RD/ROBINSON RD;DIST: 10.55 FT
 CAD:1;MUTUAL;158/WHITTY
 CAD:STKD;ST12;1;SERV;8480 SOUTHARD RD;THOMAS RD;53A05
 CAD:UNDER CONTROL;8480 SOUTHARD RD; ST
 CAD:STKD;ST12;1;MED;4585 ELLISBORO RD;CHRISTOPHER RD;10-50 PI PIN IN
 CAD:UNDER CONTROL;4585 ELLISBORO RD; ROCK;10-50 PI PIN IN
 
 */
 
 public class NCGuilfordCountyParser extends DispatchOSSIParser {
   
   private static final Pattern MARKER = Pattern.compile("^[\\w@\\-\\.]+? *[\n:](?=CAD:)");
   
   public NCGuilfordCountyParser() {
     super("GUILFORD COUNTY", "NC",
            "( CALL2 ADDR! | PRI/Z MUTUAL ADDR! | ( SRC SRC PRI | PRI ) CODE? CALL ADDR! ) XINFO+");
   }
   
   @Override
   public String getFilter() {
     return "@edispatches.com,93001,firedistrict13@listserve.com,CAD@greensboro-nc.gov";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (match.find()) {
       body = body.substring(match.end()).trim();
     }
     
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     if (!super.parseMsg(body, data)) return false;
     
     // If out of county mutual aid call, cancel the default county
     if (data.strCall.equals("MUTUAL")) data.defCity = "";
     return true;
   }
 
 
 
   private class Call2Field extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.length() < 6) return false;
       parse(field, data);
       return true;
     }
   }
   
   private class MutualField extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.equals("MUTUAL")) return false;
       parse(field, data);
       return true;
     }
   }
   
   private class MySourceField extends SourceField {
     @Override
     public void parse(String field, Data data) {
       data.strSource = append(data.strSource, "-", field);
     }
   }
   
   private class CrossInfoField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       if (field.equalsIgnoreCase("ROCK")) {
         data.strCity = "ROCKINGHAM COUNTY";
       } else if (checkAddress(field) > 0) {
         data.strCross = append(data.strCross, " & ", field);
       } else {
         data.strSupp = append(data.strSupp, " / ", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "X CITY INFO";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL2")) return new Call2Field();
     if (name.equals("MUTUAL")) return new MutualField();
     if (name.equals("SRC")) return new MySourceField();
    if (name.equals("PRI")) return new PriorityField("\\d");
     if (name.equals("CODE")) return new CodeField("\\d\\d[A-Z]\\d\\d[A-Za-z]?");
     if (name.equals("XINFO")) return new CrossInfoField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
 }
