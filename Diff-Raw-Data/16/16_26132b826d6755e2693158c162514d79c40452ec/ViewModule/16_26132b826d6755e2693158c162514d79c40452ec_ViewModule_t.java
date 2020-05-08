 package edu.teco.dnd.eclipse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import edu.teco.dnd.module.Module;
 
 /**
  * ModuleView: Shows available modules, Start / Stop Server.
  * 
  * @author jung
  * 
  */
 public class ViewModule extends ViewPart implements ModuleManagerListener {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(ViewModule.class);
 
 	private Composite parent;
 	private Button button;
 	private Label serverStatus;
 	private Table moduleTable;
 
 	private Activator activator;
 	private ModuleManager manager;
 	private Map<UUID, TableItem> map = new HashMap<UUID, TableItem>();
 
 	private Display display;
 
 	public ViewModule() {
 		super();
 	}
 
 	@Override
 	public void setFocus() {
 
 	}
 
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		LOGGER.entry(site, memento);
 		super.init(site, memento);
 		activator = Activator.getDefault();
 		display = Display.getCurrent();
 		manager = activator.getModuleManager();
 		if (display == null) {
 			display = Display.getDefault();
 			LOGGER.trace("Display.getCurrent() returned null, using Display.getDefault(): {}", display);
 		}
 		manager.addModuleManagerListener(this);
 		LOGGER.exit();
 	}
 
 	@Override
 	public void dispose() {
 		manager.removeModuleManagerListener(this);
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		this.parent = parent;
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		this.parent.setLayout(layout);
 
 		createStartButton();
 		createServerInfo();
 		createModuleTable();
 	}
 
 	/**
 	 * Creates a Button that starts the Server when pressed
 	 * 
 	 * @param parent
 	 *            Composite containing the button
 	 */
 	private void createStartButton() {
 		button = new Button(parent, SWT.NONE);
 		if (activator.isRunning()) {
 			button.setText("Stop Server");
 		} else {
 			button.setText("Start Server");
 		}
 		button.setToolTipText("Start / Stop the server. duh.");
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				new Thread() {
 					@Override
 					public void run() {
 						if (ViewModule.this.activator.isRunning()) {
 							ViewModule.this.serverStatus.setText("Stopping server…");
 							ViewModule.this.activator.shutdownServer();
 						} else {
 							ViewModule.this.serverStatus.setText("Starting server…");
 							ViewModule.this.activator.startServer();
 						}
 					}
				}.start();
 			}
 		});
 
 	}
 
 	private void createServerInfo() {
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.BEGINNING;
 		gridData.horizontalAlignment = GridData.FILL;
 
 		serverStatus = new Label(parent, 0);
 		if (activator.isRunning()) {
 			serverStatus.setText("Server running");
 		} else {
 			serverStatus.setText("Server down");
 		}
 		serverStatus.setLayoutData(gridData);
 	}
 
 	/**
 	 * Creates a Table containing currently available modules.
 	 * 
 	 * @param parent
 	 *            Composite containing the table
 	 */
 	private void createModuleTable() {
 		GridData grid = new GridData();
 		grid.horizontalSpan = 2;
 		grid.verticalAlignment = GridData.FILL;
 		grid.horizontalAlignment = GridData.FILL;
 		grid.grabExcessHorizontalSpace = true;
 		grid.grabExcessVerticalSpace = true;
 
 		moduleTable = new Table(parent, 0);
 		moduleTable.setLinesVisible(true);
 		moduleTable.setHeaderVisible(true);
 		moduleTable.setLayoutData(grid);
 
 		TableColumn column1 = new TableColumn(moduleTable, SWT.None);
 		column1.setText("Module ID");
 		TableColumn column2 = new TableColumn(moduleTable, SWT.None);
 		column2.setText("Name");
 		TableColumn column3 = new TableColumn(moduleTable, SWT.None);
 		column3.setText("Location");
 		moduleTable.setToolTipText("Currently available modules");
 		/**
 		 * Collection<UUID> modules = getModules();
 		 * 
 		 * for (UUID moduleID : modules) { addID(moduleID); }
 		 **/
 		moduleTable.getColumn(0).pack();
 		moduleTable.getColumn(1).pack();
 		moduleTable.getColumn(2).pack();
 	}
 
 	/**
 	 * Adds a Module ID to the table.
 	 * 
 	 * @param id
 	 *            the ID to add
 	 */
 	private synchronized void addID(final UUID id) {
 		LOGGER.entry(id);
 		if (!map.containsKey(id)) {
 			LOGGER.trace("id {} is new, adding", id);
 			TableItem item = new TableItem(moduleTable, SWT.NONE);
 			item.setText(0, id.toString());
 			map.put(id, item);
 
 		} else {
 			LOGGER.debug("trying to add existing id {}", id);
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Removes a Module ID from the table.
 	 * 
 	 * @param id
 	 *            the ID to remove
 	 */
 	private synchronized void removeID(final UUID id) {
 		LOGGER.entry(id);
 		TableItem item = map.get(id);
 		if (item != null) {
 			LOGGER.trace("found item {} for id {}", item, id);
 			moduleTable.remove(moduleTable.indexOf(item));
 			map.remove(id);
 		} else {
 			LOGGER.debug("trying to remove nonexistant id {}", id);
 		}
 		LOGGER.exit();
 	}
 
 	@Override
 	public void moduleOnline(final UUID id) {
 		LOGGER.entry(id);
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				addID(id);
 			}
 		});
 		LOGGER.exit();
 	}
 
 	@Override
 	public void moduleOffline(final UUID id) {
 		LOGGER.entry(id);
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				removeID(id);
 			}
 		});
 		LOGGER.exit();
 	}
 
 	@Override
 	public void moduleResolved(final UUID id, final Module module) {
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 
 				if (!map.containsKey(id)) {
 					addID(id);
 				}
 
 				TableItem item = map.get(id);
 				if (module.getName() != null) {
 					item.setText(1, module.getName());
 				}
 				if (module.getLocation() != null) {
 					item.setText(2, module.getLocation());
 				}
 			}
 		});
 	}
 
 	@Override
 	public void serverOnline(final Map<UUID, Module> modules) {
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				serverStatus.setText("Server running");
 				button.setText("Stop Server");
 
 				synchronized (ViewModule.this) {
 					for (UUID id : new ArrayList<UUID>(map.keySet())) {
 						removeID(id);
 					}
 					for (UUID moduleID : modules.keySet()) {
 						addID(moduleID);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void serverOffline() {
 
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (ViewModule.this) {
 					for (UUID id : new ArrayList<UUID>(map.keySet())) {
 						removeID(id);
 					}
 				}
 				if (serverStatus != null && button != null) {
 					serverStatus.setText("Server down");
 					button.setText("Start Server");
 				}
 			}
 		});
 	}
 
 }
