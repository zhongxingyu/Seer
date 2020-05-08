 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
 import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 /*
 Madison County (Alexandria), IN
 Contact: Pamela Stigall <pamstigall@gmail.com>
 Sender: Mplus@madisoncty.com
 Unit:ST40 UnitSts: Loc:6302 APOLLO DR Venue:RICHLA TWP Inc:Brush Fire Date:10/17/2010 Time:18:49 ON GEMINI / 1 ST HOUSE SOUTH EAST END Addtl:TRASH FIRE ON THE GROUND/WHI MODULAR WHI VAN SITTING IN DRIVE
 Unit:AM45 UnitSts: Loc:6197 N SR 9 Venue:RICHLA TWP Inc:EMS Call Date:10/17/2010 Time:01:12 56 YOM / ADV HE DOESNT FEEL GOOD / VOMITING A Addtl:ND NO APPITITE ALL DAY / WILL BE WATING OUTSI DE RESD:
 Unit:ST40 UnitSts: Loc:E 400N/100E Venue:RICHLA TWP Inc:Brush Fire Date:10/15/2010 Time:14:33 OPEN BURN BETWEEEN 100E AND ALEX PIKE/ S SIDE Addtl: OF ROAD
 Unit:ST40 UnitSts: Loc:3551 N SR 9 Venue:LAFAYE TWP TOM WOOD HONDA Inc:Accidnt PI Date:10/14/2010 Time:07:43 UNKNOWN 2 VEHS Addtl:
 */
 
 public class INMadisonCountyParser extends SmsMsgParser {
   
   private static final String[] keywords = new String[] {"Unit", "UnitSts", "Loc", "Venue", "Inc", "Date", "Addtl"};
                                                       
 
   @Override
   public boolean isPageMsg(String body) {
     return body.startsWith("Unit:");
   }
 
   @Override
   protected void parse(String body, Data data) {
     Log.v("DecodeMadisonCountyPage: Message Body of:" + body);
     data.defState="IN";
     data.defCity = "MADISON COUNTY";
     
     Properties props = parseMessage(body, keywords);
     body = body.trim();
     data.strUnit = props.getProperty("Unit", "");
    parseAddress(props.getProperty("Loc", ""), data);
     data.strCall = props.getProperty("Inc", "");
   }
 }
