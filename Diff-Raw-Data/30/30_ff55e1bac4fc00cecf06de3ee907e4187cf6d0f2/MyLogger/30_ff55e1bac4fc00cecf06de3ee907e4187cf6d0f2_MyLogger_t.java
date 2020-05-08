 package assignmentImplementation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.io.RandomAccessFile;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import keyValueBaseInterfaces.LogRecord;
 import keyValueBaseInterfaces.Logger;
 
 public class MyLogger extends Thread implements Logger {
 
     private RandomAccessFile logFile;
     private ExecutorService executor;
     private boolean enabled;
     
     public MyLogger(RandomAccessFile logFile) {
         this.logFile = logFile;
         this.enabled = false;
     }
 
 	@Override
 	public void run() {
 	    executor = Executors.newFixedThreadPool(10);
 	}
 	
 	public void enable() {
 	    this.enabled = true;
 	}
 
 	@Override
 	synchronized public Future<?> logRequest(final LogRecord record) {
 		return executor.submit(new Callable<Void>() {
 		    public Void call() throws Exception {
 		        if (!enabled) {
 		            return null;
 		        }
 		        
 		        ByteArrayOutputStream b = new ByteArrayOutputStream();
 		        ObjectOutputStream out = new ObjectOutputStream(b);
 		        try {
 		            out.writeObject(record);
		            logFile.writeInt(b.size());
     		        logFile.write(b.toByteArray());
 		        } finally {
 		            out.close();
 		        }
 		        return null;
 		    }
 		});
 	}
 
 }
