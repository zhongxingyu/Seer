 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.rcp.views;
 
 import gda.configuration.properties.LocalProperties;
 import gda.jython.IAllScanDataPointsObserver;
 import gda.jython.IJythonContext;
 import gda.jython.Jython;
 import gda.jython.JythonServerFacade;
 import gda.jython.Terminal;
 import gda.jython.gui.JythonGuiConstants;
 import gda.rcp.GDAClientActivator;
 import gda.rcp.util.ScanDataPointFormatterUtils;
 import gda.rcp.views.dashboard.DashboardView;
 import gda.rcp.views.dashboard.SimpleScannableObject;
 import gda.scan.IScanDataPoint;
 import gda.scan.ScanCompletedEvent;
 import gda.scan.ScanDataPointFormatter;
 import gda.util.PropertyUtils;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Vector;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.actions.TextViewerAction;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.ClientManager;
 import uk.ac.gda.client.HelpHandler;
 
 import com.swtdesigner.SWTResourceManager;
 
 /**
  * Design to look and act like a command terminal for the GDA Jython Interpreter. NOTE: Currently this class does not
  * work unless it is a top most view when the user goes to a perspective containing it. This is because eclipse only
  * makes a view when it is visible. To fix this UIScanDataPointService could be used to tell this view of previous scan
  * data points missed by the view not being present. For now always use this view as a default visible one and this
  * issue will not be as bad.
  */
 public class JythonTerminalView extends ViewPart implements Runnable, IAllScanDataPointsObserver, Terminal {
 
 	// The output panel is similar to the details panel at the bottom of the
 	// "Variables" view used when debugging. See these classes:
 	//
 	// org.eclipse.debug.internal.ui.views.variables.details.DefaultDetailPane
 	// org.eclipse.debug.internal.ui.actions.variables.details.DetailPaneWordWrapAction
 
 	/**
 	 * ID of this view. Referenced by string to reduce dependencies in places, refactor will not work.
 	 */
 	public static final String ID = "gda.rcp.jythonterminalview";
 
 	private static final Logger logger = LoggerFactory.getLogger(JythonTerminalView.class);
 	private static final String NORMALPROMPT = ">>>";
 	private static final String ADDITONALINPUTPROMPT = "...";
 	private static final String RAWINPUTPROMPT = "-->";
 	private static final int maxCommandsToSave = 100;
 
 	private static Boolean scrollLock = false;
 	private static Boolean moveToTopOnUpdate = false;
 
 	private volatile String txtInputText = "";
 	private volatile String txtPromptText = "";
 	private volatile int caretPosition = 0;
 
 	private Text txtInput;
 	/** {@link Document} containing output text */
 	JythonTerminalDocument outputDoc;
 
 	/** {@link TextViewer} that displays the output document */
 	TextViewer outputTextViewer;
 
 	String txtOutputLast; //copy of string sent to outputDoc.set()
 	private Text txtPrompt;
 
 	private Vector<String> cmdHistory = new Vector<String>(0);
 	private int cmdHistory_index = 0;
 	private String commandFileName;
 	private JythonServerFacade jsf;
 	private boolean runFromHistory = false;
 	private String currentCmd;
 	private boolean printOutput = false;
 
 	private FileWriter outputFile;
 
 	private Object lastScanDataPointUniqueName;
 
 	// to update the output area from the buffer
 	UpdaterRunner updaterRunner = new UpdaterRunner();
 	volatile boolean outputBufferUpdated = false;
 
 	private ScanDataPointFormatter scanDataPointFormatter;
 
 	private AutoCompleter autoCompleter;
 
 	private HelpHandler helpHandler;
 
 	private Composite root;
 
 
 	/***/
 	public JythonTerminalView() {
 		try {
 			this.scanDataPointFormatter = ScanDataPointFormatterUtils.getDefinedFormatter();
 		} catch (Exception ne) {
 			logger.error("Cannot read formatter extension point", ne);
 		}
 	}
 
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 
 		if (!ClientManager.isTestingMode()) {
 			jsf = JythonServerFacade.getInstance();
 			jsf.addIObserver(this);
 		}
 
 		Object namedService = GDAClientActivator.getNamedService(HelpHandler.class, null);
 		if( namedService != null)
 			helpHandler = (HelpHandler)namedService;
 		
 		fetchOldHistory();
 
 		// start the thread to update the output area from the buffer
 		uk.ac.gda.util.ThreadManager.getThread(updaterRunner,
 				getClass().getName() + " " + getName() + "  updater runner").start();
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 
 		Font font = new Font(parent.getDisplay(), "DejaVu Sans Mono", 10, SWT.NORMAL);
 		int tabSize = 4;
 
 		{
 			root = new Composite(parent, SWT.NONE);
 			root.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 			GridLayout gl = new GridLayout();
 			gl.horizontalSpacing = 0;
 			gl.verticalSpacing = 0;
 			gl.marginWidth = 0;
 			gl.marginHeight = 0;
 			root.setLayout(gl);
 			{
 				outputTextViewer = new TextViewer(root, SWT.H_SCROLL | SWT.V_SCROLL);
 				outputTextViewer.getTextWidget().setLayoutData(new GridData(GridData.FILL_BOTH));
 				outputTextViewer.setEditable(false);
 				outputTextViewer.getTextWidget().setFont(font);
 				outputTextViewer.getTextWidget().setTabs(tabSize);
 				outputDoc = new JythonTerminalDocument();
 				outputTextViewer.setDocument(outputDoc);
 				txtOutputLast = "";
 
 				createContextMenuForOutputBox();
 				wordWrapAction.run(); // to set initial word wrap state
 
 				if (!ClientManager.isTestingMode()) {
 					String startupOutput = jsf.getStartupOutput();
 					if (startupOutput != null) {
 						setOutputText(startupOutput);
 					}
 				}
 			}
 
 			Composite inputHolder = new Composite(root, SWT.None);
 			inputHolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 			inputHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			GridLayout layout = new GridLayout(2, false);
 			layout.horizontalSpacing = 0;
 			layout.verticalSpacing = 0;
 			layout.marginWidth = 0;
 			layout.marginHeight = 0;
 			inputHolder.setLayout(layout);
 
 			{
 				txtPrompt = new Text(inputHolder, SWT.None);
 				txtPrompt.setEditable(false);
 				txtPrompt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 				txtPrompt.setLayoutData(new GridData(GridData.FILL_VERTICAL));
 				txtPrompt.setText(NORMALPROMPT);
 				txtPrompt.setFont(font);
 				txtPrompt.setTabs(tabSize);
 			}
 			{
 				txtInput = new Text(inputHolder, SWT.NONE);
 				txtInput.setFont(font);
 				txtInput.setTabs(tabSize);
 				txtInput.addListener(SWT.DefaultSelection, new Listener() {
 					@Override
 					public void handleEvent(Event e) {
 						JythonTerminalView.this.txtInput_ActionPerformed();
 					}
 				});
 				txtInput.addKeyListener(new KeyAdapter() {
 					@Override
 					public void keyPressed(KeyEvent e) {
 						handleTxtInputKeyEvent(e);
 					}
 				});
 
 				txtInput.addTraverseListener(new TraverseListener() {
 					@Override
 					public void keyTraversed(TraverseEvent e) {
 						switch (e.detail) {
 						case SWT.TRAVERSE_TAB_NEXT:
 						case SWT.TRAVERSE_TAB_PREVIOUS:
 							e.doit = false;
 							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 								@Override
 								public void run() {
									txtInput.insert(String.valueOf(SWT.TAB)); // also grab the focus back!
 
 								}
 							});
 						}
 					}
 				});
 
 				txtInput.setLayoutData(new GridData(GridData.FILL_BOTH));
 				autoCompleter = new AutoCompleter(txtInput);
 			}
 		}
 		setHelpContextIDS();
 	}
 
 	private void setHelpContextIDS() {
 		IWorkbenchHelpSystem helpSystem = getSite().getWorkbenchWindow().getWorkbench().getHelpSystem();
 		helpSystem.setHelp(root, "uk.ac.gda.client.jython_console");
 	}
 
 	/**
 	 * Lock to allow exclusive access to the current content of the output text box, and the copy of the current
 	 * content.
 	 */
 	final Lock outputLock = new ReentrantLock(true);
 
 	protected void setOutputText(String text) {
 		outputLock.lock();
 		try {
 			outputDoc.set(text);
 			txtOutputLast = text;
 			outputBuffer.setLength(0);
 		} finally {
 			outputLock.unlock();
 		}
 	}
 
 	private void createContextMenuForOutputBox() {
 		// We only want one instance of this action, that will hold the current
 		// setting
 		wordWrapAction = new TextViewerWordWrapToggleAction(outputTextViewer);
 
 		// The copy and select all actions will get recreated each time the
 		// context menu is opened, so that they reflect whether or not text is
 		// currently selected
 
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				fillContextMenuForOutputBox(manager);
 			}
 		});
 
 		Menu menu = menuMgr.createContextMenu(outputTextViewer.getTextWidget());
 		outputTextViewer.getTextWidget().setMenu(menu);
 	}
 
 	private TextViewerAction copyAction;
 	private TextViewerAction selectAllAction;
 	private TextViewerWordWrapToggleAction wordWrapAction;
 
 
 	private void fillContextMenuForOutputBox(IMenuManager menuMgr) {
 		copyAction = new TextViewerAction(outputTextViewer, ITextOperationTarget.COPY);
 		copyAction.setText("&Copy");
 		copyAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
 
 		selectAllAction = new TextViewerAction(outputTextViewer, ITextOperationTarget.SELECT_ALL);
 		selectAllAction.setText("Select &All");
 		selectAllAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
 
 		menuMgr.add(copyAction);
 		menuMgr.add(selectAllAction);
 		menuMgr.add(new Separator());
 		menuMgr.add(wordWrapAction);
 	}
 
 	public String getName() {
 		return JythonGuiConstants.TERMINALNAME;
 	}
 
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		// if from scans then objects are in the format String, ScanDataPoint
 		if (changeCode instanceof IScanDataPoint && theObserved instanceof JythonServerFacade) {
 			IScanDataPoint sdpt = (IScanDataPoint) changeCode;
 			// print headerString if last printed was different
 			String uniqueName = sdpt.getUniqueName();
 			if (uniqueName != null) {
 				if (lastScanDataPointUniqueName == null || !lastScanDataPointUniqueName.equals(uniqueName)) {
 					appendOutput(sdpt.getHeaderString(scanDataPointFormatter) + "\n");
 					lastScanDataPointUniqueName = uniqueName;
 				}
 			}
 			// always print the point to the terminal
 			appendOutput(sdpt.toFormattedString(scanDataPointFormatter) + "\n");
 		} else if (theObserved instanceof JythonServerFacade && changeCode instanceof ScanCompletedEvent) {
 			// BEEP to info users scan completed.
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					logger.debug("======= make system beep =======");
 					PlatformUI.getWorkbench().getDisplay().beep();
 				}
 			});
 		} else if (changeCode instanceof String) {
 			String message = (String) changeCode;
 
 			if (message.compareTo(Jython.RAWINPUTREQUESTED) == 0) {
 				// change prompt and next input will go through a different
 				// method call
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						txtPrompt.setText(RAWINPUTPROMPT);
 						txtInput.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
 						// clear the command-line
 						txtInput.setText("");
 					}
 				});
 			} else if (message.compareTo(Jython.RAWINPUTRECEIVED) == 0) {
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						// change prompt back to usual
 						txtPrompt.setText(NORMALPROMPT);
 						txtInput.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 					}
 				});
 			}
 		}
 	}
 
 	@Override
 	public void write(byte[] data) {
 		String output = "encoding error!!!";
 		try {
 			output = new String(data, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.warn("UnsupportedEncodingException while converting to UTF-8 in Jython Console: " + e.getMessage(),
 					e);
 		}
 		write(output);
 	}
 
 	@Override
 	public void write(String output) {
 		appendOutput(output);
 	}
 
 	@Override
 	public void setFocus() {
 		if (txtInput != null) {
 			txtInput.setFocus();
 		}
 	}
 
 	@Override
 	public void run() {
 		if (jsf == null) {
 			jsf = JythonServerFacade.getInstance();
 			jsf.addIObserver(this);
 		}
 
 		// print out what was typed
 
 		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtPromptText = txtPrompt.getText();
 				txtInputText = txtInput.getText();
 			}
 		});
 
 		appendOutput(txtPromptText + txtInputText + "\n");
 		// if this is the start of a new command
 		if (txtPromptText.compareTo(NORMALPROMPT) == 0) {
 			String typedCmd = txtInputText;
 			// add the command to cmdHistory
 			if (cmdHistory.size() == 0) {
 				addCommandToHistory(typedCmd);
 			} else if ((typedCmd.compareTo("") != 0)
 					&& (typedCmd.compareTo(cmdHistory.get(cmdHistory.size() - 1)) != 0)) {
 				addCommandToHistory(typedCmd);
 			}
 			if (cmdHistory_index != cmdHistory.size() - 2) {
 				runFromHistory = true;
 			}
 			// run the command
 			boolean needMore = jsf.runsource(typedCmd, getName());
 			// if not a complete Jython command
 			if (needMore) {
 				// save the command so far
 				currentCmd = typedCmd;
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						// change the prompt
 						txtPrompt.setText(ADDITONALINPUTPROMPT);
 						// clear the command-line
 						txtInput.setText("");
 					}
 				});
 			} else {
 				currentCmd = "";
 				// reset the cmdHistory pointer if we just added a new line
 				cmdHistory_index = cmdHistory.size();
 				runFromHistory = false;
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						// clear the command-line
 						txtInput.setText("");
 					}
 				});
 			}
 		}
 		// if we are part way through a multi-line command
 		else if (txtPromptText.compareTo(ADDITONALINPUTPROMPT) == 0) {
 			// add to history if something was entered
 			if (txtInputText.compareTo("") != 0) {
 				if (cmdHistory.size() == 0) {
 					addCommandToHistory(txtInputText);
 				} else if ((txtInputText.compareTo("") != 0)
 						&& (txtInputText.compareTo(cmdHistory.get(cmdHistory.size() - 1)) != 0)) {
 					addCommandToHistory(txtInputText);
 				}
 				if (cmdHistory_index != cmdHistory.size() - 2) {
 					runFromHistory = true;
 				}
 			}
 			// append to whole command
 			currentCmd += "\n" + txtInputText;
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					txtInput.setEnabled(false);
 				}
 			});
 			// run the command
 			boolean needMore = jsf.runsource("\n" + currentCmd, getName());
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					txtInput.setEnabled(true);
 					txtInput.forceFocus();
 				}
 			});
 
 			// if not a complete Jython command
 			if (needMore) {
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {// change the prompt
 						txtPrompt.setText(ADDITONALINPUTPROMPT);
 						// clear the command-line
 						txtInput.setText("");
 					}
 				});
 			} else {
 				currentCmd = "";
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {// change the prompt
 						txtPrompt.setText(NORMALPROMPT);
 						// clear the command-line
 						txtInput.setText("");
 					}
 				});
 				// reset the cmdHistory pointer
 				cmdHistory_index = cmdHistory.size();
 				runFromHistory = false;
 			}
 		}
 		// else a script has asked for input
 		else if (txtPromptText.compareTo(RAWINPUTPROMPT) == 0) {
 			// get the next input from the user
 			jsf.setRawInput(txtInputText);
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {// clear the command-line
 					txtInput.setText("");
 				}
 			});
 		}
 	}
 
 	private void fetchOldHistory() {
 		try {
 			commandFileName = getCommandFilename();
 			File commandFile = new File(commandFileName);
 
 			// if the file exists, read its contents
 			if (commandFile.exists()) {
 				BufferedReader in = new BufferedReader(new FileReader(commandFile));
 				String str = "";
 				while ((str = in.readLine()) != null) {
 					if (!(str.compareTo("") == 0)) {
 						cmdHistory.add(str);
 					}
 				}
 				in.close();
 
 				// if we have read in more than 500 commands, then
 				// reduce file
 				// and array size down to 500. This is done here as
 				// during
 				// running
 				// we want to write to file every command as quickly as
 				// possible
 				int numberToRemove = cmdHistory.size() - maxCommandsToSave;
 				if (numberToRemove > 0) {
 					for (int i = 0; i < numberToRemove; i++) {
 						cmdHistory.removeElementAt(0);
 					}
 
 					// then rebuild file
 					BufferedWriter out = new BufferedWriter(new FileWriter(commandFile));
 
 					for (int i = 0; i < cmdHistory.size(); i++) {
 						out.write(cmdHistory.get(i) + "\n");
 					}
 					out.close();
 				}
 				this.cmdHistory_index = cmdHistory.size();
 			}
 
 			// else make a new file
 			else {
 				commandFile.createNewFile();
 			}
 		} catch (Exception e) {
 			logger.warn("JythonTerminal: error reading Jython terminal history from file " + commandFileName
 					+ " during configure");
 			commandFileName = null;
 		}
 
 	}
 
 	private void handleTxtInputKeyEvent(KeyEvent e) {
 		// when up or down arrows pressed, scroll through vector of commands
 		// down arrow
 		if (e.keyCode == SWT.ARROW_DOWN) {
 			runFromHistory = false;
 			if (cmdHistory_index < cmdHistory.size() - 1) {
 				cmdHistory_index++;
 				txtInput.setText(cmdHistory.get(cmdHistory_index));
 				moveCaretToEnd();
 			}
 			// if at end of array then dont move index pointer but add a
 			// blank
 			// string
 			else if (cmdHistory_index == cmdHistory.size() - 1) {
 				cmdHistory_index++;
 				txtInput.setText("");
 			}
 		}
 		// up arrow
 		else if (e.keyCode == SWT.ARROW_UP) {
 			if (runFromHistory) {
 				runFromHistory = false;
 			} else if (cmdHistory_index > 0) {
 				cmdHistory_index--;
 			}
 			if (cmdHistory.size() != 0) {
 				txtInput.setText(cmdHistory.get(cmdHistory_index));
 				moveCaretToEnd();
 			}
 		}
 		// Ctrl-U clears the text box
 		else if (e.stateMask == SWT.CTRL && e.keyCode == 'u') {
 			txtInput.setText("");
 		}
 	}
 
 	private void txtInput_ActionPerformed() {
 		// first intercept to see if there's any command which this panel is
 		// interested in rather than passing to the interpreter.
 		String inputText = txtInput.getText();
 		String[] parts = inputText.split(" ");
 		if (parts.length < 1) {
 			return;
 		}
 		// if its a watch
 		if (parts[0].toLowerCase().compareTo("watch") == 0) {
 			// print out what was typed
 			appendOutput(this.txtPrompt.getText() + inputText + "\n");
 			
 			try {
 				DashboardView dashboard = (DashboardView)PlatformUI.
 					getWorkbench().getActiveWorkbenchWindow().getActivePage().
 					showView(DashboardView.ID);
 				if (parts.length > 1) {
 					for (int i = 1; i < parts.length; ++i) {
 						dashboard.addServerObject(new SimpleScannableObject(parts[i]));
 					}
 				}
 				addCommandToHistory(inputText);
 			} catch (PartInitException e) {
 				logger.error("Failed to get DashboardView", e);
 			}
 			txtInput.setText("");
 		}
 		else if ((helpHandler != null) && (parts[0].toLowerCase().compareTo("help") == 0)) {
 			boolean handled = false;
 			StringBuffer buf = new StringBuffer();
 			try {
 				handled = helpHandler.handle(inputText, buf);
 			} catch (Exception e) {
 				logger.error("Error handling " + inputText, e);
 			}
 			if( handled){
 				appendOutput(this.txtPrompt.getText() + inputText + "\n");
 				if( buf.length()>0){
 					appendOutput(buf.toString() + "\n");
 				}
 				addCommandToHistory(inputText);
 				txtInput.setText("");
 			} else {
 				uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
 			}
 		}
 		// if its the history command
 		else if (parts[0].toLowerCase().compareTo("history") == 0) {
 			// print out what was typed
 			appendOutput(this.txtPrompt.getText() + parts[0] + "\n");
 
 			// print out the last 100 commands
 			int i = 0;
 			i = cmdHistory.size() > 100 ? cmdHistory.size() - 100 : 0;
 
 			for (; i < cmdHistory.size(); i++) {
 				appendOutput(i + "\t" + cmdHistory.get(i) + "\n");
 			}
 			txtInput.setText("");
 
 			addCommandToHistory("history");
 		}
 		// repeat old commands
 		else if (parts[0].startsWith("!")) {
 			String stringToMatch = inputText.substring(1);
 
 			// if stringToMatch is a number, then use that command
 			if (stringIsAnInteger(stringToMatch)) {
 				txtInput.setText(cmdHistory.get(Integer.parseInt(stringToMatch)));
 				uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
 				return;
 			}
 			// else search backwards through the history to find a match
 			int i = cmdHistory.size() - 1;
 			boolean foundOne = false;
 			for (; i >= 0; i--) {
 				String oldCommand = cmdHistory.get(i);
 
 				if (oldCommand.length() >= stringToMatch.length()) {
 					String oldCmd = cmdHistory.get(i).substring(0, stringToMatch.length());
 					if (oldCmd.compareTo(stringToMatch) == 0) {
 						txtInput.setText(cmdHistory.get(i));
 						i = 0;
 						foundOne = true;
 					}
 				}
 			}
 			if (foundOne) {
 				uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
 			} else {
 				appendOutput("" + "\n");
 				txtInput.setText("");
 			}
 
 		}
 		// record output to a text file
 		else if (parts[0].toLowerCase().compareTo("record") == 0) {
 			if (parts[1].toLowerCase().compareTo("on") == 0) {
 				startNewOutputFile();
 				appendOutput(this.txtPrompt.getText() + inputText + "\n");
 				addCommandToHistory(inputText);
 			}
 
 			else if (parts[1].toLowerCase().compareTo("off") == 0) {
 				// print out what was typed
 				appendOutput(this.txtPrompt.getText() + inputText + "\n");
 				addCommandToHistory(inputText);
 				closeOutputFile();
 			}
 			txtInput.setText("");
 		}
 		// everything else, pass to the Command Server in a separate thread to
 		// stop the GUI freezing.
 		else {
 			uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
 		}
 	}
 
 	private void closeOutputFile() {
 		try {
 			logger.info("Stopped recording terminal output");
 			printOutput = false;
 			outputFile.close();
 			outputFile = null;
 		} catch (IOException e) {
 			outputFile = null;
 		}
 
 	}
 
 	private void startNewOutputFile() {
 		String filename = determineOutputFileName();
 		// open the file
 		try {
 			outputFile = new FileWriter(new File(filename));
 			printOutput = true;
 			logger.info("Recording terminal output to: " + filename);
 		} catch (IOException e) {
 			printOutput = false;
 			logger.warn("JythonTerminal could not create the output file: " + filename);
 		}
 	}
 
 	private String determineOutputFileName() {
 		String terminalOutputDirName = getTerminalOutputDirName();
 
 		return findNextFileInSequence(terminalOutputDirName, "terminal_output", ".txt");
 	}
 
 	String getTerminalOutputDirName() {
 		// get the terminal output directory from java properties. If no property defined, use the user script directory
 		String terminalOutputDirName;
 		if (LocalProperties.get("gda.jython.terminalOutputDir") != null) {
 			terminalOutputDirName = PropertyUtils.getExistingDirFromLocalProperties("gda.jython.terminalOutputDir");
 		} else { // get the user script directory from jython context
 			terminalOutputDirName = getJythonContext().getDefaultScriptProjectFolder();
 		}
 
 		if (!(terminalOutputDirName.endsWith("\\") || terminalOutputDirName.endsWith("/"))) {
 			terminalOutputDirName += System.getProperty("file.separator");
 		}
 		return terminalOutputDirName;
 	}
 
 	/**
 	 * Given a file name pattern and a folder, return a file name in the folder that is unique and related to any
 	 * existing file names via an incremental index. So if you ask for the prefix "foo", suffix ".txt", and nothing
 	 * already exists, you would get the name "foo_1.txt". Then "foo_2.txt", and so on.
 	 * 
 	 * @note There's a clear time-of-checking-time-of-use vulnerability. An attacker can arrange the source folder such
 	 *       that this code returns a name of the attacker's choosing, which can then be used to perform truncation
 	 *       attacks. I haven't (yet) addressed that.
 	 * @param terminalOutputDirName
 	 *            The folder where the file should be located.
 	 * @param fileNamePrefix
 	 *            The start of the target file's name.
 	 * @param fileNameSuffix
 	 *            The end of the target file's name.
 	 * @return A path to a file that didn't exist when this method was called.
 	 */
 	String findNextFileInSequence(String terminalOutputDirName, final String fileNamePrefix, final String fileNameSuffix) {
 		// filter to use when looking in the user script directory
 		FilenameFilter filter = new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.startsWith(fileNamePrefix) && name.endsWith(fileNameSuffix);
 			}
 		};
 
 		// look through this directory for existing files
 		File userScriptsDirectory = new File(terminalOutputDirName);
 		String[] files = userScriptsDirectory.list(filter);
 		int maxValue = 0;
 
 		// determine the highest existing file of the format:
 		// terminal_output_XX.txt
 		for (String fileName : files) {
 			String whatsLeft = fileName.substring(15);
 			whatsLeft = whatsLeft.substring(0, whatsLeft.lastIndexOf("."));
 
 			if (whatsLeft.length() > 0) {
 				int value = Integer.parseInt(whatsLeft.substring(1));
 
 				if (value > maxValue) {
 					maxValue = value;
 				}
 			}
 		}
 
 		// determine the file name to use
 		String filename;
 		if (maxValue == 0) {
 			filename = terminalOutputDirName + File.separator + fileNamePrefix + "_1.txt";
 		} else {
 			filename = terminalOutputDirName + File.separator + fileNamePrefix + "_" + (maxValue + 1) + ".txt";
 		}
 		return filename;
 	}
 
 	private void addCommandToHistory(String newCommand) {
 		// add command to the history
 		cmdHistory.add(newCommand);
 
 		// also save command to a file
 		try {
 			if (getCommandFilename() != null) {
 				BufferedWriter out = new BufferedWriter(new FileWriter(getCommandFilename(), true));
 				out.write(newCommand + "\n");
 				out.close();
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	String getCommandFilename() {
 		if (commandFileName == null) {
 			commandFileName = LocalProperties.get("gda.jythonTerminal.commandHistory.path", getJythonContext()
 					.getDefaultScriptProjectFolder());
 			if (!(commandFileName.endsWith("\\") || commandFileName.endsWith("/"))) {
 				commandFileName += System.getProperty("file.separator");
 			}
 			commandFileName += ".cmdHistory.txt";
 		}
 		return commandFileName;
 	}
 
 	private boolean stringIsAnInteger(String stringToCheck) {
 		// a bit of a hack, but works!
 		try {
 			Integer.parseInt(stringToCheck);
 			return true; // Did not throw, must be a number
 		} catch (NumberFormatException err) {
 			return false; // Threw, So is not a number
 		}
 	}
 
 	StringBuffer outputBuffer = new StringBuffer("");
 
 	private IJythonContext mockJythonContext;
 
 	private synchronized void appendOutput(String text) {
 		// if output being saved to a file (record command)
 		if (printOutput && outputFile != null) {
 			try {
 				outputFile.append(text);
 				outputFile.flush();
 			} catch (IOException e) {
 				closeOutputFile();
 				logger.warn("Error writing terminal output to file. Closing the file. Error was: " + e.getMessage());
 			}
 		}
 		recalculateBuffer(text);
 		outputBufferUpdated = true;
 	}
 
 	private class UpdaterRunner implements Runnable {
 		private SimpleOutputUpdater latestUpdater;
 
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					// test if anything added
 					while (!outputBufferUpdated) {
 						Thread.sleep(50);
 					}
 					outputBufferUpdated = false;
 					//if there is not already a SimpleOutputUpdater on the UIThread queue then add one 
 					if( latestUpdater ==null || !latestUpdater.inqueue){
 						latestUpdater = new SimpleOutputUpdater(JythonTerminalView.this);
 						if (!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
 							PlatformUI.getWorkbench().getDisplay().asyncExec(latestUpdater);
 						}
 					}
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					outputBufferUpdated = true;
 				}
 			}
 		}
 	}
 
 	private void recalculateBuffer(String text) {
 		text = text.replaceAll("\\r+\\n", "\n");
 
 		// if a repeat of the command prompt
 		if (text.startsWith(NORMALPROMPT)) {
 			// make sure that the print out starts on a new line
 			if (outputBuffer.length() > 0 && outputBuffer.charAt(outputBuffer.length() - 1) != '\n') {
 				outputBuffer.append("\r\n");
 				// update where new print out should start
 				caretPosition = outputBuffer.length();
 			}
 			// print
 			addToOutputBuffer(text);
 			// update where new print out should start
 			caretPosition = outputBuffer.length();
 			// scrollToBottom = true;
 		}
 		// if just regular output simply append
 		else if (!text.contains("\r") && !text.startsWith(RAWINPUTPROMPT)) {
 			// If text field has grown too long, trim off 10% from the beginning. Note: This is only performed for
 			// this "regular output" case because changing the text field length for the other cases messes up the
 			// caretPosition value.
 			int currentLength = outputBuffer.length();
 			if (currentLength > LocalProperties.getInt("gda.jython.jythonTerminal.textFieldCharactersCap", 100000)) {
 				outputBuffer.delete(0, currentLength / 10);
 			}
 			// print
 			addToOutputBuffer(text);
 			// update where new print out should start
 			caretPosition = outputBuffer.length();
 			// scrollToBottom = true;
 			// if output starts with '-->' when user requested input mid-script
 		} else if (text.startsWith(RAWINPUTPROMPT)) {
 			// add this output to the end of the previous line
 			caretPosition = outputBuffer.length();
 			// print
 			addToOutputBuffer(text);
 			// update where new print out should start
 			caretPosition = outputBuffer.length();
 			// scrollToBottom = true;
 		}
 		// Otherwise must contain a \r.
 		// This should be handled properly so the caret is returned to the start of the last line rather than \r
 		// being treated as a new line marker.
 		else {
 			try {
 				// find out where the \r is
 				int locOfCR = text.indexOf("\r");
 
 				// remove any final \n
 				if (text.endsWith("\n")) {
 					text = text.substring(0, text.length() - 1);
 				}
 
 				// if \r at start of string, move caret to start of previous
 				// line, unless that line started with '>>>'
 				if (locOfCR == 0) {
 					int locofLastEndofLine = outputBuffer.lastIndexOf("\n");
 					caretPosition = locofLastEndofLine + 1;
 					// caretPosition = txtOutput.getOffsetAtLine(txtOutput.getLineCount() - 1);
 				}
 				// else add first part of text and then move the caret of
 				// that line
 				else {
 					String substring = text.substring(0, locOfCR);
 					addToOutputBuffer(substring);
 					int locofLastEndofLine = outputBuffer.lastIndexOf("\n");
 					caretPosition = locofLastEndofLine + 1;
 					// caretPosition = txtOutput.getOffsetAtLine(txtOutput.getLineCount() - 1);
 				}
 
 				// if anything after the /r in the text, append that
 				if (text.length() > locOfCR + 1) {
 					String stringToAppend = text.substring(locOfCR + 1);
 					// print
 					addToOutputBuffer(stringToAppend);
 					// update where new print out should start
 					caretPosition += stringToAppend.length();
 				}
 
 			}
 			// any error, simply output everything and treat \r as a \n
 			catch (Exception e) {
 				addToOutputBuffer(text);
 				// txtOutput.setText(txtOutput.getText() + newOutput);
 				caretPosition = outputBuffer.length();
 			}
 		}
 	}
 
 	/**
 	 * Overwrites the text in the JTextArea with the supplied string starting at the location defined by caretPosition.
 	 * 
 	 * @param theString
 	 */
 	private void addToOutputBuffer(String theString) {
 
 		// find location of end of last line
 		int finalCharacter = outputBuffer.length();
 
 		// if caret at the end of the last line
 		if (caretPosition >= finalCharacter) {
 			outputBuffer.append(theString);
 		}
 		// else if the output would only overwrite existing text
 		else if (theString.length() + caretPosition < finalCharacter) {
 			outputBuffer.replace(caretPosition, theString.length(), theString);
 		}
 		// else a mixture of overwriting and appending
 		else {
 			int firstPartLength = finalCharacter - caretPosition;
 			String firstPart = theString.substring(0, firstPartLength);
 			outputBuffer.replace(caretPosition, firstPartLength, firstPart);
 			if (firstPartLength < theString.length()) {
 				String lastPart = theString.substring(firstPartLength);
 				outputBuffer.append(lastPart);
 			}
 		}
 	}
 
 	private void moveCaretToEnd() {
 		txtInput.setSelection(txtInput.getCharCount(), txtInput.getCharCount());
 	}
 
 
 	private IJythonContext getJythonContext() {
 		return (this.mockJythonContext != null) ? this.mockJythonContext : jsf;
 	}
 
 	void setJythonContextForTesting(IJythonContext mockJythonContext) {
 		logger.warn("Overriding JythonServerFacade with test object");
 		this.mockJythonContext = mockJythonContext;
 	}
 
 	public static Boolean getScrollLock() {
 		return scrollLock;
 	}
 
 	public static void setScrollLock(Boolean scrollLock) {
 		JythonTerminalView.scrollLock = scrollLock;
 	}
 
 	public static Boolean getMoveToTopOnUpdate() {
 		return moveToTopOnUpdate;
 	}
 
 	public static void setMoveToTopOnUpdate(Boolean moveToTopOnUpdate) {
 		JythonTerminalView.moveToTopOnUpdate = moveToTopOnUpdate;
 	}
 
 	public void clearConsole() {
 		setOutputText("");
 	}
 	
 	@Override
 	public void dispose() {
 		if(autoCompleter!=null){
 			autoCompleter.dispose();
 		}
 			
 		super.dispose();
 	}
 
 }
 /**
  * Used by appendOutpupt
  */
 class SimpleOutputUpdater implements Runnable {
 
 	public boolean inqueue;
 	private final JythonTerminalView jtv;
 
 	SimpleOutputUpdater(JythonTerminalView jtv) {
 		this.jtv = jtv;
 		inqueue=true;
 	}
 
 	@Override
 	public void run() {
 		inqueue = false; 
 		// On Windows, newlines are \r\n terminated, when you call setText() or append()
 		// \n is replaced with \r\n, so this sequence unexpectedly may return false:
 		// txtOutput.setText(newOutput);
 		// txtOutput.getText().equals(newOutput);
 		// The effect of this is we need to keep a local copy of the last
 		// string to be set, and we need to calculate the selection index on
 		// what the text actually is to know what the selection index should be
 
 		jtv.outputLock.lock();
 		try {
 			String newOutput = jtv.outputBuffer.toString().trim();
 			//decide whether to call outputDoc.append or set
 			if (newOutput .startsWith(jtv.txtOutputLast)) {
 				String append = newOutput.substring(jtv.txtOutputLast.length());
 				jtv.outputDoc.append(append);
 			} else {
 				jtv.outputDoc.set(newOutput);
 			}
 			jtv.txtOutputLast = newOutput;
 		} finally {
 			jtv.outputLock.unlock();
 		}
 
 		if (!JythonTerminalView.getScrollLock()) {
 			//we need to change what is shown
 			String realOutput = jtv.outputDoc.get();
 			if (jtv.outputTextViewer.getTextWidget() != null && realOutput.contains("\n")) {
 				int index = realOutput.lastIndexOf("\n") + 1;
 				jtv.outputTextViewer.getTextWidget().setSelection(index);
 				jtv.outputTextViewer.getTextWidget().showSelection();
 			}
 		}
 
 		if (JythonTerminalView.getMoveToTopOnUpdate()) {
 			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 			IWorkbenchPage page = window.getActivePage();
 			if (jtv.getSite().getPage().equals(page)) {
 				page.bringToTop(jtv);
 			}
 		}
 	}
 	
 	
 }
