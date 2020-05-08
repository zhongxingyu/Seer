 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.gui.rulesetmanager;
 
 import com.sun.org.apache.xerces.internal.dom.ParentNode;
 import evopaint.Configuration;
 import evopaint.gui.rulesetmanager.util.NamedObjectListCellRenderer;
 import evopaint.pixel.rulebased.RuleSet;
 import evopaint.pixel.rulebased.RuleSetCollection;
 import evopaint.pixel.rulebased.interfaces.IRule;
 import evopaint.util.CollectionNode;
 import evopaint.util.RuleSetNode;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author tam
  */
 public class JRuleSetBrowser extends JPanel {
     private Configuration configuration;
     JRuleSetTree tree;
     DefaultTreeModel treeModel;
     DefaultMutableTreeNode root;
     Component newDialogOwner;
 
     public JRuleSetBrowser(Configuration configuration, JRuleSetTree tree) {
         this.configuration = configuration;
         this.tree = tree;
         this.treeModel = (DefaultTreeModel)tree.getModel();
         this.root = (DefaultMutableTreeNode)treeModel.getRoot();
         
         setLayout(new BorderLayout());
         setBackground(Color.WHITE);
         setBorder(new LineBorder(Color.GRAY));
 
         // don't set the preferred size! set the divider location instead
         // or else the scrollpane will scroll even if the tree is empty
         //setPreferredSize(new Dimension(250, 250));
 
         JScrollPane scrollPaneForTree = new JScrollPane(tree,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPaneForTree.setBorder(null);
         scrollPaneForTree.setViewportBorder(null);
 
         // create buttons for control panel of browser
         JPanel controlPanel = new JPanel();
         controlPanel.setBackground(new Color(0xF2F2F5));
         JButton browserBtnNew = new JButton("New...");
         newDialogOwner = browserBtnNew;
         browserBtnNew.addActionListener(new BrowserBtnNewListener());
         controlPanel.add(browserBtnNew);
         JButton browserBtnCopy = new JButton("Copy");
         browserBtnCopy.addActionListener(new BtnCopyListener());
         controlPanel.add(browserBtnCopy);
         JButton browserBtnDelete = new JButton("Delete");
         browserBtnDelete.addActionListener(new BtnDeleteListener());
         controlPanel.add(browserBtnDelete);
 
         add(scrollPaneForTree, BorderLayout.CENTER);
         add(controlPanel, BorderLayout.SOUTH);
     }
 
     private class BrowserBtnNewListener implements ActionListener {
         private JDialog dialog;
         private JRadioButton collectionRadio;
         private JRadioButton ruleSetRadio;
         private JComboBox comboBoxCollections;
         private JTextField nameField;
 
         public void actionPerformed(ActionEvent e) {
 
             dialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(newDialogOwner), "Edit Action", true);
             dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
             dialog.setLayout(new GridBagLayout());
 
             GridBagConstraints constraints = new GridBagConstraints();
             constraints.fill = GridBagConstraints.HORIZONTAL;
             constraints.anchor = GridBagConstraints.CENTER;
             constraints.insets = new Insets(10, 10, 10, 10);
 
             JPanel radioPanel = new JPanel();
             radioPanel.setBorder(new TitledBorder("Type"));
             collectionRadio = new JRadioButton("Collection");
 
             radioPanel.add(collectionRadio);
             ruleSetRadio = new JRadioButton("Rule Set");
             if (root.getChildCount() == 0) {
                 ruleSetRadio.setEnabled(false);
             }
             radioPanel.add(ruleSetRadio);
             ButtonGroup group = new ButtonGroup();
             group.add(collectionRadio);
             group.add(ruleSetRadio);
             dialog.add(radioPanel, constraints);
 
             // setup combobox for collectin selection for rule set
             DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
             Enumeration children = root.children();
             while (children != null && children.hasMoreElements()) {
                 comboBoxModel.addElement((CollectionNode)children.nextElement());
             }
             comboBoxCollections = new JComboBox(comboBoxModel);
             comboBoxCollections.setRenderer(new NamedObjectListCellRenderer());
             final JPanel comboBoxWrapper = new JPanel(new GridLayout());
             comboBoxWrapper.setBorder(new TitledBorder("Add to Collection"));
             comboBoxWrapper.add(comboBoxCollections);
             comboBoxWrapper.setVisible(false);
             constraints.gridy = 1;
             dialog.add(comboBoxWrapper, constraints);
 
             // intelligent preset
             DefaultMutableTreeNode selectedNode =
                     (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
             if (selectedNode == null) {
                 collectionRadio.setSelected(true);
                 comboBoxWrapper.setVisible(false);
             } else if (selectedNode instanceof CollectionNode) {
                 comboBoxCollections.setSelectedItem(selectedNode);
                 ruleSetRadio.setSelected(true);
                 comboBoxWrapper.setVisible(true);
             } else if (selectedNode instanceof RuleSetNode) {
                 CollectionNode collectionNode = (CollectionNode)selectedNode.getParent();
                 comboBoxCollections.setSelectedItem(collectionNode);
                 ruleSetRadio.setSelected(true);
                 comboBoxWrapper.setVisible(true);
             } else {
                 assert(false);
             }
 
             collectionRadio.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     if (comboBoxWrapper.isVisible()) {
                         comboBoxWrapper.setVisible(false);
                         dialog.pack();
                     }
                 }
             });
 
             ruleSetRadio.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     if (false == comboBoxWrapper.isVisible()) {
                         comboBoxWrapper.setVisible(true);
                         dialog.pack();
                     }
                 }
             });
 
             // name field
             JPanel nameFieldWrapper = new JPanel(new GridLayout());
             nameFieldWrapper.setBorder(new TitledBorder("Name"));
             nameField = new JTextField();
             nameFieldWrapper.add(nameField);
             constraints.gridy = 2;
             dialog.add(nameFieldWrapper, constraints);
 
             // and the control panel
             JPanel controlPanel = new JPanel();
             final JButton btnOK = new JButton("OK");
             btnOK.addActionListener(new newDialogOKListener());
             controlPanel.add(btnOK);
             final JButton btnCancel = new JButton("Cancel");
             btnCancel.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     dialog.dispose();
                 }
              });
             controlPanel.add(btnCancel);
             constraints.gridy = 3;
             dialog.add(controlPanel, constraints);
 
             dialog.pack();
             dialog.setLocationRelativeTo(newDialogOwner);
             dialog.setVisible(true);
         }
 
         private class newDialogOKListener implements ActionListener {
 
             public void actionPerformed(ActionEvent e) {
                 dialog.dispose();
 
                 // creating a collection
                 if (collectionRadio.isSelected()) {
                     RuleSetCollection collection = new RuleSetCollection(
                             nameField.getText(),
                             "I am a sad collection, because I have no description but you can brighten up my day by clicking 'Edit' to describe me.");
 
                     // check if name is free
                     String desiredName = nameField.getText();
                     if (root.getChildCount() > 0) {
                         if (false == tree.isUniqueSiblingName(root, collection, desiredName)) {
                             return;
                         }
                     }
 
                     // add new collection to tree model
                     CollectionNode collectionNode = new CollectionNode(collection);
                     treeModel.insertNodeInto(collectionNode,
                             root, root.getChildCount());
 
                 } else { // creating a rule set
                 
                     RuleSet ruleSet = new RuleSet(nameField.getText(),
                             "These rules are made for ruling, that's just what they'll do. One of these days these rule are gonna rule all over you.",
                             new ArrayList<IRule>());
 
                     // check if name is free
                     String desiredName = nameField.getText();
                     DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)comboBoxCollections.getSelectedItem();
                     if (false == tree.isUniqueSiblingName(parentNode, ruleSet, desiredName)) {
                         return;
                     }
 
                     RuleSetNode ruleSetNode = new RuleSetNode(ruleSet);
                    // the following line is a bugfix for the bug where
                    // deletion of nodes that where inserted when their
                    // parent was not expanded causes the syntUI to eat null
                    // pointers. see below (around line 340) "WARNING"...
                    tree.expandPath(new TreePath(parentNode.getPath()));
                     treeModel.insertNodeInto(ruleSetNode, parentNode, parentNode.getChildCount());
                 }
             }
         }
     }
 
     private class BtnCopyListener implements ActionListener {
         
         public void actionPerformed(ActionEvent e) {
             
             DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();
             
             if (selectedNode == null) {
                 return;
             }
 
              // collection copy
             if (selectedNode instanceof CollectionNode) {
                 RuleSetCollection collection =
                         (RuleSetCollection)selectedNode.getUserObject();
                 RuleSetCollection newCollection = collection.getCopy();
 
                 // make sure the name of the copy is unique
                 boolean found = true;
                 for (int i = 1; found == true; i++) {
                     newCollection.setName(collection.getName() + " (" + i + ")");
                     found = false;
                     Enumeration siblingCollectionNodes = root.children();
                     while (siblingCollectionNodes.hasMoreElements()) {
                         CollectionNode node = (CollectionNode)siblingCollectionNodes.nextElement();
                         RuleSetCollection rsc = (RuleSetCollection)node.getUserObject();
                         if (rsc.getName().equals(newCollection.getName())) {
                             found = true;
                             break;
                         }
                     }
                 }
 
                 // add collection with copied rule sets to tree
                 CollectionNode newNode = new CollectionNode(newCollection);
                 DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                 model.insertNodeInto(newNode, root, root.getChildCount());
 
                 // collapse the old collection
                 tree.collapsePath(new TreePath(selectedNode.getPath()));
 
             } else { // rule set copy
 
                 CollectionNode collectionNode = (CollectionNode)selectedNode.getParent();
                 RuleSetCollection collection = (RuleSetCollection)
                         collectionNode.getUserObject();
                 RuleSet ruleSet = (RuleSet)selectedNode.getUserObject();
                 RuleSet newRuleSet = ruleSet.getCopy();
 
                 // make sure the name of the copy is unique
                 boolean found = true;
                 for (int i = 1; found == true; i++) {
                     newRuleSet.setName(ruleSet.getName() + " (" + i + ")");
                     found = false;
                     Enumeration siblingRuleSetNodes = collectionNode.children();
                     while (siblingRuleSetNodes.hasMoreElements()) {
                         RuleSetNode node = (RuleSetNode)
                                 siblingRuleSetNodes.nextElement();
                         RuleSet rs = (RuleSet)node.getUserObject();
                         if (rs.getName().equals(newRuleSet.getName())) {
                             found = true;
                             break;
                         }
                     }
                 }
 
                 // add new rule set to tree
                 RuleSetNode newNode = new RuleSetNode(newRuleSet);
                 DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                 model.insertNodeInto(newNode, collectionNode, collectionNode.getChildCount());
             }
         }
     }
 
     private class BtnDeleteListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();
 
             if (selectedNode == null) {
                 return;
             }
 
            // WARNING
            // bugged. reproduce: create a rule set without expanding
             // its collection node first, then delete it.
             //
             // as of JDK 1.6.0_15 this causes a NullPointerException
             // when using any synth laf
             // in javax.swing.plaf.synth.SynthTreeUI.paint(SynthTreeUI.java:297)
            treeModel.removeNodeFromParent(selectedNode);
 
             // this works, but will cause a structure change event to be posted
             // instead of removed
             //DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)selectedNode.getParent();
             //parentNode.remove(selectedNode);
             //treeModel.reload(parentNode);
         }
     }
 }
