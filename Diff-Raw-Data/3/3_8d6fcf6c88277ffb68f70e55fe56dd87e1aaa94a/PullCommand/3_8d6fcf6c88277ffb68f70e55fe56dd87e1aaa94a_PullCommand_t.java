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
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 
 @Parameters(commandDescription = "Pull a Content Specification from the server")
 public class PullCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
     private Boolean pullContentSpec = false;
 
     @Parameter(names = {Constants.TOPIC_LONG_PARAM, Constants.TOPIC_SHORT_PARAM})
     private Boolean pullTopic = false;
 
     @Parameter(names = {Constants.REVISION_LONG_PARAM, Constants.REVISION_SHORT_PARAM})
     private Integer revision;
 
     @Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM},
             description = "Save the output to the specified file/directory.", metaVar = "<FILE>")
     private String outputPath;
 
     public PullCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.PULL_COMMAND_NAME;
     }
 
     public Boolean useContentSpec() {
         return pullContentSpec;
     }
 
     public void setContentSpec(final Boolean contentSpec) {
         pullContentSpec = contentSpec;
     }
 
     public Boolean useTopic() {
         return pullTopic;
     }
 
     public void setTopic(final Boolean topic) {
         pullTopic = topic;
     }
 
     public Integer getRevision() {
         return revision;
     }
 
     public void setRevision(final Integer revision) {
         this.revision = revision;
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public String getOutputPath() {
         return outputPath;
     }
 
     public void setOutputPath(final String outputPath) {
         this.outputPath = outputPath;
     }
 
     protected boolean isValid() {
         return !(useContentSpec() && useTopic());
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         boolean pullForConfig = false;
 
         // Initialise the basic data and perform basic input checks
         if (ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds())) {
             pullForConfig = true;
             setRevision(null);
         }
 
         // Check that the additional options are valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, Constants.INVALID_ARG_MSG, true);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         String outputString = "";
         String fileName = "";
         // Topic
         if (useTopic()) {
             final TopicWrapper topic = getProviderFactory().getProvider(TopicProvider.class).getTopic(getIds().get(0), getRevision());
             if (topic == null) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE,
                         getRevision() == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
             } else {
                 outputString = topic.getXml();
                 fileName = DocBookUtilities.escapeTitle(topic.getTitle()) + ".xml";
             }
             // Content Specification
         } else {
             final ContentSpecWrapper contentSpecEntity = contentSpecProvider.getContentSpec(getIds().get(0), getRevision());
             final String contentSpecString = contentSpecProvider.getContentSpecAsString(getIds().get(0), getRevision());
             if (contentSpecEntity == null || contentSpecString == null) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE,
                         getRevision() == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
             } else {
                 outputString = contentSpecString;
 
                 // Calculate the filenames and output directory.
                 if (getRevision() == null) {
                     fileName = DocBookUtilities.escapeTitle(contentSpecEntity.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
                 } else {
                     fileName = DocBookUtilities.escapeTitle(
                            contentSpecEntity.getTitle()) + "-post-r" + contentSpecEntity.getRevision() + "." + Constants
                            .FILENAME_EXTENSION;
                 }
                 if (pullForConfig) {
                     setOutputPath(ClientUtilities.getOutputRootDirectory(getCspConfig(), contentSpecEntity));
                 }
             }
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Save or print the data
         if (getOutputPath() == null) {
             JCommander.getConsole().println(outputString);
         } else {
             ClientUtilities.saveOutputFile(this, fileName, getOutputPath(), outputString);
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0 && !pullTopic;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
