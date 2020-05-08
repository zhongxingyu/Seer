 package iumfs;
 
 import java.util.logging.Formatter;
 import java.util.logging.LogRecord;
 
 public final class SingleLineLogFormatter extends Formatter {
 
     /**
      * Setting log format
      */    
     @Override
     public synchronized String format(final LogRecord rec) {
        StringBuilder line =  new StringBuilder();
         
         line.append(String.format("%tD %<tT.%<tL", rec.getMillis()));
         line.append(" ");
         line.append(rec.getThreadID());
         line.append(" ");
         line.append(rec.getLevel().toString());
         line.append(" ");
         line.append(rec.getSourceClassName());
         line.append(".");
         line.append(rec.getSourceMethodName());
         line.append(": ");        
         line.append(rec.getMessage());
         line.append("\n");
         
         return line.toString();
     }
 }
