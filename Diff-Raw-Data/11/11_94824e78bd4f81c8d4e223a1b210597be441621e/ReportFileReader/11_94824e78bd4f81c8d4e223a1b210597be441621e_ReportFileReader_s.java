 package com.bitresolution.ledger.core.files;
 
 import com.bitresolution.ledger.core.ledger.Entry;
 import com.bitresolution.ledger.core.ledger.Report;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.nio.CharBuffer;
 
 public class ReportFileReader implements Readable {
 
     private static final Logger log = LoggerFactory.getLogger(ReportFileReader.class);
 
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
         String line;
         while((line = reader.readLine()) != null) {
             LineType lineType = LineType.of(line);
             log.trace("line[{}] is type {}", new Object[]{reader.getLineNumber(), lineType.name()});
             switch(lineType) {
                 case PERIOD_OF_REPORT:
                     parsePeriodOfReport(line, lineType, report);
                     break;
 
                 case REPORT_FILING_DATE:
                     parseFilingDate(line, lineType, report);
                     break;
 
                 case ENTRY:
                 case SKIPPABLE:
                     break;
             }
         }
         return report;
     }
 
     private void parseFilingDate(String line, LineType type, Report report) {
         String value = line.substring(type.getPrefixLength()).trim();
        DateTime filingDate = new DateTime(value);
         report.setFilingDate(filingDate);
         log.debug("Line [{}] - parsed 'filing date': {}", reader.getLineNumber(), filingDate);
     }
 
     private void parsePeriodOfReport(String line, LineType type, Report report) {
         String value = line.substring(type.getPrefixLength()).trim();
        DateTime periodOfReport = new DateTime(value);
         report.setPeriodOfReport(periodOfReport);
         log.debug("Line [{}] - parsed 'period of report': {}", reader.getLineNumber(), periodOfReport);
     }
 }
