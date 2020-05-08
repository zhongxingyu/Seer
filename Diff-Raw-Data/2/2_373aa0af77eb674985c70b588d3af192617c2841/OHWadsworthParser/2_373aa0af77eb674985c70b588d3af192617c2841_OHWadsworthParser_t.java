 package net.anei.cadpage.parsers.OH;
 
 
 public class OHWadsworthParser extends OHSummitCountyParser {
   
   public OHWadsworthParser() {
    super("WADSWORTH", "OH");
   }
   
   @Override
   public String getFilter() {
     return "info@sundance-sys.com";
   }
 }
