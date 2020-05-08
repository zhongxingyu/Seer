 package com.taperay.android.preview;
 
 import com.taperay.android.preview.R;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.FrameLayout;
 
 public class ShowPreviewActivity extends TapeRayActivity {
 	private static ImagePreview imagePreview;
 	private ContentManager contentManager;
 	private Artwork artwork;
 	private TouchImageView imageView;
 	private ProgressDialog dialog;
 	private Bitmap bitmap;
 
 	void loadImage() {
		if (dialog != null)
 			dialog.dismiss();
		
 		if (bitmap != null) {
 			artwork.setBitmapColor(contentManager.getCurrentColor());
 			imageView.setImageBitmap(bitmap);
 			return;
 		}
		
 		dialog = ProgressDialog.show(this,
 						getResources().getString(R.string.progress_dialog_header),
 						getResources().getString(R.string.loading_artwork),
 						true);
 
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					bitmap = artwork.getImageBitmap();
 				} catch (Exception e) {
 					displayNetworkErrorAndFinish();
 					return;
 				}
 				artwork.setBitmapColor(contentManager.getCurrentColor());
 
				imageView.post(new Runnable() {
 					public void run() {
 						imageView.setImageBitmap(bitmap);
 					}
 				});
 				
 				if (dialog != null) {
 					dialog.dismiss();
 					dialog = null;
 				}
 			}
 		}).start();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.preview);
 
 		TapeRayApplication app = (TapeRayApplication) getApplication();
 		contentManager = app.getContentManager();
 
 		// add imagepreview
 		imagePreview = new ImagePreview(this);
 		((FrameLayout) findViewById(R.id.preview)).addView(imagePreview);
 
 		// add touch controller
 		imageView = new TouchImageView(this, null);
 		((FrameLayout) findViewById(R.id.preview)).addView(imageView);
 
 		artwork = contentManager.getCurrentArtwork();
 		setTitle(artwork.getTitle());
 
 		loadImage();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.preview_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.change_color:
 			i = new Intent(ShowPreviewActivity.this, ShowColorsActivity.class);
 			startActivity(i);
 			return true;
 		case R.id.visit_website:
 			i = new Intent(Intent.ACTION_VIEW, Uri.parse(artwork.getURL()));
 			startActivity(i);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	protected void onPause() {
 		super.onPause();
 		if (dialog != null) {
 			dialog.dismiss();
 			dialog = null;
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		loadImage();
 	}
 }
