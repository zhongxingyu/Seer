 /**
  * 
  */
 package net.frontlinesms.ui.handler;
 
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_EVENTS_LIST;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import net.frontlinesms.FrontlineSMSConstants;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.ui.Event;
 import net.frontlinesms.ui.FrontlineUI;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.UiGeneratorControllerConstants;
 import net.frontlinesms.ui.UiProperties;
 import net.frontlinesms.ui.handler.contacts.ContactSelecter;
 import net.frontlinesms.ui.handler.message.MessagePanelHandler;
 import net.frontlinesms.ui.i18n.FileLanguageBundle;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * Event handler for the Home tab and associated dialogs
  * @author Alex
  */
 public class HomeTabHandler extends BaseTabHandler {
 //> STATIC CONSTANTS
 	/** Limit of the number of events to be displayed on the home screen */
 	static final int EVENTS_LIMIT = 30;
 	
 	/** UI XML File Path: the Home Tab itself */
 	protected static final String UI_FILE_HOME_TAB = "/ui/core/home/homeTab.xml";
 	/** UI XML File Path: settings dialog for the home tab */
 	private static final String UI_FILE_HOME_TAB_SETTINGS = "/ui/core/home/dgHomeTabSettings.xml";
 	/** Thinlet Component Name: Home Tab: logo */
 	private static final String COMPONENT_LB_HOME_TAB_LOGO = "lbHomeTabLogo";
 	/** Thinlet Component Name: Settings dialog: checkbox indicating if the logo is visible */
 	private static final String COMPONENT_CB_HOME_TAB_LOGO_VISIBLE = "cbHomeTabLogoVisible";
 	/** Thinlet Component Name: Settings dialog: checkbox used to choose the default logo */
 	private static final String COMPONENT_CB_HOME_TAB_USE_DEFAULT_LOGO = "cbHomeTabLogoDefault";
 	/** Thinlet Component Name: Settings dialog: checkbox used to choose a custom logo */
 	private static final String COMPONENT_CB_HOME_TAB_USE_CUSTOM_LOGO = "cbHomeTabLogoCustom";
 	/** Thinlet Component Name: Settings dialog: checkbox used to choose a custom logo */
 	private static final String COMPONENT_CB_HOME_TAB_LOGO_KEEP_ORIGINAL_SIZE = "cbHomeTabLogoKeepOriginalSize";
 	/** Thinlet Component Name: Settings dialog: textfield inidicating the path of the image file for the logo */
 	private static final String COMPONENT_TF_IMAGE_SOURCE = "tfImageSource";
 
 	/** Default FrontlineSMS home logo */
 	private static final String FRONTLINE_LOGO = "/icons/frontlineSMS_logo.png";
 
 
 //> INSTANCE PROPERTIES
 
 //> CONSTRUCTORS
 	/**
 	 * Create a new instance of this class.
 	 * @param ui value for {@link #ui}
 	 */
 	public HomeTabHandler(UiGeneratorController ui) {
 		super(ui);
 	}
 
 //> UI METHODS
 	/** Show the settings dialog for the home tab. */
 	public void showHomeTabSettings() {
 		log.trace("ENTER");
 		Object homeTabSettings = ui.loadComponentFromFile(UI_FILE_HOME_TAB_SETTINGS, this);
 		UiProperties uiProperties = UiProperties.getInstance();
 		boolean visible 			= uiProperties.isHometabLogoVisible();
 		boolean isCustomLogo 		= uiProperties.isHometabCustomLogo();
 		boolean isOriginalSizeKept 	= uiProperties.isHometabLogoOriginalSizeKept();
 		
 		String imageLocation = uiProperties.getHomtabLogoPath();
 		log.debug("Visible? " + visible);
 		log.debug("Logo: " + (isCustomLogo ? "custom" : "default"));
 		if (isCustomLogo)
 			log.debug("Keep original size: " + isOriginalSizeKept);
 		log.debug("Image location [" + imageLocation + "]");
 		
 		if (visible)
 			ui.setSelected(ui.find(homeTabSettings, (isCustomLogo ? COMPONENT_CB_HOME_TAB_USE_CUSTOM_LOGO : COMPONENT_CB_HOME_TAB_USE_DEFAULT_LOGO)), true);
 		else
 			ui.setSelected(ui.find(homeTabSettings, COMPONENT_CB_HOME_TAB_LOGO_VISIBLE), true);
 		
 		ui.setSelected(ui.find(homeTabSettings, COMPONENT_CB_HOME_TAB_LOGO_KEEP_ORIGINAL_SIZE), isOriginalSizeKept);
 		setHomeTabCustomLogo(ui.find(homeTabSettings, "pnImgSource"), isCustomLogo && visible);
 		
 		if (imageLocation != null && imageLocation.length() > 0) {
 			ui.setText(ui.find(homeTabSettings, COMPONENT_TF_IMAGE_SOURCE), imageLocation);
 		}
 		
 		
 		//setHomeTab(ui.find(homeTabSettings, "pnImgSource"), !isDefaultLogo);
 		
 		ui.add(homeTabSettings);
 		log.trace("EXIT");
 	}
 	
 	/**
 	 * Save the home tab settings from the settings dialog, and remove the dialog.
 	 * @param panel
 	 */
 	public void saveHomeTabSettings(Object panel) {
 		log.trace("ENTER");
 		boolean visible 			= ui.isSelected(ui.find(panel, COMPONENT_CB_HOME_TAB_LOGO_VISIBLE));
 		boolean isCustomLogo 		= ui.isSelected(ui.find(panel, COMPONENT_CB_HOME_TAB_USE_CUSTOM_LOGO));
 		boolean isOriginalSizeKept 	= ui.isSelected(ui.find(panel, COMPONENT_CB_HOME_TAB_LOGO_KEEP_ORIGINAL_SIZE));
 		
 		String imgSource = ui.getText(ui.find(panel, COMPONENT_TF_IMAGE_SOURCE));
 		log.debug("Visible? " + visible);
 		log.debug("Logo: " + (isCustomLogo ? "default" : "custom"));
 		log.debug("Image location [" + imgSource + "]");
 		UiProperties uiProperties = UiProperties.getInstance();
 		uiProperties.setHometabLogoVisible(visible);
 		uiProperties.setHometabCustomLogo(isCustomLogo);
 		uiProperties.setHometabLogoOriginalSizeKept(isOriginalSizeKept);
 		uiProperties.setHometabLogoPath(imgSource);
 		uiProperties.saveToDisk();
 
 		// Update visibility of logo
 		refreshLogoVisibility(getTab());
 		
 		ui.remove(panel);
 		log.trace("EXIT");
 	}
 	
 	/**
 	 * Enable or disable the bottom panel whether the logo is custom or not.
 	 * @param panel
 	 * @param isCustom <code>true</code> if the logo is a custom logo; <code>false</code> otherwise.
 	 */
 	public void setHomeTabCustomLogo(Object panel, boolean isCustom) {
 		ui.setEnabled(panel, isCustom);
 		for (Object obj : ui.getItems(panel)) {
 			ui.setEnabled(obj, isCustom);
 		}
 	}
 	
 
 	/**
 	 * Sets the phone number of the selected contact.
 	 * 
 	 * This method is triggered by the contact selected, as detailed in {@link #selectMessageRecipient()}.
 	 * 
 	 * @param contactSelecter_contactList
 	 * @param dialog
 	 */
 	public void setRecipientTextfield(Object contactSelecter_contactList, Object dialog) {
 		Object tfRecipient = ui.find(this.getTab(), UiGeneratorControllerConstants.COMPONENT_TF_RECIPIENT);
 		Object selectedItem = ui.getSelectedItem(contactSelecter_contactList);
 		if (selectedItem == null) {
 			ui.alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_NO_CONTACT_SELECTED));
 			return;
 		}
 		Contact selectedContact = ui.getContact(selectedItem);
 		ui.setText(tfRecipient, selectedContact.getPhoneNumber());
 		ui.remove(dialog);
 		ui.updateCost();
 	}
 
 	/** Method which triggers showing of the contact selecter. */
 	public void selectMessageRecipient() {
 		ContactSelecter contactSelecter = new ContactSelecter(ui);
 		contactSelecter.show(InternationalisationUtils.getI18NString(FrontlineSMSConstants.SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE), "setRecipientTextfield(contactSelecter_contactList, contactSelecter)", null, this);
 	}
 	
 //> UI PASSTHRU METHODS TO UiGC
 	/**
 	 * @param component Component whose contents are to be removed
 	 * @see UiGeneratorController#removeAll()
 	 */
 	public void removeAll(Object component) {
 		this.ui.removeAll(component);
 	}
 	/**
 	 * @param component
 	 * @see UiGeneratorController#showOpenModeFileChooser(Object)
 	 */
 	public void showOpenModeFileChooser(Object component) {
 		this.ui.showOpenModeFileChooser(component);
 	}
 	
 //> INSTANCE HELPER METHODS	
 	/**
 	 * Refresh the contents of the tab.
 	 */
 	protected Object initialiseTab() {
 		Object tabComponent = ui.loadComponentFromFile(UI_FILE_HOME_TAB, this);
 		
 		Object pnSend = ui.find(tabComponent, UiGeneratorControllerConstants.COMPONENT_PN_SEND);
 		Object pnMessage = MessagePanelHandler.create(this.ui).getPanel();
 		ui.add(pnSend, pnMessage);
 		
 		refreshLogoVisibility(tabComponent);
 		
 		Object fastLanguageSwitch = ui.find(tabComponent, "fastLanguageSwitch");
 		for (FileLanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
 			// Don't show the flag for the current language
 			if(languageBundle.equals(FrontlineUI.currentResourceBundle)) continue;
 			
 			Object button = ui.createLink("", "changeLanguage(this)", tabComponent);
 			ui.setIcon(button, ui.getFlagIcon(languageBundle));
 			ui.setString(button, "tooltip", languageBundle.getLanguageName());
 			ui.setWeight(button, 1, 0);
 			ui.setAttachedObject(button, languageBundle.getFile().getAbsolutePath());
 			ui.add(fastLanguageSwitch, button);
 		}
 		
 		return tabComponent;
 	}
 	
 	public void refresh() { /* No refresh required */ }
 	
 	/**
 	 * Update the visibility of the logo.
 	 * @param tabComponent The tab component.  This is passed in, as this method can be called form {@link #initialiseTab()}, in which case {@link #getTab()} will return null.
 	 */
 	private void refreshLogoVisibility(Object tabComponent) {
 		Object lbLogo = ui.find(tabComponent, COMPONENT_LB_HOME_TAB_LOGO);
 		if (!UiProperties.getInstance().isHometabLogoVisible()) {
 			Image noIcon = null;
 			ui.setIcon(lbLogo, noIcon);
 		} else {
 			String imageLocation = UiProperties.getInstance().getHomtabLogoPath();
 			boolean useDefault = true;
 			if (UiProperties.getInstance().isHometabCustomLogo() && imageLocation != null && imageLocation.length() > 0) {
 				// Absolute or relative path provided
 				try {
 					BufferedImage homeTabLogoImage = ImageIO.read(new File(imageLocation));
					ui.setIcon(lbLogo, homeTabLogoImage);
 					useDefault = false;
 				} catch (IOException e) {
 					// We are unable to find the specified image, using the default
 					log.warn("We are unable to find the specified image [" + imageLocation + "], using the default one.", e);
 				}
 			}
 			if (useDefault) {
 				// We go for the default one, inside the package
 				ui.setIcon(lbLogo, ui.getIcon(FRONTLINE_LOGO));
 			}
 		}
 	}
 	
 
 //> UI HELPER METHODS
 	private Object getRow(Event newEvent) {
 		Object row = ui.createTableRow(newEvent);
 		String icon = null;
 		switch(newEvent.getType()) {
 		case Event.TYPE_INCOMING_MESSAGE:
 			icon = Icon.SMS_RECEIVE;
 			break;
 		case Event.TYPE_OUTGOING_MESSAGE:
 			icon = Icon.SMS_SEND;
 			break;
 		case Event.TYPE_OUTGOING_MESSAGE_FAILED:
 			icon = Icon.SMS_SEND_FAILURE;
 			break;
 		case Event.TYPE_OUTGOING_EMAIL:
 			icon = Icon.EMAIL_SEND;
 			break;
 		case Event.TYPE_PHONE_CONNECTED:
 			icon = Icon.PHONE_CONNECTED;
 			break;
 		case Event.TYPE_SMS_INTERNET_SERVICE_CONNECTED:
 			icon = Icon.SMS_INTERNET_SERVICE_CONNECTED;
 			break;
 		case Event.TYPE_SMS_INTERNET_SERVICE_RECEIVING_FAILED:
 			icon = Icon.SMS_INTERNET_SERVICE_RECEIVING_FAILED;
 			break;
 		}
 		
 		Object cell = ui.createTableCell("");
 		ui.setIcon(cell, icon);
 		ui.add(row, cell);
 		ui.add(row, ui.createTableCell(newEvent.getDescription()));
 		ui.add(row, ui.createTableCell(InternationalisationUtils.getDatetimeFormat().format(newEvent.getTime())));
 		return row;
 	}
 
 //> LISTENER EVENT METHODS
 	public void newEvent(Event newEvent) {
 		Object eventListComponent = find(COMPONENT_EVENTS_LIST);
 		if(eventListComponent != null) {
 			if (ui.getItems(eventListComponent).length >= HomeTabHandler.EVENTS_LIMIT) {
 				ui.remove(ui.getItem(eventListComponent, 0));
 			}
 			ui.add(eventListComponent, getRow(newEvent));
 		}
 	}
 
 //> STATIC FACTORIES
 
 //> STATIC HELPER METHODS
 }
