 package network;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 
 public class WriteThread extends Thread{
 	private Message msg;
 	private String ip;
 	private int port;
 	private static int BUF_SIZE = 1024;
	private boolean running = true;
 	
 	public WriteThread(String ip, int port, Message msg) {
 		this.msg = msg;
 		this.ip = ip;
 		this.port = port;
 	}
 	
 	
	public void connect(SelectionKey key) throws IOException {
 		
 		System.out.print("CONNECT: ");
 		
 		SocketChannel socketChannel = (SocketChannel)key.channel();
 		if (! socketChannel.finishConnect()) {
 			System.err.println("Eroare finishConnect");
 			running = false;
 		}
 		
 		//socketChannel.close();
 		key.interestOps(SelectionKey.OP_WRITE);
 	}
 	
 	
	public void write(SelectionKey key) throws IOException {
 		
 		System.out.println("WRITE: ");
 		
 		ByteBuffer buf = (ByteBuffer)key.attachment();		
 		SocketChannel socketChannel = (SocketChannel)key.channel();
 		
 		while (socketChannel.write(buf) > 0);
 		
 		//socketChannel.close();
 		running = false;
 		
 		/*if (! buf.hasRemaining()) {
 			socketChannel.close();
 			running = false;
 		}*/
 	}
 	
 	@Override 
 	public void run () {
 		Selector selector			= null;
 		SocketChannel socketChannel	= null;
 		try {
 			selector = Selector.open();
 			
 			socketChannel = SocketChannel.open();
 			socketChannel.configureBlocking(false);
 			socketChannel.connect(new InetSocketAddress(this.ip, this.port));
 			
 			ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
 			
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			ObjectOutput out = null;
 			byte[] data;
 			
 			try {
 				  out = new ObjectOutputStream(bos);   
 				  out.writeObject(this.msg);
 				  data = bos.toByteArray();
 			} finally {
 				out.close();
 				bos.close();
 			}
 			
 			buf.put(data);
 			buf.flip();
 			
			
 			socketChannel.register(selector, SelectionKey.OP_CONNECT, buf);
 			
			
 			while (running) {
 				selector.select();
 				
 				for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
 					SelectionKey key = it.next();
 					it.remove();
 					
 					if (key.isConnectable())
 						connect(key);
 					else if (key.isWritable())
 						write(key);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			
 		} finally {
 			if (selector != null)
 				try {
 					selector.close();
 				} catch (IOException e) {}
 			
 			if (socketChannel != null)
 				try {
 					socketChannel.close();
 				} catch (IOException e) {}
 		}
 	}
 }
