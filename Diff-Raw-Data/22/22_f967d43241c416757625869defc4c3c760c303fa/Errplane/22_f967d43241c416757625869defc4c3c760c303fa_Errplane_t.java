 package com.errplane.api;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.SocketException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Pattern;
 
 import com.errplane.http.HTTPPostHelper;
 import com.errplane.util.ExceptionHelper;
 import com.errplane.util.MergedReports;
 import com.errplane.util.ReportHelper;
 
 /**
  * This class is the entrypoint for the Errplane library.  Use it to report
  * data to Errplane.
  *
  * @author gdix
  *
  */
 public class Errplane {
 
   private static final String EXCEPTIONS_TIMESERIES = "exceptions";
 
   private static final Pattern METRIC_NAME_REGEX = Pattern.compile("^[a-zA-Z0-9._]+$");
 
   private static AtomicInteger reportCount = new AtomicInteger(0);
 
   private static AtomicInteger reportCapacity = new AtomicInteger(10000);
 
   private static AtomicInteger breadcrumbCapacity = new AtomicInteger(10);
 
   private static ConcurrentLinkedQueue<ReportHelper> reportQueue =
     new ConcurrentLinkedQueue<ReportHelper>();
 
   private static ConcurrentLinkedQueue<String> breadcrumbQueue =
     new ConcurrentLinkedQueue<String>();
 
   private static URL errplaneUrl;
 
   private static String urlStr = "https://w.apiv3.errplane.com/databases/";
 
   private static String udpHost = "udp.apiv3.errplane.com";
 
   private static DatagramSocket udpSocket;
 
   private static String app;
 
   private static String api;
 
   private static String environment;
 
   private static String hostname;
 
   private static int udpPort = 8126;
 
   private static String database;
 
   private static InetSocketAddress address;
 
   /**
    * This method initializes Errplane so it is ready to send data.
    * @param appKey the application key.
    * @param apiKey the api key.
    * @param env the environment (development, staging, or production).
    * @return true if initialization succeeded.
    */
   public static boolean init(String appKey, String apiKey, String env) {
 
     if ((appKey == null) || (apiKey == null) || (env == null)) {
       return false;
     }
 
     try {
       hostname = InetAddress.getLocalHost().getHostName();
     } catch (UnknownHostException e) {
       return false;
     }
     app = appKey;
     api = apiKey;
     environment = env;
 
     return initUrl() && initUdpConnection();
   }
 
   public static boolean setUrl(String url) {
     urlStr = url;
     return initUrl();
   }
 
   /**
    *
    * @param host
    * @return
    */
   public static boolean setUdpHostAndPort(String host, int port) {
     udpHost = host;
     udpPort = port;
     return initUdpConnection();
   }
 
   private static boolean initUdpConnection() {
     try {
       if (udpSocket != null) {
         udpSocket.close();
         udpSocket = null;
       }
 
       address = new InetSocketAddress(udpHost, udpPort);
       udpSocket = new DatagramSocket();
       udpSocket.connect(address);
 
     } catch (SocketException e) {
       e.printStackTrace();
       return false;
     }
     return true;
   }
 
   private static boolean initUrl() {
     try {
       database = app+environment;
       errplaneUrl = new URL((urlStr+database+"/points?api_key="+api));
     } catch (MalformedURLException e) {
       e.printStackTrace();
       return false;
     }
     return true;
   }
 
   public static int getCount() {
     return reportCount.get();
   }
 
   public static void setReportCapacity(int capacity) {
     reportCapacity.set(capacity);
   }
 
   public static void setBreadcrumbCapacity(int capacity) {
     breadcrumbCapacity.set(capacity);
   }
 
   /**
      The user associated with the current Errplane session.
 
      @param sessUser the session user to be sent with exception details.
   */
   public static synchronized void setSessionUser(String sessUser) {
   }
 
   /**
    * Converts the passed in bytes to a hex String
    * @param bytes the bytes to convert to hex
    * @return converted hex String
    */
   public static String getHex(byte[] bytes) {
     final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
     char[] hexChars = new char[bytes.length * 2];
     int v;
     for ( int j = 0; j < bytes.length; j++ ) {
       v = bytes[j] & 0xFF;
       hexChars[j * 2] = hexArray[v >>> 4];
       hexChars[j * 2 + 1] = hexArray[v & 0x0F];
     }
     return new String(hexChars);
   }
 
   /**
      Try to clear any outstanding Errplane reports.  Returns the number of
      reports sent.
   */
   public static synchronized int flush() {
     int numRemoved = 0;
     if (!reportQueue.isEmpty() && (errplaneUrl != null)) {
       try {
         List<ReportHelper> httpReports = new ArrayList<ReportHelper>();
         List<ReportHelper> udpReports = new ArrayList<ReportHelper>();
         List<ReportHelper> udpAggregates = new ArrayList<ReportHelper>();
         List<ReportHelper> udpSums = new ArrayList<ReportHelper>();
 
         while (!reportQueue.isEmpty() && (numRemoved < 200)) {
           ReportHelper rh = reportQueue.remove();
           numRemoved++;
 
           // System.out.printf("type: %s, report type: %s\n", rh.getType(), rh.getReportType());
 
           switch (rh.getType()) {
           case HTTP:
             httpReports.add(rh);
             break;
           case UDP:
             if (rh.getReportType().equals("r"))
               udpReports.add(rh);
             else if (rh.getReportType().equals("t"))
               udpAggregates.add(rh);
             else if (rh.getReportType().equals("c"))
               udpSums.add(rh);
             else
               throw new IllegalArgumentException("Invalid report type " + rh.getReportType());
             break;
           default:
             throw new IllegalArgumentException("Unkown type " + rh.getType());
           }
         }
 
         if (httpReports.size() > 0) {
           sendHttpReport(httpReports);
         }
         if (udpReports.size() > 0) {
           sendUdpReport(udpReports);
         }
         if (udpAggregates.size() > 0) {
           sendUdpReport(udpAggregates);
         }
         if (udpSums.size() > 0) {
           sendUdpReport(udpSums);
         }
       }
       catch (Exception e) {
         e.printStackTrace();
       }
 
       if (numRemoved > 0) {
         reportCount.addAndGet(-numRemoved);
       }
     }
     return numRemoved;
   }
 
   private static void sendHttpReport(List<ReportHelper> rhs) {
     try {
       int bytesWritten = 0;
       HTTPPostHelper postHelper = new HTTPPostHelper();
       String rptBody = new MergedReports(rhs).getReportBody();
       bytesWritten += rptBody.length() + 1;
       OutputStream os = postHelper.getOutputStream(errplaneUrl, bytesWritten);
       os.write(rptBody.getBytes());
       os.write('\n');
       if (!postHelper.sendPost(bytesWritten)) {
         System.out.println("Cannot send tick to server");
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   private static void sendUdpReport(List<ReportHelper> rhs) {
     byte[] payloadString = new MergedReports(rhs).getReportBody().getBytes();
     try {
       DatagramPacket packet = new DatagramPacket(payloadString,payloadString.length);
       udpSocket.send(packet);
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   /**
      Leave a trail indicating what might have lead to an Exception.  The last 10 are sent
      with exception details.  If pushing a breadcrumb on the queue when it already has
      10 breadcrumbs, the oldest will be popped off the back of the queue.
 
      @param bc the meaningful breadcrumb to push on the queue.
   */
   public static void breadcrumb(String bc) {
     breadcrumbQueue.add(bc);
   }
 
   /**
    * Convenience method for creating a ExceptionData object for use in an
    * Exception report.
    * @param u the user agent that initiated the Exception
   * @param m the method name, this can be any string that describe the nature of the request that was being processed
    * @return a new ExceptionData object to pass along to the Exception report
    */
  public static ExceptionData getExceptionData(String u, String m) {
    return new ExceptionData("", "", u, m);
  }

  /**
   * Convenience method for creating a ExceptionData object for use in an
   * Exception report.
   * @deprecated use {@link #getExceptionData(String, String)} instead
   * @param c the controller where the Exception occurred (this is deprecated use m instead)
   * @param a the action where the Exception occurred (this is deprecated use m instead)
   * @param m the method name, this can be any string that describe the nature of the request that was being processed
   * @return a new ExceptionData object to pass along to the Exception report
   */
  @Deprecated
   public static ExceptionData getExceptionData(String c, String a, String u) {
    return new ExceptionData(c, a, u, "");
   }
 
   private static void addReportHelper(String name, double val, String context, Map<String, String> dimensions) {
     ReportHelper rh = new ReportHelper(name, "", ReportHelper.ReportType.HTTP, database, api);
     rh.setReportValue(val);
     rh.setContext(context);
     rh.setDimensions(dimensions);
     reportQueue.add(rh);
     reportCount.addAndGet(1);
   }
 
   /**
      Posts a datapoint with a default int value of 1 to the timeline[s] specified.
      @param name the name[s] of the timeline[s] to post the data point to.
      @return false if Errplane was not previously initialized or the name exceeds 249 characters.
   */
   public static boolean report(String name) {
     return report(name, 1.0, null, null);
   }
 
   /**
    * throws an {@link IllegalArgumentException} if the metricName is longer than 255 characters
    * or if the metric name contains characters other than alphanumeric characters, period or
    * underscore.
    *
    * @param metricName the name to validate
    */
   public static boolean verifyMetricName(String metricName) {
     if (metricName == null) {
       System.err.println("Metric name cannot be null");
       return false;
     }
 
     if (metricName.length() > 255) {
       System.err.println("Metric names cannot be longer than 255 characters");
       return false;
     }
 
     if (!METRIC_NAME_REGEX.matcher(metricName).matches()) {
       System.err.println("Invalid metric name " + metricName + ". See docs for valid metric names");
       return false;
     }
 
     return true;
   }
 
   /**
      Posts a datapoint with the value specified to the timeline[s] specified.
      @param name the name[s] of the timeline[s] to post the data point to.
      @param value the int value to post to the timeline.
      @return false if Errplane was not previously initialized or the name exceeds 249 characters.
      @deprecated use {@link #report(String,double)} instead
   */
   @Deprecated
   public static boolean report(String name, int value) {
     return report(name, value, null, null);
   }
 
   /**
      Posts a datapoint with the value specified to the timeline[s] specified.
      @param name the name[s] of the timeline[s] to post the data point to.
      @param value the double value to post to the timeline.
      @return false if Errplane was not previously initialized or the name exceeds 249 characters.
   */
   public static boolean report(String name, double value) {
     return report(name, value, null, null);
   }
 
   /**
      Posts a datapoint with a default int value of 1 and a context to the timeline[s] specified.
      @param name the name[s] of the timeline[s] to post the data point to.
      @param context the context to post along with the datapoint to the timeline.
      @return false if Errplane was not previously initialized or the name exceeds 249 characters.
   */
   public static boolean report(String name, String context) {
     return report(name, 1, context, null);
   }
 
   /**
      Posts a datapoint with the int value and a context to the timeline[s] specified.
      @param name the name[s] of the timeline[s] to post the data point to.
      @param value the int value to post to the timeline[s].
      @param context the context to post along with the datapoint to the timeline.
      @return false if Errplane was not previously initialized or the name exceeds 249 characters.
      @deprecated use {@link #report(String,double,String)} instead
   */
   @Deprecated
   public static boolean report(String name, int value, String context) {
     return report(name,value,context, null);
   }
 
   /**
      Posts a datapoint with a default int value of 1 and a context to the name[s] specified.
      @param name the name[s] of the timeline[s] to post the data point to.
      @param value the double value to post to the timeline[s].
      @param context the context to post along with the datapoint to the timeline.
      @return false if Errplane was not previously initialized or the name exceeds 249 characters.
   */
   public static boolean report(String name, double value, String context) {
     return report(name, value, context, null);
   }
 
   /**
      Posts a datapoint with a value, context and dimensions
   */
   public static boolean report(String name, double value, String context, Map<String, String> dimensions) {
     if (!verifyMetricName(name)) {
       return false;
     }
 
     addReportHelper(name,value,context, dimensions);
 
     return true;
   }
 
 
   /**
    * see {@link #aggregate(String, double, String, Map)}
    *
    * @param name
    * @param value
    * @return
    */
   public static boolean aggregate(String name, double value) {
     return aggregate(name, value, null, null);
   }
 
   /**
    * see {@link #aggregate(String, double, String, Map)}
    *
    * @param name
    * @param value
    * @param context
    * @return
    */
   public static boolean aggregate(String name, double value, String context) {
     return aggregate(name, value, context, null);
   }
 
   /**
    * Similar to report, but aggregate the values (mean, count, etc.)
    *
    * @param name
    * @param value
    * @param context
    * @param dimensions
    * @return
    */
   public static boolean aggregate(String name, double value, String context, Map<String, String> dimensions) {
     return sendUdpCommon("t", name, value, context, dimensions);
   }
 
 
   /**
    * see {@link #sum(String, double, String, Map)}
    *
    * @param name
    * @param value
    * @return
    */
   public static boolean sum(String name, double value) {
     return sum(name, value, null, null);
   }
 
   /**
    * see {@link #sum(String, double, String, Map)}
    *
    * @param name
    * @param value
    * @param context
    * @return
    */
   public static boolean sum(String name, double value, String context) {
     return sum(name, value, context, null);
   }
 
   /**
    * Similar to report, but increment a counter
    *
    * @param name
    * @param value
    * @param context
    * @param dimensions
    * @return
    */
   public static boolean sum(String name, double value, String context, Map<String, String> dimensions) {
     return sendUdpCommon("c", name, value, context, dimensions);
   }
 
   private static boolean sendUdpCommon(String reportType, String name, double value, String context, Map<String, String> dimensions) {
     if (!verifyMetricName(name)) {
       return false;
     }
 
     // example
     // {"a":"962cdc9b-15e7-4b25-9a0d-24a45cfc6bc1","d":"app4you2lovestaging","o":"t","w":[{"n":"some_aggregate","p":[{"c":"","d":null,"v":30.091186058528706}]}]}
 
     ReportHelper rh = new ReportHelper(name, reportType, ReportHelper.ReportType.UDP, database, api);
     rh.setReportValue(value);
     rh.setContext(context);
     rh.setDimensions(dimensions);
     reportQueue.add(rh);
     reportCount.addAndGet(1);
 
     return true;
   }
 
   /**
      Posts an exception using either the default hash method or the overridden one (if provided).
      @param ex the exception to report.
      @param exData the additional exception data to be sent with the report
      @return false if Errplane was not previously initialized.
   */
   public static boolean reportException(Exception ex, ExceptionData exData) {
     return reportException(ex, null, exData);
   }
 
 
 
   /**
      Posts an exception using either the default hash method or the overridden one (if provided).
      @param ex the exception to report.
      @param hash the overridden hash to use rather than the default.
      @param customData the NSString to place in the custom_data section of the exception detail
      reporting to Errplane.
      @param exData the additional exception data to be sent with the report
      @return false if Errplane was not previously initialized.
   */
   public static boolean reportException(Exception ex, String customData, ExceptionData exData) {
     if (ex == null) {
       return false;
     }
 
     ExceptionHelper helper = new ExceptionHelper(ex, exData, breadcrumbQueue.toArray(), customData, hostname);
 
     return report(EXCEPTIONS_TIMESERIES, 1.0, helper.createExceptionContext(), helper.createExceptionDimensions());
   }
 
   /**
    * Starts a timer using the specified name for recording duration of
    * executed code.
    * @param name the time series name.
    * @return true if Errplane reported successfully.
    */
   public static TimerTask startTimer(String name) {
     if (!verifyMetricName(name)) {
       return null;
     }
     return (new TimerTask(name));
   }
 }
