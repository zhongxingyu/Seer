 package org.jboss.pressgang.ccms.contentspec.client.utils;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.internal.Console;
 import com.google.code.regexp.Matcher;
 import com.google.code.regexp.Pattern;
 import com.redhat.j2koji.base.KojiConnector;
 import com.redhat.j2koji.entities.KojiBuild;
 import com.redhat.j2koji.exceptions.KojiException;
 import com.redhat.j2koji.rpc.search.KojiBuildSearch;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.builder.utils.DocbookBuildUtilities;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommand;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.Spec;
 import org.jboss.pressgang.ccms.contentspec.entities.SpecList;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.LogMessageProvider;
 import org.jboss.pressgang.ccms.provider.StringConstantProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.UserProvider;
 import org.jboss.pressgang.ccms.provider.exception.NotFoundException;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 import org.jboss.pressgang.ccms.rest.v1.query.RESTTopicQueryBuilderV1;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.ExceptionUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.StringConstantWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.wrapper.base.BaseContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 public class ClientUtilities {
     private static int MAX_DOWNLOAD_SIZE = 500;
 
     /**
      * Checks if the location for a config location is in the correct format then corrects it if not.
      *
      * @param location The path location to be checked
      * @return The fixed path location string
      */
     public static String fixConfigLocation(final String location) {
         if (location == null || location.isEmpty()) return location;
 
         String fixedLocation = fixFilePath(location);
 
         final File file = new File(fixedLocation);
         // If the file exists then return
         if (file.exists() && file.isFile()) {
             return fixedLocation;
         }
         // The file doesn't exist so check if the location is a directory and if so fix it
         if (!location.endsWith(File.separator) && !location.endsWith(".ini")) {
             fixedLocation = fixDirectoryPath(fixedLocation);
         }
 
         // The location is a directory so add the default config file name to search for that
         if (!location.endsWith(".ini")) {
             fixedLocation += Constants.CONFIG_FILENAME;
         }
         return fixedLocation;
     }
 
     /**
      * Checks if the directory location is in the correct format then corrects it if not.
      *
      * @param location The directory path to be checked
      * @return The fixed directory location string
      */
     public static String fixDirectoryPath(final String location) {
         if (location == null || location.isEmpty()) return location;
 
         String fixedLocation = fixFilePath(location);
         if (!fixedLocation.endsWith(File.separator)) {
             fixedLocation += File.separator;
         }
 
         return fixedLocation;
     }
 
     /**
      * Checks if the host address is in the correct format then corrects it if not.
      *
      * @param host The host address of the server to be checked
      * @return The fixed host address string
      */
     public static String fixHostURL(final String host) {
         if (host == null || host.isEmpty()) return host;
 
         String fixedHost = host;
         if (!host.endsWith("/")) {
             fixedHost += "/";
         }
         if (!host.startsWith("http://") && !host.startsWith("https://")) {
             fixedHost = "http://" + fixedHost;
         }
         return fixedHost;
     }
 
     /**
      * Checks that a server exists at the specified URL by sending a request to get the headers from the URL.
      *
      * @param serverUrl The URL of the server.
      * @return True if the server exists and got a successful response otherwise false.
      */
     public static boolean validateServerExists(final String serverUrl) {
         try {
             URL url = new URL(serverUrl);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("HEAD");
             HttpURLConnection.setFollowRedirects(true);
             int response = connection.getResponseCode();
             if (response == HttpURLConnection.HTTP_MOVED_PERM || response == HttpURLConnection.HTTP_MOVED_TEMP) {
                 return validateServerExists(connection.getHeaderField("Location"));
             } else {
                 return response == HttpURLConnection.HTTP_OK || response == HttpURLConnection.HTTP_BAD_METHOD;
             }
         } catch (IOException e) {
             return false;
         }
     }
 
     /**
      * Checks if the file path is in the correct format then corrects it if not. Ie is a relative path, has the home (tilde) symbol, etc...
      *
      * @param path The path to be checked and fixed.
      * @return The fixed file path location string
      */
     public static String fixFilePath(final String path) {
         if (path == null) return null;
 
         String fixedPath = path;
         if (path.startsWith("~")) {
             fixedPath = Constants.HOME_LOCATION + fixedPath.substring(1);
         } else if (path.startsWith("./") || path.startsWith("../")) {
             try {
                 fixedPath = new File(fixedPath).getCanonicalPath();
             } catch (IOException e) {
                 // Do nothing
             }
 
             // getCanonicalPath will remove any trailing slashes so re-add it if needed.
             if (path.endsWith("\\") && !fixedPath.endsWith("\\")) {
                 fixedPath += "\\";
             } else if (path.endsWith("/") && !fixedPath.endsWith("/")) {
                 fixedPath += "/";
             }
         }
         return fixedPath;
     }
 
     /**
      * Read from a csprocessor.cfg file and intitialise the variables into a configuration object.
      *
      * @param csprocessorcfg The csprocessor.cfg file.
      * @return The content spec configuration object to load the settings into
      * @throws FileNotFoundException The csprocessor.cfg couldn't be found
      * @throws IOException
      */
     public static ContentSpecConfiguration readFromCsprocessorCfg(final File csprocessorcfg) throws IOException {
         final ContentSpecConfiguration cspConfig = new ContentSpecConfiguration();
         readFromCsprocessorCfg(csprocessorcfg, cspConfig);
         return cspConfig;
     }
 
     /**
      * Read from a csprocessor.cfg file and intitialise the variables into a configuration object.
      *
      * @param csprocessorcfg The csprocessor.cfg file.
      * @param cspConfig      The content spec configuration object to load the settings into
      * @throws FileNotFoundException The csprocessor.cfg couldn't be found
      * @throws IOException
      */
     public static void readFromCsprocessorCfg(final File csprocessorcfg, final ContentSpecConfiguration cspConfig) throws IOException {
         final Properties prop = new Properties();
         prop.load(new FileInputStream(csprocessorcfg));
 
         // If the file contains no properties then return an empty config
         if (prop.isEmpty()) {
             return;
         }
 
         final String specId = prop.getProperty("SPEC_ID");
         cspConfig.setContentSpecId(Integer.parseInt(specId == null ? null : specId));
         cspConfig.setServerUrl(fixHostURL(prop.getProperty("SERVER_URL")));
         cspConfig.getZanataDetails().setServer(fixHostURL(prop.getProperty("ZANATA_URL")));
         cspConfig.getZanataDetails().setProject(prop.getProperty("ZANATA_PROJECT_NAME"));
         cspConfig.getZanataDetails().setVersion(prop.getProperty("ZANATA_PROJECT_VERSION"));
         cspConfig.setKojiHubUrl(fixHostURL(prop.getProperty("KOJI_HUB_URL")));
         cspConfig.setPublishCommand(prop.getProperty("PUBLISH_COMMAND"));
     }
 
     /**
      * Generates the contents of a csprocessor.cfg file from the passed arguments.
      *
      * @param contentSpec The content specification object the csprocessor.cfg will be used for.
      * @param serverUrl   The server URL that the content specification exists on.
      * @return The generated contents of the csprocessor.cfg file.
      */
     public static String generateCsprocessorCfg(final ContentSpecWrapper contentSpec, final String serverUrl,
             final ZanataDetails zanataDetails) {
         final StringBuilder output = new StringBuilder();
         output.append("# SPEC_TITLE=").append(DocBookUtilities.escapeTitle(contentSpec.getTitle())).append("\n");
         output.append("SPEC_ID=").append(contentSpec.getId()).append("\n");
         output.append("SERVER_URL=").append(serverUrl).append("\n");
         output.append("ZANATA_URL=").append(zanataDetails.getServer() == null ? "" : zanataDetails.getServer()).append("\n");
         output.append("ZANATA_PROJECT_NAME=").append(zanataDetails.getProject() == null ? "" : zanataDetails.getProject()).append("\n");
         output.append("ZANATA_PROJECT_VERSION=").append(zanataDetails.getVersion() == null ? "" : zanataDetails.getVersion()).append("\n");
         output.append("KOJI_HUB_URL=\n");
         output.append("PUBLISH_COMMAND=\n");
         return output.toString();
     }
 
     /**
      * Runs a command from a specified directory
      *
      * @param command       The command to be run.
      * @param dir           The directory to run the command from.
      * @param console       The console to print the output to.
      * @param displayOutput Whether the output should be displayed or not.
      * @param allowInput    Whether the process should allow input form stdin.
      * @return The exit value of the command
      * @throws IOException
      */
     public static Integer runCommand(final String command, final File dir, final Console console, boolean displayOutput,
             boolean allowInput) throws IOException {
         return runCommand(command, null, dir, console, displayOutput, allowInput);
     }
 
     /**
      * Runs a command from a specified directory
      *
      * @param command       The command to be run.
      * @param envVariables  An array of environment variables to be used.
      * @param dir           The directory to run the command from.
      * @param console       The console to print the output to.
      * @param displayOutput Whether the output should be displayed or not.
      * @param allowInput    Whether the process should allow input form stdin.
      * @return The exit value of the command
      * @throws IOException
      */
     public static Integer runCommand(final String command, final String[] envVariables, final File dir, final Console console,
             boolean displayOutput, boolean allowInput) throws IOException {
         if (!dir.isDirectory()) throw new IOException();
 
         try {
             String[] fixedEnvVariables = envVariables == null ? null : envVariables.clone();
             final Map<String, String> env = System.getenv();
             final List<String> envVars = new ArrayList<String>();
             for (final Entry<String, String> entry : env.entrySet()) {
                 final String key = entry.getKey();
                 if (!key.equals("XML_CATALOG_FILES")) envVars.add(key + "=" + entry.getValue());
             }
             if (envVariables != null) {
                 Collections.addAll(envVars, envVariables);
             }
             fixedEnvVariables = envVars.toArray(new String[envVars.size()]);
 
             final Process p = Runtime.getRuntime().exec(splitCommandArguments(command), fixedEnvVariables, dir);
 
             // Create a separate thread to read the stderr stream
             final InputStreamHandler stdErr = new InputStreamHandler(p.getErrorStream(), console);
             final InputStreamHandler stdInPipe = new InputStreamHandler(System.in, p.getOutputStream());
 
             // Pipe stdin to the process
             if (allowInput) {
                 stdInPipe.start();
             }
 
             // Get the output of the command
             if (displayOutput) {
                 stdErr.start();
 
                 final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                 String line;
                 try {
                     while ((line = br.readLine()) != null) {
                         synchronized (console) {
                             console.println(line);
                         }
                     }
                 } catch (Exception e) {
                     // Do nothing
                     JCommander.getConsole().println(e.getMessage());
                     e.printStackTrace();
                 }
             }
 
             // Wait for the process to finish
             p.waitFor();
 
             // Ensure that the stdin reader gets shutdown
             stdInPipe.shutdown();
 
             // Wait for the output to be printed
             while (stdErr.isAlive()) {
                 Thread.sleep(100);
             }
 
             return p.exitValue();
         } catch (InterruptedException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Split a command into an array of arguments that can be passed to Runtime.exec().
      *
      * @param command The command to be split
      * @return The split command.
      */
     protected static String[] splitCommandArguments(String command) {
         String[] args = command.split(" ");
         List<String> commandArgs = new LinkedList<String>();
         boolean quotes = false;
         StringBuilder quotesString = null;
         for (final String arg : args) {
             if (arg.startsWith("\"") && !quotes) {
                 quotes = true;
                 quotesString = new StringBuilder(arg.substring(1)).append(" ");
             } else if (arg.endsWith("\"") && quotes) {
                 quotes = false;
                 quotesString.append(arg.substring(0, arg.length() - 1));
                 commandArgs.add(quotesString.toString());
             } else if (quotes) {
                 quotesString.append(arg).append(" ");
             } else {
                 commandArgs.add(arg);
             }
         }
 
         return commandArgs.toArray(new String[commandArgs.size()]);
     }
 
     /**
      * Builds a Content Specification list for a list of content specifications.
      */
     public static SpecList buildSpecList(final List<ContentSpecWrapper> specList, final DataProviderFactory providerFactory) {
         final List<Spec> specs = new ArrayList<Spec>();
         for (final ContentSpecWrapper cs : specList) {
             UserWrapper creator = null;
             if (cs.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID) != null) {
                 final CollectionWrapper<UserWrapper> users = providerFactory.getProvider(UserProvider.class).getUsersByName(
                         cs.getProperty(CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue());
                 if (users != null && users.size() == 1) {
                     creator = users.getItems().get(0);
                 }
             }
             specs.add(new Spec(cs.getId(), cs.getTitle(), cs.getProduct(), cs.getVersion(), creator != null ? creator.getUsername() : null,
                     cs.getLastModified()));
         }
         return new SpecList(specs, specs.size());
     }
 
     /**
      * Generates the response output for a list of content specifications
      *
      * @param contentSpecs The SpecList that contains the processed Content Specifications
      * @return The generated response output.
      */
     public static String generateContentSpecList(final SpecList contentSpecs) {
         final LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
         // Create the initial sizes incase they never increase
         sizes.put("ID", 2);
         sizes.put("SPEC ID", 7);
         sizes.put("TITLE", 5);
         sizes.put("SPEC TITLE", 10);
         sizes.put("PRODUCT", 7);
         sizes.put("VERSION", 7);
         sizes.put("CREATED BY", 10);
         sizes.put("LAST MODIFIED", 13);
         if (contentSpecs != null && contentSpecs.getSpecs() != null && !contentSpecs.getSpecs().isEmpty()) {
             for (final Spec spec : contentSpecs.getSpecs()) {
                 if (spec.getId().toString().length() > sizes.get("ID")) {
                     sizes.put("ID", spec.getId().toString().length());
                 }
 
                 if (spec.getProduct() != null && spec.getProduct().length() > sizes.get("PRODUCT")) {
                     sizes.put("PRODUCT", spec.getProduct().length());
                 }
 
                 if (spec.getTitle() != null && spec.getTitle().length() > sizes.get("TITLE")) {
                     sizes.put("TITLE", spec.getTitle().length());
                 }
 
                 if (spec.getVersion() != null && spec.getVersion().length() > sizes.get("VERSION")) {
                     sizes.put("VERSION", spec.getVersion().length());
                 }
 
                 if (spec.getCreator() != null && spec.getCreator().length() > sizes.get("CREATED BY")) {
                     sizes.put("CREATED BY", spec.getCreator().length());
                 }
             }
 
             final String format = "%" + (sizes.get("ID") + 2) + "s%" + (sizes.get("TITLE") + 2) + "s%" + (sizes.get(
                     "PRODUCT") + 2) + "s%" + (sizes.get("VERSION") + 2) + "s%" + (sizes.get("CREATED BY") + 2) + "s%" + (sizes.get(
                     "LAST MODIFIED") + 2) + "s";
 
             final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
             final StringBuilder output = new StringBuilder(
                     String.format(format, "ID", "TITLE", "PRODUCT", "VERSION", "CREATED BY", "LAST MODIFIED") + "\n");
             for (final Spec spec : contentSpecs.getSpecs()) {
                 output.append(
                         String.format(format, spec.getId().toString(), escapeForList(spec.getTitle()), escapeForList(spec.getProduct()),
                                 escapeForList(spec.getVersion()), spec.getCreator() == null ? "Unknown" : spec.getCreator(),
                                 spec.getLastModified() == null ? "Unknown" : dateFormatter.format(spec.getLastModified())) + "\n");
             }
             return output.toString();
         }
         return "";
     }
 
     private static String escapeForList(final String input) {
         return input == null ? "" : input;
     }
 
     /**
      * Get the next pubsnumber from koji for the content spec that will be built.
      *
      * @param contentSpec The contentspec to be built.
      * @param kojiHubUrl  The URL of the Koji Hub server to connect to.
      * @return The next valid pubsnumber for a build to koji.
      * @throws KojiException         Thrown if an error occurs when searching koji for the builds.
      * @throws MalformedURLException Thrown if the passed Koji Hub URL isn't a valid URL.
      */
     public static Integer getPubsnumberFromKoji(final ContentSpec contentSpec,
             final String kojiHubUrl) throws KojiException, MalformedURLException {
         assert contentSpec != null;
 
         if (kojiHubUrl == null) {
             throw new MalformedURLException();
         }
 
         final String product = DocBookUtilities.escapeTitle(contentSpec.getProduct());
         final String version = contentSpec.getVersion();
         final String bookTitle = DocBookUtilities.escapeTitle(contentSpec.getTitle());
         final String locale = contentSpec.getLocale() == null ? CommonConstants.DEFAULT_LOCALE : contentSpec.getLocale();
         final String bookVersion = DocbookBuildUtilities.generateRevision(contentSpec);
 
         // Connect to the koji hub
         final KojiConnector connector = new KojiConnector();
         connector.connectTo(fixHostURL(kojiHubUrl));
 
         // Perform the search using the info from the content spec
         final String packageName = product + "-" + bookTitle + "-" + version + "-web-" + locale + "-" + bookVersion + "-";
         final KojiBuildSearch buildSearch = new KojiBuildSearch(packageName + "*");
         connector.executeMethod(buildSearch);
 
         // Search through each result to find the pubsnumber
         final List<KojiBuild> builds = buildSearch.getResults();
 
         // Check to see if we found any results
         if (builds.size() > 0) {
             Integer pubsnumber = 0;
             for (final KojiBuild build : builds) {
                 final String buildName = build.getName();
                 final String matchString = buildName.replace(packageName, "");
                 final Pattern pattern = Pattern.compile("(?<Pubsnumber>[0-9]+).*");
                 final Matcher matcher = pattern.matcher(matchString);
 
                 while (matcher.find()) {
                     final Integer buildPubsnumber = Integer.parseInt(matcher.group("Pubsnumber"));
                     if (buildPubsnumber > pubsnumber) pubsnumber = buildPubsnumber;
                     break;
                 }
             }
 
             return pubsnumber + 1;
         } else {
             return null;
         }
     }
 
     /**
      * @param providerFactory   The Entity Provider Factory to create Providers to get Entities from a Datasource.
      * @param loggerManager     The Logging manager that keeps tracks of error logs.
      * @param contentSpecString The Content Spec String representation to be parsed.
      * @return The parsed content spec if no errors occurred otherwise null.
      */
     public static ContentSpec parseContentSpecString(final DataProviderFactory providerFactory, final ErrorLoggerManager loggerManager,
             final String contentSpecString) {
         return parseContentSpecString(providerFactory, loggerManager, contentSpecString, ContentSpecParser.ParsingMode.EITHER);
     }
 
     /**
      * @param providerFactory   The Entity Provider Factory to create Providers to get Entities from a Datasource.
      * @param loggerManager     The Logging manager that keeps tracks of error logs.
      * @param contentSpecString The Content Spec String representation to be parsed.
      * @param parsingMode       The mode that the content spec should be parsed as.
      * @return The parsed content spec if no errors occurred otherwise null.
      */
     public static ContentSpec parseContentSpecString(final DataProviderFactory providerFactory, final ErrorLoggerManager loggerManager,
             final String contentSpecString, final ContentSpecParser.ParsingMode parsingMode) {
         return parseContentSpecString(providerFactory, loggerManager, contentSpecString, parsingMode, false);
     }
 
     /**
      * @param providerFactory   The Entity Provider Factory to create Providers to get Entities from a Datasource.
      * @param loggerManager     The Logging manager that keeps tracks of error logs.
      * @param contentSpecString The Content Spec String representation to be parsed.
      * @param parsingMode       The mode that the content spec should be parsed as.
      * @param processProcesses  If processes should be processed to setup their relationships (makes external calls)
      * @return The parsed content spec if no errors occurred otherwise null.
      */
     public static ContentSpec parseContentSpecString(final DataProviderFactory providerFactory, final ErrorLoggerManager loggerManager,
             final String contentSpecString, final ContentSpecParser.ParsingMode parsingMode, boolean processProcesses) {
         final ContentSpecParser csp = new ContentSpecParser(providerFactory, loggerManager);
         if (csp.parse(contentSpecString, parsingMode, processProcesses)) {
             return csp.getContentSpec();
         } else {
             return null;
         }
     }
 
     /**
      * Gets the output root directory based on the configuration files and ContentSpec object.
      *
      * @param cspConfig   The content spec configuration settings.
      * @param contentSpec The content spec object to get details from for the output directory.
      * @return A string that represents where the root folder is for content to be saved.
      */
     public static String getOutputRootDirectory(final ContentSpecConfiguration cspConfig, final ContentSpec contentSpec) {
         return getOutputRootDirectory(cspConfig, contentSpec.getTitle());
     }
 
     /**
      * Gets the output root directory based on the configuration files and ContentSpec entity object.
      *
      * @param providerFactory
      * @param cspConfig       The content spec configuration settings.
      * @param contentSpec     The content spec object to get details from for the output directory.
      * @return A string that represents where the root folder is for content to be saved.
      */
     public static String getOutputRootDirectory(final DataProviderFactory providerFactory, final ContentSpecConfiguration cspConfig,
             final BaseContentSpecWrapper<?> contentSpec) {
         return getOutputRootDirectoryFromEscapedTitle(cspConfig, getEscapedContentSpecTitle(providerFactory, contentSpec));
     }
 
     /**
      * Gets the output root directory based on the configuration files and ContentSpec entity object.
      *
      * @param cspConfig        The content spec configuration settings.
      * @param contentSpecTitle The title of the content specification for the output directory.
      * @return A string that represents where the root folder is for content to be saved.
      */
     public static String getOutputRootDirectory(final ContentSpecConfiguration cspConfig, final String contentSpecTitle) {
         assert contentSpecTitle != null;
 
         return getOutputRootDirectoryFromEscapedTitle(cspConfig, DocBookUtilities.escapeTitle(contentSpecTitle));
     }
 
     /**
      * Gets the output root directory based on the configuration files and ContentSpec entity object.
      *
      * @param cspConfig               The content spec configuration settings.
      * @param escapedContentSpecTitle The title of the content specification for the output directory.
      * @return A string that represents where the root folder is for content to be saved.
      */
     public static String getOutputRootDirectoryFromEscapedTitle(final ContentSpecConfiguration cspConfig,
             final String escapedContentSpecTitle) {
         assert escapedContentSpecTitle != null;
         assert cspConfig != null;
 
         return (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals(
                 "") ? "" : (cspConfig.getRootOutputDirectory() + escapedContentSpecTitle + File.separator));
     }
 
     /**
      * TODO
      *
      * @param command
      * @param cspConfig
      * @param ids
      */
     public static boolean prepareAndValidateIds(final BaseCommandImpl command, final ContentSpecConfiguration cspConfig,
             final Collection<Integer> ids) {
         boolean isLoadingFromConfig = prepareIds(command, cspConfig, ids);
         validateIdsOrFiles(command, ids, true);
         return isLoadingFromConfig;
     }
 
     /**
      * TODO
      *
      * @param command
      * @param cspConfig
      * @param ids
      */
     public static boolean prepareIds(final BaseCommandImpl command, final ContentSpecConfiguration cspConfig,
             final Collection<Integer> ids) {
         boolean isLoadingFromConfig = false;
         // If there are no ids then use the csprocessor.cfg file
         if (command.loadFromCSProcessorCfg()) {
             // Check that the config details are valid
             if (cspConfig != null && cspConfig.getContentSpecId() != null) {
                 ids.clear();
                 ids.add(cspConfig.getContentSpecId());
                 isLoadingFromConfig = true;
             }
         }
 
         return isLoadingFromConfig;
     }
 
     /**
      * TODO
      *
      * @param command
      * @param cspConfig
      * @param ids
      */
     public static boolean prepareAndValidateStringIds(final BaseCommandImpl command, final ContentSpecConfiguration cspConfig,
             final Collection<String> ids) {
         boolean isLoadingFromConfig = prepareStringIds(command, cspConfig, ids);
         validateIdsOrFiles(command, ids, true);
         return isLoadingFromConfig;
     }
 
     /**
      * Prepares a commands ids using configuration files if required.
      *
      * @param command   The command to prepare the ids for.
      * @param cspConfig The csprocessor.cfg information for the command execution.
      * @param ids       The list of ids that should be populated.
      * @return True if the command prepared the ids from a csprocessor.cfg, otherwise false.
      */
     public static boolean prepareStringIds(final BaseCommandImpl command, final ContentSpecConfiguration cspConfig,
             final Collection<String> ids) {
         boolean isLoadingFromConfig = false;
         // If there are no ids then use the csprocessor.cfg file
         if (command.loadFromCSProcessorCfg()) {
             // Check that the config details are valid
             if (cspConfig != null && cspConfig.getContentSpecId() != null) {
                 ids.clear();
                 ids.add(cspConfig.getContentSpecId().toString());
                 isLoadingFromConfig = true;
             }
         }
 
         return isLoadingFromConfig;
     }
 
     /**
      * Validates that a list of ids or files has one and only one id/file set. If not then the commands {@link
      * BaseCommandImpl#printErrorAndShutdown(int, String, boolean)} method is called.
      *
      * @param command                   The command that the validation is happening for.
      * @param ids                       The list of ids or files to validate.
      * @param canLoadFromCsprocessorCfg Whether or not the command is allowed to load from the csprocessor.cfg file.
      */
     public static void validateIdsOrFiles(BaseCommandImpl command, final Collection<?> ids, boolean canLoadFromCsprocessorCfg) {
         // Check that one and only one ID exists
         if (ids.size() == 0) {
             if (canLoadFromCsprocessorCfg) {
                 command.printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, Constants.ERROR_NO_ID_MSG, false);
             } else {
                 command.printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, Constants.ERROR_NO_ID_CMD_LINE_MSG, false);
             }
         } else if (ids.size() > 1) {
             command.printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, Constants.ERROR_MULTIPLE_ID_MSG, false);
         }
     }
 
     /**
      * Creates the Content Spec Project directory and adds the csprocessor.cfg and Content Spec file to the directory.
      *
      * @param command           The command the details are being created from.
      * @param cspConfig         The csprocessor.cfg configuration settings.
      * @param directory         The project directory.
      * @param contentSpecString The content spec string representation.
      * @param contentSpec       The content spec object from a datasource.
      * @param zanataDetails     The Connection details for zanata.
      */
     public static void createContentSpecProject(final BaseCommandImpl command, final ContentSpecConfiguration cspConfig,
             final File directory, final String contentSpecString, final ContentSpecWrapper contentSpec, ZanataDetails zanataDetails) {
         // If the output directory exists and force is enabled delete the directory contents
         if (directory.exists() && directory.isDirectory()) {
             // TODO Check that the directory was successfully deleted
             FileUtilities.deleteDir(directory);
         }
 
         boolean error = false;
 
         // Save the csprocessor.cfg and post spec to file if the create was successful
         final String escapedTitle = getEscapedContentSpecTitle(command.getProviderFactory(), contentSpec);
         final File outputSpec = new File(
                 cspConfig.getRootOutputDirectory() + escapedTitle + File.separator + escapedTitle + "-post." + Constants
                         .FILENAME_EXTENSION);
         final File outputConfig = new File(cspConfig.getRootOutputDirectory() + escapedTitle + File.separator + "csprocessor.cfg");
         final String config = generateCsprocessorCfg(contentSpec, cspConfig.getServerUrl(), zanataDetails);
 
         // Create the directory
         if (outputConfig.getParentFile() != null && !outputConfig.getParentFile().exists()) {
             // TODO Check that the directory was successfully created
             outputConfig.getParentFile().mkdirs();
         }
 
         // Save the csprocessor.cfg
         try {
             FileUtilities.saveFile(outputConfig, config, Constants.FILE_ENCODING);
             JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, outputConfig.getAbsolutePath()));
         } catch (IOException e) {
             command.printError(String.format(Constants.ERROR_FAILED_SAVING_FILE, outputConfig.getAbsolutePath()), false);
             error = true;
         }
 
         // Save the Post Processed spec
         try {
             FileUtilities.saveFile(outputSpec, contentSpecString, Constants.FILE_ENCODING);
             JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, outputSpec.getAbsolutePath()));
         } catch (IOException e) {
             command.printError(String.format(Constants.ERROR_FAILED_SAVING_FILE, outputSpec.getAbsolutePath()), false);
             error = true;
         }
 
         if (error) {
             command.shutdown(Constants.EXIT_FAILURE);
         }
     }
 
     /**
      * Saves some content to a file and backups a file if one exists with the same name. The method will also determine the correct
      * directory to save to using the filename and output path.
      *
      * @param command    The command that the file is being saved for.
      * @param fileName   The name that the file should be saved as.
      * @param outputPath The location that the file should be saved to.
      * @param content    The content to be saved to the file.
      */
     public static void saveOutputFile(final BaseCommandImpl command, String fileName, String outputPath, String content) {
         // Create the output file
         File output;
         String fixedOutputPath = ClientUtilities.fixFilePath(outputPath);
         if (fixedOutputPath != null && fixedOutputPath.endsWith(File.separator)) {
             output = new File(fixedOutputPath + fileName);
         } else if (fixedOutputPath == null || fixedOutputPath.equals("")) {
             output = new File(fileName);
         } else {
             output = new File(fixedOutputPath);
         }
 
         // Make sure the directories exist
         if (output.isDirectory()) {
             output = new File(output.getAbsolutePath() + File.separator + fileName);
         } else if (output.getParentFile() != null) {
             output.getParentFile().mkdirs();
         }
 
         // If the file exists then create a backup file
         if (output.exists()) {
             output.renameTo(new File(output.getAbsolutePath() + ".backup"));
         }
 
         // Create and write to the file
         try {
             FileUtilities.saveFile(output, content, Constants.FILE_ENCODING);
             JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getName()));
         } catch (IOException e) {
             command.printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_FAILED_SAVING, false);
         }
     }
 
     public static LogMessageWrapper createLogDetails(final DataProviderFactory providerFactory, final UserWrapper user,
             final String message, final boolean isRevisionHistoryMessage) {
         return createLogDetails(providerFactory, user.getUsername(), message, isRevisionHistoryMessage);
     }
 
     public static LogMessageWrapper createLogDetails(final DataProviderFactory providerFactory, final String user, final String message,
             final boolean isRevisionHistoryMessage) {
         LogMessageWrapper logDetails = null;
         if (message != null) {
             logDetails = providerFactory.getProvider(LogMessageProvider.class).createLogMessage();
             logDetails.setMessage(message);
             logDetails.setUser(user);
             if (isRevisionHistoryMessage) {
                 logDetails.setFlags(0 | RESTLogDetailsV1.MAJOR_CHANGE_FLAG_BIT);
             } else {
                 logDetails.setFlags(0 | RESTLogDetailsV1.MINOR_CHANGE_FLAG_BIT);
             }
         }
 
         return logDetails;
     }
 
     /**
      * Validate that a Language is a valid language as defined by the server.
      *
      * @param providerFactory
      * @param lang            The language to be validated.
      * @return True if the language exists on the server otherwise false.
      */
     public static boolean validateLanguage(final BaseCommand command, final DataProviderFactory providerFactory, final String lang) {
         return validateLanguages(command, providerFactory, new String[]{lang});
     }
 
     /**
      * Validate that a Language is a valid language as defined by the server.
      *
      * @param providerFactory
      * @param langs           The languages to be validated.
      * @return True if the language exists on the server otherwise false.
      */
     public static boolean validateLanguages(final BaseCommand command, final DataProviderFactory providerFactory, final String[] langs) {
         final StringConstantWrapper localesConstant = providerFactory.getProvider(StringConstantProvider.class).getStringConstant(
                 CommonConstants.LOCALES_STRING_CONSTANT_ID);
         final List<String> locales = CollectionUtilities.replaceStrings(CollectionUtilities.sortAndReturn(
                 CollectionUtilities.toArrayList(localesConstant.getValue().split("[\\s\r\n]*,[\\s\r\n]*"))), "_", "-");
 
         boolean valid = true;
         for (final String lang : langs) {
             if (!locales.contains(lang)) {
                 command.printError(
                         String.format(Constants.ERROR_INVALID_LOCALE_MSG, lang, localesConstant.getValue().replaceAll("\r\n|\n", " ")),
                         false);
                 valid = false;
             }
         }
 
         return valid;
     }
 
     /**
      * Saves the content specification to the server using the provided task.
      *
      * @param command The command the save event is occurring for.
      * @param task    The Task to be executed that will save the content spec to the server.
      * @return The result from saving the content spec to the server.
      */
     public static TextContentSpecWrapper saveContentSpec(final BaseCommand command, final FutureTask<TextContentSpecWrapper> task) {
         // Run the task in a separate thread and output a waiting message every 10 seconds
         JCommander.getConsole().println("Saving to the server...");
         final Thread thread = new Thread(task);
         thread.start();
         int count = 0;
         while (!task.isDone()) {
             // Sleep for 2 seconds
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 // Do nothing here as it's fine if this gets interrupted
             }
             count++;
 
             if (count % 10 == 0) {
                 JCommander.getConsole().println("Waiting for a response from the server...");
             }
         }
 
         // Get the response from the task
         TextContentSpecWrapper retValue = null;
         try {
             retValue = task.get();
         } catch (InterruptedException e) {
             JCommander.getConsole().println(ExceptionUtilities.getStackTrace(e));
             command.shutdown(Constants.EXIT_FAILURE);
         } catch (ExecutionException e) {
             JCommander.getConsole().println(ExceptionUtilities.getStackTrace(e));
             command.shutdown(Constants.EXIT_FAILURE);
         }
 
         return retValue;
     }
 
     public static ContentSpecWrapper getContentSpecEntity(ContentSpecProvider contentSpecProvider, Integer id, Integer revision) {
         ContentSpecWrapper contentSpec = null;
         try {
             contentSpec = contentSpecProvider.getContentSpec(id, revision);
         } catch (NotFoundException e) {
             // Do nothing as we handle this below.
         }
         return contentSpec;
     }
 
     public static String getContentSpecAsString(ContentSpecProvider contentSpecProvider, Integer id, Integer revision) {
         String contentSpec = null;
         try {
             contentSpec = contentSpecProvider.getContentSpecAsString(id, revision);
         } catch (NotFoundException e) {
             // Do nothing as we handle this below.
         }
         return contentSpec;
     }
 
     public static String getEscapedContentSpecTitle(final DataProviderFactory providerFactory,
             final BaseContentSpecWrapper<?> contentSpecEntity) {
         final String title;
         if (contentSpecEntity.getTitle() != null) {
             title = contentSpecEntity.getTitle();
         } else if (contentSpecEntity.getFailed() != null) {
             final ContentSpec contentSpec = parseContentSpecString(providerFactory, new ErrorLoggerManager(),
                     contentSpecEntity.getFailed());
             if (contentSpec != null && contentSpec.getTitle() != null) {
                 title = contentSpec.getTitle();
             } else {
                 title = contentSpecEntity.getId().toString();
             }
         } else {
             title = contentSpecEntity.getId().toString();
         }
 
         return DocBookUtilities.escapeTitle(title);
     }
 
     public static String createLogMessage(final String username, final String message) {
         final StringBuilder retValue = new StringBuilder();
         if (!isNullOrEmpty(username)) {
             retValue.append(username + ": ");
         }
         if (!isNullOrEmpty(message)) {
             retValue.append(message);
         }
         return retValue.toString();
     }
 
     /**
      * Download all the topics that are to be used during processing from the
      * parsed Content Specification.
      */
     public static void downloadAllTopics(final DataProviderFactory providerFactory, final ContentSpec contentSpec,
             final Integer maxRevision) {
         final TopicProvider topicProvider = providerFactory.getProvider(TopicProvider.class);
         final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
         final List<Integer> topicIds = new ArrayList<Integer>();
         final List<Pair<Integer, Integer>> revisionTopicIds = new ArrayList<Pair<Integer, Integer>>();
 
         // populate the topicIds and revisionTopicIds
         for (final SpecTopic specTopic : specTopics) {
             if (specTopic.getRevision() == null) {
                 topicIds.add(specTopic.getDBId());
             } else {
                 revisionTopicIds.add(new Pair<Integer, Integer>(specTopic.getDBId(), specTopic.getRevision()));
             }
         }
 
         // Check if a maximum revision was specified for processing
         if (maxRevision == null && !topicIds.isEmpty()) {
             // Download the list of topics in one go to reduce I/O overhead
             JCommander.getConsole().println("Attempting to download all the latest topics...");
             final RESTTopicQueryBuilderV1 queryBuilder = new RESTTopicQueryBuilderV1();
             if (topicIds.size() > MAX_DOWNLOAD_SIZE) {
                 int start = 0;
                while (start < topicIds.size()) {
                     final List<Integer> subList = topicIds.subList(start, Math.min(start + MAX_DOWNLOAD_SIZE, topicIds.size()));
                     queryBuilder.setTopicIds(subList);
                     topicProvider.getTopicsWithQuery(queryBuilder.getQuery());
 
                     start += MAX_DOWNLOAD_SIZE;
                 }
             } else {
                 queryBuilder.setTopicIds(topicIds);
                 topicProvider.getTopicsWithQuery(queryBuilder.getQuery());
             }
         } else if (!topicIds.isEmpty()) {
             // Add to the list of referenced topic ids
             for (final Integer topicId : topicIds) {
                 revisionTopicIds.add(new Pair<Integer, Integer>(topicId, maxRevision));
             }
         }
 
         if (!revisionTopicIds.isEmpty()) {
             downloadRevisionTopics(topicProvider, revisionTopicIds);
         }
     }
 
     /**
      * Download the Topics from the REST API that specify a revision.
      *
      * @param referencedRevisionTopicIds The Set of topic ids and revision to download.
      */
     public static void downloadRevisionTopics(final TopicProvider topicProvider, final List<Pair<Integer,
             Integer>> referencedRevisionTopicIds) {
         JCommander.getConsole().println("Attempting to download all the revision topics...");
 
         final int showPercent = 10;
         final float total = referencedRevisionTopicIds.size();
         float current = 0;
         int lastPercent = 0;
 
         for (final Pair<Integer, Integer> topicToRevision : referencedRevisionTopicIds) {
             // If we want to update the revisions then we should get the latest topic and not the revision
             topicProvider.getTopic(topicToRevision.getFirst(), topicToRevision.getSecond());
 
             ++current;
             final int percent = Math.round(current / total * 100);
             if (percent - lastPercent >= showPercent) {
                 lastPercent = percent;
                 JCommander.getConsole().println("\tDownloading revision topics " + percent + "% Done");
             }
         }
     }
 }
 
 class InputStreamHandler extends Thread implements ShutdownAbleApp {
     private final InputStream stream;
     private final StringBuffer buffer;
     private final Console console;
     private final OutputStream outStream;
 
     private final AtomicBoolean shutdown = new AtomicBoolean(false);
     private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
 
     public InputStreamHandler(final InputStream stream, final StringBuffer buffer) {
         this.stream = stream;
         this.buffer = buffer;
         console = null;
         outStream = null;
     }
 
     public InputStreamHandler(final InputStream stream, final Console console) {
         this.stream = stream;
         buffer = null;
         this.console = console;
         outStream = null;
     }
 
     public InputStreamHandler(final InputStream stream, final OutputStream outStream) {
         this.stream = stream;
         buffer = null;
         console = null;
         this.outStream = outStream;
     }
 
     @Override
     public void run() {
         int nextChar;
         try {
             while ((nextChar = stream.read()) != -1 && !isShuttingDown.get()) {
                 final char c = (char) nextChar;
                 if (buffer != null) {
                     buffer.append(c);
                 } else if (outStream != null) {
                     outStream.write(c);
                     outStream.flush();
                 } else {
                     synchronized (console) {
                         console.print(c + "");
                     }
                 }
             }
         } catch (Exception e) {
             // Do nothing
             JCommander.getConsole().println(e.getMessage());
             e.printStackTrace();
         }
     }
 
     @Override
     public void shutdown() {
         this.isShuttingDown.set(true);
     }
 
     @Override
     public boolean isShutdown() {
         return shutdown.get();
     }
 }
