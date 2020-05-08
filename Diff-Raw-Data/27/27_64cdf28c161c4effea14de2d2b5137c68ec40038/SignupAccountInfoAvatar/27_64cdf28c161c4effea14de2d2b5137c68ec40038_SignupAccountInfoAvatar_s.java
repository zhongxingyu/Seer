 /*
  * SignupAccountInfoAvatar.java
  *
  * Created on April 2, 2007, 10:04 AM
  */
 
 package com.thinkparity.ophelia.browser.platform.firstrun;
 
 import java.awt.Component;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.Document;
 
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.email.EMailFormatException;
 import com.thinkparity.codebase.swing.SwingUtil;
 import com.thinkparity.codebase.swing.text.JTextFieldLengthFilter;
 
 import com.thinkparity.codebase.model.profile.EMailReservation;
 import com.thinkparity.codebase.model.profile.ProfileConstraints;
 import com.thinkparity.codebase.model.profile.UsernameReservation;
 import com.thinkparity.codebase.model.session.Credentials;
 
 import com.thinkparity.ophelia.model.profile.ReservationExpiredException;
 import com.thinkparity.ophelia.model.session.OfflineException;
 
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Colours;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.component.LabelFactory;
 import com.thinkparity.ophelia.browser.application.browser.component.TextFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.platform.LearnMore;
 
 /**
  * <b>Title:</b>thinkParity OpheliaUI Sign-Up Account Info Avatar<br>
  * <b>Description:</b>Collects the following information from the user:
  * <ol>
  * <li>Feature set [FREE,PREMIUM]
  * <li>Username
  * <li>Password
  * <li>Security question
  * <li>Security answer
  * </ol>
  * Then moves to one of the following pages:
  * <ul>
  * <li>Payment - If a standard account is selected.
  * <li>Payment Plan - If a standard account is selected and we are running
  * internal.
  * <li>Login - If a guest account is selected.
  * </ul>
  * <br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.12
  */
 public class SignupAccountInfoAvatar extends DefaultSignupPage {
 
     /** The number of security questions. */
     private static final int NUMBER_SECURITY_QUESTIONS;
 
     static {
         NUMBER_SECURITY_QUESTIONS = 5;
     }
 
     /**
      * A <code>Map</code> of <code>EMail</code> addresses to their
      * <code>EMailReservation</code>s.
      */
     private final Map<EMail,EMailReservation> emailReservations;
 
     /** An instance of <code>ProfileConstraints</code>. */
     private final ProfileConstraints profileConstraints;
 
     /** The country <code>DefaultComboBoxModel</code>. */
     private final DefaultComboBoxModel securityQuestionModel;
 
     /** The temporary error <code>String</code>. */
     private String temporaryError;
 
     /** The most recent unacceptable <code>EMail</code> address. */
     private EMail unacceptableEMail;
 
     /** The most recent unacceptable username <code>String</code>. */
     private String unacceptableUsername;
 
     /**
      * A <code>Map</code> of username <code>String</code>s to their
      * <code>UsernameReservation</code>s.
      */
     private final Map<String, UsernameReservation> usernameReservations;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JRadioButton accountTypeGuestJRadioButton = new javax.swing.JRadioButton();
     private final javax.swing.JRadioButton accountTypeStandardJRadioButton = new javax.swing.JRadioButton();
     private final javax.swing.JPasswordField confirmPasswordJPasswordField = TextFactory.createPassword();
     private final javax.swing.JTextField emailJTextField = TextFactory.create();
     private final javax.swing.JLabel errorMessageJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel learnMoreJLabel = LabelFactory.createLink("",Fonts.DefaultFont);
     private final javax.swing.JPasswordField passwordJPasswordField = TextFactory.createPassword();
     private final javax.swing.JTextField securityAnswerJTextField = TextFactory.create();
     private final javax.swing.JComboBox securityQuestionJComboBox = new javax.swing.JComboBox();
     private final javax.swing.JTextField usernameJTextField = TextFactory.create();
     // End of variables declaration//GEN-END:variables
 
     /**
      * Create SignupAccountInfoAvatar.
      */
     public SignupAccountInfoAvatar() {
         super("SignupAvatar.AccountInfo", BrowserConstants.DIALOGUE_BACKGROUND);
         this.profileConstraints = ProfileConstraints.getInstance();
         this.securityQuestionModel = new DefaultComboBoxModel();
         this.usernameReservations = new HashMap<String, UsernameReservation>();
         this.emailReservations = new HashMap<EMail, EMailReservation>();
         initSecurityQuestionModel();
         initComponents();
         addValidationListeners();
         initFocusListener();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getId()
      * 
      */
     public AvatarId getId() {
         return AvatarId.DIALOG_PLATFORM_SIGNUP_ACCOUNT;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.firstrun.SignupPage#getNextPageName()
      * @see com.thinkparity.ophelia.browser.platform.firstrun.SignupAccountInfoAvatar#isNextOk()
      * 
      */
     public String getNextPageName() {
         final FeatureSet featureSet = extractFeatureSet();
         switch (featureSet) {
         case FREE:
             return getPageName(AvatarId.DIALOG_PLATFORM_SIGNUP_SUMMARY);
         case PREMIUM:
             if (platform.isInternal()) {
                 return getPageName(AvatarId.DIALOG_PLATFORM_SIGNUP_PAYMENT_PLAN);
             } else {
                 return getPageName(AvatarId.DIALOG_PLATFORM_SIGNUP_PAYMENT);
             }
         default:
             throw Assert.createUnreachable("Unknown feature set {0}.", featureSet);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.firstrun.SignupPage#getPreviousPageName()
      * 
      */
     public String getPreviousPageName() {
         return getPageName(AvatarId.DIALOG_PLATFORM_SIGNUP_PROFILE);
     }
 
     /**
      * The next page is determined by the feature set; if the feature set is
      * "free"; a guest profile is created here and the next page should be the
      * login page; otherwise nothing is done here and the next page should be
      * the payment info page
      * 
      * @see com.thinkparity.ophelia.browser.platform.firstrun.SignupPage#isNextOk()
      */
     public Boolean isNextOk() {
         if (!isInputValid()) {
             return Boolean.FALSE;
         }
         createReservations();
         if (containsInputErrors()) {
             return Boolean.FALSE;
         } else {
             switch (extractFeatureSet()) {
             case FREE:
                 createGuestProfile();
                 if (containsInputErrors()) {
                     return Boolean.FALSE;
                 }
                 break;
             case PREMIUM:
                 break;
             default:
                 throw Assert.createUnreachable("Unknown feature set {0}.",
                         extractFeatureSet());
             }
         }
         return Boolean.TRUE;
     }
 
     /**
      * Create a guest profile.
      * 
      */
     private void createGuestProfile() {
         saveData();
         signupDelegate.enableNextButton(Boolean.FALSE);
         SwingUtil.setCursor(this, java.awt.Cursor.WAIT_CURSOR);
         errorMessageJLabel.setText(getString("SigningUp"));
         errorMessageJLabel.paintImmediately(0, 0, errorMessageJLabel.getWidth(),
                 errorMessageJLabel.getHeight());
         try {
             getSignupHelper().createGuestProfile();
         } catch (final OfflineException ox) {
            logger.logError(ox, "An offline error has occured.");
             addInputError(getSharedString("ErrorOffline"));
         } catch (final ReservationExpiredException rex) {
             logger.logWarning(rex, "The username/e-mail reservation has expired.");
             addInputError(getString("ErrorReservationExpired"));
         } catch (final Throwable t) {
            logger.logFatal(t, "An unexpected error has occured.");
             addInputError(getSharedString("ErrorUnexpected"));
         } finally {
             errorMessageJLabel.setText(" ");
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#reload()
      */
     @Override
     public void reload() {
         reloadAccountTypeRadioButtons();
         validateInput();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.firstrun.SignupPage#saveData()
      */
     public void saveData() {
         ((Data) input).set(SignupData.DataKey.FEATURE_SET, extractFeatureSet());
         ((Data) input).set(SignupData.DataKey.EMAIL, extractEMail());
         ((Data) input).set(SignupData.DataKey.CREDENTIALS, extractCredentials());
         ((Data) input).set(SignupData.DataKey.USERNAME_RESERVATION, usernameReservations.get(extractUsername().toLowerCase()));
         ((Data) input).set(SignupData.DataKey.EMAIL_RESERVATION, emailReservations.get(extractEMail()));
         ((Data) input).set(SignupData.DataKey.SECURITY_QUESTION, extractSecurityQuestion());
         ((Data) input).set(SignupData.DataKey.SECURITY_ANSWER, extractSecurityAnswer());
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.firstrun.DefaultSignupPage#setDefaultFocus()
      */
     @Override
     public void setDefaultFocus() {
         accountTypeStandardJRadioButton.requestFocusInWindow();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#validateInput()
      *
      */
     @Override
     public final void validateInput() {
         validateInput(Boolean.FALSE);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#isInputValid()
      */
     @Override
     protected Boolean isInputValid() {
         validateInput(Boolean.TRUE);
         return !containsInputErrors();
     }
 
     /**
      * Add validation listeners.
      */
     private void addValidationListeners() {
         addValidationListener(emailJTextField);
         addValidationListener(usernameJTextField);
         addValidationListener(passwordJPasswordField);
         addValidationListener(confirmPasswordJPasswordField);
         addValidationListener(securityAnswerJTextField);
 
         // NOTE The following ensures validateInput() is called as keyboard edits are made in the combo box.
         if (null != getSecurityQuestionDocument()) {
             getSecurityQuestionDocument().addDocumentListener(new DocumentListener() {
                 public void changedUpdate(final DocumentEvent e) {
                     validateInput();
                 }
                 public void insertUpdate(final DocumentEvent e) {
                     validateInput();
                 }
                 public void removeUpdate(final DocumentEvent e) {
                     validateInput();
                 }
             });
         }
     }
 
     /**
      * Create an e-mail address reservation.
      * 
      * @param email
      *            An <code>EMail</code> address.
      * @return A <code>EMailReservation</code>.
      */
     private EMailReservation createEMailReservation(final EMail email) {
         if (emailReservations.containsKey(email)) {
             return emailReservations.get(email);
         } else {
             return ((SignupProvider) contentProvider).createEMailReservation(email);
         }
     }
 
     /**
      * Create the username and e-mail address reservations.
      *
      */
     private void createReservations() {
         final String username = extractUsername();
         final EMail email = extractEMail();
         if (!isReserved(username) || !isReserved(email)) {
             signupDelegate.enableNextButton(Boolean.FALSE);
             SwingUtil.setCursor(this, java.awt.Cursor.WAIT_CURSOR);
             UsernameReservation usernameReservation = null;
             EMailReservation emailReservation = null;
             try {
                 errorMessageJLabel.setText(getString("CheckingUsername"));
                 errorMessageJLabel.paintImmediately(0, 0, errorMessageJLabel
                         .getWidth(), errorMessageJLabel.getHeight());
 
                 // get username reservation
                 usernameReservation = createUsernameReservation(username);
                 if (null == usernameReservation) {
                     unacceptableUsername = username;
                 } else {
                     usernameReservations.put(username.toLowerCase(), usernameReservation);
                 }
 
                 // get email reservation
                 emailReservation = createEMailReservation(email);
                 if (null == emailReservation) {
                     unacceptableEMail = email;
                 } else {
                     emailReservations.put(email, emailReservation);
                 }
             } catch (final OfflineException ox) {
                logger.logError(ox, "An offline error has occured.");
                 temporaryError = getSharedString("ErrorOffline");
             } catch (final Throwable t) {
                logger.logError(t, "An unexpected error has occured.");
                 temporaryError = getSharedString("ErrorUnexpected");
             } finally {
                 SwingUtil.setCursor(this, null);
             }
             validateInput();
         }
     }
 
     /**
      * Create a username reservation.
      * 
      * @param username
      *            A username <code>String</code>.
      * @return A <code>UsernameReservation</code>.
      */
     private UsernameReservation createUsernameReservation(final String username) {
         final String lowerCaseUsername = username.toLowerCase();
         if (usernameReservations.containsKey(lowerCaseUsername)) {
             return usernameReservations.get(lowerCaseUsername);
         } else {
             return ((SignupProvider) contentProvider)
                     .createUsernameReservation(lowerCaseUsername);
         }
     }
 
     /**
      * Extract the confirm password from the control.
      * 
      * @return The password <code>String</code>.
      */
     private String extractConfirmPassword() {
         return SwingUtil.extract(confirmPasswordJPasswordField, Boolean.TRUE);
     }
 
     /**
      * Extract the credentials.
      * 
      * @return An instance of <code>Credentials</code>.
      */
     private Credentials extractCredentials() {
         final Credentials credentials = new Credentials();
         credentials.setPassword(extractPassword());
         credentials.setUsername(extractUsername().toLowerCase());
         return credentials;
     }
 
     /**
      * Extract the input e-mail address string from the control.
      * 
      * @return The e-mail address <code>String</code>.
      */
     private EMail extractEMail() {
         final String emailAddress = extractEMailAddress();
         if (null == emailAddress) {
             return null;
         } else {
             try {
                 return EMailBuilder.parse(emailAddress);
             } catch (final EMailFormatException emfx) {
                 return null;
             }
         }
     }
 
     /**
      * Extract the input e-mail address string from the control.
      * 
      * @return The e-mail address <code>String</code>.
      */
     private String extractEMailAddress() {
         return SwingUtil.extract(emailJTextField, Boolean.TRUE);
     }
 
     /**
      * Extract the feature set based upon the account type selection.
      * 
      * @return A <code>FeatureSet</code>.
      */
     private FeatureSet extractFeatureSet() {
         if (accountTypeGuestJRadioButton.isSelected()) {
             return FeatureSet.FREE;
         } else if (accountTypeStandardJRadioButton.isSelected()) {
             return FeatureSet.PREMIUM;
         } else {
             throw Assert.createUnreachable("Unknown account type selected.");
         }
     }
 
     /**
      * Extract the password from the control.
      * 
      * @return The password <code>String</code>.
      */
     private String extractPassword() {
         return SwingUtil.extract(passwordJPasswordField, Boolean.TRUE);
     }
 
     /**
      * Extract the security answer from the control.
      * 
      * @return The security answer <code>String</code>.
      */
     private String extractSecurityAnswer() {
         return SwingUtil.extract(securityAnswerJTextField, Boolean.TRUE);
     }
 
     /**
      * Extract the security question from the control.
      * 
      * @return The security question <code>String</code>.
      */
     private String extractSecurityQuestion() {
         // NOTE JComboBox.getSelectedItem() will not catch keyboard edits as they are typed,
         // so JComboBox.getEditor().getItem() is used.
         String securityQuestion = ((String) securityQuestionJComboBox.getEditor().getItem()).trim();
         if (isEmpty(securityQuestion)) {
             return null;
         } else {
             return securityQuestion;
         }
     }
 
     /**
      * Extract the username from the control.
      * 
      * @return The username <code>String</code>.
      */
     private String extractUsername() {
         return SwingUtil.extract(usernameJTextField, Boolean.TRUE);
     }
 
     /**
      * Get the document for the security question control
      * 
      * @return The <code>AbstractDocument</code>.
      */
     private Document getSecurityQuestionDocument() {
         final Component editorComponent = securityQuestionJComboBox.getEditor().getEditorComponent();
         if (editorComponent instanceof JTextField) {
             return ((JTextField) editorComponent).getDocument();
         } else {
             return null;
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         final javax.swing.ButtonGroup accountTypeButtonGroup = new javax.swing.ButtonGroup();
         final javax.swing.JLabel accountTypeTitleJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel loginInfoTitleJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel emailJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel usernameJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel passwordJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel confirmPasswordJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel securityTitleJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel securityQuestionJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel securityAnswerJLabel = new javax.swing.JLabel();
         final javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
         final javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
 
         setOpaque(false);
         accountTypeTitleJLabel.setFont(Fonts.DialogFont);
         accountTypeTitleJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.AccountTypeTitle"));
 
         accountTypeButtonGroup.add(accountTypeGuestJRadioButton);
         accountTypeGuestJRadioButton.setFont(Fonts.DialogFont);
         accountTypeGuestJRadioButton.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.AccountTypeGuest"));
         accountTypeGuestJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         accountTypeGuestJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
         accountTypeGuestJRadioButton.setOpaque(false);
 
         accountTypeButtonGroup.add(accountTypeStandardJRadioButton);
         accountTypeStandardJRadioButton.setFont(Fonts.DialogFont);
         accountTypeStandardJRadioButton.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.AccountTypeStandard"));
         accountTypeStandardJRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         accountTypeStandardJRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
         accountTypeStandardJRadioButton.setOpaque(false);
 
         learnMoreJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.LearnMore"));
         learnMoreJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 learnMoreJLabelMousePressed(evt);
             }
         });
 
         loginInfoTitleJLabel.setFont(Fonts.DialogFont);
         loginInfoTitleJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.LoginInfoTitle"));
 
         emailJLabel.setFont(Fonts.DialogFont);
         emailJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.EmailLabel"));
 
         emailJTextField.setFont(Fonts.DialogTextEntryFont);
         emailJTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));
         emailJTextField.setMinimumSize(new java.awt.Dimension(300, 20));
         emailJTextField.setPreferredSize(new java.awt.Dimension(300, 20));
         ((AbstractDocument) emailJTextField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(profileConstraints.getEmail()));
 
         usernameJLabel.setFont(Fonts.DialogFont);
         usernameJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.UsernameLabel"));
 
         usernameJTextField.setFont(Fonts.DialogTextEntryFont);
         usernameJTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));
         usernameJTextField.setMinimumSize(new java.awt.Dimension(300, 20));
         usernameJTextField.setPreferredSize(new java.awt.Dimension(300, 20));
         ((AbstractDocument) usernameJTextField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(profileConstraints.getUsername()));
 
         passwordJLabel.setFont(Fonts.DialogFont);
         passwordJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.PasswordLabel"));
 
         passwordJPasswordField.setFont(Fonts.DialogTextEntryFont);
         passwordJPasswordField.setMaximumSize(new java.awt.Dimension(300, 2147483647));
         passwordJPasswordField.setMinimumSize(new java.awt.Dimension(300, 20));
         passwordJPasswordField.setPreferredSize(new java.awt.Dimension(300, 20));
         ((AbstractDocument) passwordJPasswordField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(profileConstraints.getPassword()));
 
         confirmPasswordJLabel.setFont(Fonts.DialogFont);
         confirmPasswordJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.ConfirmPasswordLabel"));
 
         confirmPasswordJPasswordField.setFont(Fonts.DialogTextEntryFont);
         confirmPasswordJPasswordField.setMaximumSize(new java.awt.Dimension(300, 2147483647));
         confirmPasswordJPasswordField.setMinimumSize(new java.awt.Dimension(300, 20));
         confirmPasswordJPasswordField.setPreferredSize(new java.awt.Dimension(300, 20));
         ((AbstractDocument) confirmPasswordJPasswordField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(profileConstraints.getPassword()));
 
         securityTitleJLabel.setFont(Fonts.DialogFont);
         securityTitleJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.SecurityTitle"));
 
         securityQuestionJLabel.setFont(Fonts.DialogFont);
         securityQuestionJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.SecurityQuestion"));
 
         securityQuestionJComboBox.setEditable(true);
         securityQuestionJComboBox.setFont(Fonts.DialogTextEntryFont);
         securityQuestionJComboBox.setModel(securityQuestionModel);
         securityQuestionJComboBox.setMaximumSize(new java.awt.Dimension(300, 32767));
         securityQuestionJComboBox.setMinimumSize(new java.awt.Dimension(300, 18));
         securityQuestionJComboBox.setPreferredSize(new java.awt.Dimension(300, 20));
         if (null != getSecurityQuestionDocument()) {
             ((AbstractDocument) getSecurityQuestionDocument()).setDocumentFilter(new JTextFieldLengthFilter(profileConstraints.getSecurityQuestion()));
         }
 
         securityAnswerJLabel.setFont(Fonts.DialogFont);
         securityAnswerJLabel.setText(java.util.ResourceBundle.getBundle("localization/Browser_Messages").getString("SignupAvatar.AccountInfo.SecurityAnswer"));
 
         securityAnswerJTextField.setFont(Fonts.DialogTextEntryFont);
         securityAnswerJTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));
         securityAnswerJTextField.setMinimumSize(new java.awt.Dimension(300, 20));
         securityAnswerJTextField.setPreferredSize(new java.awt.Dimension(300, 20));
         ((AbstractDocument) securityAnswerJTextField.getDocument()).setDocumentFilter(new JTextFieldLengthFilter(profileConstraints.getSecurityAnswer()));
 
         errorMessageJLabel.setFont(Fonts.DialogFont);
         errorMessageJLabel.setForeground(Colours.DIALOG_ERROR_TEXT_FG);
         errorMessageJLabel.setText("!Error Message!");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(26, 26, 26)
                         .addComponent(accountTypeGuestJRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(55, 55, 55)
                         .addComponent(learnMoreJLabel))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(26, 26, 26)
                         .addComponent(accountTypeStandardJRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(26, 26, 26)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(confirmPasswordJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(emailJLabel)
                             .addComponent(passwordJLabel)
                             .addComponent(usernameJLabel))
                         .addGap(33, 33, 33)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(emailJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(usernameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(passwordJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(confirmPasswordJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(26, 26, 26)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(securityQuestionJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(securityAnswerJLabel))
                         .addGap(26, 26, 26)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(securityAnswerJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(securityQuestionJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(securityTitleJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(accountTypeTitleJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(loginInfoTitleJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(errorMessageJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {securityAnswerJTextField, securityQuestionJComboBox});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(27, 27, 27)
                 .addComponent(accountTypeTitleJLabel)
                 .addGap(14, 14, 14)
                 .addComponent(accountTypeStandardJRadioButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(learnMoreJLabel)
                     .addComponent(accountTypeGuestJRadioButton))
                 .addGap(15, 15, 15)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(loginInfoTitleJLabel)
                 .addGap(14, 14, 14)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(emailJLabel)
                     .addComponent(emailJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(usernameJLabel)
                     .addComponent(usernameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(passwordJLabel)
                     .addComponent(passwordJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(confirmPasswordJLabel)
                     .addComponent(confirmPasswordJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(15, 15, 15)
                 .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(securityTitleJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(15, 15, 15)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(securityQuestionJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(securityQuestionJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(securityAnswerJLabel)
                     .addComponent(securityAnswerJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(19, 19, 19)
                 .addComponent(errorMessageJLabel)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Initialize the focus listener.
      */
     private void initFocusListener() {
         final FocusListener focusListener = new FocusListener() {
             public void focusGained(FocusEvent e) {
                 validateInput();
             }
             public void focusLost(FocusEvent e) {
                 validateInput();
             }
         };
         emailJTextField.addFocusListener(focusListener);
         usernameJTextField.addFocusListener(focusListener);
         passwordJPasswordField.addFocusListener(focusListener);
         confirmPasswordJPasswordField.addFocusListener(focusListener);
     }
 
     /**
      * Initialize the list of questions for the security question picklist.
      */
     private void initSecurityQuestionModel() {
         for (Integer i = 1; i <= NUMBER_SECURITY_QUESTIONS; i++) {
             final StringBuffer key = new StringBuffer("SecurityQuestion.").append(i.toString());
             securityQuestionModel.addElement(getString(key.toString()));
         }
     }
 
     /**
      * Determine if the input email is valid.
      * 
      * @return True if the input email is valid; false otherwise.
      */
     private Boolean isEmailAddressValid() {
         final String emailAddress = extractEMailAddress();
         if (null == emailAddress) {
             return Boolean.FALSE;
         } else {
             try {
                 return null != EMailBuilder.parse(emailAddress);
             } catch (final EMailFormatException emfx) {
                 return Boolean.FALSE;
             }
         }
     }
 
     /**
      * Determine if the email has been reserved.
      * 
      * @param email
      *            A <code>EMail</code> address.
      * @return True if the e-mail address has been reserved, false otherwise.
      */
     private Boolean isReserved(final EMail email) {
         return emailReservations.containsKey(email);
     }
 
     /**
      * Determine if the username has been reserved.
      * 
      * @param username
      *            A username <code>String</code>.
      * @return True if the username has been reserved, false otherwise.
      */
     private Boolean isReserved(final String username) {
         return usernameReservations.containsKey(username.toLowerCase());
     }
 
     private void learnMoreJLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_learnMoreJLabelMousePressed
         platform.runLearnMore(LearnMore.Topic.SIGNUP);
     }//GEN-LAST:event_learnMoreJLabelMousePressed
 
     /**
      * Reload the account type radio buttons.
      */
     private void reloadAccountTypeRadioButtons() {
         accountTypeStandardJRadioButton.setSelected(true);
     }
 
     /**
      * Validate input.
      * 
      * @param ignoreFocus
      *            A <code>Boolean</code> to ignore focus or not.
      */
     private void validateInput(final Boolean ignoreFocus) {
         super.validateInput();
         final String emailAddress = extractEMailAddress();
         final String username = extractUsername();
         final String password = extractPassword();
         final String confirmPassword = extractConfirmPassword();
         final String securityQuestion = extractSecurityQuestion();
         final String securityAnswer = extractSecurityAnswer();
 
         if (null == emailAddress) {
             addInputError(Separator.Space.toString());
         } else if (!isEmailAddressValid()) {
             if (ignoreFocus || !emailJTextField.isFocusOwner()) {
                 addInputError(getString("ErrorInvalidEmail"));
             }
         }
         if (null != emailAddress && null != unacceptableEMail
                 && emailAddress.equalsIgnoreCase(unacceptableEMail.toString())) {
             addInputError(getString("ErrorEMailTaken"));
         }
 
         final int minimumUsernameLength = profileConstraints.getUsername().getMinLength();
         if (null == username) {
             addInputError(Separator.Space.toString());
         } else if (username.length() < minimumUsernameLength) {
             if (ignoreFocus || !usernameJTextField.isFocusOwner()) {
                 addInputError(getString("ErrorUsernameTooShort", new Object[] {minimumUsernameLength}));
             }
         }
         if (null != username && null != unacceptableUsername
                 && username.equalsIgnoreCase(unacceptableUsername)) {
             addInputError(getString("ErrorUsernameTaken"));
         }
 
         final int minimumPasswordLength = profileConstraints.getPassword().getMinLength();
         if (null == password) {
             addInputError(Separator.Space.toString());
         } else if (password.length() < minimumPasswordLength) {
             if (ignoreFocus || !passwordJPasswordField.isFocusOwner()) {
                 addInputError(getString("ErrorPasswordTooShort", new Object[] {minimumPasswordLength}));
             }
         }
 
         if (null == confirmPassword) {
             addInputError(Separator.Space.toString());
         } else if (null != password && !password.equals(confirmPassword)) {
             if (ignoreFocus || !confirmPasswordJPasswordField.isFocusOwner()) {
                 addInputError(getString("ErrorPasswordsDoNotMatch"));
             }
         }
 
         if (null == securityQuestion) {
             addInputError(Separator.Space.toString());
         }
 
         if (null == securityAnswer) {
             addInputError(Separator.Space.toString());
         }
 
         // enable or disable next button based on errors, but note that
         // temporary errors do not disable the next button
         if (isSignupDelegateInitialized()) {
             signupDelegate.enableNextButton(!containsInputErrors());
         }
 
         // show temporary errors once
         if (null != temporaryError) {
             addInputError(temporaryError);
             temporaryError = null;
         }
 
         // display error message
         errorMessageJLabel.setText(" ");
         if (containsInputErrors()) {
             errorMessageJLabel.setText(getInputErrors().get(0));
         }
     }
 }
