 package com.redcareditor.mate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.logging.Handler;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.CompositeRuler;
 import org.eclipse.jface.text.source.LineNumberRulerColumn;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 import com.redcareditor.mate.colouring.Colourer;
 import com.redcareditor.mate.colouring.swt.SwtColourer;
 import com.redcareditor.mate.document.MateDocument;
 import com.redcareditor.mate.document.MateTextLocation;
 import com.redcareditor.mate.document.swt.SwtMateTextLocation;
 import com.redcareditor.mate.document.swt.SwtMateDocument;
 import com.redcareditor.mate.undo.MateTextUndoManager;
 import com.redcareditor.mate.undo.swt.SwtMateTextUndoManager;
 import com.redcareditor.mate.WhitespaceCharacterPainter;
 import com.redcareditor.onig.NullRx;
 import com.redcareditor.onig.Rx;
 import com.redcareditor.theme.Theme;
 import com.redcareditor.theme.ThemeManager;
 import com.redcareditor.util.SingleLineFormatter;
 
 public class MateText {
 	public Parser parser;
 	public Colourer colourer;
 	public Logger logger;
 
 	static private Handler _consoleHandler;
 	static public Handler consoleHandler() {
 		if (_consoleHandler == null) {
 			_consoleHandler = new ConsoleHandler();
 			_consoleHandler.setFormatter(new SingleLineFormatter());
 		}
 		return _consoleHandler;
 	}
 
 	/* components plugged together */
 	public SourceViewer viewer;
 	private IDocument document;
 	private CompositeRuler gutter;
 	private LineNumberRulerColumn lineNumbers;
 	private SwtMateDocument mateDocument;
 
 	private MateTextUndoManager undoManager;
 	private List<IGrammarListener> grammarListeners;
 	
 	private boolean singleLine;
 	private WhitespaceCharacterPainter whitespaceCharacterPainter;
 	private boolean showingInvisibles;

 	public MateText(Composite parent) {
 		this(parent, false);
 	}
 
 	public MateText(Composite parent, boolean thisSingleLine) {
 		singleLine = thisSingleLine;
 		document = new Document();
 		gutter = constructRuler();
 		if (singleLine) {
 			viewer = new SourceViewer(parent, null, SWT.FULL_SELECTION | SWT.HORIZONTAL | SWT.VERTICAL | SWT.SINGLE);
 		}
 		else {
 			viewer = new SourceViewer(parent, gutter, SWT.FULL_SELECTION | SWT.HORIZONTAL | SWT.VERTICAL);
 		}
 		viewer.setDocument(document);
 		
 		whitespaceCharacterPainter = new WhitespaceCharacterPainter(viewer);
 		showingInvisibles = false;
 		
 		colourer = new SwtColourer(this);
 		mateDocument = new SwtMateDocument(this);
 		grammarListeners = new ArrayList<IGrammarListener>();
 		getTextWidget().setLeftMargin(5);
 		logger = Logger.getLogger("JMV.MateText");
 		logger.setUseParentHandlers(false);
 		logger.setLevel(Level.SEVERE);
 		for (Handler h : logger.getHandlers()) {
 			logger.removeHandler(h);
 		}
 		logger.addHandler(MateText.consoleHandler());
 		logger.info("Created MateText");
 	}
 
 	public boolean isSingleLine() {
 	  return singleLine;
 	}
 	
 	public void showInvisibles(boolean should) {
 		if (should) {
 			showingInvisibles = true;
 			viewer.addPainter(whitespaceCharacterPainter);
 		} else {
 			showingInvisibles = false;
 			viewer.removePainter(whitespaceCharacterPainter);
 		}
 	}
 	
 	public boolean isShowingInvisibles() {
 		return showingInvisibles;
 	}
 	
 	private CompositeRuler constructRuler() {
 		CompositeRuler ruler = new CompositeRuler(0);
 		lineNumbers = new LineNumberRulerColumn();
 		ruler.addDecorator(0, lineNumbers);
 		return ruler;
 	}
 
 	public void attachUpdater() {
 
 	}
 	
 	public String grammarName() {
 		return parser.grammar.name;
 	}
 
 	public StyledText getTextWidget() {
 		return viewer.getTextWidget();
 	}
 
 	public IDocument getDocument() {
 		return document;
 	}
 
 	public MateDocument getMateDocument() {
 		return mateDocument;
 	}
 
 	public StyledText getControl() {
 		return viewer.getTextWidget();
 	}
 
 	public boolean shouldColour() {
 		return parser.shouldColour();
 	}
 	
 	public String scopeAt(int line, int line_offset) {
 		return parser.root.scopeAt(line, line_offset).hierarchyNames(true);
 	}
 	
 	// Sets the grammar explicitly by name.
 	// TODO: restore the uncolouring stuff
 	public boolean setGrammarByName(String name) {
 		// System.out.printf("setGrammarByName(%s)\n", name);
 		if (this.parser != null && this.parser.grammar.name.equals(name))
 			return true;
 
 		for (Bundle bundle : Bundle.getBundles()) {
 			for (Grammar grammar : bundle.getGrammars()) {
 				if (grammar.name.equals(name)) {
 					if (this.parser != null) {
 						this.parser.close();
 					}
 					this.parser = new Parser(grammar, this);
 					if (colourer != null) {
 						colourer.setGlobalColours();
 					}
 					getMateDocument().reparseAll();
 					for (IGrammarListener grammarListener : grammarListeners) {
 						grammarListener.grammarChanged(grammar.name);
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	// Sets the grammar by the file extension. If unable to find
 	// a grammar, sets the grammar to null. Returns the grammar
 	// name or null.
 	public String setGrammarByFilename(String fileName) {
 		String bestName = null;
 		long bestLength = 0;
 		for (Bundle bundle : Bundle.getBundles()) {
 			for (Grammar grammar : bundle.getGrammars()) {
 				if (grammar.fileTypes != null) {
 					for (String ext : grammar.fileTypes) {
 						if (fileName.endsWith(ext) && (bestName == null || ext.length() > bestLength)) {
 							bestName = grammar.name;
 							bestLength = ext.length();
 						}
 					}
 				}
 			}
 		}
 		if (bestName != null) {
 			if (this.parser == null || this.parser.grammar.name != bestName) {
 				setGrammarByName(bestName);
 			}
 			return bestName;
 		}
 		return null;
 	}
 
 	// Sets the grammar by examining the first line. If unable to find
 	// a grammar, sets the grammar to null. Returns the grammar
 	// name or null.
 	public String setGrammarByFirstLine(String firstLine) {
 		Rx re;
 		for (Bundle bundle : Bundle.getBundles()) {
 			for (Grammar grammar : bundle.getGrammars()) {
 				re = grammar.firstLineMatch;
 				if (re instanceof NullRx) {
 				} else {
 					if (re.search(firstLine, 0, (int) firstLine.length()) != null) {
 						setGrammarByName(grammar.name);
 						return grammar.name;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean setThemeByName(String name) {
 		for (Theme theme : ThemeManager.themes) {
 			if (theme.name.equals(name)) {
 				this.colourer.setTheme(theme);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void setFont(String name, int size) {
 		Font font = new Font(Display.getCurrent(), name, size, 0);
 		viewer.getTextWidget().setFont(font);
 		lineNumbers.setFont(font);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setGutterBackground(Color color) {
 		lineNumbers.setBackground(color);
 	}
 
 	public void setGutterForeground(Color color) {
 		lineNumbers.setForeground(color);
 	}
 	
 	public void addGrammarListener(IGrammarListener listener) {
 		grammarListeners.add(listener);
 	}
 	
 	public void removeGrammarListener(IGrammarListener listener) {
 		grammarListeners.remove(listener);
 	}
 	
 	public void redraw() {
 		// SwtMateTextLocation startLocation = new SwtMateTextLocation(0, getMateDocument());
 		// SwtMateTextLocation endLocation = new SwtMateTextLocation(0 + getTextWidget().getCharCount(), getMateDocument());
 		getTextWidget().redraw();
 	}
 }
