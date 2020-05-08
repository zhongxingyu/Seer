 package com.teleca.mm5.gallery;
 
 import javax.security.auth.callback.Callback;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 
 /** Type of content the gallery able to display */
 enum GalleryContentType {
     GALLERY_AUDIO,
     GALLERY_IMAGES,
     GALLERY_VIDEO,
     GALLERY_STATIC,
     GALLERY_ALLTYPES
 }
 
 
 
 /** enum for available gallery views to be used in
    methods and data which these views provides */
 enum GalleryViewType {
     GALLERY_MAINMENU    ( R.layout.main, R.id.main_menu, GalleryContentType.GALLERY_STATIC ),
     GALLERY_LIST        ( 0 , 0, GalleryContentType.GALLERY_AUDIO ),
     GALLERY_THUMBNAILS  ( R.layout.thumbnails, R.id.gridView1, GalleryContentType.GALLERY_IMAGES ),
     GALLERY_FULLSCREEN  ( R.layout.fullscreen, R.id.fullscrImageView, GalleryContentType.GALLERY_IMAGES ),
     GALLERY_DETAILS     ( 0, 0, GalleryContentType.GALLERY_ALLTYPES );
 
     private final Integer layoutId;
     private final Integer mainViewId;
     private final GalleryContentType contentType;
 
     GalleryViewType(Integer layoutId, Integer mainViewId, GalleryContentType contentType ) {
         this.layoutId = layoutId;
         this.contentType = contentType;
         this.mainViewId = mainViewId;
     }
 
     /**
      * @return the viewId
      */
     public Integer layoutId() {
         return layoutId;
     }
 
     /**
      * @return the contentType
      */
     public GalleryContentType contentType() {
         return contentType;
     }
 
     /**
      * @return the mainViewId
      */
     public Integer mainViewId() {
         return mainViewId;
     }
 }
 
 
 
 @ClassPreamble (
                 author = "Alexander Starchenko",
                 description = "interface for view for interaction with GalleryWorkTask task"
 )
 interface GalleryViewInterface {
     public void progressWorkExecution( int NumberFiles );
     public void finishedWorkExecution( GalleryWorkTaskResult processingResult );
     public void setContentCursor( Cursor contentViewCursor );
     public Context getGalleryViewContext();
 }
 
 @ClassPreamble (
                 author = "Alexander Starchenko",
                 description = "General abstract class; every Gallery View should extend it"
 )
 public abstract class GalleryView<V extends View> extends Activity implements Callback, GalleryViewInterface {
     private V mainView;
     private GalleryWorkTask galleryWorkBg;
     private Cursor contentCursor;
     private GalleryViewType contentViewType;
     private final String contentPathField = MediaStore.Images.Media.DATA;
     private final String contentNameField = MediaStore.Images.Media.DISPLAY_NAME;
     private final static String TAG = "GalleryView";
 
     @SuppressWarnings("unchecked")
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(contentViewType.layoutId());
 
         try {
             setMainView((V)findViewById(contentViewType.mainViewId()));
 
             if( !contentViewType.contentType().equals(GalleryContentType.GALLERY_STATIC) ) {
                 setGalleryWorkBg( new GalleryWorkTask(this) );
                 galleryWorkBg.execute(contentViewType.contentType());
             }
         } catch(Exception e) {
             Log.e( TAG, "onCreate(): " + e.getClass() + " thrown " + e.getMessage());
         }
     }
 
     @Override
     protected void onDestroy() {
         // need additional stop BG work task
         try {
             if( null != galleryWorkBg )
             {
                 galleryWorkBg.cancel(true);
             }
         } catch(Exception e) {
             Log.e( TAG, "onDestroy(): " + e.getClass() + " thrown " + e.getMessage());
         }
         super.onDestroy();
     }
 
     @Override
     public Context getGalleryViewContext() {
         return getApplicationContext();
     }
 
     /**
      * @param mainView the mainView to set
      */
     public final void setMainView(V mainView) {
         this.mainView = mainView;
     }
 
     /**
      * @return the mainView
      */
     public final V getMainView() {
         return mainView;
     }
 
     /**
      * @param galleryWorkBg the galleryWorkBg to set
      */
     public final void setGalleryWorkBg(GalleryWorkTask galleryWorkBg) {
         this.galleryWorkBg = galleryWorkBg;
     }
 
     /**
      * @return the galleryWorkBg
      */
     public final GalleryWorkTask getGalleryWorkBg() {
         return galleryWorkBg;
     }
 
     /**
      * @param contentType the contentType to set
      */
     public final void setContentType(GalleryViewType contentType) {
         this.contentViewType = contentType;
     }
 
     /**
      * @return the contentType
      */
     public final GalleryViewType getContentType() {
         return contentViewType;
     }
 
     /**
      * @return the contentPathField
      */
     public final String getContentPathField() {
         return contentPathField;
     }
 
     /**
      * @return the contentNameField
      */
     public final String getContentNameField() {
         return contentNameField;
     }
 
     /**
      * @param contentCursor the contentCursor to set
      */
     @Override
     public final void setContentCursor(Cursor contentCursor) {
         this.contentCursor = contentCursor;
     }
 
     /**
      * @return the contentCursor
      */
     public final Cursor getContentCursor() {
         return contentCursor;
     }
 
     @Override
     public void progressWorkExecution(int NumberFiles) {
         // TODO Auto-generated method stub
     }
 
     public void finishedWorkExecution( GalleryWorkTaskResult processingResult ) {
         // TODO Auto-generated method stub
     }
 }
