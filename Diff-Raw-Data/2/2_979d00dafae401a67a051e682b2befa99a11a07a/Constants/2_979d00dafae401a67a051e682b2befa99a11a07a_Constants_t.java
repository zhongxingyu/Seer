 package gov.nih.nci.evs.browser.common;
 
 /**
  * Application constants class
  * @author garciawa2
  */
 public class Constants {
 
     // Application version
     static public final int MAJOR_VER = 1;
     static public final int MINOR_VER = 0;
     static public final String CONFIG_FILE = "NCItBrowserProperties.xml";
     static public final String CODING_SCHEME_NAME = "NCI Thesaurus";
 
 	// Application constants
 	static public final String NA = "N/A";
 	static public final String TRUE = "true";
 	static public final String FALSE = "false";
 	static public final String EMPTY = "";
 
 	static public final String ALL = "ALL";
 
 	// Application error constants
 	static public final String INIT_PARAM_ERROR_PAGE = "errorPage";
 	static public final String ERROR_MESSAGE = "systemMessage";
 	static public final String ERROR_UNEXPECTED = "Warning: An unexpected processing error has occurred.";
 
     static public final int DEFAULT_PAGE_SIZE = 50;
 
     static public final String ERROR_NO_VOCABULARY_SELECTED = "Please select at least one vocabulary.";
     static public final String ERROR_NO_SEARCH_STRING_ENTERED = "Please enter a search string.";
     static public final String ERROR_NO_MATCH_FOUND = "No match found.";
    static public final String ERROR_NO_MATCH_FOUND_TRY_OTHER_ALGORITHMS = "No match found. Please try 'Begins With' or 'Contains' search instead.";
 
     static public final String ERROR_ENCOUNTERED_TRY_NARROW_QUERY = "Unable to perform search successfully. Please narrow your query.";
 
 
 
 
 	public static final String EXACT_SEARCH_ALGORITHM = "exactMatch";// "literalSubString";//"subString";
 	public static final String STARTWITH_SEARCH_ALGORITHM = "startsWith";// "literalSubString";//"subString";
 	public static final String CONTAIN_SEARCH_ALGORITHM = "nonLeadingWildcardLiteralSubString";// "literalSubString";//"subString";
 	public static final String LICENSE_STATEMENT = "license_statement";// "literalSubString";//"subString";
 
 	/**
 	 * Constructor
 	 */
 	private Constants() {
 		// Prevent class from being explicitly instantiated
 	}
 
 	public static String getCodingSchemeName() {
 		return CODING_SCHEME_NAME.replaceAll(" ", "%20");
 	}
 
 } // Class Constants
