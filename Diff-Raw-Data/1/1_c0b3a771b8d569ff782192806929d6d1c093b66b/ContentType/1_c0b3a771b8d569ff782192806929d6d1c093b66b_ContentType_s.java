 package fr.cg95.cvq.business.document;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang3.ArrayUtils;
 
 import fr.cg95.cvq.dao.hibernate.PersistentStringEnum;
 
 public final class ContentType extends PersistentStringEnum {
 
     private static final long serialVersionUID = 1L;
 
     public static final ContentType BMP = new ContentType("image/x-ms-bmp", "bmp");
     public static final ContentType GIF = new ContentType("image/gif", "gif");
     public static final ContentType JPEG = new ContentType("image/jpeg", "jpg");
     public static final ContentType OCTET_STREAM = new ContentType("application/octet-stream", "");
     public static final ContentType PDF = new ContentType("application/pdf", "pdf");
     public static final ContentType PNG = new ContentType("image/png", "png");
     public static final ContentType TIFF = new ContentType("image/tiff", "tiff");
 
     public static final ContentType[] allowedContentTypes = {
         BMP, GIF, JPEG, PDF, PNG, TIFF
     };
 
     public static final ContentType[] allContentTypes =
         ArrayUtils.add(allowedContentTypes, OCTET_STREAM);
 
     private String extension;
 
     /**
      * Prevent instantiation and subclassing with a private constructor.
      */
     private ContentType(final String type, final String extension) {
         super(type);
         this.extension = extension;
     }
 
     public ContentType() { /* public constructor for Hibernate */ }
 
     public static ContentType forString(String mimeType) {
         for (ContentType contentType : allowedContentTypes) {
             if (contentType.toString().equals(mimeType)) {
                 return contentType;
             }
         }
         return OCTET_STREAM;
     }
 
     public boolean isAllowed() {
         return ArrayUtils.contains(allowedContentTypes, this);
     }
 
     public String getExtension() {
         return extension;
     }
 }
