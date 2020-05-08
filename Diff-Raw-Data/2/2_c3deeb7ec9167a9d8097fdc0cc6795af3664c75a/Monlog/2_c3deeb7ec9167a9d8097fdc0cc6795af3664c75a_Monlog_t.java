 package se.liu.tdp024.util;
 
 import java.io.*;
 import java.net.*;
 import java.util.Calendar;
 
 public final class Monlog {
     private static boolean loggingOn = false;
 
     public static void setLoggingOn() { loggingOn = true; }
     public static void setLoggingOff() { loggingOn = false; }
 
     public static enum Severity {
         DEBUG,
         INFO,
         NOTIFY,
         WARNING,
         ERROR,
         CRITICAL,
         ALERT,
         EMERGENCY
     }
 
     public static Monlog getLogger() {
         final StackTraceElement[] methodCaller = Thread.currentThread().getStackTrace();
         Monlog logger = new Monlog(methodCaller[2].getClassName());
         logger.level = Severity.DEBUG;
         return logger;
     }
     
     public static Monlog getLogger(Severity level) {
         final StackTraceElement[] methodCaller = Thread.currentThread().getStackTrace();
         return new Monlog(methodCaller[2].getClassName());
     }
     
     public void setLevel(Severity level) {
         this.level = level;
     }
 
     private Monlog(String caller) {
         this.caller = caller;
         this.level = Severity.DEBUG;
     }
 
     private String caller;
     private Severity level;
     
     private static final String MONLOG_ENDPOINT = "http://www.ida.liu.se/~TDP024/monlog/api/log/";
     private static final String API_KEY = "423b0ef8aa9b0e030e785a63262c06c81d1beaa7";
     private static final String REQUEST_URL = MONLOG_ENDPOINT + "?api_key=" + API_KEY + "&format=json";
 
     private String buildResponse(Severity severity, String shortDesc, String longDesc) {
         StringBuilder dataBuilder = new StringBuilder();
         dataBuilder.append("{");
         dataBuilder.append("\"").append("severity").append("\":").append(severity.ordinal()).append(",");
         dataBuilder.append("\"").append("timestamp").append("\":\"").append(Calendar.getInstance().getTimeInMillis()).append("\",");
         dataBuilder.append("\"").append("short_desc").append("\":\"").append(shortDesc).append("\",");
         dataBuilder.append("\"").append("long_desc").append("\":\"").append(longDesc).append("\"");
         dataBuilder.append("}");
         // make newlines safe
         return dataBuilder.toString().replaceAll("\\\n", "\\\\n");
     }
     
     private String getStackTrace(Exception ex) {
             // http://stackoverflow.com/questions/1149703/stacktrace-to-string-in-java
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             ex.printStackTrace(pw);
             return sw.toString(); // stack trace as a string
     }
     
     /* Log wrappers to make sure correct calling method is logged. */
     public void log(Severity severity, String shortDescArg, String longDescArg) {
         log(severity, shortDescArg, longDescArg, null);
     }
     public void log(Severity severity, String shortDescArg, String longDescArg, Exception ex) {
         doLog(severity, shortDescArg, longDescArg, ex);
     }
     
     private void doLog(Severity severity, String shortDescArg, String longDescArg, Exception ex) {
        if (!loggingOn || severity.ordinal() < level.ordinal()) { return; }
 
         final StackTraceElement[] methodCaller = Thread.currentThread().getStackTrace();
         String methodName = methodCaller[3].getMethodName();
 
         String shortDescription = caller + "." + methodName + " - ";
         String longDescription = longDescArg;
         if (ex != null) {
             shortDescription += ex.toString() + " - ";
             
             longDescription = "\n\nException: " + ex.toString() + "\n";
             longDescription += "Message: " + ex.getMessage() + "\n\n";
             longDescription += "Stacktrace --------------------------------\n";
             longDescription += getStackTrace(ex);
         }
         shortDescription += shortDescArg;
         
         String json = buildResponse(severity, shortDescription, longDescription);
         try {
 
             URL url = new URL(REQUEST_URL);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.addRequestProperty("Content-Type", "application/json");
             connection.setDoOutput(true);
             connection.setConnectTimeout(60000);
             connection.setRequestMethod("POST");
 
             OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
             writer.write(json);
             writer.close();
 
             if (connection.getResponseCode() == HttpURLConnection.HTTP_OK || connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
 
                 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                 String line;
                 StringBuilder builder = new StringBuilder();
                 while ((line = reader.readLine()) != null) {
                     builder.append(line);
                 }
                 reader.close();
             } else {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                 String line;
                 StringBuilder builder = new StringBuilder();
                 while ((line = reader.readLine()) != null) {
                     builder.append(line);
                 }
                 reader.close();
             }
 
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
