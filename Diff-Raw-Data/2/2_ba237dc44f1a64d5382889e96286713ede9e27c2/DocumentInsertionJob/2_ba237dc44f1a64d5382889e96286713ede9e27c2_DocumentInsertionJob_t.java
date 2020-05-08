 package org.dawnsci.macro.console;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawnsci.macro.Activator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.dawnsci.plotting.api.PlottingFactory;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Display;
 import org.python.pydev.debug.newconsole.prefs.InteractiveConsolePrefs;
 import org.python.pydev.shared_interactive_console.console.ui.internal.ScriptConsoleViewer;
 
 public class DocumentInsertionJob extends Job {
 	
 	private ISourceViewer         viewer;
 	private BlockingDeque<String> queue;
 	private DocumentInserter      inserter;
 	private IPreferenceStore store;
 	
 	private static final String defaultPause = " org.dawnsci.macro.console.defaultPause";
 	private static final String numpyPause   = " org.dawnsci.macro.console.numpyPause";
 	private static final String plottingPause= " org.dawnsci.macro.console.plottingPause";
 
 	public DocumentInsertionJob(DocumentInserter  inserter, ISourceViewer viewer) {
 		
 		super("Document Insertion Job");
 		this.inserter   = inserter;
 		this.viewer = viewer;
 		setPriority(Job.INTERACTIVE);
 		setUser(false);
 		setSystem(true);
 		
 		this.queue = new LinkedBlockingDeque<String>();
 		schedule();
 		
 		this.store = Activator.getPlugin().getPreferenceStore();
		store.setDefault(defaultPause,  350);
 		store.setDefault(plottingPause, 600);
 		store.setDefault(numpyPause,    2000);
 	}
 
 	@Override
 	public IStatus run(IProgressMonitor mon) {
 
 		String cmd;
 		try {
 			while((cmd = queue.take())!=null) {
 				
 				if (!inserter.isConnected()) continue; 
 
 				if (mon.isCanceled()) return Status.CANCEL_STATUS;
 
 				if (!cmd.endsWith("\n")) cmd = cmd+"\n";
 				
 				
 				
 				checkSetup(cmd);
 
 				// If a single line, takes a while, the console will not respond until
 				// it has completed ( numpy console bug )
 				String[] cmds = cmd.split("\n");
 				for (final String c : cmds) {
 					
 					// Insert the line
 					Display.getDefault().syncExec(new Runnable() {
 						public void run() {
 							try {
 								IDocument document = viewer.getDocument();
 								document.replace(document.getLength(), 0, c);
 								document.replace(document.getLength(), 0, "\n");
 							    viewer.setSelectedRange(document.getLength(), -1);
 							    
 							} catch (BadLocationException e) {
 								e.printStackTrace(); 
 							}
 
 							if (InteractiveConsolePrefs.getFocusConsoleOnSendCommand() && viewer!=null && viewer instanceof ScriptConsoleViewer) {
 								StyledText textWidget = viewer.getTextWidget();
 								if (textWidget != null) textWidget.setFocus();
 							}
 						}
 					});
 
 					try {
 			            // We must deal with pydev sending the command
 						// Currently we assume that commands greater than 200ms are not common.
 						Thread.sleep(store.getLong(defaultPause));
 					} catch (InterruptedException e) {
 						continue;
 					}
 				}
 			}
 		} catch (InterruptedException e) {
 			// This is allowed when the job is being stopped
 			return Status.CANCEL_STATUS;
 		}
 		return Status.OK_STATUS;
 
 	}
 	
 	
 	/**
 	 * This method inserts various setup commands if they cannot be found.
 	 * This step ensures that future plotting system commands echoed to python
 	 * are less likely to stack trace in the console.
 	 * 
 	 * @param cmd
 	 * @return
 	 */
 	private void checkSetup(String cmd) {
 		
 		
 		// Check numpy
 		if (inserter.getType()==InsertionType.PYTHON) {
 
 			checkAdd("# Turn py4j on under Window->Preferences->Py4j Default Server > 'Py4j active'", cmd, store.getLong(defaultPause));
 			checkAdd("import numpy", cmd, store.getLong(numpyPause));
 			
 		}
 			
 		if (cmd.indexOf("ps = dnp.plot.getPlottingSystem(")<0) {
 			final List<Boolean> inserted = new ArrayList<Boolean>(1);
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					IDocument document = viewer.getDocument();
 	                if (document.get().indexOf("ps = dnp.plot.getPlottingSystem(")<0) {
 						try {
 							// We see if the active part might be a plotting system and take this name
 							IPlottingSystem active = (IPlottingSystem)EclipseUtils.getPage().getActivePart().getAdapter(IPlottingSystem.class);
 							if (active==null) active = (IPlottingSystem)EclipseUtils.getPage().getActiveEditor().getAdapter(IPlottingSystem.class);
 							if (active==null) active = PlottingFactory.getPlottingSystems()!=null
 									                 ? PlottingFactory.getPlottingSystems()[0]
 									                 : null;
 							StringBuilder buf = new StringBuilder("ps = dnp.plot.getPlottingSystem(");
 							if (active!=null) {
 								buf.append("\"");
 								buf.append(active.getPlotName());
 								buf.append("\"");
 							}
 							buf.append(")\n");
 							
 							document.replace(document.getLength(), 0, buf.toString());
 						    viewer.setSelectedRange(document.getLength(), -1);
 							inserted.add(Boolean.TRUE);
 							
 						} catch (BadLocationException e) {
 							e.printStackTrace();
 						}
 	                }
 				}
 			});
 			if (inserted.size()==1 && inserted.get(0)) {
 				try {
 					Thread.sleep(store.getLong(plottingPause)); // Load system - how longs a piece of string?
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void checkAdd(final String toAdd, String cmd, long time) {
 		
 		final List<Boolean> inserted = new ArrayList<Boolean>(1);
 		if (cmd.indexOf(toAdd)>-1) return;
 		
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				try {
 					IDocument document = viewer.getDocument();
 					if (document.get().indexOf(toAdd)<0) {
 						document.replace(document.getLength(), 0, toAdd+"\n");
 						inserted.add(Boolean.TRUE);
 					}
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		if (inserted.size()==1 && inserted.get(0)) {
 			try {
 				Thread.sleep(time); // Load system - how longs a piece of string?
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public void stop() {
 		super.cancel();
 		try {
 		    getThread().interrupt(); // Incase waiting on a take
 		} catch (Throwable swallowed) {
 			// we just try this in case
 		}
 	}
 
 	/**
 	 * Commands must be added in order
 	 * @param cmd
 	 */
 	public synchronized void add(String cmd) {
 		String next = queue.peek();
 		if (cmd.equals(next)) return; // Two the same not usually required.
 		queue.add(cmd);
 	}
 }
