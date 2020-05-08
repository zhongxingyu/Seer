 package uk.ac.ox.oucs.search.solr.indexing;
 
 import com.google.common.collect.Iterators;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrRequest;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
 import org.apache.solr.client.solrj.request.UpdateRequest;
 import org.apache.solr.client.solrj.util.ClientUtils;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.common.SolrInputField;
 import org.apache.solr.common.util.ContentStreamBase;
 import org.apache.solr.common.util.DateUtil;
 import org.apache.solr.common.util.NamedList;
 import org.apache.tika.Tika;
 import org.apache.tika.metadata.Metadata;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchIndexBuilder;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ox.oucs.search.producer.BinaryEntityContentProducer;
 import uk.ac.ox.oucs.search.producer.ContentProducerFactory;
 import uk.ac.ox.oucs.search.solr.SolrSearchIndexBuilder;
 import uk.ac.ox.oucs.search.solr.util.AdminStatRequest;
 import uk.ac.ox.oucs.search.solr.util.UpdateRequestReader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * @author Colin Hebert
  */
 public class SolrTools {
     public static final String LITERAL = "literal.";
     public static final String PROPERTY_PREFIX = "property_";
     public static final String UPREFIX = PROPERTY_PREFIX + "tika_";
     public static final String SOLRCELL_PATH = "/update/extract";
     private static final Logger logger = LoggerFactory.getLogger(SolrTools.class);
     private SiteService siteService;
     private SearchIndexBuilder searchIndexBuilder;
     private ContentProducerFactory contentProducerFactory;
     private SolrServer solrServer;
     private boolean tikaEnabled;
     private Tika tika;
 
     public void init() {
         if (tikaEnabled)
             tika = new Tika();
     }
 
     /**
      * Generate a {@link SolrRequest} to index the given resource thanks to its {@link EntityContentProducer}
      *
      * @param resourceName    resource to index
      * @param contentProducer content producer associated with the resource
      * @return an update request for the resource
      */
     public SolrRequest toSolrRequest(String resourceName, Date actionDate, EntityContentProducer contentProducer) {
         if (logger.isDebugEnabled())
             logger.debug("Create a solr request to add '" + resourceName + "' to the index");
         SolrRequest request;
         SolrInputDocument document = generateBaseSolrDocument(resourceName, actionDate, contentProducer);
         if (logger.isDebugEnabled())
             logger.debug("Base solr document created ." + document);
 
         //Prepare the actual request based on a stream/reader/string
         if (contentProducer instanceof BinaryEntityContentProducer) {
             BinaryEntityContentProducer binaryContentProducer = (BinaryEntityContentProducer) contentProducer;
             if (!tikaEnabled) {
                 if (logger.isDebugEnabled())
                     logger.debug("Create a SolrCell request");
                 request = prepareSolrCellRequest(resourceName, binaryContentProducer, document);
             } else {
                 if (logger.isDebugEnabled())
                     logger.debug("Create a request based on a document parsed by Tika");
                 setDocumentTikaProperties(resourceName, document, binaryContentProducer);
                 request = new UpdateRequest().add(document);
             }
         } else if (contentProducer.isContentFromReader(resourceName)) {
             if (logger.isDebugEnabled())
                 logger.debug("Create a request with a Reader");
             document.setField(SearchService.FIELD_CONTENTS, contentProducer.getContentReader(resourceName));
             request = new UpdateRequestReader().add(document);
         } else {
             if (logger.isDebugEnabled())
                 logger.debug("Create a request based on a String");
             document.setField(SearchService.FIELD_CONTENTS, contentProducer.getContent(resourceName));
             request = new UpdateRequest().add(document);
         }
 
         return request;
     }
 
     private void setDocumentTikaProperties(String resourceName, SolrInputDocument document, BinaryEntityContentProducer binaryContentProducer) {
         try {
             Metadata metadata = new Metadata();
             String documentContent = tika.parseToString(binaryContentProducer.getContentStream(resourceName), metadata);
             document.setField(SearchService.FIELD_CONTENTS, documentContent);
 
             for (String metadataName : metadata.names())
                 for (String metadataValue : metadata.getValues(metadataName))
                     document.addField(UPREFIX + metadataName, metadataValue);
         } catch (Exception e) {
             logger.warn("Couldn't parse the content of '" + resourceName + "'", e);
         }
     }
 
     /**
      * Create a solrDocument for a specific resource
      *
      * @param resourceName    resource used to generate the document
      * @param contentProducer contentProducer in charge of extracting the data
      * @return a SolrDocument
      */
     private SolrInputDocument generateBaseSolrDocument(String resourceName, Date actionDate, EntityContentProducer contentProducer) {
         SolrInputDocument document = new SolrInputDocument();
 
         //The date_stamp field should be automatically set by solr (default="NOW"), if it isn't set here
         document.addField(SearchService.DATE_STAMP, format(actionDate));
         document.addField(SearchService.FIELD_CONTAINER, contentProducer.getContainer(resourceName));
         document.addField(SearchService.FIELD_ID, contentProducer.getId(resourceName));
         document.addField(SearchService.FIELD_TYPE, contentProducer.getType(resourceName));
         document.addField(SearchService.FIELD_SUBTYPE, contentProducer.getSubType(resourceName));
         document.addField(SearchService.FIELD_REFERENCE, resourceName);
         document.addField(SearchService.FIELD_TITLE, contentProducer.getTitle(resourceName));
         document.addField(SearchService.FIELD_TOOL, contentProducer.getTool());
         document.addField(SearchService.FIELD_URL, contentProducer.getUrl(resourceName));
         document.addField(SearchService.FIELD_SITEID, contentProducer.getSiteId(resourceName));
 
         //Add the custom properties
         Map<String, Collection<String>> properties = extractCustomProperties(resourceName, contentProducer);
         for (Map.Entry<String, Collection<String>> entry : properties.entrySet()) {
             document.addField(PROPERTY_PREFIX + entry.getKey(), entry.getValue());
         }
         return document;
     }
 
     /**
      * Prepare a request toward SolrCell to parse a binary document.
      * <p>
      * The given document will be send in its binary form to apache tika to be analysed and stored in the index.
      * </p>
      *
      * @param resourceName    name of the document
      * @param contentProducer associated content producer providing a binary stream of data
      * @param document        {@link SolrInputDocument} used to prepare index fields
      * @return a solrCell request
      */
     private SolrRequest prepareSolrCellRequest(final String resourceName, final BinaryEntityContentProducer contentProducer,
                                                SolrInputDocument document) {
         //Send to tika
         ContentStreamUpdateRequest contentStreamUpdateRequest = new ContentStreamUpdateRequest(SOLRCELL_PATH);
         contentStreamUpdateRequest.setParam("fmap.content", SearchService.FIELD_CONTENTS);
         contentStreamUpdateRequest.setParam("uprefix", UPREFIX);
         ContentStreamBase contentStreamBase = new ContentStreamBase() {
             @Override
             public InputStream getStream() throws IOException {
                 return contentProducer.getContentStream(resourceName);
             }
         };
         contentStreamUpdateRequest.addContentStream(contentStreamBase);
         for (SolrInputField field : document) {
             contentStreamUpdateRequest.setParam("fmap.sakai_" + field.getName(), field.getName());
             for (Object o : field) {
                 //The "sakai_" part is due to SOLR-3386, this fix should be temporary
                 contentStreamUpdateRequest.setParam(LITERAL + "sakai_" + field.getName(), o.toString());
             }
         }
         return contentStreamUpdateRequest;
     }
 
     /**
      * Extract properties from the {@link EntityContentProducer}
      * <p>
      * The {@link EntityContentProducer#getCustomProperties(String)} method returns a map of different kind of elements.
      * To avoid casting and calls to {@code instanceof}, extractCustomProperties does all the work and returns a formated
      * map containing only {@link Collection<String>}.
      * </p>
      *
      * @param resourceName    affected resource
      * @param contentProducer producer providing properties for the given resource
      * @return a formated map of {@link Collection<String>}
      */
     private Map<String, Collection<String>> extractCustomProperties(String resourceName, EntityContentProducer contentProducer) {
         Map<String, ?> m = contentProducer.getCustomProperties(resourceName);
 
         if (m == null)
             return Collections.emptyMap();
 
         Map<String, Collection<String>> properties = new HashMap<String, Collection<String>>(m.size());
         for (Map.Entry<String, ?> propertyEntry : m.entrySet()) {
             String propertyName = toSolrFieldName(propertyEntry.getKey());
             Object propertyValue = propertyEntry.getValue();
             Collection<String> values;
 
             //Check for basic data type that could be provided by the EntityContentProducer
             //If the data type can't be defined, nothing is stored. The toString method could be called, but some values
             //could be not meant to be indexed.
             if (propertyValue instanceof String)
                 values = Collections.singleton((String) propertyValue);
             else if (propertyValue instanceof String[])
                 values = Arrays.asList((String[]) propertyValue);
             else if (propertyValue instanceof Collection)
                 values = (Collection<String>) propertyValue;
             else {
                 if (propertyValue != null)
                     logger.warn("Couldn't find what the value for '" + propertyName + "' was. It has been ignored. " + propertyName.getClass());
                 values = Collections.emptyList();
             }
 
             //If this property was already present there (this shouldn't happen, but if it does everything must be stored
             if (properties.containsKey(propertyName)) {
                 logger.warn("Two properties had a really similar name and were merged. This shouldn't happen! " + propertyName);
                 if (logger.isDebugEnabled())
                     logger.debug("Merged values '" + properties.get(propertyName) + "' with '" + values);
                 values = new ArrayList<String>(values);
                 values.addAll(properties.get(propertyName));
             }
 
             properties.put(propertyName, values);
         }
 
         return properties;
     }
 
     /**
      * Replace special characters, turn to lower case and avoid repetitive '_'
      *
      * @param propertyName String to filter
      * @return a filtered name more appropriate to use with solr
      */
     private String toSolrFieldName(String propertyName) {
         StringBuilder sb = new StringBuilder(propertyName.length());
         boolean lastUnderscore = false;
         for (Character c : propertyName.toLowerCase().toCharArray()) {
             if ((c < 'a' || c > 'z') && (c < '0' || c > '9'))
                 c = '_';
             if (!lastUnderscore || c != '_')
                 sb.append(c);
             lastUnderscore = (c == '_');
         }
         if (logger.isDebugEnabled())
             logger.debug("Transformed the '" + propertyName + "' property into: '" + sb + "'");
         return sb.toString();
     }
 
     public String format(Date creationDate) {
         return DateUtil.getThreadLocalDateFormat().format(creationDate);
     }
 
     public Queue<String> getIndexableSites() {
         Queue<String> refreshedSites = new LinkedList<String>();
         for (Site s : siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)) {
             if (isSiteIndexable(s)) {
                 refreshedSites.offer(s.getId());
             }
         }
         return refreshedSites;
     }
 
     public Queue<String> getResourceNames(String siteId) throws SolrServerException {
         if (logger.isDebugEnabled())
             logger.debug("Obtaining indexed elements for site: '" + siteId + "'");
         SolrQuery query = new SolrQuery()
                 .setQuery(SearchService.FIELD_SITEID + ":" + ClientUtils.escapeQueryChars(siteId))
                         //TODO: Use paging?
                 .setRows(Integer.MAX_VALUE)
                 .addField(SearchService.FIELD_REFERENCE);
         SolrDocumentList results = solrServer.query(query).getResults();
         Queue<String> resourceNames = new LinkedList<String>();
         for (SolrDocument document : results) {
             resourceNames.add((String) document.getFieldValue(SearchService.FIELD_REFERENCE));
         }
         return resourceNames;
     }
 
     public Queue<String> getSiteDocumentsReferences(String siteId) {
         //TODO: Replace by a lazy queuing system
         Queue<String> references = new LinkedList<String>();
 
         for (EntityContentProducer contentProducer : contentProducerFactory.getContentProducers()) {
             Iterators.addAll(references, contentProducer.getSiteContentIterator(siteId));
         }
 
         return references;
     }
 
     public boolean isDocumentOutdated(String documentId, Date currentDate) throws SolrServerException {
         if (logger.isDebugEnabled())
             logger.debug("Obtaining creation date for document '" + documentId + "'");
         SolrQuery query = new SolrQuery()
                 .setQuery(SearchService.FIELD_ID + ":" + ClientUtils.escapeQueryChars(documentId) + " AND " +
                         SearchService.DATE_STAMP + ":[" + format(currentDate) + " TO *]")
                 .setRows(0);
         return solrServer.query(query).getResults().getNumFound() == 0;
     }
 
     private boolean isSiteIndexable(Site site) {
         return !(siteService.isSpecialSite(site.getId()) ||
                 (searchIndexBuilder.isOnlyIndexSearchToolSites() && site.getToolForCommonId(SolrSearchIndexBuilder.SEARCH_TOOL_ID) == null) ||
                 (searchIndexBuilder.isExcludeUserSites() && siteService.isUserSite(site.getId())));
     }
 
     public int getPendingDocuments() {
         try {
             AdminStatRequest adminStatRequest = new AdminStatRequest();
             adminStatRequest.setParam("key", "updateHandler");
             NamedList<Object> result = solrServer.request(adminStatRequest);
             NamedList<Object> mbeans = (NamedList<Object>) result.get("solr-mbeans");
             NamedList<Object> updateHandler = (NamedList<Object>) mbeans.get("UPDATEHANDLER");
             NamedList<Object> updateHandler2 = (NamedList<Object>) updateHandler.get("updateHandler");
             NamedList<Object> stats = (NamedList<Object>) updateHandler2.get("stats");
             return ((Long) stats.get("docsPending")).intValue();
         } catch (SolrServerException e) {
             logger.warn("Couldn't obtain the number of pending documents", e);
             return 0;
         } catch (IOException e) {
             logger.error("Can't contact the search server", e);
             return 0;
         }
     }
 
     public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
         this.searchIndexBuilder = searchIndexBuilder;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     public void setContentProducerFactory(ContentProducerFactory contentProducerFactory) {
         this.contentProducerFactory = contentProducerFactory;
     }
 
     public void setSolrServer(SolrServer solrServer) {
         this.solrServer = solrServer;
     }
 
    public void setTikaEnabled(boolean tikaEnabled) {
         this.tikaEnabled = tikaEnabled;
     }
 }
