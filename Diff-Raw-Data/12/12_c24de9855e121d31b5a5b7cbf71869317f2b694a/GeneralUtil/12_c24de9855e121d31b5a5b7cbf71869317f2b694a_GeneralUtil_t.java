 package util;
 
 import java.awt.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mayacat
  * Date: 10/29/13
  * Time: 4:46 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GeneralUtil {
 
     public static Color farmColor = new Color(0x80, 0x80, 0x80);
     public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
     public static String[] alarmMessages = {"Sau er død.", "Sau registrert med usedvanlig lav puls over et lengre tidsrom", "Sau registrert med usedvanlig høy puls over et lengre tidsrom"};
 
     public static Color[] generateSheepColors(int count){
         int greenStart = 0xF0;
         int greenEnd = 0x20;
         int redStart = 0x60;
         int redEnd = 0xFF;
 
        if(count == 1){
            return new Color[]{new Color(redStart, greenStart, 0)};
        }

         int dg = (greenEnd - greenStart)/(count-1);
         int dr = (redEnd - redStart)/(count-1);
 
         Color[] colors = new Color[count];
         for (int i = 0; i < count; i++){
             colors[i] = new Color(redStart + dr*i, greenStart + dg*i, 0);
         }
         return colors;
     }
 
     public static String charToString(char[] array){
         StringBuilder sb = new StringBuilder();
         for(char c : array){
             sb.append(c);
         }
 
         return sb.toString();
     }
 
     /**
      * Validates a date and returns its long value
      * @param date
      * @return
      * @throws PotentialNinjaException
      */
     public static long validateDate(String date)throws PotentialNinjaException{
         String[] splitted = date.split("/");
 
         if(splitted.length != 3)
             throw new PotentialNinjaException("Feil format på dato, manglende \"/\".");
 
         try{
             int month = Integer.parseInt(splitted[0]);
             int day = Integer.parseInt(splitted[1]);
             int year = Integer.parseInt(splitted[2]);
 
             if(month > 12 || month < 0)
                 throw new PotentialNinjaException("Feil format på dato, det er bare 12 måneder i året");
             else if((day > 30 && month%2 == 0) || day < 0)
                 throw new PotentialNinjaException("Feil format på dato, den registrerte dagen finnes ikke");
             else if(day == 29 && month == 2 && year%4 == 0 && year%100 == 0 && year%400 != 0)
                 throw new PotentialNinjaException("Feil format på dato, et år delelig på hundre (1700, 1800 etc.) er bare skuddår hvis det også er delelig på 400 (1600, 2000 etc.");
             else if(day == 29 && month == 2 && year%4 != 0)
                 throw new PotentialNinjaException("Feil format på dato, februar har bare 29 dager i et skuddår.");
             else if(day > 28 && month == 2 && year%4 != 0)
                 throw new PotentialNinjaException("Feil format på dato, februar har ikke " + day + " dager.");
             else if(day > 29 && month == 2 && year%4 == 0)
                 throw new PotentialNinjaException("Feil format på dato, skuddår har bare 29 dager i februar.");
         }catch(NumberFormatException e ){
             throw new PotentialNinjaException("Feil format på dato.");
         }
 
         try{
             long dateRaw = sdf.parse(date).getTime();
             long now = new Date().getTime();
 
             if(dateRaw > now)
                 throw new PotentialNinjaException("Feil format på dato, Closed Timelike Curves (CTC's) er ikke bevist enda, og du kan ikke lage en dato i fremtiden, desverre.");
             return dateRaw;
         } catch (ParseException e) {
             throw new PotentialNinjaException("Feil format på dato.");
         }
     }
 
     /**
      * Helper function to get the key from a TreeMap by value
      * @param map
      * @param value
      * @param <T>
      * @param <E>
      * @return
      */
     public static <T, E> T getKeyByValue(TreeMap<T, E> map, E value) {
         for (Map.Entry<T, E> entry : map.entrySet()) {
             if (value.equals(entry.getValue())) {
                 return entry.getKey();
             }
         }
         return null;
     }
 
     /**
      * Validates the format of an email
      * @param email
      * @return
      */
     public static boolean isEmailValid(String email){
         String[] splitt = email.split("@");
         if(splitt.length != 2)
             return false;
 
         splitt = email.split("@")[1].split("\\p{P}");
         if(splitt.length != 2){
             return false;
         }
         return true;
     }
 
     /**
      * Gets distance between points
      * @param pos1x
      * @param pos1y
      * @param pos2x
      * @param pos2y
      * @return
      */
     public static double getDistance(double pos1x, double  pos1y, double  pos2x, double  pos2y){
         double dx = pos2x - pos1x;
         double dy = pos2y - pos1y;
         return Math.sqrt(dx*dx + dy*dy);
     }
 
     /**
      * Gets distance between points from two vectors
      * @param pos1
      * @param pos2
      * @return
      */
     public static double getDistance(Vec2 pos1, Vec2 pos2){
         return getDistance(pos1.x, pos1.y, pos2.x, pos2.y);
     }
 
     public static boolean assertEquals(double one, double two, double epsilonRelative, double epsilonAbsolute){
         return Math.abs(one-two) < epsilonAbsolute || Math.abs(1 - one/two) < epsilonRelative;
     }
 
     /**
      * Takes a timestamp and returns how long time it represents
      * @return
      */
     public static String formatTimeToString(long time)
     {
         int days = (int)(time/(60*60*24*1000));
         int hours = (int)(time/(60*60*1000) - days*24);
         int minutes = (int)(time/(60*1000) - days*24*60 - hours*60);
         int seconds = (int)(time/1000 - days*24*60*60 - hours*60*60 - minutes*60);
 
         String s = days + "d ";
         s += hours + "h ";
         s += minutes + "m ";
         s += seconds + "s";
 
         return s;
     }
 
 }
