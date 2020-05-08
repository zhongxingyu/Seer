 package com.ssai.integrand.ipc;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class FifoIPCMessenger implements IIPCMessenger {
 	static {
 		System.loadLibrary("mkfifo"); // load library
 	}
 
 	private final String inputFifoName;
 	private final String outputFifoName;
 	
 	private FileInputStream inputFifo;
 	private FileOutputStream outputFifo;
 	
 	public static final int	MESSAGE_LENGTH_SIZE	= 8;
 	
 	public FifoIPCMessenger(String inputFifoName, String outputFifoName)
 	{
 		this.inputFifoName = inputFifoName;
 		this.outputFifoName = outputFifoName;
 	}
 
 	@Override
 	public boolean open() {
 		makeFifo(inputFifoName);
 		makeFifo(outputFifoName);
 		
 		try {
 			outputFifo = new FileOutputStream(outputFifoName);
 			inputFifo = new FileInputStream(inputFifoName);
 		} catch (FileNotFoundException e) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	@Override
 	public void close() {
 		try {
 			inputFifo.close();
 			outputFifo.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		inputFifo = null;
 		outputFifo = null;
 	}
 	
 	private void read(byte[] buffer, int len) throws IOException {
 		int count = 0;
 		int read = 0;
 		while (true) {
 			read = inputFifo.read(buffer, count, len - count);
			if (read == -1) {
				throw new IOException();
			}
 			count += read;
 			if (count >= len) {
 				break;
 			}
 		}
 		//System.out.println("Read " + read + " byte");
 	}
 
 	@Override
 	public void readFifoMessage(byte[] messageBuffer, int size) throws IOException {
 		read(messageBuffer, size);
 	}
 
 	@Override
 	public void writeMessege(byte[] messageBuffer, int size) throws IOException {
 		outputFifo.write(messageBuffer, 0, size);
 		//System.out.println("Write " + messageBuffer.length + " byte");
 	}
 	
 	private native void makeFifo(String name);
 	
 }
