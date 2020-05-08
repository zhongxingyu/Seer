 package ar.edu.it.itba.pdc.proxy.implementations.configurator.implementations;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 import javax.ws.rs.core.MediaType;
 
 import nl.bitwalker.useragentutils.Browser;
 import nl.bitwalker.useragentutils.OperatingSystem;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.interfaces.ConfiguratorConnectionDecoderInt;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.interfaces.ConnectionHandler;
 
 public class ConfiguratorConnectionHandler implements ConnectionHandler {
 
 	private int maxMessageLength;
 	private static String greeting = "Hello, please authenticate\n";
 	private ConfiguratorConnectionDecoderInt decoder;
 	private static Charset acceptedCharset = Charset.forName("ISO-8859-1");
 
 	public ConfiguratorConnectionHandler(int maxMessageLength,
 			ConfiguratorConnectionDecoderInt decoder) {
 		this.maxMessageLength = maxMessageLength;
 		this.decoder = decoder;
 	}
 
 	public void handle(Socket socket) throws IOException {
 		Logger configHandler = Logger.getLogger(this.getClass());
 		configHandler.setLevel(Level.INFO);
 		byte[] buffer = new byte[maxMessageLength];
 		ByteBuffer cumBuffer = ByteBuffer.allocate(maxMessageLength);
 		int receivedLength = 0, total = 0;
 		String send = new String(greeting.getBytes(), acceptedCharset);
 		OutputStream os = socket.getOutputStream();
 		InputStream is = socket.getInputStream();
 		boolean keepReading = true, firstTime = true;
 
 		while (!decoder.closeConnection()) {
 			if (firstTime) {
 				os.write(send.getBytes());
 				firstTime = false;
 			}
 			while (keepReading && (receivedLength = is.read(buffer)) != -1) {
 				total += receivedLength;
 				cumBuffer.put(buffer, cumBuffer.position(), receivedLength);
 				keepReading = reachedEnd(buffer);
 			}
 			configHandler.info("Received line from client: "
 					+ new String(cumBuffer.array()).substring(0, total));
 
 			String answer = decoder.decode(new String(cumBuffer.array())
 					.substring(0, total));
 			configHandler.info("Responding " + answer + " to client");
 
 			try {
 				os.write(answer.getBytes());
 			} catch (SocketException e) {
 				configHandler
 						.info("Client closed connection abruptly. Exiting handler");
 				return;
 			}
 			cumBuffer.clear();
 			keepReading = true;
 			receivedLength = 0;
 			total = 0;
 		}
 		configHandler
 				.info("Client closed connection. Closing socket, reseting decoder and exiting handler");
 		socket.close();
 		decoder.reset();
 	}
 
 	private boolean reachedEnd(byte[] data) {
 		String dataAsString = new String(data);
 		return dataAsString.contains("\r\n");
 	}
 
 	public boolean applyRotations(Browser b) {
 		return decoder.applyRotationsFor(b);
 	}
 
 	public boolean applyRotations(OperatingSystem b) {
 		return decoder.applyRotationsFor(b);
 	}
 
 	public boolean applyRotations(InetAddress b) {
 		return decoder.applyRotationsFor(b);
 	}
 
 	public boolean applyTextTransformation(Browser b) {
 		return decoder.applyTransformationsFor(b);
 	}
 
 	public boolean applyTextTransformation(OperatingSystem b) {
 		return decoder.applyTransformationsFor(b);
 	}
 
 	public boolean applyTextTransformation(InetAddress b) {
 		return decoder.applyTransformationsFor(b);
 	}
 
 	public int getMaxSize(Browser b, OperatingSystem os, InetAddress ip) {
 
 		int bs = decoder.getMaxSizeFor(b);
 		int oss = decoder.getMaxSizeFor(os);
 		int ips = decoder.getMaxSizeFor(ip);
		if (bs >= oss && bs >= ips) {
			return bs;
		}
		if (oss >= bs && oss >= ips)
			return oss;
		return ips;
 
 	}
 
 	public boolean isAccepted(InetAddress addr, Browser b, OperatingSystem os,
 			InetAddress ip) {
 		return isAccepted(addr, decoder.getBlockedAddressesFor(b))
 				&& isAccepted(addr, decoder.getBlockedAddressesFor(ip))
 				&& isAccepted(addr, decoder.getBlockedAddressesFor(os));
 	}
 
 	public boolean isAccepted(InetAddress addr, Object[] set) {
 		if (set == null)
 			return true;
 		for (Object blocked : set) {
 			if (blocked.equals((InetAddress) addr))
 				return false;
 		}
 		return true;
 	}
 
 	public boolean isAccepted(String str, Browser b, OperatingSystem os,
 			InetAddress ip) {
 		return isAccepted(str, decoder.getBlockedURIsFor(b))
 				&& isAccepted(str, decoder.getBlockedURIsFor(ip))
 				&& isAccepted(str, decoder.getBlockedURIsFor(os));
 	}
 
 	public boolean isAccepted(String str, Object[] set) {
 		if (set == null)
 			return true;
 		for (Object blocked : set) {
 			String regex = (String) blocked;
 			if (str.matches(regex)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean isAccepted(MediaType mt, Browser b, OperatingSystem os,
 			InetAddress ip) {
 		return isAccepted(mt, decoder.getBlockedMediaTypeFor(b))
 				&& isAccepted(mt, decoder.getBlockedMediaTypeFor(ip))
 				&& isAccepted(mt, decoder.getBlockedMediaTypeFor(os));
 	}
 
 	public boolean isAccepted(MediaType mtype, Object[] set) {
 		if (set == null)
 			return true;
 		for (Object blocked : set) {
 			if (mtype.toString().equals((String) blocked)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
