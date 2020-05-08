 package org.aptrust.ingest.ips;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.aptrust.client.api.IngestStatus;
 import org.aptrust.client.impl.SolrQueryClause;
 import org.aptrust.common.duracloud.StubbornContentStore;
 import org.aptrust.common.exception.AptrustException;
 import org.aptrust.common.metadata.APTrustMetadata;
 import org.aptrust.common.solr.AptrustSolrDocument;
 import org.aptrust.common.solr.ContentSolrDocument;
 import org.aptrust.ingest.api.DigitalObject;
 import org.aptrust.ingest.api.IngestManifest;
 import org.aptrust.ingest.api.IngestPackage;
 import org.aptrust.ingest.dspace.DSpaceAIPPackage;
 import org.aptrust.ingest.exceptions.UnrecognizedContentException;
 import org.aptrust.ingest.ips.solr.IngestSolrDocument;
 import org.aptrust.ingest.ips.solr.ObjectSolrDocument;
 import org.aptrust.ingest.ips.solr.PackageSolrDocument;
 import org.duracloud.client.ContentStore;
 import org.duracloud.domain.Content;
 import org.duracloud.error.ContentStoreException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 import com.github.cwilper.fcrepo.dto.core.ControlGroup;
 import com.github.cwilper.fcrepo.dto.core.Datastream;
 import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
 import com.github.cwilper.fcrepo.dto.core.FedoraObject;
 import com.github.cwilper.fcrepo.dto.foxml.FOXMLReader;
 import com.yourmediashelf.fedora.client.FedoraClient;
 import com.yourmediashelf.fedora.client.FedoraClientException;
 
 /**
  * <p>
  *   A class that can respond to updates in files in a single AP Trust staging 
  *   space.  The current implementation is not thread safe and should run 
  *   in a single thread with exclusive "write" access to records in the Solr
  *   index that pertain to the institution to which this space belongs.  It does
  *   however support having one instance of this class for each staging space 
  *   running in parallel.
  * </p>
  * <p>
  *   The normal use case is that this class is made aware of manifest files and
  *   the files pointed to by those AP Trust ingest manifests.  Once a complete
  *   object arrives, it is ingested into the AP Trust system, but only when the
  *   entire manifest's content is processed does Solr get updated for the
  *   packages and objects.
  * </p>
  * <p>
  *   The current implementation of this class is designed to keep track of the
  *   current set of available content in the staging space based on the 
  *   assumption that notifyUpdate() will be called for every file that appears
  *   in the space.  Because operations against the DuraCloud space are costly,
  *   this class caches information it gleans when it first identifies files and
  *   only has to access them again when ready to complete the ingest.
  * </p>
  * <p>
  *   Currently not-supported: 
  *   <ul>
  *     <li>package/object replacement</li>
  *   </ul>
  * </p>
  */
 public class DropboxProcessor implements SpaceListener {
 
     private static final String INGEST_CMODEL_PID = "aptrust:ingest";
     private static final String MANIFEST_DSID = "manifest";
     private static final String PACKAGE_CMODEL_PID = "aptrust:package";
     private static final String HAS_MODEL_PREDICATE = "info:fedora/fedora-system:def/model#hasModel";
 
     final Logger logger = LoggerFactory.getLogger(DropboxProcessor.class);
 
     private String stagingSpaceId;
 
     private String productionSpaceId;
 
     private String institutionId;
 
     private FedoraClient fc;
 
     private SolrServer solr;
 
     private ContentStore contentStore;
 
     private ContentAnalysisCache cache;
 
     public DropboxProcessor(String spaceId, FedoraClient fc, SolrServer solr, ContentStore cs) throws ContentStoreException, AptrustException, IOException {
         contentStore = new StubbornContentStore(cs);
         this.fc = fc;
         stagingSpaceId = spaceId;
         if (!isStagingSpace(spaceId)) {
             throw new AptrustException("The space \"" + spaceId + "\" does not appear to be a staging space.");
         }
         institutionId = getInstitutionIdFromStagingSpaceId(spaceId);
         productionSpaceId = institutionId;
         this.solr = solr;
         cache = new InMemoryContentAnalysisCache();
 
         // walk through all present content
         Iterator<String> contentIdIt = cs.getSpaceContents(spaceId);
         while (contentIdIt.hasNext()) {
             processContentId(contentIdIt.next(), true);
         }
     }
 
     public void notifyUpdate(String contentId) throws ContentStoreException, AptrustException, IOException {
         logger.trace("notifyUpdate({})", contentId);
         processContentId(contentId, false);
     }
 
     public void processContentId(String contentId, boolean offline) throws ContentStoreException, AptrustException, IOException {
         // 1.  determine if it's a manifest or another file
         if (isManifest(contentId)) {
             logger.trace("{} is a manifest file", contentId);
             processNewManifest(contentId, offline);
         } else {
             logger.trace("{} is not a manifest file", contentId);
             processNonManifestFile(contentId, offline);
         }
     }
 
     public void notifyDelete(String contentId) {
         logger.trace("notifyDelete({})", contentId);
         cache.forgetContentId(contentId);
     }
 
     /**
      * Determine if a given file is an APTrust manifest.  The current
      * implementation looks for the "aptrust_manifest" tag on the referenced
      * content id.
      * @throws ContentStoreException if an exception prevents this code from 
      * examining the tags for a contentId.
      */
     private boolean isManifest(String contentId) throws ContentStoreException {
         Map<String, String> p = contentStore.getContentProperties(stagingSpaceId, contentId);
         return p.containsKey("tags") && parseTags(p.get("tags")).contains("aptrust_manifest");
     }
 
     /**
      * @param manifestFile the manifest xml file, exactly as it appeared when
      * discovered by the ingest processor.
      */
     private void processNewManifest(String contentId, boolean offline) throws AptrustException {
         File manifestFile = null;
         String pid = null;
         List<String> packagePids = null;
         List<String> objectPids = new ArrayList<String>();
         IngestManifest manifest = null;
         try {
             manifestFile = File.createTempFile("manifest", ".xml");
             logger.trace("created manifest file \"{}\"", manifestFile.getName());
             downloadContent(stagingSpaceId, contentId, manifestFile);
             logger.trace("downloaded manifest from DuraCloud");
 
             // 1. create a fedora object for the ingest operation
             pid = FedoraClient.ingest().execute(fc).getPid();
             logger.trace("created new object {} for manifest", pid);
             FedoraClient.addRelationship(pid).object("info:fedora/" + INGEST_CMODEL_PID).predicate(HAS_MODEL_PREDICATE).execute(fc);
             FedoraClient.addDatastream(pid, MANIFEST_DSID).content(manifestFile).execute(fc);
             logger.trace("added manifest and relationship to ingest operation object {}", pid);
 
             // 2. create any new package objects required by the manifest and
             //    validate/update any referenced packages
             Unmarshaller u = JAXBContext.newInstance(IngestManifest.class, IngestPackage.class, DigitalObject.class, APTrustMetadata.class).createUnmarshaller();
             manifest = (IngestManifest) u.unmarshal(manifestFile);
             manifest.setId(pid);
             logger.trace("parsed manifest and set id");
             packagePids = new ArrayList<String>();
             for (IngestPackage p : manifest.getPackagesToSubmit()) {
                 if (p.getMetadata().getId() != null) {
                     // new package
                     String packagePid = FedoraClient.ingest().execute(fc).getPid();
                     logger.trace("created package object {}", packagePid);
                     packagePids.add(packagePid);
                     p.getMetadata().setId(packagePid);
                     p.getMetadata().setInstitution(institutionId);
                     PackageRELSEXT.Description d = new PackageRELSEXT.Description(p.getMetadata());
                     d.setId("info:fedora/" + packagePid);
                     d.setContentModelURIs(new PackageRELSEXT.ResourceDesignator[] { new PackageRELSEXT.ResourceDesignator("info:fedora/" + PACKAGE_CMODEL_PID) });
                     ArrayList<PackageRELSEXT.ResourceDesignator> uris = new ArrayList<PackageRELSEXT.ResourceDesignator>();
                     for (DigitalObject o : p.getDigitalObjects()) {
                         objectPids.add(o.getId());
                         uris.add(new PackageRELSEXT.ResourceDesignator(o.getId()));
                     }
                     d.setIncludedResourceURIs(uris.toArray(new PackageRELSEXT.ResourceDesignator[0]));
                     PackageRELSEXT relsExt = new PackageRELSEXT();
                     relsExt.setDescription(d);
                     File relsExtFile = File.createTempFile("RELS-EXT", "-rdf.xml");
                     Marshaller m = JAXBContext.newInstance(PackageRELSEXT.class).createMarshaller();
                     m.marshal(relsExt, relsExtFile);
                     logger.trace("marshalled the metadata to the RELS-EXT temporary file {}", relsExtFile.getAbsolutePath());
                     FedoraClient.addDatastream(packagePid, "RELS-EXT").content(relsExtFile).controlGroup("X").formatURI("info:fedora/fedora-system:FedoraRELSExt-1.0").mimeType("application/rdf+xml").execute(fc);
                     relsExtFile.delete();
                     logger.trace("deleted RELS-EXT temporary file {}", relsExtFile.getAbsolutePath());
                     logger.trace("updated package object {} with metadata and relationships", packagePid);
                 } else {
                     // determine if the update is supported
                     throw new UnsupportedOperationException("Updating existing packages is not yet supported.");
                     // update package
                 }
             }
 
             // 3. move the manifest to the production space
             contentStore.moveContent(stagingSpaceId, contentId, productionSpaceId, pid);
 
             // 4. store the updated manifest in fedora
             Marshaller m = JAXBContext.newInstance(IngestManifest.class, IngestPackage.class, DigitalObject.class, APTrustMetadata.class).createMarshaller();
             m.marshal(manifest, manifestFile);
             FedoraClient.addDatastream(pid, MANIFEST_DSID).content(manifestFile).execute(fc);
             logger.trace("updated manifest in ingest operation object {}", pid);
 
             
             // 5. write that manifest to Solr (which will be an "in-progress" 
             //    operation
             IngestSolrDocument d = IngestSolrDocument.newIngest(institutionId, manifest);
             solr.add(AptrustSolrDocument.createValidSolrDocument(d));
             solr.commit();
             logger.info("wrote manifest {} to solr", manifest.getId() );
 
             // 6. see if any of the referenced objects have already arrived and 
             //    process them
             long ingestedObjectCount = 0;
             for (String oPid : objectPids) {
                 if (cache.isObjectComplete(oPid)) {
                     ingestObject(pid, ingestedObjectCount ++, oPid, offline);
                 }
             }
 
         } catch (Throwable t) {
             try {
                 logger.error("Error while processing manifest", t);
                 if (pid != null) {
                     FedoraClient.purgeObject(pid).execute(fc);
                 }
                 solr.rollback();
 
                 // now log the error in Solr
                 if (manifest != null) {
                     IngestSolrDocument doc = IngestSolrDocument.failedIngest(institutionId, manifest, "System error while processing manifest!" + t.getMessage() != null ? " (" + t.getMessage() + ")" : "");
                     solr.add(AptrustSolrDocument.createValidSolrDocument(doc));
                     solr.commit();
                 }
             } catch (Throwable t2) {
                 throw new AptrustException("Exception while attempting to recover from previous exception." + pid + "!", t2);
             }
         } finally {
             if (manifestFile != null && manifestFile.exists()) {
                 manifestFile.delete();
                 logger.trace("deleted manifest file {}", manifestFile.getAbsolutePath());
             }
         }
     }
 
     private void processNonManifestFile(String contentId, boolean offline) throws AptrustException {
         // 1.  determine the PID represented by the file (if possible do this
         //     without reading the file
         String objectId = null;
         try {
             objectId = determineObjectId(contentId);
         } catch (UnrecognizedContentException ex) {
             // log the error but do nothing... this object is unrecognized
             logger.info(ex.getContentId() + " was not recognized as a Fedoara or DSpace submission.");
             return;
         }
 
         // 2.  determine if all of that object is present
         try {
             addRecognizedContent(contentId, objectId);
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }
 
         if (!cache.isObjectComplete(objectId)) {
             return;
         }
 
         // 3.  query SOLR to determine if there's a manifest waiting for this
         //     object
         ModifiableSolrParams params = new ModifiableSolrParams();
         SolrQueryClause ingestRecords = new SolrQueryClause(AptrustSolrDocument.RECORD_TYPE, "ingest");
         SolrQueryClause currentInstitution = new SolrQueryClause(AptrustSolrDocument.INSTITUTION_ID, institutionId);
         SolrQueryClause inProgress = new SolrQueryClause(AptrustSolrDocument.OPERATION_STATUS, IngestStatus.IN_PROGRESS.name());
         SolrQueryClause hasPid = new SolrQueryClause(AptrustSolrDocument.INCLUDED_PID, objectId);
         String query = ingestRecords.and(currentInstitution).and(inProgress).and(hasPid).getQueryString();
         params.set("q", query);
         QueryResponse solrResponse = null;
         try {
             logger.trace("searching for \"{}\"", query);
             solrResponse = solr.query(params);
             logger.trace(solrResponse.getResults().getNumFound() + " items found");
             if (solrResponse.getResults().getNumFound() > 1) {
                 throw new AptrustException("There are more than one manifest expecting the object " + objectId);
             }
         } catch (SolrServerException ex) {
             throw new AptrustException(ex);
         }
 
         // 3a. if not, return.  When the manifest arrives it will be ingested.
         if (solrResponse.getResults().getNumFound() == 0) {
             logger.trace("no manifest found for complete object " + objectId);
             return;
         } else {
             // 3b. if so, process this object and update the "ingest" record as
             //     well as the "package" and "object" records
             String manifestId = (String) solrResponse.getResults().get(0).getFieldValue(AptrustSolrDocument.ID); 
             try {
                 ingestObject(manifestId, ((Integer) solrResponse.getResults().get(0).getFieldValue(AptrustSolrDocument.COMPLETED_OBJECT_COUNT)).longValue(), objectId, offline);
             } catch (Exception ex) {
                 logger.error("Error ingesting object " + objectId, ex);
                 // 4.  if any sort of error occurs while processing the file, update
                 //     the solr "ingest" record for the manifest to indicate the failure
                 //     (also this should be noted somewhere in the provenance record
                 //     for safekeeping)
                 try {
                     reportIngestError(ex, pullManifestFromFedora(manifestId));
                 } catch (Exception exe) {
                     throw new RuntimeException(exe);
                 }
             }
         }
     }
 
     /**
      * Stores a record in Solr indicating an exception that occurred while
      * processing a manifest or file.
      * @throws IOException 
      * @throws SolrServerException 
      */
     private void reportIngestError(Throwable t, IngestManifest m) {
         try {
             solr.add(AptrustSolrDocument.createValidSolrDocument(IngestSolrDocument.failedIngest(institutionId, m, t.getMessage())));
             solr.commit();
         } catch (Throwable thrown) {
             logger.error("Error while reporting error!", thrown);
         }
     }
 
     /**
      * <p>
      * A helper method that maintains the idToContentMap, foxmlCache and 
      * contentIdToChunkManifestMap which maintain caches of information about
      * existing content to facilitate quickly processing once the remaining
      * pieces or manifest arrive.
      * </p>
      * <p>
      * The current implementation stores this information in memory.  That is 
      * short-term solution.  To scale up, this should use some other mechanism
      * (perhaps Solr or a RDBMS).
      * </p>
      * @throws ContentStoreException 
      * @throws IOException 
      * @throws JAXBException 
      * @throws ParserConfigurationException 
      * @throws SAXException 
      */
     private void addRecognizedContent(String contentId, String objectId) throws ContentStoreException, IOException, JAXBException, SAXException, ParserConfigurationException {
         // parse and add it to the foxmlCache if appropriate
         Matcher m = FOXML_CONTENTID_PATTERN.matcher(contentId);
         if (m.matches()) {
             Content foxml = contentStore.getContent(stagingSpaceId, contentId);
             logger.debug("parsing and caching foxml for " +  objectId);
             FedoraObject o = new FOXMLReader().readObject(foxml.getStream());
             cache.cacheObjectTitle(o.pid(), o.label());
             cache.assertObjectParts(getRequiredContentIdsFromFedoraObject(o), o.pid());
         }
 
         // parse and add it to the manifest cache if appropriate
         if (contentId.endsWith(".dura-manifest")) {
             Content manifest = contentStore.getContent(stagingSpaceId, contentId);
             JAXBContext jc = JAXBContext.newInstance(DuraChunkManifest.class);
             Unmarshaller u = jc.createUnmarshaller();
             DuraChunkManifest chunkManifest = (DuraChunkManifest) u.unmarshal(manifest.getStream());
             cache.notifyChunkManifest(contentId.replace(".dura-manifest", ""), chunkManifest);
         }
 
         // notify the cache of the content
         cache.notifyContentId(contentId);
     }
 
     /**
      * Walks through the fedora object and for each version of each MANAGED 
      * datastream, creates the contentId that FedoraCloudSync would use for that
      * datastream version.
      */
     private Collection<String> getRequiredContentIdsFromFedoraObject(FedoraObject o) {
         List<String> requiredContentIds = new ArrayList<String>();
         requiredContentIds.add(o.pid());
         for (Datastream ds : o.datastreams().values()) {
             if (ds.controlGroup().equals(ControlGroup.MANAGED)) {
                 for (DatastreamVersion v : ds.versions()) {
                     requiredContentIds.add(o.pid() + "+" + ds.id() + "+" + v.id());
                 }
             }
         }
         return requiredContentIds;
     }
 
     public static final Pattern FEDORA_CLOUDSYNC_CONTENTID_PATTERN = Pattern.compile("^(([A-Za-z0-9]|-|\\.)+:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+)(\\+.*)*$");
     public static final Pattern FOXML_CONTENTID_PATTERN = Pattern.compile("^(([A-Za-z0-9]|-|\\.)+:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+)$");
     
     /**
      * Determines the local (institution-specific) identifier for the object 
      * that is represented in the given content Id.  For Fedora objects, this
      * method uses the well-known naming conventions of Fedora CloudSync.  For
      * files ending in the Zip extension, this method assumes they are DSPace
      * AIPs and parses the ID from the "mets.xml" entry.
      * @param spaceId the id of the space in which the content resides
      * @param contentId the ide of the content
      * @return the object ID for the specified contentId
      * @throws AptrustException if any exception occurs while analyzing or
      * accessing the content
      * @throws UnrecognizedContentException in the event that the content is
      * not recognized as belonging to a fedora or dspace object.
      */
     private String determineObjectId(String contentId) throws AptrustException {
         // 1.  see if it's part of a fedora object
         Matcher m = FEDORA_CLOUDSYNC_CONTENTID_PATTERN.matcher(contentId);
         if (m.matches()) {
             return m.group(1);
         }
 
         // 2.  see if it's a zip file (dspace AIP)
         Pattern zipFilePattern = Pattern.compile("^.*\\.[zZ][iI][pP]$");
         m = zipFilePattern.matcher(contentId);
         if (m.matches()) {
             try {
                 DSpaceAIPPackage p = new DSpaceAIPPackage(contentStore.getContent(stagingSpaceId, contentId).getStream());
                 cache.cacheObjectTitle(p.getId(), p.getTitle());
                 return p.getId();
             } catch (ContentStoreException ex) {
                 throw new AptrustException(ex);
             }
         }
 
         // 3.  for now, other files are unrecognized
         throw new UnrecognizedContentException(contentId);
     }
 
     /**
      * Completes the ingest of a single object.  This breaks any possibility
      * for package-based transactions, but is acceptable given that the
      * architecture will likely change to an object-centric approach.
      * @param ingestDoc the SolrDocument for the ingest operation for which
      * this object update is part.
      * @param ref a reference to the content whose arrival instigate the ingest
      * of the object.  This method must only be called after it has been
      * verified that all other pieces of content that make up the object have
      * arrived.
      * @throws JAXBException 
      * @throws FedoraClientException 
      */
     private void ingestObject(String manifestId, long ingestedObjectCount, String pid, boolean offline) {
         IngestManifest manifest = null;
         try {
             // 1.  pull the manifest from fedora
             manifest = pullManifestFromFedora(manifestId);
     
             IngestPackage ingestPackage = null;
             for (IngestPackage p : manifest.getPackagesToSubmit()) {
                 for (DigitalObject o : p.getDigitalObjects()) {
                     if (o.getId().equals(pid)) {
                         ingestPackage = p;
                         break;
                     }
                 }
             }
     
             // 2.  move the content to production)
             for (String contentId : cache.getObjectContent(pid)) {
                 contentStore.copyContent(stagingSpaceId, contentId, productionSpaceId, contentId);
                 // TODO: ensure that fixity checking is part of the copy operation in DuraCloud
                 solr.add(AptrustSolrDocument.createValidSolrDocument(new ContentSolrDocument(institutionId, ingestPackage.getMetadata().getId(), pid, contentId, true, new Date())));
                 logger.info("Copied content {} from staging to production.", contentId);
             }
 
             for (String contentId : cache.getObjectContent(pid)) {
                 contentStore.deleteContent(stagingSpaceId, contentId);
                 if (offline) {
                     cache.forgetContentId(contentId);
                 }
                 logger.info("Deleted content {} from staging.", contentId);
             }
 
             // 3.  update the manifest in Solr
             ingestedObjectCount ++;
             if (ingestedObjectCount == manifest.getTotalObjectsToSubmit()) {
                 // create all the package and object records in Solr
                 for (IngestPackage p : manifest.getPackagesToSubmit()) {
                     PackageSolrDocument pDoc = new PackageSolrDocument(p);
                     solr.add(AptrustSolrDocument.createValidSolrDocument(pDoc));
                     for (DigitalObject o : p.getDigitalObjects()) {
                         ObjectSolrDocument oDoc = new ObjectSolrDocument(o.getId(), p, cache.getObjectTitle(o.getId()));
                         solr.add(AptrustSolrDocument.createValidSolrDocument(oDoc));
                         cache.forgetObject(o.getId());
                     }
                 }
                 // update the ingest object in Solr
                 IngestSolrDocument solrDoc = IngestSolrDocument.completedIngest(institutionId, manifest, new Date());
                 solr.add(AptrustSolrDocument.createValidSolrDocument(solrDoc));
                 solr.commit();
             } else {
                 IngestSolrDocument solrDoc = IngestSolrDocument.updateIngest(institutionId, manifest, (int) ingestedObjectCount);
                 solr.add(AptrustSolrDocument.createValidSolrDocument(solrDoc));
                 solr.commit();
             }
         } catch (Throwable t) {
             logger.error("Exception while ingesting object " + pid + ".", t);
             try {
                 solr.rollback();
                 reportIngestError(t, manifest);
             } catch (Exception ex) {
                 logger.error("Exception while rolling back solr!", ex);
             }
         }
     }
 
     /**
      * A helper method to fetch (and parse) the specified IngestManifest from 
      * fedora.
      * @param manifestId the id (pid) of the manifest
      * @return an IngestManifest object representing the current version from
      * fedora
      * @throws JAXBException if an error occurs parsing the XML
      * @throws FedoraClientException if an error occurs accessing fedora
      */
     private IngestManifest pullManifestFromFedora(String manifestId) throws JAXBException, FedoraClientException {
         JAXBContext jc = JAXBContext.newInstance(IngestManifest.class, IngestPackage.class, DigitalObject.class, APTrustMetadata.class);
         Unmarshaller u = jc.createUnmarshaller();
         IngestManifest manifest = (IngestManifest) u.unmarshal(FedoraClient.getDatastreamDissemination((String) manifestId, MANIFEST_DSID).execute(fc).getEntityInputStream());
         manifest.setId(manifestId);
         return manifest;
     }
 
     /**
      * Parses multiple values out of the string representation of the "Tags"
      * property.  The current implementation assumes a |-separated list.
      * @param tags a |-separated list of values
      * @return a collection containing one or more tags parsed from the value
      */
     private Collection<String> parseTags(String tags) {
         return (tags.contains("|") ? Arrays.asList(tags.split("|")) : Collections.singleton(tags));
     }
 
     /**
      * A helper method that downloads content from DuraCloud to a local file.
      */
     private void downloadContent(String spaceId, String contentId, File localFile) throws ContentStoreException, FileNotFoundException, IOException {
         // Consider adding retries upon errors
         Content content = contentStore.getContent(spaceId, contentId);
         IOUtils.copy(content.getStream(), new FileOutputStream(localFile));
     }
 
     /**
      * Determines if the space specified in the update event is an APTrust 
      * staging space (from which content should be processed).  The current
      * implementation relies on the naming convention that all "staging" spaces
      * have a name that is made up of the institution id followed by the word
      * "staging".
      */
     private boolean isStagingSpace(String spaceId) throws ContentStoreException {
         return (spaceId.endsWith("staging"));
     }
 
     /**
      * Gets the institution Id from a staging space name.  This is simply done
      * by removing the "spacing" from the end of the spaceId.
      * @param spaceId the id of a known staging space
      * @return the institution Id
      */
     private String getInstitutionIdFromStagingSpaceId(String spaceId) {
         if (!spaceId.endsWith("staging")) {
             throw new IllegalArgumentException("Not a staging space!");
         } else {
             return spaceId.substring(0, spaceId.length() - 7);
         }
     }
 }
