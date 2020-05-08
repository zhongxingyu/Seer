 /*
  *   Copyright (C) 2006  The Concord Consortium, Inc.,
  *   25 Love Lane, Concord, MA 01742
  *
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.modeler.text;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.sound.sampled.Clip;
 import javax.swing.BorderFactory;
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultEditorKit;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.ElementIterator;
 import javax.swing.text.Highlighter;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.TextAction;
 import javax.swing.text.html.HTML;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoManager;
 
 import org.xml.sax.SAXException;
 
 import org.concord.modeler.ActionNotifier;
 import org.concord.modeler.ActivityButton;
 import org.concord.modeler.AudioPlayer;
 import org.concord.modeler.AutoResizable;
 import org.concord.modeler.ConnectionManager;
 import org.concord.modeler.ComponentPool;
 import org.concord.modeler.Debugger;
 import org.concord.modeler.DisasterHandler;
 import org.concord.modeler.Editor;
 import org.concord.modeler.Embeddable;
 import org.concord.modeler.Engine;
 import org.concord.modeler.FileFilterFactory;
 import org.concord.modeler.HistoryManager;
 import org.concord.modeler.HtmlService;
 import org.concord.modeler.ImageQuestion;
 import org.concord.modeler.Initializer;
 import org.concord.modeler.InstancePool;
 import org.concord.modeler.LogDumper;
 import org.concord.modeler.MidiPlayer;
 import org.concord.modeler.Model;
 import org.concord.modeler.ModelCanvas;
 import org.concord.modeler.Modeler;
 import org.concord.modeler.ModelerUtilities;
 import org.concord.modeler.Navigable;
 import org.concord.modeler.Navigator;
 import org.concord.modeler.PageApplet;
 import org.concord.modeler.PageBarGraph;
 import org.concord.modeler.PageButton;
 import org.concord.modeler.PageCheckBox;
 import org.concord.modeler.PageComboBox;
 import org.concord.modeler.PageDNAScroller;
 import org.concord.modeler.PageDiffractionInstrument;
 import org.concord.modeler.PageElectronicStructureViewer;
 import org.concord.modeler.PageFeedbackArea;
 import org.concord.modeler.PageFunctionGraph;
 import org.concord.modeler.PageJContainer;
 import org.concord.modeler.PageMd3d;
 import org.concord.modeler.PageMolecularViewer;
 import org.concord.modeler.PageMultipleChoice;
 import org.concord.modeler.PageNumericBox;
 import org.concord.modeler.PagePeriodicTable;
 import org.concord.modeler.PagePhotonSpectrometer;
 import org.concord.modeler.PagePotentialHill;
 import org.concord.modeler.PagePotentialWell;
 import org.concord.modeler.PageRadioButton;
 import org.concord.modeler.PageScriptConsole;
 import org.concord.modeler.PageSlider;
 import org.concord.modeler.PageSpinner;
 import org.concord.modeler.PageTable;
 import org.concord.modeler.PageTextArea;
 import org.concord.modeler.PageTextBox;
 import org.concord.modeler.PageTextField;
 import org.concord.modeler.PageXYGraph;
 import org.concord.modeler.SampledAudioPlayer;
 import org.concord.modeler.ScriptCallback;
 import org.concord.modeler.Scriptable;
 import org.concord.modeler.SearchTextField;
 import org.concord.modeler.Upload;
 import org.concord.modeler.draw.FillMode;
 import org.concord.modeler.draw.GradientFactory;
 import org.concord.modeler.event.HotlinkListener;
 import org.concord.modeler.event.ImageEvent;
 import org.concord.modeler.event.ImageImporter;
 import org.concord.modeler.event.PageEvent;
 import org.concord.modeler.event.PageListener;
 import org.concord.modeler.event.ProgressEvent;
 import org.concord.modeler.event.ProgressListener;
 import org.concord.modeler.ui.IconPool;
 import org.concord.modeler.ui.TextComponentPopupMenu;
 import org.concord.modeler.util.FileChooser;
 import org.concord.modeler.util.FileUtilities;
 import org.concord.modeler.util.ImageReader;
 import org.concord.modeler.util.SwingWorker;
 import org.concord.mw2d.MDView;
 import org.concord.mw2d.StyleConstant;
 import org.concord.mw2d.ui.AtomContainer;
 import org.concord.mw2d.ui.ChemContainer;
 import org.concord.mw2d.ui.GBContainer;
 import org.concord.mw2d.models.MDModel;
 import org.concord.mw2d.models.MesoModel;
 import org.concord.mw2d.models.MolecularModel;
 import org.concord.mw2d.models.Solvent;
 
 public class Page extends JTextPane implements Navigable, HotlinkListener, HyperlinkListener, ProgressListener,
 		ImageImporter, ClipboardOwner {
 
 	final static int LOADER_THREAD_PRIORITY = Thread.MIN_PRIORITY;
 	final static String OS = System.getProperty("os.name");
 
 	public final static String REFRESH = "Reload";
 	public final static String BULLET = "bullet";
 	public final static String INCREASE_INDENT = "increase-indent";
 	public final static String DECREASE_INDENT = "decrease-indent";
 	public final static String INCREASE_FONT_SIZE = "Increase Font Size";
 	public final static String DECREASE_FONT_SIZE = "Decrease Font Size";
 	public final static String LEFT_ALIGN = "left-justify";
 	public final static String RIGHT_ALIGN = "right-justify";
 	public final static String CENTER_ALIGN = "center-justify";
 	public final static String BOLD = "font-bold";
 	public final static String ITALIC = "font-italic";
 	public final static String UNDERLINE = "font-underline";
 	public final static String NEW_PAGE = "New Blank Page";
 	public final static String CLOSE_PAGE = "Close";
 	public final static String OPEN_PAGE = "Open";
 	public final static String SAVE_PAGE = "Save";
 	public final static String SAVE_PAGE_AS = "Save As";
 	public final static String HTML_CONVERSION = "Save As HTML";
 	public final static String UNDO = "Undo";
 	public final static String REDO = "Redo";
 	public final static String SET_PROPERTIES = OS.startsWith("Mac") ? "Get Info" : "Properties";
 	public final static String INSERT_ATOM_CONTAINER = "Basic 2D Molecular Simulator";
 	public final static String INSERT_CHEM_CONTAINER = "2D Chemical Reaction Simulator";
 	public final static String INSERT_PROSYN_CONTAINER = "2D Protein Synthesis Simulator";
 	public final static String INSERT_GB_CONTAINER = "2D Mesoscale Particle Simulator";
 	public final static String INSERT_JMOL = "Jmol Molecular Viewer";
 	public final static String INSERT_MW3D = "3D Molecular Simulator";
 	public final static String INSERT_COMPONENT = "Embedded Component";
 
 	private final static Object[] RADIO_BUTTON_OPTIONS = new Object[] { (byte) 2, (byte) 3, (byte) 4, (byte) 5,
 			(byte) 6, "None" };
 
 	private final static String UNKNOWN_LOCATION = "? unknown location......";
 	private final static Pattern SCRIPT_PATTERN = Pattern.compile("(?i)script(\\s*):");
 	private final static Pattern SCRIPT_PATTERN2 = Pattern.compile("(?i)@");
 	private static String softwareVersion = "x";
 	private static boolean nativeLookAndFeelUsed;
 	private static String softwareName;
 
 	final static float INDENT_STEP = 20;
 	private static final Date DATE = new Date();
 	private final static String TMPZIP = System.getProperty("file.separator") + "cache"
 			+ System.getProperty("file.separator") + "tmpzip" + System.getProperty("file.separator");
 
 	private FontDialog fontDialog;
 	private ParagraphDialog paragraphDialog;
 	private BulletDialog bulletDialog;
 	private SymbolDialog symbolDialog;
 	private HyperlinkDialog hyperlinkDialog;
 	private PagePropertiesDialog propertiesDialog;
 	ColorBarDialog colorBarDialog;
 	private PagePopupMenu popupMenu;
 	ImagePopupMenu imagePopupMenu;
 	ColorBarPopupMenu colorBarPopupMenu;
 	private HyperlinkPopupMenu linkPopupMenu;
 
 	/*
 	 * <code>PrintPreview</code> acts like a mediator between <code>Page</code> and printer. Any printing job should
 	 * pass through the <code>PrintPreview</code> gateway to get to a printer, even if the user does not mean to
 	 * preview pages before printing.
 	 */
 	private PrintPreview printPreview;
 	private static PrintParameters printParam = new PrintParameters();
 
 	private MidiPlayer midiPlayer;
 	private SampledAudioPlayer sampledAudioPlayer;
 	private ActionNotifier actionNotifier;
 	private UndoManager undoManager;
 	private Highlighter.HighlightPainter myHighlightPainter;
 	private UndoableEditListener undoHandler;
 	private ComponentPool componentPool;
 	private volatile String pageAddress = "Untitled.cml";
 	private URI pageURI;
 	private String pageTitle;
 	private String backgroundSound;
 	private boolean loopBackgroundSound;
 	private FillMode fillMode;
 	private static Color linkColor = Color.blue, visitedColor = new Color(0, 0, 0x99);
 	private static boolean linkUnderline = true;
 	private static int defaultFontSize = 12;
 	private static String defaultFontFamily = "Verdana";
 	private static Clipboard clipboard;
 	private static String clipboardText;
 	private static boolean runOnCD;
 
 	private List<Action> actionToLockWhileLoading;
 	private List<Icon> selectedImages;
 	private List<JComponent> selectedComponents;
 	private List<PageListener> pageListenerList;
 	private Map<Object, Object> properties;
 	private Map<String, Action> activityActionMap;
 	private Map<String, Action> actions;
 
 	private Editor editor;
 	private PageXMLDecoder decoder;
 	private PageXMLEncoder encoder;
 	private PlainTextWriter plainTextWriter;
 	private JLabel urlDisplay;
 	private JMenu recentFilesMenu;
 	private EditResponder editResponder;
 	SaveReminder saveReminder;
 	private int fontIncrement;
 	private boolean selectedImagesInverted;
 	private boolean componentSelected;
 	private ImageIcon backgroundImage;
 	private int iconWidth, iconHeight;
 	private Navigator navigator;
 	private volatile boolean isReading, isWriting;
 	private volatile boolean isReadingModel;
 	private volatile boolean targetIsBlank;
 	private volatile HyperlinkParameter linkParam;
 	private static StyledDocument styledDocumentInstance;
 	private static int oldCaretPosition;
 	private ImageReader imageReader;
 	private static FileChooser fileChooser;
 	Action uploadReportAction;
 	private String reportTitle;
 	private boolean rememberViewPosition = true;
 	private String characterEncoding = "UTF-8";
 	private final Object lock = new Object();
 	private final Object scriptLock = new Object();
 	private Thread pageLoadingThread;
 	private boolean embeddedImageFound;
 	private PageScripter scripter;
 
 	Action printAction, htmlizeAction, saveAsAction;
 	private Action gradeAction, hintAction, bulletAction, selectAllAction, hyperlinkAction, propertiesAction;
 	private Action increaseFontSizeAction, decreaseFontSizeAction, increaseIndentAction, decreaseIndentAction;
 	private Action insertFileAction, refreshAction, newAction, scriptAction, closeAction;
 	private Action openAction, saveAction, colorBarAction, symbolAction, insertComponentAction;
 	private Action insertAtomContainerAction, insertChemContainerAction, insertProsynContainerAction;
 	private Action insertGBContainerAction;
 	private Action printPreviewAction, pageSetupAction, fontAction, paragraphAction, insertBulletAction;
 
 	static byte threadIndex;
 
 	public Page() {
 
 		super();
 
 		createActions();
 
 		setOpaque(false);
 		setBackground(Color.white);
 		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		setFont(new Font(defaultFontFamily, Font.PLAIN, defaultFontSize));
 
 		saveReminder = new SaveReminder();
 		editResponder = new EditResponder(this);
 
 		undoManager = new UndoManager();
 		myHighlightPainter = new MyHighlightPainter(Color.yellow);
 
 		bulletAction = new BulletAction();
 		increaseFontSizeAction = new ResizeFontAction(this, true);
 		decreaseFontSizeAction = new ResizeFontAction(this, false);
 		increaseIndentAction = new ChangeIndentAction(this, true);
 		decreaseIndentAction = new ChangeIndentAction(this, false);
 
 		imageReader = new ImageReader("Input Image", fileChooser, JOptionPane.getFrameForComponent(this));
 		imageReader.addImageImporter(this);
 		createActionTable();
 
 		undoHandler = new UndoableEditListener() {
 			public void undoableEditHappened(UndoableEditEvent e) {
 				undoManager.addEdit(e.getEdit());
 				((UndoAction) getAction(UNDO)).updateState();
 				((RedoAction) getAction(REDO)).updateState();
 			}
 		};
 		getDocument().addUndoableEditListener(undoHandler);
 		getDocument().addDocumentListener(editResponder);
 
 		decoder = new PageXMLDecoder(this);
 		encoder = new PageXMLEncoder(this);
 
 		hintAction = new HintAction(this);
 		gradeAction = new GradeAction(this);
 		activityActionMap = Collections.synchronizedMap(new TreeMap<String, Action>());
 		activityActionMap.put(hintAction.toString(), hintAction);
 		activityActionMap.put(gradeAction.toString(), gradeAction);
 		activityActionMap.put(scriptAction.toString(), scriptAction);
 		activityActionMap.put(closeAction.toString(), closeAction);
 		activityActionMap.put(printAction.toString(), printAction);
 		activityActionMap.put(saveAsAction.toString(), saveAsAction);
 		activityActionMap.put(refreshAction.toString(), refreshAction);
 
 		addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				processMousePressed(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				processMouseReleased(e);
 			}
 		});
 
 		addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				processMouseMoved(e);
 			}
 
 			public void mouseDragged(MouseEvent e) {
 				processMouseDragged(e);
 			}
 		});
 
 		popupMenu = new PagePopupMenu(this);
 		imagePopupMenu = new ImagePopupMenu(this);
 		colorBarPopupMenu = new ColorBarPopupMenu(this);
 		linkPopupMenu = new HyperlinkPopupMenu(this);
 
 		properties = new HashMap<Object, Object>();
 
 		selectedComponents = Collections.synchronizedList(new ArrayList<JComponent>());
 		selectedImages = Collections.synchronizedList(new ArrayList<Icon>());
 
 		addActionToLockWhileLoading(saveAction);
 		addActionToLockWhileLoading(saveAsAction);
 		addActionToLockWhileLoading(refreshAction);
 		addActionToLockWhileLoading(openAction);
 		addActionToLockWhileLoading(newAction);
 
 		setEditable(false); // called to disable the caret
 
 		if (clipboard == null)
 			clipboard = new Clipboard("Auxilary styled text clipboard");
 
 		// register key bindings
 
 		getInputMap().put((KeyStroke) refreshAction.getValue(Action.ACCELERATOR_KEY), REFRESH);
 		getActionMap().put(REFRESH, refreshAction);
 		getInputMap().put((KeyStroke) closeAction.getValue(Action.ACCELERATOR_KEY), CLOSE_PAGE);
 		getActionMap().put(CLOSE_PAGE, closeAction);
 
 		// NOTE!!! workaround to screen important key bindings to be passed to embedded components.
 		// This is a temporary solution, still searching for a solution of how to stop KeyEvent from
 		// passing from one component to another through the registered key bindings. The following are
 		// the keys that could cause conflicts between the Word Processor and the Model Builder.
 
 		Action emptyAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		};
 
 		if (OS.startsWith("Mac")) {
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK, true), "_x");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK, true), "_c");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK, true), "_v");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_MASK, true), "_m");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK, true), "_z");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_MASK, true), "_y");
 		}
 		else {
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK, true), "_x");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK, true), "_c");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK, true), "_v");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK, true), "_m");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK, true), "_z");
 			getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK, true), "_y");
 		}
 		getActionMap().put("_x", emptyAction);
 		getActionMap().put("_c", emptyAction);
 		getActionMap().put("_v", emptyAction);
 		getActionMap().put("_m", emptyAction);
 		getActionMap().put("_z", emptyAction);
 		getActionMap().put("_y", emptyAction);
 
 		// multiple key bindings
 
 		if (OS.startsWith("Windows")) {
 
 			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK, true);
 			getInputMap().put(ks, REFRESH);
 			getActionMap().put(REFRESH, refreshAction);
 
 			ks = KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK);
 			getInputMap().put(ks, CLOSE_PAGE);
 			getActionMap().put(CLOSE_PAGE, closeAction);
 
 		}
 
 		runOnCD = "true".equalsIgnoreCase(System.getProperty("mw.cd.mode"));
 
 	}
 
 	public void setEditor(Editor editor) {
 		this.editor = editor;
 	}
 
 	public Editor getEditor() {
 		return editor;
 	}
 
 	/*
 	 * This runs the scripts for this page. Note that it processes only the scripts for the page, but not those for the
 	 * embedded components such as text boxes.
 	 */
 	private void runScript(String script) {
 		if (scripter == null)
 			scripter = new PageScripter(this);
 		scripter.runScript(script);
 	}
 
 	public void setCharacterEncoding(String characterEncoding) {
 		this.characterEncoding = characterEncoding;
 	}
 
 	public String getCharacterEncoding() {
 		return characterEncoding;
 	}
 
 	public static void setNativeLookAndFeelUsed(boolean b) {
 		nativeLookAndFeelUsed = b;
 	}
 
 	public static boolean isNativeLookAndFeelUsed() {
 		return nativeLookAndFeelUsed;
 	}
 
 	public void rememberViewPosition(boolean b) {
 		rememberViewPosition = b;
 	}
 
 	public void removeAllDocumentListeners() {
 		try {
 			getDocument().remove(0, getDocument().getLength());
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		if (getDocument() instanceof AbstractDocument) {
 			DocumentListener[] dl = ((AbstractDocument) getDocument()).getDocumentListeners();
 			if (dl != null) {
 				for (DocumentListener l : dl)
 					getDocument().removeDocumentListener(l);
 			}
 		}
 	}
 
 	/**
 	 * destroy this page to allow it to be garbage-collected. Do NOT call this method unless you dispose the window that
 	 * contains this page.
 	 */
 	public void destroy() {
 
 		stopSound();
 		if (midiPlayer != null)
 			midiPlayer.destroy();
 		if (sampledAudioPlayer != null)
 			sampledAudioPlayer.destroy();
 
 		List<Embeddable> list = getEmbeddedComponents();
 		if (list != null) {
 			for (Embeddable embed : list) {
 				if (!(embed instanceof ModelCanvas))
 					embed.destroy();
 			}
 		}
 
 		// must call to deactive the page's document listeners first, otherwise the destroy code may
 		// cause the AbstractDocument.fireChangedUpdate() method to be called.
 		removeAllDocumentListeners();
 
 		if (pageLoadingThread != null)
 			pageLoadingThread.interrupt();
 		getDocument().removeUndoableEditListener(undoHandler);
 		getDocument().removeDocumentListener(editResponder);
 		editResponder.setPage(null);
 
 		actions.clear();
 		getInputMap().clear();
 		getActionMap().clear();
 		actionToLockWhileLoading.clear();
 		activityActionMap.clear();
 		pageListenerList.clear();
 		selectedComponents.clear();
 		selectedImages.clear();
 		properties.clear();
 
 		imageReader.setParent(null);
 		imageReader.getImageImporters().clear();
 		imageReader = null;
 
 		navigator.destroy();
 		setNavigator(null);
 		setProgressBar(null);
 		setURLDisplay(null);
 		setComponentPool(null);
 		setActionNotifier(null);
 		setUploadReportAction(null);
 
 		// getClipboard().setContents(null, null);
 
 		CaretListener[] cl = getCaretListeners();
 		if (cl != null) {
 			for (CaretListener i : cl)
 				removeCaretListener(i);
 		}
 
 		KeyListener[] kl = getKeyListeners();
 		if (kl != null) {
 			for (KeyListener i : kl)
 				removeKeyListener(i);
 		}
 
 		encoder.destroy();
 		decoder.destroy();
 		encoder = null;
 		decoder = null;
 		plainTextWriter = null;
 		myHighlightPainter = null;
 
 		destroyPopupMenu(popupMenu);
 		destroyPopupMenu(imagePopupMenu);
 		destroyPopupMenu(colorBarPopupMenu);
 		destroyPopupMenu(linkPopupMenu);
 
 		if (fontDialog != null)
 			fontDialog.dispose();
 		if (paragraphDialog != null)
 			paragraphDialog.dispose();
 		if (bulletDialog != null)
 			bulletDialog.dispose();
 		if (hyperlinkDialog != null)
 			hyperlinkDialog.dispose();
 		if (propertiesDialog != null) {
 			propertiesDialog.destroy();
 			propertiesDialog = null;
 		}
 
 		popupMenu = null;
 		imagePopupMenu = null;
 		colorBarPopupMenu = null;
 		linkPopupMenu = null;
 		undoManager = null;
 		undoHandler = null;
 		editResponder = null;
 		navigator = null;
 		urlDisplay = null;
 		refreshAction = null;
 		newAction = null;
 		closeAction = null;
 		hintAction = null;
 		gradeAction = null;
 		scriptAction = null;
 		insertFileAction = null;
 		selectAllAction = null;
 		bulletAction = null;
 		// numberingAction=null;
 		symbolAction = null;
 		hyperlinkAction = null;
 		printAction = null;
 		pageSetupAction = null;
 		printPreviewAction = null;
 		openAction = null;
 		saveAction = null;
 		saveAsAction = null;
 		insertAtomContainerAction = null;
 		insertChemContainerAction = null;
 		insertProsynContainerAction = null;
 		insertGBContainerAction = null;
 		insertComponentAction = null;
 		increaseIndentAction = null;
 		decreaseIndentAction = null;
 		increaseFontSizeAction = null;
 		decreaseFontSizeAction = null;
 		fontAction = null;
 		paragraphAction = null;
 		insertBulletAction = null;
 		propertiesAction = null;
 		htmlizeAction = null;
 		colorBarAction = null;
 
 	}
 
 	private static void destroyPopupMenu(JPopupMenu pm) {
 		if (pm == null)
 			return;
 		pm.setInvoker(null);
 		Component c;
 		AbstractButton b;
 		for (int i = 0, n = pm.getComponentCount(); i < n; i++) {
 			c = pm.getComponent(i);
 			if (c instanceof AbstractButton) {
 				b = (AbstractButton) c;
 				b.setAction(null);
 				ActionListener[] al = b.getActionListeners();
 				if (al != null) {
 					for (ActionListener l : al)
 						b.removeActionListener(l);
 				}
 				ItemListener[] il = b.getItemListeners();
 				if (il != null) {
 					for (ItemListener l : il)
 						b.removeItemListener(l);
 				}
 			}
 		}
 		PopupMenuListener[] pml = pm.getPopupMenuListeners();
 		if (pml != null) {
 			for (PopupMenuListener l : pml)
 				pm.removePopupMenuListener(l);
 		}
 		pm.removeAll();
 		if (pm instanceof TextComponentPopupMenu)
 			((TextComponentPopupMenu) pm).destroy();
 	}
 
 	public static void setSoftwareVersion(String s) {
 		softwareVersion = s;
 	}
 
 	public static void setSoftwareName(String s) {
 		softwareName = s;
 	}
 
 	static String getSoftwareName() {
 		return softwareName;
 	}
 
 	public Clipboard getClipboard() {
 		return clipboard;
 	}
 
 	/**
 	 * Set the editable status of this page. Also set editable status of embedded components on this page.
 	 */
 	public void setEditable(boolean b) {
 		super.setEditable(b);
 		List<Embeddable> list = getEmbeddedComponents();
 		int i = 0, j = 0;
 		if (list != null && !list.isEmpty()) {
 			synchronized (list) {
 				for (Embeddable e : list) {
 					e.setChangable(b);
 					if (e instanceof PageTextBox) {
 						((PageTextBox) e).setIndex(i++);
 						if (EventQueue.isDispatchThread())
 							((PageTextBox) e).showBoundary(b);
 					}
 					else if (e instanceof IconWrapper) {
 						((IconWrapper) e).setIndex(j++);
 						if (EventQueue.isDispatchThread())
 							((IconWrapper) e).showBoundary(b);
 					}
 					else if (e instanceof ModelCanvas) {
 						((ModelCanvas) e).getContainer().getView().setEditable(b);
 					}
 				}
 			}
 		}
 	}
 
 	public void addPageListener(PageListener pl) {
 		if (pl == null)
 			return;
 		if (pageListenerList == null)
 			pageListenerList = Collections.synchronizedList(new ArrayList<PageListener>());
 		pageListenerList.add(pl);
 	}
 
 	public void removePageListener(PageListener pl) {
 		if (pl == null)
 			return;
 		if (pageListenerList == null)
 			return;
 		pageListenerList.remove(pl);
 	}
 
 	void notifyPageListeners(final PageEvent e) {
 		if (pageListenerList == null || pageListenerList.isEmpty())
 			return;
 		Runnable r = new Runnable() {
 			public void run() {
 				synchronized (pageListenerList) {
 					for (PageListener l : pageListenerList)
 						l.pageUpdate(e);
 				}
 			}
 		};
 		if (EventQueue.isDispatchThread()) {
 			r.run();
 		}
 		else {
 			EventQueue.invokeLater(r);
 		}
 	}
 
 	public void openPageInNewWindow(String address) {
 		notifyPageListeners(new PageEvent(this, PageEvent.OPEN_NEW_WINDOW, address, null));
 	}
 
 	public void putActivityAction(Action a) {
 		activityActionMap.put(a.toString(), a);
 	}
 
 	public Map<String, Action> getActivityActions() {
 		return activityActionMap;
 	}
 
 	public Action getActivityAction(String s) {
 		return activityActionMap.get(s);
 	}
 
 	public void setReportTitle(String s) {
 		reportTitle = s;
 	}
 
 	public String getReportTitle() {
 		return reportTitle;
 	}
 
 	public void setUploadReportAction(Action a) {
 		uploadReportAction = a;
 	}
 
 	public Action getUploadReportAction() {
 		return uploadReportAction;
 	}
 
 	public void addActionToLockWhileLoading(Action a) {
 		if (actionToLockWhileLoading == null)
 			actionToLockWhileLoading = new ArrayList<Action>();
 		actionToLockWhileLoading.add(a);
 	}
 
 	public void lockActionsWhileLoading(final boolean b) {
 		if (actionToLockWhileLoading == null || actionToLockWhileLoading.isEmpty())
 			return;
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				for (Action a : actionToLockWhileLoading)
 					a.setEnabled(!b);
 			}
 		});
 	}
 
 	public static PrintParameters getPrintParameters() {
 		return printParam;
 	}
 
 	public static void setDefaultFontFamily(String s) {
 		defaultFontFamily = s;
 	}
 
 	public static String getDefaultFontFamily() {
 		return defaultFontFamily;
 	}
 
 	public static void setDefaultFontSize(int i) {
 		defaultFontSize = i;
 	}
 
 	public static int getDefaultFontSize() {
 		return defaultFontSize;
 	}
 
 	public static void setLinkColor(Color c) {
 		linkColor = c;
 	}
 
 	public static Color getLinkColor() {
 		return linkColor;
 	}
 
 	public static void setLinkUnderlined(boolean b) {
 		linkUnderline = b;
 	}
 
 	public static boolean isLinkUnderlined() {
 		return linkUnderline;
 	}
 
 	public static void setVisitedColor(Color c) {
 		visitedColor = c;
 	}
 
 	public static Color getVisitedColor() {
 		return visitedColor;
 	}
 
 	public void setRecentFilesMenu(JMenu menu) {
 		recentFilesMenu = menu;
 	}
 
 	public static void setFileChooser(FileChooser fc) {
 		fileChooser = fc;
 	}
 
 	public static FileChooser getFileChooser() {
 		return fileChooser;
 	}
 
 	public void setActionNotifier(ActionNotifier a) {
 		actionNotifier = a;
 	}
 
 	public ActionNotifier getActionNotifier() {
 		return actionNotifier;
 	}
 
 	public void setNavigator(Navigator n) {
 		navigator = n;
 		if (navigator == null)
 			return;
 		navigator.setNavigable(this);
 		// register key bindings
 		Action a = navigator.getAction(Navigator.BACK);
 		getInputMap().put((KeyStroke) a.getValue(Action.ACCELERATOR_KEY), Navigator.BACK);
 		getActionMap().put(Navigator.BACK, a);
 		a = navigator.getAction(Navigator.FORWARD);
 		getInputMap().put((KeyStroke) a.getValue(Action.ACCELERATOR_KEY), Navigator.FORWARD);
 		getActionMap().put(Navigator.FORWARD, a);
 		a = navigator.getAction(Navigator.HOME);
 		getInputMap().put((KeyStroke) a.getValue(Action.ACCELERATOR_KEY), Navigator.HOME);
 		getActionMap().put(Navigator.HOME, a);
 	}
 
 	public Navigator getNavigator() {
 		return navigator;
 	}
 
 	public void setBackgroundSound(String s) {
 		backgroundSound = s;
 	}
 
 	public String getBackgroundSound() {
 		return backgroundSound;
 	}
 
 	public void setLoopBackgroundSound(boolean b) {
 		loopBackgroundSound = b;
 	}
 
 	public boolean getLoopBackgroundSound() {
 		return loopBackgroundSound;
 	}
 
 	public JPopupMenu[] getPopupMenus() {
 		return new JPopupMenu[] { popupMenu, linkPopupMenu, imagePopupMenu, colorBarPopupMenu };
 	}
 
 	public void stopLoading() {
 		decoder.stop();
 	}
 
 	public boolean isReadingModel() {
 		synchronized (lock) {
 			return isReadingModel;
 		}
 	}
 
 	public void setReadingModel(boolean b) {
 		synchronized (lock) {
 			isReadingModel = b;
 		}
 	}
 
 	public boolean isReading() {
 		synchronized (lock) {
 			return isReading;
 		}
 	}
 
 	public void setReading(boolean b) {
 		synchronized (lock) {
 			isReading = b;
 		}
 	}
 
 	public boolean isWriting() {
 		synchronized (lock) {
 			return isWriting;
 		}
 	}
 
 	public void setWriting(boolean b) {
 		synchronized (lock) {
 			isWriting = b;
 		}
 	}
 
 	Map<Object, Object> getProperties() {
 		return properties;
 	}
 
 	public int getFontIncrement() {
 		return fontIncrement;
 	}
 
 	public void setFontIncrement(int i) {
 		fontIncrement = i;
 	}
 
 	public boolean isRemote() {
 		return FileUtilities.isRemote(pageAddress);
 	}
 
 	/**
 	 * Used when background image is intentionally changed, NOT changed due to loading.
 	 */
 	public void changeBackgroundImage(String str) {
 		if (isRemote())
 			throw new RuntimeException("Background image of a remote page cannot be changed.");
 		if (str == null) {
 			if (getBackgroundImage() != null)
 				setBackgroundImage(null);
 		}
 		else {
 			if (str.equals("null") || str.trim().length() == 0) {
 				if (getBackgroundImage() != null)
 					setBackgroundImage(null);
 			}
 			else {
 				ImageIcon icon = null;
 				if (!FileUtilities.isRemote(str)) {
 					icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(str));
 				}
 				else {
 					try {
 						icon = new ImageIcon(new URL(str));
 					}
 					catch (MalformedURLException mfe) {
 						mfe.printStackTrace();
 					}
 				}
 				if (icon != null) {
 					icon.setDescription(str);
 					setBackgroundImage(icon);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Used ONLY when background image is changed because of loading a new page. The editor is not notified content
 	 * change when this method is applied.
 	 */
 	public void setBackgroundImage(ImageIcon icon) {
 		if (icon == null) {
 			backgroundImage = null;
 			return;
 		}
 		backgroundImage = new ImageIcon(icon.getImage());
 		backgroundImage.setDescription(icon.getDescription());
 		iconWidth = backgroundImage.getIconWidth();
 		iconHeight = backgroundImage.getIconHeight();
 	}
 
 	public ImageIcon getBackgroundImage() {
 		return backgroundImage;
 	}
 
 	String getBackgroundImageName() {
 		if (backgroundImage == null)
 			return null;
 		return backgroundImage.getDescription();
 	}
 
 	public void setProgressBar(JProgressBar pb) {
 		decoder.setProgressBar(pb);
 		encoder.setProgressBar(pb);
 	}
 
 	public JProgressBar getProgressBar() {
 		return decoder.getProgressBar();
 	}
 
 	private ModelCanvas insertModel(String type) {
 		boolean reorder = false;
 		Map<Integer, Object> map = getEmbeddedComponent(ModelCanvas.class);
 		if (map != null && !map.isEmpty()) {
 			for (Integer key : map.keySet()) {
 				Object val = map.get(key);
 				if (val instanceof ModelCanvas) {
 					ModelCanvas canvas = (ModelCanvas) val;
 					if (canvas.getContainer().getRepresentationName().equals(type)) {
 						int caret = getCaretPosition();
 						if (caret <= key) {
 							componentPool.processInsertionOrRemoval(true, type);
 							reorder = true;
 						}
 					}
 				}
 			}
 		}
 		ModelCanvas mc = componentPool.request(type);
 		if (mc != null) {
 			mc.setUsed(true);
 			mc.addMenuBar();
 			mc.setResourceAddress("Inserted model");
 			MDModel model = mc.getContainer().getModel();
 			model.clear();
 			if (model.getModelListeners() != null)
 				model.getModelListeners().clear();
 			mc.getContainer().getView().setEnergizer(true);
 			if (model.getRecorderDisabled())
 				model.activateEmbeddedMovie(false);
 			if (mc.getContainer() instanceof AtomContainer) {
 				((AtomContainer) mc.getContainer()).enableDNAScroller(false);
 			}
 			mc.getContainer().enableRecorder(true);
 			mc.getContainer().addDefaultToolBar();
 			insertComponent(mc);
 			if (reorder)
 				nameModels();
 		}
 		else {
 			showContainerUsedUpMessage();
 		}
 		return mc;
 	}
 
 	private ModelCanvas insertProteinSynthesisModel() {
 		final ModelCanvas mc = componentPool.request(AtomContainer.getCompatibleName());
 		if (mc != null) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					boolean context = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(Page.this),
 							"Would you like to include a promoter and a terminator in the DNA string?", "DNA setting",
 							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
 					mc.setBackground(getBackground());
 					mc.setUsed(true);
 					mc.removeMenuBar();
 					mc.setResourceAddress("Inserted model");
 					MolecularModel model = (MolecularModel) mc.getContainer().getModel();
 					model.clear();
 					if (model.getModelListeners() != null)
 						model.getModelListeners().clear();
 					model.activateEmbeddedMovie(false);
 					mc.getContainer().removeToolbar();
 					((AtomContainer) mc.getContainer()).setDNAString("AAAAAAAAACCCCCCCCCGGGGGGGGGTTTTTTTTT");
 					((AtomContainer) mc.getContainer()).setDNAContextEnabled(context);
 					((AtomContainer) mc.getContainer()).enableDNAScroller(true);
 					((AtomContainer) mc.getContainer()).resetScrollerParameters();
 					model.activateHeatBath(true);
 					model.getHeatBath().setExpectedTemperature(300);
 					model.setSolvent(new Solvent(Solvent.WATER));
 					model.getView().setBackground(Solvent.WATER_COLOR);
 					((MDView) model.getView()).setColorCoding("Hydrophobicity");
 					((MDView) model.getView()).setShowClock(false);
 					((MDView) model.getView()).setRestraintStyle(StyleConstant.RESTRAINT_GHOST_STYLE);
 					insertComponent(mc);
 				}
 			});
 		}
 		else {
 			showContainerUsedUpMessage();
 		}
 		return mc;
 	}
 
 	private void showContainerUsedUpMessage() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Page.this),
 						"Sorry, you have used up the model containers. Only two\n"
 								+ "containers are allowed for this type of model on a single window.\n"
 								+ "if your purpose is to change a model on the page, you can\n"
 								+ "change the content of a container.", "Model Container",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 	}
 
 	private void importComponent(String type) {
 		if (type.equalsIgnoreCase("Activity Button")) {
 			ActivityButton ab = ActivityButton.create(this);
 			if (ab != null)
 				insertComponent(ab);
 		}
 		else if (type.equalsIgnoreCase("Audio Player")) {
 			AudioPlayer ap = AudioPlayer.create(this);
 			if (ap != null)
 				insertComponent(ap);
 		}
 		else if (type.equalsIgnoreCase("Applet")) {
 			PageApplet applet = PageApplet.create(this);
 			if (applet != null) {
 				insertComponent(applet);
 				applet.setIndex(applet.getIndex() + 1);
 			}
 		}
 		else if (type.equalsIgnoreCase("Plugin")) {
 			PageJContainer plugin = PageJContainer.create(this);
 			if (plugin != null) {
 				insertComponent(plugin);
 				plugin.setIndex(plugin.getIndex() + 1);
 			}
 		}
 		else if (type.equalsIgnoreCase("Feedback Area")) {
 			PageFeedbackArea fa = PageFeedbackArea.create(this);
 			if (fa != null)
 				insertComponent(fa);
 		}
 		else if (type.equalsIgnoreCase("Button")) {
 			PageButton pb = PageButton.create(this);
 			if (pb != null)
 				insertComponent(pb);
 		}
 		else if (type.equalsIgnoreCase("Spinner")) {
 			PageSpinner s = PageSpinner.create(this);
 			if (s != null)
 				insertComponent(s);
 		}
 		else if (type.equalsIgnoreCase("Check Box")) {
 			PageCheckBox cb = PageCheckBox.create(this);
 			if (cb != null)
 				insertComponent(cb);
 		}
 		else if (type.equalsIgnoreCase("A Group of Radio Buttons")) {
 			deselect();
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					int i = JOptionPane.showOptionDialog(JOptionPane.getFrameForComponent(Page.this),
 							"How many radio buttons would you like to create for this group?",
 							"Number of Radio Buttons", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
 							RADIO_BUTTON_OPTIONS, RADIO_BUTTON_OPTIONS[2]);
 					if (i == JOptionPane.CLOSED_OPTION || i == RADIO_BUTTON_OPTIONS.length - 1)
 						return;
 					long t = System.currentTimeMillis();
 					ButtonGroup bg = new ButtonGroup();
 					PageRadioButton rb;
 					i += 2;
 					for (int k = 0; k < i; k++) {
 						rb = PageRadioButton.create("Button " + (k + 1), Page.this);
 						rb.setGroupID(t);
 						rb.putClientProperty("button group", bg);
 						bg.add(rb);
 						insertComponent(rb);
 					}
 				}
 			});
 		}
 		else if (type.equalsIgnoreCase("Slider")) {
 			PageSlider s = PageSlider.create(this);
 			if (s != null)
 				insertComponent(s);
 		}
 		else if (type.equalsIgnoreCase("Combo Box")) {
 			PageComboBox cb = PageComboBox.create(this);
 			if (cb != null)
 				insertComponent(cb);
 		}
 		else if (type.equalsIgnoreCase("User Input Text Field")) {
 			PageTextField tf = PageTextField.create(this);
 			if (tf != null)
 				insertComponent(tf);
 		}
 		else if (type.equalsIgnoreCase("User Input Text Area")) {
 			PageTextArea ta = PageTextArea.create(this);
 			if (ta != null)
 				insertComponent(ta);
 		}
 		else if (type.equalsIgnoreCase("Database Search Text Field")) {
 			SearchTextField stf = SearchTextField.create(this);
 			if (stf != null)
 				insertComponent(stf);
 		}
 		else if (type.equalsIgnoreCase("Table")) {
 			PageTable table = PageTable.create(this);
 			if (table != null)
 				insertComponent(table);
 		}
 		else if (type.equalsIgnoreCase("Text Box")) {
 			PageTextBox tb = PageTextBox.create(this);
 			if (tb != null)
 				insertComponent(tb);
 		}
 		else if (type.equalsIgnoreCase("Multiple Choice")) {
 			PageMultipleChoice choice = PageMultipleChoice.create(this);
 			if (choice != null)
 				insertComponent(choice);
 		}
 		else if (type.equalsIgnoreCase("Image Question")) {
 			ImageQuestion iq = ImageQuestion.create(this);
 			if (iq != null)
 				insertComponent(iq);
 		}
 		else if (type.equalsIgnoreCase("Numeric Box")) {
 			PageNumericBox box = PageNumericBox.create(this);
 			if (box != null)
 				insertComponent(box);
 		}
 		else if (type.equalsIgnoreCase("Bar Graph")) {
 			PageBarGraph bar = PageBarGraph.create(this);
 			if (bar != null)
 				insertComponent(bar);
 		}
 		else if (type.equalsIgnoreCase("X-Y Graph")) {
 			PageXYGraph curve = PageXYGraph.create(this);
 			if (curve != null)
 				insertComponent(curve);
 		}
 		else if (type.equalsIgnoreCase("Bond-Breaking Barrier")) {
 			PagePotentialWell well = PagePotentialWell.create(this);
 			if (well != null)
 				insertComponent(well);
 		}
 		else if (type.equalsIgnoreCase("Activation Barrier")) {
 			PagePotentialHill hill = PagePotentialHill.create(this);
 			if (hill != null)
 				insertComponent(hill);
 		}
 		else if (type.equalsIgnoreCase("DNA Scroller")) {
 			PageDNAScroller scroller = PageDNAScroller.create(this);
 			if (scroller != null)
 				insertComponent(scroller);
 		}
 		else if (type.equalsIgnoreCase("Electronic Structure")) {
 			PageElectronicStructureViewer es = PageElectronicStructureViewer.create(this);
 			if (es != null)
 				insertComponent(es);
 		}
 		else if (type.equalsIgnoreCase("Diffraction Device")) {
 			PageDiffractionInstrument di = PageDiffractionInstrument.create(this);
 			if (di != null)
 				insertComponent(di);
 		}
 		else if (type.equalsIgnoreCase("Emission and Absorption Spectrometer")) {
 			PagePhotonSpectrometer ps = PagePhotonSpectrometer.create(this);
 			if (ps != null)
 				insertComponent(ps);
 		}
 		else if (type.equalsIgnoreCase(INSERT_JMOL)) {
 			PageMolecularViewer mv = PageMolecularViewer.create(this);
 			if (mv != null) {
 				insertComponent(mv);
 				mv.setIndex(mv.getIndex() + 1);
 			}
 		}
 		else if (type.equalsIgnoreCase("Script Console")) {
 			PageScriptConsole sc = PageScriptConsole.create(this);
 			if (sc != null)
 				insertComponent(sc);
 		}
 		else if (type.equalsIgnoreCase(INSERT_MW3D)) {
 			PageMd3d md = PageMd3d.create(this);
 			if (md != null) {
 				insertComponent(md);
 				md.setIndex(md.getIndex() + 1);
 			}
 		}
 		else if (type.equalsIgnoreCase("Periodic Table")) {
 			PagePeriodicTable pt = PagePeriodicTable.create(this);
 			if (pt != null)
 				insertComponent(pt);
 		}
 		else if (type.equalsIgnoreCase("Function Graph")) {
 			PageFunctionGraph g = PageFunctionGraph.create(this);
 			if (g != null)
 				insertComponent(g);
 		}
 	}
 
 	public void setURLDisplay(JLabel l) {
 		urlDisplay = l;
 	}
 
 	public JLabel getURLDisplay() {
 		return urlDisplay;
 	}
 
 	public URL getURL() throws MalformedURLException {
 		if (isRemote())
 			return new URL(pageAddress);
 		return new File(pageAddress).toURI().toURL();
 	}
 
 	public String getAddress() {
 		return pageAddress;
 	}
 
 	void setAddress(String s) {
 		pageAddress = s;
 		// prepare a URI for resolving relative URIs, etc.
 		if (isRemote()) {
 			try {
 				pageURI = new URI(FileUtilities.httpEncode(s));
 			}
 			catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			pageURI = null;
 		}
 	}
 
 	public void setTitle(String s) {
 		pageTitle = s;
 		Frame f = JOptionPane.getFrameForComponent(this);
 		if (f instanceof Modeler)
 			f.setTitle((pageTitle == null ? pageAddress : pageTitle) + " - " + softwareName + " V" + softwareVersion
 					+ (isRemote() ? "" : " (" + FileUtilities.getFileName(pageAddress) + ")"));
 	}
 
 	public String getTitle() {
 		return pageTitle;
 	}
 
 	public void setSaveReminder(SaveReminder s) {
 		saveReminder = s;
 	}
 
 	public SaveReminder getSaveReminder() {
 		return saveReminder;
 	}
 
 	public void setBackground(final Color color) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				Page.super.setBackground(color);
 			}
 		});
 		setCaretColor(new Color(0xffffff ^ color.getRGB()));
 	}
 
 	public Action[] getActions() {
 		Action[] a = new Action[actions.size()];
 		return actions.values().toArray(a);
 	}
 
 	public Action getAction(String s) {
 		return actions.get(s);
 	}
 
 	/** TODO: ComponentPool should really be redone. */
 	public void setComponentPool(ComponentPool a) {
 		componentPool = a;
 	}
 
 	/** TODO: ComponentPool should really be redone. */
 	public ComponentPool getComponentPool() {
 		return componentPool;
 	}
 
 	List<Object> getComponentsOfGroup(String type) {
 		List<Object> list = new ArrayList<Object>();
 		Object name = null, attr = null;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		Enumeration enum1 = section.children();
 		AbstractDocument.BranchElement paragraph = null;
 		Enumeration enum2 = null, enum3 = null;
 		AbstractDocument.LeafElement content = null;
 		while (enum1.hasMoreElements()) {
 			paragraph = (AbstractDocument.BranchElement) enum1.nextElement();
 			enum2 = paragraph.children();
 			while (enum2.hasMoreElements()) {
 				content = (AbstractDocument.LeafElement) enum2.nextElement();
 				enum3 = content.getAttributeNames();
 				while (enum3.hasMoreElements()) {
 					name = enum3.nextElement();
 					attr = content.getAttribute(name);
 					if (type.equals("Question")) {
 						if (attr instanceof PageTextField || attr instanceof PageTextArea
 								|| attr instanceof PageMultipleChoice || attr instanceof ImageQuestion) {
 							list.add(attr);
 						}
 						else if (attr instanceof PageTextBox) {
 							list.addAll(((PageTextBox) attr).getEmbeddedComponents(JTextComponent.class));
 						}
 					}
 					else if (type.equals("Model Container")) {
 						if (attr instanceof Engine)
 							list.add(attr);
 					}
 				}
 			}
 		}
 		return list;
 	}
 
 	/* check if the interval [i, j](i<=j) overlaps with [m, n](m<=n). */
 	private static boolean intervalOverlap(int m, int n, int i, int j) {
 		if (m == i && n == j)
 			return true;
 		if (i > m && i < n)
 			return true;
 		if (j > m && j < n)
 			return true;
 		if (m > i && m < j)
 			return true;
 		if (n > i && n < j)
 			return true;
 		return false;
 	}
 
 	/** return the leaf elements included in the selected text */
 	protected List<Element> getSelectedLeafElements() {
 		if (getSelectedText() == null)
 			return null;
 		List<Element> list = new ArrayList<Element>();
 		ElementIterator i = new ElementIterator(getDocument());
 		Element e = i.next();
 		int start = getSelectionStart();
 		int end = getSelectionEnd();
 		while (e != null) {
 			if (e instanceof AbstractDocument.LeafElement) {
 				if (intervalOverlap(e.getStartOffset(), e.getEndOffset(), start, end)) {
 					list.add(e);
 				}
 			}
 			e = i.next();
 		}
 		return list;
 	}
 
 	/**
 	 * override this method to provide cut/copy/paste with more <code>DataFlavor</code>. The original implemention of
 	 * this method with <code>JTextComponent</code> deals only with <code>DataFlavor.stringFlavor</code>.
 	 */
 	public void cut() {
 		if (!isEditable())
 			return;
 		if (getSelectedText() == null)
 			return;
 		StyledTextSelection contents = new StyledTextSelection();
 		contents.clearText();
 		int start = getSelectionStart();
 		int end = getSelectionEnd();
 		List<Element> leaves = getSelectedLeafElements();
 		Document d = getDocument();
 		if (leaves != null && !leaves.isEmpty()) {
 			int i, j;
 			String str = null;
 			for (Element e : leaves) {
 				i = Math.max(e.getStartOffset(), start);
 				j = Math.min(e.getEndOffset(), end);
 				try {
 					str = d.getText(i, j - i);
 					contents.insertString(i - start, str, e.getAttributes());
 				}
 				catch (BadLocationException ble) {
 					ble.printStackTrace();
 				}
 			}
 		}
 		clipboard.setContents(contents, this);
 		clipboardText = getSelectedText();
 		super.cut();
 	}
 
 	/**
 	 * override this method to provide cut/copy/paste with more <code>DataFlavor</code>. The original implemention of
 	 * this method with <code>JTextComponent</code> deals only with <code>DataFlavor.stringFlavor</code>.
 	 */
 	public void copy() {
 		if (getSelectedText() == null)
 			return;
 		StyledTextSelection contents = new StyledTextSelection();
 		contents.clearText();
 		int start = getSelectionStart();
 		int end = getSelectionEnd();
 		List<Element> leaves = getSelectedLeafElements();
 		Document d = getDocument();
 		if (leaves != null && !leaves.isEmpty()) {
 			int i, j;
 			String str = null;
 			for (Element e : leaves) {
 				i = Math.max(e.getStartOffset(), start);
 				j = Math.min(e.getEndOffset(), end);
 				try {
 					str = d.getText(i, j - i);
 					contents.insertString(i - start, str, e.getAttributes());
 				}
 				catch (BadLocationException ble) {
 					ble.printStackTrace();
 				}
 			}
 		}
 		clipboard.setContents(contents, this);
 		clipboardText = getSelectedText();
 		super.copy();
 	}
 
 	private static String getSystemClipboardText() {
 		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
 		try {
 			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
 				String text = (String) t.getTransferData(DataFlavor.stringFlavor);
 				return text;
 			}
 		}
 		catch (UnsupportedFlavorException e) {
 		}
 		catch (IOException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * override this method to provide cut/copy/paste with more <code>DataFlavor</code>. The original implemention of
 	 * this method with <code>JTextComponent</code> deals only with <code>DataFlavor.stringFlavor</code>. FIXME:
 	 * Pasting a component to a different page will fail, because the content of the component is destroyed when leaving
 	 * a page (in order to prevent memory leak). As a result, the content cannot be copied.
 	 */
 	public void paste() {
 		if (!isEditable())
 			return;
 		super.paste();
 		if (clipboardText == null)
 			return;
 		if (!clipboardText.equals(getSystemClipboardText()))
 			return; // this means that the text is from an external application
 		unhighlightEmbedded();
 		Transferable contents = clipboard.getContents(this);
 		if (contents != null && contents.isDataFlavorSupported(StyledTextSelection.styledTextFlavor)) {
 			StyledDocument s = null;
 			try {
 				s = (StyledDocument) contents.getTransferData(StyledTextSelection.styledTextFlavor);
 			}
 			catch (UnsupportedFlavorException e) {
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			if (s != null) {
 				int pos = getCaretPosition() - s.getLength();
 				ElementIterator i = new ElementIterator(s);
 				Element e = i.next();
 				while (e != null) {
 					if (e instanceof AbstractDocument.LeafElement) {
 						int m = e.getStartOffset();
 						if (m >= s.getLength())
 							break;
 						int n = e.getEndOffset();
 						AttributeSet a = e.getAttributes();
 						Object name, attr;
 						Component comp = null;
 						Icon icon = null;
 						for (Enumeration enum1 = a.getAttributeNames(); enum1.hasMoreElements();) {
 							name = enum1.nextElement();
 							attr = a.getAttribute(name);
 							if (attr instanceof Icon) {
 								icon = (Icon) attr;
 							}
 							else if (attr instanceof Component) {
 								comp = (Component) attr;
 							}
 						}
 						if (icon != null) {
 							select(pos + m, pos + m + 1);
 							insertDuplicatedIcon(icon);
 							setCaretPosition(pos + s.getLength());
 							// set the character attributes in case this image is hyperlinked
 							getStyledDocument().setCharacterAttributes(pos + m, n - m, a, true);
 						}
 						else if (comp != null) {
 							select(pos + m, pos + m + 1);
 							pasteComponent(comp);
 							setCaretPosition(pos + s.getLength());
 						}
 						else {
 							getStyledDocument().setCharacterAttributes(pos + m, n - m, a, true);
 						}
 					}
 					e = i.next();
 				}
 			}
 		}
 	}
 
 	/* insert a duplicate of the specified component */
 	private void pasteComponent(final Component c) {
 		if (c == null)
 			return;
 		if (c instanceof Engine) {
 			if (c instanceof ModelCanvas) {
 				if (((ModelCanvas) c).getPage() == this) {
 					if (!((ModelCanvas) c).isUsed()) {
 						insertComponent(c);
 					}
 				}
 				else {
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Page.this),
 									"Sorry, currently a model container cannot be transfered to a new window.",
 									"Model Container", JOptionPane.INFORMATION_MESSAGE);
 						}
 					});
 				}
 			}
 			else {
 				if (c instanceof PageMolecularViewer) {
 					new SwingWorker() {
 						public Object construct() {
 							PageMolecularViewer mv = ((PageMolecularViewer) c).getCopy();
 							mv.loadCurrentResource();
 							return mv;
 						}
 
 						public void finished() {
 							final PageMolecularViewer mv = (PageMolecularViewer) get();
 							insertComponent(mv);
 							EventQueue.invokeLater(new Runnable() {
 								public void run() {
 									mv.loadCurrentResource(); // FIXME:why need to call this twice?
 								}
 							});
 						}
 					}.start();
 				}
 			}
 		}
 		else {
 			if (c instanceof AudioPlayer) {
 				insertComponent(new AudioPlayer((AudioPlayer) c, this));
 			}
 			if (c instanceof PageApplet) {
 				insertComponent(new PageApplet((PageApplet) c, this));
 			}
 			if (c instanceof PageJContainer) {
 				insertComponent(new PageJContainer((PageJContainer) c, this));
 			}
 			else if (c instanceof PageButton) {
 				insertComponent(new PageButton((PageButton) c, this));
 			}
 			else if (c instanceof PageCheckBox) {
 				insertComponent(new PageCheckBox((PageCheckBox) c, this));
 			}
 			else if (c instanceof PageRadioButton) {
 				insertComponent(new PageRadioButton((PageRadioButton) c, this));
 			}
 			else if (c instanceof PageComboBox) {
 				insertComponent(new PageComboBox((PageComboBox) c, this));
 			}
 			else if (c instanceof PageSpinner) {
 				insertComponent(new PageSpinner((PageSpinner) c, this));
 			}
 			else if (c instanceof PageSlider) {
 				insertComponent(new PageSlider((PageSlider) c, this));
 			}
 			else if (c instanceof PageDNAScroller) {
 				insertComponent(new PageDNAScroller((PageDNAScroller) c, this));
 			}
 			else if (c instanceof PagePotentialWell) {
 				insertComponent(new PagePotentialWell((PagePotentialWell) c, this));
 			}
 			else if (c instanceof PagePotentialHill) {
 				insertComponent(new PagePotentialHill((PagePotentialHill) c, this));
 			}
 			else if (c instanceof PageNumericBox) {
 				insertComponent(new PageNumericBox((PageNumericBox) c, this));
 			}
 			else if (c instanceof PageBarGraph) {
 				insertComponent(new PageBarGraph((PageBarGraph) c, this));
 			}
 			else if (c instanceof PageXYGraph) {
 				insertComponent(new PageXYGraph((PageXYGraph) c, this));
 			}
 			else if (c instanceof PageElectronicStructureViewer) {
 				insertComponent(new PageElectronicStructureViewer((PageElectronicStructureViewer) c, this));
 			}
 			else if (c instanceof PageDiffractionInstrument) {
 				insertComponent(new PageDiffractionInstrument((PageDiffractionInstrument) c, this));
 			}
 			else if (c instanceof PagePhotonSpectrometer) {
 				insertComponent(new PagePhotonSpectrometer((PagePhotonSpectrometer) c, this));
 			}
 			else if (c instanceof PageTable) {
 				insertComponent(new PageTable((PageTable) c, this));
 			}
 			else if (c instanceof PageTextField) {
 				insertComponent(new PageTextField((PageTextField) c, this));
 			}
 			else if (c instanceof PageTextArea) {
 				insertComponent(new PageTextArea((PageTextArea) c, this));
 			}
 			else if (c instanceof PageTextBox) {
 				insertComponent(new PageTextBox((PageTextBox) c, this));
 			}
 			else if (c instanceof PageMultipleChoice) {
 				insertComponent(new PageMultipleChoice((PageMultipleChoice) c, this));
 			}
 			else if (c instanceof ImageQuestion) {
 				insertComponent(new ImageQuestion((ImageQuestion) c, this));
 			}
 			else if (c instanceof SearchTextField) {
 				insertComponent(new SearchTextField((SearchTextField) c, this));
 			}
 			else if (c instanceof ActivityButton) {
 				insertComponent(new ActivityButton((ActivityButton) c, this));
 			}
 			else if (c instanceof PageFeedbackArea) {
 				insertComponent(new PageFeedbackArea((PageFeedbackArea) c, this));
 			}
 			else if (c instanceof PageScriptConsole) {
 				insertComponent(new PageScriptConsole((PageScriptConsole) c, this));
 			}
 			else if (c instanceof PagePeriodicTable) {
 				insertComponent(new PagePeriodicTable((PagePeriodicTable) c, this));
 			}
 			else if (c instanceof PageFunctionGraph) {
 				insertComponent(new PageFunctionGraph((PageFunctionGraph) c, this));
 			}
 			else if (c instanceof IconWrapper) {
 				insertComponent(IconWrapper.newInstance(((IconWrapper) c).getIcon(), this));
 			}
 		}
 	}
 
 	/* insert a duplicate of the specified icon */
 	private void insertDuplicatedIcon(final Icon icon) {
 		if (icon == null)
 			return;
 		if (icon instanceof ImageIcon) {
 			ImageIcon i2 = new ImageIcon(((ImageIcon) icon).getImage());
 			i2.setDescription(((ImageIcon) icon).getDescription());
 			insertIcon(i2);
 		}
 		else if (icon instanceof LineIcon) {
 			insertIcon(new LineIcon((LineIcon) icon));
 		}
 		else {
 			/*
 			 * CAUTION!! This code works but it won't get saved because of the lack of information. If reaching this,
 			 * there must be an unsupported type of icon
 			 */
 			insertIcon(new Icon() {
 				public int getIconWidth() {
 					return icon.getIconWidth();
 				}
 
 				public int getIconHeight() {
 					return icon.getIconHeight();
 				}
 
 				public void paintIcon(Component c, Graphics g, int x, int y) {
 					icon.paintIcon(c, g, x, y);
 				}
 
 				public String toString() {
 					return icon.toString();
 				}
 			});
 		}
 	}
 
 	/**
 	 * remove the image object from the page.
 	 * 
 	 * @return true if removal is successful
 	 */
 	protected boolean removeImage(Icon image) {
 		if (getDocument() == null)
 			return false;
 		if (getDocument().getLength() <= 0)
 			return false;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph = null;
 		AbstractDocument.LeafElement content = null;
 		Object name = null, attr = null;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					name = k.nextElement();
 					attr = content.getAttribute(name);
 					if ((attr instanceof Icon && attr == image)
 							|| (attr instanceof IconWrapper && ((IconWrapper) attr).getIcon() == image)) {
 						select(content.getStartOffset(), content.getEndOffset());
 						cut();
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/** copy the image to the clipboard. */
 	public void copyImage(Icon icon) {
 		if (getDocument() == null)
 			return;
 		if (getDocument().getLength() <= 0)
 			return;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph = null;
 		AbstractDocument.LeafElement content = null;
 		Object name = null, attr = null;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					name = k.nextElement();
 					attr = content.getAttribute(name);
 					if ((attr instanceof Icon && attr == icon)
 							|| (attr instanceof IconWrapper && ((IconWrapper) attr).getIcon() == icon)) {
 						select(content.getStartOffset(), content.getEndOffset());
 						copy();
 						if (content.getEndOffset() < getDocument().getLength() - 1)
 							setCaretPosition(content.getEndOffset() + 1);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/** insert a line break programatically */
 	public void insertLineBreak() {
 		try {
 			getDocument().insertString(getCaretPosition(), "\n", null);
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void insertString(String str) {
 		insertString(str, null);
 	}
 
 	public void insertString(String str, Style style) {
 		try {
 			getDocument().insertString(getCaretPosition(), str, style);
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void insertString(String str, Font font, Color color) {
 		Style style = addStyle(null, null);
 		switch (font.getStyle()) {
 		case Font.BOLD:
 			StyleConstants.setBold(style, true);
 			break;
 		case Font.ITALIC:
 			StyleConstants.setItalic(style, true);
 			break;
 		case Font.BOLD | Font.ITALIC:
 			StyleConstants.setBold(style, true);
 			StyleConstants.setItalic(style, true);
 			break;
 		}
 		StyleConstants.setFontSize(style, font.getSize());
 		StyleConstants.setForeground(style, color);
 		StyleConstants.setFontFamily(style, font.getFamily());
 		try {
 			getDocument().insertString(getCaretPosition(), str, style);
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * import all the snapshots currently stored in the snapshot gallery.
 	 */
 	public void importSnapshots() {
 		new SnapshotGalleryFormatter(this).format();
 	}
 
 	/**
 	 * remove the component from the page.
 	 * 
 	 * @return true if removal is successful
 	 */
 	public boolean removeComponent(Component c) {
 		if (getDocument() == null)
 			return false;
 		if (getDocument().getLength() <= 0)
 			return false;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph = null;
 		AbstractDocument.LeafElement content = null;
 		Object name = null, attr = null;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					name = k.nextElement();
 					attr = content.getAttribute(name);
 					if (attr == c) {
 						select(content.getStartOffset(), content.getEndOffset());
 						cut();
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/** copy the component to the clipboard. */
 	public void copyComponent(Component c) {
 		if (getDocument() == null)
 			return;
 		if (getDocument().getLength() <= 0)
 			return;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph = null;
 		AbstractDocument.LeafElement content = null;
 		Object name = null, attr = null;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					name = k.nextElement();
 					attr = content.getAttribute(name);
 					if (attr == c) {
 						select(content.getStartOffset(), content.getEndOffset());
 						copy();
 						if (content.getEndOffset() < getDocument().getLength() - 1)
 							setCaretPosition(content.getEndOffset() + 1);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/** get the position (end offset) of the embedded component. */
 	public int getPosition(Component c) {
 		if (c == null)
 			return -1;
 		if (getDocument() == null)
 			return -1;
 		if (getDocument().getLength() <= 0)
 			return -1;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph = null;
 		AbstractDocument.LeafElement content = null;
 		Object attr = null;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					attr = content.getAttribute(k.nextElement());
 					if (c == attr)
 						return content.getEndOffset();
 				}
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * store the embedded components, which is of the specified class, in a <code>TreeMap</code>, with the start
 	 * offset (<code>Integer</code>) as the key
 	 */
 	public Map<Integer, Object> getEmbeddedComponent(Class c) {
 		if (c == null)
 			return null;
 		if (getDocument() == null)
 			return null;
 		if (getDocument().getLength() <= 0)
 			return null;
 		Map<Integer, Object> map = Collections.synchronizedMap(new TreeMap<Integer, Object>());
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph = null;
 		AbstractDocument.LeafElement content = null;
 		Object name = null, attr = null;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					name = k.nextElement();
 					attr = content.getAttribute(name);
 					if (c.isInstance(attr))
 						map.put(content.getStartOffset(), attr);
 				}
 			}
 		}
 		return map;
 	}
 
 	/** return the n-th embedded object of the specified class. */
 	public Object getEmbeddedComponent(Class c, int n) {
 		if (c == null)
 			return null;
 		if (getDocument() == null)
 			return null;
 		if (getDocument().getLength() <= 0)
 			return null;
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		AbstractDocument.BranchElement paragraph;
 		AbstractDocument.LeafElement content;
 		Object name, attr;
 		int index = 0;
 		for (Enumeration i = section.children(); i.hasMoreElements();) {
 			paragraph = (AbstractDocument.BranchElement) i.nextElement();
 			for (Enumeration j = paragraph.children(); j.hasMoreElements();) {
 				content = (AbstractDocument.LeafElement) j.nextElement();
 				for (Enumeration k = content.getAttributeNames(); k.hasMoreElements();) {
 					name = k.nextElement();
 					attr = content.getAttribute(name);
 					if (c.isInstance(attr)) {
 						if (index == n)
 							return attr;
 						index++;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	protected List<Embeddable> getEmbeddedComponents() {
 		if (getDocument() == null)
 			return null;
 		if (getDocument().getLength() <= 0)
 			return null;
 		List<Embeddable> list = Collections.synchronizedList(new ArrayList<Embeddable>());
 		AbstractDocument.BranchElement section = (AbstractDocument.BranchElement) getDocument().getDefaultRootElement();
 		Object name = null, attr = null;
 		Enumeration enum1 = section.children();
 		AbstractDocument.BranchElement paragraph = null;
 		Enumeration enum2 = null, enum3 = null;
 		AbstractDocument.LeafElement content = null;
 		while (enum1.hasMoreElements()) {
 			paragraph = (AbstractDocument.BranchElement) enum1.nextElement();
 			enum2 = paragraph.children();
 			while (enum2.hasMoreElements()) {
 				content = (AbstractDocument.LeafElement) enum2.nextElement();
 				enum3 = content.getAttributeNames();
 				while (enum3.hasMoreElements()) {
 					name = enum3.nextElement();
 					attr = content.getAttribute(name);
 					if (attr instanceof Embeddable) {
 						list.add((Embeddable) attr);
 					}
 				}
 			}
 		}
 		return list;
 	}
 
 	private void createActionTable() {
 		if (actions == null) {
 			actions = new HashMap<String, Action>();
 		}
 		else {
 			actions.clear();
 		}
 		for (Action a : augmentActions())
 			actions.put((String) a.getValue(Action.NAME), a);
 	}
 
 	private Action[] augmentActions() {
 		Action[] defaultActions = { new UndoAction(), new RedoAction(), refreshAction, saveAction, saveAsAction,
 				htmlizeAction, openAction, newAction, insertFileAction, increaseIndentAction, decreaseIndentAction,
 				increaseFontSizeAction, decreaseFontSizeAction, bulletAction, printPreviewAction, pageSetupAction,
 				printAction, propertiesAction, symbolAction, hyperlinkAction, imageReader, colorBarAction,
 				insertAtomContainerAction, insertGBContainerAction, insertChemContainerAction,
 				insertProsynContainerAction, insertComponentAction, fontAction, paragraphAction, insertBulletAction };
 		Action[] old = super.getActions();
 		/* change the select all action and its key binding */
 		for (Action a : old) {
 			if (DefaultEditorKit.selectAllAction.equals(a.getValue(Action.NAME))) {
 				ActionMap am = getActionMap();
 				Object[] keys = am.allKeys();
 				for (int k = 0; k < keys.length; k++) {
 					if (am.get(keys[k]) == a) {
 						am.put(keys[k], selectAllAction);
 					}
 				}
 				a = selectAllAction;
 				break;
 			}
 		}
 		return TextAction.augmentList(old, defaultActions);
 	}
 
 	public void setSelection(int begin, int end, boolean moveUp) {
 		if (moveUp) {
 			setCaretPosition(end);
 			moveCaretPosition(begin);
 		}
 		else {
 			select(begin, end);
 		}
 		highlight(getSelectionStart(), getSelectionEnd());
 	}
 
 	void playSound(String clipName) {
 		stopSound();
 		File f = null;
 		if (isRemote()) {
 			URL u = null;
 			try {
 				u = new URL(FileUtilities.getCodeBase(pageAddress) + clipName);
 			}
 			catch (MalformedURLException e) {
 				e.printStackTrace();
 				return;
 			}
 			try {
 				f = ConnectionManager.sharedInstance().shouldUpdate(u);
 				if (f == null)
 					f = ConnectionManager.sharedInstance().cache(u);
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 		else {
 			f = new File(FileUtilities.getCodeBase(pageAddress), clipName);
 		}
 		try {
 			if (clipName.toLowerCase().endsWith(".mid")) {
 				if (midiPlayer == null)
 					midiPlayer = new MidiPlayer();
 				midiPlayer.setLoopCount(loopBackgroundSound ? -1 : 0);
 				midiPlayer.play(f);
 			}
 			else {
 				if (sampledAudioPlayer == null)
 					sampledAudioPlayer = new SampledAudioPlayer();
 				sampledAudioPlayer.setLoopCount(loopBackgroundSound ? Clip.LOOP_CONTINUOUSLY : 0);
 				sampledAudioPlayer.play(f);
 			}
 		}
 		catch (Throwable t) { // in case the players have unexpected errors
 			t.printStackTrace();
 		}
 	}
 
 	void stopSound() {
 		try {
 			if (midiPlayer != null)
 				midiPlayer.stop();
 			if (sampledAudioPlayer != null)
 				sampledAudioPlayer.stop();
 		}
 		catch (Throwable t) { // in case the players have unexpected errors
 			t.printStackTrace();
 		}
 	}
 
 	/*
 	 * Hack to stop embedded animated GIFs. Note that animated GIFs embedded in a TextBox are automatically stopped when
 	 * the text box is destroyed. So there is no need to call this method from within it.
 	 */
 	private void stopImageAnimators() {
 		synchronized (lock) {
 			if (embeddedImageFound) {
 				Thread[] list = new Thread[Thread.activeCount()];
 				Thread.enumerate(list);
 				for (Thread t : list) {
 					if (t.getName().startsWith("Image Animator")) {
 						t.interrupt();
 					}
 				}
 				embeddedImageFound = false;
 			}
 		}
 	}
 
 	void setEmbeddedImageFound(boolean b) {
 		embeddedImageFound = b;
 	}
 
 	boolean isEmbeddedImageFound() {
 		return embeddedImageFound;
 	}
 
 	/**
 	 * this method clears the current content. It also removes listeners and release dependencies of embedded components
 	 * on live objects to prevent memory leak.
 	 */
 	protected void clearAllContent() {
 		if (getDocument().getLength() <= 0)
 			return;
 		stopSound();
 		stopImageAnimators();
 		backgroundSound = null;
 		loopBackgroundSound = false;
 		if (componentPool != null)
 			componentPool.resetStatus();
 		InstancePool.sharedInstance().reset();
 		List<Embeddable> list = getEmbeddedComponents();
 		if (list != null) {
 			for (Embeddable embed : list) {
 				if (embed instanceof Engine) {
 					InstancePool.sharedInstance().setStatus(embed, false);
 				}
 				else {
 					embed.destroy();
 				}
 			}
 		}
 		resetToDefaultParagraphAttributes(0);
 		getDocument().removeDocumentListener(editResponder);
 		try {
 			getDocument().remove(0, getDocument().getLength());
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		getDocument().addDocumentListener(editResponder);
 		setFillMode(FillMode.getNoFillMode());
 		properties.clear();
 		resetUndoManager();
 		removeHighlights();
 		unhighlightEmbedded();
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				if (decoder.getProgressBar() != null) {
 					decoder.getProgressBar().setIndeterminate(false);
 					decoder.getProgressBar().setValue(0);
 				}
 				if (encoder.getProgressBar() != null) {
 					encoder.getProgressBar().setIndeterminate(false);
 					encoder.getProgressBar().setValue(0);
 				}
 			}
 		});
 		reload();
 	}
 
 	/** Creates highlights around all occurrences of pattern */
 	public void highlight(String pattern) {
 		/* First remove all old highlights */
 		removeHighlights();
 		try {
 			Highlighter hilite = getHighlighter();
 			Document doc = getDocument();
 			String text = doc.getText(0, doc.getLength());
 			int pos = 0;
 			while ((pos = text.indexOf(pattern, pos)) >= 0) {
 				hilite.addHighlight(pos, pos + pattern.length(), myHighlightPainter);
 				pos += pattern.length();
 			}
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/** Creates highlights around a selected segment */
 	public void highlight(int start, int end) {
 		removeHighlights();
 		try {
 			getHighlighter().addHighlight(start, end, myHighlightPainter);
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/** Removes only our private highlights */
 	public void removeHighlights() {
 		Highlighter hilite = getHighlighter();
 		Highlighter.Highlight[] hilites = hilite.getHighlights();
 		for (int i = 0; i < hilites.length; i++) {
 			if (hilites[i].getPainter() instanceof MyHighlightPainter) {
 				hilite.removeHighlight(hilites[i]);
 			}
 		}
 	}
 
 	/**
 	 * This method is used to re-layout components on this page. It should be called if the user imports a model that
 	 * has a different size than that of the current container. In this case, calling this method will result in an
 	 * automatical expansion or collapsing of the model components in the textual environment.
 	 */
 	public void reload() {
 		// notifyPageListeners(new PageEvent(this, PageEvent.STORE_VIEW_POSITION));
 		if (isEditable())
 			oldCaretPosition = getCaretPosition();
 		StyledDocument old = getStyledDocument();
 		if (styledDocumentInstance == null)
 			styledDocumentInstance = new DefaultStyledDocument();
 		setDocument(styledDocumentInstance);// FIXME: why must we do this?
 		setDocument(old);
 		if (isEditable())
 			setCaretPosition(oldCaretPosition);
 		// notifyPageListeners(new PageEvent(this, PageEvent.RESTORE_VIEW_POSITION));
 	}
 
 	/**
 	 * <code>clearAllContent()</code> MUST be called in prior to this method to empty the document tree first. It
 	 * cannot be put into the same thread, for it may cause deadlock.
 	 */
 	private void readPage(final String uri) {
 		synchronized (lock) {
 			// this disables the SaveReminder when the file is in the "/cache/tmpzip/" zone
 			if (!isRemote()) {
 				if (runOnCD) {
 					saveReminder.setEnabled(false);
 				}
 				else {
 					saveReminder.setEnabled(uri.indexOf(TMPZIP) == -1);
 				}
 			}
 			else {
 				if (runOnCD) {
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							showErrorMessage(new CdModeException(uri));
 						}
 					});
 					return;
 				}
 			}
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					if (urlDisplay != null)
 						urlDisplay.setText("Loading...");
 					if (decoder.getProgressBar() != null) {
 						decoder.getProgressBar().setString(null);
 						decoder.getProgressBar().setIndeterminate(true);
 					}
 				}
 			});
 			try {
 				loadPage(uri);
 			}
 			catch (final Exception e) {
 				e.printStackTrace();
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						showErrorMessage(e);
 					}
 				});
 			}
 			finally {
 				saveReminder.setChanged(false);
 			}
 		}
 	}
 
 	private void showErrorMessage(Exception e) {
 		ErrorMessage m = ErrorMessageFactory.createErrorMessage(e, this);
 		clearAllContent();
 		if (m != null) {
 			StyledDocument doc = getStyledDocument();
 			for (int i = 0, n = m.getStrings().length; i < n; i++) {
 				try {
 					doc.insertString(doc.getLength(), m.getStrings()[i], m.getStyles()[i]);
 				}
 				catch (BadLocationException ble) {
 					ble.printStackTrace();
 				}
 				SimpleAttributeSet sas = new SimpleAttributeSet();
 				sas.addAttribute(StyleConstants.Alignment, new Integer(StyleConstants.ALIGN_LEFT));
 				doc.setParagraphAttributes(doc.getLength() - 1, 1, sas, false);
 			}
 		}
 		else {
 			StackTraceElement[] ste = e.getStackTrace();
 			StringBuffer sb = new StringBuffer("Error message:\n\n");
 			for (int i = 0; i < ste.length; i++)
 				sb.append(ste[i] + "\n");
 			setText(sb.toString());
 		}
 		if (navigator != null)
 			navigator.reportDeadLink(pageAddress);
 		notifyPageListeners(new PageEvent(this, PageEvent.LOAD_ERROR, "loading error"));
 	}
 
 	private boolean loadPage(final String uri) throws IOException, SAXException, UnsupportedFormatException {
 
 		Debugger.print("Start loading page: " + uri);
 
 		deselect();
 		if (uri == null)
 			return false;
 
 		final String uriLC = uri.toLowerCase();
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			}
 		});
 
 		String queryString = null;
 		if (FileUtilities.isRemote(uri))
 			queryString = new URL(uri).getQuery();
 
 		if (queryString != null) {
 
 			// load results from the search engine
 			if (uriLC.indexOf("/cgi-bin/htsearch?") != -1) {
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						renderHTMLContent(uri.replaceAll(";", "&"), false);
 					}
 				});
 			}
 			else {
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						renderHTMLContent(uri, true);
 					}
 				});
 			}
 
 		}
 
 		// load a JSP page
 		else if (uriLC.endsWith(".jsp")) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					renderHTMLContent(uri, true);
 				}
 			});
 		}
 
 		// load a CML page
 		else if (uriLC.endsWith(".cml")) {
 			boolean loadSuccess = false;
 			if (FileUtilities.isRemote(uri) && uriLC.endsWith(".cml")) {
 				loadSuccess = decoder.read(FileUtilities.httpEncode(uri));
 			}
 			else {
 				loadSuccess = decoder.read(uri);
 			}
 			if (loadSuccess) {
 				/* Critically important!!!! */
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						StyledDocument doc = decoder.getDocument();
 						if (doc == null)
 							return;
 						// release dependency of document on listeners for it to be garbage-collected
 						getDocument().removeUndoableEditListener(undoHandler);
 						getDocument().removeDocumentListener(editResponder);
 						doc.addUndoableEditListener(undoHandler);
 						doc.addDocumentListener(editResponder);
 						setStyledDocument(doc);
 					}
 				});
 			}
 			else {
 				System.err.println("XML decoder failed......");
 			}
 		}
 
 		// load a plain text file or a script source file
 		else if (uriLC.endsWith(".txt") || uriLC.endsWith(".mws")) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					try {
 						readPlainTextFile(uri);
 					}
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 
 		// load an image file
 		else if (uriLC.endsWith(".gif") || uriLC.endsWith(".png") || uriLC.endsWith(".jpg") || uriLC.endsWith(".jpeg")) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					readImageFile(uri);
 				}
 			});
 		}
 
 		// load a molecule with Jmol
 		else if (uriLC.endsWith(".pdb") || uriLC.endsWith(".xyz")) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					loadMoleculeFile(uri);
 				}
 			});
 		}
 
 		// load a plain molecular dynamics model
 		else if (uriLC.endsWith(".gbl") || uriLC.endsWith(".mml")) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					loadMDModel(uri);
 				}
 			});
 		}
 
 		else if (uriLC.endsWith(".jnlp")) {
 			ExternalClient.open(ExternalClient.JNLP_CLIENT, uri);
 		}
 
 		else if (uriLC.endsWith(".html") || uriLC.endsWith(".htm")) {
 			ExternalClient.open(ExternalClient.HTML_CLIENT, uri);
 		}
 
 		// unrecognized format
 		else {
 			if (uri.endsWith("/") || uri.endsWith("\\")) {
 				loadPage(uri + "index.cml");
 				return true;
 			}
 			if (FileUtilities.isRemote(uri)) {
 				ExternalClient.open(ExternalClient.HTML_CLIENT, uri);
 				return true;
 			}
 			throw new UnsupportedFormatException(uri);
 		}
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			}
 		});
 
 		Debugger.print("Finish loading page");
 
 		return true;
 
 	}
 
 	private boolean loadMDModel(final String uri) {
 		if (uri == null)
 			return false;
 		final ModelCanvas mc = insertModel(uri.toLowerCase().endsWith("gbl") ? GBContainer.getCompatibleName()
 				: ChemContainer.getCompatibleName());
 		if (mc == null)
 			return false;
 		final boolean b = isEditable();
 		if (!b)
 			setEditable(true);
 		notifyPageListeners(new PageEvent(this, PageEvent.PAGE_READ_BEGIN));
 		new SwingWorker() {
 			public Object construct() {
 				Model model = mc.getContainer().getModel();
 				mc.setResourceAddress(uri);
 				mc.getContainer().setLoading(true);
 				if (!FileUtilities.isRemote(uri)) {
 					model.input(new File(uri));
 				}
 				else {
 					URL u = null;
 					try {
 						u = new URL(FileUtilities.httpEncode(uri));
 					}
 					catch (MalformedURLException mue) {
 						mue.printStackTrace();
 					}
 					if (u != null)
 						model.input(u);
 				}
 				return model;
 			}
 
 			public void finished() {
 				mc.getContainer().setLoading(false);
 				if (!b)
 					setEditable(false);
 				setTitle(uri);
 				notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_READ_END));
 			}
 		}.start();
 		return true;
 	}
 
 	private boolean loadMoleculeFile(final String uri) {
 		if (uri == null)
 			return false;
 		final PageMolecularViewer mv = (PageMolecularViewer) InstancePool.sharedInstance().getUnusedInstance(
 				PageMolecularViewer.class);
 		if (mv == null)
 			return false;
 		notifyPageListeners(new PageEvent(this, PageEvent.PAGE_READ_BEGIN));
 		setBackground(Color.black);
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				final boolean b = isEditable();
 				if (!b)
 					setEditable(true);
 				insertComponent(mv);
 				mv.setPage(Page.this);
 				mv.reset();
 				mv.enableMenuBar(false);
 				mv.enableToolBar(false);
 				mv.enableBottomBar(false);
 				mv.setBorder(null);
 				mv.setPreferredSize(new Dimension(getWidth() - 2, getHeight() - 2));
 				mv.setResourceAddress(uri);
 				mv.setCustomInitializationScript("reset;set frank on;");
 				Thread t = new Thread("Load molecule (full-page mode)") {
 					public void run() {
 						mv.loadCurrentResource();
 					}
 				};
 				t.setPriority(LOADER_THREAD_PRIORITY);
 				t.setUncaughtExceptionHandler(new DisasterHandler(DisasterHandler.LOAD_ERROR, null, null, Page.this));
 				t.start();
 				if (!b)
 					setEditable(false);
 				setTitle(uri);
 				notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_READ_END));
 			}
 		});
 		return true;
 	}
 
 	private boolean readImageFile(final String uri) {
 		if (uri == null)
 			return false;
 		final boolean b = isEditable();
 		if (!b)
 			setEditable(true);
 		notifyPageListeners(new PageEvent(this, PageEvent.PAGE_READ_BEGIN));
 		if (FileUtilities.isRemote(uri)) {
 			URL u = null;
 			try {
 				u = new URL(FileUtilities.httpEncode(uri));
 			}
 			catch (MalformedURLException ex) {
 				ex.printStackTrace();
 				return false;
 			}
 			insertIcon(new ImageIcon(u));
 		}
 		else {
 			insertIcon(new ImageIcon(uri));
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				setTitle(uri);
 				if (!b)
 					setEditable(false);
 				notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_READ_END));
 			}
 		});
 		return true;
 	}
 
 	private boolean readPlainTextFile(final String uri) throws IOException {
 		if (uri == null)
 			return false;
 		Reader reader = null;
 		if (FileUtilities.isRemote(uri)) {
 			URL u = null;
 			try {
 				u = new URL(FileUtilities.httpEncode(uri));
 			}
 			catch (MalformedURLException ex) {
 				ex.printStackTrace();
 				return false;
 			}
 			URLConnection conn = ConnectionManager.getConnection(u);
 			if (conn == null)
 				return false;
 			try {
 				reader = new InputStreamReader(conn.getInputStream());
 			}
 			catch (IOException e) {
 				throw new IOException(e.getMessage());
 			}
 		}
 		else {
 			try {
 				reader = new FileReader(new File(uri));
 			}
 			catch (IOException e) {
 				throw new IOException(e.getMessage());
 			}
 		}
 		final boolean b = isEditable();
 		if (!b)
 			setEditable(true);
 		notifyPageListeners(new PageEvent(this, PageEvent.PAGE_READ_BEGIN));
 		char[] c = new char[1024];
 		int cpos = getCaretPosition();
 		int n = 0;
 		boolean success = true;
 		try {
 			while ((n = reader.read(c)) != -1) {
 				getDocument().insertString(cpos, new String(c, 0, n), null);
 				cpos += n;
 			}
 		}
 		catch (BadLocationException e) {
 			e.printStackTrace();
 			success = false;
 		}
 		catch (IOException e) {
 			success = false;
 			throw new IOException(e.getMessage()); // rethrow
 		}
 		finally {
 			reader.close();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				setCaretPosition(0);
 				setTitle(uri);
 				if (!b)
 					setEditable(false);
 				notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_READ_END));
 			}
 		});
 		return success;
 	}
 
 	/*
 	 * reset the attributes of the paragraph specified by the position to the default values. Removing all the character
 	 * elements does not automatically reset the paragraph's attributes. This method should be called to attend to that
 	 * effect whenever a new page is loaded.
 	 */
 	private void resetToDefaultParagraphAttributes(int position) {
 		StyledDocument doc = getStyledDocument();
 		if (doc == null)
 			return;
 		Element elem = doc.getParagraphElement(position);
 		if (elem == null)
 			return;
 		AttributeSet a = elem.getAttributes();
 		SimpleAttributeSet sas = new SimpleAttributeSet(a);
 		StyleConstants.setAlignment(sas, StyleConstants.ALIGN_LEFT);
 		StyleConstants.setFirstLineIndent(sas, 0);
 		StyleConstants.setLeftIndent(sas, 0);
 		StyleConstants.setRightIndent(sas, 0);
 		StyleConstants.setSpaceAbove(sas, 0);
 		StyleConstants.setSpaceBelow(sas, 0);
 		doc.setParagraphAttributes(position, 1, sas, true);
 	}
 
 	/* insert a html file to the caret's position */
 	private boolean importFile() {
 		boolean success = false;
 		final Frame frame = JOptionPane.getFrameForComponent(this);
 		final File lastFile = fileChooser.getSelectedFile();
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		fileChooser.addChoosableFileFilters(new String[] { "html", "txt" });
 		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
 		fileChooser.setDialogTitle("Import");
 		fileChooser.setApproveButtonMnemonic('O');
 		String latestPath = fileChooser.getLastVisitedPath();
 		if (latestPath != null)
 			fileChooser.setCurrentDirectory(new File(latestPath));
 		fileChooser.recallLastFile(lastFile);
 		fileChooser.setAccessory(null);
 		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			final File file = fileChooser.getSelectedFile();
 			File dir = fileChooser.getCurrentDirectory();
 			if (file.exists()) {
 				if (FileFilterFactory.getFilter("html").accept(file)) {
 					JOptionPane.showMessageDialog(frame, "Some HTML content may not be converted.", "Import HTML File",
 							JOptionPane.INFORMATION_MESSAGE);
 					HTMLConverter.insertHTMLFile(getStyledDocument(), getCaretPosition(), file);
 				}
 				else if (FileFilterFactory.getFilter("txt").accept(file)) {
 					try {
 						readPlainTextFile(file.getAbsolutePath());
 					}
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				fileChooser.rememberPath(dir.toString());
 				success = true;
 			}
 			else {
 				JOptionPane.showMessageDialog(frame, file + " does not exist.", "File does not exist",
 						JOptionPane.ERROR_MESSAGE);
 				fileChooser.resetChoosableFileFilters();
 				return false;
 			}
 
 		}
 		fileChooser.resetChoosableFileFilters();
 		return success;
 	}
 
 	/* read a page from a file chooser */
 	private boolean openPage() {
 		boolean success = false;
 		Frame frame = JOptionPane.getFrameForComponent(this);
 		File lastFile = fileChooser.getSelectedFile();
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		fileChooser.addChoosableFileFilters(new String[] { "png", "gif", "jpg", "txt", "mws", "pdb", "xyz", "cml" });
 		fileChooser.setFileFilter(FileFilterFactory.getFilter("cml"));
 		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
 		String s = Modeler.getInternationalText("OpenPage");
 		fileChooser.setDialogTitle(s != null ? s : "Open page");
 		fileChooser.setApproveButtonMnemonic('O');
 		String latestPath = fileChooser.getLastVisitedPath();
 		if (latestPath != null)
 			fileChooser.setCurrentDirectory(new File(latestPath));
 		if (lastFile != null && !lastFile.isDirectory())
 			fileChooser.recallLastFile(lastFile);
 		fileChooser.setAccessory(null);
 		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File file = fileChooser.getSelectedFile();
 			FileFilter ff = fileChooser.getFileFilter();
 			if (ff != null && !ff.accept(file))
 				file = new File(file.getAbsolutePath() + "." + ff);
 			if (file.exists()) {
 				if (navigator != null) {
 					navigator.visitLocation(file.getAbsolutePath());
 				}
 				else {
 					visit(file.getAbsolutePath());
 				}
 				fileChooser.rememberPath(fileChooser.getCurrentDirectory().toString());
 				fileChooser.rememberFile(file.getAbsolutePath(), recentFilesMenu);
 				success = true;
 			}
 			else {
 				JOptionPane.showMessageDialog(frame, file + " does not exist.\n"
 						+ "If you meant to create a new file, please use the <New> menu or button, then save it.",
 						"File does not exist", JOptionPane.ERROR_MESSAGE);
 				fileChooser.resetChoosableFileFilters();
 				return false;
 			}
 
 		}
 		fileChooser.resetChoosableFileFilters();
 		return success;
 	}
 
 	void nameModels() {
 		if (!isRemote())
 			nameModels(new File(pageAddress));
 	}
 
 	private void nameModels(File file) {
 		Model m = null;
 		String ext = "mml";
 		int incr_mml = 0, incr_gbl = 0;
 		synchronized (componentPool) {
 			for (ModelCanvas c : componentPool.getModels()) {
 				if (c.isUsed()) {
 					m = c.getContainer().getModel();
 					if (m instanceof MolecularModel) {
 						m.putProperty("old url", m.getProperty("url"));
 						ext = "mml";
 						m.putProperty("url", FileUtilities.changeExtension(file.toString(), ext, incr_mml));
 						m.putProperty("filename", FileUtilities.changeExtension(FileUtilities.getFileName(file
 								.toString()), ext, incr_mml));
 						incr_mml++;
 					}
 					else if (m instanceof MesoModel) {
 						m.putProperty("old url", m.getProperty("url"));
 						ext = "gbl";
 						m.putProperty("url", FileUtilities.changeExtension(file.toString(), ext, incr_gbl));
 						m.putProperty("filename", FileUtilities.changeExtension(FileUtilities.getFileName(file
 								.toString()), ext, incr_gbl));
 						incr_gbl++;
 					}
 				}
 			}
 		}
 	}
 
 	private boolean validateFileName(File file) {
 		if (file == null)
 			return false;
 		switch (FileUtilities.checkFileName(file)) {
 		case FileUtilities.DIRECTORY_ERROR:
 			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "Directory error: " + file,
 					"Path error", JOptionPane.ERROR_MESSAGE);
 			return false;
 		case FileUtilities.ILLEGAL_CHARACTER:
 			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
 					"A file name cannot contain any of the following characters:\n\\/:*?\"<>|", "Path error",
 					JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		return true;
 	}
 
 	public void saveReport() {
 		File file = new File(Initializer.sharedInstance().getCacheDirectory(), "Untitled.cml");
 		synchronized (lock) {
 			saveTo(file);
 		}
 		String s = file.getAbsolutePath();
 		if (navigator != null)
 			navigator.storeLocation(s);
 	}
 
 	/*
 	 * this method MUST be called by a <code>SwingWorker</code>'s <code>construct()</code> method. Depending on the
 	 * situation, call <code>SwingWorker.finished()</code> method to decide on what to do immediately following the
 	 * save action. This method should NOT be directly used by another class other than a subclass of this class.
 	 */
 	private void saveTo(File file) {
 		if (file == null)
 			return;
 		String s = file.toString().toLowerCase();
 		if (s.endsWith(".cml")) {
 			isWriting = true;
 			nameModels(file);
 			((AbstractDocument) getDocument()).readLock();
 			try {
 				encoder.write(file);
 				setAddress(file.toString());
 				setTitle(getTitle());
 			}
 			catch (Exception e) {
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Page.this),
 								"The content on this page contains errors that\n"
 										+ "cannot be saved. Please correct them, and try\n" + "again.",
 								"Output data error", JOptionPane.ERROR_MESSAGE);
 					}
 				});
 				e.printStackTrace();
 			}
 			finally {
 				isWriting = false;
 				((AbstractDocument) getDocument()).readUnlock();
 			}
 		}
 		else if (s.endsWith(".mml") || s.endsWith(".gbl")) {
 			Object o = getEmbeddedComponent(ModelCanvas.class, 0);
 			if (o instanceof ModelCanvas) {
 				ModelCanvas mc = (ModelCanvas) o;
 				Model model = mc.getContainer().getModel();
 				model.output(file);
 				properties.put(mc, mc.getURL());
 			}
 		}
 		else if (s.endsWith(".txt") || s.endsWith(".mws")) {
 			if (plainTextWriter == null) {
 				plainTextWriter = new PlainTextWriter(this);
 				plainTextWriter.setProgressBar(encoder.getProgressBar());
 			}
 			plainTextWriter.write(file);
 		}
 		else if (s.endsWith(".gif") || s.endsWith(".jpg") || s.endsWith(".png") || s.endsWith(".jpeg")
 				|| s.endsWith(".pdb") || s.endsWith(".xyz")) {
 			if (!pageAddress.equalsIgnoreCase(s))
 				ModelerUtilities.copyResource(pageAddress, file);
 		}
 		saveReminder.setChanged(false);
 	}
 
 	public void convertToHTML() {
 
 		final Frame frame = JOptionPane.getFrameForComponent(this);
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		fileChooser.addChoosableFileFilter(FileFilterFactory.getFilter("html"));
 		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
 		String s = Modeler.getInternationalText("SavePageAsHTML");
 		fileChooser.setDialogTitle(s != null ? s : "Save page in HTML format");
 		fileChooser.setApproveButtonMnemonic('S');
 		String latestPath = fileChooser.getLastVisitedPath();
 		if (latestPath != null)
 			fileChooser.setCurrentDirectory(new File(latestPath));
 		fileChooser.setAccessory(null);
 		String filename = FileUtilities.getFileName(pageAddress);
 		fileChooser.setSelectedFile(new File(FileUtilities.changeExtension(filename, "html")));
 		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			filename = FileUtilities.fileNameAutoExtend(FileFilterFactory.getFilter("html"), fileChooser
 					.getSelectedFile());
 			final File fil = new File(filename);
 			if (fil.exists()) {
 				s = Modeler.getInternationalText("FileExists");
 				String s1 = Modeler.getInternationalText("File");
 				String s2 = Modeler.getInternationalText("Overwrite");
 				String s3 = Modeler.getInternationalText("Exists");
 				if (JOptionPane.showConfirmDialog(frame, (s1 != null ? s1 : "File") + " " + fil.getName() + " "
 						+ (s3 != null ? s3 : "exists") + ", " + (s2 != null ? s2 : "overwrite") + "?", s != null ? s
 						: "File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
 					fileChooser.resetChoosableFileFilters();
 					return;
 				}
 			}
 
 			if (!isWriting()) {
 				new SwingWorker("HTML-encoder thread") {
 					public Object construct() {
 						isWriting = true;
 						PageHTMLEncoder htmlEncoder = new PageHTMLEncoder(Page.this);
 						htmlEncoder.setProgressBar(encoder.getProgressBar());
 						((AbstractDocument) getDocument()).readLock();
 						try {
 							htmlEncoder.write(fil);
 						}
 						catch (Exception e) {
 							EventQueue.invokeLater(new Runnable() {
 								public void run() {
 									JOptionPane.showMessageDialog(frame, "This page cannot be converted to HTML.",
 											"Conversion error", JOptionPane.ERROR_MESSAGE);
 								}
 							});
 							e.printStackTrace();
 						}
 						finally {
 							isWriting = false;
 							((AbstractDocument) getDocument()).readUnlock();
 						}
 						return htmlEncoder;
 					}
 
 					public void finished() {
 						isWriting = false;
 						((PageHTMLEncoder) get()).setProgressBar(null);
 						if (JOptionPane.showConfirmDialog(frame,
 								"The page has been saved in the HTML format.\nDo you want to view it now?",
 								"View HTML", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
 							return;
 						ExternalClient.open(ExternalClient.HTML_CLIENT, fil.toString());
 					}
 				}.start();
 				fileChooser.rememberPath(fileChooser.getCurrentDirectory().toString());
 				// fileChooser.rememberFile(filename);
 
 			}
 		}
 
 		fileChooser.resetChoosableFileFilters();
 
 	}
 
 	/**
 	 * save the current page and signify that the specified window should be closed shortly. This method should be
 	 * called when shutting down or closing window.
 	 */
 	public void saveAndClose(final Window win) {
 		final File file = new File(pageAddress);
 		if (!pageAddress.equals("Untitled.cml")) {
 			if (!validateFileName(file))
 				return;
 			if (!isWriting()) {
 				new SwingWorker("Page-saving thread") {
 					public Object construct() {
 						synchronized (lock) {
 							SaveComponentStateReminder.setEnabled(false);
 							saveTo(file);
 						}
 						return file;
 					}
 
 					public void finished() {
 						isWriting = false;
 						System.out.println(file + " written");
 						win.dispatchEvent(new WindowEvent(win, WindowEvent.WINDOW_CLOSING));
 					}
 				}.start();
 			}
 		}
 		else {
 			fileChooser.setAcceptAllFileFilterUsed(false);
 			fileChooser.addChoosableFileFilter(FileFilterFactory.getFilter("cml"));
 			fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
 			String s = Modeler.getInternationalText("SavePage");
 			fileChooser.setDialogTitle(s != null ? s : "Save page");
 			fileChooser.setApproveButtonMnemonic('S');
 			fileChooser.resetTextField();
 			String latestPath = fileChooser.getLastVisitedPath();
 			if (latestPath != null)
 				fileChooser.setCurrentDirectory(new File(latestPath));
 			if (fileChooser.showSaveDialog(win) == JFileChooser.APPROVE_OPTION) {
 				final String filename = FileUtilities.fileNameAutoExtend(FileFilterFactory.getFilter("cml"),
 						fileChooser.getSelectedFile());
 				final File file2 = new File(filename);
 				boolean b = true;
 				if (file2.exists()) {
 					s = Modeler.getInternationalText("FileExists");
 					String s1 = Modeler.getInternationalText("File");
 					String s2 = Modeler.getInternationalText("Overwrite");
 					String s3 = Modeler.getInternationalText("Exists");
 					if (JOptionPane.showConfirmDialog(win, (s1 != null ? s1 : "File") + " " + file2.getName() + " "
 							+ (s3 != null ? s3 : "exists") + ", " + (s2 != null ? s2 : "overwrite") + "?",
 							s != null ? s : "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION
 							|| isWriting()) {
 						b = false;
 					}
 				}
 				if (b) {
 					new SwingWorker() {
 						public Object construct() {
 							synchronized (lock) {
 								SaveComponentStateReminder.setEnabled(false);
 								saveTo(file2);
 							}
 							return file2;
 						}
 
 						public void finished() {
 							isWriting = false;
 							fileChooser.rememberPath(fileChooser.getCurrentDirectory().toString());
 							fileChooser.rememberFile(filename, null);
 							System.out.println(filename + " written");
 							win.dispatchEvent(new WindowEvent(win, WindowEvent.WINDOW_CLOSING));
 						}
 					}.start();
 				}
 			}
 		}
 	}
 
 	/**
 	 * save this page to disk.
 	 * 
 	 * @param alwaysAsk
 	 *            always ask before overwriting
 	 * @param newAddress
 	 *            after writing this page, load this new page. Pass null if no new page has to be loaded.
 	 */
 	protected boolean savePage(boolean alwaysAsk, String newAddress) {
 
 		FileFilter filter = FileFilterFactory.getFilter(FileUtilities.getSuffix(pageAddress));
 		if (filter == null)
 			filter = FileFilterFactory.getFilter("cml");
 
 		saveReminder.setChanged(false);
 		properties.clear();
 		deselect();
 
 		if (!alwaysAsk) {
 			if (!pageAddress.equals("Untitled.cml") && !isRemote()) {
 				if (!isWriting()) {
 					final File file = new File(pageAddress);
 					if (validateFileName(file)) {
 						final String newPageAddress = newAddress;
 						new SwingWorker("Page-saving thread") {
 							public Object construct() {
 								notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_OVERWRITE_BEGIN));
 								synchronized (lock) {
 									saveTo(file);
 								}
 								return file;
 							}
 
 							public void finished() {
 								isWriting = false;
 								if (newPageAddress != null)
 									readPageWithThread(newPageAddress);
 								notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_OVERWRITE_END));
 							}
 						}.start();
 					}
 				}
 				else {
 					System.out.println("busy writing " + pageAddress);
 				}
 				return true;
 			}
 		}
 
 		boolean overwrite = false;
 		Frame frame = JOptionPane.getFrameForComponent(this);
 		if (filter != null) {
 			fileChooser.setAcceptAllFileFilterUsed(false);
 			fileChooser.addChoosableFileFilter(filter);
 		}
 		else {
 			fileChooser.setAcceptAllFileFilterUsed(true);
 		}
 		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
 		String s = Modeler.getInternationalText("SavePage");
 		fileChooser.setDialogTitle(s != null ? s : "Save page");
 		fileChooser.setApproveButtonMnemonic('S');
 		String latestPath = fileChooser.getLastVisitedPath();
 		if (latestPath != null)
 			fileChooser.setCurrentDirectory(new File(latestPath));
 		fileChooser.setAccessory(null);
 		if (isRemote()) {
 			try {
 				fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), FileUtilities
 						.getFileName(pageAddress)));
 			}
 			catch (NullPointerException npe) {
 				npe.printStackTrace();
 				fileChooser.resetTextField();
 			}
 		}
 		else {
 			try {
 				File f2 = new File(fileChooser.getCurrentDirectory(), FileUtilities.getFileName(pageAddress));
 				if (f2.exists()) {
 					fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), "Copy of "
 							+ FileUtilities.getFileName(pageAddress)));
 				}
 				else {
 					fileChooser.setSelectedFile(f2);
 				}
 			}
 			catch (NullPointerException npe) {
 				npe.printStackTrace();
 				fileChooser.resetTextField();
 			}
 		}
 
 		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			final File file = new File(FileUtilities.fileNameAutoExtend(filter, fileChooser.getSelectedFile()));
 			if (!validateFileName(file)) {
 				fileChooser.resetChoosableFileFilters();
 				return false;
 			}
 			if (file.exists()) {
 				s = Modeler.getInternationalText("FileExists");
 				String s1 = Modeler.getInternationalText("File");
 				String s2 = Modeler.getInternationalText("Overwrite");
 				String s3 = Modeler.getInternationalText("Exists");
 				if (JOptionPane.showConfirmDialog(frame, (s1 != null ? s1 : "File") + " " + file.getName() + " "
 						+ (s3 != null ? s3 : "exists") + ", " + (s2 != null ? s2 : "overwrite") + "?", s != null ? s
 						: "File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
 					fileChooser.resetChoosableFileFilters();
 					return false;
 				}
 				overwrite = true;
 			}
 
 			final PageEvent pe_beg = new PageEvent(this, overwrite ? PageEvent.PAGE_OVERWRITE_BEGIN
 					: PageEvent.PAGE_WRITE_BEGIN);
 			final PageEvent pe_end = new PageEvent(this, overwrite ? PageEvent.PAGE_OVERWRITE_END
 					: PageEvent.PAGE_WRITE_END);
 
 			if (!isWriting()) {
 				if (isRemote()) {
 					List list = getEmbeddedComponents();
 					String pathBase = getPathBase();
 					for (Object o : list) {
 						if (o instanceof HtmlService) {
 							((HtmlService) o).useCachedImages(false, pathBase);
 						}
 					}
 				}
 				final String newPageAddress = newAddress;
 				new SwingWorker("Page-saving thread") {
 					public Object construct() {
 						notifyPageListeners(pe_beg);
 						synchronized (lock) {
 							saveTo(file);
 						}
 						return file;
 					}
 
 					public void finished() {
 						isWriting = false;
 						String s = file.getAbsolutePath();
 						if (navigator != null)
 							navigator.storeLocation(s);
 						notifyPageListeners(pe_end);
 						if (newPageAddress != null) {
 							readPageWithThread(newPageAddress);
 							if (navigator != null)
 								navigator.storeLocation(newPageAddress);
 						}
 						setAddress(s);
 					}
 				}.start();
 				fileChooser.rememberPath(fileChooser.getCurrentDirectory().toString());
 				fileChooser.rememberFile(file.getAbsolutePath(), recentFilesMenu);
 
 			}
 		}
 
 		fileChooser.resetChoosableFileFilters();
 		return true;
 
 	}
 
 	/**
 	 * download a page or an activity.
 	 * 
 	 * @param src
 	 *            the URL address of the source to download from
 	 * @param des
 	 *            the destination directory to download to
 	 */
 	public void downloadPage(String src, File des) {
 		new ZipDownloader(this, src, des, false).download();
 	}
 
 	/**
 	 * @see org.concord.modeler.Upload
 	 */
 	public void uploadPage(Upload upload) {
 		new ZipUploader(this, upload).upload();
 	}
 
 	/**
 	 * This method does not use a thread. It is your own responsibility to put it in a thread.
 	 */
 	public boolean writePage(ZipOutputStream zos) {
 		if (zos == null)
 			return false;
 		File zipDir = new File(Initializer.sharedInstance().getCacheDirectory(), FileUtilities
 				.removeSuffix(FileUtilities.getFileName(pageAddress)));
 		zipDir.mkdir();
 		if (!isWriting()) {
 			synchronized (lock) {
 				zipPage(zos, zipDir);
 				isWriting = false;
 			}
 		}
 		return true;
 	}
 
 	private boolean zipPage(ZipOutputStream zipOut, File zipDir) {
 		if (zipOut == null)
 			return false;
 		boolean isChanged = saveReminder.isChanged();
 		saveTo(new File(zipDir, FileUtilities.getFileName(pageAddress)));
 		saveReminder.setChanged(isChanged); // reset the SaveReminder
 		nameModels(); // reset the model names
 		File[] f = zipDir.listFiles();
 		FileInputStream in = null;
 		int c;
 		boolean b = true;
 		try {
 			for (int i = 0; i < f.length; i++) {
 				zipOut.putNextEntry(new ZipEntry(f[i].getName()));
 				in = new FileInputStream(f[i]);
 				while ((c = in.read()) != -1)
 					zipOut.write(c);
 				in.close();
 				zipOut.closeEntry();
 				zipOut.flush();
 			}
 			zipOut.finish(); // necessary?
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			b = false;
 		}
 		finally {
 			if (in != null) { // just in case the last input stream is not closed
 				try {
 					in.close();
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			try {
 				zipOut.close();
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			FileUtilities.deleteAllFiles(zipDir);
 			zipDir.delete();
 		}
 		return b;
 	}
 
 	/**
 	 * add all files of this page to a zip file, to be designated by the file chooser
 	 */
 	public boolean writePageToZipFile() {
 
 		Frame frame = JOptionPane.getFrameForComponent(this);
 		FileFilter filter = FileFilterFactory.getFilter("zip");
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		fileChooser.addChoosableFileFilter(filter);
 		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
 		String s = Modeler.getInternationalText("CompressPage");
 		fileChooser.setDialogTitle(s != null ? s : "Compress current page to file");
 		fileChooser.setApproveButtonMnemonic('S');
 		String latestPath = fileChooser.getLastVisitedPath();
 		if (latestPath != null)
 			fileChooser.setCurrentDirectory(new File(latestPath));
 		fileChooser.setAccessory(null);
 		try {
 			fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), FileUtilities.changeExtension(
 					FileUtilities.getFileName(pageAddress), "zip")));
 		}
		catch (NullPointerException e) {
			e.printStackTrace();
 			fileChooser.resetTextField();
 		}
 
 		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 
 			File file = new File(FileUtilities.fileNameAutoExtend(filter, fileChooser.getSelectedFile()));
 			if (!validateFileName(file)) {
 				fileChooser.resetChoosableFileFilters();
 				return false;
 			}
 			if (file.exists()) {
 				s = Modeler.getInternationalText("FileExists");
 				if (JOptionPane.showConfirmDialog(frame, "Zip file " + file.getName() + " exists, overwrite?\n\n"
 						+ "WARNING: The old files contained in the existing zip\n"
 						+ "file will be deleted if you choose to overwrite.", s != null ? s : "File exists",
 						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
 					fileChooser.resetChoosableFileFilters();
 					return false;
 				}
 			}
 
 			String filename = FileUtilities.getFileName(file.getAbsolutePath());
 			final File zipDir = new File(fileChooser.getCurrentDirectory(), FileUtilities.removeSuffix(filename));
 			zipDir.mkdir();
			
 			ZipOutputStream zipOut = null;
 			try {
 				/*
 				 * NOTE!!! "append" does not seem to work: while the new zip entries can be successfully added, the old
 				 * ones become invisible and unrestorable by ZIP, though they still count for the zip file's size. This
 				 * is believed to caused by the fact that Java's zip implementation is not random-access.
 				 */
 				zipOut = new ZipOutputStream(new FileOutputStream(file, false));
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			if (!isWriting() && zipOut != null) {
 				final ZipOutputStream zos = zipOut;
 				final String oldAddress = pageAddress;
 				new SwingWorker("Page Compressor") {
 					public Object construct() {
 						synchronized (lock) {
 							zipPage(zos, zipDir);
 						}
 						return null;
 					}
 
 					public void finished() {
 						isWriting = false;
 						setAddress(oldAddress);
 					}
 				}.start();
 			}
 			fileChooser.rememberPath(fileChooser.getCurrentDirectory().toString());
 
 		}
 
 		fileChooser.resetChoosableFileFilters();
 		return true;
 
 	}
 
 	protected void resetUndoManager() {
 		undoManager.discardAllEdits();
 		((UndoAction) getAction(UNDO)).updateState();
 		((RedoAction) getAction(REDO)).updateState();
 	}
 
 	public void setFillMode(FillMode fm) {
 		fillMode = fm;
 		if (fillMode == FillMode.getNoFillMode()) {
 			setBackground(Color.white);
 			setBackgroundImage(null);
 		}
 		else if (fillMode instanceof FillMode.ColorFill) {
 			setBackground(((FillMode.ColorFill) fillMode).getColor());
 			setBackgroundImage(null);
 		}
 		else if (fillMode instanceof FillMode.ImageFill) {
 			String s = ((FillMode.ImageFill) fillMode).getURL();
 			if (FileUtilities.isRemote(s)) {
 				URL url = null;
 				try {
 					url = new URL(s);
 				}
 				catch (MalformedURLException e) {
 					setBackgroundImage(null);
 					repaint();
 					return;
 				}
 				ImageIcon icon = ConnectionManager.sharedInstance().loadImage(url);
 				if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
 					setBackgroundImage(icon);
 				}
 				else {
 					setBackgroundImage(null);
 				}
 			}
 			else {
 				setBackgroundImage(new ImageIcon(Toolkit.getDefaultToolkit().createImage(s), s));
 			}
 		}
 		else {
 			setBackgroundImage(null);
 		}
 		repaint();
 	}
 
 	public FillMode getFillMode() {
 		return fillMode;
 	}
 
 	public void changeFillMode(FillMode fm) {
 		if (fm == null)
 			return;
 		if (fm.equals(getFillMode()))
 			return;
 		setFillMode(fm);
 		saveReminder.setChanged(true);
 	}
 
 	public void paintComponent(Graphics g) {
 		g.setColor(getBackground());
 		g.fillRect(0, 0, getWidth(), getHeight());
 		if (backgroundImage != null) {
 			int imax = getWidth() / iconWidth + 1;
 			int jmax = getHeight() / iconHeight + 1;
 			for (int i = 0; i < imax; i++) {
 				for (int j = 0; j < jmax; j++) {
 					backgroundImage.paintIcon(this, g, i * iconWidth, j * iconHeight);
 				}
 			}
 		}
 		if (fillMode instanceof FillMode.GradientFill) {
 			FillMode.GradientFill gfm = (FillMode.GradientFill) fillMode;
 			GradientFactory.paintRect((Graphics2D) g, gfm.getStyle(), gfm.getVariant(), gfm.getColor1(), gfm
 					.getColor2(), 0, 0, getWidth(), getHeight());
 		}
 		else if (fillMode instanceof FillMode.PatternFill) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setPaint(((FillMode.PatternFill) fillMode).getPaint());
 			g2.fillRect(0, 0, getWidth(), getHeight());
 		}
 		super.paintComponent(g);
 	}
 
 	/* A thread wrapper of the <code>readPage(String)</code> method. */
 	private void readPageWithThread(final String uri) {
 		// files that should be opened in a pop-up.
 		if (uri.toLowerCase().endsWith(".zip")) { // load a compressed file
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					ZipDownloader downloader = new ZipDownloader(Page.this, uri);
 					if (downloader.chooseFile())
 						downloader.download();
 				}
 			});
 			return;
 		}
 		notifyPageListeners(new PageEvent(this, PageEvent.STORE_VIEW_POSITION));
 		notifyPageListeners(new PageEvent(this, PageEvent.PAGE_READ_BEGIN));
 		clearAllContent();
 		synchronized (lock) {
 			lock.notify();
 			setAddress(uri);
 		}
 		if (pageLoadingThread == null) {
 			decoder.threadIndex = threadIndex;
 			final Runnable r = new Runnable() {
 				public void run() {
 					saveReminder.setChanged(false);
 					setReading(false);
 					// notify Editor to update buttons accordingly
 					notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_READ_END));
 					if (!rememberViewPosition) {
 						rememberViewPosition = true;
 					}
 					else {
 						notifyPageListeners(new PageEvent(Page.this, PageEvent.RESTORE_VIEW_POSITION));
 					}
 				}
 			};
 			pageLoadingThread = new Thread("Page Loader #" + (threadIndex++)) {
 				public void run() {
 					while (true) {
 						setReading(true);
 						readPage(pageAddress);
 						EventQueue.invokeLater(r);
 						synchronized (lock) {
 							try {
 								lock.wait();
 							}
 							catch (InterruptedException e) {
 								// e.printStackTrace();
 								break;
 							}
 						}
 					}
 				}
 			};
 			pageLoadingThread.setPriority(LOADER_THREAD_PRIORITY);
 			pageLoadingThread.setUncaughtExceptionHandler(new DisasterHandler(DisasterHandler.LOAD_ERROR,
 					new Runnable() {
 						public void run() {
 							pageLoadingThread = null;
 						}
 					}, null, this));
 			pageLoadingThread.start();
 		}
 	}
 
 	private void renderHTMLContent(String address, final boolean sendID) {
 		if (address == null)
 			return;
 		if (FileUtilities.isRelative(address))
 			address = resolvePath(address);
 		setTitle(address);
 		final PageTextBox t = new PageTextBox();
 		t.setPage(this);
 		t.setText("<html><body face=Verdana>Opening " + address + " ......</body></html>");
 		// hack for Concord's search engine
 		int i = address.indexOf("/cgi-bin/");
 		if (i != -1) {
 			t.setBase(address.substring(0, i));
 		}
 		t.setImageCached(false);
 		t.setChangable(isEditable());
 		Dimension dim = new Dimension(getWidth(), getHeight());
 		t.setPreferredSize(dim);
 		t.setWidthRatio(1);
 		t.setWidthRelative(true);
 		t.setHeightRatio(1);
 		t.setHeightRelative(true);
 		t.addScrollerMouseWheelListener();
 		t.setIndex(0);
 		insertComponent(t);
 		final String s = address;
 		Thread thread = new Thread("load html") {
 			public void run() {
 				t.load(s, sendID);
 			}
 		};
 		thread.setPriority(LOADER_THREAD_PRIORITY);
 		thread.setUncaughtExceptionHandler(new DisasterHandler(DisasterHandler.LOAD_ERROR, null, null, this));
 		thread.start();
 	}
 
 	/** if there is a hyperlink at the specified location, open it */
 	protected void openHyperlink(Point p) {
 		openHyperlink(isHyperlinked(p));
 	}
 
 	/**
 	 * This is used to open a hyperlink, instead of calling <code>visit()</code>. Calling this method will push the
 	 * current URL addresss into the stacks of the <code>Navigator</code> registered with this <code>Page</code>.
 	 */
 	public void openHyperlink(URL url) {
 		if (url == null)
 			return;
 		rememberViewPosition = false;
 		processHyperlink(url.toString());
 	}
 
 	/** @see org.concord.modeler.text.Page#openHyperlink(java.net.URL) */
 	public void openHyperlink(String href) {
 		if (href == null)
 			return;
 		rememberViewPosition = false;
 		processHyperlink(resolvePath(href));
 	}
 
 	private static String fixLink(String href) {
 		if (FileUtilities.isRemote(href))
 			return href;
 		href = FileUtilities.httpDecode(href);
 		if (href.startsWith("file:")) {
 			// watch here: we need 4 backslashes to make up a new regex "\\"
 			href = OS.startsWith("Windows") ? href.substring(6).replaceAll("/", "\\\\") : href.substring(5);
 		}
 		return href;
 	}
 
 	public ScriptCallback getScriptCallback() {
 		return new ScriptCallback() {
 			public String execute() {
 				String s = getScript();
 				if (s != null) {
 					Matcher matcher = SCRIPT_PATTERN.matcher(s);
 					if (matcher.find()) {
 						return executeScripts(s.split(SCRIPT_PATTERN.pattern()));
 					}
 					matcher = SCRIPT_PATTERN2.matcher(s);
 					if (matcher.find()) {
 						return executeScripts(s.split(SCRIPT_PATTERN2.pattern()));
 					}
 				}
 				return writeErrorMessage(s);
 			}
 		};
 	}
 
 	private void processHyperlink(String href) {
 		href = fixLink(href.trim());
 		Matcher matcher = SCRIPT_PATTERN.matcher(href);
 		if (matcher.find()) {
 			executeScripts(href.split(SCRIPT_PATTERN.pattern()));
 		}
 		else {
 			boolean valid = true;
 			final String s = href.toLowerCase();
 			boolean mwClient = s.indexOf("client=mw") != -1
 					|| (s.startsWith(Modeler.getContextRoot()) && s.endsWith(".jsp"));
 			if (mwClient || s.endsWith(".cml") || s.endsWith(".mml") || s.endsWith(".gbl") || s.endsWith(".gif")
 					|| s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".txt")
 					|| s.endsWith(".pdb") || s.endsWith(".xyz") || s.endsWith(".mws")
 					|| s.indexOf("/cgi-bin/htsearch?") != -1) {
 				if (targetIsBlank) {
 					notifyPageListeners(new PageEvent(this, PageEvent.OPEN_NEW_WINDOW, href, linkParam));
 				}
 				else {
 					if (navigator != null) { // if there is a navigator, the address will be pushed into a stack
 						navigator.visitLocation(href);
 					}
 					else {
 						visit(href);
 					}
 				}
 			}
 			else if (s.endsWith(".zip")) {
 				// navigator.visitLocation(href);
 				// FIXME: can we not put the address into the navigator's stack?
 				visit(href);
 			}
 			else if (s.endsWith(".htm") || s.endsWith(".html")) {
 				ExternalClient.open(ExternalClient.HTML_CLIENT, href);
 			}
 			else if (s.endsWith(".pdf")) {
 				ExternalClient.open(ExternalClient.PDF_CLIENT, href);
 			}
 			else if (s.endsWith(".swf")) {
 				ExternalClient.open(ExternalClient.FLASH_CLIENT, href);
 			}
 			else if (s.endsWith(".rm") || s.endsWith(".ram") || s.endsWith(".avi")) {
 				ExternalClient.open(ExternalClient.REALPLAYER_CLIENT, href);
 			}
 			else if (s.endsWith(".qt") || s.endsWith(".mov")) {
 				ExternalClient.open(ExternalClient.QUICKTIME_CLIENT, href);
 			}
 			else if (s.endsWith(".mpg") || s.endsWith(".mpeg") || s.endsWith(".mp3")) {
 				if (OS.startsWith("Mac")) {
 					ExternalClient.open(ExternalClient.QUICKTIME_CLIENT, href);
 				}
 				else {
 					ExternalClient.open(ExternalClient.REALPLAYER_CLIENT, href);
 				}
 			}
 			else if (s.endsWith(".jnlp")) {
 				ExternalClient.open(ExternalClient.JNLP_CLIENT, href);
 			}
 			else if (FileUtilities.isRemote(href)) {
 				ExternalClient.open(ExternalClient.HTML_CLIENT, href);
 			}
 			else if (s.startsWith("mailto:") && href.indexOf("@") != -1) {
 				ExternalClient.open(ExternalClient.EMAIL_CLIENT, href.substring(href.indexOf(":") + 1, href.length()));
 			}
 			else {
 				valid = false;
 				final String s0 = href;
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Page.this),
 								"This hyperlink connects to an unrecognized type of document,\n"
 										+ "or an unsupported action.\n" + s0, "Hyperlink error",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				});
 			}
 			if (valid) {
 				if (!HistoryManager.sharedInstance().wasVisited(href))
 					HistoryManager.sharedInstance().addAddress(href);
 			}
 		}
 	}
 
 	private String sendScript(String[] token, Class klass) {
 		int n = -1;
 		try {
 			n = Integer.valueOf(token[1].trim()).intValue();
 		}
 		catch (NumberFormatException e) {
 			e.printStackTrace();
 			n = -1;
 		}
 		if (n > 0) {
 			if (token[2] != null && token[2].trim().length() > 0) {
 				Object o = getEmbeddedComponent(klass, n - 1);
 				if (o instanceof Scriptable) {
 					return ((Scriptable) o).runScript(token[2].trim());
 				}
 			}
 		}
 		return writeErrorMessage(Arrays.asList(token) + " for " + klass);
 	}
 
 	private static String writeErrorMessage(String s) {
 		return Scriptable.ERROR_HEADER + s;
 	}
 
 	private String executeScripts(String[] s) {
 		String output = "";
 		String msg = "";
 		boolean commentOut = false;
 		synchronized (scriptLock) {
 			for (String str : s) {
 				msg = "";
 				str = str.trim();
 				if (str.equals(""))
 					continue;
 				if (commentOut) {
 					commentOut = str.endsWith("//");
 					continue;
 				}
 				String[] token = str.split(":");
 				if (token.length >= 3) {
 					// reconnect "http://......" and others that should not have been broken up
 					if (token.length >= 4) {
 						for (int k = 3; k < token.length; k++)
 							token[2] += ":" + token[k];
 					}
 					String t0 = token[0].trim().intern();
 					if (t0 == "jmol")
 						msg = sendScript(token, PageMolecularViewer.class);
 					else if (t0 == "mw" || t0 == "mw2d")
 						msg = sendScript(token, ModelCanvas.class);
 					else if (t0 == "mw3d")
 						msg = sendScript(token, PageMd3d.class);
 					else if (t0 == "textbox")
 						msg = sendScript(token, PageTextBox.class);
 					else if (t0 == "colorbar")
 						msg = sendScript(token, IconWrapper.class);
 					else if (t0 == "xygraph")
 						msg = sendScript(token, PageXYGraph.class);
 					else if (t0 == "energylevel")
 						msg = sendScript(token, PageElectronicStructureViewer.class);
 					else if (t0 == "spectrometer")
 						msg = sendScript(token, PagePhotonSpectrometer.class);
 					else if (t0 == "applet")
 						msg = sendScript(token, PageApplet.class);
 					else if (t0 == "plugin")
 						msg = sendScript(token, PageJContainer.class);
 					else if (t0 == "page") {
 						int n = 0;
 						try {
 							n = Integer.valueOf(token[1].trim());
 						}
 						catch (NumberFormatException nfe) {
 							nfe.printStackTrace();
 							n = 0;
 							System.out.println(n);
 						}
 						if (token[2] != null && token[2].trim().length() > 0) {
 							runScript(token[2].trim());
 						}
 					}
 				}
 				output += msg;
 				commentOut = str.endsWith("//");
 			}
 		}
 		return output;
 	}
 
 	private void saveCheck(final String address) {
 		int opt = -1;
 		if (saveReminder.isChanged()) {
 			opt = isRemote() ? JOptionPane.NO_OPTION : saveReminder.showConfirmDialog(this, FileUtilities
 					.getFileName(pageAddress));
 		}
 		boolean readAction = true;
 		switch (opt) {
 		case JOptionPane.YES_OPTION: // save page first and continue to load new page
 			SaveComponentStateReminder.setEnabled(false);
 			savePage(false, address);
 			readAction = false;
 			break;
 		case JOptionPane.CANCEL_OPTION: // cancel this action
 			readAction = false;
 			if (navigator != null) {
 				if (pageAddress.equals("Untitled.cml")) {
 					navigator.storeLocation(UNKNOWN_LOCATION);
 				}
 				else {
 					navigator.storeLocation(pageAddress);
 				}
 			}
 			break;
 		}
 		if (readAction) {
 			readPageWithThread(address);
 		}
 		requestFocusInWindow();
 	}
 
 	public void stopAllRunningModels() {
 		if (componentPool != null)
 			componentPool.stopAllRunningModels();
 		InstancePool.sharedInstance().stopAllRunningModels();
 	}
 
 	public void visit(String str) {
 		if (!isReading()) {
 			stopAllRunningModels();
 			DATE.setTime(System.currentTimeMillis());
 			LogDumper.sharedInstance().dump(DATE + " : " + str);
 			final String address = ModelerUtilities.convertURLToFilePath(str.trim());
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					saveCheck(address);
 				}
 			});
 		}
 		else {
 			System.err.println("Attempt to read before last input completes");
 		}
 	}
 
 	private boolean isElementImage(Element e) {
 		if (e == null)
 			throw new IllegalArgumentException("null element");
 		AttributeSet a = e.getAttributes();
 		if (a != null && a.getAttribute(HTML.Attribute.SRC) != null)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Warning: This method works only when an image containing a map of hyperlinks is clicked. In all other cases,
 	 * <code>HyperlinkEvent</code>s are processed in <code>hotlinkUpdate</code>. This is a workaround for solving
 	 * the image map problem.
 	 * 
 	 * @see org.concord.modeler.event.HotlinkListener
 	 */
 	public void hyperlinkUpdate(HyperlinkEvent e) {
 		String desc = e.getDescription();
 		if (e.getURL() != null && (desc != null && !desc.trim().toLowerCase().startsWith("script")) && isEditable())
 			return;
 		Element src = e.getSourceElement();
 		if (src == null)
 			return;
 		if (!isElementImage(src))
 			return;
 		Object eventType = e.getEventType();
 		if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
 			if (targetIsBlank && linkParam != null)
 				linkParam.reset();
 			if (e.getURL() != null) {
 				openHyperlink(e.getURL());
 			}
 			else {
 				openHyperlink(desc);
 			}
 		}
 		else if (eventType == HyperlinkEvent.EventType.ENTERED) {
 			urlDisplay.setText(e.getURL() == null ? desc : e.getURL().toString());
 		}
 		else if (eventType == HyperlinkEvent.EventType.EXITED) {
 			urlDisplay.setText(null);
 		}
 	}
 
 	public void hotlinkUpdate(HyperlinkEvent e) {
 		String desc = e.getDescription();
 		if (e.getURL() != null && (desc != null && !desc.trim().toLowerCase().startsWith("script")) && isEditable())
 			return;
 		Element src = e.getSourceElement();
 		if (src == null)
 			return;
 		if (isElementImage(src))
 			return;
 		Object eventType = e.getEventType();
 		if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
 			targetIsBlank = false;
 			AttributeSet a = src.getAttributes();
 			AttributeSet b = null;
 			if (a != null) {
 				b = (AttributeSet) a.getAttribute(HTML.Tag.A);
 				if (b != null) {
 					String s = (String) b.getAttribute(HTML.Attribute.HREF);
 					if (s != null) {
 						s = (String) b.getAttribute(HTML.Attribute.TARGET);
 						if (s != null)
 							targetIsBlank = s.equalsIgnoreCase("_blank");
 					}
 				}
 			}
 			if (targetIsBlank)
 				setLinkParam(b);
 			if (e.getURL() == null) {
 				if (desc != null && desc.startsWith("?client=mw")) {
 					desc = Modeler.getContextRoot() + desc;
 				}
 				openHyperlink(desc);
 			}
 			else {
 				openHyperlink(e.getURL());
 			}
 		}
 		else if (eventType == HyperlinkEvent.EventType.ENTERED) {
 			if (urlDisplay != null)
 				urlDisplay.setText(e.getURL() == null ? desc : e.getURL().toString());
 		}
 		else if (eventType == HyperlinkEvent.EventType.EXITED) {
 			if (urlDisplay != null)
 				urlDisplay.setText(null);
 		}
 	}
 
 	/** side effect of implementing <code>ClipboardOwner</code> */
 	public void lostOwnership(Clipboard c, Transferable t) {
 	}
 
 	/** side effect of implementing <code>ImageImporter</code> */
 	public void imageImported(ImageEvent e) {
 		if (isEditable()) {
 			insertWrappedIcon(new ImageIcon(e.getImage(), e.getPath()));
 		}
 		else {
 			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Page.this),
 					"This document is not editable. Make it editable first.", "Document not editable",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	private void insertWrappedIcon(Icon icon) {
 		if (wrapIconWithComponent(icon)) {
 			insertComponent(new IconWrapper(icon, this));
 		}
 		else {
 			insertIcon(icon);
 		}
 	}
 
 	static boolean wrapIconWithComponent(Icon icon) {
 		if (icon instanceof ImageIcon)
 			return false;
 		if (icon instanceof LineIcon) {
 			String s = ((LineIcon) icon).getText();
 			if (s != null && !s.trim().equals(""))
 				return true;
 		}
 		return icon.getIconHeight() > 16;
 	}
 
 	private boolean containedInEmbeddedComponent(Point p) {
 		List<Embeddable> list = getEmbeddedComponents();
 		if (list == null || list.isEmpty())
 			return false;
 		synchronized (list) {
 			for (Embeddable e : list) {
 				if (e instanceof Component) {
 					Point p1 = SwingUtilities.convertPoint(this, p, (Component) e);
 					if (((Component) e).contains(p1))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	void processMousePressed(MouseEvent e) {
 		requestFocusInWindow();
 		removeHighlights();
 		if (ModelerUtilities.isRightClick(e)) {
 			Point p = new Point(e.getX(), e.getY());
 			String link = isHyperlinked(p);
 			if (link != null) {
 				setCaretPosition(viewToModel(p));
 				linkPopupMenu.setName(link);
 				linkPopupMenu.show(this, p.x, p.y);
 			}
 			else {
 				Icon image = isIcon(p);
 				if (image instanceof ImageIcon) {
 					imagePopupMenu.setImage((ImageIcon) image);
 					imagePopupMenu.putClientProperty("copy disabled", Boolean.FALSE);
 					imagePopupMenu.show(this, p.x, p.y);
 				}
 				else if (image instanceof LineIcon) {
 					colorBarPopupMenu.setColorBar((LineIcon) image);
 					colorBarPopupMenu.show(this, p.x, p.y);
 				}
 				else {
 					if (!containedInEmbeddedComponent(p)) {
 						popupMenu.show(this, p.x, p.y);
 					}
 				}
 			}
 		}
 	}
 
 	void processMouseReleased(MouseEvent e) {
 		unhighlightEmbedded();
 		if (ModelerUtilities.isRightClick(e))
 			return;
 		if (!isEditable() && !isReading())
 			openHyperlink(new Point(e.getX(), e.getY()));
 	}
 
 	// If no other action is specified, dragging the mouse means selecting a segment of text.
 	void processMouseDragged(MouseEvent e) {
 		highlightEmbedded();
 	}
 
 	void processMouseMoved(MouseEvent e) {
 		if (!isEditable() && !isReading() && !isReadingModel()) {
 			String href = isHyperlinked(new Point(e.getX(), e.getY()));
 			setCursor(href != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor
 					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			if (urlDisplay != null)
 				urlDisplay.setText(href);
 		}
 	}
 
 	/** side effect of implementing <code>ProgressListener</code> */
 	public void progressReported(final ProgressEvent e) {
 		Runnable r = new Runnable() {
 			public void run() {
 				JProgressBar pb = encoder.getProgressBar();
 				if (pb == null)
 					return;
 				pb.setMinimum(e.getMinimum());
 				pb.setMaximum(e.getMaximum());
 				pb.setValue(e.getPercent());
 				pb.setString(e.getDescription());
 			}
 		};
 		if (EventQueue.isDispatchThread()) {
 			r.run();
 		}
 		else {
 			EventQueue.invokeLater(r);
 		}
 	}
 
 	public String getPathBase() {
 		return getPathBase(pageAddress);
 	}
 
 	private static String getPathBase(String url) {
 		if (url == null)
 			return null;
 		if (FileUtilities.isRemote(url)) {
 			int i = url.lastIndexOf('/');
 			if (i == -1)
 				return null;
 			return url.substring(0, i + 1);
 		}
 		int i = url.lastIndexOf(System.getProperty("file.separator"));
 		if (i == -1)
 			i = url.lastIndexOf("/");
 		if (i == -1)
 			return null;
 		return url.substring(0, i + 1);
 	}
 
 	public String resolvePath(String s) {
 		if (FileUtilities.isRemote(s))
 			return s;
 		String lc = s.toLowerCase();
 		if (lc.startsWith("mailto:"))
 			return s;
 		if (lc.startsWith("script:"))
 			return s;
 		if (lc.indexOf("client=mw") != -1) {
 			// this means that when refering to a web service, you should use an address relative to its
 			// context root, regardless of where the current directory is.
 			return Modeler.getContextRoot() + s;
 		}
 		// link to concord search engine
 		if (lc.startsWith("/cgi-bin/"))
 			return "http://www.concord.org" + s;
 		if (isRemote() && FileUtilities.isRelative(s) && pageURI != null) { // resolving network paths
 			String s2 = s.replace('\\', '/');
 			s2 = FileUtilities.httpEncode(s2);
 			return pageURI.resolve(s2).toString();
 		}
 		// resolving local paths
 		if (OS.startsWith("Windows")) {
 			if (s.charAt(1) == ':')
 				return s;
 		}
 		else {
 			if (s.charAt(0) == '/')
 				return s;
 		}
 		int fromIndex = 0, count = 0;
 		while (fromIndex > -1) {
 			fromIndex = s.indexOf("..", fromIndex);
 			if (fromIndex >= 0) {
 				fromIndex += 2;
 				count++;
 			}
 		}
 		String s2 = s.replace('\\', '/');
 		String[] s3 = s2.split("/");
 		ArrayList<String> list = new ArrayList<String>();
 		for (int k = 0; k < s3.length; k++) {
 			if (!s3[k].trim().equals("") && !s3[k].equals("."))
 				list.add(s3[k]);
 		}
 		String[] s1 = new String[list.size()];
 		for (int k = 0; k < s1.length; k++) {
 			s1[k] = list.get(k);
 		}
 		String b = getPathBase(pageAddress);
 		if (b == null || b.equals("")) {
 			return s;
 		}
 		if (FileUtilities.isRemote(b)) {
 			String[] b1 = b.substring(7).split("/");
 			String path = "http://";
 			int i;
 			for (i = 0; i < b1.length - count; i++)
 				path += b1[i] + "/";
 			for (i = count; i < s1.length - 1; i++)
 				path += s1[i] + "/";
 			path += s1[s1.length - 1];
 			return path;
 		}
 		String b2 = b.replace('\\', '/');
 		String[] b1 = b2.split("/");
 		String path = "";
 		int i;
 		String fs = System.getProperty("file.separator");
 		for (i = 0; i < b1.length - count; i++)
 			path += b1[i] + fs;
 		for (i = count; i < s1.length - 1; i++)
 			path += s1[i] + fs;
 		path += s1[s1.length - 1];
 		return path;
 	}
 
 	private String getElementName(int location) {
 		StyledDocument doc = getStyledDocument();
 		if (doc != null) {
 			Element elem = doc.getCharacterElement(location);
 			if (elem != null)
 				return elem.getName();
 		}
 		return null;
 	}
 
 	private Icon getIcon(int location) {
 		StyledDocument doc = getStyledDocument();
 		if (doc != null) {
 			Element elem = doc.getCharacterElement(location);
 			if (elem != null) {
 				AttributeSet a = elem.getAttributes();
 				Object obj = a.getAttribute(StyleConstants.IconAttribute);
 				if (obj instanceof Icon) {
 					return (Icon) obj;
 				}
 				obj = a.getAttribute(StyleConstants.ComponentAttribute);
 				if (obj instanceof IconWrapper) {
 					Icon icon = ((IconWrapper) obj).getIcon();
 					if (!(icon instanceof LineIcon))
 						return icon;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Detect whether the specified point is on an embedded icon. NOTE: The default Java implementation for this does
 	 * not divide properly the space between an icon and character.
 	 */
 	protected Icon isIcon(Point p) {
 		Icon image = null;
 		int pos = viewToModel(p);
 		if (pos >= 0) {
 			image = getIcon(pos);
 			if (pos == 0)
 				return image;
 			if (image == null) {
 				String name = getElementName(pos - 1);
 				if (name.equals("icon") || name.equals("component")) {
 					Icon icon = getIcon(pos - 1);
 					if (icon != null) {
 						Rectangle rect = null;
 						try {
 							rect = modelToView(pos - 1);
 						}
 						catch (BadLocationException e) {
 							e.printStackTrace();
 						}
 						if (rect != null) {
 							rect.width = icon.getIconWidth();
 							rect.height = icon.getIconHeight();
 							if (rect.contains(p))
 								image = icon;
 						}
 					}
 				}
 			}
 			else {
 				// detect if the previous element is also an icon
 				Icon icon = getIcon(pos - 1);
 				if (icon != null) {
 					Rectangle rect = null;
 					try {
 						rect = modelToView(pos - 1);
 					}
 					catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 					// detect if the click point falls in the previous icon. If so, return the previous icon.
 					if (rect != null) {
 						rect.width = icon.getIconWidth();
 						rect.height = icon.getIconHeight();
 						if (rect.contains(p))
 							return icon;
 					}
 				}
 			}
 		}
 		return image;
 	}
 
 	private void setLinkParam(AttributeSet a) {
 
 		if (linkParam == null)
 			linkParam = new HyperlinkParameter();
 		else linkParam.reset();
 		if (a == null)
 			return;
 
 		Object o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_RESIZABLE);
 		if (o instanceof Boolean) {
 			linkParam.setResizable(((Boolean) o).booleanValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setResizable(Boolean.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setResizable(true);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_TOOLBAR);
 		if (o instanceof Boolean) {
 			linkParam.setToolbar(((Boolean) o).booleanValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setToolbar(Boolean.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setToolbar(true);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_MENUBAR);
 		if (o instanceof Boolean) {
 			linkParam.setMenubar(((Boolean) o).booleanValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setMenubar(Boolean.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setMenubar(true);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_STATUSBAR);
 		if (o instanceof Boolean) {
 			linkParam.setStatusbar(((Boolean) o).booleanValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setStatusbar(Boolean.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setStatusbar(true);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_LEFT);
 		if (o instanceof Integer) {
 			linkParam.setLeft(((Integer) o).intValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setLeft(Integer.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setLeft(0);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_TOP);
 		if (o instanceof Integer) {
 			linkParam.setTop(((Integer) o).intValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setTop(Integer.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setTop(0);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_WIDTH);
 		if (o instanceof Integer) {
 			linkParam.setWidth(((Integer) o).intValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setWidth(Integer.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setWidth(0);
 		}
 
 		o = a.getAttribute(HyperlinkParameter.ATTRIBUTE_HEIGHT);
 		if (o instanceof Integer) {
 			linkParam.setHeight(((Integer) o).intValue());
 		}
 		else if (o instanceof String) {
 			try {
 				linkParam.setHeight(Integer.valueOf((String) o));
 			}
 			catch (Exception e) {
 			}
 		}
 		else {
 			linkParam.setHeight(0);
 		}
 
 	}
 
 	private String getHyperlink(int location) {
 		StyledDocument doc = getStyledDocument();
 		if (doc != null) {
 			Element elem = doc.getCharacterElement(location);
 			if (elem != null) {
 				AttributeSet a = elem.getAttributes();
 				targetIsBlank = "_blank".equals(a.getAttribute(HTML.Attribute.TARGET));
 				if (targetIsBlank)
 					setLinkParam(a);
 				return (String) a.getAttribute(HTML.Attribute.HREF);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Detect if there is a hyperlink associated with the specified point. NOTE: The default Java implementation for
 	 * this does not divide properly the space between an image and character, and the space between two adjacent
 	 * images. We have corrected this problem for hyperlinked images to work.
 	 */
 	protected String isHyperlinked(Point p) {
 		String href = null;
 		int pos = viewToModel(p);
 		if (pos >= 0) {
 			Rectangle rect = null;
 			/*
 			 * FIXME: viewToModel() returns the NEAREST representative location in the model. If the element is indented
 			 * or non-left-aligned, there will be a problem. try { rect=modelToView(pos); } catch(BadLocationException
 			 * e){ e.printStackTrace(); } if(rect!=null){ if(!getElementName(pos).equals("icon")){ if(p.x<rect.x-2)
 			 * return null; } }
 			 */
 			href = getHyperlink(pos);
 			if (pos == 0)
 				return href;
 			if (href == null) {
 				href = getHyperlink(pos - 1);
 				if (href != null) {
 					String name = getElementName(pos - 1);
 					if (name.equals("icon") || name.equals("component")) {
 						try {
 							rect = modelToView(pos - 1);
 						}
 						catch (BadLocationException e) {
 							e.printStackTrace();
 						}
 						if (rect != null) {
 							Icon icon = getIcon(pos - 1);
 							if (icon != null) {
 								rect.width = icon.getIconWidth();
 								rect.height = icon.getIconHeight();
 								if (!rect.contains(p))
 									href = null;
 							}
 						}
 					}
 					else {
 						href = null;
 					}
 				}
 			}
 			else {
 				// detect if the previous element is an icon
 				Icon icon2 = getIcon(pos - 1);
 				if (icon2 != null) {
 					try {
 						rect = modelToView(pos - 1);
 					}
 					catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 					// detect if the click point falls in the previous icon. If so, return the previous hyperlink on it
 					if (rect != null) {
 						rect.width = icon2.getIconWidth();
 						rect.height = icon2.getIconHeight();
 						if (rect.contains(p))
 							return getHyperlink(pos - 1);
 					}
 				}
 			}
 		}
 		return href;
 	}
 
 	private void invertSelectedImages() {
 		if (selectedImages.isEmpty())
 			return;
 		synchronized (selectedImages) {
 			for (Icon icon : selectedImages) {
 				if (icon instanceof ImageIcon) {
 					ImageIcon imageIcon = (ImageIcon) icon;
 					BufferedImage bi = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
 							BufferedImage.TYPE_INT_RGB);
 					Graphics bg = bi.getGraphics();
 					bg.setColor(Color.white);
 					bg.fillRect(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight());
 					imageIcon.paintIcon(null, bg, 0, 0);
 					imageIcon.setImage(ImageOp.INVERT.filter(bi, null));
 				}
 				else if (icon instanceof LineIcon) {
 					((LineIcon) icon).invert();
 				}
 			}
 		}
 		repaint();
 	}
 
 	private void findSelectedImages() {
 		selectedImages.clear();
 		if (getSelectedText() == null)
 			return;
 		Element elem = null;
 		AttributeSet attributes = null;
 		StyledDocument doc = getStyledDocument();
 		Enumeration e = null;
 		Object attr = null;
 		int start = getSelectionStart();
 		int end = getSelectionEnd();
 		for (int i = start; i < end; i++) {
 			elem = doc.getCharacterElement(i);
 			attributes = elem.getAttributes();
 			e = attributes.getAttributeNames();
 			while (e.hasMoreElements()) {
 				attr = attributes.getAttribute(e.nextElement());
 				if (attr instanceof Icon)
 					selectedImages.add((Icon) attr);
 				else if (attr instanceof IconWrapper)
 					selectedImages.add(((IconWrapper) attr).getIcon());
 			}
 		}
 	}
 
 	private void setComponentSelection(boolean b) {
 		if (selectedComponents.isEmpty())
 			return;
 		synchronized (selectedComponents) {
 			for (JComponent c : selectedComponents) {
 				if (c instanceof Embeddable)
 					((Embeddable) c).setMarked(b);
 				c.repaint();
 			}
 		}
 	}
 
 	private void findSelectedComponents() {
 		selectedComponents.clear();
 		if (getSelectedText() == null)
 			return;
 		Element elem = null;
 		AttributeSet attributes = null;
 		StyledDocument doc = getStyledDocument();
 		Enumeration e = null;
 		Object attr = null;
 		int start = getSelectionStart();
 		int end = getSelectionEnd();
 		for (int i = start; i < end; i++) {
 			elem = doc.getCharacterElement(i);
 			attributes = elem.getAttributes();
 			e = attributes.getAttributeNames();
 			while (e.hasMoreElements()) {
 				attr = attributes.getAttribute(e.nextElement());
 				if (attr instanceof JComponent)
 					selectedComponents.add((JComponent) attr);
 			}
 		}
 	}
 
 	/**
 	 * return the paragraph elements included in the current selection. A paragraph is considered included if a part of
 	 * it is selected.
 	 */
 	protected Element[] getSelectedParagraphs() {
 		StyledDocument doc = getStyledDocument();
 		String t = getSelectedText();
 		if (t == null) {
 			Element[] e = new Element[1];
 			e[0] = doc.getParagraphElement(getCaretPosition());
 			return e;
 		}
 		ArrayList<Element> list = new ArrayList<Element>();
 		int start = getSelectionStart();
 		int end = getSelectionEnd();
 		int length = doc.getLength();
 		Element elem = doc.getParagraphElement(start);
 		list.add(elem);
 		int offset = 0;
 		for (int n = 0; n < t.length(); n++) {
 			if (t.charAt(n) == '\n') {
 				if (n == 0) {
 					try {
 						if (doc.getText(start - 1 < 0 ? 0 : start - 1, 1).charAt(0) != '\n') {
 							elem = doc.getParagraphElement(start + 1);
 							list.add(elem);
 						}
 					}
 					catch (BadLocationException ble) {
 						ble.printStackTrace();
 					}
 				}
 				else if (n == t.length() - 1) {
 					try {
 						if (doc.getText(end + 1 > length - 1 ? length - 1 : end + 1, 1).charAt(0) != '\n') {
 							elem = doc.getParagraphElement(end);
 							list.add(elem);
 						}
 					}
 					catch (BadLocationException ble) {
 						ble.printStackTrace();
 					}
 				}
 				else {
 					if (t.charAt(n + 1) != '\n') {
 						offset = start + n + 1;
 						if (offset < doc.getLength()) {
 							elem = doc.getParagraphElement(offset);
 							list.add(elem);
 						}
 					}
 				}
 			}
 		}
 		Element[] e = new Element[list.size()];
 		for (int i = 0; i < e.length; i++)
 			e[i] = list.get(i);
 		return e;
 	}
 
 	public void deselect() {
 		if (getSelectedText() == null)
 			return;
 		select(getSelectionEnd(), getSelectionEnd());
 		unhighlightEmbedded();
 	}
 
 	private void unhighlightEmbedded() {
 		if (getSelectedText() == null) {
 			if (selectedImagesInverted) {
 				invertSelectedImages();
 				selectedImagesInverted = false;
 			}
 			if (componentSelected) {
 				setComponentSelection(false);
 				componentSelected = false;
 			}
 		}
 	}
 
 	private void highlightEmbedded() {
 		if (selectedImagesInverted) {
 			invertSelectedImages();
 			selectedImagesInverted = false;
 		}
 		if (componentSelected) {
 			setComponentSelection(false);
 			componentSelected = false;
 		}
 		findSelectedImages();
 		if (!selectedImages.isEmpty()) {
 			if (!selectedImagesInverted) {
 				invertSelectedImages();
 				selectedImagesInverted = true;
 			}
 		}
 		else {
 			selectedImagesInverted = false;
 		}
 		findSelectedComponents();
 		if (!selectedComponents.isEmpty()) {
 			if (!componentSelected) {
 				setComponentSelection(true);
 				componentSelected = true;
 			}
 		}
 		else {
 			componentSelected = false;
 		}
 	}
 
 	void createNewPage() {
 		clearAllContent();
 		setAddress("Untitled.cml");
 		setTitle(null);
 		if (navigator != null)
 			navigator.storeLocation(UNKNOWN_LOCATION);
 		notifyPageListeners(new PageEvent(this, PageEvent.NEW_PAGE));
 	}
 
 	private void saveAndCreateNewPage() {
 		if (isWriting())
 			return;
 		if (!pageAddress.equals("Untitled.cml") && !isRemote()) {
 			final File file = new File(pageAddress);
 			if (!validateFileName(file))
 				return;
 			if (EventQueue.isDispatchThread())
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			new SwingWorker("Page-saving thread") {
 				public Object construct() {
 					notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_OVERWRITE_BEGIN));
 					SaveComponentStateReminder.setEnabled(false);
 					synchronized (lock) {
 						saveTo(file);
 					}
 					return file;
 				}
 
 				public void finished() {
 					isWriting = false;
 					notifyPageListeners(new PageEvent(Page.this, PageEvent.PAGE_OVERWRITE_END));
 					createNewPage();
 					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				}
 			}.start();
 		}
 	}
 
 	/**
 	 * get the last-modified time and content length of this page. The LMT is the number of milliseconds since January
 	 * 1, 1970 GMT.
 	 */
 	private long[] getLastModifiedAndContentLength() {
 		long[] t = new long[2];
 		t[0] = 0L;
 		t[1] = 0L;
 		if (isRemote()) {
 			File file = ConnectionManager.sharedInstance().getLocalCopy(pageAddress);
 			if (file == null || !file.exists()) {
 				URL url = null;
 				try {
 					url = new URL(pageAddress);
 				}
 				catch (MalformedURLException e) {
 					e.printStackTrace();
 					showErrorMessage(e);
 				}
 				if (url != null) {
 					if (ConnectionManager.isDynamicalContent(pageAddress))
 						return t;
 					HttpURLConnection conn = ConnectionManager.getConnection(url);
 					if (conn != null) {
 						t[0] = conn.getLastModified();
 						t[1] = conn.getContentLength();
 					}
 				}
 			}
 			else {
 				t[0] = file.lastModified();
 				t[1] = file.length();
 			}
 		}
 		else {
 			t[0] = new File(pageAddress).lastModified();
 			t[1] = new File(pageAddress).length();
 		}
 		return t;
 	}
 
 	/**
 	 * return the bullet type of the current selected paragraphs. If there are more than one paragraph selected and
 	 * their icon types are different, or there is no paragraph selected, return -1.
 	 * 
 	 * @see org.concord.modeler.text.BulletIcon
 	 */
 	public int getBulletType() {
 		Element[] elem = getSelectedParagraphs();
 		if (elem == null || elem.length == 0)
 			return -1;
 		StyledDocument doc = getStyledDocument();
 		Element head = null;
 		Icon[] icon = new Icon[elem.length];
 		for (int i = 0; i < elem.length; i++) {
 			head = doc.getCharacterElement(elem[i].getStartOffset());
 			icon[i] = StyleConstants.getIcon(head.getAttributes());
 			if (!(icon[i] instanceof BulletIcon))
 				return -1;
 		}
 		if (icon.length == 1)
 			return ((BulletIcon) icon[0]).getType();
 		for (int i = 1; i < icon.length; i++) {
 			if (((BulletIcon) icon[i]).getType() != ((BulletIcon) icon[0]).getType())
 				return -1;
 		}
 		return ((BulletIcon) icon[0]).getType();
 	}
 
 	public void setBulletType(int type) {
 
 		Element[] elem = getSelectedParagraphs();
 		if (elem == null)
 			return;
 
 		float currentIndent = 0.0f;
 		AttributeSet as = null;
 		StyledDocument doc = getStyledDocument();
 		Element head = null;
 		Icon icon = null;
 		for (Element e : elem) {
 
 			as = e.getAttributes();
 			currentIndent = StyleConstants.getLeftIndent(as);
 			head = doc.getCharacterElement(e.getStartOffset());
 			icon = StyleConstants.getIcon(head.getAttributes());
 
 			if (!(icon instanceof BulletIcon)) { // this includes null
 				Icon bi = BulletIcon.get(type);
 				if (bi != null) {
 					SimpleAttributeSet sas = new SimpleAttributeSet(as);
 					StyleConstants.setLeftIndent(sas, currentIndent + INDENT_STEP);
 					doc.setParagraphAttributes(e.getStartOffset(), e.getEndOffset() - e.getStartOffset(), sas, false);
 					as = new SimpleAttributeSet();
 					StyleConstants.setIcon((MutableAttributeSet) as, bi);
 					try {
 						doc.insertString(e.getStartOffset(), "  ", null);
 						doc.insertString(e.getStartOffset(), " ", as);
 					}
 					catch (BadLocationException ble) {
 						ble.printStackTrace();
 					}
 				}
 			}
 			else {
 				Icon bi = BulletIcon.get(type);
 				if (bi == null) {
 					SimpleAttributeSet sas = new SimpleAttributeSet(as);
 					StyleConstants
 							.setLeftIndent(sas, currentIndent - INDENT_STEP > 0 ? currentIndent - INDENT_STEP : 0);
 					doc.setParagraphAttributes(e.getStartOffset(), e.getEndOffset() - e.getStartOffset(), sas, false);
 					try {
 						doc.remove(e.getStartOffset(), 3);
 					}
 					catch (BadLocationException ble) {
 						ble.printStackTrace();
 					}
 				}
 				else {
 					as = new SimpleAttributeSet();
 					StyleConstants.setIcon((MutableAttributeSet) as, bi);
 					try {
 						doc.remove(e.getStartOffset(), 1);
 						doc.insertString(e.getStartOffset(), " ", as);
 					}
 					catch (BadLocationException ble) {
 						ble.printStackTrace();
 					}
 				}
 			}
 
 		}
 
 	}
 
 	/**
 	 * automatically resize those components whose sizes are relative to the page's size.
 	 */
 	public void autoResizeComponents() {
 		if (getDocument().getLength() <= 0)
 			return;
 		boolean changed = saveReminder.isChanged();
 		boolean b = false;
 		List<Embeddable> list = getEmbeddedComponents();
 		if (list != null && !list.isEmpty()) {
 			AutoResizable t;
 			int w, h;
 			boolean b2;
 			synchronized (list) {
 				for (Embeddable e : list) {
 					if (e instanceof IconWrapper) {
 						IconWrapper wrapper = (IconWrapper) e;
 						Icon icon = wrapper.getIcon();
 						if (icon instanceof LineIcon) {
 							LineIcon li = (LineIcon) icon;
 							float width = li.getWidth();
 							if (width <= 1.05f) {
 								Dimension dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
 								wrapper.setPreferredSize(dim);
 								wrapper.validate();
 								b = true;
 							}
 						}
 					}
 					else if (e instanceof AutoResizable) {
 						t = (AutoResizable) e;
 						if (!(t instanceof JComponent))
 							continue;
 						JComponent c = (JComponent) t;
 						b2 = false;
 						if (t.isWidthRelative()) {
 							w = (int) (t.getWidthRatio() * getWidth());
 							b = true;
 							b2 = true;
 						}
 						else {
 							w = c.getWidth() == 0 ? c.getPreferredSize().width : c.getWidth();
 						}
 						if (t.isHeightRelative()) {
 							h = (int) (t.getHeightRatio() * getHeight());
 							b = true;
 							b2 = true;
 						}
 						else {
 							h = c.getHeight() == 0 ? c.getPreferredSize().height : c.getHeight();
 						}
 						if (b2) {
 							Dimension d = new Dimension(w, h);
 							c.setMaximumSize(d);
 							c.setPreferredSize(d);
 							c.validate();
 						}
 					}
 				}
 			}
 		}
 		if (b)
 			reload();
 		saveReminder.setChanged(changed);
 	}
 
 	// if the page has been changed, return true; otherwise, return false;
 	private boolean askToCreateNewPage() {
 		if (!isRemote() && isEditable()) {
 			if (saveReminder.isChanged()) {
 				int opt = isRemote() ? JOptionPane.NO_OPTION : saveReminder.showConfirmDialog(this, FileUtilities
 						.getFileName(pageAddress));
 				boolean newAction = false;
 				switch (opt) {
 				case JOptionPane.YES_OPTION: // save page first and continue to empty the page
 					saveAndCreateNewPage();
 					newAction = false;
 					break;
 				case JOptionPane.NO_OPTION: // no save, continue to load new page
 					newAction = true;
 					break;
 				case JOptionPane.CANCEL_OPTION: // cancel this action
 					newAction = false;
 					break;
 				default:
 					newAction = true;
 				}
 				if (newAction) {
 					saveReminder.setChanged(false);
 					createNewPage();
 				}
 				return true;
 			}
 			createNewPage();
 		}
 		else {
 			createNewPage();
 		}
 		return false;
 	}
 
 	/**
 	 * summarize the current page group, and put the information into a map.
 	 * 
 	 * @see org.concord.modeler.text.Page#createReportForPageGroup
 	 */
 	public Map<String, Object> prepareReportForPageGroup(PageNameGroup png) {
 		return new MultiplePageReportBuilder(this).prepare(reportTitle, png);
 	}
 
 	/**
 	 * create a report page on multiple pages based on the information passed through the map.
 	 * 
 	 * @see org.concord.modeler.text.Page#prepareReportForPageGroup
 	 */
 	public void createReportForPageGroup(Map<String, Object> map, PageNameGroup png) {
 		new MultiplePageReportFormatter(this).format(map, png);
 	}
 
 	/**
 	 * summarize the current page, and put the information into a map.
 	 * 
 	 * @see org.concord.modeler.text.Page#createReport
 	 */
 	public Map<String, Object> prepareReport() {
 		return new SinglePageReportBuilder(this).prepare();
 	}
 
 	/**
 	 * create a report page based on the information passed through the map.
 	 * 
 	 * @see org.concord.modeler.text.Page#prepareReport
 	 */
 	public void createReport(Map<String, Object> map) {
 		new SinglePageReportFormatter(this).format(map);
 	}
 
 	public void inputTitle() {
 		new TitleInputDialog(this).setVisible(true);
 	}
 
 	public void inputBackgroundSound() {
 		new SoundInputDialog(this).setVisible(true);
 	}
 
 	private void createActions() {
 
 		boolean mac = OS.startsWith("Mac");
 
 		insertAtomContainerAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				insertModel(AtomContainer.getCompatibleName());
 			}
 		};
 		insertAtomContainerAction.putValue(Action.NAME, INSERT_ATOM_CONTAINER);
 		insertAtomContainerAction.putValue(Action.SHORT_DESCRIPTION, "Insert " + INSERT_ATOM_CONTAINER);
 		insertAtomContainerAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
 				"images/MolecularModel.gif")));
 
 		insertGBContainerAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				insertModel(GBContainer.getCompatibleName());
 			}
 		};
 		insertGBContainerAction.putValue(Action.NAME, INSERT_GB_CONTAINER);
 		insertGBContainerAction.putValue(Action.SHORT_DESCRIPTION, "Insert " + INSERT_GB_CONTAINER);
 		insertGBContainerAction
 				.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("images/GBModel.gif")));
 
 		insertChemContainerAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				insertModel(ChemContainer.getCompatibleName());
 			}
 		};
 		insertChemContainerAction.putValue(Action.NAME, INSERT_CHEM_CONTAINER);
 		insertChemContainerAction.putValue(Action.SHORT_DESCRIPTION, "Insert " + INSERT_CHEM_CONTAINER);
 		insertChemContainerAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
 				"images/ChemReaction.gif")));
 
 		insertProsynContainerAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				insertProteinSynthesisModel();
 			}
 		};
 		insertProsynContainerAction.putValue(Action.NAME, INSERT_PROSYN_CONTAINER);
 		insertProsynContainerAction.putValue(Action.SHORT_DESCRIPTION, "Insert " + INSERT_PROSYN_CONTAINER);
 		insertProsynContainerAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
 				"images/Prosyn.gif")));
 
 		insertFileAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				importFile();
 			}
 		};
 		insertFileAction.putValue(Action.NAME, "File");
 		insertFileAction.putValue(Action.SHORT_DESCRIPTION, "Insert a file");
 
 		refreshAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				rememberViewPosition = true;
 				visit(pageAddress);
 			}
 
 			public String toString() {
 				return (String) getValue(Action.SHORT_DESCRIPTION);
 			}
 		};
 		refreshAction.putValue(Action.NAME, REFRESH);
 		refreshAction.putValue(Action.SHORT_DESCRIPTION, "Reload page");
 		refreshAction.putValue(Action.SMALL_ICON, new ImageIcon(Page.class.getResource("images/Refresh.gif")));
 		refreshAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
 		refreshAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.META_MASK,
 				true) : KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, true));
 
 		newAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				askToCreateNewPage();
 				inputTitle();
 			}
 		};
 		newAction.putValue(Action.NAME, "New Blank Page");
 		newAction.putValue(Action.SHORT_DESCRIPTION, "Create a new blank page");
 		newAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke
 				.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				KeyEvent.CTRL_MASK, true));
 
 		scriptAction = new DefaultAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (ModelerUtilities.stopFiring(e))
 					return;
 				AbstractButton button = (AbstractButton) e.getSource();
 				Object script = button.getClientProperty("script");
 				if (script instanceof String) {
 					processHyperlink((String) script);
 				}
 				else { // backward compatible
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Page.this),
 							"No script has been set.");
 				}
 			}
 		};
 		scriptAction.putValue(Action.NAME, "Script");
 		scriptAction.putValue(Action.SHORT_DESCRIPTION, "Script");
 
 		closeAction = new DefaultAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (ModelerUtilities.stopFiring(e))
 					return;
 				notifyPageListeners(new PageEvent(Page.this, PageEvent.CLOSE_CURRENT_WINDOW));
 			}
 		};
 		closeAction.putValue(Action.NAME, CLOSE_PAGE);
 		closeAction.putValue(Action.SHORT_DESCRIPTION, "Close this page");
 		closeAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_MASK)
 				: KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
 
 		openAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				openPage();
 			}
 		};
 		openAction.putValue(Action.NAME, OPEN_PAGE);
 		openAction.putValue(Action.SHORT_DESCRIPTION, "Open a page");
 		openAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK,
 				true) : KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK, true));
 
 		saveAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				savePage(false, null);
 			}
 		};
 		saveAction.putValue(Action.NAME, SAVE_PAGE);
 		saveAction.putValue(Action.SHORT_DESCRIPTION, "Save this page");
 		saveAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK,
 				true) : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK, true));
 
 		saveAsAction = new DefaultAction() {
 			public void actionPerformed(ActionEvent e) {
 				savePage(true, null);
 			}
 		};
 		saveAsAction.putValue(Action.NAME, SAVE_PAGE_AS);
 		saveAsAction.putValue(Action.SHORT_DESCRIPTION, "Save this page as");
 		saveAsAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK
 				| KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK
 				| KeyEvent.SHIFT_MASK, true));
 
 		htmlizeAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (componentPool.isUsed()) {
 					if (actionNotifier != null) {
 						actionNotifier.setParentComponent(Page.this);
 						actionNotifier.show(ActionNotifier.HTML_CONVERSION_WARNING);
 					}
 				}
 				convertToHTML();
 			}
 		};
 		htmlizeAction.putValue(Action.NAME, HTML_CONVERSION);
 		htmlizeAction.putValue(Action.SHORT_DESCRIPTION, "Convert to HTML format");
 
 		colorBarAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (colorBarDialog == null)
 					colorBarDialog = new ColorBarDialog(Page.this);
 				LineIcon lineIcon = new LineIcon(Page.this);
 				colorBarDialog.setColorBar(lineIcon);
 				colorBarDialog.setVisible(true);
 				if (colorBarDialog.getOption() == JOptionPane.CANCEL_OPTION)
 					return;
 				insertWrappedIcon(lineIcon);
 			}
 		};
 		colorBarAction.putValue(Action.NAME, "Color Bar");
 		colorBarAction.putValue(Action.SHORT_DESCRIPTION, "Insert a color bar");
 
 		insertComponentAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				Object source = e.getSource();
 				if (source instanceof AbstractButton) {
 					importComponent(((AbstractButton) source).getName());
 				}
 			}
 		};
 		insertComponentAction.putValue(Action.NAME, INSERT_COMPONENT);
 
 		printPreviewAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				AbstractButton ab = (AbstractButton) e.getSource();
 				ab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				if (printPreview == null)
 					printPreview = new PrintPreview(Page.this);
 				printPreview.showPreviewScreen();
 				ab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			}
 		};
 		printPreviewAction.putValue(Action.NAME, "Preview");
 		printPreviewAction.putValue(Action.SHORT_DESCRIPTION, "Preview this page before printing");
 
 		pageSetupAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				AbstractButton ab = (AbstractButton) e.getSource();
 				ab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				PrintPreview.invokePageFormatDialog();
 				ab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			}
 		};
 		pageSetupAction.putValue(Action.NAME, "Page Setup");
 		pageSetupAction.putValue(Action.SHORT_DESCRIPTION, "Set up printing format");
 		pageSetupAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.META_MASK
 				| KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK
 				| KeyEvent.SHIFT_MASK, true));
 
 		printAction = new DefaultAction() {
 			public void actionPerformed(ActionEvent e) {
 				AbstractButton ab = (AbstractButton) e.getSource();
 				ab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				if (printPreview == null)
 					printPreview = new PrintPreview(Page.this);
 				printPreview.print();
 				ab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			}
 		};
 		printAction.putValue(Action.NAME, "Print");
 		printAction.putValue(Action.SHORT_DESCRIPTION, "Print this page");
 		printAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.META_MASK,
 				true) : KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK, true));
 
 		selectAllAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				requestFocusInWindow();
 				selectAll();
 				highlightEmbedded();
 			}
 		};
 		selectAllAction.putValue(Action.NAME, DefaultEditorKit.selectAllAction);
 		selectAllAction.putValue(Action.SHORT_DESCRIPTION, DefaultEditorKit.selectAllAction);
 		selectAllAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke
 				.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_A,
 				KeyEvent.CTRL_MASK));
 
 		symbolAction = new DefaultAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (symbolDialog == null)
 					symbolDialog = new SymbolDialog(Page.this);
 				symbolDialog.setVisible(true);
 			}
 		};
 		symbolAction.putValue(Action.NAME, "Symbol (Unicode Only)");
 		symbolAction.putValue(Action.SHORT_DESCRIPTION, "Insert a symbol");
 
 		hyperlinkAction = new DefaultAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (hyperlinkDialog == null)
 					hyperlinkDialog = new HyperlinkDialog(Page.this);
 				hyperlinkDialog.setCurrentValues();
 				hyperlinkDialog.setVisible(true);
 			}
 		};
 		hyperlinkAction.putValue(Action.NAME, "Hyperlink");
 		hyperlinkAction.putValue(Action.SHORT_DESCRIPTION, "Add a hyperlink to the selected area");
 		hyperlinkAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("images/Hyperlink.gif")));
 		hyperlinkAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_K,
 				KeyEvent.META_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK, true));
 
 		fontAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (fontDialog == null)
 					fontDialog = new FontDialog(Page.this);
 				AttributeSet a = ((DefaultStyledDocument) Page.this.getDocument()).getCharacterElement(
 						getCaretPosition() - 1).getAttributes();
 				fontDialog.setAttributes(a);
 				fontDialog.setVisible(true);
 				if (fontDialog.getOption() == JOptionPane.OK_OPTION) {
 					setCharacterAttributes(fontDialog.getAttributes(), false);
 					saveReminder.setChanged(true);
 					notifyPageListeners(new PageEvent(Page.this, PageEvent.FONT_CHANGED));
 				}
 			}
 		};
 		fontAction.putValue(Action.NAME, "Font");
 		fontAction.putValue(Action.SHORT_DESCRIPTION, "Font");
 		fontAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("images/Font.gif")));
 		fontAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_MASK
 				| KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK
 				| KeyEvent.SHIFT_MASK, true));
 
 		paragraphAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (paragraphDialog == null)
 					paragraphDialog = new ParagraphDialog(Page.this);
 				AttributeSet a = ((DefaultStyledDocument) getDocument()).getParagraphElement(
 						Page.this.getCaretPosition()).getAttributes();
 				paragraphDialog.setAttributes(a);
 				paragraphDialog.setVisible(true);
 				if (paragraphDialog.getOption() == JOptionPane.OK_OPTION) {
 					setParagraphAttributes(paragraphDialog.getAttributes(), false);
 					saveReminder.setChanged(true);
 				}
 			}
 		};
 		paragraphAction.putValue(Action.NAME, "Paragraph");
 		paragraphAction.putValue(Action.SHORT_DESCRIPTION, "Change paragraph");
 		paragraphAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("images/Paragraph.gif")));
 		paragraphAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_MASK
 				| KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK
 				| KeyEvent.SHIFT_MASK, true));
 
 		insertBulletAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (bulletDialog == null)
 					bulletDialog = new BulletDialog(Page.this);
 				bulletDialog.setBulletType(getBulletType());
 				bulletDialog.setVisible(true);
 			}
 		};
 		insertBulletAction.putValue(Action.NAME, "Bullet");
 		insertBulletAction.putValue(Action.SHORT_DESCRIPTION, "Insert Bullet");
 		insertBulletAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("images/Bullet.gif")));
 		insertBulletAction.putValue(Action.ACCELERATOR_KEY, mac ? KeyStroke.getKeyStroke(KeyEvent.VK_B,
 				KeyEvent.META_MASK | KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_B,
 				KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, true));
 
 		propertiesAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				new SwingWorker() {
 					public Object construct() {
 						return getLastModifiedAndContentLength();
 					}
 
 					public void finished() {
 						long[] info = (long[]) getValue();
 						if (info == null)
 							return;
 						if (info[0] > 0L)
 							properties.put("Last modified", new Date(info[0]));
 						if (info[1] > 0L)
 							properties.put("Size", new Long(info[1]));
 						if (propertiesDialog == null)
 							propertiesDialog = new PagePropertiesDialog(Page.this);
 						propertiesDialog.setCurrentValues();
 						propertiesDialog.pack();
 						propertiesDialog.setVisible(true);
 					}
 				}.start();
 			}
 		};
 		propertiesAction.putValue(Action.NAME, SET_PROPERTIES);
 		propertiesAction.putValue(Action.SHORT_DESCRIPTION, "Set properties of this page");
 		propertiesAction.putValue(Action.SMALL_ICON, IconPool.getIcon("properties"));
 
 	}
 
 	class UndoAction extends AbstractAction {
 		public UndoAction() {
 			super(UNDO);
 			putValue(NAME, UNDO);
 			putValue(ACCELERATOR_KEY, OS.startsWith("Mac") ? KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK)
 					: KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (undoManager.canUndo()) {
 				try {
 					undoManager.undo();
 				}
 				catch (CannotUndoException ex) {
 					ex.printStackTrace();
 				}
 			}
 			updateState();
 			((RedoAction) getAction(REDO)).updateState();
 		}
 
 		protected void updateState() {
 			if (undoManager.canUndo()) {
 				setEnabled(true);
 				putValue(SHORT_DESCRIPTION, undoManager.getUndoPresentationName());
 			}
 			else {
 				setEnabled(false);
 				putValue(SHORT_DESCRIPTION, "Undo");
 			}
 		}
 	}
 
 	class RedoAction extends AbstractAction {
 		public RedoAction() {
 			super(REDO);
 			putValue(NAME, REDO);
 			putValue(ACCELERATOR_KEY, OS.startsWith("Mac") ? KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK
 					| KeyEvent.SHIFT_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (undoManager.canRedo()) {
 				try {
 					undoManager.redo();
 				}
 				catch (CannotRedoException ex) {
 					ex.printStackTrace();
 				}
 			}
 			updateState();
 			((UndoAction) getAction(UNDO)).updateState();
 		}
 
 		protected void updateState() {
 			if (undoManager.canRedo()) {
 				setEnabled(true);
 				putValue(SHORT_DESCRIPTION, undoManager.getRedoPresentationName());
 			}
 			else {
 				setEnabled(false);
 				putValue(SHORT_DESCRIPTION, "Redo");
 			}
 		}
 	}
 
 	private abstract class DefaultAction extends AbstractAction {
 		public String toString() {
 			return (String) getValue(SHORT_DESCRIPTION);
 		}
 	}
 
 }
