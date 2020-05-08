 package common;
 
 import static org.junit.Assert.fail;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import voldemort.VoldemortTestResult;
 
 /**
  * Revision contains 1. a reference to its repository 2. commit id 3. a set of
  * its parents' commit ids 4. diff files between this revision and each of its
  * parent 5. compilable flag 6. test result
  */
 public class Revision implements Serializable {
     /**
      * serial version ID
      */
     private static final long serialVersionUID = -4044614975764741642L;
 
     public enum COMPILABLE {
         YES, NO, UNKNOWN
     }
 
     // TODO: A revision should know about its parents -- i.e., a list of
     // Revision objects that have it as a child. And, a Revision should know
     // about its children, too.
 
     private final Repository repository;
     private final String commitID;
     /**
      * index mapping : a parent revision -> a list of files that are different
      * between the parent and this revision
      **/
     private final List<Revision> parents;
     private final List<List<DiffFile>> diffFiles;
     private COMPILABLE compilable;
     private/* @Nullable */TestResult testResult;
 
     /**
      * create a revision initially, compilable flag is unknown and test result
      * is null
      */
     public Revision(Repository repository, String commitID) {
         this.repository = repository;
         this.commitID = commitID;
         this.parents = new ArrayList<Revision>();
         this.diffFiles = new ArrayList<List<DiffFile>>();
         compilable = COMPILABLE.UNKNOWN;
         testResult = null;
     }
     
     public Repository getRepository() {
     	return repository;
     }
 
     public String getCommitID() {
         return commitID;
     }
     
     public void addParent(Revision parent, List<DiffFile> files) {
     	parents.add(parent);
     	diffFiles.add(files);
     }
     
     public List<Revision> getParents() {
     	return parents;
     }
 
     public List<DiffFile> getDiffFiles(Revision parent) {
     	int i = parents.indexOf(parent);
     	assert i >= 0;
         return diffFiles.get(i);
     }
 
     public COMPILABLE isCompilable() {
     	if (compilable == COMPILABLE.UNKNOWN) {
 			compile();
 		}
 		return compilable;
     }
     
     public TestResult getTestResult() {
 		if (compilable == COMPILABLE.UNKNOWN || 
 				(compilable == COMPILABLE.YES && testResult == null)) {
 			runAllTests();
 		}
 		return testResult;
 	}
 	
     /**
      * compile this revision and set compilable flag
      */
 	public void compile() {
 		boolean build =  build(repository.getBuildCommand());
 		boolean buildtest = build(repository.getBuildtestCommand());
 		
 		if (build && buildtest) {
 			compilable = COMPILABLE.YES;
 		} else {
 			compilable = COMPILABLE.NO;
 		}
 	}
 	
 	/**
 	 * run all junit tests on this revision, set compilable flag and test result
 	 */
 	public void runAllTests() {
 		testResult = run(repository.getRunJunitCommand());
 		
 		if (testResult == null) {
 			compilable = COMPILABLE.NO;
 		} else {
 			compilable = COMPILABLE.YES;
 		}
 	}
 	
 	/**
 	 * Helper method for compile()
 	 * @param command
 	 * @return true if build successful, false if build failed
 	 */
 	private boolean build(String[] command) {
 		int exitValue = repository.checkoutCommit(commitID);
         assert (exitValue == 0);
 
         Process process = Util.runProcess(command,
                 repository.getDirectory());
 
         BufferedReader stdOutputReader = new BufferedReader(
                 new InputStreamReader(process.getInputStream()));
 
         BufferedReader stdErrorReader = new BufferedReader(
                 new InputStreamReader(process.getErrorStream()));
 
         List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
         List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);
         
         return buildSuccessful(outputStreamContent, errorStreamContent);
 	}
 	
 	/**
 	 * Helper method for runAllTests()
 	 * @param command
 	 * @return test result
 	 */
 	private TestResult run(String[] command) {
 		int exitValue = repository.checkoutCommit(commitID);
         assert (exitValue == 0);
 
         Process process = Util.runProcess(command,
                 repository.getDirectory());
 
         BufferedReader stdOutputReader = new BufferedReader(
                 new InputStreamReader(process.getInputStream()));
 
         BufferedReader stdErrorReader = new BufferedReader(
                 new InputStreamReader(process.getErrorStream()));
 
         List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
         List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);
         
         if (buildSuccessful(outputStreamContent, errorStreamContent)) {
         	return new VoldemortTestResult(commitID, outputStreamContent, errorStreamContent);
         }
         
         return null;
 	}
 	
 	/**
 	 * Helper method for build(command)
      * @return true if build successful, false if build failed
      */
     private boolean buildSuccessful(List<String> outputStreamContent, List<String> errorStreamContent) {
     	Pattern buildSuccessfulPattern = Pattern.compile("BUILD SUCCESSFUL");
         Pattern buildFailedPattern = Pattern.compile("BUILD FAILED");
         
         for (String line : outputStreamContent) {
             Matcher buildSuccessfulMatcher = buildSuccessfulPattern.matcher(line);
             if (buildSuccessfulMatcher.find()) {
                 return true;
             }
         }
         
         for (String line : errorStreamContent) {
             Matcher buildFailedMatcher = buildFailedPattern.matcher(line);
             if (buildFailedMatcher.find()) {
                 return false;
             }
         }
 
         fail("Neither BUILD SUCCESSFUL nor BUILD FAILED found");
         return false;
     }
 
     @Override
     public boolean equals(Object other) {
         if (other == null || !other.getClass().equals(this.getClass())) {
             return false;
         }
 
         Revision revision = (Revision) other;
 
         return repository.equals(revision.repository)
                 && commitID.equals(revision.commitID)
                && parents.equals(revision.parents)
                 && diffFiles.equals(revision.diffFiles)
                 && compilable == revision.compilable
                 && ((testResult == null && revision.testResult == null) || testResult
                         .equals(revision.testResult));
     }
 
     @Override
     public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() 
        		+ 17 * parents.hashCode() + 19 * diffFiles.hashCode() + 23 * compilable.hashCode();
         if (testResult != null) {
            code += 29 * testResult.hashCode();
         }
 
         return code;
     }
 
     @Override
     public String toString() {
         String result = "commit : " + commitID + "\n";
         result += "compilable : ";
         if (compilable == COMPILABLE.YES) {
             result += "yes\n";
             result += testResult.toString();
         } else if (compilable == COMPILABLE.NO) {
             result += "no\n";
         } else {
             result += "unknown\n";
         }
         
         for (int i = 0; i < parents.size(); i++) {
         	result += "parent : " + parents.get(i).getCommitID() + "\n";
         	result += "diff files :\n";
         	List<DiffFile> files = diffFiles.get(i);
         	
         	for (DiffFile file : files) {
                 result += file + "\n";
             }
         }
 
         return result;
     }
 }
