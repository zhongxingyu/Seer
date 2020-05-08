 package webApplication.grafica;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
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
  * L'oggetto per l'undo di una azione di aggiunta di un nodo all'albero
  * 
  * @author Andrea
  * 
  */
 public class UndoableAddNode extends AbstractUndoableEdit {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5324954463054904036L;
 
 	public static final String PRESENTATIONNAME = "Node Added";
 
 	private JTree tree;
 	private Componente nodeComp;
 	private int parentIndex;
 	private int index;
 	private HashMap<Integer, Integer> movingNodes;
 
 	/**
 	 * Il costruttore di base
 	 * 
 	 * @param t
 	 *            L'albero
 	 * @param n
 	 *            Il componente
 	 * @param p
 	 *            L'indice del genitore
 	 * @param i
 	 *            L'indice del nodo
 	 */
 	public UndoableAddNode(JTree t, Componente n, int p, int i, HashMap<Integer, Integer> mvn) {
 		tree = t;
 		nodeComp = n;
 		parentIndex = p;
 		index = i;
 		movingNodes = mvn;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void undo() throws CannotUndoException {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		DisabledNode parent = findParent();
 		DisabledNode node;
 		if (parentIndex == -1) {
 			parent = (DisabledNode) model.getRoot();
 		} else {
 			DisabledNode root = (DisabledNode) model.getRoot();
 			parent = (DisabledNode) root.getChildAt(parentIndex);
 		}
 		node = (DisabledNode) parent.getChildAt(index);
 		if (movingNodes != null) {
 			Set<Integer> set = movingNodes.keySet();
 			Iterator<Integer> it = set.iterator();
 			while (it.hasNext()) {
 				int newIndex = it.next();
 				ComponenteSemplice comp = (ComponenteSemplice) ((ComponenteSemplice) ((DisabledNode) node.getChildAt(newIndex)).getUserObject()).clone();
 				model.insertNodeInto(new DisabledNode(comp), (DisabledNode) model.getRoot(), movingNodes.get(newIndex));
 			}
 		}
 		model.removeNodeFromParent(node); // se era un nodo composto automaticamente sono deletati anche i suoi figli
 		if (!(parent.isRoot()) && (!((Componente) parent.getUserObject()).isSimple())) {
 			ComponenteMolteplice parentComp = (ComponenteMolteplice) parent.getUserObject();
 			parentComp.cancellaOpzione(index);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void redo() throws CannotRedoException {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		DisabledNode parent = findParent();
 		DisabledNode node = new DisabledNode(nodeComp);
 		if (parentIndex == -1) {
 			parent = (DisabledNode) model.getRoot();
 		} else {
 			DisabledNode root = (DisabledNode) model.getRoot();
 			parent = (DisabledNode) root.getChildAt(parentIndex);
 		}
 		if (movingNodes != null) {
 			Object[] valori = movingNodes.values().toArray();
 			Arrays.sort(valori);
 			for (int i = movingNodes.size() - 1; i >= 0; i--) {
 				model.removeNodeFromParent((DisabledNode) ((DisabledNode) model.getRoot()).getChildAt((Integer) valori[i]));
 			}
 		}
 		model.insertNodeInto(node, parent, index);
 		if (!(parent.isRoot()) && (!((Componente) parent.getUserObject()).isSimple())) {
 			ComponenteMolteplice parentComp = (ComponenteMolteplice) parent.getUserObject();
 			parentComp.aggiungiOpzione((ComponenteSemplice) node.getUserObject(), index);
 		}
 		// se era un nodo composto riaggiungo anche i suoi elementi
 		if (!(nodeComp.isSimple())) {
 			ComponenteMolteplice moltCompNode = (ComponenteMolteplice) nodeComp;
 			for (int i = 0; i < moltCompNode.getOpzioni().size(); i++) {
				model.insertNodeInto(new DisabledNode(moltCompNode.getOpzione(i)), node, i);
 			}
 		}
 		tree.setSelectionPath(new TreePath(node.getPath()));
 		tree.expandPath(new TreePath(node.getPath()));
 	}
 
 	/**
 	 * Cerca il genitore del nodo salvato nel campo nodeComp
 	 * 
 	 * @return Il genitore
 	 */
 	private DisabledNode findParent() {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		if ((!nodeComp.isSimple()) || (parentIndex == -1)) {
 			return (DisabledNode) model.getRoot();
 		} else {
 			DisabledNode root = (DisabledNode) model
 					.getRoot();
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
 	public String getUndoPresentationName() {
 		return PRESENTATIONNAME;
 	}
 
 }
