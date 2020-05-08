 package fedora.utilities.install;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 public class OptionDefinition {
 
     private static Properties _PROPS;
 
     private String _id;
     private String _label;
 
     private String _description;
 
     private String[] _validValues;
 
     private String _defaultValue;
 
     static {
         String path = "fedora/utilities/install/OptionDefinition.properties";
         try {
             InputStream in = OptionDefinition.class.getClassLoader().
                     getResourceAsStream(path);
             _PROPS = new Properties();
             _PROPS.load(in);
         } catch (Exception e) {
             System.err.println("ERROR: Unable to load required resource: " + path);
             System.exit(1);
         }
     }
 
     private OptionDefinition(String id,
                              String label,
                              String description,
                              String[] validValues,
                              String defaultValue) {
         _id = id;
         _label = label;
 
         _description = description;
 
         _validValues = validValues;
 
         _defaultValue = defaultValue;
     }
 
     /**
      * Get the definition of the identified option, or <code>null</code>
      * if no such definition exists.
      */
     public static OptionDefinition get(String id) {
         String label = _PROPS.getProperty(id + ".label");
         String description = _PROPS.getProperty(id + ".description");
         String[] validValues = getArray(id + ".validValues");
         String defaultValue = _PROPS.getProperty(id + ".defaultValue");
         
         // Use the environment variable FEDORA_HOME as the default, if defined
         if (id.equals(InstallOptions.FEDORA_HOME)) {
         	String eFH = System.getenv("FEDORA_HOME");
         	if (eFH != null && eFH.length() != 0) {
         		defaultValue = eFH;
         	}
         }
         return new OptionDefinition(id, label, description, validValues, defaultValue);
     }
 
     private static String[] getArray(String propName) {
 
         String value = _PROPS.getProperty(propName);
         if (value == null) {
             return null;
         } else {
             return value.trim().split(" ");
         }
     }
 
     public String getId() {
         return _id;
     }
 
     public String getLabel() {
         return _label;
     }
 
     public String getDescription() {
         return _description;
     }
 
     public String[] getValidValues() {
         return _validValues;
     }
 
     public String getDefaultValue() {
         return _defaultValue;
     }
     
     public void validateValue(String value) throws OptionValidationException {
     	validateValue(value, false);
     }
 
     public void validateValue(String value, boolean unattended) 
             throws OptionValidationException {
         if (value.length() == 0) {
             throw new OptionValidationException("Must specify a value", _id);
         }
         String[] valids = getValidValues();
         if (valids != null) {
             boolean isValid = false;
             for (int i = 0; i < valids.length; i++) {
                 if (valids[i].equals(value)) {
                     isValid = true;
                 }
             }
             if (!isValid) {
                 throw new OptionValidationException("Not a valid value: " 
                         + value, _id);
             }
         } else {
             if (_id.equals(InstallOptions.FEDORA_HOME)) {
             	String eFH = System.getenv("FEDORA_HOME");
             	if (eFH == null || eFH.length() == 0) {
                 	System.out.println("WARNING: The environment variable, FEDORA_HOME, is not defined");
                 	System.out.println("WARNING: Remember to define the FEDORA_HOME environment variable");
                 	System.out.println("WARNING: before starting Fedora.");
             	} else if (!eFH.equals(value)) {
             		System.out.println("WARNING: The environment variable, FEDORA_HOME, is defined as");
             		System.out.println("WARNING:   " + eFH);
             		System.out.println("WARNING: but you entered ");
             		System.out.println("WARNING:   " + value);
             		System.out.println("WARNING: Please ensure you have correctly defined FEDORA_HOME");
             		System.out.println("WARNING: before starting Fedora.");
             	}
             	
                 File dir = new File(value);
                 if (dir.isDirectory()) {
                	if (unattended) {
                		System.out.println("WARNING: Overwriting existing directory: " + dir.getAbsolutePath());
                	} else {
	                	System.out.println("WARNING: " + dir.getAbsolutePath() + " already exists.");
	                	System.out.println("WARNING: Overwrite? (yes or no) [default is no] ==> ");
	                	String confirm = readLine().trim();
	                    if (confirm.length() == 0 || confirm.equalsIgnoreCase("no")) {
	                    	throw new OptionValidationException("Directory exists; delete it or choose another", _id);
	                    }
                 	}
                 } else {
                     // must be creatable
                     boolean created = dir.mkdirs();
                     if (!created) {
                         throw new OptionValidationException("Unable to create specified directory", _id);
                     } else {
                         dir.delete();
                     }
                 }
             } else if (_id.equals(InstallOptions.TOMCAT_HOME)) {
                 File dir = new File(value);
                 if (dir.exists()) {
                     // must have webapps subdir
                     File webapps = new File(dir, "webapps");
                     if (!webapps.exists()) {
                         throw new OptionValidationException("Directory exists but does not contain a webapps subdirectory", _id);
                     }
                 } else {
                     // or must be creatable
                     boolean created = dir.mkdir();
                     if (!created) {
                         throw new OptionValidationException("Unable to create specified directory", _id);
                     } else {
                         dir.delete();
                     }
                 }
             } else if (_id.equals(InstallOptions.TOMCAT_SHUTDOWN_PORT)) {
                 validatePort(value);
             } else if (_id.equals(InstallOptions.TOMCAT_HTTP_PORT)) {
                 validatePort(value);
             } else if (_id.equals(InstallOptions.TOMCAT_SSL_PORT)) {
                 validatePort(value);
             } else if (_id.equals(InstallOptions.KEYSTORE_FILE)) {
                 if (!value.equals(InstallOptions.INCLUDED)) {
                     validateExistingFile(value);
                 }
             } else if (_id.startsWith(InstallOptions.DATABASE) && _id.endsWith(".driver")) {
             	if (!value.equals(InstallOptions.INCLUDED)) {
             		validateExistingFile(value);
             	}
             }
         }
     }
     
     private String readLine() {
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             return reader.readLine();
         } catch (Exception e) {
             throw new RuntimeException("Error: Unable to read from STDIN");
         }
     }
     
     private void validateExistingFile(String val) throws OptionValidationException {
         File f = new File(val);
         if (!f.exists()) {
             throw new OptionValidationException("No such file", _id);
         }
     }
 
     private void validatePort(String val) throws OptionValidationException {
         try {
             int port = Integer.parseInt(val);
             if (port < 0 || port > 65535) {
                 throw new OptionValidationException("Not a valid port number", _id);
             }
         } catch (NumberFormatException e) {
             throw new OptionValidationException("Not an integer", _id);
         }
     }
 }
