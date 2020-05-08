 package org.iplantc.core.uicommons.client.views.gxt3.dialogs;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.form.Validator;
 import com.sencha.gxt.widget.core.client.form.validator.MaxLengthValidator;
 
 public class IPlantPromptDialog extends IPlantDialog {
 
     private static IPlantPromptDialogUiBinder uiBinder = GWT.create(IPlantPromptDialogUiBinder.class);
 
     interface IPlantPromptDialogUiBinder extends UiBinder<Widget, IPlantPromptDialog> {
     }
 
     @UiField
     FieldLabel fieldLabel;
 
     @UiField
     TextField textField;
 
     private final TextButton okButton;
 
     public IPlantPromptDialog(String caption, int maxLength, String initialText,
             Validator<String> validator) {
         add(uiBinder.createAndBindUi(this));
         okButton = getButtonById(PredefinedButton.OK.name());
         okButton.setEnabled(false);
         setInitialText(initialText);
         init(caption, maxLength, validator);
     }
 
     private void init(String caption, int maxLength, Validator<String> validator) {
         fieldLabel.setText(caption);
 
         if (maxLength > 0) {
             addValidator(new MaxLengthValidator(maxLength));
         }
 
         addValidator(validator);
     }
 
     protected void setInitialText(String initialText) {
         textField.setText(initialText);
         textField.selectAll();
     }
 
     @UiHandler("textField")
    void onKeyUp(KeyUpEvent event) {
        okButton.setEnabled(textField.isCurrentValid());
    }

    @UiHandler("textField")
    void onKeyDown(KeyDownEvent event) {
        if (textField.isCurrentValid() && (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)) {
             onButtonPressed(okButton);
         }
     }
 
     public void addValidator(Validator<String> validator) {
         if (validator == null) {
             return;
         }
         textField.addValidator(validator);
     }
 
     public String getFieldText() {
         return textField.getCurrentValue();
     }
 
     @Override
     protected void afterShow() {
         super.afterShow();
         textField.focus();
         textField.selectAll();
     }
 
 }
