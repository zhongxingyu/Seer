 package net.anei.cadpage.parsers.MO;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Gasconade County, MO
 Contact: Jason Reed <reedjasonf@gmail.com>
 Sender: central@fidmail.com
 System: Global Dispatch
 
 MESSAGE / M50 CHEST PAIN  4449 MARIES CR 426 OUT OF COUNTY MapRegions:  CrossStreets: Description:  Dispatch: 3/25/2011 03:51:43\n\n\n\n\n
 MESSAGE / M51 MOTOR VEHICLE ACCIDENT-INJURIES  HWY 89  MapRegions:  CrossStreets: Description:  Dispatch: 3/26/2011 11:29:32\n\n\n\n\n
 MESSAGE / M51 MOTOR VEHICLE ACCIDENT-UNKNOWN INJ  HWY 89  MapRegions:  CrossStreets: Description:  Dispatch: 3/26/2011 11:39:53\n\n\n\n\n
 M51 FALL- PERSON FELL 408 N ALVARADO AVE BELLE MapRegions: CrossStreets: E BELAIR AVE 0.03 mi S E ROHRER DR 0.13 mi N Description: Dispatch: 3/24/2011 11:47:54
 M50 SICK PERSON- SICK CASE 4644 HIGHWAY B, Apt. BOX 14 GASCONADE COUNTY MapRegions: PAGE 103 CrossStreets: ELK HEAD RD 0.08 mi SW RED BIRD RD 0.46 mi N Description: HALF MILE SOUTH OF REDBIRD RD Dispatch: 3/24/2011 11:25:45
 M50 SICK PERSON- SICK CASE 901 W EIGHTH ST BELLE MapRegions: CrossStreets: S SHOCKLEY AVE 0.01 mi NE S APPLE AVE 0.19 mi E Description: Dispatch: 3/24/2011 21:05:48
 
 Contact:  bretth530 <bretth530@gmail.com>
 501 FALL- PERSON FELL FRENE VALLEY HEALTHCARE SOUTH 1016 W HIGHWAY 28 OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 072 CrossStreets: HIGHWAY Y 0.05 mi N S L&D DR 0.44 mi SW Description: 96 YOA FALL BROKEN WRIST Dispatch: 4/3/2011 06:00:43
 
Body: 1 of 2\nFRM:central@fidmail.com\nSUBJ:MESSAGE\nMSG:\n501 ACCIDENTAL INJURY  1009 SPRINGFIELD RD, Apt. APT 21 OWENSVILLE MapRegions: OFD SECTOR 1,\n(Con't) 2 of 2\nPAGE 065, PAGE 122 CrossStreets: CHERRY AVE 0.06 mi SE INDUSTRIAL DR 0.22 mi NE Description:  Dispatch: 4/6/2011 12:08:04\n\n\n\n(End)
 
 */
 
 
 public class MOGasconadeCountyParser extends FieldProgramParser {
   
   private static final String[] CITY_TABLE = new String[]{
     "BLAND",
     "BELLE",
     "GASCONADE",
     "HERMANN",
     "MORRISON",
     "MT STERLING",
     "OWENSVILLE",
     "ROSEBUD",
     "GASCONADE COUNTY",
     "OUT OF COUNTY"
   };
   
   public MOGasconadeCountyParser() {
     super(CITY_TABLE, "", "MO",
            "ADDR/SC! MapRegions:MAP! CrossStreets:X! Description:INFO Dispatch:SKIP");
   }
   
   @Override
   public String getFilter() {
     return "central@fidmail.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.startsWith("MESSAGE / ")) body = body.substring(10).trim();
     return super.parseMsg(body, data);
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       field = field.replaceAll(",", "");
       super.parse(field, data);
       if (data.strCity.equals("GASCONADE COUNTY")) data.strCity = "";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
