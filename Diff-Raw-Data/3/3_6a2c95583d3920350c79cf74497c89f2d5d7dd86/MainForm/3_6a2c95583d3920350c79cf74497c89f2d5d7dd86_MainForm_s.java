 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.applications.rainbow.lib.EncodingItem;
 import net.sf.okapi.applications.rainbow.lib.EncodingManager;
 import net.sf.okapi.applications.rainbow.lib.FilterAccess;
 import net.sf.okapi.applications.rainbow.lib.FormatManager;
 import net.sf.okapi.applications.rainbow.lib.ILog;
 import net.sf.okapi.applications.rainbow.lib.LanguageItem;
 import net.sf.okapi.applications.rainbow.lib.LanguageManager;
 import net.sf.okapi.applications.rainbow.lib.LogForm;
 import net.sf.okapi.applications.rainbow.lib.PathBuilderPanel;
 import net.sf.okapi.applications.rainbow.lib.Utils;
 //import net.sf.okapi.applications.rainbow.pipeline.PipelineEditor;
 //import net.sf.okapi.applications.rainbow.pipeline.PipelineWrapper;
 import net.sf.okapi.applications.rainbow.plugins.PluginItem;
 import net.sf.okapi.applications.rainbow.plugins.PluginsAccess;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersProvider;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.ui.AboutDialog;
 import net.sf.okapi.common.ui.BaseHelp;
 import net.sf.okapi.common.ui.CharacterInfoDialog;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.InputDialog;
 import net.sf.okapi.common.ui.MRUList;
 import net.sf.okapi.common.ui.ResourceManager;
 import net.sf.okapi.common.ui.UIUtil;
 import net.sf.okapi.common.ui.UserConfiguration;
 import net.sf.okapi.lib.ui.segmentation.SRXEditor;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 public class MainForm implements IParametersProvider {
 	
 	protected static final String APPNAME = "Rainbow"; //$NON-NLS-1$
 	
 	public static final String OPT_ALLOWDUPINPUT = "allowDupInput"; //$NON-NLS-1$
 	public static final String OPT_LOADMRU       = "loadMRU"; //$NON-NLS-1$
 	public static final String OPT_BOUNDS        = "bounds"; //$NON-NLS-1$
 	public static final String OPT_LOGLEVEL      = "logLevel"; //$NON-NLS-1$
 
 	private int currentInput;
 	private ArrayList<Table> inputTables;
 	private ArrayList<InputTableModel> inputTableMods;
 	private Shell shell;
 	private ILog log;
 	private LogHandler logHandler;
 	private UserConfiguration config;
 	private MRUList mruList;
 	private String rootFolder;
 	private String sharedFolder;
 	private BaseHelp help;
 	private Project prj;
 	//private PipelineWrapper wrapper; 
 	private StatusBar statusBar;
 	private TabFolder tabFolder;
 	private Label stInputRoot;
 	private Text edInputRoot;
 	private Button btGetRoot;
 	private Button chkUseOutputRoot;
 	private Text edOutputRoot;
 	private Text edSourceLang;
 	private List lbSourceLang;
 	private boolean inSourceLangSelection;
 	private Text edSourceEnc;
 	private List lbSourceEnc;
 	private boolean inSourceEncSelection;
 	private Text edTargetLang;
 	private List lbTargetLang;
 	private boolean inTargetLangSelection;
 	private Text edTargetEnc;
 	private List lbTargetEnc;
 	private boolean inTargetEncSelection;
 	private Button chkUseCustomParametersFolder;
 	private Text edParamsFolder;
 	private Button btGetParamsFolder; 
 	private TabItem tiInputList1;
 	private TabItem tiInputList2;
 	private TabItem tiInputList3;
 	private TabItem tiOptions;
 	private PathBuilderPanel pnlPathBuilder;
 	private int waitCount;
 	private boolean startLogWasRequested;
 	private LanguageManager lm;
 	private ResourceManager rm;
 	private FormatManager fm;
 	private FilterAccess fa;
 	private EncodingManager em;
 	private PluginsAccess plugins;
 	private UtilityDriver ud;
 	private MenuItem miInput;
 	private MenuItem miUtilities;
 	private MenuItem miHelp;
 	private MenuItem miSave;
 	private MenuItem miTools;
 	private MenuItem miEditInputProperties;
 	private MenuItem cmiEditInputProperties;
 	private MenuItem miOpenInputDocument;
 	private MenuItem cmiOpenInputDocument;
 	private MenuItem miRemoveInputDocuments;
 	private MenuItem cmiRemoveInputDocuments;
 	private MenuItem miMoveDocumentsUp;
 	private MenuItem cmiMoveDocumentsUp;
 	private MenuItem miMoveDocumentsDown;
 	private MenuItem cmiMoveDocumentsDown;
 	private MenuItem miOpenFolder;
 	private MenuItem cmiOpenFolder;
 	private MenuItem miOpenOutputFolder;
 	private MenuItem miMRU;
 	private ToolItem tbiMoveUp;
 	private ToolItem tbiMoveDown;
 	private ToolItem tbiAddDocs;
 	private ToolItem tbiOpenFolder;
 	private ToolItem tbiEditDocProp;
 
 	public MainForm (Shell shell,
 		String projectFile)
 	{
 		try {
 			this.shell = shell;
 			
 			setDirectories();
 			loadResources();
 
 			config = new UserConfiguration();
 			config.setProperty(OPT_LOADMRU, "0"); // Defaults //$NON-NLS-1$
 			config.load(APPNAME); // Load the current user preferences
 			mruList = new MRUList(9);
 			mruList.getFromProperties(config);
 			
 			createContent();
 			createProject(false);
 
 			if ( projectFile != null ) {
 				// Load project if passed as parameter
 				openProject(projectFile);
 			}
 			else { // Load MRU project if requested
 				int n = config.getInteger(OPT_LOADMRU); //$NON-NLS-1$
 				if ( n > 0 ) { // 1=ask, 2=load without asking
 					String path = mruList.getfirst();
 					if ( path != null ) {
 						if ( n == 1 ) { // Ask
 							// Ask confirmation
 							MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
 							dlg.setMessage(Res.getString("MainForm.askMRU1")+path+Res.getString("MainForm.askMRU2")); //$NON-NLS-1$ //$NON-NLS-2$
 							dlg.setText(APPNAME);
 							if ( dlg.open() == SWT.YES ) n = 2;
 						}
 						if ( n == 2 ) openProject(path);
 					}
 				}
 			}
 		}
 		catch ( Throwable E ) {
 			Dialogs.showError(shell, E.getMessage(), null);			
 		}
 	}
 
 	private void createContent ()
 		throws Exception
 	{
 		GridLayout layTmp = new GridLayout(3, false);
 		shell.setLayout(layTmp);
 		shell.setImage(rm.getImage("Rainbow")); //$NON-NLS-1$
 		
 		// Handling of the closing event
 		shell.addShellListener(new ShellListener() {
 			public void shellActivated(ShellEvent event) {}
 			public void shellClosed(ShellEvent event) {
 				saveUserConfiguration();
 				if ( !canContinue() ) event.doit = false;
 			}
 			public void shellDeactivated(ShellEvent event) {}
 			public void shellDeiconified(ShellEvent event) {}
 			public void shellIconified(ShellEvent event) {}
 		});
 
 		log = new LogForm(shell);
 		log.setTitle(Res.getString("LOG_CAPTION")); //$NON-NLS-1$
 		logHandler = new LogHandler(log);
 		setLogLevel();
 		Logger.getLogger("").addHandler(logHandler); //$NON-NLS-1$
 
 		fa = new FilterAccess();
 		fa.loadList(sharedFolder + File.separator + "filters.xml"); //$NON-NLS-1$
 		// Define default editor, if none, the fall back for .txt will be used.
 		fa.setDefaultEditor(config.getProperty("defaultEditor")); //$NON-NLS-1$
 
 		// Toolbar
 		createToolbar();
 		
 		// Menus
 	    Menu menuBar = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menuBar);
 
 		// File menu
 		MenuItem topItem = new MenuItem(menuBar, SWT.CASCADE);
 		topItem.setText(rm.getCommandLabel("file")); //$NON-NLS-1$
 		Menu dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		topItem.setMenu(dropMenu);
 
 		MenuItem menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "file.new"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				createProject(true);
             }
 		});
 		menuItem.setImage(rm.getImage("newproject")); //$NON-NLS-1$
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "file.open"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openProject(null);
             }
 		});
 		menuItem.setImage(rm.getImage("openproject")); //$NON-NLS-1$
 
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 
 		miSave = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miSave, "file.save"); //$NON-NLS-1$
 		miSave.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
             	saveProject(prj.path);
             }
 		});
 		miSave.setImage(rm.getImage("saveproject")); //$NON-NLS-1$
 		
 		miSave = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miSave, "file.saveas"); //$NON-NLS-1$
 		miSave.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
             	saveProject(null);
             }
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 
 		miMRU = new MenuItem(dropMenu, SWT.CASCADE);
 		rm.setCommand(miMRU, "file.mru"); //$NON-NLS-1$
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "file.clearmru"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
             	clearMRU();
             }
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "file.userpref"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				editUserPreferences();
             }
 		});
 
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "file.exit"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				shell.close();
             }
 		});
 
 		// View menu
 		topItem = new MenuItem(menuBar, SWT.CASCADE);
 		topItem.setText(rm.getCommandLabel("view")); //$NON-NLS-1$
 		dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		topItem.setMenu(dropMenu);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "view.inputList1"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				tabFolder.setSelection(0);
 				updateTabInfo();
             }
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "view.inputList2"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				tabFolder.setSelection(1);
 				updateTabInfo();
             }
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "view.inputList3"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				tabFolder.setSelection(2);
 				updateTabInfo();
             }
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "view.langAndEnc"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				tabFolder.setSelection(3);
 				updateTabInfo();
             }
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "view.otherSettings"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				tabFolder.setSelection(4);
 				updateTabInfo();
             }
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "view.log"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if ( log.isVisible() ) log.hide();
 				else log.show();
             }
 		});
 		menuItem.setImage(rm.getImage("log")); //$NON-NLS-1$
 		
 		// Input menu
 		miInput = new MenuItem(menuBar, SWT.CASCADE);
 		miInput.setText(rm.getCommandLabel("input")); //$NON-NLS-1$
 		dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		miInput.setMenu(dropMenu);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "input.addDocuments"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				addDocumentsFromList(null);
             }
 		});
 		menuItem.setImage(rm.getImage("addinput")); //$NON-NLS-1$
 		
 		miRemoveInputDocuments = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miRemoveInputDocuments, "input.removeDocuments"); //$NON-NLS-1$
 		miRemoveInputDocuments.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				removeDocuments(-1);
             }
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "input.editRoot"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				changeRoot();
             }
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		miOpenInputDocument = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miOpenInputDocument, "input.openDocument"); //$NON-NLS-1$
 		miOpenInputDocument.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openDocument(-1);
             }
 		});
 		
 		miOpenFolder = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miOpenFolder, "input.openFolder"); //$NON-NLS-1$
 		miOpenFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openContainingFolder(-1);
             }
 		});
 		miOpenFolder.setImage(rm.getImage("openfolder")); //$NON-NLS-1$
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		miMoveDocumentsUp = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miMoveDocumentsUp, "input.moveDocumentsUp"); //$NON-NLS-1$
 		miMoveDocumentsUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDocumentsUp();
 			}
 		});
 		miMoveDocumentsUp.setImage(rm.getImage("moveup")); //$NON-NLS-1$
 		
 		
 		miMoveDocumentsDown = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miMoveDocumentsDown, "input.moveDocumentsDown"); //$NON-NLS-1$
 		miMoveDocumentsDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDocumentsDown();
 			}
 		});
 		miMoveDocumentsDown.setImage(rm.getImage("movedown")); //$NON-NLS-1$
 
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		miEditInputProperties = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miEditInputProperties, "input.editProperties"); //$NON-NLS-1$
 		miEditInputProperties.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editInputProperties(-1);
 			}
 		});
 		miEditInputProperties.setImage(rm.getImage("properties")); //$NON-NLS-1$
 		
 		// Utilities menu
 		miUtilities = new MenuItem(menuBar, SWT.CASCADE);
 		miUtilities.setText(rm.getCommandLabel("utilities")); //$NON-NLS-1$
 		buildUtilitiesMenu();
 
 		// Pipeline menu
 /*		MenuItem miPipeline = new MenuItem(menuBar, SWT.CASCADE);
 		miPipeline.setText(rm.getCommandLabel("pipeline")); //$NON-NLS-1$
 		dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		miPipeline.setMenu(dropMenu);
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "pipeline.edit"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				editPipeline();
 			}
 		});
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "pipeline.execute"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				executePipeline();
 			}
 		});
 */
 		// Tools menu
 		miTools = new MenuItem(menuBar, SWT.CASCADE);
 		miTools.setText(rm.getCommandLabel("tools")); //$NON-NLS-1$
 		dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		miTools.setMenu(dropMenu);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "tools.editsegrules"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				editSegmentationRules(null);
 			}
 		});
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "tools.listencodings"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				listEncodings();
 			}
 		});
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "tools.charinfo"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				showCharInfo();
 			}
 		});
 
 		// Help menu
 		miHelp = new MenuItem(menuBar, SWT.CASCADE);
 		miHelp.setText(rm.getCommandLabel("help")); //$NON-NLS-1$
 		dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		miHelp.setMenu(dropMenu);
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.topics"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if ( help != null ) help.showTopic(this, "index"); //$NON-NLS-1$
 			}
 		});
 		menuItem.setImage(rm.getImage("help")); //$NON-NLS-1$
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.howtouse"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if ( help != null ) help.showTopic(this, "index", "howTo.html"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.update"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				//TODO Help check for updates
 				UIUtil.start("http://okapi.opentag.com/updates?" 
 					+ getClass().getPackage().getImplementationTitle()
 					+ "="
 					+ getClass().getPackage().getImplementationVersion());
 			}
 		});
 
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.feedback"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				UIUtil.start("mailto:okapitools@opentag.com&subject=Feedback (Rainbow)"); //$NON-NLS-1$
 			}
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.bugreport"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				UIUtil.start("http://code.google.com/p/okapi/issues/list"); //$NON-NLS-1$
 			}
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.featurerequest"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				UIUtil.start("http://code.google.com/p/okapi/issues/list"); //$NON-NLS-1$
 			}
 		});
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.users"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				UIUtil.start("http://groups.yahoo.com/group/okapitools/"); //$NON-NLS-1$
 			}
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		menuItem = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "help.about"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				AboutDialog dlg = new AboutDialog(shell,
 					Res.getString("MainForm.aboutCaption"), //$NON-NLS-1$
 					Res.getString("MainForm.aboutAppName"), //$NON-NLS-1$
 					getClass().getPackage().getImplementationVersion());
 				dlg.showDialog();
 			}
 		});
 
 		
 		// Drop target for opening project
 		DropTarget dropTarget = new DropTarget(shell, DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE);
 		dropTarget.setTransfer(new FileTransfer[]{FileTransfer.getInstance()}); 
 		dropTarget.addDropListener(new DropTargetAdapter() {
 			public void drop (DropTargetEvent e) {
 				FileTransfer FT = FileTransfer.getInstance();
 				if ( FT.isSupportedType(e.currentDataType) ) {
 					String[] paths = (String[])e.data;
 					if ( paths != null ) {
 						openProject(paths[0]);
 					}
 				}
 			}
 		});
 		
 		// Root panel
 		stInputRoot = new Label(shell, SWT.NONE);
 		// Text an approximative text for correct sizing
 		stInputRoot.setText(Res.getString("MainForm.inputRootSizingLabel")); //$NON-NLS-1$
 		
 		edInputRoot = new Text(shell, SWT.SINGLE | SWT.BORDER);
 		edInputRoot.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edInputRoot.setEditable(false);
 		
 		btGetRoot = new Button(shell, SWT.PUSH);
 		btGetRoot.setText("..."); //$NON-NLS-1$
 		btGetRoot.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				changeRoot();
 			}
 		});
 		
 		// Tab control
 		tabFolder = new TabFolder(shell, SWT.NONE);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 3;
 		tabFolder.setLayoutData(gdTmp);
 		// Events are set at the end of this methods
 
 		inputTables = new ArrayList<Table>(1);
 		inputTableMods = new ArrayList<InputTableModel>();
 		
 		// Input List 1
 		Composite comp = new Composite(tabFolder, SWT.NONE);
 		comp.setLayout(new GridLayout());
 		tiInputList1 = new TabItem(tabFolder, SWT.NONE);
 		tiInputList1.setText(Res.getString("tiInputList1")); //$NON-NLS-1$
 		tiInputList1.setControl(comp);
 		buildInputTab(0, comp);
 		
 		// Input List 2
 		comp = new Composite(tabFolder, SWT.NONE);
 		comp.setLayout(new GridLayout());
 		tiInputList2 = new TabItem(tabFolder, SWT.NONE);
 		tiInputList2.setText(Res.getString("tiInputList2")); //$NON-NLS-1$
 		tiInputList2.setControl(comp);
 		buildInputTab(1, comp);
 		
 		// Input List 3
 		comp = new Composite(tabFolder, SWT.NONE);
 		comp.setLayout(new GridLayout());
 		tiInputList3 = new TabItem(tabFolder, SWT.NONE);
 		tiInputList3.setText(Res.getString("tiInputList3")); //$NON-NLS-1$
 		tiInputList3.setControl(comp);
 		buildInputTab(2, comp);
 		
 		// Context menu for the input list
 		Menu inputTableMenu = new Menu(shell, SWT.POP_UP);
 		
 		menuItem = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(menuItem, "input.addDocuments"); //$NON-NLS-1$
 		menuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				addDocumentsFromList(null);
             }
 		});
 		menuItem.setImage(rm.getImage("addinput")); //$NON-NLS-1$
 		
 		cmiRemoveInputDocuments = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(cmiRemoveInputDocuments, "input.removeDocuments"); //$NON-NLS-1$
 		cmiRemoveInputDocuments.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				removeDocuments(-1);
             }
 		});
 		
 		new MenuItem(inputTableMenu, SWT.SEPARATOR);
 		
 		cmiOpenInputDocument = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(cmiOpenInputDocument, "input.openDocument"); //$NON-NLS-1$
 		cmiOpenInputDocument.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openDocument(-1);
             }
 		});
 		
 		cmiOpenFolder = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(cmiOpenFolder, "input.openFolder"); //$NON-NLS-1$
 		cmiOpenFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openContainingFolder(-1);
             }
 		});
 		cmiOpenFolder.setImage(rm.getImage("openfolder")); //$NON-NLS-1$
 		
 		new MenuItem(inputTableMenu, SWT.SEPARATOR);
 		
 		cmiMoveDocumentsUp = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(cmiMoveDocumentsUp, "input.moveDocumentsUp"); //$NON-NLS-1$
 		cmiMoveDocumentsUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDocumentsUp();
 			}
 		});
 		cmiMoveDocumentsUp.setImage(rm.getImage("moveup")); //$NON-NLS-1$
 		
 		cmiMoveDocumentsDown = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(cmiMoveDocumentsDown, "input.moveDocumentsDown"); //$NON-NLS-1$
 		cmiMoveDocumentsDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDocumentsDown();
 			}
 		});
 		cmiMoveDocumentsDown.setImage(rm.getImage("movedown")); //$NON-NLS-1$
 
 		new MenuItem(inputTableMenu, SWT.SEPARATOR);
 		
 		cmiEditInputProperties = new MenuItem(inputTableMenu, SWT.PUSH);
 		rm.setCommand(cmiEditInputProperties, "input.editProperties"); //$NON-NLS-1$
 		cmiEditInputProperties.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editInputProperties(-1);
 			}
 		});
 		cmiEditInputProperties.setImage(rm.getImage("properties")); //$NON-NLS-1$
 
 		// Set the popup menus for the input lists
 		inputTables.get(0).setMenu(inputTableMenu);
 		inputTables.get(1).setMenu(inputTableMenu);
 		inputTables.get(2).setMenu(inputTableMenu);
 		
 		// Pop up the property editor by double-clicking
 		MouseAdapter ma = new MouseAdapter(){
 			public void mouseDoubleClick(MouseEvent e) {
 				Table t = (Table) e.getSource();				
 				if (( e.x > t.getColumn(0).getWidth() )
 					&& ( e.x < t.getColumn(0).getWidth()+t.getColumn(1).getWidth() )) {
 					editInputProperties(-1);
 				}
 			}
 		};
 		inputTables.get(0).addMouseListener(ma);
 		inputTables.get(1).addMouseListener(ma);
 		inputTables.get(2).addMouseListener(ma);
 
 		// Select all items by clicking Ctrl+A
 		KeyAdapter ka = new KeyAdapter(){	
 			public void keyPressed(KeyEvent e) {
 				if ((( e.stateMask & SWT.CTRL) != 0 ) && ( e.keyCode==97 )) {
 					Table t = (Table) e.getSource();
 					t.setSelection(t.getItems());
 				}
 			}
 		};
 		inputTables.get(0).addKeyListener(ka);
 		inputTables.get(1).addKeyListener(ka);
 		inputTables.get(2).addKeyListener(ka);
 			
 		// Options tab
 		comp = new Composite(tabFolder, SWT.NONE);
 		comp.setLayout(new GridLayout());
 		tiOptions = new TabItem(tabFolder, SWT.NONE);
 		tiOptions.setText(Res.getString("OPTTAB_CAPTION")); //$NON-NLS-1$
 		tiOptions.setControl(comp);
 		
 		Group group = new Group(comp, SWT.NONE);
 		group.setText(Res.getString("OPTTAB_GRPSOURCE")); //$NON-NLS-1$
 		group.setLayoutData(new GridData(GridData.FILL_BOTH));
 		group.setLayout(new GridLayout(4, false));
 		
 		Label label = new Label(group, SWT.NONE);
 		label.setText(Res.getString("OPTTAB_LANG")); //$NON-NLS-1$
 		
 		edSourceLang = new Text(group, SWT.BORDER);
 		edSourceLang.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edSourceLang.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				updateSourceLanguageSelection();
 			}
 		});
 
 		label = new Label(group, SWT.NONE);
 		label.setText(Res.getString("OPTTAB_ENC")); //$NON-NLS-1$
 		
 		edSourceEnc = new Text(group, SWT.BORDER);
 		edSourceEnc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edSourceEnc.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				updateSourceEncodingSelection();
 			}
 		});
 		
 		lbSourceLang = new List(group, SWT.BORDER | SWT.V_SCROLL);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 2;
 		gdTmp.heightHint = 30;
 		lbSourceLang.setLayoutData(gdTmp);
 		lbSourceLang.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				int n = lbSourceLang.getSelectionIndex();
 				if ( n > -1 ) {
 					inSourceLangSelection = true;
 					edSourceLang.setText(lm.getItem(n).code);
 					pnlPathBuilder.setSourceLanguage(lm.getItem(n).code);
 					inSourceLangSelection = false;
 				}
 			};
 		});
 
 		lbSourceEnc = new List(group, SWT.BORDER | SWT.V_SCROLL);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 2;
 		gdTmp.heightHint = 30;
 		lbSourceEnc.setLayoutData(gdTmp);
 		lbSourceEnc.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				int n = lbSourceEnc.getSelectionIndex();
 				if ( n > -1 ) {
 					inSourceEncSelection = true;
 					edSourceEnc.setText(em.getItem(n).ianaName);
 					inSourceEncSelection = false;
 				}
 			};
 		});
 
 		group = new Group(comp, SWT.NONE);
 		group.setText(Res.getString("OPTTAB_GRPTARGET")); //$NON-NLS-1$
 		group.setLayoutData(new GridData(GridData.FILL_BOTH));
 		group.setLayout(new GridLayout(4, false));
 		
 		label = new Label(group, SWT.NONE);
 		label.setText(Res.getString("OPTTAB_LANG")); //$NON-NLS-1$
 		
 		edTargetLang = new Text(group, SWT.BORDER);
 		edTargetLang.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edTargetLang.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				updateTargetLanguageSelection();
 			}
 		});
 
 		label = new Label(group, SWT.NONE);
 		label.setText(Res.getString("OPTTAB_ENC")); //$NON-NLS-1$
 		
 		edTargetEnc = new Text(group, SWT.BORDER);
 		edTargetEnc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edTargetEnc.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				updateTargetEncodingSelection();
 			}
 		});
 
 		lbTargetLang = new List(group, SWT.BORDER | SWT.V_SCROLL);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 2;
 		gdTmp.heightHint = 30;
 		lbTargetLang.setLayoutData(gdTmp);
 		lbTargetLang.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				int n = lbTargetLang.getSelectionIndex();
 				if ( n > -1 ) {
 					inTargetLangSelection = true;
 					edTargetLang.setText(lm.getItem(n).code);
 					pnlPathBuilder.setTargetLanguage(lm.getItem(n).code);
 					inTargetLangSelection = false;
 				}
 			};
 		});
 
 		lbTargetEnc = new List(group, SWT.BORDER | SWT.V_SCROLL);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 2;
 		gdTmp.heightHint = 30;
 		lbTargetEnc.setLayoutData(gdTmp);
 		lbTargetEnc.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				int n = lbTargetEnc.getSelectionIndex();
 				if ( n > -1 ) {
 					inTargetEncSelection = true;
 					edTargetEnc.setText(em.getItem(n).ianaName);
 					inTargetEncSelection = false;
 				}
 			};
 		});
 		
 		LanguageItem li;
 		for ( int i=0; i<lm.getCount(); i++ ) {
 			li = lm.getItem(i);
 			lbSourceLang.add(li.name + "  -  " + li.code); //$NON-NLS-1$
 			lbTargetLang.add(li.name + "  -  " + li.code); //$NON-NLS-1$
 		}
 		
 		EncodingItem ei;
 		for ( int i=0; i<em.getCount(); i++ ) {
 			ei = em.getItem(i);
 			lbSourceEnc.add(ei.name + "  -  " + ei.ianaName); //$NON-NLS-1$
 			lbTargetEnc.add(ei.name + "  -  " + ei.ianaName); //$NON-NLS-1$
 		}
 		
 		// Other settings tab
 		comp = new Composite(tabFolder, SWT.NONE);
 		comp.setLayout(new GridLayout());
 		tiOptions = new TabItem(tabFolder, SWT.NONE);
 		tiOptions.setText(Res.getString("MainForm.tabOtherSettings")); //$NON-NLS-1$
 		tiOptions.setControl(comp);
 		
 		group = new Group(comp, SWT.NONE);
 		group.setLayout(new GridLayout(2, false));
 		group.setText(Res.getString("MainForm.outputGroup")); //$NON-NLS-1$
 		group.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		chkUseOutputRoot = new Button(group, SWT.CHECK);
 		chkUseOutputRoot.setText(Res.getString("MainForm.outputUseRoot")); //$NON-NLS-1$
 		chkUseOutputRoot.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				edOutputRoot.setEnabled(chkUseOutputRoot.getSelection());
 				updateOutputRoot();
 			};
 		});
 		
 		edOutputRoot = new Text(group, SWT.SINGLE | SWT.BORDER);
 		edOutputRoot.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edOutputRoot.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				updateOutputRoot();
 			}
 		});
 		
 		pnlPathBuilder = new PathBuilderPanel(group, SWT.NONE);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 2;
 		pnlPathBuilder.setLayoutData(gdTmp);
 
 		group = new Group(comp, SWT.NONE);
 		group.setLayout(new GridLayout(3, false));
 		group.setText(Res.getString("MainForm.paramFolderGroup")); //$NON-NLS-1$
 		group.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		label = new Label(group, SWT.NONE);
 		label.setText(Res.getString("MainForm.paramFolder")); //$NON-NLS-1$
 		
 		edParamsFolder = new Text(group, SWT.BORDER);
 		edParamsFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		btGetParamsFolder = new Button(group, SWT.PUSH);
 		btGetParamsFolder.setText("..."); //$NON-NLS-1$
 		btGetParamsFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				browseParamsFolder();
             }
 		});
 		
 		// Space-holder
 		new Label(group, SWT.NONE);
 		
 		chkUseCustomParametersFolder = new Button(group, SWT.CHECK);
 		chkUseCustomParametersFolder.setText(Res.getString("MainForm.useCustomParamFolder")); //$NON-NLS-1$
 		chkUseCustomParametersFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				// First save the custom folder if it was custom (so !was)
 				if ( !chkUseCustomParametersFolder.getSelection() ) {
 					prj.setCustomParametersFolder(edParamsFolder.getText());
 				}
 				btGetParamsFolder.setEnabled(chkUseCustomParametersFolder.getSelection());
 				edParamsFolder.setEditable(chkUseCustomParametersFolder.getSelection());
 				edParamsFolder.setText(prj.getParametersFolder(chkUseCustomParametersFolder.getSelection(), true));
             }
 		});
 	
 		
 		// Tabs change event (define here to avoid triggering it while creating the content)
 		tabFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				updateTabInfo();
             }
 		});
 
 		statusBar = new StatusBar(shell, SWT.NONE);
 		updateMRU();
 		
 		// Set the minimal size to the packed size
 		// And then set the start size
 		Point startSize = shell.getSize();
 		shell.pack();
 		shell.setMinimumSize(shell.getSize());
 		shell.setSize(startSize);
 		// Maximize if requested
 		if ( config.getBoolean("maximized") ) { //$NON-NLS-1$
 			shell.setMaximized(true);
 		}
 		else { // Or try to re-use the bounds of the previous session
 			Rectangle ar = UIUtil.StringToRectangle(config.getProperty(OPT_BOUNDS));
 			if ( ar != null ) {
 				Rectangle dr = shell.getDisplay().getBounds();
 				if ( dr.contains(ar.x+ar.width, ar.y+ar.height)
 					&& dr.contains(ar.x, ar.y) ) {
 					shell.setBounds(ar);
 				}
 			}
 		}
 	}
 
 	private void setLogLevel () {
 		int n = config.getInteger(MainForm.OPT_LOGLEVEL);
 		switch ( n ) {
 		case 1:
 			logHandler.setLevel(Level.FINE);
 			break;
 		case 2:
 			logHandler.setLevel(Level.FINER);
 			break;
 		case 3:
 			logHandler.setLevel(Level.FINEST);
 			break;
 		default:
 			logHandler.setLevel(Level.INFO);
 			break;
 		}
 	}
 	
 	private void createToolbar () {
 		ToolBar toolbar = new ToolBar(shell, SWT.FLAT | SWT.WRAP);
 		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 3;
 		toolbar.setLayoutData(gdTmp);
 		
 		ToolItem item = new ToolItem(toolbar, SWT.PUSH);
 	    item.setImage(rm.getImage("newproject")); //$NON-NLS-1$
 	    item.setToolTipText(Res.getString("MainForm.newProjectTip")); //$NON-NLS-1$
 		item.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				createProject(true);
 			}
 		});
 	    
 		item = new ToolItem(toolbar, SWT.PUSH);
 	    item.setImage(rm.getImage("openproject")); //$NON-NLS-1$
 	    item.setToolTipText(Res.getString("MainForm.openProjectTip")); //$NON-NLS-1$
 		item.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				openProject(null);
 			}
 		});
 	    
 		item = new ToolItem(toolbar, SWT.PUSH);
 	    item.setImage(rm.getImage("saveproject")); //$NON-NLS-1$
 	    item.setToolTipText(Res.getString("MainForm.saveProjectTip")); //$NON-NLS-1$
 		item.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				saveProject(prj.path);
 			}
 		});
 		
 		new ToolItem(toolbar, SWT.SEPARATOR);
 
 		item = new ToolItem(toolbar, SWT.PUSH);
 	    item.setImage(rm.getImage("log")); //$NON-NLS-1$
 	    item.setToolTipText(Res.getString("MainForm.logTip")); //$NON-NLS-1$
 		item.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( log.isVisible() ) log.hide();
 				else log.show();
 			}
 		});
 
 		new ToolItem(toolbar, SWT.SEPARATOR);
 
 		tbiAddDocs = new ToolItem(toolbar, SWT.PUSH);
 		tbiAddDocs.setImage(rm.getImage("addinput")); //$NON-NLS-1$
 		tbiAddDocs.setToolTipText(Res.getString("MainForm.addDocsTip")); //$NON-NLS-1$
 		tbiAddDocs.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				addDocumentsFromList(null);
 			}
 		});
 
 		tbiOpenFolder = new ToolItem(toolbar, SWT.PUSH);
 		tbiOpenFolder.setImage(rm.getImage("openfolder")); //$NON-NLS-1$
 		tbiOpenFolder.setToolTipText(Res.getString("MainForm.openContFolderTip")); //$NON-NLS-1$
 		tbiOpenFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				openContainingFolder(-1);
 			}
 		});
 
 		tbiMoveUp = new ToolItem(toolbar, SWT.PUSH);
 		tbiMoveUp.setImage(rm.getImage("moveup")); //$NON-NLS-1$
 		tbiMoveUp.setToolTipText(Res.getString("MainForm.moveUpTip")); //$NON-NLS-1$
 		tbiMoveUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDocumentsUp();
 			}
 		});
 
 		tbiMoveDown = new ToolItem(toolbar, SWT.PUSH);
 		tbiMoveDown.setImage(rm.getImage("movedown")); //$NON-NLS-1$
 		tbiMoveDown.setToolTipText(Res.getString("MainForm.moveDownTip")); //$NON-NLS-1$
 		tbiMoveDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDocumentsDown();
 			}
 		});
 
 		tbiEditDocProp = new ToolItem(toolbar, SWT.PUSH);
 		tbiEditDocProp.setImage(rm.getImage("properties")); //$NON-NLS-1$
 		tbiEditDocProp.setToolTipText(Res.getString("MainForm.editInputPropTip")); //$NON-NLS-1$
 		tbiEditDocProp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editInputProperties(-1);
 			}
 		});
 
 		new ToolItem(toolbar, SWT.SEPARATOR);
 
 		item = new ToolItem(toolbar, SWT.PUSH);
 	    item.setImage(rm.getImage("help")); //$NON-NLS-1$
 	    item.setToolTipText(Res.getString("MainForm.helpTip")); //$NON-NLS-1$
 		item.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( help != null ) help.showTopic(this, "index"); //$NON-NLS-1$
 			}
 		});
 	}
 	
 	private void browseParamsFolder () {
 		try {
 			DirectoryDialog dlg = new DirectoryDialog(shell);
 			dlg.setFilterPath(edParamsFolder.getText());
 			String dir = dlg.open();
 			if (  dir == null ) return;
 			edParamsFolder.setText(dir);
 			edParamsFolder.selectAll();
 			edParamsFolder.setFocus();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 
 /*	private void editPipeline () {
 		try {
 			PipelineEditor dlg = new PipelineEditor();
 			dlg.edit(shell, plugins, wrapper, null, null, false);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void executePipeline () {
 		try {
 			// Save any pending data
 			saveSurfaceData();
 
 			PipelineEditor dlg = new PipelineEditor();
 			if ( !dlg.edit(shell, plugins, wrapper, null, null, true) ) return;
 
 			// Create the utility driver if needed
 			startWaiting(Res.getString("MainForm.startWaiting"), true); //$NON-NLS-1$
 
 			wrapper.execute();
 		}
 		catch ( Exception E ) {
 			Dialogs.showError(shell, E.getMessage(), null);
 		}
 		finally {
 			stopWaiting();
 		}
 	}
 */	
 	private void updateTabInfo () {
 		if ( tabFolder.getSelectionIndex() < inputTables.size() ) {
 			currentInput = tabFolder.getSelectionIndex();
 		}
 		else {
 			currentInput = -1;
 		}
 		updateCommands();
 		updateInputRoot();
 		
 		miInput.setEnabled(currentInput!=-1);
 		btGetRoot.setEnabled(currentInput!=-1);
 		
 		tbiAddDocs.setEnabled(currentInput!=-1);
 		tbiMoveUp.setEnabled(currentInput!=-1);
 		tbiMoveDown.setEnabled(currentInput!=-1);
 		tbiOpenFolder.setEnabled(currentInput!=-1);
 		tbiEditDocProp.setEnabled(currentInput!=-1);
 	}
 	
 	private void buildInputTab (int index,
 		Composite comp)
 	{
 		final Table table = new Table(comp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
 		
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 3;
 		table.setLayoutData(gdTmp);
 		table.addControlListener(new ControlAdapter() {
 		    public void controlResized(ControlEvent e) {
 		    	Table table = (Table)e.getSource();
 		    	Rectangle rect = table.getClientArea();
 				//TODO: Check behavior when manual resize a column width out of client area
 				int nPart = (int)(rect.width / 100);
 				table.getColumn(0).setWidth(70*nPart);
 				table.getColumn(1).setWidth(rect.width-table.getColumn(0).getWidth());
 		    }
 		});
 		table.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if ( e.character == ' ' ) {
 					editInputProperties(-1);
 				}
 				else if ( e.keyCode == SWT.DEL ) {
 					removeDocuments(-1);
 				}
 			}
 		});
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				statusBar.setCounter(table.getSelectionIndex(),
 					table.getItemCount());
             }
 		});
 
 		InputTableModel model = new InputTableModel();
 		model.linkTable(table);
 
 		// Drop target for the table
 		DropTarget dropTarget = new DropTarget(table, DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE);
 		dropTarget.setTransfer(new FileTransfer[]{FileTransfer.getInstance()}); 
 		dropTarget.addDropListener(new DropTargetAdapter() {
 			public void drop (DropTargetEvent e) {
 				FileTransfer FT = FileTransfer.getInstance();
 				if ( FT.isSupportedType(e.currentDataType) ) {
 					String[] paths = (String[])e.data;
 					if ( paths != null ) {
 						addDocumentsFromList(paths);
 					}
 				}
 			}
 		});
 		
 		inputTables.add(table);
 		inputTableMods.add(model);
 		currentInput = index;
 	}
 	
 	private void buildUtilitiesMenu () {
 		// Remove an existing menu
 		Menu menu = miUtilities.getMenu();
 		if ( menu != null ) menu.dispose();
 		// Create new one
 		Menu dropMenu = new Menu(shell, SWT.DROP_DOWN);
 		miUtilities.setMenu(dropMenu);
 		
 		// Add the default entries
 		miOpenOutputFolder = new MenuItem(dropMenu, SWT.PUSH);
 		rm.setCommand(miOpenOutputFolder, "utilities.openOutputFolder"); //$NON-NLS-1$
 		miOpenOutputFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if ( prj.getLastOutputFolder() == null ) return;
 				Program.launch(prj.getLastOutputFolder());
             }
 		});
 		
 		new MenuItem(dropMenu, SWT.SEPARATOR);
 		
 		// Add the plug-in utilities
 		Iterator<String> iter = plugins.getIterator();
 		while ( iter.hasNext() ) {
 			PluginItem item = plugins.getItem(iter.next());
 			if ( item.type == -1 ) {
 				new MenuItem(dropMenu, SWT.SEPARATOR);
 			}
 			else {
 				MenuItem menuItem = new MenuItem(dropMenu, SWT.PUSH);
 				menuItem.setText(item.name+"..."); //$NON-NLS-1$
 				menuItem.setData(item.id);
 				menuItem.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent event) {
 						launchUtility((String)((MenuItem)event.getSource()).getData());
 					}
 				});
 			}
 		}
 	}
 
 	public void run () {
 		try {
 			Display Disp = shell.getDisplay();
 			while ( !shell.isDisposed() ) {
 				if (!Disp.readAndDispatch())
 					Disp.sleep();
 			}
 		}
 		finally {
 			// Dispose of any global resources
 			if ( rm != null ) rm.dispose();
 		}
 	}
 
 	private void launchUtility (String utilityID) {
 		try {
 			if ( utilityID == null ) return;
 			// Save any pending data
 			saveSurfaceData();
 			// Create the utility driver if needed
 			if ( ud == null ) {
 				ud = new UtilityDriver(log, fa, plugins, help, true);
 			}
 			// Get the data for the utility and instantiate it
 			ud.setData(prj, utilityID);
 			// Run it
 			if ( !ud.checkParameters(shell) ) return;
 			startWaiting(Res.getString("MainForm.startWaiting"), true); //$NON-NLS-1$
 			ud.execute(shell);
 			// Gets the latest folder to open.
 			prj.setLastOutpoutFolder(ud.getUtility().getFolderAfterProcess());
 		}
 		catch ( Exception E ) {
 			Dialogs.showError(shell, E.getMessage(), null);
 		}
 		finally {
 			miOpenOutputFolder.setEnabled(prj.getLastOutputFolder()!=null);
 			stopWaiting();
 		}
 	}
 
 	private void updateSourceLanguageSelection () {
 		if ( inSourceLangSelection ) return;
 		int n = lm.getIndexFromCode(edSourceLang.getText());
 		if ( n > -1 ) {
 			lbSourceLang.setSelection(n);
 			lbSourceLang.showSelection();
 		}
 		pnlPathBuilder.setSourceLanguage(edSourceLang.getText());
 	}
 
 	private void updateTargetLanguageSelection () {
 		if ( inTargetLangSelection ) return;
 		int n = lm.getIndexFromCode(edTargetLang.getText());
 		if ( n > -1 ) {
 			lbTargetLang.setSelection(n);
 			lbTargetLang.showSelection();
 		}
 		pnlPathBuilder.setTargetLanguage(edTargetLang.getText());
 	}
 
 	private void updateSourceEncodingSelection () {
 		if ( inSourceEncSelection ) return;
 		int n = em.getIndexFromIANAName(edSourceEnc.getText());
 		if ( n > -1 ) {
 			lbSourceEnc.setSelection(n);
 			lbSourceEnc.showSelection();
 		}
 	}
 
 	private void updateTargetEncodingSelection () {
 		if ( inTargetEncSelection ) return;
 		int n = em.getIndexFromIANAName(edTargetEnc.getText());
 		if ( n > -1 ) {
 			lbTargetEnc.setSelection(n);
 			lbTargetEnc.showSelection();
 		}
 	}
 
 	private void updateOutputRoot () {
 		try {
 			if ( chkUseOutputRoot.getSelection() ) {
 				pnlPathBuilder.setTargetRoot(edOutputRoot.getText());
 			}
 			else {
 				pnlPathBuilder.setTargetRoot(null);
 			}
 			pnlPathBuilder.updateSample();
 		}
 		catch ( Exception E ) {
 			Dialogs.showError(shell, E.getMessage(), null);
 		}
 	}
 	
 	private void setDirectories () throws UnsupportedEncodingException {
     	// Get the location of the main class source
     	File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
     	rootFolder = URLDecoder.decode(file.getAbsolutePath(),"utf-8"); //$NON-NLS-1$
     	// Remove the JAR file if running an installed version
     	if ( rootFolder.endsWith(".jar") ) rootFolder = Util.getDirectoryName(rootFolder); //$NON-NLS-1$
     	// Remove the application folder in all cases
     	rootFolder = Util.getDirectoryName(rootFolder);
 		sharedFolder = Utils.getOkapiSharedFolder(rootFolder);
 		help = new BaseHelp(rootFolder+File.separator+"help"); //$NON-NLS-1$
 	}
 	
 	private void startWaiting (String text,
 		boolean p_bStartLog)
 	{
 		if ( ++waitCount > 1 ) {
 			shell.getDisplay().update();
 			return;
 		}
 		if ( text != null ) statusBar.setInfo(text);
 		startLogWasRequested = p_bStartLog;
 		if ( startLogWasRequested ) log.beginProcess(null);
 		shell.getDisplay().update();
 	}
 
 	private void stopWaiting () {
 		waitCount--;
 		if ( waitCount < 1 ) statusBar.clearInfo();
 		shell.getDisplay().update();
 		if ( log.inProgress() ) log.endProcess(null); 
 		if ( startLogWasRequested && ( log.getErrorAndWarningCount() > 0 )) log.show();
 		startLogWasRequested = false;
 	}
 
 	private void updateCommands () {
 		boolean enabled = (( currentInput > -1 ) && ( currentInput < inputTables.size() ));
 		if ( enabled ) {
 			int total = inputTables.get(currentInput).getItemCount();
 			enabled = ( total > 0);
 			statusBar.setCounter(inputTables.get(currentInput).getSelectionIndex(), total);
 		}
 		else {
 			statusBar.setCounter(-1, 0);
 		}
 
 		miEditInputProperties.setEnabled(enabled);
 		cmiEditInputProperties.setEnabled(enabled);
 		miOpenInputDocument.setEnabled(enabled);
 		cmiOpenInputDocument.setEnabled(enabled);
 		miRemoveInputDocuments.setEnabled(enabled);
 		cmiRemoveInputDocuments.setEnabled(enabled);
 
 		miOpenFolder.setEnabled(enabled);
 		cmiOpenFolder.setEnabled(enabled);
 		miOpenOutputFolder.setEnabled(prj.getLastOutputFolder()!=null);
 		
 		miMoveDocumentsUp.setEnabled(enabled);
 		cmiMoveDocumentsUp.setEnabled(enabled);
 		miMoveDocumentsDown.setEnabled(enabled);
 		cmiMoveDocumentsDown.setEnabled(enabled);
 	}
 	
 	private void loadResources ()
 		throws Exception 
 	{
 		rm = new ResourceManager(MainForm.class, shell.getDisplay());
 		rm.addImage("Rainbow"); //$NON-NLS-1$
 		rm.addImage("newproject"); //$NON-NLS-1$
 		rm.addImage("openproject"); //$NON-NLS-1$
 		rm.addImage("saveproject"); //$NON-NLS-1$
 		rm.addImage("log"); //$NON-NLS-1$
 		rm.addImage("addinput"); //$NON-NLS-1$
 		rm.addImage("openfolder"); //$NON-NLS-1$
 		rm.addImage("properties"); //$NON-NLS-1$
 		rm.addImage("moveup"); //$NON-NLS-1$
 		rm.addImage("movedown"); //$NON-NLS-1$
 		rm.addImage("help"); //$NON-NLS-1$
 		
 		//TODO: deal with commands localization
 		rm.loadCommands("commands.xml"); //$NON-NLS-1$
 
 		fm = new FormatManager();
 		fm.load(null); // TODO: implement real external file, for now it's hard-coded
 		lm = new LanguageManager();
 		lm.loadList(sharedFolder + File.separator + "languages.xml"); //$NON-NLS-1$
 		em = new EncodingManager();
 		em.loadList(sharedFolder + File.separator + "encodings.xml"); //$NON-NLS-1$
 		plugins = new PluginsAccess();
 		//TODO: Choose a better location 
 		plugins.addAllPackages(sharedFolder);
 
 	}
 	
 	private void editUserPreferences () {
 		try {
 			saveSurfaceData();
 			PreferencesForm dlg = new PreferencesForm(shell, help);
 			dlg.setData(config);
 			dlg.showDialog();
 			setLogLevel();
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void changeRoot () {
 		try {
 			saveSurfaceData();
 			InputDialog dlg = new InputDialog(shell,
 				String.format(Res.getString("MainForm.rootCaption"), currentInput+1), //$NON-NLS-1$
 				Res.getString("MainForm.editRootLabel"), //$NON-NLS-1$
 				prj.getInputRoot(currentInput), null, 1);
 			dlg.setAllowEmptyValue(true);
 			String newRoot = dlg.showDialog();
 			if ( newRoot == null ) return; // Canceled
 			if ( newRoot.length() < 2 ) newRoot = ""; // Use project's //$NON-NLS-1$
 			//TODO: additional check, dir exists, no trailing separator, etc.
 			prj.setInputRoot(currentInput, newRoot, newRoot.length()>0);
 			resetDisplay(currentInput);
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 
 	/**
 	 * Gets the table index if the input currently focused. If none is
 	 * focused, tries to selected the first item. 
 	 * @return The index of the focused item, or -1.
 	 */
 	private int getFocusedInputIndex () {
 		int index = inputTables.get(currentInput).getSelectionIndex();
 		if ( index == -1 ) {
 			if ( inputTables.get(currentInput).getItemCount() > 0 ) {
 				inputTables.get(currentInput).select(0);
 				return inputTables.get(currentInput).getSelectionIndex();
 			}
 			else return -1;
 		}
 		return inputTables.get(currentInput).getSelectionIndex();
 	}
 	
 	private void saveUserConfiguration () {
 		// Set the window placement
 		config.setProperty("maximized", shell.getMaximized()); //$NON-NLS-1$
 		Rectangle r = shell.getBounds();
 		config.setProperty(OPT_BOUNDS, String.format("%d,%d,%d,%d", r.x, r.y, r.width, r.height));
 		// Set the MRU list
 		mruList.copyToProperties(config);
 		// Save to the user home directory as ".appname" file
 		config.save(APPNAME, getClass().getPackage().getImplementationVersion());
 	}
 
 	private boolean canContinue () {
 		try {
 			saveSurfaceData();
 			if ( !prj.isModified ) return true;
 			else {
 				// Ask confirmation
 				MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
 				dlg.setMessage(Res.getString("MainForm.askToSave")); //$NON-NLS-1$
 				dlg.setText(APPNAME);
 				switch  ( dlg.open() ) {
 				case SWT.NO:
 					return true;
 				case SWT.CANCEL:
 					return false;
 				}
 				// Else save the project
 				saveProject(prj.path);
 			}
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		return true;
 	}
 	
 	private void clearMRU () {
 		try {
 			mruList.clear();
 			updateMRU();
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void updateMRU () {
 		try {
 			// miMRU is the MenuItem where to attached the sub-menu
 			// Remove and dispose of the previous sub-menu
 			Menu oldMenu = miMRU.getMenu();
 			miMRU.setMenu(null);
 			if ( oldMenu != null ) oldMenu.dispose();
 
 			// Set the new one
 			if ( mruList.getfirst() == null ) {
 				// No items to set: it's disabled
 				miMRU.setEnabled(false);
 			}
 			else { // One or more items
 				// Create the menu
 				Menu submenu = new Menu(shell, SWT.DROP_DOWN);
 				int i = 0;
 				String path;
 				MenuItem menuItem;
 				Iterator<String> iter = mruList.getIterator();
 				while ( iter.hasNext() ) {
 					menuItem = new MenuItem(submenu, SWT.PUSH);
 					path = iter.next();
 					menuItem.setText(String.format("&%d %s", ++i, path)); //$NON-NLS-1$
 					menuItem.setData(path);
 					menuItem.addSelectionListener(new SelectionAdapter() {
 						public void widgetSelected(SelectionEvent event) {
 							openProject((String)((MenuItem)event.getSource()).getData());
 						}
 					});
 				}
 				miMRU.setMenu(submenu);
 				miMRU.setEnabled(true);
 			}
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void saveProject (String path) {
 		try {
 			if ( path == null ) {
 				path = Dialogs.browseFilenamesForSave(shell, Res.getString("MainForm.saveProjectBrowseCaption"), null, //$NON-NLS-1$
 					Res.getString("MainForm.33"), "*.rnb");  //$NON-NLS-1$//$NON-NLS-2$
 				if ( path == null ) return;
 				mruList.add(path);
 				updateMRU();
 			}
 			saveSurfaceData();
 			prj.save(path);
 			updateTitle();
 			edParamsFolder.setText(prj.getParametersFolder(true));
 			updateInputRoot();
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 
 	private void updateTitle () {
 		shell.setText(((prj.path == null)
 			? Res.getString("UNTITLED") //$NON-NLS-1$
 			: Util.getFilename(prj.path, true))
 			+ " - " //$NON-NLS-1$ 
 			+ APPNAME);
 	}
 
 	// Use -1 for all lists
 	private void resetDisplay (int listIndex) {
 		updateTitle();
 
 		if (( listIndex < 0 ) || ( listIndex == 0 ))
 			inputTableMods.get(0).setProject(prj.getList(0));
 		if (( listIndex < 0 ) || ( listIndex == 1 ))
 			inputTableMods.get(1).setProject(prj.getList(1));
 		if (( listIndex < 0 ) || ( listIndex == 2 ))
 			inputTableMods.get(2).setProject(prj.getList(2));
 		
 		setSurfaceData();
 		updateCommands();
 		if ( currentInput != -1 ) {
 			inputTables.get(currentInput).setFocus();
 		}
 	}
 	
 	private void createProject (boolean checkCanContinue ) {
 		if ( checkCanContinue ) {
 			if ( !canContinue() ) return;
 		}
 		
 		prj = new Project(lm);
 		//wrapper = new PipelineWrapper();
 		currentInput = 0;
 		resetDisplay(-1);
 	}
 	
 	private void openProject (String path) {
 		try {
 			if ( !canContinue() ) return;
 			if ( path == null ) {
 				String[] paths = Dialogs.browseFilenames(shell, Res.getString("MainForm.openProjectBrowsecaption"), false, null, //$NON-NLS-1$
 					Res.getString("MainForm.35"), "*.rnb\t*.*");  //$NON-NLS-1$//$NON-NLS-2$
 				if ( paths == null ) return;
 				path = paths[0];
 			}
 			
 			// Check if the file exists
 			if ( !(new File(path)).exists() ) {
 				Dialogs.showError(shell, Res.getString("MainForm.projectNotFound")+path, null); //$NON-NLS-1$
 				mruList.remove(path);
 				updateMRU();
 				return;
 			}
 
 			// Load it and update the UI
 			prj = new Project(lm);
 			prj.load(path);
 			mruList.add(path);
 			updateMRU();
 			resetDisplay(-1);
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 
 	private void setSurfaceData () {
 		for ( InputTableModel model : inputTableMods ) {
 			model.updateTable(null, 0);
 		}
 
 		updatePathBuilderSampleData();
 		chkUseOutputRoot.setSelection(prj.getUseOutputRoot());
 		edOutputRoot.setText(prj.getOutputRoot());
 
 		edSourceLang.setText(prj.getSourceLanguage());
 		edSourceEnc.setText(prj.getSourceEncoding());
 		edTargetLang.setText(prj.getTargetLanguage());
 		edTargetEnc.setText(prj.getTargetEncoding());
 		
 		chkUseCustomParametersFolder.setSelection(prj.useCustomParametersFolder());
 		btGetParamsFolder.setEnabled(chkUseCustomParametersFolder.getSelection());
 		edParamsFolder.setEditable(prj.useCustomParametersFolder());
 		edParamsFolder.setText(prj.getParametersFolder(true));
 		
 		// Updates
 		edOutputRoot.setEnabled(chkUseOutputRoot.getSelection());
 		updateInputRoot();
 		updateOutputRoot();
 	}
 	
 	private void updateInputRoot () {
 		if ( currentInput == -1 ) return;
 		stInputRoot.setText(String.format(Res.getString("MainForm.inputRootLabel"), currentInput+1)); //$NON-NLS-1$
 		edInputRoot.setText(prj.getInputRootDisplay(currentInput));
 		updateOutputRoot();
 	}
 
 	/*
 	 * Updates the data used to show the sample path in the path builder panel.
 	 */
 	private void updatePathBuilderSampleData () {
 		String sampleInput;
 		if ( inputTables.get(0).getItemCount() > 0 ) {
 			// Use the first file of the first list as example
 			sampleInput = prj.getInputRoot(0) + File.separator + inputTables.get(0).getItem(0).getText();
 		}
 		else {
 			sampleInput = prj.getInputRoot(0) + File.separator
 				+ Res.getString("MainForm.37") + File.separator //$NON-NLS-1$
 				+ Res.getString("MainForm.38"); //$NON-NLS-1$
 		}
 		pnlPathBuilder.setData(prj.pathBuilder, prj.getInputRoot(0), sampleInput,
 			prj.getOutputRoot(), prj.getSourceLanguage(), prj.getSourceEncoding());
 	}
 	
 	/**
 	 * Saves the UI-accessible properties of the project into the project object.
 	 */
 	private void saveSurfaceData () {
 		//TODO: Fix this, tmp is already equal because of example display
 		String tmp = prj.pathBuilder.toString();
 		pnlPathBuilder.saveData(prj.pathBuilder);
 		if ( !tmp.equals(prj.pathBuilder.toString()))
 			prj.isModified = true;
 	
 		prj.setUseOutputRoot(chkUseOutputRoot.getSelection());
 		prj.setOutputRoot(edOutputRoot.getText());
 		prj.setSourceLanguage(edSourceLang.getText());
 		prj.setSourceEncoding(edSourceEnc.getText());
 		prj.setTargetLanguage(edTargetLang.getText());
 		prj.setTargetEncoding(edTargetEnc.getText());
 		
 		prj.setUseCustomParametersFolder(chkUseCustomParametersFolder.getSelection());
 		if ( prj.useCustomParametersFolder() ) {
 			prj.setCustomParametersFolder(edParamsFolder.getText());
 		}
 	}
 
 	private void addDocumentsFromList (String[] paths) {
 		try {
 			if ( currentInput == -1 ) return;
 			// Get a list of paths if needed
 			if ( paths == null ) {
 				paths = Dialogs.browseFilenames(shell, Res.getString("MainForm.addDocsBrowsecaption"), //$NON-NLS-1$
 					true, prj.getInputRoot(currentInput), null, null);
 			}
 			if ( paths == null ) return;
 			// Add all the selected files and folders
 			startWaiting(Res.getString("MainForm.addingInputDocs"), false); //$NON-NLS-1$
 			doAddDocuments(paths, null);
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		finally {
 			inputTableMods.get(currentInput).updateTable(null, 0);
 			updatePathBuilderSampleData();
 			updateCommands();
 			stopWaiting();
 		}
 	}
 
 	private int doAddDocuments (String[] paths,
 		String dir)
 		throws Exception
 	{
 		int n = 0;
 		boolean allowDup = config.getBoolean(OPT_ALLOWDUPINPUT);
 		for ( String path : paths ) {
 			if ( dir != null ) {
 				path = dir + File.separator + path;
 			}
 			File f = new File(path);
 			if ( f.isDirectory() ) {
 				n += doAddDocuments(f.list(), path);
 			}
 			else {
 				String[] res = fm.guessFormat(path);
 				
 				// If project is not saved and it's the first added file:
 				if (( prj.path == null )
 					&& ( prj.inputLists.get(currentInput).size() == 0 )
 					&& !prj.useCustomeInputRoot(currentInput) )
 				{
 					// Set the root and the parameters folder to the file's directory
 					String root = Util.getDirectoryName(path);
 					prj.setInputRoot(currentInput, root, true);
 					chkUseCustomParametersFolder.setSelection(true);
 					edParamsFolder.setText(root);
 					prj.setCustomParametersFolder(root);
 					prj.setUseCustomParametersFolder(true);
 					resetDisplay(currentInput);
 				}
 				
 				switch ( prj.addDocument(currentInput, path, res[0], null, res[1], allowDup) ) {
 				case 0: // OK
 					n++;
 					break;
 				case 1: // Bad root
 					// Tell the user
 					Dialogs.showError(shell, Res.getString("MainForm.42")+path+Res.getString("MainForm.43"), null); //$NON-NLS-1$ //$NON-NLS-2$
 					return n;
 				default:
 					break;
 				}
 			}
 		}
 		return n;
 	}
 	
 	private void editInputProperties (int index) {
 		Table table = null;
 		try {
 			if ( currentInput == -1 ) return;
 			table = inputTables.get(currentInput);
 			saveSurfaceData();
 			int n = index;
 			if ( n < 0 ) {
 				if ( (n = getFocusedInputIndex()) < 0 ) return;
 			}
 			
 			Input inp = prj.getItemFromRelativePath(currentInput,
 				table.getItem(n).getText(0));
 
 			// Call the dialog
 			InputPropertiesForm dlg = new InputPropertiesForm(shell, help, this, prj.getProjectFolder());
 			dlg.setData(inp.filterSettings, inp.sourceEncoding,
 				inp.targetEncoding, fa);
 			String[] aRes = dlg.showDialog();
 			if ( aRes == null ) return;
 
 			// Update the file(s) data
 			if ( aRes[3] != null ) prj.isModified = true;
 			startWaiting(Res.getString("MainForm.updatingProject"), false); //$NON-NLS-1$
 			if ( index < 0 ) {
 				int[] indices = table.getSelectionIndices();
 				for ( int i=0; i<indices.length; i++ ) {
 					inp = prj.getItemFromRelativePath(currentInput,
 						table.getItem(indices[i]).getText(0));
 					inp.filterSettings = aRes[0];
 					inp.sourceEncoding = aRes[1];
 					inp.targetEncoding = aRes[2];
 				}
 			}
 			else {
 				inp = prj.getItemFromRelativePath(currentInput,
 					table.getItem(index).getText(0));
 				inp.filterSettings = aRes[0];
 				inp.sourceEncoding = aRes[1];
 				inp.targetEncoding = aRes[2];
 			}
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		finally {
 			if ( table != null ) {
 				inputTableMods.get(currentInput).updateTable(table.getSelectionIndices(), 0);
 			}
 			stopWaiting();
 		}
 	}
 
 	public IParameters createParameters (String location)
 		throws Exception
 	{
 		String[] aRes = FilterAccess.splitFilterSettingsType1(prj.getParametersFolder(),
 			location);
 		IFilter filter = fa.loadFilter(aRes[1], null, null);
 		return filter.getParameters();
 	}
 
 	public IParameters load (String location)
 		throws Exception
 	{
 		String[] aRes = FilterAccess.splitFilterSettingsType1(prj.getParametersFolder(),
 			location);
 		IFilter filter = fa.loadFilter(aRes[1], aRes[3], null);
 		return filter.getParameters();
 	}
 
 	public void save (String location,
 		IParameters paramsObject)
 		throws Exception
 	{
 		String[] aRes = FilterAccess.splitFilterSettingsType1(prj.getParametersFolder(),
 			location);
 		paramsObject.save(aRes[3]);
 	}
 	
 	public boolean deleteParameters (String location) {
 		String[] aRes = FilterAccess.splitFilterSettingsType1(prj.getParametersFolder(),
 			location);
 		File file = new File(aRes[3]);
 		return file.delete();
 	}
 
 	public String[] splitLocation (String location) {
 		return FilterAccess.splitFilterSettingsType1(prj.getParametersFolder(), location);
 	}
 
 	public String[] getParametersList () {
 		return FilterAccess.getParametersList(prj.getParametersFolder());
 	}
 
 	private void openDocument (int index) {
 		try {
 			if ( currentInput == -1 ) return;
 			if ( index < 0 ) {
 				if ( (index = getFocusedInputIndex()) < 0 ) return;
 			}
 			Input inp = prj.getItemFromRelativePath(currentInput,
 				inputTables.get(currentInput).getItem(index).getText(0));
 			Program.launch(prj.getInputRoot(currentInput) + File.separator + inp.relativePath); 
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void openContainingFolder (int index) {
 		try {
 			if ( currentInput == -1 ) return;
 			if ( index < 0 ) {
 				if ( (index = getFocusedInputIndex()) < 0 ) return;
 			}
 			Input inp = prj.getItemFromRelativePath(currentInput,
 				inputTables.get(currentInput).getItem(index).getText(0));
 			Program.launch(Util.getDirectoryName(
 				prj.getInputRoot(currentInput) + File.separator + inp.relativePath)); 
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void editSegmentationRules (String path) {
 		SRXEditor dlg = null;
 		try {
 			dlg = new SRXEditor(shell, true, help);
 			dlg.showDialog(path);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		finally {
 			if ( dlg != null ) dlg.dispose();
 		}
 	}
 	
 	private void showCharInfo () {
 		try {
 			CharacterInfoDialog dlg = new CharacterInfoDialog(shell, Res.getString("MainForm.charInfoCaption"), help); //$NON-NLS-1$
 			dlg.showDialog(0x9F99);
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void listEncodings () {
 		try {
 			int count = 0;
 			int countAlias = 0;
 			startWaiting(Res.getString("MainForm.listingEncodings"), true); //$NON-NLS-1$
 			log.beginTask(Res.getString("MainForm.listEncodingsTask")); //$NON-NLS-1$
 			SortedMap<String, Charset> charsets = Charset.availableCharsets();
 			for ( String key : charsets.keySet() ) {
 				log.message(charsets.get(key).displayName());
 				count++;
 				Set<String> aliases = charsets.get(key).aliases();
 				for ( String alias : aliases ) {
 					log.message("\t" + alias); //$NON-NLS-1$
 					countAlias++;
 				}
 			}
 			log.message(String.format(Res.getString("MainForm.numEncodings"), count)); //$NON-NLS-1$
 			log.message(String.format(Res.getString("MainForm.numAliases"), count+countAlias)); //$NON-NLS-1$
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		finally {
 			log.endTask(null);
 			stopWaiting();
 			log.show();
 		}
 	}
 	
 	private void removeDocuments (int index) {
 		boolean refresh = false;
 		try {
 			if ( index < 0 ) {
 				if ( getFocusedInputIndex() < 0 ) return;
 			}
 			// Ask confirmation
 			MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
 			dlg.setMessage(Res.getString("MainForm.askForRemove")); //$NON-NLS-1$
 			dlg.setText(APPNAME);
 			if ( dlg.open() != SWT.YES ) return;
 
 			refresh = true;
 			Input inp;
 			startWaiting(Res.getString("MainForm.updatingProject"), false); //$NON-NLS-1$
 			Table table = inputTables.get(currentInput);
 			if ( index < 0 ) {
 				int[] indices = table.getSelectionIndices();
 				index = indices[0];
 				for ( int i=0; i<indices.length; i++ ) {
 					inp = prj.getItemFromRelativePath(currentInput, table.getItem(indices[i]).getText(0));
 					prj.getList(currentInput).remove(inp);
 				}
 			}
 			else {
 				inp = prj.getItemFromRelativePath(currentInput, table.getItem(index).getText(0));
 				prj.getList(currentInput).remove(inp);
 			}
 			prj.isModified = true;
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		finally {
 			if ( refresh ) {
 				inputTableMods.get(currentInput).updateTable(null, index);
 				updateCommands();
 				stopWaiting();
 			}
 		}
 	}
 
 	private void moveDocumentsUp () {
 		try {
 			if ( currentInput == -1 ) return;
 			if ( getFocusedInputIndex() < 0 ) return;
 			// Get the selected documents
 			Table table = inputTables.get(currentInput);
 			int[] indices = table.getSelectionIndices();
 			
 			// Make sure the selection covers all items from first to last
 			int first = indices[0];
 			if ( first == 0 ) return; // Already at the top
 			int last = indices[indices.length-1];
 			table.select(first, last);
 			indices = table.getSelectionIndices();
 
 			// Move the document before first to position after last
 			Input inp = prj.getItemFromRelativePath(currentInput, table.getItem(first-1).getText(0));
 			prj.getList(currentInput).remove(inp);
 			prj.getList(currentInput).add(last, inp);
 			prj.isModified = true;
 
 			// Update the selection
 			for ( int i=0; i<indices.length; i++ ) {
 				indices[i] = indices[i]-1;
 			}
 			inputTableMods.get(currentInput).updateTable(indices, 0);
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void moveDocumentsDown () {
 		try {
 			if ( currentInput == -1 ) return;
 			if ( getFocusedInputIndex() < 0 ) return;
 			// Get the selected documents
 			Table table = inputTables.get(currentInput);
 			int[] indices = table.getSelectionIndices();
 			
 			// Make sure the selection covers all items from first to last
 			int last = indices[indices.length-1];
 			if ( last >= table.getItemCount()-1 ) return; // Already at the bottom
 			int first = indices[0];
 			table.select(first, last);
 			indices = table.getSelectionIndices();
 
 			// Move the document after last to position before first
 			Input inp = prj.getItemFromRelativePath(currentInput, table.getItem(last+1).getText(0));
 			prj.getList(currentInput).remove(inp);
 			prj.getList(currentInput).add(first, inp);
 			prj.isModified = true;
 
 			// Update the selection
 			for ( int i=0; i<indices.length; i++ ) {
 				indices[i] = indices[i]+1;
 			}
 			inputTableMods.get(currentInput).updateTable(indices, 0);
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 
 /*	private void testRunnable () {
 		try {
 //			startWaiting("Test", true);
 //			RunnableTest test = new RunnableTest(shell.getDisplay(), log);
 //			Thread T = new Thread(test);
 //			T.start();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}*/
 }
