 /**
  *
  * Copyright 2010 Laurent Desgrange
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package net.desgrange.pwad.ui;
 
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ResourceBundle;
 
 import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class OpenAlbumDialog extends JDialog {
     private static final long serialVersionUID = -1066338781417528922L;
    private final Logger logger = LoggerFactory.getLogger(getClass());
     private String link;
 
     public OpenAlbumDialog(final Frame parent) {
         super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
         getRootPane().putClientProperty("apple.awt.documentModalSheet", "true");
         initComponents();
         setLocationRelativeTo(parent);
     }
 
     public String getLink() {
         return link;
     }
 
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         messageLabel = new JLabel();
         invitationLinkLabel = new JLabel();
         invitationLinkField = new JTextField();
         cancelButton = new JButton();
         okButton = new JButton();
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         final ResourceBundle bundle = ResourceBundle.getBundle("pwad/l10n/OpenAlbumDialog"); // NOI18N
         setTitle(bundle.getString("OpenAlbumDialog.title")); // NOI18N
         setMinimumSize(new Dimension(474, 97));
 
         messageLabel.setText(bundle.getString("OpenAlbumDialog.messageLabel.text")); // NOI18N
         messageLabel.setName("messageLabel"); // NOI18N
 
         invitationLinkLabel.setLabelFor(invitationLinkField);
         invitationLinkLabel.setText(bundle.getString("OpenAlbumDialog.invitationLinkLabel.text")); // NOI18N
         invitationLinkLabel.setName("invitationLinkLabel"); // NOI18N
 
         invitationLinkField.setText(bundle.getString("OpenAlbumDialog.invitationLinkField.text")); // NOI18N
         invitationLinkField.setName("invitationLinkField"); // NOI18N
 
         cancelButton.setText(bundle.getString("OpenAlbumDialog.cancelButton.text")); // NOI18N
         cancelButton.setName("cancelButton"); // NOI18N
         cancelButton.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
 
         okButton.setText(bundle.getString("OpenAlbumDialog.okButton.text")); // NOI18N
         okButton.setName("okButton"); // NOI18N
         okButton.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent evt) {
                 okButtonActionPerformed(evt);
             }
         });
 
         final GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(
                 layout.createSequentialGroup().addContainerGap().addGroup(
                         layout.createParallelGroup(Alignment.LEADING).addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE).addGroup(
                                 layout.createSequentialGroup().addComponent(invitationLinkLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(invitationLinkField, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)).addGroup(Alignment.TRAILING,
                                 layout.createSequentialGroup().addComponent(okButton).addPreferredGap(ComponentPlacement.RELATED).addComponent(cancelButton))).addContainerGap()));
         layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(
                 layout.createSequentialGroup().addContainerGap().addComponent(messageLabel).addPreferredGap(ComponentPlacement.RELATED).addGroup(
                         layout.createParallelGroup(Alignment.BASELINE).addComponent(invitationLinkLabel).addComponent(invitationLinkField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(
                         layout.createParallelGroup(Alignment.BASELINE).addComponent(cancelButton).addComponent(okButton)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void cancelButtonActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
        logger.trace("{}", evt);
         dispose();
     }// GEN-LAST:event_cancelButtonActionPerformed
 
     private void okButtonActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
        logger.trace("{}", evt);
         link = invitationLinkField.getText();
         dispose();
     }// GEN-LAST:event_okButtonActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JButton cancelButton;
     private JTextField invitationLinkField;
     private JLabel invitationLinkLabel;
     private JLabel messageLabel;
     private JButton okButton;
     // End of variables declaration//GEN-END:variables
 }
