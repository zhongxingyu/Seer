 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2013  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.accessory;
 
 import org.trypticon.hex.HexViewer;
import org.trypticon.hex.interpreters.FixedLengthInterpreter;
 import org.trypticon.hex.interpreters.Interpreter;
 import org.trypticon.hex.interpreters.InterpreterInfo;
 import org.trypticon.hex.interpreters.MasterInterpreterStorage;
 import org.trypticon.hex.interpreters.meta.LengthOption;
 import org.trypticon.hex.interpreters.nulls.NullInterpreterInfo;
 import org.trypticon.hex.util.Format;
 import org.trypticon.hex.util.swingsupport.NameRenderingComboBox;
 import org.trypticon.hex.util.swingsupport.PLAFUtils;
 import org.trypticon.hex.util.swingsupport.StealthFormattedTextField;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.text.JTextComponent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.prefs.Preferences;
 
 /**
  * An accessory bar which shows the interpretation of the selected bytes as a value.
  *
  * @author trejkaz
  */
 public class InterpreterAccessoryBar extends AccessoryBar {
     private final ResourceBundle bundle = ResourceBundle.getBundle("org/trypticon/hex/Bundle");
 
     private final HexViewer viewer;
     private final JComboBox<InterpreterInfo> typeComboBox;
     private final JComboBox<LengthOption> lengthComboBox;
     private final JComboBox<?> byteOrderComboBox;
     private final JFormattedTextField valueTextField;
 
     private Preferences preferencesNode;
 
     public InterpreterAccessoryBar(HexViewer viewer) {
         this.viewer = viewer;
 
         List<InterpreterInfo> infos = filter(new MasterInterpreterStorage().getGroupedInterpreterInfos());
 
         typeComboBox = new NameRenderingComboBox<>(infos.toArray(new InterpreterInfo[infos.size()]));
         lengthComboBox = new JComboBox<>();
         byteOrderComboBox = new JComboBox<>(new String[] {
                 bundle.getString("AccessoryBars.Interpreter.byteOrderComboBox.big"),
                 bundle.getString("AccessoryBars.Interpreter.byteOrderComboBox.little")
                 });
 
         valueTextField = new StealthFormattedTextField(new ValueFormatterFactory());
         valueTextField.setEditable(false);
 
         SharedListener sharedListener = new SharedListener();
         selectedTypeChanged();
         typeComboBox.addItemListener(sharedListener);
         lengthComboBox.addItemListener(sharedListener);
         byteOrderComboBox.addItemListener(sharedListener);
         viewer.getSelectionModel().addChangeListener(sharedListener);
         viewer.addPropertyChangeListener("binary", sharedListener);
         valueTextField.addPropertyChangeListener("value", sharedListener);
 
         PLAFUtils.makeSquare(typeComboBox, lengthComboBox, byteOrderComboBox);
         PLAFUtils.makeSmall(typeComboBox, lengthComboBox, byteOrderComboBox, valueTextField);
 
         GroupLayout layout = new GroupLayout(this);
         setLayout(layout);
 
         layout.setHorizontalGroup(layout.createSequentialGroup()
                                           .addGap(4)
                                           .addComponent(typeComboBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                           .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                           .addComponent(lengthComboBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                           .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                           .addComponent(byteOrderComboBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                           .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                           .addComponent(valueTextField)
                                           .addGap(4));
 
         layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(typeComboBox)
                                         .addComponent(lengthComboBox)
                                         .addComponent(byteOrderComboBox)
                                         .addComponent(valueTextField));
     }
 
     @Override
     protected void setPreferencesNode(Preferences node) {
         preferencesNode = node;
         if (node != null) {
             String type = node.get("type", null);
             ComboBoxModel<InterpreterInfo> typeComboBoxModel = typeComboBox.getModel();
             int typeCount = typeComboBoxModel.getSize();
             for (int index = 0; index < typeCount; index++) {
                 InterpreterInfo info = typeComboBoxModel.getElementAt(index);
                 if (info.toLocalisedString(Format.SHORT, Locale.ROOT).equals(type)) {
                     typeComboBoxModel.setSelectedItem(info);
                     break;
                 }
             }
 
             String lengthString = node.get("length", null);
             if (lengthString != null) {
                 lengthComboBox.setSelectedItem(LengthOption.valueOf(lengthString));
             }
 
             //TODO: A ByteOrderOption class with proper localisation perhaps?
             String byteOrderString = node.get("byteOrder", null);
             if (byteOrderString != null) {
                 byteOrderComboBox.setSelectedIndex("big".equals(byteOrderString) ? 0 : 1);
             }
         }
     }
 
     private void selectedDataChanged() {
         updateInterpretedValue();
     }
 
     private void selectedTypeChanged() {
         updateLengthComboBox();
         updateByteOrderComboBox();
         updateInterpretedValue();
         if (preferencesNode != null) {
             InterpreterInfo selectedItem = (InterpreterInfo) typeComboBox.getSelectedItem();
             preferencesNode.put("type", selectedItem.toLocalisedString(Format.SHORT, Locale.ROOT));
         }
     }
 
     private void selectedLengthChanged() {
         updateInterpretedValue();
         if (preferencesNode != null) {
             preferencesNode.put("length", ((LengthOption) lengthComboBox.getSelectedItem()).name());
         }
     }
 
     private void selectedByteOrderChanged() {
         updateInterpretedValue();
         if (preferencesNode != null) {
             preferencesNode.put("byteOrder", byteOrderComboBox.getSelectedIndex() == 0 ? "big" : "little");
         }
     }
 
     private void updateLengthComboBox() {
         InterpreterInfo info = (InterpreterInfo) typeComboBox.getSelectedItem();
         List<LengthOption> values = getLengthOptions(info);
         if (values != null) {
             lengthComboBox.setModel(new DefaultComboBoxModel<>(values.toArray(new LengthOption[values.size()])));
         }
         lengthComboBox.setVisible(values != null);
     }
 
     private void updateByteOrderComboBox() {
         InterpreterInfo info = (InterpreterInfo) typeComboBox.getSelectedItem();
         byteOrderComboBox.setVisible(interpreterHasByteOrderOption(info));
     }
 
     private void updateInterpretedValue() {
         InterpreterInfo info = (InterpreterInfo) typeComboBox.getSelectedItem();
         Map<String, Object> options = new HashMap<>(4);
         if (interpreterHasLengthOption(info)) {
             options.put("length", lengthComboBox.getSelectedItem());
         }
         if (interpreterHasByteOrderOption(info)) {
             options.put("byteOrder", byteOrderComboBox.getSelectedIndex() == 0 ?
                                      ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
         }
         Interpreter<?> interpreter = info.create(options);
 
         long selectionStart = viewer.getSelectionModel().getSelectionStart();
         long selectionEnd = viewer.getSelectionModel().getSelectionEnd();
         Object value;
         try {
            if (interpreter instanceof FixedLengthInterpreter) {
                value = interpreter.interpret(viewer.getBinary(), selectionStart,
                                              ((FixedLengthInterpreter) interpreter).getValueLength());
            } else {
                value = interpreter.interpret(viewer.getBinary(), selectionStart,
                                              selectionEnd - selectionStart + 1);
            }
         } catch (IllegalArgumentException e) {
             valueTextField.setValue(bundle.getString("AccessoryBars.Interpreter.invalidSelection"));
             return;
         }
 
         valueTextField.setValue(value);
     }
 
     private List<LengthOption> getLengthOptions(InterpreterInfo info) {
         for (InterpreterInfo.Option option : info.getOptions()) {
             // Can expand this as the UI grows to support more options.
             if ("length".equals(option.getName()) && LengthOption.class.equals(option.getType())) {
                 @SuppressWarnings("unchecked")
                 List<LengthOption> safeValues = (List<LengthOption>) option.getValues();
                 return safeValues;
             }
         }
         return null;
     }
 
     private boolean interpreterHasLengthOption(InterpreterInfo info) {
         for (InterpreterInfo.Option option : info.getOptions()) {
             // Can expand this as the UI grows to support more options.
             if ("length".equals(option.getName()) && LengthOption.class.equals(option.getType())) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean interpreterHasByteOrderOption(InterpreterInfo info) {
         for (InterpreterInfo.Option option : info.getOptions()) {
             // Can expand this as the UI grows to support more options.
             if ("byteOrder".equals(option.getName()) && ByteOrder.class.equals(option.getType())) {
                 return true;
             }
         }
         return false;
     }
 
     private static List<InterpreterInfo> filter(List<InterpreterInfo> infos) {
         List<InterpreterInfo> result = new ArrayList<>(infos.size());
         for (InterpreterInfo info : infos) {
             if (info instanceof NullInterpreterInfo) {
                 // Not much point to using this one.
                 continue;
             }
 
             boolean usable = true;
             for (InterpreterInfo.Option option : info.getOptions()) {
                 // Can expand this as the UI grows to support more options.
                 if (!("byteOrder".equals(option.getName()) && ByteOrder.class.equals(option.getType())) &&
                         !("length".equals(option.getName()) && LengthOption.class.equals(option.getType()))) {
                     usable = false;
                     break;
                 }
             }
             if (usable) {
                 result.add(info);
             }
         }
         return result;
     }
 
     // All listeners in a single handler
     private class SharedListener implements PropertyChangeListener, ChangeListener, ItemListener {
         @Override
         public void propertyChange(PropertyChangeEvent event) {
             switch (event.getPropertyName()) {
                 case "value":
                     ((JTextComponent) event.getSource()).setCaretPosition(0);
                     break;
                 case "binary":
                     selectedDataChanged();
                     break;
             }
         }
 
         @Override
         public void stateChanged(ChangeEvent event) {
             selectedDataChanged();
         }
 
         @Override
         public void itemStateChanged(ItemEvent event) {
             Object source = event.getSource();
             if (source == typeComboBox) {
                 selectedTypeChanged();
             } else if (source == lengthComboBox) {
                 selectedLengthChanged();
             } else if (source == byteOrderComboBox) {
                 selectedByteOrderChanged();
             }
         }
     }
 }
