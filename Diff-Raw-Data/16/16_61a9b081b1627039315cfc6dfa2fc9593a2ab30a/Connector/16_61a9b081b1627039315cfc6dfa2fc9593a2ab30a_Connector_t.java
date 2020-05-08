 package com.zhuri.net;
 
 import java.net.*;
 import java.nio.*;
 import com.zhuri.slot.*;
 import java.nio.channels.*;
 
 import java.io.IOException;
 
 public class Connector implements IWaitableChannel, IConnectable {
 	boolean sCancel;
 	SlotChannel sChannel;
 	SocketChannel mSocket;
 
 	public Connector() {
 		sCancel = false;
 		try {
 			mSocket = SocketChannel.open();
 			sChannel = SlotChannel.open(mSocket);
 		} catch (IOException e) {
 			e.printStackTrace();
 			sCancel = true;
 		}
 	}
 
 	/* mSocket.finishConnect(); */
 	public void connect(String target) {
 		String[] parts;
 
 		try {
 			parts = target.split(":");
 			mSocket.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
 		} catch (UnresolvedAddressException e) {
 			sChannel.wakeupall();
 			e.printStackTrace();
 			sCancel = true;
 		} catch (IOException e) {
 			sChannel.wakeupall();
 			e.printStackTrace();
 			sCancel = true;
 		}
 	}
 
 	public void waitI(SlotWait wait) {
 		sChannel.wantIn(wait);
 		if (sCancel)
 			sChannel.wakeupall();
 	}
 
 	public void waitO(SlotWait wait) {
 		sChannel.wantOut(wait);
 		if (sCancel)
 			sChannel.wakeupall();
 	}
 
 	private void checkConnectException(SocketChannel channel) throws IOException {
 		try {
 			channel.finishConnect();
		} catch (NoRouteToHostException e) {
			throw new IOException(e);
 		} catch (NoConnectionPendingException e) {
 			throw new IOException(e);
 		}
 	}
 
 	public long read(ByteBuffer dst) throws IOException {
 		checkConnectException(mSocket);
 		return mSocket.read(dst);
 	}
 
 	public long write(ByteBuffer src) throws IOException {
 		checkConnectException(mSocket);
 		return mSocket.write(src);
 	}
 
 	public void close() throws IOException {
 		if (sChannel != null)
 			sChannel.detach();
 		if (mSocket != null)
 			mSocket.close();
 	}
 }
 
