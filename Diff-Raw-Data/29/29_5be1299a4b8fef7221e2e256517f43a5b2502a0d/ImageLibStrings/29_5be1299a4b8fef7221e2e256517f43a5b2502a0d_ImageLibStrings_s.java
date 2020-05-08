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
 package com.celements.photo.container;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.web.Utils;
 
 public class ImageLibStrings {
   //Spaces
   public static final String XWIKI_CLASS_SPACE = "Classes";
 
   //Filetype recognition
   public static final String MIME_ZIP = "application/zip";
   public static final String MIME_ZIP_MICROSOFT = "application/x-zip-compressed";
   public static final String MIME_BMP = "bmp";
   public static final String MIME_GIF = "gif";
   public static final String MIME_JPE = "jpe";
   public static final String MIME_JPG = "jpg";
   public static final String MIME_JPEG = "jpeg";
   public static final String MIME_PNG = "png";
   
   //Hashing
   public static final String HASHING_ALGORITHM = "SHA-256";
   
   //Classes
   public static final String METAINFO_CLASS = XWIKI_CLASS_SPACE + "." + "PhotoMetainfoClass";
   public static final String METAINFO_CLASS_NAME = "name";
   public static final String METAINFO_CLASS_NAME_PRETTY = "Name";
   public static final String METAINFO_CLASS_DESCRIPTION = "description";
   public static final String METAINFO_CLASS_DESCRIPTION_PRETTY = "Description";
  public static final String PHOTO_ALBUM_CLASS = XWIKI_CLASS_SPACE + "." + "PhotoAlbumClass";
   public static final String PHOTO_ALBUM_COPYRIGHT = "copyright";
   public static final String PHOTO_ALBUM_COPYRIGHT_PRETTY = "Copyright";
   public static final String PHOTO_ALBUM_SPACE_NAME = "space_name";
   public static final String PHOTO_ALBUM_SPACE_NAME_PRETTY = "Space Name";
   public static final String PHOTO_ALBUM_WATERMARK = "watermark";
   public static final String PHOTO_ALBUM_WATERMARK_PRETTY = "Watermark";
   public static final String PHOTO_IMAGE_CLASS = XWIKI_CLASS_SPACE + "." + "PhotoImageClass";
   public static final String PHOTO_IMAGE_HASH = "image_hash";
   public static final String PHOTO_IMAGE_HASH_PRETTY = "Image Hash";
   public static final String PHOTO_IMAGE_FILENAME = "image_filename";
   public static final String PHOTO_IMAGE_FILENAME_PRETTY = "Image Filename";
   public static final String PHOTO_IMAGE_ZIPDIRECTORY = "zip_directory";
   public static final String PHOTO_IMAGE_ZIPDIRECTORY_PRETTY = "Directory in zip file";
   public static final String PHOTO_IMAGE_ZIPNAME = "zip_filename";
   public static final String PHOTO_IMAGE_ZIPNAME_PRETTY = "Zip Filename";
   public static final String PHOTO_IMAGE_REVISION = "revision";
   public static final String PHOTO_IMAGE_REVISION_PRETTY = "Revision";
   public static final String PHOTO_IMAGE_DELETED = "deleted";
   public static final String PHOTO_IMAGE_DELETED_PRETTY = "Deleted";
   public static final String PHOTO_IMAGE_WIDTH = "width";
   public static final String PHOTO_IMAGE_WIDTH_PRETTY = "Original image's width";
   public static final String PHOTO_IMAGE_HEIGHT = "height";
   public static final String PHOTO_IMAGE_HEIGHT_PRETTY = "Original image's height";
   
   //Metatag names
   public static final String METATAG_UNKNOWN_TAG = "Unknown tag";
   public static final String METATAG_ZIP_FILENAME = "Zip Filename";
   public static final String METATAG_IMAGE_HASH = "Image Hash";
   
   public static String getPhotoSpace(String space, String album, XWikiContext context) throws XWikiException {
     XWikiDocument doc = context.getWiki().getDocument(getWebUtils(
         ).resolveDocumentReference(space + "." + album), context);
     return getPhotoSpace(doc);
   }
   
   public static String getPhotoSpace(XWikiDocument doc) {
     List<BaseObject> objList = doc.getXObjects(getWebUtils(
         ).resolveDocumentReference(PHOTO_ALBUM_CLASS));
     for (Iterator<BaseObject> iter = objList.iterator(); iter.hasNext();) {
       BaseObject element = (BaseObject) iter.next();
       if(element != null){
         return element.getStringValue(PHOTO_ALBUM_SPACE_NAME);
       }
     }
     return "";
   }
   
   static IWebUtilsService getWebUtils() {
     return Utils.getComponent(IWebUtilsService.class);
   }
 }
