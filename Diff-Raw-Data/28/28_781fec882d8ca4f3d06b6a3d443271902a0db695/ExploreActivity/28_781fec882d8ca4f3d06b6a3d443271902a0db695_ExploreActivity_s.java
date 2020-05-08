 package com.parworks.mars.view.siteexplorer;
 
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.parworks.arcameraview.CaptureImageActivity;
 import com.parworks.arviewer.MiniARViewer;
 import com.parworks.mars.R;
 import com.parworks.mars.model.db.SiteInfoTable;
 import com.parworks.mars.model.sync.SyncHandler;
 import com.parworks.mars.utils.Utilities;
 import com.parworks.mars.view.siteexplorer.AugmentedImagesLoader.AugmentedImagesLoaderListener;
 import com.parworks.mars.view.siteexplorer.CommentsLoader.CommentsLoaderListener;
 import com.parworks.mars.view.siteexplorer.SiteInfoLoader.SiteInfoLoaderListener;
 
 public class ExploreActivity extends SherlockFragmentActivity { 
 	
 	public static final String SITE_ID_ARGUMENT_KEY = "siteIdKey";
 	public static final String TAG = ExploreActivity.class.getName();
 	
 	private String mSiteId;
 	private static final int SITE_INFO_LOADER_ID = 0;
 	private static final int AUGMENTED_IMAGES_LOADER_ID = 1;
 	private static final int COMMENTS_LOADER_ID = 2;
 	
 	private View mLayoutView;
 	private AddCommentManager mAddCommentManager;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mLayoutView = getLayoutInflater().inflate(R.layout.activity_explore, null);
 		setContentView(mLayoutView);
 		
 		mSiteId = getIntent().getStringExtra(SITE_ID_ARGUMENT_KEY);
 		
 
 				
 		SiteInfoLoader siteInfoLoader = new SiteInfoLoader(mSiteId, this, new SiteInfoLoaderListener() {			
 			@Override
 			public void onSiteLoaded(Cursor siteData) {
 				loadSiteInfoIntoUi(siteData);				
 			}
 		});
 		getSupportLoaderManager().initLoader(SITE_INFO_LOADER_ID, null, siteInfoLoader);
 		
 		AugmentedImagesLoader augmentedImagesLoader = new AugmentedImagesLoader(mSiteId, this, new AugmentedImagesLoaderListener() {			
 			@Override
 			public void onImagesLoaded(Cursor data) {
 				loadAugmentedImagesIntoUi(data);				
 			}
 		});
 		getSupportLoaderManager().initLoader(AUGMENTED_IMAGES_LOADER_ID, null, augmentedImagesLoader);
 		
 		CommentsLoader commentsLoader = new CommentsLoader(mSiteId, this, new CommentsLoaderListener() {			
 			@Override
 			public void onCommentsLoaded(Cursor data) {
 				loadCommentsIntoUi(data);				
 			}
 		});
 		getSupportLoaderManager().initLoader(COMMENTS_LOADER_ID, null, commentsLoader);
 					
 		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
 		getSupportActionBar().setTitle(mSiteId);
 		getSupportActionBar().setHomeButtonEnabled(true);
 		getSupportActionBar().setDisplayShowHomeEnabled(true);
 		
 		mAddCommentManager = new AddCommentManager(this,mSiteId);
 		Button addCommentButton = (Button) findViewById(R.id.buttonAddComment);
 		addCommentButton.setOnClickListener(mAddCommentManager);
 		
 		// sync the site
 		SyncHandler.syncSiteInfo(mSiteId, true);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		mAddCommentManager.facebookOnActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_site_explorer, menu);
 		MenuItem augmentTitleBar = menu.findItem(R.id.augmentTitleBar);
 		ImageView imageView = (ImageView) augmentTitleBar
 				.getActionView().findViewById(R.id.augmentActionButton);
 		imageView.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(ExploreActivity.this, CaptureImageActivity.class);
 				intent.putExtra(CaptureImageActivity.SITE_ID_KEY, mSiteId);
 				intent.putExtra(CaptureImageActivity.IS_AUGMENT_ATTR, true);
 				startActivity(intent);
 			}
 		});
 		
 		TextView textView = (TextView) augmentTitleBar.getActionView().findViewById(R.id.siteTitle);
 		textView.setText(mSiteId);		
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(
 			com.actionbarsherlock.view.MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.augmentTitleBar:
 			break;
 		case android.R.id.home:
 			onBackPressed();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	
 	private void loadSiteInfoIntoUi(Cursor data) {
 		if(data.getCount() == 0 ) {
 			return;
 		}
 		//set site name
 		String siteName = data.getString(data.getColumnIndex(SiteInfoTable.COLUMN_NAME));
		if(siteName.isEmpty() || siteName == null) {
 			siteName = data.getString(data.getColumnIndex(SiteInfoTable.COLUMN_SITE_ID));
 		}
 		TextView nameTextView = (TextView) findViewById(R.id.textViewSiteName);
 		nameTextView.setText(siteName);
 		
 		//set site address
 		String siteAddress = data.getString(data.getColumnIndex(SiteInfoTable.COLUMN_ADDRESS));
 		TextView addressTextView = (TextView) findViewById(R.id.textViewSiteAddress);
 		if(siteAddress == null) {
 			addressTextView.setText("No address available");
 		} else {
 			addressTextView.setText(siteAddress);
 		}
 		
 		
 		
 		//set site description
 		String siteDesc = data.getString(data.getColumnIndex(SiteInfoTable.COLUMN_DESC));
 		TextView descriptionTextView = (TextView) findViewById(R.id.textViewSiteDescription);
 		descriptionTextView.setText(siteDesc);
 		
 		//let siteImageManager handle the SiteImageView
 		MiniARViewer miniARView = (MiniARViewer) findViewById(R.id.siteExplorerPosterImageView);
 		ProgressBar siteImageProgressBar = (ProgressBar) findViewById(R.id.progressBarSiteImage);
 		PosterImageManager siteImageManager = new PosterImageManager(mSiteId, miniARView, siteImageProgressBar, this);
 		siteImageManager.setSiteImage(data);
 		
 		//let mapImageManager handle the map view
 		ImageView mapImageView = (ImageView) findViewById(R.id.imageViewMap);
 		ProgressBar mapProgressBar = (ProgressBar) findViewById(R.id.progressBarMapView);
 		MapImageManager mapImageManager = new MapImageManager(mSiteId, mapImageView, mapProgressBar, this);
 		mapImageManager.setMapView(data);
 	}
 
 	private void loadAugmentedImagesIntoUi(Cursor data) {
 		ProgressBar augmentedImagesProgressBar = (ProgressBar) findViewById(R.id.progressBarAugmentedPhotos);
 		LinearLayout augmentedImagesLayout = (LinearLayout) findViewById(R.id.linearLayoutAugmentedImagesLayout);
 		TextView augmentedImagesTotalTextView = (TextView) findViewById(R.id.textViewAugmentedPhotoTotal);
 		AugmentedImageViewManager augmentedImagesViewManager = new AugmentedImageViewManager(mSiteId, this, augmentedImagesProgressBar, augmentedImagesLayout,augmentedImagesTotalTextView);
 		augmentedImagesViewManager.setAugmentedImages(data);		
 	}
 	
 	private void loadCommentsIntoUi(Cursor data) {
 		Log.d(Utilities.DEBUG_TAG_SYNC,"Explore Activity - loadCommentsIntoUi");
 		ProgressBar commentsProgressBar = (ProgressBar) findViewById(R.id.progressBarComments);
 		LinearLayout commentsLayout = (LinearLayout) findViewById(R.id.linearLayoutComments);
 		TextView commentsTotalTextView = (TextView) findViewById(R.id.textViewCommentTotal);
 		CommentsViewManager commentsViewManager = new CommentsViewManager(mSiteId, this, commentsProgressBar, commentsLayout,commentsTotalTextView);
 		commentsViewManager.setCommentsView(data);		
 	}
 }
