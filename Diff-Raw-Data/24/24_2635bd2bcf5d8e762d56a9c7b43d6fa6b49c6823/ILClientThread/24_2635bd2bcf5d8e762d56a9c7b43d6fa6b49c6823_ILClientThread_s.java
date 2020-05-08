 package jig.ironLegends.oxide.client;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import jig.ironLegends.CommandState;
 import jig.ironLegends.oxide.exceptions.PacketFormatException;
 import jig.ironLegends.oxide.packets.ILGameStatePacket;
 import jig.ironLegends.oxide.packets.ILLobbyPacket;
 import jig.ironLegends.oxide.packets.ILPacket;
 import jig.ironLegends.oxide.packets.ILPacketFactory;
 import jig.ironLegends.oxide.packets.ILReadyPacket;
 import jig.ironLegends.oxide.packets.ILServerAdvertisementPacket;
 import jig.ironLegends.oxide.packets.ILStartGamePacket;
 import jig.ironLegends.oxide.sockets.ILAdvertisementSocket;
 import jig.ironLegends.oxide.util.ChangeRequest;
 
 /**
  * @author Travis Hall
  */
 public class ILClientThread implements Runnable {
 	// The host:port combination to listen on
 	private InetAddress hostAddress;
 	private int port;
 	
 	// The selector we monitor
 	private Selector selector;
 	
 	// The advertisement socket we listen on
 	private ILAdvertisementSocket advertSocket;
 	
 	private SocketChannel serverChannel;
 	
 	// A map of servers discovered by the aSocket
 	public Map<InetSocketAddress, ILServerAdvertisementPacket> servers;
 	
 	private List<ChangeRequest> pendingChanges;
 	
 	public List<ILGameStatePacket> stateUpdates;
 	public List<ILPacket> outgoingData;
 	public ILLobbyPacket lobbyState;
 	
 	private boolean lookingForServers;
 	private boolean active;
 	
 	public boolean receivedStartGame = false;
 	public ILStartGamePacket startGamePacket;
 	
 	private int tickrate;
 	private long lastTick = 0;
 	private long time = 0;
 	
 	private int packetID = 0;
 	
 	public ILClientThread(int tickrate) 
 			throws SocketException, IOException 
 	{
 		this.tickrate = tickrate;
 		
 		this.advertSocket = new ILAdvertisementSocket("230.0.0.1", 5000);
 		this.selector = this.initSelector();
 		this.lookingForServers = false;
 		this.active = false;
 		
 		this.servers = new HashMap<InetSocketAddress, ILServerAdvertisementPacket>();
 		this.pendingChanges = new LinkedList<ChangeRequest>();
 		this.stateUpdates = new LinkedList<ILGameStatePacket>();
 		this.outgoingData = new LinkedList<ILPacket>();
 	}
 	
 	public int packetID() {
 		if (this.packetID == Integer.MAX_VALUE) this.packetID = 0;
 		return this.packetID++;
 	}
 	
 	public void update(long deltaMs) {
 		this.time += deltaMs;
 	}
 	
 	public boolean tickExpired() {
 		return (this.time - this.lastTick > this.tickrate);
 	}
 	
 	public void connectTo(InetAddress hostAddress, int port) throws IOException {
 		this.hostAddress = hostAddress;
 		this.port = port;
 		
 		this.serverChannel = SocketChannel.open();
 		this.serverChannel.configureBlocking(false);
 		
 		// Kick off our connection establishment
 		boolean ic = this.serverChannel.connect(new InetSocketAddress(this.hostAddress, this.port));
 		
 		// Queue a channel registration since the caller is not the selecting
 		// thread. As part of the registration, we'll register an interest in 
 		// connection events. These are raised when a channel is ready to
 		// complete connection establishment.
 		// If we connected immediately (as we would with a local host)
 		// we can immediately change the key to read interest
 		synchronized (this.pendingChanges) {
 			if (ic) {
 				this.pendingChanges.add(new ChangeRequest(this.serverChannel, 
 						ChangeRequest.CHANGEOPS, SelectionKey.OP_READ));
 			} else {
 				this.pendingChanges.add(new ChangeRequest(this.serverChannel, 
 					ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		while(true) {
 			if (!active) {
 				try {
 					// Apparently need this or else everything breaks? What the hell?
 					Thread.sleep(50L);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				continue;
 			}
 			try {
 				// Process any pending changes
 				synchronized(this.pendingChanges) {
 					Iterator<ChangeRequest> changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
 						ChangeRequest change = changes.next();
 						switch (change.type){
 						case ChangeRequest.CHANGEOPS:
 							SelectionKey key = change.socket.keyFor(this.selector);
 							key.interestOps(change.ops);
 							break;
 						case ChangeRequest.REGISTER:
 							change.socket.register(this.selector, change.ops);
 							break;
 						}
 					}
					this.pendingChanges.clear();
 				}
 				
 				if (lookingForServers) {
 					try{
 						ILServerAdvertisementPacket aPacket = (ILServerAdvertisementPacket) advertSocket.getMessage();
 						servers.put(aPacket.address, aPacket);
 					} catch (SocketTimeoutException e) {
 						;
 					}
 				}
  				
 				// Wait for an event on one of the registered channels
 				this.selector.select(1);
 				
 				// Iterate over the set of keys for which events are available
 				Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
 				while (selectedKeys.hasNext()) {
 					SelectionKey key = selectedKeys.next();
 					selectedKeys.remove();
 					
 					if (!key.isValid()) {
 						continue;
 					}
 					
 					// Check what event is available and deal with it
 					if (key.isConnectable()) {
 						this.finishConnection(key);
 					} else if (key.isReadable()) {
 						this.read(key);
 					} else if (key.isWritable()) {
 						this.write(key);
 					}
 				} 
 			} catch (Exception e1) {
 				Logger.getLogger("global").warning(e1.toString());
 			}
 		}
 	}
 	
 	public void send(CommandState event) throws IOException {
 		ILPacket p = ILPacketFactory.newEventPacket((int) this.packetID(), event);
 		this.send(p);
 	}
 	
 	public void send(ILPacket packet) throws IOException {
 		
 		synchronized(this.pendingChanges) {
 			this.pendingChanges.add(new ChangeRequest(this.serverChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
 			
 			synchronized(this.outgoingData) {
 				this.outgoingData.add(packet);
 			}
 		}
 		
 		this.selector.wakeup();
 	}
 	
 	public void sendReadyPacket() throws IOException {
 		synchronized(this.outgoingData) {
 			ILReadyPacket p = ILPacketFactory.newReadyPacket((int) this.packetID());
 			this.receivedStartGame = false;
 			this.outgoingData.add(p);
 		}
 	}
 	
 	private void read(SelectionKey key) throws IOException {
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 		
 		ByteBuffer readBuffer = ByteBuffer.allocate(ILPacket.MAX_PACKET_SIZE);
 		
 		int numRead;
 		try {
 			numRead = socketChannel.read(readBuffer);
 		} catch (IOException e) {
 			// The remote forcibly closed the connection. Cancel the selection key
 			// and close the channel
 			Logger.getLogger("global").severe("Remote server forcibly closed a connection on socket: " + socketChannel.toString());
 			key.cancel();
 			socketChannel.close();
 			return;
 		}
 		
 		if (numRead == -1) {
 			// Remote entity shut the socket down cleanly. Do the same from our end
 			// and cancel the channel.
 			key.channel().close();
 			key.cancel();
 			return;
 		}
 		
 		readBuffer.flip();
 		this.handleResponse(socketChannel, readBuffer, numRead);
 	}
 	
 	private void handleResponse(SocketChannel socketChannel, ByteBuffer data, int numRead)  
 	{	
 		ILPacket p = null;
 		try {
 			p = ILPacketFactory.getPacketFromData(data);
 		} catch (PacketFormatException e) {
 			Logger.getLogger("global").warning("Received garbage packet");
 			return;
 		}
 		
 		if (p instanceof ILGameStatePacket) {
 			synchronized(this.stateUpdates) {
 				this.stateUpdates.add((ILGameStatePacket) p);
 			}
 		} else if (p instanceof ILLobbyPacket) {
 			if (this.lobbyState == null) this.lobbyState = (ILLobbyPacket) p;
 			else {
 				synchronized(this.lobbyState) {
 					this.lobbyState = (ILLobbyPacket) p;
 				}
 			}
 		} else if (p instanceof ILStartGamePacket) {
 			this.receivedStartGame = true;
 			this.startGamePacket = (ILStartGamePacket) p;
 		}
 		
 	}
 	
 	private void write(SelectionKey key) throws IOException {
 	SocketChannel socketChannel = (SocketChannel) key.channel();
 		if (this.tickExpired()) {
 			this.lastTick = this.time;
 			synchronized (this.outgoingData) {
 				// Write until there's no more data
 				while (!this.outgoingData.isEmpty()) {
 					ILPacket packet = this.outgoingData.get(0);
 					socketChannel.write(packet.getByteBuffer());
 					this.outgoingData.remove(0);
 				}
 				
 				if (this.outgoingData.isEmpty()) {
 					// We wrote all the data off, so we no longer are interested
 					// in writing on this socket. Switch back to waiting for data.
 					key.interestOps(SelectionKey.OP_READ);
 				}
 			}
 		}
 	}
 	
 	private void finishConnection(SelectionKey key) throws IOException {
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 		
 		// Finish the connection. If the connection operation failed
 		// this will raise an IOException
 		try {
 			socketChannel.finishConnect();
 		} catch (IOException e) {
 			// Cancel the chanel's registration with our selector
 			key.cancel();
 			e.printStackTrace();
 			return;
 		}
 		
 		// Register an interest in reading from this channel
 		key.interestOps(SelectionKey.OP_READ);
 	}
 	
 	private Selector initSelector() throws IOException {
 		return SelectorProvider.provider().openSelector();
 	}
 
 	/**
 	 * @param b
 	 */
 	public void setActive(boolean b) {
 		this.active = b;
 	}
 	
 	public void setLookingForServers(boolean b) {
 		this.lookingForServers = b;
 	}
 	
 }
