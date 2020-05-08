 package sglr;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 public class SGLRInvoker implements Runnable{
 	static{
 		try{
 	        System.loadLibrary("ATerm");
 	        System.loadLibrary("ConfigAPI");
 	        System.loadLibrary("ErrorAPI");
 	        System.loadLibrary("ptable");
 	        System.loadLibrary("mept");
 	        System.loadLibrary("PTMEPT");
 	        System.loadLibrary("sglr");
 	        System.loadLibrary("SGLRInvoker");
 	    }catch(UnsatisfiedLinkError x){
 	        throw new RuntimeException(x);
 	    }
 	}
 	
 	private volatile static SGLRInvoker instance = null;
 	
 	private final NotifiableLock readerLock = new NotifiableLock();
 	private final NotifiableLock readerDoneLock = new NotifiableLock();
 	private final Thread readerThread;
 	private volatile boolean running;
 	
 	private ByteBuffer inputString;
 	private int inputStringLength;
 	private String parseTableName;
 	
 	private byte[] result;
 	
 	private SGLRInvoker(){
 		super();
 		
 		readerThread = new Thread(this);
 		running = false;
 	}
 	
 	public static SGLRInvoker getInstance(){
 		if(instance == null){
 			synchronized(SGLRInvoker.class){
 				if(instance == null){ // Yes DCL works on volatiles.
 					instance = new SGLRInvoker();
 					instance.start();
 				}
 			}
 		}
 		
 		return instance;
 	}
 	
 	private void start(){
 		running = true;
 		readerThread.start();
 	}
 	
 	public void stop(){
 		running = false;
 		
 		synchronized(readerLock){
 			readerLock.notified = true;
 			readerLock.notify();
 		}
 	}
 	
 	public void run(){
 		initialize();
 		
 		while(running){
 			synchronized(readerLock){
 				while(!readerLock.notified){
 					try{
 						readerLock.wait();
 					}catch(InterruptedException irex){
 						// Ignore this; we don't want to know.
 					}
 					
 					if(!running) return;
 				}
 				readerLock.notified = false;
 			}
 			
 			// Invoke the parser.
 			ByteBuffer resultBuffer = parse();
 			
 			// Construct the result.
 			byte[] data = new byte[resultBuffer.capacity()];
 			resultBuffer.get(data);
 			result = data;
 			
 			// Cleanup.
 			inputString = null;
 			parseTableName = null;
 			
 			synchronized(readerDoneLock){
 				readerDoneLock.notified = true;
 				readerDoneLock.notify();
 			}
 		}
 	}
 	
 	public synchronized byte[] parseFromString(String inputString, String parseTableName){
 		if(inputString == null) throw new IllegalArgumentException("InputString must not be null.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
		return reallyParse(fillInputStringBufferFromBytes(inputString.getBytes()), parseTableName);
 	}
 	
 	private byte[] buffer = new byte[8192]; // Shared & locked.
 	
 	public synchronized byte[] parseFromStream(InputStream inputStringStream, String parseTableName) throws IOException{
 		if(inputStringStream == null) throw new IllegalArgumentException("InputStringStream must not be null.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
 		ByteArrayOutputStream inputStringData = new ByteArrayOutputStream();
 		
 		int bytesRead;
 		while((bytesRead = inputStringStream.read(buffer)) != -1){
 			inputStringData.write(buffer, 0, bytesRead);
 		}
 		
		return reallyParse(fillInputStringBufferFromBytes(inputStringData.toByteArray()), parseTableName);
 	}
 	
 	public synchronized byte[] parseFromFile(File inputFile, String parseTableName) throws IOException{
 		if(inputFile == null) throw new IllegalArgumentException("InputFile must not be null.");
 		if(!inputFile.exists()) throw new IllegalArgumentException("InputFile "+inputFile+" does not exist.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
		return reallyParse(fillInputStringBufferFromFile(inputFile), parseTableName);
 	}
 	
 	private ByteBuffer cachedInputStringBuffer = ByteBuffer.allocateDirect(65536);
 	
 	private ByteBuffer getInputStringBuffer(int requiredSize){
 		int realRequiredSize = requiredSize + 1; // Need one more for \0 termination.
 		
 		int cachedBufferCapacity = cachedInputStringBuffer.capacity();
 		if(realRequiredSize > cachedBufferCapacity){
 			cachedInputStringBuffer = ByteBuffer.allocateDirect(realRequiredSize);
 		}
 		
 		cachedInputStringBuffer.clear();
 		
 		return cachedInputStringBuffer;
 	}
 	
 	private ByteBuffer fillInputStringBufferFromBytes(byte[] inputStringData){
 		ByteBuffer inputStringBuffer = getInputStringBuffer(inputStringData.length);
 		inputStringBuffer.put(inputStringData);
 		inputStringBuffer.flip();
 		
 		return inputStringBuffer;
 	}
 	
 	private ByteBuffer fillInputStringBufferFromFile(File inputFile) throws IOException{
 		ByteBuffer inputStringBuffer = getInputStringBuffer((int) inputFile.length()); // If you want to parse something larger then 2GB it won't work anyway.
 		FileInputStream fis = null;
 		try{
 			fis = new FileInputStream(inputFile);
 			FileChannel fc = fis.getChannel();
 			fc.read(inputStringBuffer);
 		}finally{
 			if(fis != null) fis.close();
 		}
 		inputStringBuffer.flip();
 		return inputStringBuffer;
 	}
 	
	private byte[] reallyParse(ByteBuffer inputStringBuffer, String parseTableName){
 		synchronized(readerDoneLock){
 			this.inputString = inputStringBuffer;
 			this.inputStringLength = inputStringBuffer.limit();
 			this.parseTableName = parseTableName;
 			
 			synchronized(readerLock){
 				readerLock.notified = true;
 				readerLock.notify();
 			}
 		
 			/* Wait for it. */
 			while(!readerDoneLock.notified){
 				try{
 					readerDoneLock.wait();
 				}catch(InterruptedException irex){
 					// Ignore this; we don't want to know.
 				}
 			}
 			readerDoneLock.notified = false;
 		}
 		
 		byte[] parseTree = result;
 		
 		result = null; // Cleanup.
 		
 		return parseTree;
 	}
 	
 	private ByteBuffer getInputString(){
 		return inputString;
 	}
 	
 	private int getInputStringLength(){
 		return inputStringLength;
 	}
 	
 	private String getParseTableName(){
 		return parseTableName;
 	}
 	
 	private native void initialize();
 	
 	private native ByteBuffer parse();
 	
 	private static class NotifiableLock{
 		public boolean notified = false;
 	}
 }
