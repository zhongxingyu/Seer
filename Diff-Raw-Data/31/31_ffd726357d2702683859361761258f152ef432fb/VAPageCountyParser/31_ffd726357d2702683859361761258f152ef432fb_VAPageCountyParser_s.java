 package net.anei.cadpage.parsers.VA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.dispatch.DispatchDAPROParser;
 
 
 /*
 Page County, VA
 Contact: Shane Taylor <hm88jr@gmail.com>
 Sender: 93001045
 
 MAILBOX:RS3 PAIN 16 E MAIN ST LUR CFS# 2011-000782 CROSS: BROAD ST/TANNERY RD
 MAILBOX:RS4 CHEST PAINS 185 GRAY DR STA CFS# 2011-004572 CROSS: AYLOR GRUBBS AV
 MAILBOX:RS1 GENERAL ILLNESS 117 PULASKI AV SHE CFS# 2011-004542 CROSS: S SECOND ST/FRONT ST
 
 Contact: Ben Zimmerman <zimmerbm@gmail.com>
 MAILBOX:CO24 COMMERCIAL ALARM 525 MIDDLEBURG RD STA CFS# 2011-005247 CROSS: US HWY BSN 340/GOODRICH RD
 
 */
 
 public class VAPageCountyParser extends DispatchDAPROParser {
   
   
   private static final Properties CITY_CODE_TABLE = 
     buildCodeTable(new String[]{
       "LUR", "LURAY",
       "SHE", "SHENANDOAH",
       "STA", "STANLEY"
     });
 
   
   public VAPageCountyParser() {
     super(CITY_CODE_TABLE, "PAGE COUNTY", "VA");
   }
   
 }
