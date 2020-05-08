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
 package de.cismet.cids.custom.objecteditors.utils;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.text.JTextComponent;
 
 import de.cismet.cids.editors.DefaultBindableDateChooser;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class RendererTools {
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  comp  DOCUMENT ME!
      */
     public static void makeReadOnly(final JComponent comp) {
         if (comp instanceof JTextComponent) {
             final JTextComponent tComp = (JTextComponent)comp;
             tComp.setEditable(false);
             tComp.setOpaque(false);
             tComp.setBorder(null);
         } else if (comp instanceof JScrollPane) {
             final JScrollPane jsp = (JScrollPane)comp;
             jsp.setOpaque(false);
             jsp.getViewport().setOpaque(false);
         } else if (comp instanceof JComboBox) {
             final JComboBox cb = (JComboBox)comp;
             cb.setEnabled(false);
             cb.setRenderer(new CustomListCellRenderer());
         } else if (comp instanceof DefaultBindableDateChooser) {
             final DefaultBindableDateChooser dc = (DefaultBindableDateChooser)comp;
             dc.setEnabled(false);
         } else if (comp instanceof JCheckBox) {
             ((JCheckBox)comp).setEnabled(false);
        } else if (comp != null) {
             comp.setEnabled(false);
         }
     }
 }
