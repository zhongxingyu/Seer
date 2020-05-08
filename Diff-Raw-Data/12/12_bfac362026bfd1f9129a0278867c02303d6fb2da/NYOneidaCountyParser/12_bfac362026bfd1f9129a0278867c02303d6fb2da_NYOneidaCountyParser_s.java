 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*    
 Oneida County, NY
 Contact: jim humphreys <jimmycrack79@gmail.com>
 Contact: Gary Reese <greese7169@yahoo.com>
 Sender: <"dispatch@oc911.org"@unknown.person>
 
 o;?WHIF:2010:0648\nDispatched\nEMS/BREATHING PROBLEMS\n111 HARDCASTLE AVE, WHITESBORO VILLAGE (MERITON DR/PERIMETER RD)
 o;?WHIF:2010:0641\nDispatched\nEMS/DIABETIC PROBLEM\n31 MOHAWK ST, WHITESBORO VILLAGE (/SAUQUOIT STNear:AMERICAN LEGION POST
 o;?WHIF:2010:0636\nDispatched\nINVESTIGATE\n124 HARTS HILL TERR, WHITESTOWN (GILBERT RD/Near:HARTS HILL INN)
 o;?WHIF:2010:0644\nDispatched\nMVA-PI\nHUGHES ST, WHITESBORO VILLAGE/ WEST ST, WHITESBORO VILLAGE
 o;?WHIF:2010:0677\nDispatched\nEMS/PSYCHIATRIC/SUICIDE ATTEMPT\n19 ELLMORE DR, WHITESBORO VILLAGE (SAUQUOIT ST/WIND PL)
 i>¿WEVF:2010:0170\nDispatched\nEMS/STROKE/CVA\n9132 MAIN ST, WESTERN (GIFFORD HILL RD/GEORGE STNear:WOODS VALLEY
 i>¿WEVF:2010:0171\nDispatchedWIRES DOWN/BURNHILLSIDE RD, WESTERN
 i>¿WEVF:2010:0169\nDispatched\nFIRE STRUCTURE7893 GIFFORD HILL RD, WESTERN (CAMRODEN RD/ROUTE 46)
 o;?WHIF:2010:0704>Dispatched>EMS/ABDOMINAL PAIN>20 MOHAWK ST, WHITESBORO VILLAGE (SAUQUOIT ST/FERNBANK CIR; Near:AMERICAN LEGION POST 1113)
 
 ¿WEVF:2010:0182Dispatched31D02 - UNCONSCIOUS -- EFFECTIVE BREATHING9725 N STEUBEN RD, STEUBEN (STARR HILL RD/LEWIS RD;)
 i>¿WEVF:2011:0010DispatchedFIRE STRUCTURE8801 CAMRODEN RD, WESTERN (EVANS RD/TOWNLINE RD; Near:INGERSOLL TRAILER PARK)
 
 Contact: brad white <wfdcar1@gmail.com>
 i>¿WEMF:2011:0089Dispatched17A03 - PUBLIC ASSIST (NO INJURIES AND NO PRIORITY SYMPTOMS)7700 STONE RD, WESTMORELAND (PECKVILLE RD/STOP 7 RD;)
 
 Contact: James Brown <landscapesunlimitedcny@gmail.com>
 MSG:i>¿DURF:2011:0013DispatchedALARM EMS5593 ROUTE 46, VERONA (ROUTE 31/FOX ST;)
 MSG:i>¿DURF:2011:0013DispatchedALARM EMS5593 ROUTE 46, VERONA (ROUTE 31/FOX ST;)
 
 Contact: james rouillier jr <scaredtwo@gmail.com>
 ?LECF:2011:0085>Dispatched\n>17A03 - PUBLIC ASSIST (NO INJURIES AND NO PRIORITY SYMPTOMS)\n>5368 SLONE RD, LEE (LEE CENTER TABERG RD/SKINNER RD;)
 
 Contact: eric roux <eroux33@gmail.com>
 ?WEMF:2011:0144>Dispatched\n>ASSIST\n>6471 W CARTER RD, WESTMORELAND, NY
 
 Contact: James Griffiths <griffballs72@gmail.com>
Body:?DEEF:2011:0075>Dispatched\n>WIRES DOWN/BURN\n>6352 WALKER RD, DEERFIELD (CRUIKSHANK RD/MILLER RD;)
 
 */
 
 public class NYOneidaCountyParser extends SmsMsgParser {
   
   private static final Pattern DELIM = Pattern.compile(" *(?:\\n>?|>) *");
   
   public NYOneidaCountyParser() {
     super("ONEIDA COUNTY", "NY");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@oc911.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     int pt = body.indexOf("Dispatched");
     if (pt < 0) return false;
     
     pt = body.lastIndexOf(':', pt-1);
     if (pt < 0) return false;
     pt = body.lastIndexOf(':', pt-1);
     if (pt < 0) return false;
     body = body.substring(pt+1);
     
     
     String[] flds = DELIM.split(body);
 
     data.strCallId = flds[0];
     if (flds.length <= 2) return false;
     if (flds[1].equals("Dispatch")) return false;
     data.strCall = flds[2];
     
     if (flds.length <= 3) return false;
     Parser p = new Parser(flds[3]);
     parseAddress(p.get(','), data);
     data.strCity = p.getOptional('(');
     if (data.strCity.length() > 0) {
       data.strCross = p.get(')');
     } else {
       data.strCity = p.get('/');
       data.strCross = p.get();
     }
     return true;
   }
 }
