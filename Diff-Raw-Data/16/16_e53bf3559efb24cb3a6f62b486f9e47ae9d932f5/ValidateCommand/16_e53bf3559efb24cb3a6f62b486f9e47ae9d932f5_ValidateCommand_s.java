 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.converter.FileConverter;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.contentspec.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 
 @Parameters(commandDescription = "Validate a Content Specification")
 public class ValidateCommand extends BaseCommandImpl {
     @Parameter(converter = FileConverter.class, metaVar = "[FILE]")
     private List<File> files = new ArrayList<File>();
 
     @Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
     private Boolean permissive = false;
 
     private ContentSpecProcessor csp = null;
 
     public ValidateCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.VALIDATE_COMMAND_NAME;
     }
 
     public List<File> getFiles() {
         return files;
     }
 
     public void setFiles(final List<File> files) {
         this.files = files;
     }
 
     public Boolean getPermissive() {
         return permissive;
     }
 
     public void setPermissive(final Boolean permissive) {
         this.permissive = permissive;
     }
 
     public boolean isValid() {
         // We should have only one file
         if (files.size() != 1) return false;
 
         // Check that the file exists
         final File file = files.get(0);
         return !(file.isDirectory() || !file.exists() || !file.isFile());
     }
 
     @Override
     public void process() {
         // Authenticate the user
         final UserWrapper user = authenticate(getUsername(), getProviderFactory());
 
         // If files is empty then we must be using a csprocessor.cfg file
         if (loadFromCSProcessorCfg()) {
             // Check that the config details are valid
             if (getCspConfig() != null && getCspConfig().getContentSpecId() != null) {
                 final ContentSpecWrapper contentSpec = getProviderFactory().getProvider(ContentSpecProvider.class).getContentSpec(
                         getCspConfig().getContentSpecId(), null);
                 final String fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
                 File file = new File(fileName);
                 if (!file.exists()) {
                     // Backwards compatibility check for files ending with .txt
                     file = new File(DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-post.txt");
                     if (!file.exists()) {
                         printErrorAndShutdown(Constants.EXIT_FAILURE, String.format(Constants.NO_FILE_FOUND_FOR_CONFIG, fileName), false);
                     }
                 }
                 files.add(file);
             }
         }
 
         // Check that the parameters are valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_FILE_MSG, true);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         boolean success = false;
 
         // Read in the file contents
         final String contentSpecString = FileUtilities.readFileContents(files.get(0));
 
         if (contentSpecString.equals("")) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_EMPTY_FILE_MSG, false);
         }
 
         // Parse the spec
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         ContentSpec contentSpec = ClientUtilities.parseContentSpecString(getProviderFactory(), loggerManager, contentSpecString);
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setPermissiveMode(permissive);
         processingOptions.setValidating(true);
         processingOptions.setAllowEmptyLevels(true);
 
         // Process the content spec to see if it's valid
         csp = new ContentSpecProcessor(getProviderFactory(), loggerManager, processingOptions);
         try {
             success = csp.processContentSpec(contentSpec, user, ContentSpecParser.ParsingMode.EITHER);
         } catch (Exception e) {
            e.printStackTrace();
             printErrorAndShutdown(Constants.EXIT_INTERNAL_SERVER_ERROR, Constants.ERROR_INTERNAL_ERROR, false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Print the logs
         JCommander.getConsole().println(loggerManager.generateLogs());
         if (success) {
             JCommander.getConsole().println("VALID");
         } else {
             JCommander.getConsole().println("INVALID");
             JCommander.getConsole().println("");
             shutdown(Constants.EXIT_TOPIC_INVALID);
         }
     }
 
     @Override
     public void shutdown() {
         super.shutdown();
         if (csp != null) {
             csp.shutdown();
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return files.size() == 0;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
