 package com.redhat.contentspec.client.utils;
 
 import java.awt.*;
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
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.internal.Console;
 import com.google.code.regexp.Matcher;
 import com.google.code.regexp.Pattern;
 import com.redhat.contentspec.builder.utils.DocbookBuildUtilities;
 import com.redhat.contentspec.client.commands.base.BaseCommand;
 import com.redhat.contentspec.client.config.ClientConfiguration;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.entities.Spec;
 import com.redhat.contentspec.client.entities.SpecList;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.j2koji.base.KojiConnector;
 import com.redhat.j2koji.entities.KojiBuild;
 import com.redhat.j2koji.exceptions.KojiException;
 import com.redhat.j2koji.rpc.search.KojiBuildSearch;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTReader;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTStringConstantV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.utils.common.StringUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 public class ClientUtilities {
     /**
      * Checks if the location for a config location is in the correct format then corrects it if not.
      *
      * @param location The path location to be checked
      * @return The fixed path location string
      */
     public static String validateConfigLocation(final String location) {
         if (location == null || location.isEmpty()) return location;
 
         String fixedLocation = location;
         if (location.startsWith("~")) {
             fixedLocation = Constants.HOME_LOCATION + location.substring(1);
         } else if (location.startsWith("./") || location.startsWith("../")) {
             try {
                 fixedLocation = new File(location).getCanonicalPath();
             } catch (IOException e) {
                 // Do nothing
             }
         }
         final File file = new File(fixedLocation);
         if (file.exists() && file.isFile()) {
             return fixedLocation;
         }
         if (!location.endsWith(File.separator) && !location.endsWith(".ini")) {
             fixedLocation += File.separator;
         }
         if (!location.endsWith(".ini")) {
             fixedLocation += Constants.CONFIG_FILENAME;
         }
         return fixedLocation;
     }
 
     /**
      * Checks if the directory location is in the correct format then corrects it if not.
      *
      * @param location The path location to be checked
      * @return The fixed path location string
      */
     public static String validateDirLocation(final String location) {
         if (location == null || location.isEmpty()) return location;
 
         String fixedLocation = location;
         if (!location.endsWith(File.separator)) {
             fixedLocation += File.separator;
         }
         if (location.startsWith("~")) {
             fixedLocation = Constants.HOME_LOCATION + fixedLocation.substring(1);
         } else if (location.startsWith("./") || location.startsWith("../")) {
             try {
                 fixedLocation = new File(fixedLocation).getCanonicalPath();
             } catch (IOException e) {
                 // Do nothing
             }
         }
         return fixedLocation;
     }
 
     /**
      * Checks if the host address is in the correct format then corrects it if not.
      *
      * @param host The host address of the server to be checked
      * @return The fixed host address string
      */
     public static String validateHost(final String host) {
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
      * Checks if the file path is in the correct format then corrects it if not.
      *
      * @param host The path location to be checked
      * @return The fixed path location string
      */
     public static String validateFilePath(final String filePath) {
         if (filePath == null) return null;
         String fixedPath = filePath;
         if (filePath.startsWith("~")) {
             fixedPath = Constants.HOME_LOCATION + fixedPath.substring(1);
         } else if (filePath.startsWith("./") || filePath.startsWith("../")) {
             try {
                 fixedPath = new File(fixedPath).getCanonicalPath();
             } catch (IOException e) {
                 // Do nothing
             }
         }
         return fixedPath;
     }
 
     /**
      * Authenticates a user against the database specified by their username
      *
      * @param username The key used to search the database for a user
      * @return The database User object for the specified API Key or null if none was found
      */
     public static RESTUserV1 authenticateUser(final String username, final RESTReader reader) {
         // Check that the username is valid and get the user for that username
         if (username == null) return null;
         if (!StringUtilities.isAlphanumeric(username)) {
             return null;
         }
         final List<RESTUserV1> users = reader.getUsersByName(username);
         return users != null && users.size() == 1 ? users.get(0) : null;
     }
 
     /**
      * Read from a csprocessor.cfg file and intitialise the variables into a configuration object.
      *
      * @param csprocessorcfg The csprocessor.cfg file.
      * @param cspConfig      The content spec configuration object to load the settings into
      * @throws FileNotFoundException The csprocessor.cfg couldn't be found
      * @throws IOException
      */
     public static void readFromCsprocessorCfg(final File csprocessorcfg,
             final ContentSpecConfiguration cspConfig) throws FileNotFoundException, IOException {
         final Properties prop = new Properties();
         prop.load(new FileInputStream(csprocessorcfg));
         cspConfig.setContentSpecId(Integer.parseInt(prop.getProperty("SPEC_ID")));
         cspConfig.setServerUrl(validateHost(prop.getProperty("SERVER_URL")));
         cspConfig.getZanataDetails().setServer(validateHost(prop.getProperty("ZANATA_URL")));
         cspConfig.getZanataDetails().setProject(prop.getProperty("ZANATA_PROJECT_NAME"));
         cspConfig.getZanataDetails().setVersion(prop.getProperty("ZANATA_PROJECT_VERSION"));
         cspConfig.setKojiHubUrl(validateHost(prop.getProperty("KOJI_HUB_URL")));
         cspConfig.setPublishCommand(prop.getProperty("PUBLISH_COMMAND"));
     }
 
     /**
      * Generates the contents of a csprocessor.cfg file from the passed arguments.
      *
      * @param contentSpec  The content specification object the csprocessor.cfg will be used for.
      * @param serverUrl    The server URL that the content specification exists on.
      * @param clientConfig TODO
      * @return The generated contents of the csprocessor.cfg file.
      */
     public static String generateCsprocessorCfg(final RESTTopicV1 contentSpec, final String serverUrl,
             final ClientConfiguration clientConfig, final ZanataDetails zanataDetails) {
         final StringBuilder output = new StringBuilder();
         output.append("# SPEC_TITLE=" + DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "\n");
         output.append("SPEC_ID=" + contentSpec.getId() + "\n");
         output.append("SERVER_URL=" + serverUrl + "\n");
         output.append("ZANATA_URL=" + (zanataDetails.getServer() == null ? "" : zanataDetails.getServer()) + "\n");
         output.append("ZANATA_PROJECT_NAME=" + (zanataDetails.getProject() == null ? "" : zanataDetails.getProject()) + "\n");
         output.append("ZANATA_PROJECT_VERSION=" + (zanataDetails.getVersion() == null ? "" : zanataDetails.getVersion()) + "\n");
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
             String[] fixedEnvVariables = envVariables;
             final Map<String, String> env = System.getenv();
             final List<String> envVars = new ArrayList<String>();
             for (final Entry<String, String> entry : env.entrySet()) {
                 final String key = entry.getKey();
                 if (!key.equals("XML_CATALOG_FILES")) envVars.add(key + "=" + entry.getValue());
             }
             if (envVariables != null) {
                 for (final String envVar : envVariables) {
                     envVars.add(envVar);
                 }
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
      * Opens a file using the Java Desktop API.
      *
      * @param file The file to be opened.
      * @throws Exception
      */
     public static void openFile(final File file) throws Exception {
         // Check that the file is a file
         if (!file.isFile()) throw new Exception("Passed file is not a file.");
 
         // Check that the Desktop API is supported
         if (!Desktop.isDesktopSupported()) {
             throw new Exception("Desktop is not supported");
         }
 
         final Desktop desktop = Desktop.getDesktop();
 
         // Check that the open functionality is supported
         if (!desktop.isSupported(Desktop.Action.OPEN)) {
             throw new Exception("Desktop doesn't support the open action");
         }
 
         // Open the file
         desktop.open(file);
     }
 
     /**
      * Builds a Content Specification list for a list of content specifications.
      */
     public static SpecList buildSpecList(final List<RESTTopicV1> specList, final RESTManager restManager,
             final ErrorLoggerManager elm) throws Exception {
         final List<Spec> specs = new ArrayList<Spec>();
         for (final RESTTopicV1 cs : specList) {
             RESTUserV1 creator = null;
             if (ComponentTopicV1.returnProperty(cs, CSConstants.ADDED_BY_PROPERTY_TAG_ID) != null) {
                 final List<RESTUserV1> users = restManager.getReader().getUsersByName(
                         ComponentTopicV1.returnProperty(cs, CSConstants.ADDED_BY_PROPERTY_TAG_ID).getValue());
                 if (users.size() == 1) {
                     creator = users.get(0);
                 }
             }
             final ContentSpecParser csp = new ContentSpecParser(elm, restManager);
             csp.parse(cs.getXml());
             final ContentSpec contentSpec = csp.getContentSpec();
             specs.add(new Spec(cs.getId(), cs.getTitle(), contentSpec.getProduct(), contentSpec.getVersion(),
                     creator != null ? creator.getName() : null, cs.getLastModified()));
         }
         return new SpecList(specs, specs.size());
     }
 
     /**
      * Generates the response output for a list of content specifications
      *
      * @param contentSpecs The SpecList that contains the processed Content Specifications
      * @return The generated response ouput.
      */
     public static String generateContentSpecListResponse(final SpecList contentSpecs) {
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
 
                 if (spec.getTitle().length() > sizes.get("TITLE")) {
                     sizes.put("TITLE", spec.getTitle().length());
                 }
 
                 if (spec.getVersion() != null && spec.getVersion().length() > sizes.get("VERSION")) {
                     sizes.put("VERSION", spec.getVersion().length());
                 }
 
                 if (spec.getCreator() != null && spec.getCreator().length() > sizes.get("CREATED BY")) {
                     sizes.put("CREATED BY", spec.getCreator().length());
                 }
             }
 
             final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
             final String format = "%" + (sizes.get("ID") + 2) + "s%" + (sizes.get("TITLE") + 2) + "s%" + (sizes.get(
                     "PRODUCT") + 2) + "s%" + (sizes.get("VERSION") + 2) + "s%" + (sizes.get("CREATED BY") + 2) + "s%" + (sizes.get(
                     "LAST MODIFIED") + 2) + "s";
 
             final StringBuilder output = new StringBuilder(
                     String.format(format, "ID", "TITLE", "PRODUCT", "VERSION", "CREATED BY", "LAST MODIFIED") + "\n");
             for (final Spec spec : contentSpecs.getSpecs()) {
                 output.append(String.format(format, spec.getId().toString(), spec.getTitle(), spec.getProduct(), spec.getVersion(),
                         spec.getCreator() == null ? "Unknown" : spec.getCreator(),
                         spec.getLastModified() == null ? "Unknown" : dateFormatter.format(spec.getLastModified())) + "\n");
             }
             return output.toString();
         }
         return "";
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
         connector.connectTo(validateHost(kojiHubUrl));
 
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
      * Fixes a Content Specs CHECKSUM to match the content of the Content Spec.
      *
      * @param contentSpec The content spec to be fixed.
      * @return The fixed Content Specifcation.
      */
     public static String fixContentSpecChecksum(final String contentSpec) {
         String contentSpecData = contentSpec.replaceFirst("CHECKSUM[ ]*=.*(\r)?\n", "");
         final String checksum = HashUtilities.generateMD5(contentSpecData);
 
         return "CHECKSUM=" + checksum + "\n" + contentSpecData;
     }
 
     public static RESTLogDetailsV1 createLogDetails(final String message, final boolean isRevisionHistoryMessage) {
         return createLogDetails(null, message, isRevisionHistoryMessage);
     }
 
     public static RESTLogDetailsV1 createLogDetails(final RESTUserV1 user, final String message, final boolean isRevisionHistoryMessage) {
         RESTLogDetailsV1 logDetails = null;
         if (message != null) {
             logDetails = new RESTLogDetailsV1();
             logDetails.setMessage(message);
             logDetails.setUser(user);
             if (isRevisionHistoryMessage) {
                 logDetails.setFlag(0 | RESTLogDetailsV1.MAJOR_CHANGE_FLAG_BIT);
             } else {
                 logDetails.setFlag(0 | RESTLogDetailsV1.MINOR_CHANGE_FLAG_BIT);
             }
         }
 
         return logDetails;
     }
 
     /**
      * Validate that a Language is a valid language as defined by the server.
      *
      * @param restManager
      * @param lang        The language to be validated.
      * @return True if the language exists on the server otherwise false.
      */
     public static boolean validateLanguage(final BaseCommand command, final RESTManager restManager, final String lang) {
         return validateLanguages(command, restManager, new String[]{lang});
     }
 
     /**
      * Validate that a Language is a valid language as defined by the server.
      *
      * @param restManager
      * @param langs       The languages to be validated.
      * @return True if the language exists on the server otherwise false.
      */
     public static boolean validateLanguages(final BaseCommand command, final RESTManager restManager, final String[] langs) {
         final RESTStringConstantV1 localesConstant = restManager.getRESTClient().getJSONStringConstant(
                 CommonConstants.LOCALES_STRING_CONSTANT_ID, "");
         final List<String> locales = CollectionUtilities.replaceStrings(CollectionUtilities.sortAndReturn(
                 CollectionUtilities.toArrayList(localesConstant.getValue().split("[\\s\r\n]*,[\\s\r\n]*"))), "_", "-");
 
         boolean valid = true;
         for (final String lang : langs) {
             if (!locales.contains(lang)) {
                 command.printError(
                        String.format(Constants.ERROR_INVALID_LOCALE_MSG, lang, localesConstant.getValue().replaceAll("\r|\n", " ")),
                         false);
                 valid = false;
             }
         }
 
         return valid;
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
         this.console = null;
         this.outStream = null;
     }
 
     public InputStreamHandler(final InputStream stream, final Console console) {
         this.stream = stream;
         this.buffer = null;
         this.console = console;
         this.outStream = null;
     }
 
     public InputStreamHandler(final InputStream stream, final OutputStream outStream) {
         this.stream = stream;
         this.buffer = null;
         this.console = null;
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
