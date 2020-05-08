 package eu.europeana.uim.logging.mongo;
 
 import eu.europeana.uim.api.LogEntry;
 import eu.europeana.uim.api.LoggingEngine;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class MongoLogEntry<T extends Serializable> implements LogEntry<T> {
 
     private LoggingEngine.Level level;
     private Date date;
     private Long executionId, mdrId;
     private String pluginIdentifier;
     private T message;
 
     public LoggingEngine.Level getLevel() {
         return level;
     }
 
     public Date getDate() {
         return date;
     }
 
     public Long getExecutionId() {
         return executionId;
     }
 
     public String getPluginIdentifier() {
         return pluginIdentifier;
     }
 
     public Long getMetaDataRecordId() {
         return mdrId;
     }
 
     public T getMessage() {
         return message;
     }
 
     public MongoLogEntry(Date date, Long executionId, LoggingEngine.Level level, Long mdrId, T message, String pluginIdentifier) {
         this.date = date;
         this.executionId = executionId;
         this.level = level;
         this.mdrId = mdrId;
         this.message = message;
         this.pluginIdentifier = pluginIdentifier;
     }
 }
