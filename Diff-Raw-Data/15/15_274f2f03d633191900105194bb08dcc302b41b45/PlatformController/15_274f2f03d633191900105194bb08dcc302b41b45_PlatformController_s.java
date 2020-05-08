 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber 
  *
  * Copyright 2010: Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh.platform;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ServiceLoader;
 import java.util.prefs.Preferences;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IBranchSpecificCalculator;
 import org.bh.data.DTO;
 import org.bh.data.DTOAccessException;
 import org.bh.data.DTOBranch;
 import org.bh.data.DTOBusinessData;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.data.types.IValue;
 import org.bh.data.types.StringValue;
 import org.bh.gui.IBHAction;
 import org.bh.gui.chart.BHChartFactory;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHMenuItem;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.importexport.BHDataExchangeDialog;
 import org.bh.gui.swing.tree.BHTreeNode;
 import org.bh.gui.swing.tree.BHTreeSelectionListener;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.formula.IFormulaFactory;
 
 /**
  * The Platform Controller handles a) start up of the application b) main
  * application flow c) all events which are fired by platform controls (e.g.
  * toolbar-button klicks or menu klicks)
  * 
  * 
  * 
  * @author Alexander Schmalzhaf
  * @author Patrick Tietze
  * 
  * @version 0.1 2009/12/22 Alexander Schmalzhaf
  */
 
 public class PlatformController {
 
 	/**
 	 * Instance for Singleton; PlugIns can get access to all parts of Platform
 	 * through that
 	 */
 	private static PlatformController singletonInstance;
 
 	BHMainFrame bhmf;
 	private ProjectRepositoryManager projectRepoManager = ProjectRepositoryManager
 			.getInstance();
 
 	private static DTOBusinessData businessDataDTO;
 	private static List<Double> goodnessBranches;
 
 	/**
 	 * Reference to a preference object which allows platform independent
 	 * 
 	 * @author Marcus Katzor
 	 */
 	public static Preferences preferences = Preferences
 			.userNodeForPackage(PlatformController.class);
 
 	/**
 	 * PlatformPersistenceManager Instance
 	 * 
 	 * @author Loeckelt.Michael
 	 */
 	public static PlatformPersistenceManager platformPersistenceManager;
 
 	/**
 	 * PlatformactionListener
 	 */
 	public PlatformActionListener pal;
 	
 	private DataChangedListener dcl;
 	
 	/**
 	 * Logging
 	 */
 	private static final Logger log = Logger
 			.getLogger(PlatformController.class);
 
 	public static PlatformController getInstance() {
 		if (singletonInstance == null) {
 			singletonInstance = new PlatformController();
 		}
 		return singletonInstance;
 	}
 
 	private PlatformController() {
 		
 		/*------------------------------------
 		 * Pre initialization of formula & charts
 		 * -----------------------------------
 		 */
 		//gains 400ms on first calculation (Core 2Duo T7500, 2GB Ram)
 		IFormulaFactory.instance.initialInit();
 		BHChartFactory.initialInit();
 		
 		/*------------------------------------
 		 * start mainFrame
 		 * -----------------------------------
 		 */
 		bhmf = new BHMainFrame();
 
 		// create PlatformUserDialog
 		PlatformUserDialog.init(bhmf);
 
 		/*------------------------------------
 		 * fill Project/Scenario/Period-Tree
 		 * -----------------------------------
 		 */
 		setupTree(bhmf, projectRepoManager);
 
 		/*------------------------------------
 		 * Add EventHandler to Platform-Items
 		 * -----------------------------------
 		 */
 		pal = new PlatformActionListener(bhmf, projectRepoManager, this);
 
 		// Add ActionListener to Toolbar-buttons
 		for (IBHAction item : BHButton.getPlatformItems()) {
 			item.addActionListener(pal);
 		}
 
 		// Add ActionListener to the menu-items
 		for (IBHAction item : BHMenuItem.getPlatformItems()) {
 			item.addActionListener(pal);
 		}
 
 		/*
 		 * Create a new Persistence instance
 		 * 
 		 * @author Loeckelt.Michael
 		 */
 		platformPersistenceManager = new PlatformPersistenceManager(bhmf,
 				projectRepoManager);
 
 		// Fire event.
 		Services.firePlatformEvent(new PlatformEvent(this,
 				Type.PLATFORM_LOADING_COMPLETED));
 
 		platformPersistenceManager.lastEditedFile();
 
 		// rebuild Tree
 		setupTree(bhmf, projectRepoManager);
 		bhmf.getBHTree().expandAll();
 
 	}
 
 	/*------------------------------------
 	 * Methods for Tree-Handling
 	 * -----------------------------------
 	 */
 	protected void setupTree(BHMainFrame bhmf,
 			ProjectRepositoryManager projectRepoManager) {
 
 		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
 				"BusinessHorizon");
 
 		List<DTOProject> repoList = projectRepoManager.getRepositoryList();
 
 		for (DTOProject project : repoList) {
 
 			// create project...
 			BHTreeNode projectNode = new BHTreeNode(project);
 
 			// ...and add scenarios...
 			BHTreeNode scenarioNode;
 			for (DTOScenario scenario : project.getChildren()) {
 				scenarioNode = new BHTreeNode(scenario);
 				projectNode.add(scenarioNode);
 
 				// if periods are available - add them!
 				BHTreeNode periodNode;
 				List<DTOPeriod> children = scenario.getChildren();
 				if (!scenario.isDeterministic()) {
 					children = new ArrayList<DTOPeriod>(children);
 					Collections.reverse(children);
 				}
 				for (DTOPeriod period : children) {
 					periodNode = new BHTreeNode(period);
 					scenarioNode.add(periodNode);
 				}
 			}
 
 			// in the end, add all to rootNode
 			rootNode.add(projectNode);
 		}
 
 		bhmf.getBHTree().setModel(new BHTreeModel(rootNode));
 
 		// avoid to sum number of listeners -- done in context with ticket #85
 		if (bhmf.getBHTree().getTreeSelectionListeners().length < 3) {
 			bhmf.getBHTree().addTreeSelectionListener(
 					new BHTreeSelectionListener(this, bhmf));
 		}
 		
 		
 		if(dcl == null){
 		    dcl = new DataChangedListener();
 		    Services.addPlatformListener(dcl);
 		}
 	}
 
 	public void addProject(DTOProject newProject) {
 		projectRepoManager.addProject(newProject);
 
 		// and create a Node for tree on gui
 		BHTreeNode newProjectNode = bhmf.getBHTree().addProject(newProject);
 
 		// last steps: unfold tree to new element, set focus and start editing
 		bhmf.getBHTree().scrollPathToVisible(
 				new TreePath(newProjectNode.getPath()));
 		bhmf.getBHTree().startEditingAtPath(
 				new TreePath(newProjectNode.getPath()));
 	}
 	
 	public BHDataExchangeDialog createBalanceSheetAndPLSExchangeDialog()
 	{
 		// Create data exchange dialog
 		return new BHDataExchangeDialog(
 				bhmf, true);
 		
 	}
 	
 
 	/*------------------------------------
 	 * Subclasses
 	 * -----------------------------------
 	 */
 
 	
 
 	/**
 	 * 
 	 * Listens to changed data and removes result panel / dashboard if necessary
 	 * 
 	 * <p>
 	 * This listener waits for platform events from DTOs with the type DATA_CHANGED and 
 	 * a) refreshes the UI
 	 * b) checks if it's necessary to empty the result panel / dashboard
 	 * 
 	 * @author Schmalzhaf.Alexander
 	 * @version 1.0, 04.01.2010
 	 * 
 	 */
 	class DataChangedListener implements IPlatformListener {
 
 		public void platformEvent(PlatformEvent e) {
 			if (e.getEventType() != PlatformEvent.Type.DATA_CHANGED)
 				return;
 
 			if (e.getSource() instanceof DTOProject
 					|| e.getSource() instanceof DTOScenario
 					|| e.getSource() instanceof DTOPeriod
 					|| e.getSource() instanceof IPeriodicalValuesDTO) {
 
 				for (BHTreeNode projectNode : bhmf.getBHTree()
 						.getProjectNodes()) {
 					if (((DTOProject) projectNode.getUserObject())
 							.isMeOrChild(e.getSource())) {
 						bhmf.getBHTree().updateUI();
 						break;
 					}
 				}
 			}
 
 			if (e.getSource() instanceof DTO<?>) {
 				// check if data has changed and 
 				// a) remove result panel from scenario
 				// b) dashboard from project 
 				//then...
 				ArrayList<BHTreeNode> scenarioNodes = bhmf.getBHTree()
 						.getScenarioNodes();
 				if (!scenarioNodes.isEmpty()) {
 					for (BHTreeNode scenarioNode : scenarioNodes) {
 						if (((DTOScenario) scenarioNode.getUserObject())
 								.isMeOrChild(e.getSource())) {
 							
 							
 							//throw away dashboard of corresponding project
 							((BHTreeNode)scenarioNode.getParent()).setResultPane(null);
 							
 							// throw away present screen, if scenario is on
 							// screen
 							TreePath tp = bhmf.getBHTree().getSelectionPath();
 							if (tp.getPathCount() == 3) {
 								bhmf.moveOutResultForm();
 							}
 							scenarioNode.setResultPane(null);
 						}
 					}
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Extends the DefaultTreeModel with a extended valueForPathChanged-Method that
 	 * helps loading data to the View
 	 * 
 	 * 
 	 * @author Schmalzhaf.Alexander
 	 * @version 1.0, 04.01.2010
 	 * 
 	 */
 	@SuppressWarnings("serial")
 	class BHTreeModel extends DefaultTreeModel {
 
 		public BHTreeModel(TreeNode root) {
 			super(root);
 		}
 
 		@Override
 		public void valueForPathChanged(TreePath path, Object newValue) {
 			BHTreeNode activeNode = (BHTreeNode) path.getLastPathComponent();
 			DTO<?> tempDTO = (DTO<?>) activeNode.getUserObject();
 
 			Object key = null;
 			if (tempDTO instanceof DTOProject) {
 				key = DTOProject.Key.NAME;
 			} else if (tempDTO instanceof DTOScenario) {
 				key = DTOScenario.Key.NAME;
 			} else if (tempDTO instanceof DTOPeriod) {
 				key = DTOPeriod.Key.NAME;
 			}
 
 			if (key != null) {
 				tempDTO.put(key, new StringValue(newValue.toString()));
 				if (activeNode.getController() != null) {
 					activeNode.getController().loadToView(key);
 				}
 			}
 		}
 	}
 
 	public BHMainFrame getMainFrame() {
 		return this.bhmf;
 	}
 
 	public IValue[][] prepareScenarioTableData(DTOScenario scenarioDto) {
 		List<DTOPeriod> periods = scenarioDto.getChildren();
 
 		IValue[][] periodData = new IValue[periods.size()][3];
 
 		// transform data
 		for (int i = 0; i < periods.size(); i++) {
 			DTOPeriod period = periods.get(i);
 
 			try {
 				periodData[i][0] = period.get(DTOPeriod.Key.NAME);
 			} catch (DTOAccessException dtoae) {
 				periodData[i][0] = new StringValue(" ");
 			}
 
 			if (!period.isValid(true))
 				continue;
 
 			try {
 				periodData[i][1] = period.getLiabilities();
 			} catch (DTOAccessException dtoae) {
 				log.error("Cannot get liabilities for period table", dtoae);
 			}
 			try {
 				periodData[i][2] = period.getFCF();
 			} catch (DTOAccessException dtoae) {
 				if (i > 0) {
 					log.error("Cannot get FCF for period table", dtoae);
 				}
 			}
 		}
 
 		return periodData;
 	}
 
 	/**
 	 * is called once while initialisation to store branches for calculation.
 	 * @param businessDataDTO
 	 */
 	public static void setBusinessDataDTO(DTOBusinessData businessDataDTO) {
 		PlatformController.businessDataDTO = businessDataDTO;
 		
 		calculateRatings();
 	}
 	
 	private static void calculateRatings(){
 		List<DTOBranch> branches = businessDataDTO.getChildren();
 		
 		DTOScenario scenario = new DTOScenario();
 		
		
 		//Load Calculator Plugin(s) - should be one
 		ServiceLoader<IBranchSpecificCalculator> calculators = PluginManager
 		.getInstance().getServices(IBranchSpecificCalculator.class);
 		// calculate branch specific values
 		
 		IBranchSpecificCalculator calculator = calculators.iterator().next();
 		
 		LinkedList<Double> ratings = new LinkedList<Double>();
 		
 		if(calculator != null){
 			for(DTOBranch branch: branches){
 				String key = branch.get(DTOBranch.Key.BRANCH_KEY_MAIN_CATEGORY).toString();
 				key += "." + branch.get(DTOBranch.Key.BRANCH_KEY_MID_CATEGORY).toString();
 				key += "." + branch.get(DTOBranch.Key.BRANCH_KEY_SUB_CATEGORY).toString();
 			
 				StringValue string = new StringValue(key);
 			
 				DTO.setThrowEvents(false);
 				
 				scenario.put(DTOScenario.Key.INDUSTRY, string);
 			
				
				// berechne Branch Specific Representative und dessen Güte
				calculator.calculateBSR(businessDataDTO, scenario);
				ratings.add(calculator.getRating());
 				
 //				log.debug(branchSpecificRep);
 				DTO.setThrowEvents(true);
 			}
 		}
 		
 		goodnessBranches = ratings;
 	}
 	
 	public static List<Double> getAllBSRRatings(){
 		return goodnessBranches;
 	}
 
 	/**
 	 * Get all branches to calculate branch specific representatives.
 	 * @return
 	 */
 	public static DTOBusinessData getBusinessDataDTO() {
 		return businessDataDTO;
 	}
 }
