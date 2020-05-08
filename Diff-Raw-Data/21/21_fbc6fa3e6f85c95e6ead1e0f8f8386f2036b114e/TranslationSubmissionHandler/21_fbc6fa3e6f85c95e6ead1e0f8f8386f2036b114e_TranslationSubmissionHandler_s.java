 /*
  * FrontlineSMS <http://www.frontlinesms.com>
  * Copyright 2007, 2008 kiwanja
  * 
  * This file is part of FrontlineSMS.
  * 
  * FrontlineSMS is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at
  * your option) any later version.
  * 
  * FrontlineSMS is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.frontlinesms.plugins.translation.ui;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.email.EmailException;
 import net.frontlinesms.plugins.translation.MasterTranslationFile;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.TextResourceKeyOwner;
 
 /**
  * This class is responsible for showing the New/Edit translation dialog and handling its events.
  * 
  * @author Morgan Belkadi <morgan@frontlinesms.com>
  */
 @TextResourceKeyOwner(prefix={"MESSAGE_", "I18N"})
 public class TranslationSubmissionHandler implements ThinletUiEventHandler {
 	
 //> UI LAYOUT FILE PATHS
 	/** [ui layout file path] The language selection page */
 	private static final String UI_FILE_SUBMIT_TRANSLATION = "/ui/plugins/translation/dgSubmitTranslation.xml";
 	
 	private static final String I18N_COMMON_INVALID_EMAIL = "common.invalid.email";
 	private static final String I18N_TRANSLATION_SENT = "plugins.translation.translation.sent";
 	private static final String I18N_UNABLE_SEND_TRANSLATION = "plugins.translation.unable.send.translation";
 
 //> UI COMPONENT NAMES
 	private static final String UI_COMPONENT_BT_SUBMIT = "btSubmit";
 	private static final String UI_COMPONENT_LB_SUBMITTED_LANGUAGE = "lbSubmittedLanguage";
 
 	private static final String EMAIL_REG_EXP = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
 
 	
 //> INSTANCE VARIABLES
 	private MasterTranslationFile languageBundle;
 	private Object dialogComponent;
 	private UiGeneratorController ui;
 
 //> CONSTRUCTORS
 	/**
 	 * Language Properties Handler
 	 */
 	public TranslationSubmissionHandler(UiGeneratorController ui, MasterTranslationFile languageBundle) {
 		this.ui = ui;
 		this.languageBundle = languageBundle;
 		this.dialogComponent = this.ui.loadComponentFromFile(UI_FILE_SUBMIT_TRANSLATION, this);
 	}
 	
 	/**
 	 * @return the instance of the language selection dialog 
 	 */
 	public Object getDialog() {
 		return this.dialogComponent;
 	}
 	
 	/**
 	 * Initializes the dialog
 	 */
 	public void initDialog() {
 		this.populateSubmittedLanguage();
 	}
 
 	/** @see UiGeneratorController#removeDialog(Object) */
 	public void removeDialog() {
 		this.ui.removeDialog(dialogComponent);
 	}
 	
 	/**
 	 * Open the browser to display a web page
 	 * @param url
 	 */
 	public void openBrowser(String url) {
 		this.ui.openBrowser(url);
 	}
 
 //> INSTANCE HELPER METHODS
 	/** Populate and display the selected language in a Combo Box. */
 	private void populateSubmittedLanguage() {
 		Object lbSubmittedLanguage = find(UI_COMPONENT_LB_SUBMITTED_LANGUAGE);
 		this.ui.setIcon(lbSubmittedLanguage, this.ui.getFlagIcon(this.languageBundle));
 		this.ui.setText(lbSubmittedLanguage, this.languageBundle.getLanguageName());
 	}
 	
 	/**
 	 * UI event triggered when either the user name field or the user e-mail address field text changed
 	 * @param userName The content of the User Name Field
 	 * @param userEmail The content of the User E-Mail address Field
 	 */
 	public void textFieldChanged (String userName, String userEmail) {
 		boolean enableSendButton = (userName != null
 								&& userEmail != null
 								&& userName.length() > 0
 								&& userEmail.length() > 0);
 		
 		this.ui.setEnabled(find(UI_COMPONENT_BT_SUBMIT), enableSendButton);
 	}
 	
 	/**
 	 * Actually submits the selected translation
 	 * @param userName The content of the User Name Field
 	 * @param userEmail The content of the User E-Mail address Field
 	 */
 	public void submitTranslation (String userName, String userEmail, boolean contribute) {
 		if (this.isValidEmailAddress(userEmail)) {
 			this.ui.alert(InternationalisationUtils.getI18NString(I18N_COMMON_INVALID_EMAIL));
 		} else {
 			String subject = "FrontlineSMS translation: " + this.languageBundle.getLanguageName();
 			String textContent = "Sent translation: " + this.languageBundle.getLanguageName() + " (" + this.languageBundle.getLanguageCode() + ").\n" +
 								 userName + (contribute ? " would" : " wouldn't") + " like to appear as a contributor for this translation.";
 			try {
				FrontlineUtils.sendToFrontlineSupport(userName, userEmail, subject, textContent, InternationalisationUtils.getLanguageDirectory() + languageBundle.getFilename());
 				this.removeDialog();
 				this.ui.infoMessage(InternationalisationUtils.getI18NString(I18N_TRANSLATION_SENT));
 			} catch (EmailException e) {
 				this.ui.alert(InternationalisationUtils.getI18NString(I18N_UNABLE_SEND_TRANSLATION));
 			}
 		}
 	}
 	
 	private boolean isValidEmailAddress (String emailAddress) {
 		CharSequence inputStr = emailAddress;  
 		Pattern pattern = Pattern.compile(EMAIL_REG_EXP,Pattern.CASE_INSENSITIVE);  
 		Matcher matcher = pattern.matcher(inputStr);
 		
 		return !matcher.matches();
 	}
 
 	private Object find (String component) {
 		return this.ui.find(this.dialogComponent, component);
 	}
 }
