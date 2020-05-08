 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Armstrong County, PA
 Contact: larry umbaugh <lumbaugh17@gmail.com>
 Sender: 911dispatch@co.armstrong.pa.us
 
 473 TROY HILL RD AIRPORT RD / MCGREGOR RD FMISC 39992 05:40 7245453398
 11878 STATE ROUTE 85 ARMSDALE RD / CRYTZER RD ORPHANS OF THE STORM VAENT 40313 19:09 7247120225
 141 ADAMS RD STATE ROUTE 85 / BALLOCK LN FMISC 40720 09:07 7247837418
 [Dispatch]  580 N CHERRY ST HOOKS LN / SPRUCE LN FSTR2 41628 13:58 7246640045
 [Dispatch]  868 STATE ROUTE 28/66 MECHLING RD / SLOAN HILL RD VAENT 42068 15:55 7248597657
 
 Armstrong County, PA
 Contact: brandon kilgore <firefghter10@gmail.com>
 Sender: 911Dispatch@co.armstrong.pa.us
 (Dispatch) FREEPORT RD, US ROUTE 422 / PONY FARM RD, US ROUTE 422 VAENT 3706 06:42
 
 Contact: Jacob Dively <jpdively@gmail.com>
 Sender: 911Dispatch@co.armstrong.pa.us
 (Dispatch) 105 ELM DR MAPLE LN / LOCUST LN FCOALM 4524 14:11 8003643616
 (Dispatch) 571 EAST BRADY RD ROLLING HILLS RD / CORNMAN DR VAINJ 4360 01:41 7245451228
 
 */
 
 public class PAArmstrongCountyParser extends SmartAddressParser {
 
   // Marker is time and run number at end of message
   private static final Pattern MARKER_PATTERN = Pattern.compile(" +(\\d{1,6}) (\\d\\d:\\d\\d)(?: (\\d{10}))? *$");
   
   public PAArmstrongCountyParser() {
     super("ARMSTRONG COUNTY", "PA");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = MARKER_PATTERN.matcher(body);
     if (! match.find()) return false;
     
     data.strCallId = match.group(1);
     data.strTime = match.group(2);
     data.strPhone = getOptGroup(match.group(3));
     body = body.substring(0,match.start());
     
     Parser p = new Parser(body);
     data.strCall = p.getLast(' ');
     body = p.get();
     
     
     // We need to call the smart parser twice, once for the real address
     // and a second time to get the cross streets (which look like intersections)
     
     parseAddress(StartType.START_ADDR, body, data);
     body = getLeft();
     do {
       if (body.startsWith("/")) body = body.substring(1).trim();
       Result result2 = parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS, body);
       if (result2.getStatus() > 0) {
         result2.getData(data);
         body = getLeft();
       }
     } while (body.startsWith("/"));
     
     data.strPlace = body;
 
     return true;
   }
 }
