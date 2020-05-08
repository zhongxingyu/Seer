 package net.anei.cadpage.parsers.CT;
 
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Waterford Town, CT
 Contact: Jason Finnigan <finni94tac@gmail.com>
 Dispatched from waterford communications center
 Sender: wecc@waterfordct.org
 
 2012-009336*\n16 SCENIC VIEW DRIVE*\n*\n*\nWATERFORD*\n*\nEMS BLS*\nAMBULANCE CALL - BLS*\n*\n*\nCO4*\n*\n*\n*\n*
 2012-009419*\nINTERSTATE 395 (X77-MONT)*\n*\n*\nWATERFORD*\n*\nMVA HIGH*\nVEHICLE ACCIDENT - HIGHWAY/SPEED*\n*\n*\n49,CO4,CO5,MED11,W500,W51,W57*\n*\n*\n*\n*
 2012-009444*\n14 KINGFISHER WAY*\n*\n*\nWATERFORD*\n*\nCARB MONOX*\nCARBON MONOXIDE INCIDENT*\nNOLDA,ZACH*\n*\nCO1,CO4,W100,W11,W400*\n*\n*\n*\n*
 2012-009566*\n16 CENTER STREET*\nB*\n*\nWATERFORD*\n*\nEMS ALS*\nAMBULANCE CALL - ALS*\nDE*SANTO,ROBERT S*\n*\nCO4*\n*\n*\n*\n*
 2012-009600*\n45 NORWICH ROAD*\n*\n*\nQUAKER HILL*\n*\nSTRUCTURE*\nSTRUCTURE/BUILDING FIRE*\nCUMBERLAND FARMS*\n*\nCO2,CO4,CO5,W200,W21,W25,W51,W94*\n*\n*\n*\n*
 2012-009697*\n277 NIANTIC RIVER ROAD*\n*\n*\nWATERFORD*\n*\nEMS ALS*\nAMBULANCE CALL - ALS*\nBARRY,THOMAS R*\n*\nCO4*\n*\n*\n*\n*
 2012-009759*\n14 HIGH RIDGE DRIVE*\n*\n*\nWATERFORD*\n*\nEMS BLS*\nAMBULANCE CALL - BLS*\nRISLEY,R A*\n*\nCO1,CO4,W100,W400*\n*\n*\n*\n*
 2012-009851*\n745 VAUXHALL STREET EXTENSION*\n*\n*\nWATERFORD*\n*\nMVA WITH*\nVEHICLE ACCIDENT WITH INJURIES*\nDanielle Chaude*\n*\nCO4,CO5,MED11,W200,W500*\n*\n*\n*\n*
 2012-009905*\n563 BOSTON POST ROAD*\n14*\n2*\nWATERFORD*\n*\nEMS ALS*\nAMBULANCE CALL - ALS*\nBlue Anchor*\n*\nCO4*\n*\n*\n*\n*
 2012-009851*\n745 VAUXHALL STREET EXTENSION*\n*\n*\nWATERFORD*\n*\nMVA WITH*\nVEHICLE ACCIDENT WITH INJURIES*\nDanielle Chaude*\n*\nCO4,CO5,MED11,W200,W500*\n*\n*\n*\n*
 
 */
 
 public class CTWaterfordTownParser extends FieldProgramParser {
   
   public CTWaterfordTownParser() {
     super("WATERFORD TWP", "CT",
           "ID ADDR APT APT CITY INFO SKIP CALL PLACENAME PHONE UNIT! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "wecc@waterfordct.org";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.endsWith("*")) body = body + '\n';
    return parseFields(body.split("\\*\n"), 11, data);
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new IdField("\\d{4}-\\d{6}", true);
     return super.getField(name);
   }
 }
