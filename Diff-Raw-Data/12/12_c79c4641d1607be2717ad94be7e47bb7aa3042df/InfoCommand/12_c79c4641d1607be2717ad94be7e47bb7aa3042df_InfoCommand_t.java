 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TopicProvider;
 import org.jboss.pressgang.ccms.contentspec.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.CollectionWrapper;
 
 @Parameters(commandDescription = "Get some basic information and metrics about a project.")
 public class InfoCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     public InfoCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     public String getCommandName() {
         return Constants.INFO_COMMAND_NAME;
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         final TopicProvider topicProvider = getProviderFactory().getProvider(TopicProvider.class);
 
         // Initialise the basic data and perform basic checks
         ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Get the Content Specification from the server.
         final ContentSpecWrapper contentSpecEntity = contentSpecProvider.getContentSpec(ids.get(0), null);
         if (contentSpecEntity == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
         }
 
         // Print the initial CSP ID & Title message
         JCommander.getConsole().println(String.format(Constants.CSP_ID_MSG, ids.get(0)));
         JCommander.getConsole().println(String.format(Constants.CSP_REVISION_MSG, contentSpecEntity.getRevision()));
         JCommander.getConsole().println(String.format(Constants.CSP_TITLE_MSG, contentSpecEntity.getTitle()));
         JCommander.getConsole().println("");
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         JCommander.getConsole().println("Starting to calculate the statistics...");
 
         // Transform the content spec
         final ContentSpec contentSpec = ClientUtilities.transformContentSpec(contentSpecEntity, getProviderFactory());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Create the list of referenced topics
         final List<Integer> referencedTopicIds = new ArrayList<Integer>();
         final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
         for (final SpecTopic specTopic : specTopics) {
            if (specTopic.getDBId() != null && specTopic.getDBId() > 0) {
                 referencedTopicIds.add(specTopic.getDBId());
             }
         }
 
         // Calculate the percentage complete
         int numTopics = referencedTopicIds.size();
         int numTopicsComplete = calculateNumTopicsComplete(topicProvider, referencedTopicIds);
 
         // Print the completion status
         JCommander.getConsole().println(String.format(Constants.CSP_COMPLETION_MSG, numTopics, numTopicsComplete,
                 ((float) numTopicsComplete / (float) numTopics * 100.0f)));
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0;
     }
 
     /**
      * Calculate the number of topics that have been written (or in other words has at least some content stored in the topic XML).
      *
      * @param topicProvider The Provider that supplies topics from an external datasource.
      * @param topicIds      A list of topic ids to get stats for.
      * @return The number of completed topics from the list of ids provided.
      */
     protected Integer calculateNumTopicsComplete(final TopicProvider topicProvider, final List<Integer> topicIds) {
         int numTopicsComplete = 0;
         final CollectionWrapper<TopicWrapper> topics = topicProvider.getTopics(topicIds);
         if (topics != null && topics.getItems() != null) {
             final List<TopicWrapper> topicItems = topics.getItems();
             for (final TopicWrapper topic : topicItems) {
                 if (topic.getXml() != null && !topic.getXml().isEmpty()) {
                     numTopicsComplete++;
                 }
 
                 // Good point to check for a shutdown
                 allowShutdownToContinueIfRequested();
             }
         }
 
         return numTopicsComplete;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
