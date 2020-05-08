 /**
  * 
  */
 package net.frontlinesms.ui.handler.keyword;
 
 import static net.frontlinesms.FrontlineSMSConstants.ACTION_ADD_KEYWORD;
 import static net.frontlinesms.FrontlineSMSConstants.ACTION_CREATE;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_AUTO_FORWARD_FOR_KEYWORD;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_AUTO_JOIN_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_AUTO_LEAVE_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_BLANK;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_EDITING_KEYWORD;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_KEYWORD;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_KEYWORD_ACTIONS_OF;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_TO_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_UNDEFINED;
 import static net.frontlinesms.FrontlineSMSConstants.DEFAULT_END_DATE;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_BLANK_RECIPIENTS;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_KEYWORD_EXISTS;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_KEYWORD_SAVED;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_NO_ACCOUNT_SELECTED_TO_SEND_FROM;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_NO_CONTACT_SELECTED;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_NO_GROUP_CREATED_BY_USERS;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_NO_GROUP_SELECTED_TO_FWD;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_START_DATE_AFTER_END;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_WRONG_FORMAT_DATE;
 import static net.frontlinesms.FrontlineSMSConstants.SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_ACTION_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_BT_CLEAR;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_BT_SAVE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_BT_SENDER_NAME;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_ACTION_TYPE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_AUTO_REPLY;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_FORWARD;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_GROUPS_TO_JOIN;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_GROUPS_TO_LEAVE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_JOIN_GROUP;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CB_LEAVE_GROUP;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_EXTERNAL_COMMAND_GROUP_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_FORWARD_FORM_GROUP_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_FORWARD_FORM_TEXTAREA;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_FORWARD_FORM_TITLE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUP_SELECTER_GROUP_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUP_SELECTER_OK_BUTTON;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUP_SELECTER_TITLE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_KEYWORDS_DIVIDER;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_KEYWORD_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_KEY_ACT_PANEL;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_KEY_PANEL;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MAIL_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_CREATE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_KEYWORD_BUTTON_DONE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_KEYWORD_FORM_DESCRIPTION;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_KEYWORD_FORM_KEYWORD;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_KEYWORD_FORM_TITLE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_PN_BOTTOM;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_PN_RESPONSE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_PN_TIP;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RB_FRONTLINE_COMMANDS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RB_NO_RESPONSE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RB_PLAIN_TEXT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RB_TYPE_COMMAND_LINE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RB_TYPE_HTTP;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_AUTO_REPLY;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_COMMAND;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_END_DATE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_KEYWORD;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_MESSAGE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_RECIPIENT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_START_DATE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_SUBJECT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.UI_FILE_GROUP_SELECTER;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.UI_FILE_SENDER_NAME_PANEL;
 
 import java.text.ParseException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import thinlet.Thinlet;
 
 import net.frontlinesms.FrontlineSMS;
 import net.frontlinesms.Utils;
 import net.frontlinesms.csv.CsvUtils;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.EmailAccount;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.domain.Keyword;
 import net.frontlinesms.data.domain.KeywordAction;
 import net.frontlinesms.data.repository.EmailAccountDao;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.data.repository.KeywordActionDao;
 import net.frontlinesms.data.repository.KeywordDao;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.handler.BaseTabHandler;
 import net.frontlinesms.ui.handler.ContactSelecter;
 import net.frontlinesms.ui.handler.message.MessagePanelHandler;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * @author Alex Anderson 
  * <li> alex(at)masabi(dot)com
  * @author Carlos Eduardo Genz
  * <li> kadu(at)masabi(dot)com
  */
 public class KeywordTabHandler extends BaseTabHandler {
 //> UI LAYOUT FILES
 	public static final String UI_FILE_KEYWORDS_TAB = "/ui/core/keyword/keywordsTab.xml";
 	public static final String UI_FILE_KEYWORDS_SIMPLE_VIEW = "/ui/core/keyword/pnSimpleView.xml";
 	public static final String UI_FILE_KEYWORDS_ADVANCED_VIEW = "/ui/core/keyword/pnAdvancedView.xml";
 	public static final String UI_FILE_NEW_KEYWORD_FORM = "/ui/core/keyword/newKeywordForm.xml";
 	public static final String UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM = "/ui/core/keyword/externalCommandDialog.xml";
 	public static final String UI_FILE_NEW_KACTION_EMAIL_FORM = "/ui/core/keyword/dgEmailKAction.xml";
 
 	private EmailAccountDao emailAccountDao;
 	private GroupDao groupDao;
 	private KeywordDao keywordDao;
 	private KeywordActionDao keywordActionDao;
 
 	private Object keywordListComponent;
 	
 	/** The number of people the current SMS will be sent to */
 	private int numberToSend = 1;
 	
 	public KeywordTabHandler(UiGeneratorController ui, FrontlineSMS frontlineController) {
 		super(ui);
 		this.emailAccountDao = frontlineController.getEmailAccountFactory();
 		this.groupDao = frontlineController.getGroupDao();
 		this.keywordDao = frontlineController.getKeywordDao();
 		this.keywordActionDao = frontlineController.getKeywordActionDao(); 
 	}
 
 	protected Object initialiseTab() {
 		Object tabComponent = ui.loadComponentFromFile(UI_FILE_KEYWORDS_TAB, this);
 		this.keywordListComponent = ui.find(tabComponent, COMPONENT_KEYWORD_LIST);
 		return tabComponent;
 	}
 
 	public void refresh() {
 		updateKeywordList();
 	}
 	
 //> UI EVENT METHODS
 	/**
 	 * Shows the export wizard dialog for exporting contacts.
 	 * @param list The list to get selected items from.
 	 */
 	public void showExportWizard(Object list) {
 		this.ui.showExportWizard(list, "keywords");
 	}
 	
 	public void autoReplyChanged(String reply, Object cbAutoReply) {
 		ui.setSelected(cbAutoReply, reply.length() > 0);
 	}
 	
 	/**
      *  0 - Auto Reply
      *  1 - Auto Forward
      *  2 - Join Group
      *  3 - Leave Group
      *  4 - Survey
      *  5 - E-mail
      *  6 - External Command
      *  
      *  TODO MAKE SURE THAT THIS STILL WORKS - WE HAVE REMOVED THE SURVEY ACTION!!!
      */
 	public void keywordTab_createAction(int index) {
 		switch (index) {
 		case 0:
 			show_newKActionReplyForm(keywordListComponent);
 			break;
 		case 1:
 			show_newKActionForwardForm(keywordListComponent);
 			break;
 		case 2:
 			show_newKActionJoinForm(keywordListComponent);
 			break;
 		case 3:
 			show_newKActionLeaveForm(keywordListComponent);
 			break;
 		case 4:
 			show_newKActionEmailForm(keywordListComponent);
 			break;
 		case 5:
 			show_newKActionExternalCmdForm(keywordListComponent);
 			break;
 		}
 	}
 	
 	/**
 	 * Shows the new forward message action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_newKActionForwardForm(Object keywordList) {
 		Keyword keyword = ui.getKeyword(ui.getSelectedItem(keywordList));
 		ForwardActionDialog dialog = new ForwardActionDialog(ui, this);
 		dialog.init(keyword);
 		dialog.show();
 	}
 
 	/**
 	 * Shows the new auto reply action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_newKActionReplyForm(Object keywordList) {
 		ReplyActionDialogHandler replyActionDialogHandler = new ReplyActionDialogHandler(ui, this);
 		replyActionDialogHandler.init(ui.getKeyword(ui.getSelectedItem(keywordList)));
 		replyActionDialogHandler.show();
 	}
 	
 	/**
 	 * Shows the new email action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_newKActionEmailForm(Object keywordList) {
 		log.trace("ENTER");
 		Keyword keyword = ui.getKeyword(ui.getSelectedItem(keywordList));
 		Object emailForm = ui.loadComponentFromFile(UI_FILE_NEW_KACTION_EMAIL_FORM, this);
 		ui.setAttachedObject(emailForm, keyword);
 		//Adds the date panel to it
 		ui.addDatePanel(emailForm);
 		Object list = ui.find(emailForm, COMPONENT_MAIL_LIST);
 		for (EmailAccount acc : emailAccountDao.getAllEmailAccounts()) {
 			log.debug("Adding existent e-mail account [" + acc.getAccountName() + "] to list");
 			Object item = ui.createListItem(acc.getAccountName(), acc);
 			ui.setIcon(item, Icon.SERVER);
 			ui.add(list, item);
 		}
 		ui.add(emailForm);
 		log.trace("EXIT");
 	}
 
 	public void keywordShowAdvancedView() {
 		Object divider = find(COMPONENT_KEYWORDS_DIVIDER);
 		if (ui.getItems(divider).length >= 2) {
 			ui.remove(ui.getItems(divider)[ui.getItems(divider).length - 1]);
 		}
 		Object panel = ui.loadComponentFromFile(UI_FILE_KEYWORDS_ADVANCED_VIEW, this);
 		Object table = ui.find(panel, COMPONENT_ACTION_LIST);
 		Keyword keyword = ui.getKeyword(ui.getSelectedItem(keywordListComponent));
 		String key = keyword.getKeyword().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeyword();
 		ui.setText(panel, InternationalisationUtils.getI18NString(COMMON_KEYWORD_ACTIONS_OF, key));
 		for (KeywordAction action : this.keywordActionDao.getActions(keyword)) {
 			ui.add(table, ui.getRow(action));
 		}
 		enableKeywordActionFields(table, ui.find(panel, COMPONENT_KEY_ACT_PANEL));
 		ui.add(divider, panel);
 	}
 	
 	/**
 	 * UI Method.
 	 * Deletes the keyword that is selected in {@link #keywordListComponent}.
 	 */
 	public void removeSelectedFromKeywordList() {
 		// Get the selected keyword
 		Object selected = ui.getSelectedItem(keywordListComponent);
 		Keyword keyword = ui.getAttachedObject(selected, Keyword.class);
 		
 		// Delete attached actions, and then delete they keyword
 		for(KeywordAction action : this.keywordActionDao.getActions(keyword)) {
 			this.keywordActionDao.deleteKeywordAction(action);
 		}
 		this.keywordDao.deleteKeyword(keyword);
 
 		// Now update the UI - remove the selected item and set a new selected item
 		ui.remove(selected);
 		ui.setSelectedIndex(keywordListComponent, 0);
 		showSelectedKeyword();
 
 		// Finally, remove the "confirm delete" dialog
 		ui.removeConfirmationDialog();
 	}
 	
 	/**
 	 * Event fired when the popup menu (in the keyword manager tab) is shown.
 	 * If there is no keyword listed in the tree, the only option allowed is
 	 * to create one. Otherwise, all components are allowed.
 	 */
 	public void enableKeywordFields(Object component) {
 		log.trace("ENTER");
 		int selected = ui.getSelectedIndex(keywordListComponent);
 		String field = Thinlet.getClass(component) == Thinlet.PANEL ? Thinlet.ENABLED : Thinlet.VISIBLE;
 		if (selected <= 0) {
 			log.debug("Nothing selected, so we only allow keyword creation.");
 			for (Object o : ui.getItems(component)) {
 				String name = ui.getString(o, Thinlet.NAME);
 				if (name == null)
 					continue;
 				if (!name.equals(COMPONENT_MENU_ITEM_CREATE)) {
 					ui.setBoolean(o, field, false);
 				} else {
 					ui.setBoolean(o, field, true);
 				}
 			}
 		} else {
 			//Keyword selected
 			for (Object o : ui.getItems(component)) {
 				ui.setBoolean(o, field, true);
 			}
 		}
 		log.trace("EXIT");
 	}
 	
 	public void keywordTab_newAction(Object combo) {
 		keywordTab_createAction(ui.getSelectedIndex(combo));
 	}
 	/**
 	 * Creates a new leave group action.
 	 */
 	public void do_newKActionLeave(Object groupSelecterDialog, Object groupList) {
 		createActionLeaveOrJoin(groupSelecterDialog, groupList, false);
 	}
 
 	/**
 	 * Shows the new keyword dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_createKeywordForm(Object keywordList) {
 		showNewKeywordForm(ui.getKeyword(ui.getSelectedItem(keywordList)));
 	}
 
 	/**
 	 * Create a new keyword with the supplied information (newKeyword and description).
 	 * 
 	 * @param formPanel The panel to be removed from the application.
 	 * @param newKeyword The desired keyword.
 	 * @param description The description for this new keyword.
 	 */
 	public void do_createKeyword(Object formPanel, String newKeyword, String description) {
 		log.trace("ENTER");
 		log.debug("Creating keyword [" + newKeyword + "] with description [" + description + "]");
 		try {
 			// Trim the keyword to remove trailing and leading whitespace
 			newKeyword = newKeyword.trim();
 			// Remove any double-spaces within the keyword
 			newKeyword = newKeyword.replaceAll("\\s+", " ");
 			
 			Keyword keyword = new Keyword(newKeyword, description);
 			this.keywordDao.saveKeyword(keyword);
 		} catch (DuplicateKeyException e) {
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_EXISTS));
 			log.trace("EXIT");
 			return;
 		}
 		updateKeywordList();
 		ui.remove(formPanel);
 		log.trace("EXIT");
 	}	public void keywordTab_doSave(Object panel) {
 		log.trace("ENTER");
 		long startDate;
 		try {
 			startDate = InternationalisationUtils.parseDate(InternationalisationUtils.getDefaultStartDate()).getTime();
 		} catch (ParseException e) {
 			log.debug("We never should get this", e);
 			log.trace("EXIT");
 			return;
 		}
 		
 		// Get the KeywordAction details
 		String replyText = keywordSimple_getAutoReply(panel);
 		Group joinGroup = keywordSimple_getJoin(panel);
 		Group leaveGroup = keywordSimple_getLeave(panel);
 		
 		// Get the keyword attached to the selected item.  If the "Add Keyword" option is selected,
 		// there will be no keyword attached to it.
 		Keyword keyword = null;
 		Object selectedKeywordItem = ui.getSelectedItem(keywordListComponent);
 		if(selectedKeywordItem != null) keyword = ui.getKeyword(selectedKeywordItem);
 		
 		if (keyword == null) {
 			//Adding keyword as well as actions
 			String newkeyword = ui.getText(ui.find(panel, COMPONENT_TF_KEYWORD));
 			try {
 				keyword = new Keyword(newkeyword, "");
 				this.keywordDao.saveKeyword(keyword);
 			} catch (DuplicateKeyException e) {
 				ui.alert(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_EXISTS));
 				log.trace("EXIT");
 				return;
 			}
 			keywordTab_doClear(panel);
 		} else {
 			// Editing an existent keyword.  This keyword may already have actions applied to it, so
 			// we need to check for actions and update them as appropriate.
 			KeywordAction replyAction = this.keywordActionDao.getAction(keyword, KeywordAction.TYPE_REPLY);
 			if (replyAction != null) {
 				if (replyText == null) {
 					// The reply action has been removed
 					keywordActionDao.deleteKeywordAction(replyAction);
 				} else {
 					replyAction.setReplyText(replyText);
 					this.keywordActionDao.updateKeywordAction(replyAction);
 					//We set null to don't add it in the end
 					replyText = null;
 				}
 			}
 			
 			KeywordAction joinAction = this.keywordActionDao.getAction(keyword, KeywordAction.TYPE_JOIN);
 			if (joinAction != null) {
 				if (joinGroup == null) {
 					// Previous join action has been removed, so delete it.
 					keywordActionDao.deleteKeywordAction(joinAction);
 				} else {
 					// Group to join has been updated
 					joinAction.setGroup(joinGroup);
 					this.keywordActionDao.updateKeywordAction(joinAction);
 					// Join Group has been handled, so unset it.
 					joinGroup = null;
 				}
 			}
 			
 			KeywordAction leaveAction = this.keywordActionDao.getAction(keyword, KeywordAction.TYPE_LEAVE);
 			if (leaveAction != null) {
 				if (leaveGroup == null) {
 					keywordActionDao.deleteKeywordAction(leaveAction);
 				} else {
 					leaveAction.setGroup(leaveGroup);
 					this.keywordActionDao.updateKeywordAction(leaveAction);
 					//We set null to don't add it in the end
 					leaveGroup = null;
 				}
 			}
 		}
 		
 		// Handle creation of new KeywordActions if required
 		if (replyText != null) {
 			KeywordAction action = KeywordAction.createReplyAction(keyword, replyText, startDate, DEFAULT_END_DATE);
 			keywordActionDao.saveKeywordAction(action);
 		}
 		if (joinGroup != null) {
 			KeywordAction action = KeywordAction.createGroupJoinAction(keyword, joinGroup, startDate, DEFAULT_END_DATE);
 			keywordActionDao.saveKeywordAction(action);
 		}
 		if (leaveGroup != null) {
 			KeywordAction action = KeywordAction.createGroupLeaveAction(keyword, leaveGroup, startDate, DEFAULT_END_DATE);
 			keywordActionDao.saveKeywordAction(action);
 		}
 		
 		// Refresh the UI
 		updateKeywordList();
 		ui.setStatus(InternationalisationUtils.getI18NString(MESSAGE_KEYWORD_SAVED));
 		log.trace("EXIT");
 	}
 
 	/**
 	 * Removes selected keyword action.
 	 */
 	public void removeSelectedFromKeywordActionsList() {
 		ui.removeConfirmationDialog();
 		Object list = find(COMPONENT_ACTION_LIST);
 		Object selected = ui.getSelectedItem(list);
 		KeywordAction keyAction = ui.getAttachedObject(selected, KeywordAction.class);
 		this.keywordActionDao.deleteKeywordAction(keyAction);
 		ui.remove(selected);
 		enableKeywordActionFields(list, find(COMPONENT_KEY_ACT_PANEL));
 	}
 
 	/**
 	 * Event fired when the popup menu (in the keyword manager tab) is shown.
 	 * If there is no keyword action listed in the table, the only option allowed is
 	 * to create one. Otherwise, all components are allowed.
 	 */
 	public void enableKeywordActionFields(Object table, Object component) {
 		log.trace("ENTER");
 		int selected = ui.getSelectedIndex(table);
 		String field = Thinlet.getClass(component) == Thinlet.PANEL ? Thinlet.ENABLED : Thinlet.VISIBLE;
 		if (selected < 0) {
 			log.debug("Nothing selected, so we only allow keyword action creation.");
 			for (Object o : ui.getItems(component)) {
 				String name = ui.getString(o, Thinlet.NAME);
 				if (name == null)
 					continue;
 				if (!name.equals(COMPONENT_MENU_ITEM_CREATE)
 						&& !name.equals(COMPONENT_CB_ACTION_TYPE)) {
 					ui.setBoolean(o, field, false);
 				} else {
 					ui.setBoolean(o, field, true);
 				}
 			}
 		} else {
 			//Keyword action selected
 			for (Object o : ui.getItems(component)) {
 				ui.setBoolean(o, field, true);
 			}
 		}
 		log.trace("EXIT");
 	}
 	
 	/**
 	 * Shows the new join group action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_newKActionJoinForm(Object keywordList) {
 		Keyword keyword = ui.getKeyword(ui.getSelectedItem(keywordList));
 		showGroupSelecter(keyword, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + keyword.getKeyword() + "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_JOIN_GROUP) + ":", "do_newKActionJoin(groupSelecter, groupSelecter_groupList)", this);
 	}
 	
 	/**
 	 * Shows the new leave group action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_newKActionLeaveForm(Object keywordList) {
 		Keyword keyword = ui.getKeyword(ui.getSelectedItem(keywordList));
 		showGroupSelecter(keyword, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + keyword.getKeyword() + "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_LEAVE_GROUP) + ":", "do_newKActionLeave(groupSelecter, groupSelecter_groupList)", this);
 	}
 	
 	/**
 	 * Creates a email message action.
 	 */
 	public void do_newKActionEmail(Object mailDialog, Object mailList) {
 		log.trace("ENTER");
 		String message = ui.getText(ui.find(mailDialog, COMPONENT_TF_MESSAGE));
 		String recipients = ui.getText(ui.find(mailDialog, COMPONENT_TF_RECIPIENT));
 		String subject = ui.getText(ui.find(mailDialog, COMPONENT_TF_SUBJECT));
 		log.debug("Message [" + message + "]");
 		log.debug("Recipients [" + recipients + "]");
 		log.debug("Subject [" + subject + "]");
 		if (recipients.equals("") || recipients.equals(";")) {
 			log.debug("No valid recipients.");
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_BLANK_RECIPIENTS));
 			return;
 		}
 		EmailAccount account = (EmailAccount) ui.getAttachedObject(ui.getSelectedItem(mailList));
 		if (account == null) {
 			log.debug("No account selected to send the e-mail from.");
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_ACCOUNT_SELECTED_TO_SEND_FROM));
 			return;
 		}
 		log.debug("Account [" + account.getAccountName() + "]");
 		String startDate = ui.getText(ui.find(mailDialog, COMPONENT_TF_START_DATE));
 		String endDate = ui.getText(ui.find(mailDialog, COMPONENT_TF_END_DATE));
 		log.debug("Start Date [" + startDate + "]");
 		log.debug("End Date [" + endDate + "]");
 		if (startDate.equals("")) {
 			log.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
 			startDate = InternationalisationUtils.getDefaultStartDate();
 		}
 		long start;
 		long end;
 		try {
 			Date ds = InternationalisationUtils.parseDate(startDate); 
 			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
 				Date de = InternationalisationUtils.parseDate(endDate);
 				if (!Utils.validateDates(ds, de)) {
 					log.debug("Start date is not before the end date");
 					ui.alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
 					log.trace("EXIT");
 					return;
 				}
 				end = de.getTime();
 			} else {
 				end = DEFAULT_END_DATE;
 			}
 			start = ds.getTime();
 		} catch (ParseException e) {
 			log.debug("Wrong format for date", e);
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
 			log.trace("EXIT");
 			return;
 		} 
 		KeywordAction action = null;
 		boolean isNew = false;
 		if (ui.isAttachment(mailDialog, KeywordAction.class)) {
 			action = ui.getKeywordAction(mailDialog);
 			log.debug("We are editing action [" + action + "]. Setting new values.");
 			action.setEmailAccount(account);
 			action.setReplyText(message);
 			action.setEmailRecipients(recipients);
 			action.setEmailSubject(subject);
 			action.setStartDate(start);
 			action.setEndDate(end);
 		} else {
 			isNew = true;
 			Keyword keyword = ui.getKeyword(mailDialog);
 			log.debug("Creating new action  for keyword[" + keyword.getKeyword() + "].");
 			action = KeywordAction.createEmailAction(keyword, message, account, recipients, subject,start, end);
 			keywordActionDao.saveKeywordAction(action);
 		}
 		updateKeywordActionList(action, isNew);
 		ui.remove(mailDialog);
 		log.trace("EXIT");
 	}
 
 	/**
 	 * Creates a new forward message action.
 	 */
 	public void do_newKActionExternalCommand(Object externalCommandDialog) {
 		log.trace("ENTER");
 		String startDate = ui.getText(ui.find(externalCommandDialog, COMPONENT_TF_START_DATE));
 		String endDate = ui.getText(ui.find(externalCommandDialog, COMPONENT_TF_END_DATE));
 		log.debug("Start Date [" + startDate + "]");
 		log.debug("End Date [" + endDate + "]");
 		if (startDate.equals("")) {
 			log.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
 			startDate = InternationalisationUtils.getDefaultStartDate();
 		}
 		long start;
 		long end;
 		try {
 			Date ds = InternationalisationUtils.parseDate(startDate); 
 			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
 				Date de = InternationalisationUtils.parseDate(endDate);
 				if (!Utils.validateDates(ds, de)) {
 					log.debug("Start date is not before the end date");
 					ui.alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
 					log.trace("EXIT");
 					return;
 				}
 				end = de.getTime();
 			} else {
 				end = DEFAULT_END_DATE;
 			}
 			start = ds.getTime();
 		} catch (ParseException e) {
 			log.debug("Wrong format for date", e);
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
 			log.trace("EXIT");
 			return;
 		} 
 		int commandType = ui.isSelected(ui.find(externalCommandDialog, COMPONENT_RB_TYPE_HTTP)) ? KeywordAction.EXTERNAL_HTTP_REQUEST : KeywordAction.EXTERNAL_COMMAND_LINE;
 		String commandLine = ui.getText(ui.find(externalCommandDialog, COMPONENT_TF_COMMAND));
 		int responseType = KeywordAction.EXTERNAL_RESPONSE_DONT_WAIT;
 		if (ui.isSelected(ui.find(externalCommandDialog, COMPONENT_RB_PLAIN_TEXT))) {
 			responseType = KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT;
 		} else if (ui.isSelected(ui.find(externalCommandDialog, COMPONENT_RB_FRONTLINE_COMMANDS))) {
 			responseType = KeywordAction.EXTERNAL_RESPONSE_LIST_COMMANDS;
 		}
 		
 		log.debug("Command type [" + commandType + "]");
 		log.debug("Command [" + commandLine + "]");
 		log.debug("Response type [" + responseType + "]");
 		
 		Group group = null;
 		String message = null;
 		int responseActionType = KeywordAction.EXTERNAL_DO_NOTHING; 
 		if (responseType == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT) {
 			boolean reply = ui.isSelected(ui.find(externalCommandDialog, COMPONENT_CB_AUTO_REPLY));
 			boolean fwd = ui.isSelected(ui.find(externalCommandDialog, COMPONENT_CB_FORWARD));
 			
 			if (reply && fwd) {
 				responseActionType = KeywordAction.EXTERNAL_REPLY_AND_FORWARD;
 			} else if (reply) {
 				responseActionType = KeywordAction.TYPE_REPLY;
 			} else if (fwd) {
 				responseActionType = KeywordAction.TYPE_FORWARD;
 			}
 			log.debug("Response Action type [" + responseActionType + "]");
 			if (responseActionType == KeywordAction.TYPE_REPLY 
 					|| responseActionType == KeywordAction.TYPE_FORWARD
 					|| responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
 				message = ui.getText(ui.find(externalCommandDialog, COMPONENT_TF_MESSAGE));
 				log.debug("Message [" + message + "]");
 			}
 			if (responseActionType == KeywordAction.TYPE_FORWARD 
 					|| responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
 				group = ui.getGroup(ui.getSelectedItem(ui.find(externalCommandDialog, COMPONENT_EXTERNAL_COMMAND_GROUP_LIST)));
 				if (group == null) {
 					log.debug("No group selected to forward");
 					ui.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_SELECTED_TO_FWD));
 					log.trace("EXIT");
 					return;
 				}
 				log.debug("Group [" + group.getName() + "]");
 			}
 		}
 		KeywordAction action = null;
 		boolean isNew = false;
 		if (ui.isAttachment(externalCommandDialog, KeywordAction.class)) {
 			//Editing
 			action = ui.getKeywordAction(externalCommandDialog);
 			log.debug("We are editing action [" + action + "]. Setting new values.");
 			if (group != null) {
 				action.setGroup(group);
 			}
 			action.setCommandLine(commandLine);
 			action.setExternalCommandType(commandType);
 			action.setExternalCommandResponseType(responseType);
 			action.setCommandResponseActionType(responseActionType);
 			action.setCommandText(message);
 			action.setStartDate(start);
 			action.setEndDate(end);
 			keywordActionDao.updateKeywordAction(action);
 		} else {
 			isNew = true;
 			Keyword keyword = ui.getKeyword(externalCommandDialog);
 			log.debug("Creating new keyword action for keyword [" + keyword.getKeyword() + "]");
 			action = KeywordAction.createExternalCommandAction(
 					keyword,
 					commandLine,
 					commandType,
 					responseType,
 					responseActionType,
 					message,
 					group,
 					start,
 					end
 			);
 			keywordActionDao.saveKeywordAction(action);
 		}
 		updateKeywordActionList(action, isNew);
 		ui.remove(externalCommandDialog);
 		log.trace("EXIT");
 	}
 	
 	public void keywordTab_doClear(Object panel) {
 		ui.setText(ui.find(panel, COMPONENT_TF_KEYWORD), "");
 		ui.setSelected(ui.find(panel, COMPONENT_CB_AUTO_REPLY), false);
 		ui.setText(ui.find(panel, COMPONENT_TF_AUTO_REPLY), "");
 		ui.setSelected(ui.find(panel, COMPONENT_CB_JOIN_GROUP), false);
 		ui.setSelectedIndex(ui.find(panel, COMPONENT_CB_GROUPS_TO_JOIN), 0);
 		ui.setSelected(ui.find(panel, COMPONENT_CB_LEAVE_GROUP), false);
 		ui.setSelectedIndex(ui.find(panel, COMPONENT_CB_GROUPS_TO_LEAVE), 0);
 	}
 	
 	/**
 	 * Activates or deactivates the supplied panel according to user selection.
 	 * 
 	 * @param list
 	 * @param selected
 	 */
 	public void controlExternalCommandResponseType(Object list, boolean selected) {
 		if (selected) {
 			ui.activate(list);
 		} else {
 			ui.deactivate(list);
 		}
 	}
 	
 	/**
 	 * Method called when the user has selected the edit option inside the Keywords tab.
 	 * 
 	 * @param tree
 	 */
 	public void keywordManager_edit(Object tree) {
 		log.trace("ENTER");
 		Object selectedObj = ui.getSelectedItem(tree);
 		if (ui.isAttachment(selectedObj, KeywordAction.class)) {
 			//KEYWORD ACTION EDITION
 			KeywordAction action = ui.getKeywordAction(selectedObj);
 			log.debug("Editing keyword action [" + action + "]");
 			showActionEditDialog(action);
 		} else {
 			Keyword keyword = ui.getKeyword(selectedObj);
 			//KEYWORD EDITION
 			log.debug("Editing keyword [" + keyword.getKeyword() + "]");
 			showKeywordDialogForEdition(keyword);
 		} 
 		log.trace("EXIT");
 	}
 	
 	/**
 	 * Method called when the user has finished to edit a keyword.
 	 * 
 	 * @param dialog The dialog, which is holding the current reference to the keyword being edited.
 	 * @param desc The new description for the keyword.
 	 */
 	public void finishKeywordEdition(Object dialog, String desc) {
 		log.trace("ENTER");
 		Keyword key = ui.getKeyword(dialog);
 		log.debug("New description [" + desc + "] for keyword [" + key.getKeyword() + "]");
 		key.setDescription(desc);
 		ui.removeDialog(dialog);
 		log.trace("EXIT");
 	}
 	
 	/**
 	 * Method invoked when the user decides to send a mail specifically to one contact.
 	 */
 	public void selectMailRecipient(Object dialog) {
 		ContactSelecter contactSelecter = new ContactSelecter(ui);
 		contactSelecter.show(InternationalisationUtils.getI18NString(SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE), "mail_setRecipient(contactSelecter_contactList, contactSelecter)", dialog, this);
 	}
 	
 	/**
 	 * Sets the phone number of the selected contact.
 	 * 
 	 * @param contactSelecter_contactList
 	 * @param dialog
 	 */
 	public void mail_setRecipient(Object contactSelecter_contactList, Object dialog) {
 		log.trace("ENTER");
 		Object emailDialog = ui.getAttachedObject(dialog);
 		Object recipientTextfield = ui.find(emailDialog, COMPONENT_TF_RECIPIENT);
 		Object selectedItem = ui.getSelectedItem(contactSelecter_contactList);
 		if (selectedItem == null) {
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
 			log.trace("EXIT");
 			return;
 		}
 		Contact selectedContact = ui.getContact(selectedItem);
 		String currentText = ui.getText(recipientTextfield);
 		log.debug("Recipients begin [" + currentText + "]");
 		if (!currentText.equals("")) {
 			currentText += ";";
 		}
 		currentText += selectedContact.getEmailAddress();
 		log.debug("Recipients final [" + currentText + "]");
 		setText(recipientTextfield, currentText);
 		removeDialog(dialog);
 		log.trace("EXIT");
 	}
 
 	/**
 	 * Creates a new join group action.
 	 */
 	public void do_newKActionJoin(Object groupSelecterDialog, Object groupList) {
 		createActionLeaveOrJoin(groupSelecterDialog, groupList, true);
 	}
 
 	/**
 	 * Creates an action to leave or join group, according to supplied information.
 	 * 
 	 * @param groupSelecterDialog
 	 * @param groupList
 	 * @param join
 	 */
 	private void createActionLeaveOrJoin(Object groupSelecterDialog,
 			Object groupList, boolean join) {
 		log.trace("ENTER");
 		log.debug("Join [" + join + "]");
 		Group group = ui.getGroup(ui.getSelectedItem(groupList));
 		if (group == null) {
 			log.debug("No group selected");
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_SELECTED_TO_FWD));
 			log.trace("EXIT");
 			return;
 		}
 		String startDate = ui.getText(ui.find(groupSelecterDialog, COMPONENT_TF_START_DATE));
 		String endDate = ui.getText(ui.find(groupSelecterDialog, COMPONENT_TF_END_DATE));
 		log.debug("Start Date [" + startDate + "]");
 		log.debug("End Date [" + endDate + "]");
 		if (startDate.equals("")) {
 			log.debug("No start date set, so we set to [" + InternationalisationUtils.getDefaultStartDate() + "]");
 			startDate = InternationalisationUtils.getDefaultStartDate();
 		}
 		long start;
 		long end;
 		try {
 			Date ds = InternationalisationUtils.parseDate(startDate); 
 			if (!endDate.equals("") && !endDate.equals(InternationalisationUtils.getI18NString(COMMON_UNDEFINED))) {
 				Date de = InternationalisationUtils.parseDate(endDate);
 				if (!Utils.validateDates(ds, de)) {
 					log.debug("Start date is not before the end date");
 					ui.alert(InternationalisationUtils.getI18NString(MESSAGE_START_DATE_AFTER_END));
 					log.trace("EXIT");
 					return;
 				}
 				end = de.getTime();
 			} else {
 				end = DEFAULT_END_DATE;
 			}
 			start = ds.getTime();
 		} catch (ParseException e) {
 			log.debug("Wrong format for date", e);
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_WRONG_FORMAT_DATE));
 			log.trace("EXIT");
 			return;
 		} 
 		KeywordAction action;
 		boolean isNew = false;;
 		if (ui.isAttachment(groupSelecterDialog, KeywordAction.class)) {
 			action = ui.getKeywordAction(groupSelecterDialog);
 			log.debug("Editing action [" + action + "]. Setting new values!");
 			action.setGroup(group);
 			action.setStartDate(start);
 			action.setEndDate(end);
			keywordActionDao.updateKeywordAction(action);
 		} else {
 			isNew  = true;
 			Keyword keyword = ui.getKeyword(groupSelecterDialog);
 			log.debug("Creating action for keyword [" + keyword.getKeyword() + "].");
 			if (join) {
 				action = KeywordAction.createGroupJoinAction(keyword, group, start, end);
 			} else {
 				action = KeywordAction.createGroupLeaveAction(keyword, group, start, end);
 			}
			keywordActionDao.saveKeywordAction(action);
 		}
 		updateKeywordActionList(action, isNew);
 		ui.remove(groupSelecterDialog);
 		log.trace("EXIT");
 	}
 	
 	public void showSelectedKeyword() {
 		int index = ui.getSelectedIndex(keywordListComponent);
 		Object selected = ui.getSelectedItem(keywordListComponent);
 		Object divider = find(COMPONENT_KEYWORDS_DIVIDER);
 		if (ui.getItems(divider).length >= 2) {
 			ui.remove(ui.getItems(divider)[ui.getItems(divider).length - 1]);
 		}
 		if (index == 0) {
 			//Add keyword selected
 			Object panel = ui.loadComponentFromFile(UI_FILE_KEYWORDS_SIMPLE_VIEW, this);
 			fillGroups(panel);
 			Object btSave = ui.find(panel, COMPONENT_BT_SAVE);
 			ui.setText(btSave, InternationalisationUtils.getI18NString(ACTION_CREATE));
 			ui.setVisible(ui.find(panel,COMPONENT_PN_TIP), false);
 			ui.add(divider, panel);
 		} else if (index > 0) {
 			//An existent keyword is selected, let's check if it is simple or advanced.
 			Keyword keyword = ui.getAttachedObject(selected, Keyword.class);
 			Collection<KeywordAction> actions = this.keywordActionDao.getActions(keyword);
 			boolean simple = actions.size() <= 3;
 			if (simple) {
 				int previousType = -1;
 				for (KeywordAction action : actions) {
 					int type = action.getType();
 					if (type != KeywordAction.TYPE_REPLY
 							&& type != KeywordAction.TYPE_JOIN
 							&& type != KeywordAction.TYPE_LEAVE) {
 						simple = false;
 						break;
 					}
 					
 					if (action.getEndDate() != DEFAULT_END_DATE) {
 						simple = false;
 						break;
 					}
 					
 					if (type == previousType) {
 						simple = false;
 						break;
 					}
 					
 					previousType = type;
 				}
 			}
 			if (simple) {
 				Object panel = ui.loadComponentFromFile(UI_FILE_KEYWORDS_SIMPLE_VIEW, this);
 				//Fill every field
 				fillGroups(panel);
 				Object tfKeyword = ui.find(panel, COMPONENT_TF_KEYWORD);
 				ui.setEnabled(tfKeyword, false);
 				String key = keyword.getKeyword().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeyword();
 				ui.setText(tfKeyword, key);
 				for (KeywordAction action : actions) {
 					int type = action.getType();
 					if (type == KeywordAction.TYPE_REPLY) {
 						Object cbReply = ui.find(panel, COMPONENT_CB_AUTO_REPLY);
 						Object tfReply = ui.find(panel, COMPONENT_TF_AUTO_REPLY);
 						ui.setSelected(cbReply, true);
 						ui.setText(tfReply, action.getUnformattedReplyText());
 					} else if (type == KeywordAction.TYPE_JOIN) {
 						Object checkboxJoin = ui.find(panel, COMPONENT_CB_JOIN_GROUP);
 						Object cbJoinGroup = ui.find(panel, COMPONENT_CB_GROUPS_TO_JOIN);
 						for (int i = 0; i < ui.getItems(cbJoinGroup).length; i++) {
 							Group g = ui.getAttachedObject(ui.getItems(cbJoinGroup)[i], Group.class);
 							if (g.equals(action.getGroup())) {
 								ui.setInteger(cbJoinGroup, Thinlet.SELECTED, i);
 								break;
 							}
 						}
 						ui.setSelected(checkboxJoin, true);
 					} else if (type == KeywordAction.TYPE_LEAVE) {
 						Object checkboxLeave = ui.find(panel, COMPONENT_CB_LEAVE_GROUP);
 						Object cbLeaveGroup = ui.find(panel, COMPONENT_CB_GROUPS_TO_LEAVE);
 						for (int i = 0; i < ui.getItems(cbLeaveGroup).length; i++) {
 							Group g = ui.getAttachedObject(ui.getItems(cbLeaveGroup)[i], Group.class);
 							if (g.equals(action.getGroup())) {
 								ui.setInteger(cbLeaveGroup, Thinlet.SELECTED, i);
 								break;
 							}
 						}
 						ui.setSelected(checkboxLeave, true);
 					}
 				}
 				
 				ui.setVisible(ui.find(panel, COMPONENT_BT_CLEAR), false);
 				ui.add(divider, panel);
 			} else {
 				Object panel = ui.loadComponentFromFile(UI_FILE_KEYWORDS_ADVANCED_VIEW, this);
 				Object table = ui.find(panel, COMPONENT_ACTION_LIST);
 				String key = keyword.getKeyword().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeyword();
 				ui.setText(panel, InternationalisationUtils.getI18NString(COMMON_KEYWORD_ACTIONS_OF, key));
 				//Fill every field
 				for (KeywordAction action : actions) {
 					ui.add(table, ui.getRow(action));
 				}
 				ui.add(divider, panel);
 				enableKeywordActionFields(table, ui.find(panel, COMPONENT_KEY_ACT_PANEL));
 			}
 		}
 		enableKeywordFields(ui.find(COMPONENT_KEY_PANEL));
 	}
 
 //> UI HELPER METHODS
 	/** 
 	 * In advanced mode, updates the list of keywords in the Keyword Manager.  
 	 * <br>Has no effect in classic mode.
 	 */
 	private void updateKeywordList() {
 		int selectedIndex = ui.getSelectedIndex(keywordListComponent);
 		ui.removeAll(keywordListComponent);
 		Object newKeyword = ui.createListItem(InternationalisationUtils.getI18NString(ACTION_ADD_KEYWORD), null);
 		ui.setIcon(newKeyword, Icon.KEYWORD_NEW);
 		ui.add(keywordListComponent, newKeyword);
 		for(Keyword keyword : keywordDao.getAllKeywords()) {
 			ui.add(keywordListComponent, ui.createListItem(keyword));
 		}
 		if (selectedIndex >= ui.getItems(keywordListComponent).length || selectedIndex == -1) {
 			selectedIndex = 0;
 		}
 		ui.setSelectedIndex(keywordListComponent, selectedIndex);
 		showSelectedKeyword();
 	}
 
 	/**
 	 * Shows the new keyword dialog.
 	 * 
 	 * @param keyword
 	 */
 	private void showNewKeywordForm(Keyword keyword) {
 		String title = "Create new keyword.";
 		Object keywordForm = ui.loadComponentFromFile(UI_FILE_NEW_KEYWORD_FORM, this);
 		ui.setAttachedObject(keywordForm, keyword);
 		ui.setText(ui.find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_TITLE), title);
 		// Pre-populate the keyword textfield with currently-selected keyword string so that
 		// a sub-keyword can easily be created.  Append a space to save the user from having
 		// to do it!
 		if (keyword != null) ui.setText(ui.find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_KEYWORD), keyword.getKeyword() + ' ');
 		ui.add(keywordForm);
 	}
 	
 	private void fillGroups(Object panel) {
 		Object cbJoin = ui.find(panel, COMPONENT_CB_GROUPS_TO_JOIN);
 		Object cbLeave = ui.find(panel, COMPONENT_CB_GROUPS_TO_LEAVE);
 		Object cbJoinGroup = ui.find(panel, COMPONENT_CB_JOIN_GROUP);
 		Object cbLeaveGroup = ui.find(panel, COMPONENT_CB_LEAVE_GROUP);
 		List<Group> groups = this.groupDao.getAllGroups();
 		for (Group g : groups) {
 			Object item = createComboBoxChoice(g);
 			ui.add(cbJoin, item);
 			ui.add(cbLeave, item);
 		}
 		if (groups.size() == 0) {
 			ui.setEnabled(cbJoinGroup, false);
 			ui.setEnabled(cbJoin, false);
 			ui.setEnabled(cbLeaveGroup , false);
 			ui.setEnabled(cbLeave, false);
 		} else {
 			ui.setSelectedIndex(cbJoin, 0);
 			ui.setSelectedIndex(cbLeave, 0);
 		}
 	}
 	
 	/**
 	 * This method invokes the correct edit dialog according to the supplied action type.
 	 * 
 	 * @param action
 	 */
 	private void showActionEditDialog(KeywordAction action) {
 		switch (action.getType()) {
 			case KeywordAction.TYPE_FORWARD:
 				show_newKActionForwardFormForEdition(action);
 				break;
 			case KeywordAction.TYPE_JOIN: 
 				showGroupSelecter(action, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + action.getKeyword().getKeyword()+ "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_LEAVE_GROUP) + ":", "do_newKActionJoin(groupSelecter, groupSelecter_groupList)");
 				break;
 			case KeywordAction.TYPE_LEAVE: 
 				showGroupSelecter(action, InternationalisationUtils.getI18NString(COMMON_KEYWORD) + " \"" + action.getKeyword().getKeyword()+ "\" " + InternationalisationUtils.getI18NString(COMMON_AUTO_LEAVE_GROUP) + ":", "do_newKActionLeave(groupSelecter, groupSelecter_groupList)");
 				break;
 			case KeywordAction.TYPE_REPLY:
 				show_newKActionReplyFormForEdition(action);
 				break;
 			case KeywordAction.TYPE_EXTERNAL_CMD:
 				show_newKActionExternalCmdFormForEdition(action);
 				break;
 			case KeywordAction.TYPE_EMAIL:
 				show_newKActionEmailFormForEdition(action);
 				break;
 		}
 	}
 	
 	/**
 	 * Shows the keyword dialog for edit purpose.
 	 * 
 	 * @param keyword The object to be edited.
 	 */
 	private void showKeywordDialogForEdition(Keyword keyword) {
 		String key = keyword.getKeyword().length() == 0 ? "<" + InternationalisationUtils.getI18NString(COMMON_BLANK) + ">" : keyword.getKeyword();
 		String title = InternationalisationUtils.getI18NString(COMMON_EDITING_KEYWORD, key);
 		Object keywordForm = ui.loadComponentFromFile(UI_FILE_NEW_KEYWORD_FORM, this);
 		ui.setAttachedObject(keywordForm, keyword);
 		ui.setText(ui.find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_TITLE), title);
 		// Pre-populate the keyword textfield with currently-selected keyword string so that
 		// a sub-keyword can easily be created.  Append a space to save the user from having
 		// to do it!
 		Object textField = ui.find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_KEYWORD);
 		Object textFieldDescription = ui.find(keywordForm, COMPONENT_NEW_KEYWORD_FORM_DESCRIPTION);
 		ui.setText(textField, key);
 		ui.setEnabled(textField, false);
 		if (keyword.getDescription() != null) { 
 			ui.setText(textFieldDescription, keyword.getDescription());
 		}
 		String method = "finishKeywordEdition(newKeywordForm, newKeywordForm_description.text)";
 		ui.setAction(ui.find(keywordForm, COMPONENT_NEW_KEYWORD_BUTTON_DONE), method, keywordForm, this);
 		ui.add(keywordForm);
 	}
 	
 	void updateKeywordActionList_(KeywordAction action, boolean isNew) {
 		updateKeywordActionList(action, isNew);
 	}
 	
 	private void updateKeywordActionList(KeywordAction action, boolean isNew) {
 		Object table = find(COMPONENT_ACTION_LIST);
 		if (isNew) {
 			ui.add(table, ui.getRow(action));
 		} else {
 			int index = -1;
 			for (Object o : ui.getItems(table)) {
 				KeywordAction a = ui.getKeywordAction(o);
 				if (a.equals(action)) {
 					index = ui.getIndex(table, o);
 					ui.remove(o);
 				}
 			}
 			ui.add(table, ui.getRow(action), index);
 		}
 	}
 
 	/**
 	 * Shows the forward message action dialog for editing purpose.
 	 * 
 	 * @param action
 	 */
 	private void show_newKActionForwardFormForEdition(KeywordAction action) {
 		ForwardActionDialog dialog = new ForwardActionDialog(ui, this);
 		dialog.init(action);
 		dialog.show();
 	}
 
 	/**
 	 * Shows the new auto reply action dialog for editing purpose.
 	 * 
 	 * @param action
 	 */
 	private void show_newKActionReplyFormForEdition(KeywordAction action) {
 		ReplyActionDialogHandler dialog = new ReplyActionDialogHandler(ui, this);
 		dialog.init(action);
 		dialog.show();
 	}
 	
 	/**
 	 * Shows the new external command action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	public void show_newKActionExternalCmdForm(Object keywordList) {
 		log.trace("ENTER");
 		Keyword keyword = ui.getKeyword(ui.getSelectedItem(keywordList));
 		log.debug("External command for keyword [" + keyword.getKeyword() + "]");
 		Object externalCmdForm = ui.loadComponentFromFile(UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM, this);
 		//Adds the date panel to it
 		ui.addDatePanel(externalCmdForm);
 		ui.setAttachedObject(externalCmdForm, keyword);
 		Object list = ui.find(externalCmdForm, COMPONENT_EXTERNAL_COMMAND_GROUP_LIST);
 		List<Group> userGroups = this.groupDao.getAllGroups();
 		for (Group g : userGroups) {
 			log.debug("Adding group [" + g.getName() + "] to list");
 			Object item = ui.createListItem(g.getName(), g);
 			ui.setIcon(item, Icon.GROUP);
 			ui.add(list, item);
 		}
 		ui.add(externalCmdForm);
 		log.trace("EXIT");
 	}
 	
 	/**
 	 * Shows the new email action dialog.
 	 * 
 	 * @param keywordList
 	 */
 	private void show_newKActionEmailFormForEdition(KeywordAction action) {
 		Object emailForm = ui.loadComponentFromFile(UI_FILE_NEW_KACTION_EMAIL_FORM, this);
 		//Adds the date panel to it
 		ui.addDatePanel(emailForm);
 		ui.setAttachedObject(emailForm, action);
 		Object list = ui.find(emailForm, COMPONENT_MAIL_LIST);
 		for (EmailAccount acc : emailAccountDao.getAllEmailAccounts()) {
 			log.debug("Adding existent e-mail account [" + acc.getAccountName() + "] to list");
 			Object item = ui.createListItem(acc.getAccountName(), acc);
 			ui.setIcon(item, Icon.SERVER);
 			ui.add(list, item);
 			if (acc.equals(action.getEmailAccount())) {
 				log.debug("Selecting the current account for this e-mail [" + acc.getAccountName() + "]");
 				ui.setSelected(item, true);
 			}
 		}
 		ui.setText(ui.find(emailForm, COMPONENT_TF_SUBJECT), action.getEmailSubject());
 		ui.setText(ui.find(emailForm, COMPONENT_TF_MESSAGE), action.getUnformattedReplyText());
 		ui.setText(ui.find(emailForm, COMPONENT_TF_RECIPIENT), action.getEmailRecipients());
 		
 		ui.setText(ui.find(emailForm, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
 		Object endDate = ui.find(emailForm, COMPONENT_TF_END_DATE);
 		String toSet = "";
 		if (action != null) {
 			if (action.getEndDate() == DEFAULT_END_DATE) {
 				toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
 			} else {
 				toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
 			}
 		}
 		ui.setText(endDate, toSet);
 		ui.add(emailForm);
 		log.trace("EXIT");
 	}
 
 	private String keywordSimple_getAutoReply(Object panel) {
 		String ret = null;
 		if (ui.isSelected(ui.find(panel, COMPONENT_CB_AUTO_REPLY))) {
 			ret = ui.getText(ui.find(panel, COMPONENT_TF_AUTO_REPLY));
 		}
 		return ret;
 	}
 	
 	private Group keywordSimple_getJoin(Object panel) {
 		Group ret = null;
 		if (ui.isSelected(ui.find(panel, COMPONENT_CB_JOIN_GROUP))) {
 			ret = ui.getAttachedObject(ui.getSelectedItem(ui.find(panel, COMPONENT_CB_GROUPS_TO_JOIN)), Group.class);
 		}
 		return ret;
 	}
 	
 	private Group keywordSimple_getLeave(Object panel) {
 		Group ret = null;
 		if (ui.isSelected(ui.find(panel, COMPONENT_CB_LEAVE_GROUP))) {
 			ret = ui.getAttachedObject(ui.getSelectedItem(ui.find(panel, COMPONENT_CB_GROUPS_TO_LEAVE)), Group.class);
 		}
 		return ret;
 	}
 
 	/** @see #showGroupSelecter(Object, String, String, Object) */
 	private void showGroupSelecter(Object actionObject, String title, String callbackMethodName) {
 		showGroupSelecter(actionObject, title, callbackMethodName, this);
 	}
 
 	/** @see #showGroupSelecter(Object, String, String, Object) */
 	private void showGroupSelecter(Object actionObject, String title, String callbackMethodName, ThinletUiEventHandler eventHandler) {
		showGroupSelecter(actionObject, true, title, callbackMethodName, eventHandler);
 	}
 	
 	/**
 	 * Shows the group selecter dialog, which is used for JOIN/LEAVE group actions.
 	 * @param actionObject The object to be edited, or null if we are creating one.
 	 * @param title
 	 * @param callbackMethodName
 	 * TODO remove data panel references
 	 */
 	private void showGroupSelecter(Object actionObject, boolean addDatePanel, String title, String callbackMethodName, ThinletUiEventHandler eventHandler) {
 		if(log.isTraceEnabled()) {
 			log.trace("UiGeneratorController.showGroupSelecter()");
 			log.trace("actionObject: " + actionObject);
 			log.trace("title: " + title);
 		}
 		Object selecter = ui.loadComponentFromFile(UI_FILE_GROUP_SELECTER, eventHandler);
 		//Adds the date panel to it
 		if(addDatePanel) {
 			ui.addDatePanel(selecter);
 		}
 		ui.setAttachedObject(selecter, actionObject);
 		ui.setText(ui.find(selecter, COMPONENT_GROUP_SELECTER_TITLE), title);
 		Object list = ui.find(selecter, COMPONENT_GROUP_SELECTER_GROUP_LIST);
 		List<Group> userGroups = this.groupDao.getAllGroups();
 		if (userGroups.size() == 0) {
 			ui.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_GROUP_CREATED_BY_USERS));
 			return;
 		}
 		for (Group g : userGroups) {
 			Object item = ui.createListItem(g.getName(), g);
 			ui.setIcon(item, Icon.GROUP);
 			if (actionObject instanceof KeywordAction) {
 				KeywordAction action = (KeywordAction) actionObject;
 				if (g.getName().equals(action.getGroup().getName())) {
 					ui.setSelected(item, true);
 				}
 			}
 			ui.add(list, item);
 		}
 		if (addDatePanel && actionObject instanceof KeywordAction) {
 			log.trace("UiGeneratorController.showGroupSelecter() : ADDING THE DATES COMPONENT.");
 			KeywordAction action = (KeywordAction) actionObject;
 			ui.setText(ui.find(selecter, COMPONENT_TF_START_DATE), action == null ? "" : InternationalisationUtils.getDateFormat().format(action.getStartDate()));
 			Object endDate = ui.find(selecter, COMPONENT_TF_END_DATE);
 			String toSet = "";
 			if (action != null) {
 				if (action.getEndDate() == DEFAULT_END_DATE) {
 					toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
 				} else {
 					toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
 				}
 			}
 			ui.setText(endDate, toSet);
 		}
 		ui.setAction(ui.find(selecter, COMPONENT_GROUP_SELECTER_OK_BUTTON), callbackMethodName, selecter, eventHandler);
 		ui.add(selecter);
 	}
 	
 	/**
 	 * Shows the new external command action dialog for edition.
 	 * 
 	 * @param keywordList
 	 */
 	private void show_newKActionExternalCmdFormForEdition(KeywordAction action) {
 		log.trace("ENTER");
 		Object externalCmdForm = ui.loadComponentFromFile(UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM, this);
 		//Adds the date panel to it
 		ui.addDatePanel(externalCmdForm);
 		Object list = ui.find(externalCmdForm, COMPONENT_EXTERNAL_COMMAND_GROUP_LIST);
 		List<Group> userGroups = this.groupDao.getAllGroups();
 		for (Group g : userGroups) {
 			log.debug("Adding group [" + g.getName() + "] to list");
 			Object item = ui.createListItem(g.getName(), g);
 			ui.setIcon(item, Icon.GROUP);
 			ui.add(list, item);
 		}
 		ui.setAttachedObject(externalCmdForm, action);
 		//COMMAND TYPE
 		ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_TYPE_HTTP), action.getExternalCommandType() == KeywordAction.EXTERNAL_HTTP_REQUEST);
 		ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_TYPE_COMMAND_LINE), action.getExternalCommandType() == KeywordAction.EXTERNAL_COMMAND_LINE);
 		
 		//COMMAND
 		ui.setText(ui.find(externalCmdForm, COMPONENT_TF_COMMAND), action.getUnformattedCommand());
 		
 		Object pnResponse = ui.find(externalCmdForm, COMPONENT_PN_RESPONSE);
 		//RESPONSE TYPE
 		if (action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_PLAIN_TEXT) {
 			log.debug("Setting up dialog for PLAIN TEXT response.");
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_PLAIN_TEXT), true);
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_FRONTLINE_COMMANDS), false);
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_NO_RESPONSE), false);
 			
 			ui.activate(pnResponse);
 			ui.deactivate(list);
 			//RESPONSE PANEL
 			ui.setText(ui.find(externalCmdForm, COMPONENT_TF_MESSAGE), action.getUnformattedCommandText());
 			int responseActionType = action.getCommandResponseActionType();
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_CB_AUTO_REPLY),
 						responseActionType == KeywordAction.TYPE_REPLY || responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD);
 		
 			if (responseActionType == KeywordAction.TYPE_FORWARD || responseActionType == KeywordAction.EXTERNAL_REPLY_AND_FORWARD) {
 				ui.setSelected(ui.find(externalCmdForm, COMPONENT_CB_FORWARD), true);
 				ui.activate(list);
 				//Select group
 				Group g = action.getGroup();
 				for (Object item : ui.getItems(list)) {
 					Group it = ui.getGroup(item);
 					if (it.equals(g)) {
 						log.debug("Selecting group [" + g.getName() + "].");
 						ui.setSelected(item, true);
 						break;
 					}
 				}
 			}
 		} else if (action.getExternalCommandResponseType() == KeywordAction.EXTERNAL_RESPONSE_LIST_COMMANDS) {
 			log.debug("Setting up dialog for LIST COMMANDS response.");
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_PLAIN_TEXT), false);
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_FRONTLINE_COMMANDS), true);
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_NO_RESPONSE), false);
 			ui.deactivate(pnResponse);
 		} else {
 			log.debug("Setting up dialog for NO response.");
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_PLAIN_TEXT), false);
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_FRONTLINE_COMMANDS), false);
 			ui.setSelected(ui.find(externalCmdForm, COMPONENT_RB_NO_RESPONSE), true);
 			ui.deactivate(pnResponse);
 		}
 		
 		//START and END dates
 		ui.setText(ui.find(externalCmdForm, COMPONENT_TF_START_DATE), InternationalisationUtils.getDateFormat().format(action.getStartDate()));
 		Object endDate = ui.find(externalCmdForm, COMPONENT_TF_END_DATE);
 		String toSet = "";
 		if (action.getEndDate() == DEFAULT_END_DATE) {
 			toSet = InternationalisationUtils.getI18NString(COMMON_UNDEFINED);
 		} else {
 			toSet = InternationalisationUtils.getDateFormat().format(action.getEndDate());
 		}
 		ui.setText(endDate, toSet);
 		ui.add(externalCmdForm);
 		log.trace("EXIT");
 	}
 
 	private Object createComboBoxChoice(Group g) {
 		Object item = ui.createComboboxChoice(g.getName(), g);
 		ui.setIcon(item, Icon.GROUP);
 		return item;
 	}
 	
 //> UI PASSTHROUGH METHODS
 	/**
 	 * @param component
 	 * @param value
 	 */
 	public void setText(Object component, String value) {
 		this.ui.setText(component, value);
 	}
 	/** Show the email account settings dialog. */
 	public void showEmailAccountsSettings() {
 		this.ui.showEmailAccountsSettings();
 	}
 	public void activate(Object component) {
 		this.ui.activate(component);
 	}
 	public void deactivate(Object component) {
 		this.ui.deactivate(component);
 	}
 }
