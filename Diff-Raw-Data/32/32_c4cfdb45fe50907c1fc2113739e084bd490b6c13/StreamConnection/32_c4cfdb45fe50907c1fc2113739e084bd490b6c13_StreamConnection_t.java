 package bs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.util.NoSuchElementException;
 
 import comm.Message;
 
 /**
  * 
  */
 public abstract class StreamConnection extends Connection {
 	private InputStream in;
 	private OutputStream out;
 
 	private boolean connected = false;
 
 	/**
 	 * A thread to continuously read and process messages from the input stream
 	 */
 	private class Reader extends Thread {
 		public void run() {
 			// repeat until interrupted
 			while (!Thread.interrupted()) {
 				Message msg = null;
 				Log.v(this, "Waiting for message from stream");
 				try {
 					msg = Message.deserialize(in);
				} catch (NoSuchElementException e) {
					// sleep if no messages have been received yet
					try {
						Thread.sleep(100);
					} catch (InterruptedException e2) {
					}
 				} catch (IOException e) {
 					// Disconnect and terminate if there is an error in reading
 					// the next message from the input stream
 					disconnect();
 					e.printStackTrace();
 					Log.d(this, "Terminating reader thread");
 					return;
 				}
 				// if we've successfully read a message
 				if (msg != null) {
 					notifyMessageReceiver(msg);
 				}
 			}
 		}
 	};
 
 	private Thread readerThread;
 
 	/**
 	 * Starts listening for messages on a separate thread
 	 * 
 	 * @param in
 	 *            The input stream on which to begin listening for messages
 	 * @param out
 	 *            The output stream to write messages
 	 */
 	public void connect(InputStream in, OutputStream out) {
 		if (!connected) {
 			this.in = in;
 			this.out = out;
 			if (readerThread != null) {
 				readerThread.interrupt();
 			}
 			readerThread = new Reader();
 			readerThread.start();
 
 			connected = true;
 			notifyConnectionEstablished();
 		}
 	}
 
 	/**
 	 * Stops the thread listening for messages and closes the IO streams
 	 */
 	public final void disconnect() {
 		if (readerThread.isAlive()) {
 			readerThread.interrupt();
 		}
 		try {
 			if (in != null) {
 				in.close();
 			}
 		} catch (IOException e) {
 			Log.e(this, "Error closing input stream: " + e.getMessage());
 		}
 		try {
 			if (out != null) {
 				out.close();
 			}
 		} catch (IOException e) {
 			Log.e(this, "Error closing output stream: " + e.getMessage());
 		}
 
 		connected = false;
 		notifyConnectionLost();
 	}
 
 	@Override
 	public final boolean isConnected() {
 		return connected;
 	}
 
 	@Override
 	public final synchronized boolean sendMessage(Message m) {
 		Log.v(this, "sendMessage(" + m + ")");
 		if (out == null) {
 			Log.e(this, "Unable to write message to null output stream");
 			return false;
 		}
 
 		try {
 			m.serialize(out);
 		} catch (IOException e) {
 			// if there was an error writing to the output stream, disconnect
			Log.e(this, "Error writing message to stream");
			e.printStackTrace();
 			disconnect();
 			return false;
 		}
 		return true;
 	}
 
 }
