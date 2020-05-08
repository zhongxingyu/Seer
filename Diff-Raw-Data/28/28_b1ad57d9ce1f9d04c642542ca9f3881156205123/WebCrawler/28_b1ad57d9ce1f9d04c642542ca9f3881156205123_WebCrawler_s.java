 package test;
 
 import java.net.*;
 import java.nio.*;
 import com.zhuri.slot.*;
 import java.nio.channels.*;
 import com.zhuri.ssl.EmotionSSLChannel;
 
 public class WebCrawler {
 	URL mUrl;
 	SocketChannel socketChannel;
 	EmotionSSLChannel mEmotionSSLChannel;
 
 	SlotWait mReadBlock = new SlotWait() {
 		public void invoke() {
 			long count = -1;
 			System.out.println("read event"); 
 			try {
 				ByteBuffer buffer = ByteBuffer.allocate(80000);
 				count = mEmotionSSLChannel.read(buffer);
 				System.out.println(new String(buffer.array(), 0, (int)count));
 			} catch (Exception e) {
 				close();
 				return;
 			}
 
 			if (count != -1) {
 				mEmotionSSLChannel.selectIn(mReadBlock);
 				return;
 			}
 
 			System.out.println("stream is close");
 			close();
 			return;
 		}
 	};
 
 	SlotWait mWriteBlock = new SlotWait() {
 		public void invoke() {
 			int count;
 			System.out.println("write event");
 			try {
 				String header = "GET " + mUrl.getFile() + " HTTP/1.0\r\nHost: " + mUrl.getHost() + "\r\n\r\n";
 				ByteBuffer buffer = ByteBuffer.wrap(header.getBytes());
 				count = mEmotionSSLChannel.write(buffer);
 			} catch (Exception e) {
 				e.printStackTrace();
 				close();
 			}
 		}
 	};
 
 	SlotWait mProxyBlock = new SlotWait() {
 		public void invoke() {
 
 			long count = -1;
 			String title = "";
 			System.out.println("proxy read event");
 
 			try {
 				ByteBuffer buffer = ByteBuffer.allocate(8000);
 				count = mEmotionSSLChannel.read(buffer);
 				title = new String(buffer.array(), 0, (int)count);
 				System.out.println("Proxy Response: " + title);
 
 				if (title.endsWith("\r\n\r\n")) {
 					if (mUrl.getProtocol().equals("https")) {
 						System.out.println("handshake event");
 						mEmotionSSLChannel.handshake();
 					}
 					mEmotionSSLChannel.selectIn(mReadBlock);
 					mEmotionSSLChannel.selectOut(mWriteBlock);
 					return;
 				}
 			} catch (Exception e) {
 				close();
 				return;
 			}
 
 			if (count != -1) {
 				mEmotionSSLChannel.selectIn(mProxyBlock);
 				return;
 			}
 
 			System.out.println("stream is close");
 			close();
 		}
 	};
 
 	SlotWait mConnBlock = new SlotWait() {
 		public void invoke() {
 			String xyHead =
			"CONNECT " + mUrl.getAuthority() + " HTTP/1.0\r\n" +
 			"Proxy-Authorization: Basic cHJveHk6QWRabkdXVE0wZExUCg==\r\n" +
 			"\r\n";
 
 			try {
				System.out.println("Conntion block is OK");
 				socketChannel.finishConnect();
 
 				ByteBuffer buffer = ByteBuffer.wrap(xyHead.getBytes());
 				socketChannel.write(buffer);
 
 				mEmotionSSLChannel.selectIn(mProxyBlock);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 	};
 
 	public WebCrawler(String url) throws Exception {
 		mUrl = new URL(url);
 		socketChannel = SocketChannel.open();
 		mEmotionSSLChannel = new EmotionSSLChannel(socketChannel);
 	}
 
 	public void start() {
 		InetAddress address;
 
 		try {
 			address = InetAddress.getByName("223.167.213.254");
 			mEmotionSSLChannel.selectOut(mConnBlock);
 			socketChannel.connect(new InetSocketAddress(address, 9418));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void close() {
 		mWriteBlock.clean();
 		mReadBlock.clean();
 	}
 }
 
