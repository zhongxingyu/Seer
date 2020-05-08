 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.verdis.gui;
 
 import Sirius.server.middleware.types.HistoryObject;
 
 import java.awt.Component;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class HistoryComboBox extends JComboBox {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new HistoryComboBox object.
      */
     public HistoryComboBox() {
         setRenderer(new HistoryComboBoxRenderer());
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
class HistoryComboBoxRenderer extends DefaultListCellRenderer {
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Component getListCellRendererComponent(final JList list,
             final Object value,
             final int index,
             final boolean isSelected,
             final boolean cellHasFocus) {
         super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         if (value != null) {
             if (value instanceof HistoryObject) {
                 final HistoryObject ho = (HistoryObject)value;
                 setText(ho.getValidFrom().toString()); // NOI18N
             }
         }
         return this;
     }
 }
