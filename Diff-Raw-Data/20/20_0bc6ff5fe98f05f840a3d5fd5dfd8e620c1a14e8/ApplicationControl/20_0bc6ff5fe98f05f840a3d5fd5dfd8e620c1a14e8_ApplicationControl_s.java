 package ch.zhaw.simulation.app;
 
 import java.awt.Desktop;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.SwingUtilities;
 
 import butti.javalibs.config.ConfigPath;
 import butti.javalibs.config.Settings;
 import butti.javalibs.config.WindowPositionProperties;
 import butti.javalibs.errorhandler.Errorhandler;
 import butti.javalibs.gui.messagebox.Messagebox;
 import butti.javalibs.util.RestartUtil;
 import butti.plugin.PluginDescription;
 import ch.zhaw.simulation.control.flow.FlowEditorControl;
 import ch.zhaw.simulation.diagram.DiagramFrame;
 import ch.zhaw.simulation.diagram.persist.DiagramConfiguration;
 import ch.zhaw.simulation.dialog.aboutdlg.AboutDialog;
 import ch.zhaw.simulation.dialog.settings.SettingsDlg;
 import ch.zhaw.simulation.dialog.snapshot.SnapshotDialog;
 import ch.zhaw.simulation.editor.control.AbstractEditorControl;
 import ch.zhaw.simulation.editor.imgexport.ImageExport;
 import ch.zhaw.simulation.editor.xy.XYEditorControl;
 import ch.zhaw.simulation.filehandling.ImportPluginLoader;
 import ch.zhaw.simulation.filehandling.LoadSaveHandler;
 import ch.zhaw.simulation.math.console.MatrixConsole;
 import ch.zhaw.simulation.math.exception.SimulationModelException;
 import ch.zhaw.simulation.menu.MenuActionListener;
 import ch.zhaw.simulation.menu.RecentMenu;
 import ch.zhaw.simulation.menutoolbar.actions.MenuToolbarAction;
 import ch.zhaw.simulation.model.SimulationDocument;
 import ch.zhaw.simulation.model.SimulationType;
 import ch.zhaw.simulation.model.element.AbstractSimulationData;
 import ch.zhaw.simulation.model.flow.SimulationFlowModel;
 import ch.zhaw.simulation.plugin.ExecutionListener;
 import ch.zhaw.simulation.plugin.PluginDataProvider;
 import ch.zhaw.simulation.plugin.SimulationManager;
 import ch.zhaw.simulation.plugin.SimulationPlugin;
 import ch.zhaw.simulation.plugin.StandardParameter;
 import ch.zhaw.simulation.plugin.data.SimulationCollection;
 import ch.zhaw.simulation.plugin.data.XYDensityRaw;
 import ch.zhaw.simulation.status.StatusHandler;
 import ch.zhaw.simulation.sysintegration.Sysintegration;
 import ch.zhaw.simulation.sysintegration.SysintegrationEventlistener;
 import ch.zhaw.simulation.sysintegration.SysintegrationFactory;
 import ch.zhaw.simulation.undo.debug.UndoRedoDebugDialog;
 import ch.zhaw.simulation.window.SimulationWindow;
 import ch.zhaw.simulation.window.flow.FlowWindow;
 import ch.zhaw.simulation.window.xy.XYWindow;
 import ch.zhaw.simulation.xyviewer.ResultViewerDialog;
 import ch.zhaw.simulation.plugin.data.XYResultChooser;
 import ch.zhaw.simulation.plugin.data.XYResultList;
 
 public class ApplicationControl extends StatusHandler implements SimulationApplication, MenuActionListener, SysintegrationEventlistener {
 
 	/**
 	 * The settings
 	 */
 	private Settings settings;
 
 	/**
 	 * The import plugins
 	 */
 	private ImportPluginLoader importPluginLoader;
 
 	/**
 	 * The settings Dialog
 	 */
 	private SettingsDlg settigsDialog;
 
 	/**
 	 * Recent opened files
 	 */
 	private RecentMenu recentMenu;
 
 	/**
 	 * Load / Save handler
 	 */
 	private LoadSaveHandler savehandler;
 
 	/**
 	 * The simulation document with the data
 	 */
 	private SimulationDocument doc = new SimulationDocument();
 
 	/**
 	 * The main frame
 	 */
 	private SimulationWindow<?, ?, ?> mainFrame;
 
 	/**
 	 * The main editor controller
 	 */
 	private AbstractEditorControl<?> controller;
 
 	/**
 	 * The name of the current document
 	 */
 	private String documentName = null;
 
 	/**
 	 * This objects manages the simulation plugins
 	 */
 	private SimulationManager manager;
 
 	/**
 	 * The window position of the main window
 	 */
 	private WindowPositionProperties windowPosition = new WindowPositionProperties();
 
 	/**
 	 * The instance of the simulation integration
 	 */
 	private Sysintegration sysintegration;
 
 	/**
 	 * Automatically saves the default simulation settings
 	 */
 	private SimulationSettingsSaver simulationSettingsSaver;
 
 	/**
 	 * The submodels of the current model
 	 */
 	private HashMap<SimulationFlowModel, FlowSubmodelRef> submodels = new HashMap<SimulationFlowModel, FlowSubmodelRef>();
 
 	/**
 	 * Watches the simulation state / progress
 	 */
 	private ExecutionListener executionListener;
 
 	/**
 	 * The settings key for last used type
 	 */
 	private static final String LAST_USED_SIMTYPE = "simulation.last.type";
 
 	/**
 	 * Ctor
 	 */
 	public ApplicationControl() {
 	}
 
 	public void start(Settings settings, Vector<String> parameter, String openfile) {
 		this.settings = settings;
 
 		if (settings == null) {
 			throw new NullPointerException("settings == null");
 		}
 
 		// Load and initialize the import plugins
 		this.importPluginLoader = new ImportPluginLoader(settings);
 
 		this.recentMenu = new RecentMenu(settings);
 		this.recentMenu.addListener(this);
 
 		this.executionListener = new ExecutionListener() {
 
 			@Override
 			public void executionStarted(final String message) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						mainFrame.lock(message, new ActionListener() {
 
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								PluginDescription<SimulationPlugin> selectedPluginDescription = getSelectedPluginDescriptor();
 								if (selectedPluginDescription != null) {
 									SimulationPlugin plugin = selectedPluginDescription.getPlugin();
 									plugin.cancelSimulation();
 								}
 							}
 						});
 					}
 				});
 			}
 
 			@Override
 			public void setExecutionMessage(final String message) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						mainFrame.setLockText(message);
 					}
 				});
 			}
 
 			@Override
 			public void executionFinished(final String message, final FinishState state) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						mainFrame.unlock();
 
 						if (state == FinishState.SUCCESSFULLY) {
 							loadLastResults();
 						} else if (state == FinishState.CANCELED) {
 							Messagebox.showInfo(getMainFrame(), "Abgebrochen", "Die Simulaton wurde abgebrochen.");
 						} else if (state == FinishState.SURRENDER) {
 							// do nothing
 						} else {
 							Messagebox.showError(getMainFrame(), "Simulation fehlgeschlagen", message);
 						}
 					}
 				});
 			}
 
 			@Override
 			public void setState(int percent) {
 				mainFrame.setPercent(percent);
 			}
 
 		};
 
 		try {
 			File f = new File(ConfigPath.getSettingsPath() + "mainWindow.windowPos");
 			if (f.exists()) {
 				windowPosition.load(new FileInputStream(f));
 			}
 		} catch (Exception e) {
 			System.err.println("Laden der Windowposition des Hauptfensters fehlgeschlagen");
 			e.printStackTrace();
 		}
 
 		// Before creating SimulationManager, set document-type first
 		SimulationType type = getLastUsedSimulationType();
 		doc.setType(type);
 
 		this.manager = new SimulationManager(settings, doc.getSimulationConfiguration(), new PluginDataProvider() {
 
 			@Override
 			public JFrame getParent() {
 				return ApplicationControl.this.mainFrame;
 			}
 
 			@Override
 			public ExecutionListener getExecutionListener() {
 				return ApplicationControl.this.executionListener;
 			}
 
 			@Override
 			public SimulationType getSimulationType() {
 				return doc.getType();
 			}
 		});
 
 		loadSimulationParameterFromSettings();
 		if (type == SimulationType.FLOW_SIMULATION) {
 			showFlowWindow(true);
 		} else {
 			showXYWindow();
 		}
 
 		if (!windowPosition.applay(this.mainFrame)) {
 			this.mainFrame.setSize(800, 600);
 			this.mainFrame.setLocationRelativeTo(null);
 		}
 
 		this.sysintegration = SysintegrationFactory.getSysintegration();
 		this.sysintegration.addListener(this);
 
 		this.savehandler = new LoadSaveHandler(mainFrame, settings, sysintegration, this.importPluginLoader);
 		this.savehandler.addListener(this);
 
 		// Speichern der Einstellungen
 		simulationSettingsSaver = new SimulationSettingsSaver(doc.getSimulationConfiguration(), settings);
 
 		// Alle relevanten Settings in die Konfiguration übernehmen
 		simulationSettingsSaver.load();
 
 		if (openfile != null && open(new File(openfile)) || openLastFile()) {
 			// TODO Optimize, do not show a window and close it and open then
 		}
 
 		if (parameter.contains("--debug-undo")) {
 			UndoRedoDebugDialog dlg = new UndoRedoDebugDialog(this.mainFrame.getView().getControl().getUndoManager());
 			dlg.setVisible(true);
 		}
 	}
 
 	public void showXYWindow() {
 		XYWindow win = new XYWindow();
 		XYEditorControl control = new XYEditorControl(this, doc, doc.getXyModel(), win, settings);
 		this.controller = control;
 		this.addListener(control);
 		win.init(control);
 		win.addListener(control);
 
 		this.mainFrame = win;
 
 		win.setVisible(true);
 	}
 
 	public void showFlowWindow(boolean mainWindow) {
 		FlowWindow win = new FlowWindow(mainWindow);
 		FlowEditorControl control = new FlowEditorControl(this, doc.getFlowModel(), doc, win, settings);
 		this.controller = control;
 		this.addListener(control);
 		win.init(control);
 		win.addListener(control);
 
 		this.mainFrame = win;
 		mainFrame.setVisible(true);
 	}
 
 	public void takeSnapshot() {
 		String name = "Unbennant";
 		if (savehandler.getPath() != null) {
 			name = savehandler.getPath().getName();
 
 			int pos = name.lastIndexOf('.');
 			if (pos > 0) {
 				// remove .xxx
 				name = name.substring(0, pos);
 			}
 		}
 
 		ImageExport expor = new ImageExport(getController());
 		SnapshotDialog dlg = new SnapshotDialog(this.mainFrame, this.settings, getController().getSysintegration(), expor, name);
 		dlg.setVisible(true);
 	}
 
 	private void loadSimulationParameterFromSettings() {
 		int prefixLen = StandardParameter.SIM_PROPERTY_STRING_PREFIX.length();
 		for (String k : settings.getKeysStartingWith(StandardParameter.SIM_PROPERTY_STRING_PREFIX)) {
 			doc.getSimulationConfiguration().setParameter(k.substring(prefixLen), settings.getSetting(k));
 		}
 
 		prefixLen = StandardParameter.SIM_PROPERTY_DOUBLE_PREFIX.length();
 		for (String k : settings.getKeysStartingWith(StandardParameter.SIM_PROPERTY_DOUBLE_PREFIX)) {
 			try {
 				double d = Double.parseDouble(settings.getSetting(k));
 				doc.getSimulationConfiguration().setParameter(k.substring(prefixLen), d);
 			} catch (Exception e) {
 				System.err.println("Invalid double setting: \"" + k + "\" = \"" + settings.getSetting(k) + "\"");
 			}
 		}
 	}
 
 	public void releaseOpenWindow() {
 		windowPosition.readWindowPos(this.mainFrame);
 		File f = new File(ConfigPath.getSettingsPath() + "mainWindow.windowPos");
 		try {
 			windowPosition.store(new FileOutputStream(f), "Fensterposition des Fenstertypes \"Hauptfensters\"");
 		} catch (Exception e) {
 			System.err.println("Speichern der Windowposition des Hauptfensters fehlgeschlagen");
 			e.printStackTrace();
 		}
 
 		if (this.mainFrame != null) {
 			this.mainFrame.dispose();
 			this.mainFrame.removeListener(this.controller);
 		}
 		if (this.controller != null) {
 			this.controller.dispose();
 			this.removeListener(this.controller);
 		}
 
 		for (FlowSubmodelRef flow : this.submodels.values()) {
 			flow.dispose();
 		}
 
 		this.submodels.clear();
 	}
 
 	@Override
 	public SimulationManager getManager() {
 		return manager;
 	}
 
 	public Settings getSettings() {
 		return settings;
 	}
 
 	public AbstractEditorControl<?> getController() {
 		return controller;
 	}
 
 	/**
 	 * Shows the about dialog
 	 */
 	public void showAboutDialog() {
 		AboutDialog dlg = new AboutDialog(mainFrame);
 		dlg.setVisible(true);
 	}
 
 	/**
 	 * Shows the settings dialog
 	 */
 	public void showSettingsDialog() {
 		if (settigsDialog == null) {
 			settigsDialog = new SettingsDlg(this);
 		}
 		settigsDialog.setVisible(true);
 	}
 
 	/**
 	 * Shows the help dialog
 	 */
 	public void showHelpDialog() {
 		try {
 			File f = new File("help/index.html");
 			Desktop.getDesktop().browse(f.toURI());
 		} catch (Exception e) {
 			e.printStackTrace();
 			Messagebox.showInfo(getMainFrame(), "Hilfe öffnen fehlgeschlagen", "Bitte öffnen Sie help/index.html manuell im Browser");
 		}
 	}
 
 	/**
 	 * Shows the math console
 	 */
 	public void showMathConsole() {
 		new MatrixConsole().setVisible(true);
 	}
 
 	public ImportPluginLoader getImportPluginLoader() {
 		return importPluginLoader;
 	}
 
 	@Override
 	public JFrame getMainFrame() {
 		return mainFrame;
 	}
 
 	public PluginDescription<SimulationPlugin> getSelectedPluginDescriptor() {
 		String pluginName = doc.getSimulationConfiguration().getSelectedPluginName();
 
 		if (pluginName == null) {
 			Messagebox.showError(getMainFrame(), "Kein Plugin gewählt", "Bitte wählen Sie in der Sidebar mit welchem Plugin simuliert werden soll");
 			return null;
 		}
 
 		PluginDescription<SimulationPlugin> selectedPluginDescription = null;
 		for (PluginDescription<SimulationPlugin> pluginDescription : manager.getPluginDescriptions()) {
 			if (pluginName.equalsIgnoreCase(pluginDescription.getName())) {
 				selectedPluginDescription = pluginDescription;
 				break;
 			}
 		}
 
 		return selectedPluginDescription;
 	}
 
 	public void startSimulation() {
 		PluginDescription<SimulationPlugin> selectedPluginDescription = getSelectedPluginDescriptor();
 
 		if (selectedPluginDescription == null) {
 			Messagebox.showError(getMainFrame(), "Plugin nicht gefunden", "Bitte wählen Sie in der Sidebar mit welchem Plugin simuliert werden soll");
 			return;
 		}
 
 		SimulationPlugin plugin = selectedPluginDescription.getPlugin();
 
 		try {
 			plugin.checkDocument(doc);
 		} catch (SimulationModelException ex) {
 			Messagebox.showError(getMainFrame(), "Simulation nicht möglich", ex.getMessage());
 
 			Object obj = ex.getSimObject();
 			if (obj instanceof AbstractSimulationData) {
 				controller.getView().selectElement((AbstractSimulationData) obj);
 			}
 
 			ex.printStackTrace();
 			return;
 		} catch (Exception e) {
 			Errorhandler.showError(e, "Simulation fehlgeschlagen");
 			return;
 		}
 
 		try {
 			plugin.executeSimulation(doc);
 		} catch (Exception e) {
 			Errorhandler.showError(e, "Simulation fehlgeschlagen");
 		}
 	}
 
 	public void loadLastResults() {
 		if (doc.getType() == SimulationType.FLOW_SIMULATION) {
 			DiagramConfiguration config = new DiagramConfiguration();
 
 			SimulationCollection collection = getSelectedPluginDescriptor().getPlugin().getSimulationResults(doc);
 			DiagramFrame frame = new DiagramFrame(collection, ApplicationControl.this.settings, config, getDocumentName(), sysintegration);
 			frame.setVisible(true);
 
 		} else if (doc.getType() == SimulationType.XY_MODEL) {
 			SimulationCollection collection = getSelectedPluginDescriptor().getPlugin().getSimulationResults(doc);
 			Vector<XYDensityRaw> rawList = getSelectedPluginDescriptor().getPlugin().getXYResults(doc);
 			XYResultChooser chooser = new XYResultChooser(doc.getXyModel(), collection);
 			XYResultList xyResultList = chooser.chooseMesoPart();
 			ResultViewerDialog dlg = new ResultViewerDialog(getMainFrame(), xyResultList, rawList);
 			dlg.setVisible(true);
 		}
 	}
 
 	@Override
 	public void newFile(SimulationType type) {
 		if (askSave() == true) {
 			this.settings.setSetting(LAST_USED_SIMTYPE, type.toString());
 
 			releaseOpenWindow();
 			doc.clear();
 			doc.setType(type);
 			loadSimulationParameterFromSettings();
 
 			createMainWindow();
 
 			setDocumentTitle(null);
 
 			setStatusText("Neues Dokument erstellt");
 			savehandler.clear();
 
 			simulationSettingsSaver.load();
 			doc.setSaved();
 
 			this.controller.getUndoManager().discardAllEdits();
 		}
 	}
 
 	public SimulationType getLastUsedSimulationType() {
 		String type = this.settings.getSetting(LAST_USED_SIMTYPE, SimulationType.FLOW_SIMULATION.toString());
 
 		SimulationType t = SimulationType.valueOf(type);
 		if (t == null) {
 			t = SimulationType.FLOW_SIMULATION;
 		}
 		return t;
 	}
 
 	private void createMainWindow() {
 		if (doc.getType() == SimulationType.FLOW_SIMULATION) {
 			showFlowWindow(true);
 		} else if (doc.getType() == SimulationType.XY_MODEL) {
 			showXYWindow();
 		} else {
 			throw new RuntimeException("Simulation type " + doc.getType() + " unhandled");
 		}
 
 		windowPosition.applay(this.mainFrame);
 	}
 
 	@Override
 	public boolean save() {
 		if (!savehandler.save(doc)) {
 			return false;
 		}
 
 		updatePaths();
 		return true;
 	}
 
 	private boolean open(File file) {
 		if (!file.canRead()) {
 			Messagebox msg = new Messagebox(getMainFrame(), "Fehler beim Öffnen", "Die Datei \"" + file.getAbsolutePath()
 					+ "\" kann nicht gelesen werden, keine Rechte!", Messagebox.ERROR);
 			msg.addButton("andere Datei öffnen", 0);
 			msg.addButton("Abbrechen", 1, true);
 			if (msg.display() == 0) {
 				return open();
 			}
 			return false;
 		}
 
 		if (askSave() == true) {
 			releaseOpenWindow();
 			doc.clear();
 			if (savehandler.open(file, doc)) {
 				updatePaths();
 
 				createMainWindow();
 				updateTitle();
 				return true;
 			} else {
 				newFile(getLastUsedSimulationType());
 			}
 
 			updateTitle();
 		}
 
 		return false;
 	}
 
 	private boolean saveAs() {
 		if (!savehandler.saveAs(doc)) {
 			return false;
 		}
 		updatePaths();
 
 		return true;
 
 	}
 
 	private void setLookAndFeel(String lookAndFeel) {
 		settings.setSetting("ui.look-and-feel", lookAndFeel);
 
 		if (this.exit()) {
 			if (!RestartUtil.restartApplication("startup.Startup")) {
 				Messagebox msg = new Messagebox(null, "Neu Starten", "<html>Das Programm konnte nicht neu gstartet werden.<br>"
 						+ "Es wird jetzt beendet, bitte starten Sie es manuell neu.</html>", Messagebox.ERROR);
 				msg.addButton("OK", 0);
 				msg.display();
 			}
 		}
 
 	}
 
 	private boolean open() {
 		if (askSave() == true) {
 			File f = sysintegration.showOpenDialog(getMainFrame(), importPluginLoader.getSimulationFileOpen(), savehandler.getLastSavePath());
 
 			if (f != null) {
 				if (!f.isFile()) {
 					Messagebox.showError(getMainFrame(), "Öffnen fehlgeschlagen", "Dies ist keine Datei!");
 					return false;
 				} else {
 					return open(f);
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public boolean exit() {
 		if (askSave() == true) {
 			releaseOpenWindow();
 
 			doc.getSimulationConfiguration().removePluginChangeListener(simulationSettingsSaver);
 			doc.getSimulationConfiguration().removeSimulationParameterListener(simulationSettingsSaver);
 
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					Window[] windows = Window.getWindows();
 					if (windows.length > 0) {
 						System.err.println("Open windows (disposing now):");
 						for (Window frame : windows) {
 							System.err.println(frame.getName() + ": " + frame.getClass());
 							frame.dispose();
 						}
 					}
 				}
 			});
 
 			return true;
 		}
 
 		return false;
 	}
 
 	protected boolean askSave() {
 		if (doc.isChanged()) {
 			Messagebox msg = new Messagebox(this.mainFrame, "Aktuelles Dokument", "Soll das aktuelle Dokument verworfen werden?", Messagebox.QUESTION);
 			msg.addButton("Abbrechen", 0);
 			msg.addButton("Verwerfen", 1);
 			msg.addButton("Speichern", 2, true);
 
 			int result = msg.display();
 			if (result == 1) {
 				return true;
 			}
 			if (result == 2) {
 				return save();
 			}
 
 			return false;
 		}
 		return true;
 	}
 
 	public boolean openLastFile() {
 		if (settings.isSetting("autoloadLastDocument", false)) {
 			String path = recentMenu.getNewstEntry();
 			if (path != null) {
 				File f = new File(path);
 				if (f.exists() && f.canRead()) {
 					return open(new File(path));
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void menuActionPerformed(MenuToolbarAction action) {
 
 		switch (action.getType()) {
 		case NEW_FILE:
 			if (action.getData() != null) {
 				this.newFile((SimulationType) action.getData());
 			} else {
 				this.newFile(getLastUsedSimulationType());
 			}
 			break;
 
 		case SAVE_AS:
 			this.saveAs();
 			break;
 
 		case OPEN_FILE:
 			if (action.getData() != null) {
 				this.open(new File(action.getData().toString()));
 			} else {
 				this.open();
 			}
 			break;
 
 		case SNAPSHOT:
 			takeSnapshot();
 			break;
 
 		case ABOUT:
 			this.showAboutDialog();
 			break;
 
 		case SHOW_MATH_CONSOLE:
 			this.showMathConsole();
 			break;
 
 		case SETTINGS:
 			this.showSettingsDialog();
 			break;
 
 		case HELP:
 			this.showHelpDialog();
 			break;
 
 		case EXIT:
 			this.exit();
 			break;
 
 		case LOOK_AND_FEEL_CHANGED:
 			setLookAndFeel(action.getData().toString());
 			break;
 
 		case START_SIMULATION:
 			startSimulation();
 			break;
 
 		case LOAD_RESULTS:
 			loadLastResults();
 
 		default:
 			System.err.println("Unhandled event (ApplicationControl): " + action.getType() + " / " + action.getData());
 
 		}
 	}
 
 	@Override
 	public JMenu getRecentMenu() {
 		return recentMenu.getMenu();
 	}
 
 	public void updatePaths() {
 		if (savehandler.getPath() != null) {
 			recentMenu.addFile(savehandler.getPath().getAbsolutePath());
 			setDocumentTitle(savehandler.getPath().getName());
 		}
 	}
 
 	public void setDocumentTitle(String name) {
 		documentName = name;
 		updateTitle();
 	}
 
 	public Sysintegration getSysintegration() {
 		return sysintegration;
 	}
 
 	@Override
 	public void updateTitle() {
 		if (documentName == null) {
 			this.mainFrame.setTitle("(AB)² Simulation");
 		} else {
 			String saved = "";
 			if (doc.isChanged()) {
 				saved = " *";
 			}
 			this.mainFrame.setTitle(documentName + saved + " - (AB)² Simulation");
 		}
 	}
 
 	public String getDocumentName() {
 		return documentName;
 	}
 
 	@Override
 	public void sysEvent(EventType type, String param) {
 		switch (type) {
 		case EXIT:
 			exit();
 			break;
 
 		case OPEN:
 			open(new File(param));
 			break;
 
 		case PREFERENCES:
 			showSettingsDialog();
 			break;
 
 		case ABOUT:
 			showAboutDialog();
 			break;
 		}
 	}
 
 	@Override
 	public void openFlowEditor(SimulationFlowModel model) {
 		FlowSubmodelRef ref = submodels.get(model);
 		if (ref == null) {
 			ref = new FlowSubmodelRef(this, this.doc, settings, model);
 			submodels.put(model, ref);
 		}
 
 		ref.getWin().setVisible(true);
 	}
 }
