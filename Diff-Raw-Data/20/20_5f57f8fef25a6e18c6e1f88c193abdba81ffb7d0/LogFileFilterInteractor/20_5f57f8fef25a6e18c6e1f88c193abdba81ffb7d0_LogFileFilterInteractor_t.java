 package com.crudetech.sample.logcrunch;
 
 import com.crudetech.sample.filter.FilterChain;
 import com.crudetech.sample.filter.MappingIterable;
 import com.crudetech.sample.filter.PredicateBuilder;
 import com.crudetech.sample.filter.UnaryFunction;
 import org.joda.time.Interval;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import static com.crudetech.sample.logcrunch.LogLinePredicates.hasLogLevel;
 import static com.crudetech.sample.logcrunch.LogLinePredicates.isInDateTimeRange;
 
 public class LogFileFilterInteractor {
     private final LogFileLocator locator;
     public static interface Query {
         LogFileNamePattern getLogFileNamePattern();
         List<Interval> getSearchIntervals();
         List<LogLevel> getLevels();
         List<Pattern> getLoggers();
         List<Pattern> getMessageRegex();
     }
 
     public LogFileFilterInteractor(LogFileLocator locator) {
         this.locator = locator;
     }
 
     public Iterable<LogFile> getFilteredLogFiles(Query model) {
         Iterable<LogFile> logFiles = locator.find(model.getLogFileNamePattern(), model.getSearchIntervals());
 
         FilterChain<LogLine> lineFilter = new FilterChain<LogLine>();
         PredicateBuilder<LogLine> filterBuilder = lineFilter.filterBuilder();
 
         // At this point, the open closed principle is hurt!
        // to add more filters, we have to change this class.
         // For more information, google for "open closed principle", OCP,
         // SOLID principles and Bob Martin
        buildLogLevelFilter(model.getLevels(), filterBuilder);
        buildTimeIntervalFilter(model.getSearchIntervals(), filterBuilder);

 
         return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
     }
 
     private void buildTimeIntervalFilter(Iterable<Interval> searchIntervals, PredicateBuilder<LogLine> filterBuilder) {
         if (!searchIntervals.iterator().hasNext()) {
             return;
         }
         filterBuilder.andOpenBrace();
         for (Interval searchInterval : searchIntervals) {
             filterBuilder.or(isInDateTimeRange(searchInterval));
         }
         filterBuilder.closeBrace();
     }
 
     private static void buildLogLevelFilter(Iterable<LogLevel> levels, PredicateBuilder<LogLine> filterBuilder) {
         if (!levels.iterator().hasNext()) {
             return;
         }
         filterBuilder.openBrace();
         for (LogLevel level : levels) {
             filterBuilder.or(hasLogLevel(level));
         }
         filterBuilder.closeBrace();
     }
 
     private UnaryFunction<LogFile, LogFile> filterFiles(final FilterChain<LogLine> filters) {
         return new UnaryFunction<LogFile, LogFile>() {
             @Override
             public LogFile evaluate(LogFile logFile) {
                 return new FilterLogFile(logFile, filters);
             }
         };
     }
 
     private static class FilterLogFile implements LogFile {
         private final LogFile logFile;
         private final FilterChain<LogLine> filterChain;
 
         public FilterLogFile(LogFile logFile, FilterChain<LogLine> filterChain) {
             this.logFile = logFile;
             this.filterChain = filterChain;
         }
 
         @Override
         public Iterable<LogLine> getLines() {
             return filterChain.apply(logFile.getLines());
         }
 
         @Override
         public void close() {
             logFile.close();
         }
     }
 }
