 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.event;
 
 import Sirius.navigator.ui.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.JTree;
 import javax.swing.tree.TreePath;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class CataloguePopupMenuListener extends MouseAdapter {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private final MutablePopupMenu popupMenu;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of CataloguePopupMenuListener.
      *
      * @param  popupMenu  DOCUMENT ME!
      */
     public CataloguePopupMenuListener(final MutablePopupMenu popupMenu) {
         this.popupMenu = popupMenu;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Invoked when the mouse button has been clicked (pressed and released) on a component.
      *
      * @param  e  DOCUMENT ME!
      */
     /* public void mouseClicked(MouseEvent e)
      * { if(e.isPopupTrigger()) { popupMenu.show((Component)e.getSource(), e.getX(), e.getY()); }}*/
     @Override
     public void mousePressed(final MouseEvent e) {
         if (e.isPopupTrigger()) {
             try {
                 if (e.getSource() instanceof JTree) {
                     final JTree currentTree = (JTree)e.getSource();
                     final TreePath selPath = currentTree.getPathForLocation(e.getX(), e.getY());
                     if (selPath != null) {
                         currentTree.setSelectionPath(selPath);
                     }
                 }
             } catch (Exception ex) {
                 log.error("Error during on-the-fly-selection", ex);
             }
 
             popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
         }
     }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
        }
    }
 }
