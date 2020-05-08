 package net.anei.cadpage.parsers.GA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Dougherty County, Albany Ga
 Contact: Darren Eubanks <medic7046@gmail.com>
 
 110126 152104110260350/01-26-11/15:20:15/ALTERED MENTAL STATE/1906 LULLWATER RD/ALBANY/FOREST GLEN DR/SUMMIT DR/E01/73YOF C/B/MED5/
 110126 191509110260454/01-26-11/19:12:59/PERSON ILL/1905 BARNESDALE WAY/ALBANY/ROBINHOOD RD/LADY MARION LN/E01/58YM C/B  LOTS OF PAIN,PAIN IN FEET/MED5/
 110126 125725110260273/01-26-11/12:57:03/STROKE/803 BEECHWOOD DR/ALBANY/WHITE OAK DR/VINE LN/E01/MED5/
 110127 054453110270051/01-27-11/05:44:19/CARDIAC ARREST/2509 FAIRFAX DR/ALBANY/HUTCHINSON LN/STAR LN/E01/84yof   Not Responding n Not Breathing/MED5 MED2/
 110127 042557110270043/01-27-11/04:25:49/ABDOMINAL PROBLEMS/3201 TROWBRIDGE RD/ALBANY/DEVON DR/BEATTIE RD/E01/MED5/
 110126 064823110260088/01-26-11/06:47:46/DIABETIC/2823 GILLIONVILLE RD/ALBANY/EVERGREEN ASSISTED LIVING/N WESTOVER BLV/NATIONAL CT/E01/98 YOF C/B  SUGAR IS  58/MED5/
 
 */
 public class GADoughertyCountyParser extends FieldProgramParser {
   
  private static final Pattern MARKER = Pattern.compile("^\\d{6} (\\d{15})/");
   
   public GADoughertyCountyParser() {
     super("DOUGHERTY COUNTY", "GA",
            "SKIP SKIP CALL ADDR CITY! PLACE? X X UNIT INFO+");
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end());
     data.strCallId = match.group(1);
     return parseFields(body.split("/"), data);
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
 }
