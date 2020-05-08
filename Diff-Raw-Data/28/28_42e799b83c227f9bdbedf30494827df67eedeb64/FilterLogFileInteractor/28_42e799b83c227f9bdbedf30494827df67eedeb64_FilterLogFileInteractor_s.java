 package com.crudetech.sample.logcrunch;
 
 import com.crudetech.sample.filter.BinaryFunction;
 import com.crudetech.sample.filter.FilterChain;
 import com.crudetech.sample.filter.MappingIterable;
 import com.crudetech.sample.filter.PredicateBuilder;
 import com.crudetech.sample.filter.UnaryFunction;
 import org.joda.time.Interval;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import static com.crudetech.sample.filter.Algorithm.accumulate;
 import static com.crudetech.sample.logcrunch.LogLinePredicates.hasLogLevel;
 import static com.crudetech.sample.logcrunch.LogLinePredicates.isInDateTimeRange;
 
 public class FilterLogFileInteractor {
     private final LogFileLocator locator;
     private final Iterable<FilterBuilder> filterBuilders;
 
     public static interface FilterQuery {
         LogFileNamePattern getLogFileNamePattern();
 
         List<Interval> getSearchIntervals();
 
         List<LogLevel> getLevels();
 
         List<Pattern> getLoggers();
 
         List<Pattern> getMessageRegex();
     }
 
     public static interface FilterResult {
         void filteredLogLine(LogLine line);
 
         void noFilesFound();
 
         void noLinesFound();
     }
 
     public static interface FilterBuilder {
         PredicateBuilder<LogLine> build(FilterQuery filterQuery, PredicateBuilder<LogLine> filterBuilder);
     }
 
     public FilterLogFileInteractor(LogFileLocator locator, Collection<? extends FilterBuilder> filterBuilders) {
         this.filterBuilders = new ArrayList<FilterBuilder>(filterBuilders);
         this.locator = locator;
     }
 
     public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
         boolean noFilesFound = true;
         boolean noLinesFound = true;
         for (LogFile filteredLogFile : getFilteredLogFiles(filterQuery)) {
             for (LogLine logLine : filteredLogFile.getLines()) {
                 filterResult.filteredLogLine(logLine);
                 noLinesFound = false;
             }
             filteredLogFile.close();
             noFilesFound = false;
         }
         if (noFilesFound) {
             filterResult.noFilesFound();
        }
        if (noLinesFound) {
             filterResult.noLinesFound();
         }
     }
 
 
     private Iterable<LogFile> getFilteredLogFiles(FilterQuery filterQuery) {
         Iterable<LogFile> logFiles = locator.find(filterQuery.getLogFileNamePattern(), filterQuery.getSearchIntervals());
 
         FilterChain<LogLine> lineFilter = new FilterChain<LogLine>();
         PredicateBuilder<LogLine> filterBuilder = lineFilter.filterBuilder();
 
         accumulate(filterBuilder, filterBuilders, applyFilterBuilder(filterQuery));
 
         return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
     }
 
     private BinaryFunction<PredicateBuilder<LogLine>, PredicateBuilder<LogLine>, FilterBuilder> applyFilterBuilder(final FilterQuery filterQuery) {
         return new BinaryFunction<PredicateBuilder<LogLine>, PredicateBuilder<LogLine>, FilterBuilder>() {
             @Override
             public PredicateBuilder<LogLine> evaluate(PredicateBuilder<LogLine> predicateBuilder, FilterBuilder filterBuilder) {
                 return filterBuilder.build(filterQuery, predicateBuilder);
             }
         };
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
 
         @Override
         public void print(PrintWriter w) {
             logFile.print(w);
         }
 
     }
 
     static class SearchIntervalFilterBuilder implements FilterBuilder {
         @Override
         public PredicateBuilder<LogLine> build(FilterQuery filterQuery, PredicateBuilder<LogLine> filterBuilder) {
             Iterable<Interval> searchIntervals = filterQuery.getSearchIntervals();
             if (!searchIntervals.iterator().hasNext()) {
                 return filterBuilder;
             }
             filterBuilder.andOpenBrace();
             for (Interval searchInterval : searchIntervals) {
                 filterBuilder.or(isInDateTimeRange(searchInterval));
             }
             filterBuilder.closeBrace();
             return filterBuilder;
         }
 
     }
 
     static class LogLevelFilterBuilder implements FilterBuilder {
         @Override
         public PredicateBuilder<LogLine> build(FilterQuery filterQuery, PredicateBuilder<LogLine> filterBuilder) {
             List<LogLevel> levels = filterQuery.getLevels();
             if (!levels.iterator().hasNext()) {
                 return filterBuilder;
             }
             filterBuilder.openBrace();
             for (LogLevel level : levels) {
                 filterBuilder.or(hasLogLevel(level));
             }
             filterBuilder.closeBrace();
             return filterBuilder;
         }
 
     }
 }
