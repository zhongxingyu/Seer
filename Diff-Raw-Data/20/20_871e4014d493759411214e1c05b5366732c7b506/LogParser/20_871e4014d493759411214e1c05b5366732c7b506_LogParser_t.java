 package parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import report.Damage;
 import report.Energize;
 import report.Fight;
 import report.Heal;
 import report.LogReport;
 import report.Miss;
 import report.Miss.Type;
 import report.UnitActivity;
 import world.TimeInterval;
 import world.Timestamp;
 import world.Unit;
 
 import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;
 
 import event.LogEvent;
 
 public class LogParser {
 
     LogReport report;
 
     List<EventParser> eventParsers = new ArrayList<EventParser>();
 
     public LogParser(String string) throws FileNotFoundException, java.text.ParseException {
 
         report = new LogReport();
 
         initParsers();
 
         Scanner scanner = new Scanner(new File(string));
 
         while (scanner.hasNextLine()) {
             String line = scanner.nextLine();
 
             try {
                 LogEvent event = parserEvent(line);
 
                 if (event != null) {
                     report.addEvent(event);
                 }
 
             } catch (ParseException e) {
                 System.err.println(e.getMessage());
             }
 
         }
 
         report.compute();
 
     }
 
     private void initParsers() {
         eventParsers.add(new SwingEventParser());
         eventParsers.add(new RangeEventParser());
         eventParsers.add(new SpellEventParser(report));
         eventParsers.add(new SpecialDamageEventParser(report));
         eventParsers.add(new SpecialsEventParser());
 
 
     }
 
     private LogEvent parserEvent(String line) throws ParseException, java.text.ParseException {
         String[] split = line.split(" +", 3);
 
         if (split.length != 3) {
             throw new ParseException("Bad formated line. Excepted Date Time Other. Found: " + line);
         }
 
         Timestamp time = new Timestamp(split[0], split[1]);
         String params = split[2];
 
         String[] splitParam = params.split(",");
 
        for(int i = 0; i < splitParam.length ; i++) {
        	if(splitParam[i].startsWith("\"") && !splitParam[i].endsWith("\"")) {
        		String[] newSplitParam = new String[splitParam.length -1];
        		for(int j = 0; j < i ; j++) {
        			newSplitParam[j] = splitParam[j];
        		}
        		newSplitParam[i] = splitParam[i]+ "," + splitParam[i+1];
        		
        		for(int j = i+2; j < splitParam.length ; j++) {
        			newSplitParam[j-1] = splitParam[j];
        		}
        		splitParam = newSplitParam;
        		i--;
        	}
        }
        
        
         if (splitParam.length < 7) {
             throw new ParseException("Bad formated line. Excepted at least 7. Found: " + params);
         }
 
         String key = splitParam[0];
 
         Unit sourceUnit = report.getUnitManager().parseUnit(parseGuid(splitParam[1]), parseString(splitParam[2]), parseLong(splitParam[3]));
         Unit targetUnit = report.getUnitManager().parseUnit(parseGuid(splitParam[4]), parseString(splitParam[5]), parseLong(splitParam[6]));
 
         for (EventParser eventParser : eventParsers) {
             if (eventParser.match(key)) {
                 return eventParser.parse(time, sourceUnit, targetUnit, splitParam);
             }
         }
 
         throw new ParseException("Unknow event " + key + " in " + params);
 
     }
 
     public static long parseLong(String string) {
         return Long.parseLong(string.substring(2), 16);
     }
 
     public static int parseInt(String string) {
         return Integer.parseInt(string.substring(2), 16);
     }
 
     public static String parseString(String string) {
         return string.substring(1, string.length() - 1);
     }
 
     public static String parseGuid(String string) {
         return string.substring(2);
     }
 
     public static Damage parseDamage(String[] params, int index) {
 
         int amount = Integer.valueOf(params[index + 0]);
         int overkill = Integer.valueOf(params[index + 1]);
         int school = Integer.valueOf(params[index + 2]);
         int resisted = Integer.valueOf(params[index + 2]);
         int blocked = Integer.valueOf(params[index + 2]);
         int absorbed = Integer.valueOf(params[index + 2]);
         boolean critical = (params[index + 2].equals("nil") ? false : true);
         // Ricoché
         boolean glancing = (params[index + 2].equals("nil") ? false : true);
         boolean crushing = (params[index + 2].equals("nil") ? false : true);
 
         return new Damage(amount, overkill, school, resisted, blocked, absorbed, critical, glancing, crushing);
 
     }
 
     public static Heal parseHeal(String[] params, int index) {
 
         int amount = Integer.valueOf(params[index + 0]);
         int overhealing = Integer.valueOf(params[index + 1]);
         int absorbed = Integer.valueOf(params[index + 2]);
         boolean critical = (params[index + 2].equals("nil") ? false : true);
 
         return new Heal(amount, overhealing, absorbed, critical);
 
     }
 
     public static Miss parseMiss(String[] params, int index) {
 
         String reason = params[index + 0];
 
         int amount = 0;
 
         if(params.length > index +1) {
             amount = Integer.valueOf(params[index + 1]);
         }
 
         Miss.Type type = null;
 
         if (reason.equals("ABSORB")) {
             type = Type.ABSORB;
         } else if (reason.equals("BLOCK")) {
             type = Type.BLOCK;
         } else if (reason.equals("DEFLECT")) {
             type = Type.DEFLECT;
         } else if (reason.equals("DODGE")) {
             type = Type.DODGE;
         } else if (reason.equals("EVADE")) {
             type = Type.EVADE;
         } else if (reason.equals("IMMUNE")) {
             type = Type.IMMUNE;
         } else if (reason.equals("MISS")) {
             type = Type.MISS;
         } else if (reason.equals("PARRY")) {
             type = Type.PARRY;
         } else if (reason.equals("REFLECT")) {
             type = Type.REFLECT;
         } else if (reason.equals("RESIST")) {
             type = Type.RESIST;
         }
 
         return new Miss(amount, type);
 
     }
 
     public static Energize parseEnergize(String[] params, int index) {
 
         int amount = Integer.valueOf(params[index + 0]);
         String reason = params[index + 1];
 
         Energize.Type type = null;
 
         if (reason.equals("-2")) {
             type = Energize.Type.HEALTH;
         } else if (reason.equals("0")) {
             type = Energize.Type.MANA;
         } else if (reason.equals("1")) {
             type = Energize.Type.RAGE;
         } else if (reason.equals("2")) {
             type = Energize.Type.FOCUS;
         } else if (reason.equals("3")) {
             type = Energize.Type.ENERGY;
         } else if (reason.equals("4")) {
             type = Energize.Type.PET_HAPPINESS;
         } else if (reason.equals("5")) {
             type = Energize.Type.RUNES;
         } else if (reason.equals("6")) {
             type = Energize.Type.RUNIC_POWER;
         } else if (reason.equals("7")) {
             type = Energize.Type.SOUL_SHARD;
         } else if (reason.equals("8")) {
             type = Energize.Type.ECLIPSE;
         } else if (reason.equals("9")) {
             type = Energize.Type.HOLY_POWER;
         } else if (reason.equals("10")) {
             type = Energize.Type.ATRAMEDES_SOUND;
         }
 
         return new Energize(amount, type);
 
     }
 
 
     public static void main(String[] args) throws FileNotFoundException, java.text.ParseException {
        LogParser parser = new LogParser("/home/fred/tmp/WoWCombatLog.txt");
 
         LogReport report = parser.getReport();
 
         System.out.println("Nombre d'evenements: " + report.getEventCount());
 
         List<Fight> allFights = report.getAllFigths();
 
         for (Fight fight : allFights) {
             int index = allFights.indexOf(fight);
             List<UnitActivity> mobs = fight.getMobActivities();
             List<UnitActivity> characters = fight.getCharacterActivities();
 
             String mobNames = "";
             for (UnitActivity mob : mobs) {
                 mobNames += mob.getUnit().getName() + (mob.isDying() ? " †" : "") + ", ";
             }
             mobNames = mobNames.substring(0, mobNames.length() - 2);
 
             String characterNames = "";
             for (UnitActivity character : characters) {
                 characterNames += character.getUnit().getName() + (character.isDying() ? " †" : "") + ", ";
             }
             characterNames = characterNames.substring(0, characterNames.length() - 2);
 
             System.out.println("Combat " + index + " - " + (fight.isWipe() ? "wipe" : "success"));
             System.out.println("    Mobs   : " + mobNames);
             System.out.println("    Persos : " + characterNames);
             System.out.println("    Début  : " + fight.getBeginTime().toString());
             System.out.println("    Fin    : " + fight.getEndTime().toString());
             System.out.println("    Durée  : " + TimeInterval.print(fight.getTimeInterval().getDuration()));
         }
 
         Unit homeostasie = report.getUnitManager().getUniqueByName("Homéostasie");
         List<Fight> fights = report.getFigthsWith("Chimaeron");
 
         // for(Fight fight:fights) {
         //
         // System.out.println("");
         // System.out.println("");
         // System.out.println("============");
         // System.out.println("= Chimaron =");
         // System.out.println("============");
         // System.out.println("");
         //
         // List<LogEvent> eventList = fight.getEventList();
         // for(LogEvent event: eventList) {
         // if(event.involve(homeostasie)) {
         // System.out.println(event.toString());
         // }
         // }
         // }
 
     }
 
     public LogReport getReport() {
         return report;
     }
 
 }
