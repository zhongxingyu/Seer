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
  * Created on 2007/02/08
  *
  */ 
 package org.rcsb.pw.ui.tree;
 
 // MBT
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeSelectionModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.rcsb.mbt.model.Atom;
 import org.rcsb.mbt.model.Bond;
 import org.rcsb.mbt.model.Chain;
 import org.rcsb.mbt.model.ExternChain;
 import org.rcsb.mbt.model.Fragment;
 import org.rcsb.mbt.model.Residue;
 import org.rcsb.mbt.model.Structure;
 import org.rcsb.mbt.model.StructureComponent;
 import org.rcsb.mbt.model.StructureMap;
 import org.rcsb.mbt.model.StructureModel;
 import org.rcsb.mbt.model.StructureComponentRegistry.ComponentType;
 import org.rcsb.mbt.model.attributes.IStructureStylesEventListener;
 import org.rcsb.mbt.model.attributes.StructureStyles;
 import org.rcsb.mbt.model.attributes.StructureStylesEvent;
 import org.rcsb.pw.controllers.app.ProteinWorkshop;
 import org.rcsb.uiApp.controllers.app.AppBase;
 import org.rcsb.uiApp.controllers.update.IUpdateListener;
 import org.rcsb.uiApp.controllers.update.UpdateEvent;
 import org.rcsb.vf.controllers.app.VFAppBase;
 import org.rcsb.vf.controllers.scene.mutators.MutatorBase;
 
 
 /**
  * This class implements a pop-out list-based tree viewer.
  * <P>
  * 
  * @author John L. Moreland
  * @see org.rcsb.uiApp.controllers.update.IUpdateListener
  * @see org.rcsb.uiApp.controllers.update.UpdateEvent
  */
 public class TreeViewer extends JPanel implements IUpdateListener,
 TreeSelectionListener, IStructureStylesEventListener, MouseListener
 {
 	private static final long serialVersionUID = -3564103946574642455L;
 
 	public static final int SELECTION_MODEL_NONSELECTOR = 0;
 
 	public static final int SELECTION_MODEL_REGULAR = 1;
 
 	public static final int SELECTION_MODEL_MULTIPLE = 2;
 
 	//
 	// Private variables.
 	//
 
 	private final TreeSelectionModel nonSelectorSelectionModel = new NonSelectionModel();
 
 	private final TreeSelectionModel defaultSelectionModel = new DefaultTreeSelectionModel();
 
 	private final TreeSelectionModel multipleSelectionModel = new DefaultTreeSelectionModel();
 
 	private final class CustomJTree extends JTree {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 5618034603390785737L;
 
 		public CustomJTree(final TreeViewerModel tvm) {
 			super(tvm);
 		}
 	}
 
 	private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 6648235647292439163L;
 
 		public CustomTreeCellRenderer() {
 
 			// Set the DefaultTreeCellRenderer's selection color
 			final float color[] = new float[3];
 			StructureStyles.getSelectionColor(color);
 			super.backgroundSelectionColor = // Color.lightGray;
 				new Color(color[0], color[1], color[2]);
 
 		}
 
 		private final ImageIcon atomIcon = new ImageIcon(this.getClass()
 				.getResource("atom_16.jpg"));
 
 		private final ImageIcon residueIcon = new ImageIcon(this.getClass()
 				.getResource("residue_16.jpg"));
 
 		private final ImageIcon chainIcon = new ImageIcon(this.getClass()
 				.getResource("chain_16.jpg"));
 
 		private final ImageIcon bondIcon = new ImageIcon(this.getClass()
 				.getResource("bonds_16.jpg"));
 
 		private final ImageIcon documentIcon = new ImageIcon(this.getClass()
 				.getResource("document_16.jpg"));
 
 		@Override
 		public Component getTreeCellRendererComponent(final JTree tree,
 				final Object value, final boolean selected,
 				final boolean expanded, final boolean leaf, final int row,
 				final boolean hasFocus) {
 			final CustomTreeCellRenderer component = (CustomTreeCellRenderer) super
 			.getTreeCellRendererComponent(tree, value, selected,
 					expanded, leaf, row, hasFocus);
 
 			ImageIcon imageIcon = null;
 			String componentText = null;
 
 			if (value instanceof StructureModel) {
 				componentText = "Document";
 				// setToolTipText( "A StructureDocument contains Structures" );
 				imageIcon = this.documentIcon;
 			} else if (value instanceof Structure) {
 				final Structure struc = (Structure) value;
 				final StructureMap sm = struc.getStructureMap();
 
 				// Try the url.
 				if (componentText == null) {
 					componentText = sm.getPdbId();
 				}
 
 				// Ensure that its short enough to display nicely.
 				if (componentText != null) {
 					final int ctLen = componentText.length();
 					if (ctLen > 20) {
 						// Shorten the url so that the ends are visible.
 						componentText = componentText.substring(0, 5) + " ... "
 						+ componentText.substring(ctLen - 15, ctLen);
 					}
 				}
 			}
 
 			else if (value instanceof ExternChain)
 			{
 				final ExternChain xc = (ExternChain) value;
 				String entityName = xc.getEntityName();
 				String chainSpec = "Chain " + xc.getChainId();
 				
 
 				if (xc.isBasicChain()) {
 					if (entityName != null && entityName.length() > 1) {
 						chainSpec = chainSpec + ":" + entityName.toLowerCase();
 					}
 					componentText = chainSpec;
 				} else if (xc.isBirdChain()) {
					componentText = chainSpec + ":" + entityName;
 				} else if (xc.isWaterChain()) {
 					componentText = "Water molecules";
 				} else {
 					componentText = "Miscellaneous molecules";
 				}
 
 				imageIcon = this.chainIcon;
 			}
 
 			else if (value instanceof StructureComponent) {
 				final StructureComponent structureComponent = (StructureComponent) value;
 				final StructureMap structureMap = structureComponent.structure
 				.getStructureMap();
 				final StructureStyles structureStyles = structureMap
 				.getStructureStyles();
 				final ComponentType type = structureComponent.getStructureComponentType();
 				componentText = type.toString(); // Default
 
 				// determine if we should worry about atom mode or backbone
 				// mode.
 				boolean isAtomMode = false;
 				boolean isBackboneMode = false;
 				final boolean disableVisibilityCheck = false;
 
 				switch (MutatorBase.getActivationType())
 				{
 				case ATOMS_AND_BONDS:
 					isAtomMode = true;
 					break;
 				default:
 					isBackboneMode = true;
 				break;
 				}
 				// }
 				// break;
 				// default:
 				// // for everything but the VISIBILITY_MUTATOR, we want the
 				// residues to always appear as "visible".
 				// disableVisibilityCheck = true;
 				// }
 
 				if (structureStyles.isSelected(structureComponent)) {
 					component.setBackgroundNonSelectionColor(Color.YELLOW);
 				} else {
 					component.setBackgroundNonSelectionColor(Color.WHITE);
 				}
 
 				if (type == ComponentType.ATOM) {
 					final Atom atom = (Atom) structureComponent;
 					componentText = atom.number + " " + atom.name;
 					imageIcon = this.atomIcon;
 					// setToolTipText( atom.coordinate[0] + ", " +
 					// atom.coordinate[1] + ", " + atom.coordinate[2] );
 
 					// If atom is visible, draw text black else lightGray.
 					// StructureMap structureMap =
 					// atom.structure.getStructureMap( );
 					// StructureStyles structureStyles =
 					// structureMap.getStructureStyles( );
 					if (disableVisibilityCheck
 							|| (isAtomMode && structureStyles.isVisible(atom))
 							|| (isBackboneMode && structureStyles
 									.isVisible(structureMap.getResidue(atom)
 											.getFragment()))) {
 						component.setForeground(Color.black);
 					} else {
 						component.setForeground(Color.lightGray);
 
 					}
 				} else if (type == ComponentType.RESIDUE) {
 					final Residue residue = (Residue) value;
 
 					componentText = residue.getAuthorChainId() + " " 
 					+ residue.getAuthorResidueId() + residue.getInsertionCode() + " "
 					+ residue.getCompoundCode();
 
 					imageIcon = this.residueIcon;
 
 					boolean isVisible = false;
 					if (isAtomMode) {
 						// denote the residue as visible if even one of its
 						// atoms are visible.
 						for (Atom a : residue.getAtoms())
 							if (structureStyles.isVisible(a))
 							{
 								isVisible = true;
 								break;
 							}
 					}
 
 					else if (isBackboneMode) {
 						final Fragment f = residue.getFragment();
 						if (f != null && structureStyles.isVisible(f)) {
 							isVisible = true;
 						}
 					}
 
 					if (disableVisibilityCheck || isVisible) {
 						component.setForeground(Color.black);
 					} else {
 						component.setForeground(Color.lightGray);
 					}
 				} else if (type == ComponentType.FRAGMENT) {
 					final Fragment fragment = (Fragment) value;
 					ComponentType conformation = fragment.getConformationType();
 
 					final Chain c = fragment.getChain();
 					componentText = conformation + ": " + 
 					c.getResidue(fragment.getStartResidueIndex()).getAuthorResidueId()
 					+ " to " + 
 					c.getResidue(fragment.getEndResidueIndex()).getAuthorResidueId();
 					imageIcon = this.chainIcon;
 
 					//					// setToolTipText( chain.getClassification() );
 				} else if (type == ComponentType.CHAIN) {
 					final Chain chain = (Chain) value;
 					componentText = chain.getAuthorChainId();
 					imageIcon = this.chainIcon;
 
 					if (componentText == null) {
 						componentText = "(no chain id)";
 					}
 					// setToolTipText( chain.getClassification() );
 				} else if (type == ComponentType.BOND) {
 					final Bond bond = (Bond) value;
 					final Atom atom0 = bond.getAtom(0);
 					final Atom atom1 = bond.getAtom(1);
 					componentText = atom0.number + " " + atom0.name + " - "
 					+ atom1.number + " " + atom1.name;
 					imageIcon = this.bondIcon;
 					// setToolTipText( chain.getClassification() );
 				}
 			}
 
 			if (componentText != null) {
 				this.setText(componentText);
 			}
 			if (imageIcon != null) {
 				this.setIcon(imageIcon);
 			}
 			return component;
 		}
 	}
 
 	/**
 	 * The primary JTree objects.
 	 */
 	public JTree tree = null;
 
 	public TreeViewerModel treeViewerModel = null;
 
 	public JScrollPane scrollPane = null;
 
 	/**
 	 * Constructs a new TreeViewer object.
 	 */
 	public TreeViewer()
 	{
 		super(null, false);
 		super.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createTitledBorder("4)  Choose items from the tree or 3D viewer."),
 				BorderFactory.createEmptyBorder(-1, 1, 1, 1)));
 
 		// Create a JTree with default models/nodes
 		this.treeViewerModel = new TreeViewerModel();
 		this.tree = new CustomJTree(this.treeViewerModel);
 		// make sure this mouse listener receives events first...
 		final MouseListener[] listeners = this.tree.getMouseListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			this.tree.removeMouseListener(listeners[i]);
 		}
 		this.tree.addMouseListener(this);
 		for (int i = 0; i < listeners.length; i++) {
 			this.tree.addMouseListener(listeners[i]);
 		}
 
 		this.multipleSelectionModel
 		.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 
 		ProteinWorkshop.sgetActiveFrame().setTreeViewer(this);
 
 		// Set the basic JTree properties
 		this.tree.setRootVisible(false);
 		this.tree.setShowsRootHandles(true);
 		this.tree.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 		this.tree.putClientProperty("JTree.lineStyle", "Angled");
 		this.setSelectionModel(TreeViewer.SELECTION_MODEL_NONSELECTOR);
 
 		// Enable tool tips.
 		// ToolTipManager.sharedInstance().registerComponent( tree );
 		// ToolTipManager.sharedInstance().setLightWeightPopupEnabled( false );
 
 		// Set a custom JTree cell renderer
 		final DefaultTreeCellRenderer treeCellRenderer = new CustomTreeCellRenderer();
 		this.tree.setCellRenderer(treeCellRenderer);
 
 		// Set up a tree selection listener
 		this.tree.addTreeSelectionListener(this);
 
 		// Add the JTree to a JScrollPane
 		this.setLayout(new java.awt.GridLayout(1, 1));
 		this.scrollPane = new JScrollPane(this.tree);
 		this.add(this.scrollPane);
 
 		// model.getStructure().getStructureMap().getStructureStyles().addStructureStylesEventListener(this);
 
 		AppBase.sgetUpdateController().registerListener(this);
 	}
 
 	/**
 	 * JTree TreeSelectionListener handler. This gets called when the user
 	 * picks/clicks-on a tree node. The TreeSelectionEvent message has a
 	 * reference to the StructureComponent object. The actual hilighing does NOT
 	 * happen here (see processStructureStylesEvent).
 	 */
 	public void valueChanged(final TreeSelectionEvent treeSelectionEvent) {
 		// System.out.println("changed");
 		// ** quick fix to make the NonSelectionModel work.
 		if (this.tree.getSelectionModel() instanceof NonSelectionModel
 				&& !treeSelectionEvent.isAddedPath()) {
 			return;
 		}
 
 		// System.err.println( "TreeViewer.processTreeSelectionEvent:
 		// treeSelectionEvent=" + treeSelectionEvent );
 
 		final TreePath treePaths[] = treeSelectionEvent.getPaths();
 		for (int i = treePaths.length - 1; i >= 0; i--) {
 			final Object userObject = treePaths[i].getLastPathComponent();
 			if (userObject == null) {
 				return;
 			}
 
 			final MutatorBase curMut = ProteinWorkshop.sgetSceneController().getMutatorEnum().getCurrentMutator();
 
 			if (userObject instanceof Structure)
 			{
 				curMut.setConsiderClickModifiers(false);
 				curMut.setConsiderSelectedFlag(true);
 				curMut.setSelected(treeSelectionEvent.isAddedPath(i));
 
 				if (VFAppBase.sgetSceneController().areSelectionsEnabled()) {
 					if (curMut.supportsBatchMode()) {
 						curMut.toggleMutee(userObject);
 					} else {
 						curMut.doMutationSingle(userObject);
 					}
 				}
 			}
 
 			else if (userObject instanceof StructureComponent)
 			{
 				final StructureComponent structureComponent = (StructureComponent) userObject;
 
 				curMut.setConsiderClickModifiers(false);
 				curMut.setConsiderSelectedFlag(true);
 				curMut.setSelected(treeSelectionEvent.isAddedPath(i));
 				curMut.setCtrlDown(this.latestClickEvent.isControlDown());
 				curMut.setShiftDown(this.latestClickEvent.isShiftDown());
 				if (VFAppBase.sgetSceneController().areSelectionsEnabled()) {
 					if (curMut.supportsBatchMode()) {
 						curMut.toggleMutee(structureComponent);
 					} else {
 						curMut.doMutationSingle(structureComponent);
 					}
 				}
 			} else if (userObject instanceof StructureModel) {
 				continue; // StructureDocument (root)
 			}
 		}
 	}
 
 	public void setSelectionModel(final int model) {
 		switch (model) {
 		case SELECTION_MODEL_NONSELECTOR:
 			this.tree.setSelectionModel(this.nonSelectorSelectionModel);
 			break;
 		case SELECTION_MODEL_REGULAR:
 			this.tree.setSelectionModel(this.defaultSelectionModel);
 			break;
 		case SELECTION_MODEL_MULTIPLE:
 			this.tree.setSelectionModel(this.multipleSelectionModel);
 			break;
 		default:
 			(new Exception()).printStackTrace();
 		}
 	}
 
 	//
 	// Viewer methods
 	// 
 
 	/**
 	 * Process a StructureStylesEvent. This method reacts to color and selection
 	 * events from the StructureStyles class. The picking does NOT happen here
 	 * (see valueChanged).
 	 */
 	public void processStructureStylesEvent(
 			final StructureStylesEvent structureStylesEvent) {
 		// System.err.println( "TreeViewer.processStructureStylesEvent: " +
 		// structureStylesEvent );
 
 		if (structureStylesEvent.attribute == StructureStyles.ATTRIBUTE_SELECTION) {
 			this.tree.repaint();
 			this.tree.repaint();
 		}
 	}
 
 	/**
 	 * Process a StructureDocumentEvent message.
 	 */
 	public void handleUpdateEvent(
 			final UpdateEvent evt)
 	{
 		switch (evt.action)
 		{
 		case STRUCTURE_ADDED:
 			structureAdded(evt.structure);
 			break;
 
 		case STRUCTURE_REMOVED:
 			structureRemoved(evt.structure);
 			break;
 
 		case VIEW_ADDED:
 			viewerAdded(evt.view);
 			break;
 
 		case VIEW_REMOVED:
 			viewerRemoved(evt.view);
 			break;
 
 		case VIEW_RESET:
 			reset();
 			break;
 		}
 	}
 
 	/**
 	 * A Viewer was just added to a StructureDocument.
 	 */
 	private void viewerAdded(final IUpdateListener viewer)
 	{
 		if (viewer == null)
 			return;
 
 		// This viewer doesn't care about other viewers.
 		if (viewer != this)
 			return;
 
 		this.treeViewerModel.setRoot(AppBase.sgetModel());
 		this.tree.expandRow(1);
 	}
 
 	/**
 	 * A Viewer was just removed from the StructureDocument.
 	 */
 	private void viewerRemoved(final IUpdateListener viewer)
 	{
 		if (viewer == null) {
 			return;
 		}
 
 		// This viewer doesn't care about other viewers.
 		if (viewer != this) {
 			return;
 		}
 
 		this.treeViewerModel.setRoot(null); // Ditch the StructureDocument
 		// reference.
 	}
 
 	/**
 	 * A Structure was just added to the StructureDocument.
 	 */
 	public void structureAdded(final Structure structure) {
 		if (structure == null) {
 			return;
 		}
 
 		// Force a root-level TreeModelEvent.
 		this.treeViewerModel.setRoot((StructureModel) this.treeViewerModel
 				.getRoot());
 
 		// Add a StructureStylesEventListener so we recieve updates.
 		final StructureMap structureMap = structure.getStructureMap();
 		final StructureStyles structureStyles = structureMap
 		.getStructureStyles();
 		structureStyles.addStructureStylesEventListener(this);
 
 		StructureModel model = AppBase.sgetModel();
 
 		// Expand the path for the structure
 		TreePath treePath = new TreePath(new Object[] { model, structure });
 		this.tree.expandPath(treePath);
 
 		// expand the path for the non-protein atoms, if any are present...
 		for (StructureComponent next : structureMap.getPdbTopLevelElements()) {
 			if (next instanceof ExternChain && ((ExternChain)next).isMiscellaneousChain())
 			{	
 				treePath = new TreePath(new Object[] { model, structure, next });
 				this.tree.expandPath(treePath);
 			}
 		}
 	}
 
 	/**
 	 * A Structure was just removed from the StructureDocument.
 	 */
 	private void structureRemoved(final Structure structure) {
 		if (structure == null) {
 			return;
 		}
 
 		// Force a root-level TreeModelEvent.
 		this.treeViewerModel.setRoot((StructureModel) this.treeViewerModel
 				.getRoot());
 
 		// Remove the StructureStylesEventListener so don't recieve updates.
 		final StructureMap structureMap = structure.getStructureMap();
 		final StructureStyles structureStyles = structureMap
 		.getStructureStyles();
 		structureStyles.removeStructureStylesEventListener(this);
 	}
 
 	public void reset()
 	{
 		this.tree.clearSelection();
 
 		// collapse everything...
 		try {
 			for (int i = this.tree.getRowCount() - 1; i >= 0; i--) {
 				this.tree.collapseRow(i);
 			}
 
 			this.tree.expandPath(new TreePath(
 					new Object[] { AppBase.sgetModel() }));
 		} catch (final Exception e) {
 			// usually not necessary to debug here (?)
 			System.err.println("Warning: exception in TreeViewer.reset()");
 		}
 
 		this.setSelectionModel(TreeViewer.SELECTION_MODEL_NONSELECTOR);
 	}
 
 	public JTree getTree() {
 		return this.tree;
 	}
 
 	private MouseEvent latestClickEvent = null;
 
 	public void mouseClicked(final MouseEvent e) {
 	}
 
 	public void mouseEntered(final MouseEvent e) {
 	}
 
 	public void mouseExited(final MouseEvent e) {
 	}
 
 	public void mousePressed(final MouseEvent e) {
 		this.latestClickEvent = e;
 	}
 
 	public void mouseReleased(final MouseEvent e) {
 	}
 }
 
 class NonSelectionModel extends DefaultTreeSelectionModel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7668948115407156550L;
 
 	private final NonSelectionIndicatorThread nonSelector = new NonSelectionIndicatorThread(
 			this);
 
 	public NonSelectionModel() {
 		super.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	}
 
 	@Override
 	public void setSelectionPath(final TreePath path) {
 		super.setSelectionPath(path);
 		this.nonSelector.setLivePath(path);
 	}
 }
 
 class NonSelectionIndicatorThread extends Thread {
 	private TreeSelectionModel selectionModel;
 
 	private TreePath livePath;
 
 	private static final int TIMER_LENGTH = 500;
 
 	private static final int FOREVER = 1000000000;
 
 	public NonSelectionIndicatorThread(final TreeSelectionModel selectionModel) {
 		this.selectionModel = selectionModel;
 		super.start();
 	}
 
 	public void setLivePath(final TreePath livePath) {
 		this.livePath = livePath;
 		this.reset();
 	}
 
 	public void reset() {
 		this.interrupt();
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				super.join(NonSelectionIndicatorThread.TIMER_LENGTH);
 			} catch (final InterruptedException e) {
 				continue;
 			}
 
 			if (this.livePath != null) {
 				this.selectionModel.removeSelectionPath(this.livePath);
 			}
 			this.livePath = null;
 
 			try {
 				super.join(NonSelectionIndicatorThread.FOREVER);
 			} catch (final InterruptedException e) {
 
 			}
 		}
 	}
 }
