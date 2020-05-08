 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.admin.page.gallery;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.faces.component.UIComponent;
 
 import org.primefaces.event.FileUploadEvent;
 import org.shredzone.cilla.admin.UploadedFileDataSource;
 import org.shredzone.cilla.admin.WrappedCillaException;
 import org.shredzone.cilla.admin.page.PageSelectionObserver;
 import org.shredzone.cilla.ws.exception.CillaServiceException;
 import org.shredzone.cilla.ws.page.GallerySectionDto;
 import org.shredzone.cilla.ws.page.PageDto;
 import org.shredzone.cilla.ws.page.PageWs;
 import org.shredzone.cilla.ws.page.PictureDto;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 
 /**
  * Bean for editing a gallery section of the current page.
  *
  * @author Richard "Shred" Körber
  */
 @Controller
 @Scope("session")
 public class GalleryBean implements PageSelectionObserver {
 
     private @Resource PageWs pageWs;
 
     private GallerySectionDto section;
     private PictureDto selectedImage;
     private UIComponent uiGrid;
 
     /**
      * {@link GallerySectionDto} to be edited.
      */
     public GallerySectionDto getGallerySection() { return section; }
     public void setGallerySection(GallerySectionDto section) {
         this.section = section;
         this.selectedImage = null;
     }
 
     /**
      * {@link PictureDto} to be edited.
      */
     public PictureDto getSelectedImage()        { return selectedImage; }
    public void setSelectedImage(PictureDto selectedImage) { this.selectedImage = selectedImage; }
 
     /**
      * Reference to the {@link UIComponent} of the data grid showing the pictures of the
      * gallery.
      */
     public UIComponent getUiGrid()              { return uiGrid; }
     public void setUiGrid(UIComponent uiGrid)   { this.uiGrid = uiGrid; }
 
     /**
      * List of all {@link PictureDto} of the selected gallery.
      */
     public List<PictureDto> getPictures()       { return section.getPictures(); }
 
     /**
      * Moves a picture within the picture list.
      *
      * @param picture
      *            {@link PictureDto} to be moved
      * @param relative
      *            Relative new position of the picture
      */
     public void moveImage(PictureDto picture, int relative) {
         synchronized (this) {
             List<PictureDto> list = section.getPictures();
 
             int current = list.indexOf(picture);
             if (current < 0) {
                 return;
             }
 
             int target = current + relative;
             if (target < 0) {
                 // Item moved out from the left, wrap around
                 PictureDto wrap = list.remove(current);
                 list.add(list.size() + 1 + target, wrap);
 
             } else if (target >= list.size()) {
                 // Item moved out from the right, wrap around
                 PictureDto wrap = list.remove(current);
                 list.add(target - list.size() - 1, wrap);
 
             } else {
                 // No wrap-around: just swap the items
                 PictureDto swap = list.get(target);
                 list.set(target, picture);
                 list.set(current, swap);
             }
         }
     }
 
     /**
      * Removes a picture.
      */
     public void removeImage(PictureDto picture) {
         section.getPictures().remove(picture);
     }
 
     /**
      * Handles the upload of a new picture.
      *
      * @param event
      *            {@link FileUploadEvent} containing the picture being uploaded
      */
     public void handleFileUpload(FileUploadEvent event) {
         try {
             PictureDto dto = pageWs.createNewPicture();
             dto.setUploadFile(new UploadedFileDataSource(event.getFile()).toDataHandler());
             getPictures().add(dto);
         } catch (CillaServiceException ex) {
             throw new WrappedCillaException(ex);
         }
     }
 
     @Override
     public void onPageSelected(PageDto selectedPage) {
         section = null;
         selectedImage = null;
     }
 
 }
