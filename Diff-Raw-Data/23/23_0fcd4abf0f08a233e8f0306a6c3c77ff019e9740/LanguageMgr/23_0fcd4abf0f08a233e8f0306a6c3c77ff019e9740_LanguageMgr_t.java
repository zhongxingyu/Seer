 package de.dhbw.swe.campingplatzverwaltung.common.language_mgt;
 
 import java.io.*;
 import java.lang.reflect.Field;
 import java.util.*;
 
 /**
  * Class to manage the {@link Language}s.
  * 
  * @author GieSeel
  * @version 1.0
  */
 public class LanguageMgr {
     /**
      * The cell separator.
      * */
     private static String CELL_SEPARATOR = ";";
 
     private static final Object DEFAULT_LANGUAGE = "English";
 
     /** The singleton. */
     private static LanguageMgr languageMgr;
     /**
      * The line separator.
      * */
     private static String LINE_SEPARATOR = "\r\n";
 
     private static String PROGRAM_PROPERTY_DIR = System.getProperty("user.home")
 	    + "\\Documents\\Campinplatzverwaltung\\";
 
     static public LanguageMgr getInstance() {
 	if (languageMgr == null) {
 	    languageMgr = new LanguageMgr();
 	}
 	return languageMgr;
     }
 
     /**
      * Constructor.
      * 
      */
     private LanguageMgr() {
 	languages = new HashMap<>();
 	initLanguageFiles();
 	selectLanguage();
     }
 
     /**
      * Gets a particular phrase for the current Language.
      * 
      * @param languageProperty
      *            the {@link LanguageProperties}
      * @return the phrase
      */
     public String get(final String languageProperty) {
 	return selectedLanguage.get(languageProperty);
     }
 
     private void addContent(final String languageProperty, final String value) {
 	content.append(languageProperty);
 	content.append(CELL_SEPARATOR);
 	content.append(value);
 	content.append(LINE_SEPARATOR);
     }
 
     /**
      * Build default language file.
      * 
      * @param defaultFilePath
      *            the default files path
      */
     private void buildDefaultLanguageFile(final String defaultFilePath) {
 	content = new StringBuilder();
 	addContent(LanguageProperties.JUST_A_COMMENT,
 		" Put all words/phrases used in the program in this File!");
 	addContent(LanguageProperties.JUST_A_COMMENT + "To generate new language:",
 		"copy this file into the language "
 			+ "direktory, rename it with the new Language and "
 			+ "translate the property values.");
	addContent(LanguageProperties.JUST_A_COMMENT + " LANGUAGE PROPERTY",
 		"VALUE for this property in this files language");
 	for (final Field field : LanguageProperties.class.getDeclaredFields()) {
 	    field.setAccessible(true);
 	    try {
		addContent(field.get(null).toString(), field.get(null) + "");
 	    } catch (final IllegalArgumentException e) {
 		e.printStackTrace();
 	    } catch (final IllegalAccessException e) {
 		e.printStackTrace();
 	    }
 	}
 	writeFile(defaultFilePath, content);
 	System.out.println("INFO: Wrote default language file at: "
 		+ defaultFilePath);
     }
 
     /**
      * Checks if the default language file already exists.
      * 
      * @param defaultFilePath
      *            the default files path
      * 
      * @return whether it exists or not
      */
     private boolean defaultFileExists(final String defaultFilePath) {
 	final File defaultFile = new File(defaultFilePath);
 	if (defaultFile.exists()) {
 	    System.out.println("INFO: Use existing default language file ("
 		    + defaultFile.getName() + ") at: "
 		    + defaultFile.getAbsolutePath());
 	    return true;
 	}
 	return false;
     }
 
     private void initLanguageFiles() {
 	languageFileDir = new File(PROGRAM_PROPERTY_DIR + "Languages\\");
 	final String defaultFilePath = languageFileDir.getAbsolutePath() + "\\"
 		+ DEFAULT_LANGUAGE + ".csv";
 	if (!languageFileDir.exists()) {
 	    languageFileDir.mkdirs();
 	    buildDefaultLanguageFile(defaultFilePath);
 	    parseLanguageFiles();
 	} else if (defaultFileExists(defaultFilePath)) {
 	    parseLanguageFiles();
 	} else if (languageFileDir.list().length > 0) {
 	    parseLanguageFiles();
 	}
 	if (languages.isEmpty()) {
 	    buildDefaultLanguageFile(defaultFilePath);
 	    parseLanguageFiles();
 	}
     }
 
     private void parseLanguageFiles() {
 	for (final File file : languageFileDir.listFiles()) {
 	    if (file.isFile()) {
 		final Language language = new Language(file);
 		languages.put(language.getName(), language);
 	    }
 	}
 	System.out.println("INFO: Available languages: " + languages.keySet());
     }
 
     /**
      * Selects language. Prefer first local, then default and finally one of the
      * other available languages.
      */
     private void selectLanguage() {
 	final String localLanguage = Locale.getDefault().getDisplayLanguage();
 	if (languages.containsKey(localLanguage)) {
 	    selectedLanguage = languages.get(localLanguage);
 	} else if (languages.containsKey(DEFAULT_LANGUAGE)) {
 	    selectedLanguage = languages.get(DEFAULT_LANGUAGE);
 	} else {
 	    selectLanguageRandomly();
 	}
 	System.out.println("Selected language: " + selectedLanguage.getName());
     }
 
     /**
      * Selects language randomly out of all available languages.
      */
     private void selectLanguageRandomly() {
 	final double anzLanguages = languages.keySet().size();
 	final int languageNumber = (int) (Math.random() * anzLanguages);
 	selectedLanguage = languages.get(languages.keySet().toArray()[languageNumber]);
     }
 
     private void writeFile(final String path, final StringBuilder content) {
 	try {
 	    final FileWriter fstream = new FileWriter(path);
 	    final BufferedWriter out = new BufferedWriter(fstream);
 	    out.write(content.toString());
 	    out.close();
 	} catch (final Exception e) {
 	    System.err.println("Error: " + e.getMessage());
 	}
     }
 
     private StringBuilder content;
 
     private File languageFileDir;
 
     private final HashMap<String, Language> languages;
 
     private Language selectedLanguage;
 }
