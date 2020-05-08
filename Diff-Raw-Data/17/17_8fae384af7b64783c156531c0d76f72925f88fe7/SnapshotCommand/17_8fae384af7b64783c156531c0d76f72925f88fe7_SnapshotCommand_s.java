 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
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
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 
 @Parameters(
         commandDescription = "Pull a revision of a content specification that represents a snapshot in time and push it back to the " +
                 "server.")
 public class SnapshotCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = {Constants.REVISION_LONG_PARAM, Constants.REVISION_SHORT_PARAM})
     private Integer revision = null;
 
    @Parameter(names = {Constants.UPDATE_LONG_PARAM}, description = "Update all current revisions when pulling down the snapshot.",
            hidden = true)
     private Boolean update = false;
 
     @Parameter(names = {Constants.NEW_LONG_PARAM}, description = "Create the snapshot as a new content specification")
     private Boolean createNew = false;
 
     @Parameter(names = {Constants.MESSAGE_LONG_PARAM, Constants.MESSAGE_SHORT_PARAM}, description = "A commit message about what was " +
             "changed.")
     private String message = null;
 
     @Parameter(names = Constants.REVISION_MESSAGE_FLAG_LONG_PARAMETER, description = "The commit message should be set to be included in " +
             "the Revision History.")
     private Boolean revisionHistoryMessage = false;
 
     private ContentSpecProcessor csp = null;
 
     public SnapshotCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
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
 
     public Boolean getUpdate() {
         return update;
     }
 
     public void setUpdate(final Boolean update) {
         this.update = update;
     }
 
     public Boolean getCreateNew() {
         return createNew;
     }
 
     public void setCreateNew(Boolean createNew) {
         this.createNew = createNew;
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
 
         final String fixedContentSpec;
 
         // If we want to create it as a new spec then remove the checksum and id
         if (createNew) {
             fixedContentSpec = contentSpec.getXml().replaceAll("CHECKSUM\\s*=.*?(\r)?\nID\\s*=.*?(\r)?\n", "");
         } else {
             fixedContentSpec = contentSpec.getXml();
         }
 
         // Create the log details
         RESTLogDetailsV1 logDetails = ClientUtilities.createLogDetails(message, revisionHistoryMessage);
 
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setPermissiveMode(true);
         processingOptions.setValidating(false);
         processingOptions.setAllowEmptyLevels(true);
         processingOptions.setAddRevisions(true);
         processingOptions.setUpdateRevisions(update);
         processingOptions.setIgnoreChecksum(true);
         processingOptions.setRevision(revision);
 
         // Process the content spec to make sure the spec is valid,
         csp = new ContentSpecProcessor(restManager, elm, processingOptions);
         Integer revision = null;
         try {
             if (createNew) {
                 success = csp.processContentSpec(fixedContentSpec, getUsername(), logDetails, ContentSpecParser.ParsingMode.NEW);
             } else {
                 success = csp.processContentSpec(fixedContentSpec, getUsername(), logDetails, ContentSpecParser.ParsingMode.EDITED);
             }
             if (success) {
                 revision = restManager.getReader().getLatestCSRevById(csp.getContentSpec().getId());
             }
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
         } else {
             JCommander.getConsole().println(Constants.SUCCESSFUL_PUSH_SNAPSHOT_MSG);
             JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_PUSH_MSG, csp.getContentSpec().getId(), revision));
         }
 
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0;
     }
 }
