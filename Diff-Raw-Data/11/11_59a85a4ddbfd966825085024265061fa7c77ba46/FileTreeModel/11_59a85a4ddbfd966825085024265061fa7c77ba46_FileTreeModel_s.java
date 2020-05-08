 package kkckkc.jsourcepad.ui;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import com.google.common.collect.MapMaker;
 import kkckkc.utils.Os;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.EventListenerList;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import java.awt.*;
 import java.io.File;
 import java.io.FileFilter;
 import java.util.*;
 import java.util.List;
 
 public class FileTreeModel implements TreeModel {
     public static final Comparator<File> COMPARATOR = new Comparator<File>() {
         public int compare(File f1, File f2) {
             if (f1.isDirectory() && ! f2.isDirectory()) return -1;
             if (! f1.isDirectory() && f2.isDirectory()) return 1;
             return f1.getName().compareTo(f2.getName());
         }
     };
     private final EventListenerList listeners = new EventListenerList();
 
     private final Node root;
     private final FileFilter filter;
     private List<? extends Decorator> decorators = Lists.newArrayList();
 
     private Map<Node, Node[]> expandedNodes =
             new MapMaker().concurrencyLevel(2).makeMap();
 
     private static final int MAX_ENTRIES = 2000;
 
     private Map<File, Node> nodeCache = new LinkedHashMap<File, Node>(MAX_ENTRIES, .75F, true) {
         protected boolean removeEldestEntry(Map.Entry<File, Node> eldest) {
             return size() > MAX_ENTRIES;
         }
     };
 
 
     public FileTreeModel(final File root, final FileFilter filter, List<? extends Decorator> decorators) {
         this.root = makeNode(root);
         this.filter = filter;
 
         this.decorators = Objects.firstNonNull(decorators, Collections.<Decorator>emptyList());
     }
 
     public Object getRoot() {
         return root;
     }
 
     public Object getChild(Object parent, int index) {
         Node[] children = getChildren((Node) parent);
         if (children == null) return null;
         return children[index];
     }
 
     public int getChildCount(Object parent) {
         Node[] children = getChildren((Node) parent);
         if (children == null) return 0;
         return children.length;
     }
 
     public boolean isLeaf(Object node) {
         Node n = (Node) node;
         if (n.getFile() == null) return true;
         return ! n.getFile().isDirectory();
     }
 
     public void valueForPathChanged(TreePath path, Object newValue) {}
 
     public int getIndexOfChild(Object parent, Object child) {
         if (parent == null || child == null) return -1;
 
         Node[] children = getChildren((Node) parent);
         if (children == null) return -1;
 
         for (int i = 0; i < children.length; i++) {
             if (children[i].equals(child)) return i;
         }
 
         return -1;
     }
 
     public void addTreeModelListener(TreeModelListener l) {
         listeners.add(TreeModelListener.class, l);
     }
 
     public void removeTreeModelListener(TreeModelListener l) {
         listeners.remove(TreeModelListener.class, l);
     }
 
     public void refresh() {
         for (Node parent : expandedNodes.keySet()) {
             refresh(parent.getFile());
         }
     }
 
     public void refresh(File node) {
         Node parent = makeNode(node);
 
         Node[] currentChildren = expandedNodes.get(parent);
         expandedNodes.remove(parent);
         Node[] tobeChildren = getChildren(parent);
 
         LinkedHashMap<Integer, Node> changedNodes = new LinkedHashMap<Integer, Node>();
         LinkedHashMap<Integer, Node> insertedNodes = new LinkedHashMap<Integer, Node>();
         LinkedHashMap<Integer, Node> removedNodes = new LinkedHashMap<Integer, Node>();
 
         int tobeIdx = 0, currIdx = 0;
 
         while (tobeIdx < tobeChildren.length) {
             int cmp;
             if (currIdx >= currentChildren.length) {
                 cmp = 1;
             } else {
                 cmp = currentChildren[currIdx].getFile().getName().compareTo(
                     tobeChildren[tobeIdx].getFile().getName());
             }
 
             if (cmp == 0) {
                 changedNodes.put(tobeIdx, tobeChildren[tobeIdx]);
                 tobeIdx++;
                 currIdx++;
             } else if (cmp > 0) {
                 insertedNodes.put(tobeIdx, tobeChildren[tobeIdx]);
                 tobeIdx++;
             } else {
                 removedNodes.put(currIdx, currentChildren[currIdx]);
                 currIdx++;
             }
         }
 
         while (currIdx < currentChildren.length) {
             removedNodes.put(currIdx, currentChildren[currIdx]);
             currIdx++;
         }
 
         TreePath path = createTreePath(node);
 
         TreeModelEvent evt;
 
         if (! removedNodes.isEmpty()) {
             evt = new TreeModelEvent(this, path, getIndices(removedNodes), getValues(removedNodes));
             for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
                 l.treeNodesRemoved(evt);
             }
         }
 
         if (! insertedNodes.isEmpty()) {
             evt = new TreeModelEvent(this, path, getIndices(insertedNodes), getValues(insertedNodes));
             for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
                 l.treeNodesInserted(evt);
             }
         }
 
         if (! changedNodes.isEmpty()) {
             evt = new TreeModelEvent(this, path, getIndices(changedNodes), getValues(changedNodes));
             for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
                 l.treeNodesChanged(evt);
             }
         }
     }
 
     private Object[] getValues(LinkedHashMap<Integer, Node> nodes) {
         return nodes.values().toArray();
     }
 
     private int[] getIndices(LinkedHashMap<Integer, Node> nodes) {
         int i = 0;
         int[] dest = new int[nodes.size()];
         for (Integer key  : nodes.keySet()) {
             dest[i++] = key;
         }
         return dest;
     }
 
     private Node makeNode(File file) {
         Node n = nodeCache.get(file);
         if (n == null) {
             n = new Node(file);
             nodeCache.put(file, n);
         }
         return n;
     }
 
     private Node[] getChildren(final Node parent) {
         if (expandedNodes.containsKey(parent))
             return expandedNodes.get(parent);
 
         File[] children = parent.getFile().listFiles(filter);
         if (children != null) Arrays.sort(children, COMPARATOR);
 
         final Node[] nodes = new Node[children.length];
         for (int i = 0; i < children.length; i++) {
             nodes[i] = makeNode(children[i]);
         }
 
         for (Decorator d : decorators) {
             d.decorate(parent, nodes, new Runnable() {
                 @Override
                 public void run() {
                     fireNodesChanged(parent, nodes);
                 }
             });
         }
 
         expandedNodes.put(parent, nodes);
 
         return nodes;
     }
 
     private TreePath createTreePath(File file) {
         Node[] elements = createPath(file, 1);
         if (elements == null) return null;
         return new TreePath(elements);
     }
 
     private Node[] createPath(final File file, int level) {
         Node[] path;
         if (root.getFile().equals(file)) {
             path = new Node[level];
             path[0] = root;
         } else if (file != null) {
             path = createPath(file.getParentFile(), level + 1);
             if (path != null) path[path.length - level] = makeNode(file);
         } else {
             path = null;
         }
         return path;
     }
 
     private void fireTreeStructureChanged(TreePath path) {
         TreeModelEvent evt = new TreeModelEvent(this, path, null, null);
         for (TreeModelListener listener : listeners.getListeners(TreeModelListener.class)) {
             listener.treeStructureChanged(evt);
         }
     }
 
     private void fireNodesChanged(Node parent, Node[] children) {
         int[] indices = new int[children.length];
         for (int i = 0; i < indices.length; i++) indices[i] = i;
         TreeModelEvent evt = new TreeModelEvent(this, createTreePath(parent.getFile()), indices, children);
         for (TreeModelListener listener : listeners.getListeners(TreeModelListener.class)) {
             listener.treeNodesChanged(evt);
         }
     }
 
     public void onCollapse(Node node) {
         synchronized (this) {
             expandedNodes.remove(node);
         }
     }
 
     public void onExpand(Node node) {
     }
 
 
     public static class Node {
         private File file;
         private Map properties;
 
         Node(File file) {
             this.file = file;
         }
 
         public void putProperty(String key, Object value) {
             if (properties == null) properties = new HashMap(10);
             properties.put(key, value);
         }
 
         public Object getProperty(String key) {
             if (properties == null) return null;
             return properties.get(key);
         }
 
         public File getFile() {
             return file;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Node node = (Node) o;
 
             if (file == null) return node.file == null;
 
             return file.equals(node.file);
         }
 
         @Override
         public int hashCode() {
             return file == null ? 0 : file.hashCode();
         }
 
         public String toString() {
             return file == null ? "" : file.toString();
         }
 
         public String getLabel() {
             return file == null ? "" : file.getName();
         }
     }
 
     public interface Decorator {
         public void decorate(Node parent, Node[] children, Runnable notifyChange);
     }
 
     public interface DecorationRenderer {
         public void renderDecoration(Node node, CellRenderer renderer);
     }
 
 
     public static class CellRenderer extends DefaultTreeCellRenderer {
         private List<DecorationRenderer> decorationRenderers;
         private IconProvider iconProvider;
 
         public CellRenderer(IconProvider iconProvider, List<DecorationRenderer> decorationRenderers) {
             this.decorationRenderers = decorationRenderers;
             this.iconProvider = iconProvider;
 			if (Os.isMac()) {
 				setBackgroundNonSelectionColor(null);
 				setBackgroundSelectionColor(null);
 				setBorderSelectionColor(null);
 			}
 		}
 
 	    public Component getTreeCellRendererComponent(
 	            final JTree tree,
 	            final Object value,
 	            final boolean selected,
 	            final boolean expanded,
 	            final boolean leaf,
 	            final int row,
 	            final boolean hasFocus) {
 	        super.getTreeCellRendererComponent(
 	                tree, ((FileTreeModel.Node) value).getLabel(), selected, expanded, leaf, row, hasFocus);
 	        setOpaque(false);
 	        setBorder(new EmptyBorder(1, 0, 1, 0));
 	        setIcon(getNodeIcon((Node) value));
 
             for (DecorationRenderer renderer : decorationRenderers) {
                 renderer.renderDecoration((Node) value, this);
             }
 
 	        return this;
 	    }
 
 		public Color getBackground() {
 	    	return null;
 	    }
 
 		private Icon getNodeIcon(Node node) {
             File file = node.getFile();
 		    if (file != null && file.isDirectory()) {
                 return iconProvider.getIcon(IconProvider.Type.FOLDER);
             } else {
                 return iconProvider.getIcon(IconProvider.Type.FILE);
 			}
 	    }
 	}
 }
