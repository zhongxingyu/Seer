 package com.project202.model;
 
 import com.googlecode.androidannotations.annotations.EBean;
 import com.project202.address.SettingsPreferences_;
 
 @EBean
 public class Weighted {
 	
	public static Float getMarkSum(Rating rating){
 		float sum = 0f;
 		for(Theme t : rating.getThemes()){
 			sum += t.getNote();
 		}
 		return sum;
 	}
 	
 
 	public static Float getWeightedMark(Rating rating, SettingsPreferences_ preferences){
 		float sum = 0f;
 		float weights = 0;
 		
 		for(Theme t : rating.getThemes()){
 			float w = getMultiplicator(t.name, preferences);
			sum += t.getNote()*w;
 			weights += w;
 		}
 		
 		return sum / weights;
 	}
 	
 	private static float getMultiplicator(ThemeName themeName, SettingsPreferences_ preferences) {
 		switch (themeName) {
 		case CULTURE:
 			return getMultiplicator(preferences.cultureWeight().get());
 		case LEISURE:
 			return getMultiplicator(preferences.leisureWeight().get());
 		case NATURE:
 			return getMultiplicator(preferences.natureWeight().get());
 		case SECURITY:
 			return getMultiplicator(preferences.securityWeight().get());
 		case SHOPS:
 			return getMultiplicator(preferences.shopsWeight().get());
 		case TRANSIT:
 			return getMultiplicator(preferences.transitWeight().get());
 		default:
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	private static float getMultiplicator(int preferenceWeight){
 		switch(preferenceWeight){
 		case 0:
 			return 1;
		case 1:
 			return 2;
		case 2:
			return 3;
 		default:
 			throw new UnsupportedOperationException();
 		}
 	}
 }
