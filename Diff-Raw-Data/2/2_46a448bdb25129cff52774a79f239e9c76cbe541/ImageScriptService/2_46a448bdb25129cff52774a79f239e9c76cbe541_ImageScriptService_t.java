 package com.celements.photo.service;
 
 import java.net.URL;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.AttachmentReference;
 import org.xwiki.script.service.ScriptService;
 
 import com.celements.photo.container.ImageDimensions;
 import com.celements.web.sajson.Builder;
 import com.celements.web.service.CelementsWebScriptService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Attachment;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.web.XWikiURLFactory;
 
 @Component("celementsphoto")
 public class ImageScriptService implements ScriptService {
 
   public static final String IMAGE_FILE_SIZE = "fileSize";
   public static final String IMAGE_HEIGHT = "maxHeight";
   public static final String IMAGE_WIDTH = "maxWidth";
   public static final String IMAGE_ATT_VERSION = "attversion";
   public static final String IMAGE_FILENAME = "filename";
   public static final String IMAGE_SRC = "src";
   public static final String IMAGE_CHANGED_BY = "lastChangedBy";
   public static final String IMAGE_MIME_TYPE = "mimeType";
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ImageScriptService.class);
 
   @Requirement
   IImageService imageService;
 
   @Requirement("celementsweb")
   ScriptService celementsService;
 
   private CelementsWebScriptService getCelWebService() {
     return (CelementsWebScriptService) celementsService;
   }
 
   @Requirement
   Execution execution;
 
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 
   public void addImage(Builder jsonBuilder, Attachment imgAttachment) {
     Document theDoc = imgAttachment.getDocument();
     XWikiURLFactory urlFactory = getContext().getURLFactory();
     URL theAttUrl = urlFactory.createAttachmentURL(imgAttachment.getFilename(),
         theDoc.getSpace(), theDoc.getName(), "download", "", getContext());
     jsonBuilder.openDictionary();
     jsonBuilder.addStringProperty(IMAGE_SRC, urlFactory.getURL(theAttUrl, getContext()));
     jsonBuilder.addStringProperty(IMAGE_FILENAME, imgAttachment.getFilename());
     jsonBuilder.addStringProperty(IMAGE_ATT_VERSION, imgAttachment.getVersion());
     jsonBuilder.addStringProperty(IMAGE_CHANGED_BY, getContext().getWiki(
         ).getLocalUserName(imgAttachment.getAuthor(), null, false, getContext()));
     AttachmentReference imgRef = new AttachmentReference(imgAttachment.getFilename(),
         theDoc.getDocumentReference());
     try {
       ImageDimensions imgDim = imageService.getDimension(imgRef);
       jsonBuilder.openProperty(IMAGE_HEIGHT);
       jsonBuilder.addInteger((int)Math.floor(imgDim.getHeight()));
       jsonBuilder.openProperty(IMAGE_WIDTH);
       jsonBuilder.addInteger((int)Math.floor(imgDim.getWidth()));
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get image dimensions for image [" + imgRef + "].", exp);
     }
     jsonBuilder.addStringProperty(IMAGE_FILE_SIZE, getCelWebService(
        ).getHumanReadableSize(imgAttachment.getFilesize(), true));
     jsonBuilder.addStringProperty(IMAGE_MIME_TYPE, imgAttachment.getMimeType());
     jsonBuilder.closeDictionary();
   }
 
   public ImageDimensions getDimension(String imageFullName) {
     try {
       return imageService.getDimension(imageFullName);
     } catch (XWikiException exp) {
       LOGGER.warn("Failed to getDimension for [" + imageFullName + "].", exp);
     }
     return null;
   }
 
 }
