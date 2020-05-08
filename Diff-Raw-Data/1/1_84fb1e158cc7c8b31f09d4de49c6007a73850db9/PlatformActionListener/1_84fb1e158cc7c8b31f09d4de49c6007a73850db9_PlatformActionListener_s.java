 package org.bh.platform;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.StringValue;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.gui.swing.IBHAction;
 import org.bh.platform.PlatformController.BHTreeModel;
 
 /**
  * The PlatformActionListener handles all actions that are fired by a button
  * etc. of the platform.
  */
 class PlatformActionListener implements ActionListener {
 
 	BHMainFrame bhmf;
 	ProjectRepositoryManager projectRepoManager;
 	
 	public PlatformActionListener(BHMainFrame bhmf, ProjectRepositoryManager projectRepoManager){
 		this.bhmf = bhmf;
 		this.projectRepoManager = projectRepoManager;
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent aEvent) {
 
 		// get actionKey of fired action
 		PlatformKey actionKey = ((IBHAction) aEvent.getSource()).getPlatformKey();
 		
 		//do right action...
 		switch (actionKey) {
 		
 		case FILENEW:
 			
 			break;
 		
 		// TODO Loeckelt.Michael  
 			
 		case FILEOPEN:
 			System.out.println("FILEOPEN gefeuert");
 			int returnVal = bhmf.getChooser().showOpenDialog(bhmf);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				System.out.println("You chose to open this file: "
 						+ bhmf.getChooser().getSelectedFile().getName());
 	
 			}
 			break;
 			
 		// TODO Loeckelt.Michael
 			
 		case FILESAVE:
 			System.out.println("bla");
 			break;
 			
 		// TODO Loeckelt.Michael
 			
 		case FILESAVEAS:
 			break;
 			
 		case FILEQUIT:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case PROJECTCREATE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case PROJECTRENAME:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case PROJECTDUPLICATE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		// TODO Katzor.Marcus
 		case PROJECTIMPORT:
 			
 			break;
 		
 		// TODO Katzor.Marcus
 			
 		case PROJECTEXPORT:
 			break;
 			
 		case PROJECTREMOVE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case SCENARIOCREATE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case SCENARIORENAME:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case SCENARIODUPLICATE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case SCENARIOMOVE:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case SCENARIOREMOVE:
 			//TODO Prüfen und ggf. implementieren!
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
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case HELPUSERHELP:
 			
 			System.out.println("HELPUSERHELP gefeuert");
 			JFrame frame = new JFrame();
 			frame.setTitle("Business Horizon Help");
 			frame.setSize(610,600);
 			frame.getContentPane().add(new BHHelpSystem());
 			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			frame.setVisible(true);
 			break;
 			
 		case HELPMATHHELP:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case HELPINFO:
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case TOOLBAROPEN: 
 			//TODO Prüfen und ggf. implementieren!
 			break;
 			
 		case TOOLBARSAVE: 
 			//TODO Prüfen und ggf. implementieren!
 			break; 
 			
 		case TOOLBARADDPRO:
 			//Create new project
 			DTOProject newProject = new DTOProject();
 			//TODO hardgecodeder String raus! AS
 			newProject.put(DTOProject.Key.NAME, new StringValue("neues Projekt"));
 			//add it to DTO-Repository
 			projectRepoManager.addProject(newProject);
 			//and create a Node for tree on gui
 			BHTreeNode newProjectNode = new BHTreeNode(newProject);
 			((DefaultTreeModel)bhmf.getBHTree().getModel()).insertNodeInto(
 					newProjectNode, 
 					(DefaultMutableTreeNode)bhmf.getBHTree().getModel().getRoot(), 
 					((DefaultMutableTreeNode)bhmf.getBHTree().getModel().getRoot()).getChildCount()
 			);
 			
 			//last steps: unfold tree to new element, set focus and start editing
 			bhmf.getBHTree().scrollPathToVisible(new TreePath(newProjectNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(new TreePath(newProjectNode.getPath()));
 			
 			break;
 			
 		case TOOLBARADDS: 
 			//If a path is selected...
 			if(bhmf.getBHTree().getSelectionPath() != null){
 				//...create new scenario
 				DTOScenario newScenario = new DTOScenario();
 				//TODO hardgecodeder String raus! AS
 				newScenario.put(DTOScenario.Key.NAME, new StringValue("neues Scenario"));
 				
 				//...add it to DTO-Repository
 				((DTOProject)((BHTreeNode)bhmf.getBHTree().getSelectionPath().getPathComponent(1)).getUserObject()).addChild(newScenario);
 				
 				//...and insert it into GUI-Tree
 				BHTreeNode newScenarioNode = new BHTreeNode(newScenario);
 				((BHTreeModel)bhmf.getBHTree().getModel()).insertNodeInto(
 						newScenarioNode, 
 						(BHTreeNode)(bhmf.getBHTree().getSelectionPath().getPathComponent(1)), 
 						((BHTreeNode) bhmf.getBHTree().getSelectionPath().getPathComponent(1)).getChildCount()
 				);
 				
 				//last steps: unfold tree to new element, set focus and start editing
 				bhmf.getBHTree().scrollPathToVisible(new TreePath(newScenarioNode.getPath()));
 				bhmf.getBHTree().startEditingAtPath(new TreePath(newScenarioNode.getPath()));
 			}
 			
 							
 			break;
 		
 		case TOOLBARADDPER:
 			//If a scenario or a period is selected...
 			if(bhmf.getBHTree().getSelectionPath()!=null && bhmf.getBHTree().getSelectionPath().getPathCount()>2){
 				//...create new period
 				DTOPeriod newPeriod = new DTOPeriod();
 				//TODO hardgecodeder String raus! AS
 				newPeriod.put(DTOPeriod.Key.NAME, new StringValue("neue Periode"));
 				
 				//...add it to DTO-Repository
 				((DTOScenario)((BHTreeNode)bhmf.getBHTree().getSelectionPath().getPathComponent(2)).getUserObject()).addChild(newPeriod);
 				
 				//...and insert it into GUI-Tree
 				BHTreeNode newPeriodNode = new BHTreeNode(newPeriod);
 				((BHTreeModel)bhmf.getBHTree().getModel()).insertNodeInto(
 						newPeriodNode,
 						(BHTreeNode)(bhmf.getBHTree().getSelectionPath().getPathComponent(2)), 
 						((BHTreeNode) bhmf.getBHTree().getSelectionPath().getPathComponent(2)).getChildCount()
 				);
 				
 				//last steps: unfold tree to new element, set focus and start editing
 				bhmf.getBHTree().scrollPathToVisible(new TreePath(newPeriodNode.getPath()));
 				bhmf.getBHTree().startEditingAtPath(new TreePath(newPeriodNode.getPath()));
 			}
 			
 			
 			break;
 			
 		case TOOLBARREMOVE:
 			
 			TreePath currentSelection = bhmf.getBHTree().getSelectionPath();
 			//is a node selected?
 			if (currentSelection != null) {
 				//remove node from GUI...
 				 BHTreeNode currentNode = (BHTreeNode)bhmf.getBHTree().getSelectionPath().getLastPathComponent();
 				 ((BHTreeModel) bhmf.getBHTree().getModel()).removeNodeFromParent(currentNode);
 				 
 				 //..and from data model
 				 if(currentNode.getUserObject() instanceof DTOProject){
 					 projectRepoManager.removeProject((DTOProject) currentNode.getUserObject());
 				
 				 }else if(currentNode.getUserObject() instanceof DTOScenario){
 					 ((DTOProject)((BHTreeNode)currentNode.getParent()).getUserObject()).removeChild((DTOScenario) currentNode.getUserObject());
 				
 				 }else if(currentNode.getUserObject() instanceof DTOPeriod){
 					 ((DTOScenario)((BHTreeNode)currentNode.getParent()).getUserObject()).removeChild((DTOPeriod) currentNode.getUserObject());
 					 
 				 }
 			}
 			
 			
 			
 			break;
 			
 		default:
 			System.out.println("Was anderes, und zwar: "+actionKey.getActionKey());
 			break;
 		}
 	}
 
 }
