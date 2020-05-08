 package com.buglabs.osgi.tester;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Enumeration;
 
 import junit.framework.Test;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 import com.buglabs.util.xml.XmlNode;
 
 public class BundleTestRunnerThread extends Thread {
 	private static final long SETTLE_MILLIS = 5000;
 
 	private final BundleContext context;
 
 	private final File outputDir;
 	private boolean errorOccurred = false;
 
 	public BundleTestRunnerThread(BundleContext context, final File outputDir) {
 		this.context = context;
 		this.outputDir = outputDir;
 	}
 
 	@Override
 	public void run() {
 		try {
 			System.out.println("Waiting " + SETTLE_MILLIS + " millis for OSGi instance to settle...");
 			Thread.sleep(SETTLE_MILLIS);
 			
 			ServiceReference[] srefs = context.getServiceReferences(TestSuite.class.getName(), null);
 
 			if (srefs != null && srefs.length > 0) {				
 
 				for (ServiceReference sr : Arrays.asList(srefs)) {
 					TestSuite ts = (TestSuite) context.getService(sr);
 
 					if (ts != null)
 						try {
 							runTest(ts);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 				}
 			} else {
 				System.out.println("No " + TestSuite.class.getName() + " tests were found in the service registry.");
 			}
 		} catch (InvalidSyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 		}
 
 		// Test execution complete, now forcably shutdown the JVM.
 		if (errorOccurred)
 			System.exit(1);
 		else 
 			System.exit(0);
 	}
 
 	protected void runTest(TestSuite tc) throws IOException {
 		XmlNode out = new XmlNode("testsuite");
 		writeProperties(out);
 		ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
 		TestRunner tr = new TestRunner(new PrintStream(outbuf));
 		System.out.println("Running Test Suite: " + tc.getName());
 		TestResult result = tr.doRun(tc);
 
 		Enumeration<Test> te = tc.tests();
 		while (te.hasMoreElements()) {
 			Test test = te.nextElement();
			out.addChild(new XmlNode("testcase").addAttribute("classname", tc.getName()).addAttribute("name", test.toString()).addAttribute("time", "0.030"));

 		}
 
 		out.addAttribute("errors", "" + result.errorCount());
 		out.addAttribute("failures", "" + result.failureCount());
 
 		out.addAttribute("hostname", getHostName());
 		out.addAttribute("name", tc.getName());
 		out.addAttribute("tests", "" + tc.testCount());
		out.addAttribute("time", "0.100");
 		out.addAttribute("timestamp", getDateStamp());
 
 		out.addChild(new XmlNode("system-out", outbuf.toString()));
 		
 		File outFile = new File(outputDir, "TEST-" + tc.getName() + ".xml");
 		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
 		bos.write(out.toString().getBytes());
 		bos.flush();
 		bos.close();
 
 		System.out.print("Test Suite Complete: " + tc.getName());
 		System.out.println("  Results ~ Errors: " + result.errorCount() + " Failures: " + result.failureCount());
 		
 		if (result.errorCount() > 0 || result.failureCount() > 0)
 			errorOccurred = true;
 	}
 
 	private String getDateStamp() {
 		Calendar cal = Calendar.getInstance();
 		//2011-05-11T06:20:03
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		return sdf.format(cal.getTime());
 	}
 
 	private String getHostName() {
 
 		return "t410s";
 	}
 
 	private String wrapCdata(String in) {
 		return "<![CDATA[" + in + "]]>";
 	}
 
 	private void writeProperties(XmlNode out) {
 		for (Object key : System.getProperties().keySet())
 			out.addChild(new XmlNode("property").addAttribute("name", key.toString()).addAttribute("value", System.getProperty(key.toString())));
 	}
 }
