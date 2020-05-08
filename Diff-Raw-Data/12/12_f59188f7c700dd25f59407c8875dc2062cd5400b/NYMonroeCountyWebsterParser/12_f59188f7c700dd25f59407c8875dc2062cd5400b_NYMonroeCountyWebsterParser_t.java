 package net.anei.cadpage.parsers.NY;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Monroe County, NY (Webster) 
 Contact: Matt Micsak <wfd316@gmail.com>
 Sender: paging@rednmxcad.com
 System: Red Alert
 
 L: 701 AUDLEY END\nT: STRUCTURE FIRE\nO: \nB: \nC1: FINCHINGFIELD LA\nC2: WOODBRIDGE LA\nX: PLASTIC CONTAINER CAUGHT FIRE ON STOVE..COMP SAYS THAT CONTAINER IS SMOLD
 L: 286 PHILLIPS RD\nT: AUTOMATIC FIRE ALARM\nO: SMELTER RES - 265451\nB:\nC1: N CHIGWELL LA\nC2: MEADOW BREEZE LA\nX: FIRE ALRM ,GEN, C/C - ONLY CORNER STREET ALRM
 L: 736 BLUE CREEK DR\nT: AUTOMATIC FIRE ALARM\nO: FRANCO RESIDENCE\nB:\nC1: JOHN GLENN BL\nC2: LITHUANICA LA\nX: FIRE ALRM--SMOKE DETECTOR--UNK WHERE----C/C-----PH#
 L: 1271 FAIRPRT NINE MILE PT RD\nT: MVAIA - W/INJURIES\nO:\nB:\nC1: BAINBRIDGE LA\nC2: PLANK RD\nX: PRECAUTIONARY FOR CHEST PAIN DUE TO MVA
 
 Contact: John Overacker <joveracker@fairportfd.org>
 A: FAIF BOX: 0176\nL: 2 LEWIS ST\nT: CARDIAC/RESP-NOT BREATHING\nO: \nB: \nC1: POTTER PL\nC2: FILKINS ST\nX: 46 YO/M NOT BREATHING -- COLD TO THE TOUCH
 
Contact: Mark Smith <smity725@gmail.com>
Sender: chilifd@rednmxcad.com
L: 50 COTTAGE GROVE CI BOX: 0622\nT: CHIE - 6D2 : TRB BREATHING- DIFF SPEAKING            \nB: ROCH PRESB  PH:\nC1: BUFFALO RD\nC2: DEAD END\nX: 43 YO F PROB BREAT

 */
 
 
 public class NYMonroeCountyWebsterParser extends FieldProgramParser {
   
   public NYMonroeCountyWebsterParser() {
     super(CITY_CODES, "MONROE COUNTY", "NY",
          "BOX:BOX? L:ADDR! BOX:BOX? T:CALL! O:NAME? B:PLACE! PH:PHONE? C1:X! C2:X! X:INFO!");
   }
   
   @Override
   public String getFilter() {
    return "@rednmxcad.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
    body = body.replace(" BOX:", "\nBOX:").replace(" PH:", "\nPH:");
     return parseFields(body.split("\n"), data);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "AL", "Aldan",
       "AS", "Aston Twp",
       "BE", "Bethel Twp",
       "BM", "Birmingham Twp",
       "BR", "Brookhaven",
       "CC", "City Of Chester",
       "CF", "Chadds Ford Twp",
       "CH", "Chester Heights",
       "CL", "Clifton Heights",
       "CN", "Concord Twp",
       "CO", "Collingdale",
       "CW", "Colwyn",
       "DB", "Darby",
       "DT", "Darby Twp",
       "ED", "Eddystone",
       "EG", "Edgemont Twp",
       "EL", "East Lansdowne",
       "ES", "Essington Section Of Tinnicum Twp",
       "FL", "Folcroft",
       "GL", "Glenolden",
       "HV", "Haverford Twp",
       "LA", "Lansdowne",
       "LC", "Lower Chichester Twp",
       "LS", "Lester Section Of Tinnicum Twp",
       "MB", "Millbourne",
       "MD", "MiddletownTwp",
       "ME", "Media",
       "MH", "Marcus Hook",
       "MO", "Morton",
       "MP", "Marple Twp",
       "NP", "Nether Providence Twp",
       "NT", "Newtown Twp",
       "NW", "Norwood",
       "PK", "Parkside",
       "PP", "Prospect Park",
       "RN", "RadnorTwp",
       "RP", "Ridley Park",
       "RT", "RidleyTwp",
       "RU", "Rutledge",
       "RV", "Rose Valley",
       "SH", "Sharon Hill",
       "SP", "Springfield Twp",
       "SW", "Swarthmore",
       "TC", "Chester Twp",
       "TN", "Tinnicum Twp",
       "TR", "Trainer",
       "UC", "Upper Chichester Twp",
       "UD", "Upper Darby Twp",
       "UL", "Upland",
       "UP", "Upper Providence Twp",
       "YE", "Yeadon",
   });
   
 }
 	
