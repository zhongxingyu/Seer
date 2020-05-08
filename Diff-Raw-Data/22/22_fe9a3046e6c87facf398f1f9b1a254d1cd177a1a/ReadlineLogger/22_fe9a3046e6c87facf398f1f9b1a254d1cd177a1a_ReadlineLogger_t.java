 package txtfnnl.uima.wrappers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.channels.ClosedByInterruptException;
 
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 class ReadlineLogger extends Thread {
 
 	/** Input stream of error messages. */
 	InputStream err;
 
 	/** The actual logger instance. */
 	Logger l;
 
 	/**
 	 * If <code>true</code>, the readline loop is canceled after the next
 	 * iteration.
 	 */
 	boolean stop;
 
 	/**
 	 * Create a new threaded logger for the given stream.
 	 * 
 	 * @param in the error stream to observe
 	 * @param a logger instance for the contents on the stream
 	 */
 	ReadlineLogger(InputStream in, Logger logger) {
 		err = in;
 		l = logger;
 		stop = false;
 	}
 
 	/**
 	 * Log additional (non-streamed) messages to the underlying logger.
 	 * 
 	 * @param level for the message
 	 * @param message to log
 	 */
 	public void log(Level level, String message) {
 		l.log(level, message);
 	}
 
 	/** Stop the readline loop an close the input stream. */
 	void halt() throws IOException {
 		stop = true;
 		err.close();
 	}
 
 	@Override
 	public void run() {
 		try {
 			InputStreamReader reader = new InputStreamReader(err, "UTF-8");
 			BufferedReader buffer = new BufferedReader(reader);
 			String line = null;
 
 			// first four lines: setup messages
 			while ((line = buffer.readLine()) != null) {
 				l.log(Level.WARNING, line.trim());
				if (stop) break;
 			}
 		} catch (ClosedByInterruptException e) {
 			l.log(Level.FINE, "error input stream interrupted");
 		} catch (IOException e) {
 			l.log(Level.WARNING, "threaded logger died: " + e.getMessage());
 		}
 	}
 
 }
