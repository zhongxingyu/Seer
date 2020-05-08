 /**
  * Handles messages from clients
  *
  * @author Eldar Damari, Ory Band.
  */
 
 package irc;
 
 import java.nio.channels.SocketChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ClosedChannelException;
 import java.nio.ByteBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.net.SocketAddress;
 import java.io.IOException;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 
 public class TpcConnectionHandler<T> implements ConnectionHandler<T>, Runnable {
 
 	private static final int BUFFER_SIZE = 1024;
 
 	protected final SocketChannel _sChannel;
 
 	protected final AsyncServerProtocol<T> _protocol;
 	protected final MessageTokenizer<T> _tokenizer;
 
 	protected Vector<ByteBuffer> _outData = new Vector<ByteBuffer>();
 
 	private static final Logger logger = Logger.getLogger("edu.spl.reactor");
 
 
 	/**
 	 * Creates a new ConnectionHandler object
 	 * 
 	 * @param sChannel
 	 *            the SocketChannel of the client
 	 * @param data
 	 *            a reference to a ReactorData object
 	 */
 	private TpcConnectionHandler(
             SocketChannel sChannel,
             AsyncServerProtocol<T> protocol,
             MessageTokenizer<T> tokenizer) {
 
 		_sChannel  = sChannel;
 		_protocol  = protocol;
 		_tokenizer = tokenizer;
 	}
 
 
 	public static <T> TpcConnectionHandler<T> create(
             SocketChannel sChannel,
             AsyncServerProtocol<T> protocol,
             MessageTokenizer<T> tokenizer) {
 
 		TpcConnectionHandler<T> h = new TpcConnectionHandler<T>(sChannel, protocol, tokenizer);
 
 		return h;
 	}
 
 
    public void run() {
         // go over all complete messages and process them.
         while ( ! _protocol.shouldClose() ) {
             read();
 
             while (_tokenizer.hasMessage()) {
                 T msg = _tokenizer.nextMessage();
 
                 T response = this._protocol.processMessage(msg);
 
                 // Reply to client if necessary.
                 if (response != null) {
                     try {
                         ByteBuffer bytes = _tokenizer.getBytesForMessage(response);
                         addOutData(bytes);
                     } catch (CharacterCodingException e) { e.printStackTrace(); }
                 }
             }
         }
 
         closeConnection();
     }
 
 
 	private void closeConnection() {
         logger.info(
                 _sChannel.socket().getInetAddress().toString() +
                 " disconnected.");
 
 		// remove from the selector.
 		try {
 			_sChannel.close();
 		} catch (IOException ignored) {
 			ignored = null;
             System.out.println("Error closing connectionHandler.");
 		}
 	}
 
 
 	/**
 	 * Reads some bytes from the SocketChannel
 	 * 
 	 * @throws IOException
 	 *             in case of an IOException during reading
 	 */
 	public void read() {
 		// do not read if protocol has terminated. only write of pending data is
 		// allowed
 		if (_protocol.shouldClose()) {
 			return;
 		}
 
 		SocketAddress address = _sChannel.socket().getRemoteSocketAddress();
 
 		ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
 		int numBytesRead = 0;
 		try {
 			numBytesRead = _sChannel.read(buf);
 		} catch (IOException e) {
 			numBytesRead = -1;
 		}
 		// is the channel closed??
 		if (numBytesRead == -1) {
 			_protocol.connectionTerminated();
 			return;
 		}
 
 		//add the buffer to the protocol task
 		buf.flip();
         this._tokenizer.addBytes(buf);
 	}
 
 
 	/**
 	 * attempts to send data to the client
 	 * 
 	 * @throws IOException
 	 *             if the write operation fails
 	 * @throws ClosedChannelException
 	 *             if the channel have been closed while registering to the Selector
 	 */
	public void write() {
         // if nothing left in the output string, return.
 		if (_outData.size() == 0) {
 			return;
 		}
 
 		// if there is something to send, send it.
         ByteBuffer buf = _outData.remove(0);
 		while (buf.remaining() > 0) {
 			try {
 				_sChannel.write(buf);
 			} catch (IOException e) {
 				// this should never happen.
 				e.printStackTrace();
 			}
 
 			// check if the buffer contains more data
             if (buf.remaining() == 0) {
                 if (_outData.size() > 0) {
                     buf = _outData.remove(0);
                 }
             }
 		}
 	}
 
 
     /**
      * @param buf byte string to be added to message queue.
      */
	public void addOutData(ByteBuffer buf) {
 		_outData.add(buf);
         write();
 	}
 
 
     /**
      * @param msg string to be added to message queue.
      */
	public void addOutData(T msg) {
         try {
             ByteBuffer bytes = _tokenizer.getBytesForMessage(msg);
             addOutData(bytes);
         } catch (CharacterCodingException e) {
             e.printStackTrace();
         }
 	}
 
 
     // Irrelevant for thread-per-client model.
 	public void switchToReadOnlyMode()  {}
 	public void switchToWriteOnlyMode() {}
 	public void switchToReadWriteMode() {}
 }
