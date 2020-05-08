 import java.io.IOException;
 import java.util.Iterator;
 
 import common.HistoryGraph;
 import common.Repository;
 import common.Revision;
 import common.Util;
 
 import plume.Option;
 import plume.OptionGroup;
 import plume.Options;
 
 /**
  * TestIsolationDataGenerator builds a HistoryGraph from voldemort repository
  * and writes each Revision in the graph to a serialized file.
  */
 public class TestIsolationDataGenerator {
     // Prefix of files to which HistoryGraph instances are written.
     public static final String FILE_PREFIX = "historyGraph";
 
     // Extension of serialized files.
     public static final String SERIALIZED_EXTENSION = ".ser";
 
     // Extension of human-readable files.
     public static final String HUMAN_READ_EXTENSION = ".log";
 
     /**
      * Print the short usage message.
      */
     @OptionGroup("General Options")
     @Option(value = "-h Print short usage message", aliases = { "-help" })
     public static boolean showHelp = false;
 
     /**
      * The ant command used for running ant. By default this is just 'ant'.
      */
     @Option(value = "-a ant command (Optional)", aliases = { "-antCommand" })
     public static String antCommand = "ant";
 
     /**
      * The commit ID from which to begin the HistoryGraph analysis.
      */
     @Option(
             value = "-S Starting commit ID for HistoryGraph analysis (Required)",
             aliases = { "-startHGraphID" })
     public static String startHGraphID = null;
 
     /**
      * The commit ID from which to begin the TestResult analysis.
      */
     @Option(value = "-s Starting commit ID for TestResult analysis (Optional)",
             aliases = { "-startTResultID" })
     public static String startTResultID = null;
 
     /**
      * The commit ID where the TestResult analysis should terminate.
      */
     @Option(value = "-e Ending commit ID for TestResult analysis (Optional)",
             aliases = { "-endTResultID" })
     public static String endTResultID = null;
 
     /**
      * Full path to the repository directory.
      */
     @Option(value = "-r Full path to the repository directory (Required)",
             aliases = { "-repoDir" })
     public static String repositoryDirName = null;
 
     /**
      * Full path to the output directory.
      */
     @Option(value = "-o Full path to the output directory (Required)",
             aliases = { "-outputDir" })
     public static String outputDirName = null;
 
     /** One line synopsis of usage */
     public static final String usage_string = "TestIsolationDataGenerator [options]";
 
     /**
      * Initial program entrance -- parses the arguments and runs the data
      * extraction.
      * 
      * @param args
      *            : command line arguments.
      * @throws IOException
      */
     public static void main(String[] args) throws IOException {
         Options plumeOptions = new Options(
                 TestIsolationDataGenerator.usage_string,
                 TestIsolationDataGenerator.class);
         plumeOptions.parse_or_usage(args);
 
         // Display the help screen.
         if (showHelp) {
             plumeOptions.print_usage();
             return;
         }
 
         if (startHGraphID == null || repositoryDirName == null
                 || outputDirName == null) {
             plumeOptions.print_usage();
             return;
         }
 
         Repository repository = new Repository(repositoryDirName, antCommand);
         HistoryGraph historyGraph = repository.buildHistoryGraph(startHGraphID);
 
         if (startTResultID != null && endTResultID != null) {
             populateTestResults(historyGraph);
         }
 
         String fileName = "";
         if (startTResultID != null && endTResultID != null) {
            // TODO: Bug -- startHGraphID should be startTResultID.
             fileName = "_" + startHGraphID + "_" + endTResultID;
         }
 
         Util.writeToSerializedFile(outputDirName + FILE_PREFIX + fileName
                 + SERIALIZED_EXTENSION, historyGraph);
         Util.writeToHumanReadableFile(outputDirName + FILE_PREFIX + fileName
                 + HUMAN_READ_EXTENSION, historyGraph);
     }
 
     /**
      * Construct a TestResult instance for each revision in a specified range in
      * historyGraph.
      * 
      * @modifies historyGraph
      */
     public static void populateTestResults(HistoryGraph historyGraph) {
         Iterator<Revision> itr = historyGraph.iterator();
         Revision revision = null;
 
         // Search for start revision.
         while (itr.hasNext()) {
             revision = itr.next();
             if (revision.getCommitID().equals(startTResultID)) {
                 break;
             }
         }
 
         // We could not find the start revision.
         if (revision == null || !revision.getCommitID().equals(startTResultID)) {
             return;
         }
 
         // For each revision between start and end IDs, get the test results,
         // and record them to a serialized file.
         while (true) {
             revision.getTestResult();
 
             String filename = outputDirName + revision.getCommitID()
                     + SERIALIZED_EXTENSION;
             Util.writeToSerializedFile(filename, revision);
 
             if (revision.getCommitID().equals(endTResultID)) {
                 return;
             }
 
             if (!itr.hasNext()) {
                 break;
             }
             revision = itr.next();
         }
     }
 }
