 package org.sakaiproject.search.solr.indexing;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.util.AbstractSolrTestCase;
 import org.hamcrest.CoreMatchers;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.producer.ContentProducerFactory;
 import org.sakaiproject.search.producer.ProducersHelper;
 
 import java.util.Date;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 
 /**
  * @author Colin Hebert
  */
 @org.apache.lucene.util.LuceneTestCase.SuppressCodecs({"Lucene3x","Lucene40"})
 public class SolrTaskHandlerIT extends AbstractSolrTestCase {
     private SolrTools solrTools;
     private ContentProducerFactory contentProducerFactory;
     private SolrServer solrServer;
     private SolrTaskHandler solrTaskHandler;
 
     @BeforeClass
     public static void beforeClass() throws Exception {
         initCore("org/sakaiproject/search/solr/conf/search/conf/solrconfig.xml",
                 "org/sakaiproject/search/solr/conf/search/conf/schema.xml",
                 "org/sakaiproject/search/solr/conf/search/");
     }
 
     @Before
     @Override
     public void setUp() throws Exception {
         super.setUp();
         solrServer = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
         clearIndex();
 
         solrTaskHandler = new SolrTaskHandler();
         solrTaskHandler.setSolrServer(solrServer);
         solrTools = new SolrTools();
         solrTaskHandler.setSolrTools(solrTools);
         solrTools.setSolrServer(solrServer);
         contentProducerFactory = new ContentProducerFactory();
         solrTools.setContentProducerFactory(contentProducerFactory);
     }
 
     @Test
     public void testIndexDocument() throws Exception {
         String reference = "testIndexDocument";
         DateTime actionDate = new DateTime(2013, 3, 10, 17, 0, 0);
         contentProducerFactory.addContentProducer(ProducersHelper.getStringContentProducer(reference));
         assertIndexIsEmpty();
 
         solrTaskHandler.indexDocument(reference, actionDate.toDate());
 
         SolrDocumentList results = getSolrDocuments();
         // A new documents has been created
         assertThat(results.getNumFound(), is(1L));
         // The document matches the input
         assertDocumentMatches(results.get(0), reference, actionDate.toDate());
     }
 
     @Test
     public void testIndexDocumentOutdatedFails() throws Exception {
         String reference = "testIndexDocument";
         DateTime indexationDate = new DateTime(2013, 3, 10, 18, 0, 0);
         DateTime actionDate = new DateTime(2013, 3, 10, 17, 0, 0);
         contentProducerFactory.addContentProducer(ProducersHelper.getStringContentProducer(reference));
         addDocumentToIndex(reference, indexationDate);
 
         solrTaskHandler.indexDocument(reference, actionDate.toDate());
 
         SolrDocumentList results = getSolrDocuments();
         // No new documents have been created
         assertThat(results.getNumFound(), is(1L));
         // The document hasn't been modified
         assertDocumentMatches(results.get(0), reference, indexationDate.toDate());
     }
 
     @Test
     public void testRemoveDocument() throws Exception {
         String reference = "testRemoveDocument";
         DateTime indexationDate = new DateTime(2013, 3, 10, 16, 0, 0);
         DateTime actionDate = new DateTime(2013, 3, 10, 17, 0, 0);
         contentProducerFactory.addContentProducer(ProducersHelper.getStringContentProducer(reference));
         addDocumentToIndex(reference, indexationDate);
 
         solrTaskHandler.removeDocument(reference, actionDate.toDate());
 
         assertIndexIsEmpty();
     }
 
     @Test
     public void testRemoveDocumentOutdatedFails() throws Exception {
         String reference = "testRemoveDocument";
         DateTime indexationDate = new DateTime(2013, 3, 10, 18, 0, 0);
         DateTime actionDate = new DateTime(2013, 3, 10, 17, 0, 0);
         contentProducerFactory.addContentProducer(ProducersHelper.getStringContentProducer(reference));
         addDocumentToIndex(reference, indexationDate);
 
         solrTaskHandler.removeDocument(reference, actionDate.toDate());
 
        assertIndexIsEmpty();
     }
 
     private void assertIndexIsEmpty() throws Exception {
         assertThat(getSolrDocuments().getNumFound(), is(0L));
     }
 
     private void assertDocumentMatches(SolrDocument document, String reference, Date actionDate) {
         assertDocumentMatches(document, reference);
         assertThat((Date) document.getFieldValue(SearchService.DATE_STAMP), equalTo(actionDate));
     }
 
     private void assertDocumentMatches(SolrDocument document, String reference) {
         EntityContentProducer contentProducer = contentProducerFactory.getContentProducerForElement(reference);
 
         assertThat(document.getFieldValue(SearchService.FIELD_REFERENCE),
                 CoreMatchers.<Object>equalTo(reference));
         assertThat(document.getFieldValue(SearchService.FIELD_CONTAINER),
                 CoreMatchers.<Object>equalTo(contentProducer.getContainer(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_TYPE),
                 CoreMatchers.<Object>equalTo(contentProducer.getType(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_TITLE),
                 CoreMatchers.<Object>equalTo(contentProducer.getTitle(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_TOOL),
                 CoreMatchers.<Object>equalTo(contentProducer.getTool()));
         assertThat(document.getFieldValue(SearchService.FIELD_URL),
                 CoreMatchers.<Object>equalTo(contentProducer.getUrl(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_SITEID),
                 CoreMatchers.<Object>equalTo(contentProducer.getSiteId(reference)));
 
     }
 
     private void addDocumentToIndex(String reference, DateTime indexationDate) throws Exception {
         solrTaskHandler.indexDocument(reference, indexationDate.toDate());
         solrServer.commit();
     }
 
     private SolrDocumentList getSolrDocuments() throws Exception {
         solrServer.commit();
         SolrQuery query = new SolrQuery("*:*");
         QueryResponse qr = solrServer.query(query);
         return qr.getResults();
     }
 }
