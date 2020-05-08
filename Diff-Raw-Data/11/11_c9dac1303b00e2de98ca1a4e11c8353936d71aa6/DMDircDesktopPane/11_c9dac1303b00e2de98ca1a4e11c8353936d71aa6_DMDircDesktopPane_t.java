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
 
 package com.dmdirc.addons.ui_swing.components.desktopPane;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.BackgroundOption;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.SwingWindowListener;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.TreeScroller;
 import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewModel;
 import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewNode;
 import com.dmdirc.addons.ui_swing.framemanager.windowmenu.JInternalFrameComparator;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.interfaces.SelectionListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.util.ReturnableThread;
 import com.dmdirc.util.URLBuilder;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.imageio.ImageIO;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 import javax.swing.UIManager;
 import javax.swing.plaf.DesktopPaneUI;
 import javax.swing.tree.DefaultTreeSelectionModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 /**
  * DMDirc Extentions to JDesktopPane.
  */
 public class DMDircDesktopPane extends JDesktopPane implements
         SwingWindowListener, SelectionListener, PropertyChangeListener,
         ConfigChangeListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** The current number of pixels to displace new frames in the X
      * direction. */
     private int xOffset;
     /** The current number of pixels to displace new frames in the Y
      * direction. */
     private int yOffset;
     /** The number of pixels each new internal frame is offset by. */
     private static final int FRAME_OPENING_OFFSET = 30;
     /** Node storage, used for adding and deleting nodes correctly. */
     private final Map<Window, TreeViewNode> nodes;
     /** Data model. */
     private final TreeViewModel model;
     /** Selected model. */
     private final TreeSelectionModel selectionModel;
     /** Tree Scroller. */
     private final TreeScroller treeScroller;
     /** Selected window. */
     private Window selectedWindow;
     /** Maximised state. */
     private boolean maximised;
     /** Changing maximisation. */
     private final AtomicBoolean changing = new AtomicBoolean(false);
     /** Main Frame. */
     private final MainFrame mainFrame;
     /** Background image. */
     private Image backgroundImage;
     /** Background image option. */
     private BackgroundOption backgroundOption;
     /** Config domain. */
     private final String domain;
     /** Swing controller. */
     private final SwingController controller;
 
     /**
      * Initialises the DMDirc desktop pane.
      *
      * @param controller The controller that owns this desktop pane
      * @param mainFrame Main frame
      * @param domain Config domain
      */
     public DMDircDesktopPane(final SwingController controller,
             final MainFrame mainFrame, final String domain) {
         super();
 
         this.mainFrame = mainFrame;
         this.domain = domain;
         this.controller = controller;
         setBackground(UIManager.getColor("Panel.background"));
         setBorder(BorderFactory.createEtchedBorder());
 
         nodes = new HashMap<Window, TreeViewNode>();
         model = new TreeViewModel(new TreeViewNode(null, null));
         selectionModel = new DefaultTreeSelectionModel();
         treeScroller = new TreeScroller(model, selectionModel, false) {
 
             /** {@inheritDoc} */
             @Override
             protected void setPath(final TreePath path) {
                 super.setPath(path);
                 ((TreeViewNode) path.getLastPathComponent()).getWindow().
                         activateFrame();
             }
         };
 
         controller.getWindowFactory().addWindowListener(this);
 
         IdentityManager.getGlobalConfig().addChangeListener(domain,
                 "desktopbackground", this);
         IdentityManager.getGlobalConfig().addChangeListener(domain,
                 "desktopbackgroundoption", this);
 
         updateCachedSettings();
     }
 
     /** {@inheritDoc} */
     @Override
     public void paintComponent(final Graphics g) {
         if (backgroundImage == null) {
             super.paintComponent(g);
         } else {
             UIUtilities.paintBackground((Graphics2D) g, getBounds(),
                     backgroundImage, backgroundOption);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void setUI(final DesktopPaneUI ui) {
         if (ui instanceof ProxyDesktopPaneUI) {
             super.setUI(ui);
         } else {
             super.setUI(new ProxyDesktopPaneUI(ui, this));
         }
     }
 
     /**
      * Add a specified component at the specified index.
      *
      * @param comp Component to add
      * @param index Index for insertion
      */
     public void add(final JComponent comp, final int index) {
         addImpl(comp, null, index);
 
         // Make sure it'll fit with our offsets
         if (comp.getWidth() + xOffset > getWidth()) {
             xOffset = 0;
         }
         if (comp.getHeight() + yOffset > getHeight()) {
             yOffset = 0;
         }
 
         // Position the frame
         comp.setLocation(xOffset, yOffset);
 
         // Increase the offsets
         xOffset += FRAME_OPENING_OFFSET;
         yOffset += FRAME_OPENING_OFFSET;
     }
 
     /**
      * Arranges the windows in the specified number of rows and columns.
      *
      * @param windows Windows to arranges
      * @param columns Number of columns to use
      * @param rows Number of rows to use
      */
     private void tileWindows(final JInternalFrame[] windows, final int columns,
             final int rows) {
         ((TextFrame) windows[0]).restore();
         final int total = windows.length;
         final int windowWidth = getWidth() / columns;
         final int windowHeight = getHeight() / rows;
 
         int column = 0;
         int x = 0;
         int y = 0;
         for (int i = 0; i < total; i++) {
             windows[i].setBounds(x, y, windowWidth, windowHeight);
             column++;
             x += windowWidth;
             if (column >= columns) {
                 column = 0;
                 x = 0;
                 y += windowHeight;
             }
         }
     }
 
     /**
      * Arranges the windows to be equal size in a grid.
      */
     public void tileWindows() {
         if (getAllFrames().length == 0) {
             return;
         }
         final JInternalFrame[] windows = getAllFrames();
         Arrays.sort(windows, new JInternalFrameComparator());
         final int total = windows.length;
         final int columns = (int) Math.floor(Math.sqrt(total));
         final int rows = (int) (Math.ceil(((double) total) / columns));
         tileWindows(windows, columns, rows);
     }
 
     /**
      * Arranges the windows to be equal size in a horizontal row.
      */
     public void tileHorizontally() {
         if (getAllFrames().length == 0) {
             return;
         }
         final JInternalFrame[] windows = getAllFrames();
         Arrays.sort(windows, new JInternalFrameComparator());
         tileWindows(windows, 1, windows.length);
     }
 
     /**
      * Arranges the windows to be equal size in a vertical row.
      */
     public void tileVertically() {
         if (getAllFrames().length == 0) {
             return;
         }
         final JInternalFrame[] windows = getAllFrames();
         Arrays.sort(windows, new JInternalFrameComparator());
         tileWindows(windows, windows.length, 1);
     }
 
     /**
      * Returns the select window.
      *
      * @return Selected window, or null.
      */
     public Window getSelectedWindow() {
         return UIUtilities.invokeAndWait(new ReturnableThread<Window>() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setObject(selectedWindow);
             }
         });
     }
 
     @Override
     public void windowAdded(final Window parent, final Window window) {
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 synchronized (nodes) {
                     addWindow(parent == null ? model.getRootNode() : nodes.get(
                             parent), window);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowDeleted(final Window parent, final Window window) {
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 remove((JInternalFrame) window);
                 if (nodes == null || nodes.get(window) == null) {
                     return;
                 }
                 final TreeViewNode node = nodes.get(window);
                 if (node.getLevel() == 0) {
                     Logger.appError(ErrorLevel.MEDIUM,
                             "delServer triggered for root node"
                             + node.toString(),
                             new IllegalArgumentException());
                 } else {
                     model.removeNodeFromParent(nodes.get(window));
                 }
                 nodes.remove(window);
                 window.getContainer().removeSelectionListener(
                         DMDircDesktopPane.this);
                 ((TextFrame) window).removePropertyChangeListener(
                         DMDircDesktopPane.this);
                 if (getAllFrames().length == 0) {
                     mainFrame.setTitle(null);
                 }
             }
         });
     }
 
     /**
      * Adds a window to the frame container.
      *
      * @param parent Parent node
      * @param window Window to add
      */
     public void addWindow(final TreeViewNode parent, final Window window) {
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 add((JInternalFrame) window, getAllFrames().length - 1);
                 final TreeViewNode node = new TreeViewNode(null, window.
                         getContainer());
                 synchronized (nodes) {
                     nodes.put(window, node);
                 }
                 node.setUserObject(window);
                 model.insertNodeInto(node, parent);
                 window.getContainer().addSelectionListener(
                         DMDircDesktopPane.this);
                 ((TextFrame) window).addPropertyChangeListener(
                         DMDircDesktopPane.this);
             }
         });
     }
 
     /** Scrolls up. */
     public void scrollUp() {
         treeScroller.changeFocus(true);
     }
 
     /** Scrolls down. */
     public void scrollDown() {
         treeScroller.changeFocus(false);
     }
 
     /** {@inheritDoc} */
     @Override
     public void selectionChanged(final FrameContainer<?> window) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 selectedWindow = controller.getWindowFactory().getSwingWindow(
                         window);
                 final TreeNode[] path = model.getPathToRoot(nodes.get(
                         selectedWindow));
                 if (path != null && path.length > 0) {
                     selectionModel.setSelectionPath(new TreePath(path));
                 }
                 if (selectedWindow instanceof InputTextFrame) {
                     ((InputTextFrame) selectedWindow).requestInputFieldFocus();
                 }
                 mainFrame.setTitle(selectedWindow.getContainer().getTitle());
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void propertyChange(final PropertyChangeEvent evt) {
         if ("title".equals(evt.getPropertyName())) {
             handleTitleEvent((Window) evt.getSource(),
                     ((Window) evt.getSource()).getContainer().getTitle());
         } else if ("maximum".equals(evt.getPropertyName())) {
             handleMaximiseEvent((Boolean) evt.getNewValue(),
                     ((Window) evt.getSource()).getContainer().getTitle());
         }
     }
 
     private void handleTitleEvent(final Window window, final String title) {
         if (maximised && window == selectedWindow) {
             mainFrame.setTitle(title);
         } else if (!maximised) {
             mainFrame.setTitle(null);
         }
     }
 
     private void handleMaximiseEvent(final boolean isMaximised,
             final String title) {
         if (changing.getAndSet(true)) {
             return;
         }
 
         maximised = isMaximised;
         final Stack<JInternalFrame> stack = new Stack<JInternalFrame>();
         stack.addAll(Arrays.asList(getAllFrames()));
 
         while (!stack.empty()) {
             final JInternalFrame frame = stack.pop();
             if (isMaximised && !frame.isMaximum()) {
                 ((Window) frame).maximise();
             } else if (!isMaximised && frame.isMaximum()) {
                 ((Window) frame).restore();
             }
         }
         if (selectedWindow != null) {
             selectedWindow.activateFrame();
         }
         mainFrame.setTitle(isMaximised ? title : null);
         changing.set(false);
     }
 
     private void updateCachedSettings() {
         final String backgroundPath = IdentityManager.getGlobalConfig().
                 getOption(domain, "desktopbackground");
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 try {
                     final URL url = URLBuilder.buildURL(backgroundPath);
                     if (url != null) {
                         backgroundImage = ImageIO.read(url);
                     }
                 } catch (IOException ex) {
                     backgroundImage = null;
                 }
             }
         });
         backgroundOption = BackgroundOption.valueOf(IdentityManager
                 .getGlobalConfig().getOption(domain,
                 "desktopbackgroundoption"));
         repaint();
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         updateCachedSettings();
     }
 }
