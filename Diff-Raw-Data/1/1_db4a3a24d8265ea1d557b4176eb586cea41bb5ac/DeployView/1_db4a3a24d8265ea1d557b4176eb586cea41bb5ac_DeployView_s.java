 package edu.teco.dnd.eclipse.deployView;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import org.apache.logging.log4j.Level;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 
 import edu.teco.dnd.deploy.Constraint;
 import edu.teco.dnd.deploy.Deploy;
 import edu.teco.dnd.deploy.Distribution;
 import edu.teco.dnd.deploy.Distribution.BlockTarget;
 import edu.teco.dnd.deploy.UserConstraints;
 import edu.teco.dnd.eclipse.Activator;
 import edu.teco.dnd.eclipse.EclipseUtil;
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.server.DistributionCreator;
 import edu.teco.dnd.server.ModuleManager;
 import edu.teco.dnd.server.ModuleManagerListener;
 import edu.teco.dnd.server.NoBlocksException;
 import edu.teco.dnd.server.NoModulesException;
 import edu.teco.dnd.server.ServerManager;
 import edu.teco.dnd.util.Dependencies;
 import edu.teco.dnd.util.FutureListener;
 import edu.teco.dnd.util.FutureNotifier;
 import edu.teco.dnd.util.JoinedFutureNotifier;
 import edu.teco.dnd.util.StringUtil;
 
 /**
  * This class gives the user access to all functionality needed to deploy an application. The user can load an existing
  * data flow graph, rename its function blocks and constrain them to specific modules and / or places. The user can also
  * create a distribution and deploy the function blocks on the modules.
  * 
  */
 public class DeployView extends EditorPart implements ModuleManagerListener,
 		FutureListener<JoinedFutureNotifier<Module>> {
 
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(DeployView.class);
 
 	/**
 	 * Key code for the 'enter' key.
 	 */
 	private static final int ENTER = 13;
 	private Display display;
 	private Activator activator;
 	private ServerManager serverManager;
 	private ModuleManager manager;
 
 	private List<String> infoTexts;
 
 	private ArrayList<UUID> idList = new ArrayList<UUID>();
 
 	private Collection<FunctionBlockModel> functionBlocks;
 	private Map<TableItem, FunctionBlockModel> mapItemToBlockModel;
 
 	private Map<FunctionBlockModel, BlockTarget> mapBlockToTarget;
 
 	private Resource resource;
 	private DeployViewGraphics graphicsManager;
 
 	private boolean newConstraints;
 
 	private Button serverButton;
 	private Button updateModulesButton; // Button to update moduleCombo
 	private Button updateBlocksButton;
 	private Button createButton; // Button to create deployment
 	private Button deployButton; // Button to deploy deployment
 	private Button constraintsButton;
 	private Label appName;
 	private Text blockModelName; // Name of BlockModel
 	private Combo moduleCombo;
 	private Text places;
 	private StyledText infoText;
 	private Table deployment; // Table to show blockModels and current
 								// deployment
 
 	private int selectedIndex; // Index of selected field of moduleCombo
 								// = index in idList + 1
 	private UUID selectedID;
 	private TableItem selectedItem;
 	private FunctionBlockModel selectedBlockModel;
 	private Map<FunctionBlockModel, UUID> moduleConstraints = new HashMap<FunctionBlockModel, UUID>();
 	private Map<FunctionBlockModel, String> placeConstraints = new HashMap<FunctionBlockModel, String>();
 
 	private URL[] classPath = new URL[0];
 
 	@Override
 	public void setFocus() {
 
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		LOGGER.entry(site, input);
 		setSite(site);
 		setInput(input);
 		activator = Activator.getDefault();
 		serverManager = ServerManager.getDefault();
 		display = Display.getCurrent();
 		manager = serverManager.getModuleManager();
 		if (display == null) {
 			display = Display.getDefault();
 			LOGGER.trace("Display.getCurrent() returned null, using Display.getDefault(): {}", display);
 		}
 		manager.addModuleManagerListener(this);
 		mapBlockToTarget = new HashMap<FunctionBlockModel, BlockTarget>();
 		LOGGER.exit();
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		LOGGER.entry(parent);
 		functionBlocks = new ArrayList<FunctionBlockModel>();
 		mapItemToBlockModel = new HashMap<TableItem, FunctionBlockModel>();
 		infoTexts = new LinkedList<String>();
 
 		graphicsManager = new DeployViewGraphics(parent);
 		graphicsManager.initializeParent();
 
 		appName = graphicsManager.createAppNameLabel();
 		serverButton = graphicsManager.createServerButton();
 		graphicsManager.createBlockModelSpecsLabel();
 		deployment = graphicsManager.createDeploymentTable();
 		updateBlocksButton = graphicsManager.createUpdateBlocksButton();
 		graphicsManager.createBlockModelLabel();
 		blockModelName = graphicsManager.createBlockModelName();
 		updateModulesButton = graphicsManager.createUpdateModulesButton();
 		graphicsManager.createModuleLabel();
 		moduleCombo = graphicsManager.createModuleCombo();
 		createButton = graphicsManager.createCreateButton();
 		graphicsManager.createPlaceLabel();
 		places = graphicsManager.createPlacesText();
 		deployButton = graphicsManager.createDeployButton();
 		constraintsButton = graphicsManager.createConstraintsButton();
 		infoText = graphicsManager.createInformationText();
 		createSelectionListeners();
 
 		loadBlockModels(getEditorInput());
 		LOGGER.exit();
 	}
 
 	/**
 	 * Invoked whenever the UpdateModules Button is pressed.
 	 */
 	private void updateModules() {
 		if (ServerManager.getDefault().isRunning()) {
 			FutureNotifier<Collection<Module>> notifier = manager.updateModuleInfo();
 			notifier.addListener(this);
 		} else {
 			warn("Server not running");
 			addNewInfoText("As long as there is no server, no modules can be accessed.");
 		}
 	}
 
 	/**
 	 * Invoked whenever the UpdateBlocks Button is pressed.
 	 */
 	private void updateBlocks() {
 		LOGGER.entry();
 		Collection<FunctionBlockModel> newBlockModels = new ArrayList<FunctionBlockModel>();
 		Map<UUID, FunctionBlockModel> newIDs = new HashMap<UUID, FunctionBlockModel>();
 		Map<UUID, FunctionBlockModel> oldIDs = new HashMap<UUID, FunctionBlockModel>();
 
 		if (getEditorInput() instanceof FileEditorInput) {
 			try {
 				newBlockModels = loadInput((FileEditorInput) getEditorInput());
 			} catch (IOException e) {
 				LOGGER.catching(e);
 			}
 		} else {
 			LOGGER.error("Input is not a FileEditorInput {}", getEditorInput());
 		}
 
 		for (FunctionBlockModel model : newBlockModels) {
 			newIDs.put(model.getID(), model);
 		}
 		for (FunctionBlockModel model : functionBlocks) {
 			oldIDs.put(model.getID(), model);
 		}
 
 		resetDeployment();
 
 		if (selectedBlockModel != null) {
 			if (!newIDs.keySet().contains(selectedBlockModel.getID())) {
 				resetSelectedBlock();
 			} else {
 				selectedBlockModel = newIDs.get(selectedBlockModel.getID());
 				if (selectedBlockModel.getPosition() != null) {
 					places.setText(selectedBlockModel.getPosition());
 				}
 				if (selectedBlockModel.getBlockName() != null) {
 					blockModelName.setText(selectedBlockModel.getBlockName());
 				}
 			}
 		}
 
 		for (FunctionBlockModel oldModel : functionBlocks) {
 			if (newIDs.containsKey(oldModel.getID())) {
 				FunctionBlockModel newModel = newIDs.get(oldModel.getID());
 				replaceBlock(oldModel, newModel);
 			} else {
 				removeBlock(oldModel);
 			}
 		}
 
 		for (FunctionBlockModel newModel : newBlockModels) {
 			if (!oldIDs.containsKey(newModel.getID())) {
 				addBlock(newModel);
 			}
 		}
 		functionBlocks = newBlockModels;
 		addNewInfoText("Block update complete.");
 		LOGGER.exit();
 	}
 
 	/**
 	 * Adds representation of a functionBlockModel that has just been added to the functionBlockModels list.
 	 * 
 	 * @param model
 	 *            FunctionBlockModel to add.
 	 */
 	private void addBlock(FunctionBlockModel model) {
 		TableItem item = new TableItem(deployment, SWT.NONE);
 
 		String name = model.getBlockName();
 		if (name != null) {
 			item.setText(0, name);
 		}
 
 		String position = model.getPosition();
 		if (position != null && !position.isEmpty()) {
 			item.setText(2, position);
 			placeConstraints.put(model, position);
 		}
 
 		mapItemToBlockModel.put(item, model);
 	}
 
 	/**
 	 * Replaces a FunctionBlockModel another FunctionBlockModel. This method does NOT add the newBlock to the
 	 * functionBlockModels list but takes care of everything else - representation and constraints.
 	 * 
 	 * @param oldBlock
 	 *            old Block
 	 * @param newBlock
 	 *            new Block.
 	 */
 	private void replaceBlock(FunctionBlockModel oldBlock, FunctionBlockModel newBlock) {
 		TableItem item = getItem(oldBlock);
 		UUID module = moduleConstraints.get(oldBlock);
 		moduleConstraints.remove(oldBlock);
 		placeConstraints.remove(oldBlock);
 
 		mapItemToBlockModel.put(item, newBlock);
 
 		if (newBlock.getBlockName() != null) {
 			item.setText(0, newBlock.getBlockName());
 		}
 
 		if (module != null) {
 			moduleConstraints.put(newBlock, module);
 		}
 
 		String newPosition = newBlock.getPosition();
 		if (newPosition != null && !newPosition.isEmpty()) {
 			item.setText(2, newPosition);
 			placeConstraints.put(newBlock, newPosition);
 		} else {
 			item.setText(2, "");
 		}
 	}
 
 	private void removeBlock(FunctionBlockModel model) {
 		TableItem item = getItem(model);
 		moduleConstraints.remove(model);
 		placeConstraints.remove(model);
 		mapItemToBlockModel.remove(item);
 		item.dispose();
 	}
 
 	/**
 	 * Invoked whenever the Create Button is pressed.
 	 */
 	private void create() {
 		LOGGER.entry();
 		Collection<Constraint> constraints = new ArrayList<Constraint>();
 		constraints.add(new UserConstraints(moduleConstraints, placeConstraints));
 
 		Distribution dist = null;
 		try {
 			dist = DistributionCreator.createDistribution(functionBlocks, constraints);
 		} catch (NoBlocksException e) {
 			warn("No blockModels to distribute");
 			LOGGER.exit();
 		} catch (NoModulesException e) {
 			warn("No modules to deploy on");
 			LOGGER.exit();
 		}
 
 		if (dist == null) {
 			warn("No valid deployment exists");
 		} else {
 			mapBlockToTarget = dist.getMapping();
 			for (FunctionBlockModel block : mapBlockToTarget.keySet()) {
 				TableItem item = getItem(block);
 				final String name = mapBlockToTarget.get(block).getModule().getName();
 				item.setText(3, name == null ? "" : name);
 				final String location = mapBlockToTarget.get(block).getModule().getLocation();
 				item.setText(4, location == null ? "" : location);
 				deployButton.setEnabled(true);
 				newConstraints = false;
 			}
 			addNewInfoText("Deployment created.");
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Invoked whenever the Deploy Button is pressed.
 	 */
 	private void deploy() {
 		LOGGER.entry();
 		if (newConstraints) {
 			int cancel = warn(DeployViewTexts.NEWCONSTRAINTS);
 			if (cancel == -4) {
 				LOGGER.exit();
 				return;
 			}
 		}
 
 		if (mapBlockToTarget.isEmpty()) {
 			warn(DeployViewTexts.NO_DEPLOYMENT_YET);
 			LOGGER.exit();
 			return;
 		}
 
 		final Dependencies dependencies =
 				new Dependencies(StringUtil.joinArray(classPath, ":"), Arrays.asList(Pattern.compile("java\\..*"),
 						Pattern.compile("edu\\.teco\\.dnd\\..*"), Pattern.compile("com\\.google\\.gson\\..*"),
 						Pattern.compile("org\\.apache\\.bcel\\..*"), Pattern.compile("io\\.netty\\..*"),
 						Pattern.compile("org\\.apache\\.logging\\.log4j")));
 		final Deploy deploy =
 				new Deploy(serverManager.getConnectionManager(), mapBlockToTarget, appName.getText(), dependencies);
 		// TODO: I don't know if this will be needed by DeployView. It can be used to wait until the deployment finishes
 		// or to run code at that point
 		
 		deploy.getDeployFutureNotifier().addListener(new FutureListener<FutureNotifier<? super Void>>() {
 			@Override
 			public void operationComplete(final FutureNotifier<? super Void> future) {
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (LOGGER.isInfoEnabled()) {
 							LOGGER.info("deploy: {}", future.isSuccess());
 						}
 						if (future.isSuccess()) {
 							addNewInfoText("Deployment complete.");
 						} else {
 							addNewInfoText("Deployment failed.");
 						}
 					}
 				});
 			}
 		});
 		
 		DeployViewProgress prog = new DeployViewProgress();
 		prog.startDeploying(appName.getText(), deploy, mapBlockToTarget);
 		resetDeployment();
 		LOGGER.exit();
 	}
 
 	/**
 	 * Invoked whenever a Function BlockModel from the deploymentTable is selected.
 	 */
 	protected void blockModelSelected() {
 		if (!idList.isEmpty()) {
 			moduleCombo.setEnabled(true);
 		}
 		places.setEnabled(true);
 		constraintsButton.setEnabled(true);
 		blockModelName.setEnabled(true);
 		TableItem[] items = deployment.getSelection();
 		if (items.length == 1) {
 			selectedItem = items[0];
 			selectedBlockModel = mapItemToBlockModel.get(items[0]);
 			blockModelName.setText(selectedBlockModel.getBlockName());
 			if (placeConstraints.containsKey(selectedBlockModel)) {
 				places.setText(placeConstraints.get(selectedBlockModel));
 			} else {
 				places.setText("");
 			}
 			selectedIndex = idList.indexOf(moduleConstraints.get(selectedBlockModel)) + 1;
 			moduleCombo.select(selectedIndex);
 		}
 	}
 
 	/**
 	 * Invoked whenever a Module from moduleCombo is selected.
 	 */
 	private void moduleSelected() {
 		selectedIndex = moduleCombo.getSelectionIndex();
 	}
 
 	private void saveConstraints() {
 		String text = places.getText();
 
 		if (selectedIndex > 0) {
 			selectedID = idList.get(selectedIndex - 1);
 		} else {
 			selectedID = null;
 		}
 
 		if (!text.isEmpty() && selectedID != null) {
 			replaceInfoText(DeployViewTexts.INFORM_CONSTRAINTS);
 			int cancel = warn(DeployViewTexts.WARN_CONSTRAINTS);
 			if (cancel == -4) {
 				addNewInfoText("Constrains not saved.");
 				return;
 			}
 		}
 
 		if (text.isEmpty()) {
 			placeConstraints.remove(selectedBlockModel);
 		} else {
 			placeConstraints.put(selectedBlockModel, text);
 		}
 		selectedBlockModel.setPosition(text);
 		selectedItem.setText(2, text);
 
 		if (selectedID != null) {
 			moduleConstraints.put(selectedBlockModel, selectedID);
 			String module = moduleCombo.getItem(selectedIndex);
 			selectedItem.setText(1, module);
 		} else {
 			selectedItem.setText(1, "");
 			moduleConstraints.remove(selectedBlockModel);
 		}
 
 		String newName = this.blockModelName.getText();
 		selectedBlockModel.setBlockName(newName);
 		selectedItem.setText(0, newName);
 
 		try {
 			resource.save(Collections.EMPTY_MAP);
 			resource.setModified(true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		firePropertyChange(IEditorPart.PROP_DIRTY);
 
 		addNewInfoText("Constrains saved.");
 
 		newConstraints = true;
 	}
 
 	/**
 	 * Adds a Module ID.
 	 * 
 	 * @param id
 	 *            the ID to add
 	 */
 	private synchronized void addID(final UUID id) {
 		LOGGER.entry(id);
 		if (!idList.contains(id)) {
 			LOGGER.trace("id {} is new, adding", id);
 			moduleCombo.add(id.toString());
 			idList.add(id);
 
 		} else {
 			LOGGER.debug("trying to add existing id {}", id);
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Removes a Module ID including all dependent information on this module.
 	 * 
 	 * @param id
 	 *            the ID to remove
 	 */
 	private synchronized void removeID(final UUID id) {
 		LOGGER.entry(id);
 		int idIndex = idList.indexOf(id);
 		if (idIndex >= 0) {
 			moduleCombo.remove(idIndex + 1);
 			idList.remove(idIndex);
 			resetDeployment();
 			for (FunctionBlockModel model : moduleConstraints.keySet()) {
 				if (moduleConstraints.get(model).equals(id)) {
 					moduleConstraints.remove(model);
 					getItem(model).setText(1, "");
 				}
 			}
 			LOGGER.trace("found combo entry for id {}", id);
 		} else {
 			LOGGER.debug("trying to remove nonexistant id {}", id);
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Loads the FunctinoBlockModels of a data flow graph and displays them in the deployment table.
 	 * 
 	 * @param input
 	 *            the input of the editor
 	 */
 	private void loadBlockModels(IEditorInput input) {
 		// set to empty Collection to prevent NPE in case loading fails
 		functionBlocks = new ArrayList<FunctionBlockModel>();
 		if (input instanceof FileEditorInput) {
 			final IProject project = EclipseUtil.getWorkspaceProject(((FileEditorInput) input).getPath());
 			if (project != null) {
 				classPath = getClassPath(project);
 			} else {
 				classPath = new URL[0];
 			}
 
 			try {
 				functionBlocks = loadInput((FileEditorInput) input);
 			} catch (IOException e) {
 				LOGGER.catching(e);
 			}
 		} else {
 			LOGGER.error("Input is not a FileEditorInput {}", input);
 		}
 		mapItemToBlockModel.clear();
 		for (FunctionBlockModel blockModel : functionBlocks) {
 			addBlock(blockModel);
 		}
 	}
 
 	private URL[] getClassPath(final IProject project) {
 		final Set<IPath> paths = EclipseUtil.getAbsoluteBinPaths(project);
 		final ArrayList<URL> classPath = new ArrayList<URL>(paths.size());
 		for (final IPath path : paths) {
 			try {
 				classPath.add(path.toFile().toURI().toURL());
 			} catch (final MalformedURLException e) {
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.catching(Level.DEBUG, e);
 					LOGGER.debug("Not adding path {} to class path", path);
 				}
 			}
 		}
 		return classPath.toArray(new URL[0]);
 	}
 
 	/**
 	 * Loads the given data flow graph. The file given in the editor input must be a valid graph. Its function block
 	 * models are loaded into a list and returned.
 	 * 
 	 * @param input
 	 *            the input of the editor
 	 * @return a collection of FunctionBlockModels that were defined in the model
 	 * @throws IOException
 	 *             if reading fails
 	 */
 	private Collection<FunctionBlockModel> loadInput(final FileEditorInput input) throws IOException {
 		LOGGER.entry(input);
 		Collection<FunctionBlockModel> blockModelList = new ArrayList<FunctionBlockModel>();
 		appName.setText(input.getFile().getName().replaceAll("\\.blocks", ""));
 
 		System.out.println(input.getURI().toASCIIString());
 
 		URI uri = URI.createURI(input.getURI().toASCIIString());
 		resource = new XMIResourceImpl(uri);
 		resource.setTrackingModification(true);
 		resource.load(null);
 		for (EObject object : resource.getContents()) {
 			if (object instanceof FunctionBlockModel) {
 				LOGGER.trace("found FunctionBlockModel {}", object);
 				FunctionBlockModel blockmodel = (FunctionBlockModel) object;
 				blockModelList.add(blockmodel);
 			}
 		}
 		return blockModelList;
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
 	 * Returns table item representing a given function blockModel.
 	 * 
 	 * @param blockModel
 	 *            The blockModel to find in the table
 	 * @return item holding the blockModel
 	 */
 	private TableItem getItem(FunctionBlockModel blockModel) {
 		UUID id = blockModel.getID();
 		for (TableItem i : mapItemToBlockModel.keySet()) {
 			if (mapItemToBlockModel.get(i).getID() == id) {
 				return i;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Invoked whenever a possibly created deployment gets invalid.
 	 */
 	private void resetDeployment() {
 		mapBlockToTarget = new HashMap<FunctionBlockModel, BlockTarget>();
 		deployButton.setEnabled(false);
 		for (TableItem item : deployment.getItems()) {
 			item.setText(3, "");
 			item.setText(4, "");
 		}
 	}
 
 	/**
 	 * Invoked whenever the selected Block gets dirty.
 	 */
 	private void resetSelectedBlock() {
 		places.setText("");
 		selectedItem = null;
 		selectedBlockModel = null;
 		blockModelName.setText("<select block on the left>");
 		blockModelName.setEnabled(false);
 		moduleCombo.setEnabled(false);
 		places.setEnabled(false);
 		constraintsButton.setEnabled(false);
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isDirty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void dispose() {
 		manager.removeModuleManagerListener(this);
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
 	public void moduleOffline(final UUID id, Module module) {
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
 				if (!idList.contains(id)) {
 					LOGGER.entry(id);
 					LOGGER.trace("id {} is new, adding", id);
 					idList.add(id);
 					LOGGER.exit();
 				}
 
 				int comboIndex = idList.indexOf(id) + 1;
 				String text = "";
 				if (module.getName() != null) {
 					text = module.getName();
 				}
 				text = text.concat(" : ");
 				text = text.concat(id.toString());
 				moduleCombo.setItem(comboIndex, text);
 				if (moduleConstraints.containsValue(id)) {
 					for (FunctionBlockModel blockModel : moduleConstraints.keySet()) {
 						getItem(blockModel).setText(1, text);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void serverOnline(final Map<UUID, Module> modules) {
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (serverButton != null) {
 					serverButton.setText("Stop Server");
 				}
 				updateModulesButton.setEnabled(true);
 				createButton.setEnabled(true);
 				moduleCombo.setToolTipText("");
 
 				synchronized (DeployView.this) {
 					while (!idList.isEmpty()) {
 						removeID(idList.get(0)); // TODO: Unschön, aber geht
 													// hoffentlich?
 					}
 					for (UUID moduleID : modules.keySet()) {
 						addID(moduleID);
 					}
 				}
 				if (infoText != null && infoTexts != null) {
 					addNewInfoText("Server online.");
 				}
 			}
 		});
 	}
 
 	@Override
 	public void serverOffline() {
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (serverButton != null) {
 					serverButton.setText("Start Server");
 				}
 				updateModulesButton.setEnabled(false);
 				createButton.setEnabled(false);
 				resetDeployment();
 				moduleCombo.setToolTipText(DeployViewTexts.SELECTMODULEOFFLINE_TOOLTIP);
 				synchronized (DeployView.this) {
 					while (!idList.isEmpty()) {
 						removeID(idList.get(0)); // TODO: Unschön, aber geht
 													// hoffentlich?
 					}
 				}
 				if (infoText != null && infoTexts != null) {
 					addNewInfoText("Server offline.");
 				}
 			}
 		});
 	}
 
 	private void createSelectionListeners() {
 		serverButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				new Thread() {
 					@Override
 					public void run() {
 						if (ServerManager.getDefault().isRunning()) {
 							DeployView.this.activator.shutdownServer();
 						} else {
 							DeployView.this.activator.startServer();
 						}
 					}
 				}.start();
 			}
 		});
 
 		deployment.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.blockModelSelected();
 			}
 		});
 
 		updateModulesButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.updateModules();
 			}
 		});
 
 		updateBlocksButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.updateBlocks();
 			}
 		});
 
 		blockModelName.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				if (e.keyCode == ENTER) {
 					DeployView.this.saveConstraints();
 				}
 			}
 		});
 
 		moduleCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.moduleSelected();
 			}
 		});
 
 		places.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				if (e.keyCode == ENTER) {
 					DeployView.this.saveConstraints();
 				}
 			}
 		});
 
 		createButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.create();
 			}
 		});
 
 		deployButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.deploy();
 			}
 		});
 
 		constraintsButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DeployView.this.saveConstraints();
 			}
 		});
 	}
 
 	/**
 	 * Invoked whenever the update modules button was pressed and the update failed or is completed.
 	 */
 
 	@Override
 	public void operationComplete(final JoinedFutureNotifier<Module> future) throws Exception {
 		display.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				String newText;
 				if (future.isSuccess()) {
 					newText = "Module update complete.";
 				} else {
 					newText = "Module update failed.";
 				}
 				addNewInfoText(newText);
 			}
 		});
 	}
 
 	private void addNewInfoText(String text) {
 		infoTexts.add(text);
 		text = text.concat("\n");
 		text = text.concat(infoText.getText());
 		infoText.setText(text);
 		limitInfoText();
 	}
 
 	private void replaceInfoText(String text) {
 		infoTexts.clear();
 		infoTexts.add(text);
 		infoText.setText(text);
 		limitInfoText();
 	}
 
 	private void limitInfoText() {
 		while (infoText.getLineCount() > 20 && !infoTexts.isEmpty()) {
 			String oldText = infoTexts.remove(0);
 			int newInfoLength = infoText.getText().length() - 1 - oldText.length();
 			infoText.setText(infoText.getText().substring(0, newInfoLength));
 		}
 	}
 }
