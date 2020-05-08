 package org.otherobjects.cms.model;
 
 import java.io.File;
 
 import org.otherobjects.cms.types.annotation.Property;
 import org.otherobjects.cms.types.annotation.PropertyType;
 import org.otherobjects.cms.types.annotation.Type;
 import org.otherobjects.cms.util.StringUtils;
 
 /**
  * Represents a content managed image object.
  * 
  * <p>Supported images types are JPEG, GIF, PNG, SVG.
  * 
  * <p>Does not contain the image data at any size but has a reference
  * <code>originalFileName</code> to the original image in the data store.
  * 
  * FIXME Add validation rules
  * FIXME Integrate this with OoResource
  * FIXME Sync with IPTC/DublinCore metadata
  * FIXME How can we extend this for individual sites?
  * 
  * @author rich
  */
 @Type(labelProperty = "label")
 public class CmsImage extends BaseNode
 {
     public static final String DATA_FILE_COLLECTION_NAME = "images";
     public static final String ORIGINALS_PATH = "/originals/";
 
     // Meta data
     private String label;
     private String description;
     private String keywords;
     private String copyright;
     private String mimeType;
 
     // Source information
     private String originalFileName;
     private Long originalWidth;
     private Long originalHeight;
     private String originalProvider;
     private String providerId;
 
     // Temporary holder for new and relacement files
     private File newFile;
     private String thumbnailPath;
 
     /**
      * FIXME Temp hack to allow thumbnails in Workbench interface.
      * 
      * @return
      */
     public String getThumbnailPath()
     {
         if (this.thumbnailPath != null)
             return this.thumbnailPath;
 
         if (getOriginalFileName() != null)
            return "/data" + getOriginalFileName().replaceAll("originals", "100x100%23FFFFFF");
         else
             return null;
     }
 
     /**
      * Generates default code based on image label and file extension.
      */
     @Override
     public String getCode()
     {
         return this.code != null ? this.code : StringUtils.generateUrlCode(getLabel()) + "." + getExtension();
     }
 
     /**
      * Needed for external image services.
      * 
      * @return
      */
     public void setThumbnailPath(String url)
     {
         this.thumbnailPath = url;
     }
 
     public double getAspectRatio()
     {
         return ((double) getOriginalWidth() / (double) getOriginalHeight());
     }
 
     @Override
     @Property(order = 10, required = true)
     public String getLabel()
     {
         return this.label;
     }
 
     @Override
     public void setLabel(String label)
     {
         this.label = label;
     }
 
     @Property(order = 20, type = PropertyType.TEXT)
     public String getDescription()
     {
         return this.description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     @Property(order = 30, type = PropertyType.TEXT)
     public String getKeywords()
     {
         return this.keywords;
     }
 
     public void setKeywords(String keywords)
     {
         this.keywords = keywords;
     }
 
     @Property(order = 40, type = PropertyType.TEXT)
     public String getCopyright()
     {
         return this.copyright;
     }
 
     public void setCopyright(String copyright)
     {
         this.copyright = copyright;
     }
 
     @Property(order = 70, required = true)
     public Long getOriginalWidth()
     {
         return this.originalWidth;
     }
 
     public void setOriginalWidth(Long originalWidth)
     {
         this.originalWidth = originalWidth;
     }
 
     @Property(order = 80, required = true)
     public Long getOriginalHeight()
     {
         return this.originalHeight;
     }
 
     public void setOriginalHeight(Long originalHeight)
     {
         this.originalHeight = originalHeight;
     }
 
     @Property(order = 100)
     public String getOriginalProvider()
     {
         return this.originalProvider;
     }
 
     public void setOriginalProvider(String originalProvider)
     {
         this.originalProvider = originalProvider;
     }
 
     public void setNewFile(File newFile)
     {
         this.newFile = newFile;
     }
 
     @Property(type = PropertyType.TEXT)
     public File getNewFile()
     {
         return this.newFile;
     }
 
     public String getFileName()
     {
         // FIXME Do this propertly once we have support for codeProperty
         return getCode();
     }
 
     @Property(order = 90, required = true)
     public String getOriginalFileName()
     {
         return originalFileName;
     }
 
     public void setOriginalFileName(String originalFileName)
     {
         this.originalFileName = originalFileName;
     }
 
     @Property(order = 110)
     public String getProviderId()
     {
         return providerId;
     }
 
     public void setProviderId(String providerId)
     {
         this.providerId = providerId;
     }
 
     /**
      * Returns mime type based on orginal file extension.
      * 
      * @return
      */
     public String getMimeType()
     {
         // TODO Should we validate mime types
         if (mimeType != null)
             return mimeType;
 
         String extension = getExtension();
 
         if (extension.equals("jpg"))
             return "image/jpeg";
         else if (extension.equals("png"))
             return "image/png";
         else if (extension.equals("svg"))
             return "image/svg+xml";
         else if (extension.equals("gif"))
             return "image/gif";
         else
             return "unknown";
     }
 
     /**
      * Returns extension of original file name. 
      * @return
      */
     private String getExtension()
     {
         String extension = getOriginalFileName().substring(getOriginalFileName().lastIndexOf(".") + 1);
         return extension;
     }
 
     public void setMimeType(String mimeType)
     {
         this.mimeType = mimeType;
     }
 
 }
