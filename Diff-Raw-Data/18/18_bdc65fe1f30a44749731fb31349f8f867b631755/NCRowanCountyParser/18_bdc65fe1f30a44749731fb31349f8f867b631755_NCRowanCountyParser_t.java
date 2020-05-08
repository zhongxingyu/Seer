 package net.anei.cadpage.parsers.NC;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Rowan County, NC
 Contact: Timothy Ross <firefighter4047@gmail.com>
 Sender: 93001041
 System: OSSI Dispatch
 
 CAD:10C4 CHEST PAIN;201 N FRANKLIN ST;SWINK ST;PARK ST;CHGV;4301;RFG1
 CAD:FIRE DEPT SERVICE ASSIGNMENT;213 WASHINGTON ST;KLONDALE ST;CHGV;4301
 CAD:13D1 DIABETIC PROBLEM;1075 DEAL RD;KARRIKER FARMS RD;FIELD TRACE RD;MOOR;CARE INN RETIREMENT CENTER;4002;RFG4
 CAD:RFD COMMAND ESTABLISHED;2470 E NC 152 HWY; CHGV
 CAD:29B1 TRAFFIC ACCIDENT WITH INJ;E NC 152 HWY/S I 85;CHGV;4123;RFG2
 CAD:RFD COMMAND ESTABLISHED;E NC 152 HWY/S I 85; CHGV
 
 Contact: Jamie Thomas <jamie.thomas75@yahoo.com>
 Sender: 93001018
 CAD:STRUCTURE FIRE;625 SIDES RD;GOLD KNOB RD;GOLD KNOB RD;SALS;7106;RFG1
 
 Contact: Jeremy Shimberg <jshimberg@carolina.rr.com>
 Sender: CAD@co.rowan.nc.us
 CAD:13D1 DIABETIC PROBLEM;3035 STOKES FERRY RD;EDZELL DR;EARNHARDT RD;SALS;7604;RFG4
CAD:10D1 CHEST PAIN;6235 FISH POND RD;SWANNER PARK DR;ODDIE RD;SALS;7605;RFG1
CAD:6C1 BREATHING PROBLEMS;140 PARTRIDGE TR;PALOMINO DR;DEER RD;SALS;7603;RFG1
CAD:FIRE ALARM;1190 LONG FERRY RD;N LONG FERRY OFF;WILLOW CREEK DR;SALS;RUSHCO (LIBERTY) STATION #18;6401;RFG1
 
 */
 
 public class NCRowanCountyParser extends DispatchOSSIParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CHGV", "CHINA GROVE",
       "CLVD", "CLEVELAND",
       "ESPN", "EAST SPENCER",
       "FATH", "FAITH",
       "GOLD", "GOLD HILL",
       "GRQY", "GRANITE QUARRY",
       "KANN", "KANNAPOLIS",
       "LAND", "LANDIS",
       "MOOR", "MOORESVILLE",
       "ROCK", "ROCKWELL",
       "SALS", "SALISBURY",
       "SPEN", "SPENCER",
       "WOOD", "WOODLEAF"
   }); 
   
   public NCRowanCountyParser() {
     super(CITY_CODES, "ROWAN COUNTY", "NC",
            "CALL ADDR X/Z+? CITY PLACE? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "9300,CAD";
   }
   
   private class MyPlaceField extends PlaceField {
     
     @Override
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       
       if (field.length() <= 4) return false;
       parse(field, data);
       return true;
     }
   }
   
   @Override
   protected Field getField(String name) {
     if (name.equals("PLACE")) return new MyPlaceField();
     return super.getField(name);
   }
 }
