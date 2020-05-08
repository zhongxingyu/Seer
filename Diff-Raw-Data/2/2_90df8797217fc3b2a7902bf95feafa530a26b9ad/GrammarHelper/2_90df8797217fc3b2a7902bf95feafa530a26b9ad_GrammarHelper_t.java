 package org.anhonesteffort.sciencebox.language;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Programmer: rhodey
  * Date: 10/12/13
  */
 public class GrammarHelper {
 
   private static enum WaitType {
     MILLISECONDS,
     SECONDS,
     MINUTES,
     HOURS,
     DAYS
   }
 
   private static final String[] DEVICE_TOKENS = {
       Grammar.TOKEN_DEVICE_FAN,
       Grammar.TOKEN_DEVICE_HEATER,
       Grammar.TOKEN_DEVICE_COOLER,
       Grammar.TOKEN_DEVICE_HUMIDIFIER
   };
 
   private static final String[] CONTROL_TOKENS = {
       Grammar.TOKEN_CONTROL_TEMPERATURE,
       Grammar.TOKEN_CONTROL_HUMIDITY
   };
 
   public static boolean isBlockBegin(String line) {
     return line.matches("^(" + Grammar.TOKEN_BLOCK_BEGIN + ")(\\s+).*");
   }
 
   public static boolean isBlockEnd(String line) {
     return line.matches("^(" + Grammar.TOKEN_BLOCK_END + ")(\\s+).*");
   }
 
   public static boolean isLoopBegin(String line) {
     return line.matches("^(" + Grammar.TOKEN_BLOCK_BEGIN + ")(\\s+)(" + Grammar.TOKEN_LOOP + ")(\\s+).*");
   }
 
   public static boolean isLoopEnd(String line) {
     return line.matches("^(" + Grammar.TOKEN_BLOCK_END + ")(\\s+)(" + Grammar.TOKEN_LOOP + ")$");
   }
 
   public static int getLoopCount(String line) throws IllegalSyntaxException {
     if (!isLoopBegin(line))
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN BLOCK BEGIN and/or TOKEN LOOP.");
 
     int num_pos = line.indexOf(Grammar.TOKEN_LOOP) + Grammar.TOKEN_LOOP.length() + 1;
     String numString = line.substring(num_pos);
     return Integer.parseInt(numString);
   }
 
   public static String getProcedureName(String line) throws IllegalSyntaxException {
     if (!isBlockBegin(line))
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN BLOCK BEGIN.");
 
     int name_pos = Grammar.TOKEN_BLOCK_BEGIN.length() + 1;
     String nameFound = line.substring(name_pos);
 
     if (nameFound.length() == 0)
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN PROCEDURE NAME.");
 
     if (nameFound.contains(" "))
       throw new IllegalSyntaxException("White space found inside TOKEN PROCEDURE NAME.");
 
     return nameFound;
   }
 
   public static boolean isProcedureEnd(String line, String procedureName) throws IllegalSyntaxException {
     if (!isBlockEnd(line))
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN BLOCK END.");
 
     int name_pos = Grammar.TOKEN_BLOCK_END.length() + 1;
     String nameFound = line.substring(name_pos);
 
     if (nameFound.length() == 0)
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN PROCEDURE NAME.");
 
     if (nameFound.contains(" "))
       throw new IllegalSyntaxException("White space found inside TOKEN PROCEDURE NAME.");
 
     return nameFound.equals(procedureName);
   }
 
   public static boolean isWaitStatement(String line) {
     return line.matches("^(" + Grammar.TOKEN_WAIT + ")(\\s+).*");
   }
 
   private static WaitType getWaitType(String line) throws IllegalSyntaxException {
     Pattern pattern = Pattern.compile("^(" + Grammar.TOKEN_WAIT + ")(\\s+)([0-9]+)(\\s*)(\\S+)$");
 
     Matcher matcher = pattern.matcher(line);
     while (matcher.find()) {
       if (matcher.group(5).equals(Grammar.TOKEN_TYPE_MILLISECONDS))
         return WaitType.MILLISECONDS;
       if (matcher.group(5).equals(Grammar.TOKEN_TYPE_SECONDS))
         return WaitType.SECONDS;
       if (matcher.group(5).equals(Grammar.TOKEN_TYPE_MINUTES))
         return WaitType.MINUTES;
       if (matcher.group(5).equals(Grammar.TOKEN_TYPE_HOURS))
         return WaitType.HOURS;
       if (matcher.group(5).equals(Grammar.TOKEN_TYPE_DAYS))
         return WaitType.DAYS;
     }
 
     throw new IllegalSyntaxException("Provided line does not contain legal TOKEN WAIT TYPE.");
   }
 
   private static long getWaitCount(String line) throws IllegalSyntaxException {
     Pattern pattern = Pattern.compile("^(" + Grammar.TOKEN_WAIT + ")(\\s+)([0-9]+)(\\s*)(\\S+)$");
 
     Matcher matcher = pattern.matcher(line);
     while (matcher.find())
       return Long.parseLong(matcher.group(3));
 
     throw new IllegalSyntaxException("Provided line does not contain legal TOKEN WAIT COUNT.");
   }
 
   public static long getWaitCountMilliseconds(String line) throws IllegalSyntaxException {
     if (!isWaitStatement(line))
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN WAIT.");
 
     WaitType waitType = getWaitType(line);
     long wait_milliseconds = getWaitCount(line);
 
     switch (waitType) {
       case MILLISECONDS:
         break;
 
       case SECONDS:
         wait_milliseconds = wait_milliseconds * 1000;
         break;
 
       case MINUTES:
         wait_milliseconds = (wait_milliseconds * 1000) * 60;
         break;
 
       case HOURS:
         wait_milliseconds = ((wait_milliseconds * 1000) * 60) * 60;
         break;
 
       case DAYS:
         wait_milliseconds = (((wait_milliseconds * 1000) * 60) * 60) * 24;
         break;
     }
 
     return wait_milliseconds;
   }
 
   public static boolean isDeviceSetting(String line) {
     for (int i = 0; i < DEVICE_TOKENS.length; i++) {
       if (line.matches("^(" + DEVICE_TOKENS[i] + ")(\\s+).*"))
         return true;
     }
     return false;
   }
 
   public static boolean isControlSetting(String line) {
     for (int i = 0; i < CONTROL_TOKENS.length; i++) {
       if (line.matches("^(" + CONTROL_TOKENS[i] + ")(\\s+).*"))
         return true;
     }
     return false;
   }
 
   public static Grammar.SettingType getSettingType(String line) throws IllegalSyntaxException {
     if (!isDeviceSetting(line) && !isControlSetting(line))
       throw new IllegalSyntaxException("Provided line does not contain legal TOKEN DEVICE TYPE or TOKEN CONTROL TYPE.");
 
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_PERCENTAGE + ")$"))
       return Grammar.SettingType.PERCENTAGE;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_CELSIUS + ")$"))
       return Grammar.SettingType.CELSIUS;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_FAHRENHEIT + ")$"))
       return Grammar.SettingType.FAHRENHEIT;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_MILLISECONDS + ")$"))
       return Grammar.SettingType.MILLISECONDS;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_SECONDS + ")$"))
       return Grammar.SettingType.SECONDS;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_MINUTES + ")$"))
       return Grammar.SettingType.MINUTES;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_HOURS + ")$"))
       return Grammar.SettingType.HOURS;
     if (line.matches("^(\\S+)(\\s+)([0-9]+)(\\s*)(" + Grammar.TOKEN_TYPE_DAYS + ")$"))
       return Grammar.SettingType.DAYS;
     if (line.matches("^(\\S+)(\\s+)([ON,OFF]+)$"))
       return Grammar.SettingType.ON_OFF;
 
     throw new IllegalSyntaxException("Provided line does not contain legal TOKEN SETTING TYPE " +
         "and/or TOKEN SETTING VALUE.");
   }
 
   public static double getSettingValue(String line) throws IllegalSyntaxException {
     Pattern pattern;
     Matcher matcher;
 
     if (getSettingType(line) == Grammar.SettingType.ON_OFF)  {
       pattern = Pattern.compile("^(\\S+)(\\s+)([ON,OFF]+)$");
       matcher = pattern.matcher(line);
       if (matcher.find()) {
         if (matcher.group(3).equals(Grammar.TOKEN_VALUE_ON))
           return 1.0;
        if (matcher.group(3).equals(Grammar.TOKEN_VALUE_OFF))
           return 0.0;
       }
     }
     else {
       pattern = Pattern.compile("^(\\S+)(\\s+)([0-9]+)(\\s*)(\\S+)$");
       matcher = pattern.matcher(line);
       if(matcher.find())
         return Double.parseDouble(matcher.group(3));
     }
 
     throw new IllegalSyntaxException("Provided line does not contain legal TOKEN SETTING VALUE.");
   }
 
   public static Grammar.DeviceType getDeviceType(String line) throws IllegalSyntaxException {
     if (line.matches("^(" + Grammar.TOKEN_DEVICE_FAN + ")(\\s+).*"))
       return Grammar.DeviceType.FAN;
     if (line.matches("^(" + Grammar.TOKEN_DEVICE_HEATER + ")(\\s+).*"))
       return Grammar.DeviceType.HEATER;
     if (line.matches("^(" + Grammar.TOKEN_DEVICE_COOLER + ")(\\s+).*"))
       return Grammar.DeviceType.COOLER;
     if (line.matches("^(" + Grammar.TOKEN_DEVICE_HUMIDIFIER + ")(\\s+).*"))
       return Grammar.DeviceType.HUMIDIFIER;
 
     throw new IllegalSyntaxException("Provided line does not contain legal TOKEN DEVICE TYPE.");
   }
 
   public static Grammar.ControlType getControlType(String line) throws IllegalSyntaxException {
     if (line.matches("^(" + Grammar.TOKEN_CONTROL_TEMPERATURE + ")(\\s+).*$"))
       return Grammar.ControlType.TEMPERATURE;
     if (line.matches("^(" + Grammar.TOKEN_CONTROL_HUMIDITY + ")(\\s+).*$"))
       return Grammar.ControlType.HUMIDITY;
 
     throw new IllegalSyntaxException("Provided line does not contain legal TOKEN CONTROL TYPE.");
   }
 
 }
