 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Davie County, NC
 Contact: Michael Wilson <fireman1700@gmail.com>
 
 911:Call #110619-2932* Address:866 ANGELL RD* * * City:MOCKSVILLE* * Type:HC* HAZARDOUS CONDITION* HILL JOEL* PH#:336-940-2303* Units:17* IRA:* Medical: No* Haza.
 911:Call #110622-3347* Address:2388 LIBERTY CHURCH RD* * * City:MOCKSVILLE* * Type:19D4* HEART PROBLEMS* HALL GENE* PH#:336-492-5114* Units:17,34,ST4,YC24* IRA:*
 911:Call #110622-3355* Address:389 LIBERTY CHURCH RD* * * City:MOCKSVILLE* * Type:MED* MEDICAL CALL* CULLER C O* PH#:336-492-7249* Units:17* IRA:* Medical: No* H
 911:Call #110623-3420* Address:261 COOPER CREEK DR* * * City:MOCKSVILLE* Geo Comment: BUSINESSES IN CITY LIMITS NBH: OFF 1734 US HWY 601 N Landmark Comment: KNOX
 911:Call #110624-3510* Address:3868 US HWY 601 N* * * City:MOCKSVILLE* * Type:YEL* CARDIAC PROBLEM* HOHFF, EARL V* PH#:336-492-3017* Units:17* IRA:* Medical: No*
 
 911:Call #110627-3974* Address:NC HWY 801 N // BONKIN LAKE RD* * * City:MOCKSVILLE* * Type:GWB* GRASS/WOODS/BRUSH FIRE* PAUL TONY* PH#:336-575-4524* Units:17,23*
 911:Call #110630-4406* Address:284 BRANGUS WAY* * * City:MOCKSVILLE* NBH: OFF 2386 CANA RD* Type:VF* VEHICLE FIRE* MEADER CORTLAND J* PH#:336-940-2666* Units:17
 911:Call #110628-4171* Address:700 RICHIE RD* * * City:MOCKSVILLE* NBH: FROM 444 EATONS CHURCH RD TO 3558 US HWY 601 N NBH: I40 TO FARMSTEAD LN* Type:HC* HAZARDO
 
 Contact: Brent Crotts <jvfd2110@gmail.com>
 911@[70.60.255.70] 911:Call #110713-6266* Address:154 CRESTVIEW DR* * * City:MOCKSVILLE* NBH: OFF 499 EAST LAKE DR* Type:26A2-11* SICK PERSON (SPECIFIC DIAGNOSIS)* linda packett* P
 
 Contact: Kenneth Wheeler <wheelerk1411@yahoo.com>
 Sender: From:911@co.davie.nc.us
 911:Call #111107-2492* Address:100 SUN BURST LN* * * City:MOCKSVILLE* NBH: OFF 1627 JUNCTION RD NEAR NOLLEY RD* Type:HC* HAZARDOUS CONDITION* *
 911:Call #111108-2718* Address:255 NEELY RD* * * City:COOLEEMEE* * Type:SF* STRUCTURE FIRE* FOWLER ANSLO* PH#:336-284-4275* Units:14* IRA:* Me
 911:Call #120201-4100* Address:1407 JUNCTION RD* * * City:MOCKSVILLE* * Type:50PD* ACCIDENT/DAMAGE* INNES* PH#:704-640-5463* Units:14* IRA:*
 
 Contact: support@active911.com
 911:Call #120223-6774* Address:277 OLD TOWNE DR* * * City:ADVANCE* Geo Comment: KINDERTON DEV NBH: OFF 160 BLOCK YADKIN VALLEY RD* Type:MED* MEDICAL CALL* GAIL GODWIN* PH#:336-909-2667* Units:24* IRA:* Medical: No* Hazards: No* NARR:02/23/2012 01:51:24 : pos2 : MCROWE Cross streets: BROOKSTONE DR//DEADEND Geo Comment: KINDERTON DEV NBH: OFF 160 BLOCK YADKIN VALLEY RD*
 
  */
 
 
 public class NCDavieCountyParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile("\\*(?: \\*)*"); 
   
   public NCDavieCountyParser() {
     super("DAVIE COUNTY", "NC",
           "ID Address:ADDR! City:CITY! Geo_Comment:INFO2? NBH:INFO3 Type:CALL CALL NAME PH:PHONE Units:UNIT IRA:SKIP INFO+ Geo_Comment:INFO2");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     int ipt = body.indexOf("911:Call #");
     if (ipt < 0) return false;
     body = body.substring(ipt+10).trim();
     body = body.replace("PH#:", "PH:");
     return parseFields(DELIM.split(body), data);
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       field = field.replaceAll("//+", "/");
       super.parse(field, data);
     }
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   private class MyInfo2Field extends InfoField {
     @Override
     public void parse(String field, Data data) {
      data.strSupp = append(data.strSupp, " ", "Geo Comment: " + field);
     }
   }
   
   private class MyInfo3Field extends InfoField {
     @Override
     public void parse(String field, Data data) {
      data.strSupp = append(data.strSupp, " ", "NBH: " + field);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("INFO2")) return new MyInfo2Field();
     if (name.equals("INFO3")) return new MyInfo3Field();
     return super.getField(name);
   }
 }
