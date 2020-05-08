 package collector.desktop;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import collector.desktop.database.DatabaseWrapper;
 import collector.desktop.filesystem.FileSystemAccessWrapper;
 import collector.desktop.filesystem.export.CSVExporter;
 import collector.desktop.filesystem.export.HTMLExporter;
 import collector.desktop.gui.AlbumManager;
 import collector.desktop.gui.AlbumViewManager;
 import collector.desktop.gui.AlbumViewManager.AlbumView;
 import collector.desktop.gui.BrowserContent;
 import collector.desktop.gui.BrowserListener;
 import collector.desktop.gui.ComponentFactory;
 import collector.desktop.gui.CompositeFactory;
 import collector.desktop.gui.LoadingOverlayShell;
 import collector.desktop.gui.PanelType;
 import collector.desktop.gui.StatusBarComposite;
 import collector.desktop.gui.ToolbarComposite;
 import collector.desktop.interfaces.UIObservable;
 import collector.desktop.interfaces.UIObserver;
 import collector.desktop.internationalization.DictKeys;
 import collector.desktop.internationalization.Translator;
 import collector.desktop.settings.ApplicationSettingsManager;
 
 public class Collector implements UIObservable, UIObserver {
 	private static final int RIGHT_PANEL_LARGE_WIDTH = 320;
 	private static final int RIGHT_PANEL_MEDIUM_WIDTH = 225;
 	private static final int RIGHT_PANEL_SMALL_WIDTH = 150;
 	private static final int RIGHT_PANEL_NO_WIDTH = 0;
 
 	/** The minimum width of the shell in pixels. The shell can never have a smaller width than this. */
 	private static final int MIN_SHELL_WIDTH = 1150;
 	/** The minimum height of the shell in pixels. The shell can never have a smaller height than this. */
 	private static final int MIN_SHELL_HEIGHT = 700;
 	/** A reference to the main display */
 	private final static Display display = new Display();
 	/** A reference to the main shell */
 	private final static Shell shell = new Shell(display);
 	/** A reference to a composite being part of the general user interface */
 	private static Composite threePanelComposite = null, upperLeftSubComposite = null, lowerLeftSubComposite = null, 
 			leftComposite = null, rightComposite = null, centerComposite = null, statusComposite = null, toolbarComposite = null;
 	/** The currently selected album. The selected album changes via selections within the album list */
 	private static String selectedAlbum;
 	/** A reference to the SWT list containing all available albums */
 	private static List albumSWTList;
 	/** A reference to the SWT Text representing the quickSearch field*/
 	private static Text quickSearchTextField;
 	/** A reference to the SWT list containing all available views */
 	private static List viewSWTList;
 	/** True if the current view is list based, false if item based (picture based) */
 	private static boolean viewIsDetailed = true;	
 	/** A reference to the SWT browser in charge of presenting album items */
 	private static Browser albumItemSWTBrowser;
 	/** A reference to the SWT album item browser listener*/
 	private static BrowserListener albumItemSWTBrowserListener;
 	/** The panel type that is currently visible on the right of the main three panel composite */
 	private static PanelType currentRightPanelType = PanelType.Empty;
 	/** A list of observers, waiting for certain global changes */
 	private static ArrayList<UIObserver> observers = new ArrayList<UIObserver>();
 	/** An instance to the main collector */
 	private static Collector instance = null;
 	/** This flag indicates if an error (e.g. corrupt db) was encountered during startup*/
 	private static boolean normalStartup = true;
 	/**
 	 * The default constructor initializes the file structure and opens the database connections.
 	 * Furthermore the constructor creates the program instance which is used to register observers
 	 * @throws Exception Either a class not found excpetion if the jdbc driver could not be initialized or
 	 * an exception if the database connection could not be established.
 	 */
 	private Collector() throws Exception {
 		Class.forName("org.sqlite.JDBC");
 		
 		if (!DatabaseWrapper.openConnection()) {	
 			normalStartup =  false;
 			if (DatabaseWrapper.openCleanConnection() == false) {
 				throw new Exception("Could not open a database connection");
 			}
 		}
 		
 		instance = this;
 	}
 
 	/** This method creates the main user interface. This involves the creation of different sub-composites 
 	 * using the CompositeFactory 
 	 * @param shell the shell used to create the user interface */
 	public static void createCollectorShell(final Shell shell) {				
 		// setup the Layout for the shell
 		GridLayout shellGridLayout = new GridLayout(1, false);
 		shellGridLayout.marginHeight = 0;
 		shellGridLayout.marginWidth = 0;
 		
 		shell.setMinimumSize(MIN_SHELL_WIDTH, MIN_SHELL_HEIGHT);
 		//shell.setSize(MIN_SHELL_WIDTH, MIN_SHELL_HEIGHT);TODO: check if needed or not
 		// setup the Shell
 		shell.setText(Translator.get(DictKeys.TITLE_MAIN_WINDOW));				
 		shell.setLayout(shellGridLayout);
 
 		// center the shell to primary screen
 		Monitor primaryMonitor = display.getPrimaryMonitor();
 		Rectangle primaryMonitorBounds = primaryMonitor.getClientArea();
 		Rectangle shellBounds = shell.getBounds();
 		int xCoordinateForShell = primaryMonitorBounds.x + (primaryMonitorBounds.width - shellBounds.width) / 2;
 		int yCoordinateForShell = primaryMonitorBounds.y + (primaryMonitorBounds.height - shellBounds.height) / 2;
 		shell.setLocation(xCoordinateForShell, yCoordinateForShell);
 
 		// define toolbar composite layout data
 		GridData gridDataForToolbarComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForToolbarComposite.grabExcessHorizontalSpace = true;
 		gridDataForToolbarComposite.grabExcessVerticalSpace = false;
 
 		// define three panel composite layout data
 		GridData gridDataForThreePanelComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForThreePanelComposite.grabExcessHorizontalSpace = true;
 		gridDataForThreePanelComposite.grabExcessVerticalSpace = true;
 		gridDataForThreePanelComposite.verticalAlignment = GridData.FILL;
 		gridDataForThreePanelComposite.horizontalAlignment = GridData.FILL;
 		GridLayout mainGridLayout = new GridLayout(3, false);
 
 		// define left (upper & lower) composite layout data
 		GridData gridDataForLeftComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForLeftComposite.grabExcessHorizontalSpace = false;
 		GridData gridDataForUpperLeftComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForUpperLeftComposite.verticalAlignment = GridData.BEGINNING;
 		GridData gridDataForLowerLeftComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForLowerLeftComposite.verticalAlignment = GridData.END;
 
 		// define center composite layout data
 		GridData gridDataForCenterComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
 
 		// define right composite layout data
 		GridData gridDataForRightComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForRightComposite.grabExcessHorizontalSpace = false;
 		gridDataForRightComposite.verticalAlignment = GridData.BEGINNING;
 
 		// define statusbar composite layout data
 		GridData gridDataForStatusBarComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForStatusBarComposite.grabExcessHorizontalSpace = true;
 		gridDataForStatusBarComposite.grabExcessVerticalSpace = false;
 
 		// Setup composites using layout definitions from before
 		toolbarComposite = ToolbarComposite.getInstance(shell).getToolBarComposite();
 		toolbarComposite.setLayout(new GridLayout(1, false));
 		toolbarComposite.setLayoutData(gridDataForToolbarComposite);
 
 		threePanelComposite = new Composite(shell, SWT.NONE);
 		threePanelComposite.setLayout(mainGridLayout);
 		threePanelComposite.setLayoutData(gridDataForThreePanelComposite);
 
 		leftComposite = new Composite(threePanelComposite, SWT.NONE);
 		leftComposite.setLayout(new GridLayout(1, false));
 		leftComposite.setLayoutData(gridDataForLeftComposite);
 		upperLeftSubComposite = CompositeFactory.getQuickControlComposite(leftComposite);
 		upperLeftSubComposite.setLayoutData(gridDataForUpperLeftComposite);
 		lowerLeftSubComposite = CompositeFactory.getEmptyComposite(leftComposite);		
 		lowerLeftSubComposite.setLayoutData(gridDataForLowerLeftComposite);
 		albumItemSWTBrowserListener = new BrowserListener(threePanelComposite);
 		centerComposite = CompositeFactory.getBrowserComposite(threePanelComposite, albumItemSWTBrowserListener);
 		centerComposite.setLayout(new GridLayout(1, false));
 		centerComposite.setLayoutData(gridDataForCenterComposite);
 		rightComposite = CompositeFactory.getEmptyComposite(threePanelComposite);
 		rightComposite.setLayout(new GridLayout(1, false));
 		rightComposite.setLayoutData(gridDataForRightComposite);
 
 		statusComposite = StatusBarComposite.getInstance(shell).getStatusbarComposite();
 		statusComposite.setLayout(new GridLayout(1, false));
 		statusComposite.setLayoutData(gridDataForStatusBarComposite);
 
 		// Create the menu bar
 		createMenuBar(shell);
 		
 		// SWT display management
 		shell.pack();
 
 		Rectangle displayClientArea = display.getPrimaryMonitor().getClientArea();
 		if (maximizeShellOnStartUp(displayClientArea.width, displayClientArea.height)){
 			shell.setMaximized(true);
 		}
 		
 		createAutosaveOverlay();		
 		
 		shell.open();
 		
 		if (!normalStartup) {
 			//TODO: make international
 			ComponentFactory.getMessageBox(shell, "Fatal error occured during startup", "The database is corrupt and was removed. A snapshot can be found in the program folder", SWT.ICON_INFORMATION).open();
 		}
 
 		selectDefaultAndShowWelcomePage();		
 
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 
 		display.dispose();
 	}
 
 	public static Composite getThreePanelComposite() {
 		return threePanelComposite;
 	}
 
 	private static void selectDefaultAndShowWelcomePage() {
 		if (albumSWTList.getItemCount() > 0) {
 			albumSWTList.setSelection(-1);
 		}
 
 		BrowserContent.loadWelcomePage();
 	}
 
 	/** This method creates the menu for the main user interface
 	 * @param shell the shell used to create the user interface */
 	private static void createMenuBar(Shell parentShell) {
 		// Create the bar menu
 		Menu menu = new Menu(getShell(), SWT.BAR);
 
 		// Create all the items in the bar menu
 		MenuItem collectorItem = new MenuItem(menu, SWT.CASCADE);
 		collectorItem.setText(Translator.get(DictKeys.MENU_COLLECTOR));
 		MenuItem albumItem = new MenuItem(menu, SWT.CASCADE);
 		albumItem.setText(Translator.get(DictKeys.MENU_ALBUM));
 		MenuItem synchronizeItem = new MenuItem(menu, SWT.CASCADE);
 		synchronizeItem.setText(Translator.get(DictKeys.MENU_SYNCHRONIZE));
 		MenuItem settingsItem = new MenuItem(menu, SWT.CASCADE);
 		settingsItem.setText(Translator.get(DictKeys.MENU_SETTINGS));
 		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
 		helpItem.setText(Translator.get(DictKeys.MENU_HELP));
 
 		// Create the Collector item's dropdown menu
 		Menu collectorMenu = new Menu(menu);
 		collectorItem.setMenu(collectorMenu);
 
 		// Create all the items in the Collector dropdown menu
 		MenuItem exportItem = new MenuItem(collectorMenu, SWT.NONE);
 		exportItem.setText(Translator.get(DictKeys.MENU_EXPORT_VISIBLE_ITEMS));
 		exportItem.addSelectionListener(instance.new MenuActionListener());
 		new MenuItem(collectorMenu, SWT.SEPARATOR);
 		MenuItem backupItem = new MenuItem(collectorMenu, SWT.NONE);
 		backupItem.setText(Translator.get(DictKeys.MENU_BACKUP_ALBUMS_TO_FILE));
 		backupItem.addSelectionListener(instance.new MenuActionListener());
 		MenuItem restoreItem = new MenuItem(collectorMenu, SWT.NONE);
 		restoreItem.setText(Translator.get(DictKeys.MENU_RESTORE_ALBUM_FROM_FILE));
 		restoreItem.addSelectionListener(instance.new MenuActionListener());
 		new MenuItem(collectorMenu, SWT.SEPARATOR);
 		MenuItem exitItem = new MenuItem(collectorMenu, SWT.NONE);
 		exitItem.setText(Translator.get(DictKeys.MENU_EXIT));
 		exitItem.addSelectionListener(instance.new MenuActionListener());
 
 		// Create the Album item's dropdown menu
 		Menu albumMenu = new Menu(menu);
 		albumItem.setMenu(albumMenu);
 
 		// Create all the items in the Album dropdown menu
 		MenuItem advancedSearch = new MenuItem(albumMenu, SWT.NONE);
 		advancedSearch.setText(Translator.get(DictKeys.MENU_ADVANCED_SEARCH));
 		advancedSearch.addSelectionListener(instance.new MenuActionListener());
 		new MenuItem(albumMenu, SWT.SEPARATOR);
 		MenuItem createAlbum = new MenuItem(albumMenu, SWT.NONE);
 		createAlbum.setText(Translator.get(DictKeys.MENU_CREATE_NEW_ALBUM));
 		createAlbum.addSelectionListener(instance.new MenuActionListener());
 		MenuItem alterAlbum = new MenuItem(albumMenu, SWT.NONE);
 		alterAlbum.setText(Translator.get(DictKeys.MENU_ALTER_SELECTED_ALBUM));
 		alterAlbum.addSelectionListener(instance.new MenuActionListener());
 		new MenuItem(albumMenu, SWT.SEPARATOR);
 		MenuItem deleteAlbum = new MenuItem(albumMenu, SWT.NONE);
 		deleteAlbum.setText(Translator.get(DictKeys.MENU_DELETE_SELECTED_ALBUM));
 		deleteAlbum.addSelectionListener(instance.new MenuActionListener());	
 
 		// Create the Synchronize item's dropdown menu
 		Menu synchronizeMenu = new Menu(menu);
 		synchronizeItem.setMenu(synchronizeMenu);
 
 		// Create all the items in the Synchronize dropdown menu
 		MenuItem Synchronize = new MenuItem(synchronizeMenu, SWT.NONE);
 		Synchronize.setText(Translator.get(DictKeys.MENU_SYNCHRONIZE));
 		Synchronize.addSelectionListener(instance.new MenuActionListener());
 
 		// Create the Settings item's dropdown menu
 		Menu settingsMenu = new Menu(menu);
 		settingsItem.setMenu(settingsMenu);
 
 		// Create all the items in the Settings dropdown menu
 		MenuItem settings = new MenuItem(settingsMenu, SWT.NONE);
 		settings.setText(Translator.get(DictKeys.MENU_SETTINGS));
 		settings.addSelectionListener(instance.new MenuActionListener());
 		
 		// Create the Help item's dropdown menu
 		Menu helpMenu = new Menu(menu);
 		helpItem.setMenu(helpMenu);
 
 		// Create all the items in the Help dropdown menu
 		MenuItem helpContentsMenu = new MenuItem(helpMenu, SWT.NONE);
 		helpContentsMenu.setText(Translator.get(DictKeys.MENU_HELP_CONTENTS));
 		helpContentsMenu.addSelectionListener(instance.new MenuActionListener());
 
 		MenuItem aboutMenu = new MenuItem(helpMenu, SWT.NONE);
 		aboutMenu.setText(Translator.get(DictKeys.MENU_ABOUT));
 		aboutMenu.addSelectionListener(instance.new MenuActionListener());
 
 		// Set the bar menu as the menu in the shell
 		getShell().setMenuBar(menu);
 	} 
 
 	/** This method exchanges the center composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
 	 * @param newCenterComposite the new composite for the center element of the user interface */
 	public static void changeCenterCompositeTo(Composite newCenterComposite) {
 		Layout layout = centerComposite.getLayout();
 		GridData layoutData = (GridData) centerComposite.getLayoutData();
 
 		centerComposite.dispose();
 		newCenterComposite.setLayout(layout);
 		newCenterComposite.setLayoutData(layoutData);
 
 		centerComposite = newCenterComposite;
 		centerComposite.moveAbove(rightComposite);
 		centerComposite.getParent().layout();
 	}
 
 	/** This method exchanges the lower left composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
 	 * @param newLowerLeftComposite the new composite for the lower left element of the user interface */
 	public static void changeLowerLeftCompositeTo(Composite newLowerLeftComposite) {
 		Layout layout = lowerLeftSubComposite.getLayout();
 		GridData layoutData = (GridData) lowerLeftSubComposite.getLayoutData();
 
 		lowerLeftSubComposite.dispose();
 		newLowerLeftComposite.setLayout(layout);
 		newLowerLeftComposite.setLayoutData(layoutData);
 
 		lowerLeftSubComposite = newLowerLeftComposite;
 		lowerLeftSubComposite.moveBelow(upperLeftSubComposite);
 		lowerLeftSubComposite.getParent().layout();
 	}
 
 	private static HashMap<PanelType, Integer> panelTypeToPixelSize = new HashMap<PanelType, Integer>() {
 		private static final long serialVersionUID = 1L;	{
 			put(PanelType.Empty, RIGHT_PANEL_NO_WIDTH);
 			put(PanelType.AddAlbum, RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.AddEntry, RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.AdvancedSearch, RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.AlterAlbum, RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.Synchronization, RIGHT_PANEL_MEDIUM_WIDTH);
 			put(PanelType.UpdateEntry, RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.Help, RIGHT_PANEL_SMALL_WIDTH);
 		}
 	};
 
 	public static void resizeRightCompositeTo(int pixels) {
 		GridData layoutData = new GridData(GridData.FILL_BOTH);
 		layoutData.grabExcessHorizontalSpace = false;
 		layoutData.grabExcessVerticalSpace = true;
 		layoutData.verticalAlignment = GridData.BEGINNING;
 		layoutData.widthHint = pixels;
 
 		rightComposite.setLayoutData(layoutData);
 		rightComposite.getParent().layout();
 	}
 
 	/** This method exchanges the right composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
 	 * @param newRightComposite the new composite for the right element of the user interface */
 	public static void changeRightCompositeTo(PanelType panelType, Composite newRightComposite) {
 		currentRightPanelType = panelType;
 
 		GridData layoutData = new GridData(GridData.FILL_BOTH);
 		layoutData.grabExcessHorizontalSpace = false;
 		layoutData.grabExcessVerticalSpace = true;
 		layoutData.verticalAlignment = GridData.BEGINNING;
 
 		if (panelTypeToPixelSize.containsKey(panelType)) {
 			if (ScrolledComposite.class.isInstance(newRightComposite)) {
 				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
 				layoutData.widthHint = panelTypeToPixelSize.get(panelType) - sc.getVerticalBar().getSize().x;
 			} else {		
 				layoutData.widthHint = panelTypeToPixelSize.get(panelType);
 			}
 		} else {
 			if (ScrolledComposite.class.isInstance(newRightComposite)) {
 				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
 				layoutData.widthHint = RIGHT_PANEL_MEDIUM_WIDTH - sc.getVerticalBar().getSize().x;
 			} else {		
 				layoutData.widthHint = RIGHT_PANEL_MEDIUM_WIDTH;
 			}
 		}
 
 		newRightComposite.setLayoutData(layoutData);
 
 		rightComposite.dispose();
 		rightComposite = newRightComposite;
 		rightComposite.moveBelow(centerComposite);
 		rightComposite.getParent().layout();
 
 		instance.notifyObservers();
 	}
 
 	/** Returns the currently selected/active album or view
 	 * @return the currently selected/active album or view */
 	public static String getSelectedAlbum() {
 		return selectedAlbum;
 	}
 
 	/**
 	 * Determines is an album has been selected.
 	 * @return True if the selectedAlbumName is not null and not empty. True if an album is selected.
 	 */
 	public static boolean hasSelectedAlbum() {
 		if (selectedAlbum != null && !selectedAlbum.isEmpty()) {
 			return true;
 		}
 		return false;
 	}
 	
 	public static void setQuickSearchTextField(Text quickSearchTextField) {
 		Collector.quickSearchTextField = quickSearchTextField;
 	}
 	
 	public static Text getQuickSearchTextField() {
 		return Collector.quickSearchTextField;
 	}
 
 	/** Sets the currently selected/active album
 	 * @param albumName The name of the now selected/active album. If the albumName is null or empty then all albums are deselected.  
 	 * @return True if the album is selected internally and in the SWT Album list. If all albums were successfully deselected then true is also returned. 
 	 * False otherwise.*/
 	public static boolean setSelectedAlbum(String albumName) {
 		// Set the album name and verify that it is in the list
 		Collector.selectedAlbum = albumName;
 		if (albumName== null || albumName.isEmpty()) {
 			Collector.albumSWTList.deselectAll();
 			return true;
 		}
 		
 		int albumListItemCount = Collector.albumSWTList.getItemCount();
 		boolean albumSelectionIsInSync = false;
 		for (int itemIndex = 0; itemIndex<albumListItemCount; itemIndex++) {
 			 if ( Collector.albumSWTList.getItem(itemIndex).equals(albumName) ) {
 				 Collector.albumSWTList.setSelection(itemIndex);
 				 albumSelectionIsInSync = true;
 				 break;
 			 }
 		}
 		if (!albumSelectionIsInSync){
 			System.err.println("The album list does not contain the album that is supposed to be selected.");// TODO:log instead of printout
 			return false;
 		}
 	
 		Collector.getQuickSearchTextField().setText("");
 		Collector.getQuickSearchTextField().setEnabled(
 				DatabaseWrapper.isAlbumQuicksearchable(albumName));
 
 		BrowserContent.performBrowserQueryAndShow(
 				Collector.getAlbumItemSWTBrowser(), 							
 				DatabaseWrapper.createSelectStarQuery(albumName));
 		
 		Collector.getViewSWTList().setEnabled(AlbumViewManager.hasAlbumViewsAttached(albumName));
 		AlbumViewManager.getInstance().notifyObservers();
 		
 		// TODO: check if null could be passed because the toolbar always exists when this method is called.		
 		ToolbarComposite.getInstance(Collector.getThreePanelComposite()).enableAlbumButtons(albumName);
 		
 		return true;
 	}
 	
 	/** After adding/removing albums, this method should be used to refresh the SWT album list with the current album names thus leaving no album selected.*/
 	public static void refreshSWTAlbumList() {
 		instance.update(AlbumManager.class);
 		Collector.getQuickSearchTextField().setEnabled(false);
 	}
 
 	/** Sets the the list of albums
 	 * @param albumSWTList the list of albums */ 
 	public static void setAlbumSWTList(List albumSWTList) {
 		Collector.albumSWTList = albumSWTList;
 	}
 
 	/** Returns the list of albums 
 	 * @return the album SWT list */
 	public static List getAlbumSWTList() {
 		return albumSWTList;
 	}
 
 	/** Sets the the list of views
 	 * @param albumSWTList the list of albums */ 
 	public static void setViewSWTList(List viewSWTList) {
 		Collector.viewSWTList = viewSWTList;
 	}
 
 	/** Returns the list of views 
 	 * @return the album SWT list */
 	public static List getViewSWTList() {
 		return viewSWTList;
 	}
 
 	/** Sets the album item SWT browser
 	 * @param browser the reference to the albumItemSWTBrowser */
 	public static void setAlbumItemSWTBrowser(Browser browser) {
 		Collector.albumItemSWTBrowser = browser;
 	}
 
 	/** Returns the album item SWT browser
 	 * @return the album item SWT browser */
 	public static Browser getAlbumItemSWTBrowser() {
 		return albumItemSWTBrowser;
 	}
 
 	/** This class is used to catch and process every menu action */
 	private class MenuActionListener extends SelectionAdapter {
 		public void widgetSelected(SelectionEvent event) {
 			
 			if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_EXIT))) {
 				getShell().close();
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_CREATE_NEW_ALBUM))) {
 				changeRightCompositeTo(PanelType.AddAlbum, CompositeFactory.getCreateNewAlbumComposite(threePanelComposite));
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_RESTORE_ALBUM_FROM_FILE))) {
 				changeRightCompositeTo(PanelType.Empty, CompositeFactory.getEmptyComposite(Collector.threePanelComposite));
 				
 				FileDialog openFileDialog = new FileDialog(getShell(), SWT.OPEN);
 				openFileDialog.setText(Translator.get(DictKeys.DIALOG_RESTORE_FROM_FILE));
 				openFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.cbk" };
 				openFileDialog.setFilterExtensions(filterExt);
 
 				String path = openFileDialog.open();
 				if (path != null) {
 					DatabaseWrapper.restoreFromFile(path);
 					// No default album is selected on restore
 					Collector.refreshSWTAlbumList();
 					BrowserContent.loadHtmlPage(Collector.getAlbumItemSWTBrowser(), getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/albums_restored.html"));
 				}
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_HELP_CONTENTS))) {
 				// No default album is selected on help
 				Collector.refreshSWTAlbumList();
 				BrowserContent.loadHtmlPage(
 						getAlbumItemSWTBrowser(),
 						getShell().getClass().getClassLoader().getResourceAsStream("helpfiles/index.html"));
 				changeRightCompositeTo(PanelType.Help, CompositeFactory.getEmptyComposite(threePanelComposite));
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_ABOUT))) {
 				// No default album is selected on help
 				Collector.refreshSWTAlbumList();
 				BrowserContent.loadHtmlPage(
 						getAlbumItemSWTBrowser(),
 						getShell().getClass().getClassLoader().getResourceAsStream("helpfiles/about.html"));				
 				changeRightCompositeTo(PanelType.Help, CompositeFactory.getEmptyComposite(threePanelComposite));
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_SYNCHRONIZE))) {
 				changeRightCompositeTo(PanelType.Synchronization, CompositeFactory.getSynchronizeComposite(threePanelComposite));
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_SETTINGS))) {
 				changeRightCompositeTo(PanelType.Settings, CompositeFactory.getSettingsComposite(threePanelComposite));
 			} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_BACKUP_ALBUMS_TO_FILE))) {
 				FileDialog saveFileDialog = new FileDialog(getShell(), SWT.SAVE);
 				saveFileDialog.setText(Translator.get(DictKeys.DIALOG_BACKUP_TO_FILE));
 				saveFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.cbk" };
 				saveFileDialog.setFilterExtensions(filterExt);
 
 				String path = saveFileDialog.open();
 				if (path != null) {
 					DatabaseWrapper.backupToFile(path);
 				}		        
 			} else {
 				// --------------------------------------------------------------
 				// Ensure that the following context sensitive actions are applied only when an album has been selected. // TODO comment style
 				// --------------------------------------------------------------
 				if (!Collector.hasSelectedAlbum()) {
 					ComponentFactory.showErrorDialog(
 							Collector.getShell(), 
 							Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
 							Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
 					
 					return;
 				}
 				if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_DELETE_SELECTED_ALBUM))) {
 
 					MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
 					messageBox.setText(Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM));
 					messageBox.setMessage(Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM, Collector.getSelectedAlbum()));
 					if (messageBox.open() == SWT.YES) {
 						DatabaseWrapper.removeAlbum(getSelectedAlbum());
 						Collector.refreshSWTAlbumList();
 						BrowserContent.loadHtmlPage(Collector.getAlbumItemSWTBrowser(), getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/album_deleted.html"));
 					}
 				} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_ALTER_SELECTED_ALBUM))) {
 					changeRightCompositeTo(PanelType.AlterAlbum, CompositeFactory.getAlterAlbumComposite(threePanelComposite, getSelectedAlbum()));
 				} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_EXPORT_VISIBLE_ITEMS))) {
 					FileDialog saveFileDialog = new FileDialog(getShell(), SWT.SAVE);
 					saveFileDialog.setText(Translator.get(DictKeys.DIALOG_EXPORT_VISIBLE_ITEMS));
 					saveFileDialog.setFilterPath(System.getProperty("user.home"));
 					String[] filterExt = { "*.html", "*.csv"};
 					saveFileDialog.setFilterExtensions(filterExt);
 					String[] filterNames = { Translator.get(DictKeys.DIALOG_HTML_FOR_PRINT) , Translator.get(DictKeys.DIALOG_CSV_FOR_SPREADSHEET) };
 					saveFileDialog.setFilterNames(filterNames);
 
 					String filepath = saveFileDialog.open();
 					if (filepath != null) {
 						if (filepath.endsWith(".csv")) {
 							CSVExporter.exportVisibleItems(filepath);
 						} else if (filepath.endsWith(".html")) {
 							HTMLExporter.exportVisibleItems(filepath);
 						} else {
 							// TODO: support further export types. 
 						}
 					}
 				} else if (((MenuItem) event.widget).getText().equals(Translator.get(DictKeys.MENU_ADVANCED_SEARCH))) {
 					changeRightCompositeTo(PanelType.AdvancedSearch, CompositeFactory.getAdvancedSearchComposite(threePanelComposite, selectedAlbum));
 				}
 			}
 		}
 	}
 
 	/** The main method initializes the database (using the collector constructor) and establishes the user interface */
 	public static void main(String[] args) throws ClassNotFoundException {
 	    try {
 	    	ApplicationSettingsManager.loadFromSettingsFile();
 	    	Translator.setLanguageFromSettingsOrSystem();
 	    	// Ensure that folder structure including lock file exists before locking
 	    	FileSystemAccessWrapper.updateCollectorFileStructure();
 	    	
 	    	RandomAccessFile randomFile = new RandomAccessFile(FileSystemAccessWrapper.LOCK_FILE, "rw");
 		    FileChannel channel = randomFile.getChannel();
 		    
 		    if (channel.tryLock() != null) {
 				// Initialize the Database connection
 				new Collector();
 		
 				// Register the toolbar as an observer for collector updates
 				AlbumManager.getInstance().registerObserver(instance);
 				ToolbarComposite.getInstance(Collector.getShell()).registerAsObserverToCollectorUpdates();
 				AlbumViewManager.getInstance().registerObserver(instance);
 		
 				// create the shell and show the user interface. This blocks until the shell is closed
 				createCollectorShell(getShell());
 		
 				// close the database connection if the the shell is closed
 				DatabaseWrapper.closeConnection();
 				
 				// close file & channel
 				channel.close();
 				randomFile.close();
 		    } else {
 		    	ComponentFactory.getMessageBox(getShell(), 
 		    			Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
 		    			Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
 		    			SWT.ICON_INFORMATION).open();
 		    }
 	    } catch (Exception ex) {
 	    	System.out.println(ex.toString()); // TODO log me
 	    }
 	}
 	
 	public static PanelType getCurrentRightPanelType() {
 		return currentRightPanelType;
 	}
 
 	public static void setCurrentRightPanelType(PanelType currentRightPanel) {
 		Collector.currentRightPanelType = currentRightPanel;
 	}
 
 	public void registerObserver(UIObserver observer) {
 		observers.add(observer);
 	}
 
 	public void unregisterObserver(UIObserver observer) {
 		observers.remove(observer);
 	}
 
 	public void unregisterAllObservers() {
 		observers.clear();
 	}
 
 	public void notifyObservers() {
 		for (UIObserver observer : observers) {
 			observer.update(this.getClass());
 		}
 	}
 
 	public static Collector getInstance() {
 		return instance;
 	}
 
 	public static boolean isViewDetailed() {
 		return viewIsDetailed;
 	}
 
 	public static void setViewIsDetailed(boolean viewIsDetailed) {
 		Collector.viewIsDetailed = viewIsDetailed;
 	}
 
 	public static Shell getShell() {
 		return shell;
 	}
 
 	@Override
 	public void update(Class<?> origin) {
 		if (origin == AlbumManager.class) {
 			albumSWTList.removeAll();
 			
 			for (String album : AlbumManager.getInstance().getAlbums()) {
 				albumSWTList.add(album);
 			}
 		} else if (origin == AlbumViewManager.class) {
 			viewSWTList.removeAll();
 
 			for (AlbumView albumView : AlbumViewManager.getAlbumViews(selectedAlbum)) {
 				viewSWTList.add(albumView.getName());				
 			}
 			
 			if (viewSWTList.isEnabled() == false && viewSWTList.getItemCount() != 0) {
 				viewSWTList.setEnabled(true);
 			}
 		}
 	}
 	
 	/**
 	 * When the horizontal or vertical screen resolution is smaller than their respective thresholds
 	 * {@value #MIN_DISPLAY_WIDTH_BEFORE_MAXIMIZE} and {@link #MIN_DISPLAY_HEIGHT_BEFORE_MAXIMIZE} then
 	 * it returns true, False otherwise.
 	 */
 	private static boolean maximizeShellOnStartUp(int screenWidth, int screenHeight) {
 		if (MIN_SHELL_WIDTH >= screenWidth || MIN_SHELL_HEIGHT >= screenHeight){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private static void launchLoadingOverlayShell(final LoadingOverlayShell shell, boolean useWorkerThread) {
 		shell.start();
 		if (useWorkerThread){
 			final Thread performer = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					executeTask(shell);
 				}
 			}, "perform action");
 			performer.start();
 		}else {
 			executeTask(shell);
 		}
 	}
 
 	private static void executeTask(LoadingOverlayShell shell) {
 		// Do work in parallel to UI Thread
 		// Backup database 
 		try {
 			Thread.sleep(0);
 			if(!DatabaseWrapper.backupAutoSave() ){
 				System.err.println("Error while autosaving!!");
 			}
			
			FileSystemAccessWrapper.recursiveDeleteFSObject(new File("C:/Users/ameos/.collector/app-data/0ebced74-1187-4519-965d-aad1b699d3a8"));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		// After task is completed stop the shell
 		shell.stop();
 	}	
 	
 	private static LoadingOverlayShell createAutosaveOverlay () {
 		// TODO: replace message text by international string
 		final LoadingOverlayShell loadingOverlayShell = new LoadingOverlayShell(shell, " Autosaving the database");
 		loadingOverlayShell.setCloseParentWhenDone(true);
 		shell.addListener(SWT.Close, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				// Back up db file while displaying the loading overlay
 				if (!loadingOverlayShell.isDone()) {
 					// Setup the database backup overlay
 					launchLoadingOverlayShell(loadingOverlayShell, false);					
 					event.doit =  false;
 				}
 			}
 		});
 		
 		return loadingOverlayShell;
 	}
 }
 
 
