 package ar.edu.it.itba.pdc.Implementations.proxy.server;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Queue;
 
 import ar.edu.it.itba.pdc.Implementations.proxy.TCPSelector;
 import ar.edu.it.itba.pdc.Implementations.proxy.utils.DecoderImpl;
 import ar.edu.it.itba.pdc.Interfaces.Attachment;
 import ar.edu.it.itba.pdc.Interfaces.Decoder;
 import ar.edu.it.itba.pdc.Interfaces.ProxyWorker;
 import ar.edu.it.itba.pdc.Interfaces.TCPProtocol;
 
 public class ProxyServerSelectorProtocol implements TCPProtocol {
 
 	private static int bufSize = 20 * 1024;
 	public static Charset charset = Charset.forName("UTF-8");
 
 	private Map<SocketChannel, Decoder> requestDecoders = new HashMap<SocketChannel, Decoder>();
 	private Map<SocketChannel, Decoder> responseDecoders = new HashMap<SocketChannel, Decoder>();
 	private ProxyWorker worker;
 	private TCPSelector caller;
 
 	public ProxyServerSelectorProtocol() {
 	}
 
 	@Override
 	public void handleAccept(SelectionKey key) throws IOException {
 		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
 		clntChan.configureBlocking(false); // Must be nonblocking to register
 		// Register the selector with new channel for read and attach byte
 		// buffer
 		System.out.println(Calendar.getInstance().getTime().toString()
 				+ "-> Connection accepted. Client address: "
 				+ clntChan.socket().getInetAddress());
 		requestDecoders.put(clntChan, new DecoderImpl(bufSize));
 		responseDecoders.put(clntChan, new DecoderImpl(bufSize));
 
 		clntChan.register(key.selector(), SelectionKey.OP_READ);
 	}
 
 	@Override
 	public void handleRead(SelectionKey key) throws IOException {
 		// Client socket channel has pending data
 		SocketChannel clntChan = (SocketChannel) key.channel();
 		ByteBuffer buf = ByteBuffer.allocate(bufSize);
 
 		Decoder decoder = requestDecoders.get(clntChan);
 		long bytesRead;
 		try {
 			bytesRead = clntChan.read(buf);
 		} catch (IOException e) {
 			return;
 		}
 
 		if (bytesRead == -1) { // Did the other end close?
 			clntChan.close();
 			requestDecoders.remove(clntChan);
 		} else if (bytesRead > 0) {
 			byte[] write = buf.array();
 			decoder.decode(write, (int) bytesRead);
 			decoder.applyRestrictions();
 
 			System.out.println(Calendar.getInstance().getTime().toString()
 					+ "-> Request from client to proxy. Client address: "
 					+ clntChan.socket().getInetAddress());
 			boolean isMultipart = decoder.keepReading();
 
 			URL url = new URL("http://" + decoder.getHeader("Host"));
 
 			worker.sendData(caller, clntChan, write, bytesRead, isMultipart,
 					url);
 			buf.clear();
 			if (isMultipart)
 				key.interestOps(SelectionKey.OP_READ);
 		}
 	}
 
 	@Override
 	public void handleWrite(SelectionKey key,
 			Map<SocketChannel, Queue<ByteBuffer>> map) throws IOException {
 		// TODO: peek, do not remove. In case the buffer can not be completely
 		// written
 		ByteBuffer buf = map.get(key.channel()).peek();
		if(buf == null)
			return;
 		// buf.flip(); // Prepare buffer for writing
 		SocketChannel clntChan = (SocketChannel) key.channel();
 		Decoder decoder = responseDecoders.get(clntChan);
		System.out.println(buf);
 		decoder.decode(buf.array(), buf.array().length);
 		decoder.applyRestrictions();
		boolean isMultipart = decoder.keepReading();
 
 		System.out.println(Calendar.getInstance().getTime().toString()
 				+ "-> Response from proxy to client with status code "
 				+ decoder.getHeader("StatusCode") + ". Client address: "
 				+ clntChan.socket().getInetAddress());
 
 		if (!clntChan.isConnected()) {
 			clntChan.close();
 		}
 		try {
 			clntChan.write(buf);
 		} catch (IOException e) {
 			System.out.println("Broken pipe");
 			clntChan.close();
 			map.get(key.channel()).remove();
 			return;
 		}
 		// TODO: change condition. Shouldn't write any more if queue is empty
 		if (!buf.hasRemaining()) { // Buffer completely written?
 			map.get(key.channel()).remove();
 			if (map.get(key.channel()).isEmpty() && !isMultipart) {
 				// Nothing left, so no longer interested in writes
 				key.interestOps(SelectionKey.OP_READ);
				responseDecoders.put(clntChan, new DecoderImpl(bufSize));
 			} else {
 				key.interestOps(SelectionKey.OP_WRITE);
 				buf.clear();
 			}
 			buf.compact(); // Make room for more data to be read in
 		} else {
 			buf.compact();
 			key.interestOps(SelectionKey.OP_WRITE);
 		}
 	}
 
 	public void setWorker(ProxyWorker worker) {
 		this.worker = worker;
 	}
 
 	@Override
 	public void setCaller(TCPSelector caller) {
 		this.caller = caller;
 	}
 }
