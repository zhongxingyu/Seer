 package org.aptrust.client.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.FacetField;
 import org.apache.solr.client.solrj.response.FacetField.Count;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.aptrust.client.api.AptrustClient;
 import org.aptrust.client.api.AptrustObjectDetail;
 import org.aptrust.client.api.ContentSummary;
 import org.aptrust.client.api.HealthCheckInfo;
 import org.aptrust.client.api.IngestProcessSummary;
 import org.aptrust.client.api.IngestStatus;
 import org.aptrust.client.api.InstitutionInfo;
 import org.aptrust.client.api.PackageSummary;
 import org.aptrust.client.api.PackageSummaryQueryResponse;
 import org.aptrust.client.api.SearchConstraint;
 import org.aptrust.client.api.SearchParams;
 import org.aptrust.client.api.Summary;
 import org.aptrust.common.exception.AptrustException;
 import org.aptrust.common.solr.AptrustSolrDocument;
 import org.aptrust.common.solr.ContentSolrDocument;
 import org.duracloud.client.ContentStore;
 import org.duracloud.client.ContentStoreImpl;
 import org.duracloud.common.model.Credential;
 import org.duracloud.common.web.RestHttpHelper;
 import org.duracloud.common.web.RestHttpHelper.HttpResponse;
 import org.duracloud.domain.Content;
 import org.duracloud.error.ContentStoreException;
 import org.duracloud.error.NotFoundException;
 import org.duracloud.storage.domain.StorageProviderType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 /**
  * An AptrustClient implementation that gets all available information from Solr
  * queries.
  */
 public class AptrustClientImpl implements AptrustClient {
     private Logger log = LoggerFactory.getLogger(AptrustClientImpl.class);
     
     public static DateFormat SOLR_DATE_FACET_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
     public static DateFormat SOLR_DATE_FACET_FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
     static {
         SOLR_DATE_FACET_FORMAT_1.setTimeZone(TimeZone.getTimeZone("GMT"));
         SOLR_DATE_FACET_FORMAT_2.setTimeZone(TimeZone.getTimeZone("GMT"));
     }
     
     public static Date parseSolrDateFacet(String dateStr) throws ParseException {
         try {
             return SOLR_DATE_FACET_FORMAT_1.parse(dateStr);
         } catch (ParseException ex) {
             return SOLR_DATE_FACET_FORMAT_2.parse(dateStr);
         }
     }
 
     private final Logger logger =
         LoggerFactory.getLogger(AptrustClientImpl.class);
 
     protected ClientConfig config;
 
     protected SolrServer solr;
 
     private List<InstitutionInfo> cachedInstitutions;
 
     public AptrustClientImpl(ClientConfig config) {
         if (config == null) {
             throw new IllegalArgumentException("client config must be non-null");
         }
 
         this.config = config;
         solr = new HttpSolrServer(config.getSolrUrl());
     }
 
     /**
      * Gets a summary of the status of content from the institution specified by
      * the institutionId parameter. The current implementation makes use of the
      * Solr index as well as the "duraboss" REST API on the DuraSpace instance
      * referenced in the configuration.
      */
     public Summary getSummary(String institutionId) throws AptrustException {
         validateInstitutionId(institutionId);
 
         Summary s = new Summary();
         s.setInstitutionId(institutionId);
 
         SolrQueryClause packageRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "package");
         SolrQueryClause objectRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "object");
         SolrQueryClause currentInstitution =
             new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID,
                                 institutionId);
         SolrQueryClause dpnBound =
             new SolrQueryClause(AptrustSolrDocument.DPN_BOUND, "true");
         SolrQueryClause isPublic =
             new SolrQueryClause(AptrustSolrDocument.ACCESS_CONTROL_POLICY,
                                 "world");
         SolrQueryClause isPrivate =
             new SolrQueryClause(AptrustSolrDocument.ACCESS_CONTROL_POLICY,
                                 "private");
         SolrQueryClause isInstitutionOnly =
             new SolrQueryClause(AptrustSolrDocument.ACCESS_CONTROL_POLICY,
                                 "institution");
         SolrQueryClause failedHealthCheck =
             new SolrQueryClause(AptrustSolrDocument.FAILED_HEALTH_CHECK, "true");
 
         try {
             s.setPackageCount(getResponseCount(packageRecords.and(currentInstitution)
                                                              .getQueryString()));
             s.setObjectCount(getResponseCount(objectRecords.and(currentInstitution)
                                                            .getQueryString()));
             s.setDpnBoundPackageCount(getResponseCount(packageRecords.and(dpnBound)
                                                                      .and(currentInstitution)
                                                                      .getQueryString()));
             s.setPublicPackageCount(getResponseCount(packageRecords.and(isPublic)
                                                                    .and(currentInstitution)
                                                                    .getQueryString()));
             s.setInstitutionPackageCount(getResponseCount(packageRecords.and(isInstitutionOnly)
                                                                         .and(currentInstitution)
                                                                         .getQueryString()));
             s.setPrivatePackageCount(getResponseCount(packageRecords.and(isPrivate)
                                                                     .and(currentInstitution)
                                                                     .getQueryString()));
             s.setFailedPackageCount(getResponseCount(packageRecords.and(failedHealthCheck)
                                                                    .and(currentInstitution)
                                                                    .getQueryString()));
         } catch (SolrServerException ex) {
             throw new AptrustException("Error generating summary from Solr!",
                                        ex);
         }
 
         // Get the total usage. DuraCloud doesn't expose a quick query for
         // total space used. The current implementation fetches the storage
         // report (which may not be up-to-the-minute) and reports the usage
         // from that report. If we need real-time storage usage, we should
         // consider maintaining a value in SOLR that is updated in response to
         // file updates/removals.
         RestHttpHelper rest =
             new RestHttpHelper(new Credential(config.getDuracloudUsername(),
                                               config.getDuracloudPassword()));
         try {
             String url = config.getDuracloudUrl() + "duraboss/report/storage";
             HttpResponse response = rest.get(url);
             if (response.getStatusCode() / 100 != 2) {
                 throw new RuntimeException("HTTP Status code "
                     + response.getStatusCode() + " returned from GET of " + url);
             }
             DocumentBuilder parser =
                 DocumentBuilderFactory.newInstance().newDocumentBuilder();
             XPath xpath = XPathFactory.newInstance().newXPath();
             Document report = parser.parse(response.getResponseStream());
             try {
                 long totalBytes =
                     Long.parseLong((String) xpath.evaluate("storageReport/storageMetrics/storageProviderMetrics/storageProvider[@id='"
                                                                + config.getDuraCloudProviderId()
                                                                + "']/spaceMetrics/space[@name='"
                                                                + institutionId
                                                                + "']/totalSize",
                                                            report,
                                                            XPathConstants.STRING));
                 s.setBytesUsed(totalBytes);
             } catch (NumberFormatException ex) {
                 // no bytes stored
                 s.setBytesUsed((long) 0);
             }
         } catch (Exception ex) {
             throw new AptrustException("Error extracting storage usage from the  DuraCloud storage report!",
                                        ex);
         }
         return s;
     }
 
     protected void validateInstitutionId(String institutionId)
         throws AptrustException {
         //validate the institution id
         getInstitutionInfo(institutionId);
     }
 
     /**
      * Gets all institutions registered with AP Trust.  For an institution to
      * be recognized there must be two spaces one with the id "x" and another
      * with the id "xstaging".  The value of "x" is the institution id.  Within
      * the production space there must be a file "institution-info.txt" that 
      * contains a single line of text with the institution's full name.
      */
     public List<InstitutionInfo> getInstitutions() throws AptrustException {
         ContentStore cs = new ContentStoreImpl(config.getDuracloudUrl() + "durastore", 
                 StorageProviderType.valueOf(config.getDuraCloudProviderName()),
                 config.getDuraCloudProviderId(), 
                 new RestHttpHelper(
                         new Credential(config.getDuracloudUsername(), 
                                        config.getDuracloudPassword())));
 
         List<InstitutionInfo> institutions = new ArrayList<InstitutionInfo>();
         try {
             List<String> spaces = cs.getSpaces();
             for (String spaceId : spaces) {
                 if (spaces.contains(spaceId + "staging")) {
                     try {
                         Content c = cs.getContent(spaceId, "institution-info.txt");
                         BufferedReader r = new BufferedReader(new InputStreamReader(c.getStream()));
                         try {
                             institutions.add(new InstitutionInfo(spaceId, r.readLine()));
                         } catch (IOException ex) {
                             logger.error("Error reading first line of " + c.getId() + " from space \"" + spaceId + "\".");
                             throw new AptrustException(ex);
                         } finally {
                             try {
                                 r.close();
                             } catch (IOException ex) {
                                 throw new AptrustException(ex);
                             }
                         }
                     } catch (NotFoundException ex) {
                         logger.warn("\"" + spaceId + "\" and \"" + spaceId + "staging\" look like institutional staging and productions spaces, but \"" + spaceId + "\" does not contain institution-info.txt.");
                     }
                 }
             }
         } catch (ContentStoreException ex) {
             throw new AptrustException(ex);
         }
         cachedInstitutions = institutions;
         return institutions;
     }
 
     public InstitutionInfo getInstitutionInfo(String institutionId) throws AptrustException {
         if (cachedInstitutions == null || !institutionId.contains(institutionId)) {
             cachedInstitutions = getInstitutions();
         }
         for (InstitutionInfo i : cachedInstitutions) {
             if (i.getId().equals(institutionId)) {
                 return i;
             }
         }
         
         throw new AptrustException("An institution with id ("+ institutionId + ") could not be found.");
     }
 
     /**
      * Queries Solr for ingest processes from a given institution that match the
      * provided criteria. <br />
      * 
      * @param institutionId
      *            (required) specifies the institution to which all of the
      *            returned IngestProcessSummary objects pertain
      * @param startDate
      *            the date after which all the results must have begun
      * @param name
      *            the user-assigned title of all returned operations
      * @param status
      *            the status all returned operations must have
      * @deprecated Use the method with start and rows
      */
     public List<IngestProcessSummary> findIngestProcesses(String institutionId,
                                                           Date startDate,
                                                           String name,
                                                           IngestStatus status)
         throws AptrustException {
         return findIngestProcesses(institutionId, startDate, name, status, 0, 25);
     }
 
     public List<IngestProcessSummary> findIngestProcesses(String institutionId, Date startDate, String name, IngestStatus status, int start, int rows) throws AptrustException {
         validateInstitutionId(institutionId);
         
         //query solr
         SolrQueryClause ingestRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "ingest");
         SolrQueryClause currentInstitution =
             new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID,
                                 institutionId);
 
         SolrQueryClause query = ingestRecords.and(currentInstitution);
         if (status != null) {
             query =
                 query.and(new SolrQueryClause(AptrustSolrDocument.OPERATION_STATUS,
                                               status.toString()));
         }
         if (name != null) {
             query =
                 query.and(new SolrQueryClause(AptrustSolrDocument.TITLE, name));
         }
         if (startDate != null) {
             query =
                 query.and(SolrQueryClause.dateRange(AptrustSolrDocument.OPERATION_START_DATE,
                                                     startDate,
                                                     null));
         }
         List<IngestProcessSummary> results =
             new ArrayList<IngestProcessSummary>();
 
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
         params.set("sort", AptrustSolrDocument.OPERATION_START_DATE + " desc");
         params.set("start", String.valueOf(start));
         logger.debug(query.toString());
         try {
             QueryResponse response = solr.query(params);
             SolrDocumentList page = response.getResults();
             for (long i = page.getStart(); (i < page.getNumFound()) && (i < rows); i++) {
                 int pageOffset = (int) (i - page.getStart());
                 SolrDocument doc = page.get(pageOffset);
                 IngestProcessSummary s = new IngestProcessSummary();
                 AptrustSolrDocument.populateFromSolrDocument(s, doc);
                 results.add(s);
                 if (pageOffset + 1 >= page.size()) {
                     // fetch next page of results
                     params.set("start", String.valueOf(i + 1));
                     page = solr.query(params).getResults();
                 }
             }
         } catch (SolrServerException ex) {
             throw new AptrustException(ex);
         }
         return results;
     }
 
 
     @Override
     public PackageSummaryQueryResponse findPackageSummaries(String institutionId, SearchParams searchParams, String ... facetFields) throws AptrustException {
 
         String institutionName =
             getInstitutionInfo(institutionId).getFullName();
         SolrQueryClause packageRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "package");
         SolrQueryClause currentInstitution =
             new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID,
                                 institutionId);
 
         SolrQueryClause query = packageRecords.and(currentInstitution);
         if (searchParams.getQuery() != null
             && !searchParams.getQuery().equals("")) {
             query =
                 query.and(SolrQueryClause.parseUserQuery(searchParams.getQuery()));
         }
 
         // add date range to query
         if (searchParams.getStartDate() != null || searchParams.getEndDate() != null) {
             query = query.and(SolrQueryClause.dateRange(AptrustSolrDocument.INGEST_DATE, searchParams.getStartDate(), searchParams.getEndDate()));
         }
 
         // add constraints to query
         for(SearchConstraint sc : searchParams.getConstraints()) {
             query = query.and(new SolrQueryClause(sc.getName(), sc.getValue()));
         }
 
         List<PackageSummary> packages = new ArrayList<PackageSummary>();
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
         params.set("start", String.valueOf(searchParams.getStartIndex()));
         params.set("rows", String.valueOf(searchParams.getPageSize()));
         logger.debug("findPackageSummaries: " + query.toString());
         try {
             SolrDocumentList page = solr.query(params).getResults();
             for (int i = 0; (i < page.size()); i++) {
                 SolrDocument doc = page.get(i);
                 PackageSummary s = new PackageSummary();
                 s.setInstitutionName(institutionName);
                 // populate the easily-mapped field
                 AptrustSolrDocument.populateFromSolrDocument(s, doc);
 
                 // there's a couple complex fields that must be populated
                 // from data not simply stored in the Solr record
                 s.setInstitutionName(institutionName);
 
                 // populate the health check
                 s.setHealthCheckInfo(computePackageHealthCheck(institutionId, s.getId()));
 
                 packages.add(s);
             }
 
             params.set("facet", "true");
             if (facetFields != null) {
                 params.set("facet.field", facetFields);
             }
             params.set("rows", "0");
             QueryResponse response = solr.query(params);
             List<FacetField> facets = response.getFacetFields();
             for(SearchConstraint sc : searchParams.getConstraints()){
                for(FacetField ff : response.getFacetFields()){
                    if(ff.getName().equals(sc.getName())){
                        List<Count> values = new LinkedList<Count>(ff.getValues());
                        for(Count c : values){
                            if(c.getName().equals(sc.getValue())){
                                ff.getValues().remove(c);
                            }
                        }
                    }
                }
             }
             return new PackageSummaryQueryResponse(packages,
                                                    facets, response.getResults().getNumFound());
         } catch (SolrServerException ex) {
             throw new AptrustException(ex);
         }
     }
     
 
     private HealthCheckInfo computePackageHealthCheck(String institutionId, String packageId) throws AptrustException {
         SolrQueryClause contentRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "content");
         SolrQueryClause currentInstitution = new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
         SolrQueryClause fromPackage = new SolrQueryClause(AptrustSolrDocument.PACKAGE_ID, packageId);
 
         SolrQueryClause query = contentRecords.and(currentInstitution).and(fromPackage);
 
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
         params.set("facet", "true");
         params.set("facet.field", AptrustSolrDocument.LAST_HEALTH_CHECK_DATE, AptrustSolrDocument.FAILED_HEALTH_CHECK);
         try {
             QueryResponse response = solr.query(params);
             boolean failed = false;
             Date lastHealthCheckDate = null;
             for (Count c : response.getFacetField(AptrustSolrDocument.FAILED_HEALTH_CHECK).getValues()) {
                if (c.getName().equals("true") && c.getCount() > 0) {
                     failed = true;
                 }
             }
             if (response.getFacetField(AptrustSolrDocument.LAST_HEALTH_CHECK_DATE).getValues().isEmpty()) {
                 // no last health check
             } else {
                 // most common health check date is reported as the date for the package, this is potentially 
                 // incorrect for the package (ie, the package as a whole may never have been checked)
                 // NOTE this should be fixed with a future version
                 String dateStr = response.getFacetField(AptrustSolrDocument.LAST_HEALTH_CHECK_DATE).getValues().get(0).getName();
                 try {
                     lastHealthCheckDate = parseSolrDateFacet(dateStr);
                     logger.debug("Date: " + dateStr + " was parsed as " + lastHealthCheckDate);
                 } catch (ParseException ex) {
                     logger.warn("Error parsing date value \"" + dateStr + "\" from " + AptrustSolrDocument.LAST_HEALTH_CHECK_DATE + " facet.", ex);
                 }
             }
 
             return new HealthCheckInfo(lastHealthCheckDate, !failed);
         } catch (Exception ex) {
             throw new AptrustException(ex);
         }
     }
 
     /**
      * Builds a complete AptrustPackageDetail through queries to the Solr
      * server.
      */
     public AptrustPackageDetail getPackageDetail(String institutionId,
                                                  String packageId)
         throws AptrustException {
         String institutionName =
             getInstitutionInfo(institutionId).getFullName();
         SolrQueryClause packageRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "package");
         SolrQueryClause idClause =
             new SolrQueryClause(AptrustSolrDocument.ID, packageId);
 
         SolrQueryClause query = packageRecords.and(idClause);
 
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
         logger.debug("getPackageDetail: " + query.toString());
 
         try {
             QueryResponse response = solr.query(params);
             if (response.getResults().getNumFound() == 0) {
                 return null;
             } else if (response.getResults().getNumFound() > 1) {
                 throw new AptrustException("Multiple fields found with same id ("
                     + packageId + ")!");
             } else {
                 SolrDocument doc = response.getResults().get(0);
                 AptrustPackageDetail p = new AptrustPackageDetail();
 
                 // populate the easily mapped fields
                 AptrustSolrDocument.populateFromSolrDocument(p, doc);
 
                 // populate complex fields
                 p.setInstitutionName(institutionName);
                 
                 // ingested by, modified by, ingest date
                 ModifiableSolrParams subqueryParams = new ModifiableSolrParams();
                 SolrQueryClause ingestRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "ingest");
                 SolrQueryClause currentInstitution = new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
                 SolrQueryClause completed = new SolrQueryClause(AptrustSolrDocument.OPERATION_STATUS, IngestStatus.COMPLETED.name());
                 SolrQueryClause containingPackage = new SolrQueryClause(AptrustSolrDocument.INCLUDED_PACKAGE, p.getId());
                 SolrQueryClause ingestsQuery = ingestRecords.and(currentInstitution).and(completed).and(containingPackage);
                 subqueryParams.set("q", ingestsQuery.toString());
                 subqueryParams.set("sort", AptrustSolrDocument.OPERATION_END_DATE + " desc");
                 subqueryParams.set("rows", "1");
                 
                 QueryResponse subqueryResponse = solr.query(subqueryParams);
                 if (subqueryResponse.getResults().getNumFound() > 0) {
                     IngestProcessSummary s = new IngestProcessSummary();
                     AptrustSolrDocument.populateFromSolrDocument(s, subqueryResponse.getResults().get(0));
                     p.setIngestedBy(s.getInitiatingUser());
                     p.setIngestDate(s.getEndDate());
                     if (subqueryResponse.getResults().getNumFound() > 1) {
                         subqueryParams.set("start", String.valueOf(subqueryResponse.getResults().getNumFound() - 1));
                         subqueryResponse = solr.query(subqueryParams);
                         AptrustSolrDocument.populateFromSolrDocument(s, subqueryResponse.getResults().get(0));
                     }
                     p.setModifiedBy(s.getInitiatingUser());
                     p.setModifiedDate(s.getEndDate());
                 } else {
                     logger.warn("No ingest operations found for package " + p.getId());
                 }
 
                 // populate the health check
                 p.setHealthCheckInfo(computePackageHealthCheck(institutionId, p.getId()));
 
                 // query to populate object details
                 List<ObjectDescriptor> objects =
                     new ArrayList<ObjectDescriptor>();
                 ModifiableSolrParams objectQueryParams =
                     new ModifiableSolrParams();
                 objectQueryParams.set("q",
                                       new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE,
                                                           "object").and(new SolrQueryClause(AptrustSolrDocument.PACKAGE_ID,
                                                                                             p.getId()))
                                                                    .getQueryString());
                 objectQueryParams.set("sort", AptrustSolrDocument.ID + " asc");
 
                 SolrDocumentList page =
                     solr.query(objectQueryParams).getResults();
                 for (long i = 0; i < page.getNumFound(); i++) {
                     int pageOffset = (int) (i - page.getStart());
                     SolrDocument d = page.get(pageOffset);
                     ObjectDescriptor o = new ObjectDescriptor();
                     AptrustSolrDocument.populateFromSolrDocument(o, d);
                     objects.add(o);
                     if (pageOffset + 1 >= page.size()) {
                         // fetch next page of results
                         objectQueryParams.set("start", String.valueOf(i + 1));
                         page = solr.query(objectQueryParams).getResults();
                     }
                 }
                 p.setObjectDescriptors(objects);
                 return p;
             }
         } catch (SolrServerException ex) {
             throw new AptrustException(ex);
         }
     }
 
     /**
      * Builds a complete AptrustObject detail through queries to the Solr
      * server.
      */
     public AptrustObjectDetail getObjectDetail(String institutionId,
                                                String packageId,
                                                String objectId)
         throws AptrustException {
         
         validateInstitutionId(institutionId);
         
         SolrQueryClause objectRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "object");
         SolrQueryClause idClause =
             new SolrQueryClause(AptrustSolrDocument.ID, objectId);
 
         SolrQueryClause query = objectRecords.and(idClause);
 
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
         logger.debug("getObjectDetail(): " + query.toString());
         try {
             QueryResponse response = solr.query(params);
             if (response.getResults().getNumFound() == 1) {
                 SolrDocument doc = response.getResults().get(0);
                 AptrustObjectDetail d = new AptrustObjectDetail();
                 AptrustSolrDocument.populateFromSolrDocument(d, doc);
 
                 // get information on content (datastream versions or files)
                 ModifiableSolrParams subqueryParams = new ModifiableSolrParams();
                 SolrQueryClause contentRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "content");
                 SolrQueryClause currentInstitution = new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
                 SolrQueryClause fromObject = new SolrQueryClause(AptrustSolrDocument.OBJECT_ID, d.getLocalId());
                 SolrQueryClause contentQuery = contentRecords.and(currentInstitution).and(fromObject);
                 subqueryParams.set("q", contentQuery.toString());
                 subqueryParams.set("sort", AptrustSolrDocument.ID + " asc");
                 List<ContentSummary> summaries = new ArrayList<ContentSummary>();
                 SolrDocumentList page = solr.query(subqueryParams).getResults();
                 for (long i = 0; i < page.getNumFound(); i++) {
                     int pageOffset = (int) (i - page.getStart());
                     SolrDocument content = page.get(pageOffset);
                     ContentSolrDocument csd = new ContentSolrDocument();
                     AptrustSolrDocument.populateFromSolrDocument(csd, content);
                     ContentSummary sum = new ContentSummary();
                     sum.setLastFixityCheck(csd.getHealthCheckDate());
                     sum.setPassed(!Boolean.parseBoolean(csd.getFailedHealthCheck()));
                     sum.setName(csd.getContentId());
                     summaries.add(sum);
                     if (pageOffset + 1 >= page.size()) {
                         // fetch next page of results
                         subqueryParams.set("start", String.valueOf(i + 1));
                         page = solr.query(subqueryParams).getResults();
                     }
                 }
                 d.setContentSummaries(summaries);
 
                 return d;
             } else if (response.getResults().getNumFound() > 1) {
                 throw new AptrustException("Solr Configuration Error: Solr has more than one record with id "
                     + objectId + "!");
             } else {
                 return null;
             }
         } catch (SolrServerException ex) {
             throw new AptrustException(ex);
         }
     }
 
     private long getResponseCount(String query) throws SolrServerException {
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query);
         return solr.query(params).getResults().getNumFound();
     }
 
     private QueryResponse fetchFacetPage(String field, int offset, int max)
         throws SolrServerException {
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("facet.field", "institution_id");
         params.set("facet", "true");
         params.set("facet.limit", max);
         params.set("facet.offset", offset);
         return solr.query(params);
     }
     
     @Override
     public String getStorageReport(String institutionId, boolean staging) throws AptrustException {
         try {
             String spaceId = institutionId;
             if(staging){
                 spaceId +="staging";
             }
             
             String storeId = this.config.getDuraCloudProviderId();
             
             String url = this.config.getDuracloudUrl()
                 + "duradmin/storagereport/summaries?storeId=" + storeId
                 + "&spaceId=" + spaceId;
 
             String username = this.config.getDuracloudUsername();
             String password = this.config.getDuracloudPassword();
 
             RestHttpHelper helper = new RestHttpHelper(new Credential(username, password));
             
             HttpResponse response = helper.get(url);
 
             return response.getResponseBody();
         } catch (Exception e) {
             e.printStackTrace();
             throw new AptrustException(e);
         }
     }
     
     @Override
     public void rebuildIndex() throws AptrustException{
         log.info("rebuilding index...");
         //FIXME To be Implemented.
         log.warn("rebuildIndex() not implemented.");
         throw new AptrustException("Not implemented!");
     }
 }
