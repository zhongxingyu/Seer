 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.builder.BuildType;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.utils.common.ZipUtilities;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 
 @Parameters(commandDescription = "Builds and Assembles a Content Specification so that it is ready to be previewed")
 public class AssembleCommand extends BuildCommand {
 
     @Parameter(names = Constants.NO_BUILD_LONG_PARAM, description = "Don't build the Content Specification.")
     private Boolean noBuild = false;
 
     @Parameter(names = Constants.HIDE_OUTPUT_LONG_PARAM, description = "Hide the output from assembling the Content Specification.")
     private Boolean hideOutput = false;
 
     @Parameter(names = Constants.NO_PUBLICAN_BUILD_LONG_PARAM,
             description = "Don't build the Content Specification after unzipping.",
             hidden = true)
     private Boolean noPublicanBuild = false;
 
     private String buildFileDirectory = "";
     String buildFileName = null;
     String outputDirectory = "";
 
     public AssembleCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     protected String getBuildFileDirectory() {
         return buildFileDirectory;
     }
 
     protected void setBuildFileDirectory(String buildFileDirectory) {
         this.buildFileDirectory = buildFileDirectory;
     }
 
     protected String getBuildFileName() {
         return buildFileName;
     }
 
     protected void setBuildFileName(String buildFileName) {
         this.buildFileName = buildFileName;
     }
 
     protected String getOutputDirectory() {
         return outputDirectory;
     }
 
     protected void setOutputDirectory(String outputDirectory) {
         this.outputDirectory = outputDirectory;
     }
 
     @Override
     public String getCommandName() {
         return Constants.ASSEMBLE_COMMAND_NAME;
     }
 
     public Boolean getNoBuild() {
         return noBuild;
     }
 
     public void setNoBuild(final Boolean noBuild) {
         this.noBuild = noBuild;
     }
 
     public Boolean getHideOutput() {
         return hideOutput;
     }
 
     public void setHideOutput(Boolean hideOutput) {
         this.hideOutput = hideOutput;
     }
 
     public Boolean isNoPublicanBuild() {
         return noPublicanBuild;
     }
 
     public void setNoPublicanBuild(Boolean noPublicanBuild) {
         this.noPublicanBuild = noPublicanBuild;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
 
         // Validate that only one id or file was entered
        final boolean assembleFromConfig = ClientUtilities.prepareAndValidateStringIds(this, getCspConfig(), getIds());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (!getNoBuild()) {
             super.process();
         }
 
         JCommander.getConsole().println(Constants.STARTING_ASSEMBLE_MSG);
 
         // Find the build directory and required files
         findBuildDirectoryAndFiles(contentSpecProvider, assembleFromConfig);
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         final File buildFile = new File(ClientUtilities.fixDirectoryPath(getBuildFileDirectory()) + getBuildFileName());
         if (!buildFile.exists()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, String.format(Constants.ERROR_UNABLE_TO_FIND_ZIP_MSG, getBuildFileName()), false);
         }
 
         // Make sure the output directories exist
         final File buildOutputDirectory = new File(ClientUtilities.fixDirectoryPath(getOutputDirectory()));
         buildOutputDirectory.mkdirs();
 
         // Ensure that the directory is empty
         FileUtilities.deleteDirContents(buildOutputDirectory);
 
         // Unzip the file
         if (!ZipUtilities.unzipFileIntoDirectory(buildFile, getOutputDirectory())) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_FAILED_TO_ASSEMBLE_MSG, false);
         } else {
             JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_UNZIP_MSG, buildOutputDirectory.getAbsolutePath()));
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Run publican to assemble the book into the output format(s)
         if (!isNoPublicanBuild()) {
             if (getBuildType() == BuildType.JDOCBOOK) {
                 runMaven(buildOutputDirectory);
             } else {
                 runPublican(buildOutputDirectory);
             }
             JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_ASSEMBLE_MSG, buildOutputDirectory.getAbsolutePath()));
         }
     }
 
     /**
      * Find the Build Directory and output files.
      *
      * @param contentSpecProvider The Content Spec provider that can be used to get information about a content spec.
      * @param assembleFromConfig  Whether or not the command is assembling from a csprocessor.cfg directory or not.
      */
     protected void findBuildDirectoryAndFiles(final ContentSpecProvider contentSpecProvider, boolean assembleFromConfig) {
         boolean assembleFromId = true;
         Integer id = null;
         // Find if we are assembling from a file or ID
         if (!assembleFromConfig) {
             assert getIds() != null && getIds().size() == 1;
 
             if (!getIds().get(0).matches("^\\d+")) {
                 assembleFromId = false;
             } else {
                 id = Integer.parseInt(getIds().get(0));
             }
         } else {
             id = getCspConfig().getContentSpecId();
         }
 
         if (assembleFromId) {
             final ContentSpecWrapper contentSpec = contentSpecProvider.getContentSpec(id, null);
 
             // Check that that content specification was found
             if (contentSpec == null) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
             }
 
             // Check that the content spec has a valid version
             if (contentSpec.getChildren() == null || contentSpec.getChildren().isEmpty()) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_VALID_CONTENT_SPEC, false);
             }
 
             if (assembleFromConfig) {
                 /*
                  * If we are assembling from a CSP Project directory then we need to get the location of the ZIP and the assembly directory
                  * relative to the current working directory, or the CSP root project directory.
                  */
                 final String rootDir = ClientUtilities.getOutputRootDirectory(getCspConfig(), contentSpec);
 
                 setBuildFileDirectory(rootDir + Constants.DEFAULT_CONFIG_ZIP_LOCATION);
                 if (getBuildType() == BuildType.JDOCBOOK) {
                     setOutputDirectory(rootDir + Constants.DEFAULT_CONFIG_JDOCBOOK_LOCATION);
                     setBuildFileName(
                             DocBookUtilities.escapeTitle(contentSpec.getTitle()) + Constants.DEFAULT_CONFIG_JDOCBOOK_BUILD_POSTFIX +
                                     ".zip");
                 } else {
                     setOutputDirectory(rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION);
                     setBuildFileName(
                             DocBookUtilities.escapeTitle(contentSpec.getTitle()) + Constants.DEFAULT_CONFIG_PUBLICAN_BUILD_POSTFIX +
                                     ".zip");
                 }
             } else {
                 // Find the build directories and files from the content spec
                 findBuildDirectoryAndFiles(contentSpec.getTitle());
             }
         } else {
             // Parse the spec from a file to get the main details
             final ContentSpec contentSpec = getContentSpecFromFile(getIds().get(0), false);
 
             // Find the build directories and files from the content spec
             findBuildDirectoryAndFiles(contentSpec.getTitle());
         }
     }
 
     /**
      * Find the Build Directory and output files for a content spec using it's title
      *
      * @param contentSpecTitle The content specs title.
      */
     private void findBuildDirectoryAndFiles(final String contentSpecTitle) {
         // Create the fully qualified output path
         if (getOutputPath() != null && (getOutputPath().endsWith(File.separator) || new File(getOutputPath()).isDirectory())) {
             setBuildFileDirectory(ClientUtilities.fixDirectoryPath(getOutputPath()));
             setBuildFileName(DocBookUtilities.escapeTitle(contentSpecTitle) + ".zip");
         } else if (getOutputPath() == null) {
             setBuildFileName(DocBookUtilities.escapeTitle(contentSpecTitle) + ".zip");
         } else {
             setBuildFileName(getOutputPath());
         }
 
         // Add the full file path to the output path
         final File file = new File(ClientUtilities.fixFilePath(getBuildFileDirectory() + getBuildFileName()));
         if (file.getParent() != null) {
             setOutputDirectory(file.getParent() + File.separator + DocBookUtilities.escapeTitle(contentSpecTitle));
         } else {
             setOutputDirectory(DocBookUtilities.escapeTitle(contentSpecTitle));
         }
     }
 
     /**
      * Run Publican to assemble the book from Docbook XML markup to the required format.
      *
      * @param publicanFilesDirectory The directory location that hosts the publican files.
      */
     protected void runPublican(final File publicanFilesDirectory) {
         String publicanOptions = getClientConfig().getPublicanBuildOptions();
 
         // Replace the locale in the build options if the locale has been set
         if (getTargetLocale() != null) {
             publicanOptions = publicanOptions.replaceAll("--lang(s)?(=| )[A-Za-z\\-,]+", "--langs=" + getTargetLocale());
         } else if (getLocale() != null) {
             publicanOptions = publicanOptions.replaceAll("--lang(s)?(=| )[A-Za-z\\-,]+", "--langs=" + getLocale());
         }
 
         try {
             JCommander.getConsole().println(Constants.STARTING_PUBLICAN_BUILD_MSG);
             final Integer exitValue = ClientUtilities.runCommand("publican build " + publicanOptions, publicanFilesDirectory,
                     JCommander.getConsole(), !hideOutput, false);
             if (exitValue == null || exitValue != 0) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE,
                         String.format(Constants.ERROR_RUNNING_PUBLICAN_EXIT_CODE_MSG, (exitValue == null ? 0 : exitValue)), false);
             }
         } catch (IOException e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_RUNNING_PUBLICAN_MSG, false);
         }
     }
 
     /**
      * Run Maven to assemble the book from Docbook XML markup to the required format.
      *
      * @param jDocbookFilesDirectory The directory location that hosts the jDocbook files.
      */
     protected void runMaven(final File jDocbookFilesDirectory) {
         String jDocbookOptions = getClientConfig().getjDocbookBuildOptions();
 
         try {
             JCommander.getConsole().println(Constants.STARTING_MAVEN_BUILD_MSG);
             final Integer exitValue = ClientUtilities.runCommand("mvn " + jDocbookOptions, null, jDocbookFilesDirectory,
                     JCommander.getConsole(), !hideOutput, false);
             if (exitValue == null || exitValue != 0) {
                 printError(String.format(Constants.ERROR_RUNNING_MAVEN_EXIT_CODE_MSG, (exitValue == null ? 0 : exitValue)), false);
                 shutdown(Constants.EXIT_FAILURE);
             }
         } catch (IOException e) {
             printError(Constants.ERROR_RUNNING_MAVEN_MSG, false);
             shutdown(Constants.EXIT_FAILURE);
         }
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
