 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Sirius.navigator.ui;
 
 import Sirius.navigator.Navigator;
 import Sirius.navigator.docking.CustomView;
 import Sirius.navigator.plugin.interfaces.LayoutManager;
 import Sirius.navigator.ui.widget.FloatingFrame;
 import Sirius.navigator.ui.widget.FloatingFrameConfigurator;
 
 import net.infonode.docking.DockingWindow;
 import net.infonode.docking.DockingWindowListener;
 import net.infonode.docking.OperationAbortedException;
 import net.infonode.docking.RootWindow;
 import net.infonode.docking.SplitWindow;
 import net.infonode.docking.TabWindow;
 import net.infonode.docking.View;
 import net.infonode.docking.properties.RootWindowProperties;
 import net.infonode.docking.theme.DockingWindowsTheme;
 import net.infonode.docking.theme.ShapedGradientDockingTheme;
 import net.infonode.docking.util.DeveloperUtil;
 import net.infonode.docking.util.DockingUtil;
 import net.infonode.docking.util.PropertiesUtil;
 import net.infonode.docking.util.StringViewMap;
 import net.infonode.gui.componentpainter.AlphaGradientComponentPainter;
 import net.infonode.util.Direction;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * DOCUMENT ME!
  *
  * @author   spuhl
  * @version  $Revision$, $Date$
  */
 public class LayoutedContainer implements GUIContainer, LayoutManager {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(MutableContainer.class);
     public static final String DEFAULT_LAYOUT = Navigator.NAVIGATOR_HOME + "navigator.layout"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final Hashtable components = new Hashtable(); // private final JSplitPane rootSplitPane;
     // private final JSplitPane leftSplitPane;
 
     private final RootWindow rootWindow;
     private StringViewMap viewMap = new StringViewMap();
     private final Vector<View> p1Pane;
     private final Vector<View> p2Pane;
     private final Vector<View> p3Pane;
     private final ConstrainsChangeListener constrainsChangeListener;
     private final FloatingFrameListener floatingFrameListener;
     private boolean continuousLayout = false;
     private boolean oneTouchExpandable = false;
     private boolean proportionalResize = false;
     private boolean obeyMinimumSize = false;
     private final MutableToolBar toolBar;
     private final MutableMenuBar menuBar;
     private final DockingWindowListener dockingWindowListener = new DockingWindowListener() {
 
             @Override
             public void windowAdded(final DockingWindow arg0, final DockingWindow arg1) {
 //            logger.fatal("windowadded" + arg0);
 //            logger.fatal("addedwindow" + arg1);
 //
 //            if (arg0 instanceof TabWindow && arg1 instanceof View && ((TabWindow) arg0).getChildWindowCount() > 1) {
 //                int count = ((TabWindow) arg0).getChildWindowCount();
 //                for (int i = 0; i < count; i++) {
 //                    DockingWindow child = ((TabWindow) arg0).getChildWindow(i);
 //                    if (child instanceof CustomView) {
 //                        ((View) child).getViewProperties().getViewTitleBarProperties().getNormalProperties().setTitle("");
 //                        ((View) child).getViewProperties().getViewTitleBarProperties().getNormalProperties().setIcon(null);
 //                    }
 //                }
 
 //                DockingWindow first = ((TabWindow) arg0).getChildWindow(0);
 //                if (first instanceof CustomView) {
 //                    logger.fatal("Title/Icon disabled");
 //                    //((CustomView)first).getViewProperties().getViewTitleBarProperties().setVisible(false);
 //                    ((View) first).getViewProperties().getViewTitleBarProperties().getNormalProperties().setTitle("");
 //                    ((View) first).getViewProperties().getViewTitleBarProperties().getNormalProperties().setIcon(null);
 //                }
 
                 // }
             }
 
             @Override
             public void windowRemoved(final DockingWindow arg0, final DockingWindow arg1) {
 //            if (arg1 instanceof CustomView) {
 //                ((View) arg1).getViewProperties().getViewTitleBarProperties().getNormalProperties().setTitle(((CustomView) arg1).getViewName());
 //                ((View) arg1).getViewProperties().getViewTitleBarProperties().getNormalProperties().setIcon(((CustomView) arg1).getViewIcon());
 //                if (arg0 instanceof TabWindow && ((TabWindow) arg0).getChildWindowCount() == 1) {
 //                    DockingWindow first = ((TabWindow) arg0).getChildWindow(0);
 //                    if (first instanceof CustomView) {
 //                        logger.fatal("Title/Icon enabled");
 //                        //((CustomView)first).getViewProperties().getViewTitleBarProperties().setVisible(false);
 //                        ((View) first).getViewProperties().getViewTitleBarProperties().getNormalProperties().setTitle(((CustomView) first).getViewName());
 //                        ((View) first).getViewProperties().getViewTitleBarProperties().getNormalProperties().setIcon(((CustomView) first).getViewIcon());
 //                    }
 //                }
 //
 //            }
                 // throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void windowShown(final DockingWindow arg0) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Docking window shown");                           // NOI18N
                 }
                 try {
                     if (arg0 instanceof CustomView) {
                         menuBar.setMoveableMenuesEnabled(((CustomView)arg0).getId(), true);
                         toolBar.setMoveableToolBarEnabled(((CustomView)arg0).getId(), true);
                     }
                 } catch (Exception ex) {
                     logger.error("Error while activating the MenuBar/Toolbar", ex); // NOI18N
                 }
             }
 
             @Override
             public void windowHidden(final DockingWindow arg0) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Docking window hidden");                            // NOI18N
                 }
                 try {
                     if (arg0 instanceof CustomView) {
                         menuBar.setMoveableMenuesEnabled(((CustomView)arg0).getId(), false);
                         toolBar.setMoveableToolBarEnabled(((CustomView)arg0).getId(), false);
                     }
                 } catch (Exception ex) {
                     logger.error("Error while deactivating the MenuBar/Toolbar", ex); // NOI18N
                 }
             }
 
             @Override
             public void viewFocusChanged(final View arg0, final View arg1) {
             }
 
             @Override
             public void windowClosing(final DockingWindow arg0) throws OperationAbortedException {
             }
 
             @Override
             public void windowClosed(final DockingWindow arg0) {
             }
 
             @Override
             public void windowUndocking(final DockingWindow arg0) throws OperationAbortedException {
             }
 
             @Override
             public void windowUndocked(final DockingWindow arg0) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Docking window shown");                           // NOI18N
                 }
                 try {
                     menuBar.setMoveableMenuesEnabled(((CustomView)arg0).getId(), true);
                     toolBar.setMoveableToolBarEnabled(((CustomView)arg0).getId(), true);
                 } catch (Exception ex) {
                     logger.error("Error while activating the MenuBar/Toolbar", ex); // NOI18N
                 }
             }
 
             @Override
             public void windowDocking(final DockingWindow arg0) throws OperationAbortedException {
             }
 
             @Override
             public void windowDocked(final DockingWindow arg0) {
             }
 
             @Override
             public void windowMinimizing(final DockingWindow arg0) throws OperationAbortedException {
             }
 
             @Override
             public void windowMinimized(final DockingWindow arg0) {
             }
 
             @Override
             public void windowMaximizing(final DockingWindow arg0) throws OperationAbortedException {
             }
 
             @Override
             public void windowMaximized(final DockingWindow arg0) {
             }
 
             @Override
             public void windowRestoring(final DockingWindow arg0) throws OperationAbortedException {
             }
 
             @Override
             public void windowRestored(final DockingWindow arg0) {
             }
         };
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new LayoutedContainer object.
      *
      * @param  toolBar  DOCUMENT ME!
      * @param  menuBar  DOCUMENT ME!
      */
     public LayoutedContainer(final MutableToolBar toolBar, final MutableMenuBar menuBar) {
         this(toolBar, menuBar, false);
     }
 
     /**
      * Creates a new LayoutedContainer object.
      *
      * @param  toolBar         DOCUMENT ME!
      * @param  menuBar         DOCUMENT ME!
      * @param  advancedLayout  DOCUMENT ME!
      */
     public LayoutedContainer(final MutableToolBar toolBar, final MutableMenuBar menuBar, final boolean advancedLayout) {
         this(toolBar, menuBar, advancedLayout, advancedLayout, advancedLayout);
     }
 
     /**
      * Creates a new LayoutedContainer object.
      *
      * @param  toolBar             DOCUMENT ME!
      * @param  menuBar             DOCUMENT ME!
      * @param  continuousLayout    DOCUMENT ME!
      * @param  oneTouchExpandable  DOCUMENT ME!
      * @param  proportionalResize  DOCUMENT ME!
      */
     public LayoutedContainer(final MutableToolBar toolBar,
             final MutableMenuBar menuBar,
             final boolean continuousLayout,
             final boolean oneTouchExpandable,
             final boolean proportionalResize) {
         if (logger.isDebugEnabled()) {
             logger.debug("creating LayoutedContainer instance"); // NOI18N
         }
 
         this.toolBar = toolBar;
         this.menuBar = menuBar;
         this.continuousLayout = continuousLayout;
         this.oneTouchExpandable = oneTouchExpandable;
         this.proportionalResize = proportionalResize;
 
 //        p1Pane = new JTabbedPane (JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
 //        p2Pane = new JTabbedPane (JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
 //        p3Pane = new JTabbedPane (JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
 
 //        p1Pane = new TabWindow();
 //        p2Pane = new TabWindow();
 //        p3Pane = new TabWindow();
 
         p1Pane = new Vector<View>();
         p2Pane = new Vector<View>();
         p3Pane = new Vector<View>();
 
         // this.leftSplitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT, this.continuousLayout, p1Pane, p2Pane);
         // this.rootSplitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, this.continuousLayout, this.leftSplitPane,
         // this.p3Pane);
 
         // this.leftSplitPane.setOneTouchExpandable(oneTouchExpandable);
         // this.rootSplitPane.setOneTouchExpandable(oneTouchExpandable);
 
 // if(this.proportionalResize)
 // {
 // //this.leftSplitPane.setResizeWeight(0.75);
 // //this.rootSplitPane.setResizeWeight(0.25);
 //
 // this.leftSplitPane.setResizeWeight(0.60);
 // this.rootSplitPane.setResizeWeight(0.30);
 // }
 
 // if(this.obeyMinimumSize)
 // {
 // p1Pane.setMinimumSize(new Dimension(240,200));
 // }
 
         if (Sirius.navigator.resource.PropertyManager.getManager().isAdvancedLayout()) { // p1Pane.setBorder(new
                                                                                          // javax.swing.border.EmptyBorder(1,1,1,1));
 //            p2Pane.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
 //            p3Pane.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
 //            this.leftSplitPane.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
 //            this.rootSplitPane.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
         }
 
         // this.leftSplitPane.setDividerLocation(600);
         // this.rootSplitPane.setDividerLocation(250);
 
         constrainsChangeListener = new ConstrainsChangeListener();
         floatingFrameListener = new FloatingFrameListener();
 
         rootWindow = DockingUtil.createRootWindow(viewMap, true);
         doConfigKeystrokes();
 
         // Cismap
         // rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
         //
         // DockingWindowsTheme theme = new ShapedGradientDockingTheme();
         // rootWindow.getRootWindowProperties().addSuperObject( theme.getRootWindowProperties());
         //
         // RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
         //
         // rootWindow.getRootWindowProperties().addSuperObject( titleBarStyleProperties);
         //
         // rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(true);
         //
         // AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(java.awt.SystemColor.inactiveCaptionText,
         // java.awt.SystemColor.activeCaptionText, java.awt.SystemColor.activeCaptionText,
         // java.awt.SystemColor.inactiveCaptionText);
         // rootWindow.getRootWindowProperties().getDragRectangleShapedPanelProperties().setComponentPainter(x);
 
         final DockingWindowsTheme theme = new ShapedGradientDockingTheme();
         rootWindow.getRootWindowProperties().addSuperObject(
             theme.getRootWindowProperties());
 
 //        RootWindowProperties titleBarStyleProperties =
 //                PropertiesUtil.createTitleBarStyleRootWindowProperties();
 
 //        rootWindow.getRootWindowProperties().addSuperObject(
 //                titleBarStyleProperties);
 
         rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(true);
         // AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(java.awt.SystemColor.inactiveCaptionText,
         // java.awt.SystemColor.activeCaptionText, java.awt.SystemColor.activeCaptionText,
         // java.awt.SystemColor.inactiveCaptionText); AlphaGradientComponentPainter x = new
         // AlphaGradientComponentPainter(java.awt.SystemColor.inactiveCaptionText, Color.blue,
         // java.awt.SystemColor.activeCaptionText, Color.black);
         final AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(
                 java.awt.SystemColor.inactiveCaptionText,
                 java.awt.SystemColor.activeCaptionText,
                 java.awt.SystemColor.activeCaptionText,
                 java.awt.SystemColor.inactiveCaptionText);
         rootWindow.getRootWindowProperties().getDragRectangleShapedPanelProperties().setComponentPainter(x);
 
         //
         // rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().getNormalProperties().getShapedPanelProperties().setComponentPainter(new
         // GradientComponentPainter(new Color(124,160,221),new Color(236,233,216),new Color(124,160,221),new
         // Color(236,233,216))); LagisBroker.getInstance().setTitleBarComponentpainter(LagisBroker.DEFAULT_MODE_COLOR);
         // rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setOrientation(Direction.UP);
         // r
         // ootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().getNormalProperties().setIconVisible(false);
         // r
         // ootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().getNormalProperties().setTitleVisible(false);
         // rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().setVisible(false);
         // rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties().getShapedPanelProperties().setDirection(Direction.DOWN);
         // r
         // ootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().setTabAreaOrientation(Direction.UP);
         rootWindow.getRootWindowProperties()
                 .getTabWindowProperties()
                 .getTabbedPanelProperties()
                 .setTabAreaOrientation(Direction.UP);
         // rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getDefaultProperties().getTabAreaComponentsProperties().
         rootWindow.getRootWindowProperties()
                 .getTabWindowProperties()
                 .getTabbedPanelProperties()
                 .setPaintTabAreaShadow(true);
         rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().setShadowSize(10);
         rootWindow.getRootWindowProperties()
                 .getTabWindowProperties()
                 .getTabbedPanelProperties()
                 .setShadowStrength(0.8f);
         rootWindow.getRootWindowProperties().getTabWindowProperties().getMinimizeButtonProperties().setVisible(false);
         // rootWindow.getRootWindowProperties().getTabWindowProperties()
         // rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getContentPanelProperties().getComponentProperties().setBorder(new
         // DropShadowBorder(Color.BLACK,5,5,0.5f,12,true,true,false,true));
         // rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().getContentPanelProperties().getComponentProperties().setBorder();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  rootSplitPaneDividerLocation  DOCUMENT ME!
      * @param  leftSplitPaneDividerLocation  DOCUMENT ME!
      */
     public void setDividerLocations(final double rootSplitPaneDividerLocation,
             final double leftSplitPaneDividerLocation) {
 //        this.rootSplitPane.setDividerLocation(rootSplitPaneDividerLocation);
 //        this.leftSplitPane.setDividerLocation(leftSplitPaneDividerLocation);
     }
 
     @Override
     public synchronized void add(final MutableConstraints constraints) {
         if (logger.isInfoEnabled()) {
             logger.info("adding component '" + constraints.getName() + "' to mutable container at position '"
                         + constraints.getPosition() + "'");      // NOI18N
         }
         if (logger.isDebugEnabled()) {
             logger.debug(constraints.toString());
         }
         if (!components.containsKey(constraints.getId())) {
             components.put(constraints.getId(), constraints);
             if (!this.rootWindow.isDisplayable() || SwingUtilities.isEventDispatchThread()) {
                 doAdd(constraints);
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("add(): synchronizing method"); // NOI18N
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             doAdd(constraints);
                         }
                     });
             }
 
             if (constraints.isMutable()) {
                 constraints.addPropertyChangeListener(constrainsChangeListener);
             }
         } else {
             logger.error("a component with the same id '" + constraints.getId() + "' is already in this container"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  constraints  DOCUMENT ME!
      */
     private void doAdd(final MutableConstraints constraints) {
         final Vector<View> tabbedPane = this.getViewsAtPosition(constraints.getPosition());
         if ((constraints.getPreferredIndex() != -1) && (tabbedPane.size() > constraints.getPreferredIndex())) {
             if (logger.isDebugEnabled()) {
                 logger.debug("inserting component at index '" + constraints.getPreferredIndex() + "'"); // NOI18N
             }
             if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
                 tabbedPane.add(constraints.getPreferredIndex(), constraints.getView());
                 menuBar.addViewMenuItem(constraints.getView().getMenuItem());
                 viewMap.addView(constraints.getName(), constraints.getView());
                 doLayoutInfoNode();
                 // TODO
                 // tabbedPane.setSelectedIndex(constraints.getPreferredIndex());
                 // ((FloatingFrame) constraints.getContainer()).getCon;
                 this.addFloatingFrame(constraints);
             } else {
                 tabbedPane.add(constraints.getPreferredIndex(), constraints.getView());
                 menuBar.addViewMenuItem(constraints.getView().getMenuItem());
                 viewMap.addView(constraints.getName(), constraints.getView());
                 doLayoutInfoNode();
                 // TODO tabbedPane.insertTab(constraints.getName(), constraints.getIcon(), constraints.getContainer(),
                 // constraints.getToolTip(), constraints.getPreferredIndex());
                 // tabbedPane.setSelectedIndex(constraints.getPreferredIndex());
             }
         } else {
             if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
                 tabbedPane.add(constraints.getView());
                 menuBar.addViewMenuItem(constraints.getView().getMenuItem());
                 viewMap.addView(constraints.getName(), constraints.getView());
                 doLayoutInfoNode();
                 // tabbedPane.addTab(constraints.getName(), constraints.getIcon(),
                 // ((FloatingFrame)constraints.getContainer()).getFloatingPanel(), constraints.getToolTip());
                 this.addFloatingFrame(constraints);
             } else {
                 tabbedPane.add(constraints.getView());
                 menuBar.addViewMenuItem(constraints.getView().getMenuItem());
                 viewMap.addView(constraints.getName(), constraints.getView());
                 doLayoutInfoNode();
                 // tabbedPane.addTab(constraints.getName(), constraints.getIcon(), constraints.getContainer(),
                 // constraints.getToolTip());
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  constraints  DOCUMENT ME!
      */
     private void addFloatingFrame(final MutableConstraints constraints) {
         if (logger.isDebugEnabled()) {
             logger.debug("adding FloatingFrame"); // NOI18N
         }
         final FloatingFrameConfigurator configurator = constraints.getFloatingFrameConfigurator();
         ((FloatingFrame)constraints.getContainer()).setTileBarVisible(false);
         // configurator.setIcon(null);
         // configurator.setButtons(null);
 
         // logger.info("FloatingFrame constraints '" + constraints + "'");
         // logger.info("FloatingFrame constraints id: '" + constraints.getId() + "'");
         // logger.info("FloatingFrame configurator '" + configurator + "'");
         // logger.info("FloatingFrame configurator id: '" + configurator.getId() + "'");
         if (!configurator.getId().equals(constraints.getId())) {
             logger.warn("FloatingFrame constraints id: '" + constraints.getId()
                         + "' != FloatingFrame configurator id: '" + configurator.getId() + "'"); // NOI18N
         }
 
         if (configurator.isSwapMenuBar() || configurator.isSwapToolBar()) {
             if (logger.isDebugEnabled()) {
                 logger.debug("enabling Floating Listener"); // NOI18N
             }
             ((FloatingFrame)constraints.getContainer()).addPropertyChangeListener(
                 FloatingFrame.FLOATING,
                 floatingFrameListener);
             constraints.getView().addListener(dockingWindowListener);
         }
 
         if (configurator.isSwapMenuBar()) {
             if (logger.isInfoEnabled()) {
                 logger.info("adding FloatingFrameMenuBar '" + configurator.getId() + "' to MutableMenuBar"); // NOI18N
             }
             menuBar.addMoveableMenues(configurator.getId(), configurator.getMenues());
         }
 
         if (configurator.isSwapToolBar()) {
             if (logger.isInfoEnabled()) {
                 logger.info("adding FloatingFrameToolBar '" + configurator.getId() + "' to MutableToolBar"); // NOI18N
             }
             toolBar.addMoveableToolBar(((FloatingFrame)constraints.getContainer()).getToolBar());
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  constraints  DOCUMENT ME!
      */
     private void removeFloatingFrame(final MutableConstraints constraints) {
         if (logger.isDebugEnabled()) {
             logger.debug("removing FloatingFrame"); // NOI18N
         }
         final FloatingFrameConfigurator configurator = constraints.getFloatingFrameConfigurator();
 
         if (configurator.isSwapMenuBar() || configurator.isSwapToolBar()) {
             ((FloatingFrame)constraints.getContainer()).removePropertyChangeListener(floatingFrameListener);
         }
 
         if (configurator.isSwapMenuBar()) {
             if (logger.isInfoEnabled()) {
                 logger.info("removing FloatingFrameMenuBar '" + configurator.getId() + "' from MutableMenuBar"); // NOI18N
             }
             menuBar.removeMoveableMenues(configurator.getId());
         }
 
         if (configurator.isSwapToolBar()) {
             if (logger.isInfoEnabled()) {
                 logger.info("removing FloatingFrameToolBar '" + configurator.getId() + "' from MutableToolBar"); // NOI18N
             }
             toolBar.removeMoveableToolBar(configurator.getId());
         }
     }
 
     @Override
     public synchronized void remove(final String id) {
         if (logger.isInfoEnabled()) {
             logger.info("removing component '" + id + "'"); // NOI18N
         }
         if (components.containsKey(id)) {
             final MutableConstraints constraints = (MutableConstraints)components.remove(id);
             if (constraints.isMutable()) {
                 constraints.removePropertyChangeListener(constrainsChangeListener);
             }
 
             if (SwingUtilities.isEventDispatchThread()) {
                 doRemove(constraints);
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("remove(): synchronizing method"); // NOI18N
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             doRemove(constraints);
                         }
                     });
             }
         } else {
             logger.error("component '" + id + "' not found in this container"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  constraints  DOCUMENT ME!
      */
     private void doRemove(final MutableConstraints constraints) {
         if (logger.isDebugEnabled()) {
             logger.debug("removing component '" + constraints.getName() + "' at position '" + constraints.getPosition()
                         + "'"); // NOI18N
         }
         final Vector<View> tabbedPane = this.getViewsAtPosition(constraints.getPosition());
 
         if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
             tabbedPane.remove(((FloatingFrame)constraints.getContainer()).getFloatingPanel());
             this.removeFloatingFrame(constraints);
         } else {
             tabbedPane.remove(constraints.getContainer());
         }
     }
 
     @Override
     public synchronized void select(final String id) {
         if (logger.isDebugEnabled()) {
             logger.debug("selecting component '" + id + "'");       // NOI18N
         }
         if (components.containsKey(id)) {
             final MutableConstraints constraints = (MutableConstraints)components.get(id);
             if (SwingUtilities.isEventDispatchThread()) {
                 doSelect(constraints);
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("select(): synchronizing method"); // NOI18N
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             doSelect(constraints);
                         }
                     });
             }
         } else {
             logger.error("component '" + id + "' not found in this container"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  constraints  DOCUMENT ME!
      */
     private void doSelect(final MutableConstraints constraints) {
        if (!constraints.getView().isClosable()) {
            constraints.getView().restore();
        }
         final Vector<View> tabbedPane = this.getViewsAtPosition(constraints.getPosition());
 
 //        if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
 //            //tabbedPane.setSelectedTab(tabbedPane.getChildWindowIndex(constraints.getView()));
 //            int index = -1;
 //            if((index = tabbedPane.indexOf(constraints.getView())) != -1){
 //                tabbedPane.get(index).restoreFocus();
 //            };
 //        //tabbedPane.setSelectedComponent(((FloatingFrame)constraints.getContainer()).getFloatingPanel());
 //        } else {
 //            tabbedPane.setSelectedTab(tabbedPane.getChildWindowIndex(constraints.getView()));
 //        //tabbedPane.setSelectedComponent(constraints.getContainer());
 //        }
 
         int index = -1;
         if ((index = tabbedPane.indexOf(constraints.getView())) != -1) {
             tabbedPane.get(index).restoreFocus();
         }
         ;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   position  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Vector<View> getViewsAtPosition(final String position) {
         if (position.equals(MutableConstraints.P1)) {
             return p1Pane;
         } else if (position.equals(MutableConstraints.P2)) {
             return p2Pane;
         } else if (position.equals(MutableConstraints.P3)) {
             return p3Pane;
         } else {
             logger.warn("unknown position '" + position + "', using default '" + MutableConstraints.P3 + "'"); // NOI18N
             return p3Pane;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getContainer() {
         return rootWindow;
     }
 
     /**
      * DOCUMENT ME!
      */
     public synchronized void doLayoutInfoNode() {
 // default
 //        rootWindow.setWindow(new SplitWindow(false, 0.38277513f,
 //                p1Pane,
 //                new SplitWindow(false, 0.4300518f,
 //                p2Pane,
 //                p3Pane)));
 
         try {
             if (logger.isDebugEnabled()) {
                 // logger.fatal("p1Pane: "+p1Pane);
                 // logger.fatal("p1 Childcount"+getTabWindowAt().getChildWindowCount());
                 logger.debug("remove all listener"); // NOI18N
             }
             TabWindow p1 = null;
             if (p1Pane.size() != 0) {
                 p1 = new TabWindow();
                 p1.addListener(dockingWindowListener);
                 for (final View currentView : p1Pane) {
                     p1.addTab(currentView);
                 }
             }
             TabWindow p2 = null;
             if (p2Pane.size() != 0) {
                 p2 = new TabWindow();
                 p2.addListener(dockingWindowListener);
                 for (final View currentView : p2Pane) {
                     p2.addTab(currentView);
                 }
             }
             TabWindow p3 = null;
             if (p3Pane.size() != 0) {
                 p3 = new TabWindow();
                 p3.addListener(dockingWindowListener);
                 // DockingWindow[] p3Array = p3Pane.toArray(new DockingWindow[1]);
                 for (final View currentView : p3Pane) {
                     p3.addTab(currentView);
                 }
             }
 
             if ((p1 != null) && (p2 != null) && (p3 == null)) {
                 rootWindow.setWindow(
                     new SplitWindow(false, 0.6032864f,
                         p1,
                         p2));
             } else if ((p1 != null) && (p2 != null) && (p2 != null)) {
                 rootWindow.setWindow(new SplitWindow(true, 0.2505929f,
                         new SplitWindow(false, 0.6032864f,
                             p1,
                             p2),
                         p3));
             }
 
             if ((p1 != null) && (p1.getChildWindow(0) != null)) {
                 p1.getChildWindow(0).restoreFocus();
             }
             if ((p2 != null) && (p2.getChildWindow(0) != null)) {
                 p2.getChildWindow(0).restoreFocus();
             }
             if ((p3 != null) && (p3.getChildWindow(0) != null)) {
                 p3.getChildWindow(0).restoreFocus();
             }
         } catch (Exception ex) {
             logger.warn("Error while layouting the Navigator", ex); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void doConfigKeystrokes() {
         final KeyStroke showLayoutKeyStroke = KeyStroke.getKeyStroke('D', InputEvent.CTRL_MASK);
         final Action showLayoutAction = new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     java.awt.EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 DeveloperUtil.createWindowLayoutFrame(
                                         org.openide.util.NbBundle.getMessage(
                                             LayoutedContainer.class,
                                             "LayoutedContainer.doConfigKeystrokes.rootWindow.title"), // NOI18N
                                         rootWindow)
                                         .setVisible(true);
                             }
                         });
                 }
             };
         rootWindow.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(showLayoutKeyStroke, "SHOW_LAYOUT"); // NOI18N
         rootWindow.getActionMap().put("SHOW_LAYOUT", showLayoutAction); // NOI18N
         // rootWindow.registerKeyboardAction(showLayoutAction,showLayoutKeyStroke,JComponent.WHEN_FOCUSED);
     }
 
     @Override
     public void loadLayout(final Component parent) {
         final JFileChooser fc = new JFileChooser(Navigator.NAVIGATOR_HOME);
         fc.setFileHidingEnabled(false);
         fc.setFileFilter(new FileFilter() {
 
                 @Override
                 public boolean accept(final File f) {
                     return f.getName().toLowerCase().endsWith(".layout"); // NOI18N
                 }
 
                 @Override
                 public String getDescription() {
                     return "Layout"; // NOI18N
                 }
             });
         fc.setMultiSelectionEnabled(false);
         final int state = fc.showOpenDialog(parent);
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".layout")) { // NOI18N
                 loadLayout(name, false, parent);
             } else {
                 JOptionPane.showMessageDialog(
                     parent,
                     org.openide.util.NbBundle.getMessage(
                         LayoutedContainer.class,
                         "LayoutedContainer.loadLayout(Component).JOptionPane.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         LayoutedContainer.class,
                         "LayoutedContainer.loadLayout(Component).JOptionPane.title"), // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  file    DOCUMENT ME!
      * @param  isInit  DOCUMENT ME!
      * @param  parent  DOCUMENT ME!
      */
     public void loadLayout(final String file, final boolean isInit, final Component parent) {
         if (logger.isDebugEnabled()) {
             logger.debug("Load Layout.. from " + file); // NOI18N
         }
         final File layoutFile = new File(file);
 
         if (layoutFile.exists()) {
             if (logger.isDebugEnabled()) {
                 logger.debug("Layout File exists");                                                  // NOI18N
             }
             try {
                 final FileInputStream layoutInput = new FileInputStream(layoutFile);
                 final ObjectInputStream in = new ObjectInputStream(layoutInput);
                 rootWindow.read(in);
                 in.close();
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
                 if (isInit) {
                     final int count = viewMap.getViewCount();
                     for (int i = 0; i < count; i++) {
                         final View current = viewMap.getViewAtIndex(i);
                         if (current.isUndocked()) {
                             current.dock();
                         }
                     }
                 }
                 if (logger.isDebugEnabled()) {
                     logger.debug("Loading Layout successfull");                                      // NOI18N
                 }
             } catch (IOException ex) {
                 logger.error("Layout File IO Exception --> loading default Layout", ex);             // NOI18N
                 if (isInit) {
                     JOptionPane.showMessageDialog(
                         parent,
                         org.openide.util.NbBundle.getMessage(
                             LayoutedContainer.class,
                             "LayoutedContainer.loadLayout(String,boolean,Component).message.reset"), // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             LayoutedContainer.class,
                             "LayoutedContainer.loadLayout(String,boolean,Component).title"),         // NOI18N
                         JOptionPane.INFORMATION_MESSAGE);
                     doLayoutInfoNode();
                 } else {
                     JOptionPane.showMessageDialog(
                         parent,
                         org.openide.util.NbBundle.getMessage(
                             LayoutedContainer.class,
                             "LayoutedContainer.loadLayout(String,boolean,Component).message"),       // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             LayoutedContainer.class,
                             "LayoutedContainer.loadLayout(String,boolean,Component).title"),         // NOI18N
                         JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         } else {
             if (isInit) {
                 logger.warn("Datei exitstiert nicht --> default layout (init)");                     // NOI18N
                 SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             // UGLY WINNING --> Gefixed durch IDW Version 1.5
                             // setupDefaultLayout();
                             // DeveloperUtil.createWindowLayoutFrame("nach setup1",rootWindow).setVisible(true);
                             doLayoutInfoNode();
                             // DeveloperUtil.createWindowLayoutFrame("nach setup2",rootWindow).setVisible(true);
                         }
                     });
             } else {
                 logger.warn("Datei exitstiert nicht)");                                             // NOI18N
                 JOptionPane.showMessageDialog(
                     parent,
                     org.openide.util.NbBundle.getMessage(
                         LayoutedContainer.class,
                         "LayoutedContainer.loadLayout(String,boolean,Component).message.notFound"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         LayoutedContainer.class,
                         "LayoutedContainer.loadLayout(String,boolean,Component).title"),            // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     @Override
     public void resetLayout() {
         doLayoutInfoNode();
     }
 
     @Override
     public void saveCurrentLayout(final Component parent) {
         final JFileChooser fc = new JFileChooser(Navigator.NAVIGATOR_HOME);
         fc.setFileFilter(new FileFilter() {
 
                 @Override
                 public boolean accept(final File f) {
                     return f.getName().toLowerCase().endsWith(".layout"); // NOI18N
                 }
 
                 @Override
                 public String getDescription() {
                     return "Layout"; // NOI18N
                 }
             });
         fc.setMultiSelectionEnabled(false);
         final int state = fc.showSaveDialog(parent);
         if (logger.isDebugEnabled()) {
             logger.debug("state:" + state); // NOI18N
         }
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             if (logger.isDebugEnabled()) {
                 logger.debug("file:" + file); // NOI18N
             }
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".layout")) { // NOI18N
                 saveLayout(name, parent);
             } else {
                 saveLayout(name + ".layout", parent); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  file    DOCUMENT ME!
      * @param  parent  DOCUMENT ME!
      */
     public void saveLayout(final String file, final Component parent) {
         if (logger.isDebugEnabled()) {
             logger.debug("Saving Layout.. to " + file);                                                                  // NOI18N
         }
         final File layoutFile = new File(file);
         try {
             if (!layoutFile.exists()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Saving Layout.. File does not exit");                                                  // NOI18N
                 }
                 layoutFile.createNewFile();
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Saving Layout.. File does exit");                                                      // NOI18N
                 }
             }
             final FileOutputStream layoutOutput = new FileOutputStream(layoutFile);
             final ObjectOutputStream out = new ObjectOutputStream(layoutOutput);
             rootWindow.write(out);
             out.flush();
             out.close();
             if (logger.isDebugEnabled()) {
                 logger.debug("Saving Layout.. to " + file + " successfull");                                             // NOI18N
             }
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(
                 parent,
                 org.openide.util.NbBundle.getMessage(LayoutedContainer.class, "LayoutedContainer.saveLayout().message"), // NOI18N
                 org.openide.util.NbBundle.getMessage(LayoutedContainer.class, "LayoutedContainer.saveLayout().title"),   // NOI18N
                 JOptionPane.INFORMATION_MESSAGE);
             logger.error("A failure occured during writing the layout file", ex);                                        // NOI18N
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class FloatingFrameListener implements PropertyChangeListener {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * This method gets called when a bound property is changed.
          *
          * @param  evt  A PropertyChangeEvent object describing the event source and the property that has changed.
          */
         @Override
         public void propertyChange(final PropertyChangeEvent evt) {
             final FloatingFrame floatingFrame = (FloatingFrame)evt.getSource();
 
             if (floatingFrame.getConfigurator().isSwapMenuBar()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("setting floating frame meneus visible: '" + !floatingFrame.isFloating() + "'"); // NOI18N
                 }
                 menuBar.setMoveableMenuesVisible(floatingFrame.getConfigurator().getId(), !floatingFrame.isFloating());
             }
 
             if (floatingFrame.getConfigurator().isSwapToolBar()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("setting floating frame toolbar visible: '" + !floatingFrame.isFloating() + "'"); // NOI18N
                 }
                 toolBar.setMoveableToolBarVisible(floatingFrame.getConfigurator().getId(), !floatingFrame.isFloating());
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ConstrainsChangeListener implements PropertyChangeListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void propertyChange(final PropertyChangeEvent e) {
             if (e.getSource() instanceof MutableConstraints) {
                 final MutableConstraints constraints = (MutableConstraints)e.getSource();
                 if (logger.isDebugEnabled()) {
                     logger.debug("setting new value of property '" + e.getPropertyName() + "' of component '"
                                 + constraints.getId() + "'"); // TabWindow tabbedPane =
                                                               // getTabWindowAt(constraints.getPosition());   //NOI18N
                                                               // int index =
                                                               // tabbedPane.getChildWindowIndex(constraints.getView());
                 }
                 final View changedView = constraints.getView();
                 if (e.getPropertyName().equals("name")) {     // NOI18N
                     // tabbedPane.seTitleAt(index, constraints.getName());
                     changedView.getViewProperties().setTitle(constraints.getName());
                 } else if (e.getPropertyName().equals("tooltip")) { // NOI18N
                     if (logger.isDebugEnabled()) {
 //                    tabbedPane.setToolTipTextAt(index, constraints.getToolTip());
 //                    changedView.get
                         // TODO
                         logger.debug("Tooltip konnte nicht geändert werden, da nicht implementiert");                // NOI18N
                     }
                 } else if (e.getPropertyName().equals("icon")) {                                                     // NOI18N
                     changedView.getViewProperties().setIcon(constraints.getIcon());
                 } else if (e.getPropertyName().equals("position") || e.getPropertyName().equals("preferredIndex")) { // NOI18N
                     // add() f\u00FChrt automatisch zu einem remove()
                     // doRemove(constraints);
 
                     // Extrawurst bei FLoatingFrame: Men\u00FCs und Toolbars entfernen
                     if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
                         removeFloatingFrame(constraints);
                     }
 
                     doAdd(constraints);
                     doSelect(constraints);
                 } else {
                     logger.warn("unsupported property change of '" + e.getPropertyName() + "'"); // NOI18N
                 }
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("unexpected property change event '" + e.getPropertyName() + "'"); // NOI18N
                 }
             }
         }
     }
 }
