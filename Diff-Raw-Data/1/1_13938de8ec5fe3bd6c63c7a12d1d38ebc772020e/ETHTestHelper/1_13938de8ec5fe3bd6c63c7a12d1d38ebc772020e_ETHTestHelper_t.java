 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 public class ETHTestHelper {
 
 	public static Set<ETHTestCase> getETHTestCases(File directory) {
 		Set<ETHTestCase> inFiles = new HashSet<ETHTestCase>();
 
 		for (File assignment : directory.listFiles()) {
 			if (!isValidTestFolder(assignment))
 				continue;
 			if (assignment.listFiles() == null)
 				continue;
 
 			for (File in : assignment.listFiles()) {
 				if (ETHTestCase.isInFile(in))
 					inFiles.add(new ETHTestCase(in));
 			}
 		}
 		return inFiles;
 	}
 
 	private static boolean isValidTestFolder(File assignment) {
 		String skipMarkerPath = assignment.getAbsolutePath() + File.separator + "skip.txt";
 		return assignment.isDirectory() && !new File(skipMarkerPath).exists();
 	}
 
 }
