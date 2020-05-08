 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.beust.jcommander.DynamicParameter;
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.converters.CommaParameterSplitter;
 import com.beust.jcommander.internal.Maps;
 import com.google.common.collect.Lists;
 import com.redhat.j2koji.exceptions.KojiException;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.builder.BuildType;
 import org.jboss.pressgang.ccms.contentspec.builder.ContentSpecBuilder;
 import org.jboss.pressgang.ccms.contentspec.builder.exception.BuildProcessingException;
 import org.jboss.pressgang.ccms.contentspec.builder.exception.BuilderCreationException;
 import org.jboss.pressgang.ccms.contentspec.builder.structures.CSDocbookBuildingOptions;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.converter.BuildTypeConverter;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.client.validator.BuildTypeValidator;
 import org.jboss.pressgang.ccms.contentspec.client.validator.OverrideValidator;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.utils.CSTransformer;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 @Parameters(commandDescription = "Build a Content Specification from the server")
 public class BuildCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID] or [FILE]")
     private List<String> ids = new ArrayList<String>();
 
     @Parameter(names = Constants.HIDE_ERRORS_LONG_PARAM, description = "Hide the errors in the output.")
     private Boolean hideErrors = false;
 
     @Parameter(names = Constants.SHOW_CONTENT_SPEC_LONG_PARAM, description = "Show the content spec page in the output.")
     private Boolean hideContentSpec = false;
 
     @Parameter(names = Constants.INLINE_INJECTION_LONG_PARAM, description = "Stop injections from being processed when building.")
     private Boolean inlineInjection = true;
 
     @Parameter(names = Constants.INJECTION_TYPES_LONG_PARAM, splitter = CommaParameterSplitter.class, metaVar = "[arg1[,arg2,...]]",
             description = "Specify certain topic types that injection should be processed on.")
     private List<String> injectionTypes;
 
     @Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
     private Boolean executionTime = false;
 
     @DynamicParameter(names = Constants.OVERRIDE_LONG_PARAM, metaVar = "<variable>=<value>", validateWith = OverrideValidator.class)
     private Map<String, String> overrides = Maps.newHashMap();
 
     @DynamicParameter(names = Constants.PUBLICAN_CFG_OVERRIDE_LONG_PARAM, metaVar = "<parameter>=<value>")
     private Map<String, String> publicanCfgOverrides = Maps.newHashMap();
 
     @Parameter(names = Constants.BUG_REPORTING_LONG_PARM, description = "Hide the bug reporting links in the output.")
     private Boolean hideBugLinks = false;
 
     @Parameter(names = Constants.FORCE_BUG_REPORTING_LONG_PARM, description = "Forcibly show the bug reporting links in the output.",
             hidden = true)
     private Boolean forceBugLinks = false;
 
     @Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM},
             description = "Save the output to the specified file/directory.", metaVar = "<FILE>")
     private String outputPath;
 
     @Parameter(names = Constants.EMPTY_LEVELS_LONG_PARAM, description = "Allow building with empty levels.", hidden = true)
     private Boolean allowEmptyLevels = false;
 
     @Parameter(names = Constants.EDITOR_LINKS_LONG_PARAM, description = "Insert Editor links for each topic.")
     private Boolean insertEditorLinks = false;
 
     @Parameter(names = Constants.LOCALE_LONG_PARAM, description = "What locale to build the content spec for.", metaVar = "<LOCALE>")
     private String locale = null;
 
     @Parameter(names = Constants.FETCH_PUBSNUM_LONG_PARAM, description = "Fetch the pubsnumber directly from " + Constants.KOJI_NAME + ".")
     protected Boolean fetchPubsnum = false;
 
     @Parameter(names = Constants.SHOW_REPORT_LONG_PARAM, description = "Show the Report chapter in the output.")
     protected Boolean showReport = false;
 
     @Parameter(names = Constants.ZANATA_SERVER_LONG_PARAM,
             description = "The zanata server to be associated with the Content Specification.")
     private String zanataUrl = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_LONG_PARAM,
             description = "The zanata project name to be associated with the Content Specification.")
     private String zanataProject = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_VERSION_LONG_PARAM,
             description = "The zanata project version to be associated with the Content Specification.")
     private String zanataVersion = null;
 
     @Parameter(names = Constants.TARGET_LANG_LONG_PARAM,
             description = "The output target locale if it is different from the --lang option.", metaVar = "<LOCALE>")
     private String targetLocale = null;
 
     @Parameter(names = {Constants.REVISION_LONG_PARAM, Constants.REVISION_SHORT_PARAM})
     private Integer revision = null;
 
     @Parameter(names = {Constants.UPDATE_LONG_PARAM}, description = "Update all current revisions, to the latest version when building.",
             hidden = true)
     private Boolean useLatestVersions = false;
 
     @Parameter(names = {Constants.DRAFT_LONG_PARAM, Constants.DRAFT_SHORT_PARAM}, description = "Build the book as a draft.")
     private Boolean draft = false;
 
     @Parameter(names = Constants.SHOW_REMARKS_LONG_PARAM, description = "Build the book with remarks visible.")
     private Boolean showRemarks = false;
 
     @Parameter(names = Constants.REV_MESSAGE_LONG_PARAM, description = "Add a message for the revision history.")
     private List<String> messages = Lists.newArrayList();
 
     @Parameter(names = {Constants.FLATTEN_TOPICS_LONG_PARAM},
             description = "Flatten the topics folder, so no subdirectories exist when building.")
     private Boolean flattenTopics = false;
 
     @Parameter(names = {Constants.YES_LONG_PARAM, Constants.YES_SHORT_PARAM},
             description = "Automatically answer \"yes\" to any questions.")
     private Boolean answerYes = false;
 
     @Parameter(names = Constants.FLATTEN_LONG_PARAM, description = "Flatten the topics so only chapter/appendix/part files exist.")
     private Boolean flatten = false;
 
     @Parameter(names = Constants.FORMAT_LONG_PARAM, description = "What format to build the content spec in.", metaVar = "<FORMAT>",
             converter = BuildTypeConverter.class, validateWith = BuildTypeValidator.class)
     private BuildType buildType = null;
 
     private ContentSpecProcessor csp = null;
     private ContentSpecBuilder builder = null;
 
     public BuildCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     protected ContentSpecProcessor getCsp() {
         return csp;
     }
 
     protected void setCsp(ContentSpecProcessor csp) {
         this.csp = csp;
     }
 
     protected ContentSpecBuilder getBuilder() {
         return builder;
     }
 
     protected void setBuilder(ContentSpecBuilder builder) {
         this.builder = builder;
     }
 
     @Override
     public String getCommandName() {
         return Constants.BUILD_COMMAND_NAME;
     }
 
     public List<String> getInjectionTypes() {
         return injectionTypes;
     }
 
     public void setInjectionTypes(final List<String> injectionTypes) {
         this.injectionTypes = injectionTypes;
     }
 
     public Boolean getInlineInjection() {
         return inlineInjection;
     }
 
     public void setInlineInjection(final Boolean inlineInjection) {
         this.inlineInjection = inlineInjection;
     }
 
     public Boolean getHideErrors() {
         return hideErrors;
     }
 
     public void setHideErrors(final Boolean hideErrors) {
         this.hideErrors = hideErrors;
     }
 
     public Boolean getHideContentSpecPage() {
         return hideContentSpec;
     }
 
     public void setHideContentSpecPage(final Boolean hideContentSpecPage) {
         this.hideContentSpec = hideContentSpecPage;
     }
 
     public List<String> getIds() {
         return ids;
     }
 
     public void setIds(final List<String> ids) {
         this.ids = ids;
     }
 
     public Boolean getExecutionTime() {
         return executionTime;
     }
 
     public void setExecutionTime(final Boolean executionTime) {
         this.executionTime = executionTime;
     }
 
     public Map<String, String> getOverrides() {
         return overrides;
     }
 
     public void setOverrides(final Map<String, String> overrides) {
         this.overrides = overrides;
     }
 
     public Map<String, String> getPublicanCfgOverrides() {
         return publicanCfgOverrides;
     }
 
     public void setPublicanCfgOverrides(final Map<String, String> publicanCfgOverrides) {
         this.publicanCfgOverrides = publicanCfgOverrides;
     }
 
     public String getOutputPath() {
         return outputPath;
     }
 
     public void setOutputPath(final String outputPath) {
         this.outputPath = outputPath;
     }
 
     public Boolean getAllowEmptyLevels() {
         return allowEmptyLevels;
     }
 
     public void setAllowEmptyLevels(final Boolean allowEmptyLevels) {
         this.allowEmptyLevels = allowEmptyLevels;
     }
 
     public Boolean getInsertEditorLinks() {
         return insertEditorLinks;
     }
 
     public void setInsertEditorLinks(final Boolean insertEditorLinks) {
         this.insertEditorLinks = insertEditorLinks;
     }
 
     public Boolean getForceBugLinks() {
         return forceBugLinks;
     }
 
     public void setForceBugLinks(final Boolean forceBugLinks) {
         this.forceBugLinks = forceBugLinks;
     }
 
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(final String locale) {
         this.locale = locale;
     }
 
     public Boolean getFetchPubsnum() {
         return fetchPubsnum;
     }
 
     public void setFetchPubsnum(final Boolean fetchPubsnum) {
         this.fetchPubsnum = fetchPubsnum;
     }
 
     public Boolean getShowReport() {
         return showReport;
     }
 
     public void setShowReport(final Boolean showReport) {
         this.showReport = showReport;
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
 
     public String getTargetLocale() {
         return targetLocale;
     }
 
     public void setTargetLocale(final String targetLocale) {
         this.targetLocale = targetLocale;
     }
 
     public Integer getRevision() {
         return revision;
     }
 
     public void setRevision(final Integer revision) {
         this.revision = revision;
     }
 
     public Boolean getUseLatestVersions() {
         return useLatestVersions;
     }
 
     public void setUseLatestVersions(final Boolean useLatestVersion) {
         useLatestVersions = useLatestVersion;
     }
 
     public Boolean getDraft() {
         return draft;
     }
 
     public void setDraft(final Boolean draft) {
         this.draft = draft;
     }
 
     public Boolean getShowRemarks() {
         return showRemarks;
     }
 
     public void setShowRemarks(final Boolean showRemarks) {
         this.showRemarks = showRemarks;
     }
 
     public List<String> getMessage() {
         return messages;
     }
 
     public void setMessage(final List<String> messages) {
         this.messages = messages;
     }
 
     public Boolean getFlattenTopics() {
         return flattenTopics;
     }
 
     public void setFlattenTopics(Boolean flattenTopics) {
         this.flattenTopics = flattenTopics;
     }
 
     public Boolean getHideBugLinks() {
         return hideBugLinks;
     }
 
     public void setHideBugLinks(Boolean hideBugLinks) {
         this.hideBugLinks = hideBugLinks;
     }
 
     public Boolean getAnswerYes() {
         return answerYes;
     }
 
     public void setAnswerYes(Boolean answerYes) {
         this.answerYes = answerYes;
     }
 
     public Boolean getFlatten() {
         return flatten;
     }
 
     public void setFlatten(Boolean flatten) {
         this.flatten = flatten;
     }
 
     public BuildType getBuildType() {
         return buildType;
     }
 
     public void setBuildType(BuildType buildType) {
         this.buildType = buildType;
     }
 
     @Override
     public void process() {
         final long startTime = System.currentTimeMillis();
 
         // Add the details for the csprocessor.cfg if no ids are specified and then validate input
         boolean buildingFromConfig = ClientUtilities.prepareAndValidateStringIds(this, getCspConfig(), getIds());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Check to make sure the lang is valid
         if (getLocale() != null && !ClientUtilities.validateLanguage(this, getProviderFactory(), getLocale())) {
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         }
 
         // Check the passed publican.cfg overrides
         validatePublicanCfgOverride();
 
         // Get the content spec and make sure it exists
         final ContentSpec contentSpec = getContentSpec(getIds().get(0));
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Validate that the content spec is valid
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         boolean success = validateContentSpec(getProviderFactory(), loggerManager, getUsername(), contentSpec);
 
         // Print the error/warning messages
         JCommander.getConsole().println(loggerManager.generateLogs());
 
         // Check that everything validated fine
         if (!success) {
             shutdown(Constants.EXIT_TOPIC_INVALID);
         }
 
         // Pull in the pubsnumber from koji if the option is set
         if (getFetchPubsnum()) {
             final Integer pubsnumber = getContentSpecPubsNumberFromKoji(contentSpec);
             if (pubsnumber != null) {
                 contentSpec.setPubsNumber(pubsnumber);
             }
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         JCommander.getConsole().println(Constants.STARTING_BUILD_MSG);
 
         // Setup the zanata details incase some were overridden via the command line
         setupZanataOptions();
 
         // Build the Content Specification
         byte[] builderOutput = buildContentSpec(contentSpec, getUsername());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Print the success messages
         long elapsedTime = System.currentTimeMillis() - startTime;
         JCommander.getConsole().println(String.format(Constants.ZIP_SAVED_ERRORS_MSG, getBuilder().getNumErrors(),
                 getBuilder().getNumWarnings()) + (getBuilder().getNumErrors() == 0 && getBuilder().getNumWarnings() == 0 ? " - Flawless "
                 + "Victory!" : ""));
         if (getExecutionTime()) {
             JCommander.getConsole().println(String.format(Constants.EXEC_TIME_MSG, elapsedTime));
         }
 
         // Get the filename for the spec, using it's title.
         String fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle());
 
         // Create the output file
         String outputDir = "";
         if (buildingFromConfig) {
             outputDir = ClientUtilities.getOutputRootDirectory(getCspConfig(), contentSpec) + Constants.DEFAULT_CONFIG_ZIP_LOCATION;
             if (getBuildType() == BuildType.JDOCBOOK) {
                 fileName += Constants.DEFAULT_CONFIG_JDOCBOOK_BUILD_POSTFIX;
             } else {
                 fileName += Constants.DEFAULT_CONFIG_PUBLICAN_BUILD_POSTFIX;
             }
             setOutputPath(null);
         }
         fileName += ".zip";
 
         // Create the output file based on the command line params and content spec
         final File outputFile = getOutputFile(outputDir, fileName);
 
         // Make sure the directories exist
         if (outputFile.isDirectory() && !outputFile.exists()) {
             // TODO ensure that the directory is created
             outputFile.mkdirs();
         } else if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
             // TODO ensure that the directory is created
             outputFile.getParentFile().mkdirs();
         }
 
         // Save the build output to the output file
         saveBuildToFile(builderOutput, outputFile, buildingFromConfig);
     }
 
     /**
      * Get the options that should be used when building.
      *
      * @return The Object that holds all the options used when building.
      */
     protected CSDocbookBuildingOptions getBuildOptions() {
         // Fix up the values for overrides so file names are expanded
         fixOverrides();
 
         final CSDocbookBuildingOptions buildOptions = new CSDocbookBuildingOptions();
         buildOptions.setInjection(getInlineInjection());
         buildOptions.setInjectionTypes(getInjectionTypes());
         buildOptions.setIgnoreMissingCustomInjections(getHideErrors());
         buildOptions.setSuppressErrorsPage(getHideErrors());
         buildOptions.setInsertBugLinks(!getHideBugLinks());
         buildOptions.setOverrides(getOverrides());
         buildOptions.setPublicanCfgOverrides(getPublicanCfgOverrides());
         buildOptions.setSuppressContentSpecPage(!getHideContentSpecPage());
         buildOptions.setInsertEditorLinks(getInsertEditorLinks());
         buildOptions.setShowReportPage(getShowReport());
         buildOptions.setLocale(getLocale());
         buildOptions.setCommonContentDirectory(getClientConfig().getPublicanCommonContentDirectory());
         buildOptions.setOutputLocale(getTargetLocale());
         buildOptions.setDraft(getDraft());
         buildOptions.setPublicanShowRemarks(getShowRemarks());
         buildOptions.setRevisionMessages(getMessage());
         buildOptions.setUseLatestVersions(getUseLatestVersions());
         buildOptions.setFlattenTopics(getFlattenTopics());
         buildOptions.setForceInjectBugLinks(getForceBugLinks());
         buildOptions.setCommonContentDirectory(getClientConfig().getPublicanCommonContentDirectory());
         buildOptions.setFlatten(getFlatten());
 
         return buildOptions;
     }
 
     /**
      * Gets the override files from the file system and create a byte array of each file.
      *
      * @return The map of Override File to their actual data.
      */
     protected Map<String, byte[]> getOverrideFiles() {
         final Map<String, byte[]> overrideFiles = new HashMap<String, byte[]>();
         for (final Entry<String, String> override : getOverrides().entrySet()) {
             final String overrideName = override.getKey();
             if (overrideName.equals(CSConstants.AUTHOR_GROUP_OVERRIDE) || overrideName.equals(
                     CSConstants.REVISION_HISTORY_OVERRIDE) || overrideName.equals(CSConstants.FEEDBACK_OVERRIDE)) {
                 final File file = new File(override.getValue());
                 overrideFiles.put(overrideName, FileUtilities.readFileContentsAsByteArray(file));
             }
         }
 
         return overrideFiles;
     }
 
     /**
      * Gets the content spec from a file and parses it into a content spec object
      *
      * @param file             The file to load and read the content from.
      * @param processProcesses If the processes should be processed fully during parsing
      * @return A String representation of the file,
      */
     protected ContentSpec getContentSpecFromFile(final String file, boolean processProcesses) {
         // Get the content spec from the file
         String contentSpecString = FileUtilities.readFileContents(new File(ClientUtilities.fixFilePath(file)));
 
         if (contentSpecString.equals("")) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_EMPTY_FILE_MSG, false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Parse the content spec
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         JCommander.getConsole().println("Starting to parse...");
         final ContentSpec contentSpec = ClientUtilities.parseContentSpecString(getProviderFactory(), loggerManager, contentSpecString,
                 ContentSpecParser.ParsingMode.EITHER, processProcesses);
 
         // Check that that content specification was parsed successfully
         if (contentSpec == null) {
             JCommander.getConsole().println(loggerManager.generateLogs());
             JCommander.getConsole().println(ProcessorConstants.ERROR_INVALID_CS_MSG);
             shutdown(Constants.EXIT_TOPIC_INVALID);
         }
 
         return contentSpec;
     }
 
     /**
      * Sets the zanata options applied by the command line
      * to the options that were set via configuration files.
      */
     protected void setupZanataOptions() {
         // Set the zanata url
         if (getZanataUrl() != null) {
             // Find the zanata server if the url is a reference to the zanata server name
             for (final String serverName : getClientConfig().getZanataServers().keySet()) {
                 if (serverName.equals(getZanataUrl())) {
                     setZanataUrl(getClientConfig().getZanataServers().get(serverName).getUrl());
                     break;
                 }
             }
 
             getCspConfig().getZanataDetails().setServer(ClientUtilities.fixHostURL(getZanataUrl()));
         }
 
         // Set the zanata project
         if (getZanataProject() != null) {
             getCspConfig().getZanataDetails().setProject(getZanataProject());
         }
 
         // Set the zanata version
         if (getZanataVersion() != null) {
             getCspConfig().getZanataDetails().setVersion(getZanataVersion());
         }
     }
 
     /**
      * Gets the Pubsnumber for a Content Specification from Koji/Brew, using the content spec product/version/edition to perform the
      * search.
      *
      * @param contentSpec The content spec to look up the pubsnumber for.
      * @return The pubsnumber for the content spec if it could be found, otherwise null.
      */
     protected Integer getContentSpecPubsNumberFromKoji(final ContentSpec contentSpec) {
         JCommander.getConsole().println(Constants.FETCHING_PUBSNUMBER_MSG);
 
         try {
             return ClientUtilities.getPubsnumberFromKoji(contentSpec, getCspConfig().getKojiHubUrl());
         } catch (MalformedURLException e) {
             printErrorAndShutdown(Constants.EXIT_CONFIG_ERROR, Constants.ERROR_INVALID_KOJIHUB_URL, false);
         } catch (KojiException e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_FAILED_FETCH_PUBSNUM, false);
         }
 
         return null;
     }
 
     /**
      * Builds a ContentSpec object into a ZIP archive that can be later built by Publican.
      *
      * @param contentSpec The content spec to build from.
      * @param user        The user who requested the build.
      * @return A ZIP archive as a byte array that is ready to be saved as a file.
      */
     protected byte[] buildContentSpec(final ContentSpec contentSpec, final String username) {
         byte[] builderOutput = null;
         try {
             setBuilder(new ContentSpecBuilder(getProviderFactory()));
             BuildType buildType = getBuildType() == null ? BuildType.PUBLICAN : getBuildType();
             if (getLocale() == null) {
                 builderOutput = getBuilder().buildBook(contentSpec, username, getBuildOptions(), getOverrideFiles(), buildType);
             } else {
                 builderOutput = getBuilder().buildTranslatedBook(contentSpec, username, getBuildOptions(), getOverrideFiles(),
                         getCspConfig().getZanataDetails(), buildType);
             }
         } catch (BuildProcessingException e) {
             printErrorAndShutdown(Constants.EXIT_INTERNAL_SERVER_ERROR, Constants.ERROR_INTERNAL_ERROR, false);
         } catch (BuilderCreationException e) {
             printErrorAndShutdown(Constants.EXIT_INTERNAL_SERVER_ERROR, Constants.ERROR_INTERNAL_ERROR, false);
         }
 
         return builderOutput;
     }
 
     /**
      * Validates that a content specification object and it's contents are valid.
      *
      * @param providerFactory The provider factory that can create providers to lookup various entities from a datasource.
      * @param loggerManager   The logging manager that handles output.
      * @param user            The user who requested the build/validation.
      * @param contentSpec     The content spec object to be validated.
      * @return True if the content spec is valid, otherwise false.
      */
     protected boolean validateContentSpec(final DataProviderFactory providerFactory, final ErrorLoggerManager loggerManager,
             final String username, final ContentSpec contentSpec) {
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setValidating(true);
         processingOptions.setIgnoreChecksum(true);
         processingOptions.setAllowNewTopics(false);
        processingOptions.setStrictBugLinks(true);
         if (getAllowEmptyLevels()) {
             processingOptions.setAllowEmptyLevels(true);
         }
 
         // Validate the Content Specification
         setCsp(new ContentSpecProcessor(providerFactory, loggerManager, processingOptions));
         return getCsp().processContentSpec(contentSpec, username, ContentSpecParser.ParsingMode.EITHER, getLocale());
     }
 
     /**
      * Gets a ContentSpec from either an ID or File path.
      *
      * @param fileOrId The File or Id string to use to get the content spec.
      * @return The content spec object if it could be found, otherwise null.
      */
     protected ContentSpec getContentSpec(final String fileOrId) {
         // Get the Content Spec either from file or from the REST API
         final ContentSpec contentSpec;
         if (fileOrId.matches("^\\d+$")) {
             final Integer id = Integer.parseInt(fileOrId);
             final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
 
             // Get the Content Spec from the server. If the locale is set then find the closest translated spec and load it from there.
             final ContentSpecWrapper contentSpecEntity;
             if (getLocale() != null) {
                 final TranslatedContentSpecWrapper translatedContentSpec = EntityUtilities.getClosestTranslatedContentSpecById
                         (getProviderFactory(), id, getRevision());
                 if (translatedContentSpec != null) {
                     contentSpecEntity = translatedContentSpec.getContentSpec();
                 } else {
                     contentSpecEntity = contentSpecProvider.getContentSpec(id, getRevision());
                 }
             } else {
                 contentSpecEntity = contentSpecProvider.getContentSpec(id, getRevision());
             }
 
             // Check that the content spec entity exists.
             if (contentSpecEntity == null) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
             }
 
             contentSpec = CSTransformer.transform(contentSpecEntity, getProviderFactory());
         } else {
             // Get the content spec from the file
             contentSpec = getContentSpecFromFile(fileOrId, true);
         }
 
         return contentSpec;
     }
 
     /**
      * Generates the output file object using the command
      * line parameters and the calculated output directory
      * and filename from the content specification.
      *
      * @param outputDir The output directory calculated from the content spec.
      * @param fileName  The file name calculated from the content spec.
      * @return The file that
      */
     protected File getOutputFile(final String outputDir, final String fileName) {
         final String output;
         final String fixedOutputDir = ClientUtilities.fixDirectoryPath(outputDir);
         // Create the fully qualified output path
         if (getOutputPath() != null && (getOutputPath().endsWith(File.separator) || new File(getOutputPath()).isDirectory())) {
             output = ClientUtilities.fixDirectoryPath(getOutputPath()) + fixedOutputDir + fileName;
         } else if (getOutputPath() == null) {
             output = fixedOutputDir + fileName;
         } else {
             output = getOutputPath();
         }
 
         return new File(ClientUtilities.fixFilePath(output));
     }
 
     /**
      * Fixes the Override variables that point to File locations to change relative paths to absolute paths.
      */
     protected void fixOverrides() {
         final Map<String, String> overrides = getOverrides();
         for (final Entry<String, String> entry : overrides.entrySet()) {
             final String key = entry.getKey();
             if (key.equals(CSConstants.AUTHOR_GROUP_OVERRIDE) || key.equals(CSConstants.REVISION_HISTORY_OVERRIDE) || key.equals(
                     CSConstants.FEEDBACK_OVERRIDE)) {
                 overrides.put(key, ClientUtilities.fixFilePath(entry.getValue()));
             }
         }
     }
 
     /**
      * Saves the build output to a file location.
      *
      * @param buildZip           The File Contents to be saved.
      * @param outputFile         The location/name of the output file.
      * @param buildingFromConfig If the build was built from a csprocessor.cfg config file or by an id.
      */
     protected void saveBuildToFile(final byte[] buildZip, final File outputFile, final boolean buildingFromConfig) {
         String answer = "y";
         /*
          * Check if the file exists, if we aren't building from Project directory with a csprocessor.cfg file. If it does then check if the
          * file should be overwritten.
          */
         if (!buildingFromConfig && outputFile.exists() && !getAnswerYes()) {
             JCommander.getConsole().println(String.format(Constants.FILE_EXISTS_OVERWRITE_MSG, outputFile.getName()));
             answer = JCommander.getConsole().readLine();
             while (!(answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase(
                     "yes") || answer.equalsIgnoreCase("no"))) {
                 JCommander.getConsole().print(String.format(Constants.FILE_EXISTS_OVERWRITE_MSG, outputFile.getName()));
                 answer = JCommander.getConsole().readLine();
 
                 // Check if the app is shutting down and if so let it.
                 allowShutdownToContinueIfRequested();
             }
         }
 
         // Save the book to file
         try {
             if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                 FileUtilities.saveFile(outputFile, buildZip);
                 JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, outputFile.getAbsolutePath()));
             } else {
                 shutdown(Constants.EXIT_FAILURE);
             }
         } catch (IOException e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_FAILED_SAVING, false);
         }
     }
 
     /**
      * Validates the passed publican.cfg overrides to check that they are valid.
      */
     protected void validatePublicanCfgOverride() {
         for (final Entry<String, String> overrideEntry : getPublicanCfgOverrides().entrySet()) {
             if (!CSConstants.PUBLICAN_CFG_PARAMETERS.contains(overrideEntry.getKey())) {
                 printWarn(String.format(Constants.WARN_UNKNOWN_PUBLICAN_CFG_OVERRIDE, overrideEntry.getKey()));
             }
         }
     }
 
     @Override
     public void shutdown() {
         // No need to wait as the ShutdownInterceptor is waiting
         // on the whole program.
         if (getCsp() != null) {
             getCsp().shutdown();
         }
 
         if (getBuilder() != null) {
             getBuilder().shutdown();
         }
 
         super.shutdown();
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return getIds().size() == 0;
     }
 
     @Override
     public boolean validateServerUrl() {
         if (!super.validateServerUrl()) return false;
 
         /*
          * Check the KojiHub server url to ensure that it exists
          * if the user wants to fetch the pubsnumber from koji.
          */
         if (getFetchPubsnum()) {
             // Print the kojihub server url
             JCommander.getConsole().println(String.format(Constants.KOJI_WEBSERVICE_MSG, getCspConfig().getKojiHubUrl()));
 
             // Test that the server address is valid
             if (!ClientUtilities.validateServerExists(getCspConfig().getKojiHubUrl())) {
                 // Print a line to separate content
                 JCommander.getConsole().println("");
 
                 printErrorAndShutdown(Constants.EXIT_NO_SERVER, Constants.UNABLE_TO_FIND_SERVER_MSG, false);
             }
         }
 
         /*
          * Check the Zanata server url and Project/Version to ensure that it
          * exists if the user wants to insert editor links for translations.
          */
         if (getInsertEditorLinks() && getLocale() != null) {
             setupZanataOptions();
 
             final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
             if (!ClientUtilities.validateServerExists(zanataDetails.returnUrl())) {
                 // Print a line to separate content
                 JCommander.getConsole().println("");
 
                 printErrorAndShutdown(Constants.EXIT_NO_SERVER,
                         String.format(Constants.ERROR_INVALID_ZANATA_CONFIG_MSG, zanataDetails.getProject(), zanataDetails.getVersion(),
                                 zanataDetails.getServer()), false);
             }
         }
 
         return true;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
