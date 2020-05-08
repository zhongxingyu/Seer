 package org.mule.tooling.jubula.results;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.maven.surefire.report.AbstractReporter;
 import org.apache.maven.surefire.report.ReportEntry;
 import org.apache.maven.surefire.report.ReporterException;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.codehaus.plexus.util.xml.Xpp3DomWriter;
 
 public class XMLSurefireReporter extends AbstractReporter {
 
 	private static final String LS = System.getProperty("line.separator");
 
 	private File reportsDirectory;
 
 	private List<Xpp3Dom> results = Collections.synchronizedList(new ArrayList<Xpp3Dom>());
 
 	public XMLSurefireReporter(File reportsDirectory) {
 		super(true);
 
 		this.reportsDirectory = reportsDirectory;
 	}
 	
 	public void testSetCompleted(ReportEntry report) throws ReporterException {
 		this.testSetCompleted(report, null);
 	}
 
 	public void testSetCompleted(ReportEntry report, Long duration) throws ReporterException {
 		super.testSetCompleted(report);
 		
 		long runTime;
 		
 		if(duration == null){
 			runTime = System.currentTimeMillis() - testSetStartTime;
 		} else {
 			runTime = duration;
 		}
 
 		Xpp3Dom testSuite = createTestSuiteElement(report, runTime);
 
 		showProperties( testSuite );
 		
 		testSuite.setAttribute("tests", String.valueOf(this.getNumTests()));
 
 		testSuite.setAttribute("errors", String.valueOf(this.getNumErrors()));
 
 		testSuite.setAttribute("skipped", String.valueOf(this.getNumSkipped()));
 
 		testSuite.setAttribute("failures",
 				String.valueOf(this.getNumFailures()));
 
 		for (Iterator<Xpp3Dom> i = results.iterator(); i.hasNext();) {
 			Xpp3Dom testcase = (Xpp3Dom) i.next();
 			testSuite.addChild(testcase);
 		}
 
 		File reportFile = new File(reportsDirectory, "TEST-" + report.getName()
 				+ ".xml");
 
 		File reportDir = reportFile.getParentFile();
 
 		reportDir.mkdirs();
 
 		PrintWriter writer = null;
 
 		try {
 			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
 					new FileOutputStream(reportFile), "UTF-8")));
 
 			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + LS);
 
 			Xpp3DomWriter.write(writer, testSuite);
 		} catch (UnsupportedEncodingException e) {
 			throw new ReporterException("Unable to use UTF-8 encoding", e);
 		} catch (FileNotFoundException e) {
 			throw new ReporterException("Unable to create file: "
 					+ e.getMessage(), e);
 		}
 
 		finally {
 			IOUtil.close(writer);
 		}
 	}
 
 	private String getReportName(ReportEntry report) {
 		String reportName;
 
 		if (report.getName().indexOf("(") > 0) {
 			reportName = report.getName().substring(0,
 					report.getName().indexOf("("));
 		} else {
 			reportName = report.getName();
 		}
 		return reportName;
 	}
 
 	public void testSucceeded(ReportEntry report) {
 		this.testSucceeded(report, null);
 	}
 	
 	public void testSucceeded(ReportEntry report, Long duration) {
 		super.testSucceeded(report);
 		
 		long runTime;
 		
 		if(duration == null){
 			runTime = this.endTime - this.startTime;
 		} else {
 			runTime = duration;
 		}
 
 		Xpp3Dom testCase = createTestElement(report, runTime);
 
 		results.add(testCase);
 	}
 
 	private Xpp3Dom createTestElement(ReportEntry report, long runTime) {
 		Xpp3Dom testCase = new Xpp3Dom("testcase");
 		testCase.setAttribute("name", getReportName(report));
 		if (report.getGroup() != null) {
 			testCase.setAttribute("group", report.getGroup());
 		}
 		if (report.getSourceName() != null) {
 			testCase.setAttribute("classname", report.getSourceName());
 		}
 		testCase.setAttribute("time", elapsedTimeAsString(runTime));
 		return testCase;
 	}
 
 	private Xpp3Dom createTestSuiteElement(ReportEntry report, long runTime) {
 		Xpp3Dom testCase = new Xpp3Dom("testsuite");
 		testCase.setAttribute("name", getReportName(report));
 		if (report.getGroup() != null) {
 			testCase.setAttribute("group", report.getGroup());
 		}
 		testCase.setAttribute("time", elapsedTimeAsString(runTime));
 		return testCase;
 	}
 
 	public void testError(ReportEntry report, String stdOut, String stdErr) {
 		this.testError(report, stdOut, stdErr, null);
 	}
 	
 	public void testError(ReportEntry report, String stdOut, String stdErr, Long duration) {
 		super.testError(report, stdOut, stdErr);
 
 		writeTestProblems(report, stdOut, stdErr, "error", duration);
 	}
 
 	public void testFailed(ReportEntry report, String stdOut, String stdErr) {
 		this.testFailed(report, stdOut, stdErr, null);
 	}
 	
 	public void testFailed(ReportEntry report, String stdOut, String stdErr, Long duration) {
 		super.testFailed(report, stdOut, stdErr);
 
 		writeTestProblems(report, stdOut, stdErr, "failure", duration);
 	}
 
 	public void testSkipped(ReportEntry report) {
 		this.testSkipped(report, null);
 	}
 	
 	public void testSkipped(ReportEntry report, Long duration) {
 		super.testSkipped(report);
 		writeTestProblems(report, null, null, "skipped", duration);
 	}
 
 	private void writeTestProblems(ReportEntry report, String stdOut,
 			String stdErr, String name, Long duration) {
 		
 		long runTime;
 		
 		if(duration == null){
 			runTime = this.endTime - this.startTime;
 		} else {
 			runTime = duration;
 		}
 
 		Xpp3Dom testCase = createTestElement(report, runTime);
 
 		Xpp3Dom element = createElement(testCase, name);
 
 		String stackTrace = getStackTrace(report);
 
 		Throwable t = null;
 		if (report.getStackTraceWriter() != null) {
 			t = report.getStackTraceWriter().getThrowable();
 		}
 
 		if (t != null) {
 
 			String message = t.getMessage();
 
 			if (message != null) {
 				element.setAttribute("message", message);
 
 				element.setAttribute(
 						"type",
 						(stackTrace.indexOf(":") > -1 ? stackTrace.substring(0,
 								stackTrace.indexOf(":")) : stackTrace));
 			} else {
 				element.setAttribute("type",
 						new StringTokenizer(stackTrace).nextToken());
 			}
 		}
 
 		if (stackTrace != null) {
 			element.setValue(stackTrace);
 		}
 
 		addOutputStreamElement(stdOut, "system-out", testCase);
 
 		addOutputStreamElement(stdErr, "system-err", testCase);
 
 		results.add(testCase);
 	}
 
 	private void addOutputStreamElement(String stdOut, String name,
 			Xpp3Dom testCase) {
 		if (stdOut != null && stdOut.trim().length() > 0) {
 			createElement(testCase, name).setValue(stdOut);
 		}
 	}
 
 	private Xpp3Dom createElement(Xpp3Dom element, String name) {
 		Xpp3Dom component = new Xpp3Dom(name);
 
 		element.addChild(component);
 
 		return component;
 	}
 	
 	/**
      * Adds system properties to the XML report.
      *
      * @param testSuite
      */
     private void showProperties( Xpp3Dom testSuite )
     {
         Xpp3Dom properties = createElement( testSuite, "properties" );
 
         Properties systemProperties = System.getProperties();
 
         if ( systemProperties != null )
         {
             Enumeration propertyKeys = systemProperties.propertyNames();
 
             while ( propertyKeys.hasMoreElements() )
             {
                 String key = (String) propertyKeys.nextElement();
 
                 String value = systemProperties.getProperty( key );
 
                 if ( value == null )
                 {
                     value = "null";
                 }
 
                 Xpp3Dom property = createElement( properties, "property" );
 
                 property.setAttribute( "name", key );
 
                 property.setAttribute( "value", value );
 
             }
         }
     }
 
 	public Iterator<Xpp3Dom> getResults() {
 		return results.iterator();
 	}
 
 	public void reset() {
 		results.clear();
 		super.reset();
 	}
 
 	@Override
 	public void writeMessage(String message) {
 	}
 
 }
