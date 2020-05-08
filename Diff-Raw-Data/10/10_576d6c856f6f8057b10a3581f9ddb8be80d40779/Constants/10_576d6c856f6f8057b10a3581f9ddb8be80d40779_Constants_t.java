 package com.github.pozo.volan.utils;
 
 public final class Constants {
 	public static final class IntentParameters {
		public final static String KEY_DIRECTION = "toback";
 		public final static String KEY_LINE_STOP = "stop";
 		public final static String KEY_LINE = "line";
		public final static String VALUE_DIRECTION_TO = "to";
		public final static String VALUE_DIRECTION_BACK = "back";
 	}
 
 	public static final class SharedPreferences {
 		public final static int FAVORITE_LIST_EDIT = 1;
 		public final static int FAVORITE_LIST_BASIC = 0;
 	}
 
 	public static enum Cities {
 		GYOR("Győr", "gyor","gyor_zold", 0), 
 		SOPRON("Sopron", "sopron","sopron_red", 1), 
 		MOVAR("Mosonmagyaróvár", "movar","movar_gyor_kek", 2);
 
 		private final String name;
 		private final String colorValueName;
 		private final int dialogLine;
 		private final String folderName;
 
 		Cities(String showName, String folderName, String colorValueName, int dialogLine) {
 			this.name = showName;
 			this.folderName = folderName;
 			this.colorValueName = colorValueName;
 			this.dialogLine = dialogLine;
 		}
 
 		public String getName() {
 			return name;
 		}
 		public String getColorValueName() {
 			return colorValueName;
 		}
 		public String getCityFolderName() {
 			return folderName;
 		}
 		public int getDialogLine() {
 			return dialogLine;
 		}
 
 		public static String getDefaultName() {
 			return Cities.GYOR.getName();
 		}
 		public static String getDefaultCityFolderName() {
 			return Cities.GYOR.getCityFolderName();
 		}
 		public static String[] getCities() {
 			String[] retval = new String[Cities.values().length];
 			
 			int i = 0;
 			for (Cities cities : Cities.values()) {
                retval[i++] = cities.getName();
 	        }
 			return retval;
 		}
 		public static String getCitynameByFolder(String folderName) {
 			for (Cities cities : Cities.values()) {
                 if(cities.getCityFolderName().equals(folderName)) {
                 	return cities.getName();
                 }
 	        }
 			return null;
 		}
 		public static String getCityFolderByName(String cityName) {
 			for (Cities cities : Cities.values()) {
                 if(cities.getName().equals(cityName)) {
                 	return cities.getCityFolderName();
                 }
 	        }
 			return null;
 		}
 	}
 	public static final class XMLAttributes {
 		public final static String XML_FOLDER = "raw";
 		public final static String LINES_POSTFIX = "lines";
 		public final static String STOPS_POSTFIX = "stops";
 		public final static String LINES_TO = "to";
 		public final static String LINES_LINE = "line";
 		public final static String LINES_BACK = "back";
 		public final static String LINES_STOP = "stop";
 	}
 }
