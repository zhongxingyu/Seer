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
 
 package com.dmdirc.addons.ui_swing.dialogs.newaliases;
 
 import com.dmdirc.addons.ui_swing.components.GenericTableModel;
 import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.commandparser.aliases.Alias;
 import com.dmdirc.commandparser.validators.CommandNameValidator;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.util.validators.FileNameValidator;
 import com.dmdirc.util.validators.ValidationResponse;
 import com.dmdirc.util.validators.ValidatorChain;
 
 import com.google.common.base.Optional;
 
 import java.awt.Dialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.JButton;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Links the Alias Manager Dialog with its controller and model.
  */
 public class AliasManagerLinker {
 
     private final AliasManagerController controller;
     private final AliasManagerModel model;
     private final AliasManagerDialog dialog;
     private final IconManager iconManager;
     private final CommandController commandController;
 
     public AliasManagerLinker(final AliasManagerController controller,
             final AliasManagerModel model,
             final AliasManagerDialog dialog,
             final IconManager iconManager,
             final CommandController commandController) {
         this.controller = controller;
         this.model = model;
         this.dialog = dialog;
         this.iconManager = iconManager;
         this.commandController = commandController;
     }
 
     public void bindCommandList(final JTable commandList) {
         final GenericTableModel<Alias> commandModel = new GenericTableModel<>(
                 Alias.class, "getName", "getMinArguments", "getSubstitution");
         commandModel.setHeaderNames("Name", "Minimum Arguments", "Substitution");
         commandList.setModel(commandModel);
         commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         commandList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 final int index = commandList.getSelectedRow();
                 if (index == -1) {
                     model.setSelectedAlias(Optional.<Alias>absent());
                 } else {
                     model.setSelectedAlias(Optional.fromNullable(commandModel.getValue(index)));
                 }
             }
         });
         model.addPropertyChangeListener("aliases", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 for (Alias alias : model.getAliases()) {
                     commandModel.addValue(alias);
                 }
             }
         });
         model.addPropertyChangeListener("editAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 if (evt.getNewValue() != null) {
                     final Alias oldAlias = (Alias) evt.getOldValue();
                     final Alias newAlias = (Alias) evt.getNewValue();
                     final int oldIndex = commandModel.getIndex(oldAlias);
                     commandModel.replaceValueAt(newAlias, oldIndex);
                 }
             }
         });
         model.addPropertyChangeListener("renameAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 if (evt.getNewValue() != null) {
                     final Alias oldAlias = (Alias) evt.getOldValue();
                     final Alias newAlias = (Alias) evt.getNewValue();
                     final int oldIndex = commandModel.getIndex(oldAlias);
                     commandModel.replaceValueAt(newAlias, oldIndex);
                 }
             }
         });
         model.addPropertyChangeListener("deleteAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 if (evt.getOldValue() != null) {
                     commandModel.removeValue((Alias) evt.getOldValue());
                 }
             }
         });
         model.addPropertyChangeListener("addAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 if (evt.getNewValue() != null) {
                     commandModel.addValue((Alias) evt.getNewValue());
                 }
             }
         });
     }
 
     public void bindCommand(final ValidatableJTextField command) {
         command.setEnabled(false);
 
         model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 final Optional<Alias> selectedAlias = model.getSelectedAlias();
                 command.setEnabled(selectedAlias.isPresent());
                 if (selectedAlias.isPresent()) {
                     command.setText(selectedAlias.get().getName());
                 } else {
                     command.setText("");
                    command.setValidation(new ValidationResponse());
                 }
             }
         });
         command.getDocument().addDocumentListener(new DocumentListener() {
 
             private void update() {
                 final ValidatorChain<String> chain = ValidatorChain.<String>builder()
                         .addValidator(new CommandNameValidator(commandController.getCommandChar()))
                         .addValidator(new FileNameValidator())
                        .addValidator(new AliasNameValidator(model, model.getSelectedAlias()))
                         .build();
                 final ValidationResponse validation = chain.validate(command.getText());
                 command.setValidation(validation);
                 if (validation.isFailure()) {
                     return;
                 }
                 if (command.isEnabled() && model.getSelectedAlias().isPresent()) {
                     final String oldName = model.getSelectedAlias().get().getName();
                     if (!command.getText().equals(oldName)) {
                         model.renameAlias(oldName, command.getText());
                     }
                 }
             }
 
             @Override
             public void insertUpdate(final DocumentEvent e) {
                 update();
             }
 
             @Override
             public void removeUpdate(final DocumentEvent e) {
                 update();
             }
 
             @Override
             public void changedUpdate(final DocumentEvent e) {
                 update();
             }
         });
     }
 
     public void bindArgumentsNumber(final JSpinner argumentsNumber) {
         argumentsNumber.setEnabled(false);
         argumentsNumber.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
         model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 final Optional<Alias> selectedAlias = model.getSelectedAlias();
                 argumentsNumber.setEnabled(selectedAlias.isPresent());
                 if (selectedAlias.isPresent()) {
                     argumentsNumber.setValue(model.getSelectedAlias().get().getMinArguments());
                 } else {
                     argumentsNumber.setValue(0);
                 }
             }
         });
         argumentsNumber.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(final ChangeEvent e) {
                 if (argumentsNumber.isEnabled() && model.getSelectedAlias().isPresent()) {
                     final String name = model.getSelectedAlias().get().getName();
                     model.editAlias(name, (Integer) argumentsNumber.getValue(),
                             model.getSelectedAlias().get().getSubstitution());
                 }
             }
         });
     }
 
     public void bindResponse(final JTextArea response) {
         response.setEnabled(false);
         model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 final Optional<Alias> selectedAlias = model.getSelectedAlias();
                 response.setEnabled(selectedAlias.isPresent());
                 if (selectedAlias.isPresent()) {
                     response.setText(model.getSelectedAlias().get().getSubstitution());
                 } else {
                     response.setText("");
                 }
             }
         });
         response.getDocument().addDocumentListener(new DocumentListener() {
 
             private void update() {
                 if (response.isEnabled() && model.getSelectedAlias().isPresent()) {
                     final String name = model.getSelectedAlias().get().getName();
                     final int minArgs = model.getSelectedAlias().get().getMinArguments();
                     model.editAlias(name, minArgs, response.getText());
                 }
             }
 
             @Override
             public void insertUpdate(final DocumentEvent e) {
                 update();
             }
 
             @Override
             public void removeUpdate(final DocumentEvent e) {
                 update();
             }
 
             @Override
             public void changedUpdate(final DocumentEvent e) {
                 update();
             }
         });
     }
 
     public void bindAddAlias(final JButton addAlias) {
         addAlias.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 final ValidatorChain<String> chain = ValidatorChain.<String>builder()
                         .addValidator(new CommandNameValidator(commandController.getCommandChar()))
                         .addValidator(new FileNameValidator())
                         .addValidator(new UniqueAliasNameValidator(model))
                         .build();
                 new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                         "Add Alias", "Enter the alias name", chain) {
 
                     @Override
                     public boolean save() {
                         model.addAlias(getText(), 0, "");
                         return true;
                             }
 
                             @Override
                             public void cancelled() {
                             }
                         }.display();
             }
         });
     }
 
     public void bindDeleteAlias(final JButton deleteAlias) {
         deleteAlias.setEnabled(false);
         model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 final Optional<Alias> alias = model.getSelectedAlias();
                 deleteAlias.setEnabled(alias.isPresent());
             }
         });
         deleteAlias.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 final Optional<Alias> alias = model.getSelectedAlias();
                 if (alias.isPresent()) {
                     model.removeAlias(alias.get().getName());
                 }
             }
         });
     }
 
     public void bindOKButton(final JButton okButton) {
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 controller.saveAndCloseDialog();
             }
         });
     }
 
     public void bindCancelButton(final JButton cancelButton) {
         cancelButton.setEnabled(true);
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 controller.discardAndCloseDialog();
             }
         });
     }
 
 }
