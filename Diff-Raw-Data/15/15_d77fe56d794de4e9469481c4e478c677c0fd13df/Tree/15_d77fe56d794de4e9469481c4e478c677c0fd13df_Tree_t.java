 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.serverlist;
 
 import com.dmdirc.addons.ui_swing.components.TreeScroller;
 import com.dmdirc.addons.ui_swing.components.renderers.ServerGroupTreeRenderer;
 import com.dmdirc.serverlists.ServerGroupItem;
 
 import java.awt.Rectangle;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Enumeration;
 import javax.swing.JButton;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Tree of server groups and items.
  */
 public class Tree extends JPanel implements TreeSelectionListener,
         ServerListListener, ActionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Tree. */
     private final JTree items;
     /** Tree model. */
     private final DefaultTreeModel treeModel;
     /** Server list model. */
     private final ServerListModel model;
     /** Add group. */
     private final JButton addGroupButton;
     /** Add Item. */
     private final JButton addItemButton;
     /** Parent window. */
     private final Window parentWindow;
 
     /**
      * Instantiates a new tree of server groups.
      *
      * @param model Model backing this tree
      */
     public Tree(final ServerListModel model, final Window parentWindow) {
         super();
 
         this.model = model;
         this.parentWindow = parentWindow;
         addGroupButton = new JButton("Add group");
         addItemButton = new JButton("Add item");
         model.addServerListListener(this);
         treeModel = model.getTreeModel();
         items = new JTree(treeModel) {
 
             /**
              * A version number for this class. It should be changed whenever
              * the class structure is changed (or anything else that would
              * prevent serialized objects being unserialized with the new
              * class).
              */
             private static final long serialVersionUID = 2;
 
             /** {@inheritDoc} */
             @Override
             public void scrollRectToVisible(final Rectangle aRect) {
                 final Rectangle rect = new Rectangle(0, aRect.y,
                         aRect.width, aRect.height);
                 super.scrollRectToVisible(rect);
             }
         };
 
         items.putClientProperty("JTree.lineStyle", "Angled");
         items.getInputMap().setParent(null);
         items.getInputMap(JComponent.WHEN_FOCUSED).clear();
         items.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();
         items.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
         items.getSelectionModel().setSelectionMode(
                 TreeSelectionModel.SINGLE_TREE_SELECTION);
         items.setRootVisible(true);
        items.setShowsRootHandles(true);
         items.setOpaque(true);
         items.setFocusable(false);
         new TreeScroller(items);
         items.setCellRenderer(new ServerGroupTreeRenderer());
         items.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
         items.setSelectionRow(0);
         valueChanged(null);
         addListeners();
 
         setLayout(new MigLayout("fill, ins 0, wrap 1"));
 
         add(new JScrollPane(items), "grow, push");
         add(addGroupButton, "growx, pushx");
         add(addItemButton, "growx, pushx");
     }
 
     /**
      * Adds required listeners.
      */
     private void addListeners() {
         items.addTreeSelectionListener(this);
         addGroupButton.addActionListener(this);
         addItemButton.addActionListener(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public final void valueChanged(final TreeSelectionEvent e) {
         if (items.getSelectionPath() == null) {
             return;
         }
         final DefaultMutableTreeNode itemNode = (DefaultMutableTreeNode) items.
                 getSelectionPath().getLastPathComponent();
         if (itemNode.getUserObject() instanceof ServerGroupItem) {
             model.setSelectedItem((ServerGroupItem) itemNode.getUserObject());
         } else {
             model.setSelectedItem(null);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupChanged(final ServerGroupItem item) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void dialogClosed(final boolean save) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupAdded(final ServerGroupItem parent,
             final ServerGroupItem group) {
         final DefaultMutableTreeNode parentNode = getNodeForGroup(parent);
         treeModel.insertNodeInto(new DefaultMutableTreeNode(group), parentNode,
                 parentNode.getChildCount());
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupRemoved(final ServerGroupItem parent,
             final ServerGroupItem group) {
         //TODO when I let people remove groups...
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (e.getSource() == addGroupButton) {
             new AddGroupInputDialog(parentWindow, items, model).display();
         } else {
             new AddEntryInputDialog(parentWindow, items, model).display();
         }
     }
 
     /**
      * Find the node for a specified server group item, will return null if the
      * group is not found.
      *
      * @param item Item to search for in the tree
      *
      * @return Node for group, or null if not found
      */
     private DefaultMutableTreeNode getNodeForGroup(final ServerGroupItem item) {
         DefaultMutableTreeNode node = null;
         final Enumeration enumeration = ((DefaultMutableTreeNode) items
                 .getModel().getRoot()).breadthFirstEnumeration();
         while (enumeration.hasMoreElements()) {
             final DefaultMutableTreeNode current =
                     (DefaultMutableTreeNode) enumeration.nextElement();
             if (item == current.getUserObject()) {
                 node = current;
                 break;
             }
         }
         return node;
     }
 }
