 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.Activities;
 
 import java.io.File;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.ContextMenu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Activities.Helpers.ImageAdapter;
 import de.raptor2101.GalDroid.Activities.Helpers.ImageAdapter.ScaleMode;
 import de.raptor2101.GalDroid.Activities.Helpers.ImageAdapter.TitleConfig;
 import de.raptor2101.GalDroid.Activities.Views.GalleryImageView;
 
 import de.raptor2101.GalDroid.WebGallery.ImageCache;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;
 
 public class GridViewActivity extends GalleryActivity implements OnItemClickListener, ImageLoaderTaskListener {
 
   private static final int CURRENT_INDEX = 0;
   private GridView mGridView;
   private ImageAdapter mAdapter;
   private ImageLoaderTask mImageLoaderTask;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
     super.onCreate(savedInstanceState);
     setContentView(R.layout.grid_view_activity);
     ActionBar actionBar = getActionBar();
     actionBar.setDisplayShowHomeEnabled(false);
     actionBar.setDisplayShowTitleEnabled(false);
     actionBar.hide();
 
     mGridView = (GridView) findViewById(R.id.gridViewWidget);
     mGridView.setOnItemClickListener(this);
 
     GalDroidApp app = (GalDroidApp) getApplicationContext();
     
     DisplayMetrics metrics = new DisplayMetrics();
     getWindowManager().getDefaultDisplay().getMetrics(metrics);
     
     int colums = metrics.widthPixels / 295;
     int rows = metrics.heightPixels /295;
     
     mImageLoaderTask = new ImageLoaderTask(app.getWebGallery(), app.getImageCache(), (int)(rows*colums*1.5));
     mAdapter = new ImageAdapter(this, new GridView.LayoutParams(295, 295), ScaleMode.ScaleSource, mImageLoaderTask);
     mAdapter.setTitleConfig(TitleConfig.ShowTitle);
 
     mGridView.setAdapter(mAdapter);
 
     registerForContextMenu(mGridView);
   }
 
   @Override
   public void onBackPressed() {
     mAdapter.cleanUp();
     super.onBackPressed();
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     switch (requestCode) {
     case (CURRENT_INDEX): {
 
       int scrollPos = data.getIntExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_INDEX, -1);
       mGridView.smoothScrollToPositionFromTop(scrollPos, 600);
 
       break;
     }
     }
   }
 
   public void onItemClick(AdapterView<?> parent, View view, int pos, long rowId) {
     GalleryImageView imageView = (GalleryImageView) view;
     GalleryObject galleryObject = imageView.getGalleryObject();
 
     Intent intent;
     if (!galleryObject.hasChildren()) {
       intent = new Intent(this, ImageViewActivity.class);
       intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_GALLERY, getDisplayedGallery());
       intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_OBJECT, galleryObject);
       // The startActivityForResult is needed to scroll to the last
       // disyplayed imaged from the ImageView
       // The Result is handled by onActivityResult
       this.startActivityForResult(intent, CURRENT_INDEX);
     } else {
       intent = new Intent(this, GridViewActivity.class);
       intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_GALLERY, galleryObject);
       this.startActivity(intent);
     }
 
   }
 
   public void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects) {
     mAdapter.setGalleryObjects(galleryObjects);
     mImageLoaderTask.start();
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.grid_view_context_menu, menu);
   }
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
     GalleryImageView imageView = (GalleryImageView) info.targetView;
     
     
     switch (item.getItemId()){
     case R.id.item_additional_info_object: 
       callAdditionalInfoActivity(imageView);
       break;
     case R.id.item_additional_share_object:
       callShareIntentActivity(imageView);
       break;
     }
     
     
     return true;
   }
 
   private void callShareIntentActivity(GalleryImageView imageView) {
     
     
     GalDroidApp app = (GalDroidApp) getApplicationContext();
     ImageCache cache = app.getImageCache();
     
     
     GalleryDownloadObject image = imageView.getGalleryObject().getImage();
     File file = cache.getFile(image.getUniqueId());
     if (file.exists()) {
       callShareIntentActivity(file);
     } else {
       mImageLoaderTask.download(image, null, this);
     }
   }
   
   private void callShareIntentActivity(File file) {
     Intent intent = new Intent();
     intent.setAction(android.content.Intent.ACTION_VIEW);
     // TODO: ermittle den typen richtig oder convertiere das file...    
     intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
     intent = Intent.createChooser(intent, "Share Image");
     this.startActivity(intent);
   }
 
   private void callAdditionalInfoActivity(GalleryImageView imageView) {
     Intent intent = null;
     intent = new Intent(this, ImageViewActivity.class);
     intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_GALLERY, getDisplayedGallery());
     intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_OBJECT, imageView.getGalleryObject());
     intent.putExtra(GalDroidApp.INTENT_EXTRA_SHOW_IMAGE_INFO, true);
     this.startActivity(intent);
   }
 
   @Override
   protected void onResume() {
     super.onResume();
   
     ImageAdapter adapter = (ImageAdapter) mGridView.getAdapter();
     if (adapter != null) {
       adapter.refreshImages();
     }
     
     mImageLoaderTask.start();
   }
 
   protected void onPause() {
     super.onPause();
     
     try {
       mImageLoaderTask.stop(false);
     } catch (InterruptedException e) {
       
     }
   }
   
   @Override
   protected void onStop() {
     super.onStop();
     
     try {
       mImageLoaderTask.cancel(true);
     } catch (InterruptedException e) {
       
     }
     
     mAdapter.cleanUp();
   }
 
   public void onLoadingStarted(String uniqueId) {
     showProgressBar(R.string.progress_title_load_image);
   }
 
   public void onLoadingProgress(String uniqueId, int currentValue, int maxValue) {
     
     updateProgressBar(currentValue, maxValue);
     
   }
 
   public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
     GalDroidApp app = (GalDroidApp) getApplicationContext();
     ImageCache cache = app.getImageCache();
     
     File file = cache.getFile(uniqueId);
     if (file.exists()) {
       callShareIntentActivity(file);
     }
     
     dismissProgressBar();
   }
 
   public void onLoadingCancelled(String uniqueId) {
     dismissProgressBar();
   }
 }
