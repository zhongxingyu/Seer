 package org.aptrust.client.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
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
 import org.aptrust.client.api.HealthCheckInfo;
 import org.aptrust.client.api.IngestProcessSummary;
 import org.aptrust.client.api.IngestStatus;
 import org.aptrust.client.api.InstitutionInfo;
 import org.aptrust.client.api.PackageSummary;
 import org.aptrust.client.api.PackageSummaryQueryResponse;
 import org.aptrust.client.api.SearchParams;
 import org.aptrust.client.api.Summary;
 import org.aptrust.common.exception.AptrustException;
 import org.aptrust.common.solr.AptrustSolrDocument;
 import org.duracloud.client.ContentStore;
 import org.duracloud.client.ContentStoreImpl;
 import org.duracloud.common.model.Credential;
 import org.duracloud.common.web.RestHttpHelper;
 import org.duracloud.common.web.RestHttpHelper.HttpResponse;
 import org.duracloud.error.ContentStoreException;
 import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 /**
  * An AptrustClient implementation that gets all available information from Solr
  * queries.
  */
 public class AptrustClientImpl implements AptrustClient {
 
    private final Logger logger = LoggerFactory.getLogger(AptrustClientImpl.class);
    
     protected ClientConfig config;
 
     protected SolrServer solr;
 
     public AptrustClientImpl(ClientConfig config) {
         if (config == null) {
             throw new IllegalArgumentException("client config must be non-null");
         }
 
         this.config = config;
         solr = new HttpSolrServer(config.getSolrUrl());
     }
 
     /**
      * Gets a summary of the status of content from the institution specified
      * by the institutionId parameter.  The current implementation makes use of
      * the Solr index as well as the "duraboss" REST API on the DuraSpace 
      * instance referenced in the configuration.
      */
     public Summary getSummary(String institutionId) throws AptrustException {
         Summary s = new Summary();
         s.setInstitutionId(institutionId);
 
         SolrQueryClause packageRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "package");
         SolrQueryClause objectRecords =
             new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "object");
         SolrQueryClause currentInstitution =
             new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
         SolrQueryClause dpnBound =
             new SolrQueryClause(AptrustSolrDocument.DPN_BOUND, "true");
         SolrQueryClause isPublic =
             new SolrQueryClause(AptrustSolrDocument.ACCESS_CONTROL_POLICY, "world");
         SolrQueryClause isPrivate =
             new SolrQueryClause(AptrustSolrDocument.ACCESS_CONTROL_POLICY, "private");
         SolrQueryClause isInstitutionOnly =
             new SolrQueryClause(AptrustSolrDocument.ACCESS_CONTROL_POLICY, "institution");
         SolrQueryClause failedHealthCheck =
             new SolrQueryClause(AptrustSolrDocument.FAILED_HEATH_CHECK, "true");
 
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
 
         // Get the total usage.  DuraCloud doesn't expose a quick query for
         // total space used.  The current implementation fetches the storage
         // report (which may not be up-to-the-minute) and reports the usage 
         // from that report.  If we need real-time storage usage, we should
         // consider maintaining a value in SOLR that is updated in response to
         // file updates/removals.
         RestHttpHelper rest = new RestHttpHelper(
                 new Credential(config.getDuracloudUsername(), 
                         config.getDuracloudPassword()));
         try {
             String url = config.getDuracloudUrl() + "duraboss/report/storage";
             HttpResponse response = rest.get(url);
             if (response.getStatusCode() / 100 != 2) {
                 throw new RuntimeException("HTTP Status code " 
                         + response.getStatusCode() + " returned from GET of "
                         + url);
             }
             DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             XPath xpath = XPathFactory.newInstance().newXPath();
             Document report = parser.parse(response.getResponseStream());
             try {
                 long totalBytes = Long.parseLong((String) xpath.evaluate("storageReport/storageMetrics/storageProviderMetrics/storageProvider[@id='" + config.getDuraCloudProviderId() + "']/spaceMetrics/space[@name='" + institutionId + "']/totalSize", report, XPathConstants.STRING));
                 s.setBytesUsed(totalBytes);
             } catch (NumberFormatException ex) {
                 // no bytes stored
                 s.setBytesUsed((long) 0);
             }
         } catch (Exception ex) {
             throw new AptrustException("Error extracting storage usage from the  DuraCloud storage report!", ex);
         }
         return s;
     }
 
     /**
      * Gets all institutions registered with AP Trust.  For an institution to
      * be registered there must be a Space in the APTrust DuraCloud instance 
      * with the "type" property set to "preservation_store".  The id for that
      * space is the Institution id, the "institution_display_name" property 
      * value is the display name.  
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
             for (String spaceId : cs.getSpaces()) {
                 Map<String, String> properties = cs.getSpaceProperties(spaceId);
                 if ("preservation_store".equals(properties.get("type"))) {
                     institutions.add(new InstitutionInfo(spaceId,
                             properties.get("institution_display_name")));
                 }
             }
             return institutions;
         } catch (ContentStoreException ex) {
             throw new AptrustException(ex);
         }
     }
 
     /**
      * Gets identifiers for all institutions for which any data is present in
      * the solr index. This implementation may not result in a comprehensive
      * list of institutions in cases where no content has been ingested. TODO:
      * possibly fetch this information from another source
      */
     public Collection<String> getInstitutionIds() throws AptrustException {
         int count = 0;
         List<String> institutionIds = new ArrayList<String>();
         do {
             try {
                 QueryResponse r =
                     fetchFacetPage(AptrustSolrDocument.INSTITUTION_ID,
                                    institutionIds.size(),
                                    100);
                 FacetField f = r.getFacetField(AptrustSolrDocument.INSTITUTION_ID);
                 for (Count c : f.getValues()) {
                     institutionIds.add(c.getName());
                 }
             } catch (SolrServerException ex) {
                 throw new AptrustException(ex);
             }
         } while (institutionIds.size() < count);
         return institutionIds;
     }
 
     /**
      * Queries DuraCloud for an institution with the given identifier.  The 
      * current implementation assumes that for every registered institution 
      * there is a space with that institution's id which has a property 
      * "institution_display_name" that contains the full name of that 
      * institution.
      * @return an InstitutionInfo for the given institutionId, or null if none
      * can be found
      * @throws AptrustException wrapping any exception caught while querying 
      * the DuraCloud API.
      */
     public InstitutionInfo getInstitutionInfo(String institutionId)
         throws AptrustException {
         ContentStore cs = new ContentStoreImpl(
                 config.getDuracloudUrl() + "durastore", 
                 StorageProviderType.valueOf(config.getDuraCloudProviderName()),
                 config.getDuraCloudProviderId(), 
                 new RestHttpHelper(
                         new Credential(config.getDuracloudUsername(), 
                                        config.getDuracloudPassword())));
         try {
             return new InstitutionInfo(
                     institutionId,cs.getSpaceProperties(institutionId).get(
                             "institution_display_name"));
         } catch (NullPointerException ex) {
             return null;
         } catch (ContentStoreException ex) {
             throw new AptrustException(ex);
         }
     }
 
     /**
      * Queries Solr for ingest processes from a given institution that match
      * the provided criteria.
      * <br />
      * TODO: This method should force paging of results, this method might
      * return an extremely large number of results!
      * 
      * @param institutionId (required) specifies the institution to which all
      * of the returned IngestProcessSummary objects pertain
      * @param startDate the date after which all the results must have begun
      * @param name the user-assigned title of all returned operations
      * @param status the status all returned operations must have
      */
     public List<IngestProcessSummary> findIngestProcesses(String institutionId,
             Date startDate, String name, IngestStatus status) throws AptrustException {
         SolrQueryClause ingestRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "ingest");
         SolrQueryClause currentInstitution = new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
 
         SolrQueryClause query =  ingestRecords.and(currentInstitution);
         if (status != null) {
             query = query.and(new SolrQueryClause(AptrustSolrDocument.OPERATION_STATUS, status.toString()));
         }
         if (name != null) {
             query = query.and(new SolrQueryClause(AptrustSolrDocument.TITLE, name));
         }
         if (startDate != null) {
             query = query.and(SolrQueryClause.dateRange(AptrustSolrDocument.OPERATION_START_DATE, startDate, null));
         }
         List<IngestProcessSummary> results = new ArrayList<IngestProcessSummary>();
 
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
        logger.debug(query.toString());
         try {
             QueryResponse response = solr.query(params);
             SolrDocumentList page = response.getResults();
             for (long i = 0; i < page.getNumFound(); i ++) {
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
 
     /**
      * Performs a query against Solr for packages from the given institute
      * that match the given SearchParams.
      * 
      * TODO: This method should force paging of results, this method might
      * return an extremely large number of results!
      */
     public PackageSummaryQueryResponse
         findPackageSummaries(String institutionId, SearchParams searchParams)
             throws AptrustException {
         
         String institutionName = getInstitutionInfo(institutionId).getFullName();
         SolrQueryClause packageRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "package");
         SolrQueryClause currentInstitution = new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
 
        SolrQueryClause query =  packageRecords.and(currentInstitution);
        if (searchParams.getQuery() != null && !searchParams.getQuery().equals("")) {
            query = query.and(SolrQueryClause.parseUserQuery(searchParams.getQuery()));
        }
         List<PackageSummary> packages = new ArrayList<PackageSummary>();
 
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
         params.set("facet", "true");
         params.set("f.field", AptrustSolrDocument.DPN_BOUND);
        logger.debug("findPackageSummaries: " + query.toString());
         try {
             QueryResponse response = solr.query(params);
             SolrDocumentList page = response.getResults();
             for (long i = 0; i < page.getNumFound(); i ++) {
                 int pageOffset = (int) (i - page.getStart());
                 SolrDocument doc = page.get(pageOffset);
                 PackageSummary s = new PackageSummary();
 
                 // populate the easily-mapped field
                 AptrustSolrDocument.populateFromSolrDocument(s, doc);
 
                 // there's a couple complex fields that must be populated 
                 // from data not simply stored in the Solr record
                 s.setInstitutionName(institutionName);
                 if (doc.containsKey(AptrustSolrDocument.LAST_HEALTH_CHECK_DATE)) {
                     s.setHealthCheckInfo(new HealthCheckInfo((Date) doc.getFieldValue(AptrustSolrDocument.LAST_HEALTH_CHECK_DATE), !Boolean.TRUE.equals(doc.getFieldValue(AptrustSolrDocument.FAILED_HEATH_CHECK))));
                 }
 
                 packages.add(s);
                 if (pageOffset + 1 >= page.size()) {
                     // fetch next page of results
                     params.set("start", String.valueOf(i + 1));
                     response = solr.query(params);
                 }
             }
             return new PackageSummaryQueryResponse(packages, response.getFacetFields());
         } catch (SolrServerException ex) {
             throw new AptrustException(ex);
         }
     }
 
     /**
      * Builds a complete AptrustPackageDetail through queries to the Solr
      * server.
      */
     public AptrustPackageDetail getPackageDetail(String institutionId, String packageId)
         throws AptrustException {
         String institutionName = getInstitutionInfo(institutionId).getFullName();
         SolrQueryClause packageRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "package");
         SolrQueryClause idClause = new SolrQueryClause(AptrustSolrDocument.ID, packageId);
 
         SolrQueryClause query =  packageRecords.and(idClause);
         
         ModifiableSolrParams params = new ModifiableSolrParams();
         params.set("q", query.toString());
        logger.debug("getPackageDetail: " + query.toString());
 
         try {
             QueryResponse response = solr.query(params);
             if (response.getResults().getNumFound() == 0) {
                 return null;
             } else if (response.getResults().getNumFound() > 1) {
                 throw new AptrustException("Multiple fields found with same id (" + packageId + ")!");
             } else {
                 SolrDocument doc = response.getResults().get(0);
                 AptrustPackageDetail p = new AptrustPackageDetail();
 
                 // populate the easily mapped fields
                 AptrustSolrDocument.populateFromSolrDocument(p, doc);
 
                 // populate complex fields
                 p.setInstitutionName(institutionName);
                 if (doc.containsKey(AptrustSolrDocument.LAST_HEALTH_CHECK_DATE)) {
                     p.setHealthCheckInfo(new HealthCheckInfo((Date) doc.getFieldValue(AptrustSolrDocument.LAST_HEALTH_CHECK_DATE), !Boolean.TRUE.equals(doc.getFieldValue(AptrustSolrDocument.FAILED_HEATH_CHECK))));
                 }
 
                 // query to populate object details
                 List<ObjectDescriptor> objects = new ArrayList<ObjectDescriptor>();
                 ModifiableSolrParams objectQueryParams = new ModifiableSolrParams();
                 objectQueryParams.set("q", new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "object").and(new SolrQueryClause(AptrustSolrDocument.PACKAGE_ID, p.getId())).getQueryString());
 
                 SolrDocumentList page = solr.query(objectQueryParams).getResults();
                 for (long i = 0; i < page.getNumFound(); i ++) {
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
     public AptrustObjectDetail getObjectDetail(String institutionId, String packageId, String objectId) throws AptrustException {
         SolrQueryClause objectRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "object");
         SolrQueryClause idClause = new SolrQueryClause(AptrustSolrDocument.ID, objectId);
 
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
                 return d;
             } else if (response.getResults().getNumFound() > 1) {
                 throw new AptrustException("Solr Configuration Error: Solr has more than one record with id " + objectId + "!");
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
 }
