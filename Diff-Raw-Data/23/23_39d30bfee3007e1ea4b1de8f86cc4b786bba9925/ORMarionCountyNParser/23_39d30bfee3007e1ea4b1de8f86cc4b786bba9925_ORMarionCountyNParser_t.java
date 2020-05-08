 package net.anei.cadpage.parsers.OR;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 import net.anei.cadpage.SmsMsgInfo.Data;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Marion County (North), OR
 Contact: Mike Jensen <sfdmike@gmail.com>
 Sender: trex@ci.woodburn.or.us
 System: New World Systems :Aegis Public Safety System
 
 [(49962]  : ) TONE:  819::RAILWAY:AV:::::3030:VTON,STAF,:SAFE COM STA1@1830/APP CKS@1900 EMS@1945 STA1:20110302:174858
 [(45354]  : ) CVA: 1732::PINE:ST:9::::3029:M24,R404,D411,STAF,N7,:35 Y M C/B/A LEFT SIDE PARALYSIS:20110223:110234
 [(47486]  : ) STRC:::ABIQUA:RD:::HW 213::2833:E435,E485,E405,E415,D411,T489,T418,T419,R484,PGE,TIME,STAF,N7,:LARGE FLAMES APPX 2 MILES EAST:20110226:183324
 [(48144]  : ) SICK: 5024::CROOKED FINGER:RD:::::3340:E485,D411,:75YOM C/B/A DIZZY:20110228:043844
 [(50323]  : ) BREA:12884::MASCHER:RD:::::3230:E415,D411,STAF,:85YOF C/A/SOB FAST HEART RATE/150:20110303:140259
 [(50791]  : ) FALL:  930::OAK:ST:130::::3031:M23,R404,D411,N7,:99YOF C/A/B GLF R HIP PAIN:20110304:073911
 [(52678]  : ) ALM: 1106::PINE:ST:::::3030:E405,D411,N7,:CO2 DETECTOR ALRM AT COMM LOC:20110306:215554
 [(52806]  : ) UNC: 5870::VALLEY VIEW:RD:::::3032:M24,R404,D411,N7,:65YOM UNC/NOT BREA:20110307:073728
 [(53886]  : ) MISC:12622::SILVERTON:RD:::::3030:E415,D411,N7,:FLAMES SEEN FROM FIELD NEAR BY:20110308:190734
 [(53944]  : ) SUIC: 1418::PINE:ST:D::::3030:M24,R404,D411,N7,:64YOF/C/B/SUIC ATTEMPT, SLASHED WRISTS:20110308:194443
 [(54072]  : ) MVA:  918:N:FIRST:ST:::::3031:E405,E435,R404,D411,M23,N7,:ONE VEH ROLLOVER IFO ROTHS STORE:20110309:022055
 [(45886]  : ) MVA:::CASCADE:HW:::STATE:ST:3630:E425,D411,R404,M31,SBLE,SBLR,PGE,E415,LAW,N7,:SINGLE VEH ROLLOVER/ENTRAPMENT, NO INJ:20110224:063549
 
 ((56873) : ) MISC: 8170::CASCADE:HW:::::2733:E485,D411,N7,:POWER LINE DOWN W/LARGE TREE BLKING:20110313:152421
 ((56971) : ) AOA:34000:S:ELLIS:RD:::::2737:E485,N7,:LARGE TREE ONTO HOUSE/UNK FURTHER:20110313:154421
 ((57141) : ) UNC: 3351::SEMINOLE:RD:::::3234:M24,R404,D411,N7,:86 YOF UNC/DIFF B/NOT ALERT:20110313:203929
 ((57514) : ) UNC:  728:W:MAIN:ST:::::3030:M24,R404,D411,STAF,N7,:74 YOM IN SEIZ:20110314:124736
 ((57903) : ) MVA:::MT ANGEL:HW:::HOBART:RD:2929:E405,R404,M24,D411,LAW,STAF,N7,:2 VEH MVA/ELDERLY
 ((58711) : ) CVA:  173::STEELHAMMER:RD:::::3031:M24,R404,D411,STAF,N7,:93YOF C/A/B HX OF CVA/NOT ABLE TO SPEAK:20110316:103935
 ((58975) : ) PBLC:  206::WESTFIELD:ST:::::3030:E415,D411,STAF,N7,:REQ D411 RESPOND W/LADDER TO GET IN WINDOW:20110316:141622
 
Contact: robert aerni <aernijr@gmail.com>
(14036) :  / TRAU:10581::SARATOGA:DR:::::2628:R454,M23,N3,:65 YOF C/B/A LEG PAIN FROM FALL FROM FARM EQ:20110728:220453\n
(13523) :  /  UNC:  830:N:MAIN:ST:9::::2530:R454,M22,:77YOM UNC/NOT RESP:20110728:114333\n

 */
 
 public class ORMarionCountyNParser extends FieldProgramParser {
   
  private static final Pattern CALL_ID_PTN = Pattern.compile("\\d{5}");
  private static final Pattern LEAD_PTN = Pattern.compile("^: +[/)] *");
  
   public ORMarionCountyNParser() {
     super("MARION COUNTY", "OR",
            "CALL ADDR1 ADDR1 ADDR1 ADDR1 APT ADDR2 ADDR2 ADDR2 MAP UNIT INFO");
   }
   
   private String address[] = new String[2];
   
   @Override
   public String getFilter() {
     return "trex@ci.woodburn.or.us";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (subject.startsWith("(")) subject = new Parser(subject.substring(1).trim()).get(')');
    if (! CALL_ID_PTN.matcher(subject).matches()) return false;
    data.strCallId = subject;

    Matcher match = LEAD_PTN.matcher(body);
    if (match.find()) body = body.substring(match.end());
     
     address[0] = address[1] = "";
     if (! parseFields(body.split(":"), data)) return false;
     
     data.strAddress = append(address[0], " & ", address[1]);
     return true;
   }
   
   private class MyAddressField extends AddressField {
     
     private int index;
     
     public MyAddressField(int index) {
       this.index = index;
     }
     
     @Override
     public void parse(String fld, Data data) {
       address[index] = append(address[index], " ", fld); 
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String fld, Data data) {
       if (fld.endsWith(",")) fld = fld.substring(0,fld.length()-1);
       super.parse(fld, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR1")) return new MyAddressField(0);
     if (name.equals("ADDR2")) return new MyAddressField(1);
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
 }
