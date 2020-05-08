 package net.anei.cadpage.parsers.OH;
 
 import net.anei.cadpage.parsers.dispatch.DispatchA1Parser;
 
 /*
 Deleware County, OH
 Contact: ryan kirby <ryankirby950@gmail.com>
 Sender: 911@co.delaware.oh.us
 
 (Alert: Service Run-Fire) ALRM LVL: 1\nLOC:\n210 BIG RUN RD\nBERLIN TWP\nBTWN: STATE ROUTE 37 E & SOUTH END\nRCVD AS Radio\n\nCOM:\n\nCT:\n9961 at POS 05
 (Alert: Unresponsive / Unconscious) ALRM LVL: 1\nLOC:\n4114 CURVE RD\nBERLIN TWP\nBTWN: WINDING CREEK LN & LACKEY OLD STATE RD\nRCVD AS E-911 Call\nCOM:\nPROQA Urg
 (Alert: Gas Leak-Residential) ALRM LVL: 1\nLOC:\n210 BIG RUN RD\nBERLIN TWP\nBTWN: STATE ROUTE 37 E & SOUTH END\n\nCOM:\nPROQA Urgent Message: basement\nPROQA Urgen
 
 (Alert: Fire-Vehicle) ALRM LVL: 1\nLOC:\nALUM CREEK BIG RUN PARKING LOT NORTH (UPPER)\n606 BIG RUN RD\nBERLIN TWP\nBTWN: STATE ROUTE 37 E & SOUTH END\n\nCOM:\nVEHIC
 (Alert: Auto Accident Injuries) ALRM LVL: 1\nLOC:\nSHANAHAN RD/COLUMBUS PIKE\nBERLIN TWP\nBTWN: COLUMBUS PIKE & NORTH RD IN ORANGE TWP\n\nRCVD AS Cellular 911\nCOM:

  */
 
 
 public class OHDelawareCountyParser extends DispatchA1Parser {
   
   public OHDelawareCountyParser() {
     super("DELAWARE COUNTY", "OH");
   }
   
   @Override
   public String getFilter() {
     return "911@co.delaware.oh.us";
   }
 }
