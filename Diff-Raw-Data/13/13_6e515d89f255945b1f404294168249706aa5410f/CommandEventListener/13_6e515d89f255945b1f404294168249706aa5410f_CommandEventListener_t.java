 package org.hannes.ui;
 
 import java.util.concurrent.Callable;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.hannes.concurrency.ConcurrencyUtils;
 import org.hannes.concurrency.FutureListener;
 
 /**
  * Detects devices when an event has been received
  * 
  * @author red
  *
  */
 public class CommandEventListener implements Listener {
 
 	/**
 	 * The command and its arguments
 	 */
 	private final String[] command;
 	
 	/**
 	 * The current display
 	 */
 	private final Display display;
 
 	/**
 	 * Creates a default command event listener
 	 * 
 	 * @param command
 	 */
 	public CommandEventListener(Display display, String... command) {
 		this.command = command;
 		this.display = display;
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		/*
		 * 
		 */
		final ProgressDialog progressDialog = new ProgressDialog(display, new Shell(SWT.SHELL_TRIM & (~SWT.RESIZE)));
		
		/*
 		 * Open the progress dialog
 		 */
 		progressDialog.open();
 		
 		/*
 		 * Execute the command
 		 */
 		ConcurrencyUtils.execute(
 				/*
 				 * The future listener will dispose of the progress
 				 * dialog and show an error message should an exception
 				 * have occurred
 				 */
 				new FutureListener<Process>() {
 
 					@Override
 					public void listen(Process process) throws Exception {
 						process.waitFor();
 						display.asyncExec(new Runnable() {
 							
 							@Override
 							public void run() {
 								progressDialog.close();
 							}
 							
 						});
 					}
 
 					@Override
 					public void exceptionCaught(Throwable t) {
 						/*
 						 * TODO: Proper exception handling
 						 */
 						t.printStackTrace();
 					}
 				},
 				
 				/*
 				 * Executes the command on a new thread
 				 */
 				new Callable<Process>() {
 
 					@Override
 					public Process call() throws Exception {
 						return Runtime.getRuntime().exec(command);
 					}
 					
 				}
 		);
 	}
 
 }
