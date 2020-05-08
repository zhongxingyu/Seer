 package net.node42.filter;
 
 
 // Copyright 2013 Mark Roderick
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 
 import com.google.common.annotations.VisibleForTesting;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.InvalidPathException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 
 
 
 /**
  * <p>
  * Filter implementation that only allows a certain number of threads through simultaneously. Overflow threads
  * periodically check to see if a slot has freed up. If they are unable to run in a certain amount of time then a
  * configurable html file is returned.
  * </p>
  * <p>
  * The specific configuration properties that can be set are:
  * </p>
  * <dl>
  *     <dt><b>request_count_concurrent_max</b></dt>
  *     <dd>The maximum number of requests the instance will allow to run concurrently. This number is likely to vary
  *     significantly between applications. The default value is {@link #REQUEST_COUNT_CONCURRENT_MAX_DEFAULT}.</dd>
  *
  *     <dt><b>request_count_check_interval</b></dt>
  *     <dd>Sets the frequency, in milliseconds, for how long to wait before checking to see if an execution slot has
  *     opened. Longer running servlets can check less frequently, shorted live ones for frequently. The default
  *     value is {@link #REQUEST_COUNT_CHECK_INTERVAL_DEFAULT}.</dd>
  *
  *     <dt><b>request_timeout</b></dt>
  *     <dd>Specifies how long, in milliseconds, to wait for a chance to run before returning an error page. The default
  *     value is {@link #REQUEST_TIMEOUT_DEFAULT}.</dd>
  *
  *     <dt><b>response_timeout_file_name</b></dt>
  *     <dd>Path to the file, presumably html, that will be returned when a request is not able to be processed. The
  *     default is to output {@link #CONTENT_TIMEOUT_DEFAULT}</dd>
  * </dl>
  * <p>
  * If any of the above are not configured, or if the value is bad, the default is used. A log entry at <b>warn</b>
  * level will be written whenever the default is used. This is only to aid troubleshooting configuration issues and does
  * not indicate a technical issue.
  * </p>
  * <p><strong>Considerations</strong><br/>
  * This implementation does <strong>not</strong> use a queue. It is entirely possible for a request to time out even
  * as request submitted after it are processed. See documentation on <a href="https://github.com/node42/LoadLimitingFilter">
  * GitHub</a> for general considerations on using this project.
  * </p>
  *
  * @author Mark Roderick [root -at- node42 dooot net]
  * @since 0.00
  */
 public class SimpleTimeoutLoadLimitingFilter implements Filter {
 
 
     public static final int    REQUEST_COUNT_CONCURRENT_MAX_DEFAULT = 15;
     public static final long   REQUEST_COUNT_CHECK_INTERVAL_DEFAULT = 500L;
     public static final long   REQUEST_TIMEOUT_DEFAULT              = 3000L;
     public static final String CONTENT_TIMEOUT_DEFAULT              = "<!DOCTYPE html><html><head><title>Service Temporarily Overloaded</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><p>The service is <em>temporarily</em> overloaded. Please try again in a couple of minutes.<p>Sorry for the inconvenience!</body></html>";
 
     private static final Logger LOGGER = LoggerFactory.getLogger ("net.node42.filter.SimpleTimeoutLoadLimitingFilter");
 
     @VisibleForTesting
     final AtomicInteger ACTIVE_REQUESTS = new AtomicInteger (0);
     @VisibleForTesting
     final AtomicBoolean DESTROY         = new AtomicBoolean (false);
     @VisibleForTesting
     int    maxConcurrentRequests;
     @VisibleForTesting
     long   requestCountCheckInterval;
     @VisibleForTesting
     long   requestTimeoutMillis;
     @VisibleForTesting
     String timeoutResponseContent;
 
 
     /**
      * <p>
      * Class configuration is performed through this method. It relies on initialization parameters delivered by the
      * {@code filterConfig}.
      * </p>
      * <p>
      * The specific configuration properties that can be set are:
      * </p>
      * <dl>
      *     <dt><b>request_count_concurrent_max</b></dt>
      *     <dd>The maximum number of requests the instance will allow to run concurrently. This number is likely to vary
      *     significantly between applications. The default value is {@link #REQUEST_COUNT_CONCURRENT_MAX_DEFAULT}.</dd>
      *
      *     <dt><b>request_count_check_interval</b></dt>
      *     <dd>Sets the frequency, in milliseconds, for how long to wait before checking to see if an execution slot has
      *     opened. Longer running servlets can check less frequently, shorted live ones for frequently. The default
      *     value is {@link #REQUEST_COUNT_CHECK_INTERVAL_DEFAULT}.</dd>
      *
      *     <dt><b>request_timeout</b></dt>
      *     <dd>Specifies how long, in milliseconds, to wait for a chance to run before returning an error page. The default
      *     value is {@link #REQUEST_TIMEOUT_DEFAULT}.</dd>
      *
      *     <dt><b>response_timeout_file_name</b></dt>
      *     <dd>Path to the file, presumably html, that will be returned when a request is not able to be processed. The
      *     default is to output {@link #CONTENT_TIMEOUT_DEFAULT}</dd>
      * </dl>
      * @param filterConfig      Envelop for the initialization parameters.
      * @throws ServletException Thrown by servlet framework, not us :)
      */
     @Override
     public void init (final FilterConfig filterConfig) throws ServletException {
         maxConcurrentRequests     = intFromStringOrSpecified  (filterConfig.getInitParameter ("request_count_concurrent_max"),
                                                                REQUEST_COUNT_CONCURRENT_MAX_DEFAULT,
                                                                "request_count_concurrent_max");
         LOGGER.debug ("Initialized SimpleTimeoutLoadLimitingFilter setting \"Max Concurrent Requests\" to: \"{}\"", maxConcurrentRequests);
 
         requestCountCheckInterval = longFromStringOrSpecified (filterConfig.getInitParameter ("request_count_check_interval"),
                                                                REQUEST_COUNT_CHECK_INTERVAL_DEFAULT,
                                                                "request_count_check_interval");
         LOGGER.debug ("Initialized SimpleTimeoutLoadLimitingFilter setting \"Request Count Check Interval\" to: \"{}\"", requestCountCheckInterval);
 
         requestTimeoutMillis      = longFromStringOrSpecified (filterConfig.getInitParameter ("request_timeout"),
                                                                REQUEST_TIMEOUT_DEFAULT,
                                                                "request_timeout");
         LOGGER.debug ("Initialized SimpleTimeoutLoadLimitingFilter setting \"Request Timeout In Millis\" to: \"{}\"", requestTimeoutMillis);
 
         timeoutResponseContent    = contentFromFileOrDefaultContent (  // Read in specified file, or use the default embedded content.
                                                                     filterConfig.getInitParameter ("response_timeout_file_name"),
                                                                     CONTENT_TIMEOUT_DEFAULT);
         LOGGER.debug ("Initialized SimpleTimeoutLoadLimitingFilter setting \"Timeout Response Content\" to: \"{}\"", timeoutResponseContent);
 
         LOGGER.info ("The Servlet Filter \"{}\" has been initialized.", this.getClass ().getSimpleName ());
     }
 
 
     @Override
     public void doFilter (final ServletRequest  servletRequest,
                           final ServletResponse servletResponse,
                           final FilterChain     filterChain)
     throws IOException, ServletException {
 
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
         final long startTime = System.currentTimeMillis ();
         while (System.currentTimeMillis () <= (startTime + requestTimeoutMillis)
                && !DESTROY.get ()) {
 
             // This is fluffier than a get/compare/increment, but avoids synchronization and results in much more simple
             // code. The real world effect of increment/get/compare[/decrement] for this application is negligible.
             if (maxConcurrentRequests <= ACTIVE_REQUESTS.incrementAndGet ()) {
                 LOGGER.trace ("Blocking request {} due to load of {} requests. Will evaluate again in {}ms"
                        , httpServletResponse
                         , ACTIVE_REQUESTS.get ()
                         , requestCountCheckInterval);
                 ACTIVE_REQUESTS.decrementAndGet ();  // Take our incrementation off the counter
                 sleepForRequestCountCheckInterval (requestCountCheckInterval);
             }
             else {
                 // We are cleared to allow the request on it's way
                 try {
                    filterChain.doFilter (servletRequest, httpServletResponse);
                     return;
                 }
                 finally {
                     // Take this request off the counter
                     ACTIVE_REQUESTS.decrementAndGet ();
                 }
             }
         }

        // Return the configured content, set the overloaded HTTP Status Code...
        httpServletResponse.getWriter ().write (timeoutResponseContent);
        httpServletResponse.setStatus (HttpServletResponse.SC_SERVICE_UNAVAILABLE);
     }
 
 
     /**
      * <p>
      * Sets an internal kill flag and then sleeps for the time specified by the request_count_check_interval init
      * parameter. Highly likely to kill requests waiting for a chance to run, has no effect on currently running
      * requests.
      * </p>
      */
     @Override
     public void destroy () {
         LOGGER.info ("Initiating destroy procedure on Servlet Filter \"{}\"", this.getClass ().getSimpleName ());
         DESTROY.set (true);
         sleepForRequestCountCheckInterval (requestCountCheckInterval);
     }
 
 
 
     // Helper methods...
 
 
     /**
      * <p>
      * Simple wrapper for {@link Thread#sleep(long)} that swallows {@link InterruptedException}s.
      * </p>
      *
      * @param timeToSleep time in milliseconds to sleep for
      */
     private void sleepForRequestCountCheckInterval (final long timeToSleep) {
         try {
             Thread.sleep (timeToSleep);
         }
         catch (InterruptedException e) {
             // meh. Weird but harmless. We just end up checking the counter sooner
             // than planned.
         }
     }
 
 
     /**
      * <p>
      * Parses an integer from the provided {@link String} {@code stringInt}. If it is unable to do so for any reason
      * then the provided int {@code specified} is returned instead.
      * </p>
      *
      * @param stringInt         {@link String} to parse an int from
      * @param specified         {@code int} to return if parsing {@code stringInt} fails
      * @param configurationName Name of the configuration setting the string is being transformed for. This is only
      *                          used for logging when there is a problem converting the string.
      * @return int representation of the provided {@code stringInt}, or the {@code specified} if parsing fails.
      */
     private int intFromStringOrSpecified (final String stringInt,
                                           final int    specified,
                                           final String configurationName) {
 
         final String normalizedStringInt = stringTrimmedOrNull (stringInt);
         try {
             return Integer.parseInt (normalizedStringInt);
         }
         catch (NumberFormatException e) {
             LOGGER.warn ("The numeric setting of \"{}\" for the configuration parameter \"{}\" was not valid. Falling back to use the default value of \"{}\"",
                          stringInt,
                          configurationName,
                          specified);
             return specified;
         }
     }
 
 
     /**
      * <p>
      * Parses a long from the provided {@link String} {@code stringLong}. If it is unable to do so for any reason
      * then the provided long {@code specified} is returned instead.
      * </p>
      *
      * @param stringLong        {@link String} to parse a long from
      * @param specified         {@code long} to return if parsing {@code stringLong} fails
      * @param configurationName Name of the configuration setting the string is being transformed for. This is only
      *                          used for logging when there is a problem converting the string.
      * @return long representation of the provided {@code stringLong}, or the {@code specified} if parsing fails.
      */
     private long longFromStringOrSpecified (final String stringLong,
                                             final long   specified,
                                             final String configurationName) {
 
         final String normalizedStringLong = stringTrimmedOrNull (stringLong);
         try {
             return Long.parseLong (normalizedStringLong);
         }
         catch (NumberFormatException e) {
             LOGGER.warn ("The numeric setting of \"{}\" for the configuration parameter \"{}\" was not valid. Falling back to use the default value of \"{}\"",
                          stringLong,
                          configurationName,
                          specified);
             return specified;
         }
     }
 
 
     /**
      * <p>
      * If the provided {@code stingToTrim} is a {@link String} instance then this return a {@link String#trim()}
      * version of it. Otherwise it returns null. Essentially this is just an NPE safe wrapper.
      * </p>
      *
      * @param stingToTrim   {@link String} to perform {@link String#trim()} on
      * @return              trimmed version of {@code stringToTrim}, or {@code null} if {@code stringToTrim} is
      *                      {@code null}
      */
     private String stringTrimmedOrNull (final String stingToTrim) {
 
         return null == stingToTrim
                ? null
                : stingToTrim.trim ();
     }
 
 
     /**
      * <p>
      * This method will read in the content to return if a request is not able to run in the specified amount of time.
      * </p>
      * <p>
      * Any failures in the process are logged out and the specified default content is used instead.
      * </p>
      *
      * @param fileName           Name of the file containing the content to return when a request times out.
      * @param contentFallback    Content to use instead of the content in {@code fileName} when processing that file
      *                           fails.
      * @return The content of the specified {@code fileName}, or the provided fallback {@link String}
      *         {@code contentFallback}.
      */
     private String contentFromFileOrDefaultContent (final String fileName,
                                                     final String contentFallback) {
 
         final Path fileNamePath;
         try {
             fileNamePath = Paths.get (fileName);
         }
         catch (InvalidPathException|NullPointerException e) {
             // Return the backup backup content if we can't get the specified file's path...
             return contentFallback;
         }
 
         // Explicit checks for correctly logging issues...
         if (!Files.exists (fileNamePath)) {
             LOGGER.warn ("The specified timeout content file \"{}\" doesn't exist, using default content instead!",
                          fileName);
             return contentFallback;
         }
         else if (!Files.isRegularFile (fileNamePath)) {
             LOGGER.warn ("The specified timeout content file \"{}\" isn't a file (directory maybe?), using default content instead!",
                          fileName);
             return contentFallback;
         }
         else if (!Files.isReadable (fileNamePath)) {
             LOGGER.warn ("The specified timeout content file \"{}\" isn't readable, using default content instead!",
                          fileName);
             return contentFallback;
         }
 
         final StringBuilder contentBuilder = new StringBuilder ();
         try (final BufferedReader bufferedReader = Files.newBufferedReader (fileNamePath, Charset.defaultCharset ())) {
 
             String currentLine;
             boolean readFistLine = false;
             while (null != (currentLine = bufferedReader.readLine ())) {
                 // Restore line endings stripped by readLine()...
                 if (readFistLine) contentBuilder.append (System.lineSeparator ());
                 else readFistLine = true;
 
                 contentBuilder.append (currentLine);
             }
         }
         catch (IOException e) {
             LOGGER.warn ("Failed to read the specified timeout content file \"{}\", using default content instead: {}",
                          fileName,
                          e.getMessage ());
             return contentFallback;
         }
         catch (SecurityException e) {
             LOGGER.warn ("A security manager didn't allow reading of the specified timeout content file \"{}\", using default content instead: {}",
                          fileName,
                          e.getMessage ());
             return contentFallback;
         }
 
         return contentBuilder.toString ();
     }
 
 }
