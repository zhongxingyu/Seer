 package org.jboss.pressgang.ccms.model;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.imageio.ImageIO;
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import org.apache.commons.codec.binary.Base64;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.imgscalr.Scalr;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.exceptions.CustomConstraintViolationException;
 import org.jboss.pressgang.ccms.model.utils.SVGIcon;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "LanguageImage", uniqueConstraints = @UniqueConstraint(columnNames = {"ImageFileID", "Locale"}))
 public class LanguageImage extends AuditedEntity implements java.io.Serializable {
     private static final Logger log = LoggerFactory.getLogger(LanguageImage.class);
     private static final long serialVersionUID = 1585978752264763594L;
     private static final String SVG_MIME_TYPE = "image/svg+xml";
     private static final String JPG_MIME_TYPE = "image/jpeg";
     private static final String GIF_MIME_TYPE = "image/gif";
     private static final String PNG_MIME_TYPE = "image/png";
     public static final String SELECT_ALL_QUERY = "SELECT languageImage FROM LanguageImage languageImage";
 
     /**
      * The dimensions of the generated thumbnail
      */
     private static final int THUMBNAIL_SIZE = 64;
     private Integer languageImageId;
     private ImageFile imageFile;
     private byte[] imageData;
     private byte[] thumbnail;
     private byte[] imageDataBase64;
     private String locale;
     private String originalFileName;
 
     private byte[] uiImageData;
     private String uiOriginalFileName;
 
     public LanguageImage() {
     }
 
     public LanguageImage(final String originalFileName, final byte[] imageData) {
         this.imageData = imageData;
         this.originalFileName = originalFileName;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "LanguageImageID", unique = true, nullable = false)
     public Integer getLanguageImageId() {
         return languageImageId;
     }
 
     public void setLanguageImageId(final Integer languageImageId) {
         this.languageImageId = languageImageId;
     }
 
     @Column(name = "OriginalFileName", length = 255)
     @Size(max = 255)
     public String getOriginalFileName() {
         return originalFileName;
     }
 
     public void setOriginalFileName(final String originalFileName) {
         this.originalFileName = originalFileName;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "ImageFileID", nullable = false)
     @NotNull
     public ImageFile getImageFile() {
         return imageFile;
     }
 
     public void setImageFile(ImageFile imageFile) {
         this.imageFile = imageFile;
     }
 
     @Column(name = "ImageDataBase64", columnDefinition = "mediumblob")
     public byte[] getImageDataBase64() {
         return imageDataBase64;
     }
 
     public void setImageDataBase64(final byte[] imageDataBase64) {
         this.imageDataBase64 = imageDataBase64;
     }
 
     @Transient
     public String getImageDataBase64String() {
         return imageDataBase64 == null ? "" : new String(imageDataBase64);
     }
 
     @Column(name = "ImageData", columnDefinition = "mediumblob")
     public byte[] getImageData() {
         return imageData;
     }
 
     public void setImageData(final byte[] imageData) {
         this.imageData = imageData;
     }
 
     @Column(name = "ThumbnailData", columnDefinition = "mediumblob")
     public byte[] getThumbnailData() {
         return thumbnail;
     }
 
     public void setThumbnailData(final byte[] thumbnail) {
         this.thumbnail = thumbnail;
     }
 
     @Transient
     public String getThumbnailDataString() {
         return thumbnail == null ? "" : new String(thumbnail);
     }
 
     private byte[] createImage(final boolean resize) {
         if (imageData == null) return null;
 
         try {
             BufferedImage outImage = null;
 
             if (getMimeType().equals(SVG_MIME_TYPE)) {
                 SVGIcon svgIcon = null;
                 if (resize) {
                     svgIcon = new SVGIcon(new ByteArrayInputStream(imageData), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
                 } else {
                     svgIcon = new SVGIcon(new ByteArrayInputStream(imageData));
                 }
 
                 outImage = new BufferedImage(svgIcon.getIconWidth(), svgIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
                 final Graphics2D g2d = outImage.createGraphics();
 
                 svgIcon.setBackgroundColour(Color.WHITE);
                 svgIcon.paintIcon(null, g2d, 0, 0);
                 g2d.dispose();
             } else {
                 final BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                 outImage = Scalr.resize(img, Scalr.Method.AUTOMATIC, THUMBNAIL_SIZE, THUMBNAIL_SIZE, Scalr.OP_ANTIALIAS);
             }
 
            // Determine the output format
            final String extension = getExtension();
            final String formatName = extension == null || extension.equalsIgnoreCase("svg") ? "JPG" : extension.toUpperCase();

             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(outImage, formatName, baos);
             final byte[] bytesOut = baos.toByteArray();
 
             return Base64.encodeBase64(bytesOut);
         } catch (final Exception ex) {
             log.error("An error creating an image thumbnail", ex);
         }
 
         return null;
     }
 
     @Transient
     public String getImageDataString() {
         if (imageData == null) return "";
         return new String(imageData);
     }
 
     @Override
     @Transient
     public Integer getId() {
         return languageImageId;
     }
 
     /**
      * Create the thumbnails, and make sure the parent imagefile is valid.
      *
      * @throws CustomConstraintViolationException
      *
      */
     @PrePersist
     @PreUpdate
     private void updateImageData() throws CustomConstraintViolationException {
         thumbnail = createImage(true);
         imageDataBase64 = Base64.encodeBase64(imageData);
 
         imageFile.validate();
     }
 
     @Column(name = "Locale", nullable = false)
     @NotNull(message = "languageimage.locale.notBlank")
     @NotBlank(message = "languageimage.locale.notBlank")
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 
     @Transient
     public String getMimeType() {
         final String extension = getExtension();
         if (extension != null) {
             if (extension.equalsIgnoreCase("JPG")) return JPG_MIME_TYPE;
             if (extension.equalsIgnoreCase("GIF")) return GIF_MIME_TYPE;
             if (extension.equalsIgnoreCase("PNG")) return PNG_MIME_TYPE;
             if (extension.equalsIgnoreCase("SVG")) return SVG_MIME_TYPE;
         }
 
         return "application/octet-stream";
     }
 
     @Transient
     public String getExtension() {
         final int lastPeriodIndex = originalFileName.lastIndexOf(".");
         if (lastPeriodIndex != -1 && lastPeriodIndex < originalFileName.length() - 1) {
             return originalFileName.substring(lastPeriodIndex + 1);
         }
 
         return null;
     }
 
     @Transient
     public byte[] getUiImageData() {
         return uiImageData;
     }
 
     /**
      * The UI will attempt to assign a null value if the file upload box does not have a file selected. This method is used to
      * ignore any null values, while passing through any legitimate file uploads. This means that the absence of a file in the
      * upload box does not indicate that no file should be assigned to the entity.
      *
      * @param uiImageData The image data uploaded through the UI
      */
     public void setUiImageData(byte[] uiImageData) {
         this.uiImageData = uiImageData;
         if (this.uiImageData != null) imageData = this.uiImageData;
     }
 
     @Transient
     public String getUiOriginalFileName() {
         return uiOriginalFileName;
     }
 
     /**
      * The UI will attempt to assign an empty value if the file upload box does not have a file selected. This method is used to
      * ignore any empty values, while passing through any legitimate file uploads. This means that the absence of a file in the
      * upload box does not indicate that no file should be assigned to the entity.
      *
      * @param uiImageData The image file name uploaded through the UI
      */
     public void setUiOriginalFileName(final String uiOriginalFileName) {
         this.uiOriginalFileName = uiOriginalFileName;
         if (this.uiOriginalFileName != null && !this.uiOriginalFileName.isEmpty()) originalFileName = this.uiOriginalFileName;
     }
 }
