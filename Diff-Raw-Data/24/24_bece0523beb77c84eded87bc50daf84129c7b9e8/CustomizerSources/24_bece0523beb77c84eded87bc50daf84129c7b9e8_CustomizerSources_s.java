 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Deve1loper of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 package org.netbeans.modules.javafx.project.ui.customizer;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.nio.charset.IllegalCharsetNameException;
 import java.util.logging.Logger;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JList;
 import javax.swing.UIManager;
 import org.netbeans.api.queries.FileEncodingQuery;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 
 /**
  * Customizer panel "Sources": source roots, level, includes/excludes.
  * @author  Tomas Zezula
  */
 public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider {
     
     
     private String originalEncoding;
     private boolean notified;
 
     private final JavaFXProjectProperties uiProperties;
 
     public CustomizerSources( JavaFXProjectProperties uiProperties ) {
         this.uiProperties = uiProperties;
         initComponents();
         
         jScrollPane1.getViewport().setBackground( sourceRoots.getBackground() );
 //        jScrollPane2.getViewport().setBackground( testRoots.getBackground() );
         
         sourceRoots.setModel( uiProperties.SOURCE_ROOTS_MODEL );
 //        testRoots.setModel( uiProperties.TEST_ROOTS_MODEL );
         sourceRoots.getTableHeader().setReorderingAllowed(false);
 //        testRoots.getTableHeader().setReorderingAllowed(false);
         
         FileObject projectFolder = uiProperties.getProject().getProjectDirectory();
         File pf = FileUtil.toFile( projectFolder );
         this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
         
         
         JavaFXSourceRootsUi.EditMediator emSR = JavaFXSourceRootsUi.registerEditMediator(
             uiProperties.getProject(),
             uiProperties.getProject().getSourceRoots(),
             sourceRoots,
             addSourceRoot,
             removeSourceRoot, 
             upSourceRoot, 
             downSourceRoot);
 /*        
         JavaFXSourceRootsUi.EditMediator emTSR = JavaFXSourceRootsUi.registerEditMediator(
             uiProperties.getProject(),
             uiProperties.getProject().getTestSourceRoots(),
             testRoots,
             addTestRoot,
             removeTestRoot, 
             upTestRoot, 
             downTestRoot);
 */        
 //        emSR.setRelatedEditMediator( emTSR );
 //        emTSR.setRelatedEditMediator( emSR );
         this.originalEncoding = this.uiProperties.getProject().evaluator().getProperty(JavaFXProjectProperties.SOURCE_ENCODING);
         if (this.originalEncoding == null) {
             this.originalEncoding = FileEncodingQuery.getDefaultEncoding().name();
         }
         this.encoding.setModel(new EncodingModel(this.originalEncoding));
         this.encoding.setRenderer(new EncodingRenderer());
         final String lafid = UIManager.getLookAndFeel().getID();
         if (!"Aqua".equals(lafid)) { //NOI18N
             this.encoding.putClientProperty ("JComboBox.isTableCellEditor", Boolean.TRUE);    //NOI18N
             this.encoding.addItemListener(new java.awt.event.ItemListener(){ 
                 public void itemStateChanged(java.awt.event.ItemEvent e){ 
                     javax.swing.JComboBox combo = (javax.swing.JComboBox)e.getSource(); 
                     combo.setPopupVisible(false); 
                 } 
             });
         }
         this.encoding.addActionListener(new ActionListener () {
             public void actionPerformed(ActionEvent arg0) {
                 handleEncodingChange();
             }            
         });
     }
     
     
     private void handleEncodingChange () {
             Charset enc = (Charset) encoding.getSelectedItem();
             String encName;
             if (enc != null) {
                 encName = enc.name();
             }
             else {
                 encName = originalEncoding;
             }
             if (!notified && encName!=null && !encName.equals(originalEncoding)) {
                 DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                         NbBundle.getMessage(CustomizerSources.class,"MSG_EncodingWarning"), NotifyDescriptor.WARNING_MESSAGE)); // NOI18N
                 notified=true;
             }
             this.uiProperties.putAdditionalProperty(JavaFXProjectProperties.SOURCE_ENCODING, encName);
     }
 
 
 
     public HelpCtx getHelpCtx() {
         return new HelpCtx (CustomizerSources.class);
     }
     
     
     private static class EncodingRenderer extends DefaultListCellRenderer {
         
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             assert value instanceof Charset; 
             return super.getListCellRendererComponent(list, ((Charset)value).displayName(), index, isSelected, cellHasFocus);
         }
     }
     
     private static class EncodingModel extends DefaultComboBoxModel {
         
         public EncodingModel (String originalEncoding) {
             Charset defEnc = null;
             for (Charset c : Charset.availableCharsets().values()) {
                 if (c.name().equals(originalEncoding)) {
                     defEnc = c;
                 }
                 addElement(c);
             }
             if (defEnc == null) {
                 //Create artificial Charset to keep the original value
                 //May happen when the project was set up on the platform
                 //which supports more encodings
                 try {
                     defEnc = new UnknownCharset (originalEncoding);
                     addElement(defEnc);
                 } catch (IllegalCharsetNameException e) {
                     //The source.encoding property is completely broken
                     Logger.getLogger(this.getClass().getName()).info("IllegalCharsetName: " + originalEncoding); // NOI18N
                 }
             }
             if (defEnc == null) {
                 defEnc = FileEncodingQuery.getDefaultEncoding();
             }
             setSelectedItem(defEnc);
         }
     }
     
     private static class UnknownCharset extends Charset {
         
         UnknownCharset (String name) {
             super (name, new String[0]);
         }
     
         public boolean contains(Charset c) {
             throw new UnsupportedOperationException();
         }
 
         public CharsetDecoder newDecoder() {
             throw new UnsupportedOperationException();
         }
 
         public CharsetEncoder newEncoder() {
             throw new UnsupportedOperationException();
         }
 }
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jLabel1 = new javax.swing.JLabel();
         projectLocation = new javax.swing.JTextField();
         sourceRootsPanel = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         sourceRoots = new javax.swing.JTable();
         addSourceRoot = new javax.swing.JButton();
         removeSourceRoot = new javax.swing.JButton();
         upSourceRoot = new javax.swing.JButton();
         downSourceRoot = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         encoding = new javax.swing.JComboBox();
 
         setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle").getString("MNE_ProjectFolder").charAt(0));
         jLabel1.setLabelFor(projectLocation);
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle"); // NOI18N
         jLabel1.setText(bundle.getString("CTL_ProjectFolder")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
         add(jLabel1, gridBagConstraints);
 
         projectLocation.setEditable(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         add(projectLocation, gridBagConstraints);
         projectLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_projectLocation")); // NOI18N
 
         sourceRootsPanel.setLayout(new java.awt.GridBagLayout());
 
         jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle").getString("MNE_SourceRoots").charAt(0));
         jLabel2.setLabelFor(sourceRoots);
         jLabel2.setText(bundle.getString("CTL_SourceRoots")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
         sourceRootsPanel.add(jLabel2, gridBagConstraints);
 
         sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null},
                 {null, null},
                 {null, null},
                 {null, null}
             },
             new String [] {
                "Package Folder", "Label" // NOI18N
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Object.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane1.setViewportView(sourceRoots);
         sourceRoots.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_sourceRoots")); // NOI18N
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
         sourceRootsPanel.add(jScrollPane1, gridBagConstraints);
 
         addSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle").getString("MNE_AddSourceRoot").charAt(0));
         addSourceRoot.setText(bundle.getString("CTL_AddSourceRoot")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         sourceRootsPanel.add(addSourceRoot, gridBagConstraints);
         addSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_addSourceRoot")); // NOI18N
 
         removeSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle").getString("MNE_RemoveSourceRoot").charAt(0));
         removeSourceRoot.setText(bundle.getString("CTL_RemoveSourceRoot")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
         sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);
         removeSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_removeSourceRoot")); // NOI18N
 
         upSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle").getString("MNE_UpSourceRoot").charAt(0));
         upSourceRoot.setText(bundle.getString("CTL_UpSourceRoot")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
         sourceRootsPanel.add(upSourceRoot, gridBagConstraints);
         upSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_upSourceRoot")); // NOI18N
 
         downSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/javafx/project/ui/customizer/Bundle").getString("MNE_DownSourceRoot").charAt(0));
         downSourceRoot.setText(bundle.getString("CTL_DownSourceRoot")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
         sourceRootsPanel.add(downSourceRoot, gridBagConstraints);
         downSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_downSourceRoot")); // NOI18N
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 0.45;
         gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
         add(sourceRootsPanel, gridBagConstraints);
 
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         jLabel3.setLabelFor(encoding);
         org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_Encoding")); // NOI18N
        jLabel3.setMaximumSize(new java.awt.Dimension(54, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(54, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(54, 14));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
         gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 12);
         jPanel1.add(jLabel3, gridBagConstraints);
         jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_Encoding")); // NOI18N
 
        encoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" })); // NOI18N
         encoding.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 encodingActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
         jPanel1.add(encoding, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
         add(jPanel1, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     private void encodingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodingActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_encodingActionPerformed
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addSourceRoot;
     private javax.swing.JButton downSourceRoot;
     private javax.swing.JComboBox encoding;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTextField projectLocation;
     private javax.swing.JButton removeSourceRoot;
     private javax.swing.JTable sourceRoots;
     private javax.swing.JPanel sourceRootsPanel;
     private javax.swing.JButton upSourceRoot;
     // End of variables declaration//GEN-END:variables
     
 }
