 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.io.File;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.contentspec.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 @Parameters(commandDescription = "Build, Assemble and then open the preview of the Content Specification")
 public class PreviewCommand extends AssembleCommand {
     @Parameter(names = Constants.NO_ASSEMBLE_LONG_PARAM, description = "Don't assemble the Content Specification.")
     private Boolean noAssemble = false;
 
     public PreviewCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.PREVIEW_COMMAND_NAME;
     }
 
     public Boolean getNoAssemble() {
         return noAssemble;
     }
 
     public void setNoAssemble(final Boolean noAssemble) {
         this.noAssemble = noAssemble;
     }
 
     private boolean validateFormat(final String previewFormat) {
         if (previewFormat == null) return false;
 
         if (previewFormat.equals("html") || previewFormat.equals("html-single") || previewFormat.equals("pdf")) return true;
         else return false;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         final boolean previewFromConfig = loadFromCSProcessorCfg();
         final String previewFormat = getClientConfig().getPublicanPreviewFormat();
 
         // Validate that only one id or file was entered
         ClientUtilities.validateIdsOrFiles(this, getIds(), true);
 
         // Check that the format can be previewed
         if (!validateFormat(previewFormat)) {
             printErrorAndShutdown(Constants.EXIT_CONFIG_ERROR, String.format(Constants.ERROR_UNSUPPORTED_FORMAT, previewFormat), false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (!getNoAssemble()) {
             // Assemble the content specification
             super.process();
         }
 
         // Find the preview file to be opened.
         String previewFileName = findFileToPreview(contentSpecProvider, previewFromConfig, previewFormat);
         final File previewFile = new File(previewFileName);
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Check that the file exists
         if (!previewFile.exists() || previewFile.isDirectory()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE,
                     String.format(Constants.ERROR_UNABLE_TO_FIND_PREVIEW_FILE_MSG, previewFile.getAbsolutePath()), false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Open the file
         try {
             FileUtilities.openFile(previewFile);
         } catch (Exception e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE,
                     String.format(Constants.ERROR_UNABLE_TO_OPEN_FILE_MSG, previewFile.getAbsolutePath()), false);
         }
     }
 
     /**
      * Find the name/location of the file to be opened for previewing.
      *
      * @param contentSpecProvider The Content Spec provider that can be used to get information about a content spec.
      * @param previewFromConfig   Whether or not the command is executing from a csprocessor.cfg directory or not.
      * @param previewFormat       The format of the file that should be previewed.
      * @return The filename and location of the file to be opened to be previewed, or null if it can't be found.
      */
     protected String findFileToPreview(final ContentSpecProvider contentSpecProvider, boolean previewFromConfig,
             final String previewFormat) {
         boolean previewFromId = true;
         Integer id = null;
         // Find if we are assembling from a file or ID
         if (!previewFromConfig) {
             assert getIds() != null && getIds().size() == 1;
 
             if (!getIds().get(0).matches("^\\d+")) {
                 previewFromId = false;
             } else {
                 id = Integer.parseInt(getIds().get(0));
             }
         } else {
             id = getCspConfig().getContentSpecId();
         }
 
         if (previewFromId) {
             final ContentSpecWrapper contentSpec = contentSpecProvider.getContentSpec(id, null);
 
             // Check that that content specification was found
             if (contentSpec == null) {
                 printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
             }
 
             // If using a content spec project directory the file names/locations are static based on the root directory
             if (previewFromConfig) {
                 final String rootDir = ClientUtilities.getOutputRootDirectory(getCspConfig(), contentSpec);
                 final String locale = generateOutputLocale();
 
                 final String FS = File.separator;
                 if (previewFormat.equals("pdf")) {
                     return rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION + "tmp" + FS + locale + FS + previewFormat + FS +
                             DocBookUtilities.escapeTitle(
                                     contentSpec.getProduct()) + "-" + contentSpec.getVersion() + "-" + DocBookUtilities.escapeTitle(
                             contentSpec.getTitle()) + "-" + locale + ".pdf";
                 } else {
                     return rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION + "tmp" + FS + locale + FS + previewFormat + FS + "index" +
                             ".html";
                 }
             } else {
                 return findFileToPreview(contentSpec.getTitle(), contentSpec.getVersion(), contentSpec.getProduct(), previewFormat);
             }
         } else {
             // Create the file based on an ID passed from the command line
             final String contentSpecString = getContentSpecFromFile(getIds().get(0));
 
             // Parse the spec to get the main details
             final ContentSpec contentSpec = parseContentSpec(getProviderFactory(), contentSpecString, false);
 
             return findFileToPreview(contentSpec.getTitle(), contentSpec.getVersion(), contentSpec.getProduct(), previewFormat);
         }
     }
 
     /**
      * Find the file to preview when previewing from a file or id passed via the command line, for a content specification.
      *
      * @param contentSpecTitle   The title of the content spec.
      * @param contentSpecVersion The version of the content specs product.
      * @param contentSpecProduct The product that content spec is for.
      * @param previewFormat      The file format to be previewed (html, html-single, pdf, etc...).
      * @return The filename and location of the file to be opened to be previewed, or null if it can't be found.
      */
     protected String findFileToPreview(final String contentSpecTitle, final String contentSpecVersion, final String contentSpecProduct,
             final String previewFormat) {
         // Create the fully qualified output path
         String fileDirectory = "";
         if (getOutputPath() != null && (getOutputPath().endsWith(File.separator) || new File(getOutputPath()).isDirectory())) {
             fileDirectory = ClientUtilities.fixDirectoryPath(getOutputPath());
         } else if (getOutputPath() != null) {
             final File file = new File(ClientUtilities.fixFilePath(getOutputPath()));
             if (file.getParent() != null) {
                 fileDirectory = ClientUtilities.fixDirectoryPath(file.getParent());
             }
         }
 
         final String locale = generateOutputLocale();
 
         final String FS = File.separator;
         if (previewFormat.equals("pdf")) {
            return fileDirectory + DocBookUtilities.escapeTitle(contentSpecTitle) + FS + "tmp" + locale + FS + previewFormat + FS +
                     DocBookUtilities.escapeTitle(contentSpecProduct) + "-" + contentSpecVersion + "-" + DocBookUtilities.escapeTitle(
                     contentSpecTitle) + "-en-US.pdf";
         } else {
             return fileDirectory + DocBookUtilities.escapeTitle(
                     contentSpecTitle) + FS + "tmp" + FS + locale + FS + previewFormat + FS + "index.html";
         }
     }
 
     /**
      * Gets the Locale for the publican build, which can be used to find the location of files to preview.
      *
      * @return The locale that the publican files were created as.
      */
     protected String generateOutputLocale() {
         return getTargetLocale() == null ? (getLocale() == null ? CommonConstants.DEFAULT_LOCALE : getLocale()) : getTargetLocale();
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
