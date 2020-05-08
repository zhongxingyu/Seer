 package net.anei.cadpage.parsers.IL;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 Roscoe, IL
 Contact: Ron Klaman <hrfdrklaman@gmail.com>
 Sender: hrfdfiresvcrunnotification@gmail.com
 S:HRFD: Med: 687 Marion Rd M: 56 f deleious\n\n
 S:HRFD: Med:9918 Bluebonnet M: 70 M diaahra\n\n
 
 Contact: John Donovan <hrfdfireman@gmail.com>
 Sender: hrfdfiresvcrunnotification@gmail.com
 (HRFD: Med:14244 krotz Dr) m fall victim
 (HRFD: Med:6792 Michelle Dr) F Trouble Breathing
 
 Contact: Thomas Aaker <hrfdtaaker@gmail.com>
 Sender: hrfdfiresvcrunnotification@gmail.com
 S:HRFD: Med:4694 indigo place M: c/o alarm no \n
 
 */
 
 public class ILRoscoeParser extends SmsMsgParser {
   
   public ILRoscoeParser() {
     super("ROSCOE", "IL");
   }
   
   @Override
   public String getFilter() {
     return "hrfdfiresvcrunnotification@gmail.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    int pt = subject.indexOf(": Med:");
    if (pt < 0) return false;
    data.strSource = subject.substring(0, pt).trim();
    parseAddress(subject.substring(pt+6).trim(), data);
     data.strCall = body;
     return true;
   }
 }
