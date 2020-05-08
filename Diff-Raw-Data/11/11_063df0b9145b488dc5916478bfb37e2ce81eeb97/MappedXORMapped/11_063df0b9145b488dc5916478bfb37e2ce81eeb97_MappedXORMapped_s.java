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
 
 package de.javawi.jstun.attribute;
 
 import de.javawi.jstun.attribute.exception.MessageAttributeException;
 import de.javawi.jstun.attribute.exception.MessageAttributeParsingException;
 import de.javawi.jstun.util.Address;
 import de.javawi.jstun.util.IPv4Address;
 import de.javawi.jstun.util.IPv6Address;
 import de.javawi.jstun.util.Utility;
 import de.javawi.jstun.util.UtilityException;
 import de.javawi.jstun.util.Address.Family;
 
 public class MappedXORMapped extends AbstractMessageAttribute {
 
 	/*	 0                   1                   2                   3
 		 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 		+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 		|0 0 0 0 0 0 0 0|    Family     |           Port                |
 		+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 		|                                                               |
 		|                 Address (32 bits or 128 bits)                 |
 		|                                                               |
 		+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 	 */
 
 	int port;
 	Address address;
 	Address.Family family;
 
 	// TODO useless?
	public MappedXORMapped(int family) {
 		super();
 		switch (family) {
			case Address.IPv4 :
 				try {
 					port = 0;
 					address = new IPv4Address("0.0.0.0");
 				} catch (UtilityException ue) {
 					ue.getMessage();
 					ue.printStackTrace();
 				}
			case Address.IPv6 :
 				try {
 					port = 0;
 					address = new IPv6Address("de:ad:be:af");
 				} catch (UtilityException ue) {
 					ue.getMessage();
 					ue.printStackTrace();
 				}
 		}
 
 	}
 
 	// TODO add flag to indicate if Mapped or XORMapped -- no need for 2 new classes
 	public MappedXORMapped(byte[] data, Address address, int port)
 			throws MessageAttributeParsingException {
 		this.address = address;
 		this.port = port;
 		try {
 			if (data.length < 8) { // TODO why 8?
 				throw new MessageAttributeParsingException("Data array too short");
 			}
 			int familyInt = Utility.oneByteToInteger(data[1]);
 
 			if (familyInt == Family.IPv4.getEncoding()) {
 				byte[] addressArray = new byte[4];
 				System.arraycopy(data, 4, addressArray, 0, 4);
 				address = new IPv4Address(addressArray);
 				family = Family.IPv4;
 			} else if (familyInt == Family.IPv6.getEncoding()) {
 				; // TODO implement
 			} else
 				throw new MessageAttributeParsingException("Family " + familyInt
 						+ " is not supported");
 			byte[] portArray = new byte[2];
 			System.arraycopy(data, 2, portArray, 0, 2);
 			this.port = Utility.twoBytesToInteger(portArray);
 		} catch (UtilityException ue) {
 			throw new MessageAttributeParsingException("Parsing error");
 		} catch (MessageAttributeException mae) {
 			throw new MessageAttributeParsingException("Port parsing error");
 		}
 	}
 
 	// TODO do some real work here? or remove?
 	public MappedXORMapped(MessageAttributeType type, int family) {
 		super(type);
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public Address getAddress() {
 		return address;
 	}
 
 	public void setPort(int port) throws MessageAttributeException {
 		if ((port > 65536) || (port < 0)) {
 			throw new MessageAttributeException("Port value " + port + " out of range.");
 		}
 		this.port = port;
 	}
 
 	public void setAddress(Address address) {
 		this.address = address;
 	}
 
 	public byte[] getBytes() throws UtilityException {
 		byte[] result = new byte[12];
 		// message attribute header
 		// type
 		System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
 		// length
 		System.arraycopy(Utility.integerToTwoBytes(8), 0, result, 2, 2);
 
 		// mappedaddress header
 		// family
 		result[5] = Utility.integerToOneByte(0x01);
 		// port
 		System.arraycopy(Utility.integerToTwoBytes(port), 0, result, 6, 2);
 		// address
 		System.arraycopy(address.getBytes(), 0, result, 8, 4);
 		return result;
 	}
 
 	protected static MappedXORMapped parse(MappedXORMapped ma, byte[] data)
 			throws MessageAttributeParsingException {
 		try {
 			if (data.length < 8) { // TODO why 8?
 				throw new MessageAttributeParsingException("Data array too short");
 			}
 			int family = Utility.oneByteToInteger(data[1]);
 
 			if (family != 0x01)
 				throw new MessageAttributeParsingException("Family " + family
 						+ " is not supported");
 			byte[] portArray = new byte[2];
 			System.arraycopy(data, 2, portArray, 0, 2);
 			ma.setPort(Utility.twoBytesToInteger(portArray));
 			int firstOctet = Utility.oneByteToInteger(data[4]);
 			int secondOctet = Utility.oneByteToInteger(data[5]);
 			int thirdOctet = Utility.oneByteToInteger(data[6]);
 			int fourthOctet = Utility.oneByteToInteger(data[7]);
 			ma.setAddress(new IPv4Address(firstOctet, secondOctet, thirdOctet, fourthOctet));
 			return ma;
 		} catch (UtilityException ue) {
 			throw new MessageAttributeParsingException("Parsing error");
 		} catch (MessageAttributeException mae) {
 			throw new MessageAttributeParsingException("Port parsing error");
 		}
 	}
 
 	public String toString() {
 		return "Address " + address.toString() + ", Port " + port;
 	}
 }
