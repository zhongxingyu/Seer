 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.FutureTask;
 
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
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.LogMessageProvider;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.provider.TextContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.exception.ProviderException;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextCSProcessingOptionsWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextContentSpecWrapper;
 
 @Parameters(commandDescription = "Push an updated Content Specification to the server")
 public class PushCommand extends BaseCommandImpl {
     @Parameter(converter = FileConverter.class, metaVar = "[FILE]")
     private List<File> files = new ArrayList<File>();
 
     @Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
     private Boolean permissive = false;
 
     @Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
     private Boolean executionTime = false;
 
     @Parameter(names = Constants.PUSH_ONLY_LONG_PARAM,
             description = "Only push the Content Specification and don't save the Post Processed Content Specification.")
     private Boolean pushOnly = false;
 
     @Parameter(names = {Constants.MESSAGE_LONG_PARAM, Constants.MESSAGE_SHORT_PARAM},
             description = "A commit message about what was " + "changed.")
     private String message = null;
 
     @Parameter(names = Constants.REVISION_MESSAGE_FLAG_LONG_PARAMETER,
             description = "The commit message should be set to be included in " + "the Revision History.")
     private Boolean revisionHistoryMessage = false;
 
     @Parameter(names = Constants.STRICT_LEVEL_TITLES_LONG_PARAM, description = "Enforce that the level titles match their topic titles.")
     protected Boolean strictLevelTitles = false;
 
     private ContentSpecProcessor csp = null;
 
     public PushCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.PUSH_COMMAND_NAME;
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
 
     public Boolean getExecutionTime() {
         return executionTime;
     }
 
     public void setExecutionTime(final Boolean executionTime) {
         this.executionTime = executionTime;
     }
 
     public Boolean getPushOnly() {
         return pushOnly;
     }
 
     public void setPushOnly(final boolean pushOnly) {
         this.pushOnly = pushOnly;
     }
 
     public Boolean getRevisionHistoryMessage() {
         return revisionHistoryMessage;
     }
 
     public void setRevisionHistoryMessage(Boolean revisionHistoryMessage) {
         this.revisionHistoryMessage = revisionHistoryMessage;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public Boolean getStrictLevelTitles() {
         return strictLevelTitles;
     }
 
     public void setStrictLevelTitles(Boolean strictLevelTitles) {
         this.strictLevelTitles = strictLevelTitles;
     }
 
     /**
      * Checks that the input from the command line is valid arguments.
      *
      * @return True if the set arguments are valid, otherwise false.
      */
     public boolean isValid() {
         // We should have only one file
         if (getFiles().size() != 1) return false;
 
         // Check that the file exists
         final File file = getFiles().get(0);
         return !(file.isDirectory() || !file.exists() || !file.isFile());
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         boolean pushingFromConfig = false;
         // If files is empty then we must be using a csprocessor.cfg file
         if (loadFromCSProcessorCfg()) {
             // Check that the config details are valid
             if (getCspConfig() != null && getCspConfig().getContentSpecId() != null) {
                 final ContentSpecWrapper contentSpecEntity = contentSpecProvider.getContentSpec(getCspConfig().getContentSpecId(), null);
                 final String fileName = DocBookUtilities.escapeTitle(
                         contentSpecEntity.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
                 File file = new File(fileName);
                 if (!file.exists()) {
                     // Backwards compatibility check for files ending with .txt
                     file = new File(DocBookUtilities.escapeTitle(contentSpecEntity.getTitle()) + "-post.txt");
                     if (!file.exists()) {
                         printErrorAndShutdown(Constants.EXIT_FAILURE, String.format(Constants.NO_FILE_FOUND_FOR_CONFIG, fileName), false);
                     }
                 }
                 getFiles().add(file);
             }
             pushingFromConfig = true;
         }
 
         // Check that the parameters are valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_FILE_MSG, true);
         }
 
         // Good point to check for a shutdown (before starting)
         allowShutdownToContinueIfRequested();
 
         long startTime = System.currentTimeMillis();
 
         // Load the content spec from the file and parse it into a ContentSpec object
         final ContentSpec contentSpec = getContentSpecFromFile(getFiles().get(0));
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Process/Save the content spec
         final TextContentSpecWrapper output = processAndSaveContentSpec(getProviderFactory(), contentSpec, getUsername());
         final boolean success = output.getErrors() != null && output.getErrors().contains(ProcessorConstants.INFO_SUCCESSFUL_SAVE_MSG);
 
         // Print the logs
         long elapsedTime = System.currentTimeMillis() - startTime;
         JCommander.getConsole().println(output.getErrors());
         if (getExecutionTime()) {
             JCommander.getConsole().println(String.format(Constants.EXEC_TIME_MSG, elapsedTime));
         }
 
         // if we failed validation then exit
         if (!success) {
             shutdown(Constants.EXIT_TOPIC_INVALID);
         } else {
             JCommander.getConsole().println(String.format(ProcessorConstants.SUCCESSFUL_PUSH_MSG, output.getId(), output.getRevision()));
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (success && !pushOnly) {
             savePostProcessedContentSpec(pushingFromConfig, output);
         }
     }
 
     /**
      * Get a content specification from a file and parse it into a ContentSpec object, so that ti can be used for processing.
      *
      * @param file The file to load the content spec from.
      * @return The parsed content specification object.
      */
     protected ContentSpec getContentSpecFromFile(File file) {
         // Read in the file contents
         String contentSpecString = FileUtilities.readFileContents(file);
 
         if (contentSpecString.equals("")) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_EMPTY_FILE_MSG, false);
         }
 
         // Parse the spec
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         JCommander.getConsole().println("Starting to parse...");
         ContentSpec contentSpec = ClientUtilities.parseContentSpecString(getProviderFactory(), loggerManager, contentSpecString,
                 ContentSpecParser.ParsingMode.EDITED);
 
         // Check that that content specification was parsed successfully
         if (contentSpec == null) {
             JCommander.getConsole().println(loggerManager.generateLogs());
             shutdown(Constants.EXIT_FAILURE);
         }
 
         return contentSpec;
     }
 
     /**
      * Process a content specification and save it to the server.
      *
      * @param providerFactory The provider factory to create providers to lookup entity details.
      * @param contentSpec     The content spec to be processed and saved.
      * @param username        The user who requested the content spec be processed and saved.
      * @return True if the content spec was processed and saved successfully, otherwise false.
      */
     protected TextContentSpecWrapper processAndSaveContentSpec(final RESTProviderFactory providerFactory, final ContentSpec contentSpec,
             final String username) {
         final TextContentSpecProvider textContentSpecProvider = providerFactory.getProvider(TextContentSpecProvider.class);
         final TextCSProcessingOptionsWrapper processingOptions = textContentSpecProvider.newTextProcessingOptions();
         processingOptions.setPermissive(permissive);
 
         // Create the task to update the content spec on the server
         final FutureTask<TextContentSpecWrapper> task = new FutureTask<TextContentSpecWrapper>(new Callable<TextContentSpecWrapper>() {
             @Override
             public TextContentSpecWrapper call() throws Exception {
                 int flag = 0;
                 if (getRevisionHistoryMessage()) {
                     flag = 0 | RESTLogDetailsV1.MAJOR_CHANGE_FLAG_BIT;
                 } else {
                     flag = 0 | RESTLogDetailsV1.MINOR_CHANGE_FLAG_BIT;
                 }
                 final LogMessageWrapper logMessage = providerFactory.getProvider(LogMessageProvider.class).createLogMessage();
                 logMessage.setFlags(flag);
                 logMessage.setMessage(getMessage());
                 logMessage.setUser(username);
 
                 TextContentSpecWrapper output = null;
                 try {
                     final TextContentSpecWrapper contentSpecEntity = textContentSpecProvider.newTextContentSpec();
                     contentSpecEntity.setText(contentSpec.toString());
                     output = textContentSpecProvider.updateTextContentSpec(contentSpecEntity, processingOptions, logMessage);
                 } catch (ProviderException e) {
                     output = textContentSpecProvider.newTextContentSpec();
                     output.setErrors(e.getMessage());
                 }
 
                 return output;
             }
         });
 
         return ClientUtilities.saveContentSpec(this, task);
     }
 
     /**
      * Saves a post processed content specification to the project directory if pushing using a csprocessor.cfg,
      * otherwise save it in the current working directory.
      *
      * @param pushingFromConfig If the command is pushing from a CSP Project directory.
      * @param contentSpec       The post processed content spec object.
      */
     protected void savePostProcessedContentSpec(boolean pushingFromConfig, final TextContentSpecWrapper contentSpec) {
         // Save the post spec to file if the push was successful
         final File outputSpec;
         if (pushingFromConfig) {
             final String escapedTitle = DocBookUtilities.escapeTitle(contentSpec.getTitle());
             outputSpec = new File(ClientUtilities.getOutputRootDirectory(getCspConfig(), contentSpec) + escapedTitle + "-post." +
                     Constants.FILENAME_EXTENSION);
         } else {
             outputSpec = getFiles().get(0);
         }
 
         // Create the directory
         if (outputSpec.getParentFile() != null) {
             outputSpec.getParentFile().mkdirs();
         }
 
         // Save the Post Processed spec
         try {
             FileUtilities.saveFile(outputSpec, contentSpec.toString(), Constants.FILE_ENCODING);
         } catch (IOException e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, String.format(Constants.ERROR_FAILED_SAVING_FILE, outputSpec.getAbsolutePath()),
                     false);
         }
     }
 
     @Override
     public void shutdown() {
         if (csp != null) {
             csp.shutdown();
         }
         super.shutdown();
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return getFiles().size() == 0;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
