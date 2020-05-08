 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Bucks County, PA
 Contact: "Ryan Pankoe" <rpankoe@gmail.com>
 Sender: 8276,a@bnn.us
 
 911:SQ134  type:BTAI    adr:S LIMEKILN PK/UPPER STATE RD ,48  aai:  box:74064  map:3033F7  tm:21:05:23  ED1042264
 911:SQ134  type:ARESP   adr:4217 HORSESHOE WY ,36  btwn BERRY BROW DR &amp; SPARROW WY  aai:  box:60002  map:2921B10  tm:05:02:04  ED1042293  
 911:SQ134  type:ARESP   adr:221 HAMPSHIRE DR ,48  btwn CORNWALL DR &amp; SURREY RD  aai:  box:74062  map:3033E6  tm:14:06:02  ED1042334  
 911:SQ134  type:AFAINT  adr:1553 LOWER STATE RD ,29  btwn BRISTOL RD &amp; OLD NEW RD  aai:  box:78054  map:3034E7  tm:20:28:45  ED1042385  
 911:SQ134  type:BTAI    adr:1235 S TOWNSHIP LINE RD ,36  btwn CHALFONT RD &amp; LIMEKILN PK  aai:  box:61003  map:2921D10  tm:12:02:56  ED1042577  
 911:SQ134  type:BBLED   adr:102 HARRISON FORGE CT ,48  btwn E FAIRWOOD DR &amp; CUL DE SAC  aai:  box:34030  map:3033H1  tm:14:38:26  ED1042595  
 911:SQ134  type:BINJ    adr:BARC HOUSE ,47 at 80 PUEBLO RD ,47 btwn LENAPE DR &amp; UTE RD  aai:DOWNSTAIRS  box:34033  map:3033J2  tm:16:57:04  ED1043086  
 911:STA19  type:FALRM   adr:DTH ,29 at 595 W STATE ST ,29 btwn PROGRESS DR &amp; RT 611 BY  aai:215 345-2200  box:19051  map:3034G2  tm:12:37:42  FD101999
 911:SQ134  type:ASEIZ   adr:BRISTOL RD/UPPER STATE RD ,29  aai:AT ENTR TO BLOOMING GLEN  box:34041  map:3033K5  tm:11:01:28  ED1043679
 911: STA19  type:FUNK    adr:EDISON RD/RT 611 ,29  aai:  box:79028  map:3034K5  tm:12:44:52  FD1108375    Run: E79    \n
 
 0001/0002 911:SQ134  type:ATRAN   adr:2343 BETHLEHEM PK #312 ,36 -- DOCK MEADOWS NH btwn UNIONVILLE PK &amp; CHURCH RD  aai:GO TO CHURCH RD NORTH ENTRANCE  
 0002/0002 box:60063 map:3032E3 &nbsp;tm:14:48:06 ED1042598&nbsp;
 
 0001/0002 911: SQ134  type:BFALL   adr:800 MANOR DR #315 ,48 -- NEW SEASONS ASSIST btwn E SCHOOL HOUSE RD & COUNTY LINE  aai:  box:74066 map:3033E8
 0002/0002 tm:16:50:43  ED1101813
 
 0001/0002 911: SQ134  type:AALLR   adr:4371 COUNTY LINE RD ,48 -- VERTIS DIRECT MKTG btwn BRITAIN WOOD CI & RR CROSSING  aai:  box:74096 map:3033C7
 0002/0002 tm:09:28:45  ED1102262
 
 Contact: Terry <securetalk@gmail.com>
 Sender: 2674243300
 911: STA57  type:FEMS    adr:SLEEPY HOLLOW RD/SPINNERSTOWN RD ,45  aai:  box:75011  map:2693K9  tm:10:26:44  FD1104574    Run: E75
 911: STA57  stype:FTAI    adr:FRIER RD/MILL RD ,45  aai:  box:57002  map:  tm:13:51:38  FD1104463    Run: E57
 
 Contact: Ed Ackerman <ed.ackerman@gmail.com>
 911: STA4  type:RAUTO   adr:PHILMONT AV/E STREET RD ,43  aai:PHILMONT SO STREET  box:04003  map:3261B8  tm:10:21:22  FD1104774    Run: R4 R6 E1
 
 Contact: "Russ Swinehart Jr." <snoopfire10@yahoo.com>
 911: STA53  type:RMAR    adr:LIONS PARK ,24 at 102 SAMUEL CLIFT ST ,24 btwn MILL ST & MARKET ST  aai:  box:51012  map:3375D5  tm:00:59:10  FD1105678 Run: R53 M53 M11
 911: STA53  type:WIREIN  adr:2218 TRENTON AV ,24  btwn BARRY PL & TAFT ST  aai:  box:53034  map:3375F2  tm:11:40:15  FD1104667    Run: L25 Q51 E50 E52
 911: STA53  stype:RAUTO   adr:NEW FALLS RD/PINE AV ,25  aai:  box:12044  map:3262G9  tm:21:30:34  FD1107837    Run: R13 R53 E82
  
  */
 
 
 public class PABucksCountyParser extends FieldProgramParser {
   
   public PABucksCountyParser() {
     super("BUCKS COUNTY", "PA",
            "911:SRC! type:CALL! adr:ADDR! btwn:X aai:INFO box:BOX map:MAP tm:ID% Run:UNIT");
   }
   
   @Override
   public String getFilter() {
     return "8276,@bnn.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     int pt = body.indexOf("911:");
     if (pt < 0) return false;
     data.expectMore = true;
     
     body = body.replaceAll(" btwn ", " btwn:").replaceAll(" stype:", " type:").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&").trim();
     return super.parseMsg(body, data);
   }
   
   private class MyCallField extends CallField {
     
     @Override
     public void parse(String field, Data data) {
       String desc = TYPE_CODES.getProperty(field);
       if (desc != null) field = field + " - " + desc;
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String sAddr, Data data) {
       Parser p = new Parser(sAddr);
       data.strPlace = p.getOptional(" at ");
       if (data.strPlace.length() == 0) data.strPlace = p.getLastOptional("--");
       if (data.strPlace.length() > 0) {
         int pt = data.strPlace.indexOf(',');
         if (pt >= 0) data.strPlace = data.strPlace.substring(0, pt).trim();
       }
       String cityCode = p.getLastOptional(',');
       super.parse(p.get(), data);
       if (cityCode.length() > 0) {
         try {
           int iCity = Integer.parseInt(cityCode);
           iCity = iCity - INIT_TOWN_CODE;
           if (iCity >= 0 && iCity < TOWN_CODES.length){
             data.strCity = TOWN_CODES[iCity];
           }
         } catch (NumberFormatException ex) {}
       }
     }
   }
     
   private class MyIdField extends IdField {
     
     @Override
     public void parse(String tm, Data data) {
       if (tm != null) {
         Parser p = new Parser(tm);
         data.strCallId = p.getLastOptional(' ');
       }
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
   
   private static final int INIT_TOWN_CODE = 21;
   private static final String[] TOWN_CODES = new String[]{
   /*21*/ "BEDMINSTER TWP",
   /*22*/ "BENSALEM TWP",
   /*23*/ "BRIDGETON TWP",
   /*24*/ "BRISTOL",
   /*25*/ "BRISTOL TWP",
   /*26*/ "BUCKINGHAM TWP",
   /*27*/ "CHALFONT",
   /*28*/ "DOYLESTOWN",
   /*29*/ "DOYLESTOWN TWP",
   /*30*/ "",
   /*31*/ "DUBLIN",
   /*32*/ "DURHAM TWP",
   /*33*/ "EAST ROCKHILL",
   /*34*/ "FALLS TWP",
   /*35*/ "HAYCOCK TWP",
   /*36*/ "HILLTOWN TWP",
   /*37*/ "HULMEVILLE",
   /*38*/ "IVYLAND",
   /*39*/ "LANGHORNE",
   /*40*/ "",
   /*41*/ "LANGHORNE MANOR",
   /*42*/ "LOWER MAKEFIELD TWP",
   /*43*/ "LOWER SOUTHAMPTON TWP",
   /*44*/ "MIDDLETOWN TWP",
   /*45*/ "MILFORD TWP",
   /*46*/ "MORRISVILLE",
   /*47*/ "NEW BRITAIN",
   /*48*/ "NEW BRITAIN TWP",
   /*49*/ "NEW HOPE",
   /*50*/ "",
   /*51*/ "NEWTOWN",
   /*52*/ "NEWTOWN TWP",
   /*53*/ "NOCKAMIXON TWP",
   /*54*/ "NORTHAMPTON TWP",
   /*55*/ "PENNDEL",
   /*56*/ "PERKASIE",
   /*57*/ "PLUMSTEAD TWP",
   /*58*/ "QUAKERTOWN TWP",
   /*59*/ "RICHLAND TWP",
   /*60*/ "",
   /*61*/ "RICHLANDTOWN",
   /*62*/ "RIEGELSVILLE",
   /*63*/ "SELLERSVILLE",
   /*64*/ "SILVERDALE",
   /*65*/ "SOLEBURY TWP",
   /*66*/ "SPRINGFIELD TWP",
   /*67*/ "TELFORD",
   /*68*/ "TINICUM TWP",
   /*69*/ "TRUMBAUERSVILLE",
   /*70*/ "",
   /*71*/ "TULLYTOWN",
   /*72*/ "UPPER MAKEFIELD TWP",
   /*73*/ "U SOUTHAMPTON TWP",
   /*74*/ "WARMINSTER TWP",
   /*75*/ "WARRINGTON TWP",
   /*76*/ "WARWICK TWP",
   /*77*/ "PENNRIDGE REGIONAL PD",
   /*78*/ "WRIGHTSTOWN TWP",
   /*79*/ "YARDLEY"
   };
   
   private static final Properties TYPE_CODES = buildCodeTable(new String[]{
       "AIRC",     "AIRPLANE CRASH (TAC)",
       "ACARDA",   "Cardiac/Respiratory Arrest",
       "BOMB",     "BOMB THREAT",
       "BURN",     "BURN VICTIM (LOC)",
       "CBURN",    "CONTROLLED BURN",
       "COALRM",   "CARBON MONOXIDE ALARM (LOC )",
       "DECON",    "DECON ASSIGNMENT (LOC)",
       "ELEVAT",   "STUCK ELEVATOR (LOC)",
       "EXBLD",    "EXTINGUISHED STRUCTURE (TAC)",
       "FALRM",    "FIRE ALARM (LOC)",
       "FAPL",     "APPLIANCE FIRE (TAC)",
       "FAPT",     "APARTMENT FIRE (BOX)",
       "FAST",     "FIRE ASSIST (OUT OF COUNTY)",
       "FAUTO",    "AUTOMOBILE (LOC)",
       "FBARN",    "BARN FIRE (TAC)",
       "FBLD",     "BUILDING FIRE (BOX)",
       "FBOAT",    "BOAT FIRE (LOC)",
       "FBRU",     "BRUSH FIRE (LOC)",
       "FBUS",     "BUS FIRE (LOC)",
       "FCHIMN",   "CHIMNEY FIRE (TAC)",
       "FCOV",     "FIRE STATION COVER",
       "FDRILL",   "FIRE DRILL",
       "FDUMP",    "DUMPSTER FIRE (LOC)",
       "FDWL",     "DWELLING FIRE (TAC)",
       "FEXP",     "EXPLOSION (LOC)",
       "FEMS",     "FIRE ASSIST EMS (LOC)",
       "FGARAG",   "GARAGE FIRE (TAC)",
       "FGRILL",   "GRILL FIRE (LOC)",
       "FHAZ",     "HAZMAT FIXED LOCATION (TAC)",
       "FHOSP",    "HOSPITAL FIRE",
       "FINFO",    "FIRE INFORMATION",
       "FINV",     "FIRE INVESTIGATION (LOC)",
       "FIRE",     "FIRE OTHERS (LOC)",
       "FIRCAL",   "FIRE CALL (EMS)",
       "FMPAGE",   "FM PAGE REQUEST",
       "FNURS",    "NURSING HOME FIRE (BOX)",
       "FOBLD",    "OUTBUILDING FIRE (TAC)",
       "FPAGE",    "FD PAGE REQUEST",
       "FPOL",     "FIRE POLICE REQUEST",
       "FRUB",     "RUBBISH FIRE (LOC)",
       "FSCHOL",   "SCHOOL FIRE (BOX)",
       "FSPEC",    "SPECIAL ASSIGNMENT",
       "FSPILL",   "FUEL LEAK INCIDENT (LOC)",
       "FSTORE",   "SMALL STORE FIRE (TAC)",
       "FTA",      "ACCIDENT W/FIRE (LOC)",
       "FTAI",     "ACCIDENT W/FIRE & INJ (LOC)",
       "FTHAZ",    "TRANSPORTATION HAZMAT",
       "FTRAIN",   "TRAIN FIRE (TAC)",
       "FTRUCK",   "TRUCK FIRE (LOC)",
       "FUMEIN",   "FUMES INSIDE STRUCTURE (TAC)",
       "FUMES",    "FUMES OUTSIDE (LOC)",
       "FUNK",     "UNKNOWN TYPE FIRE",
       "RAUTO",    "AUTO EXTRICATION",
       "RMAR",     "MARINE RESCUE",
       "WIREIN",   "WIRES IN DWELLING",
 
       "AABDO",      "ACUTE ABDOMEN",
       "AALLR",      "ANAPHYLAXIS",
       "ABLED",      "ACUTE HEMORRHAGE",
       "ABURN",      "CRITICAL BURN VICTIM",
       "ACHESP",     "CHEST PAINS (CARDIAC SYMPT.)",
       "ACHOKE",     "OBSTRUCTED AIRWAY (ACTIVE)",
       "ACOP",       "CARBON MONOXIDE POISONING",
       "ACVA",       "CVA/STROKE",
       "ADIAB",      "DIABETIC EMERGENCY",
       "ADROWN",     "DROWNING - CRITICAL",
       "AELEC",      "ELECTRICAL SHOCK VICTIM",
       "AENVIR",     "ENVIRONMENTAL EMERGENCY",
       "AFAINT",     "SYNCOPAL EPISODE",
       "AFALL",      "FALL VICTIM - CRITICAL",
       "AGUN",       "GUNSHOT VICTIM",
       "AHRI",       "ALS HIT & RUN ACCIDENT WITH INJURY",
       "AOBG",       "ALS OB/GYN EMERGENCY",
       "AOD",        "OVERDOSE/POISONING - CRITICAL",
       "APREG",      "MATERNITY - CRITICAL",
       "ARESP",      "RESPIRATORY DISTRESS",
       "ASEIZ",      "SEIZURES - ACTIVE",
       "ASTAB",      "STABBING VICTIM",
       "ATAI",       "ALS TRAFFIC ACCIDENT INJURIES",
       "ATRAN",      "ALS/UNSPECIFIED EMERGENCY",
       "ATRAUM",     "MULTI-SYSTEMS TRAUMA",
       "AUNC",       "UNCONSCIOUS SUBJECT",
       "AUNR",       "UNRESPONSIVE SUBJECT",
 
       "BABDO",      "ABDOMINAL PAIN",
       "BALLR",      "ALLERGIC REACTION",
       "BBLED",      "BLEEDING (NON TRAUMATIC)",
       "BCHESP",     "CHEST PAIN  (NON CRITICAL)",
       "BINJ",       "MINOR INJURY - NON CRITICAL",
       "BLAC",       "ANIMAL BITE",
       "BPSYCH",     "PSYCHIATRIC TRANSPORTATION",
       "BOD",        "OVERDOSE/POISONING - NON CRITICAL",
       "BPREG",      "MATERNITY - NON CRITICAL",
       "BUNK",       "BLS/UNKNOWN PROBLEM",
       "BBURN",      "SCALDING/MINOR BURNS",
       "BDROWN",     "AQUATIC ACCIDENT (NON CRITICAL)",
       "BFALL",      "FALL VICTIM - NON CRITICAL",
       "BLAC",       "LACERATION/CONTROLLED BLEEDING",
       "BPTAST",     "PATIENT ASSIST",
       "BTAI",       "BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
       "BTRAN",      "EMERGENCY TRANSPORTATION",
   });
 }
