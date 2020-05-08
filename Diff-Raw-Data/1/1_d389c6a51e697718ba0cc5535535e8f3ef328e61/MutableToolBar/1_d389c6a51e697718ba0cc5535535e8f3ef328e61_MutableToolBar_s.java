 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 import Sirius.navigator.exception.ExceptionManager;
 import Sirius.navigator.method.MethodManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.ui.embedded.AbstractEmbeddedComponentsMap;
 import Sirius.navigator.ui.embedded.EmbeddedComponent;
 import Sirius.navigator.ui.embedded.EmbeddedToolBar;
 
 import com.jgoodies.looks.HeaderStyle;
 import com.jgoodies.looks.Options;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 
 import de.cismet.tools.gui.downloadmanager.DownloadManagerAction;
 
 /**
  * Eine Toolbar, zu der zur Laufzeit automatisch neue Buttons hinzugefuegt- und entfernt werden koennen.
  *
  * @version  $Revision$, $Date$
  */
 public class MutableToolBar extends JToolBar {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(MutableToolBar.class);
 
     private static final ResourceManager resources = ResourceManager.getManager();
 
     //~ Instance fields --------------------------------------------------------
 
     private final JToolBar defaultToolBar;
     private final MoveableToolBarsMap moveableToolBars;
     private final PluginToolBarsMap pluginToolBars;
 
     private final boolean advancedLayout;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MutableToolBar object.
      */
     public MutableToolBar() {
         this(false);
     }
 
     /**
      * Creates a new MutableToolBar object.
      *
      * @param  advancedLayout  DOCUMENT ME!
      */
     public MutableToolBar(final boolean advancedLayout) {
         super(HORIZONTAL);
 
         this.advancedLayout = advancedLayout;
 
         moveableToolBars = new MoveableToolBarsMap();
         pluginToolBars = new PluginToolBarsMap();
 
         this.defaultToolBar = new JToolBar(
                 org.openide.util.NbBundle.getMessage(MutableToolBar.class, "MutableToolBar.defaultToolBar.name"), // NOI18N
                 HORIZONTAL);
         this.defaultToolBar.setFloatable(false);
         this.defaultToolBar.setRollover(advancedLayout);
         defaultToolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
         defaultToolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         defaultToolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         this.createDefaultButtons();
         this.add(defaultToolBar);
         putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
         putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         this.setFloatable(false);
 
         if (advancedLayout) {
             this.setBorder(null);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void createDefaultButtons() {
         if (logger.isDebugEnabled()) {
             logger.debug("creating default buttons"); // NOI18N
         }
 
         final ActionListener toolBarListener = new ToolBarListener();
         JButton button = null;
 
         button = new JButton(resources.getIcon("find24.gif"));            // NOI18N
         button.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableToolBar.class,
                 "MutableToolBar.createDefaultButtons().search.tooltip")); // NOI18N
         button.setActionCommand("search");                                // NOI18N
         button.setMargin(new Insets(4, 4, 4, 4));
         button.addActionListener(toolBarListener);
         defaultToolBar.add(button);
 
         button = new JButton(resources.getIcon("plugin24.gif"));          // NOI18N
         button.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableToolBar.class,
                 "MutableToolBar.createDefaultButtons().plugin.tooltip")); // NOI18N
         button.setActionCommand("plugin");                                // NOI18N
         button.setEnabled(false);                                         // HELL
         button.setMargin(new Insets(4, 4, 4, 4));
         button.addActionListener(toolBarListener);
         defaultToolBar.add(button);
 
         button = new JButton(new DownloadManagerAction(this)); // NOI18N
        button.setText("");
         button.setMargin(new Insets(4, 4, 4, 4));
         defaultToolBar.add(button);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  toolBar  DOCUMENT ME!
      */
     public void addMoveableToolBar(final EmbeddedToolBar toolBar) {
         toolBar.setRollover(this.advancedLayout);
         toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
         toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         this.moveableToolBars.add(toolBar);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id  DOCUMENT ME!
      */
     public void removeMoveableToolBar(final String id) {
         this.moveableToolBars.remove(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  visible  DOCUMENT ME!
      */
     public void setMoveableToolBarVisible(final String id, final boolean visible) {
         this.moveableToolBars.setVisible(id, visible);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isMoveableToolBarVisible(final String id) {
         return this.moveableToolBars.isVisible(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  enabled  DOCUMENT ME!
      */
     public void setMoveableToolBarEnabled(final String id, final boolean enabled) {
         this.moveableToolBars.setEnabled(id, enabled);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isMoveableToolBarEnabled(final String id) {
         return this.moveableToolBars.isEnabled(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isMoveableToolBarAvailable(final String id) {
         return this.moveableToolBars.isAvailable(id);
     }
 
     /**
      * PLUGIN TOOLBARS ---------------------------------------------------------
      *
      * @param  toolBar  DOCUMENT ME!
      */
     public void addPluginToolBar(final EmbeddedToolBar toolBar) {
         toolBar.setRollover(this.advancedLayout);
         toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
         toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
         this.pluginToolBars.add(toolBar);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id  DOCUMENT ME!
      */
     public void removePluginToolBar(final String id) {
         this.pluginToolBars.remove(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  enabled  DOCUMENT ME!
      */
     public void setPluginToolBarEnabled(final String id, final boolean enabled) {
         this.pluginToolBars.setEnabled(id, enabled);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginToolBarEnabled(final String id) {
         return this.pluginToolBars.isEnabled(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginToolBarAvailable(final String id) {
         return this.pluginToolBars.isAvailable(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JToolBar getDefaultToolBar() {
         return defaultToolBar;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ToolBarListener implements ActionListener {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * Invoked when an action occurs.
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (e.getActionCommand().equals("exit"))                        // NOI18N
             {
                 if (ExceptionManager.getManager().showExitDialog(ComponentRegistry.getRegistry().getMainWindow())) {
                     logger.info("closing program");                         // NOI18N
                     ComponentRegistry.getRegistry().getNavigator().dispose();
                     System.exit(0);
                 }
             } else if (e.getActionCommand().equals("search"))               // NOI18N
             {
                 try {
                     MethodManager.getManager().showSearchDialog();
                 } catch (Throwable t) {
                     logger.fatal("Error while processing searchmethod", t); // NOI18N
                 }
             } else if (e.getActionCommand().equals("plugin"))               // NOI18N
             {
                 MethodManager.getManager().showPluginManager();
             } else if (e.getActionCommand().equals("info"))                 // NOI18N
             {
                 MethodManager.getManager().showAboutDialog();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class PluginToolBarsMap extends AbstractEmbeddedComponentsMap {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PluginToolBarsMap object.
          */
         private PluginToolBarsMap() {
             Logger.getLogger(PluginToolBarsMap.class);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void doAdd(final EmbeddedComponent component) {
             if (logger.isDebugEnabled()) {
                 logger.debug("adding toolbar: '" + component + "'");             // NOI18N
             }
             if (component instanceof EmbeddedToolBar) {
                 MutableToolBar.this.add((EmbeddedToolBar)component);
             } else {
                 this.logger.error("doAdd(): invalid object type '" + component.getClass().getName()
                             + "', 'Sirius.navigator.EmbeddedToolBar' expected"); // NOI18N
             }
 
             MutableToolBar.this.invalidate();
             SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         MutableToolBar.this.validateTree();
                         MutableToolBar.this.repaint();
                     }
                 });
         }
 
         @Override
         protected void doRemove(final EmbeddedComponent component) {
             if (component instanceof EmbeddedToolBar) {
                 MutableToolBar.this.remove((EmbeddedToolBar)component);
             } else {
                 this.logger.error("doRemove(): invalid object type '" + component.getClass().getName()
                             + "', 'Sirius.navigator.EmbeddedToolBar' expected"); // NOI18N
             }
 
             MutableToolBar.this.invalidate();
             SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         MutableToolBar.this.validateTree();
                         MutableToolBar.this.repaint();
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class MoveableToolBarsMap extends PluginToolBarsMap {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new MoveableToolBarsMap object.
          */
         private MoveableToolBarsMap() {
             Logger.getLogger(MoveableToolBarsMap.class);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void doSetVisible(final EmbeddedComponent component, final boolean visible) {
             if (component.isVisible() != visible) {
                 super.doSetVisible(component, visible);
 
                 if (visible) {
                     this.doAdd(component);
                 } else {
                     this.doRemove(component);
                 }
             } else {
                 this.logger.warn("unexpected call to 'setVisible()': '" + visible + "'"); // NOI18N
             }
         }
     }
 }
