 package net.anei.cadpage.parsers.KS;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Johnson County, KS
 Contact: Mike Morse <michaeltmorse@gmail.com>
 Sender: 93001xxx    (email) ecc1@jocogov.org<mailto:ecc1@jocogov.org>
 
 Add: 2908 W 48th Ter Apt: Loc: Nature: MEDICAL Grid: 19/1. Incident# 10-3560129 Cross:Dead End/MISSION RD
 Add: 5055 Buena Vista St Apt: Loc: Nature: Bleeding - Laceration Grid: 18/1. Incident# 10-3560040 Cross:W 51ST ST/SYCAMORE DR
 Add: 2435 Drury Ln Apt: Loc: Nature: Auto Alarm - Residence Grid: 34/1. Incident# 10-3560070 Cross:OAKWOOD RD/OVERHILL RD
 Add: 6028 Mission Rd Apt: Loc: Nature: MEDICAL Grid: 34/1. Incident# 10-3560106 Cross:EASTVALE DR/W 61ST ST
 Add: 4817 Skyline Dr Apt: Loc: Nature: Building Fire Grid: 18/1. Incident# 10-3620163 Cross:W 48TH ST/Dead End
 Add: Broadmoor St & Johnson Dr Apt: Loc: Nature: Injury Accident Grid: 32/1. Incident# 10-3640058 Cross:
 Add: Shawnee Mission Pkwy & Nal Apt: Loc: Nature: Injury Accident C2 Grid: 32/1. Incident# 10-3640048 Cross:
 
 Contact: Mike Shaw <mshaw1957@gmail.com>
 Sender: calls+bncCAAQ1K3H7wQaBJUY9Sk@jocofd1.org
 (info:) Add: Gardner West Rd & Lake Roa Apt:            Loc:                 Nature: Investigate Power Lines Down   Grid: 290/1 Incident# 11-1
 
 
 */
 public class KSJohnsonCountyParser extends FieldProgramParser {
  
  private static final String DEF_CITY = "JOHNSON COUNTY";
  private static final String DEF_STATE = "KS";
 
   public KSJohnsonCountyParser() {
    super(DEF_CITY, DEF_STATE,
           "Add:ADDR! Apt:APT Loc:SKIP Nature:CALL! Grid:MAP! Incident:ID Cross:X");
   }
   
   @Override
   public String getFilter() {
    return "93001,ecc1@jocogov.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     body = body.replaceAll("Incident#", "Incident:");
     return super.parseMsg(body, data);
   }
 }
