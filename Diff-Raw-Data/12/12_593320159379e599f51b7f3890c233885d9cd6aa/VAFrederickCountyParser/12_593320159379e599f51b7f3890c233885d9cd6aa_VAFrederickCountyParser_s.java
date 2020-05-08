 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Frederick County, VA
 Contact: Timothy Vaught <tvaught5388@gmail.com>
 Sender: CAD@co.frederick.va.us
 CAD:107 HICKORY HILL DR;GENERAL ILL-NEW ONSET IMMOBILE;LAUREL HILL DR;LAKESIDE DR
 CAD:458 HITES RD;UNCON/FAINT-UNCON/BREATHING;CARSON LN;GEM DR
 CAD:2044-79 FAIRFAX PIKE;PREGNANCY-LABOR;APPRENTICE LN
 CAD:250 COSTELLO DR;FILL STATION;FRONT ROYAL PIKE;PRINCE FREDERICK DR
 CAD:3632 VALLEY PIKE;FALL - POSS DANGEROUS;COMMONWEALTH CT;FAY ST
 
 Contact: William Pier <piferfcf@gmail.com>
 Sender: CAD@psb.net
 CAD:285 PARK CENTER DR;LARCENY GENERAL;FIRE ALARM ZONE 7 PLANT PULL STATION [06/19/11 11:09:10 SFULTZ] ;W BROOKE RD
 
  */
 public class VAFrederickCountyParser extends DispatchOSSIParser {
   
   public VAFrederickCountyParser() {
     super("FREDERICK COUNTY", "VA",
            "ADDR CALL! X? X? INFO+");
   }
   
   @Override
   public String getFilter() {
     return "CAD@co.frederick.va.us,CAD@psb.net";
   }
 }
