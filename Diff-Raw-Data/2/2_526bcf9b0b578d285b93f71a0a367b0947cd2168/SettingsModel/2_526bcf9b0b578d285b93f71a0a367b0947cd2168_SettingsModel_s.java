 package model.save;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Locale;
 
 
 /**
  * 
  * @author Vidar Eriksson
  *
  */
 public class SettingsModel {	
 	/**
 	 * 
 	 * @return the in game language.
 	 */
 	public static Locale getLocale(){
 		return SettingsWrapper.getLocale();
 	}
 	/**
 	 * sets the in game language.
 	 * @param locale the new language to be set.
 	 */
 	public static void setLocale(Locale locale) {
 		SettingsWrapper.setLocale(locale);
 	}
 	/**
 	 * 
 	 * @return the name of the active player.
 	 */
 	public static String getUserName() {
 		return SettingsWrapper.getUserName();
 	}
 	/**
 	 * Sets the username.
 	 * @param text the new username to be set.
 	 */
 	public static void setUserName(String text) {
 		SettingsWrapper.setName(text);	
 	}
 	/**
 	 * 
 	 * @return <code>true<code> if the system is to run at fullscreen.
 	 */
 	public static boolean getFullscreen() {
 		return SettingsWrapper.getFullscreen();
 	}
 	/**
 	 * Set the systems fullscreen  preference.
 	 * @param b the fullscreen preference to be used.
 	 */
 	public static void setFullscreen(boolean b) {
 		SettingsWrapper.setFullscreen(b);
 	}
 	/**
 	 * Saves the current settings to file.
 	 */
 	public static void save() {
 		SettingsWrapper.write();
 	}
 	/**
 	 * 
 	 * @return a list of the available languages.
 	 */
 	public static Locale[] getAllLocales() {
 		return SettingsWrapper.getAllLocales();
 	}
 	
 	
 	private static class SettingsWrapper{
 		private static final String DATA_DIVIDER = "#";
 		
 		private static String name;
 		private static Locale locale;
 		private static boolean fullscreen;
 
 		private static boolean getFullscreen() {
 			read();
 			return fullscreen;
 		}
 		private static String getUserName() {
 			read();
 			return name;
 		}
 		private static Locale getLocale() {
 			read();
 			return locale;
 		}
 		private static void setLocale(Locale l) {
 			locale = l;
 			write();			
 		}
 		private static void setFullscreen(boolean b) {
 			fullscreen = b;
 			write();		
 		}
 		private static void setName(String text) {
 			text.replace(DATA_DIVIDER, "");
 			name = text;
 			write();			
 		}
 		
 
 		private static void read() {
 			String temp = null;
 			try {
 				File file = new File(SavePath.getSettingsPath());
 				if(file.exists()) {
 					FileReader fileReader = new FileReader(file.getAbsoluteFile());
 					BufferedReader bufferedReader = new BufferedReader(fileReader);
 					temp=bufferedReader.readLine();
 					bufferedReader.close();
 					fileReader.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				setDefaultSettings();
 			}
 			if (temp!=null){
 				convertFromString(temp);
 			} else { 
 				setDefaultSettings();
 			}
 		}
 		private static void setDefaultSettings() {
 			name = "Player 1";
 			fullscreen = true;
			locale = Locale.getDefault();
 		}	
 		private static void write() {
 			try {				 			
 				File file = new File(SavePath.getSettingsPath());
 	 
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 	 
 				FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
 				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
 				bufferedWriter.write(convertToSaveableString());
 				bufferedWriter.close();
 				fileWriter.close();
 	 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		
 		}
 		
 		private static String convertToSaveableString(){
 			String temp="";
 			
 			if (fullscreen){
 				temp+=1;
 			} else {
 				temp+=0;
 			}
 			temp += DATA_DIVIDER;
 			
 			temp += name + DATA_DIVIDER;
 			
 			temp += toSaveString(locale) + DATA_DIVIDER;
 
 			return temp;
 			
 		}
 		private static void convertFromString(String temp) {
 			if (temp.charAt(0) == '0' ){
 				fullscreen = false;
 			} else {
 				fullscreen = true;
 			}
 			
 			temp = temp.substring(temp.indexOf(DATA_DIVIDER)+1);
 			
 			name = temp.substring(0, temp.indexOf(DATA_DIVIDER));
 			temp = temp.substring(temp.indexOf(DATA_DIVIDER)+1);
 			
 			locale = loadFrom(temp.substring(0, temp.indexOf(DATA_DIVIDER)));
 			
 		}
 		
 		private static Locale loadFrom(String s){
 			if (s.contains("SWE")){
 				return new Locale("sv_SE");
 			} else {
 				return Locale.ENGLISH;
 			}
 		}
 		/**
 		 * converts a language to a savable string.
 		 * @return the string to be saved.
 		 */
 		private static String toSaveString(Locale l){
 			if (l.equals(new Locale("sv_SE"))){
 				return "SWE";
 			} else {
 				return "ENG";
 			}
 		}
 		
 		
 		private static Locale[] getAllLocales() {
 			return new Locale[]{new Locale("sv_SE"), Locale.ENGLISH};
 		}
 	}
 
 }
