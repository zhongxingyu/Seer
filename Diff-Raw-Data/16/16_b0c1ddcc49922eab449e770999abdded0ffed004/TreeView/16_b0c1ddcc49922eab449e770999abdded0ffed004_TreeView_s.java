 
 package GUI;
 
 import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.swing.*;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
 
 public class TreeView extends JPanel {
     private JTree tree;
    private DocumentGUI doc;
    private String title;
     private JScrollPane scrollPane;
     
     public TreeView(DocumentGUI docParam, String titleParam, DefaultMutableTreeNode node){
         setLayout(new BorderLayout());
     	tree = new JTree(node);
     	for (int i = 0; i < tree.getRowCount(); i++) {
             tree.expandRow(i);
     	}
     	tree.setRootVisible(true);
     	DefaultTreeCellRenderer cellRenderer= new DefaultTreeCellRenderer();
     	cellRenderer.setLeafIcon(null);
     	cellRenderer.setOpenIcon(null);
     	cellRenderer.setClosedIcon(null);
     	tree.setRowHeight(25);
     	tree.setCellRenderer(cellRenderer);
     	tree.setFont(new Font(Font.DIALOG,Font.PLAIN,20));
         scrollPane = new JScrollPane(tree);
        add(scrollPane);
        
         setPreferredSize(new Dimension(300, 500));
     }
 
 }
 
