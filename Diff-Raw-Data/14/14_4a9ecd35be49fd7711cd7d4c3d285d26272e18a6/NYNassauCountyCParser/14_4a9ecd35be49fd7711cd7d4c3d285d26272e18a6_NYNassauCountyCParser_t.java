 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Nassau County, NY (version C)
 Contact: Besnik Gjonlekaj <tigerfdny@gmail.com>
 Sender: backup@westburyfd.xohost.com
 System: Fire Rescue Systems (SCM)
 
 * 3- AUTO ACCIDENT W/AIDED * POST ROAD & JERICHO TURNPIKE  POST ROAD CS: JERICHO TURNPIKE TOA: 14:03 04-07-11
 * 16- AFA * PUBLIC STORAGE 1055 STEWART AVENUE CS: MERCHANTS CONCOURSE \ SOUTH STREET TOA: 15:58 04-07-11
 * 8- MUTUAL AID RESCUE * LAND LN CS: CARMAN AVENUE TOA: 11:41 04-08-11
 * 3- AUTO ACCIDENT W/ AIDED * TACO BELL #3204 23 OLD COUNTRY ROAD CS: GRAND STREET \ CENTRAL AVENUE TOA: 13:50 04-08-11
 * 4- SIGNAL 9  * LLOYD L. LANGLEY 248 SHERMAN STREET CS: BROADWAY \ ROMAN AVENUE TOA: 19:47 04-08-11
 
Contact: kevin williams <kevinc.williams2@gmail.com>
* 3- AUTO ACCIDENT W/AIDED * BEST BUY (STORE #454) 1100 OLD COUNTRY ROAD CS: MERCHANTS CONCOURSE  \ ZECKENDORF BOULEVARD TOA: 00:18 09-29-11

 */
 
 
 public class NYNassauCountyCParser extends FieldProgramParser {
 
   public NYNassauCountyCParser() {
     super("NASSAU COUNTY", "NY",
            "ADDR/SP CS:X TOA:SKIP");
   }
   
   @Override
   public String getFilter() {
     return "backup@westburyfd.xohost.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (! body.startsWith("* ")) return false;
     int pt = body.indexOf(" * ", 2);
     if (pt < 0) return false;
     data.strCall =  body.substring(2,pt).trim();
     body = body.substring(pt+3).trim();
     
     return super.parseMsg(body, data);
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       int pt1 = field.indexOf("  ");
       if (pt1 >= 0) {
         int pt2 = field.lastIndexOf("  ");
         if (pt1 == pt2) {
           if (field.startsWith("CODE ")) {
             pt2 = field.length();
           } else {
             pt1 = -2;
           }
         }
         if (pt1 >= 0) data.strCall = append(data.strCall, " ", field.substring(0,pt1).trim());
         if (pt2 < field.length()) data.strPlace = field.substring(pt2+2).trim();
         field = field.substring(pt1+2,pt2).trim();
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "CALL " + super.getProgram();
   }
   
 }
 
 
