 /*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package eu.scape_project.service;
 
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_BITSTREAM;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_BITSTREAM_TYPE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_CURRENT_VERSION;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_FILE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_FILENAME;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_INGEST_SOURCE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_ITEM;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_LIFECYCLESTATE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_LIFECYCLESTATE_DETAILS;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_MIMETYPE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_REFERENCED_CONTENT;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_REPRESENTATION;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_SCHEMA;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_TITLE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_TYPE;
 import static eu.scape_project.rdf.ScapeRDFVocabulary.HAS_VERSION;
 import info.lc.xmlns.premis_v2.PremisComplexType;
 import info.lc.xmlns.premis_v2.RightsComplexType;
 import info.lc.xmlns.textmd_v3.TextMD;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import javax.jcr.ItemExistsException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.jcr.query.qom.Constraint;
 import javax.jcr.query.qom.QueryObjectModelFactory;
 import javax.jcr.query.qom.Source;
 import javax.xml.bind.JAXBException;
 
 import org.fcrepo.http.commons.session.SessionFactory;
 import org.fcrepo.kernel.Datastream;
 import org.fcrepo.kernel.FedoraObject;
 import org.fcrepo.kernel.exception.InvalidChecksumException;
 import org.fcrepo.kernel.rdf.SerializationUtils;
 import org.fcrepo.kernel.rdf.impl.DefaultGraphSubjects;
 import org.fcrepo.kernel.services.DatastreamService;
 import org.fcrepo.kernel.services.NodeService;
 import org.fcrepo.kernel.services.ObjectService;
 import org.purl.dc.elements._1.ElementContainer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 
 import com.google.books.gbs.GbsType;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 
 import edu.harvard.hul.ois.xml.ns.fits.fits_output.Fits;
 import eu.scape_project.model.BitStream;
 import eu.scape_project.model.File;
 import eu.scape_project.model.Identifier;
 import eu.scape_project.model.IntellectualEntity;
 import eu.scape_project.model.IntellectualEntityCollection;
 import eu.scape_project.model.LifecycleState;
 import eu.scape_project.model.LifecycleState.State;
 import eu.scape_project.model.Representation;
 import eu.scape_project.model.VersionList;
 import eu.scape_project.rdf.ScapeRDFVocabulary;
 import eu.scape_project.util.ContentTypeInputStream;
 import eu.scape_project.util.ScapeMarshaller;
 import gov.loc.audiomd.AudioType;
 import gov.loc.marc21.slim.RecordType;
 import gov.loc.mix.v20.Mix;
 import gov.loc.videomd.VideoType;
 
 /**
  * Component which does all the interaction with fcrepo4
  *
  * @author frank asseg
  *
  */
 @Component
 public class ConnectorService {
 
     public final static String ENTITY_FOLDER = "objects/scape/entities";
 
     public final static String QUEUE_NODE = "/objects/scape/queue";
 
     public String fedoraUrl;
 
     public boolean referencedContent;
 
     private final ScapeMarshaller marshaller;
 
     private static final Logger LOG = LoggerFactory
             .getLogger(ConnectorService.class);
 
     @Autowired
     private ObjectService objectService;
 
     @Autowired
     private NodeService nodeService;
 
     @Autowired
     private DatastreamService datastreamService;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     private final java.io.File tempDirectory;
 
     public ConnectorService()
             throws JAXBException {
         System.out.println("new instance for marshaller");
         marshaller = ScapeMarshaller.newInstance();
         tempDirectory =
                 new java.io.File(System.getProperty("java.io.tmpdir") +
                         "/scape-connector-queue");
         if (!tempDirectory.exists()) {
             tempDirectory.mkdir();
         }
     }
 
     public String getFedoraUrl() {
         return fedoraUrl;
     }
 
     public void setFedoraUrl(String fedoraUrl) {
         this.fedoraUrl = fedoraUrl;
     }
 
     public boolean isReferencedContent() {
         return referencedContent;
     }
 
     public void setReferencedContent(boolean referencedContent) {
         this.referencedContent = referencedContent;
     }
 
     public IntellectualEntity
             fetchEntity(final Session session, final String id)
                     throws RepositoryException {
         return fetchEntity(session, id, null);
     }
 
     public IntellectualEntity fetchEntity(final Session session,
             final String id, final Integer versionNumber)
             throws RepositoryException {
 
         final IntellectualEntity.Builder ie = new IntellectualEntity.Builder();
         ie.identifier(new Identifier(id));
 
         final String entityPath = "/" + ENTITY_FOLDER + "/" + id;
         final FedoraObject ieObject =
                 this.objectService.getObject(session, entityPath);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String entityUri =
                 subjects.getGraphSubject(ieObject.getNode()).getURI();
 
         final Dataset ds = ieObject.getPropertiesDataset(subjects);
         final Model entityModel = SerializationUtils.unifyDatasetModel(ds);
         String versionPath;
         if (versionNumber != null) {
             versionPath = entityPath + "/version-" + versionNumber;
         } else {
             versionPath = getCurrentVersionPath(entityModel, entityUri);
         }
 
         final FedoraObject versionObject =
                 this.objectService.getObject(session, versionPath);
         final Model versionModel =
                 SerializationUtils.unifyDatasetModel(versionObject
                         .getPropertiesDataset(subjects));
 
         /* fetch the ie's metadata form the repo */
         ie.descriptive(fetchMetadata(session, versionPath + "/DESCRIPTIVE"));
 
         /* find all the representations of this entity */
         final Resource versionResource =
                 versionModel.createResource(subjects.getGraphSubject(
                         versionPath).getURI());
         final List<Representation> reps = new ArrayList<>();
         for (String repUri : getLiteralStrings(versionModel, versionResource,
                 HAS_REPRESENTATION)) {
             reps.add(fetchRepresentation(session, repUri.substring(repUri
                     .indexOf('/'))));
         }
         ie.representations(reps);
 
         /* fetch the lifecycle state */
         final Resource entityResource = versionModel.createResource(entityUri);
         final String state =
                 getFirstLiteralString(entityModel, entityResource,
                         HAS_LIFECYCLESTATE);
         final String details =
                 getFirstLiteralString(entityModel, entityResource,
                         HAS_LIFECYCLESTATE_DETAILS);
         ie.lifecycleState(new LifecycleState(details, LifecycleState.State
                 .valueOf(state)));
 
         return ie.build();
     }
 
     private String getCurrentVersionPath(Model entityModel, String uri)
             throws RepositoryException {
         final Resource parent = entityModel.createResource(uri);
         String version =
                 getResourceFromModel(entityModel, parent, HAS_CURRENT_VERSION);
         return version.substring(version.indexOf('/'));
     }
 
     private String getResourceFromModel(Model model, Resource parent,
             String propertyName) {
         StmtIterator it =
                 model.listStatements(parent,
                         model.createProperty(propertyName), (RDFNode) null);
         String uri = it.next().getResource().getURI();
         return uri;
     }
 
     public BitStream fetchBitStream(final Session session, final String bsUri)
             throws RepositoryException {
         final BitStream.Builder bs = new BitStream.Builder();
         bs.identifier(new Identifier(bsUri
                 .substring(bsUri.lastIndexOf('/') + 1)));
         bs.technical(fetchMetadata(session, bsUri + "/TECHNICAL"));
         return bs.build();
     }
 
     public ContentTypeInputStream fetchBinaryFile(final Session session,
             final String entityId, final String repId, final String fileId,
             final String versionId) throws RepositoryException {
 
         final String entityPath, dsPath;
         if (versionId == null) {
             entityPath = "/" + ENTITY_FOLDER + "/" + entityId;
             final FedoraObject fo =
                     this.objectService.getObject(session, entityPath);
             final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
             final String uri = subjects.getGraphSubject(entityPath).getURI();
             final Model entityModel =
                     SerializationUtils.unifyDatasetModel(fo
                             .getPropertiesDataset(new DefaultGraphSubjects(
                                     session)));
             dsPath =
                     this.getCurrentVersionPath(entityModel, uri) + "/" +
                             repId + "/" + fileId + "/DATA";
         } else {
             entityPath =
                     "/" + ENTITY_FOLDER + "/" + entityId + "/version-" +
                             versionId;
             dsPath = entityPath + "/" + repId + "/" + fileId + "/DATA";
         }
 
         final Datastream ds =
                 this.datastreamService.getDatastream(session, dsPath);
 
         return new ContentTypeInputStream(ds.getMimeType(), ds.getContent());
     }
 
     public File fetchFile(final Session session, final String fileUri)
             throws RepositoryException {
         final File.Builder f = new File.Builder();
         final FedoraObject fileObject =
                 this.objectService.getObject(session, fileUri);
         final Model fileModel =
                 SerializationUtils
                         .unifyDatasetModel(fileObject
                                 .getPropertiesDataset(new DefaultGraphSubjects(
                                         session)));
         final Resource parent =
                 fileModel.createResource(new DefaultGraphSubjects(session)
                         .getGraphSubject(fileObject.getNode()).getURI());
 
         /* fetch and add the properties and metadata from the repo */
         f.technical(fetchMetadata(session, fileUri + "/TECHNICAL"));
         String fileId = fileUri.substring(fileUri.lastIndexOf('/') + 1);
         f.identifier(new Identifier(fileId));
         f.filename(getFirstLiteralString(fileModel, parent, HAS_FILENAME));
         f.mimetype(getFirstLiteralString(fileModel, parent, HAS_MIMETYPE));
         String[] ids = fileUri.split("/");
         if (this.referencedContent) {
             f.uri(URI.create(getFirstLiteralString(fileModel, parent,
                     HAS_REFERENCED_CONTENT)));
         } else {
             f.uri(URI.create(fedoraUrl + "/scape/file/" + ids[ids.length - 4] +
                     "/" + ids[ids.length - 2] + "/" + ids[ids.length - 1]));
         }
         /* discover all the Bistreams and add them to the file */
         final List<BitStream> streams = new ArrayList<>();
         for (String bsUri : getLiteralStrings(fileModel, parent, HAS_BITSTREAM)) {
             streams.add(fetchBitStream(session, bsUri.substring(bsUri
                     .indexOf('/'))));
         }
         f.bitStreams(streams);
 
         return f.build();
     }
 
     public Object
             fetchCurrentMetadata(final Session session, final String path)
                     throws RepositoryException {
 
         String[] ids = path.substring(ENTITY_FOLDER.length() + 2).split("/");
         String entityPath = "/" + ENTITY_FOLDER + "/" + ids[0];
         final FedoraObject entityObject =
                 objectService.getObject(session, entityPath);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri = subjects.getGraphSubject(entityObject.getNode()).getURI();
 
         StringBuilder versionPath = new StringBuilder();
         versionPath.append(this.getCurrentVersionPath(
                 SerializationUtils
                         .unifyDatasetModel(entityObject
                                 .getPropertiesDataset(subjects)), uri));
         for (int i = 1; i < ids.length; i++) {
             versionPath.append("/");
             versionPath.append(ids[i]);
         }
 
         try {
             if (!this.datastreamService.exists(session, versionPath.toString())) {
                 throw new PathNotFoundException("No metadata available for " +
                         path);
             }
             final Datastream mdDs =
                     this.datastreamService.getDatastream(session, versionPath
                             .toString());
             return this.marshaller.deserialize(mdDs.getContent());
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
     }
 
     public Object fetchMetadata(final Session session, final String path)
             throws RepositoryException {
 
         try {
             if (!this.datastreamService.exists(session, path)) {
                 return null;
             }
             final Datastream mdDs =
                     this.datastreamService.getDatastream(session, path);
             return this.marshaller.deserialize(mdDs.getContent());
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
     }
 
     public Representation fetchRepresentation(final Session session,
             final String repPath) throws RepositoryException {
         final Representation.Builder rep = new Representation.Builder();
         final FedoraObject repObject =
                 this.objectService.getObject(session, repPath);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri =
                 subjects.getGraphSubject(repObject.getNode()).getURI();
         final Model repModel =
                 SerializationUtils.unifyDatasetModel(repObject
                         .getPropertiesDataset(subjects));
         final Resource parent = repModel.createResource(uri);
 
         /* find the title and id */
         rep.identifier(new Identifier(repPath.substring(repPath
                 .lastIndexOf('/') + 1)));
         rep.title(getFirstLiteralString(repModel, parent, HAS_TITLE));
 
         /* find and add the metadata */
         rep.technical(fetchMetadata(session, repObject.getPath() + "/TECHNICAL"));
         rep.source(fetchMetadata(session, repObject.getPath() + "/SOURCE"));
         rep.provenance(fetchMetadata(session, repObject.getPath() +
                 "/PROVENANCE"));
         rep.rights(fetchMetadata(session, repObject.getPath() + "/RIGHTS"));
 
         /* add the individual files */
         final List<File> files = new ArrayList<>();
         for (String fileUri : getLiteralStrings(repModel, parent, HAS_FILE)) {
             files.add(fetchFile(session, fileUri
                     .substring(fileUri.indexOf('/'))));
         }
 
         rep.files(files);
         return rep.build();
     }
 
     public Representation fetchRepresentation(final Session session,
             final String entityId, String repId, Integer versionId)
             throws RepositoryException {
 
         String entityPath, repPath;
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         if (versionId == null) {
             entityPath = "/" + ENTITY_FOLDER + "/" + entityId;
             final FedoraObject fo =
                     this.objectService.getObject(session, entityPath);
             final String uri = subjects.getGraphSubject(fo.getNode()).getURI();
             final Model entityModel =
                     SerializationUtils.unifyDatasetModel(fo
                             .getPropertiesDataset(subjects));
             repPath =
                     this.getCurrentVersionPath(entityModel, uri) + "/" +
                             repId;
         } else {
             entityPath =
                     "/" + ENTITY_FOLDER + "/" + entityId + "/version-" +
                             versionId;
             repPath = entityPath + "/" + repId;
         }
 
         return this.fetchRepresentation(session, repPath);
     }
 
     public VersionList fetchVersionList(final Session session,
             final String entityId) throws RepositoryException {
         final String entityPath = "/" + ENTITY_FOLDER + "/" + entityId;
         final FedoraObject entityObject =
                 this.objectService.getObject(session, entityPath);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri =
                 subjects.getGraphSubject(entityObject.getNode()).getURI();
         final Model model =
                 SerializationUtils.unifyDatasetModel(entityObject
                         .getPropertiesDataset(subjects));
         final Resource subject = model.createResource(uri);
         return new VersionList(entityId, getLiteralStrings(model, subject,
                 HAS_VERSION));
     }
 
     public String addEntity(final Session session, final InputStream src)
             throws RepositoryException {
         return addEntity(session, src, null);
     }
 
     public String addEntity(final Session session, final InputStream src,
             String entityId) throws RepositoryException {
         try {
             /* read the post body into an IntellectualEntity object */
             final IntellectualEntity ie =
                     this.marshaller.deserialize(IntellectualEntity.class, src);
             final StringBuilder sparql = new StringBuilder();
 
             if (entityId == null) {
                 if (ie.getIdentifier() != null) {
                     entityId = ie.getIdentifier().getValue();
                 } else {
                     entityId = UUID.randomUUID().toString();
                 }
 
             }
             /* create the entity top level object in fcrepo as a first version */
             final String entityPath = ENTITY_FOLDER + "/" + entityId;
             final String versionPath = entityPath + "/version-1";
 
             if (this.objectService.exists(session, "/" + entityPath)) {
                 /* return a 409: Conflict result */
                 throw new ItemExistsException("Entity '" + entityId +
                         "' already exists");
             }
 
             final FedoraObject entityObject =
                     objectService.createObject(session, entityPath);
             entityObject.getNode().addMixin("scape:intellectual-entity");
 
             final FedoraObject versionObject =
                     objectService.createObject(session, versionPath);
             versionObject.getNode().addMixin(
                     "scape:intellectual-entity-version");
 
             /* add the metadata datastream for descriptive metadata */
             if (ie.getDescriptive() != null) {
                 sparql.append(addMetadata(session, ie.getDescriptive(),
                         versionPath + "/DESCRIPTIVE"));
             }
 
             /* add all the representations */
             sparql.append(addRepresentations(session, ie.getRepresentations(),
                     versionPath));
 
             /* update the intellectual entity's properties */
             final DefaultGraphSubjects subjects =
                     new DefaultGraphSubjects(session);
             final String entityUri =
                     subjects.getGraphSubject(entityObject.getNode()).getURI();
             final String versionUri =
                     subjects.getGraphSubject(versionObject.getNode()).getURI();
 
             sparql.append("INSERT {<" + entityUri + "> <" + HAS_LIFECYCLESTATE +
                     "> \"" + LifecycleState.State.INGESTED + "\"} WHERE {};");
             sparql.append("INSERT {<" + entityUri + "> <" +
                     HAS_LIFECYCLESTATE_DETAILS +
                     "> \"successfully ingested at " + new Date().getTime() +
                     "\"} WHERE {};");
             sparql.append("INSERT {<" + entityUri + "> <" + HAS_TYPE +
                     "> \"intellectualentity\"} WHERE {};");
             sparql.append("INSERT {<" + entityUri + "> <" + HAS_VERSION +
                     "> \"" + versionUri + "\"} WHERE {};");
             sparql.append("INSERT {<" + entityUri + "> <" +
                     HAS_CURRENT_VERSION + ">  <" + versionUri + "> } WHERE {};");
             /* update the object and it's child's using sparql */
             entityObject.updatePropertiesDataset(subjects, sparql.toString());
 
             /* save the changes made to the objects */
             session.save();
             return entityId;
 
         } catch (JAXBException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new RepositoryException(e);
         }
     }
 
     private String addBitStreams(final Session session,
             final List<BitStream> bitStreams, final String filePath)
             throws RepositoryException {
 
         final StringBuilder sparql = new StringBuilder();
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
 
         for (BitStream bs : bitStreams) {
             final String bsId =
                     (bs.getIdentifier() != null) ? bs.getIdentifier()
                             .getValue() : UUID.randomUUID().toString();
             final String bsPath = filePath + "/" + bsId;
             final FedoraObject bsObject =
                     this.objectService.createObject(session, bsPath);
             final String uri =
                     subjects.getGraphSubject(bsObject.getNode()).getURI();
             final String fileUri = subjects.getGraphSubject(filePath).getURI();
             if (bs.getTechnical() != null) {
                 sparql.append(addMetadata(session, bs.getTechnical(), bsPath +
                         "/TECHNICAL"));
             }
             final String bsType =
                     (bs.getType() != null) ? bs.getType().name()
                             : BitStream.Type.STREAM.name();
 
             sparql.append("INSERT {<" + uri + "> <" + HAS_TYPE +
                     "> \"bitstream\"} WHERE {};");
             sparql.append("INSERT {<" + uri + "> <" + HAS_BITSTREAM_TYPE +
                     "> \"" + bsType + "\"} WHERE {};");
             sparql.append("INSERT {<" + fileUri + "> <" + HAS_BITSTREAM +
                     "> \"" + uri + "\"} WHERE {};");
         }
 
         return sparql.toString();
     }
 
     private String addFiles(final Session session, final List<File> files,
             final String repPath) throws RepositoryException {
 
         final StringBuilder sparql = new StringBuilder();
         for (File f : files) {
 
             final String fileId =
                     (f.getIdentifier() != null) ? f.getIdentifier().getValue()
                             : UUID.randomUUID().toString();
             final String filePath = repPath + "/" + fileId;
 
             URI fileUri = f.getUri();
             if (fileUri.getScheme() == null) {
                 fileUri = URI.create("file:" + fileUri.toASCIIString());
             }
 
             /* create a datastream in fedora for this file */
             final FedoraObject fileObject =
                     this.objectService.createObject(session, filePath);
             fileObject.getNode().addMixin("scape:file");
             final DefaultGraphSubjects subjects =
                     new DefaultGraphSubjects(session);
             final String uri =
                     subjects.getGraphSubject(fileObject.getNode()).getURI();
             final String repUri =
                     subjects.getGraphSubject("/" + repPath).getURI();
 
             /* add the metadata */
             if (f.getTechnical() != null) {
                 sparql.append(addMetadata(session, f.getTechnical(), filePath +
                         "/TECHNICAL"));
             }
 
             /* add all bitstreams as child objects */
             if (f.getBitStreams() != null) {
                 sparql.append(addBitStreams(session, f.getBitStreams(), "/" +
                         filePath));
             }
             String fileName = f.getFilename();
             if (fileName == null) {
                 fileName = f.getUri().toASCIIString().substring(f.getUri().toASCIIString().lastIndexOf('/') + 1);
             }
             final String mimeType =
                     (f.getMimetype() != null) ? f.getMimetype()
                             : "application/binary";
 
             sparql.append("INSERT {<" + uri + "> <" + HAS_TYPE +
                     "> \"file\"} WHERE {};");
             sparql.append("INSERT {<" + uri + "> <" + HAS_FILENAME + "> \"" +
                     fileName + "\"} WHERE {};");
             sparql.append("INSERT {<" + uri + "> <" + HAS_MIMETYPE + "> \"" +
                     mimeType + "\"} WHERE {};");
             sparql.append("INSERT {<" + uri + "> <" + HAS_INGEST_SOURCE +
                     "> \"" + f.getUri() + "\"} WHERE {};");
             sparql.append("INSERT {<" + repUri + "> <" + HAS_FILE + "> \"" +
                     uri + "\"} WHERE {};");
 
             if (this.referencedContent) {
                 /* only write a reference to the file URI as a node property */
                 sparql.append("INSERT {<" + uri + "> <" +
                         HAS_REFERENCED_CONTENT + "> \"" + fileUri +
                         "\"} WHERE {};");
             } else {
                 /* load the actual binary data into the repo */
                 LOG.info("reding binary from {}", fileUri.toASCIIString());
                 try (final InputStream src = fileUri.toURL().openStream()) {
                     final Node fileDs =
                             this.datastreamService.createDatastreamNode(
                                     session, filePath + "/DATA", f
                                             .getMimetype(), null, src);
                 } catch (IOException | InvalidChecksumException e) {
                     throw new RepositoryException(e);
                 }
             }
         }
 
         return sparql.toString();
     }
 
     private String addMetadata(final Session session, final Object metadata,
             final String path) throws RepositoryException {
         final StringBuilder sparql = new StringBuilder();
         try {
 
             /* use piped streams to copy the data to the repo */
             final PipedInputStream dcSrc = new PipedInputStream();
             final PipedOutputStream dcSink = new PipedOutputStream();
             dcSink.connect(dcSrc);
             new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         ConnectorService.this.marshaller.getJaxbMarshaller()
                                 .marshal(metadata, dcSink);
                         dcSink.flush();
                         dcSink.close();
                     } catch (JAXBException e) {
                         LOG.error(e.getLocalizedMessage(), e);
                     } catch (IOException e) {
                         LOG.error(e.getLocalizedMessage(), e);
                     }
                 }
             }).start();
 
             final Node desc =
                     datastreamService.createDatastreamNode(session, path,
                             "text/xml", null, dcSrc);
 
             final DefaultGraphSubjects subjects =
                     new DefaultGraphSubjects(session);
             final String dsUri = subjects.getGraphSubject(desc).getURI();
             /* get the type of the metadata */
             String type = "unknown";
             String schema = "";
 
             if (metadata.getClass() == ElementContainer.class) {
                 type = "dublin-core";
                 schema = "http://purl.org/dc/elements/1.1/";
             } else if (metadata.getClass() == GbsType.class) {
                 type = "gbs";
                 schema = "http://books.google.com/gbs";
             } else if (metadata.getClass() == Fits.class) {
                 type = "fits";
                 schema = "http://hul.harvard.edu/ois/xml/ns/fits/fits_output";
             } else if (metadata.getClass() == AudioType.class) {
                 type = "audiomd";
                 schema = "http://www.loc.gov/audioMD/";
             } else if (metadata.getClass() == RecordType.class) {
                 type = "marc21";
                 schema = "http://www.loc.gov/MARC21/slim";
             } else if (metadata.getClass() == Mix.class) {
                 type = "mix";
                 schema = "http://www.loc.gov/mix/v20";
             } else if (metadata.getClass() == VideoType.class) {
                 type = "videomd";
                 schema = "http://www.loc.gov/videoMD/";
             } else if (metadata.getClass() == PremisComplexType.class) {
                 type = "premis-provenance";
                 schema = "info:lc/xmlns/premis-v2";
             } else if (metadata.getClass() == RightsComplexType.class) {
                 type = "premis-rights";
                 schema = "info:lc/xmlns/premis-v2";
             } else if (metadata.getClass() == TextMD.class) {
                 type = "textmd";
                 schema = "info:lc/xmlns/textmd-v3";
             }
 
             /* add a sparql query to set the type of this object */
             sparql.append("INSERT {<" + dsUri + "> <" + HAS_TYPE + "> \"" +
                     type + "\"} WHERE {};");
             sparql.append("INSERT {<" + dsUri + "> <" + HAS_SCHEMA + "> \"" +
                     schema + "\"} WHERE {};");
 
             return sparql.toString();
 
         } catch (IOException e) {
             throw new RepositoryException(e);
         } catch (InvalidChecksumException e) {
             throw new RepositoryException(e);
         }
     }
 
     private String
             addRepresentations(final Session session,
                     final List<Representation> representations,
                     final String versionPath) throws RepositoryException {
         final StringBuilder sparql = new StringBuilder();
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String versionUri =
                 subjects.getGraphSubject("/" + versionPath).getURI();
         for (Representation rep : representations) {
             final String repId =
                     (rep.getIdentifier() != null) ? rep.getIdentifier()
                             .getValue() : UUID.randomUUID().toString();
             final String repPath = versionPath + "/" + repId;
             final FedoraObject repObject =
                     objectService.createObject(session, repPath);
             final String repUri =
                     subjects.getGraphSubject(repObject.getNode()).getURI();
             repObject.getNode().addMixin("scape:representation");
 
             /* add the metadatasets of the rep as datastreams */
             if (rep.getTechnical() != null) {
                 sparql.append(addMetadata(session, rep.getTechnical(), repPath +
                         "/TECHNICAL"));
             }
             if (rep.getSource() != null) {
                 sparql.append(addMetadata(session, rep.getSource(), repPath +
                         "/SOURCE"));
             }
             if (rep.getRights() != null) {
                 sparql.append(addMetadata(session, rep.getRights(), repPath +
                         "/RIGHTS"));
             }
             if (rep.getProvenance() != null) {
                 sparql.append(addMetadata(session, rep.getProvenance(),
                         repPath + "/PROVENANCE"));
             }
 
             /* add all the files */
             sparql.append(addFiles(session, rep.getFiles(), repPath));
 
             /* add a sparql query to set the type of this object */
             sparql.append("INSERT {<" + repUri + "> <" + HAS_TYPE +
                     "> \"representation\"} WHERE {};");
             sparql.append("INSERT {<" + repUri + "> <" + HAS_TITLE + "> \"" +
                     rep.getTitle() + "\"} WHERE {};");
             sparql.append("INSERT {<" + versionUri + "> <" +
                     HAS_REPRESENTATION + "> \"" + repUri + "\"} WHERE {};");
 
         }
         return sparql.toString();
     }
 
     public void updateEntity(final Session session, final InputStream src,
             final String entityId) throws RepositoryException {
         final String entityPath = "/" + ENTITY_FOLDER + "/" + entityId;
         final FedoraObject entityObject =
                 this.objectService.getObject(session, entityPath);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri =
                 subjects.getGraphSubject(entityObject.getNode()).getURI();
         /* fetch the current version number from the repo */
         final String oldVersionPath =
                 getCurrentVersionPath(SerializationUtils
                         .unifyDatasetModel(entityObject
                                 .getPropertiesDataset(subjects)), uri);
         final String oldVersionUri =
                 subjects.getGraphSubject(oldVersionPath).getURI();
         int versionNumber =
                 Integer.parseInt(oldVersionPath.substring(oldVersionPath
                         .lastIndexOf('-') + 1)) + 1;
         final String newVersionPath = entityPath + "/version-" + versionNumber;
         final String newVersionUri =
                 subjects.getGraphSubject(newVersionPath).getURI();
 
         try {
             /* read the post body into an IntellectualEntity object */
             final IntellectualEntity ie =
                     this.marshaller.deserialize(IntellectualEntity.class, src);
             final StringBuilder sparql = new StringBuilder();
 
             final FedoraObject versionObject =
                     objectService.createObject(session, newVersionPath);
 
             /* add the metadata datastream for descriptive metadata */
             if (ie.getDescriptive() != null) {
                 sparql.append(addMetadata(session, ie.getDescriptive(),
                         newVersionPath + "/DESCRIPTIVE"));
             }
 
             /* add all the representations */
             sparql.append(addRepresentations(session, ie.getRepresentations(),
                     newVersionPath));
 
             sparql.append("DELETE {<" + uri + "> <" + HAS_CURRENT_VERSION +
                     "> <" + oldVersionUri + ">} WHERE {};");
             sparql.append("INSERT {<" + uri + "> <" + HAS_VERSION + "> \"" +
                     newVersionUri + "\"} WHERE {};");
             sparql.append("INSERT {<" + uri + "> <" + HAS_CURRENT_VERSION +
                     ">  <" + newVersionUri + ">} WHERE {};");
 
             /* update the object and it's child's using sparql */
             entityObject.updatePropertiesDataset(subjects, sparql.toString());
 
             /* save the changes made to the objects */
             session.save();
 
         } catch (JAXBException e) {
             LOG.error(e.getLocalizedMessage(), e);
             throw new RepositoryException(e);
         }
 
     }
 
     public void updateRepresentation(Session session, String entityId,
             String repId, InputStream src) throws RepositoryException {
         try {
             final Representation rep =
                     (Representation) this.marshaller.deserialize(src);
             final List<Representation> representations = new ArrayList<>();
             final IntellectualEntity orig = this.fetchEntity(session, entityId);
             for (Representation r : orig.getRepresentations()) {
                 if (!r.getIdentifier().getValue().equals(repId)) {
                     representations.add(r);
                 }
             }
             representations.add(rep);
 
             final IntellectualEntity ieUpdate =
                     new IntellectualEntity.Builder(orig).representations(
                             representations).build();
 
             final ByteArrayOutputStream sink = new ByteArrayOutputStream();
             this.marshaller.serialize(ieUpdate, sink);
             this.updateEntity(session, new ByteArrayInputStream(sink
                     .toByteArray()), entityId);
 
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
 
     }
 
     public void updateMetadata(final Session session, final String path,
             final InputStream src) throws RepositoryException {
         String[] ids = path.split("/");
         final String entityId = ids[0];
         final String metadataName = ids[ids.length - 1];
         switch (ids.length) {
             case 2:
                 /* it's entity metadata */
                 updateEntityMetadata(session, entityId, metadataName, src);
                 break;
             case 3:
                 /* it's rep metadata */
                 updateRepresentationMetadata(session, entityId, ids[1],
                         metadataName, src);
                 break;
             case 4:
                 /* it's file metadata */
                 updateFileMetadata(session, entityId, ids[1], ids[2],
                         metadataName, src);
                 break;
             case 5:
                 /* it's bitstream metadata */
                 updateBitStreamMetadata(session, entityId, ids[1], ids[2],
                         ids[3], metadataName, src);
                 break;
             default:
                 throw new RepositoryException(
                         "Unable to parse path for metadata update");
         }
     }
 
     private void updateBitStreamMetadata(Session session, String entityId,
             String repId, String fileId, String bsId, String metadataName,
             InputStream src) throws RepositoryException {
 
         try {
 
             if (!metadataName.equals("TECHNICAL")) {
                 throw new RepositoryException("Unknown metadata type " +
                         metadataName);
             }
             final Object metadata = this.marshaller.deserialize(src);
 
             final List<Representation> representations = new ArrayList<>();
             final IntellectualEntity orig = this.fetchEntity(session, entityId);
             for (Representation r : orig.getRepresentations()) {
                 if (!r.getIdentifier().getValue().equals(repId)) {
                     representations.add(r);
                 } else {
                     Representation.Builder newRep =
                             new Representation.Builder(r);
                     List<File> files = new ArrayList<>();
                     for (File f : r.getFiles()) {
                         if (!f.getIdentifier().getValue().equals(fileId)) {
                             files.add(f);
                         } else {
                             File.Builder newFile = new File.Builder(f);
                             List<BitStream> bitstreams = new ArrayList<>();
                             for (BitStream bs : f.getBitStreams()) {
                                 if (!bs.getIdentifier().getValue().equals(bsId)) {
                                     bitstreams.add(bs);
                                 } else {
                                     BitStream newBs =
                                             new BitStream.Builder(bs)
                                                     .technical(metadata)
                                                     .build();
                                     bitstreams.add(newBs);
                                 }
                             }
                             newFile.bitStreams(bitstreams);
                             files.add(newFile.build());
                         }
                     }
                     newRep.files(files);
                     representations.add(newRep.build());
                 }
             }
 
             final IntellectualEntity ieUpdate =
                     new IntellectualEntity.Builder(orig).representations(
                             representations).build();
 
             final ByteArrayOutputStream sink = new ByteArrayOutputStream();
             this.marshaller.serialize(ieUpdate, sink);
             this.updateEntity(session, new ByteArrayInputStream(sink
                     .toByteArray()), entityId);
 
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
     }
 
     private void updateFileMetadata(Session session, String entityId,
             String repId, String fileId, String metadataName, InputStream src)
             throws RepositoryException {
         try {
 
             if (!metadataName.equals("TECHNICAL")) {
                 throw new RepositoryException("Unknown metadata type " +
                         metadataName);
             }
             final Object metadata = this.marshaller.deserialize(src);
 
             final List<Representation> representations = new ArrayList<>();
             final IntellectualEntity orig = this.fetchEntity(session, entityId);
             for (Representation r : orig.getRepresentations()) {
                 if (!r.getIdentifier().getValue().equals(repId)) {
                     representations.add(r);
                 } else {
                     Representation.Builder newRep =
                             new Representation.Builder(r);
                     List<File> files = new ArrayList<>();
                     for (File f : r.getFiles()) {
                         if (!f.getIdentifier().getValue().equals(fileId)) {
                             files.add(f);
                         } else {
                             File newFile =
                                     new File.Builder(f).technical(metadata)
                                             .build();
                             files.add(newFile);
                         }
                     }
                     newRep.files(files);
                     representations.add(newRep.build());
                 }
             }
 
             final IntellectualEntity ieUpdate =
                     new IntellectualEntity.Builder(orig).representations(
                             representations).build();
 
             final ByteArrayOutputStream sink = new ByteArrayOutputStream();
             this.marshaller.serialize(ieUpdate, sink);
             this.updateEntity(session, new ByteArrayInputStream(sink
                     .toByteArray()), entityId);
 
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
     }
 
     private void updateRepresentationMetadata(Session session, String entityId,
             String repId, String metadataName, InputStream src)
             throws RepositoryException {
 
         try {
 
             if (!(metadataName.equals("TECHNICAL") ||
                     metadataName.equals("SOURCE") ||
                     metadataName.equals("PROVENANCE") || metadataName
                         .equals("RIGHTS"))) {
                 throw new RepositoryException("Unknown metadata type " +
                         metadataName);
             }
             final Object metadata = this.marshaller.deserialize(src);
 
             final List<Representation> representations = new ArrayList<>();
             final IntellectualEntity orig = this.fetchEntity(session, entityId);
             for (Representation r : orig.getRepresentations()) {
                 if (!r.getIdentifier().getValue().equals(repId)) {
                     representations.add(r);
                 } else {
                     Representation.Builder newRep =
                             new Representation.Builder(r);
                     if (metadataName.equals("TECHNICAL")) {
                         newRep.technical(metadata);
                     } else if (metadataName.equals("SOURCE")) {
                         newRep.source(metadata);
                     } else if (metadataName.equals("PROVENANCE")) {
                         newRep.provenance(metadata);
                     } else if (metadataName.equals("RIGHTS")) {
                         newRep.rights(metadata);
                     }
                     representations.add(newRep.build());
                 }
             }
 
             final IntellectualEntity ieUpdate =
                     new IntellectualEntity.Builder(orig).representations(
                             representations).build();
 
             final ByteArrayOutputStream sink = new ByteArrayOutputStream();
             this.marshaller.serialize(ieUpdate, sink);
             this.updateEntity(session, new ByteArrayInputStream(sink
                     .toByteArray()), entityId);
 
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
     }
 
     private void updateEntityMetadata(Session session, String entityId,
             String metadataName, InputStream src) throws RepositoryException {
         try {
             final IntellectualEntity orig = this.fetchEntity(session, entityId);
             if (!metadataName.equals("DESCRIPTIVE")) {
                 throw new RepositoryException("Unknown metadata type " +
                         metadataName);
             }
             final Object desc = this.marshaller.deserialize(src);
             final IntellectualEntity ieUpdate =
                     new IntellectualEntity.Builder(orig).descriptive(desc)
                             .build();
 
             final ByteArrayOutputStream sink = new ByteArrayOutputStream();
             this.marshaller.serialize(ieUpdate, sink);
             this.updateEntity(session, new ByteArrayInputStream(sink
                     .toByteArray()), entityId);
 
         } catch (JAXBException e) {
             throw new RepositoryException(e);
         }
     }
 
     private String getFirstLiteralString(Model model, Resource subject,
             String propertyName) {
 
         final Property p = model.createProperty(propertyName);
         final StmtIterator it =
                 model.listStatements(subject, p, (RDFNode) null);
         return it.next().getLiteral().getString();
     }
 
     private List<String> getLiteralStrings(Model model, Resource subject,
             String propertyName) {
         final List<String> result = new ArrayList<>();
         final Property p = model.createProperty(propertyName);
         final StmtIterator it =
                 model.listStatements(subject, p, (RDFNode) null);
         while (it.hasNext()) {
             result.add(it.next().getLiteral().getString());
         }
         return result;
     }
 
     public String queueEntityForIngest(final Session session,
             final InputStream src) throws RepositoryException {
         try {
             /* try to deserialize and extraxt an existing id */
             IntellectualEntity ie =
                     this.marshaller.deserialize(IntellectualEntity.class, src);
             String id =
                     (ie.getIdentifier() == null ||
                             ie.getIdentifier().getValue() == null || ie
                             .getIdentifier().getValue().length() == 0) ? UUID
                             .randomUUID().toString() : ie.getIdentifier()
                             .getValue();
 
             /* copy the data to a temporary node */
             ByteArrayOutputStream sink = new ByteArrayOutputStream();
             this.marshaller.serialize(ie, sink);
             final FedoraObject queue =
                     this.objectService.getObject(session, QUEUE_NODE);
             if (this.objectService.exists(session, "/" + ENTITY_FOLDER + "/" +
                     id)) {
                 throw new RepositoryException(
                         "Unable to queue item with id " +
                                 id +
                                 " for ingest since an intellectual entity with that id already esists in the repository");
             }
             if (this.datastreamService.exists(session, QUEUE_NODE + "/" + id)) {
                 throw new RepositoryException("Unable to queue item with id " +
                         id +
                         " for ingest since an item with that id is alread in the queue");
             }
             final Node item =
                     this.datastreamService.createDatastreamNode(session,
                             QUEUE_NODE + "/" + id, "text/xml", null,
                             new ByteArrayInputStream(sink.toByteArray()));
             item.setProperty(ScapeRDFVocabulary.HAS_INGEST_STATE, "QUEUED");
             /* update the ingest queue */
             final DefaultGraphSubjects subjects =
                     new DefaultGraphSubjects(session);
             final String uri = subjects.getGraphSubject(QUEUE_NODE).getURI();
             final String sparql =
                     "INSERT {<" + uri + "> <" + HAS_ITEM + "> \"" +
                             item.getPath() + "\"} WHERE {}";
             queue.updatePropertiesDataset(subjects, sparql);
             session.save();
             return id;
         } catch (IOException | InvalidChecksumException | JAXBException e) {
             throw new RepositoryException(e);
         }
 
     }
 
     public LifecycleState fetchLifeCycleState(Session session, String entityId)
             throws RepositoryException {
         /* check the async queue for the entity */
         final FedoraObject queueObject =
                 this.objectService.getObject(session, QUEUE_NODE);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri =
                 subjects.getGraphSubject(queueObject.getNode()).getURI();
         final Model queueModel =
                 SerializationUtils.unifyDatasetModel(queueObject
                         .getPropertiesDataset(subjects));
         final Resource parent = queueModel.createResource(uri);
         final List<String> asyncIds =
                 this.getLiteralStrings(queueModel, parent, HAS_ITEM);
         if (asyncIds.contains(QUEUE_NODE + "/" + entityId)) {
             String state =
                     this.datastreamService.getDatastreamNode(session,
                             QUEUE_NODE + "/" + entityId).getProperties(
                             ScapeRDFVocabulary.HAS_INGEST_STATE).nextProperty()
                             .getString();;
             switch (state) {
                 case "INGESTING":
                     return new LifecycleState("", State.INGESTING);
                 case "INGEST_FAILED":
                     return new LifecycleState("", State.INGEST_FAILED);
                 case "QUEUED":
                     return new LifecycleState("", State.INGESTING);
                 default:
                     break;
             }
         }
 
         /* check if the entity exists */
         if (this.objectService.exists(session, "/" + ENTITY_FOLDER + "/" +
                 entityId)) {
             /* fetch the state form the entity itself */
             final FedoraObject entityObject =
                     this.objectService.getObject(session, "/" + ENTITY_FOLDER +
                             "/" + entityId);
             final String entityUri =
                     subjects.getGraphSubject(entityObject.getNode()).getURI();
             final Model entityModel =
                     SerializationUtils.unifyDatasetModel(entityObject
                             .getPropertiesDataset(subjects));
             final Resource subject = entityModel.createResource(entityUri);
             final String state =
                     this.getFirstLiteralString(entityModel, subject,
                             HAS_LIFECYCLESTATE);
             final String details =
                     this.getFirstLiteralString(entityModel, subject,
                             HAS_LIFECYCLESTATE_DETAILS);
             return new LifecycleState(details, LifecycleState.State
                     .valueOf(state));
         } else {
             throw new ItemNotFoundException("Unable to find lifecycle for '" +
                     entityId + "'");
         }
 
     }
 
     @Scheduled(fixedDelay = 1000, initialDelay = 5000)
     public void ingestFromQueue() throws RepositoryException {
         Session session = sessionFactory.getInternalSession();
         if (!this.objectService.exists(session, QUEUE_NODE)) {
             return;
         }
         for (String item : getItemsFromQueue(session)) {
             final Datastream ds =
                     this.datastreamService.getDatastream(session, item);
             /* update the ingest state so that it won't get ingested twice */
             try {
                 ds.getNode().setProperty(ScapeRDFVocabulary.HAS_INGEST_STATE,
                         "INGESTING");
                 addEntity(session, ds.getContent(), item.substring(QUEUE_NODE
                         .length() + 1));
                 deleteFromQueue(session, item);
             } catch (Exception e) {
                 ds.getNode().setProperty(ScapeRDFVocabulary.HAS_INGEST_STATE,
                         "INGEST_FAILED");
             }
         }
         session.save();
     }
 
     private void deleteFromQueue(final Session session, final String item)
             throws RepositoryException {
         final FedoraObject queueObject =
                 this.objectService.getObject(session, QUEUE_NODE);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri =
                 subjects.getGraphSubject(queueObject.getNode()).getURI();
 
         final String sparql =
                 "DELETE {<" + uri + "> <" + HAS_ITEM + "> \"" + item +
                         "\"} WHERE {}";
         queueObject.updatePropertiesDataset(subjects, sparql);
         this.nodeService.deleteObject(session, item);
         session.save();
     }
 
     private List<String> getItemsFromQueue(final Session session)
             throws RepositoryException {
         final FedoraObject queueObject =
                 this.objectService.getObject(session, QUEUE_NODE);
         final DefaultGraphSubjects subjects = new DefaultGraphSubjects(session);
         final String uri =
                 subjects.getGraphSubject(queueObject.getNode()).getURI();
         final Model queueModel =
                 SerializationUtils.unifyDatasetModel(queueObject
                         .getPropertiesDataset(subjects));
         final Resource parent = queueModel.createResource(uri);
         StmtIterator it =
                 queueModel.listStatements(parent, queueModel
                         .createProperty(HAS_ITEM), (RDFNode) null);
         List<String> queueItems = new ArrayList<>();
         while (it.hasNext()) {
             String path = it.next().getObject().asLiteral().getString();
             if (this.datastreamService.getDatastreamNode(session, path)
                     .getProperties(ScapeRDFVocabulary.HAS_INGEST_STATE)
                     .nextProperty().getString().equals("QUEUED")) {
                 queueItems.add(path);
             }
         }
         return queueItems;
     }
 
     public IntellectualEntityCollection fetchEntites(final Session session,
             final List<String> paths) throws RepositoryException {
         List<IntellectualEntity> entities = new ArrayList<>();
         for (String path : paths) {
             path = path.substring(path.indexOf("/scape/entity") + 14);
             entities.add(this.fetchEntity(session, path));
         }
         return new IntellectualEntityCollection(entities);
     }
 
     public List<String> searchEntities(Session session, String terms,
             int offset, int limit) throws RepositoryException {
 
         return searchObjectOfType(session, "scape:intellectual-entity", terms,
                 offset, limit);
     }
 
     public List<String> searchRepresentations(Session session, String terms,
             int offset, int limit) throws RepositoryException {
         return searchObjectOfType(session, "scape:representation", terms,
                 offset, limit);
     }
 
     public List<String> searchFiles(Session session, String terms, int offset,
             int limit) throws RepositoryException {
         return searchObjectOfType(session, "scape:file", terms, offset, limit);
     }
 
     public List<String> searchObjectOfType(final Session session,
             final String mixinType, final String terms, final int offset,
             final int limit) throws RepositoryException {
         final QueryManager queryManager =
                 session.getWorkspace().getQueryManager();
 
         final QueryObjectModelFactory factory = queryManager.getQOMFactory();
 
         final Source selector =
                 factory.selector(mixinType, "resourcesSelector");
         final Constraint constraints =
                 factory.fullTextSearch("resourcesSelector", null, factory
                         .literal(session.getValueFactory().createValue(terms)));
 
         final Query query =
                 factory.createQuery(selector, constraints, null, null);
 
         query.setLimit(limit);
         query.setOffset(offset);
         final QueryResult result = query.execute();
         final NodeIterator it = result.getNodes();
         final List<String> uris = new ArrayList<>();
         while (it.hasNext()) {
             Node n = it.nextNode();
             uris.add(n.getPath());
         }
         return uris;
     }
 }
