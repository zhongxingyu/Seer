 package es.upm.dit.gsi.beast.reader.system;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import es.upm.dit.gsi.beast.exception.BeastException;
 import es.upm.dit.gsi.beast.reader.Reader;
 
 /**
  * Project: beast File: es.upm.dit.gsi.beast.reader.system.SystemReader.java
  * 
  * Main class to transform the plain text given by the client to the necessary
  * classes to run each Test. These classes are the Scenario, Setup and
  * Evaluation, which emulate the GIVEN, WHEN and THEN parts of the plain text;
  * the .story file whit the plain text of the test and the its .java class with
  * the same name to interpret it. Furthermore, one casemanager must be created,
  * which will run all the tests.
  * 
  * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
  * Telemáticos Universidad Politécnica de Madrid (UPM)
  * 
  * @author Alberto Mardomingo
  * @author Jorge Solitario
  * 
  * @author alvarocarrera
  * @email a.carrera@gsi.dit.upm.es
  * @twitter @alvarocarrera
  * @date 21/06/2013
  * @version 0.3
  * 
  */
 public class SystemReader extends Reader {
 
     private static Logger logger = Logger.getLogger(SystemReader.class
             .getName());
 
     /**
      * Main method of the class, which handles the process of creating the tests
      * 
      * @param requirementsFolder
      *            , it is the folder where the plain text given by the client is
      *            stored
      * @param platformName
      *            , to choose the MAS platform (JADE, JADEX, etc.)
      * @param src_test_dir
      *            , the folder where our classes are created
      * @param tests_package
      *            , the name of the package where the stories are created
      * @param casemanager_package
      *            , the path where casemanager must be created
      * @param loggingPropFile
      *            , properties file
      * @throws Exception
      *             , if any error is found in the configuration
      */
     public static void generateJavaFiles(String requirementsFolder,
             String platformName, String src_test_dir, String tests_package,
             String casemanager_package, String loggingPropFile)
             throws Exception {
 
         File reqFolder = new File(requirementsFolder);
         if (reqFolder.isDirectory()) {
             for (File f : reqFolder.listFiles()) {
                 if (f.getName().endsWith(".story")) {
                     try {
                         SystemReader.generateJavaFilesForOneStory(
                                 f.getCanonicalPath(), platformName,
                                 src_test_dir, tests_package,
                                 casemanager_package, loggingPropFile);
                     } catch (IOException e) {
                         String message = "ERROR: " + e.getMessage();
                         logger.severe(message);
                         throw new BeastException(message, e);
                     }
                 }
             }
             for (File f : reqFolder.listFiles()) {
                 if (f.isDirectory()) {
                     SystemReader.generateJavaFiles(requirementsFolder
                             + File.separator + f.getName(), platformName,
                             src_test_dir, tests_package + "." + f.getName(),
                             casemanager_package, loggingPropFile);
                 }
             }
         } else if (reqFolder.getName().endsWith(".story")) {
             SystemReader.generateJavaFilesForOneStory(requirementsFolder,
                     platformName, src_test_dir, tests_package,
                     casemanager_package, loggingPropFile);
         } else {
             String message = "No story file found in " + requirementsFolder;
             logger.severe(message);
             throw new BeastException(message);
         }
 
     }
 
     /**
      * Main method of the class, which handles all the process to create all
      * tests.
      * 
      * @param storyFilePath
      *            , it is the folder where the plain text given by the client is
      *            stored
      * @param platformName
      *            , to choose the MAS platform (JADE, JADEX, etc.)
      * @param src_test_dir
      *            , the folder where our classes are created
      * @param tests_package
      *            , the name of the package where the stories are created
      * @param casemanager_package
      *            , the path where casemanager must be created
      * @param loggingPropFile
      *            , properties file
      * @throws Exception
      *             , if any error is found in the configuration
      */
     public static void generateJavaFilesForOneStory(String storyFilePath,
             String platformName, String src_test_dir, String tests_package,
             String casemanager_package, String loggingPropFile)
             throws Exception {
 
         /*
          * This map has the following structure: {Scenario1ID =>
          * [GivenDescription1, WhenDescription1, ThenDescription1], Scenario2ID
          * => [GivenDescription2, WhenDescription2, ThenDescription2], ...}
          */
         HashMap<String, String[]> scenarios = new HashMap<String, String[]>();
         String storyName = null;
         String story_user = null;
         String user_feature = null;
         String user_benefit = null;
         BufferedReader fileReader = createFileReader(storyFilePath);
         if (fileReader == null) {
             logger.severe("ERROR Reading the file " + storyFilePath);
         } else {
             // Starting with the CaseManager
             // Shall I perish, may $Deity have mercy on my soul, and on those
             // who should finish this
             File caseManager = CreateSystemCaseManager.startSystemCaseManager(
                     casemanager_package, src_test_dir);
             try {
                 String nextLine = null;
                 // TYPES:
                 // As a -> 1
                 // I want to -> 2
                 // So that -> 3
                 int lineType = 0;
                 while ((nextLine = fileReader.readLine()) != null) {
 
                     // Again, why am I using class variables to store the data
                     // I only fucking use in this method?
 
                     if (nextLine.startsWith("Story")) {
                         String aux = nextLine.replaceFirst("Story", "").trim();
                         if (aux.startsWith(":") || aux.startsWith("-")) {
                             aux = aux.substring(1).trim();
                         }
                         storyName = aux;
                     } else if (nextLine.startsWith("As a")) {
                         story_user = nextLine.replaceFirst("As a", "").trim();
                         lineType = 1;
                     } else if (nextLine.startsWith("I want to")) {
                         user_feature = nextLine.replaceFirst("I want to", "")
                                 .trim();
                         lineType = 2;
                     } else if (nextLine.startsWith("So that")) {
                         user_benefit = nextLine.replaceFirst("So that", "")
                                 .trim();
                         lineType = 3;
                     } else if (nextLine.startsWith("And")) {
                         switch (lineType) {
                         case 1:
                             story_user= story_user + " and " + nextLine.replaceFirst("And", "").trim();
                             break;
                         case 2:
                             user_feature= user_feature + " and " + nextLine.replaceFirst("And", "").trim();                            
                             break;
                         case 3:
                             user_benefit= user_benefit + " and " + nextLine.replaceFirst("And", "").trim();                            
                             break;
                         default:
                             break;
                         }
                     } else if (nextLine.startsWith("Scenario")) {
 
                         // I am assuming that the file is properly formated
                         // TODO: Check that it actually is properly formated.
 
                         String aux = nextLine.replaceFirst("Scenario", "").trim();
                         if (aux.startsWith(":") || aux.startsWith("-")) {
                             aux = aux.substring(1).trim();
                         }
                         aux.toLowerCase();
                         String scenarioID = createClassName(aux);
                         while (!fileReader.ready()) {
                             Thread.yield();
                         }
                         nextLine = fileReader.readLine();
                         String givenDescription = nextLine.replaceFirst(
                                 "Given", "").trim();
 
                         while (!fileReader.ready()) {
                             Thread.yield();
                         }
                         nextLine = fileReader.readLine();
                         while (nextLine.startsWith("And")) {
                             givenDescription = givenDescription + " and " + nextLine.replaceFirst("And", "").trim();
                             while (!fileReader.ready()) {
                                 Thread.yield();
                             }
                             nextLine = fileReader.readLine();
                         }
                         String whenDescription = nextLine.replaceFirst("When",
                                 "").trim();
 
                         while (!fileReader.ready()) {
                             Thread.yield();
                         }
                         nextLine = fileReader.readLine();
                         while (nextLine.startsWith("And")) {
                             whenDescription = whenDescription + " and " + nextLine.replaceFirst("And", "").trim();
                             while (!fileReader.ready()) {
                                 Thread.yield();
                             }
                             nextLine = fileReader.readLine();
                         }
                         String thenDescription = nextLine.replaceFirst("Then",
                                 "").trim();
                         
                         nextLine = fileReader.readLine();
                         
                         while (nextLine!=null && nextLine.startsWith("And")) {
                             thenDescription = thenDescription +  " and " + nextLine.replaceFirst("And", "").trim();
                             nextLine = fileReader.readLine();
                         }
 
                         String[] scenarioData = new String[3];
                         scenarioData[0] = givenDescription;
                         scenarioData[1] = whenDescription;
                         scenarioData[2] = thenDescription;
 
                         scenarios.put(scenarioID, scenarioData);
                     } else if (!nextLine.trim().isEmpty()) {
                         // Is not an empty line, but has not been recognized.
                         logger.severe("ERROR: The test writen in the plain text can not be handed");
                         logger.severe("Try again whit the following key-words: {Story -,"
                                 + " As a, I want to, So that, Scenario:, Given, When, Then}");
                     } // The only possibility here is to get an empty line,
                       // so I don't have to do anything.
                 }
 
                 fileReader.close();
 
                 // Now, I should have all the variables set.
 
                 if (storyName != null) {
                     // // I have a story, so...
                     if (fileDoesNotExist(storyName + ".java", tests_package,
                             src_test_dir)) {
                         CreateSystemTestSuite.createSystemTestSuite(storyName,
                                 platformName, tests_package, src_test_dir,
                                 loggingPropFile, story_user, user_feature,
                                 user_benefit, scenarios);
                     }
                     CreateSystemCaseManager.addStory(caseManager, storyName,
                             tests_package, story_user, user_feature,
                             user_benefit);
 
                 } else {
 
                     // This should not happen, since this class should only be
                     // used
                     // to create System tests, (i.e. "story" should never be
                     // null)
 
                     logger.severe("ERROR: No Story found in :" + storyFilePath);
 
                 }
 
                 CreateSystemCaseManager.closeSystemCaseManager(caseManager);
 
             } catch (Exception e) {
                 logger.severe("ERROR: " + e.getMessage());
                 throw e;
             }
         }
     }
 
     /**
      * Main method to start reading the plain text given by the client
      * 
      * @param args
      *            , where arg[0] is Beast configuration file (beast.properties)
      *            and arg[1] is Logger configuration file (logger.properties)
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         Logger logger = Logger.getLogger("MASReader.main");
 
         Properties beastConfigProperties = new Properties();
         String beastConfigPropertiesFile = null;
         if (args.length > 0) {
             beastConfigPropertiesFile = args[0];
             beastConfigProperties.load(new FileInputStream(
                     beastConfigPropertiesFile));
             logger.info("Properties file selected -> "
                     + beastConfigPropertiesFile);
         } else {
             logger.severe("No properties file found. Set the path of properties file as argument.");
             throw new BeastException(
                     "No properties file found. Set the path of properties file as argument.");
         }
         String loggerConfigPropertiesFile;
         if (args.length > 1) {
             Properties loggerConfigProperties = new Properties();
             loggerConfigPropertiesFile = args[1];
             try {
                 FileInputStream loggerConfigFile = new FileInputStream(
                         loggerConfigPropertiesFile);
                 loggerConfigProperties.load(loggerConfigFile);
                 LogManager.getLogManager().readConfiguration(loggerConfigFile);
                 logger.info("Logging properties configured successfully. Logger config file: "
                         + loggerConfigPropertiesFile);
             } catch (IOException ex) {
                 logger.warning("WARNING: Could not open configuration file");
                 logger.warning("WARNING: Logging not configured (console output only)");
             }
         } else {
             loggerConfigPropertiesFile = null;
         }
 
         SystemReader.generateJavaFiles(
                 beastConfigProperties.getProperty("requirementsFolder"), "\""
                         + beastConfigProperties.getProperty("MASPlatform")
                         + "\"",
                 beastConfigProperties.getProperty("srcTestRootFolder"),
                 beastConfigProperties.getProperty("storiesPackage"),
                 beastConfigProperties.getProperty("caseManagerPackage"),
                 loggerConfigPropertiesFile,
                 beastConfigProperties.getProperty("specificationPhase"));
 
     }
 }
