 package net.anei.cadpage.parsers.TX;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /* Sample Harris Page
 
 Contact: chris pivonka <cpivonka87@gmail.com>
 Sender: CommCenter@ccems.com <From%3ACommCenter@ccems.com>
 
 1of2:09/06 11:28 19707 WOOD WALK LN, ; Map:337U Sub:PINEHURST OF ATASCOCITA Nat:09E01-ARREST - NOT BREATHING Units:E-M19 E-M29 E-7900
 1of2:09/05 08:56 19226 AQUATIC DR, ; Map:378A Sub:WALDEN ON LAKE HOUSTON Nat:52B01G-FIRE ALARM - RESIDENTIAL Units:ATFD E-E39 X-
 1of2:09/04 19:45 17219 KOBUK VALLEY CIR, ; Map:377E Sub:EAGLE SPRINGS Nat:67B03U-OUTSIDE FIRE - INVESTIGA Units:E-E39 X-St:*** Dead
 1of2:09/03 08:14 LILES LN/WOODLAND HILLS DR, ; Map:376H Sub:ATASCOCITA FOREST Nat:29-MOTOR VEHICLE INCIDENT Units:E-M19 E-E39 X-
 11/15 11:28 19506 SWEETGUM FOREST DR, ; Map:337V Sub:PINEHURST OF ATASCOCITA Nat:28B01U-STROKE/CVA - UNKNOWN Units:E-E39 X-St:TWELFTH FAIRWAY *** Dead
 11/13 13:12 6918 ATASCA CREEK DR, ; Map:377C Sub:ATASCA WOODS Nat:52B01G-FIRE ALARM - RESIDENTIAL Units:ATFD E-E39 X-St:LEENS LODGE LN FOUNTAIN L
 11/13 02:18 E FM 1960/DINERO DR, ; Map:337Z Sub: Nat:29D02M-MVI - PEDESTRIAN Units:E-M19 E-E39 E-B39 X-St:DINERO DR E FM 1960
 11/13 02:17 E FM 1960/DINERO DR, ; Map:337Z Sub: Nat:29-MOTOR VEHICLE INCIDENT Units:E-M19 E-E39 X-St:DINERO DR E FM 1960
 11/12 18:24 Repage: 8922 PINE SHORES DR, ; Map:338S Sub:ATASCOCITA SHORES Nat:60B02-GAS LEAK - UNKNOWN Units:E-E19 X-St:SHOREVIEW LN SUNCOVE LN
 11/12 18:08 Repage: ATASCOCITA RD/TIMBER FOREST D, ; Map:377A Sub: Nat:29-MOTOR VEHICLE INCIDENT Units:HUM2 E-E29 X-St:TIMBER FOREST DR ATASCOCI
 11/12 17:31 E FM 1960/POSSUM PARK RD, ; Map:336T Sub: Nat:68C01-HEAVY SMOKE OUTSIDE Units:E-E39 E-B39 X-St:POSSUM PARK RD E FM 1960
 11/19 17:00 OAK HOLLOW DR-HC/GRANT RD-HC, ; Map:369E Nat:67B01U-SMALL OUTSIDE FIRE Units:E21 E26 B22 X-St:GRANT RD WILLOW LN
 11/28 12:49 6006 WOODMANCOTE DR-HC, ; Map:337X- Nat:27-STABBING / SHOOTING Units:E-M29 X-St:DANALYN CT CLOYANNA LN
 
 1of2:09/06 11:56 W HILLSIDE DR/EASTEX FRWY, ; Map:414D Sub: Nat:MA-MUTUAL AID / ASSIST AGENCY Units:E91 T81 T73 E-L19 X-St:EASTEX
 12/07 12:26 14231 CELLINI DR-HC, ; Map:368P- Nat:MA-MUTUAL AID / ASSIST AGENCY Units:MUTAID E24 X-St:TERREBONE DR BONAPARTE DR
 12/08 17:53 13830 ROSETTA DR-HC, ; Map:368P- Nat:MA-MUTUAL AID / ASSIST AGENCY Units:MUTAID E22 X-St:BELLA DR LA BARRE DR
 
 Contact: Bryan Perry <Bryan.Perry@ccvfd.com>
 12/29 11:17 7575<tel:177575> N SAM HOUSTON PKWY W EB-, SAM HOUSTON RACE PARK; FL 1 Map:370T- Nat:71B01-VEHICLE FIRE Units:E24 NWE43 B23 X-St:FAIRBANKS N HOUSTON RD N
 
 Contact: Patrick Boren <boren.patrick@gmail.com>
 02/16 FM 2920-HC/RHODES RD-HC, ; Map:291N- Sub: Nat:29-MOTOR VEHICLE INCIDENT Units:M510 FO290 X-St:RHODES RD MARCIN DR 201107335
 
 Contact: Nathan Mathews <nbmathews807@gmail.com>
 03/14 10:10 HUFSMITH KOHRVILLE RD-HC/BOUD, ; Map:289X- Sub: Nat:59C02O-FUEL SPILL - UNCONTAINED Units:E306 HM1 X-St:BOUDREAUX RD
 03/19 09:48 CROSBY TRAINING, ; Map:- Sub: Nat:HM3-HC HAZMAT TRAINING Units:HM1 X-St: 201112139
 
 Contact: Preston Bartlett <fighter727@gmail.com>
   06/08 12:42 8314 CINNAMON RUN DR-HC, ; Map:250J- Sub: Nat:67-OUTSIDE FIRE Units:WDE107 E76 X-St:S ROLLING OAKS DR KUYKEND 201125394 TXT STOP to
   
 Contact: joe marroni <joeman1115@gmail.com>
   06/08 16:04 12110 SARTI ST-HC, ; Map:371A- Sub:FOUNTAINHEAD Nat:31D02-UNCON - EFFECTIVE BREATHI Units:HCM93 D52 11M X-St:BROOKHEAD TRL 201125435 TXT STOP to opt-out
 
 Contact: Preston Bartlett <fighter727@gmail.com>
  09/05 15:19 18300 ELLA BLVD-HC, ; Map:331G- Sub:CYPRESSWOOD Nat:67D03-LARGE OUTSIDE FIRE Units:E62 B78 E61 B76 E37 X-St:BLUE CYPRESS DR
  
Contact: Christopher Day <mrday010@gmail.com>   
  09/11 23:25 22539 SH 249 SB-HC, MATTRESS GIANT; Map:329N- Sub: Nat:69-LIGHT STRUCTURE INCIDENT Units:E23 E21 E33 E24 E36 TW23 L36 X-St:LAKEWOOD CROSSING DR 
 
 */
 
 public class TXCyCreekCommCenterParser extends SmsMsgParser {
   
   private static final Pattern MARKER = Pattern.compile("\\d\\d/\\d\\d (\\d\\d:\\d\\d )?");
   private static final Pattern TRAILER = Pattern.compile(" +\\d{8,} *$");
   
   public TXCyCreekCommCenterParser() {
     super("HARRIS COUNTY", "TX");
   }
   
   @Override
   public String getFilter() {
     return "CommCenter@ccems.com,93001,777";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Strip message prefix
     
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     
     match = TRAILER.matcher(body);
     if (match.find()) body = body.substring(0,match.start());
     
     if (body.startsWith("Repage: ")) body = body.substring(8);
     body = "Loc:" + body;
     
     Properties props = parseMessage(body, new String[]{"Loc", "Map", "Sub", "Nat", "Units", "X-St"});
     String sAddr = props.getProperty("Loc", "");
     Parser p = new Parser(sAddr);
     if (sAddr.startsWith("<tel:")) {
       p.get("<tel:");
       data.strPhone = p.get('>');
     }
     parseAddressCity(p.get(','), data);
     if (data.strCity.equals("HC")) data.strCity = "";
     data.strPlace = p.get(';');
     data.strApt = p.get();
     
     data.strMap = props.getProperty("Map", "");
     String sPlace = props.getProperty("Sub", "");
     if (sPlace.length() > 0) data.strPlace = sPlace;
     data.strCall = props.getProperty("Nat", "");
     data.strUnit = props.getProperty("Units", "");
     data.strCross = props.getProperty("X-St", "");
     
     if (data.strCall.contains("MA-MUTUAL AID")) data.strCity = "HOUSTON";
     return true;
   }
 }
