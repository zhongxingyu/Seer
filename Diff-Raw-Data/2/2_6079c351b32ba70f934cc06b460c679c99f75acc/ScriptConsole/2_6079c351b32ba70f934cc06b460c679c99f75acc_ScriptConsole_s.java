 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.console.ui;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchesListener2;
 import org.eclipse.debug.core.IStreamListener;
 import org.eclipse.debug.core.model.IFlushableStreamMonitor;
 import org.eclipse.debug.core.model.IStreamMonitor;
 import org.eclipse.debug.core.model.IStreamsProxy;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.console.IScriptConsoleInterpreter;
 import org.eclipse.dltk.console.IScriptExecResult;
 import org.eclipse.dltk.console.IScriptInterpreter;
 import org.eclipse.dltk.console.ScriptConsoleHistory;
 import org.eclipse.dltk.console.ScriptConsolePrompt;
 import org.eclipse.dltk.console.ScriptExecResult;
 import org.eclipse.dltk.console.ui.internal.ICommandHandler;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleInput;
 import org.eclipse.dltk.console.ui.internal.ScriptConsolePage;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleSession;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer;
 import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer.ConsoleDocumentListener;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.launching.process.IScriptProcess;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.console.IConsoleDocumentPartitioner;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.TextConsole;
 import org.eclipse.ui.part.IPageBookViewPage;
 
 public class ScriptConsole extends TextConsole implements ICommandHandler {
 	private ILaunch launch = null;
 	private ILaunchesListener2 listener = null;
 
 	private class ScriptConsoleLaunchListener implements ILaunchesListener2 {
 		public void launchesTerminated(ILaunch[] launches) {
 			if (terminated) {
 				return;
 			}
 			for (int i = 0; i < launches.length; i++) {
 				if (launches[i].equals(launch)) {
 					final ScriptConsoleViewer consoleViewer = (ScriptConsoleViewer) page
 							.getViewer();
 					page.getControl().getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							if (consoleViewer != null) {
 								consoleViewer.disableProcessing();
 								// TODO use different color
 								updateText(
 										consoleViewer,
 										Messages.ScriptConsole_processTerminated);
 								consoleViewer.setEditable(false);
 							}
 						}
 					});
 				}
 			}
 		}
 
 		public void launchesAdded(ILaunch[] launches) {
 		}
 
 		public void launchesChanged(ILaunch[] launches) {
 		}
 
 		public void launchesRemoved(ILaunch[] launches) {
 		}
 	};
 
 	private final class InitialStreamReader implements Runnable {
 		private final IScriptInterpreter interpreter;
 
 		private InitialStreamReader(IScriptInterpreter interpreter) {
 			this.interpreter = interpreter;
 		}
 
 		public void run() {
 			// We need to be sure what page is already created
 			while (page == null || (page != null && page.getViewer() == null)) {
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 			final ScriptConsoleViewer viewer = (ScriptConsoleViewer) page
 					.getViewer();
 			InputStream stream = interpreter.getInitialOutputStream();
 			if (stream == null) {
 				return;
 			}
 			final BufferedReader reader = new BufferedReader(
 					new InputStreamReader(stream));
 			Thread readerThread = new Thread() {
 				public void run() {
 					while (!terminated) {
 						String readLine;
 						try {
 							readLine = reader.readLine();
 							if (readLine != null) {
 								updateText(viewer, readLine);
 							} else {
 								break;
 							}
 						} catch (IOException e) {
 							if (DLTKCore.DEBUG) {
 								e.printStackTrace();
 							}
 							break;
 						}
 					}
 					enableEdit(viewer);
 				}
 
 			};
 			readerThread.start();
 		}
 
 	}
 
 	protected void enableEdit(final ScriptConsoleViewer viewer) {
 		Control control = viewer.getControl();
 		if (control == null) {
 			return;
 		}
 		control.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				viewer.setEditable(true);
 			}
 		});
 	}
 
 	private void updateText(final ScriptConsoleViewer viewer, final String text) {
 		Control control = viewer.getControl();
 		if (control == null) {
 			return;
 		}
 		control.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				IDocument document = getDocument();
 				final String delim = TextUtilities
 						.getDefaultLineDelimiter(document);
 				getDocumentListener().write(text + delim, false);
 			}
 		});
 	}
 
 	private ScriptConsolePage page;
 
 	private ScriptConsolePartitioner partitioner;
 
 	private IContentAssistProcessor processor;
 
 	private ITextHover hover;
 
 	private IScriptInterpreter interpreter;
 
 	private ScriptConsoleSession session;
 
 	private ListenerList consoleListeners;
 
 	private ScriptConsolePrompt prompt;
 
 	private ScriptConsoleHistory history;
 
 	private boolean terminated = false;
 
 	protected IConsoleDocumentPartitioner getPartitioner() {
 		return partitioner;
 	}
 
 	public ScriptConsolePage getPage() {
 		return page;
 	}
 
 	public ScriptConsole(String consoleName, String consoleType,
 			ImageDescriptor image) {
 		super(consoleName, consoleType, image, true);
 
 		this.consoleListeners = new ListenerList(ListenerList.IDENTITY);
 		this.prompt = new ScriptConsolePrompt("=>", "->"); //$NON-NLS-1$ //$NON-NLS-2$
 		this.history = new ScriptConsoleHistory();
 
 		this.session = new ScriptConsoleSession();
 		addListener(this.session);
 
 		partitioner = new ScriptConsolePartitioner();
 		getDocument().setDocumentPartitioner(partitioner);
 		partitioner.connect(getDocument());
 	}
 
 	public ScriptConsole(String consoleName, String consoleType) {
 		this(consoleName, consoleType, null);
 	}
 
 	public IScriptConsoleSession getSession() {
 		return session;
 	}
 
 	public void addListener(IScriptConsoleListener listener) {
 		consoleListeners.add(listener);
 	}
 
 	public void removeListener(IScriptConsoleListener listener) {
 		consoleListeners.remove(listener);
 	}
 
 	protected void setContentAssistProcessor(IContentAssistProcessor processor) {
 		this.processor = processor;
 	}
 
 	protected void setInterpreter(final IScriptInterpreter interpreter) {
 		this.interpreter = interpreter;
 		interpreter.addInitialListenerOperation(new InitialStreamReader(
 				interpreter));
 	}
 
 	public void setPrompt(ScriptConsolePrompt prompt) {
 		this.prompt = prompt;
 	}
 
 	public ScriptConsolePrompt getPrompt() {
 		return prompt;
 	}
 
 	public ScriptConsoleHistory getHistory() {
 		return history;
 	}
 
 	protected void setTextHover(ITextHover hover) {
 		this.hover = hover;
 	}
 
 	private ConsoleDocumentListener documentListener;
 
 	public ConsoleDocumentListener getDocumentListener() {
 		if (documentListener == null) {
 			documentListener = new ConsoleDocumentListener(this, this
 					.getPrompt(), this.getHistory());
 			documentListener.setDocument(getDocument());
 
 		}
 
 		return documentListener;
 	}
 
 	public IPageBookViewPage createPage(IConsoleView view) {
 		SourceViewerConfiguration cfg = new ScriptConsoleSourceViewerConfiguration(
 				processor, hover);
 		page = createPage(view, cfg);
 		return page;
 	}
 
 	protected ScriptConsolePage createPage(IConsoleView view,
 			SourceViewerConfiguration cfg) {
 		return new ScriptConsolePage(this, view, cfg);
 	}
 
 	public void clearConsole() {
 		page.clearConsolePage();
 	}
 
 	public IScriptConsoleInput getInput() {
 		return new ScriptConsoleInput(page);
 	}
 
 	public int getState() {
 		return interpreter.getState();
 	}
 
 	public IScriptExecResult handleCommand(String userInput) throws IOException {
		if (this.interpreter == null && this.interpreter.isValid()) {
 			return new ScriptExecResult(Util.EMPTY_STRING);
 		}
 		Object[] listeners = consoleListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			((IScriptConsoleListener) listeners[i]).userRequest(userInput);
 		}
 
 		IScriptExecResult output = interpreter.exec(userInput);
 
 		if (interpreter.getState() == IScriptConsoleInterpreter.WAIT_NEW_COMMAND) {
 			prompt.setMode(true);
 		} else {
 			prompt.setMode(false);
 		}
 
 		for (int i = 0; i < listeners.length; i++) {
 			((IScriptConsoleListener) listeners[i]).interpreterResponse(output);
 		}
 
 		return output;
 	}
 
 	/**
 	 * Executes the specified code and displays the results
 	 * 
 	 * @param command
 	 */
 	public void executeCommand(String command) {
 		getDocumentListener().executeCommand(command);
 	}
 
 	public void terminate() {
 		terminated = true;
 		try {
 			interpreter.close();
 		} catch (IOException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void dispose() {
 		partitioner.clearRanges();
 
 		terminate();
 		if (listener != null) {
 			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(
 					listener);
 			listener = null;
 		}
 
 		super.dispose();
 	}
 
 	public void setLaunch(ILaunch launch) {
 		this.launch = launch;
 		if (this.listener == null) {
 			this.listener = new ScriptConsoleLaunchListener();
 			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(
 					listener);
 		}
 	}
 
 	/**
 	 * @return the launch
 	 */
 	public ILaunch getLaunch() {
 		return launch;
 	}
 
 	private Set connectedProcesses;
 
 	/**
 	 * @param process
 	 */
 	public synchronized void connect(IScriptProcess process) {
 		if (connectedProcesses == null) {
 			connectedProcesses = new HashSet();
 		}
 		if (connectedProcesses.add(process)) {
 			final IStreamsProxy proxy = process.getScriptStreamsProxy();
 			if (proxy == null) {
 				return;
 			}
 			connect(proxy);
 		}
 	}
 
 	public void connect(final IStreamsProxy proxy) {
 		IStreamMonitor streamMonitor = proxy.getErrorStreamMonitor();
 		if (streamMonitor != null) {
 			connect(streamMonitor, IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
 		}
 		streamMonitor = proxy.getOutputStreamMonitor();
 		if (streamMonitor != null) {
 			connect(streamMonitor, IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
 		}
 	}
 
 	private List fStreamListeners = new ArrayList();
 
 	/**
 	 * @param streamMonitor
 	 * @param idStandardErrorStream
 	 */
 	private void connect(IStreamMonitor streamMonitor, String streamIdentifier) {
 		synchronized (streamMonitor) {
 			StreamListener listener = new StreamListener(streamIdentifier,
 					streamMonitor);
 			fStreamListeners.add(listener);
 		}
 	}
 
 	/**
 	 * This class listens to a specified IO stream
 	 */
 	private class StreamListener implements IStreamListener {
 		private IStreamMonitor fStreamMonitor;
 		private String fStreamId;
 		private boolean fFlushed = false;
 		private boolean fListenerRemoved = false;
 
 		public StreamListener(String streamIdentifier, IStreamMonitor monitor) {
 			this.fStreamId = streamIdentifier;
 			this.fStreamMonitor = monitor;
 			fStreamMonitor.addListener(this);
 			// fix to bug 121454. Ensure that output to fast processes is
 			// processed.
 			streamAppended(null, monitor);
 		}
 
 		public void streamAppended(String text, IStreamMonitor monitor) {
 			if (fFlushed) {
 				getDocumentListener().write(
 						text,
 						IDebugUIConstants.ID_STANDARD_ERROR_STREAM
 								.equals(fStreamId));
 			} else {
 				String contents = null;
 				synchronized (fStreamMonitor) {
 					fFlushed = true;
 					contents = fStreamMonitor.getContents();
 					if (fStreamMonitor instanceof IFlushableStreamMonitor) {
 						IFlushableStreamMonitor m = (IFlushableStreamMonitor) fStreamMonitor;
 						m.flushContents();
 						m.setBuffered(false);
 					}
 				}
 				if (contents != null && contents.length() > 0) {
 					getDocumentListener().write(
 							contents,
 							IDebugUIConstants.ID_STANDARD_ERROR_STREAM
 									.equals(fStreamId));
 				}
 			}
 		}
 
 		public IStreamMonitor getStreamMonitor() {
 			return fStreamMonitor;
 		}
 
 		public void closeStream() {
 			if (fStreamMonitor == null) {
 				return;
 			}
 			synchronized (fStreamMonitor) {
 				fStreamMonitor.removeListener(this);
 				if (!fFlushed) {
 					String contents = fStreamMonitor.getContents();
 					streamAppended(contents, fStreamMonitor);
 				}
 				fListenerRemoved = true;
 			}
 		}
 
 		public void dispose() {
 			if (!fListenerRemoved) {
 				closeStream();
 			}
 			fStreamMonitor = null;
 			fStreamId = null;
 		}
 	}
 }
