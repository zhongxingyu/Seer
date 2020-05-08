 /*
  * Copyright 2008-2010 Xebia and the original author or authors.
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
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.TimeZone;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.FilterHolder;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.springframework.mock.web.MockFilterConfig;
 import org.springframework.util.StringUtils;
 
 import fr.xebia.servlet.filter.ExpiresFilter.Duration;
 import fr.xebia.servlet.filter.ExpiresFilter.DurationUnit;
 import fr.xebia.servlet.filter.ExpiresFilter.ExpiresConfiguration;
 import fr.xebia.servlet.filter.ExpiresFilter.StartingPoint;
 
 public class ExpiresFilterTest {
 
     @Test
     public void testConfiguration() throws ServletException {
         MockFilterConfig filterConfig = new MockFilterConfig();
         filterConfig.addInitParameter("ExpiresDefault", "access plus 1 month");
        filterConfig.addInitParameter("ExpiresByType text/javascript", "access plus 1 year");
         filterConfig.addInitParameter("ExpiresByType text/html", "access plus 1 month 15 days 2 hours");
         filterConfig.addInitParameter("ExpiresByType image/gif", "modification plus 5 hours 3 minutes");
         filterConfig.addInitParameter("ExpiresByType image/jpg", "A10000");
         filterConfig.addInitParameter("ExpiresByType video/mpeg", "M20000");
         filterConfig.addInitParameter("ExpiresActive", "Off");
         filterConfig.addInitParameter("ExpiresExcludedResponseStatusCodes", "304, 503");
 
         ExpiresFilter expiresFilter = new ExpiresFilter();
         expiresFilter.init(filterConfig);
 
         Assert.assertEquals(false, expiresFilter.isActive());
 
         // VERIFY EXCLUDED RESPONSE STATUS CODES
         {
             int[] excludedResponseStatusCodes = expiresFilter.getExcludedResponseStatusCodesAsInts();
             Assert.assertEquals(2, excludedResponseStatusCodes.length);
             Assert.assertEquals(304, excludedResponseStatusCodes[0]);
             Assert.assertEquals(503, excludedResponseStatusCodes[1]);
         }
 
         // VERIFY DEFAULT CONFIGURATION
         {
             ExpiresConfiguration expiresConfiguration = expiresFilter.getDefaultExpiresConfiguration();
             Assert.assertEquals(StartingPoint.ACCESS_TIME, expiresConfiguration.getStartingPoint());
             Assert.assertEquals(1, expiresConfiguration.getDurations().size());
             Assert.assertEquals(DurationUnit.MONTH, expiresConfiguration.getDurations().get(0).getUnit());
             Assert.assertEquals(1, expiresConfiguration.getDurations().get(0).getAmount());
         }
 
        // VERIFY TEXT/JAVASCRIPT
        {
            ExpiresConfiguration expiresConfiguration = expiresFilter.getExpiresConfigurationByContentType().get("text/javascript");
            Assert.assertEquals(StartingPoint.ACCESS_TIME, expiresConfiguration.getStartingPoint());

            Assert.assertEquals(1, expiresConfiguration.getDurations().size());

            Duration oneYear = expiresConfiguration.getDurations().get(0);
            Assert.assertEquals(DurationUnit.YEAR, oneYear.getUnit());
            Assert.assertEquals(1, oneYear.getAmount());

        }
         // VERIFY TEXT/HTML
         {
             ExpiresConfiguration expiresConfiguration = expiresFilter.getExpiresConfigurationByContentType().get("text/html");
             Assert.assertEquals(StartingPoint.ACCESS_TIME, expiresConfiguration.getStartingPoint());
 
             Assert.assertEquals(3, expiresConfiguration.getDurations().size());
 
             Duration oneMonth = expiresConfiguration.getDurations().get(0);
             Assert.assertEquals(DurationUnit.MONTH, oneMonth.getUnit());
             Assert.assertEquals(1, oneMonth.getAmount());
 
             Duration fifteenDays = expiresConfiguration.getDurations().get(1);
             Assert.assertEquals(DurationUnit.DAY, fifteenDays.getUnit());
             Assert.assertEquals(15, fifteenDays.getAmount());
 
             Duration twoHours = expiresConfiguration.getDurations().get(2);
             Assert.assertEquals(DurationUnit.HOUR, twoHours.getUnit());
             Assert.assertEquals(2, twoHours.getAmount());
         }
         // VERIFY IMAGE/GIF
         {
             ExpiresConfiguration expiresConfiguration = expiresFilter.getExpiresConfigurationByContentType().get("image/gif");
             Assert.assertEquals(StartingPoint.LAST_MODIFICATION_TIME, expiresConfiguration.getStartingPoint());
 
             Assert.assertEquals(2, expiresConfiguration.getDurations().size());
 
             Duration fiveHours = expiresConfiguration.getDurations().get(0);
             Assert.assertEquals(DurationUnit.HOUR, fiveHours.getUnit());
             Assert.assertEquals(5, fiveHours.getAmount());
 
             Duration threeMinutes = expiresConfiguration.getDurations().get(1);
             Assert.assertEquals(DurationUnit.MINUTE, threeMinutes.getUnit());
             Assert.assertEquals(3, threeMinutes.getAmount());
 
         }
         // VERIFY IMAGE/JPG
         {
             ExpiresConfiguration expiresConfiguration = expiresFilter.getExpiresConfigurationByContentType().get("image/jpg");
             Assert.assertEquals(StartingPoint.ACCESS_TIME, expiresConfiguration.getStartingPoint());
 
             Assert.assertEquals(1, expiresConfiguration.getDurations().size());
 
             Duration tenThousandSeconds = expiresConfiguration.getDurations().get(0);
             Assert.assertEquals(DurationUnit.SECOND, tenThousandSeconds.getUnit());
             Assert.assertEquals(10000, tenThousandSeconds.getAmount());
 
         }
         // VERIFY VIDEO/MPEG
         {
             ExpiresConfiguration expiresConfiguration = expiresFilter.getExpiresConfigurationByContentType().get("video/mpeg");
             Assert.assertEquals(StartingPoint.LAST_MODIFICATION_TIME, expiresConfiguration.getStartingPoint());
 
             Assert.assertEquals(1, expiresConfiguration.getDurations().size());
 
             Duration twentyThousandSeconds = expiresConfiguration.getDurations().get(0);
             Assert.assertEquals(DurationUnit.SECOND, twentyThousandSeconds.getUnit());
             Assert.assertEquals(20000, twentyThousandSeconds.getAmount());
         }
     }
 
     /**
      * Test that a resource with empty content is also processed
      */
     @Test
     public void testEmptyContent() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("text/plain");
                 // no content is written in the response
             }
         };
 
         int expectedMaxAgeInSeconds = 7 * 60;
 
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testParseExpiresConfigurationCombinedDuration() {
         ExpiresFilter expiresFilter = new ExpiresFilter();
         ExpiresConfiguration actualConfiguration = expiresFilter.parseExpiresConfiguration("access plus 1 month 15 days 2 hours");
 
         Assert.assertEquals(StartingPoint.ACCESS_TIME, actualConfiguration.getStartingPoint());
 
         Assert.assertEquals(3, actualConfiguration.getDurations().size());
 
     }
 
     @Test
     public void testParseExpiresConfigurationMonoDuration() {
         ExpiresFilter expiresFilter = new ExpiresFilter();
         ExpiresConfiguration actualConfiguration = expiresFilter.parseExpiresConfiguration("access plus 2 hours");
 
         Assert.assertEquals(StartingPoint.ACCESS_TIME, actualConfiguration.getStartingPoint());
 
         Assert.assertEquals(1, actualConfiguration.getDurations().size());
         Assert.assertEquals(2, actualConfiguration.getDurations().get(0).getAmount());
         Assert.assertEquals(DurationUnit.HOUR, actualConfiguration.getDurations().get(0).getUnit());
 
     }
 
     @Test
     public void testSkipBecauseCacheControlMaxAgeIsDefined() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("text/xml; charset=utf-8");
                 response.addHeader("Cache-Control", "private, max-age=232");
                 response.getWriter().print("Hello world");
             }
         };
 
         int expectedMaxAgeInSeconds = 232;
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testExcludedResponseStatusCode() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                 response.addHeader("ETag", "W/\"1934-1269208821000\"");
                 response.addDateHeader("Date", System.currentTimeMillis());
             }
         };
 
         validate(servlet, null, HttpServletResponse.SC_NOT_MODIFIED);
     }
 
     @Test
     public void testNullContentType() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType(null);
             }
         };
 
         int expectedMaxAgeInSeconds = 1 * 60;
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testSkipBecauseExpiresIsDefined() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("text/xml; charset=utf-8");
                 response.addDateHeader("Expires", System.currentTimeMillis());
                 response.getWriter().print("Hello world");
             }
         };
 
         validate(servlet, null);
     }
 
     @Test
     public void testUseContentTypeExpiresConfiguration() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("text/xml; charset=utf-8");
                 response.getWriter().print("Hello world");
             }
         };
 
         int expectedMaxAgeInSeconds = 3 * 60;
 
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testUseContentTypeWithoutCharsetExpiresConfiguration() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("text/xml; charset=iso-8859-1");
                 response.getWriter().print("Hello world");
             }
         };
 
         int expectedMaxAgeInSeconds = 5 * 60;
 
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testUseDefaultConfiguration1() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("image/jpeg");
                 response.getWriter().print("Hello world");
             }
         };
 
         int expectedMaxAgeInSeconds = 1 * 60;
 
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testUseDefaultConfiguration2() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("image/jpeg");
                 response.addHeader("Cache-Control", "private");
 
                 response.getWriter().print("Hello world");
             }
         };
 
         int expectedMaxAgeInSeconds = 1 * 60;
 
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     @Test
     public void testUseMajorTypeExpiresConfiguration() throws Exception {
         HttpServlet servlet = new HttpServlet() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                 response.setContentType("text/json; charset=iso-8859-1");
                 response.getWriter().print("Hello world");
             }
         };
 
         int expectedMaxAgeInSeconds = 7 * 60;
 
         validate(servlet, expectedMaxAgeInSeconds);
     }
 
     protected void validate(HttpServlet servlet, Integer expectedMaxAgeInSeconds) throws Exception {
         validate(servlet, expectedMaxAgeInSeconds, HttpURLConnection.HTTP_OK);
     }
 
     protected void validate(HttpServlet servlet, Integer expectedMaxAgeInSeconds, int expectedResponseStatusCode) throws Exception {
         // SETUP
         int port = 6666;
         Server server = new Server(port);
         Context context = new Context(server, "/", Context.SESSIONS);
 
         MockFilterConfig filterConfig = new MockFilterConfig();
         filterConfig.addInitParameter("ExpiresDefault", "access plus 1 minute");
         filterConfig.addInitParameter("ExpiresByType text/xml; charset=utf-8", "access plus 3 minutes");
         filterConfig.addInitParameter("ExpiresByType text/xml", "access plus 5 minutes");
         filterConfig.addInitParameter("ExpiresByType text", "access plus 7 minutes");
         filterConfig.addInitParameter("ExpiresExcludedResponseStatusCodes", "304, 503");
 
         ExpiresFilter expiresFilter = new ExpiresFilter();
         expiresFilter.init(filterConfig);
 
         context.addFilter(new FilterHolder(expiresFilter), "/*", Handler.REQUEST);
 
         context.addServlet(new ServletHolder(servlet), "/test");
 
         server.start();
         try {
             Calendar.getInstance(TimeZone.getTimeZone("GMT"));
             long timeBeforeInMillis = System.currentTimeMillis();
 
             // TEST
             HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://localhost:" + port + "/test").openConnection();
 
             // VALIDATE
             Assert.assertEquals(expectedResponseStatusCode, httpURLConnection.getResponseCode());
 
             for (Entry<String, List<String>> field : httpURLConnection.getHeaderFields().entrySet()) {
                 System.out.println(field.getKey() + ": " + StringUtils.arrayToDelimitedString(field.getValue().toArray(), ", "));
             }
 
             Integer actualMaxAgeInSeconds;
 
             String cacheControlHeader = httpURLConnection.getHeaderField("Cache-Control");
             if (cacheControlHeader == null) {
                 actualMaxAgeInSeconds = null;
             } else {
                 actualMaxAgeInSeconds = null;
                 // System.out.println("Evaluate Cache-Control:" +
                 // cacheControlHeader);
                 StringTokenizer cacheControlTokenizer = new StringTokenizer(cacheControlHeader, ",");
                 while (cacheControlTokenizer.hasMoreTokens() && actualMaxAgeInSeconds == null) {
                     String cacheDirective = cacheControlTokenizer.nextToken();
                     // System.out.println("\tEvaluate directive: " +
                     // cacheDirective);
                     StringTokenizer cacheDirectiveTokenizer = new StringTokenizer(cacheDirective, "=");
                     // System.out.println("countTokens=" +
                     // cacheDirectiveTokenizer.countTokens());
                     if (cacheDirectiveTokenizer.countTokens() == 2) {
                         String key = cacheDirectiveTokenizer.nextToken().trim();
                         String value = cacheDirectiveTokenizer.nextToken().trim();
                         if (key.equalsIgnoreCase("max-age")) {
                             actualMaxAgeInSeconds = Integer.parseInt(value);
                         }
                     }
                 }
             }
 
             if (expectedMaxAgeInSeconds == null) {
                 Assert.assertNull("actualMaxAgeInSeconds '" + actualMaxAgeInSeconds + "' should be null", actualMaxAgeInSeconds);
                 return;
             }
 
             Assert.assertNotNull(actualMaxAgeInSeconds);
 
             int deltaInSeconds = Math.abs(actualMaxAgeInSeconds - expectedMaxAgeInSeconds);
             Assert.assertTrue("actualMaxAgeInSeconds: " + actualMaxAgeInSeconds + ", expectedMaxAgeInSeconds: " + expectedMaxAgeInSeconds
                     + ", request time: " + timeBeforeInMillis + " for content type " + httpURLConnection.getContentType(),
                     deltaInSeconds < 2000);
 
         } finally {
             server.stop();
         }
     }
 
     @Test
     public void testIntsToCommaDelimitedString() {
         String actual = ExpiresFilter.intsToCommaDelimitedString(new int[] { 500, 503 });
         String expected = "500, 503";
 
         Assert.assertEquals(expected, actual);
     }
 }
