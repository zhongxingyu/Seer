 package edu.rutgers.MOST.presentation;
 
 import java.awt.Color;
 import java.util.Arrays;
 
 public class GraphicalInterfaceConstants {
 	
 	public static final String TITLE = "MOST";
 	
 	public static final String DEFAULT_MODEL_NAME = "untitled";
 	
 	public static final int BLANK_METABOLITE_ROW_COUNT = 2000;
 	public static final int BLANK_REACTION_ROW_COUNT = 2000;
 	
 	public static final String[] REACTIONS_COLUMN_NAMES = 
 	{ 
 		"ID",
 		"KO",
 		"Flux Value",		
 		"Reaction Abbreviation",
 		"Reaction Name",
 		"Reaction Equation (Metabolite Abbreviation)",
 		"Reaction Equation (Metabolite Name)",
 		"Reversible",		
 		"Lower Bound",
 		"Upper Bound",	
 		"Biological Objective",
 		"Synthetic Objective",
 		"Gene Association", 
 		"Protein Association",
 		"Subsystem", 
 		"Protein Class"
 	};
 	
 	private static java.util.List<String> reactionsList = Arrays.asList(REACTIONS_COLUMN_NAMES);
 	
 	public static final String KNOCKOUT_TOOLTIP = "Knockout";	
 	
 	public static final String[] METABOLITES_COLUMN_NAMES = 
 	{ 
 		"ID",
 		"Metabolite Abbreviation",
 		"Metabolite Name",		
 		"Charge",
 		"Compartment",
 		"Boundary Condition"
 	};
 	
 	private static java.util.List<String> metabolitesList = Arrays.asList(METABOLITES_COLUMN_NAMES);
 	
 	//reactions table column numbers and associated widths
 	public static final int DEFAULT_WIDTH = 90; //columns with no assigned width use default	
 	public static final int REACTIONS_ID_COLUMN = reactionsList.indexOf("ID"); 
 	public static final int REACTIONS_ID_WIDTH = 50;    
     public static final int KO_COLUMN = reactionsList.indexOf("KO");
     public static final int KO_WIDTH = 60;    
     public static final int FLUX_VALUE_COLUMN = reactionsList.indexOf("Flux Value");
     public static final int REACTION_ABBREVIATION_WIDTH = 150;
     public static final int REACTION_ABBREVIATION_COLUMN = reactionsList.indexOf("Reaction Abbreviation");
     public static final int REACTION_NAME_WIDTH = 200; 
     public static final int REACTION_NAME_COLUMN = reactionsList.indexOf("Reaction Name");
     public static final int REACTION_EQUN_ABBR_WIDTH = 300;
     public static final int REACTION_EQUN_ABBR_COLUMN = reactionsList.indexOf("Reaction Equation (Metabolite Abbreviation)");
     public static final int REACTION_EQUN_NAMES_WIDTH = 300;
     public static final int REACTION_EQUN_NAMES_COLUMN = reactionsList.indexOf("Reaction Equation (Metabolite Name)");
     public static final int REVERSIBLE_WIDTH = 60;
     public static final int REVERSIBLE_COLUMN = reactionsList.indexOf("Reversible");         
     public static final int LOWER_BOUND_COLUMN = reactionsList.indexOf("Lower Bound");
     public static final int UPPER_BOUND_COLUMN = reactionsList.indexOf("Upper Bound");
     public static final int BIOLOGICAL_OBJECTIVE_COLUMN = reactionsList.indexOf("Biological Objective"); 
     public static final int SYNTHETIC_OBJECTIVE_COLUMN = reactionsList.indexOf("Synthetic Objective");
     public static final int GENE_ASSOCIATION_COLUMN = reactionsList.indexOf("Gene Association");
     public static final int PROTEIN_ASSOCIATION_COLUMN = reactionsList.indexOf("Protein Association");
     public static final int SUBSYSTEM_COLUMN = reactionsList.indexOf("Subsystem");
     public static final int PROTEIN_CLASS_COLUMN = reactionsList.indexOf("Protein Class");
     public static final int REACTION_META_DEFAULT_WIDTH = 150;
     
     //metabolites table column numbers and associated widths
     public static final int METABOLITE_ID_WIDTH = 50;  
     //"id_m" to distinguish between numerical id in reactions table and metabolites table
     public static final int METABOLITE_ID_COLUMN = metabolitesList.indexOf("ID");
     public static final int METABOLITE_ABBREVIATION_WIDTH = 200;
     public static final int METABOLITE_ABBREVIATION_COLUMN = metabolitesList.indexOf("Metabolite Abbreviation");
     public static final int METABOLITE_NAME_WIDTH = 300;
     public static final int METABOLITE_NAME_COLUMN = metabolitesList.indexOf("Metabolite Name");
     public static final int CHARGE_WIDTH = 80;
     public static final int CHARGE_COLUMN = metabolitesList.indexOf("Charge");
     public static final int COMPARTMENT_WIDTH = 150;
     public static final int COMPARTMENT_COLUMN = metabolitesList.indexOf("Compartment");
     public static final int BOUNDARY_WIDTH = 60;
     public static final int BOUNDARY_COLUMN = metabolitesList.indexOf("Boundary Condition");
     public static final int METABOLITE_META_DEFAULT_WIDTH = 150;
     
     public static final double FLUX_VALUE_DEFAULT = 0.0;
     public static final String FLUX_VALUE_DEFAULT_STRING = "0.0";
     public static final double LOWER_BOUND_DEFAULT = 0.0;
     public static final double LOWER_BOUND_REVERSIBLE_DEFAULT = -999999.0;
     public static final String LOWER_BOUND_DEFAULT_IRREVERBIBLE_STRING = "0.0";
     public static final String LOWER_BOUND_DEFAULT_REVERSIBLE_STRING = "-999999.0";
     public static final double UPPER_BOUND_DEFAULT = 999999.0;
     public static final String UPPER_BOUND_DEFAULT_STRING = "999999.0";
     public static final double BIOLOGICAL_OBJECTIVE_DEFAULT = 0.0;
     public static final String BIOLOGICAL_OBJECTIVE_DEFAULT_STRING = "0.0";
     public static final double SYNTHETIC_OBJECTIVE_DEFAULT = 0.0;
     public static final String SYNTHETIC_OBJECTIVE_DEFAULT_STRING = "0.0";
     public static final String KO_DEFAULT = "false";
     // if lower bound default < 0 then reversible must be true
     public static final String REVERSIBLE_DEFAULT = "true";
     public static final String BOUNDARY_DEFAULT = "false";
         
     // prefixes and suffixes
     public static final String DB_COPIER_SUFFIX = "_orig";
     
     public static final String OPTIMIZATION_PREFIX = "OPT_";
     
     public static final String MIP_SUFFIX = "_MIP"; 
     
     /*****************************************************************************/
     //metabolite column filtering names
     /*****************************************************************************/
     public static final String[] METAB_ABBREVIATION_FILTER =
     {"abbreviation", "id"
     };
     
     public static final String[] METAB_ABBREVIATION_NOT_FILTER =
     {"required", "recommended", "optional"
     };
     
     public static final String[] METAB_NAME_FILTER =
     {"name"
     };
     
     public static final String[] COMPARTMENT_FILTER =
     {"compartment"
     }; 
     
     public static final String[] CHARGE_FILTER =
     {"charge"
     };
     
     public static final String[] CHARGE_NOT_FILTER =
     {"charged"
     };
     
     public static final String[] BOUNDARY_FILTER =
     {"boundary"
     };
       
     /*****************************************************************************/
     //reaction column filtering names  
     /*****************************************************************************/
     public static final String[] ABBREVIATION_COLUMN_FILTER =
     {"abbreviation", "reaction id"
     };
     
     public static final String[] ABBREVIATION_COLUMN_NOT_FILTER =
     {"metabolite"
     };
     
     public static final String[] NAME_COLUMN_FILTER =
     {"name"
     };
     
     public static final String[] NAME_COLUMN_NOT_FILTER =
     {"metabolite"
     };
     
     public static final String[] EQUATION_COLUMN_FILTER =
     {"equation", "reaction"
     };
     
     public static final String[] EQUATION_COLUMN_NOT_FILTER =
     {"metabolite name"
     };
     
     public static final String[] REVERSIBLE_COLUMN_FILTER =
     {"reversible"
     };
     
     public static final String[] LOWER_BOUND_FILTER =
     {"lb", "lower"
     };
     
     public static final String[] UPPER_BOUND_FILTER =
     {"ub", "upper"
     };
     
     public static final String[] BIOLOGICAL_OBJECTIVE_FILTER =
     {"obj"
     };
     
     public static final String[] BIOLOGICAL_OBJECTIVE_NOT_FILTER =
     {"syn"
     };
     
     public static final String[] SYNTHETIC_OBJECTIVE_FILTER =
     {"obj"
     };
     
     public static final String[] SYNTHETIC_OBJECTIVE_NOT_FILTER =
     {"bio"
     };
     
     public static final String[] KNOCKOUT_COLUMN_FILTER =
     {"knockout", "ko"
     };
     
     public static final String[] FLUX_VALUE_COLUMN_FILTER =
     {"flux"
     };
         
     public static final String[] GENE_ASSOCIATION_COLUMN_FILTER =
     {"gene", "assoc"
     };
     
     public static final String[] PROTEIN_ASSOCIATION_COLUMN_FILTER =
     {"protein", "assoc"
     };
     
     public static final String[] SUBSYSTEM_COLUMN_FILTER =
     {"subsystem"
     };
     
     public static final String[] PROTEIN_CLASS_COLUMN_FILTER =
     {"protein", "class"
     };
     
     public static final String[] REACTIONS_COLUMN_IGNORE_LIST = 
     {"Reaction Equation (Metabolite Name)"
     };
     
     // messages box titles and messages
     // general 
     public static final String NUMERIC_VALUE_ERROR_TITLE = "Invalid numeric entry.";
     public static final String NUMERIC_VALUE_ERROR_MESSAGE = "Number Format Error";
     
     public static final String[] BOOLEAN_VALUES = {"false", "true"};
     public static final String[] VALID_FALSE_VALUES = {"f"};
     public static final String[] VALID_TRUE_VALUES = {"t"};
     public static final String BOOLEAN_VALUE_ERROR_TITLE = "Boolean Value Error";
     public static final String BOOLEAN_VALUE_ERROR_MESSAGE = "Invalid Boolean Value";
     //public static final String BOOLEAN_VALUE_ERROR_MESSAGE = "Invalid entry. Enter \"t\" for \"true\", \"f\" for \"false\"";
 	
     // lower/upper bound/reversible
     public static final String LOWER_BOUND_ERROR_TITLE = "Lower Bound Error.";
     public static final String LOWER_BOUND_ERROR_MESSAGE = "Reaction is irreversible and lower bound is negative. Do you wish to set it to 0?";
     public static final String LOWER_BOUND_ERROR_MESSAGE2 = "Lower bound must be less than or equal to upper bound. Do you wish to set it to 0?";
     public static final String UPPER_BOUND_ERROR_TITLE = "Upper Bound Error.";
     public static final String UPPER_BOUND_ERROR_MESSAGE = "Upper bound must be greater than or equal to lower bound. Do you wish to set it to default value?";
     
     public static final String IRREVERSIBLE_REACTION_ERROR_TITLE = "Irreversible Reaction Error";
     public static final String IRREVERSIBLE_REACTION_ERROR_MESSAGE = "Irreversible reaction requires lower bound to be >= 0";
        
     public static final String REVERSIBLE_ERROR_TITLE = "Non-Editable Column";
     public static final String REVERSIBLE_ERROR_MESSAGE = "Reversible Column cannot only edited or changed by editing reaction equation.";
     
     // participating
     public static final String PARTICIPATING_METAB_ERROR_TITLE = "Participating Metabolite Deletion Error";
     public static final String PARTICIPATING_METAB_ERROR_MESSAGE = "One or more selected metabolites participate in reactions and cannot be deleted.";
      
     public static final String PARTICIPATING_METAB_PASTE_ERROR_TITLE = "Participating Metabolite Paste Error";
     public static final String PARTICIPATING_METAB_PASTE_ERROR_MESSAGE = "Invalid Paste. One or more selected metabolites participate in reactions.";
     
     public static final String PARTICIPATING_METAB_RENAME_TITLE = "Rename Participating Metabolite?";
     public static final String PARTICIPATING_METAB_RENAME_MESSAGE_PREFIX = "Renaming ";
     public static final String PARTICIPATING_METAB_RENAME_MESSAGE_SUFFIX = " will result in changing one or more reactions. Are you sure you want to do this?";
         
     public static final String METABOLITE_RENAME_ERROR_TITLE = "Rename Error";
     public static final String METABOLITE_RENAME_ERROR_MESSAGE = "Metabolite Abbreviation or Name Cannot Be Blank";
     
     // duplicate metabolite
     public static final String DUPLICATE_METABOLITE_TITLE = "Duplicate Metabolite";
     public static final String DUPLICATE_METABOLITE_MESSAGE = "Duplicate Metabolite. Rename As "; 
     public static final String DUPLICATE_METABOLITE_PASTE_MESSAGE = "Duplicate Metabolites. Names Will Be Appended With '[1]', '[2]', etc.";
     
     public static final String DUPLICATE_SUFFIX = "[1]";
     
     // invalid reactions
     public static final String INVALID_REACTIONS_ERROR_TITLE = "Invalid Reactions Warning";
     public static final String INVALID_REACTIONS_ERROR_MESSAGE = "Model contains invalid reactions. Are you sure you wish to save?";    
     public static final String INVALID_REACTIONS_ENTRY_ERROR_TITLE = "Invalid Reaction";
     public static final String INVALID_REACTIONS_ENTRY_ERROR_MESSAGE = "Invalid Reaction Format";    
     
     // replace errors
     public static final String REPLACE_ALL_ERROR_TITLE = "Replace All Error";
     public static final String REPLACE_ALL_BOOLEAN_VALUE_ERROR = "Replace value is not a valid boolean value";
     public static final String REPLACE_ALL_PARTICIPATING_ERROR_MESSAGE = "Invalid Replace. One or more selected metabolites participate in reactions and cannot be changed.";
     public static final String LOWER_BOUND_REPLACE_ALL_ERROR = "Lower bound must be less than or equal to upper bound.";
     public static final String UPPER_BOUND_REPLACE_ALL_ERROR = "Upper bound must be greater than or equal to lower bound.";
     public static final String INVALID_REPLACE_ALL_BOOLEAN_VALUE = "Boolean Value Error. Must be \"True\" or \"False\"";
     
     // paste errors
     public static final String PASTE_AREA_ERROR = "Copy Area and Paste Area are different sizes.";
     public static final String LOWER_BOUND_PASTE_ERROR = "Lower bound must be less than or equal to upper bound.";
     public static final String UPPER_BOUND_PASTE_ERROR = "Upper bound must be greater than or equal to lower bound.";
     public static final String INVALID_PASTE_BOOLEAN_VALUE = "Boolean Value Error";
     
     // clear error
     public static final String CLEAR_ERROR_MESSAGE = "One or more cells selected require a value and cannot be cleared.";
     
     // interface titles and labels
     public static final String COLUMN_ADD_INTERFACE_TITLE = "Add Column";   
     public static final String COLUMN_RENAME_INTERFACE_TITLE = "Rename Column";    
     public static final String COLUMN_ADD_RENAME_LABEL = "Enter Column Name";
       
     public static final String RENAME_METABOLITE_INTERFACE_TITLE = "Rename Metabolite";
     public static final String RENAME_METABOLITE_LABEL = "Enter New Metabolite Name";
     
     public static final String CSV_FILE_LOAD_INTERFACE_TITLE = "CSV File Load";    
     public static final String CSV_FILE_LOAD_METAB_BUTTON = "Load Metabolites File";
     public static final String CSV_FILE_LOAD_REAC_BUTTON = " Load Reactions File "; 
     
     // delete optimize database and log files
     public static final String DELETE_ASSOCIATED_FILES_TITLE = "Delete Associated Files?";
     public static final String DELETE_ASSOCIATED_FILES = "Delete Associated database and log Files?";
     
     // tab names
     public static final String DEFAULT_METABOLITE_TABLE_TAB_NAME = "Metabolites";
     
     public static final String DEFAULT_REACTION_TABLE_TAB_NAME = " Reactions ";
     
     // help
     public static final String HELP_TOPICS_URL = "https://github.com/dennisegen/MOST/wiki";
     public static final String HELP_URL_NOT_FOUND_TITLE = "URL Not Found";
     public static final String HELP_URL_NOT_FOUND_MESSAGE = "This URL may not exist. Check internet connection.";
     
     // about
     public static final String ABOUT_BOX_TITLE = "About MOST";
     public static final String ABOUT_BOX_TEXT = "MOST - Metabolic Optimization and Simulation Tool." +
 	" Version 1.0.0";
     
     // other
     public static final String[] REVERSIBLE_ARROWS =
    {"<==> ", "<=> ", "= "
     };
     
     public static final String[] NOT_REVERSIBLE_ARROWS =
    {"=> ", "--> ", "-> "
     };
     
     public static final int PROGRESS_BAR_WIDTH = 175;
     public static final int PROGRESS_BAR_HEIGHT = 38;
     
     public static final int UNDO_MAX_VISIBLE_ROWS = 10;
     public static final double UNDO_VISIBILITY_FRACTION = 0.1;
     public static final int UNDO_BORDER_HEIGHT = 30;
     
     //public static final String UNDO_ICON_IMAGE_PATH = "etc/toolbarIcons/Sideways_Arrow_Icon16b.png";
     public static final String UNDO_ICON_IMAGE_PATH = "etc/toolbarIcons/1374634434_back_undo.png";
     public static final String REDO_ICON_IMAGE_PATH = "etc/toolbarIcons/1374634411_redo_forward.png";
     //public static final String REDO_ICON_IMAGE_PATH = "etc/toolbarIcons/Sideways_Arrow_Icon16a.png";
     //public static final String UNDO_GRAYED_ICON_IMAGE_PATH = "etc/toolbarIcons/Sideways_Arrow_Icon16d.png";
     public static final String UNDO_GRAYED_ICON_IMAGE_PATH = "etc/toolbarIcons/1374634434_back_undo_grey.png";
     //public static final String REDO_GRAYED_ICON_IMAGE_PATH = "etc/toolbarIcons/Sideways_Arrow_Icon16c.png";
     public static final String REDO_GRAYED_ICON_IMAGE_PATH = "etc/toolbarIcons/1374634411_redo_forward_grey.png";
     
     // icons from http://www.veryicon.com/icons/application/toolbar-icons/
     /*
     Icon Author: Ruby Software
 
     HomePage: http://www.rubysoftware.nl/free-toolbar-icon-collection/old.php?lang=en
     License: Commercial usage: Allowed
     */
     public static final String OPEN_ICON_IMAGE_PATH = "etc/toolbarIcons/Open.png";
     public static final String SAVE_ICON_IMAGE_PATH = "etc/toolbarIcons/Save.png";
     // From a 48x48 image, resized in Photoshop and color and contrast changed
     public static final String COPY_ICON_IMAGE_PATH = "etc/toolbarIcons/copy16.png";
     public static final String PASTE_ICON_IMAGE_PATH = "etc/toolbarIcons/Paste16.png";
     
     // icons from http://www.softpedia.com/get/Desktop-Enhancements/Icons-Related/24x24-Free-Application-Icons.shtml
     // also free license from Aha-soft
     // resized to 16x16 in Photoshop
     public static final String FIND_ICON_IMAGE_PATH = "etc/toolbarIcons/Search1.png";
     
     public static final Color FIND_ALL_COLOR = new Color(140,160,200);
     //public static final Color FIND_ALL_COLOR = new Color(190,205,225);
     public static final Color SELECTED_AREA_COLOR = new Color(190,205,225);
     
     public static final String GUROBI_JAR_PATH_INTERFACE_TITLE = "Gurobi Jar Path Locator";
     public static final String GUROBI_JAR_PATH_NOT_FOUND_LABEL = "Gurobi Jar Path Not Found. Click Load Gurobi Jar Path to Browse For Path";
     public static final String GUROBI_JAR_PATH_FOUND_LABEL = "Gurobi Jar Path Found. Click 'OK' if Correct or Click 'Load Gurobi Jar Path' if Not Correct.";
     public static final String GUROBI_JAR_PATH_BUTTON = "Load Gurobi Jar Path";
     public static final String NO_GUROBI_JAR_PATH_ERROR = "There is no Gurobi Jar path set.\n FBA and GDBB will not function";
     public static final String NO_GUROBI_JAR_PATH_ERROR_TITLE = "No Gurobi Jar Path";
     public static final String GUROBI_JAR_PATH_FILE_CHOOSER_TITLE = "Browse For Gurobi Jar Path";
     public static final String GUROBI_JAR_PATH_OPTIONS_MENU_ITEM = "Set Gurobi Jar Path";
     
     public static final String GUROBI_KEY_ERROR_TITLE = "Gurobi Key Error";
     public static final String GUROBI_KEY_ERROR = "Gurobi Key Error";
         
     public static final String LOAD_FROM_MODEL_COLLECTION_TABLE_TITLE = "Load from Model Collection Database";
     
     // spaces for alignment
     public static final String ROW_HEADER_TITLE = "   Row";
     
     // this is used when exiting to choose what is default for saving opt files
     // should be csv if user is building a model since it accepts anything, sbml
     // is much more stringent and should only be used for models that are "mature"
     public static final String DEFAULT_FILE_TYPE = "csv";
 
 	public static final String GDBB_PREFIX = "GDBB";
 }
 
