 package com.project202;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 
 import android.app.Activity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.View;
 import android.view.animation.Animation;
 import android.widget.TextView;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.NoTitle;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.googlecode.androidannotations.annotations.res.AnimationRes;
 import com.project202.adapter.SimplePagerAdapter;
 import com.project202.loading.RatingDownloadTask;
 import com.project202.model.Rating;
 import com.project202.model.ThemeName;
 import com.project202.views.HistoryView_;
 import com.project202.views.OnHistoryFocusedListener;
 import com.project202.views.RatingDetailsView_;
 import com.project202.views.RatingView.OnRatingClickListener;
 import com.project202.views.RatingView_;
 import com.project202.views.SettingsView;
 import com.viewpagerindicator.TitlePageIndicator;
 
 @EActivity(R.layout.show_note)
 @NoTitle
 public class ShowNoteActivity extends Activity implements OnRatingClickListener, OnSettingsUpdatedListener {
 
 	@Extra("address")
 	String address;
 
 	@Extra("rating")
 	Rating rating;
 	
 	@Extra("firstView")
 	Integer firstView;
 
 	@ViewById
 	SettingsView settingsView;
 
 	@AnimationRes
 	Animation slideInFromBottom;
 
 	@AnimationRes
 	Animation slideOutFromTop;
 
 	@ViewById(R.id.view_pager)
 	protected ViewPager viewPager;
 
 	private List<OnHistoryFocusedListener> onHistoryFocusedListeners;
 
 	private RatingDetailsView_ ratingDetailsView;
 
 	@ViewById
 	TitlePageIndicator titles;
 	
 	@ViewById
 	TextView titleView;
 
 	private SimplePagerAdapter pagerAdapter;
 
 	private List<OnSettingsUpdatedListener> onSettingsUpdatedListeners = new ArrayList<OnSettingsUpdatedListener>();
 	
 	@AfterViews
 	public void afterViews() {
 
 		onHistoryFocusedListeners = new ArrayList<OnHistoryFocusedListener>();
 
 		titleView.setText(address);
 
 		// Creating ViewPager Views
 		RatingView_ ratingView = new RatingView_(this);
 		ratingDetailsView = new RatingDetailsView_(this);
 		final HistoryView_ historyView = new HistoryView_(this);
 
 		// Inflating layouts
 		ratingView.onFinishInflate();
 		ratingDetailsView.onFinishInflate();
 		historyView.onFinishInflate();
 
 		// Registering listeners
 		onSettingsUpdatedListeners.add(ratingView);
 		onSettingsUpdatedListeners.add(historyView);
 		onSettingsUpdatedListeners.add(ratingDetailsView);
 
 		// Injecting content
 		ratingView.setOnRatingClickListener(this);
 		settingsView.setOnSettingsUpdatedListener(this);
 		
 		// Storing views
 		List<View> views = Arrays.<View> asList(historyView, ratingView, ratingDetailsView);
 		List<String> pageTitles = Arrays.asList(getString(R.string.history_title), getString(R.string.rating_title), getString(R.string.rating_details_title));
 		
 		// Initializing PagerAdapter
 		pagerAdapter = new SimplePagerAdapter(views, pageTitles);
 		
 		// Initializing ViewPager
 		viewPager.setAdapter(pagerAdapter);
 		
 		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				if (position == 0) {
 					for (OnHistoryFocusedListener listener : onHistoryFocusedListeners) {
 						listener.onHistoryFocused();
 					}
 				} else {
 					for (OnHistoryFocusedListener listener : onHistoryFocusedListeners) {
 						listener.onHistoryHidden();
 					}
 				}
 				titles.onPageSelected(position);
 			}
 			
 			@Override
 			public void onPageScrollStateChanged(int state) {
 				titles.onPageScrollStateChanged(state);
 			}
 			
 			@Override
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 				titles.onPageScrolled(position, positionOffset, positionOffsetPixels);
 			}
 		});
 		
 		titles.setViewPager(viewPager);
 
 		List<Rating> ratings = loadRatingsFromFiles();
 		historyView.setRatings(ratings);

		boolean ratingNull = rating==null;
 		
		if(ratingNull && !ratings.isEmpty())
 			rating = ratings.get(0);
		
		ratingView.setValuesFromRating(rating);
		ratingDetailsView.setRating(rating);
 
		if(ratingNull)
			viewPager.setCurrentItem(0);
		else
			viewPager.setCurrentItem(1);
 	}
 
 	private List<Rating> loadRatingsFromFiles() {
 		File directory = getFilesDir();
 		File[] historyFiles = directory.listFiles(new FilenameFilter() {
 			
 			@Override
 			public boolean accept(File dir, String filename) {
 				return filename.startsWith(RatingDownloadTask.HISTO_FILE_PREFIX);
 			}
 		});
 		
 		ObjectMapper objectMapper = new ObjectMapper();
 		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 		objectMapper.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, false);
 		objectMapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
 		objectMapper.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
 		
 		List<Rating> ratings = new ArrayList<Rating>();
 		
 		Arrays.sort(historyFiles, new Comparator<File>() {
 			@Override
 			public int compare(File lhs, File rhs) {
 				return rhs.getName().compareTo(lhs.getName());
 			}
 		});
 		
 		for(File histFile : historyFiles){
 			try {
 				ratings.add(objectMapper.readValue(histFile, Rating.class));
 			} catch (JsonParseException e) {
 				e.printStackTrace();
 			} catch (JsonMappingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return ratings;
 	}
 
 	public void addOnSettingsFocusedHandler(OnHistoryFocusedListener convertView) {
 		onHistoryFocusedListeners.add(convertView);
 	}
 
 	@Click
 	void settingsButtonClicked() {
 		if (settingsView.getVisibility() == View.VISIBLE) {
 			settingsView.startAnimation(slideOutFromTop);
 			settingsView.setVisibility(View.GONE);
 		} else {
 			settingsView.startAnimation(slideInFromBottom);
 			settingsView.setVisibility(View.VISIBLE);
 		}
 	}
 
 	@Override
 	public void onRatingClickListener(ThemeName themeName) {
 		ratingDetailsView.onRatingClickListener(themeName);
 		viewPager.setCurrentItem(2);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if(settingsView.getVisibility() == View.VISIBLE)
 			settingsButtonClicked();
 		else
 			super.onBackPressed();
 	}
 
 	@Click
 	void homeClicked() {
 		HomeHelper.goToHome(this);
 	}
 
 	
 	@Override
 	public void onSettingsUpdated() {
 		for(OnSettingsUpdatedListener l : onSettingsUpdatedListeners)
 			l.onSettingsUpdated();
 	}
 
 }
