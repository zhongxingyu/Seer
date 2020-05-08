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
 package de.cismet.cids.custom.butler;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import org.apache.log4j.Logger;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Icon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 
 import de.cismet.cids.custom.nas.NasDialog;
 
 import de.cismet.cismap.commons.gui.ToolbarComponentDescription;
 import de.cismet.cismap.commons.gui.ToolbarComponentsProvider;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.tools.gui.JPopupMenuButton;
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = ToolbarComponentsProvider.class)
 public class DigitalDataExportToolbarComponentProvider implements ToolbarComponentsProvider {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger log = Logger.getLogger(DigitalDataExportToolbarComponentProvider.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final List<ToolbarComponentDescription> toolbarComponents;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DigitalDataExportToolbarComponentProvider object.
      */
     public DigitalDataExportToolbarComponentProvider() {
         final List<ToolbarComponentDescription> preparationList = new LinkedList<ToolbarComponentDescription>();
         final ToolbarComponentDescription description = new ToolbarComponentDescription(
                 "tlbMain",
                 new DataExportButton(),
                 ToolbarPositionHint.AFTER,
                 "cmdPrint");
         preparationList.add(description);
         this.toolbarComponents = Collections.unmodifiableList(preparationList);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getPluginName() {
         return "BUTLER";
     }
 
     @Override
     public Collection<ToolbarComponentDescription> getToolbarComponents() {
         if (validateUserHasButler1Access() || validateUserHasNasAccess()) {
             return toolbarComponents;
         } else {
             return Collections.emptyList();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasButler1Access() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "csa://butler1Query")
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Butler!", ex);
         }
         return false;
     }
 
     /**
      * public static boolean validateUserHasButler2Acces(){ try { return SessionManager.getConnection()
      * .getConfigAttr(SessionManager.getSession().getUser(), "csa://butler1Query") != null; } catch (ConnectionException
      * ex) { log.error("Could not validate action tag for Butler!", ex); } return false; }.
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasNasAccess() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "csa://nasDataQuery")
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Butler!", ex);
         }
         return false;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class DataExportButton extends JPopupMenuButton {
 
         //~ Instance fields ----------------------------------------------------
 
         Icon exportIcon = new javax.swing.ImageIcon(getClass().getResource(
                     "/de/cismet/cids/custom/icons/alkis_export.png")); // NOI18N
         private JPopupMenu popUpMenu = new DataExportPopupMenu();
         private boolean setsPopUpVisible = false;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DataExportButton object.
          */
         public DataExportButton() {
             super();
             super.setIcon(exportIcon);
             super.setPopupMenu(popUpMenu);
             setFocusPainted(false);
             setBorderPainted(false);
             this.addActionListener(new ActionListener() {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         SwingUtilities.invokeLater(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     if (!popUpMenu.isVisible() && !setsPopUpVisible) {
                                         setsPopUpVisible = true;
                                         popUpMenu.setVisible(true);
                                     } else {
                                         if (!popUpMenu.isVisible()) {
                                             setsPopUpVisible = false;
                                         }
                                     }
                                 }
                             });
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class DataExportPopupMenu extends JPopupMenu {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DataExportPopupMenu object.
          */
         public DataExportPopupMenu() {
             if (validateUserHasNasAccess()) {
                 this.add(createNASMenuItem());
             }
             if (validateUserHasButler1Access()) {
                 this.add(createButler2MenuItem());
                this.add(createButler1MenuItem());
             }
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private JMenuItem createNASMenuItem() {
             final String title = org.openide.util.NbBundle.getMessage(NasDialog.class, "NasDialog.title");
             final AbstractAction action = new AbstractAction(title) {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         StaticSwingTools.showDialog(
                             new NasDialog(
                                 StaticSwingTools.getParentFrame(
                                     CismapBroker.getInstance().getMappingComponent()),
                                 true));
                     }
                 };
             return new JMenuItem(action);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private JMenuItem createButler1MenuItem() {
             final String title = org.openide.util.NbBundle.getMessage(Butler1Dialog.class, "Butler1Dialog.title_1");
             final AbstractAction action = new AbstractAction(title) {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         StaticSwingTools.showDialog(
                             new Butler1Dialog(
                                 StaticSwingTools.getParentFrame(
                                     CismapBroker.getInstance().getMappingComponent()),
                                 true));
                     }
                 };
             return new JMenuItem(action);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private JMenuItem createButler2MenuItem() {
             final String title = org.openide.util.NbBundle.getMessage(Butler2Dialog.class, "Butler2Dialog.title");
             final AbstractAction action = new AbstractAction(title) {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         StaticSwingTools.showDialog(
                             new Butler2Dialog(
                                 StaticSwingTools.getParentFrame(
                                     CismapBroker.getInstance().getMappingComponent()),
                                 true));
                     }
                 };
             return new JMenuItem(action);
         }
     }
 }
