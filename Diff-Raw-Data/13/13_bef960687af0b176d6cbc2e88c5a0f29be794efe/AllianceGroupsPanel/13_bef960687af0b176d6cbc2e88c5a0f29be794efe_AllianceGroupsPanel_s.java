 /*
  * Copyright (C) 2008
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Derived from Travian world" logo. If the display of the logo is not
  * reasonably feasible for technical reasons, the Appropriate Legal Notices must
  * display the words "Derived from Travian world".
  */
 package ste.travian.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.ActionMap;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import org.jdesktop.swingx.JXDialog;
 import org.jdesktop.swingx.action.BoundAction;
 
 public class AllianceGroupsPanel extends JPanel {
 
     public static final String REST_OF_THE_WORLD_GROUP = "Rest of the world";
 
     private AllianceGroupsTree tree;
     private AllianceList restOfTheWorldList;
     private Toolkit toolkit = Toolkit.getDefaultToolkit();
 
     public AllianceGroupsPanel() {
         super(new BorderLayout());
 
         tree = new AllianceGroupsTree();
        add(new JScrollPane(tree), BorderLayout.LINE_START);
 
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         JButton button = new JButton("+");
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 addGroup();
             }
         });
         panel.add(button);
 
         button = new JButton("-");
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 deleteGroup();
             }
         });
         panel.add(button);
         panel.setPreferredSize(new Dimension(40, 100));
         add(panel, BorderLayout.CENTER);
 
         restOfTheWorldList = new AllianceList();
        JScrollPane pane = new JScrollPane(restOfTheWorldList);
         pane.setPreferredSize(new Dimension(200, 300));
         add(pane, BorderLayout.LINE_END);
 
         ActionMap actions = getActionMap();
         BoundAction action = new BoundAction(
             "OK",
             JXDialog.EXECUTE_ACTION_COMMAND
         );
         action.registerCallback(this, "doExecute");
         actions.put(JXDialog.EXECUTE_ACTION_COMMAND, action);
      }
 
     /**
      * Delete the selected group if not the "rest of the world" group.
      * All children will be added back to the alliance list
      */
     public void deleteGroup() {
         DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 
         TreePath currentSelection = tree.getSelectionPath();
         if (currentSelection != null) {
             DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
             
             if (currentNode.getParent().equals(model.getRoot())) {
                 AllianceDnDInfo info = null;
                 if (!REST_OF_THE_WORLD_GROUP.equals(currentNode.getUserObject())) {
                     //
                     // Let's add back the children to the alliance list
                     //
                     Enumeration nodes = currentNode.children();
                     while (nodes.hasMoreElements()) {
                         info = (AllianceDnDInfo)((DefaultMutableTreeNode)nodes.nextElement()).getUserObject();
                         restOfTheWorldList.addAlliance(info);
                     }
                     model.removeNodeFromParent(currentNode);
                     return;
                 }
             }
 
         }
 
         // Either there was no selection, or the root was selected.
         toolkit.beep();
     }
 
     /**
      * Adds a new group of alliances with a default name
      */
     public void addGroup() {
         DefaultMutableTreeNode node = null;
         DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("New group");
         newNode.setAllowsChildren(true);
 
         DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 
         DefaultMutableTreeNode rootNode =
             (DefaultMutableTreeNode)model.getRoot();
 
         int nChildren = rootNode.getChildCount(), i = 0;
         for (; i<nChildren; ++i) {
             node = (DefaultMutableTreeNode)rootNode.getChildAt(i);
             if (String.valueOf(node.getUserObject()).compareTo(String.valueOf(newNode.getUserObject())) > 0) {
                 break;
             }
         }
         model.insertNodeInto(newNode, rootNode, i);
         tree.scrollPathToVisible(new TreePath(newNode.getPath()));
 
     }
 
     public AllianceGroupsTree getTree() {
         return tree;
     }
 
     public JList getRestOfTheWorldList() {
         return restOfTheWorldList;
     }
 
     public void doExecute() {
         //
         // TODO: remove test code duplication
         //
         AllianceGroupsController c = 
             ((AllianceGroupsDialog)getRootPane().getParent()).getController();
 
         Map<String,ArrayList<String>> groups = new HashMap<String,ArrayList<String>>();
         ArrayList<String> alliances = null;
 
         DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 
         DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
         Enumeration g = root.children();
         while (g.hasMoreElements()) {
             DefaultMutableTreeNode group = (DefaultMutableTreeNode)g.nextElement();
 
             alliances = new ArrayList<String>();
             groups.put(group.toString(), alliances);
             Enumeration a = group.children();
             while (a.hasMoreElements()) {
                 alliances.add(a.nextElement().toString());
             }
         }
        
         c.updateAllianceGroups(groups);
         
         ((JXDialog)getRootPane().getParent()).dispose();
     }
 }
