 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: Settings.java,v $
	$Date: 2004-02-17 16:11:54 $ 
 */
 package de.berlios.statcvs.xml;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import net.sf.statcvs.output.WebRepositoryIntegration;
 import net.sf.statcvs.util.FilePatternMatcher;
 import net.sf.statcvs.util.FileUtils;
 
 /**
  * Class for storing all command line parameters. The parameters
  * are set by the {@link net.sf.statcvs.Main#main} method. Interested classes
  * can read all parameter values from here.
  * 
  * @author jentzsch
 * @version $Id: Settings.java,v 1.3 2004-02-17 16:11:54 squig Exp $
  */
 public class Settings {
 
 	private static boolean generateHistory;
 	private static boolean useHistory;
 	private static String logFileName = null;
 	private static String checkedOutDirectory = null;
 	private static String projectTitle = null;
 	private static String outputDir = "";
 	private static Level loggingLevel = Level.WARNING;
 	private static boolean showCreditInformation = true;
 	private static String notesFile = null;
 	private static String notes = null;
 
 	private static String includePattern = null;
 	private static String excludePattern = null;
 
 	private static WebRepositoryIntegration webRepository = null;
 
 	private static String outputSuite = null;
 
 	/**
 	 * Method getProjectName.
 	 * @return String name of the project
 	 */
 	public static String getProjectName() {
 		return projectTitle;
 	}
 
 	/**
 	 * Method getCheckedOutDirectory.
 	 * @return String name of the checked out directory
 	 */
 	public static String getCheckedOutDirectory() {
 		return checkedOutDirectory;
 	}
 
 	/**
 	 * Method getLogfilename.
 	 * @return String name of the logfile to be parsed
 	 */
 	public static String getLogFileName() {
 		return logFileName;
 	}
 
 	/**
 	 * Returns the outputDir.
 	 * @return String output Directory
 	 */
 	public static String getOutputDir() {
 		return outputDir;
 	}
 
 	/**
 	 * Returns the report notes (from "-notes filename" switch) or <tt>null</tt>
 	 * if not specified
 	 * @return the report notes
 	 */
 	public static String getNotes() {
 		return notes;
 	}
 
 	public static String getOutputSuite()
 	{
 		return outputSuite;
 	}
 
 	/**
 	 * Returns whether the credit information should be shown
 	 * @return boolean showCreditInformation
 	 */
 	public static boolean getShowCreditInformation() {
 		return showCreditInformation;
 	}
 
 	/**
 	 * Returns a {@link WebRepositoryIntegration} object if the user
 	 * has specified a URL to one. <tt>null</tt> otherwise.
 	 * @return the web repository
 	 */
 	public static WebRepositoryIntegration getWebRepository() {
 		return webRepository;
 	}
 
 	/**
 	 * Sets the checkedOutDirectory.
 	 * @param checkedOutDirectory The checkedOutDirectory to set
 	 * @throws IOException if directory does not exist
 	 */
 	public static void setCheckedOutDirectory(String checkedOutDirectory) 
 			throws IOException {
 		File directory = new File (checkedOutDirectory);
 		if (!directory.exists() || !directory.isDirectory()) {
 			throw new IOException(
 					"directory does not exist: " + checkedOutDirectory);
 		}
 		Settings.checkedOutDirectory = checkedOutDirectory;
 	}
 
 	/**
 	 * Sets the logFileName.
 	 * @param logFileName The logFileName to set
 	 * @throws IOException if the file does not exist
 	 */
 	public static void setLogFileName(String logFileName) throws IOException {
 		File inputFile = new File(logFileName);
 		if (!inputFile.exists()) {
 			throw new IOException(
 					"Specified logfile not found: " + logFileName);
 		}
 		Settings.logFileName = logFileName;
 	}
 
 	/**
 	 * Sets the outputDir.
 	 * @param outputDir The outputDir to set
 	 * @throws IOException if the output directory cannot be created
 	 */
 	public static void setOutputDir(String outputDir) throws IOException {
 		if (!outputDir.endsWith(FileUtils.getDirSeparator())) {
 			outputDir += FileUtils.getDefaultDirSeparator();
 		}
 		File outDir = new File(outputDir);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		if (outDir.length() > 0 && (!outDir.exists() || !outDir.isDirectory())) {
 			throw new IOException("Can't create output directory: " + outputDir);
 		}
 
 		Settings.outputDir = outputDir;
 	}
 
 	/**
 	 * Sets the name of the notes file. The notes file will be included
 	 * on the {@link IndexPage} of the output. It must contain a valid
 	 * block-level HTML fragment (for example
 	 * <tt>"&lt;p&gt;Some notes&lt;/p&gt;"</tt>) 
 	 * @param notesFile a local filename
 	 * @throws IOException if the file is not found or can't be read
 	 */
 	public static void setNotesFile(String notesFile) throws IOException {
 		File f = new File(notesFile);
 		if (!f.exists()) {
 			throw new IOException(
 					"Notes file not found: " + notesFile);
 		}
 		if (!f.canRead()) {
 			throw new IOException(
 					"Can't read notes file: " + notesFile);
 		}
 		Settings.notesFile = notesFile;
 		try {
 			notes = readNotesFile();
 		} catch (IOException e) {
 			throw new IOException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Sets a configured WebRepositoy instance 	 
 	 * @param repo a WebRepositoryIntegration instance
 	 */
 	public static void setWebRepository(WebRepositoryIntegration repo) {
 		Settings.webRepository = repo;
 	}
 
 	/**
 	 * Sets a project title to be used in the reports
 	 * @param projectTitle The project title to be used in the reports
 	 */
 	public static void setProjectTitle(String projectTitle) {
 		Settings.projectTitle = projectTitle;
 	}
 
 	/**
 	 * Gets the name of the logging properties file
 	 * @return the name of the logging properties file
 	 */
 	public static Level getLoggingLevel() {
 		return loggingLevel;
 	}
 
 	/**
 	 * Sets the logging level to verbose
 	 */
 	public static void setVerboseLogging() {
 		Settings.loggingLevel = Level.INFO;
 	}
 
 	/**
 	 * Sets the logging level to debug
 	 */
 	public static void setDebugLogging() {
 		Settings.loggingLevel = Level.FINEST;
 	}
 
 	private static String readNotesFile() throws IOException {
 		BufferedReader r = new BufferedReader(new FileReader(notesFile));
 		String line = r.readLine();
 		String result = "";
 		while (line != null) {
 			result += line; 
 			line = r.readLine();
 		}
 		return result;
 	}
 
 	/**
 	 * Enabe or disable the credit information in the generated charts
 	 * @param enable Enabe or disable the credit information in the generated charts
 	 */
 	public static void setShowCreditInformation(boolean enable) {
 		showCreditInformation = enable;
 	}
 
 	/**
 	 * Sets a file include pattern list. Only files matching one of the
 	 * patterns will be included in the analysis.
 	 * @param patternList a list of Ant-style wildcard patterns, seperated
 	 *                    by : or ;
 	 * @see net.sf.statcvs.util.FilePatternMatcher
 	 */
 	public static void setIncludePattern(String patternList) {
 		Settings.includePattern = patternList;
 	}
 	
 	/**
 	 * @see net.sf.statcvs.util.ConfigurationOptions#setExcludePattern(String)
 	 */
 	public static void setExcludePattern(String patternList) {
 		Settings.excludePattern = patternList;
 	}
 
 	public static void setOutputSuite(String outputSuite) {
 		Settings.outputSuite = outputSuite;
 	}
 
 	public static boolean getUseHistory() {
 		return useHistory;
 	}
 	
 	public static void setUseHistory(boolean b) {
 		useHistory = b;
 	}
 
 	public static void setGenerateHistory(boolean b) {
 		generateHistory = b;
 	}
 	
 	public static boolean getGenerateHistory() {
 		return generateHistory;
 	}
 
 	/**
 	 * @return
 	 */
 	public static String getExcludePattern() {
 		return excludePattern;
 	}
 
 	/**
 	 * @return
 	 */
 	public static String getIncludePattern() {
 		return includePattern;
 	}
 }
