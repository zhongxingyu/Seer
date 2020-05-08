 package org.geworkbench.builtin.projects.util;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 import javax.swing.border.Border;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.*;
 
 import gov.nih.nci.common.search.*; //import gov.nih.nci.common.search.session.SecureSession;
 //import gov.nih.nci.common.search.session.SecureSessionFactory;
 //import gov.nih.nci.mageom.domain.BioAssay.BioAssay;
 //import gov.nih.nci.mageom.domain.BioAssay.impl.*;
 //import gov.nih.nci.mageom.domain.BioAssayData.BioAssayData;
 //import gov.nih.nci.mageom.domain.Description.Description;
 //import gov.nih.nci.mageom.domain.Experiment.Experiment;
 //import gov.nih.nci.mageom.domain.Experiment.impl.ExperimentImpl;
 //import gov.nih.nci.mageom.search.SearchCriteriaFactory;
 //import gov.nih.nci.mageom.search.Experiment.ExperimentSearchCriteria;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
 import org.geworkbench.bison.parsers.resources.CaArrayResource;
 import org.geworkbench.builtin.projects.LoadData;
 import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
 
 import org.geworkbench.engine.management.Publish;
 import org.geworkbench.events.CaArrayEvent;
 import org.geworkbench.events.CaArrayRequestEvent;
 import org.geworkbench.events.ProjectEvent;
 import org.geworkbench.util.ProgressBar;
 
 /**
  * <p>
  * Title: Bioworks
  * </p>
  * <p>
  * Description: Modular Application Framework for Gene Expession, Sequence and
  * Genotype Analysis
  * </p>
  * <p>
  * Copyright: Copyright (c) 2003 -2004
  * </p>
  * <p>
  * Company: Columbia University
  * </p>
  * 
  * @author manjunath at genomecenter dot columbia dot edu
  * @version 1.0
  */
 
 public class CaARRAYPanel extends JPanel implements Observer {
 	private static final String CAARRAY_TITLE = "caARRAY";
 
 	private static Log log = LogFactory.getLog(CaARRAYPanel.class);
 	/**
 	 * Stores experiments from the remote server.
 	 */
 	protected CaArrayExperiment[] experiments = null;
 
 	/**
 	 * Used to avoid querying the server for all experiments all the time.
 	 */
 	protected boolean experimentsLoaded = false;
 	// display the status of connection.
 	private boolean stopConnection = false;
 	private boolean stillConnecting = true;
 	private String currentResourceName = null;
 	private String previousResourceName = null;
 	private JLabel displayLabel = new JLabel();
 	private JPanel jPanel6 = new JPanel();
 	private GridLayout grid4 = new GridLayout();
 	private JPanel caArrayDetailPanel = new JPanel();
 	private JPanel caArrayTreePanel = new JPanel();
 	private BorderLayout borderLayout4 = new BorderLayout();
 	private BorderLayout borderLayout7 = new BorderLayout();
 	private Border border1 = null;
 	private Border border2 = null;
 	private JScrollPane jScrollPane1 = new JScrollPane();
 	private JPanel jPanel10 = new JPanel();
 	private JLabel jLabel4 = new JLabel();
 	private JScrollPane jScrollPane2 = new JScrollPane();
 	private JPanel jPanel14 = new JPanel();
 	private JPanel jPanel16 = new JPanel();
 	private JLabel derivedLabel = new JLabel("Number of Assays");
 	private JTextField measuredField = new JTextField();
 	private JTextField derivedField = new JTextField();
 	private JTextArea experimentInfoArea = new JTextArea();
 	private JPanel jPanel13 = new JPanel();
 	private JButton extendButton = new JButton();
 	private JButton openButton = new JButton();
 	private JButton cancelButton = new JButton();
 	private LoadData parent = null;
 	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(
 			"caARRAY experiments");
 	private DefaultTreeModel remoteTreeModel = new DefaultTreeModel(root);
 	private JTree remoteFileTree = new JTree(remoteTreeModel);
 	private JPopupMenu jRemoteDataPopup = new JPopupMenu();
 	private JMenuItem jGetRemoteDataMenu = new JMenuItem();
 	private boolean connectionSuccess = true;
 	private String user;
 	private String passwd;
 	private String url;
 	private int portnumber;
 	private ProgressMonitor progressMonitor;
 	private ConnectionTask task;
 	private Timer timer;
 	private ProgressBar progressBar = ProgressBar
 			.create(ProgressBar.INDETERMINATE_TYPE);
 	private LoadData parentPanel;
 	private boolean merge;
 	private boolean stillWaitForConnecting = true;
 	private TreeMap<String, CaArray2Experiment> treeMap;
 	private String currentSelectedExperimentName;
 	private String[] currentSelectedBioAssayName;
 	private CaArray2Experiment[] currentLoadedExps;
 	private String currentQuantitationType;
 	private static final int INTERNALTIMEOUTLIMIT = 600;
 
 	public CaARRAYPanel(LoadData p) {
 		parent = p;
 		try {
 
 			jbInit();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean isStillWaitForConnecting() {
 		return stillWaitForConnecting;
 	}
 
 	public void setStillWaitForConnecting(boolean stillWaitForConnecting) {
 		this.stillWaitForConnecting = stillWaitForConnecting;
 	}
 
 	public void receive(CaArrayEvent ce) {
 		if(!stillWaitForConnecting){
 			return;
 		}
 		stillWaitForConnecting = false;
 		progressBar.stop();
 
 		if (!ce.isPopulated()) {
 			String errorMessage = ce.getErrorMessage();
 			if (errorMessage == null) {
 				errorMessage = "Cannot connect with the server.";
 			}
 			if (!ce.isSucceed()) {
 
 				JOptionPane.showMessageDialog(null, "The error: "
 						+ errorMessage);
 				// + ce.getErrorMessage()==null? "Cannot connect with the
 				// server.":ce.getErrorMessage());
 			} else {
 				JOptionPane.showMessageDialog(null, errorMessage);
 			}
 
 		}
 
 		if (ce.getInfoType().equalsIgnoreCase(CaArrayEvent.EXPERIMENT)) {
 			currentLoadedExps = ce.getExperiments();
 			treeMap = new TreeMap<String, CaArray2Experiment>();
 			root = new DefaultMutableTreeNode("caARRAY experiments");
 			remoteTreeModel.setRoot(root);
 			if (currentLoadedExps == null) {
 				return;
 			}
 
 			for (int i = 0; i < currentLoadedExps.length; ++i) {
 				DefaultMutableTreeNode node = new DefaultMutableTreeNode(
 						currentLoadedExps[i].getName());
 				treeMap.put(currentLoadedExps[i].getName(),
 						currentLoadedExps[i]);
 				remoteTreeModel
 						.insertNodeInto(node, root, root.getChildCount());
 			}
 			remoteFileTree.expandRow(0);
 			experimentsLoaded = true;
 			previousResourceName = currentResourceName;
 			connectionSuccess = true;
 			if (currentLoadedExps != null) {
 				displayLabel.setText("Total Experiments: "
 						+ currentLoadedExps.length);
 				caArrayTreePanel.add(displayLabel, BorderLayout.SOUTH);
 			}
 			revalidate();
 		} else {
 			dispose();// make itself disappear.
 		}
 	}
 
 	public void publishCaArrayEvent(CaArrayRequestEvent event) {
 		parent.publishCaArrayRequestEvent(event);
 	}
 
 	public void publishProjectNodeAddedEvent(
 			org.geworkbench.events.ProjectNodeAddedEvent event) {
 		parent.publishProjectNodeAddedEvent(event);
 	}
 
 	private void jbInit() throws Exception {
 		border1 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
 		border2 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
 		grid4.setColumns(2);
 		grid4.setHgap(10);
 		grid4.setRows(1);
 		grid4.setVgap(10);
 		jLabel4.setMaximumSize(new Dimension(200, 15));
 		jLabel4.setMinimumSize(new Dimension(200, 15));
 		jLabel4.setPreferredSize(new Dimension(200, 15));
 		jLabel4.setText("Experiment Information:");
 
 		jPanel13.setLayout(new BoxLayout(jPanel13, BoxLayout.X_AXIS));
 		jPanel13.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		derivedField.setMinimumSize(new Dimension(8, 20));
 		derivedField.setPreferredSize(new Dimension(8, 20));
 		caArrayTreePanel.setPreferredSize(new Dimension(197, 280));// change
 		// from 137
 		// to 167.
 		caArrayDetailPanel.setPreferredSize(new Dimension(380, 300));
 		jPanel10.setPreferredSize(new Dimension(370, 280));
 		this.setMinimumSize(new Dimension(510, 200));
 		this.setPreferredSize(new Dimension(510, 200));
 		jPanel13.add(extendButton);
 		// jPanel13.add(Box.createHorizontalGlue());
 		jPanel13.add(openButton);
 		// jPanel13.add(Box.createRigidArea(new Dimension(10, 0)));
 		jPanel13.add(cancelButton);
 
 		measuredField.setEditable(false);
 
 		extendButton.setPreferredSize(new Dimension(100, 25));
 		extendButton.setText("Show arrays");
 		extendButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				extendBioassays_action(e);
 			}
 		});
 		extendButton.setToolTipText("Display Bioassys");
 		openButton.setPreferredSize(new Dimension(60, 25));
 		openButton.setText("Open");
 		openButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openRemoteFile_action(e);
 			}
 		});
 		openButton.setToolTipText("Load remote MicroarrayDataSet");
 		cancelButton.setMinimumSize(new Dimension(68, 25));
 		cancelButton.setPreferredSize(new Dimension(68, 25));
 		cancelButton.setText("Cancel");
 		cancelButton.setToolTipText("Close the Window");
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jButton1_actionPerformed(e);
 			}
 		});
 
 		jPanel16.setLayout(new BoxLayout(jPanel16, BoxLayout.Y_AXIS));
 		jPanel16.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		jPanel16.add(Box.createVerticalGlue());
 		jPanel16.add(Box.createRigidArea(new Dimension(10, 0)));
 		jPanel16.add(derivedLabel);
 		jPanel16.add(derivedField);
 		jPanel16.add(Box.createRigidArea(new Dimension(10, 0)));
 		derivedField.setEditable(false);
 		jScrollPane2.getViewport().add(experimentInfoArea, null);
		jScrollPane2.setPreferredSize(new Dimension(300, 500));
 		jPanel14.setLayout(new BoxLayout(jPanel14, BoxLayout.X_AXIS));
 		jPanel14.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		jPanel14.add(Box.createHorizontalGlue());
 		jPanel14.add(Box.createRigidArea(new Dimension(10, 0)));
 		jPanel14.add(Box.createRigidArea(new Dimension(10, 0)));
 		experimentInfoArea.setPreferredSize(new Dimension(300, 600));
 		experimentInfoArea.setText("");
 		experimentInfoArea.setEditable(false);
 		experimentInfoArea.setLineWrap(true);
 		experimentInfoArea.setWrapStyleWord(true);
 
 		jPanel6.setLayout(grid4);
 		caArrayTreePanel.setLayout(borderLayout4);
 		caArrayDetailPanel.setLayout(borderLayout7);
 		jPanel10.setLayout(new BoxLayout(jPanel10, BoxLayout.Y_AXIS));
 		jPanel10.add(jLabel4, null);
 		jPanel10.add(Box.createRigidArea(new Dimension(0, 10)));
 		jPanel10.add(jScrollPane2, null);
 		jPanel10.add(Box.createRigidArea(new Dimension(0, 10)));
 		jPanel10.add(jPanel14, null);
 		jPanel14.add(jPanel16);
 		jPanel10.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
 		caArrayDetailPanel.setBorder(border1);
 		caArrayTreePanel.setBorder(border2);
 		jScrollPane1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		caArrayDetailPanel.add(jPanel10, BorderLayout.CENTER);
 		caArrayDetailPanel.add(jPanel13, BorderLayout.SOUTH);
 		jPanel6.setMaximumSize(new Dimension(700, 449));
 		jPanel6.setMinimumSize(new Dimension(675, 339));
 		jPanel6.setPreferredSize(new Dimension(700, 449));
 		jPanel6.add(caArrayTreePanel);
 		caArrayTreePanel.add(jScrollPane1, BorderLayout.CENTER);
 
 		jScrollPane1.getViewport().add(remoteFileTree, null);
 		jPanel6.add(caArrayDetailPanel);
 
 		remoteFileTree.setToolTipText("");
 		remoteFileTree.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 		remoteFileTree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent tse) {
 				remoteFileSelection_action(tse);
 			}
 		});
 		remoteFileTree.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseReleased(MouseEvent e) {
 				jRemoteFileTree_mouseReleased(e);
 			}
 		});
 		jGetRemoteDataMenu.setText("Get bioassays");
 		ActionListener listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				jGetRemoteData_actionPerformed(e);
 			}
 		};
 		jGetRemoteDataMenu.addActionListener(listener);
 		jRemoteDataPopup.add(jGetRemoteDataMenu);
 		jPanel6.setMaximumSize(new Dimension(500, 300));
 		jPanel6.setMinimumSize(new Dimension(500, 285));
 		jPanel6.setPreferredSize(new Dimension(500, 285));
 		this.add(jPanel6, BorderLayout.CENTER);
 
 	}
 
 	/**
 	 * Update the progressBar to reflect the current time.
 	 * 
 	 * @param text
 	 */
 	public void updateProgressBar(final String text) {
 		Runnable r = new Runnable() {
 			public void run() {
 				try {
 					if (text.startsWith("Loading")) {
 						int i = 0;
 						do {
 							Thread.sleep(250);
 							i++;
 							if (i > 4) {
 								progressBar.setMessage(text + i / 4
 										+ " seconds.");
 							}
 							if(i>INTERNALTIMEOUTLIMIT*4){
 								stillWaitForConnecting = false;
 								JOptionPane.showMessageDialog(null, "Cannot connect with the server in " + INTERNALTIMEOUTLIMIT + " seconds.", "Timeout", JOptionPane.ERROR_MESSAGE);
 								update(null, null);
 							}
 						} while (stillWaitForConnecting);
 
 					}
 				} catch (Exception e) {
 				}
 			}
 		};
 		new Thread(r).start();
 		// SwingUtilities.invokeLater(r);
 	}
 
 	/**
 	 * Action listener invoked when the user presses the "Open" button after
 	 * having selected a remote microarray. The listener will attempt to get the
 	 * microarray data from the remote server and load them in the application.
 	 * 
 	 * @param e
 	 */
 	private void openRemoteFile_action(ActionEvent e) {
 		String qType = checkQuantationTypeSelection();
 
 		if (qType != null) {
 			stillWaitForConnecting = true;
 			progressBar
 					.setMessage("Loading selected bioassays - elapsed time: ");
 			updateProgressBar("Loading selected bioassays - elapsed time: ");
 			progressBar.addObserver(this);
 			progressBar.setTitle(CAARRAY_TITLE);
 			progressBar.start();
 			task = new ConnectionTask();
 			task.getBioAssayThread();
 		}
 
 	}
 
 	public void startProgressBar() {
 		stillWaitForConnecting = true;
 		updateProgressBar("Loading the filtered experiments - elapsed time: ");
 		progressBar.setTitle(CAARRAY_TITLE);
 		progressBar.start();
 
 	}
 
 	/**
 	 * Action listener invoked when the user presses the "Show Bioassays" button
 	 * after having selected a remote microarray. If no node is selected, all
 	 * experiments will be extended with their associated bioassays. Otherwise,
 	 * only current selection will be extended.
 	 * 
 	 * @param e
 	 */
 	private void extendBioassays_action(ActionEvent e) {
 		if (treeMap == null || treeMap.size() == 0) {
 			JOptionPane.showMessageDialog(null, "No Experiment to display");
 			return;
 		}
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode) remoteFileTree
 				.getLastSelectedPathComponent();
 		if (node != null && node != root) {
 			// Lazy computation of the children on an experiment.
 			DefaultMutableTreeNode assayNode = null;
 
 			if (treeMap.containsKey(node.getUserObject())) {
 				String exp = (String) node.getUserObject();
 				// Add in the tree the measured bioassays
 				if (node.getChildCount() == 0) {
 
 					// Add in the tree the derived bioassays
 					String[] derived = treeMap.get(exp).getHybridizations();
 					if (derived != null && derived.length > 0) {
 						for (int i = 0; i < derived.length; ++i) {
 							assayNode = new DefaultMutableTreeNode(derived[i]);
 							remoteTreeModel.insertNodeInto(assayNode, node,
 									node.getChildCount());
 						}
 					}
 					// exp.setPopulated();
 					remoteFileTree.expandPath(new TreePath(node.getPath()));
 				}
 			}
 		} else {
 			Enumeration<DefaultMutableTreeNode> nodes = root.children();
 			if (nodes != null) {
 				while (nodes.hasMoreElements()) {
 					DefaultMutableTreeNode currentNode = nodes.nextElement();
 					// Lazy computation of the children on an experiment.
 					DefaultMutableTreeNode assayNode = null;
 
 					if (treeMap.containsKey(node.getUserObject())) {
 						String exp = (String) node.getUserObject();
 						// Add in the tree the measured bioassays
 						if (node.getChildCount() == 0) {
 
 							// Add in the tree the derived bioassays
 							String[] derived = treeMap.get(exp)
 									.getHybridizations();
 							if (derived != null && derived.length > 0) {
 								for (int i = 0; i < derived.length; ++i) {
 									assayNode = new DefaultMutableTreeNode(
 											derived[i]);
 									remoteTreeModel.insertNodeInto(assayNode,
 											node, node.getChildCount());
 								}
 							}
 							// exp.setPopulated();
 							remoteFileTree.expandPath(new TreePath(node
 									.getPath()));
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	private String checkQuantationTypeSelection() {
 		merge = parent.isMerge();
 		TreePath[] paths = remoteFileTree.getSelectionPaths();
 		CaArray2Experiment exp = null;
 		if (paths.length > 0) {
 			currentSelectedExperimentName = (String) ((DefaultMutableTreeNode) paths[0]
 					.getPath()[1]).getUserObject();
 			exp = treeMap.get(currentSelectedExperimentName);
 			currentSelectedBioAssayName = new String[paths.length];
 			for (int i = 0; i < paths.length; i++) {
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i]
 						.getLastPathComponent();
 				currentSelectedBioAssayName[i] = (String) node.getUserObject();
 			}
 			String[] qTypes = exp.getQuantitationTypes();
 			if (qTypes != null && qTypes.length > 0) {
 				boolean findMatchedQType = false;
 				if (currentQuantitationType != null) {
 					for (String candidateQType : qTypes) {
 						if (currentQuantitationType
 								.equalsIgnoreCase(candidateQType)) {
 							findMatchedQType = true;
 							break;
 						}
 					}
 				}
 				String s = null;
 				if (findMatchedQType) {
 					s = (String) JOptionPane.showInputDialog(null,
 							"Please select the quantitation type to query:\n",
 							"Selection Dialog", JOptionPane.PLAIN_MESSAGE,
 							null, qTypes, currentQuantitationType);
 				} else {
 					s = (String) JOptionPane.showInputDialog(null,
 							"Please select the quantitation type to query:\n",
 							"Selection Dialog", JOptionPane.PLAIN_MESSAGE,
 							null, qTypes, qTypes[0]);
 				}
 				// If a string was returned, say so.
 				if ((s != null) && (s.length() > 0)) {
 					currentQuantitationType = s;
 					return currentQuantitationType;
 				}
 
 			} else {
 				JOptionPane.showMessageDialog(null,
 						"There is no data associated with that experiment.");
 			}
 
 		} else {
 			JOptionPane.showMessageDialog(null,
 					"Please select at least one Bioassay to retrieve.");
 		}
 		return null;
 
 	}
 
 	/**
 	 * This method is called if the cancel button is pressed.
 	 * 
 	 * @param e -
 	 *            Event information.
 	 */
 	private void jButton1_actionPerformed(ActionEvent e) {
 		dispose();
 	}
 
 	public void getExperiments(ActionEvent e) {
 		stillConnecting = true;
 		displayLabel.setText("");
 		if (!experimentsLoaded
 				|| !currentResourceName.equals(previousResourceName)) {
 
 			getExperiments();
 
 			this.validate();
 			this.repaint();
 		} else if (currentResourceName.equals(previousResourceName)
 				&& experimentsLoaded) {
 
 			if (parentPanel != null) {
 				parentPanel.addRemotePanel();
 			}
 		}
 	}
 
 	/**
 	 * Menu action listener that brings up the popup menu that allows populating
 	 * the tree node for a remote file.
 	 * 
 	 * @param e
 	 */
 	private void jRemoteFileTree_mouseReleased(MouseEvent e) {
 		TreePath path = remoteFileTree.getPathForLocation(e.getX(), e.getY());
 		if (path != null) {
 			DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode) path
 					.getLastPathComponent());
 			Object nodeObj = selectedNode.getUserObject();
 			// if (e.isMetaDown() && (selectedNode instanceof
 			// CaArrayExperiment)) {
 			if (e.isMetaDown() && (treeMap.containsKey(nodeObj))) {
 				jRemoteDataPopup.show(remoteFileTree, e.getX(), e.getY());
 				if (selectedNode.getChildCount() > 0) {
 					jGetRemoteDataMenu.setEnabled(false);
 				} else {
 					jGetRemoteDataMenu.setEnabled(true);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Prints the reporter related data to standard out.
 	 * 
 	 * @param reporterData -
 	 *            Array of reporter related data to be printed.
 	 */
 	// private static void printReporterRelatedData(ReporterRelatedData[]
 	// reporterData) {
 	// for (int x = 0; x < reporterData.length; x++) {
 	// System.out.println("\n*********************************************************");
 	// System.out.println("Reporter: " + reporterData[x].getReporterName());
 	// System.out.println("Reporter ID: " + reporterData[x].getReporterId());
 	// BioAssayDatumData[] datum = reporterData[x].getBioAssayDatumData();
 	// for (int y = 0; y < datum.length; y++) {
 	// System.out.println("\tDatum[" + y + "]: '" + datum[y].getType() + "' - "
 	// + datum[y].getValue());
 	// }
 	//
 	// System.out.println("");
 	// OntologyEntryData[] onts = reporterData[x].getSequenceOntologies();
 	// for (int y = 0; y < onts.length; y++) {
 	// System.out.println("\tOntology[" + y + "]: '" + onts[y].getCategory() +
 	// "' - " + onts[y].getValue());
 	// }
 	// System.out.println("*********************************************************\n");
 	// }
 	// }
 	/**
 	 * Action listener invoked when a remote file is selected in the remote file
 	 * tree. Updates the experiment information text area.
 	 * 
 	 * @param
 	 */
 	private void remoteFileSelection_action(TreeSelectionEvent tse) {
 		if (tse == null) {
 			return;
 		}
 		updateTextArea(); // Update the contents of the text area.
 	}
 
 	/**
 	 * Properly update the experiment text area, based on the currently selected
 	 * node.
 	 */
 	private void updateTextArea() {
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode) remoteFileTree
 				.getLastSelectedPathComponent();
 
 		if (node == null || node == root || treeMap == null
 				|| treeMap.size() == 0) {
 			experimentInfoArea.setText("");
 			measuredField.setText("");
 			derivedField.setText("");
 		} else {
 			// get the parent experiment.
 			String exp = (String) ((DefaultMutableTreeNode) node.getPath()[1])
 					.getUserObject();
 			experimentInfoArea.setText(treeMap.get(exp).getDescription());
 			String[] hybridization = treeMap.get(exp).getHybridizations();
 			if (hybridization != null) {
 				measuredField.setText(String.valueOf(hybridization.length));
 				derivedField.setText(String.valueOf(hybridization.length));
 			}
 		}
 		experimentInfoArea.setCaretPosition(0); // For long text.
 	}
 
 	/**
 	 * Populate the subtree corresponding to an Experiment node with data from
 	 * the remote server.
 	 * 
 	 * @param e
 	 */
 	private void jGetRemoteData_actionPerformed(ActionEvent e) {
 		if (e == null) {
 			return;
 		}
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode) remoteFileTree
 				.getLastSelectedPathComponent();
 		if (node != null && node != root && treeMap != null
 				&& treeMap.size() != 0) {
 			// Lazy computation of the children on an experiment.
 			DefaultMutableTreeNode assayNode = null;
 			DefaultMutableTreeNode dataNode = null;
 			if (treeMap.containsKey(node.getUserObject())) {
 				String exp = (String) node.getUserObject();
 				// Add in the tree the measured bioassays
 				if (node.getChildCount() == 0) {
 
 					// Add in the tree the derived bioassays
 					String[] derived = treeMap.get(exp).getHybridizations();
 					if (derived != null && derived.length > 0) {
 						for (int i = 0; i < derived.length; ++i) {
 							assayNode = new DefaultMutableTreeNode(derived[i]);
 							remoteTreeModel.insertNodeInto(assayNode, node,
 									node.getChildCount());
 						}
 					}
 					// exp.setPopulated();
 					remoteFileTree.expandPath(new TreePath(node.getPath()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * The actionPerformed method in this class is called each time the Timer
 	 * "goes off".
 	 */
 	class TimerListener implements ActionListener {
 		public void actionPerformed(ActionEvent evt) {
 			int taskStatus = 10;
 			if (task != null) {
 				taskStatus = task.getStatus() * 100;
 				// progressBar.updateTo(taskStatus);
 
 				if (task.isDone()) {
 					progressMonitor.close();
 					if (parentPanel != null && !stopConnection) {
 						// update the GUI with new loaded panel.
 						parentPanel.addRemotePanel();
 					}
 					Toolkit.getDefaultToolkit().beep();
 					timer.stop();
 					progressBar.dispose();
 				}
 			}
 		}
 	}
 
 	/**
 	 * For connection to remote resource
 	 */
 	class ConnectionTask {
 		CaArrayExperiment[] exps = new CaArrayExperiment[0];
 		Vector temp = new Vector();
 		private int status = 0;
 		private boolean done = false;
 		private boolean stopped = false;
 
 		// SecureSession sess;
 
 		public ConnectionTask() {
 
 		}
 
 		public ConnectionTask(String _url, String _user, String _passwd) {
 
 		}
 
 		// public ConnectionTask(SecureSession _sess) {
 		// sess = _sess;
 		// }
 
 		public void go() {
 			Runnable dataLoader = new Runnable() {
 				public void run() {
 					connect();
 				}
 			};
 			Thread t = new Thread(dataLoader);
 			t.setPriority(Thread.MAX_PRIORITY);
 			t.start();
 
 		}
 
 		public void getBioAssayThread() {
 			Runnable dataLoader = new Runnable() {
 				public void run() {
 					getBioAssay();
 				}
 			};
 			Thread t = new Thread(dataLoader);
 			t.setPriority(Thread.MAX_PRIORITY);
 			t.start();
 
 		}
 
 		public void getBioAssay() {
 			// CaArrayRequestEvent event = new CaArrayRequestEvent(
 			// "array-stage.nci.nih.gov", 8080);
 			CaArrayRequestEvent event = new CaArrayRequestEvent(url, portnumber);
 			if (user == null || user.trim().length() == 0) {
 				event.setUsername(null);
 			} else {
 				event.setUsername(user);
 				event.setPassword(passwd);
 			}
 			event.setRequestItem(CaArrayRequestEvent.BIOASSAY);
 			event.setMerge(merge);
 			HashMap<String, String[]> filterCrit = new HashMap<String, String[]>();
 			filterCrit.put(CaArrayRequestEvent.EXPERIMENT,
 					new String[] { currentSelectedExperimentName });
 			filterCrit.put(CaArrayRequestEvent.BIOASSAY,
 					currentSelectedBioAssayName);
 			event.setFilterCrit(filterCrit);
 			event.setQType(currentQuantitationType);
 			log.info("publish CaArrayEvent at CaArrayPanel");
 			publishCaArrayEvent(event);
 		}
 
 		public void connect() {
 
 			// update the progress message.
 			 
 			//stillWaitForConnecting = false;
 			 stillWaitForConnecting = true;
 			progressBar
 					.setMessage("Connecting with the server... The initial step may take a few minutes.");
 			// CaArrayRequestEvent event = new CaArrayRequestEvent(
 			CaArrayRequestEvent event = new CaArrayRequestEvent(url, portnumber);
 			if (user == null || user.trim().length() == 0) {
 				event.setUsername(null);
 			} else {
 				event.setUsername(user);
 				event.setPassword(passwd);
 
 			}
 			event.setRequestItem(CaArrayRequestEvent.EXPERIMENT);
 			publishCaArrayEvent(event);
 			stillConnecting = false;
 
 		}
 
 		public int getStatus() {
 			status++;
 			return status;
 		}
 
 		public boolean isDone() {
 			return done;
 		}
 
 		public void stop() {
 			stopped = true;
 		}
 
 	}
 
 	/**
 	 * Gets a list of all experiments available on the remote server.
 	 */
 	public void getExperiments() {
 
 		stillWaitForConnecting = true;
 		progressBar
 				.setMessage("Loading experiments from the remote resource...");
 		updateProgressBar("Loading experiments from the remote resource for ");
 		progressBar.addObserver(this);
 		progressBar.setTitle(CAARRAY_TITLE);
 		progressBar.start();
 
 		try {
 
 			if (url == null) {
 				user = System.getProperty("caarray.mage.user");
 				passwd = System.getProperty("caarray.mage.password");
 				url = System.getProperty("SecureSessionManagerURL");
 			}
 
 			// System.setProperty("RMIServerURL", url +
 			// "/SearchCriteriaHandler");
 			System.setProperty("SecureSessionManagerURL", url);
 			task = new ConnectionTask();
 			timer = new Timer(2500, new TimerListener());
 			timer.start();
 			task.go();
 			if (stopConnection) {
 				return;
 			}
 
 			if (task.isDone()) {
 				timer.stop();
 			}
 
 			// return experiments;
 
 		}
 
 		catch (Exception e) {
 			e.printStackTrace();
 			stopConnection = true;
 			// pb.stop();
 
 		}
 
 	}
 
 	private void dispose() {
 		parent.dispose();
 	}
 
 	public void update(java.util.Observable ob, Object o) {
 		stopConnection = true;
 		stillWaitForConnecting = false;
 		if (timer != null) {
 			timer.stop();
 		}
 		if (task != null) {
 			task.stop();
 		}
 		log.error("Get Cancelled");
 		 
 		progressBar.dispose();
 		CaArrayRequestEvent event = new CaArrayRequestEvent(url, portnumber);
 		if (user == null || user.trim().length() == 0) {
 			event.setUsername(null);
 		} else {
 			event.setUsername(user);
 			event.setPassword(passwd);
 
 		}
 		event.setRequestItem(CaArrayRequestEvent.CANCEL);
 		publishCaArrayEvent(event);
 
 	}
 
 	/**
 	 * This class provides a GUI wrapper for a BioAssayImpl object. This object
 	 * will be displayed as a node in the JTree.
 	 */
 	// class CaArrayBioassay {
 	// private String m_id = null;
 	// private BioAssay ba = null;
 	// private BioAssayData[] baData = null;
 	// private int dataCount = 0;
 	// // Convenience flag.
 	// private boolean countFlag = false;
 	// // Convenience flag.
 	// private boolean flag = false;
 	//
 	// public CaArrayBioassay(BioAssay b) {
 	// ba = b;
 	// try {
 	// m_id = b.getIdentifier();
 	// } catch (Exception e) {
 	// System.out.println("Error getting id for bioasay: " +
 	// e.toString());
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// public String getId() {
 	// String id = "";
 	// if (m_id != null) {
 	// return m_id;
 	// }
 	// return id;
 	// }
 	//
 	// public BioAssay getBioAssayImpl() {
 	// return ba;
 	// }
 	//
 	// public int getDataCount() {
 	// if (ba == null) {
 	// return 0;
 	// }
 	// if (countFlag) {
 	// return dataCount;
 	// }
 	// try {
 	// if (ba instanceof MeasuredBioAssayImpl) {
 	// dataCount = ((MeasuredBioAssayImpl) ba).
 	// getMeasuredBioAssayDataCount();
 	// } else if (ba instanceof DerivedBioAssayImpl) {
 	// dataCount = ((DerivedBioAssayImpl) ba).
 	// getDerivedBioAssayDataCount();
 	// } else {
 	// dataCount = 0;
 	// }
 	// } catch (Exception e) {
 	// return 0;
 	// }
 	// countFlag = true;
 	// return dataCount;
 	// }
 	//
 	// BioAssayData[] getData() {
 	// if (ba == null) {
 	// return null;
 	// }
 	// if (flag) {
 	// return baData;
 	// }
 	// try {
 	// if (ba instanceof MeasuredBioAssayImpl) {
 	// baData = ((MeasuredBioAssayImpl) ba).
 	// getMeasuredBioAssayData();
 	// } else if (ba instanceof DerivedBioAssayImpl) {
 	// baData = ((DerivedBioAssayImpl) ba).
 	// getDerivedBioAssayData();
 	// } else {
 	// baData = null;
 	// }
 	// } catch (Exception e) {
 	// return null;
 	// }
 	// flag = true;
 	// return baData;
 	// }
 	//
 	// /**
 	// * Return the display name for the bioassay.
 	// */
 	// public String toString() {
 	// if (ba == null) {
 	// return "null";
 	// } else if (ba.getName() != null) {
 	// return ba.getName();
 	// } else if (ba instanceof MeasuredBioAssayImpl) {
 	// StringBuffer name = new StringBuffer("Measured");
 	// if (m_id != null) {
 	// name.append(": ");
 	// name.append(m_id);
 	// }
 	// return name.toString();
 	// } else if (ba instanceof DerivedBioAssayImpl) {
 	// StringBuffer name = new StringBuffer("Derived");
 	// if (m_id != null) {
 	// name.append(":");
 	// name.append(m_id);
 	// }
 	// return name.toString();
 	// } else {
 	// return "Unknown";
 	// }
 	// }
 	// }
 	//
 	/**
 	 * This class represents an experiment in the load data JTree.
 	 */
 	class CaArrayExperiment implements Comparable {
 		/**
 		 * The ID of this experiment.
 		 */
 		Long m_id = null;
 		/**
 		 * The name of this experiment.
 		 */
 		String m_name = null;
 		String m_platform = "Unknown";
 		/**
 		 * The reference <code>ExperimentImpl</code> object.
 		 */
 		// ExperimentImpl exp = null; //xq
 		/**
 		 * The number of measured Bioassays that are part of the experiment.
 		 */
 		private int measuredNum = 0;
 		/**
 		 * The number of derived Bioassays that are part of the experiment.
 		 */
 		private int derivedNum = 0;
 		/**
 		 * Convenience flag
 		 */
 		private boolean expInfoComputed = false;
 		/**
 		 * Convenience flags.
 		 */
 		private boolean hasBeenComputedFlag = false;
 		/**
 		 * The text of the experiment information
 		 */
 		private String experimentInfo = "";
 		/**
 		 * The list of measured bioassays in the experiment.
 		 */
 		// private CaArrayBioassay[] measuredAssays = null;
 		/**
 		 * The list of derived bioassays in the experiment.
 		 */
 		// private CaArrayBioassay[] derivedAssays = null;
 		/**
 		 * This is set to true when the tree node corresponding to the
 		 * experiment has been populated with the proper bioassays.
 		 */
 		private boolean treeNodePopulated = false;
 
 		/**
 		 * Creates a new experiment object that will wrap the specified
 		 * ExperimentImpl.
 		 * 
 		 * @param e -
 		 *            The ExperimentImpl that this object represents.
 		 */
 		// public CaArrayExperiment(ExperimentImpl e) {
 		// exp = e;
 		// //get the experiment id and name and store them locally
 		// try {
 		// m_id = e.getId();
 		// m_name = e.getName();
 		// //m_platform = e.getPlatformType;
 		// } catch (Exception ex) {
 		// System.out.println(
 		// "Error getting experiment information: " +
 		// ex.toString());
 		// ex.printStackTrace();
 		// }
 		// }
 		/**
 		 * Returns the ID of this experiment if known, -1 otherwise.
 		 */
 		public long getId() {
 			long id = -1;
 			if (m_id != null) {
 				id = m_id.longValue();
 			}
 			return id;
 		}
 
 		/**
 		 * Returns the platform of this experiment.
 		 */
 		public String getPlatform() {
 			return m_platform;
 		}
 
 		/**
 		 * Checks if the platform of this experiment is usable by caWorkbench.
 		 * 
 		 * @return true if the platform type is usable, false otherwise.
 		 */
 		public boolean isPlatformTypeUsable() {
 			boolean ret = false;
 			if (isGenepix() || isAffymetrix()) {
 				ret = true;
 			}
 			return ret;
 		}
 
 		/**
 		 * Checks if the experiment platform is Affymetrix.
 		 * 
 		 * @return <code>true</code> if the experiment platform is Affy.
 		 *         <code>false</code> otherwise.
 		 */
 		public boolean isAffymetrix() {
 			boolean ret = false;
 			if (m_platform.equalsIgnoreCase("Affymetrix")
 					|| m_platform.equalsIgnoreCase("Affy-MAS 5.0")) {
 				ret = true;
 			}
 			return ret;
 		}
 
 		/**
 		 * Checks if the experiment platform is Genepix.
 		 * 
 		 * @return <code>true</code> if the experiment platform is Genepix.
 		 *         <code>false</code> otherwise.
 		 */
 		public boolean isGenepix() {
 			boolean ret = false;
 			if (m_platform.equalsIgnoreCase("Long Oligo - Spotted")
 					|| m_platform.equalsIgnoreCase("cDNA - Spotted")) {
 				ret = true;
 			}
 			return ret;
 		}
 
 		/**
 		 * Return the number of measured bioassays in the experiment.
 		 * 
 		 * @return
 		 */
 		public int getMeasuredBioassaysCount() {
 			computeArrayNum();
 			return measuredNum;
 		}
 
 		/**
 		 * Return the number of derived bioassays in the experiment.
 		 * 
 		 * @return
 		 */
 		public int getDerivedBioassaysCount() {
 			computeArrayNum();
 			return derivedNum;
 		}
 
 		/**
 		 * Returns the experiment name, which will be displayed within the
 		 * experiment tree. This method is impicitly invoked by the
 		 * <code>DefaultMutableTreeNode</code> "hosting" this experiment
 		 * within the experiment tree.
 		 * 
 		 * @return The name of this experiment.
 		 */
 		public String toString() {
 			StringBuffer nameBuff = new StringBuffer();
 			if (m_id != null) {
 				nameBuff.append(m_id);
 				nameBuff.append(": ");
 			}
 
 			if (m_name != null) {
 				nameBuff.append(m_name);
 			}
 			nameBuff.append(" - ");
 			nameBuff.append(getPlatform());
 			return nameBuff.toString();
 		}
 
 		/**
 		 * This method will compare this Experiment to another object.
 		 * 
 		 * @param rhs -
 		 *            The object to compare to.
 		 * @return 0 if the objects are equal or cannot be compared, negative
 		 *         int if this object has an ID less than that of rhs object,
 		 *         positive otherwise.
 		 */
 		public int compareTo(Object rhs) {
 			int ret = 0;
 			if (rhs instanceof CaArrayExperiment) {
 				ret = (int) (getId() - ((CaArrayExperiment) rhs).getId());
 			}
 			return ret;
 		}
 
 		/**
 		 * Returns the wrapped ExperimentImpl object.
 		 */
 		// public ExperimentImpl getExperiment() {
 		// return exp;
 		// }
 		/**
 		 * Return the information associated with this experiment.
 		 * 
 		 * @return
 		 */
 		public String getExperimentInformation() {
 			// if (exp != null && !expInfoComputed) {
 			// Description[] desc = exp.getDescriptions();
 			// if (desc == null || desc.length == 0) {
 			// experimentInfo = "";
 			// } else {
 			// return experimentInfo = desc[0].getText();
 			// }
 			// expInfoComputed = true;
 			// }
 			return experimentInfo;
 		}
 
 		/**
 		 * Return an array containing the measured bioassays (if there are any).
 		 * 
 		 * @return
 		 */
 		// public CaArrayBioassay[] getMeasuredAssays() {
 		// computeArrayNum();
 		// return measuredAssays;
 		// }
 		/**
 		 * Return an array containing the derived bioassays (if there are any).
 		 * 
 		 * @return
 		 */
 		// public CaArrayBioassay[] getDerivedAssays() {
 		// computeArrayNum();
 		// return derivedAssays;
 		// }
 		public boolean hasBeenPopulated() {
 			return treeNodePopulated;
 		}
 
 		public void setPopulated() {
 			treeNodePopulated = true;
 		}
 
 		/**
 		 * Compute the number of measured and derived bioassays in the
 		 * experiment.
 		 */
 		private void computeArrayNum() {
 			// No need for repeated computation.
 			// if (hasBeenComputedFlag || exp == null) {
 			// return;
 			// }
 			// try {
 			// measuredNum = derivedNum = 0;
 			// BioAssay[] bioassays = exp.getBioAssays();
 			// if (bioassays.length > 0) {
 			// for (int j = 0; j < bioassays.length; ++j) {
 			// if (bioassays[j] instanceof
 			// MeasuredBioAssayImpl) {
 			// measuredNum++;
 			// } else if (bioassays[j] instanceof
 			// DerivedBioAssayImpl) {
 			// derivedNum++;
 			// }
 			// }
 			// }
 			//
 			// // Allocate space for the assays.
 			// if (bioassays.length > 0) {
 			// measuredAssays = new CaArrayBioassay[measuredNum];
 			// derivedAssays = new CaArrayBioassay[derivedNum];
 			// int k = 0, l = 0;
 			// for (int j = 0; j < bioassays.length; ++j) {
 			// if (bioassays[j] instanceof
 			// MeasuredBioAssayImpl) {
 			// measuredAssays[k++] = new CaArrayBioassay(
 			// bioassays[
 			// j]);
 			// } else if (bioassays[j] instanceof
 			// DerivedBioAssayImpl) {
 			// derivedAssays[l++] = new CaArrayBioassay(
 			// bioassays[
 			// j]);
 			// }
 			// }
 			// }
 			// hasBeenComputedFlag = true;
 			// } catch (Exception e) {
 			// measuredNum = derivedNum = 0;
 			// measuredAssays = derivedAssays = null;
 			// }
 		}
 	}
 
 	public boolean isConnectionSuccess() {
 		return connectionSuccess;
 	}
 
 	public int getPortnumber() {
 		return portnumber;
 	}
 
 	public void setPortnumber(int portnumber) {
 		this.portnumber = portnumber;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public String getPasswd() {
 		return passwd;
 	}
 
 	public String getCurrentResourceName() {
 		return currentResourceName;
 	}
 
 	public LoadData getParentPanel() {
 		return parentPanel;
 	}
 
 	public boolean isExperimentsLoaded() {
 		return experimentsLoaded;
 	}
 
 	public boolean isMerge() {
 		return merge;
 	}
 
 	public void setConnectionSuccess(boolean isConnectionSuccess) {
 		this.connectionSuccess = isConnectionSuccess;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public void setPasswd(String passwd) {
 		this.passwd = passwd;
 	}
 
 	public void setCurrentResourceName(String currentResourceName) {
 		this.currentResourceName = currentResourceName;
 	}
 
 	public void setExperiments(
 			org.geworkbench.builtin.projects.util.CaARRAYPanel.CaArrayExperiment[] experiments) {
 		this.experiments = experiments;
 		if (experiments == null) {
 			root = new DefaultMutableTreeNode("caARRAY experiments");
 			remoteTreeModel.setRoot(root);
 			repaint();
 		}
 	}
 
 	// public void setCaExperiments(Experiment[] experiment) {
 	// if (experiment == null || experiment.length == 0) {
 	// JOptionPane.showMessageDialog(null, "0 result retrieved.", "0
 	// experiment", JOptionPane.ERROR_MESSAGE);
 	// return;
 	// }
 	// CaArrayExperiment[] newExperiments = new
 	// CaArrayExperiment[experiment.length];
 	// for (int x = 0; x < experiment.length; x++) {
 	// if (experiment[x] instanceof ExperimentImpl) {
 	// CaArrayExperiment exp = new CaArrayExperiment((
 	// ExperimentImpl) experiment[x]);
 	// // pb.updateTo(x / totalexpNum * model.getMaximum());
 	//
 	// newExperiments[x] = exp;
 	//
 	// }
 	// experiments = newExperiments;
 	// jScrollPane1.getViewport().removeAll();
 	// DefaultMutableTreeNode queryResultRoot = new DefaultMutableTreeNode(
 	// "selected caARRAY experiments");
 	// for (int i = 0; i < experiments.length; ++i) {
 	// DefaultMutableTreeNode node = new DefaultMutableTreeNode(
 	// experiments[i]);
 	// remoteTreeModel.insertNodeInto(node, queryResultRoot,
 	// queryResultRoot.getChildCount());
 	// }
 	// remoteFileTree.expandRow(0);
 	// experimentsLoaded = true;
 	// previousResourceName = currentResourceName;
 	// connectionSuccess = true;
 	// //done = true;
 	//
 	// remoteTreeModel = new DefaultTreeModel(queryResultRoot);
 	// remoteFileTree.setModel(remoteTreeModel);
 	// //remoteFileTree = new JTree(remoteTreeModel);
 	// jScrollPane1.getViewport().add(remoteFileTree, null);
 	// revalidate();
 	// repaint();
 	// }
 	// }
 
 	public void setParentPanel(LoadData parentPanel) {
 		this.parentPanel = parentPanel;
 	}
 
 	public void setExperimentsLoaded(boolean experimentsLoaded) {
 		this.experimentsLoaded = experimentsLoaded;
 	}
 
 	public void setMerge(boolean merge) {
 		this.merge = merge;
 	}
 }
