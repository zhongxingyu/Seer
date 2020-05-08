 package edu.teco.dnd.eclipse.appView;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import edu.teco.dnd.discover.ApplicationInformation;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.module.messages.killApp.KillAppMessage;
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.network.messages.Response;
 import edu.teco.dnd.server.ApplicationManager;
 import edu.teco.dnd.server.ApplicationManagerListener;
 import edu.teco.dnd.server.ModuleManager;
 import edu.teco.dnd.server.ServerManager;
 import edu.teco.dnd.util.FutureListener;
 import edu.teco.dnd.util.FutureNotifier;
 import edu.teco.dnd.util.JoinedFutureNotifier;
 
 /**
  * View for the applications / running function blocks.
  * 
  * @author jung
  * 
  */
 public class AppView extends ViewPart implements ApplicationManagerListener,
 		FutureListener<FutureNotifier<Collection<Response>>> {
 
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(AppView.class);
 
 	public static final String SORT_APP = "Sort by Apps";
 
 	public static final String SORT_MOD = "Sort by Modules";
 
 	/**
 	 * Indicates that the applications are currently sorted by themselves.
 	 */
 	public static final int SORTED_BY_APPS = 0;
 
 	/**
 	 * Indicates that the applications are currently sorted by modules.
 	 */
 	public static final int SORTED_BY_MODULES = 1;
 
 	/**
 	 * Index of the Application column in the appTable.
 	 */
 	public static final int APP_INDEX = 0;
 
 	/**
 	 * Index of the UUID column in the appTable.
 	 */
 	public static final int UUID_INDEX = 1;
 
 	/**
 	 * Index of the Block Name column in the blockTable.
 	 */
 	public static final int BLOCK_INDEX = 0;
 
 	/**
 	 * Index of the Block Type column in the blockTable.
 	 */
 	public static final int TYPE_INDEX = 1;
 
 	/**
 	 * Index of the column for information on the block, either an application or a module, in the blockTable.
 	 */
 	public static final int BLOCK_INFO_INDEX = 2;
 
 	private Label selectedLabel;
 	private Label selectedInfoLabel;
 	private Button updateButton;
 	private Button killAppButton;
 	private Button sortButton; // Sort by App or Module
 	private Table appTable;
 	private Table blockTable;
 
 	private Display display;
 	private ApplicationManager appManager;
 	private AppViewGraphics graphicsManager;
 
 	private Map<TableItem, UUID> itemToUUID;
 	private Map<UUID, TableItem> uuidToItem;
 	private Map<TableItem, ApplicationInformation> blockTableItemToApp;
 	private Map<TableItem, UUID> blockTableItemToModule;
 	private Module selectedModule;
 	private ApplicationInformation selectedApp;
 	private int sorted;
 
 	// Used to map FunctionBlocks on table items
 	// private Map<UUID, TableItem> map = new HashMap<UUID, TableItem>();
 
 	/**
 	 * Creates a new Appview.
 	 */
 	public AppView() {
 		super();
 	}
 
 	public void setFocus() {
 	}
 
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		LOGGER.entry(site, memento);
 		super.init(site, memento);
 		display = Display.getCurrent();
 		if (display == null) {
 			display = Display.getDefault();
 			LOGGER.trace("Display.getCurrent() returned null, using Display.getDefault(): {}", display);
 		}
 		appManager = ServerManager.getDefault().getApplicationManager();
 		appManager.addApplicationListener(this);
 
 		itemToUUID = new HashMap<TableItem, UUID>();
 		uuidToItem = new HashMap<UUID, TableItem>();
 		blockTableItemToApp = new HashMap<TableItem, ApplicationInformation>();
 		blockTableItemToModule = new HashMap<TableItem, UUID>();
 		sorted = SORTED_BY_APPS;
 		LOGGER.exit();
 	}
 
 	@Override
 	public void dispose() {
 		appManager.removeApplicationListener(this);
 	}
 
 	public void createPartControl(Composite parent) {
 		graphicsManager = new AppViewGraphics();
 
 		graphicsManager.initParent(parent);
 
 		updateButton = graphicsManager.createUpdateAppsButton();
 		killAppButton = graphicsManager.createKillAppButton();
 		sortButton = graphicsManager.createSortButton();
 		selectedLabel = graphicsManager.createSelectedLabel();
 		selectedInfoLabel = graphicsManager.createSelectedInfoLabel();
 		appTable = graphicsManager.createAppTable();
 		blockTable = graphicsManager.createBlockTable();
 
 		createListeners();
 	}
 
 	private void createListeners() {
 		updateButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AppView.this.updateAppList();
 			}
 		});
 		killAppButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AppView.this.killApp();
 			}
 		});
 		sortButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AppView.this.sort(true);
 			}
 		});
 		appTable.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (sorted == SORTED_BY_APPS) {
 					AppView.this.appSelected();
 				} else if (sorted == SORTED_BY_MODULES) {
 					AppView.this.moduleSelected();
 				} else {
 					warn("Something went wrong with sorting the applications. Please try to restart the view.");
 				}
 			}
 		});
 		blockTable.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (sorted == SORTED_BY_MODULES) {
 					AppView.this.blocktableAppSelected();
 				} else if (sorted == SORTED_BY_APPS) {
 					AppView.this.blocktableModuleSelected();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Updates Information on which applications are running on a module.
 	 */
 	private synchronized void updateAppList() {
 		appManager.updateAppInfo();
 	}
 
 	/**
 	 * Kills an Application.
 	 */
 	private void killApp() {
 		if (selectedApp != null) {
 			Collection<UUID> moduleIDs = selectedApp.getModules();
 			ConnectionManager connectionManager = ServerManager.getDefault().getConnectionManager();
 			final Collection<FutureNotifier<? extends Response>> futures =
 					new ArrayList<FutureNotifier<? extends Response>>();
 
 			for (final UUID module : moduleIDs) {
 				final KillAppMessage killAppMsg = new KillAppMessage(selectedApp.getAppId());
 				futures.add(connectionManager.sendMessage(module, killAppMsg));
 			}
 			JoinedFutureNotifier<Response> joined = new JoinedFutureNotifier<Response>(futures);
 			joined.addListener(this);
 			if (sorted == SORTED_BY_APPS) {
 				appTable.remove(appTable.indexOf(removeUUIDAndItem(selectedApp.getAppId())));
 				blockTable.removeAll();
 			} else if (sorted == SORTED_BY_MODULES) {
 				for (TableItem item : blockTable.getItems()) {
 					if (selectedApp.getAppId().equals(blockTableItemToApp.get(item).getAppId())) {
 						blockTable.remove(blockTable.indexOf(item));
 					}
 				}
 			}
 			selectedApp = null;
 		}
 	}
 
 	/**
 	 * Sorts the tables by applications or modules
 	 * 
 	 * @param toggleSortMode
 	 *            Indicates whether to toggle the sort mode (from modules to applications / applications to modules) or
 	 *            re-sort according to the current mode.
 	 */
 	private void sort(boolean toggleSortMode) {
 		clearTables();
 		killAppButton.setEnabled(false);
 		selectedInfoLabel.setText("<select on the left>");
 
 		if (toggleSortMode) {
 			if (sorted == SORTED_BY_APPS) {
 				sorted = SORTED_BY_MODULES;
 			} else if (sorted == SORTED_BY_MODULES) {
 				sorted = SORTED_BY_APPS;
 			}
 			updateAppList();
 		} else {
 			if (sorted == SORTED_BY_APPS) {
 				sortByApps();
 			}
 			if (sorted == SORTED_BY_MODULES) {
 				sortByModules();
 			}
 		}
 	}
 
 	private synchronized void sortByModules() {
 		selectedApp = null;
 		selectedLabel.setText("Module:");
 		appTable.getColumn(APP_INDEX).setText("Modules:");
 		blockTable.getColumn(BLOCK_INFO_INDEX).setText("Application:");
 		sortButton.setText(SORT_APP);
 		sorted = SORTED_BY_MODULES;
 
 		blockTableItemToApp.clear();
 		Map<UUID, Collection<ApplicationInformation>> modToApp = appManager.getModulesToApps();
 		Map<UUID, Module> idToMod = ServerManager.getDefault().getModuleManager().getMap();
 		for (UUID id : modToApp.keySet()) {
 			TableItem item = new TableItem(appTable, SWT.NONE);
 			if (idToMod.containsKey(id)) {
 				item.setText(APP_INDEX, idToMod.get(id).getName());
 			}
 			item.setText(UUID_INDEX, id.toString());
 			addUUIDAndItem(id, item);
 		}
 
 		if (selectedModule != null) {
			System.out.println("Selected Module nciht null");
 			appTable.setSelection(uuidToItem.get(selectedModule.getUUID()));
 			moduleSelected();
 		}
 	}
 
 	private synchronized void sortByApps() {
 		selectedModule = null;
 		selectedLabel.setText("Application:");
 		appTable.getColumn(APP_INDEX).setText("Applications:");
 		blockTable.getColumn(BLOCK_INFO_INDEX).setText("On Module:");
 		sortButton.setText(SORT_MOD);
 		sorted = SORTED_BY_APPS;
 
 		for (ApplicationInformation app : appManager.getApps()) {
 			TableItem item = new TableItem(appTable, SWT.NONE);
 			item.setText(APP_INDEX, app.getName());
 			item.setText(UUID_INDEX, app.getAppId().toString());
 			addUUIDAndItem(app.getAppId(), item);
 		}
 		if (selectedApp != null) {
 			System.out.println("Selected App nicht null");
 			appTable.setSelection(uuidToItem.get(selectedApp.getAppId()));
 			appSelected();
 		}
 	}
 
 	/**
 	 * To be invoked whenever an application of the left table was selected AND the tables are sorted by applications.
 	 * Check before calling whether the tables are currently sorted by applications or modules.
 	 */
 	private void appSelected() {
 		if (sorted != SORTED_BY_APPS) {
 			warn("Something went terribly wrong. Please restart the Application View or retry sorting.");
 			return;
 		}
 		blockTable.removeAll();
 		selectedModule = null;
 
 		TableItem[] items = appTable.getSelection();
 		if (items.length == 1) {
 			TableItem selectedItem = items[0];
 			selectedApp = appManager.getMap().get(itemToUUID.get(selectedItem));
 			selectedInfoLabel.setText(selectedApp.getName() + " : " + selectedApp.getAppId().toString());
 			ModuleManager m = ServerManager.getDefault().getModuleManager();
 			Map<UUID, Module> uuidToModule = m.getMap();
 
 			Map<UUID, Collection<UUID>> modToBlocks = selectedApp.getBlocksRunningOn();
 
 			for (UUID moduleID : modToBlocks.keySet()) {
 				String moduleText = moduleID.toString();
 
 				if (uuidToModule.containsKey(moduleID)) {
 					moduleText = uuidToModule.get(moduleID).getName().concat(" : " + moduleText);
 				}
 				for (UUID blockUUID : modToBlocks.get(moduleID)) {
 					TableItem item = new TableItem(blockTable, SWT.NONE);
 					item.setText(
 							BLOCK_INDEX,
 							selectedApp.getBlockName(blockUUID) == null ? blockUUID.toString() : selectedApp
 									.getBlockName(blockUUID));
 					item.setText(TYPE_INDEX,
 							selectedApp.getBlockType(blockUUID) == null ? "" : selectedApp.getBlockType(blockUUID));
 					item.setText(BLOCK_INFO_INDEX, moduleText);
 					blockTableItemToModule.put(item, moduleID);
 				}
 			}
 			killAppButton.setEnabled(true);
 		}
 	}
 
 	/**
 	 * To be invoked whenever a module of the left table was selected AND the tables are sorted by modules. Check before
 	 * calling whether the tables are currently sorted by applications or modules.
 	 */
 	private void moduleSelected() {
 		if (sorted != SORTED_BY_MODULES) {
 			warn("Something went terribly wrong. Please restart the Application View or retry sorting.");
 			return;
 		}
 		blockTable.removeAll();
 		blockTableItemToApp.clear();
 		killAppButton.setEnabled(false);
 		selectedApp = null;
 
 		TableItem[] items = appTable.getSelection();
 		if (items.length == 1) {
 			TableItem selectedItem = items[0];
 			UUID moduleUUID = itemToUUID.get(selectedItem);
 			selectedModule = ServerManager.getDefault().getModuleManager().getMap().get(moduleUUID);
 			if (selectedModule == null) {
 				warn("The module hasn't been resolved yet.");
 				return;
 			}
 			selectedLabel.setText("Module:");
 			selectedInfoLabel.setText(selectedModule.getName());
 
 			Map<UUID, Collection<ApplicationInformation>> moduleToApps = appManager.getModulesToApps();
 			Collection<ApplicationInformation> apps = moduleToApps.get(moduleUUID);
 			for (ApplicationInformation app : apps) {
 				String appText = app.getName();
 				appText = appText.concat(" : " + app.getAppId());
 				for (UUID block : app.getBlocksRunningOn().get(moduleUUID)) {
 					TableItem item = new TableItem(blockTable, SWT.NONE);
 					item.setText(BLOCK_INDEX,
 							app.getBlockName(block) == null ? block.toString() : app.getBlockName(block));
 					item.setText(TYPE_INDEX,
 							app.getBlockType(block) == null ? "" : app.getBlockType(block));
 					item.setText(BLOCK_INFO_INDEX, appText);
 					blockTableItemToApp.put(item, app);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * To be invoked whenever the tables are sorted by modules and an application is selected in the blockTable.
 	 */
 	private void blocktableAppSelected() {
 		TableItem[] items = blockTable.getSelection();
 		if (items.length == 1) {
 			selectedApp = blockTableItemToApp.get(items[0]);
 			selectedLabel.setText("Application selected:");
 			selectedInfoLabel.setText(selectedApp.getName() + " : " + selectedApp.getAppId().toString());
 		}
 		killAppButton.setEnabled(true);
 	}
 
 	private void blocktableModuleSelected() {
 		TableItem[] items = blockTable.getSelection();
 		if (items.length == 1) {
 			Map<UUID, Module> map = ServerManager.getDefault().getModuleManager().getMap();
 			if (map.containsKey(blockTableItemToModule.get(items[0]))) {
 				selectedModule = map.get(blockTableItemToModule.get(items[0]));
 			}
 		}
 	}
 
 	@Override
 	public void serverOnline() {
 		LOGGER.entry();
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (updateButton != null) {
 					updateButton.setEnabled(true);
 					sortButton.setEnabled(true);
 					updateAppList();
 				}
 			}
 		});
 		LOGGER.exit();
 	}
 
 	@Override
 	public void serverOffline() {
 		LOGGER.entry();
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				killAppButton.setEnabled(false);
 				updateButton.setEnabled(false);
 				sortButton.setEnabled(false);
 				selectedApp = null;
 				selectedModule = null;
 				clearTables();
 			}
 		});
 		LOGGER.exit();
 	}
 
 	@Override
 	public void applicationsResolved(final Collection<ApplicationInformation> apps) {
 		LOGGER.entry();
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				sort(false);
 			}
 		});
 		LOGGER.exit();
 	}
 
 	/**
 	 * Adds a UUID and a TableItem to the maps to connect them.
 	 * 
 	 * The UUID can be used both for ApplicationInformations and Modules. Be careful to use only one of them
 	 * simultaneously throughout the maps and clear the maps whenever you wish to switch from ApplicationInformations to
 	 * Modules or otherwise. The current status is indicated by the sorted field: whenever it is set to SORTED_BY_APPS,
 	 * the maps should contain information on TableItems and ApplicationInformations. Whenever sorted is set to
 	 * SORTED_BY_MODULES, the maps should contain information on TableItems and Modules.
 	 * 
 	 * @param id
 	 *            UUID of the Application or Module to connect to a TableItem
 	 * @param item
 	 *            the TableItem to connect to the UUID of an Application or module
 	 */
 	private void addUUIDAndItem(UUID id, TableItem item) {
 		uuidToItem.put(id, item);
 		itemToUUID.put(item, id);
 	}
 
 	/**
 	 * Removes a UUID and the corresponding TableItem from the Maps linking the two together.
 	 * 
 	 * The UUID can be used both for ApplicationInformations and Modules. Be careful to use only one of them
 	 * simultaneously throughout the maps and clear the maps whenever you wish to switch from ApplicationInformations to
 	 * Modules or otherwise. The current status is indicated by the sorted field: whenever it is set to SORTED_BY_APPS,
 	 * the maps should contain information on TableItems and ApplicationInformations. Whenever sorted is set to
 	 * SORTED_BY_MODULES, the maps should contain information on TableItems and Modules.
 	 * 
 	 * @param id
 	 *            UUID of the application / module to remove from the connecting maps.
 	 * @return TableItem previously linked to the application / module.
 	 */
 	private TableItem removeUUIDAndItem(UUID id) {
 		TableItem item = uuidToItem.get(id);
 		itemToUUID.remove(item);
 		uuidToItem.remove(id);
 		return item;
 	}
 
 	/**
 	 * Clears all tables and maps containing information on items of the tables.
 	 */
 	private void clearTables() {
 		blockTableItemToApp.clear();
 		blockTableItemToModule.clear();
 		uuidToItem.clear();
 		itemToUUID.clear();
 		appTable.removeAll();
 		blockTable.removeAll();
 	}
 
 	/**
 	 * Opens a warning window displaying the given message.
 	 * 
 	 * @param message
 	 *            Warning message
 	 * @return int representing the choice of the user.
 	 */
 	private int warn(String message) {
 		Display display = Display.getCurrent();
 		Shell shell = new Shell(display);
 		MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
 		dialog.setText("Warning");
 		dialog.setMessage(message);
 		return dialog.open();
 	}
 
 	/**
 	 * Gets called whenever an application was killed.
 	 */
 
 	@Override
 	public void operationComplete(FutureNotifier<Collection<Response>> future) throws Exception {
 		if (!future.isSuccess()) {
 			warn("Something went wrong while cancelling the application.");
 		}
 		System.out.println("App killed.");
 		updateAppList();
 
 	}
 }
