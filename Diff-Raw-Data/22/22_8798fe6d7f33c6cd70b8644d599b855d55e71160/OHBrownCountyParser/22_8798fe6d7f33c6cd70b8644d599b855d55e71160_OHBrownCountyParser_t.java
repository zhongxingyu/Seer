 package net.anei.cadpage.parsers.OH;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchA1Parser;
 
 /*
 Brown County, OH
 Contact: "kieth" <kiethw@frontier.com>
 Sender: browncommctr@roadrunner.com
 
 [Alert: SMOKE IN THE AREA] ALRM LVL: 1 \nLOC: \n13101 PURDY RD\nWASHINGTON \nBTWN: N/A & N/A\n\nRCVD AS Phone\n\nCOM: CALLER ADV MAN BURNING SOMETHING ABOUT TWO HO
 (Alert: SICK PERSON (SPECIFIC DIAG)) \nALRM LVL: 1 \nLOC: DEERFIELD TOWNHOUSE \nAPARTMENTS \n455 W MAIN ST \nAPT 703 \nMT ORAB \nBTWN: N/A & N/A\n\nRCVD AS Phone\n\nCOM
 [Alert: SMOKE IN THE AREA]  ALRM LVL: 1 \nLOC:\n13101 PURDY RD\nWASHINGTON \nBTWN: N/A & N/A\n\nRCVD AS Phone\n\nCOM:\nCALLER ADV MAN BURNING SOMETHING ABOUT TWO HO (comments)
 (Alert: SICK PERSON (SPECIFIC DIAG)) ALRM LVL: 1\nLOC:\nDEERFIELD TOWNHOUSE APARTMENTS\n455 W MAIN ST\nAPT 703\nMT ORAB\nBTWN: N/A & N/A\n\nRCVD AS Phone\n\nCOM:
 (Alert: BREATHING PROBLEMS) ALRM LVL: 1\nLOC:\n14885 EASTWOOD RD\nSTERLING\nBTWN: TRI-COUNTY HWY & N/A\n\nRCVD AS 911\n\nCOM:\n60 YOF\n\nCT:\n6D0013 at POS 03
 (Alert: PSYCH/ABNRML BEHVR/SUICIDE ATT) ALRM LVL: 1\nLOC:\n14964 HILL CREST RD\nGREEN\nBTWN: BARDWELL-BUFORD RD & GREENBUSH EAST RD\n\nRCVD AS Phone\n\nCOM:\nCAL
 (Alert: BREATHING PROBLEMS) ALRM LVL: 1\nLOC:\n1871 CROSSTOWN ROAD\nSTERLING TWP\nBTWN: N/A & N/A\n\n\n\nCOM:\n78 Y O F\nHAVING TROUBLE BRAETHING\nCOPD\nDIFFICULTY
 (Alert: CHEST PAIN (NON TRAUMATIC)) ALRM LVL: 1\nLOC:\n7703 GARDNER RD\nLEWIS\nBTWN: OAK GROVE RD & N/A\n\nRCVD AS Phone\n\nCOM:\nSEVERE CHEST PAINS\n77 Y O M\nPA
 (Alert: PSYCH/ABNRML BEHVR/SUICIDE ATT) ALRM LVL: 1\nLOC:\n200 COLUMBIA ST\nHIGGINSPORT\nBTWN: OLIVE ST & JOHN ST\n\nRCVD AS Phone\n\nCOM:\nBOYFRIEND WENT TO CHE
 (Alert: SICK PERSON (SPECIFIC DIAG)) ALARM LVL: 1\nLOC:\nDEERFIELD TOWNHOUSE APARTMENTS\n455 W MAIN ST\nAPT 703\nMT ORAB\nBTWN: N/A & N/A\n\nRCVD AS Phone\n\nCOM:
 (Alert: STRUCTURE FIRE UNCONFIRMED) ALARM LVL: 1\nLOC:\n2567 US RTE 134\nBTWN: N/A & N/A\n\nRCVD AS PHONE\n\nCOM: ADVISED SMOKE WAS COMING OUT OF THE WINDOWS CA
 (Alert: FIRE ALARM) ALARM LVL: 1\nLOC:\n11519 US RTE 62\nEAGLE\nBTWN: N/A & MAPLE DR\n\nRCVD AS PHONE\n\nCOM: FIRE ALARM EASTERN MIDDLE SCHOOL DUCT SMOKE D
(Alert: BREATHING PROBLEMS) ALRM LVL: 1\r\nLOC:\r\n6072 TRI-COUNTY HWY\r\nLOT 1\r\nWASHINGTON\r\nBTWN: WARDLOW RD & N/A\r\n\r\nRCVD AS Phone\r\n\r\nCOM:\r\nBREATHING PROBLE
 
  */
 
 
 public class OHBrownCountyParser extends DispatchA1Parser {
   
   public OHBrownCountyParser() {
     super("BROWN COUNTY", "OH");
   }
   
   @Override
   public String getFilter() {
     return "browncommctr@roadrunner.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!super.parseMsg(subject, body, data)) return false;
     if (data.strCross.startsWith("N/A & N/A")) data.strCross = "";
     else if (data.strCross.startsWith("N/A & ")) {
       data.strCross = data.strCross.substring(6).trim();
     }
     else if (data.strCross.endsWith(" & N/A")) {
       data.strCross = data.strCross.substring(0,data.strCross.length()-6).trim();
     }
     return true;
   }
 }
