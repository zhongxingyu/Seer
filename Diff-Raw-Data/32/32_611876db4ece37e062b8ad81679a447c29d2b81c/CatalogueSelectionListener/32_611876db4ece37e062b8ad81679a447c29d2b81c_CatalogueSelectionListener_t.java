 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.event;
 
import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.DescriptionPane;
 import Sirius.navigator.ui.attributes.AttributeViewer;
import Sirius.navigator.ui.tree.MetaCatalogueTree;
 
 import java.awt.event.ActionEvent;
 
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JTree;
 import javax.swing.Timer;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.*;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class CatalogueSelectionListener implements TreeSelectionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final int SELECTION_CADENCE_TIME = 300;
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private final AttributeViewer attributeViewer;
     private final DescriptionPane descriptionPane;
     private final Timer timer;
     private final SelectionActionListener timerListener;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of CatalogueSelectionListener.
      *
      * @param  attributeViewer  DOCUMENT ME!
      * @param  descriptionPane  DOCUMENT ME!
      */
     public CatalogueSelectionListener(final AttributeViewer attributeViewer, final DescriptionPane descriptionPane) {
         this.attributeViewer = attributeViewer;
         this.descriptionPane = descriptionPane;
         timerListener = new SelectionActionListener();
         timer = new Timer(SELECTION_CADENCE_TIME, timerListener);
         timer.setRepeats(false);
         // class attributes only this.classAttributeIterator = new SingleAttributeIterator(new
         // SimpleAttributeRestriction(AttributeRestriction.CLASS, AttributeRestriction.TRUE,
         // AttributeRestriction.IGNORE, null, null), false); object attributes only this.objectAttributeIterator = new
         // SingleAttributeIterator();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  e  DOCUMENT ME!
      */
     private void performValueChanged(final TreeSelectionEvent e) {
 //        final Runnable r = new Runnable() {
 //
 //            @Override
 //            public void run() {
         final JTree t = (JTree)e.getSource();

        final MetaCatalogueTree catalogue = ComponentRegistry.getRegistry().getCatalogueTree();
        final MetaCatalogueTree searchResults = ComponentRegistry.getRegistry().getSearchResultsTree();

        if (t == catalogue) {
            log.fatal("Suchergebnisse");
            searchResults.removeTreeSelectionListener(this);
            searchResults.clearSelection();
            searchResults.addTreeSelectionListener(this);
        } else {
            log.fatal("Katalog");
            catalogue.removeTreeSelectionListener(this);
            catalogue.clearSelection();
            catalogue.addTreeSelectionListener(this);
        }

         final TreePath[] treePaths = t.getSelectionPaths();
 
         final List<Object> objects = TypeSafeCollections.newArrayList();
         if (treePaths != null) {
             for (int i = 0; i < treePaths.length; i++) {
                 objects.add(treePaths[i].getLastPathComponent());
             }
         }
         CatalogueSelectionListener.this.attributeViewer.setTreeNodes(objects);
         CatalogueSelectionListener.this.descriptionPane.setNodesDescriptions(objects);
 
 //            }
 //        };
 //        ApplicationThreadPool.execute(r);
     }
 
     /**
      * Called whenever the value of the selection changes.
      *
      * @param  e  the event that characterizes the change.
      */
     @Override
     public void valueChanged(final TreeSelectionEvent e) {
         timer.stop();
         descriptionPane.prepareValueChanged();
         timerListener.setEvt(e);
         timer.start();
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class SelectionActionListener extends AbstractAction {
 
         //~ Instance fields ----------------------------------------------------
 
         private TreeSelectionEvent evt;
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (evt != null) {
                 performValueChanged(evt);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  the evt
          */
         public TreeSelectionEvent getEvt() {
             return evt;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  evt  the evt to set
          */
         public void setEvt(final TreeSelectionEvent evt) {
             this.evt = evt;
         }
     }
 }
