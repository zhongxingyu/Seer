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
 
 import java.util.Arrays;
 
 import javax.persistence.*;
 
import org.hibernate.annotations.DiscriminatorFormula;
 import org.smslib.util.HexUtils;
 import org.smslib.util.TpduUtils;
 
 import net.frontlinesms.data.EntityField;
 
 /**
  * Object representing an SMS message in our data structure.
  * @author Alex
  */
 @Entity
 // This class is mapped to the database table called "message", as this class used to be called "Message"
 @Table(name="message")
@DiscriminatorFormula("(CASE WHEN dtype IS NULL THEN 'FrontlineMessage' ELSE dtype END)")
 public class FrontlineMessage {
	/** Discriminator column for this class.  This was only implemented when {@link FrontlineMultimediaMessage} was
	 * added.  Setting it to null will result in a plain {@link FrontlineMessage} being instantiated, as per the
	 * {@link DiscriminatorFormula} annotation on this class. */
	private String dtype = this.getClass().getSimpleName();
	
 //> DATABASE COLUMN NAMES
 	/** Database column name for field {@link #textMessageContent} */
 	private static final String COLUMN_TEXT_CONTENT = "textContent";
 	
 //> CONSTANTS
 	public enum Type {
 		/** This is a pseudo-message type, used as a blanket for all types. */
 		ALL,
 		/** Message type: unknown */
 		UNKNOWN,
 		/** Message type: received */
 		RECEIVED,
 		/** Message type: outbound */
 		OUTBOUND,
 		/** Message type: delivery report */
 		DELIVERY_REPORT;
 	}
 	
 	public enum Status {
 		/** Message status: DRAFT - nothing has been done with this message yet */
 		DRAFT,
 		/** messages of TYPE_RECEIVED should always be STATUS_RECEIVED */
 		RECEIVED,
 		/** outgoing message that is created, and will be sent to a phone as soon as one is available */
 		OUTBOX,
 		/** outgoing message given to a phone, which the phone is trying to send */
 		PENDING,
 		/** outgoing message successfully delivered to the GSM network*/
 		SENT,
 		/** outgoing message that has had delivery confirmed by the GSM network */
 		DELIVERED,
 		/** Outgoing message that had status KEEP TRYING returned by the GSM network */
 		KEEP_TRYING,
 		/** Outgoing message that had status ABORTED returned by the GSM network */
 		ABORTED,
 		/** Outgoing message that had status UNKNOWN returned by the GSM network */
 		UNKNOWN,
 		/** Outgoing message that had status FAILED returned by the GSM network */
 		FAILED;
 		
 	}
 	
 	/** Number of times a failed message send is retried before status is set to STATUS_FAILED */
 	public static final int MAX_RETRIES = 2;
 	
 	/** The maximum number of parts in an SMS message.  TODO rename this SMS_PART_LIMIT */
 	public static final int SMS_LIMIT = 255;
 	/** Maximum number of characters that can be fit into a single 7-bit GSM SMS message. TODO this value should probably be fetched from {@link TpduUtils}. */
 	public static final int SMS_LENGTH_LIMIT = 160;
 	/** Maximum number of characters that can be fit in one part of a multipart 7-bit GSM SMS message.  TODO this number is incorrect, I suspect.  The value should probably be fetched from {@link TpduUtils}. */
 	public static final int SMS_MULTIPART_LENGTH_LIMIT = 135;
 	/** Maximum number of characters that can be fit into a single UCS-2 SMS message. TODO this value should probably be fetched from {@link TpduUtils}. */
 	public static final int SMS_LENGTH_LIMIT_UCS2 = 70;
 	/** Maximum number of characters that can be fit in one part of a multipart UCS-2 SMS message.  TODO this number is incorrect, I suspect.  The value should probably be fetched from {@link TpduUtils}. */
 	public static final int SMS_MULTIPART_LENGTH_LIMIT_UCS2 = 60;
 	/** Maximum number of characters that can be fit into a 255-part GSM 7bit message */
 	public static final int SMS_MAX_CHARACTERS = 39015;
 	
 
 
 //> ENTITY FIELDS
 	/** Details of the fields that this class has. */
 	public enum Field implements EntityField<FrontlineMessage> {
 		TYPE("type"),
 		DATE("date"),
 		STATUS("status"),
 		SENDER_MSISDN("senderMsisdn"),
 		RECIPIENT_MSISDN("recipientMsisdn"),
 		MESSAGE_CONTENT("textMessageContent"),
 		SMSC_REFERENCE("smscReference");
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
 	
 //> INSTANCE PROPERTIES
 	/** Unique id for this entity.  This is for hibernate usage. */
 	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
 	private long id;
 	private Type type;
 	private int retriesRemaining;
 	private Status status;
 	private String recipientMsisdn;
 	private int recipientSmsPort;
 	private int smsPartsCount;
 	private long date;
 	private Integer smscReference;
 	private String senderMsisdn;
 	/** Text content of this message. */
 	@Column(name=COLUMN_TEXT_CONTENT, length=SMS_MAX_CHARACTERS)
 	private String textMessageContent;
 	/** Binary content of this message. */
 	private byte[] binaryMessageContent;
 	
 //> CONSTRUCTOR
 	/** Default constructor empty for hibernate */
 	FrontlineMessage() {}
 	
 	protected FrontlineMessage(Type type, String textContent) {
 		this.type = type;
 		this.textMessageContent = textContent;
 	}
 	
 //> ACCESSOR METHODS
 	/**
 	 * Gets the type of this Message.  Should be one of the Message.TYPE_ constants.
 	 * @return
 	 */
 	public Type getType() {
 		return this.type;
 	}
 	
 	/**
 	 * Gets the status of this Message.  Should be one of the Message.STATUS_ constants.
 	 * @return
 	 */
 	public Status getStatus() {
 		return this.status;
 	}
 	
 	/**
 	 * sets the type of this Message.  Should be one of the Message.STATUS_ constants.
 	 * only allows you to change the status of an outgoing message
 	 * @param messageStatus
 	 */
 	public void setStatus(Status messageStatus) {
 		this.status = messageStatus;
 	}
 
 	/**
 	 * Gets the MSISDN (phone number) of the sender of this message.
 	 * @return
 	 */
 	public String getSenderMsisdn() {
 		return this.senderMsisdn;
 	}
 	
 	/**
 	 * sets the sender number of an outgoing message, 
 	 * usually done once it is assigned to an outgoing device, 
 	 * if the MSISDN is known, or manually assigned to the device.
 	 * @param senderPhoneNumber new value for {@link #senderMsisdn}
 	 */
 	public void setSenderMsisdn(String senderPhoneNumber) {
 		this.senderMsisdn = senderPhoneNumber;
 	}
 	
 	/**
 	 * Sets {@link #recipientMsisdn}
 	 * @param recipientPhoneNumber new value for {@link #recipientMsisdn}
 	 */
 	public void setRecipientMsisdn(String recipientPhoneNumber) {
 		this.recipientMsisdn = recipientPhoneNumber;
 	}
 	
 	/**
 	 * Sets {@link #recipientSmsPort}
 	 * @param recipientSmsPort new value for {@link #recipientSmsPort}
 	 */
 	public void setRecipientSmsPort(int recipientSmsPort) {
 		this.recipientSmsPort = recipientSmsPort;
 	}
 	
 	/**
 	 * Gets the MSISDN (phone number) of the recipient of this message.
 	 * @return
 	 */
 	public String getRecipientMsisdn() {
 		return this.recipientMsisdn;
 	}
 	
 	/**
 	 * Gets the sms port of the recipient of this message, or -1
 	 * if none is specified.
 	 * @return {@link #recipientSmsPort}
 	 */
 	public int getRecipientSmsPort() {
 		return this.recipientSmsPort;
 	}
 	
 	/**
 	 * Gets the text content of this message.
 	 * @return {@link #textMessageContent}
 	 */
 	public String getTextContent() {
 		return this.textMessageContent;
 	}
 	
 	/**
 	 * Gets the binary content of this message.
 	 * @return {@link #binaryMessageContent}
 	 */
 	public byte[] getBinaryContent() {
 		return this.binaryMessageContent;
 	}
 	
 	/**
 	 * Gets the number of SMS sent.
 	 * @return the number of parts this message was sent as
 	 */
 	public int getNumberOfSMS() {
 		return this.smsPartsCount;
 	}
 	
 	/**
 	 * Gets the date at which this message was sent (messages of TYPE_SENT)
 	 * or received (messages of TYPE_RECEIVED).
 	 * @return
 	 */
 	public long getDate() {
 		return this.date;
 	}
 
 	/**
 	 * @return the SMSC reference number of this Message.  this appears after a message is sent, so that 
 	 * delivery reciepts can be matched up to previous messages.
 	 */
 	public Integer getSmscReference() {
 		return this.smscReference;
 	}
 	/**
 	 * sets the SMSC reference number of this Message.  this appears after a message is sent, so that 
 	 * delivery reciepts can be matched up to previous messages.
 	 * Don't set this for incoming messages
 	 * @param smscReference
 	 */
 	public void setSmscReference(int smscReference) {
 		this.smscReference = smscReference;
 	}
 	
 	/** @return the retries left for this message */
 	public int getRetriesRemaining() {
 		return this.retriesRemaining;
 	}
 	/** sets the retries left for this message */
 	public void setRetriesRemaining(int retries) {
 		this.retriesRemaining = retries;
 	}
 
 	/**
 	 * Check whether the content of this message is binary or text
 	 * @return <code>true</code> if the content of this message is binary; <code>false</code> otherwise.
 	 */
 	public boolean isBinaryMessage() {
 		return this.binaryMessageContent != null;
 	}
 	
 //> STATIC FACTORY METHODS
 	/**
 	 * Creates an binary incoming message in the internal data structure.
 	 * @param dateReceived The date this message was received.
 	 * @param senderMsisdn The MSISDN (phone number) of the sender of this message.
 	 * @param recipientMsisdn The MSISDN (phone number) of the recipient of this message.
 	 * @param recipientPort 
 	 * @param content 
 	 * @return Message object representing the sent message.
 	 */
 	public static FrontlineMessage createBinaryIncomingMessage(long dateReceived, String senderMsisdn, String recipientMsisdn, int recipientPort, byte[] content) {
 		FrontlineMessage m = new FrontlineMessage();
 		m.type = Type.RECEIVED;
 		m.status = Status.RECEIVED;
 		m.setDate(dateReceived);
 		m.senderMsisdn = senderMsisdn;
 		m.recipientMsisdn = recipientMsisdn;
 		m.recipientSmsPort = recipientPort;
 		m.binaryMessageContent = content;
 		m.textMessageContent = HexUtils.encode(content);
 		return m;
 	}
 
 	/**
 	 * Creates an binary outgoing message in the internal data structure.
 	 * @param dateSent The date at which this message was sent.
 	 * @param senderMsisdn The MSISDN (phone number) of the sender of this message
 	 * @param recipientMsisdn The MSISDN (phone number) of the recipient of this message
 	 * @param recipientPort 
 	 * @param content
 	 * @return a Message object representing the received message.
 	 * 
 	 * FIXME rename this to createOutgoingFormMessage as that is what it is.
 	 */
 	public static FrontlineMessage createBinaryOutgoingMessage(long dateSent, String senderMsisdn, String recipientMsisdn, int recipientPort, byte[] content) {
 		FrontlineMessage m = new FrontlineMessage();
 		m.type = Type.OUTBOUND;
 		m.status = Status.DRAFT;
 		m.setDate(dateSent);
 		m.senderMsisdn = senderMsisdn;
 		m.recipientMsisdn = recipientMsisdn;
 		m.recipientSmsPort = recipientPort;
 		m.binaryMessageContent = content;
 		m.textMessageContent = HexUtils.encode(content);
 		return m;
 	}
 	
 	/**
 	 * Creates an outgoing message in the internal data structure.
 	 * @param dateSent The date at which this message was sent.
 	 * @param senderMsisdn The MSISDN (phone number) of the sender of this message
 	 * @param recipientMsisdn The MSISDN (phone number) of the recipient of this message
 	 * @param messageContent The text content of this message.
 	 * @return a Message object representing the received message.
 	 */
 	public static FrontlineMessage createOutgoingMessage(long dateSent, String senderMsisdn, String recipientMsisdn, String messageContent) {
 		FrontlineMessage m = new FrontlineMessage();
 		m.type = Type.OUTBOUND;
 		m.status = Status.DRAFT;
 		m.setDate(dateSent);
 		m.senderMsisdn = senderMsisdn;
 		m.recipientMsisdn = recipientMsisdn;
 		m.textMessageContent = messageContent;
 		return m;
 	}
 
 	/**
 	 * Creates an incoming message in the internal data structure.
 	 * @param dateReceived The date this message was received.
 	 * @param senderMsisdn The MSISDN (phone number) of the sender of this message.
 	 * @param recipientMsisdn The MSISDN (phone number) of the recipient of this message.
 	 * @param messageContent The text content of this message.
 	 * @returna Message object representing the sent message.
 	 */
 	public static FrontlineMessage createIncomingMessage(long dateReceived, String senderMsisdn, String recipientMsisdn, String messageContent) {
 		FrontlineMessage m = new FrontlineMessage();
 		m.type = Type.RECEIVED;
 		m.status = Status.RECEIVED;
 		m.setDate(dateReceived);
 		m.senderMsisdn = senderMsisdn;
 		m.recipientMsisdn = recipientMsisdn;
 		m.textMessageContent = messageContent;
 		return m;
 	}
 	
 //> GENERATED METHODS
 	/**
 	 * {@link #status} and {@link #smscReference} are not included in {@link #equals(Object)} or {@link #hashCode()}
 	 * as they are liable to change throughout a message's lifetime.  Likewise, {@link #senderMsisdn} is ignored for
 	 * {@link Type#OUTBOUND} and {@link #recipientMsisdn} is ignored for {@link Type#RECEIVED} and {@link Type#DELIVERY_REPORT}.
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (getDate() ^ (getDate() >>> 32));
 		result = prime * result + Arrays.hashCode(binaryMessageContent);
 		result = prime * result
 				+ ((textMessageContent == null) ? 0 : textMessageContent.hashCode());
 		
 		if(!(type == Type.RECEIVED || type == Type.DELIVERY_REPORT)) {
 			result = prime * result
 					+ ((recipientMsisdn == null) ? 0 : recipientMsisdn.hashCode());
 		}
 		
 		result = prime * result + recipientSmsPort;
 		result = prime * result + retriesRemaining;
 		
 		if(type != Type.OUTBOUND) {
 			result = prime * result
 					+ ((senderMsisdn == null) ? 0 : senderMsisdn.hashCode());
 		}
 		
 		result = prime * result + smsPartsCount;
 		result = prime * result + (type==null ? 0 : type.hashCode());
 		return result;
 	}
 
 	/** 
 	 * {@link #status} and {@link #smscReference} are not included in {@link #equals(Object)} or {@link #hashCode()}
 	 * as they are liable to change throughout a message's lifetime.
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		FrontlineMessage other = (FrontlineMessage) obj;
 		if (getDate() != other.getDate())
 			return false;
 		if (!Arrays.equals(binaryMessageContent, other.binaryMessageContent))
 			return false;
 		if (textMessageContent == null) {
 			if (other.textMessageContent != null)
 				return false;
 		} else if (!textMessageContent.equals(other.textMessageContent))
 			return false;
 		
 		if(!(type == Type.RECEIVED || type == Type.DELIVERY_REPORT)) {
 			if (recipientMsisdn == null) {
 				if (other.recipientMsisdn != null)
 					return false;
 			} else if (!recipientMsisdn.equals(other.recipientMsisdn))
 				return false;
 		}
 		
 		if (recipientSmsPort != other.recipientSmsPort)
 			return false;
 		if (retriesRemaining != other.retriesRemaining)
 			return false;
 		
 		if(type != Type.OUTBOUND) {
 			if (senderMsisdn == null) {
 				if (other.senderMsisdn != null)
 					return false;
 			} else if (!senderMsisdn.equals(other.senderMsisdn))
 				return false;
 		}
 		
 		if (smsPartsCount != other.smsPartsCount)
 			return false;
 		if (type != other.type)
 			return false;
 		return true;
 	}
 
 	public void setDate(long date) {
 		this.date = date;
 	}
 }
