 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Handles parsing for a vendor identified as VisionCAD
 
 Camden County, GA (version 1)
 astudstill@co.camden.ga.us:2011-101695* HIGHWAY 17 STEFFANS* * * KINGSLAND* * Traffic Stop* TRAFFIC STOP* * * 1128,1140,1150,1152,509,514,523,532,LS3,R3* * Med
 astudstill@co.camden.ga.us:2011-104696* 4059 MARTIN LUTHER KING BLVD* N4* * 514,541,546,ENG4,LS4,MED4 KINGSLAND* * INJURY* INJURY* 509,ENG5,LS3* * Medical: No
 astudstill@co.camden.ga.us:2011-101995* 405 & HIGHWAY 40 OLD WAFFLE HOUSE* * * KINGSLAND* * ACCIDENT* ACCIDENT* ELAINE* 386-208-4465* 514,541,546,ENG4,LS4,MED4
 astudstill@co.camden.ga.us:2011-104843* 429 EAGLE BLVD* * * KINGSLAND* * PERSON SICK* PERSON SICK* * 540,ENG4,LS4* * Medical: No* Haz
 214 REDWOOD ST* * * KINGSLAND* * PERSON SICK* PERSON SICK* MS FAGEN*912-269-6157* LS3,R3* * Medical: No* Hazards: No* 
 astudstill@co.camden.ga.us:2011-181161* ADVANCE COLLISION CENTER* * * KINGSLAND* * WIRE DOWN* WIRE DOWN* JOHN* 316-258-1559* ENG4,LS4* * Medical: No* Haz
 astudstill@co.camden.ga.us:2012-037466* BOONE AND SUMMERBROOK* * * KINGSLAND* * INVESTIGATE* INVESTIGATE SUSPICIOUS PERSON/VEHICLE* JUAN RODRIGUEZ* 912-8
 astudstill@co.camden.ga.us:2012-142354* CMC* * * ST MARYS* * * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED3* * Medical: No* Hazards: No* Lin
 astudstill@co.camden.ga.us:2012-142422* 119 TERESA LN* * * KINGSLAND* * * * * DOMESTIC* DOMESTIC PROBLEM* HUNTER* 912-576-1327* 525,531,544,LS3,R3* * Med
 astudstill@co.camden.ga.us:2012-142419* CMC TO SHANDS* * * KINGSLAND* * * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED4* * Medical: No* Hazar
 astudstill@co.camden.ga.us:2012-142755* 102 ALMOND CIR* * * KINGSLAND* * * * * UNCONSCIOUS* UNCONSCIOUS PERSON* * 912-674-6173* 542,ENG4,LS4* * Medical: 
 astudstill@co.camden.ga.us:2012-142422* 119 TERESA LN* * * KINGSLAND* * * * * DOMESTIC* DOMESTIC PROBLEM* HUNTER* 912-576-1327* 525,531,544,LS3,R3* * Med
 astudstill@co.camden.ga.us:2012-143146* 200 CHESTNUT CT* * * KINGSLAND* * * * * INJURY* INJURY* * 912- -* 511,ENG4,KFD,LS4* * Medical: No* Hazards: No* L
 astudstill@co.camden.ga.us:2012-142419* CMC TO SHANDS* * * KINGSLAND* * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED4* * Medical: No* Hazar
 astudstill@co.camden.ga.us:2012-142222* STATION 4* * * KINGSLAND* * * * PERSON SICK* PERSON SICK* * * ENG4,LS4* * Medical: No* Hazards: No* Line18=*
 astudstill@co.camden.ga.us:2012-142193* 2060 HWY 40 E* * * KINGSLAND* * * * FIRE ALARM* FIRE ALARM* * * ENG4,LS2,FIRE3,FIRE4,Q9,LAD3,ENG3,ENG5* Medic
 astudstill@co.camden.ga.us:2012-142755* 102 ALMOND CIR* * * KINGSLAND* * * * UNCONSCIOUS* UNCONSCIOUS PERSON* * 912-674-6173* 542,ENG4,LS4* * Medical:
 astudstill@co.camden.ga.us:2012-143345* SATILLA TO CMC* * * KINGSLAND* * * * * TRANSPORT* PICKUP PRISONER/SUBJECT* * * MED3* * Medical: No* Hazards: No* 
 astudstill@co.camden.ga.us:2012-143364* CMC* * * ST MARYS* * * * * TRANSPORT* PICKUP PRISONER/SUBJECT* * * MED3* * Medical: No* Hazards: No* Line18=*
 astudstill@co.camden.ga.us:2012-143368* 955 S GROVE BLVD* LOT 42* * KINGSLAND* * * * * BREATHING* DIFFICULTY BREATHING* DESTINY* 912- -* 526,542,KFD,LS3,
 
 Northampton County, NC (version 1)
 S: M:Northampton911:* URIAH MARTIN RD // NEAR CEMETERY* * * CONWAY* * FIRE - BRUSH* * * * EMS3,FS20* * Medical: No* Hazards: No* * 
 S: M:Northampton911:* BOAT LANDING* * * WELDON* * MISSING PERS* * * * EMS8,FS20* * Medical: No* Hazards: No* * 
 S: M:Northampton911:* 907 HORNE RD* * * PENDLETON* * FIRE - SMOKE* * * * EMS3,FS18,FS20* * Medical: No* Hazards: No* * 
 S: M:Northampton911:* 203 WHITE ST* * * CONWAY* * ODOR OF GAS* * * * FS20* * Medical: No* Hazards: No* * 
 
 Halifax County, NC (version 0)
 HalifaxCoE911@HalifaxNC911.com S: M:HalifaxCoE911:* JACKSON ST // 7TH ST* * * ROANOKE RAPIDS* * * * * WRECK NOPI* * * * C181,C182,FI14* * * * *
 HalifaxCoE911@HalifaxNC911.com S: M:HalifaxCoE911:* HWY 158 // AVE* * * ROANOKE RAPIDS* * * * * WRECK NOPI* * * * EMS4,EMS6,FI14* * * * *
 HalifaxCoE911@HalifaxNC911.com S: M:HalifaxCoE911:* 1403 EAST 10TH ST* * * ROANOKE RAPIDS* * * * * FIRE-ELEC* * * * FI14* * * * *
 HalifaxCoE911@HalifaxNC911.com S: M:HalifaxCoE911:* PILAND ST* * * ROANOKE RAPIDS* * * * * SPECIAL ASSG* * * * F1402,FI14* * * * *
 HalifaxCoE911@HalifaxNC911.com S: M:HalifaxCoE911:* SUBWAY // JULLIAN R ALLSBROOK* * * ROANOKE RAPIDS* * * * * WRECK NOPI* * * * FI14* * * * *
 HalifaxCoE911@HalifaxNC911.com S: M:HalifaxCoE911:* 93 ROANOKE AVE* * * ROANOKE RAPIDS* * * * * FIRE-SMOKE* * * * FI14* * * * *
 
 Pender County, NC (version 0)
 911-:=12-010417* OLD MAPLE HILL RD N // NC HWY 50* * * * * * * * * 29-TRAFFIC/TRANSPORTATION ACCIDENTS* * * FD13* * * * *
 911-:=12-012441* OLD MAPLE HILL RD N // NC HWY 50* * * * * * * * * BRUSH/FOREST FIRE (RP 1-4)* * * FD13* * * * *
 911-:=12-012596* HOLLINGSWORTH DR // NC HWY 50* * * * * * * * * 29-TRAFFIC/TRANSPORTATION ACCIDENTS* * * FD13* * * * *
 911-:=12-013390* 1520 OLD MAPLE HILL RD* * * * * * * * * 29-TRAFFIC/TRANSPORTATION ACCIDENTS* * * FD13* * * * *
 911-:=12-009752* 5471 NC HWY 50* * * * * * * * * 29-TRAFFIC/TRANSPORTATION ACCIDENTS* * * FD13* * * * *
 911-:=12-009855* 14976 NC HWY 53 EAST* * * * * * * * * ASSIST EMS* * * FD13,FD15* * * * *
 
 Sampson County, NC (version 0)
 EMS:2012-013061* 2280 HOBBTON HWY* * * CLINTON* WEDGEWOOD LN* BUMPY LN* K12* * ABDOMINAL* ABDOMINAL PAIN* CSRS,EMS79* 1328A* Medical: No* Hazards:  
 EMS:2012-013062* 219 W CARTER ST* * * CLINTON* BARDEN ST* BUNTING ST* L11* * BREATH DIFF* BREATHING DIFFICULTIES* CSRS,EMS75* 1379* Medical: No* Hazards: No* 02/2
 EMS:2012-013047* I40 MM 343* * * NEWTON GROVE* MCLAMB RD UNDERPASS* EXIT 343 HOBBTON HWY* D11* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* EMS79* 1263* Medical
 EMS:2012-013001* 91 N CHURCH AVE* * * GARLAND* W THIRD ST* W FOURTH ST* T10* * CHEST PAIN* CHEST PAIN* EMS77* 1376* Medical: No* Hazards: No* *
 EMS:2012-012994* 303 MARTIN LUTHER KING JR BLVD* * * CLINTON* WEEKS ST* SOUTHEAST BLVD* L12* * UNCONSCIOUS* UNCONSCIOUS/FAINTING* CSRS,EMS77* 1380* Medical: No* H
 EMS:2012-012960* 120 SOUTHWOOD DR* 305A* * CLINTON* SOUTH WEST BLVD* * M12* Landmark Comment: UPDATED 01/07* BREATH DIFF* BREATHING DIFFICULTIES* CSRS,EMS75* 1380
 EMS:2012-012906* 229 e MORISEY BLVD* * * CLINTON* LISBON ST* DEVANE ST* L12* * BURNS* BURNS* CSRS,EMS75* 1275* Medical: No* Hazards: No* *
 EMS:2012-012904* 134 W SECOND ST* * * GARLAND* S CHURCH AVE* BROOKS AVE* T10* * SICK CALLS* SICK CALLS* 7112* 1376* Medical: No* Hazards: No* *
 EMS:2012-012890* 2100 REEDSFORD RD* * * CLINTON* MELVABROOK DR* BILLY LN* M13,M14* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* CSRS,EMS76,STA14* 1330* Medical: Comment: TRACT 10 OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *
 EMS:2012-012887* 249 KAYLA LN* * * CLINTON* KING RD* KING RD* I14* Geo Comment: TRACT *10* OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *
 EMS:2012-012877* 233 EFFIE PETERSON LN* * * ROSEBORO* PORTER RD* DEAD END* M9* * STRUCTURE FI* STRUCTURE FIRE* CSRS,EMS76* 1336* Medical: No* Hazards: No* *
 EMS:2012-012839* 4056 REEDSFORD RD* * * CLINTON* HUCKLEBERRY LN* STEVE HARRIS LN* N14,N15* * CHEST PAIN* CHEST PAIN* CSRS,EMS71* 1347A* Medical: No* Hazards: No*
 EMS:2012-012887* 249 KAYLA LN* * * CLINTON* KING RD* KING RD* I14* Geo Comment: TRACT 10 OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *
 EMS:2012-012838* 1498 HAWLEY RD* * OR* DUNN* FRED TEW RD* UNION GROVE CH RD* C4* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* EMS72,PVRS,Z951RT* 1254C* Medical:
 EMS:2012-012833* 253 PINE OAK LN* * * DUNN* MIDWAY ELEM SCH RD* DEAD END* E6* * SICK CALLS* SICK CALLS* EMS78* 1255A* Medical: No* Hazards: No* *
 EMS:2012-012830* 203 N MAIN ST* * * SALEMBURG* CLINTON ST* CHURCH ST* K6* * BREATH DIFF* BREATHING DIFFICULTIES* EMS76* 1378* Medical: No* Hazards: No* 02/26/2012
 EMS:2012-012826* 939 SOUTHWEST BLVD* * TAC3* CLINTON* MARTIN LUTH KING BLV* * L12* * STRUCTURE FI* STRUCTURE FIRE* CSRS,EMS72,STA14,STA16,STA8* 1380* Medical: No*
 EMS:2012-012816* 301 MAIN ST* * * NEWTON GROVE* E CIRCLE ST* N CHURCH ST* C11* Landmark Comment: UPDATED 2-02* BREATH DIFF* BREATHING DIFFICULTIES* EMS71,EMS72,NG
 EMS:2012-017694* 79 ROMAY MCKOY LN* * * ROSE HILL* BILL TOWN RD* LOOPS BACK TO ITSELF* S14* Geo Comment: *24* ABDOMINAL* ABDOMINAL PAIN* EMS77* 1352A* Medical: No
 
 Butler County, OH (version 0)
 BCSO:12-026842* 7897 JESSIES WY* * * * MORRIS RD* TYLERSVILLE RD* * NBH: JESSIE`S LANDING CONDOS* MEDICAL* LIFE SQUAD REQUEST* * * FFTLS* * Line16=Medical: No* Line17=Hazards: No* Line18=05/18/2012 21:06:12 : pos1 : RLONEILL Cross streets: MORRIS RD//TYLERSVILLE RD NBH: JESSIE`S LANDING CONDOS 80 YO MALE URINATING BLOOD*
 BCSO:12-026840* 4090 MILLIKIN RD* * * * FAIRCREST* LIBERTY FAIRFIELD RD* * * MEDICAL* LIFE SQUAD REQUEST* * * FFTLS* * Line16=Medical: No* Line17=Hazards: No* Line18=05/18/2012 20:47:29 : pos4 : dkrednour Cross streets: FAIRCREST//LIBERTY FAIRFIELD RD 41 YOM,. DIABETIC UNRESPONSIVE*
 BCSO:12-026808* 6926 CHESTNUT OAK CT* * * * * JOCELYN DR* * NBH: ASHWOOD SUBD* MEDICAL* LIFE SQUAD REQUEST* WOLFER, NORBERT* * * * Line16=Medical: No* Line17=Hazards: No* Line18=05/18/2012 17:39:09 : pos5 : PDFRYER REQUESTING SILENT APPROACH 05/18/2012 17:38:51 : pos5 : PDFRYER Cross streets: //JOCELYN DR NBH: ASHWOOD SUBD 90 YO FEMALE RUNNING FEVER, UNABLE TO WALK - DIABETIC*
 BCSO:12-026807* 2580 UTICA AV* * * * CANASTOTA DR* * * NBH: NORMANDY HEIGHTS* MEDICAL* LIFE SQUAD REQUEST* SINGLETON,RAY* * * * Line16=Medical: No* Line17=Hazards: No* Line18=05/18/2012 17:34:43 : pos5 : PDFRYER Cross streets: CANASTOTA DR// NBH: NORMANDY HEIGHTS 75 YO FEMALE - UNABLE TO MOVE ONE LEG, KNEE AND LEG ARE SWOLLEN - NEEDS TRANSPORTED*
 BCSO:12-027009* 7309 CLARION CT* * * * * SARATOGA DR* * Special Comment:: *LOADED WEAPONS INSIDE* MAY2012 NBH: HOWARD`S SUBDIVISION* MEDICAL* LIFE SQUAD REQUEST* * * FFTLS* * Line16=Medical: Yes* Line17=Hazards: Yes* Line18=05/19/2012 18:06:17 : pos3 : RLONEILL Special Comment:: *LOADED WEAPONS INSIDE* MAY2012 Cross streets: //SARATOGA DR NBH: HOWARD`S SUBDIVISION 56 YO FEMALE DIFF BREATHING*
 BCSO:12-026888* 7418 CHATEAUGUAY ST* * * * RIVERDOWNS CT* CITATION DR* * NBH: ASCOT DOWNS* SUICIDE* SUICIDE OR ATTEMPT SUICIDE* * * 21P45,FFTLS* * Line16=Medical: No* Line17=Hazards: No* Line18=05/19/2012 01:40:26 : pos3 : KKMETSKER Cross streets: RIVERDOWNS CT//CITATION DR NBH: ASCOT DOWNS 24 Y.O.F. - HANG IS PREG 4 MO*
 BCSO:12-026932* 2923 HAMILTON MASON RD* * * * * * * * MEDICAL* LIFE SQUAD REQUEST* * * FFTLS* * Line16=Medical: No* Line17=Hazards: No* Line18=05/19/2012 10:33:24 : pos3 : HRMILLER Landmark: WELLINGTON MANOR 60YOM LEFT HIP PAIN .. FRONT DOUBLE DOORS GO TO THE RIGHT*
 BCSO:12-026944* 3219 PRINCETON RD* * * * BYPASS 4* BYPASS 4* * * CRASH INJURY* INJURY ACCIDENT* * * FFTFD,FFTLS* * Line16=Medical: No* Line17=Hazards: No* Line18=05/19/2012 11:43:04 : pos3 : HRMILLER Cross streets: BYPASS 4//BYPASS 4 Landmark: MURPHY GAS STATION ALI X Coordinate: -84.5082092 ALI Y Coordinate: 39.39026713 ALI Uncertainty Factor: 000 ALI Confidence Factor: 28 **Nearest Address: 3213 PRINCETON RD, FAIRFIELD TWP GREEN CAMRY .. 2 CHILDREN IN THE CAR 11 AND 9 .. LIGHT BEIGE MINI VAN LEFT THE SCENE .. NOW PARKED IN THE LOT OF THE GAS STATION*
 BCSO:12-026968* 5753 GREEN CREST DR* * * * CREST MANOR DR* MILLCREST DR* * NBH: GREEN CREST MANOR* MEDICAL* LIFE SQUAD REQUEST* * * FFTLS* * Line16=Medical: No* Line17=Hazards: No* Line18=05/19/2012 13:24:53 : pos1 : TJMILLER Cross streets: CREST MANOR DR//MILLCREST DR NBH: GREEN CREST MANOR 75 YOF UNRESPONSIVE BUT BREATHING - ALZHEIMER PATIENT , HEART HX*
 
 */
 public class DispatchA3Parser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile("\\* +");
   
   private String prefix;
   
   public DispatchA3Parser(int version, String prefix, String defCity, String defState) {
     super(defCity, defState,
            version == 0 ?
              "ID? ADDR/SXP APT CH CITY! X X MAP INFO1 CALL INFO ( UNIT! | NAME UNIT! | NAME PHONE UNIT ) INFO+"
            : version == 1 ?
                "ID? ADDR/SXP APT CH CITY! EMPTY+? CALL CALL ( UNIT! | NAME UNIT! | NAME PHONE UNIT ) INFO+"
            : null);
     this.prefix = prefix;
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!body.startsWith(prefix)) return false;
     body = body.substring(prefix.length()).trim();
     if (body.endsWith("*")) body = body + " ";
     return parseFields(DELIM.split(body), data);
   }
   
   private class BaseAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       field = field.replace("//", "/");
       super.parse(field, data);
       if (data.strPlace.startsWith("/")) data.strPlace = data.strPlace.substring(1).trim();
     }
   }
   
   private class BaseChannelField extends ChannelField {
     @Override
     public void parse(String field, Data data) {
       
       // If nothing entered, see if the apt looks like a channel
       if (field.length() == 0 && data.strApt.startsWith("TAC")) {
         data.strChannel = data.strApt;
         data.strApt = "";
       } else {
         super.parse(field, data);
       }
     }
   }
   
   private class BaseCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       
       // Override previous call field
       if (field.length() > 0) data.strCall = field;
     }
   }
   
   private class BaseInfo1Field extends InfoField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf("NBH:");
       if (pt >= 0) {
         data.strPlace = append(data.strPlace, " - ", field.substring(pt+4).trim());
         field = field.substring(0,pt).trim();
       } 
       super.parse(field, data);
     }
   }
   
   private static final Pattern LINE_PTN = Pattern.compile("Line\\d+=");
   private static final Pattern DATE_TIME_PTN = Pattern.compile("(\\d\\d/\\d\\d/\\d{4}) (\\d\\d:\\d\\d:\\d\\d) : \\w+ : \\w+\\b");
   private class BaseInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (field.toUpperCase().startsWith(data.strCall.toUpperCase())) {
         data.strCall = field;
         return;
       }
       Matcher match = LINE_PTN.matcher(field);
       if (match.find()) {
         field = field.substring(match.end()).trim();
         match = DATE_TIME_PTN.matcher(field);
         if (match.find()) {
           data.strDate = match.group(1);
           data.strTime = match.group(2);
           field = match.replaceAll("");
           
           // Strip redundant place
           int pt = field.indexOf(" NBH:");
           if (pt >= 0) {
             int pt2 = field.indexOf(data.strPlace, pt);
             if (pt2 >= 0) {
               pt2 += data.strPlace.length();
               field = field.substring(0,pt) + field.substring(pt2);
             }
           }
           
           // Strip redundant cross street info
           pt = field.indexOf(" Cross streets:");
           if (pt >= 0) {
             String cross = data.strCross;
             int pt2 = cross.lastIndexOf('&');
             if (pt2 >= 0) cross = cross.substring(pt2+1).trim();
             pt2 = field.lastIndexOf(cross);
             if (pt2 >= pt) {
               pt2 += cross.length();
               while (pt2 < field.length() && field.charAt(pt2)=='/') pt2++;
               field = field.substring(0,pt) + field.substring(pt2);
             }
           }
           field = field.trim();
         }
       }
       
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "INFO DATE TIME";
     }
   }
   
   private class BaseNameField extends NameField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf("*");
       if (pt >= 0) {
         data.strPhone = field.substring(pt+1).trim();
         field = field.substring(0,pt).trim();
       }
       super.parse(field, data);
     }
   }
   
   
   @Override
   public Field getField(String name) {
    if (name.equals("ID")) return new IdField("\\d{2,4}-\\d{6}", true);
     if (name.equals("ADDR")) return new BaseAddressField();
     if (name.equals("CH")) return new BaseChannelField();
     if (name.equals("CALL")) return new BaseCallField();
     if (name.equals("INFO1")) return new BaseInfo1Field();
     if (name.equals("INFO")) return new BaseInfoField();
     if (name.equals("NAME")) return new BaseNameField();
     if (name.equals("UNIT")) return new UnitField("(?:[A-Z0-9]{1,4}[0-9]|CSRS)(?:,(?:[A-Z]{0,3}[0-9]+[A-Z]{0,3}|[A-Z]{1,4}))*");
     return super.getField(name);
   }
 }
