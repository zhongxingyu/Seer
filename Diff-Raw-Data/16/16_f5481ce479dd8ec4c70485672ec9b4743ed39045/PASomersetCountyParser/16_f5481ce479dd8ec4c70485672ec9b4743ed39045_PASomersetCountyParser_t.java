 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Somerset County, PA
 Contact: "Kevin" <Pastorkbroadwater@verizon.net>
 Sender: alert@ecm2.com
 
 Code:  MVA   >MOTOR VEHICLE ACCIDENT\nCode Detail:  Address:  XS:  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #:  2011-0000029533\n\nUnformatted Message: 9-1-1 CENTER:GRANT ST AT MAIN ST MEYERSDALE RICH SECHLER 8142891447 Cad: 2011-0000029533
 Code:  FIRES >STRUCTURE FIRE\nCode Detail:  Address: 9-1-1 CENTER:605 PINEY RUN RD  XS:  ENGLES MILL RD SALISBURY WEIMER SUSAN  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #: 2011-0000028647\n\nUnformatted Message: 9-1-1 CENTER:605 PINEY RUN RD XS: ENGLES MILL RD SALISBURY WEIMER SUSAN 8146624073 Map: Grids:, Cad: 2011-0000028647
 Code:  FIRES >STRUCTURE FIRE\nCode Detail:  Address: 9-1-1 CENTER:5467 MOUNT DAVIS RD  XS:  SAINT PAUL RD MEYERSDALE BENDER LINDA  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #: 2011-0000030088\n\nUnformatted Message: 9-1-1 CENTER:5467 MOUNT DAVIS RD XS: SAINT PAUL RD MEYERSDALE BENDER LINDA 8146622352 Map: Grids:, Cad: 2011-0000030088
 Code:  SPILL >SPILL (TYPE)\nCode Detail:  Address: 9-1-1 CENTER:1041 SHAW MINES RD  XS:  City:  Caller: Caller Ph:  Map:  Grid: CAD INC #:  2011-0000029694\n\nUnformatted Message: 9-1-1 CENTER:1041 SHAW MINES RD XS: SKYLINE DR MEYERSDALE 130 Map: Grids:, Cad: 2011-0000029694
 Code:  TC    >TRAFFIC CONTROL\nCode Detail:  Address: 9-1-1 CENTER:1222 GLADE CITY RD  XS:  DEAL RD MEYERSDALE LOTTIG,C  City:  Caller:  Caller Ph:  Map:  Grid: CAD INC #: 2011-0000030890\n\nUnformatted Message: 9-1-1 CENTER:1222 GLADE CITY RD XS: DEAL RD MEYERSDALE LOTTIG,C 8146348503 Map: Grids:, Cad: 2011-0000030890
 
 Contact: Paul Bomboy <training@easthillsems.com>
 Sender: 911CENTER@co.somerset.pa.us
 1 of 2\nFRM:911CENTER@co.somerset.pa.us\nSUBJ:FALLB >FALL INJURY BLS\nMSG:911CENTER:969 BARN ST Apt: 101 Bldg XS: HIGHLAND AVE HOOVERSVILLE STANKO,\n(Con't) 2 of 2\nGEORGIANA 8147987022 Map: Grids:, Cad: 2012-0000017811\r\n\r\n(End)
 
 Contact: Marc Jackson <marcmjackson@gmail.com>
 [FIRES >STRUCTURE FIRE]  911CENTER:MAIN ST BOSWELL LEWIS,NANCY 8146299728 Cad: 2012-0000017870
 
Contact: "Samuel Dively" <8142794645@vzwpix.com>
(FIREB >FIRE BRUSH) 911CENTER:TOOLAND RD XS: COMPS RD FAIRHOPE BUD CLAYTON/ CSX RR 8002320144 Map: Grids:, Cad: 2012-0000019181

 Contact: Active911
 Agency name: Hooversville EMS Location: Hooversville, PA
 Sender:911CENTER@co.somerset.pa.us 
 (BREATH>BREATHING PROBLEMS/DIFFICULTY) 911CENTER:148 Y ST XS: WILBUR RD STOYSTOWN OLSEN,LINDA 8148936267 Map: Grids:, Cad: 2012-0000017210
 (ILLB  >ILL PERSON BLS) 911CENTER:492 WILBUR RD XS: A ST STOYSTOWN ED BARTA 8147986054 Map: Grids:, Cad: 2012-0000017365
 (INJURB>INJURED PERSON BLS) 911CENTER:1414 PLANK RD XS: BLOOM RD HOOVERSVILLE CAMP HARMONY 8147985885 Map: Grids:, Cad: 2012-0000017394
 (FALLB >FALL INJURY BLS) 911CENTER:969 BARN ST Apt: 101 Bldg XS: HIGHLAND AVE HOOVERSVILLE STANKO, GEORGIANA 8147987022 Map: Grids:, Cad: 2012-0000017811
 
 */
 
 public class PASomersetCountyParser extends DispatchBParser {
   
   private static final Pattern MASTER = Pattern.compile("Code: ([^\n]*)\nCode Detail: .*\n\nUnformatted Message: 9-1-1 CENTER:(.*)");
 
   public PASomersetCountyParser() {
     super(CITY_LIST, "SOMERSET COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "alert@ecm2.com,911CENTER@co.somerset.pa.us";
   }
   
   @Override
   protected boolean isPageMsg(String body) {
     return true;
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     do {
       Matcher match = MASTER.matcher(body);
       if (match.matches()) {
         body = match.group(1) + " @ " + match.group(2);
         break;
       }
       
       if (body.startsWith("911CENTER:") && subject.length() > 0) {
         body = subject + " " + body.substring(10);
         break;
       }
       return false;
     } while (false);
     
     body = body.replace(" AT ", " & ");
     return super.parseMsg(body, data);
   }
   
   private static final String[] CITY_LIST = new String[]{
     // Boroughs
     "ADDISON",
     "BENSON",
     "BERLIN",
     "BOSWELL",
     "CALLIMONT",
     "CASSELMAN",
     "CENTRAL CITY",
     "CONFLUENCE",
     "GARRETT",
     "HOOVERSVILLE",
     "INDIAN LAKE",
     "JENNERSTOWN",
     "MEYERSDALE",
     "NEW BALTIMORE",
     "NEW CENTERVILLE",
     "PAINT",
     "ROCKWOOD",
     "SALISBURY",
     "SEVEN SPRINGS",
     "SHANKSVILLE",
     "SOMERSET",
     "STOYSTOWN",
     "URSINA",
     "WELLERSBURG",
     "WINDBER",
     // Townships
     "ADDISON TWP",
     "ALLEGHENY TWP",
     "BLACK TWP",
     "BROTHERSVALLEY TWP",
     "CONEMAUGH TWP",
     "ELK LICK TWP",
     "FAIRHOPE TWP",
     "GREENVILLE TWP",
     "JEFFERSON TWP",
     "JENNER TWP",
     "LARIMER TWP",
     "LINCOLN TWP",
     "LOWER TURKEYFOOT TWP",
     "MIDDLECREEK TWP",
     "MILFORD TWP",
     "NORTHAMPTON TWP",
     "OGLE TWP",
     "PAINT TWP",
     "QUEMAHONING TWP",
     "SHADE TWP",
     "SOMERSET TWP",
     "SOUTHAMPTON TWP",
     "STONYCREEK TWP",
     "SUMMIT TWP",
     "UPPER TURKEYFOOT TWP",
     // Census designated places
     "DAVIDSVILLE",
     "FRIEDENS",
     "JEROME",
     // Unincorporated communities
     "JENNERS",
     "SPRINGS",
   };
 }
