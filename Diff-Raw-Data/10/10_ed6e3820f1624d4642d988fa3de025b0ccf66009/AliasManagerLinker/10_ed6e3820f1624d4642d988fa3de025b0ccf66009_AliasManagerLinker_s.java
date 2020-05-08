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
 
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.GenericListModel;
 import com.dmdirc.addons.ui_swing.components.renderers.PropertyListCellRenderer;
 import com.dmdirc.addons.ui_swing.components.vetoable.VetoableListSelectionModel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.commandparser.aliases.Alias;
 import com.dmdirc.interfaces.ui.AliasDialogModel;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.core.aliases.AliasDialogModelAdapter;
 
 import com.google.common.base.Optional;
 
 import java.awt.Dialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyVetoException;
 import java.beans.VetoableChangeListener;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
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
 
     private final AliasDialogModel model;
     private final AliasManagerDialog dialog;
     private final IconManager iconManager;
 
     public AliasManagerLinker(
             final AliasDialogModel model,
             final AliasManagerDialog dialog,
             final IconManager iconManager) {
         this.model = model;
         this.dialog = dialog;
         this.iconManager = iconManager;
     }
 
     public void bindCommandList(final JList<Alias> commandList) {
         final GenericListModel<Alias> commandModel = new GenericListModel<>();
         final VetoableListSelectionModel selectionModel = new VetoableListSelectionModel();
         commandList.setCellRenderer(new PropertyListCellRenderer<>(commandList.getCellRenderer(),
                 Alias.class, "name"));
         commandList.setModel(commandModel);
         commandList.setSelectionModel(selectionModel);
         commandList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 if (e.getValueIsAdjusting()) {
                     return;
                 }
                 final int index = commandList.getSelectedIndex();
                 if (index == -1) {
                     model.setSelectedAlias(Optional.<Alias>absent());
                 } else if (commandModel.getSize() == 0) {
                     model.setSelectedAlias(Optional.<Alias>absent());
                 } else if (index >= commandModel.getSize()) {
                     model.setSelectedAlias(Optional.fromNullable(commandModel.
                             getElementAt(index - 1)));
                 } else {
                     model.setSelectedAlias(Optional.fromNullable(commandModel.getElementAt(index)));
                 }
             }
         });
         selectionModel.addVetoableSelectionListener(new VetoableChangeListener() {
 
             @Override
             public void vetoableChange(final PropertyChangeEvent evt) throws PropertyVetoException {
                 if (!model.isChangeAliasAllowed()) {
                     throw new PropertyVetoException("Currently selected alias is invalid.", evt);
                 }
             }
         });
         model.addListener(new AliasDialogModelAdapter() {
 
             @Override
             public void aliasRenamed(final Alias oldAlias, final Alias newAlias) {
                 commandModel.replace(oldAlias, newAlias);
             }
 
             @Override
             public void aliasEdited(final Alias oldAlias, final Alias newAlias) {
                 commandModel.replace(oldAlias, newAlias);
             }
 
             @Override
             public void aliasRemoved(final Alias alias) {
                 commandModel.remove(alias);
             }
 
             @Override
             public void aliasAdded(final Alias alias) {
                 commandModel.add(alias);
                 commandList.getSelectionModel().setSelectionInterval(
                         commandModel.indexOf(alias), commandModel.indexOf(alias));
             }
 
             @Override
             public void aliasSelectionChanged(final Optional<Alias> alias) {
                 final int index;
                 if (alias.isPresent()) {
                     index = commandModel.indexOf(alias.get());
                 } else {
                     index = -1;
                 }
                 if (index != selectionModel.getLeadSelectionIndex()) {
                     selectionModel.setLeadSelectionIndex(index);
                 }
             }
 
         });
     }
 
     public void bindCommand(final JTextField command) {
         command.setEnabled(false);
         command.getDocument().addDocumentListener(new DocumentListener() {
 
             private void update() {
                 model.setSelectedAliasName(command.getText());
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
         model.addListener(new AliasDialogModelAdapter() {
 
             @Override
             public void aliasSelectionChanged(final Optional<Alias> alias) {
                 command.setEnabled(model.isCommandValid());
                 command.setText(model.getSelectedAliasName());
             }
 
         });
     }
 
     public void bindArgumentsNumber(final JSpinner argumentsNumber) {
         argumentsNumber.setEnabled(false);
         argumentsNumber.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
         argumentsNumber.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(final ChangeEvent e) {
                 model.setSelectedAliasMinimumArguments((Integer) argumentsNumber.getValue());
             }
         });
 
         model.addListener(new AliasDialogModelAdapter() {
 
             @Override
             public void aliasSelectionChanged(final Optional<Alias> alias) {
                 argumentsNumber.setEnabled(model.isMinimumArgumentsValid());
                 argumentsNumber.setValue(model.getSelectedAliasMininumArguments());
             }
 
         });
     }
 
     public void bindResponse(final JTextArea response, final JScrollPane responseScroll) {
         response.setEnabled(false);
         response.getDocument().addDocumentListener(new DocumentListener() {
 
             private void update() {
                 model.setSelectedAliasSubstitution(response.getText());
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
         model.addListener(new AliasDialogModelAdapter() {
 
             @Override
             public void aliasSelectionChanged(final Optional<Alias> alias) {
                 response.setEnabled(model.isSubstitutionValid());
                 response.setText(model.getSelectedAliasSubstitution());
                 UIUtilities.resetScrollPane(responseScroll);
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
                         model.addAlias(getText(), 0, getText());
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
         deleteAlias.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 final Optional<Alias> alias = model.getSelectedAlias();
                 if (alias.isPresent()) {
                     model.removeAlias(alias.get().getName());
                 }
             }
         });
         model.addListener(new AliasDialogModelAdapter() {
 
             @Override
             public void aliasSelectionChanged(final Optional<Alias> alias) {
                 deleteAlias.setEnabled(model.getSelectedAlias().isPresent());
             }
 
         });
     }
 
     public void bindOKButton(final JButton okButton) {
         okButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 model.save();
                 dialog.dispose();
             }
         });
         model.addListener(new AliasDialogModelAdapter() {
 
             @Override
             public void selectedAliasEdited(String name, int minArgs, String sub) {
                 okButton.setEnabled(model.isSelectedAliasValid());
             }
 
         });
     }
 
     public void bindCancelButton(final JButton cancelButton) {
         cancelButton.setEnabled(true);
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 dialog.dispose();
             }
         });
     }
 
 }
