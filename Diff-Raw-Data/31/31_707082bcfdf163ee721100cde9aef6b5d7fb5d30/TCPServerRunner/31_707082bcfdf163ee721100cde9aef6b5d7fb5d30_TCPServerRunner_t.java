 package com.king.cai.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 
 import com.king.cai.KingCAIConfig;
 
 import android.os.Handler;
 
 public class TCPServerRunner  extends FirableRunner{
     private ServerSocket mTcpSocket = null;
 	private ByteBuffer mReceiveBuf = ByteBuffer.allocate(8192);
 	private int mPort = 0;
 	
 	public TCPServerRunner(Handler innerHandler, int port) {
 		super(innerHandler);
 		mPort = port;
 		if (mTcpSocket == null){
 			try{ 
 				mTcpSocket = new ServerSocket(mPort);
 				mTcpSocket.setReceiveBufferSize(8192);
 				mTcpSocket.setReuseAddress(true);
 			}catch (IOException e){
				KingService.getLogService().addLog(e.toString());				
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	@Override
 	protected void onStart() {
 	}
 
 	@Override
 	protected void doRun() {	
 		Socket socket = null;
 		try{
 		    socket = mTcpSocket.accept();
 		    
 		    InputStream in = socket.getInputStream();
 		    mReceiveBuf.clear();
 		    while (in.read(mReceiveBuf.array()) > 0){
 		    	String msg = new String(mReceiveBuf.array(), KingCAIConfig.mCharterSet);
 
		    	fireMessage(contructTextBundle(socket.getInetAddress().getHostAddress(), 
		    			ByteBuffer.wrap(msg.getBytes()), ByteBuffer.wrap(msg.getBytes()).capacity()));
 			    mReceiveBuf.clear();
 		    } 
 		    in.close();
 		} catch (IOException e) {
 			e.printStackTrace();  
 		}finally{
 			if (socket != null){
 				try {
 					socket.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}			
 	}
 
 	@Override
 	protected void onExit() {
 
 		try {
 			if (mTcpSocket != null) {
 				mTcpSocket.close();
 				mTcpSocket = null;
 			}
 		} catch (IOException e) {
			KingService.getLogService().addLog(e.toString());			
 			e.printStackTrace();
 		}  
 		
 	}
 
 }
