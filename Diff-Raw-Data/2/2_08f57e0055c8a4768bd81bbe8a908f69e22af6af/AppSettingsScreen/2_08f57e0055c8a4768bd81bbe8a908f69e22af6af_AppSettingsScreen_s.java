 package com.cleverua.bb.example;
 
 import com.cleverua.bb.settings.SettingsException;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.EditField;
 import net.rim.device.api.ui.container.MainScreen;
 
 public class AppSettingsScreen extends MainScreen {
     private static final String SETTINGS_SUCCESSFUL_DIALOG = "Settings were saved successfully!";
     private static final String SAVE_BUTTON_LABEL = "Save settings";
     private static final String CHECK_BOX_LABEL = "User choice";
     
     private CheckboxField userChoice;
     private EditField userText;
     
     
     public AppSettingsScreen() {
         super();
         initUI();
     }
 
     private void initUI() {
         try {
             AppSettingsApplication.getSettings().initialize();
         } catch (SettingsException e) {
             Dialog.alert("Unable to load settings: " + e);
         }
         boolean userChoiceSetting = AppSettingsApplication.getSettingsDelegate().getUserChoice();
         userChoice = new CheckboxField(CHECK_BOX_LABEL, userChoiceSetting, USE_ALL_WIDTH);
         userText = new EditField(USE_ALL_WIDTH);
         String userTextSetting = AppSettingsApplication.getSettingsDelegate().getUserText();
         userText.setText(userTextSetting);
         
         add(userText);
         add(userChoice);
         userText.setFocus();
         
         ButtonField saveButton = new ButtonField(SAVE_BUTTON_LABEL, FIELD_HCENTER);
         saveButton.setChangeListener(new FieldChangeListener() {
             public void fieldChanged(Field arg0, int arg1) {
                 AppSettingsApplication.getSettingsDelegate().setUserChoice(userChoice.getChecked());
                 AppSettingsApplication.getSettingsDelegate().setUserText(userText.getText());
                 try {
                     AppSettingsApplication.getSettings().flush();
                     Dialog.inform(SETTINGS_SUCCESSFUL_DIALOG);
                 } catch (SettingsException e) {
                    Dialog.alert("Unable to save settings: " + e);
                 }
             }
         });
         setStatus(saveButton);
     }
     
     protected boolean onSavePrompt() {
         return true;
     }
 }
