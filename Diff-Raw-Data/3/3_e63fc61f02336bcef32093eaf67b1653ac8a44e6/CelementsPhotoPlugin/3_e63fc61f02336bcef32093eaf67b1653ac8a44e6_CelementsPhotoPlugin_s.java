 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.photo.plugin;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.container.ImageLibStrings;
 import com.celements.photo.container.ImageStrings;
 import com.celements.photo.container.Metadate;
 import com.celements.photo.container.PhotoAlbumClass;
 import com.celements.photo.container.PhotoImageClass;
 import com.celements.photo.container.PhotoMetainfoClass;
 import com.celements.photo.image.GenerateThumbnail;
 import com.celements.photo.image.Image;
 import com.celements.photo.image.Thumbnail;
 import com.celements.photo.metadata.Metainfo;
 import com.celements.photo.plugin.cmd.ComputeImageCommand;
 import com.celements.photo.service.IImageService;
 import com.celements.photo.utilities.AddAttachmentToDoc;
 import com.celements.photo.utilities.ImportFileObject;
 import com.celements.photo.utilities.Unzip;
 import com.drew.metadata.MetadataException;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Api;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
 import com.xpn.xwiki.plugin.XWikiPluginInterface;
 import com.xpn.xwiki.web.Utils;
 
 public class CelementsPhotoPlugin extends XWikiDefaultPlugin {
   
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       CelementsPhotoPlugin.class);
 
   /**
    * The image formats supported by the image plugin
    */
   public enum SupportedFormat
   {
       JPG(1, "image/jpg"),
       JPEG(1, "image/jpeg"),
       PNG(2, "image/png"),
       GIF(3, "image/gif"),
       BMP(4, "image/bmp");//,
 //      EPS(4, "application/postscript"),
 //      PDF(4, "application/pdf"),
 //      PSD(4, "image/x-photoshop"),
 //      TIF(4, "image/tiff");
 
       /**
        * The mime type associated to the supported format
        */
       private String mimeType;
 
       /**
        * A integer code used to generate the image cache key
        */
       private int code;
 
       SupportedFormat(int code, String mimeType)
       {
           this.mimeType = mimeType;
           this.code = code;
       }
 
       public int getCode()
       {
           return this.code;
       }
 
       public String getMimeType()
       {
           return this.mimeType;
       }
   }
   
   private Image image;
   private Thumbnail thumbnail;
   private Metainfo metainfo;
 
   private ComputeImageCommand computeImgCmd;
   
   // PLUGIN .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.
   public CelementsPhotoPlugin(String name, String className, XWikiContext context) {
     super(name,className,context);
     image = new Image();
     thumbnail = new Thumbnail();
     metainfo = new Metainfo();
     init(context);
   }
   
   public String getName() {
     return "celementsphoto";
   }
   
   public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
     return new CelementsPhotoPluginAPI((CelementsPhotoPlugin) plugin, context);
   }
   
   public void virtualInit(XWikiContext context){
    //TODO move to where it's first needed, not all are using these classes
    // set up the wiki for the photo plugin
     try{
       generatePhotoMetainfoClass(context);
       generatePhotoAlbumClass(context);
       generatePhotoImageClass(context);
     } catch(XWikiException xe){
       //no problem, class can be generated later or manually
       LOGGER.error(xe);
     }
   }
   
   public void init(XWikiContext context) {
     super.init(context);
   }
   
   public void flushCache() {
     //DO NOT FLUSH IMAGE CACHE, BECAUSE IT IS ON DISK AND DOES NOT HELP TO FREE MEMORY!
   }
   
   public void flushImageCache() {
     getComputeImgCmd().flushCache();
   }
   
   public XWikiAttachment downloadAttachment(XWikiAttachment attachment, 
       XWikiContext context) {
 
 //TODO check why in Debian resize of "problematic" images uses a lot more time than 
 //     resize AND crop
     if (this.isSupportedImageFormat(attachment.getMimeType(context))) {
       String sheight = context.getRequest().getParameter("celheight");
       String swidth = context.getRequest().getParameter("celwidth");
       String copyright = context.getRequest().getParameter("copyright");
       String watermark = context.getRequest().getParameter("watermark");
       Color defaultBg = null;
       String defaultBgString = context.getRequest().getParameter("background");
       return getComputeImgCmd().computeImage(attachment, context, attachment, sheight,
           swidth, copyright, watermark, defaultBg, defaultBgString);
     }
     return attachment;
   }
 
   ComputeImageCommand getComputeImgCmd() {
     if (computeImgCmd == null) {
       computeImgCmd = new ComputeImageCommand();
     }
     return computeImgCmd;
   }
 
   /**
    * @return true if the passed mime type is supported by the plugin, false otherwise.
    */
   public boolean isSupportedImageFormat(String mimeType)
   {
     for (SupportedFormat f : SupportedFormat.values()) {
       if (f.getMimeType().equals(mimeType)) {
         return true;
       }
     }
     return false;
   }
   
   // IMAGE .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
   /**
    * Returns an array of ImageStrings for all images in the specified album.
    * 
    * @see com.celements.photo.plugin.container.ImageStrings
    * @param doc XWikiDocument of the album.
    * @param width Desired maximum width of the thumbnails (aspect ratio is maintained).
    * @param height Desired maximum height of the thumbnails (aspect ratio is maintained).
    * @param context XWikiContext
    * @return Array of ImageStrings.
    * @throws XWikiException
    * @throws IOException
    */
   public ImageStrings[] getImageList(XWikiDocument doc, int width, int height, XWikiContext context) throws XWikiException, IOException{
     return image.getImageList(doc, width, height, thumbnail, context);
   }
   
   /**
    * Returns an array of ImageStrings for all images in the specified album,
    * excluding the link to a thumbnail. This method's primar use is to get
    * the image's id.
    * 
    * @see com.celements.photo.plugin.container.ImageStrings
    * @param doc XWikiDocument of the album.
    * @param context XWikiContext
    * @return Array of ImageStrings.
    * @throws XWikiException
    * @throws IOException
    */
   public ImageStrings[] getImageListExclThumbs(XWikiDocument doc, XWikiContext context) throws XWikiException, IOException{
     return image.getImageListExclThumbs(doc, context);
   }
 
   /**
    * Returns wether the specified image is marked as deleted or not.
    * 
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param context XWikiContext
    * @return true if the image is tagged as deleted.
    * @throws XWikiException
    */
   public boolean isImageDeleted(XWikiDocument doc, String id, XWikiContext context) throws XWikiException{
     return this.image.isDeleted(doc, id, context);
   }
 
   /**
    * Set the "deleted" tag for the image to the specified value.
    * 
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param deleted true to tag the image as deleted.
    * @param XWikiContext
    * @throws XWikiException
    */
   public void setImageDeleted(XWikiDocument doc, String id, boolean deleted, XWikiContext context) throws XWikiException{
     this.image.setDeleted(doc, id, deleted, context);
   }
 
   // THUMBNAIL .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:  
   /**
    * Returns the URL to the thumbnail of a certain image in the specified
    * size. If the thumbnail does not exist it is created.
    * 
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param width Desired width for the thumb
    * @param height Desired height for the thumb
    * @param context XWikiContext
    * @return The download URL for the thumb
    * @throws XWikiException
    * @throws IOException
    */
   public String getThumbnailUrl(XWikiDocument doc, String id, int width, int height, XWikiContext context) throws XWikiException, IOException{
     return thumbnail.getUrl(doc, id, width, height, context);
   }
 
   // METADATA .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.
   /**
    * Returns the specified metatag. If there is no metatag with the specified
    * name an empty metadate is returned.
    * 
    * @see com.celements.photo.plugin.container.Metadate
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param tag The name of the desired metatag.
    * @param context XWikiContext
    * @return The specified metatag as a Metadate object.
    * @throws XWikiException
    * @throws MetadataException
    * @throws IOException
    */
   public Metadate getMetatag(XWikiDocument doc, String id, String tag, XWikiContext context) throws XWikiException, MetadataException, IOException{
     return metainfo.getTag(doc, id, tag, context);
   }
 
   /**
    * Returns all metatags contained in the image, excluding "Unknown tag" tags.
    * 
    * @see com.celements.photo.plugin.container.Metadate
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param XWikiContext
    * @return Array of Metadate objects.
    * @throws XWikiException
    * @throws MetadataException
    * @throws IOException
    */
   public Metadate[] getMetadata(XWikiDocument doc, String id, XWikiContext context) throws XWikiException, MetadataException, IOException{
     return metainfo.getMetadataWithCondition(doc, id, ImageLibStrings.METATAG_UNKNOWN_TAG, context);
   }
 
   /**
    * Returns all metatags contained in the image, including "Unknown tag" tags.
    * 
    * @see com.celements.photo.plugin.container.Metadate
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param XWikiContext
    * @return Array of Metadate objects.
    * @throws XWikiException
    * @throws MetadataException
    * @throws IOException
    */
   public Metadate[] getMetadataFull(XWikiDocument doc, String id, XWikiContext context) throws XWikiException, MetadataException, IOException{
     return metainfo.getMetadataWithCondition(doc, id, "", context);
   }
   
   // DATA MANIPULATION .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
   /**
    * Deletes the document with the metadata attached (only the metadata 
    * extracted from the image is deleted, but not the data generated by 
    * CelementsPhotoPlugin).
    * 
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param context XWikiContext
    * @throws XWikiException
    * @throws IOException
    */
   public void forceClearMetadata(XWikiDocument doc, String id, XWikiContext context) throws XWikiException, IOException{
     XWikiDocument metadataDoc = context.getWiki().getDocument(ImageLibStrings.getPhotoSpace(doc), doc.getName() + ImageLibStrings.DOCUMENT_SEPARATOR_IMAGE + id, context);
     context.getWiki().deleteDocument(metadataDoc, context);
   }
   
   // CLASSES .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
   /**
    * Generates the PhotoMetainfoClass with the fields 'name' and 'description'.
    * 
    * @param context The XWikiContext used to get the xwiki and save.
    * @return A BaseObject of the PhotoMetainfoClass
    * @throws XWikiException
    */
   public BaseClass generatePhotoMetainfoClass(XWikiContext context) throws XWikiException{
     return (new PhotoMetainfoClass()).getNewPhotoMetainfoClass(context);
   }  
   
   /**
    * Generates the PhotoAlbumClass with fields for the data space, watermark 
    * and copyright.
    * 
    * @param context The XWikiContext used to get the xwiki and save.
    * @return A BaseObject of the PhotoAlbumClass
    * @throws XWikiException
    */
   public BaseClass generatePhotoAlbumClass(XWikiContext context) throws XWikiException{
     return (new PhotoAlbumClass()).getNewPhotoAlbumClass(context);
   }  
   
   /**
    * Generates the PhotoImageClass with fields for different fields to cash
    * information about the image.
    * 
    * @param context The XWikiContext used to get the xwiki and save.
    * @return A BaseObject of the PhotoImageClass
    * @throws XWikiException
    */
   public BaseClass generatePhotoImageClass(XWikiContext context) throws XWikiException{
     return (new PhotoImageClass()).getNewPhotoImageClass(context);
   }  
   
   /**
    * Get a List of all attachments in the specified archive and the suggested 
    * action when importing.
    * 
    * @param importFile Zip archive to check the files for the existence in the gallery.
    * @param galleryDoc Gallery Document to check for the files.
    * @param context XWikiContext
    * @return List of {@link ImportFileObject} for each file.
    * @throws XWikiException
    */
   public List<ImportFileObject>getAttachmentFileListWithActions(XWikiAttachment importFile, XWikiDocument galleryDoc, XWikiContext context) throws XWikiException{
     List<ImportFileObject> resultList = new ArrayList<ImportFileObject>();
     
     if(importFile != null){
       if(isZipFile(importFile, context)){
         List<String> fileList = (new Unzip()).getZipContentList(importFile.getContent(context));
         
         String fileSep = System.getProperty("file.separator");
         for (Iterator<String> fileIterator = fileList.iterator(); fileIterator.hasNext();) {
           String fileName = (String) fileIterator.next();
           if(!fileName.endsWith(fileSep)
               && !fileName.startsWith(".") && !fileName.contains(fileSep + ".")){
             ImportFileObject file = new ImportFileObject(fileName, getActionForFile(fileName, galleryDoc, context));
             resultList.add(file);
           }
         }
       } else if(isImgFile(importFile, context)){
         ImportFileObject file = new ImportFileObject(importFile.getFilename(), getActionForFile(importFile.getFilename(), galleryDoc, context));
         resultList.add(file);
       }
     } else{
       LOGGER.error("zipFile='null' - galleryDoc='" + galleryDoc.getFullName() + "'");
     }
     
     return resultList;
   }
 
   /**
    * For a given filename return if, in the specified gallery, its import 
    * should be added, overwritten or skiped.
    * 
    * @param fileName Filename of the file to check.
    * @param galleryDoc Document of the gallery to check if the file already exists.
    * @return action when importing: -1 skip, 0 overwrite, 1 add
    */
   private short getActionForFile(String fileName, XWikiDocument galleryDoc, XWikiContext context) {
     short action = ImportFileObject.ACTION_SKIP;
     if(isImgFile(fileName)){
       fileName = fileName.replace(System.getProperty("file.separator"), ".");
       fileName = context.getWiki().clearName(fileName, false, true, context);
       XWikiAttachment attachment = galleryDoc.getAttachment(fileName);
       if(attachment == null){
         action = ImportFileObject.ACTION_ADD;
       } else{
         action = ImportFileObject.ACTION_OVERWRITE;
       }
     }
     
     return action;
   }
 
   /**
    * Get a specified image file in a zip archive, extract it, change it to the 
    * desired size and save it as an attachment to the given page.
    * 
    * @param zipFile File containing the image to extract.
    * @param unzipFileName Filename of the image to extract.
    * @param attachToDoc Document to attach the extracted and resized image.
    * @param width Width (max - aspect ratio is maintained) to resize the image to.
    * @param height Height (max - aspect ratio is maintained) to resize the image to.
    * @param context XWikiContezt
    * @throws XWikiException
    */
   public void unzipFileToAttachment(XWikiAttachment zipFile, String unzipFileName, XWikiDocument attachToDoc,
       int width, int height, XWikiContext context) throws XWikiException {
     LOGGER.info("START: zip='" + zipFile.getFilename() + "' file='" + unzipFileName + "' gallery='" + attachToDoc + "' " +
         "width='" + width + "' height='" + height + "'");
     ByteArrayInputStream imgFullSize = null;
     ByteArrayOutputStream out = null;
     try {
       if(isZipFile(zipFile, context)){
         imgFullSize = new ByteArrayInputStream(
           (new Unzip()).getFile(zipFile.getContent(context), unzipFileName).toByteArray());
       } else if(isImgFile(zipFile, context)){
         imgFullSize = new ByteArrayInputStream(zipFile.getContent(context));
       }
       //TODO is there a better way to find the mime type of the file in the in stream?
       //      -> look at http://tika.apache.org/ or maybe in image magic?
       String mimeType = "png";
       if((unzipFileName.lastIndexOf('.') > -1) && (!unzipFileName.endsWith("."))) {
         mimeType = unzipFileName.substring(unzipFileName.lastIndexOf('.') + 1);
       }
       LOGGER.debug("unzip mimetype is " + mimeType);
       out = new ByteArrayOutputStream();
       ImageDimensions id = (new GenerateThumbnail()).createThumbnail(imgFullSize, out, 
           width, height, null, null, mimeType, null);
       LOGGER.info("width='" + id.width + "' height='" + id.height + "'");
       LOGGER.info("output stream size: " + out.size());
       unzipFileName = unzipFileName.replace(System.getProperty("file.separator"), ".");
       unzipFileName = context.getWiki().clearName(unzipFileName, false, true, context);
       XWikiAttachment att = (new AddAttachmentToDoc()).addAtachment(attachToDoc, out.toByteArray(), unzipFileName, context);
       LOGGER.info("attachment='" + att.getFilename() + "', gallery='" + att.getDoc().getFullName() + "' size='" + att.getFilesize() + "'");
     } catch (IOException e) {
       LOGGER.error(e);
     } finally {
       if(imgFullSize != null) {
         try {
           imgFullSize.close();
         } catch (IOException ioe) {
           LOGGER.error("Could not close input stream.", ioe);
         }
       }
       if(out != null) {
         try {
           out.close();
         } catch (IOException ioe) {
           LOGGER.error("Could not close output stream.", ioe);
         }
       }
     }
     LOGGER.info("END file='" + unzipFileName + "'");
   }
   
   private boolean isZipFile(XWikiAttachment file, XWikiContext context) {
     return file.getMimeType(context).equalsIgnoreCase(ImageLibStrings.MIME_ZIP) 
         || file.getMimeType(context).equalsIgnoreCase(ImageLibStrings.MIME_ZIP_MICROSOFT);
   }
   
   private boolean isImgFile(XWikiAttachment file, XWikiContext context) {
     return file.getMimeType(context).equalsIgnoreCase("image/" + ImageLibStrings.MIME_BMP)
         || file.getMimeType(context).equalsIgnoreCase("image/" + ImageLibStrings.MIME_GIF)
         || file.getMimeType(context).equalsIgnoreCase("image/" + ImageLibStrings.MIME_JPE)
         || file.getMimeType(context).equalsIgnoreCase("image/" + ImageLibStrings.MIME_JPG)
         || file.getMimeType(context).equalsIgnoreCase("image/" + ImageLibStrings.MIME_JPEG)
         || file.getMimeType(context).equalsIgnoreCase("image/" + ImageLibStrings.MIME_PNG);
   }
 
   private boolean isImgFile(String fileName) {
     return fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_BMP)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_GIF)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPE)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPG)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPEG)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_PNG);
   }
 
   private IImageService getImageService() {
     return Utils.getComponent(IImageService.class);
   }
 
   /**
    * @deprecated instead use getDimension from ImageService.
    */
   @Deprecated
   public ImageDimensions getDimension(String imageFullName, XWikiContext context
       ) throws XWikiException {
     LOGGER.warn("deprecated getDimension used!");
     return getImageService().getDimension(imageFullName);
   }
 
 }
