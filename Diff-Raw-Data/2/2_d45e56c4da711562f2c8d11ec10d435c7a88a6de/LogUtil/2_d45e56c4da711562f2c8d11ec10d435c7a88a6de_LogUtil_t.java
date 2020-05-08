 package fr.ybonnel.codestory.util;
 
 import fr.ybonnel.codestory.WebServer;
 import fr.ybonnel.codestory.WebServerResponse;
 import fr.ybonnel.codestory.database.DatabaseManager;
 import fr.ybonnel.codestory.database.modele.LogMessage;
 
 import javax.servlet.http.HttpServletRequest;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.Map;
 
 import static com.google.common.collect.Maps.newHashMap;
 
 public class LogUtil {
 
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
 
     /**
      * Log the request and specific informations.
      */
     public static void logHttpRequest(Date startTime, HttpServletRequest request, String payLoad, long timeWithNetwork, long timeWithoutNetwork, WebServerResponse response) {
         String query = request.getParameter(WebServer.QUERY_PARAMETER);
         if (query != null && query.startsWith("log")
                 || "/favicon.ico".equals(request.getPathInfo())
                 || request.getPathInfo().endsWith(".png")) {
             return;
         }
 
 
         StringBuilder logMessage = new StringBuilder();
         logMessage.append('\t');
         logMessage.append(request.getMethod());
         logMessage.append("\n\tPath info : ")
                 .append(request.getPathInfo());
         logMessage.append("\n\tRequest parameters : ")
                 .append(convertParametersMap(request));
         logMessage.append("\n\tRemote adress : ")
                 .append(request.getRemoteAddr());
         logMessage.append("\n\tRequest headers : ")
                 .append(getRequestHeaders(request));
         if (response.getSpecificLog() == null) {
             logMessage.append("\n\tRequest payload : ")
                     .append(payLoad);
         }
         logMessage.append("\n\tResponse status : ").append(response.getStatusCode());
         logMessage.append("\n\tResponse time with network: ").append(NumberFormat.getInstance(Locale.FRANCE).format(timeWithNetwork)).append("ns");
         logMessage.append("\n\tResponse time without network: ").append(NumberFormat.getInstance(Locale.FRANCE).format(timeWithoutNetwork)).append("ns");
         if (response.getSpecificLog() == null) {
             logMessage.append("\n\tResponse : ").append(response.getResponse());
         } else {
            logMessage.append("\n\tSpecific : ").append(response.getSpecificLog());
         }
 
         DatabaseManager.INSTANCE.getLogDao().insert(new LogMessage(startTime, DatabaseManager.TYPE_Q, logMessage.toString()));
 
         logMessage(logMessage.toString());
     }
 
     /**
      * Get useful headers of request.
      */
     private static Map<String, String> getRequestHeaders(HttpServletRequest request) {
         Map<String, String> headersMap = newHashMap();
         if (request.getHeader("Accept") != null) {
             headersMap.put("Accept", request.getHeader("Accept"));
         }
         if (request.getHeader("Content-Type") != null) {
             headersMap.put("Content-Type", request.getHeader("Content-Type"));
         }
         return headersMap;
     }
 
     /**
      * Get the content of all request parameters.
      */
     private static String convertParametersMap(HttpServletRequest request) {
         StringBuilder builder = new StringBuilder();
         Enumeration<?> parameters = request.getParameterNames();
         while (parameters.hasMoreElements()) {
             String parameter = (String) parameters.nextElement();
             builder.append("\n\t\tparameter(").append(parameter).append("={");
             builder.append(request.getParameter(parameter)).append('}');
         }
         return builder.toString();
     }
 
     /**
      * Log the url called.
      */
     public static void logRequestUrl(HttpServletRequest request) {
         StringBuilder logRequest = new StringBuilder();
         logRequest.append(request.getMethod()).append(':');
         logRequest.append(request.getRequestURL());
         if (request.getQueryString() != null) {
             logRequest.append('?').append(request.getQueryString());
         }
         logMessage(logRequest.toString());
     }
 
     /**
      * Log a message.
      */
     public static void logMessage(String message) {
         StringBuilder logContent = new StringBuilder(DATE_FORMAT.format(new Date()));
         System.out.println(logContent.append(':').append(message).toString());
     }
 }
