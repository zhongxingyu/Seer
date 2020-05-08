 package webApplication.grafica;
 
 import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 
 import webApplication.business.Componente;
 import webApplication.business.ComponenteMolteplice;
 import webApplication.business.ComponenteSemplice;
 
 /**
  * L'oggetto per l'undo di una azione di rimozione di un nodo dall'albero
  * 
  * @author Andrea
  * 
  */
 public class UndoableRemoveNode extends AbstractUndoableEdit {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8110828293232783974L;
 
 	public static final String PRESENTATIONNAME = "Remove Node";
 
 	private JTree tree;
 	private Componente nodeComp;
 	private int parentIndex;
 	private int index;
 
 	/**
 	 * Il costruttore di base
 	 * 
 	 * @param t
 	 *            L'albero
 	 * @param n
 	 *            Il componente rimosso
 	 * @param p
 	 *            L'indice del genitore
 	 * @param i
 	 *            L'indice del nodo
 	 */
 	public UndoableRemoveNode(JTree t, Componente n, int p, int i) {
 		tree = t;
 		nodeComp = n;
 		parentIndex = p;
 		index = i;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void undo() throws CannotUndoException {
 		DisabledNode node = new DisabledNode(nodeComp);
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		DisabledNode parent = findParent();
 		if (!(parent.isRoot()) && (!((Componente) parent.getUserObject()).isSimple())) {
 			ComponenteMolteplice parentComp = (ComponenteMolteplice) parent.getUserObject();
 			parentComp.aggiungiOpzione((ComponenteSemplice) node.getUserObject(), index);
 		}
 		model.insertNodeInto(node, parent, index);
 		if (!((Componente) node.getUserObject()).isSimple()) {
 			ComponenteMolteplice parentNodeComp = (ComponenteMolteplice) node.getUserObject();
 			for (int i = 0; i < parentNodeComp.getOpzioni().size(); i++) {
				DisabledNode newNode = new DisabledNode(parentNodeComp.getOpzione(i));
				model.insertNodeInto(newNode, node, i);
				newNode.setAllowsChildren(false);
 			}
		} else {
			node.setAllowsChildren(false);
 		}
 		tree.setSelectionPath(new TreePath(node.getPath()));
 		tree.expandPath(new TreePath(node.getPath()));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void redo() throws CannotRedoException {
 		DisabledNode parent = findParent();
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		DisabledNode node = (DisabledNode) parent.getChildAt(index);
 		if (parent != model.getRoot()) {
 			((ComponenteMolteplice) parent.getUserObject()).cancellaOpzione(index);
 		}
 		model.removeNodeFromParent(node);
 	}
 
 	/**
 	 * Cerca il genitore del nodo
 	 * 
 	 * @return Il genitore
 	 */
 	private DisabledNode findParent() {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		if ((!nodeComp.isSimple()) || (parentIndex == -1)) {
 			return (DisabledNode) model.getRoot();
 		} else {
 			DisabledNode root = (DisabledNode) model.getRoot();
 			return (DisabledNode) root.getChildAt(parentIndex);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean canUndo() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean canRedo() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getPresentationName() {
 		return PRESENTATIONNAME;
 	}
 
 }
