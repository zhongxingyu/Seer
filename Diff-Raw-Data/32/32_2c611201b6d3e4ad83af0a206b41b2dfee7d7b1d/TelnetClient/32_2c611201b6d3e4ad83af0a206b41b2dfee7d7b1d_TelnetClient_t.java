 package com.sf.jintn3270.telnet;
 
 import com.sf.jintn3270.TerminalModel;
 import com.sf.jintn3270.DefaultTerminalModel;
 
 import java.net.InetSocketAddress;
 
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 import java.io.BufferedInputStream;
 
 import java.net.Socket;
 import java.net.UnknownHostException;
 import javax.net.ssl.SSLSocketFactory;
 
 import java.nio.ByteBuffer;
 
 import java.util.AbstractQueue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * TelnetClient is a Runnable that implements all I/O for Telnet functions.
  * 
  * Options can be added to the TelnetClient. Options which are successfully 
  * negotiated with the remote host end up enabled. 
  * 
  * Options allow for injecting bytes to be sent, and handling bytes that have
  * been received. Doing this allows us to create Options which maintain their
  * own state and implement the behavior necessary for the Option to function.
  *
  * This class, as well as all Options implement a simple mechanism for making
  * sure all the data necessary to carry out a task is available at the time
  * the consumeIncommingBytes method is called. If no one consumes any bytes 
  * (everything returns 'zero') 
  */
 public class TelnetClient extends Thread {
 	String host;
 	int port;
 	boolean ssl;
 	
 	Socket sock = null;
 	BufferedInputStream inStream;
 	
 	ByteArrayOutputStream outStream;
 	
 	AbstractQueue<Option> options;
 	
 	TerminalModel model;
 	
 	/* Constants as defined by RFC 854 */ 
 	
 	public static final byte IAC = (byte)255;
 	
 	private static final byte DONT = (byte)254;
 	private static final byte DO = (byte)253;
 	private static final byte WONT = (byte)252;
 	private static final byte WILL = (byte)251;
 	
 	
 	public static final byte NOP = (byte)241; // No Op!
 	public static final byte DM = (byte)242; // Data Mark... data stream portion of Sync
 	
 	public static final byte BRK = (byte)243; // Break
 	public static final byte IP = (byte)244; // Interrupt Process
 	public static final byte AO = (byte)245; // Abort Output
 	public static final byte AYT = (byte)246; // Are you there
 	public static final byte EC = (byte)247; // Erase Character
 	public static final byte EL = (byte)248; // Erase Line
 	public static final byte GA = (byte)249; // Go Ahead
 	public static final byte SB = (byte)250; // Start Subcommand
 	public static final byte SE = (byte)240; // End Subcommand
 	
 	/**
 	 * Create a TelnetClient with a DefaultTerminalModel that connects to the
 	 * given host and port, with the given ssl setting
 	 *
 	 * @param host The hostname / IP to connect to.
 	 * @param port The port to connect to (default for TELNET is 23)
 	 * @param ssl If <code>true</code> An SSLSocketFactory is used to create
 	 *        the socket. Otherwise, a normal client Socket is used.
 	 */
 	public TelnetClient(String host, int port, boolean ssl) {
 		this(host, port, ssl, new DefaultTerminalModel());
 	}
 	
 	
 	/**
 	 * Create a TelnetClient that writes to the given TerminalModel,
 	 * connects to the given host and port, with the given ssl setting
 	 *
 	 * @param host The hostname / IP to connect to.
 	 * @param port The port to connect to (default for TELNET is 23)
 	 * @param ssl If <code>true</code> An SSLSocketFactory is used to create
 	 *        the socket. Otherwise, a normal client Socket is used.
 	 * @param model The TerminalModel to write incoming data to.
 	 */
 	public TelnetClient(String host, int port, boolean ssl, TerminalModel model) {
 		this.model = model;
 		this.host = host;
 		this.port = port;
 		this.ssl = ssl;
 		
 		sock = null;
 		
 		options = new ConcurrentLinkedQueue<Option>();
 		options.add(new SuppressGA());
 		outStream = new ByteArrayOutputStream();
 	}
 	
 	
 	public TerminalModel getTerminalModel() {
 		return model;
 	}
 	
 	/**
 	 * You may want to call this from another thread...
 	 */
 	void connect() throws UnknownHostException, IOException {
 		if (isConnected()) {
 			disconnect();
 		}
 		
 		if (!ssl) {
 			sock = new Socket(host, port);
 		} else {
 			sock = SSLSocketFactory.getDefault().createSocket(host, port);
 		}
 		
 		if (sock != null) {
 			sock.setKeepAlive(true);
 			inStream = new BufferedInputStream(sock.getInputStream(), sock.getReceiveBufferSize());
 			connected();
 		}
 	}
 	
 	/**
 	 * Disconnects from the host.
 	 */
 	public void disconnect() {
 		if (sock != null) {
 			try {
 				sock.close();
 			} catch (IOException ioe) {
 			} finally {
 				sock = null;
 			}
 		}
 		disconnected();
 	}
 	
	/**
	 * Gets the hostname we're connecting to.
	 * @return the Hostname.
	 */
 	public String getHost() {
 		return host;
 	}
 	
	/**
	 * Gets the port we're connecting to.
	 * @return the port
	 */
 	public int getPort() {
 		return port;
 	}
 	
	/**
	 * Returns weather or not we're attempting to use SSL.
	 * @return ssl
	 */
 	public boolean useSSL() {
 		return ssl;
 	}
 	
 	/**
 	 * Invoked when we're disconnected
 	 */
 	protected void disconnected() {
		this.model.setClient(null);
 	}
 	
 	/**
 	 * Invoked when we're connected
 	 */
 	public void connected() {
 		this.model.setClient(this);
 		for (Option o : options) {
 			sendWill(o.getCode());
 		}
 	}
 	
 	/**
 	 * Determines if we have an output channel
 	 */
 	public boolean isConnected() {
 		return sock != null;
 	}
 	
 	/**
 	 * Add the option and send a WILL.
 	 */
 	public void addOption(Option o) {
 		options.add(o);
 		if (isConnected()) {
 			sendWill(o.getCode());
 		}
 	}
 	
 	
 	/**
 	 * Remove the option and send a WONT
 	 */
 	public void removeOption(Option o) {
 		if (o.isEnabled()) {
 			sendWont(o.getCode());
 		}
 	}
 	
 	/**
 	 * Send the given byte to the remote host.
 	 */
 	public void send(byte b) {
 		try {
 			// If we're using send to send 255, we need to escape it.
 			if (b == IAC) {
 				outStream.write(new byte[] {IAC, IAC});
 			} else {
 				outStream.write(b);
 			}
 		} catch (IOException e) {}
 	}
 	
 	/**
 	 * Send the outgoing non-command bytes
 	 */
 	public void send(byte[] bytes) {
 		// Look for 255 in the stream
 		for (byte b : bytes) {
 			send(b);
 		}
 	}
 	
 	
 	/**
 	 * Sends the outgoing byte preceeded by an IAC marker
 	 */
 	public void sendCommand(byte b) {
 		sendCommand(new byte[] {b});
 	}
 	
 	/**
 	 * Sends the outgoing bytes preceeded by an IAC marker.
 	 */
 	public void sendCommand(byte[] commandBytes) {
 		byte[] toSend = new byte[commandBytes.length + 1];
 		toSend[0] = IAC;
 		System.arraycopy(commandBytes, 0, toSend, 1, commandBytes.length);
 		try {
 			outStream.write(toSend);
 		} catch (IOException e) {} ;
 	}
 	
 	/**
 	 * Writes a DO option to the output buffer
 	 */
 	void sendDo(byte code) {
 		try {
 			outStream.write(new byte[] {IAC, DO, code});
 		} catch (IOException e) {}
 	}
 	
 	/**
 	 * Writes a WILL option to the output buffer.
 	 */
 	void sendWill(byte code) {
 		try {
 			outStream.write(new byte[] {IAC, WILL, code});
 		} catch (IOException e) {}
 	}
 	
 	/**
 	 * Writes a DONT option to the output buffer.
 	 */
 	void sendDont(byte code) {
 		try {
 			outStream.write(new byte[] {IAC, DONT, code});
 		} catch (IOException e) {}
 	}
 	
 	/**
 	 * Writes a WONT option to the output buffer.
 	 */
 	void sendWont(byte code) {
 		try {
 			outStream.write(new byte[] {IAC, WONT, code});
 		} catch (IOException e) {}
 	}
 	
 	
 	/**
 	 * The read/write loop (I/O thread) passing byte[] buffers to this method,
 	 * In this implementation, we look for IAC, and other TELNET commands to 
 	 * respond to. If we can handle the incoming data, we return the number of 
 	 * bytes we've read & handled. These bytes are then consumed by the input 
 	 * stream after this method is called. If zero bytes are consumed, the 
 	 * data is considered to be an incomplete frame, and will be re-delivered 
 	 * when more data is available.
 	 * 
 	 * @param incoming The bytes coming in from the buffer.
 	 * @return The number of bytes consumed in this pass.
 	 */
 	private int consumeIncomingBytes(byte[] incoming) {
 		int read = 0;
 		if (incoming[0] == IAC) {
 			if (incoming.length >= 2) {
 				switch(incoming[1]) {
 					case IAC: // Handle escaped 255. we leave 'read' at 0 so this will be passed along.
 						// Trim the first byte.
 						System.arraycopy(incoming, 1, incoming, 0, incoming.length - 1);
 						incoming[incoming.length - 1] = (byte)0x00;
 						break;
 					case WILL: // Option Offered! Send do or don't.
 						if (incoming.length >= 3) {
 							boolean dosent = false;
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									sendDo(o.getCode());
 									dosent = true;
 									break;
 								}
 							}
 							if (!dosent) {
 								System.out.println("Sending a dont for " + incoming[2]);
 								sendDont(incoming[2]);
 							}
 						}
 						read = 3;
 						break;
 					case DO: // Option requested. Send will or wont!
 						if (incoming.length >= 3) {
 							boolean enabled = false;
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									o.setEnabled(true);
 									enabled = true;
 									break;
 								}
 							}
 							if (enabled) {
 								sendWill(incoming[2]);
 							} else {
 								System.out.println("Sending a wont for " + incoming[2]);
 								sendWont(incoming[2]);
 							}
 						}
 						read = 3;
 						break;
 					case DONT: // Handle disable requests.
 					case WONT:
 						if (incoming.length >= 3) {
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									o.setEnabled(false);
 								}
 							}
 						}
 						read = 3;
 						break;
 					case DM: // Data Mark?
 					case NOP:
 					case BRK:
 					case IP:
 					case AO:
 					case AYT:
 						read = 2;
 						break;
 					case EC: // Erase Character
 						model.eraseChar();
 						read = 2;
 						break;
 					case EL: // Erase Line
 						model.eraseLine();
 						read = 2;
 						break;
 					case GA: // We got a GO-Ahead.
 						read = 2;
 						break;
 					case SB: // Sub-negotiation!
 						if (incoming.length >= 5) { // Must be at least IAC, SB, <code>, IAC, SE
 							for (Option o : options) {
 								if (o.getCode() == incoming[2]) {
 									read = o.consumeIncomingBytes(incoming, this);
 									break;
 								}
 							}
 						}
 						break;
 					default:
 						System.out.println("UNKNOWN IAC: " + incoming[2]);
 				}
 			}
 		}
 		
 		// If we didn't handle anything, we need to find something that can.
 		if (read == 0) {
 			// For any enabled options, let's try them.
 			for (Option o : options) {
 				if (o.isEnabled()) {
 					read += o.consumeIncomingBytes(incoming, this);
 				}
 			}
 		}
 		
 		// If no options handled the data, then we need to read up to the 
 		// next IAC, or the end of the buffer, and we'll treat that as 
 		// if it's data for display.
 		if (read == 0) {
 			for (byte b : incoming) {
 				if (b == IAC) {
 					break;
 				}
 				read++;
 			}
 			model.print(incoming, 0, read);
 		}
 		return read;
 	}
 	
 	
 	/**
 	 * Gathers and combines all the outgoing bytes from all the enabled Options
 	 * and our own internal output buffer for writing to the socket.
 	 */
 	private byte[] outgoingBytes() {
 		// collect all options outgoing bytes.
 		for (Option o : options) {
 			try {
 				outStream.write(o.outgoingBytes(outStream));
 			} catch (IOException ioe) {
 			}
 		}
 		
 		// append any of ours.
 		byte[] out = outStream.toByteArray();
 		outStream.reset();
 		
 		return out;
 	}
 	
 	
 	/**
 	 * The main I/O loop. As long as the socket is not null, and not closed, 
 	 * we continue.
 	 */
 	public void run() {
 		try {
 			connect();
 		} catch (Exception ex) {
 			disconnected();
 		}
 		
 		byte[] buf;
 		int available;
 		int consumed;
  		while (sock != null && !sock.isClosed()) {
 			try {
 				consumed = 0;
 				
 				// Do I have data to read?
 				available = inStream.available();
 				if (available > 0) {
 					inStream.mark(available);
 					
 					buf = new byte[available];
 					inStream.read(buf);
 					
 					// Determine how many bytes (if any) we've successfully
 					// consumed in this pass.
 					consumed = consumeIncomingBytes(buf);
 					
 					// reset the stream mark, then skip past the consumed bytes.
 					inStream.reset();
 					if (consumed > 0) {
 						inStream.skip(consumed);
 					}
 				}
 				
 				
 				// Do I have data to write?
 				buf = outgoingBytes();
 				if (buf.length > 0) {
 					sock.getOutputStream().write(buf);
 					sock.getOutputStream().flush();
 				}
 			} catch (Exception ex) {
 			} 
 			// Play nice with thread schedulers.
 			Thread.yield();
 		}
 	}
 	
 	
 	/**
 	 * Simple Test Harness
 	 */
 	public static void main(String[] args) {
 		TelnetClient client = new TelnetClient(args[0], Integer.parseInt(args[1]), Boolean.valueOf(args[2]).booleanValue());
 		client.addOption(new EndOfRecord());
 		client.start();
 	}
 }
