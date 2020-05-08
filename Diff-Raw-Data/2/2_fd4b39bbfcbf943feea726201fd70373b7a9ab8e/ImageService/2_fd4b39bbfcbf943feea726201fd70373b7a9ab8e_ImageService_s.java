 package com.celements.photo.service;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.AttachmentReference;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.EntityReferenceResolver;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.image.GenerateThumbnail;
 import com.celements.web.service.IWebUtilsService;
 import com.celements.web.utils.WebUtils;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Attachment;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.web.Utils;
 
 @Component
 public class ImageService implements IImageService {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ImageService.class);
 
   @Requirement
   Execution execution;
 
   @Requirement
   EntityReferenceResolver<String> stringRefResolver;
 
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 
   private DocumentReference getDocRefFromFullName(String collDocName) {
     DocumentReference eventRef = new DocumentReference(stringRefResolver.resolve(
         collDocName, EntityType.DOCUMENT));
     eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
     LOGGER.debug("getDocRefFromFullName: for [" + collDocName + "] got reference ["
         + eventRef + "].");
     return eventRef;
   }
 
   public ImageDimensions getDimension(String imageFullName)
       throws XWikiException {
     String fullName = imageFullName.split(";")[0];
     String imageFileName = imageFullName.split(";")[1];
     DocumentReference docRef = getDocRefFromFullName(fullName);
     AttachmentReference imgRef = new AttachmentReference(imageFileName, docRef);
     return getDimension(imgRef);
   }
 
   public ImageDimensions getDimension(AttachmentReference imgRef) throws XWikiException {
     DocumentReference docRef = (DocumentReference) imgRef.getParent();
     XWikiDocument theDoc = getContext().getWiki().getDocument(docRef, getContext());
     XWikiAttachment theAttachment = theDoc.getAttachment(imgRef.getName());
     byte[] data;
     try {
       data = theAttachment.getContent(getContext());
     } catch (XWikiException exp) {
       LOGGER.warn("getDimension Image Attachment content [" + theAttachment.getFilename()
           + "] not found.", exp);
       Object[] args = { theAttachment.getFilename() };
       throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
           XWikiException.ERROR_XWIKI_APP_ATTACHMENT_NOT_FOUND,
           "Attachment content {0} not found", exp, args);
     }
     GenerateThumbnail genThumbnail = new GenerateThumbnail();
     ByteArrayInputStream imageInStream = new ByteArrayInputStream(data);
     ImageDimensions imageDimensions = genThumbnail.getImageDimensions(imageInStream);
     imageInStream.reset();
     return imageDimensions;
   }
   
   /**
    * getRandomImages computes a set of <num> randomly chosen images
   * from the given AttachmentList. It chooses the Images without dubilcats if
    * possible.
    */
   public List<Attachment> getRandomImages(DocumentReference galleryRef, int num) {
     try {
       Document imgDoc = getContext().getWiki().getDocument(galleryRef, getContext()
           ).newDocument(getContext());
       List<Attachment> allImagesList = getWebUtilsService().getAttachmentListSorted(imgDoc,
           "AttachmentAscendingNameComparator", true);
       if (allImagesList.size() > 0) {
         List<Attachment> preSetImgList = prepareMaxCoverSet(num, allImagesList);
         List<Attachment> imgList = new ArrayList<Attachment>(num);
         Random rand = new Random();
         for (int i=1; i<=num ; i++) {
           int nextimg = rand.nextInt(preSetImgList.size());
           imgList.add(preSetImgList.remove(nextimg));
         }
         return imgList;
       }
     } catch (XWikiException e) {
       LOGGER.error(e);
     }
     return Collections.emptyList();
   }
   
   <T> List<T> prepareMaxCoverSet(int num, List<T> allImagesList) {
     List<T> preSetImgList = new Vector<T>(num);
     preSetImgList.addAll(allImagesList);
     for(int i=2; i <= coveredQuotient(allImagesList.size(), num); i++) {
       preSetImgList.addAll(allImagesList);
     }
     return preSetImgList;
   }
   
   int coveredQuotient(int divisor, int dividend) {
     if (dividend >= 0) {
       return ( (dividend + divisor - 1) / divisor);
     } else {
       return (dividend / divisor);
     }
   }
   
   private IWebUtilsService getWebUtilsService() {
     return Utils.getComponent(IWebUtilsService.class);
   }
 
 }
