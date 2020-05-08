 package polly.rx.parsing;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import de.skuzzle.polly.tools.iterators.ArrayIterator;
 
 import polly.rx.ParseException;
 import polly.rx.entities.FleetScan;
 import polly.rx.entities.FleetScanShip;
 
 
 public class FleetScanParser {
     
     private final static Pattern CLAN_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
     private final static int CLAN_GROUP = 1;
 
     private final static Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
     private final static int NUMBER_GROUP = 1;
     
     
     private final static Pattern SHIP_PATTERN = Pattern.compile(
             "(.+?)\\s+\\(ID:(\\d+)\\)\\s+(\\d+)\\s+(.+)");
     private final static int SHIP_NAME_GROUP = 1;
     private final static int SHIP_ID_GROUP = 2;
     private final static int SHIP_TL_GROUP = 3;
     private final static int SHIP_OWNER_GROUP = 4;
     
     public static void main(String[] args) throws ParseException {
         String paste = "\n" + 
         		"Lokale Sensorstrke: 106\n" + 
         		"Tarnwert der maximal erfassbaren Schiffe: 106\n" + 
         		"\n" + 
         		"Flotten Daten\n" + 
         		"PitArmor    Newborn[Loki]\n" + 
         		"\n" + 
         		"Gescannte Schiffe\n" + 
         		"Name    Techlevel   Besitzer\n" + 
         		"Begleitschiff (LIII) Neddah (ID:11267746)   17  Newborn[Loki]\n" + 
         		"Begleitschiff (XLIII) Fergon (ID:8654080)   15  Newborn[Loki]\n" + 
         		"Begleitschiff (XLIII) Werato (ID:8654109)   15  Newborn[Loki]\n" + 
         		"Begleitschiff (XLVIII) Regnaman (ID:10404473)   16  Newborn[Loki]\n" + 
         		"Kommandoschiff (XLII) Lerbha (ID:8646668)   15  Newborn[Loki]\n" + 
         		"Tankschiff (L) Chanis (ID:8510853)  15  Newborn[Loki]\n" + 
         		"Tankschiff (L) Werfola (ID:8495382) 15  Newborn[Loki]\n" + 
         		"Transportschiff (LIX) Celesto (ID:12021144) 17  Newborn[Loki]\n" + 
         		"Transportschiff (LIX) Domkas (ID:12112445)  17  Newborn[Loki]\n" + 
         		"Transportschiff (LIX) Grolito (ID:12066370) 17  Newborn[Loki]\n" + 
         		"Transportschiff (LIX) Leivo (ID:12166115)   17  Newborn[Loki]\n" + 
         		"Transportschiff (LIX) Sangitan (ID:12068103)    17  Newborn[Loki]\n" + 
         		"Transportschiff (LIX) Tionsis (ID:12085622) 17  Newborn[Loki]\n" + 
         		"Zerstrer (XXXVIII) Imatron (ID:10328672)   21  Newborn[Loki]";
         
         
         parseFleetScan(paste, "", 0, 0, "");
     }
     
     
     
     public final static FleetScan parseFleetScan(String paste, String quadrant, 
             int x, int y, String metaData) throws ParseException {
         try {
             return parseFleetScanHelper(paste, quadrant, x, y, metaData);
         } catch (Exception e) {
             throw new ParseException("Ungltiger Flottenscan.", e);
         }
     }
     
     
     
     private final static FleetScan parseFleetScanHelper(String paste, String quadrant, 
             int x, int y, String metaData) throws ParseException {
         String[] lines = paste.split("[\n\r]+");
         
         int sens = 0;
         String fleetName = "";
         String owner = "";
         String ownerClan = "";
         String fleetTag = "";
         List<FleetScanShip> ships = new LinkedList<FleetScanShip>();
         
         ArrayIterator<String> it = ArrayIterator.get(lines);
         while (it.hasNext()) {
             String line = it.next();
             
            if (line.startsWith("Lokale Sensorstrke")) {
                 Matcher m = NUMBER_PATTERN.matcher(line);
                 m.find();
                 
                 sens = RegexUtils.subint(line, m, NUMBER_GROUP);
                 
             } else if (line.startsWith("Flotten Daten")) {
                 line = it.next();
                 String parts[] = line.split("\\s+");
                 fleetName = parts[0];
                 
                 Matcher m = CLAN_PATTERN.matcher(parts[1]);
                 if (m.find()) {
                     ownerClan = RegexUtils.substr(parts[1], m, CLAN_GROUP);
                     owner = RegexUtils.substr(
                         parts[1], 0, parts[1].length() - (ownerClan.length() + 2));
                 } else {
                     owner = parts[1];
                 }
                 
                 String next = it.peekNext();
                 if (!next.equals("") && !next.startsWith("Gescannte Schiffe")) {
                     fleetTag = it.next();
                 }
             } else if (line.startsWith("Gescannte Schiffe")) {
                 it.next();
                 while (it.hasNext()) {
                     line = it.next();
                     Matcher m = SHIP_PATTERN.matcher(line);
                     if (m.matches()) {
                         String shipName = RegexUtils.substr(line, m, SHIP_NAME_GROUP);
                         int shipId = RegexUtils.subint(line, m, SHIP_ID_GROUP);
                         int shipTl = RegexUtils.subint(line, m, SHIP_TL_GROUP);
                         String ownerName = RegexUtils.substr(line, m, SHIP_OWNER_GROUP);
                         String shipOwnerClan = "";
                         
                         Matcher clanMatcher = CLAN_PATTERN.matcher(ownerName);
                         if (clanMatcher.find()) {
                             shipOwnerClan = RegexUtils.substr(
                                 ownerName, clanMatcher, CLAN_GROUP);
                             ownerName = RegexUtils.substr(
                                 ownerName, 0, 
                                 ownerName.length() - (shipOwnerClan.length() + 2));
                         }
                         
                         FleetScanShip ship = new FleetScanShip(shipId, shipName, shipTl, 
                             ownerName, shipOwnerClan, quadrant, x, y);
                         ships.add(ship);
                     }
                 }
             }
         }
         
         if (fleetName.equals("") || owner.equals("") || ships.isEmpty()) {
             throw new ParseException("unglter Flottenscan.");
         }
         
         return new FleetScan(sens, fleetName, owner, ownerClan, fleetTag, ships, 
             quadrant, x, y, metaData);
     }
 }
