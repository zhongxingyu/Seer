 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 /*
 Prineville, OR
 Reporting email: 5414603655@vzwpix.com
 Sender:dispatch@prinevillepd.org
 
 Neither of these maps, sloppy addresses
 (PPD) 6:01 PM\nNEW EVENT PFD\nMED1\n1201: 1271\n228 SW MEADOWLAKES DR\nPRINEVILLE
 (PPD) 3:17 PM\nNEW EVENT PFD\nMVA\n1201: 1273 / 1225\nLAMONTA/PIONEER CUTSTOCK\nPRINEVILLE
 */
 
 public class ORPrinevilleParser extends SmsMsgParser {
   
  public ORPrinevilleParser() {
     super("PRINEVILLE", "OR");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@prinevillepd.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (!body.startsWith("(PPD)")) return false;
     
     String[] lines = body.split("\n");
     if (lines.length <= 4) return false;
     data.strCall = lines[2].trim();
     parseAddress(lines[4].trim(), data);
     if (lines.length > 5) data.strCity = lines[5];
     return true;
   }
 }
