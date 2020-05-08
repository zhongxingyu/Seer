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
 
 package com.dmdirc.addons.ui_swing.dialogs.aliases;
 
 import com.dmdirc.addons.ui_swing.components.GenericTableModel;
 import com.dmdirc.addons.ui_swing.components.vetoable.VetoableListSelectionModel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.commandparser.aliases.Alias;
 import com.dmdirc.ui.IconManager;
 
 import com.google.common.base.Optional;
 
 import java.awt.Dialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 import java.beans.VetoableChangeListener;
 
 import javax.swing.JButton;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
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
 
     public AliasManagerLinker(final AliasManagerController controller,
             final AliasManagerModel model,
             final AliasManagerDialog dialog,
             final IconManager iconManager) {
         this.controller = controller;
         this.model = model;
         this.dialog = dialog;
         this.iconManager = iconManager;
     }
 
     public void bindCommandList(final JTable commandList) {
         final GenericTableModel<Alias> commandModel = new GenericTableModel<>(
                 Alias.class, "getName", "getMinArguments", "getSubstitution");
         commandModel.setHeaderNames("Name", "Minimum Arguments", "Substitution");
         commandList.setModel(commandModel);
         commandList.setSelectionModel(new VetoableListSelectionModel());
         ((VetoableListSelectionModel) commandList.getSelectionModel()).addVetoableSelectionListener(
                 new VetoableChangeListener() {
 
                     @Override
                     public void vetoableChange(final PropertyChangeEvent evt) throws
                             PropertyVetoException {
                         if ((Integer) evt.getNewValue() == -1) {
                             throw new PropertyVetoException("Blank selection not allowed", evt);
                         }
                     }
                 });
         commandList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 final int index = commandList.getSelectedRow();
                 if (index == -1) {
                     model.setSelectedAlias(Optional.<Alias>absent());
                } else if (index >= commandModel.getRowCount()) {
                    model.setSelectedAlias(Optional.fromNullable(commandModel.getValue(index - 1)));
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
                 if (commandModel.getRowCount() > 0) {
                     commandList.getSelectionModel().setLeadSelectionIndex(0);
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
                    final int index = commandList.getSelectedRow();
                    if (index == -1) {
                        model.setSelectedAlias(Optional.<Alias>absent());
                    } else if (index >= commandModel.getRowCount()) {
                        model.setSelectedAlias(Optional.fromNullable(commandModel.
                                getValue(index - 1)));
                        commandList.getSelectionModel().setLeadSelectionIndex(index - 1);
                    } else {
                        model.setSelectedAlias(Optional.fromNullable(commandModel.getValue(index)));
                    }
                 }
             }
         });
         model.addPropertyChangeListener("addAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 if (evt.getNewValue() != null) {
                     final Alias alias = (Alias) evt.getNewValue();
                     commandModel.addValue(alias);
                     commandList.getSelectionModel().setSelectionInterval(
                             commandModel.getIndex(alias), commandModel.getIndex(alias));
                 }
             }
         });
     }
 
     public void bindCommand(final JTextField command) {
         command.setEnabled(false);
 
         model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {
 
             @Override
             public void propertyChange(final PropertyChangeEvent evt) {
                 final Optional<Alias> selectedAlias = model.getSelectedAlias();
                 command.setEnabled(selectedAlias.isPresent());
                 command.setText(model.getName());
             }
         });
         command.getDocument().addDocumentListener(new DocumentListener() {
 
             private void update() {
                 model.setName(command.getText());
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
                 model.setMinimumArguments((Integer) argumentsNumber.getValue());
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
                 model.setSubstitution(response.getText());
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
                 new StandardInputDialog(dialog, Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                         "Add Alias", "Enter the alias name", model.getNewCommandValidator()) {
 
                             private static final long serialVersionUID = 3;
 
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
