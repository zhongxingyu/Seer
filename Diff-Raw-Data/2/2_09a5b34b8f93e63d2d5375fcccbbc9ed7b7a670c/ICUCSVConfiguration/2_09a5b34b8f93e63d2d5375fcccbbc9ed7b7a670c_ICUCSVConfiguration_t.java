 package de.jpaw.bonaparte.core;
 
 import java.util.Locale;
 
 import com.ibm.icu.util.ULocale;
 
 import de.jpaw.bonaparte.core.CSVConfiguration.Builder;
 
 public class ICUCSVConfiguration extends CSVConfiguration {
 
     public final ULocale ulocale; // the ICU locale replacement (597 instead of 159 available Locales)
 
     public ICUCSVConfiguration(String separator, Character quote, String quoteReplacement, String ctrlReplacement, boolean datesQuoted, boolean removePoint4BD,
             String mapStart, String mapEnd, String arrayStart, String arrayEnd, String objectStart, String objectEnd, String booleanTrue, String booleanFalse,
             Locale locale, CSVStyle dateStyle, CSVStyle timeStyle, String customDayFormat, String customTimestampFormat, String customTimestampWithMsFormat,
             String customCalendarFormat, ULocale ulocale) {
         super(separator, quote, quoteReplacement, ctrlReplacement, datesQuoted, removePoint4BD, mapStart, mapEnd, arrayStart, arrayEnd, objectStart, objectEnd,
                 booleanTrue, booleanFalse, locale, dateStyle, timeStyle, customDayFormat, customTimestampFormat, customTimestampWithMsFormat,
                customCalendarFormat, false);
         this.ulocale = ulocale;
     }
 
     /** Creates a new CSVConfiguration.Builder based on the current object. */
     public Builder builder() {
         return new Builder(this);
     }
 
     /** Builder for the configuration */
     public static class Builder extends CSVConfiguration.Builder {
         private ULocale ulocale; // used to determine date format
 
         protected final void xferBaseClass(ICUCSVConfiguration cfg) {
             super.xferBaseClass(cfg);
             this.ulocale = cfg.ulocale;
         }
 
         /** Creates a new CSVConfiguration.Builder with default settings. */
         public Builder() {
             xferBaseClass(CSV_DEFAULT_CONFIGURATION);
             this.ulocale = ULocale.US;
         }
 
         /** Creates a new CSVConfiguration.Builder from some existing configuration. */
         public Builder(ICUCSVConfiguration cfg) {
             xferBaseClass(cfg);
         }
 
         /** Creates a new CSVConfiguration.Builder (as a factory method). */
         public static Builder from(ICUCSVConfiguration cfg) {
             return new Builder(cfg);
         }
 
         /** Constructs the CSVConfiguration from the data collected so far */
         public ICUCSVConfiguration build() {
             return new ICUCSVConfiguration(separator, quote, quoteReplacement, ctrlReplacement, datesQuoted, removePoint4BD, mapStart, mapEnd, arrayStart,
                     arrayEnd, objectStart, objectEnd, booleanTrue, booleanFalse, locale, dateStyle, timeStyle, customDayFormat, customTimestampFormat,
                     customTimestampWithMsFormat, customCalendarFormat, ulocale);
         }
 
         // now the individual builder setters follow
         public Builder forULocale(ULocale ulocale) {
             this.ulocale = ulocale;
             return this;
         }
 
     }
 
 }
