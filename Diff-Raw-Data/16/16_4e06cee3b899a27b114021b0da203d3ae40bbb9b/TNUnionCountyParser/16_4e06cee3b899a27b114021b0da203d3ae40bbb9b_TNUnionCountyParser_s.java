 package net.anei.cadpage.parsers.TN;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Union County, TN
 Contact: Joshua Durham <ff_j.durham@yahoo.com>
 Sender: dispatch@911email.net
 
 Subject:E911\nACCIDENT\n8654568681\nHIGHWAY 61 W, ANDERSONVILLE
 Subject:E911\nFIRE GENERAL\nBRIAN 8653232387\nMONROE RD, MAYNARDVILLE\nBRUSH FIRE
 Subject:E911\nFIRE GENERAL\nDAVID FRANKS 8652560768\n372 RACCOON VALLEY RD, Maynardville\nSMOKE IN THE AREA
 Subject:E911\nACCIDENT W/INJURY\nRAYMOND 8652565414\nOLD TAZEWELL PIKE, Corryton
 Subject:E911\nACCIDENT W/INJURY\nANGIE GATTO 8653605936\nMAYNARDVILLE HWY, MAYNARDVILLE\n4 PEOPLE
 Subject:E911\nFIRE RESIDENCE\nWILLIAM KRONER 8659925951\n1159 SATTERFIELD RD, MAYNARDVILLE
 Subject:E911\nFIRE RESIDENCE\nDAIL CAUGHORN 8652546959\n256 LUCAS LN, Maynardville
 Subject:E911\nTREE IN ROADWAY\nEDDIE SHOFFNER 8653141048\nHINDS CREEK RD, ANDERSONVILLE\nBLOCKING ROAD
 Subject:E911\nFIRE GENERAL\nSHIRLEY TURNER 4234891893\n116 MAYNARDVILLE HWY, MAYNARDVILLE\nADVISED THE POP MACHINE IS ON FIRE
 Subject:E911\nTREE IN ROADWAY\nSHIRLEY HAMMOCK 9116953536\nOAKLAND RD, Maynardville\nROADWAY IS BLOCKED
 Subject:E911\nTREE IN ROADWAY\nJENNIFER HARMON 8656807375\nBEARD VALLEY RD, MAYNARDVILLE\nTREE BLOCKING ROADWAY DANGEROUS AREA
 Subject:E911\nACCIDENT\nELIZABETH SAVAGE 8653854992\nHIGHWAY 61 W, Maynardville
 Subject:E911\nTREE IN ROADWAY\nMINDY HAWK 8652283331\nBUTCHER HOLLOW RD, MAYNARDVILLE\nTREE IN ROAD
 Subject:E911\nTREE IN ROADWAY\nWADE BRANTLEY 8658064337\nOUSLEY GAP RD, MAYNARDVILLE\nTOTAL ROAD BLOCKAGE
 Subject:E911\nOTHER\nMILLIE HARTGROVE 8659928378\n1983 HICKORY VALLEY RD, MAYNARDVILLE\nLIGHTENING STRUCK HOUSE
 Subject:E911\nSTORM OR WEATHER RELATED INCIDENTS/DAMAGE\nDAVID SEXTON 8655666873\nHIGHWAY 61 E, MAYNARDVILLE\nPOWER LINES IN ROAD WAY
 Subject:E911\nTREE IN ROADWAY\n\nOAKLAND RD, MAYNARDVILLE\nTREE BLOCKING ROAD
 Subject:E911\nTREE IN ROADWAY\nBRENDA MERRITT \nGUYTON DR, MAYNARDVILLE
 Subject:E911\nTREE IN ROADWAY\nDONNA EKERLY 8658515782\nJERRY HOLLOW RD, ANDERSONVILLE
 Subject:E911\nOTHER\nMARGARET TRENT 8653604629\n311 DOGWOOD LN, MAYNARDVILLE\nPOWER LINES DOWN
 Subject:E911\nFIRE BUSINESS\nUNION U COUNTY PROPER 4232110400\n901 MAIN ST, STE 115, MAYNARDVILLE\nHIGH VOLTAGE CLOSET
 Subject:E911\nTREE IN ROADWAY\n627 8653569465\nCHESTNUT RIDGE RD, MAYNARDVILLE\nBLOCKING THE ENTIRE ROAD
 Subject:E911\nACCIDENT W/INJURY\nEDDIE'S AUTO & SALVAGE 8659928151\n565 MAYNARDVILLE HWY, MAYNARDVILLE
 Subject:E911\nTREE IN ROADWAY\nJEREMY 8659926262\n466 HANSARD RD, MAYNARDVILLE
 Subject:E911\nTREE IN ROADWAY\nMICHAEL WITH PVFD 8657056703\nHANKINS HOLLOW RD, MAYNARDVILLE\nALREADY ONSCENE DO NOT TONE OUT JUST BUILD C
 Subject:E911\nACCIDENT W/INJURY\nKELLY HOLT 8656610782\n248 SATTERFIELD RD, Maynardville\n2 VEHICLE MVA
 Subject:E911\nCHEST PAIN\nTANASI GIRL SCOUT CAMP 8654947470\n123 DARK HOLLOW RD NORTH, ANDERSONVILLE
 Subject:E911\nBREATHING DIFFICULTY\nCHAD 8657409487\n1115 BEARD VALLEY RD, LOT-6, MAYNARDVILLE\n14 MONTH OLD FEMALE, LOSING BREATH
 Subject:E911\nFIRE GENERAL\nJANET CABBAGE 4232110397\n830 SATTERFIELD RD, MAYNARDVILLE\nLOG CABIN
 Subject:E911\nFIRE GENERAL\nROSCOE SANDS 8659920605\n1527 MAIN ST, MAYNARDVILLE
 
  */
 
 public class TNUnionCountyParser extends FieldProgramParser {
   
   public TNUnionCountyParser() {
     super("UNION COUNTY", "TN",
            "CALL NAMEPH ADDR INFO+");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@911email.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("E911")) return false;
     return parseFields(body.split("\n"), data);
   }
   
   // Name & Phone field
   private static final Pattern PHONE_PTN = Pattern.compile("\\b\\d+$");
   private class NamePhoneField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = PHONE_PTN.matcher(field);
       if (match.find()) {
         data.strPhone = match.group();
         field = field.substring(0,match.start()).trim();
       }
       data.strName = field;
     }
     
     @Override
     public String getFieldNames() {
       return "NAME PHONE";
     }
   }
 
   // Address, City field
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strCity = p.getLastOptional(',');
       super.parse(p.get(','), data);
       data.strApt = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR APT CITY";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("NAMEPH")) return new NamePhoneField();
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
