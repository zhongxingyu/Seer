 package org.apache.lenya.cms.site.usecases;
 
 import org.apache.excalibur.source.SourceResolver;
 import org.apache.lenya.cms.cocoon.source.SourceUtil;
 import org.apache.lenya.cms.publication.Document;
 import org.apache.lenya.cms.publication.Publication;
 import org.apache.lenya.defaultpub.cms.usecases.Publish;
 
 public class CustomPublish extends Publish {
     protected void initParameters() {
         super.initParameters();
     }
 
     protected void doCheckPreconditions() throws Exception {
         super.doCheckPreconditions();
     }
 
     protected void doExecute() throws Exception {
         super.doExecute();
     }
 
     protected void publish(Document authoringDocument) {
         SourceResolver resolver = null;
         String destination;
         int authoringOccurence;
         Document doc = getSourceDocument();
         String sourceUri = getSourceDocument().getSourceURI();
         try {
             resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
             // lock destinationDocument
             Document destinationDocument = doc.getIdentityMap().getAreaVersion(doc,
                             Publication.LIVE_AREA);
             // publish
             super.publish(authoringDocument);
             // copy again (workaround for lenya bug)
             destinationDocument.getRepositoryNode().lock();
             SourceUtil.copy(resolver, sourceUri, destinationDocument.getSourceURI());
             // replace the main extension with the media type extra information
             // extension
             sourceUri = sourceUri.substring(0, sourceUri.lastIndexOf(".") + 1).concat(
                             MediaAssets.MEDIA_FILE_EXTENSION);
             authoringOccurence = sourceUri.indexOf(Publication.AUTHORING_AREA);
             // replace "authoring" with "live"
             destination = sourceUri.substring(0, authoringOccurence).concat(Publication.LIVE_AREA);
             // add the docid
             destination = destination.concat(sourceUri.substring(authoringOccurence
                             + Publication.AUTHORING_AREA.length(), sourceUri.length()));
             resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
             try {
                 SourceUtil.copy(resolver, sourceUri, destination);
             } catch (Exception e) {
                 throw new Exception(e);
             } finally {
                 this.manager.release(resolver);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
