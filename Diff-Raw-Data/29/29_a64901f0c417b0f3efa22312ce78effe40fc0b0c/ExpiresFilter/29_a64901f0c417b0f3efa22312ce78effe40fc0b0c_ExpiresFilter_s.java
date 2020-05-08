 /*
  * Copyright 2008-2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.xebia.servlet.filter;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p>
  * Port of <a
  * href="http://httpd.apache.org/docs/2.2/mod/mod_expires.html">Apache
  * mod_expires</a>. Following documentation is inspired by
  * <code>mod_expires</code> .
  * </p>
  * <p>
  * <strong>Description</strong>
  * </p>
  * <p>
  * Generation of <code>Expires</code> and <code>Cache-Control</code> HTTP
  * headers according to user-specified criteria
  * </p>
  * <p>
  * <strong>Summary</strong>
  * </p>
  * <p>
  * This module controls the setting of the <code>Expires</code> HTTP header and
  * the <code>max-age</code> directive of the <code>Cache-Control</code> HTTP
  * header in server responses. The expiration date can set to be relative to
  * either the time the source file was last modified, or to the time of the
  * client access.
  * </p>
  * 
  * <p>
  * These HTTP headers are an instruction to the client about the document's
  * validity and persistence. If cached, the document may be fetched from the
  * cache rather than from the source until this time has passed. After that, the
  * cache copy is considered "expired" and invalid, and a new copy must be
  * obtained from the source.
  * </p>
  * 
  * <p>
  * To modify <code>Cache-Control</code> directives other than
  * <code>max-age</code> (see <a
  * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9">RFC
  * 2616 section 14.9</a>), you can use the
  * <code class="directive"><a href="http://httpd.apache.org/docs/2.2/mod/mod_headers.html#header">Header</a></code>
  * directive for the <a
  * href="http://httpd.apache.org/docs/2.2/mod/mod_headers.html">Apache Httpd
  * mod_headers</a> module.
  * </p>
  * 
  * 
  * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
  */
 public class ExpiresFilter implements Filter {
 
     protected static class Duration {
 
         public static Duration minutes(int amount) {
             return new Duration(amount, DurationUnit.MINUTE);
         }
 
         public static Duration seconds(int amount) {
             return new Duration(amount, DurationUnit.SECOND);
         }
 
         final protected int amount;
 
         final protected DurationUnit unit;
 
         public Duration(int amount, DurationUnit unit) {
             super();
             this.amount = amount;
             this.unit = unit;
         }
 
         public int getAmount() {
             return amount;
         }
 
         public DurationUnit getUnit() {
             return unit;
         }
 
         @Override
         public String toString() {
             return amount + " " + unit;
         }
     }
 
     protected enum DurationUnit {
         DAY(Calendar.DAY_OF_YEAR), HOUR(Calendar.HOUR), MINUTE(Calendar.MINUTE), MONTH(Calendar.MONTH), SECOND(Calendar.SECOND), WEEK(
                 Calendar.WEEK_OF_YEAR), YEAR(Calendar.YEAR);
         private final int calendardField;
 
         private DurationUnit(int calendardField) {
             this.calendardField = calendardField;
         }
 
         public int getCalendardField() {
             return calendardField;
         }
 
     }
 
     protected static class ExpiresConfiguration {
         private List<Duration> durations;
 
         private StartingPoint startingPoint;
 
         public ExpiresConfiguration(StartingPoint startingPoint, Duration... durations) {
             this(startingPoint, Arrays.asList(durations));
         }
 
         public ExpiresConfiguration(StartingPoint startingPoint, List<Duration> durations) {
             super();
             this.startingPoint = startingPoint;
             this.durations = durations;
         }
 
         public List<Duration> getDurations() {
             return durations;
         }
 
         public StartingPoint getStartingPoint() {
             return startingPoint;
         }
 
         @Override
         public String toString() {
             return "ExpiresConfiguration[startingPoint=" + startingPoint + ", duration=" + durations + "]";
         }
     }
 
     /**
      * Expiration configuration starting point. Either the time the
      * html-page/servlet-response was served ({@link StartingPoint#ACCESS_TIME})
      * or the last time the html-page/servlet-response was modified (
      * {@link StartingPoint#LAST_MODIFICATION_TIME}).
      */
     protected enum StartingPoint {
         ACCESS_TIME, LAST_MODIFICATION_TIME
     }
 
     /**
      * <p>
      * Wrapping extension of the {@link HttpServletResponse} to yrap the
      * "Start Write Response Body" event.
      * </p>
      * <p>
      * For performance optimization, this extended response only holds the
      * {@link #lastModifiedHeader} and {@link #cacheControlHeader} instead of
      * holding the full list of headers.
      * </p>
      */
     public class XHttpServletResponse extends HttpServletResponseWrapper {
 
         private String cacheControlHeader;
 
         private long lastModifiedHeader;
 
         private PrintWriter printWriter;
 
         private HttpServletRequest request;
 
         private ServletOutputStream servletOutputStream;
 
         private int status = HttpServletResponse.SC_OK;
 
         private boolean writeStarted;
 
         public XHttpServletResponse(HttpServletRequest request, HttpServletResponse response) {
             super(response);
             this.request = request;
         }
 
         @Override
         public void addDateHeader(String name, long date) {
             super.addDateHeader(name, date);
             if (!containsHeader(HEADER_LAST_MODIFIED)) {
                 this.lastModifiedHeader = date;
             }
         }
 
         @Override
         public void addHeader(String name, String value) {
             super.addHeader(name, value);
             if (HEADER_CACHE_CONTROL.equalsIgnoreCase(name) && cacheControlHeader == null) {
                 cacheControlHeader = value;
             }
         }
 
         public String getCacheControlHeader() {
             return cacheControlHeader;
         }
 
         public boolean isLastModifiedHeaderSet() {
             return containsHeader(HEADER_LAST_MODIFIED);
         }
 
         public long getLastModifiedHeader() {
             return this.lastModifiedHeader;
         }
 
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
             if (servletOutputStream == null) {
                 servletOutputStream = new XServletOutputStream(super.getOutputStream(), request, this);
             }
             return servletOutputStream;
         }
 
         public int getStatus() {
             return status;
         }
 
         @Override
         public PrintWriter getWriter() throws IOException {
             if (printWriter == null) {
                 printWriter = new XPrintWriter(super.getWriter(), request, this);
             }
             return printWriter;
         }
 
         public boolean isWriteStarted() {
             return writeStarted;
         }
 
         @Override
         public void sendError(int sc) throws IOException {
             this.status = sc;
             super.sendError(sc);
         }
 
         @Override
         public void sendError(int sc, String msg) throws IOException {
             this.status = sc;
             super.sendError(sc, msg);
         }
 
         @Override
         public void sendRedirect(String location) throws IOException {
             this.status = HttpServletResponse.SC_FOUND;
             super.sendRedirect(location);
         }
 
         @Override
         public void setDateHeader(String name, long date) {
             super.setDateHeader(name, date);
             if (HEADER_LAST_MODIFIED.equalsIgnoreCase(name)) {
                 this.lastModifiedHeader = date;
             }
         }
 
         @Override
         public void setHeader(String name, String value) {
             super.setHeader(name, value);
             if (HEADER_CACHE_CONTROL.equalsIgnoreCase(name)) {
                 this.cacheControlHeader = value;
             }
         }
 
         @Override
         public void setStatus(int sc) {
             this.status = sc;
             super.setStatus(sc);
         }
 
         @Override
         public void setStatus(int sc, String sm) {
             this.status = sc;
             super.setStatus(sc, sm);
         }
 
         public void setWriteStarted(boolean writeStarted) {
             this.writeStarted = writeStarted;
         }
     }
 
     /**
      * Wrapping extension of {@link PrintWriter} to trap the
      * "Start Write Response Body" event.
      */
     public class XPrintWriter extends PrintWriter {
         private PrintWriter out;
 
         private HttpServletRequest request;
 
         private XHttpServletResponse response;
 
         public XPrintWriter(PrintWriter out, HttpServletRequest request, XHttpServletResponse response) {
             super(out);
             this.out = out;
             this.request = request;
             this.response = response;
         }
 
         public PrintWriter append(char c) {
             fireBeforeWriteResponseBodyEvent();
             return out.append(c);
         }
 
         public PrintWriter append(CharSequence csq) {
             fireBeforeWriteResponseBodyEvent();
             return out.append(csq);
         }
 
         public PrintWriter append(CharSequence csq, int start, int end) {
             fireBeforeWriteResponseBodyEvent();
             return out.append(csq, start, end);
         }
 
         public void close() {
             fireBeforeWriteResponseBodyEvent();
             out.close();
         }
 
         private void fireBeforeWriteResponseBodyEvent() {
             if (!this.response.isWriteStarted()) {
                 this.response.setWriteStarted(true);
                 onBeforeWriteResponseBody(request, response);
             }
         }
 
         public void flush() {
             fireBeforeWriteResponseBodyEvent();
             out.flush();
         }
 
         public void print(boolean b) {
             fireBeforeWriteResponseBodyEvent();
             out.print(b);
         }
 
         public void print(char c) {
             fireBeforeWriteResponseBodyEvent();
             out.print(c);
         }
 
         public void print(char[] s) {
             fireBeforeWriteResponseBodyEvent();
             out.print(s);
         }
 
         public void print(double d) {
             fireBeforeWriteResponseBodyEvent();
             out.print(d);
         }
 
         public void print(float f) {
             fireBeforeWriteResponseBodyEvent();
             out.print(f);
         }
 
         public void print(int i) {
             fireBeforeWriteResponseBodyEvent();
             out.print(i);
         }
 
         public void print(long l) {
             fireBeforeWriteResponseBodyEvent();
             out.print(l);
         }
 
         public void print(Object obj) {
             fireBeforeWriteResponseBodyEvent();
             out.print(obj);
         }
 
         public void print(String s) {
             fireBeforeWriteResponseBodyEvent();
             out.print(s);
         }
 
         public PrintWriter printf(Locale l, String format, Object... args) {
             fireBeforeWriteResponseBodyEvent();
             return out.printf(l, format, args);
         }
 
         public PrintWriter printf(String format, Object... args) {
             fireBeforeWriteResponseBodyEvent();
             return out.printf(format, args);
         }
 
         public void println() {
             fireBeforeWriteResponseBodyEvent();
             out.println();
         }
 
         public void println(boolean x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(char x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(char[] x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(double x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(float x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(int x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(long x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(Object x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void println(String x) {
             fireBeforeWriteResponseBodyEvent();
             out.println(x);
         }
 
         public void write(char[] buf) {
             fireBeforeWriteResponseBodyEvent();
             out.write(buf);
         }
 
         public void write(char[] buf, int off, int len) {
             fireBeforeWriteResponseBodyEvent();
             out.write(buf, off, len);
         }
 
         public void write(int c) {
             fireBeforeWriteResponseBodyEvent();
             out.write(c);
         }
 
         public void write(String s) {
             fireBeforeWriteResponseBodyEvent();
             out.write(s);
         }
 
         public void write(String s, int off, int len) {
             fireBeforeWriteResponseBodyEvent();
             out.write(s, off, len);
         }
 
     }
 
     /**
      * Wrapping extension of {@link ServletOutputStream} to trap the
      * "Start Write Response Body" event.
      */
     public class XServletOutputStream extends ServletOutputStream {
 
         private HttpServletRequest request;
 
         private XHttpServletResponse response;
 
         private ServletOutputStream servletOutputStream;
 
         public XServletOutputStream(ServletOutputStream servletOutputStream, HttpServletRequest request, XHttpServletResponse response) {
             super();
             this.servletOutputStream = servletOutputStream;
             this.response = response;
             this.request = request;
         }
 
         public void close() throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.close();
         }
 
         private void fireOnBeforeWriteResponseBodyEvent() {
             if (!this.response.isWriteStarted()) {
                 this.response.setWriteStarted(true);
                 onBeforeWriteResponseBody(request, response);
             }
         }
 
         public void flush() throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.flush();
         }
 
         public void print(boolean b) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(b);
         }
 
         public void print(char c) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(c);
         }
 
         public void print(double d) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(d);
         }
 
         public void print(float f) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(f);
         }
 
         public void print(int i) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(i);
         }
 
         public void print(long l) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(l);
         }
 
         public void print(String s) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.print(s);
         }
 
         public void println() throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println();
         }
 
         public void println(boolean b) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(b);
         }
 
         public void println(char c) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(c);
         }
 
         public void println(double d) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(d);
         }
 
         public void println(float f) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(f);
         }
 
         public void println(int i) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(i);
         }
 
         public void println(long l) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(l);
         }
 
         public void println(String s) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.println(s);
         }
 
         public void write(byte[] b) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.write(b);
         }
 
         public void write(byte[] b, int off, int len) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.write(b, off, len);
         }
 
         public void write(int b) throws IOException {
             fireOnBeforeWriteResponseBodyEvent();
             servletOutputStream.write(b);
         }
 
     }
 
     /**
      * {@link Pattern} for a comma delimited string that support whitespace
      * characters
      */
     private static final Pattern commaSeparatedValuesPattern = Pattern.compile("\\s*,\\s*");
 
     private static final String HEADER_CACHE_CONTROL = "Cache-Control";
 
     private static final String HEADER_EXPIRES = "Expires";
 
     private static final String HEADER_LAST_MODIFIED = "Last-Modified";
 
     private static final Logger logger = LoggerFactory.getLogger(ExpiresFilter.class);
 
     private static final String PARAMETER_EXPIRES_ACTIVE = "ExpiresActive";
 
     private static final String PARAMETER_EXPIRES_BY_TYPE = "ExpiresByType";
 
     private static final String PARAMETER_EXPIRES_DEFAULT = "ExpiresDefault";
 
     private static final String PARAMETER_EXPIRES_EXCLUDED_RESPONSE_STATUS_CODES = "ExpiresExcludedResponseStatusCodes";
 
     protected static int[] commaDelimitedListToIntArray(String commaDelimitedInts) {
         String[] intsAsStrings = commaDelimitedListToStringArray(commaDelimitedInts);
         int[] ints = new int[intsAsStrings.length];
         for (int i = 0; i < intsAsStrings.length; i++) {
             String intAsString = intsAsStrings[i];
             try {
                 ints[i] = Integer.parseInt(intAsString);
             } catch (NumberFormatException e) {
                 throw new RuntimeException("Exception parsing number '" + i + "' (zero based) of comma delimited list '"
                         + commaDelimitedInts + "'");
             }
         }
         return ints;
     }
 
     /**
      * Convert a given comma delimited list of regular expressions into an array
      * of String
      * 
      * @return array of patterns (non <code>null</code>)
      */
     protected static String[] commaDelimitedListToStringArray(String commaDelimitedStrings) {
         return (commaDelimitedStrings == null || commaDelimitedStrings.length() == 0) ? new String[0] : commaSeparatedValuesPattern
                 .split(commaDelimitedStrings);
     }
 
     /**
      * Returns <code>true</code> if the given <code>str</code> contains the
      * given <code>searchStr</code>.
      */
     protected static boolean contains(String str, String searchStr) {
         if (str == null || searchStr == null) {
             return false;
         }
         return str.indexOf(searchStr) >= 0;
     }
 
     /**
      * Convert an array of ints into a comma delimited string
      */
     protected static String intsToCommaDelimitedString(int[] ints) {
         if (ints == null) {
             return "";
         }
 
         StringBuilder result = new StringBuilder();
 
         for (int i = 0; i < ints.length; i++) {
             result.append(ints[i]);
            if (i < ints.length) {
                 result.append(", ");
             }
         }
         return result.toString();
     }
 
     /**
      * Returns <code>true</code> if the given <code>str</code> is
      * <code>null</code> or has a zero characters length.
      */
     protected static boolean isEmpty(String str) {
         return str == null || str.length() == 0;
     }
 
     /**
      * Returns <code>true</code> if the given <code>str</code> has at least one
      * character (can be a withespace).
      */
     protected static boolean isNotEmpty(String str) {
         return !isEmpty(str);
     }
 
     protected static boolean startsWithIgnoreCase(String string, String prefix) {
         if (string == null || prefix == null) {
             return string == null && prefix == null;
         }
         if (prefix.length() > string.length()) {
             return false;
         }
 
         return string.regionMatches(true, 0, prefix, 0, prefix.length());
     }
 
     protected static String substringBefore(String str, String separator) {
         if (str == null || str.isEmpty() || separator == null) {
             return null;
         }
 
         if (separator.isEmpty()) {
             return "";
         }
 
         int separatorIndex = str.indexOf(separator);
         if (separatorIndex == -1) {
             return str;
         }
         return str.substring(0, separatorIndex);
     }
 
     private boolean active = true;
 
     /**
      * Default Expires configuration.
      */
     private ExpiresConfiguration defaultExpiresConfiguration;
 
     /**
      * list of response status code for which the {@link ExpiresFilter} will not
      * generate expiration headers.
      */
     private int[] excludedResponseStatusCodes = new int[] { HttpServletResponse.SC_NOT_MODIFIED };
 
     /**
      * Expires configuration by content type. Visible for test.
      */
     private Map<String, ExpiresConfiguration> expiresConfigurationByContentType = new LinkedHashMap<String, ExpiresConfiguration>();
 
     public void destroy() {
 
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
             HttpServletRequest httpRequest = (HttpServletRequest) request;
             HttpServletResponse httpResponse = (HttpServletResponse) response;
 
             if (response.isCommitted()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Request '" + httpRequest.getRequestURL()
                             + "', can not apply ExpiresFilter on already committed response.");
                 }
                 chain.doFilter(request, response);
             } else if (active) {
                 XHttpServletResponse xResponse = new XHttpServletResponse(httpRequest, httpResponse);
                 chain.doFilter(request, xResponse);
                 if (!xResponse.isWriteStarted()) {
                     // Empty response, manually trigger
                     // onBeforeWriteResponseBody()
                     onBeforeWriteResponseBody(httpRequest, xResponse);
                 }
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Request '" + httpRequest.getRequestURL() + "', ExpiresFilter is NOT active");
                 }
                 chain.doFilter(request, response);
             }
         } else {
             chain.doFilter(request, response);
         }
     }
 
     public ExpiresConfiguration getDefaultExpiresConfiguration() {
         return defaultExpiresConfiguration;
     }
 
     public String getExcludedResponseStatusCodes() {
         return intsToCommaDelimitedString(excludedResponseStatusCodes);
     }
 
     public int[] getExcludedResponseStatusCodesAsInts() {
         return excludedResponseStatusCodes;
     }
 
     /**
      * <p>
      * Returns the expiration date of the given {@link XHttpServletResponse} or
      * <code>null</code> if no expiration date has been configured for the
      * declared content type.
      * </p>
      * <p>
      * <code>protected</code> for extension.
      * </p>
      * 
      * @see HttpServletResponse#getContentType()
      */
     protected Date getExpirationDate(HttpServletRequest request, XHttpServletResponse response) {
         String contentType = response.getContentType();
 
         // lookup exact content-type match (e.g.
         // "text/html; charset=iso-8859-1")
         ExpiresConfiguration configuration = expiresConfigurationByContentType.get(contentType);
         String matchingContentType = contentType;
 
         if (configuration == null && contains(contentType, ";")) {
             // lookup content-type without charset match (e.g. "text/html")
             String contentTypeWithoutCharset = substringBefore(contentType, ";").trim();
             configuration = expiresConfigurationByContentType.get(contentTypeWithoutCharset);
             matchingContentType = contentTypeWithoutCharset;
         }
 
         if (configuration == null && contains(contentType, "/")) {
             // lookup content-type without charset match (e.g. "text/html")
             String majorType = substringBefore(contentType, "/");
             configuration = expiresConfigurationByContentType.get(majorType);
             matchingContentType = majorType;
         }
 
         if (configuration == null) {
             configuration = defaultExpiresConfiguration;
             matchingContentType = "#DEFAULT#";
         }
 
         if (configuration == null) {
             logger.trace("No Expires configuration found for content-type {}", contentType);
             return null;
         }
 
         if (logger.isTraceEnabled()) {
             logger.trace("Use {} matching '{}' for content-type '{}'", new Object[] { configuration, matchingContentType, contentType });
         }
 
         Calendar calendar;
         switch (configuration.getStartingPoint()) {
         case ACCESS_TIME:
             calendar = GregorianCalendar.getInstance();
             break;
         case LAST_MODIFICATION_TIME:
             if (response.isLastModifiedHeaderSet()) {
                 long lastModified = response.getLastModifiedHeader();
                 calendar = GregorianCalendar.getInstance();
                 calendar.setTimeInMillis(lastModified);
             } else {
                 // Last-Modified header not found, use now
                 calendar = GregorianCalendar.getInstance();
             }
             break;
         default:
             throw new IllegalStateException("Unsupported startingPoint '" + configuration.getStartingPoint() + "'");
         }
         for (Duration duration : configuration.getDurations()) {
             calendar.add(duration.getUnit().getCalendardField(), duration.getAmount());
         }
 
         return calendar.getTime();
     }
 
     public Map<String, ExpiresConfiguration> getExpiresConfigurationByContentType() {
         return expiresConfigurationByContentType;
     }
 
     @SuppressWarnings("unchecked")
     public void init(FilterConfig filterConfig) throws ServletException {
         for (Enumeration<String> names = filterConfig.getInitParameterNames(); names.hasMoreElements();) {
             String name = names.nextElement();
             String value = filterConfig.getInitParameter(name);
 
             try {
                 if (name.startsWith(PARAMETER_EXPIRES_BY_TYPE)) {
                     String contentType = name.substring(PARAMETER_EXPIRES_BY_TYPE.length()).trim();
                     ExpiresConfiguration expiresConfiguration = parseExpiresConfiguration(value);
                     this.expiresConfigurationByContentType.put(contentType, expiresConfiguration);
                 } else if (name.equalsIgnoreCase(PARAMETER_EXPIRES_DEFAULT)) {
                     ExpiresConfiguration expiresConfiguration = parseExpiresConfiguration(value);
                     this.defaultExpiresConfiguration = expiresConfiguration;
                 } else if (name.equalsIgnoreCase(PARAMETER_EXPIRES_ACTIVE)) {
                     this.active = "On".equalsIgnoreCase(value) || Boolean.valueOf(value);
                 } else if (name.equalsIgnoreCase(PARAMETER_EXPIRES_EXCLUDED_RESPONSE_STATUS_CODES)) {
                     this.excludedResponseStatusCodes = commaDelimitedListToIntArray(value);
                 } else {
                     logger.warn("Uknown parameter '" + name + "' with value '" + value + "' is ignored !");
                 }
             } catch (RuntimeException e) {
                 throw new ServletException("Exception processing configuration parameter '" + name + "':'" + value + "'", e);
             }
         }
 
         logger.info("Filter initialized with configuration " + this.toString());
     }
 
     public boolean isActive() {
         return active;
     }
 
     /**
      *
      * <p>
      * <code>protected</code> for extension.
      * </p>
      */
     protected boolean isEligibleToExpirationHeaderGeneration(HttpServletRequest request, XHttpServletResponse response) {
         boolean expirationHeaderHasBeenSet = response.containsHeader(HEADER_EXPIRES)
                 || contains(response.getCacheControlHeader(), "max-age");
         if (expirationHeaderHasBeenSet) {
             if (logger.isDebugEnabled()) {
                 logger.debug("Request '{}' with response status '{}' content-type '{}‘, expiration header already defined", new Object[] {
                         request.getRequestURI(), response.getStatus(), response.getContentType() });
             }
             return false;
         }
 
         for (int skippedStatusCode : this.excludedResponseStatusCodes) {
             if (response.getStatus() == skippedStatusCode) {
                 if (logger.isDebugEnabled()) {
                     logger.debug(
                             "Request '{}' with response status '{}' content-type '{}‘, skip expiration header generation for given status",
                             new Object[] { request.getRequestURI(), response.getStatus(), response.getContentType() });
                 }
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * <p>
      * If no expiration header has been set by the servlet and an expiration has
      * been defined in the {@link ExpiresFilter} configuration, sets the
      * "Expires header" and the attribute "max-age" of the "Cache-Control"
      * header.
      * </p>
      * <p>
      * Must be called on the "Start Write Response Body" event.
      * </p>
      * <p>
      * Invocations to {@link Logger#debug(String, Object[])} are guarded by
      * {@link Logger#isDebugEnabled()} because
      * {@link HttpServletRequest#getRequestURI()} and
      * {@link HttpServletResponse#getContentType()} costs {@link String}
      * instantiations.
      * </p>
      */
     public void onBeforeWriteResponseBody(HttpServletRequest request, XHttpServletResponse response) {
 
         if (!isEligibleToExpirationHeaderGeneration(request, response)) {
             return;
         }
 
         Date expirationDate = getExpirationDate(request, response);
         if (expirationDate == null) {
             if (logger.isDebugEnabled()) {
                 logger.debug("Request '{}' with response status '{}' content-type '{}‘ status , no expiration configured", new Object[] {
                         request.getRequestURI(), response.getStatus(), response.getContentType() });
             }
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("Request '{}' with response status '{}' content-type '{}‘, set expiration date {}", new Object[] {
                         request.getRequestURI(), response.getStatus(), response.getContentType(), expirationDate });
             }
 
             String maxAgeDirective = "max-age=" + ((expirationDate.getTime() - System.currentTimeMillis()) / 1000);
 
             String cacheControlHeader = response.getCacheControlHeader();
             String newCacheControlHeader = (cacheControlHeader == null) ? maxAgeDirective : cacheControlHeader + ", " + maxAgeDirective;
             response.setHeader(HEADER_CACHE_CONTROL, newCacheControlHeader);
             response.setDateHeader(HEADER_EXPIRES, expirationDate.getTime());
         }
 
     }
 
     /**
      * "access plus 1 month 15 days 2 hours"
      * "access plus 1 month 15 days 2 hours"
      * 
      * @param line
      * @return
      */
     protected ExpiresConfiguration parseExpiresConfiguration(String line) {
         line = line.trim();
 
         StringTokenizer tokenizer = new StringTokenizer(line, " ");
 
         String currentToken;
 
         try {
             currentToken = tokenizer.nextToken();
         } catch (NoSuchElementException e) {
             throw new IllegalStateException("Starting point (access|now|modification|a<seconds>|m<seconds>) not found in directive '"
                     + line + "'");
         }
 
         StartingPoint startingPoint;
         if ("access".equalsIgnoreCase(currentToken) || "now".equalsIgnoreCase(currentToken)) {
             startingPoint = StartingPoint.ACCESS_TIME;
         } else if ("modification".equalsIgnoreCase(currentToken)) {
             startingPoint = StartingPoint.LAST_MODIFICATION_TIME;
         } else if (!tokenizer.hasMoreTokens() && startsWithIgnoreCase(currentToken, "a")) {
             startingPoint = StartingPoint.ACCESS_TIME;
             // trick : convert duration configuration from old to new style
             tokenizer = new StringTokenizer(currentToken.substring(1) + " seconds", " ");
         } else if (!tokenizer.hasMoreTokens() && startsWithIgnoreCase(currentToken, "m")) {
             startingPoint = StartingPoint.LAST_MODIFICATION_TIME;
             // trick : convert duration configuration from old to new style
             tokenizer = new StringTokenizer(currentToken.substring(1) + " seconds", " ");
         } else {
             throw new IllegalStateException("Invalid starting point (access|now|modification|a<seconds>|m<seconds>) '" + currentToken
                     + "' in directive '" + line + "'");
         }
 
         try {
             currentToken = tokenizer.nextToken();
         } catch (NoSuchElementException e) {
             throw new IllegalStateException("Duration not found in directive '" + line + "'");
         }
 
         if ("plus".equalsIgnoreCase(currentToken)) {
             // skip
             try {
                 currentToken = tokenizer.nextToken();
             } catch (NoSuchElementException e) {
                 throw new IllegalStateException("Duration not found in directive '" + line + "'");
             }
         }
 
         List<Duration> durations = new ArrayList<Duration>();
 
         while (currentToken != null) {
             int amount;
             try {
                 amount = Integer.parseInt(currentToken);
             } catch (NumberFormatException e) {
                 throw new IllegalStateException("Invalid duration (number) '" + currentToken + "' in directive '" + line + "'");
             }
 
             try {
                 currentToken = tokenizer.nextToken();
             } catch (NoSuchElementException e) {
                 throw new IllegalStateException("Duration unit not found after amount " + amount + " in directive '" + line + "'");
             }
             DurationUnit durationUnit;
             if ("years".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.YEAR;
             } else if ("month".equalsIgnoreCase(currentToken) || "months".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.MONTH;
             } else if ("week".equalsIgnoreCase(currentToken) || "weeks".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.WEEK;
             } else if ("day".equalsIgnoreCase(currentToken) || "days".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.DAY;
             } else if ("hour".equalsIgnoreCase(currentToken) || "hours".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.HOUR;
             } else if ("minute".equalsIgnoreCase(currentToken) || "minutes".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.MINUTE;
             } else if ("second".equalsIgnoreCase(currentToken) || "seconds".equalsIgnoreCase(currentToken)) {
                 durationUnit = DurationUnit.SECOND;
             } else {
                 throw new IllegalStateException("Invalid duration unit (years|months|weeks|days|hours|minutes|seconds) '" + currentToken
                         + "' in directive '" + line + "'");
             }
 
             Duration duration = new Duration(amount, durationUnit);
             durations.add(duration);
 
             if (tokenizer.hasMoreTokens()) {
                 currentToken = tokenizer.nextToken();
             } else {
                 currentToken = null;
             }
         }
 
         return new ExpiresConfiguration(startingPoint, durations);
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public void setDefaultExpiresConfiguration(ExpiresConfiguration defaultExpiresConfiguration) {
         this.defaultExpiresConfiguration = defaultExpiresConfiguration;
     }
 
     public void setExcludedResponseStatusCodes(int[] excludedResponseStatusCodes) {
         this.excludedResponseStatusCodes = excludedResponseStatusCodes;
     }
 
     public void setExpiresConfigurationByContentType(Map<String, ExpiresConfiguration> expiresConfigurationByContentType) {
         this.expiresConfigurationByContentType = expiresConfigurationByContentType;
     }
 
     @Override
     public String toString() {
         return getClass().getSimpleName() + "[active=" + this.active + ", excludedResponseStatusCode="
                 + intsToCommaDelimitedString(this.excludedResponseStatusCodes) + ", default=" + this.defaultExpiresConfiguration
                 + ", byType=" + this.expiresConfigurationByContentType + "]";
     }
 }
