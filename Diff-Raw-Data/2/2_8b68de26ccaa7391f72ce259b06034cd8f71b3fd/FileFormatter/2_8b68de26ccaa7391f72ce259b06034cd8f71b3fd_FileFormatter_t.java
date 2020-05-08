 package org.avrbuddy.log;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Formatter;
 import java.util.logging.LogRecord;
 
 /**
  * @author Roman Elizarov
  */
 class FileFormatter extends Formatter {
     @Override
     public String format(LogRecord record) {
        String time = new SimpleDateFormat("yyyyMMdd HHmmss.SSS").format(new Date(record.getMillis()));
         String name = record.getLoggerName();
         int i = name.lastIndexOf('.');
         if (i > 0)
             name = name.substring(i + 1);
         String str = formatFullMessage(record);
         return  time + " " + record.getLevel() + " {" + name + "} " + str;
     }
 
     private String formatFullMessage(LogRecord record) {
         if (record.getThrown() == null)
             return String.format("%s%n", formatMessage(record));
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         pw.println(formatMessage(record));
         record.getThrown().printStackTrace(pw);
         pw.close();
         return sw.toString();
     }
 }
