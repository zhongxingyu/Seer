 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.exception.ExceptionManager;
 import Sirius.navigator.method.MethodManager;
 import Sirius.navigator.resource.PropertyManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.search.dynamic.SearchSearchTopicsDialogAction;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.ui.dialog.ErrorDialog;
 import Sirius.navigator.ui.embedded.AbstractEmbeddedComponentsMap;
 import Sirius.navigator.ui.embedded.EmbeddedComponent;
 import Sirius.navigator.ui.embedded.EmbeddedContainer;
 import Sirius.navigator.ui.embedded.EmbeddedContainersMap;
 import Sirius.navigator.ui.embedded.EmbeddedMenu;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.Collection;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class MutableMenuBar extends JMenuBar {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(MutableMenuBar.class);
 
     //~ Instance fields --------------------------------------------------------
 
     ResourceManager resources = ResourceManager.getManager();
     private final PluginMenuesMap pluginMenues;
     private final EmbeddedContainersMap moveableMenues;
     private Sirius.navigator.plugin.interfaces.LayoutManager layoutManager;
     private JMenu viewMenu;
     private JMenu pluginMenu;
     private JMenu searchMenu;
 
     private String helpUrl;
     private String newsUrl;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MutableMenuBar object.
      */
     public MutableMenuBar() {
         super();
 
         pluginMenues = new PluginMenuesMap();
         moveableMenues = new EmbeddedContainersMap();
 
         this.makeDefaultMenues();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Adds new moveable menues to this menu bar.
      *
      * @param  id      DOCUMENT ME!
      * @param  menues  DOCUMENT ME!
      */
     public void addMoveableMenues(final String id, final Collection menues) {
         this.moveableMenues.add(new MoveableMenues(id, menues));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id  DOCUMENT ME!
      */
     public void removeMoveableMenues(final String id) {
         this.moveableMenues.remove(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  visible  DOCUMENT ME!
      */
     public void setMoveableMenuesVisible(final String id, final boolean visible) {
         this.moveableMenues.setVisible(id, visible);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isMoveableMenuesVisible(final String id) {
         return this.moveableMenues.isVisible(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  enabled  DOCUMENT ME!
      */
     public void setMoveableMenuesEnabled(final String id, final boolean enabled) {
         this.moveableMenues.setEnabled(id, enabled);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isMoveableMenuesEnabled(final String id) {
         return this.moveableMenues.isEnabled(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isMoveableMenuesAvailable(final String id) {
         return this.moveableMenues.isAvailable(id);
     }
     /**
      * DOCUMENT ME!
      *
      * @param  menu  DOCUMENT ME!
      */
     public void addPluginMenu(final EmbeddedMenu menu) {
         if (menu.getItemCount() > 0) {
             this.pluginMenues.add(menu);
         } else if (LOG.isDebugEnabled()) {
             LOG.warn("menu '" + menu.getId() + "' does not contain any items, ignoring menu"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id  DOCUMENT ME!
      */
     public void removePluginMenu(final String id) {
         this.pluginMenues.remove(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  id       DOCUMENT ME!
      * @param  enabled  DOCUMENT ME!
      */
     public void setPluginMenuEnabled(final String id, final boolean enabled) {
         this.pluginMenues.setEnabled(id, enabled);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginMenuEnabled(final String id) {
         return this.pluginMenues.isEnabled(id);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginMenuAvailable(final String id) {
         return this.pluginMenues.isAvailable(id);
     }
 
     /**
      * Creates the default menues & menu entries.
      */
     private void makeDefaultMenues() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("creating default menues"); // NOI18N
         }
 
         final MenuItemActionListener itemListener = new MenuItemActionListener();
 
         JMenu menu = new JMenu(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.navigatorMenu.title"));          // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.mnemonic").charAt(0)); // NOI18N
         this.add(menu);
 
         // LayoutControls
         // Gegenwärtiges Layout Speichern
         JMenuItem item = new JMenuItem(
                 org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.navigatorMenu.saveCurrentLayout.title")); // NOI18N
         menu.add(item);
         item.setIcon(resources.getIcon("layout.png"));                        // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));                // NOI18N
         item.setActionCommand("navigator.save.current.layout");               // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.saveCurrentLayout.tooltip"));   // NOI18N
 
         // Layout öffnen
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.openLayout.title"))); // NOI18N
         item.setIcon(resources.getIcon("layout.png"));                      // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));              // NOI18N
         item.setActionCommand("navigator.open.layout");                     // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.openLayout.tooltip"));        // NOI18N
 
         // Layout reseten
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.resetLayout.title"))); // NOI18N
         item.setIcon(resources.getIcon("layout.png"));                       // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("strg R"));               // NOI18N
         item.setActionCommand("navigator.reset.layout");                     // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.resetLayout.tooltip"));        // NOI18N
 
         menu.add(new JSeparator());
 
         // Hell
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.reloadCatalogue.title")));     // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.reloadCatalogue.mnemonic").charAt(0)); // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("F5"));                           // NOI18N
         item.setActionCommand("tree.refresh");                                       // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.reloadCatalogue.tooltip"));            // NOI18N
 
         menu.add(new JSeparator());
 
         // Beenden .............................................................
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.exit.title")));     // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.exit.mnemonic").charAt(0)); // NOI18N
         item.setIcon(resources.getIcon("stop16.gif"));                    // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("alt X"));             // NOI18N
         item.setActionCommand("navigator.exit");                          // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.exit.tooltip"));            // NOI18N
 
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.searchMenu.title")); // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.searchMenu.mnemonic").charAt(0));                                                        // NOI18N
         this.add(menu);
 
         // Suche ...............................................................
         if (PropertyManager.getManager().isEnableSearchDialog()) {
             menu.add(item = new JMenuItem(
                         org.openide.util.NbBundle.getMessage(
                             MutableMenuBar.class,
                             "MutableMenuBar.searchMenu.search.title")));     // NOI18N
             item.setMnemonic(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.searchMenu.search.mnemonic").charAt(0)); // NOI18N
             item.setIcon(resources.getIcon("find16.gif"));                   // NOI18N
             item.setAccelerator(KeyStroke.getKeyStroke("alt S"));            // NOI18N
             item.setActionCommand("search.search");                          // NOI18N
             item.addActionListener(itemListener);
             item.setToolTipText(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.searchMenu.search.tooltip"));            // NOI18N);
 
             menu.addSeparator();
         } else {
             menu.add(item = new JMenuItem(new SearchSearchTopicsDialogAction())); // NOI18N
             item.setText(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.searchMenu.search.title"));                   // NOI18N
             item.setMnemonic(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.searchMenu.search.mnemonic").charAt(0));      // NOI18N
             item.setAccelerator(KeyStroke.getKeyStroke("alt S"));                 // NOI18N
             item.setToolTipText(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.searchMenu.search.tooltip"));                 // NOI18N);
             menu.addSeparator();
         }
         // show search results .................................................
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.searchMenu.showResults.title")));     // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.searchMenu.showResults.mnemonic").charAt(0)); // NOI18N
         item.setIcon(resources.getIcon("searchresults16.gif"));               // NOI18N
         item.setActionCommand("search.show");                                 // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.searchMenu.showResults.tooltip"));            // NOI18N);
 
         // search result profiles .....................................................
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.searchMenu.searchResultProfiles.title")));     // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.searchMenu.searchResultProfiles.mnemonic").charAt(0)); // NOI18N
         item.setIcon(resources.getIcon("searchresultprofiles16.gif"));                 // NOI18N
         item.setActionCommand("search.profiles.result");                               // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.searchMenu.searchResultProfiles.tooltip"));            // NOI18N);
 
         // search profiles .....................................................
         if (PropertyManager.getManager().isEnableSearchDialog()) {
             menu.addSeparator();
             menu.add(item = new JMenuItem(
                         org.openide.util.NbBundle.getMessage(
                             MutableMenuBar.class,
                             "MutableMenuBar.searchMenu.searchProfiles.title")));     // NOI18N
             item.setMnemonic(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.searchMenu.searchProfiles.mnemonic").charAt(0)); // NOI18N
             item.setIcon(resources.getIcon("searchresultprofiles16.gif"));           // NOI18N
             item.setActionCommand("search.profiles");                                // NOI18N
             item.addActionListener(itemListener);
         }
 
         searchMenu = menu;
 
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.pluginMenu.title")); // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.pluginMenu.mnemonic").charAt(0));                                                        // NOI18N
         menu.setEnabled(false);                                                                                          // HELL
         this.add(menu);
         this.pluginMenu = menu;
         // plugin manager ......................................................
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.pluginMenu.pluginManager.title")));     // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.pluginMenu.pluginManager.mnemonic").charAt(0)); // NOI18N
         item.setIcon(resources.getIcon("plugin_node_root.gif"));                // NOI18N
         item.setActionCommand("plugin.manager");                                // NOI18N
         item.addActionListener(itemListener);
         menu.addSeparator();
 
         // Extras menu ......................................................
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.extrasMenu.title")); // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.extrasMenu.mnemonic").charAt(0));                                                        // NOI18N
         this.add(menu);
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.extrasMenu.options.title")));                                                    // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.extrasMenu.options.mnemonic").charAt(0));                                                // NOI18N
         item.setIcon(resources.getIcon("tooloptions.png"));                                                              // NOI18N
         item.setActionCommand("extras.options");                                                                         // NOI18N
         item.addActionListener(itemListener);
         item.setToolTipText(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.extrasMenu.options.tooltip"));                                                           // NOI18N
 
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.windowMenu.title")); // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.windowMenu.mnemonic").charAt(0));                                                        // NOI18N
         this.add(menu);
         viewMenu = menu;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  viewItem  DOCUMENT ME!
      */
     public void addViewMenuItem(final JMenuItem viewItem) {
         if (viewMenu != null) {
             viewMenu.add(viewItem);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JMenu getViewMenu() {
         return viewMenu;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JMenu getSearchMenu() {
         return searchMenu;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  layoutManager  DOCUMENT ME!
      */
     public void registerLayoutManager(final Sirius.navigator.plugin.interfaces.LayoutManager layoutManager) {
         this.layoutManager = layoutManager;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * INNERE KLASSEN ZUM BEARBEITEN DER EREIGNISSE ============================.
      *
      * @version  $Revision$, $Date$
      */
     private class MenuItemActionListener implements ActionListener {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (e.getActionCommand().equals("search.search")) {            // NOI18N
                 try {
                     MethodManager.getManager().showSearchDialog();
                 } catch (Exception ex) {
                     LOG.fatal("Error while processing search method", ex); // NOI18N
 
                     final ErrorDialog errorDialog = new ErrorDialog(
                             org.openide.util.NbBundle.getMessage(
                                 MutableMenuBar.class,
                                 "MutableMenuBar.MenuItemActionListener.actionPerformed(ActionEvent).ErrorDialog.message"), // NOI18N
                             ex.toString(),
                             ErrorDialog.WARNING);
                     StaticSwingTools.showDialog(errorDialog);
                 }
             } else if (e.getActionCommand().equals("navigator.exit")) { // NOI18N
                 if (ExceptionManager.getManager().showExitDialog(ComponentRegistry.getRegistry().getMainWindow())) {
                     LOG.info("closing program"); // NOI18N
                     ComponentRegistry.getRegistry().getNavigator().dispose();
                     System.exit(0);
                 }
             } else if (e.getActionCommand().equals("search.show")) { // NOI18N
                 MethodManager.getManager().showSearchResults();
             } else if (e.getActionCommand().equals("search.profiles.result")) { // NOI18N
                 MethodManager.getManager().showQueryResultProfileManager();
             } else if (e.getActionCommand().equals("search.profiles")) { // NOI18N
                 MethodManager.getManager().showQueryProfilesManager();
             } else if (e.getActionCommand().equals("plugin.manager")) { // NOI18N
                 MethodManager.getManager().showPluginManager();
             } else if (e.getActionCommand().equals("extras.options")) { // NOI18N
                 MethodManager.getManager().showOptionsDialog();
             } else if (e.getActionCommand().equals("tree.refresh")) { // NOI18N
                 try {
                     final TreePath selectionPath = ComponentRegistry.getRegistry()
                                 .getCatalogueTree()
                                 .getSelectionPath();
                     if ((selectionPath != null) && (selectionPath.getPath().length > 0)) {
                         final RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());
                         ((DefaultTreeModel)ComponentRegistry.getRegistry().getCatalogueTree().getModel()).setRoot(
                             rootTreeNode);
                         ((DefaultTreeModel)ComponentRegistry.getRegistry().getCatalogueTree().getModel()).reload();
                         ComponentRegistry.getRegistry().getCatalogueTree().exploreSubtree(selectionPath);
                     }
                 } catch (ConnectionException ex) {
                     LOG.error("Error while refreshing the tree", ex); // NOI18N
                 } catch (RuntimeException ex) {
                     LOG.error("Error while refreshing the tree", ex); // NOI18N
                 }
             } else if (e.getActionCommand().equals("navigator.reset.layout")) { // NOI18N
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("reset layout"); // NOI18N
                 }
                 if (layoutManager != null) {
                     layoutManager.resetLayout();
                 } else {
                     // TODO Meldung Benutzer
                 }
             } else if (e.getActionCommand().equals("navigator.open.layout")) { // NOI18N
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("open layout"); // NOI18N
                 }
                 if (layoutManager != null) {
                     layoutManager.loadLayout((java.awt.Component)StaticSwingTools.getParentFrame(MutableMenuBar.this));
                 } else {
                     // TODO Meldung Benutzer
                 }
             } else if (e.getActionCommand().equals("navigator.save.current.layout")) { // NOI18N
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("save layout"); // NOI18N
                 }
                 if (layoutManager != null) {
                     layoutManager.saveCurrentLayout((java.awt.Component)StaticSwingTools.getParentFrame(
                             MutableMenuBar.this));
                 } else {
                     // TODO Meldung Benutzer
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class PluginMenuesMap extends AbstractEmbeddedComponentsMap {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PluginMenuesMap object.
          */
         private PluginMenuesMap() {
             Logger.getLogger(PluginMenuesMap.class);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  component  DOCUMENT ME!
          */
         @Override
         protected void doAdd(final EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutableMenuBar.this.pluginMenu.add((EmbeddedMenu)component);
             } else {
                 this.logger.error("doAdd(): invalid object type '" + component.getClass().getName() // NOI18N
                             + "', 'Sirius.navigator.EmbeddedMenu' expected"); // NOI18N
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  component  DOCUMENT ME!
          */
         @Override
         protected void doRemove(final EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutableMenuBar.this.pluginMenu.remove((EmbeddedMenu)component);
             } else {
                 this.logger.error("doRemove(): invalid object type '" + component.getClass().getName() // NOI18N
                             + "', 'Sirius.navigator.EmbeddedMenu' expected"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class MoveableMenues extends EmbeddedContainer {
 
         //~ Instance fields ----------------------------------------------------
 
         /** LOGGER. */
         private final transient Logger logger = Logger.getLogger(MoveableMenues.class);
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new MoveableMenues object.
          *
          * @param  id          DOCUMENT ME!
          * @param  components  DOCUMENT ME!
          */
         public MoveableMenues(final String id, final Collection components) {
             super(id, components);
             super.setVisible(true);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  visible  DOCUMENT ME!
          */
         @Override
         public void setVisible(final boolean visible) {
             if (this.isVisible() != visible) {
                 super.setVisible(visible);
 
                 if (visible) {
                     this.addComponents();
                 } else {
                     this.removeComponents();
                 }
             } else {
                 this.logger.warn("unexpected call to 'setVisible()': '" + visible + "'"); // NOI18N
             }
         }
 
         /**
          * DOCUMENT ME!
          */
         @Override
         protected void addComponents() {
             Component component = MutableMenuBar.this.getComponent(MutableMenuBar.this.getComponentCount() - 1);
             if (!(component instanceof MutableMenuSeparator)) {
                 MutableMenuBar.this.add(new MutableMenuSeparator());
             }
 
             final ComponentIterator iterator = this.iterator();
             while (iterator.hasNext()) {
                 component = iterator.next();
                 if (component != null) {
                     if (component instanceof JMenu) {
                         MutableMenuBar.this.add((JMenu)component);
                     } else {
                         this.logger.error("addComponents(): invalid object type '" + component.getClass().getName()
                                     + "', 'javax.swing.JMenu' expected"); // NOI18N
                     }
                 }
             }
 
             MutableMenuBar.this.invalidate();
             SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                        synchronized (getTreeLock()) {
                            validateTree();
                            repaint();
                        }
                     }
                 });
         }
 
         /**
          * DOCUMENT ME!
          */
         @Override
         protected void removeComponents() {
             Component component;
             final ComponentIterator iterator = this.iterator();
 
             while (iterator.hasNext()) {
                 component = iterator.next();
                 if (component != null) {
                     if (component instanceof JMenu) {
                         MutableMenuBar.this.remove(component);
                     } else {
                         this.logger.error("removeComponents(): invalid object type '" + component.getClass().getName()
                                     + "', 'javax.swing.JMenu' expected"); // NOI18N
                     }
                 }
             }
 
             component = MutableMenuBar.this.getComponent(MutableMenuBar.this.getComponentCount() - 1);
             if (component instanceof MutableMenuSeparator) {
                 MutableMenuBar.this.remove(component);
             }
 
             MutableMenuBar.this.invalidate();
             SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                        synchronized (getTreeLock()) {
                            validateTree();
                            repaint();
                        }
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class MutableMenuSeparator extends JSeparator {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new MutableMenuSeparator object.
          */
         public MutableMenuSeparator() {
             super(SwingConstants.VERTICAL);
             this.setMaximumSize(new Dimension(5, 60));
         }
     }
 }
