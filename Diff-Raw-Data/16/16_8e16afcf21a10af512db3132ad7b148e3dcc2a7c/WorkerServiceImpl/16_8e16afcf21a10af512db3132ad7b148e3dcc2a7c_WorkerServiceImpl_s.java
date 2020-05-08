 package org.ourgrid.browser.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.ourgrid.browser.client.WorkerService;
 import org.ourgrid.browser.shared.Execution;
 import org.ourgrid.browser.shared.ResultFile;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class WorkerServiceImpl extends RemoteServiceServlet implements
 		WorkerService {
 
 	private static final String JOBS_DIR = "/jobs";
 	
 	@Override
 	public Execution getExecution() throws Exception {
 		
 		Thread.sleep(2000);
 		
		File jobsDir = new File(new File(getServletContext().getContextPath()).getAbsolutePath() + JOBS_DIR);
 		
 		for (File jobDir : jobsDir.listFiles()) {
 			
 			Properties jobProperties = new Properties();
 			jobProperties.load(new FileInputStream(jobDir.getCanonicalPath() + "/job"));
 			
 			String inputStr = jobProperties.getProperty("inputs");
 			String appStr = jobProperties.getProperty("app");
 			String outputStr = jobProperties.getProperty("outputs");
 			
 			Execution execution = new Execution();
 			execution.setExecFile(appStr);
 			execution.setInputFiles(Arrays.asList(inputStr.split(",")));
 			execution.setOutputFiles(Arrays.asList(outputStr.split(",")));
 			execution.setJobName(jobDir.getName());
 			
 			for (File wuDir : jobDir.listFiles()) {
 				if (wuDir.isDirectory()) {
 					
 					List<String> outputFiles = execution.getOutputFiles();
 					for (String outputFile : outputFiles) {
 						if (!new File(wuDir.getAbsolutePath() + "/" + outputFile).exists()) {
 							execution.setId(wuDir.getName());
 							return execution;
 						}
 					}
 				}
 			}
 			
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void sendResults(Execution execution, List<ResultFile> outputs)
 			throws Exception {
 		
		String wuPath = new File(getServletContext().getContextPath()).getAbsolutePath() + JOBS_DIR + 
				"/" + execution.getJobName() + "/" + execution.getId();
 		
 		for (ResultFile resultFile : outputs) {
 			File outputFile = new File(wuPath + "/" + resultFile.getFileName());
 			IOUtils.write(resultFile.getFileContents(), new FileOutputStream(
 					outputFile));
 		}
 		
 	}
 
 }
