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
 package de.cismet.verdis.gui;
 
 import Sirius.server.middleware.types.HistoryObject;
 
import org.apache.log4j.Logger;

 import java.awt.Component;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JList;
import javax.swing.ListCellRenderer;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class HistoryComboBox extends JComboBox {
 
    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(HistoryComboBox.class);

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
class HistoryComboBoxRenderer extends DefaultListCellRenderer implements ListCellRenderer {
 
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
