 package net.anei.cadpage.parsers.NJ;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Burlington County, NJ
 Contact: Jerseyfirefighter@gmail.com
 Contact: Darren Randall <darren.randall@vincentfire.org>
 Sender: 7770*
 
 .... (Station 171) : 1  \n: 10 WINDING WY                                        Venue: Southamptn  \nLocated Between  : PHEASANT CL/DEAD END \n\nCall Time- 19:13:51                                : 1711 \n\n: (856) 287-8607                Caller : VERIZON WIRELESS \n\nNature of Call : MALE UNCONSCIOUS POSS OVERDOSE \n\nAdditional Info  \n  WPH2:  RADIUS 02 MILES
 .... (Station 171) : 1  \n: STOCKTONS BRIDGE RD                                  Venue: Pembtn Twp  \n  Cross Street . : SIMONTOWN RD \n\nCall Time- 19:47:43                                : 1822 \n\n: (000) 000-0000                Caller : KGB CARRIER \n\nNature of Call : TRAILER FIRE \n\n  \nF1911 RSPNDING/COLUMN IN SIGHT                               19:50:52 \n\nAdditional Inc#s
 .... (Station 171) : 1  \n: 6 PLEASANT ST                                        Venue: Southamptn  \nLocated Between  : MAIN ST/CHURCH ST \n\nCall Time- 13:27:39                                : 1700 \n\n: (609) 859-2302                Caller : RICHARD E FRANTZMAN \n\nNature of Call : MVA//VEH HIT THE HOUSE \n\nAdditional Info \n  RESD: \n\nAdditional Inc#s \n  RESD:  \n  VEHICLE TRYING TO LEAVE SCENE / NO REPORTED INJURIES \n\nAdditional Inc#s
 .... (Station 171) : 1  \n: 6 PLEASANT ST                                        Venue: Southamptn  \nLocated Between  : MAIN ST/CHURCH ST \n\nCall Time- 13:27:39                                : 1700 \n\n: (609) 859-2302                Caller : RICHARD E FRANTZMAN \n\nNature of Call : MVA//VEH HIT THE HOUSE \n\nAdditional Info
 .... (Station 171) : 1  \n: 2 RIDGE RD                                           Venue: Southamptn  \nLocated Between  : RED LION RD/RT206  \n: VERIZON 17 \n\nCallTime- 01:28:31                                : 1712 \n\n: (800) 685-1608                Caller : VERIZON SEC  \nComplaintant: OP SHARON \n\nNature of Call : GENERAL FIRE ALARM/NO PHONE ON SITE
 .... (Station 171) : 1  \n: 19 FALCON DR                                         Venue: Southamptn  \nLocated Between  : NEW RD/ROBBINS WY \n\nCall Time- 12:33:33                                  : 1728 \n\n: (609) 859-0179                Caller : T R SUNDSTROM \n\nNature of Call : POSSIBLE FIRE/SMELL AND SEE SMOKE COMING FROM\n\nAdditional Info  \n  NEW RD AREA  \n  NW/AND INSIDE RES /TOT NJSP \n\nAdditional Inc#s
 .... (Station 171) : 1  \n: 1771 RT206                                           Venue: Southamptn  \nLocated Between  : RT70/RIDGE RD  \n: EXXON 17 (RT206)\n\nCall Time- 07:12:50                              : 1712 \n\n: (609) 859-0060                Caller : E1782 \n\nNature of Call : 2 CAR MVA / \n\nAdditional Inc#s
 .... (Station 171) : 1  \n: 413 CHAIRVILLE RD                                    Venue: Southamptn  \n Located Between  : NEW FREEDOM RD/CHURCH RD \n\nCall Time- 14:15:30                                : 1713 \n\n: (609) 714-7797                Caller : LORI FERRI \n\nNature of Call : ALLERGIC REACTION TO MEDS.TROUBLE BREATHING\n\nAdditional Info  \n  RESD:
 .... (Station 171) : 1  \n: 3 SUNSET BV                                          Venue: Southamptn  \nLocated Between  : RT530/WINDING WY \n\nCall Time- 12:57:51                                : 1711 \n\n: (609) 556-9277                Caller : LENA FRIEDMEN \n\nNature of Call : 65/M C&amp;A/ PNEMONIA// ON O2 \n\nAdditional Info  \n  MOBIL ESTATES
 .... (Station 171) : 9  \n: 15 TRENTON RD                                        Venue: Pembtn Twp  \nLocated Between  : BUSANSKY LA/PEMBERTON BROWNS MILLS RD  \n: STA 181 \n\nCall Time- 14:48:58                                : 1811 \n\n: (609) 893-2114                :  TEL \n\nNature of Call : BUSANSKY LA/PEMBERTON BROWNS MILLS RD
 .... (Station 171) : 1  \n: 2357 RT206                                           Venue: Southamptn  \nLocated Between  : RT38/EASTAMPTON TWP LINE  \n: VINCENTOWN DINER \n\nCall Time- 15:08:40                                : 1711 \n\n: (609) 354-2191                Caller : RICHARD SIMONE \n\nNature of Call : 2 CAR MVA WITH NECK AND BACK INJURIES/ \n\nAdditional Info  \n  IAO  \n  EVERYONE OUT VEHICLE MOVED TO SIDE/
 
 Contact: Ryan Willitts <ryan.willitts@vincentfire.org>
 Sender: 7771
 .... (Station 171) :   13 F Vehicle              Priority: 1  \n: RIDGE RD & RT206                                    Venue: Southamptn  \n  Cross Street . : RT206 \n\nCall Time- 20:06:09                                : 1712 \n\n: (609) 372-3923                Caller : AMANDA CHAMBERLAIN \n\nNature of Call : ENGINE COMPARTMENT FIRE - VEHICLE OFF TO \n\nAdditional Info  \n  WPH2:  \n  SIDE OF ROAD \n\nAdditional Inc#s
 .... (Station 171) : FIRE F Fire Call            Priority: 1  \n: 2356 RT206                                           Venue: Southamptn  \nLocated Between  : RT38/EASTAMPTON TWP LINE  \n   : WAWA 17 (RT38 NEW WAWA) \n\nCall Time- 05:46:07                                  : 1711 \n\n: (800) 929-2011                 
 .... (Station 171) :   16 F Alarms               Priority: 1  \n: 2356 RT206                                           Venue: Southamptn  \nLocated Between  : RT38/EASTAMPTON TWP LINE  \n   : WAWA 17 (RT38 NEW WAWA) \n\nCall Time- 05:46:07                        : 1711 \n\n: (800) 929-2011                 Caller  : OP430 CENTRAL STATION \n\nNature of Call : ACT FIRE//DUCT DETECTOR//6092671257
 .... (Station 171) :   16 F Alarms               Priority: 1  \n: 2356 RT206                                           Venue: Southamptn  \nLocated Between  : RT38/EASTAMPTON TWP LINE  \n   : WAWA 17 (RT38 NEW WAWA) \n\nCall Time- 05:46:07                                  : 1711 \n\n: (800) 929-2011                 Caller  : OP430 CENTRAL STATION \n\nNature of Call : ACT FIRE//DUCT DETECTOR//6092671257
 
 */
 
 public class NJBurlingtonCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "Southamptn", "Southampton",
       "Woodland",   "Woodland",
       "Pembtn Twp", "Pemberton Twp",
       "Pembtn Bor", "Pemberton",
       "Eastampton", "Eastampton",
       "Tabernacle", "Tabernacle",
       "MedfordTwp", "Medford Twp",
       "MedfordLks", "Medford Lakes",
       "Lumberton",  "Lumberton",
       "Shamong",    "Shamong"
   });
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("[\\w ]+");
   
   public NJBurlingtonCountyParser() {
     super(CITY_CODES, "BURLINGTON COUNTY", "NJ",
            "CALL Priority:PRI! ADDR! Venue:CITY! CrossStreet:ADDR2? LocatedBetween:X? PLACE CallTime:TIME ID SKIP PHONE Caller:NAME Complaintant:SKIP NatureOfCall:INFO INFO+ AdditionalInfo:INFO");
   }
   
   @Override
   public String getFilter() {
     return "777";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     Matcher match = SUBJECT_PTN.matcher(subject);
     if (!match.find()) return false;
     data.strSource = subject;
     
     if (!body.startsWith(": ")) return false;
     body = body.substring(2).trim();
     if (!body.contains("Priority:")) body = "Priority:" + body;
     
     // See if we have all of it
     int pt = body.indexOf("Additional Inc#s");
     if (pt >= 0) {
       body = body.substring(0,pt);
     } else {
       data.expectMore = true;
     }
     
     body = body.replace(" Priority:", "\nPriority:");
     body = body.replace("Venue:", "\nVenue:");
     body = body.replace("Cross Street . :", "CrossStreet:");
     body = body.replace("Located Between  :", "LocatedBetween:");
     body = body.replace("Call Time-", "CallTime:");
     body = body.replace("Caller :", "\nCaller:");
     body = body.replace("Nature of Call :", "NatureOfCall:");
     body = body.replace("Additional Info", "AdditionalInfo:");
     body = body.replace(" : ", "\n");
     body = body.replaceAll("\n *:?", "\n");
     pt = body.indexOf("AdditionalInfo");
     if (pt >= 0) {
       body = body.substring(0,pt) + body.substring(pt).replaceAll("\\s+", " ");
     }
     body = body.replaceAll("  +", " ");
     return parseFields(body.split("\n"), data);
   }
   
   private class Address2Field extends Field {
     @Override
     public void parse(String field, Data data) {
       if (data.strAddress.contains("&") & data.strAddress.contains(field)) return;
       data.strAddress = append(data.strAddress, " & ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR2")) return new Address2Field();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
 }
