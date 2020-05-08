 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.profiles;
 
 import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;
 import com.dmdirc.addons.ui_swing.components.validating.ValidatableReorderableJList;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
 import com.dmdirc.ui.IconManager;
 
 import java.awt.Dialog.ModalityType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Links the UI code with the model and controller.
  */
 public class ProfileManagerDialogLinker {
 
     /** Dialog controller. */
     private final ProfileManagerController controller;
     /** Dialog model. */
     private final ProfileManagerModel model;
     /** Dialog. */
     private final ProfileManagerDialog dialog;
     /** The icon manager to use for validating text fields. */
     private final IconManager iconManager;
 
     public ProfileManagerDialogLinker(
             final ProfileManagerController controller,
             final ProfileManagerModel model,
             final ProfileManagerDialog dialog,
             final IconManager iconManager) {
         this.controller = controller;
         this.model = model;
         this.dialog = dialog;
         this.iconManager = iconManager;
     }
 
     /**
      * Binds the profile list to the model.
      *
      * @param list List to bind
      */
     public void bindProfileList(final JList<Profile> list) {
         list.setModel(new DefaultListModel<Profile>());
         list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 model.setSelectedProfile(list.getSelectedValue());
             }
         });
         model.addPropertyChangeListener("profiles", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 ((DefaultListModel) list.getModel()).clear();
                 for (Profile profile : model.getProfiles()) {
                     ((DefaultListModel<Profile>) list.getModel()).addElement(profile);
                 }
                 if (list.getSelectedValue() != model.getSelectedProfile()) {
                     list.setSelectedValue(model.getSelectedProfile(), true);
                 }
             }
         });
         model.addPropertyChangeListener("name", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 list.repaint();
             }
         });
         for (Profile profile : model.getProfiles()) {
             ((DefaultListModel<Profile>) list.getModel()).addElement(profile);
         }
     }
 
     /**
      * Binds the nicknames list to the model.
      *
      * @param nicknames list to bind
      */
     public void bindProfileNicknames(final ValidatableReorderableJList<String> nicknames) {
         nicknames.setModel(new DefaultListModel<String>());
         nicknames.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         nicknames.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 model.setSelectedNickname(nicknames.getSelectedValue());
             }
         });
         model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 nicknames.getModel().clear();
                 for (String nickname : model.getNicknames()) {
                     nicknames.getModel().addElement(nickname);
                 }
                 nicknames.setSelectedValue(model.getSelectedNickname(), true);
                 nicknames.setEnabled(model.isManipulateProfileAllowed());
                 nicknames.setValidation(model.isNicknamesValid());
             }
         });
         model.addPropertyChangeListener("profiles", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 nicknames.setEnabled(model.isManipulateProfileAllowed());
             }
         });
         model.addPropertyChangeListener("nicknames", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 nicknames.getModel().clear();
                 for (String nickname : model.getNicknames()) {
                     nicknames.getModel().addElement(nickname);
                 }
                 nicknames.setSelectedValue(model.getSelectedNickname(), true);
                 nicknames.setEnabled(model.isManipulateProfileAllowed());
                 nicknames.setValidation(model.isNicknamesValid());
             }
         });
         nicknames.setEnabled(model.isManipulateProfileAllowed());
     }
 
     /**
      * Binds the add nickname button to the controller.
      *
      * @param addNickname Button to bind
      */
     public void bindAddNickname(final JButton addNickname) {
         addNickname.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 new StandardInputDialog(dialog, ModalityType.DOCUMENT_MODAL, iconManager,
                         "Add nickname", "Enter nickname to add:", new AddNicknameValidator(model)) {
                             private static final long serialVersionUID = 1L;
 
                             @Override
                             public boolean save() {
                                 controller.addNickname(getText());
                                 return true;
                             }
 
                             @Override
                             public void cancelled() {
                             }
                         }.display();
             }
         });
         model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 addNickname.setEnabled(model.isManipulateProfileAllowed());
             }
         });
         model.addPropertyChangeListener("profiles", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 addNickname.setEnabled(model.isManipulateProfileAllowed());
             }
         });
         addNickname.setEnabled(model.isManipulateProfileAllowed());
     }
 
     /**
      * Binds the edit nickname button to the controller.
      *
      * @param editNickname Button to bind
      */
     public void bindEditNickname(final JButton editNickname) {
         editNickname.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 final StandardInputDialog inputDialog = new StandardInputDialog(dialog,
                         ModalityType.DOCUMENT_MODAL, iconManager,
                         "Add nickname", "Enter edited nickname:",
                         new EditNicknameValidator(model)) {
                             private static final long serialVersionUID = 1L;
 
                             @Override
                             public boolean save() {
                                 controller.editNickname(getText());
                                 return true;
                             }
 
                             @Override
                             public void cancelled() {
                             }
                         };
                 inputDialog.setText((String) model.getSelectedNickname());
                 inputDialog.display();
             }
         });
         model.addPropertyChangeListener("selectednickname", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 editNickname.setEnabled(model.isManipulateNicknameAllowed());
             }
         });
         editNickname.setEnabled(model.isManipulateNicknameAllowed());
     }
 
     /**
      * Binds the delete nickname button to the controller.
      *
      * @param deleteNickname Button to bind
      */
     public void bindDeleteNickname(final JButton deleteNickname) {
         deleteNickname.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 new StandardQuestionDialog(dialog, ModalityType.DOCUMENT_MODAL,
                         "Delete nickname?", "Are you sure you want to delete this nickname?") {
                             private static final long serialVersionUID = 1L;
 
                             @Override
                             public boolean save() {
                                 controller.deleteNickname();
                                 return true;
                             }
 
                             @Override
                             public void cancelled() {
                             }
                         }.display();
             }
         });
         model.addPropertyChangeListener("selectednickname", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 deleteNickname.setEnabled(model.isManipulateNicknameAllowed());
             }
         });
         deleteNickname.setEnabled(model.isManipulateNicknameAllowed());
     }
 
     /**
      * Binds the profile name to the model.
      *
      * @param profileName Text field to bind
      */
     public void bindProfileName(final ValidatableJTextField profileName) {
         profileName.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(final DocumentEvent e) {
                 model.setName(profileName.getText());
                 profileName.setEnabled(model.isManipulateProfileAllowed());
                 profileName.setValidation(model.isNameValid());
             }
 
             @Override
             public void removeUpdate(final DocumentEvent e) {
                 model.setName(profileName.getText());
                 profileName.setEnabled(model.isManipulateProfileAllowed());
                 profileName.setValidation(model.isNameValid());
             }
 
             @Override
             public void changedUpdate(final DocumentEvent e) {
                 if (!model.isNameValid().isFailure()) {
                     model.setName(profileName.getText());
                 }
                 profileName.setEnabled(model.isManipulateProfileAllowed());
                 profileName.setValidation(model.isNameValid());
             }
         });
         model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 profileName.setText(model.getName());
                 profileName.setEnabled(model.isManipulateProfileAllowed());
                 profileName.setValidation(model.isNameValid());
             }
         });
        model.addPropertyChangeListener("profiles", new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                profileName.setText(model.getName());
                profileName.setEnabled(model.isManipulateProfileAllowed());
                profileName.setValidation(model.isNameValid());
            }
        });
         profileName.setEnabled(model.isManipulateProfileAllowed());
     }
 
     /**
      * Binds the profile realname to the model.
      *
      * @param profileRealname Textfield to bind
      */
     public void bindProfileRealnames(final ValidatableJTextField profileRealname) {
         profileRealname.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(final DocumentEvent e) {
                 model.setRealname(profileRealname.getText());
                 profileRealname.setEnabled(model.isManipulateProfileAllowed());
                 profileRealname.setValidation(model.isRealnameValid());
             }
 
             @Override
             public void removeUpdate(final DocumentEvent e) {
                 model.setRealname(profileRealname.getText());
                 profileRealname.setEnabled(model.isManipulateProfileAllowed());
                 profileRealname.setValidation(model.isRealnameValid());
             }
 
             @Override
             public void changedUpdate(final DocumentEvent e) {
                 model.setRealname(profileRealname.getText());
                 profileRealname.setEnabled(model.isManipulateProfileAllowed());
                 profileRealname.setValidation(model.isRealnameValid());
             }
         });
         model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 profileRealname.setText(model.getRealname());
                 profileRealname.setEnabled(model.isManipulateProfileAllowed());
                 profileRealname.setValidation(model.isRealnameValid());
             }
         });
         profileRealname.setEnabled(model.isManipulateProfileAllowed());
     }
 
     /**
      * Binds the profile ident to the model.
      *
      * @param profileIdent Textfield to bind
      */
     public void bindProfileIdent(final ValidatableJTextField profileIdent) {
         profileIdent.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(final DocumentEvent e) {
                 model.setIdent(profileIdent.getText());
                 profileIdent.setEnabled(model.isManipulateProfileAllowed());
                 profileIdent.setValidation(model.isIdentValid());
             }
 
             @Override
             public void removeUpdate(final DocumentEvent e) {
                 model.setIdent(profileIdent.getText());
                 profileIdent.setEnabled(model.isManipulateProfileAllowed());
                 profileIdent.setValidation(model.isIdentValid());
             }
 
             @Override
             public void changedUpdate(final DocumentEvent e) {
                 model.setIdent(profileIdent.getText());
                 profileIdent.setEnabled(model.isManipulateProfileAllowed());
                 profileIdent.setValidation(model.isIdentValid());
             }
         });
         model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 profileIdent.setEnabled(model.isManipulateProfileAllowed());
                 profileIdent.setValidation(model.isIdentValid());
             }
         });
         model.addPropertyChangeListener("profiles", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 profileIdent.setEnabled(model.isManipulateProfileAllowed());
                 profileIdent.setValidation(model.isIdentValid());
             }
         });
         profileIdent.setEnabled(model.isManipulateProfileAllowed());
     }
 
     /**
      * Binds the add profile button to the controller.
      *
      * @param addProfile Button to bind
      */
     public void bindAddProfile(final JButton addProfile) {
         addProfile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 final StandardInputDialog inputDialog = new StandardInputDialog(dialog,
                         ModalityType.DOCUMENT_MODAL, iconManager,
                         "Add profile", "New profile name:",
                         new ProfileNameValidator(model.getProfiles())) {
                             private static final long serialVersionUID = 1L;
 
                             @Override
                             public boolean save() {
                                 controller.addProfile(getText());
                                 return true;
                             }
 
                             @Override
                             public void cancelled() {
                             }
                         };
                 inputDialog.setText((String) model.getSelectedNickname());
                 inputDialog.display();
             }
         });
     }
 
     /**
      * Binds the delete profile button to the controller.
      *
      * @param deleteProfile Button to bind
      */
     public void bindDeleteProfile(final JButton deleteProfile) {
         deleteProfile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 new StandardQuestionDialog(dialog, ModalityType.DOCUMENT_MODAL,
                         "Delete profile?", "Are you sure you want to delete this profile?") {
                             private static final long serialVersionUID = 1L;
 
                             @Override
                             public boolean save() {
                                 controller.deleteProfile();
                                 return true;
                             }
 
                             @Override
                             public void cancelled() {
                             }
                         }.display();
             }
         });
         model.addPropertyChangeListener("profiles", new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 deleteProfile.setEnabled(model.isManipulateProfileAllowed());
             }
         });
         deleteProfile.setEnabled(model.isManipulateProfileAllowed());
     }
 
     /**
      * Binds the OK button to the controller.
      *
      * @param okButton Button to bind
      */
     public void bindOKButton(final JButton okButton) {
         okButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 controller.saveAndCloseDialog();
             }
         });
         model.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 okButton.setEnabled(model.isOKAllowed());
             }
         });
         okButton.setEnabled(model.isOKAllowed());
     }
 
     /**
      * Binds the cancel button to the controller.
      *
      * @param cancelButton Button to bind
      */
     public void bindCancelButton(final JButton cancelButton) {
         cancelButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 controller.closeDialog();
             }
         });
     }
 
 }
