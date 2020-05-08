 /*
  * XTree.java
  *
  * Created on August 2, 2010, 10:27 AM
  * @author jaycverg
  */
 
 package com.rameses.rcp.control;
 
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.common.Node;
 import com.rameses.rcp.common.NodeFilter;
 import com.rameses.rcp.common.NodeListener;
 import com.rameses.rcp.common.PropertySupport;
 import com.rameses.rcp.common.TreeNodeModel;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.rcp.framework.NavigatablePanel;
 import com.rameses.rcp.framework.NavigationHandler;
 import com.rameses.rcp.ui.UIControl;
 import com.rameses.rcp.util.UIControlUtil;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JTree;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 public class XTree extends JTree implements UIControl 
 {
     private DefaultProvider provider = new DefaultProvider(); 
     private TreeEventSupport eventSupport = new TreeEventSupport();
     
     private Binding binding;
     private String[] depends;
     private String handler;    
     private Object handlerObject;
     private boolean dynamic;
     private int index;
     
     private NodeTreeRenderer renderer;
     private DefaultMutableTreeNode root;
     private DefaultTreeModel model;
     private TreeNodeModel nodeModel;
             
     public XTree() { 
         initComponents(); 
     } 
     
     // <editor-fold defaultstate="collapsed" desc=" initComponents ">
     
     private void initComponents() 
     {
         getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         setCellRenderer(renderer=new NodeTreeRenderer()); 
 
         //install listeners
         super.addTreeSelectionListener(eventSupport); 
         
         getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openNode");
         getActionMap().put("openNode", new AbstractAction(){             
             public void actionPerformed(ActionEvent e) {
                 fireOpenSelectedNode();
             } 
         });
         
         setBorder(BorderFactory.createEmptyBorder(3, 2, 0, 0)); 
         addAncestorListener(new AncestorListener() {
             private boolean inited;
             
             public void ancestorAdded(AncestorEvent event) 
             {
                 if (inited) return;
                 
                 inited = true; 
                 JComponent owner = XTree.this;
                 Container parent = owner.getParent(); 
                 if (parent instanceof JViewport) 
                 {
                     JViewport jv = (JViewport) parent;
                     jv.setBackground(owner.getBackground()); 
                 }
             }
             public void ancestorMoved(AncestorEvent event) {
             }
             public void ancestorRemoved(AncestorEvent event) {
             }
         });
     }
     
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc=" Getters and Setters ">
     
     public boolean isDynamic() { return dynamic; }    
     public void setDynamic(boolean dynamic) { this.dynamic = dynamic; }
     
     public String getHandler() { return handler; }    
     public void setHandler(String handler) { this.handler = handler; }
     
     public Object getHandlerObject() { return handlerObject; }
     public void setHandlerObject(Object handlerObject) {
         this.handlerObject = handlerObject; 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" UIControl implementation ">
     
     public Binding getBinding() { return binding; }    
     public void setBinding(Binding binding) { this.binding = binding; }
     
     public String[] getDepends() { return depends; }    
     public void setDepends(String[] depends) { this.depends = depends; }
     
     public int getIndex() { return index; }    
     public void setIndex(int index) { this.index = index; }
 
     public int compareTo(Object o) { 
         return UIControlUtil.compare(this, o);
     }
     
     public void setPropertyInfo(PropertySupport.PropertyInfo info) {}
     
     public void refresh() {}
     
     public void load() 
     {
         try 
         {
             Object obj = getHandlerObject(); 
             if (obj instanceof String) 
                 obj = UIControlUtil.getBeanValue(this, obj.toString());
 
             String shandler = getHandler(); 
             if (shandler != null) 
                 obj = UIControlUtil.getBeanValue(this, shandler);
 
             if (obj == null) throw new Exception("A handler must be provided");
 
             nodeModel = (TreeNodeModel) obj;
         }
         catch(Exception ex) 
         {
             nodeModel = new DummyTreeNodeModel();
             
             if (ClientContext.getCurrentContext().isDebugMode()) 
                 ex.printStackTrace(); 
         }
         
         nodeModel.setProvider(provider); 
 
         Node rootNode = nodeModel.getRootNode(); 
         if (rootNode == null) 
         {
             rootNode = new Node("root", "");
             setRootVisible(false); 
         } 
         else {
             setRootVisible(nodeModel.isRootVisible()); 
         }
 
         rootNode.setId("root");
         root = new DefaultNode(rootNode); 
         model = new DefaultTreeModel(root, true);
         //treat items w/ no children as folders unless explicitly defined as leaf
         model.setAsksAllowsChildren(true);         
         setModel(model); 
     }
 
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" Owned and helper methods ">
 
     public void removeTreeSelectionListener(TreeSelectionListener handler) {
         eventSupport.remove(handler); 
     }
     public void addTreeSelectionListener(TreeSelectionListener handler) {
         eventSupport.add(handler); 
     }
     
     protected void processMouseEvent(MouseEvent me) 
     {
         if (me.getID() == MouseEvent.MOUSE_CLICKED) {
             if (SwingUtilities.isLeftMouseButton(me)) {
                 if (me.getClickCount() == 2) {
                     fireOpenSelectedNode();
                 } else { 
                     fireOpenSelectedNode(); 
                 } 
             } 
         }
         super.processMouseEvent(me); 
     }
     
     private void fireOpenSelectedNode() 
     {        
         final DefaultNode selNode = getSelectedNode(); 
         if (selNode == null) return;
         if (!selNode.hasChanged) return;
 
         selNode.hasChanged = false;        
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try { 
                     openNode(selNode.getNode());
                 } catch(Exception ex) {
                     MsgBox.err(ex); 
                 } finally {
                     
                 }
             }
         });
     }
         
     private XTree.DefaultNode getSelectedNode() 
     {
         TreePath treePath = getSelectionPath();
         if (treePath == null) return null; 
         
         return (DefaultNode) treePath.getLastPathComponent(); 
     }
     
     private void openNode(Node node) 
     {
         Object retVal = null;
         if (node == null) {
             //do nothing
         } else if (node.isLeaf()) {
             retVal = nodeModel.openLeaf(node);
         } else {
             retVal = nodeModel.openFolder(node);
         }
         
         if (retVal == null) return;
         
         NavigationHandler handler = ClientContext.getCurrentContext().getNavigationHandler();
         NavigatablePanel panel = UIControlUtil.getParentPanel(this, null);
         handler.navigate(panel, this, retVal);
     }
     
     private Node doFindNode(DefaultNode parent, NodeFilter filter) 
     {
         for (int i = 0; i < parent.getChildCount(); i++) 
         {
             DefaultNode child = (DefaultNode) parent.getChildAt(i);
             Node n = child.getNode();
             if (filter.accept(n)) return n;
             
             if (n.isLoaded() && child.getChildCount() > 0) 
             {
                 Node nn = doFindNode(child, filter);
                 if (nn != null) return nn;
             }
         }
         return null;
     }
     
     private void doCollectNodeList(DefaultNode parent, NodeFilter filter, List nodes) 
     {
         for (int i=0; i < parent.getChildCount(); i++) 
         {
             DefaultNode oChild = (DefaultNode) parent.getChildAt(i);
             Node oNode = oChild.getNode();
             if (filter.accept(oNode)) nodes.add(oNode);
             
             if (oNode.isLoaded() && oChild.getChildCount() > 0) 
                 doCollectNodeList(oChild, filter, nodes);
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" DefaultNode (class) ">
     
     public class DefaultNode extends DefaultMutableTreeNode implements NodeListener 
     {        
         XTree root = XTree.this;
         private Node node;
         private boolean hasChanged;
         private Node[] nodes;
         
         public DefaultNode(String n) {
             super(n); 
         }
         
         public DefaultNode(Node node) { 
             this(node, null); 
         } 
         
         public DefaultNode(Node node, Node parent) {
             super(node.getCaption(), !node.isLeaf());
             this.node = node;
             this.node.setParent(parent);
             this.node.addListener(this); 
             this.node.setProvider(new DefaultNodeProvider(this));
         } 
         
         public Node getNode() { return node; }
         
         public int size() { return super.getChildCount(); } 
                 
         public int getChildCount() {
             if (!node.isLoaded()) {
                 synchronized(this) {
                     node.setLoaded(true);
                     hasChanged = true;
                     loadChildren(); 
                 }
             }
             return super.getChildCount();
         }
         
         public void loadChildren() {
             Node[] nodes = nodeModel.fetchNodes(node);            
             if (nodes == null) return;
 
             super.removeAllChildren();             
             for (Node n: nodes) { 
                 if (n == null) continue; 
                 
                 this.add(new DefaultNode(n, getNode())); 
             } 
         }
         
         public void reload() {
             if (!node.isLoaded()) return;
             
             synchronized(this) {
                 loadChildren();
                 root.model.reload(this);
             }
         } 
         
         List<Node> getItems() {
             List<Node> nodes = new ArrayList();
             Enumeration en = super.children();
             while (en.hasMoreElements()) { 
                 Object item = en.nextElement(); 
                 XTree.DefaultNode dNode = (XTree.DefaultNode) item; 
                 nodes.add(dNode.getNode()); 
             } 
             return nodes; 
         } 
 
         public void insert(MutableTreeNode newChild, int childIndex) {
             super.insert(newChild, childIndex);
         }
 
         public void remove(int childIndex) {
             super.remove(childIndex);
         }
     } 
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" DefaultNodeProvider (class) ">
     
     private class DefaultNodeProvider implements Node.Provider 
     {
         XTree root = XTree.this;
         private DefaultNode treeNode;
         private Node userNode;
         
         DefaultNodeProvider(DefaultNode treeNode) {
             this.treeNode = treeNode;
             this.userNode = treeNode.getNode();
             if (this.userNode != null)
                 this.userNode.setProvider(this);
         }
         
         public int getIndex() {
             TreeNode parent = treeNode.getParent(); 
             return (parent == null? -1: parent.getIndex(treeNode)); 
         }
         
         public boolean hasItems() {
             Enumeration en = treeNode.children();
             return (en == null? false: en.hasMoreElements()); 
         } 
         
         public void reloadItems() { 
             treeNode.loadChildren(); 
             root.model.nodeStructureChanged(treeNode); 
         } 
         
         public List<Node> getItems() { 
             return treeNode.getItems(); 
         } 
 
         public void select() {
             TreeNode[] treeNodes = treeNode.getPath();
             if (treeNodes == null || treeNodes.length == 0) return;
             
             treeNode.loadChildren();
             root.setSelectionPath(new TreePath(treeNodes)); 
         }
 
         public Object open() {
             select();
             
             if (userNode == null) 
                 return null; 
             else if (userNode.isLeaf())
                 return nodeModel.openLeaf(userNode);
             else 
                 return nodeModel.openFolder(userNode); 
         } 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" NodeTreeRenderer (class) ">
     
     private class NodeTreeRenderer extends DefaultTreeCellRenderer 
     {  
         XTree root = XTree.this;
 
         public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) 
         {
             super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
             super.setText(value+"");
             super.setToolTipText(value+"");
             super.setBorder( BorderFactory.createEmptyBorder(2,2,2,5) );
             
             Icon oIcon = lookupIcon(root.nodeModel.getIcon());
             if (oIcon != null) setIcon(oIcon);
             
             if (value != null && (value instanceof DefaultNode)) {
                 Node n = ((DefaultNode)value).getNode();
                 if (n != null) {
                     if (n.getIcon() != null) {
                         oIcon = lookupIcon(n.getIcon());
                         if (oIcon != null) super.setIcon(oIcon);
                     }
                     
                     if (n.getTooltip() !=null) 
                         super.setToolTipText(n.getTooltip());
                 }
             }
             return this;
         }   
         
         private Icon lookupIcon(String name) {
             try { 
                 Icon icon = ControlSupport.getImageIcon(name);
                 if (icon == null) icon = UIManager.getIcon(name);
                 
                 return icon;
             } catch(Throwable t) {
                 return null;
             }
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" DummyTreeNodeModel (class) "> 
     
     private class DummyTreeNodeModel extends TreeNodeModel 
     {
         public Node[] fetchNodes(Node node) { return null; }
 
         public Node getRootNode() { 
             return new Node("root", "Default");
         }
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc=" DefaultProvider (class) "> 
     
     private class DefaultProvider implements TreeNodeModel.Provider 
     {
         XTree root = XTree.this; 
         
         public Node getSelectedNode() 
         {
             DefaultNode defNode = root.getSelectedNode(); 
             return (defNode == null? null: defNode.getNode()); 
         }
         
         public Node findNode(NodeFilter filter) 
         {
             DefaultNode parent = (DefaultNode) root.model.getRoot();
             Node n = parent.getNode();
             if (filter.accept(n)) return n;
 
             return root.doFindNode(parent, filter);
         }
 
         public List<Node> findNodes(NodeFilter filter) 
         {
             List<Node> nodes = new ArrayList();
             DefaultNode parent = (DefaultNode) root.model.getRoot();
 
             Node n = parent.getNode();
             if (filter.accept(n)) nodes.add(n);
 
             root.doCollectNodeList(parent, filter, nodes);      
             return nodes;
         }      
         
         public List<Node> children() {
             DefaultNode defNode = root.getSelectedNode(); 
             if (defNode == null) return null; 
 
             List<Node> list = new ArrayList();            
             Enumeration en = defNode.children(); 
             while (en.hasMoreElements()) {
                 DefaultNode dn = (DefaultNode) en.nextElement(); 
                 list.add(dn.getNode()); 
             }
             return list; 
         }
         
         
     }
             
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" TreeEventSupport (class) "> 
    
     private class TreeEventSupport implements TreeSelectionListener 
     {
         XTree root = XTree.this; 
         
         private Node oldNode;
         private List<TreeSelectionListener> selectionHandlers = new ArrayList(); 
         
         void add(TreeSelectionListener handler) 
         {
             if (handler != null && !selectionHandlers.contains(handler)) 
                 selectionHandlers.add(handler); 
         }
         
         void remove(TreeSelectionListener handler) 
         {
             if (handler != null) selectionHandlers.remove(handler); 
         }
         
         public void valueChanged(final TreeSelectionEvent evt) 
         {
             try {
                 if (root.getName() != null) {
                     boolean nodeHasChanged = false;
                     XTree.DefaultNode selNode = getSelectedNode(); 
                     Node node = (selNode == null? null: selNode.getNode()); 
                     if (oldNode != null && node != null && oldNode.equals(node)) 
                         nodeHasChanged = false; 
                     else 
                         nodeHasChanged = true;
                     
                     if (selNode != null && !selNode.hasChanged) 
                         selNode.hasChanged = nodeHasChanged;
                     
                     UIControlUtil.setBeanValue(root.getBinding(), root.getName(), node); 
                     oldNode = node;                    
                     EventQueue.invokeLater(new Runnable(){
                         public void run() {
                             fireChangeNode(evt); 
                         }    
                     });
                 }
             }
             catch(Exception ex) { 
                 MsgBox.err(ex);  
             }            
         } 
         
         private void fireChangeNode(TreeSelectionEvent evt) {
             //notify dependencies that the node has changed
             root.getBinding().notifyDepends(root, root.getName(), false); 
             //fire onChangeNode on the TreeNodeModel
             //Node node = root.nodeModel.getSelectedNode(); 
             //Object result = root.nodeModel.onChangeNode(node); 
             //if (result != null) root.getBinding().fireNavigation(result, null, false); 
             
             for (TreeSelectionListener handler : selectionHandlers) {
                 handler.valueChanged(evt); 
             }
         } 
     }
     
     // </editor-fold>     
 }
