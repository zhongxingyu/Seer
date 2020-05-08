 package com.seawolfsanctuary.tmt;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class Helpers {
 
 	public static final String exportDirectoryPath = Environment
 			.getExternalStorageDirectory().toString();
 
 	public static final String dataDirectoryPath = Environment
 			.getExternalStorageDirectory().toString()
 			+ "/Android/data/com.seawolfsanctuary.tmt";
 
 	public static final String dataDirectoryURI = "file:///sdcard/Android/data/com.seawolfsanctuary.tmt";
 
 	public static void hideKeyboard(View view) {
 		try {
 			InputMethodManager imm = (InputMethodManager) view.getContext()
 					.getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
 		} catch (Exception e) {
 			// couldn't close keyboards
 		}
 	}
 
 	public static String trimCSVSpeech(String input) {
 		if (input.startsWith("" + '"') && input.endsWith("" + '"')) {
 			return input.substring(1, input.length() - 1);
 		} else {
 			return input;
 		}
 	}
 
 	public static String[] arrayListToArray(ArrayList<String> input) {
 		String[] returnedArray = new String[input.size()];
 		for (int i = 0; i < input.size(); i++) {
 			returnedArray[i] = input.get(i);
 		}
 		return returnedArray;
 	}
 
 	public static String[][] multiArrayListToArray(
 			ArrayList<ArrayList<String>> input) {
 		String[][] returnedArray = new String[input.size()][];
 		for (int i = 0; i < input.size(); i++) {
 			returnedArray[i] = arrayListToArray(input.get(i));
 		}
 		return returnedArray;
 	}
 
 	public static String leftPad(String s, int width) {
 		return String.format("%" + width + "s", s).replace(" ", "0");
 	}
 
 	public static String rightPad(String s, int width) {
 		return String.format("%-" + width + "s", s).replace(" ", "0");
 	}
 
 	public static String readAccessToken() {
 		String accessToken = "";
 
 		try {
 			String line = null;
 			File f = new File(dataDirectoryPath + "/access_token.txt");
 
 			BufferedReader reader = new BufferedReader(new FileReader(f));
 
 			while ((line = reader.readLine()) != null) {
 				accessToken = line;
 			}
 			reader.close();
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 		return accessToken;
 	}
 
 	public static boolean writeAccessToken(String accessToken) {
 		boolean success = false;
 		try {
 
 			File f = new File(dataDirectoryPath + "/access_token.txt");
 
 			if (f.exists()) {
 				f.delete();
 			}
 
 			if (!f.exists()) {
 				f.createNewFile();
 			}
 
 			FileWriter writer = new FileWriter(f, true);
 
 			writer.write(accessToken);
 			writer.close();
 
 			success = true;
 
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 		return success;
 	}
 
 	public static boolean removeAccessToken() {
 		boolean success = false;
 
 		try {
 			File f = new File(dataDirectoryPath + "/access_token.txt");
 			success = f.delete();
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 		return success;
 	}
 
 	public static String trimCodeFromStation(String station) {
 		if (station.length() > 4) {
 			if (station.substring(0, 4).matches("([A-Z]){3} ")) {
 				station = station.substring(4);
 			}
 		}
 		return station;
 	}
 
 	public static String trimNameFromStation(String station) {
 		if (station.length() > 3) {
 			if (station.substring(0, 3).matches("([A-Z]){3}")) {
 				station = station.substring(0, 3);
 			}
 		}
 		return station;
 	}
 
 	public static String nameAndCodeFromStation(String station) {
 		if (station.length() > 4) {
			if (station.matches("([A-Z]){3} ")) {
 				station = trimCodeFromStation(station) + " ("
 						+ trimNameFromStation(station) + ")";
 			}
 		}
 		return station;
 	}
 
 	public static Bundle saveCurrentJourney(Bundle previousBundle, Activity src) {
 		Bundle journey = new Bundle();
 
 		// Static
 
 		if (previousBundle.containsKey("editing")) {
 			journey.putBoolean("editing", previousBundle.getBoolean("editing"));
 		}
 
 		if (previousBundle.containsKey("id")) {
 			journey.putInt("id", previousBundle.getInt("id"));
 		}
 
 		// Possibly Updated in View
 
 		AutoCompleteTextView actv_FromSearch = (AutoCompleteTextView) src
 				.findViewById(R.id.actv_FromSearch);
 		DatePicker dp_FromDate = (DatePicker) src
 				.findViewById(R.id.dp_FromDate);
 		TimePicker tp_FromTime = (TimePicker) src
 				.findViewById(R.id.tp_FromTime);
 		journey.putString("from_stn", actv_FromSearch.getText().toString());
 		journey.putInt("from_date_day", dp_FromDate.getDayOfMonth());
 		journey.putInt("from_date_month", dp_FromDate.getMonth());
 		journey.putInt("from_date_year", dp_FromDate.getYear());
 		journey.putInt("from_time_hour", tp_FromTime.getCurrentHour());
 		journey.putInt("from_time_minute", tp_FromTime.getCurrentMinute());
 
 		CheckBox chk_DetailClass = (CheckBox) src
 				.findViewById(R.id.chk_DetailClass);
 		TextView txt_DetailClass = (TextView) src
 				.findViewById(R.id.txt_DetailClass);
 		CheckBox chk_DetailHeadcode = (CheckBox) src
 				.findViewById(R.id.chk_DetailHeadcode);
 		TextView txt_DetailHeadcode = (TextView) src
 				.findViewById(R.id.txt_DetailHeadcode);
 		journey.putBoolean("detail_class_enabled", chk_DetailClass.isChecked());
 		journey.putString("detail_class", txt_DetailClass.getText().toString());
 		journey.putBoolean("detail_headcode_checked",
 				chk_DetailHeadcode.isChecked());
 		journey.putString("detail_headcode", txt_DetailHeadcode.getText()
 				.toString());
 
 		AutoCompleteTextView actv_ToSearch = (AutoCompleteTextView) src
 				.findViewById(R.id.actv_ToSearch);
 		DatePicker dp_ToDate = (DatePicker) src.findViewById(R.id.dp_ToDate);
 		TimePicker tp_ToTime = (TimePicker) src.findViewById(R.id.tp_ToTime);
 		journey.putString("to_stn", actv_ToSearch.getText().toString());
 		journey.putInt("to_date_day", dp_ToDate.getDayOfMonth());
 		journey.putInt("to_date_month", dp_ToDate.getMonth());
 		journey.putInt("to_date_year", dp_ToDate.getYear());
 		journey.putInt("to_time_hour", tp_ToTime.getCurrentHour());
 		journey.putInt("to_time_minute", tp_ToTime.getCurrentMinute());
 
 		return journey;
 	}
 
 	public static void loadCurrentJourney(Bundle journey, Activity dest) {
 		if (journey != null) {
 			if (journey.containsKey("from_stn")) {
 				if (journey.getString("from_stn").length() > 0) {
 					AutoCompleteTextView actv_FromSearch = (AutoCompleteTextView) dest
 							.findViewById(R.id.actv_FromSearch);
 					actv_FromSearch.setText(journey.getString("from_stn"));
 				}
 			}
 
 			if (journey.containsKey("from_date_year")) {
 				if (journey.getInt("from_date_year")
 						+ journey.getInt("from_date_month")
 						+ journey.getInt("from_date_day") > 0) {
 					DatePicker dp_FromDate = (DatePicker) dest
 							.findViewById(R.id.dp_FromDate);
 					dp_FromDate.init(journey.getInt("from_date_year"),
 							journey.getInt("from_date_month") - 1,
 							journey.getInt("from_date_day"), null);
 				}
 			}
 
 			if (journey.containsKey("from_time_hour")) {
 				if (journey.getInt("from_time_hour")
 						+ journey.getInt("from_time_minute") > 0) {
 					TimePicker tp_FromTime = (TimePicker) dest
 							.findViewById(R.id.tp_FromTime);
 					tp_FromTime
 							.setCurrentHour(journey.getInt("from_time_hour"));
 					tp_FromTime.setCurrentMinute(journey
 							.getInt("from_time_minute"));
 				}
 			}
 
 			if (journey.containsKey("detail_class_checked")) {
 				CheckBox chk_DetailClass = (CheckBox) dest
 						.findViewById(R.id.chk_DetailClass);
 				chk_DetailClass.setChecked(journey
 						.getBoolean("detail_class_checked"));
 
 				TextView txt_DetailClass = (TextView) dest
 						.findViewById(R.id.txt_DetailClass);
 				txt_DetailClass.setEnabled(journey
 						.getBoolean("detail_class_checked"));
 			}
 
 			if (journey.containsKey("detail_class")) {
 				if (journey.getString("detail_class").length() > 0) {
 					TextView txt_DetailClass = (TextView) dest
 							.findViewById(R.id.txt_DetailClass);
 					txt_DetailClass.setText(journey.getString("detail_class"));
 				}
 			}
 
 			if (journey.containsKey("detail_headcode_checked")) {
 				CheckBox chk_DetailHeadcode = (CheckBox) dest
 						.findViewById(R.id.chk_DetailHeadcode);
 				chk_DetailHeadcode.setChecked(journey
 						.getBoolean("detail_headcode_checked"));
 
 				TextView txt_DetailHeadcode = (TextView) dest
 						.findViewById(R.id.txt_DetailHeadcode);
 				txt_DetailHeadcode.setEnabled(journey
 						.getBoolean("detail_headcode_checked"));
 			}
 
 			if (journey.containsKey("detail_headcode")) {
 				if (journey.getString("detail_headcode").length() > 0) {
 					TextView txt_DetailHeadcode = (TextView) dest
 							.findViewById(R.id.txt_DetailHeadcode);
 					txt_DetailHeadcode.setText(journey
 							.getString("detail_headcode"));
 				}
 			}
 
 			if (journey.containsKey("to_stn")) {
 				if (journey.getString("to_stn").length() > 0) {
 					AutoCompleteTextView actv_ToSearch = (AutoCompleteTextView) dest
 							.findViewById(R.id.actv_ToSearch);
 					actv_ToSearch.setText(journey.getString("to_stn"));
 				}
 			}
 
 			if (journey.containsKey("to_date_year")) {
 				if (journey.getInt("to_date_year")
 						+ journey.getInt("to_date_month")
 						+ journey.getInt("to_date_day") > 0) {
 					DatePicker dp_ToDate = (DatePicker) dest
 							.findViewById(R.id.dp_ToDate);
 					dp_ToDate.init(journey.getInt("to_date_year"),
 							journey.getInt("to_date_month") - 1,
 							journey.getInt("to_date_day"), null);
 				}
 			}
 
 			if (journey.containsKey("to_time_hour")) {
 				if (journey.getInt("to_time_hour")
 						+ journey.getInt("to_time_minute") > 0) {
 
 					TimePicker tp_ToTime = (TimePicker) dest
 							.findViewById(R.id.tp_ToTime);
 					tp_ToTime.setCurrentHour(journey.getInt("to_time_hour"));
 					tp_ToTime
 							.setCurrentMinute(journey.getInt("to_time_minute"));
 				}
 			}
 		}
 	}
 
 	public static String guageSizeToName(String guage) {
 		String result = guage;
 
 		String[] definitions = new String[] { "broad", "standard", "medium",
 				"metre", "narrow", "minumum" };
 		Hashtable<String, String> relationships = new Hashtable<String, String>();
 		relationships.put("1435", definitions[0]);
 
 		if (guage == "") {
 			result = "(unknown)";
 		}
 		if (relationships.containsKey(guage)) {
 			result = relationships.get(guage);
 		}
 
 		return result;
 	}
 }
