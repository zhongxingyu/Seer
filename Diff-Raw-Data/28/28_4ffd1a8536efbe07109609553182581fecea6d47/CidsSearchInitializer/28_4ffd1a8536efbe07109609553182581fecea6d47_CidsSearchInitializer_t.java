 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.search;
 
 import Sirius.navigator.actiontag.ActionTagProtected;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.GUIContainer;
 import Sirius.navigator.ui.MutableConstraints;
 import Sirius.navigator.ui.MutableMenuBar;
 import Sirius.navigator.ui.MutableToolBar;
 
 import org.openide.util.Lookup;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 
 import de.cismet.cids.tools.search.clientstuff.CidsDialogSearch;
 import de.cismet.cids.tools.search.clientstuff.CidsToolbarSearch;
 import de.cismet.cids.tools.search.clientstuff.CidsWindowSearch;
 
 import de.cismet.tools.StaticDebuggingTools;
 
 /**
  * This class is responsibility is to lookup all relevant search components, to initialize and to plug them into the
  * navigator gui.
  *
  * @author   stefan
  * @version  $Revision$, $Date$
  */
 public class CidsSearchInitializer {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CidsSearchInitializer object.
      */
     public CidsSearchInitializer() {
         initializeSearch();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void initializeSearch() {
         final Collection<? extends CidsToolbarSearch> toolbarSearches = Lookup.getDefault()
                     .lookupAll(CidsToolbarSearch.class);
         final Collection<? extends CidsDialogSearch> dialogeSearches = Lookup.getDefault()
                     .lookupAll(CidsDialogSearch.class);
         final Collection<? extends CidsWindowSearch> windowSearches = Lookup.getDefault()
                     .lookupAll(CidsWindowSearch.class);
         //
         final MutableMenuBar menuBar = ComponentRegistry.getRegistry().getMutableMenuBar();
         final MutableToolBar toolBar = ComponentRegistry.getRegistry().getMutableToolBar();
         final GUIContainer guiContainer = ComponentRegistry.getRegistry().getGUIContainer();
         // Toolbar Searches
        if (StaticDebuggingTools.checkHomeForFile("cidsNewServerSearchEnabled")) {
            if (!toolbarSearches.isEmpty()) {
                final CidsSearchComboBar searchBar = new CidsSearchComboBar();
                final Iterator<? extends CidsToolbarSearch> itTS = toolbarSearches.iterator();
                while (itTS.hasNext()) {
                    if (!checkActionTag(itTS.next())) {
                        itTS.remove();
                    }
                 }
                searchBar.setSearches(toolbarSearches);
                final JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
                innerPanel.add(searchBar);
                toolBar.add(innerPanel, -1);
             }
         }
         // Dialog Searches
         if (!dialogeSearches.isEmpty()) {
             final JMenu menu = menuBar.getSearchMenu();
             menu.add(new JSeparator());
             for (final CidsDialogSearch dialogSearch : dialogeSearches) {
                 if (checkActionTag(dialogSearch)) {
                     final JMenuItem item = new JMenuItem(dialogSearch.getName());
                     menu.add(item);
                     item.setIcon(dialogSearch.getIcon());
                     item.addActionListener(new DialogActionListener(dialogSearch));
                 }
             }
         }
         // Window Searches
         if (!windowSearches.isEmpty()) {
             for (final CidsWindowSearch windowSearch : windowSearches) {
                 if (checkActionTag(windowSearch)) {
                     final MutableConstraints mutableConstraints = new MutableConstraints();
                     final String name = windowSearch.getName();
                     mutableConstraints.addAsComponent(windowSearch.getClass().getName(),
                         windowSearch.getSearchWindowComponent(),
                         name,
                         name,
                         windowSearch.getIcon(),
                         MutableConstraints.P1,
                         MutableConstraints.ANY_INDEX);
                     guiContainer.add(mutableConstraints);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toCheck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean checkActionTag(final Object toCheck) {
         if (toCheck instanceof ActionTagProtected) {
             final ActionTagProtected atp = (ActionTagProtected)toCheck;
             return atp.checkActionTag();
         }
         return true;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     static final class DialogActionListener implements ActionListener {
 
         //~ Instance fields ----------------------------------------------------
 
         private final JDialog dialoge;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DialogActionListener object.
          *
          * @param  search  DOCUMENT ME!
          */
         public DialogActionListener(final CidsDialogSearch search) {
             this.dialoge = search.getDialogComponent();
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
             dialoge.setLocation((screenSize.width - dialoge.getWidth()) / 2,
                 (screenSize.height - dialoge.getHeight())
                         / 2);
             dialoge.setVisible(true);
         }
     }
 }
