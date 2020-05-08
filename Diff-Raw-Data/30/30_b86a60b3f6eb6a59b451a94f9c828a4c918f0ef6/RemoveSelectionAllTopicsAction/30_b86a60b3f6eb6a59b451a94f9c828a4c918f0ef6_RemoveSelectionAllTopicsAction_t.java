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
 
import java.util.ArrayList;
import java.util.List;

 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
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
 public class RemoveSelectionAllTopicsAction extends AbstractAction {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(RemoveSelectionAllTopicsAction.class);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CloseAction object.
      */
     public RemoveSelectionAllTopicsAction() {
         final String tooltip = org.openide.util.NbBundle.getMessage(
                 RemoveSelectionAllTopicsAction.class,
                 "RemoveSelectionAllTopicsAction.toolTipText");
         putValue(SHORT_DESCRIPTION, tooltip);
         final String text = org.openide.util.NbBundle.getMessage(
                 RemoveSelectionAllTopicsAction.class,
                 "RemoveSelectionAllTopicsAction.text");
         putValue(NAME, text);
         final String mnemonic = org.openide.util.NbBundle.getMessage(
                 RemoveSelectionAllTopicsAction.class,
                 "RemoveSelectionAllTopicsAction.mnemonic");
         putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
         final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                     "/de/cismet/watergis/res/icons16/icon-selectionremove.png"));
         putValue(SMALL_ICON, icon);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void actionPerformed(final ActionEvent e) {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> sel = sl.getAllSelectedPFeatures();
        final List<Feature> toBeUnselected = new ArrayList<Feature>();

        for (final PFeature feature : sel) {
            if (feature.isSelected()) {
                feature.setSelected(false);
                sl.removeSelectedFeature(feature);
                toBeUnselected.add(feature.getFeature());
            }
        }
        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection()).unselect(
            toBeUnselected);
     }
 
     @Override
     public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
     }
 }
