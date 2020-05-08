 package nardiff;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * Just outputs Object[]s to a .tsv file, one per line. Has a separate writer
  * thread; can accept input from several threads simultaneously. User is
  * responsible fora header row (if any) and writing the same data structure each
  * time i.e., for import into a database table.
  * 
  * @author kkoning
  * 
  */
 public class DataOutputFile implements Runnable {
 
 	static final int queueSize = 8000; // max # tuples to queue
 	static final int outputEveryTimes = 2000; // drain after # tuples
 	static final int outputEveryMS = 1000; // write at least once every 1s
 	static final int fileBufferSize = 8192; // or every 8K writes
 
 	static final String SEPARATOR = "\t";
 
 	ArrayBlockingQueue<Object[]> queue;
 	boolean shutdown = false;
 	Thread writerThread;
 
 	String fileName;
 	File outFile;
 	PrintStream out;
 	int approxQueueSize = 0;
 
 	public DataOutputFile(String fileName) {
 		this(fileName,null);
 	}
 	
 	public DataOutputFile(String fileName, String[] colNames) {
 		this.fileName = fileName;
 		queue = new ArrayBlockingQueue<Object[]>(queueSize);
 
 		try {
 			outFile = new File(fileName);
 			FileOutputStream fos = new FileOutputStream(outFile);
 			OutputStream bos = new BufferedOutputStream(fos, fileBufferSize);
 			out = new PrintStream(bos);
 		} catch (Exception e) {
			throw new RuntimeException("Commence Spanish Inquisition");
 		}
 
 		writerThread = new Thread(this); // Spawn writer thread
 		writerThread.start();
 		
 		// Add a shutdown hook to make sure file is flushed and closed
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				flush();
 				close();
 				try {
 					writerThread.join();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		});	
 
 		if (colNames != null) {
 			writeTuple(colNames);
 		}
 		
 		
 		
 	}
 	
 
 	public void close() {
 		shutdown = true;
 		writerThread.interrupt();
 	}
 
 	public void flush() {
 		writerThread.interrupt();
 	}
 
 	public synchronized void writeTuple(Object[] variables) {
 		if (shutdown)
 			throw new RuntimeException("Writing to a closed DataOutputFile");
 
 		try {
 			queue.put(variables);
 		} catch (InterruptedException e) {
 			throw new RuntimeException("Could not add tuple to " + this
 					+ " output queue");
 		}
 
 		approxQueueSize++;
 		if (approxQueueSize > outputEveryTimes) {
 			writerThread.interrupt();
 			approxQueueSize = 0;
 		}
 
 	}
 
 	@Override
 	public void run() {
 		ArrayList<Object[]> toWrite;
 
 		do {
 			try {
 				Thread.sleep(outputEveryMS);
 			} catch (InterruptedException e) {
 				// Don't worry; just draining early
 			}
 
 			toWrite = new ArrayList<Object[]>();
 			queue.drainTo(toWrite);
 			doOutput(toWrite);
 
 		} while (!shutdown);
 
 		// Do one final flush, after shutdown
 		toWrite = new ArrayList<Object[]>();
 		queue.drainTo(toWrite);
 		doOutput(toWrite);
 
 		// Close and flush the output file.
 		out.flush();
 		out.close();
 	}
 
 	private void doOutput(List<Object[]> tuples) {
 		// For now, just a tsv
 		for (Object[] var : tuples) {
 			StringBuffer sb = new StringBuffer();
 			int size = var.length;
 			for (int i = 0; i < size; i++) {
 				sb.append(var[i]);
 				if (i < (size - 1))
 					sb.append(SEPARATOR);
 			}
 			out.println(sb.toString());
 		}
 		out.flush();
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		DataOutputFile dos = new DataOutputFile("test.tsv");
 
 		int i;
 
 		for (i = 0; i < 1000000; i++) {
 			Object[] vars = new Object[4];
 			vars[0] = i;
 			vars[1] = i + "bob";
 			vars[2] = null;
 			vars[3] = i * Math.E;
 
 			dos.writeTuple(vars);
 
 		}
 
 		dos.close();
 
 	}
 
 }
