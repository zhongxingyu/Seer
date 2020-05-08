 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
import com.redhat.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.utils.common.StringUtilities;
 
 @Parameters(commandDescription = "Search for a Content Specification")
 public class SearchCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[QUERY]")
     private List<String> queries = new ArrayList<String>();
 
     @Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
     private Boolean useContentSpec;
 
     @Parameter(names = {Constants.SNAPSHOT_LONG_PARAM, Constants.SNAPSHOT_SHORT_PARAM}, hidden = true)
     private Boolean useSnapshot = false;
 
     public SearchCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     public List<String> getQueries() {
         return queries;
     }
 
     public void setQueries(final List<String> queries) {
         this.queries = queries;
     }
 
     public Boolean isUseContentSpec() {
         return useContentSpec;
     }
 
     public void setUseContentSpec(final Boolean useContentSpec) {
         this.useContentSpec = useContentSpec;
     }
 
     public Boolean isUseSnapshot() {
         return useSnapshot;
     }
 
     public void setUseSnapshot(final Boolean useSnapshot) {
         this.useSnapshot = useSnapshot;
     }
 
     @Override
     public void printError(final String errorMsg, final boolean displayHelp) {
         printError(errorMsg, displayHelp, Constants.SEARCH_COMMAND_NAME);
     }
 
     @Override
     public void printHelp() {
         printHelp(Constants.SEARCH_COMMAND_NAME);
     }
 
     @Override
     public void process(final RESTManager restManager, final ErrorLoggerManager elm) {
         final List<RESTTopicV1> csList = new ArrayList<RESTTopicV1>();
         final String searchText = StringUtilities.buildString(queries.toArray(new String[queries.size()]), " ").toLowerCase();
 
         // Good point to check for a shutdown
         if (isAppShuttingDown()) {
             shutdown.set(true);
             return;
         }
 
         // Search the database for content specs that match the query parameters
         final List<RESTTopicV1> contentSpecs = restManager.getReader().getContentSpecs(null, null);
         if (contentSpecs != null) {
             for (final RESTTopicV1 contentSpec : contentSpecs) {
 
                 // Good point to check for a shutdown
                 if (isAppShuttingDown()) {
                     shutdown.set(true);
                     return;
                 }
 
                 final ContentSpecParser csp = new ContentSpecParser(elm, restManager);
                 try {
                     csp.parse(contentSpec.getXml());
                 } catch (Exception e) {
                     printError(Constants.ERROR_INTERNAL_ERROR, false);
                     shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
                 }
 
                 // Search on title
                 if (contentSpec.getTitle().toLowerCase().matches(".*" + searchText + ".*")) {
                     csList.add(contentSpec);
                 }
                 // Search on Product title
                 else if (csp.getContentSpec().getProduct().toLowerCase().matches(".*" + searchText + ".*")) {
                     csList.add(contentSpec);
                 }
                 // Search on Version
                else if (csp.getContentSpec().getVersion() != null && csp.getContentSpec().getVersion().toLowerCase().matches(".*" +
                        searchText + ".*")) {
                     csList.add(contentSpec);
                 }
                 // Search on created by
                 else if (ComponentTopicV1.returnProperty(contentSpec,
                         CSConstants.ADDED_BY_PROPERTY_TAG_ID) != null && ComponentTopicV1.returnProperty(contentSpec,
                         CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue() != null) {
                     if (ComponentTopicV1.returnProperty(contentSpec, CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue().toLowerCase().matches(
                             ".*" + searchText + ".*")) {
                         csList.add(contentSpec);
                     }
                 }
             }
         }
 
         // Good point to check for a shutdown
         if (isAppShuttingDown()) {
             shutdown.set(true);
             return;
         }
 
         // Display the search results
         if (csList.isEmpty()) {
             JCommander.getConsole().println(Constants.NO_CS_FOUND_MSG);
         } else {
             try {
                 JCommander.getConsole().println(
                         ClientUtilities.generateContentSpecListResponse(ClientUtilities.buildSpecList(csList, restManager, elm)));
             } catch (Exception e) {
                 printError(Constants.ERROR_INTERNAL_ERROR, false);
                 shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
             }
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         /*
         * Searching involves looking for a String so
 		 * there's no point in loading from the csprocessor.cfg
 		 */
         return false;
     }
 }
