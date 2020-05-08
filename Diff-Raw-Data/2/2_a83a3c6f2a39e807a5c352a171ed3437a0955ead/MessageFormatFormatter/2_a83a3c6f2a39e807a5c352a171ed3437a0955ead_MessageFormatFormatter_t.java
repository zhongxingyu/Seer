 package org.dss.mumbleIceConnector;
 
 import java.text.MessageFormat;
 import java.util.Date;
 import java.util.logging.Formatter;
 import java.util.logging.LogRecord;
 
 public class MessageFormatFormatter extends Formatter {
     
    private static final MessageFormat messageFormat = new MessageFormat("{0}[{1}|{2}|{3,date,dd/MM/yyyy HH:mm:ss.SSS}]: {4}{5}");
     
     public MessageFormatFormatter() {
         super();
     }
     
     @Override public String format(LogRecord record) {
         Object[] arguments = new Object[6];
         arguments[0] = record.getLoggerName();
         arguments[0] = ""; // as long as there's only 1 logger this just takes unnecessary space -atomy
         arguments[1] = String.format("%8s", record.getLevel());
         arguments[2] = String.format("%10s", Thread.currentThread().getName());
         arguments[3] = new Date(record.getMillis());
         arguments[4] = record.getMessage();
         arguments[5] = System.getProperty("line.separator");
         return messageFormat.format(arguments);
     }   
  
 }
