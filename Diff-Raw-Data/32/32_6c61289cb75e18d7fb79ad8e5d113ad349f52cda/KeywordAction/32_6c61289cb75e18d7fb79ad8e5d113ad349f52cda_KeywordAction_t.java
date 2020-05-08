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
 package net.frontlinesms.data.domain;
 
 import javax.persistence.*;
 
 import net.frontlinesms.FrontlineSMSConstants;
 import net.frontlinesms.Utils;
 import net.frontlinesms.csv.CsvUtils;
 import net.frontlinesms.data.EntityField;
 
 /**
  * @author Alex Anderson alex(at)masabi(dot)com
  */
 @Entity
 public class KeywordAction {
 //> ENTITY FIELDS
 	/** Details of the fields that this class has. */
 	public enum Field implements EntityField<KeywordAction> {
 		/** Field name for {@link KeywordAction#type} */
 		TYPE("type"),
 		/** File name for {@link KeywordAction#keyword} */
 		KEYWORD("keyword");
 		/** name of a field */
 		private final String fieldName;
 		/**
 		 * Creates a new {@link Field}
 		 * @param fieldName name of the field
 		 */
 		Field(String fieldName) { this.fieldName = fieldName; }
 		/** @see EntityField#getFieldName() */
 		public String getFieldName() { return this.fieldName; }
 	}
 	
 //> CONSTRUCTORS
 	/**
 	 * Default constructor, to be used by hibernate.
 	 * This constructor should <b>not</b> be used in factory methods.
 	 */
 	KeywordAction() {}
 
 	/**
 	 * Creates a new keyword action and sets the keyword for it. 
 	 * @param type The type of this keyword.
 	 * @param keyword value for {@link #keyword}.
 	 */
 	KeywordAction(int type, Keyword keyword) {
 		assert(keyword!=null): "You must supply a " + Keyword.class.getSimpleName() + " for your new " + getClass().getSimpleName();
 		this.type = type;
 		this.keyword = keyword;
 	}
 	
 //> CONSTANTS
 	// TODO could make this an enum rather nicely
 	/** Action: forward the received message to a group */
 	public static final int TYPE_FORWARD = 0;
 	/** Action: add the sender's msisdn to a group */
 	public static final int TYPE_JOIN = 1;
 	/** Action: remove the sender's msisdn from a group */
 	public static final int TYPE_LEAVE = 2;
 	/** Reply: send a specified reply to the sender's msisdn */
 	public static final int TYPE_REPLY = 3;
 	/** Action: executes an external command */
 	public static final int TYPE_EXTERNAL_CMD = 4;
 	/** Action: send an e-mail */
 	public static final int TYPE_EMAIL = 5;
 	
 	// FIXME rename these - we can't have two types of types
 	public static final int EXTERNAL_HTTP_REQUEST = 0;
 	public static final int EXTERNAL_COMMAND_LINE = 1;
 	
 	public static final int EXTERNAL_RESPONSE_PLAIN_TEXT = 0;
 	public static final int EXTERNAL_RESPONSE_LIST_COMMANDS = 1;
 	public static final int EXTERNAL_RESPONSE_DONT_WAIT = 2;
 	
 	public static final int EXTERNAL_REPLY_AND_FORWARD = 1;
 	public static final int EXTERNAL_DO_NOTHING = 2;
 	
 //> INSTANCE PROPERTIES
 	/** Unique id for this entity.  This is for hibernate usage. */
 	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
 	private long id;
 	private int type;
 	/** Keyword which this action is attached to */
 	@ManyToOne(targetEntity=Keyword.class, optional=false)
 	private Keyword keyword;
 	@ManyToOne(optional=true)
 	private Group group;
 	@ManyToOne(optional=true)
 	private EmailAccount emailAccount;
 	private String commandString;
 	private int commandInteger;
 	private int counter;
 	private long startDate;
 	private long endDate;
 	private String emailRecipients;
 	private String emailSubject;
 	private int externalCommandType;
 	private String externalCommand;
 	private int externalCommandResponseType;
 	private int externalCommandResponseActionType;
 	
 //> ACCESSOR METHODS
 	/** @return {@link #type} */
 	public int getType() {
 		return this.type;
 	}
 	
 	/** @return the external command type */
 	public int getExternalCommandType() {
		assert(this.type==TYPE_EXTERNAL_CMD) : "This method cannot be called on an action of type " + type;
 		return this.externalCommandType;
 	}
 	
 	/** @return the external command response type */
 	public int getExternalCommandResponseType() {
		assert(this.type==TYPE_EXTERNAL_CMD) : "Cannot get command response from type: " + type;
 		return externalCommandResponseType;
 	}
 	
 	/**
 	 * Sets the external command response type of this instance of KeywordAction.
 	 * @param type new value for {@link #externalCommandResponseType}
 	 */
 	public void setExternalCommandResponseType(int type) {
		assert(this.type==TYPE_EXTERNAL_CMD) : "Cannot set command response from type: " + type;
 		this.externalCommandResponseType = type;
 	}
 	
 	/**
 	 * Gets the external command response action type of this instance of KeywordAction.
 	 * @return
 	 */
 	public int getCommandResponseActionType() {
		assert(this.type==TYPE_EXTERNAL_CMD) : "Cannot get command response action from type: " + type;
 		return externalCommandResponseActionType;
 	}
 	
 	/**
 	 * Sets the external command response action type of this instance of KeywordAction.
 	 * @param type
 	 */
 	public void setCommandResponseActionType(int type) {
		assert(this.type==TYPE_EXTERNAL_CMD) : "Cannot get command response action from type: " + type;
 		this.externalCommandResponseActionType = type;
 	}
 	
 	/** @param type new value for {@link #externalCommandType} */
 	public void setExternalCommandType(int type) {
 		this.externalCommandType = type;
 	}
 	
 	/** 
 	 * @param time The time that this action was triggered
 	 * @return <code>true</code> if the current date is within {@link #startDate} and {@link #endDate}; <code>false</code> otherwise.
 	 */
 	public boolean isAlive(long time) {
 		return time >= this.startDate && time <= this.endDate;
 	}
 	
 	/**
 	 * Gets this action start date.
 	 * @return {@link #startDate}
 	 */
 	public long getStartDate() {
 		return this.startDate;
 	}
 	
 	/**
 	 * Gets this action email recipients.
 	 * @return {@link #emailRecipients}
 	 */
 	public String getEmailRecipients() {
 		assert(this.type==TYPE_EMAIL) : "Cannot get email recipients from action of type: " + type;
 		return this.emailRecipients;
 	}
 	
 	/**
 	 * Gets this action email subject.
 	 * @return {@link #emailSubject} 
 	 */
 	public String getEmailSubject() {
 		assert(this.type==TYPE_EMAIL) : "Cannot get email subject from action of type: " + type;
 		return this.emailSubject;
 	}
 	
 	/** @return {@link #endDate} */
 	public long getEndDate() {
 		return this.endDate;
 	}
 	
 	/** @param date new value for {@link #startDate} */
 	public void setStartDate(long date) {
 		this.startDate = date;
 	}
 	
 	/** @param date new value for {@link #endDate} */
 	public void setEndDate(long date) {
 		this.endDate = date;
 	}
 	
 	/** @param group new value for {@link #group} */
 	public void setGroup(Group group) {
 		assert(hasGroup()) : "Cannot set group from action of type: " + type;
 		this.group = group;
 	}
 	
 	/**
 	 * Check if this action can have a group attached to it. 
 	 * @return <code>true</code> if a group may be attached to an action of this type; <code>false</code> otherwise.
 	 */
 	private boolean hasGroup() {
 		return this.type == TYPE_JOIN
 				|| this.type==TYPE_LEAVE
 				|| this.type==TYPE_FORWARD
 				|| this.type==TYPE_EXTERNAL_CMD;
 	}
 	
 	/**
 	 * Sets the email recipients of this instance of KeywordAction.
 	 * @param recipients new value for {@link #emailRecipients}
 	 */
 	public void setEmailRecipients(String recipients) {
 		assert(this.type==TYPE_EMAIL) : "Cannot set email recipients from action of type: " + type;
 		this.emailRecipients = recipients;
 	}
 	
 	/** @param subject new value for {@link #emailSubject} of {@link #TYPE_EMAIL} */
 	public void setEmailSubject(String subject) {
 		assert(this.type==TYPE_EMAIL) : "Cannot set email subject from action of type: " + type;
 		this.emailSubject = subject;
 	}
 	
 	/** @param emailAccount new value for {@link #emailAccount} of {@link #TYPE_EMAIL} */
 	public void setEmailAccount(EmailAccount emailAccount) {
 		assert(this.type==TYPE_EMAIL) : "Cannot get group from action of type: " + type;
 		this.emailAccount = emailAccount;
 	}
 	
 	/** @param text new value for {@link #commandString} of a {@link #TYPE_FORWARD} */
 	public void setForwardText(String text) {
		assert(this.type==TYPE_FORWARD) : "Cannot get forward text from action of type: " + type;
 		this.commandString = text;
 	}
 	
 	/** @param commandText new value for {@link #commandString} */
 	public void setCommandText(String commandText) {
 		this.commandString = commandText;
 	}
 	
 	/** @param commandLine new value for {@link #externalCommand} */
 	public void setCommandLine(String commandLine) {
 		this.externalCommand = commandLine;
 	}
 	
 	/** @return how many times this action was executed */
 	public int getCounter() {
 		return this.counter;
 	}
 	
 	/** Increments how many times this action was executed. */
 	public void incrementCounter() {
 		++counter;
 	}
 	
 	/** @return the group related to this keyword action */
 	public Group getGroup() {
 		assert(hasGroup()) : "Cannot get group from action of type: " + type;
 		return this.group;
 	}
 
 	/** @return the email account related to this keyword action */
 	public EmailAccount getEmailAccount() {
 		assert(this.type==TYPE_EMAIL) : "Cannot get group from action of type: " + type;
 		return this.emailAccount;
 	}
 	
 	/** @return {@link #unformattedReplyText} the reply text for this action (if it is of TYPE_REPLY or TYPE_EMAIL) */
 	public String getUnformattedReplyText() {
		assert(this.type==TYPE_REPLY || this.type==TYPE_EMAIL) : "Cannot get reply text from action of type: " + type;
 		return this.commandString;
 	}
 	
 	/**
 	 * If this action is of TYPE_REPLY or TYPE_EMAIL, sets the reply text. 
 	 * @param replyText new value for {@link #replyText}
 	 */
 	public void setReplyText(String replyText) {
		assert(this.type==TYPE_REPLY || this.type==TYPE_EMAIL) : "Cannot set reply text from action of type: " + type;
 		this.commandString = replyText;
 	}
 	
 	/** @return the forward text for this action (if it is of TYPE_FORWARD). */
 	public String getUnformattedForwardText() {
		assert(this.type==TYPE_FORWARD) : "Cannot get forward text from action of type: " + type;
 		return this.commandString;
 	}
 	
 	/** @return the command text for this action (if it is of TYPE_EXTERNAL_CMD). */
 	public String getUnformattedCommandText() {
		assert(this.type==TYPE_EXTERNAL_CMD) : "Cannot get command text from type: " + type;
 		return this.commandString;
 	}
 	
 	/** @return the command line for this action (if it is of TYPE_EXTERNAL_CMD). */
 	public String getUnformattedCommand() {
		assert(this.type==TYPE_EXTERNAL_CMD) : "Cannot get command from type: " + type;
 		return this.externalCommand;
 	}
 	
 	/**
 	 * Gets the keyword that this action is associated with.
 	 * @return {@link #keyword}
 	 */
 	public Keyword getKeyword() {
 		return this.keyword;
 	}
 	
 //> PRIVATE ACCESSORS
 	/** @param type value for {@link #type} */
 	private void setType(int type) {
 		this.type = type;
 	}
 	/** @param keyword value for {@link #keyword} */
 	private void setKeyword(Keyword keyword) {
 		this.keyword = keyword;
 	}
 	/** @param counter value for {@link #counter} */
 	private void setCounter(int counter) {
 		this.counter = counter;
 	}
 
 //> STATIC HELPER METHODS
 	public static class KeywordUtils {
 		public static final String personaliseMessage(Contact contact, String messageText) {
 			// Replace any user-defined variables they might have been included
 			messageText = messageText.replace(FrontlineSMSConstants.USER_MARKER_TO_NAME, contact.getName());
 			return messageText;
 		}
 		
 		/**
 		 * Creates the formatted reply text for this action from an incoming message.
 		 * 
 		 * If this action is not of TYPE_REPLY, throws an IllegalStateException.
 		 * @param senderMsisdn
 		 * @param incomingMessageText
 		 * @return
 		 * TODO remove incomingKeyword parameter
 		 */
 		public static final String getReplyText(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText, String incomingKeyword)  {
 			String senderDisplayName;
 			if(sender != null) senderDisplayName = sender.getDisplayName();
 			else senderDisplayName = senderMsisdn;
 			return formatText(action.getUnformattedReplyText(), false, action, senderMsisdn, senderDisplayName, incomingMessageText);
 		}
 		
 		/**
 		 * Creates the formatted reply text for this action from an incoming message.
 		 * 
 		 * If this action is not of TYPE_REPLY, throws an IllegalStateException.
 		 * @param senderMsisdn
 		 * @param incomingMessageText
 		 * @return
 		 */
 		public static final String getEmailSubject(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText, String incomingKeyword) throws IllegalStateException {
 			String senderDisplayName;
 			if(sender != null) senderDisplayName = sender.getDisplayName();
 			else senderDisplayName = senderMsisdn;
 			
 			return formatText(action.getEmailSubject(), false, action, senderMsisdn, senderDisplayName, incomingMessageText);
 		}
 		
 		/**
 		 * Creates the formatted external command or email for this action from an incoming message.
 		 * 
 		 * If this action is not of TYPE_EXTERNAL_CMD, throws an IllegalStateException.
 		 * @param action 
 		 * @param sender 
 		 * @param senderMsisdn
 		 * @param incomingMessageText
 		 * @return
 		 */
 		public static final String getExternalCommand(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText) {
 			String senderDisplayName;
 			if (sender != null) senderDisplayName = sender.getDisplayName();
 			else senderDisplayName = senderMsisdn;
 			return formatText(action.getUnformattedCommand(), true, action, senderMsisdn, senderDisplayName, incomingMessageText);
 		}
 		
 		
 		/**
 		 * Creates the formatted external command reply for this action from an incoming message.
 		 * 
 		 * If this action is not of TYPE_EXTERNAL_CMD, throws an IllegalStateException.
 		 * @param action
 		 * @param response
 		 * @return
 		 * @throws IllegalStateException
 		 */
 		public static final String getExternalCommandReplyMessage(KeywordAction action, String response) {
 			return KeywordUtils.getFormattedCommandReply(action, response);
 		}
 		
 		/**
 		 * Creates the formatted forward text for this action from an incoming message.
 		 * 
 		 * If this action is not of TYPE_FORWARD an IllegalStateException should be thrown.
 		 * @param sender The Contact object representing the sender of this message, or NULL if this msisdn is not associated with a Contact.
 		 * @param senderMsisdn The msisdn from which the message
 		 * @param incomingMessageText The text of the received message.
 		 * @return
 		 */
 		public static final String getForwardText(KeywordAction action, Contact sender, String senderMsisdn, String incomingMessageText) {
 			String senderDisplayName;
 			if(sender != null) senderDisplayName = sender.getDisplayName();
 			else senderDisplayName = senderMsisdn;
 			return formatText(action.getUnformattedForwardText(), false, action, senderMsisdn, senderDisplayName, incomingMessageText);
 		}
 		
 		/**
 		 * Formats a message, inserting particular variables where their presence has been requested by placeholders.
 		 */
 		protected static final String getFormattedCommandReply(KeywordAction action, String response) {
 			String command = action.getUnformattedCommandText();
 			 
 			command = command.replace(CsvUtils.MARKER_COMMAND_RESPONSE, response);
 			
 			return command;
 		}
 		
 		/**
 		 * Remove the keyword from the start of a received message.  If called on text that does not start with the
 		 * keyword, the text will be returned unchanged.
 		 * @param messageText
 		 * @param keywordString
 		 * @return
 		 */
 		static final String removeKeyword(String messageText, String keywordString) {
 			String keywordInMessage = extractKeyword(messageText, keywordString);
 			if(keywordInMessage == null) {
 				return messageText;
 			} else {
 				if(messageText.length() == keywordString.length()) return "";
 				else return messageText.substring(keywordString.length() + 1);
 			}
 		}
 		
 		/**
 		 * Extracts the keyword from the start of a message, and returns the keyword as it appeared in
 		 * the message.
 		 * @param messageText
 		 * @param keywordString
 		 * @return the keyword string <em>as it appears at the start of messageText</em> or <code>null</code> if messageText does not start with keyword.
 		 */
 		static final String extractKeyword(String messageText, String keywordString) {
 			int keywordLength = keywordString.length();
 			if(messageText.length() == keywordLength) {
 				if(messageText.equalsIgnoreCase(keywordString)) {
 					return messageText;
 				}
 			} else if(messageText.length() > keywordLength) {
 				String keywordInMessage = messageText.substring(0, keywordLength);
 				if(keywordInMessage.equalsIgnoreCase(keywordString)) {
 					char charAfterKeyword = messageText.charAt(keywordLength);
 					if(charAfterKeyword == ' '
 							||charAfterKeyword == '\n'
 							||charAfterKeyword == '\r') {
 						return keywordInMessage;
 					}
 				}
 			}
 			return null;
 		}
 		
 		static String formatText(String unformattedText, boolean urlEncode, KeywordAction action, String senderMsisdn, String senderDisplayName, String incomingMessageText) {
 			String keywordString = action.getKeyword().getKeyword();
 			
 			String keywordInMessage = extractKeyword(incomingMessageText, keywordString);
 			String messageWithoutKeyword = removeKeyword(incomingMessageText, keywordString);
 			
 			if(urlEncode) {
 				senderMsisdn = Utils.urlEncode(senderMsisdn);
 				keywordInMessage = Utils.urlEncode(keywordInMessage);
 				senderDisplayName = Utils.urlEncode(senderDisplayName);
 				messageWithoutKeyword = Utils.urlEncode(messageWithoutKeyword);
 			}
 			
 			// TODO perhaps all variables should be subbed?
 			return Utils.replace(unformattedText,
 					CsvUtils.MARKER_SENDER_NUMBER,		/*->*/ senderMsisdn,
 					CsvUtils.MARKER_KEYWORD_KEY,		/*->*/ keywordInMessage,
 					CsvUtils.MARKER_SENDER_NAME,		/*->*/ senderDisplayName,
 					// N.B. message content should always be substituted last to prevent injection attacks
 					CsvUtils.MARKER_MESSAGE_CONTENT,	/*->*/ messageWithoutKeyword 
 					);
 		}
 	}
 	
 //> STATIC FACTORY METHODS
 	/**
 	 * Creates a keyword action to automatically REPLY to messages.
 	 * @param keyword The keyword that triggers this action
 	 * @param replyText The text to reply with when this action is triggered
 	 * @param start 
 	 * @param end 
 	 * @return a new instance of KeywordAction
 	 */
 	public static KeywordAction createReplyAction(Keyword keyword, String replyText, long start, long end) {
 		KeywordAction action = new KeywordAction(TYPE_REPLY, keyword);
 		action.setReplyText(replyText);
 		action.setStartDate(start);
 		action.setEndDate(end);
 		return action;
 	}
 	
 	/**
 	 * Creates a keyword action to automatically send Email.
 	 * @param keyword The keyword that triggers this action
 	 * @param replyText The text to reply with when this action is triggered
 	 * @param account 
 	 * @param to 
 	 * @param subject 
 	 * @param start 
 	 * @param end 
 	 * @return a new instance of KeywordAction
 	 */
 	public static KeywordAction createEmailAction(Keyword keyword, String replyText, EmailAccount account, String to, String subject,long start, long end) {
 		KeywordAction action = new KeywordAction(TYPE_EMAIL, keyword);
 		action.setReplyText(replyText);
 		action.setEmailAccount(account);
 		action.setEmailRecipients(to);
 		action.setEmailSubject(subject);
 		action.setStartDate(start);
 		action.setEndDate(end);
 		return action;
 	}
 	
 	/**
 	 * Creates a keyword action to automatically execute a external command.
 	 * @param keyword The keyword that triggers this action
 	 * @param commandLine The command to be executed
 	 * @param commandType 
 	 * <li> HTTP request
 	 * <li> Command line execution.
 	 * @param responseType 
 	 * <li> Plain Text 
 	 * <li> Command list
 	 * <li> No response
 	 * @param responseActionType
 	 * <li> Forward to Group Only 
 	 * <li> Auto Reply Only
 	 * <li> Both
 	 * <li> Neither
 	 * @param commandMsg The message to be sent with response.
 	 * @param toFwd The group to be forwarded.
 	 * @param start
 	 * @param end
 	 * @return a new instance of KeywordAction
 	 */
 	public static KeywordAction createExternalCommandAction(Keyword keyword, String commandLine, int commandType, int responseType,
 			int responseActionType, String commandMsg, Group toFwd, long start, long end) {
 		KeywordAction action = new KeywordAction(TYPE_EXTERNAL_CMD, keyword);
 		action.setCommandLine(commandLine);
 		action.setExternalCommandType(commandType);
 		action.setExternalCommandResponseType(responseType);
 		action.setCommandResponseActionType(responseActionType);
 		action.setCommandText(commandMsg);
 		action.setGroup(toFwd);
 		action.setStartDate(start);
 		action.setEndDate(end);
 		return action;
 	}
 	
 	/**
 	 * Creates a keyword action to automatically add a contact to a group.
 	 * @param keyword The keyword that triggers this action
 	 * @param group The group to add the sender to when this action is triggered
 	 * @return a new instance of KeywordAction
 	 */
 	public static KeywordAction createGroupJoinAction(Keyword keyword, Group group, long start, long end) {
 		KeywordAction action = new KeywordAction(TYPE_JOIN, keyword);
 		action.setGroup(group);
 		action.setStartDate(start);
 		action.setEndDate(end);
 		return action;
 	}
 	
 	/**
 	 * Creates a keyword action to automatically remove a contact from a group.
 	 * @param keyword The keyword that triggers this action
 	 * @param group The group to remove the sender from when this action is triggered
 	 * @return a new instance of KeywordAction
 	 */
 	public static KeywordAction createGroupLeaveAction(Keyword keyword, Group group, long start, long end) {
 		KeywordAction action = new KeywordAction(TYPE_LEAVE, keyword);
 		action.setGroup(group);
 		action.setStartDate(start);
 		action.setEndDate(end);
 		return action;
 	}
 	
 	/**
 	 * Creates a keyword action to automatically forward a message to a group
 	 * @param keyword The keyword that triggers the action
 	 * @param group The group to forward the message onto
 	 * @param forwardText The message text to forward on to the group
 	 * @return a new instance of KeywordAction
 	 */
 	public static KeywordAction createForwardAction(Keyword keyword, Group group, String forwardText, long start, long end) {
 		KeywordAction action = new KeywordAction(TYPE_FORWARD, keyword);
 		action.setGroup(group);
 		action.setForwardText(forwardText);
 		action.setStartDate(start);
 		action.setEndDate(end);
 		return action;
 	}
 
 //> GENERATED CODE
 	/** @see java.lang.Object#hashCode() */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + commandInteger;
 		result = prime * result
 				+ ((commandString == null) ? 0 : commandString.hashCode());
 		result = prime * result
 				+ ((emailRecipients == null) ? 0 : emailRecipients.hashCode());
 		result = prime * result
 				+ ((emailSubject == null) ? 0 : emailSubject.hashCode());
 		result = prime * result + (int) (endDate ^ (endDate >>> 32));
 		result = prime * result
 				+ ((externalCommand == null) ? 0 : externalCommand.hashCode());
 		result = prime * result + externalCommandResponseActionType;
 		result = prime * result + externalCommandResponseType;
 		result = prime * result + externalCommandType;
 		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
 		result = prime * result + (int) (startDate ^ (startDate >>> 32));
 		result = prime * result + type;
 		return result;
 	}
 
 	/** @see java.lang.Object#equals(java.lang.Object) */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		KeywordAction other = (KeywordAction) obj;
 		if (commandInteger != other.commandInteger)
 			return false;
 		if (commandString == null) {
 			if (other.commandString != null)
 				return false;
 		} else if (!commandString.equals(other.commandString))
 			return false;
 		if (emailRecipients == null) {
 			if (other.emailRecipients != null)
 				return false;
 		} else if (!emailRecipients.equals(other.emailRecipients))
 			return false;
 		if (emailSubject == null) {
 			if (other.emailSubject != null)
 				return false;
 		} else if (!emailSubject.equals(other.emailSubject))
 			return false;
 		if (endDate != other.endDate)
 			return false;
 		if (externalCommand == null) {
 			if (other.externalCommand != null)
 				return false;
 		} else if (!externalCommand.equals(other.externalCommand))
 			return false;
 		if (externalCommandResponseActionType != other.externalCommandResponseActionType)
 			return false;
 		if (externalCommandResponseType != other.externalCommandResponseType)
 			return false;
 		if (externalCommandType != other.externalCommandType)
 			return false;
 		if (keyword == null) {
 			if (other.keyword != null)
 				return false;
 		} else if (!keyword.equals(other.keyword))
 			return false;
 		if (startDate != other.startDate)
 			return false;
 		if (type != other.type)
 			return false;
 		return true;
 	}
 }
