 package net.anei.cadpage.parsers.MT;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Lewis & Clark County, MT (alternate)
 Contact: support@active911.com
 Sender: runcards@lcpage.org
 
(MUTUAL AID REQ) REQ: 1 Management Staff |Engine 6411 |Tender 6423\nLOC: 5106 bluebell rd  TYPE: South Structure - 1st Alarm West Valley is requesting mutual aid, South Structure - 1st Alarm. CONTACT West Valley STAGING ON GOLD CHANNEL
 
 */
 public class MTLewisAndClarkCountyBParser extends FieldProgramParser {
 
   public MTLewisAndClarkCountyBParser() {
     super("LEWIS AND CLARK COUNTY", "MT",
            "REQ:CALL! LOC:ADDR! TYPE:INFO!");
   }
  
  @Override
  protected boolean parseMsg(String body, Data data) {
    body = body.replace('\n', ' ');
    return super.parseMsg(body, data);
  }
 }
