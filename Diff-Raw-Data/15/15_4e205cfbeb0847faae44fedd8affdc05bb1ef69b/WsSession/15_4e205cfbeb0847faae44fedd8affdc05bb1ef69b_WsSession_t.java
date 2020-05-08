 package info.dt.srv;
 
 import flexjson.JSONDeserializer;
 import info.dt.data.IDateConfig;
 import info.dt.data.TimeSheet;
 import info.dt.report.IReportView;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.eclipse.jetty.websocket.WebSocket;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.joda.time.ReadableInterval;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Strings;
 import com.google.common.collect.Sets;
 
 @Slf4j
 class WsSession extends Thread implements WebSocket.OnTextMessage {
 
   @Inject
   private IJsonSerializer serializer;
 
   @Inject
   private IDateConfig iDateConfig;
 
   @Inject
   private IReportView report;
 
   private Connection connection = null;
 
   private Set<String> idsOnClient = Sets.newHashSet();
 
   private ReadableInterval interval = null;
 
   public void onOpen(Connection connection) {
     this.connection = connection;
     connection.setMaxTextMessageSize(Integer.MAX_VALUE);
     start();
 
   }
 
   protected void send(String string) {
     if (!Strings.isNullOrEmpty(string)) {
       int textMessageLength = string.length();
       int maxTextMessageLength = connection.getMaxTextMessageSize();
       if (maxTextMessageLength > 0 && textMessageLength > maxTextMessageLength) {
 
         throw new IllegalStateException("Too mutch text " + textMessageLength + " > "
             + maxTextMessageLength);
       }
       try {
         connection.sendMessage(string);
       } catch (IOException e) {
         throw new IllegalStateException(e);
       }
     }
   }
 
   @Override
   public void run() {
     while (!isInterrupted()) {
       try {
         ReadableInterval currentInterval;
         if (interval != null) {
           currentInterval = interval;
         } else {
 
           currentInterval = report.getCurrentInterval();
         }
 
         TimeSheet timeSheet = iDateConfig.getTimeSheet(currentInterval);
         send(serializer.toJson(timeSheet, idsOnClient, currentInterval));
       } catch (RuntimeException e) {
         log.error("", e);
         // TODO send error; format?
       }
       try {
         synchronized (this) {
           wait(20000);
         }
       } catch (InterruptedException e) {
         interrupt();
       }
     }
   }
 
   public void onClose(int closeCode, String message) {
     connection = null;
     log.warn("connection closed {}", message);
     interrupt();
   }
 
   public void onMessage(String jsonString) {
     JSONDeserializer<Map<String, Object>> deserializer = new JSONDeserializer<Map<String, Object>>();
     Map<String, Object> requestMap = deserializer.deserialize(jsonString);
 
     maintainIdsOnClient(Optional.fromNullable((List<String>) requestMap.get("ids")));
 
     String startString = (String) requestMap.get("start");
     String endString = (String) requestMap.get("end");
     Optional<String> start = Optional.fromNullable(startString);
     Optional<String> end = Optional.fromNullable(endString);
     updateInterval(start, end);
 
     synchronized (this) {
 
       notify();
     }
 
   }
 
   private void updateInterval(Optional<String> start, Optional<String> end) {
     if (start.isPresent() && end.isPresent()) {
       String startString = start.get();
       String endString = end.get();
       try {
         if (interval != null) {
           Duration duration = interval.toDuration();
           duration = duration.plus(1000);
           if ("next".equals(startString) && startString.equals(endString)) {
             interval = new Interval(interval.getStart().plus(duration) //
                 , interval.getEnd().plus(duration));
           } else if ("prev".equals(startString) && startString.equals(endString)) {
             interval = new Interval(interval.getStart().minus(duration) //
                 , interval.getEnd().minus(duration));
 
           } else {
            parseInterval(startString, endString);
           }
         } else {
          parseInterval(startString, endString);
         }
         idsOnClient.clear();
         idsOnClient.add("force reload");
       } catch (Exception e) {
         log.error("", e);
       }
 
     }
   }
 
  private void parseInterval(String startString, String endString) {
    try {
      interval = Interval.parse(startString + "T00:00:00/" + endString + "T23:59:59"); // XXX
    } catch (IllegalArgumentException e) {
      log.warn("{}", e.getMessage());
      interval = Interval.parse(startString + "T00:00:00/" + startString + "T23:59:59");
    }
  }

   private void maintainIdsOnClient(Optional<List<String>> idsOnClientOpt) {
 
     if (idsOnClientOpt.isPresent()) {
       Set<String> possibleIds = Sets.newHashSet(idsOnClientOpt.get());
       idsOnClient = validateIds(possibleIds);
     }
   }
 
   private Set<String> validateIds(Set<String> possibleIds) {
     for (String id : possibleIds) {
       if (id.length() != 40) {
         throw new IllegalArgumentException(id);
       }
     }
     return possibleIds;
   }
 
 }
