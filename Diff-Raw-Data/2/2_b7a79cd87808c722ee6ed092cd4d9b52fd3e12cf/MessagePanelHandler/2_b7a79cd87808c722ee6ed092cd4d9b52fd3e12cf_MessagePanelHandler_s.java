 /**
  * 
  */
 package net.frontlinesms.ui.handler.message;
 
 // TODO Remove static imports
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_NO_CONTACT_SELECTED;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_ESTIMATED_MONEY;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_FIRST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_MSG_NUMBER;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_REMAINING_CHARS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_SECOND;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_THIRD;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LB_TOO_MANY_MESSAGES;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_MESSAGE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_TF_RECIPIENT;
 
 import java.awt.Color;
 
 import net.frontlinesms.FrontlineSMSConstants;
 import net.frontlinesms.Utils;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Message;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.UiGeneratorControllerConstants;
 import net.frontlinesms.ui.UiProperties;
 import net.frontlinesms.ui.handler.contacts.ContactSelecter;
 import net.frontlinesms.ui.handler.core.DatabaseSettingsPanel;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 import org.apache.log4j.Logger;
 import org.smslib.util.GsmAlphabet;
 
 /**
  * Controller for a panel which allows sending of text SMS messages
  * @author Alex
  */
 public class MessagePanelHandler implements ThinletUiEventHandler {
 //> STATIC CONSTANTS
 	/** UI XML File Path: the panel containing the messaging controls */
 	protected static final String UI_FILE_MESSAGE_PANEL = "/ui/core/messages/pnComposeMessage.xml";
 	
 //> THINLET COMPONENTS
 	/** Thinlet component name: Button to send message */
 	private static final String COMPONENT_BT_SEND = "btSend";
 
 //> INSTANCE PROPERTIES
 	/** Logging obhect */
 	private final Logger log = Utils.getLogger(this.getClass());
 	/** The {@link UiGeneratorController} that shows the tab. */
 	private final UiGeneratorController uiController;
 	/** The parent component */
 	private Object messagePanel;
 	/** The number of people the current SMS will be sent to */
 	private int numberToSend = 1;
 	/** The boolean changing whether the recipient field should be displayed */
 	private boolean shouldDisplayRecipientField;
 
 //> CONSTRUCTORS
 	/**
 	 * @param uiController
 	 */
 	private MessagePanelHandler(UiGeneratorController uiController, boolean shouldDisplay) {
 		this.uiController = uiController;
 		this.shouldDisplayRecipientField = shouldDisplay;
 	}
 	
 	private synchronized void init() {
 		assert(this.messagePanel == null) : "This has already been initialised.";
 		this.messagePanel = uiController.loadComponentFromFile(UI_FILE_MESSAGE_PANEL, this);
 		Object 	pnRecipient 		= uiController.find(this.messagePanel, UiGeneratorControllerConstants.COMPONENT_PN_MESSAGE_RECIPIENT),
 				lbTooManyMessages 	= uiController.find(this.messagePanel, UiGeneratorControllerConstants.COMPONENT_LB_TOO_MANY_MESSAGES);
 		uiController.setVisible(pnRecipient, shouldDisplayRecipientField);
 		uiController.setVisible(lbTooManyMessages, false);
 		uiController.setColor(lbTooManyMessages, "foreground", Color.RED);
 		messageChanged("", "");
 	}
 
 //> ACCESSORS
 	/** @return {@link #messagePanel} */
 	public Object getPanel() {
 		return this.messagePanel;
 	}
 	
 	private double getCostPerSms() {
 		return UiProperties.getInstance().getCostPerSms();
 	}
 	
 //> THINLET UI METHODS
 	/** Sets the method called by the send button at the bottom of the compose message panel */
 	public void setSendButtonMethod(ThinletUiEventHandler eventHandler, Object rootComponent, String methodCall) {
 		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
 		uiController.setAction(sendButton, methodCall, rootComponent, eventHandler);
 	}
 	
 	/**
 	 * Extract message details from the controls in the panel, and send an SMS.
 	 */
 	public void send() {
 		String recipient = uiController.getText(uiController.find(COMPONENT_TF_RECIPIENT));
 		String message = uiController.getText(uiController.find(COMPONENT_TF_MESSAGE));
 		
 		if (recipient.equals("")) {
 			uiController.alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_BLANK_PHONE_NUMBER));
 			return;
 		} 
 		this.uiController.getFrontlineController().sendTextMessage(recipient, message);
 		clearMessageComponent();
 	}
 	
 	/**
 	 * Event triggered when the message recipient has changed
 	 * @param text the new text value for the message recipient
 	 * 
 	 */
 	public void recipientChanged(String recipient, String message) {
 		int recipientLength = recipient.length(),
 			messageLength = message.length();
 		
 		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
 		
 		int totalLengthAllowed;
 		if(GsmAlphabet.areAllCharactersValidGSM(message))totalLengthAllowed = Message.SMS_MULTIPART_LENGTH_LIMIT * Message.SMS_LIMIT;
 		else totalLengthAllowed = Message.SMS_MULTIPART_LENGTH_LIMIT_UCS2 * Message.SMS_LIMIT;
 		
 		boolean shouldEnableSendButton = (sendButton != null
 											&& messageLength <= totalLengthAllowed
 											&& recipientLength > 0
 											&& messageLength > 0);
 		uiController.setEnabled(sendButton, shouldEnableSendButton);
 	}
 	
 	/** Method which triggers showing of the contact selecter. */
 	public void selectMessageRecipient() {
 		ContactSelecter contactSelecter = new ContactSelecter(this.uiController);
 		contactSelecter.show(InternationalisationUtils.getI18NString(FrontlineSMSConstants.SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE), "setRecipientTextfield(contactSelecter_contactList, contactSelecter)", null, this);
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
 		Object 	tfRecipient = uiController.find(this.messagePanel, UiGeneratorControllerConstants.COMPONENT_TF_RECIPIENT),
 				tfMessage	= uiController.find(this.messagePanel, UiGeneratorControllerConstants.COMPONENT_TF_MESSAGE);
 		Object selectedItem = uiController.getSelectedItem(contactSelecter_contactList);
 		if (selectedItem == null) {
 			uiController.alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_NO_CONTACT_SELECTED));
 			return;
 		}
 		Contact selectedContact = uiController.getContact(selectedItem);
 		uiController.setText(tfRecipient, selectedContact.getPhoneNumber());
 		uiController.remove(dialog);
 		uiController.updateCost();
 		
 		// The recipient text has changed, we check whether the send button should be enabled
 		this.recipientChanged(uiController.getText(tfRecipient), uiController.getText(tfMessage));
 	}
 	
 	/**
 	 * Event triggered when the message details have changed
 	 * @param panel TODO this should be removed
 	 * @param message the new text value for the message body
 	 * 
 	 */
 	public void messageChanged(String recipient, String message) {
 		int recipientLength = recipient.length(),
 			messageLength = message.length();
 		
 		if (messageLength == 0) {
 			clearMessageComponent();
 			return;
 		}
 		
 		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
 		boolean areAllCharactersValidGSM = GsmAlphabet.areAllCharactersValidGSM(message);
 		int totalLengthAllowed;
 		if(areAllCharactersValidGSM) totalLengthAllowed = Message.SMS_MULTIPART_LENGTH_LIMIT * Message.SMS_LIMIT;
 		else totalLengthAllowed = Message.SMS_MULTIPART_LENGTH_LIMIT_UCS2 * Message.SMS_LIMIT;
 		
 		boolean shouldEnableSendButton = (sendButton != null 
 											&& messageLength <= totalLengthAllowed
 											&& (!shouldDisplayRecipientField || recipientLength > 0));
 		uiController.setEnabled(sendButton, shouldEnableSendButton);
 		
 		
 		int singleMessageCharacterLimit;
 		int multipartMessageCharacterLimit;
 		if(areAllCharactersValidGSM) {
 			singleMessageCharacterLimit = Message.SMS_LENGTH_LIMIT;
 			multipartMessageCharacterLimit = Message.SMS_MULTIPART_LENGTH_LIMIT;
 		} else {
 			// It appears there are some unicode-only characters here.  We should therefore
 			// treat this message as if it will be sent as unicode.
 			singleMessageCharacterLimit = Message.SMS_LENGTH_LIMIT_UCS2;
 			multipartMessageCharacterLimit = Message.SMS_MULTIPART_LENGTH_LIMIT_UCS2;
 		}
 		
 		Object 	tfMessage = uiController.find(this.messagePanel, COMPONENT_TF_MESSAGE),
 				lbTooManyMessages = uiController.find(this.messagePanel, COMPONENT_LB_TOO_MANY_MESSAGES);
 		
 		int numberOfMsgs, remaining;
 		double costEstimate;
 		
 		
 		if (messageLength > totalLengthAllowed) {
 			remaining = 0;
 			numberOfMsgs = (int)Math.ceil(messageLength / multipartMessageCharacterLimit) + 1;
 			costEstimate = 0;
 			
 			uiController.setVisible(lbTooManyMessages, true);
 			uiController.setColor(tfMessage, "foreground", Color.RED);
 		} 
 		else {
 			uiController.setVisible(lbTooManyMessages, false);
 			uiController.setColor(tfMessage, "foreground", Color.BLACK);
 
 			if (messageLength <= singleMessageCharacterLimit) {
 			//First message
 				remaining = (messageLength % singleMessageCharacterLimit) == 0 ? 0
 						: singleMessageCharacterLimit - (messageLength % singleMessageCharacterLimit);
 				numberOfMsgs = messageLength == 0 ? 0 : 1;
 			} else if (messageLength <= (2*multipartMessageCharacterLimit)) {
 				numberOfMsgs = 2;
 				int charCount = messageLength - multipartMessageCharacterLimit;
 				remaining = (charCount % multipartMessageCharacterLimit) == 0 ? 0
 						: multipartMessageCharacterLimit - (charCount % multipartMessageCharacterLimit);
 			} else { //if (messageLength <= (3*multipartMessageCharacterLimit)) {
 				numberOfMsgs = 3;
 				int charCount = messageLength - multipartMessageCharacterLimit;
 				remaining = (charCount % multipartMessageCharacterLimit) == 0 ? 0
 						: multipartMessageCharacterLimit - (charCount % multipartMessageCharacterLimit);
 			}
 
 			costEstimate = numberOfMsgs * this.getCostPerSms() * this.numberToSend;
 		}
 		
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_REMAINING_CHARS), String.valueOf(remaining));
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_MSG_NUMBER), String.valueOf(numberOfMsgs));
 		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_FIRST), Icon.SMS_DISABLED);
 		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_SECOND), Icon.SMS_DISABLED);
 		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS_DISABLED);
 		if (numberOfMsgs >= 1) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_FIRST), Icon.SMS);
 		if (numberOfMsgs >= 2) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_SECOND), Icon.SMS);
 		if (numberOfMsgs == 3) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS);
 		if (numberOfMsgs > 3) uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS_DELETE);
 		
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_ESTIMATED_MONEY), InternationalisationUtils.formatCurrency(costEstimate));
 	}
 	
 	/**
 	 * Sets the phone number of the selected contact.
 	 * 
 	 * @param contactSelecter_contactList
 	 * @param dialog
 	 */
 	public void homeScreen_setRecipientTextfield(Object contactSelecter_contactList, Object dialog) {
 		Object tfRecipient = uiController.find(this.messagePanel, COMPONENT_TF_RECIPIENT);
 		Object selectedItem = uiController.getSelectedItem(contactSelecter_contactList);
 		if (selectedItem == null) {
 			uiController.alert(InternationalisationUtils.getI18NString(MESSAGE_NO_CONTACT_SELECTED));
 			return;
 		}
 		Contact selectedContact = uiController.getContact(selectedItem);
 		uiController.setText(tfRecipient, selectedContact.getPhoneNumber());
 		uiController.remove(dialog);
 		this.numberToSend = 1;
 		uiController.updateCost();
 	}
 
 //> INSTANCE HELPER METHODS
 	/**
 	 * Clear the details of a message component.
 	 * At some point, this should be nicely refactored so that a message component has its own controller.
 	 * @param panel
 	 */
 	private void clearMessageComponent() {
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_TF_RECIPIENT), "");
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_TF_MESSAGE), "");
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_REMAINING_CHARS), String.valueOf(Message.SMS_LENGTH_LIMIT));
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_MSG_NUMBER), "0");
 		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_FIRST), Icon.SMS_DISABLED);
 		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_SECOND), Icon.SMS_DISABLED);
 		uiController.setIcon(uiController.find(this.messagePanel, COMPONENT_LB_THIRD), Icon.SMS_DISABLED);
 		uiController.setText(uiController.find(this.messagePanel, COMPONENT_LB_ESTIMATED_MONEY), InternationalisationUtils.formatCurrency(0));
 		Object sendButton = uiController.find(this.messagePanel, COMPONENT_BT_SEND);
 		if (sendButton != null) uiController.setEnabled(sendButton, false);
 	}
 
 //> STATIC FACTORIES
 	/**
 	 * Create and initialise a new {@link MessagePanelHandler}.
 	 * @return a new, initialised instance of {@link MessagePanelHandler}
 	 */
 	public static final MessagePanelHandler create(UiGeneratorController ui, boolean shouldDisplayRecipientField) {
 		MessagePanelHandler handler = new MessagePanelHandler(ui, shouldDisplayRecipientField);
 		handler.init();
 		return handler;
 	}
 
 //> STATIC HELPER METHODS
 }
