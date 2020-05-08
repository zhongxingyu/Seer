 package org.eclipse.dltk.console.ui.internal;
 
 import java.io.IOException;
 
 import org.eclipse.dltk.console.ScriptConsoleHistory;
 import org.eclipse.dltk.console.ScriptConsolePrompt;
 import org.eclipse.dltk.console.ui.IScriptConsoleViewer;
 import org.eclipse.dltk.console.ui.ScriptConsole;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.console.TextConsoleViewer;
 
 public class ScriptConsoleViewer extends TextConsoleViewer implements
 		IScriptConsoleViewer {
 
 	private static class ConsoleDocumentListener implements IDocumentListener {
 
 		private ICommandHandler handler;
 
 		private ScriptConsolePrompt prompt;
 
 		private ScriptConsoleHistory history;
 
 		private int offset;
 
 		private IDocument doc;
 
 		protected void connectListener() {
 			doc.addDocumentListener(this);
 		}
 
 		protected void disconnectListener() {
 			doc.removeDocumentListener(this);
 		}
 
 		public void clear() {
 			try {
 				disconnectListener();
 				doc.set("");
 				appendInvitation();
 				viewer.setCaretPosition(doc.getLength());
 				connectListener();
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 		}
 
 		private ScriptConsoleViewer viewer;
 
 		public ConsoleDocumentListener(ScriptConsoleViewer viewer,
 				ICommandHandler handler, ScriptConsolePrompt prompt,
 				ScriptConsoleHistory history) {
 			this.prompt = prompt;
 			this.handler = handler;
 			this.history = history;
 
 			this.viewer = viewer;
 
 			this.offset = 0;
 
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
 
 		protected void handleCommandLine() throws BadLocationException,
 				IOException {
 			// viewer.getTextWidget().setEditable(false);
 
 			final String command = getCommandLine();
 			appendDelimeter();
 
 			processResult(handler.handleCommand(command));
 
 		}
 
 		protected void processResult(final String result)
 				throws BadLocationException {
 			if (result != null) {
 				appendText(result);
 				history.commit();
 				offset = getLastLineLength();
 			}
 			appendInvitation();
 		}
 
 		protected void printString(String str) {
 			for (int i = 0; i < str.length(); ++i) {
 				char ch = str.charAt(i);
 				if (ch == '\r') {
 					System.out.print("\\r");
 				} else if (ch == '\n') {
 					System.out.print("\\n");
 				} else {
 					System.out.print(ch);
 				}
 			}
 
 			System.out.print('\n');
 		}
 
 		protected void proccessAddition(int offset, String text) {
 			try {
 				String delim = getDelimeter();
 
 				text = doc.get(offset, doc.getLength() - offset);
 
 				doc.replace(offset, text.length(), "");
 
 				text = text.replaceAll("\r\n|\n|\r", delim);
 
 				int start = 0;
 				int index = -1;
 				while ((index = text.indexOf(delim, start)) != -1) {
 					appendText(text.substring(start, index));
 					history.update(getCommandLine());
 					start = index + delim.length();
 					handleCommandLine();
 				}
 
 				appendText(text.substring(start, text.length()));
 				history.update(getCommandLine());
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public void documentChanged(DocumentEvent event) {
 			disconnectListener();
 			proccessAddition(event.getOffset(), event.getText());
 			connectListener();
 		}
 
 		protected void appendText(String text) throws BadLocationException {
 			doc.replace(doc.getLength(), 0, text);
 		}
 
 		protected void appendInvitation() throws BadLocationException {
 			appendText(prompt.toString());
 			viewer.setCaretPosition(doc.getLength());
 			viewer.revealEndOfDocument();
 		}
 
 		protected void appendDelimeter() throws BadLocationException {
 			appendText(getDelimeter());
 		}
 
 		protected String getDelimeter() {
 			return TextUtilities.getDefaultLineDelimiter(doc);
 		}
 
 		protected int getLastLineLength() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineLength(lastLine);
 		}
 
 		public int getLastLineReadOnlySize() {
 			return prompt.toString().length() + offset;
 		}
 
 		public int getCommandLineOffset() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineOffset(lastLine) + getLastLineReadOnlySize();
 		}
 
 		public int getCommandLineLength() throws BadLocationException {
 			int lastLine = doc.getNumberOfLines() - 1;
 			return doc.getLineLength(lastLine) - getLastLineReadOnlySize();
 		}
 
 		public String getCommandLine() throws BadLocationException {
 			return doc.get(getCommandLineOffset(), getCommandLineLength());
 		}
 
 		public void setCommandLine(String command) throws BadLocationException {
 			doc
 					.replace(getCommandLineOffset(), getCommandLineLength(),
 							command);
 		}
 	}
 
 	private class ScriptCnosoleStyledText extends StyledText {
 
 		public ScriptCnosoleStyledText(Composite parent, int style) {
 			super(parent, style);
 		}
 
 		public void invokeAction(int action) {
 			if (isCaretOnLastLine()) {
 				try {
 					switch (action) {
 					case ST.LINE_UP:
 						history.prev();
 						listener.setCommandLine(history.get());
 						setCaretOffset(getDocument().getLength());
 						return;
 
 					case ST.LINE_DOWN:
 						history.next();
 						listener.setCommandLine(history.get());
 						setCaretOffset(getDocument().getLength());
 						return;
 
 					case ST.DELETE_PREVIOUS:
 						if (getCaretOffset() <= getCommandLineOffset()) {
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
 					}
 
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 					return;
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
 
 		public void paste() {
 			if (isCaretOnLastLine()) {
 				super.paste();
 			}
 		}
 	}
 
 	private ScriptConsoleHistory history;
 
 	private ConsoleDocumentListener listener;
 
 	public int getCaretPosition() {
 		return getTextWidget().getCaretOffset();
 	}
 
 	public void setCaretPosition(final int offset) {
 		getTextWidget().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				getTextWidget().setCaretOffset(offset);
 			}
 		});
 	}
 
 	public int beginLineOffset() throws BadLocationException {
 		IDocument doc = getDocument();
 		int offset = getCaretPosition();
 		int line = doc.getLineOfOffset(offset);
 		return offset - doc.getLineOffset(line);
 	}
 
 	protected boolean isCaretOnLastLine() {
 		try {
 			IDocument doc = getDocument();
 			int line = doc.getLineOfOffset(getCaretPosition());
 			return line == doc.getNumberOfLines() - 1;
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	protected StyledText createTextWidget(Composite parent, int styles) {
 		return new ScriptCnosoleStyledText(parent, styles);
 	}
 
 	public ScriptConsoleViewer(Composite parent, ScriptConsole console,
 			final IScriptConsoleContentHandler contentHandler) {
 		super(parent, console);
 
 		this.history = console.getHistory();
 
 		this.listener = new ConsoleDocumentListener(this, console, console
 				.getPrompt(), console.getHistory());
 		this.listener.setDocument(getDocument());
 
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
 
 		styledText.addVerifyKeyListener(new VerifyKeyListener() {
 			public void verifyKey(VerifyEvent event) {
 				try {
 					if (event.character != '\0') {
 						// Printable character
 						if (!isCaretOnLastLine()) {
 							event.doit = false;
 							return;
 						}
 
 						if (beginLineOffset() < listener
 								.getLastLineReadOnlySize()) {
 							event.doit = false;
 							return;
 						}
 
 						if (event.character == SWT.CR) {
 							getTextWidget().setCaretOffset(
 									getDocument().getLength());
 							return;
 						}
 						if (event.keyCode == 9) {
 							event.doit = false;
 							return;
 						}
 						if (event.keyCode == 32
 								&& (event.stateMask & SWT.CTRL) > 0) {
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
 				// if (e.keyCode == 32 && (e.stateMask & SWT.CTRL) > 0) {
 				if ((e.keyCode == 32 && (e.stateMask & SWT.CTRL) > 0)
 						|| (e.keyCode == 9)) {
 					// System.out.println(".keyPressed()");
 					contentHandler.contentAssistRequired();
 				}
 
 			}
 
 			public void keyReleased(KeyEvent e) {
 			}
 		});
 
 		clear();
 	}
 
 	// IConsoleTextViewer
 	public String getCommandLine() {
 		try {
 			return listener.getCommandLine();
 		} catch (BadLocationException e) {
 			return null;
 		}
 	}
 
 	public int getCommandLineOffset() {
 		try {
 			return listener.getCommandLineOffset();
 		} catch (BadLocationException e) {
 			return -1;
 		}
 	}
 
 	public void clear() {
 		listener.clear();
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
	        case PASTE:
	        case SHIFT_LEFT:
	        case SHIFT_RIGHT:
	        case PREFIX:
	        case STRIP_PREFIX:
	          canDoOperation = isCaretOnLastLine();
	      }
	    }

	    return canDoOperation;
	  }
 }
