 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tsdaggregator;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import org.apache.log4j.Logger;
 
 
 /**
  *
  * @author brandarp
  */
 public class FileListener implements AggregationListener {
 
     String _FileName = "";
     FileWriter _Writer;
     static final Logger _Logger = Logger.getLogger(FileListener.class);
    long _LinesWritten = 0;
 
     public FileListener(String file) {
         _FileName = file;
         try {
             _Writer = new FileWriter(_FileName, true);
         } catch (IOException ex) {
             _Logger.error("Could not open output file for writing: " + _FileName, ex);
             return;
         }
    }

    @Override
    public void recordAggregation(AggregatedData[] data) {
        
         StringBuilder sb = new StringBuilder();
         if (data.length > 0) {
             for (AggregatedData d : data) {
 
                 sb.append("{\"value\":\"").append(d.getValue().toString())
                         .append("\",\"counter\":\"").append(d.getMetric())
                         .append("\",\"service\":\"").append(d.getService())
                         .append("\",\"host\":\"").append(d.getHost())
                         .append("\",\"period\":\"").append(d.getPeriod())
                         .append("\",\"periodStart\":\"").append(d.getPeriodStart())
                         .append("\",\"statistic\":\"").append(d.getStatistic().getName())
                         .append("\"}\n");
                _LinesWritten++;
             }
         }
         try {
             _Writer.append(sb.toString());
         } catch (IOException ex) {
             _Logger.error("Error writing to output file", ex);
         }
     }
 
     @Override
     public void close() {
         try {
            _Logger.info("Closing after " + _LinesWritten + " lines written.");
             _Writer.flush();
             _Writer.close();
         } catch (IOException ex) {
             _Logger.error("Error writing to output file", ex);
         }
     }
 }
