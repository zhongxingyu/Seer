 package org.bitrepository.commandline;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.SettingsProvider;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 import org.bitrepository.protocol.security.BasicMessageAuthenticator;
 import org.bitrepository.protocol.security.BasicMessageSigner;
 import org.bitrepository.protocol.security.BasicOperationAuthorizor;
 import org.bitrepository.protocol.security.BasicSecurityManager;
 import org.bitrepository.protocol.security.MessageAuthenticator;
 import org.bitrepository.protocol.security.MessageSigner;
 import org.bitrepository.protocol.security.OperationAuthorizor;
 import org.bitrepository.protocol.security.PermissionStore;
 
 /**
  * Interface for handling the command line arguments.
  */
 public abstract class CommandLineInterface {
     /** The parser of the command line arguments.*/
     protected final CommandLineParser parser;
     /** The options for the command line arguments*/
     protected final Options options;
     
     /**
      * Constructor.
      */
     public CommandLineInterface() {
         parser = new PosixParser();
         options = new Options();
         
         createDefaultOptions();
         createSpecificOptions();
     }
     
     /**
      * Creates the default options for the command line arguments for the clients.
      */
     private void createDefaultOptions() {
         Option settingsOption = new Option(Constants.SETTINGS_ARG, true, "The path to the directory with the settings "
                 + "files for the client");
         settingsOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
         options.addOption(settingsOption);
         
         Option privateKeyOption = new Option(Constants.PRIVATE_KEY_ARG, true, "The path to the file containing "
                 + "the private key.");
         privateKeyOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);        
         options.addOption(privateKeyOption);
     }
     
     /**
      * Parses the commandline arguments.
      * @param args The command line arguments to pass.
      * @return The commandline with the parsed arguments.
      */
     protected CommandLine parseArguments(String ... args) {
         try {
             return parser.parse(options, args);
         } catch (ParseException e) {
             String errMsg = "Cannot parse the argumets: " + Arrays.asList(args);
             throw new IllegalStateException(errMsg, e);
         }
     }
     
     /**
      * Adds the specific options for the given instance.
      */
     abstract void createSpecificOptions(); 
     
     /**
      * @return Lists the possible arguments in a human readable format.
      */
     @SuppressWarnings("unchecked")
     protected String listArguments() {
         StringBuilder res = new StringBuilder();
         res.append("Takes the following arguments:\n");
         for(Option option : (Collection<Option>) options.getOptions()) {
             res.append("-" + option.getOpt() + " " + option.getDescription() + "\n");
         }
         return res.toString();
     }    
     
     /**
      * Method for retrieving the settings for the launcher.
      * @param componentId The id of the component.
      * @param pathToSettings The path to the settings.
      * @return The settings.
      */
     protected static Settings loadSettings(String componentId, String pathToSettings) {
         ArgumentValidator.checkNotNullOrEmpty(componentId, "String componentId");
         ArgumentValidator.checkNotNullOrEmpty(pathToSettings, "String pathToSettings");
         
         SettingsProvider settingsLoader =
                 new SettingsProvider(new XMLFileSettingsLoader(pathToSettings), componentId);
         return settingsLoader.getSettings();
     }
     
     /**
      * Instantiates the security manager based on the settings and the path to the key file.
      * @param pathToPrivateKeyFile The path to the key file.
      * @param settings The settings.
      * @return The security manager.
      */
     protected static BasicSecurityManager loadSecurityManager(String pathToPrivateKeyFile, Settings settings) {
         ArgumentValidator.checkNotNullOrEmpty(pathToPrivateKeyFile, "String pathToPrivaetKeyFile");
         ArgumentValidator.checkNotNull(settings, "Settings settings");
         
         String privateKeyFile;
         privateKeyFile = pathToPrivateKeyFile;
         
         PermissionStore permissionStore = new PermissionStore();
         MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
         MessageSigner signer = new BasicMessageSigner();
         OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
         return new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile,
                 authenticator, signer, authorizer, permissionStore,
                 settings.getComponentID());
     }
 }
