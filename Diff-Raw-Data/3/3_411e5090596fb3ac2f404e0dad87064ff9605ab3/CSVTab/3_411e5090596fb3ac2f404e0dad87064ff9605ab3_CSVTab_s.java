 package edu.usc.chla.vpicu.explorer.newui.connection;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.AbstractButton;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.ToolTipManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeCellRenderer;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.springframework.dao.DataAccessException;
 
 import edu.usc.chla.vpicu.explorer.BaseProvider;
 import edu.usc.chla.vpicu.explorer.H2Provider;
 
 public class CSVTab extends ConnectionTab implements SaveFileCallback {
 
   private static final long serialVersionUID = 1L;
 
   public static final String TABLENAME = "Table Name";
   public static final String CSVFILE = "CSV File";
   public static final String ADDTABLE = "Add Table";
   public static final String REMOVETABLE = "Remove Table";
   public static final String SAVEDB = "Save Database ...";
 
   private static final ImageIcon TABLE_ICON = new ImageIcon(CSVTab.class.getClassLoader().getResource("table.png"));
 
   protected final Map<String, JButton> buttons = new HashMap<String, JButton>();
   protected final Map<String, FileChooserButton> choosers = new HashMap<String, FileChooserButton>();
   private JTree tree;
   private CSVTreeModel tmodel;
   private File h2db;
 
   public CSVTab() {
     FileChooserButton c = new FileChooserButton(CSVFILE, null);
     c.setSuffix(".csv");
     choosers.put(CSVFILE, c);
     add(c, gbc(0,row,1,1,0.5,0,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL));
 
     JTextField f = new HintTextField(TABLENAME);
     fields.put(TABLENAME, f);
     add(f, gbc(1,row,1,1,0.5,0,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL));
 
     JButton b = new JButton(ADDTABLE);
     b.setEnabled(false);
     buttons.put(ADDTABLE, b);
     add(b, gbc(2,row++,1,1,0,0,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL));
 
     JScrollPane sp = new JScrollPane(createImportTree());
     add(sp, gbc(0,row,2,2,1,1,GridBagConstraints.LINE_START,GridBagConstraints.BOTH));
 
     b = new JButton(REMOVETABLE);
     b.setEnabled(false);
     buttons.put(REMOVETABLE, b);
     add(b, gbc(2,row++,1,1,0,0,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL));
 
     c = new FileChooserButton(SAVEDB, null);
     c.setMode(FileDialog.SAVE);
     c.setSaveFileCallback(this);
     c.setEnabled(false);
     choosers.put(SAVEDB, c);
     add(c, gbc(2,row++,1,1,0,0,GridBagConstraints.NORTHWEST,GridBagConstraints.HORIZONTAL));
 
     // enable import button when table is named and file is chosen
     ImportButtonEnabledListener importListener = new ImportButtonEnabledListener(
         fields.get(TABLENAME), choosers.get(CSVFILE), buttons.get(ADDTABLE));
     choosers.get(CSVFILE).addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, importListener);
     fields.get(TABLENAME).getDocument().addDocumentListener(importListener);
 
     addImportTreeListeners();
   }
 
   @Override
   public void saveFile(File selectedFile) {
     h2db = selectedFile;
     H2Provider p = new H2Provider(h2db, "sa", "");
     Map<String, List<String>> tableMap = tmodel.getTableMap();
     for (String table : tableMap.keySet()) {
       StringBuilder sql = new StringBuilder("CREATE TABLE ").append(table).append(" AS ");
       for (String path : tableMap.get(table)) {
         sql.append("SELECT * FROM CSVREAD('").append(path).append("') UNION ALL ");
       }
       sql.setLength(sql.lastIndexOf(" UNION ALL "));
       try {
         p.execute(sql.toString());
       } catch (DataAccessException e) {
         JTextArea area = new JTextArea(MessageFormat.format("Error creating table \"{0}\" for database \"{1}\":\n\n{2}",
             table, h2db.getName(), e.getMessage()));
         area.setLineWrap(true);
         area.setWrapStyleWord(true);
         area.setEditable(false);
         area.setPreferredSize(new Dimension(300,300));
         JOptionPane.showMessageDialog(this, new JScrollPane(area), "Error creating H2 database", JOptionPane.ERROR_MESSAGE);
       }
     }
   }
 
   @Override
   public BaseProvider getProvider() {
     if (h2db == null) {
       try {
         h2db = File.createTempFile("csv", ".tmp");
         h2db.deleteOnExit();
         saveFile(h2db);
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
     return new H2Provider(h2db, "sa", "");
   }
 
   private JTree createImportTree() {
     tmodel = new CSVTreeModel(new DefaultMutableTreeNode());
     tree = new JTree(tmodel);
     tree.setRootVisible(false);
     tree.setShowsRootHandles(true);
     tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
     tree.setCellRenderer(new TooltipCellRenderer());
     ToolTipManager.sharedInstance().registerComponent(tree);
 
     // enable "Delete" button when row selected
     tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
 
       @Override
       public void valueChanged(TreeSelectionEvent e) {
         buttons.get(REMOVETABLE).setEnabled(!tree.isSelectionEmpty());
         if (!tree.isSelectionEmpty())
           buttons.get(REMOVETABLE).setText(e.getPath().getPathCount() == 3 ? "Remove File" : REMOVETABLE);
       }
 
     });
 
     // enable "Save" button when row exists
     tmodel.addTreeModelListener(new TreeModelListener() {
 
       @Override
       public void treeNodesChanged(TreeModelEvent e) {
       }
 
       @Override
       public void treeNodesInserted(TreeModelEvent e) {
         choosers.get(SAVEDB).setEnabled(true);
       }
 
       @Override
       public void treeNodesRemoved(TreeModelEvent e) {
         choosers.get(SAVEDB).setEnabled(!tmodel.getRoot().isLeaf());
       }
 
       @Override
       public void treeStructureChanged(TreeModelEvent e) {
       }
 
     });
     return tree;
   }
 
   private void addImportTreeListeners() {
     // create row when "Add Table" button clicked
     buttons.get(ADDTABLE).addActionListener(new ActionListener() {
 
       @Override
       public void actionPerformed(ActionEvent e) {
         String file = choosers.get(CSVFILE).getSelectedFile().getAbsolutePath();
         String name = fields.get(TABLENAME).getText();
         tree.expandPath(tmodel.add(name, file).getParentPath());
         // clear selection
         choosers.get(CSVFILE).clearSelectedFile();
         fields.get(TABLENAME).setText("");
       }
 
     });
 
     // delete row when "Remove Table" button clicked
     buttons.get(REMOVETABLE).addActionListener(new ActionListener() {
 
       @Override
       public void actionPerformed(ActionEvent e) {
         tmodel.remove(tree.getSelectionPath());
       }
 
     });
   }
 
   private static class TooltipCellRenderer implements TableCellRenderer, TreeCellRenderer {
     private final DefaultTableCellRenderer tableCR = new DefaultTableCellRenderer();
     private final DefaultTreeCellRenderer treeCR = new DefaultTreeCellRenderer();
     @Override
     public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column) {
       JLabel l = (JLabel)tableCR.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
       l.setToolTipText(value.toString());
       return l;
     }
     @Override
     public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
         boolean leaf, int row, boolean hasFocus) {
       JLabel l = (JLabel)treeCR.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
       if (!leaf)
         l.setIcon(TABLE_ICON);
       l.setToolTipText(value.toString());
       return l;
     }
   }
 
   private static class CSVTreeModel extends DefaultTreeModel {
 
     private static final long serialVersionUID = 1L;
 
     public CSVTreeModel(DefaultMutableTreeNode root) {
       super(root);
     }
 
     @Override
     public DefaultMutableTreeNode getRoot() {
       return (DefaultMutableTreeNode)root;
     }
 
     @SuppressWarnings("unchecked")
     public TreePath add(String tableName, String csvPath) {
       TreePath path = new TreePath(getRoot());
       Enumeration<DefaultMutableTreeNode> tables = getRoot().children();
       DefaultMutableTreeNode tableNode = null;
       while (tables.hasMoreElements()) {
         DefaultMutableTreeNode n = tables.nextElement();
         if (n.getUserObject().equals(tableName)) {
           tableNode = n;
           break;
         }
       }
       if (tableNode == null) {
         tableNode = new DefaultMutableTreeNode(tableName);
         insertNodeInto(tableNode, getRoot(), getRoot().getChildCount());
       }
       path = path.pathByAddingChild(tableNode);
       DefaultMutableTreeNode child = new DefaultMutableTreeNode(csvPath, false);
       insertNodeInto(child, tableNode, tableNode.getChildCount());
       return path.pathByAddingChild(child);
     }
 
     public void remove(TreePath path) {
       if (path.getPathCount() == 3) { // csv file
         DefaultMutableTreeNode tableNode = (DefaultMutableTreeNode)path.getPathComponent(1);
         if (tableNode.getChildCount() == 1)
           removeNodeFromParent(tableNode);
         else
           removeNodeFromParent((DefaultMutableTreeNode)path.getLastPathComponent());
       }
       else if (path.getPathCount() == 2) { // table name
         removeNodeFromParent((DefaultMutableTreeNode)path.getLastPathComponent());
       }
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, List<String>> getTableMap() {
       Map<String, List<String>> map = new HashMap<String, List<String>>();
       Enumeration<DefaultMutableTreeNode> tables = getRoot().children();
       while (tables.hasMoreElements()) {
         List<String> files = new ArrayList<String>();
         DefaultMutableTreeNode t = tables.nextElement();
         map.put((String)t.getUserObject(), files);
         Enumeration<DefaultMutableTreeNode> fileNodes = t.children();
         while (fileNodes.hasMoreElements()) {
           files.add((String)fileNodes.nextElement().getUserObject());
         }
       }
       return map;
     }
 
   }
 
   private static class ImportButtonEnabledListener implements PropertyChangeListener, DocumentListener {
     private final JTextField field;
     private final FileChooserButton chooser;
     private final JButton button;
     public ImportButtonEnabledListener(JTextField field, FileChooserButton chooser, JButton button) {
       this.field = field;
       this.chooser = chooser;
       this.button = button;
     }
     private void update() {
       button.setEnabled(!field.getText().isEmpty() && chooser.getSelectedFile() != null);
     }
     @Override
     public void insertUpdate(DocumentEvent e) {
       update();
     }
     @Override
     public void removeUpdate(DocumentEvent e) {
       update();
     }
     @Override
     public void changedUpdate(DocumentEvent e) {
       update();
     }
     @Override
     public void propertyChange(PropertyChangeEvent evt) {
       if (evt.getPropertyName().equals(AbstractButton.TEXT_CHANGED_PROPERTY)) {
         // auto fill "Table Name" field (which triggers update())
         File f = chooser.getSelectedFile();
         if (f != null) {
           String basename = f.getName();
           int i = basename.lastIndexOf('.');
           String name = basename.substring(0, i);
           field.setText(name);
         }
       }
     }
   }
 
 }
