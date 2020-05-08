 package com.dbstar.app;
 
 import com.dbstar.R;
 
 import android.content.Context;
 
 public class GDResourceAccessor {
 	public static final int PROPERTY_TITLE = 0;
 	public static final int PROPERTY_DIRECTOR = 1;
 	public static final int PROPERTY_ACTORS = 2;
 	public static final int PROPERTY_TYPE = 3;
 	public static final int PROPERTY_YEAR = 4;
 	public static final int PROPERTY_REGION = 5;
 	public static final int PROPERTY_RATE = 6;
 	public static final int PROPERTY_SEMICOLON = 7;
 	public static final int PROPERTY_SPACE = 8;
 	public static final int PROPERTY_EPISODES = 9;
 	public static final int PROPERTY_COUNT = 10;
 
 	String[] mTexts = new String[PROPERTY_COUNT];
 
 	public String HeaderDirector;
 	public String HeaderActors;
 	
 	public String HanZi_Di, HanZi_Ye, HanZi_Ji;
 
 	private Context mContext;
 
 	public GDResourceAccessor(Context context) {
 		mContext = context;
 
 		loadResource();
 
 		HeaderDirector = mTexts[PROPERTY_DIRECTOR] + mTexts[PROPERTY_SEMICOLON]
 				+ mTexts[PROPERTY_SPACE];
 		HeaderActors = mTexts[PROPERTY_ACTORS] + mTexts[PROPERTY_SEMICOLON]
 				+ mTexts[PROPERTY_SPACE];
 	}
 
 	public void loadResource() {
 		mTexts[PROPERTY_TITLE] = mContext.getResources().getString(
 				R.string.property_title);
 		mTexts[PROPERTY_ACTORS] = mContext.getResources().getString(
 				R.string.property_actors);
 		mTexts[PROPERTY_DIRECTOR] = mContext.getResources().getString(
 				R.string.property_director);
 		mTexts[PROPERTY_TYPE] = mContext.getResources().getString(
 				R.string.property_type);
 		mTexts[PROPERTY_YEAR] = mContext.getResources().getString(
 				R.string.property_year);
 		mTexts[PROPERTY_REGION] = mContext.getResources().getString(
 				R.string.property_region);
 		mTexts[PROPERTY_RATE] = mContext.getResources().getString(
 				R.string.property_rate);
 		mTexts[PROPERTY_SEMICOLON] = mContext.getResources().getString(
 				R.string.property_semicolon);
 		mTexts[PROPERTY_SPACE] = mContext.getResources().getString(
 				R.string.property_space);
 		mTexts[PROPERTY_EPISODES] = mContext.getResources().getString(
 				R.string.property_episodes);
 		
 		HanZi_Di = mContext.getResources().getString(R.string.text_di);
 		HanZi_Ye = mContext.getResources().getString(R.string.text_ye);
 		HanZi_Ji = mContext.getResources().getString(R.string.text_ji);
 	}
 
 }
