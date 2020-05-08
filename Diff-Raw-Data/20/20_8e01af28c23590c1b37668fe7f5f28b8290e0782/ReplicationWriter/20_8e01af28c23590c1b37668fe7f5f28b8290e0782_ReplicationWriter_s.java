 /*
  * (C) Copyright 2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  */
 package org.nuxeo.ecm.platform.replication.exporter;
 
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_BASE_VERSION_ID;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_CHECKED_IN;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_LIFECYCLE_POLICY;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_LIFECYCLE_STATE;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_LOCK;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_PROXY_TARGET_ID;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_PROXY_VERSIONABLE_ID;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_CREATED;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_DESCRIPTION;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_LABEL;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_MAJOR;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_MINOR;
 import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_VERSIONABLE_ID;
 import static org.nuxeo.ecm.platform.replication.common.ReplicationConstants.DOCUMENTARY_BASE_LOCATION_NAME;
 import static org.nuxeo.ecm.platform.replication.common.ReplicationConstants.USUAL_DOCUMENTS_LOCATION_NAME;
 import static org.nuxeo.ecm.platform.replication.common.ReplicationConstants.VERSIONS_LOCATION_NAME;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.nuxeo.common.utils.StringUtils;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.VersionModel;
 import org.nuxeo.ecm.core.api.facet.VersioningDocument;
 import org.nuxeo.ecm.core.io.DocumentTranslationMap;
 import org.nuxeo.ecm.core.io.ExportedDocument;
 import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;
 import org.nuxeo.ecm.core.schema.FacetNames;
 import org.nuxeo.ecm.core.schema.types.primitives.DateType;
 import org.nuxeo.ecm.platform.replication.exporter.reporter.ExporterReporter;
 
 /**
  * Extends XMLDirectoryWriter to provide additional metadata .
  *
  * @author cpriceputu@nuxeo.com
  * @author rux added the reporter logging stuff
  */
 public class ReplicationWriter extends XMLDirectoryWriter {
 
     public static final String FAKE_BLOB_BODY = "The original blob could not be found, this is a fake replacement one.";
 
     private static final Logger log = Logger.getLogger(ReplicationWriter.class);
 
     private static final Object mutex = new Object();
 
     private final CoreSession session;
 
     public ReplicationWriter(File file, CoreSession session) {
         super(file);
         this.session = session;
     }
 
     @Override
     public DocumentTranslationMap write(ExportedDocument doc)
     throws IOException {
 
         File parent = new File(getDestination().toString(),
                 DOCUMENTARY_BASE_LOCATION_NAME);
         ExporterReporter.getInstance().incrementDocumentNumber();
 
         try {
             DocumentModel document = session
             .getDocument(new IdRef(doc.getId()));
             OutputFormat format = OutputFormat.createPrettyPrint();
 
             if (!document.isVersion()) {
                 parent = new File(parent, USUAL_DOCUMENTS_LOCATION_NAME);
                 parent = new File(parent, doc.getPath().toString());
             } else {
                 parent = new File(parent, VERSIONS_LOCATION_NAME);
                 parent = new File(parent, document.getId());
             }
             synchronized (mutex) {
                 parent.mkdirs();
             }
             String documentLocation = parent.getAbsolutePath();
 
             XMLWriter writer = null;
             try {
                 writer = new XMLWriter(new FileOutputStream(new File(parent,
                         "document.xml")), format);
                 writer.write(doc.getDocument());
                 writer.close();
             } catch (Exception xmle) {
                 // can't recover, document structure corrupted?
                 log.error(documentLocation + " can't be exported!", xmle);
                 ExporterReporter.getInstance().logDocumentStructure(
                         documentLocation);
                 return null;
             }
             Map<String, Blob> blobs = doc.getBlobs();
             for (Map.Entry<String, Blob> entry : blobs.entrySet()) {
                 File file = new File(parent, entry.getKey());
 
                 try {
                     entry.getValue().transferTo(file);
                 } catch (Exception e) {
                     // this is case 4
                     ExporterReporter.getInstance().logMissingBlob(
                             documentLocation, entry.getKey());
                     log.warn("Could not export blob creating a fake one.", e);
                     createFakeBlob(file);
                 }
             }
 
             // write external documents
             for (Map.Entry<String, Document> entry : doc.getDocuments()
                     .entrySet()) {
                 writer = new XMLWriter(new FileOutputStream(new File(parent,
                         entry.getKey() + ".xml")), format);
                 writer.write(entry.getValue());
                 writer.close();
             }
 
             Properties metadata = getDocumentMetadata(session, document,
                     documentLocation);
             File metadataFile = new File(parent, "metadata.properties");
             metadata.store(new FileOutputStream(metadataFile),
             "Document Metadata");
         } catch (Exception e) {
             String location = parent.getAbsolutePath();
             log.error(location + " missing!", e);
             ExporterReporter.getInstance().logUnknownError(location,
                     e.getMessage());
         }
         return null;
     }
 
     public static void createFakeBlob(File file) {
 
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(file, false);
             fos.write(FAKE_BLOB_BODY.getBytes());
         } catch (Exception e) {
             log.error("Can't create even the fake blob", e);
         } finally {
             if (fos != null) {
                 try {
                     fos.close();
                 } catch (Exception e2) {
                     // so what?
                     log.error("Error closing stream", e2);
                 }
             }
         }
     }
 
     public static Properties getDocumentMetadata(CoreSession documentManager,
             DocumentModel document, String documentLocation) {
         Properties props = new Properties();
         DocumentRef ref = document.getRef();
         if (document.isProxy()) {
             // export proxy
             DocumentModel version = null;
             try {
                 version = documentManager.getSourceDocument(ref);
             } catch (Exception e) {
                 // so, can't get to the content of proxy
                 // this is case 1
                 log.warn("Can't identify the content of the proxy "
                         + documentLocation, e);
                 ExporterReporter.getInstance().logMissingVersion(
                         documentLocation, "<unknown>");
             }
             if (version != null) {
                 props.setProperty(IMPORT_PROXY_TARGET_ID,
                         version.getId() == null ? "" : version.getId());
                 DocumentModel liveDocument = null;
                 try {
                     liveDocument = documentManager.getSourceDocument(version
                             .getRef());
                 } catch (Exception e) {
                     // so, can't get to the live document of proxy
                     log.warn("Can't identify the source of the proxy "
                             + documentLocation, e);
                     ExporterReporter.getInstance().logMissingLivedoc(
                             documentLocation);
                 }
                 if (liveDocument != null) {
                     props.setProperty(IMPORT_PROXY_VERSIONABLE_ID, liveDocument
                             .getId() == null ? "" : liveDocument.getId());
                 } else {
                     props.setProperty(IMPORT_PROXY_VERSIONABLE_ID, "");
                 }
             } else {
                 props.setProperty(IMPORT_PROXY_TARGET_ID, "");
             }
         } else if (document.isVersion()) {
             // export version
             String docLabel = document.getVersionLabel();
             if (docLabel == null) {
                 docLabel = "";
             }
             props.setProperty(IMPORT_VERSION_LABEL, docLabel);
             DocumentModel liveDocument = null;
             try {
                 liveDocument = documentManager.getSourceDocument(ref);
             } catch (Exception e) {
                 // so, can't get to the live document of version
                 log.warn("Can't identify the source of the version "
                         + documentLocation, e);
                 ExporterReporter.getInstance().logMissingLivedoc(
                         documentLocation);
             }
             if (liveDocument == null) {
                 props.setProperty(IMPORT_VERSION_VERSIONABLE_ID, "");
                 props.setProperty(IMPORT_VERSION_DESCRIPTION, "");
                 props.setProperty(IMPORT_VERSION_CREATED, "");
             } else {
                 props.setProperty(IMPORT_VERSION_VERSIONABLE_ID, liveDocument
                         .getId() == null ? "" : liveDocument.getId());
                 // as the version related metadata are available only through
                 // listing and not direct introspection...
                 try {
                     List<VersionModel> versions = documentManager
                     .getVersionsForDocument(liveDocument.getRef());
                     for (VersionModel version : versions) {
                         if (!docLabel.equals(version.getLabel())) {
                             continue;
                         }
                         // add version description
                         String propValue = version.getDescription();
                         props.setProperty(IMPORT_VERSION_DESCRIPTION,
                                 propValue == null ? "" : propValue);
                         // add version creation date
                         propValue = new DateType().encode(version.getCreated());
                         props.setProperty(IMPORT_VERSION_CREATED,
                                 propValue == null ? "" : propValue);
                         break;
                     }
                 } catch (Exception e) {
                     // can't list the versions of the live document
                     log.warn(
                             "Failure listing the versions on the same level with "
                             + documentLocation, e);
                     // don't even bother to register as error: missing
                     // description and date is not big
                     props.setProperty(IMPORT_VERSION_DESCRIPTION, "");
                     props.setProperty(IMPORT_VERSION_CREATED, "");
                 }
             }
 
             VersioningDocument docVer = document
             .getAdapter(VersioningDocument.class);
             String minorVer = null;
             try {
                 minorVer = docVer.getMinorVersion().toString();
             } catch (Exception e) {
                 // bad luck, not important
                 log.warn("Error looking for minor version of "
                         + documentLocation, e);
             }
             String majorVer = null;
             try {
                 majorVer = docVer.getMajorVersion().toString();
             } catch (Exception e) {
                 // bad luck, not important
                 log.warn("Error looking for major version of "
                         + documentLocation, e);
             }
 
             props.setProperty(IMPORT_VERSION_MAJOR, majorVer == null ? ""
                     : majorVer);
 
             props.setProperty(IMPORT_VERSION_MINOR, minorVer == null ? ""
                     : minorVer);
         } else {
             // export usual
             props.setProperty(IMPORT_LOCK, document.getLock() == null ? ""
                     : document.getLock());
             if (document.isVersionable()) {
                 props.setProperty(IMPORT_CHECKED_IN, Boolean.FALSE.toString());
                 // add the id of the last version, which represents the base for
                 // the current state of the document
                 DocumentModel version = null;
                 try {
                     version = documentManager.getLastDocumentVersion(ref);
                 } catch (Exception e) {
                     // this is case 3 and 5
                     log.error("Failure to get last known version of "
                             + documentLocation, e);
                     ExporterReporter.getInstance().logMissingVersion(
                             documentLocation, "last version");
                 }
                 if ((version != null)
                         && version.getId().equals(document.getId())) {
                     props.setProperty(IMPORT_BASE_VERSION_ID, version.getId());
                 }
                 VersioningDocument docVer = document
                 .getAdapter(VersioningDocument.class);
                 if (docVer != null) {
                     String minorVer = null;
                     try {
                         minorVer = docVer.getMinorVersion().toString();
                     } catch (Exception e) {
                         // bad luck, not important
                         log.warn("Error looking for minor version of "
                                 + documentLocation, e);
                     }
                     String majorVer = null;
                     try {
                         majorVer = docVer.getMajorVersion().toString();
                     } catch (Exception e) {
                         // bad luck, not important
                         log.warn("Error looking for major version of "
                                 + documentLocation, e);
                     }
                     // add major version
                     props.setProperty(IMPORT_VERSION_MAJOR,
                             majorVer == null ? "" : majorVer);
                     // add minor version
                     props.setProperty(IMPORT_VERSION_MINOR,
                             minorVer == null ? "" : minorVer);
                 }
             }
         }
 
         String propValue = null;
         try {
             propValue = document.getCurrentLifeCycleState();
         } catch (Exception e) {
             log.warn("Can't get the lifecycle for " + documentLocation, e);
         }
 
         props.setProperty(IMPORT_LIFECYCLE_STATE, propValue == null ? ""
                 : propValue);
         propValue = null;
         try {
             propValue = document.getLifeCyclePolicy();
         } catch (Exception e) {
             log.warn("Can't get the lifecycle for " + documentLocation, e);
         }
         props.setProperty(IMPORT_LIFECYCLE_POLICY, propValue == null ? ""
                 : propValue);
 
        // added the order of the children  
         if ( document.hasFacet(FacetNames.ORDERABLE)) {
             try {
                 List<DocumentRef> list = documentManager.getChildrenRefs(document.getRef(), null);
                 if ( list != null) {
                     int len = list.size();
                     StringBuilder builder = new StringBuilder();
                     if ( len > 0) {
                         builder.append(list.get(0));
                     }
                     for ( int i=1; i < len ; i++) {
                         builder.append(';').append(list.get(i));
                     }
                     props.put("ecm:childrenOrder", builder.toString());
                 }
             } catch (ClientException e) {
                 log.warn("Can't get the children order for " + documentLocation, e);
             }
         }
 
         return props;
     }
 }
