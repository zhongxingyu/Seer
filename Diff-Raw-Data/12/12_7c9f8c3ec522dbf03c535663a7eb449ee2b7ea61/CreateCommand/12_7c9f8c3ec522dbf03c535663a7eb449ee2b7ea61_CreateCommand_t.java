 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
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
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 @Parameters(commandDescription = "Create a new Content Specification on the server")
 public class CreateCommand extends BaseCommandImpl {
     @Parameter(converter = FileConverter.class, metaVar = "[FILE]")
     private List<File> files = new ArrayList<File>();
 
     @Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
     private Boolean permissive = false;
 
     @Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
     private Boolean executionTime = false;
 
     @Parameter(names = Constants.NO_CREATE_CSPROCESSOR_CFG_LONG_PARAM, description = "Don't create the csprocessor.cfg and other files.")
     private Boolean noCreateCsprocessorCfg = false;
 
     @Parameter(names = {Constants.FORCE_LONG_PARAM, Constants.FORCE_SHORT_PARAM},
             description = "Force the Content Specification directories to be created.")
     private Boolean force = false;
 
     @Parameter(names = {Constants.MESSAGE_LONG_PARAM, Constants.MESSAGE_SHORT_PARAM}, description = "A commit message about what was " +
             "changed.")
     private String message = null;
 
     @Parameter(names = Constants.REVISION_MESSAGE_FLAG_LONG_PARAMETER, description = "The commit message should be set to be included in " +
             "the Revision History.")
     private Boolean revisionHistoryMessage = false;
 
     @Parameter(names = Constants.STRICT_LEVEL_TITLES_LONG_PARAM, description = "Enforce that the level titles match their topic titles.")
     protected Boolean strictLevelTitles = false;
 
     public CreateCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.CREATE_COMMAND_NAME;
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
 
     public Boolean getNoCreateCsprocessorCfg() {
         return noCreateCsprocessorCfg;
     }
 
     public void setNoCreateCsprocessorCfg(final Boolean noCreateCsprocessorCfg) {
         this.noCreateCsprocessorCfg = noCreateCsprocessorCfg;
     }
 
     public Boolean getForce() {
         return force;
     }
 
     public void setForce(final Boolean force) {
         this.force = force;
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
 
         // Check that the options set are valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_FILE_MSG, true);
         }
 
         long startTime = System.currentTimeMillis();
 
         // Read in the file contents and parse the file into a ContentSpec object
         final ContentSpec contentSpec = getContentSpecFromFile(getFiles().get(0));
 
         // Check that the output directory doesn't already exist
         final File directory = new File(getCspConfig().getRootOutputDirectory() + DocBookUtilities.escapeTitle(contentSpec.getTitle()));
         if (directory.exists() && !getForce() && !getNoCreateCsprocessorCfg() && directory.isDirectory()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE,
                     String.format(Constants.ERROR_CONTENT_SPEC_EXISTS_MSG, directory.getAbsolutePath()), false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Process/Save the content spec
         final TextContentSpecWrapper output = processContentSpec(getProviderFactory(), contentSpec, getUsername());
         final boolean success = output.getErrors() != null && output.getErrors().contains(ProcessorConstants.INFO_SUCCESSFUL_SAVE_MSG);
 
         // Print the logs
         long elapsedTime = System.currentTimeMillis() - startTime;
         JCommander.getConsole().println(output.getErrors());
         // Print the command execution time as saving files shouldn't be included
         if (executionTime) {
             JCommander.getConsole().println(String.format(Constants.EXEC_TIME_MSG, elapsedTime));
         }
         if (!success) {
             shutdown(Constants.EXIT_FAILURE);
         } else {
             final Integer revision = contentSpecProvider.getContentSpec(output.getId()).getRevision();
            JCommander.getConsole().println(String.format(ProcessorConstants.SUCCESSFUL_PUSH_MSG, output.getId(), revision));
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (success && !getNoCreateCsprocessorCfg()) {
             // Create the blank zanata details as we shouldn't have a zanata setup at creation time
             final ZanataDetails zanataDetails = new ZanataDetails();
             zanataDetails.setServer(null);
             zanataDetails.setProject(null);
             zanataDetails.setVersion(null);
 
             final ContentSpecWrapper contentSpecEntity = contentSpecProvider.getContentSpec(output.getId());
 
             // Create the project directory and files
             ClientUtilities.createContentSpecProject(this, getCspConfig(), directory, output.getText(), contentSpecEntity,
                     zanataDetails);
         }
     }
 
     /**
      * Get a content specification from a file and parse it into a ContentSpec object, so that ti can be used for processing.
      *
      * @param file The file to load the content spec from.
      * @return The parsed content specification object.
      */
     protected ContentSpec getContentSpecFromFile(final File file) {
         final String contentSpecString = FileUtilities.readFileContents(file);
         if (contentSpecString.equals("")) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_EMPTY_FILE_MSG, false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Parse the spec to get the title
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         JCommander.getConsole().println("Starting to parse...");
         ContentSpec contentSpec = ClientUtilities.parseContentSpecString(getProviderFactory(), loggerManager, contentSpecString,
                 ContentSpecParser.ParsingMode.NEW, true);
 
         // Check that that content specification was parsed successfully
         if (contentSpec == null) {
             JCommander.getConsole().println(loggerManager.generateLogs());
             shutdown(Constants.EXIT_FAILURE);
         }
 
         return contentSpec;
     }
 
     /**
      * Process and save a content specification to the server.
      *
      * @param providerFactory
      * @param contentSpec   The content spec to be saved.
      * @param username      The user who requested the content spec be saved.
      * @return
      */
     protected TextContentSpecWrapper processContentSpec(final RESTProviderFactory providerFactory, final ContentSpec contentSpec,
             final String username) {
         final TextContentSpecProvider textContentSpecProvider = providerFactory.getProvider(TextContentSpecProvider.class);
         final TextCSProcessingOptionsWrapper processingOptions = textContentSpecProvider.newTextProcessingOptions();
         processingOptions.setPermissive(permissive);
 
         // Create the task to create the content spec on the server
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
                     output = textContentSpecProvider.createTextContentSpec(contentSpecEntity, processingOptions, logMessage);
                 } catch (ProviderException e) {
                     output = textContentSpecProvider.newTextContentSpec();
                     output.setErrors(e.getMessage());
                 }
 
                 return output;
             }
         });
 
         return ClientUtilities.saveContentSpec(this, task);
     }
 
     @Override
     public void shutdown() {
         super.shutdown();
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         // Never use the csprocessor.cfg for creating files
         return false;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
