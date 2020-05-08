 package net.anei.cadpage.parsers.VA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.dispatch.DispatchDAPROParser;
 
 /*
 Franklin County, VA
 Contact: Larry Davids <davidsla07@gmail.com
 Sender: mailbox@franklincountyva.org
 
 MAILBOX:S07 EMS-OTHER/DEFINE 18360 VIRGIL H GOODE HWY 124 RMT CFS# 2010-030542 CROSS: SHADY LN/BLACKWATER RIVER
 MAILBOX:S07 EMS-CHEST PAIN 413 WOODDALE DR RMT CFS# 2010-030355 CROSS: VIRGIL H GOODE HWY/DEAD END
 MAILBOX:S07 EMS-PATIENT FALLEN 3005 GREEN LEVEL RD RMT CFS# 2010-030541 CROSS: GRASSY HILL RD/LITTLE MOUNTAIN DR
 MAILBOX:S07 EMS-CARDIAC VIRGIL H GOODE HWY & LINK ST RMT CFS# 2010-030580
 MAILBOX:S07 EMS-HIGH BLOOD PRESSURE 1808 BETHLEHEM RD BML CFS# 2010-030643 CROSS: BETHANY RD/DILLONS MILL RD
 MAILBOX:C07 FIRE-ALARM COMMERCIAL 3325 GRASSY HILL RD RMT CFS# 2010-033866 CROSS: JAMESTOWN RD/CALLAWAY RD
 */
 
 public class VAFranklinCountyParser extends DispatchDAPROParser {
   
   
   private static final Properties CITY_CODE_TABLE = 
     buildCodeTable(new String[]{
       "RMT", "Rocky Mount",
       "BML", "Boones Mill",
       "CAL", "Calaway"
     });
 
   
   public VAFranklinCountyParser() {
     super(CITY_CODE_TABLE, "FRANKLIN COUNTY", "VA");
   }
       
   
   @Override
   public String getFilter() {
     return "mailbox@franklincountyva.org";
   }
 
 }
