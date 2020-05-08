 /**
  * 
  */
 package net.frontlinesms.ui.handler.email;
 
 import javax.mail.MessagingException;
 
 import net.frontlinesms.FrontlineSMS;
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.EmailAccount;
 import net.frontlinesms.data.repository.EmailAccountDao;
 import net.frontlinesms.email.EmailUtils;
import net.frontlinesms.ui.FrontlineUI;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.TextResourceKeyOwner;
 
 import org.apache.log4j.Logger;
 
 /**
  * Dialog handler for adding/editing Email accounts
  * @author Morgan Belkadi <morgan@frontlinesms.com>
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 @TextResourceKeyOwner(prefix="I18N_")
 public class EmailAccountSettingsDialogHandler implements ThinletUiEventHandler {
 //> STATIC CONSTANTS
 	
 //> THINLET LAYOUT DEFINITION FILES
 	/** UI XML File Path: This is the outline for the dialog */
 	private static final String UI_FILE_EMAIL_ACCOUNT_FORM = "/ui/core/email/dgAccountSettings.xml";
 	private static final String UI_FILE_CONNECTION_WARNING_FORM = "/ui/core/email/dgConnectionWarning.xml";
 	private static final String UI_FILE_CONNECTION_WARNING_ERROR_MESSAGE = "/ui/core/email/dgConnectionWarningError.xml";
 	
 //> THINLET COMPONENT NAMES
 
 	private static final String UI_COMPONENT_CB_USE_SSL = "cbUseSSL";
 	private static final String UI_COMPONENT_LB_EXAMPLE_PORT = "lbPortExample";
 	private static final String UI_COMPONENT_LB_EXAMPLE_SERVER = "lbServerExample";
 	private static final String UI_COMPONENT_LK_HELP = "lkHelp";
 	private static final String UI_COMPONENT_RB_IMAP = "rbImap";
 	private static final String UI_COMPONENT_RB_POP = "rbPop";
 	private static final String UI_COMPONENT_PN_POP_IMAP = "pnPopImap";
 	private static final String UI_COMPONENT_TA_ERROR_MESSAGE = "taErrorMessage";
 	private static final String UI_COMPONENT_TF_ACCOUNT = "tfAccount";
 	private static final String UI_COMPONENT_TF_ACCOUNT_PASS = "tfAccountPass";
 	private static final String UI_COMPONENT_TF_ACCOUNT_SERVER_PORT = "tfPort";
 	private static final String UI_COMPONENT_TF_MAIL_SERVER = "tfMailServer";
 	
 //> I18N TEXT KEYS
 
 	private static final String I18N_ACCOUNT_NAME_BLANK = "message.account.name.blank";
 	private static final String I18N_ACCOUNT_NAME_ALREADY_EXISTS = "message.account.already.exists";
 	private static final String I18N_COMMON_EMAIL_ACCOUNT_SETTINGS = "common.email.account.settings";
 	private static final String I18N_EDITING_EMAIL_ACCOUNT = "common.editing.email.account";
 	private static final String I18N_MMS_EMAIL_ACCOUNT_SETTINGS = "mms.email.account.settings";
 	private static final String I18N_NEW_ACCOUNT = "common.new.account";
 	
 	private static final String EXAMPLE_PORT_POP = "email.account.example.port.pop";
 	private static final String EXAMPLE_PORT_SMTP = "email.account.example.port.smtp";
 	private static final String EXAMPLE_SERVER_POP = "email.account.example.server.pop";
 	private static final String EXAMPLE_SERVER_SMTP = "email.account.example.server.smtp";
 	
 	private static final int DEFAULT_PORT_POP = 110;
 	private static final int DEFAULT_PORT_SMTP = 25;
 	
 //> INSTANCE PROPERTIES
 	/** Logging object */
 	private final Logger log = FrontlineUtils.getLogger(this.getClass());
 	/** The {@link UiGeneratorController} that shows the tab. */
 	private final UiGeneratorController ui;
 	
 	private Object dialogComponent;
 	
 	private boolean isForReceiving;
 	private EmailAccountDao emailAccountDao;
 	private EmailAccount originalEmailAccount;
 	private String connectionWarningMessage;
 
 //> CONSTRUCTORS
 	/**
 	 * Create a new instance of this controller.
 	 * @param uiController 
 	 * @param notification the notification which triggered this dialog; used to determine what to display in the dialog
 	 */
 	public EmailAccountSettingsDialogHandler(UiGeneratorController uiController, boolean isForReceiving) {
 		this.ui = uiController;
 		this.isForReceiving = isForReceiving;
 		
 		FrontlineSMS frontlineController = ui.getFrontlineController();
 		this.emailAccountDao = frontlineController.getEmailAccountFactory();
 	}
 	
 //> ACCESSORS
 
 //> UI SHOW METHODS
 	public Object getDialog() {
 		return this.dialogComponent;
 	}
 	
 	/**
 	 * Setup the details of the dialog.
 	 */
 	public void initDialog(EmailAccount emailAccount) {
 		this.dialogComponent = ui.loadComponentFromFile(UI_FILE_EMAIL_ACCOUNT_FORM, this);
 		this.originalEmailAccount = emailAccount; 
 		
 		Object tfPort = ui.find(dialogComponent, UI_COMPONENT_TF_ACCOUNT_SERVER_PORT);
 		
 		this.ui.setSelected(find(UI_COMPONENT_RB_IMAP), true);
 		this.ui.setSelected(find(UI_COMPONENT_RB_POP), false);
 		
 		if (this.isForReceiving) {
 			ui.setText(find(UI_COMPONENT_LB_EXAMPLE_PORT), InternationalisationUtils.getI18NString(EXAMPLE_PORT_POP));
 			ui.setText(find(UI_COMPONENT_LB_EXAMPLE_SERVER), InternationalisationUtils.getI18NString(EXAMPLE_SERVER_POP));
 			ui.setText(tfPort, String.valueOf(DEFAULT_PORT_POP));
 			this.ui.setText(dialogComponent, InternationalisationUtils.getI18NString(I18N_MMS_EMAIL_ACCOUNT_SETTINGS));
 			if (this.originalEmailAccount != null && this.originalEmailAccount.getProtocol().equals(EmailUtils.POP3)) {
 				this.ui.setSelected(find(UI_COMPONENT_RB_IMAP), false);
 				this.ui.setSelected(find(UI_COMPONENT_RB_POP), true);
 			}
 		} else {
 			ui.setText(dialogComponent, InternationalisationUtils.getI18NString(I18N_NEW_ACCOUNT));
 			ui.setText(find(UI_COMPONENT_LB_EXAMPLE_PORT), InternationalisationUtils.getI18NString(EXAMPLE_PORT_SMTP));
 			ui.setText(find(UI_COMPONENT_LB_EXAMPLE_SERVER), InternationalisationUtils.getI18NString(EXAMPLE_SERVER_SMTP));
 			ui.setText(tfPort, String.valueOf(DEFAULT_PORT_SMTP));
 			this.ui.setText(dialogComponent, InternationalisationUtils.getI18NString(I18N_COMMON_EMAIL_ACCOUNT_SETTINGS));
 		}
 		
 		this.ui.setVisible(find(UI_COMPONENT_PN_POP_IMAP), isForReceiving);
 		this.ui.setVisible(find(UI_COMPONENT_LK_HELP), isForReceiving);
 		
 		if (emailAccount != null) {
 			ui.setText(dialogComponent, InternationalisationUtils.getI18NString(I18N_EDITING_EMAIL_ACCOUNT, emailAccount.getAccountName()));
 			this.populatePanel();
 		}
 	}
 
 	/** Populate the panel containing settings specific to the currently-selected {@link EmailAccount}. */
 	private void populatePanel() {		
 		Object tfServer = ui.find(dialogComponent, UI_COMPONENT_TF_MAIL_SERVER);
 		Object tfAccountName = ui.find(dialogComponent, UI_COMPONENT_TF_ACCOUNT);
 		Object tfPassword = ui.find(dialogComponent, UI_COMPONENT_TF_ACCOUNT_PASS);
 		Object cbUseSSL = ui.find(dialogComponent, UI_COMPONENT_CB_USE_SSL);
 		Object tfPort = ui.find(dialogComponent, UI_COMPONENT_TF_ACCOUNT_SERVER_PORT);
 		
 		ui.setText(tfServer, this.originalEmailAccount.getAccountServer());
 		ui.setText(tfAccountName, this.originalEmailAccount.getAccountName());
 		ui.setText(tfPassword, this.originalEmailAccount.getAccountPassword());
 		ui.setSelected(cbUseSSL, this.originalEmailAccount.useSsl());
 		ui.setText(tfPort, String.valueOf(this.originalEmailAccount.getAccountServerPort()));
 	}
 	
 	private Object find(String componentName) {
 		return ui.find(this.dialogComponent, componentName);
 	}
 
 //> UI EVENT METHODS
 	
 	/**
 	 * After failing to connect to the email server, the user has an option to
 	 * create the account anyway. This method handles this action. 
 	 * 
 	 * @param currentDialog
 	 */
 	public void createAccount(Object currentDialog) {
 		log.trace("ENTER");
 		ui.removeDialog(currentDialog);
 		log.debug("Creating account anyway!");
 		Object accountDialog = ui.getAttachedObject(currentDialog);
 		String server = ui.getText(ui.find(accountDialog, UI_COMPONENT_TF_MAIL_SERVER));
 		String accountName = ui.getText(ui.find(accountDialog, UI_COMPONENT_TF_ACCOUNT));
 		String password = ui.getText(ui.find(accountDialog, UI_COMPONENT_TF_ACCOUNT_PASS));
 		boolean useSSL = ui.isSelected(ui.find(accountDialog, UI_COMPONENT_CB_USE_SSL));
 		String portAsString = ui.getText(ui.find(accountDialog, UI_COMPONENT_TF_ACCOUNT_SERVER_PORT));
 		
 		int serverPort;
 		try {
 			serverPort = Integer.parseInt(portAsString);
 		} catch (NumberFormatException e1) {
 			if (useSSL) serverPort = EmailAccount.DEFAULT_SMTPS_PORT;
 			else serverPort = EmailAccount.DEFAULT_SMTP_PORT;
 		}
 		
 		
 		log.debug("Server Name [" + server + "]");
 		log.debug("Account Name [" + accountName + "]");
 		log.debug("Account Server Port [" + serverPort + "]");
 		log.debug("SSL [" + useSSL + "]");
 		try {
 			if (this.originalEmailAccount == null) { // Then it's a new account
 				EmailAccount acc = new EmailAccount(accountName, server, serverPort, password, useSSL, this.isForReceiving, getCurrentProtocol());
 				
 				emailAccountDao.saveEmailAccount(acc);
 				log.debug("Account [" + acc.getAccountName() + "] created!");
 			} else { // Then we're editing the account
 				this.originalEmailAccount.setAccountName(accountName);
 				this.originalEmailAccount.setAccountServer(server);
 				this.originalEmailAccount.setAccountServerPort(serverPort);
 				this.originalEmailAccount.setAccountPassword(password);
 				this.originalEmailAccount.setUseSSL(useSSL);
 				this.originalEmailAccount.setProtocol(getCurrentProtocol());
 				// We're not setting the isForReceiving flag, as it must never change
 				
 				emailAccountDao.updateEmailAccount(originalEmailAccount);
 				log.debug("Account [" + accountName + "] updated!");
 			}
 		} catch (DuplicateKeyException e) {
 			log.debug("Account already exists", e);
 			ui.alert(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_ALREADY_EXISTS));
 			log.trace("EXIT");
 			return;
 		}
 		log.trace("EXIT");
 		
 		this.removeDialog(dialogComponent);
 	}
 	
 	private String getCurrentProtocol() {
 		return (!this.isForReceiving ? EmailUtils.SMTP 
 				: (ui.isSelected(ui.find(UI_COMPONENT_RB_POP)) ? EmailUtils.POP3 : EmailUtils.IMAP));
 	}
 
 	/**
 	 * This method is called when the save button is pressed in the new mail account dialog. 
 	 
 	 * @param dialog
 	 */
 	public void saveEmailAccount(Object dialog) {
 		log.trace("ENTER");
 		String server = ui.getText(find(UI_COMPONENT_TF_MAIL_SERVER));
 		String accountName = ui.getText(find(UI_COMPONENT_TF_ACCOUNT));
 		String password = ui.getText(find(UI_COMPONENT_TF_ACCOUNT_PASS));
 		boolean useSSL = ui.isSelected(find(UI_COMPONENT_CB_USE_SSL));
 		String portAsString = ui.getText(find(UI_COMPONENT_TF_ACCOUNT_SERVER_PORT));
 		String protocol = getCurrentProtocol();
 		
 		int serverPort;
 		try {
 			serverPort = Integer.parseInt(portAsString);
 		} catch (NumberFormatException e1) {
 			if (useSSL) serverPort = EmailAccount.DEFAULT_SMTPS_PORT;
 			else serverPort = EmailAccount.DEFAULT_SMTP_PORT;
 		}
 		
 		log.debug("Server [" + server + "]");
 		log.debug("Account [" + accountName + "]");
 		log.debug("Account Server Port [" + serverPort + "]");
 		log.debug("SSL [" + useSSL + "]");
 		
 		if (accountName.equals("")) {
 			ui.alert(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_BLANK));
 			log.trace("EXIT");
 			return;
 		}
 		
 		try {
 			log.debug("Testing connection to [" + server + "]");
 			EmailUtils.testConnection(isForReceiving, server, accountName, serverPort, password, useSSL, protocol); // Exception catched if failed 
 			log.debug("Connection was successful, creating account [" + accountName + "]");
 			
 			if (this.originalEmailAccount == null) {
 				EmailAccount account = new EmailAccount(accountName, server, serverPort, password, useSSL, this.isForReceiving, protocol);
 				emailAccountDao.saveEmailAccount(account);
 			} else {
 				this.originalEmailAccount.setAccountName(accountName);
 				this.originalEmailAccount.setAccountServer(server);
 				this.originalEmailAccount.setAccountServerPort(serverPort);
 				this.originalEmailAccount.setAccountPassword(password);
 				this.originalEmailAccount.setUseSSL(useSSL);
 				this.originalEmailAccount.setProtocol(protocol);
 				// We're not setting the isForReceiving flag, as it must never change
 				
 				emailAccountDao.updateEmailAccount(this.originalEmailAccount);
 			}
 			
 			ui.removeDialog(this.dialogComponent);
 		} catch (MessagingException e) {
 			log.info("Fail to connect to server [" + server + "]");
 			log.debug("Fail to connect to server [" + server + "]", e);
 			this.connectionWarningMessage = e.getMessage();
 			if (e.getNextException() != null) {
 				this.connectionWarningMessage += e.getNextException();
 			}
 			this.showConnectionWarningDialog(dialog);
 		}  catch (DuplicateKeyException e) {
 			log.debug(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_ALREADY_EXISTS), e);
 			ui.alert(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_ALREADY_EXISTS));
 		}
 		log.trace("EXIT");
 	}
 	
 	private void showConnectionWarningDialog(Object dialog) {
 		Object connectWarning = ui.loadComponentFromFile(UI_FILE_CONNECTION_WARNING_FORM, this);
 		ui.setAttachedObject(connectWarning, dialog);
 		ui.add(connectWarning);
 	}
 
 	/** @param dialog the dialog to remove
 	 * @see UiGeneratorController#remove(Object) */
 	public void removeDialog(Object dialog) {
 		this.ui.closeDeviceConnectionDialog(dialog);
 	}
 	
 	public void showDetails () {
 		Object errorPanel = ui.loadComponentFromFile(UI_FILE_CONNECTION_WARNING_ERROR_MESSAGE, this);
 		ui.setText(this.ui.find(errorPanel, UI_COMPONENT_TA_ERROR_MESSAGE), this.connectionWarningMessage);
 		ui.add(errorPanel);
 	}
	
	/**
	 * Opens a page of the help manual
	 * @see FrontlineUI#showHelpPage(String)
	 */
	public void showHelpPage(String page) {
		this.ui.showHelpPage(page);
	}
 
 }
