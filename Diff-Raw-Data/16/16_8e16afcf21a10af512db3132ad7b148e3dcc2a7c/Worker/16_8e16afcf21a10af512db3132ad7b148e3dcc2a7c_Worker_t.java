 package org.ourgrid.browser.client;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.ourgrid.browser.client.executor.PythonExecutor;
 import org.ourgrid.browser.client.fs.FileSystem;
 import org.ourgrid.browser.client.fs.FileSystemCallback;
 import org.ourgrid.browser.client.fs.Uint8Array;
 import org.ourgrid.browser.shared.Execution;
 import org.ourgrid.browser.shared.ResultFile;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Worker implements EntryPoint {
 
 	/**
 	 * Create a remote service proxy to talk to the server-side Greeting service.
 	 */
 	private final WorkerServiceAsync service = GWT
 			.create(WorkerService.class);
 	
 	
 	@Override
 	public void onModuleLoad() {
 		final PythonExecutor executor = new PythonExecutor();
 		final FileSystem fs = new FileSystem();
 
 		getNextExecution(executor, fs);
 		
 	}
 
 	private void getNextExecution(final PythonExecutor executor,
 			final FileSystem fs) {
 		
 		printMessage("Fetching job to be executed...");
 		
 		service.getExecution(new AsyncCallback<Execution>() {
 			
 			@Override
 			public void onSuccess(final Execution execution) {
 				
 				if (execution == null) {
 					printMessage("There is no job to be executed. Thanks anyway ;)");
 					return;
 				}
 				
 				printMessage("Executing job [" + execution.getJobName() + "], task [" + execution.getId() + "], type: Python\n");
 				
 				printMessage("Initializing Python module...\n");
 				
 				executor.initialize();
 				
 				printMessage("Python module initialized!\n");
 				
 				printMessage("Staging input files...\n");
 				
 				final List<String> filesToStage = execution.getInputFiles(); 
 				
 				FileSystemCallback fsc = createUploadCallback(executor, fs,
 						execution, filesToStage.size() + 1);
 				
 				for (String file : filesToStage) {
 					String filePath = GWT.getHostPageBaseURL() + "jobs/"
 							+ execution.getJobName() + "/" + execution.getId() + "/" + file;
 					fs.getAndMountFile(filePath, file, fsc);
 				}
 				
 				String execFilePath = GWT.getHostPageBaseURL() + "jobs/"
 						+ execution.getJobName() + "/" + execution.getExecFile();
 				fs.getAndMountFile(execFilePath, execution.getExecFile(), fsc);
 			}
 
 			private FileSystemCallback createUploadCallback(
 					final PythonExecutor executor, final FileSystem fs,
 					final Execution execution, final int filesToStage) {
 				
 				FileSystemCallback fsc = new FileSystemCallback() {
 					
 					public void operationDone() {
 						super.operationDone();
 						if (getDoneOperations() == filesToStage) {
 							
 							printMessage("Input files staged!\n");
 							printMessage("Executing task...\n");
 							
 							executor.eval("execfile('" + execution.getExecFile() + "')");
 							
 							printMessage("Task executed!\n");
 							
 							uploadOutputs(execution, executor, fs);
 						}
 					}
 
 				};
 				return fsc;
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
				printMessage(caught.getMessage());
 			}
 		});
 	}
 	
 	private void uploadOutputs(final Execution execution, 
 			final PythonExecutor executor, final FileSystem fs) {
 		
 		printMessage("Uploading results...\n");
 		
 		List<ResultFile> files = new LinkedList<ResultFile>();
 		
 		for (String output : execution.getOutputFiles()) {
 			Uint8Array jso = fs.readFile(output);
 			
 			ResultFile file = new ResultFile();
 			file.setFileContents(jso.getByteArray());
 			file.setFileName(output);
 			files.add(file);
 		}
 		
 		service.sendResults(execution, files, new AsyncCallback<Void>() {
 
 			@Override
 			public void onSuccess(Void result) {
 				printMessage("Results uploaded!\n");
 				fs.clearSandbox();
 				executor.reset();
 				getNextExecution(executor, fs);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
				printMessage(caught.getMessage());
 			}
 		});
 	};
 	
 	public native void printMessage(String message) /*-{
 		$wnd.output.value += message;
 		$wnd.output.scrollTop = $wnd.output.scrollHeight;
 	}-*/;
 	
 }
