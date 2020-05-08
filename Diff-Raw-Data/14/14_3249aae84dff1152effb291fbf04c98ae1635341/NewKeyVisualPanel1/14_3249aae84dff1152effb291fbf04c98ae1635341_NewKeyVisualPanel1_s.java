 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 mscholl
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.abf.domainserver.project.configattr;
 
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 
 import org.openide.ErrorManager;
 import org.openide.util.NbBundle;
 import org.openide.util.WeakListeners;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import de.cismet.cids.abf.utilities.ProgressIndicatingExecutor;
 
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class NewKeyVisualPanel1 extends JPanel {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient ExecutorService backgroundExecutor;
 
     private final transient NewKeyWizardPanel1 model;
     private final transient DocumentListenerImpl docL;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox cboGroup;
     private javax.swing.JLabel lblGroup;
     private javax.swing.JLabel lblKey;
     private javax.swing.JTextField txtKey;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NewKeyVisualPanel1 object.
      *
      * @param  model  DOCUMENT ME!
      */
     public NewKeyVisualPanel1(final NewKeyWizardPanel1 model) {
         initComponents();
 
         final DefaultComboBoxModel<String> cboModel = new DefaultComboBoxModel<String>();
         cboModel.addElement(ConfigAttrGroupNode.NO_GROUP_DISPLAYNAME);
         cboGroup.setModel(cboModel);
         AutoCompleteDecorator.decorate(cboGroup);
 
         this.model = model;
         this.backgroundExecutor = new ProgressIndicatingExecutor(
                 NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.<init>.backgroundExecutor.label"), // NOI18N
                 "new-config-attr-key-wizard-executor", // NOI18N
                 1);
         docL = new DocumentListenerImpl();
         txtKey.getDocument().addDocumentListener(WeakListeners.document(docL, txtKey.getDocument()));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getName() {
         return NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.getName().returnValue"); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     ConfigAttrKey getKey() {
         final ConfigAttrKey key = new ConfigAttrKey();
         key.setKey(txtKey.getText());
 
         final String group = (String)cboGroup.getSelectedItem();
         if (group.isEmpty() || ConfigAttrGroupNode.NO_GROUP_DISPLAYNAME.equals(group)) {
             key.setGroupName(ConfigAttrKey.NO_GROUP);
         } else {
             key.setGroupName(group);
         }
 
         return key;
     }
 
     /**
      * DOCUMENT ME!
      */
     void init() {
         backgroundExecutor.execute(new SwingWorker() {
 
                 @Override
                 protected Object doInBackground() throws Exception {
                     final List<String> groups = model.getProject()
                                 .getCidsDataObjectBackend()
                                 .getConfigAttrGroups(model.getType());
                     Collections.sort(groups, new Comparator<String>() {
 
                             @Override
                             public int compare(final String o1, final String o2) {
                                 if (ConfigAttrKey.NO_GROUP.equals(o1)) {
                                     return -1;
                                 } else if (ConfigAttrKey.NO_GROUP.equals(o2)) {
                                     return 1;
                                 } else {
                                     return o1.compareTo(o2);
                                 }
                             }
                         });
                    groups.remove(0);
                     groups.add(0, ConfigAttrGroupNode.NO_GROUP_DISPLAYNAME);
 
                     return groups;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final List<String> groups = (List)get();
                         final DefaultComboBoxModel<String> cboModel = new DefaultComboBoxModel<String>(
                                 groups.toArray(new String[groups.size()]));
                         cboGroup.setModel(cboModel);
                     } catch (InterruptedException ex) {
                         ErrorManager.getDefault().annotate(ex, "cannot fetch groups"); // NOI18N
                     } catch (ExecutionException ex) {
                         ErrorManager.getDefault().annotate(ex, "cannot fetch groups"); // NOI18N
                     }
                 }
             });
         model.fireChangeEvent();
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         lblKey = new javax.swing.JLabel();
         txtKey = new javax.swing.JTextField();
         lblGroup = new javax.swing.JLabel();
         cboGroup = new javax.swing.JComboBox();
 
         setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblKey,
             NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.lblKey.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(lblKey, gridBagConstraints);
 
         txtKey.setText(NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.txtKey.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(txtKey, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGroup,
             NbBundle.getMessage(NewKeyVisualPanel1.class, "NewKeyVisualPanel1.lblGroup.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(lblGroup, gridBagConstraints);
 
         cboGroup.setEditable(true);
         cboGroup.setModel(new javax.swing.DefaultComboBoxModel(
                 new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(cboGroup, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class DocumentListenerImpl implements DocumentListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void insertUpdate(final DocumentEvent e) {
             model.fireChangeEvent();
         }
 
         @Override
         public void removeUpdate(final DocumentEvent e) {
             model.fireChangeEvent();
         }
 
         @Override
         public void changedUpdate(final DocumentEvent e) {
             model.fireChangeEvent();
         }
     }
 }
