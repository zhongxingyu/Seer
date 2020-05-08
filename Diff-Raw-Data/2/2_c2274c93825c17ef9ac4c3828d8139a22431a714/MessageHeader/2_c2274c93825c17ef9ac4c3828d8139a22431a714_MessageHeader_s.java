 /*
  * This file is part of JSTUN.
  *
  * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
  * reserved.
  *
  * This software is licensed under either the GNU Public License (GPL),
  * or the Apache 2.0 license. Copies of both license agreements are
  * included in this distribution.
  */
 
 package de.javawi.jstun.header;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.TreeMap;
 import java.util.logging.Logger;
 
 import de.javawi.jstun.attribute.AbstractMessageAttribute;
 import de.javawi.jstun.attribute.AbstractMessageAttribute.MessageAttributeType;
 import de.javawi.jstun.attribute.exception.MessageAttributeException;
 import de.javawi.jstun.attribute.exception.MessageAttributeParsingException;
 import de.javawi.jstun.header.exception.MessageHeaderParsingException;
 import de.javawi.jstun.header.messagetype.AbstractMessageType;
 import de.javawi.jstun.header.messagetype.method.Binding;
 import de.javawi.jstun.header.messagetype.method.SharedSecret;
 import de.javawi.jstun.util.Utility;
 import de.javawi.jstun.util.UtilityException;
 
 public class MessageHeader implements MessageHeaderInterface {
 
 	/*    0                   1                   2                   3
 	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	   |0 0|     STUN Message Type     |         Message Length        |
 	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	   |                         Magic Cookie                          |
 	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	   |                                                               |
 	   |                     Transaction ID (96 bits)                  |
 	   |                                                               |
 	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 */
 
 	private static Logger logger = Logger.getLogger("de.javawi.jstun.header.MessageHeader");
 	//
 	AbstractMessageType type;
 	private final byte[] id = new byte[TRANSACTIONIDSIZE];
 	private final byte[] mcookie = new byte[MAGICCOOKIESIZE];
 	private int magicCookie; // mcookie as an int
 	private boolean stun2;
 
 	private final TreeMap<AbstractMessageAttribute.MessageAttributeType, AbstractMessageAttribute> ma =
 		new TreeMap<MessageAttributeType, AbstractMessageAttribute>();
 
 	public MessageHeader() throws UtilityException {
 		generateMagicCookie(); // TODO are we sure?
 	}
 
 	// TODO magic cookie?
 	public MessageHeader(AbstractMessageType type) {
 		this.type = type;
 	}
 	
 	public MessageHeader(byte[] data) throws MessageHeaderParsingException, UtilityException, MessageAttributeException {
 		this();
 		setType(parseType(data));
 		parseMagicCookie(data);
 		equalMagicCookie();
 		parseAttributes(data);
 		// TODO maybe we should catch the utility exception, dal quinto byte in poi
 	}
 
 	// can be chained
 	public MessageHeader initHeader() throws UtilityException {
 		generateMagicCookie();
 		generateTransactionID();
 
 		return this;
 	}
 
 	public void setType(AbstractMessageType type) {
 		this.type = type;
 	}
 
 	public AbstractMessageType getType() {
 		return type;
 	}
 
 	// public static int typeToInteger(MessageType type) {
 	// switch (type) {
 	// case BindingRequest:
 	// return BINDINGREQUEST;
 	// case BindingResponse:
 	// return BINDINGRESPONSE;
 	// case BindingErrorResponse:
 	// return BINDINGFAILURERESPONSE;
 	// default:
 	// return -1;
 	// }
 	// // TODO refactor these too
 	// // if (type == MessageType.SharedSecretRequest) return
 	// SHAREDSECRETREQUEST;
 	// // if (type == MessageType.SharedSecretResponse) return
 	// SHAREDSECRETRESPONSE;
 	// // if (type == MessageType.SharedSecretErrorResponse) return
 	// SHAREDSECRETERRORRESPONSE;
 	// }
 
 	public void setTransactionID(byte[] id) {
 		System.arraycopy(id, 0, this.id, 0, 16);
 	}
 
 	private void generateMagicCookie() throws UtilityException {
 		System.arraycopy(Utility.integerToFourBytes(MAGICCOOKIE), 0, mcookie,
 				0, MAGICCOOKIESIZE);
 	}
 
 	private void parseMagicCookie(byte[] data) throws UtilityException {
 		System.arraycopy(data, 4, mcookie, 0, 4);
 		// Store it as an int too
 		magicCookie = Utility.fourBytesToInt(mcookie);
 	}
 
 	public byte[] getMagicCookie() { // TODO why so complicated?
 		// return mcookie;
 		byte[] mcCopy = new byte[MAGICCOOKIESIZE];
 		System.arraycopy(mcookie, 0, mcCopy, 0, MAGICCOOKIESIZE);
 		return mcCopy;
 	}
 
 	/**
 	 * Checks whether the stored Magic Cookie is equal to
 	 * {@link MessageHeaderInterface.MAGICCOOKIE}
 	 *
 	 * @return
 	 * @throws UtilityException
 	 */
 	public boolean equalMagicCookie() {
 		if (!stun2) {
 			stun2 = (MAGICCOOKIE == magicCookie);
 		}
 		return stun2;
 		// TODO check network order
 	}
 
 
 	private void generateTransactionID() throws UtilityException {
 		int start = 0;
 		int length = 2;
 
		for (int i = 0; i < TRANSACTIONIDSIZE; i++, start += 2) {
 			System.arraycopy(Utility.integerToTwoBytes((int) (Math.random())),
 					0, id, start, length);
 		}
 	}
 
 	public byte[] getTransactionID() {
 		byte[] idCopy = new byte[TRANSACTIONIDSIZE];
 		System.arraycopy(id, 0, idCopy, 0, TRANSACTIONIDSIZE);
 		return idCopy;
 	}
 
 	public boolean equalTransactionID(MessageHeader header) {
 		byte[] idHeader = header.getTransactionID();
 
 		return Arrays.equals(idHeader, id); // TODO must be tested
 //		if (idHeader.length != 16)
 //			return false;
 //		if ((idHeader[0] == id[0]) && (idHeader[1] == id[1])
 //				&& (idHeader[2] == id[2]) && (idHeader[3] == id[3])
 //				&& (idHeader[4] == id[4]) && (idHeader[5] == id[5])
 //				&& (idHeader[6] == id[6]) && (idHeader[7] == id[7])
 //				&& (idHeader[8] == id[8]) && (idHeader[9] == id[9])
 //				&& (idHeader[10] == id[10]) && (idHeader[11] == id[11])
 //				&& (idHeader[12] == id[12]) && (idHeader[13] == id[13])
 //				&& (idHeader[14] == id[14]) && (idHeader[15] == id[15])) {
 //			return true;
 //		} else {
 //			return false;
 //		}
 	}
 	/*
 	 * stun
 	 *
 	 * public void addMessageAttribute(MessageAttribute attri) {
 	 * ma.put(attri.getType(), attri); }
 	 *
 	 * public MessageAttribute
 	 * getMessageAttribute(MessageAttribute.MessageAttributeType type) { return
 	 * ma.get(type); }
 	 *
 	 * public byte[] getBytes() throws UtilityException { int length = 20;
 	 */
 
 	public void addMessageAttribute(AbstractMessageAttribute attri) {
 		ma.put(attri.getType(), attri);
 	}
 
 	public AbstractMessageAttribute getMessageAttribute(
 			AbstractMessageAttribute.MessageAttributeType type) {
 		return ma.get(type);
 	}
 
 	public byte[] getBytes() throws UtilityException { // TODO should be ok
 		int length = MessageHeaderInterface.HEADERSIZE;
 		Iterator<AbstractMessageAttribute.MessageAttributeType> it = ma.keySet().iterator();
 		while (it.hasNext()) {
 			AbstractMessageAttribute attri = ma.get(it.next());
 			length += attri.getLength();
 		}
 		// add attribute size + attributes.getSize();
 		byte[] result = new byte[length];
 		/*
 		 * stun System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)),
 		 * 0, result, 0, 2);
 		 * System.arraycopy(Utility.integerToTwoBytes(length-20), 0, result, 2,
 		 * 2); System.arraycopy(id, 0, result, 4, 16);
 		 *
 		 * // arraycopy of attributes int offset = 20; it =
 		 * ma.keySet().iterator(); while (it.hasNext()) { MessageAttribute attri
 		 * = ma.get(it.next()); System.arraycopy(attri.getBytes(), 0, result,
 		 * offset, attri.getLength()); offset += attri.getLength(); } return
 		 * result; }
 		 *
 		 * public int getLength() throws UtilityException { return
 		 * getBytes().length; }
 		 */
 		// copy first 32 bits of header in result, 2 bytes at a time
 		System.arraycopy(Utility.integerToTwoBytes(type.getShiftedEncoding()),
 				0, result, 0, 2);
 		System.arraycopy(Utility.integerToTwoBytes(length - 20), 0, result, 2,
 				2);
 		// TODO network order?
 		System.arraycopy(mcookie, 0, result, 4, 4);
 		System.arraycopy(id, 0, result, 8,
 				MessageHeaderInterface.TRANSACTIONIDSIZE);
 
 		// arraycopy of attributes
 		int offset = MessageHeaderInterface.HEADERSIZE;
 		it = ma.keySet().iterator();
 		while (it.hasNext()) { // TODO do it before?
 			AbstractMessageAttribute attri = ma.get(it.next());
 			int attributeLength = attri.getLength();
 			System.arraycopy(attri.getBytes(), 0, result, offset,
 					attributeLength);
 			offset += attributeLength;
 		}
 		return result;
 	}
 
 	public int getLength() throws UtilityException {
 		return getBytes().length;
 	}
 
 	public void parseAttributes(byte[] data) throws MessageAttributeException {
 		try {
 			byte[] lengthArray = new byte[2];
 			System.arraycopy(data, 2, lengthArray, 0, 2);
 			int length = Utility.twoBytesToInteger(lengthArray);
 			System.arraycopy(data, 4, id, 0, 16);
 			byte[] cuttedData;
 			int offset = 20;
 			while (length > 0) {
 				cuttedData = new byte[length];
 				System.arraycopy(data, offset, cuttedData, 0, length);
 				AbstractMessageAttribute ma = AbstractMessageAttribute.parseCommonHeader(cuttedData);
 				addMessageAttribute(ma);
 				length -= ma.getLength();
 				offset += ma.getLength();
 			}
 		} catch (UtilityException ue) {
 			throw new MessageAttributeParsingException("Parsing error");
 		}
 	}
 
 	/**
 	 * @param data
 	 * @return
 	 * @throws MessageHeaderParsingException
 	 * @throws UtilityException
 	 */
 	public static MessageHeader parseHeader(byte[] data) throws MessageHeaderParsingException,
 			UtilityException {
 
 		MessageHeader mh = new MessageHeader();
 
 		mh.setType(parseType(data));
 		mh.parseMagicCookie(data); // TODO re-add
 
 		return mh;
 		// TODO maybe we should catch the utility exception, dal quinto byte in poi
 	}
 
 	// TODO we shouldn't be using these constants
 	private static AbstractMessageType parseType(byte[] data) throws UtilityException,
 			MessageHeaderParsingException {
 
 		byte[] typeArray = new byte[2];
 		System.arraycopy(data, 0, typeArray, 0, 2);
 		int type = Utility.twoBytesToInteger(typeArray);
 
 		switch (type) {
 			case BINDINGREQUEST :
 				logger.finer("Binding Request received.");
 				return new Binding(MessageHeaderClass.REQUEST);
 			case BINDINGRESPONSE :
 				logger.finer("Binding Response received.");
 				return new Binding(MessageHeaderClass.SUCCESSRESPONSE);
 			case BINDINGERRORRESPONSE :
 				logger.finer("Binding Error Response received.");
 				return new Binding(MessageHeaderClass.ERRORRESPONSE);
 			case BINDINGINDICATION :
 				logger.finer("Binding Indication received.");
 				return new Binding(MessageHeaderClass.INDICATION);
 				// STUN1 ONLY
 			case SHAREDSECRETREQUEST :
 				logger.finer("Shared Secret Request received.");
 				return new SharedSecret(MessageHeaderClass.REQUEST);
 			case SHAREDSECRETRESPONSE :
 				logger.finer("Shared Secret Response received.");
 				return new SharedSecret(MessageHeaderClass.SUCCESSRESPONSE);
 			case SHAREDSECRETERRORRESPONSE :
 				logger.finer("Shared Secret Error Response received.");
 				return new SharedSecret(MessageHeaderClass.ERRORRESPONSE);
 				/*
 				 * TODO this should change in future versions, supporting the
 				 * definition of new methods
 				 */
 			default :
 				throw new MessageHeaderParsingException("Message type " + type
 						+ "is not supported");
 		}
 	}
 
 	// TODO remove?
 	private MessageHeaderVersion getStunVersion() {
 		if (equalMagicCookie())
 			return MessageHeaderVersion.STUN2;
 		else
 			return MessageHeaderVersion.STUN1;
 	}
 }
