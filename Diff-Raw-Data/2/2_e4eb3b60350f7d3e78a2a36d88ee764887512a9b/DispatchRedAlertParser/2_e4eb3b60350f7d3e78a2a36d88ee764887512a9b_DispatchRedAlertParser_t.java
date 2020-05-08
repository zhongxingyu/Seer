 package net.anei.cadpage.parsers.dispatch;
 
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 Red Alert CAD software parser
 
 Holbrook FD, NY
 Contact: "bfdtecresq@aol.com" <bfdtecresq@aol.com>
 MOTOR VEHICLE ACCIDENT . . MVA 29D4 at intersection of GREENBELT PKWY and PATCHOGUE-HOLBROOK RD. . 10:48:57
 WIRES . . POWER CONDENSER FIRE at intersection of PATCHOGUE-HOLBROOK RD and FURROWS RD. . 07:14:36
 WORKING STRUCTURE FIRE at 223 SPRINGMEADOW DR. . 16:22:21
 AMBULANCE CALL . . E/M RESP 6D2 at 76 LINCOLN AVE. . 17:48:14
 AMBULANCE CALL: 63 YOF RESP. 6-D-2 at 1585 CLAAS AVE, Holbrook . . 20:49:28
 CARBON MONOXIDE ALARM: ALARM GOING OFF - NEG SYMPTOMS at 53 TIMBER RIDGE DR, Holbrook . . 22:46:56
 FUEL SPILL: SHERIFFS ON SCENE REQUESTING at E/B SUNRISE HWY SRVC / BROADWAY AVE, HOLBROOK . . 23:06:50
 CARBON MONOXIDE ALARM: CO ALARM at 53 TIMBER RIDGE DR, HOLBROOK  c/s: SPRINGMEADOW DR . . 00:15:25
 MUTUAL AID - STRUCTURE FIRE at 62 POTOMAC  c/s: GRUNDY . . 15:08:02
 MOTOR VEHICLE ACCIDENT at LINCOLN AVE / VETERANS MEMORIAL HWY, HOLBROOK . . 12:16:23
 MISC: UNKNOWN TYPE FIRE - SOUTH END OF MUSKET DR at MUSKET DR / CANNON DR, HOLBROOK . . 20:58:10
 MUTUAL AID - STRUCTURE FIRE: 1 LADDER TO THE SCENE at 456 WAVERLY AVE  c/s: BUCKLEY RD . . 13:56:21
 AMBULANCE CALL: E/F FALL 17B1  at 229 SPRINGMEADOW DR #G, HOLBROOK  c/s: BEECH LN . . 08:39:55
 VEHICLE FIRE: CAR FIRE - MULTIPLE CALLS at 24 ANNANDALE ROAD  c/s: INVERNESS ROAD . . 06:07:57
 Contact: "bfdtecresq@aol.com" <bfdtecresq@aol.com>
 HazMat 10 is OOS, HazMat 11 relocated to Fire Rescue. TFN
 Hazmat: gas pumps knocked over at 125 Crooked hill road, Brentwood  c/s: Wicks road   O: 7-11 store . . 11:29:34
 need a team leader to respond to brentwood fire for  gas pump knocked over at 7-11 store at 125 crooked hill road
 
 Farmingdale, NY
 Contact: Frank Romano <fromano129@gmail.com>
 Signal 9 . . Stroke (CVA) at 64 DOUD ST. . 11:31:21
 General Alarm . . SMOKE ODOR IN AREA at 36 WAVERLY PL. . 17:04:46
 House Fire . . Electrical Fire at 23 BEECHWOOD ST. . 18:24:03
 Motor Vehicle Accident . . With Aided at intersection of MAIN ST and FULTON ST. . 11:46:09
 Signal 9 . . Cardiac / Respiratory Arrest at 610 CONKLIN ST. . 06:20:05
 
 Setauket, NY
 AMBULANCE, CONVULSIONS / SEIZURES: 12-D-2 44 YOF SEIZURES at 43 NEAL PATH, SETAUKET   O: FAIRFIELD GABLES            TRUSS . . 19:39:51
 AMBULANCE, SICK PERSON (SPECIFIC DIAGNOSIS): 56 YOF SICK 26-A-5 IFO BUILDING at 5000 ROUTE 347, East Setauket  c/s: ARROWHEAD LN SOUTH   O: KOHLS . . 14:54:20
 MISC CALL, CHIEFS INVESTIGATION: GENERAL FIRE ALARM 52C04 Residential (multiple) at 700H HEALTH SCIENCES DR, STONY BROOK  c/s: NICOLLS RD   O: CHAPIN APTS - S: 26-A-16 at 20 FAWN LN W, SOUTH SETAUKET  c/s: LONGBOW LN . . 00:20:06
 AMBULANCE, HEMORRHAGE / LACERATIONS: 21-A-1 - 61 YOM - CUT TO HAND at 8 HOLLY LN, EAST SETAUKET  c/s: CRANE NECK RD . . 16:57:21
 MISC CALL WITH RESCUE, MVA: 29-B-1U - INJURIES at C/O, Setauket  c/s: WIRELESS RD . . 16:08:02
 
 Rocky Point, NY
 Contact: Jonathan broder <redorbj@gmail.com>
 Falls, Not dangerous body area at 6 CROWN RD, ROCKY POINT  c/s: FISH RD . . 08:00:02
 Automatic Alarm, Residential at 38 CHERYL DR, SHOREHAM  c/s: COBBLESTONE DR . . 08:23:03
 Breathing Problems, Abnormal breathing at 151 BROADWAY, ROCKY POINT  c/s: CLUBHOUSE DR . . 12:42:23
 Breathing Problems, Difficulty Speaking Between Breaths at 6 CROWN RD, ROCKY POINT  c/s: FISH RD . . 16:09:05
 Cardiac or Respiratory Arrest, Not breathing at all at 258 GLEN DR, SHOREHAM  c/s: LEISURE DR . . 14:31:40
 
 Southhampton Village, NY
 Contact: Chris Mckay <ccmckay18@gmail.com>
 ALARM - GENERAL at 64 COUNTY RD 39, SOUTHAMPTON  c/s: NORTH SEA RD   O: HAMPTONS CENTER FOR REHAB . . 11:09:34
 STRUCTURE FIRE, RESIDENTIAL at 133 North Magee St, Southampton  c/s: Sebonic Inlet Rd . . 16:42:18
 MISC ALARM, GAS LEAK at 271 Gin . . 09:24:18
 ALARM - GENERAL at 151 WINDMILL LN, SOUTHAMPTON  c/s: JAGGER LN   O: SVPD HQ . . 12:02:10
 STRUCTURE FIRE, RESIDENTIAL at HILL ST / CAPTAINS NECK LN, SOUTHAMPTON . . 15:36:34
 
 Belmore, NY (Actually in neighboring Nassau county)
 Contact: Timothy Beuth <tbeuth@gmail.com>
 RESCUE at 2438 BEDFORD PLACE. . 11:34:04
 AUTO ACCIDENT at intersection of BELTAGH AVENUE and BELLMORE AVENUE. . 15:03:39
 AUTO ACCIDENT at 2971 LEE PLACE. . 04:08:29
 MUTUAL AID RESCUE at 3015 CHERYL ROAD. . 19:12:04
 AUTOMATIC ALARM BUILDING . . UNREGISTERED ALARM at 609 SUNRISE HIGHWAY. . 11:46:13
 HAZMAT - NATURAL GAS INTERIOR . . ODOR INSIDE at 1 SUNRISE HIGHWAY. . 05:07:37
 HOUSE FIRE . . SMOKE IN THE HOUSE at 2764 MARTIN AVENUE. . 17:09:53
 6011 IS OUT OF SERVICE
 6011 IS BACK IN SERVICE 
 
 Levittown Fire Department, Nassau County, NY (Also in Nassau County)
 Contact: Adam Freed <afreed00789@gmail.com>
 Sender: paging@rednmxcad.com <From%3Apaging@rednmxcad.com>
 System: Red Alert
 [Auto Alarm]  at 2890 HEMPSTEAD TPKE #A, LEVITTOWN  c/s: SHELTER LN   O: THE LITTLE GYM . . 16:01:16
 [Phone In]  at 47 STEVEDORE LN, LEVITTOWN  c/s: PARSON LN   O: CAIOLA . . 10:24:26
 Signal 9 at 62 FLAMINGO RD, LEVITTOWN  c/s: GULL LN   O: OLSEN . . 09:34:27
 Brush Fires at TARRY LN / BAYBERRY LN, LEVITTOWN . . 14:28:07
 
 Brentwood, NY
 General Alarm, Auto Fire Alarm at 601 SUFFOLK AVE #201, BRENTWOOD  c/s: ADAMS AVE   O: LONG ISLAND EYE SURGICAL CARE . . 08:56:41
 
 Hicksville FD
 SIGNAL 9 at 17 BRIDLE LN, HICKSVILLE c/s: BLUEBERRY LN O: KENDALL, ANNA..14:48:48
 
 Glen Cove FD
 GENERAL ALARM, POSSIBLE HOUSE FIRE: SMOKE IN THE HOUSE at 62 TOWN PATH RD, GLEN COVE  c/s: WALNUT RD   O: CAPOBIANCO . . 12:34:31
 SIG 8, CARBON MONOXIDE ACTIVATION at 238 WALNUT RD EXT, GLEN COVE  c/s: SEAMAN RD   O: IANNONE . . 11:32:25
 GENERAL ALARM, AUTOMATIC FIRE ALARM at 5 MCKINLEY PL, GLEN COVE  c/s: GRANT PL   O: RASENBERGER . . 19:11:33
 GENERAL ALARM, ELECTRICAL BURNING at 10 YALE PL, GLEN COVE c/s: PURDUE RD O: MORRISSEY..09:27:08
 
 Oyster Bay FD
 RESCUE, AMBU at OYSTER BAY LIBRARY 89 MAIN ST E, OYSTER BAY c/s: PEARL ST / ANSTICE ST, PEDESTRIAN STRUCK . . 12:45:00
 CHIEF, WIRE at  SANDY HILL ROAD, OYSTER BAY c/s: MAIN (E) ST, WIRES BURNING . . 00:20:00
 
 */
 
 public class DispatchRedAlertParser extends SmsMsgParser {
   
   private static final Pattern TIME_MARK = Pattern.compile("\\. ?\\. ?[\\d:]*$");
   private static final Pattern CODE_PATTERN = Pattern.compile("\\b\\d{1,2}-?[A-Z]-?\\d{1,2}[A-Z]?\\b");
   
   public DispatchRedAlertParser(String defCity, String defState) {
     super(defCity, defState);
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     // Look for the trailing time signature
     // If we find it, strip it off.
     boolean ok = false;
     Matcher match = TIME_MARK.matcher(body);
     if (match.find()) {
       ok = true;
       body = body.substring(0,match.start()).trim();
     }
     
     // Call is sometimes in square brackets, which got treated as a subject
     // in which case it needs to be restored
     if (subject.length() > 0) body = subject + " " + body;
     
     // Also must have at " at " keyword which we will change to "LOC:"
     // If there happen to be more than one, only change the last one
     int pt = body.indexOf(" S:");
     if (pt <0) pt = body.length();
     pt = body.lastIndexOf(" at ", pt);
     if (pt < 0) return false;
     body = "TYPE:" + body.substring(0, pt) + " LOC: " + body.substring(pt+4);
     body = body.replace("c/s:", "CROSS:").replaceAll("\\s+", " ");
     Properties props = parseMessage(body, new String[]{"TYPE","LOC","CROSS","O", "- S"});
     
     String sAddress = props.getProperty("LOC", "");
     if (sAddress.startsWith("intersection of ")) sAddress = sAddress.substring(16);
     if (sAddress.contains(",")){
       int indx = sAddress.indexOf(",");
       data.strCity = sAddress.substring(indx + 1).trim();
       sAddress = sAddress.substring(0,indx).trim();
     }
     // Protect C/O sequence form being treated as an intersection
     parseAddress(sAddress.replace("C/O", "C%O"), data);
     data.strAddress = data.strAddress.replace("C%O", "C/O");
     String sCall = props.getProperty("TYPE", "");
     int ipt = sCall.indexOf(':');
     if (ipt >= 0) {
       data.strCall= sCall.substring(0,ipt).trim();
       data.strSupp= sCall.substring(ipt+1).trim();
       match = CODE_PATTERN.matcher(data.strSupp);
       if (match.find()) {
         data.strCode = data.strSupp.substring(match.start(), match.end());
         data.strSupp = cutOut(data.strSupp, match.start(), match.end());
         if (data.strSupp.startsWith("- ")) data.strSupp = data.strSupp.substring(2).trim();
       }
     } else {
       data.strCall = sCall;
     }
     data.strCall = data.strCall.replaceAll("\\. \\.", "-");
 
     data.strPlace = props.getProperty("O", "");
    String sCross = props.getProperty("CROSS");
     if (sCross != null) {
       ok = true;
       data.strCross = sCross;
     }
 
     return ok;
   }
 }
