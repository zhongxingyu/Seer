 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTReader;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.services.zanatasync.SyncMaster;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 import org.jboss.pressgang.ccms.zanata.ZanataConstants;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 import org.jboss.pressgang.ccms.zanata.ZanataInterface;
 import org.zanata.common.LocaleId;
 
 @Parameters(commandDescription = "Sync the translations for a Content Specification with Zanata")
 public class SyncTranslationCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[IDs]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = Constants.LOCALES_LONG_PARAM, metaVar = "[LOCALES]",
             description = "The locales to sync for the specified IDs.")
     private String locales = "";
 
     @Parameter(names = Constants.ZANATA_SERVER_LONG_PARAM,
             description = "The zanata server to be associated with the Content Specification.")
     private String zanataUrl = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_LONG_PARAM,
             description = "The zanata project name to be associated with the Content Specification.")
     private String zanataProject = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_VERSION_LONG_PARAM,
             description = "The zanata project version to be associated with the Content Specification.")
     private String zanataVersion = null;
 
     public SyncTranslationCommand(JCommander parser, ContentSpecConfiguration cspConfig, ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public void printHelp() {
         printHelp(Constants.SYNC_TRANSLATION_COMMAND_NAME);
     }
 
     @Override
     public void printError(final String errorMsg, final boolean displayHelp) {
         printError(errorMsg, displayHelp, Constants.SYNC_TRANSLATION_COMMAND_NAME);
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(List<Integer> ids) {
         this.ids = ids;
     }
 
     public String getLocales() {
         return locales;
     }
 
     public void setLocales(String locales) {
         this.locales = locales;
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
     public void process(RESTManager restManager, ErrorLoggerManager elm) {
         // Load the data from the config data if no ids were specified
         if (loadFromCSProcessorCfg()) {
             // Check that the config details are valid
             if (cspConfig != null && cspConfig.getContentSpecId() != null) {
                 setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
             }
         }
 
         // Check that only one ID exists
         if (ids.size() == 0) {
             printError(Constants.ERROR_NO_ID_MSG, false);
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         }
 
         // Check that at least one locale has been specified
         if (locales.trim().length() == 0) {
             printError(Constants.ERROR_NO_LOCALES_MSG, false);
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         }
 
         // Good point to check for a shutdown
         if (isAppShuttingDown()) {
             shutdown.set(true);
             return;
         }
 
         // Check that the zanata details are valid
         if (!isValid()) {
             printError(Constants.ERROR_PUSH_NO_ZANATA_DETAILS_MSG, false);
             shutdown(Constants.EXIT_CONFIG_ERROR);
         }
 
         final ZanataInterface zanataInterface = initialiseZanataInterface(restManager);
         final SyncMaster syncMaster = new SyncMaster(getServerUrl(), restManager.getRESTClient(), zanataInterface);
 
         // Good point to check for a shutdown
         if (isAppShuttingDown()) {
             shutdown.set(true);
             return;
         }
 
         // Process the ids
         final Set<String> zanataIds = getZanataIds(restManager, ids);
         JCommander.getConsole().println("Syncing the topics...");
         syncMaster.processZanataResources(zanataIds);
     }
 
     protected boolean isValid() {
         final ZanataDetails zanataDetails = cspConfig.getZanataDetails();
 
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
 
     /**
      * Sets the zanata options applied by the command line to the options that were set via configuration files.
      */
     protected void setupZanataOptions() {
         // Set the zanata url
         if (this.zanataUrl != null) {
             // Find the zanata server if the url is a reference to the zanata server name
             for (final String serverName : clientConfig.getZanataServers().keySet()) {
                 if (serverName.equals(zanataUrl)) {
                     zanataUrl = clientConfig.getZanataServers().get(serverName).getUrl();
                     break;
                 }
             }
 
             cspConfig.getZanataDetails().setServer(ClientUtilities.validateHost(zanataUrl));
         }
 
         // Set the zanata project
         if (this.zanataProject != null) {
             cspConfig.getZanataDetails().setProject(zanataProject);
         }
 
         // Set the zanata version
         if (this.zanataVersion != null) {
             cspConfig.getZanataDetails().setVersion(zanataVersion);
         }
     }
 
     /**
      * Initialise the Zanata Interface and setup it's locales.
      *
      * @return The initialised Zanata Interface.
      */
     protected ZanataInterface initialiseZanataInterface(final RESTManager restManager) {
         final ZanataInterface zanataInterface = new ZanataInterface(0.2);
 
         final List<LocaleId> localeIds = new ArrayList<LocaleId>();
         final String[] splitLocales = locales.split(",");
 
         // Check to make sure the locales are valid
         if (!ClientUtilities.validateLanguages(this, restManager, splitLocales)) {
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         }
 
         for (final String locale : splitLocales) {
             // Covert the language into a LocaleId
             localeIds.add(LocaleId.fromJavaName(locale));
         }
 
         zanataInterface.getLocaleManager().setLocales(localeIds);
 
         return zanataInterface;
     }
 
     /**
      * Get the Zanata IDs to be synced from a list of content specifications.
      *
      * @param restManager    The REST Manager to lookup entities from the REST API.
      * @param contentSpecIds The list of Content Spec IDs to sync.
      * @return A Set of Zanata IDs that represent the topics to be synced from the list of Content Specs.
      */
     protected Set<String> getZanataIds(final RESTManager restManager, final List<Integer> contentSpecIds) {
         JCommander.getConsole().println("Downloading topics...");
         final ContentSpecParser parser = new ContentSpecParser(new ErrorLoggerManager(), restManager);
         final RESTReader restReader = restManager.getReader();
 
         final RESTTopicCollectionV1 topics = new RESTTopicCollectionV1();
         for (final Integer contentSpecId : contentSpecIds) {
             final RESTTopicV1 contentspec = restReader.getTopicById(contentSpecId, null, true);
 
             if (contentspec == null) {
                 printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
                 shutdown(Constants.EXIT_ARGUMENT_ERROR);
             }
 
             try {
                 parser.parse(contentspec.getXml());
             } catch (Exception e) {
                 printError("Content Spec " + contentSpecId + " is not valid!", false);
                 shutdown(Constants.EXIT_FAILURE);
             }
 
             // Create the query to get the latest topics
             RESTTopicCollectionV1 tempTopics = null;
             final List<Integer> topicIds = parser.getReferencedLatestTopicIds();
             if (topicIds != null && !topicIds.isEmpty()) {
                 // Get the latest topics
                tempTopics = restReader.getTopicsByIds(topicIds, false);
             }
 
             // Copy the topics to the primary topic list
             if (tempTopics != null && tempTopics.getItems() != null) {
                 for (final RESTTopicV1 topic : tempTopics.returnItems()) {
                     topics.addItem(topic);
                 }
             }
 
             // Get the revision topics
             final List<Pair<Integer, Integer>> revTopicIds = parser.getReferencedRevisionTopicIds();
             for (final Pair<Integer, Integer> revTopicId : revTopicIds) {
                 final RESTTopicV1 revTopic = restReader.getTopicById(revTopicId.getFirst(), revTopicId.getSecond(), true);
                 topics.addItem(revTopic);
             }
 
             // Add the content spec itself to be deleted
             topics.addItem(contentspec);
         }
 
         return getZanataIds(topics);
     }
 
     /**
      * Get the Zanata IDs that represent a Collection of Topics.
      *
      * @param topics The topics to get the Zanata IDs for.
      * @return The Set of Zanata IDs that represent the topics.
      */
     protected Set<String> getZanataIds(final RESTTopicCollectionV1 topics) {
         final Set<String> zanataIds = new HashSet<String>();
 
         final List<RESTTopicV1> topicList = topics.returnItems();
         for (final RESTTopicV1 topic : topicList) {
             // Find the latest pushed translated topic
             final RESTTranslatedTopicV1 translatedTopic = ComponentTopicV1.returnPushedTranslatedTopic(topic);
             if (translatedTopic != null) {
                 zanataIds.add(ComponentTranslatedTopicV1.returnZanataId(translatedTopic));
             }
         }
 
         return zanataIds;
     }
 
     @Override
     public void validateServerUrl() {
         // Print the server url
         JCommander.getConsole().println(String.format(Constants.WEBSERVICE_MSG, getServerUrl()));
 
         // Test that the server address is valid
         if (!ClientUtilities.validateServerExists(getPressGangServerUrl())) {
             // Print a line to separate content
             JCommander.getConsole().println("");
 
             printError(Constants.UNABLE_TO_FIND_SERVER_MSG, false);
             shutdown(Constants.EXIT_NO_SERVER);
         }
 
         setupZanataOptions();
         final ZanataDetails zanataDetails = cspConfig.getZanataDetails();
 
         // Print the zanata server url
         JCommander.getConsole().println(String.format(Constants.ZANATA_WEBSERVICE_MSG, zanataDetails.getServer()));
 
         // Test that the server address is valid
         if (!ClientUtilities.validateServerExists(zanataDetails.getServer())) {
             // Print a line to separate content
             JCommander.getConsole().println("");
 
             printError(Constants.UNABLE_TO_FIND_SERVER_MSG, false);
             shutdown(Constants.EXIT_NO_SERVER);
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0;
     }
 }
