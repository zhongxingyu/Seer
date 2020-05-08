 package net.anei.cadpage.parsers.OR;
 
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Linn County, OR
 Contact: Zachary Lincoln <vtzach88@gmail.com>
 Sender: linn911@le.linn.or.us
 
 OTH STRCT FIRE/40560 MCQUEEN DR Linn County/HWY 228 &/6654B23*21/ E23 PT23 STN23 SF M21 A201 E21 E25 STN21 TONE WT21 1ST E22 STN22 WT22 STN24
 UNCONC/FAINTING/ 300 W HOLLEY RD Sweet Home STONEBROOK APARTMENTS/ 61602221/ F
 BREATH PROB/ 5050 MOUNTAIN FIR ST Sweet Home WILEY CREEK COMMUNITY/ 59662221/
 SICK PERSON/ 1048 26TH CT Sweet Home/ 60632221/
 SICK PERSON/ 24490 WEST BRUSH CREEK RD Linn County/ 705023*21/
 SICK PERSON/ 45265 SUNNYSIDE RD Linn County/ 547021*21/
 TRAUMA INJ/ 43813 WILEY CREEK DR Linn County/ 587022*21/
 STROKE/CVA/ 43786 WILEY CREEK DR Linn County/ 587022*21/
 SICK PERSON/ 1177 1ST AV Sweet Home/ 60602221/
 UNK PROB (man down)/ 1228 4TH AV Sweet Home/ 60602221/
 O D/INGESTION/POISONING/ 1131 29TH AV #C2 Sweet Home/ 60632221/
 DIABETIC PROB/ 950 NANDINA ST Sweet Home TWIN OAKS CARE CENTER/ 60612221/
 BREATH PROB/ 43156 NORTH RIVER DR Linn County/ 586622*21/
 BREATH PROB/ 38110 HWY 228 Linn County/ 6649A23*21/
 ALARM-FIRE/ 1400 18TH AV Sweet Home LINMAR INDUSTRIES LLC/ 60612221/
 ALARM-FIRE/1400 18TH AV Sweet Home LINMAR INDUSTRIES LLC/HWY 20 & TAMARACK ST/60612221/ E21 STN21/Overhead door ~ water flow ~ zone 3 ~ riser 1
 SICK PERSON/ 1099 LONG ST Sweet Home SWEET HOME FIRE DEPARTMENT/ 60612221/
 CHEST PAIN/ 1530 TAMARACK ST #62 Sweet Home MT SHADOW TRAILER PARK/ 60612221/
 CARDIAC/RESP ARREST/ 930 POPLAR ST Sweet Home/ 60602221/
 CARDIAC/RESP ARREST/930 POPLAR ST Sweet Home/9TH AV & 11TH AV/60602221/ E21 RESCUE M21/neighbor came over and found her
 EYE PROB/INJURIES/ 1530 TAMARACK ST #218 Sweet Home MT SHADOW TRAILER PARK/ 60612221/
 TRAUMA INJ/ 3137 SANTIAM HWY SP 11 Sweet Home TRAILER VILLA/ 60632221/
 
 Contact: Zach Akin <brfdfireman@gmail.com>
 Sender: 93001NNN
 ICOM/400 notification,HOUSE FIRE/ 1173 W SHERMAN ST Lebanon/ 4142A3131/
 
 Contact: Active911.com
 [ICOM/400 notification] TEST 1st ALARM FIRE/ 1115 SE JACKSON ST Albany LINN CO SHERIFFS OFFICE/JAIL/ 282511S11/\n
 [ICOM/400 notification] OTH STRCT FIRE/ 36149 RIVERSIDE DR INTERSECTN Linn County  /  OAKVILLE RD/ 30181111/\n
 [ICOM/400 notification] ALARM-FIRE/ 618 SW WASHINGTON ST Albany/ 282411N11/\n
 [ICOM/400 notification] ALARM-FIRE/ 525 SW 25TH AV Albany OREGON FREEZE DRY PLANT #2/ 30241212/\n
 [ICOM/400 notification] ALARM-FIRE/ 1929 SE GRAND PRAIRIE RD Albany VILLAS AT COURTYARD/ 30261212/\n
 [ICOM/400 notification] TRAF COLLISION/ 3700 NE KNOX BUTTE RD Linn County LINN CO FAIRGROUNDS AND EXPO/ 26291313/\n
 [ICOM/400 notification] CARDIAC/RESP ARREST/ 2801 SW UMATILLA ST Albany/ 30231212/\n
[ICOM/400 notification] A501 Recall Alarm/ TRAF COLLISION/25010 S I5 Linn County MP212 S I5//vehicle was traveling south, put his blinker on to the\n
 
 */
 
 public class ORLinnCountyParser extends FieldProgramParser {
   
   public ORLinnCountyParser() {
     super(CITY_LIST, "LINN COUNTY", "OR",
           "CALL CALL!+? ADDR/SXP! X/Z? MAP! UNIT? INFO");
   }
   
   @Override
   public String getFilter() {
     return "linn911@le.linn.or.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (body.startsWith("ICOM/400 notification,")) body = body.substring(22).trim();
     String[] flds = body.split("/");
     if (flds.length < 3) return false;
     if (!parseFields(flds, data)) return false;
     if (data.strAddress.endsWith(" INTERSECTN")) {
       data.strAddress = data.strAddress.substring(0,data.strAddress.length()-11).trim();
     }
     if (data.strCity.equalsIgnoreCase("LINN COUNTY")) data.strCity = "";
     return true;
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, "/", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
    if (name.equals("MAP")) return new MapField("(?:\\d{4}[A-Z]?\\d\\d\\*?[A-Z]?\\d\\d|)", true);
    if (name.equals("UNIT")) return new UnitField("[A-Z0-9 ]+");
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "LINN COUNTY",
     "ALBANY",
     "BROWNSVILLE",
     "GATES",
     "HALSEY",
     "HARRISBURG",
     "IDANHA",
     "LEBANON",
     "LYONS",
     "MILL CITY",
     "MILLERSBURG",
     "SCIO",
     "SODAVILLE",
     "SWEET HOME",
     "TANGENT",
     "WATERLOO",
     "CASCADIA",
     "CRABTREE",
     "CRAWFORDSVILLE",
     "MARION FORKS",
     "SHEDD",
     "SOUTH LEBANON"
   };
 }
