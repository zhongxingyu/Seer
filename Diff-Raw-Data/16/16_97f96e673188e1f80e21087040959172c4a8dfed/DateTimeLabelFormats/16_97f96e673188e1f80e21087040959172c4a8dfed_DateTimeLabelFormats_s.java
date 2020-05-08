 package nl.pvanassen.highchart.api.format;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import nl.pvanassen.highchart.api.base.BaseObject;
 
 public class DateTimeLabelFormats extends BaseObject {
     public enum TimeUnit {
        second, minute, hour, day, week, month, year;
     }
 
     private Map<TimeUnit, String> formats;
 
     public DateTimeLabelFormats() {
         formats = null;
     }
 
     public String getFormat( TimeUnit unit ) {
         return ( formats != null ) ? formats.get( unit ) : null;
     }
 
     public Map<TimeUnit, String> getFormats() {
         return formats;
     }
 
     public DateTimeLabelFormats set( TimeUnit unit, String format ) {
         if ( formats == null ) {
             formats = new HashMap<TimeUnit, String>();
         }
         formats.put( unit, format );
         return this;
     }
 
 }
