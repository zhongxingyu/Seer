 package uk.co.appembassy.log4mqtt;
 
 import org.apache.log4j.Layout;
 import org.apache.log4j.spi.LoggingEvent;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rad
  * Date: 29/09/2012
  * Time: 14:17
  * To change this template use File | Settings | File Templates.
  */
 public class JsonLoggingEventLayout extends Layout {
 
     private String hostname;
     private String ip;
 
     public void activateOptions() {
         try {
             hostname = InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException ex) {
             hostname = "<unknown>";
         }
         try {
             ip = InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException ex) {
             ip = "<unknown>";
         }
     }
 
     public boolean ignoresThrowable() { return true; }
 
     public String format(LoggingEvent event) {
         StringBuilder json = new StringBuilder();
         json.append("{");
         json.append("\"hostname\":");
         json.append("\"" + hostname + "\"");
         json.append(",\"ip\":");
         json.append("\"" + ip + "\"");
         json.append(",\"timestamp\":");
         json.append(event.getTimeStamp());
         json.append(",\"error_level_string\":");
         json.append("\"" + event.getLevel().toString() + "\"");
         json.append(",\"error_level_code\":");
         json.append(event.getLevel().toInt());
         json.append(",\"message\":");
         json.append("\"" + event.getMessage().toString().replaceAll("\"", "\\\\\"") + "\"");
         json.append(",\"fqn\":");
         json.append("\"" + event.getFQNOfLoggerClass().replaceAll("\"", "\\\\\"") + "\"");
         json.append(",\"logger_name\":");
         json.append("\"" + event.getLoggerName().replaceAll("\"", "\\\\\"") + "\"");
         if ( event.getNDC() != null ) {
             json.append(",\"ndc\":");
             json.append("\"" + event.getNDC().replaceAll("\"", "\\\\\"") + "\"");
         }
         if ( event.getRenderedMessage() != null ) {
             json.append(",\"rendered_message\":");
             json.append("\"" + event.getRenderedMessage().replaceAll("\"", "\\\\\"") + "\"");
         }
         if (event.locationInformationExists()) {
             json.append(",\"location_info\":");
             json.append("\"" + event.getLocationInformation().fullInfo.replaceAll("\"", "\\\\\"") + "\"");
         }
         if ( event.getThreadName() != null ) {
             json.append(",\"thread_name\":");
             json.append("\"" + event.getThreadName().replaceAll("\"", "\\\\\"") + "\"");
         }
         if ( event.getThrowableStrRep() != null ) {
             String[] throwable = event.getThrowableStrRep();
             if ( throwable.length > 0 ) {
                 json.append(",\"throwable\":[");
                 for ( int i=0; i<throwable.length; i++ ) {
                     if ( i > 0 ) json.append(",");
                     json.append("\"" + throwable[i].replaceAll("\"", "\\\\\"") + "\"");
                 }
                json.append("\"]\"");
             }
         }
 
         if ( event.getProperties() != null && event.getProperties().size() > 0 ) {
             json.append(",\"properties\":{");
             Iterator<String> iter = event.getProperties().keySet().iterator();
             int c = 0;
             while (iter.hasNext()) {
                 if ( c > 0 ) json.append(",");
                 String key = iter.next();
                 json.append("\"" + key.replaceAll("\"", "\\\\\"") + "\":");
                 json.append("\"" + event.getProperty(key).replaceAll("\"", "\\\\\"") + "\"");
             }
             json.append("}");
         }
 
         json.append("}");
         return json.toString();
     }
 
 }
