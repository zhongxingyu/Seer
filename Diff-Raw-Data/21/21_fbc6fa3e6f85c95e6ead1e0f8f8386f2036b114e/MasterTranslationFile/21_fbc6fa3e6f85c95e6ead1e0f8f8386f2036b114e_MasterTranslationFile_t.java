 /**
  * 
  */
 package net.frontlinesms.plugins.translation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.plugins.PluginController;
 import net.frontlinesms.plugins.PluginProperties;
 import net.frontlinesms.plugins.translation.KeyNotFoundException;
 import net.frontlinesms.plugins.translation.MasterTranslationFile;
 import net.frontlinesms.plugins.translation.TextFileContent;
 import net.frontlinesms.ui.i18n.*;
 
 /**
  * This creates a master translation file, containing translation for FrontlineSMS core and all available plugins.
  * @author Alex <alex@frontlinesms.com>
  * @author Morgan <morgan@frontlinesms.com>
  */
 public class MasterTranslationFile extends LanguageBundle {
 //> STATIC CONSTANTS
 	/** prefix applied in {@link #getIdentifier()} */
 	private static final String IDENTIFIER_PREFIX = "master:";
 
 	/** Logging object */
 	private static final Logger LOG = FrontlineUtils.getLogger(MasterTranslationFile.class);
 	
 //> INSTANCE VARIABLES
 	private String filename;
 	
 	private final List<TextFileContent> translationFiles;
 
 	private ArrayList<String> changes;
 	
 
 	//> CONSTRUCTORS
 	public MasterTranslationFile(String filename, List<TextFileContent> translationFiles) {
 		super(getTranslationMap(translationFiles));
 		this.filename = filename;
 		this.translationFiles = translationFiles;
 		this.changes = new ArrayList<String>();
 	}
 	
 //> ACCESSORS
 	@Override
 	public String getIdentifier() {
 		return IDENTIFIER_PREFIX + this.filename;
 	}
 	
 	/**
 	 * Save the MTF to a file 
 	 * @throws IOException
 	 */
 	void saveToDisk(File targetDirectory) throws IOException {
 		FileOutputStream fos = null;
 		OutputStreamWriter osw = null;
 		PrintWriter out = null;
 		try {
 			File file = new File(targetDirectory, this.filename);
 			fos = new FileOutputStream(file);
 			osw = new OutputStreamWriter(fos, InternationalisationUtils.CHARSET_UTF8);
 			out = new PrintWriter(osw);
 			for(TextFileContent translationFile : this.translationFiles) {
 				for(String line : translationFile.getLines()) {
 					out.write(line + "\n");
 				}
 				out.write("\n");
 			}
 		} finally {
 			if(out != null) out.close();
 			if(osw != null) try { osw.close(); } catch(IOException ex) {}
 			if(fos != null) try { fos.close(); } catch(IOException ex) {}
 		}
 	}
 
 //> STATIC HELPERS
 	/** @return map of key-value pairs of translations found in the MTF */
 	private static Map<String, String> getTranslationMap(List<TextFileContent> translationFiles) {
 		HashMap<String, String> translations = new HashMap<String, String>();
 		for(TextFileContent file : translationFiles) {
 			for(String line : file.getLines()) {
 				line = line.trim();
 				if(line.length() > 0 && line.charAt(0)!='#') {
 					int eqIndex = line.indexOf('=');
 
 					// Do not overwrite entries from previous file contents
 					String key = line.substring(0, eqIndex);
 					String value = line.substring(eqIndex+1);
 					if(!translations.containsKey(key)) {
 						translations.put(key, value);
 					} else {
 						LOG.trace("Omitting overridden translation: " + key + "=" + value);
 					}
 				}
 			}
 		}
 		return translations;
 	}
 	
 //> STATIC FACTORIES
 	/**
 	 * <p>Creates a {@link MasterTranslationFile} for each {@link FileLanguageBundle} found in the languages directory.</p>
 	 * <p>N.B. This does not create a {@link MasterTranslationFile} for the default language bundle.  This can be fetched
 	 * by calling the {@link #getDefault()} method.</p>
 	 * @return a {@link MasterTranslationFile} for each {@link FileLanguageBundle} found in the languages directory.
 	 */
 	public static Collection<MasterTranslationFile> getAll() {
 		ArrayList<MasterTranslationFile> all = new ArrayList<MasterTranslationFile>();
 		for(FileLanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
 			all.add(get(languageBundle));
 		}
 		return all;
 	}
 	
 	/**
 	 * Gets the language bundle corresponding to the language code
 	 * @return a {@link MasterTranslationFile} for each {@link FileLanguageBundle} found in the languages directory.
 	 */
 	public static MasterTranslationFile getFromLanguageCode(String languageCode) {
 		for(FileLanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
 			if (languageBundle.getLanguageCode().equals(languageCode)) {
 				return get(languageBundle);
 			}
 		}
 		return null;
 	}
 	
 	public static MasterTranslationFile getFromIdentifier(String identifier) {
 		String filename = identifier.substring(IDENTIFIER_PREFIX.length());
 		String localeBits = filename.substring("frontlineSMS".length(), filename.length() - ".properties".length());
 		String[] bits = localeBits.split("_");
 		
		File file = new File(InternationalisationUtils.getLanguageDirectory() + File.separator, filename);
 		Locale locale;
 
 		if(bits.length <= 1) {
 			locale = new Locale("");
 		} else if(bits.length == 2) {
 			locale = new Locale(bits[1]);
 		} else if(bits.length == 3) {
 			locale = new Locale(bits[1], bits[2]);
 		} else if(bits.length == 4) {
 			locale = new Locale(bits[1], bits[2], bits[3]);
 		} else throw new RuntimeException("Too many bits in " + filename);
 		return MasterTranslationFile.get(file, locale);
 	}
 	
 
 	public static String getIdentifier(String filename) {
 		return IDENTIFIER_PREFIX + filename;
 	}
 
 	/** @return {@link MasterTranslationFile} for the supplied file */
 	static MasterTranslationFile get(File file, Locale locale) {
 		List<TextFileContent> content = new ArrayList<TextFileContent>();
 		
 		// add core content
 		FileInputStream fis;
 		try {
 			fis = new FileInputStream(file);
 		} catch (FileNotFoundException ex) {
 			throw new RuntimeException(ex);
 		}
 		content.add(TextFileContent.getFromStream("FrontlineSMS Core", fis));
 		
 		// load plugin bundles for this language
 		Collection<Class<PluginController>> pluginClasses = PluginProperties.getInstance().getPluginClasses();
 		for(Class<PluginController> pluginClass : pluginClasses) {
 			try {
 				PluginController controller = pluginClass.newInstance();
 				Map<String, String> textResource;
 				if(isDefault(locale)) {
 					textResource = controller.getDefaultTextResource();
 				} else {
 					textResource = controller.getTextResource(locale);
 				}
 				String tfcDescription = "Plugin: " + controller.getName();
 				content.add(TextFileContent.getFromMap(tfcDescription, textResource));
 			} catch (Exception ex) {
 				throw new RuntimeException("Unable to instantiate plugin: " + pluginClass.getName(), ex);
 			}
 		}
 		
 		return new MasterTranslationFile(file.getName(), content);
 	}
 	
 	/** @return true if the supplied locale is for an unspecified language, country and variant; <code>false</code> otherwise */
 	private static boolean isDefault(Locale locale) {
 		String lang = locale.getLanguage();
 		String var = locale.getVariant();
 		String country = locale.getCountry();
 		return (lang==null || lang.length()==0)
 				&& (var==null || var.length()==0)
 				&& (country==null || country.length()==0);
 	}
 
 	/** @return {@link MasterTranslationFile} for the supplied {@link LanguageBundle} */
 	static MasterTranslationFile get(FileLanguageBundle languageBundle) {
 		return get(languageBundle.getFile(), languageBundle.getLocale());
 	}
 	
 	/** @return the {@link MasterTranslationFile} for the default translation */
 	static MasterTranslationFile getDefault() {
 		List<TextFileContent> content = new ArrayList<TextFileContent>();
 		
 		// add core content
 		content.add(TextFileContent.getFromStream("FrontlineSMS Core", InternationalisationUtils.getDefaultLanguageBundleInputStream()));
 		
 		// load default bundles for all plugins
 		Collection<Class<PluginController>> pluginClasses = PluginProperties.getInstance().getPluginClasses();
 		for(Class<PluginController> pluginClass : pluginClasses) {
 			try {
 				PluginController controller = pluginClass.newInstance();
 				content.add(TextFileContent.getFromMap(
 						"Plugin: " + controller.getName(),
 						controller.getDefaultTextResource()));
 			} catch (Exception ex) {
 				throw new RuntimeException("Unable to instantiate plugin: " + pluginClass.getName(), ex);
 			}
 		}
 		
 		return new MasterTranslationFile("frontlineSMS.properties", content);
 	}
 	
 	/**
 	 * Create a {@link MasterTranslationFile} and save it locally.
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		System.out.println("Starting Master Language File generation...");
 		String targetDirPath = args[0];
 		File targetDir = new File(targetDirPath);
 		
 		processDefaultMtf(targetDir);
 		
 		for(FileLanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
 			MasterTranslationFile mtf = get(languageBundle);
 			mtf.saveToDisk(new File(targetDir, languageBundle.getFile().getName()));	
 		}
 		
 		System.out.println("Master language files generated at: " + targetDirPath);
 	}
 
 	/** Create the {@link MasterTranslationFile} for the default translation. */
 	private static void processDefaultMtf(File targetDir) throws IOException {
 		MasterTranslationFile mtf = getDefault();
 		mtf.saveToDisk(new File(targetDir, "frontlineSMS.properties"));
 	}
 
 	public void add(String textKey, String textValue) {
 		if (textValue.equals("")) {
 			try {
 				this.delete(textKey);
 				this.valueChanged(textKey);
 			} catch (KeyNotFoundException e) {
 				// Key didn't exist
 			}
 		} else {
 			String oldValue = super.getProperties().get(textKey);
 			if (super.getProperties().put(textKey, textValue) == null || !textValue.equals(oldValue)) {
 				this.valueChanged(textKey);
 			}
 			try {
 				// Attempt to update the translation in the current files
 				updateTranslation(textKey, textValue);
 			} catch(KeyNotFoundException ex) {
 			}
 		}
 		
 	}
 
 	private void valueChanged(String textKey) {
 		this.changes.add(textKey);
 	}
 
 	/**
 	 * Updates the value of a translation in the attached files.
 	 * @param textKey
 	 * @param textValue
 	 * @throws KeyNotFoundException 
 	 */
 	private void updateTranslation(String textKey, String textValue) throws KeyNotFoundException {
 		TextFileContent tfc = getTextFileContent(textKey);
 		tfc.updateValue(textKey, textValue);
 	}
 
 	public void delete(String textKey) throws KeyNotFoundException {
 		TextFileContent tfc = getTextFileContent(textKey);
 		String line = tfc.getLine(textKey);
 		tfc.removeLine(line);
 		this.valueChanged(textKey);
 		super.getProperties().remove(textKey);
 	}
 	
 	/**
 	 * Gets the {@link TextFileContent} containing the supplied text key.
 	 * @param textKey
 	 * @return
 	 * @throws KeyNotFoundException 
 	 */
 	private TextFileContent getTextFileContent(String textKey) throws KeyNotFoundException {
 		for(TextFileContent tf : this.translationFiles) {
 			if(tf.containsKey(textKey)) {
 				return tf;
 			}
 		}
 		throw new KeyNotFoundException("The text key could not be found in any of the attached content: " + textKey);
 	}
 	
 	/**
 	 * Gets the translation files for this language bundle
 	 * @return
 	 */
 	public List<TextFileContent> getTranslationFiles() {
 		return translationFiles;
 	}
 
 	/**
 	 * Gets the filename for this language bundle
 	 * @return The filename
 	 */
 	public String getFilename() {
 		return filename;
 	}
 
 	
 	/**
 	 * Sets the filename for this language bundle
 	 * @param fileName
 	 */
 	public void setFilename (String fileName) {
 		this.filename = fileName;
 	}
 
 	/** Sets the ISO-???? country code relating to the language in this bundle 
 	 * @throws KeyNotFoundException */
 	public void setCountry(String country) {
 		super.setCountry(country);
 		try {
 			this.translationFiles.get(0).updateValue(KEY_LANGUAGE_COUNTRY, country);
 		} catch (KeyNotFoundException ex) { }
 	}
 	
 	/** Sets the name of this language bundle */
 	/** @throws KeyNotFoundException */
 	public void setLanguageName(String languageName) {
 		super.setLanguageName(languageName);
 		try {
 			this.translationFiles.get(0).updateValue(KEY_LANGUAGE_NAME, languageName);
 		} catch (KeyNotFoundException ex) { }
 	}
 
 	/** Sets the ISO-???? code relating to this language */
 	/** @throws KeyNotFoundException */
 	public void setLanguageCode(String languageCode) {
 		super.setLanguageCode(languageCode);
 		try {
 			this.translationFiles.get(0).updateValue(KEY_LANGUAGE_CODE, languageCode);
 		} catch (KeyNotFoundException ex) { }
 	}
 	
 	/** Sets the font for this language bundle */
 	/** @throws KeyNotFoundException */
 	public void setLanguageFont(String fontNames) {
 		super.setLanguageFont(fontNames);
 		TextFileContent tfContent = this.translationFiles.get(0); 
 		if (fontNames == null || fontNames.length() == 0) {
 			try {
 				tfContent.removeLine(tfContent.getLine(KEY_LANGUAGE_FONT));
 			} catch (KeyNotFoundException e) {
 				// Font wasn't previously set. No problem.
 			}
 		} else {
 			try {
 				tfContent.updateValue(KEY_LANGUAGE_FONT, fontNames);
 			} catch (KeyNotFoundException ex) { 
 				tfContent.addLine(KEY_LANGUAGE_FONT + "=" + fontNames);
 			}
 		}
 	}
 	
 	public ArrayList<String> getChanges() {
 		return changes;
 	}
 
 	public void setChanges(ArrayList<String> changes) {
 		this.changes = changes;
 	}
 
 	public boolean hasBeenEdited(String textKey) {
 		return this.changes.contains(textKey);
 	}
 }
 
 class TextFileContent {
 	/** Prefix for plugins properties */
 	private static final String PLUGINS_PREFIX = "plugins.";
 	/** Description of this file */
 	private String description;
 	/** Lines in the file */
 	private final LinkedList<String> lines = new LinkedList<String>();
 	
 	private TextFileContent(String description) {
 		this.description = description;
 	}
 	
 	public void removeLine(String line) {
 		this.lines.remove(line);
 	}
 
 	public static TextFileContent createEmpty() {
 		return new TextFileContent(null);
 	}
 
 	public String getDescription() {
 		return description;
 	}
 	
 	public LinkedList<String> getLines() {
 		return lines;
 	}
 	
 	void addLine(String line) {
 		this.lines.add(line);
 	}
 	
 	/** @return <code>true</code> if this contains the supplied key; <code>false</code> otherwise */
 	boolean containsKey(String textKey) {
 		for(String line : getLines()) {
 			if(line.trim().startsWith(textKey + "=")) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/** 
 	 * @return the line containing the supplied key 
 	 * @throws KeyNotFoundException
 	 */
 	String getLine(String textKey) throws KeyNotFoundException {
 		for(String line : getLines()) {
 			if(line.trim().startsWith(textKey + "=")) {
 				return line;
 			}
 		}
 		throw new KeyNotFoundException(textKey);
 	}
 	
 	/**
 	 * Changes the value for a text key in this file.
 	 * @param textKey
 	 * @param newValue
 	 * @throws KeyNotFoundException 
 	 */
 	void updateValue(String textKey, String newValue) throws KeyNotFoundException {
 		String oldLine = getLine(textKey);
 		String newLine = textKey + "=" + newValue;
 		this.lines.set(lines.indexOf(oldLine), newLine);
 	}
 	
 	static TextFileContent getFromMap(String description, Map<String, String> map) {
 		TextFileContent content = new TextFileContent(description);
 		for(Entry<String, String> entry : map.entrySet()) {
 			content.addLine(entry.getKey() + "=" + entry.getValue());
 		}
 		return content;
 	}
 	
 	static TextFileContent getFromStream(String description, InputStream is) {
 		BufferedReader in = null;
 		TextFileContent content = new TextFileContent(description);
 		try {
 			in = new BufferedReader(new InputStreamReader(is, InternationalisationUtils.CHARSET_UTF8));
 			String line;
 			while((line = in.readLine()) != null) {
 				if (!line.startsWith(PLUGINS_PREFIX)) {
 					content.addLine(line);
 				}
 			}
 			return content;
 		} catch (IOException ex) {
 			throw new IllegalStateException("Unhandled problem reading stream: '" + description + "'", ex);
 		} finally {
 			if(in != null) try { in.close(); } catch(IOException ex) {}
 		}
 	}
 }
 
 class KeyNotFoundException extends Exception {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4988194381702067732L;
 
 	public KeyNotFoundException(String key) {
 		super("Key not found: " + key);
 	}
 }
