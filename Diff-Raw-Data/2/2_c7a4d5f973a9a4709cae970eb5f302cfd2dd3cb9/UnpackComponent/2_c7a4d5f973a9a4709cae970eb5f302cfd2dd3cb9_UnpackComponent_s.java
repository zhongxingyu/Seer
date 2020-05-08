 package com.celements.photo.unpack;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.photo.container.ImageLibStrings;
 import com.celements.photo.utilities.AddAttachmentToDoc;
 import com.celements.photo.utilities.Unzip;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 @Component
 public class UnpackComponent implements IUnpackComponentRole {
   
   @Requirement
   Execution execution;
   
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       UnpackComponent.class);
   
   public void unzipFileToAttachment(DocumentReference zipSrcDocRef, String attachmentName,
       String unzipFileName, DocumentReference destinationDoc) {
     try {
       XWikiDocument zipSourceDoc = getContext().getWiki().getDocument(zipSrcDocRef, 
           getContext());
       XWikiAttachment zipAtt = zipSourceDoc.getAttachment(attachmentName);
       unzipFileToAttachment(zipAtt, attachmentName, destinationDoc);
     } catch (XWikiException xwe) {
       LOGGER.error("Exception getting zip source document", xwe);
     }
   }
   
   public String unzipFileToAttachment(XWikiAttachment zipSrcFile, String attName,
       DocumentReference destDocRef) {
     String cleanName = attName;
     if(zipSrcFile != null) {
       LOGGER.info("START unzip: zip='" + zipSrcFile.getFilename() + "' file='" + attName + 
           "'");
       if(isZipFile(zipSrcFile)){
         ByteArrayOutputStream newAttOutStream = null;
         try {
           newAttOutStream = (new Unzip()).getFile(IOUtils.toByteArray(
               zipSrcFile.getContentInputStream(getContext())), attName);
           cleanName = attName.replace(System.getProperty("file.separator"), ".");
           cleanName = getContext().getWiki().clearName(cleanName, false, true, 
               getContext());
           XWikiDocument destDoc = getContext().getWiki().getDocument(destDocRef, 
               getContext());
           XWikiAttachment att = (new AddAttachmentToDoc()).addAtachment(destDoc, 
              newAttOutStream.toByteArray(), attName, getContext());
           LOGGER.info("attachment='" + att.getFilename() + "', doc='" + att.getDoc(
               ).getDocumentReference() + "' size='" + att.getFilesize() + "'");
         } catch (IOException ioe) {
           LOGGER.error("Exception while unpacking zip", ioe);
         } catch (XWikiException xwe) {
           LOGGER.error("Exception while unpacking zip", xwe);
         } finally {
           if(newAttOutStream != null) {
             try {
               newAttOutStream.close();
             } catch (IOException ioe) {
               LOGGER.error("Could not close input stream.", ioe);
             }
           }
         }
       }
     } else {
       LOGGER.error("Source document which should contain zip file is null: [" 
           + zipSrcFile + "]");
     }
     LOGGER.info("END unzip: file='" + attName + "', cleaned name is '" + cleanName + "'");
     return cleanName;
   }
   
   boolean isZipFile(XWikiAttachment file) {
     return (file != null) && (file.getMimeType(getContext()).equalsIgnoreCase(
         ImageLibStrings.MIME_ZIP) || file.getMimeType(getContext()).equalsIgnoreCase(
         ImageLibStrings.MIME_ZIP_MICROSOFT));
   }
   
   boolean isImgFile(String fileName) {
     return (fileName != null) 
         && (fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_BMP)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_GIF)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPE)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPG)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPEG)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_PNG));
   }
   
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 }
