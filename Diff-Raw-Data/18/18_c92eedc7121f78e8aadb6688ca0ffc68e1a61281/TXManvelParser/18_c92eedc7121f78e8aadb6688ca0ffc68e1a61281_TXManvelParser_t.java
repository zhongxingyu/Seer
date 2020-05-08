 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Manvel, TX (In Brazoria County)
 Contact: John Aucoin <johnaucoin@sbcglobal.net>
 Sender: dispatch@manvelpd.org
 
 [ NEW CALL ]  MVFD\nALFIRE\n3403 SOUTHFORK PARKWAY\nMANVEL\n1103200011 12:50 PM
 [ NEW CALL ]  MVFD\nACCUNK\nS. MASTER / JUST PAST GATES LOOP\nMANVEL\n1102210022 5:25 PM
 [ NEW CALL ]  MVFD\nFIRE\n13508 AUTUMN ASH CT\nMANVEL\n1103030017 3:36 PM
 [ NEW CALL ]  MVFD\nFIRE\n10516 LEEDY RD\nMANVEL\n1103010018 6:21 PM
 [ NEW CALL ]  MVFD\nALFIRE\n3602 BEHLER\nMANVEL\n1102230010 3:30 PM
 [ NEW CALL ]  MVFD\nFIRE\n815 CR 393\nMANVEL\n1102280014 2:47 PM
 [ NEW CALL ]  MVFD\nACCUNK\n288/6\nMANVEL\n1102220014 12:04 PM
 
NEW CALL / MVFD\nFIRE\n3110 EWING CT\nMANVEL\n1103270030 3:37 PM

 */
 
 
 public class TXManvelParser extends FieldProgramParser {
   
   private static final String[] CITY_LIST = new String[]{
     "MANVEL"
   };
 
   public TXManvelParser() {
     super(CITY_LIST, "MANVEL", "TX",
            "SRC CALL ADDR PLACE? CITY");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@manvelpd.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
    if (subject.equals("NEW CALL")) {}
    else if (body.startsWith("NEW CALL / ")) {
      body = body.substring(10).trim();
    } else {
      return false;
    }
    
     return parseFields(body.split("\n"), data);
   }
 
 }
