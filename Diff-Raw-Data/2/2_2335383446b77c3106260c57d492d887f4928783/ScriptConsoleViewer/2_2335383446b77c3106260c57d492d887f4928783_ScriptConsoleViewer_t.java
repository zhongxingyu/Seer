 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.console.ui.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.console.IScriptConsoleInterpreter;
 import org.eclipse.dltk.console.IScriptExecResult;
 import org.eclipse.dltk.console.ScriptConsoleHistory;
 import org.eclipse.dltk.console.ScriptConsolePrompt;
 import org.eclipse.dltk.console.ui.AnsiColorHelper;
 import org.eclipse.dltk.console.ui.AnsiColorHelper.IAnsiColorHandler;
 import org.eclipse.dltk.console.ui.IScriptConsoleViewer;
 import org.eclipse.dltk.console.ui.ScriptConsole;
 import org.eclipse.dltk.console.ui.ScriptConsolePartitioner;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.hyperlink.HyperlinkManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.TextConsoleViewer;
 
 public class ScriptConsoleViewer extends TextConsoleViewer implements
 		IScriptConsoleViewer {
 	public static class ConsoleDocumentListener implements IDocumentListener {
 
 		private boolean bEnabled = true;
 		private ICommandHandler handler;
 		private boolean handleSynchronously;
 
 		private ScriptConsolePrompt prompt;
 
 		private ScriptConsoleHistory history;
 
 		private int inviteStart = 0;
 		private int inviteEnd = 0;
 
 		private IDocument doc;
 
 		private AnsiColorHelper ansiHelper = new AnsiColorHelper();
 
 		private List viewerList = new ArrayList();
 
 		private void addViewer(ScriptConsoleViewer viewer) {
 			viewerList.add(viewer);
 		}
 
 		private void removeViewer(ScriptConsoleViewer viewer) {
 			viewerList.remove(viewer);
 		}
 
 		protected void connectListener() {
 			doc.addDocumentListener(this);
 		}
 
 		protected void disconnectListener() {
 			doc.removeDocumentListener(this);
 		}
 
 		public void clear() {
 			try {
 				disconnectListener();
 				doc.set(""); //$NON-NLS-1$
 				ScriptConsoleViewer viewer;
 				for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 					viewer = (ScriptConsoleViewer) iter.next();
 					IDocumentPartitioner partitioner = viewer.getDocument()
 							.getDocumentPartitioner();
 					if (partitioner instanceof ScriptConsolePartitioner) {
 						ScriptConsolePartitioner scriptConsolePartitioner = (ScriptConsolePartitioner) partitioner;
 						scriptConsolePartitioner.clearRanges();
 					}
 				}
 				appendInvitation();
 				for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 					((ScriptConsoleViewer) iter.next()).setCaretPosition(doc
 							.getLength());
 				}
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			} finally {
 				connectListener();
 			}
 		}
 
 		public ConsoleDocumentListener(ICommandHandler handler,
 				ScriptConsolePrompt prompt, ScriptConsoleHistory history) {
 			this.prompt = prompt;
 			this.handler = handler;
 			this.history = history;
 			this.doc = null;
 		}
 
 		public void setDocument(IDocument doc) {
 			if (this.doc != null) {
 				disconnectListener();
 			}
 
 			this.doc = doc;
 
 			if (this.doc != null) {
 				connectListener();
 			}
 		}
 
 		public void documentAboutToBeChanged(DocumentEvent event) {
 
 		}
 
 		protected void handleCommandLine(final String command)
 				throws BadLocationException, IOException {
 			if (handleSynchronously) {
 				IScriptExecResult result = handler.handleCommand(command);
 				if (((ScriptConsole) handler).getState() != IScriptConsoleInterpreter.WAIT_USER_INPUT) {
 					processResult(result);
 				}
 				return;
 			}
 
 			Thread handlerThread = new Thread(
 					Messages.ScriptConsoleViewer_scriptConsoleCommandHandler) {
 
 				public void run() {
 					try {
 						final IScriptExecResult result = handler
 								.handleCommand(command);
 
 						if (((ScriptConsole) handler).getState() != IScriptConsoleInterpreter.WAIT_USER_INPUT) {
 							((ScriptConsole) handler).getPage().getSite()
 									.getShell().getDisplay()
 									.asyncExec(new Runnable() {
 
 										public void run() {
 											processResult(result);
 										}
 
 									});
 						}
 					} catch (IOException ixcn) {
 						ixcn.printStackTrace();
 					}
 				}
 
 			};
 			handlerThread.setDaemon(true);
 			handlerThread.setPriority(Thread.MIN_PRIORITY);
 			handlerThread.start();
 		}
 
 		protected void appendText(int offset, String text)
 				throws BadLocationException {
 			doc.replace(offset, 0, text);
 		}
 
 		protected void processText(int originalOffset, String content,
 				boolean isInput, boolean isError, final boolean shouldReveal,
 				final boolean shouldRedraw) throws BadLocationException {
 			if (originalOffset == -1) {
 				originalOffset = doc.getLength();
 			}
 
 			ansiHelper.processText(originalOffset, content, isInput, isError,
 					new IAnsiColorHandler() {
 
 						public void handleText(int start, String content,
 								boolean isInput, boolean isError)
 								throws BadLocationException {
 							appendText(start, content);
 							addToPartitioner(start, content, isInput, isError);
 						}
 
 						public void processingComplete(int start, int length) {
 							for (Iterator iter = viewerList.iterator(); iter
 									.hasNext();) {
 								final ScriptConsoleViewer viewer = (ScriptConsoleViewer) iter
 										.next();
 								if (shouldReveal == true) {
 									viewer.setCaretPosition(doc.getLength());
 									viewer.revealEndOfDocument();
 								}
 
 								if (shouldRedraw == true) {
 									if (viewer.getTextWidget() != null) {
 										viewer.getTextWidget().redrawRange(
 												start, length, true);
 									}
 								}
 							}
 						}
 
 					});
 		}
 
 		protected void processResult(final IScriptExecResult result) {
 			disconnectListener();
 			try {
 				if (result != null) {
 					final String output = result.getOutput();
 					if (output != null && output.length() != 0) {
 						ansiHelper.reset();
 						processText(-1, output, false, result.isError(), false,
 								true);
 					}
 				}
 				appendInvitation();
 			} catch (BadLocationException bxcn) {
 				bxcn.printStackTrace();
 			} finally {
 				connectListener();
 			}
 		}
 
 		private void addToPartitioner(ScriptConsoleViewer viewer,
 				StyleRange style) {
 			IDocumentPartitioner partitioner = viewer.getDocument()
 					.getDocumentPartitioner();
 			if (partitioner instanceof ScriptConsolePartitioner) {
 				ScriptConsolePartitioner scriptConsolePartitioner = (ScriptConsolePartitioner) partitioner;
 				scriptConsolePartitioner.addRange(style);
 			}
 		}
 
 		protected void addToPartitioner(int start, String content,
 				boolean isInput, boolean isError) {
 			// ssanders: Content has to be tokenized in order for style and
 			// hyperlinks to display correctly
 			StringTokenizer tokenizer = new StringTokenizer(content,
 					" \t\n\r\f@#=|,()[]{}<>'\"", true); //$NON-NLS-1$
 			String token;
 			int tokenStart = start;
 			ScriptConsoleViewer viewer;
 			while (tokenizer.hasMoreTokens() == true) {
 				token = tokenizer.nextToken();
 
 				for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 					viewer = (ScriptConsoleViewer) iter.next();
 					if (isInput == true) {
 						addToPartitioner(viewer, new StyleRange(tokenStart,
 								token.length(), AnsiColorHelper.COLOR_BLACK,
 								null, SWT.BOLD));
 					} else {
 						addToPartitioner(
 								viewer,
 								ansiHelper.resolveStyleRange(tokenStart,
 										token.length(), isError));
 					}
 				}
 
 				tokenStart += token.length();
 			}
 			for (Iterator iter = viewerList.iterator(); iter.hasNext();) {
 				viewer = (ScriptConsoleViewer) iter.next();
 				viewer.getTextWidget().redraw();
 			}
 
 		}
 
 		protected void processAddition(int offset, String text) {
 			if (!bEnabled) {
 				return;
 			}
 			try {
 				final String delim = TextUtilities.getDefaultLineDelimiter(doc);
 				text = doc.get(offset, doc.getLength() - offset);
 				doc.replace(offset, text.length(), ""); //$NON-NLS-1$
 				text = text.replaceAll("\r\n|\n|\r", delim); //$NON-NLS-1$
 				int start = 0;
 				int index;
 				while ((index = text.indexOf(delim, start)) != -1) {
 					if (index > start) {
 						processText(getCommandLineOffset(),
 								text.substring(start, index), true, false,
 								false, true);
 					}
 					final String commandLine = getCommandLine();
 					processText(-1, delim, true, false, false, true);
 					inviteStart = inviteEnd = doc.getLength();
 					history.add(commandLine);
 					start = index + delim.length();
 					handleCommandLine(commandLine);
 				}
 				if (start < text.length()) {
 					processText(-1, text.substring(start, text.length()), true,
 							false, false, true);
 				}
 			} catch (BadLocationException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		public void documentChanged(final DocumentEvent event) {
 			ansiHelper.disableWhile(new Runnable() {
 
 				public void run() {
 					disconnectListener();
 					try {
 						processAddition(event.getOffset(), event.getText());
 					} finally {
 						connectListener();
 					}
 				}
 
 			});
 		}
 
 		public void appendInvitation() throws BadLocationException {
 			inviteStart = doc.getLength();
 			processText(inviteStart, prompt.toString(), true, false, true, true);
 			inviteEnd = doc.getLength();
 		}
 
 		public void appendDelimeter() throws BadLocationException {
 			processText(-1, TextUtilities.getDefaultLineDelimiter(doc), false,
 					false, false, true);
 		}
 
 		protected int getLastLineLength() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineLength(lastLine);
 		}
 
 		public int getCommandLineOffset() throws BadLocationException {
 			return inviteEnd;
 		}
 
 		public int getCommandLineLength() throws BadLocationException {
 			return doc.getLength() - inviteEnd;
 		}
 
 		public String getCommandLine() throws BadLocationException {
 			return doc.get(getCommandLineOffset(), getCommandLineLength());
 		}
 
 		public void setCommandLine(final String command) {
 			ansiHelper.disableWhile(new Runnable() {
 
 				public void run() {
 					try {
 						doc.replace(getCommandLineOffset(),
 								getCommandLineLength(), command);
 					} catch (BadLocationException bxcn) {
 						bxcn.printStackTrace();
 					}
 				}
 
 			});
 		}
 
 		/**
 		 * @param command
 		 */
 		public void executeCommand(String command) {
 			disconnectListener();
 			try {
 				final int docLen = doc.getLength();
 				if (docLen > inviteEnd) {
 					// clear current command if any
 					doc.replace(inviteEnd, docLen - inviteEnd,
 							Util.EMPTY_STRING);
 					// TODO should we restore the text after this command
 					// execution?
 				}
 				processText(getCommandLineOffset(),
 						command + TextUtilities.getDefaultLineDelimiter(doc),
 						true, false, true, true);
 				inviteStart = inviteEnd = doc.getLength();
 				handleCommandLine(command);
 			} catch (BadLocationException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} finally {
 				connectListener();
 			}
 		}
 
 		/**
 		 * @param text
 		 * @param isError
 		 */
 		public void write(final String text, final boolean isError) {
 			final Display display = PlatformUI.getWorkbench().getDisplay();
 			if (display != null && !display.isDisposed())
 				display.asyncExec(new Runnable() {
 					public void run() {
 						disconnectListener();
 						try {
 							processText(inviteStart, text, false, isError,
 									true, true);
 							inviteStart += text.length();
 							inviteEnd += text.length();
 						} catch (BadLocationException bxcn) {
 							if (DLTKCore.DEBUG) {
 								bxcn.printStackTrace();
 							}
 						} finally {
 							connectListener();
 						}
 					}
 				});
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public class ScriptConsoleStyledText extends StyledText {
 
 		public ScriptConsoleStyledText(Composite parent, int style) {
 			super(parent, (style | SWT.WRAP));
 		}
 
 		public void invokeAction(int action) {
 			if (isEditable() && isCaretOnLastLine()) {
 				switch (action) {
 				case ST.LINE_UP:
 					updateSelectedLine();
 					if (history.prev()) {
 						console.getDocumentListener().setCommandLine(
 								history.get());
 						setCaretOffset(getDocument().getLength());
 					} else {
 						beep();
 					}
 					return;
 
 				case ST.LINE_DOWN:
 					updateSelectedLine();
 					if (history.next()) {
 						console.getDocumentListener().setCommandLine(
 								history.get());
 						setCaretOffset(getDocument().getLength());
 					} else {
 						beep();
 					}
 					return;
 
 				case ST.DELETE_PREVIOUS:
 					if (getCaretOffset() <= getCommandLineOffset()
 							&& getSelectionCount() == 0) {
 						return;
 					}
 					break;
 
 				case ST.DELETE_NEXT:
 					if (getCaretOffset() < getCommandLineOffset()) {
 						return;
 					}
 					break;
 
 				case ST.DELETE_WORD_PREVIOUS:
 					return;
 
 				case ST.SELECT_LINE_START:
 					if (isCaretOnLastLine()) {
 						final int prevCaret = getCaretOffset();
 						final Point prevSelection = getSelection();
 						final int caret = getCommandLineOffset();
 						if (prevCaret == prevSelection.x) {
 							setSelection(prevSelection.y, caret);
 						} else if (prevCaret == prevSelection.y) {
 							setSelection(prevSelection.x, caret);
 						} else {
 							setCaretOffset(caret);
 						}
						Point selectedRange = getSelectedRange();
						selectionChanged(selectedRange.x, selectedRange.y);
 						return;
 					}
 					break;
 				case ST.LINE_START:
 					if (isCaretOnLastLine()) {
 						setCaretOffset(getCommandLineOffset());
 						return;
 					}
 					break;
 				case ST.COLUMN_PREVIOUS:
 				case ST.SELECT_COLUMN_PREVIOUS:
 					if (isCaretOnLastLine()
 							&& getCaretOffset() == getCommandLineOffset()) {
 						return;
 					}
 				}
 
 				super.invokeAction(action);
 
 				if (isCaretOnLastLine()
 						&& getCaretOffset() <= getCommandLineOffset()) {
 					setCaretOffset(getCommandLineOffset());
 				}
 			} else {
 
 				super.invokeAction(action);
 			}
 		}
 
 		private void updateSelectedLine() {
 			try {
 				history.updateSelectedLine(console.getDocumentListener()
 						.getCommandLine());
 			} catch (BadLocationException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		private void beep() {
 			getDisplay().beep();
 		}
 
 		public void paste() {
 			if (isCaretOnLastLine()) {
 				console.getDocumentListener().ansiHelper
 						.disableWhile(new Runnable() {
 
 							public void run() {
 								checkWidget();
 								Clipboard clipboard = new Clipboard(
 										getDisplay());
 								TextTransfer plainTextTransfer = TextTransfer
 										.getInstance();
 								String text = (String) clipboard.getContents(
 										plainTextTransfer, DND.CLIPBOARD);
 								clipboard.dispose();
 								paste(text);
 							}
 
 						});
 			}
 		}
 
 		/**
 		 * @param text
 		 */
 		private void paste(String text) {
 			if (text != null && text.length() > 0) {
 				// ssanders: Process the lines one-by-one in
 				// order to have the proper prompting
 				console.getDocumentListener().handleSynchronously = true;
 				try {
 
 					if (text.indexOf("\n") == -1) {
 						Point selectedRange = getSelectedRange();
 						getTextWidget().insert(text);
 						setCaretOffset(selectedRange.x + text.length());
 
 					} else {
 						StringTokenizer tokenizer = new StringTokenizer(text,
 								"\n\r"); //$NON-NLS-1$
 						while (tokenizer.hasMoreTokens() == true) {
 							final String finText = tokenizer.nextToken();
 							insertText(finText + "\n"); //$NON-NLS-1$
 						}
 					}
 				} finally {
 					console.getDocumentListener().handleSynchronously = false;
 				}
 			}
 		}
 
 	}
 
 	private ScriptConsoleHistory history;
 
 	private ScriptConsole console;
 
 	public int getCaretPosition() {
 		return getTextWidget().getCaretOffset();
 	}
 
 	public void enableProcessing() {
 		console.getDocumentListener().bEnabled = true;
 	}
 
 	public void disableProcessing() {
 		console.getDocumentListener().bEnabled = false;
 	}
 
 	public void setCaretPosition(final int offset) {
 		if (getTextWidget() != null) {
 			getTextWidget().getDisplay().asyncExec(new Runnable() {
 
 				public void run() {
 					if (getTextWidget() != null) {
 						getTextWidget().setCaretOffset(offset);
 					}
 				}
 			});
 		}
 	}
 
 	// public int beginLineOffset() throws BadLocationException {
 	// IDocument doc = getDocument();
 	// int offset = getCaretPosition();
 	// int line = doc.getLineOfOffset(offset);
 	// return offset - doc.getLineOffset(line);
 	// }
 
 	protected boolean isCaretOnLastLine() {
 		try {
 			IDocument doc = getDocument();
 			int line = doc.getLineOfOffset(getCaretPosition());
 			return line == doc.getNumberOfLines() - 1;
 		} catch (BadLocationException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 	}
 
 	protected StyledText createTextWidget(Composite parent, int styles) {
 		return new ScriptConsoleStyledText(parent, styles);
 	}
 
 	public ScriptConsoleViewer(Composite parent, final ScriptConsole console,
 			final IScriptConsoleContentHandler contentHandler) {
 		super(parent, console);
 
 		this.console = console;
 
 		this.history = console.getHistory();
 
 		console.getDocumentListener().addViewer(this);
 
 		final StyledText styledText = getTextWidget();
 
 		// styledText.setEditable(false);
 
 		// Correct keyboard actions
 		styledText.addFocusListener(new FocusListener() {
 
 			public void focusGained(FocusEvent e) {
 				setCaretPosition(getDocument().getLength());
 				styledText.removeFocusListener(this);
 			}
 
 			public void focusLost(FocusEvent e) {
 
 			}
 		});
 		DropTarget target = new DropTarget(styledText, DND.DROP_DEFAULT
 				| DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
 		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
 		target.addDropListener(new DropTargetAdapter() {
 			public void dragEnter(DropTargetEvent e) {
 				if (e.detail == DND.DROP_DEFAULT)
 					e.detail = DND.DROP_COPY;
 			}
 
 			public void dragOperationChanged(DropTargetEvent e) {
 				if (e.detail == DND.DROP_DEFAULT)
 					e.detail = DND.DROP_COPY;
 			}
 
 			public void drop(DropTargetEvent e) {
 				((ScriptConsoleStyledText) styledText).paste((String) e.data);
 			}
 		});
 		styledText.setKeyBinding('X' | SWT.MOD1, ST.COPY);
 		styledText.addVerifyKeyListener(new VerifyKeyListener() {
 			public void verifyKey(VerifyEvent event) {
 				try {
 					if (event.character != '\0') {
 						if ((event.stateMask & SWT.MOD1) == 0) {
 							// Printable character
 							// ssanders: Ensure selection is on last line
 							ConsoleDocumentListener listener = console
 									.getDocumentListener();
 							int selStart = getSelectedRange().x;
 							int selEnd = (getSelectedRange().x + getSelectedRange().y);
 							int clOffset = listener.getCommandLineOffset();
 							int clLength = listener.getCommandLineLength();
 							if (selStart < clOffset) {
 								int selLength;
 
 								if (selEnd < clOffset) {
 									selStart = (clOffset + clLength);
 									selLength = 0;
 								} else {
 									selStart = clOffset;
 									selLength = (selEnd - selStart);
 								}
 
 								setSelectedRange(selStart, selLength);
 							}
 
 							if (getCaretPosition() < console
 									.getDocumentListener()
 									.getCommandLineOffset()) {
 								event.doit = false;
 								return;
 							}
 						}
 
 						if (event.character == SWT.CR) {
 							getTextWidget().setCaretOffset(
 									getDocument().getLength());
 							return;
 						}
 
 						// ssanders: Avoid outputting " " when invoking
 						// completion on Mac OS X
 						if (event.keyCode == 32
 								&& (event.stateMask & SWT.CTRL) > 0) {
 							event.doit = false;
 							return;
 						}
 
 						// ssanders: Avoid outputting "<Tab>" when invoking
 						// completion on Mac OS X
 						if (event.keyCode == SWT.TAB) {
 							event.doit = false;
 							return;
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		styledText.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent e) {
 				if (e.keyCode == SWT.TAB
 						|| (e.keyCode == ' ' && e.stateMask == SWT.CTRL)) {
 					contentHandler.contentAssistRequired();
 				}
 			}
 
 			public void keyReleased(KeyEvent e) {
 			}
 		});
 
 		if (console.getDocumentListener().viewerList.size() == 1) {
 			clear();
 		}
 	}
 
 	// IConsoleTextViewer
 	public String getCommandLine() {
 		try {
 			return console.getDocumentListener().getCommandLine();
 		} catch (BadLocationException e) {
 			return null;
 		}
 	}
 
 	public int getCommandLineOffset() {
 		try {
 			return console.getDocumentListener().getCommandLineOffset();
 		} catch (BadLocationException e) {
 			return -1;
 		}
 	}
 
 	public void clear() {
 		console.getDocumentListener().clear();
 	}
 
 	public void insertText(String text) {
 		getTextWidget().append(text);
 	}
 
 	public boolean canDoOperation(int operation) {
 		boolean canDoOperation = super.canDoOperation(operation);
 
 		if (canDoOperation) {
 			switch (operation) {
 			case CUT:
 			case DELETE:
 				// case PASTE:
 			case SHIFT_LEFT:
 			case SHIFT_RIGHT:
 			case PREFIX:
 			case STRIP_PREFIX:
 				canDoOperation = isCaretOnLastLine();
 			}
 		}
 
 		return canDoOperation;
 	}
 
 	public void activatePlugins() {
 		fHyperlinkManager = new HyperlinkManager(
 				HyperlinkManager.LONGEST_REGION_FIRST);
 		fHyperlinkManager.install(this, fHyperlinkPresenter,
 				fHyperlinkDetectors, fHyperlinkStateMask);
 
 		super.activatePlugins();
 	}
 
 	public void dispose() {
 		console.getDocumentListener().removeViewer(this);
 	}
 
 }
