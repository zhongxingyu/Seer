 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /* Accomack Test Pages
 Accomack County, VA - (class I)
 Contact: niblett25@gmail.com
 Sender: cad@esva911.org
 Field 3 is unknown
 Field 4-5 make up a map code
 Field 6-7 are cross streets
 CAD:STROKE;6225 HOLLAND RD;NEWC;A25;A6;DEBRA LN;R J DR
 CAD:ACCIDENT WITH INJURY;12432 LANKFORD HWY;HALL;A59;B6;FOX DR;JOHN TAYLOR RD
 CAD:ABDOMINAL PAIN;15361 LANKFORD HWY;MAPP;A68;D4;GILLESPIE LN;SHERWOOD DR;MILE MARKER 125
  */
 
 public class VAAccomackCountyParser extends SmsMsgParser {
 
     @Override
     public boolean isPageMsg(String body) {
       return body.startsWith("CAD:");
     }
 
   @Override
   protected void parse(String body, Data data) {
 
     Log.v("DecodeAccomackPage: Message Body of:" + body);
     data.defState="VA";
     data.defCity="ACCOMACK COUNTY";
 
      String[] lines = body.substring(4).split(";");
      if (lines.length > 1) data.strCall = lines[0];
      if (lines.length > 3) parseAddress(lines[1], data);
      if (lines.length <= 3) return;
      data.strMap = " " + lines[3] + ", " + lines[4];
      if (lines.length <= 4) return;
      if (lines[5].length() < 0) return;
      if (lines[6].length() < 0) 
        {
        data.strCross = lines[5];
        }
      else
        data.strCross = lines[5]+ " & " + lines[6];
    
   }
 }
