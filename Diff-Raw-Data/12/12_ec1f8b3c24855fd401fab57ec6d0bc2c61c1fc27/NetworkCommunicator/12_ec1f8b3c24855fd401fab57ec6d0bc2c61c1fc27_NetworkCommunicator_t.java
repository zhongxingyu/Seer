 package AuctionHouse.Network;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.SequenceInputStream;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Logger;
 
 import AuctionHouse.Mediator.NetworkMediator;
 import AuctionHouse.Mediator.Transaction;
 import AuctionHouse.NetworkMessages.FileNetworkMessage;
 import AuctionHouse.NetworkMessages.NetworkMessage;
 import AuctionHouse.NetworkMessages.StartTransactionNetworkMessage;
 
 /**
  * Communicates directly (p2p) with another user (of opposite role) Also
  * sends/receives services (files)
  */
 
 public class NetworkCommunicator extends Thread {
 
 	final Logger logger = Logger.getLogger("generic.mediator.acceptoffer");
 
 	/*
 	 * this should be made configurable from file!!!
 	 */
 	public static final int NumberOfThreadsInPool = 3;
 
 	private NetworkMediator mediator;
 
 	private InetSocketAddress hostSocketAddress;
 	private Selector selector;
 	private ArrayList<SocketChannel> socketChannels;
 	private ExecutorService threadPool;
 	private LinkedBlockingQueue<ByteBuffer> bufferPool;
 
 	/*
 	 * Is the hash() method of these classes well implemented? Or is it a hash
 	 * on the Object reference? Possible source of bugs
 	 */
 	private HashMap<SocketAddress, SocketChannel> addressToChannel;
 	private HashMap<SelectionKey, MessageBuffer> readBuffers;
 	private HashMap<SocketChannel, LinkedList<InputStream>> writeBuffers;
 
 	private LinkedList<ChangeRequest> changeRequestQueue;
 
 	// private ByteBuffer rBuffer = ByteBuffer.allocate(8192);
 
 	private boolean running;
 
 	public NetworkCommunicator(NetworkMediator mediator, String hostIp,
 			int hostPort) {
 		super();
 
 		this.mediator = mediator;
 		hostSocketAddress = new InetSocketAddress(hostIp, hostPort);
 		try {
 			selector = SelectorProvider.provider().openSelector();
 		} catch (Exception e) {
 			logger.error("Error on Selector creation: " + e);
 			e.printStackTrace();
 		}
 		this.threadPool = Executors.newFixedThreadPool(NumberOfThreadsInPool);
 		this.bufferPool = new LinkedBlockingQueue<ByteBuffer>();
 
 		for (int i = 0; i < NumberOfThreadsInPool; i++) {
 			this.bufferPool.add(ByteBuffer.allocate(8192));
 		}
 
 		readBuffers = new HashMap<SelectionKey, MessageBuffer>();
 		writeBuffers = new HashMap<SocketChannel, LinkedList<InputStream>>();
 		addressToChannel = new HashMap<SocketAddress, SocketChannel>();
 
 		this.changeRequestQueue = new LinkedList<ChangeRequest>();
 
 		socketChannels = new ArrayList<SocketChannel>();
 	}
 
 	public synchronized void startRunning() {
 		this.running = true;
 		this.start();
 	}
 
 	public synchronized void stopRunning() {
 		this.running = false;
 		this.selector.wakeup();
 	}
 
 	protected synchronized boolean isRunning() {
 		return this.running;
 	}
 
 	public void run() {
 		logger.info("Server started on port: "
 				+ hostSocketAddress.getPort());
 
 		// Creeaza ServerSocketChannels
 		ServerSocketChannel serverChannel = null;
 		try {
 			serverChannel = ServerSocketChannel.open();
 			serverChannel.configureBlocking(false);
 
 			serverChannel.socket().bind(hostSocketAddress);
 			serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
 		} catch (Exception e) {
 			logger.error("Error on ServerSocketChannel creation: " + e);
 			e.printStackTrace();
 		}
 
 		while (this.isRunning()) {
 			try {
 				this.selector.select(10000);
 
 				// Proceseaza cheile pentru care s-au produs evenimente.
 				Iterator<SelectionKey> selectedKeys = this.selector
 						.selectedKeys().iterator();
 				while (selectedKeys.hasNext()) {
 					SelectionKey key = selectedKeys.next();
 					selectedKeys.remove();
 
 					if (!key.isValid()) {
 						continue;
 					}
 
 					if (key.isConnectable()) {
 						logger.debug("Connect with key: " + key);
 						key.interestOps(key.interestOps()
 								^ SelectionKey.OP_CONNECT);
 						this.doConnect(key);
 					}
 					if (key.isAcceptable()) {
 						logger.debug("Accept with key: " + key);
 						key.interestOps(key.interestOps()
 								^ SelectionKey.OP_ACCEPT);
 						this.doAccept(key);
 					}
 
 					if (key.isWritable()) {
 						logger.debug("Write with key: " + key);
 						key.interestOps(key.interestOps()
 								^ SelectionKey.OP_WRITE);
 						this.threadPool.submit(new WriteJob(this, key));
 					}
 
 					if (key.isReadable()) {
 						logger.debug("Read with key: " + key);
 						// this.doRead(key);
 						/*
 						 * We set the key as unreadable, so no more reads from
 						 * this key until the current one is treated
 						 */
 						key.interestOps(key.interestOps()
 								^ SelectionKey.OP_READ);
 
 						this.threadPool.submit(new ReadJob(this, key));
 					}
 
 					/*
 					 * We process selection key changes here because new
 					 * selection keys can only be added by the thread running
 					 * the selector (not really sure)
 					 */
 				}
 
 				ChangeRequest creq;
 				synchronized (this.changeRequestQueue) {
 					while ((creq = this.changeRequestQueue.poll()) != null) {
 						if (creq.socketChannel == null) {
 							creq.key.interestOps(creq.newOps);
 						} else {
 							creq.socketChannel.register(this.selector,
 									creq.newOps);
 						}
 					}
 				}
 
 			} catch (Exception e) {
 				logger.error("Exceptie in thread-ul Selectorului: " + e);
 				e.printStackTrace();
 			}
 		}
 
 		// Inchide ServerSocketChannel
 		try {
 			serverChannel.close();
 		} catch (IOException e1) {
 		}
 
 		// Inchide toate SocketChannel-urile.
 		for (SocketChannel schan : this.socketChannels) {
 			try {
 				schan.close();
 			} catch (Exception e) {
 			}
 		}
 
 		logger.warn("Server stopped");
 	}
 
 	private void doConnect(SelectionKey key) {
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		try {
 			socketChannel.finishConnect();
 		} catch (IOException e) {
 			// Cancel the channel's registration with our selector
 			key.cancel();
 			return;
 		}
 
 		/*
 		 * Set the key to accept writes
 		 */
 		synchronized (this.changeRequestQueue) {
 			this.changeRequestQueue.add(new ChangeRequest(key,
 					SelectionKey.OP_WRITE));
 		}
 
 		this.selector.wakeup();
 
 	}
 
 	protected void doAccept(SelectionKey key) throws Exception {
 		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
 				.channel();
 		SocketChannel socketChannel = serverSocketChannel.accept();
 		socketChannel.configureBlocking(false);
 		socketChannel.register(this.selector, SelectionKey.OP_READ);
 
 		this.socketChannels.add(socketChannel);
 
 		if (!addressToChannel.containsKey(socketChannel.getRemoteAddress()))
 			addressToChannel.put(socketChannel.getRemoteAddress(),
 					socketChannel);
 
 		key.interestOps(key.interestOps() | SelectionKey.OP_ACCEPT);
 	}
 
 	protected void doRead(SelectionKey key) throws Exception {
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 		SocketAddress addr = socketChannel.getRemoteAddress();
 
 		ByteBuffer rBuffer = this.bufferPool.take();
 		rBuffer.clear();
 
 		int numRead;
 
 		try {
 			numRead = socketChannel.read(rBuffer);
 		} catch (Exception e) {
 			numRead = -1000000000;
 		}
 
 		/*
 		 * Inchidem toate cele
 		 */
 		if (numRead <= 0) {
 			logger.info("[NetworkCommunicator] S-a inchis socket-ul asociat cheii " + key);
 			key.channel().close();
 			key.cancel();
 			readBuffers.remove(key);
 			writeBuffers.remove(socketChannel);
 			mediator.CheckTransactionChannelClosed(socketChannel);
 			this.addressToChannel.remove(key.channel());
 			this.socketChannels.remove(key.channel());
 			return;
 		}
 
 		logger.info("[NetworkCommunicator] S-au citit " + numRead
 				+ " bytes.");
 
 		/*
 		 * Iau bufferul asociat acestei key.
 		 * Daca nu exista, instantiez un buffer nou
 		 */
 		byte[] currentBuf = rBuffer.array();
 		MessageBuffer msgBuf = readBuffers.get(key);
 
 		if (msgBuf == null) {
 			msgBuf = new MessageBuffer();
 			
 			String persaddr = mediator.getPerson(addr);
 			logger.debug("## Person with address: "+ (((InetSocketAddress) addr).getPort()) + "  is " + persaddr);
 			msgBuf.setSource(persaddr);
 			this.readBuffers.put(key, msgBuf);
 		}
 
 		msgBuf.putBytes(currentBuf, numRead);
 
 		/*
 		 * Daca sunt mesaje complete, le tratez
 		 */
 		if (msgBuf.hasMessages())
 			for (NetworkMessage nMess : msgBuf.getAndClearMessages()) {
 				nMess.setSocketChannel(socketChannel);
 				mediator.sendNetworkMessage(nMess.toMessage());
 			}
 
 		this.bufferPool.add(rBuffer);
 
 		/*
 		 * Set the key to accept further reads
 		 */
 		synchronized (this.changeRequestQueue) {
 			this.changeRequestQueue.add(new ChangeRequest(key, key
 					.interestOps() | SelectionKey.OP_READ));
 		}
 
 		this.selector.wakeup();
 
 	}
 
 	protected void doWrite(SelectionKey key) throws Exception {
 		/*
 		 * TODO: for testing purpose
 		 */
 		Thread.sleep(1000);
 		
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		ByteBuffer wBuffer = this.bufferPool.take();
 		wBuffer.clear();
 		List<InputStream> wbuf = null;
 
 		synchronized (key) {
 			wbuf = this.writeBuffers.get(socketChannel);
 			/*
 			 * Iau primul stream din care trebuie sa trimit
 			 */
 			if (wbuf.size() > 0) {
 				InputStream is = wbuf.get(0);
 				wbuf.remove(0);
 				int available = wBuffer.capacity() - wBuffer.position();
 				byte[] bbuf = new byte[available];
 				int readFromStream = is.read(bbuf, 0, available);
 
 				if (readFromStream == -1) {
 					is.close();
 				} else {
 
 					wBuffer.clear();
 					wBuffer.put(bbuf, 0, readFromStream);
 					wBuffer.flip();
 
 					int numWritten = socketChannel.write(wBuffer);
 
 					logger.info("[NetworkCommunicator] Am scris "
 							+ numWritten + " bytes pe socket-ul asociat cheii "
 							+ key);
 
 					if (numWritten < readFromStream) {
 						byte[] newBuf = new byte[bbuf.length - numWritten];
 
 						// Copiaza datele inca nescrise din bbuf in newBuf.
 						for (int i = numWritten; i < bbuf.length; i++) {
 							newBuf[i - numWritten] = bbuf[i];
 						}
 
 						/*
 						 * Adaug ce nu am putut scrie la urmatorul stream
 						 */
 						InputStream newIs = new SequenceInputStream(
 								new ByteArrayInputStream(newBuf), is);
 						wbuf.add(0, newIs);
 					} else {
 						wbuf.add(0, is);
 					}
 				}
 			}
 
 			if (wbuf.size() > 0) {
 				synchronized (this.changeRequestQueue) {
 					this.changeRequestQueue.add(new ChangeRequest(key, key
 							.interestOps() | SelectionKey.OP_WRITE));
 				}
 
 				this.selector.wakeup();
 			}
 		}
 
 		this.bufferPool.add(wBuffer);
 
 	}
 
 	public void sendMessage(NetworkMessage netMsg) {
 		String person = netMsg.getDestinationPerson();
 		InetSocketAddress addr = mediator.getPersonsAddress(person);
 		byte[] msg = netMsg.serialize();
 		
 		// FIXME: O chestie hidoasa care vrea sa faca din lipsa Webserviceului bici
 		// --- Incepe magaria ---
 		FileOutputStream fop = null;
 		File file;
 		String source = mediator.getSelf();
  
 		try {
  
 			file = new File("cine.txt");
 			fop = new FileOutputStream(file);
  
 			// if file doesnt exists, then create it
 			if (!file.exists()) {
 				file.createNewFile();
 			}
  
 			// get the content in bytes
 			byte[] contentInBytes = source.getBytes();
  
 			fop.write(contentInBytes);
 			fop.flush();
 			fop.close();
  
 			logger.debug("### Done writing my name to file");
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (fop != null) {
 					fop.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		// --- Se termina magaria ---
 		
 
 		if (addressToChannel.containsKey(addr)) { // Not tested !!!
 			SocketChannel socketChannel = addressToChannel.get(addr);
 			LinkedList<InputStream> list = writeBuffers.get(socketChannel);
 			list.add(new ByteArrayInputStream(msg));
 			synchronized (this.changeRequestQueue) {
 				this.changeRequestQueue.add(new ChangeRequest(socketChannel
 						.keyFor(selector), socketChannel.keyFor(selector)
 						.interestOps() | SelectionKey.OP_WRITE));
 			}
 			this.selector.wakeup();
 		} else {
 			try {
 				SocketChannel socketChannel = SocketChannel.open();
 				socketChannel.configureBlocking(false);
 				
 				socketChannel.connect(new InetSocketAddress(addr.getAddress(),
 						addr.getPort()));
 				
				/* null exception */
				//logger.debug("### Open a new Channel: " + ((InetSocketAddress) socketChannel.getLocalAddress()).getPort());
 
 				this.socketChannels.add(socketChannel);
 				addressToChannel.put(socketChannel.getRemoteAddress(),
 						socketChannel);
 
 				LinkedList<InputStream> list = new LinkedList<InputStream>();
 				list.add(new ByteArrayInputStream(msg));
 				writeBuffers.put(socketChannel, list);
 
 				synchronized (this.changeRequestQueue) {
 					this.changeRequestQueue.add(new ChangeRequest(
 							socketChannel, SelectionKey.OP_CONNECT));
 				}
 
 				this.selector.wakeup();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/*
 	 * Always create a new socket channel for file
 	 */
 	public void startTransaction(StartTransactionNetworkMessage netMsg,
 			FileNetworkMessage fileMess, AHFileInputStream stream,
 			Transaction trans) {
 
 		String person = netMsg.getDestinationPerson();
 		InetSocketAddress addr = mediator.getPersonsAddress(person);
 		byte[] msg = netMsg.serialize();
 		byte[] fileInfo = fileMess.serialize();
 
 		logger.info("Sending file: " + fileMess.getService() + " of "
 				+ fileMess.getSize() + " bytes");
 
 		try {
 			SocketChannel socketChannel = SocketChannel.open();
 
 			socketChannel.configureBlocking(false);
 
 			socketChannel.connect(new InetSocketAddress(addr.getAddress(), addr
 					.getPort()));
 
 			this.socketChannels.add(socketChannel);
 
 			trans.setSocketChannel(socketChannel);
 
 			LinkedList<InputStream> list = new LinkedList<InputStream>();
 			/*
 			 * We send the start transaction message, then the file info and
 			 * finally the actual file
 			 */
 			list.add(new ByteArrayInputStream(msg));
 			list.add(new ByteArrayInputStream(fileInfo));
 			list.add(stream);
 
 			System.out
 					.println("available "
 							+ (stream.available()
 									+ new ByteArrayInputStream(msg).available() + new ByteArrayInputStream(
 									fileInfo).available()));
 			writeBuffers.put(socketChannel, list);
 
 			synchronized (this.changeRequestQueue) {
 				this.changeRequestQueue.add(new ChangeRequest(socketChannel,
 						SelectionKey.OP_CONNECT));
 			}
 
 			this.selector.wakeup();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public int getPort() {
 		return hostSocketAddress.getPort();
 	}
 
 	public MessageBuffer getBuffer(SocketChannel chan) {
 		for (SelectionKey k : selector.keys()) {
 			if (k.channel().equals(chan))
 				return readBuffers.get(k);
 		}
 
 		return null;
 	}
 
 	public void CancelChannel(SelectableChannel chan) throws IOException {
 		SelectionKey key = null;
 		for (SelectionKey k : selector.keys()) {
 			if (k.channel().equals(chan)) {
 				key = k;
 			}
 		}
 		if (key != null) {
 			System.out
 					.println("[NetworkCommunicator] S-a inchis socket-ul asociat cheii "
 							+ key);
 			key.channel().close();
 			key.cancel();
 			readBuffers.remove(key);
 			writeBuffers.remove(key.channel());
 			mediator.CheckTransactionChannelClosed((SocketChannel)key.channel());
 			this.addressToChannel.remove(key.channel());
 			this.socketChannels.remove(key.channel());
 			return;
 		}
 	}
 
 }
