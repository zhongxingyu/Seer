 package com.bitresolution.ledger.core.files;
 
 import com.bitresolution.ledger.core.ledger.Report;
import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.nio.CharBuffer;
 
 public class ReportFileReader implements Readable {
 
     private static final Logger log = LoggerFactory.getLogger(ReportFileReader.class);
     private static final DateTimeFormatter FILING_DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
     private static final DateTimeFormatter REPORT_PERIOD_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
 
     private final LineNumberReader reader;
 
     public ReportFileReader(File source) throws FileNotFoundException {
         reader = new LineNumberReader(new FileReader(source));
     }
 
     @Override
     public int read(CharBuffer charBuffer) throws IOException {
         return reader.read(charBuffer);
     }
 
     public Report readReport() throws IOException {
         Report report = new Report();
         State state = State.READING_METADATA;
         String line;
         while((line = reader.readLine()) != null) {
            log.trace("Line[{}] content: {}", reader.getLineNumber(), line);
             switch(state) {
                 case READING_METADATA:
                     state = processMetadata(report, state, line);
                     break;
                 case READ_COLUMN_MARKS:
                 case READING_ENTRIES:
                    if(StringUtils.isBlank(line)) {
                        log.debug("line[{}] is blank", reader.getLineNumber());
                        break;
                    }
                    if(line.startsWith("</TABLE>")) {
                        log.debug("line[{}] found entry data terminator", reader.getLineNumber());
                        state = State.READING_METADATA;
                        break;
                    }
                    log.debug("line[{}] parsing for entry data", reader.getLineNumber());
                     break;
             }
         }
         return report;
     }
 
     private State processMetadata(Report report, State state, String line) {
         LineType lineType = LineType.of(line);
         switch(lineType) {
             case PERIOD_OF_REPORT:
                 parsePeriodOfReport(line, report);
                 break;
 
             case REPORT_FILING_DATE:
                 parseFilingDate(line, report);
                 break;
 
             case COLUMN_MARKERS:
                 log.debug("line[{}] is type {}", new Object[]{reader.getLineNumber(), lineType.name()});
                 state = State.READ_COLUMN_MARKS;
                 log.debug("Switching state to {}", state.name());
                 break;
 
             case SKIPPABLE:
                 log.debug("line[{}] is type {}", new Object[]{reader.getLineNumber(), lineType.name()});
                 break;
         }
         return state;
     }
 
     private void parseFilingDate(String line, Report report) {
         String value = line.substring(line.lastIndexOf(":") + 1).trim();
         DateTime filingDate = FILING_DATE_FORMAT.parseDateTime(value);
         report.setFilingDate(filingDate);
         log.debug("Line [{}] - parsed 'filing date': {}", reader.getLineNumber(), filingDate);
     }
 
     private void parsePeriodOfReport(String line, Report report) {
         String value = line.substring(line.lastIndexOf(":") + 1).trim();
         DateTime periodOfReport = REPORT_PERIOD_FORMAT.parseDateTime(value);
         report.setPeriodOfReport(periodOfReport);
         log.debug("Line [{}] - parsed 'period of report': {}", reader.getLineNumber(), periodOfReport);
     }
 
     private static enum State {
         READING_METADATA,
         READ_COLUMN_MARKS,
         READING_ENTRIES
     }
 }
