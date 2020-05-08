 package de.ptb.epics.eve.viewer.jobs.messages;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 import de.ptb.epics.eve.viewer.views.messages.ViewerMessage;
 
 /**
  * 
  * @author Marcus Michalsky
  * @since 1.4
  */
 public class Save extends Job {
 
 	private static Logger logger = Logger.getLogger(Save.class.getName());
 	
 	private String family = "file";
 	private String fileName;
 	private List<ViewerMessage> messages;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param name the name of the job
 	 * @param fileName the save file name
 	 * @param messages the messages that should be written to the file
 	 */
 	public Save(String name, String fileName, List<ViewerMessage> messages) {
 		super(name);
 		this.fileName = fileName;
 		this.messages = messages;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		// TODO When switching to Java 7 the new I/O should be used, see
 		// http://docs.oracle.com/javase/tutorial/essential/io/file.html#textfiles
 
 		monitor.beginTask("Saving messages", this.messages.size());
 
 		BufferedWriter bufferedWriter = null;
 		try {
 			bufferedWriter = new BufferedWriter(new FileWriter(fileName));
 
 			for (ViewerMessage msg : this.messages) {
 				monitor.subTask("writing message: " + msg.getMessage());
				bufferedWriter.write(msg.getDate() + " " + "["
 						+ msg.getMessageType() + "] " + msg.getMessageSource()
 						+ ": " + msg.getMessage());
 				bufferedWriter.newLine();
 				monitor.worked(1);
 			}
 		} catch (FileNotFoundException e) {
 			logger.error(e.getMessage(), e);
 			return Status.CANCEL_STATUS;
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			return Status.CANCEL_STATUS;
 		} finally {
 			try {
 				if (bufferedWriter != null) {
 					bufferedWriter.flush();
 					bufferedWriter.close();
 				}
 			} catch (IOException e) {
 				logger.error(e.getMessage(), e);
 				return Status.CANCEL_STATUS;
 			}
 		}
 		monitor.done();
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean belongsTo(Object family) {
 		return family.equals(this.family);
 	}
 }
