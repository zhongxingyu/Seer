 /*
  * Created On:  31-Aug-07 11:53:42 AM
  */
 package com.thinkparity.codebase;
 
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 /**
  * <b>Title:</b>thinkParity Common Time Format<br>
  * <b>Description:</b>Format/localize a duration.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class TimeFormat {
 
     /** The base format (displaying total milliseconds). */
     private static MessageFormat BASE_FORMAT;
 
     /** A localization bundle. */
     private static final ResourceBundle BUNDLE;
 
     /** The format (fractional units). */
     private static MessageFormat FORMAT;
 
     static {
         BUNDLE = ResourceBundle.getBundle("localization/TimeFormat_Messages");
         BASE_FORMAT = new MessageFormat("{0,number,0} {1}");
         FORMAT = new MessageFormat("{0,number,0.0} {1}");
     }
 
     /**
      * Create TimeFormat.
      *
      */
     public TimeFormat() {
         super();
     }
 
     /**
      * Format a duration.
      * 
      * @param duration
      *            A <code>Long</code>.
      * @return A <code>String</code>.
      */
     public String format(final Long duration) {
         return format(Unit.AUTO, duration);
     }
 
     /**
      * Format a duration.
      * 
      * @param unit
      *            A <code>Unit</code>.
      * @param duration
      *            A <code>Long</code>.
      * @return A <code>String</code>.
      */
     public String format(final Unit unit, final Long duration) {
         final MessageFormat format;
         final Object[] arguments = new Object[2];
         switch (unit) {
         case AUTO:
             if (duration >= (1000L * 60L * 60L * 24L)) {
                 return format(Unit.DAYS, duration);
             } else if (duration >= (1000L * 60L * 60L)) {
                 return format(Unit.HOURS, duration);
             } else if (duration >= (1000L * 60L)) {
                 return format(Unit.MINUTES, duration);
             } else if (duration >= 1000L) {
                 return format(Unit.SECONDS, duration);
             } else {
                 return format(Unit.MILLISECONDS, duration);
             }
         case DAYS:
             format = FORMAT;
            arguments[0] = Float.valueOf((float) duration / 1000F / 60F / 60F / 24F);
             arguments[1] = BUNDLE.getString("Days");
             break;
         case HOURS:
             format = FORMAT;
            arguments[0] = Float.valueOf((float) duration / 1000F / 60F / 60F);
             arguments[1] = BUNDLE.getString("Hours");
             break;
         case MILLISECONDS:
             format = BASE_FORMAT;
            arguments[0] = Float.valueOf((float) duration);
             arguments[1] = BUNDLE.getString("MilliSeconds");
             break;
         case MINUTES:
             format = FORMAT;
            arguments[0] = Float.valueOf((float) duration / 1000F / 60F);
             arguments[1] = BUNDLE.getString("Minutes");
             break;
         case SECONDS:
             format = FORMAT;
            arguments[0] = Float.valueOf((float) duration / 1000F);
             arguments[1] = BUNDLE.getString("Seconds");
             break;
         default:
             throw Assert.createUnreachable("Unknown time format unit {0}.", unit);
         }
         return format.format(arguments);
     }
 
     /** <b>Title:</b>Time Format Unit<br> */
     public enum Unit {
         AUTO, DAYS, HOURS, MILLISECONDS, MINUTES, SECONDS
     }
 }
