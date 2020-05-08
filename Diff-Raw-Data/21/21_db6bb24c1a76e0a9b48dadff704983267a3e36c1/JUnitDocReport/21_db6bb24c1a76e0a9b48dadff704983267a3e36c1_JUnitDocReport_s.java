 /*
  * Created on Jul 18, 2006
  */
 package edu.duke.cabig.catrip.test.report.ant;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 
 import edu.duke.cabig.catrip.test.report.JUnitDoclet;
 import edu.duke.cabig.catrip.test.report.JUnitXmlParser;
 import edu.duke.cabig.catrip.test.report.data.TestSuite;
 import edu.duke.cabig.catrip.test.report.report.JUnitHtmlReport;
 import edu.duke.cabig.catrip.test.report.report.JUnitReport;
 import edu.duke.cabig.catrip.test.report.report.JUnitTextReport;
 
 public class JUnitDocReport
 	extends Task
 {
 	private File destfile;
 	private String format;
 	private String reportClass;
 	private List<JUnitResults> resultsList = new ArrayList<JUnitResults>();
 	private List<JUnitDocs> docsList = new ArrayList<JUnitDocs>();
 
 	public void execute()
 		throws BuildException
 	{
 		// hack the classpath
 		//String javaHome = System.getProperty("java.home");
 		//if (javaHome.endsWith("jre")) javaHome = new File(javaHome).getParent();
 		//System.setProperty("env.class.path",
 		//	"build\\classes" + File.pathSeparator + 
 		//	javaHome + File.separator + "lib" + File.separator + "tools.jar" + File.pathSeparator + 
 		//	System.getProperty("env.class.path")
 		//);
 		//System.out.println(System.getProperty("env.class.path"));
 		System.out.println(System.getProperty("java.class.path"));
 		//if (true) return;
 		
 		// parse results
 		JUnitXmlParser parser = new JUnitXmlParser();
 		try {
 			for (JUnitResults results : resultsList) {
 				for (File file : getFiles(getProject(), results.fileSetList)) {
 					parser.parse(file);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BuildException(e);
 		}
 		TestSuite[] suites = parser.getTestSuites();
 		
 		// parse docs
 		try {
			//Class.forName("edu.duke.cabig.catrip.test.report.JUnitDoclet");
 			for (JUnitDocs docs : docsList) {
 				File[] files = getFiles(getProject(), docs.fileSetList);
 				JUnitDoclet.addDocs(files, suites);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BuildException(e);
 		}		
 		
 		// get reporter
 		JUnitReport report = null;
 		if (reportClass != null) {
 			try {
 				report = (JUnitReport) Class.forName(reportClass).newInstance();
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new BuildException(e);
 			}
 		} else if ("html".equals(format)) {
 			report = new JUnitHtmlReport();			
 		} else {
 			report = new JUnitTextReport();
 		}
 		
 		// write report
 		PrintStream out = null;
 		boolean shouldClose = false;
 		try {
 			if (destfile == null) {
 				out = System.out;
				shouldClose = true;
 			} else {
 				destfile.getParentFile().mkdirs();
 				out = new PrintStream(new BufferedOutputStream(new FileOutputStream(destfile)));
 			}
			report.writeReport(suites, System.out);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BuildException(e);
 		} finally {
 			if (out != null && shouldClose) {
 				out.flush();
 				out.close();
 			}
 		}
 	}
 	
 	private static File[] getFiles(Project project, List<FileSet> fileSetList)
 	{
 		ArrayList<File> fileList = new ArrayList<File>();
 		
 		for (FileSet fs : fileSetList) {
 			Collections.addAll(fileList, getFiles(project, fs));
 		}
 		
 		return fileList.toArray(new File[0]);
 	}
 	
 	private static File[] getFiles(Project project, FileSet fs)
 	{
 		ArrayList<File> fileList = new ArrayList<File>();
 		
 		DirectoryScanner ds = fs.getDirectoryScanner(project);
 		for (String file : ds.getIncludedFiles()) {
 			fileList.add(new File(ds.getBasedir(), file));
 		}
 		for (String file : ds.getIncludedDirectories()) {
 			fileList.add(new File(ds.getBasedir(), file));
 		}
 		
 		return fileList.toArray(new File[0]);
 	}
 
 	public File getDestfile()
 	{
 		return destfile;
 	}
 
 	public void setDestfile(File destfile)
 	{
 		this.destfile = destfile;
 	}
 	
 	public void addConfiguredJunitResults(JUnitResults results) 
 	{
 		resultsList.add(results);
 	}
 	
 	public void addConfiguredJunitDocs(JUnitDocs docs) 
 	{
 		docsList.add(docs);
 	}
 
 	public String getFormat()
 	{
 		return format;
 	}
 
 	public void setFormat(String format)
 	{
 		this.format = format;
 	}
 
 	public String getReportClass()
 	{
 		return reportClass;
 	}
 
 	public void setReportClass(String reportClass)
 	{
 		this.reportClass = reportClass;
 	}
 }
