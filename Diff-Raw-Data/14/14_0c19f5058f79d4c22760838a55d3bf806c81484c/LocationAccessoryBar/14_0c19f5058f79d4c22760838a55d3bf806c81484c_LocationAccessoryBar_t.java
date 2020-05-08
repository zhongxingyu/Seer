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
 import org.trypticon.hex.HexViewerSelectionModel;
import org.trypticon.hex.binary.Binary;
 import org.trypticon.hex.util.swingsupport.PLAFUtils;
 import org.trypticon.hex.util.swingsupport.StealthFormattedTextField;
 import org.trypticon.hex.util.swingsupport.StealthSpinnerNumberEditor;
 
 import javax.swing.GroupLayout;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.ParseException;
 import java.util.ResourceBundle;
 
 /**
  * An accessory bar showing the location within the file and allowing changing it.
  *
  * @author trejkaz
  */
 public class LocationAccessoryBar extends AccessoryBar {
     private final HexViewer viewer;
     private final JSpinner columnsSpinner;
     private final CustomHexFormattedTextField offsetField;
     private final CustomHexFormattedTextField lengthField;
     private final Handler handler;
 
     public LocationAccessoryBar(HexViewer viewer) {
         this.viewer = viewer;
         handler = new Handler();
 
         ResourceBundle bundle = ResourceBundle.getBundle("org/trypticon/hex/Bundle");
         JLabel columnsLabel = new JLabel(bundle.getString("AccessoryBars.Location.columns"));
         columnsSpinner = new JSpinner(new SpinnerNumberModel(16, 8, 64, 8));
         JLabel offsetLabel = new JLabel(bundle.getString("AccessoryBars.Location.selectedOffset"));
         offsetField = new CustomHexFormattedTextField();
         JLabel lengthLabel = new JLabel(bundle.getString("AccessoryBars.Location.length"));
         lengthField = new CustomHexFormattedTextField();
 
         JSpinner.DefaultEditor columnsSpinnerEditor = new StealthSpinnerNumberEditor(columnsSpinner);
         columnsSpinner.setEditor(columnsSpinnerEditor);
         JTextField columnsSpinnerTextField = columnsSpinnerEditor.getTextField();
         columnsSpinner.getModel().addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent event) {
                 userChangedColumns();
             }
         });
 
         PLAFUtils.makeSmall(columnsLabel, columnsSpinner, columnsSpinnerTextField,
                             offsetLabel, offsetField, lengthLabel, lengthField);
 
         GroupLayout layout = new GroupLayout(this);
         setLayout(layout);
 
         layout.setHorizontalGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(columnsLabel)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(columnsSpinner, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(offsetLabel)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(offsetField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(lengthLabel)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lengthField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap());
 
         layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                 .addComponent(columnsLabel)
                 .addComponent(columnsSpinner)
                 .addComponent(offsetLabel)
                 .addComponent(offsetField)
                 .addComponent(lengthLabel)
                 .addComponent(lengthField));
     }
 
     private void binaryChanged() {
        Binary binary = viewer.getBinary();
        long maxLength = binary != null ? binary.length() : 1;
         String longestValue;
         try {
            longestValue = offsetField.getFormatter().valueToString(maxLength);
         } catch (ParseException e) {
             throw new RuntimeException("Unexpected error converting to string", e);
         }
        // Allocates one more character because sometimes using the exact value is too tight.
        int columns = longestValue.length() + 1;
        offsetField.setColumns(columns);
        lengthField.setColumns(columns);
     }
 
     private void locationChanged() {
         HexViewerSelectionModel selectionModel = viewer.getSelectionModel();
         long selectionStart = selectionModel.getSelectionStart();
         long selectionEnd = selectionModel.getSelectionEnd();
         offsetField.setValue(selectionStart);
         lengthField.setValue(selectionEnd - selectionStart + 1);
     }
 
     private void userChangedColumns() {
         int columns = ((Number) columnsSpinner.getValue()).intValue();
         viewer.setBytesPerRow(columns);
     }
 
     private void userChangedLocation() {
         long offset = ((Number) offsetField.getValue()).longValue();
         long length = ((Number) lengthField.getValue()).longValue();
         if (offset < 0 || offset > viewer.getBinary().length() ||
             offset + length > viewer.getBinary().length()) {
             return;
         }
         HexViewerSelectionModel selectionModel = viewer.getSelectionModel();
         selectionModel.setCursor(offset + length - 1);
         selectionModel.setCursorAndExtendSelection(offset);
     }
 
     @Override
     public void addNotify() {
         super.addNotify();
         viewer.addPropertyChangeListener("binary", handler);
         viewer.getSelectionModel().addChangeListener(handler);
         binaryChanged();
         locationChanged();
     }
 
     @Override
     public void removeNotify() {
         super.removeNotify();
         viewer.removePropertyChangeListener("binary", handler);
     }
 
     private class Handler implements PropertyChangeListener, ChangeListener {
         @Override
         public void propertyChange(PropertyChangeEvent event) {
             switch (event.getPropertyName()) {
                 case "binary":
                     binaryChanged();
                     break;
             }
         }
 
         @Override
         public void stateChanged(ChangeEvent event) {
             locationChanged();
         }
     }
 
     private class CustomHexFormattedTextField extends StealthFormattedTextField {
         // setFormatter() appears to have no effect. Swing uses its own default number formatter.
         CustomHexFormattedTextField() {
             super(new AbstractFormatterFactory() {
                 @Override
                 public AbstractFormatter getFormatter(JFormattedTextField tf) {
                     return new AbstractFormatter() {
                         @Override
                         public Object stringToValue(String text) throws ParseException {
                             text = text.trim();
                             if (text.isEmpty()) {
                                 return null;
                             }
                             try {
                                 return Long.decode(text);
                             } catch (NumberFormatException e) {
                                 throw new ParseException(text, 0);
                             }
                         }
 
                         @Override
                         public String valueToString(Object value) throws ParseException {
                             if (value == null) {
                                 return "";
                             }
                             return "0x" + Long.toString(((Number) value).longValue(), 16).toUpperCase();
                         }
                     };
                 }
             });
         }
 
         @Override
         public void commitEdit() throws ParseException {
             super.commitEdit();
             userChangedLocation();
         }
     }
 }
