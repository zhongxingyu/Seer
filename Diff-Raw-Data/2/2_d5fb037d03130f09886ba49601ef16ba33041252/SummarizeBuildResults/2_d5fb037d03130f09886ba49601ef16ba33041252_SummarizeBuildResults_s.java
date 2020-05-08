 package com.kevinearls.hudson;
 
 import generated.hudson.build.ActionsType;
 import generated.hudson.build.HudsonTasksJunitTestResultActionType;
 import generated.hudson.build.MatrixRunType;
 import org.apache.commons.io.FileUtils;
 import org.apache.poi.hssf.usermodel.*;
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.*;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.util.*;
 
 /**
  * Create a summary of result of the Platform and dualjdk results on Hudson in a single
  * csv report.
  * 
  * TODO explain
  * 
  * @author kearls
  *
  */
 public class SummarizeBuildResults {
 
     private static final String passedTdOpenTag = "<td style=\"background-color: #adff2f;\">";
     private static final String failedTestsTdOpenTag = "<td style=\"background-color: #ffff00;\">";
     private static final String failedBuildTdOpenTag =  "<td style=\"background-color: #dc143c;\">";
     private static final String tdCloseTag = "</td>";
 
 
 	private static Unmarshaller unmarshaller = null;
 	static {
 		try {
 			JAXBContext jaxbContext = JAXBContext.newInstance(MatrixRunType.class);
 			unmarshaller = jaxbContext.createUnmarshaller();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Use JAXB to create a MatrixRunType object from the file
 	 * 
 	 * @param buildFileName
 	 * @return
 	 * @throws JAXBException
 	 */
 	private MatrixRunType getTestSuiteFromFile(String buildFileName) throws JAXBException {
 		File buildResultsFile = new File(buildFileName);
 		StreamSource source = new StreamSource(buildResultsFile);
 		JAXBElement<MatrixRunType> root = unmarshaller.unmarshal(source, MatrixRunType.class); 
 		return root.getValue(); 
 	}
 
 
 	/**
 	 * Return the latest build in a given build directory
 	 * 
 	 * @param targetDirectory Something like: cxf-2.6.0.fuse-7-1-x-stable-platform/configurations/axis-jdk/jdk6/axis-label/ubuntu/builds/
 	 * @return cxf-2.6.0.fuse-7-1-x-stable-platform/configurations/axis-jdk/jdk6/axis-label/ubuntu/builds/2012-11-02_21-09-35
 	 */
     private File getLatestBuildDirectory(File targetDirectory) throws IOException {
         if (targetDirectory != null && targetDirectory.listFiles() != null) {
             List<File> fud = Arrays.asList(targetDirectory.listFiles());
 
             // Remove symlinks, which are just numbered.  We want to be able to use the date.
             List<File> contents = new ArrayList<File>();
             for (File f : fud) {
                 if (!FileUtils.isSymlink(f)) {
                     contents.add(f);
                 }
             }
 
             Collections.sort(contents, new BuildDirectoryComparator());
             Collections.reverse(contents);
 
             if (!contents.isEmpty()) {
                 return contents.get(0);
             } else {
                 return null;
             }
         } else {
             return null;
         }
     }
 
 
 	/**
 	 * Return a list of 7.1 platform test result directories
 	 * 
 	 * @param root this should be the root of the Hudson jobs directory
 	 */
 	private List<File> getPlatformDirectories(File root) {
 		PlatformDirectoryFilter pdf = new PlatformDirectoryFilter();
         File[] files = root.listFiles(pdf);
 		List<File> directories = Arrays.asList(files);
         Collections.sort(directories);
 		
 		return directories;
 	}
 	
  
 	/**
 	 *   Create an Excel spreadsheet containing 2 sheets on build results from the directory "root".  There will
      *   be both a summary and details sheet.
      *
      *   For content, we will select the latest platform build result for 7.2 builds or the directory specified.
 	 * 
 	 * 
 	 * @param root
      * @param workbook
 	 * @throws JAXBException
 	 */
 	public void createSummary(File root, HSSFWorkbook workbook) throws JAXBException, IOException {
         HSSFSheet summarySheet = workbook.createSheet("Summary");
         printSpreadsheetHeaders(workbook, summarySheet);
         int summaryRowIndex=2;
 
         HSSFSheet detailsSheet = workbook.createSheet("Details");
         int detailsRowIndex=0;
 
         Font whiteFont = workbook.createFont();
         whiteFont.setColor(HSSFColor.WHITE.index);
 
         HSSFCellStyle passedStyle = getPassedCellStyle(workbook, whiteFont);
         HSSFCellStyle failedTestsStyle = getFailedTestsCellStyle(workbook, whiteFont);
         HSSFCellStyle failedBuildStyle = getFailedBuildCellStyle(workbook, whiteFont);
 
         Map<String, List<BuildResult>> allResults = getAllResults(root);
 
         List<String> projectNames = new ArrayList<String>(allResults.keySet());
 		Collections.sort(projectNames);
         for (String projectName : projectNames) {
             HSSFRow summarySheetRow = summarySheet.createRow(summaryRowIndex++);
             int summaryCellIndex = 0;
 
 			List<BuildResult> buildResults = allResults.get(projectName);
 			Collections.reverse(buildResults);
 			Collections.sort(buildResults, new BuildResultComparator());
 
             HSSFCell platformNameCell = summarySheetRow.createCell(summaryCellIndex++);
             platformNameCell.setCellValue(projectName);
 
 				for (PLATFORM platform : PLATFORM.values()) {
 				for (JDK jdk : JDK.values()) {
 					for (BuildResult br: buildResults) {
 						if (platform.equals(br.getPlatform()) && jdk.equals(br.getJdk()) ) {
                             addDetailsLine(detailsSheet, br, detailsRowIndex++);
                             String blah = br.getFailedTests() + "/" + br.getTestsRun();
                             HSSFCell summarySheetRowCell = summarySheetRow.createCell(summaryCellIndex++);
 
                             if (br.getResult().equalsIgnoreCase("success")) {
                                 summarySheetRowCell.setCellStyle(passedStyle);
                             } else if (br.getTestsRun().equals(0)) {
                                 summarySheetRowCell.setCellStyle(failedBuildStyle);
                             } else {
                                 summarySheetRowCell.setCellStyle(failedTestsStyle);
                             }
                             summarySheetRowCell.setCellValue(blah);
 						}
 					}
 
 				}
 			}
             detailsRowIndex++;  // skip a row on the details sheet at the end of each project;
 		}
 
         summarySheet.setColumnWidth(0, 40 * 256);   // Cell widths are in units of 1/256 of a character
 	}
 
     /**
      * Create the summary as an HTML table which can be pasted into a DOCSPACE page
      *
      * @param root
      * @throws JAXBException
      * @throws IOException
      */
     public void createHTMLSummary(File root) throws JAXBException, IOException {
 
         FileWriter writer = new FileWriter("result.html");  // TODO add date
         writer.write("<html>");
         writer.write("<body>");
         String style = "<style><!--\n" +
                 "table { border-collapse: collapse; font-family: Futura, Arial, sans-serif; } caption { font-size: larger; margin: 1em auto; } th, td { padding: .65em; } th, thead { background: #000; color: #fff; border: 1px solid #000; } td { border: 1px solid #777; }\n" +
                 "--></style>";
         writer.write(style);
         writer.write("<table>");
         writer.write("<caption>JBoss Fuse 6 Platform Test Results as of " + new Date().toString() + "</caption>");
         printHtmlHeaders(writer);
 
         Map<String, List<BuildResult>> allResults = getAllResults(root);
         List<String> projectNames = new ArrayList<String>(allResults.keySet());
         Collections.sort(projectNames);
         for (String projectName : projectNames) {
             writer.write("    <tr>");
             List<BuildResult> buildResults = allResults.get(projectName);
             Collections.reverse(buildResults);
             Collections.sort(buildResults, new BuildResultComparator());
 
             writer.write("<td>" + projectName + "</td>");
             for (PLATFORM platform : PLATFORM.values()) {
                 for (JDK jdk : JDK.values()) {
                     for (BuildResult br: buildResults) {
                         if (platform.equals(br.getPlatform()) && jdk.equals(br.getJdk()) ) {
                             String testResult = br.getFailedTests() + "/" + br.getTestsRun();
 
                             if (br.getResult().equalsIgnoreCase("success")) {
                                 writer.write(passedTdOpenTag + testResult + tdCloseTag);
                             } else if (br.getTestsRun().equals(0)) {
                                 writer.write(failedBuildTdOpenTag + testResult + tdCloseTag);
                             } else {
                                 writer.write(failedTestsTdOpenTag + testResult + tdCloseTag);
                             }
                         }
                     }
 
                 }
             }
             writer.write("</tr>");
         }
 
         writer.write("<table>");
         writer.write("</body>");
         writer.write("</html>");
         writer.close();
     }
 
     /**
      * Print the headers for the HTML summary
      */
     private void printHtmlHeaders(FileWriter writer) throws IOException {
         // Print headers
         writer.write("<thead>");
         writer.write("<tr>");
         writer.write("<td>Platform</td>");
         for (PLATFORM platform : PLATFORM.values()) {
             for (JDK jdk : JDK.values()) {
                 writer.write("<td>" + platform + " " + jdk + "</td>");
             }
         }
         writer.write("</tr>");
         writer.write("</thead>");
     }
 
 
     private HSSFCellStyle getFailedBuildCellStyle(HSSFWorkbook workbook, Font whiteFont) {
         HSSFCellStyle failedBuildStyle = workbook.createCellStyle();
         failedBuildStyle.setFillForegroundColor(HSSFColor.PINK.index);
         failedBuildStyle.setFillPattern(CellStyle.SPARSE_DOTS);
         failedBuildStyle.setFont(whiteFont);
         return failedBuildStyle;
     }
 
     private HSSFCellStyle getFailedTestsCellStyle(HSSFWorkbook workbook, Font whiteFont) {
         HSSFCellStyle failedTestsStyle = workbook.createCellStyle();
         failedTestsStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
         failedTestsStyle.setFillPattern(CellStyle.FINE_DOTS);
         failedTestsStyle.setFont(whiteFont);
         return failedTestsStyle;
     }
 
     private HSSFCellStyle getPassedCellStyle(HSSFWorkbook workbook, Font whiteFont) {
         HSSFCellStyle passedStyle = workbook.createCellStyle();
         passedStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
         passedStyle.setFillPattern(CellStyle.FINE_DOTS);
         passedStyle.setFont(whiteFont);
         return passedStyle;
     }
 
     public void addDetailsLine(Sheet detailsSheet, BuildResult buildResult, int rowIndex) {
         Row row = detailsSheet.createRow(rowIndex);
 
         int detailsCellIndex=0;
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getName());
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getRunDate());
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getJdk().toString());
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getPlatform().toString());
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getResult());
         row.createCell(detailsCellIndex++).setCellValue("Tests Run");
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getTestsRun());
         row.createCell(detailsCellIndex++).setCellValue("Failed");
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getFailedTests());
         row.createCell(detailsCellIndex++).setCellValue("Duration");
         row.createCell(detailsCellIndex++).setCellValue(buildResult.getFormattedDuration());
     }
 
     /**
      * Add the headers to the worksheet.
      *
      * @param workbook
      * @param summarySheet
      */
 	private void printSpreadsheetHeaders(Workbook workbook, Sheet summarySheet) {
         Font font = workbook.createFont();
         font.setBoldweight(Font.BOLDWEIGHT_BOLD);
         CellStyle style = workbook.createCellStyle();
         style.setWrapText(true);
         style.setFont(font);
 
 		// Print 2 header rows. The top row just has platform names (which should go above jdk)
         Row row = summarySheet.createRow(0);
         row.setRowStyle(style);
 
         int cellIndex = 0;
         Cell cell = row.createCell(cellIndex++);
         for (PLATFORM platform : PLATFORM.values()) {
             for (JDK jdk : JDK.values()) {
                 cell = row.createCell(cellIndex++);
                 cell.setCellStyle(style);
                 cell.setCellValue(platform.toString());
             }
         }
 
         // Second row
         row = summarySheet.createRow(1);
         row.setRowStyle(style);
         row.createCell(0).setCellValue("Platform");
         cellIndex = 1;
 		for (PLATFORM platform : PLATFORM.values()) {
 			for (JDK jdk : JDK.values()) {
                 cell = row.createCell(cellIndex++);
                 cell.setCellStyle(style);
                 cell.setCellValue(jdk.toString());
 			}
 		}
 	}
 
 
     /**
      * TODO summarize; exactly what does this return?
      *
      * @param root
      * @return
      */
     private Map<String, List<BuildResult>> getAllResults(File root) throws IOException{
         Map<String, List<BuildResult>> allResults = new HashMap<String, List<BuildResult>>();
         List<File>platformDirectories = getPlatformDirectories(root);
         for (File platformDirectory : platformDirectories) {
             for (PLATFORM platform : PLATFORM.values()) {
                 for (JDK jdk : JDK.values()) {
                     String targetDirectoryName = platformDirectory.getAbsolutePath() + "/configurations/axis-jdk/" + jdk + "/axis-label/" + platform + "/builds/";
                     File latestBuildDirectory = getLatestBuildDirectory(new File(targetDirectoryName));
                     if (latestBuildDirectory != null) {
                         try {
                             String buildDateTime = latestBuildDirectory.getName(); 	// directory name of the build is date time in the format 2012-11-02_21-09-35
                             String latestBuildFileName = latestBuildDirectory.getAbsolutePath() + "/build.xml";
                             MatrixRunType mrt = getTestSuiteFromFile(latestBuildFileName);
                             ActionsType actions = mrt.getActions();
                             HudsonTasksJunitTestResultActionType junitResults = actions.getHudsonTasksJunitTestResultAction();
 
                             BuildResult buildResult;
                             if (junitResults != null) {
                                 buildResult = new BuildResult(platformDirectory.getName(),  buildDateTime, jdk, platform, mrt.getResult(), junitResults.getTotalCount(), junitResults.getFailCount(), mrt.getDuration());
                             } else {
                                 buildResult = new BuildResult(platformDirectory.getName(),  buildDateTime, jdk, platform, mrt.getResult(), 0, 0, 0);
                             }
                             // TODO need to store by platformDirectory.getName() (which is projectname) jdk, platform
                             List<BuildResult> platformResults = allResults.get(platformDirectory.getName());
                             if (platformResults == null) {
                                 platformResults = new ArrayList<BuildResult>();
                                 allResults.put(platformDirectory.getName(), platformResults);
                             }
                             platformResults.add(buildResult);
                         } catch(Exception e) {
                             // TODO this could occur if the build is still running.
                             System.err.println("************ Exception " + e.getMessage() + " on " + latestBuildDirectory.getAbsolutePath());
                         }
 
                     }
                 }
 
             }
         }
         return allResults;
     }
 
 
 	/**
 	 * @param args
 	 * @throws JAXBException 
 	 */
 	public static void main(String[] args) throws JAXBException, IOException {
         HSSFWorkbook workbook = new HSSFWorkbook();
 		String testRoot ="/mnt/hudson/jobs";
        testRoot="/Users/kearls/mytools/junit-results-analyzer/jobs/";
 		if (args.length > 0) {
 			testRoot = args[0];
 		} 
 
 		
 		System.out.println("Starting at " + testRoot);
 		SummarizeBuildResults me = new SummarizeBuildResults();
 		File theRoot = new File(testRoot);
 		me.createSummary(theRoot, workbook);
         me.createHTMLSummary(theRoot);
 
         FileOutputStream fileOut = new FileOutputStream("workbook.xls");
         workbook.write(fileOut);
         fileOut.close();
 	}
 
 }
 
 
 /**
  * Filter to select directories which contain 7.1 platform test results
  * @author kearls
  *
  */
 class PlatformDirectoryFilter implements FileFilter {	// TODO change to FileNameFilter???
 	public boolean accept(File pathname) {
 		// TODO test for isDirectory too?
 		String name = pathname.getName();
         if ((name.equalsIgnoreCase("fuseenterprise-master-platform")) || ((name.contains("7-2") || name.contains("7.2")) && (name.endsWith("-platform")))) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
 
 
 /**
  * Comparator to help find the newest subdirectory in the builds directory
  * @author kearls
  *
  */
 class BuildDirectoryComparator implements Comparator<File> {
 	public int compare(File first, File second) {
 		if (first.lastModified() > second.lastModified()) {
 			return 1;
 		} else {
 			return -1;
 		}
 	}
 }
 
 
 /**
  * Comparator to sort a list of builds results on name, platform, and jdk
  * @author kearls
  *
  */
 class BuildResultComparator implements Comparator<BuildResult> {
 	@Override
 	public int compare(BuildResult b1, BuildResult b2) {
 		int nameValue = b1.getName().compareTo(b2.getName());
 		if (nameValue != 0) {
 			return nameValue;
 		} else {
 			// TODO sort on date too?
 			int platformValue = b1.getPlatform().compareTo(b2.getPlatform());
 			if (platformValue != 0) {
 				return platformValue;
 			} else {
 				return b1.getJdk().compareTo(b2.getJdk());
 			}
 		}
 	}
 	
 }
