 package main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Random;
 import java.util.Vector;
 
 import gui.MainFrame;
 
 import javax.swing.ImageIcon;
 
 import org.xnap.commons.i18n.I18n;
 import org.xnap.commons.i18n.I18nFactory;
 
 /**
  * A singleton class which holds various utility methods for purposes
  * such as Locale handling, stdout/stderr handling, and file handling.
  * <p>
  * Always use the static get() method to reference to get the current
  * instance of this class.
  * <p>
  * At some point this class might be put into its own util package,
  * with some of the methods split out in own classes.
  */
 public class Util {
 	private static final Util INSTANCE = new Util();
 	private Util() {
 	}
 	/*private static class UtilSingletonHolder { 
 		public static final Util INSTANCE = new Util();
 	}*/
 	/**
 	 * If an instance of Util already exists, this method returns it.
 	 * Otherwise it constructs an instance of Util and returns it.
 	 * @return the singleton instance of the Util class.
 	 */
 	public static Util get() {
 		try {
 		return INSTANCE;
 		} catch(Throwable t) {
 			t.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static final int VERBOSITY_ERROR = 0;
 	public static final int VERBOSITY_WARNING = 1;
 	public static final int VERBOSITY_DEBUG_INFO = 2;
 	public static final int VERBOSITY_DETAILED_DEBUG_INFO = 3;
 	
 	/**
 	 * Constructs a string representation of a duration from a
 	 * value in milliseconds. Note that this is NOT for representing
 	 * a time of day (e.g. half past eight), rather an amount of time
 	 * (e.g. eight minutes and thirty seconds).
 	 * @param milliSeconds the amount of milliseconds to be converted
 	 * into a string representation
 	 * @return string representation on the form MM:SS
 	 */
 	public String millisToMinsSecsString(long milliSeconds) {
 		long totalMinutes = milliSeconds / 1000;
 		int hours = (int)(totalMinutes / 60);
 		int minutes = (int)(totalMinutes % 60);
 		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
 	}
 	
 	/**
 	 * Constructs an instance of ImageIcon to represent an icon on e.g.
 	 * a button in the GUI. The path representation is relative from
 	 * the position of the MainFrame class. This means to refer to
 	 * something not in the gui package (which contains the MainFrame
 	 * class), use something like "../somepackage/somefile"
 	 * @param relativePath path relative to the MainFrame class. The path
 	 * must lead to a graphics file (png, gif, etc.)
 	 * @return instance of ImageIcon, ready for e.g. the constructor
 	 * of a JButton
 	 */
 	public ImageIcon createImageIcon(String relativePath) {
 		URL url = MainFrame.class.getResource(relativePath);
 		if (url == null) {
 			out("Resource not found: " + relativePath, VERBOSITY_ERROR);
 			System.exit(1);
 		}
 		return new ImageIcon(url);
 	}
 	
 	/**
 	 * Creates a randomly chosen string representation of a filename
 	 * using only lower case letters from a to z, with the suffix
 	 * .wav
 	 * @return randomly chosen filename as a String
 	 */
 	public String createLowerCaseRandomWAVFilename() {
 		Random rand = new Random();
 		StringBuffer str = new StringBuffer();
 		int nameLength = 16;
 		int firstChar = 97; // 'a'
 		int lastChar = 122; // 'z'
 		
 		for (int i = 0; i < nameLength; i++) {
 			str.append((char)(rand.nextInt(lastChar - firstChar + 1) + firstChar));
 		}
 		str.append(".wav");
 		return str.toString();
 	}
 	
 	/**
 	 * Creates an new instance of File, having a file name that adheres to
 	 * the rules of createLowerCaseRandomWAVFilename() and points to
 	 * a non-existing file in the data store directory (as given by the
 	 * getDataStoreDir() method. Further, this method ensures that a file
 	 * with the same filename doesn't already exist in that directory (though
 	 * unlikely as it is)
 	 * @return a unique file instance for storing newly recorded audio data 
 	 */
 	public File createUniqueLowerCaseRandomWAVFileInDataDir() {
 		File outFile = null;
 		while (outFile == null || outFile.exists()) { // make sure file doesn't already exist (though unlikely)
 			outFile = new File(getDataStoreDir(), createLowerCaseRandomWAVFilename());
 		}
 		return outFile;
 	}
 	
 	/**
 	 * Deletes a file, first making sure that the file exists, is not a
 	 * directory, and is writable.
 	 * @param file the File instance which points to the file to delete
 	 */
 	public void deleteFile(File file) {
 		if (file.exists() && !file.isDirectory() && file.canWrite()) {
 			file.delete();
 			out("Deleting file " + file.getName(), VERBOSITY_DEBUG_INFO);
 		}
 	}
 	
 	private File dataDir = null;
 	
 	/**
 	 * Gets a File representation of the directory, in which to
 	 * store audio data files.
 	 * <p>
 	 * The location is determined by first looking for a saved
 	 * location in the preferences file. If none such is found
 	 * (e.g. at first run of the program), or if it is invalid,
 	 * for some reason, the location is determined in the
 	 * following order (whereafter it is saved to preferences):
 	 * <ul>
 	 * <li>If running on Windows, use a subfolder under the
 	 * system's default location for application data, if
 	 * writable. If not:
 	 * <li>Use a subdirectory under the current user's home
 	 * directory (as is usually done under e.g. Linux), if
 	 * writable.
 	 * </ul>
 	 * If no writable directory was found as described above,
 	 * the method returns null.
 	 * 
 	 * @return an instance of File which points to a writable
 	 * data directory, stored in preferences, or null if no
 	 * suiting data directory was found.
 	 */
 	public File getDataStoreDir() {
 		// If we already figured out the data store dir in this session, no need to do so again
 		if (dataDir != null)
 			return dataDir;
 		
 		// Get dir from preferences, if exists and is still valid
 		String prefsDirStr = Prefs.prefs.get(Prefs.DATA_DIR, null);
 		if (prefsDirStr != null) {
 			File prefsDir = new File(prefsDirStr);
 			if (prefsDir != null && prefsDir.exists() && prefsDir.isDirectory() && prefsDir.canWrite()) {
 				dataDir = prefsDir;
 				return dataDir;
 			}
 		}
 		
 		// Figure out where to store data
 		File appDataDir = null;
 		String sysName = System.getProperty("os.name").toLowerCase();
 		if (sysName.contains("windows")) { // Windows special case
 			String winDirStr = System.getenv("APPDATA");
 			if (winDirStr != null) {
 				File winDir = new File(winDirStr);
 				if (winDir != null && winDir.exists() && winDir.isDirectory() && winDir.canWrite()) {
 					appDataDir = winDir;
 				}
 			}
 		}
 		
 		if (appDataDir == null) { // default: the current user's home directory
 			String homeDirStr = System.getProperty("user.home");
 			if (homeDirStr != null) {
 				File homeDir = new File(homeDirStr);
 				if (homeDir != null && homeDir.exists() && homeDir.isDirectory() && homeDir.canWrite()) {
 					appDataDir = homeDir;
 				}
 			}
 		}
 		
 		if (appDataDir == null) {
 			return null;
 		} else {
 		    // by now we have a writable dir in which we can make our own dir or use it if it already exists
 			File saveDir = new File(appDataDir, ".spotmachine");
 			if (!saveDir.exists()) {
 				out("Creating data directory " + saveDir.getAbsolutePath(), VERBOSITY_DEBUG_INFO);
 				saveDir.mkdir();
 			}
 			Prefs.prefs.put(Prefs.DATA_DIR, saveDir.getAbsolutePath());
 			return saveDir;
 		}
 	}
 	
 	/**
 	 * Convert an instance of Vector containing instances of
 	 * Integers into an array of primitive type ints. The returned
 	 * array is guaranteed to be of the same length as the size
 	 * of the given Vector.
 	 * @param source a Vector instance to be converted. May be empty
 	 * @return the values of the Integers expressed as int array
 	 */
 	public int[] IntegerVectorToIntArray(Vector<Integer> source) {
 		int[] target = new int[source.size()];
 		for (int i = 0; i < source.size(); i++) {
 			target[i] = source.get(i).intValue();
 		}
 		return target;
 	}
 	
 	/**
 	 * Sorts an int array by value in descending numerical order.
 	 * Uses java.util.Arrays.sort(int[]) to sort into ascending
 	 * order, and then reverses the order to descending by
 	 * traversing half-way through the array, swapping the values
 	 * along the way.
 	 * <p>
 	 * Notice, this method will sort the array fed to it, rather
 	 * than create a new sorted array!
 	 * 
 	 * @param intArray an array of ints to be sorted into descending
 	 * numerical order.
 	 * 
 	 * @see #java.util.Arrays.sort(int[])
 	 */
 	public void reverseSort(int[] intArray) {
 		Arrays.sort(intArray);
 		for(int i = 0; i < intArray.length / 2; i++) {
 		     int temp = intArray[i];
 		     intArray[i] = intArray[intArray.length - (i + 1)];
 		     intArray[intArray.length - (i + 1)] = temp;
 		}
 	}
 	
 	private Locale savedLocale;
 	
 	/**
 	 * If a Locale was previously (in this session) found by this method,
 	 * that Locale is returned. Otherwise the method will check the preferences
 	 * file for a saved Locale. If a Locale is saved, it is stored in a private
 	 * variable for faster reference in this session, then the Locale is set as
 	 * default locale for this session, and then returned. If no Locale is
 	 * saved, null is returned.
 	 * @return the saved Locale, or null if no Locale is saved
 	 */
 	public Locale getSavedLocale() {
 		String lang = Prefs.prefs.get(Prefs.LOCALE_LANGUAGE, Prefs.LOCALE_LANGUAGE_DEFAULT);
 		String coun = Prefs.prefs.get(Prefs.LOCALE_COUNTRY, Prefs.LOCALE_COUNTRY_DEFAULT);
 		
 		if (savedLocale == null && lang != null && coun != null) {
 			savedLocale = new Locale(lang, coun);
 			Locale.setDefault(savedLocale);
 		}
 		return savedLocale;
 	}
 	
 	private Locale currentLocale;
 	
 	/**
 	 * Returns the currently used locale, i.e.:
 	 * <ul>
 	 * <li>the one which is saved in the preferences file (jf.
 	 * {@link #getSavedLocale()}) ,if exists, otherwise:
 	 * <li>the system's default, if supported, otherwise:
 	 * <li>the first mentioned locale in the list of supported locales
 	 * (i.e. the program default).
 	 * </ul>
 	 * The first time this method is alled in a session, the locale is
 	 * saved in a private variable for faster access upon following
 	 * calls.
 	 * <p>
 	 * If the list of supported locales contains at least one valid
 	 * locale, this method is guaranteed to not return a valid locale.
 	 * @return the currently used locale, as found in preferences, the
 	 * system default, or the program default
 	 */
 	public Locale getCurrentLocale() {
 	    if (currentLocale != null)
 	        return currentLocale;
 	    
 		currentLocale = getSavedLocale();
 		if (currentLocale == null) {
 			currentLocale = Locale.getDefault();
             Util.get().out("No locale is saved. Getting system's default locale, " + currentLocale.getDisplayName(), Util.VERBOSITY_DEBUG_INFO);
 			if (!isLocaleSupported(currentLocale)) {
 	            currentLocale = getSupportedLocales()[0];
                 Util.get().out("The system's default locale is not supported. Getting program's default locale, " + currentLocale.getDisplayName(), Util.VERBOSITY_DEBUG_INFO);
 			}
 		}
 		return currentLocale;
 	}
 	
 	/**
 	 * Checks if a given Locale is supported by this program, that is, if
 	 * there is a translation file for that Locale. Note that the method
 	 * calls {@link #getSupportedLocales()} which in turn might read from
 	 * disk.
 	 * @param candidate an instance of Locale on which to test for support
 	 * @return true if there is a language file for this Locale, false
 	 * otherwise
 	 */
 	public boolean isLocaleSupported(Locale candidate) {
 	    Locale[] supportedLocales = getSupportedLocales();
 	    for (int i = 0; i < supportedLocales.length; i++) {
 	        if (supportedLocales[i].equals(candidate))
 	            return true;
 	    }
 	    return false;
 	}
 		
 	private I18n i18n = null;
 	
 	/**
 	 * Gets the I18n object used for getting a translated string for
 	 * display to end users.
 	 * @return the I18n object for the current locale
 	 */
 	public I18n i18n() {
 		// TODO: not thread safe! But maybe getI18n() is, so it doesn't matter?
 		if (i18n == null)
 			i18n = I18nFactory.getI18n(getClass(), "i18n.Messages", getCurrentLocale());
 		return i18n;
 	}
 	
 	private Locale[] supportedLocales;
 	
 	/**
 	 * Returns an array of Locale instances which corresponds to the
 	 * country/language codes listed in the strings.list file. Empty
 	 * lines, and lines starting with a # (after whitespace has been
 	 * trimmed) are ignored.
 	 * <p>
 	 * The file must contain one locale per line, every locale being
 	 * a sequence on the format ll_CC. That is, a two-letter language
 	 * code in lower-case, an underscore, and finally a two-letter
 	 * country code in upper-case.
 	 * <p>
 	 * If all locale code sequences in the file are found to be
 	 * correctly formatted, and a Locale instance could be created
 	 * for each code sequence, an array of these Locale instances is
 	 * returned. Otherwise a Locale array of length 0 is returned.
 	 * <p>
 	 * If successful, the list of Locales is stored in a private
 	 * variable for further reference without having to read the file
 	 * again. Only, if something goes wrong, and an empty array is
 	 * returned, the following call of this method will try to read
 	 * the file again.
 	 * 
 	 * @return an array of Locale instances supported by this program
 	 * or an empty array if locales are malformed.
 	 */
 	public Locale[] getSupportedLocales() {
 	    // If the file has already been read, no need to read it again
 	    // since this method is called at least twice for every string
 	    // in the program.
 	    if (supportedLocales != null)
 	        return supportedLocales;
 	    
		File localeListFile = new File(MainFrame.class.getResource("../po/languages").getFile());
 		BufferedReader reader;
 		Vector<String> localeStrings = new Vector<String>();
 		
 		try {
 			reader = new BufferedReader(new FileReader(localeListFile));
 			String readLine;
 			while ((readLine = reader.readLine()) != null) {
 			    readLine = readLine.trim();
			    if (!readLine.equals("") && readLine.charAt(0) != '#')
 			        localeStrings.add(readLine.trim());
 			}
 			reader.close();
 		} catch (IOException ioe) {
 			out(ioe.toString(), VERBOSITY_ERROR);
 			return new Locale[0];
 		}
 		
 		supportedLocales = new Locale[localeStrings.size()];
 		for (int i = 0; i < supportedLocales.length; i++) {
 			String[] currentLocale = localeStrings.get(i).split("_");
 			if (currentLocale.length == 1) {
 				supportedLocales[i] = new Locale(currentLocale[0]);
 			} else if (currentLocale.length == 2) {
 				supportedLocales[i] = new Locale(currentLocale[0], currentLocale[1]);
 			} else if (currentLocale.length == 3) {
 				supportedLocales[i] = new Locale(currentLocale[0], currentLocale[1], currentLocale[2]);
 			} else {
 				out("At least one line in the 'po/languages' file is incorrect. The file must contain a list " +
 						"of locales, one locale per line, in the form 'll', 'll_CC', or 'll_CC_vv', where ll is " +
 						"a two-letter language code, CC is a two-letter country code, and vv is a variant. " +
 						"Ignoring all locales.", VERBOSITY_ERROR);
 				return new Locale[0];
 			}
 		}
 		return supportedLocales;
 	}
 	
 	/**
 	 * Writes a debugging message followed by a newline to stdout or
 	 * stderr, depending on the verbosity level. This method is
 	 * equivalent to doing outNoLN(text + "\n", verbosityLevel).
 	 * See description of outNoLN(String, int) for details.
      * @param text a debug message to be written to stdout or stderr
      * @param verbosityLevel level determining in which situations to
      * write the message, and whether to write it to stdout or stderr
 	 * 
 	 * @see #outNoLN(String, int)
 	 */
 	public void out(String text, int verbosityLevel) {
 		outNoLN(text + "\n", verbosityLevel);
 	}
 	
 	/**
 	 * Writes a debugging message to stdout or stderr, depending on
 	 * the specified verbosity level and the current verbosity level
 	 * as set on the command line when starting the program.
 	 * <p>
 	 * The message is preceeded by the time at which the message was
 	 * written. Note, that a newline is NOT added to the end of the
 	 * message. If you want one, include it in the text, or use
 	 * {@link #out(String, int)} instead.
 
 	 * <p>
 	 * verbosityLevel is one of:
 	 * <ul>
 	 * <li>{@link #VERBOSITY_ERROR}
      * <li>{@link #VERBOSITY_WARNING}
      * <li>{@link #VERBOSITY_DEBUG_INFO}
      * <li>{@link #VERBOSITY_DETAILED_DEBUG_INFO}
      * </ul>
      * If the program was started without any command line arguments,
      * only messages with the verbosity level of WARNING and above are
      * written out, while the rest are suppressed. If the program is
      * started in debug mode (with the -debug argument), DEBUG_INFO,
      * WARNING, and ERROR messages are written, and so forth. See the
      * command line arguments as listed in {@link #SpotMachine.main(String[])}
      * or run the program with the argument -help to see all command
      * line arguments of the program.
      * <p>
      * Note: Messages of verbosity level {@link #VERBOSITY_ERROR} or
      * {@link #VERBOSITY_WARNING} are written to stderr, while the rest
      * are written to stdout.
      * 
 	 * @param text a debug message to be written to stdout or stderr
 	 * @param verbosityLevel level determining in which situations to
 	 * write the message, and whether to write it to stdout or stderr
 	 */
 	public void outNoLN(String text, int verbosityLevel) {
 		/**
 		 * verbosityLevel
 		 * 0 = Error
 		 * 1 = Warning
 		 * 2 = Debug info
 		 * 3 = Detailed debug info
 		 */
 		
 		if (verbosityLevel <= SpotMachine.currentVerbosityLevel) {
 			Calendar now = Calendar.getInstance();
 			int hour = now.get(Calendar.HOUR_OF_DAY);
 			int minute = now.get(Calendar.MINUTE);
 			int second = now.get(Calendar.SECOND);
 			    // (minutes < 10 ? "0" + minutes : minutes)
 			String time = "[" + (hour < 10 ? "0" + hour : hour) + ":" +
 			        (minute < 10 ? "0" + minute : minute) + ":" +
 			        (second < 10 ? "0" + second : second) + "] ";
 			if (verbosityLevel <= VERBOSITY_WARNING)
 				System.err.print(time + text);
 			else
 				System.out.print(time + text);
 		}
 	}
 	
 	/**
 	 * Method for exception handling of thread sleeping, for ease of use.
 	 * Simply calls {@link #Thread.sleep(long)}. If an exception is
 	 * catched, it is written to stderr (as VERBOSITY_WARNING) and nothing
 	 * else.
 	 * @param millis amount of milliseconds to make the current thread sleep
 	 * 
 	 * @see #Thread.sleep(long)
 	 */
 	public void threadSleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch(Exception e) {
 			Util.get().out(e.toString(), Util.VERBOSITY_WARNING);
 		}
 	}
 	
 	/**
 	 * Word-wraps a long lined string to the given max-width.
 	 * <p>
 	 * If there are already newlines in the input string, the input
 	 * is split at these newlines, and each substring is fed to the
 	 * private method {@link #wordWrapOneLine(String, int)}, after
 	 * which all (now wrapped) substrings are re-joined, with a
 	 * newline in-between each of them.
 	 * @param input a string to be word-wrapped
 	 * @param width maximum length of each line, in characters
 	 * @return new string containing word-wrapped version of input
 	 * 
 	 * @see #wordWrapOneLine(String, int)
 	 */
 	public String wordWrap(String input, int width) {
 	    String[] inputLines = input.split("\n");
 	    StringBuffer output = new StringBuffer();
 	    for (int i = 0; i < inputLines.length; i++) {
 	        if (i != 0)
 	            output.append("\n");
 	        output.append(wordWrapOneLine(inputLines[i], width));
 	    }
 	    return output.toString();
 	}
 	
 	/**
 	 * Word-wraps a single-lined string to the given max-width.
 	 * It is probably safest to always call {@link #wordWrap(String, int)}
 	 * instead of this (private) method, as this method will yield
 	 * unexpected results if called with a string that includes newline
 	 * characters.
 	 * <p>
 	 * If length of input is already less than (or equal to) width,
 	 * input is returned as-is. Otherwise: Finds last space within the
 	 * first 'width' characters of input and splits the string at that
 	 * point. Returns the first substring, followed by a newline character,
 	 * followed by the return of a recursive call of this method on
 	 * the second substring. 
 	 * 
      * @param input a string with no newline characters
      * @param width maximum length of each line, in characters
      * @return new string containing word-wrapped version of input
 	 * 
 	 * @see #wordWrap(String, int)
 	 */
 	private String wordWrapOneLine(String input, int width) {
 	    input = input.trim();
 	    if (input.length() <= width) {
 	        return input;
 	    } else {
 	        int lastSpaceIndex = input.lastIndexOf(" ", width);
 	        if (lastSpaceIndex == -1)
 	            lastSpaceIndex = width;
 	        
 	        String output1 = input.substring(0, lastSpaceIndex).trim() + "\n";
 	        String output2 = input.substring(lastSpaceIndex).trim();
 	        input = null;
 	        return output1 + wordWrapOneLine(output2, width);
 	    }
 	}
 }
