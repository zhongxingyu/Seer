 package ch.supsi.dti.multilanguage;
 
 import org.eclipse.osgi.util.NLS;
 
 import ch.supsi.dti.preferences.PreferenceConstants;
 import ch.supsi.dti.preferences.SpeakingPreferences;
 
 import com.gtranslate.Translator;
 
 /**
  * Principal class for the multilanguage system. The static fields allow you to
  * refer to the "properties" files.
  * 
  * @author Claudio
  * 
  */
 public class Messages extends NLS {
 
 	/**
 	 * Path of the english language file
 	 */
 	public static final String BUNDLE_NAME_EN = "ch.supsi.dti.multilanguage.en";
 	/**
 	 * Path of the italian language file
 	 */
 	public static final String BUNDLE_NAME_IT = "ch.supsi.dti.multilanguage.it";
 
 	/**
 	 * Are opened, female gender
 	 */
 	public static String areOpenedF;
 	/**
 	 * Classes, (plural)
 	 */
 	public static String classes;
 	/**
 	 * Class not selected
 	 */
 	public static String classNotSelected;
 	/**
 	 * Closed, female gender
 	 */
 	public static String closedF;
 	/**
 	 * Closed, male gender
 	 */
 	public static String closedM;
 	/**
 	 * Collapsed, female gender
 	 */
 	public static String collapsedF;
 	/**
 	 * Collapsed, male gender
 	 */
 	public static String collapsedM;
 	/**
 	 * Expand the selection
 	 */
 	public static String expand;
 	/**
 	 * Constructor
 	 */
 	public static String constuctor;
 	/**
 	 * Cursor
 	 */
 	public static String cursor;
 	/**
 	 * The cursor is in
 	 */
 	public static String cursorIn;
 	/**
 	 * Done
 	 */
 	public static String done;
 	/**
 	 * Editor
 	 */
 	public static String editor;
 	/**
 	 * Error
 	 */
 	public static String error;
 	/**
 	 * Expanded, female gender
 	 */
 	public static String expandedF;
 	/**
 	 * Expanded, male gender
 	 */
 	public static String expandedM;
 	/**
 	 * Filed
 	 */
 	public static String field;
 	/**
 	 * Focused, female gender
 	 */
 	public static String focusedF;
 	/**
 	 * Focused, male gender
 	 */
 	public static String focusedM;
 	/**
 	 * Folder
 	 */
 	public static String folder;
 	/**
 	 * Info
 	 */
 	public static String info;
 	/**
 	 * It has
 	 */
 	public static String itHas;
 	/**
 	 * This is the main method
 	 */
 	public static String mainMethod;
 	/**
 	 * Method
 	 */
 	public static String method;
 	/**
 	 * Method not found in this class
 	 */
 	public static String methodNotFoundInClass;
 	/**
 	 * Methods (plural)
 	 */
 	public static String methods;
 	/**
 	 * Multiple class founded, reduce the selection
 	 */
 	public static String multipleClass;
 	/**
 	 * The nature project is
 	 */
 	public static String natureProject;
 	/**
 	 * No class with the name
 	 */
 	public static String noClass;
 	/**
 	 * No Java Element
 	 */
 	public static String noJavaElement;
 	/**
 	 * No opened editor
 	 */
 	public static String noOpenedEditor;
 	/**
 	 * Opened, female gender
 	 */
 	public static String openedF;
 	/**
 	 * Opened, male gender
 	 */
 	public static String openedM;
 	/**
 	 * Package Explorer
 	 */
 	public static String packageExplorer;
 	/**
 	 * Package not found
 	 */
 	public static String packageNotFound;
 	/**
 	 * Packages (plural)
 	 */
 	public static String packages;
 	/**
 	 * Project
 	 */
 	public static String project;
 	/**
 	 * Project doesn't exist
 	 */
 	public static String projectNotExist;
 	/**
 	 * Project not selected
 	 */
 	public static String projectNotSelected;
 	/**
 	 * Projects (plural)
 	 */
 	public static String projects;
 	/**
 	 * Read-only
 	 */
 	public static String readOnly;
 	/**
 	 * The return type is
 	 */
 	public static String returnType;
 	/**
 	 * Select
 	 */
 	public static String select;
 	/**
 	 * Selected, female gender
 	 */
 	public static String selectedF;
 	/**
 	 * Selected, male gender
 	 */
 	public static String selectedM;
 	/**
 	 * The signature is
 	 */
 	public static String signature;
 	/**
 	 * Speaking View
 	 */
 	public static String speakingView;
 	/**
 	 * Syntax Error
 	 */
 	public static String syntaxError;
 	/**
 	 * Class
 	 */
 	public static String theClass;
 	/**
 	 * Package
 	 */
 	public static String thePackage;
 	/**
 	 * There are
 	 */
 	public static String thereAre;
 	/**
 	 * There is
 	 */
 	public static String thereIs;
 	/**
 	 * Unknown java element
 	 */
 	public static String unknownJavaElement;
 	/**
 	 * Was added, female gender
 	 */
 	public static String wasAddedF;
 	/**
 	 * Was added, male gender
 	 */
 	public static String wasAddedM;
 	/**
 	 * Was changed, female gender
 	 */
 	public static String wasChangedF;
 	/**
 	 * Was changed, male gender
 	 */
 	public static String wasChangedM;
 	/**
 	 * Was removed, female gender
 	 */
 	public static String wasRemovedF;
 	/**
 	 * Was removed, male gender
 	 */
 	public static String wasRemovedM;
 
 	static {
 		// initialize resource bundle
 		switch (new SpeakingPreferences().getPreferenceStore().getString(
 				PreferenceConstants.MULTILANGUAGE)) {
 		case "en":
 			NLS.initializeMessages(BUNDLE_NAME_EN, Messages.class);
 			break;
 		case "it":
 			NLS.initializeMessages(BUNDLE_NAME_IT, Messages.class);
 			break;
 		}
 
 	}
 
 	/**
 	 * The constructor 
 	 */
 	private Messages() {
 	}
 	
 	/**
 	 * Reset the class with the language passed
 	 * 
 	 * @param language
 	 *            the language that reset for
 	 */
 	public static void reinitializeMessages(String language) {
 		NLS.initializeMessages(language, Messages.class);
 	}
 
 	/**
 	 * Translates the string passed, it need for the command line.
 	 * @param commandLineText the string to be translated 
 	 * @return the string translated
 	 */
 	public static String traduceText(String commandLineText) {
 
 		Translator translate = Translator.getInstance();
 		StringBuilder sb = new StringBuilder();
 		String[] arrCommands = commandLineText.split(" ");
 
 		for (int i = 0; i < arrCommands.length; i++) {
 			switch (arrCommands[i]) {
 			case "select":
 				sb.append(Messages.select);
 				sb.append(" ");
 				break;
 			case "info":
 				sb.append(Messages.info);
 				sb.append(" ");
 				break;
 			case "project":
 				sb.append(Messages.project);
 				sb.append(" ");
 				break;
 			case "projects":
 				sb.append(Messages.projects);
 				sb.append(" ");
 				break;
 			case "folder":
 				sb.append(Messages.folder);
 				sb.append(" ");
 				break;
 			case "package":
 				sb.append(Messages.thePackage);
 				sb.append(" ");
 				break;
 			case "packages":
 				sb.append(Messages.packages);
 				sb.append(" ");
 				break;
 			case "class":
 				sb.append(Messages.theClass);
 				sb.append(" ");
 				break;
 			case "classes":
 				sb.append(Messages.classes);
 				sb.append(" ");
 				break;
 			case "method":
 				sb.append(Messages.method);
 				sb.append(" ");
 				break;
 			case "methods":
 				sb.append(Messages.methods);
 				sb.append(" ");
 				break;
 			case "cursor":
 				sb.append(Messages.cursor);
 				sb.append(" ");
 				break;
 			default:
 				sb.append(arrCommands[i]);
 				sb.append(" ");
 				break;
 			}
 		}
 		return sb.toString();
 
 	}
 
 }
