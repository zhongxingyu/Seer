 package com.alexrnl.commons.translation;
 
 import java.nio.file.Path;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.Configuration;
 import com.alexrnl.commons.utils.StringUtils;
 
 /**
  * Implementation of the translator.<br />
  * Class which provide utilities method to translate key to the locale set in the configuration
  * file.
  * @author Alex
  */
 public class Translator extends Configuration {
 	/** Logger */
 	private static Logger			lg					= Logger.getLogger(Translator.class.getName());
 	
 	/** Character used for mnemonics in menu bar */
 	public static final Character	MNEMONIC_MARK		= '#';
 	/** Character used for parameters in translations */
 	public static final Character	PARAMETER_PREFIX	= '%';
 	/** Character used to include a translation into an other one */
 	public static final Character	INCLUDE_PREFIX		= '§';
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param translationFile
 	 *        the translation file to load.
 	 */
 	public Translator (final Path translationFile) {
 		super(translationFile, true);
 		
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("Language locale file " + translationFile + " successfully loaded (" + size() + " keys loaded)");
 		}
 	}
 	
 	/**
 	 * Get the translation of a specific key.
 	 * @param key
 	 *        the key to translate.
 	 * @return the translation of the key from the specified locale file.
 	 */
 	@Override
 	public String get (final String key) {
 		if (!has(key)) {
 			if (lg.isLoggable(Level.INFO)) {
 				lg.info("Cannot find translation for key " + key);
 			}
 			return key;
 		}
 		
 		String translation = super.get(key);
 		
 		// Replace other translations included in the current one
 		while (translation.contains(INCLUDE_PREFIX.toString())) {
 			// Isolate the string to replace, from the Constants.INCLUDE_PREFIX to the next space
			final int location = translation.indexOf(INCLUDE_PREFIX);
 			final int endLocation = translation.substring(location).indexOf(StringUtils.SPACE) + location;
			final String strToReplace;
			if (endLocation == -1) {
 				// End of string case
 				strToReplace = translation.substring(location);
 			} else {
 				strToReplace = translation.substring(location, endLocation);
 			}
 			// Replace the prefix + key with the translation of the key (hence the substring)
			translation = translation.replace(strToReplace, get(strToReplace.substring(1)));
 		}
 		
 		return translation;
 	}
 	
 	/**
 	 * Get the translation of a specific key.
 	 * @param key
 	 *        the key to translate.
 	 * @param parameters
 	 *        the parameters to use to build the translation.
 	 * @return the translation of the key from the specified locale file.
 	 */
 	public String get (final String key, final Object... parameters) {
 		String translation = get(key);
 		// Case with no parameters or no translation found
 		if (key.equals(translation) || parameters == null || parameters.length == 0) {
 			return translation;
 		}
 		
 		// Replace parameters
 		for (int indexParameter = 0; indexParameter < parameters.length; ++indexParameter) {
 			final Object parameter = parameters[indexParameter];
 			final String strToReplace = PARAMETER_PREFIX.toString() + indexParameter;
 			if (translation.indexOf(strToReplace) == -1) {
 				lg.warning("Parameter '" + parameter + "' cannot be put into the translation, '" +
 						strToReplace + "' was not found.");
 				continue;
 			}
 			translation = translation.replace(strToReplace, parameter.toString());
 		}
 		
 		return translation;
 	}
 	
 	/**
 	 * Get the translation of a specific key and append the field value separator.
 	 * @param key
 	 *        the key to translate.
 	 * @return the translation of the key from the specified locale file.
 	 */
 	public String getField (final String key) {
 		return get(key);
 		// TODO put that in commons? '+ get(TranslationKeys.MISC.fieldValueSeparator());'
 	}
 	
 }
