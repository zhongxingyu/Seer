 package org.aesthete.swingobjects.util;
 
 import org.aesthete.swingobjects.SwingObjProps;
 import org.aesthete.swingobjects.exceptions.InvalidDateException;
 import org.aesthete.swingobjects.exceptions.SwingObjectRunException;
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Sethuram
  * Date: 29/09/12
  * Time: 10:36 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DateUtils {
 
     private static final Map<Pattern,String> patterns=getPatterns();
    public static final DateTimeFormatter DEFAULT_DATE_FORMAT =DateTimeFormat.forPattern(SwingObjProps.getApplicationProperty("defaultDateFormat"));
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT =DateTimeFormat.forPattern(SwingObjProps.getApplicationProperty("defaultDateTimeFormat"));
 
     private static Map<Pattern,String> getPatterns() {
         String[] acceptabledateformatses = StringUtils.split(SwingObjProps.getApplicationProperty("acceptabledateformats"),"~");
         Map<Pattern,String> localPatterns=new HashMap<Pattern, String>();
         for(int i=0;i<acceptabledateformatses.length;i++){
             String[] split = acceptabledateformatses[i].trim().split("#");
             localPatterns.put(Pattern.compile(split[0].trim()),split[1].trim());
         }
         return localPatterns;
     }
 
 
     public static Date getDateFromFormatOfString(String date){
         try {
             for(Map.Entry<Pattern,String> entry : patterns.entrySet()){
                 if(entry.getKey().matcher(date).matches()){
                     SimpleDateFormat simpleDateFormat = new SimpleDateFormat(entry.getValue());
                     simpleDateFormat.setLenient(false);
                     return simpleDateFormat.parse(date);
                 }
             }
         } catch (ParseException e) {
             throw new InvalidDateException(date, e, DateUtils.class);
         }
         throw new InvalidDateException(date, DateUtils.class);
     }
 
     public static void getCurrentDateFromGoogle(){
         try{
             URLConnection urlConnection=new URL("http://www.google.com").openConnection();
             String date = urlConnection.getHeaderField("Date");
             System.out.println(date);
 
         }catch (Exception e){
             throw new SwingObjectRunException(e,DateUtils.class);
         }
     }
 
     public static String getStringFromDateDefaultFormat(Date date){
         if(date==null){
             return null;
         }
        return new DateTime(date).toString(DEFAULT_DATE_FORMAT);
    }

    public static String getStringFromDateTimeDefaultFormat(Date date){
        if(date==null){
            return null;
        }
        return new DateTime(date).toString(DEFAULT_DATE_TIME_FORMAT);
     }
 
 }
