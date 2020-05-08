 package org.synyx.opencms.solr;
 
import java.util.Calendar;
 import org.opencms.main.CmsException;
 import org.synyx.opencms.solr.indexing.SolrIndexWriter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Fieldable;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.SortField;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.opencms.file.CmsObject;
 import org.opencms.file.CmsResource;
 import org.opencms.file.CmsResourceFilter;
 import org.opencms.main.OpenCms;
 import org.opencms.search.CmsIndexException;
 import org.opencms.search.CmsSearchException;
 import org.opencms.search.CmsSearchIndex;
 import org.opencms.search.CmsSearchParameters;
 import org.opencms.search.CmsSearchResultList;
 import org.opencms.search.I_CmsIndexWriter;
 import org.opencms.search.Messages;
 import org.opencms.search.fields.CmsSearchField;
 import org.opencms.search.fields.CmsSearchFieldConfiguration;
 import org.opencms.search.CmsTimeWindowSearchFieldSupport;
 import org.opencms.search.RangeQuery;
 import org.opencms.db.CmsUserSettings;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import org.opencms.search.MinMaxRangeQuery;
 
 /**
  * A search index for OpenCms that uses SolrJ to query documents.
  * @author Florian Hopf, Synyx GmbH & Co. KG, hopf@synyx.de
  * @author Oliver Messner, Synyx GmbH & Co. KG, messner@synyx.de
  */
 public abstract class SolrSearchIndex extends CmsSearchIndex implements CmsTimeWindowSearchFieldSupport {
 
     public final static String FIELD_ID = "id";
     private Log LOG = LogFactory.getLog(SolrSearchIndex.class);
     private SolrServer solrServer;
     private boolean useSolrPaging = true;
    private static final long DEFAULT_DATE_EXPIRED;
     private static final long DEFAULT_DATE_RELEASED = 0L;
 
    static {
        // 500 years should be enough
        // too large numbers will make DateTools fail when parsing the date
        Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.YEAR, 500);
        DEFAULT_DATE_EXPIRED = cal.getTimeInMillis();
    }

     @Override
     public void initialize() throws CmsSearchException {
         super.initialize();
         IndexConfiguration config = ConfigurationFactory.initIndexConfiguration(getName());
         this.solrServer = config.initializeServer();
         this.useSolrPaging = config.isSolrPaging();
     }
 
     @Override
     public Fieldable getDateReleaseSearchField(CmsResource resource) {
         long dateReleased = DEFAULT_DATE_RELEASED;
         if (resource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT) {
             dateReleased = resource.getDateReleased();
         }
         Fieldable dateReleasedField = new Field(FIELD_RELEASE, DateTools.dateToString(new Date(dateReleased),
                 DateTools.Resolution.MILLISECOND), Field.Store.YES, Field.Index.NOT_ANALYZED);
         dateReleasedField.setBoost(0);
         return dateReleasedField;
     }
 
     @Override
     public Fieldable getDateExpiredSearchField(CmsResource resource) {
         long dateExpired = DEFAULT_DATE_EXPIRED;
         if (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT) {
             dateExpired = resource.getDateExpired();
         }
 
         Fieldable dateExpiredField = new Field(FIELD_EXPIRED, DateTools.dateToString(new Date(dateExpired),
                 DateTools.Resolution.MILLISECOND), Field.Store.YES, Field.Index.NOT_ANALYZED);
         dateExpiredField.setBoost(0);
         return dateExpiredField;
     }
 
     @Override
     public RangeQuery getDateReleaseRangeQuery(long timeMillis) {
         DateTime dateTime = new DateTime(timeMillis, DateTimeZone.UTC);
         return new RangeQuery("[* TO " + dateTime + "]", dateTime.getMillis());
     }
 
     @Override
     public RangeQuery getDateExpiredRangeQuery(long timeMillis) {
         DateTime dateTime = new DateTime(timeMillis, DateTimeZone.UTC);
         return new RangeQuery("[" + dateTime + " TO *]", dateTime.getMillis());
     }
 
     @Override
     public MinMaxRangeQuery getCreatedDateRangeQuery(long minTimeMillis, long maxTimeMillis) {
         if (minTimeMillis == Long.MIN_VALUE && maxTimeMillis == Long.MAX_VALUE) {
             return null;
         }
 
         DateTime dateTimeMin = new DateTime(minTimeMillis, DateTimeZone.UTC);
         DateTime dateTimeMax = new DateTime(maxTimeMillis, DateTimeZone.UTC);
        StringBuilder sb = new StringBuilder("[").append((minTimeMillis > Long.MIN_VALUE ? dateTimeMin : "*")).append(" TO ").append((maxTimeMillis < Long.MAX_VALUE ? dateTimeMax : "*")).append("]");
         return new MinMaxRangeQuery(sb.toString(), dateTimeMin.getMillis(), dateTimeMax.getMillis());
     }
 
     @Override
     public MinMaxRangeQuery getLastModifiedDateRangeQuery(long minTimeMillis, long maxTimeMillis) {
         if (minTimeMillis == Long.MIN_VALUE && maxTimeMillis == Long.MAX_VALUE) {
             return null;
         }
 
         DateTime dateTimeMin = new DateTime(minTimeMillis, DateTimeZone.UTC);
         DateTime dateTimeMax = new DateTime(maxTimeMillis, DateTimeZone.UTC);
        StringBuilder sb = new StringBuilder("[").append((minTimeMillis > Long.MIN_VALUE ? dateTimeMin : "*")).append(" TO ").append((maxTimeMillis < Long.MAX_VALUE ? dateTimeMax : "*")).append("]");
         return new MinMaxRangeQuery(sb.toString(), minTimeMillis, maxTimeMillis);
     }
 
     /**
      * Returns the Lucene document with the given root path from the index.<p>
      *
      * @param rootPath the root path of the document to get
      *
      * @return the Lucene document with the given root path from the index
      */
     @Override
     public Document getDocument(String rootPath) {
 
         try {
             SolrQuery query = new SolrQuery("id:".concat(rootPath)); // TODO: this does not work as expected for dismax query handlers
             QueryResponse queryResponse = solrServer.query(query);
             SolrDocumentList docs = queryResponse.getResults();
             if (!docs.isEmpty()) {
                 SolrDocument doc = docs.get(0);
                 return new DocumentConverter().asDocument(doc);
             }
         } catch (SolrServerException e) {
             throw new SolrSearchIndexException(
                     "Caught a SolrServerException while trying to get a document from the index: " + e.getMessage(), e);
         }
 
         return null;
     }
 
     /**
      * Returns a new index writer for this index.<p>
      *
      * @param create if <code>true</code> a whole new index is created, if <code>false</code> an existing index is updated
      *
      * @return a new instance of IndexWriter
      * @throws CmsIndexException if the index can not be opened
      */
     @Override
     public I_CmsIndexWriter getIndexWriter(boolean create) throws CmsIndexException {
         // TODO it's very likely that we have to include the index name in the url
         return new SolrIndexWriter(solrServer);
     }
 
     /**
      * Returns the Lucene index searcher used for this search index.<p>
      *
      * @return the Lucene index searcher used for this search index
      */
     @Override
     public IndexSearcher getSearcher() {
         throw new UnsupportedOperationException("Invalid call to getSearcher for SolrSearchIndex");
     }
 
     /**
      * This is a template method. Its implementations are responsible to prepare a solr query object by setting the
      * specified search related parameters on it. Implementations of this method depend on the Solr request handler to
      * be used.
      * @param solrQuery the solr query object.
      * @param params the search related parameters to be set on the solr query object.
      */
     public abstract void addQueryToSolrQuery(SolrQuery solrQuery, CmsSearchParameters params);
 
     /**
      * Performs a search on the index within the given fields.<p>
      *
      * The result is returned as List with entries of type I_CmsSearchResult.<p>
      * @param cms the current user's Cms object
      * @param params the parameters to use for the search
      * @return the List of results found or an empty list
      * @throws CmsSearchException if something goes wrong
      */
     @Override
     public final synchronized CmsSearchResultList search(CmsObject cms, CmsSearchParameters params)
             throws CmsSearchException {
 
         // TODO check if access to Solr needs to be synchronized
 
         long timeTotal = -System.currentTimeMillis();
         long timeLucene;
         long timeResultProcessing;
 
         if (LOG.isDebugEnabled()) {
             LOG.debug(Messages.get().getBundle().key(Messages.LOG_SEARCH_PARAMS_2, params, getName()));
         }
 
         // storage for the results found
         SolrSearchResultList searchResults = new SolrSearchResultList();
 
         int previousPriority = Thread.currentThread().getPriority();
 
         SolrDocumentList hits = null;
 
         try {
             // copy the user OpenCms context
             CmsObject searchCms = OpenCms.initCmsObject(cms);
 
             if (getPriority() > 0) {
                 // change thread priority in order to reduce search impact on overall system performance
                 Thread.currentThread().setPriority(getPriority());
             }
 
             // change the project
             searchCms.getRequestContext().setCurrentProject(searchCms.readProject(getProject()));
 
             timeLucene = -System.currentTimeMillis();
 
             SolrQuery solrQuery = new SolrQuery();
             addSearchRootFilterQueryToSolrQuery(solrQuery, params, searchCms);
             addCategoryFilterQueryToSolrQuery(solrQuery, params);
             addResourceTypesFilterQueryToSolrQuery(solrQuery, params);
             addQueryToSolrQuery(solrQuery, params);
             addDateReleasedRangeFilterQuery(solrQuery, searchCms);
             addDateExpiredRangeFilterQuery(solrQuery, searchCms);
             addDateCreatedFilterQuery(solrQuery, params);
             addDateLastModifiedFilterQuery(solrQuery, params);
 
             if (params.getSort() != null) {
                 for (SortField sortField : params.getSort().getSort()) {
                     if (sortField.getReverse()) {
                         solrQuery.addSortField(sortField.getField(), SolrQuery.ORDER.desc);
                     } else {
                         solrQuery.addSortField(sortField.getField(), SolrQuery.ORDER.asc);
                     }
                 }
             }
 
             if (useSolrPaging) {
                 solrQuery.setRows(params.getMatchesPerPage());
                 solrQuery.setStart(params.getMatchesPerPage() * (params.getSearchPage() - 1));
             }
 
             if (params instanceof SolrSearchParameters) {
                 SolrSearchParameters solrParams = (SolrSearchParameters) params;
                 solrQuery.setQueryType(solrParams.getQueryType());
 
                 // add any filter queries that are configured
                 for (SolrSearchParameters.FilterQuery filterQuery : solrParams.getFilterQueries()) {
                     solrQuery.addFilterQuery(buildFilterQuery(filterQuery.getFieldname(), filterQuery.getQuery(), filterQuery.getOccur()));
                 }
             }
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug(Messages.get().getBundle().key(Messages.LOG_BASE_QUERY_1, solrQuery));
             }
 
 
             // perform the search operation
             QueryResponse response = null;
             try {
                 response = solrServer.query(solrQuery);
             } catch (SolrServerException e) {
                 LOG.error("Caught a SolrServerException while trying to perform an index search.", e);
                 throw new SolrSearchIndexException(e.getMessage(), e);
             }
             hits = response.getResults();
 
             timeLucene += System.currentTimeMillis();
             timeResultProcessing = -System.currentTimeMillis();
 
             if (hits != null) {
                 //int hitCount = hits.size() > hits.scoreDocs.length ? hits.scoreDocs.length : hits.totalHits;
                 // TODO prevent this cast?
                 int hitCount = (int) hits.getNumFound();
                 int page = params.getSearchPage();
                 int start = -1, end = -1;
                 if ((params.getMatchesPerPage() > 0) && (page > 0) && (hitCount > 0)) {
                     // calculate the final size of the search result
                     start = params.getMatchesPerPage() * (page - 1);
                     end = start + params.getMatchesPerPage();
                     // ensure that both i and n are inside the range of foundDocuments.size()
                     start = (start > hitCount) ? hitCount : start;
                     end = (end > hitCount) ? hitCount : end;
                 } else {
                     // return all found documents in the search result
                     start = 0;
                     end = hitCount;
                 }
 
                 int visibleHitCount = hitCount;
                 for (int i = start, cnt = 0; (i < hitCount) && (i < end); i++) {
                     try {
                         SolrDocument solrDocument = null;
                         if (useSolrPaging) {
                             solrDocument = hits.get(cnt);
                         } else {
                             solrDocument = hits.get(i);
                         }
                         Date dateCreated = (Date) solrDocument.get(CmsSearchField.FIELD_DATE_CREATED);
                         Date dateLastModified = (Date) solrDocument.get(CmsSearchField.FIELD_DATE_LASTMODIFIED);
                         String path = (String) solrDocument.get(CmsSearchField.FIELD_PATH);
                         String type = (String) solrDocument.get(CmsSearchField.FIELD_TYPE);
 
                         if ((isInTimeRange(dateCreated, dateLastModified, params))
                                 && (hasReadPermission(searchCms, type, path))) {
                             searchResults.add(createSearchResult(response, solrDocument, hits));
                         } else {
                             visibleHitCount--;
                             cnt++;
                             LOG.warn("Indexed document found could not be added to the search result: " + solrDocument);
                         }
                         cnt++;
                     } catch (Exception e) {
                         // should not happen, but if it does we want to go on with the next result nevertheless
                         if (LOG.isWarnEnabled()) {
                             LOG.warn(Messages.get().getBundle().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
                         }
                     }
                 }
 
                 searchResults.setHitCount(visibleHitCount); // save the total count of search results
                 searchResults.setFacetFields(response.getFacetFields());
             } else {
                 searchResults.setHitCount(0);
             }
 
             timeResultProcessing += System.currentTimeMillis();
 
 
         } catch (RuntimeException e) {
             throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
 
 
         } catch (Exception e) {
             throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
 
 
         } finally {
 
             // re-set thread to previous priority
             Thread.currentThread().setPriority(previousPriority);
 
 
         }
 
         if (LOG.isDebugEnabled()) {
             timeTotal += System.currentTimeMillis();
             Object[] logParams = new Object[]{
                 new Integer(hits == null ? 0 : hits.size()),
                 new Long(timeTotal),
                 new Long(timeLucene),
                 new Long(timeResultProcessing)};
             LOG.debug(Messages.get().getBundle().key(Messages.LOG_STAT_RESULTS_TIME_4, logParams));
 
 
         }
 
         return searchResults;
 
 
     }
 
     private void addSearchRootFilterQueryToSolrQuery(SolrQuery solrQuery, CmsSearchParameters params, CmsObject searchCms) {
 
         if ((params.getRoots() != null) && (params.getRoots().size() > 0)) {
             // add the all configured search roots with will request context
             for (int i = 0; i < params.getRoots().size(); i++) {
                 String searchRoot = searchCms.getRequestContext().addSiteRoot(params.getRoots().get(i));
                 extendPathFilter(solrQuery, searchRoot);
             }
         } else {
             // just use the current site root as the search root
             extendPathFilter(solrQuery, searchCms.getRequestContext().getSiteRoot());
         }
     }
 
     private void addCategoryFilterQueryToSolrQuery(SolrQuery solrQuery, CmsSearchParameters params) {
 
         if ((params.getCategories() != null) && (params.getCategories().size() > 0)) {
             // MUST
             for (String category : params.getCategories()) {
                 solrQuery.addFilterQuery(buildFilterQuery(CmsSearchField.FIELD_CATEGORY, category, Occur.MUST));
             }
         }
     }
 
     private void addResourceTypesFilterQueryToSolrQuery(SolrQuery solrQuery, CmsSearchParameters params) {
 
         if ((params.getResourceTypes() != null) && (params.getResourceTypes().size() > 0)) {
             // MUST
             for (String resourceType : params.getResourceTypes()) {
                 solrQuery.addFilterQuery(buildFilterQuery(CmsSearchField.FIELD_TYPE, resourceType, Occur.MUST));
             }
         }
     }
 
     private void addDateReleasedRangeFilterQuery(SolrQuery solrQuery, CmsObject cms) {
         String dateReleasedRangeFilterQuery = null;
 
         CmsUserSettings cmsUserSettings = new CmsUserSettings(cms);
         long timeWarp = cmsUserSettings.getTimeWarp();
         if (timeWarp == -1) {
             dateReleasedRangeFilterQuery =
                     getDateReleaseRangeQuery(new DateTime(DateTimeZone.UTC).getMillis()).getRangeQueryString();
         } else {
             dateReleasedRangeFilterQuery = getDateReleaseRangeQuery(timeWarp).getRangeQueryString();
         }
 
         solrQuery.addFilterQuery(buildFilterQuery(FIELD_RELEASE, dateReleasedRangeFilterQuery, Occur.MUST));
     }
 
     private void addDateExpiredRangeFilterQuery(SolrQuery solrQuery, CmsObject cms) {
         String dateExpiredRangeFilterQuery = null;
 
         CmsUserSettings cmsUserSettings = new CmsUserSettings(cms);
         long timeWarp = cmsUserSettings.getTimeWarp();
         if (timeWarp == -1) {
             dateExpiredRangeFilterQuery =
                     getDateExpiredRangeQuery(new DateTime(DateTimeZone.UTC).getMillis()).getRangeQueryString();
         } else {
             dateExpiredRangeFilterQuery = getDateExpiredRangeQuery(timeWarp).getRangeQueryString();
         }
 
         solrQuery.addFilterQuery(buildFilterQuery(FIELD_EXPIRED, dateExpiredRangeFilterQuery, Occur.MUST));
     }
 
     private void addDateCreatedFilterQuery(SolrQuery solrQuery, CmsSearchParameters params) {
         MinMaxRangeQuery createdDateRangeQuery =
                 getCreatedDateRangeQuery(params.getMinDateCreated(), params.getMaxDateCreated());
         if (createdDateRangeQuery != null) {
             solrQuery.addFilterQuery(buildFilterQuery(CmsSearchField.FIELD_DATE_CREATED,
                     createdDateRangeQuery.getRangeQueryString(), Occur.MUST));
         }
     }
 
     private void addDateLastModifiedFilterQuery(SolrQuery solrQuery, CmsSearchParameters params) {
         MinMaxRangeQuery lastModifiedDateRangeQuery =
                 getLastModifiedDateRangeQuery(params.getMinDateLastModified(), params.getMaxDateLastModified());
         if (lastModifiedDateRangeQuery != null) {
             solrQuery.addFilterQuery(buildFilterQuery(CmsSearchField.FIELD_DATE_LASTMODIFIED,
                     lastModifiedDateRangeQuery.getRangeQueryString(), Occur.MUST));
         }
     }
 
     @Override
     protected boolean hasReadPermission(CmsObject cms, String type, String path) {
         if (!isCheckingPermissions()) {
             return true;
         }
 
         if ((type == null) || (path == null)) {
             // permission check needs only to be performed for VFS documents that contain both fields
             return true;
         }
 
         if (!CmsSearchFieldConfiguration.VFS_DOCUMENT_KEY_PREFIX.equals(type)
                 && !OpenCms.getResourceManager().hasResourceType(type)) {
             // this is not a known VFS resource type (also not the generic "VFS" type of OpenCms before 7.0)
             return true;
         }
 
         String contextPath = cms.getRequestContext().removeSiteRoot(path);
         CmsUserSettings cmsUserSettings = new CmsUserSettings(cms);
         long timeWarp = cmsUserSettings.getTimeWarp();
         if (timeWarp != -1) {
             return hasReadPermissionsInTimeWarpMode(cms, contextPath, timeWarp);
         }
         return cms.existsResource(contextPath, CmsResourceFilter.DEFAULT);
     }
 
     private boolean hasReadPermissionsInTimeWarpMode(CmsObject cms, String contextPath, long timeWarp) {
         if (cms.existsResource(contextPath, CmsResourceFilter.IGNORE_EXPIRATION)) {
             long dateRelease = getDateReleaseRangeQuery(timeWarp).getTimeMillis();
             long dateExpired = getDateExpiredRangeQuery(timeWarp).getTimeMillis();
             CmsResource resource = null;
             try {
                 resource = cms.readResource(contextPath, CmsResourceFilter.IGNORE_EXPIRATION);
                 if (resource.getDateReleased() > dateRelease || resource.getDateExpired() < dateExpired) {
                     return false;
                 }
             } catch (CmsException e) {
                 LOG.error(e.getMessage(), e);
                 return false;
             }
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     protected boolean isInTimeRange(Date dateCreated, Date dateLastModified, CmsSearchParameters params) {
         return true;
     }
 
     private String buildFilterQuery(String fieldName, String fieldValue, Occur occur) {
 
         if (occur == Occur.MUST_NOT) {
             return String.format(" -%s:%s", fieldName, fieldValue);
         } else if (occur == Occur.MUST) {
             return String.format(" +%s:%s", fieldName, fieldValue);
         } else {
             return String.format(" %s:%s", fieldName, fieldValue); // SHOULD or unspecified
         }
     }
 
     private void extendPathFilter(SolrQuery solrQuery, String searchRoot) {
 
         if (searchRoot != null) {
             if (!CmsResource.isFolder(searchRoot)) {
                 searchRoot += "/";
             }
             solrQuery.addFilterQuery(buildFilterQuery(CmsSearchField.FIELD_PARENT_FOLDERS, searchRoot, Occur.SHOULD));
         }
     }
 
     private SolrSearchResult createSearchResult(QueryResponse response, SolrDocument doc, SolrDocumentList hits) {
         // do not use the resource to obtain the raw content, read it from the lucene document!
         String excerpt = "";
         if (response.getHighlighting() != null) {
             excerpt = getHighlightFragment(response, doc);
         }
 
         float score = (Float) doc.getFieldValue("score");
         float maxScore = hits.getMaxScore() == null ? 1f : hits.getMaxScore();
         return new SolrSearchResult(Math.round((score / maxScore) * 100f), doc, excerpt);
     }
 
     private String getHighlightFragment(QueryResponse response, SolrDocument doc) {
         String highlightFragment = "";
         String documentId = (String) doc.get(FIELD_ID);
 
         Map<String, List<String>> map = response.getHighlighting().get(documentId);
         if (map != null) {
             List<String> highlightSnippets = null;
             for (String key : map.keySet()) {
                 highlightSnippets = map.get(key);
                 if (!highlightSnippets.isEmpty()) {
                     break;
                 }
             }
 
             if (highlightSnippets != null) {
                 StringBuilder highlightBuilder = new StringBuilder();
                 for (String snippet : highlightSnippets) {
                     highlightBuilder.append(snippet);
                 }
                 highlightFragment = highlightBuilder.toString();
             }
         }
 
         return highlightFragment; // TODO: set non-empty default excerpt?
     }
 
     @Override
     protected String createIndexBackup() {
         return null;
     }
 
     @Override
     protected synchronized void indexSearcherClose() {
         // NOOP
     }
 
     @Override
     protected synchronized void indexSearcherOpen(String path) {
         // NOOP
     }
 }
