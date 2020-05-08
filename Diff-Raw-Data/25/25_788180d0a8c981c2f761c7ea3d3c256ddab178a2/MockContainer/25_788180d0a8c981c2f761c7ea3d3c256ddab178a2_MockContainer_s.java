 package eu.scapeproject;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.SerializationException;
 import org.purl.dc.elements._1.ElementContainer;
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.core.Container;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.scapeproject.model.BitStream;
 import eu.scapeproject.model.File;
 import eu.scapeproject.model.Identifier;
 import eu.scapeproject.model.IntellectualEntity;
 import eu.scapeproject.model.LifecycleState;
 import eu.scapeproject.model.LifecycleState.State;
 import eu.scapeproject.model.Representation;
 import eu.scapeproject.util.ONBConverter;
 import eu.scapeproject.util.ScapeMarshaller;
 import gov.loc.marc21.slim.RecordType;
 
 public class MockContainer implements Container {
 
     private static final Logger LOG = LoggerFactory.getLogger(MockContainer.class);
 
     private final PosixStorage storage;
     private final LuceneIndex index;
     private final int asyncIngestDelay = 1000;
     private final Map<String, Object> technicalMetadata = new HashMap<String, Object>();
     private final Map<String, Object> sourceMetadata = new HashMap<String, Object>();
     private final Map<String, Object> provenanceMetadata = new HashMap<String, Object>();
     private final Map<String, Object> rightsMetadata = new HashMap<String, Object>();
     private final Map<String, Object> descriptiveMetadata = new HashMap<String, Object>();
     private final Map<String, String> fileIdMap = new HashMap<String, String>();
     private final Map<String, String> bitstreamIdMap = new HashMap<String, String>();
     private final Map<String, String> representationIdMap = new HashMap<String, String>();
     private final Map<Long, Object> asyncIngestMap = new HashMap<Long, Object>();
     private final AsyncIngester asyncIngester = new AsyncIngester();
     private final int port;
     private final ScapeMarshaller marshaller;
 
     private Thread asyncIngesterThread = new Thread();
 
     public MockContainer(String path, int port) throws JAXBException {
         this.storage = new PosixStorage(path);
         this.index = new LuceneIndex();
         this.port = port;
         this.marshaller = ScapeMarshaller.newInstance();
 
         
     }
 
     public void close() throws Exception {
         this.asyncIngester.stop();
         this.asyncIngesterThread.join();
         this.purgeStorage();
         this.index.close();
     }
 
     private Object getBitStream(String bsId, IntellectualEntity entity) {
         for (Representation r : entity.getRepresentations()) {
             for (File f : r.getFiles()) {
                 if (f.getBitStreams() != null) {
                     for (BitStream bs : f.getBitStreams()) {
                         if (bs.getIdentifier().getValue().equals(bsId)) {
                             return bs;
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     private File getFile(String id, IntellectualEntity entity) {
         for (Representation r : entity.getRepresentations()) {
             for (File f : r.getFiles()) {
                 if (f.getIdentifier().getValue().equals(id)) {
                     return f;
                 }
             }
         }
         return null;
     }
 
     private Integer getVersionFromPath(String path) {
         // check for a version parameter or use the latest version
         Matcher m = Pattern.compile("/\\d*/").matcher(path);
         if (m.find()) {
             return Integer.parseInt(path.substring(m.start() + 1, m.end() - 1));
         } else {
             return null;
         }
     }
 
     public void handle(Request req, Response resp) {
         try {
             if (req.getMethod().equals("POST")) {
                 handlePost(req, resp);
             } else if (req.getMethod().equals("DELETE")) {
 
             } else if (req.getMethod().equals("PUT")) {
                 handlePut(req, resp);
             } else if (req.getMethod().equals("GET")) {
                 handleGet(req, resp);
             } else {
                 LOG.error("Unable to handle method of type " + req.getMethod());
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void handleAsyncIngest(Request req, Response resp, int okValue) throws Exception {
         // get entity from request body
         IntellectualEntity.Builder entityBuilder = new IntellectualEntity.Builder(marshaller.deserialize(
                 IntellectualEntity.class, req.getInputStream()));
         entityBuilder.lifecycleState(new LifecycleState("async ingest", State.INGESTING));
         IntellectualEntity entity = entityBuilder.build();
 
         // add the entity to the async map, for later ingestion,
         // and set the the time the ingestion should be processed by the mock
         long time = new Date().getTime();
         do {
             time += new Random().nextInt(asyncIngestDelay) + 1000;
         } while (asyncIngestMap.containsKey(time));
         asyncIngestMap.put(time, entity);
 
         // have to check for id existence and generate some if necessary
         if (entity.getIdentifier() == null) {
             entityBuilder.identifier(new Identifier(UUID.randomUUID().toString()));
         }
 
         // return the identity to get the lifecyclestate
         marshaller.serialize(entity.getIdentifier(), resp.getOutputStream());
         resp.setCode(okValue);
     }
 
     private void handleEntitySRU(Request req, Response resp) throws Exception {
         // String term = req.getParameter("query");
         // List<MetsDocument> entities = new ArrayList<MetsDocument>();
         // for (String id : index.searchEntity(term)) {
         // byte[] xml = storage.getXML(id);
         // entities.add((MetsDocument)
         // marshaller.getJaxbUnmarshaller().unmarshal(new
         // ByteArrayInputStream(xml)));
         // }
         // IntellectualEntityCollection coll = new
         // IntellectualEntityCollection(entities);
         // marshaller.getJaxbMarshaller().marshal(coll, resp.getOutputStream());
         // resp.setCode(200);
     }
 
     private void handleGet(Request req, Response resp) throws IOException {
         String contextPath = req.getPath().getPath();
         LOG.info("-- HTTP/1.1 GET " + contextPath + " from " + req.getClientAddress().getAddress().getHostAddress());
         try {
             if (contextPath.startsWith("/entity/")) {
                 handleRetrieveEntity(req, resp);
             } else if (contextPath.startsWith("/metadata/")) {
                 handleRetrieveMetadata(req, resp);
             } else if (contextPath.startsWith("/representation/")) {
                 handleRetrieveRepresentation(req, resp);
             } else if (contextPath.startsWith("/entity-version-list/")) {
                 handleRetrieveVersionList(req, resp);
             } else if (contextPath.startsWith("/sru/entities")) {
                 handleEntitySRU(req, resp);
             } else if (contextPath.startsWith("/sru/representations")) {
                 handleRepresentationSRU(req, resp);
             } else if (contextPath.startsWith("/file/")) {
                 handleRetrieveFile(req, resp);
             } else if (contextPath.startsWith("/bitstream/")) {
                 handleRetrieveBitStream(req, resp);
             } else if (contextPath.startsWith("/lifecycle/")) {
                 handleRetrieveLifecycleState(req, resp);
             } else {
                 resp.setCode(404);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             resp.close();
         }
     }
 
     private void handleIngest(Request req, Response resp, int okValue) throws Exception {
         try {
             IntellectualEntity ent = marshaller.deserialize(
                     IntellectualEntity.class, req.getInputStream());
             IntellectualEntity.Builder entityBuilder = new IntellectualEntity.Builder(ent);
             entityBuilder.lifecycleState(new LifecycleState("", State.INGESTED));
             IntellectualEntity entity = entityBuilder.build();
 
             // have to check for id existence and generate some if necessary
             if (entity.getIdentifier() == null || entity.getIdentifier().getValue() == null) {
                 entityBuilder.identifier(new Identifier(UUID.randomUUID().toString()));
                 entity = entityBuilder.build();
             }
 
             ingestEntity(entity);
             index.addEntity(entity);
 
             // generate the server response with the ingested entity's id
             resp.setCode(okValue);
             resp.set("Content-Type", "text/plain");
             IOUtils.copy(new ByteArrayInputStream(entity.getIdentifier().getValue().getBytes()), resp.getOutputStream());
         } catch (IOException e) {
             resp.setCode(500);
             throw new SerializationException(e);
         }
     }
 
     private void handlePost(Request req, Response resp) throws IOException {
         String contextPath = req.getPath().getPath();
         LOG.info("-- HTTP/1.1 POST " + contextPath + " from " + req.getClientAddress().getAddress().getHostAddress());
         try {
             if (contextPath.startsWith("/entity-async")) {
                 handleAsyncIngest(req, resp, 200);
             } else if (contextPath.equals("/entity")) {
                 handleIngest(req, resp, 201);
             } else if (contextPath.startsWith("/entity-list")) {
                 handleRetrieveEntityList(req, resp);
             } else {
                 resp.setCode(404);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             resp.close();
         }
     }
 
     private void handlePut(Request req, Response resp) throws IOException {
         String contextPath = req.getPath().getPath();
         LOG.info("-- HTTP/1.1 PUT " + contextPath + " from " + req.getClientAddress().getAddress().getHostAddress());
         try {
             if (contextPath.startsWith("/entity/")) {
                 handleUpdateEntity(req, resp);
             } else if (contextPath.startsWith("/representation/")) {
                 handleUpdateRepresentation(req, resp);
             } else if (contextPath.startsWith("/metadata/")) {
                 handleUpdateMetadata(req, resp);
             } else {
                 resp.setCode(404);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             resp.close();
         }
 
     }
 
     private void handleRepresentationSRU(Request req, Response resp) throws Exception {
         // String term = req.getParameter("query");
         // List<MetsDocument> entities = new ArrayList<MetsDocument>();
         // for (String id : index.searchRepresentation(term)) {
         // byte[] xml = storage.getXML(representationIdMap.get(id));
         // entities.add((MetsDocument)
         // marshaller.getJaxbUnmarshaller().unmarshal(new
         // ByteArrayInputStream(xml)));
         // }
         // IntellectualEntityCollection coll = new
         // IntellectualEntityCollection(entities);
         // marshaller.getJaxbMarshaller().marshal(coll, resp.getOutputStream());
         // resp.setCode(200);
     }
 
     private void handleRetrieveBitStream(Request req, Response resp) throws Exception {
         String bsId = req.getPath().getPath().substring(11);
         String entityId = bitstreamIdMap.get(bsId);
         if (entityId == null) {
             resp.setCode(404);
         } else {
             byte[] blob = storage.getXML(entityId, getVersionFromPath(req.getPath().getPath()));
             IntellectualEntity entity = marshaller.deserialize(IntellectualEntity.class,
                     new ByteArrayInputStream(blob));
             marshaller.serialize(getBitStream(bsId, entity), resp.getOutputStream());
             resp.setCode(200);
         }
     }
 
     private void handleRetrieveEntity(Request req, Response resp) throws Exception {
         String id = req.getPath().getPath().substring(req.getPath().getPath().lastIndexOf('/') + 1);
 
         try {
             byte[] blob = storage.getXML(id, getVersionFromPath(req.getPath().getPath()));
             IOUtils.write(blob, resp.getOutputStream());
             resp.setCode(200);
             resp.close();
         } catch (FileNotFoundException e) {
             resp.setCode(404);
         } finally {
             resp.close();
         }
     }
 
     private void handleRetrieveEntityList(Request req, Response resp) throws Exception {
         // String[] uris = req.getContent().split("\\n");
         // List<MetsDocument> docs = new ArrayList<MetsDocument>();
         // for (String uri : uris) {
         // byte[] blob = storage.getXML(uri);
         // docs.add((MetsDocument)
         // marshaller.getJaxbUnmarshaller().unmarshal(new
         // ByteArrayInputStream(blob)));
         // }
         // IntellectualEntityCollection entities = new
         // IntellectualEntityCollection(docs);
         // marshaller.getJaxbMarshaller().marshal(entities,
         // resp.getOutputStream());
         // resp.setCode(200);
     }
 
     private void handleRetrieveFile(Request req, Response resp) throws Exception {
         String fileId = req.getPath().getPath().substring(6);
         String entityIdid = fileIdMap.get(fileId);
         byte[] blob = storage.getXML(entityIdid, getVersionFromPath(req.getPath().getPath()));
         if (entityIdid == null) {
             resp.setCode(404);
         } else {
             IntellectualEntity entity = marshaller.deserialize(IntellectualEntity.class,
                     new ByteArrayInputStream(blob));
             marshaller.serialize(getFile(fileId, entity), resp.getOutputStream());
             resp.setCode(200);
         }
     }
 
     private void handleRetrieveLifecycleState(Request req, Response resp) throws Exception {
         String id = req.getPath().getPath().substring(11);
         Integer version = getVersionFromPath(req.getPath().getPath());
         if (storage.exists(id, version)) {
             IntellectualEntity entity = marshaller.deserialize(IntellectualEntity.class,
                     new ByteArrayInputStream(storage.getXML(id, version)));
             marshaller.serialize(entity.getLifecycleState(), resp.getOutputStream());
             return;
         }
         for (Entry<Long, Object> entry : asyncIngestMap.entrySet()) {
             if (entry.getValue() instanceof IntellectualEntity) {
                 IntellectualEntity ent = (IntellectualEntity) entry.getValue();
                 if (ent.getIdentifier().getValue().equals(id)) {
                     marshaller.serialize(ent.getLifecycleState(), resp.getOutputStream());
                     return;
                 }
             }
         }
 
     }
 
     private void handleRetrieveMetadata(Request req, Response resp) throws Exception {
         // String id =
         // req.getPath().getPath().substring(req.getPath().getPath().lastIndexOf('/')
         // + 1);
         // byte[] blob = storage.getXML(metadataIdMap.get(id),
         // getVersionFromPath(req.getPath().getPath()));
         //
         // try {
         // IntellectualEntity e =
         // marshaller.deserialize(IntellectualEntity.class, new
         // ByteArrayInputStream(blob));
         // DCMetadata dc = (DCMetadata) e.getDescriptive();
         // resp.set("Content-Type", "text/xml");
         // marshaller.getJaxbMarshaller().marshal(MetsUtil.convertDCMetadata(dc),
         // resp.getOutputStream());
         // resp.setCode(200);
         // } catch (FileNotFoundException e) {
         // resp.setCode(404);
         // }
     }
 
     private void handleRetrieveRepresentation(Request req, Response resp) throws Exception {
         String id = req.getPath().getPath().substring(req.getPath().getPath().lastIndexOf('/') + 1);
         try {
             byte[] blob = storage.getXML(representationIdMap.get(id), getVersionFromPath(req.getPath().getPath()));
             IOUtils.write(blob, resp.getOutputStream());
             resp.setCode(200);
             resp.close();
         } catch (FileNotFoundException e) {
             resp.setCode(404);
         } finally {
             resp.close();
         }
     }
 
     private void handleRetrieveVersionList(Request req, Response resp) throws Exception {
        // String id = req.getPath().getPath().substring(21);
        // List<String> versions = storage.getVersionList(id);
        // Collections.sort(versions);
        // VersionList versionList = new VersionList(id, versions);
        // marshaller.getJaxbMarshaller().marshal(versionList,
        // resp.getOutputStream());
        // resp.setCode(200);
     }
 
     private void handleUpdateEntity(Request req, Response resp) throws Exception {
         try {
             handleIngest(req, resp, 200);
         } finally {
             resp.close();
         }
     }
 
     private void handleUpdateMetadata(Request req, Response resp) throws Exception {
         // MetsMetadata data = marshaller.deserialize(MetsMetadata.class,
         // req.getInputStream());
         // String entityId = metadataIdMap.get(data.getId());
         // byte[] blob = storage.getXML(entityId);
         // IntellectualEntity oldVersion =
         // marshaller.deserialize(IntellectualEntity.class, new
         // ByteArrayInputStream(blob));
         // IntellectualEntity.Builder newVersion = new
         // IntellectualEntity.Builder(oldVersion);
         // if (data instanceof DCMetadata) {
         // newVersion.descriptive((DescriptiveMetadata) data);
         // }
         // ByteArrayOutputStream bos = new ByteArrayOutputStream();
         // marshaller.serialize(newVersion.build(), bos);
         // storage.saveXML(bos.toByteArray(), entityId,
         // storage.getNewVersionNumber(entityId), false);
     }
 
     private void handleUpdateRepresentation(Request req, Response resp) throws Exception {
         try {
             Representation newRep = marshaller.deserialize(Representation.class, req.getInputStream());
             byte[] blob = storage.getXML(representationIdMap.get(newRep.getIdentifier().getValue()));
             IntellectualEntity ie = marshaller.deserialize(IntellectualEntity.class, new ByteArrayInputStream(blob));
             List<Representation> newRepresentations = new ArrayList<Representation>(ie.getRepresentations().size());
             for (Representation orig : ie.getRepresentations()) {
                 if (orig.getIdentifier().getValue().equals(newRep.getIdentifier().getValue())) {
                     newRepresentations.add(newRep);
                 } else {
                     newRepresentations.add(orig);
                 }
             }
             IntellectualEntity newVersion = new IntellectualEntity.Builder(ie)
                     .representations(newRepresentations)
                     .build();
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             marshaller.serialize(newVersion, bos);
             storage.saveXML(bos.toByteArray(), newVersion.getIdentifier().getValue(), storage.getNewVersionNumber(newVersion.getIdentifier().getValue()), false);
             LOG.debug("updated representation " + newRep.getIdentifier().getValue() + " of intellectual entity " + newVersion.getIdentifier().getValue());
             index.addRepresentation(newRep);
             resp.setCode(200);
         } catch (Exception e) {
             resp.setCode(500);
         }
     }
 
     private void ingestEntity(IntellectualEntity entity) throws Exception {
         IntellectualEntity.Builder entityBuilder = new IntellectualEntity.Builder(entity);
         if (entity.getDescriptive() != null) {
             this.descriptiveMetadata.put(entity.getIdentifier().getValue(), entity.getDescriptive());
         }
         // add the lifecycle state
         entityBuilder.lifecycleState(new LifecycleState("ingested", State.INGESTED));
 
         // save the identifiers to the according maps
         // and check that all objects have identifiers
         if (entity.getRepresentations() != null) {
             List<Representation> representationsCopy = new ArrayList<Representation>();
             for (Representation r : entity.getRepresentations()) {
                 Representation.Builder repCopyBuilder = new Representation.Builder(r)
                         .identifier((r.getIdentifier() == null) ? new Identifier(UUID.randomUUID().toString()) : r.getIdentifier());
                 if (r.getFiles() != null) {
                     repCopyBuilder.files(null);
                     List<File> fList = new ArrayList<File>();
                     for (File f : r.getFiles()) {
                         File.Builder fileCopyBuilder = new File.Builder(f)
                                 .identifier((f.getIdentifier() == null) ? new Identifier(UUID.randomUUID().toString()) : f.getIdentifier());
                         if (f.getBitStreams() != null) {
                             fileCopyBuilder.bitStreams(null);
                             List<BitStream> bsList = new ArrayList<BitStream>();
                             for (BitStream bs : f.getBitStreams()) {
                                 BitStream bsCopy = new BitStream.Builder(bs)
                                         .identifier((bs.getIdentifier() == null) ? new Identifier(UUID.randomUUID().toString()) : bs.getIdentifier())
                                         .build();
                                 bitstreamIdMap.put(bsCopy.getIdentifier().getValue(), entity.getIdentifier().getValue());
                                 bsList.add(bsCopy);
                                 fileCopyBuilder.bitStreams(bsList);
                             }
                         }
                         File fileCopy = fileCopyBuilder.build();
                         fileIdMap.put(fileCopy.getIdentifier().getValue(), entity.getIdentifier().getValue());                        
                         fList.add(fileCopy);
                         repCopyBuilder.files(fList);
                     }
                 }
                 Representation repCopy = repCopyBuilder.build();
                 representationsCopy.add(repCopy);
                 representationIdMap.put(repCopy.getIdentifier().getValue(), entity.getIdentifier().getValue());
             }
             entityBuilder.representations(representationsCopy);
         }
 
         // now build the entity again with proper identifiers
         entity = entityBuilder.build();
 
         // save the data in the storage backend
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         marshaller.serialize(entity, bos);
         int version = entity.getVersionNumber();
         if (storage.exists(entity.getIdentifier().getValue(), entity.getVersionNumber())) {
             version = storage.getNewVersionNumber(entity.getIdentifier().getValue());
         }
         storage.saveXML(bos.toByteArray(), entity.getIdentifier().getValue(), version, false);
 
         // update the hashmap with the metadata references to the entities
         LOG.debug("++ adding descriptive metadata for entity " + entity.getIdentifier().getValue());
     }
 
     private String extractId(Object descriptive) {
         if (descriptive instanceof ElementContainer) {
 
         } else if (descriptive instanceof RecordType) {
             /* extract the id from the MARC record set */
         }
         return null;
     }
 
     public void ingestObject(Object value) throws Exception {
         if (value instanceof IntellectualEntity) {
             IntellectualEntity entity = (IntellectualEntity) value;
             ingestEntity(entity);
         }
     }
 
     public void purgeStorage() throws Exception {
         storage.purge();
     }
 
     public void start() {
         this.asyncIngesterThread = new Thread(asyncIngester);
         this.asyncIngesterThread.start();
     }
 
     public class AsyncIngester implements Runnable {
         private boolean stop = false;
 
         public void run() {
             while (!stop) {
                 for (Entry<Long, Object> asyncRequest : asyncIngestMap.entrySet()) {
                     if (new Date().getTime() >= asyncRequest.getKey()) {
                         try {
                             LOG.info("ingesting object at " + asyncRequest.getKey());
                             ingestObject(asyncRequest.getValue());
                         } catch (Exception e) {
                             e.printStackTrace();
                         } finally {
                             asyncIngestMap.remove(asyncRequest.getKey());
                         }
                     }
                 }
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     stop = true;
                 }
             }
         }
 
         public synchronized void stop() {
             stop = true;
         };
     }
 }
