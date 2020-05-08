 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.detailviews;
 
 /**
  *
  * @author jackie
  */
 import java.awt.*;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.MouseEvent;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.tree.*;
 import org.openide.util.Lookup;
 import ovation.DataContext;
 import ovation.IEntityBase;
 import ovation.LogLevel;
 import ovation.Ovation;
 import ovation.User;
 import us.physion.ovation.interfaces.ConnectionProvider;
 import us.physion.ovation.interfaces.IEntityWrapper;
 
 public class TreeWithTableRenderer extends JScrollPane {
 
     private static final long serialVersionUID = 1L;
     private JTree tree;
     Set<String> uris;
 
     public void setEntities(Collection<? extends IEntityWrapper> entities) {
         DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection().getContext();
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
 
         uris.clear();
         Set<IEntityBase> entitybases = new HashSet();
         Set<String> owners = new HashSet();
         for (IEntityWrapper w : entities) {
                 IEntityBase e = w.getEntity();
                 entitybases.add(e);
                 uris.add(e.getURIString());
                 owners.add(e.getOwner().getUuid());
         }
        Iterator<User> users = c.getUsersIterator();
         while (users.hasNext()) {
             User u = users.next();
             Map<String, Object> userProps = new HashMap();
             for (IEntityBase e : entitybases) {
                 userProps.putAll(e.getUserProperties(u));
             }
             if (!userProps.isEmpty()) {
                 String uuid = u.getUuid();
                 UserPropertySet propertySet = new UserPropertySet(u, owners.contains(uuid), c.currentAuthenticatedUser().getUuid().equals(uuid), userProps, uris);
                 DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(propertySet.getDisplayName());
                 userNode.add(new TableNode(propertySet));
 
                 root.add(userNode);
             }
        }
 
         //((DefaultMutableTreeNode)((DefaultTreeModel) tree.getModel()).getRoot()).removeAllChildren();
         ((DefaultTreeModel) tree.getModel()).setRoot(root);
     }
 
     public TreeWithTableRenderer() {
         super();
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
         tree = new JTree(root);
        tree.setRootVisible(false);
         TableInTreeCellRenderer r = new TableInTreeCellRenderer();
         tree.setCellRenderer(r);
         tree.setRowHeight(0);
         tree.setEditable(true);
         tree.setCellEditor(r);
         
         uris = new HashSet<String>();
        //tree.setRootVisible(false);
         getViewport().add(tree, null);
         
     }
 
     class TableInTreeCellRenderer extends AbstractCellEditor implements
             TreeCellEditor, TreeCellRenderer {
 
         TablePanel currentPanel;
         boolean trueFalse = true;
         
         boolean isCurrentUser;
         Map<String, TablePanel> tableLookup;
         
         public TableInTreeCellRenderer()
         {
             super();
             tableLookup = new HashMap<String, TablePanel>();
             //this.addCellEditorListener(new PropertyTableModelListener(new HashSet<String>()));
         }
         
         public Component getTreeCellRendererComponent(JTree tree, Object value,
                 boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
             final Object o = ((DefaultMutableTreeNode) value).getUserObject();
             if (o instanceof String) {
                 return new JLabel((String) o);
             }
 
                 // otherwise we expect it to be UserPropertySet object
                 //table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
             if (o instanceof UserPropertySet)
             {   
                 String user = ((UserPropertySet)o).getURI();
                 //lookup tables
                 TablePanel panel;
                 if (tableLookup.containsKey(user))
                 {
                     panel = tableLookup.get(user);
                 }else{
                     
                     if (((UserPropertySet) o).isCurrentUser())
                     {
                         panel = new EditableTable(new JTable());
                     }else{
                         panel = new NonEditableTable(new JTable());
                     }
                    
                     tableLookup.put(user, panel);
                 }
 
                 Map<String, Object> props = ((UserPropertySet) o).getProperties();
                 Object[][] dataVector = new Object[props.size()][2];
                 int i=0;
                 for (Map.Entry<String, Object> entry : props.entrySet()) {
                     dataVector[i][0] = entry.getKey();
                     dataVector[i][1] = entry.getValue();
                     i++;
                 }
                 String[] columnNames = {"property", "value"};
                 DefaultTableModel tableModel = new DefaultTableModel(columnNames, props.size());
                 tableModel.setDataVector(dataVector, columnNames);
 
                 JTable table = panel.getTable();
                 table.setModel(tableModel);
                 if (((UserPropertySet) o).isCurrentUser()) {
                     tableModel.addTableModelListener(new PropertyTableModelListener(uris, tree,(TableNode) value ));
                 }
                 //table.setPreferredScrollableViewportSize(table.getPreferredSize());
 
                 //panel.setSize(new Dimension(500, 500));
                 //table.setSize(panel.getSize());
                 
                 return panel.getPanel();
 
             }
             return null;
         }
         
         @Override
         public Object getCellEditorValue() {
             return null;//currentPanel
         }
        /* 
         @Override
         public void cancelCellEditing() {
             System.out.println("cancelling cell editing");
             return;
         }
         
         @Override
         public boolean stopCellEditing() {
             System.out.println("stopping cell editing");
             return true;
         }
         
         @Override
         protected void fireEditingCanceled() {
             System.out.println("Editing cancelled");
             return;
         }
         
         @Override
         protected void fireEditingStopped() {
             System.out.println("Editing stopped");
             return;
         }*/
 
         @Override
         public boolean isCellEditable(final EventObject event) {
             
             Object node;
             if (event instanceof MouseEvent)
             {
                 TreePath p = tree.getPathForLocation(((MouseEvent)event).getX(), ((MouseEvent)event).getY());
                 node = p.getLastPathComponent();
             }
             else{
                 node = tree.getLastSelectedPathComponent();
             }
             if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
                 DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                 
                 Object p = treeNode.getUserObject();
                 if (p instanceof UserPropertySet)
                 {
                     return ((UserPropertySet)p).isCurrentUser();
                 }
             }
             return false;
         }
 
         @Override
         public Component getTreeCellEditorComponent(
                 final JTree tree,
                 final Object value,
                 final boolean isSelected,
                 final boolean expanded,
                 final boolean leaf,
                 final int row) {
             tree.setRowHeight(0);
             Object o = ((DefaultMutableTreeNode) value).getUserObject();
             if (o instanceof UserPropertySet)
             {
                 currentPanel = tableLookup.get(((UserPropertySet)o).getURI());
                 return currentPanel.getPanel();
             }
             else{
                 Component editor = getTreeCellRendererComponent(tree,
                     value, true, expanded, leaf, row, true);
                 currentPanel = null;
                 return editor;
             }
         }
         
        public void valueForPathChanged(TreePath path, Object newValue) {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                 System.out.println(newValue.getClass() + " newValue: " + newValue);
         }
 
         public void itemStateChanged(ItemEvent itemEvent) {
             if (stopCellEditing()) {
                 System.out.println("Stopped editing cell");
                 fireEditingStopped();
             }
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 //new TreeWithTableRenderer().setVisible(true);
             }
         });
     }
 }
