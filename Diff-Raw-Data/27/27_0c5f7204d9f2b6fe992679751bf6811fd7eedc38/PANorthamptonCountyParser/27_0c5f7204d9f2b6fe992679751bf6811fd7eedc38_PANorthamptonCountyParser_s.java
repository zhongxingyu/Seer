 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 
 
 public class PANorthamptonCountyParser extends DispatchBParser {
   
   private static final Pattern MARKER = Pattern.compile(">.* Cad: ");
   
   @Override
   public String getFilter() {
     return "@notifync.org,14100";
   }
  
   @Override
   protected boolean isPageMsg(String body) {
     return MARKER.matcher(body).find();
   }
 
   public PANorthamptonCountyParser() {
     super(CITY_LIST, "NORTHAMPTON COUNTY", "PA");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     data.strUnit = new Parser(subject).getLast('|');
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     body = body.replace(" CALL ", " AT ");
     if (!super.parseMsg(body, data)) return false;
     return true;
   }
   
   @Override
   public String getProgram() {
     return "UNIT " + super.getProgram();
   }
   
   private static final String[] CITY_LIST = new String[]{
     "BETHLEHEM",
     "EASTON",
 
     "BANGOR",
     "BATH",
     "CHAPMAN",
     "EAST ALLEN",
     "EAST BANGOR",
     "FREEMANSBURG",
     "GLENDON",
     "HELLERTOWN",
     "NAZARETH",
     "NORTH CATASAUQUA",
     "NORTHAMPTON",
     "PEN ARGYL",
     "PORTLAND",
     "ROSETO",
     "STOCKERTOWN",
     "TATAMY",
     "WALNUTPORT",
     "WEST EASTON",
     "WILSON",
     "WIND GAP",
 
     "ALLEN TWP",
     "BETHLEHEM TWP",
     "BUSHKILL TWP",
     "EAST ALLEN TWP",
     "FORKS TWP",
     "HANOVER TWP",
     "LEHIGH TWP",
     "LOWER MOUNT BETHEL TWP",
     "LOWER NAZARETH TWP",
     "LOWER SAUCON TWP",
     "MOORE TWP",
     "PALMER TWP",
     "PLAINFIELD TWP",
     "UPPER MOUNT BETHEL TWP",
     "UPPER NAZARETH TWP",
     "WASHINGTON TWP",
     "WILLIAMS TWP",
 
     "ALLEN",
     "BETHLEHEM",
     "BUSHKILL",
     "EAST ALLEN",
     "FORKS",
     "HANOVER",
     "LEHIGH",
     "LOWER MOUNT BETHEL",
     "LOWER NAZARETH",
     "LOWER SAUCON",
     "MOORE",
     "PALMER",
     "PLAINFIELD",
     "UPPER MOUNT BETHEL",
     "UPPER NAZARETH",
     "WASHINGTON",
     "WILLIAMS"
 
   };
 }
