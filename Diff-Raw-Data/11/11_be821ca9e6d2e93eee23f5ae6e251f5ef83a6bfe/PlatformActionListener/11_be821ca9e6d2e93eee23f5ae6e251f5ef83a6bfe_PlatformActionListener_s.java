 package org.bh.platform;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
import java.util.ServiceLoader;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
import org.bh.controller.IController;
 import org.bh.controller.IPeriodController;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.StringValue;
 import org.bh.gui.swing.BHComboBox;
 import org.bh.gui.swing.BHContent;
import org.bh.gui.swing.BHExportDialog;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHOptionDialog;
 import org.bh.gui.swing.BHStatusBar;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.gui.swing.IBHAction;
 import org.bh.platform.PlatformController.BHTreeModel;
 import org.bh.platform.i18n.BHTranslator;
import org.bh.plugin.xmldataexchange.XMLDataExchangeController;
 
 /**
  * The PlatformActionListener handles all actions that are fired by a button
  * etc. of the platform.
  */
 class PlatformActionListener implements ActionListener {
 
 	private static final Logger log = Logger
 			.getLogger(PlatformActionListener.class);
 
 	BHMainFrame bhmf;
 	ProjectRepositoryManager projectRepoManager;
 	PlatformController pC;
 
 	public PlatformActionListener(BHMainFrame bhmf,
 			ProjectRepositoryManager projectRepoManager,
 			PlatformController platformController) {
 		this.bhmf = bhmf;
 		this.projectRepoManager = projectRepoManager;
 		this.pC = platformController;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent aEvent) {
 
 		// get actionKey of fired action
 		PlatformKey actionKey = ((IBHAction) aEvent.getSource())
 				.getPlatformKey();
 
 		// do right action...
 		switch (actionKey) {
 
 		/*
 		 * Clear current workspace
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILENEW:
 			log.debug("handling FILENEW event");
 			this.fileNew();
 			break;
 
 		/*
 		 * Open a workspace
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILEOPEN:
 			log.debug("handling FILEOPEN event");
 			this.fileOpen();
 			break;
 
 		/*
 		 * Save the whole workspace using the already defined filepath or ask for new path
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILESAVE:
 			log.debug("handling FILESAVE event");
 			Services.firePlatformEvent(new PlatformEvent(PlatformActionListener.class, PlatformEvent.Type.SAVE));
 			break;
 
 		/*
 		 * Save the whole workspace - incl. filepath save dialog
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILESAVEAS:
 			log.debug("handling FILESAVEAS event");
 			Services.firePlatformEvent(new PlatformEvent(PlatformActionListener.class, PlatformEvent.Type.SAVEAS));
 			break;
 		
 		/*
 		 * Clear workspace - same like filenew
 		 * 
 		 * @author Michael Löckelt
 		 */	
 		case FILECLOSE:
 			log.debug("handling FILECLOSE event");
 			this.fileNew();
 			break;
 
 		case FILEQUIT:
 			log.debug("handling FILEQUIT event");
 			bhmf.dispose();
 			break;
 
 		case PROJECTCREATE:
 			this.createProject();
 			break;
 
 		case PROJECTDUPLICATE:
 			this.duplicateProject();
 			break;
 
 		// TODO Katzor.Marcus
 		case PROJECTIMPORT:
 
 			break;
 
 		// TODO Katzor.Marcus
 
 		case PROJECTEXPORT:
 			if (bhmf.getBHTree().getSelectionPath() != null)
 			{
 				BHTreeNode selectedNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 				if (selectedNode.getUserObject() instanceof DTOProject)
 				{
 					// Get export plugin
 					PluginManager.getInstance().loadAllServices(IController.class);
 					ServiceLoader<IController> controller = PluginManager.getInstance().getServices(IController.class);		
 					XMLDataExchangeController exportController = null;
 					for (IController contrl : controller)
 					{			
 						contrl.getClass().getPackage().getName().equals("org.bh.plugin.xmldataexchange");
 						{
 							exportController = (XMLDataExchangeController) contrl;				
 							break;
 						}
 					}
 					if (exportController == null)
 					{
 						// TODO Katzor.Marcus Show Message
 						return;
 					}
 					
 					exportController.setExportView();
 					
 					BHExportDialog exportDialog = new BHExportDialog();
 					exportDialog.add(exportController.getViewPanel());
 					exportDialog.setVisible(true);
 					
 				}
 				else
 				{
 					// TODO Katzor.Marcus Show Message
 				}
 			}
 			else
 			{
 				// TODO Katzor.Marcus Show Message
 			}
 			break;
 
 		case PROJECTREMOVE:
 			TreePath currentRemoveProjectSelection = bhmf.getBHTree()
 					.getSelectionPath();
 			if (currentRemoveProjectSelection != null) {
 				BHTreeNode removeProjectNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 				if (removeProjectNode.getUserObject() instanceof DTOProject) {
 					((BHTreeModel) bhmf.getBHTree().getModel())
 							.removeNodeFromParent(removeProjectNode);
 					projectRepoManager
 							.removeProject((DTOProject) removeProjectNode
 									.getUserObject());
 				} else {
 					BHStatusBar.getInstance().setHint(
 							BHTranslator.getInstance().translate(
 									"EisSelectProject"), true);
 				}
 			}
 			break;
 
 		case SCENARIOCREATE:
 			this.createScenario();
 			break;
 
 		case SCENARIODUPLICATE:
 			this.duplicateScenario();
 			break;
 
 		case SCENARIOMOVE:
 			// TODO Drag&Drop
 
 			break;
 
 		case SCENARIOREMOVE:
 			TreePath currentRemoveScenarioSelection = bhmf.getBHTree()
 					.getSelectionPath();
 			if (currentRemoveScenarioSelection != null) {
 				BHTreeNode removeScenarioNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 				if (removeScenarioNode.getUserObject() instanceof DTOScenario) {
 					((BHTreeModel) bhmf.getBHTree().getModel())
 							.removeNodeFromParent(removeScenarioNode);
 					((DTOScenario) ((BHTreeNode) removeScenarioNode.getParent())
 							.getUserObject())
 							.removeChild((DTOPeriod) removeScenarioNode
 									.getUserObject());
 				} else {
 					BHStatusBar.getInstance().setHint(
 							BHTranslator.getInstance().translate(
 									"EisSelectScenario"), true);
 				}
 			}
 			break;
 
 		case BILANZGUVSHOW:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case BILANZGUVCREATE:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case BILANZGUVIMPORT:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case BILANZGUVREMOVE:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case OPTIONSCHANGE:
 			// TODO Tietze.Patrick
 			new BHOptionDialog();
 
 			break;
 
 		case HELPUSERHELP:
 
 			log.debug("HELPUSERHELP gefeuert");
 			JDialog frame = new JDialog();
 			frame.setTitle(BHTranslator.getInstance().translate(
 					"MuserHelpDialog"));
 			frame.setSize(610, 600);
 			frame.getContentPane().add(new BHHelpSystem("userhelp"));
 			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			frame.setResizable(false);
 			frame.setLocationRelativeTo(null);
 			frame.setVisible(true);
 			break;
 
 		case HELPMATHHELP:
 			System.out.println("HELPMATHHELP gefeuert");
 			JDialog mathframe = new JDialog();
 			// TODO Hartgecodeder String raus!! LZ
 			mathframe.setTitle("Business Horizon Mathematische Erläuterungen");
 			mathframe.setSize(610, 600);
 			mathframe.getContentPane().add(new BHHelpSystem("mathhelp"));
 			mathframe
 					.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			mathframe.setLocationRelativeTo(null);
 			mathframe.setResizable(false);
 			mathframe.setVisible(true);
 			break;
 
 		case HELPINFO:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case TOOLBAROPEN:
 			log.debug("handling TOOLBAROPEN event");
 			this.fileOpen();
 			break;
 
 		case TOOLBARSAVE:
 			log.debug("handling TOOLBARSAVE event");
 			Services.firePlatformEvent(new PlatformEvent(PlatformActionListener.class, PlatformEvent.Type.SAVE));
 			break;
 
 		case TOOLBARADDPRO:
 			this.createProject();
 			break;
 
 		case TOOLBARADDS:
 			this.createScenario();
 			break;
 
 		case TOOLBARADDPER:
 			this.createPeriod();
 			break;
 
 		case TOOLBARREMOVE:
 
 			TreePath currentSelection = bhmf.getBHTree().getSelectionPath();
 			// is a node selected?
 			if (currentSelection != null) {
 				// find out current selected node
 				BHTreeNode currentNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 
 				// remove node from data model...
 				if (currentNode.getUserObject() instanceof DTOProject) {
 					projectRepoManager.removeProject((DTOProject) currentNode
 							.getUserObject());
 
 				} else if (currentNode.getUserObject() instanceof DTOScenario) {
 					((DTOProject) ((BHTreeNode) currentNode.getParent())
 							.getUserObject())
 							.removeChild((DTOScenario) currentNode
 									.getUserObject());
 
 				} else if (currentNode.getUserObject() instanceof DTOPeriod) {
 					((DTOScenario) ((BHTreeNode) currentNode.getParent())
 							.getUserObject())
 							.removeChild((DTOPeriod) currentNode
 									.getUserObject());
 				}
 
 				// ... and from GUI and select other node or empty screen
 				TreePath tp = new TreePath(currentNode.getPreviousNode().getPath());
 				bhmf.getBHTree().setSelectionPath(tp);
 				if(bhmf.getBHTree().getSelectionPath().getPathCount() == 1)
 					bhmf.addContentForms(new BHContent());
 				
 				((BHTreeModel) bhmf.getBHTree().getModel())
 						.removeNodeFromParent(currentNode);
 				
 				
 			}
 
 			break;
 
 		default:
 			// TODO implementieren?
 			break;
 		}
 	}
 
 	/*
 	 * could be done with more glamour ;)
 	 */
 	private void fileNew() {
 		if (ProjectRepositoryManager.isChanged()) {
 			
 			int i = JOptionPane.showConfirmDialog(bhmf, Services.getTranslator().translate("Psave"));
 			
 			if (i == JOptionPane.YES_OPTION) {
 				Services.firePlatformEvent(new PlatformEvent(BHMainFrame.class, PlatformEvent.Type.SAVE));
 				projectRepoManager.clearProjectList();
 				pC.setupTree(bhmf, projectRepoManager);
 				PlatformController.preferences.remove("path");
 				
 				ProjectRepositoryManager.setChanged(false);
 				bhmf.resetTitle();
 			} else if (i == JOptionPane.NO_OPTION) {
 				Logger.getLogger(getClass()).debug("Existing changes but no save wish - clear project list");
 				projectRepoManager.clearProjectList();
 				pC.setupTree(bhmf, projectRepoManager);
 				PlatformController.preferences.remove("path");
 				
 				ProjectRepositoryManager.setChanged(false);
 				bhmf.resetTitle();
 			} else if (i == JOptionPane.CANCEL_OPTION) {
 				
 			}
 			
 		} else { 
 			Logger.getLogger(getClass()).debug("No changes - clear project list");
 			projectRepoManager.clearProjectList();
 			pC.setupTree(bhmf, projectRepoManager);
 			PlatformController.preferences.remove("path");
 			
 			ProjectRepositoryManager.setChanged(false);
 			bhmf.resetTitle();
 		}
 	}
 
 	private void fileOpen() {
 		// create a open-dialog
 		int returnVal = bhmf.getChooser().showOpenDialog(bhmf);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			log.debug("You chose to open this file: "
 					+ bhmf.getChooser().getSelectedFile().getName());
 
 			// open already provided file
 			PlatformController.platformPersistenceManager.openFile(bhmf.getChooser().getSelectedFile());
 
 			// rebuild Tree
 			pC.setupTree(bhmf, projectRepoManager);
 			
 			bhmf.getBHTree().expandAll();
 		}
 	}
 
 	private void createProject() {
 		// Create new project
 		DTOProject newProject = new DTOProject();
 		// TODO hardgecodeder String raus! AS
 		newProject.put(DTOProject.Key.NAME, new StringValue("neues Projekt"));
 		// add it to DTO-Repository
 		projectRepoManager.addProject(newProject);
 
 		// and create a Node for tree on gui
 		BHTreeNode newProjectNode = bhmf.getBHTree().addProjectNode(newProject,
 				bhmf);
 
 		// last steps: unfold tree to new element, set focus and start editing
 		bhmf.getBHTree().scrollPathToVisible(
 				new TreePath(newProjectNode.getPath()));
 		bhmf.getBHTree().startEditingAtPath(
 				new TreePath(newProjectNode.getPath()));
 
 	}
 
 	private void createScenario() {
 		// If a path is selected...
 		if (bhmf.getBHTree().getSelectionPath() != null) {
 			
 			// ...create new scenario
 			DTOScenario newScenario = new DTOScenario();
 			// TODO hardgecodeder String raus! AS
 			newScenario.put(DTOScenario.Key.NAME, new StringValue(
 					"neues Scenario"));
 			
 			// ...add it to DTO-Repository
 			((DTOProject) ((BHTreeNode) bhmf.getBHTree().getSelectionPath()
 					.getPathComponent(1)).getUserObject())
 					.addChild(newScenario);
 
 			//ceck kind of scenario: deterministic or stochastic?
 			//TODO Schmalzhaf.Alexander: String raus!
 			
 			ArrayList<BHComboBox.Item> itemsList = new ArrayList<BHComboBox.Item>();
 			itemsList.add(new BHComboBox.Item("stochastic", new StringValue("stochastisch")));
 			itemsList.add(new BHComboBox.Item("deterministic", new StringValue("deterministisch")));
 			BHComboBox.Item res = (BHComboBox.Item) JOptionPane
 			.showInputDialog(bhmf,
 					"Bitte gewünschten Szenariotyp auswählen:",
 					"Periodentyp auswählen",
 					JOptionPane.QUESTION_MESSAGE, null, itemsList
 							.toArray(), null);
 			
 			if(res.getKey().equalsIgnoreCase("STOCHASTIC"))
 					newScenario.put(DTOScenario.Key.STOCHASTIC_PROCESS, new StringValue("true"));
 			
 			// ...and insert it into GUI-Tree
 			BHTreeNode newScenarioNode = bhmf.getBHTree().addScenarioNode(
 					newScenario, bhmf);
 
 			// last steps: unfold tree to new element, set focus and start
 			// editing
 			bhmf.getBHTree().scrollPathToVisible(
 					new TreePath(newScenarioNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(
 					new TreePath(newScenarioNode.getPath()));
 		} else {
 			BHStatusBar.getInstance().setHint(
 					BHTranslator.getInstance().translate("EisSelectPeriod"),
 					true);
 		}
 
 	}
 
 	private void createPeriod() {
 		// If a scenario or a period is selected...
 		if (bhmf.getBHTree().getSelectionPath() != null
 				&& bhmf.getBHTree().getSelectionPath().getPathCount() > 2) {
 			// TODO Schmalzhaf.Alexander at first: show selection screen for
 			// sort of Period
 			IPeriodController[] periodTypes = Services.getPeriodControllers()
 					.values().toArray(new IPeriodController[0]);
 			Arrays.sort(periodTypes, new Comparator<IPeriodController>() {
 				@Override
 				public int compare(IPeriodController o1, IPeriodController o2) {
 					return o2.getGuiPriority() - o1.getGuiPriority();
 				}
 			});
 			ArrayList<BHComboBox.Item> itemsList = new ArrayList<BHComboBox.Item>();
 			for (IPeriodController periodType : periodTypes) {
 				itemsList.add(new BHComboBox.Item(periodType.getGuiKey(),
 						new StringValue(periodType.getGuiKey())));
 			}
 
 			// TODO Thiele.Klaus dürfen "wir" die OptionPane benutzen?
 
 			// TODO Schmalzhaf.Alexander hartgecodete Strings raus!
 			BHComboBox.Item res = (BHComboBox.Item) JOptionPane
 					.showInputDialog(bhmf,
 							"Bitte gewünschten Periodentyp auswählen:",
 							"Periodentyp auswählen",
 							JOptionPane.QUESTION_MESSAGE, null, itemsList
 									.toArray(), null);
 
 			if (res == null) {
 				return;
 			}
 
 			// ...create new period
 			DTOPeriod newPeriod = new DTOPeriod();
 			// TODO hardgecodeder String raus! AS
 			newPeriod.put(DTOPeriod.Key.NAME, new StringValue("neue Periode"));
 
 			// ...add it to DTO-Repository
 			((DTOScenario) ((BHTreeNode) bhmf.getBHTree().getSelectionPath()
 					.getPathComponent(2)).getUserObject()).addChild(newPeriod);
 
 			// ...and insert it into GUI-Tree
 			BHTreeNode newPeriodNode = bhmf.getBHTree().addPeriodNode(
 					newPeriod, bhmf);
 
 			// last steps: unfold tree to new element, set focus and start
 			// editing
 			bhmf.getBHTree().scrollPathToVisible(
 					new TreePath(newPeriodNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(
 					new TreePath(newPeriodNode.getPath()));
 
 			// create concrete Period
 			// TODO Schmalzhaf.Alexander es gibt kein Panel, das ich greifen
 			// kann vom bhmf -> muss eigenes erzeugen
 			JPanel panel = new JPanel();
 			bhmf.addContentForms(panel);
 			Services.getPeriodController(res.getKey())
 					.editDTO(newPeriod, panel);
 		}
 	}
 
 	private void duplicateProject() {
 		TreePath currentDuplicateProjectSelection = bhmf.getBHTree()
 				.getSelectionPath();
 		if (currentDuplicateProjectSelection != null) {
 			// Access to selected project
 			BHTreeNode duplicateProjectNode = (BHTreeNode) bhmf.getBHTree()
 					.getSelectionPath().getLastPathComponent();
 
 			// zu kopierendes Project in eigene Variable
 			DTOProject duplicateProject = (DTOProject) duplicateProjectNode
 					.getUserObject();
 
 			// neues DTOProject mit Referenz auf den Klon
 
 			DTOProject newProject = (DTOProject) duplicateProject.clone();
 
 			BHTreeNode newProjectNode = bhmf.getBHTree().addProjectNode(
 					newProject, bhmf);
 
 			for (int x = 0; x < newProject.getChildren().size(); x++) {
 				BHTreeNode newScenarioNode = bhmf.getBHTree()
 						.duplicateScenarioNode(newProject.getChildren().get(x),
 								bhmf, newProjectNode);
 
 				for (int y = 0; y < newProject.getChildren().get(x)
 						.getChildren().size(); y++) {
 					newScenarioNode.add(bhmf.getBHTree().duplicatePeriodNode(
 							newProject.getChildren().get(x).getChildren()
 									.get(y), bhmf, newScenarioNode));
 				}
 			}
 
 			// add DTOProject to repository
 			projectRepoManager.addProject(newProject);
 
 			// add DTOProject to repository
 			projectRepoManager.addProject(newProject);
 
 		} else {
 			BHStatusBar.getInstance().setHint(
 					BHTranslator.getInstance().translate("EisSelectProject"),
 					true);
 		}
 	}
 
 	private void duplicateScenario() {
 		TreePath currentDuplicateProjectSelection = bhmf.getBHTree()
 				.getSelectionPath();
 		if (currentDuplicateProjectSelection != null) {
 			// Access to selected project
 			BHTreeNode duplicateScenarioNode = (BHTreeNode) bhmf.getBHTree()
 					.getSelectionPath().getLastPathComponent();
 
 			// zu kopierendes Project in eigene Variable
 			DTOScenario duplicateScenario = (DTOScenario) duplicateScenarioNode
 					.getUserObject();
 
 			// neues DTOProject mit Referenz auf den Klon
 			DTOScenario newScenario = (DTOScenario) duplicateScenario.clone();
 
 			BHTreeNode newScenarioNode = bhmf.getBHTree().addScenarioNode(
 					newScenario, bhmf);
 
 			for (int y = 0; y < newScenario.getChildren().size(); y++) {
 				newScenarioNode.add(bhmf.getBHTree()
 						.duplicatePeriodNode(newScenario.getChildren().get(y),
 								bhmf, newScenarioNode));
 			}
 
 			// add DTOScenario to parent DTOProject
 			((DTOProject) ((BHTreeNode) bhmf.getBHTree().getSelectionPath()
 					.getPathComponent(1)).getUserObject())
 					.addChild(newScenario);
 
 		} else {
 			BHStatusBar.getInstance().setHint(
 					BHTranslator.getInstance().translate("EisSelectScenario"),
 					true);
 		}
 	}
 }
