 /*
  * Copyright (c) 2006-2012 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.framemanager.tree;
 
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.CloseFrameContainerAction;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTree;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import net.miginfocom.layout.PlatformDefaults;
 
 /**
  * Specialised JTree for the frame manager.
  */
 public class Tree extends JTree implements MouseMotionListener,
         ConfigChangeListener, MouseListener, ActionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Tree frame manager. */
     private final TreeFrameManager manager;
     /** UI Controller. */
     private final SwingController controller;
     /** Config manager. */
     private final ConfigManager config;
     /** Drag selection enabled? */
     private boolean dragSelect;
     /** Drag button 1? */
     private boolean dragButton;
     /** Show handles. */
     private boolean showHandles;
 
     /**
      * Specialised JTree for frame manager.
      *
      * @param manager Frame manager
      * @param model tree model.
      * @param controller Swing controller
      */
     public Tree(final TreeFrameManager manager, final TreeModel model,
             final SwingController controller) {
         super(model);
 
         this.manager = manager;
         this.controller = controller;
         this.config = controller.getGlobalConfig();
 
         putClientProperty("JTree.lineStyle", "Angled");
         getInputMap().setParent(null);
         getInputMap(JComponent.WHEN_FOCUSED).clear();
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();
         getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
         getSelectionModel().setSelectionMode(
                 TreeSelectionModel.SINGLE_TREE_SELECTION);
         setRootVisible(false);
        setRowHeight(0);
         setOpaque(true);
         setBorder(BorderFactory.createEmptyBorder(
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue()));
         setFocusable(false);
 
         dragSelect = config.getOptionBool("treeview", "dragSelection");
         showHandles = config.getOptionBool(controller.getDomain(), "showtreeexpands");
         config.addChangeListener(controller.getDomain(), "showtreeexpands", this);
         config.addChangeListener("treeview", this);
 
         setShowsRootHandles(showHandles);
         putClientProperty("showHandles", showHandles);
 
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public void scrollRectToVisible(final Rectangle aRect) {
         final Rectangle rect = new Rectangle(0, aRect.y,
                 aRect.width, aRect.height);
         super.scrollRectToVisible(rect);
     }
 
     /**
      * Set path.
      *
      * @param path Path
      */
     public void setTreePath(final TreePath path) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 setSelectionPath(path);
             }
         });
     }
 
     /**
      * Returns the node for the specified location, returning null if rollover
      * is disabled or there is no node at the specified location.
      *
      * @param x x coordiantes
      * @param y y coordiantes
      *
      * @return node or null
      */
     public TreeViewNode getNodeForLocation(final int x,
             final int y) {
         TreeViewNode node = null;
         final TreePath selectedPath = getPathForLocation(x, y);
         if (selectedPath != null) {
             node = (TreeViewNode) selectedPath.getLastPathComponent();
         }
         return node;
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         if ("dragSelection".equals(key)) {
             dragSelect = config.getOptionBool("treeview", "dragSelection");
         } else if ("showtreeexpands".equals(key)) {
             config.getOptionBool(controller.getDomain(), "showtreeexpands");
             setShowsRootHandles(showHandles);
             putClientProperty("showHandles", showHandles);
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseDragged(final MouseEvent e) {
         if (dragSelect && dragButton) {
             final TreeViewNode node = getNodeForLocation(e.getX(), e.getY());
             if (node != null) {
                 controller.requestWindowFocus(controller.getWindowFactory()
                         .getSwingWindow(((TreeViewNode) new TreePath(node.getPath()).
                         getLastPathComponent()).getWindow()));
             }
         }
         manager.checkRollover(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseMoved(final MouseEvent e) {
         manager.checkRollover(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseClicked(final MouseEvent e) {
         processMouseEvents(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mousePressed(final MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
             dragButton = true;
             final TreePath selectedPath = getPathForLocation(e.getX(),
                     e.getY());
             if (selectedPath != null) {
                 controller.requestWindowFocus(controller.getWindowFactory()
                         .getSwingWindow(((TreeViewNode) selectedPath
                         .getLastPathComponent()).getWindow()));
             }
         }
         processMouseEvents(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseReleased(final MouseEvent e) {
         dragButton = false;
         processMouseEvents(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseExited(final MouseEvent e) {
         manager.checkRollover(null);
     }
 
     /**
      * Processes every mouse button event to check for a popup trigger.
      * @param e mouse event
      */
     public void processMouseEvents(final MouseEvent e) {
         final TreePath localPath = getPathForLocation(e.getX(), e.getY());
         if (localPath != null && e.isPopupTrigger()) {
             final TextFrame frame = controller.getWindowFactory()
                     .getSwingWindow(((TreeViewNode) localPath.getLastPathComponent())
                     .getWindow());
 
             if (frame == null) {
                 return;
             }
 
             final JPopupMenu popupMenu = frame.getPopupMenu(null,
                     new Object[][] {new Object[]{""}});
             frame.addCustomPopupItems(popupMenu);
             if (popupMenu.getComponentCount() > 0) {
                 popupMenu.addSeparator();
             }
             final TreeViewNodeMenuItem popoutMenuItem;
             if (frame.getPopoutFrame() == null) {
                 popoutMenuItem = new TreeViewNodeMenuItem("Pop Out", "popout",
                         (TreeViewNode) localPath.getLastPathComponent());
             } else {
                 popoutMenuItem = new TreeViewNodeMenuItem("Pop In", "popin",
                         (TreeViewNode) localPath.getLastPathComponent());
             }
             popupMenu.add(popoutMenuItem);
             popupMenu.addSeparator();
             popoutMenuItem.addActionListener(this);
 
             final TreeViewNodeMenuItem moveUp =
                     new TreeViewNodeMenuItem("Move Up", "Up",
                     (TreeViewNode) localPath.getLastPathComponent());
             final TreeViewNodeMenuItem moveDown =
                     new TreeViewNodeMenuItem("Move Down", "Down",
                     (TreeViewNode) localPath.getLastPathComponent());
 
             moveUp.addActionListener(this);
             moveDown.addActionListener(this);
 
             popupMenu.add(moveUp);
             popupMenu.add(moveDown);
             popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.
                     getContainer())));
             popupMenu.show(this, e.getX(), e.getY());
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         final TreeViewNode node = ((TreeViewNodeMenuItem) e.getSource()).
                 getTreeNode();
         int index = getModel().getIndexOfChild(node.getParent(), node);
         if ("Up".equals(e.getActionCommand())) {
             if (index == 0) {
                 index = node.getSiblingCount() - 1;
             } else {
                 index--;
             }
         } else if ("Down".equals(e.getActionCommand())) {
             if (index == (node.getSiblingCount() - 1)) {
                 index = 0;
             } else {
                 index++;
             }
         } else if ("popout".equals(e.getActionCommand())) {
             controller.getWindowFactory().getSwingWindow(node.getWindow())
                     .setPopout(true);
         } else if ("popin".equals(e.getActionCommand())) {
             controller.getWindowFactory().getSwingWindow(node.getWindow())
                     .setPopout(false);
         }
         final TreeViewNode parentNode = (TreeViewNode) node.getParent();
         final TreePath nodePath = new TreePath(node.getPath());
         final boolean isExpanded = isExpanded(nodePath);
         ((TreeViewModel) getModel()).removeNodeFromParent(node);
         ((TreeViewModel) getModel()).insertNodeInto(node, parentNode, index);
         setExpandedState(nodePath, isExpanded);
     }
 }
