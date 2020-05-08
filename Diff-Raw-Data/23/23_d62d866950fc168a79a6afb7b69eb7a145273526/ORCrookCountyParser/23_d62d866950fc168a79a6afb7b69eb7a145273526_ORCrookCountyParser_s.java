 package net.anei.cadpage.parsers.OR;
 
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 public class ORCrookCountyParser extends FieldProgramParser {
   
   public ORCrookCountyParser() {
     super("CROOK COUNTY", "OR",
            "SKIP EVENT:ID! CALL! PRIORITY:SKIP! LOCATION:ADDR! CITY:CITY APT:APT PREMISE:PLACE COMMENT:INFO");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@prinevillepd.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {

    if (!subject.contains("NEW INCIDENT")) return false;
    
     body = body.replace("\nEVENT # ","\nEVENT:")
                .replace("\nPRIORITY ","\nPRIORITY:").replace("\nLOCATION ","\nLOCATION:")
                .replace("\nCITY ","\nCITY:").replace("\nAPT ","\nAPT:");
     return parseFields(body.split("\n"), data);
   }
 }
