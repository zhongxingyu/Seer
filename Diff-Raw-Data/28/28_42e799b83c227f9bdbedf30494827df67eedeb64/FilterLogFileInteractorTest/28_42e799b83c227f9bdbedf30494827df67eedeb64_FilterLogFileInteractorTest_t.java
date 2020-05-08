 package com.crudetech.sample.logcrunch;
 
 import com.crudetech.sample.logcrunch.FilterLogFileInteractor.LogLevelFilterBuilder;
 import com.crudetech.sample.logcrunch.FilterLogFileInteractor.SearchIntervalFilterBuilder;
 import com.crudetech.sample.logcrunch.http.ParameterFilterQuery;
 import org.joda.time.Interval;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class FilterLogFileInteractorTest {
     @Rule
     public InMemoryTestLogFile logFileStub1 = new InMemoryTestLogFile("machine101-20090610");
     @Rule
     public InMemoryTestLogFile logFileStub2 = new InMemoryTestLogFile("machine101-20090611");
     private LogFileLocator locator;
     private LogFileNamePattern logFileNamePattern;
     private FilterLogFileInteractor logFileInteractor;
 
     @Before
     public void setUp() throws Exception {
         logFileNamePattern = mock(LogFileNamePattern.class);
         locator = mock(LogFileLocator.class);
         logFileInteractor = new FilterLogFileInteractor(
                 locator,
                 asList(new LogLevelFilterBuilder(), new SearchIntervalFilterBuilder()));
     }
 
     @Test
     public void findFiles() {
         Interval testInterval = new Interval(1, 5);
         setupLocator(asList(testInterval), logFileStub1, logFileStub2);
         setupLocator(logFileStub1, logFileStub2);
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.setLogFileNamePattern(logFileNamePattern);
         request.addSearchInterval(testInterval);
 
         logFileInteractor.getFilteredLines(request, new CollectingFilterResultStub());
 
         verify(locator).find(logFileNamePattern, asList(testInterval));
     }
 
     private void setupLocator(Iterable<Interval> intervals, LogFile... logFiles) {
         when(locator.find(logFileNamePattern, intervals)).thenReturn(asList(logFiles));
     }
 
     @SuppressWarnings("unchecked")
     private void setupLocator(LogFile... logFiles) {
         when(locator.find(eq(logFileNamePattern), any(Iterable.class))).thenReturn(asList(logFiles));
     }

     private void setupLocatorThatFindsNothing() {
         setupLocator();
     }
 
 
     @Test
     public void levelFiltersAreApplied() {
         setupLocator(logFileStub1, logFileStub2);
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.addSearchInterval(allTimeOfTheWorld());
         request.setLogFileNamePattern(logFileNamePattern);
         request.addLevel(LogLevel.Info);
         request.addLevel(LogLevel.Warn);
 
         CollectingFilterResultStub lineCollector = new CollectingFilterResultStub();
         logFileInteractor.getFilteredLines(request, lineCollector);
 
 
         List<LogLine> expectedLines = new ArrayList<LogLine>();
         expectedLines.addAll(asList(TestLogFile.SampleInfoLine, logFileStub1.logLines.get(2), logFileStub1.logLines.get(3)));
         expectedLines.addAll(asList(TestLogFile.SampleInfoLine, logFileStub2.logLines.get(2), logFileStub2.logLines.get(3)));
 
         assertThat(lineCollector.collectedLines, is(expectedLines));
     }
 
     private Interval allTimeOfTheWorld() {
         return new Interval(0, Long.MAX_VALUE);
     }
 
 
     private static class CollectingFilterResultStub implements FilterLogFileInteractor.FilterResult {
         private final List<LogLine> collectedLines = new ArrayList<LogLine>();
         private boolean noFilesFound = false;
         public boolean noLinesFound = false;
 
         @Override
         public void filteredLogLine(LogLine line) {
             collectedLines.add(line);
         }
 
         @Override
         public void noFilesFound() {
             noFilesFound = true;
         }
 
         @Override
         public void noLinesFound() {
             noLinesFound = true;
         }
     }
 
     @Test
     public void timeFiltersAreApplied() {
         setupLocator(logFileStub1);
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.setLogFileNamePattern(logFileNamePattern);
         request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));
 
 
         CollectingFilterResultStub lineCollector = new CollectingFilterResultStub();
         logFileInteractor.getFilteredLines(request, lineCollector);
 
         assertThat(lineCollector.collectedLines, is(asList(TestLogFile.SampleInfoLine)));
     }
 
     @Test
    public void givenNoLogFileFound_errorMessageIsSend() {
         setupLocatorThatFindsNothing();
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.setLogFileNamePattern(logFileNamePattern);
         request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));
 
         CollectingFilterResultStub lineCollector = new CollectingFilterResultStub();
         logFileInteractor.getFilteredLines(request, lineCollector);
 
         assertThat(lineCollector.noFilesFound, is(true));
     }
 
     @Test
     public void givenNothingFoundInsideLogFiles_messageIsSend() throws Exception {
         setupLocator(logFileStub1);
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.setLogFileNamePattern(logFileNamePattern);
         request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));
         request.addLevel(LogLevel.Debug);
 
         CollectingFilterResultStub lineCollector = new CollectingFilterResultStub();
         logFileInteractor.getFilteredLines(request, lineCollector);
 
         assertThat(lineCollector.noLinesFound, is(true));
     }

    @Test
    public void givenNoLogFileFound_noNoLinesFoundIsSend() {
        setupLocatorThatFindsNothing();

        ParameterFilterQuery request = new ParameterFilterQuery();
        request.setLogFileNamePattern(logFileNamePattern);
        request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));

        CollectingFilterResultStub lineCollector = new CollectingFilterResultStub();
        logFileInteractor.getFilteredLines(request, lineCollector);

        assertThat(lineCollector.noLinesFound, is(false));
    }

     @Test
     public void givenNothingFoundInsideLogFiles_logFileIsClosed() throws Exception {
         setupLocator(logFileStub1);
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.setLogFileNamePattern(logFileNamePattern);
         request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));
         request.addLevel(LogLevel.Debug);
 
         CollectingFilterResultStub lineCollector = new CollectingFilterResultStub();
         logFileInteractor.getFilteredLines(request, lineCollector);
 
         assertThat(logFileStub1.closed, is(true));
     }
 
     @Test
     public void logFilesAreClosed() {
         setupLocator(logFileStub1);
 
         ParameterFilterQuery request = new ParameterFilterQuery();
         request.setLogFileNamePattern(logFileNamePattern);
         request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));
 
 
         CollectingFilterResultStub filterResult = new CollectingFilterResultStub();
         logFileInteractor.getFilteredLines(request, filterResult);
 
         assertThat(logFileStub1.closed, is(true));
     }
 }
