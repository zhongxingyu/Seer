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
 import org.jboss.pressgang.ccms.contentspec.entities.RevisionList;
 import org.jboss.pressgang.ccms.contentspec.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
import org.jboss.pressgang.ccms.provider.exception.NotFoundException;
 
 @Parameters(commandDescription = "Get a list of revisions for a specified ID")
 public class RevisionsCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
     private Boolean contentSpec = false;
 
     @Parameter(names = {Constants.TOPIC_LONG_PARAM, Constants.TOPIC_SHORT_PARAM})
     private Boolean topic = false;
 
     public RevisionsCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.REVISIONS_COMMAND_NAME;
     }
 
     public Boolean isUseContentSpec() {
         return contentSpec;
     }
 
     public void setUseContentSpec(final Boolean useContentSpec) {
         this.contentSpec = useContentSpec;
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public Boolean isUseTopic() {
         return topic;
     }
 
     public void setUseTopic(final Boolean useTopic) {
         this.topic = useTopic;
     }
 
     public boolean isValid() {
         return !(contentSpec && topic);
     }
 
     @Override
     public void process() {
         // Initialise the ids and validate that some were passed
         ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds());
 
         // Check that the command is valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, Constants.INVALID_ARG_MSG, true);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Get the list of revisions
         RevisionList revisions = null;
        try {
            if (topic) {
                revisions = EntityUtilities.getTopicRevisionsById(getProviderFactory().getProvider(TopicProvider.class), getIds().get(0));
            } else {
                revisions = ContentSpecUtilities.getContentSpecRevisionsById(getProviderFactory().getProvider(ContentSpecProvider.class),
                        getIds().get(0));
            }
        } catch (NotFoundException e) {
            printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
         }
 
         // Check that the content spec is valid
         if (revisions == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Display the list
         JCommander.getConsole().println(revisions.toString());
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return getIds().size() == 0;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 
 }
