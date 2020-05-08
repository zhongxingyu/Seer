 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.gui.rulesetmanager;
 
 import evopaint.Configuration;
 import evopaint.pixel.rulebased.RuleSet;
 import evopaint.pixel.rulebased.RuleSetCollection;
 import evopaint.pixel.rulebased.interfaces.IRule;
 import evopaint.util.RuleSetNode;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.border.LineBorder;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author tam
  */
 public class JRuleSetManager extends JPanel implements TreeSelectionListener {
 
     private Configuration configuration;
     private RuleSet selectedRuleSet;
     private JRuleSetTree jRuleSetTree;
     private Container contentPane;
     private JRuleSetBrowser jRuleSetBrowser;
     private JDescriptionPanel jDescriptionPanel;
     private JRuleList jRuleList;
     private JRuleEditor jRuleEditor;
     private JSplitPane splitPaneVertical;
     JButton btnUse;
 
     public RuleSet getSelectedRuleSet() {
        return selectedRuleSet.getCopy(); // a copy, or we will change painted rule sets
     }
 
     public Configuration getConfiguration() {
         return configuration;
     }
 
     public void setConfiguration(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public JRuleSetManager(Configuration configuration, ActionListener OKListener, ActionListener CancelListener) {
         this.configuration = configuration;
         this.contentPane = this;
 
         setLayout(new CardLayout());
 
         DefaultTreeModel treeModel = configuration.fileHandler.readCollections();
         jRuleSetTree = new JRuleSetTree(treeModel, new TreeDoubleClickListener());
         jRuleSetTree.addTreeSelectionListener(this);
 
         // FIRST CARD
         jRuleSetBrowser = new JRuleSetBrowser(configuration, jRuleSetTree);
         jDescriptionPanel = new JDescriptionPanel(configuration, jRuleSetTree);
 
         // [ browser | description ]
         JSplitPane upperSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                            jRuleSetBrowser, jDescriptionPanel);
         upperSplitPane.setDividerLocation(250);
         upperSplitPane.setContinuousLayout(true);
         upperSplitPane.setResizeWeight(0.1); // most space goes to description
 
         jRuleList = new JRuleList(configuration, jRuleSetTree, new EditRuleBtnListener(), new DoubleClickOnRuleListener());
 
         // [ browser | description ]
         // [       rule list       ]
         splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                            upperSplitPane, jRuleList);
         splitPaneVertical.setContinuousLayout(true);
         splitPaneVertical.setResizeWeight(0.2); // most new space goes to rule list
         splitPaneVertical.getBottomComponent().setVisible(false); // hide rule list at first
 
         // control panel
         JPanel controlPanel = new JPanel();
         btnUse = new JButton("Use");
         btnUse.addActionListener(OKListener);
         btnUse.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (jRuleList.isDirty()) {
                     jRuleList.clean();
                 }
             }
         });
         btnUse.setEnabled(false);
         controlPanel.add(btnUse);
         JButton btnCancel = new JButton("Cancel");
         btnCancel.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (jRuleList.isDirty()) {
                     jRuleList.clean();
                 }
             }
         });
         btnCancel.addActionListener(CancelListener);
         controlPanel.add(btnCancel);
 
         // create a main panel to contain everything, this will be added
         // to the card layout of this panel
         JPanel mainPanel = new JPanel();
         mainPanel.setLayout(new BorderLayout(20, 20));
         mainPanel.setBorder(new LineBorder(getBackground(), splitPaneVertical.getDividerSize()));
         mainPanel.add(splitPaneVertical, BorderLayout.CENTER);
         mainPanel.add(controlPanel, BorderLayout.SOUTH);
         add(mainPanel, "manager");
 
         // SECOND CARD
         // editor
         this.jRuleEditor = new JRuleEditor(configuration, new RuleEditorOKListener(), new RuleEditorCancelListener());
         this.jRuleEditor.setVisible(false);
         add(jRuleEditor, "editor");
     }
 
     public void valueChanged(TreeSelectionEvent e) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
         Object userObject = node.getUserObject();
 
         if (userObject == null) {
             selectedRuleSet = null;
             btnUse.setEnabled(false);
             return;
         }
 
         if (userObject instanceof RuleSetCollection) {
             splitPaneVertical.getBottomComponent().setVisible(false);
             selectedRuleSet = null;
             btnUse.setEnabled(false);
             return;
         }
 
         if (userObject instanceof RuleSet) {
             splitPaneVertical.getBottomComponent().setVisible(true);
             splitPaneVertical.setDividerLocation(260);
             selectedRuleSet = (RuleSet)userObject;
             btnUse.setEnabled(true);
             return;
         }
 
         assert(false);
     }
 
     private class RuleEditorOKListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             jRuleList.replaceSelectedRule(jRuleEditor.getRule());
             ((CardLayout)contentPane.getLayout()).show(contentPane, "manager");
         }
 
     }
 
     private class RuleEditorCancelListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             ((CardLayout)contentPane.getLayout()).show(contentPane, "manager");
         }
     }
     
     private class EditRuleBtnListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             IRule selectedRule = jRuleList.getSelectedRule();
 
             if (selectedRule == null) {
                 return;
             }
             
             jRuleEditor.setRule(selectedRule);
             ((CardLayout)contentPane.getLayout()).show(contentPane, "editor");
         }
     };
 
     private class DoubleClickOnRuleListener extends MouseAdapter {
         @Override
         public void mouseClicked(MouseEvent e) {
             if (e.getClickCount() == 2) {
                 int index = jRuleList.locationToIndex(e.getPoint());
 
                 if (index == -1) {
                     return;
                 }
                 
                 IRule selectedRule = jRuleList.getSelectedRule();
 
                 assert (selectedRule != null);
 
                 jRuleEditor.setRule(selectedRule);
                 ((CardLayout)contentPane.getLayout()).show(contentPane, "editor");
              }
         }
     }
 
     private class TreeDoubleClickListener extends MouseAdapter {
 
         @Override
         public void mousePressed(MouseEvent e) {
             TreePath path = jRuleSetTree.getPathForLocation(e.getX(), e.getY());
             if (e.getClickCount() == 2) {
 
                 DefaultMutableTreeNode node =
                         (DefaultMutableTreeNode)path.getLastPathComponent();
 
                 if (node instanceof RuleSetNode) {
                     RuleSet ruleSet = (RuleSet)node.getUserObject();
                     selectedRuleSet = ruleSet;
                     btnUse.doClick();
                 }
             }
         }
     }
 }
