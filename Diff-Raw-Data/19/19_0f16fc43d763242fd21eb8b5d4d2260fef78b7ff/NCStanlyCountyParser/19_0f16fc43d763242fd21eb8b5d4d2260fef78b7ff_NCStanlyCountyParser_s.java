 package net.anei.cadpage.parsers.NC;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Stanly County, NC
 Contact: "D. E. Gerald" <trafficfuzz@yahoo.com>
 Sender:CAD@sclg.gov
 
 CAD:E6-BREATHING PROBLEMS/533 BARRINGER CHAPEL RD/NOR/WIL-KRIS DR/FORK RD/[Medical Priority Info] PROBLEM: cant breathe # PATS: 1 AGE: 24 Years SE
 CAD:E12-SEIZURES/CONVULSION/5761 DOVE LN/NOR/LOOP RD/[Medical Priority Info] PROBLEM: # PATS: 1 AGE: 49 Years SEX: Male CONSCIOUS: No BREATHING: Y
 CAD:E6-BREATHING PROBLEMS/533 BARRINGER CHAPEL RD/NOR/WIL-KRIS DR/FORK RD/[Medical Priority Info] PROBLEM: cant breathe # PATS: 1 AGE: 24 Years SE
 CAD:80-DWELLING ALARM/8947 WHITLEY RD/NOR/W WHITLEY ST﻿
 CAD:80-STRUCTURE ALARM/9029 COTTONVILLE RD/NOR/HINSON RD/S STANLY SCHOOL RD/3 KEYPAD FIRE ALARM, 2 BURGLAR, AND HOLD UP ALARM [03/03/11 19:12:48 C﻿
 CAD:E10-CHEST PAINS/231 PRICE ST/NOR/CARVER ST/WIL-KRIS DR/[Medical Priority Info] PROBLEM: CHEST PAIINS.VOMITTING # PATS: 1 AGE: 66 Years SEX: Ma
 
 */
 
 public class NCStanlyCountyParser extends DispatchOSSIParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ALB", "ALBEMARLE",
       "BAD", "BADIN",
       "LOC", "LOCUST",
       "NOR","NORWOOD"
   });
   
   public NCStanlyCountyParser() {
     super(CITY_CODES, "STANLY COUNTY", "NC",
           "CALL CALL? ADDR CITY X X? INFO+");
     setDelimiter('/');
   }
   
   @Override
   public String getFilter() {
     return "CAD@sclg.gov";
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String fld, Data data) {
       data.strCall = append(data.strCall, "/", fld);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     return super.getField(name);
   }
 }
