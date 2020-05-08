 package nl.pvanassen.highchart.api.serializer;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import nl.pvanassen.highchart.api.format.DateTimeLabelFormats;
 import nl.pvanassen.highchart.api.format.DateTimeLabelFormats.TimeUnit;
 
 public class DateTimeLabelFormatsSerializer extends Serializer<DateTimeLabelFormats> {
 
     @Override
     public Map<String, String> getProperties( DateTimeLabelFormats instance ) {
         Map<TimeUnit, String> map = instance.getFormats();
         Map<String, String> r = new HashMap<String, String>();
         for ( Entry<TimeUnit, String> e : map.entrySet() ) {
            r.put( e.getKey().name(), e.getValue() );
         }
         return r;
     }
 
 }
