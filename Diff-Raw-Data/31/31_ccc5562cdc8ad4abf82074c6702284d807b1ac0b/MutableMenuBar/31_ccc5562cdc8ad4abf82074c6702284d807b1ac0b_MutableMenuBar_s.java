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
  * Filename             :
  * Version                      :       2.0
  * Purpose                      :
  * Created                      :       27.04.2000
  * History                      :       02.08.2000 added support for dynamic Menus
  *
  *******************************************************************************/
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.*;
 //import Sirius.navigator.ui.embedded.*;
 //import Sirius.navigator.*;
 //import Sirius.navigator.Controls.*;
 //import Sirius.navigator.Views.*;
 //import Sirius.navigator.Views.Tree.*;
 //import Sirius.navigator.Dialog.*;
 //import Sirius.navigator.Dialog.Search.*;
 //import Sirius.navigator.connection.ConnectionHandler;
 //import Sirius.navigator.tools.ObjectManager;
 //import Sirius.navigator.PlugIn.*;
 //import Sirius.navigator.types.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.ui.dialog.*;
 import Sirius.navigator.ui.embedded.*;
 
 import org.apache.log4j.Logger;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.*;
 
 import javax.swing.*;
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
 
     private static final Logger logger = Logger.getLogger(MutableMenuBar.class);
 
     //~ Instance fields --------------------------------------------------------
 
     ResourceManager resources = ResourceManager.getManager();
     private final PluginMenuesMap pluginMenues;
     private final EmbeddedContainersMap moveableMenues;
     private Sirius.navigator.plugin.interfaces.LayoutManager layoutManager;
     private JMenu viewMenu;
     // Control Stuff
     // private ControlModel model;
     // private MethodManager methodManager;
     // private GenericMetaTree activeTree;
     // Default Menues
     // private JMenu navigatorMenu;
     // private JMenu functionsMenu;
     private JMenu pluginMenu;
     private JMenu searchMenu;
 //    private JMenu windowMenu;
     // private JMenu helpMenu;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * public MutableMenuBar(ControlModel model) { super(); this.model = model; methodManager = new
      * MethodManager(model); this.makeDefaultMenues(); }.
      */
     public MutableMenuBar() {
         super();
 
         pluginMenues = new PluginMenuesMap();
         moveableMenues = new EmbeddedContainersMap();
 
         this.makeDefaultMenues();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     // MOVEABLE MENUES ---------------------------------------------------------
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
      * PLUGIN MENUES -----------------------------------------------------------
      *
      * @param  menu  DOCUMENT ME!
      */
     public void addPluginMenu(final EmbeddedMenu menu) {
         if (menu.getItemCount() > 0) {
             this.pluginMenues.add(menu);
         } else if (logger.isDebugEnabled()) {
             logger.warn("menu '" + menu.getId() + "' does not contain any items, ignoring menu"); // NOI18N
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
      * Um festzustellen, welcher Tree gerade aktiv ist.
      */
     /*public GenericMetaTree getActiveTree()
      * { if(model.metaTree != null && (model.metaTree.hasFocus() || model.metaTree.isShowing())) return model.metaTree;
      * else if(model.searchTree != null && (model.searchTree.hasFocus() || model.searchTree.isShowing())) return
      * model.searchTree; else return null;}*/
     /**
      * Creates the default menues & menu entries.
      */
     private void makeDefaultMenues() {
         if (logger.isDebugEnabled()) {
             logger.debug("creating default menues"); // NOI18N
         }
 
         JMenu menu = null;
         JMenuItem item = null;
         final MenuItemActionListener itemListener = new MenuItemActionListener();
 
         menu = new JMenu(org.openide.util.NbBundle.getMessage(
                     MutableMenuBar.class,
                     "MutableMenuBar.navigatorMenu.title"));          // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.navigatorMenu.mnemonic").charAt(0)); // NOI18N
         this.add(menu);
 
         // LayoutControls
         // Gegenwärtiges Layout Speichern
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.saveCurrentLayout.title"))); // NOI18N
         item.setIcon(resources.getIcon("layout.png"));                             // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));                     // NOI18N
 
         item.setActionCommand("navigator.save.current.layout"); // NOI18N
         item.addActionListener(itemListener);
         // Layout öffnen
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.openLayout.title"))); // NOI18N
         item.setIcon(resources.getIcon("layout.png"));                      // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));              // NOI18N
         item.setActionCommand("navigator.open.layout");                     // NOI18N
         item.addActionListener(itemListener);
         // Layout reseten
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.navigatorMenu.resetLayout.title"))); // NOI18N
         item.setIcon(resources.getIcon("layout.png"));                       // NOI18N
         item.setAccelerator(KeyStroke.getKeyStroke("strg R"));               // NOI18N
         item.setActionCommand("navigator.reset.layout");                     // NOI18N
         item.addActionListener(itemListener);
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
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.toolsMenu.title")); // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.toolsMenu.mnemonic").charAt(0));                                                        // NOI18N
         this.add(menu);
         // password  ...............................................................
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(
                         MutableMenuBar.class,
                         "MutableMenuBar.toolsMenu.password.title")));     // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.toolsMenu.password.mnemonic").charAt(0)); // NOI18N
         item.setActionCommand("tools.password");                          // NOI18N
         item.addActionListener(itemListener);
 
         // this.add(new JSeparator(SwingConstants.HORIZONTAL));
 
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
 
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.windowMenu.title")); // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.windowMenu.mnemonic").charAt(0));                                                        // NOI18N
         this.add(menu);
         viewMenu = menu;
 
         // Help menu ......................................................
         menu = new JMenu(org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.helpMenu.title"));  // NOI18N
         menu.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.helpMenu.mnemonic").charAt(0));                                                         // NOI18N
         this.add(menu);
         menu.add(item = new JMenuItem(
                     org.openide.util.NbBundle.getMessage(MutableMenuBar.class, "MutableMenuBar.helpMenu.info.title"))); // NOI18N
         item.setMnemonic(org.openide.util.NbBundle.getMessage(
                 MutableMenuBar.class,
                 "MutableMenuBar.helpMenu.info.mnemonic").charAt(0));                                                    // NOI18N
         item.setIcon(resources.getIcon("information16.gif"));                                                           // NOI18N
         item.setActionCommand("help.info");                                                                             // NOI18N
         item.addActionListener(itemListener);
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
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (e.getActionCommand().equals("search.search")) {              // NOI18N
                 try {
                     MethodManager.getManager().showSearchDialog();
                 } catch (Throwable t) {
                     logger.fatal("Error while processing search method", t); // NOI18N
 
                     final ErrorDialog errorDialog = new ErrorDialog(
                             org.openide.util.NbBundle.getMessage(
                                 MutableMenuBar.class,
                                 "MutableMenuBar.MenuItemActionListener.actionPerformed(ActionEvent).ErrorDialog.message"), // NOI18N
                             t.toString(),
                             ErrorDialog.WARNING);
                     errorDialog.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
                     errorDialog.show();
                 }
             } else if (e.getActionCommand().equals("tools.password")) { // NOI18N
                 MethodManager.getManager().showPasswordDialog();
             }                 /*else if (e.getActionCommand().equals("navigator_logout"))
                                * {
                                * if(ExceptionManager.getManager().showExitDialog(ComponentRegistry.getRegistry().getMainWindow()))
                                * { logger.info("closing program"); System.exit(0); }}*/
             else if (e.getActionCommand().equals("navigator.exit")) { // NOI18N
                 if (ExceptionManager.getManager().showExitDialog(ComponentRegistry.getRegistry().getMainWindow())) {
                     logger.info("closing program"); // NOI18N
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
             } else if (e.getActionCommand().equals("help.info")) { // NOI18N
                 MethodManager.getManager().showAboutDialog();
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
                     logger.error("Error while refreshing the tree", ex); // NOI18N
                 } catch (RuntimeException ex) {
                     logger.error("Error while refreshing the tree", ex); // NOI18N
                 }
             } else if (e.getActionCommand().equals("navigator.reset.layout")) { // NOI18N
                 if (logger.isDebugEnabled()) {
                     logger.debug("reset layout"); // NOI18N
                 }
                 if (layoutManager != null) {
                     layoutManager.resetLayout();
                 } else {
                     // TODO Meldung Benutzer
                 }
             } else if (e.getActionCommand().equals("navigator.open.layout")) { // NOI18N
                 if (logger.isDebugEnabled()) {
                     logger.debug("open layout"); // NOI18N
                 }
                 if (layoutManager != null) {
                     layoutManager.loadLayout((java.awt.Component)StaticSwingTools.getParentFrame(MutableMenuBar.this));
                 } else {
                     // TODO Meldung Benutzer
                 }
             } else if (e.getActionCommand().equals("navigator.save.current.layout")) { // NOI18N
                 if (logger.isDebugEnabled()) {
                     logger.debug("save layout"); // NOI18N
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
     /*if (e.getActionCommand().equals("functions_search"))
      * { try { activeTree = getActiveTree();
      *
      * //NavigatorLogger.printMessage(activeTree); //methodManager.callSearch(activeTree, false);
      * methodManager.callSearch(activeTree, true); } catch (Throwable t) { if(NavigatorLogger.DEV) {
      * NavigatorLogger.printMessage("Fehler bei der Verarbeitung der Suchmethode"); t.printStackTrace(); }
      *
      * ErrorDialog errorDialog = new ErrorDialog(StringLoader.getString("STL@searchError"), t.toString(),
      * ErrorDialog.WARNING); errorDialog.setLocationRelativeTo(model.navigator); errorDialog.show(); } } else if
      * (e.getActionCommand().equals("functions_change_password")) { model.passwordDialog.show(); } else if
      * (e.getActionCommand().equals("navigator_logout")) { try { model.navigator.setVisible(false);
      *
      * // Das komplette Model musss geloescht werden, da der MetaTree // neue RootNodes erhaelt.
      * model.metaTree.setModel(null); model.searchTree.clear();
      *
      * ConnectionHandler.reset(); model.loginDialog.reset(); System.gc(); model.loginDialog.show();
      * ConnectionHandler.refillCache();
      *
      * model.metaTree.setModel(new DefaultTreeModel(new MetaTreeNode(ConnectionHandler.getTopNodes(), true), true));
      * System.gc(); model.navigator.setVisible(true); } catch(Exception ex) { if(NavigatorLogger.DEV) {
      * NavigatorLogger.printMessage("Fehler waehrend des Anmeldevorgangs: "); ex.printStackTrace(); }
      *
      * ErrorDialog errorDialog = new ErrorDialog(StringLoader.getString("STL@loginError"), e.toString(),
      * ErrorDialog.ERROR); errorDialog.setLocationRelativeTo(model.navigator); errorDialog.show(); } } else if
      * (e.getActionCommand().equals("navigator_exit")) { String message = StringLoader.getString("STL@shouldClose");
      * JOptionPane optionPane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
      * StringLoader.getStringArray("STL@yesNoOptionARRAY"), null); JDialog dialog =
      * optionPane.createDialog(model.navigator, StringLoader.getString("STL@exitProgram")); dialog.show();
      *
      * if(optionPane.getValue().equals(StringLoader.getString("STL@yes"))) {
      * if(NavigatorLogger.VERBOSE)NavigatorLogger.printMessage("<NAV> Navigator closed()"); model.navigator.dispose();
      * System.exit(0); } } else if(e.getActionCommand().equals("functions_search_profiles")) {
      * model.queryResultProfileManager.show();
      *
      * if(model.queryResultProfileManager.newNodesLoaded()) {
      * model.statusBar.setStatusString(model.searchTree.getResultNodes().length +
      * StringLoader.getString("STL@objectsLoaded"), StatusBar.STATUS_2); model.searchTree.bringToFront(); } }}*/
 
     /**
      * Local MoveableComponents implementation.
      *
      * @version  $Revision$, $Date$
      */
     /*
      * private class MoveableMenues extends MoveableComponents { private MoveableMenues(String id, Collection menues) {
      * super(id, menues); }
      *
      * protected void doSetComponentsVisible(boolean componentsVisible) { MoveableMenues.ComponentIterator iterator =
      * this.getComponentIterator();
      *
      * if(this.componentsVisible !=  componentsVisible) { if(componentsVisible) { MutableMenuBar.this.add(new
      * JSeparator(SwingConstants.VERTICAL));
      *
      * while(iterator.hasNext()) { MutableMenuBar.this.add(iterator.next()); } } else { while(iterator.hasNext()) {
      * MutableMenuBar.this.remove(iterator.next()); }
      *
      * Component component = MutableMenuBar.this.getComponent(MutableMenuBar.this.getMenuCount()-1); if(component
      * instanceof JSeparator) { MutableMenuBar.this.remove(component); } else { logger.warn("synchronization error: no
      * separator found but component '" + component + "'"); }
      *
      * } } else { logger.warn("synchronization error: componentsVisible = '" + componentsVisible + "' but '" +
      * !componentsVisible + "' expected"); }}*/
     // }
     private class PluginMenuesMap extends AbstractEmbeddedComponentsMap {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new PluginMenuesMap object.
          */
         private PluginMenuesMap() {
             Logger.getLogger(PluginMenuesMap.class);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void doAdd(final EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutableMenuBar.this.pluginMenu.add((EmbeddedMenu)component);
             } else {
                 this.logger.error("doAdd(): invalid object type '" + component.getClass().getName()
                             + "', 'Sirius.navigator.EmbeddedMenu' expected"); // NOI18N
             }
         }
 
         @Override
         protected void doRemove(final EmbeddedComponent component) {
             if (component instanceof EmbeddedMenu) {
                 MutableMenuBar.this.pluginMenu.remove((EmbeddedMenu)component);
             } else {
                 this.logger.error("doRemove(): invalid object type '" + component.getClass().getName()
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
                         MutableMenuBar.this.validateTree();
                         MutableMenuBar.this.repaint();
                     }
                 });
         }
 
         @Override
         protected void removeComponents() {
             Component component = null;
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
                         MutableMenuBar.this.validateTree();
                         MutableMenuBar.this.repaint();
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
