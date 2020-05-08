 package net.anei.cadpage.parsers.dispatch;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Otsego County, NY
 1 of 10\nFRM:CAD@otsegocounty.com\nSUBJ:911 EVENT\nMSG:HAZARD ALL|WILBER NATIONAL BANK - COOPERS|5378 ST HWY 28   STA COOP3 XS CO HWY 26\n(Con't) 2 of 10\n/WALNUT|09:04|NARR SOUTH OF BANK MVA PDAA NEED FIR POLICE  PERSON: (COMPLAINANT) (FMLS) TIM  DONLAN\nDisclaimer:\n\nThis communication,\n(Con't 3 of 10\nincluding any attachments, may contain confidential information and is intended only for \nthe individual or entity to whom it is\n(Con't) 4 of 10\naddressed. Any review, dissemination, or copying of this communication \nby anyone other than the intended recipient is strictly\n(Con't) 5 of 10\nprohibited. If you are not the intended recipient, please \ncontact the sender by reply e-mail, delete and destroy all copies of the origi\nMore?
 1 of 10\nFRM:CAD@otsegocounty.com\nSUBJ:911 EVENT\nMSG:MVA PD|GRASSLANDS|ST HWY 28   STA COUNTY XS  |09:00|NARR 1 CAR PDMVA PERSON: (COMPLAINANT)\n(Con' 2 of 10\n(FMLS) SUE  JENNINGS\nDisclaimer:\nThis communication, including any attachments, may contain confidential information and is intended\n(Con't) 3 of 10\nonly for the individual or entity to whom it is addressed. Any review, dissemination, or copying of this communication by anyone\n(Con't) 4 of 10\nother than the intended recipient is strictly prohibited. If you are not the intended recipient, please contact the sender by reply\n(Con't) 5 of 10\ne-mail, delete and destroy all copies of the original message. No responsibility is accepted by Otsego County Government for any loss or\nMore?
 1 of 10\nFRM:CAD@otsegocounty.com\nSUBJ:911 EVENT\nMSG:TRAFFIC ACCIDENTS|| STA MIDD1 XS ST HWY 166 /CO HWY 52|08:22|NARR CAR OVER THE BANK -\n(Con't) 2 of 10\nROLLOVER PERSON: (COMPLAINANT)\nDisclaimer:\nThis communication, including any attachments, may contain confidential information and\n(Con't) 3 of 10\nis intended only for the individual or entity to whom it is addressed. Any review, dissemination, or copying of this communication\n(Con't) 4 of 10\nby anyone other than the intended recipient is strictly prohibited. If you are not the intended recipient, please contact the sender\n(Con't) 5 of 10\nby reply e-mail, delete and destroy all copies of the original message. No responsibility is accepted by Otsego County Government for an\nMore?
 1 of 10\nFRM:CAD@otsegocounty.com\nSUBJ:911 EVENT\nMSG:HAZARD ALL|| STA COOP4 XS CO HWY 52 /GATEWAY LN|08:14|NARR 2 CAR MVA NO\n(Con't) 2 of 10\nINJ\nDisclaimer:\nThis communication, including any attachments, may contain confidential information and is intended only for the\n(Con't) 3 of 10\nindividual or entity to whom it is addressed. Any review, dissemination, or copying of this communication by anyone other than the\n(Con't) 4 of 10\nintended recipient is strictly prohibited. If you are not the intended recipient, please contact the sender by reply e-mail, delete\n(Con't) 5 of 10\nand destroy all copies of the original message. No responsibility is accepted by Otsego County Government for any loss or damage arising\nMore?
 1 of 10\nFRM:CAD@otsegocounty.com\nSUBJ:911 EVENT\nMSG:TRAFFIC ACCIDENTS||4 GLEN AV  STA COOP1 XS CHESTNUT /BRUNLAR CT|13:06|NARR WPH2-S\n(Con't) 2 of 10\nVEH/BICYCLE -  PERSON: (COMPLAINANT) (FMLS) ZACK  MAHLUM\nDisclaimer:\nThis communication, including any attachments, may contain\n(Con't) 3 of 10\nconfidential information and is intended only for the individual or entity to whom it is addressed. Any review, dissemination, or\n(Con't) 4 of 10\ncopying of this communication by anyone other than the intended recipient is strictly prohibited. If you are not the intended\n(Con't) 5 of 10\nrecipient, please contact the sender by reply e-mail, delete and destroy all copies of the original message. No responsibility is acce\nMore?
 
 Chengango County, NY
 [911 EVENT]  BREATHING PROBLEMS|XTRA MART- GREENE|94 GENESEE ST  STA GREENE XS STATE HWY 12 /BIRDSALL ST|NARR PROQA DETAIL:DELTA 06D02 ABD PAIN| YOU ARE RESPON
 [911 EVENT]  STANDBY|GREENE HIGH SCHOOL|40 S CANAL ST  STA GREENE XS PAGE ST/S CHENANGO ST EXT
 [911 EVENT]  BREATHING PROBLEMS|XTRA MART- GREENE|94 GENESEE ST  STA GREENE XS STATE HWY 12 /BIRDSALL ST|NARR PROQA DETAIL:DELTA 06D02 ABD PAIN| YOU ARE RESPON
 [911 EVENT]  STANDBY|GREENE HIGH SCHOOL|40 S CANAL ST  STA GREENE XS PAGE ST/S CHENANGO ST EXT
 [911 EVENT]  CHEST PAIN|GREENE FAMILY MEDICINE|29 N CHENANGO ST  STA GREENE XS SCOTT  AV/ELM ST|NARR PROQA DETAIL:CHARLIE 10C04 CHEST PAIN| YOU ARE RESPONDING
 [911 EVENT]  HEMORRHAGE / LACERATION|NYS VETS HOME|4207 STATE HWY 220   STA OXFORD XS COUNTY RD 35 /COUNTY RD 32|NARR SPRUCE COTTAGE - 68YOM - NOSE BLEED - 21-
 [911 EVENT]  STRUCTURE FIRE/RESIDENTIAL||103 CURTIS COURT   STA SMITHVILLE XS ROUND POND  RD/***DEAD END***|NARR PERSON: (COMPLAINANT) (FMLS) VICKY CURTIS  NO
 [911 EVENT] MVA- UNKNOWN||  |STA NEW BERLIN XS STATE HWY 8 |NARR 1 CAR MVA OVER THE GARD RAIL UNKNOWN ON INJURIES BY THE OLD ROOSTER TAIL  1 MILE 1/2 SOUTH OF THE VILLAGE  PERSON: (COMPLAINANT) (FMLS) TERESA  HYZER  DARK COLORED CAR\n
 [911 EVENT] TRAUMATIC INJURIES (SPECIFIC)|UNADILLA VALLEY CENTRAL SCHOOL| 4238 STATE HWY 8,NEW BERLIN |STA NB/UVAC XS GOLF COURSE  RD/RAINBOW LN|NARR PROQA SUMMARY:BRAVO 30B01 FEMALE HIT IN THE FOREHEAD| 11 YEAR OLD, FEMALE, CONSCIOUS, BREATHING. TRAUMATIC INJURIES (SPECIFIC).\n
 [911 EVENT] UNCONSCIOUS/FAINTING (NEAR)|UNADILLA VALLEY CENTRAL SCHOOL| 4238 STATE HWY 8,NEW BERLIN |STA NB/UVAC XS GOLF COURSE  RD/RAINBOW LN|NARR PROQA SUMMARY:DELTA 31D03 UNRESPONSIVE| 45 YEAR OLD, FEMALE, CONSCIOUS, BREATHING. UNCONSCIOUS / FAINTING (NEAR).\n
 
 */
 
 
 public class DispatchA2Parser extends FieldProgramParser {
     
     public DispatchA2Parser(String defCity, String defState) {
       super(defCity, defState,
              "CALL PLACE ADDR SRCX? TIME? INFO+");
     }
 
 	  @Override
 	  protected boolean parseMsg(String subject, String body, Data data) {
 	    
 	    if (!subject.equals("911 EVENT")) return false;
 	    
 	    int ipt = body.indexOf("\nDisclaimer:");
 	    if (ipt >= 0) body = body.substring(0,ipt).trim();
 	    
 	    if (!parseFields(body.split("\\|"), data)) return false;
 	    if (data.strAddress.length() == 0) {
 	      parseAddress(data.strCross, data);
 	      data.strCross = "";
 	    }
 	    return true;
 	  }
 
 	  // Address field may contains Address, station, and cross steet info
 	  private class BaseAddressField extends AddressField {
 
       @Override
       public void parse(String field, Data data) {
         int pt = field.lastIndexOf(',');
         if (pt >= 0) {
           parseAddress(field.substring(0,pt).trim(), data);
          data.strCity = field.substring(pt).trim();
         } else {
           Parser p = new Parser(" " + field + " ");
           parseAddress(p.get(" STA "), data);
           data.strSource = p.get(" XS ");
           data.strCross = p.get();
         }
       }
 
       @Override
       public String getFieldNames() {
         return super.getFieldNames() + " SRC X CITY";
       }
 	  }
 	  
 	  private class BaseSourceCrossField extends Field {
 	    
 	    @Override
 	    public boolean canFail() {
 	      return true;
 	    }
 	    
 	    @Override
 	    public boolean checkParse(String field, Data data) {
 	      if (!field.startsWith("STA ")) return false;
 	      Parser p = new Parser(field.substring(4).trim() + " ");
 	      data.strSource =p.get(" XS ");
 	      data.strCross = p.get();
 	      return true;
 	    }
 
       @Override
       public void parse(String field, Data data) {
         if (!checkParse(field, data)) abort();
       }
 
       @Override
       public String getFieldNames() {
         return "SRC X";
       }
 	  }
 
 	  private class BaseInfoField extends InfoField {
 	    @Override
 	    public void parse(String field, Data data) {
 	      if (field.equals("NARR")) return;
 	      if (field.startsWith("NARR ")) field = field.substring(5).trim();
 	      super.parse(field, data);
 	    }
 	  }
 
     @Override
     protected Field getField(String name) {
       if (name.equals("ADDR")) return new BaseAddressField();
       if (name.equals("SRCX")) return new BaseSourceCrossField();
       if (name.equals("TIME")) return new TimeField("\\d\\d:\\d\\d");
       if (name.equals("INFO")) return new BaseInfoField();
       return super.getField(name);
     }
 	  
 	}
 	
