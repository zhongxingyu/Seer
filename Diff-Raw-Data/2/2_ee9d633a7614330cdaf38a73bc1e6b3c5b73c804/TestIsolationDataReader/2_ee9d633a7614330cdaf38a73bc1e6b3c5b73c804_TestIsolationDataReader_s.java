 import git.GitRepository;
 
 import java.io.File;
 
 import ant.AntBuildStrategy;
 
 import plume.Option;
 import plume.OptionGroup;
 import plume.Options;
 
 import common.BuildStrategy;
 import common.HistoryGraph;
 import common.MixingTool;
 import common.Repository;
 import common.Util;
 
 public class TestIsolationDataReader {
 
 	/**
      * Print the short usage message.
      */
     @OptionGroup("General Options")
     @Option(value = "-h Print short usage message", 
     		aliases = { "-help" })
     public static boolean showHelp = false;
 
     /**
      * Full path to the directory containing serialized revision files.
      */
     @Option(value = "-z Directory containing serialized revision files (Required)", 
     		aliases = { "-serializedRevisionsDir" })
     public static String serializedRevisionsDirName = null;
     
     /**
 	 * Full path to the repository directory.
 	 */
 	@Option(value = "-r Full path to the repository directory (Required)",
 	        aliases = { "-repoPath" })
 	public static String repoPath = null;
 	
 	/**
 	 * Full path to the cloned repository directory.
 	 */
	@Option(value = "-r Full path to the cloned repository directory (Required)",
 	        aliases = { "-clonedRepoPath" })
 	public static String clonedRepoPath = null;
 
 	/**
 	 * Type of the repository. Default is Git.
 	 */
 	@Option(value = "-R repository type (Optional)", 
 			aliases = { "-repoType" })
 	public static String repoType = "Git";
 
 	/**
 	 * Build tool used by the project. Default is Ant.
 	 */
 	@Option(value = "-B build tool (Optional)", 
 			aliases = { "-buildTool" })
 	public static String buildTool = "Ant";
 
 	/**
 	 * Build command. Default is 'ant'.
 	 */
 	@Option(value = "-b build command (Optional)", aliases = { "-buildCommand" })
 	public static String buildCommand = "ant";
 
 	/**
 	 * Command to compile the project and run all unit tests.
 	 */
 	@Option(value = "-t test command (Required)", aliases = { "-testCommand" })
 	public static String testCommand = null;
 
 	/** One line synopsis of usage */
 	public static final String usage_string = "TestIsolationDataReader [options]";
 
 	/**
 	 * Initial program entrance -- reconstruct a HistoryGraph from 
 	 * serialized revision files from an input directory.
 	 * 
 	 * @param args : command line arguments.
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		Options plumeOptions = new Options(TestIsolationDataReader.usage_string, TestIsolationDataReader.class);
 	    plumeOptions.parse_or_usage(args);
 	
 	    // Display the help screen.
 	    if (showHelp) {
 	        plumeOptions.print_usage();
 	        return;
 	    }
 	    
 	    if (serializedRevisionsDirName == null 
 	    		|| repoPath == null || clonedRepoPath == null 
 	    		|| testCommand == null) {
 	    	plumeOptions.print_usage();
 	    	return;
 	    }
 	    
 	    File repoDir = new File(repoPath);
 	    File clonedRepoDir = new File(clonedRepoPath);
         
         Repository repository = null;
         Repository clonedRepository = null;
         BuildStrategy buildStrategy = null;
         BuildStrategy clonedBuildStrategy = null;
         
         if (buildTool.equals(TestIsolationDataGenerator.ANT)) {
         	buildStrategy = new AntBuildStrategy(repoDir, buildCommand, testCommand);
         	clonedBuildStrategy = new AntBuildStrategy(clonedRepoDir, buildCommand, testCommand);
         }
         
         if (repoType.equals(TestIsolationDataGenerator.GIT)) {
         	repository = new GitRepository(repoDir, buildStrategy);
         	clonedRepository = new GitRepository(clonedRepoDir, clonedBuildStrategy);
         }
         
         assert buildStrategy != null && repository != null 
         		&& clonedBuildStrategy != null && clonedRepository != null;
 	    
 	    File serializedRevisionsDir = new File(serializedRevisionsDirName);
 	    
 	    HistoryGraph historyGraph = Util.reconstructHistoryGraph(serializedRevisionsDir, repository);
 	    	    
 	    MixingTool mixing = new MixingTool(historyGraph, clonedRepository);
 	    mixing.run();
 	}
 }
