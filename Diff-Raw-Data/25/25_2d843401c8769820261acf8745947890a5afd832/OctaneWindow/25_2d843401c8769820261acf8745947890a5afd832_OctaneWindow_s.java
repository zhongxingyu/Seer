 package edu.uchc.octane;
 
 import ij.IJ;
 import ij.gui.GenericDialog;
 
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JLabel;
 import javax.swing.JSplitPane;
 import javax.swing.JScrollPane;
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import java.awt.GridBagLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.GridBagConstraints;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import bsh.Interpreter;
 
 /**
  * The main octane window
  */
 public class OctaneWindow extends JFrame {
 
 	private OctaneWindowControl ctr_ = null;
 	private JSplitPane contentPane_;
 	private TrajsTable trajsTable_;
 	private NodesTable nodesTable_;
 
 	/**
 	 * Create the frame.
 	 */
 	public OctaneWindow() {
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 740, 505);
 		
 		////////////////////////////
 		// Setup Menu
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		// -- File Menu --
 		
 		JMenu mnFile = new JMenu("File    ");
 		mnFile.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmSave = new JMenuItem("Save");
 		mntmSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.saveDataset(); 
 			}
 		});
 		mnFile.add(mntmSave);
 		
 		JMenuItem mntmRebuild = new JMenuItem("Rebuild");
 		mntmRebuild.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 				ctr_.rebuildTrajectories();
 				if (isDisplayable()) {
 					setVisible(true);
 				}
 			}
 		});
 		mnFile.add(mntmRebuild);
 		
 		mnFile.addSeparator();
 		
 		JMenuItem mntmExportNodes = new JMenuItem("Export Nodes");
 		mntmExportNodes.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					JFileChooser fc = new JFileChooser();
 					if (fc.showSaveDialog(ctr_.getWindow()) == JFileChooser.APPROVE_OPTION) {
 						ctr_.exportTrajectories(fc.getSelectedFile());
 					}
 				} catch (IOException err) {
					IJ.showMessage("Can't save file! " + err.getMessage()); 
 				}
 			}
 		});
 		mnFile.add(mntmExportNodes);
 		
 		JMenuItem mntmExportTrajectories = new JMenuItem("Export Trajectories");
 		mntmExportTrajectories.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					JFileChooser fc = new JFileChooser();
 					if (fc.showSaveDialog(ctr_.getWindow()) == JFileChooser.APPROVE_OPTION) {
 						ctr_.exportNodes(fc.getSelectedFile());
 					}
 				} catch (IOException err) {
					IJ.showMessage("Can't save file! " + err.getMessage()); 
 				}
 			}
 		});
 		mnFile.add(mntmExportTrajectories);
 		
 		// -- Edit Menu --
 		
 		JMenu mnEdit = new JMenu("Edit    ");
 		mnEdit.setMnemonic(KeyEvent.VK_E);
 		menuBar.add(mnEdit);
 		
 		JMenuItem mntmDelete = new JMenuItem("Delete");
 		mntmDelete.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.deleteSelectedTrajectories();
 				trajsTable_.clearSelection();
 				trajsTable_.tableDataChanged();
 			}
 		});
 		
 		JMenuItem mntmCopy = new JMenuItem("Copy");
 		mntmCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.copySelectedTrajectories();
 			}
 		});
 		mnEdit.add(mntmCopy);
 		mnEdit.add(mntmDelete);
 		
 		JMenuItem mntmToggleMarked = new JMenuItem("Toggle Marked");
 		mntmToggleMarked.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.reverseMarkOfSelected();
 			}
 		});
 		mnEdit.add(mntmToggleMarked);
 		
 		mnEdit.addSeparator();
 		
 		JMenuItem mntmSelectRoi = new JMenuItem("Select ROI");
 		mntmSelectRoi.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.getSelectionModel().setValueIsAdjusting(true);
 				ctr_.selectTrajectoriesWithinRoi();
 				trajsTable_.getSelectionModel().setValueIsAdjusting(false);
 			}
 		});
 		mnEdit.add(mntmSelectRoi);
 		
 		// -- View Menu --
 		
 		JMenu mnView = new JMenu("View    ");
 		mnView.setMnemonic(KeyEvent.VK_V);
 		menuBar.add(mnView);
 		
 		JCheckBoxMenuItem chckbxmntmHideUnmarked = new JCheckBoxMenuItem("Hide Unmarked");
 		chckbxmntmHideUnmarked.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
 				if (cb.getState() == true) {
 					trajsTable_.hideUnmarked();
 				} else {
 					trajsTable_.showAll();
 				}
 			}
 		});
 		mnView.add(chckbxmntmHideUnmarked);
 		
 		JCheckBoxMenuItem chckbxmntmShowOverlay = new JCheckBoxMenuItem("Show Overlay");
 		chckbxmntmShowOverlay.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
 				GlobalPrefs.showOverlay_ = cb.getState();
 				ctr_.drawOverlay();
 			}
 		});
 		mnView.add(chckbxmntmShowOverlay);
 		
 		// -- Analysis Menu --
 		
 		JMenu mnAnalysis = new JMenu("Analysis    ");
 		mnAnalysis.setMnemonic(KeyEvent.VK_A);
 		menuBar.add(mnAnalysis);
 		
 		JMenuItem mntmTraceLengthDistribution = new JMenuItem("Trajectory Length Histogram");
 		mntmTraceLengthDistribution.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.showLengthHistogram();
 			}
 		});
 		mnAnalysis.add(mntmTraceLengthDistribution);
 		
 		JMenuItem mntmDisplacementDistribution = new JMenuItem("Displacement Histogram");
 		mntmDisplacementDistribution.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				if (DspHistogramParameters.showDialog()) {
 					ctr_.showDisplacementHistogram(DspHistogramParameters.stepSize_);					
 				}
 				
 			}
 		});
 		mnAnalysis.add(mntmDisplacementDistribution);
 		
 		JMenuItem mntmMsd = new JMenuItem("MSD");
 		mntmMsd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				GenericDialog gd = new GenericDialog("MSD parameters");
 				gd.addNumericField("Maximum time steps", 20, 0);
 				gd.showDialog();
 				if (gd.wasCanceled())
 					return;
 				int maxSteps = (int) gd.getNextNumber();
 				ctr_.showMSD(maxSteps);
 			}
 		});
 		mnAnalysis.add(mntmMsd);
 		
 		JMenuItem mntmResidueHistogram = new JMenuItem("Residue Histogram");
 		mntmResidueHistogram.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.showResidueHistogram();
 			}
 		});
 		mnAnalysis.add(mntmResidueHistogram);
 		
 		// -- Image Menu --
 		
 		JMenu mnImage = new JMenu("Image    ");
 		mnImage.setMnemonic(KeyEvent.VK_I);
 		menuBar.add(mnImage);
 		
 		JMenuItem mntmComputeDrift = new JMenuItem("Compute Drift");
 		mntmComputeDrift.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				ctr_.computeDrift();
 			}
 		});
 		mnImage.add(mntmComputeDrift);
 		
 		JMenuItem mntmImportDriftData = new JMenuItem("Import Drift Data ...");
 		mntmImportDriftData.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				// FIX IT
 			}
 		});
 		mnImage.add(mntmImportDriftData);
 		
 		JCheckBoxMenuItem mntmApplyDriftCompensation = new JCheckBoxMenuItem("Apply Drift Compensation");
 		mntmApplyDriftCompensation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
 				GlobalPrefs.compensateDrift_ = cb.getState();
 			}
 		});
 		mnImage.add(mntmApplyDriftCompensation);
 		
 		mnImage.addSeparator();
 		
 		JMenuItem mntmPalm = new JMenuItem("PALM");
 		mntmPalm.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.constructPalm();
 			}
 		});
 		mnImage.add(mntmPalm);
 		
 //		JMenuItem mntmIfs = new JMenuItem("IFS");
 //		mntmIfs.addActionListener(new ActionListener() {
 //			public void actionPerformed(ActionEvent e){
 //				ctlr_.constructIFS();
 //			}
 //		});
 //		mnImage.add(mntmIfs);
 		
 //		JMenuItem mntmFlowMap = new JMenuItem("Flow Map");
 //		mntmFlowMap.addActionListener(new ActionListener() {
 //			public void actionPerformed(ActionEvent e){
 //				ctlr_.constructFlowMap();
 //			}
 //		});
 //		mnImage.add(mntmFlowMap);
 		
 		JMenuItem mntmMobilityMap = new JMenuItem("Mobility Map");
 		mntmMobilityMap.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				ctr_.constructMobilityMap();
 			}
 		});
 		mnImage.add(mntmMobilityMap);
 		
 		// -- Tools Menu --
 		
 		JMenu mnTools = new JMenu("Tools    ");
 		mnTools.setMnemonic(KeyEvent.VK_T);
 		menuBar.add(mnTools);
 		
 		JMenuItem mntmRunScript = new JMenuItem("Run Script...");
 		mntmRunScript.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				if (fc.showOpenDialog(ctr_.getWindow()) == JFileChooser.APPROVE_OPTION) {
 					Interpreter bsh = new Interpreter();
 					try {
 						bsh.set("octaneData", ctr_.getData());
 						bsh.source(fc.getSelectedFile().getPath());
 					} catch (Exception e1) {
 						IJ.log(e1.toString());
 					}
 					trajsTable_.tableDataChanged();
 				}
 			}
 		});
 		mnTools.add(mntmRunScript);
 
 		contentPane_ = new JSplitPane();
 		contentPane_.setResizeWeight(0.5);
 		setContentPane(contentPane_);
 		
 		////////////////////////////
 		// Setup Trajectory Table
 
 		JPanel leftPanel = new JPanel();
 		leftPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
 		contentPane_.setLeftComponent(leftPanel);
 		GridBagLayout gbl_leftPanel = new GridBagLayout();
 		gbl_leftPanel.columnWidths = new int[]{0, 0};
 		gbl_leftPanel.rowHeights = new int[]{0, 0, 0, 0};
 		gbl_leftPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_leftPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
 		leftPanel.setLayout(gbl_leftPanel);
 		
 		JLabel lPaneLabel = new JLabel("List of Trajectories:");
 		lPaneLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
 		GridBagConstraints gbc_lPaneLabel = new GridBagConstraints();
 		gbc_lPaneLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_lPaneLabel.anchor = GridBagConstraints.WEST;
 		gbc_lPaneLabel.gridx = 0;
 		gbc_lPaneLabel.gridy = 0;
 		leftPanel.add(lPaneLabel, gbc_lPaneLabel);
 		
 		JScrollPane trajListScrollPane = new JScrollPane();
 		trajListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		GridBagConstraints gbc_trajListScrollPane = new GridBagConstraints();
 		gbc_trajListScrollPane.fill = GridBagConstraints.BOTH;
 		gbc_trajListScrollPane.gridx = 0;
 		gbc_trajListScrollPane.gridy = 1;
 		leftPanel.add(trajListScrollPane, gbc_trajListScrollPane);
 		
 		trajsTable_ = new TrajsTable((TrajDataset) null);
 		trajsTable_.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2 ) {
 					ctr_.animate();
 				}
 			}
 		});
 		trajsTable_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				ctr_.stopAnimation();
 				
 				if (e.getValueIsAdjusting()) {
 					return;
 				}
 				
 				ctr_.getImp().killRoi();
 				
 				if (nodesTable_ != null) {
 					populateNodesTable();
 				}
 			}
 		});
 		trajsTable_.getModel().addTableModelListener(new TableModelListener() {
 			@Override
 			public void tableChanged(TableModelEvent e) {
 				ctr_.drawOverlay();
 			}
 		});		
 		trajListScrollPane.setViewportView(trajsTable_);
 		
 		JPanel buttonBox = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) buttonBox.getLayout();
 		flowLayout.setHgap(10);
 		GridBagConstraints gbc_buttonBox = new GridBagConstraints();
 		gbc_buttonBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_buttonBox.anchor = GridBagConstraints.SOUTH;
 		gbc_buttonBox.gridx = 0;
 		gbc_buttonBox.gridy = 2;
 		leftPanel.add(buttonBox, gbc_buttonBox);
 		
 		JButton btnMark = new JButton("Toggle Mark");
 		btnMark.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.reverseMarkOfSelected();
 			}
 		});
 		btnMark.setToolTipText("Mark/Unmark selection");
 		buttonBox.add(btnMark);
 		
 		JButton btnCopy = new JButton("Copy");
 		btnCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ctr_.copySelectedTrajectories();
 			}
 		});
 		btnCopy.setToolTipText("Copy selected tracks to clipboard");
 		buttonBox.add(btnCopy);
 
 		////////////////////////////
 		// Setup Node Table
 
 		JPanel rightPanel = new JPanel();
 		rightPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
 		contentPane_.setRightComponent(rightPanel);
 		GridBagLayout gbl_rightPanel = new GridBagLayout();
 		gbl_rightPanel.columnWidths = new int[]{0, 0};
 		gbl_rightPanel.rowHeights = new int[]{0, 0, 0};
 		gbl_rightPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_rightPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		rightPanel.setLayout(gbl_rightPanel);
 		
 		JLabel rPanelLabel = new JLabel("Current Trajectory:");
 		rPanelLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
 		GridBagConstraints gbc_rPanelLabel = new GridBagConstraints();
 		gbc_rPanelLabel.anchor = GridBagConstraints.WEST;
 		gbc_rPanelLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_rPanelLabel.gridx = 0;
 		gbc_rPanelLabel.gridy = 0;
 		rightPanel.add(rPanelLabel, gbc_rPanelLabel);
 		
 		JScrollPane nodesTableScrollPane = new JScrollPane();
 		nodesTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		GridBagConstraints gbc_nodesTableScrollPane = new GridBagConstraints();
 		gbc_nodesTableScrollPane.fill = GridBagConstraints.BOTH;
 		gbc_nodesTableScrollPane.gridx = 0;
 		gbc_nodesTableScrollPane.gridy = 1;
 		rightPanel.add(nodesTableScrollPane, gbc_nodesTableScrollPane);
 		
 		nodesTable_ = new NodesTable((Trajectory) null);
 		nodesTable_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (!e.getValueIsAdjusting())
 					ctr_.drawBox();
 			}
 		} );
 		nodesTableScrollPane.setViewportView(nodesTable_);
 	}
 	
 	public TrajsTable getTrajsTable() {
 		return trajsTable_;
 	}
 	
 	public NodesTable getNodesTable() {
 		return nodesTable_;
 	}
 
 	public OctaneWindowControl getController() {
 		return ctr_;
 	}
 	
 	public void setController(OctaneWindowControl ctlr) {
 		ctr_  = ctlr;
 		trajsTable_.setData(ctlr.getData());
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.Window#dispose()
 	 */
 	public void dispose() {
 		this.setVisible(false);
 		if (ctr_ != null ) {
 			ctr_.saveDataset();
 		}
 		GlobalPrefs.savePrefs();
 		super.dispose();
 	}
 	
 	/**
 	 * Select a trajectory in the trajectory table by its index.
 	 *
 	 * @param index the index
 	 */
 	public void selectTrajectoryByIndex(int index) {
 		selectTrajectoryAndNodeByIndex(index, 0);
 	}
 
 	/**
 	 * Select a trajectory and a node.
 	 *
 	 * @param index the index of the trajectory
 	 * @param nodeIndex the index of the node
 	 */
 	public void selectTrajectoryAndNodeByIndex(int trajIndex, int nodeIndex) {
 		if (trajIndex < 0) {
 			trajsTable_.clearSelection();
 			return;
 		}
 		nodesTable_.getSelectionModel().setValueIsAdjusting(true);
 		int row = trajsTable_.convertRowIndexToView(trajIndex);
 		trajsTable_.setRowSelectionInterval(row,row);
 		Rectangle r = trajsTable_.getCellRect(row, 0, true);
 		trajsTable_.scrollRectToVisible(r);
 		nodesTable_.setRowSelectionInterval(nodeIndex,nodeIndex);
 		nodesTable_.getSelectionModel().setValueIsAdjusting(true);
 	}
 	
 	/**
 	 * Adds a trajectories to the current selection in the trajectory table.
 	 *
 	 * @param index the index
 	 */
 	public void addTrajectoriesToSelection(int index) {
 		int row = trajsTable_.convertRowIndexToView(index);
 		trajsTable_.addRowSelectionInterval(row,row);
 	}
 	
 	private void populateNodesTable() {
 		if (trajsTable_.getSelectedRowCount() > 0) {
 			int index = trajsTable_.getSelectedTrajectoryIndex();
 			nodesTable_.setData(ctr_.getData().getTrajectoryByIndex(index));
 		} else {
 			nodesTable_.setData(null);
 		}
 		
 		if (trajsTable_.getSelectedRowCount() == 1) { 
 			nodesTable_.setRowSelectionInterval(0,0);
 		}		
 	}
 }
