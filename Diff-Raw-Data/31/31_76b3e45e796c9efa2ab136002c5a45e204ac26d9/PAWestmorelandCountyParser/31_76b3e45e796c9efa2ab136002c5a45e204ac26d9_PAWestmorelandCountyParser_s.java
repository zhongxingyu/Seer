 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Westmoreland County, PA
 Contact: Barry Karelitz <bkemt6@gmail.com>
 Contact: Anthony Collins <anthonycollins48@gmail.com>
Sender: alert@emgcall.net
 
 Loc: 118 S 4TH ST YNGWD_B X-sts: CHESTNUT ST & LOCUST ST  Inc#:F11000861 NATURE: FIRE  CALLER: ANTHONY GALADIAY TOC: 19:08:40 Comments: FLAMES COMING OUT OF THE WALL  Disp: E26-1
 Loc: 400 PAINTERSVILLE RD HEMP_T: @SUPER VALUE PERISHABLE WAREHOUSE X-sts: S CENTER AVE &   Inc#:F11001007 NATURE: AFA  CALLER: ADT....KANDRA TOC: 06:46:18 Comments: GENERAL & DRY RISER #9 WATER FLOW WATC  Disp: E28,E25-2,TL26,E27
 Loc: 325 N CENTER AVE N_STANTN_B: @KFC X-sts: WESTMORE AVE & HIGHLAND AVE Inc#:F11001017 NATURE: FIRE  CALLER: STEVEN NELSON TOC: 10:33:44 Comments: WATER IS COMING IN THRU THE LIGHTS POSS ELECTRICAL FIRE HE SAW SMOPKE IN THE KITC E26-1,E25-2,E19,E27
 Loc: 911 S MAIN ST SW_GBG_B: @WESTMORELAND COUNTY BLIND ASSOCIATION X-sts: CRIBBS ST & GREEN ST  Inc#:F11001035 NATURE: AFA  CALLER: MARK TOC: 15:58:53 Comments: WATER FLOW ALARM MAAS NOTF  Disp: L23,E32-1,TL26,E24-1
 Loc: 407 CHESTNUT ST SW_GBG_B X-sts: STANTON ST & BRANDON ST  Inc#:F11000412 NATURE: AFA  CALLER: SCHUR NANCY TOC: 11:14:39 Comments: APT A AUDIBLE FIRE ALARM ACTIVATED CALLER DOES NOT WERE ITS COMING FROM DOES NOT KNOW WERE ITS COMING FROM  Disp: L23,E32-1,TL26,E24-1
 Loc: 800 I 76 E MT_PLEAS_T X-sts: RAMP EXIT 75 TO I 76 E & RAMP I 76 E TO EXIT 91  Inc#:F11001058 NATURE: VF  CALLER: KEN GAY TOC: 06:31:36 Comments: IN THE MIDDLE OF THE ROAD  Disp: E25-2,E26-1
 Loc: 680 I 76 W HEMP_T X-sts: RAMP EXIT 75 TO I 76 W & RAMP I 76 W TO EXIT 57  Inc#:F11000422 NATURE: 29B06  CALLER: EVAN ROBINSONS TOC: 13:46:00 Comments: 1 VEH UNK INJS Response text: Bravo Responder script: Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown.Unknown number of patients involved. Traffic / Transportation Accidents. 1.The accident involves a single vehicle. 2.It's not known if chemicals or other hazards are involved. 3.It's not known if anyone is pinned. 4.It's not known if anyone was thrown from the vehicle. 5.It's not known if everyone is completely awake (alert). 6.Their injuries are described as other than to a NOT DANGEROUS area. 7.There is no bleeding now. -079.645493 +040.26574 Duplicate Event:Location = 680 I 76 W HEMP_T, Cross Street 1 = RAMP EXIT 75 TO I VEH ACC FEMALE HAS BLOOD ON HER FACE 76 W RAMP I 76 W TO EXIT 57, Cross Street 2 = RAMP I 76 W TO EXIT 57, Caller LAYING NEXT TO CAR Name = TOM ALAND, Caller Ph Number = (412)607-7756, Caller Address = 423 HIT MEDIAN MIDDLETOWN RD HEMPFIELD, Call Source = ANI/ALI, Alarm Level = 0 End of Duplicate Event data  Disp: R25,R26
 Loc: 859 ROUTE 119 N_STANTN_B: alias 859 RT 119 N_STANTN_B X-sts: RAMP RT 119 S TO TOLL 66 N & RAMP TOLL 66 N TO SR 3091  Inc#:F11001086 NATURE: 29D02F  CALLER: LORI STRIPAY TOC: 15:06:51 Comments: TRUCK OTed  Disp: R25,R26
 Loc: 2399 RACE TRACK RD HEMP_T: EST X-sts: N CENTER AVE & WILSON FOX RD Inc#:F11000404 NATURE: 29B06  CALLER: BETTY JEAN FALO TOC: 08:05:39 Comments: JUST BEFORE FOX DEN ACRES VEH INTO THE GULLY COMING FROM NEW STANTON ITS ON THE LEFT HAND SIDE UNK INJ Response text: Bravo Responder script: Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown.Unknown number of patients involved. Traffic / Transportation Accidents. 1.The accident involves a single vehicle. 2.It's not known if chemicals or other hazards are involved. 3.It's not known if anyone is pinned. 4.It's not known if anyone was thrown from the vehicle. 5.It's not known if everyone is completely awake (alert). 6.The type and nature of their injuries are not known. 7.It's not known if there is SERIOUS bleeding. Chief complaint: UNK INJ Response description: Bravo  Disp: R26,E25-2,E104 
 X-sts: ROUTE 819 & ALBRIGHTS LAKE RD  Inc#:F11000118 NATURE: FIRE CALLER: CHRIS WENSEL TOC: 17:19:37 Comments:  JUST PAST INTERSECTION ON RIGHT.. WHEN HEADING AWAY FROM GREENSBURG S NOT SURE OF ADDRESS. // IS ON RT 819// // DARK SMOKE ALL AROUND HOUSE AND COMING OUT OF CHIMNEY.. DOES NOT SEE FLAMES UNK IF ANYONE INSIDE THE RES MAAS NOTIF CALLER LIVES AT 465 ALBRIGHT LAKE RD. . MAAS REQ NOT IF ACTUAL FIRE FT 5  Disp: E28,E79-8,E32-1,TL26
 X-sts: ARMBRUST RD & FAIRGROUNDS RD  Inc#:F10019394 NATURE: 29B01 CALLER: PSPG TOC: 21:02:22 Comments:  1 INJ  Disp: R26,E28,R25,R28,E25-2,SS26
 X-sts: HILLIS ST & GENARD LN  Inc#:F10019516 NATURE: 29A01 CALLER: PSPGB TOC: 15:01:13 Comments:  BETWEEN 9 AND WAGNER MAA NOT FIRE TAC 7  Disp: R26
 Loc: 407 N 4TH ST YNGWD_B X-sts: HALLER AVE & OVERHEAD BRIDGE RD Inc#:F11000526 NATURE: AED  CALLER: CRABTREE JAMES T/ RITA TOC: 04:42:15 Comments: 73 YOM / UNCON / NOT BREATHING Response text: Echo Responder script: 73 year old, Male, Unconscious, Not breathing. Cardiac or Respiratory Arrest / Death. CONN MAAS FIRE TAC 10  Disp: E26-1
 Loc: 220 N 5TH ST YNGWD_B X-sts: LINCOLN ST & WASHINGTON AVE  Inc#:F10019293 NATURE: AMBAS CALLER: MAAS TOC: 23:03:07 Comments:  REQ FIRE FOR LIFTING NON EMERGENCY  Disp: E26-1
 
 */
 
 public class PAWestmorelandCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
      "HEMP_T",   "HEMPFIELD TWP",
      "N_STANTN_B","NEW STANTON",
       "MT_PLEAS_T","MT PLEASANT TWP",
      "SW_GBG_B", "SW GREENSBURG",
      "YNGWD_B",  "YOUNGWOOD"
   });
   
   public PAWestmorelandCountyParser() {
     super("WESTMORELAND COUNTY", "PA",
            "Loc:ADDR/S X-sts:X Inc:ID NATURE:CALL! CALLER:NAME TOC:SKIP Comments:INFO Response_text:SKIP Disp:UNIT");
   }
   
   @Override
   public String getFilter() {
    return "alert@emgcall.net,alert@emgcall.net,message@ecm2.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
    body = body.replaceAll("Inc#:", "Inc:");
     if (!super.parseMsg(body, data)) return false;
     
     // Intersections go in the cross street and leave the Loc: field empty
     if (data.strAddress.length() == 0) {
       if (data.strCross.length() == 0) return false;
       data.strAddress = data.strCross;
       data.strCross = "";
     }
     return true;
   }
   
   // Address field parser
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String fld, Data data) {
       Parser p = new Parser(fld);
       data.strPlace = p.getLastOptional(":");
       if (data.strPlace.startsWith("@")) data.strPlace = data.strPlace.substring(1).trim(); 
       data.strCity = convertCodes(p.getLast(' '), CITY_CODES); 
       super.parse(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
