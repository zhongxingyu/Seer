 package org.bh.platform;
 
 import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Popup;
 import javax.swing.PopupFactory;
 import javax.swing.WindowConstants;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.IPeriodController;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.data.types.StringValue;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHOptionDialog;
 import org.bh.gui.swing.BHStatusBar;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.gui.swing.IBHAction;
 import org.bh.platform.PlatformController.BHTreeModel;
 import org.bh.platform.i18n.BHTranslator;
 
 /**
  * The PlatformActionListener handles all actions that are fired by a button
  * etc. of the platform.
  */
 class PlatformActionListener implements ActionListener {
 	
 	private static final Logger log = Logger.getLogger(PlatformActionListener.class);
 
 	BHMainFrame bhmf;
 	ProjectRepositoryManager projectRepoManager;
 	PlatformController pC;
 	
 	public PlatformActionListener(BHMainFrame bhmf, ProjectRepositoryManager projectRepoManager, PlatformController platformController){
 		this.bhmf = bhmf;
 		this.projectRepoManager = projectRepoManager;
 		this.pC = platformController;
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent aEvent) {
 
 		// get actionKey of fired action
 		PlatformKey actionKey = ((IBHAction) aEvent.getSource()).getPlatformKey();
 		
 		//do right action...
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
 		 * Save the whole workspace using the already defined filepath
 		 * 
 		 * @author Michael Löckelt
 		 */
 			
 		case FILESAVE:
 			log.debug("handling FILESAVE event");
 			this.fileSave();
 			break;
 			
 		/*
 		 * Save the whole workspace - incl. filepath save dialog
 		 * 
 		 * @author Michael Löckelt
 		 */
 			
 		case FILESAVEAS:
 			log.debug("handling FILESAVEAS event");
 			this.fileSaveAs();
 			break;
 			
 		case FILECLOSE:
 			// TODO Loeckelt.Michael Fileclose implementieren.
 			break;
 			
 		case FILEQUIT:
 			//TODO Prüfen und ggf. implementieren!
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
 			DTOProject t = new DTOProject();
 			break;
 			
 		case PROJECTREMOVE:
 			TreePath currentRemoveProjectSelection = bhmf.getBHTree().getSelectionPath();
 			if(currentRemoveProjectSelection != null){
 				BHTreeNode removeProjectNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 				if(removeProjectNode.getUserObject() instanceof DTOProject){
 					((BHTreeModel) bhmf.getBHTree().getModel()).removeNodeFromParent(removeProjectNode);
 					projectRepoManager.removeProject((DTOProject) removeProjectNode.getUserObject());
 				} else {
 					BHStatusBar.getInstance().setHint(BHTranslator.getInstance().translate("EisSelectProject"), true);										
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
 			//TODO Drag&Drop
 			
 			break;
 			
 		case SCENARIOREMOVE:
 			TreePath currentRemoveScenarioSelection = bhmf.getBHTree().getSelectionPath();
 			if(currentRemoveScenarioSelection != null){
 				BHTreeNode removeScenarioNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 				if(removeScenarioNode.getUserObject() instanceof DTOScenario){
 					((BHTreeModel) bhmf.getBHTree().getModel()).removeNodeFromParent(removeScenarioNode);
 					((DTOScenario)((BHTreeNode)removeScenarioNode.getParent()).getUserObject()).removeChild((DTOPeriod) removeScenarioNode.getUserObject());
 				} else {
 					BHStatusBar.getInstance().setHint(BHTranslator.getInstance().translate("EisSelectScenario"), true);					
 				}
 			}
 			break;
 			
 		case BILANZGUVSHOW:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case BILANZGUVCREATE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case BILANZGUVIMPORT:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case BILANZGUVREMOVE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case OPTIONSCHANGE:
 			//TODO Tietze.Patrick
 			new BHOptionDialog();
 		
 			break;
 			
 		case HELPUSERHELP:
 			
 			log.debug("HELPUSERHELP gefeuert");
 			JFrame frame = new JFrame();
 			frame.setTitle(BHTranslator.getInstance().translate("MuserHelpDialog"));
 			frame.setSize(610,600);
 			frame.getContentPane().add(new BHHelpSystem("userhelp"));
 			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			frame.setResizable(false);
 			frame.setVisible(true);
 			break;
 			
 		case HELPMATHHELP:
 			System.out.println("HELPMATHHELP gefeuert");
 			JFrame mathframe = new JFrame();
 			//TODO Hartgecodeder String raus!! LZ
 			mathframe.setTitle("Business Horizon Mathematische Erläuterungen");
 			mathframe.setSize(610,600);
 			mathframe.getContentPane().add(new BHHelpSystem("mathhelp"));
 			mathframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			mathframe.setResizable(false);
 			mathframe.setVisible(true);
 			break;
 			
 		case HELPINFO:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case TOOLBAROPEN:
 			log.debug("handling TOOLBAROPEN event");
 			this.fileOpen();
 			break;
 			
 		case TOOLBARSAVE: 
 			log.debug("handling FILESAVE event");
 			this.fileSave();
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
 			//is a node selected?
 			if (currentSelection != null) {
 				//find out current selected node 
 				BHTreeNode currentNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 				
 				 //..and from data model
 				 if(currentNode.getUserObject() instanceof DTOProject){
 					 projectRepoManager.removeProject((DTOProject) currentNode.getUserObject());
 				
 				 }else if(currentNode.getUserObject() instanceof DTOScenario){
 					 ((DTOProject)((BHTreeNode)currentNode.getParent()).getUserObject()).removeChild((DTOScenario) currentNode.getUserObject());
 				
 				 }else if(currentNode.getUserObject() instanceof DTOPeriod){
 					 ((DTOScenario)((BHTreeNode)currentNode.getParent()).getUserObject()).removeChild((DTOPeriod) currentNode.getUserObject());
 				 }
 				 
 				//remove node from GUI...
 				 ((BHTreeModel) bhmf.getBHTree().getModel()).removeNodeFromParent(currentNode);
 			}
 			
 			
 			
 			break;
 			
 		default:
 			//TODO implementieren?
 			break;
 		}
 	}
 	
 	private void fileNew() {
 		projectRepoManager.clearProjectList();
 		pC.setupTree(bhmf, projectRepoManager);
 		PlatformController.preferences.remove("path");
 		ProjectRepositoryManager.setChanged(false);
 		bhmf.resetTitle();
 	}
 	
 	private void fileOpen() {
 		// create a open-dialog
 		int returnVal = bhmf.getChooser().showOpenDialog(bhmf);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			log.debug("You chose to open this file: "
 					+ bhmf.getChooser().getSelectedFile().getName());
 			
 			// create a PlatformPersistence instance incl. filepath
 			PlatformPersistence myOpener = new PlatformPersistence(bhmf.getChooser().getSelectedFile(),projectRepoManager);
 			
 			// open already provided file
 			ArrayList<DTOProject> projectList = myOpener.openFile();
 			
 			// replace ProjectRepository
 			projectRepoManager.replaceProjectList(projectList);
 			
 			// TODO rebuild Tree
 			pC.setupTree(bhmf, projectRepoManager);
 			
 			// Save path to preferences
 			PlatformController.preferences.put("path", bhmf.getChooser().getSelectedFile().toString());
 			
 			// Refresh title
 			bhmf.resetTitle();
 			
 			// Set isChanged
 			ProjectRepositoryManager.setChanged(false);
 			
 			log.debug("file " + bhmf.getChooser().getSelectedFile() + " successfully opened");
 		}
 	}
 	
 	private void fileSave() {
 		if (PlatformController.preferences.get("path","").equals("")) {
 			this.fileSaveAs();
 		} else {
 			File path = new File( PlatformController.preferences.get("path","") );
 			
 			// create a PlatformPersistence instance incl. filepath
 			PlatformPersistence mySaver = new PlatformPersistence(path,projectRepoManager);
 			
 			// perform save
 			mySaver.saveFile(projectRepoManager.getRepositoryList());
 			
 			// Set isChanged
 			ProjectRepositoryManager.setChanged(false);
 			
 			// Refresh title
 			bhmf.resetTitle();
 			
 			log.debug("ProjectRepository saved to " + path);
 		}
 	}
 	
 	private void fileSaveAs() {
 		// create save dialog
 		int returnSave = bhmf.getChooser().showSaveDialog(bhmf);
 		if (returnSave == JFileChooser.APPROVE_OPTION) {
 			log.debug("You chose to save this file: "
 					+ bhmf.getChooser().getSelectedFile().getName());
 			
 			// create a PlatformPersistence instance incl. filepath
 			PlatformPersistence mySaver = new PlatformPersistence(bhmf.getChooser().getSelectedFile(),projectRepoManager);
 			
 			// perform save
 			mySaver.saveFile(projectRepoManager.getRepositoryList());
 			
 			// Save path to preferences
 			PlatformController.preferences.put("path", bhmf.getChooser().getSelectedFile().toString());
 		
 			// Set isChanged
 			ProjectRepositoryManager.setChanged(false);
 			
 			// Refresh title
 			bhmf.resetTitle();
 			
 			log.debug("ProjectRepository saved to " + bhmf.getChooser().getSelectedFile());
 
 		}
 	}
 	
 	private void createProject(){
 		//Create new project
 		DTOProject newProject = new DTOProject();
 		//TODO hardgecodeder String raus! AS
 		newProject.put(DTOProject.Key.NAME, new StringValue("neues Projekt"));
 		//add it to DTO-Repository
 		projectRepoManager.addProject(newProject);
 		
 		//and create a Node for tree on gui
 		BHTreeNode newProjectNode = bhmf.getBHTree().addProjectNode(newProject, bhmf);
 		
 		//last steps: unfold tree to new element, set focus and start editing
 		bhmf.getBHTree().scrollPathToVisible(new TreePath(newProjectNode.getPath()));
 		bhmf.getBHTree().startEditingAtPath(new TreePath(newProjectNode.getPath()));
 		
 	}
 	private void createScenario(){
 		//If a path is selected...
 		if(bhmf.getBHTree().getSelectionPath() != null){
 			//...create new scenario
 			DTOScenario newScenario = new DTOScenario();
 			//TODO hardgecodeder String raus! AS
 			newScenario.put(DTOScenario.Key.NAME, new StringValue("neues Scenario"));
 			
 			//...add it to DTO-Repository
 			((DTOProject)((BHTreeNode)bhmf.getBHTree().getSelectionPath().getPathComponent(1)).getUserObject()).addChild(newScenario);
 			
 			//...and insert it into GUI-Tree
 			BHTreeNode newScenarioNode = bhmf.getBHTree().addScenarioNode(newScenario, bhmf);
 			
 			//last steps: unfold tree to new element, set focus and start editing
 			bhmf.getBHTree().scrollPathToVisible(new TreePath(newScenarioNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(new TreePath(newScenarioNode.getPath()));
 		}else {
 			BHStatusBar.getInstance().setHint(BHTranslator.getInstance().translate("EisSelectPeriod"), true);
 		}
 			
 		
 	}
 	
 	
 	private void createPeriod(){
 		//If a scenario or a period is selected...
 		if(bhmf.getBHTree().getSelectionPath()!=null && bhmf.getBHTree().getSelectionPath().getPathCount()>2){
 			//TODO Schmalzhaf.Alexander at first: show selection screen for sort of Period
 			PluginManager.getInstance().loadAllServices(IPeriodController.class);
 			Object[] periodTypes = Services.getPeriodControllers().toArray();
 			
 			//TODO Thiele.Klaus dürfen "wir" die OptionPane benutzen?
 			
 			//TODO Schmalzhaf.Alexander hartgecodete Strings raus!
 			DisplayablePluginWrapper<IPeriodController>  res = (DisplayablePluginWrapper<IPeriodController> )
 			JOptionPane.showInputDialog(
                     bhmf,
                     "Bitte gewünschten Periodentyp auswählen:",
                     "Periodentyp auswählen",
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     periodTypes,
                     null);
 			
 			if(res == null){
 				return;
 			}
 			
 			
 			
 			//...create new period
 			DTOPeriod newPeriod = new DTOPeriod();
 			//TODO hardgecodeder String raus! AS
 			newPeriod.put(DTOPeriod.Key.NAME, new StringValue("neue Periode"));
 			
 			//...add it to DTO-Repository
 			((DTOScenario)((BHTreeNode)bhmf.getBHTree().getSelectionPath().getPathComponent(2)).getUserObject()).addChild(newPeriod);
 			
 			//...and insert it into GUI-Tree
 			BHTreeNode newPeriodNode = bhmf.getBHTree().addPeriodNode(newPeriod, bhmf);
 			
 			//last steps: unfold tree to new element, set focus and start editing
 			bhmf.getBHTree().scrollPathToVisible(new TreePath(newPeriodNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(new TreePath(newPeriodNode.getPath()));
 			
 			
 			//create concrete Period
 			//TODO Schmalzhaf.Alexander es gibt kein Panel, das ich greifen kann vom bhmf -> muss eigenes erzeugen
 			JPanel panel = new JPanel();
 			bhmf.addContentForms(panel);
 			res.getPlugin().editDTO(newPeriod,panel );
 		}
 	}
 	
 	
 	private void duplicateProject(){
 		TreePath currentDuplicateProjectSelection = bhmf.getBHTree().getSelectionPath();
 		if(currentDuplicateProjectSelection != null){
			//Zugriff auf markiertes Projekt
 			BHTreeNode duplicateProjectNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 			
 			//zu kopierendes Project in eigene Variable
 			DTOProject duplicateProject = (DTOProject)duplicateProjectNode.getUserObject();
 			
 			//neues DTOProject mit Referenz auf den Klon
 
 			DTOProject newProject = (DTOProject)duplicateProject.clone();
 			
 			BHTreeNode newProjectNode = bhmf.getBHTree().addProjectNode(newProject, bhmf);
 			
 			for(int x = 0; x < newProject.getChildren().size(); x++){
 				BHTreeNode newScenarioNode = bhmf.getBHTree().duplicateScenarioNode(newProject.getChildren().get(x), bhmf, newProjectNode);
 				
 				for(int y = 0; y < newProject.getChildren().get(x).getChildren().size(); y++){
 					newScenarioNode.add(bhmf.getBHTree().duplicatePeriodNode(newProject.getChildren().get(x).getChildren().get(y), bhmf, newScenarioNode));
 				}
 			}
 		} else {
 			BHStatusBar.getInstance().setHint(BHTranslator.getInstance().translate("EisSelectProject"), true);
 		}
 	}
 	private void duplicateScenario(){
 		TreePath currentDuplicateProjectSelection = bhmf.getBHTree().getSelectionPath();
 		if(currentDuplicateProjectSelection != null){
 			//Zugriff auf markiertes Projekt
 			BHTreeNode duplicateScenarioNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 			
 			//zu kopierendes Project in eigene Variable
 			DTOScenario duplicateScenario = (DTOScenario)duplicateScenarioNode.getUserObject();
 			
 			//neues DTOProject mit Referenz auf den Klon
 			DTOScenario newScenario = (DTOScenario)duplicateScenario.clone();
 			
 			BHTreeNode newScenarioNode = bhmf.getBHTree().addScenarioNode(newScenario, bhmf);
 			
				for(int y = 0; y < newScenario.getChildren().size(); y++){
					newScenarioNode.add(bhmf.getBHTree().duplicatePeriodNode(newScenario.getChildren().get(y), bhmf, newScenarioNode));
				}
 		} else {
 			BHStatusBar.getInstance().setHint(BHTranslator.getInstance().translate("EisSelectScenario"), true);
 		}
 	}
 }
