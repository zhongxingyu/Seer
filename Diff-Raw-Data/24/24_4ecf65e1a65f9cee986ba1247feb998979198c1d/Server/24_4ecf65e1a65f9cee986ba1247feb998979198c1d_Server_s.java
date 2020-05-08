 package server;
 
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.net.*;
 import java.util.*;
 
 import app.Log;
 
 
 import network.C2SMessage;
 import network.Message;
 
 public class Server {
 	public final int PORT		= 7777;		// server port
 
 	ServerMediator m;
 	Vector<SocketChannel> sockets;
 	public Server() {
 		m = new ServerMediator(this);
 		sockets = new Vector<SocketChannel>();
 	}
 
 	public void accept(SelectionKey key) throws IOException {
 
 		ServerSocketChannel serverSocketChannel = null; // initialize from key
 		SocketChannel socketChannel = null;				// initialize from accept
 
 		Selector select = key.selector();
 
 		DataContainer cont = new DataContainer();
 		serverSocketChannel = (ServerSocketChannel) key.channel();
 		socketChannel = serverSocketChannel.accept();
 		socketChannel.configureBlocking(false);
 		socketChannel.register(select, SelectionKey.OP_READ, cont);
 		// display remote client address
 		Log.debug("Connection from: " + socketChannel.socket().getRemoteSocketAddress());
 		sockets.add(socketChannel);
 	}
 

 
 	public void read(final SelectionKey key)  {
 		DataContainer data		= (DataContainer)key.attachment();		
 		SocketChannel socket	= (SocketChannel)key.channel();
 		int bytesRead = 0;
 		try {
 
 			while (data.lengthByteBuffer.remaining()!=0) {
 				bytesRead = socket.read(data.lengthByteBuffer);
 				if (bytesRead < 0 ) {
 					cleanup(key);
 					return;
 				}
 			}
 
 			data.dataByteBuffer = ByteBuffer.allocate(data.lengthByteBuffer.getInt(0));
 			data.lengthByteBuffer.clear();
 
 			while (data.dataByteBuffer.remaining()!=0) {
 				bytesRead = socket.read(data.dataByteBuffer);
 				if (bytesRead < 0 ) {
 					cleanup(key);
 					return;
 				}
 			}
 
 			Log.debug("READ: " + data.dataByteBuffer);
 			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data.dataByteBuffer.array()));
 			Serializable ret = (Serializable) ois.readObject();
 			// clean up
 			data.dataByteBuffer = null;
 			data.readLength = true;
 			((C2SMessage)ret).execute(m, key);
 		} catch (Exception e) {
 			Log.error("Disconnect: "+e);
 			cleanup(key);
 		}
 	}
 	
 	public void cleanup(SelectionKey key) {
 		
 		Log.debug("CLEANUP");
 		SocketChannel chan = (SocketChannel) key.channel();
 		try {
 			chan.close();
 		} catch (IOException e) {
 			Log.error("Socket Close error: "+e);
 		}
 		sockets.remove(chan);
 		m.disconnectUser(chan);	
 	}
 
 	public void write(SelectionKey key, Message m){
 		write((SocketChannel)key.channel(),m);
 	}
 
 	public void write(SocketChannel	chan, Message m){
 		if (!chan.isConnected()) {
 			System.out.println("Channel CLOSED");
 			sockets.remove(chan);
 			return;
 		}
 		try {
 			ByteArrayOutputStream bs = new ByteArrayOutputStream();
 			for(int i=0;i<4;i++) bs.write(0);
 			ObjectOutputStream os = new ObjectOutputStream(bs);
 			os.writeObject(m);
 			os.close();
 			ByteBuffer wrap = ByteBuffer.wrap(bs.toByteArray());
 			wrap.putInt(0, bs.size()-4);
 			int bytesOut = 0;
 			while (bytesOut<bs.size()) {
 				int out =  chan.write(wrap);
 				if (out<0) { 
 					cleanup(chan.keyFor(selector));
 					return;
 				}
 				bytesOut += out;
 			}
 			Log.debug("WRITE:" + bytesOut);
 		} catch (Exception e) {
 			Log.error("Disconnect: "+e);
 			cleanup(chan.keyFor(selector));
 		}
 	}
 
 
 	public void broadcast(Message m) {
 		for (SocketChannel sock : sockets)
 			write(sock,m);
 
 	}
 
 	Selector selector;
 	public void exec() {
 
 		//Selector selector						= null;
 		ServerSocketChannel serverSocketChannel	= null;
 
 			try {
 			selector = Selector.open();
 
 			serverSocketChannel = ServerSocketChannel.open();
 			serverSocketChannel.configureBlocking(false);
 			serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
 			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
 			} catch (Exception e) {
 				System.err.println(e);
 				System.exit(1);
 			}
 			// main loop
 			while (true) {
 				// wait for something to happen
 				try {
 					selector.select();
 					// iterate over the events
 					for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
 						// get current event and REMOVE it from the list!!!
 						SelectionKey key = it.next();
 						it.remove();
 						if (key.isAcceptable())
 							accept(key);
 						else if (key.isReadable())
 							read(key);	
 					}
 				} catch (Exception e) {
 					Log.error(e.toString());
 					System.err.println(e.toString());
 				}
 			}
 
 	}
 
 	public static void main(String args[]) throws Exception {
 		Server s = new Server();
 		s.exec();
 	}
 
 }
 
 class DataContainer {
 	ByteBuffer buf = null;
 	ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
 	boolean readLength = true;
 	ByteBuffer dataByteBuffer = null;
 }
