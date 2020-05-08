 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.entities.SpecList;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 
@Parameters(resourceBundle = "commands", commandDescription = "LIST")
 public class ListCommand extends BaseCommandImpl {
     @Parameter(names = {Constants.FORCE_LONG_PARAM, Constants.FORCE_SHORT_PARAM}, hidden = true)
     private Boolean force = false;
 
     @Parameter(names = Constants.LIMIT_LONG_PARAM, metaVar = "<NUM>", descriptionKey = "LIST_LIMIT")
     private Integer limit = null;
 
     public ListCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.LIST_COMMAND_NAME;
     }
 
     public Boolean isForce() {
         return force;
     }
 
     public void setForce(final Boolean force) {
         this.force = force;
     }
 
     public Integer getLimit() {
         return limit;
     }
 
     public void setLimit(final Integer limit) {
         this.limit = limit;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
 
         // Get the content specs from the database
         final CollectionWrapper<ContentSpecWrapper> contentSpecs = contentSpecProvider.getContentSpecsWithQuery("query;");
         int noSpecs = contentSpecs.size();
 
         // Get the number of results to display
         final Integer limit = getLimit() == null ? Constants.MAX_LIST_RESULT : (getLimit() > 0 ? getLimit() : 0);
         final Integer numResults;
         if (limit > noSpecs) {
             numResults = noSpecs;
         } else if (!isForce()) {
             numResults = limit;
         } else {
             numResults = noSpecs;
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // If there are too many content specs & force isn't set then send back an error message
         if (noSpecs > Constants.MAX_LIST_RESULT && getLimit() == null && !isForce()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_LIST_TOO_MANY_MSG", noSpecs, Constants.LIMIT_LONG_PARAM), false);
         } else if (noSpecs == 0) {
             JCommander.getConsole().println(getMessage("NO_CS_FOUND_MSG"));
         } else {
             // Get the sublist of results to display
             final List<ContentSpecWrapper> csList = contentSpecs.getItems().subList(0, numResults);
 
             // Good point to check for a shutdown
             allowShutdownToContinueIfRequested();
 
             // Generate and then print the list of content specs
             final SpecList specList = ClientUtilities.buildSpecList(csList, getProviderFactory());
             JCommander.getConsole().println(ClientUtilities.generateContentSpecList(specList));
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         /*
          * We shouldn't use a csprocessor.cfg file for the
          * list URL as it should be specified or read from the
          * csprocessor.ini
          */
         return false;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
