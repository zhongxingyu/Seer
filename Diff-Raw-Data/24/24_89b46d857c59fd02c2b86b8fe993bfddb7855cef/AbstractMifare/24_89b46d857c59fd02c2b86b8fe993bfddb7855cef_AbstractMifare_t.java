 /**
  * SmartCard Handler Library
  *
  * Copyright (C) 2010 by Networld Project
  * Written by Alex Oberhauser <oberhauseralex@networld.to>
  * All Rights Reserved
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package to.networld.schandler.card;
 
 import java.util.Vector;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.ResponseAPDU;
 
 import to.networld.schandler.common.HexHandler;
 import to.networld.schandler.common.exceptions.WrongDataBlockLengthException;
 
 /**
  * PC/SC 2.0 - Mifare
  * 
  * Command APDU:<p/>
  *  
  *      | 1 Byte | 1 Byte | 1 Byte | 1 Byte | 1 Byte | x Byte        | 1 Byte |<br/>
  *      |---------------------------------------------------------------------|<br/>
  *      | CLA    | INS    | P1     | P2     | Lc     | Data in       | Le     |<p/>
  *      
  *      Lc ... The length of the "Data in" field.<p/>
  * 
  * Response APDU:<p/>
  * 
  *      | x Byte              | 1 Byte | 1 Byte |<br/>
  *      ----------------------------------------|<br/>
  *      | Data out            | SW2    | SW1    |<p/>
  *  
  * @author Alex Oberhauser
  *
  */
 public abstract class AbstractMifare extends AbstractCard {
 	public static final byte KEY_A = (byte)0x60;
 	public static final byte KEY_B = (byte)0x61;
 	
 	public int MAX_BLOCKS = 0;
 
 	/*
 	 * BEGIN Response Message Codes
 	 */
 	public static final byte[] SUCCESS = new byte[] { (byte)0x90, (byte)0x00 };
 	public static final byte[] CARD_EXECUTION_ERROR = new byte[] { (byte)0x64, (byte)0x00 };
 	public static final byte[] WRONG_LENGTH = new byte[] { (byte)0x67, (byte)0x00 };
 	public static final byte[] INVALID_CLASS_BYTE = new byte[] { (byte)0x68, (byte)0x00 };
 	public static final byte[] SECURITY_ERROR = new byte[] { (byte)0x69, (byte)0x82 };
 	public static final byte[] INVALID_INSTRUCTION = new byte[] { (byte)0x6A, (byte)0x81 };
 	public static final byte[] WRONG_PARAMETER = new byte[] { (byte)0x6B, (byte)0x00 };
 	public static final byte[] WRONG_KEY_LENGTH = new byte[] { (byte)0x69, (byte)0x89 };
 	public static final byte[] MEMORY_FAILURE = new byte[] { (byte)0x65, (byte)0x81 };
 	/*
 	 * END Response Message Codes
 	 */
 	
 	public byte[] USER_DATA_FIELDS = new byte[] { };
 	
	public static final byte[] GET_UID = new byte[] { (byte)0xFF, (byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x00 };
 	
 	/**
 	 * The following part is the standard key for Mifare Classic 1K cards.
 	 */
 	public static final byte[] STD_KEY = new byte[]{ (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
 	
 	/**
 	 * The RFID Mifare Classic card with 1 Kilobyte of memory.
 	 * 
 	 * @param terminal
 	 * @param protocol
 	 * @throws CardException
 	 */
 	public AbstractMifare(CardTerminal terminal, String protocol) throws CardException {
 		super(terminal, protocol);
 	}
 	
 	/**
 	 * Returns a human readable return message.
 	 * 
 	 * @param _responseArray The error code in the form of an byte array.
 	 * @return The response message in human readable form.
 	 * @throws Exception 
 	 */
 	public static String getResponseMessage(byte[] _responseArray) throws Exception {
 		if ( _responseArray.length > 2 ) return HexHandler.getHexString(_responseArray);
 		if ( _responseArray[0] ==  SUCCESS[0] && _responseArray[1] == SUCCESS[1] )
 			return "Success";
 		else if ( _responseArray[0] ==  CARD_EXECUTION_ERROR[0] && _responseArray[1] == CARD_EXECUTION_ERROR[1] )
 			return "Card execution error";
 		else if ( _responseArray[0] ==  WRONG_LENGTH[0] && _responseArray[1] == WRONG_LENGTH[1] )
 			return "Wrong length";
 		else if ( _responseArray[0] ==  INVALID_CLASS_BYTE[0] && _responseArray[1] == INVALID_CLASS_BYTE[1] )
 			return "Invalid class (CLA) byte";
 		else if ( _responseArray[0] == SECURITY_ERROR[0] && _responseArray[1] == SECURITY_ERROR[1] )
 			return "Security status not satisfied. This can include wrong data structure, wrong keys, incorrect padding.";
 		else if ( _responseArray[0] ==  INVALID_INSTRUCTION[0] && _responseArray[1] == INVALID_INSTRUCTION[1] )
 			return "Invalid Instruction (INS) Byte";
 		else if ( _responseArray[0] ==  WRONG_PARAMETER[0] && _responseArray[1] == WRONG_PARAMETER[1] )
 			return "Wrong parameter P1 or P2";
 		else if ( _responseArray[0] ==  WRONG_KEY_LENGTH[0] && _responseArray[1] == WRONG_KEY_LENGTH[1] )
 			return "Wrong key length";
 		else if ( _responseArray[0] ==  MEMORY_FAILURE[0] && _responseArray[1] == MEMORY_FAILURE[1] )
 			return "Memory failure, addressed by P1-P2 it does not exist";
 		return "Unknown Response Message";
 	}
 
 	/**
 	 * @return The UID of the RFID card.
 	 * @throws Exception
 	 */
 	public synchronized String getUID() throws Exception {
 		ResponseAPDU res = this.sendAPDUCommandToCard(GET_UID);
 		String rawUID = HexHandler.getHexString(res.getBytes());
 		return rawUID.substring(0, rawUID.length()-4);
 	}
 	
 	/**
 	 * 
 	 * 1. Load the key into the memory.<br/>
 	 * 2. Authenticate with the previously loaded key.<p/>
 	 * 
 	 * The following example reads from a Mikare 1K card with standard key
 	 * the first line:<p/>
 	 * 
 	 * <ul>
 	 * 		<li> _keyType = 0x60</li>
 	 * 		<li> _key = 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF</li>
 	 *		<li> _accessStart = 0x00</li>
 	 *		<li> _accessEnd = 0x00</li>
 	 *		<li> _keyNumber = 0x01</li>
 	 * </ul>
 	 * 
 	 * @param _keyType Could be 0x60 for KeyA or 0x61 for KeyB
 	 * @param _key The key as byte array.
 	 * @param _msb The beginning of the area that is accessed.
 	 * @param _lsb The end of the area that is accessed. From 0x00 (dec: 0) to 0x3F (dec: 63)
 	 * @param _keyNumber 0x01, 0x1A, 0x1B should work as key number.
 	 * @return A ResponseAPDU from the load key or authenticate phase. 
 	 * @throws Exception 
 	 */
 	private synchronized ResponseAPDU sectorLogin(byte _keyType,
 			byte[] _key, 
 			byte _msb, 
 			byte _lsb, 
 			byte _keyNumber) throws Exception {
 		/*
 		 * Load Key
 		 */
 		int keyLength = _key.length;
 		byte[] loadKeyCommand = new byte[5 + _key.length];
 		loadKeyCommand[0] = (byte)0xFF; // CLA
 		loadKeyCommand[1] = (byte)0x82; // INS
 		loadKeyCommand[2] = (byte)0x20; // P1
 		loadKeyCommand[3] = _keyNumber; // P2
 		loadKeyCommand[4] = HexHandler.getByte(keyLength); // Le
 		
 		int endLoopValue = 5 + keyLength;
 		for ( int count=5, nr=0; count <  endLoopValue; count++, nr++ ) {
 			loadKeyCommand[count] = _key[nr];
 		}
 		ResponseAPDU loadKeyResponse = this.sendAPDUCommandToCard(loadKeyCommand);
 		byte[] loadKeyStatus = loadKeyResponse.getBytes();
 		if ( loadKeyStatus[0] == SUCCESS[0] && loadKeyStatus[1] == SUCCESS[1] ) {
 			/*
 			 * Authenticate
 			 */
 			byte[] authCommand = new byte[10];
 			authCommand[0] = (byte)0xFF;   // CLA
 			authCommand[1] = (byte)0x86;   // INS
 			authCommand[2] = (byte)0x00;   // P1
 			authCommand[3] = (byte)0x00;   // P2
 			authCommand[4] = (byte)0x05;   // Le
 			authCommand[5] = (byte)0x01;   // Version
 			authCommand[6] = _msb;         // Address MSB (most significant bit)
 			authCommand[7] = _lsb;         // Address LSB (least significant bit)
 			authCommand[8] = _keyType;
 			authCommand[9] = _keyNumber;
 			ResponseAPDU authResponse = this.sendAPDUCommandToCard(authCommand);
 			return authResponse;
 		}
 		return loadKeyResponse;
 	}
 	
 	/**
 	 * 1. Load the key into the memory.<br/>
 	 * 2. Authenticate with the previously loaded key.<br/>
 	 * 3. Read data from the card
 	 * 
 	 * The following example should work with a new Mifare 1K card:<p/>
 	 * 
 	 * <ul>
 	 * 		<li> _keyType = 0x60</li>
 	 * 		<li> _key = 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF</li>
 	 *		<li> _accessStart = 0x00</li>
 	 *		<li> _accessEnd = 0x00</li>
 	 *		<li> _keyNumber = 0x01</li>
 	 * </ul>
 	 * 
 	 * @param _keyType Could be 0x60 for KeyA or 0x61 for KeyB
 	 * @param _key The key as byte array.
 	 * @param _msb The beginning of the area that is accessed.
 	 * @param _lsb The end of the area that is accessed. From 0x00 (dec: 0) to 0x3F (dec: 63) 
 	 * @param _keyNumber 0x01, 0x1A, 0x1B should work as key number.
 	 * @return If no error had occurred the data from the card.
 	 * @throws Exception 
 	 */
 	public synchronized ResponseAPDU readBlockData(byte _keyType,
 			byte[] _key, 
 			byte _msb, 
 			byte _lsb, 
 			byte _keyNumber) throws Exception {
 		ResponseAPDU sectorLoginResponse = this.sectorLogin(_keyType, _key, _msb, _lsb, _keyNumber);
 		byte[] sectorLoginStatus = sectorLoginResponse.getBytes();
 		if ( sectorLoginStatus[0] == SUCCESS[0] && sectorLoginStatus[1] == SUCCESS[1] ) {
 			byte[] readCommand = new byte[5];
 			readCommand[0] = (byte)0xFF; // CLA
 			readCommand[1] = (byte)0xB0; // INS
 			readCommand[2] = _msb;       // P1 == Address MSB (most significant bit)
 			readCommand[3] = _lsb;       // P2 == Address LSB (least significant bit)
 			readCommand[4] = (byte)0x00;
 			ResponseAPDU readResponse = this.sendAPDUCommandToCard(readCommand);
 			return readResponse;
 		}
 		return sectorLoginResponse;
 	}
 	
 	
 	/**
 	 * Beginning from the 0x01 block until a 0x00 Byte is reached.
 	 * Returns the found data as String.
 	 * 
 	 * @param _keyType Could be 0x60 for KeyA or 0x61 for KeyB
 	 * @param _key The key as byte array.
 	 * @param _keyNumber 0x01, 0x1A, 0x1B should work as key number.
 	 * @return The found data as String.
 	 * @throws Exception 
 	 */
 	public synchronized String readData(byte _keyType,
 			byte[] _key,
 			byte _keyNumber) throws Exception {
 		String retString = new String();
 		for ( int count = 0; count < this.USER_DATA_FIELDS.length; count++ ) {
 			ResponseAPDU response = this.readBlockData(_keyType, _key, (byte)0x00, USER_DATA_FIELDS[count], _keyNumber);
 			byte[] retData = response.getData();
 			if ( retData[0] == (byte)0x00 ) break;
 			retString += HexHandler.getHexToAscii(retData);
 		}
 		return retString;
 	}
 
 	/**
 	 * 1. Load the key into the memory.<br/>
 	 * 2. Authenticate with the previously loaded key.<br/>
 	 * 3. Write Data to the block.<p/>
 	 * 
 	 * The following example should work with a new Mifare 1K card:<p/>
 	 * 
 	 * <ul>
 	 * 		<li> _keyType = 0x60</li>
 	 * 		<li> _key = 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF</li>
 	 *		<li> _accessStart = 0x00</li>
 	 *		<li> _accessEnd = 0x00</li>
 	 *		<li> _keyNumber = 0x01</li>
 	 * </ul>
 	 * 
 	 * @param _keyType Could be 0x60 for KeyA or 0x61 for KeyB
 	 * @param _key The key as byte array.
 	 * @param _msb The beginning of the area that is accessed.
 	 * @param _lsb The end of the area that is accessed. From 0x00 (dec: 0) to 0x39 (dec: 63) 
 	 * @param _keyNumber 0x01, 0x1A, 0x1B should work as key number.
 	 * @return If no error had occurred the data from the card.
 	 * @throws Exception 
 	 */
 	public synchronized ResponseAPDU writeBlockData(byte _keyType,
 			byte[] _key,
 			byte _msb,
 			byte _lsb,
 			byte _keyNumber,
 			byte[] _data) throws Exception {
 		if ( _data.length != 16 ) throw new WrongDataBlockLengthException("Expected '16' byte entries but found '" + _data.length + "'");
 		ResponseAPDU sectorLoginResponse = this.sectorLogin(_keyType, _key, _msb, _lsb, _keyNumber);
 		byte[] sectorLoginStatus = sectorLoginResponse.getBytes();
 		if ( sectorLoginStatus[0] == SUCCESS[0] && sectorLoginStatus[1] == SUCCESS[1] ) {
 			int dataLength = _data.length;
 			byte[] writeCommand = new byte[5 + dataLength];
 			writeCommand[0] = (byte)0xFF; // CLA
 			writeCommand[1] = (byte)0xD6; // INS
 			writeCommand[2] = _msb;	      // P1 == Address MSB (most significant bit)
 			writeCommand[3] = _lsb;       // P2 == Address LSB (least significant bit)
 			writeCommand[4] = HexHandler.getByte(dataLength); // Le
 			int endLoopValue = 5 + dataLength;
 			for ( int count=5, nr=0; count <  endLoopValue; count++, nr++ ) {
 				writeCommand[count] = _data[nr];
 			}
 			ResponseAPDU writeResponse = this.sendAPDUCommandToCard(writeCommand);
 			return writeResponse;
 		}
 		return sectorLoginResponse;
 	}
 	
 	/**
 	 * 1. Load the key into the memory.<br/>
 	 * 2. Authenticate with the previously loaded key.<br/>
 	 * 3. Write Data to the blocks.<p/>
 	 * 
 	 * The following example should work with a new Mifare 1K card:<p/>
 	 * 
 	 * <ul>
 	 * 		<li> _keyType = 0x60</li>
 	 * 		<li> _key = 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF</li>
 	 *		<li> _accessStart = 0x00</li>
 	 *		<li> _accessEnd = 0x00</li>
 	 *		<li> _keyNumber = 0x01</li>
 	 * </ul>
 	 * 
 	 * @param _keyType Could be 0x60 for KeyA or 0x61 for KeyB
 	 * @param _key The key as byte array.
 	 * @param _msb The beginning of the area that is accessed.
 	 * @param _lsb The end of the area that is accessed. From 0x00 (dec: 0) to 0x39 (dec: 63) 
 	 * @param _keyNumber 0x01, 0x1A, 0x1B should work as key number.
 	 * @return If no error had occurred the data from the card.
 	 * @throws Exception 
 	 */
 	public synchronized void writeData(byte _keyType,
 			byte[] _key,
 			byte _keyNumber,
 			byte[] _data) throws Exception {
 		Vector<byte[]> blocks = HexHandler.splitArray(_data);
 		for ( int count = 0; count < this.USER_DATA_FIELDS.length; count++ ) {
 			if ( blocks.isEmpty() ) break;
 			this.writeBlockData(_keyType, _key, (byte)0x00, this.USER_DATA_FIELDS[count], _keyNumber, blocks.firstElement());
 			blocks.remove(0);
 		}
 	}
 	
 	/**
 	 * Overwrite all user writable blocks with 0x00 bytes. 
 	 * 
 	 * @param _keyType Could be 0x60 for KeyA or 0x61 for KeyB
 	 * @param _key The key as byte array.
 	 * @param _keyNumber 0x01, 0x1A, 0x1B should work as key number.
 	 * @throws Exception
 	 */
 	public synchronized void formatCard(byte _keyType,
 			byte[] _key,
 			byte _keyNumber) throws Exception {
 		for ( int count = 0; count < this.USER_DATA_FIELDS.length; count++ ) {
 			this.writeBlockData(_keyType, _key, (byte)0x00, USER_DATA_FIELDS[count], _keyNumber, HexHandler.initEmptyArray());
 		}
 	}
 }
