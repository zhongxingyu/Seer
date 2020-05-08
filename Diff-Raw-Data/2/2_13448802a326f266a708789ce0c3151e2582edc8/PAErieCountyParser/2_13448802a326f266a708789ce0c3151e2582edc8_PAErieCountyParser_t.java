 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.GroupBestParser;
 
 public class PAErieCountyParser extends GroupBestParser {
   
  public PAErieCountyParser() {
     super(new PAErieCountyAParser(), new PAErieCountyBParser());
   }
 }
