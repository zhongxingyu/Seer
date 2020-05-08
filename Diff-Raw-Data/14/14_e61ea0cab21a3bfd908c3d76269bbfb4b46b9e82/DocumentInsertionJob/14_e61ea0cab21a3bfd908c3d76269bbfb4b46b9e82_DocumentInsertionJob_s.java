 package org.dawnsci.macro.console;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Display;
 import org.python.pydev.debug.newconsole.prefs.InteractiveConsolePrefs;
 
 public class DocumentInsertionJob extends Job {
 	
 	private String        cmd;
 	private ISourceViewer viewer;
 
 	public DocumentInsertionJob(ISourceViewer viewer) {
 		super("Document Insertion Job");
 		this.viewer = viewer;
 		setPriority(Job.INTERACTIVE);
 		setUser(false);
 		setSystem(true);
 	}
 
 	@Override
 	public IStatus run(IProgressMonitor mon) {
 
 		if (!cmd.endsWith("\n")) cmd = cmd+"\n";
 
 		// If a single line, takes a while, the console will not respond until
 		// it has completed ( numpy console bug )
 		String[] cmds = cmd.split("\n");
 		for (final String c : cmds) {
 
 			if (mon.isCanceled()) return Status.CANCEL_STATUS;
 			
 			// Insert the line
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					try {
 						IDocument document = viewer.getDocument();
 						document.replace(document.getLength(), 0, c);
 						document.replace(document.getLength(), 0, "\n");
 					} catch (BadLocationException e) {
 						e.printStackTrace(); 
 					}
 
					if (InteractiveConsolePrefs.getFocusConsoleOnSendCommand() && viewer!=null) {
 						StyledText textWidget = viewer.getTextWidget();
 						if (textWidget != null) textWidget.setFocus();
 					}
 				}
 			});
 
 			try {
                 // We must deal with pydev sending the command
 				// Currently we assume that commands greater than 200ms are not common.
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				return new Status(IStatus.ERROR, "org.dawnsci.macro", "Interupted thread!", e);
 			}
 		}
 		return Status.OK_STATUS;
 
 	}
 
 	public void schedule(String cmd) {
 		this.cmd      = cmd;
 		schedule();
 	}
 }
