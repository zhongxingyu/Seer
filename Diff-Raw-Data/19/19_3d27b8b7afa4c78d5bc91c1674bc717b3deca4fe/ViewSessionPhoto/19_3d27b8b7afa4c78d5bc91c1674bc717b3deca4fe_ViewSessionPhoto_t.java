 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.service.exchange;
 
 /**
  *
  * @author kevin
  */
 public interface ViewSessionPhoto extends ViewSession {
 
     interface ViewSessionPhotoFastEdit extends ViewSessionPhoto {
 
         enum TagAction {SET}
         
         String getDesc();
         
        Integer[] getTagSet();
         
         TagAction getTagAction();
         
         Integer getStars();
     }
     interface ViewSessionPhotoSubmit extends ViewSessionPhoto {
 
         boolean getSuppr();
 
         String getDesc();
 
         boolean getRepresent();
 
         String getDroit() ;
 
         Integer[] getNewTag();
 
         Integer[] getTags();
 
         boolean getThemeBackground();
 
         boolean getThemePicture();
     }
     interface ViewSessionPhotoEdit extends ViewSessionPhoto {
 
     }
     interface ViewSessionPhotoDisplay extends ViewSessionPhoto {
 
         interface ViewSessionPhotoDisplayMassEdit extends ViewSessionPhotoDisplay {
 
             enum Turn {
 
                 RIGHT, LEFT, TAG, UNTAG, MVTAG, NOTHING, AUTHOR
             }
 
             Turn getTurn();
 
             boolean getChk(Integer id);
 
             Integer[] getAddTags();
 
             Integer getRmTag();
         }
 
         ViewSessionPhotoDisplayMassEdit getMassEdit();
     }
 
     Integer getId();
 
     Integer getTagPhoto();
 
     Mode getMode();
 
     Integer getAlbum();
 
     Integer getAlbmPage();
     
     Integer getPage();
 }
