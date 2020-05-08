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
 import com.dmdirc.addons.ui_swing.components.GenericListModel;
 import com.dmdirc.addons.ui_swing.components.IconManager;
 import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
 import com.dmdirc.addons.ui_swing.components.vetoable.VetoableListSelectionModel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.interfaces.ui.ProfilesDialogModel;
 import com.dmdirc.interfaces.ui.ProfilesDialogModelListener;
 import com.dmdirc.ui.core.profiles.MutableProfile;
 
 import com.google.common.collect.Lists;
 
 import java.awt.Dialog;
 import java.beans.PropertyVetoException;
 import java.util.Optional;
 import java.util.function.Consumer;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 public class ProfileManagerController implements ProfilesDialogModelListener {
 
     private final ProfileManagerDialog dialog;
     private final ProfilesDialogModel model;
     private final IconManager iconManager;
     private VetoableListSelectionModel selectionModel;
     private JButton deleteProfile;
     private JTextField name;
     private JList<String> nicknames;
     private JButton addNickname;
     private JButton editNickname;
     private JButton deleteNickname;
     private JTextField realname;
     private JTextField ident;
     private JList<String> highlights;
     private JButton addHighlight;
     private JButton editHighlight;
     private JButton deleteHighlight;
     private JButton okButton;
     private GenericListModel<MutableProfile> profilesModel;
     private GenericListModel<String> nicknamesModel;
     private GenericListModel<String> highlightsModel;
 
     public ProfileManagerController(final ProfileManagerDialog dialog,
             final ProfilesDialogModel model, final IconManager iconManager) {
         this.dialog = dialog;
         this.model = model;
         this.iconManager = iconManager;
     }
 
     public void init(final JList<MutableProfile> profileList, final JButton addProfile,
             final JButton deleteProfile, final JTextField name,
             final ReorderableJList<String> nicknames, final JButton addNickname,
             final JButton editNickname, final JButton deleteNickname, final JTextField realname,
             final JTextField ident, final ReorderableJList<String> highlights,
             final JButton addHighlight, final JButton editHighlight, final JButton deleteHighlight,
             final JButton okButton, final JButton cancelButton) {
         this.deleteProfile = deleteProfile;
         this.name = name;
         this.nicknames = nicknames;
         this.addNickname = addNickname;
         this.editNickname = editNickname;
         this.deleteNickname = deleteNickname;
         this.realname = realname;
         this.ident = ident;
         this.highlights = highlights;
         this.addHighlight = addHighlight;
         this.editHighlight = editHighlight;
         this.deleteHighlight = deleteHighlight;
         this.okButton = okButton;
         model.loadModel();
         setupOKButton(okButton);
         setupCancelButton(cancelButton);
         setupProfileList(profileList);
         setupAddProfile(addProfile);
         setupDeleteProfile(deleteProfile);
         setupEditNickname(editNickname);
         setupAddNickname(addNickname);
         setupDeleteNickname(deleteNickname);
         setupProfileName(name);
         setupProfileNicknames(nicknames);
         setupProfileRealname(realname);
         setupProfileIdent(ident);
         setupProfileHighlights(highlights);
         setupAddHighlight(addHighlight);
         setupEditHighlight(editHighlight);
         setupDeleteHighlight(deleteHighlight);
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
         selectionModel = new VetoableListSelectionModel();
         profilesModel = (GenericListModel<MutableProfile>) profileList.getModel();
         profilesModel.addAll(model.getProfileList());
         profileList.setSelectionModel(selectionModel);
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
     }
 
     private void setupAddProfile(final JButton addProfile) {
         addProfile.addActionListener(
                 e -> new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL,
                         iconManager, "Profile Manager: Add Profile", "Enter the new profile's name",
                         model.getNewProfileNameValidator(), (Consumer<String>) model::addProfile)
                         .display());
     }
 
     private void setupDeleteProfile(final JButton deleteProfile) {
         deleteProfile.setEnabled(model.getSelectedProfile().isPresent());
         deleteProfile.addActionListener(
                 l -> model.getSelectedProfile().ifPresent(model::removeProfile));
     }
 
     private void setupProfileName(final JTextField name) {
         name.setEnabled(model.getSelectedProfileName().isPresent());
         name.setText(model.getSelectedProfileName().orElse(""));
         name.getDocument().addDocumentListener(
                 new ConsumerDocumentListener(s -> model.setSelectedProfileName(Optional.of(s))));
     }
 
     private void setupProfileNicknames(final ReorderableJList<String> nicknames) {
         nicknamesModel = nicknames.getModel();
         nicknamesModel.addAll(model.getSelectedProfileNicknames().orElse(Lists.newArrayList()));
         nicknames.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         nicknames.setEnabled(model.getSelectedProfileNicknames().isPresent());
         nicknames.addListSelectionListener(l -> model.setSelectedProfileSelectedNickname(
                 Optional.ofNullable(nicknames.getSelectedValue())));
     }
 
     private void setupAddNickname(final JButton addNickname) {
         addNickname.setEnabled(!model.getProfileList().isEmpty());
         addNickname.addActionListener(
                 e -> new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL,
                         iconManager, "Profile Manager: Add Nickname", "Enter nickname to add",
                         model.getSelectedProfileAddNicknameValidator(),
                         model::addSelectedProfileNickname).display());
     }
 
     private void setupEditNickname(final JButton editNickname) {
         editNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
         editNickname.addActionListener(l -> model.getSelectedProfileSelectedNickname().ifPresent(
                 (String oldName) -> new StandardInputDialog(dialog,
                         Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                         "Profile Manager: Edit Nickname", "Enter new nickname",
                         model.getSelectedProfileEditNicknameValidator(),
                         (String newName) -> model.editSelectedProfileNickname(oldName, newName))
                         .display()));
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
 
     private void setupDeleteNickname(final JButton deleteNickname) {
         deleteNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
         deleteNickname.addActionListener(l -> model.getSelectedProfileSelectedNickname()
                 .ifPresent(model::removeSelectedProfileNickname));
     }
 
     private void setupProfileHighlights(final ReorderableJList<String> highlights) {
         highlightsModel = highlights.getModel();
         highlightsModel.addAll(model.getSelectedProfileHighlights().orElse(Lists.newArrayList()));
         highlights.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         highlights.setEnabled(model.getSelectedProfileHighlights().isPresent());
         highlights.addListSelectionListener(l -> model.setSelectedProfileSelectedHighlight(
                 Optional.ofNullable(highlights.getSelectedValue())));
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
 
     @Override
     public void profileAdded(final MutableProfile profile) {
         addNickname.setEnabled(model.isProfileListValid());
         addHighlight.setEnabled(model.isProfileListValid());
         okButton.setEnabled(model.isSaveAllowed());
         profilesModel.add(profile);
     }
 
     @Override
     public void profileRemoved(final MutableProfile profile) {
         addNickname.setEnabled(model.isProfileListValid());
         addHighlight.setEnabled(model.isProfileListValid());
         okButton.setEnabled(model.isSaveAllowed());
         profilesModel.remove(profile);
     }
 
     @Override
     public void profileEdited(final MutableProfile profile) {
         okButton.setEnabled(model.isSaveAllowed());
     }
 
     @Override
     public void profileSelectionChanged(final Optional<MutableProfile> profile) {
         okButton.setEnabled(model.isSaveAllowed());
        deleteProfile.setEnabled(model.getSelectedProfile().isPresent());
         if (profile.isPresent()) {
             final int index = profilesModel.indexOf(profile.get());
             selectionModel.setLeadSelectionIndex(index);
         } else {
             selectionModel.setLeadSelectionIndex(-1);
         }
         name.setEnabled(model.getSelectedProfile().isPresent());
         name.setText(model.getSelectedProfileName().orElse(""));
         nicknames.setEnabled(model.getSelectedProfile().isPresent());
         nicknamesModel.clear();
         nicknamesModel.addAll(model.getSelectedProfileNicknames().orElse(Lists.newArrayList()));
         highlights.setEnabled(model.getSelectedProfile().isPresent());
         highlightsModel.clear();
         highlightsModel.addAll(model.getSelectedProfileHighlights().orElse(Lists.newArrayList()));
         realname.setEnabled(model.getSelectedProfile().isPresent());
         realname.setText(model.getSelectedProfileRealname().orElse(""));
         ident.setEnabled(model.getSelectedProfile().isPresent());
         ident.setText(model.getSelectedProfileIdent().orElse(""));
     }
 
     @Override
     public void selectedNicknameChanged(final Optional<String> nickname) {
         okButton.setEnabled(model.isSaveAllowed());
         deleteNickname.setEnabled(model.isSelectedProfileNicknamesValid());
         editNickname.setEnabled(model.isSelectedProfileNicknamesValid());
     }
 
     @Override
     public void selectedProfileNicknameEdited(final String oldNickname, final String newNickname) {
         okButton.setEnabled(model.isSaveAllowed());
         nicknamesModel.set(nicknamesModel.indexOf(oldNickname), newNickname);
         addNickname.setEnabled(model.isSelectedProfileNicknamesValid());
         editNickname.setEnabled(model.isSelectedProfileNicknamesValid());
     }
 
     @Override
     public void selectedProfileNicknameAdded(final String nickname) {
         okButton.setEnabled(model.isSaveAllowed());
         nicknamesModel.add(nickname);
     }
 
     @Override
     public void selectedProfileNicknameRemoved(final String nickname) {
         okButton.setEnabled(model.isSaveAllowed());
         nicknamesModel.remove(nickname);
     }
 
     @Override
     public void selectedHighlightChanged(final Optional<String> highlight) {
         deleteHighlight.setEnabled(model.isSelectedProfileHighlightsValid());
         editHighlight.setEnabled(model.isSelectedProfileHighlightsValid());
     }
 
     @Override
     public void selectedProfileHighlightEdited(final String oldHighlight, final String newHighlight) {
         okButton.setEnabled(model.isSaveAllowed());
         highlightsModel.set(highlightsModel.indexOf(oldHighlight), newHighlight);
         deleteHighlight.setEnabled(model.isSelectedProfileHighlightsValid());
         editHighlight.setEnabled(model.isSelectedProfileHighlightsValid());
     }
 
     @Override
     public void selectedProfileHighlightAdded(final String highlight) {
         okButton.setEnabled(model.isSaveAllowed());
         highlightsModel.add(highlight);
         deleteHighlight.setEnabled(model.isSelectedProfileHighlightsValid());
         editHighlight.setEnabled(model.isSelectedProfileHighlightsValid());
     }
 
     @Override
     public void selectedProfileHighlightRemoved(final String highlight) {
         okButton.setEnabled(model.isSaveAllowed());
         highlightsModel.remove(highlight);
     }
 
 }
