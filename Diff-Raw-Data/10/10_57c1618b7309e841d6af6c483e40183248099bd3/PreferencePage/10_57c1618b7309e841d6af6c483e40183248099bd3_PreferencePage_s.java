 package com.versionone.common.preferences;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import com.versionone.apiclient.ConnectionException;
 import com.versionone.common.Activator;
 import com.versionone.common.preferences.ButtonFieldEditor.Validator;
 import com.versionone.common.sdk.ApiDataLayer;
 import com.versionone.common.sdk.ConnectionSettings;
 
 public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
 
     private StringFieldEditor urlEditor;
     private StringFieldEditor userEditor;
     private StringFieldEditor pwdField;
     private BooleanFieldEditor enabledEditor;
     private ButtonFieldEditor requiresValidation;
     private BooleanFieldEditor integratedAuthEditor;
     private boolean resetConnection = false;
     private BooleanFieldEditor useProxyEditor;
     private StringFieldEditor proxyUriEditor;
     private StringFieldEditor proxyUserEditor;
     private StringFieldEditor proxyPasswordEditor;
 
     public static IPreferenceStore getPreferences() {
         return Activator.getDefault().getPreferenceStore();
     }
 
     /**
      * Default Constructor
      */
     public PreferencePage() {
         super(GRID);
         setPreferenceStore(PreferencePage.getPreferences());
         setDescription("Connection Preferences");
     }
 
     /**
      * Initialize {@link #init(IWorkbench)}
      */
     public void init(IWorkbench workbench) {
     }
 
     /**
      * Creates the field editors. Field editors are abstractions of the common
      * GUI blocks needed to manipulate various types of preferences. Each field
      * editor knows how to save and restore itself.
      * {@link #createFieldEditors()}
      */
     @Override
     protected void createFieldEditors() {
         enabledEditor = new BooleanFieldEditor(PreferenceConstants.P_ENABLED, "Enabled", this.getFieldEditorParent());
         addField(enabledEditor);
 
         integratedAuthEditor = new BooleanFieldEditor(PreferenceConstants.P_INTEGRATED_AUTH,
                 "Windows Integrated Authentication", this.getFieldEditorParent());
         addField(integratedAuthEditor);
 
         urlEditor = new UrlFieldEditor(PreferenceConstants.P_URL, "Application URL:", this.getFieldEditorParent());
         addField(urlEditor);
 
         userEditor = new UserNameFieldEditor(PreferenceConstants.P_USER, "Username:", this.getFieldEditorParent());
         addField(userEditor);
 
         pwdField = new VersionOneStringFieldEditor(PreferenceConstants.P_PASSWORD, "Password:", this.getFieldEditorParent());
         pwdField.getTextControl(this.getFieldEditorParent()).setEchoChar('*');
         addField(pwdField);
         
         useProxyEditor = new BooleanFieldEditor(PreferenceConstants.P_PROXY_ENABLED, "Use proxy", this.getFieldEditorParent());
         addField(useProxyEditor);
         
         proxyUriEditor = new ProxyUrlFieldEditor(PreferenceConstants.P_PROXY_URI, "Proxy Uri:", this.getFieldEditorParent());
         addField(proxyUriEditor);
         
         proxyUserEditor = new VersionOneStringFieldEditor(PreferenceConstants.P_PROXY_USER, "Proxy Username:", this.getFieldEditorParent());
         addField(proxyUserEditor);
         
         proxyPasswordEditor = new VersionOneStringFieldEditor(PreferenceConstants.P_PROXY_PASSWORD, "Proxy Password:", this.getFieldEditorParent());
         proxyPasswordEditor.getTextControl(this.getFieldEditorParent()).setEchoChar('*');
         addField(proxyPasswordEditor);
         
         requiresValidation = new ButtonFieldEditor(PreferenceConstants.P_REQUIRESVALIDATION, "Validate Connection",
                 new ConnectionValidator(), this.getFieldEditorParent());
         addField(requiresValidation);
 
         setControlAccess(PreferencePage.getPreferences().getBoolean(PreferenceConstants.P_ENABLED));
         setProxyAccess(PreferencePage.getPreferences().getBoolean(PreferenceConstants.P_PROXY_ENABLED));        
     }
 
     /**
      * Determines if fields are enabled
      * 
      * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean,
      *      org.eclipse.swt.widgets.Composite)
      * @param value
      *            passed to setEnabled on field editors
      */
     private void setControlAccess(boolean value) {
         urlEditor.setEnabled(value, this.getFieldEditorParent());
         if (getPreferences().getBoolean(PreferenceConstants.P_INTEGRATED_AUTH)) {
             userEditor.setEnabled(false, this.getFieldEditorParent());
             pwdField.setEnabled(false, this.getFieldEditorParent());
         } else {
             userEditor.setEnabled(value, this.getFieldEditorParent());
             pwdField.setEnabled(value, this.getFieldEditorParent());
         }
 
         requiresValidation.setEnabled(value, this.getFieldEditorParent());
         integratedAuthEditor.setEnabled(value, this.getFieldEditorParent());
         useProxyEditor.setEnabled(value, this.getFieldEditorParent());
         //setProxyAccess(PreferencePage.getPreferences().getBoolean(PreferenceConstants.P_PROXY_ENABLED) && value);
     }
     
     /**
      * Determines if proxy enabled.
      * 
      * @param value -  proxy enable status.
      */
     private void setProxyAccess(boolean value) {
         proxyPasswordEditor.setEnabled(value, this.getFieldEditorParent());
         proxyUriEditor.setEnabled(value, this.getFieldEditorParent());
         proxyUserEditor.setEnabled(value, this.getFieldEditorParent());                
     }
 
     /**
      * {@link #performDefaults()}
      */
     @Override
     protected void performDefaults() {
         super.performDefaults();
         this.setControlAccess(this.enabledEditor.getBooleanValue());
         setProxyAccess(useProxyEditor.getBooleanValue() && enabledEditor.getBooleanValue());
         this.checkState();
     }
 
     /**
      * {@link #propertyChange(PropertyChangeEvent)}
      */
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         super.propertyChange(event);
         if (event.getSource().equals(enabledEditor)) {
             setControlAccess(enabledEditor.getBooleanValue());
             setProxyAccess(enabledEditor.getBooleanValue() && useProxyEditor.getBooleanValue());
             this.checkState();
         }
         if (event.getSource().equals(useProxyEditor)) {
             setProxyAccess(useProxyEditor.getBooleanValue());
             requiresValidation.setEnabled(true, this.getFieldEditorParent());
             this.checkState();            
         }        
         if (event.getSource().equals(integratedAuthEditor)) {
             userEditor.loadDefault();
             pwdField.loadDefault();
             userEditor.setEnabled(!integratedAuthEditor.getBooleanValue(), this.getFieldEditorParent());
             pwdField.setEnabled(!integratedAuthEditor.getBooleanValue(), this.getFieldEditorParent());
             requiresValidation.setEnabled(true, this.getFieldEditorParent());
             this.checkState();
         } else if (event.getProperty().equals(FieldEditor.VALUE)) {
            if ((event.getSource().equals(userEditor)) || (event.getSource().equals(pwdField))
                    || (event.getSource().equals(urlEditor))) {
                 requiresValidation.setEnabled(true, this.getFieldEditorParent());
             }
             this.checkState();
         }
     }
 
     /**
      * {@link #checkState()}
      */
     @Override
     protected void checkState() {
         super.checkState();
         boolean isValid = true;
         String message = null;
         if (!enabledEditor.getBooleanValue()) {
             setValid(true);
             setErrorMessage(null);
             return;            
         } else if (!this.isValid()) {
             setValid(false);
             setErrorMessage(errorMessage);
             return;
         }
         
         if (requiresValidation.getValue()) {
             message = "Connection Parameters Require Validation";
             isValid = false;
         } else {
             message = null;
             isValid = true;
         }
         
         setErrorMessage(message);
         setValid(isValid);
     }
     
 
 
     /**
      * Validate the connection
      * 
      * @author Jerry D. Odenwelder Jr.
      * 
      */
     class ConnectionValidator implements Validator {
 
         public boolean isValid() {
             boolean rc = true;
             try {
                 rc = ApiDataLayer.getInstance().checkConnection(getConnectionSettings());
                 if (rc) {
                     resetConnection = true;
                 } else {
                     setErrorMessage("Validation Failed.");                
                 }            
             } catch (ConnectionException ex) {
                 setErrorMessage(ex.getMessage());
             }
             return rc;
         }       
     }
 
     @Override
     public boolean performOk() {        
         
         boolean rc = true;
         if (resetConnection) {
             try {
                 Activator.connect(getConnectionSettings());
                 updateMemberToken(urlEditor.getStringValue());
                 PreferencePage.getPreferences().setValue(PreferenceConstants.P_PROJECT_TOKEN, ApiDataLayer.getInstance().getCurrentProjectId());
                 resetConnection = false;
                 rc = super.performOk();
             } catch (Exception e) {
                 Activator.logError(e);
                 MessageDialog.openInformation(this.getShell(), "VersionOne Preferences",
                         "Cannot obtain user identity from server.  Check Error Log for more details");
                 rc = false;
             }
             
         } else if (!enabledEditor.presentsDefaultValue()) {
             rc = super.performOk();
         }
         return rc;
     }
     
     private ConnectionSettings getConnectionSettings() {
         ConnectionSettings settings = new ConnectionSettings();
         settings.v1Path = urlEditor.getStringValue();
         settings.v1Username = userEditor.getStringValue(); 
         settings.v1Password = pwdField.getStringValue();
         settings.isWindowsIntegratedAuthentication = integratedAuthEditor.getBooleanValue();
         settings.isProxyEnabled = useProxyEditor.getBooleanValue();
         settings.proxyUri = proxyUriEditor.getStringValue();
         settings.proxyUsername = proxyUserEditor.getStringValue();
         settings.proxyPassword = proxyPasswordEditor.getStringValue();
         return settings;
     }
     
     private void updateMemberToken(String path) {
         String userToken = ApiDataLayer.getInstance().getCurrentMemberToken();
 
         String currentOid = PreferencePage.getPreferences().getString(PreferenceConstants.P_MEMBER_TOKEN);
         if (userToken != null && !currentOid.equals(userToken + ":" + path)) {
             PreferencePage.getPreferences().setValue(PreferenceConstants.P_MEMBER_TOKEN, userToken + ":" + path);
         }
 
         
     }
 
     @Override
     public boolean performCancel() {
         resetConnection = false;
         return super.performCancel();
     }
 
     // Special classes for properties
     private String errorMessage = null;
     
     private class VersionOneStringFieldEditor extends StringFieldEditor {
 
         public VersionOneStringFieldEditor(String pUrl, String string, Composite fieldEditorParent) {
             super(pUrl, string, fieldEditorParent);            
         }
 
         protected boolean doCheckState() {
             boolean isValid = super.doCheckState();
             
             if (requiresValidation.getValue()) {
                 errorMessage = "Connection Parameters Require Validation";
                 isValid = false;
             }            
             setErrorMessage(errorMessage);
             
             return isValid;
         }
         
         @Override
         public boolean isValid() {
             refreshValidState();
             return super.isValid();
         }        
     }
     
     private class UrlFieldEditor extends VersionOneStringFieldEditor {
         
         public UrlFieldEditor(String pUrl, String string, Composite fieldEditorParent) {
             super(pUrl, string, fieldEditorParent);
         }
 
         @Override
         protected boolean doCheckState() {
             boolean isValid = super.doCheckState();
             
             if (0 == getStringValue().length()) {
                 errorMessage = "Application URL Is a required field";
                 isValid = false;
                 requiresValidation.setEnabled(false, getFieldEditorParent());
             } else if (!getStringValue().endsWith("/")) {
                 errorMessage = "URL Must end with a /";
                 isValid = false;
                 requiresValidation.setEnabled(false, getFieldEditorParent());
             } 
 
             setErrorMessage(errorMessage);
             
             return isValid;
         }
     }
     
     private class ProxyUrlFieldEditor extends VersionOneStringFieldEditor {
         
         public ProxyUrlFieldEditor(String pUrl, String string, Composite fieldEditorParent) {
             super(pUrl, string, fieldEditorParent);
         }
 
         @Override
         protected boolean doCheckState() {
             boolean isValid = super.doCheckState();
             
             if (getStringValue().length() == 0 && useProxyEditor.getBooleanValue()) {
                 errorMessage = "Proxy Uri Is a required field";
                 isValid = false;
                 requiresValidation.setEnabled(false, getFieldEditorParent());
             } else {
                 try {
                     new URI(getStringValue());
                 } catch (URISyntaxException e) {
                     errorMessage = "Proxy Uri syntax error.";
                     isValid = false;
                 }
             }
             
 
             setErrorMessage(errorMessage);
             
             return isValid;
         }
     } 
     
     private class UserNameFieldEditor extends VersionOneStringFieldEditor {
         public UserNameFieldEditor(String pUrl, String string, Composite fieldEditorParent) {
             super(pUrl, string, fieldEditorParent);
         }
 
         @Override
         protected boolean doCheckState() {
             boolean isValid = super.doCheckState();
             
             if ((!integratedAuthEditor.getBooleanValue()) && (0 == getStringValue().length())) {
                 errorMessage = "Username is a required field";
                 isValid = false;
                 requiresValidation.setEnabled(false, getFieldEditorParent());
             }
 
             setErrorMessage(errorMessage);
             
             return isValid;
         }
     }    
     
 }
