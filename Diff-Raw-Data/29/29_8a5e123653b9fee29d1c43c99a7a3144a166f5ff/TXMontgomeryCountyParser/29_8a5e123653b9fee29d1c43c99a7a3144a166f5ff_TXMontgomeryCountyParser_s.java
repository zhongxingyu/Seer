 package net.anei.cadpage.parsers.TX;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Montgomery County, TX
 Contact: Brian Mulligan <fireguyl19@gmail.com>, Brian Mulligan <bmulligan@avfd.com>
 Sender: firecad@thewoodlandstownship-tx.gov
 System: Firecom
 
 [Fire CAD Message]  New Fire Run: 2010-63654,Nature: 12D02E-SEIZURE - Continuous or Multiple seizures,MR54,Location: 12246 TRAIL HOLLOW DR,Buildi
 [Fire CAD Message]  New Fire Run: 2010-63564,Nature: 19D04-HEART PROBLEMS - Clammy,MR51,Location: HIGHWAY 105 W & S FM 1486,Building: ,Cross: ,,G
 [Fire CAD Message]  New Fire Run: 2010-63733,Nature: F15-ILLEGAL BURN,E55;,Location: 14321 FOREST CIR E-MO,Building: ,Cross: 14658 FOREST CIR-MO,
 [Fire CAD Message]  New Fire Run: 2010-63709,Nature: F25-RESIDENTIAL FIRE,E51;,Location: 159 S PINE DR-MO,Building: ,Cross: 132 N PINE DR-MO,132
 [Fire CAD Message]  New Fire Run: 2010-63739,Nature: F15-ILLEGAL BURN,E51;,Location: PINE LN-MO/HICKORY DR-MO,Building: ,Cross: 19902 PINE LN-MO,
 [Fire CAD Message]  New Fire Run: 2010-63762,Nature: 10C04-CHEST PAIN - Breathing Normally => 35,MR51,Location: 24495 REED RD,Building: ,Cross: ,
 [Fire CAD Message]  New Fire Run: 2010-63789,Nature: 17D03-FALL - Not alert,MR51,Location: 1953 DR MARTIN LUTHER KING JR DR,Building: ,Cross: ,,G
 [Fire CAD Message]  New Fire Run: 2010-63800,Nature: 28C01G-STROKE/CVA - Not alert - Greater than one hour onset,MR55,Location: 1 FAIRFIELD DR,Bu
[Fire CAD Message]  2011-64534,CLOSED CALL: DISP 18:06:34,AVL 18:24:17,.
[Fire CAD Message]  Run# 2010-63824 Trk E51,E54,E55,E31,T51,T31,TN51,FM1,BAT5,TN31 TAC NORTH (01900149-940),.
 
 Contact: johnny jessie <johann475@gmail.com>
 (Nature: 13D01-DIABETIC PROBLEMS - Unconscious) New Fire Run: 2011-76911,,MR18,Location: 35219 W PINE HILL,Building: ,Cross: ,,Grid: 247F,Map: 4
 
 Contact: "Scott Collins" <policefiremedic@gmail.com>
 New Fire Run: 2011-75288,Nature: 28C01G-STROKE/CVA - Not alert - Greater than one hour onset,MR18,Location: 158 CHARLIE,Building: ,Cross: ,,Grid: 212K,Map: 4476,.
 
 Contact: Mike and April Covington <madwzc@gmail.com>
 (Nature: 06D02-BREATHING PROBLEMS -) New Fire Run: 2011-79434,,MR51,Location: 21760 CHAPPEL WAY,Building: ,Cross: ,,Grid: 69A,Map: 4291,.
 
 Contact: Stephen Rorai <stephenrorai@yahoo.com>
 (Nature: F25-RESIDENTIAL FIRE) New Fire Run: 2011-86558,,E82;,Location: 12767 COON MASSEY RD-CR,Building: ,Cross: 15344 FM 3083-CR,127,Grid: 190
 
 Contact:Curtis Pratt <cprattco@gmail.com>
 Sender:  firecad@thewoodlandstownship-tx.gov
 (Nature: 29-MVA - PRE-ALERT) New Fire Run: 2011-96042,,E112,Location: RAYFORD RD-SC/FOX RUN BLVD-SC,Building: ,Cross: 3050 RAYFORD RD-SC,,Grid:
 (Nature: 12B01-SEIZURE - Effective breathing NOT) New Fire Run: 2011-95939,,B113,Location: 25808 I45 N-SC,Building: ,Cross: 102 OAKWOOD DR-SC,BR
 (Nature: F48-CODE 1000 DRILL ONLY) New Fire Run: 2011-95833,,E113,Location: 27430 ROBINSON RD-OR,Building: ST 113 S MONT CO FIRE,Cross: 27254 BL
 (Nature: F03-AUTOMATIC ALARM) New Fire Run: 2011-96053,,E112,Location: 1805 RILEY FUZZEL RD-SC,Building: ,Cross: 28500 SPRING TRAILS,Grid: 293E,
 (Nature: F08-CONTROLLED BURN) New Fire Run: 2011-96055,,B112,Location: 1638 JULIA PARK DR-SC,Building: ,Cross: 27918 TESSIE HILLS L,Grid: 293K,M
 (Nature: F08-CONTROLLED BURN) New Fire Run: 2011-96061,,B112,Location: 2121 RILEY FUZZEL RD-SC,Building: ,Cross: 28702 LODDINGTON ST-,Grid: 293E
 (Nature: 10C01-CHEST PAIN - Abnormal Breathing) New Fire Run: 2011-96069,,B113,Location: 319 PINE MANOR DR-OR,Building: ,Cross: 27072 N HARLAN L
 (Nature: 28C01U-STROKE/CVA - Not alert - Unknown) New Fire Run: 2011-96082,,T112,Location: 2115 OLD OX RD-SC,Building: ,Cross: 29502 TIMBER TRL-
 (Nature: 29-MVA - PRE-ALERT) New Fire Run: 2011-96081,,L111,Location: INTERSTATE 45 N & RAYFORD RD,Building: ,Cross: ,,Grid: 252W,Map: 5173,.
 (Nature: 32D01-UNKNOWN PROBLEM/MAN DOWN - Life St) New Fire Run: 2011-96095,,B111,Location: INTERSTATE 45 N & RAYFORD RD,Building: ,Cross: ,,Gri
 (Nature: 28C01L-STROKE/CVA - Not alert - Less tha) New Fire Run: 2011-96102,,B113,Location: 25610 OAKHURST DR-SC,Building: GRACE MEMORY CARE II,
 (Nature: F44-SMALL OUTSIDE FIRE) New Fire Run: 2011-96146,,E112,Location: 29903 S LEGENDS VILLAGE CIR-SC,Building: ,Cross: 29820 LEGENDS PASS L,
 (Nature: 06C01-BREATHING PROBLEMS - Abnormal Brea) New Fire Run: 2011-96151,,B111,Location: I45 N-SC/RAYFORD RD-SC,Building: ,Cross: 25130 I45 N
 (Nature: 06D02-BREATHING PROBLEMS - DIFICULTY SPE) New Fire Run: 2011-96229,,B113,Location: 258 SPRING PINES DR-SC,Building: ,Cross: 26202 MAPLE
 (Nature: 10D02-CHEST PAIN - Difficulty speaking b) New Fire Run: 2011-96261,,B111,Location: 25469 BOROUGH PARK DR-SC,617,Building: MISSION WOODS
 (Nature: F08-CONTROLLED BURN) New Fire Run: 2012-03389,,B113,Location: 2226 BOB WHITE-SC,Building: ,Cross: ,1702 COUGAR CREEK-C,Grid: 253E,Map:
 (Nature: F36-SMOKE IN THE BUILDING) New Fire Run: 2012-03786,,TK10,Location: 10510 SIX PINES DR-WD,2211,Building: TAMARAC PINES APTS,Cross: 1800
 (Nature: F18-LIVE WIRES DOWN) New Fire Run: 2012-04542,,E111,Location: MM 73,Building: SPRING CREEK BRIDGE M,Cross: ,,Grid: 292B,Map: ,.
 (Nature: 29B04U-MVA - Unknown status/Other codes) New Fire Run: 2012-08140,,L111,Location: IH45 N & RAYFORD RD,Building: ,Cross: ,,Grid: 252W,Ma
 
 Contact: support@active911.com
 Sender:Fire Dispatch <firecad@thewoodlandstownship-tx.gov>
 (Nature: 02C01-ALLERGIC REACTION - Difficulty bre) New Fire Run: 2012-13412,,E112,Location: 29048 SAN BERNARD RIVER LOOP,Building: ,Cross: ,,Grid: 253Y,Map: 5373,.
 (Nature: 21B02-HEMORRHAGE/LACERATIONS - Serious H) New Fire Run: 2012-13430,,B113,Location: 26014 LEAFYWOOD DR,Building: ,Cross: ,,Grid: 252P,Map: 5173,.
 (Nature: 29-MVA - PRE-ALERT) New Fire Run: 2012-13431,,L111,Location: 25602 INTERSTATE 45 N,Building: ,Cross: ,,Grid: 252S,Map: 5173,.
 (Nature: F04A-AUTOMATIC ALARM PULL) New Fire Run: 2012-13376,,E111,Location: 24717 OAKHURST DR-SC,Building: KINDERCARE LEARNING C,Cross: 25078 I45 N-SC,,Grid: 252W,Map: 5172,.
 (Nature: 21D02-HEMORRHAGE/LACERATION - Not Alert) New Fire Run: 2012-13401,,B113,Location: 25807 WOODGLEN DR,Building: ,Cross: ,,Grid: 252T,Map: 5173,.
 (Nature: F06-CHILD LOCKED IN A VEHICLE) New Fire Run: 2012-13384,,E113,Location: I45 N-OR/ROBINSON RD-OR,Building: ,Cross: 27000 I45 N-OR,,Grid: 252J,Map: 5174,.
 (Nature: 10C04-CHEST PAIN - Breathing Normally =>) New Fire Run: 2012-13343,,E114,Location: 1619 EASTVALE DR,Building: ,Cross: ,,Grid: 252U,Map: 5273,.
 (Nature: 17B01G-FALL on the Ground or Floor - Pos) New Fire Run: 2012-13370,,B113,Location: 26523 HILLSIDE DR,Building: ,Cross: ,,Grid: 252J,Map: 5173,.
 (Nature: F18-LIVE WIRES DOWN) New Fire Run: 2012-13449,,E114,Location: 2359 HICKORY HOLLOW LN-SC,Building: ,Cross: 29702 WILD ROSE DR-S,Grid: 252Z,Map: 5272,.
 (Nature: F16-VEHICLE EXTRICATION) New Fire Run: 2012-13333,,E111,Location: 25186 I45 N-SC,Building: ,Cross: 102 RAYFORD FOREST L,Grid: 252S,Map: 5173,.
 (Nature: 02C01-ALLERGIC REACTION - Difficulty bre) New Fire Run: 2012-13412,,E112,Location: 29048 SAN BERNARD RIVER LOOP,Building: ,Cross: ,,Grid: 253Y,Map: 5373,.
 (Nature: 21B02-HEMORRHAGE/LACERATIONS - Serious H) New Fire Run: 2012-13430,,B113,Location: 26014 LEAFYWOOD DR,Building: ,Cross: ,,Grid: 252P,Map: 5173,.
 (Nature: 29-MVA - PRE-ALERT) New Fire Run: 2012-13431,,L111,Location: 25602 INTERSTATE 45 N,Building: ,Cross: ,,Grid: 252S,Map: 5173,.
 (Nature: F04A-AUTOMATIC ALARM PULL) New Fire Run: 2012-13376,,E111,Location: 24717 OAKHURST DR-SC,Building: KINDERCARE LEARNING C,Cross: 25078 I45 N-SC,,Grid: 252W,Map: 5172,.
 (Nature: 21D02-HEMORRHAGE/LACERATION - Not Alert) New Fire Run: 2012-13401,,B113,Location: 25807 WOODGLEN DR,Building: ,Cross: ,,Grid: 252T,Map: 5173,.
 (Nature: F06-CHILD LOCKED IN A VEHICLE) New Fire Run: 2012-13384,,E113,Location: I45 N-OR/ROBINSON RD-OR,Building: ,Cross: 27000 I45 N-OR,,Grid: 252J,Map: 5174,.
 (Nature: F04A-AUTOMATIC ALARM PULL) New Fire Run: 2012-13476,,E113,Location: 327 TALLOW DR-CR,Building: CHATEAU WOODLANDS FAC,Cross: 10502 SONGWOOD-CR,10,Grid: 252F,Map: 5274,.
 (Nature: 10C04-CHEST PAIN - Breathing Normally =>) New Fire Run: 2012-13343,,E114,Location: 1619 EASTVALE DR,Building: ,Cross: ,,Grid: 252U,Map: 5273,.
 (Nature: 17B01G-FALL on the Ground or Floor - Pos) New Fire Run: 2012-13370,,B113,Location: 26523 HILLSIDE DR,Building: ,Cross: ,,Grid: 252J,Map: 5173,.
 (Nature: F18-LIVE WIRES DOWN) New Fire Run: 2012-13449,,E114,Location: 2359 HICKORY HOLLOW LN-SC,Building: ,Cross: 29702 WILD ROSE DR-S,Grid: 252Z,Map: 5272,.
 (Nature: 30D02-TRAUMATIC INJURIES - Not alert) New Fire Run: 2012-13472,,B111,Location: 25202 OAKHURST DR-SC,Building: ,Cross: 400 RAYFORD FOREST L,Grid: 252T,Map: 5173,.
 (Nature: F16-VEHICLE EXTRICATION) New Fire Run: 2012-13333,,E111,Location: 25186 I45 N-SC,Building: ,Cross: 102 RAYFORD FOREST L,Grid: 252S,Map: 5173,.
 (Nature: 29-MVA - PRE-ALERT) New Fire Run: 2012-13492,,E111,Location: BUDDE RD & PRUITT RD,Building: ,Cross: ,,Grid: 252W,Map: 5172,.
 


 [Nature: 32D01-UNKNOWN PROBLEM/MAN DOWN - Life St] New Fire Run: 2012-16406,,B111,Location: INTERSTATE 45 N & RAYFORD RD,Building: ,Cross: ,,Grid: 252W,Map: 5173,.\n
 [Fire CAD Message] Run# 2012-16406 Trk B111,TN111 FD3 (01900153-937),.\n
 [Fire CAD Message] 2012-16406,CLOSED CALL: DISP 02:33:31,AVL 02:36:49,.\n
 
 ** NOT PARSING ***
 Contact: Bill Holt <billholt1960@gmail.com>
 Sender: firecad@thewoodlandstownship-tx.gov
 Fire CAD Message / Run# 2012-05791 Trk E105 FD2>> (01900145-937),.\n
 
  */
 
 public class TXMontgomeryCountyParser extends FieldProgramParser {
   
   public TXMontgomeryCountyParser() {
     super(CITY_CODES, "MONTGOMERY COUNTY", "TX",
            "New_Fire_Run:ID! Nature:CALL? UNIT! Location:ADDR/y! Building:PLACE Cross:X/c");
   }
   
   @Override
   public String getFilter() {
     return "firecad@thewoodlandstownship-tx.gov";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (subject.equals("Fire CAD Message")) {
      data.strCall = "RUN REPORT";
      data.strPlace = body;
      return true;
    }
    
     // Sometime Nature field gets pulled out of text string and put in subject
     // When that happens we need to put it back
     if (subject.startsWith("Nature:")) {
       int pt = body.indexOf("New Fire Run:");
       if (pt < 0) return false;
       pt = body.indexOf(',', pt+13);
       if (pt < 0) return false;
       body = body.substring(0,pt+1) + subject + body.substring(pt+1);
     }
    return parseFields(body.split(","), data);
   }
   
   // Unit field, remove extraneous trailing semicolon
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(";")) field = field.substring(0,field.length()-1).trim();
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       field = field.replace("FUZZEL RD", "FUZZELL RD");
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CR",   "CONROE",
       "MO",   "MONTGOMERY",
       "NC",   "NEW CANEY",
       "OR",   "OAK RIDGE",
       "PG",   "PATTON VILLAGE",
       "RF",   "ROMAN FOREST",
       "SC",   "SPRING",
       "SP",   "SPLENDORA",
       "WD",   "WOODLANDS"
   });
 }
