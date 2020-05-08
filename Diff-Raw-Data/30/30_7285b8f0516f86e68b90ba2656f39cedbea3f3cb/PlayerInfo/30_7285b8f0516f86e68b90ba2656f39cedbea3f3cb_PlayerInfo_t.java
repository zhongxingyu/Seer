 package se.chalmers.chessfeud.utils;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Scanner;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * This is a class that hold all the information about the client user. It saves
  * data to the internal memory for automatic login. It also uses the database to
  * see if the login is valid.
  * 
  * @author Simon Almgren Henrik Alburg
  * 
  *         Copyright (c) 2012 Simon Almgren Henrik Alburg
  */
 public class PlayerInfo {
 
 	private static PlayerInfo instance = null;
 
 	private String userName;
 	private String password;
 	private boolean loggedIn;
 	private boolean soundSettings;
 	private boolean helpTip;
 
 	/**
 	 * A singleton class for handling info about the player currently logged in.
 	 */
 	protected PlayerInfo() {
 	}
 
 	/**
 	 * If there isnt an instance, it creates an instance and initiates it. else
 	 * it just returns the instance.
 	 * 
 	 * @return the player singleton
 	 */
 	public static PlayerInfo getInstance() {
 		if (instance == null) {
 			instance = new PlayerInfo();
 		}
 		return instance;
 	}
 
 	/**
 	 * Returns the username of the currently logged in player.
 	 * 
 	 * @return username the name of the user currently logged in
 	 */
 	public String getUserName() {
 		return userName;
 	}
 
 	/**
 	 * Returns the encrypted password of the currently logged in player.
 	 * 
 	 * @return the password for the logged in user.
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/*
 	 * Loads the username and the password from a textfile, if there isnt any,
 	 * sets loggedIn to false.
 	 */
 	public void loadInfoFromFile(Context c) {
 		loadUserinfo(c);
 		loadSettings(c);
 	}
 	
 	/* Loads the user info from the user-file.  
 	 * If a file cannot be found, the variable are left empty*/
 	private void loadUserinfo(Context c) {
 		String[] stuffLines = getLinesFromFile(C.FILENAME_USERFILE, c);
 		if (stuffLines != null && stuffLines.length > 1) {
 			userName = stuffLines[0];
 			password = stuffLines[1];
 		} else {
 			userName = "";
 			password = "";
 		}
 	}
 
 	/* Loads the settings from the settings-file 
 	 * If there is no settings-file , one will be created 
 	 */
 	private void loadSettings(Context c){
 		String[] settingsLines = getLinesFromFile(C.FILENAME_SETTINGS, c);
 		if (settingsLines != null) {
 			String[] help = settingsLines[0].split("\t");
 			helpTip = ((int)Integer.parseInt(help[1])) == 1;
 			String[] sound = settingsLines[1].split("\t");
 			soundSettings = ((int)Integer.parseInt(sound[1])) == 1;
 		} else {
 			createSettingsFile(c);
 		}
 	}
 
 	/* Create the file containing the settings. */
 	private void createSettingsFile(Context c) {
 		String fileContent = "Helptip:\t1\nSound:\t1";
 		helpTip = true;
 		soundSettings = true;
 		writeStringToFile(fileContent, "chessfeud_settings", c);
 	}
 
 	/**
 	 * Returns the state of sound settings.
 	 * 
 	 * @return true if sound is enabled.
 	 */
 	public boolean getSoundEnabled() {
 		return soundSettings;
 	}
 
 	/**
 	 * Returns the state of helptip.
 	 * 
 	 * @return true if helptip is enabled.
 	 */
 	public boolean getHelpTip() {
 		return helpTip;
 	}
 
 	/**
 	 * Sets the string for the settings.
 	 * 
 	 * @param settingsId
 	 *            The constant id of the setting to be changed.
 	 * @param newValue
 	 *            The value to change it to.
 	 * @param c
 	 *            The context allowing files to be written to.
 	 */
 	public void setString(int settingsId, int newValue, Context c) {
 		StringBuilder newString = new StringBuilder("");
 		String[] lines = getLinesFromFile(C.FILENAME_SETTINGS, c);
 		if (lines != null) {
 			for (int rowNbr = 0; rowNbr < lines.length; rowNbr++) {
 				if (rowNbr == settingsId) {
 					String[] split = lines[rowNbr].split("\t");
 					newString.append(split[0]+"\t"+newValue + "\n");
 				} else {
 					newString.append(lines[rowNbr] + "\n");
 				}
 			}
 			writeStringToFile(newString.toString(), C.FILENAME_SETTINGS, c);
 		} else {
 			Log.e(C.EXCEPTION_LOCATION_SETTINGS,
 					"Error when trying to read from text file.");
 		}
 		loadSettings(c);
 	}
 
 	/**
 	 * Returns if the user is currently logged in.
 	 * 
 	 * @return true if the user is loggedIn.
 	 */
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 
 	/**
 	 * This method checks the username and password with the database and checks
 	 * if you should be able to log in, then writes a textfile with username and
 	 * password.
 	 * 
 	 * @param userName
 	 *            The name of the user.
 	 * @param password
 	 *            The wanted password by the user.
 	 * @return true if able to login.
 	 */
 	public boolean login(String username, String password, Context c) {
 		boolean suc = DbHandler.getInstance().login(username, password);
 		if (suc) {
 			this.userName = username;
 			this.password = password;
 			String s = username + "\n" + password;
 			writeStringToFile(s, C.FILENAME_USERFILE, c);
 		}
 		return suc;
 	}
 
 	/**
 	 * Logs out the user. This is done by resetting the user/pass in the file
 	 * and also the variables.
 	 * 
 	 * @param c
 	 *            The current context (from an activity)
 	 */
 	public void logout(Context c) {
 		writeStringToFile("", C.FILENAME_USERFILE, c);
 		this.userName = ""; 
 		this.password = "";
 		this.loggedIn = false;
 	}
 
 	/* Writes the given string to the given file */
 	private boolean writeStringToFile(String text, String fileName, Context c) {
 		try {
 			FileOutputStream out = c.openFileOutput(fileName,
 					Context.MODE_PRIVATE);
 			out.write(text.getBytes());
 			out.close();
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/* Returns the String in the given file */
 	private String[] getLinesFromFile(String fileName, Context c) {
 		StringBuilder fileContent = new StringBuilder("");
 		try {
 			FileInputStream fis = c.openFileInput(fileName);
 			Scanner sc = new Scanner(fis);
 			while (sc.hasNext()) {
 				fileContent.append(sc.nextLine() + "\n");
 			}
 			if(fileContent.length() > 2){
 				fileContent.delete(fileContent.length() - 1, fileContent.length());
 			}
 			
 		} catch (IOException e) {
 			return null;
 		}
 		return fileContent.toString().split("\n");
 	}
 }
