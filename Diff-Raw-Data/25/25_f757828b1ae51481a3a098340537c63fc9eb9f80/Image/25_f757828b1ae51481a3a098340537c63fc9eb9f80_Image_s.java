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
 package com.celements.photo.image;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import com.celements.photo.container.ImageLibStrings;
 import com.celements.photo.container.ImageStrings;
 import com.celements.photo.utilities.BaseObjectHandler;
 import com.celements.photo.utilities.ZipAttachmentChanges;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 /**
  * Provides diverse methodes to access and handle images. e.g. extract images 
  * from zip files, to get a list of images or to delete and restore images.
  */
 public class Image {
   /**
    * Get an array of ImageStrings containing all the (non deleted) images in
    * the album.
    * 
    * @see com.celements.photo.plugin.container.ImageStrings
    * @param doc XWikiDocument of the album.
    * @param width Maximum allowed width of the thumbnails (aspect ratio 
    *         maintaining).
    * @param height Maximum allowed height of the thumbnails (aspect ratio 
    *          maintaining).
    * @param thumb Thumbnail handler to get the URLs to the thumbnails.
    * @param context XWikiContext
    * @return Array of ImageStrings for all images in the album.
    * @throws XWikiException
    * @throws IOException
    */
   public ImageStrings[] getImageList(XWikiDocument doc, int width, int height, 
       Thumbnail thumb, XWikiContext context) throws XWikiException, IOException{
     ImageStrings[] imageArray = getImageListExclThumbs(doc, context);
     
     for (int i = 0; i < imageArray.length; i++) {
       imageArray[i].setThumbURL(thumb.getUrl(doc, imageArray[i].getId(), width, height, 
           context));
     }
     
     return imageArray;
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
   public ImageStrings[] getImageListExclThumbs(XWikiDocument doc, XWikiContext context
       ) throws XWikiException, IOException{
    String album = doc.getName();
     (new ZipAttachmentChanges()).checkZipAttatchmentChanges(doc, context);
     
     List<XWikiDocument> images = new Vector<XWikiDocument>();
     List<String> imageList = context.getWiki().getSpaceDocsName(
         ImageLibStrings.getPhotoSpace(doc), context);
     getImagesFromAlbum(doc, album, images, imageList, context);
     
     BaseObjectHandler handler = new BaseObjectHandler();
     ImageStrings[] imageArray = new ImageStrings[images.size()];
     Iterator<XWikiDocument> iter = images.iterator();
     for (int i = 0; iter.hasNext(); i++) {
       imageArray[i] = fetchDataFromImage(iter.next(), doc, imageArray, handler, context);
     }
     
     return imageArray;
   }
 
   private void getImagesFromAlbum(XWikiDocument doc, String album,
       List<XWikiDocument> images, List<String> imageList, XWikiContext context)
       throws XWikiException {
     if(imageList != null){
       for (Iterator<String> iter = imageList.iterator(); iter.hasNext();) {
         String image = iter.next();
         if(image.lastIndexOf(album + "_img_") == 0){
          XWikiDocument imageDoc = context.getWiki().getDocument(
              ImageLibStrings.getPhotoSpace(doc), image, context); 
           if(!isDeleted(doc, image.substring(image.lastIndexOf(
               "_")+1), context)){
             images.add(imageDoc);
           }
         }
       }
     }
   }
 
   private ImageStrings fetchDataFromImage(XWikiDocument element, XWikiDocument doc,
       ImageStrings[] imageArray, BaseObjectHandler handler,
       XWikiContext context) throws XWikiException {
     String id = handler.getImageString(element, ImageLibStrings.PHOTO_IMAGE_HASH);
     String name = handler.getImageString(element, ImageLibStrings.PHOTO_IMAGE_FILENAME);
     String zipFilename = handler.getImageString(element, 
         ImageLibStrings.PHOTO_IMAGE_ZIPNAME);
     String origUrl = (new ZipAttachmentChanges()).getZipExplorerPluginApi(context
         ).getFileLink(new Document(doc, context), zipFilename, name);
     return new ImageStrings(id, name, origUrl, "");
   }
 
   /**
    * Check if the specified image is tagged as deleted.
    * 
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param context XWikiContext
    * @return true if the image is tagged as deleted.
    * @throws XWikiException
    */
   public boolean isDeleted(XWikiDocument doc, String id, XWikiContext context
       ) throws XWikiException {
    XWikiDocument celementsMetaDoc = context.getWiki().getDocument(
        ImageLibStrings.getPhotoSpace(doc), doc.getName() + "_img_" + id, context);
     return new BaseObjectHandler().getImageBoolean(celementsMetaDoc, 
         ImageLibStrings.PHOTO_IMAGE_DELETED);
   }
 
   /**
    * Set the deleted status of an image to the specified value.
    * 
    * @param doc XWikiDocument of the album.
    * @param id Id of the image.
    * @param deleted true taggs the image as deleted.
    * @param context XWikiContext
    * @throws XWikiException
    */
   public void setDeleted(XWikiDocument doc, String id, boolean deleted, 
       XWikiContext context) throws XWikiException {
    XWikiDocument celementsMetaDoc = context.getWiki().getDocument(
        ImageLibStrings.getPhotoSpace(doc), doc.getName() + "_img_" + id, context);
     new BaseObjectHandler().setImageBoolean(celementsMetaDoc, 
         ImageLibStrings.PHOTO_IMAGE_DELETED, deleted, context);
   }
 }
