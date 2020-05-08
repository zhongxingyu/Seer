 package net.anei.cadpage.parsers.CO;
 
import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 20442,ALARMF,23691 CR 60H.E1 E4 L1,TEXT:AUDIBLE FROM GENERAL AND SMOKE DETECTOR \COMP:1ST CLASS SECURITY \PH:800 482 9800,
 20431,SMKODR,1ST ST/8TH AV.E4 L1,TEXT:SMELL OF NATURAL GAS ALL OVER THE AREA \COMP:W012,
 20373,SMKODR,10TH AV/22ND ST.E4 R1,TEXT:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA \COMP:MIKE RUANE \PH:608 630 2959,
 20032,SMKODR,2114 BLUEBELL AV.E4 R1,TEXT:FROM THE SOUTH, STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM,
 19897,ALARMF,2013 1ST AV.E4 E1 L1,TEXT:AUDIBLE COMING FROM PULL STATION  NW DOOR, NO ATC, PREM PH 970 674 1957 \COMP:OP 311 SHERLOCK PLUS \PH:800 443
 19547,AIRAF ,600 AIRPORT RD.E4,TEXT:PLANE COMING IN WITHOUT LANDING GEAR.  RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY. \COMP:MANDY \PH:336-3000,
 22668,TAUF  ,11TH AV/18TH ST.E2 R1 WA,TEXT:RP HIT A GUY IN CROSSWALK,HE\nIS TRYING TO WALK AWAY,
 
 Contact: Doug Gilliland <dgilliland88@gmail.com>
 Sender: CommtechMessenger@mauser.greeleypd.com
   / 10903,SI    -SICK & INJ (F&A),3103 23RD AV.E2 WA,,\n\n\n
   / 11470,SI    -SICK & INJ (F&A),3136 51ST AV.E5 WA,TEXT:RIB PAIN,FELL A WEEK \nAGO \COMP:DEL \PH:314-226-9976,\n\n\n
 
 Contact: Greg McGinn <GMcGinn@ftluptonfire.com>
 Contact: Joshua Tapia <jtapia0145@gmail.com>
 Contact: JC Langley <ashaman01@gmail.com>
 Contact: Ryan Fuller <rfuller131@gmail.com>
 Contact: Bernard Covillo <bcovillo@gmail.com>
 Contact: Kirk Bauer <kbauer620@gmail.com>
 Contact: JC Langley <ashaman01@gmail.com>
 Sender: unpredictable
 " " (Dispatch) 22452,FIREVF-FIRE VEHICLE (F),15899 NANCY AV.26,TEXT:TRUCK ON FIRE, NO \nONE INSIDE 2 OUTSIDE, TXT STOP to opt-out
 " " (Dispatch) 22440,TAUF  -TA UNK INJURY(F),1108 LANTERN DR.26 PM3,,FG10 TXT STOP to opt-out
 " " (Dispatch) 22422,SIPFF -SICK & INJ   (F),905 S MCKINLEY AV.26 PM3,TEXT:FEM FAINTED/WON'T \nWAKE UP NOW \COMP:LUCY GALLEGOS \SOURCE:E911 \PH:720.985.1607,FG10 TXT STOP to opt-out
 " " (Dispatch) 22411,SIPFF -SICK & INJ   (F),15125 GOOD AV.26 PM3,TEXT:HUSB IN THE TUB, \nUNCONS/BREATHING \COMP:VALERIE JENKINS \PH:303*710*9486, TXT STOP to opt-out
 " " (Dispatch) 22407,SI    -SICK & INJ (F&A),9643 CR 23.26 PM3,TEXT:CO ALARM GOING OFF \n\COMP:LYNETTE \PH:303 857 4432, TXT STOP to opt-out
 " " (Dispatch) 22452,FIREVF-FIRE VEHICLE (F),15899 NANCY AV.26,TEXT:TRUCK ON FIRE, NO \nONE INSIDE 2 OUTSIDE, TXT STOP to opt-out
 " " (Dispatch) 22570,SIPFF -SICK & INJ   (F),1300 DEXTER ST.26,TEXT:MAN FELL , BLEEDING \nFROM HEAD BADLY \COMP:MICHELLE \PH:7209388915, TXT STOP to opt-out
 " " (Dispatch) 22606,ATSUCF-ATTP SUICIDE (F),2926 GLEN DALE DR.24 WA,TEXT:OVERDOSE \COMP:DIANA \n\LOC:ALI - SHOWS #6 \PH:9706738057, TXT STOP to opt-out
 " " (Dispatch) 22648,SIPFF -SICK & INJ   (F),3804 BURLINGTON AV.24 WA,, TXT STOP to opt-out
 " " (Dispatch) 23039,FIREGF-FIRE GRD CVR (F),103 N JOSEPHINE AV.32 29,TEXT:TREE W/ A POWER \nLINE TOUCH IT AND ITS SMOKING \COMP:DAVID \PH:405-8450,FG5 TXT STOP to opt-out
 " " (Dispatch) 21196,SIPFF -SICK & INJ   (F),805 COMPASSION DR.WI4201 WA 42,TEXT:RES FELL \n\COMP:AUDREY \PH:970.686.2743, TXT STOP to opt-out
 " " (Dispatch) 26965,SIPFF -SICK & INJ   (F),3400 LATHAM AV.24 WA,TEXT:SISTER ILL AND \nFELL \COMP:PEGGY PANTOJA \PH:3369*3860, TXT STOP to opt-out
 " " (Dispatch) 26974,SIPFF -SICK & INJ   (F),3501 MAGNOLIA ST.24 WA,TEXT:RPS MOTHER SICK \COMP:ESMERALA \PH:970 405 3476, TXT STOP to opt-out
 
 Contact: Kevin Hudson <motorworks3@gmail.com>
 Sender: 93001026
 Dispatch,21461,SIPFF -SICK & INJ (F),CR 61/HY 34.37 WA,TEXT:NORTH OF HY 34 1 MILE, \nPATIENT IN A SEMI VOMITING \COMP:JOE ROBERTSON \PH:303-579-1849,FG3
 Dispatch,21489,TAIF -TA W/INJURY (F),CR 49/HY 34.37 WA,TEXT:HEAD ON\n\COMP:CHAD\n\n\PH:970.744.8045,FG7
 Dispatch,21489,TAIF -TA W/INJURY (F),CR 49/HY 34.37 WA,TEXT:HEAD ON\n\COMP:CHAD\n\n\PH:970.744.8045,FG7
 Dispatch,21461,SIPFF -SICK & INJ (F),CR 61/HY 34.37 WA,TEXT:NORTH OF HY 34 1 MILE,\n\nPATIENT IN A SEMI VOMITING \COMP:JOE ROBERTSON \P
 Dispatch,21415,SIPFF -SICK & INJ (F),24098 CR 50.37 WA,TEXT:RP NEEDING\nHELP OFF\n\nTHE FLOOR \COMP:EVELYN \PH:970.284.7774,FG5
 Dispatch,21377,FASIST-ASSIST (FIRE),21000 I 76.28,,
 Dispatch,21367,SIPFF -SICK & INJ (F),6876 CR 47.28 PVA56,TEXT:CHEST PAIN \COMP:JOSH\n\n\PH:303 319 9255,FG9
 Dispatch,21355,TAIF -TA W/INJURY (F),201 E BISON HY.28A PVA56,TEXT:BLU NEON, DIDN'T\n\nROLL. \COMP:NICKI \PH:970-215-5558,FG9
 
 Weld County, CO
 Contact: Kaleb Staley <kalebstaley@gmail.com>
 Sender: 86245
 Fr:<Basepage@weldcorcc.com>\nSu:Dispatch\nTxt: 27849,SI -SICK & INJ (F&A),CR 55/CR 62.37 WA,FG 5\n\n\nid:7
 
Contact: Doug Gilliland <dgilliland88@gmail.com>
Sender: CommtechMessenger@weldcorcc.com
Dispatch / 01417,TAIF  -TA W/INJURY (F),65TH AV/HY 34 BY.E5 R1 WA,TEXT:2 VEHS, INJURIES \n\COMP:DANIEL \PH:970 397 9175,\n\n\n

  */
 
 
 public class COWeldCountyParser extends FieldProgramParser {
   
  private static final Pattern MARKER = Pattern.compile("^Dispatch *[,/] *");
   private static final Pattern DELIM = Pattern.compile("[,\\\\]");
 
   public COWeldCountyParser() {
     super("WELD COUNTY", "CO",
           "ID CALL ADDR! TEXT:INFO INFO2+ COMP:NAME INFO+ PH:PHONE");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     body = body.replace('\n', ' ').replaceAll("  +", " ");
    Matcher match = MARKER.matcher(body);
    if (match.find()) body = body.substring(match.end());
     String[] flds = DELIM.split(body);
     return super.parseFields(flds, data);
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{5}"), true);
     }
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.lastIndexOf('.');
       if (pt < 0) abort();
       data.strUnit = field.substring(pt+1).trim();
       super.parse(field.substring(0,pt).trim(), data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " UNIT";
     }
   }
   
   private class MyInfoField extends InfoField {
     
     private String delim;
     
     public MyInfoField(boolean cont) {
       delim = (cont ? ", " : " / ");
     }
     
     @Override
     public void parse(String field, Data data) {
       data.strSupp = append(data.strSupp, delim, field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("INFO")) return new MyInfoField(false);
     if (name.equals("INFO2")) return new MyInfoField(true);
     return super.getField(name);
   }
 }
