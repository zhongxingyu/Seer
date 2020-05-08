 package analyzation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
 import org.apache.commons.math.stat.regression.SimpleRegression;
 
 public class Analysis {
 	File[] subjects;
 
 	public static void main(String[] args) {
 		Analysis analysis = new Analysis(
 				"/home/per/workspace/stresstetris/tests/");
 		analysis.printLatex();
 	}
 
 	public Analysis(String testFolderPath) {
 		subjects = getTestFolders(testFolderPath);
 	}
 
 	private void printResult() {
 		StringBuilder sb = new StringBuilder();
 
 		for (File subject : subjects) {
 			List<PatternTestFile> tests = getPatternTestData(subject);
 			sb.append(subject.getName()).append("\n");
 			sb.append("-------------").append("\n");
 
 			for (PatternTestFile test : tests) {
 				sb.append(test.getName()).append("\n");
 				sb.append(test.getCorrelation()).append("\n\n");
 			}
 			sb.append("\n\n");
 		}
 
 		System.out.println(sb.toString());
 
 	}
 
 	private void printLatex() {
 		StringBuilder sb = new StringBuilder();
 
 		for (File subject : subjects) {
 			List<PatternTestFile> tests = getPatternTestData(subject);
 			for (int i = 0; i < tests.size(); i++) {
 				sb.append("    ").append(
 						capitalize(subject.getName())
 								+ (i > 0 ? " (" + (i + 1) + ") " : ""));
 				sb.append(" & ").append(tests.get(i).getRawCorrelation());
 				sb.append(" & ").append(tests.get(i).getCorrelation());
 				sb.append("\\\\ \\hline\n");
 			}
 		}
 
 		System.out.println(sb.toString());
 
 	}
 
 	private String capitalize(String line) {
 		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
 	}
 
 	private List<PatternTestFile> getPatternTestData(File firstTestSubject) {
 		List<PatternTestFile> testFiles = new ArrayList<PatternTestFile>();
 
 		File[] edaFiles = firstTestSubject.listFiles(csvFilter());
 
 		for (File eda : edaFiles) {
 			File time = new File(eda.getAbsolutePath().replace("-eda", "-dif"));
 			testFiles.add(new PatternTestFile(eda, time));
 		}
 
 		return testFiles;
 	}
 
 	private File[] getTestFolders(String testFolderPath) {
 		File testRoot = new File(testFolderPath);
 
 		File[] listOfTestFolder = testRoot.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File folder, String name) {
 				return folder.isDirectory()
 						&& new File(folder.getAbsolutePath() + "/" + name + "/")
 								.listFiles(csvFilter()).length > 0;
 			}
 		});
 
 		return listOfTestFolder;
 	}
 
 	private FilenameFilter csvFilter() {
 		return new FilenameFilter() {
 			@Override
 			public boolean accept(File file, String name) {
 				return name.contains("csv") && name.contains("eda")
						&& !name.contains("feedback") && !name.contains("FIXED");
 			}
 		};
 	}
 
 }
