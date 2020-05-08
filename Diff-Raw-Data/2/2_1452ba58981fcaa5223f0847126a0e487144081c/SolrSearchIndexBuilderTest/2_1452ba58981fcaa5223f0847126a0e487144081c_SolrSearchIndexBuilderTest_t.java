 package uk.ac.ox.oucs.search.solr;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.util.AbstractSolrTestCase;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.event.api.Notification;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.model.SearchBuilderItem;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.ToolConfiguration;
 import uk.ac.ox.oucs.search.solr.producer.BinaryEntityContentProducer;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Colin Hebert
  */
 public class SolrSearchIndexBuilderTest extends AbstractSolrTestCase {
     private SolrSearchIndexBuilder solrSearchIndexBuilder;
     private SolrServer solrServer;
     private BinaryEntityContentProducer binaryContentProducer;
     private EntityContentProducer contentProducer;
     private Notification notification;
 
     @Before
     public void setUp() throws Exception {
         super.setUp();
         solrServer = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
 
         solrSearchIndexBuilder = new SolrSearchIndexBuilder();
         SiteService siteService = mock(SiteService.class);
         binaryContentProducer = mock(BinaryEntityContentProducer.class);
         contentProducer = mock(EntityContentProducer.class);
         notification = mock(Notification.class);
 
         solrSearchIndexBuilder.setSolrServer(solrServer);
         solrSearchIndexBuilder.setSiteService(siteService);
         solrSearchIndexBuilder.registerEntityContentProducer(binaryContentProducer);
         solrSearchIndexBuilder.registerEntityContentProducer(contentProducer);
 
         // SiteService think that all sites have the search tool enabled
         when(siteService.getSite(anyString())).then(new Answer<Site>() {
             @Override
             public Site answer(InvocationOnMock invocationOnMock) throws Throwable {
                 Site site = mock(Site.class);
                 when(site.getToolForCommonId(anyString())).thenReturn(mock(ToolConfiguration.class));
                 return site;
             }
         });
     }
 
     @Test
     public void testAddBinaryResource() throws Exception {
         Event event = mockBinaryEvent("BinaryFile",
                 SolrSearchServiceTest.class.getResourceAsStream("/uk/ac/ox/oucs/search/solr/refcard.pdf"));
         solrSearchIndexBuilder.addResource(notification, event);
 
         SolrQuery query = new SolrQuery();
         query.setQuery("*:*");
         QueryResponse rsp = solrServer.query(query);
         assertEquals(1, rsp.getResults().getNumFound());
     }
 
     @Test
     public void testAddReaderResource() throws Exception {
         Event event = mockReaderEvent("TextFile",
                 new InputStreamReader(SolrSearchServiceTest.class.getResourceAsStream("/uk/ac/ox/oucs/search/solr/README.markdown")));
         solrSearchIndexBuilder.addResource(notification, event);
 
         SolrQuery query = new SolrQuery();
         query.setQuery("*:*");
         QueryResponse rsp = solrServer.query(query);
         assertEquals(1, rsp.getResults().getNumFound());
     }
 
     @Test
     public void testAddStringResource() throws Exception {
         Event event = mockStringEvent("StringContent", "Random string content that will be indexed");
         solrSearchIndexBuilder.addResource(notification, event);
 
         SolrQuery query = new SolrQuery();
         query.setQuery("*:*");
         QueryResponse rsp = solrServer.query(query);
         assertEquals(1, rsp.getResults().getNumFound());
     }
 
     @Test
     public void testRemoveResource() throws Exception {
         Event addEvent = mockStringEvent("RemovableContent", "Random string content that will be indexed");
         solrSearchIndexBuilder.addResource(notification, addEvent);
         Event removeEvent = mockRemoveEvent("RemovableContent", contentProducer);
         solrSearchIndexBuilder.addResource(notification, removeEvent);
 
         SolrQuery query = new SolrQuery();
         query.setQuery("*:*");
         QueryResponse rsp = solrServer.query(query);
         assertEquals(0, rsp.getResults().getNumFound());
     }
 
     @Override
     public String getSchemaFile() {
         return "solr/conf/schema.xml";
     }
 
     @Override
     public String getSolrConfigFile() {
         return "solr/conf/solrconfig.xml";
     }
 
     public static Event mockRemoveEvent(String reference, EntityContentProducer entityContentProducer) throws Exception {
         Event event = mock(Event.class);
         when(event.getResource()).thenReturn(reference);
         when(entityContentProducer.matches(event)).thenReturn(true);
         when(entityContentProducer.getId(reference)).thenReturn(reference + ".id");
         when(entityContentProducer.getAction(event)).thenReturn(SearchBuilderItem.ACTION_DELETE);
         return event;
     }
 
     public static Event mockAddEvent(final String reference, EntityContentProducer entityContentProducer) throws Exception {
         Event event = mock(Event.class);
         when(event.getResource()).thenReturn(reference);
         when(entityContentProducer.matches(event)).thenReturn(true);
         when(entityContentProducer.getSiteId(reference)).thenReturn(reference + ".siteId");
         when(entityContentProducer.getContainer(reference)).thenReturn(reference + ".container");
         when(entityContentProducer.getId(reference)).thenReturn(reference + ".id");
         when(entityContentProducer.getType(reference)).thenReturn(reference + ".type");
         when(entityContentProducer.getSubType(reference)).thenReturn(reference + ".subtype");
         when(entityContentProducer.getTitle(reference)).thenReturn(reference + ".title");
         when(entityContentProducer.getTool()).thenReturn(reference + ".tool");
         when(entityContentProducer.getUrl(reference)).thenReturn(reference + ".url");
         when(entityContentProducer.getCustomProperties(reference)).then(new Answer<Map<String, ?>>() {
             @Override
             public Map<String, ?> answer(InvocationOnMock invocationOnMock) throws Throwable {
                 return new HashMap<String, Object>() {
                     {
                         this.put("dc_author", reference + "author");
                         this.put("dc_title", reference + "title");
                         this.put("dc_description", reference + "description");
                     }
                 };
             }
         });
 
 
         when(entityContentProducer.getAction(event)).thenReturn(SearchBuilderItem.ACTION_ADD);
         return event;
     }
 
     public Event mockStringEvent(String reference, String content) throws Exception {
         Event event = mockAddEvent(reference, contentProducer);
         when(contentProducer.isContentFromReader(reference)).thenReturn(false);
         when(contentProducer.getContent(reference)).thenReturn(content);
         return event;
     }
 
     public Event mockReaderEvent(String reference, Reader content) throws Exception {
         Event event = mockAddEvent(reference, contentProducer);
         when(contentProducer.isContentFromReader(reference)).thenReturn(true);
         when(contentProducer.getContentReader(reference)).thenReturn(content);
         return event;
     }
 
     public Event mockBinaryEvent(String reference, InputStream content) throws Exception {
         Event event = mockAddEvent(reference, binaryContentProducer);
        when(binaryContentProducer.isContentFromReader(reference)).thenReturn(false);
         when(binaryContentProducer.getContentStream(reference)).thenReturn(content);
         return event;
     }
 }
