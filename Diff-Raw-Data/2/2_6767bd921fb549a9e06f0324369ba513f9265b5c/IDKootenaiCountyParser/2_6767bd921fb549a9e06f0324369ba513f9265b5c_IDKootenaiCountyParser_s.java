 package net.anei.cadpage.parsers.ID;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Kootenai County, ID
 Contact: "pbarkle1@mac.com" <pbarkle1@mac.com>
 Contact: "Larry & Elaine Simms" <lesimms@gmail.com>
 Contact: Paul Barkley <PFB@mac.com>
 System: Spillman coupled through Hiplink
 Sender: Varies
 
 KOOTENAI COUNTY SHERIFF KCFR\nFIRE STRUCTURE \n1576 W TUALATIN DR\nF1A  \nF1V\nOPS1 \nVISIBLE FLAMES    IN GARAGE\n13:12:17 04/30/2011 - T.TAYLOR\nRES IS UNOCC\n13:12:50 04/30/2011 - T.TAYLOR\nVEH IN GARAGE    RP SAYS HER HOUSE IS GOING TO BLOW UP\n13:13:00 04/30/2011 - D.WILSON       \nFLAME\nSent by CLI  Sat Apr 30 13:16:15 2011\n0000 Confirm 0001 Refuse TXT STOP to opt-out
 HAUS\nACCIDENT INJURY\nN WATERFORD LOOP & N HAUSER LAKE RD\nGW7A \nB751\nOPS4 \nMC ACCIDENT    WENT OFF RDWY    CAB    POSS BROKEN COLLARS     CUTS ON FACE   \nCONTROLLED
 KCFR\nFIRE STRUCTURE \nN SMITH LN & E MULLAN AVE\nF1C  \nF1V\nOPS1 \nTRAILER FULLY ENGULFED ON SMITH RD JUST OFF MULLAN   FLAMES FROM WINDOWS AND\nROOF \n10:49:32 05/28/20
 
 */
 
 
 public class IDKootenaiCountyParser extends FieldProgramParser {
   
   public IDKootenaiCountyParser() {
     super("KOOTENAI COUNTY", "ID",
           "SRC CALL ADDR UNIT+? CH! INFO+");
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (body.startsWith("KOOTENAI COUNTY SHERIFF ")) {
      body = body.substring(25).trim();
     }
     int pt = body.indexOf("\nSent by CLI");
     if (pt >= 0) body = body.substring(0,pt).trim();
     
     String[] flds = body.split("\n");
     if (flds.length < 5) return false;
     return parseFields(flds, data);
   }
   
   // Radio channel must start with OPS
   private class MyChannelField extends ChannelField {
     public MyChannelField() {
       setPattern(Pattern.compile("OPS.*"));
     }
   }
   
   // INFO field skips anything starting with a date/time
   private static Pattern DATE_TIME_PTN = Pattern.compile("^\\d\\d:\\d\\d:\\d\\d: \\d\\d/\\d\\d/\\d\\d\\d\\d\\b");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (DATE_TIME_PTN.matcher(field).matches()) return;
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CH")) return new MyChannelField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }
