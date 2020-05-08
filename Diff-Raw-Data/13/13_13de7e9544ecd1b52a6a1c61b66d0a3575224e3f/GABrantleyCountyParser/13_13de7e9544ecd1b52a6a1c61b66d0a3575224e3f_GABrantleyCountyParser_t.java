 package net.anei.cadpage.parsers.GA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Brantley county, GA
 Contact: Travis Robinson <calvary420@gmail.com>
 Sender: brantleyga@smtp.sgcce-inc.com <From%3Abrantleyga@smtp.sgcce-inc.com>
 
 (Disp CVFD Case # 2011-2-92 Call # 22763) 2/2/2011 14:51:00 /1070 /52 /SAMANTHA LN /YARD FIRE / STRUCTURE IN DANGER
 [Disp CVFD Case # 2011-1-1473 Call # 22262]  1/25/2011 0:55:31 /1070 /9513 /HWY 121 /BRUSH BEHIND HUMPTEY DUMPTEY
 [Disp CVFD Case # 2011-1-1452 Call # 22241]  1/24/2011 17:06:14 /1070 /602 /WARE COUNTY LINE RD /WOODS FIRE
 [Disp CVFD Case # 2011-1-1317 Call # 22106]  1/22/2011 17:02:58 /1070 /2074 /MATTIE SHUMAN RD /CONTROL BURN
 [Disp CVFD Case # 2011-1-1260 Call # 22049]  1/21/2011 15:23:22 /1052 /5013 /WHITEHALL CHURCH RD /62 YOM SICK; HOME HEALTH NURSE IS AT THE RESIDENCE AN
 [Disp CVFD Case # 2011-1-841 Call # 21630]  1/15/2011 13:53:32 /1070 /132 /SADDLE CLUB RD /FIELD ON FIRE
 
Contact: Daniel Stone <dfstone93061@gmail.com>
Sender: brantleyga@ez911mail.com
Subject:Disp EMS1 Case # 2011-10-633 Call # 3967\n10/10/2011 07:44:22 /1050 /449 /EAGLES NEST RD /EAST BOUND / 1 MILE WEST OF BCHS - IN

 */
 public class GABrantleyCountyParser extends FieldProgramParser {
   
  private static final Pattern SUBJECT_PTN = Pattern.compile("Disp ([A-Z0-9]+) Case # [-0-9]+ Call # ([0-9]+)");
   
   public GABrantleyCountyParser() {
     super("BRANTLEY COUNTY", "GA",
            "SKIP CALL ADDR ADDR! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "brantleyga@smtp.sgcce-inc.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     Matcher match = SUBJECT_PTN.matcher(subject);
     if (!match.matches()) return false;
     data.strSource = match.group(1);
     data.strCallId = match.group(2);
     
     return parseFields(body.split(" /"), data);
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String fld, Data data) {
       data.strAddress = append(data.strAddress, " ", fld);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC ID " + super.getProgram();
   }
 }
