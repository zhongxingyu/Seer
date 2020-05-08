 package nl.tomsanders.processenprocessoren.emulator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class AsyncInputBuffer implements Runnable {
 	private InputStream stream;
 	private Thread thread;
 	private boolean open;
 	
 	private ConcurrentLinkedQueue<Integer> buffer;
 	
 	public AsyncInputBuffer() {
 		this(System.in);
 	}
 	
 	public AsyncInputBuffer(InputStream stream) {
 		this.stream = stream;
 		
 		this.buffer = new ConcurrentLinkedQueue<Integer>();
 		this.open = true;
 		this.thread = new Thread(this);
 		this.thread.start();
 	}
 
 	@Override
 	public void run() {
 		while (this.open) {
 			try {
 				byte[] lineBuffer = new byte[1024];
 				int input = this.stream.read(lineBuffer);
 				for (int i = 0; i < input - 2; i++) {
 					this.buffer.offer((int)lineBuffer[i]);
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	public int getNext() {
 		Integer element = this.buffer.poll();
 		
 		if (element != null)
 			return element;
 		else
 			return 0xFFFFFFFF;
 	}
 	
 	public void close() {
 		this.open = false;
 	}
 }
