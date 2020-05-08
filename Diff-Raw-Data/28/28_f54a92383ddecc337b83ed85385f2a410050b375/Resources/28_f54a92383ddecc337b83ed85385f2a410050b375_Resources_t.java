 package tng;
 
 public class Resources
 {
 	// Command line arguments
 	public static String dbName;
 	public static String repository;
 	public static String branch;
 	public static String configFile;
 	public static String repositoryName;
 	
 	// Config file constants
 	public static final String classpath = "--ClassFolders";
 	public static final String sourcepath = "--SourceFolders";
 	
 	// Encoding to use
 	public static final String encoding = "UTF-8";
 	
 	// Diff regex
 	public static final String diffOldFile = 	"^---.*";
 	public static final String diffNewFile = 	"^\\+\\+\\+.*";
 	public static final String diffHunkRange = 	"@@.*@@.*";
 	public static final String diffOldRange = 	"-[0-9]+,?[0-9]*";
 	public static final String diffNewRange = 	"\\+[0-9]+,?[0-9]*";
 	
 	// Git log regex
 	public static final String gitLogCommit = "commit [a-z0-9]+";
 	
	// Git HEAD regex
	public static final String gitHead = "[a-z0-9]+";
	
 	// Database
 	public static final String eggnetDB = "eggnet";
 	
 	// Git blame regex
 	//71a4479b (<jordan.ell7@gmail.com> 2012-07-18 15:08:52 -0700   3) import java.io.
 	public static final String gitBlame = "\\<(.+?)\\>";
 }
