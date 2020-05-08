 package org.lantern.exceptional4j;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.commons.io.FileSystemUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHeaders;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.Level;
 import org.apache.log4j.Priority;
 import org.apache.log4j.spi.LocationInfo;
 import org.apache.log4j.spi.LoggingEvent;
 import org.apache.log4j.spi.ThrowableInformation;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  * Log4J appender that sends data to Exceptional.
  */
 public class ExceptionalAppender extends AppenderSkeleton {
 
     private final Collection<Bug> recentBugs = 
         Collections.synchronizedSet(new LinkedHashSet<Bug>());
     
     private final ExecutorService pool = Executors.newSingleThreadExecutor();
     
     private final ExceptionalAppenderCallback callback;
 
     private final boolean threaded;
 
     private final String apiKey;
 
     private final Priority reportingLevel;
 
     private final boolean active;
 
     /**
      * Creates a new appender.
      * 
      * @param apiKey Your API key.
      */
     public ExceptionalAppender(final String apiKey) {
         this(apiKey, Level.WARN);
     }
     
     /**
      * Creates a new appender with the specified minimum log level to report.
      * 
      * @param apiKey Your API key.
      * @param reportingLevel The log4j level to report errors at. Anything 
      * logged at the specified level or above will be reported. If you set
      * the reportingLevel to Level.WARN, for example, all warn level logs will
      * be sent along with any more severe logs such as ERROR and FATAL.
      */
     public ExceptionalAppender(final String apiKey, 
         final Priority reportingLevel) {
         this(apiKey, new ExceptionalAppenderCallback() {
             public boolean addData(final JSONObject json, final LoggingEvent le) {return true;}
         }, reportingLevel);
     }
 
     /**
      * Creates a new appender with callback.
      * 
      * @param apiKey Your API key.
      * @param callback The class to call for modifications prior to submitting
      * the bug.
      */
     public ExceptionalAppender(final String apiKey, 
         final ExceptionalAppenderCallback callback) {
         this(apiKey, callback, true, Level.WARN);
     }
     
     /**
      * Creates a new appender with callback.
      * 
      * @param apiKey Your API key.
      * @param callback The class to call for modifications prior to submitting
      * the bug.
      * @param reportingLevel The log4j level to report errors at. Anything 
      * logged at the specified level or above will be reported. If you set
      * the reportingLevel to Level.WARN, for example, all warn level logs will
      * be sent along with any more severe logs such as ERROR and FATAL.
      */
     public ExceptionalAppender(final String apiKey, 
         final ExceptionalAppenderCallback callback, 
         final Priority reportingLevel) {
         this(apiKey, callback, true, reportingLevel);
     }
     
     /**
      * Creates a new appender with a flag for whether or not to thread 
      * submissions. Not threading can be useful for testing in particular.
      * 
      * @param apiKey Your API key.
      * @param threaded Whether or not to thread submissions to Exceptional.
      */
     public ExceptionalAppender(final String apiKey, final boolean threaded) {
         this(apiKey, new ExceptionalAppenderCallback() {
             public boolean addData(final JSONObject json, final LoggingEvent le) {return true;}
         }, threaded, Level.WARN);
     }
     
     /**
      * Creates a new appender with callback.
      * 
      * @param apiKey Your API key.
      * @param callback The class to call for modificatios prior to submitting
      * the bug.
      * @param threaded Whether or not to thread submissions to Exceptional.
      * @param reportingLevel The log4j level to report errors at. Anything 
      * logged at the specified level or above will be reported. If you set
      * the reportingLevel to Level.WARN, for example, all warn level logs will
      * be sent along with any more severe logs such as ERROR and FATAL.
      */
     public ExceptionalAppender(final String apiKey, 
         final ExceptionalAppenderCallback callback,
         final boolean threaded, final Priority reportingLevel) {
         this.apiKey = apiKey;
         this.callback = callback;
         this.threaded = threaded;
         this.reportingLevel = reportingLevel;
         if (this.apiKey.equals(ExceptionalUtils.NO_OP_KEY)) {
             this.active = false;
         } else {
             this.active = true;
         }
     }
 
     @Override
     public void append(final LoggingEvent le) {
         // Only submit the bug under certain conditions.
         if (!active) {
             System.out.println("Exceptional reporting is not active");
             return;
         }
         if (submitBug(le)) {
             // Just submit it to the thread pool to avoid holding up the calling
             // thread.
             if (threaded) {
                 this.pool.submit(new BugRunner(le));
             } else {
                 new BugRunner(le).run();
             }
         } 
     }
 
     private boolean submitBug(final LoggingEvent le) {
         // Ignore plain old logs.
         if (!le.getLevel().isGreaterOrEqual(this.reportingLevel)) {
             return false;
         }
 
         final LocationInfo li = le.getLocationInformation();
         final Bug lastBug = new Bug(li);
         if (recentBugs.contains(lastBug)) {
             // Don't send duplicates. This should be configurable, but we
             // want to avoid hammering the server.
             return false;
         }
         synchronized (this.recentBugs) {
             // Remove the oldest bug.
             if (this.recentBugs.size() >= 200) {
                 final Bug lastIn = this.recentBugs.iterator().next();
                 this.recentBugs.remove(lastIn);
             }
             recentBugs.add(lastBug);
             return true;
         }
     }
 
     public void close() {
     }
 
     public boolean requiresLayout() {
         return false;
     }
     
 
     private final class BugRunner implements Runnable {
 
         private final LoggingEvent loggingEvent;
 
         private BugRunner(final LoggingEvent le) {
             this.loggingEvent = le;
         }
 
         public void run() {
             try {
                 submitBug(this.loggingEvent);
             } catch (final Throwable t) {
                 System.err.println("Error submitting bug: " + t);
             }
         }
 
         private void submitBug(final LoggingEvent le) {
             System.err.println("Starting to submit bug...");
 
             final JSONObject json = new JSONObject();
             json.put("request", requestData(le));
            json.put("application_environment", appData(le));
             json.put("exception", exceptionData(le));
             json.put("client", clientData(le));
            final String jsonStr = json.toJSONString();
            System.out.println("JSON:\n"+jsonStr);
            if (callback.addData(json, this.loggingEvent)) {
                 submitData(jsonStr);
             }
         }
     }
     
     private void submitData(final String requestBody) {
         System.out.println("Submitting data...");
         final DefaultHttpClient httpclient = new DefaultHttpClient();
 
         final String url = "https://www.exceptional.io/api/errors?" +
             "api_key="+this.apiKey+"&protocol_version=6";
         final HttpPost post = new HttpPost(url);
         post.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
         final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         GZIPOutputStream gos = null;
         InputStream is = null;
         try {
             gos = new GZIPOutputStream(baos);
             gos.write(requestBody.getBytes("UTF-8"));
             gos.close();
             post.setEntity(new ByteArrayEntity(baos.toByteArray()));
             System.err.println("Sending data to server...");
             final HttpResponse response = httpclient.execute(post);
             System.err.println("Sent data to server...");
 
             final int statusCode = response.getStatusLine().getStatusCode();
             final HttpEntity responseEntity = response.getEntity();
             is = responseEntity.getContent();
             if (statusCode < 200 || statusCode > 299) {
                 final String body = IOUtils.toString(is);
                 InputStream bais = null;
                 OutputStream fos = null;
                 try {
                     bais = new ByteArrayInputStream(body.getBytes());
                     fos = new FileOutputStream(new File("bug_error.html"));
                     IOUtils.copy(bais, fos);
                 } finally {
                     IOUtils.closeQuietly(bais);
                     IOUtils.closeQuietly(fos);
                 }
 
                 //System.err.println("Could not send bug:\n"
                 //        + method.getStatusLine() + "\n" + body);
                 final Header[] headers = response.getAllHeaders();
                 for (int i = 0; i < headers.length; i++) {
                     System.err.println(headers[i]);
                 }
                 return;
             }
 
             // We always have to read the body.
             EntityUtils.consume(responseEntity);
         } catch (final IOException e) {
             System.err.println("\n\nERROR::IO error connecting to server" + e);
             System.out.println(dumpStack(e));
         } catch (final Throwable e) {
             System.err.println("Got error\n" + e);
             System.out.println(dumpStack(e));
         } finally {
             IOUtils.closeQuietly(is);
             IOUtils.closeQuietly(gos);
             httpclient.getConnectionManager().shutdown();
         }
     }
 
     private JSONObject requestData(final LoggingEvent le) {
         final JSONObject json = new JSONObject();
         return json;
     }
     
    private JSONObject appData(final LoggingEvent le) {
        final JSONObject json = new JSONObject();
        json.put("application_root_directory", "/");
        json.put("env", getEnv(le));
        return json;
    }
     private JSONObject getEnv(final LoggingEvent le) {
         final JSONObject json = new JSONObject();
         final LocationInfo li = le.getLocationInformation();
         final int lineNumber;
         final String ln = li.getLineNumber();
         if (NumberUtils.isNumber(ln)) {
             lineNumber = Integer.parseInt(ln);
         } else {
             lineNumber = -1;
         }
         json.put("message", le.getMessage().toString());
         json.put("logLevel", le.getLevel().toString());
         json.put("methodName", li.getMethodName());
         json.put("lineNumber", lineNumber);
         json.put("threadName", le.getThreadName());
         json.put("javaVersion", SystemUtils.JAVA_VERSION);
         json.put("osName", SystemUtils.OS_NAME);
         json.put("osArch", SystemUtils.OS_ARCH);
         json.put("osVersion", SystemUtils.OS_VERSION);
         json.put("language", SystemUtils.USER_LANGUAGE);
         json.put("country", SystemUtils.USER_COUNTRY);
         json.put("timeZone", SystemUtils.USER_TIMEZONE);
         
         final String osRoot = SystemUtils.IS_OS_WINDOWS ? "c:" : "/";
         long free = Long.MAX_VALUE;
         try {
             free = FileSystemUtils.freeSpaceKb(osRoot);
             // Convert to megabytes for easy reading.
             free = free / 1024L;
         } catch (final IOException e) {
         }
         json.put("disk_space", String.valueOf(free));
         
         return json;
     }
     
     private JSONObject exceptionData(final LoggingEvent le) {
         final JSONObject json = new JSONObject();
         json.put("message", le.getMessage().toString());
         json.put("backtrace", getThrowableArray(le));
         final LocationInfo li = le.getLocationInformation();
         final String exceptionClass;
         if (li == null) {
             exceptionClass = "unknown";
         } else {
             exceptionClass = li.getClassName();
         }
         json.put("exception_class", exceptionClass);
         json.put("occurred_at", ExceptionalUtils.iso8601());
         return json;
     }
     
     private JSONObject clientData(final LoggingEvent le) {
         final JSONObject json = new JSONObject();
         json.put("client", "exceptional-java-plugin");
         json.put("version", "0.1");
         json.put("protocol_version", "6");
         return json;
     }
     
     private JSONArray getThrowableArray(final LoggingEvent le) {
         final JSONArray array = new JSONArray();
         final ThrowableInformation ti = le.getThrowableInformation();
         if (ti != null) {
             final String[] throwableStr = ti.getThrowableStrRep();
             for (final String str : throwableStr) {
                 array.add(str.trim());
             }
         } 
         return array;
     }
     
     /**
      * Returns the stack trace as a string.
      * 
      * @param cause
      *            The thread to dump.
      * @return The stack trace as a string.
      */
     private static String dumpStack(final Throwable cause) {
         if (cause == null) {
             return "Throwable was null";
         }
         final StringWriter sw = new StringWriter();
         final PrintWriter s = new PrintWriter(sw);
 
         // This is very close to what Thread.dumpStack does.
         cause.printStackTrace(s);
 
         final String stack = sw.toString();
         try {
             sw.close();
         } catch (final IOException e) {
             System.err.println("Could not close "+e);
         }
         s.close();
         return stack;
     }
 
     private static final class Bug {
 
         private final String className;
         private final String methodName;
         private final String lineNumber;
 
         private Bug(final LocationInfo li) {
             this.className = li.getClassName();
             this.methodName = li.getMethodName();
             this.lineNumber = li.getLineNumber();
         }
 
         @Override
         public int hashCode() {
             final int PRIME = 31;
             int result = 1;
             result = PRIME * result
                     + ((className == null) ? 0 : className.hashCode());
             result = PRIME * result
                     + ((lineNumber == null) ? 0 : lineNumber.hashCode());
             result = PRIME * result
                     + ((methodName == null) ? 0 : methodName.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             final Bug other = (Bug) obj;
             if (className == null) {
                 if (other.className != null)
                     return false;
             } else if (!className.equals(other.className))
                 return false;
             if (lineNumber == null) {
                 if (other.lineNumber != null)
                     return false;
             } else if (!lineNumber.equals(other.lineNumber))
                 return false;
             if (methodName == null) {
                 if (other.methodName != null)
                     return false;
             } else if (!methodName.equals(other.methodName))
                 return false;
             return true;
         }
     }
 
 }
