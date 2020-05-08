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
 package de.cismet.watergis.gui.actions.selection;
 
 import org.apache.log4j.Logger;
 
 import java.awt.event.ActionEvent;
 
import java.util.List;

 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToFeaturesWorker;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.watergis.broker.AppBroker;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Gilles Baatz
  * @version  $Revision$, $Date$
  */
 public class ZoomSelectedObjectsAction extends AbstractAction {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(ZoomSelectedObjectsAction.class);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CloseAction object.
      */
     public ZoomSelectedObjectsAction() {
         final String tooltip = org.openide.util.NbBundle.getMessage(
                 ZoomSelectedObjectsAction.class,
                 "ZoomSelectedObjectsAction.toolTipText");
         putValue(SHORT_DESCRIPTION, tooltip);
         final String text = org.openide.util.NbBundle.getMessage(
                 ZoomSelectedObjectsAction.class,
                 "ZoomSelectedObjectsAction.text");
         putValue(NAME, text);
         final String mnemonic = org.openide.util.NbBundle.getMessage(
                 ZoomSelectedObjectsAction.class,
                 "ZoomSelectedObjectsAction.mnemonic");
         putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
         final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                     "/de/cismet/watergis/res/icons16/icon-resize.png"));
         putValue(SMALL_ICON, icon);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void actionPerformed(final ActionEvent e) {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> sel = sl.getAllSelectedPFeatures();
        final Feature[] features = new Feature[sel.size()];

        for (int i = 0; i < sel.size(); ++i) {
            features[i] = sel.get(i).getFeature();
        }

        final ZoomToFeaturesWorker worker = new ZoomToFeaturesWorker(features);
        worker.execute();
     }
 
     @Override
     public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
     }
 }
