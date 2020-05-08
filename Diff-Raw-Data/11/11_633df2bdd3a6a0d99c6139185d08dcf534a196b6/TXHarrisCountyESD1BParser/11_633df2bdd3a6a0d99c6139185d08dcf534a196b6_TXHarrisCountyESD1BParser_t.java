 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Harris County ESD #1, TX
 Contact: Nathan Mathews <nbmathews807@gmail.com>
 Contact: Richy Hancock <richy@huffmanfire.org>
 Contact: Ennio Ponte <EPonte@channelviewfire.com>
 Sender: cadnoreply@proxy.hcec.com
 Sender: 930010nn
 ID#:11-06-20685 - - TRASH FIRE - 111 Bayou Dr - Apt:C - Bldg: - Key Map: 498D - Cross Streets:EAST FWY/MARKET - Box #:2002
 ID#:11-06-20710 - - FIRE - PreAlert - 16217 Ridlon - Apt: - Bldg: intra SERVICES - Key Map: 498C - Cross Streets:SHEILA LN/WOODLAND D
 ID#:11-06-20658 - - 29A1 MOTOR VEHICLE A - East Fwy / Delldale - Apt: - Bldg: - Key Map: 498E - Cross Streets: - Box #:2004
 ID#:11-06-20627 - - 29A1 MOTOR VEHICLE A - 16410 Ave D - Apt:1 - Bldg: AVE D APTS - Key Map: 498D - Cross Streets:WOODLAND DR/CEDAR L
 
 ID#:11-07-25331 - - ALARMS - 1518 Great Dover Cir - Apt: - Bldg: - Key Map: 458W
 ID#:11-07-25341 - 06D02 - 6D1 RESPIRATORY - 11870 Greenloch Ln - Apt: - Bldg: - Key Map: 416K - Cross Streets:GREENCANYON DR/GREENROC 
 ID#:11-07-25342 - 25A01 - PSYCHIATRIC - 15215 S Brentwood Dr - Apt: - Bldg: - Key Map: 498E - Cross Streets:Dead End/DELLDALE - Box #:2004
 ID#:11-07-25378 - - Stroke (CVA) - 11715 Greenglen Dr - Apt: - Bldg: - Key Map: 416K - Cross Streets:JOHN RALSTON RD/GREENROCK LN - B 
 ID#:11-07-25416 - - HEMORRHAGE/LACERATIO - 12370 S San Circle Dr - Apt: - Bldg: UNK TRAILER PARK - Key Map: 456C - Cross Streets:BARK 
 ID#:11-08-25733 - 55B04 - ELECTRICAL HAZARD - 16102 E Ih 10 - Apt: - Bldg: OLD RIVER CHURCH DAY - Key Map: 498G - Cross Streets:IH 10 EAST BAYOU RAMP/IH 10 SH -
 ID#:11-08-25969 - 29D02m - 29D5 MOTOR VEHICLE A - 539 S Sheldon Rd - Apt: - Bldg: - Key Map: 498K - Cross Streets:JACINTO PORT BLVD/MARKET - Box #:2001
 ID#:11-08-25936 - 29B04 - Traffic/Trans Incide - E BW 8 N / WOODFOREST BW 8 NB - Apt: - Bldg: - Key Map: 457Y - Cross Streets: - Box #:2005
 ID#:11-08-26006 - - Citizen Assist - 5500 E Bw 8 N - Apt: - Bldg: big tex storage - Key Map: 457Y - Cross Streets:BW 8 NB WALLISVILLE EXIT RAMP/ - Box #:2005
 ID#:11-08-28674 - Units:M151 - 32B01 - 32B1 UNKNOWN PROBLEM - 606 Commons Vista Dr - Apt: - Bldg: - Key Map: 298Q - Cross Streets:COM
 ID#:11-08-29430 - - FALLS - 17015 Faring Rd - Apt: - Bldg: - Key Map: 418X - Cro
 ID#:11-08-29436 - - ALARMS - 1714 Hidden Terrace Dr - Apt: - Bldg: - Key Map: 458S - Cross Streets:HIDDEN CREST DR/HIDDEN MANOR D - Box #:2005
 ID#:11-08-26039 - - FUEL SPILL - 15880 Wallisville Rd - Apt: - Bldg: LEGEND OAKS - EAST - Key Map: 457V - Cross Streets:HIDDEN PARK DR/HYACINTH PATH W - Box #:2005
 ID#:11-08-26105 - 21B01 - HEMORRHAGE/LACERATIO - 11803 Greenmesa Dr - Apt: - Bld
 ID#:11-08-26080 - 13D01 - DIABETIC PROBLEMS - 10710 Green River Dr - Apt: - Bldg
 ID#:11-08-26159 - - Stab / GSW (B) - 919 Holbech - Apt: - Bldg: - Key Map: 497D
 ID#:11-08-26160 - - HEART PROBLEMS - 9402 Abbotshall Ln - Apt: - Bldg: - Key Map: 456D - Cross Streets:ARBOR FIELD LN/STONEFIELD MANO - Box #:3304
 
  1 of 2\nFRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170 - 67D02 - 67D2 LG GRASS AND WO - US 90 / SHELDON US 90 RAMP - Apt: - Bldg: - Key Map:\n(Con't) 2 of 2\n458C - Cross Streets: - Box #:3304(End)
 
 ID#:11-08-29410 - Address changed to:uvalde/kinsman - Cross Streets: - Key Map:NOT F
 ID#:11-08-26067 - Address changed to:620 SHELDON RD - Cross Streets:AVE C/RIDLON - Key Map:498B
 ID#:11-08-26080 - Address changed to:10710 Greens Crossing Blvd - Cross Streets
 
 ID#:11-08-29436 - UNIT:L32 - DISP: 21:06:25 - ER: - OUT: - TO: - AT: - AIR: 21:0
 FRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170- UNIT:E13 - DISP: 11:19:11 - ER: - OUT: - AIR: 11:23:42
 FRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170- UNIT:BRU23 - DISP: 10:57:35 - ER: - OUT: - AIR: 11:23:56
 
Contact: Active911
[] ID#:12-06-23555 - - 60B1 GAS LEAK/ODOR ( - 11000 E Canal Rd - Apt: - Bldg: - Key Map: 460J - Cross Streets:OAK AVE/HOLLY DR - Box #:1705\n
[]  ID#:12-06-23555 - Address changed to:1100 E CANAL RD - Cross Streets:OAK AVE/HOLLY DR - Key Map:460J\n
[]  ID#:12-06-23588 - Address changed to:ORLEANS ST / COTTONTAIL DR - Cross Streets: - Key Map:420W\n
[]  ID#:12-06-23588 - Address changed to:630 COTTONTAIL DR - Cross Streets:JOAN OF ARC ST/ORLEANS ST - Key Map:419Z\n

*** Not parsing 
[]  - - 32B1 UNKNOWN PROBLEM - 123 Test Street - Apt: - Bldg: TESTING AP - Key Map:496K - Cross Streets:Dead End/Dead End - Box #:4104\n

 */
 
 public class TXHarrisCountyESD1BParser extends FieldProgramParser {
   
   private static final Pattern REPORT_PTN = Pattern.compile("^ID#:[^ ]+ *- *UNIT:[^ ]+ *- *DISP:");
   private static final Pattern DELIM = Pattern.compile(" *(?: -)+ *| *(?:- )+ *");
 
   public TXHarrisCountyESD1BParser() {
     super("HARRIS COUNTY", "TX",
            "ID#:ID! ( ADDRCH! Cross_Streets:X Key_Map:MAP " + 
            "| UNIT? CODE? CALL! PREALERT? ADDR! Apt:APT Bldg:APT Key_Map:MAP% Cross_Streets:X Box_#:BOX )");
   }
   
   @Override
   public String getFilter() {
     return "cadnoreply@proxy.hcec.com,93001";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (REPORT_PTN.matcher(body).find()) {
       data.strCall = "RUN REPORT";
       data.strPlace = body;
       return true;
     }
     return parseFields(DELIM.split(body), data);
   }
   
   @Override
   public String getProgram() {
     return super.getProgram() + " PLACE";
   }
   
   // Address change field 
   private class AddressChangeField extends AddressField {
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("Address changed to:")) return false;
      data.strCall = "Address changed to:";
       field = field.substring(19).trim();
       super.parse(field, data);
       return true;
     }
   }
   
   // Unit field only exists if it is tagged
   private class MyUnitField extends UnitField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("Units:")) return false;
       super.parse(field.substring(6).trim(), data);
       return true;
     }
   }
   
   // Code field has to fit specific pattern
   private class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(Pattern.compile("\\d\\d[A-Z]\\d\\d[A-Za-z]?"));
     }
   }
   
   // PreAlert is a special call extender field
   private class PreAlertField extends CallField {
     
     public PreAlertField() {
       setPattern(Pattern.compile("PreAlert"));
     }
     
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   // There are two fields, APT and BLD mapped to the apartment field.  But they
   // occur in reverse order, if both are specified, we want to to put bldg first.
   private class MyAptField extends AptField {
     @Override
     public void parse(String field, Data data) {
       data.strApt = append(field, "-", data.strApt);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDRCH"))  return new AddressChangeField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("CODE")) return new MyCodeField();
     if (name.equals("PREALERT")) return new PreAlertField();
     if (name.equals("APT")) return new MyAptField();
     return super.getField(name);
   }
   
 }
