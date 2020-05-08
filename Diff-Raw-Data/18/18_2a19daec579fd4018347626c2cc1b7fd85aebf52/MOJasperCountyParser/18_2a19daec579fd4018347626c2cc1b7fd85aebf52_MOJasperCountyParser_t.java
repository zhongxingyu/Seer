 package net.anei.cadpage.parsers.MO;
 
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /**
  * Jasper County, MO
  */
 public class MOJasperCountyParser extends DispatchOSSIParser {
  
   public MOJasperCountyParser() {
     super(CITY_CODES, "JASPER COUNTY", "MO",
            "FYI CALL PLACE? ADDR/Z CITY X/Z+? CODE! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "CAD@jasco.org";
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CODE")) return new CodeField("\\d\\d[A-Z]", true);
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CJCT", "CARL JUNCTION",
      "JPLN", "JOPLIN",
       "WBCT", "WEB CITY",
       
       "CNTY", ""
   });
 }
