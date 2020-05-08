 package org.logtools.core.domain.log4jimpl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.logtools.core.domain.LogDocument;
 import org.logtools.core.domain.LogEntry;
 
 public class Log4jLogDocument implements LogDocument {
 
     private static final Integer DEFAULT_SIZE = 100;
     private Vector<LogEntry> logs;
 
     public Log4jLogDocument() {
         this(DEFAULT_SIZE);
     }
 
     public Log4jLogDocument(Integer size) {
         logs = new Vector<LogEntry>(size);
     }
 
     public Iterator<LogEntry> iterator() {
         return new WapperLogIterator(this.logs.iterator());
     }
 
     /**
      * just disable the remove function;
     * ii will delegate other methods.
      * 
      * @author Chandler.Song
      * 
      */
     private class WapperLogIterator implements Iterator<LogEntry> {
         private Iterator<LogEntry> iter;
 
         public WapperLogIterator(Iterator<LogEntry> iter) {
             this.iter = iter;
         }
 
         public boolean hasNext() {
             return this.iter.hasNext();
         }
 
         public LogEntry next() {
             return this.iter.next();
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
 
         }
     }
 
     public void addLine(LogEntry le) {
         this.logs.add(le);
     }
 
     public LogEntry findLog(int lineNumber) {
         return this.logs.get(lineNumber);
     }
 
     public Integer size() {
         return this.logs.size();
     }
 
     public List<LogEntry> listAllEntry() {
         return new ArrayList<LogEntry>(this.logs);
     }
 
 }
