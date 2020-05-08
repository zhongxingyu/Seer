 package org.ict4h.integration;
 
 import org.ict4h.atomfeed.IntegrationTest;
 import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.domain.Marker;
 import org.ict4h.atomfeed.client.factory.AtomClientFactory;
 import org.ict4h.atomfeed.client.repository.memory.AllFailedEventsInMemoryImpl;
 import org.ict4h.atomfeed.client.repository.memory.AllMarkersInMemoryImpl;
 import org.ict4h.atomfeed.client.service.AtomFeedClient;
 import org.ict4h.atomfeed.client.service.EventWorker;
 import org.ict4h.atomfeed.server.domain.EventRecord;
 import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
 import org.ict4h.integration.repository.DbEventRecordCreator;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import static junit.framework.Assert.assertEquals;
 
 public class AtomFeedClientIT extends IntegrationTest {
 
     private DbEventRecordCreator recordCreator;
     private Connection connection;
     private AllEventRecordsJdbcImpl eventRecords;
     private AtomFeedClient atomFeedClient;
 
     @Before
     public void before() throws SQLException {
         connection = getConnection();
         Statement statement = connection.createStatement();
         statement.execute("TRUNCATE atomfeed.event_records  RESTART IDENTITY;");
         statement.close();
         eventRecords = new AllEventRecordsJdbcImpl(getProvider(connection));
         recordCreator = new DbEventRecordCreator(eventRecords);
     }
 
     @After
     public void after() throws SQLException {
         connection.close();
     }
 
     @Test
     public void shouldReadEventsCreatedEvents() throws URISyntaxException, SQLException {
         List<EventRecord> events = createEvents(7, "Hello, DiscWorld");
         final int[] counter = {0};
         URI uri = new URI("http://localhost:8080/feed/recent");
         AllMarkersInMemoryImpl allMarkersInMemoryImpl = new AllMarkersInMemoryImpl();
         allMarkersInMemoryImpl.put(uri, events.get(4).getTagUri().toString(), new URI("http://localhost:8080/feed/1"));
         atomFeedClient = new AtomClientFactory().create(allMarkersInMemoryImpl, new AllFailedEventsInMemoryImpl());
         atomFeedClient.processEvents(uri, new EventWorker() {
             @Override
             public void process(Event event) {
                 counter[0] += 1;
             }
         });
         assertEquals(2, counter[0]);
     }
 
     private List<EventRecord> createEvents(int numberOfEventsToCreate, String titleTemplate) throws URISyntaxException, SQLException {
         List<EventRecord> records = new ArrayList<EventRecord>();
         int index = 1;
         do {
             records.add(createOneEvent(String.format("%s%s", titleTemplate, index), String.format("%s%s", "http://google.com?q=", index)));
             index++;
         } while (index <= numberOfEventsToCreate);
         return records;
     }
 
     private EventRecord createOneEvent(String title, String url) throws URISyntaxException, SQLException {
         EventRecord eventRecord = recordCreator.create(UUID.randomUUID().toString(), title, url, null);
        connection.commit();
         return eventRecord;
     }
 }
