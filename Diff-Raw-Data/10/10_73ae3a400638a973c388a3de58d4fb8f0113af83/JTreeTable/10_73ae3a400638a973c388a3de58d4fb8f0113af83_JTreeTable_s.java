 package edu.wustl.cab2b.client.ui.treetable;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.tree.DefaultTreeSelectionModel;
 import javax.swing.tree.TreeModel;
 
 import org.jdesktop.swingx.JXTable;
 
 public class JTreeTable extends JXTable {
 
     protected TreeTableCellRenderer tree;
 
     public JTreeTable(TreeTableModel treeTableModel) {
         super();
 
         // Create the tree. It will be used as a renderer and editor. 
         tree = new TreeTableCellRenderer(treeTableModel);
 
         // Install a tableModel representing the visible rows in the tree. 
         super.setModel(new TreeTableModelAdapter(treeTableModel, tree));
 
         // Force the JTable and JTree to share their row selection models. 
         tree.setSelectionModel(new DefaultTreeSelectionModel() {
             // Extend the implementation of the constructor, as if: 
             /* public this() */{
                 setSelectionModel(listSelectionModel);
             }
         });
         // Make the tree and table row heights the same. 
         tree.setRowHeight(getRowHeight());
 
         // Install the tree editor renderer and editor. 
         setDefaultRenderer(TreeTableModel.class, tree);
         setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
 
         setShowGrid(true);
         setIntercellSpacing(new Dimension(1, 1));
        setBackground(Color.white);
     }
 
     /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
      * paint the editor. The UI currently uses different techniques to 
      * paint the renderers and editors and overriding setBounds() below 
      * is not the right thing to do for an editor. Returning -1 for the 
      * editing row in this case, ensures the editor is never painted. 
      */
     public int getEditingRow() {
         return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
     }
 
     // 
     // The renderer used to display the tree nodes, a JTree.  
     //
 
     public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
 
         protected int visibleRow;
 
         public TreeTableCellRenderer(TreeModel model) {
             super(model);
         }
 
         public void setBounds(int x, int y, int w, int h) {
             super.setBounds(x, 0, w, JTreeTable.this.getHeight());
         }
 
         public void paint(Graphics g) {
             g.translate(0, -visibleRow * getRowHeight());
             super.paint(g);
         }
 
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
             if (isSelected)
                 setBackground(table.getSelectionBackground());
             else
                 setBackground(table.getBackground());
 
             visibleRow = row;
             return this;
         }
     }
 
     // 
     // The editor used to interact with tree nodes, a JTree.  
     //
 
     public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
             return tree;
         }
     }
 
 }
