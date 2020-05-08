 package net.sourceforge.importscrubber;
 
 import java.util.ListResourceBundle;
 
 public class Resources extends ListResourceBundle
 {
     public static final String APP_NAME = "Importscrubber";
     public static final String FILE_BROWSER_TITLE = "Browse";
     public static final String VERSION_ID = APP_NAME + " 1.4.3";
     public static final String BROWSE_LABEL = "Browse";
     public static final String GO_LABEL = "Go";
     public static final String FIND_FILES_LABEL = "Add files";
     public static final String CLEAR_FILES_LABEL = "Clear files";
     public static final String ALL_DONE = "All done!";
     public static final String HELP_LABEL = "Help";
     public static final String FILE_LABEL = "File";
     public static final String ABOUT_LABEL = "About";
     public static final String OPTIONS_LABEL = "Options";
     public static final String RECURSE_LABEL = "Recurse";
     public static final String EXIT_LABEL = "Exit";
     public static final String HELP_MESSAGE = "                     " + APP_NAME + ImportScrubber.LINE_SEPARATOR +  "This is a utility to clean up import statements.  To use it:" + ImportScrubber.LINE_SEPARATOR +  "1) 1) Select your source code and class file directories " + ImportScrubber.LINE_SEPARATOR +  "2) Click \"Add files\"" + ImportScrubber.LINE_SEPARATOR +  "3) Pick any other options" + ImportScrubber.LINE_SEPARATOR +  "4) Click \"Go\"" + ImportScrubber.LINE_SEPARATOR +  "Importscrubber will crank away for a few seconds and then pop up a box telling you it's done." + ImportScrubber.LINE_SEPARATOR +  "To process multiple files, just pick a directory and toggle the \"recursive\" checkbox" + ImportScrubber.LINE_SEPARATOR +  "Questions? Comments? Post them here - http://sourceforge.net/forum/forum.php?forum_id=60067";
     public static final String BREAK_EACH_PACKAGE = "Break for each package";
     public static final String BREAK_NONE = "No breaks";
 
    public static final String ERR_NOT_DIR = " is not a directory!";
     public static final String ERR_UNABLE_TO_FINISH = "Unable to finish due to:";
    public static final String ERR_DIR_NOT_EXIST = " does not exist!";
     public static final String ERR_CLASS_FILE_MUST_EXIST = "Class file must exist: ";
     public static final String ERR_MUST_NOT_BE_DIR = "Input file cannot be a directory: ";
     public static final String SORT_JAVA_LIBS_LABEL = "Sort standard libraries high";
 
     private static final Object[][] contents = {
                 {FILE_BROWSER_TITLE, FILE_BROWSER_TITLE},
                 {VERSION_ID, VERSION_ID},
                 {BROWSE_LABEL, BROWSE_LABEL},
                 {GO_LABEL, GO_LABEL},
                 {OPTIONS_LABEL, OPTIONS_LABEL},
                 {HELP_LABEL, HELP_LABEL},
                 {EXIT_LABEL, EXIT_LABEL},
                 {ABOUT_LABEL, ABOUT_LABEL},
                 {RECURSE_LABEL, RECURSE_LABEL},
                 {FILE_LABEL, FILE_LABEL},
                 {FIND_FILES_LABEL, FIND_FILES_LABEL},
                 {CLEAR_FILES_LABEL, CLEAR_FILES_LABEL},
                 {ALL_DONE, ALL_DONE},
                 {APP_NAME, APP_NAME},
                 {HELP_MESSAGE, HELP_MESSAGE},
                 {BREAK_EACH_PACKAGE, BREAK_EACH_PACKAGE},
                 {BREAK_NONE, BREAK_NONE},
                 {ERR_NOT_DIR, ERR_NOT_DIR},
                 {ERR_DIR_NOT_EXIST, ERR_DIR_NOT_EXIST},
                 {ERR_CLASS_FILE_MUST_EXIST, ERR_CLASS_FILE_MUST_EXIST},
                 {ERR_MUST_NOT_BE_DIR, ERR_MUST_NOT_BE_DIR},
                 {ERR_UNABLE_TO_FINISH, ERR_UNABLE_TO_FINISH},
                 {SORT_JAVA_LIBS_LABEL, SORT_JAVA_LIBS_LABEL}
             };
 
     public Object[][] getContents()
     {
         return contents;
     }
 }
