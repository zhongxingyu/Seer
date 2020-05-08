 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*  
 Mifflin County, PA
 (CAD Page for CFS 111710-43) ALS - Uncon/Syncope\n1 DERRY PARK DR\nApt:\nBURNHAM\nCross Streets : 400-412  S LOGAN BLVD
 (CAD Page for CFS 111710-42) URG - Ment/Emot/Psych\n41 INDUSTRIAL PARK RD\nApt: A\nGRANVILLE\nCross Streets : 708-924  LOOP RD * 2120-2456 MIDDLE RD
 (CAD Page for CFS 111710-37) ALS - Breathing Difficulty\n27 ANDERSON ST\nApt:\nBRATTON\nCross Streets : 1-19  MATTAWANA BLVD
 (CAD Page for CFS 111710-2) ALS - Motor Vehicle Accident\nROSEWOOD AVE and US HIGHWAY 522 NORTH\nApt: //\nDERRY\nCross Streets : 1-24  ROSEWOOD AVE
 (CAD Page for CFS 111610-52) ALS - Breathing Difficulty\n163 SUMMIT DR\nApt:\nDERRY\nCross Streets : 400-406  SIXTH ST * 1-14 CASTLE CT
 (CAD Page for CFS 111510-56) ALS - Diabetic\n7 MAPLE ST\nApt: ////\nKISTLER\nCross Streets : 150-198  N RIVERSIDE DR * 142-148 PARK RD
 (CAD Page for CFS 013011-1) Residential Fire\n56 RED SHALE LN\nApt:\nBRATTON\nCross Streets : 26-92  CARLISLE GAP RD
 (CAD Page for CFS 011512-8) MSG:ALS - Sick Person\n11 SUNSET RD\nApt: \nLEWISTOWN\nhttp://maps.google.c\n(Con't) 2 of 2\nom/maps?q=+40.58788%20-77.56015\nCross Streets : 1-8  E WILSON AVE * 1-8 HARDING AVE \n(End)
 
 */
 
 public class DispatchA4Parser extends FieldProgramParser {
   
   private static final Pattern SLASH_PTN = Pattern.compile("/+");
   
   public DispatchA4Parser(String defCity, String defState) {
     super(defCity, defState,
          "CALL! ADDR! Apt:APT! CITY! Cross_Streets:X!");
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.startsWith("CAD Page for CFS ")) return false;
     data.strCallId = subject.substring(17).trim();
     if (! parseFields(body.split("\n"), data)) return false;
     if (SLASH_PTN.matcher(data.strApt).matches()) data.strApt = "";
     return true;
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
   
 }
