 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchA4Parser;
 
 /*  
 Mifflin County, PA
 Contact: Eric Moist <emoist@ymail.com>
 Sender: cmessages@co.mifflin.pa.us and cadmessages@co.mifflin.pa.us 
 
 (CAD Page for CFS 111710-43) ALS - Uncon/Syncope\n1 DERRY PARK DR\nApt:\nBURNHAM\nCross Streets : 400-412  S LOGAN BLVD
 (CAD Page for CFS 111710-42) URG - Ment/Emot/Psych\n41 INDUSTRIAL PARK RD\nApt: A\nGRANVILLE\nCross Streets : 708-924  LOOP RD * 2120-2456 MIDDLE RD
 (CAD Page for CFS 111710-37) ALS - Breathing Difficulty\n27 ANDERSON ST\nApt:\nBRATTON\nCross Streets : 1-19  MATTAWANA BLVD
 (CAD Page for CFS 111710-2) ALS - Motor Vehicle Accident\nROSEWOOD AVE and US HIGHWAY 522 NORTH\nApt: //\nDERRY\nCross Streets : 1-24  ROSEWOOD AVE
 (CAD Page for CFS 111610-52) ALS - Breathing Difficulty\n163 SUMMIT DR\nApt:\nDERRY\nCross Streets : 400-406  SIXTH ST * 1-14 CASTLE CT
 (CAD Page for CFS 111510-56) ALS - Diabetic\n7 MAPLE ST\nApt: ////\nKISTLER\nCross Streets : 150-198  N RIVERSIDE DR * 142-148 PARK RD
 
 Contact: justin trego <jtrego77@gmail.com>
 FRM:cmessages@co.mifflin.pa.us<Body%3AFRM%3Acmessages@co.mifflin.pa.us>\nSUBJ:CAD Page for CFS 013011-1\nMSG:Residential Fire\n56 RED SHALE LN\nApt:\nBRATTON\nCross Streets : 26-92  CARLISLE GAP RD
 
 Contact: Wendy Varner <wendy.s.varner@gmail.com>
 Sender: cmessages@co.mifflin.pa.us
 1 of 2\nFRM:cmessages@co.mifflin.pa.us\nSUBJ:CAD Page for CFS 011512-8\nMSG:ALS - Sick Person\n11 SUNSET RD\nApt: \nLEWISTOWN\nhttp://maps.google.c\n(Con't) 2 of 2\nom/maps?q=+40.58788%20-77.56015\nCross Streets : 1-8  E WILSON AVE * 1-8 HARDING AVE \n(End)
 
Contact: Russ Mcmahon <rmcmahon0@yahoo.com>
Sender: cmessages@co.mifflin.pa.us
Subject:CAD Page for CFS 031012-66\nALS - Breathing Difficulty\r\n204 CORNFIELD CIR\r\nApt: \r\nDERRY\r\nhttp://maps.google.com/maps?q=+40.61\r

 */
 
 public class PAMifflinCountyParser extends DispatchA4Parser {
   
   public PAMifflinCountyParser() {
     super("MIFFLIN COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "cmessages@co.mifflin.pa.us,cadmessages@co.mifflin.pa.us";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (! super.parseMsg(subject, body, data)) return false;
     if (TOWNSHIPS.contains(data.strCity)) data.strCity += " TWP"; 
     return true;
   }
 
   private static final MatchList TOWNSHIPS = new MatchList(new String[]{
       "ARMAGH", "BRATTON", "BROWN", "DECATUR", "DERRY", "GRANVILLE", "MENNO", "OLIVER", "UNION", "WAYNE"
   });
 }
