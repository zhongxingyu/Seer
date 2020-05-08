 package server;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class Pipe implements Runnable {
 
 	InputStream inputStream;
 	OutputStream outputStream;
 
 	// read ==> write
 	public Pipe(InputStream inputStream, OutputStream outputStream) {
 		this.inputStream = inputStream;
 		this.outputStream = outputStream;
 
 	}
 
 	/**
 	 * Connect the output to the input
 	 */
 	public void run() {
 		try {
			// this might need some tweaking later 
			// but it works for now
 			//for (;;) {
 				byte[] buffer = new byte[1024];
 				int len;
 				len = inputStream.read(buffer);
 				while (len != -1) {
 					BufferedOutputStream bos = new BufferedOutputStream(outputStream);
 					bos.write(buffer, 0, len);
 					bos.flush();
 					len = inputStream.read(buffer);
 					if (Thread.interrupted()) {
 						throw new InterruptedException();
 					}
 				}
 				//System.out.println("END OF THE PIPE REACHED");
 			//}
 		} catch (IOException e) {
 			return;// ?
 		} catch (InterruptedException e) {
 			return; // why not?
 		} finally {
 			// close the stream
 			try {
 				this.inputStream.close();
 			} catch (IOException e) {
 				// don't do anything because it might already be closed
 			}
 			try {
 				this.outputStream.close();
 			} catch (IOException e) {
 				// don't do anything because it might already be closed
 			}
 
 		}
 
 	}
 
 }
