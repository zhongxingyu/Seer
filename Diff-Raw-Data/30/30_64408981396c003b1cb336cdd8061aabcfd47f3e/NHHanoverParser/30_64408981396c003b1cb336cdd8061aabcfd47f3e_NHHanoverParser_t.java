 package net.anei.cadpage.parsers.NH;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Hanover, NH (in Grafton County)
 Contact: "Daniel A. Perry (Student)" <DAP634@students.jwu.edu>
Contact: Zachary Lang <zacharymichaellang@gmail.com>,8024613731@vtext.com
 Sender: DISPATCH@HANOVER.PD
 
 (_) MEDU 72 Y M FALLEN\nHUNTINGTON APARTMENTS\n493 S MAIN ST \nBRADFORD\n2/9/2012 18:54
 (_) FD- STILL MV FIRE\nMAIN ST \nBRADFORD\n12/4/2011 19:19
 (_) ACCIDENT (WITH INJURY)\nON RAMP\nI-91 S\nBRADFORD\n12/5/2011 06:40
 (_) AMBULANCE ASSIST\n33 S MAIN ST APT 4\nBRADFORD\n11/25/2011 23:53
 (_) FDS CO DETECTOR ACTIVATION\n5003 SOUTH RD\nBRADFORD\n10/30/2011 19:24
 (_) FDS FUEL SPILL\n286 WAITS RIVER RD\nBRADFORD\n10/24/2011 09:05
 (_) FIRE CALL - FIRE ALARM\n378 N MAIN ST\nBRADFORD\n10/22/2011 16:09
 (_) FDS POWER LINES DOWN\n1025 GOSHEN RD\nBRADFORD\n10/15/2011 16:26
 (_) FIRE CALL - FIRE ALARM\n378 N MAIN ST\nBRADFORD\n10/13/2011 21:18
 [_]  MEDU - DIFF BREATHING\nOXBOW HIGH SCHOOL\n36 OXBOW DR\nBRADFORD\n1/30/2012 09:59
 [_]  MEDU\nVALLEY VISTA\n23 UPPER PLAIN\nBRADFORD\n1/30/2012 13:19
 [_]  MEDU- 57 YO M DIFF BREATHING\n101 ERWIN RD\nBRADFORD\n1/30/2012 20:48
 [_]  FD- STILL\n1176 FAIRGROUND RD\nBRADFORD\n2/2/2012 11:01
 [_]  MEDU 80 YO MALE FEELING ILL\n613 UPPER PLAIN\nBRADFORD\n2/2/2012 18:37
 [_]  MEDU 30 Y M MENTAL HEALTH PROB\n245 S MAIN ST APT 5\nBRADFORD\n2/2/2012 21:44
 [_]  MEDU 56 Y M FALLEN BACK INJ\nCLEAR MEADOW LN\nBRADFORD\n2/3/2012 14:47
 [_]  MEDU\nBRADFORD MINI MART\n530 WAIT'S RIVER RD\nBRADFORD\n2/4/2012 19:33
 [_]  MEDU76 F LETHARGIC CHEST PAIN\n1022 LOWER PLAIN\nBRADFORD\n2/5/2012 09:25
 [_]  MEDU - 87 F LOW O2 LEVELS\nGILBERT RESD - FARMERS EXCHG BLDG\n245 S MAIN ST APT 2\nBRADFORD\n2/6/2012 16:53
 (_)  MEDU 72 Y M FALLEN\nHUNTINGTON APARTMENTS\n493 S MAIN ST\nBRADFORD\n2/9/2012 18:54
 
(_) MUTUAL AID FIRE/AMB CALL\nHAVERHILL CORNER STATION\nHAVERHILL\nWITH ENGINE AND TANKER FOR COVERAGE\n6/10/2012 17:05

 */
 
 
 public class NHHanoverParser extends FieldProgramParser {
   
   public NHHanoverParser() {
    super(CITY_LIST, "HANOVER","NH",
           "CALL PLACE? ADDR/Z CITY! INFO? DATETIME/Z! END");
   }
   
   @Override
   public String getFilter() {
     return "DISPATCH@HANOVER.PD";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("_")) return false;
     String[] flds = body.split("\n");
    if (flds.length < 4 || flds.length > 6) return false;
    if (!parseFields(flds, data)) return false;
    if (data.strCity.equals("BRADFORD")) data.defState = "VT";
    return true;
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("DATETIME")) return new DateTimeField("\\d{1,2}/\\d{1,2}/\\d{4} \\d\\d:\\d\\d", true);
     return super.getField(name);
   }
  
  private static final String[] CITY_LIST = new String[]{
    "BRADFORD",
    "HANOVER",
    "HAVERHILL"
  };
 }
