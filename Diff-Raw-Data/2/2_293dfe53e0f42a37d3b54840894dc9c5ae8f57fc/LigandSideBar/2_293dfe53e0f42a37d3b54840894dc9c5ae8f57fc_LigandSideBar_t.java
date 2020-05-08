 /*
  * BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence. This should
  * be distributed with the code. If you do not have a copy,
  * see:
  *
  * http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors. These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  * http://www.biojava.org/
  *
  * This code was contributed from the Molecular Biology Toolkit
  * (MBT) project at the University of California San Diego.
  *
  * Please reference J.L. Moreland, A.Gramada, O.V. Buzko, Qing
  * Zhang and P.E. Bourne 2005 The Molecular Biology Toolkit (MBT):
  * A Modular Platform for Developing Molecular Visualization
  * Applications. BMC Bioinformatics, 6:21.
  *
  * The MBT project was funded as part of the National Institutes
  * of Health PPG grant number 1-P01-GM63208 and its National
  * Institute of General Medical Sciences (NIGMS) division. Ongoing
  * development for the MBT project is managed by the RCSB
  * Protein Data Bank(http://www.pdb.org) and supported by funds
  * from the National Science Foundation (NSF), the National
  * Institute of General Medical Sciences (NIGMS), the Office of
  * Science, Department of Energy (DOE), the National Library of
  * Medicine (NLM), the National Cancer Institute (NCI), the
  * National Center for Research Resources (NCRR), the National
  * Institute of Biomedical Imaging and Bioengineering (NIBIB),
  * the National Institute of Neurological Disorders and Stroke
  * (NINDS), and the National Institute of Diabetes and Digestive
  * and Kidney Diseases (NIDDK).
  *
  * Created on 2008/12/22
  *
  */ 
 package org.rcsb.lx.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.rcsb.lx.controllers.app.LigandExplorer;
 import org.rcsb.lx.model.InteractionConstants;
 import org.rcsb.mbt.model.Chain;
 import org.rcsb.mbt.model.Residue;
 import org.rcsb.mbt.model.Structure;
 import org.rcsb.mbt.model.StructureComponent;
 import org.rcsb.mbt.model.StructureModel;
 import org.rcsb.mbt.model.util.Status;
 import org.rcsb.uiApp.controllers.app.AppBase;
 
 public class LigandSideBar extends JPanel
 {
 	private final class InteractionListener implements ActionListener {
 		private final JCheckBox box;
 		private final JCheckBox hydrogenBondBox;
 		private final JCheckBox hydrophobicBox;
 		private final JCheckBox metalInteractionBox;
 		private final JCheckBox neighborBox;
 		private final JCheckBox distanceBox;
 		private final FloatLimitField l_h2o_pflf2;
 		private final FloatLimitField philicFLF2;
 		private final FloatLimitField phobicFLF2;
 		private final FloatLimitField otherFLF2;
 		private final FloatLimitField neighborFLF1;
 		
 
 		private InteractionListener(JCheckBox box,
 				JCheckBox hydrogenBondBox, JCheckBox hydrophobicBox,
 				JCheckBox metalInteractionBox, JCheckBox neighborBox, JCheckBox distanceBox,
 				FloatLimitField l_h2o_pflf2, FloatLimitField philicFLF2,
 				FloatLimitField phobicFLF2, FloatLimitField otherFLF2,
 				FloatLimitField neighborFLF1) {
 			this.box = box;
 			this.hydrogenBondBox = hydrogenBondBox;
 			this.hydrophobicBox = hydrophobicBox;
 			this.metalInteractionBox = metalInteractionBox;
 			this.neighborBox = neighborBox;
 			this.distanceBox = distanceBox;
 			this.l_h2o_pflf2 = l_h2o_pflf2;
 			this.philicFLF2 = philicFLF2;
 			this.phobicFLF2 = phobicFLF2;
 			this.otherFLF2 = otherFLF2;
 			this.neighborFLF1 = neighborFLF1;
 		}
 
 		public void actionPerformed(final ActionEvent ae) {
 			final boolean saveInteractionsFlag = LigandExplorer.saveInteractionsFlag;
 			LigandExplorer.saveInteractionsFlag = false;
 
 			final StructureModel model = LigandExplorer.sgetModel();
 			//			final LXGlGeometryViewer glViewer = LigandExplorer.sgetGlGeometryViewer();
 
 			// **J check to make sure all text fields have three
 			// characters in them; if not, make the correction
 			String philicText2 = philicFLF2.getText();
 			switch (philicText2.length()) {
 			case 0:
 				philicText2 = "0.0";
 				philicFLF2.setText(philicText2);
 				break;
 			case 1:
 				philicText2 = philicText2 + ".0";
 				philicFLF2.setText(philicText2);
 				break;
 			case 2:
 				philicText2 = philicText2 + "0";
 				philicFLF2.setText(philicText2);
 			}
 
 			String phobicText2 = phobicFLF2.getText();
 			switch (phobicText2.length()) {
 			case 0:
 				phobicText2 = "0.0";
 				phobicFLF2.setText(phobicText2);
 				break;
 			case 1:
 				phobicText2 = phobicText2 + ".0";
 				phobicFLF2.setText(phobicText2);
 				break;
 			case 2:
 				phobicText2 = phobicText2 + "0";
 				phobicFLF2.setText(phobicText2);
 			}
 
 			String l_h2o_pText2 = l_h2o_pflf2.getText();
 			switch (l_h2o_pText2.length()) {
 			case 0:
 				l_h2o_pText2 = "0.0";
 				l_h2o_pflf2.setText(l_h2o_pText2);
 				break;
 			case 1:
 				l_h2o_pText2 = l_h2o_pText2 + ".0";
 				l_h2o_pflf2.setText(l_h2o_pText2);
 				break;
 			case 2:
 				l_h2o_pText2 = l_h2o_pText2 + "0";
 				l_h2o_pflf2.setText(l_h2o_pText2);
 			}
 
 			String otherText2 = otherFLF2.getText();
 			switch (otherText2.length()) {
 			case 0:
 				otherText2 = "0.0";
 				otherFLF2.setText(otherText2);
 				break;
 			case 1:
 				otherText2 = otherText2 + ".0";
 				otherFLF2.setText(otherText2);
 				break;
 			case 2:
 				otherText2 = otherText2 + "0";
 				otherFLF2.setText(otherText2);
 			}
 			String neighborText1 = neighborFLF1.getText();
 			switch (neighborText1.length()) {
 			case 0:
 				neighborText1 = "0.0";
 				neighborFLF1.setText(neighborText1);
 				break;
 			case 1:
 				neighborText1 = neighborText1 + ".0";
 				neighborFLF1.setText(neighborText1);
 				break;
 			case 2:
 				neighborText1 = neighborText1 + "0";
 				neighborFLF1.setText(neighborText1);
 			}
 
 			if (model.hasStructures())
 			{
 				final Structure structure = model.getStructures().get(0);
 
 				final TreePath treeSelection[] = ligandJList.getSelectionPaths();
 				if (treeSelection != null)
 				{
 					Residue residues[] = null;
 					DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeSelection[0].getLastPathComponent();
 					StructureComponent nodeValue = (StructureComponent)node.getUserObject();
 					if (nodeValue instanceof Chain)
 					{
 						assert(treeSelection.length == 1);
 						Chain chain = (Chain) nodeValue;
 						residues = new Residue[chain.getResidueCount()];
 						for (int ix = 0; ix < chain.getResidueCount(); ix++)
 							residues[ix] = chain.getResidue(ix);
 					}
 
 					else 
 					{
 						int ix = 0;
 						residues = new Residue[treeSelection.length];
 
 						for (TreePath path : treeSelection)
 						{
 							node = (DefaultMutableTreeNode)path.getLastPathComponent();
 							nodeValue = (StructureComponent)node.getUserObject();
 							assert(nodeValue instanceof Residue);
 							residues[ix] = (Residue)nodeValue;
 						}
 
 						LigandExplorer.sgetSceneController().setLigandResidues(residues);
 					}
 				}
 
 				final float philicf2 = Float
 				.parseFloat(philicText2);
 				final float phobicf2 = Float
 				.parseFloat(phobicText2);
 				final float l_h2o_pf2 = Float.parseFloat(l_h2o_pText2);
 				final float otherf2 = Float.parseFloat(otherText2);
 				final float neighborf1 = Float.parseFloat(neighborText1);
 
 
 				// Thread runner = new Thread() {
 				//
 				// public void run() {
 				try
 				{
 					LigandExplorer.sgetSceneController().processLeftPanelEvent(structure,
 							l_h2o_pf2,
 							box.isSelected(),
 							hydrogenBondBox.isSelected(),
 							philicf2,
 							hydrophobicBox.isSelected(),
 							phobicf2,
 							metalInteractionBox.isSelected(),
 							otherf2,
 							distanceBox.isSelected(),
 							neighborf1, 
 							neighborBox.isSelected(),
 							saveInteractionsFlag);
 				}
 
 				catch (final Exception e)
 				{
 					e.printStackTrace();
 					Status.progress(100, "Error while processing your options...Please select different options");
 				}
 				// }
 				// };
 				// runner.start();
 				
 				// remove current surfaces from display list and add them again to ensure
 				// transparent surfaces are drawn on top of the interactions
 //			System.out.println("LigandSideBar: redraw surfaces");
 				LigandExplorer.sgetGlGeometryViewer().surfaceRemoved(structure);
 				LigandExplorer.sgetGlGeometryViewer().surfaceAdded(structure);	
 			}
 
 			else
 			{
 				// add a display message here, ask user to load a
 				// structure, do this later
 			}
 		}
 	}
 
 	private class LigandTreeCellRenderer extends DefaultTreeCellRenderer
 	{
 		private static final long serialVersionUID = -5623805877904497060L;
 
 		private final ImageIcon residueIcon = new ImageIcon(this.getClass()
 				.getResource("residue_16.jpg"));
 
 		private final ImageIcon chainIcon = new ImageIcon(this.getClass()
 				.getResource("chain_16.jpg"));
 
 		@Override
 		public Component getTreeCellRendererComponent(final JTree tree,
 				final Object value, final boolean selected,
 				final boolean expanded, final boolean leaf, final int row,
 				final boolean hasFocus)
 		{
 			final LigandTreeCellRenderer component = (LigandTreeCellRenderer) super
 			.getTreeCellRendererComponent(tree, value, selected,
 					expanded, leaf, row, hasFocus);
 
 			ImageIcon imageIcon = null;
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
 
 			if (node.getUserObject() instanceof Chain)
 				imageIcon = chainIcon;
 
 			else if (node.getUserObject() instanceof Residue)
 				imageIcon = residueIcon;
 
 			setIcon(imageIcon);
 
 			return component;
 		}
 	}
 
 	private static final long serialVersionUID = 119898371139373941L;
 
 	private InteractionListener interactionListener;
 
 	Vector<Chain> ligandList = null;
 	public Vector<Chain> getLigandList() { return ligandList; }
 
 	JTree ligandJList = null;
 
 	public JButton applyButton = null;
 	public JTree getLigandJList () { return ligandJList; }
 	private JScrollPane ligandScroller;
 
 	public LigandSideBar(LXDocumentFrame mainFrame)
 	{
 		super();
 
 		final StructureModel model = LigandExplorer.sgetModel();
 
 		if (!model.hasStructures())
 		{
 			this.setBackground(LXDocumentFrame.sidebarColor);
 			this.setLayout(new BorderLayout());
 
 			final JPanel pdbPanel = new JPanel();
 			pdbPanel.setLayout(new FlowLayout());
 			pdbPanel.setBackground(LXDocumentFrame.sidebarColor);
 
 			final JLabel pdbLabel = new JLabel("PDB id");
 			pdbLabel.setFont(pdbLabel.getFont().deriveFont(Font.BOLD));
 			pdbLabel.setBackground(LXDocumentFrame.sidebarColor);
 
 			mainFrame.getPdbIdList().setBackground(LXDocumentFrame.sidebarColor);
 
 			pdbPanel.add(pdbLabel);
 			pdbPanel.add(mainFrame.getPdbIdList());
 
 			this.add(pdbPanel, BorderLayout.NORTH);
 
 		}
 
 		else
 		{
 			this.ligandList = this.getLigandList(model.getStructures().get(0));
 			
 			final Comparator<Chain> chainComparator = new Comparator<Chain>() {
 				public int compare(Chain c1, Chain c2) {
 					return c1.getAuthorChainId().compareTo(c2.getAuthorChainId());
 				}
 			};
 			Collections.sort(ligandList, chainComparator);
 
 			this.setLayout(null);
 
 			final JLabel centerView = new JLabel("Choose a ligand to analyze...");
 			centerView.setFont(centerView.getFont().deriveFont(
 					Font.BOLD + Font.ITALIC));
 			this.add(centerView);
 
 			if (this.ligandList.size() > 0)
 			{
 				DefaultMutableTreeNode root = new DefaultMutableTreeNode("Ligands:");
 				ligandJList = new JTree(root);
 				ligandJList.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
 				ligandJList.setCellRenderer(new LigandTreeCellRenderer());
 				ligandJList.setExpandsSelectedPaths(true);
 				ligandJList.setRootVisible(false);
 				ligandJList.setShowsRootHandles(true);
 
 				String chainId = "";
 				DefaultMutableTreeNode chainNode = null;
 		
 				for (Chain chain : ligandList)
 				{
 					// create new node only if chain id is different from previous chain id
 					if (! chain.getAuthorChainId().equals(chainId)) {
 						chainNode = new DefaultMutableTreeNode(chain);
 						root.add(chainNode);
 						chainId = chain.getAuthorChainId();
 					}
 					if (chain.getClassification() == Residue.Classification.LIGAND || chain.getClassification() == Residue.Classification.BIRD)
 						for (Residue residue : chain.getResidues())
 							chainNode.add(new DefaultMutableTreeNode(residue));
 					// add all the residues.
 
 					else if (chain.hasModifiedResidues())
 						for (Residue residue : chain.getModifiedResidues())
 							chainNode.add(new DefaultMutableTreeNode(residue));
 				}
 
 				ligandJList.getSelectionModel().addTreeSelectionListener(
 						new TreeSelectionListener()
 						{
 							public void valueChanged(TreeSelectionEvent e)
 							{
 							    applyButton.doClick();
 							    Structure structure = AppBase.sgetModel().getStructures().get(0);
 							    if (structure.getStructureMap().getSurfaceCount() > 0) {
 							    	LigandExplorer.sgetGlGeometryViewer().ligandViewWithSurface(structure);
 							    } else {
 							    	LigandExplorer.sgetGlGeometryViewer().ligandView(structure);
 							    }
 							}				
 						});
 
 				ligandScroller = new JScrollPane(this.ligandJList);
 
 				add(ligandScroller);
 			}
 
 			else
 				centerView.setText("No ligands in this structure");
 
 			final JLabel displayView = new JLabel("Choose interactions & thresholds...");
 			displayView.setFont(displayView.getFont().deriveFont(
 					Font.BOLD + Font.ITALIC));
 			this.add(displayView);
 
 			final JCheckBox hydrophilicBox = new JCheckBox(InteractionConstants.hydrogenBondType);
 			final LegendPanel hydrophilicPanel = new LegendPanel(InteractionConstants.hydrogenBondColor,
 					LXDocumentFrame.sidebarColor);
 			final FloatLimitField philicFLF2 = new FloatLimitField("3.3");
 			this.add(hydrophilicBox);
 			this.add(philicFLF2);
 			this.add(hydrophilicPanel);
 
 			final JCheckBox hydrophobicBox = new JCheckBox(InteractionConstants.hydrophobicType);
 			final LegendPanel hydrophobicPanel = new LegendPanel(InteractionConstants.hydrophobicBondColor,
 					LXDocumentFrame.sidebarColor);
 			final FloatLimitField phobicFLF2 = new FloatLimitField("3.9");
 			this.add(hydrophobicBox);
 			this.add(phobicFLF2);
 			this.add(hydrophobicPanel);
 
 			final JCheckBox l_h2o_pBox = new JCheckBox(InteractionConstants.waterMediatedType);
 			l_h2o_pBox.setBackground(LXDocumentFrame.sidebarColor);
 			final LegendPanel l_h2o_pPanel = new LegendPanel(InteractionConstants.waterMediatedColor,
 					LXDocumentFrame.sidebarColor);
 			l_h2o_pBox.setBackground(LXDocumentFrame.sidebarColor);
 			final FloatLimitField l_h2o_pFLF2 = new FloatLimitField("3.3");
 			this.add(l_h2o_pBox);
 			this.add(l_h2o_pFLF2);
 			this.add(l_h2o_pPanel);
 
 			final JCheckBox otherBox = new JCheckBox(InteractionConstants.metalInteractionType);
 			final LegendPanel otherPanel = new LegendPanel(InteractionConstants.metalInteractionColor,
 					LXDocumentFrame.sidebarColor);
 			final FloatLimitField otherFLF2 = new FloatLimitField("3.5");
 			this.add(otherBox);
 			this.add(otherFLF2);
 			this.add(otherPanel);
 
 			final JCheckBox neighborBox = new JCheckBox(InteractionConstants.neighborInteractionType);
 			final FloatLimitField neighborFLF1 = new FloatLimitField("4.0");
 			this.add(neighborBox);
 			this.add(neighborFLF1);
 
 			// new: added this separator
 			final JSeparator separator = new JSeparator();
 			this.add(separator);
 
 			final JCheckBox distanceBox = new JCheckBox("Label interactions by distance");
 			distanceBox.setBackground(LXDocumentFrame.sidebarColor);
 			distanceBox.setSelected(true);
 			this.add(distanceBox);
 			
 //			System.out.println("Adding BindingSiteSurfacePanel");
 			final JPanel surfacePanel = new BindingSiteSurfacePanel();
 			surfacePanel.setBackground(LXDocumentFrame.sidebarColor);
 			this.add(surfacePanel);
 			
 			// this button should eventually be removed. Its still used
 			// to fire some update events
 			applyButton = new JButton("Apply");
 			this.add(applyButton);
 
 			
 
 			this.setLayout(
 					new LayoutManager()
 					{
 						public void addLayoutComponent(String arg0, Component arg1) {}
 
 						private Dimension layoutSize = null;
 
 						public void layoutContainer(Container parent)
 						{
 							final int visualBuffer = 3;
 							Insets parentInsets = parent.getInsets();
 
 							if(ligandList == null || ligandList.size() == 0)
 							{
 								Dimension preferred = centerView.getPreferredSize();
 								centerView.setBounds(parentInsets.left + visualBuffer, parentInsets.top + visualBuffer, preferred.width, preferred.height);
 							}
 
 							else
 							{
 								Dimension step1Preferred = centerView.getPreferredSize();
 								Dimension step2Preferred = displayView.getPreferredSize();;
 								Dimension hydrophilicBoxPreferred = hydrophilicBox.getPreferredSize();
 								Dimension hydrophobicBoxPreferred = hydrophobicBox.getPreferredSize();
 								Dimension l_h2o_pBoxPreferred = l_h2o_pBox.getPreferredSize();
 								Dimension otherBoxPreferred = otherBox.getPreferredSize();
 								Dimension neighborBoxPreferred = neighborBox.getPreferredSize();
 								Dimension distancePreferred = distanceBox.getPreferredSize();
 								Dimension hydrophilicFLF2Preferred = philicFLF2.getPreferredSize();
 								Dimension hydrophobicFLF2Preferred = phobicFLF2.getPreferredSize();
 								Dimension l_h2o_pFLF2Preferred = l_h2o_pFLF2.getPreferredSize();
 								Dimension otherFLF2Preferred = otherFLF2.getPreferredSize();
 								Dimension neighborFLF1Preferred = neighborFLF1.getPreferredSize();
 								Dimension separatorPreferred = separator.getPreferredSize();
 								Dimension surfacePanelPreferred = surfacePanel.getPreferredSize();
 
 								int parentHeight = parent.getHeight();
 								int parentWidth = parent.getWidth();
 								int fullWidth = parentWidth - parentInsets.left - parentInsets.right - visualBuffer * 2;
 
 								int listHeight = parentHeight - parentInsets.top - parentInsets.bottom - (step1Preferred.height + step2Preferred.height + hydrophilicBoxPreferred.height + 
 										hydrophobicBoxPreferred.height + l_h2o_pBoxPreferred.height + otherBoxPreferred.height + neighborBoxPreferred.height + separatorPreferred.height + 
 										distancePreferred.height + surfacePanelPreferred.height + visualBuffer * 13);
 
 								int curY = parentInsets.top + visualBuffer;
 								int curX = parentInsets.left + visualBuffer;
 								int maxWidth = 0;
 
 								centerView.setBounds(curX, curY, step1Preferred.width, step1Preferred.height);
 								curY += step1Preferred.height + visualBuffer;
 								maxWidth = step1Preferred.width;
 
 								ligandScroller.setBounds(curX,curY,fullWidth, listHeight);
 								curY += listHeight + visualBuffer;
 
 								displayView.setBounds(curX, curY, step2Preferred.width, step2Preferred.height);
 								curY += step2Preferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, step2Preferred.width);
 
 								int maxCheckboxWidth = 0; 
 								int hydrophilicBoxStartY = curY;
 								hydrophilicBox.setBounds(curX, curY, hydrophilicBoxPreferred.width, hydrophilicBoxPreferred.height);
 								curY += hydrophilicBoxPreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, hydrophilicBoxPreferred.width);
 								maxCheckboxWidth = Math.max(maxCheckboxWidth, hydrophilicBoxPreferred.width);
 
 								int hydrophobicBoxStartY = curY;
 								hydrophobicBox.setBounds(curX, curY, hydrophobicBoxPreferred.width, hydrophobicBoxPreferred.height);
 								curY += hydrophobicBoxPreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, hydrophobicBoxPreferred.width);
 								maxCheckboxWidth = Math.max(maxCheckboxWidth, hydrophobicBoxPreferred.width);
 
 								int l_h2o_pBoxStartY = curY;
 								l_h2o_pBox.setBounds(curX, curY, l_h2o_pBoxPreferred.width, l_h2o_pBoxPreferred.height);
 								curY += l_h2o_pBoxPreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, l_h2o_pBoxPreferred.width);
 								maxCheckboxWidth = Math.max(maxCheckboxWidth, l_h2o_pBoxPreferred.width);
 
 								int otherBoxStartY = curY;
 								otherBox.setBounds(curX, curY, otherBoxPreferred.width, otherBoxPreferred.height);
 								curY += otherBoxPreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, otherBoxPreferred.width);
 								maxCheckboxWidth = Math.max(maxCheckboxWidth, otherBoxPreferred.width);
 
 								int neighborBoxStartY = curY;
 								neighborBox.setBounds(curX, curY, neighborBoxPreferred.width, neighborBoxPreferred.height);
 								curY += neighborBoxPreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, neighborBoxPreferred.width);
 								maxCheckboxWidth = Math.max(maxCheckboxWidth, neighborBoxPreferred.width);
 
 								// now align the constraint boxes to a grid defined by the check boxes.
 								final int legendWidth = 50;
 								curX += maxCheckboxWidth + visualBuffer * 2;
 								neighborFLF1.setBounds(curX, neighborBoxStartY, neighborFLF1Preferred.width, neighborFLF1Preferred.height);
 								philicFLF2.setBounds(curX, hydrophilicBoxStartY, hydrophilicFLF2Preferred.width, hydrophilicFLF2Preferred.height);
 								phobicFLF2.setBounds(curX, hydrophobicBoxStartY, hydrophobicFLF2Preferred.width, hydrophobicFLF2Preferred.height);
 								l_h2o_pFLF2.setBounds(curX, l_h2o_pBoxStartY, l_h2o_pFLF2Preferred.width, l_h2o_pFLF2Preferred.height);
 								otherFLF2.setBounds(curX, otherBoxStartY, otherFLF2Preferred.width, otherFLF2Preferred.height);
 								curX += philicFLF2.getWidth() + visualBuffer;
 								hydrophilicPanel.setBounds(curX, hydrophilicBoxStartY, legendWidth, hydrophilicFLF2Preferred.height);
 								hydrophobicPanel.setBounds(curX, hydrophobicBoxStartY, legendWidth, hydrophobicFLF2Preferred.height);
 								l_h2o_pPanel.setBounds(curX, l_h2o_pBoxStartY, legendWidth, l_h2o_pFLF2Preferred.height);
 								otherPanel.setBounds(curX, otherBoxStartY, legendWidth, otherFLF2Preferred.height);
 								curX = parentInsets.left + visualBuffer;							
 
 								separator.setBounds(curX, curY, separatorPreferred.width, separatorPreferred.height);
 								curY += separatorPreferred.height + 3 * visualBuffer;
 								maxWidth = Math.max(maxWidth, separatorPreferred.width);
 
 								distanceBox.setBounds(curX, curY, distancePreferred.width, distancePreferred.height);
 								curY += distancePreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, distancePreferred.width);
 
 								surfacePanel.setBounds(curX, curY, surfacePanelPreferred.width, surfacePanelPreferred.height);
 								curY += surfacePanelPreferred.height + visualBuffer;
 								maxWidth = Math.max(maxWidth, surfacePanelPreferred.width);
 								
 	//							this.layoutSize.width = maxWidth + parentInsets.left + parentInsets.right + visualBuffer * 2;
 								
 								// TODO check width on Mac OSX
 								this.layoutSize.width = maxWidth + parentInsets.left + parentInsets.right + visualBuffer * 2;
 							}
 						}
 
 						public Dimension minimumLayoutSize(Container parent) {
 							if(this.layoutSize == null) {
 								this.layoutSize = new Dimension();
 								this.layoutContainer(parent);
 							}
 							return this.layoutSize;
 						}
 
 						public Dimension preferredLayoutSize(Container parent) {
 							if(this.layoutSize == null) {
 								this.layoutSize = new Dimension();
 								this.layoutContainer(parent);
 							}
 							return this.layoutSize;
 						}
 
 						public void removeLayoutComponent(Component comp) {}
 					});
 
 			// set colors and initial state
 			this.setBackground(LXDocumentFrame.sidebarColor);
 			hydrophilicBox.setBackground(LXDocumentFrame.sidebarColor);
 			hydrophilicBox.setSelected(false);
 			hydrophobicBox.setBackground(LXDocumentFrame.sidebarColor);
 			hydrophobicBox.setSelected(false);
 			otherBox.setBackground(LXDocumentFrame.sidebarColor);
 			otherBox.setSelected(false);
 			neighborBox.setBackground(LXDocumentFrame.sidebarColor);
 			neighborBox.setSelected(false);
 
 			// separator is not visible, why ??
 			separator.setBackground(Color.BLACK);
 			separator.setForeground(Color.BLACK);
 			separator.setVisible(true);
 
 
 			interactionListener = new InteractionListener(l_h2o_pBox,
 					hydrophilicBox, hydrophobicBox, otherBox, neighborBox,
 					distanceBox,
 					l_h2o_pFLF2, philicFLF2, phobicFLF2,
 					otherFLF2, neighborFLF1);
 
 			applyButton.addActionListener(interactionListener);
 			hydrophilicBox.addActionListener(interactionListener);
 			hydrophobicBox.addActionListener(interactionListener);
 			l_h2o_pBox.addActionListener(interactionListener);
 			otherBox.addActionListener(interactionListener);
 			neighborBox.addActionListener(interactionListener);
 			distanceBox.addActionListener(interactionListener);
 
 			// add action listener to the distance threshold text boxes
 			l_h2o_pFLF2.addActionListener(interactionListener);
 			philicFLF2.addActionListener(interactionListener);
 			phobicFLF2.addActionListener(interactionListener);
 			otherFLF2.addActionListener(interactionListener);
 			neighborFLF1.addActionListener(interactionListener);;
 		}
 	}
 
 	private Vector<Chain> getLigandList(final Structure structure)
 	{
 		final Vector<Chain> ligandList = new Vector<Chain>();
 
 		for (Chain chain : structure.getStructureMap().getChains())
 			if (chain.getClassification() == Residue.Classification.LIGAND ||
 			chain.getClassification() == Residue.Classification.BIRD ||
 					chain.hasModifiedResidues())
 				ligandList.add(chain);
 
 		
 		return ligandList;
 	}
 
 
 	/**
 	 * This will select the intial ligand in the tree and trigger an update
 	 */
 	public void selectInitialLigand()
 	{
 		String initialLigand = LigandExplorer.sgetModel().getInitialLigand();
 		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)ligandJList.getModel().getRoot();
 		TreePath paths[] = null;
 
 
 		for (int ix = 0; ix < rootNode.getChildCount(); ix++)
 		{
 			DefaultMutableTreeNode chainNode = (DefaultMutableTreeNode)rootNode.getChildAt(ix);
 			if (initialLigand != null)
 			{
 				for (int lx = 0; lx < chainNode.getChildCount(); lx++)
 				{
 					DefaultMutableTreeNode residueNode = (DefaultMutableTreeNode)chainNode.getChildAt(lx);
 					Residue residue = (Residue)residueNode.getUserObject();
 					
 					if (isInitialLigandResidue(residue, initialLigand))
 					{
 						Residue[] ligs = new Residue[1];
 		                ligs[0] = residue;
 		                LigandExplorer.sgetSceneController().setLigandResidues(ligs);
 		                
 						// The following code generates an exception in the swing libs 
 						// (see bug PDBWW-1917 PDB ID 1OLN, ligand XBB), most likely because
 						// the path has null entries, i.e. for the case above.
 //						if (paths == null) {
 //							paths = new TreePath[chainNode.getChildCount()];
 //						}
 //						paths[lx] = new TreePath(residueNode.getPath());
 						
 						// Since we are only selecting a single entry, there is no need to pass in an array with
 						// null entries.
 						if (paths == null) {
 							paths = new TreePath[1];
 							paths[0] = new TreePath(residueNode.getPath());
 						} else {
 							break;
 						}
 						
 					}
 				}
 			}
 		}
 
 		TreePath rootPath = new TreePath(rootNode);
 		for (int ix = 0; ix < rootNode.getChildCount(); ix++)
 			ligandJList.expandPath(rootPath.pathByAddingChild(rootNode.getChildAt(ix)));
 
 		if (paths != null)
 			ligandJList.setSelectionPaths(paths);
 		// set the discovered intial ligand path(s) as selected
 		else
 		{
 			DefaultMutableTreeNode firstChainNode = (DefaultMutableTreeNode)rootNode.getFirstChild();
 			TreePath selectedPath = rootPath.pathByAddingChild(firstChainNode);
 			selectedPath = selectedPath.pathByAddingChild(firstChainNode.getFirstChild());
 			ligandJList.setSelectionPath(selectedPath);
 			// set the first residue in the first chain as selected
 			// (jeez, this is a lot of work to do something this simple, I might add...)
 		}
 
 	}
 	
 	/**
 	 * Returns true if ligand specified by either 3-letter ligand id (i.e. HEM) or
	 * chain/residue number (i.e. A156) or PRD ID (i.e., PRD_000223) matches the passed in residue.
 	 * @param residue
 	 * @param initialLigand Specification of ligand to be highlighted in Ligand Explorer upon startup
 	 * @return
 	 */
 	private boolean isInitialLigandResidue(Residue residue, String initialLigand) {
 		if (residue.getCompoundCode().equalsIgnoreCase(initialLigand)) {
 			return true;
 		}
 			
 		if (residue.getPrdId().equalsIgnoreCase(initialLigand)) {
 			return true;
 		}
 		
 		String chainId = initialLigand.substring(0,1);
 		int residueNumber;
 		
 		try {
 		    residueNumber = Integer.parseInt(initialLigand.substring(1));
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		if (residue.getAuthorChainId().equalsIgnoreCase(chainId) &&
 				residue.getAuthorResidueId() == residueNumber) {
 			return true;
 		}
 		return false;
 	}
 }
