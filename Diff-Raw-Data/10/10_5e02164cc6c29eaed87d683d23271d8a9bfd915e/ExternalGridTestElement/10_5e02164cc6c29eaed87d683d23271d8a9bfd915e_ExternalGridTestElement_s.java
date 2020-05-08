 package org.vpac.grisu.clients.gridTests.testElements;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 import org.vpac.grisu.clients.gridTests.GridExternalTestInfoImpl;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.clientexceptions.MdsInformationException;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.grisu.frontend.control.login.ServiceInterfaceFactory;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.utils.SeveralXMLHelpers;
 import org.w3c.dom.Document;
 
 public class ExternalGridTestElement extends GridTestElement {
 
 	public static void main(String[] args) throws Exception {
 
 		String username = args[0];
 		char[] password = args[1].toCharArray();
 
 		LoginParams loginParams = new LoginParams(
 		// "http://localhost:8080/grisu-cxf/services/grisu",
 				// "https://ngportaldev.vpac.org/grisu-ws/services/grisu",
 				"Local", username, password);
 
 		final ServiceInterface si = ServiceInterfaceFactory
 				.createInterface(loginParams);
 
 		Document jsdl = SeveralXMLHelpers
 				.loadXMLFile(new File(
 						"/home/markus/Workspaces/Grisu-SNAPSHOT/grisu/frontend/grisu-grid-tests/tests/pbstest/pbsTest.jsdl"));
 
 		try {
 			si.kill("pbsTest", true);
 		} catch (Exception e) {
 			//
 		}
 
 		JobObject job = new JobObject(si, jsdl);
 
 		job.setSubmissionLocation("sque@edda-m:ng2.vpac.org");
 		job.createJob("/ARCS/NGAdmin");
 
 		job.submitJob();
 
 		job.waitForJobToFinish(5);
 
 		File stdout = job.getStdOutFile();
 		StringBuffer output = new StringBuffer();
 
 		Properties testProperties = new Properties();
 		testProperties
 				.load(new FileInputStream(
 						"/home/markus/Workspaces/Grisu-SNAPSHOT/grisu/frontend/grisu-grid-tests/tests/pbstest/grisu-test.properties"));
 
 		// int result = executeScript(testProperties, new
 		// File("/home/markus/Workspaces/Grisu-SNAPSHOT/grisu/frontend/grisu-grid-tests/tests/pbstest/"),
 		// job.getStdOutFile().getParentFile(), output);
 
 		System.out.println("Output: " + output.toString());
 
 	}
 
 	public ExternalGridTestElement(GridExternalTestInfoImpl info,
 			String version, String subLoc, String fqan)
 			throws MdsInformationException {
 
 		super(info, version, subLoc, fqan);
 
 	}
 
 	@Override
 	protected boolean checkJobSuccess() {
 
 		try {
 			String jobDir = jobObject.getJobDirectoryUrl();
 			addMessage("Downloading output files from jobdirectory: " + jobDir);
 
			File localCacheDir = null;

 			for (String filename : ((GridExternalTestInfoImpl) getTestInfo())
 					.getOutputFiles()) {
 				addMessage("Downloading: " + filename + "...");
 				File file = jobObject.downloadAndCacheOutputFile(filename);
				if (localCacheDir == null) {
					localCacheDir = file.getParentFile();
				}
 			}
 			StringBuffer output = new StringBuffer();
 
 			int out = executeScript(localCacheDir, output);
 
 			addMessage(output.toString());
 
 			if (out == 0) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			addMessage("Could not check job success: "
 					+ e.getLocalizedMessage());
 			setPossibleExceptionForCurrentStage(e);
 			return false;
 		}
 	}
 
 	// @Override
 	// public String getApplicationSupported() {
 	// return application;
 	// }
 	//
 	// @Override
 	// public String getTestDescription() {
 	// return description;
 	// }
 	//
 	// @Override
 	// public String getTestName() {
 	// return testname;
 	// }
 	//
 	// @Override
 	// protected boolean useMDS() {
 	// return useMds;
 	// }
 
 	@Override
 	protected JobObject createJobObject() throws MdsInformationException {
 
 		Document jsdlDoc = ((GridExternalTestInfoImpl) getTestInfo())
 				.getJsdlDoc();
 
 		// System.out.println(SeveralXMLHelpers.toStringWithoutAnnoyingExceptions(jsdlDoc));
 
 		JobObject job = new JobObject(serviceInterface, jsdlDoc);
 		for (String input : ((GridExternalTestInfoImpl) getTestInfo())
 				.getInputFiles()) {
 			job.addInputFileUrl(((GridExternalTestInfoImpl) getTestInfo())
 					.getTestBaseDir()
 					+ File.separator + input);
 		}
 
 		return job;
 
 	}
 
 	private int executeScript(File outputDir, StringBuffer output) {
 
 		String s = null;
 
 		try {
 			String externalCommand = ((GridExternalTestInfoImpl) getTestInfo())
 					.getCommand();
 			if (outputDir != null) {
 				externalCommand = externalCommand.replaceAll("\\$OUTPUT_DIR",
 						outputDir.getPath());
 			}
 
 			System.out.println("Running script using: " + externalCommand);
 
 			Process p = Runtime.getRuntime().exec(externalCommand);
 			p.waitFor();
 
 			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p
 					.getInputStream()));
 
 			BufferedReader stdError = new BufferedReader(new InputStreamReader(
 					p.getErrorStream()));
 
 			// read the output from the command
 			StringBuffer out = new StringBuffer("Stdout:\n");
 			while ((s = stdOut.readLine()) != null) {
 				out.append(s + "\n");
 			}
 			out.append("\n");
 			// read any errors from the attempted command
 			StringBuffer err = new StringBuffer("Stderr:\n");
 			while ((s = stdError.readLine()) != null) {
 				err.append(s + "\n");
 			}
 			err.append("\n");
 
 			output.append(out);
 			output.append(err);
 
 			int exitValue = p.exitValue();
 			output.append("\nExitValue: " + exitValue);
 
 			return exitValue;
 		} catch (Exception e) {
 			e.printStackTrace();
 			output.append("Execution of external command failed:\n"
 					+ e.getLocalizedMessage());
 			return -1;
 		}
 	}
 
 }
