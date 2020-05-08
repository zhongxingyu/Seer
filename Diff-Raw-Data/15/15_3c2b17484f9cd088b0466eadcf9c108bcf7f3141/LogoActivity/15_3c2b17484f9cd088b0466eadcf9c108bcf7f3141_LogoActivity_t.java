 package com.example.ad_creation;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 
 import com.example.custom_views.LogoView;
 import com.example.maple_android.CompanyLogo;
 import com.example.maple_android.R;
 import com.twotoasters.android.horizontalimagescroller.image.ImageToLoad;
 import com.twotoasters.android.horizontalimagescroller.image.ImageToLoadUrl;
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScroller;
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScrollerAdapter;
 
 public class LogoActivity extends FunnelActivity {
 	private LogoView mLogoView;
 	private HorizontalImageScroller mScroller;
	private Bitmap mLogo;
 
 	private final int FRAME_COLOR = Color.TRANSPARENT;
 	private final int FRAME_SELECTED_COLOR = Color.BLACK;
 	private final int SCROLLER_VIEW = R.layout.horizontal_image_scroller_with_text_item;
 	// this image is displayed as the company picture when we are unable to load
 	// any logos
 	private final String DEFAULT_LOGO = "http://www.clker.com/cliparts/X/d/3/i/V/9/black-and-white-sad-face-md.png";
 	// the image shown before the logo is loaded from the server
 	private final int LOADING_IMAGE = R.drawable.maple;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setCustomContent(R.layout.activity_logo);
 
 		mConfig.put(Config.HELP_MESSAGE,
 				"Select which company logo you want to place on the ad, and move it around!");
 		mConfig.put(Config.NAME, "Logo");
 
 		mLogoView = (LogoView) findViewById(R.id.ad);
 		mLogoView.setAd(mAdCreationManager.getCurrentBitmap(),
 				mAdCreationManager.getRatio());
 
 		mAdCreationManager.setup(this);
 
 		// Load Logo.
 		// logoArray will only be non null if the user picked
 		// a logo in the LogoPickerActivity. Otherwise we have to
 		// direct them there to pick a logo before they can use one
 
 		Bitmap logo = mAdCreationManager.getCompanyLogo();
 
 		if (logo != null) {
 			mLogoView.setLogo(logo, 0, 0);
 		}
 
 		// make a list of ImageToLoad objects for image scroller
 		ArrayList<ImageToLoad> imagesToLoad = new ArrayList<ImageToLoad>();
 		ArrayList<CompanyLogo> logos = mAdCreationManager.getCompany()
 				.getLogos();
 		for (CompanyLogo l : logos) {
 			imagesToLoad.add(new ImageToLoadUrl(l.getThumb()));
 		}
 
 		// if there are no logos available show a default image
 		if (logos.isEmpty()) {
 			imagesToLoad.add(new ImageToLoadUrl(DEFAULT_LOGO));
 		}
 
 		// set up the scroller with an adapter populated with the list of
 		// ImageToLoad objects
 		mScroller = (HorizontalImageScroller) findViewById(R.id.logoScroller);
 		HorizontalImageScrollerAdapter adapter = new HorizontalImageScrollerAdapter(
 				this, imagesToLoad);
 
 		// set adapter options
 		// shows the frame around the view
 		adapter.setShowImageFrame(true);
 		// only shows frame when item is selected
 		adapter.setHighlightActiveImage(true);
 		// the background color when selected
 		adapter.setFrameColor(FRAME_SELECTED_COLOR);
 		// the default background color
 		adapter.setFrameOffColor(FRAME_COLOR);
 		// set image to be used while loading
 		adapter.setLoadingImageResourceId(LOADING_IMAGE);
 		adapter.setImageLayoutResourceId(SCROLLER_VIEW);
 
 		mScroller.setAdapter(adapter);
 
 		// start with the first company selected
 		mScroller.setCurrentImageIndex(0);
 
 		// add callback function when image in scroller is selected
 		mScroller.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int pos,
 					long id) {
 				// Updates the background color to indicate selection
 				mScroller.setCurrentImageIndex(pos);
 
 				// change logoview to display this image
 				ImageView scrollerImage = (ImageView) view.findViewById(R.id.image);
				mLogo = scrollerImage.getDrawingCache();
				mLogoView.setLogo(mLogo, 0, 0);
 			}
 		});
 	}
 
 	/**
 	 * Save modified ad and continue to the next stage in the funnel
 	 * 
 	 * @param view
 	 */
 	public void nextStage(View view) {
 		selectNext();
		
		// update selected logo in adManager
		mAdCreationManager.setCompanyLogo(mLogo);
 
 		// update bitmap to include logo
 		Bitmap ad = mLogoView.addLogo();
 		// null is returned if a logo hasn't been set
 		if (ad == null)
 			ad = mAdCreationManager.getCurrentBitmap();
 		mAdCreationManager.nextStage(this, ad);
 	}
 
 	/**
 	 * Return to the previous stage without saving any changes
 	 * 
 	 * @param view
 	 */
 	public void prevStage(View view) {
 		selectPrev();
 		mAdCreationManager.previousStage(this);
 	}
 
 }
