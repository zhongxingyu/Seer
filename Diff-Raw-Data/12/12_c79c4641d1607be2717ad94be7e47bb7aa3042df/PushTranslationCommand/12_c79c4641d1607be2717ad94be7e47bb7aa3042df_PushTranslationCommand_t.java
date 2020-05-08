 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
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
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.provider.CSTranslatedNodeProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.CSTranslatedNodeStringProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.contentspec.provider.TopicProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TranslatedTopicProvider;
 import org.jboss.pressgang.ccms.contentspec.structures.StringToCSNodeCollection;
 import org.jboss.pressgang.ccms.contentspec.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CSTranslatedNodeStringWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CSTranslatedNodeWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TranslatedTopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.utils.common.XMLUtilities;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 import org.jboss.pressgang.ccms.utils.structures.StringToNodeCollection;
 import org.jboss.pressgang.ccms.zanata.ZanataConstants;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 import org.jboss.pressgang.ccms.zanata.ZanataInterface;
 import org.w3c.dom.Document;
 import org.zanata.common.ContentType;
 import org.zanata.common.LocaleId;
 import org.zanata.common.ResourceType;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.TextFlow;
 
 @Parameters(commandDescription = "Push a Content Specification and it's topics to Zanata for translation.")
 public class PushTranslationCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = Constants.ZANATA_SERVER_LONG_PARAM,
             description = "The Zanata server to be associated with the Content Specification.")
     private String zanataUrl = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_LONG_PARAM,
             description = "The Zanata project name to be associated with the Content Specification.")
     private String zanataProject = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_VERSION_LONG_PARAM,
             description = "The Zanata project version to be associated with the Content Specification.")
     private String zanataVersion = null;
 
     @Parameter(names = Constants.TOPICS_ONLY_LONG_PARAM, description = "Only push the topics in the content specification to Zanata.")
     private Boolean topicsOnly = false;
 
     private ContentSpecProcessor csp;
 
     public PushTranslationCommand(final JCommander parser, final ContentSpecConfiguration cspConfig,
             final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.PUSH_TRANSLATION_COMMAND_NAME;
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public String getZanataUrl() {
         return zanataUrl;
     }
 
     public void setZanataUrl(final String zanataUrl) {
         this.zanataUrl = zanataUrl;
     }
 
     public String getZanataProject() {
         return zanataProject;
     }
 
     public void setZanataProject(final String zanataProject) {
         this.zanataProject = zanataProject;
     }
 
     public String getZanataVersion() {
         return zanataVersion;
     }
 
     public void setZanataVersion(final String zanataVersion) {
         this.zanataVersion = zanataVersion;
     }
 
     @Override
     public boolean validateServerUrl() {
         if (!super.validateServerUrl()) return false;
 
         final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
 
         // Print the zanata server url
         JCommander.getConsole().println(String.format(Constants.ZANATA_WEBSERVICE_MSG, zanataDetails.getServer()));
 
         // Test that the server address is valid
         if (!ClientUtilities.validateServerExists(zanataDetails.getServer())) {
             // Print a line to separate content
             JCommander.getConsole().println("");
 
             printErrorAndShutdown(Constants.EXIT_NO_SERVER, Constants.UNABLE_TO_FIND_SERVER_MSG, false);
         }
 
         return true;
     }
 
     /**
      * Sets the zanata options applied by the command line
      * to the options that were set via configuration files.
      */
     protected void setupZanataOptions() {
         // Set the zanata url
         if (this.zanataUrl != null) {
             // Find the zanata server if the url is a reference to the zanata server name
             for (final String serverName : getClientConfig().getZanataServers().keySet()) {
                 if (serverName.equals(zanataUrl)) {
                     zanataUrl = getClientConfig().getZanataServers().get(serverName).getUrl();
                     break;
                 }
             }
 
             getCspConfig().getZanataDetails().setServer(ClientUtilities.fixHostURL(zanataUrl));
         }
 
         // Set the zanata project
         if (this.zanataProject != null) {
             getCspConfig().getZanataDetails().setProject(zanataProject);
         }
 
         // Set the zanata version
         if (this.zanataVersion != null) {
             getCspConfig().getZanataDetails().setVersion(zanataVersion);
         }
     }
 
     protected boolean isValid() {
         setupZanataOptions();
         final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
 
         // Check that we even have some zanata details.
         if (zanataDetails == null) return false;
 
         // Check that none of the fields are invalid.
         if (zanataDetails.getServer() == null || zanataDetails.getServer().isEmpty() || zanataDetails.getProject() == null ||
                 zanataDetails.getProject().isEmpty() || zanataDetails.getVersion() == null || zanataDetails.getVersion().isEmpty() ||
                 zanataDetails.getToken() == null || zanataDetails.getToken().isEmpty() || zanataDetails.getUsername() == null ||
                 zanataDetails.getUsername().isEmpty()) {
             return false;
         }
 
         // At this point the zanata details are valid, so save the details.
         System.setProperty(ZanataConstants.ZANATA_SERVER_PROPERTY, zanataDetails.getServer());
         System.setProperty(ZanataConstants.ZANATA_PROJECT_PROPERTY, zanataDetails.getProject());
         System.setProperty(ZanataConstants.ZANATA_PROJECT_VERSION_PROPERTY, zanataDetails.getVersion());
         System.setProperty(ZanataConstants.ZANATA_USERNAME_PROPERTY, zanataDetails.getUsername());
         System.setProperty(ZanataConstants.ZANATA_TOKEN_PROPERTY, zanataDetails.getToken());
 
         return true;
     }
 
     @Override
     public void process() {
         // Authenticate the user
         final UserWrapper user = authenticate(getUsername(), getProviderFactory());
 
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         final TopicProvider topicProvider = getProviderFactory().getProvider(TopicProvider.class);
 
         // Load the ids and validate that one and only one exists
         ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds());
 
         // Check that the zanata details are valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_CONFIG_ERROR, Constants.ERROR_PUSH_NO_ZANATA_DETAILS_MSG, false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         final ContentSpecWrapper contentSpecEntity = contentSpecProvider.getContentSpec(ids.get(0), null);
 
         if (contentSpecEntity == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
         }
 
         // Transform the content spec
         final ContentSpec contentSpec = ClientUtilities.transformContentSpec(contentSpecEntity, getProviderFactory());
 
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setPermissiveMode(true);
         processingOptions.setValidating(true);
         processingOptions.setIgnoreChecksum(true);
         processingOptions.setAllowNewTopics(false);
 
         // Validate and parse the Content Specification
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         csp = new ContentSpecProcessor(getProviderFactory(), loggerManager, processingOptions);
         boolean success = csp.processContentSpec(contentSpec, user, ContentSpecParser.ParsingMode.EITHER);
 
         // Print the error/warning messages
         JCommander.getConsole().println(loggerManager.generateLogs());
 
         // Check that everything validated fine
         if (!success) {
             shutdown(Constants.EXIT_TOPIC_INVALID);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Create the list of referenced topics
         final List<Integer> referencedLatestTopicIds = new ArrayList<Integer>();
         final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
         for (final SpecTopic specTopic : specTopics) {
            if (specTopic.getDBId() != null && specTopic.getDBId() > 0 && specTopic.getRevision() == null) {
                 referencedLatestTopicIds.add(specTopic.getDBId());
             }
         }
 
         // Get the topics that were processed by the ContentSpecProcessor. (They should be stored in cache)
         CollectionWrapper<TopicWrapper> topics = topicProvider.getTopics(referencedLatestTopicIds);
 
         if (topics == null) {
             topics = topicProvider.newTopicCollection();
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Create the list of referenced revision topics
         final List<Pair<Integer, Integer>> referencedRevisionTopicIds = new ArrayList<Pair<Integer, Integer>>();
         for (final SpecTopic specTopic : specTopics) {
            if (specTopic.getDBId() != null && specTopic.getDBId() > 0 && specTopic.getRevision() != null) {
                 referencedRevisionTopicIds.add(new Pair<Integer, Integer>(specTopic.getDBId(), specTopic.getRevision()));
             }
         }
 
         // Get the revision topics that were processed by the ContentSpecProcessor. (They should be stored in cache)
         for (final Pair<Integer, Integer> referencedRevisionTopic : referencedRevisionTopicIds) {
             final TopicWrapper revisionTopic = topicProvider.getTopic(referencedRevisionTopic.getFirst(),
                     referencedRevisionTopic.getSecond());
             topics.addItem(revisionTopic);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (!pushToZanata(getProviderFactory(), topics, contentSpecEntity)) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_ZANATA_PUSH_FAILED_MSG, false);
             shutdown(Constants.EXIT_FAILURE);
         } else {
             JCommander.getConsole().println(Constants.SUCCESSFUL_ZANATA_PUSH_MSG);
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0;
     }
 
     /**
      * Pushes a content spec and its topics to zanata.
      *
      * @param providerFactory
      * @param topics
      * @param contentSpecEntity
      * @return True if the push was successful otherwise false.
      */
     protected boolean pushToZanata(final DataProviderFactory providerFactory, final CollectionWrapper<TopicWrapper> topics,
             final ContentSpecWrapper contentSpecEntity) {
         final Map<TopicWrapper, Document> topicToDoc = new HashMap<TopicWrapper, Document>();
         boolean error = false;
         final ZanataInterface zanataInterface = new ZanataInterface(0.2);
 
         // Convert all the topics to DOM Documents first so we know if any are invalid
         final List<TopicWrapper> topicItems = topics.getItems();
         for (final TopicWrapper topic : topicItems) {            /*
              * make sure the section title is the same as the
              * topic title
              */
             Document doc = null;
             try {
                 doc = XMLUtilities.convertStringToDocument(topic.getXml());
             } catch (Exception e) {
                 // Do Nothing as we handle the error below.
             }
 
             if (doc == null) {
                 JCommander.getConsole().println(
                         "ERROR: Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " does not have valid XML");
                 error = true;
             } else {
                 topicToDoc.put(topic, doc);
             }
 
             // Good point to check for a shutdown
             allowShutdownToContinueIfRequested();
         }
 
         // Return if creating the documents failed
         if (error) {
             return false;
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         final float total = topics.getItems().size() + (topicsOnly ? 0 : 1);
         float current = 0;
         final int showPercent = 5;
         int lastPercent = 0;
 
         JCommander.getConsole().println("You are about to push " + ((int) total) + " topics to zanata. Continue? (Yes/No)");
         String answer = JCommander.getConsole().readLine();
 
         final List<String> messages = new ArrayList<String>();
 
         if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
             JCommander.getConsole().println("Starting to push topics to zanata...");
 
             // Loop through each topic and upload it to zanata
             for (final Entry<TopicWrapper, Document> topicEntry : topicToDoc.entrySet()) {
                 ++current;
                 final int percent = Math.round(current / total * 100);
                 if (percent - lastPercent >= showPercent) {
                     lastPercent = percent;
                     JCommander.getConsole().println("\tPushing topics to zanata " + percent + "% Done");
                 }
 
                 final TopicWrapper topic = topicEntry.getKey();
                 final Document doc = topicEntry.getValue();
 
                 if (!pushTopicToZanata(providerFactory, topic, doc, zanataInterface, messages)) {
                     error = true;
                 }
             }
 
             // Upload the content specification to zanata
             if (!topicsOnly) {
                 if (!pushContentSpecToZanata(providerFactory, contentSpecEntity, zanataInterface, messages)) {
                     error = true;
                 }
             }
         }
 
         // Print the info/error messages
         if (messages.size() > 0) {
             JCommander.getConsole().println("Output:");
             for (final String message : messages) {
                 JCommander.getConsole().println("\t" + message);
             }
         }
 
         return !error;
     }
 
     /**
      * @param providerFactory
      * @param topic
      * @param doc
      * @param zanataInterface
      * @param messages
      * @return True if the topic was pushed successful otherwise false.
      */
     protected boolean pushTopicToZanata(final DataProviderFactory providerFactory, final TopicWrapper topic, final Document doc,
             final ZanataInterface zanataInterface, final List<String> messages) {
         boolean error = false;
 
         final TranslatedTopicProvider translatedTopicProvider = providerFactory.getProvider(TranslatedTopicProvider.class);
         final String zanataId = topic.getId() + "-" + topic.getRevision();
 
         final Resource zanataFile = zanataInterface.getZanataResource(zanataId);
 
         if (zanataFile == null) {
             final boolean translatedTopicExists = EntityUtilities.getTranslatedTopicByTopicId(providerFactory, topic.getId(),
                     topic.getRevision(), topic.getLocale()) != null;
 
             final Resource resource = new Resource();
 
             resource.setContentType(ContentType.TextPlain);
             resource.setLang(LocaleId.fromJavaName(topic.getLocale()));
             resource.setName(zanataId);
             resource.setRevision(1);
             resource.setType(ResourceType.FILE);
 
             final List<StringToNodeCollection> translatableStrings = XMLUtilities.getTranslatableStringsV2(doc, false);
 
             for (final StringToNodeCollection translatableStringData : translatableStrings) {
                 final String translatableString = translatableStringData.getTranslationString();
                 if (!translatableString.trim().isEmpty()) {
                     final TextFlow textFlow = new TextFlow();
                     textFlow.setContents(translatableString);
                     textFlow.setLang(LocaleId.fromJavaName(topic.getLocale()));
                     textFlow.setId(HashUtilities.generateMD5(translatableString));
                     textFlow.setRevision(1);
 
                     resource.getTextFlows().add(textFlow);
                 }
             }
 
             if (!zanataInterface.createFile(resource)) {
                 messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in Zanata.");
                 error = true;
             } else if (!translatedTopicExists) {
                 final TranslatedTopicWrapper translatedTopic = createTranslatedTopic(providerFactory, topic);
                 try {
                     if (translatedTopicProvider.createTranslatedTopic(translatedTopic) == null) {
                         messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in " +
                                 "PressGang.");
                         error = true;
                     }
                 } catch (Exception e) {
                             /*
                              * Do nothing here as it shouldn't fail. If it does then it'll be created
                              * by the sync service anyways.
                              */
                     messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in " +
                             "PressGang.");
                     error = true;
                 }
             }
         } else {
             messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " already exists - Skipping.");
         }
 
         return !error;
     }
 
     /**
      * @param providerFactory
      * @param contentSpecEntity
      * @param zanataInterface
      * @param messages
      * @return True if the content spec was pushed successful otherwise false.
      */
     protected boolean pushContentSpecToZanata(final DataProviderFactory providerFactory, final ContentSpecWrapper contentSpecEntity,
             final ZanataInterface zanataInterface, final List<String> messages) {
         boolean error = false;
 
         final CSTranslatedNodeProvider translatedNodeProvider = providerFactory.getProvider(CSTranslatedNodeProvider.class);
         final String zanataId = "CS" + contentSpecEntity.getId() + "-" + contentSpecEntity.getRevision();
         final Resource zanataFile = zanataInterface.getZanataResource(zanataId);
 
         if (zanataFile == null) {
             // TODO Fix this to use a content spec entity
             final boolean translatedContentSpecExists = EntityUtilities.getTranslatedTopicByTopicId(providerFactory,
                     contentSpecEntity.getId(), contentSpecEntity.getRevision(), contentSpecEntity.getLocale()) != null;
 
             final Resource resource = new Resource();
 
             resource.setContentType(ContentType.TextPlain);
             resource.setLang(LocaleId.fromJavaName(contentSpecEntity.getLocale()));
             resource.setName(zanataId);
             resource.setRevision(1);
             resource.setType(ResourceType.FILE);
 
             final List<StringToCSNodeCollection> translatableStrings = ContentSpecUtilities.getTranslatableStrings(contentSpecEntity,
                     false);
 
             for (final StringToCSNodeCollection translatableStringData : translatableStrings) {
                 final String translatableString = translatableStringData.getTranslationString();
                 if (!translatableString.trim().isEmpty()) {
                     final TextFlow textFlow = new TextFlow();
                     textFlow.setContents(translatableString);
                     textFlow.setLang(LocaleId.fromJavaName(contentSpecEntity.getLocale()));
                     textFlow.setId(HashUtilities.generateMD5(translatableString));
                     textFlow.setRevision(1);
 
                     resource.getTextFlows().add(textFlow);
                 }
             }
 
             if (!zanataInterface.createFile(resource)) {
                 messages.add("Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() + " " +
                         "failed to be created in Zanata.");
                 error = true;
             } else if (!translatedContentSpecExists) {
                 // Get all the nodes to be translated
                 final Set<CSNodeWrapper> nodes = new HashSet<CSNodeWrapper>();
                 for (final StringToCSNodeCollection translatableString : translatableStrings) {
                     nodes.addAll(translatableString.getNodeCollections());
                 }
 
                 // Save the translated nodes
                 final CollectionWrapper<CSTranslatedNodeWrapper> translatedNodes = createCSTranslatedNodes(providerFactory,
                         contentSpecEntity, nodes);
                 try {
                     if (translatedNodeProvider.createCSTranslatedNodes(translatedNodes) == null) {
                         messages.add("Content Spec ID " + contentSpecEntity.getId() + ", " +
                                 "Revision " + contentSpecEntity.getRevision() + " failed to be created in PressGang.");
                         error = true;
                     }
                 } catch (Exception e) {
                             /*
                              * Do nothing here as it shouldn't fail. If it does then it'll be created
                              * by the sync service anyways.
                              */
                     messages.add("Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() + " " +
                             "failed to be created in PressGang.");
                     error = true;
                 }
             }
         } else {
             messages.add("Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() + " already " +
                     "exists - Skipping.");
         }
 
         return !error;
     }
 
     /**
      * Create a TranslatedTopic based on the content from a normal Topic.
      *
      * @param topic The topic to transform to a TranslatedTopic
      * @return The new TranslatedTopic initialised with data from the topic.
      */
     protected TranslatedTopicWrapper createTranslatedTopic(final DataProviderFactory providerFactory, final TopicWrapper topic) {
         final TranslatedTopicWrapper translatedTopic = providerFactory.getProvider(TranslatedTopicProvider.class).newTranslatedTopic();
         translatedTopic.setLocale(topic.getLocale());
         translatedTopic.setTranslationPercentage(100);
         translatedTopic.setTopicId(topic.getId());
         translatedTopic.setTopicRevision(topic.getRevision());
         translatedTopic.setXml(topic.getXml());
         translatedTopic.setHtml(topic.getHtml());
         translatedTopic.setHtmlUpdated(new Date());
         return translatedTopic;
     }
 
     /**
      * Creates a list of Content Spec Translated Node entities from a list of Content Spec node entities.
      *
      * @param providerFactory The factory to produce entity providers to lookup entity details.
      * @param contentSpec     The content spec object the nodes belong to.
      * @param nodes           The nodes to create the translated nodes for.
      * @return
      */
     protected CollectionWrapper<CSTranslatedNodeWrapper> createCSTranslatedNodes(final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpec, final Collection<CSNodeWrapper> nodes) {
         final CollectionWrapper<CSTranslatedNodeWrapper> translatedNodes = providerFactory.getProvider(
                 CSTranslatedNodeProvider.class).newCSTranslatedNodeCollection();
         for (final CSNodeWrapper node : nodes) {
             translatedNodes.addNewItem(createCSTranslatedNode(providerFactory, contentSpec, node));
         }
 
         return translatedNodes;
     }
 
     /**
      * Creates a Translated Content Spec Node based on an original Content Spec Node.
      *
      * @param providerFactory The factory to produce entity providers to lookup entity details.
      * @param contentSpec     The content spec the nodes belong to.
      * @param node            The node to create a translated node for.
      * @return A CSTranslatedNode entity that contains the information from the original node.
      */
     protected CSTranslatedNodeWrapper createCSTranslatedNode(final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpec, final CSNodeWrapper node) {
         final CSTranslatedNodeWrapper translatedNode = providerFactory.getProvider(CSTranslatedNodeProvider.class).newCSTranslatedNode();
         translatedNode.setNodeId(node.getId());
         translatedNode.setNodeRevision(node.getRevision());
 
         final CSTranslatedNodeStringProvider translatedNodeStringProvider = providerFactory.getProvider(
                 CSTranslatedNodeStringProvider.class);
         final UpdateableCollectionWrapper<CSTranslatedNodeStringWrapper> translatedStrings = translatedNodeStringProvider
                 .newCSTranslatedNodeStringCollection(
                 translatedNode);
 
         final CSTranslatedNodeStringWrapper translatedNodeString = translatedNodeStringProvider.newCSTranslatedNodeString(translatedNode);
         translatedNodeString.setLocale(contentSpec.getLocale());
         translatedNodeString.setOriginalString(node.getAdditionalText());
         translatedNodeString.setTranslatedString(node.getAdditionalText());
         translatedNodeString.setFuzzy(false);
         translatedStrings.addNewItem(translatedNodeString);
 
         translatedNode.setTranslatedStrings(translatedStrings);
 
         return translatedNode;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
