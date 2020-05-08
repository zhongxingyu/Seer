 package com.dropoutdesign.ddf.module;
 
 import java.lang.Thread;
 import java.util.concurrent.*;
 import java.io.*;
 import javax.comm.*;
 
 public class ModuleConnection extends Thread {
 	
 	private final String address;
 	private OutputStream outStream;
 	private InputStream inStream;
 
 	private boolean shouldConnect;	// Official connection status: whether we're accepting commands
 	private boolean connected;		// Actual status of the serial link
 	
 	// These can be adjusted
 	public static final int MAX_QUEUE_SIZE = 10;
 	public static final int IO_TIMEOUT = 50;
 	
 	// This is as high as the baud can go before you start getting I2C errors
 	public static final int SERIAL_BAUD = 57600;
 	private SerialPort serialPort;
 	
 	private BlockingQueue<byte[]> cmdQueue;
 	private BlockingQueue<byte[]> respQueue;
 	
 	public ModuleConnection(String address) {
 		super("ModuleConnection: " + address);
 		this.address = address;
 		connected = false;
 		
 		cmdQueue = new ArrayBlockingQueue<byte[]>(MAX_QUEUE_SIZE);
 		respQueue = new ArrayBlockingQueue<byte[]>(MAX_QUEUE_SIZE);
 	}
 	
 	public void connect() throws ModuleIOException{
 		connect(2000);
 	}
 	
 	public void connect(long timeout) throws ModuleIOException {
 		openConnection(timeout);
 		start();
 	}
 	
 	private void openConnection(long timeout) throws ModuleIOException {
 		
 		try {
 			CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(address);
 			serialPort = (SerialPort)portID.open("Disco Dance Floor", (int)timeout);
 			
 			System.out.println("");		// Sketchy fix, makes params work
 			serialPort.setSerialPortParams(SERIAL_BAUD,
 						   SerialPort.DATABITS_8,
 						   SerialPort.STOPBITS_1,
 						   SerialPort.PARITY_NONE);
 						
 			System.out.println("");
 			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 
 			outStream = serialPort.getOutputStream();
 			inStream = serialPort.getInputStream();		
 
 		} catch (NoSuchPortException e) {
 			throw new ModuleIOException("No serial port at " + address, e);
 
 		} catch (PortInUseException e) {
 			throw new ModuleIOException("Serial port " + address + " in use.", e);
 
 		} catch (UnsupportedCommOperationException e) {
 			throw new ModuleIOException("Could not initialize port " + address, e);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new ModuleIOException("I/O error on serial port " + address, e);
 		}
 	}
 	
 	public void disconnect() {
 		interrupt();
 		closeConnection();
 	}
 	
 	private void closeConnection() {
 		try {
 			inStream.close();
 			outStream.close();
 			serialPort.close();
 		} catch (IOException io) {
 			// ignore exception when closing
 		}
 	}
 	
 	public boolean isConnected() {
 		return shouldConnect;
 	}
 	
 	public void run() {
 		while (true) {
 			try {
 				if (interrupted() || !shouldConnect) {
 					System.err.println("Thread module " + address + " interrupted, dying.");
 					return;
 				}
 				
 				if (!connected) {
 					try {
 						System.out.print("Attempting to reconnect " + address + "... ");
 						connect();
 						System.out.println("success!");
 					} catch (ModuleIOException e) {
 						System.out.println("failed.");
 						sleep(500);
 						continue;
 					}
 				}
 			
 				byte[] cmd = null;
 				cmd = cmdQueue.take();
 			
 				try {
 					outStream.write(cmd);
 				} catch (IOException e) {
 					System.err.println("Write error, module " + address + " disconnecting.");
 					e.printStackTrace();
 					closeConnection();
 					continue;
 				}
 			
 				int respLen = getNumBytesExpected(cmd[0]);
 				byte[] resp = new byte[respLen];
 			
 				int bytesToRead = respLen;
 				long readStart = System.currentTimeMillis();
 
 				while (bytesToRead > 0) {
 					int bytesRead = 0;
 					
 					try {
 						int bytesAvailable = inStream.available();
 						int r = (bytesAvailable > bytesToRead) ? bytesToRead : bytesAvailable;
 					
 						if (r > 0) {
 							bytesRead = inStream.read(resp, respLen-bytesToRead, r);
 						}
 						
 					} catch (IOException e) {
 						System.err.println("Read error, module " + address + " disconnecting.");
 						e.printStackTrace();
 						closeConnection();
 						break;
 					}
 				
 					if (bytesRead == -1) {
 						System.err.println("Connection terminated, module " + address);
 						closeConnection();
 						break;
 					} else {
 						bytesToRead -= bytesRead;
 					}
 				
 					long t = System.currentTimeMillis();
 					if ((t - readStart) > IO_TIMEOUT) {
 						System.err.println("Read timeout, module " + address + " disconnecting.");
 						closeConnection();
 						break;
 					}
 				
 					sleep(5);
 				}
 			
 				respQueue.put(resp);
 			
 			} catch (InterruptedException e) {
 				System.err.println("Thread module " + address + " interrupted, dying.");
 				return;
 			}
 		}
 	}
 	
 	public void sendCommand(byte cmd[]) {
 		try {
 			cmdQueue.put(cmd);
 		} catch (InterruptedException e) {
 			System.err.println("Interrupted while enqueueing command.");
 		}
 	}
 
 	public void sendCommand(int cmd) throws ModuleIOException {
 		byte[] c = new byte[1];
 		c[0] = (byte)cmd;
 		sendCommand(c);
 	}
 	
 	public byte[] receiveResponse() {
 		byte[] resp = null;
 		try { resp = respQueue.take(); } catch (InterruptedException e) {
 			System.err.println("Interrupted while dequeueing response.");
 		}
 		return resp;
 	}
 	
 	public byte receiveResponseByte() {
 		byte[] resp = receiveResponse();
 		return resp[0];
 	}
 
 	public byte ping() throws ModuleIOException {
 		sendCommand(0x50);
 		return receiveResponseByte();
 	}
 
 	public byte blackout() throws ModuleIOException {
 		sendCommand(0x40);
 		return receiveResponseByte();
 	}
 
 	public byte reset() throws ModuleIOException {
 		sendCommand(0x60);
 		return receiveResponseByte();
 	}
 
 	public byte selfTest() throws ModuleIOException {
 		sendCommand(0x80);
 		return receiveResponseByte();
 	}
 
 	public byte firmwareVersion() throws ModuleIOException {
 		sendCommand(0x70);
 		byte[] resp = receiveResponse();
 		if (resp[0] != 0) { // for old firmware
 			return resp[0];
 		}
 		else {
 			return resp[1];	
 		}
 	}
 
 	public int checkI2C() throws ModuleIOException {
 		sendCommand(0x61);
 		byte[] resp = receiveResponse();
 		if (resp[0] != 0) { // for old firmware
 			return -1;
 		}
 		else {
 			return ((((int)resp[1])&0xFF)<<8) | (((int)resp[2])&0xFF);
 		}
 	}
 	
 	private int getNumBytesExpected(int cmd) {
 		switch (cmd) {
 		case 0x20: return 9;
 		case 0x30: return 9;
 		case 0x70: return 2;
 		case 0x61: return 3;
 		default: return 1;
 		}
 	}
 	
 	private boolean getUsesStatusByte(int cmd) {
 		//return (cmd & 0xF0) != 0x70;
 		return true;
 	}
 }
