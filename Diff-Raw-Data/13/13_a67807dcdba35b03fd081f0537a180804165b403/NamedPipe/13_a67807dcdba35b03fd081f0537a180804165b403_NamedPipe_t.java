 /**
  * 
  */
 package server;
 
import java.io.File;
import java.nio.file.Files;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import model.NamedPipeStream;
 
 /**
  * @author Derek Carr
  *
  */
 public class NamedPipe {
 	private RandomAccessFile pipe;
 	
 	public NamedPipe() throws FileNotFoundException {
 		try {
      //File file = new File("path");
      //file.delete();
			Runtime.getRuntime().exec("rm -rf pipe");
 			Runtime.getRuntime().exec("mkfifo pipe");
			Runtime.getRuntime().exec("chmod 666 pipe");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
		pipe = new RandomAccessFile("./pipe", "rw");
 	}
 
 	public NamedPipeStream readPipe() throws IOException {
 		String next =  pipe.readLine();
 
 		NamedPipeStream stream;
 		if (next != null) {
 
 			stream = new NamedPipeStream(next);
 		} else {
 			next = null;
 			stream = null;
 		}
 		return stream;
 	}
 }
