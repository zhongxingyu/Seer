 package org.purl.wf4ever.rosrs.client.notifications;
 
 import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
 import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
 import static com.github.tomakehurst.wiremock.client.WireMock.get;
 import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
 import static com.github.tomakehurst.wiremock.client.WireMock.matching;
 import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
 import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
 import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
 import static com.github.tomakehurst.wiremock.client.WireMock.verify;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.io.IOUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.ISODateTimeFormat;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.purl.wf4ever.rosrs.client.exception.NotificationsException;
 
 import com.github.tomakehurst.wiremock.junit.WireMockRule;
 
 /**
  * Test of the {@link NotificationService} class.
  * 
  * @author piotrekhol
  * 
  */
 public class NotificationServiceTest {
 
     /** Service URI mapped to a test service description document. */
     private static final URI EXAMPLE_SERVICE_URI = URI.create("http://example.org/notifications/");
 
     /** Service URI of the mock HTTP server, mapped to a test service description document. */
     private static final URI MOCK_SERVICE_URI = URI.create("http://localhost:8089/");
 
     /** Notifications stored in the test feed. */
     private static List<Notification> exampleNotifications;
 
     /** A test HTTP mock server. */
     @Rule
     public static final WireMockRule WIREMOCK_RULE = new WireMockRule(8089); // No-args constructor defaults to port 8080
 
 
     /**
      * Create the expected notifications.
      * 
      * @throws Exception
      *             when something unexpected happens
      */
     @BeforeClass
     public static void setUpBeforeClass()
             throws Exception {
         exampleNotifications = new ArrayList<>();
 
         Notification n1 = new Notification("urn:X-rodl:13", "Quality improvement", "<p>Lorem ipsum</p>");
         n1.setPublished(ISODateTimeFormat.dateTimeParser().parseDateTime("2005-06-13T16:20:02Z"));
         n1.setSource(URI.create("http://sandbox.wf4ever-project.org/roevaluate/"));
         n1.setResearchObjectUri(URI.create("http://example.org/rodl/ROs/myGenes/"));
         exampleNotifications.add(n1);
 
         Notification n2 = new Notification("urn:X-rodl:15", "Quality decrease", "<strong>Lorem ipsum</strong>");
         n2.setPublished(ISODateTimeFormat.dateTimeParser().parseDateTime("2005-07-21T11:45:07Z"));
         n2.setSource(URI.create("http://sandbox.wf4ever-project.org/roevaluate/"));
         n2.setResearchObjectUri(URI.create("http://example.org/rodl/ROs/myPuzzles/"));
         exampleNotifications.add(n2);
     }
 
 
     /**
      * Test that the service description document is loaded properly.
      */
     @Test
     public final void testInit() {
         NotificationService notificationService = new NotificationService(EXAMPLE_SERVICE_URI, null);
         notificationService.init();
         Assert.assertEquals("notifications{?ro,from,to,source,limit}",
             notificationService.getNotificationsUriTemplateString());
     }
 
 
     /**
      * Test that the notification URI template is expanded properly for all criteria.
      * 
      * @throws UnsupportedEncodingException
      *             when the URL encoder in test uses an invalid encoding
      */
     @Test
     public final void testGetNotificationsUriForAllCriteria()
             throws UnsupportedEncodingException {
         NotificationService notificationService = new NotificationService(EXAMPLE_SERVICE_URI, null);
         URI roUri = URI.create("http://example.org/ROs/ro1/");
         DateTime from = ISODateTimeFormat.dateTimeParser().parseDateTime("2000-06-13T18:20:02.000+02:00");
         DateTime to = ISODateTimeFormat.dateTimeParser().parseDateTime("2006-06-13T18:20:02.000+02:00");
         URI source = URI.create("http://www.example.com/exampleRO/");
         Integer limit = 5;
         URI expectedAll = UriBuilder.fromUri(EXAMPLE_SERVICE_URI).path("notifications")
                 .queryParam("ro", URLEncoder.encode("http://example.org/ROs/ro1/", "UTF-8"))
                .queryParam("from", URLEncoder.encode("2000-06-13T18:20:02.000+02:00", "UTF-8"))
                .queryParam("to", URLEncoder.encode("2006-06-13T18:20:02.000+02:00", "UTF-8"))
                 .queryParam("source", URLEncoder.encode("http://www.example.com/exampleRO/", "UTF-8"))
                 .queryParam("limit", URLEncoder.encode("5", "UTF-8")).build();
         Assert.assertEquals(expectedAll, notificationService.getNotificationsUri(roUri, from, to, source, limit));
     }
 
 
     /**
      * Test that the notification URI template is expanded properly for 'RO', 'from' and 'to' criteria.
      * 
      * @throws UnsupportedEncodingException
      *             when the URL encoder in test uses an invalid encoding
      */
     @Test
     public final void testGetNotificationsUriForROFromToCriteria()
             throws UnsupportedEncodingException {
         NotificationService notificationService = new NotificationService(EXAMPLE_SERVICE_URI, null);
         URI roUri = URI.create("http://example.org/ROs/ro1/");
         DateTime from = ISODateTimeFormat.dateTimeParser().parseDateTime("2000-06-13T18:20:02.000+02:00");
         DateTime to = ISODateTimeFormat.dateTimeParser().parseDateTime("2006-06-13T18:20:02.000+02:00");
         URI expectedAll = UriBuilder.fromUri(EXAMPLE_SERVICE_URI).path("notifications")
                 .queryParam("ro", URLEncoder.encode("http://example.org/ROs/ro1/", "UTF-8"))
                .queryParam("from", URLEncoder.encode("2000-06-13T18:20:02.000+02:00", "UTF-8"))
                .queryParam("to", URLEncoder.encode("2006-06-13T18:20:02.000+02:00", "UTF-8")).build();
         Assert.assertEquals(expectedAll, notificationService.getNotificationsUri(roUri, from, to));
     }
 
 
     /**
      * Test that the notification URI template is expanded properly if only one criterion is provided.
      * 
      * @throws UnsupportedEncodingException
      *             when the URL encoder in test uses an invalid encoding
      */
     @Test
     public final void testGetNotificationsUriForOneCriterion()
             throws UnsupportedEncodingException {
         NotificationService notificationService = new NotificationService(EXAMPLE_SERVICE_URI, null);
         DateTime from = ISODateTimeFormat.dateTimeParser().parseDateTime("2000-06-13T18:20:02.000+02:00");
 
         URI expectedOnlyFrom = UriBuilder.fromUri(EXAMPLE_SERVICE_URI).path("notifications")
                .queryParam("from", URLEncoder.encode("2000-06-13T18:20:02.000+02:00", "UTF-8")).build();
         Assert.assertEquals(expectedOnlyFrom, notificationService.getNotificationsUri(null, from, null));
     }
 
 
     /**
      * Test that an example feed can be parsed.
      * 
      * @throws NotificationsException
      *             the example feed is invalid
      */
     @Test
     public final void testGetNotificationsInputStream()
             throws NotificationsException {
         InputStream feed = getClass().getClassLoader().getResourceAsStream("notifications/exampleFeed.xml");
         NotificationService notificationService = new NotificationService(EXAMPLE_SERVICE_URI, null);
         List<Notification> notifications = notificationService.getNotifications(feed);
         Assert.assertNotNull(notifications);
         Assert.assertEquals(exampleNotifications.size(), notifications.size());
         for (int i = 0; i < notifications.size(); i++) {
             assertNotificationsEquals(exampleNotifications.get(i), notifications.get(i));
         }
     }
 
 
     /**
      * Check that all properties of two notifications are the same.
      * 
      * @param n1
      *            expected notification
      * @param n2
      *            tested notification
      */
     private void assertNotificationsEquals(Notification n1, Notification n2) {
         Assert.assertEquals(n1.getId(), n2.getId());
         Assert.assertEquals(n1.getTitle(), n2.getTitle());
         Assert.assertEquals(n1.getContent(), n2.getContent());
         Assert.assertEquals(n1.getPublished(), n2.getPublished());
         Assert.assertEquals(n1.getSource(), n2.getSource());
         Assert.assertEquals(n1.getResearchObjectUri(), n2.getResearchObjectUri());
     }
 
 
     /**
      * Test that the service can fetch the notifications from a HTTP server.
      * 
      * @throws NotificationsException
      *             when the test feed is invalid
      * @throws IOException
      *             when the mock HTTP server has problems
      */
     @Test
     public final void testGetNotificationsURIDateTimeDateTime()
             throws NotificationsException, IOException {
         // this is the path of the URI that the NotificationService should call
         String serviceUrl = "/";
        String feedUrl = "/notifications?ro=http%3A%2F%2Fexample.org%2FROs%2Fro1%2F&from=2000-06-13T18%3A20%3A02.000%2B02%3A00&to=2006-06-13T18%3A20%3A02.000%2B02%3A00";
         // this is what the mock HTTP server will return
         InputStream serviceDesc = getClass().getClassLoader().getResourceAsStream(
             "notifications/serviceDescription.rdf");
         InputStream feed = getClass().getClassLoader().getResourceAsStream("notifications/exampleFeed.xml");
         // here we configure the mock HTTP server
         stubFor(get(urlEqualTo(feedUrl)).withHeader("Accept", equalTo(MediaType.APPLICATION_ATOM_XML)).willReturn(
             aResponse().withStatus(200).withHeader("Content-Type", MediaType.APPLICATION_ATOM_XML)
                     .withBody(IOUtils.toByteArray(feed))));
         stubFor(get(urlEqualTo(serviceUrl)).willReturn(
             aResponse().withStatus(200).withHeader("Content-Type", "application/rdf+xml")
                     .withBody(IOUtils.toByteArray(serviceDesc))));
 
         URI roUri = URI.create("http://example.org/ROs/ro1/");
         DateTime from = ISODateTimeFormat.dateTimeParser().parseDateTime("2000-06-13T18:20:02.000+02:00");
         DateTime to = ISODateTimeFormat.dateTimeParser().parseDateTime("2006-06-13T18:20:02.000+02:00");
 
         NotificationService notificationService = new NotificationService(MOCK_SERVICE_URI, null);
         List<Notification> notifications = notificationService.getNotifications(roUri, from, to);
         Assert.assertNotNull(notifications);
         Assert.assertEquals(exampleNotifications.size(), notifications.size());
         for (int i = 0; i < notifications.size(); i++) {
             assertNotificationsEquals(exampleNotifications.get(i), notifications.get(i));
         }
 
         // make sure the request was made
         verify(getRequestedFor(urlMatching("/")));
         verify(getRequestedFor(urlMatching("/notifications?.+")).withHeader("Content-Type",
             matching(MediaType.APPLICATION_ATOM_XML)));
     }
 }
