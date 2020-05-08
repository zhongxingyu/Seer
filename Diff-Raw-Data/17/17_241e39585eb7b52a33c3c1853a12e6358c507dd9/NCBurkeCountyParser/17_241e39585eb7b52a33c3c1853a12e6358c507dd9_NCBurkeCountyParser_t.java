 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Burke County, NC
 Contact: kevin head <headly1531@gmail.com>
 Sender: CAD@bceoc.org
 
 |CAD:69;MED;10D01;2447 LAIL RD;RIVER CLUB DR;CANNONBALL ST;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: 50 year old, Male, [1 of 2]
 |CAD:69;MVAPI;TURKEY TAIL LN/LINVILLE ST;[LAW] SUBJECT`S VEHICLES HAS ROLLED OVER AND HE WANTS TO BE CHECKED OUT -- ROUTINE [02/25/11 19:[1 of 2]
 |CAD:75;MED;17D03;5751 ABEE FARM ST;NURSERY RD;NEIL RD;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: You are responding to a [1 of 2]
 |CAD:75;FIRE;67B01;3678 SILVER CREEK CT;SILVER CREEK TER;[Fire Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Outside Fire. SMALL [1 of 2]
 |CAD:75;MED;26D01;5134 RAIN HILL CHURCH RD;MOUNT OLIVE CHURCH RD;RAINHILL DR;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: [1 of 2]
 
Contact: john huffman <threeammetal@gmail.com>
CAD:75;MED;31D02;4612 AMBER LN;DENTONS CHAPEL RD;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: You are responding to a patient who is
CAD:75;MED;10D02;1968 CONLEY RD;REESE STORE AV;FOREMAN ST;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: 44 year old, Male, Conscious,

 */
 
 public class NCBurkeCountyParser extends DispatchOSSIParser {
   
   public NCBurkeCountyParser() {
     super("BURKE COUNTY", "NC",
            "SRC CALL CODE? ADDR! X? X? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "CAD@bceoc.org";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    if (body.startsWith("|")) body = body.substring(1).trim();
     return super.parseMsg(body, data);
   }
   
   class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(Pattern.compile("\\d\\d[A-Z]\\d\\d"));
     }
   }
   
   @Override
   protected Field getField(String name) {
     if (name.equals("CODE")) return new MyCodeField();
     return super.getField(name);
   }
 }
