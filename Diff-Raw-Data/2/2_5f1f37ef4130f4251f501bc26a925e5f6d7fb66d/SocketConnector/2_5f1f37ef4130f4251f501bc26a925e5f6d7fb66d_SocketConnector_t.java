 /**
  * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.huy.firebase.clients.j2me.connector;
 
 import com.cubeia.firebase.api.util.Arguments;
 import com.cubeia.firebase.clients.java.connector.*;
 import com.cubeia.firebase.io.ProtocolObject;
 import com.cubeia.firebase.io.StyxSerializer;
 import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
 import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
 import com.jmobilecore.comm.BufferedInputStream;
 import com.jmobilecore.comm.BufferedOutputStream;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;
 import j2me.nio.ByteBuffer;
 import j2me.util.logging.Level;
 import java.io.*;
 import java.security.GeneralSecurityException;
 import javax.microedition.io.Connector;
 import javax.microedition.io.SocketConnection;
 import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
 import org.bouncycastle.crypto.params.RSAKeyParameters;
 
 public class SocketConnector extends ConnectorBase {
 
 	private final StyxSerializer styx = new StyxSerializer(new ProtocolObjectFactory());
 	private SocketConnection socket;
 	private StreamReader reader;
 	private StreamWriter writer;
 	private final Encryption encryption;
 	private AsymmetricCipherKeyPair keyExchange;
 	//AES cryptoProvider
 	private AtomicReference crypto = new AtomicReference(null);
 	private RSACryptoProvider rsaCrypto = new RSACryptoProvider();
 	private long keyExchangeWait = CryptoConstants.DEFAULT_KEY_ECHANGE_WAIT;
 	private final String host;
 	private final int port;
 	// use OutputStream to send requests
 	private DataOutputStream dataOutputStream = null;
 	// use InputStream to receive responses from server
 	private DataInputStream dataInputStream = null;
 
 	/**
 	* @param host Host to connect to, must not be null
 	* @param port Port to connect to, must be > 0
 	* @param listener Initial listener, may be null
 	* @param encryption Encryption to use, or null for none
 	* @param useHandshake True if handshake should be used, false otherwise
 	* @param handshakeSignature Handshake to use if "useHandshake" is true
 	* @throws IOException On general IO errors
 	* @throws GeneralSecurityException On SSL errors
 	*/
 	public SocketConnector(String host, int port, PacketListener listener, Encryption encryption, boolean useHandshake, int handshakeSignature) throws IOException, GeneralSecurityException {
 		super(useHandshake, handshakeSignature);
 		Arguments.notNull(host, "host");
 		if (listener != null) {
 			addListener(listener);
 		}
 		this.encryption = (encryption == null ? Encryption.NONE : encryption);
 		this.host = host;
 		this.port = port;
 	}
 
 /**
 	* @param host Host to connect to, must not be null
 	* @param port Port to connect to, must be > 0
 	* @param encryption Encryption to use, or null
 	* @param useHandshake True if handshake should be used, false otherwise
 	* @param handshakeSignature Handshake to use if "useHandshake" is true
 	* @throws IOException On general IO errors
 	* @throws GeneralSecurityException On SSL errors
 	*/
 	public SocketConnector(String host, int port, Encryption encryption, boolean useHandshake, int handshakeSignature) throws IOException, GeneralSecurityException {
 		this(host, port, null, encryption, useHandshake, handshakeSignature);
 	}
 
 /**
 	* @param host Host to connect to, must not be null
 	* @param port Port to connect to, must be > 0
 	* @param encryption Encryption to use, or null
 	* @throws IOException On general IO errors
 	* @throws GeneralSecurityException On SSL errors
 	*/
 	public SocketConnector(String host, int port, Encryption encryption) throws IOException, GeneralSecurityException {
 		this(host, port, null, encryption, false, -1);
 	}
 
 /**
 	* @param host Host to connect to, must not be null
 	* @param port Port to connect to, must be > 0
 	* @throws IOException On general IO errors
 	* @throws GeneralSecurityException On SSL errors
 	*/
 	public SocketConnector(String host, int port) throws IOException, GeneralSecurityException {
 		this(host, port, null, Encryption.NONE, false, -1);
 	}
 
 /**
 	* This object waits for the session key to arrive when created
 	* with native firebase encryption enabled. This method specifies
 	* the default wait for the session key in milliseconds. Set to -1
 	* to disable waiting.
 	* 
 	* @param millis Millis to wait for session key, or -1 for no wait
 	*/
 	public void setKeyExchangeWait(long millis) {
 		this.keyExchangeWait = millis;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * com.cubeia.firebase.clients.java.connector.Connector#connect()
 	 */
 	public void connect() throws IOException, GeneralSecurityException {
 		try {
 			socket = createSocket(host, port);
 		} catch (Exception ex) {
 		}
 		dataInputStream = new DataInputStream(socket.openInputStream());
 		reader = new StreamReader(dataInputStream);
 
 		dataOutputStream = new DataOutputStream(socket.openDataOutputStream());
 		writer = new StreamWriter(dataOutputStream);
 
 		//reader.setDaemon(true);
 		reader.start();
 		checkSendHandshake();
 		checkSendKeyExchange();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * com.cubeia.firebase.clients.java.connector.Connector#send(com.cubeia.firebase.io.ProtocolObject)
 	 */
 	public void send(ProtocolObject packet) {
 		Arguments.notNull(packet, "packet");
 		writer.sendPacket(packet);
 	}
 
 	public ProtocolObject read() throws IOException {
 		return reader.readPacket();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * com.cubeia.firebase.clients.java.connector.Connector#disconnect()
 	 */
 	public void disconnect() {
 		dispatcher.complete();
 		reader.close();
 		writer.close();
 		closeSocket();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * com.cubeia.firebase.clients.java.connector.Connector#isConnected()
 	 */
 	public boolean isConnected() {
 		return (socket != null);
 	}
 
 	protected void handleReadException(Exception e) {
 		if (e instanceof IOException) {
 			if (e instanceof EOFException) {
 				log.log(Level.SEVERE, "Remote connection closed");
 			} else {
 				log.log(Level.SEVERE, "Faile to read packet", e);
 			}
 		} else if (e instanceof GeneralSecurityException) {
 			log.log(Level.SEVERE, "General security error", e);
 		} else {
 			log.log(Level.SEVERE, "Unknown error", e);
 		}
 	}
 
 	// --- PRIVATE METHODS --- //
 	private void checkSendKeyExchange() throws GeneralSecurityException {
 		if (encryption == Encryption.FIREBASE_NATIVE) {
 			this.keyExchange = RSACryptoProvider.generateRSAKey();
 			EncryptedTransportPacket p = new EncryptedTransportPacket();
 			p.func = CryptoConstants.SESSION_KEY_REQUEST;
 			String key = ((RSAKeyParameters) keyExchange.getPublic()).getModulus().toString(16);
 			p.payload = key.getBytes(); // CHARSET ?!
 			log.info("Sending session key request (RSA)");
 			send(p);
 			if (keyExchangeWait >= 0) {
 				log.info("Waiting for session key exchange to finnish for " + keyExchangeWait + " millis");
 				synchronized (keyExchange) {
 					if (crypto.get() == null) {
 						try {
 							keyExchange.wait(keyExchangeWait);
 						} catch (InterruptedException e) {
 						}
 
 						if (crypto.get() == null) {
 							log.log(Level.WARNING, "Key exchange not finished; No package will be encrypted until session key arrives");
 						}
 					}
 				}
 			} else {
 				log.log(Level.WARNING, "Key exchange not finished, no package should be sent until session key has arrived");
 			}
 		}
 	}
 
 	private void checkSendHandshake() throws IOException {
 		if (useHandshake) {
 			writer.sendHandshake();
 		}
 	}
 
 	private void closeSocket() {
 		try {
 			socket.close();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Failed to close connector", e);
 		}
 	}
 
 	private void dispatch(final ProtocolObject packet) throws IOException, GeneralSecurityException {
 		if (packet instanceof EncryptedTransportPacket) {
 			/*
 			 * We'll be slightly naive here, we'll assume that 
 			 * the first encrypted packet is the key...
 			 */
 			EncryptedTransportPacket wrap = (EncryptedTransportPacket) packet;
 			if (wrap.func == CryptoConstants.SESSION_KEY_RESPONSE) {
 				synchronized (keyExchange) {
 					byte[] decrypted = rsaCrypto.decrypt(wrap.payload, keyExchange.getPrivate());
 
 					SessionKey key = new SessionKey(decrypted);
 					crypto.set(new AESCryptoProvider());
 					((AESCryptoProvider) crypto.get()).setSessionKey(key);
 					keyExchange.notifyAll();
 				}
 			} else if (wrap.func == CryptoConstants.ENCRYPTED_DATA) {
 				if (crypto.get() == null) {
 					throw new IllegalStateException("Received encrypted data before session key");
 				}
 				byte[] decrypted = ((AESCryptoProvider) crypto.get()).decrypt(wrap.payload);
 				ProtocolObject unpacked = styx.unpack(ByteBuffer.wrap(decrypted));
 				doFinalDispatch(unpacked);
 			} else if (wrap.func == CryptoConstants.ENCRYPTION_MANDATORY) {
 				log.log(Level.SEVERE, "Server demands native Firebase packet encryption, but client is using " + encryption);
 			} else {
 				throw new IllegalStateException("Illegal ecrypted package function: " + wrap.func);
 			}
 		} else {
 			doFinalDispatch(packet);
 		}
 	}
 
 	private void doFinalDispatch(final ProtocolObject packet) {
 		dispatcher.assign(new Runnable() {
 
 			public void run() {
 				for (int i = 0; i < listeners.size(); i++) {
 					PacketListener v = (PacketListener) listeners.get(i);
 					v.packetRecieved(packet);
 				}
 			}
 		});
 	}
 
 	private SocketConnection createSocket(String host, int port) throws IOException, GeneralSecurityException, Exception {
 		if (encryption == Encryption.NAIVE_SSL || encryption == Encryption.SSL) {
 			throw new Exception("Not support SSL yet");
 		} else {
 			SocketConnection conn = (SocketConnection) Connector.open("socket://" + host + ":" + port);
 			conn.setSocketOption(SocketConnection.DELAY, 0);
 			conn.setSocketOption(SocketConnection.KEEPALIVE, 0);
 			return conn;
 		}
 	}
 
 	// --- PRIVATE CLASSES --- //
 	private class StreamReader extends Thread {
 
 		private final DataInputStream in;
 		private final AtomicBoolean flag;
 
 		private StreamReader(InputStream stream) {
 			in = new DataInputStream(new BufferedInputStream(stream));
 			flag = new AtomicBoolean(true);
 		}
 
 		public void run() {
 			doRead();
 			try {
 				doClose();
 			} catch (IOException ex) {
 			}
 		}
 
 		public void close() {
 			flag.set(false);
 		}
 
 		// --- PRIVATE METHODS --- //
 		private void doClose() throws IOException {
 			in.close();
 			//IoUtil.safeClose(in);
 		}
 
 		private void doRead() {
 			try {
 				while (flag.get()) {
 					ProtocolObject packet = readPacket();
 					if (packet != null) {
 						dispatch(packet);
 					}
 				}
 			} catch (Exception e) {
 				handleReadException(e);
 			}
 		}
 
 		private ProtocolObject readPacket() throws IOException {
 			int len = in.readInt();
 			if (!flag.get()) {
 				return null;
 			}
 			byte[] arr = new byte[len - 4];
 			in.readFully(arr);
 			if (!flag.get()) {
 				return null;
 			}
 			return unpack(len, arr);
 		}
 
 		private ProtocolObject unpack(int len, byte[] arr) throws IOException {
 			ByteBuffer buf = toByteBuffer(len, arr);
 			return styx.unpack(buf);
 		}
 
 		private ByteBuffer toByteBuffer(int len, byte[] arr) {
 			ByteBuffer buf = ByteBuffer.allocateDirect(len);
 			buf.putInt(len);
			buf.position(buf.position() + 3);
 			buf.put(arr);
 			buf.rewind();
 			return buf;
 		}
 	}
 
 	private class StreamWriter {
 
 		private DataOutputStream out;
 
 		private StreamWriter(OutputStream stream) {
 			out = new DataOutputStream(new BufferedOutputStream(stream));
 		}
 
 		public void close() {
 			try {
 				//IoUtil.safeClose(out);
 				out.close();
 			} catch (IOException ex) {
 				//TODO
 			}
 		}
 
 		public void sendPacket(ProtocolObject packet) {
 			try {
 				if (crypto.get() != null) {
 					packet = encrypt(packet);
 				}
 
 				ByteBuffer buffer = styx.pack(packet);
 				byte[] array = buffer.array();
 				out.write(array);
 				out.flush();
 			} catch (Exception ex) {
 				log.log(Level.SEVERE, "Faile to write packet", ex);
 			}
 		}
 
 		private ProtocolObject encrypt(ProtocolObject packet) throws IOException, GeneralSecurityException {
 			ByteBuffer buffer = styx.pack(packet);
 			byte[] array = buffer.array();
 			byte[] encrypted = ((AESCryptoProvider) crypto.get()).encrypt(array);
 			EncryptedTransportPacket p = new EncryptedTransportPacket();
 			p.func = CryptoConstants.ENCRYPTED_DATA;
 			p.payload = encrypted;
 			return p;
 		}
 
 		public void sendHandshake() throws IOException {
 			out.writeInt(handshakeSignature);
 			out.flush();
 		}
 	}
 }
