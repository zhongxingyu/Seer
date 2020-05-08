 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 
 /*
 Wilkes County, NC
 Contact: wfdffps@gmail.com
 Sender: CAD@wilkes.nc.local
 
 FRM:CAD@wilkes.nc.local\nMSG:CAD:WCFD;741;05/09/2011 14:38:38;UNRESPONSIVE;100 WINDING TRAILS PL
 FRM:CAD@wilkes.nc.local\nMSG:CAD:WCFD;727;05/09/2011 13:03:25;UNRESPONSIVE;1016 FLETCHER ST
 FRM:CAD@wilkes.nc.localf\nMSG:CAD:WCFD;12;05/06/2011 04:02:34;FIRE ALARM;1605 CURTIS BRIDGE RD
 FRM:CAD@wilkes.nc.local\nMSG:CAD:WCFD;702;05/04/2011 14:53:07;ACCIDENT W/INJURY;1500 RIVER ST

Contact: Craig Hollar <spanky4142@gmail.com>
Sender: 93001046
CAD:WCFD;396;07/07/2012 01:47:54;HEART ATTACK;298 DRAGWAY RD

 */
 
 public class NCWilkesCountyParser extends DispatchOSSIParser {
   
   public NCWilkesCountyParser() {
     super("WILKES COUNTY", "NC",
           "SRC ID DATETIME CALL ADDR!");
   }
   
   @Override
   public String getFilter() {
     return "CAD@wilkes.nc.local";
   }
 }
