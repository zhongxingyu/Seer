 package com.attask.jenkins.testreport;
 
 import com.attask.jenkins.testreport.utils.RunUtils;
 import hudson.FilePath;
 import hudson.Util;
 import hudson.model.Run;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: Joel Johnson
  * Date: 1/19/13
  * Time: 2:09 PM
  */
 @ExportedBean
 public class TestResult implements Comparable<TestResult> {
 	public static final Logger log = Logger.getLogger("TestReportTool");
 	private final String name;
 	private final int time;
 	private final String threadId;
 	private final TestStatus status;
 	private final String runId;
 	private final String stackTrace;
 	private final int age;
 	private final String firstFailingBuildId;
 	private final String url;
 	private final String uniquifier;
 
 	public TestResult(String name, int time, String threadId, TestStatus status, String runId, String stackTrace, int age, String firstFailingBuildId, String url, String uniquifier) {
 		this.name = name;
 		this.time = time;
 		this.threadId = threadId;
 		this.status = status;
 		this.runId = runId;
 		this.stackTrace = stackTrace;
 		this.age = age;
 		this.firstFailingBuildId = firstFailingBuildId;
 		this.url = url;
 		this.uniquifier = uniquifier;
 	}
 
 	@Exported
 	public String getName() {
 		return name;
 	}
 
 	@Exported
 	public String getNameUrlEncoded() {
 		return name.replace("#", "%23");
 	}
 
 	@Exported
 	public int getTime() {
 		return time;
 	}
 
 	public String findTimeSpan() {
 		return Util.getTimeSpanString(getTime());
 	}
 
 	@Exported
 	public String getUniquifier() {
 		return uniquifier;
 	}
 
 	public String findPaddedTime() {
 		String time = String.valueOf(getTime());
 		int length = 10 - time.length();
 		StringBuilder result = new StringBuilder();
 		for(int i = 0; i < length; i++) {
 			result.append('0');
 		}
 		result.append(time);
 		return result.toString();
 	}
 
 	@Exported
 	public String getThreadId() {
 		return threadId;
 	}
 
 	@Exported
 	public TestStatus getStatus() {
 		return status;
 	}
 
 	@Exported
 	public String getRunId() {
 		return runId;
 	}
 
 	public Run findRun() {
 		return RunUtils.findRun(getRunId());
 	}
 
 	@Exported
 	public String getStackTrace() {
 		return stackTrace;
 	}
 
 	public String htmlifyStackTrace() {
 		Pattern pattern = Pattern.compile("((:?[\\$a-zA-Z0-9_]+\\.)+(:?[\\$a-zA-Z0-9_]+))\\((:?([\\$a-zA-Z0-9_]+)\\.[\\$a-zA-Z0-9_]+:(\\d+)|Unknown Source|Native Method)\\)");
 
 		StringBuilder sb = new StringBuilder();
 		Scanner scanner = new Scanner(stackTrace);
 		while(scanner.hasNextLine()) {
 			String line = Util.escape(scanner.nextLine());
 			Matcher matcher = pattern.matcher(line);
 			if(matcher.find()) {
 				String fullyQualifiedName = matcher.group(1); // This is the fully qualified method name
 				sb.append("<pre name=\"").append(fullyQualifiedName).append("\">").append(line).append("</pre>");
 			} else {
 				sb.append("<pre>").append(line).append("</pre>");
 			}
 		}
 		return sb.toString();
 
 	}
 
 	@Exported
 	public int getAge() {
 		return age;
 	}
 
 	@Exported
 	public String getFirstFailingBuildId() {
 		return firstFailingBuildId;
 	}
 
 	@Exported
 	public String getUrl() {
 		return url;
 	}
 
 	@SuppressWarnings("UnusedDeclaration")
 	public String findFirstFailureUrl() {
 		String firstFailingBuildId = getFirstFailingBuildId();
 		if(firstFailingBuildId != null) {
 			if(firstFailingBuildId.contains("$$")) {
 				String matrixBuild = findMatrixBuildUrl(firstFailingBuildId);
 				if(matrixBuild != null) {
 					return matrixBuild;
 				}
 			}
 			Run<?, ?> firstFailingBuild = Run.fromExternalizableId(firstFailingBuildId);
 			if(firstFailingBuild != null) {
 				return firstFailingBuild.getUrl();
 			}
 		}
 		return null;
 	}
 
 	private String findMatrixBuildUrl(String matrixId) {
 		Run run = RunUtils.findRun(matrixId);
 		if(run == null) {
 			return null;
 		}
 		return run.getUrl();
 	}
 
 	public static Collection<TestResult> parse(FilePath file, Run build, String uniqueId, String url) throws IOException, IllegalFormatException {
 		Map<TestResult, TestStatus> results = new HashMap<TestResult, TestStatus>();
 
 		List<String> fileLines = Arrays.asList(file.readToString().split("\r?\n"));
 		for (int lineNumber = 0; lineNumber < fileLines.size(); lineNumber++) {
 			String line = fileLines.get(lineNumber);
 			if(lineNumber == 0) {
 				if(!line.equals("AtTask Failures v2")) {
 					throw new IllegalFailureFileFormatException(file, lineNumber, "Unsupported file version: " + line);
 				}
 				continue;
 			}
 
 			int firstWhitespaceIndex = line.indexOf(" ");
 			if(firstWhitespaceIndex < 0) {
 				continue;
 			}
 
 			String statusString = line.substring(0, firstWhitespaceIndex);
 			String token = line.substring(firstWhitespaceIndex+1);
 
 			TestStatus testStatus;
 			try {
 				testStatus = TestStatus.valueOf(statusString.toUpperCase());
 			} catch(IllegalArgumentException e) {
 				throw new IllegalFailureFileFormatException(file, lineNumber, "Line status token invalid. '" + statusString + "'");
 			}
 
 			TestResult result;
 			switch (testStatus) {
 				case ADDED:
 				case STARTED:
 					result = parseSimple(testStatus, token, build, url, uniqueId);
 					break;
 				case FINISHED:
 				case SKIPPED:
 					result = parseSimplePlusMetadata(file, lineNumber, testStatus, token, build, url, uniqueId);
 					break;
 				case FAILED:
 					String[] tokenizedLine = token.split("\\s");
 					String name = tokenizedLine[0];
 					String threadId;
 					if (tokenizedLine.length > 1) {
 						threadId = tokenizedLine[1];
 					} else {
 						throw new IllegalFailureFileFormatException(file, lineNumber, "Missing Thread ID");
 					}
 					int runTime;
 					if (tokenizedLine.length > 2) {
 						runTime = Integer.parseInt(tokenizedLine[2]);
 					} else {
 						throw new IllegalFailureFileFormatException(file, lineNumber, "Missing Runtime");
 					}
 					AgeStat ageStat = findAge(name, build, uniqueId);
 					StringBuilder stackTrace = new StringBuilder();
 					int linesToAdvance = readStackTrace(fileLines, lineNumber, stackTrace);
 					lineNumber += linesToAdvance;
 
 					result = new TestResult(name, runTime, threadId, testStatus, RunUtils.getRealExternalizableId(build), stackTrace.toString(), ageStat.age, ageStat.firstFailingBuild, url, uniqueId);
 					break;
 				default:
 					throw new IllegalFailureFileFormatException(file, lineNumber, "Status not implemented: " + testStatus);
 			}
 
 			TestStatus oldStatus = results.get(result);
 			if(oldStatus == null || result.getStatus().isMoreInterestingThan(oldStatus)) {
 				results.remove(result);
 				results.put(result, result.getStatus());
 			}
 		}
 
 		return results.keySet();
 	}
 
 	private static int readStackTrace(List<String> file, int currentPosition, StringBuilder sb) {
 		int count = 0;
 		for(int i = currentPosition+1; i < file.size(); i++) {
 			String line = file.get(i);
 			if(checkIsTestLine(line)) {
 				break;
 			}
 			count++;
 			sb.append(line).append("\n");
 		}
 		return count;
 	}
 
 	private static TestResult parseSimple(TestStatus status, String token, Run build, String url, String uniquifier) {
 		return new TestResult(token.trim(), -1, null, status, RunUtils.getRealExternalizableId(build), null, 0, null, url, uniquifier);
 	}
 
 	private static TestResult parseSimplePlusMetadata(FilePath file, int lineNumber, TestStatus status, String token, Run build, String url, String uniquifier) {
 		String[] split = token.split("\\s");
 		String name = split[0];
 		String threadId;
 		if (split.length > 1) {
 			threadId = split[1];
 		} else {
 			throw new IllegalFailureFileFormatException(file, lineNumber, "Missing Thread ID");
 		}
 		int runTime;
 		if (split.length > 2) {
 			runTime = Integer.parseInt(split[2]);
 		} else {
 			throw new IllegalFailureFileFormatException(file, lineNumber, "Missing Runtime");
 		}
 		return new TestResult(name, runTime, threadId, status, RunUtils.getRealExternalizableId(build), null, 0, null, url, uniquifier);
 	}
 
 	/**
 	 * TODO: Optimize if needed. If we need to optimize: we should be finding the age in groups so we don't have to iterate all the builds multiple times.
 	 */
 	private static AgeStat findAge(String testName, Run build, String uniqueId) {
 		assert uniqueId != null : "null uniqueId";
 		AgeStat ageStat = new AgeStat();
 		ageStat.age = 1;
 		ageStat.firstFailingBuild = RunUtils.getRealExternalizableId(build);
 		while((build = build.getPreviousBuild()) != null) {
 			TestResultAction testResultAction = build.getAction(TestResultAction.class);
 			if(testResultAction != null) {
 				if(uniqueId.equals(testResultAction.getUniquifier())) {
 					TestResult oldTestResult = testResultAction.getTestResults().get(testName);
 					if(oldTestResult != null) {
 						TestStatus oldStatus = oldTestResult.getStatus();
 						if(oldStatus == TestStatus.FAILED) {
 							// FAILED tests should always have an accurate count. So just add that to our running total and return.
 							ageStat.age += oldTestResult.getAge();
 							ageStat.firstFailingBuild = oldTestResult.getFirstFailingBuildId();
 							if(ageStat.firstFailingBuild == null || ageStat.firstFailingBuild.isEmpty()) {
 								ageStat.firstFailingBuild = oldTestResult.getRunId();
 							}
 							return ageStat;
 						} else if(oldStatus == TestStatus.STARTED || oldStatus == TestStatus.ADDED) {
 							//age isn't calculated on STARTED or ADDED to save time, but we include them in our age, so we should add here, and then continue counting.
 							ageStat.age++;
 							ageStat.firstFailingBuild = oldTestResult.getRunId();
 						} else {
 							return ageStat;
 						}
 					}
 				}
 			}
 		}
 		return ageStat;
 	}
 
 	private static boolean checkIsTestLine(String line) {
 		for (TestStatus testStatus : TestStatus.values()) {
 			if(line.startsWith(testStatus.toString().toLowerCase() + " ")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return name != null ? name.hashCode() : 0;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		return other != null && other instanceof TestResult && this.getName().equals(((TestResult) other).getName());
 	}
 
 	@Override
 	public int compareTo(TestResult o) {
 		return this.getName().compareTo(o.getName());
 	}
 
	public static boolean uniquifierMatches(TestResult first, TestResult second) {
		if(first == null) {
			throw new NullPointerException("first");
		}
		if(second == null) {
			throw new NullPointerException("second");
		}

		if(first.getUniquifier() == null) {
			return second.getUniquifier() == null;
		}

		return first.getUniquifier().equals(second.getUniquifier());
	}

 	private static class AgeStat {
 		private int age;
 		private String firstFailingBuild;
 	}
 }
