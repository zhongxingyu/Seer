 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.contentspec.processor.ContentSpecProcessor;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTReader;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 
 @Parameters(commandDescription = "Pull a revision of a content specification that represents a snapshot in time.")
 public class PullSnapshotCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = {Constants.REVISION_LONG_PARAM, Constants.REVISION_SHORT_PARAM})
     private Integer revision = null;
 
     @Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM},
             description = "Save the output to the specified file/directory.", metaVar = "<FILE>")
     private String outputPath;
 
    @Parameter(names = {Constants.UPDATE_LONG_PARAM}, description = "Update all current revisions when pulling down the snapshot.")
     private Boolean update = false;
 
     private ContentSpecProcessor csp = null;
 
     public PullSnapshotCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public Integer getRevision() {
         return revision;
     }
 
     public void setRevision(final Integer revision) {
         this.revision = revision;
     }
 
     public String getOutputPath() {
         return outputPath;
     }
 
     public void setOutputPath(final String outputPath) {
         this.outputPath = outputPath;
     }
 
     public Boolean getUpdate() {
         return update;
     }
 
     public void setUpdate(final Boolean update) {
         this.update = update;
     }
 
     @Override
     public void printError(final String errorMsg, final boolean displayHelp) {
         printError(errorMsg, displayHelp, Constants.PULL_SNAPSHOT_COMMAND_NAME);
     }
 
     @Override
     public void printHelp() {
         printHelp(Constants.PULL_SNAPSHOT_COMMAND_NAME);
     }
 
     @Override
     public void process(final RESTManager restManager, final ErrorLoggerManager elm) {
         final RESTReader reader = restManager.getReader();
         final boolean pullForConfig = loadFromCSProcessorCfg();
 
         // If files is empty then we must be using a csprocessor.cfg file
         if (pullForConfig) {
             // Check that the config details are valid
             if (cspConfig != null && cspConfig.getContentSpecId() != null) {
                 ids.add(cspConfig.getContentSpecId());
             }
         }
 
         // Check that only one ID exists
         if (ids.size() == 0) {
             printError(Constants.ERROR_NO_ID_MSG, false);
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         } else if (ids.size() > 1) {
             printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         }
 
         // Good point to check for a shutdown
         if (isAppShuttingDown()) {
             shutdown.set(true);
             return;
         }
 
         boolean success = false;
 
         // Get the topic from the rest interface
         final RESTTopicV1 contentSpec = reader.getPostContentSpecById(ids.get(0), revision);
         if (contentSpec == null) {
             printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
             shutdown(Constants.EXIT_FAILURE);
         }
 
         // Good point to check for a shutdown
         if (isAppShuttingDown()) {
             shutdown.set(true);
             return;
         }
 
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setPermissiveMode(true);
         processingOptions.setValidating(true);
         processingOptions.setAllowEmptyLevels(true);
         processingOptions.setAddRevisions(true);
         processingOptions.setUpdateRevisions(update);
         processingOptions.setIgnoreChecksum(true);
         processingOptions.setRevision(revision);
 
         // Process the content spec to make sure the spec is valid,
         csp = new ContentSpecProcessor(restManager, elm, processingOptions);
         try {
             success = csp.processContentSpec(contentSpec.getXml(), getUsername(), ContentSpecParser.ParsingMode.EITHER);
         } catch (Exception e) {
             e.printStackTrace();
             printError(Constants.ERROR_INTERNAL_ERROR, false);
             shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
         }
 
         if (!success) {
             JCommander.getConsole().println(elm.generateLogs());
             JCommander.getConsole().println(Constants.ERROR_PULL_SNAPSHOT_INVALID);
             JCommander.getConsole().println("");
             shutdown(Constants.EXIT_TOPIC_INVALID);
         }
 
         if (pullForConfig) {
             outputPath = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals(
                     "") ? "" : (cspConfig.getRootOutputDirectory() + DocBookUtilities.escapeTitle(
                     contentSpec.getTitle()) + File.separator)) + Constants.DEFAULT_SNAPSHOT_LOCATION + File.separator;
         }
 
         // Save or print the data
         final DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
         final String data = csp.getContentSpec().toString();
         final String fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-snapshot-" + dateFormatter.format(
                 contentSpec.getLastModified()) + "." + Constants.FILENAME_EXTENSION;
         if (outputPath == null) {
             JCommander.getConsole().println(data);
         } else {
             // Create the output file
             File output;
             outputPath = ClientUtilities.validateFilePath(outputPath);
             if (outputPath != null && outputPath.endsWith(File.separator)) {
                 output = new File(outputPath + fileName);
             } else if (outputPath == null || outputPath.equals("")) {
                 output = new File(fileName);
             } else {
                 output = new File(outputPath);
             }
 
             // Make sure the directories exist
             if (output.isDirectory()) {
                 output.mkdirs();
                 output = new File(output.getAbsolutePath() + File.separator + fileName);
             } else {
                 if (output.getParentFile() != null) output.getParentFile().mkdirs();
             }
 
             // Good point to check for a shutdown
             if (isAppShuttingDown()) {
                 shutdown.set(true);
                 return;
             }
 
             // If the file exists then create a backup file
             if (output.exists()) {
                 output.renameTo(new File(output.getAbsolutePath() + ".backup"));
             }
 
             // Create and write to the file
             try {
                 final FileOutputStream fos = new FileOutputStream(output);
                 fos.write(data.getBytes("UTF-8"));
                 fos.flush();
                 fos.close();
                 JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getName()));
             } catch (IOException e) {
                 printError(Constants.ERROR_FAILED_SAVING, false);
                 shutdown(Constants.EXIT_FAILURE);
             }
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0;
     }
 }
