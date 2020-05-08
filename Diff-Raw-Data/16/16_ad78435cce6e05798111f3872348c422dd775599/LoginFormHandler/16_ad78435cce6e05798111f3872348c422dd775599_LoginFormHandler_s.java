 package com.lavida.swing.handler;
 
 import com.lavida.service.UserService;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.exception.ExceptionHandler;
 import com.lavida.swing.preferences.UserSettings;
 import com.lavida.swing.preferences.UsersSettings;
 import com.lavida.swing.service.UserSettingsService;
 import com.lavida.swing.preferences.UsersSettingsHolder;
 import com.lavida.swing.form.MainForm;
 import com.lavida.swing.exception.UserValidationException;
 import com.lavida.swing.form.LoginForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.security.authentication.AuthenticationServiceException;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import javax.xml.bind.JAXBException;
 import java.awt.*;
 import java.io.IOException;
 
 /**
  * LoginFormHandler - handler for login form.
  * <p/>
  * Created: 20:08 08.08.13
  *
  * @author Pavel
  */
 @Component
 public class LoginFormHandler {
     private static final Logger logger = LoggerFactory.getLogger(LoginFormHandler.class);
 
     // Regular expression for checking input data in loginField and passwordField
     private static final String REGULAR_EXPRESSION_FOR_CREDENTIALS = "[A-Za-zА-Яа-я0-9.-_]*";
 
     @Resource
     private LoginForm form;
 
     @Resource
     private UserService userService;
 
     @Resource
     private MainForm mainForm;
 
     @Resource
     private UsersSettingsHolder usersSettingsHolder;
 
     @Resource
     private UserSettingsService userSettingsService;
 
     @Resource
     private MessageSource messageSource;
 
     @Resource
     private LocaleHolder localeHolder;
 
 
     /**
      * EventListener for submit button checks user credentials from database "lavida".
      * If input fields are empty or incorrect the error message will be shown in error label.
      */
     public void submitButtonClicked(String loginEntered, String passwordEntered) {
         Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(messageSource, localeHolder));
 
         try {
             form.clearFields();
             validateCredentials(loginEntered, passwordEntered);
             userService.login(loginEntered, passwordEntered);
             loadUserSettings(loginEntered);
             mainForm.filterTableByRoles();
             mainForm.filterTableDataByRole(userService.getCurrentUserRoles());
             mainForm.filterAnalyzePanelByRoles();
             mainForm.filterMenuBarByRoles();
             mainForm.removeFiltersByRoles();
             mainForm.filterSellDialogByRoles(userService.getCurrentUserRoles());
             mainForm.initializeTableViewComponents();
             if (!userSettingsService.userDefaultPresetExists()) {
                 mainForm.createDefaultPreset();
             }
 //            String defaultPresetName = messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale());
 //            if (!defaultPresetName.equals(usersSettingsHolder.getPresetName())) {
 //                mainForm.initializeUserSettings();
 //            }
             mainForm.initializePresetMenuItems();
             mainForm.updatePresetNameField();
             mainForm.getHandler().refreshTableItemClicked();
             mainForm.show();
             form.hide();
         } catch (UserValidationException | BadCredentialsException | AuthenticationServiceException e) {
             Toolkit.getDefaultToolkit().beep();
             form.showErrorMessage(e.getMessage());
         }
     }
 
 
     private void loadUserSettings(String login) {
         usersSettingsHolder.setLogin(login);
         try {
             if (userSettingsService.getSettingsFile().exists()) {
                 UsersSettings usersSettings = userSettingsService.getSettings();
                 usersSettingsHolder.setUsersSettings(usersSettings);
                 UserSettings userSettings = userSettingsService.getUserSettings();
                 if (userSettings != null) {
                     String lastPresetName = userSettings.getLastPresetName();
                     if (lastPresetName != null) {
                         usersSettingsHolder.setPresetName(lastPresetName);
                     } else {
                         usersSettingsHolder.setPresetName(messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale()));
                     }
                 } else {
                     usersSettingsHolder.setUsersSettings(new UsersSettings());
                     userSettingsService.saveSettings(usersSettingsHolder.getUsersSettings());
                     usersSettingsHolder.setPresetName(messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale()));
                 }
 
             } else {
                 usersSettingsHolder.setUsersSettings(new UsersSettings());
                 userSettingsService.saveSettings(usersSettingsHolder.getUsersSettings());
                 usersSettingsHolder.setPresetName(messageSource.getMessage("settings.user.preset.default.name", null, localeHolder.getLocale()));
             }
             if (usersSettingsHolder.getUsersSettings().getEditorsSettings() == null) {
                 mainForm.holdAllTables();
                 usersSettingsHolder.setUsersSettings(userSettingsService.createEditorsSettings());
                 userSettingsService.saveSettings(usersSettingsHolder.getUsersSettings());
             }
         } catch (JAXBException | IOException e) {
            logger.error(e.getMessage(), e);
            Toolkit.getDefaultToolkit().beep();
            form.showWarningMessage("mainForm.exception.message.dialog.title", "mainForm.handler.save.usersSettings.error.message");
         }
     }
 
     /**
      * Validates the input data in login form for not empty fields, correct format and existence of user
      * in database "lavida".
      *
      * @param login    the login text from loginField to be validated;
      * @param password the password text from passwordField to be validated;
      * @throws UserValidationException if login and/or password are null, if login and/or password
      *                                 don't match regular expression, if login and/or password don't match database values.
      */
     private void validateCredentials(String login, String password) throws UserValidationException {
         if (login == null || login.trim().isEmpty()) {
             throw new UserValidationException(UserValidationException.NULL_PRINCIPAL_MESSAGE_RU);
         } else if (password == null || password.trim().isEmpty()) {
             throw new UserValidationException(UserValidationException.NULL_CREDENTIALS_MESSAGE_RU);
         } else if (!login.matches(REGULAR_EXPRESSION_FOR_CREDENTIALS) || !password.matches(REGULAR_EXPRESSION_FOR_CREDENTIALS)) {
             throw new UserValidationException(UserValidationException.INCORRECT_FORMAT_MESSAGE_RU);
         }
     }
 }
