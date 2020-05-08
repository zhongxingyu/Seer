 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Surry County, NC
 Contact: Zackary Welch <zwelch85@gmail.com>
 Sender: SC911-CallAlert!!@co.surry.nc.us
 
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110425-043* Address - 105 DUTCH LN* City - MOUNT AIRY* Call Type - CARDIAC D* *﻿
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110424-196* Address - 1642 S MAIN ST* City - MOUNT AIRY* Call Type - RESP DIS D* *﻿
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110424-100* Address - 120 W PINE ST* City - MOUNT AIRY* Call Type - UNRESPONSIVE* *﻿
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110420-120* Address - 70 MAYBERRY MALL RD* City - MOUNT AIRY* Call Type - FALL* *﻿
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110419-061* Address - 908 REEVES DR* City - MOUNT AIRY* Call Type - CARD ARR E* *﻿
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110419-291* Address - 1053 N MAIN ST* City - MOUNT AIRY* Call Type - SICK C* *﻿
 SC911-CallAlert!!@co.surry.nc.us S: M:SC911 - Call Alert!!:Call Number - 110430-103* Address - 1020 NEWSOME ST* City - MOUNT AIRY* Call Type - ACCIDENT PI* *
 
 */
 
 public class NCSurryCountyParser extends FieldProgramParser {
   
   public NCSurryCountyParser() {
     super("SURRY COUNTY", "NC",
         "Call_Number:ID! Address:ADDR! City:CITY! Call_Type:CALL!");
   }
   
   @Override
   public String getFilter() {
     return "SC911-CallAlert!!@co.surry.nc.us";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (!body.startsWith("SC911 - Call Alert!!:")) return false;
     body = body.substring(21).trim();
     body = body.replace(" - ", ": ");
     return parseFields(body.split("\\*"),data);
   }
 }
