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
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
 /**
  * Generates the PhotoAlbumClass with fields for the data space, watermark
  * and copyright.
  */
 public class PhotoAlbumClass {
   /**
    * Generates the PhotoAlbumClass with fields for the data space, 
    * watermark and copyright.
    * 
    * @param context The XWikiContext used to get the xwiki and save.
    * @return A BaseObject of the PhotoAlbumClass
    * @throws XWikiException
    */
   public BaseClass getNewPhotoAlbumClass(XWikiContext context) throws XWikiException {
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
      doc = xwiki.getDocument(ImageLibStrings.PHOTO_ALBUM_CLASS, context);
     } catch (XWikiException e) {
      doc = new XWikiDocument();
      String[] metainfoClass = ImageLibStrings.PHOTO_ALBUM_CLASS.split("\\.");
      doc.setSpace(metainfoClass[0]);
      doc.setName(metainfoClass[1]);
       needsUpdate = true;
     }
     
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(ImageLibStrings.PHOTO_ALBUM_CLASS);
     needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_ALBUM_SPACE_NAME, ImageLibStrings.PHOTO_ALBUM_SPACE_NAME_PRETTY, 50);
     needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_ALBUM_COPYRIGHT, ImageLibStrings.PHOTO_ALBUM_COPYRIGHT_PRETTY, 50);
     needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_ALBUM_WATERMARK, ImageLibStrings.PHOTO_ALBUM_WATERMARK_PRETTY, 50);
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     
     return bclass;
   }
 }
