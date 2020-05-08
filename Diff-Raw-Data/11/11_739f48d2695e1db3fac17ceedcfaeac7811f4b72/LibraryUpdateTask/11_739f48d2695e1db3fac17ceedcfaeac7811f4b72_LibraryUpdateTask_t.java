 package net.sparktank.morrigan.library;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 
 import net.sparktank.morrigan.exceptions.MorriganException;
 import net.sparktank.morrigan.model.media.MediaLibrary;
 import net.sparktank.morrigan.playback.ImplException;
 import net.sparktank.morrigan.playback.PlaybackEngineFactory;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 /**
  * FIXME prevent concurrent executions of this task.
  */
 public class LibraryUpdateTask extends Job {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private final MediaLibrary library;
 	
 	public LibraryUpdateTask (MediaLibrary library) {
 		super("Update " + library.getListName());
 		this.library = library;
 		setUser(true);
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	/**
 	 * TODO use monitor.worked(1);
 	 */
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		monitor.beginTask("Updating library", IProgressMonitor.UNKNOWN);
 		
 		monitor.subTask("Scanning sources");
 		
 		List<String> supportedFormats;
 		try {
 			supportedFormats = Arrays.asList(PlaybackEngineFactory.getSupportedFormats());
 		} catch (ImplException e) {
 			monitor.done();
 			return new FailStatus("Failed to retrieve list of supported formats.", e);
 		}
 		
 		List<String> sources = null;
 		try {
 			sources = library.getSources();
 		} catch (DbException e) {
			monitor.done();
			return new FailStatus("Failed to retrieve list of media sources.", e);
 		}
 		
 		int filesAdded = 0;
 		
 		if (sources!=null) {
 			for (String source : sources) {
 				Stack<File> dirStack = new Stack<File>();
 				dirStack.push(new File(source));
 				
 				while (!dirStack.isEmpty()) {
 					File dirItem = dirStack.pop();
 					File[] listFiles = dirItem.listFiles();
 					
 					monitor.subTask("Scanning "+dirItem.getAbsolutePath());
 					
 					for (File file : listFiles) {
 						if (file.isDirectory()) {
 							dirStack.push(file);
 							
 						} else if (file.isFile()) {
 							String ext = file.getName();
 							ext = ext.substring(ext.lastIndexOf(".")+1).toLowerCase();
 							if (supportedFormats.contains(ext)) {
 								try {
 									if (library.addFile(file)) {
 										filesAdded++;
 									}
 								} catch (MorriganException e) {
 									// FIXME log this somewhere useful.
 									e.printStackTrace();
 								}
 							}
 						}
 						
 						if (monitor.isCanceled()) break;
 					}
 					
 					if (monitor.isCanceled()) break;
 				}
 				
 				if (monitor.isCanceled()) {
 					System.out.println("Task was canceled desu~.");
 					break;
 				}
 			}
 		}
 		
		try {
			library.reRead();
		} catch (MorriganException e) {
			monitor.done();
			return new FailStatus("Failed to refresh the library.", e);
		}
		
 		System.out.println("Added " + filesAdded + " files.");
 		
 		monitor.done();
 		return Status.OK_STATUS;
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public class FailStatus implements IStatus {
 		
 		private final String message;
 		private final Exception e;
 
 		public FailStatus (String message, Exception e) {
 			this.message = message;
 			this.e = e;
 		}
 		
 		@Override
 		public IStatus[] getChildren() {
 			return null;
 		}
 		
 		@Override
 		public int getCode() {
 			return 0;
 		}
 		
 		@Override
 		public Throwable getException() {
 			return e;
 		}
 		
 		@Override
 		public String getMessage() {
 			return message;
 		}
 		
 		@Override
 		public String getPlugin() {
 			return null;
 		}
 		
 		@Override
 		public int getSeverity() {
 			return 0;
 		}
 		
 		@Override
 		public boolean isMultiStatus() {
 			return false;
 		}
 		
 		@Override
 		public boolean isOK() {
 			return false;
 		}
 		
 		@Override
 		public boolean matches(int severityMask) {
 			return false;
 		}
 		
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
