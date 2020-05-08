 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.dialogs.profile;
 
 import com.dmdirc.addons.ui_swing.components.ConsumerDocumentListener;
 import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
 import com.dmdirc.addons.ui_swing.components.vetoable.VetoableListSelectionModel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.interfaces.ui.ProfilesDialogModel;
 import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.core.profiles.MutableProfile;
 
 import com.google.common.collect.Lists;
 
 import java.awt.Dialog;
 import java.beans.PropertyVetoException;
 import java.util.Optional;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 public class ProfileManagerController implements ProfilesDialogModelListener {
 
     private final ProfileManagerDialog dialog;
     private final ProfilesDialogModel model;
     private final IconManager iconManager;
     private final DefaultListModel<MutableProfile> listModel;
     private final VetoableListSelectionModel selectionModel;
 
     public ProfileManagerController(final ProfileManagerDialog dialog,
             final ProfilesDialogModel model, final IconManager iconManager) {
         this.dialog = dialog;
         this.model = model;
         this.iconManager = iconManager;
 
         listModel = (DefaultListModel<MutableProfile>) dialog.getProfileList().getModel();
         selectionModel = new VetoableListSelectionModel();
     }
 
     public void init() {
         model.loadModel();
         setupOKButton(dialog.getOkButton());
         setupCancelButton(dialog.getCancelButton());
         setupProfileList(dialog.getProfileList());
         setupAddProfile(dialog.getAddProfile());
         setupDeleteProfile(dialog.getDeleteProfile());
         setupEditNickname(dialog.getProfileEditNickname());
         setupAddNickname(dialog.getProfileAddNickname());
         setupDeleteNickname(dialog.getProfileDeleteNickname());
         setupProfileName(dialog.getProfileName());
         setupProfileNicknames(dialog.getProfileNicknames());
         setupProfileRealname(dialog.getProfileRealname());
         setupProfileIdent(dialog.getProfileIdent());
         setupProfileHighlights(dialog.getProfileHighlights());
         setupAddHighlight(dialog.getProfileAddHighlight());
         setupEditHighlight(dialog.getProfileEditHighlight());
         setupDeleteHighlight(dialog.getProfileDeleteHighlight());
         model.addListener(this);
     }
 
     private void setupOKButton(final JButton okButton) {
         okButton.setEnabled(model.isSaveAllowed());
         okButton.addActionListener(l -> {
             model.save();
             dialog.dispose();
         });
     }
 
     private void setupCancelButton(final JButton cancelButton) {
         cancelButton.addActionListener(l -> dialog.dispose());
     }
 
     private void setupProfileList(final JList<MutableProfile> profileList) {
         profileList.setSelectionModel(selectionModel);
         model.getProfileList().forEach(listModel::addElement);
         selectionModel.addVetoableSelectionListener(e -> {
             if (!model.canSwitchProfiles()) {
                 throw new PropertyVetoException("Cannot switch with invalid profile", e);
             }
         });
         profileList.addListSelectionListener(l -> {
             if (!selectionModel.isSelectionEmpty()) {
                 model.setSelectedProfile(Optional.ofNullable(profileList.getSelectedValue()));
             }
         });
         if (model.getSelectedProfileNicknames().isPresent()) {
             model.getSelectedProfileNicknames().get().forEach(
                     p -> dialog.getProfileNicknames().getModel().addElement(p));
         }
         if (!listModel.isEmpty()) {
             selectionModel.setLeadSelectionIndex(0);
         }
     }
 
     private void setupAddProfile(final JButton addProfile) {
         addProfile.addActionListener(
                 e -> new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL,
                         iconManager, "Profile Manager: Add Profile", "Enter the new profile's name",
                         model.getNewProfileNameValidator(),
                         (String s) -> model.addProfile(s, s, s, Lists.newArrayList(s))).display());
     }
 
     private void setupDeleteProfile(final JButton deleteProfile) {
         deleteProfile.setEnabled(model.getSelectedProfile().isPresent());
         deleteProfile.addActionListener(l -> model.getSelectedProfile()
                 .ifPresent(p -> model.removeProfile(p.getName())));
     }
 
     private void setupEditNickname(final JButton editNickname) {
         editNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
         editNickname.addActionListener(l -> model.getSelectedProfileSelectedNickname().ifPresent(
                 (String oldName) -> new StandardInputDialog(dialog,
                         Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                         "Profile Manager: Edit Nickname", "Enter new nickname",
                         (String newName) -> model.editSelectedProfileNickname(oldName, newName))
                         .display()));
     }
 
     private void setupAddNickname(final JButton addNickname) {
         addNickname.setEnabled(!model.getProfileList().isEmpty());
         addNickname.addActionListener(
                 e -> new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL,
                         iconManager, "Profile Manager: Add Nickname", "Enter nickname to add",
                         model::addSelectedProfileNickname).display());
     }
 
     private void setupDeleteNickname(final JButton deleteNickname) {
         deleteNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
         deleteNickname.addActionListener(l -> model.getSelectedProfileSelectedNickname()
                 .ifPresent(model::removeSelectedProfileNickname));
     }
 
     private void setupAddHighlight(final JButton addHighlight) {
         addHighlight.setEnabled(!model.getProfileList().isEmpty());
         addHighlight.addActionListener(e ->
                 new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL,
                         iconManager, "Profile Manager: Add Highlight", "Enter highlight to add",
                         model.getSelectedProfileAddHighlightValidator(),
                         model::addSelectedProfileHighlight).display());
     }
 
     private void setupEditHighlight(final JButton editHighlight) {
         editHighlight.setEnabled(model.getSelectedProfileSelectedHighlight().isPresent());
         editHighlight.addActionListener(l -> model.getSelectedProfileSelectedHighlight().ifPresent(
                 (String oldName) -> new StandardInputDialog(dialog,
                         Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                         "Profile Manager: Edit Highlight", "Enter new highlight",
                         model.getSelectedProfileEditHighlightValidator(),
                         (String newName) -> model.editSelectedProfileHighlight(oldName, newName))
                         .display()));
     }
 
     private void setupDeleteHighlight(final JButton deleteHighlight) {
         deleteHighlight.setEnabled(model.getSelectedProfileSelectedHighlight().isPresent());
         deleteHighlight.addActionListener(l -> model.getSelectedProfileSelectedHighlight()
                 .ifPresent(model::removeSelectedProfileHighlight));
     }
 
     private void setupProfileName(final JTextField name) {
         name.setEnabled(model.getSelectedProfileName().isPresent());
         name.setText(model.getSelectedProfileName().orElse(""));
         name.getDocument().addDocumentListener(new ConsumerDocumentListener(s -> {
             if (model.getSelectedProfile().isPresent()) {
                 model.setSelectedProfileName(Optional.of(s));
             }
         }));
     }
 
     private void setupProfileHighlights(final ReorderableJList<String> highlights) {
         highlights.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         highlights.setEnabled(model.getSelectedProfileHighlights().isPresent());
         highlights.addListSelectionListener(l -> model.setSelectedProfileSelectedHighlight(
                 Optional.ofNullable(highlights.getSelectedValue())));
     }
 
     private void setupProfileNicknames(final ReorderableJList<String> nicknames) {
         nicknames.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         nicknames.setEnabled(model.getSelectedProfileNicknames().isPresent());
         nicknames.addListSelectionListener(l -> model.setSelectedProfileSelectedNickname(
                 Optional.ofNullable(nicknames.getSelectedValue())));
     }
 
     private void setupProfileRealname(final JTextField realname) {
         realname.setEnabled(model.getSelectedProfileRealname().isPresent());
         realname.setText(model.getSelectedProfileRealname().orElse(""));
         realname.getDocument().addDocumentListener(new ConsumerDocumentListener(s -> {
             if (model.getSelectedProfile().isPresent()) {
                 model.setSelectedProfileRealname(Optional.of(s));
             }
         }));
     }
 
     private void setupProfileIdent(final JTextField ident) {
         ident.setEnabled(model.getSelectedProfileIdent().isPresent());
         ident.setText(model.getSelectedProfileIdent().orElse(""));
         ident.getDocument().addDocumentListener(new ConsumerDocumentListener(s -> {
             if (model.getSelectedProfile().isPresent()) {
                 model.setSelectedProfileIdent(Optional.of(s));
             }
         }));
     }
 
     @Override
     public void profileAdded(final MutableProfile profile) {
         dialog.getProfileAddNickname().setEnabled(model.isProfileListValid());
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         listModel.addElement(profile);
     }
 
     @Override
     public void profileRemoved(final MutableProfile profile) {
         dialog.getProfileAddNickname().setEnabled(model.isProfileListValid());
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         listModel.removeElement(profile);
     }
 
     @Override
     public void profileEdited(final MutableProfile profile) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
     }
 
     @Override
     public void profileSelectionChanged(final Optional<MutableProfile> profile) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getDeleteProfile().setEnabled(model.getSelectedProfile().isPresent());
         if (profile.isPresent()) {
             final int index = listModel.indexOf(profile.get());
             selectionModel.setLeadSelectionIndex(index);
         } else {
             selectionModel.setLeadSelectionIndex(-1);
         }
         dialog.getProfileName().setEnabled(model.getSelectedProfileIdent().isPresent());
         dialog.getProfileName().setText(model.getSelectedProfileName().orElse(""));
         dialog.getProfileNicknames().setEnabled(model.getSelectedProfileNicknames().isPresent());
         dialog.getProfileNicknames().getModel().clear();
         if (model.getSelectedProfileNicknames().isPresent()) {
             model.getSelectedProfileNicknames().get().forEach(
                     p -> dialog.getProfileNicknames().getModel().addElement(p));
         }
         dialog.getProfileRealname().setEnabled(model.getSelectedProfileRealname().isPresent());
         dialog.getProfileRealname().setText(model.getSelectedProfileRealname().orElse(""));
         dialog.getProfileIdent().setEnabled(model.getSelectedProfile().isPresent());
         dialog.getProfileIdent().setText(model.getSelectedProfileIdent().orElse(""));
     }
 
     @Override
     public void selectedNicknameChanged(final Optional<String> nickname) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileDeleteNickname()
                 .setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
         dialog.getProfileEditNickname()
                 .setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
     }
 
     @Override
     public void selectedProfileNicknameEdited(final String oldNickname, final String newNickname) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileNicknames().getModel().setElementAt(newNickname,
                 dialog.getProfileNicknames().getModel().indexOf(oldNickname));
     }
 
     @Override
     public void selectedProfileNicknameAdded(final String nickname) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileNicknames().getModel().addElement(nickname);
     }
 
     @Override
     public void selectedProfileNicknameRemoved(final String nickname) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileNicknames().getModel().removeElement(nickname);
     }
 
     @Override
     public void selectedHighlightChanged(final Optional<String> highlight) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileDeleteHighlight()
                 .setEnabled(model.getSelectedProfileSelectedHighlight().isPresent());
         dialog.getProfileEditHighlight()
                 .setEnabled(model.getSelectedProfileSelectedHighlight().isPresent());
     }
 
     @Override
     public void selectedProfileHighlightEdited(final String oldHighlight, final String newHighlight) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileHighlights().getModel().setElementAt(newHighlight,
                 dialog.getProfileHighlights().getModel().indexOf(oldHighlight));
     }
 
     @Override
     public void selectedProfileHighlightAdded(final String highlight) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileHighlights().getModel().addElement(highlight);
     }
 
     @Override
     public void selectedProfileHighlightRemoved(final String highlight) {
         dialog.getOkButton().setEnabled(model.isSaveAllowed());
         dialog.getProfileHighlights().getModel().removeElement(highlight);
     }
 
 }
