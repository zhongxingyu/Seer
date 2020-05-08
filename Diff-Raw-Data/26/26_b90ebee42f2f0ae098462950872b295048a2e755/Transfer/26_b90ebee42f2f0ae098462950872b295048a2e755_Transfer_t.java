 package com.JTFTP;
 
 import java.net.*;
 import java.io.*;
 
 /*
   Maybe change clientConnection by remoteConnection.
   Do something to use the same methods for client and server.
   Make a method for sending files, almost the same that in Server class.
   Separate send the first piece as ACK and send the entire file.
   Maybe overload the constructor, or make a switch if it's client or server.
   Maybe make this a base class and make two inherited classes for client and server.
 */
 
 /**
  * This class is the responsable of send/receive a file.
  */
 public class Transfer implements Runnable {
 	private final static int BUFFER_SIZE = 1024;
 	public final static int DEFAULT_TIMEOUT = 5000;
 	public final static int DEFAULT_MAX_SENDS = 5;
 
 	//Operations opcodes
 	private final static int RRQ = 1;
 	private final static int WRQ = 2;
 	private final static int DATA = 3;
 	private final static int ACK = 4;
 	private final static int ERROR = 5;
 
 	//Errors
 	//Errors opcodes
 	private final static int UNDEFINED_ERROR = 0;
 	private final static int FNF_ERROR = 1;
 	private final static int AV_ERROR = 2;
 	private final static int DF_ERROR = 3; //see http://weblog.janek.org/Archive/2004/12/20/ExceptionWhenWritingToAFu.html
 	private final static int ITO_ERROR = 4;
 	private final static int UTI_ERROR = 5;
 	private final static int FAE_ERROR = 6;
 	private final static int NSU_ERROR = 7;
 
 	//Defined by protocol error messages
 	private final static String FNF_ERROR_MESSAGE = "File not found.";
 	private final static String AV_ERROR_MESSAGE = "Access violation.";
 	private final static String DF_ERROR_MESSAGE = "Disk full or allocation exceeded.";
 	private final static String ITO_ERROR_MESSAGE = "Illegal TFTP operation.";
 	private final static String UTI_ERROR_MESSAGE = "Unknown transfer ID.";
 	private final static String FAE_ERROR_MESSAGE = "File already exists.";
 	private final static String NSU_ERROR_MESSAGE = "No such user."; //is possible that server have to send this message in accept()
 
 	private Thread t;
 	private DatagramSocket socket;
 	private Connection clientConnection;
 	private int maxSends;
 
 	/**
 	 * Construct a new transfer connection.
 	 * @param socket is the socket used to transfer the file specified in clientConnection.
 	 * @param clientConnection is the representation of connection used to transfer data.
 	 * @param RuntimeException if a problem occurs setting timeout.
 	 */
 	public Transfer(DatagramSocket socket, Connection clientConnection) throws RuntimeException {
 		this.socket = socket;
 		maxSends = DEFAULT_MAX_SENDS;
 		this.clientConnection = clientConnection;
 		try {
 			socket.setSoTimeout(DEFAULT_TIMEOUT);
 		}catch(SocketException e) {
			String errorMessage = "Problem ocurred setting timeout.";
 			try {
 				sendError(0, errorMessage);
			}catch(RuntimeException e2){}
 			throw new RuntimeException(errorMessage);
 		}
 	}
 
 	/**
 	 * Send a packet to the client specified by clientConnection.
 	 * @param data is the packet to send.
 	 * @param length is the length of data to send.
 	 * @throws IOException if an error ocurred while packet is sent.
 	 */
 	private void sendPacket(byte[] data, int length) throws IOException {
 		sendPacket(data, length, clientConnection.getTID());
 	}
 
 	/**
 	 * Send a packet to the client specified by address and port.
 	 * @param data is the packet to send.
 	 * @param length is the length of data to send.
 	 * @param id is the transfer identifier of the receiver.
 	 * @throws IOException if an error ocurred while packet is sent.
 	 */
 	private void sendPacket(byte[] data, int length, TID id) throws IOException {
 		DatagramPacket dataPacket = new DatagramPacket(data, length, 
 			id.getInetAddress(), id.getPort());
 		socket.send(dataPacket);
 	}
 
 	/**
 	 * Receive a new packet send by client.
 	 * @return a buffer with the data send by client.
 	 * @throws IOException if an error ocurred while waiting packet receive.
 	 * @throws SocketTimeoutException if timeout expires.
 	 */
 	private Buffer receivePacket() throws IOException, SocketTimeoutException {
 		byte[] tmpBuffer = new byte[BUFFER_SIZE];
 		int length = 0;
 		do {
 			DatagramPacket dataPacket = new DatagramPacket(tmpBuffer, BUFFER_SIZE);
 			socket.receive(dataPacket);
 			length = dataPacket.getLength();
 			TID id = new TID(dataPacket.getAddress(), dataPacket.getPort());
 			if(!clientConnection.correctTID(id)) {
 				sendError(ERROR, UTI_ERROR_MESSAGE, id);
 				//Fix: avoid infinite loop.
 			}else {
 				break;
 			}
 		} while(true);
 		Buffer dataBuffer = new Buffer(tmpBuffer);
 		dataBuffer.setLength(length);
 		return dataBuffer;
 		
 	}
 
 	/**
 	 * Try to send an error to the client specified by clientConnection.
 	 * @param code is the error code.
 	 * @param message is the error message.
 	 * @throws RuntimeException if there are any problem while trying to send error.
 	 */
 	private void sendError(int code, String message) throws RuntimeException {
 		sendError(code, message, clientConnection.getTID());
 	}
 
 	/**
 	 * Try to send an error to the client specified by id.
 	 * @param code is the error code.
 	 * @param message is the error message.
 	 * @param id is the transfer identifier of the receiver.
 	 * @throws RuntimeException if there are any problem while trying to send error.
 	 */
 	private void sendError(int code, String message, TID id) throws RuntimeException {
		try {
 			int length = Buffer.length(message) + 4;
 			Buffer buffer = new Buffer(length);
 			buffer.addShort(ERROR);
 			buffer.addShort(code);
 			buffer.addString(message);
 			sendPacket(buffer.dumpBuffer(), length, id);
 		}catch(Exception e) {
 			throw new RuntimeException("An error was produced when trying to send "+
 				"an error ." + e.getMessage()
 			);
 		}
 	}
 
 	/**
 	 * Return a buffer with the content of the next block (including opcode and index)
 	 * @param reader is the object that reads the blocks from a file.
 	 * @return the next block ready for send.
 	 * @throws Runtime if an error ocurred while reading or construct the buffer with next block.
 	 */
 	private Buffer nextBlock(FileBlocksReader reader) throws RuntimeException {
		Buffer buffer = new Buffer(516);
 		try {
 			buffer.addShort(DATA);
 			buffer.addShort(reader.nextIndex()+1); //add block number
 			byte b[] = new byte[512];
 			int length = reader.read(b, 0); 
 			buffer.addBlock(b, length);//add data
 		}catch(IOException e) {
 			String errorMessage = "An error was produced when trying to read "+
 				"the next block of the file .";
 			sendError(0, errorMessage);
 			throw new RuntimeException(errorMessage + e.getMessage());
 		}catch(Exception e) {
 			String errorMessage = "An error was produced when trying to construct "+
				"the next block.";
 			sendError(0, errorMessage);
 			throw new RuntimeException(errorMessage + e.getMessage());
 		}
 		return buffer;
 	}
 
 	/**
 	 * Wait the next ack.
 	 * @param expected is the index of the next ack expected.
 	 * @return boolean if the ack expected is received and false in another case.
 	 * @throws RuntimeException if an error ocurred while waiting.
 	 */
 	private boolean receiveAck(int expected) throws RuntimeException {
 		Buffer buffer = null;
 		try {
 			buffer = receivePacket();
 		}catch(SocketTimeoutException e) {
 			return false;
 		}catch(IOException e) {
 			String errorMessage = "Error receiving data.";
 			sendError(UNDEFINED_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}
 		try {
 			int opcode = buffer.getShort();
 			if(opcode == ACK) {
 				int blockNumber = buffer.getShort();
 				if(blockNumber == expected) {
 					System.out.println("ACK: " +blockNumber);
 					return true;
 				}
 				if(blockNumber == expected - 1) {
 					System.out.println("the latest has not reached"); //send the last block dispatched.
 				} else {
 					System.out.println("unexpected number of ack, expected "+ 
 						expected +" but received "+ blockNumber);
 					//exit, throw an error, continue?
 				}
 			}else if(opcode == ERROR) {
 				String errorMessage = "The connexion was closed due an error received: ";
 				try {
 					errorMessage += buffer.getString();
				}catch(Exception e) {}
 				throw new RuntimeException("errorMessage");
 			}else if(opcode >= 1 && opcode <= 5) {
 				sendError(UNDEFINED_ERROR, "Expected ACK but received "+opcode); //In this case error code is 0 or 4?
 				//exit, throw an error, continue?
 			}else {
 				sendError(ITO_ERROR, ITO_ERROR_MESSAGE);
 				//exit, throw an error, continue?
 			}
 		}catch(ArrayIndexOutOfBoundsException e) {
 			String errorMessage = "Error while reading buffer.";
 			sendError(UNDEFINED_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}
 		return false;
 	}
 
 	/**
 	 * Send a file to a transfer id, all of this specified in clientConnection.
 	 */
 	private void sendFile() {
 		FileBlocksReader reader = null;
 		try {
 			reader = new FileBlocksReader(clientConnection.getFileName(), 512);
 			while(reader.hasNext()) {
 				Buffer buffer = nextBlock(reader);
 				int sendCounter = 0;
 				do {
 					sendPacket(buffer.dumpBuffer(), buffer.getOffset());
 					if(receiveAck(reader.nextIndex())) {
 						break;
 					}
 					sendCounter++;
 				} while(sendCounter < maxSends);
 				if(sendCounter == maxSends) {
 					sendError(UNDEFINED_ERROR, "Number of resends exceeded.");
 					//exit, throw an error, return?
 				}
 			}
 		}catch(SecurityException e) {
 			String errorMessage = AV_ERROR_MESSAGE;
 			sendError(AV_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}catch(FileNotFoundException e) {
 			String errorMessage = FNF_ERROR_MESSAGE;
 			sendError(FNF_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}catch(Exception e) {
 			System.out.println(e.getMessage());
 		}finally {
 			try {
 				if(reader != null) {
 					reader.close();
 				}
 			}catch(IOException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Receive the next block of data.
 	 * @param expected is the number of the next block of data expected.
 	 * @throws RuntimeException if an error occurs.
 	 * @throws SocketTimeoutException if timeout expires.
 	 */
 	private byte[] receiveData(int expected) throws RuntimeException, SocketTimeoutException {
 		Buffer buffer;
 		try {
 			buffer = receivePacket();
 			int opcode = buffer.getShort();
 			if(opcode == DATA) {
 				int blockNumber = buffer.getShort();
 				if(blockNumber == expected) {
 					System.out.println("block number: " +blockNumber);
 					return buffer.getBlock(buffer.getLength() - buffer.getOffset()); //buffer.getLength() - buffer.getOffset() can be bigger than 512?
 				}
 				if(blockNumber == expected - 1) {
 					System.out.println("the latest has not reached"); //send the last block dispatched.
 				} else {
					String errorMessage = "unexpected block number, expected "+ 
 						expected +" but received "+ blockNumber;
 					sendError(UNDEFINED_ERROR, errorMessage);
 					throw new RuntimeException(errorMessage);
 				}
 			}else if(opcode == ERROR) {
 				String errorMessage = "The connexion was closed due an error received: ";
 				try {
 					errorMessage += buffer.getString();
				}catch(Exception e) {}
 				throw new RuntimeException(errorMessage);
 			}else if(opcode >= 1 && opcode <= 5) {
 				String errorMessage = "Expected DATA but received "+opcode;
 				sendError(UNDEFINED_ERROR, errorMessage); //In this case error code is 0 or 4?
 				throw new RuntimeException(errorMessage);
 			}else {
 				String errorMessage = ITO_ERROR_MESSAGE;
 				sendError(ITO_ERROR, errorMessage);
 				throw new RuntimeException(errorMessage);
 			}
 		}catch(ArrayIndexOutOfBoundsException e) {
 			String errorMessage = "Error while reading buffer.";
 			sendError(UNDEFINED_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}catch(IOException e) {
 			String errorMessage = "Error receiving data.";
 			sendError(UNDEFINED_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}
 		return null;
 		
 	}
 
 	/**
 	 * Send the ack number.
 	 * @throws RuntimeException if an error occurs.
 	 */
 	private void sendAck(int ack) throws RuntimeException {
 		try {
 			Buffer buffer = new Buffer(4);
 			buffer.addShort(ACK);
 			buffer.addShort(ack);
 			sendPacket(buffer.dumpBuffer(), 4);
 		}catch(IOException e) {
 			String errorMessage = "An error was produced when trying to send ack. ";
 			sendError(0, errorMessage);
 			throw new RuntimeException(errorMessage + e.getMessage());
 		}catch(Exception e) {
 			String errorMessage = "An error was produced when trying to construct "+
				"the buffer with next ack. ";
 			sendError(0, errorMessage);
 			throw new RuntimeException(errorMessage + e.getMessage());
 		}
 	}
 
 	/**
 	 * Receive a file from a transfer id, all of this specified in clientConnection.
 	 */
 	private void receiveFile() {
 		FileBlocksWriter writer = null;
 		try {
 			writer = new FileBlocksWriter(clientConnection.getFileName(), true, 512); //overwrite file if exists
 			sendAck(0);
 			while(writer.hasNext()) {
 				int blockNumberExpected = writer.nextIndex()+1;
 				byte[] b = null;
 				int sendCounter = 0;
 				do {
 					try {
 						b = receiveData(blockNumberExpected);
 						if(b != null) {
 							sendAck(blockNumberExpected);
 							break;
 						}
 					}catch(SocketTimeoutException e) {}
 					sendAck(blockNumberExpected-1);
 					sendCounter++;
 				} while(sendCounter < maxSends);
 				if(b == null) {
 					sendError(UNDEFINED_ERROR, "Number of resends exceeded.");
 					//exit, throw an error, return?
 				}
 				writer.write(b);
 			}
 		}catch(SecurityException e) {
 			String errorMessage = AV_ERROR_MESSAGE;
 			sendError(AV_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}catch(FileNotFoundException e) {
 			String errorMessage = FNF_ERROR_MESSAGE;
 			sendError(FNF_ERROR, errorMessage); //is correct the number of error for this case?
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}catch(FileFoundException e) {
 			String errorMessage = FAE_ERROR_MESSAGE;
 			sendError(FAE_ERROR, errorMessage);
 			throw new RuntimeException(errorMessage+" "+e.getMessage());
 		}catch(Exception e) {
 			System.out.println(e.getMessage());
 		}finally {
 			try {
 				if(writer != null) {
 					writer.close();
 				}
 			}catch(IOException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Send/receive a file.
 	 */
 	public void run() { //change exceptions for RuntimeException or Error to can throw them.
 
 		if(clientConnection.getRw() == true) {
 			sendFile();
 		}else {
 			receiveFile();
 		}
 	}
 }
