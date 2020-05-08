 package com.od.jtimeseries.server;
 
 import com.od.jtimeseries.net.httpd.HttpRequestMonitor;
 import com.od.jtimeseries.source.Counter;
 import com.od.jtimeseries.source.ValueRecorder;
 import com.od.jtimeseries.source.impl.DefaultCounter;
 import com.od.jtimeseries.source.impl.DefaultValueRecorder;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 
 import java.net.Socket;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 27/06/11
  * Time: 06:37
  */
 public class ServerHttpRequestMonitor implements HttpRequestMonitor {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(ServerHttpRequestMonitor.class);
     private static volatile ValueRecorder htpRequestTimeValueRecorder = DefaultValueRecorder.NULL_VALUE_RECORDER;
    private static Counter requestCounter = DefaultCounter.NULL_COUNTER;
    private static Counter requestErrorCounter = DefaultCounter.NULL_COUNTER;
    private static Counter requestInvalidCounter;
 
     private ThreadLocal<Long> requestStartTimes = new ThreadLocal<Long>();
 
     public void requestStarting(long requestId, Socket mySocket) {
         logMethods.logDebug("Starting HTTPD request " + requestId + " from client " + mySocket.getInetAddress());
         requestCounter.incrementCount();
         requestStartTimes.set(System.currentTimeMillis());
     }
 
     public void servingRequest(long requestId, Socket mySocket, String uri, String method, Properties header, Properties params) {
         StringBuilder sb = new StringBuilder().append(
                 "Serving HTTPD request ").append(requestId).append(
                 " from client ").append(mySocket.getInetAddress()).append(
                 ", URI ").append(uri);
 
         if ( logMethods.getLogLevel().equalsOrExceeds(LogMethods.LogLevel.DEBUG)) {
             logProperties(header, sb, ", Header:");
         }
         logProperties(params, sb, ", Param:");
         logMethods.logInfo(sb.toString());
     }
 
     private void logProperties(Properties header, StringBuilder sb, String keyDescription) {
         sb.append(keyDescription);
         for ( Map.Entry<Object,Object> e : header.entrySet())  {
             sb.append("[").append(e.getKey()).append("=").append(e.getValue()).append("] ");
         }
     }
 
     public void finishedRequest(long requestId, Socket mySocket) {
         long timeTaken = logQueryTime();
         logMethods.logDebug("Finished HTTPD request " + requestId + " in " + timeTaken + " millis");
     }
 
     private long logQueryTime() {
         long startTime = requestStartTimes.get();
         long timeTaken = System.currentTimeMillis() - startTime;
         if ( htpRequestTimeValueRecorder != null) {
             htpRequestTimeValueRecorder.newValue(timeTaken);
         }
         return timeTaken;
     }
 
     public void exceptionDuringProcessing(long requestId, Socket mySocket, Throwable t) {
         requestErrorCounter.incrementCount();
         logMethods.logWarning("Error processing HTTPD request " + requestId, t);
     }
 
     public void invalidRequest(long requestId, Socket mySocket, String httpErrorType) {
         requestInvalidCounter.incrementCount();
         logMethods.logWarning("Processed invalid HTTPD request " + requestId + " " + httpErrorType);
     }
 
     public static void setHttpRequestTimeValueRecorder(ValueRecorder htpRequestTimeValueRecorder) {
         ServerHttpRequestMonitor.htpRequestTimeValueRecorder = htpRequestTimeValueRecorder;
     }
 
     public static void setHttpRequestCounter(Counter requestCounter) {
         ServerHttpRequestMonitor.requestCounter = requestCounter;
     }
 
     public static void setHttpRequestErrorCounter(Counter requestErrorCounter) {
         ServerHttpRequestMonitor.requestErrorCounter = requestErrorCounter;
     }
 
     public static void setHttpRequestInvalidCounter(Counter requestInvalidCounter) {
         ServerHttpRequestMonitor.requestInvalidCounter = requestInvalidCounter;
     }
 }
