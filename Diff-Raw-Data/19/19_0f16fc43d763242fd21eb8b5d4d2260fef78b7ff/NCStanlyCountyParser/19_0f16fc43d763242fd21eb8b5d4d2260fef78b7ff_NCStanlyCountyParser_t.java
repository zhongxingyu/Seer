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
 
CAD:80-STRUCTURE ALARM/OLD CHARLOTTE RD/HARWOOD ST/ALB/SMOKE COMING FROM BARN ON OLD CHARLOTTE [04/03/11 17:44:15 BWHITLEY] B/T HARWOOD AND COBLE [04/03/11 17:
CAD:80-STRUCTURE ALARM/1700 WOODHURST LN/ALB/BIRD RD/ANDERSON RD/manual activation [04/03/11 17:54:08 DSHAVER]
CAD:80-DWELLING ALARM/601 EAST ST/ALB/N SEVENTH ST/N EIGHTH ST

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
           "CALL CALL? ADDR ADDR? CITY X X? INFO+");
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
   
  private class MyAddressField extends AddressField {
    @Override
    public void parse(String fld, Data data) {
      String oldAddress = data.strAddress;
      super.parse(fld, data);
      data.strAddress = append(oldAddress, " & ", data.strAddress);
    }
  }
  
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
    if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
