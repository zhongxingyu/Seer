 /*
  * Copyright 2012 CoreMedia AG
  *
  * This file is part of Joala.
  *
  * Joala is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Joala is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Joala.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.joala.expression.library.net;
 
 import net.joala.expression.ExpressionEvaluationException;
 import net.joala.net.EmbeddedWebservice;
 import net.joala.time.Timeout;
 import net.joala.time.TimeoutImpl;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.util.concurrent.TimeUnit;
 
 import static java.net.InetAddress.getByName;
 import static net.joala.net.DelayedResponse.delay;
 import static net.joala.net.PortUtils.freePort;
 import static net.joala.net.StatusCodeResponse.statusCode;
 import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
 import static org.junit.Assert.assertEquals;
 
 /**
  * <p>
  * Tests {@link UriStatusCodeExpression}.
  * </p>
  *
  * @since 10/2/12
  */
 @SuppressWarnings("ProhibitedExceptionDeclared")
 public class UriStatusCodeExpressionTest {
   private static EmbeddedWebservice webservice;
   private static final int SOME_PORT = 12345;
  private static final long TIMED_OUT_RESPONSE_DELAY_MILLIS = 5L;
   private static final Timeout FAST_TIMEOUT = new TimeoutImpl(1L, TimeUnit.MILLISECONDS);
 
   @BeforeClass
   public static void setUpClass() throws Exception {
     webservice = new EmbeddedWebservice();
     webservice.start();
   }
 
   @AfterClass
   public static void tearDownClass() throws Exception {
     webservice.stop();
   }
 
   @Before
   public void setUp() throws Exception {
     webservice.getHttpHandler().clearResponses();
   }
 
   @After
   public void tearDown() throws Exception {
     webservice.getHttpHandler().clearResponses();
   }
 
   @Test(expected = IllegalStateException.class)
   public void unknownHosts_should_fail_with_exception() throws Exception {
     final URI clientUri = URI.create(String.format("http://%s:%d/", randomAlphabetic(10), SOME_PORT));
     new UriStatusCodeExpression(clientUri).get();
   }
 
   @Test
   public void get_should_work_for_HTTP_OK() throws Exception {
     final int expectedStatusCode = HttpURLConnection.HTTP_OK;
     webservice.getHttpHandler().feedResponses(statusCode(expectedStatusCode));
     final int actualStatusCode = new UriStatusCodeExpression(webservice.getClientUri()).get();
     assertEquals("Retrieved status code should match expected one.", expectedStatusCode, actualStatusCode);
     assertEquals("All fed responses should have been read.", 0, webservice.getHttpHandler().availableResponses());
   }
 
   @Test
   public void get_should_work_for_HTTP_NOT_FOUND() throws Exception {
     final int expectedStatusCode = HttpURLConnection.HTTP_NOT_FOUND;
     webservice.getHttpHandler().feedResponses(statusCode(expectedStatusCode));
     final int actualStatusCode = new UriStatusCodeExpression(webservice.getClientUri()).get();
     assertEquals("Retrieved status code should match expected one.", expectedStatusCode, actualStatusCode);
     assertEquals("All fed responses should have been read.", 0, webservice.getHttpHandler().availableResponses());
   }
 
   @Test(expected = ExpressionEvaluationException.class)
   public void get_should_signal_retry_for_unbound_port() throws Exception {
     final URI clientUri = URI.create(String.format("http://%s:%d/", getByName(null).getHostName(), freePort()));
     new UriStatusCodeExpression(clientUri).get();
   }
 
   @Test(expected = ExpressionEvaluationException.class)
   public void get_should_signal_retry_after_timeout() throws Exception {
     webservice.getHttpHandler().feedResponses(delay(TIMED_OUT_RESPONSE_DELAY_MILLIS, statusCode(HttpURLConnection.HTTP_OK)));
     new UriStatusCodeExpression(FAST_TIMEOUT, webservice.getClientUri()).get();
   }
 
 }
