 /*
  * ValueUtil.java
  *
  * Created on June 21, 2010, 3:24 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.util;
 
 import com.rameses.common.PropertyResolver;
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author compaq
  */
 public class ValueUtil {
     
     private static final Pattern ESC_STRING = Pattern.compile("[\\\\$\"\n\t\r]");
     private static final SimpleDateFormat DT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TS_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
     private static final DecimalFormat DEC_FORMATTER = new DecimalFormat("###0.00");
     
     
     public static final boolean isStringValueEqual(Object obj1, Object obj2) {
         if ( obj1 == null && obj2 == null)
             return true;
         if ( obj1 == null && obj2 != null )
             return false;
         if ( obj1 != null && obj2 == null )
             return false;
         
         return (obj1.toString()).equals(obj2.toString());
     }
     
     
     public static final boolean isEqual(Object obj1, Object obj2) {
         if ( obj1 == null && obj2 == null)
             return true;
         if ( obj1 == null && obj2 != null )
             return false;
         if ( obj1 != null && obj2 == null )
             return false;
         
         return obj1.equals(obj2);
     }
     
     public static final boolean isEqual(Object obj1, Object obj2, String keyFld, PropertyResolver resolver) {
         if ( obj1 == null && obj2 == null)
             return true;
         if ( obj1 == null && obj2 != null )
             return false;
         if ( obj1 != null && obj2 == null )
             return false;
         if( isEmpty(keyFld) || resolver == null )
             return obj1.equals(obj2);
         
         try {
             Object key1 = resolver.getProperty(obj1, keyFld);
             Object key2 = resolver.getProperty(obj2, keyFld);
             
             return isEqual(key1, key2);
         } catch(Exception e) {
             e.printStackTrace();
         }
         
         return false;
     }
     
     public static final boolean isEmpty(Object obj) {
         if ( obj == null)                return true;
         if ( obj instanceof Map )        return ((Map) obj).isEmpty();
         if ( obj instanceof Collection ) return ((Collection) obj).isEmpty();
         if ( obj.getClass().isArray() )   return ((Object[]) obj).length == 0;
         
         return obj.toString().trim().length() == 0;
     }
     
     public static String getValueAsString(Class type, Object value) {
         if (value == null) {
             //do nothing
         } else if (value instanceof String) {
             return escape(value.toString()).insert(0, "\"").append("\"").toString();
         }
         //if value is double, float or BigDecimal, format it as ###0.00
         else if(type == Double.class || type == double.class ||
                 type == float.class || type == Float.class ||
                 type == BigDecimal.class) {
             return DEC_FORMATTER.format( value );
         } else if(type == long.class || type == Long.class ||
                 type == int.class || type == Integer.class ||
                 type == Boolean.class || type == boolean.class ) {
             return value+"";
         } else if (value instanceof Timestamp) {
             return "\"" + TS_FORMATTER.format(value) + "\"";
         } else if (value instanceof Date) {
             return "\"" + DT_FORMATTER.format(value) + "\"";
         }
         
         return "null";
     }
     
     public static StringBuffer escape(String str) {
         StringBuffer sb = new StringBuffer();
         Matcher m = ESC_STRING.matcher(str);
         while( m.find() ) {
             char match = m.group(0).charAt(0);
             String rep = null;
             switch(match) {
                 case '\n': rep = "\\\\n"; break;
                 case '\r': rep = "\\\\r"; break;
                 case '\t': rep = "\\\\t"; break;
                 case '\\': rep = "\\\\\\\\"; break;
                 case '"' : rep = "\\\\\""; break;
                 case '$' : rep = "\\\\\\$"; break;
             }
             
             m.appendReplacement(sb, rep);
         }
         m.appendTail(sb);
         
         return sb;
     }
     
     public static String repeat(String val, int repeat ) {
         if(val==null) val = " ";
         StringBuffer sb = new StringBuffer();
         for(int i=0; i<repeat;i++) {
             sb.append( val );
         }
         return sb.toString();
     }
     
     
     
 }
