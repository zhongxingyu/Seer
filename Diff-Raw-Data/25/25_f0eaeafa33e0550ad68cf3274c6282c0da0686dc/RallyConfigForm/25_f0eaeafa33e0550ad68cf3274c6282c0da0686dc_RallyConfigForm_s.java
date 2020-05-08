 package com.rallydev.intellij.config;
 
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.options.SearchableConfigurable;
 import org.apache.commons.lang.StringUtils;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 public class RallyConfigForm implements SearchableConfigurable {

     private static final String PASSWORD_PLACEHOLDER = "passwordPlaceholderpasswordPlaceholder";
 
     private JPanel configPanel;
 
     private JTextField url;
     private JTextField userName;
     private JPasswordField password;
     private JCheckBox rememberPassword;
 
     private JButton testConnectionButton;
     private JButton invalidateCachesButton;
 
     private RallyConfig rallyConfig;
 
     private boolean passwordChanged = false;
 
     public RallyConfigForm() {
         rallyConfig = RallyConfig.getInstance();
         rememberPassword.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent changeEvent) {
                 togglePassword();
             }
         });
     }
 
     public JTextField getUrl() {
         return url;
     }
 
     public JTextField getUserName() {
         return userName;
     }
 
     public JPasswordField getPassword() {
         return password;
     }
 
     public JCheckBox getRememberPassword() {
         return rememberPassword;
     }
 
     public boolean getPasswordChanged() {
         return passwordChanged;
     }
 
     @Override
     public String getDisplayName() {
         return "Rally";
     }
 
     public Icon getIcon() {
         return null;
     }
 
     @NotNull
     @Override
     public String getId() {
         return getHelpTopic();
     }
 
     @NotNull
     @Override
     public String getHelpTopic() {
         return "reference.idesettings.rally";
     }
 
     @Override
     public boolean isModified() {
         boolean isModified = !StringUtils.equals(url.getText(), rallyConfig.url);
         isModified = isModified || !StringUtils.equals(userName.getText(), rallyConfig.userName);
         if (rememberPassword.isSelected() && passwordChanged) {
             isModified = isModified || !StringUtils.equals(new String(password.getPassword()), rallyConfig.getPassword());
         }
         isModified = isModified || rememberPassword.isSelected() != rallyConfig.rememberPassword;
 
         return isModified;
     }
 
     @Nullable
     @Override
     public JComponent createComponent() {
         setupFromConfig();
 
         testConnectionButton.addActionListener(new TestConnectionButtonListener(this));
         invalidateCachesButton.addActionListener(new InvalidateCachesActionListener());
         password.getDocument().addDocumentListener(new DocumentListener() {
             public void insertUpdate(DocumentEvent documentEvent) {
                 onChange();
             }
 
             public void removeUpdate(DocumentEvent documentEvent) {
                 onChange();
             }
 
             public void changedUpdate(DocumentEvent documentEvent) {
                 onChange();
             }
 
             private void onChange() {
                 passwordChanged = true;
             }
         });
 
         return configPanel;
     }
 
     private void setupFromConfig() {
         url.setText(rallyConfig.url);
         userName.setText(rallyConfig.userName);
         rememberPassword.setSelected(rallyConfig.rememberPassword);
 
         togglePassword();
         if (rallyConfig.rememberPassword) {
             password.setText(PASSWORD_PLACEHOLDER);
         }
         passwordChanged = false;
     }
 
     public void togglePassword() {
         if (rememberPassword.isSelected()) {
             password.setEnabled(true);
         } else {
             password.setEnabled(false);
         }
     }
 
     @Nullable
     @Override
     public Runnable enableSearch(String option) {
         return null;
     }
 
     @Override
     public void apply() throws ConfigurationException {
         rallyConfig.url = url.getText();
         rallyConfig.userName = userName.getText();
         if (!rememberPassword.isSelected()) {
             RallyConfig.getInstance().clearPassword();
         } else {
             rallyConfig.setPassword(new String(password.getPassword()));
         }
         rallyConfig.rememberPassword = rememberPassword.isSelected();
     }
 
     @Override
     public void reset() {
         setupFromConfig();
     }
 
     @Override
     public void disposeUIResources() {
     }
 
 }
