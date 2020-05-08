 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import server.NamedPipe;
 import server.VMQueue;
 import java.io.RandomAccessFile;
 import java.sql.SQLException;
 
 import model.Job;
 import model.NamedPipeStream;
 
 public class QueueMain {
 
   public static void main(String[] args) throws InterruptedException, IOException, SQLException {
     //VMQueue queue = new VMQueue();
     //File pipe = new File("/var/www/insidesales/pipefile");
   	NamedPipe pipe = new NamedPipe();
   	VMQueue queue = new VMQueue();
   	
   	while(true) {
   		NamedPipeStream stream = pipe.readPipe();
  		Job job = queue.buildJob(stream);
  		queue.addToQueue(job);
  		queue.processQueue();
   		Thread.sleep(1000);
   		// Loop through queue and process jobs
   	}
   }
 }
