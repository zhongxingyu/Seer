 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Harford, MD Sample pages
 Contact: "Greg Dietrich" <greg.dietrich@nvfc10.org>
 Sender:  rc.279@c-msg.net
 Under box number, the N in this case actually represents a 10## box. Also notice that a separate text is sent for each piece of equipment due on a box assignment (see the two last text messages below.
 [!] EOC:A1091 UNC >UNCONSCIOUS 2200 CHANNEL RD XS: X BIG BRANCH BRDG #127 PYLESVILLE CRIZER,KIM BOX: N08 Cad: 2010-0000162717
 [!] EOC:A1091 SICK >SICK PERSON 1606 SCOTT RD XS: X FAWN GROVE RD PYLESVILLE BATCHELDER,MICHELLE BOX: N04 Cad: 2010-0000162427
 [!] EOC:A1091 DIAB >DIABETIC EMRG. 2145 HARKINS RD XS: SAINT PAULS CHURCH RD PYLESVILLE ROHREAUGH,KEN BOX: N04 Cad: 2010-0000162445
 [!] EOC:E1011 MISC >MISC 4873 CAREA RD XS: X WALNUT SPRING CT WHITE HALL SHERBS,ROBERT BOX: N08 Cad: 2010-0000162395
 [!] EOC:A1091 MVA >MV ACCIDENT W/INJURY SRT24&SAINT MARYS RD XS: SAINT MARYS RD PYLESVILLE DEDRICK.PATTI Cad: 2010-0000162161
 [!] EOC:E1011 MVA >MV ACCIDENT W/INJURY SRT24&SAINT MARYS RD XS: SAINT MARYS RD PYLESVILLE DEDRICK.PATTI Cad: 2010-0000162161
 [!] EOC:F03 BUILD >BUILDING FIRE 21 EASTERN AVE XS: S MAIN ST BEL AIR LEBRUN,MARLEEN BOX: 302 Cad: 2010-0000195539
 
 Subject:HCCAD\n[!] EOC:F03 WIRES >WIRES/POLE SHAWNEE DR&WALTERS MILL RD XS: WALTERS MILL RD FOREST HILL NOT ENTERED Cad: 2010-000019169
 
 Contact: Karen Zeiler <kmz1176@gmail.com>
 Sender: rc.391@c-msg.net
 (HCCAD) [!] EOC:E04 HEMM >HEMORRAGE/LACERATION 4324 HAMPTON HALL CT XS: DEAD END BELCAMP KOWALCHEK,JOHN BOX: 402 Cad: 2011-0000090313
 
 Contact: Ryan Pennington <ryan.pennington2@gmail.com>
 HCCAD / [!] EOC:E02 ARRES >CARDIAC ARREST 35 LINCOLN AVE Apt: C1 Bldg XS: PRITCHARD AVE ABERDEEN WARD,ASHANTE BOX: 211 Cad:11793\n\n
 
 Contact: Ian Williams <engine9company@gmail.com>
 [DVFC]  EOC:F09 911-CENTER SWR &gt;SWIFT/FLOOD WATER RES SRT24&amp;CHESTNUT HILL RD XS: CHESTNUT HILL RD FOREST HILL THOMPSON,DOUG Cad: 2011-0000122811
 
Contact: "R. Brian Frearson " <bfrearson@gmail.com>
FRM:rc.95@c-msg.net\nSUBJ:HCCAD\nMSG:[!] EOC:E91 HEART >HEART PROBLEM SRT155&SRT161 XS: SRT161 HAVRE DE GRACE Cad:143258\n

  */
 public class MDHarfordParser extends DispatchBParser {
   
   private static final String DEF_CITY = "HARFORD";
   private static final String DEF_STATE = "MD";
   
   private static final String[] CITY_LIST =new String[]{
     "ABERDEEN", "BEL AIR", "HAVRE DE GRACE", "JOPPA", "ABINGDON", "BELCAMP",
     "LEVEL", "CHURCHVILLE", "DARLINGTON", "WHITEFORD", "JARRETTSVILLE", "STREET",
     "HICKORY", "FALLSTON", "JOPPATOWNE", "FAWN GROVE", "DELTA", "PYLESVILLE", 
     "WHITE HALL", "FOREST HILL"};
 
   public MDHarfordParser() {
     super(CITY_LIST, DEF_CITY, DEF_STATE);
   }
   
   public String getFilter() {
     return "@c-msg.net";
   }
   
   @Override
   protected boolean isPageMsg(String body) {
     return true;
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     
     // First token following EOC: is the unit
     if (body.startsWith("HCCAD /")) body = body.substring(7).trim();
     if (body.startsWith("[!]")) body = body.substring(3).trim();
     if (! body.startsWith("EOC:")) return false;
     body = body.substring(4).trim();
     int pt = body.indexOf(' ');
     if (pt < 0) return false;
     data.strUnit = body.substring(0, pt);
     body = body.substring(pt+1).trim();
     if (!super.parseMsg(body, data)) return false;
     
     if (data.strCross.startsWith("X ")) data.strCross = data.strCross.substring(2);
     return true;
   }
 }
