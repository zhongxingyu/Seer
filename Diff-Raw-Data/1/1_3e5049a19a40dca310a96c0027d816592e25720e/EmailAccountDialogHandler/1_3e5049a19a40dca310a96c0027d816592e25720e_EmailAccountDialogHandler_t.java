 /**
  * 
  */
 package net.frontlinesms.ui.handler.email;
 
 import org.apache.log4j.Logger;
 
 import net.frontlinesms.EmailSender;
 import net.frontlinesms.EmailServerHandler;
 import net.frontlinesms.FrontlineSMS;
 import net.frontlinesms.Utils;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.EmailAccount;
 import net.frontlinesms.data.repository.EmailAccountDao;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.handler.keyword.EmailActionDialog;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.TextResourceKeyOwner;
 
 /**
  * @author aga
  *
  */
 @TextResourceKeyOwner
 public class EmailAccountDialogHandler implements ThinletUiEventHandler {
 //> UI LAYOUT FILES
 	public static final String UI_FILE_EMAIL_ACCOUNTS_SETTINGS_FORM = "/ui/core/email/dgServerConfig.xml";
 	public static final String UI_FILE_CONNECTION_WARNING_FORM = "/ui/core/email/dgConnectionWarning.xml";
 	public static final String UI_FILE_EMAIL_ACCOUNT_FORM = "/ui/core/email/dgAccountSettings.xml";
 	
 //> UI COMPONENT NAMES
 	public static final String COMPONENT_ACCOUNTS_LIST = "accountsList";
 	public static final String COMPONENT_TF_ACCOUNT = "tfAccount";
 	public static final String COMPONENT_TF_ACCOUNT_PASS = "tfAccountPass";
 	public static final String COMPONENT_TF_ACCOUNT_SERVER_PORT = "tfPort";
 	public static final String COMPONENT_TF_MAIL_SERVER = "tfMailServer";
 	public static final String COMPONENT_CB_USE_SSL = "cbUseSSL";
 	
 //> I18N TEXT KEYS
 	public static final String I18N_EDITING_EMAIL_ACCOUNT = "common.editing.email.account";
 	public static final String I18N_ACCOUNT_NAME_BLANK = "message.account.name.blank";
 	public static final String I18N_ACCOUNT_NAME_ALREADY_EXISTS = "message.account.already.exists";
 
 //> INSTANCE PROPERTIES
 	/** Logger */
 	private Logger LOG = Utils.getLogger(this.getClass());
 	
 	private UiGeneratorController ui;
 	private EmailAccountDao emailAccountDao;
 	/** Manager of {@link EmailAccount}s and {@link EmailSender}s */
 	private EmailServerHandler emailManager;
 	
 	private Object dialogComponent;
 	
 	public EmailAccountDialogHandler(UiGeneratorController ui) {
 		this.ui = ui;
 		FrontlineSMS frontlineController = ui.getFrontlineController();
 		this.emailAccountDao = frontlineController.getEmailAccountFactory();
 		this.emailManager = frontlineController.getEmailServerHandler();
 	}
 	
 	public Object getDialog() {
 		initDialog();
 		return this.dialogComponent;
 	}
 	
 	private void initDialog() {
 		this.dialogComponent = ui.loadComponentFromFile(UI_FILE_EMAIL_ACCOUNTS_SETTINGS_FORM, this);
 		this.refreshAccountsList();
 	}
 
 	private void refreshAccountsList() {
 		Object table = find(COMPONENT_ACCOUNTS_LIST);
 		this.ui.removeAll(table);
 		for (EmailAccount acc : emailAccountDao.getAllEmailAccounts()) {
 			this.ui.add(table, ui.getRow(acc));
 		}
 	}
 
 //> UI EVENT METHODS
 	/**
 	 * Shows the email accounts settings dialog.
 	 */
 	public void showEmailAccountsSettings(Object dialog) {
 		ui.setAttachedObject(this.dialogComponent, dialog);
 		ui.add(this.dialogComponent);
 	}
 	
 	public void finishEmailManagement(Object dialog) {
 		Object att = ui.getAttachedObject(dialog);
 		if (att != null) {
 			Object list = ui.find(att, COMPONENT_ACCOUNTS_LIST);
 			ui.removeAll(list);
 			for (EmailAccount acc : emailAccountDao.getAllEmailAccounts()) {
 				Object item = ui.createListItem(acc.getAccountName(), acc);
 				ui.setIcon(item, Icon.SERVER);
 				ui.add(list, item);
 			}
 		}
 		ui.removeDialog(dialog);
 	}
 	
 	/**
 	 * After failing to connect to the email server, the user has an option to
 	 * create the account anyway. This method handles this action. 
 	 * 
 	 * @param currentDialog
 	 */
 	public void createAccount(Object currentDialog) {
 		LOG.trace("ENTER");
 		ui.removeDialog(currentDialog);
 		LOG.debug("Creating account anyway!");
 		Object accountDialog = ui.getAttachedObject(currentDialog);
 		String server = ui.getText(ui.find(accountDialog, COMPONENT_TF_MAIL_SERVER));
 		String accountName = ui.getText(ui.find(accountDialog, COMPONENT_TF_ACCOUNT));
 		String password = ui.getText(ui.find(accountDialog, COMPONENT_TF_ACCOUNT_PASS));
 		boolean useSSL = ui.isSelected(ui.find(accountDialog, COMPONENT_CB_USE_SSL));
 		String portAsString = ui.getText(ui.find(accountDialog, COMPONENT_TF_ACCOUNT_SERVER_PORT));
 		
 		int serverPort;
 		try {
 			serverPort = Integer.parseInt(portAsString);
 		} catch (NumberFormatException e1) {
 			if (useSSL) serverPort = EmailAccount.DEFAULT_SMTPS_PORT;
 			else serverPort = EmailAccount.DEFAULT_SMTP_PORT;
 		}
 		
 		Object table = ui.find(accountDialog, COMPONENT_ACCOUNTS_LIST);
 		
 		LOG.debug("Server Name [" + server + "]");
 		LOG.debug("Account Name [" + accountName + "]");
 		LOG.debug("Account Server Port [" + serverPort + "]");
 		LOG.debug("SSL [" + useSSL + "]");
 		EmailAccount acc;
 		try {
 			acc = new EmailAccount(accountName, server, serverPort, password, useSSL);
 			emailAccountDao.saveEmailAccount(acc);
 		} catch (DuplicateKeyException e) {
 			LOG.debug("Account already exists", e);
 			ui.alert(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_ALREADY_EXISTS));
 			LOG.trace("EXIT");
 			return;
 		}
 		LOG.debug("Account [" + acc.getAccountName() + "] created!");
 		//ui.add(table, ui.getRow(acc));
 		this.refreshAccountsList();
 		cleanEmailAccountFields(accountDialog);
 		LOG.trace("EXIT");
 	}
 	
 	/**
 	 * This method is called when the save button is pressed in the new mail account dialog. 
 	 
 	 * @param dialog
 	 */
 	public void saveEmailAccount(Object dialog) {
 		LOG.trace("ENTER");
 		String server = ui.getText(ui.find(dialog, COMPONENT_TF_MAIL_SERVER));
 		String accountName = ui.getText(ui.find(dialog, COMPONENT_TF_ACCOUNT));
 		String password = ui.getText(ui.find(dialog, COMPONENT_TF_ACCOUNT_PASS));
 		boolean useSSL = ui.isSelected(ui.find(dialog, COMPONENT_CB_USE_SSL));
 		String portAsString = ui.getText(ui.find(dialog, COMPONENT_TF_ACCOUNT_SERVER_PORT));
 		
 		int serverPort;
 		try {
 			serverPort = Integer.parseInt(portAsString);
 		} catch (NumberFormatException e1) {
 			if (useSSL) serverPort = EmailAccount.DEFAULT_SMTPS_PORT;
 			else serverPort = EmailAccount.DEFAULT_SMTP_PORT;
 		}
 		
 		Object table = ui.find(dialog, COMPONENT_ACCOUNTS_LIST);
 		
 		LOG.debug("Server [" + server + "]");
 		LOG.debug("Account [" + accountName + "]");
 		LOG.debug("Account Server Port [" + serverPort + "]");
 		LOG.debug("SSL [" + useSSL + "]");
 		
 		if (accountName.equals("")) {
 			ui.alert(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_BLANK));
 			LOG.trace("EXIT");
 			return;
 		}
 		
 		try {
 			Object att = ui.getAttachedObject(dialog);
 			if (att == null || !(att instanceof EmailAccount)) {
 				LOG.debug("Testing connection to [" + server + "]");
 				if (EmailSender.testConnection(server, accountName, serverPort, password, useSSL)) {
 					LOG.debug("Connection was successful, creating account [" + accountName + "]");
 					EmailAccount account = new EmailAccount(accountName, server, serverPort, password, useSSL);
 					emailAccountDao.saveEmailAccount(account);
 					ui.add(table, ui.getRow(account));
 					cleanEmailAccountFields(dialog);
 				} else {
 					LOG.debug("Connection failed.");
 					Object connectWarning = ui.loadComponentFromFile(UI_FILE_CONNECTION_WARNING_FORM, this);
 					ui.setAttachedObject(connectWarning, dialog);
 					ui.add(connectWarning);
 				}
 			} else if (att instanceof EmailAccount) {
 				EmailAccount acc = (EmailAccount) att;
 				acc.setAccountName(accountName);
 				acc.setAccountPassword(password);
 				acc.setAccountServer(server);
 				acc.setUseSSL(useSSL);
 				acc.setAccountServerPort(serverPort);
				emailAccountDao.updateEmailAccount(acc);
 				
 				Object tableToAdd = ui.find(ui.find("emailConfigDialog"), COMPONENT_ACCOUNTS_LIST);
 				int index = ui.getSelectedIndex(tableToAdd);
 				ui.remove(ui.getSelectedItem(tableToAdd));
 				ui.add(tableToAdd, ui.getRow(acc), index);
 				
 				ui.setSelectedIndex(tableToAdd, index);
 				
 				ui.removeDialog(dialog);
 			}
 			
 		} catch (DuplicateKeyException e) {
 			LOG.debug(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_ALREADY_EXISTS), e);
 			ui.alert(InternationalisationUtils.getI18NString(I18N_ACCOUNT_NAME_ALREADY_EXISTS));
 		}
 		LOG.trace("EXIT");
 	}
 	
 	public void showEmailAccountDialog(Object list) {
 		Object selected = ui.getSelectedItem(list);
 		if (selected != null) {
 			EmailAccount acc = (EmailAccount) ui.getAttachedObject(selected);
 			showEmailAccountDialog(acc);
 		}
 	}
 	
 	/**
 	 * Enables or disables menu options in a List Component's popup list
 	 * and toolbar.  These enablements are based on whether any items in
 	 * the list are selected, and if they are, on the nature of these
 	 * items.
 	 * @param list 
 	 * @param popup 
 	 * @param toolbar
 	 * 
 	 * TODO check where this is used, and make sure there is no dead code
 	 */
 	public void enableOptions(Object list, Object popup, Object toolbar) {
 		Object[] selectedItems = ui.getSelectedItems(list);
 		boolean hasSelection = selectedItems.length > 0;
 
 		if(popup!= null && !hasSelection && "emailServerListPopup".equals(ui.getName(popup))) {
 			ui.setVisible(popup, false);
 			return;
 		}
 		
 		if (hasSelection && popup != null) {
 			// If nothing is selected, hide the popup menu
 			ui.setVisible(popup, hasSelection);
 		}
 		
 		if (toolbar != null && !toolbar.equals(popup)) {
 			for (Object o : ui.getItems(toolbar)) {
 				ui.setEnabled(o, hasSelection);
 			}
 		}
 	}
 	
 	/**
 	 * Removes the selected accounts.
 	 */
 	public void removeSelectedFromAccountList() {
 		LOG.trace("ENTER");
 		ui.removeConfirmationDialog();
 		Object list = find(COMPONENT_ACCOUNTS_LIST);
 		Object[] selected = ui.getSelectedItems(list);
 		for (Object o : selected) {
 			EmailAccount acc = ui.getAttachedObject(o, EmailAccount.class);
 			LOG.debug("Removing Account [" + acc.getAccountName() + "]");
 			emailManager.serverRemoved(acc);
 			emailAccountDao.deleteEmailAccount(acc);
 		}
 		
 		this.refreshAccountsList();
 		LOG.trace("EXIT");
 	}
 
 //> UI PASSTHROUGH METHODS
 	/** @see UiGeneratorController#showConfirmationDialog(String, Object) */
 	public void showConfirmationDialog(String methodToBeCalled) {
 		this.ui.showConfirmationDialog(methodToBeCalled, this);
 	}
 	/**
 	 * @param page page to show
 	 * @see UiGeneratorController#showHelpPage(String)
 	 */
 	public void showHelpPage(String page) {
 		this.ui.showHelpPage(page);
 	}
 	/** @see UiGeneratorController#removeDialog(Object) */
 	public void removeDialog(Object dialog) {
 		this.ui.removeDialog(dialog);
 	}
 	
 //> UI HELPER METHODS
 	/**
 	 * Find a UI component within the {@link #dialogComponent}.
 	 * @param componentName the name of the UI component
 	 * @return the ui component, or <code>null</code> if it could not be found
 	 */
 	private Object find(String componentName) {
 		return ui.find(this.dialogComponent, componentName);
 	}
 
 	private void cleanEmailAccountFields(Object accountDialog) {
 		ui.setText(ui.find(accountDialog, COMPONENT_TF_MAIL_SERVER), "");
 		ui.setText(ui.find(accountDialog, COMPONENT_TF_ACCOUNT), "");
 		ui.setText(ui.find(accountDialog, COMPONENT_TF_ACCOUNT_PASS), "");
 		ui.setText(ui.find(accountDialog, COMPONENT_TF_ACCOUNT_SERVER_PORT), "");
 		ui.setSelected(ui.find(accountDialog, COMPONENT_CB_USE_SSL), true);
 	}
 	
 	/**
 	 * Event fired when the view phone details action is chosen.
 	 */
 	private void showEmailAccountDialog(EmailAccount acc) {
 		Object settingsDialog = ui.loadComponentFromFile(UI_FILE_EMAIL_ACCOUNT_FORM, this);
 		ui.setText(settingsDialog, InternationalisationUtils.getI18NString(I18N_EDITING_EMAIL_ACCOUNT, acc.getAccountName()));
 		
 		Object tfServer = ui.find(settingsDialog, COMPONENT_TF_MAIL_SERVER);
 		Object tfAccountName = ui.find(settingsDialog, COMPONENT_TF_ACCOUNT);
 		Object tfPassword = ui.find(settingsDialog, COMPONENT_TF_ACCOUNT_PASS);
 		Object cbUseSSL = ui.find(settingsDialog, COMPONENT_CB_USE_SSL);
 		Object tfPort = ui.find(settingsDialog, COMPONENT_TF_ACCOUNT_SERVER_PORT);
 		
 		ui.setText(tfServer, acc.getAccountServer());
 		ui.setText(tfAccountName, acc.getAccountName());
 		ui.setText(tfPassword, acc.getAccountPassword());
 		ui.setSelected(cbUseSSL, acc.useSsl());
 		ui.setText(tfPort, String.valueOf(acc.getAccountServerPort()));
 		
 		ui.setAttachedObject(settingsDialog, acc);
 		ui.add(settingsDialog);
 	}
 }
