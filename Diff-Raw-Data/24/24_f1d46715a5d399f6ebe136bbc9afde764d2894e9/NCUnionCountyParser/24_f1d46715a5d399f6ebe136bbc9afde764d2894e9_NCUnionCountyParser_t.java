 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Union County, NC
 Contact: David Bruce <dbruce0571@gmail.com>
Sender: cad@uc.co.union.nc.us
 
 CAD:3711 WHITE STORE RD;WINGATE;OUTSIDE FIRE;S09;WITMORE RD;NASH RD;02/15/2011 13:21:38
 
 */
 
 public class NCUnionCountyParser extends DispatchOSSIParser {
   
   public NCUnionCountyParser() {
     super("UNION COUNTY", "NC",
            "ADDR CITY CALL SRC! X X PLACE");
   }
   
   @Override
   public String getFilter() {
    return "cad@uc.co.union.nc.us";
   }
 }
