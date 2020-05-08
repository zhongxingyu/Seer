 package com.moviepilot.sheldon.compactor.custom;
 
 import com.moviepilot.sheldon.compactor.ModMapModifier;
 import com.moviepilot.sheldon.compactor.event.PropertyContainerEvent;
 import com.moviepilot.sheldon.compactor.handler.PropertyContainerEventHandler;
 import com.moviepilot.sheldon.compactor.util.Progressor;
 import com.moviepilot.sheldon.compactor.util.ProgressorHolder;
 import gnu.trove.map.TObjectLongMap;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 
 /**
  * @author stefanp
  * @since 08.08.12
  */
 public abstract class SheldonPreprocessor<E extends PropertyContainerEvent>
         implements PropertyContainerEventHandler<E>, ProgressorHolder, ModMapModifier {
 
     private Progressor progressor;
 
     private final DateTimeFormatter formatter;
 
 
     protected SheldonPreprocessor() {
         // 2011-06-17T17:08:58+02:00
         formatter = new DateTimeFormatterBuilder()
                 .appendYear(4, 4)
                 .appendLiteral('-')
                 .appendMonthOfYear(2)
                 .appendLiteral('-')
                 .appendDayOfMonth(2)
                 .appendLiteral('T')
                 .appendClockhourOfDay(2)
                 .appendLiteral(':')
                 .appendMinuteOfHour(2)
                 .appendLiteral(':')
                 .appendSecondOfMinute(2)
                 .appendTimeZoneOffset(null, true, 2, 2).toFormatter();
     }
 
     public final void onEvent(final E event, final long sequence, final boolean endOfBatch) throws Exception {
         try {
             if (event.isOk()) {
                 if (event.props.containsKey(SheldonConstants.EXTERNAL_ID_KEY)) {
                     final Object value  = event.props.get(SheldonConstants.EXTERNAL_ID_KEY);
                     final Long newValue = value instanceof Long ? ((Long)value) : Long.parseLong(value.toString());
                     event.props.put(SheldonConstants.EXTERNAL_ID_KEY, newValue);
                 }
                 convertTimestamp(event, SheldonConstants.CREATED_AT);
                 convertTimestamp(event, SheldonConstants.UPDATED_AT);
                 convertTimestamp(event, SheldonConstants.PUBLISHED_AT);
                 onOkEvent(event, sequence, endOfBatch);
             }
         }
         catch (Exception e) {
             System.err.println("Error in " + getClass());
             Toolbox.printException(e);
             throw e;
         }
     }
 
     private void convertTimestamp(final E event,final String propertyName) {
         if (event.props.containsKey(propertyName)) {
             final Object value = event.props.get(propertyName);
             if (value instanceof String) {
                 try {
                     final long timeVal = parseRubyTimeLong(value.toString());
                     event.props.put(propertyName, timeVal);
                     progressor.tick(propertyName);
                 }
                 catch (IllegalArgumentException e) {
                     System.err.println("Error parsing timestamp for " + propertyName + " = " + value);
                 }
             }
         }
     }
 
     public final long parseRubyTimeLong(final String value) {
         return formatter.parseMillis(value) / 1000;
     }
 
     public final String formatRubyTime(final long seconds) {
         return new DateTime(seconds * 1000L).toString(formatter);
     }
 
     protected abstract void onOkEvent(final E event, final long sequence, final boolean endOfBatch) throws Exception;
 
     public Progressor getProgressor() {
         return progressor;
     }
 
     public void setProgressor(final Progressor progressor) {
         this.progressor = progressor;
     }
 
     public void modifyMap(final TObjectLongMap<String> modMap) {
         modMap.put(SheldonConstants.CREATED_AT, 20000);
         modMap.put(SheldonConstants.UPDATED_AT, 20000);
         modMap.put(SheldonConstants.PUBLISHED_AT, 20000);
     }
 }
