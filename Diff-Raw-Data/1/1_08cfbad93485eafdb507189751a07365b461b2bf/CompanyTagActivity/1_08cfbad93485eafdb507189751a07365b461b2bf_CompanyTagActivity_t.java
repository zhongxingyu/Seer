 package com.example.ad_creation;
 
 import java.util.ArrayList;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.example.maple_android.Company;
 import com.example.maple_android.CompanyData;
 import com.example.maple_android.CompanyLogo;
 import com.example.maple_android.R;
 import com.twotoasters.android.horizontalimagescroller.image.ImageToLoad;
 import com.twotoasters.android.horizontalimagescroller.image.ImageToLoadUrl;
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScroller;
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScrollerAdapter;
 
 public class CompanyTagActivity extends FunnelActivity {
 	private Company mCompany;
 	private HorizontalImageScroller mScroller;
 	private ArrayList<Company> mCompanies;
 	
 	private final int FRAME_COLOR = Color.TRANSPARENT; // the background color of the filter images
 	private final int FRAME_SELECTED_COLOR = Color.BLACK; // the color behind the selected filter
 	private final int SCROLLER_VIEW = R.layout.horizontal_image_scroller_with_text_item;
 	// this image is displayed as the company picture when we are unable to load any logos 
 	private final String DEFAULT_LOGO = "http://www.clker.com/cliparts/X/d/3/i/V/9/black-and-white-sad-face-md.png";
 	// the image shown before the logo is loaded from the server
 	private final int LOADING_IMAGE = R.drawable.maple;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setCustomContent(R.layout.activity_company_tag);
 
 		mConfig.put(
 				Config.HELP_MESSAGE,
 				"Tag your ad from a company in the database. "
 						+ "As you type, the field will autocomplete with possible companies.");
 		mConfig.put(Config.NAME, "Tag");
 
 		mAdCreationManager.setup(this);
 
 		// get list of availabe companies
 		mCompanies = CompanyData.getCompanies(this);
 
 		// make a list of ImageToLoad objects for image scroller
 		ArrayList<ImageToLoad> companyLogos = new ArrayList<ImageToLoad>();
 		ArrayList<String> companyNames = new ArrayList<String>();
 		for (Company c : mCompanies) {
 			ArrayList<CompanyLogo> logos = c.getLogos();
 			
 			// if the company doesn't have any logos availabe we can't
 			// display them
 			if(!logos.isEmpty()){
 				// default to showing the first logo in the list
 				CompanyLogo logo = logos.get(0);
 				// add logo and to scroller list
 				companyLogos.add(new ImageToLoadUrl(logo.getThumb()));				
 			}
 			
 			// use a default logo if we don't have any logos for the company
 			else {
 				companyLogos.add(new ImageToLoadUrl(DEFAULT_LOGO));		
 			}
 			
 			// add the company name to be displayed beneath the logo
 			companyNames.add(c.getName());
 		}
 
 		// set up the scroller with an adapter populated with the list of
 		// ImageToLoad objects
 		mScroller = (HorizontalImageScroller) findViewById(R.id.companyScroller);
 		HorizontalImageScrollerAdapter adapter = new HorizontalImageScrollerAdapter(
 				this, companyLogos);
 
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
 		// we want the company name to be shown beneath the logo
 		adapter.setShowText(true); 
 		// list of company names to use
 		adapter.setTextList(companyNames); 
 		
 		mScroller.setAdapter(adapter);
 
 		// start with the first company selected
 		mScroller.setCurrentImageIndex(0);
		mCompany = mCompanies.get(0);
 
 		// add callback function when image in scroller is selected
 		mScroller.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int pos,
 					long id) {
 				// Updates the background color to indicate selection
 				mScroller.setCurrentImageIndex(pos);
 
 				// keep track of which company has been picked
 				mCompany = mCompanies.get(pos);
 			}
 		});
 
 	}
 
 	/**
 	 * Set company tag and continue to the next stage in the funnel
 	 * 
 	 * @param view
 	 */
 	public void nextStage(View view) {
 		selectNext();
 		
 		mAdCreationManager.setCompany(mCompany);
 
 		mAdCreationManager.nextStage(this,
 				mAdCreationManager.getCurrentBitmap());
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
