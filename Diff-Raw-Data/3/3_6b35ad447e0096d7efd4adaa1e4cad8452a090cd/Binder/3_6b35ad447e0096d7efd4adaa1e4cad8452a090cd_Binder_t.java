 package devkit.utils;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import play.mvc.Http;
 
 public class Binder {
 
 	public static <T> T bind (String stringValue, Class<T> clazz){
 		
 		return bind(stringValue, clazz, null);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T bind (String stringValue, Class<T> clazz, String options){
 
         if (clazz.isEnum()){
             Object[] values = clazz.getEnumConstants();
             for (Object o : values){
                 if (o.toString().equals(stringValue)){
                     return (T) o;
                 }
             }
         }
 		else if (String.class.equals(clazz)){
 
 			return (T) stringValue;
 		}
 		else if (Integer.class.equals(clazz)){
 
 			return (T) asInteger(stringValue, 10);
 		}
 		else if (Long.class.equals(clazz)){
 
 			return (T) asLong(stringValue, 10);
 		}
 		else if (BigDecimal.class.equals(clazz)){
 
 			return (T) asBigDecimal(stringValue);
 		}
 		else if (Boolean.class.equals(clazz)){
 
 			return (T) asBoolean(stringValue);
 		}
 		else if (Date.class.equals(clazz)){
 
 			return (T) asDate(stringValue, options);
 		}
 		
 		return null;
 	}
 	
 	public static <T> T[] bind(String[] stringValues, Class<T> clazz){
 		return bind(stringValues, clazz, null);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T[] bind(String[] stringValues, Class<T> clazz, String options){
 
 		if (stringValues == null){
 			return null;
 		}
  
 		List<T> bindValues = new ArrayList<T>();
 		for (int i=0;i<stringValues.length;i++){
 			
 			bindValues.add(bind(stringValues[i], clazz, options));
 		}
 		
 		return (T[]) bindValues.toArray((T[]) java.lang.reflect.Array.newInstance(clazz, 0));
 	}
 	
 	public static Integer asInteger(String stringValue, int radix){
 		
 		Integer value = null;
 		try {
 			value = Integer.parseInt(stringValue, radix);
 		} catch (Exception ex) {}
 		return value;
 	}
 	
 	public static Long asLong(String stringValue, int radix){
 		
 		Long value = null;
 		try {
 			value = Long.parseLong(stringValue, radix);
 		} catch (Exception ex) {}
 		return value;
 	}
 	
 	public static BigDecimal asBigDecimal(String stringValue){
 		
 		BigDecimal value = null;
 		try {
 			value = new BigDecimal(stringValue);
 		} catch (Exception ex) {}
 		return value;
 	}
 
 	public static BigDecimal asBigDecimal(String stringValue, int scale){
 		
 		BigDecimal value = asBigDecimal(stringValue);
 		try {
 			value = value.setScale(scale);
 		} catch (Exception e) {}
 		
 		return value;
 	}
 
 	public static BigDecimal asBigDecimal(String stringValue, int scale, RoundingMode roundingMode){
 		
 		BigDecimal value = asBigDecimal(stringValue);
 		try {
 			value = value.setScale(scale, roundingMode);
 		} catch (Exception e) {}
 		return value;
 	}
 	
 	public static Boolean asBoolean(String stringValue){
 		
 		Boolean value = null;
 		try {
 			value = Boolean.parseBoolean(stringValue);
 		} catch (Exception ex) {}
 		return value;
 	}
 	
 	public static Date asDate(String stringValue, String pattern){
 		return asDate(stringValue, pattern, Http.Context.Implicit.lang().toLocale());
 	}
 	
 	public static Date asDate(String stringValue, String pattern, Locale locale){
 		
 		Date date = null;
 		try {
 			SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
 	        sdf.setLenient(false);
 	        date = sdf.parse(stringValue);
 		} catch (Exception ex){}
 		
         return date;
 	}
 
     public static DateTime asDateTime(String stringValue, String pattern){
         return asDateTime(
             stringValue, DateTimeFormat.forPattern(pattern)
         );
     }
 
     public static DateTime asDateTime(String stringValue, DateTimeFormatter formatter){
        if (stringValue == null){
            return null;
        }
         return formatter.parseDateTime(stringValue);
     }
 }
