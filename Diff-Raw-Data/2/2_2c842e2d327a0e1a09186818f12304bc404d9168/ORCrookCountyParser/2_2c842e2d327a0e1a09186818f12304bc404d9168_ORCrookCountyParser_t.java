 package net.anei.cadpage.parsers.OR;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Prineville, OR
 Contact: 5414603655@vzwpix.com
 Sender:dispatch@prinevillepd.org
 
 [NEW INCIDENT]  INCIDENT NEW\n3/9/2011 1003\nEVENT # 1103090013 PFD\nTEST-FIRE - TEST INCIDENT\nPRIORITY 2 \nLOCATION 387 NE 3RD ST\nCITY PRINEVILLE\nAPT \nPREMISE
 [UNIT DISPATCH]  UNIT DISPATCH\n3/9/2011 1203\nUNITS 1291 \nEVENT # 1103090021 PFD\nFSMK - SMOKE UNKNOWN FIRE S...\nPRIORITY 1 \nLOCATION 1231 SE 5TH ST\nCITY PRI
 [NEW INCIDENT]  INCIDENT NEW\n3/9/2011 1203\nEVENT # 1103090021 PFD\nFSMK - SMOKE UNKNOWN FIRE S...\nPRIORITY 1 \nLOCATION 1231 SE 5TH ST\nCITY PRINEVILLE\nAPT \nP
 (NEW INCIDENT) INCIDENT NEW\n3/9/2011 1203\nEVENT # 1103090021 PFD\nFSMK - SMOKE UNKNOWN FIRE S...\nPRIORITY 1 \nLOCATION 1231 SE 5TH ST\nCITY PRINEVILLE\nAPT \nP
 (NEW INCIDENT) INCIDENT NEW\n3/9/2011 1203\nEVENT # 1103090021 PFD\nFSMK - SMOKE UNKNOWN FIRE S...\nPRIORITY 1 \nLOCATION 1231 SE 5TH ST\nCITY PRINEVILLE\nAPT \nP
 
 */
 
 public class ORCrookCountyParser extends FieldProgramParser {
   
   public ORCrookCountyParser() {
     super("CROOK COUNTY", "OR",
            "SKIP SKIP UNITS:UNIT EVENT:ID! CALL! PRIORITY:SKIP! LOCATION:ADDR! CITY:CITY! APT:APT");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@prinevillepd.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     if (!body.startsWith("INCIDENT NEW\n") && !body.startsWith("UNIT DISPATCH\n")) return false;
     
     body = body.replace("\nEVENT # ","\nEVENT:").replace("\nUNITS ","\nUNITS:")
                .replace("\nPRIORITY ","\nPRIORITY:").replace("\nLOCATION ","\nLOCATION:")
               .replace("\nCITY ","\nCITY:").replace("\nAPT \n","\nAPT:");
     return parseFields(body.split("\n"), data);
   }
 }
