 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Refract.
  *
  * Refract is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Refract is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Refract. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.refract.utils;
 
 import com.ijuru.refract.renderer.Complex;
 import com.ijuru.refract.renderer.Function;
 import com.ijuru.refract.renderer.Mapping;
 import com.ijuru.refract.renderer.RendererParams;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 
 /**
  * Shared preferences utility methods
  */
 public class Preferences {
 		
 	/**
 	 * Gets a shared preference as a string
 	 * @param context the context
 	 * @param key the preference key
 	 * @param defResId the resource id of the default value
 	 * @return the preference value
 	 */
 	public static String getStringPreference(Context context, String key, int defResId) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		String defValue = context.getResources().getString(defResId);
 		return preferences.getString(key, defValue);
 	}
 	
 	/**
 	 * Gets a shared preference as an integer
 	 * @param context the context
 	 * @param key the preference key
 	 * @param defResId the resource id of the default value
 	 * @return the preference value
 	 */
 	public static int getIntegerPreference(Context context, String key, int defResId) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		String defValue = "" + context.getResources().getInteger(defResId);
 		return Integer.parseInt(preferences.getString(key, defValue));
 	}
 	
 	/**
 	 * Gets a shared preference as a double
 	 * @param context the context
 	 * @param key the preference key
 	 * @param defResId the resource id of the default value
 	 * @return the preference value
 	 */
 	public static double getDoublePreference(Context context, String key, double defValue) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		return Double.parseDouble(preferences.getString(key, "" + defValue));
 	}
 	
 	/**
 	 * Gets a shared preference as a complex value
 	 * @param context the context
 	 * @param key the preference key
 	 * @param def the default value
 	 * @return the preference value
 	 */
	public static Complex getComplexPreference(Context context, String key, Complex def) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String str = preferences.getString(key, def.toString());
		Complex complex = Complex.parseComplex(str);
		return complex != null ? complex : def;
 	}
 	
 	/**
 	 * Gets a shared preference as a function value
 	 * @param context the context
 	 * @param key the preference key
 	 * @param def the default value
 	 * @return the preference value
 	 */
 	public static Function getFunctionPreference(Context context, String key, Function def) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		String str = preferences.getString(key, def.name().toLowerCase());
		Function function = Function.parseString(str);
		return function != null ? function : def;
 	}
 	
 	/**
 	 * Gets a shared preference as a mapping value
 	 * @param context the context
 	 * @param key the preference key
 	 * @param def the default value
 	 * @return the preference value
 	 */
 	public static Mapping getMappingPreference(Context context, String key, Mapping def) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		String str = preferences.getString(key, def.name().toLowerCase());
		Mapping mapping = Mapping.parseString(str);
		return mapping != null ? mapping : def;
 	}
 	
 	/**
 	 * Gets a shared preference as renderer parameters
 	 * @param context the context
 	 * @param key the preference key
 	 * @param def the default value
 	 * @return the preference value
 	 */
 	public static RendererParams getParametersPreference(Context context, String key) {
 		Function function = getFunctionPreference(context, key + ".function", Function.MANDELBROT);
 		Complex offset = getComplexPreference(context, key + ".offset", Complex.ORIGIN);
 		double zoom = getDoublePreference(context, key + ".zoom", 0.0);
 		return new RendererParams(function, offset, zoom);
 	}
 	
 	/**
 	 * Sets a shared preference as renderer parameters
 	 * @param context the context
 	 * @param key the preference key
 	 * @param params the parameters
 	 */
 	public static void setParametersPreference(Context context, String key, RendererParams params) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		Editor editor = preferences.edit();
 		editor.putString(key + ".function", params.getFunction().toString());
 		editor.putString(key + ".offset", params.getOffset().toString());
 		editor.putString(key + ".zoom", "" + params.getZoom());
 		editor.commit();
 	}
 }
