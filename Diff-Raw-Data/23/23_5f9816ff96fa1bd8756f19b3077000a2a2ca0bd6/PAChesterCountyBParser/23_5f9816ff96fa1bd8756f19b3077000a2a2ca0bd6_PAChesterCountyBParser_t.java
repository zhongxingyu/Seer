 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /* 
  Chester County, PA (version B)
  Contact: Jason Wilkins <j.wilkins728@gmail.com>
  Sender: afc23@comcast.net
 
  OVERDOSE - ALS *\n300 CEDAR SPRINGS RD - LIBERTY KNOLL APTS\nW BALTIMORE PK & CEDAR WOODS CI\nAPT 205\nNGARDN\n3RD HAND INFO/RP NOT ON LOC/UNK AGE MALE SAID HE OD'S ON ON PILLS/RP PUT ME ON HOLD\n02/21/2011\n15:09\n610-388-7400
  UNKNOWN TYPE FIRE *\n10 ALTEMUS DR\nWATSON MILL RD & DEAD END\nBROAD RUN KNOLL\nACROSS FROM ABV 1/4 MILE\nNGARDN\nSEES FIRE UNSURE IF ITS A BUILDING OR TRASH\n02/21/2011\n08:27\nRIOFSKI, LINDA P\n610-274-0906
  HOUSE FIRE *\n109 GARDEN STATION RD\nE AVONDALE RD & WHITESTONE RD\nLGROVE\nSTOVE INSIDE BASMENT ON FIRE\n02/20/2011\n17:50\nA/F\n610-322-0944/C
  ALARM - FIRE *\n8822 GAP NEWPORT PK\nCROSSAN LA & PENN GREEN RD\nRESD - LAFFERTY 610-268-2861\nNGARDN\nSMOKE DETECTOR IN KITCHEN - ENTER THRU BACK DO OR\n02/12/2011\n19:57\nLIFE ALERT-645\n800-638-8222
  [Update]\nSMOKE / ODOR INVEST (OUTSIDE)\nRT 41 / RT 841\nLGROVE\nHEAVY SMOKE CONDITION IN THE AREA - UNK WHAT'S IT'S ACTUALLY COMING FROM\n02/16/2011\n02:18\nROCHESTER, RANDOLF\n717-468-8174
  SMOKE / ODOR INVEST (OUTSIDE)\nRT 41 / RT 841\nLGROVE\nHEAVY SMOKE CONDITION IN THE AREA - UNK WHAT'S IT'S ACTUALLY COMING FROM\n02/16/2011\n02:18\nROCHESTER, RANDOLF\n717-468-8174
 
 (Messenger 911) BSICK\n65 GOOD HOPE RD\nPARSONS RD & PEACH TREE LA\nLDNBRT\n80/F FEVER AND BEDSORES/HX: HYPERTENSION, ALZH EIMERS, DEMENTIA\n11:59
 (Messenger 911) ACCINJ\n7440 LANCASTER PK, HOCKESSIN\nREQ AMB232----ASSIST NCC19 W/NUMEROUS PT'S IN ACCT\n14:07
 (Messenger 911) ACVA\n315 E LONDON GROVE RD - CHATHAM ACRES NH\nCHATHAM CHASE BL & HOWELL MOORE RD\nLGROVE\n56/M--CHANGE IN MENTAL STATUS--LETHARGIC--NUMB IN LEG\n20:0
 (Messenger 911) HOUSE\n300 ELIZABETH DR\nPENNS MANOR DR & PENNS MANOR DR\nPENNS MNAOR\nKNTTWP\nDECK AND GRILL ON FIRE ATTACHED TO THE HOUSE\n09:26
 (Messenger 911) BRUSH\n1027 NEWARK RD - CREATIVE PLAY DAY SCHOOL\nHILLENDALE RD & MAPLE LA\nNGARDN\nRP SEES BRUSH FIRE ACROSS FROM ABOVE---COULD S AY HOW BIG BUT APPE
 (Messenger 911) BFALL\n115 E SUMMIT AV\nPARKWAY AV & MYRTLE AV\nWGROVE\n62 YO/F FELL DOWN 5 STEPS\n21:07
 (Messenger 911) BMENTAL\n 202 E SECOND ST\n CHATHAM ST & HOOPES AL\n AVNDAL\n 20 YOA/M/, POSS ON ACT 64, SAID HE WANTED TO K ILL HIMSELF, BUT THEY DON'T THINK HE HAS DONE
 (Messenger 911) BFALL\n33 DOGWOOD DR\nHOLLY DR & OAK LA\nKNTTWP\n80/F--FELL FROM STANDING--LIFT ASSISTANCE\n09:49
 (Messenger 911) BALARMM\n122 JENNERS POND RD - LUTHER HOUSE 2\nGREENBRIAR LA & ALLYSSA LA\nPENN\nGEN MEDICAL - "NURSE CALL" - ANSWERING MACHINE ON PREMISE\n23:25
 (Messenger 911) BFALL\n33 DOGWOOD DR\nHOLLY DR & OAK LA\nKNTTWP\n80/F--FELL FROM STANDING--LIFT ASSISTANCE\n09:49
 (Messenger 911) BALARMM\n219 DALEVILLE RD\nBIRCHWOOD LA & FAGGS MANOR RD\nLONDER\nPENDANT ALARM - NO CONTACT MADE - UNK HX\n08:42
(Messenger 911) [Update]\nAUNRESP\n8995 GAP NEWPORT PK\nNEW GARDEN RD & BRITTANY DR\nNGARDN\nORIG 911HU-48/F\n14:03
 
  */
 
 public class PAChesterCountyBParser extends PAChesterCountyBaseParser {
   
   private boolean good;
   
   public PAChesterCountyBParser() {
    super("EMPTY? CALL ( ADDRCITY2 X2? | ADDRPL! X2? ( CITY | APT? INFO+? CITY! ) ) INFO+? DATETIME DATETIME? NAME? PHONE");
   }
   
   @Override
   public String getFilter() {
     return "afc23@comcast.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    good = subject.startsWith("Messenger 911");
     if (!good && !subject.equals("") && !subject.equals("Update")) return false;
     if (!parseFields(body.split("\n"), data)) return false;
     return good;
   }
   
  private class EmptyField extends SkipField {
    
    @Override
    public boolean canFail() {
      return true;
    }
    
    @Override
    public boolean checkParse(String field, Data data) {
      return field.length() == 0;
    }
  }
  
   private class MyDateTimeField extends DateTimeField {
     @Override
     public boolean checkParse(String field, Data data) {
       if (! super.checkParse(field, data)) return false;
       good = true;
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
    if (name.equals("EMPTY")) return new EmptyField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     return super.getField(name);
   }
   
 } 
