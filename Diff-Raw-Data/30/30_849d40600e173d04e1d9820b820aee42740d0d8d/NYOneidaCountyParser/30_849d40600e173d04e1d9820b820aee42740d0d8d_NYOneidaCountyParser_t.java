 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*    
 Oneida County, NY (in progress)
 Contact: jim humphreys <jimmycrack79@gmail.com>
 Sender: <"dispatch@oc911.org"@unknown.person>
 
 o;?WHIF:2010:0648\nDispatched\nEMS/BREATHING PROBLEMS\n111 HARDCASTLE AVE, WHITESBORO VILLAGE (MERITON DR/PERIMETER RD)
 o;?WHIF:2010:0641\nDispatched\nEMS/DIABETIC PROBLEM\n31 MOHAWK ST, WHITESBORO VILLAGE (/SAUQUOIT STNear:AMERICAN LEGION POST
 o;?WHIF:2010:0636\nDispatched\nINVESTIGATE\n124 HARTS HILL TERR, WHITESTOWN (GILBERT RD/Near:HARTS HILL INN)
 o;?WHIF:2010:0644\nDispatched\nMVA-PI\nHUGHES ST, WHITESBORO VILLAGE/ WEST ST, WHITESBORO VILLAGE
 o;?WHIF:2010:0677\nDispatched\nEMS/PSYCHIATRIC/SUICIDE ATTEMPT\n19 ELLMORE DR, WHITESBORO VILLAGE (SAUQUOIT ST/WIND PL)
 */
 
public class NYOneidaCountyParser extends SmsMsgParser {
 
   @Override
  protected boolean parseMsg(String body, Data data) {
 
     data.defState="NY";
     data.defCity="ONEIDA COUNTY";
     
    int pt = body.indexOf("o;?WHIF:");
    if (pt < 0) return false;
     body = body.substring(pt);
     
     String[] flds = body.split(" *\n *");
 
     String fld = flds[0];
     pt = fld.indexOf(':');
     if (pt >= 0) data.strCallId = fld.substring(pt+1);
     
    if (flds.length <= 2) return false;
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
