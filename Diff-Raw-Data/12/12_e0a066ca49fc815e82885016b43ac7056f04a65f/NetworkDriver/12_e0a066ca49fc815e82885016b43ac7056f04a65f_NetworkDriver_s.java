 package network;
 
 import interfaces.MediatorNetwork;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 import data.Message;
 import data.Message.MessageType;
 
 public class NetworkDriver extends Thread {
 	private static Logger								logger	= Logger.getLogger(NetworkDriver.class);
 	// private static final Integer MAX_POOL_THREADS = 5;
 
 	private NetworkImpl									network;
 
 	private boolean										running;
 	private Selector									selector;
 	private ServerSocketChannel							serverSocketChannel;
 	private ArrayList<ServerSocketChannel>				serverChannels;
 	private ArrayList<SocketChannel>					socketChannels;
 
 	private ByteBuffer									rBuffer	= ByteBuffer.allocate(8192);
 	private ByteBuffer									wBuffer	= ByteBuffer.allocate(8192);
 
 	private Hashtable<SelectionKey, ArrayList<byte[]>>	writeBuffers;
 	private Hashtable<SelectionKey, byte[]>				readBuffers;
 
 	private LinkedList<ChangeRequest>					changeRequestQueue;
 
 	public NetworkDriver(NetworkImpl network) {
 		// TODO: logger.setLevel(Level.OFF);
 		this.network = network;
 
 		try {
 			selector = Selector.open();
 			serverSocketChannel = ServerSocketChannel.open();
 			serverSocketChannel.configureBlocking(false);
 			serverSocketChannel.socket().bind(null);
 
 			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
 
 			logger.debug("Linstening on " + serverSocketChannel.socket().getLocalSocketAddress());
 
 			serverChannels = new ArrayList<ServerSocketChannel>();
 			socketChannels = new ArrayList<SocketChannel>();
 
 			rBuffer = ByteBuffer.allocate(8192);
 			wBuffer = ByteBuffer.allocate(8192);
 
 			writeBuffers = new Hashtable<SelectionKey, ArrayList<byte[]>>();
 			readBuffers = new Hashtable<SelectionKey, byte[]>();
 			changeRequestQueue = new LinkedList<ChangeRequest>();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public InetSocketAddress getAddress() {
 		InetAddress localhost = null;
 		try {
 			localhost = InetAddress.getLocalHost();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 
 
 		logger.error("Address : " + serverSocketChannel.socket().getInetAddress().getCanonicalHostName());
 		try {
 			logger.error("IP Addr: " + InetAddress.getByAddress(localhost.getAddress()).getHostAddress());
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		logger.debug("Begin");
 		if (serverSocketChannel == null) {
 			logger.fatal("Server isn't listening on any address");
 			return null;
 		}
 
 		ServerSocket socket = serverSocketChannel.socket();
 		logger.debug("End");
 		String name = null;
 		try {
 			 name = InetAddress.getByAddress(localhost.getAddress()).getHostAddress();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		
 		//InetAddress.getByAddress(localhost.getAddress()).getHostAddress()
 		
 		return new InetSocketAddress(localhost, socket.getLocalPort());
 	}
 
 	public synchronized void startRunning() {
 		this.running = true;
 		this.start();
 	}
 
 	protected void accept(SelectionKey key) {
 		logger.debug("Begin");
 
 		SocketChannel socketChannel;
 		ServerSocketChannel serverSocketChannel;
 		Message message;
 
 		logger.debug("Accept a connection");
 		try {
 			serverSocketChannel = (ServerSocketChannel) key.channel();
 			socketChannel = serverSocketChannel.accept();
 			socketChannel.configureBlocking(false);
 			socketChannel.register(selector, SelectionKey.OP_READ);
 			socketChannels.add(socketChannel);
 
 			logger.debug("Done");
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		logger.debug("End");
 	}
 
 	private void appendMessage(Message message, SelectionKey key) {
 		logger.debug("Begin");
 		ConcurrentHashMap<String, SocketChannel> userChanelMap;
 		NetworkReceiveEvents networkEvents = network.getEventsTask();
 
 		userChanelMap = network.getUserChanelMap();
 
 		if (message.getType() == MessageType.SEND_USERNAME) {
 			String username = message.getSource();
 			logger.debug("SEND_USERNAME received from " + username);
 			userChanelMap.putIfAbsent(username, (SocketChannel) key.channel());
 
 			return;
 		}
 
 		networkEvents.enqueue(key, message);
 		logger.debug("End");
 	}
 
 	private static Integer byteArrayToInt(byte[] array) {
 		Integer result = 0;
 
 		if (array.length != Integer.SIZE / Byte.SIZE) {
 			logger.error("Integer size = 4, no more, no less");
 			return 0;
 		}
 
 		result = ((128 + (int) array[0]) << (3 * Byte.SIZE)) + ((128 + (int) array[1]) << (2 * Byte.SIZE))
 				+ ((128 + (int) array[2]) << Byte.SIZE) + (128 + (int) array[3]);
 
 		return result;
 	}
 
 	private void read(SelectionKey key) throws Exception {
 		logger.debug("Begin");
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		this.rBuffer.clear();
 
 		logger.debug("Rbuffer = " + rBuffer);
 		int numRead;
 
 		try {
 			numRead = socketChannel.read(this.rBuffer);
 		} catch (Exception e) {
 			key.channel().close();
 			key.cancel();
 			return;
 		}
 
 		if (numRead <= 0) {
 			logger.error("Socket closed for " + key);
 			key.channel().close();
 			key.cancel();
 			return;
 		}
 
 		byte[] rbuf = null;
 		rbuf = this.readBuffers.get(key);
 		logger.debug("readBuffer for this key : " + Arrays.toString(rbuf));
 
 		int rbuflen = 0;
 		if (rbuf != null) {
 			rbuflen = rbuf.length;
 		}
 
 		byte[] currentBuf = this.rBuffer.array();
 		logger.debug("Were read " + numRead + " bytes from the socket associated yo key " + key + " : " + currentBuf);
 
 		byte[] newBuf = new byte[rbuflen + numRead];
 
 		// Copiaza datele primite in newBuf (rbuf sunt datele primite
 		// anterior si care nu formeaza o cerere completa, iar currentBuf
 		// contine datele primite la read-ul curent.
 		for (int i = 0; i < rbuflen; i++) {
 			newBuf[i] = rbuf[i];
 		}
 
 		for (int i = rbuflen; i < newBuf.length; i++) {
 			newBuf[i] = currentBuf[i - rbuflen];
 		}
 
 		int i = 0;
 		int length = 0;
 
 		// Citire dimensiune cerere
 		if (i + 4 >= newBuf.length) {
 			return;
 		}
 
 		while (true) {
 			if (i + 4 >= newBuf.length) {
 				break;
 			}
 
 			logger.debug("Length as array : " + Arrays.toString(Arrays.copyOfRange(newBuf, i, i + 4)));
 
 			/* Get length */
 			length = byteArrayToInt(Arrays.copyOfRange(newBuf, i, i + 4));
 			if (i + length + 4 > newBuf.length) {
 				break;
 			}
 
 			logger.debug("Message length : " + length);
 
 			Message message = new Message(Arrays.copyOfRange(newBuf, i, length + i + 4));
 
 			logger.debug("Message received : " + message);
 			appendMessage(message, key);
 
 			i += 4 + length;
 		}
 
 		byte[] finalBuf = null;
 		if (i > 0) {
 			finalBuf = new byte[newBuf.length - i];
 			for (int j = i; j < newBuf.length; j++) {
 				finalBuf[j - i] = newBuf[j];
 			}
 		} else {
 			finalBuf = newBuf;
 		}
 
 		logger.debug("[NetworkDriver: read] For next time : " + finalBuf.length + " bytes");
 		this.readBuffers.put(key, finalBuf);
 		logger.debug("End");
 	}
 
 	protected void write(SelectionKey key) throws Exception {
 		logger.debug("Begin");
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		ArrayList<byte[]> wbuf = null;
 
 		synchronized (key) {
 			wbuf = this.writeBuffers.get(key);
 
 			while (wbuf.size() > 0) {
 				byte[] bbuf = wbuf.get(0);
 				wbuf.remove(0);
 
 				this.wBuffer.clear();
 				this.wBuffer.put(bbuf);
 				this.wBuffer.flip();
 
 				int numWritten = socketChannel.write(this.wBuffer);
 				logger.debug("Am scris " + numWritten + " bytes pe socket-ul asociat cheii " + key);
 
 				if (numWritten < bbuf.length) {
 					byte[] newBuf = new byte[bbuf.length - numWritten];
 
 					// Copiaza datele inca nescrise din bbuf in newBuf.
 					for (int i = numWritten; i < bbuf.length; i++) {
 						newBuf[i - numWritten] = bbuf[i];
 					}
 
 					wbuf.add(0, newBuf);
 					break;
 				}
 			}
 
 			if (wbuf.size() == 0) {
 				key.interestOps(SelectionKey.OP_READ);
 			}
 		}
 		logger.debug("End");
 	}
 
 	public void sendData(Message message, String username, InetSocketAddress address) {
 		logger.debug("Begin");
 		ConcurrentHashMap<String, SocketChannel> userChanelMap;
 		ConcurrentHashMap<String, ArrayList<Message>> userUnsentMessages;
 
 		userChanelMap = network.getUserChanelMap();
 		userUnsentMessages = network.getUserUnsentMessages();
 
 		if (!userChanelMap.containsKey(message.getDestination())) {
 			logger.debug("Initiate a new connection with " + message.getDestination());
 			
 			/* Initiate a new connection and save all messages */
 			userUnsentMessages.putIfAbsent(message.getDestination(), new ArrayList<Message>());
 			userUnsentMessages.get(message.getDestination()).add(message);
 			initiateConnect(address, username);
 		} else {
 			sendData(message, message.getDestination());
 		}
 		logger.debug("End");
 	}
 
 	public void sendData(Message message, String username) {
 		logger.debug("Begin");
 		logger.debug("Message : " + message);
 
 		sendData(network.getUserChanelMap().get(username).keyFor(selector), message.serialize());
 		logger.debug("End");
 	}
 
 	public void sendData(Message message, SelectionKey key) {
 		logger.debug("Begin");
 		logger.debug("Message : " + message);
 
 		logger.debug("Before serialization ...");
 		sendData(key, message.serialize());
 		logger.debug("End");
 	}
 
 	public void sendData(Message message, SocketChannel chanel) {
 		logger.debug("Begin");
 		logger.debug("Message : " + message);
 
 		sendData(message, chanel.keyFor(selector));
 		logger.debug("End");
 	}
 
 	public void sendData(SelectionKey key, byte[] data) {
 		logger.debug("Begin");
 		logger.debug("Se doreste scrierea a " + data.length + " bytes pe socket-ul asociat cheii " + key);
 
 		ArrayList<byte[]> wbuf = null;
 
 		synchronized (key) {
 			wbuf = this.writeBuffers.get(key);
 			if (wbuf == null) {
 				wbuf = new ArrayList<byte[]>();
 				this.writeBuffers.put(key, wbuf);
 			}
 
 			wbuf.add(data);
 			synchronized (changeRequestQueue) {
 				// this.changeRequestQueue.add(new ChangeRequest(key,
 				// SelectionKey.OP_READ | SelectionKey.OP_WRITE));
 				this.changeRequestQueue.add(new ChangeRequest((SocketChannel) key.channel(), ChangeRequest.CHANGEOPS,
 						SelectionKey.OP_WRITE));
 			}
 		}
 
 		this.selector.wakeup();
 		logger.debug("End");
 	}
 
 	public boolean initiateConnect(InetSocketAddress destination, String username) {
 		logger.debug("Begin");
 		SocketChannel socketChannel = null;
 		Boolean bRet;
 
 		try {
 			socketChannel = SocketChannel.open();
 			socketChannel.configureBlocking(false);
 			logger.debug("Connect to : " + destination + " " + username);
 			bRet = socketChannel.connect(destination);
 
 			if (bRet) {
 				logger.debug("Connection established");
 			} else {
 				logger.debug("Connection will be finish later");
 			}
 
 			logger.debug("Map : " + network.getUserChanelMap());
 			logger.debug("Username : " + username);
 			logger.debug("Key : " + socketChannel.keyFor(selector));
 			network.getUserChanelMap().putIfAbsent(username, socketChannel);
 			logger.debug("Before registering new interest");
 			// socketChannel.register(selector, SelectionKey.OP_CONNECT);
 
 			// Queue a channel registration since the caller is not the
 			// selecting thread. As part of the registration we'll register
 			// an interest in connection events. These are raised when a channel
 			// is ready to complete connection establishment.
 			synchronized (changeRequestQueue) {
 				changeRequestQueue
 						.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
 			}
 
 			logger.debug("Before wakeup");
 			selector.wakeup();
 			logger.debug("Wakeup was sent");
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		logger.debug("End");
 		return true;
 	}
 
 	private void finishConnection(SelectionKey key) {
 		logger.debug("Begin");
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		try {
 			socketChannel.finishConnect();
 		} catch (Exception e) {
 			logger.fatal("ERROR: finishConnect");
 			e.printStackTrace();
 			key.cancel();
 			return;
 		}
 
 		logger.debug("Connection finished");
 		key.interestOps(SelectionKey.OP_WRITE);
 
 		Message message = new Message();
 		message.setType(MessageType.SEND_USERNAME);
 		message.setSource(network.getUserProfile().getUsername());
 
 		sendData(key, message.serialize());
 
 		logger.debug("Chanel = " + key.channel());
 		logger.debug("KeyMap = " + network.getUserChanelMap());
 		/* Check if we know who is at the other end of the connection */
 		if (network.getUserChanelMap().containsValue(key.channel())) {
 			String username = null;
 			for (String user : network.getUserChanelMap().keySet()) {
 				if (network.getUserChanelMap().get(user).equals(key.channel())) {
 					username = user;
 					break;
 				}
 			}
 
 			/* Send all pending messages */
 			ConcurrentHashMap<String, ArrayList<Message>> unsentMessages = network.getUserUnsentMessages();
 			logger.debug("Unsent messages list = " + unsentMessages);
 			logger.debug("Username : " + username);
 			if (unsentMessages.containsKey(username)) {
 				logger.debug("Send pending messages");
 				network.getSendEvents().enqueue(socketChannel, unsentMessages.get(username));
 			}
 		} else {
 			// TODO : Make & send an username request
 			logger.fatal("Something wrong went ...");
 		}
 		logger.debug("End");
 	}
 
 	public void run() {
 		ChangeRequest creq;
 
 		try {
 			while (isRunning()) {
 
 				synchronized (changeRequestQueue) {
 
 					Iterator changes = this.changeRequestQueue.iterator();
 					while (changes.hasNext()) {
 						ChangeRequest change = (ChangeRequest) changes.next();
 						switch (change.type) {
 						case ChangeRequest.CHANGEOPS:
 							logger.debug("CHANGEOPS = " + change.ops);
 							SelectionKey key = change.socket.keyFor(selector);
 							key.interestOps(change.ops);
 							break;
 						case ChangeRequest.REGISTER:
 							logger.debug("REGISTER = " + change.ops);
 							change.socket.register(selector, change.ops);
 							break;
 						}
 					}
 					this.changeRequestQueue.clear();
 				}
 
 				// logger.debug("Listening on " +
 				// getAddress().getPort());
 				selector.select();
 
 				// logger.debug("After select");
 
 				for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
 					SelectionKey key = it.next();
 					it.remove();
 
 					// logger.debug("for's body");
 
 					if (!key.isValid()) {
 						logger.debug("Key isn't valid");
 						continue;
 					}
 
 					if (key.isAcceptable()) {
 						logger.debug("accept");
 						accept(key);
 					} else if (key.isReadable()) {
 						logger.debug("read");
 						read(key);
 					} else if (key.isWritable()) {
 						logger.debug("write");
 						write(key);
 					} else if (key.isConnectable()) {
 						logger.debug("connect");
 						finishConnection(key);
 					}
 				}
 
 				// synchronized (changeRequestQueue) {
 				// // while ((creq = this.changeRequestQueue.poll()) != null) {
 				// //
 				// logger.debug("Schimb operatiile cheii "
 				// // + creq.key + " la " + creq.newOps);
 				// // creq.key.interestOps(creq.newOps);
 				// // }
 				//
 				// Iterator changes = this.changeRequestQueue.iterator();
 				// while (changes.hasNext()) {
 				// ChangeRequest change = (ChangeRequest) changes.next();
 				// switch (change.type) {
 				// case ChangeRequest.CHANGEOPS:
 				// SelectionKey key = change.socket.keyFor(this.selector);
 				// key.interestOps(change.ops);
 				// break;
 				// case ChangeRequest.REGISTER:
 				// change.socket.register(this.selector, change.ops);
 				// logger.debug("ChangeRequest.REGISTER");
 				// break;
 				// }
 				// }
 				// this.changeRequestQueue.clear();
 				// }
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (selector != null) {
 					selector.close();
 				}
 
 				for (ServerSocketChannel ssc : serverChannels) {
 					ssc.close();
 				}
 
 				for (SocketChannel schan : socketChannels) {
 					schan.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public synchronized void stopRunning() {
 		this.running = false;
 		this.selector.wakeup();
 	}
 
 	protected synchronized boolean isRunning() {
 		return this.running;
 	}
 
 	public MediatorNetwork getMediator() {
 		return network.getMediator();
 	}
 
 	public Selector getSelector() {
 		return selector;
 	}
 
 	public void setSelector(Selector selector) {
 		this.selector = selector;
 	}
 
 	public boolean haveToProcess() {
 		// logger.debug("Write buffers : ");
 		for (Map.Entry<SelectionKey, ArrayList<byte[]>> entry : writeBuffers.entrySet()) {
 			if (!entry.getValue().isEmpty()) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public void removeAllDependencies(SocketChannel chanel) {
 		Integer index = socketChannels.indexOf(chanel);
 		SelectionKey key = chanel.keyFor(selector);
 
 		socketChannels.remove(index);
 		serverChannels.remove(index);
 
 		writeBuffers.remove(key);
 		readBuffers.remove(key);
 	}
 }
