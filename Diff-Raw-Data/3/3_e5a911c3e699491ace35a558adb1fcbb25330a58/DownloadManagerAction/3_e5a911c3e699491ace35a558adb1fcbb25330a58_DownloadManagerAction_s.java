 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.downloadmanager;
 
 import org.openide.util.NbBundle;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.JDialog;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * This action is responsible for the the steps to be done when the user wants to see the current downloads.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class DownloadManagerAction extends AbstractAction {
 
     //~ Instance fields --------------------------------------------------------
 
     private Component parent;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DownloadManagerAction object.
      *
      * @param  parent  DOCUMENT ME!
      */
     public DownloadManagerAction(final Component parent) {
         super();
 
         this.parent = parent;
 
         putValue(
             SMALL_ICON,
             new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/tools/gui/downloadmanager/res/downloadmanager.png")));
         putValue(
             SHORT_DESCRIPTION,
             NbBundle.getMessage(DownloadManagerAction.class, "DownloadManagerAction.tooltiptext"));
         putValue(NAME, NbBundle.getMessage(DownloadManagerAction.class, "DownloadManagerAction.name"));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         final JDialog downloadManager = DownloadManagerDialog.instance(StaticSwingTools.getParentFrame(
                     parent));
         if (!downloadManager.isVisible()) {
             downloadManager.setLocationRelativeTo(StaticSwingTools.getParentFrame(
                     parent));
             downloadManager.setVisible(true);
             downloadManager.pack();
         } else {
             downloadManager.toFront();
         }
     }
 }
