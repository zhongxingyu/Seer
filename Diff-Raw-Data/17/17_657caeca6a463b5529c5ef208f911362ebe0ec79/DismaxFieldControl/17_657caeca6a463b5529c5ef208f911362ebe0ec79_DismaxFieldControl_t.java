 package com.github.steppenwells.client.ui;
 
 import com.github.steppenwells.client.DismaxToolState;
 import com.github.steppenwells.client.model.DismaxField;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.*;
 
 public class DismaxFieldControl extends Composite {
 
     private final DismaxField dismaxField;
     private final DismaxToolState dismaxToolState;
 
     private CheckBox enabledCheckbox;
     private TextBox weightTextBox;
 
     public DismaxFieldControl(DismaxField dismaxField, DismaxToolState dismaxToolState) {
         this.dismaxField = dismaxField;
         this.dismaxToolState = dismaxToolState;
         initUi();
     }
 
     private void initUi() {
         HorizontalPanel panel = new HorizontalPanel();
 
         enabledCheckbox = new CheckBox(dismaxField.getFieldName());
        enabledCheckbox.setValue(dismaxField.isInUse());
         enabledCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
             @Override
             public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                 dismaxField.setInUse(booleanValueChangeEvent.getValue());
                 dismaxToolState.weightingsChanged();
             }
         });
         panel.add(enabledCheckbox);
 
         // TODO: is there a more appropriate control than a straight textbox?
         weightTextBox = new TextBox();
         weightTextBox.setValue(String.valueOf(dismaxField.getWeight()));
         weightTextBox.addValueChangeHandler(new ValueChangeHandler<String> () {
             @Override
             public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                 String weightString = stringValueChangeEvent.getValue();
                 try {
                     double weight = Double.parseDouble(weightString);
                     dismaxField.setWeight(weight);
                     dismaxToolState.weightingsChanged();
                 } catch (NumberFormatException nfe) {
                     //TODO put error label here.
                 }
             }
         });
         panel.add(weightTextBox);
 
         initWidget(panel);
 
     }
 
     public DismaxField getDismaxField() {
         return dismaxField;
     }
 }
