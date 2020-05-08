 package com.celements.photo.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.AttachmentReference;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.EntityReferenceResolver;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.common.classes.IClassCollectionRole;
 import com.celements.navigation.NavigationClasses;
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.image.GenerateThumbnail;
 import com.celements.web.classcollections.OldCoreClasses;
 import com.celements.web.plugin.cmd.AttachmentURLCommand;
 import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Attachment;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 @Component
 public class ImageService implements IImageService {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ImageService.class);
 
   @Requirement
   EntityReferenceResolver<String> stringRefResolver;
 
   @Requirement("celements.oldCoreClasses")
   IClassCollectionRole oldCoreClasses;
 
   @Requirement("celements.celNavigationClasses")
   IClassCollectionRole navigationClasses;
 
   @Requirement
   IWebUtilsService webUtilsService;
 
   NextFreeDocNameCommand nextFreeDocNameCmd;
 
   @Requirement
   Execution execution;
 
   AttachmentURLCommand attURLCmd;
   
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 
   private OldCoreClasses getOldCoreClasses() {
     return (OldCoreClasses) oldCoreClasses;
   }
 
   private NavigationClasses getNavigationClasses() {
     return (NavigationClasses) navigationClasses;
   }
 
   public BaseObject getPhotoAlbumObject(DocumentReference galleryDocRef
       ) throws XWikiException {
     XWikiDocument galleryDoc = getContext().getWiki().getDocument(galleryDocRef,
         getContext());
     BaseObject galleryObj = galleryDoc.getXObject(getOldCoreClasses(
         ).getPhotoAlbumClassRef(getContext().getDatabase()));
     return galleryObj;
   }
 
   public BaseObject getPhotoAlbumNavObject(DocumentReference galleryDocRef
       ) throws XWikiException, NoGalleryDocumentException {
     XWikiDocument galleryDoc = getContext().getWiki().getDocument(galleryDocRef,
         getContext());
     BaseObject navObj = galleryDoc.getXObject(getNavigationClasses(
         ).getNavigationConfigClassRef(getContext().getDatabase()));
     if (navObj == null) {
       throw new NoGalleryDocumentException();
     }
     return navObj;
   }
 
   public SpaceReference getPhotoAlbumSpaceRef(DocumentReference galleryDocRef
       ) throws NoGalleryDocumentException {
     try {
       String spaceName = getPhotoAlbumNavObject(galleryDocRef).getStringValue(
           NavigationClasses.MENU_SPACE_FIELD);
       return new SpaceReference(spaceName, webUtilsService.getWikiRef(
           galleryDocRef));
     } catch (XWikiException exp) {
       LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
     }
     return null;
   }
 
   public int getPhotoAlbumMaxHeight(DocumentReference galleryDocRef
       ) throws NoGalleryDocumentException {
     try {
       return getPhotoAlbumObject(galleryDocRef).getIntValue("height2");
     } catch (XWikiException exp) {
       LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
     }
     return 2000;
   }
 
   public int getPhotoAlbumMaxWidth(DocumentReference galleryDocRef
       ) throws NoGalleryDocumentException {
     try {
       return getPhotoAlbumObject(galleryDocRef).getIntValue("photoWidth");
     } catch (XWikiException exp) {
       LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
     }
     return 2000;
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
     ImageDimensions imageDimensions = null;
     GenerateThumbnail genThumbnail = new GenerateThumbnail();
     InputStream imageInStream = null;
     try {
       imageInStream = theAttachment.getContentInputStream(getContext());
       imageDimensions = genThumbnail.getImageDimensions(imageInStream);
     } finally {
       if(imageInStream != null) {
         try {
           imageInStream.close();
         } catch (IOException ioe) {
           LOGGER.error("Error closing InputStream.", ioe);
         }
       } else {
         imageDimensions = new ImageDimensions();
       }
     }
     return imageDimensions;
   }
   
   /**
    * getRandomImages computes a set of <num> randomly chosen images
    * from the given AttachmentList. It chooses the Images without duplicates if
    * possible.
    */
   public List<Attachment> getRandomImages(DocumentReference galleryRef, int num) {
     try {
       Document imgDoc = getContext().getWiki().getDocument(galleryRef, getContext()
           ).newDocument(getContext());
       List<Attachment> allImagesList = webUtilsService.getAttachmentListSorted(imgDoc,
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
 
   private NextFreeDocNameCommand getNextFreeDocNameCmd() {
     if (this.nextFreeDocNameCmd == null) {
       this.nextFreeDocNameCmd = new NextFreeDocNameCommand();
     }
     return this.nextFreeDocNameCmd;
   }
 
   private AttachmentURLCommand getAttURLCmd() {
     if (attURLCmd == null) {
       attURLCmd = new AttachmentURLCommand();
     }
     return attURLCmd;
   }
 
   public boolean checkAddSlideRights(DocumentReference galleryDocRef) {
     try {
       DocumentReference newSlideDocRef = getNextFreeDocNameCmd().getNextTitledPageDocRef(
           getPhotoAlbumSpaceRef(galleryDocRef).getName(), "Testname", getContext());
       String newSlideDocFN = webUtilsService.getRefDefaultSerializer().serialize(
           newSlideDocRef);
       return (getContext().getWiki().getRightService().hasAccessLevel("edit",
           getContext().getUser(), newSlideDocFN, getContext()));
     } catch (XWikiException exp) {
       LOGGER.error("failed to checkAddSlideRights for [" + galleryDocRef + "].", exp);
     } catch (NoGalleryDocumentException exp) {
       LOGGER.debug("failed to checkAddSlideRights for no gallery document ["
           + galleryDocRef + "].", exp);
     }
     return false;
   }
 
   public boolean addSlideFromTemplate(DocumentReference galleryDocRef,
       String slideBaseName, String attFullName) {
     try {
       DocumentReference slideTemplateRef = getImageSlideTemplateRef();
       String gallerySpaceName = getPhotoAlbumSpaceRef(galleryDocRef).getName();
       DocumentReference newSlideDocRef = getNextFreeDocNameCmd().getNextTitledPageDocRef(
           gallerySpaceName, slideBaseName, getContext());
       if (getContext().getWiki().copyDocument(slideTemplateRef, newSlideDocRef, true,
           getContext())) {
         XWikiDocument newSlideDoc = getContext().getWiki().getDocument(newSlideDocRef,
             getContext());
         newSlideDoc.setDefaultLanguage(webUtilsService.getDefaultLanguage(
             gallerySpaceName));
         Date creationDate = new Date();
         newSlideDoc.setLanguage("");
         newSlideDoc.setCreationDate(creationDate);
         newSlideDoc.setContentUpdateDate(creationDate);
         newSlideDoc.setDate(creationDate);
         newSlideDoc.setCreator(getContext().getUser());
         newSlideDoc.setAuthor(getContext().getUser());
         newSlideDoc.setTranslation(0);
         String imgURL = getAttURLCmd().getAttachmentURL(attFullName, "download",
             getContext());
         String resizeParam = "celwidth=" + getPhotoAlbumMaxWidth(galleryDocRef)
             + "&celheight=" + getPhotoAlbumMaxHeight(galleryDocRef);
         String fullImgURL = imgURL + ((imgURL.indexOf("?") < 0)?"?":"&") + resizeParam;
         VelocityContext vcontext = (VelocityContext)getContext().get("vcontext");
         vcontext.put("imageURL", fullImgURL);
         vcontext.put("attFullName", attFullName);
         Map<String, String> metaTagMap = new HashMap<String, String>();
         DocumentReference attDocRef = webUtilsService.resolveDocumentReference(
             attFullName.replaceAll("^(.*);.*$", "$1"));
         XWikiDocument attDoc = getContext().getWiki().getDocument(attDocRef, getContext()
             );
         DocumentReference tagClassRef = webUtilsService.resolveDocumentReference(
             "Classes.PhotoMetainfoClass");
         List<BaseObject> metaObjs = attDoc.getXObjects(tagClassRef);
         if(metaObjs != null) {
           for(BaseObject tag : metaObjs) {
             if(tag != null) {
               metaTagMap.put(tag.getStringValue("name"), tag.getStringValue("description")
                   );
             }
           }
         }
         vcontext.put("metaTagMap", metaTagMap);
         DocumentReference slideContentRef = new DocumentReference(getContext(
             ).getDatabase(), "Templates", "ImageSlideImportContent");
         String slideContent = webUtilsService.renderInheritableDocument(slideContentRef, 
             getContext().getLanguage(), webUtilsService.getDefaultLanguage());
         newSlideDoc.setContent(slideContent);
         getContext().getWiki().saveDocument(newSlideDoc, "add default image slide"
             + " content", true, getContext());
         return true;
       } else {
         LOGGER.warn("failed to copy slideTemplateRef [" + slideTemplateRef
             + "] to new slide doc [" + newSlideDocRef + "].");
       }
     } catch (NoGalleryDocumentException exp) {
       LOGGER.error("failed to addSlideFromTemplate because no gallery doc.", exp);
     } catch (XWikiException exp) {
       LOGGER.error("failed to addSlideFromTemplate.", exp);
     }
     return false;
   }
 
   public DocumentReference getImageSlideTemplateRef() {
     DocumentReference slideTemplateRef = new DocumentReference(
         getContext().getDatabase(), "ImageGalleryTemplates", "NewImageGallerySlide");
     if(!getContext().getWiki().exists(slideTemplateRef, getContext())) {
       slideTemplateRef = new DocumentReference("celements2web", "ImageGalleryTemplates",
           "NewImageGallerySlide");
     }
     return slideTemplateRef;
   }
 
   public Map<String, String> getImageURLinAllAspectRatios(XWikiAttachment ximage) {
     ImageDimensions dim = null;
     try {
       dim = (new GenerateThumbnail()).getThumbnailDimensions(ximage.getContentInputStream(
           getContext()), -1, -1, false, null);
     } catch (XWikiException xwe) {
       LOGGER.error("Exception reading image dimensions", xwe);
     }
     Map<String, String> urlMap = new HashMap<String, String>();
     if(dim != null) {
      String baseURL = getContext().getDoc().getExternalAttachmentURL(
           ximage.getFilename(), "download", getContext());
       if(baseURL.indexOf("?") < 0) {
         baseURL += "?";
       } else if(!baseURL.endsWith("&")) {
         baseURL += "&";
       }
       urlMap.put("1:1", baseURL + getFixedAspectURL(dim, 1, 1));
       urlMap.put("3:4", baseURL + getFixedAspectURL(dim, 3, 4));
       urlMap.put("4:3", baseURL + getFixedAspectURL(dim, 4, 3));
       urlMap.put("16:9", baseURL + getFixedAspectURL(dim, 16, 9));
       urlMap.put("16:10", baseURL + getFixedAspectURL(dim, 16, 10));
     }
     return urlMap;
   }
 
   String getFixedAspectURL(ImageDimensions dim, int xFact, int yFact) {
     double width = dim.getWidth();
     double height = dim.getHeight();
     double isAspRatio = width / (double)height;
     double targetAspRatio = xFact / (double)yFact;
     double epsylon = 0.00001;
     String urlParams = "";
     //only crop if dim does not matche target aspect ratio
     if(Math.abs(isAspRatio - targetAspRatio) > epsylon) {
       if(isAspRatio < targetAspRatio) {
         urlParams += "cropX=0&cropW=" + (int)width;
         int newHeight = (int)Math.floor(width * (1 / targetAspRatio));
         int top = (int)Math.floor((height - (double)newHeight) / 2);
         urlParams += "&cropY=" + top + "&cropH=" + newHeight;
       } else {
         int newWidth = (int)Math.floor(height * targetAspRatio);
         int left = (int)Math.floor((width - (double)newWidth) / 2);
         urlParams += "cropX=" + left + "&cropW=" + newWidth;
         urlParams += "&cropY=0&cropH=" + (int)height;
       }
     }
     return urlParams;
   }
 }
