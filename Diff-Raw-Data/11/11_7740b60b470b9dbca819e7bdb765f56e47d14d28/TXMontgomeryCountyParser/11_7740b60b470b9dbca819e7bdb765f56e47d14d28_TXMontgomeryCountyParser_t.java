 package net.anei.cadpage.parsers.TX;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
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
 
  */
 
 public class TXMontgomeryCountyParser extends FieldProgramParser {
   
   private static final String DEF_STATE = "TX";
   private static final String DEF_CITY = "MONTGOMERY COUNTY";
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "MO",   "MONTGOMERY"
   });
   
   public TXMontgomeryCountyParser() {
     super(CITY_CODES, DEF_CITY, DEF_STATE,
           "New_Fire_Run:ID! Nature:CALL UNIT! Location:ADDR/y! Building:APT Cross:X/c");
   }
   
   @Override
   public String getFilter() {
     return "firecad@thewoodlandstownship-tx.gov";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
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
   
   @Override
   public Field getField(String name) {
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
 }
