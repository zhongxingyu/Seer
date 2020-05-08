 package org.geworkbench.builtin.projects;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.bison.util.colorcontext.ColorContext;
 import org.geworkbench.builtin.projects.SaveFileFilterFactory.CustomFileFilter;
 import org.geworkbench.builtin.projects.WorkspaceHandler.OpenTask;
 import org.geworkbench.builtin.projects.history.HistoryPanel;
 import org.geworkbench.engine.config.GUIFramework;
 import org.geworkbench.engine.config.MenuListener;
 import org.geworkbench.engine.config.UILauncher;
 import org.geworkbench.engine.config.VisualPlugin;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.management.Publish;
 import org.geworkbench.engine.management.Subscribe;
 import org.geworkbench.engine.preferences.GlobalPreferences;
 import org.geworkbench.engine.properties.PropertiesManager;
 import org.geworkbench.engine.skin.Skin;
 import org.geworkbench.events.CaArrayQueryEvent;
 import org.geworkbench.events.CaArrayRequestEvent;
 import org.geworkbench.events.HistoryEvent;
 import org.geworkbench.events.ImageSnapshotEvent;
 import org.geworkbench.events.PendingNodeCancelledEvent;
 import org.geworkbench.events.PendingNodeLoadedFromWorkspaceEvent;
 import org.geworkbench.events.ProjectEvent;
 import org.geworkbench.events.ProjectNodePostCompletedEvent;
 import org.geworkbench.events.ProjectNodeRemovedEvent;
 import org.geworkbench.events.ProjectNodeRenamedEvent;
 import org.geworkbench.util.FilePathnameUtils;
 import org.geworkbench.util.ProgressDialog;
 import org.geworkbench.util.ProgressItem;
 import org.geworkbench.util.SaveImage;
 import org.geworkbench.util.Util;
 import org.ginkgo.labs.ws.GridEndpointReferenceType;
 
 /**
  *
  * Description: Project Panel of geWorkbench is a key controlling element.
  * </p>
  * <p>
  * Copyright: Copyright (c) 2002
  * </p>
  * <p>
  * Company: First Genetic Trust Inc.
  * </p>
  *
  * @author First Genetic Trust
  * @version $Id$
  */
 @SuppressWarnings("unchecked")
 public class ProjectPanel implements VisualPlugin, MenuListener {
 
 	static Log log = LogFactory.getLog(ProjectPanel.class);
 
 	private static final String WORKSPACE_DIR = "workspaceDir";
 
 	private LoadDataDialog loadData = new LoadDataDialog();
 
 	private ProjectSelection selection = new ProjectSelection();
 
 	private HashMap<GridEndpointReferenceType, PendingTreeNode> eprPendingNodeMap = new HashMap<GridEndpointReferenceType, PendingTreeNode>();
 
 	/**
 	 * XQ uses dataSetMenu to save/modify the new generated/old Fasta file
 	 * dataSetSubMenu to save sequence alignment result.
 	 */
 	private JPopupMenu dataSetMenu = new JPopupMenu();
 
 	private JPopupMenu dataSetSubMenu = new JPopupMenu();
 
 	private JPopupMenu pendingMenu = new JPopupMenu();
 
 	private JProgressBar progressBar = new JProgressBar();
 
 	private JMenuItem jRenameSubItem = new JMenuItem("Rename");
 
 	private JMenuItem jEditItem = new JMenuItem("View in Editor");
 
 	private JMenuItem jViewAnnotations = new JMenuItem("View Annotations");
 
 	private JMenuItem jSaveMenuItem = new JMenuItem("Save");
 
 	private JMenuItem jRenameMenuItem = new JMenuItem("Rename");
 
 	/*
 	 * enforce ProjectPanel to be singleton: 'regular' method of making
 	 * constructor private is not applicable because of the cglib
 	 * parsing/loading process
 	 */
 	private static ProjectPanel INSTANCE = null;
 
 	public static ProjectPanel getInstance() {
 		if (INSTANCE != null)
 			return INSTANCE;
 		else
 			try {
 				return new ProjectPanel();
 			} catch (Exception e) { // exception only for INSTANCE is not null
 				return INSTANCE;
 			}
 	}
 
 	/**
 	 * Constructor. Initialize GUI and selection variables
 	 *
 	 * @throws Exception
 	 */
 	public ProjectPanel() throws Exception {
 		// singleton: this constructor should never be called the second time.
 		if (INSTANCE != null)
 			throw new Exception(
 					"Second instance of ProjectPanle cannot be created.");
 
 		// Initializes Random number generator to generate unique ID's
 		RandomNumberGenerator.setSeed(System.currentTimeMillis());
 
 		initializeMenuListeners();
 		initializeMainPanel();
 		initializePopupMenuItems();
 
 		// Checks if a default workspace exists and loads it
 		File defaultWS = new File("./default.wsp");
 		if (defaultWS.exists()) {
 			WorkspaceHandler ws = new WorkspaceHandler();
 			ProgressDialog pdnonmodal = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);
 			OpenTask openTask = ws.new OpenTask(ProgressItem.INDETERMINATE_TYPE, "Workspace is being loaded.", defaultWS.getName());
 			pdnonmodal.executeTask(openTask);
 			GUIFramework.getFrame().setTitle(
 					((Skin)GeawConfigObject.getGuiWindow()).getApplicationTitle() + " ["
 							+ defaultWS.getName() + "]");
 			
 			Enumeration<?> children = root.children();
 			while (children.hasMoreElements()) {
 				TreeNode node = (TreeNode) children.nextElement();
 				projectTree.expandPath(new TreePath(node));
 			}
 		}
 
 		initializeWorkspaceBehavior();
 		
 		INSTANCE = this;
 	}
 
 	private void initializePopupMenuItems() {
 		// root menu
 		jUploadWspItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				RWspHandler ws = new RWspHandler();
 				ws.uploadWsp();
 			}
 
 		});
 		
 		// dataset menu
 		jSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveNodeAsFile();
 			}
 		});
 
 		jRenameMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameDataset_actionPerformed(e);
 			}
 		});
 
 		JMenuItem jRemoveDatasetItem = new JMenuItem("Remove");
 		jRemoveDatasetItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 
 		jEditItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (selection.getSelectedNode() instanceof DataSetNode) {
 					viewInExternalEditor();
 				}
 			}
 		});
 		jViewAnnotations.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (selection.getSelectedNode() instanceof DataSetNode) {
 					viewAnnotationInExternalEditor();
 				}
 			}
 		});
 
 		dataSetMenu.add(jSaveMenuItem);
 		dataSetMenu.addSeparator();
 		dataSetMenu.add(jRenameMenuItem);
 		dataSetMenu.add(jRemoveDatasetItem);
 		dataSetMenu.add(jEditItem);
 		dataSetMenu.add(jViewAnnotations);
 
 		// dataset sub menu
 		jRenameSubItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameDataset_actionPerformed(e);
 			}
 		});
 		JMenuItem jRemoveSubItem = new JMenuItem("Remove");
 		jRemoveSubItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 		dataSetSubMenu.add(jRenameSubItem);
 		dataSetSubMenu.add(jRemoveSubItem);
 
 		// pending menu
 		JMenuItem jRemovePendingItem = new JMenuItem("Remove");
 		jRemovePendingItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 		pendingMenu.add(jRemovePendingItem);
 
 		// project menu
 		JMenuItem jRemoveProjectItem = new JMenuItem("Remove Project");
 		jRemoveProjectItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 
 		jProjectMenu.addSeparator();
 		jProjectMenu.add(jRemoveProjectItem);
 	}
 
 	private void viewInExternalEditor() {
 
 		DSDataSet<? extends DSBioObject> ds = selection.getDataSet();
 
 		GlobalPreferences prefs = GlobalPreferences.getInstance();
 		String editor = prefs.getTextEditor();
 		if (editor == null) {
 			log.info("No editor configured.");
 			JOptionPane
 			.showMessageDialog(
 					null,
 					"No editor configured.",
 					"Unable to Edit",
 					JOptionPane.INFORMATION_MESSAGE);
 		} else {
 			if (ds.getFile() == null) {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"There is no local file for this data set.",
 								"Unable to Edit",
 								JOptionPane.INFORMATION_MESSAGE);
 			} else {
 				if (Util.isRunningOnAMac()) {
 					editor = "Open";
 				}
 
 				String[] args = { editor,
 						ds.getFile().getAbsolutePath() };
 				try {
 					Runtime.getRuntime().exec(args);
 				} catch (IOException e1) {
 					log.info("Error opening editor:");
 					JOptionPane
 					.showMessageDialog(
 							null,
 							"IOException in opening editor: "+e1.getMessage(),
 							"Unable to Edit",
 							JOptionPane.INFORMATION_MESSAGE);
 					e1.printStackTrace();
 				} catch (SecurityException se) {
 					JOptionPane
 					.showMessageDialog(
 							null,
 							"SecurityException in opening editor: "+se.getMessage(),
 							"Unable to Edit",
 							JOptionPane.INFORMATION_MESSAGE);
 				} catch (Exception ee) {
 					JOptionPane
 					.showMessageDialog(
 							null,
 							"Other Exception in opening editor: "+ee.getMessage(),
 							"Unable to Edit",
 							JOptionPane.INFORMATION_MESSAGE);
 				}
 			}
 		}
 	}
 	
 	private void viewAnnotationInExternalEditor() {
 		DSDataSet<? extends DSBioObject> ds = selection.getDataSet();
 		if (!(ds instanceof CSMicroarraySet)) {
 			return;
 		}
 		CSMicroarraySet microarraySet = (CSMicroarraySet) ds;
 		String annotationFileName = microarraySet
 				.getAnnotationFileName();
 		if (annotationFileName == null) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"There are no annotations loaded for this dataset.",
 							"Unable to View",
 							JOptionPane.INFORMATION_MESSAGE);
 			return;
 		}
 
 		GlobalPreferences prefs = GlobalPreferences.getInstance();
 		String editor = prefs.getTextEditor();
 		if (editor == null) {
 			log.info("No editor configured.");
 		} else {
 			if (Util.isRunningOnAMac()) {
 				editor = "Open";
 			}
 			String[] args = { editor, annotationFileName };
 			try {
 				Runtime.getRuntime().exec(args);
 			} catch (IOException e1) {
 				log.info("Error opening editor:");
 				e1.printStackTrace();
 			}
 		}
 	}
 	
 	private void restorePendingNode(DSDataSet<?> dataset,
 			final Collection<GridEndpointReferenceType> pendingGridEprs) {
 		GridEndpointReferenceType pendingGridEpr = (GridEndpointReferenceType) dataset
 				.getObject(GridEndpointReferenceType.class);
 		String history = dataset.getDescriptions()[0];
 		addPendingNode(pendingGridEpr,
 				dataset.getLabel(), history, true);
 		pendingGridEprs.add(pendingGridEpr);
 	}
 	
 	void populateFromSaveTree(SaveTree saveTree) {
 		java.util.List<DataSetSaveNode> projects = saveTree.getNodes();
 		ProjectTreeNode selectedNode = null;
 		Collection<GridEndpointReferenceType> pendingGridEprs = new HashSet<GridEndpointReferenceType>();
 		for (DataSetSaveNode project : projects) {
 			ProjectNode projectNode = new ProjectNode(project.getName());
 			projectNode.setDescription(project.getDescription());
 			addToProject(projectNode, true);
 			selection.setNodeSelection(projectNode);
 			/* add data sets next */
 			java.util.List<DataSetSaveNode> dataSets = project.getChildren();
 			for (DataSetSaveNode dataNode : dataSets) {
 				setComponents(dataNode);
 				DSDataSet<? extends DSBioObject> dataSet = dataNode.getDataSet();
 				dataSet.setExperimentInformation(dataNode.getDescription());
 				/* pending node */
 				if (dataSet instanceof PendingTreeNode.PendingNode) {
 					restorePendingNode(dataSet, pendingGridEprs);
 				} else { /* real node */
 					addDataSetNode(dataSet, true);
 				}
 				if (dataSet == saveTree.getSelected()) {
 					selectedNode = selection.getSelectedNode();
 				}
 				/* add ancillary data sets */
 				java.util.List<DataSetSaveNode> ancSets = dataNode
 						.getChildren();
 				for (DataSetSaveNode ancNode : ancSets) {
 					setComponents(ancNode);
 
 					/* pending node */
 					if (ancNode.getDataSet() instanceof PendingTreeNode.PendingNode) {
 						restorePendingNode(ancNode
 								.getDataSet(), pendingGridEprs);
 					} else {
 						DSAncillaryDataSet<? extends DSBioObject> ancSet = null;
 
 						if (ancNode.getDataSet() instanceof ImageData) {
 							ancSet = (ImageData) ancNode.getDataSet();
 						} else {
 							ancSet = (DSAncillaryDataSet<? extends DSBioObject>) ancNode.getDataSet();
 						}
 
 						ancSet.setExperimentInformation(ancNode
 								.getDescription());
 						addDataSetSubNode(ancSet);
 						if (ancSet == saveTree.getSelected()) {
 							selectedNode = selection.getSelectedNode();
 						}
 						selection.setNodeSelection((ProjectTreeNode) selection
 								.getSelectedDataSetSubNode().getParent());
 					}
 				}
 				selection.setNodeSelection((ProjectTreeNode) selection
 						.getSelectedDataSetNode().getParent());
 			}
 			publishPendingNodeLoadedFromWorkspaceEvent(new PendingNodeLoadedFromWorkspaceEvent(
 					pendingGridEprs));
 		}
 		// Set final selection
 		if (selectedNode != null) {
 			projectTree.scrollPathToVisible(new TreePath(selectedNode));
 			// serialize("default.ws");
 			projectTree.setSelectionPath(new TreePath(selectedNode.getPath()));
 			selection.setNodeSelection(selectedNode);
 		}
 	}
 
 	private void setComponents(DataSetSaveNode saveNode) {
 		Skin skin = (Skin) GeawConfigObject.getGuiWindow();
 		skin.setVisualLastSelected(saveNode.getDataSet(), saveNode
 				.getVisualSelected());
 		skin.setCommandLastSelected(saveNode.getDataSet(), saveNode
 				.getCommandSelected());
 		skin.setSelectionLastSelected(saveNode.getDataSet(), saveNode
 				.getSelectionSelected());
 	}
 
 	private void saveNodeAsFile() {
 		ProjectTreeNode selectedNode = selection.getSelectedNode();
 		if (selectedNode == null) {
 			return;
 		}
 
 		// case 1: ImageNode
 		if (selectedNode instanceof ImageNode) {
 			Image currentImage = ((ImageNode) selectedNode).image.getImage();
 			SaveImage si = new SaveImage(currentImage);
 			si.save();
 			return;
 		}
 
 		// case 2: CSTTestResultSet
 		if (selectedNode instanceof DataSetSubNode) {
 			DataSetSubNode dataSetSubNode = (DataSetSubNode) selectedNode;
 			if (dataSetSubNode._aDataSet instanceof CSTTestResultSet) {
 				CSTTestResultSet<? extends DSGeneMarker> tTestResultSet = (CSTTestResultSet<? extends DSGeneMarker>) dataSetSubNode._aDataSet;
 				tTestResultSet.saveDataToCSVFile();
 				return;
 			}
 		}
 
 		// other cases
 		String dirPropertyKey = "";
 		DSDataSet<? extends DSBioObject> ds = null;
 		if (selectedNode instanceof DataSetNode) {
 			ds = ((DataSetNode) selectedNode).getDataset();
 			dirPropertyKey = "datanodeDir";
 		} else if (selectedNode instanceof DataSetSubNode) {
 			ds = ((DataSetSubNode) selectedNode)._aDataSet;
 			dirPropertyKey = "subnodeDir";
 		} else {
 			JOptionPane.showMessageDialog(null,
 					"This node contains no Dataset.", "Save Error",
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		// at this point ds is not null
 		File f = ds.getFile();
 		JFileChooser jFileChooser1 = new JFileChooser(f);
 		jFileChooser1.setSelectedFile(f);
 
 		PropertiesManager properties = PropertiesManager.getInstance();
 		if (f == null) {
 			try {
 				String dir = properties.getProperty(this.getClass(),
 						dirPropertyKey, jFileChooser1.getCurrentDirectory()
 								.getPath());
 				jFileChooser1.setCurrentDirectory(new File(dir));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		CustomFileFilter filter = SaveFileFilterFactory.createFilter(ds);
 		jFileChooser1.setFileFilter(filter);
 
 		if (JFileChooser.APPROVE_OPTION == jFileChooser1
 				.showSaveDialog(jSaveMenuItem)) {
 			String newFileName = jFileChooser1.getSelectedFile()
 					.getAbsolutePath();
 
 			if (f == null) {
 				try {
 					properties.setProperty(this.getClass(), dirPropertyKey,
 							jFileChooser1.getSelectedFile().getParent());
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 			}
 
 			if (!filter.accept(new File(newFileName))) {
 				newFileName += "." + filter.getExtension();
 			}
 
 			if (new File(newFileName).exists()) {
 				int o = JOptionPane.showConfirmDialog(null, "Replace the file",
 						"Replace the existing file?",
 						JOptionPane.YES_NO_CANCEL_OPTION);
 				if (o != JOptionPane.YES_OPTION) {
 					return;
 				}
 			}
 
 			try {
 				ds.writeToFile(newFileName);
 			} catch (RuntimeException e) {
 				JOptionPane.showMessageDialog(null, e.getMessage() + " "
 						+ ds.getClass().getName(), "Save Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	/**
 	 * Change the comment text
 	 *
 	 */
 	public void setCommentText(String newComments) {
 		ProjectTreeNode selectedNode = selection.getSelectedNode();
 		selectedNode.setDescription(newComments);
 	}
 
 	/**
 	 * Inserts a new data set as a new node in the project tree. The node is a
 	 * child of the currently selected project
 	 *
 	 * @param _dataSet
 	 */
 	public void addDataSetNode(DSDataSet<? extends DSBioObject> _dataSet, boolean select) {
 		// Retrieve the project node for this node
 		ProjectNode pNode = selection.getSelectedProjectNode();
 
 		if (pNode != null) {
 			// Inserts the new node and sets the menuNode and other variables to
 			// point to it
 			DataSetNode node = new DataSetNode(_dataSet);
 			node.setDescription(_dataSet.getExperimentInformation());
 			projectTreeModel.insertNodeInto(node, pNode, pNode.getChildCount());
 			HistoryEvent event = new HistoryEvent(_dataSet);
 
 			// add to history
 			if (_dataSet instanceof CSMicroarraySet) {
 				CSMicroarraySet microarraySet = (CSMicroarraySet) _dataSet;
 				String annotationFileName = microarraySet
 						.getAnnotationFileName();
 
 				String annotationFileNameString = "";
 				if (annotationFileName != null) {
 					int i = annotationFileName
 							.lastIndexOf(FilePathnameUtils.FILE_SEPARATOR);
 					if (i >= 0) {
 						annotationFileName = annotationFileName
 								.substring(i + 1);
 					}
 					annotationFileNameString = "Loaded annotation file:  " + annotationFileName + "\n";
 				} else{
 					annotationFileNameString = "Loaded annotation file:  None" + "\n";
 				}
 				
 				String oboInfo = "obo file location: "
 						+ OboSourcePreference.getInstance().getSourceLocation()
 						+ "\n";
 				GeneOntologyTree g = GeneOntologyTree.getInstance();
 				if (g != null) {
 					oboInfo += "obo version " + g.getVersion() + "; obo date "
 							+ g.getDate() + "\n";
 				}
 
 				String setName = _dataSet.getDataSetName();
 				String dataSetString = "Data file:  " + setName + "\n" ;
 
 				String datasetHistory = dataSetString + annotationFileNameString + oboInfo 
 					+ "_____________________" + "\n";
 				HistoryPanel.addToHistory(_dataSet, datasetHistory);
 			}
 			publishHistoryEvent(event);
 
 			if (select) {
 				// Make sure the user can see the lovely new node.
 				projectTree.scrollPathToVisible(new TreePath(node));
 				// serialize("default.ws");
 				projectTree.setSelectionPath(new TreePath(node.getPath()));
 				selection.setNodeSelection(node);
 			}
 		}
 		Skin skin = (Skin) GeawConfigObject.getGuiWindow();
 		skin.resetSelectorTabOrder();
 	}
 
 	/**
 	 * This method is used to trigger HistoryPanel to refresh.
 	 *
 	 * @param event
 	 * @return
 	 */
 	@Publish
 	public HistoryEvent publishHistoryEvent(HistoryEvent event) {
 		return event;
 	}
 
 	/**
 	 * Inserts a new pending node in the project tree. The node is a
 	 * child of the currently selected project
 	 *
 	 * @param _dataSet
 	 */
 	public void addPendingNode(GridEndpointReferenceType gridEpr,
 			String label, String history, boolean startNewThread) {
 		// get the parent node for this node
 		ProjectTreeNode pNode = selection.getSelectedNode();
 		if (pNode == null) {
 			// should never happen
 			log.error("parent node of the pending node to be added is null");
 			return;
 		}
 
 		/*
 		 * Inserts the new node and sets the menuNode and other variables to
 		 * point to it.
 		 */
 		PendingTreeNode node = new PendingTreeNode(label, history,
 				gridEpr);
 		projectTreeModel.insertNodeInto(node, pNode, pNode.getChildCount());
 		// Make sure the user can see the lovely new node.
 		projectTree.scrollPathToVisible(new TreePath(node));
 		projectTree.setSelectionPath(new TreePath(node.getPath()));
 		selection.setNodeSelection(node);
 		eprPendingNodeMap.put(gridEpr, node);
 	}
 
 	private void removeCanceledNode(GridEndpointReferenceType gridEpr) {
 		PendingTreeNode node = eprPendingNodeMap.get(gridEpr);
 		if (node != null) {
 			ProjectTreeNode parent = (ProjectTreeNode) node.getParent();
 			projectTreeModel.removeNodeFromParent(node);
 			// node.setUserObject("No Results");
 			// now nothing is selected, which is annoying, let's select it's
 			// parent
 			projectTree.setSelectionPath(new TreePath(parent.getPath()));
 			selection.setNodeSelection(parent);
 		}
 	}
 
 	/**
 	 *
 	 * @param pnode
 	 * @param parentData
 	 * @return
 	 */
 	private DataSetNode getMatchNode(ProjectTreeNode pnode, DSDataSet<? extends DSBioObject> parentData) {
 
 		DSDataSet<? extends DSBioObject> dNodeFile = null;
 		if ((pnode instanceof DataSetNode)) {
 			dNodeFile = ((DataSetNode) pnode).getDataset();
 
 		}
 		if ((dNodeFile != null && dNodeFile.hashCode() == parentData.hashCode())) {
 			return (DataSetNode) pnode;
 		} else if (pnode != null) {
 			Enumeration<?> children = pnode.children();
 			while (children.hasMoreElements()) {
 				Object obj = children.nextElement();
 				if (getMatchNode((ProjectTreeNode) obj, parentData) != null) {
 
 					return getMatchNode((ProjectTreeNode) obj, parentData);
 				}
 			}
 
 		}
 
 		return null;
 	}
 
 	/**
 	 * Inserts a new ancillary data set as a new node in the project tree. The
 	 * node is a child of the currently selected data set
 	 *
 	 * @param _ancDataSet
 	 */
 	public void addDataSetSubNode(DSAncillaryDataSet<? extends DSBioObject> _ancDataSet) {
 		DataSetNode dNode = selection.getSelectedDataSetNode();
 		DataSetNode matchedDNode = null;
 		DSDataSet<? extends DSBioObject> parentSet = _ancDataSet.getParentDataSet();
 		if (parentSet != null) {
 			if (dNode != null) {
 
 				DSDataSet<? extends DSBioObject> dNodeFile = dNode.getDataset();
 				if (dNodeFile.hashCode() == parentSet.hashCode()) {
 					_ancDataSet.setDataSetFile(dNode.getDataset().getFile());
 
 				} else {
 					// get the matched node in case the node selected changed.
 					matchedDNode = getMatchNode(root, parentSet);
 				}
 
 			} else {
 				matchedDNode = getMatchNode(root, parentSet);
 			}
 		}
 		if (matchedDNode != null) {
 			dNode = matchedDNode;
 		}
 		if (dNode == null) {
 			log.info("There is no node at project panel!");
 			return;
 		}
 
 		_ancDataSet.setDataSetFile(dNode.getDataset().getFile());
 		// Makes sure that we do not already have an exact instance of this
 		// ancillary file
 		Enumeration<?> children = dNode.children();
 		while (children.hasMoreElements()) {
 			Object obj = children.nextElement();
 			if (obj instanceof DataSetSubNode) {
 				DSAncillaryDataSet<? extends DSBioObject> ads = ((DataSetSubNode) obj)._aDataSet;
 				if (_ancDataSet.equals(ads)) {
 					return;
 				}
 			}
 		}
 
 		DataSetSubNode node = null;
 		if (_ancDataSet instanceof ImageData) {
 			node = new ImageNode(((ImageData) _ancDataSet).getImageIcon());
 		} else {
 			node = new DataSetSubNode(_ancDataSet);
 		}
 
 		String originalLabel = _ancDataSet.getLabel();
 		String newLabel = originalLabel;
 		String existingLabel = "";
 		boolean foundOne = false;
 		int count = 1;
 		while (true) {
 			foundOne = false;
 			children = dNode.children();
 			while (children.hasMoreElements()) {
 				Object obj = children.nextElement();
 				if (obj instanceof DataSetSubNode) {
 					existingLabel = ((DataSetSubNode) obj)._aDataSet.getLabel();
 					if (newLabel.equals(existingLabel)) {
 						foundOne = true;
 						break;
 					}
 				}
 			}
 
 			if (foundOne) {
 				newLabel = originalLabel + " (" + count++ + ")";
 			} else {
 				_ancDataSet.setLabel(newLabel);
 				break;
 			}
 		}
 
 		// Inserts the new node and sets the menuNode and other variables to
 		// point to it
 		node.setDescription(_ancDataSet.getExperimentInformation());
 		projectTreeModel.insertNodeInto(node, dNode, dNode.getChildCount());
 		// Make sure the user can see the lovely new node.
 		projectTree.scrollPathToVisible(new TreePath(node));
 		// serialize("default.ws");
 		projectTree.setSelectionPath(new TreePath(node.getPath()));
 		selection.setNodeSelection(node);
 	}
 
 	private void setSelection() {
 
 		TreePath path = projectTree.getSelectionPath();
 		if (path != null) {
 			path.getLastPathComponent();
 			selectedNode = selection.getSelectedNode();
 			ProjectTreeNode clickedNode = (ProjectTreeNode) path
 					.getLastPathComponent();
 			// Take action only if a new node is selected.
 			if (path != null && selectedNode != clickedNode) {
 				setNodeSelection(clickedNode);
 			}
 		}
 	}
 
 	private boolean isPathSelected(TreePath path) {
 		TreePath[] selectedPaths = projectTree.getSelectionPaths();
 		if (selectedPaths == null) {
 			return false;
 		}
 		for (int i = 0; i < selectedPaths.length; i++) {
 			TreePath selectedPath = selectedPaths[i];
 			if (path == selectedPath) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Mouse release event. Used to popup menus
 	 *
 	 * @param e
 	 */
 	private void jProjectTree_mouseReleased(MouseEvent e) {
 		TreePath path = projectTree.getPathForLocation(e.getX(), e.getY());
 		TreePath[] paths = projectTree.getSelectionPaths();
 		if (path != null) {
 			ProjectTreeNode mNode = (ProjectTreeNode) path
 					.getLastPathComponent();
 
 			if (e.isMetaDown() || e.getClickCount() >= 2) {
 
 				if (!isPathSelected(path)) {
 					// Force selection of this path
 					projectTree.setSelectionPath(path);
 					setSelection();
 
 				}
 				// Make the jPopupMenu visible relative to the current mouse
 				// position in the container.
 				if (paths != null && paths.length > 1) {
 					this.jNewProjectItem.setEnabled(false);
 					this.jSaveMenuItem.setEnabled(false);
 					this.jOpenRemotePDBItem.setEnabled(false);
 					this.jLoadMArrayItem.setEnabled(false);
 					this.jRenameMenuItem.setEnabled(false);
 					this.jRenameProjectItem.setEnabled(false);
 					this.jRenameSubItem.setEnabled(false);
 					this.jEditItem.setEnabled(false);
 					this.jViewAnnotations.setEnabled(false);
 				} else {
 					this.jNewProjectItem.setEnabled(true);
 					this.jSaveMenuItem.setEnabled(true);
 					this.jOpenRemotePDBItem.setEnabled(true);
 					this.jLoadMArrayItem.setEnabled(true);
 					this.jRenameMenuItem.setEnabled(true);
 					this.jRenameProjectItem.setEnabled(true);
 					this.jRenameSubItem.setEnabled(true);
 					this.jEditItem.setEnabled(true);
 					this.jViewAnnotations.setEnabled(true);
 
 					if ((RWspHandler.wspId > 0 && RWspHandler.dirty == false)
 					|| (RWspHandler.wspId == 0 && mNode == root && mNode.getChildCount() == 0))
 						jUploadWspItem.setEnabled(false);
 					else
 						jUploadWspItem.setEnabled(true);
 				}
 
 				if ((mNode == null) || (mNode == root)) {
 					jRootMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof ProjectNode) {
 					jProjectMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof DataSetNode) {
 					dataSetMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof DataSetSubNode) {
 					dataSetMenu.show(projectTree, e.getX(), e.getY());
 				} else if (mNode instanceof PendingTreeNode) {
 					pendingMenu.show(projectTree, e.getX(), e.getY());
 				}
 			} else
 				setSelection();
 
 			Skin skin = (Skin) GeawConfigObject.getGuiWindow();
 			skin.resetSelectorTabOrder();
 		}
 	}
 
 	/**
 	 * key release event.
 	 *
 	 * @param e
 	 */
 	private void jProjectTree_keyReleased(KeyEvent e) {
 		TreePath path = projectTree.getSelectionPath();
 		if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)
 				&& path != null) {
 
 			setSelection();
 
 		}
 	}
 
 	/* Publishers of ProjectNodeAddedEvent are recommended to directly call addProjectNode(...) */
 	@Subscribe
 	public void receive(org.geworkbench.events.ProjectNodeAddedEvent pnae,
 			Object source) {
 		DSDataSet<? extends DSBioObject> dataSet = pnae.getDataSet();
 		DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet = pnae.getAncillaryDataSet();
 		addProjectNode(dataSet, ancillaryDataSet);
 	}
 	
 	public void addProjectNode(DSDataSet<? extends DSBioObject> dataSet,
 			DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet) {
 		if (dataSet instanceof DSMicroarraySet) {
 			addColorContext((DSMicroarraySet<DSMicroarray>) dataSet);
 		}
 		if (dataSet != null) {
 			addDataSetNode(dataSet, true);
 		} else if (ancillaryDataSet != null) {
 			addDataSetSubNode(ancillaryDataSet);
 		}
 	}
 
 	public void processNodeCompleted(GridEndpointReferenceType gridEpr,
 			DSAncillaryDataSet<? extends DSBioObject> ancillaryDataSet) {
 		if (ancillaryDataSet == null) {
 			// no result from grid server? let's delete this node!
 			removeCanceledNode(gridEpr);
 			return;
 		}
 
 		PendingTreeNode node = eprPendingNodeMap.get(gridEpr);
 		if (node == null) {
 			log.debug("pending node is null"); // should never happen
 			return;
 		}
 
		String history = node.getDSDataSet().getDescriptions()[0];
 		boolean pendingNodeFocused = false;
 
 		TreePath pathNow = projectTree.getSelectionPath();
 		Object lastSelected = projectTree.getLastSelectedPathComponent();
 		if (lastSelected instanceof PendingTreeNode) {
 			if (((PendingTreeNode) lastSelected).getGridEpr() == gridEpr)
 				pendingNodeFocused = true;
 		}
 		ProjectTreeNode parent = (ProjectTreeNode) node.getParent();
 		int index = parent.getIndex(node);
 		projectTreeModel.removeNodeFromParent(node);
 
 		if (!(parent instanceof DataSetNode)) {
 			log.error("parent of the pending node is null"); // should never
 																// happen
 		}
 
 		@SuppressWarnings("rawtypes")
 		DSDataSet dataset = ((DataSetNode) parent).getDataset();
 		((CSAncillaryDataSet<? extends DSBioObject>) ancillaryDataSet)
 				.setParent(dataset);
 		DataSetSubNode newNode = new DataSetSubNode(ancillaryDataSet);
 		projectTreeModel.insertNodeInto(newNode, parent, index);
 		eprPendingNodeMap.remove(gridEpr);
 
 		HistoryPanel.addToHistory(ancillaryDataSet, history);
 
 		// Make sure the user can see the lovely new node.
 		projectTree.scrollPathToVisible(new TreePath(newNode));
 		projectTree.setSelectionPath(new TreePath(newNode.getPath()));
 		projectTree.setSelectionPath(pathNow);
 		// If the pending node is focused,
 		// we assume the user is interested in this result.
 		// we visually set the focus to the new node,
 		// and select the node so user can see the result)
 		if (pendingNodeFocused) {
 			projectTree.setSelectionPath(new TreePath(newNode.getPath()));
 			selection.setNodeSelection(newNode);
 		}
 		// PS: this post processing event has to follow the node
 		// selection. otherwise it might affect wrong node.
 		// ex: significance result set will add a significant markers in
 		// the panel for wrong node.
 		publishPostProcessingEvent(new ProjectNodePostCompletedEvent(
 				ancillaryDataSet.getDataSetName(), gridEpr, ancillaryDataSet,
 				parent));
 	}
 
 	/**
 	 * Action listener handling user requests for opening a file containing
 	 * microarray set data.
 	 *
 	 * @param e
 	 */
 	private void jLoadMArrayItem_actionPerformed(ActionEvent e) {
 		// Proceed only if there is a single node selected and that node
 		// is a project node.
 		if (projectTree.getSelectionCount() != 1
 				|| !(projectTree.getSelectionPath().getLastPathComponent() instanceof ProjectNode)) {
 			JOptionPane.showMessageDialog(null, "Select a project node.",
 					"Open File Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		String dir = LoadDataDialog.getLastDataDirectory();
 		String format = LoadDataDialog.getLastDataFormat();
 		loadData.setDirectory(dir);
 		loadData.setFormat(format);
 		// setupInputFormats() is called at every invocation of the "Open File"
 		// dialog. This guarantees that any dynamically loaded file format
 		// plugins
 		// will be taken into account.
 		loadData.setupInputFormats();
 		loadData.validate();
 		loadData.checkCaArraySupportingClasses();
 		loadData.setVisible(true);
 	}
 
 	/**
 	 * Action listener handling user requests for opening a pdb file from RCSB
 	 * Protein Data Bank.
 	 *
 	 * @param e
 	 */
 	private void jOpenRemotePDBItem_actionPerformed(ActionEvent e) {
 		// Proceed only if there is a single node selected and that node
 		// is a project node.
 		if (projectTree.getSelectionCount() != 1
 				|| !(projectTree.getSelectionPath().getLastPathComponent() instanceof ProjectNode)) {
 			JOptionPane.showMessageDialog(null, "Select a project node.",
 					"Open File Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		PDBDialog pd = new PDBDialog();
 		pd.create();
 	}
 
 	/**
 	 * Action listener handling user requests to merge 2 or more microarray sets
 	 * into 1.
 	 *
 	 * @param e
 	 *            <code>ActionEvent</code>
 	 */
 	private void jMergeDatasets_actionPerformed(ActionEvent e) {
 		TreePath[] selections;
 
 		MutableTreeNode node = null;
 		Object parentProject = null;
 		TreePath sibling = null;
 		int count = projectTree.getSelectionCount();
 		int i;
 		// Obtain the selected project tree nodes.
 		selections = projectTree.getSelectionPaths();
 		DSMicroarraySet<? extends DSMicroarray>[] sets = new DSMicroarraySet[count];
 		// Check that the user has designated only microarray set nodes and that
 		// all microarray sets are from the same project.
 		// Also, identify the node that will become the parent of the new,
 		// merged
 		// microarray set node.
 		for (i = 0; i < count; i++) {
 
 			node = (MutableTreeNode) selections[i].getLastPathComponent();
 			if (node instanceof DataSetNode) {
 				try {// Provide fix for bug 666, only merge
 					// Microarraydatasets.
 					sets[i] = (DSMicroarraySet<? extends DSMicroarray>) ((DataSetNode) node).getDataset();
 					if (sibling == null
 							|| sibling.getPathCount() > selections[i]
 									.getPathCount()) {
 						sibling = selections[i];
 					}
 					if (parentProject == null) {
 						parentProject = selections[i].getPath()[1];
 					} else if (parentProject != selections[i].getPath()[1]) {
 						JOptionPane.showMessageDialog(null,
 								"Select nodes from 1 project only.",
 								"Merge Error", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 				} catch (ClassCastException ex) {
 					JOptionPane.showMessageDialog(null,
 							"Only microarray sets of the same"
 									+ " underlying platform can be merged.",
 							"Merge Error", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 			} else {
 				JOptionPane.showMessageDialog(null,
 						"Select microarray set nodes only.", "Merge Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		// Verify that at least 2 microarray sets have been selected for merging
 		if (i < 2) {
 			JOptionPane.showMessageDialog(null, "Select 2 or more data nodes.",
 					"Merge Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		// Verify that all microarrays are of the same base type.
 		for (i = 0; i < count; ++i) {
 			if (!sets[0].getClass().isAssignableFrom(sets[i].getClass())) {
 				JOptionPane.showMessageDialog(null,
 						"Only microarray sets of the same"
 								+ " underlying platform can be merged.",
 						"Merge Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		DSMicroarraySet<DSMicroarray> mergedSet = FileOpenHandler.doMergeSets(sets);
 		if(mergedSet!=null) {
 			addDataSetNode(mergedSet, true);
 		}
 	}
 
 	static void addColorContext(DSMicroarraySet<DSMicroarray> maSet) {
 		GlobalPreferences prefs = GlobalPreferences.getInstance();
 		Class<? extends ColorContext> type = prefs.getColorContextClass();
 		try {
 			ColorContext context = type.newInstance();
 			maSet.addObject(ColorContext.class, context);
 			updateColorContext(maSet);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Action listener handling user requests for renaming a project.
 	 *
 	 * @param e
 	 */
 	private void jRenameProjectItem_actionPerformed(ActionEvent e) {
 		if (projectTree == null || selection == null
 				|| (selection.areNodeSelectionsCleared())
 				|| selection.getSelectedProjectNode() == null) {
 			JOptionPane.showMessageDialog(null, "Select a project node.",
 					"Rename Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		ProjectNode project = selection.getSelectedProjectNode();
 		String inputValue = JOptionPane.showInputDialog("Project Name:",
 				project.toString());
 		if (inputValue != null) {
 			project.setUserObject(inputValue);
 			projectTreeModel.nodeChanged(project);
 		}
 	}
 
 	/** Force refreshing the visible components. */
 	public void ccmUpdate() {
 		DataSetNode selectedDataSetNode = selection.getSelectedDataSetNode();
 		if (selectedDataSetNode != null) {
 			DSDataSet<?> dataset = selectedDataSetNode.getDataset();
 			GeawConfigObject.getGuiWindow().setVisualizationType(dataset);
 			publishProjectEvent(new ProjectEvent("CCM update", dataset,
 					selectedDataSetNode));
 		}
 	}
 
 	/*
 	 * This method will remove an added subnode. If the current selected node is
 	 * not the added subNode, then do nothing.
 	 *
 	 */
 	public void removeAddedSubNode(DSAncillaryDataSet<? extends DSBioObject> aDataSet) {
 
 		if (selection.getSelectedNode() instanceof DataSetSubNode) {
 			if (((DataSetSubNode) (selection.getSelectedNode()))._aDataSet != aDataSet) {
 				log.warn("the added node is not the selected node.");
 				return;
 			}
 
 			publishNodeRemovedEvent(new ProjectNodeRemovedEvent(
 					((DataSetSubNode) (selection.getSelectedNode()))._aDataSet));
 
 			ProjectTreeNode node = selection.getSelectedNode();
 
 			ProjectTreeNode parentNode = (ProjectTreeNode) node.getParent();
 
 			projectTreeModel.removeNodeFromParent(node);
 
 			setNodeSelection(parentNode);
 		}
 	}
 
 	private void remove_actionPerformed() {
 		if (projectTree == null || selection == null ) {
 			JOptionPane.showMessageDialog(null, "Please make a selection.",
 					"Delete Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		else if (selection.getSelectedNode().isRoot())
 		{
 			JOptionPane.showMessageDialog(null, "Please don't select a ROOT node.",
 					"Delete Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		TreePath[] paths = projectTree.getSelectionPaths();
 
 		if (paths.length <= 0)
 			return;
 
 		ProjectTreeNode node = new ProjectTreeNode();
 		ProjectTreeNode parentNode = null;
 
 		for (TreePath path : paths) {
 			node = (ProjectTreeNode) path.getLastPathComponent();
 
 			if (paths == null || node.isRoot())
 				continue;
 
 			boolean isExist = false;
 			for (int i = 0; i < paths.length; i++) {
 				if (paths[i].equals(path)) {
 					isExist = true;
 					break;
 				}
 
 			}
 			if (!isExist)
 				continue;
 
 			parentNode = (ProjectTreeNode) node.getParent();
 
 			if (node instanceof ProjectNode)
 				projectRemove_actionPerformed((ProjectNode) node);
 			else if (node instanceof ImageNode)
 				projectTreeModel.removeNodeFromParent(node);
 			else
 				fileRemove_actionPerformed(node);
 			paths = projectTree.getSelectionPaths();
 		}
 		// If there are any remaining projects, select the first of them to
 		// be the next one to get the focus.
 
 		if (parentNode == null)
 			return;
 		else if (parentNode.isRoot() && parentNode.getChildCount() <= 0) {
 			clear();
 			setNodeSelection(parentNode);
 		} else if (parentNode.isRoot() && parentNode.getChildCount() > 0) {
 			ProjectTreeNode pNode = (ProjectTreeNode) parentNode.getChildAt(0);
 			if (pNode.getChildCount() > 0) {
 				setNodeSelection((DataSetNode) pNode.getChildAt(0));
 			}
 		} else if (parentNode.getChildCount() == 0
 				&& parentNode instanceof ProjectNode) {
 			setNodeSelection(parentNode);
 			publishProjectEvent(new ProjectEvent(ProjectEvent.CLEARED, null,
 					parentNode));
 			return;
 		} else if (parentNode.getChildCount() > 0
 				&& parentNode instanceof ProjectNode) {
 			setNodeSelection((DataSetNode) parentNode.getChildAt(0));
 		} else if (parentNode.getChildCount() > 0
 				&& parentNode instanceof DataSetNode) {
 			setNodeSelection((DataSetSubNode) parentNode.getChildAt(0));
 		} else {
 			setNodeSelection(parentNode);
 		}
 
 	}
 
 	/**
 	 * Action listener handling user requests for removing a dataset.
 	 *
 	 * @param e
 	 */
 	private void fileRemove_actionPerformed(ProjectTreeNode node) {
 		// clear out unused mark annotation from memory
 		if (node instanceof DataSetNode) {
 			AnnotationParser
 					.cleanUpAnnotatioAfterUnload(((DataSetNode) node).getDataset());
 
 			if (node.getChildCount() > 0) {
 				for (Enumeration<?> en = node.children(); en.hasMoreElements();) {
 					ProjectTreeNode childNode = (ProjectTreeNode) en
 							.nextElement();
 					if (childNode instanceof DataSetSubNode)
 						publishNodeRemovedEvent(new ProjectNodeRemovedEvent(
 								((DataSetSubNode) (childNode))._aDataSet));
 					if (childNode instanceof PendingTreeNode) {
 
 						publishPendingNodeCancelledEvent(new PendingNodeCancelledEvent(
 								((PendingTreeNode) childNode).getGridEpr()));
 					}
 
 				}
 			}
 		}
 
 		// if it's a pending node, we fire a PendingNodeCancelledEvent.		
 		if (node instanceof PendingTreeNode) { 
 			publishPendingNodeCancelledEvent(new PendingNodeCancelledEvent(
 					((PendingTreeNode) node).getGridEpr()));
 		}
 
 		if (node instanceof DataSetSubNode)
 			publishNodeRemovedEvent(new ProjectNodeRemovedEvent(
 					((DataSetSubNode) (node))._aDataSet));
 
 		projectTreeModel.removeNodeFromParent(node);
 
 	}
 
 	@Publish
 	public PendingNodeCancelledEvent publishPendingNodeCancelledEvent(
 			PendingNodeCancelledEvent event) {
 		return event;
 	}
 
 	/**
 	 * Action listener handling user requests for removing a project.
 	 *
 	 * @param e
 	 */
 	private void projectRemove_actionPerformed(ProjectNode node) {
 		if (node.getChildCount() > 0) {
 			for (Enumeration<?> en = node.children(); en.hasMoreElements();) {
 				ProjectTreeNode childNode = (ProjectTreeNode) en
 						.nextElement();
 				if (childNode instanceof DataSetNode)
 					fileRemove_actionPerformed(childNode);
 			}
 		}
 		projectTreeModel.removeNodeFromParent(node);
 		GeawConfigObject.getGuiWindow().setVisualizationType(null);
 	}
 
 	/**
 	 * Action listener handling user requests for renaming a dataset.
 	 *
 	 * @param e
 	 */
 	private void jRenameDataset_actionPerformed(ActionEvent e) {
 		if (projectTree == null || selection == null
 				|| (selection.areNodeSelectionsCleared())) {
 			JOptionPane.showMessageDialog(null,
 					"Select a dataset or ancillary dataset.", "Rename Error",
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		DSDataSet<DSBioObject> ds = null;
 		ProjectTreeNode dsNode = null;
 
 		if (selection.getSelectedNode() instanceof DataSetNode) {
 			ds = selection.getDataSet();
 			dsNode = selection.getSelectedDataSetNode();
 		} else if (selection.getSelectedNode() instanceof DataSetSubNode) {
 			dsNode = selection.getSelectedDataSetSubNode();
 			ds = selection.getDataSubSet();
 		}
 		String oldName = ds.getLabel();
 		if (ds != null && dsNode != null) {
 			String inputValue = JOptionPane.showInputDialog("Dataset Name:",
 					dsNode.toString());
 			if (inputValue != null) {
 				dsNode.setUserObject(inputValue);
 				ds.setLabel(inputValue);
 				projectTreeModel.nodeChanged(dsNode);
 				publishNodeRenamedEvent(new ProjectNodeRenamedEvent("rename",
 						selection.getDataSubSet(), oldName, inputValue));
 			}
 		}
 	}
 
 	/**
 	 * Action listener handling user requests for the creation of new projects
 	 * in the workspace.
 	 *
 	 * @param e
 	 */
 	private void jNewProjectItem_actionPerformed(ActionEvent e) {
 		ProjectNode childNode = new ProjectNode("Project");
 		addToProject(childNode, true);
 	}
 
 	/**
 	 * Used to add a new node to a project tree
 	 *
 	 * @param child
 	 *            The node to be added
 	 * @param shouldBeVisible
 	 *            whether it should be visible or not
 	 * @return
 	 */
 	private ProjectNode addToProject(ProjectNode child, boolean shouldBeVisible) {
 		// ProjectNodeOld childNode = new ProjectNodeOld(child);
 		projectTreeModel.insertNodeInto(child, root, root.getChildCount());
 		// Make sure the user can see the lovely new node.
 		if (shouldBeVisible) {
 			projectTree.scrollPathToVisible(new TreePath(child.getPath()));
 			projectTree.setSelectionPath(new TreePath(child.getPath()));
 			selection.setNodeSelection(child);
 		}
 		// serialize("default.wsp");
 		return child;
 	}
 
 	/**
 	 * Sets the currently selected node within the project tree.
 	 *
 	 * @param node
 	 *            The project tree node to show up as selected.
 	 */
 	private void setNodeSelection(ProjectTreeNode node) {
 
 		if (node == null) {
 			log.error("selected node is null");
 			return;
 		}
 		selection.setNodeSelection(node);
 		projectTree.setSelectionPath(new TreePath(node.getPath()));
 	}
 
 	public ProjectSelection getSelection() {
 		return selection;
 	}
 
 	private ProjectTreeNode selectedNode = null;
 
 	@Publish
 	public ProjectEvent publishProjectEvent(ProjectEvent event) {
 		return event;
 	}
 
 	@Publish
 	public CaArrayRequestEvent publishCaArrayRequestEvent(
 			CaArrayRequestEvent event) {
 
 		return event;
 	}
 
 	@Publish
 	public CaArrayQueryEvent publishCaArrayQueryEvent(CaArrayQueryEvent event) {
 		return event;
 	}
 
 	private static void updateColorContext(DSMicroarraySet<DSMicroarray> maSet) {
 		ColorContext colorContext = (ColorContext) maSet
 				.getObject(ColorContext.class);
 		if (colorContext != null) {
 			CSMicroarraySetView<DSGeneMarker, DSMicroarray> view 
 				= new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
 			colorContext.updateContext(view);
 		}
 	}
 
 	/* Publishers of ImageSnapshotEvent are recommended to directly call addImageNode(ImageIcon) */
 	@Subscribe
 	public void receive(ImageSnapshotEvent event, Object source) {
 		if (event.getAction() == ImageSnapshotEvent.Action.SAVE) {
 			addImageNode(event.getImage());
 		}
 	}
 
 	/**
 	 * Add an image node under the last selected path.
 	 */
 	public void addImageNode(ImageIcon imageIcon) {
 		TreePath path = projectTree.getSelectionPath();
 		if (path != null) {
 			ImageNode imageNode = new ImageNode(imageIcon);
 			ProjectTreeNode node = (ProjectTreeNode) path
 					.getLastPathComponent();
 			if (node instanceof DataSetNode) {
 				projectTreeModel.insertNodeInto(imageNode, node, node
 						.getChildCount());
 			} else if (node instanceof DataSetSubNode) {
 				DataSetSubNode subNode = (DataSetSubNode) node;
 				node = (ProjectTreeNode) subNode.getParent();
 				if (node instanceof DataSetNode) {
 					projectTreeModel.insertNodeInto(imageNode, node, node
 							.getChildCount());
 				}
 			}
 		}
 	}
 
 	@Subscribe
 	public void receive(
 			org.geworkbench.events.PhenotypeSelectorEvent<DSMicroarray> e,
 			Object source) {
 
 		if (!(e.getDataSet() instanceof DSMicroarraySet)) {
 			return;
 		}
 
 		DSMicroarraySet<DSMicroarray> microarraySet = (DSMicroarraySet<DSMicroarray>) e
 				.getDataSet();
 		ColorContext colorContext = (ColorContext) microarraySet
 				.getObject(ColorContext.class);
 		if (colorContext != null) {
 			CSMicroarraySetView<DSGeneMarker, DSMicroarray> view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
 					microarraySet);
 			view.useItemPanel(true);
 			if (e.getTaggedItemSetTree() != null
 					&& e.getTaggedItemSetTree().size() > 0) {
 				DSPanel<DSMicroarray> activatedArrays = e
 						.getTaggedItemSetTree().activeSubset();
 				view.setItemPanel(activatedArrays);
 			}
 			colorContext.updateContext(view);
 		}
 	}
 
 	public void processCaArrayResult(boolean succeeded, String message, TreeMap<String, Set<String>> treeMap) {
 		loadData.processCaAraryQueryResult(succeeded, message, treeMap);
 	}
 
 	/**
 	 * process the results of applying a normalizer to a microarray set.
 	 *
 	 */
 	public void processNormalization(DSMicroarraySet<?> sourceMA, DSMicroarraySet<DSMicroarray> resultMA, String information) {
 
 		updateColorContext(resultMA);
 		// Set up the "history" information for the new dataset.
 		Object[] prevHistory = sourceMA.getValuesForName(HistoryPanel.HISTORY);
 
 		Object[] historyDetail = sourceMA.getValuesForName(HistoryPanel.HISTORYDETAIL);
 		String detail = (historyDetail == null ? "" : (String) historyDetail[0]);
 		sourceMA.clearName(HistoryPanel.HISTORYDETAIL);
 
 		if (prevHistory != null) {
 			sourceMA.clearName(HistoryPanel.HISTORY);
 		}
 		sourceMA.addNameValuePair(HistoryPanel.HISTORY, (prevHistory == null ? ""
 				: (String) prevHistory[0])
 				+ "Normalized with " + information + "\n" + detail + "\n");
 
 		// Notify interested components that the selected dataset has changed
 		// The event is thrown only if the normalized dataset is the one
 		// currently selected in the project panel.
 		DSDataSet<? extends DSBioObject> currentDS = (selection != null ? selection.getDataSet()
 				: null);
 		if (currentDS != null && currentDS instanceof DSMicroarraySet
 				&& (DSMicroarraySet<DSMicroarray>) currentDS == sourceMA) {
 			publishProjectEvent(new ProjectEvent(ProjectEvent.SELECTED,
 					sourceMA, selectedNode));
 		}
 	}
 
 	/**
 	 * For receiving the results of applying a filter to a microarray set.
 	 *
 	 * @param fe
 	 */
 	@Subscribe
 	public void receive(org.geworkbench.events.FilteringEvent fe, Object source) {
 		if (fe == null) {
 			return;
 		}
 		DSMicroarraySet<DSMicroarray> sourceMA = fe.getOriginalMASet();
 		if (sourceMA == null) {
 			return;
 		}
 
 		// Set up the "history" information for the new dataset.
 		Object[] prevHistory = sourceMA.getValuesForName(HistoryPanel.HISTORY);
 		if (prevHistory != null) {
 			sourceMA.clearName(HistoryPanel.HISTORY);
 		}
 		sourceMA.addNameValuePair(HistoryPanel.HISTORY, (prevHistory == null ? ""
 				: (String) prevHistory[0])
 				+ "Filtered with " + fe.getInformation() + "\n");
 		// Notify interested components that the selected dataset has changed
 		// The event is thrown only if the dataset filtered is the one
 		// currently selected in the project panel.
 		DSDataSet<? extends DSBioObject> currentDS = (selection != null ? selection.getDataSet()
 				: null);
 
 		if (currentDS != null && currentDS instanceof DSMicroarraySet
 				&& (DSMicroarraySet<DSMicroarray>) currentDS == sourceMA) {
 			publishProjectEvent(new ProjectEvent(ProjectEvent.SELECTED,
 					sourceMA, selectedNode));
 		}
 	}
 
 	/**
 	 * Clears the current workspace from the project window and notifies all
 	 * components that have registered to receive workspace clearing events.
 	 */
 	void clear() {
 		if (root != null) {
 			root.removeAllChildren();
 			projectTreeModel.reload(root);
 			selectedNode = null;
 		}
 
 		publishProjectEvent(new ProjectEvent(ProjectEvent.CLEARED, null, null));
 		selection.clearNodeSelections();
 	}
 
 	/**
 	 * Interface <code>MenuListener</code> method that returns the appropriate
 	 * <code>ActionListener</code> to handle <code>MenuEvent</code>
 	 * generated by <code>MenuItem</code> referenced by <code>menuKey</code>
 	 * attribute
 	 *
 	 * @param menuKey
 	 *            refers to <code>MenuItem</code>
 	 * @return <ActionListener> to handle <code>MenuEvent</code> generated by
 	 *         <code>MenuItem</code>
 	 */
 	public ActionListener getActionListener(String menuKey) {
 		return (ActionListener) listeners.get(menuKey);
 	}
 
 	/**
 	 * Interface <code>VisualPlugin</code> method that returns a
 	 * <code>Component</code> which is the visual representation of the this
 	 * plugin.
 	 *
 	 * @return <code>Component</code> visual representation of
 	 *         <code>ProjectPane</code>
 	 */
 	public Component getComponent() {
 		return jProjectPanel;
 	}
 
 	/**
 	 * <code>JComponent</code> types that constitute the
 	 * <code>ProjectPanel</code>
 	 */
 	private JPanel jProjectPanel = new JPanel();
 
 	private JLabel projectPanelTitleLabel = new JLabel("Project Folders");
 
 	private JScrollPane jDataSetScrollPane = new JScrollPane();
 
 	private ProjectTreeNode root = new ProjectTreeNode("Workspace");
 
 	private DefaultTreeModel projectTreeModel = new DefaultTreeModel(root);
 
 	private JTree projectTree = new JTree(projectTreeModel);
 
 	private JPopupMenu jRootMenu = new JPopupMenu();
 
 	private JPopupMenu jProjectMenu = new JPopupMenu();
 
 	private JMenuItem jUploadWspItem = new JMenuItem("Upload to server");
 
 	private JMenuItem jNewProjectItem = new JMenuItem("New Project");
 
 	private JMenuItem jLoadMArrayItem = new JMenuItem("Open File(s)");
 
 	private JMenuItem jOpenRemotePDBItem = new JMenuItem("Open PDB File from RCSB Protein Data Bank");
 
 	private JMenuItem jRenameProjectItem = new JMenuItem("Rename Project");
 
 	public int countProjectTree() {
 		return projectTree.getRowCount();
 	}
 
 	/**
 	 * PlaceHolder for <code>JComponent</code> listeners to be added to the
 	 * application's <code>JMenuBar</code> through the application
 	 * configuration functionality
 	 */
 	private HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
 
 	private void initializeMainPanel() {
 		projectPanelTitleLabel.setBorder(BorderFactory.createEtchedBorder());
 		projectPanelTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
 		jDataSetScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
 		jDataSetScrollPane.setMinimumSize(new Dimension(122, 80));
 
 		ToolTipManager.sharedInstance().registerComponent(projectTree);
 		projectTree.setCellRenderer(new TreeNodeRenderer());
 		projectTree.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 
 		jProjectPanel.setLayout(new BorderLayout());
 		jProjectPanel.add(projectPanelTitleLabel, BorderLayout.NORTH);
 		jProjectPanel.add(jDataSetScrollPane, BorderLayout.CENTER);
 		jProjectPanel.add(progressBar, BorderLayout.SOUTH);
 
 		projectTree.setBorder(new EmptyBorder(1, 1, 0, 0));
 		jDataSetScrollPane.getViewport().add(projectTree, null);
 
 		jRootMenu.add(jNewProjectItem);
 		jRootMenu.add(jUploadWspItem);
 		jProjectMenu.add(jLoadMArrayItem);
 		jProjectMenu.addSeparator();
 		jProjectMenu.add(jOpenRemotePDBItem);
 		jProjectMenu.addSeparator();
 		jProjectMenu.add(jRenameProjectItem);
 
 		projectTree.addMouseListener(new java.awt.event.MouseAdapter() {
 
 			public void mouseReleased(MouseEvent e) {
 				jProjectTree_mouseReleased(e);
 			}
 		});
 
 		projectTree.addKeyListener(new java.awt.event.KeyAdapter() {
 
 			public void keyReleased(KeyEvent e) {
 				jProjectTree_keyReleased(e);
 			}
 		});
 	}
 
 	/**
 	 * Add the action listeners that respond to the various menu selections
 	 * and popup selections.
 	 */
 	private void initializeMenuListeners() {
 		// the following section is those invoked by both main frame menus AND the context menu (right-clicked invoked)
 		ActionListener listener = null; // reused just for simplicity
 
 		listener = new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameProjectItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("Edit.Rename.Project", listener);
 		jRenameProjectItem.addActionListener(listener);
 		
 		listener = new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jNewProjectItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.New.Project", listener);
 		jNewProjectItem.addActionListener(listener);
 		
 		listener = new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jLoadMArrayItem_actionPerformed(e);
 			}
 
 		};
 		listeners.put("File.Open.File", listener);
 		jLoadMArrayItem.addActionListener(listener);
 		
 		listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jOpenRemotePDBItem_actionPerformed(e);
 			}
 		};
 		listeners.put("File.OpenRemotePDB.File", listener);
 		jOpenRemotePDBItem.addActionListener(listener);
 		
 		// the following section is those only invoked by main frame menus
 		listeners.put("Edit.Rename.File", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jRenameDataset_actionPerformed(e);
 			}
 		});
 		
 		listeners.put("File.Merge Datasets", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jMergeDatasets_actionPerformed(e);
 			}
 		});
 		
 		listeners.put("File.Save.Workspace", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveWorkspace_actionPerformed(false);
 			}
 		});
 
 		listeners.put("File.Open.Workspace", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openWorkspace_actionPerformed();
 			}
 		});
 
 		listeners.put("File.Open.Remote Workspace", new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				RWspHandler ws = new RWspHandler();
 				ws.listWsp(true);
 			}
 		});
 		
 		listeners.put("Tools.Choose OBO Source", new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				OboSourceDialog dlg = OboSourceDialog.getInstance();
 				dlg.refresh();
 				dlg.setVisible(true);
 			}
 		});
 		
 		listeners.put("Tools.My Account", new java.awt.event.ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				RWspHandler ws = new RWspHandler();
 				ws.listWsp(false);
 			}
 		});
 
 		listeners.put("File.New.Workspace", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newWorkspace_actionPerformed(e);
 			}
 		});
 
 		listeners.put("File.Remove", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				remove_actionPerformed();
 			}
 		});
 
 		listeners.put("File.Export", new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveNodeAsFile();
 			}
 		});
 	}
 	
 	private void initializeWorkspaceBehavior() {
 		// Let the main frame listen to window-closing event
 		GeawConfigObject.getGuiWindow().addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				RWspHelper.listLock();
 
 				int n = JOptionPane
 						.showConfirmDialog(
 								null,
 								"You're closing geWorkbench. \nDo you want to save the current workspace?",
 								"Save or not?",
 								JOptionPane.YES_NO_CANCEL_OPTION);
 				if (n == JOptionPane.CANCEL_OPTION)
 					return;
 
 				if (n == JOptionPane.YES_OPTION) {
 					saveWorkspace_actionPerformed(true);
 				} else { // if choosing No
 					GeawConfigObject.getGuiWindow().dispose();
 					UILauncher.printTimeStamp("geWorkbench exited.");
 					System.exit(0);
 				}
 			}
 		});
 
 		projectTreeModel.addTreeModelListener(new TreeModelListener(){
 			public void treeNodesChanged(TreeModelEvent arg0) {
 				RWspHandler.treeModified();
 			}
 			public void treeNodesInserted(TreeModelEvent arg0) {
 				RWspHandler.treeModified();
 			}
 			public void treeNodesRemoved(TreeModelEvent arg0) {
 				RWspHandler.treeModified();
 			}
 			public void treeStructureChanged(TreeModelEvent arg0) {
 				RWspHandler.treeModified();
 			}			
 		});
 	}
 
 	private void saveWorkspace_actionPerformed(boolean terminating) {
 		if (RWspHandler.wspId > 0)
 			RWspHandler.saveLocalwsp(terminating);
 		else {		
 			WorkspaceHandler ws = new WorkspaceHandler();
 			ws.save(WORKSPACE_DIR, terminating);
 			if (!StringUtils.isEmpty(ws.getWorkspacePath()))
 				GUIFramework.getFrame().setTitle(
 						((Skin)GeawConfigObject.getGuiWindow()).getApplicationTitle() + " ["
 								+ ws.getWorkspacePath() + "]");
 		}
 	}
 
 	private void openWorkspace_actionPerformed() {
 		if (RWspHandler.wspId > 0) {
 			RWspHandler.saveLocalwsp(false);
 			clear();
 		}
 
 		WorkspaceHandler ws = new WorkspaceHandler();
 		ws.open(WORKSPACE_DIR);
 		if (!StringUtils.isEmpty(ws.getWorkspacePath()))
 			GUIFramework.getFrame().setTitle(
 					((Skin)GeawConfigObject.getGuiWindow()).getApplicationTitle() + " ["
 							+ ws.getWorkspacePath() + "]");
 	}
 
 	/**
 	 *
 	 * @param pendingEvent
 	 * @return
 	 */
 	@Publish
 	public PendingNodeLoadedFromWorkspaceEvent publishPendingNodeLoadedFromWorkspaceEvent(
 			PendingNodeLoadedFromWorkspaceEvent event) {
 		return event;
 	}
 
 	private void newWorkspace_actionPerformed(ActionEvent e) {
 		if (RWspHandler.wspId > 0)
 			RWspHandler.saveLocalwsp(false);
 		else {
 			WorkspaceHandler ws = new WorkspaceHandler();
 			if (!ws.confirmLoading(WORKSPACE_DIR, null))
 				return;
 		}
 		clear();
 		GUIFramework.getFrame().setTitle(
 				((Skin)GeawConfigObject.getGuiWindow()).getApplicationTitle());
 	}
 
 	public DSDataSet<? extends DSBioObject> getDataSet() {
 		return selection.getDataSet();
 	}
 
 	@Publish
 	public ProjectNodePostCompletedEvent publishPostProcessingEvent(
 			ProjectNodePostCompletedEvent event) {
 		return event;
 	}
 
 	@Publish
 	public ProjectNodeRemovedEvent publishNodeRemovedEvent(
 			ProjectNodeRemovedEvent event) {
 		return event;
 	}
 
 	@Publish
 	public ProjectNodeRenamedEvent publishNodeRenamedEvent(
 			ProjectNodeRenamedEvent event) {
 		return event;
 	}
 	
 	public ProjectTreeNode getRoot() {
 		return (ProjectTreeNode) projectTreeModel.getRoot();
 	}
 	
 	JProgressBar getProgressBar() { return progressBar; }
 }
