 package gov.va.mumps.debug.ui.console;
 
 import gov.va.mumps.launching.ReadCommandListener;
 import gov.va.mumps.launching.InputReadyListener;
 import gov.va.mumps.launching.WriteCommandListener;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.console.AbstractConsole;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.part.IPageBookViewPage;
 
 public class MDevConsole extends AbstractConsole implements ReadCommandListener, WriteCommandListener {
 	
 	private boolean readingUserInput;
 	private int maxCharInput;
 	private MDevConsolePage pageBookView;
 	private List<InputReadyListener> inputReadyListeners;
 
 	public MDevConsole(String name, String consoleType,
 			ImageDescriptor imageDescriptor, boolean autoLifecycle) {
 		super(name, consoleType, imageDescriptor, autoLifecycle);
 		
 		inputReadyListeners = new LinkedList<InputReadyListener>();
 	}
 
 	@Override
 	public IPageBookViewPage createPage(IConsoleView view) {
 		pageBookView = new MDevConsolePage(this);
 		return pageBookView;
 	}
 
 	public boolean isReadingUserInput() {
 		return readingUserInput;
 	}
 	
 	public void setReadingInput(boolean readingUserInput) {
 		this.readingUserInput = readingUserInput ;
 	}
 	
 	public int getMaxCharInput() {
 		return maxCharInput;
 	}
 
 	@Override
 	public void handleWriteCommand(final String output) { //TODO: bug: write command could theoretically come in before the main gui thread creates the pageBookView causing a NPE
 		//TODO: bug? is this bein called twice? why? I'm not seeing double input but that may just becaues both async threads run so closely they grab append the same value to the same orig value
 		pageBookView.getSite().getShell().getDisplay().asyncExec(new Runnable() { //Only the async thread can access SWT controls
 			
 			@Override
 			public void run() {
 				pageBookView.appendText(output);
 				//TODO: the classes for this need to be organized better? this seems kind of funny
 			}
 		});
 	}
 
 	@Override
 	public void handleReadCommand(int maxCharInput) {
 		this.maxCharInput = maxCharInput;
 		readingUserInput = true;
 		pageBookView.getSite().getShell().getDisplay().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
				pageBookView.setFocus();
 			}
 		});
 		
 	}
 	
 	public void addInputReadyListener(InputReadyListener listener) {
 		inputReadyListeners.add(listener);
 	}
 	
 	public void reemoveInputReadyListener(InputReadyListener listener) {
 		inputReadyListeners.remove(listener);
 	}
 	
 	public Iterator<InputReadyListener> getInputReadyInputListeners() {
 		return inputReadyListeners.listIterator();
 	}
 
 }
