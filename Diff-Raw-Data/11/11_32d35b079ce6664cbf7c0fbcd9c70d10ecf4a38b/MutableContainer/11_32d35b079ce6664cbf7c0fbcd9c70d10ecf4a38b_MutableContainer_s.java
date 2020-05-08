 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 /*******************************************************************************
  *
  * Copyright (c)        :       EIG (Environmental Informatics Group)
  * http://www.htw-saarland.de/eig
  * Prof. Dr. Reiner Guettler
  * Prof. Dr. Ralf Denzer
  *
  * HTWdS
  * Hochschule fuer Technik und Wirtschaft des Saarlandes
  * Goebenstr. 40
  * 66117 Saarbruecken
  * Germany
  *
  * Programmers          :       Pascal
  *
  * Project                      :       WuNDA 2
  * Version                      :       2.0
  * Purpose                      :
  * Created                      :       01.11.1999
  * History                      :       01.05.2000, new Version + UI
  *
  *******************************************************************************/
 
 import Sirius.navigator.tools.MetaToolkit;
 import Sirius.navigator.ui.widget.*;
 
 import org.apache.log4j.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.beans.*;
 
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 //import Sirius.navigator.PlugIn.*;
 
 /**
  * Ein flexibler Container in dem bis zu 7 Komponenten (JComponent) unterschiedlich angeordnet werden koennen.<br>
  * Der NavigatorLayoutManager ist durch 2 JSplitPanes in 3 Bereiche aufgeteilt: LEFT_TOP, LEFT_BOTTOM und RIGHT. In
  * LEFT_BOTTOM ist Platz fuer nur eine Komponente in LEFT_TOP und RIGHT passen jeweils 3 Komponeten, die durch Tabs
  * (JTabbedPane) voneinander abgetrennt sind. Die Komponeten koennen optional noch in eine JScrollPane oder ein
  * zentriert in ein JPanel eingebettet werden.<br>
  * Ausserdem kann sich die Groesse der 3 Bereiche proportional der Groesse des NavigatorLayoutManager anpassen. Dafuer
  * musste das BasicSplitPaneUI angepasst werden.
  *
  * @author   Pascal Dihe
  * @version  2.0
  */
 public final class MutableContainer implements GUIContainer {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(MutableContainer.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final Hashtable components = new Hashtable();
 
     private final JSplitPane rootSplitPane;
     private final JSplitPane leftSplitPane;
 
     private final JTabbedPane p1Pane;
     private final JTabbedPane p2Pane;
     private final JTabbedPane p3Pane;
 
     private final ConstrainsChangeListener constrainsChangeListener;
     private final FloatingFrameListener floatingFrameListener;
 
     private boolean continuousLayout = false;
     private boolean oneTouchExpandable = false;
     private boolean proportionalResize = false;
     private boolean obeyMinimumSize = false;
 
     private final MutableToolBar toolBar;
     private final MutableMenuBar menuBar;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MutableContainer object.
      *
      * @param  toolBar  DOCUMENT ME!
      * @param  menuBar  DOCUMENT ME!
      */
     public MutableContainer(final MutableToolBar toolBar, final MutableMenuBar menuBar) {
         this(toolBar, menuBar, false);
     }
 
     /**
      * Creates a new MutableContainer object.
      *
      * @param  toolBar         DOCUMENT ME!
      * @param  menuBar         DOCUMENT ME!
      * @param  advancedLayout  DOCUMENT ME!
      */
     public MutableContainer(final MutableToolBar toolBar, final MutableMenuBar menuBar, final boolean advancedLayout) {
         this(toolBar, menuBar, advancedLayout, advancedLayout, advancedLayout, advancedLayout);
     }
 
     /**
      * Creates a new MutableContainer object.
      *
      * @param  toolBar             DOCUMENT ME!
      * @param  menuBar             DOCUMENT ME!
      * @param  continuousLayout    DOCUMENT ME!
      * @param  oneTouchExpandable  DOCUMENT ME!
      * @param  proportionalResize  DOCUMENT ME!
      * @param  obeyMinimumSize     DOCUMENT ME!
      */
     public MutableContainer(final MutableToolBar toolBar,
             final MutableMenuBar menuBar,
             final boolean continuousLayout,
             final boolean oneTouchExpandable,
             final boolean proportionalResize,
             final boolean obeyMinimumSize) {
         if (logger.isDebugEnabled()) {
             logger.debug("creating MutableContainer instance"); // NOI18N
         }
 
         this.toolBar = toolBar;
         this.menuBar = menuBar;
         this.continuousLayout = continuousLayout;
         this.oneTouchExpandable = oneTouchExpandable;
         this.proportionalResize = proportionalResize;
         this.obeyMinimumSize = obeyMinimumSize;
 
         p1Pane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
         p2Pane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
         p3Pane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
 
         this.leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.continuousLayout, p1Pane, p2Pane);
         this.rootSplitPane = new JSplitPane(
                 JSplitPane.HORIZONTAL_SPLIT,
                 this.continuousLayout,
                 this.leftSplitPane,
                 this.p3Pane);
 
         this.leftSplitPane.setOneTouchExpandable(oneTouchExpandable);
         this.rootSplitPane.setOneTouchExpandable(oneTouchExpandable);
 
         if (this.proportionalResize) {
             // this.leftSplitPane.setResizeWeight(0.75);
             // this.rootSplitPane.setResizeWeight(0.25);
 
             this.leftSplitPane.setResizeWeight(0.60);
             this.rootSplitPane.setResizeWeight(0.30);
         }
 
         if (this.obeyMinimumSize) {
             p1Pane.setMinimumSize(new Dimension(240, 200));
         }
 
         if (Sirius.navigator.resource.PropertyManager.getManager().isAdvancedLayout()) {
             p1Pane.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
             p2Pane.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
             p3Pane.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
             this.leftSplitPane.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
             this.rootSplitPane.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
         }
 
         // this.leftSplitPane.setDividerLocation(600);
         // this.rootSplitPane.setDividerLocation(250);
 
         constrainsChangeListener = new ConstrainsChangeListener();
         floatingFrameListener = new FloatingFrameListener();
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
         this.rootSplitPane.setDividerLocation(rootSplitPaneDividerLocation);
         this.leftSplitPane.setDividerLocation(leftSplitPaneDividerLocation);
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
             if (!this.rootSplitPane.isDisplayable() || SwingUtilities.isEventDispatchThread()) {
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
         final JTabbedPane tabbedPane = this.getTabbedPaneAt(constraints.getPosition());
         if ((constraints.getPreferredIndex() != -1) && (tabbedPane.getTabCount() > constraints.getPreferredIndex())) {
             if (logger.isDebugEnabled()) {
                 logger.debug("inserting component at index '" + constraints.getPreferredIndex() + "'"); // NOI18N
             }
             if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
                 tabbedPane.insertTab(constraints.getName(),
                     constraints.getIcon(),
                     ((FloatingFrame)constraints.getContainer()).getFloatingPanel(),
                     constraints.getToolTip(),
                     constraints.getPreferredIndex());
                 tabbedPane.setSelectedIndex(constraints.getPreferredIndex());
                 this.addFloatingFrame(constraints);
             } else {
                 tabbedPane.insertTab(constraints.getName(),
                     constraints.getIcon(),
                     constraints.getContainer(),
                     constraints.getToolTip(),
                     constraints.getPreferredIndex());
                 tabbedPane.setSelectedIndex(constraints.getPreferredIndex());
             }
         } else {
             if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
                 tabbedPane.addTab(constraints.getName(),
                     constraints.getIcon(),
                     ((FloatingFrame)constraints.getContainer()).getFloatingPanel(),
                     constraints.getToolTip());
                 this.addFloatingFrame(constraints);
             } else {
                 tabbedPane.addTab(constraints.getName(),
                     constraints.getIcon(),
                     constraints.getContainer(),
                     constraints.getToolTip());
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
             logger.info("removing FloatingFrameMenuBar '" + configurator.getId() + "' from MutableMenuBar"); // NOI18N
             menuBar.removeMoveableMenues(configurator.getId());
         }
 
         if (configurator.isSwapToolBar()) {
             logger.info("removing FloatingFrameToolBar '" + configurator.getId() + "' from MutableToolBar"); // NOI18N
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
         final JTabbedPane tabbedPane = this.getTabbedPaneAt(constraints.getPosition());
 
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
         final JTabbedPane tabbedPane = this.getTabbedPaneAt(constraints.getPosition());
 
         if (constraints.getContainerType().equals(MutableConstraints.FLOATINGFRAME)) {
             tabbedPane.setSelectedComponent(((FloatingFrame)constraints.getContainer()).getFloatingPanel());
         } else {
             tabbedPane.setSelectedComponent(constraints.getContainer());
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   position  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JTabbedPane getTabbedPaneAt(final String position) {
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
         return rootSplitPane;
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
                                 + constraints.getId() + "'"); // NOI18N
                 }
 
                 final JTabbedPane tabbedPane = getTabbedPaneAt(constraints.getPosition());
                 final int index = tabbedPane.indexOfComponent(constraints.getContainer());
 
                 if (e.getPropertyName().equals("name"))                                                            // NOI18N
                 {
                     tabbedPane.setTitleAt(index, constraints.getName());
                 } else if (e.getPropertyName().equals("tooltip"))                                                  // NOI18N
                 {
                     tabbedPane.setToolTipTextAt(index, constraints.getToolTip());
                 } else if (e.getPropertyName().equals("icon"))                                                     // NOI18N
                 {
                     tabbedPane.setIconAt(index, constraints.getIcon());
                 } else if (e.getPropertyName().equals("position") || e.getPropertyName().equals("preferredIndex")) // NOI18N
                 {
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
