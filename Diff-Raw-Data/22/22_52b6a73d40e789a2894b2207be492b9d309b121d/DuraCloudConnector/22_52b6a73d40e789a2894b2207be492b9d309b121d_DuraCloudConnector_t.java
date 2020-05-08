 package org.duraspace.fcrepo.cloudsync.service.backend;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.duracloud.chunk.ChunkableContent;
 import org.duracloud.chunk.manifest.ChunksManifest;
 import org.duracloud.chunk.manifest.ChunksManifestBean;
 import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
 import org.duracloud.chunk.stream.ChunkInputStream;
 import org.duraspace.fcrepo.cloudsync.service.backend.chunk.MultiChunkInputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import org.duraspace.fcrepo.cloudsync.api.ObjectInfo;
 import org.duraspace.fcrepo.cloudsync.api.ObjectStore;
 import org.duraspace.fcrepo.cloudsync.service.util.JSON;
 import org.duraspace.fcrepo.cloudsync.service.util.StringUtil;
 import com.github.cwilper.fcrepo.dto.core.ControlGroup;
 import com.github.cwilper.fcrepo.dto.core.Datastream;
 import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
 import com.github.cwilper.fcrepo.dto.core.FedoraObject;
 import com.github.cwilper.fcrepo.dto.foxml.FOXMLReader;
 import com.github.cwilper.fcrepo.dto.foxml.FOXMLWriter;
 import com.github.cwilper.fcrepo.httpclient.HttpClientConfig;
 import com.github.cwilper.fcrepo.httpclient.MultiThreadedHttpClient;
 import com.github.cwilper.ttff.Filter;
 
 public class DuraCloudConnector extends StoreConnector {
 
     private static final int CHUNKSIZE = 999;
     private static final long MAX_CONTENT_CHUNK_SIZE = 1073741824; // one GB
     private static final Logger logger = 
             LoggerFactory.getLogger(DuraCloudConnector.class);
 
     private final URI spaceURI;
     private final String providerId;
     private final String prefix;
 
     private final MultiThreadedHttpClient httpClient;
 
     public DuraCloudConnector(ObjectStore store, HttpClientConfig httpClientConfig) {
         Map<String, String> map = JSON.getMap(JSON.parse(store.getData()));
         providerId = StringUtil.validate("providerId", map.get("providerId"));
         prefix = StringUtil.normalize(map.get("prefix"));
 
         // Determine base URI of space and init httpClient
         String duraStoreUrl = StringUtil.validate("url", map.get("url"));
         while (duraStoreUrl.endsWith("/")) {
             duraStoreUrl = duraStoreUrl.substring(0, duraStoreUrl.length() - 1);
         }
 
         String space = StringUtil.validate("space", map.get("space"));
 
         spaceURI = URI.create(duraStoreUrl + "/" + space);
         int port = spaceURI.getPort();
         if (port <= 0) {
             if (spaceURI.getScheme().equals("http")) {
                 port = 80;
             } else {
                 port = 443;
             }
         }
         httpClient = new MultiThreadedHttpClient(httpClientConfig);
         String username = StringUtil.validate("username", map.get("username"));
         String password = StringUtil.validate("password", map.get("password"));
         httpClient.getCredentialsProvider().setCredentials(
                 new AuthScope(spaceURI.getHost(), port),
                 new UsernamePasswordCredentials(username, password));
     }
 
     @Override
     public void listObjects(ObjectQuery query, ObjectListHandler handler) {
         String type = query.getType();
         if (type.equals("pidPattern")) {
             listObjects(new PIDPatternFilter(query.getPidPattern()), handler);
         } else if (type.equals("pidList")) {
             listObjects(query.getPidList().iterator(), handler);
         } else {
             throw new UnsupportedOperationException("DuraCloud does not support " + query.getQueryType() + " queries.");
         }
     }
 
     @Override
     protected boolean hasObject(String pid) {
         return headCheck(httpClient, getContentURI(pid));
     }
 
     @Override
     public FedoraObject getObject(String pid) {
         InputStream in = getStream(httpClient, getContentURI(pid));
         if (in == null) return null;
         try {
             return new FOXMLReader().readObject(in);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public boolean putObject(FedoraObject o,
                              StoreConnector source,
                              boolean overwrite,
                              boolean copyExternal,
                              boolean copyRedirect) {
         boolean existed = hasObject(o.pid());
         if (existed) {
             if (!overwrite) {
                 return existed;
             }
         }
         FOXMLWriter writer = new FOXMLWriter();
         File tempFile = null;
         OutputStream out = null;
         try {
             // convert E/R datastreams to managed, if needed
             for (Datastream ds: o.datastreams().values()) {
                 ControlGroup c = ds.controlGroup();
                 if ((c.equals(ControlGroup.EXTERNAL) && copyExternal)
                         || (c.equals(ControlGroup.REDIRECT) && copyRedirect)) {
                     ds.controlGroup(ControlGroup.MANAGED);
                 }
             }
             // write foxml to temp file
             tempFile = File.createTempFile("cloudsync", null);
             out = new FileOutputStream(tempFile);
             writer.writeObject(o, out);
             out.close();
             // upload managed datastream content and foxml to DuraCloud
             putObject(o, source, tempFile);
             return existed;
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             IOUtils.closeQuietly(out);
             if (tempFile != null) {
                 tempFile.delete();
             }
             writer.close();
         }
     }
    
     private void putObject(FedoraObject o,
                            StoreConnector source,
                            File foxmlFile) throws IOException {
         boolean success = false;
         Set<String> uris = new HashSet<String>();
         try {
             String oURI = getContentURI(o.pid());
             uris.add(oURI);
             put(httpClient, oURI, foxmlFile, "application/xml");
             for (Datastream ds: o.datastreams().values()) {
                 if (ds.controlGroup().equals(ControlGroup.MANAGED)) {
                     putVersions(o, ds, source, uris);
                 }
             }
             success = true;
         } finally {
             if (!success) {
                 logger.info("Cleaning up after failure to put {}", o.pid());
                 for (String uri: uris) {
                     try {
                         delete(httpClient, uri);
                     } catch (Exception e) {
                         logger.warn("Error cleaning up " + uri, e);
                     }
                 }
             }
         }
     }
     
     private void putVersions(FedoraObject o,
                              Datastream ds,
                              StoreConnector source,
                              Set<String> urls) throws IOException {
         for (DatastreamVersion dsv: ds.versions()) {
             InputStream in = source.getContent(o,  ds, dsv);
             File tempFile = copyToTempFile(in);
 
             try {
                 String contentId = o.pid() + "+" + ds.id() + "+" + dsv.id();
                 putChunks(tempFile, contentId, dsv.mimeType(), urls);
 
             } finally {
                 // finally, delete the local copy
                 if (!tempFile.delete()) {
                     logger.warn("Failed to delete temporary file {}", tempFile);
                 }
             }
         }
     }
 
     private void putChunks(File tempFile,
                            String contentId,
                            String mimeType,
                            Set<String> urls) throws IOException {
         logger.debug("Uploading content: {}", contentId);
 
         // Send to remote location if the file is small enough
         if (tempFile.length() <= MAX_CONTENT_CHUNK_SIZE) {
             putFile(tempFile, contentId, mimeType, urls);
 
         } else {
             // Split into chunks before sending, since file is too large
             FileInputStream inputStream = new FileInputStream(tempFile);
             ChunkableContent chunkable = new ChunkableContent(contentId,
                                                               mimeType,
                                                               inputStream,
                                                               tempFile.length(),
                                                               MAX_CONTENT_CHUNK_SIZE);
 
             // Send each chunk
             for (ChunkInputStream chunk : chunkable) {
                 putStream(chunk, chunk.getChunkId(), chunk.getMimetype(), urls);
             }
 
             // Send the chunks manifest
             ChunksManifest manifest = chunkable.finalizeManifest();
             putStream(manifest.getBody(),
                       manifest.getManifestId(),
                       manifest.getMimetype(),
                       urls);
         }
     }
 
     private void putFile(File file,
                          String contentId,
                          String mimeType,
                          Set<String> urls) {
         logger.debug("Uploading content: {}", contentId);
 
         String url = getContentURI(contentId);
        int sleep = 15000;
        for (int i = 0; ; i ++) {
            try {
                put(httpClient, url, file, mimeType);
                break;
            } catch (RuntimeException ex) {
                if (i < 5) {
                    logger.info("Exception while putting file (attempt " + (i + 1) + " of 5)", ex);
                    try {
                        logger.debug("Sleeping " + sleep + "ms. before retry...");
                        Thread.sleep(sleep);
                    } catch (InterruptedException iex) {
                        logger.debug("Sleep interrupted!");
                    }
                    sleep *= 5;
                } else {
                    // no more retries left
                    throw ex;
                }
            }
        }
         urls.add(url);
     }
 
     private void putStream(InputStream inputStream,
                            String contentId,
                            String mimeType,
                            Set<String> urls) throws IOException {
         // Copy content to local temporary file first
         File file = copyToTempFile(inputStream);
         try {
             // Then send it to the remote destination
             putFile(file, contentId, mimeType, urls);
 
         } finally {
             // finally, delete the local copy
             if (!file.delete()) {
                 logger.warn("Failed to delete temporary file {}", file);
             }
         }
     }
 
     private File copyToTempFile(InputStream in) throws IOException{
         File file = File.createTempFile("cloudsync", null);
         OutputStream out = new FileOutputStream(file);
 
         try {
             IOUtils.copy(in, out);
         } finally {
             IOUtils.closeQuietly(in);
             IOUtils.closeQuietly(out);
         }
         return file;
     }
 
     private String getContentURI(String contentId) {
         StringBuilder s = new StringBuilder();
         s.append(spaceURI.toString());
         s.append('/');
         if (prefix != null) {
             s.append(prefix);
         }
         s.append(contentId);
         s.append("?storeID=");
         s.append(providerId);
         return s.toString();
     }
 
     @Override
     public InputStream getContent(FedoraObject o, Datastream ds, DatastreamVersion dsv) {
         String contentId = o.pid() + "+" + ds.id() + "+" + dsv.id();
         String url = getContentURI(contentId);
 
         // Try to retrieve simple content
         InputStream content = getStream(httpClient, url);
         if (null != content) {
             logger.debug("Returning simple content: {}", contentId);
             return content;
         }
 
         // Content does not exist, see if it was chunked
         String manifestId = contentId + ChunksManifest.manifestSuffix;
         String manifestUrl = getContentURI(manifestId);
 
         InputStream manifestStream = getStream(httpClient, manifestUrl);
         if (null == manifestStream) {
             // No manifest, no chunks
             logger.info("No content found for: {}", contentId);
             return null;
         }
 
         // Deserialize manifest
         ChunksManifest manifest = null;
         try {
             manifest =
                 ManifestDocumentBinding.createManifestFrom(manifestStream);
         } catch (Exception e) {
             String msg = "Error deserializing manifest!";
             logger.error(msg);
         }
 
         InputStream result = null;
         if (null != manifest) {
             logger.debug("Reading chunks from manifest: {}", manifestId);
 
             // Sort chunks by their index
             Map<Integer, String> sortedChunks = new TreeMap<Integer, String>();
             for (ChunksManifestBean.ManifestEntry entry : manifest.getEntries()) {
                 sortedChunks.put(entry.getIndex(), entry.getChunkId());
             }
 
             // Collect ordered sequence of chunks
             List<String> chunks = new ArrayList<String>();
             for (String chunkId : sortedChunks.values()) {
                 chunks.add(chunkId);
             }
 
             if (chunks.size() == 0) {
                 String msg = "No chunks found in: " + manifestId + "!";
                 logger.error(msg);
                 result = null;
             } else {
                 result = new MultiChunkInputStream(this, chunks);
             }
         }
         return result;
     }
 
     public InputStream getStream(String contentId) {
         logger.debug("Downloading content: {}", contentId);
 
         String url = getContentURI(contentId);
         return super.getStream(httpClient, url);
     }
 
     @Override
     public void close() {
         httpClient.close();
     }
 
     private void listObjects(Filter<String> filter, ObjectListHandler handler) {
         boolean keepGoing = true;
         boolean moreChunks = true;
         String marker = null;
         while (moreChunks && keepGoing) {
             String lastItemId = null;
             int chunkSize = 0;
             for (String itemId: getNextChunk(marker, CHUNKSIZE)) {
                 String pid;
                 if (prefix == null) {
                     pid = itemId;
                 } else {
                     pid = itemId.substring(prefix.length());
                 }
                 try {
                     if (filter.accept(pid) != null) {
                         ObjectInfo o = new ObjectInfo();
                         o.setPid(pid);
                         keepGoing = handler.handleObject(o);
                     }
                 } catch (IOException wontHappen) {
                     throw new RuntimeException(wontHappen);
                 }
                 lastItemId = itemId;
                 chunkSize++;
             }
             if (chunkSize == CHUNKSIZE) {
                 marker = lastItemId;
             } else {
                 moreChunks = false;
             }
         }
     }
 
     private List<String> getNextChunk(String marker, int maxResults) {
         String url = spaceURI.toString() + "?storeID=" + providerId
                 + "&maxResults=" + maxResults;
         if (marker != null) {
             url += "&marker=" + marker;
         }
         if (prefix != null) {
             url += "&prefix=" + prefix;
         }
         List<String> list = new ArrayList<String>();
         try {
             Document doc = parseXML(getString(httpClient, url));
             Node root = doc.getDocumentElement();
             NodeList itemNodes = root.getChildNodes();
             for (int i = 0; i < itemNodes.getLength(); i++) {
                 Node itemNode = itemNodes.item(i);
                 if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                     list.add(itemNode.getTextContent().trim());
                 }
             }
             return list;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static Document parseXML(String xmlString)
             throws ParserConfigurationException, IOException, SAXException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = factory.newDocumentBuilder();
         InputSource inStream = new InputSource();
         inStream.setCharacterStream(new StringReader(xmlString));
         return db.parse(inStream);
     }
 
 }
