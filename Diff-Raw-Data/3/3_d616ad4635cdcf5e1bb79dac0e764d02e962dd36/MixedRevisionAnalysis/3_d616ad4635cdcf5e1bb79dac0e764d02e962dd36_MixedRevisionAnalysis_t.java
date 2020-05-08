 package histaroach.algorithm;
 
 import histaroach.model.DiffFile;
 import histaroach.model.MixedRevision;
 import histaroach.model.Revision;
 import histaroach.model.Revision.Compilable;
 import histaroach.model.TestResult;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 
 /**
  * MixedRevisionAnalysis performs file manipulation to create actual 
  * mixed revisions on the file system, runs tests on them and records 
  * the results and all data necessary for Histaroach analysis.
  */
 public class MixedRevisionAnalysis {
 		
 	private static final String COLUMN_SEPARATOR = ";";
 	private static final String FILE_SEPARATOR = ",";
 	
 	private static final String HEADER = 
 		"mixedRevisionID;parentCommitID;childCommitID;delta;" + 
 		"compilable;testAborted;test;mixedTestResult;parentTestResult;childTestResult\n";
 	
 	private static final String TRUE = "1";
 	private static final String FALSE = "0";
 	private static final String NONE = "n";
 
 	private final List<MixedRevision> mixedRevisions;
 	
 	public MixedRevisionAnalysis(List<MixedRevision> mixedRevisions) {
 		this.mixedRevisions = mixedRevisions;
 	}
 	
 	/**
 	 * For all MixedRevisions in mixedRevisions, creates actual mixed revisions 
 	 * on the file system, runs tests on them and records the results to 
 	 * an output file.
 	 * 
 	 * @throws Exception
 	 */
 	public void runTestOnMixedRevisions(File outputFile) throws Exception {
 		runTestOnMixedRevisions(0, mixedRevisions.size(), outputFile);
 	}
 	
 	/**
 	 * For a specified range in mixedRevisions, creates actual mixed revisions 
 	 * on the file system, runs tests on them and records the results to 
 	 * an output file.
 	 * 
 	 * @throws Exception
 	 */
 	public void runTestOnMixedRevisions(int startIndex, int numElements, 
 			File outputFile) throws Exception {
 		FileWriter fstream = new FileWriter(outputFile);
 		BufferedWriter out = new BufferedWriter(fstream);
 		out.write(HEADER);
 		out.flush();
 		
 		for (int i = startIndex; i < startIndex + numElements; i++) {
 			// mixedRevision already has its revertedFiles set
 			MixedRevision mixedRevision = mixedRevisions.get(i);
 			
 			mixedRevision.checkoutBaseRevision();
 			mixedRevision.revertFiles();
 			mixedRevision.runTest();
 			
 			String lines = analyzeMixedRevision(mixedRevision, i);
 			out.write(lines);
 			out.flush();
 			
 			mixedRevision.restoreBaseRevision();
 		}
 		
 		out.close();
 	}
 	
 	/**
 	 * Records data of mixedRevision.
 	 * 
 	 * @return a String representation of data of mixedRevision.
 	 */
 	public String analyzeMixedRevision(MixedRevision mixedRevision, 
 			int mixedRevisionID) {
 		String lines = "";
 		
 		Revision child = mixedRevision.getBaseRevision();
 		TestResult childTestResult = child.getTestResult();
 		
 		Map<Set<DiffFile>, Revision> revertedFileRecords = 
 			mixedRevision.getRevertedFileRecords();
 		
 		for (Map.Entry<Set<DiffFile>, Revision> entry : revertedFileRecords.entrySet()) {
 			Set<DiffFile> revertedFiles = entry.getKey();
 			
 			Revision parent = entry.getValue();
 			TestResult parentTestResult = parent.getTestResult();
 			
 			Set<DiffFile> totalDiffFiles = child.getDiffFiles(parent);
 			
 			// mixedRevisionID parentCommitID childCommitID
 			String lineHeader = mixedRevisionID + COLUMN_SEPARATOR + 
 				parent.getCommitID() + COLUMN_SEPARATOR + 
 				child.getCommitID();
 			
 			// ?file1,?file2,...,?fileN
 			String lineDelta = getLineDelta(revertedFiles, totalDiffFiles);
 			
 			if (mixedRevision.isCompilable() == Compilable.YES && 
 					!mixedRevision.hasTestAborted()) {
 				TestResult mixedTestResult = mixedRevision.getTestResult();
 				assert mixedTestResult != null;
 			
 				for (String test : childTestResult.getAllTests()) {
 					lines += getFullLine(lineHeader, lineDelta, test, 
 							mixedTestResult, childTestResult, parentTestResult);
 				}
 			} else {
 				lines += getFullLineNoTestResult(lineHeader, lineDelta, mixedRevision);
 			}
 		}
 		
 		return lines;
 	}
 	
 	/**
 	 * Line format: 
 	 * mixedRevisionID parentCommitID childCommitID delta 
 	 * compilable testAborted test mixedTestResult parentTestResult childTestResult
 	 */
 	private String getFullLine(String lineHeader, String lineDelta, 
 			String test, TestResult mixedTestResult, 
 			TestResult parentTestResult, TestResult childTestResult) {
 		String line = "";
 		
 		line += lineHeader + COLUMN_SEPARATOR;
 		line += lineDelta + COLUMN_SEPARATOR;
 				
 		// compilable testAborted
 		line += TRUE + COLUMN_SEPARATOR + FALSE + COLUMN_SEPARATOR;
 		
 		// test mixedTestResult parentTestResult childTestResult
 		line += getLineTestResults(test, mixedTestResult, parentTestResult, 
 				childTestResult) + "\n";
 		
 		return line;
 	}
 	
 	/**
 	 * Line format: 
 	 * mixedRevisionID parentCommitID childCommitID delta 
 	 * compilable testAborted n n n n
 	 */
 	private String getFullLineNoTestResult(String lineHeader, 
 			String lineDelta, MixedRevision mixedRevision) {
 		String line = "";
 		
 		line += lineHeader + COLUMN_SEPARATOR;
 		line += lineDelta + COLUMN_SEPARATOR;
 		
 		// compilable
 		line += (mixedRevision.isCompilable() == Compilable.YES ? 
 				TRUE : FALSE) + COLUMN_SEPARATOR;
 		
 		// testAborted
 		line += (mixedRevision.hasTestAborted() ? TRUE : FALSE) + 
 				COLUMN_SEPARATOR;
 		
 		line += NONE + COLUMN_SEPARATOR + // test
 				NONE + COLUMN_SEPARATOR + // baseTestResult
 				NONE + COLUMN_SEPARATOR + // otherTestResult
 				NONE + "\n";              // mixedTestResult
 		
 		return line;
 	}
 	
 	/**
 	 * Format: ?file1,?file2,...,?fileN
 	 */
 	private String getLineDelta(Set<DiffFile> revertedFiles, Set<DiffFile> totalDiffFiles) {		
 		Set<String> delta = new HashSet<String>();
 		
 		for (DiffFile diffFile : totalDiffFiles) {
 			if (revertedFiles.contains(diffFile)) {
 				continue;
 			}
 			
			String change = diffFile.getDiffType().toString().charAt(0) + 
				diffFile.getFileName();
 			delta.add(change);
 		}
 		
 		return StringUtils.join(delta, FILE_SEPARATOR);		
 	}
 	
 	/**
 	 * Format: test mixedTestResult parentTestResult childTestResult
 	 */
 	private String getLineTestResults(String test, TestResult mixedTestResult, 
 			TestResult parentTestResult, TestResult childTestResult) {
 		String testResults = test + COLUMN_SEPARATOR + 
 					mixedTestResult.encodeAsString(test) + COLUMN_SEPARATOR + 
 					parentTestResult.encodeAsString(test) + COLUMN_SEPARATOR + 
 					childTestResult.encodeAsString(test);
 		
 		return testResults;
 	}
 }
